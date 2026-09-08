import { defineStore } from 'pinia'
import { login, logout as logoutApi, claimShuntingReward, getUserLevelInfo } from '@/api/user'
import { getGameState, importLegacyGameState } from '@/api/GameApi'

// 安全的localStorage操作工具
const storage = {
  get(key) {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : null
    } catch (error) {
      console.warn(`Failed to parse localStorage item: ${key}`, error)
      localStorage.removeItem(key) // 清除损坏的数据
      return null
    }
  },

  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error(`Failed to set localStorage item: ${key}`, error)
    }
  },

  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error(`Failed to remove localStorage item: ${key}`, error)
    }
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: storage.get('userInfo'),
    token: storage.get('token') || '',
    /** 气血等级缓存（/user/level/{id}），用于导航解锁 */
    cachedLevelInfo: null,
    gameState: null
  }),
  getters: {
    // 判断是否登录（只检查token是否存在）
    isLoggedIn: (state) => !!state.token && !!state.userInfo,

    // 获取用户类型
    userType: (state) => state.userInfo?.userType || '',

    // 判断是否是管理员
    isAdmin: (state) => state.userInfo?.userType === 'ADMIN',

    // 判断是否是普通用户
    isUser: (state) => state.userInfo?.userType === 'USER',

    // 获取用户显示名称
    displayName: (state) => {
      if (!state.userInfo) return '未登录'
      return state.userInfo.nickname || state.userInfo.username || '用户'
    },

    // 获取用户头像
    avatar: (state) => state.userInfo?.avatar || '',

    // 获取用户ID
    userId: (state) => state.userInfo?.id || null,

    // 获取用户气血能量
    userScore: (state) => state.userInfo?.score || 0
  },

  actions: {
    // 初始化用户状态
    initialize() {
      // 检查登录状态有效性
      if (!this.isLoggedIn) {
        this.clearUserInfo()
      }
    },

    // 设置用户信息（登录时调用）
    setUserInfo(data) {
      if (!data || !data.token) {
        console.error('setUserInfo: 数据或token不能为空')
        return
      }

      // 更新状态
      this.userInfo = data.userInfo || data
      this.token = data.token

      // 持久化存储
      storage.set('userInfo', this.userInfo)
      storage.set('token', this.token)
    },

    // 更新用户信息（不更新token）
    updateUserInfo(data) {
      if (!data) {
        console.warn('updateUserInfo: 传入数据为空')
        return
      }

      // 合并用户信息
      this.userInfo = { ...this.userInfo, ...data }

      // 更新存储
      storage.set('userInfo', this.userInfo)
    },

    // 清除用户信息
    clearUserInfo() {
      this.userInfo = null
      this.token = ''
      this.cachedLevelInfo = null
      this.gameState = null

      // 清除存储
      storage.remove('userInfo')
      storage.remove('token')
    },

    // 登录
    async login(loginForm) {
      try {
        const res = await login(loginForm)

        if (!res || !res.token) {
          throw new Error('登录响应数据异常')
        }

        this.setUserInfo(res)
        await this.migrateLegacyGameState()
        await this.loadGameState({ showDefaultMsg: false }).catch(() => {})
        await this.ensureLevelInfo({ showDefaultMsg: false }).catch(() => {})
        return res
      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },

    // 退出登录
    async logout() {
      try {
        if (this.token) {
          await logoutApi({ showDefaultMsg: false }).catch(() => {})
        }
        this.clearUserInfo()
        console.log('用户已退出登录')
      } catch (error) {
        console.error('退出登录失败:', error)
        // 即使出错也要清除用户信息
        this.clearUserInfo()
      }
    },

    // 每日首次完成调车观察挑战时领取服务端固定奖励。
    async claimShuntingReward(requestConfig = {}) {
      try {
        if (!this.userId) {
          throw new Error('用户未登录')
        }

        const res = await claimShuntingReward(requestConfig)

        if (res && res.id != null && res.id === this.userId) {
          this.updateUserInfo({ ...this.userInfo, ...res })
        } else if (res && res.userInfo) {
          this.updateUserInfo(res.userInfo)
        }

        await this.ensureLevelInfo({ showDefaultMsg: false }).catch(() => {})
        if (typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('user-level-refresh'))
        }

        return res
      } catch (error) {
        console.error('领取调车挑战奖励失败:', error)
        throw error
      }
    },

    /**
     * 拉取并缓存等级信息（导航解锁、我的小世界等共用）
     */
    async ensureLevelInfo(requestConfig = {}) {
      if (!this.userId) {
        this.cachedLevelInfo = null
        return null
      }
      try {
        const res = await getUserLevelInfo(this.userId, requestConfig)
        this.cachedLevelInfo = res
        if (res != null && typeof res.currentScore === 'number' && this.userInfo) {
          this.updateUserInfo({ score: res.currentScore })
        }
        return res
      } catch (e) {
        console.error('ensureLevelInfo failed', e)
        return this.cachedLevelInfo
      }
    },

    async loadGameState(requestConfig = {}) {
      if (!this.userId) {
        this.gameState = null
        return null
      }
      const state = await getGameState(requestConfig)
      this.gameState = state
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('game-state-refresh', { detail: state }))
      }
      if (state && typeof state.currentScore === 'number') {
        this.updateUserInfo({ score: state.currentScore })
      }
      return state
    },

    async migrateLegacyGameState() {
      if (!this.userId || typeof window === 'undefined') return null

      const tasks = []
      const pushTask = (taskCode, progress = 1, completed = true, periodKey = 'lifetime', rewardClaimed = false) => {
        if (!taskCode || tasks.some((item) => item.taskCode === taskCode && item.periodKey === periodKey)) return
        tasks.push({ taskCode, progress, completed, periodKey, rewardClaimed })
      }

      try {
        const raw = window.localStorage.getItem('xinglin-game-state-v3') ||
          window.localStorage.getItem('xinglin-game-state-v2')
        const state = raw ? JSON.parse(raw) : {}
        const completed = Array.isArray(state.completedTaskIds) ? state.completedTaskIds : []
        completed.forEach((taskCode) => {
          if (['daily-read-copper-story', 'daily-safety-quiz', 'main-safety-case',
            'main-mist-in-xinglin', 'main-hand-star-map', 'meridian-river-completion',
            'copper-man-daily-case', 'review-daily', 'main-repair-agency'].includes(taskCode)) {
            pushTask(taskCode)
          }
        })

        const river = JSON.parse(window.localStorage.getItem('xinglin-meridian-river-progress-v4') || '{}')
        const claimedRoutes = JSON.parse(window.localStorage.getItem('xinglin-meridian-river-claimed-v4') || '{}')
        const routeTargets = {
          'meridian-lung-route': 5,
          'meridian-large-intestine-route': 4,
          'meridian-stomach-route': 5,
          'meridian-spleen-route': 5,
          'meridian-heart-route': 4,
          'meridian-small-intestine-route': 4,
          'meridian-bladder-route': 5,
          'meridian-kidney-route': 4,
          'meridian-pericardium-route': 4,
          'meridian-sanjiao-route': 5,
          'meridian-gallbladder-route': 6,
          'meridian-liver-route': 4,
          'meridian-ren-route': 5,
          'meridian-du-route': 5
        }
        Object.entries(routeTargets).forEach(([routeId, target]) => {
          const progress = Math.max(0, Math.min(target, Number(river[routeId] || 0)))
          const claimed = Boolean(claimedRoutes[routeId])
          if (progress > 0 || claimed) {
            const slug = routeId.slice('meridian-'.length, -'-route'.length)
            pushTask(`meridian-route-${slug}`, claimed ? target : progress, claimed || progress >= target, 'lifetime', claimed)
          }
        })

        const matchClaims = JSON.parse(window.localStorage.getItem('meridian-match-claimed') || '{}')
        const matchClaimMap = Array.isArray(matchClaims) ? { normal: matchClaims } : matchClaims
        Object.entries(matchClaimMap).forEach(([difficulty, scores]) => {
          if (!['easy', 'normal', 'hard'].includes(difficulty) || !Array.isArray(scores)) return
          scores.forEach((score) => {
            if ([50, 100, 200, 350, 500, 750, 1000].includes(Number(score))) {
              pushTask(`meridian-match-${difficulty}-${Number(score)}`, 1, true, 'lifetime', true)
            }
          })
        })

        const sortClaims = JSON.parse(window.localStorage.getItem('meridian-sort-claimed') || '[]')
        if (Array.isArray(sortClaims)) {
          sortClaims.forEach((score) => {
            if ([50, 100, 150, 200, 250, 300].includes(Number(score))) {
              pushTask(`meridian-sort-${Number(score)}`, 1, true, 'lifetime', true)
            }
          })
        }

        const guestCaseKeys = []
        for (let index = 0; index < window.localStorage.length; index += 1) {
          const key = window.localStorage.key(index)
          if (key && key.startsWith('copper-man-case:')) guestCaseKeys.push(key)
        }
        guestCaseKeys.forEach((key) => {
          const daily = JSON.parse(window.localStorage.getItem(key) || '{}')
          const periodKey = key.slice('copper-man-case:'.length) || 'lifetime'
          if (daily.awarded || (Array.isArray(daily.codes) && daily.codes.length >= 3)) {
            pushTask('copper-man-daily-case', 1, true, periodKey)
          }
        })
      } catch (error) {
        console.warn('读取旧游戏状态失败，保留本地数据等待下次迁移', error)
      }

      return importLegacyGameState({
        schemaVersion: 4,
        sourceKey: 'xinglin-game-state-v4',
        tasks
      }, {
        idempotencyKey: `legacy-import:${this.userId}:v4`
      })
    },

    // 获取用户气血能量等级信息
    async getLevelInfo() {
      try {
        if (!this.userId) {
          throw new Error('用户未登录')
        }

        const res = await getUserLevelInfo(this.userId)
        this.cachedLevelInfo = res
        if (res != null && typeof res.currentScore === 'number' && this.userInfo) {
          this.updateUserInfo({ score: res.currentScore })
        }
        return res
      } catch (error) {
        console.error('获取等级信息失败:', error)
        throw error
      }
    },

  }
})

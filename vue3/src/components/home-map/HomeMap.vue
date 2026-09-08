<template>
  <main class="detective-home" tabindex="0" ref="shellRef" @keydown="handleKeydown">
    <header class="detective-topbar" aria-label="小铜人侦探社首页栏">
      <button type="button" class="brand-lockup" @click="openHomeMap">
        <img class="brand-avatar" :src="brandGuideImage" alt="" aria-hidden="true" />
        <span class="brand-copy">
          <strong>小铜人侦探社</strong>
          <small>小铜人中医侦探社</small>
        </span>
      </button>

      <div class="case-title" aria-label="当前探案地图">
        <h1>杏林探案地图：{{ currentLevel.label }}</h1>
      </div>

      <div class="topbar-actions">
        <button type="button" class="home-guide-button" aria-label="重新查看小铜人引导" @click="openIntroGuide">
          <img :src="brandGuideImage" alt="" aria-hidden="true" />
          <span>小铜人引导</span>
        </button>
        <button type="button" class="study-mode-button" aria-label="当前为游学模式" @click="openHomeMap">
          <i class="fa-solid fa-map-location-dot" aria-hidden="true"></i>
          <strong>游学模式</strong>
        </button>
        <button
          type="button"
          class="home-bell-button"
          :aria-label="userStore.isLoggedIn ? '进入安全课堂' : '安全课堂（登录后解锁）'"
          :title="userStore.isLoggedIn ? '进入安全课堂' : '登录后进入安全课堂'"
          @click="openSafety"
        >
          <img :src="materialSafetyBell" alt="" aria-hidden="true" />
        </button>
      </div>

      <div class="safety-strip" aria-label="安全提醒">
        <img :src="materialBadge" alt="" aria-hidden="true" />
        <span>只观察、只学习，不自己针刺。</span>
      </div>
    </header>
    <section class="detective-board" aria-label="探案地图主区域">
      <aside class="honor-archive-card" aria-label="小侦探身份与主线进度">
        <div class="home-status-card" :class="{ 'home-status-card--guest': !userStore.isLoggedIn }">
          <button
            type="button"
            class="home-status-card__avatar"
            :aria-label="userStore.isLoggedIn ? '进入个人中心' : '当前为游学模式'"
            @click="userStore.isLoggedIn ? openUserCenter() : openHomeMap()"
          >
            <img
              v-if="userStore.isLoggedIn && homeUserAvatar"
              :src="homeUserAvatar"
              :alt="`${homeUserName}头像`"
              @error="handleHomeAvatarError"
            />
            <img v-else :src="brandGuideImage" alt="" aria-hidden="true" />
          </button>
          <span class="home-status-card__copy">
            <small>当前游戏状态</small>
            <strong :title="userStore.isLoggedIn ? homeUserName : '游学模式'">
              {{ userStore.isLoggedIn ? homeUserName : '游学模式' }}
            </strong>
            <span>{{ userStore.isLoggedIn ? `Lv.${displayUserLevel} · ${displayLevelTitle}` : '登录后保存游戏进度' }}</span>
          </span>
        </div>

        <div v-if="userStore.isLoggedIn" class="identity-level-row" aria-label="当前探案等级">
          <span>探案等级</span>
          <strong>Lv.{{ displayUserLevel }}</strong>
          <em>{{ displayLevelTitle }}</em>
        </div>
        <button v-else type="button" class="guest-login-action" @click="openLogin">
          登录 / 注册，保存进度
          <span aria-hidden="true">→</span>
        </button>

        <section class="mainline-progress" aria-label="主线探案进度">
          <div class="mainline-progress__heading">
            <span>{{ userStore.isLoggedIn ? '主线探案进度' : '体验进度' }}</span>
            <strong>{{ completedMainlineCount }} / {{ mainlineLevels.length }}</strong>
          </div>
          <div
            class="mainline-progress__track"
            role="progressbar"
            :aria-valuenow="completedMainlineCount"
            aria-valuemin="0"
            :aria-valuemax="mainlineLevels.length"
            :aria-label="`主线进度 ${completedMainlineCount}/${mainlineLevels.length}`"
          >
            <span :style="{ width: `${mainlineProgressPercent}%` }"></span>
          </div>
          <div class="mainline-progress__case">
            <small>{{ userStore.isLoggedIn ? '当前案件' : '可体验案件' }}</small>
            <strong :title="currentLevel.label">{{ currentLevel.label }}</strong>
          </div>
        </section>

        <section class="badge-summary" aria-label="徽章摘要">
          <div class="badge-summary__heading">
            <span>已获得徽章</span>
            <strong v-if="userStore.isLoggedIn">{{ earnedBadges.length }} 枚</strong>
            <strong v-else>登录后保存</strong>
          </div>
          <div v-if="userStore.isLoggedIn && visibleEarnedBadges.length" class="badge-summary__list">
            <span
              v-for="badge in visibleEarnedBadges"
              :key="badge.id"
              class="badge-summary__item"
              :title="badge.name"
            >
              <img :src="badge.image" :alt="badge.name" />
            </span>
            <span v-if="earnedBadges.length > visibleEarnedBadges.length" class="badge-summary__more">
              +{{ earnedBadges.length - visibleEarnedBadges.length }}
            </span>
          </div>
          <p v-else>完成案件后获得徽章</p>
          <button type="button" class="view-all-badges" @click="openHonorArchive">
            查看全部徽章
            <span aria-hidden="true">›</span>
          </button>
        </section>
      </aside>

      <div class="map-center">
        <section
          class="map-panel"
          aria-label="杏林探案地图"
          :style="{ background: `url(${currentMapImage}) center / 100% 100% no-repeat` }"
        >
          <MapLevelNode
            v-for="level in levels"
            :key="level.id"
            :level="level"
            :active="selectedLevel.id === level.id"
            @enter-level="selectLevel"
          />

          <button type="button" class="detective-guide-sprite" :style="guidePosition" @click="speakAgain">
            <span class="guide-speech" role="status" aria-live="polite">{{ guideMessage }}</span>
            <img :key="currentGuideAction.src" :src="currentGuideAction.src" alt="" />
            <span class="guide-help">点点我</span>
          </button>

          <Transition name="mission-pop">
            <article v-if="showMissionCard" ref="missionCardRef" class="map-mission-card" role="dialog" :aria-labelledby="`mission-${selectedLevel.id}`" tabindex="-1">
              <button type="button" class="mission-card-close" aria-label="关闭任务说明" @click="showMissionCard = false">×</button>
              <span class="mission-card-kicker">{{ missionCard.kicker }}</span>
              <h2 :id="`mission-${selectedLevel.id}`">{{ selectedLevel.label }}</h2>
              <p>{{ missionCard.message }}</p>
              <div class="mission-card-reward"><img :src="missionRewardImage" alt=""><span><small>本关线索预告</small><b>{{ missionCard.reward }}</b></span></div>
              <button type="button" class="mission-card-action" @click="handleMissionAction">{{ missionCard.action }} <span>→</span></button>
            </article>
          </Transition>

          <Transition name="welcome-pop">
            <section v-if="showDailyWelcome" ref="dailyWelcomeRef" class="daily-welcome" role="dialog" aria-labelledby="daily-welcome-title" tabindex="-1">
              <div class="daily-welcome-dialog">
                <img :src="detectiveWave" alt="挥手欢迎的小铜人侦探">
                <div class="daily-welcome-bubble">
                  <h2 id="daily-welcome-title">你好呀，小侦探！</h2>
                  <p>这里是报到处，<br>我们一起开启新一件探索吧！</p>
                </div>
              </div>
              <div class="daily-welcome-actions">
                <button type="button" @click="acceptDailyMission">带我去看看 <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></button>
                <button type="button" class="quiet" @click="dismissDailyWelcome">我先自己探索</button>
              </div>
            </section>
          </Transition>
        </section>

        <footer class="task-bar" aria-label="今日探案委托">
          <nav class="task-mode-tabs" aria-label="任务类型">
            <button type="button" class="active" aria-current="page" @click="startCurrentMission">
              <img :src="rewardStar" alt="" aria-hidden="true" />
              <span>今日委托</span>
            </button>
            <button type="button" @click="openReviewRepair">
              <i class="fa-solid fa-fire-flame-curved" aria-hidden="true"></i>
              <span>热门任务</span>
            </button>
          </nav>

          <article class="task-feature-card">
            <div class="task-feature-card__story">
              <img class="task-guide-image" :src="detectiveWave" alt="挥手发出委托的小铜人侦探" />
              <div class="task-story-copy">
                <span class="task-bar__kicker">小铜人今日委托</span>
                <strong>{{ currentLevel.label }}</strong>
                <p>{{ currentLevel.description }}</p>
                <div class="task-story-dots" aria-hidden="true">
                  <span class="active"></span><span></span><span></span><span></span><span></span>
                </div>
              </div>
              <img class="task-feature-material" :src="materialBambooSlip" alt="竹简线索" />
            </div>

            <div class="task-feature-card__reward">
              <div class="task-reward-heading">完成可获得</div>
              <div class="task-reward-items" aria-label="任务奖励">
                <span v-for="reward in currentLevel.rewards || []" :key="reward.itemCode">
                  <img :src="reward.icon" :alt="reward.name" />
                  <strong>{{ reward.name }} ×{{ reward.amount }}</strong>
                </span>
                <span v-if="!(currentLevel.rewards || []).length" class="task-reward-empty">完成后解锁下一条线索</span>
              </div>
              <div class="task-bar__stars" aria-label="星光进度">
                <img
                  v-for="star in repairStars"
                  :key="star.id"
                  class="task-star"
                  :class="{ lit: star.lit }"
                  :src="rewardStar"
                  alt=""
                  aria-hidden="true"
                />
                <em>{{ repairStars.filter(s => s.lit).length }}/{{ repairStars.length }} 星已点亮</em>
              </div>
              <button type="button" class="task-bar__action" @click="startCurrentMission">
                和小铜人去查案
                <span aria-hidden="true">→</span>
              </button>
            </div>
          </article>
        </footer>
      </div>

      <aside class="right-home-rail" aria-label="侦探线索与修补入口">
        <section class="rail-card clue-box" aria-labelledby="clue-box-title">
          <div class="rail-card-heading">
            <h2 id="clue-box-title">侦探线索箱</h2>
            <span>探索进度</span>
            <button type="button" aria-label="查看线索说明" @click="startCurrentMission">
              <i class="fa-solid fa-question" aria-hidden="true"></i>
            </button>
          </div>
          <ul class="clue-list" aria-label="已获得线索材料">
            <li v-for="item in clueItems" :key="item.id">
              <span class="clue-icon" :class="item.id" aria-hidden="true">
                <img class="clue-img" :src="item.img" :alt="item.name" />
              </span>
              <span class="clue-name">{{ item.name }}</span>
              <strong class="clue-count">× {{ item.count }}</strong>
            </li>
          </ul>
        </section>

        <ContinuousRewardRail @start="startCurrentMission" />

        <section
          class="rail-card agency-box agency-box--link"
          aria-labelledby="agency-box-title"
        >
          <h2 id="agency-box-title">侦探社修复计划 <small>展示进度</small></h2>
          <div class="agency-house-wrap" aria-hidden="true">
            <img
              class="agency-house-img"
              :src="agencyHouseImage"
              alt="侦探社修复计划"
            />
          </div>
          <div class="agency-progress-row">
            <span>修复进度：</span>
            <strong>{{ agencyRepairProgress }}%</strong>
          </div>
          <div
            class="agency-progress-track"
            role="progressbar"
            :aria-valuenow="agencyRepairProgress"
            aria-valuemin="0"
            aria-valuemax="100"
            aria-label="侦探社修复进度"
          >
            <span :style="{ width: `${agencyRepairProgress}%` }"></span>
          </div>
          <button type="button" class="agency-open-button" @click.stop="openAgencyHome">
            一起修复侦探社！
            <i class="fa-solid fa-hammer" aria-hidden="true"></i>
          </button>
        </section>
      </aside>
    </section>

    <HomeIntroGuide
      :visible="showHomeIntro"
      :map-image="currentMapImage"
      :current-level="currentLevel"
      @finish="completeHomeIntro"
      @skip="completeHomeIntro"
    />

    <Transition name="case-complete-pop">
      <section v-if="completionCelebration" ref="completionDialogRef" class="case-complete-overlay" role="dialog" aria-modal="true" aria-labelledby="case-complete-title" tabindex="-1" @keydown="handleCompletionKeydown">
        <div class="case-complete-card">
          <div class="celebration-rays" aria-hidden="true"></div>
          <img class="case-complete-guide" :src="detectiveSuccess" alt="庆祝成功的小铜人侦探">
          <span class="case-complete-kicker">案件解决 · 新线索归档</span>
          <h2 id="case-complete-title">干得漂亮，小侦探！</h2>
          <p>“{{ completedLevelName }}”已经完成，地图上有一条新路线亮起来啦。</p>
          <div class="case-complete-rewards"><span><img :src="rewardStar" alt="">案件进度已保存</span><span><img :src="materialBambooSlip" alt="">探案线索已归档</span></div>
          <button type="button" @click="continueAfterCelebration">看看下一件案子 <span>→</span></button>
        </div>
      </section>
    </Transition>
  </main>
</template>

<script setup>
import './home-map.css'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { getUserAdventureMap } from '@/api/user'
import { useGameState } from '@/composables/useGameState'
import { mainlineLevels } from '@/data/mainline'
import { loadMainlineConfig, mainlineConfig } from '@/composables/useMainlineConfig'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import HomeIntroGuide from './HomeIntroGuide.vue'
import MapLevelNode from './MapLevelNode.vue'
import ContinuousRewardRail from './ContinuousRewardRail.vue'
import brandGuideImage from '@/assets/copper-detective-guide-512.png'
import agencyHouseImage from '@/assets/侦探社/1.png'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'
import mapCheckin from '@/assets/关卡/1.png'
import mapSafeStart from '@/assets/关卡/2.png'
import mapBamboo from '@/assets/关卡/3.png'
import mapBody from '@/assets/关卡/4.png'
import mapMeridian from '@/assets/关卡/5.png'
import mapArchive from '@/assets/关卡/6.png'
import mapSecretRoom from '@/assets/关卡/7.png'
import mapSafeReview from '@/assets/关卡/8.png'

const defaultMapImage = mapCheckin

const HOME_INTRO_STORAGE_KEY = 'hasSeenHomeIntro'
const DAILY_WELCOME_STORAGE_KEY = 'xiaotongren-home-welcome-date'
const MAP_PROGRESS_STORAGE_KEY = 'xiaotongren-adventure-completed-levels'

const shellRef = ref(null)
const guideActionIndex = ref(0)
const userStore = useUserStore()
const { materials, reviewRecords, agencyArchiveIds, storyArchiveIds, mapLevels: localMapLevels } = useGameState()
const adventureMap = ref(null)
const showHomeIntro = ref(false)
const selectedLevelId = ref('')
const showMissionCard = ref(false)
const showDailyWelcome = ref(false)
const guideMessageIndex = ref(0)
const completionCelebration = ref(false)
const completedLevelName = ref('')
const missionCardRef = ref(null)
const dailyWelcomeRef = ref(null)
const completionDialogRef = ref(null)
let focusBeforeDialog = null
let guideActionTimer

const guideActions = [
  { name: '小铜人侦探发现线索', src: generatedRewardAssets.detectiveDiscover },
  { name: '小铜人侦探提醒安全', src: generatedRewardAssets.detectiveSafety },
  { name: '小铜人侦探思考', src: generatedRewardAssets.detectiveThink },
  { name: '小铜人侦探挥手', src: generatedRewardAssets.detectiveEncourage },
  { name: '小铜人侦探获得奖励', src: generatedRewardAssets.detectiveReward },
  { name: '小铜人侦探确认成功', src: generatedRewardAssets.detectiveComplete }
]

const materialBadge = generatedRewardAssets.safetyObservationBadge
const materialBambooSlip = generatedMaterialIcons['bamboo-slip-shard']
const materialXinglinLeaf = generatedMaterialIcons['apricot-kernel']
const materialSafetyBell = generatedMaterialIcons['safety-bell']
const materialStar = generatedMaterialIcons['meridian-star-sand']
const rewardStar = generatedRewardAssets.rewardStarBurst
const detectiveWave = generatedRewardAssets.detectiveEncourage
const detectiveSuccess = generatedRewardAssets.detectiveComplete

const localStoryDone = computed(() => storyArchiveIds.value.includes('missing-bamboo'))

const honorBadgeCatalog = [
  { id: 'checkin', name: '铜人见习侦探', image: generatedRewardAssets.detectiveAgencyBadge, unlocked: true },
  { id: 'safety', name: '安全守护侦探', image: generatedRewardAssets.safetyObservationBadge, unlocked: true },
  { id: 'bamboo', name: '竹简线索侦探', image: generatedRewardAssets.storyArchiveCard, unlocked: false },
  { id: 'body', name: '身体地图侦探', image: generatedRewardAssets.bodyMapPuzzle, unlocked: false },
  { id: 'meridian', name: '经络追踪侦探', image: generatedRewardAssets.meridianMapClue, unlocked: false },
  { id: 'archive', name: '铜人观察侦探', image: generatedRewardAssets.copperDisplayStand, unlocked: false },
  { id: 'search', name: '铜人搜证侦探', image: generatedRewardAssets.clueStickerPack, unlocked: false },
  { id: 'repair', name: '星光修补侦探', image: generatedRewardAssets.starClueWall, unlocked: false },
  { id: 'gold', name: '杏林金牌侦探', image: generatedRewardAssets.rewardStarBurst, unlocked: false }
]

const clueCatalog = [
  { id: 'bamboo-slip-shard', name: '竹简碎片', img: generatedMaterialIcons['bamboo-slip-shard'] },
  { id: 'apricot-kernel', name: '杏林叶', img: materialXinglinLeaf },
  { id: 'meridian-star-sand', name: '经络星砂', img: materialStar }
]
const clueItems = computed(() => {
  const counts = new Map(materials.value.map((item) => [item.id, Number(item.count || 0)]))
  return clueCatalog.map((item) => ({ ...item, count: counts.get(item.id) || 0 }))
})

const repairStars = computed(() => {
  const repairedCount = reviewRecords.value.filter((item) => item.status === 'repaired').length
  return Array.from({ length: 5 }, (_, index) => ({
    id: `repair-star-${index + 1}`,
    lit: index < repairedCount
  }))
})

const agencyRepairProgress = computed(() => Math.round((agencyArchiveIds.value.length / 7) * 100))

const unlockedMapImages = {
  checkin: mapCheckin,
  'safe-start': mapSafeStart,
  bamboo: mapBamboo,
  body: mapBody,
  meridian: mapMeridian,
  archive: mapArchive,
  'secret-room': mapSecretRoom,
  agency: mapSafeReview
}

const baseLevels = computed(() => mainlineConfig.value?.levels?.length ? mainlineConfig.value.levels : mainlineLevels)

function levelTone(level) {
  if (level.id === 'safe-start') return ' safety'
  if (level.id === 'secret-room' || level.id === 'agency') return ' repair'
  return ''
}

const homeUserName = computed(() => {
  const user = userStore.userInfo || {}
  return user.name || user.displayName || user.nickname || user.username || '小侦探'
})
const homeUserAvatarFailed = ref(false)
const homeUserAvatarPath = computed(() => resolveMediaUrl(userStore.userInfo?.avatar))
const homeUserAvatar = computed(() => homeUserAvatarFailed.value ? '' : homeUserAvatarPath.value)

function handleHomeAvatarError() {
  homeUserAvatarFailed.value = true
}

const honorBadges = computed(() => {
  const backendLevels = adventureMap.value?.levels || []
  if (!backendLevels.length) {
    return honorBadgeCatalog.map((badge) => ({
      ...badge,
      unlocked: badge.id === 'bamboo' ? localStoryDone.value : false
    }))
  }
  const completed = new Set(backendLevels.filter((level) => level.completed).map((level) => level.id))
  const badgeLevelMap = { safety: 'safe-start', search: 'secret-room', repair: 'agency' }
  return honorBadgeCatalog.map((badge) => ({
    ...badge,
    unlocked: badge.id === 'bamboo'
      ? localStoryDone.value || completed.has('bamboo')
      : badge.id === 'gold'
        ? displayUserLevel.value >= 9
        : completed.has(badgeLevelMap[badge.id] || badge.id)
  }))
})
const earnedBadges = computed(() => honorBadges.value.filter((badge) => badge.unlocked))
const visibleEarnedBadges = computed(() => earnedBadges.value.slice(0, 3))

const levels = computed(() => {
  const backendLevels = adventureMap.value?.levels || []
  const stateById = new Map((backendLevels.length ? backendLevels : localMapLevels.value).map((level) => [level.id, level]))
  return baseLevels.value.map((level) => {
    const state = stateById.get(level.id)
    if (!state) return { ...level, status: `locked${levelTone(level)}` }
    const tone = levelTone(level)
    const status = state.completed
      ? `complete${tone}`
      : state.current
        ? `current${tone}`
        : state.unlocked
          ? `unlocked${tone}`
          : `locked${tone}`
    return {
      ...level,
      label: state.label || level.label,
      route: state.route || level.route,
      status,
      seal: state.completed || state.unlocked ? level.icon : '锁',
      unlocked: Boolean(state.unlocked),
      completed: Boolean(state.completed),
      current: Boolean(state.current),
      progress: state.progress,
      target: state.target
    }
  })
})

const completedMainlineCount = computed(() => levels.value.filter((level) => level.completed).length)
const mainlineProgressPercent = computed(() => Math.round((completedMainlineCount.value / mainlineLevels.length) * 100))
const localAdventureLevel = computed(() => Math.min(mainlineLevels.length + 1, completedMainlineCount.value + 1))

const currentMapImage = computed(() => {
  const key = adventureMap.value?.currentMapKey || currentLevel.value?.id
  return unlockedMapImages[key] || defaultMapImage
})

const currentLevel = computed(() => levels.value.find((level) => level.current)
  || levels.value.find((level) => level.status.includes('current'))
  || levels.value.filter((level) => level.completed).slice(-1)[0]
  || levels.value[0])
const displayUserLevel = computed(() => adventureMap.value?.userLevel ?? localAdventureLevel.value)
const displayLevelTitle = computed(() => {
  const level = displayUserLevel.value
  if (level >= 9) return '杏林金牌侦探'
  if (level >= 5) return '经络追踪侦探'
  if (level >= 3) return '身体地图侦探'
  return '铜人见习侦探'
})
const selectedLevel = computed(() => levels.value.find((level) => level.id === selectedLevelId.value) || currentLevel.value)
const isLockedSelection = computed(() => selectedLevel.value.status.includes('locked'))
const isCompleteSelection = computed(() => selectedLevel.value.status.includes('complete'))
const currentGuideAction = computed(() => {
  if (isLockedSelection.value) return guideActions[3]
  if (isCompleteSelection.value) return guideActions[5]
  if (showMissionCard.value) return guideActions[0]
  return guideActions[guideActionIndex.value]
})
const guidePosition = computed(() => ({
  left: `${Math.min(79, Math.max(8, selectedLevel.value.x + 13))}%`,
  top: `${Math.min(68, Math.max(8, selectedLevel.value.y - 4))}%`
}))
const guideMessages = computed(() => {
  if (isLockedSelection.value) return ['这件案子还锁着呢！', '先完成前一件案子，就能拿到开门线索。']
  if (isCompleteSelection.value) return ['这件案子已经解决啦！', '想再检查一次线索吗？']
  return [`就是这里，${selectedLevel.value.label}正在发光！`, '点开任务卡，我们一起看看线索。']
})
const guideMessage = computed(() => guideMessages.value[guideMessageIndex.value % guideMessages.value.length])
const missionRewardImage = computed(() => {
  if (selectedLevel.value.id === 'bamboo') return generatedRewardAssets.storyArchiveCard
  if (selectedLevel.value.id === 'body') return generatedRewardAssets.bodyMapPuzzle
  if (selectedLevel.value.id === 'meridian') return generatedRewardAssets.meridianMapClue
  if (selectedLevel.value.id === 'agency') return generatedRewardAssets.detectiveAgencyBadge
  return rewardStar
})
const missionCard = computed(() => {
  if (isLockedSelection.value) return { kicker: '尚未解锁', message: `先完成“${currentLevel.value.label}”，小铜人就能帮你打开这里。`, reward: '新的探案路线', action: '去完成前置任务' }
  if (isCompleteSelection.value) return { kicker: '案件已完成', message: '这件案子的线索已经归档，还可以再进去检查一次。', reward: '复习星光', action: '再次查看' }
  return { kicker: selectedLevel.value.id === currentLevel.value.id ? '今日重点案件' : '可调查案件', message: selectedLevel.value.description, reward: '进入案件后查看真实奖励', action: '和小铜人一起出发' }
})

function hasSeenHomeIntro() {
  try {
    return window.localStorage.getItem(HOME_INTRO_STORAGE_KEY) === 'true'
  } catch (error) {
    console.warn('Unable to read home intro state', error)
    return false
  }
}

function rememberHomeIntro() {
  try {
    window.localStorage.setItem(HOME_INTRO_STORAGE_KEY, 'true')
  } catch (error) {
    console.warn('Unable to save home intro state', error)
  }
}

function openIntroGuide() {
  showHomeIntro.value = true
}

function completeHomeIntro() {
  rememberHomeIntro()
  showHomeIntro.value = false
  shellRef.value?.focus()
}

function continueAfterCelebration() {
  completionCelebration.value = false
  selectedLevelId.value = currentLevel.value.id
  guideMessageIndex.value = 0
  showMissionCard.value = true
  nextTick(() => missionCardRef.value?.focus())
}

function openLevel(level) {
  window.location.hash = level.route.replace(/^#/, '')
}

function selectLevel(level) {
  selectedLevelId.value = level.id
  guideMessageIndex.value = 0
  showMissionCard.value = true
}

function handleMissionAction() {
  openLevel(isLockedSelection.value ? currentLevel.value : selectedLevel.value)
}

function startCurrentMission() {
  selectedLevelId.value = currentLevel.value.id
  guideMessageIndex.value = 0
  showMissionCard.value = true
}

function speakAgain() {
  guideMessageIndex.value = (guideMessageIndex.value + 1) % guideMessages.value.length
  showMissionCard.value = true
}

function todayKey() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function rememberDailyWelcome() {
  try { window.localStorage.setItem(DAILY_WELCOME_STORAGE_KEY, todayKey()) } catch (error) { console.warn('Unable to save daily welcome state', error) }
}

function dismissDailyWelcome() {
  rememberDailyWelcome()
  showDailyWelcome.value = false
}

function acceptDailyMission() {
  dismissDailyWelcome()
  startCurrentMission()
}

function openHomeMap() {
  window.location.hash = '/home-map'
}

function openUserCenter() {
  window.location.hash = '/myworld'
}

function openLogin() {
  window.location.hash = '/auth/login?redirect=/home-map'
}

function openSafety() {
  window.location.hash = '/safety'
}

function openHonorArchive() {
  window.location.hash = '/badges'
}

function openReviewRepair() {
  window.location.hash = '/review'
}

function openAgencyHome() {
  window.location.hash = '/agency'
}

function handleKeydown(event) {
  if (showHomeIntro.value || completionCelebration.value) return
  if (event.key === 'Escape') {
    if (showDailyWelcome.value) dismissDailyWelcome()
    else showMissionCard.value = false
    return
  }
  if (event.key === 'Enter') {
    startCurrentMission()
  }
}

function handleCompletionKeydown(event) {
  if (event.key === 'Escape') {
    continueAfterCelebration()
    return
  }
  if (event.key !== 'Tab') return
  const focusable = [...(completionDialogRef.value?.querySelectorAll('button,[href],[tabindex]:not([tabindex="-1"])') || [])]
  if (!focusable.length) return
  const first = focusable[0]
  const last = focusable[focusable.length - 1]
  if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus() }
  else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus() }
}

function readCompletedSnapshot() {
  try {
    const value = JSON.parse(window.localStorage.getItem(`${MAP_PROGRESS_STORAGE_KEY}:${userStore.userId || 'guest'}`) || '[]')
    return Array.isArray(value) ? value : []
  } catch {
    return []
  }
}

function saveCompletedSnapshot(ids) {
  if (userStore.isLoggedIn) return
  try { window.localStorage.setItem(`${MAP_PROGRESS_STORAGE_KEY}:${userStore.userId || 'guest'}`, JSON.stringify(ids)) } catch (error) { console.warn('Unable to save adventure progress snapshot', error) }
}

function detectNewCompletion(nextMap) {
  const completedIds = (nextMap?.levels || []).filter((level) => level.completed).map((level) => level.id)
  const previousIds = readCompletedSnapshot()
  const newlyCompleted = completedIds.find((id) => !previousIds.includes(id))
  saveCompletedSnapshot(completedIds)
  if (!previousIds.length || !newlyCompleted) return
  completedLevelName.value = nextMap.levels.find((level) => level.id === newlyCompleted)?.label || '上一件案子'
  completionCelebration.value = true
}

async function loadAdventureMap() {
  if (!userStore.isLoggedIn || !userStore.userId) {
    adventureMap.value = null
    return
  }
  try {
    const nextMap = await getUserAdventureMap(userStore.userId, { showDefaultMsg: false })
    detectNewCompletion(nextMap)
    adventureMap.value = nextMap
    selectedLevelId.value = nextMap?.currentLevelId || currentLevel.value.id
    guideMessageIndex.value = 0
  } catch (error) {
    console.warn('Failed to load adventure map state', error)
  }
}

watch(showMissionCard, async (visible) => {
  if (!visible) return
  await nextTick()
  missionCardRef.value?.focus()
})

watch(homeUserAvatarPath, () => {
  homeUserAvatarFailed.value = false
})

watch(showDailyWelcome, async (visible) => {
  if (!visible) return
  await nextTick()
  dailyWelcomeRef.value?.focus()
})

watch(completionCelebration, async (visible) => {
  if (visible) {
    focusBeforeDialog = document.activeElement
    await nextTick()
    completionDialogRef.value?.querySelector('button')?.focus()
  } else if (focusBeforeDialog instanceof HTMLElement) {
    focusBeforeDialog.focus()
    focusBeforeDialog = null
  }
})

onMounted(() => {
  shellRef.value?.focus()
  showHomeIntro.value = !hasSeenHomeIntro()
  selectedLevelId.value = currentLevel.value.id
  try {
    showDailyWelcome.value = hasSeenHomeIntro() && window.localStorage.getItem(DAILY_WELCOME_STORAGE_KEY) !== todayKey()
  } catch {
    showDailyWelcome.value = hasSeenHomeIntro()
  }
  loadMainlineConfig({ force: true }).catch((error) => {
    console.warn('主线配置加载失败，继续使用本地基线', error)
  })
  loadAdventureMap()
  window.addEventListener('user-level-refresh', loadAdventureMap)
  window.addEventListener('game-state-refresh', loadAdventureMap)
  guideActionTimer = window.setInterval(() => {
    guideActionIndex.value = (guideActionIndex.value + 1) % guideActions.length
  }, 7000)
})

onUnmounted(() => {
  window.removeEventListener('user-level-refresh', loadAdventureMap)
  window.removeEventListener('game-state-refresh', loadAdventureMap)
  window.clearInterval(guideActionTimer)
})
</script>


<style scoped>
/* ========== 左栏：身份与主线进度卡 ========== */
.honor-archive-card {
  position: relative;
  z-index: 6;
  min-height: 0;
  padding: 12px 11px 11px;
  border: 2px solid rgba(185, 126, 45, 0.72);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(255, 248, 228, 0.98), rgba(239, 211, 157, 0.96)),
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.72), transparent 46%);
  box-shadow:
    0 14px 28px rgba(98, 54, 15, 0.2),
    inset 0 0 0 1px rgba(255, 255, 255, 0.65);
  color: #6b3f14;
}

.honor-archive-card::before,
.honor-archive-card::after {
  display: none;
}

.honor-archive-card::before {
  left: 20px;
}

.honor-archive-card::after {
  right: 20px;
}

.home-account-card {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 2px 1px 10px;
  border-bottom: 1px solid rgba(164, 107, 37, 0.28);
}

.home-account-card__avatar {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  place-items: center;
  overflow: hidden;
  border: 2px solid rgba(157, 101, 35, 0.35);
  border-radius: 13px;
  background: linear-gradient(180deg, #f3d18a, #bd7d2e);
  box-shadow: 0 4px 8px rgba(89, 48, 14, 0.16);
}

.home-account-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 3px;
}

.home-account-card__copy {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 3px;
  line-height: 1.15;
}

.home-account-card__copy small,
.home-account-card__copy strong,
.home-account-card__copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-account-card__copy small {
  color: #9a6a30;
  font-size: 10px;
  font-weight: 900;
}

.home-account-card__copy strong {
  color: #583313;
  font-size: 14px;
  font-weight: 950;
}

.home-account-card__copy span {
  color: #80613b;
  font-size: 10px;
  font-weight: 800;
}

.home-account-card__action {
  flex: 0 0 auto;
  padding: 6px 7px;
  border: 1px solid rgba(157, 101, 35, 0.34);
  border-radius: 8px;
  background: rgba(255, 238, 194, 0.84);
  color: #84501f;
  cursor: pointer;
  font-size: 10px;
  font-weight: 900;
  white-space: nowrap;
}

.home-account-card__action:hover,
.home-account-card__action:focus-visible {
  border-color: rgba(47, 125, 104, 0.55);
  background: rgba(236, 249, 228, 0.96);
  outline: none;
}

.identity-level-row {
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr);
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 10px 1px 9px;
  border-bottom: 1px solid rgba(164, 107, 37, 0.22);
}

.identity-level-row span {
  color: #8a622d;
  font-size: 11px;
  font-weight: 900;
}

.identity-level-row strong {
  padding: 3px 6px;
  border-radius: 6px;
  background: linear-gradient(180deg, #bd7b2d, #8b4e1b);
  color: #fff5d4;
  font-size: 13px;
  font-weight: 950;
  white-space: nowrap;
}

.identity-level-row em {
  min-width: 0;
  overflow: hidden;
  color: #65401e;
  font-size: 11px;
  font-style: normal;
  font-weight: 850;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.honor-archive-card .guest-login-action {
  display: flex;
  width: 100%;
  min-height: 38px;
  margin: 10px 0 0;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border: 1px solid rgba(139, 77, 25, 0.6);
  border-radius: 10px;
  background: linear-gradient(180deg, #d48a3a, #a35c20);
  box-shadow: inset 0 -3px 0 rgba(91, 47, 14, 0.16), 0 5px 10px rgba(86, 52, 18, 0.14);
  color: #fff7df;
  cursor: pointer;
  font-size: 12px;
  font-weight: 950;
}

.honor-archive-card .guest-login-action:hover,
.honor-archive-card .guest-login-action:focus-visible {
  transform: translateY(-1px);
  box-shadow: inset 0 -3px 0 rgba(91, 47, 14, 0.16), 0 8px 14px rgba(86, 52, 18, 0.2);
}

.mainline-progress {
  margin-top: 11px;
  padding: 10px 10px 9px;
  border-radius: 10px;
  background: rgba(255, 248, 222, 0.62);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.48);
}

.mainline-progress__heading,
.badge-summary__heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.mainline-progress__heading span,
.badge-summary__heading span {
  color: #8a622d;
  font-size: 11px;
  font-weight: 900;
}

.mainline-progress__heading strong,
.badge-summary__heading strong {
  color: #59330f;
  font-size: 16px;
  font-weight: 950;
  white-space: nowrap;
}

.mainline-progress__track {
  height: 8px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(138, 111, 72, 0.22);
  box-shadow: inset 0 1px 3px rgba(98, 54, 15, 0.2);
}

.mainline-progress__track span {
  display: block;
  height: 100%;
  min-width: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, #c68132, #f0bd5b);
  box-shadow: 0 0 10px rgba(221, 157, 55, 0.55);
  transition: width 0.35s ease;
}

.mainline-progress__case {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 6px;
  align-items: baseline;
  margin-top: 8px;
  padding: 4px 6px;
  border-radius: 7px;
  animation: mainline-case-glow 2.8s ease-in-out infinite;
}

.mainline-progress__case small {
  color: #9b713c;
  font-size: 10px;
  font-weight: 850;
  white-space: nowrap;
}

.mainline-progress__case strong {
  min-width: 0;
  overflow: hidden;
  color: #5c3515;
  font-size: 12px;
  font-weight: 950;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge-summary {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(164, 107, 37, 0.24);
}

.badge-summary__list {
  display: flex;
  align-items: center;
  gap: 7px;
  min-height: 38px;
  margin-top: 8px;
}

.badge-summary__item {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid rgba(177, 118, 39, 0.34);
  border-radius: 9px;
  background: rgba(255, 246, 214, 0.64);
}

.badge-summary__item img {
  width: 31px;
  height: 31px;
  object-fit: contain;
}

.badge-summary__more {
  color: #8a571f;
  font-size: 12px;
  font-weight: 950;
}

.badge-summary p {
  min-height: 38px;
  margin: 8px 0 0;
  padding: 11px 8px;
  border-radius: 8px;
  background: rgba(255, 246, 214, 0.56);
  color: #927148;
  text-align: center;
  font-size: 11px;
  font-weight: 800;
}

.view-all-badges {
  width: 100%;
  margin-top: 9px;
  padding: 7px 8px;
  border: 0;
  border-top: 1px solid rgba(176, 111, 35, 0.22);
  background: transparent;
  color: #8a4f19;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.03em;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.view-all-badges:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 14px rgba(99, 54, 14, 0.16);
}

.view-all-badges span {
  margin-left: 6px;
  font-size: 18px;
}



.right-home-rail {
  display: grid;
  align-content: start;
  gap: 8px;
  min-width: 0;
}

.detective-board > .right-home-rail {
  display: grid;
}

.rail-card {
  position: relative;
  overflow: hidden;
  padding: 10px 10px 11px;
  border: 2px solid rgba(185, 126, 45, 0.62);
  border-radius: 11px;
  background:
    linear-gradient(180deg, rgba(255, 248, 226, 0.98), rgba(243, 218, 169, 0.94)),
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.66), transparent 44%);
  box-shadow:
    0 12px 24px rgba(98, 54, 15, 0.18),
    inset 0 0 0 1px rgba(255, 255, 255, 0.68);
  color: #5c3512;
}

.rail-card::before,
.rail-card::after {
  display: none;
}

.rail-card::before { left: 18px; }
.rail-card::after { right: 18px; }

.rail-card h2 {
  margin: 0 0 8px;
  text-align: center;
  color: #5b3515;
  font-size: 15px;
  font-weight: 950;
  letter-spacing: 0.05em;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.72);
}

.clue-list {
  display: grid;
  gap: 6px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.clue-list li {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 7px;
  min-height: 46px;
  padding: 5px 8px;
  border: 1px solid rgba(189, 135, 57, 0.34);
  border-radius: 8px;
  background: rgba(255, 247, 221, 0.76);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.54);
}

.clue-icon {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 8px;
  background: rgba(255, 240, 200, 0.5);
  overflow: hidden;
}

.clue-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.clue-name {
  overflow: hidden;
  color: #563216;
  font-size: 13px;
  font-weight: 900;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.clue-count {
  color: #4d8b35;
  font-size: 14px;
  font-weight: 950;
  white-space: nowrap;
}

.repair-stars {
  display: flex;
  justify-content: center;
  gap: 3px;
  margin: 5px 0 10px;
  line-height: 1;
}

.repair-stars img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  opacity: 0.34;
  filter: grayscale(1) saturate(0.18);
}

.repair-stars img.lit {
  opacity: 1;
  filter: none;
}

.repair-action {
  display: block;
  width: min(136px, 100%);
  min-height: 38px;
  margin: 0 auto;
  border: 1px solid rgba(128, 70, 24, 0.42);
  border-radius: 9px;
  background: linear-gradient(180deg, #c8863d, #9d5b21);
  box-shadow: inset 0 -5px 0 rgba(96, 48, 13, 0.22), 0 5px 10px rgba(87, 46, 13, 0.18);
  color: #fff4cb;
  cursor: pointer;
  font-size: 16px;
  font-weight: 950;
  letter-spacing: 0.04em;
}

.repair-action:hover {
  transform: translateY(-1px);
}

.agency-house-wrap {
  width: 100%;
  height: 145px;
  margin: 2px auto 7px;
  border-radius: 9px;
  overflow: hidden;
  border: 1px solid rgba(138, 91, 40, 0.35);
  box-shadow: 0 6px 14px rgba(98, 54, 15, 0.18);
}

.agency-house-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.agency-progress-row {
  display: flex;
  justify-content: center;
  gap: 8px;
  color: #75512a;
  font-size: 12px;
  font-weight: 900;
}

.agency-progress-row strong {
  color: #4f8a32;
}

.agency-progress-track {
  position: relative;
  height: 9px;
  margin-top: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #d6c7aa;
  box-shadow: inset 0 2px 4px rgba(101, 63, 24, 0.22);
}

.agency-progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #76ad42, #a8ce58);
  box-shadow: 0 0 10px rgba(109, 169, 58, 0.42);
}

.agency-box--link {
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.agency-box--link:hover {
  transform: translateY(-2px);
  border-color: rgba(190, 120, 35, 0.72);
  box-shadow: 0 18px 34px rgba(111, 66, 31, 0.2);
}

.agency-open-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  height: 38px;
  margin-top: 12px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(180deg, #e9a246, #b96f22);
  color: #fff8df;
  font-weight: 900;
  box-shadow: 0 8px 16px rgba(123, 73, 26, 0.24);
  cursor: pointer;
}

.agency-open-button span {
  font-size: 22px;
  line-height: 1;
}

/* ========== 桌面端：常驻小铜人陪伴引导 ========== */
.detective-guide-sprite {
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  pointer-events: auto;
  transition: left 420ms ease, top 420ms ease, transform 180ms ease;
}

.detective-guide-sprite:hover,
.detective-guide-sprite:focus-visible {
  transform: translateY(-4px) scale(1.05);
}

.guide-speech {
  left: 42%;
  top: -22%;
  width: 132px;
  padding: 9px 11px;
  border: 2px solid rgba(157, 101, 35, 0.52);
  border-radius: 14px 14px 14px 4px;
  background: rgba(255, 248, 220, 0.97);
  color: #65411d;
  line-height: 1.45;
}

.guide-help {
  position: absolute;
  right: -16px;
  bottom: 4px;
  padding: 3px 7px;
  border-radius: 999px;
  background: #3f6d3d;
  color: #fffbe9;
  font-size: 9px;
  font-weight: 900;
  white-space: nowrap;
  box-shadow: 0 4px 8px rgba(43, 79, 39, 0.24);
}

.map-mission-card {
  position: absolute;
  right: 2.5%;
  bottom: 3.5%;
  z-index: 18;
  width: 290px;
  padding: 18px 18px 16px;
  border: 2px solid #be812e;
  border-radius: 17px;
  background: linear-gradient(180deg, rgba(255, 251, 232, 0.99), rgba(244, 221, 170, 0.98));
  box-shadow: 0 18px 34px rgba(68, 40, 15, 0.3), inset 0 0 0 2px rgba(255,255,255,.55);
  color: #4e3015;
}

.mission-card-close { position:absolute;right:9px;top:7px;width:28px;height:28px;border:0;border-radius:50%;background:rgba(119,73,27,.1);color:#785027;font-size:20px;cursor:pointer; }
.mission-card-kicker { color:#a36220;font-size:11px;font-weight:950;letter-spacing:.08em; }
.map-mission-card h2 { margin:4px 30px 7px 0;color:#4a2c12;font-size:22px;font-weight:950; }
.map-mission-card p { margin:0;color:#725335;font-size:13px;font-weight:750;line-height:1.55; }
.mission-card-reward { display:flex;align-items:center;gap:9px;margin:12px 0;padding:8px 10px;border-radius:11px;background:rgba(255,247,211,.78); }
.mission-card-reward img { width:42px;height:42px;object-fit:contain; }
.mission-card-reward span { display:grid; }.mission-card-reward small{color:#98713d;font-size:10px;font-weight:850}.mission-card-reward b{color:#557a3e;font-size:12px}
.mission-card-action { width:100%;min-height:43px;border:1px solid #9a561d;border-radius:12px;background:linear-gradient(180deg,#ed9a40,#c56824);box-shadow:inset 0 -4px 0 rgba(89,41,12,.16);color:#fff9e9;font-size:15px;font-weight:950;cursor:pointer; }

.daily-welcome {
  position: absolute;
  left: 3.5%;
  bottom: 5%;
  z-index: 25;
  width: min(500px, 66%);
  outline: none;
}

.daily-welcome-dialog {
  position: relative;
  min-height: 154px;
  padding-left: 108px;
}

.daily-welcome-dialog > img {
  position: absolute;
  left: 0;
  bottom: -4px;
  z-index: 2;
  width: 145px;
  height: 164px;
  object-fit: contain;
  object-position: center bottom;
  filter: drop-shadow(0 9px 8px rgba(66, 39, 15, .26));
}

.daily-welcome-bubble {
  position: relative;
  z-index: 1;
  min-height: 138px;
  padding: 24px 28px 22px 52px;
  border: 2px solid #d59535;
  border-radius: 28px 28px 28px 10px;
  background: #fff9e1;
  box-shadow: 0 10px 20px rgba(84, 49, 17, .28), inset 0 1px 0 rgba(255, 255, 255, .9);
}

.daily-welcome h2 {
  margin: 0 0 9px;
  color: #4b2b12;
  font: 950 24px/1.2 'STKaiti', 'KaiTi', serif;
}

.daily-welcome p {
  margin: 0;
  color: #5f3b20;
  font-size: 15px;
  font-weight: 850;
  line-height: 1.6;
}

.daily-welcome-actions {
  display: flex;
  gap: 12px;
  margin: 12px 0 0 108px;
}

.daily-welcome-actions button {
  min-height: 46px;
  padding: 0 20px;
  border: 1px solid #a45e20;
  border-radius: 12px;
  background: #c9782d;
  box-shadow: 0 5px 10px rgba(85, 46, 13, .22), inset 0 -3px 0 rgba(91, 43, 12, .14);
  color: #fff8df;
  font-size: 14px;
  font-weight: 950;
  white-space: nowrap;
  cursor: pointer;
}

.daily-welcome-actions button:hover,
.daily-welcome-actions button:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 7px 13px rgba(85, 46, 13, .27), inset 0 -3px 0 rgba(91, 43, 12, .14);
}

.daily-welcome-actions button:focus-visible {
  outline: 3px solid rgba(255, 220, 112, .92);
  outline-offset: 2px;
}

.daily-welcome-actions button i {
  margin-left: 6px;
  font-size: 11px;
}

.daily-welcome-actions .quiet {
  background: rgba(255, 250, 232, .95);
  color: #75471e;
}

.mission-pop-enter-active,.mission-pop-leave-active,.welcome-pop-enter-active,.welcome-pop-leave-active{transition:opacity .22s ease,transform .22s ease}.mission-pop-enter-from,.mission-pop-leave-to{opacity:0;transform:translateY(12px) scale(.96)}.welcome-pop-enter-from,.welcome-pop-leave-to{opacity:0;transform:scale(.96)}

.level-node.current { z-index:8;filter:drop-shadow(0 0 12px rgba(255,206,61,.82)); }
.level-node:not(.current):not(:hover):not(:focus-visible) { filter:saturate(.82) contrast(.94); }
.right-home-rail .rail-card { opacity:.9; }
.right-home-rail .rail-card:hover,.right-home-rail .rail-card:focus-within { opacity:1; }

.rail-card h2 small { display:inline-flex;margin-left:4px;padding:2px 5px;border-radius:6px;background:rgba(140,91,35,.1);color:#936b3c;font-size:8px;font-weight:850;vertical-align:middle; }

/* ========== 预览图对齐：宽屏探案社舞台 ========== */
@media (min-width: 1201px) {
  .detective-board {
    min-height: calc(100dvh - 124px);
  }

  .detective-topbar {
    width: 100%;
    max-width: none;
    min-height: 106px;
    padding: 0 max(24px, calc((100vw - 1510px) / 2));
    grid-template-columns: 320px minmax(0, 1fr) 430px;
    gap: 0 18px;
    border-bottom-width: 4px;
  }

  .brand-lockup {
    min-height: 92px;
  }

  .brand-avatar {
    width: 78px;
    height: 78px;
    margin-right: 12px;
  }

  .brand-lockup strong {
    font-size: 30px;
  }

  .brand-lockup small {
    font-size: 14px;
  }

  .case-title {
    width: min(100%, 650px);
    min-height: 72px;
    justify-self: center;
    padding: 10px 24px 6px;
    border: 3px solid rgba(164, 103, 29, 0.48);
    border-radius: 999px;
    background: linear-gradient(180deg, rgba(255, 249, 221, 0.98), rgba(237, 203, 139, 0.94));
    box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.5), 0 7px 0 rgba(111, 66, 31, 0.1), 0 12px 22px rgba(80, 48, 17, 0.14);
  }

  .case-title h1 {
    font-size: clamp(30px, 2.4vw, 38px);
  }

  .topbar-actions {
    gap: 12px;
  }

  .home-guide-button {
    min-width: 156px;
    min-height: 54px;
    padding: 0 14px 0 11px;
    border-radius: 14px;
    font-size: 16px;
  }

  .home-guide-button img {
    width: 36px;
    height: 36px;
  }

  .level-pill {
    min-width: 190px;
    min-height: 48px;
  }

  .level-pill strong {
    font-size: 20px;
  }

  .level-pill span {
    font-size: 16px;
  }

  .guest-status {
    min-height: 48px;
    padding: 0 14px;
  }

  .guest-status__label {
    font-size: 15px;
  }

  .home-bell-button {
    width: 52px;
    height: 52px;
    border-radius: 14px;
  }

  .home-bell-button img {
    width: 30px;
    height: 30px;
  }

  .detective-board {
    width: min(calc(100% - 96px), 1510px);
    max-width: 1510px;
    height: calc(100dvh - 106px);
    min-height: 760px;
    grid-template-columns: minmax(280px, 296px) minmax(0, 1fr) minmax(280px, 296px);
    gap: 14px;
    padding: 14px 0 18px;
  }

  .honor-archive-card {
    min-height: 560px;
    padding: 17px 16px 15px;
    border-radius: 20px;
  }

  .home-account-card {
    gap: 11px;
    padding: 3px 2px 14px;
  }

  .home-account-card__avatar {
    width: 58px;
    height: 58px;
    flex-basis: 58px;
    border-radius: 17px;
  }

  .home-account-card__copy strong {
    font-size: 17px;
  }

  .home-account-card__copy small,
  .home-account-card__copy span {
    font-size: 11px;
  }

  .honor-archive-card .guest-login-action {
    min-height: 52px;
    margin-top: 15px;
    padding: 0 15px;
    border-radius: 14px;
    font-size: 15px;
  }

  .mainline-progress {
    margin-top: 15px;
    padding: 14px 13px 13px;
    border-radius: 14px;
  }

  .mainline-progress__heading span,
  .badge-summary__heading span {
    font-size: 13px;
  }

  .mainline-progress__heading strong,
  .badge-summary__heading strong {
    font-size: 20px;
  }

  .mainline-progress__track {
    height: 11px;
    margin-top: 10px;
  }

  .mainline-progress__case {
    margin-top: 10px;
    padding: 7px 8px;
  }

  .mainline-progress__case small {
    font-size: 11px;
  }

  .mainline-progress__case strong {
    font-size: 14px;
  }

  .badge-summary {
    margin-top: 16px;
    padding-top: 14px;
  }

  .badge-summary p {
    min-height: 58px;
    margin-top: 10px;
    padding: 17px 10px;
    border-radius: 12px;
    font-size: 13px;
  }

  .view-all-badges {
    min-height: 48px;
    margin-top: 13px;
    border: 2px solid rgba(176, 111, 35, 0.3);
    border-radius: 12px;
    background: rgba(255, 246, 214, 0.7);
    font-size: 14px;
  }

  .map-center {
    gap: 12px;
  }

  .map-panel {
    border-radius: 20px;
    box-shadow: 0 10px 28px rgba(68, 45, 20, 0.25), 0 0 0 3px rgba(255, 248, 218, 0.5);
  }

  .task-bar {
    min-height: 194px;
    grid-template-columns: 140px minmax(0, 1fr) minmax(210px, 0.8fr) 248px;
    gap: 14px;
    padding: 14px 16px;
    border-width: 3px;
    border-radius: 20px;
  }

  .task-bar__icon {
    width: 134px;
    height: 132px;
  }

  .task-bar__kicker {
    font-size: 18px;
  }

  .task-bar__info strong {
    font-size: 26px;
  }

  .task-bar__info p {
    margin-top: 8px;
    font-size: 15px;
    line-height: 1.5;
  }

  .task-bar__stars {
    flex-wrap: wrap;
    justify-content: center;
    gap: 6px;
  }

  .task-star {
    width: 28px;
    height: 28px;
  }

  .task-bar__stars em {
    flex-basis: 100%;
    margin-left: 0;
    text-align: center;
    font-size: 13px;
  }

  .task-bar__action {
    min-height: 68px;
    padding: 0 22px;
    border-radius: 17px;
    font-size: 21px;
  }

  .right-home-rail {
    gap: 14px;
  }

  .rail-card {
    padding: 14px 14px 16px;
    border-radius: 18px;
  }

  .rail-card h2 {
    margin-bottom: 12px;
    font-size: 20px;
  }

  .clue-list {
    gap: 10px;
  }

  .clue-list li {
    grid-template-columns: 66px minmax(0, 1fr) auto;
    min-height: 88px;
    gap: 10px;
    padding: 8px 10px;
    border-radius: 14px;
  }

  .clue-icon {
    width: 62px;
    height: 62px;
    border-radius: 13px;
  }

  .clue-name {
    font-size: 17px;
  }

  .clue-count {
    font-size: 16px;
  }

  .repair-stars {
    gap: 5px;
    margin: 10px 0 14px;
  }

  .repair-stars img {
    width: 32px;
    height: 32px;
  }

  .repair-action {
    width: min(164px, 100%);
    min-height: 50px;
    border-radius: 13px;
    font-size: 18px;
  }

  .agency-house-wrap {
    height: 160px;
    margin-top: 4px;
    border-radius: 14px;
  }

  .agency-progress-row {
    font-size: 14px;
  }

  .agency-progress-track {
    height: 11px;
    margin-top: 8px;
  }

  .agency-open-button {
    height: 48px;
    margin-top: 13px;
    font-size: 15px;
  }
}

@media (min-width: 901px) and (max-width: 1200px) {
  .detective-topbar {
    width: 100%;
    max-width: none;
    min-height: 82px;
    padding: 0 18px;
    grid-template-columns: 250px minmax(0, 1fr) 310px;
  }

  .brand-lockup {
    min-height: 76px;
  }

  .brand-avatar {
    width: 68px;
    height: 68px;
  }

  .brand-lockup strong {
    font-size: 26px;
  }

  .case-title h1 {
    font-size: clamp(24px, 2.6vw, 32px);
  }

  .detective-board {
    width: calc(100% - 28px);
    max-width: none;
    height: auto;
    min-height: 0;
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px 0 16px;
  }

  .honor-archive-card {
    display: grid;
    grid-template-columns: minmax(0, 1.1fr) minmax(220px, 1fr);
    align-items: center;
    column-gap: 18px;
    row-gap: 0;
    min-height: auto;
  }

  .home-status-card {
    grid-column: 1 / -1;
  }

  .identity-level-row,
  .honor-archive-card .guest-login-action {
    grid-column: 1;
  }

  .mainline-progress,
  .badge-summary {
    grid-column: 2;
    margin-top: 0;
  }

  .badge-summary {
    padding-top: 0;
    border-top: 0;
  }

  .right-home-rail {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .task-bar {
    grid-template-columns: 96px minmax(0, 1fr) auto 210px;
  }

  .task-bar__icon {
    width: 92px;
    height: 92px;
  }
}

/* ========== 参考图逐区复刻：状态卡 / 卷轴地图 / 委托台 / 线索箱 ========== */
.study-mode-button {
  display: inline-flex;
  min-width: 148px;
  min-height: 54px;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 0 15px;
  border: 2px solid rgba(137, 83, 28, 0.34);
  border-radius: 14px;
  background: rgba(255, 245, 219, 0.94);
  box-shadow: inset 0 -4px 0 rgba(112, 70, 26, 0.12), 0 5px 10px rgba(86, 52, 18, 0.14);
  color: #5a3516;
  cursor: pointer;
}

.study-mode-button i {
  color: #c5882d;
  font-size: 22px;
}

.study-mode-button strong {
  font-size: 16px;
  font-weight: 950;
}

.study-mode-button:hover,
.study-mode-button:focus-visible {
  transform: translateY(-1px);
  outline: 3px solid rgba(90, 167, 216, 0.55);
  outline-offset: 2px;
}

.home-status-card {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  align-items: center;
  gap: 13px;
  padding: 5px 4px 18px;
  border-bottom: 1px solid rgba(164, 107, 37, 0.28);
}

.home-status-card__avatar {
  display: grid;
  width: 86px;
  height: 86px;
  place-items: center;
  overflow: hidden;
  padding: 0;
  border: 3px solid rgba(157, 101, 35, 0.48);
  border-radius: 50%;
  background: #efd295;
  box-shadow: inset 0 0 0 4px rgba(255, 247, 217, 0.72), 0 8px 14px rgba(89, 48, 14, 0.2);
  cursor: pointer;
}

.home-status-card__avatar img {
  width: 100%;
  height: 100%;
  padding: 7px;
  object-fit: contain;
}

.home-status-card__copy {
  display: grid;
  min-width: 0;
  gap: 4px;
}

.home-status-card__copy small {
  color: #8d6635;
  font-size: 13px;
  font-weight: 900;
}

.home-status-card__copy strong {
  overflow: hidden;
  color: #4f2c12;
  font-family: "STKaiti", "KaiTi", "Microsoft YaHei", serif;
  font-size: 24px;
  font-weight: 950;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-status-card__copy span {
  overflow: hidden;
  color: #705035;
  font-size: 12px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rail-card-heading {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto 30px;
  align-items: center;
  gap: 7px;
  margin-bottom: 12px;
}

.rail-card-heading h2 {
  margin: 0;
  text-align: left;
}

.rail-card-heading > span {
  padding: 4px 7px;
  border-radius: 999px;
  background: rgba(150, 95, 28, 0.12);
  color: #8f6735;
  font-size: 10px;
  font-weight: 900;
  white-space: nowrap;
}

.rail-card-heading button {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border: 1px solid rgba(127, 73, 24, 0.44);
  border-radius: 50%;
  background: #b87528;
  color: #fff4d0;
  cursor: pointer;
}

.task-bar {
  display: grid;
  min-height: 205px;
  grid-template-columns: 112px minmax(0, 1fr);
  gap: 8px;
  padding: 0;
  border: 0;
  border-radius: 20px;
  background: transparent;
  box-shadow: none;
}

.task-mode-tabs {
  display: grid;
  align-content: center;
  gap: 9px;
}

.task-mode-tabs button {
  display: grid;
  min-height: 74px;
  grid-template-columns: 26px minmax(0, 1fr);
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  border: 2px solid rgba(153, 95, 28, 0.42);
  border-radius: 18px 9px 9px 18px;
  background: rgba(255, 238, 193, 0.94);
  box-shadow: inset 0 -5px 0 rgba(116, 69, 25, 0.12), 0 6px 12px rgba(83, 49, 16, 0.16);
  color: #754314;
  cursor: pointer;
  font-size: 15px;
  font-weight: 950;
  text-align: left;
}

.task-mode-tabs button.active {
  background: #edb94f;
  color: #5b310d;
}

.task-mode-tabs img {
  width: 25px;
  height: 25px;
  object-fit: contain;
}

.task-mode-tabs i {
  color: #d75d19;
  font-size: 22px;
  text-align: center;
}

.task-feature-card {
  display: grid;
  min-width: 0;
  grid-template-columns: minmax(390px, 1.15fr) minmax(390px, 1fr);
  overflow: hidden;
  border: 3px solid rgba(156, 98, 31, 0.55);
  border-radius: 20px;
  background: rgba(255, 246, 219, 0.98);
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.56), 0 10px 20px rgba(82, 48, 16, 0.2);
}

.task-feature-card__story {
  display: grid;
  min-width: 0;
  grid-template-columns: 128px minmax(0, 1fr) 96px;
  align-items: center;
  gap: 8px;
  padding: 12px 12px 10px;
  background: rgba(255, 250, 230, 0.9);
}

.task-guide-image {
  width: 128px;
  height: 164px;
  align-self: end;
  object-fit: contain;
  filter: drop-shadow(0 9px 8px rgba(74, 42, 13, 0.2));
}

.task-story-copy {
  display: grid;
  min-width: 0;
  align-content: center;
  gap: 3px;
}

.task-story-copy > strong {
  color: #3f250f;
  font-family: "STKaiti", "KaiTi", "Microsoft YaHei", serif;
  font-size: 27px;
  font-weight: 950;
}

.task-story-copy p {
  margin: 4px 0 0;
  color: #765635;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.5;
}

.task-feature-material {
  width: 92px;
  height: 112px;
  object-fit: contain;
  filter: drop-shadow(0 7px 7px rgba(103, 61, 19, 0.2));
}

.task-story-dots {
  display: flex;
  gap: 10px;
  margin-top: 13px;
}

.task-story-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #c9b58d;
}

.task-story-dots span.active {
  background: #8f6b18;
}

.task-feature-card__reward {
  position: relative;
  display: grid;
  min-width: 0;
  grid-template-columns: minmax(200px, 1fr) minmax(210px, 1fr);
  grid-template-areas:
    "heading stars"
    "items action";
  align-items: center;
  gap: 5px 14px;
  padding: 18px 20px;
  border-left: 1px solid rgba(161, 105, 37, 0.22);
}

.task-reward-heading {
  grid-area: heading;
  align-self: end;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(220, 170, 82, 0.25);
  color: #89602e;
  font-size: 11px;
  font-weight: 900;
  justify-self: start;
}

.task-reward-items {
  grid-area: items;
  display: flex;
  align-items: center;
  gap: 20px;
}

.task-reward-items span {
  display: grid;
  justify-items: center;
  gap: 2px;
  color: #5d3817;
  font-size: 13px;
}

.task-reward-items strong {
  max-width: 108px;
  color: #5d3817;
  font-size: 11px;
  line-height: 1.25;
  text-align: center;
}

.task-reward-empty {
  align-self: center;
  color: #806b50;
  font-size: 12px;
}

.task-reward-items img {
  width: 54px;
  height: 54px;
  object-fit: contain;
}

.task-feature-card__reward .task-bar__stars {
  grid-area: stars;
  align-self: end;
  justify-content: flex-start;
}

.task-feature-card__reward .task-bar__action {
  grid-area: action;
  width: 100%;
  min-height: 64px;
}

@media (min-width: 1201px) {
  .detective-home {
    background: url("../../assets/maps/xinglin-detective-map-bg.png") center / cover no-repeat fixed;
  }

  .detective-topbar {
    min-height: 106px;
    grid-template-columns: 335px minmax(580px, 1fr) 390px;
  }

  .case-title {
    width: min(100%, 600px);
  }

  .detective-board {
    width: min(calc(100% - 160px), 1500px);
    max-width: 1500px;
    height: auto;
    min-height: calc(100dvh - 106px);
    grid-template-columns: 296px minmax(0, 1fr) 296px;
    gap: 14px;
    padding: 14px 0 18px;
  }

  .map-center {
    gap: 18px;
  }

  .map-panel {
    aspect-ratio: 1649 / 954;
    border-radius: 16px;
  }

  .honor-archive-card {
    min-height: 558px;
  }

  .honor-archive-card .guest-login-action {
    min-height: 52px;
    font-size: 16px;
  }

  .clue-box {
    min-height: 410px;
  }

  .clue-list li {
    min-height: 94px;
  }

  .agency-box {
    min-height: 346px;
  }

  .agency-house-wrap {
    height: 195px;
  }

  .detective-guide-sprite {
    width: 15%;
  }
}

@media (min-width: 901px) and (max-width: 1200px) {
  .study-mode-button {
    min-width: 104px;
  }

  .task-bar {
    grid-template-columns: 96px minmax(0, 1fr);
  }

  .task-feature-card {
    grid-template-columns: minmax(300px, 1fr) minmax(300px, 1fr);
  }

  .task-feature-card__story {
    grid-template-columns: 92px minmax(0, 1fr) 72px;
  }

  .task-guide-image {
    width: 92px;
    height: 132px;
  }

  .task-feature-material {
    width: 70px;
    height: 90px;
  }

  .task-feature-card__reward {
    grid-template-columns: minmax(160px, 1fr) minmax(170px, 1fr);
    padding: 12px;
  }
}

/* Keep the desktop mission card and right rail inside their visible columns. */
@media (min-width: 1201px) {
  .task-feature-card {
    grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  }

  .task-feature-card__story {
    grid-template-columns: 96px minmax(0, 1fr) 68px;
    padding-inline: 10px;
  }

  .task-guide-image {
    width: 96px;
    height: 152px;
  }

  .task-feature-material {
    width: 66px;
    height: 84px;
  }

  .task-story-copy > strong {
    font-size: 24px;
  }

  .task-feature-card__reward {
    grid-template-columns: minmax(0, 1fr) minmax(150px, 0.9fr);
    gap: 5px 8px;
    padding: 14px;
  }

  .task-reward-items {
    gap: 12px;
  }

  .task-reward-items img {
    width: 46px;
    height: 46px;
  }

  .task-feature-card__reward .task-bar__action {
    min-height: 56px;
    padding-inline: 12px;
    font-size: 17px;
  }

  .right-home-rail {
    gap: 10px;
  }

  .right-home-rail .rail-card {
    padding: 12px;
  }

  .right-home-rail .clue-box,
  .right-home-rail .agency-box {
    min-height: 0;
  }

  .clue-list {
    gap: 7px;
  }

  .clue-list li {
    grid-template-columns: 52px minmax(0, 1fr) auto;
    min-height: 68px;
    gap: 8px;
    padding: 6px 8px;
  }

  .clue-icon {
    width: 48px;
    height: 48px;
  }

  .agency-house-wrap {
    height: 88px;
    margin-top: 2px;
  }

  .agency-open-button {
    height: 42px;
    margin-top: 10px;
  }
}

@media (min-width: 1201px) and (max-width: 1600px) {
  .detective-topbar {
    grid-template-columns: 280px minmax(0, 1fr) 360px;
    padding-inline: 18px;
  }

  .detective-board {
    width: calc(100% - 32px);
    max-width: none;
    grid-template-columns: 248px minmax(0, 1fr) 248px;
    gap: 12px;
  }

  .task-bar {
    grid-template-columns: 104px minmax(0, 1fr);
  }

  .task-mode-tabs button {
    padding-inline: 9px;
  }

  .task-feature-card__story {
    grid-template-columns: 82px minmax(0, 1fr) 56px;
  }

  .task-guide-image {
    width: 82px;
    height: 136px;
  }

  .task-feature-material {
    width: 54px;
    height: 72px;
  }

  .task-feature-card__reward {
    grid-template-columns: minmax(0, 1fr) minmax(132px, 0.9fr);
    padding: 10px;
  }

  .task-reward-items {
    gap: 8px;
  }

  .task-reward-items img {
    width: 40px;
    height: 40px;
  }

  .task-feature-card__reward .task-bar__action {
    gap: 4px;
    padding-inline: 6px;
    font-size: 14px;
  }

  .task-feature-card__reward .task-bar__action span {
    font-size: 16px;
  }
}

.case-complete-overlay { position:fixed;inset:0;z-index:90;display:grid;place-items:center;padding:24px;background:rgba(42,28,15,.68);backdrop-filter:blur(6px); }
.case-complete-card { position:relative;display:grid;justify-items:center;width:min(520px,100%);padding:32px 38px 30px;border:3px solid #e3aa3f;border-radius:24px;background:radial-gradient(circle at 50% 16%,rgba(255,232,151,.82),transparent 35%),linear-gradient(180deg,#fffbea,#efd29a);box-shadow:0 30px 80px rgba(28,16,6,.46),inset 0 0 0 3px rgba(255,255,255,.5);overflow:hidden;text-align:center;color:#4b2c12; }
.celebration-rays { position:absolute;left:50%;top:92px;width:360px;height:360px;background:repeating-conic-gradient(from 0deg,rgba(255,196,48,.22) 0 9deg,transparent 9deg 20deg);transform:translate(-50%,-50%);animation:celebration-spin 18s linear infinite; }
.case-complete-guide { position:relative;z-index:1;width:160px;height:160px;object-fit:contain;filter:drop-shadow(0 12px 12px rgba(70,39,12,.25));animation:guide-success-bounce .7s ease both; }
.case-complete-kicker { position:relative;color:#9b6124;font-size:12px;font-weight:950;letter-spacing:.12em; }.case-complete-card h2{position:relative;margin:7px 0 8px;font:950 32px 'STKaiti','KaiTi',serif}.case-complete-card p{position:relative;max-width:400px;margin:0;color:#6d4d30;font-size:15px;font-weight:800;line-height:1.65}
.case-complete-rewards{position:relative;display:flex;gap:10px;margin:18px 0}.case-complete-rewards span{display:flex;align-items:center;gap:6px;padding:8px 12px;border:1px solid rgba(169,112,37,.3);border-radius:12px;background:rgba(255,249,222,.78);color:#5d6f3d;font-size:12px;font-weight:900}.case-complete-rewards img{width:32px;height:32px;object-fit:contain}
.case-complete-card>button{position:relative;min-width:240px;min-height:50px;border:2px solid #98531d;border-radius:15px;background:linear-gradient(180deg,#f0a044,#c96925);box-shadow:inset 0 -5px 0 rgba(88,42,12,.18),0 8px 16px rgba(102,55,17,.2);color:#fff9e8;font-size:17px;font-weight:950;cursor:pointer}.case-complete-card>button:hover{transform:translateY(-2px)}
.case-complete-pop-enter-active,.case-complete-pop-leave-active{transition:opacity .25s ease}.case-complete-pop-enter-from,.case-complete-pop-leave-to{opacity:0}.case-complete-pop-enter-from .case-complete-card{transform:translateY(18px) scale(.94)}.case-complete-card{transition:transform .3s ease}
@keyframes celebration-spin{to{transform:translate(-50%,-50%) rotate(360deg)}}@keyframes guide-success-bounce{0%{opacity:0;transform:translateY(20px) scale(.8)}70%{transform:translateY(-5px) scale(1.06)}100%{opacity:1;transform:none}}
@keyframes mainline-case-glow{0%,100%{background:rgba(255,248,222,.24);box-shadow:inset 0 0 0 1px rgba(190,132,48,.08)}50%{background:rgba(255,235,170,.62);box-shadow:inset 0 0 0 1px rgba(190,132,48,.24),0 0 12px rgba(220,158,60,.16)}}

@media (prefers-reduced-motion: reduce) {
  .celebration-rays,
  .case-complete-guide,
  .mainline-progress__case { animation:none; }
  .mainline-progress__track span,
  .honor-archive-card .guest-login-action,
  .home-account-card__action,
  .view-all-badges { transition:none; }
  .case-complete-card,
  .case-complete-card > button { transition:none; }
}

@media (max-width: 900px) {
  .detective-board {
    grid-template-columns: 1fr;
    height: auto;
  }

  .honor-archive-card {
    display: grid;
    grid-template-columns: minmax(0, 1.1fr) minmax(220px, 1fr);
    align-items: center;
    column-gap: 18px;
    row-gap: 0;
    min-height: auto;
  }

  .home-account-card,
  .home-status-card {
    grid-column: 1 / -1;
  }

  .identity-level-row,
  .honor-archive-card .guest-login-action {
    grid-column: 1;
  }

  .mainline-progress,
  .badge-summary {
    grid-column: 2;
    margin-top: 0;
  }

  .badge-summary {
    padding-top: 0;
    border-top: 0;
  }

  .right-home-rail {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .task-bar {
    grid-template-columns: 1fr;
  }

  .task-mode-tabs {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .task-mode-tabs button {
    min-height: 58px;
    border-radius: 14px;
  }

  .task-feature-card {
    grid-template-columns: 1fr;
  }

  .task-feature-card__reward {
    border-top: 1px solid rgba(161, 105, 37, 0.22);
    border-left: 0;
  }
}

@media (max-width: 720px) {
  .daily-welcome {
    position: fixed;
    top: 50%;
    left: 50%;
    bottom: auto;
    width: min(390px, calc(100vw - 24px));
    max-height: calc(100dvh - 40px);
    overflow-y: auto;
    transform: translate(-50%, -50%);
  }

  .daily-welcome-dialog {
    min-height: 130px;
    padding-left: 76px;
  }

  .daily-welcome-dialog > img {
    width: 112px;
    height: 132px;
  }

  .daily-welcome-bubble {
    min-height: 122px;
    padding: 20px 18px 18px 42px;
    border-radius: 23px 23px 23px 9px;
  }

  .daily-welcome h2 {
    font-size: 21px;
  }

  .daily-welcome p {
    font-size: 13px;
  }

  .daily-welcome-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 9px;
    margin: 10px 0 0 76px;
  }

  .daily-welcome-actions button {
    min-width: 0;
    padding: 0 10px;
    font-size: 13px;
  }

  .honor-archive-card {
    grid-template-columns: 1fr;
  }

  .mainline-progress,
  .badge-summary {
    grid-column: auto;
  }

  .right-home-rail {
    grid-template-columns: 1fr;
  }

  .home-status-card {
    grid-template-columns: 74px minmax(0, 1fr);
  }

  .home-status-card__avatar {
    width: 72px;
    height: 72px;
  }

  .task-feature-card__story {
    grid-template-columns: 86px minmax(0, 1fr) 68px;
    padding: 10px;
  }

  .task-guide-image {
    width: 86px;
    height: 116px;
  }

  .task-story-copy > strong {
    font-size: 22px;
  }

  .task-feature-material {
    width: 66px;
    height: 82px;
  }

  .task-feature-card__reward {
    grid-template-columns: 1fr;
    grid-template-areas:
      "heading"
      "items"
      "stars"
      "action";
  }

  .task-feature-card__reward .task-bar__stars {
    justify-content: center;
  }
}
/* Homepage typography and alignment corrections */
.detective-topbar {
  position: sticky;
  top: 0;
}

.brand-copy {
  align-content: center;
  line-height: 1;
}

.brand-lockup strong { line-height: 1.14; }
.brand-lockup small { line-height: 1.35; }

.case-title {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-block: 6px;
}

.case-title h1 { line-height: 1.18; }

.task-mode-tabs button {
  grid-template-columns: 24px minmax(0, 1fr);
  padding-inline: 8px;
}

.task-mode-tabs button span {
  justify-self: center;
  line-height: 1.2;
  white-space: nowrap;
}

.task-feature-card__reward .task-reward-items {
  justify-content: space-around;
  gap: 6px;
}

.task-feature-card__reward .task-reward-items img {
  width: 42px;
  height: 42px;
}

.task-bar__kicker,
.task-story-copy > strong { line-height: 1.2; }

.task-feature-card__reward {
  grid-template-columns: minmax(0, 1fr) minmax(150px, .92fr);
  gap: 8px 10px;
  padding-inline: 14px;
}

.task-feature-card__reward .task-bar__action {
  min-width: 0;
  padding-inline: 10px;
  font-size: 15px;
  line-height: 1.2;
  white-space: nowrap;
}

@media (max-width: 1280px) {
  .task-feature-card__reward {
    grid-template-columns: minmax(0, 1fr) minmax(136px, .9fr);
    padding-inline: 10px;
  }

  .task-feature-card__reward .task-bar__action {
    padding-inline: 7px;
    font-size: 14px;
  }
}
/* The global navigation owns branding; this row only describes the current map. */
.detective-home { min-height: calc(100vh - var(--site-nav-height)); }
.detective-topbar {
  grid-template-columns: minmax(0, 1fr) auto;
  grid-template-areas: "title actions" "safety safety";
}
.brand-lockup { display: none; }
.case-title { justify-self: start; padding-left: 16px; }
.case-title h1 { color: var(--site-primary-deep); }

@media (max-width: 640px) {
  .detective-topbar {
    grid-template-columns: 1fr;
    grid-template-areas: "title" "actions" "safety";
  }
  .case-title { justify-self: center; padding-left: 0; text-align: center; }
}
</style>

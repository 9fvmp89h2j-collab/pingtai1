import { computed, ref, watch } from 'vue'
import { dailyTasks as baseDailyTasks, mainTasks as baseMainTasks } from '@/data/tasks'
import { materials as baseMaterials } from '@/data/materials'
import { badges as baseBadges } from '@/data/badges'
import { mapLevels as baseMapLevels } from '@/data/mapLevels'
import { getMainlineTask, mainlineConfig } from '@/composables/useMainlineConfig'
import { useUserStore } from '@/store/user'
import { exchangeAgencyArchiveItem as exchangeAgencyArchiveItemApi, submitGameProgressEvent } from '@/api/GameApi'

const STORAGE_KEY = 'xinglin-game-state-v3'
const LEGACY_STORAGE_KEY = 'xinglin-game-state-v2'
const STORY_STORAGE_KEY = 'xinglin-story-state-v1'

function createInitialState() {
  return {
    completedTaskIds: [],
    taskProgress: {},
    taskClaims: {},
    agencyArchiveIds: [],
    storyArchiveIds: [],
    storyProgress: {},
    reviewRecords: [],
    materialCounts: baseMaterials.reduce((acc, item) => {
      acc[item.id] = 0
      return acc
    }, {})
  }
}

function migrateLegacyState(parsed) {
  const legacyCounts = parsed?.materialCounts || {}
  return {
    ...parsed,
    materialCounts: baseMaterials.reduce((acc, item) => {
      const legacyCount = Number(legacyCounts[item.id] || 0)
      acc[item.id] = Math.max(0, legacyCount - Number(item.count || 0))
      return acc
    }, {})
  }
}

function readState() {
  if (typeof window === 'undefined') return createInitialState()
  try {
    const currentRaw = window.localStorage.getItem(STORAGE_KEY)
    const legacyRaw = currentRaw ? null : window.localStorage.getItem(LEGACY_STORAGE_KEY)
    const storyRaw = window.localStorage.getItem(STORY_STORAGE_KEY)
    const raw = currentRaw || legacyRaw
    if (!raw && !storyRaw) return createInitialState()
    const parsed = raw
      ? (legacyRaw ? migrateLegacyState(JSON.parse(raw)) : JSON.parse(raw))
      : {}
    const storyParsed = storyRaw ? JSON.parse(storyRaw) : {}
    if (legacyRaw) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(parsed))
    }
    return {
      ...createInitialState(),
      ...parsed,
      materialCounts: {
        ...createInitialState().materialCounts,
        ...(parsed.materialCounts || {})
      },
      completedTaskIds: Array.isArray(parsed.completedTaskIds) ? parsed.completedTaskIds : [],
      taskProgress: parsed.taskProgress && typeof parsed.taskProgress === 'object'
        ? parsed.taskProgress
        : {},
      taskClaims: parsed.taskClaims && typeof parsed.taskClaims === 'object'
        ? parsed.taskClaims
        : {},
      agencyArchiveIds: Array.isArray(parsed.agencyArchiveIds)
        ? parsed.agencyArchiveIds
        : (Array.isArray(parsed.clinicDecorationIds) ? parsed.clinicDecorationIds : []),
      storyArchiveIds: Array.isArray(storyParsed.storyArchiveIds)
        ? storyParsed.storyArchiveIds
        : (Array.isArray(parsed.storyArchiveIds) ? parsed.storyArchiveIds : []),
      storyProgress: storyParsed.storyProgress && typeof storyParsed.storyProgress === 'object'
        ? storyParsed.storyProgress
        : (parsed.storyProgress && typeof parsed.storyProgress === 'object'
          ? parsed.storyProgress
          : {}),
      reviewRecords: Array.isArray(parsed.reviewRecords) ? parsed.reviewRecords : []
    }
  } catch (error) {
    console.warn('Failed to read xinglin game state', error)
    return createInitialState()
  }
}

const gameState = ref(readState())

function hasServerSession() {
  if (typeof window === 'undefined') return false
  return Boolean(window.localStorage.getItem('token') && window.localStorage.getItem('userInfo'))
}

function applyServerState(state) {
  if (!state) return
  const taskProgress = {}
  const taskClaims = {}
  const completedTaskIds = []
  if (Array.isArray(state.tasks)) {
    state.tasks.forEach((item) => {
      if (!item?.taskCode) return
      const progress = Number(item.progress || 0)
      taskProgress[item.taskCode] = progress
      taskClaims[item.taskCode] = Boolean(item.claimedAt)
      const localTaskId = localTaskIdForServerTask(item.taskCode)
      if (item.status === 'COMPLETED') {
        completedTaskIds.push(item.taskCode)
        if (localTaskId) completedTaskIds.push(localTaskId)
      }
      if (localTaskId) taskProgress[localTaskId] = progress
      if (localTaskId) taskClaims[localTaskId] = Boolean(item.claimedAt)
    })
  }
  const materialCounts = baseMaterials.reduce((counts, item) => {
    counts[item.id] = Math.max(0, Number(state.materials?.[item.id] || 0))
    return counts
  }, {})
  gameState.value = {
    ...gameState.value,
    completedTaskIds,
    taskProgress,
    taskClaims,
    agencyArchiveIds: Array.isArray(state.agencyArchiveIds) ? state.agencyArchiveIds : [],
    materialCounts
  }
}

function serverTaskCode(taskId) {
  if (taskId === 'meridian-route') return taskId
  if (taskId?.startsWith('meridian-route-')) return taskId
  if (taskId?.startsWith('meridian-') && taskId.endsWith('-route')) {
    const slug = taskId.slice('meridian-'.length, -'-route'.length)
    return `meridian-route-${slug}`
  }
  if (taskId?.startsWith('copper-man-star-')) return 'copper-man-daily-case'
  if (taskId === 'agency-first-archive') return 'main-repair-agency'
  return taskId
}

function localTaskIdForServerTask(taskCode) {
  if (taskCode?.startsWith('meridian-route-')) {
    const slug = taskCode.slice('meridian-route-'.length)
    return `meridian-${slug}-route`
  }
  return null
}

function isSupportedServerTask(taskCode) {
  const routeTasks = [
    'lung', 'large-intestine', 'stomach', 'spleen', 'heart', 'small-intestine',
    'bladder', 'kidney', 'pericardium', 'sanjiao', 'gallbladder', 'liver', 'ren', 'du'
  ].map((slug) => `meridian-route-${slug}`)
  return new Set([
    'map.checkin', 'daily-read-copper-story', 'daily-light-hand-stars', 'daily-safety-quiz',
    'main-safety-case',
    'main-mist-in-xinglin', 'main-hand-star-map', 'main-repair-agency',
    'meridian-river-completion', 'copper-man-daily-case', 'review-daily',
    ...routeTasks
  ]).has(taskCode)
    || /^meridian-match-(easy|normal|hard)-(50|100|200|350|500|750|1000)$/.test(taskCode)
    || /^meridian-sort-(50|100|150|200|250|300)$/.test(taskCode)
}

async function submitServerTask(taskId, eventType = 'TASK_COMPLETED', progressDelta = 1, periodKey = 'lifetime', options = {}) {
  const store = useUserStore()
  if (!store.isLoggedIn || !store.userId) return null
  const taskCode = serverTaskCode(taskId)
  if (!isSupportedServerTask(taskCode)) return null
  const response = await submitGameProgressEvent({
    gameCode: taskCode.startsWith('meridian') ? 'meridian-river' : 'xinglin',
    eventType,
    taskCode,
    periodKey,
    resultCode: options.resultCode || 'CLIENT_COMPLETION',
    progressDelta
  }, {
    idempotencyKey: options.idempotencyKey || `game:${store.userId}:${taskCode}:${periodKey}`
  })
  if (response?.state && typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('game-state-refresh', { detail: response.state }))
  }
  return response
}

async function recordTaskProgress(taskId, progressDelta = 1, options = {}) {
  const store = useUserStore()
  if (!store.isLoggedIn || !store.userId) {
    const task = findTask(taskId)
    const target = Number(options.target || task?.total || 1)
    const previous = getTaskProgress(taskId)
    const next = Math.min(target, previous + Math.max(0, Number(progressDelta || 0)))
    const completedTaskIds = next >= target && !isCompleted(taskId)
      ? [...gameState.value.completedTaskIds, taskId]
      : gameState.value.completedTaskIds
    const materialCounts = { ...gameState.value.materialCounts }
    if (next >= target && previous < target) {
      const rewards = task?.rewards || []
      rewards.forEach((reward) => {
        if (reward?.id) materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + Number(reward.count || 0)
      })
    }
    gameState.value = {
      ...gameState.value,
      taskProgress: { ...gameState.value.taskProgress, [taskId]: next },
      completedTaskIds,
      materialCounts
    }
    return { success: true, localOnly: true, progress: next, completed: next >= target }
  }
  try {
    const response = await submitServerTask(taskId, 'TASK_PROGRESS', progressDelta, options.periodKey || 'lifetime', {
      resultCode: options.resultCode || 'ROUTE_POINT_COMPLETE',
      idempotencyKey: options.idempotencyKey
    })
    return response ? { success: true, response } : { success: false }
  } catch (error) {
    return { success: false, error }
  }
}

async function resetTaskProgress(taskId, options = {}) {
  const store = useUserStore()
  if (!store.isLoggedIn || !store.userId) return { success: true, localOnly: true }
  try {
    const response = await submitServerTask(taskId, 'TASK_RESET', 1, options.periodKey || 'lifetime', {
      resultCode: 'ROUTE_RESET',
      idempotencyKey: options.idempotencyKey
    })
    return response ? { success: true, response } : { success: false }
  } catch (error) {
    return { success: false, error }
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('game-state-refresh', (event) => applyServerState(event.detail))
    watch(
    gameState,
    (value) => {
      window.localStorage.setItem(STORY_STORAGE_KEY, JSON.stringify({
        storyArchiveIds: value.storyArchiveIds || [],
        storyProgress: value.storyProgress || {}
      }))
      if (!hasServerSession()) {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
      }
    },
    { deep: true }
  )
}

function cloneReward(reward) {
  return {
    ...reward,
    count: Number(reward.count || 0)
  }
}

function findTask(taskId) {
  const serverTask = getMainlineTask(serverTaskCode(taskId))
  if (serverTask?.taskCode) {
    const staticTask = [...baseDailyTasks, ...baseMainTasks].find((task) => task.id === taskId || task.id === serverTask.taskCode)
    const staticRewards = new Map((staticTask?.rewards || []).map((reward) => [reward.id, reward]))
    return {
      ...(staticTask || {}),
      id: taskId,
      name: serverTask.name || staticTask?.name || taskId,
      description: serverTask.description || staticTask?.description || '',
      route: serverTask.route || staticTask?.route || '/home-map',
      total: Number(serverTask.target || staticTask?.total || 1),
      rewards: (serverTask.rewards || staticTask?.rewards || []).map((reward) => ({
        id: reward.itemCode || reward.id,
        name: reward.name || staticRewards.get(reward.itemCode)?.name || reward.itemCode || reward.id,
        count: Number(reward.amount ?? reward.count ?? 0),
        icon: reward.icon || staticRewards.get(reward.itemCode)?.icon
      }))
    }
  }
  return [...baseDailyTasks, ...baseMainTasks].find((task) => task.id === taskId)
}

function rewardText(rewards = []) {
  return rewards.map((reward) => `${reward.name} × ${reward.count}`).join('，')
}

function isCompleted(taskId) {
  const taskCode = serverTaskCode(taskId)
  return gameState.value.completedTaskIds.includes(taskId)
    || gameState.value.completedTaskIds.includes(taskCode)
}

function getTaskProgress(taskId) {
  const taskCode = serverTaskCode(taskId)
  return Number(gameState.value.taskProgress?.[taskId]
    ?? gameState.value.taskProgress?.[taskCode]
    ?? 0)
}

function isRewardClaimed(taskId) {
  const taskCode = serverTaskCode(taskId)
  return Boolean(gameState.value.taskClaims?.[taskId] || gameState.value.taskClaims?.[taskCode])
}

function createStoryProgress() {
  return {
    readPageIds: [],
    clueIds: [],
    evidenceOrder: [],
    reasoningAnswerId: null,
    safetyAnswerId: null,
    stars: 0,
    completed: false,
    rewardClaimed: false,
    threeStarRewardClaimed: false,
    threeStarRewardIds: []
  }
}

function getStoryProgress(caseId) {
  const stored = gameState.value.storyProgress?.[caseId] || {}
  const defaults = createStoryProgress()
  return {
    ...defaults,
    ...stored,
    readPageIds: Array.isArray(stored.readPageIds) ? stored.readPageIds : defaults.readPageIds,
    clueIds: Array.isArray(stored.clueIds) ? stored.clueIds : defaults.clueIds,
    evidenceOrder: Array.isArray(stored.evidenceOrder) ? stored.evidenceOrder : defaults.evidenceOrder,
    threeStarRewardIds: Array.isArray(stored.threeStarRewardIds)
      ? stored.threeStarRewardIds
      : defaults.threeStarRewardIds
  }
}

function saveStoryProgress(caseId, patch = {}) {
  if (!caseId) return createStoryProgress()

  const nextProgress = {
    ...getStoryProgress(caseId),
    ...patch
  }

  gameState.value = {
    ...gameState.value,
    storyProgress: {
      ...(gameState.value.storyProgress || {}),
      [caseId]: nextProgress
    }
  }

  return nextProgress
}

function hydrateStoryProgress(serverProgressByCase = {}) {
  if (!serverProgressByCase || typeof serverProgressByCase !== 'object') return

  const nextProgress = { ...(gameState.value.storyProgress || {}) }
  const nextArchives = new Set(gameState.value.storyArchiveIds || [])
  Object.entries(serverProgressByCase).forEach(([caseId, payload]) => {
    const serverData = payload?.data && typeof payload.data === 'object' ? payload.data : payload
    if (!serverData || typeof serverData !== 'object' || !Object.keys(serverData).length) return
    nextProgress[caseId] = {
      ...createStoryProgress(),
      ...serverData,
      readPageIds: Array.isArray(serverData.readPageIds) ? serverData.readPageIds : [],
      clueIds: Array.isArray(serverData.clueIds) ? serverData.clueIds : [],
      evidenceOrder: Array.isArray(serverData.evidenceOrder) ? serverData.evidenceOrder : [],
      threeStarRewardIds: Array.isArray(serverData.threeStarRewardIds) ? serverData.threeStarRewardIds : []
    }
    if (payload?.completed || serverData.completed) nextArchives.add(caseId)
  })

  gameState.value = {
    ...gameState.value,
    storyProgress: nextProgress,
    storyArchiveIds: [...nextArchives]
  }
}

async function completeTask(taskId) {
  const task = findTask(taskId)
  if (!task || isCompleted(taskId)) return false

  const store = useUserStore()
  if (store.isLoggedIn && store.userId) {
    try {
      await submitServerTask(taskId, 'TASK_COMPLETED', 1, 'lifetime', { resultCode: 'CLIENT_COMPLETION' })
    } catch (error) {
      console.warn('游戏任务提交失败，保留未完成状态等待重试', error)
      return false
    }
  }

  const materialCounts = { ...gameState.value.materialCounts }
  ;(task.rewards || []).forEach((reward) => {
    if (!reward.id) return
    if (!store.isLoggedIn) materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + Number(reward.count || 0)
  })

  gameState.value = {
    ...gameState.value,
    completedTaskIds: [...gameState.value.completedTaskIds, taskId],
    materialCounts
  }
  return true
}

async function addMaterials(rewards = [], taskId = null, options = {}) {
  if (!Array.isArray(rewards) || rewards.length === 0) return false

  const store = useUserStore()
  if (store.isLoggedIn && store.userId) {
    if (!taskId) return false
    try {
      await submitServerTask(taskId, options.eventType || 'TASK_COMPLETED', options.progressDelta || 1,
        options.periodKey || 'lifetime', {
          resultCode: options.resultCode || 'CLIENT_COMPLETION',
          idempotencyKey: options.idempotencyKey
        })
      return true
    } catch (error) {
      console.warn('奖励提交失败，等待重试', error)
      return false
    }
  }

  const materialCounts = { ...gameState.value.materialCounts }
  let changed = false

  rewards.forEach((reward) => {
    if (!reward?.id) return
    const count = Number(reward.count || 0)
    if (count <= 0) return
    materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + count
    changed = true
  })

  if (!changed) return false

  gameState.value = {
    ...gameState.value,
    materialCounts
  }
  return true
}

async function completeStandaloneTask(taskId, rewards = [], options = {}) {
  const store = useUserStore()
  const canClaimCompletedServerTask = Boolean(store.isLoggedIn && store.userId && options.allowCompletedClaim)
  if (!taskId || (isCompleted(taskId) && !canClaimCompletedServerTask)) {
    return { success: false, alreadyCompleted: true }
  }

  if (store.isLoggedIn && store.userId) {
    try {
      const response = await submitServerTask(taskId, options.eventType || 'TASK_COMPLETED', options.progressDelta || 1,
        options.periodKey || 'lifetime', {
          resultCode: options.resultCode || 'CLIENT_COMPLETION',
          idempotencyKey: options.idempotencyKey
        })
      if (!response) return { success: false }
      return { success: true, response }
    } catch (error) {
      console.warn('独立任务奖励提交失败，等待重试', error)
      return { success: false, error }
    }
  }

  const materialCounts = { ...gameState.value.materialCounts }
  ;(rewards || []).forEach((reward) => {
    if (!reward?.id) return
    materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + Number(reward.count || 0)
  })

  gameState.value = {
    ...gameState.value,
    completedTaskIds: [...gameState.value.completedTaskIds, taskId],
    materialCounts
  }
  return { success: true }
}

async function completeStoryCase(caseId, result = {}, rewardConfig = {}) {
  if (!caseId) return { completed: false, stars: 0, rewardsGranted: [] }

  const current = getStoryProgress(caseId)
  const stars = Math.max(Number(current.stars || 0), Math.min(3, Number(result.stars || 1)))
  const rewardsGranted = []
  const claimedThreeStarRewardIds = new Set(current.threeStarRewardIds)
  const dailyTaskId = rewardConfig.dailyTaskId
  const threeStarRewards = Array.isArray(rewardConfig.threeStars) ? rewardConfig.threeStars : []
  const mainTaskId = rewardConfig.mainTaskId

  if (!current.rewardClaimed && mainTaskId) {
    const task = findTask(mainTaskId)
    if (await completeTask(mainTaskId)) {
      rewardsGranted.push(...(task?.rewards || []).map(cloneReward))
    }
  }

  if (!current.rewardClaimed && dailyTaskId) {
    const task = findTask(dailyTaskId)
    if (await completeTask(dailyTaskId)) {
      rewardsGranted.push(...(task?.rewards || []).map(cloneReward))
    }
  }

  if (stars >= 3) {
    const materialCounts = { ...gameState.value.materialCounts }
    ;(threeStarRewards || []).forEach((reward) => {
      if (!reward?.id || claimedThreeStarRewardIds.has(reward.id) || Number(reward.count || 0) <= 0) return
      const material = baseMaterials.find((item) => item.id === reward.id)
      if (!hasServerSession() && material?.kind !== 'collectible') {
        materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + Number(reward.count || 0)
      }
      rewardsGranted.push(cloneReward(reward))
      claimedThreeStarRewardIds.add(reward.id)
    })
    gameState.value = { ...gameState.value, materialCounts }
  }

  const storyArchiveIds = gameState.value.storyArchiveIds || []
  const nextArchiveIds = storyArchiveIds.includes(caseId)
    ? storyArchiveIds
    : [...storyArchiveIds, caseId]

  const nextProgress = {
    ...current,
    ...result,
    stars,
    completed: true,
    rewardClaimed: true,
    threeStarRewardClaimed: current.threeStarRewardClaimed || stars >= 3,
    threeStarRewardIds: [...claimedThreeStarRewardIds]
  }

  gameState.value = {
    ...gameState.value,
    storyArchiveIds: nextArchiveIds,
    storyProgress: {
      ...(gameState.value.storyProgress || {}),
      [caseId]: nextProgress
    }
  }

  return {
    completed: true,
    firstCompletion: !current.completed,
    stars,
    rewardsGranted
  }
}

function hasMaterials(costs = []) {
  return costs.every((cost) => {
    if (!cost?.id) return true
    return Number(gameState.value.materialCounts[cost.id] || 0) >= Number(cost.count || 0)
  })
}

async function exchangeAgencyArchiveItem(item) {
  const ownedIds = gameState.value.agencyArchiveIds || []
  if (!item?.id || ownedIds.includes(item.id)) return false
  if ((item.prerequisites || []).some((id) => !ownedIds.includes(id))) return false
  if (!hasMaterials(item.costs || [])) return false

  if (hasServerSession()) {
    const store = useUserStore()
    const response = await exchangeAgencyArchiveItemApi(item.id, {
      idempotencyKey: `agency:${store.userId}:${item.id}`
    })
    if (response?.state) applyServerState(response.state)
    return Boolean(response?.state?.agencyArchiveIds?.includes(item.id))
  }

  const materialCounts = { ...gameState.value.materialCounts }
  ;(item.costs || []).forEach((cost) => {
    if (!cost?.id) return
    materialCounts[cost.id] = Math.max(0, Number(materialCounts[cost.id] || 0) - Number(cost.count || 0))
  })

  const agencyArchiveIds = [...ownedIds, item.id]
  const shouldCompleteMainline = Boolean(item.completesMainline)
  const completedTaskIds = !shouldCompleteMainline || gameState.value.completedTaskIds.includes('main-repair-agency')
    ? gameState.value.completedTaskIds
    : [...gameState.value.completedTaskIds, 'main-repair-agency']

  gameState.value = {
    ...gameState.value,
    completedTaskIds,
    agencyArchiveIds,
    materialCounts
  }
  return true
}

function isAgencyArchiveItemOwned(itemId) {
  return (gameState.value.agencyArchiveIds || []).includes(itemId)
}

function addReviewRecord(record) {
  if (!record?.id) return false

  const existing = gameState.value.reviewRecords || []
  const duplicate = existing.find((item) => item.id === record.id && item.status !== 'repaired')
  if (duplicate) {
    gameState.value = {
      ...gameState.value,
      reviewRecords: existing.map((item) => item.id === record.id
        ? { ...item, wrongCount: Number(item.wrongCount || 0) + 1 }
        : item
      )
    }
    return true
  }

  const nextRecord = {
    type: 'acupoint',
    title: '还没有完全点亮的星星',
    prompt: '',
    correctAnswer: '',
    choices: [],
    sourcePath: '',
    ...record,
    status: 'dim',
    wrongCount: 1,
    createdAt: Date.now(),
    repairedAt: null
  }

  gameState.value = {
    ...gameState.value,
    reviewRecords: [nextRecord, ...existing]
  }
  return true
}

function repairReviewRecord(recordId, rewards = []) {
  const records = gameState.value.reviewRecords || []
  const target = records.find((item) => item.id === recordId)
  if (!target || target.status === 'repaired') return false

  const materialCounts = { ...gameState.value.materialCounts }
  ;(rewards || []).forEach((reward) => {
    if (!reward?.id) return
    materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + Number(reward.count || 0)
  })

  gameState.value = {
    ...gameState.value,
    materialCounts,
    reviewRecords: records.map((item) => item.id === recordId
      ? { ...item, status: 'repaired', repairedAt: Date.now() }
      : item
    )
  }
  return true
}

function resetGameState() {
  gameState.value = createInitialState()
}

function badgeProgressFor(badge) {
  const done = gameState.value.completedTaskIds
  const completedCount = done.length
  const agencyArchiveCount = (gameState.value.agencyArchiveIds || []).length
  const storyArchiveCount = (gameState.value.storyArchiveIds || []).length
  const repairedReviewCount = (gameState.value.reviewRecords || []).filter((item) => item.status === 'repaired').length
  const copperSolvedCount = done.filter((taskId) => taskId.startsWith('copper-man-star-')).length
  const baseProgress = Number(badge.progress || 0)

  const progressMap = {
    'copper-apprentice': baseProgress + copperSolvedCount + (done.includes('daily-light-hand-stars') ? 3 : 0),
    'acupoint-detective': Math.min(5, baseProgress + copperSolvedCount + (done.includes('daily-light-hand-stars') ? 1 : 0)),
    'meridian-adventurer': Math.min(3, baseProgress +
      (done.includes('meridian-large-intestine-route') ? 1 : 0) +
      (done.includes('meridian-stomach-route') ? 1 : 0)
    ),
    'story-collector': Math.min(4, baseProgress + storyArchiveCount),
    'safe-little-healer': done.includes('daily-safety-quiz') ? 1 : baseProgress,
    'starlight-repairer': repairedReviewCount > 0 || completedCount >= 3 ? 1 : baseProgress,
    'xinglin-little-herbalist': Math.min(6, completedCount + baseProgress + Math.min(2, agencyArchiveCount)),
    'xinglin-heritage-messenger': completedCount >= 6 ? 1 : baseProgress
  }

  return Math.min(Number(badge.total || 1), progressMap[badge.id] ?? baseProgress)
}

function badgeStatusFor(badge, progress) {
  if (progress >= Number(badge.total || 1)) return 'earned'
  if (badge.status === 'locked' && progress <= 0) return 'locked'
  if (badge.status === 'available') return 'available'
  return 'in-progress'
}

function mapStateFor(level, levels) {
  const previousLevel = levels.find((item) => item.order === level.order - 1)
  const completed = level.requiredTaskIds.every((taskId) => isCompleted(taskId))
  const previousCompleted = !previousLevel || previousLevel.requiredTaskIds.every((taskId) => isCompleted(taskId))
  const unlocked = level.order === 1 || previousCompleted
  const current = unlocked && !completed
  const progress = level.requiredTaskIds.reduce((sum, taskId) => sum + getTaskProgress(taskId), 0)
  const target = level.requiredTaskIds.reduce((sum, taskId) => {
    const task = findTask(taskId)
    return sum + Number(task?.total || level.target || 1)
  }, 0)
  return {
    status: completed ? '已完成' : current ? '当前关卡' : unlocked ? '已解锁' : '待解锁',
    state: completed ? 'done' : current ? 'current' : unlocked ? 'open' : 'locked',
    unlocked,
    completed,
    current,
    progress: Math.min(progress, target),
    target
  }
}

export function useGameState() {
  const completedTaskIds = computed(() => gameState.value.completedTaskIds)
  const agencyArchiveIds = computed(() => gameState.value.agencyArchiveIds || [])
  const storyArchiveIds = computed(() => gameState.value.storyArchiveIds || [])
  const storyProgress = computed(() => gameState.value.storyProgress || {})
  const reviewRecords = computed(() => gameState.value.reviewRecords || [])

  const materials = computed(() => {
    return baseMaterials.filter((item) => item.kind !== 'collectible').map((item) => ({
      ...item,
      count: Number(gameState.value.materialCounts[item.id] || 0),
      status: Number(gameState.value.materialCounts[item.id] || 0) > 0 ? 'owned' : 'locked'
    }))
  })

  const dailyTasks = computed(() => {
    return baseDailyTasks.map((task) => {
      const completed = isCompleted(task.id)
      return {
        ...task,
        status: completed ? 'completed' : task.status,
        progress: completed ? task.total : task.progress,
        completed,
        rewardText: rewardText(task.rewards),
        rewards: (task.rewards || []).map(cloneReward)
      }
    })
  })

  const badges = computed(() => {
    return baseBadges.map((badge) => {
      const progress = badgeProgressFor(badge)
      return {
        ...badge,
        progress,
        status: badgeStatusFor(badge, progress)
      }
    })
  })

  const mapLevels = computed(() => {
    const levels = mainlineConfig.value?.levels?.length ? mainlineConfig.value.levels : baseMapLevels
    return levels.map((level) => ({
      ...level,
      ...mapStateFor(level, levels)
    }))
  })

  return {
    completedTaskIds,
    materials,
    dailyTasks,
    badges,
    mapLevels,
    agencyArchiveIds,
    storyArchiveIds,
    storyProgress,
    getTaskProgress,
    isRewardClaimed,
    reviewRecords,
    completeTask,
    recordTaskProgress,
    resetTaskProgress,
    addMaterials,
    completeStandaloneTask,
    completeStoryCase,
    getStoryProgress,
    saveStoryProgress,
    hydrateStoryProgress,
    exchangeAgencyArchiveItem,
    isAgencyArchiveItemOwned,
    hasMaterials,
    addReviewRecord,
    repairReviewRecord,
    isCompleted,
    resetGameState
  }
}

export { applyServerState }

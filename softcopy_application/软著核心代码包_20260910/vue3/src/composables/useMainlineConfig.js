import { computed, ref } from 'vue'
import { getMainlineConfig } from '@/api/MainlineConfigApi'
import { mainlineLevels as fallbackLevels } from '@/data/mainline'
import { dailyTasks, mainTasks } from '@/data/tasks'
import { materials } from '@/data/materials'

const taskFallbacks = new Map([...dailyTasks, ...mainTasks].map((task) => [task.id, task]))
const config = ref(createFallbackConfig())
const loading = ref(false)
let pendingRequest = null

function createFallbackConfig() {
  return {
    revisionId: 1,
    gameCode: 'xinglin-mainline',
    version: 1,
    status: 'FALLBACK',
    levels: fallbackLevels.map((level) => ({
      ...level,
      coverPath: '',
      prerequisiteLevelIds: level.order > 1 ? [fallbackLevels[level.order - 2].id] : [],
      tasks: [createFallbackTask(level.requiredTaskIds[0], level)]
    }))
  }
}

function createFallbackTask(taskCode, level) {
  const staticTask = taskFallbacks.get(taskCode)
  return {
    taskCode,
    levelId: level.id,
    name: staticTask?.name || level.label,
    description: staticTask?.description || level.description,
    taskType: staticTask?.type || 'mainline',
    route: staticTask?.route || level.route,
    target: Number(staticTask?.total || level.target || 1),
    editable: level.order > 1,
    rewards: (staticTask?.rewards || []).map((reward) => ({
      rewardType: 'MATERIAL',
      itemCode: reward.id,
      amount: Number(reward.count || 0),
      scoreDelta: 0
    }))
  }
}

function rewardName(code) {
  return materials.find((item) => item.id === code)?.name || code
}

function normalizeTask(task, level) {
  const fallback = createFallbackTask(task?.taskCode || level.requiredTaskIds[0], level)
  return {
    ...fallback,
    ...task,
    taskCode: task?.taskCode || fallback.taskCode,
    levelId: task?.levelId || level.id,
    target: Number(task?.target ?? fallback.target ?? 1),
    rewards: Array.isArray(task?.rewards)
      ? task.rewards.map((reward) => ({
        ...reward,
        amount: Number(reward.amount || 0),
        scoreDelta: Number(reward.scoreDelta || 0),
        name: reward.name || rewardName(reward.itemCode)
      }))
      : fallback.rewards
  }
}

function normalizeConfig(remote) {
  if (!remote || !Array.isArray(remote.levels) || remote.levels.length !== 8) {
    return createFallbackConfig()
  }
  return {
    ...remote,
    levels: remote.levels
      .slice()
      .sort((a, b) => Number(a.order || 0) - Number(b.order || 0))
      .map((level) => {
        const fallback = fallbackLevels.find((item) => item.id === level.id) || fallbackLevels[level.order - 1]
        const merged = { ...fallback, ...level }
        const requiredTaskIds = Array.isArray(level.requiredTaskIds) && level.requiredTaskIds.length
          ? level.requiredTaskIds
          : fallback.requiredTaskIds
        return {
          ...merged,
          requiredTaskIds,
          prerequisiteLevelIds: Array.isArray(level.prerequisiteLevelIds) ? level.prerequisiteLevelIds : [],
          tasks: Array.isArray(level.tasks) && level.tasks.length
            ? level.tasks.map((task) => normalizeTask(task, { ...merged, requiredTaskIds }))
            : [normalizeTask(null, { ...merged, requiredTaskIds })]
        }
      })
  }
}

export async function loadMainlineConfig(options = {}) {
  if (pendingRequest && !options.force) return pendingRequest
  loading.value = true
  pendingRequest = getMainlineConfig({ showDefaultMsg: false })
    .then((remote) => {
      config.value = normalizeConfig(remote)
      return config.value
    })
    .catch((error) => {
      // Public gameplay remains usable with the checked-in baseline.
      if (!config.value?.levels?.length) config.value = createFallbackConfig()
      throw error
    })
    .finally(() => {
      loading.value = false
      pendingRequest = null
    })
  return pendingRequest
}

export function getMainlineTask(taskCode) {
  for (const level of config.value.levels || []) {
    const task = level.tasks?.find((item) => item.taskCode === taskCode)
    if (task) return task
  }
  return taskFallbacks.get(taskCode) || null
}

export function useMainlineConfig() {
  return {
    config: computed(() => config.value),
    levels: computed(() => config.value.levels || []),
    loading: computed(() => loading.value),
    loadMainlineConfig
  }
}

export { config as mainlineConfig }

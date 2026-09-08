import request from '@/utils/request'

/** 获取服务端权威游戏状态。 */
export function getGameState(config = {}) {
  return request.get('/game/state', null, { enableCache: false, ...config })
}

/** 获取当前登录用户未完成的安全课堂草稿。 */
export function getSafetyGameDraft(config = {}) {
  return request.get('/game/safety/draft', null, { enableCache: false, showDefaultMsg: false, ...config })
}

/** 覆盖保存当前登录用户的安全课堂草稿。 */
export function saveSafetyGameDraft(draft, config = {}) {
  return request.put('/game/safety/draft', { draft }, { showDefaultMsg: false, ...config })
}

/** 完成或主动重开后清除服务端草稿。 */
export function clearSafetyGameDraft(config = {}) {
  return request.delete('/game/safety/draft', { showDefaultMsg: false, ...config })
}

/** 提交一个受后端白名单约束的游戏行为结果。 */
export function submitGameProgressEvent(command, config = {}) {
  return request.post('/game/progress/events', command, {
    showDefaultMsg: false,
    ...config
  })
}

/** 登录后一次性导入游客 localStorage 状态。 */
export function importLegacyGameState(command, config = {}) {
  return request.post('/game/legacy-import', command, {
    showDefaultMsg: false,
    ...config
  })
}

/** 使用服务端权威材料修复侦探社档案项目。 */
export function exchangeAgencyArchiveItem(itemId, config = {}) {
  return request.post('/game/agency/archive-items/exchange', { itemId }, {
    showDefaultMsg: false,
    ...config
  })
}

/** 获取本周三选一奖励资格与可选材料。 */
export function getWeeklyChoiceReward(config = {}) {
  return request.get('/game/weekly-choice', null, { enableCache: false, showDefaultMsg: false, ...config })
}

/** 领取本周选中的一份材料奖励。 */
export function claimWeeklyChoiceReward(itemCode, config = {}) {
  return request.post('/game/weekly-choice/claim', { itemCode }, { showDefaultMsg: false, ...config })
}

/** 获取当前孩子可读的奖励增减记录。 */
export function getRewardLedger(limit = 50, config = {}) {
  return request.get('/game/rewards/ledger', { limit }, { enableCache: false, showDefaultMsg: false, ...config })
}

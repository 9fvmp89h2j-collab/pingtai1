import request from '@/utils/request'

export function getSafetyGameConfig(config = {}) {
  return request.get('/game/safety/config', null, { showDefaultMsg: false, ...config })
}

export function submitSafetyScenarioAnswer(data, config = {}) {
  return request.post('/game/safety/scenario/answer', data, { showDefaultMsg: false, ...config })
}

export function getSafetyMemoryProgress(config = {}) {
  return request.get('/game/safety/memory-progress', null, { showDefaultMsg: false, ...config })
}

export function completeSafetyMemoryLevel(completedLevel, config = {}) {
  return request.put('/game/safety/memory-progress', { completedLevel }, { showDefaultMsg: false, ...config })
}

export function getAdminSafetyGameConfig(config = {}) {
  return request.get('/admin/safety-game/config', null, { showDefaultMsg: false, ...config })
}

export function saveSafetyGameDraft(data, config = {}) {
  return request.put('/admin/safety-game/config/draft', data, { showDefaultMsg: false, ...config })
}

export function validateSafetyGameDraft(data, config = {}) {
  return request.post('/admin/safety-game/config/draft/validate', data, { showDefaultMsg: false, ...config })
}

export function publishSafetyGameDraft(data, config = {}) {
  return request.post('/admin/safety-game/config/draft/publish', data, { showDefaultMsg: false, ...config })
}

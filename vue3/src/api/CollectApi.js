import request from '@/utils/request'

export function getMyCollect(callbacks) {
  return request.get('/user/collect', {}, { enableCache: false, ...callbacks })
}

export function addSkillToCollect(params, callbacks) {
  return request.post('/user/collect/add', params, callbacks)
}

export function removeSkillFromCollect(params, callbacks) {
  return request.post('/user/collect/remove', params, callbacks)
}

export function hasSkillCollect(params, callbacks) {
  return request.get('/user/collect/has', params, { enableCache: false, ...callbacks })
}

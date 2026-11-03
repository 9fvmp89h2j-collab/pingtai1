import request from '@/utils/request'

export function getMainlineConfig(config = {}) {
  return request.get('/game/mainline-config', null, { showDefaultMsg: false, ...config })
}

export function getAdminMainlineConfig(config = {}) {
  return request.get('/admin/mainline-config', null, { showDefaultMsg: false, ...config })
}

export function saveMainlineDraft(data, config = {}) {
  return request.put('/admin/mainline-config/draft', data, { showDefaultMsg: false, ...config })
}

export function validateMainlineDraft(data, config = {}) {
  return request.post('/admin/mainline-config/draft/validate', data, { showDefaultMsg: false, ...config })
}

export function publishMainlineDraft(data, config = {}) {
  return request.post('/admin/mainline-config/draft/publish', data, { showDefaultMsg: false, ...config })
}

export function restoreMainlineVersion(version, config = {}) {
  return request.post(`/admin/mainline-config/versions/${version}/restore`, null, { showDefaultMsg: false, ...config })
}

import request from '@/utils/request'

export function getJingluoPage(params, config = {}) {
  return request.get('/admin/jingluo/page', params, config)
}

export function getJingluoById(id, config = {}) {
  return request.get(`/admin/jingluo/${id}`, null, config)
}

export function createJingluo(data, config = {}) {
  return request.post('/admin/jingluo/create', data, config)
}

export function updateJingluo(id, data, config = {}) {
  return request.put(`/admin/jingluo/${id}`, data, config)
}

export function deleteJingluo(id, config = {}) {
  return request.delete(`/admin/jingluo/${id}`, config)
}

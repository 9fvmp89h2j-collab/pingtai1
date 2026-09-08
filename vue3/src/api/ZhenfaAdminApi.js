import request from '@/utils/request'

export function getZhenfaPage(params, config = {}) {
  return request.get('/admin/zhenfa/page', params, config)
}

export function getZhenfaById(id, config = {}) {
  return request.get(`/admin/zhenfa/${id}`, null, config)
}

export function createZhenfa(data, config = {}) {
  return request.post('/admin/zhenfa/create', data, config)
}

export function updateZhenfa(id, data, config = {}) {
  return request.put(`/admin/zhenfa/${id}`, data, config)
}

export function deleteZhenfa(id, config = {}) {
  return request.delete(`/admin/zhenfa/${id}`, config)
}


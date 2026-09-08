import request from '@/utils/request'

export function getXueweiPage(params, config = {}) {
  return request.get('/admin/xuewei/page', params, config)
}

export function getXueweiById(id, config = {}) {
  return request.get(`/admin/xuewei/${id}`, null, config)
}

export function createXuewei(data, config = {}) {
  return request.post('/admin/xuewei/create', data, config)
}

export function updateXuewei(id, data, config = {}) {
  return request.put(`/admin/xuewei/${id}`, data, config)
}

export function deleteXuewei(id, config = {}) {
  return request.delete(`/admin/xuewei/${id}`, config)
}


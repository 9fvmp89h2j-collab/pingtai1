import request from '@/utils/request'

export function pageIllness(params) {
  return request.get('/admin/illness/page', params)
}

export function getIllnessDetail(id) {
  return request.get(`/admin/illness/${id}`)
}

export function createIllness(data) {
  return request.post('/admin/illness', data)
}

export function updateIllness(id, data) {
  return request.put(`/admin/illness/${id}`, data)
}

export function deleteIllness(id) {
  return request.delete(`/admin/illness/${id}`)
}


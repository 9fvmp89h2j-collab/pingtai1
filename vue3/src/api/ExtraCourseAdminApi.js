import request from '@/utils/request'

export function getExtraCoursePage(params, config = {}) {
  return request.get('/admin/extracourse/page', params, config)
}

export function getExtraCourseById(id, config = {}) {
  return request.get(`/admin/extracourse/${id}`, null, config)
}

export function createExtraCourse(data, config = {}) {
  return request.post('/admin/extracourse/create', data, config)
}

export function updateExtraCourse(id, data, config = {}) {
  return request.put(`/admin/extracourse/${id}`, data, config)
}

export function deleteExtraCourse(id, config = {}) {
  return request.delete(`/admin/extracourse/${id}`, config)
}


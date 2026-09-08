import request from '@/utils/request'

export function getDoctorStoryPage(params, config = {}) {
  return request.get('/admin/doctorstory/page', params, config)
}

export function getDoctorStoryById(id, config = {}) {
  return request.get(`/admin/doctorstory/${id}`, null, config)
}

export function createDoctorStory(data, config = {}) {
  return request.post('/admin/doctorstory/create', data, config)
}

export function updateDoctorStory(id, data, config = {}) {
  return request.put(`/admin/doctorstory/${id}`, data, config)
}

export function deleteDoctorStory(id, config = {}) {
  return request.delete(`/admin/doctorstory/${id}`, config)
}


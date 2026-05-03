import request from '@/utils/request'

export function getOriginStoryPage(params, config = {}) {
  return request.get('/admin/originstory/page', params, config)
}

export function createOriginStory(data, config = {}) {
  return request.post('/admin/originstory/create', data, config)
}

export function updateOriginStory(id, data, config = {}) {
  return request.put(`/admin/originstory/${id}`, data, config)
}

export function deleteOriginStory(id, config = {}) {
  return request.delete(`/admin/originstory/${id}`, config)
}


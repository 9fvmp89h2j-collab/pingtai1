import request from '@/utils/request'

export function getTrainGamePage(params, config = {}) {
  return request.get('/admin/train-game/page', params, config)
}

export function getTrainGameById(id, config = {}) {
  return request.get(`/admin/train-game/${id}`, null, config)
}

export function createTrainGame(data, config = {}) {
  return request.post('/admin/train-game/create', data, config)
}

export function updateTrainGame(id, data, config = {}) {
  return request.put(`/admin/train-game/${id}`, data, config)
}

export function deleteTrainGame(id, config = {}) {
  return request.delete(`/admin/train-game/${id}`, config)
}

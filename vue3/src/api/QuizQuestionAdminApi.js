import request from '@/utils/request'

export function getQuizQuestionPage(params, config = {}) {
  return request.get('/admin/quiz-question/page', params, config)
}

export function getQuizQuestionById(id, config = {}) {
  return request.get(`/admin/quiz-question/${id}`, null, config)
}

export function createQuizQuestion(data, config = {}) {
  return request.post('/admin/quiz-question/create', data, config)
}

export function updateQuizQuestion(id, data, config = {}) {
  return request.put(`/admin/quiz-question/${id}`, data, config)
}

export function deleteQuizQuestion(id, config = {}) {
  return request.delete(`/admin/quiz-question/${id}`, config)
}

export function getQuizStage(stageCode, config = {}) {
  return request.get(`/admin/quiz-question/stage/${stageCode}`, null, config)
}

export function updateQuizStage(stageCode, questionIds, config = {}) {
  return request.put(`/admin/quiz-question/stage/${stageCode}`, { questionIds }, config)
}

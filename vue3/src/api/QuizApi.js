/**
 * 答题闯关游戏相关API接口
 */

import request from '@/utils/request'

/**
 * 获取答题题目列表（随机抽取5道题）
 * @param {object} params - 请求参数
 * @param {object} callbacks - 回调函数 { onSuccess, onError }
 * @returns {Promise}
 */
export function getQuizQuestions(params, callbacks) {
  return request.get('/quiz/questions/random', params, callbacks)
}

/**
 * 提交答题结果
 * @param {object} params - 请求参数 { answers: Array, correctCount: Number, totalCount: Number }
 * @param {object} callbacks - 回调函数 { onSuccess, onError, successMsg }
 * @returns {Promise}
 */
export function submitQuizResult(params, callbacks) {
  return request.post('/quiz/result/submit', params, callbacks)
}

/**
 * 提交单题答案（终极考验 / 新版闯关）
 * @param {object} data - { questionId, userAnswer: 'A'|'B'|'C'|'D' }
 */
export function submitQuizAnswer(data, callbacks) {
  return request.post('/quiz/answer/submit', data, callbacks)
}

export function getQuizStage(stageCode, callbacks) {
  return request.get(`/quiz/stages/${stageCode}`, null, callbacks)
}

export function submitQuizStageAnswer(stageCode, data, callbacks) {
  return request.post(`/quiz/stages/${stageCode}/answer`, data, callbacks)
}

/**
 * 获取用户答题统计（正确率等）
 * @param {object} callbacks - 回调函数 { onSuccess, onError }
 * @returns {Promise}
 */
export function getUserQuizStats(callbacks) {
  return request.get('/quiz/stats/user', null, callbacks)
}

/**
 * 获取我的错题列表（不含正确答案）
 */
export function getMyMistakes(params, callbacks) {
  return request.get('/quiz/mistakes', params, callbacks)
}

/**
 * 错题本重新作答（做对则移除错题）
 * @param {object} data - { mistakeId, questionId, userAnswer: 'A'|'B'|'C'|'D' }
 */
export function submitMistakeAnswer(data, callbacks) {
  return request.post('/quiz/mistakes/answer', data, callbacks)
}

/**
 * 获取答题记录列表
 * @param {object} params - 请求参数 { current: Number, size: Number }
 * @param {object} callbacks - 回调函数 { onSuccess, onError }
 * @returns {Promise}
 */
export function getQuizHistory(params, callbacks) {
  return request.get('/quiz/history', params, callbacks)
}

/**
 * 获取答题解析
 * @param {object} params - 请求参数 { questionId: String }
 * @param {object} callbacks - 回调函数 { onSuccess, onError }
 * @returns {Promise}
 */
export function getQuestionExplanation(params, callbacks) {
  return request.get(`/quiz/question/${params.questionId}/explanation`, null, callbacks)
}

/**
 * 问答社区相关API（针灸答疑、常见误区）
 */
import request from '@/utils/request'

// ========== 针灸答疑（帖子） ==========

export function getPostPage(params, callbacks) {
  return request.get('/community/post/page', params, callbacks)
}

export function getPostDetail(postId, callbacks) {
  return request.get(`/community/post/${postId}`, null, callbacks)
}

export function createPost(params, callbacks) {
  return request.post('/community/post/create', params, callbacks)
}

export function postLike(postId, callbacks) {
  return request.post(`/community/post/${postId}/like`, {}, callbacks)
}

export function postCollect(postId, callbacks) {
  return request.post(`/community/post/${postId}/collect`, {}, callbacks)
}

export function postReport(params, callbacks) {
  return request.post('/community/post/report', params, callbacks)
}

export function addComment(params, callbacks) {
  return request.post('/community/post/comment', params, callbacks)
}

/**
 * @param {number|object} limitOrParams - 数字表示 limit；对象可传 { limit, postType }
 * @param {object} [callbacks]
 */
export function getPostLatest(limitOrParams, callbacks) {
  const params =
    typeof limitOrParams === 'number'
      ? { limit: limitOrParams }
      : { limit: limitOrParams?.limit ?? 5, postType: limitOrParams?.postType }
  return request.get('/community/post/latest', params, callbacks)
}

export function getMyPostList(callbacks) {
  return request.get('/community/post/mine/posts', {}, callbacks)
}

export function getMyCollectedPostList(callbacks) {
  return request.get('/community/post/mine/collects', {}, callbacks)
}

export function getMyLikedPostList(callbacks) {
  return request.get('/community/post/mine/likes', {}, callbacks)
}

export function getMyCommentList(callbacks) {
  return request.get('/community/post/mine/comments', {}, callbacks)
}

// ========== 常见误区 ==========

export function getMisconceptionList(callbacks) {
  return request.get('/community/misconception/list', null, callbacks)
}

export function getMisconceptionById(id, callbacks) {
  return request.get(`/community/misconception/${id}`, null, callbacks)
}


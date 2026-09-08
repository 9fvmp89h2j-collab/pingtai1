import request from '@/utils/request'

export function pagePostComments(params, config = {}) {
  return request.get('/admin/post-comment/page', params, config)
}

export function getPostCommentDetail(id, config = {}) {
  return request.get(`/admin/post-comment/${id}`, null, config)
}

export function deletePostComment(id, config = {}) {
  return request.delete(`/admin/post-comment/${id}`, config)
}


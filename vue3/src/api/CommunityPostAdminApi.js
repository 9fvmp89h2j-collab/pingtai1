import request from '@/utils/request'

export function pageCommunityPosts(params, config = {}) {
  return request.get('/admin/community-post/page', params, config)
}

export function getCommunityPostDetail(id, config = {}) {
  return request.get(`/admin/community-post/${id}`, null, config)
}

export function createCommunityPost(data, config = {}) {
  return request.post('/admin/community-post', data, config)
}

export function deleteCommunityPost(id, config = {}) {
  return request.delete(`/admin/community-post/${id}`, config)
}


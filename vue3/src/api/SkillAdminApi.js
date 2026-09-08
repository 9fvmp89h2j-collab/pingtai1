import request from '@/utils/request'

export function pageSkills(params, config = {}) {
  return request.get('/admin/skills/page', params, config)
}

export function getSkillDetail(id, config = {}) {
  return request.get(`/admin/skills/${id}`, null, config)
}

export function createSkill(data, config = {}) {
  return request.post('/admin/skills', data, config)
}

export function updateSkill(id, data, config = {}) {
  return request.put(`/admin/skills/${id}`, data, config)
}

export function deleteSkill(id, config = {}) {
  return request.delete(`/admin/skills/${id}`, config)
}


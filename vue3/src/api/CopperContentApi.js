import request from '@/utils/request'

export const getCopperContentOverview = () =>
  request.get('/admin/copper-content/overview', null, { enableCache: false })

export const getCopperAcupointPage = (params) =>
  request.get('/admin/copper-content/acupoints', params, { enableCache: false })

export const getCopperAcupoint = (code) =>
  request.get(`/admin/copper-content/acupoints/${code}`, null, { enableCache: false })

export const saveCopperAcupointDraft = (code, data) =>
  request.put(`/admin/copper-content/acupoints/${code}/draft`, data, { showDefaultMsg: false })

export const getCopperStoryPage = (params) =>
  request.get('/admin/copper-content/stories', params, { enableCache: false })

export const getCopperStory = (code) =>
  request.get(`/admin/copper-content/stories/${code}`, null, { enableCache: false })

export const saveCopperStoryDraft = (code, data) =>
  request.put(`/admin/copper-content/stories/${code}/draft`, data, { showDefaultMsg: false })

export const transitionCopperContent = (type, key, action, data = null) =>
  request.post(`/admin/copper-content/${type}/${key}/${action}`, data, {
    showDefaultMsg: false,
    idempotencyKey: `copper-content:${type}:${key}:${action}:${Date.now()}`
  })

export const getCopperContentReleases = (params) =>
  request.get('/admin/copper-content/releases', params, { enableCache: false })

export const getCopperContentRevisions = (type, key) =>
  request.get(`/admin/copper-content/${type}/${key}/revisions`, null, { enableCache: false })

export const restoreCopperContentRevision = (type, key, version) =>
  request.post(`/admin/copper-content/${type}/${key}/revisions/${version}/restore`, null, { showDefaultMsg: false })

export const listPublishedCopperStories = () =>
  request.get('/acupuncture/copper-content/stories', null, { enableCache: false })

export const getPublishedCopperStory = (code) =>
  request.get(`/acupuncture/copper-content/stories/${code}`, null, { enableCache: false })

export const saveCopperStoryProgress = (code, data) =>
  request.put(`/acupuncture/copper-content/stories/${code}/progress`, data, { showDefaultMsg: false })

export const getCopperContentFeed = (limit = 20) =>
  request.get('/acupuncture/copper-content/feed', { limit }, { enableCache: false, showDefaultMsg: false })

export const markCopperContentRead = (releaseId) =>
  request.post(`/acupuncture/copper-content/feed/${releaseId}/read`, null, { showDefaultMsg: false })

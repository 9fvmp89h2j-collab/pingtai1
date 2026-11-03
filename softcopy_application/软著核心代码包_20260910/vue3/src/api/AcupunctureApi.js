import request from '@/utils/request'

/**
 * 针灸科普相关 API
 */
export function listOriginStories(params, callbacks) {
  return request.get('/acupuncture/origin-story/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function listDoctorStories(params, callbacks) {
  return request.get('/acupuncture/doctor-story/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function listXuewei(params, callbacks) {
  return request.get('/acupuncture/xuewei/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function getXueweiDetail(id, callbacks) {
  return request.get(`/acupuncture/xuewei/${id}`, null, {
    enableCache: false,
    ...callbacks
  })
}

export function listJingluo(params, callbacks) {
  return request.get('/acupuncture/jingluo/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function getJingluoDetail(id, callbacks) {
  return request.get(`/acupuncture/jingluo/${id}`, null, {
    enableCache: false,
    ...callbacks
  })
}

export function listZhenjiuTools(params, callbacks) {
  return request.get('/acupuncture/zhenjiu-tools/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function listIllness(params, callbacks) {
  return request.get('/acupuncture/illness/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function awardIllnessBadge(illnessId, callbacks) {
  return request.post(`/acupuncture/illness/award/${illnessId}`, null, {
    enableCache: false,
    ...callbacks
  })
}

export function listSkillNamesByIds(ids, callbacks) {
  return request.get('/skill/names', { ids }, {
    enableCache: false,
    ...callbacks
  })
}

export function listExtraCourse(params, callbacks) {
  return request.get('/acupuncture/extracourse/list', params, {
    enableCache: false,
    ...callbacks
  })
}

export function getExtraCourseDetail(id, callbacks) {
  return request.get(`/acupuncture/extracourse/${id}`, null, {
    enableCache: false,
    ...callbacks
  })
}

export function listCopperManAcupoints(callbacks) {
  return request.get('/acupuncture/copper-man/acupoints', null, {
    enableCache: false,
    showDefaultMsg: false,
    ...callbacks
  })
}

export function listBodyMapAcupoints(callbacks) {
  return request.get('/acupuncture/body-map/acupoints', null, {
    enableCache: false,
    showDefaultMsg: false,
    ...callbacks
  })
}

export function getCopperManDailyCase(callbacks) {
  return request.get('/acupuncture/copper-man/daily-case', null, {
    enableCache: false,
    showDefaultMsg: false,
    ...callbacks
  })
}

export function discoverCopperManAcupoint(code, callbacks) {
  return request.post('/acupuncture/copper-man/discover', { code }, {
    showDefaultMsg: false,
    ...callbacks
  })
}

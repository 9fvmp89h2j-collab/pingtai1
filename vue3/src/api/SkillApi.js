/**
 * 技能（skills）相关 API
 */
import request from '@/utils/request'

export function getSkillCount(callbacks = {}) {
  return request.get('/skill/count', null, callbacks)
}


/**
 * 技能背包 API
 */
import request from '@/utils/request'

export function getMyBackpack(callbacks) {
  return request.get('/user/backpack', {}, { enableCache: false, ...callbacks })
}

/**
 * 收集技能；成功后服务端可能已累加气血，需通知导航栏刷新等级并弹出升级提示（与 updateScore 一致）
 * @param {{ skillId: number }} params
 */
export async function addSkillToBackpack(params, callbacks) {
  const res = await request.post('/user/backpack/add', params, callbacks)
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('user-level-refresh'))
  }
  return res
}

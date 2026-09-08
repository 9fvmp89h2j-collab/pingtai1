import { mainlineLevels, mainlineLevelForPath } from '@/data/mainline'

export const MAIN_NAV_UNLOCK_ITEMS = [
  { key: 'home-map', path: '/home-map', public: true, label: '探险地图', icon: '图' },
  { key: 'doctor-story', path: '/doctor-story', levelId: 'bamboo', label: '故事馆', icon: '故' },
  { key: 'body-map', path: '/body-map', levelId: 'body', label: '身体地图', icon: '身' },
  { key: 'meridian', path: '/jingluo', levelId: 'meridian', label: '经络星河', icon: '经' },
  { key: 'copper-man', path: '/copper-man', levelId: 'archive', label: '小铜人馆', icon: '铜' },
  { key: 'safety', path: '/safety', levelId: 'safe-start', label: '安全课堂', icon: '安' },
  { key: 'review', path: '/review', levelId: 'secret-room', label: '星光修补册', icon: '星' },
  { key: 'agency', path: '/agency', levelId: 'agency', label: '侦探社', icon: '社' }
]

export function findNavUnlockByPath(path) {
  const level = mainlineLevelForPath(path)
  if (level) return { ...level, public: Boolean(level.public), levelId: level.id }
  const normalized = String(path || '').split('?')[0]
  return MAIN_NAV_UNLOCK_ITEMS.find((item) => normalized === item.path || normalized.startsWith(`${item.path}/`)) || null
}

export function pathForUnlockedAtLevel(level) {
  return mainlineLevels.find((entry) => entry.order === level)?.route || '/home-map'
}

export function isItemUnlocked(item, { isLoggedIn, gameState } = {}) {
  if (item?.public) return true
  if (!isLoggedIn || !gameState?.levels) return false
  const level = gameState.levels.find((entry) => entry.id === item.levelId)
  return Boolean(level?.unlocked)
}

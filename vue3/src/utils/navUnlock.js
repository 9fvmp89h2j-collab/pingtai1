/**
 * Main navigation entries for the child-facing adventure site.
 * The paths keep existing routes intact; items with query simply jump to a
 * section or panel on an existing page.
 */
export const MAIN_NAV_UNLOCK_ITEMS = [
  { key: 'landing', path: '/landing', requiredLevel: 1, public: true, label: '杏林谷', icon: '谷' },
  { key: 'map', path: '/landing?section=map', matchPath: '/landing', requiredLevel: 1, public: true, label: '探险地图', icon: '图' },
  { key: 'doctor-story', path: '/doctor-story', requiredLevel: 1, public: true, label: '故事馆', icon: '故' },
  { key: 'shuxue', path: '/shuxue', requiredLevel: 1, public: true, label: '小铜人馆', icon: '铜' },
  { key: 'clinic', path: '/myworld', matchPath: '/myworld', requiredLevel: 1, public: true, label: '小医馆', icon: '馆' },
  { key: 'safety', path: '/quiz-game', matchPath: '/quiz-game', requiredLevel: 1, public: true, label: '安全课堂', icon: '安' },
  { key: 'badge', path: '/myworld?panel=badges', matchPath: '/myworld', requiredLevel: 1, public: true, label: '我的徽章', icon: '章' }
]

export function findNavUnlockByPath(path) {
  const p = (path || '').split('?')[0]
  for (const item of MAIN_NAV_UNLOCK_ITEMS) {
    const target = (item.matchPath || item.path || '').split('?')[0]
    if (p === target || p.startsWith(`${target}/`)) {
      return item
    }
  }
  return null
}

export function pathForUnlockedAtLevel(level) {
  if (level >= 9) {
    return '/myworld'
  }
  const it = MAIN_NAV_UNLOCK_ITEMS.find((i) => i.requiredLevel === level)
  return it?.path ?? '/landing'
}

export function isItemUnlocked(item, { isLoggedIn, userLevel }) {
  if (item.public) return true
  if (!isLoggedIn) return false
  const lv = typeof userLevel === 'number' ? userLevel : 1
  return lv >= item.requiredLevel
}

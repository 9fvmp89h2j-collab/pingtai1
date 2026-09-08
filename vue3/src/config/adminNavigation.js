export const adminNavigation = [
  {
    key: 'dashboard',
    label: '首页',
    path: '/back/dashboard',
    icon: 'fas fa-house'
  },
  {
    key: 'user',
    label: '用户管理',
    path: '/back/user',
    icon: 'fas fa-user'
  },
  {
    key: 'content-library',
    label: '内容库',
    icon: 'fas fa-book',
    children: [
      { key: 'start-page', label: '开场故事', path: '/back/start-page', icon: 'fas fa-book-open' },
      { key: 'doctor-story', label: '名医资料', path: '/back/doctor-story', icon: 'fas fa-user-doctor' },
      { key: 'jingluo', label: '经络', path: '/back/jingluo', icon: 'fas fa-route' },
      { key: 'xuewei', label: '小铜人内容中心', path: '/back/xuewei', icon: 'fas fa-location-dot' },
      { key: 'train-game', label: '小火车关卡', path: '/back/train-game', icon: 'fas fa-train' },
      { key: 'mainline-level', label: '主线关卡', path: '/back/mainline-level', icon: 'fas fa-map-marked-alt' },
      { key: 'quiz-question', label: '题库', path: '/back/quiz-question', icon: 'fas fa-clipboard-list' }
    ]
  },
  {
    key: 'community-mgmt',
    label: '社区管理',
    icon: 'fas fa-comments',
    children: [
      { key: 'community-post', label: '帖子管理', path: '/back/community-post', icon: 'fas fa-file-lines' },
      { key: 'community-comment', label: '评论管理', path: '/back/community-comment', icon: 'fas fa-comment-dots' },
      { key: 'community-feedback', label: '反馈消息', path: '/back/community-feedback', icon: 'fas fa-inbox' }
    ]
  },
  {
    key: 'profile',
    label: '管理员信息',
    path: '/back/profile',
    icon: 'fas fa-user-shield'
  }
]

export function findAdminNavigation(path) {
  for (const item of adminNavigation) {
    if (item.path === path) return { item, parent: null }
    const child = item.children?.find((entry) => entry.path === path)
    if (child) return { item: child, parent: item }
  }
  return null
}

export function getAdminBreadcrumbs(path) {
  const current = findAdminNavigation(path)
  const home = adminNavigation[0]
  if (!current || current.item.path === home.path) return [home]
  return [home, ...(current.parent ? [current.parent] : []), current.item]
}

export function getAdminOpenKeys(path) {
  const current = findAdminNavigation(path)
  return current?.parent ? [current.parent.key] : []
}

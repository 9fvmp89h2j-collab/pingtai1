import { createRouter, createWebHashHistory } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'
import { findNavUnlockByPath } from '@/utils/navUnlock'

export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/backend/Dashboard.vue'), meta: { title: '首页', icon: 'HomeFilled' } },
      { path: 'user', name: 'UserManagement', component: () => import('@/views/backend/user/index.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'start-page', name: 'StartPageManagement', component: () => import('@/views/backend/start-page/index.vue'), meta: { title: '开场故事管理', icon: 'BookOutlined' } },
      { path: 'doctor-story', name: 'DoctorStoryManagement', component: () => import('@/views/backend/doctor-story/index.vue'), meta: { title: '名医故事管理', icon: 'BookOutlined' } },
      { path: 'jingluo', name: 'JingluoManagement', component: () => import('@/views/backend/jingluo/index.vue'), meta: { title: '经络管理', icon: 'BookOutlined' } },
      { path: 'mainline-level', name: 'MainlineLevelManagement', component: () => import('@/views/backend/mainline-level/index.vue'), meta: { title: '主线关卡', icon: 'BookOutlined' } },
      { path: 'train-game', name: 'TrainGameManagement', component: () => import('@/views/backend/train-game/index.vue'), meta: { title: '小火车关卡管理', icon: 'BookOutlined' } },
      { path: 'xuewei', name: 'XueweiManagement', component: () => import('@/views/backend/xuewei/index.vue'), meta: { title: '小铜人内容中心', icon: 'BookOutlined' } },
      { path: 'quiz-question', name: 'QuizQuestionManagement', component: () => import('@/views/backend/quiz-question/index.vue'), meta: { title: '题库管理', icon: 'BookOutlined' } },
      { path: 'community-post', name: 'CommunityPostManagement', component: () => import('@/views/backend/community-post/index.vue'), meta: { title: '帖子管理', icon: 'BookOutlined' } },
      { path: 'community-comment', name: 'CommunityCommentManagement', component: () => import('@/views/backend/community-comment/index.vue'), meta: { title: '评论管理', icon: 'BookOutlined' } },
      { path: 'community-feedback', name: 'CommunityFeedbackManagement', component: () => import('@/views/backend/community-feedback/index.vue'), meta: { title: '反馈消息', icon: 'BookOutlined' } },
      { path: 'profile', name: 'BackendProfile', component: () => import('@/views/profile/index.vue'), meta: { title: '管理员信息', icon: 'UserFilled' } }
    ]
  }
]

const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    redirect: '/home-map',
    children: [
      { path: 'landing', name: 'LegacyLandingRedirect', redirect: '/home-map', meta: { title: '杏林小药师' } },
      { path: 'home-map', name: 'HomeMapLanding', component: () => import('@/views/frontend/HomeMapLanding.vue'), meta: { title: '杏林探险地图' } },
      { path: 'agency', name: 'AgencyHome', component: () => import('@/views/frontend/Agency.vue'), meta: { title: '小铜人侦探社' } },
      { path: '/index.html', name: 'IndexLanding', redirect: '/home-map', meta: { title: '杏林小药师' } },
      { path: 'doctor-story', name: 'DoctorStory', component: () => import('@/views/frontend/DoctorStory.vue'), meta: { title: '故事馆' } },
      { path: 'body-map', name: 'BodyMap', component: () => import('@/views/frontend/BodyMap.vue'), meta: { title: '身体地图' } },
      { path: 'jingluo', name: 'Jingluo', component: () => import('@/views/frontend/MeridianRiver.vue'), meta: { title: '经络星河' } },
      { path: 'meridian-match', name: 'MeridianMatch', component: () => import('@/views/frontend/MeridianMatchGame.vue'), meta: { title: '经络消消看' } },
      { path: 'acupoint-sort', name: 'AcupointSort', component: () => import('@/views/frontend/AcupointSortGame.vue'), meta: { title: '穴位排序大师' } },
      { path: 'shunting-game', name: 'ShuntingGame', component: () => import('@/views/frontend/ShuntingGame.vue'), meta: { title: '经络小火车' } },
      { path: 'copper-man', name: 'CopperMan', component: () => import('@/views/frontend/CopperMan.vue'), meta: { title: '小铜人馆', requiresAuth: true } },
      { path: 'shuxue', redirect: '/body-map', meta: { title: '身体地图' } },
      { path: 'fangfa', redirect: '/safety', meta: { title: '安全课堂' } },
      { path: 'fangfa/extra/:id', redirect: '/safety', meta: { title: '安全课堂' } },
      { path: 'lianyan', redirect: '/safety', meta: { title: '安全课堂' } },
      { path: 'ultimate-challenge', redirect: '/safety', meta: { title: '安全课堂' } },
      { path: 'safety', name: 'Safety', component: () => import('@/views/frontend/SafetyGame.vue'), meta: { title: '安全课堂' } },
      { path: '/quiz-game', name: 'QuizGame', component: () => import('@/views/frontend/quiz-game/index.vue'), meta: { title: '安全问答', requiresAuth: true } },
      { path: 'review', name: 'Review', component: () => import('@/views/frontend/Review.vue'), meta: { title: '星光修补册' } },
      { path: 'clinic', redirect: '/agency', meta: { title: '侦探社' } },
      { path: 'bag', name: 'Bag', component: () => import('@/views/frontend/Bag.vue'), meta: { title: '材料背包', requiresAuth: true } },
      { path: 'rewards', name: 'Rewards', component: () => import('@/views/frontend/Rewards.vue'), meta: { title: '星光奖励站', requiresAuth: true } },
      { path: 'badges', name: 'Badges', component: () => import('@/views/frontend/Badges.vue'), meta: { title: '故事收藏墙' } },
      { path: '/community', name: 'Community', component: () => import('@/views/frontend/community/index.vue'), meta: { title: '悄悄话信箱' } },
      { path: '/community/qa', name: 'CommunityQa', component: () => import('@/views/frontend/community/qa.vue'), meta: { title: '社区问答' } },
      { path: '/community/misconceptions', redirect: '/community' },
      { path: '/community/guide', redirect: '/community' },
      { path: '/community/guide/:id', redirect: '/community' },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人设置', requiresAuth: true } },
      { path: 'myworld', name: 'MyWorld', component: () => import('@/views/frontend/MyWorld.vue'), meta: { title: '我的信息', requiresAuth: true } },
      { path: 'my-posts', name: 'MyPosts', component: () => import('@/views/frontend/MyPosts.vue'), meta: { title: '我的发帖', requiresAuth: true } },
      { path: 'my-mistakes', name: 'MyMistakes', component: () => import('@/views/frontend/MyMistakes.vue'), meta: { title: '我的错题', requiresAuth: true } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/frontend/Settings.vue'), meta: { title: '其他设置', requiresAuth: true } }
    ]
  },
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      { path: 'login', name: 'Login', component: () => import('@/views/auth/Login.vue'), meta: { title: '登录' } },
      { path: 'register', name: 'Register', component: () => import('@/views/auth/Register.vue'), meta: { title: '注册' } },
      { path: 'forgot-password', name: 'ForgotPassword', component: () => import('@/views/auth/ForgotPassword.vue'), meta: { title: '找回密码' } }
    ]
  },
  { path: '/login', redirect: '/auth/login' },
  { path: '/checkin', redirect: '/auth/register' },
  { path: '/register', redirect: '/auth/register' }
]

const errorRoutes = [
  { path: '/404', name: '404', component: () => import('@/views/error/404.vue'), meta: { title: '404' } },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHashHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0, left: 0 }
  },
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 杏林小药师`
  }

  const userStore = useUserStore()

  if (to.path.startsWith('/back')) {
    if (!userStore.isLoggedIn) {
      next({ path: '/auth/login', query: { redirect: to.fullPath } })
      return
    }
    if (!userStore.isAdmin) {
      next('/home-map')
      return
    }
    next()
    return
  }

  if (to.matched.some((record) => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({ path: '/auth/login', query: { redirect: to.fullPath } })
    return
  }

  const navRule = findNavUnlockByPath(to.path)
  if (navRule && !userStore.isAdmin && !navRule.public) {
    if (!userStore.isLoggedIn) {
      message.warning('请先登录后再访问该功能')
      next({ path: '/auth/login', query: { redirect: to.fullPath } })
      return
    }
    let gameState
    try {
      gameState = await userStore.loadGameState({ showDefaultMsg: false })
    } catch (error) {
      message.error('地图状态暂时无法确认，请返回地图重试')
      next('/home-map')
      return
    }
    const currentLevel = gameState?.levels?.find((level) => level.id === navRule.levelId)
    if (!currentLevel?.unlocked) {
      const active = gameState?.levels?.find((level) => level.current)
      message.warning(`请先完成“${active?.label || '前置关卡'}”`)
      next('/home-map')
      return
    }
  }

  if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/auth/login')) {
    next(userStore.isAdmin ? '/back/dashboard' : '/home-map')
    return
  }

  next()
})

export default router

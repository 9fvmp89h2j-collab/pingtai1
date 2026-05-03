import { createRouter, createWebHashHistory } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'
import { findNavUnlockByPath } from '@/utils/navUnlock'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'start-page',
        name: 'StartPageManagement',
        component: () => import('@/views/backend/start-page/index.vue'),
        meta: { title: '开始页管理', icon: 'BookOutlined' }
      },
      {
        path: 'doctor-story',
        name: 'DoctorStoryManagement',
        component: () => import('@/views/backend/doctor-story/index.vue'),
        meta: { title: '针灸名医管理', icon: 'BookOutlined' }
      },
      {
        path: 'jingluo',
        name: 'JingluoManagement',
        component: () => import('@/views/backend/jingluo/index.vue'),
        meta: { title: '经络管理', icon: 'BookOutlined' }
      },
      {
        path: 'train-game',
        name: 'TrainGameManagement',
        component: () => import('@/views/backend/train-game/index.vue'),
        meta: { title: '小火车关卡管理', icon: 'BookOutlined' }
      },
      {
        path: 'xuewei',
        name: 'XueweiManagement',
        component: () => import('@/views/backend/xuewei/index.vue'),
        meta: { title: '腧穴管理', icon: 'BookOutlined' }
      },
      {
        path: 'zhenfa',
        name: 'ZhenfaManagement',
        component: () => import('@/views/backend/zhenfa/index.vue'),
        meta: { title: '针法管理', icon: 'BookOutlined' }
      },
      {
        path: 'extracourse',
        name: 'ExtraCourseManagement',
        component: () => import('@/views/backend/extracourse/index.vue'),
        meta: { title: '拓展疗法管理', icon: 'BookOutlined' }
      },
      {
        path: 'illness',
        name: 'IllnessManagement',
        component: () => import('@/views/backend/illness/index.vue'),
        meta: { title: '疾病管理', icon: 'BookOutlined' }
      },
      {
        path: 'skills',
        name: 'SkillsManagement',
        component: () => import('@/views/backend/skills/index.vue'),
        meta: { title: '技能点管理', icon: 'BookOutlined' }
      },
      {
        path: 'quiz-question',
        name: 'QuizQuestionManagement',
        component: () => import('@/views/backend/quiz-question/index.vue'),
        meta: { title: '题库管理', icon: 'BookOutlined' }
      },
      {
        path: 'community-post',
        name: 'CommunityPostManagement',
        component: () => import('@/views/backend/community-post/index.vue'),
        meta: { title: '帖子管理', icon: 'BookOutlined' }
      },
      {
        path: 'community-comment',
        name: 'CommunityCommentManagement',
        component: () => import('@/views/backend/community-comment/index.vue'),
        meta: { title: '评论管理', icon: 'BookOutlined' }
      },
      {
        path: 'community-feedback',
        name: 'CommunityFeedbackManagement',
        component: () => import('@/views/backend/community-feedback/index.vue'),
        meta: { title: '反馈消息', icon: 'BookOutlined' }
      },


      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '管理员信息', icon: 'UserFilled' }
      }
    ]
  }
]

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    redirect: '/landing',
    children: [
      {
        path: 'landing',
        name: 'Landing',
        component: () => import('@/views/frontend/Landing.vue'),
        meta: { title: '小银针大魔法' }
      },
      {
        path: '/index.html',
        name: 'IndexLanding',
        component: () => import('@/views/frontend/Landing.vue'),
        meta: { title: '小银针大魔法' }
      },
      {
        path: 'doctor-story',
        name: 'DoctorStory',
        component: () => import('@/views/frontend/DoctorStory.vue'),
        meta: { title: '名医故事' }
      },
      {
        path: 'jingluo',
        name: 'Jingluo',
        component: () => import('@/views/frontend/Jingluo.vue'),
        meta: { title: '经络' }
      },
      {
        path: 'shunting-game',
        name: 'ShuntingGame',
        component: () => import('@/views/frontend/ShuntingGame.vue'),
        meta: { title: '经络小火车' }
      },
      {
        path: 'shuxue',
        name: 'Shuxue',
        component: () => import('@/views/frontend/Shuxue.vue'),
        meta: { title: '腧穴' }
      },
      {
        path: 'fangfa',
        name: 'Fangfa',
        component: () => import('@/views/frontend/Method.vue'),
        meta: { title: '法器' }
      },
      {
        path: 'fangfa/extra/:id',
        name: 'ExtraCourseDetail',
        component: () => import('@/views/frontend/ExtraCourseDetail.vue'),
        meta: { title: '拓展疗法详情' }
      },
      {
        path: 'lianyan',
        name: 'Lianyan',
        component: () => import('@/views/frontend/Lianyan.vue'),
        meta: { title: '历练' }
      },
      {
        path: 'ultimate-challenge',
        name: 'UltimateChallenge',
        component: () => import('@/views/frontend/UltimateChallenge.vue'),
        meta: { title: '终极考验', requiresAuth: true }
      },
      {
        path: '/quiz-game',
        name: 'QuizGame',
        component: () => import('@/views/frontend/quiz-game/index.vue'),
        meta: { title: '答题闯关', requiresAuth: true }
      },
      { path: '/community', name: 'Community', component: () => import('@/views/frontend/community/index.vue'), meta: { title: '悄悄话信箱' } },
      { path: '/community/qa', name: 'CommunityQa', component: () => import('@/views/frontend/community/qa.vue'), meta: { title: '社区' } },
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
  // 认证相关路由使用专门的认证布局
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/auth/Login.vue'),
        meta: { title: '登录' }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/auth/Register.vue'),
        meta: { title: '注册' }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/auth/ForgotPassword.vue'),
        meta: { title: '找回密码' }
      }
    ]
  },
  // 兼容旧路由
  {
    path: '/login',
    redirect: '/auth/login'
  },
  {
    path: '/register',
    redirect: '/auth/register'
  }
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 路由配置
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 小银针大魔法`
  }

  const userStore = useUserStore()

  // 1) 后台路由：必须管理员
  if (to.path.startsWith('/back')) {
    if (!userStore.isLoggedIn) {
      next({ path: '/auth/login', query: { redirect: to.fullPath } })
      return
    }
    if (!userStore.isAdmin) {
      next('/landing')
      return
    }
    next()
    return
  }

  // 2) 需要登录的前台路由
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({ path: '/auth/login', query: { redirect: to.fullPath } })
    return
  }

  // 3) 气血共九级；主导航八项与等级解锁（开始页 public；管理员不限）
  const navRule = findNavUnlockByPath(to.path)
  if (navRule && !userStore.isAdmin) {
    if (!navRule.public) {
      if (!userStore.isLoggedIn) {
        message.warning('请先登录后再访问该功能')
        next({ path: '/auth/login', query: { redirect: to.fullPath } })
        return
      }
      await userStore.ensureLevelInfo({ showDefaultMsg: false })
      const lv = userStore.cachedLevelInfo?.level ?? 1
      if (lv < navRule.requiredLevel) {
        message.warning(`该功能需达到等级 ${navRule.requiredLevel} 解锁，当前为等级 ${lv}，可以通过学习或者互动升级哦！`)
        next(false)
        return
      }
    }
  }

  // 4) 已登录访问登录页：按角色跳转
  if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/auth/login')) {
    next(userStore.isAdmin ? '/back/dashboard' : '/landing')
    return
  }

  next()
})

export default router

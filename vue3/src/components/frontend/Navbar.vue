<template>
  <a-layout-header class="frontend-navbar">
    <div class="navbar-container">
      <div class="navbar-logo">
        <router-link to="/landing">
          <img
            :src="logoSrc"
            alt="杏林小药师"
            class="logo-icon"
          >
          <span class="logo-text">{{ siteConfig.logo.text }}</span>
        </router-link>
      </div>

      <a-menu
        v-model:selected-keys="selectedKeys"
        mode="horizontal"
        class="navbar-menu"
        :style="{ lineHeight: '64px', borderBottom: 'none' }"
      >
        <a-menu-item
          v-for="item in MAIN_NAV_UNLOCK_ITEMS"
          :key="item.key"
          class="navbar-nav-slot"
        >
          <div
            class="nav-item-inner"
            :class="{ 'is-locked': !navUnlocked(item) }"
          >
            <router-link
              v-if="navUnlocked(item)"
              :to="item.path"
              class="nav-link"
            >
              <span class="menu-icon">{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </router-link>
            <div
              v-else
              class="nav-link nav-link--blocked"
              role="button"
              tabindex="0"
              @click.stop="onNavLocked(item)"
              @keydown.enter.prevent="onNavLocked(item)"
            >
              <span class="menu-icon">{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </div>
            <div
              v-if="!navUnlocked(item)"
              class="nav-lock-mask"
              @click.stop="onNavLocked(item)"
            />
          </div>
        </a-menu-item>
      </a-menu>

      <div class="navbar-user">
        <template v-if="isLoggedIn">
          <a-dropdown>
            <a
              class="user-info"
              @click.prevent
            >
              <a-avatar
                :size="32"
                :src="userStore.avatar"
              >
                {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="user-name">{{ userStore.userInfo?.username }}</span>
              <DownOutlined />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="my-info">
                  <router-link to="/myworld">
                    <UserOutlined />
                    <span>我的信息</span>
                  </router-link>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item
                  key="logout"
                  @click="handleLogout"
                >
                  <LogoutOutlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>

        <template v-else>
          <a-space :size="10">
            <a-button
              type="default"
              @click="router.push('/auth/login')"
            >
              登录
            </a-button>
            <a-button
              type="primary"
              @click="router.push('/auth/register')"
            >
              注册
            </a-button>
          </a-space>
        </template>
      </div>
    </div>

    <a-modal
      v-model:open="levelUpModalOpen"
      title="等级提升"
      :footer="null"
      centered
      @cancel="levelUpModalOpen = false"
    >
      <p class="level-up-text">
        {{ levelUpModalBody }}
      </p>
      <div class="level-up-actions">
        <a-button
          v-if="levelUpModalLevel === 9"
          type="primary"
          size="large"
          @click="onLevelUpViewCertificate"
        >
          查看证书
        </a-button>
        <a-button
          v-else
          type="primary"
          size="large"
          @click="goLevelUpExplore"
        >
          去看看
        </a-button>
      </div>
    </a-modal>
  </a-layout-header>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import siteConfig from '@/config/site'
import logoIcon from '@/assets/home_cat.png'
import {
  DownOutlined,
  LogoutOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import {
  MAIN_NAV_UNLOCK_ITEMS,
  isItemUnlocked,
  pathForUnlockedAtLevel
} from '@/utils/navUnlock'
import { openUserCertificateFlow } from '@/utils/userCertificate'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = ref(['landing'])
const levelUpModalOpen = ref(false)
const levelUpModalBody = ref('')
const levelUpModalLevel = ref(1)
const levelUpTargetPath = ref('/landing')

const logoSrc = computed(() => logoIcon)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userLevel = computed(() => {
  if (!userStore.isLoggedIn) return 0
  const n = userStore.cachedLevelInfo?.level
  return typeof n === 'number' ? n : 1
})

function navUnlocked(item) {
  if (userStore.isAdmin) return true
  return isItemUnlocked(item, {
    isLoggedIn: userStore.isLoggedIn,
    userLevel: userLevel.value
  })
}

function onNavLocked(item) {
  if (item.public) return
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再访问该功能')
    router.push({ path: '/auth/login', query: { redirect: item.path } })
    return
  }
  message.warning(`「${item.label}」需要达到等级 ${item.requiredLevel} 解锁，当前等级 ${userLevel.value}`)
}

function lastSeenLevelStorageKey() {
  const id = userStore.userId
  return id != null ? `nav-last-seen-level-${id}` : null
}

async function handlePossibleLevelUp() {
  if (!userStore.isLoggedIn || userStore.isAdmin) {
    return
  }
  await userStore.ensureLevelInfo({ showDefaultMsg: false })
  const info = userStore.cachedLevelInfo
  const cur = typeof info?.level === 'number' ? info.level : 1
  const key = lastSeenLevelStorageKey()
  if (!key) return

  const prevRaw = localStorage.getItem(key)
  if (prevRaw === null || prevRaw === '') {
    localStorage.setItem(key, String(cur))
    return
  }

  const prev = parseInt(prevRaw, 10)
  if (Number.isNaN(prev)) {
    localStorage.setItem(key, String(cur))
    return
  }

  if (cur > prev) {
    const name = info?.levelName || `等级${cur}`
    levelUpModalLevel.value = cur
    if (cur === 9) {
      levelUpModalBody.value = `恭喜你，已经达到最高等级 Level 9（${name}）！你已获得专属荣誉证书，可以点击下方按钮查看。`
    } else {
      levelUpModalBody.value = `恭喜你，已经升级到 Level ${cur}（${name}），新的学习入口已经解锁。`
    }
    levelUpTargetPath.value = pathForUnlockedAtLevel(cur)
    levelUpModalOpen.value = true
    localStorage.setItem(key, String(cur))
  }
}

function goLevelUpExplore() {
  levelUpModalOpen.value = false
  const p = levelUpTargetPath.value || '/landing'
  router.push(p).catch(() => {})
}

function onLevelUpViewCertificate() {
  levelUpModalOpen.value = false
  openUserCertificateFlow(userStore).catch((e) => console.error(e))
}

function onLevelRefreshEvent() {
  handlePossibleLevelUp()
}

watch(() => route.fullPath, () => {
  const newPath = route.path
  if (newPath === '/landing' || newPath.startsWith('/landing/') || newPath === '/index.html') {
    selectedKeys.value = route.query.section === 'map' ? ['map'] : ['landing']
  } else if (newPath.startsWith('/doctor-story')) {
    selectedKeys.value = ['doctor-story']
  } else if (newPath === '/shuxue' || newPath.startsWith('/shuxue/')) {
    selectedKeys.value = ['shuxue']
  } else if (newPath === '/quiz-game' || newPath.startsWith('/quiz-game')) {
    selectedKeys.value = ['safety']
  } else if (newPath === '/myworld' || newPath.startsWith('/myworld')) {
    selectedKeys.value = route.query.panel === 'badges' ? ['badge'] : ['clinic']
  } else {
    selectedKeys.value = []
  }
}, { immediate: true })

watch(isLoggedIn, async (logged) => {
  if (logged) {
    await userStore.ensureLevelInfo({ showDefaultMsg: false })
    handlePossibleLevelUp()
  }
})

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await userStore.ensureLevelInfo({ showDefaultMsg: false })
    handlePossibleLevelUp()
  }
  window.addEventListener('user-level-refresh', onLevelRefreshEvent)
})

onUnmounted(() => {
  window.removeEventListener('user-level-refresh', onLevelRefreshEvent)
})

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await userStore.logout()
      router.push('/auth/login')
    }
  })
}
</script>

<style scoped>
.frontend-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  padding: 0;
  line-height: 64px;
  background: rgba(255, 248, 232, 0.82);
  border-bottom: 1px solid rgba(47, 125, 104, 0.12);
  box-shadow: 0 8px 24px rgba(47, 77, 63, 0.08);
  backdrop-filter: blur(12px);
}

.navbar-container {
  display: flex;
  width: min(1200px, 100%);
  height: 100%;
  align-items: center;
  justify-content: space-between;
  margin: 0 auto;
  padding: 0 24px;
}

.navbar-logo {
  flex-shrink: 0;
}

.navbar-logo a {
  display: flex;
  align-items: center;
  color: inherit;
  text-decoration: none;
}

.logo-icon {
  width: 34px;
  height: 34px;
  margin-right: 8px;
  object-fit: contain;
}

.logo-text {
  color: #2f7d68;
  font-size: 20px;
  font-weight: 900;
}

.navbar-menu {
  flex: 1;
  margin: 0 12px;
  background: transparent;
  border: none;
  font-size: 14px;
}

.menu-icon {
  display: inline-flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  margin-right: 4px;
  color: #fff8e8;
  font-size: 12px;
  font-weight: 900;
  background: #b8863b;
  border-radius: 50%;
}

.navbar-nav-slot :deep(.ant-menu-title-content) {
  width: 100%;
}

.nav-item-inner {
  position: relative;
  display: flex;
  min-height: 40px;
  align-items: center;
}

.nav-link {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 4px;
  color: #2d4e43;
  font-weight: 800;
  text-decoration: none;
}

.nav-link--blocked {
  cursor: not-allowed;
  opacity: 0.85;
}

.nav-lock-mask {
  position: absolute;
  z-index: 2;
  inset: 0;
  margin: -2px -4px;
  background: rgba(160, 160, 160, 0.5);
  border-radius: 20px;
  cursor: not-allowed;
}

.nav-item-inner.is-locked :deep(.nav-link) {
  pointer-events: none;
}

:deep(.ant-menu-item) {
  height: 40px !important;
  padding: 0 9px !important;
  margin: 0 1px !important;
  line-height: 40px !important;
  border-radius: 999px !important;
}

:deep(.ant-menu-item:hover) {
  color: #2f7d68;
  background: rgba(47, 125, 104, 0.08);
}

:deep(.ant-menu-item-selected) {
  color: #fff;
  background: #2f7d68;
}

:deep(.ant-menu-item-selected a),
:deep(.ant-menu-item-selected .nav-link) {
  color: #fff;
}

:deep(.ant-menu-item-selected .menu-icon) {
  color: #6a4910;
  background: #ffd35a;
}

.navbar-user {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  color: #2d4e43;
  border-radius: 999px;
  cursor: pointer;
  text-decoration: none;
  transition: background-color 0.2s;
}

.user-info:hover {
  background: rgba(47, 125, 104, 0.08);
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.navbar-user :deep(.ant-btn) {
  border-radius: 999px;
  font-weight: 800;
}

.navbar-user :deep(.ant-btn-primary) {
  background: #2f7d68;
  border-color: #2f7d68;
}

.level-up-text {
  margin-bottom: 20px;
  font-size: 16px;
  line-height: 1.65;
}

.level-up-actions {
  text-align: center;
}

@media (max-width: 900px) {
  .navbar-container {
    padding: 0 16px;
  }

  .navbar-menu {
    display: none;
  }

  .logo-text {
    font-size: 17px;
  }

  .user-name {
    display: none;
  }
}
</style>

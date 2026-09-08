<template>
  <header class="xinglin-navbar">
    <div class="nav-inner">
      <router-link to="/home-map" class="brand" aria-label="小铜人探案社首页">
        <img class="brand__img" :src="logoSrc" alt="小铜人" />
        <span class="brand__copy">
          <span class="brand__name">小铜人探案社</span>
          <small>探索 · 发现 · 守护</small>
        </span>
      </router-link>

      <nav ref="navMain" class="nav-main" aria-label="主导航">
        <router-link
          v-for="item in MAIN_NAV_UNLOCK_ITEMS"
          :key="item.key"
          :to="item.path"
          class="nav-item"
          :class="{ on: isActive(item) }"
          :aria-current="isActive(item) ? 'page' : undefined"
        >
          <span class="nav-item__icon" aria-hidden="true">
            <img v-if="navImage(item.key)" :src="navImage(item.key)" alt="" />
            <ExperimentOutlined v-else />
          </span>
          <span class="nav-item__label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="nav-actions">
        <template v-if="isLoggedIn">
          <button
            v-if="!userStore.isAdmin"
            class="notice-btn"
            type="button"
            aria-label="新内容消息"
            :aria-expanded="noticeOpen"
            @click.stop="toggleNotice"
          >
            <span aria-hidden="true">铃</span>
            <b v-if="unreadCount">{{ unreadCount > 99 ? '99+' : unreadCount }}</b>
          </button>
          <button class="user-btn" type="button" @click.stop="toggleUserMenu">
            <span class="user-btn__avatar">{{ userStore.userInfo?.username?.charAt(0) || '铜' }}</span>
            <span class="user-btn__name">{{ userStore.userInfo?.username }}</span>
            <span class="user-btn__arrow" :class="{ up: userMenuOpen }">▾</span>
          </button>
          <div v-if="userMenuOpen" class="user-menu">
            <router-link to="/myworld" class="user-menu__item" @click="userMenuOpen = false">我的信息</router-link>
            <button class="user-menu__item" type="button" @click="handleLogout">退出登录</button>
          </div>
          <section v-if="noticeOpen" class="notice-panel" aria-label="小铜人新消息">
            <header>
              <div><strong>探案新消息</strong><small>{{ unreadCount ? `${unreadCount} 条还没看` : '都看过啦' }}</small></div>
              <span>站内提醒</span>
            </header>
            <div v-if="noticeLoading" class="notice-state">正在寻找新线索……</div>
            <div v-else-if="!noticeRecords.length" class="notice-state">暂时没有新故事或新星点</div>
            <button
              v-for="record in noticeRecords"
              v-else
              :key="record.id"
              class="notice-card"
              :class="{ unread: !isNoticeRead(record) }"
              type="button"
              @click="openNotice(record)"
            >
              <span class="notice-card__icon">{{ noticeType(record) === 'STORY' ? '案' : '星' }}</span>
              <span><strong>{{ noticeValue(record, 'title') }}</strong><small>{{ noticeValue(record, 'summary') }}</small></span>
              <i v-if="!isNoticeRead(record)">新</i>
            </button>
          </section>
        </template>
        <template v-else>
          <button class="act-btn act-btn--ghost" type="button" @click="router.push('/auth/login')">登录</button>
          <button class="act-btn act-btn--fill" type="button" @click="router.push('/auth/register')">注册</button>
        </template>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="levelUpModalOpen" class="modal-mask" @click.self="levelUpModalOpen = false">
        <div class="modal-box">
          <span class="modal-box__icon">章</span>
          <h2>侦探等级提升</h2>
          <p>{{ levelUpModalBody }}</p>
          <button
            v-if="levelUpModalLevel === 9"
            class="act-btn act-btn--fill"
            type="button"
            @click="onLevelUpViewCertificate"
          >
            查看证书
          </button>
          <button v-else class="act-btn act-btn--fill" type="button" @click="goLevelUpExplore">
            去看看
          </button>
        </div>
      </div>
    </Teleport>
  </header>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Modal } from 'ant-design-vue'
import { ExperimentOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'
import logoIcon from '@/assets/characters/copper-detective-guide-512.png'
import mapNavIcon from '@/assets/导航栏/optimized/探险地图图标.png'
import bodyMapNavIcon from '@/assets/素材/19. 身体地图拼图.png'
import storyNavIcon from '@/assets/导航栏/optimized/故事馆图标.png'
import meridianNavIcon from '@/assets/导航栏/optimized/经络星河 图标.png'
import copperNavIcon from '@/assets/导航栏/optimized/小铜人馆.png'
import safetyNavIcon from '@/assets/导航栏/optimized/安全课堂.png'
import reviewNavIcon from '@/assets/导航栏/optimized/星光修补册.png'
import agencyNavIcon from '@/assets/导航栏/optimized/侦探社.png'
import { MAIN_NAV_UNLOCK_ITEMS, pathForUnlockedAtLevel } from '@/utils/navUnlock'
import { openUserCertificateFlow } from '@/utils/userCertificate'
import { getCopperContentFeed, markCopperContentRead } from '@/api/CopperContentApi'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const userMenuOpen = ref(false)
const noticeOpen = ref(false)
const noticeLoading = ref(false)
const noticeRecords = ref([])
const unreadCount = ref(0)
const navMain = ref(null)
let noticeTimer = null

const levelUpModalOpen = ref(false)
const levelUpModalBody = ref('')
const levelUpModalLevel = ref(1)
const levelUpTargetPath = ref('/home-map')

const logoSrc = computed(() => logoIcon)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const navImages = {
  'home-map': mapNavIcon,
  'body-map': bodyMapNavIcon,
  'doctor-story': storyNavIcon,
  meridian: meridianNavIcon,
  'copper-man': copperNavIcon,
  safety: safetyNavIcon,
  review: reviewNavIcon,
  agency: agencyNavIcon
}

function navImage(key) {
  return navImages[key] || ''
}

function isActive(item) {
  const currentPath = route.path
  const relatedPaths = {
    meridian: ['/jingluo', '/meridian-match', '/acupoint-sort', '/shunting-game'],
    safety: ['/safety', '/quiz-game'],
    review: ['/review', '/my-mistakes']
  }
  if (relatedPaths[item.key]?.some((path) => currentPath === path || currentPath.startsWith(`${path}/`))) {
    return true
  }
  const targetPath = (item.matchPath || item.path).split('?')[0]
  return currentPath === targetPath || currentPath.startsWith(`${targetPath}/`)
}

async function scrollActiveNav() {
  await nextTick()
  const container = navMain.value
  const activeItem = container?.querySelector('.nav-item.on')
  if (!container || !activeItem) return
  const centeredLeft = activeItem.offsetLeft - (container.clientWidth - activeItem.offsetWidth) / 2
  container.scrollTo({ left: Math.max(0, centeredLeft), behavior: 'smooth' })
}

function toggleUserMenu() {
  noticeOpen.value = false
  userMenuOpen.value = !userMenuOpen.value
}

function noticeValue(record, key) {
  return record?.[key] ?? record?.[key.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`)] ?? ''
}

function noticeType(record) {
  return String(noticeValue(record, 'contentType')).toUpperCase()
}

function isNoticeRead(record) {
  const value = noticeValue(record, 'isRead')
  return value === true || value === 1 || value === '1'
}

async function loadNotices() {
  if (!isLoggedIn.value || userStore.isAdmin) return
  noticeLoading.value = true
  try {
    const data = await getCopperContentFeed(20)
    noticeRecords.value = Array.isArray(data?.records) ? data.records : []
    unreadCount.value = Number(data?.unreadCount || 0)
  } catch {
    noticeRecords.value = []
    unreadCount.value = 0
  } finally {
    noticeLoading.value = false
  }
}

async function toggleNotice() {
  userMenuOpen.value = false
  noticeOpen.value = !noticeOpen.value
  if (noticeOpen.value) await loadNotices()
}

async function openNotice(record) {
  const releaseId = Number(noticeValue(record, 'id'))
  if (!isNoticeRead(record) && releaseId) {
    await markCopperContentRead(releaseId).catch(() => {})
  }
  noticeOpen.value = false
  await loadNotices()
  const target = noticeValue(record, 'route') || '/home-map'
  router.push(target).catch(() => {})
}

function closeMenu(event) {
  if (!event.target.closest('.nav-actions')) {
    userMenuOpen.value = false
    noticeOpen.value = false
  }
}

function lsKey() {
  const id = userStore.userId
  return id != null ? `nav-lvl-${id}` : null
}

async function checkLevelUp() {
  if (!isLoggedIn.value || userStore.isAdmin) return
  await userStore.ensureLevelInfo({ showDefaultMsg: false })
  const currentLevel = userStore.cachedLevelInfo?.level || 1
  const key = lsKey()
  if (!key) return
  const previousLevel = parseInt(localStorage.getItem(key) || '0', 10)
  if (!previousLevel || currentLevel <= previousLevel) {
    localStorage.setItem(key, String(currentLevel))
    return
  }
  const name = userStore.cachedLevelInfo?.levelName || `等级${currentLevel}`
  levelUpModalLevel.value = currentLevel
  levelUpModalBody.value = currentLevel === 9
    ? `恭喜你，已达到最高等级 Level 9（${name}），可以查看专属证书。`
    : `恭喜升级到 Level ${currentLevel}（${name}），新入口已解锁。`
  levelUpTargetPath.value = pathForUnlockedAtLevel(currentLevel)
  levelUpModalOpen.value = true
  localStorage.setItem(key, String(currentLevel))
}

function goLevelUpExplore() {
  levelUpModalOpen.value = false
  router.push(levelUpTargetPath.value).catch(() => {})
}

function onLevelUpViewCertificate() {
  levelUpModalOpen.value = false
  openUserCertificateFlow(userStore).catch(() => {})
}

watch(isLoggedIn, async (value) => {
  if (value) {
    await userStore.ensureLevelInfo({ showDefaultMsg: false })
    checkLevelUp()
    loadNotices()
  } else {
    noticeRecords.value = []
    unreadCount.value = 0
  }
})

watch(() => route.path, scrollActiveNav)

onMounted(async () => {
  scrollActiveNav()
  if (isLoggedIn.value) {
    await userStore.ensureLevelInfo({ showDefaultMsg: false })
    checkLevelUp()
    loadNotices()
  }
  noticeTimer = window.setInterval(loadNotices, 60_000)
  window.addEventListener('user-level-refresh', checkLevelUp)
  window.addEventListener('resize', scrollActiveNav)
  document.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  window.removeEventListener('user-level-refresh', checkLevelUp)
  window.removeEventListener('resize', scrollActiveNav)
  document.removeEventListener('click', closeMenu)
  if (noticeTimer) window.clearInterval(noticeTimer)
})

function handleLogout() {
  userMenuOpen.value = false
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出小铜人探案社吗？',
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
.xinglin-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 56px;
  background: rgba(255, 250, 240, 0.96);
  border-bottom: 1px solid rgba(180, 135, 60, 0.2);
  box-shadow: 0 2px 16px rgba(80, 50, 20, 0.06);
  backdrop-filter: blur(10px);
}

.nav-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  width: 100%;
  min-width: 0;
  max-width: 1480px;
  height: 100%;
  margin: 0 auto;
  padding: 0 clamp(14px, 3vw, 32px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  text-decoration: none;
}

.brand__img {
  width: 38px;
  height: 38px;
  object-fit: contain;
  filter: drop-shadow(0 3px 5px rgba(95, 58, 21, 0.18));
}

.brand__name {
  color: #2f7d68;
  font-size: 20px;
  font-weight: 950;
  white-space: nowrap;
}

.brand__copy {
  display: grid;
  line-height: 1.05;
}

.brand__copy small {
  margin-top: 5px;
  color: #946229;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.nav-main {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 2px;
  flex: 1;
  justify-content: flex-start;
  overflow-x: auto;
  scrollbar-width: none;
}

.nav-main::-webkit-scrollbar {
  display: none;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  color: #5c4a30;
  font-size: 15px;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
  border-radius: 10px;
  transition: all 0.16s;
}

.nav-item:hover {
  color: #2f7d68;
  background: rgba(47, 125, 104, 0.06);
}

.nav-item.on {
  color: #fff;
  background: #2f7d68;
  box-shadow: 0 2px 8px rgba(47, 125, 104, 0.25);
}

.nav-item__icon {
  display: grid;
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  place-items: center;
  border-radius: 8px;
  background: rgba(180, 135, 60, 0.15);
  font-size: 13px;
  font-weight: 900;
}

.nav-item.on .nav-item__icon {
  background: rgba(255, 255, 255, 0.2);
}

.nav-actions {
  position: relative;
  display: flex;
  align-items: center;
  gap: 7px;
  flex-shrink: 0;
}

.token-pill {
  display: flex;
  min-width: 88px;
  height: 38px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #4b2f17;
  border: 1px solid rgba(180, 122, 38, 0.38);
  border-radius: 999px;
  background: #fff5d9;
  box-shadow: inset 0 -2px 0 rgba(129, 78, 21, 0.12);
}

.token-pill svg {
  color: #e5a514;
  font-size: 20px;
}

.token-pill strong {
  font-size: 15px;
}

.xinglin-navbar--body-map {
  height: 92px;
  border-bottom: 4px solid #bd7c20;
  background: rgba(255, 243, 210, 0.98);
  box-shadow: 0 7px 18px rgba(78, 43, 13, 0.24);
  backdrop-filter: none;
}

.xinglin-navbar--body-map .nav-inner {
  max-width: 1600px;
  gap: 16px;
  padding: 0 clamp(16px, 2.3vw, 38px);
}

.xinglin-navbar--body-map .brand {
  min-width: 230px;
  gap: 12px;
  padding-right: 18px;
  border-right: 1px solid rgba(135, 80, 22, 0.2);
}

.xinglin-navbar--body-map .brand__img {
  width: 56px;
  height: 64px;
}

.xinglin-navbar--body-map .brand__name {
  color: #432610;
  font-family: var(--site-title-font), "Microsoft YaHei", serif;
  font-size: 23px;
}

.xinglin-navbar--body-map .nav-main {
  height: 100%;
  justify-content: center;
  gap: 0;
}

.xinglin-navbar--body-map .nav-item {
  min-width: 82px;
  height: 82px;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 7px 11px;
  color: #5a3518;
  font-size: 13px;
  border-radius: 0;
}

.xinglin-navbar--body-map .nav-item:hover {
  color: #164d3f;
  background: rgba(25, 83, 67, 0.08);
}

.xinglin-navbar--body-map .nav-item.on {
  height: 82px;
  color: #fff6d6;
  border: 2px solid #d8a644;
  border-radius: 18px;
  background: #155b49;
  box-shadow: inset 0 -5px 0 #0c3e32, 0 5px 0 rgba(115, 70, 20, 0.35), 0 8px 18px rgba(52, 42, 18, 0.18);
}

.xinglin-navbar--body-map .nav-item__icon {
  width: 32px;
  height: 32px;
  color: #815019;
  font-size: 24px;
  border-radius: 0;
  background: transparent;
}

.xinglin-navbar--body-map .nav-item.on .nav-item__icon {
  color: #f7c64f;
  background: transparent;
}

.xinglin-navbar--body-map .nav-actions {
  gap: 9px;
}

.xinglin-navbar--body-map .notice-btn,
.xinglin-navbar--body-map .user-btn {
  height: 42px;
  border-color: rgba(157, 98, 27, 0.3);
  border-radius: 999px;
  background: #fff5d9;
}

.notice-btn {
  position: relative;
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: #2f7d68;
  font-weight: 950;
  border: 1px solid rgba(47, 125, 104, 0.18);
  border-radius: 12px;
  background: #fffaf0;
  cursor: pointer;
}

.notice-btn b {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 19px;
  padding: 2px 5px;
  color: #fff;
  font-size: 10px;
  line-height: 15px;
  border: 2px solid #fffaf0;
  border-radius: 999px;
  background: #d45b42;
}

.notice-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 12;
  width: min(370px, calc(100vw - 24px));
  max-height: min(520px, calc(100vh - 76px));
  overflow-y: auto;
  padding: 8px;
  border: 1px solid rgba(180, 135, 60, 0.25);
  border-radius: 16px;
  background: #fffaf0;
  box-shadow: 0 18px 45px rgba(64, 42, 18, 0.18);
}

.notice-panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 10px 12px;
  color: #3d2915;
}

.notice-panel header div { display: grid; gap: 2px; }
.notice-panel header strong { font-size: 16px; }
.notice-panel header small { color: #8a7355; font-size: 12px; }
.notice-panel header > span { color: #2f7d68; font-size: 12px; font-weight: 900; }
.notice-state { padding: 28px 12px; color: #8a7355; text-align: center; }

.notice-card {
  position: relative;
  display: grid;
  grid-template-columns: 38px 1fr auto;
  gap: 10px;
  width: 100%;
  margin-top: 4px;
  padding: 11px 10px;
  color: #4b3823;
  text-align: left;
  border: none;
  border-radius: 12px;
  background: transparent;
  cursor: pointer;
}

.notice-card:hover, .notice-card.unread { background: rgba(47, 125, 104, 0.07); }
.notice-card__icon { display: grid; width: 36px; height: 36px; place-items: center; color: #fff9df; font-weight: 950; border-radius: 11px; background: #b47a2e; }
.notice-card > span:nth-child(2) { display: grid; gap: 4px; min-width: 0; }
.notice-card strong { overflow: hidden; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.notice-card small { display: -webkit-box; overflow: hidden; color: #806b50; font-size: 12px; line-height: 1.45; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.notice-card i { align-self: start; padding: 2px 6px; color: #fff; font-size: 10px; font-style: normal; border-radius: 999px; background: #d45b42; }

.act-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.16s;
}

.act-btn--ghost {
  margin-right: 6px;
  color: #5c4a30;
  background: transparent;
}

.act-btn--ghost:hover {
  background: rgba(47, 125, 104, 0.08);
}

.act-btn--fill {
  color: #fff;
  background: #2f7d68;
  box-shadow: 0 2px 8px rgba(47, 125, 104, 0.2);
}

.act-btn--fill:hover {
  background: #266a58;
  transform: translateY(-1px);
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 5px;
  border: 1px solid rgba(180, 135, 60, 0.15);
  border-radius: 12px;
  background: rgba(255, 250, 240, 0.5);
  cursor: pointer;
  transition: background 0.16s;
}

.user-btn:hover {
  background: rgba(47, 125, 104, 0.06);
}

.user-btn__avatar {
  display: grid;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  place-items: center;
  color: #fff9df;
  font-size: 13px;
  font-weight: 900;
  border-radius: 9px;
  background: linear-gradient(180deg, #d49b39, #b47a2e);
  box-shadow: inset 0 -2px 0 rgba(88, 48, 14, 0.18);
}

.user-btn__name {
  max-width: 72px;
  overflow: hidden;
  color: #3d2915;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-btn__arrow {
  color: #9a8058;
  font-size: 12px;
  transition: transform 0.2s;
}

.user-btn__arrow.up {
  transform: rotate(180deg);
}

.user-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  z-index: 10;
  min-width: 136px;
  padding: 6px;
  border: 1px solid rgba(180, 135, 60, 0.2);
  border-radius: 12px;
  background: #fffaf0;
  box-shadow: 0 12px 28px rgba(80, 50, 20, 0.14);
}

.user-menu__item {
  display: block;
  width: 100%;
  padding: 10px 14px;
  color: #3d2915;
  font-size: 14px;
  font-weight: 800;
  text-align: left;
  text-decoration: none;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
}

.user-menu__item:hover {
  background: rgba(47, 125, 104, 0.08);
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: grid;
  padding: 20px;
  place-items: center;
  background: rgba(40, 25, 10, 0.35);
  backdrop-filter: blur(4px);
}

.modal-box {
  width: min(400px, 100%);
  padding: 28px 22px 22px;
  text-align: center;
  border: 2px solid rgba(180, 135, 60, 0.3);
  border-radius: 16px;
  background: #fffaf0;
  box-shadow: 0 18px 40px rgba(50, 30, 10, 0.2);
}

.modal-box__icon {
  display: inline-grid;
  width: 48px;
  height: 48px;
  margin-bottom: 10px;
  place-items: center;
  color: #fff9df;
  font-weight: 950;
  border-radius: 50%;
  background: linear-gradient(180deg, #d49b39, #b47a2e);
}

.modal-box h2 {
  margin: 0 0 10px;
  color: #3d2915;
  font-size: 21px;
  font-weight: 950;
}

.modal-box p {
  margin: 0 0 18px;
  color: #66513a;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .brand__name {
    display: none;
  }

  .nav-item__label {
    display: none;
  }

  .nav-item {
    padding: 7px 10px;
  }

  .user-btn__name {
    display: none;
  }

  .xinglin-navbar--body-map {
    height: 76px;
  }

  .xinglin-navbar--body-map .brand {
    min-width: auto;
    padding-right: 8px;
  }

  .xinglin-navbar--body-map .brand__img {
    width: 46px;
    height: 54px;
  }

  .xinglin-navbar--body-map .brand__copy {
    display: none;
  }

  .xinglin-navbar--body-map .nav-item,
  .xinglin-navbar--body-map .nav-item.on {
    min-width: 56px;
    height: 66px;
    padding: 5px 9px;
  }

  .xinglin-navbar--body-map .nav-item__icon {
    width: 28px;
    height: 28px;
    font-size: 21px;
  }

  .xinglin-navbar--body-map .token-pill {
    min-width: 62px;
  }
}

@media (max-width: 600px) {
  .nav-inner {
    gap: 8px;
    padding-inline: 8px;
  }

  .brand__img {
    width: 34px;
    height: 34px;
  }

  .nav-item {
    padding: 6px;
  }

  .nav-actions {
    display: flex;
    min-width: 0;
  }

  .act-btn {
    padding: 8px 10px;
  }

  .act-btn--ghost {
    margin-right: 0;
  }

  .xinglin-navbar--body-map .nav-inner {
    gap: 5px;
    padding-inline: 7px;
  }

  .xinglin-navbar--body-map .brand {
    border-right: 0;
  }

  .xinglin-navbar--body-map .nav-main {
    justify-content: flex-start;
  }

  .xinglin-navbar--body-map .nav-item,
  .xinglin-navbar--body-map .nav-item.on {
    min-width: 48px;
    padding-inline: 6px;
  }

  .xinglin-navbar--body-map .token-pill,
  .xinglin-navbar--body-map .notice-btn {
    display: none;
  }
}

/* Unified illustrated navigation used by every child-facing route. */
.xinglin-navbar {
  z-index: 3000;
  height: var(--site-nav-height);
  border-bottom: 3px solid rgba(184, 115, 51, 0.72);
  background: rgba(255, 247, 223, 0.97);
  box-shadow: 0 5px 18px rgba(73, 47, 20, 0.13);
  backdrop-filter: blur(14px);
}

.nav-inner {
  max-width: 1600px;
  gap: clamp(8px, 1.3vw, 20px);
  padding-inline: clamp(12px, 2.2vw, 34px);
}

.brand { gap: 9px; }
.brand__img { width: 48px; height: 58px; }
.brand__name { color: var(--site-primary-deep); font-family: var(--site-title-font), "Microsoft YaHei", serif; font-size: 19px; }
.brand__copy small { color: var(--site-copper-dark); font-size: 9px; letter-spacing: 0; }

.nav-main {
  height: 100%;
  justify-content: stretch;
  gap: clamp(2px, 0.35vw, 6px);
}
.nav-item {
  flex: 1 1 0;
  min-width: 66px;
  height: 76px;
  flex-direction: column;
  justify-content: center;
  gap: 1px;
  padding: 4px 7px;
  color: #5c452a;
  font-size: 11px;
  border: 1px solid transparent;
  border-radius: 10px;
}

.nav-item:hover {
  color: var(--site-primary-dark);
  border-color: rgba(184, 115, 51, 0.2);
  background: rgba(255, 255, 255, 0.58);
  transform: translateY(-1px);
}

.nav-item.on {
  color: #fff9df;
  border-color: #d8a641;
  background: var(--site-primary-dark);
  box-shadow: inset 0 -3px 0 var(--site-primary-deep), 0 4px 10px rgba(36, 91, 80, 0.22);
}

.nav-item__icon {
  width: 43px;
  height: 43px;
  overflow: visible;
  color: #d79b2d;
  border-radius: 50%;
  background: transparent;
  font-size: 25px;
}

.nav-item__icon img { width: 43px; height: 43px; object-fit: contain; filter: drop-shadow(0 2px 2px rgba(73, 47, 20, 0.16)); }
.nav-item.on .nav-item__icon { background: transparent; }
.nav-item.on .nav-item__icon img { filter: drop-shadow(0 2px 3px rgba(10, 40, 32, 0.34)); }
.notice-btn, .user-btn, .act-btn { min-height: 40px; border-radius: var(--site-radius-md); }
.notice-panel, .user-menu { border-color: var(--site-border-strong); border-radius: var(--site-radius-md); background: var(--site-paper); box-shadow: var(--site-shadow-md); }

@media (max-width: 1180px) {
  .brand__copy { display: none; }
  .nav-item { min-width: 60px; padding-inline: 5px; }
}

@media (max-width: 900px) {
  .nav-item__label { display: block; max-width: 52px; overflow: hidden; font-size: 10px; text-overflow: ellipsis; }
  .nav-item { min-width: 56px; padding: 4px; }
  .nav-item__icon, .nav-item__icon img { width: 39px; height: 39px; }
}

@media (max-width: 720px) {
  .nav-inner { gap: 5px; padding-inline: 7px; }
  .nav-main { justify-content: flex-start; }
  .brand__img { width: 38px; height: 46px; }
  .nav-item { flex: 0 0 auto; min-width: 50px; height: 64px; }
  .nav-item__icon, .nav-item__icon img { width: 36px; height: 36px; }
  .user-btn, .notice-btn { min-height: 36px; }
  .act-btn--ghost, .user-btn__arrow { display: none; }
}

@media (max-width: 460px) {
  .brand { display: none; }
  .nav-actions { max-width: 42px; }
  .act-btn { padding-inline: 9px; font-size: 12px; }
}
</style>

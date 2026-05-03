<template>
  <div class="myworld-page">
    <div class="myworld-grid">
      <section class="left-col">
        <div class="left-top left-split">
          <div class="user-row">
            <img
              class="avatar"
              :src="avatarUrl"
              alt=""
            >
            <div class="user-meta">
              <div class="welcome">
                欢迎你，{{ nicknameText }}小朋友
              </div>
              <div class="lv-row">
                <span>Level {{ levelInfo.level }}</span>
                <span class="lv-badge">{{ levelInfo.levelName || '等级称号' }}</span>
              </div>
              <div>用户 id {{ userDetail.id ?? '-' }}</div>
              <div>注册时间 {{ formatTime(userDetail.createdAt) }}</div>
            </div>
          </div>
        </div>

        <div class="left-bottom">
          <div class="progress-row">
            <div class="progress-left">
              <div class="title">
                我的进度
              </div>
              <a-progress
                type="circle"
                :percent="skillProgressPercent"
                :width="190"
              />
            </div>
            <div class="progress-right">
              <div class="title">
                成长等级
              </div>
              <div class="lv2-row">
                Level {{ levelInfo.level }} <span>{{ levelScoreInLevel }}/{{ levelMaxInLevel }}</span>
              </div>
              <a-progress :percent="levelProgressPercent" />
              <div class="title mt16">
                答题闯关
              </div>
              <div>答题次数 {{ quizTotalCount }} 次 正确率 {{ quizCorrectRate }}%</div>
            </div>
          </div>

          <div class="actions-row">
            <button
              type="button"
              @click="goMyMistakes"
            >
              我的错题
            </button>
            <button
              type="button"
              @click="goMyCollect"
            >
              我的收藏
            </button>
            <button
              type="button"
              @click="goMyPosts"
            >
              我的发帖
            </button>
            <button
              type="button"
              @click="goSettings"
            >
              其他设置
            </button>
          </div>
          <div class="cert-btn-row">
            <button
              type="button"
              class="cert-btn"
              :disabled="!canViewCertificate"
              :title="canViewCertificate ? '查看或领取九级荣誉证书' : '达到 Level 9 后可用'"
              @click="onViewCertificate"
            >
              查看证书
            </button>
            <p
              v-if="!canViewCertificate"
              class="cert-hint"
            >
              达到九级后可查看荣誉证书
            </p>
          </div>
        </div>
      </section>

      <section class="right-col">
        <div class="right-top right-split">
          <div class="badge-head">
            <div class="title">
              成就徽章
            </div>
            <button
              type="button"
              class="badge-share-btn"
              @click="shareBadgesToCommunity"
            >
              分享
            </button>
          </div>
          <div
            v-if="badgeList.length === 0"
            class="empty"
          >
            暂无徽章
          </div>
          <div
            v-else
            class="badge-grid"
          >
            <div
              v-for="b in badgeList"
              :key="`${b.id}-${b.badgeName}`"
              class="badge-item"
            >
              <img
                v-if="badgeImgUrl(b)"
                :src="resolveMediaUrl(b.badgePath)"
                class="badge-img"
                alt=""
                @error="onBadgeImgError(b)"
              >
              <div
                v-else
                class="badge-placeholder"
              >
                徽章
              </div>
              <div class="badge-name">
                {{ b.badgeName || '未命名徽章' }}
              </div>
            </div>
          </div>
        </div>

        <div class="right-bottom">
          <div class="title">
            每日签到
          </div>
          <div class="checkin-toolbar">
            <button
              type="button"
              class="checkin-btn"
              :disabled="checkedToday || checkinSubmitting"
              @click="onCheckinToday"
            >
              {{ checkedToday ? '今日已签到' : (checkinSubmitting ? '签到中…' : '今日签到 +5') }}
            </button>
            <div class="month-switch">
              <button
                type="button"
                class="month-btn"
                @click="goPrevMonth"
              >
                ‹
              </button>
              <span>{{ displayMonthText }}</span>
              <button
                type="button"
                class="month-btn"
                @click="goNextMonth"
              >
                ›
              </button>
            </div>
          </div>
          <div class="calendar-week-head">
            <div
              v-for="w in weekNames"
              :key="w"
              class="week-cell"
            >
              {{ w }}
            </div>
          </div>
          <div class="calendar-grid">
            <div
              v-for="cell in calendarCells"
              :key="cell.key"
              class="calendar-cell"
              :class="{
                'is-out': !cell.inMonth,
                'is-checked': cell.checked,
                'is-today': cell.isToday
              }"
            >
              {{ cell.day }}
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getCurrentUser, checkinToday, getCheckinMonth, getMyBadges } from '@/api/user'
import { getMyBackpack } from '@/api/BackpackApi'
import { getSkillCount } from '@/api'
import { getUserQuizStats } from '@/api/QuizApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import { openUserCertificateFlow } from '@/utils/userCertificate'

const userStore = useUserStore()
const router = useRouter()

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PGNpcmNsZSBjeD0iNTAiIGN5PSI1MCIgcj0iNTAiIGZpbGw9IiNlZWVlZWUiLz48L3N2Zz4='

const userDetail = ref({})
const levelInfo = ref({ level: 1, levelName: '', currentScore: 0, minScore: 0, maxScore: 0 })
const totalSkillCount = ref(0)
const quizTotalCount = ref(0)
const quizCorrectRate = ref(0)
const checkinSubmitting = ref(false)
const checkedDateSet = ref(new Set())
const currentMonth = ref(new Date())
const weekNames = ['日', '一', '二', '三', '四', '五', '六']
const badgeList = ref([])
const badgeImgErrorKeys = ref(new Set())

const avatarUrl = computed(() => resolveMediaUrl(userDetail.value.avatar) || defaultAvatar)
const nicknameText = computed(() => userDetail.value.name || userDetail.value.username || '小朋友')
const backpackCount = ref(0)

const skillProgressPercent = computed(() => {
  const total = Number(totalSkillCount.value) || 0
  if (total <= 0) return 0
  return Math.min(100, Math.round((backpackCount.value / total) * 100))
})

const levelMaxInLevel = computed(() => {
  const min = Number(levelInfo.value.minScore) || 0
  const max = Number(levelInfo.value.maxScore) || 0
  return Math.max(0, max - min)
})

const levelScoreInLevel = computed(() => {
  const cur = Number(levelInfo.value.currentScore) || 0
  const min = Number(levelInfo.value.minScore) || 0
  return Math.max(0, cur - min)
})

const levelProgressPercent = computed(() => {
  const d = levelMaxInLevel.value
  if (d <= 0) return 0
  return Math.min(100, Math.round((levelScoreInLevel.value / d) * 100))
})

const canViewCertificate = computed(() => {
  const lv = Number(levelInfo.value.level)
  return Number.isFinite(lv) && lv >= 9
})

const todayKey = computed(() => toDayKey(new Date()))
const checkedToday = computed(() => checkedDateSet.value.has(todayKey.value))

const displayMonthText = computed(() => {
  const d = currentMonth.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const calendarCells = computed(() => {
  const base = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), 1)
  const year = base.getFullYear()
  const month = base.getMonth()
  const firstWeekday = base.getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const prevDays = new Date(year, month, 0).getDate()
  const out = []
  for (let i = 0; i < firstWeekday; i += 1) {
    const day = prevDays - firstWeekday + i + 1
    out.push(makeCell(year, month - 1, day, false))
  }
  for (let day = 1; day <= daysInMonth; day += 1) {
    out.push(makeCell(year, month, day, true))
  }
  while (out.length < 42) {
    const day = out.length - (firstWeekday + daysInMonth) + 1
    out.push(makeCell(year, month + 1, day, false))
  }
  return out
})

function makeCell(year, month, day, inMonth) {
  const d = new Date(year, month, day)
  const key = toDayKey(d)
  return {
    key: `${key}-${inMonth ? 'in' : 'out'}`,
    day: d.getDate(),
    inMonth,
    checked: checkedDateSet.value.has(key),
    isToday: key === todayKey.value
  }
}

function toDayKey(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function badgeKey(b) {
  return `${b?.id ?? ''}|${b?.badgeName ?? ''}|${b?.badgePath ?? ''}`
}

function badgeImgUrl(b) {
  const p = resolveMediaUrl(b?.badgePath)
  if (!p) return ''
  if (badgeImgErrorKeys.value.has(badgeKey(b))) return ''
  return p
}

function onBadgeImgError(b) {
  badgeImgErrorKeys.value.add(badgeKey(b))
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return d.toLocaleString()
}

function onViewCertificate() {
  if (!userStore.isLoggedIn || !canViewCertificate.value) return
  openUserCertificateFlow(userStore).catch((e) => console.error(e))
}

function goSettings() {
  router.push('/settings').catch(() => {})
}

function goMyMistakes() {
  router.push('/my-mistakes').catch(() => {})
}

function goMyCollect() {
  window.dispatchEvent(new Event('collect-open'))
}

function goMyPosts() {
  router.push('/my-posts').catch(() => {})
}

function shareBadgesToCommunity() {
  const topBadgeNames = (badgeList.value || [])
    .map((x) => String(x?.badgeName || '').trim())
    .filter(Boolean)
    .slice(0, 5)
  const preset = {
    postType: '分享专区',
    title: '最近获得好多成就徽章~',
    content: topBadgeNames.join('\n'),
    images: (badgeList.value || [])
      .map((x) => x?.badgePath)
      .filter(Boolean)
      .slice(0, 5)
  }
  try {
    sessionStorage.setItem('community-publish-preset', JSON.stringify(preset))
  } catch {
    // ignore
  }
  router.push({
    path: '/community',
    query: {
      type: '分享专区',
      openPublish: '1'
    }
  }).catch(() => {})
}

function goPrevMonth() {
  const d = currentMonth.value
  currentMonth.value = new Date(d.getFullYear(), d.getMonth() - 1, 1)
  loadCheckinMonth().catch((e) => console.error(e))
}

function goNextMonth() {
  const d = currentMonth.value
  currentMonth.value = new Date(d.getFullYear(), d.getMonth() + 1, 1)
  loadCheckinMonth().catch((e) => console.error(e))
}

async function loadCheckinMonth() {
  const y = currentMonth.value.getFullYear()
  const m = currentMonth.value.getMonth() + 1
  const dates = await getCheckinMonth({ year: y, month: m }, { showDefaultMsg: false })
  checkedDateSet.value = new Set((Array.isArray(dates) ? dates : []).map((x) => String(x)))
}

async function onCheckinToday() {
  if (checkedToday.value || checkinSubmitting.value) return
  checkinSubmitting.value = true
  try {
    const data = await checkinToday({ showDefaultMsg: false })
    const user = data?.userInfo || null
    const earned = Array.isArray(data?.earnedBadgeNames) ? data.earnedBadgeNames : []
    userDetail.value = user || userDetail.value
    userStore.updateUserInfo(user || {})
    levelInfo.value = await userStore.getLevelInfo()
    await loadCheckinMonth()
    await loadBadges()
    message.success('签到成功，气血 +5')
    for (const n of earned) {
      message.success(`恭喜你达成成绩获得'${n}'`)
    }
  } catch (e) {
    message.warning(e?.message || e?.msg || '签到失败')
  } finally {
    checkinSubmitting.value = false
  }
}

async function loadBadges() {
  const data = await getMyBadges({ showDefaultMsg: false })
  badgeList.value = Array.isArray(data) ? data : []
}

async function loadData() {
  if (!userStore.isLoggedIn) return
  userDetail.value = await getCurrentUser({ showDefaultMsg: false })
  levelInfo.value = await userStore.getLevelInfo()
  const bp = await getMyBackpack({ showDefaultMsg: false })
  backpackCount.value = Array.isArray(bp) ? bp.length : 0
  totalSkillCount.value = Number(await getSkillCount({ showDefaultMsg: false })) || 0
  const qs = await getUserQuizStats({ showDefaultMsg: false })
  const total = Number(qs?.totalCount) || 0
  const correct = Number(qs?.correctCount) || 0
  quizTotalCount.value = total
  quizCorrectRate.value = total > 0 ? Math.round((correct / total) * 100) : 0
  currentMonth.value = new Date()
  await loadCheckinMonth()
  await loadBadges()
}

onMounted(() => {
  loadData().catch((e) => console.error(e))
})
</script>

<style scoped>
.myworld-page {
  min-height: calc(100vh - 64px);
  padding: 20px 24px;
  box-sizing: border-box;
  background: #f6e8d3;
}

.myworld-grid {
  width: 100%;
  height: calc(100vh - 64px - 40px);
  display: grid;
  grid-template-columns: 58% 42%;
  gap: 20px;
}

.left-col {
  display: grid;
  grid-template-rows: 3fr 7fr;
  gap: 16px;
}

.right-col {
  display: grid;
  grid-template-rows: 4fr 6fr;
  gap: 16px;
}

.left-top, .left-bottom, .right-top, .right-bottom {
  padding: 8px;
}

.left-split {
  border-bottom: 1px dashed #bdbdbd;
}

.right-split {
  border-bottom: 1px dashed #bdbdbd;
}

.user-row {
  display: flex;
  align-items: flex-start;
  gap: 22px;
}

.avatar {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  object-fit: cover;
}

.user-meta {
  font-size: 24px;
  line-height: 1.35;
}

.welcome {
  font-size: 32px;
  margin-bottom: 8px;
}

.lv-row {
  display: flex;
  align-items: center;
  gap: 18px;
}

.lv-badge {
  padding: 2px 10px;
  border: 1px solid #bdbdbd;
}

.progress-row {
  display: grid;
  grid-template-columns: 36% 64%;
  align-items: start;
  margin-bottom: 22px;
}

.title {
  font-size: 30px;
  margin-bottom: 10px;
}

.progress-left {
  padding-right: 16px;
}

.progress-right {
  font-size: 22px;
}

.lv2-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.mt16 {
  margin-top: 16px;
}

.actions-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-top: 26px;
}

.actions-row button {
  height: 64px;
  background: #fff;
  border: 1px solid #bfbfbf;
  font-size: 20px;
}

.cert-btn-row {
  margin-top: 20px;
}

.cert-btn {
  width: 100%;
  height: 56px;
  background: linear-gradient(180deg, #fff9e6 0%, #f3e4bd 100%);
  border: 1px solid #c9a227;
  font-size: 20px;
  color: #5c4810;
  cursor: pointer;
}

.cert-btn:hover:not(:disabled) {
  filter: brightness(1.03);
}

.cert-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.35);
}

.cert-hint {
  margin: 8px 0 0;
  font-size: 16px;
  color: #8c8c8c;
  text-align: center;
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.badge-head {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
}

.badge-share-btn {
  height: 42px;
  padding: 0 18px;
  border: 1px solid #999;
  background: #fff;
  font-size: 20px;
  cursor: pointer;
}

.badge-item {
  border: 1px solid #d5d5d5;
  background: #fff;
  padding: 6px;
}

.badge-img,
.badge-placeholder {
  width: 100%;
  height: 64px;
  object-fit: cover;
}

.badge-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  background: #f6f6f6;
}

.badge-name {
  margin-top: 4px;
  font-size: 12px;
  text-align: center;
  line-height: 1.3;
}

.checkin-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.checkin-btn {
  height: 42px;
  padding: 0 14px;
  border: 1px solid #9ec5ff;
  background: #e7f1ff;
  color: #1f4f93;
  font-size: 16px;
  cursor: pointer;
}

.checkin-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.month-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.month-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #ccc;
  background: #fff;
  cursor: pointer;
}

.calendar-week-head,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.week-cell {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.calendar-cell {
  height: 36px;
  border: 1px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: #fff;
}

.calendar-cell.is-out {
  color: #bbb;
}

.calendar-cell.is-checked {
  background: #c8f7d1;
  border-color: #7fdc93;
  color: #1c6d2d;
}

.calendar-cell.is-today {
  box-shadow: inset 0 0 0 2px #4d9bff;
}

@media (max-width: 1200px) {
  .myworld-grid {
    height: auto;
    grid-template-columns: 1fr;
  }
  .left-col, .right-col {
    grid-template-rows: auto;
  }
  .user-meta { font-size: 18px; }
  .welcome, .title, .bp-cat, .actions-row button { font-size: 18px; }
  .avatar { width: 100px; height: 100px; }
}
</style>

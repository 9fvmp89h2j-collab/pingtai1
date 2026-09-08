<template>
  <main class="honor-page">
    <section class="honor-hero" aria-label="小侦探荣誉档案标题">
      <div>
        <h1>小侦探荣誉档案</h1>
        <p>记录你的每一次探索与成长，见证小侦探的光荣时刻！</p>
      </div>
    </section>

    <section class="honor-grid" aria-label="小侦探荣誉档案内容">
      <aside class="profile-panel archive-card" aria-label="档案与徽章墙">
        <div class="profile-summary">
          <div class="detective-portrait">
            <img :src="detectiveGuide" alt="小铜人侦探" />
          </div>
          <div class="rank-copy">
            <span>当前称号</span>
            <strong>{{ detectiveTitle }}</strong>
            <div class="star-score">
              <img :src="rewardStar" alt="" aria-hidden="true" />
              <b>{{ starlightCount }}</b>
              <span>/ 50</span>
            </div>
            <p>再获得{{ nextLevelNeed }}颗星，升级为“明辨小侦探”</p>
          </div>
        </div>

        <section class="badge-wall" aria-labelledby="badge-wall-title">
          <div class="panel-head">
            <h2 id="badge-wall-title">徽章墙</h2>
            <button type="button" @click="showAllBadges = !showAllBadges">
              {{ showAllBadges ? '收起' : '查看全部' }}
            </button>
          </div>
          <div class="badge-wall-grid">
            <article
              v-for="badge in visibleHonorBadges"
              :key="badge.id"
              class="wall-badge"
              :class="{ locked: !badge.unlocked }"
              :title="badge.name"
            >
              <img :src="badge.image" :alt="badge.name" />
              <span>{{ badge.unlocked ? badge.name : '未解锁' }}</span>
            </article>
          </div>
        </section>

        <section class="archive-progress" aria-label="徽章收集进度">
          <div class="archive-progress__copy">
            <h2>已收集徽章进度</h2>
            <div>
              <img :src="rewardStar" alt="" aria-hidden="true" />
              <strong>{{ earnedArchiveCount }} / {{ archiveTotal }}</strong>
            </div>
            <p>继续加油，更多荣誉等着你！</p>
          </div>
          <img class="treasure-img" :src="treasureChest" alt="荣誉宝箱" />
          <div class="progress-track" aria-hidden="true">
            <span :style="{ width: `${archivePercent}%` }"></span>
          </div>
        </section>
      </aside>

      <section class="report-panel archive-card" aria-label="本次探案报告">
        <div class="report-ribbon">
          <img :src="materialBambooSlip" alt="" aria-hidden="true" />
          <h2>本次探案报告</h2>
          <span>成长数据已同步</span>
        </div>

        <div class="success-note">
          <strong>{{ archiveSummary }}</strong>
          <img :src="detectiveGuide" alt="" aria-hidden="true" />
        </div>

        <section class="report-section" aria-labelledby="tasks-title">
          <h3 id="tasks-title">推荐探索区域</h3>
          <div class="task-list">
            <article v-for="task in completedTasks" :key="task.id" class="task-chip">
              <div class="task-chip__icon">
                <img :src="task.image" :alt="task.name" />
              </div>
              <span>{{ task.name }}</span>
            </article>
          </div>
        </section>

        <section class="report-section" aria-labelledby="materials-title">
          <h3 id="materials-title">当前材料袋</h3>
          <div class="material-list">
            <p v-if="!collectedMaterials.length" class="material-empty">
              还没有收集到材料，完成探索任务后会出现在这里。
            </p>
            <article v-for="item in collectedMaterials" :key="item.id" class="material-chip">
              <img :src="item.image" :alt="item.name" />
              <div>
                <strong>{{ item.name }}</strong>
                <span>x {{ item.count }}</span>
              </div>
            </article>
          </div>
        </section>

        <section class="recommend-card" aria-label="推荐下一步任务">
          <img class="agency-img" :src="agencyHouse" alt="侦探社档案任务" />
          <div>
            <span>推荐下一步任务</span>
            <h3>整理侦探社 · 点亮线索墙</h3>
            <p>用你收集的材料，完善侦探社线索墙和荣誉档案，解锁更多探案任务！</p>
            <button type="button" @click="goAgency">
              回到侦探社
              <RightOutlined aria-hidden="true" />
            </button>
          </div>
        </section>
      </section>

      <aside class="insight-panel" aria-label="学习数据与下一步建议">
        <section class="data-card archive-card" aria-labelledby="study-data-title">
          <h2 id="study-data-title">学习数据</h2>
          <div class="data-grid">
            <article>
              <span>点亮星光</span>
              <div class="metric-row">
                <img :src="rewardStar" alt="" aria-hidden="true" />
                <strong>{{ starlightCount }}</strong>
                <em>颗</em>
              </div>
              <p>总计点亮星光</p>
            </article>
            <article>
              <span>当前等级</span>
              <div class="level-token">Lv.{{ displayLevel }}</div>
              <strong>{{ displayLevelName }}</strong>
              <p>经验值 {{ levelScoreInLevel }}/{{ levelMaxInLevel }}</p>
              <div class="mini-progress" aria-hidden="true">
                <span :style="{ width: `${levelProgressPercent}%` }"></span>
              </div>
            </article>
            <article>
              <span>已记录探索</span>
              <div class="metric-row">
                <img :src="calendarIcon" alt="" aria-hidden="true" />
                <strong>{{ exploredItemCount }}</strong>
                <em>项</em>
              </div>
            </article>
            <article>
              <span>完成任务数</span>
              <div class="metric-row">
                <img :src="reportIcon" alt="" aria-hidden="true" />
                <strong>{{ completedTaskCount }}</strong>
                <em>个</em>
              </div>
            </article>
          </div>
        </section>

        <section class="next-card archive-card" aria-labelledby="next-title">
          <h2 id="next-title">下一步建议</h2>
          <button
            v-for="item in nextSuggestions"
            :key="item.id"
            type="button"
            class="suggestion-row"
            @click="goPath(item.path)"
          >
            <img :src="item.image" :alt="item.title" />
            <span>
              <strong>{{ item.title }}</strong>
              <em>{{ item.description }}</em>
            </span>
            <RightOutlined aria-hidden="true" />
          </button>
        </section>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RightOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getCurrentUser, getMyBadges } from '@/api/user'
import { getUserQuizStats } from '@/api/QuizApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import { useGameState } from '@/composables/useGameState'
import detectiveGuide from '@/assets/copper-detective-guide-512.png'
import agencyHouse from '@/assets/侦探社/1.png'
import bodyMapImage from '@/assets/body-map-navigation-child.png'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'

const badge1 = generatedRewardAssets.detectiveAgencyBadge
const badge2 = generatedRewardAssets.safetyObservationBadge
const badge3 = generatedMaterialIcons['acupoint-star-pearl']
const badge4 = generatedMaterialIcons['meridian-star-sand']
const badge5 = generatedRewardAssets.storyArchiveCard
const badge6 = generatedRewardAssets.starClueWall
const materialBadge = generatedRewardAssets.safetyObservationBadge
const materialBambooSlip = generatedMaterialIcons['bamboo-slip-shard']
const materialStar = generatedMaterialIcons['meridian-star-sand']
const materialSafetyBell = generatedMaterialIcons['safety-bell']
const rewardStar = generatedRewardAssets.rewardStarBurst
const treasureChest = generatedRewardAssets.rewardChestOpen

const router = useRouter()
const userStore = useUserStore()
const { completedTaskIds, materials, badges, storyArchiveIds } = useGameState()

const userDetail = ref({})
const levelInfo = ref({ level: 1, levelName: '铜人见习侦探', currentScore: 0, minScore: 0, maxScore: 100 })
const quizTotalCount = ref(0)
const badgeList = ref([])
const showAllBadges = ref(false)

const archiveTotal = 18
const calendarIcon = materialBadge
const reportIcon = materialBambooSlip

const fallbackBadges = [
  { id: 'entry', name: '初探入门', image: badge1, unlocked: false },
  { id: 'expert', name: '线索达人', image: badge2, unlocked: false },
  { id: 'meridian', name: '经络小能手', image: badge3, unlocked: false },
  { id: 'safety', name: '安全先锋', image: badge4, unlocked: false },
  { id: 'story', name: '故事爱好者', image: badge5, unlocked: false },
  { id: 'agency', name: '侦探社档案员', image: badge6, unlocked: false },
  { id: 'locked-1', name: '未解锁', image: materialBadge, unlocked: false },
  { id: 'locked-2', name: '未解锁', image: materialBadge, unlocked: false },
  { id: 'locked-3', name: '未解锁', image: materialBadge, unlocked: false },
  { id: 'locked-4', name: '未解锁', image: materialBadge, unlocked: false },
  { id: 'locked-5', name: '未解锁', image: materialBadge, unlocked: false },
  { id: 'locked-6', name: '未解锁', image: materialBadge, unlocked: false }
]

const completedTasks = [
  { id: 'story', name: '故事馆', image: materialBambooSlip },
  { id: 'body', name: '身体小地图', image: bodyMapImage },
  { id: 'meridian', name: '经络星河', image: materialStar },
  { id: 'copper', name: '小铜人馆', image: detectiveGuide },
  { id: 'safety', name: '安全闯关', image: materialSafetyBell }
]

const collectedMaterials = computed(() => materials.value
  .filter((item) => Number(item.count) > 0)
  .slice(0, 4)
  .map((item) => ({ id: item.id, name: item.name, count: item.count, image: item.icon })))

const nextSuggestions = [
  { id: 'meridian', title: '探索经络星河', description: '点亮更多经络穴位，解锁星河故事！', image: materialStar, path: '/jingluo' },
  { id: 'body', title: '完善身体小地图', description: '标记更多穴位，成为穴位小专家！', image: bodyMapImage, path: '/body-map' },
  { id: 'safety', title: '挑战安全闯关', description: '学习更多安全知识，保护自己和他人！', image: materialSafetyBell, path: '/safety' }
]

const apiBadges = computed(() => {
  return badgeList.value
    .map((badge, index) => {
      const image = resolveMediaUrl(badge?.badgePath)
      return {
        id: `api-${badge?.id ?? index}`,
        name: badge?.badgeName || `荣誉徽章 ${index + 1}`,
        image: image || fallbackBadges[index % fallbackBadges.length].image,
        unlocked: true
      }
    })
    .filter((badge) => badge.name)
})

const honorBadges = computed(() => {
  const unlocked = apiBadges.value.length > 0 ? apiBadges.value : fallbackBadges.filter((badge) => badge.unlocked)
  const merged = [...unlocked]
  const storyBadge = badges.value.find((badge) => badge.id === 'story-collector')
  if (storyBadge && storyArchiveIds.value.length > 0) {
    merged.unshift({
      id: 'local-story-collector',
      name: storyBadge.name,
      image: storyBadge.icon,
      unlocked: storyBadge.status === 'earned',
      progress: storyBadge.progress,
      total: storyBadge.total
    })
  }
  for (const badge of fallbackBadges.filter((item) => !item.unlocked)) {
    if (storyArchiveIds.value.length > 0 && badge.id === 'story') continue
    if (merged.length >= 12) break
    merged.push(badge)
  }
  return merged
})

const visibleHonorBadges = computed(() => showAllBadges.value ? honorBadges.value : honorBadges.value.slice(0, 8))

const earnedArchiveCount = computed(() => {
  const ids = new Set([
    ...apiBadges.value.map((badge) => badge.id),
    ...storyArchiveIds.value.map((caseId) => `story-${caseId}`)
  ])
  return Math.min(archiveTotal, ids.size)
})

const archivePercent = computed(() => Math.round((earnedArchiveCount.value / archiveTotal) * 100))

const displayLevel = computed(() => Number(levelInfo.value.level) || 1)
const displayLevelName = computed(() => levelInfo.value.levelName || '铜人见习侦探')
const starlightCount = computed(() => {
  const fromScore = Number(levelInfo.value.currentScore)
  return Number.isFinite(fromScore) && fromScore > 0 ? Math.round(fromScore / 10) : 0
})
const nextLevelNeed = computed(() => Math.max(0, 50 - starlightCount.value))
const detectiveTitle = computed(() => displayLevel.value >= 3 ? '铜镜发光的新秀' : '铜人见习侦探')
const exploredItemCount = computed(() => completedTaskIds.value.length)
const completedTaskCount = computed(() => completedTaskIds.value.length + quizTotalCount.value)
const archiveSummary = computed(() => completedTaskCount.value > 0
  ? `已记录 ${completedTaskCount.value} 项探索成果，继续寻找下一条文化线索吧！`
  : '档案已经准备好，从第一条文化线索开始探索吧！')

const levelMaxInLevel = computed(() => {
  const min = Number(levelInfo.value.minScore) || 0
  const max = Number(levelInfo.value.maxScore) || 400
  return Math.max(1, max - min)
})

const levelScoreInLevel = computed(() => {
  const cur = Number(levelInfo.value.currentScore) || 0
  const min = Number(levelInfo.value.minScore) || 0
  return Math.max(0, cur - min)
})

const levelProgressPercent = computed(() => {
  const max = levelMaxInLevel.value
  if (max <= 0) return 0
  return Math.min(100, Math.round((levelScoreInLevel.value / max) * 100))
})

function goPath(path) {
  router.push(path).catch(() => {})
}

function goAgency() {
  goPath('/agency')
}

async function loadBadges() {
  const data = await getMyBadges({ showDefaultMsg: false })
  badgeList.value = Array.isArray(data) ? data : []
}

async function loadData() {
  if (!userStore.isLoggedIn) return
  userDetail.value = await getCurrentUser({ showDefaultMsg: false })
  levelInfo.value = await userStore.getLevelInfo()
  const qs = await getUserQuizStats({ showDefaultMsg: false })
  quizTotalCount.value = Number(qs?.totalCount) || 0
  await loadBadges()
}

onMounted(() => {
  loadData().catch((error) => {
    console.warn('[honor-archive] fallback data in use', error)
  })
})
</script>

<style scoped>
.honor-page {
  --paper: rgba(255, 250, 235, 0.95);
  --paper-solid: #fff7df;
  --line: rgba(196, 138, 58, 0.35);
  --deep-green: #2d6e56;
  --text: #4c3319;
  --muted: #7a674c;
  --gold: #dfa33c;
  --orange: #d87928;
  min-height: calc(100vh - 56px);
  padding: 30px 150px 22px 64px;
  color: var(--text);
  background:
    linear-gradient(180deg, rgba(255, 246, 222, 0.7), rgba(235, 226, 194, 0.78)),
    url("../../assets/maps/xinglin-detective-map-bg.png") center / cover fixed no-repeat,
    #f7efd8;
}

.honor-hero,
.honor-grid {
  width: min(100%, 1740px);
  margin: 0 auto;
}

.honor-hero {
  display: flex;
  align-items: end;
  justify-content: flex-start;
  gap: 24px;
  margin-bottom: 18px;
}

.honor-hero h1 {
  margin: 0;
  color: var(--deep-green);
  font-size: clamp(38px, 3.8vw, 56px);
  font-weight: 950;
  line-height: 1.04;
  letter-spacing: 0;
  text-shadow: 0 2px 0 rgba(255, 248, 220, 0.85);
}

.honor-hero p {
  margin: 12px 0 0;
  color: #6d5a3c;
  font-size: 16px;
  font-weight: 800;
}

.archive-card button {
  cursor: pointer;
}

.honor-grid {
  display: grid;
  grid-template-columns: minmax(360px, 520px) minmax(500px, 640px) minmax(330px, 430px);
  align-items: start;
  gap: 24px;
}

.archive-card {
  border: 2px solid var(--line);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 251, 239, 0.96), rgba(255, 242, 210, 0.92));
  box-shadow:
    inset 0 0 0 5px rgba(255, 255, 255, 0.32),
    0 16px 34px rgba(98, 67, 27, 0.12);
}

.profile-panel {
  padding: 20px 22px;
}

.profile-summary {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  gap: 24px;
  align-items: center;
  padding: 10px 8px 18px;
  border-bottom: 1px solid rgba(190, 137, 61, 0.25);
}

.detective-portrait {
  display: grid;
  width: 172px;
  height: 172px;
  place-items: end center;
  border: 3px solid rgba(218, 170, 87, 0.5);
  border-radius: 50%;
  background: radial-gradient(circle, #fff4cc 0%, #f2d394 68%, rgba(242, 211, 148, 0) 70%);
  overflow: hidden;
}

.detective-portrait img {
  width: 150px;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 8px 12px rgba(102, 65, 23, 0.22));
}

.rank-copy span,
.panel-head button,
.report-section h3,
.recommend-card span,
.data-grid article > span {
  color: #8a5c1e;
  font-size: 14px;
  font-weight: 950;
}

.rank-copy strong {
  display: block;
  margin-top: 6px;
  color: var(--deep-green);
  font-size: 20px;
  font-weight: 950;
  line-height: 1.2;
  white-space: nowrap;
}

.star-score {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
}

.star-score img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.star-score b {
  color: #315f4a;
  font-size: 18px;
}

.rank-copy p,
.archive-progress p,
.success-note,
.recommend-card p,
.data-grid p,
.suggestion-row em {
  color: var(--muted);
  font-size: 13px;
  font-weight: 800;
  line-height: 1.55;
}

.badge-wall {
  padding: 18px 0 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-head h2,
.archive-progress h2,
.report-ribbon h2,
.data-card h2,
.next-card h2 {
  margin: 0;
  color: #4d2c12;
  font-size: 22px;
  font-weight: 950;
}

.panel-head button {
  border: 0;
  background: transparent;
}

.badge-wall-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 10px;
}

.wall-badge {
  display: grid;
  justify-items: center;
  gap: 7px;
  min-width: 0;
}

.wall-badge img {
  width: 82px;
  height: 82px;
  object-fit: contain;
  border-radius: 18px;
  filter: drop-shadow(0 7px 8px rgba(93, 57, 19, 0.14));
}

.wall-badge.locked img {
  padding: 15px;
  background: rgba(130, 118, 97, 0.1);
  filter: grayscale(1) opacity(0.42);
}

.wall-badge span {
  max-width: 96px;
  overflow: hidden;
  color: #5d3a16;
  font-size: 13px;
  font-weight: 900;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wall-badge.locked span {
  color: #9b8a72;
}

.archive-progress {
  position: relative;
  min-height: 148px;
  margin-top: 12px;
  padding: 17px 170px 18px 20px;
  border: 1px solid rgba(196, 138, 58, 0.22);
  border-radius: 8px;
  background: rgba(255, 249, 227, 0.72);
}

.archive-progress__copy div {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0 6px;
}

.archive-progress__copy img {
  width: 24px;
  height: 24px;
}

.archive-progress strong {
  color: #684015;
  font-size: 24px;
  font-weight: 950;
}

.treasure-img {
  position: absolute;
  right: 20px;
  bottom: 20px;
  width: 132px;
  height: 100px;
  object-fit: contain;
  filter: drop-shadow(0 10px 12px rgba(103, 65, 21, 0.18));
}

.progress-track,
.mini-progress {
  overflow: hidden;
  border-radius: 999px;
  background: #d9ceb4;
}

.progress-track {
  height: 10px;
  margin-top: 8px;
}

.progress-track span,
.mini-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #79b448, #a8ca55);
}

.report-panel {
  position: relative;
  padding: 58px 18px 20px;
}

.report-ribbon {
  position: absolute;
  left: 50%;
  top: -22px;
  display: flex;
  align-items: center;
  gap: 12px;
  width: min(420px, calc(100% - 60px));
  min-height: 58px;
  padding: 8px 16px;
  border: 2px solid rgba(179, 110, 29, 0.32);
  border-radius: 8px;
  background: linear-gradient(180deg, #ffe6a8, #d99a46);
  box-shadow: 0 10px 16px rgba(103, 65, 21, 0.18);
  transform: translateX(-50%);
}

.report-ribbon img {
  width: 62px;
  height: 62px;
  object-fit: contain;
  margin-top: -16px;
}

.report-ribbon h2 {
  flex: 1;
  font-size: 25px;
  text-align: center;
}

.report-ribbon span {
  position: absolute;
  right: 8px;
  bottom: -20px;
  color: #7e6240;
  font-size: 11px;
  font-weight: 800;
}

.success-note {
  position: relative;
  min-height: 86px;
  margin-bottom: 18px;
  padding: 22px 160px 18px 30px;
  border: 1px solid rgba(196, 138, 58, 0.22);
  border-radius: 8px;
  background: rgba(255, 253, 243, 0.72);
}

.success-note strong {
  display: block;
  color: var(--deep-green);
  font-size: 22px;
  font-weight: 950;
}

.success-note img {
  position: absolute;
  right: 22px;
  bottom: 0;
  width: 108px;
  height: 108px;
  object-fit: contain;
  filter: drop-shadow(0 8px 10px rgba(96, 62, 23, 0.18));
}

.report-section {
  margin-bottom: 14px;
  padding: 16px;
  border: 1px solid rgba(196, 138, 58, 0.22);
  border-radius: 8px;
  background: rgba(255, 253, 243, 0.72);
}

.report-section h3 {
  margin: 0 0 14px;
  color: #4d2c12;
  font-size: 18px;
}

.task-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.task-chip {
  display: grid;
  justify-items: center;
  gap: 8px;
}

.task-chip__icon {
  position: relative;
  display: grid;
  width: 68px;
  height: 68px;
  place-items: center;
  border: 2px solid rgba(201, 143, 58, 0.34);
  border-radius: 50%;
  background: #fff0c8;
}

.task-chip__icon img {
  width: 52px;
  height: 52px;
  object-fit: contain;
  border-radius: 50%;
}

.task-check {
  position: absolute;
  right: -3px;
  bottom: -3px;
  color: #4d9a34;
  font-size: 23px;
  filter: drop-shadow(0 2px 2px rgba(75, 52, 20, 0.22));
}

.task-chip span,
.material-chip strong {
  color: #5c3514;
  font-size: 13px;
  font-weight: 950;
  text-align: center;
}

.material-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.material-empty {
  grid-column: 1 / -1;
  margin: 0;
  padding: 18px;
  border: 1px dashed rgba(196, 138, 58, 0.35);
  border-radius: 8px;
  color: var(--muted);
  text-align: center;
}

.material-chip {
  display: flex;
  align-items: center;
  gap: 9px;
  min-height: 66px;
  padding: 8px;
  border: 1px solid rgba(196, 138, 58, 0.18);
  border-radius: 8px;
  background: rgba(255, 248, 226, 0.72);
}

.material-chip img {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.material-chip div {
  display: grid;
  gap: 3px;
}

.material-chip span {
  color: #2f7d50;
  font-size: 13px;
  font-weight: 950;
}

.recommend-card {
  display: grid;
  grid-template-columns: 172px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
  min-height: 146px;
  padding: 14px 20px;
  border: 1px solid rgba(196, 138, 58, 0.22);
  border-radius: 8px;
  background: rgba(255, 253, 243, 0.72);
}

.agency-img {
  width: 160px;
  height: 124px;
  object-fit: contain;
  filter: drop-shadow(0 10px 12px rgba(103, 65, 21, 0.14));
}

.recommend-card h3 {
  margin: 6px 0 8px;
  color: var(--deep-green);
  font-size: 17px;
  font-weight: 950;
}

.recommend-card button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 160px;
  min-height: 42px;
  margin-top: 8px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient(180deg, #e7963d, #c96a25);
  box-shadow: inset 0 -4px 0 rgba(116, 56, 15, 0.18), 0 8px 16px rgba(116, 56, 15, 0.16);
  color: #fff8e4;
  font-weight: 950;
}

.insight-panel {
  display: grid;
  gap: 18px;
}

.data-card,
.next-card {
  padding: 18px;
}

.data-card h2,
.next-card h2 {
  margin-bottom: 14px;
  text-align: center;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.data-grid article {
  min-height: 126px;
  padding: 14px;
  border: 1px solid rgba(196, 138, 58, 0.22);
  border-radius: 8px;
  background: rgba(255, 253, 243, 0.7);
}

.metric-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
}

.metric-row img {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.metric-row strong {
  color: #4d8b43;
  font-size: 32px;
  font-weight: 950;
}

.metric-row em,
.level-token {
  font-style: normal;
  font-weight: 950;
}

.level-token {
  display: inline-grid;
  min-width: 64px;
  min-height: 42px;
  place-items: center;
  margin: 12px 0 8px;
  border-radius: 999px;
  background: var(--deep-green);
  color: #fff8e4;
  font-size: 24px;
}

.data-grid article > strong {
  display: block;
  color: #315f4a;
  font-size: 17px;
}

.mini-progress {
  height: 9px;
  margin-top: 8px;
}

.next-card {
  display: grid;
  gap: 9px;
}

.suggestion-row {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr) 28px;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 7px 10px;
  border: 1px solid rgba(196, 138, 58, 0.18);
  border-radius: 8px;
  background: rgba(255, 253, 243, 0.66);
  text-align: left;
}

.suggestion-row img {
  width: 56px;
  height: 56px;
  object-fit: contain;
  border-radius: 50%;
}

.suggestion-row span {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.suggestion-row strong {
  color: var(--deep-green);
  font-size: 15px;
  font-weight: 950;
}

.suggestion-row em {
  font-style: normal;
}

.suggestion-row :deep(svg) {
  color: #c4862d;
}

@media (max-width: 1280px) {
  .honor-page {
    padding: 24px 24px 32px;
  }

  .honor-grid {
    grid-template-columns: 1fr;
  }

  .profile-summary {
    grid-template-columns: 170px minmax(0, 1fr);
  }

  .insight-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .honor-hero {
    align-items: start;
    flex-direction: column;
  }

  .profile-summary,
  .recommend-card,
  .insight-panel {
    grid-template-columns: 1fr;
  }

  .badge-wall-grid,
  .material-list,
  .data-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .task-list {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .archive-progress {
    padding-right: 20px;
  }

  .treasure-img {
    position: static;
    margin-top: 12px;
  }
}

@media (max-width: 540px) {
  .honor-page {
    padding: 18px 12px 28px;
  }

  .honor-hero h1 {
    font-size: 34px;
  }

  .badge-wall-grid,
  .material-list,
  .data-grid,
  .task-list {
    grid-template-columns: 1fr;
  }
}
</style>

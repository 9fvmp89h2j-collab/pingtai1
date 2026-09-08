<template>
  <main class="copper-shell" :style="{ '--copper-bg': `url(${mapBackground})` }">
    <section class="copper-hero" aria-label="小铜人馆介绍">
      <button type="button" class="back-button" @click="goHome">
        <ArrowLeftOutlined />
        <span>返回地图</span>
      </button>

      <div class="hero-copy">
        <span class="brand-kicker">小铜人馆 · 人体探案实验室</span>
        <h1>今天，身体地图藏着什么线索？</h1>
        <p>转一转、看一看，寻找隐藏在小铜人身上的中医文化线索。</p>
      </div>

      <div class="hero-tools">
        <div class="detective-status" aria-label="侦探身份">
          <span class="detective-status__icon"><img :src="detectiveBadgeImage" alt="" /></span>
          <span><small>当前身份</small><strong>{{ detectiveRank }}</strong></span>
        </div>
        <button class="atlas-launch" type="button" @click="openAtlas">
          <AppstoreOutlined />百穴图鉴
          <small>{{ allPoints.length || '361' }}颗文化星</small>
        </button>
      </div>
    </section>

    <section class="copper-workspace" aria-label="3D小铜人互动学习区">
      <aside class="left-rail" aria-label="今日探案任务">
        <div class="view-tabs" role="tablist" aria-label="模型视角">
          <button
            v-for="mode in viewModes"
            :key="mode.key"
            type="button"
            :class="{ active: viewMode === mode.key }"
            :aria-pressed="viewMode === mode.key"
            @click="setView(mode.key)"
          >
            {{ mode.label }}
          </button>
        </div>

        <section class="mission-card">
          <div class="panel-title">
            <BookFilled />
            <strong>今日观察任务</strong>
          </div>
          <p>找到 3 个身体线索，修复今天的铜人档案。</p>

          <div class="task-list">
            <button
              v-for="task in todayTasks"
              :key="task.code"
              type="button"
              class="task-row"
              :class="{ active: task.code === selectedPoint?.code, done: observedCodes.includes(task.code) }"
              @click="openTaskClue(task)"
            >
              <span class="task-check">
                <CheckCircleFilled v-if="observedCodes.includes(task.code)" />
              </span>
              <span>
                <strong>{{ observedCodes.includes(task.code) ? `已找到${task.name}星点` : '寻找隐藏星点' }}</strong>
                <em>{{ observedCodes.includes(task.code) ? '线索已收入档案' : '请旋转铜人并点击发光点' }}</em>
              </span>
            </button>
          </div>

          <div class="task-progress">
            <span>搜查进度</span>
            <strong>{{ completedTasks }} / {{ todayTasks.length || 3 }}</strong>
          </div>
          <div class="progress-track" aria-hidden="true">
            <span :style="{ width: `${taskProgressPercent}%` }"></span>
          </div>
          <small class="progress-storage">{{ progressStorageHint }}</small>
        </section>

        <section class="reward-preview">
          <span class="reward-preview__eyebrow">完成本案可获得</span>
          <div class="reward-items">
            <span><img :src="copperTokenImage" alt="" />铜片 ×2</span>
            <span><img :src="starPearlImage" alt="" />经络星砂 ×5</span>
          </div>
          <p>材料将用于修复侦探社与点亮经络星河。</p>
          <p class="reward-bag">
            <img :src="materialBagImage" alt="" />
            <span>材料袋：铜片 {{ copperTokens }} · 星砂 {{ starSand }}</span>
          </p>
        </section>
      </aside>

      <section class="model-stage" aria-label="三维小铜人观察区">
        <div class="stage-heading">
          <span>铜人星点搜查案</span>
          <strong>{{ stageInstruction }}</strong>
        </div>
        <div ref="viewerRef" class="model-viewer"></div>

        <div v-if="dataLoadError" class="data-error" role="alert">
          <strong>今日探案暂时没有载入</strong>
          <span>{{ dataLoadError }}</span>
          <button type="button" :disabled="dataLoading" @click="reloadDailyCase">
            {{ dataLoading ? '正在重试…' : '重新载入' }}
          </button>
        </div>

        <div class="stage-hint stage-hint--left">
          <AimOutlined />
          <span>点击星点<br>发现线索</span>
        </div>

        <div class="stage-hint stage-hint--right">
          <RotateRightOutlined />
          <span>拖动旋转<br>360°观察</span>
        </div>

        <div class="viewer-controls" aria-label="模型操作">
          <button type="button" aria-label="放大模型" @click="zoomCamera(0.86)">
            <PlusOutlined />
          </button>
          <button type="button" aria-label="缩小模型" @click="zoomCamera(1.16)">
            <MinusOutlined />
          </button>
          <button type="button" aria-label="重置视角" @click="resetCamera">
            <SettingOutlined />
          </button>
        </div>

        <div class="viewer-status" role="status">
          <span><DragOutlined /></span>
          {{ statusText }}
        </div>
      </section>

      <aside class="right-rail" aria-label="线索档案">
        <section class="clue-file">
          <div class="clue-file__topline">
            <span><img :src="clueFileImage" alt="" />线索档案</span>
            <small>已收录 {{ completedTasks }}/3</small>
          </div>

          <article class="selected-card">
            <span class="panel-kicker">当前发现</span>
            <h2>{{ selectedPoint?.name || '等待发现' }}</h2>
            <strong>{{ selectedPoint ? `${selectedPoint.code} · ${selectedPoint.pinyin}` : '旋转铜人寻找发光星点' }}</strong>
          </article>

          <dl class="clue-meta">
            <div><dt>所属路线</dt><dd>{{ selectedMeridianName }}</dd></div>
            <div><dt>身体区域</dt><dd>{{ bodyArea }}</dd></div>
          </dl>

          <div class="clue-knowledge">
            <span><BulbFilled />小知识</span>
            <p>{{ selectedSummary }}</p>
          </div>

          <div v-if="selectedPoint?.childTraditionalUse" class="clue-knowledge clue-knowledge--use">
            <span><BookFilled />传统用途</span>
            <p>{{ selectedPoint.childTraditionalUse }}</p>
          </div>

          <div class="file-rewards">
            <span>本案完成奖励</span>
            <div>
              <strong><img :src="copperTokenImage" alt="" />铜片 ×2</strong>
              <strong><img :src="starPearlImage" alt="" />星砂 ×5</strong>
            </div>
          </div>

          <button class="collect-clue" type="button" :disabled="!selectedPoint || !isCurrentTarget || !isCurrentRevealed || isCurrentObserved || collecting" @click="collectCurrentClue">
            <CheckCircleFilled />
            {{ collectButtonText }}
          </button>

          <p class="safety-inline"><img :src="safetyShieldImage" alt="" />这里只进行观察学习，不进行针刺操作。</p>
        </section>
      </aside>
    </section>

    <aside class="copper-tip" aria-label="小铜人提示">
      <BulbFilled />
      <div>
        <strong>小铜人提示</strong>
        <p>只观察、只学习，不自己针刺哦！</p>
      </div>
      <img :src="teacherImage" alt="" aria-hidden="true" />
    </aside>

    <Teleport to="body">
      <div v-if="atlasOpen" class="atlas-mask" role="dialog" aria-modal="true" aria-labelledby="atlas-title" @click.self="closeAtlas">
        <section class="atlas-book">
          <header class="atlas-header">
            <div>
              <span class="atlas-kicker"><AppstoreOutlined />小铜人百穴图鉴</span>
              <h2 id="atlas-title">身体里的文化星图</h2>
              <p>按经络和身体区域认识穴位，只观察、只学习，不在自己身上寻找。</p>
            </div>
            <button class="atlas-close" type="button" aria-label="关闭穴位图鉴" @click="closeAtlas"><CloseOutlined /></button>
          </header>

          <section class="atlas-progress" aria-label="穴位图鉴收集进度">
            <div class="atlas-progress__seal"><strong>{{ atlasDiscoveredCount }}</strong><span>已发现</span></div>
            <div class="atlas-progress__copy">
              <div><strong>图鉴进度</strong><span>{{ atlasDiscoveredCount }} / {{ allPoints.length || 361 }}</span></div>
              <div class="atlas-progress__track" aria-hidden="true"><span :style="{ width: `${atlasProgressPercent}%` }"></span></div>
              <small>完成每日3D探案，就会把发现的星点永久收入图鉴。</small>
            </div>
          </section>

          <div class="atlas-controls">
            <label class="atlas-search">
              <SearchOutlined />
              <span class="sr-only">搜索穴位</span>
              <input v-model.trim="atlasSearch" type="search" placeholder="搜索穴位名、编号或身体区域" />
            </label>
            <label class="atlas-select">
              <span>经络</span>
              <select v-model="atlasMeridian">
                <option value="all">全部经络</option>
                <option v-for="item in atlasMeridians" :key="item.code" :value="item.code">{{ item.name }}</option>
              </select>
            </label>
            <div class="atlas-status-tabs" role="group" aria-label="发现状态筛选">
              <button v-for="item in atlasStatusOptions" :key="item.value" type="button" :class="{ active: atlasStatus === item.value }" @click="atlasStatus = item.value">
                {{ item.label }}
              </button>
            </div>
          </div>

          <div v-if="atlasLoading" class="atlas-empty" role="status">小铜人正在整理穴位档案……</div>
          <div v-else-if="atlasError" class="atlas-empty atlas-empty--error" role="alert">
            <span>{{ atlasError }}</span><button type="button" @click="loadAtlas">重新载入</button>
          </div>
          <div v-else class="atlas-layout">
            <section class="atlas-catalog" aria-label="穴位图鉴列表">
              <div class="atlas-result-line"><strong>{{ filteredAtlasPoints.length }}</strong> 颗文化星符合条件</div>
              <div v-if="visibleAtlasPoints.length" class="atlas-grid">
                <button
                  v-for="point in visibleAtlasPoints"
                  :key="point.code"
                  type="button"
                  class="atlas-card"
                  :class="{ discovered: isAtlasPointDiscovered(point), active: atlasSelected?.code === point.code }"
                  @click="atlasSelected = point"
                >
                  <span class="atlas-card__code">{{ point.code }}</span>
                  <span class="atlas-card__status">{{ isAtlasPointDiscovered(point) ? '已发现' : '待探案' }}</span>
                  <strong>{{ point.name }}</strong>
                  <small>{{ point.meridianName }}</small>
                  <em>{{ point.bodyArea }}</em>
                </button>
              </div>
              <div v-else class="atlas-empty">没有找到符合条件的穴位星点。</div>
              <button v-if="visibleAtlasPoints.length < filteredAtlasPoints.length" class="atlas-more" type="button" @click="atlasVisibleCount += 24">
                再展开24颗星
              </button>
            </section>

            <aside class="atlas-detail" aria-live="polite">
              <template v-if="atlasSelected">
                <div class="atlas-detail__topline">
                  <span>{{ isAtlasPointDiscovered(atlasSelected) ? '已收入侦探档案' : '等待在3D探案中发现' }}</span>
                  <strong>{{ atlasSelected.code }}</strong>
                </div>
                <h3>{{ atlasSelected.name }}</h3>
                <p class="atlas-pinyin">{{ atlasSelected.pinyin }} · {{ atlasSelected.meridianName }}</p>
                <dl>
                  <div><dt>身体区域</dt><dd>{{ atlasSelected.bodyArea }}</dd></div>
                  <div><dt>观察提示</dt><dd>{{ atlasSelected.childLocation }}</dd></div>
                </dl>
                <section class="atlas-story"><BulbFilled /><div><strong>文化星小档案</strong><p>{{ atlasSelected.childDescription }}</p></div></section>
                <section v-if="atlasSelected.childTraditionalUse" class="atlas-story atlas-story--use"><BookFilled /><div><strong>传统用途</strong><p>{{ atlasSelected.childTraditionalUse }}</p><small v-if="atlasSelected.traditionalUseSourceName">资料：{{ atlasSelected.traditionalUseSourceName }}</small></div></section>
                <section class="atlas-safety"><SafetyCertificateFilled /><p>{{ atlasSelected.safetyTip }}</p></section>
              </template>
              <div v-else class="atlas-detail__placeholder">
                <img :src="teacherImage" alt="" />
                <strong>选择一颗文化星</strong>
                <p>小铜人会为你打开儿童穴位档案。</p>
              </div>
            </aside>
          </div>
        </section>
      </div>

      <div v-if="welcomeOpen" class="welcome-mask" role="dialog" aria-modal="true" aria-labelledby="welcome-title">
        <section class="welcome-card">
          <div class="welcome-visual" :style="{ backgroundImage: `url(${mapBackground})` }">
            <img :src="welcomeSceneImage" alt="小铜人侦探拿着放大镜" />
            <span>新案件</span>
          </div>
          <div class="welcome-copy">
            <span class="brand-kicker">来自小铜人的紧急委托</span>
            <h2 id="welcome-title">身体地图出现了神秘星点！</h2>
            <p>小侦探，请转动铜人，找到今天的 3 条线索，一起修复缺失的铜人档案吧。</p>
            <button type="button" @click="startInvestigation"><AimOutlined />开始观察</button>
            <small><SafetyCertificateFilled />只观察、只学习，不自己针刺</small>
          </div>
        </section>
      </div>

      <div v-if="caseCompleteOpen" class="case-complete-mask" @click.self="caseCompleteOpen = false">
        <section class="case-complete-card" role="dialog" aria-modal="true" aria-label="探案完成">
          <img :src="teacherHappyImage" alt="开心的小铜人老师" />
          <span>档案修复完成</span>
          <h2>你找到了身体地图的 3 条线索！</h2>
          <p>观察力真棒！奖励已经放进你的探案材料袋。</p>
          <div class="reward-items reward-items--center">
            <span><img :src="copperTokenImage" alt="" />铜片 ×2</span>
            <span><img :src="starPearlImage" alt="" />经络星砂 ×5</span>
          </div>
          <button type="button" @click="caseCompleteOpen = false">继续观察铜人</button>
        </section>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js'
import {
  AimOutlined,
  AppstoreOutlined,
  ArrowLeftOutlined,
  BookFilled,
  BulbFilled,
  CheckCircleFilled,
  CloseOutlined,
  DragOutlined,
  MinusOutlined,
  PlusOutlined,
  RotateRightOutlined,
  SafetyCertificateFilled,
  SearchOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import mapBackground from '@/assets/maps/xinglin-detective-map-bg.png'
import teacherImage from '@/assets/copper-detective-2026/detective-guide.png'
import teacherHappyImage from '@/assets/copper-detective-2026/detective-success.png'
import welcomeSceneImage from '@/assets/copper-detective-2026/detective-welcome.png'
import detectiveBadgeImage from '@/assets/copper-detective-2026/detective-badge.png'
import clueFileImage from '@/assets/copper-detective-2026/clue-file.png'
import copperTokenImage from '@/assets/copper-detective-2026/copper-token.png'
import starPearlImage from '@/assets/copper-detective-2026/star-sand.png'
import safetyShieldImage from '@/assets/copper-detective-2026/safety-shield.png'
import materialBagImage from '@/assets/copper-detective-2026/material-bag.png'
import { discoverCopperManAcupoint, getCopperManDailyCase, listCopperManAcupoints } from '@/api/AcupunctureApi'
import { useUserStore } from '@/store/user'
import { createChibiCopperMan } from '@/utils/chibiCopperMan'

const viewerRef = ref(null)
const selectedCode = ref('')
const statusText = ref('正在载入人体模型...')
const viewMode = ref('front')
const points = ref([])
const targetCodes = ref([])
const observedCodes = ref([])
const revealedCodes = ref([])
const caseDate = ref('')
const progressPersisted = ref(false)
const copperTokens = ref(0)
const starSand = ref(0)
const completedCases = ref(0)
const dataLoading = ref(false)
const dataLoadError = ref('')
const collecting = ref(false)
const welcomeOpen = ref(true)
const caseCompleteOpen = ref(false)
const atlasOpen = ref(false)
const atlasLoading = ref(false)
const atlasError = ref('')
const allPoints = ref([])
const atlasSearch = ref('')
const atlasMeridian = ref('all')
const atlasStatus = ref('all')
const atlasVisibleCount = ref(24)
const atlasSelected = ref(null)

let renderer
let scene
let camera
let controls
let raycaster
let mouse
let animationId
let markerGroup
const markerByCode = new Map()
const userStore = useUserStore()
const route = useRoute()

const viewModes = [
  { key: 'front', label: '正面' },
  { key: 'back', label: '背面' },
  { key: 'side', label: '侧面' }
]
const atlasStatusOptions = [
  { value: 'all', label: '全部' },
  { value: 'discovered', label: '已发现' },
  { value: 'undiscovered', label: '待探案' }
]

const investigationPoints = computed(() => points.value.filter((point) => point.position).slice(0, 5))

const selectedPoint = computed(() => {
  return investigationPoints.value.find((point) => point.code === selectedCode.value) || null
})

const selectedSummary = computed(() => {
  if (!selectedPoint.value) return '旋转模型并点击发光圆点，发现今天的身体文化线索。'
  return selectedPoint.value.childDescription
})

const selectedMeridianName = computed(() => {
  return selectedPoint.value?.meridianName || '当前经络'
})

const todayTasks = computed(() => {
  return targetCodes.value
    .map((code) => points.value.find((point) => point.code === code))
    .filter(Boolean)
    .map((point) => ({ ...point, note: `去${point.bodyArea}寻找发光点` }))
})

const completedTasks = computed(() => todayTasks.value.filter((task) => observedCodes.value.includes(task.code)).length)
const atlasMeridians = computed(() => {
  const names = new Map()
  allPoints.value.forEach((point) => names.set(point.meridianCode, point.meridianName))
  return [...names.entries()].map(([code, name]) => ({ code, name }))
})
const atlasDiscoveredCount = computed(() => allPoints.value.filter(isAtlasPointDiscovered).length)
const atlasProgressPercent = computed(() => allPoints.value.length
  ? Math.round((atlasDiscoveredCount.value / allPoints.value.length) * 100)
  : 0)
const filteredAtlasPoints = computed(() => {
  const keyword = atlasSearch.value.toLowerCase()
  return allPoints.value.filter((point) => {
    const matchesKeyword = !keyword || [point.name, point.code, point.pinyin, point.bodyArea, point.meridianName]
      .some((value) => String(value || '').toLowerCase().includes(keyword))
    const matchesMeridian = atlasMeridian.value === 'all' || point.meridianCode === atlasMeridian.value
    const discovered = isAtlasPointDiscovered(point)
    const matchesStatus = atlasStatus.value === 'all'
      || (atlasStatus.value === 'discovered' && discovered)
      || (atlasStatus.value === 'undiscovered' && !discovered)
    return matchesKeyword && matchesMeridian && matchesStatus
  })
})
const visibleAtlasPoints = computed(() => filteredAtlasPoints.value.slice(0, atlasVisibleCount.value))
const isCurrentObserved = computed(() => observedCodes.value.includes(selectedCode.value))
const isCurrentTarget = computed(() => targetCodes.value.includes(selectedCode.value))
const isCurrentRevealed = computed(() => revealedCodes.value.includes(selectedCode.value))
const detectiveRank = computed(() => {
  if (completedCases.value >= 14) return '杏林金牌侦探'
  if (completedCases.value >= 7) return '铜人档案师'
  if (completedCases.value >= 3) return '星点观察员'
  return '铜人见习侦探'
})
const progressStorageHint = computed(() => progressPersisted.value
  ? '登录进度已保存到成长档案'
  : '登录状态失效，当前进度无法保存')
const collectButtonText = computed(() => {
  if (collecting.value) return '正在收入档案…'
  if (!isCurrentTarget.value) return '自由观察星点'
  if (!isCurrentRevealed.value) return '先在铜人模型上找到星点'
  return isCurrentObserved.value ? '线索已收入档案' : '收集这条线索'
})

const bodyArea = computed(() => {
  return selectedPoint.value?.bodyArea || '身体观察区'
})

const stageInstruction = computed(() => {
  if (completedTasks.value >= todayTasks.value.length && todayTasks.value.length) return '今日线索已全部找到'
  return `找到今天的线索星点 · ${completedTasks.value}/${todayTasks.value.length || 3}`
})
const taskProgressPercent = computed(() => {
  if (!todayTasks.value.length) return 0
  return Math.round((completedTasks.value / todayTasks.value.length) * 100)
})

function goHome() {
  window.location.hash = '/home-map'
}

function startInvestigation() {
  welcomeOpen.value = false
}

function isAtlasPointDiscovered(point) {
  return Boolean(point?.discovered)
}

function markAtlasPointDiscovered(code) {
  if (!code) return
  allPoints.value = allPoints.value.map((point) => point.code === code ? { ...point, discovered: true } : point)
}

async function loadAtlas() {
  if (atlasLoading.value) return
  atlasLoading.value = true
  atlasError.value = ''
  try {
    const data = await listCopperManAcupoints()
    allPoints.value = (Array.isArray(data) ? data : []).map((point) => ({
      ...point,
      discovered: Boolean(point.discovered)
    }))
    const requestedCode = String(route.query.acupoint || '').toUpperCase()
    atlasSelected.value = allPoints.value.find((point) => point.code === requestedCode)
      || allPoints.value.find(isAtlasPointDiscovered)
      || allPoints.value[0]
      || null
  } catch (error) {
    atlasError.value = error.message || '穴位图鉴暂时无法载入'
  } finally {
    atlasLoading.value = false
  }
}

async function openAtlas() {
  atlasOpen.value = true
  atlasVisibleCount.value = 24
  document.body.style.overflow = 'hidden'
  if (!allPoints.value.length) await loadAtlas()
}

function closeAtlas() {
  atlasOpen.value = false
  document.body.style.overflow = ''
}

function handleAtlasKeydown(event) {
  if (event.key === 'Escape' && atlasOpen.value) closeAtlas()
}

function selectPoint(point) {
  selectedCode.value = point.code
  highlightMarker(point.code)
}

function openTaskClue(point) {
  if (!observedCodes.value.includes(point.code)) {
    statusText.value = '这条线索还藏在铜人身上，请旋转模型寻找发光点'
    return
  }
  selectPoint(point)
}

async function collectCurrentClue() {
  if (!selectedPoint.value || !isCurrentTarget.value || !isCurrentRevealed.value || isCurrentObserved.value || collecting.value) return
  collecting.value = true
  dataLoadError.value = ''
  try {
    if (!progressPersisted.value) {
      throw new Error('登录状态已失效，请重新登录后再收入线索。')
    }
    const result = await discoverCopperManAcupoint(selectedPoint.value.code)
    observedCodes.value = result.discoveredCodes || []
    markAtlasPointDiscovered(selectedPoint.value.code)
    copperTokens.value = result.copperTokens || 0
    starSand.value = result.starSand || 0
    completedCases.value = result.completedCases || 0
    if (result.newlyCompleted) {
      caseCompleteOpen.value = true
      await userStore.loadGameState({ showDefaultMsg: false }).catch(() => {})
    }
    highlightMarker(selectedPoint.value.code)
  } catch (error) {
    dataLoadError.value = error.message || '线索暂时无法保存，请稍后重试。'
  } finally {
    collecting.value = false
  }
}

function setView(mode) {
  viewMode.value = mode
  if (!camera || !controls) return

  const distance = 310
  if (mode === 'back') {
    camera.position.set(0, 0, -distance)
  } else if (mode === 'side') {
    camera.position.set(distance, 0, 0)
  } else {
    camera.position.set(0, 0, distance)
  }
  controls.target.set(0, -12, 0)
  controls.update()
}

function zoomCamera(factor) {
  if (!camera || !controls) return
  camera.position.multiplyScalar(factor)
  camera.position.clampLength(120, 430)
  controls.update()
}

function resetCamera() {
  setView(viewMode.value)
}

async function loadDailyCase() {
  if (!userStore.isLoggedIn) {
    window.location.hash = '/auth/login?redirect=/copper-man'
    throw new Error('请先登录后再进入小铜人馆。')
  }
  const data = await getCopperManDailyCase()
  if (!data.persisted) {
    throw new Error('登录状态已失效，请重新登录后再进入小铜人馆。')
  }
  caseDate.value = data.caseDate
  targetCodes.value = data.targetCodes || []
  points.value = (data.featuredPoints || []).map((point) => ({
    ...point,
    position: {
      x: Number(point.positionX),
      y: Number(point.positionY),
      z: Number(point.positionZ)
    }
  }))
  progressPersisted.value = true
  observedCodes.value = data.discoveredCodes || []
  copperTokens.value = data.copperTokens || 0
  starSand.value = data.starSand || 0
  completedCases.value = data.completedCases || 0
  revealedCodes.value = [...observedCodes.value]
  selectedCode.value = observedCodes.value[0] || ''
  if (targetCodes.value.length && targetCodes.value.every((code) => observedCodes.value.includes(code))) {
    welcomeOpen.value = false
  }
}

async function reloadDailyCase() {
  if (dataLoading.value) return
  dataLoading.value = true
  dataLoadError.value = ''
  try {
    await loadDailyCase()
    await nextTick()
    if (!renderer) initScene()
  } catch (error) {
    dataLoadError.value = error.message || '无法连接穴位知识库，请确认后端服务已经启动。'
    statusText.value = '穴位知识载入失败'
  } finally {
    dataLoading.value = false
  }
}

function initScene() {
  const el = viewerRef.value
  const { width, height } = el.getBoundingClientRect()

  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(42, width / height, 0.5, 2000)
  camera.position.set(0, 0, 310)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setClearColor(0x000000, 0)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 0.96
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(width, height)
  renderer.domElement.style.background = 'transparent'
  renderer.domElement.setAttribute('aria-label', '可旋转的小铜人三维模型')
  el.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.enablePan = false
  controls.minDistance = 120
  controls.maxDistance = 430
  controls.target.set(0, -8, 0)
  controls.minPolarAngle = Math.PI * 0.39
  controls.maxPolarAngle = Math.PI * 0.61

  scene.add(new THREE.HemisphereLight(0xfff8e7, 0x6a3d1f, 0.56))
  scene.add(new THREE.AmbientLight(0xffe5b8, 0.14))
  const keyLight = new THREE.DirectionalLight(0xfff1cc, 1.08)
  keyLight.position.set(48, 92, 72)
  scene.add(keyLight)
  const rimLight = new THREE.DirectionalLight(0xffc675, 0.46)
  rimLight.position.set(-58, 40, -44)
  scene.add(rimLight)

  markerGroup = new THREE.Group()
  scene.add(markerGroup)
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()

  createPedestal()
  loadBodyModel()
  createMarkers()
  window.addEventListener('resize', resizeViewer)
  renderer.domElement.addEventListener('pointerdown', handlePointerDown)
  animate()
}

function createPedestal() {
  const baseMaterial = new THREE.MeshStandardMaterial({
    color: 0xc78c3e,
    metalness: 0.55,
    roughness: 0.28
  })
  const topMaterial = new THREE.MeshStandardMaterial({
    color: 0xe7c277,
    metalness: 0.5,
    roughness: 0.22
  })

  const base = new THREE.Mesh(new THREE.CylinderGeometry(62, 68, 10, 88), baseMaterial)
  base.position.y = -101
  scene.add(base)

  const top = new THREE.Mesh(new THREE.CylinderGeometry(52, 60, 5, 88), topMaterial)
  top.position.y = -93
  scene.add(top)
}

function loadBodyModel() {
  const copperMaterial = new THREE.MeshStandardMaterial({
    color: 0x9b4f1f,
    metalness: 0.42,
    roughness: 0.48,
    side: THREE.DoubleSide
  })

  new OBJLoader().load(
    '/assets/copper-man/modelo/corpo.obj',
    (object) => {
      object.name = '成人针灸铜人'
      object.traverse((child) => {
        if (child.isLineSegments) {
          child.visible = false
          return
        }
        if (!child.isMesh) return
        child.geometry.computeVertexNormals()
        child.material = copperMaterial
      })
      object.position.y = -95
      scene.add(object)
      statusText.value = '成人针灸铜人已就位：拖动旋转，滚轮缩放，点击星点查看信息'
      setView(viewMode.value)
    },
    undefined,
    () => {
      scene.add(createChibiCopperMan())
      statusText.value = '成人模型载入失败，已切换到备用小铜人'
      setView(viewMode.value)
    }
  )
}

function createMarkers() {
  const geometry = new THREE.SphereGeometry(0.78, 20, 20)
  const material = new THREE.MeshStandardMaterial({
    color: 0x1d6fd1,
    emissive: 0x0b5fc4,
    emissiveIntensity: 0.65,
    roughness: 0.2
  })

  investigationPoints.value.forEach((point) => {
    const marker = new THREE.Mesh(geometry, material.clone())
    marker.name = point.code
    marker.userData.pointCode = point.code
    marker.position.set(point.position.x, point.position.y, point.position.z)
    markerGroup.add(marker)
    markerByCode.set(point.code, marker)
  })

  highlightMarker(selectedCode.value)
}

function highlightMarker(code) {
  markerByCode.forEach((marker, markerCode) => {
    const active = markerCode === code
    marker.scale.setScalar(active ? 2.35 : 1)
    const observed = observedCodes.value.includes(markerCode)
    marker.material.color.set(active ? 0xffa51f : observed ? 0x4ab07a : 0xe9b63f)
    marker.material.emissive.set(active ? 0xff7a00 : observed ? 0x21895b : 0xd89813)
    marker.material.emissiveIntensity = active ? 1.2 : 0.9
  })
}

function handlePointerDown(event) {
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(mouse, camera)

  const hits = raycaster.intersectObjects(markerGroup.children, false)
  const code = hits[0]?.object?.userData?.pointCode
  const point = points.value.find((item) => item.code === code)
  if (point) {
    if (!revealedCodes.value.includes(point.code)) {
      revealedCodes.value = [...revealedCodes.value, point.code]
    }
    selectPoint(point)
  }
}

function resizeViewer() {
  if (!renderer || !viewerRef.value) return
  const { width, height } = viewerRef.value.getBoundingClientRect()
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

function animate() {
  animationId = window.requestAnimationFrame(animate)
  controls?.update()
  renderer?.render(scene, camera)
}

onMounted(async () => {
  window.addEventListener('keydown', handleAtlasKeydown)
  await reloadDailyCase()
  if (route.query.acupoint) await openAtlas()
})

onBeforeUnmount(() => {
  window.cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resizeViewer)
  window.removeEventListener('keydown', handleAtlasKeydown)
  document.body.style.overflow = ''
  renderer?.domElement?.removeEventListener('pointerdown', handlePointerDown)
  renderer?.dispose()
  markerByCode.clear()
})
</script>

<style scoped>
.copper-shell {
  min-height: calc(100vh - 56px);
  padding: 26px clamp(118px, 8vw, 150px) 22px clamp(34px, 3.4vw, 50px);
  background-color: #f6eedb;
  background-image:
    linear-gradient(90deg, rgba(255, 248, 232, 0.78), rgba(246, 251, 237, 0.7)),
    var(--copper-bg);
  background-size: cover;
  background-position: center;
  color: #314634;
}

.copper-hero {
  display: grid;
  grid-template-columns: 130px minmax(0, 1fr) auto;
  gap: 28px;
  max-width: 1400px;
  margin: 0 auto 16px;
  align-items: center;
}

.back-button,
.view-tabs button,
.task-row,
.split-title button,
.viewer-controls button,
.point-list button {
  font: inherit;
}

.back-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  width: 126px;
  min-height: 48px;
  border: 1px solid rgba(202, 155, 82, 0.46);
  border-radius: 8px;
  background: rgba(255, 251, 240, 0.92);
  color: #2f5d4e;
  cursor: pointer;
  font-weight: 900;
  box-shadow: 0 8px 18px rgba(106, 75, 32, 0.12);
}

.hero-copy {
  min-width: 0;
}

.brand-kicker,
.panel-kicker {
  display: block;
  color: #c28a35;
  font-size: 16px;
  font-weight: 950;
}

.hero-copy h1 {
  margin: 3px 0 4px;
  color: #365c42;
  font-size: clamp(34px, 4vw, 54px);
  line-height: 1.02;
  letter-spacing: 0;
}

.hero-copy p {
  margin: 0;
  color: #5d513e;
  font-size: 16px;
  font-weight: 700;
}

.hero-tools {
  display: grid;
  gap: 8px;
  justify-items: stretch;
}

.detective-status {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px 9px 10px;
  border: 1px solid rgba(202, 155, 82, 0.34);
  border-radius: 12px;
  background: rgba(255, 251, 240, 0.82);
}

.detective-status__icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
}

.detective-status__icon img {
  width: 44px;
  height: 44px;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(99, 67, 28, 0.18));
}

.detective-status small,
.detective-status strong {
  display: block;
}

.atlas-launch {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 1px 8px;
  align-items: center;
  min-height: 42px;
  padding: 7px 12px;
  border: 1px solid rgba(38, 104, 75, 0.34);
  border-radius: 10px;
  background: #356b50;
  color: #fffaf0;
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 950;
  text-align: left;
  box-shadow: 0 8px 18px rgba(47, 93, 72, 0.18);
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

.atlas-launch svg {
  grid-row: span 2;
  font-size: 19px;
}

.atlas-launch small {
  color: #e8d8a9;
  font-size: 10px;
  font-weight: 800;
}

.atlas-launch:hover,
.atlas-launch:focus-visible {
  outline: none;
  background: #285c44;
  box-shadow: 0 10px 22px rgba(47, 93, 72, 0.26);
  transform: translateY(-1px);
}

.detective-status small {
  color: #8a7a63;
  font-size: 11px;
  font-weight: 800;
}

.detective-status strong {
  margin-top: 2px;
  color: #365c42;
  font-size: 14px;
}

.copper-workspace {
  display: grid;
  grid-template-columns: minmax(250px, 292px) minmax(460px, 1fr) minmax(318px, 342px);
  gap: 14px;
  max-width: 1400px;
  margin: 0 auto;
  align-items: stretch;
}

.left-rail,
.right-rail {
  display: grid;
  align-self: stretch;
  gap: 14px;
  min-width: 0;
}

.left-rail {
  grid-template-rows: auto auto 1fr;
}

.right-rail {
  grid-template-rows: auto 1fr;
}

.view-tabs,
.mission-card,
.collection-card,
.reward-preview,
.model-stage,
.achievement-card,
.search-panel,
.clue-file,
.copper-tip {
  border: 1px solid rgba(202, 155, 82, 0.42);
  border-radius: 8px;
  background: rgba(255, 251, 240, 0.86);
  box-shadow: 0 12px 28px rgba(102, 74, 37, 0.12);
  backdrop-filter: blur(10px);
}

.view-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
  padding: 8px;
}

.view-tabs button {
  min-height: 36px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #4e493d;
  cursor: pointer;
  font-weight: 900;
}

.view-tabs button.active {
  background: #47715a;
  color: #fff;
  box-shadow: inset 0 -2px 0 rgba(0, 0, 0, 0.12);
}

.mission-card,
.collection-card,
.reward-preview,
.search-panel,
.clue-file {
  padding: 14px;
}

.panel-title,
.split-title > span {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4a3e2d;
  font-size: 15px;
  font-weight: 950;
}

.panel-title :deep(svg) {
  color: #d59a32;
}

.mission-card p {
  margin: 7px 0 10px;
  color: #796b55;
  font-size: 13px;
  font-weight: 700;
}

.task-list {
  display: grid;
  overflow: hidden;
  border: 1px solid rgba(202, 155, 82, 0.24);
  border-radius: 8px;
  background: rgba(255, 255, 250, 0.72);
}

.task-row {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 8px;
  min-height: 58px;
  padding: 9px 10px;
  border: 0;
  border-bottom: 1px solid rgba(202, 155, 82, 0.2);
  background: transparent;
  color: #384636;
  cursor: pointer;
  text-align: left;
}

.task-row:last-child {
  border-bottom: 0;
}

.task-row.active,
.task-row:hover {
  background: rgba(255, 243, 218, 0.9);
}

.task-check {
  display: grid;
  width: 20px;
  height: 20px;
  margin-top: 2px;
  place-items: center;
  border: 2px solid #c7b89d;
  border-radius: 50%;
  color: #ff8514;
  font-size: 14px;
}

.task-row.done .task-check {
  border-color: #ff8514;
}

.task-row strong,
.task-row em {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-row strong {
  color: #344331;
  font-size: 15px;
  font-weight: 950;
}

.task-row em {
  margin-top: 3px;
  color: #786f61;
  font-size: 13px;
  font-style: normal;
  font-weight: 700;
}

.task-progress {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  color: #574a37;
  font-size: 13px;
  font-weight: 900;
}

.progress-track {
  height: 8px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: #dddcd4;
}

.progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #4d9063, #75b179);
}

.progress-storage {
  display: block;
  margin-top: 8px;
  color: #736957;
  font-size: 11px;
  line-height: 1.45;
}

.collection-card {
  align-self: start;
}

.reward-preview {
  align-self: start;
  padding: 16px;
  background: rgba(246, 251, 232, 0.88);
}

.reward-preview__eyebrow {
  color: #6f624f;
  font-size: 12px;
  font-weight: 900;
}

.reward-items {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  margin-top: 10px;
}

.reward-items span {
  display: inline-flex;
  min-height: 38px;
  padding: 5px 9px;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(202, 155, 82, 0.28);
  border-radius: 9px;
  background: rgba(255, 253, 246, 0.88);
  color: #5d4a32;
  font-size: 12px;
  font-weight: 900;
}

.reward-items img,
.file-rewards img {
  width: 27px;
  height: 27px;
  object-fit: contain;
}

.reward-preview p {
  margin: 10px 0 0;
  color: #776b58;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.55;
}

.reward-preview .reward-bag {
  display: flex;
  padding-top: 8px;
  align-items: center;
  gap: 8px;
  border-top: 1px dashed rgba(152, 112, 51, 0.28);
  color: #285f4d;
  font-weight: 800;
}

.reward-preview .reward-bag img {
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(99, 67, 28, 0.14));
}

.split-title {
  justify-content: space-between;
}

.split-title button {
  border: 0;
  background: transparent;
  color: #8a7a63;
  cursor: pointer;
  font-size: 12px;
  font-weight: 900;
}

.collect-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  margin-top: 12px;
  border: 1px solid rgba(202, 155, 82, 0.22);
  border-radius: 8px;
  background: rgba(202, 155, 82, 0.16);
}

.collect-grid article {
  display: grid;
  min-width: 0;
  min-height: 112px;
  padding: 10px 6px;
  place-items: center;
  background: rgba(255, 255, 250, 0.78);
  text-align: center;
}

.collect-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-size: 24px;
  box-shadow: inset 0 -3px 0 rgba(70, 41, 12, 0.18);
}

.collect-icon.gold {
  background: #f5af2f;
}

.collect-icon.green {
  background: #3a9b73;
}

.collect-icon.blue {
  background: #4f86df;
}

.collect-grid small,
.collect-grid strong {
  display: block;
}

.collect-grid small {
  color: #776b58;
  font-size: 12px;
  font-weight: 800;
}

.collect-grid strong {
  color: #3c362d;
  font-size: 20px;
  line-height: 1;
}

.model-stage {
  position: relative;
  min-height: 686px;
  overflow: hidden;
  background-image:
    linear-gradient(rgba(255, 248, 231, 0.72), rgba(255, 248, 231, 0.82)),
    var(--copper-bg);
  background-size: cover;
  background-position: center;
}

.stage-heading {
  position: absolute;
  z-index: 3;
  top: 16px;
  right: 18px;
  left: 18px;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  pointer-events: none;
}

.stage-heading span {
  padding: 7px 11px;
  border-radius: 9px;
  background: #47715a;
  color: #fffbe9;
  font-size: 12px;
  font-weight: 900;
}

.stage-heading strong {
  color: #876333;
  font-size: 13px;
}

.model-stage::after {
  position: absolute;
  right: 8%;
  bottom: 5%;
  left: 8%;
  height: 86px;
  pointer-events: none;
  content: "";
  background: radial-gradient(ellipse at center, rgba(142, 93, 34, 0.2), transparent 68%);
}

.model-viewer {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 686px;
}

.reference-copper-view {
  position: absolute;
  z-index: 1;
  inset: 62px 110px 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  pointer-events: none;
  transition: transform 180ms ease, opacity 180ms ease;
}

.reference-copper-view img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: saturate(0.96) contrast(1.02);
}

.reference-copper-view--side img {
  transform: translateX(2%);
}

.reference-copper-view--side.is-mirrored img {
  transform: translateX(-2%) scaleX(-1);
}

.model-viewer canvas {
  position: relative;
  z-index: 2;
}

.data-error {
  position: absolute;
  z-index: 5;
  top: 50%;
  left: 50%;
  display: grid;
  width: min(360px, calc(100% - 48px));
  padding: 20px;
  border: 1px solid rgba(184, 117, 49, 0.45);
  border-radius: 12px;
  background: rgba(255, 250, 236, 0.96);
  box-shadow: 0 16px 38px rgba(76, 52, 19, 0.18);
  color: #5d4a32;
  gap: 8px;
  text-align: center;
  transform: translate(-50%, -50%);
}

.data-error span {
  font-size: 13px;
  line-height: 1.5;
}

.data-error button {
  min-height: 40px;
  border: 0;
  border-radius: 8px;
  background: #287458;
  color: #fff;
  cursor: pointer;
  font-weight: 800;
}

.model-viewer canvas {
  display: block;
  cursor: grab;
}

.model-viewer canvas:active {
  cursor: grabbing;
}

.stage-hint {
  position: absolute;
  z-index: 2;
  top: 43%;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #756550;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.45;
}

.stage-hint--left {
  left: 8%;
}

.stage-hint--right {
  right: 8%;
}

.stage-hint :deep(svg) {
  width: 34px;
  height: 34px;
  padding: 8px;
  border: 1px solid rgba(133, 105, 62, 0.35);
  border-radius: 50%;
  background: rgba(255, 253, 247, 0.76);
  color: #8f714d;
}

.viewer-controls {
  position: absolute;
  z-index: 2;
  right: 22px;
  bottom: 112px;
  display: grid;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(86, 62, 35, 0.12);
}

.viewer-controls button {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 0;
  border-bottom: 1px solid rgba(202, 155, 82, 0.28);
  background: rgba(255, 253, 248, 0.95);
  color: #5d513f;
  cursor: pointer;
  font-size: 17px;
}

.viewer-controls button:last-child {
  border-bottom: 0;
}

.viewer-controls button:hover {
  background: #fff2d5;
  color: #2f5d4e;
}

.viewer-status {
  position: absolute;
  z-index: 2;
  right: 24%;
  bottom: 18px;
  left: 24%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  padding: 8px 14px;
  border: 1px solid rgba(223, 153, 64, 0.48);
  border-radius: 999px;
  background: rgba(255, 247, 229, 0.9);
  color: #8b5f2a;
  font-size: 13px;
  font-weight: 900;
  text-align: center;
  box-shadow: 0 8px 16px rgba(111, 78, 36, 0.08);
}

.viewer-status span {
  display: grid;
  width: 26px;
  height: 26px;
  flex: 0 0 auto;
  place-items: center;
  color: #a97137;
}

.achievement-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 74px;
  gap: 12px;
  min-height: 94px;
  padding: 15px 16px;
  align-items: center;
}

.achievement-card span,
.search-box > span,
.list-heading span {
  color: #4f4638;
  font-size: 13px;
  font-weight: 900;
}

.achievement-card strong {
  display: block;
  margin-top: 3px;
  color: #2f5d4e;
  font-size: 38px;
  line-height: 1;
}

.achievement-card small {
  color: #4f4638;
  font-size: 15px;
}

.achievement-card img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 8px 16px rgba(141, 92, 35, 0.16);
}

.right-rail {
  grid-template-rows: 1fr;
}

.clue-file {
  display: flex;
  min-height: 686px;
  flex-direction: column;
  gap: 13px;
}

.clue-file__topline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(202, 155, 82, 0.22);
}

.clue-file__topline > span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #4a3e2d;
  font-size: 15px;
  font-weight: 950;
}

.clue-file__topline > span img {
  width: 34px;
  height: 34px;
  object-fit: contain;
  filter: drop-shadow(0 3px 5px rgba(99, 67, 28, 0.16));
}

.clue-file__topline :deep(svg) {
  color: #d59a32;
}

.clue-file__topline small {
  color: #8a7a63;
  font-weight: 800;
}

.clue-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  margin: 0;
  overflow: hidden;
  border: 1px solid rgba(202, 155, 82, 0.22);
  border-radius: 9px;
  background: rgba(202, 155, 82, 0.18);
}

.clue-meta div {
  padding: 11px;
  background: rgba(255, 253, 246, 0.92);
}

.clue-meta dt {
  color: #96836a;
  font-size: 11px;
  font-weight: 800;
}

.clue-meta dd {
  margin: 5px 0 0;
  color: #315a43;
  font-size: 14px;
  font-weight: 950;
}

.clue-knowledge {
  padding: 13px;
  border-left: 3px solid #d6a146;
  border-radius: 0 8px 8px 0;
  background: rgba(248, 244, 216, 0.76);
}

.clue-knowledge span {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #a16f25;
  font-size: 12px;
  font-weight: 950;
}

.clue-knowledge p {
  margin: 8px 0 0;
  color: #554937;
  font-size: 13px;
  font-weight: 750;
  line-height: 1.7;
}

.file-rewards {
  margin-top: auto;
  padding-top: 13px;
  border-top: 1px solid rgba(202, 155, 82, 0.22);
}

.file-rewards > span {
  color: #766650;
  font-size: 12px;
  font-weight: 900;
}

.file-rewards > div {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.file-rewards strong {
  display: inline-flex;
  flex: 1;
  padding: 7px 9px;
  align-items: center;
  gap: 5px;
  border: 1px solid rgba(202, 155, 82, 0.25);
  border-radius: 9px;
  background: #fffaf0;
  color: #654f33;
  font-size: 12px;
}

.collect-clue,
.welcome-copy button,
.case-complete-card button {
  display: inline-flex;
  min-height: 46px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 0;
  border-radius: 10px;
  background: #2f7d68;
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 950;
  box-shadow: 0 7px 16px rgba(47, 125, 104, 0.2);
}

.collect-clue:disabled {
  background: #7c9a8d;
  cursor: default;
  box-shadow: none;
}

.safety-inline {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  color: #39715e;
  font-size: 11px;
  font-weight: 900;
  line-height: 1.5;
}

.safety-inline img {
  width: 26px;
  height: 26px;
  flex: 0 0 auto;
  object-fit: contain;
}

.search-panel {
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr);
  gap: 12px;
  min-height: 578px;
}

.search-box {
  display: grid;
  gap: 9px;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 0 12px;
  border: 1px solid rgba(162, 124, 76, 0.42);
  border-radius: 8px;
  background: rgba(255, 254, 247, 0.92);
  color: #9b7d57;
}

.search-input-wrap input {
  min-width: 0;
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
  color: #2d3d31;
  font: inherit;
  font-weight: 800;
}

.search-input-wrap input::placeholder {
  color: #a89982;
}

.selected-card {
  position: relative;
  min-height: 162px;
  padding: 14px;
  border: 1px solid rgba(226, 148, 45, 0.58);
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(255, 250, 231, 0.95), rgba(238, 249, 221, 0.92));
}

.selected-card::after {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  content: "";
  background: #ff8b13;
  box-shadow: 0 0 0 5px rgba(255, 139, 19, 0.12);
}

.selected-card h2 {
  margin: 6px 0 4px;
  color: #236241;
  font-size: 30px;
  line-height: 1.05;
}

.selected-card strong {
  display: block;
  color: #4e4638;
  font-size: 14px;
  font-weight: 950;
}

.selected-card p {
  display: -webkit-box;
  overflow: hidden;
  margin: 11px 0 0;
  color: #4c4032;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.list-heading {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding-top: 4px;
  border-top: 1px solid rgba(202, 155, 82, 0.22);
}

.list-heading strong {
  min-width: 0;
  overflow: hidden;
  color: #4d3f2f;
  font-size: 14px;
  font-weight: 950;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-list {
  display: grid;
  gap: 7px;
  min-height: 0;
  overflow: auto;
  padding: 1px 5px 2px 0;
  scrollbar-color: rgba(110, 113, 104, 0.65) rgba(233, 224, 205, 0.8);
  scrollbar-width: thin;
}

.point-list button {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 12px;
  gap: 2px 10px;
  min-height: 56px;
  padding: 10px 12px;
  align-items: center;
  border: 1px solid rgba(202, 155, 82, 0.28);
  border-radius: 8px;
  background: rgba(255, 253, 246, 0.88);
  color: #2d3a31;
  cursor: pointer;
  text-align: left;
}

.point-list button:hover,
.point-list button:focus-visible,
.point-list button.active {
  outline: none;
  border-color: rgba(235, 144, 37, 0.84);
  background: #fff6e7;
  box-shadow: 0 0 0 2px rgba(235, 144, 37, 0.12);
}

.point-list strong {
  grid-row: span 2;
  color: #d37d23;
  font-size: 13px;
  font-weight: 950;
}

.point-list span,
.point-list em {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-list span {
  color: #334332;
  font-weight: 950;
}

.point-list em {
  color: #625a4d;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.point-list i {
  grid-row: span 2;
  width: 10px;
  height: 10px;
  justify-self: center;
  border-radius: 50%;
  background: transparent;
}

.point-list button.active i {
  background: #ff8b13;
}

.copper-tip {
  position: fixed;
  z-index: 900;
  bottom: 14px;
  left: clamp(26px, 3.4vw, 48px);
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) 86px;
  gap: 10px;
  width: min(392px, calc(100vw - 52px));
  min-height: 88px;
  padding: 13px 12px 10px;
  align-items: center;
  border-color: rgba(74, 169, 128, 0.6);
  background: linear-gradient(100deg, rgba(232, 249, 220, 0.95), rgba(255, 249, 223, 0.9));
}

.copper-tip :deep(svg) {
  display: block;
  width: 38px;
  height: 38px;
  padding: 8px;
  border-radius: 50%;
  background: #45aa75;
  color: #ffe88d;
}

.copper-tip strong {
  display: block;
  color: #1d7e5b;
  font-size: 17px;
}

.copper-tip p {
  margin: 4px 0 0;
  color: #2d5f52;
  font-size: 13px;
  font-weight: 900;
}

.copper-tip img {
  align-self: end;
  width: 82px;
  height: 76px;
  object-fit: contain;
}

.welcome-mask,
.case-complete-mask {
  position: fixed;
  inset: 0;
  z-index: 2200;
  display: grid;
  padding: 22px;
  place-items: center;
  background: rgba(42, 35, 24, 0.5);
  backdrop-filter: blur(8px);
}

.welcome-card {
  display: grid;
  grid-template-columns: minmax(270px, 0.82fr) minmax(360px, 1.18fr);
  width: min(820px, 100%);
  min-height: 430px;
  overflow: hidden;
  border: 1px solid rgba(208, 163, 88, 0.62);
  border-radius: 22px;
  background: #fffaf0;
  box-shadow: 0 28px 80px rgba(48, 35, 18, 0.32);
}

.welcome-visual {
  position: relative;
  display: grid;
  min-height: 430px;
  padding: 24px;
  place-items: end center;
  background-color: #eaf4df;
  background-image: var(--copper-bg);
  background-size: cover;
  background-position: center;
}

.welcome-visual::after {
  position: absolute;
  inset: 0;
  content: "";
  background: rgba(231, 243, 214, 0.76);
}

.welcome-visual img {
  position: relative;
  z-index: 1;
  width: min(290px, 86%);
  height: 330px;
  object-fit: contain;
  object-position: center bottom;
  filter: drop-shadow(0 16px 18px rgba(105, 68, 27, 0.2));
}

.welcome-visual > span {
  position: absolute;
  z-index: 2;
  top: 24px;
  left: 24px;
  padding: 7px 12px;
  border-radius: 999px;
  background: #d8892b;
  color: #fffaf0;
  font-size: 12px;
  font-weight: 950;
}

.welcome-copy {
  display: flex;
  padding: 52px 46px 38px;
  flex-direction: column;
  justify-content: center;
}

.welcome-copy h2 {
  margin: 11px 0 16px;
  color: #315a43;
  font-size: 36px;
  line-height: 1.2;
}

.welcome-copy > p {
  margin: 0 0 26px;
  color: #645742;
  font-size: 15px;
  font-weight: 750;
  line-height: 1.8;
}

.welcome-copy small {
  display: flex;
  margin-top: 17px;
  align-items: center;
  justify-content: center;
  gap: 7px;
  color: #39715e;
  font-weight: 900;
}

.case-complete-card {
  width: min(470px, 100%);
  padding: 28px;
  border: 1px solid rgba(208, 163, 88, 0.62);
  border-radius: 20px;
  background: #fffaf0;
  text-align: center;
  box-shadow: 0 28px 80px rgba(48, 35, 18, 0.32);
}

.case-complete-card > img {
  width: 130px;
  height: 130px;
  object-fit: contain;
}

.case-complete-card > span {
  display: block;
  color: #c78931;
  font-size: 13px;
  font-weight: 950;
}

.case-complete-card h2 {
  margin: 8px 0 10px;
  color: #315a43;
  font-size: 25px;
}

.case-complete-card p {
  margin: 0;
  color: #6d5f4c;
  font-weight: 750;
}

.reward-items--center {
  justify-content: center;
  margin: 18px 0;
}

.case-complete-card button {
  width: 100%;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.atlas-mask {
  position: fixed;
  z-index: 2200;
  inset: 0;
  display: grid;
  padding: 4vh 3vw;
  place-items: center;
  background: rgba(28, 41, 31, 0.72);
  backdrop-filter: blur(7px);
  animation: atlas-mask-in 180ms ease-out;
}

.atlas-book {
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr);
  width: min(1480px, 96vw);
  height: min(900px, 92vh);
  overflow: hidden;
  border: 1px solid rgba(196, 145, 65, 0.64);
  border-radius: 22px;
  background:
    radial-gradient(circle at 8% 12%, rgba(221, 190, 109, 0.15), transparent 28%),
    linear-gradient(135deg, #fffaf0, #f5ecd6 72%, #edf3df);
  box-shadow: 0 34px 90px rgba(17, 29, 22, 0.38);
  color: #344b39;
  animation: atlas-book-in 220ms ease-out;
}

.atlas-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 22px 26px 15px;
  border-bottom: 1px solid rgba(177, 129, 59, 0.2);
}

.atlas-kicker {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #b87925;
  font-size: 13px;
  font-weight: 950;
  letter-spacing: 0.08em;
}

.atlas-header h2 {
  margin: 4px 0 5px;
  color: #315b43;
  font-size: clamp(28px, 3vw, 42px);
  line-height: 1;
}

.atlas-header p {
  margin: 0;
  color: #72634d;
  font-size: 13px;
  font-weight: 750;
}

.atlas-close {
  display: grid;
  flex: 0 0 auto;
  width: 42px;
  height: 42px;
  border: 1px solid rgba(82, 101, 76, 0.2);
  border-radius: 50%;
  place-items: center;
  background: rgba(255, 255, 255, 0.65);
  color: #46634d;
  cursor: pointer;
  font-size: 18px;
}

.atlas-close:hover,
.atlas-close:focus-visible {
  outline: 3px solid rgba(197, 141, 54, 0.24);
  background: #fff;
}

.atlas-progress {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  margin: 12px 26px 0;
  padding: 10px 14px;
  border: 1px solid rgba(56, 108, 78, 0.2);
  border-radius: 15px;
  background: rgba(240, 246, 225, 0.74);
}

.atlas-progress__seal {
  display: grid;
  width: 58px;
  height: 58px;
  border: 2px solid #c98d31;
  border-radius: 50%;
  place-content: center;
  background: #fff8df;
  color: #97661e;
  text-align: center;
  box-shadow: inset 0 0 0 4px rgba(201, 141, 49, 0.1);
}

.atlas-progress__seal strong,
.atlas-progress__seal span {
  display: block;
}

.atlas-progress__seal strong {
  font-size: 19px;
  line-height: 1;
}

.atlas-progress__seal span {
  margin-top: 3px;
  font-size: 9px;
  font-weight: 900;
}

.atlas-progress__copy > div:first-child {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 900;
}

.atlas-progress__copy small {
  display: block;
  margin-top: 4px;
  color: #6f725f;
  font-size: 10px;
  font-weight: 750;
}

.atlas-progress__track {
  height: 7px;
  margin-top: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #dce4cd;
}

.atlas-progress__track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #cb8c2d, #f1c459, #4f8a62);
  transition: width 300ms ease;
}

.atlas-controls {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 210px auto;
  gap: 10px;
  padding: 12px 26px;
}

.atlas-search,
.atlas-select {
  display: flex;
  min-height: 42px;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(143, 106, 51, 0.24);
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.76);
  color: #7f6a4c;
}

.atlas-search {
  padding: 0 12px;
}

.atlas-search input,
.atlas-select select {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #344b39;
  font: inherit;
  font-size: 13px;
  font-weight: 750;
}

.atlas-select {
  padding-left: 12px;
}

.atlas-select span {
  flex: 0 0 auto;
  color: #966b2c;
  font-size: 11px;
  font-weight: 900;
}

.atlas-select select {
  height: 40px;
  padding-right: 8px;
}

.atlas-status-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 3px;
  padding: 4px;
  border: 1px solid rgba(143, 106, 51, 0.2);
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.62);
}

.atlas-status-tabs button {
  min-width: 65px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #75654e;
  cursor: pointer;
  font: inherit;
  font-size: 11px;
  font-weight: 900;
}

.atlas-status-tabs button.active {
  background: #3d7256;
  color: #fff;
}

.atlas-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  min-height: 0;
  border-top: 1px solid rgba(177, 129, 59, 0.16);
}

.atlas-catalog {
  min-height: 0;
  padding: 15px 18px 22px 26px;
  overflow: auto;
}

.atlas-result-line {
  margin: 0 0 10px;
  color: #796b56;
  font-size: 11px;
  font-weight: 800;
}

.atlas-result-line strong {
  color: #b87622;
  font-size: 14px;
}

.atlas-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(138px, 1fr));
  gap: 9px;
}

.atlas-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px 8px;
  min-height: 116px;
  padding: 12px;
  overflow: hidden;
  border: 1px solid rgba(153, 119, 66, 0.23);
  border-radius: 13px;
  background: rgba(255, 253, 246, 0.78);
  color: #394b3b;
  cursor: pointer;
  font: inherit;
  text-align: left;
  transition: border 150ms ease, transform 150ms ease, box-shadow 150ms ease;
}

.atlas-card::after {
  position: absolute;
  right: -18px;
  bottom: -26px;
  width: 70px;
  height: 70px;
  border: 1px solid rgba(180, 132, 53, 0.14);
  border-radius: 50%;
  content: "";
}

.atlas-card:hover,
.atlas-card:focus-visible,
.atlas-card.active {
  outline: none;
  border-color: rgba(191, 124, 31, 0.72);
  box-shadow: 0 9px 18px rgba(113, 80, 37, 0.12);
  transform: translateY(-2px);
}

.atlas-card.discovered {
  border-color: rgba(55, 118, 82, 0.36);
  background: linear-gradient(145deg, rgba(247, 252, 235, 0.95), rgba(255, 248, 225, 0.9));
}

.atlas-card__code {
  color: #b77b27;
  font-size: 11px;
  font-weight: 950;
}

.atlas-card__status {
  padding: 2px 6px;
  border-radius: 999px;
  background: #eee8d8;
  color: #82735b;
  font-size: 9px;
  font-weight: 900;
}

.atlas-card.discovered .atlas-card__status {
  background: #d9ead7;
  color: #2f704e;
}

.atlas-card strong,
.atlas-card small,
.atlas-card em {
  grid-column: 1 / -1;
}

.atlas-card strong {
  margin-top: 3px;
  color: #315940;
  font-size: 20px;
  line-height: 1.05;
}

.atlas-card small {
  overflow: hidden;
  color: #655b4c;
  font-size: 11px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.atlas-card em {
  color: #98733c;
  font-size: 10px;
  font-style: normal;
  font-weight: 850;
}

.atlas-more {
  display: block;
  min-width: 180px;
  min-height: 38px;
  margin: 16px auto 0;
  border: 1px solid rgba(53, 107, 80, 0.3);
  border-radius: 10px;
  background: #edf3df;
  color: #356b50;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 950;
}

.atlas-detail {
  min-height: 0;
  padding: 20px;
  overflow: auto;
  border-left: 1px solid rgba(177, 129, 59, 0.2);
  background: rgba(255, 249, 232, 0.72);
}

.atlas-detail__topline {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: #3d7755;
  font-size: 10px;
  font-weight: 900;
}

.atlas-detail__topline strong {
  color: #b67927;
}

.atlas-detail h3 {
  margin: 14px 0 2px;
  color: #2f5b41;
  font-size: 34px;
  line-height: 1;
}

.atlas-pinyin {
  margin: 0 0 16px;
  color: #8b704c;
  font-size: 12px;
  font-weight: 800;
}

.atlas-detail dl {
  display: grid;
  gap: 8px;
  margin: 0;
}

.atlas-detail dl div {
  padding: 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.62);
}

.atlas-detail dt {
  color: #a47735;
  font-size: 10px;
  font-weight: 950;
}

.atlas-detail dd {
  margin: 3px 0 0;
  color: #445346;
  font-size: 12px;
  font-weight: 750;
  line-height: 1.55;
}

.atlas-story,
.atlas-safety {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 9px;
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
}

.atlas-story {
  background: #eef3dc;
  color: #3a684c;
}

.atlas-safety {
  background: #fff2d5;
  color: #8a6129;
}

.atlas-story strong {
  font-size: 11px;
}

.atlas-story p,
.atlas-safety p {
  margin: 3px 0 0;
  font-size: 11px;
  font-weight: 750;
  line-height: 1.55;
}

.atlas-detail__placeholder {
  display: grid;
  min-height: 100%;
  place-content: center;
  color: #6d654f;
  text-align: center;
}

.atlas-detail__placeholder img {
  width: 120px;
  max-height: 150px;
  margin: 0 auto 10px;
  object-fit: contain;
}

.atlas-detail__placeholder p {
  margin: 4px 0 0;
  font-size: 11px;
}

.atlas-empty {
  display: grid;
  min-height: 150px;
  padding: 24px;
  place-content: center;
  color: #716550;
  font-weight: 850;
  text-align: center;
}

.atlas-empty--error button {
  margin-top: 10px;
  border: 0;
  background: transparent;
  color: #2f7250;
  cursor: pointer;
  font: inherit;
  font-weight: 950;
}

@keyframes atlas-mask-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes atlas-book-in {
  from { opacity: 0; transform: translateY(12px) scale(0.985); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@media (max-width: 1300px) {
  .copper-shell {
    padding-right: 28px;
  }

  .copper-workspace {
    grid-template-columns: minmax(220px, 280px) minmax(420px, 1fr);
  }

  .right-rail {
    grid-column: 1 / -1;
    grid-template-columns: 1fr;
  }

  .search-panel {
    min-height: 420px;
  }

  .copper-tip {
    position: static;
    margin: 16px auto 0;
  }

  .atlas-grid {
    grid-template-columns: repeat(3, minmax(138px, 1fr));
  }

  .atlas-layout {
    grid-template-columns: minmax(0, 1fr) 310px;
  }
}

@media (max-width: 920px) {
  .copper-shell {
    padding: 18px 14px 24px;
  }

  .copper-hero,
  .copper-workspace,
  .right-rail {
    grid-template-columns: 1fr;
  }

  .back-button {
    width: 100%;
  }

  .detective-status {
    justify-self: start;
  }

  .hero-tools {
    width: min(100%, 320px);
    justify-self: start;
  }

  .model-stage,
  .model-viewer {
    min-height: 560px;
    height: 560px;
  }

  .stage-hint {
    display: none;
  }

  .viewer-status {
    right: 14px;
    left: 14px;
  }

  .atlas-mask {
    padding: 0;
  }

  .atlas-book {
    width: 100vw;
    height: 100dvh;
    border: 0;
    border-radius: 0;
  }

  .atlas-controls {
    grid-template-columns: minmax(0, 1fr) 180px;
  }

  .atlas-status-tabs {
    grid-column: 1 / -1;
    min-height: 40px;
  }

  .atlas-layout {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(280px, 1fr) auto;
  }

  .atlas-detail {
    max-height: 270px;
    border-top: 1px solid rgba(177, 129, 59, 0.2);
    border-left: 0;
  }

  .atlas-grid {
    grid-template-columns: repeat(3, minmax(120px, 1fr));
  }
}

@media (max-width: 560px) {
  .hero-copy {
    padding-right: 118px;
  }

  .hero-copy h1 {
    font-size: 28px;
    line-height: 1.14;
  }

  .hero-copy p {
    font-size: 14px;
    line-height: 1.55;
  }

  .hero-tools {
    width: 100%;
  }

  .collect-grid {
    grid-template-columns: 1fr;
  }

  .achievement-card {
    grid-template-columns: 1fr;
  }

  .achievement-card img {
    display: none;
  }

  .model-stage,
  .model-viewer {
    min-height: 500px;
    height: 500px;
  }

  .viewer-controls {
    right: 14px;
    bottom: 86px;
  }

  .atlas-header {
    padding: 16px 14px 11px;
  }

  .atlas-header h2 {
    font-size: 25px;
  }

  .atlas-header p {
    display: none;
  }

  .atlas-progress {
    margin: 9px 14px 0;
  }

  .atlas-progress__seal {
    width: 50px;
    height: 50px;
  }

  .atlas-controls {
    grid-template-columns: 1fr;
    padding: 10px 14px;
  }

  .atlas-status-tabs {
    grid-column: auto;
  }

  .atlas-catalog {
    padding: 12px 14px 18px;
  }

  .atlas-grid {
    grid-template-columns: repeat(2, minmax(118px, 1fr));
  }

  .atlas-card {
    min-height: 108px;
  }

  .atlas-detail {
    max-height: 245px;
    padding: 14px;
  }

  .welcome-mask,
  .case-complete-mask {
    padding: 12px;
  }

  .welcome-card {
    grid-template-columns: 1fr;
    max-height: calc(100vh - 24px);
    overflow-y: auto;
  }

  .welcome-visual {
    min-height: 210px;
  }

  .welcome-visual img {
    height: 190px;
  }

  .welcome-copy {
    padding: 26px 22px;
  }

  .welcome-copy h2 {
    font-size: 28px;
  }
}
.clue-knowledge--use { border-color: rgba(202, 157, 64, .52); background: #fff7dd; }
.atlas-story--use { border-color: #d7b15b; background: #fff8df; }
.atlas-story--use small { display: block; margin-top: 7px; color: #876b3a; font-size: 10px; line-height: 1.45; }
</style>

<template>
  <main class="body-map-page">
    <div class="body-map-shell">
      <section class="mission-banner" aria-labelledby="body-map-title">
        <div class="mission-copy">
          <p class="mission-kicker">身体地图追踪案 · 第四关</p>
          <h1 id="body-map-title">认识身体区域，把穴位星星送回家</h1>
          <p>跟着小铜人老师认识 8 个身体区域，把小铜人馆中已完成定位的 100 颗穴位星送回地图。这里只做中医文化科普和身体认知学习。</p>
        </div>
        <div class="guide-callout">
          <div class="guide-bubble">先认识区域，<br />再送星星回家哦！</div>
          <img :src="guideDetectiveImage" alt="挥手提示的绿色小侦探" />
        </div>
      </section>

      <section class="explorer-grid" aria-label="身体区域学习区">
        <div class="body-board">
          <div class="board-watermark" aria-hidden="true">身体星图</div>
          <div class="board-guide" role="status">
            <span class="board-guide-step">{{ selectedPoint ? '第 2 步' : '第 1 步' }}</span>
            <strong>{{ selectedPoint ? `把${selectedPoint.name}送回${regionName(selectedPoint.target)}` : '先选一张穴位星卡' }}</strong>
            <small>{{ selectedPoint ? '看清卡片线索后，点击地图上的对应区域' : '每张卡片都会告诉你要观察的身体区域' }}</small>
          </div>
          <div class="body-figures">
            <figure v-for="view in bodyViews" :key="view.id" class="body-figure">
              <div class="body-figure-stage" :data-body-view="view.id">
                <img class="body-child" :src="view.image" :alt="view.alt" />
                <button
                  v-for="region in view.regions"
                  :key="region.id"
                  type="button"
                  class="region-hotspot"
                  :class="{ active: activeRegion.id === region.id, done: learned.includes(region.id), target: selectedPoint?.target === region.id, muted: selectedPoint && selectedPoint.target !== region.id }"
                  :style="{ left: `${region.x}%`, top: `${region.y}%`, '--arrow-scale': region.reach === 'long' ? 2.4 : 1.55 }"
                  :aria-label="hotspotLabel(region)"
                  :aria-pressed="activeRegion.id === region.id"
                  @click="selectRegion(region)"
                >
                  <ArrowRightOutlined v-if="region.arrow === 'right'" class="hotspot-arrow hotspot-arrow--right" aria-hidden="true" />
                  <ArrowLeftOutlined v-else class="hotspot-arrow hotspot-arrow--left" aria-hidden="true" />
                  <span class="hotspot-medal"><StarFilled /></span>
                  <strong>{{ region.shortName }}</strong>
                </button>
              </div>
              <figcaption>{{ view.label }}</figcaption>
            </figure>
          </div>
        </div>

        <aside class="task-card" aria-live="polite">
          <p class="card-kicker">当前区域</p>
          <h2>{{ activeRegion.name }}</h2>
          <p class="region-description">{{ activeRegion.description }}</p>
          <div class="safety-note">
            <SafetyCertificateFilled aria-hidden="true" />
            <div><strong>安全小提醒</strong><span>{{ activeRegion.safety }}</span></div>
          </div>
          <button type="button" class="primary-action" :disabled="learned.includes(activeRegion.id)" @click="markLearned(activeRegion)">
            <StarFilled aria-hidden="true" />
            {{ learned.includes(activeRegion.id) ? '这个区域已点亮' : '点亮这个区域' }}
          </button>
          <div class="progress-block">
            <div class="progress-row"><span>学习进度</span><strong>{{ visualProgress }} / 10</strong></div>
            <div class="progress-track" role="progressbar" aria-label="身体地图学习进度" :aria-valuenow="visualProgress" aria-valuemin="0" aria-valuemax="10">
              <span :style="{ width: `${visualProgress * 10}%` }"></span>
            </div>
          </div>
          <div class="practice-count"><span>百穴练习</span><strong>{{ placed.length }} / {{ points.length || 100 }}</strong></div>
          <div class="reward-block">
            <span class="reward-title">完成可获得</span>
            <div class="reward-list">
              <div v-for="reward in rewards" :key="reward.name" class="reward-item">
                <img :src="reward.image" :alt="reward.name" /><span>{{ reward.name }}</span>
              </div>
            </div>
          </div>
        </aside>
      </section>

      <section class="return-game" aria-labelledby="return-game-title">
        <div class="game-heading">
          <div><p class="game-kicker">百穴归位小游戏</p><h2 id="return-game-title">选一颗穴位星，再判断它属于哪个身体区域</h2></div>
          <p class="game-tip" role="status">{{ gameTip }}</p>
        </div>
        <div v-if="selectedPoint" class="step-guide" role="status">
          <span class="step-guide-index">1</span><span>读卡片上的“归位提示”</span><span class="step-guide-arrow">→</span><span class="step-guide-index">2</span><span>点击地图的“{{ regionName(selectedPoint.target) }}”</span>
        </div>
        <div class="game-filters" aria-label="穴位筛选">
          <label><span>经络</span><select v-model="meridianFilter"><option value="all">全部经络</option><option v-for="item in meridianOptions" :key="item.code" :value="item.code">{{ item.name }}</option></select></label>
          <label><span>身体区域</span><select v-model="areaFilter"><option value="all">全部区域</option><option v-for="region in regions" :key="region.id" :value="region.id">{{ region.name }}</option></select></label>
          <button type="button" class="change-round" :disabled="filteredPoints.length <= roundSize" @click="changeRound"><ReloadOutlined />换一组</button>
          <span class="result-count">{{ filteredPoints.length }} 颗可练习</span>
        </div>
        <div v-if="loading" class="game-empty" role="status">小铜人正在整理 100 颗穴位星……</div>
        <div v-else-if="loadError" class="game-empty game-empty--error" role="alert"><span>{{ loadError }}</span><button type="button" @click="loadPoints">重新载入</button></div>
        <div class="cards-wrap">
          <div class="point-grid">
            <button
              v-for="point in roundPoints"
              :key="point.code"
              type="button"
              class="point-card"
              :class="[`point-card--${point.tone}`, { selected: selectedPoint?.code === point.code, placed: placed.includes(point.code) }]"
              :disabled="placed.includes(point.code)"
              :aria-pressed="selectedPoint?.code === point.code"
              @click="selectPoint(point)"
            >
              <span class="card-state"><CheckCircleFilled v-if="placed.includes(point.code)" /><StarFilled v-else /></span>
              <img v-if="point.image" :src="point.image" :alt="`${point.name}穴位卡插图`" />
              <span v-else class="point-emblem" aria-hidden="true"><StarFilled /><small>{{ point.meridianCode }}</small></span>
              <span class="point-copy"><strong>{{ point.name }}</strong><small>{{ point.code }} · {{ regionName(point.target) }}</small></span>
              <span class="point-guide"><b>归位提示</b>{{ point.placementHint || fallbackPointGuide(point) }}</span>
              <span class="card-stars" aria-hidden="true"><StarFilled v-for="index in 5" :key="index" :class="{ lit: index === 1 || placed.includes(point.code) }" /></span>
            </button>
            <div v-if="!roundPoints.length && !loading && !loadError" class="game-empty">当前筛选下没有可练习的穴位星。</div>
          </div>
        </div>
      </section>
    </div>

    <Teleport to="body">
      <div v-if="knowledgePoint" class="knowledge-mask" role="dialog" aria-modal="true" aria-labelledby="knowledge-title">
        <section class="knowledge-card">
          <header><span><CheckCircleFilled />归位正确</span><strong>{{ knowledgePoint.code }}</strong></header>
          <div class="knowledge-title"><span class="knowledge-seal"><StarFilled /></span><div><h2 id="knowledge-title">{{ knowledgePoint.name }}</h2><p>{{ knowledgePoint.pinyin }} · {{ knowledgePoint.meridianName }}</p></div></div>
          <dl>
            <div><dt>在身体地图怎么归位</dt><dd>{{ knowledgePoint.placementHint }}</dd></div>
            <div><dt>身体地图知识</dt><dd>{{ knowledgePoint.childMapDescription }}</dd></div>
            <div class="knowledge-use"><dt>传统用途</dt><dd>{{ knowledgePoint.childTraditionalUse }}</dd></div>
            <div><dt>文化记忆</dt><dd>{{ knowledgePoint.childDescription }}</dd></div>
          </dl>
          <div class="knowledge-safety"><SafetyCertificateFilled /><p>{{ knowledgePoint.safetyTip }}</p></div>
          <small v-if="knowledgePoint.traditionalUseSourceName" class="knowledge-source">资料：{{ knowledgePoint.traditionalUseSourceName }}</small>
          <button type="button" class="knowledge-next" autofocus @click="closeKnowledge">记住了，继续探案</button>
        </section>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, ArrowRightOutlined, CheckCircleFilled, ReloadOutlined, SafetyCertificateFilled, StarFilled } from '@ant-design/icons-vue'
import { listBodyMapAcupoints } from '@/api/AcupunctureApi'
import { useGameState } from '@/composables/useGameState'
import { useUserStore } from '@/store/user'
import guideDetectiveImage from '@/assets/身体地图/绿色小侦探引导角色-透明.png'
import bodyImage from '@/assets/maps/body-map-navigation-child-clean.png'
import bodyBackImage from '@/assets/maps/body-map-navigation-child-back.png'
import heguImage from '@/assets/身体地图/合谷穴位卡插画-透明.png'
import zhongwanImage from '@/assets/身体地图/中脘穴位卡插画-透明.png'
import zusanliImage from '@/assets/身体地图/足三里穴位卡插画-透明.png'
import { generatedMaterialIcons } from '@/data/generatedRewardAssets'

const regions = [
  { id: 'head', view: 'front', arrow: 'left', name: '头面与颈部', shortName: '头面', x: 88, y: 27, description: '认识头面、口鼻和颈部在身体地图上的大致区域。', safety: '这里只在图片和小铜人模型上观察，不在自己或同学身上寻找。' },
  { id: 'chest', view: 'front', arrow: 'right', reach: 'long', name: '胸部', shortName: '胸部', x: 9, y: 43, description: '胸部位于身体正面上方，是呼吸文化知识常出现的区域。', safety: '胸部不舒服时要马上告诉家长、老师或医生。' },
  { id: 'belly', view: 'front', arrow: 'left', reach: 'long', name: '腹部', shortName: '腹部', x: 90, y: 55, description: '腹部在胸部下方，传统文化常把它和饮食、消化等身体话题联系起来。', safety: '肚子不舒服时不要自己尝试任何操作，要告诉大人。' },
  { id: 'shoulder', view: 'back', arrow: 'right', reach: 'long', name: '肩臂部', shortName: '肩臂', x: 10, y: 43, description: '从肩膀到上臂属于肩臂部，是上肢地图的一部分。', safety: '只认区域，不拉扯、按压自己或同学的手臂。' },
  { id: 'forearm', view: 'back', arrow: 'left', name: '前臂与肘部', shortName: '肘臂', x: 92, y: 55, description: '肘部和肘部到手腕之间的部分叫前臂。', safety: '活动手肘要轻柔，疼痛时及时告诉大人。' },
  { id: 'wrist', view: 'front', arrow: 'right', name: '手腕部', shortName: '手腕', x: 7, y: 58, description: '手腕部包括手腕、手掌和手指附近的身体地图区域。', safety: '只看模型上的星点，不照着图片在手上按揉。' },
  { id: 'leg', view: 'back', arrow: 'left', reach: 'long', name: '髋腿部', shortName: '髋腿', x: 92, y: 72, description: '从髋部、大腿到小腿都属于腿部地图。', safety: '腿部不舒服或活动困难时，要请大人帮助。' },
  { id: 'ankle', view: 'front', arrow: 'left', reach: 'long', name: '踝足部', shortName: '踝足', x: 88, y: 88, description: '脚踝、脚背和脚趾附近属于踝足部。', safety: '只做观察学习，不在脚上模仿取穴或刺激。' }
]
const bodyViews = [
  { id: 'front', label: '正面', image: bodyImage, alt: '正面站立的儿童身体地图', regions: regions.filter((region) => region.view === 'front') },
  { id: 'back', label: '背面', image: bodyBackImage, alt: '背面站立的儿童身体地图', regions: regions.filter((region) => region.view === 'back') }
]
const areaTargets = { '头面与颈部': 'head', '胸部': 'chest', '腹部': 'belly', '肩臂部': 'shoulder', '前臂与肘部': 'forearm', '手腕部': 'wrist', '髋腿部': 'leg', '踝足部': 'ankle' }
const illustrations = { 合谷: heguImage, 中脘: zhongwanImage, 足三里: zusanliImage }
const tones = { LU: 'green', LI: 'blue', ST: 'orange', PC: 'purple', CV: 'red' }
const fallbackUse = { head: '口鼻、面部和头颈舒适', chest: '呼吸和胸部舒适', belly: '饮食、消化和腹部舒适', shoulder: '肩臂活动和上肢舒适', forearm: '手臂活动和肘部舒适', wrist: '手腕、手部活动和身体舒适', leg: '腿部活动和腹部舒适', ankle: '脚踝、足部活动和身体舒适' }
const legacyCodes = { baihui: 'GV-20', hegu: 'LI-4', zhongwan: 'CV-12', feishu: 'BL-13', zusanli: 'ST-36' }

const userStore = useUserStore()
const { recordTaskProgress, getTaskProgress } = useGameState()
const starRewardImage = generatedMaterialIcons['acupoint-star-pearl']
const tokenRewardImage = generatedMaterialIcons['copper-token']
const safetyRewardImage = generatedMaterialIcons['safety-bell']
const selectedPoint = ref(null)
const knowledgePoint = ref(null)
const points = ref([])
const learned = ref([])
const placed = ref([])
const hydrated = ref(false)
const loading = ref(false)
const loadError = ref('')
const meridianFilter = ref('all')
const areaFilter = ref('all')
const roundOffset = ref(0)
const roundSize = 4
const activeRegion = ref(regions[0])
const rewards = [
  { name: '穴位星珠', image: starRewardImage },
  { name: '铜片', image: tokenRewardImage },
  { name: '安全铃铛', image: safetyRewardImage }
]

const bodyProgress = computed(() => Math.min(10, getTaskProgress('main-hand-star-map')))
const visualProgress = computed(() => Math.min(10, Math.max(bodyProgress.value, learned.value.length + placed.value.length)))
const storageKey = computed(() => `xinglin-body-map-ui-v2:${userStore.userId || 'guest'}`)
const legacyStorageKey = computed(() => `xinglin-body-map-ui-v1:${userStore.userId || 'guest'}`)
const meridianOptions = computed(() => {
  const names = new Map()
  points.value.forEach((point) => names.set(point.meridianCode, point.meridianName))
  return [...names].map(([code, name]) => ({ code, name }))
})
const filteredPoints = computed(() => points.value.filter((point) =>
  (meridianFilter.value === 'all' || point.meridianCode === meridianFilter.value) &&
  (areaFilter.value === 'all' || point.target === areaFilter.value)
))
const roundPoints = computed(() => {
  if (filteredPoints.value.length <= roundSize) return filteredPoints.value
  return Array.from({ length: roundSize }, (_, index) => filteredPoints.value[(roundOffset.value + index) % filteredPoints.value.length])
})
const gameTip = computed(() => {
  if (points.value.length && placed.value.length >= points.value.length) return '100 颗穴位星都归位了！'
  if (selectedPoint.value) return `读“${selectedPoint.value.name}”卡片上的归位提示，再点击${regionName(selectedPoint.value.target)}`
  return '先选一张星卡，再判断它属于哪个身体区域'
})

watch(storageKey, loadUiState, { immediate: true })
watch([learned, placed], saveUiState, { deep: true })
watch([meridianFilter, areaFilter], () => { roundOffset.value = 0; selectedPoint.value = null })
onMounted(loadPoints)

async function loadPoints() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await listBodyMapAcupoints()
    points.value = (Array.isArray(data) ? data : [])
      .map((point) => {
        const target = point.regionCode || areaTargets[point.bodyArea]
        return {
          ...point,
          target,
          tone: tones[point.meridianCode] || 'green',
          image: illustrations[point.name] || '',
          childTraditionalUse: point.childTraditionalUse || `传统认识中，“${point.name}”常与${fallbackUse[target] || '身体舒适与经络文化'}等身体话题相关。这里只了解传统文化，不用它判断或处理身体不舒服。`
        }
      })
      .filter((point) => point.target)
    if (points.value.length !== 100) throw new Error(`当前只载入 ${points.value.length} 颗可定位穴位星，请检查内容中心数据。`)
  } catch (error) {
    loadError.value = error?.message || '穴位星载入失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function loadUiState() {
  hydrated.value = false
  const regionIds = new Set(regions.map((region) => region.id))
  try {
    const current = JSON.parse(window.localStorage.getItem(storageKey.value) || 'null')
    const legacy = current || JSON.parse(window.localStorage.getItem(legacyStorageKey.value) || '{}')
    const learnedAliases = { hand: 'wrist', leg: 'leg', belly: 'belly', head: 'head' }
    learned.value = Array.isArray(legacy.learnedRegionIds)
      ? [...new Set(legacy.learnedRegionIds.map((id) => learnedAliases[id] || id).filter((id) => regionIds.has(id)))]
      : []
    const stored = Array.isArray(legacy.placedPointCodes) ? legacy.placedPointCodes : legacy.placedPointIds
    placed.value = Array.isArray(stored) ? [...new Set(stored.map((id) => legacyCodes[id] || id))] : []
  } catch {
    learned.value = []
    placed.value = []
  }
  nextTick(() => { hydrated.value = true })
}

function saveUiState() {
  if (!hydrated.value || typeof window === 'undefined') return
  window.localStorage.setItem(storageKey.value, JSON.stringify({ learnedRegionIds: learned.value, placedPointCodes: placed.value }))
}

function hotspotLabel(region) {
  if (selectedPoint.value) return `把${selectedPoint.value.name}送到${region.name}`
  if (learned.value.includes(region.id)) return `${region.name}已点亮`
  return `认识并选择${region.name}`
}

function selectPoint(point) {
  selectedPoint.value = point
  message.info(`先读${point.name}卡片上的“归位提示”，再点击地图区域。`)
}

async function selectRegion(region) {
  activeRegion.value = region
  if (!selectedPoint.value) return
  if (selectedPoint.value.target !== region.id) {
    message.info(`再读一遍“归位提示”：${selectedPoint.value.name}要送到${regionName(selectedPoint.value.target)}。`)
    return
  }
  if (placed.value.includes(selectedPoint.value.code)) return
  const point = selectedPoint.value
  if (bodyProgress.value < 10) {
    const result = await recordTaskProgress('main-hand-star-map', 1, { resultCode: 'BODY_POINT_COMPLETE', idempotencyKey: `body:place:${point.code}` })
    if (!result.success) {
      message.error('进度暂时未保存，请重试')
      return
    }
  }
  placed.value = [...placed.value, point.code]
  selectedPoint.value = null
  knowledgePoint.value = point
}

async function markLearned(region) {
  if (learned.value.includes(region.id)) return
  if (bodyProgress.value < 10) {
    const result = await recordTaskProgress('main-hand-star-map', 1, { resultCode: 'BODY_REGION_COMPLETE', idempotencyKey: `body:learn:${region.id}` })
    if (!result.success) {
      message.error('学习进度暂时未保存，请重试')
      return
    }
  }
  learned.value = [...learned.value, region.id]
  message.success(`${region.name}的星星亮起来了！`)
}

function closeKnowledge() { knowledgePoint.value = null }
function changeRound() { roundOffset.value = (roundOffset.value + roundSize) % filteredPoints.value.length; selectedPoint.value = null }
function regionName(id) { return regions.find((region) => region.id === id)?.name || '' }
function fallbackPointGuide(point) {
  const view = regions.find((region) => region.id === point.target)?.view === 'back' ? '右边的背面人物' : '左边的正面人物'
  return `先看${view}，再点击“${regionName(point.target)}”圆圈，沿着箭头把星星送回这个区域。`
}
</script>

<style scoped>
.body-map-page{--forest:#155b49;--forest-dark:#0d4638;--gold:#d69523;--paper:#fff5d9;--ink:#492b13;min-height:calc(100vh - 92px);padding:14px clamp(12px,2.2vw,36px) 18px;color:var(--ink);background-color:#c89a54;background-image:url('@/assets/身体地图/杏林探案书房背景.png');background-position:center;background-size:cover;font-family:var(--site-body-font),"Microsoft YaHei",sans-serif}
.body-map-shell{width:min(1400px,100%);margin:0 auto}
.mission-banner,.body-board,.task-card,.return-game{border:2px solid var(--gold);background:rgba(255,246,216,.96);box-shadow:0 10px 0 rgba(93,53,14,.16),0 18px 35px rgba(64,37,12,.2)}
.mission-banner{display:grid;min-height:128px;grid-template-columns:minmax(0,1fr) 260px;align-items:center;overflow:hidden;padding:16px 28px 14px 34px;border-radius:26px 26px 20px 20px;outline:6px solid rgba(20,91,73,.88);outline-offset:-10px}
.mission-kicker,.card-kicker,.game-kicker{margin:0;color:#9a5d16;font-size:12px;font-weight:900;letter-spacing:.12em}
.mission-copy h1{margin:2px 0 5px;color:#40240f;font-family:var(--site-title-font),"Microsoft YaHei",serif;font-size:clamp(29px,2.65vw,42px);font-weight:950;line-height:1.18}
.mission-copy>p:last-child{max-width:790px;margin:0;color:#6d4e2c;font-size:14px;line-height:1.55}
.guide-callout{position:relative;display:flex;height:112px;align-items:flex-end;justify-content:flex-end}.guide-callout img{width:112px;height:112px;object-fit:contain;filter:drop-shadow(0 8px 8px rgba(88,47,12,.18))}.guide-bubble{align-self:center;margin-right:-8px;padding:10px 14px;color:#69441d;font-size:13px;font-weight:800;line-height:1.45;border:1px solid #d9a44a;border-radius:18px;background:#fff9e9}
.explorer-grid{display:grid;height:620px;grid-template-columns:minmax(0,1fr) 300px;gap:12px;margin-top:12px}.body-board,.task-card{border-radius:24px}
.body-board{position:relative;display:grid;overflow:hidden;place-items:center;background-color:rgba(255,248,225,.94);background-image:url('@/assets/maps/xinglin-detective-map-bg.png');background-position:center;background-size:cover}.body-board::after{position:absolute;inset:12px;content:"";pointer-events:none;border:2px solid rgba(21,91,73,.42);border-radius:17px}.board-watermark{position:absolute;z-index:3;top:24px;left:30px;color:rgba(128,80,26,.33);font-family:var(--site-title-font),serif;font-size:18px;font-weight:900;letter-spacing:.18em}.body-figures{position:absolute;z-index:2;inset:12px 18px;display:grid;grid-template-columns:repeat(2,minmax(0,384px));justify-content:center;gap:clamp(8px,2vw,28px)}.body-figure{display:grid;min-width:0;height:100%;grid-template-rows:minmax(0,1fr) 28px;justify-items:center;margin:0}.body-figure-stage{position:relative;width:min(100%,384px);aspect-ratio:2/3;align-self:end}.body-child{position:absolute;z-index:1;inset:0;width:100%;height:100%;object-fit:contain;filter:drop-shadow(0 13px 10px rgba(86,52,20,.18));pointer-events:none}.body-figure figcaption{position:relative;z-index:3;align-self:center;padding:2px 14px;color:#69451e;font-size:12px;font-weight:950;border:1px solid rgba(181,125,38,.5);border-radius:999px;background:rgba(255,249,226,.94)}
.region-hotspot{position:absolute;z-index:3;display:grid;min-width:60px;padding:0;place-items:center;color:#3f2915;font-size:12px;font-weight:900;border:0;background:transparent;cursor:pointer;transform:translate(-50%,-50%)}.hotspot-medal{position:relative;z-index:1;display:grid;width:46px;height:46px;place-items:center;color:#fff6ba;font-size:20px;border:3px solid #f4c657;border-radius:50%;background:var(--forest);box-shadow:0 0 0 2px #895817,0 5px 10px rgba(72,41,12,.25);transition:transform .18s ease,box-shadow .18s ease}.region-hotspot strong{position:relative;z-index:1;margin-top:3px;padding:1px 6px;border-radius:999px;background:rgba(255,248,224,.94)}.hotspot-arrow{position:absolute;top:7px;z-index:0;color:#9d651b;font-size:32px;filter:drop-shadow(0 1px 0 #fff7d8);pointer-events:none}.hotspot-arrow--right{left:calc(50% + 25px);transform:scaleX(var(--arrow-scale));transform-origin:left center}.hotspot-arrow--left{right:calc(50% + 25px);transform:scaleX(var(--arrow-scale));transform-origin:right center}
.region-hotspot:hover .hotspot-medal,.region-hotspot:focus-visible .hotspot-medal,.region-hotspot.active .hotspot-medal{transform:translateY(-3px) scale(1.06);box-shadow:0 0 0 4px #895817,0 0 0 9px rgba(245,194,67,.28),0 9px 14px rgba(72,41,12,.3)}.region-hotspot:focus-visible{outline:3px solid #1685c6;outline-offset:5px;border-radius:14px}.region-hotspot.done .hotspot-medal{color:#fff;background:#3c9a61}.region-hotspot.target .hotspot-medal{animation:targetPulse 1.25s ease-in-out infinite}
.region-hotspot.muted{opacity:.34;filter:saturate(.55)}
.task-card{padding:25px 26px 18px;outline:4px solid rgba(128,80,26,.14);outline-offset:-10px}.task-card h2{margin:2px 0 6px;color:#40240f;font-family:var(--site-title-font),serif;font-size:29px;line-height:1.2}.region-description{min-height:58px;margin:0;color:#725536;font-size:13px;line-height:1.55}.safety-note{display:grid;grid-template-columns:38px 1fr;gap:10px;align-items:center;margin:12px 0;padding:9px 11px;color:#1f624b;border:1px solid #9ab895;border-radius:14px;background:#e8f2d9}.safety-note>svg{font-size:34px}.safety-note strong,.safety-note span{display:block}.safety-note strong{font-size:13px}.safety-note span{margin-top:1px;font-size:11px;line-height:1.45}
.primary-action{display:flex;width:100%;min-height:47px;align-items:center;justify-content:center;gap:9px;color:#fff8d9;font-size:17px;font-weight:900;border:2px solid #d9ae4b;border-radius:999px;background:var(--forest);box-shadow:inset 0 -4px 0 rgba(5,48,37,.44),0 5px 0 #8e621f;cursor:pointer}.primary-action:hover:not(:disabled){background:var(--forest-dark)}.primary-action:focus-visible{outline:3px solid #1685c6;outline-offset:3px}.primary-action:disabled{opacity:.68;cursor:default;box-shadow:inset 0 -3px 0 rgba(5,48,37,.25)}
.progress-block{margin-top:15px}.progress-row{display:flex;justify-content:space-between;font-size:12px;font-weight:900}.progress-track{height:13px;margin-top:5px;overflow:hidden;border:1px solid #d7b36f;border-radius:999px;background:#eeddb9}.progress-track span{display:block;height:100%;border-radius:inherit;background:#33ad4a;transition:width .3s ease}.reward-block{margin-top:13px}.reward-title{display:block;margin-bottom:5px;color:#8a642f;font-size:11px;font-weight:800}.reward-list{display:grid;grid-template-columns:repeat(3,1fr);gap:7px}.reward-item{min-width:0;text-align:center;color:#725536;font-size:10px;font-weight:800}.reward-item img{display:block;width:44px;height:44px;margin:0 auto 3px;object-fit:contain}
.return-game{position:relative;z-index:5;width:calc(100% - 328px);min-height:190px;margin-top:-102px;padding:15px 22px 14px;border-radius:22px}.game-heading{display:flex;align-items:center;justify-content:space-between;gap:16px}.game-heading h2{margin:1px 0 0;font-family:var(--site-title-font),serif;font-size:18px}.game-tip{margin:0;color:#826139;font-size:11px;font-weight:800}.cards-wrap{position:relative;margin-top:9px;padding:0 30px}.point-grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:9px;overflow-x:auto;scrollbar-width:none;scroll-snap-type:x mandatory}.point-grid::-webkit-scrollbar{display:none}
.point-card{position:relative;display:grid;min-width:0;height:118px;grid-template-columns:54px 1fr;grid-template-rows:1fr auto;align-items:center;gap:0 6px;overflow:hidden;padding:8px 7px 6px;color:#3f2a15;text-align:left;border:2px solid currentColor;border-radius:15px;background:#edf4d3;box-shadow:inset 0 -5px 0 rgba(56,43,20,.13),0 5px 9px rgba(75,48,17,.17);cursor:pointer;scroll-snap-align:start;transition:transform .16s ease,box-shadow .16s ease}.point-card--green{color:#28623e;background:#e4f0c9}.point-card--blue{color:#315f85;background:#dcecf6}.point-card--orange{color:#99501a;background:#ffe4bc}.point-card--purple{color:#66448d;background:#ead9f5}.point-card--red{color:#98422e;background:#f8d7ca}.point-card:hover:not(:disabled),.point-card:focus-visible,.point-card.selected{transform:translateY(-3px);box-shadow:inset 0 -5px 0 rgba(56,43,20,.13),0 0 0 4px rgba(255,194,47,.58),0 8px 12px rgba(75,48,17,.2)}.point-card:focus-visible{outline:3px solid #1685c6;outline-offset:2px}.point-card.placed{opacity:.83;cursor:default}.point-card img{width:52px;height:76px;object-fit:contain;filter:drop-shadow(0 4px 4px rgba(72,44,15,.18))}.point-copy{min-width:0}.point-copy strong{display:block;font-size:20px;line-height:1.15;white-space:nowrap}.point-copy small{display:inline-block;margin-top:5px;padding:1px 7px;color:inherit;font-size:10px;border-radius:999px;background:rgba(255,255,255,.52)}.card-state{position:absolute;top:5px;right:6px;color:#d89516;font-size:14px}.card-stars{display:flex;grid-column:1/-1;justify-content:center;gap:3px;color:rgba(60,52,38,.25);font-size:11px}.card-stars .lit{color:#e9a814}
.rail-arrow{position:absolute;top:50%;z-index:2;display:grid;width:29px;height:40px;place-items:center;color:#fff3cb;font-size:15px;border:2px solid #d7a646;border-radius:999px;background:#8b581c;cursor:pointer;transform:translateY(-50%)}.rail-arrow--left{left:-2px}.rail-arrow--right{right:-2px}.rail-arrow:focus-visible{outline:3px solid #1685c6;outline-offset:2px}
@keyframes targetPulse{0%,100%{transform:scale(1)}50%{transform:scale(1.12)}}
@media(max-width:1100px){.mission-banner{grid-template-columns:1fr 220px}.explorer-grid{grid-template-columns:minmax(0,1fr) 280px}.return-game{width:calc(100% - 292px)}.point-card{min-width:145px}.point-grid{grid-template-columns:repeat(5,145px)}}
@media(max-width:820px){.body-map-page{min-height:calc(100vh - 76px);padding:12px}.mission-banner{grid-template-columns:1fr 132px;padding:18px 20px}.mission-copy h1{font-size:29px}.guide-bubble{display:none}.guide-callout img{width:100px;height:100px}.explorer-grid{height:auto;grid-template-columns:1fr}.body-board{min-height:620px}.task-card{padding:24px}.return-game{width:100%;margin-top:12px}.point-grid{grid-template-columns:repeat(5,170px)}}
@media(max-width:520px){.mission-banner{min-height:0;grid-template-columns:1fr 82px;padding:15px 14px;border-radius:18px;outline-width:4px;outline-offset:-7px}.mission-kicker{font-size:10px}.mission-copy h1{margin-top:4px;font-size:23px}.mission-copy>p:last-child{font-size:12px}.guide-callout{height:86px}.guide-callout img{width:80px;height:80px}.body-board{min-height:1120px}.body-figures{inset:54px 10px 12px;grid-template-columns:minmax(0,360px);grid-template-rows:repeat(2,minmax(0,1fr));gap:10px}.body-figure{grid-template-rows:minmax(0,1fr) 24px}.body-figure-stage{width:min(92%,344px)}.region-hotspot{min-width:50px}.hotspot-medal{width:38px;height:38px;font-size:16px;border-width:2px}.region-hotspot strong{font-size:10px}.task-card h2{font-size:25px}.game-heading{align-items:flex-start;flex-direction:column;gap:4px}.cards-wrap{padding:0 27px}.point-grid{grid-template-columns:repeat(5,162px)}}
@media(prefers-reduced-motion:reduce){*,*::before,*::after{scroll-behavior:auto!important;animation:none!important;transition:none!important}}
.body-map-page { min-height: calc(100vh - var(--site-nav-height)); }

/* Child-first guidance layer: keep the adventure setting, reduce visual noise. */
.body-board{background-image:linear-gradient(rgba(255,248,225,.38),rgba(255,248,225,.38)),url('@/assets/maps/xinglin-detective-map-bg.png');box-shadow:0 6px 14px rgba(75,48,17,.14)}
.body-board::after{border-color:rgba(21,91,73,.25)}
.board-guide{position:absolute;z-index:4;top:14px;right:14px;display:grid;max-width:230px;gap:2px;padding:9px 11px;color:#31513f;border:1px solid #9fbd91;border-radius:10px;background:rgba(246,252,231,.96);box-shadow:0 3px 8px rgba(72,44,15,.12)}
.board-guide-step{color:#9a6417;font-size:10px;font-weight:950}.board-guide strong{font-size:13px;line-height:1.3}.board-guide small{font-size:10px;line-height:1.4}
.point-card{height:146px;padding:8px 9px 7px;box-shadow:0 3px 7px rgba(75,48,17,.12)}
.point-guide{grid-column:1/-1;display:block;min-height:31px;margin-top:2px;padding:4px 6px;overflow:hidden;color:#5f5138;font-size:10px;line-height:1.35;border-radius:6px;background:rgba(255,255,255,.62)}
.point-guide b{display:block;margin-bottom:1px;color:#9a6417;font-size:9px;font-weight:950}
.step-guide{display:flex;align-items:center;gap:7px;margin-top:9px;padding:7px 10px;color:#31513f;font-size:11px;font-weight:800;border:1px solid #a8c298;border-radius:8px;background:#eef6e1}.step-guide-index{display:grid;width:20px;height:20px;place-items:center;color:#fff;border-radius:50%;background:#2d765d;font-size:11px}.step-guide-arrow{color:#b17a1e;font-size:15px}
.return-game{box-shadow:0 6px 14px rgba(75,48,17,.14)}

@media(max-width:820px) {
  .body-map-page { min-height: calc(100vh - var(--site-nav-height)); }
}
.return-game{width:100%;min-height:238px;margin-top:12px}.game-filters{display:flex;align-items:end;gap:10px;margin-top:10px;padding:9px 10px;border:1px solid rgba(163,113,34,.24);border-radius:8px;background:rgba(255,252,236,.72)}.game-filters label{display:grid;gap:3px;color:#80591f;font-size:10px;font-weight:900}.game-filters select{min-width:145px;height:32px;padding:0 28px 0 9px;color:#3f3528;border:1px solid #cfa65b;border-radius:7px;background:#fffdf6;font:700 12px var(--site-body-font),sans-serif}.change-round{display:inline-flex;height:32px;align-items:center;gap:6px;padding:0 12px;color:#fff7d7;border:1px solid #d4a440;border-radius:7px;background:var(--forest);font-weight:900;cursor:pointer}.change-round:disabled{opacity:.45;cursor:default}.result-count{margin-left:auto;color:#765526;font-size:11px;font-weight:900}.game-empty{display:flex;min-height:106px;align-items:center;justify-content:center;gap:10px;color:#755b35;font-weight:800}.game-empty--error{color:#973e2e}.game-empty button{padding:6px 10px;color:#fff;border:0;border-radius:6px;background:var(--forest);font-weight:800}.point-emblem{display:grid;width:52px;height:52px;place-items:center;align-self:center;color:#fff0ac;border:3px solid #d5a43a;border-radius:50%;background:var(--forest);box-shadow:0 4px 0 #87581e}.point-emblem svg{font-size:20px}.point-emblem small{font-size:9px;font-weight:950}.practice-count{display:flex;justify-content:space-between;margin-top:9px;padding-top:9px;color:#765526;border-top:1px dashed #d1aa62;font-size:11px;font-weight:900}.point-copy small{max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.knowledge-mask{position:fixed;z-index:2200;inset:0;display:grid;padding:20px;place-items:center;background:rgba(34,28,17,.72);backdrop-filter:blur(3px)}.knowledge-card{width:min(620px,100%);max-height:min(760px,calc(100vh - 32px));overflow-y:auto;padding:22px;color:#49341b;border:3px solid #d7a846;border-radius:8px;background:#fff8df;box-shadow:0 0 0 7px #175746,0 24px 60px rgba(23,18,9,.4)}.knowledge-card header{display:flex;align-items:center;justify-content:space-between;color:#175746;font-size:13px;font-weight:950}.knowledge-card header span{display:flex;align-items:center;gap:7px}.knowledge-card header strong{color:#9b681b}.knowledge-title{display:flex;align-items:center;gap:14px;margin:15px 0;padding-bottom:14px;border-bottom:1px solid #dec383}.knowledge-seal{display:grid;width:58px;height:58px;flex:0 0 58px;place-items:center;color:#fff0a9;font-size:25px;border:4px solid #d6a443;border-radius:50%;background:#165846}.knowledge-title h2{margin:0;color:#3f250f;font-family:var(--site-title-font),serif;font-size:32px}.knowledge-title p{margin:3px 0 0;color:#806035;font-size:12px;font-weight:800}.knowledge-card dl{display:grid;gap:9px;margin:0}.knowledge-card dl>div{padding:11px 13px;border-left:4px solid #c99a3b;background:#fffdf3}.knowledge-card dt{color:#8c5c18;font-size:11px;font-weight:950}.knowledge-card dd{margin:4px 0 0;line-height:1.65}.knowledge-card .knowledge-use{border-left-color:#28745e;background:#edf5e5}.knowledge-safety{display:flex;gap:10px;align-items:center;margin-top:12px;padding:10px 12px;color:#1d5e4b;border:1px solid #9bbd9d;background:#e8f2dd}.knowledge-safety svg{flex:0 0 auto;font-size:24px}.knowledge-safety p{margin:0;line-height:1.5}.knowledge-source{display:block;margin-top:8px;color:#8a7350;font-size:9px;line-height:1.45}.knowledge-next{display:block;width:100%;min-height:46px;margin-top:14px;color:#fff7d8;border:2px solid #d2a33e;border-radius:8px;background:#155b49;font-size:15px;font-weight:950;cursor:pointer}.knowledge-next:focus-visible,.change-round:focus-visible,.game-filters select:focus-visible{outline:3px solid #1685c6;outline-offset:2px}
@media(max-width:820px){.game-filters{align-items:stretch;flex-wrap:wrap}.game-filters label{flex:1 1 180px}.game-filters select{width:100%}.result-count{display:flex;align-items:center;margin-left:0}.return-game{margin-top:12px}.explorer-grid{height:auto}.body-board{min-height:620px}}
@media(max-width:520px){.game-filters{display:grid;grid-template-columns:1fr 1fr}.game-filters label{min-width:0}.game-filters select{min-width:0}.result-count{justify-content:flex-end}.point-card{grid-template-columns:48px 1fr;height:154px}.point-emblem{width:46px;height:46px}.knowledge-mask{padding:12px}.knowledge-card{padding:17px}.knowledge-title h2{font-size:27px}.body-board{min-height:1120px}.board-guide{top:10px;right:10px;max-width:180px}.board-guide strong{font-size:11px}.board-guide small{font-size:9px}.step-guide{align-items:flex-start;flex-wrap:wrap;gap:5px;font-size:10px}}

/* Two cards per row gives each child's placement hint room to breathe. */
.cards-wrap{padding:0}
.point-grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:14px;overflow:visible;scroll-snap-type:none}
.point-card{height:166px;grid-template-columns:70px minmax(0,1fr);padding:13px 15px 10px;gap:2px 12px}
.point-card img{width:66px;height:90px}
.point-emblem{width:62px;height:62px}
.point-emblem svg{font-size:24px}
.point-emblem small{font-size:11px}
.point-copy strong{font-size:24px}
.point-copy small{font-size:12px}
.point-guide{min-height:52px;padding:7px 9px;font-size:13px;line-height:1.55}
.point-guide b{font-size:11px}
.card-stars{font-size:13px}
@media(max-width:620px){.point-grid{grid-template-columns:minmax(0,1fr)}.point-card{height:174px}.point-copy strong{font-size:22px}.point-guide{font-size:12px}}
</style>

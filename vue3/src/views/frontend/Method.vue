<template>
  <div class="method-page">
    <div class="method-top-tip">
      针灸必不可少的就是“针”了，小朋友们，在这里我们将学习神奇的毫针、微针、特种针法器！
    </div>
    <div
      v-if="loading"
      class="method-loading"
    >
      加载中…
    </div>
    <p
      v-else-if="error"
      class="method-error"
    >
      数据加载失败
    </p>
    <div
      v-else-if="!tools.length"
      class="method-empty"
    >
      暂无器具数据
    </div>
    <div
      v-else
      class="method-carousel-outer"
    >
      <div
        class="method-carousel-root"
        @mouseenter="pauseAuto = true"
        @mouseleave="pauseAuto = false"
      >
        <div class="method-left">
          <div class="method-left-fade method-left-fade-top" />
          <div class="method-left-fade method-left-fade-bottom" />
          <div class="method-left-list">
            <div
              v-for="(row, index) in tools"
              :key="row.toolsId ?? index"
              class="method-chip-wrap"
              :style="chipWrapStyle(index)"
            >
              <button
                type="button"
                class="method-chip"
                :class="{ 'is-active': index === currentIndex }"
                @click="goTo(index)"
                @mouseenter="pauseAuto = true"
                @mouseleave="pauseAuto = false"
              >
                <span class="method-chip-dot" />
                <span class="method-chip-label">{{ row.toolsName || '—' }}</span>
              </button>
            </div>
          </div>
        </div>
        <div class="method-right">
          <div class="method-card-stage">
            <div
              v-for="(row, index) in tools"
              :key="'c' + (row.toolsId ?? index)"
              class="method-card"
              :class="cardClass(index)"
              :style="cardStyle(index)"
              @click="cardClick(index)"
            >
              <img
                v-if="resolveMediaUrl(row.toolsPic1)"
                class="method-card-img"
                :src="resolveMediaUrl(row.toolsPic1)"
                :alt="row.toolsName || ''"
              >
              <div
                v-else
                class="method-card-img-ph"
              >
                暂无图片
              </div>
              <div
                v-if="cardStatus(index) === 'active'"
                class="method-card-cap"
              >
                <div class="method-card-pill">
                  {{ row.toolsName || '—' }}
                </div>
                <p class="method-card-desc">
                  {{ row.toolsBrief || '—' }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <section
      v-if="!loading && !error"
      class="method-extra-wrap"
    >
      <p class="method-extra-tip">
        针灸还有更多神奇疗法
      </p>
      <div class="method-bento-grid">
        <button
          v-for="(item, idx) in extraCourseCards"
          :key="item.extraCourseId || `coming-${idx}`"
          type="button"
          class="method-bento-card"
          :class="`bento-${idx % 4}`"
          :disabled="!item.extraCourseId"
          @click="openExtraCourse(item)"
        >
          <template v-if="item.extraCourseId">
            <div class="method-bento-icon">
              <img
                v-if="resolveMediaUrl(item.extraCourseIcon)"
                :src="resolveMediaUrl(item.extraCourseIcon)"
                :alt="item.extraCourseName || ''"
              >
              <span v-else>图</span>
            </div>
            <h3 class="method-bento-title">
              {{ item.extraCourseName || '—' }}
            </h3>
            <p class="method-bento-brief">
              {{ item.extraCourseBrief || '—' }}
            </p>
          </template>
          <template v-else>
            <div class="method-coming-soon">
              敬请期待
            </div>
          </template>
        </button>
      </div>
    </section>

    <a-modal
      v-model:open="modalOpen"
      :footer="null"
      width="min(960px, 96vw)"
      wrap-class-name="method-tool-modal-wrap"
      destroy-on-close
      :title="null"
      @cancel="closeModal"
    >
      <button
        type="button"
        class="method-modal-close"
        aria-label="关闭"
        @click="closeModal"
      >
        ×
      </button>
      <div
        v-if="popupTool"
        class="method-modal-body"
      >
        <div class="method-modal-grid">
          <div
            ref="imgContainerRef"
            class="method-ct-images"
          >
            <template
              v-for="(s, i) in innerSlides"
              :key="i"
            >
              <img
                v-if="resolveMediaUrl(s.src)"
                class="method-ct-img"
                :src="resolveMediaUrl(s.src)"
                :alt="s.title || ''"
                :style="innerImgStyle(i)"
              >
              <div
                v-else
                class="method-ct-img method-ct-img-ph"
                :style="innerImgStyle(i)"
              >
                暂无图
              </div>
            </template>
          </div>
          <div class="method-ct-content">
            <transition
              name="method-fade"
              mode="out-in"
            >
              <div
                :key="innerIndex"
                class="method-ct-textblock"
              >
                <h3 class="method-ct-title">
                  {{ innerSlides[innerIndex].title || '—' }}
                </h3>
                <p class="method-ct-text">
                  {{ innerSlides[innerIndex].text || '—' }}
                </p>
              </div>
            </transition>
            <div class="method-ct-arrows">
              <button
                type="button"
                class="method-ct-arrow"
                :style="{ backgroundColor: hoverPrev ? '#f6e8d3' : '#141414' }"
                aria-label="上一段"
                @mouseenter="hoverPrev = true"
                @mouseleave="hoverPrev = false"
                @click="innerPrev"
              >
                <span class="method-ct-arrow-inner">‹</span>
              </button>
              <button
                type="button"
                class="method-ct-arrow"
                :style="{ backgroundColor: hoverNext ? '#f6e8d3' : '#141414' }"
                aria-label="下一段"
                @mouseenter="hoverNext = true"
                @mouseleave="hoverNext = false"
                @click="innerNext"
              >
                <span class="method-ct-arrow-inner">›</span>
              </button>
            </div>
          </div>
        </div>
        <button
          type="button"
          class="method-modal-collect"
          :disabled="collecting || popupTool.skillId == null"
          @click="collectSkill"
        >
          {{ collecting ? '采集中…' : '采集' }}
        </button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listZhenjiuTools, listExtraCourse } from '@/api/AcupunctureApi'
import { addSkillToBackpack } from '@/api/BackpackApi'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const AUTO_MS = 3000
const ITEM_H = 58

const router = useRouter()
const userStore = useUserStore()
const tools = ref([])
const extraCourses = ref([])
const loading = ref(true)
const error = ref(false)
const currentIndex = ref(0)
const pauseAuto = ref(false)
const modalOpen = ref(false)
const popupTool = ref(null)
const innerIndex = ref(0)
const hoverPrev = ref(false)
const hoverNext = ref(false)
const collecting = ref(false)
const imgContainerRef = ref(null)
const containerW = ref(800)
const isWide = ref(false)

let timer = null

const extraCourseCards = computed(() => [
  ...extraCourses.value,
  { extraCourseId: null, extraCourseName: '', extraCourseBrief: '' }
])

function wrap(min, max, v) {
  const rangeSize = max - min
  return ((((v - min) % rangeSize) + rangeSize) % rangeSize) + min
}

function wrappedDistance(index) {
  const n = tools.value.length
  if (!n) return 0
  const d = index - currentIndex.value
  return wrap(-n / 2, n / 2, d)
}

function chipWrapStyle(index) {
  const y = wrappedDistance(index) * ITEM_H
  const o = 1 - Math.abs(wrappedDistance(index)) * 0.22
  const tx = isWide.value ? `0px` : `-50%`
  const ty = `${y}px`
  const transform =
    isWide.value ? `translate(0, ${ty})` : `translate(${tx}, ${ty})`
  return {
    transform,
    opacity: Math.max(0.35, Math.min(1, o)),
    transition: 'transform 0.55s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.45s ease'
  }
}

function cardStatus(index) {
  const n = tools.value.length
  const diff = index - currentIndex.value
  let nd = diff
  if (diff > n / 2) nd -= n
  if (diff < -n / 2) nd += n
  if (nd === 0) return 'active'
  if (nd === -1) return 'prev'
  if (nd === 1) return 'next'
  return 'hidden'
}

function cardClass(index) {
  return 'is-' + cardStatus(index)
}

function cardStyle(index) {
  const s = cardStatus(index)
  const active = s === 'active'
  const prev = s === 'prev'
  const next = s === 'next'
  const hid = s === 'hidden'
  let x = 0
  let scale = 1
  let op = 1
  let rot = 0
  let z = 0
  if (active) {
    x = 0
    scale = 1
    op = 1
    rot = 0
    z = 20
  } else if (prev) {
    x = -90
    scale = 0.85
    op = 0.45
    rot = -3
    z = 10
  } else if (next) {
    x = 90
    scale = 0.85
    op = 0.45
    rot = 3
    z = 10
  } else {
    x = 0
    scale = 0.72
    op = 0
    rot = 0
    z = 0
  }
  return {
    transform: `translateX(${x}px) scale(${scale}) rotate(${rot}deg)`,
    opacity: hid ? 0 : op,
    zIndex: z,
    pointerEvents: active ? 'auto' : 'none',
    transition: 'transform 0.55s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.45s ease'
  }
}

function goTo(i) {
  currentIndex.value = i
}

function cardClick(i) {
  if (i === currentIndex.value) openModal(tools.value[i])
}

function openModal(row) {
  popupTool.value = row
  innerIndex.value = 0
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  popupTool.value = null
}

const innerSlides = computed(() => {
  const t = popupTool.value
  if (!t) {
    return [
      { src: '', title: '', text: '' },
      { src: '', title: '', text: '' },
      { src: '', title: '', text: '' }
    ]
  }
  return [
    { src: t.toolsPic1, title: t.toolsTitle1, text: t.toolsText1 },
    { src: t.toolsPic2, title: t.toolsTitle2, text: t.toolsText2 },
    { src: t.toolsPic3, title: t.toolsTitle3, text: t.toolsText3 }
  ]
})

function innerGap() {
  const w = containerW.value
  if (w <= 1024) return 52
  if (w >= 1456) return 86
  return 52 + (86 - 52) * ((w - 1024) / (1456 - 1024))
}

function innerImgStyle(i) {
  const L = 3
  const gap = innerGap()
  const maxUp = gap * 0.8
  const ai = innerIndex.value
  const isA = i === ai
  const isL = (ai - 1 + L) % L === i
  const isR = (ai + 1) % L === i
  if (isA) {
    return {
      zIndex: 3,
      opacity: 1,
      transform: 'translateX(0) translateY(0) scale(1) rotateY(0deg)',
      transition: 'all 0.75s cubic-bezier(0.4, 2, 0.3, 1)'
    }
  }
  if (isL) {
    return {
      zIndex: 2,
      opacity: 1,
      transform: `translateX(-${gap}px) translateY(-${maxUp}px) scale(0.85) rotateY(15deg)`,
      transition: 'all 0.75s cubic-bezier(0.4, 2, 0.3, 1)'
    }
  }
  if (isR) {
    return {
      zIndex: 2,
      opacity: 1,
      transform: `translateX(${gap}px) translateY(-${maxUp}px) scale(0.85) rotateY(-15deg)`,
      transition: 'all 0.75s cubic-bezier(0.4, 2, 0.3, 1)'
    }
  }
  return {
    zIndex: 1,
    opacity: 0,
    pointerEvents: 'none',
    transform: 'translateX(0) scale(0.8)',
    transition: 'all 0.75s cubic-bezier(0.4, 2, 0.3, 1)'
  }
}

function innerPrev() {
  innerIndex.value = (innerIndex.value - 1 + 3) % 3
}

function innerNext() {
  innerIndex.value = (innerIndex.value + 1) % 3
}

function tick() {
  if (pauseAuto.value || !tools.value.length) return
  currentIndex.value = (currentIndex.value + 1) % tools.value.length
}

async function load() {
  loading.value = true
  error.value = false
  try {
    const toolsData = await listZhenjiuTools({}, { showDefaultMsg: false })
    tools.value = Array.isArray(toolsData) ? toolsData : []
  } catch (e) {
    console.error(e)
    error.value = true
    tools.value = []
  }
  try {
    const extraData = await listExtraCourse({}, { showDefaultMsg: false })
    extraCourses.value = Array.isArray(extraData) ? extraData : []
  } catch (e) {
    console.error(e)
    extraCourses.value = []
  } finally {
    loading.value = false
  }
}

function openExtraCourse(item) {
  if (!item?.extraCourseId) return
  router.push(`/fangfa/extra/${item.extraCourseId}`)
}

function measure() {
  if (imgContainerRef.value) {
    containerW.value = imgContainerRef.value.offsetWidth || 800
  }
}

function onResize() {
  isWide.value = window.innerWidth >= 1024
  measure()
}

onMounted(() => {
  isWide.value = window.innerWidth >= 1024
  load()
  timer = setInterval(tick, AUTO_MS)
  measure()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', onResize)
})

watch(modalOpen, (o) => {
  if (o) {
    requestAnimationFrame(() => measure())
  }
})

async function collectSkill() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再采集')
    return
  }
  const sid = popupTool.value?.skillId
  if (sid == null) {
    message.warning('未关联技能')
    return
  }
  collecting.value = true
  try {
    await addSkillToBackpack({ skillId: sid }, { showDefaultMsg: false })
    message.success('已加入背包')
  } catch (e) {
    message.error(e?.message || '采集失败')
  } finally {
    collecting.value = false
  }
}
</script>

<style scoped>
.method-page {
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  box-sizing: border-box;
  background: #f6e8d3;
}

.method-top-tip {
  max-width: 1280px;
  width: min(100%, 92vw);
  margin: 0 auto;
  padding: 20px 20px 0;
  box-sizing: border-box;
  font-size: 18px;
  line-height: 1.8;
  color: #5a3d1d;
  font-weight: 600;
}

.method-loading,
.method-error,
.method-empty {
  text-align: center;
  padding: 48px 16px;
  color: #666;
}

.method-carousel-outer {
  max-width: 1280px;
  width: min(100%, 92vw);
  margin: 0 auto;
  padding: 24px 20px 20px;
  box-sizing: border-box;
}

.method-extra-wrap {
  max-width: 1280px;
  margin: 0 auto;
  padding: 8px 20px 44px;
  box-sizing: border-box;
}

.method-extra-tip {
  margin: 0 0 14px;
  font-size: 22px;
  color: #111827;
}

.method-bento-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  grid-auto-rows: 180px;
}

.method-bento-card {
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;
}

.method-bento-card:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.method-bento-card:disabled {
  cursor: default;
  background: #f8fafc;
}

.bento-0 {
  grid-column: span 2;
  grid-row: span 2;
}

.bento-1,
.bento-2 {
  grid-column: span 2;
}

.bento-3 {
  grid-column: span 6;
}

.method-bento-icon {
  width: 58px;
  height: 58px;
  border-radius: 10px;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  overflow: hidden;
}

.method-bento-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.method-bento-title {
  margin: 0 0 8px;
  font-size: 20px;
  color: #0f172a;
}

.method-bento-brief {
  margin: 0;
  color: #475569;
  line-height: 1.6;
  font-size: 14px;
}

.method-coming-soon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 24px;
  font-weight: 600;
}

.method-carousel-root {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
  min-height: 560px;
  border-radius: 2.5rem;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

@media (min-width: 1024px) {
  .method-carousel-root {
    flex-direction: row;
    min-height: 520px;
    aspect-ratio: 16 / 9;
    max-height: min(78vh, 720px);
  }
}

@media (max-width: 1023px) {
  .method-bento-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    grid-auto-rows: 160px;
  }

  .bento-0,
  .bento-1,
  .bento-2,
  .bento-3 {
    grid-column: span 2;
    grid-row: span 1;
  }
}

.method-left {
  position: relative;
  width: 100%;
  min-height: 320px;
  background: #62b2fe;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 20px;
  z-index: 3;
}

@media (min-width: 1024px) {
  .method-left {
    width: 40%;
    min-height: 0;
    justify-content: flex-start;
    padding: 32px 24px 32px 40px;
  }
}

.method-left-fade {
  position: absolute;
  left: 0;
  right: 0;
  height: 48px;
  z-index: 4;
  pointer-events: none;
}

.method-left-fade-top {
  top: 0;
  background: linear-gradient(to bottom, #62b2fe 30%, transparent);
}

.method-left-fade-bottom {
  bottom: 0;
  background: linear-gradient(to top, #62b2fe 30%, transparent);
}

.method-left-list {
  position: relative;
  width: 100%;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (min-width: 1024px) {
  .method-left-list {
    justify-content: flex-start;
    height: 340px;
  }
}

.method-chip-wrap {
  position: absolute;
  left: 50%;
  display: flex;
  justify-content: center;
}

@media (min-width: 1024px) {
  .method-chip-wrap {
    left: 0;
    justify-content: flex-start;
  }
}

.method-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 22px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: transparent;
  color: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  font-size: 14px;
  max-width: min(92vw, 340px);
  transition: background 0.35s ease, color 0.35s ease, border-color 0.35s ease;
}

.method-chip.is-active {
  background: #fff;
  color: #62b2fe;
  border-color: #fff;
  z-index: 5;
}

.method-chip:not(.is-active):hover {
  border-color: rgba(255, 255, 255, 0.55);
  color: #fff;
}

.method-chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.5;
  flex-shrink: 0;
}

.method-chip-label {
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.method-right {
  flex: 1;
  position: relative;
  background: linear-gradient(145deg, #e8eef5 0%, #f5f7fa 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px 40px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

@media (min-width: 1024px) {
  .method-right {
    border-top: none;
    border-left: 1px solid rgba(0, 0, 0, 0.06);
    padding: 28px 32px;
  }
}

.method-card-stage {
  position: relative;
  width: 100%;
  max-width: 400px;
  aspect-ratio: 4 / 5;
}

.method-card {
  position: absolute;
  inset: 0;
  border-radius: 1.85rem;
  overflow: hidden;
  border: 6px solid #fff;
  box-sizing: border-box;
  background: #ddd;
  cursor: pointer;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
}

.method-card.is-hidden {
  pointer-events: none;
}

.method-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.method-card.is-active .method-card-img {
  filter: none;
}

.method-card.is-prev .method-card-img,
.method-card.is-next .method-card-img {
  filter: grayscale(0.3) brightness(0.88);
}

.method-card-img-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  font-size: 15px;
}

.method-card-cap {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 28px 20px 22px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.88), rgba(0, 0, 0, 0.2) 55%, transparent);
  pointer-events: none;
}

.method-card-pill {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 999px;
  background: #fff;
  color: #222;
  font-size: 11px;
  letter-spacing: 0.06em;
  margin-bottom: 10px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.method-card-desc {
  margin: 0;
  color: #fff;
  font-size: 1.05rem;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.method-modal-body {
  position: relative;
  padding: 8px 8px 52px;
  min-height: 320px;
}

.method-modal-close {
  position: absolute;
  top: 8px;
  right: 12px;
  z-index: 10;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.06);
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  color: #333;
}

.method-modal-grid {
  display: grid;
  gap: 28px;
  align-items: start;
}

@media (min-width: 768px) {
  .method-modal-grid {
    grid-template-columns: 1fr 1fr;
    gap: 36px;
    align-items: center;
  }
}

.method-ct-images {
  position: relative;
  width: 100%;
  height: 280px;
  perspective: 1000px;
}

@media (min-width: 768px) {
  .method-ct-images {
    height: 320px;
  }
}

.method-ct-img {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 1.25rem;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
}

.method-ct-img-ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e7eb;
  color: #6b7280;
  font-size: 14px;
}

.method-ct-title {
  margin: 0 0 12px;
  font-size: 1.35rem;
  font-weight: 700;
  color: #0a0a0a;
}

.method-ct-text {
  margin: 0;
  font-size: 1rem;
  line-height: 1.75;
  color: #374151;
  white-space: pre-wrap;
}

.method-ct-arrows {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

@media (min-width: 768px) {
  .method-ct-arrows {
    margin-top: 20px;
  }
}

.method-ct-arrow {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.25s ease;
}

.method-ct-arrow-inner {
  color: #f1f1f7;
  font-size: 28px;
  line-height: 1;
  font-weight: 300;
}

.method-modal-collect {
  position: absolute;
  right: 16px;
  bottom: 12px;
  padding: 10px 22px;
  border-radius: 999px;
  border: none;
  background: #62b2fe;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(98, 178, 254, 0.45);
}

.method-modal-collect:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.method-fade-enter-active,
.method-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.method-fade-enter-from,
.method-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>

<style>
.method-tool-modal-wrap .ant-modal-content {
  border-radius: 1.25rem;
  overflow: hidden;
}
.method-tool-modal-wrap .ant-modal-header {
  display: none;
}
.method-tool-modal-wrap .ant-modal-body {
  padding-top: 36px;
}
</style>

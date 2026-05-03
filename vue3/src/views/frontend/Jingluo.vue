<template>
  <div class="jingluo-page">
    <section class="jingluo-top-tip-wrap">
      <p
        class="jingluo-top-tip jingluo-audio-tip-1"
        @mouseenter="playTips1Audio"
      >
        小朋友们有没有好奇，为什么一根小小的针能缓解疼痛、改善身体呢？答案其实不在针本身，而在针所刺激的经络系统
        <br>
        让我们一起来认识这条人体的“生命通道”吧
      </p>
    </section>

    <section class="jingluo-carousel-wrap">
      <div class="carousel-root">
        <div class="carousel-viewport">
          <transition
            name="carousel-fade"
            mode="out-in"
          >
            <img
              :key="activeIndex"
              class="carousel-img"
              :src="slides[activeIndex].img"
              :alt="slides[activeIndex].title"
            >
          </transition>
        </div>

        <div
          class="slider-btn-group"
          role="tablist"
          aria-label="经络介绍轮播"
        >
          <div
            v-for="(item, index) in slides"
            :key="item.id"
            role="tab"
            tabindex="0"
            class="slider-btn"
            :class="{ 'is-active': activeIndex === index }"
            :aria-selected="activeIndex === index"
            @click="goTo(index)"
          >
            <span
              class="slider-progress-track"
              aria-hidden="true"
            >
              <span
                class="slider-progress-fill"
                :style="{ width: activeIndex === index ? progress + '%' : '0%' }"
              />
            </span>
            <span class="slider-btn-inner">
              <span class="slider-title-pill">{{ item.title }}</span>
              <button
                type="button"
                class="slider-collect-btn"
                :disabled="collecting === item.id || item.skillId == null"
                @click.stop="collectSkill(item)"
              >
                {{ collecting === item.id ? '采集中…' : '采集' }}
              </button>
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- 提示语占位 -->
    <section class="xuewei-tip-block">
      <p
        class="xuewei-tip-text jingluo-audio-tip-2"
        @mouseenter="playTips2Audio"
      >
        下面是个经络百宝箱，点击试试吧！
      </p>
    </section>

    <!-- 按经络分类展示经络 -->
    <section class="xuewei-list-section">
      <div
        v-if="jingluoLoading"
        class="xuewei-list-loading"
      >
        经络数据加载中…
      </div>
      <p
        v-else-if="jingluoError"
        class="xuewei-list-error"
      >
        经络数据加载失败，请确认后端已启动且 jingluo 表可用。
      </p>
      <template v-else>
        <div
          v-for="cat in JINGLUO_CATEGORIES"
          :key="cat"
          class="xuewei-cat-block"
        >
          <h3 class="xuewei-cat-title">
            {{ cat }}
          </h3>
          <div
            class="xuewei-scroll"
            role="list"
          >
            <template v-if="itemsByCategory[cat]?.length">
              <button
                v-for="row in itemsByCategory[cat]"
                :key="row.jingluoId"
                type="button"
                class="xuewei-card"
                role="listitem"
                @click="openJingluoModal(row.jingluoId)"
              >
                <div class="xuewei-card-pic">
                  <img
                    v-if="resolveMediaUrl(row.jingluoPic)"
                    :src="resolveMediaUrl(row.jingluoPic)"
                    :alt="row.jingluoName || ''"
                  >
                  <div
                    v-else
                    class="xuewei-card-pic-ph"
                  >
                    图
                  </div>
                </div>
                <span class="xuewei-card-name">{{ row.jingluoName || '—' }}</span>
              </button>
            </template>
            <div
              v-else
              class="xuewei-cat-empty"
            >
              该分类下暂无经络数据
            </div>
          </div>
        </div>
      </template>
    </section>

    <a-modal
      v-model:open="modalOpen"
      :closable="false"
      :footer="null"
      width="min(920px, 96vw)"
      class="xuewei-detail-modal"
      wrap-class-name="xuewei-detail-modal-wrap"
      destroy-on-close
      @cancel="closeModal"
    >
      <button
        type="button"
        class="xuewei-modal-close-floating"
        aria-label="关闭"
        @click="closeModal"
      >
        <span aria-hidden="true">×</span>
      </button>
      <div
        v-if="modalLoading"
        class="xuewei-modal-loading"
      >
        加载中…
      </div>
      <div
        v-else-if="!modalDetail"
        class="xuewei-modal-loading"
      >
        加载失败或经络不存在
      </div>
      <div
        v-else
        class="xuewei-modal-body"
      >
        <div class="xuewei-modal-left">
          <h2 class="xuewei-modal-name">
            {{ modalDetail.jingluoName || '—' }}
          </h2>
          <p class="xuewei-modal-cat">
            {{ modalDetail.jingluoCatagory || '—' }}
          </p>
          <div class="xuewei-modal-field">
            <div class="xuewei-modal-label">
              定位
            </div>
            <p class="xuewei-modal-value">
              {{ modalDetail.jingluoOrder || '—' }}
            </p>
          </div>
          <div class="xuewei-modal-field">
            <div class="xuewei-modal-label">
              功效
            </div>
            <p class="xuewei-modal-value">
              {{ modalDetail.illness || '—' }}
            </p>
          </div>
          <button
            type="button"
            class="xuewei-modal-collect"
            :disabled="modalCollecting || modalDetail.skillId == null"
            @click="collectModalSkill"
          >
            {{ modalCollecting ? '采集中…' : '采集' }}
          </button>
          <button
            type="button"
            class="xuewei-modal-favorite"
            :disabled="modalCollectToggling || modalDetail.skillId == null"
            @click="toggleModalCollect"
          >
            {{ modalCollectToggling ? '处理中…' : (modalCollected ? '取消收藏' : '收藏') }}
          </button>
        </div>
        <div class="xuewei-modal-right">
          <div class="xuewei-modal-img-wrap">
            <img
              v-if="resolveMediaUrl(modalDetail.jingluoPic)"
              :src="resolveMediaUrl(modalDetail.jingluoPic)"
              :alt="modalDetail.jingluoName || ''"
            >
            <div
              v-else
              class="xuewei-modal-img-ph"
            >
              暂无图片
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { addSkillToBackpack } from '@/api/BackpackApi'
import { addSkillToCollect, hasSkillCollect, removeSkillFromCollect } from '@/api/CollectApi'
import { listJingluo, getJingluoDetail } from '@/api/AcupunctureApi'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import jingluoTipsAudio from '@/assets/经络tips1.mp3'
import jingluoTips2Audio from '@/assets/经络tips2.mp3'

import slideImg1 from '@/assets/经络介绍1.png'
import slideImg2 from '@/assets/经络介绍2.png'
import slideImg3 from '@/assets/经络介绍3.png'
import slideImg4 from '@/assets/经络介绍4.png'
import slideImg5 from '@/assets/经络介绍5.png'


const userStore = useUserStore()

/** 与库中 jingluocatagory 字段取值一致（共 3 类） */
const JINGLUO_CATEGORIES = ['十二经脉', '奇经八脉', '十五络']

const slides = [
  { id: 's1', title: '经络是什么', img: slideImg1, skillId: 12 },
  { id: 's2', title: '经络和五脏六腑', img: slideImg2, skillId: 13 },
  { id: 's3', title: '经络的发现', img: slideImg3, skillId: 14 },
  { id: 's4', title: '经络的超能力', img: slideImg4, skillId: 15 },
  { id: 's5', title: '经络大家族', img: slideImg5, skillId: 16 }
]

const collecting = ref(null)

const jingluoList = ref([])
const jingluoLoading = ref(true)
const jingluoError = ref(false)

const modalOpen = ref(false)
const modalLoading = ref(false)
const modalDetail = ref(null)
const modalCollecting = ref(false)
const modalCollectToggling = ref(false)
const modalCollected = ref(false)
const tips1Audio = new Audio(jingluoTipsAudio)
const tips2Audio = new Audio(jingluoTips2Audio)
const JINGLUO_TIPS_FIRST_PLAY_KEY = 'jingluo-tips-first-played'

const itemsByCategory = computed(() => {
  const m = Object.fromEntries(JINGLUO_CATEGORIES.map((c) => [c, []]))
  for (const row of jingluoList.value) {
    const key = (row.jingluoCatagory || '').trim()
    if (m[key]) {
      m[key].push(row)
    }
  }
  return m
})

async function loadJingluoList() {
  jingluoLoading.value = true
  jingluoError.value = false
  try {
    const data = await listJingluo({}, { showDefaultMsg: false })
    jingluoList.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error(e)
    jingluoError.value = true
    jingluoList.value = []
  } finally {
    jingluoLoading.value = false
  }
}

async function openJingluoModal(id) {
  modalOpen.value = true
  modalDetail.value = null
  modalLoading.value = true
  try {
    const data = await getJingluoDetail(id, { showDefaultMsg: false })
    modalDetail.value = data || null
    await loadModalCollectState(modalDetail.value?.skillId)
  } catch {
    modalDetail.value = null
    modalCollected.value = false
  } finally {
    modalLoading.value = false
  }
}

function closeModal() {
  modalOpen.value = false
  modalDetail.value = null
  modalCollected.value = false
  modalCollectToggling.value = false
}

async function collectModalSkill() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再采集技能')
    return
  }
  const sid = modalDetail.value?.skillId
  if (sid == null) {
    message.warning('该经络未关联技能，请先在库中为 jingluo.skillid 配置 skills.skillid')
    return
  }
  modalCollecting.value = true
  try {
    await addSkillToBackpack({ skillId: sid }, { showDefaultMsg: false })
    message.success('收集成功！技能已放入背包')
    window.dispatchEvent(new CustomEvent('backpack-refresh'))
  } catch {
    /* 请求拦截器 */
  } finally {
    modalCollecting.value = false
  }
}

async function toggleModalCollect() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再收藏')
    return
  }
  const sid = modalDetail.value?.skillId
  if (sid == null || Number.isNaN(sid)) {
    message.warning('该经络未关联技能，无法收藏')
    return
  }
  modalCollectToggling.value = true
  try {
    if (modalCollected.value) {
      await removeSkillFromCollect({ skillId: sid }, { showDefaultMsg: false })
      modalCollected.value = false
      message.success('已取消收藏')
    } else {
      await addSkillToCollect({ skillId: sid }, { showDefaultMsg: false })
      modalCollected.value = true
      message.success('收藏成功')
    }
    window.dispatchEvent(new CustomEvent('collect-refresh'))
  } finally {
    modalCollectToggling.value = false
  }
}

async function loadModalCollectState(skillId) {
  modalCollected.value = false
  if (!userStore.isLoggedIn || skillId == null || Number.isNaN(skillId)) {
    return
  }
  try {
    const res = await hasSkillCollect({ skillId }, { showDefaultMsg: false })
    modalCollected.value = Boolean(res)
  } catch {
    modalCollected.value = false
  }
}

async function collectSkill(item) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再采集技能')
    return
  }
  if (item.skillId == null) {
    message.warning('该页未关联技能')
    return
  }
  collecting.value = item.id
  try {
    await addSkillToBackpack({ skillId: item.skillId }, { showDefaultMsg: false })
    message.success('收集成功！技能已放入背包')
    window.dispatchEvent(new CustomEvent('backpack-refresh'))
  } catch {
    /* */
  } finally {
    collecting.value = null
  }
}

const activeIndex = ref(0)
const progress = ref(0)

const DURATION = 9000
const FAST_DURATION = 400

let segmentStart = 0
let isFast = false
let pendingIndex = null
let startProgress = 0
let rafId = 0

function tick(now) {
  const elapsed = now - segmentStart

  if (isFast && pendingIndex !== null) {
    const t = Math.min(elapsed / FAST_DURATION, 1)
    progress.value = startProgress + (100 - startProgress) * t
    if (t >= 1) {
      activeIndex.value = pendingIndex
      pendingIndex = null
      isFast = false
      progress.value = 0
      segmentStart = now
    }
  } else {
    const t = Math.min(elapsed / DURATION, 1)
    progress.value = t * 100
    if (t >= 1) {
      activeIndex.value = (activeIndex.value + 1) % slides.length
      progress.value = 0
      segmentStart = now
    }
  }

  rafId = requestAnimationFrame(tick)
}

function goTo(index) {
  if (index === activeIndex.value) return
  startProgress = progress.value
  pendingIndex = index
  isFast = true
  segmentStart = performance.now()
}

function startLoop() {
  segmentStart = performance.now()
  rafId = requestAnimationFrame(tick)
}

function stopAllTipsAudio() {
  try {
    tips1Audio.pause()
    tips1Audio.currentTime = 0
    tips2Audio.pause()
    tips2Audio.currentTime = 0
  } catch {
    // ignore audio errors
  }
}

function playTips1Audio() {
  try {
    tips2Audio.pause()
    tips2Audio.currentTime = 0
    tips1Audio.currentTime = 0
    tips1Audio.play().catch(() => {})
  } catch {
    // ignore audio errors
  }
}

function playTips2Audio() {
  try {
    tips1Audio.pause()
    tips1Audio.currentTime = 0
    tips2Audio.currentTime = 0
    tips2Audio.play().catch(() => {})
  } catch {
    // ignore audio errors
  }
}

function tryAutoPlayTipsOnce() {
  if (localStorage.getItem(JINGLUO_TIPS_FIRST_PLAY_KEY) === '1') return
  localStorage.setItem(JINGLUO_TIPS_FIRST_PLAY_KEY, '1')
  playTips1Audio()
}

function onDocumentClick(e) {
  const t = e?.target
  if (!(t instanceof Element)) return
  if (t.closest('.jingluo-audio-tip-1')) return
  if (t.closest('.jingluo-audio-tip-2')) return
  stopAllTipsAudio()
}

onMounted(() => {
  document.addEventListener('click', onDocumentClick)
  tryAutoPlayTipsOnce()
  startLoop()
  loadJingluoList()
})

onUnmounted(() => {
  cancelAnimationFrame(rafId)
  document.removeEventListener('click', onDocumentClick)
  stopAllTipsAudio()
})
</script>

<style scoped>
.jingluo-page {
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  padding-bottom: 48px;
  box-sizing: border-box;
  background: #f6e8d3;
}

.jingluo-top-tip-wrap {
  max-width: 1280px;
  width: min(100%, 92vw);
  margin: 0 auto;
  padding: 14px 20px 0;
  box-sizing: border-box;
}

.jingluo-top-tip {
  margin: 0;
  padding: 14px 18px;
  font-size: 18px;
  line-height: 1.65;
  color: #2a2a2a;
  text-align: center;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 10px;
  border: 1px dashed rgba(0, 0, 0, 0.14);
  cursor: default;
}

.jingluo-carousel-wrap {
  max-width: 1280px;
  width: min(100%, 92vw);
  margin: 0 auto;
  padding: 20px 20px 32px;
  box-sizing: border-box;
}

.carousel-root {
  display: flex;
  flex-direction: column;
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 10px 32px rgba(0, 0, 0, 0.1);
}

.carousel-viewport {
  position: relative;
  width: 100%;
  flex-shrink: 0;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-img {
  display: block;
  width: 100%;
  height: min(500px, 52vh);
  min-height: 320px;
  object-fit: contain;
  object-position: center;
  vertical-align: top;
}

.carousel-fade-enter-active,
.carousel-fade-leave-active {
  transition: opacity 0.35s ease;
}

.carousel-fade-enter-from,
.carousel-fade-leave-to {
  opacity: 0;
}

.slider-btn-group {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  overflow: hidden;
  flex-shrink: 0;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  color: #111;
}

@media (min-width: 768px) {
  .slider-btn-group {
    grid-template-columns: repeat(5, 1fr);
  }
}

.slider-btn {
  position: relative;
  margin: 0;
  padding: 12px 10px;
  text-align: left;
  cursor: pointer;
  border: none;
  border-right: 1px solid rgba(0, 0, 0, 0.08);
  background: transparent;
  color: inherit;
  opacity: 0.55;
  transition: opacity 0.2s ease;
  min-height: 100px;
  font: inherit;
  outline: none;
}

.slider-btn:focus-visible {
  box-shadow: inset 0 0 0 2px rgba(24, 144, 255, 0.5);
  z-index: 2;
}

.slider-btn:last-child {
  border-right: none;
}

.slider-btn.is-active {
  opacity: 1;
}

.slider-progress-track {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.slider-progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: rgba(255, 255, 255, 0.65);
  transition: width 0.05s linear;
}

.slider-btn-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.slider-title-pill {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  background: #1f1f1f;
  color: #fff;
}

.slider-collect-btn {
  margin-top: 2px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  color: #1a4a3a;
  background: linear-gradient(135deg, #b5ead7 0%, #d4f5ea 100%);
  border: 1px solid rgba(24, 144, 255, 0.25);
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  transition: filter 0.2s, transform 0.15s;
}

.slider-collect-btn:hover:not(:disabled) {
  filter: brightness(1.03);
  transform: translateY(-1px);
}

.slider-collect-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

/* 提示语 */
.xuewei-tip-block {
  max-width: 1080px;
  width: min(60%, 92vw);
  margin: 0 auto 24px;
  padding: 0 20px;
  box-sizing: border-box;
}

.xuewei-tip-text {
  margin: 0;
  padding: 14px 18px;
  font-size: 15px;
  line-height: 1.65;
  text-align: center;
  color: rgba(0, 0, 0, 0.72);
  background: rgba(255, 255, 255, 0.65);
  border-radius: 10px;
  border: 1px dashed rgba(0, 0, 0, 0.12);
}

/* 分类列表 */
.xuewei-list-section {
  max-width: 1280px;
  width: min(100%, 92vw);
  margin: 0 auto;
  padding: 0 20px 32px;
  box-sizing: border-box;
}

.xuewei-list-loading,
.xuewei-list-error {
  text-align: center;
  padding: 24px;
  color: #666;
}

.xuewei-cat-block {
  margin-bottom: 28px;
}

.xuewei-cat-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 700;
  color: #3d3558;
}

.xuewei-scroll {
  display: flex;
  flex-wrap: nowrap;
  gap: 16px;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
}

.xuewei-scroll::-webkit-scrollbar {
  height: 8px;
}

.xuewei-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.xuewei-card {
  flex: 0 0 auto;
  width: 104px;
  margin: 0;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: center;
  font: inherit;
  color: inherit;
}

.xuewei-card:focus-visible {
  outline: 2px solid #1890ff;
  outline-offset: 4px;
  border-radius: 8px;
}

.xuewei-card-pic {
  width: 104px;
  height: 104px;
  border-radius: 8px;
  overflow: hidden;
  background: #ddd;
  margin: 0 auto 8px;
}

.xuewei-card-pic img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.xuewei-card-pic-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #888;
}

.xuewei-card-name {
  display: block;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.35;
  color: #333;
  word-break: break-all;
}

.xuewei-cat-empty {
  font-size: 14px;
  color: #999;
  padding: 12px 0;
}

/* 弹窗内容（覆盖 a-modal 默认标题栏，自绘关闭钮） */
.xuewei-modal-body {
  position: relative;
  display: flex;
  flex-direction: row;
  gap: 24px;
  align-items: flex-start;
  padding-top: 8px;
}

.xuewei-modal-left {
  flex: 1;
  min-width: 0;
  padding-right: 8px;
}

.xuewei-modal-name {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
}

.xuewei-modal-cat {
  margin: 0 0 20px;
  font-size: 14px;
  color: #666;
}

.xuewei-modal-field {
  margin-bottom: 16px;
}

.xuewei-modal-label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.xuewei-modal-value {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: #444;
  white-space: pre-wrap;
}

.xuewei-modal-collect {
  margin-top: 8px;
  padding: 8px 22px;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  color: #1a4a3a;
  background: linear-gradient(135deg, #b5ead7 0%, #d4f5ea 100%);
  border: 1px solid rgba(24, 144, 255, 0.25);
  border-radius: 999px;
  cursor: pointer;
}

.xuewei-modal-collect:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.xuewei-modal-favorite {
  margin-top: 8px;
  margin-left: 10px;
  padding: 8px 22px;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  color: #5a3340;
  background: linear-gradient(135deg, #ffd5e2 0%, #ffe7ef 100%);
  border: 1px solid rgba(210, 120, 150, 0.35);
  border-radius: 999px;
  cursor: pointer;
}

.xuewei-modal-favorite:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.xuewei-modal-right {
  flex: 0 0 42%;
  max-width: 420px;
}

.xuewei-modal-img-wrap {
  aspect-ratio: 1;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: #e8e8e8;
}

.xuewei-modal-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.xuewei-modal-img-ph {
  width: 100%;
  height: 100%;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
}

.xuewei-modal-loading {
  padding: 40px;
  text-align: center;
  color: #666;
}

@media (max-width: 767px) {
  .xuewei-modal-body {
    flex-direction: column;
  }

  .xuewei-modal-right {
    flex: none;
    max-width: none;
    width: 100%;
  }

  .slider-btn:nth-child(2n) {
    border-right: none;
  }

  .slider-btn {
    min-height: 88px;
  }
}
</style>

<style>
.xuewei-detail-modal-wrap .ant-modal-header {
  display: none;
}

.xuewei-detail-modal-wrap .ant-modal-body {
  position: relative;
  padding-top: 44px;
}

.xuewei-modal-close-floating {
  position: absolute;
  top: 12px;
  right: 16px;
  z-index: 10;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.06);
  color: #333;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.xuewei-modal-close-floating:hover {
  background: rgba(0, 0, 0, 0.1);
}
</style>

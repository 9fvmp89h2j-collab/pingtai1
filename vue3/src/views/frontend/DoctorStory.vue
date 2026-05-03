<template>
  <div class="doctor-story-page">
    <header class="ds-header">
      <div class="ds-header-copy">
        <p class="ds-subtitle">
          古老的针灸，从一堆篝火、一块石头慢慢诞生，经过了一代又一代人的不断尝试和改进，才变得越来越神奇。你想知道有哪些针灸名医吗？
        </p>
        <button
          type="button"
          class="ds-audio-btn"
          :class="{ 'is-playing': tipsPlaying }"
          @click="toggleDoctorTipsAudio"
        >
          {{ tipsPlaying ? '暂停导读' : '听一听导读' }}
        </button>
      </div>
    </header>

    <div class="ds-spacer" />

    <div
      v-if="loading"
      class="ds-loading"
    >
      加载中…
    </div>
    <p
      v-else-if="loadError"
      class="ds-empty"
    >
      名医故事加载失败，请确认后端已启动且 doctorstory 表可用。
    </p>
    <p
      v-else-if="!doctors.length"
      class="ds-empty"
    >
      暂无名医故事数据。
    </p>
    <div
      v-else
      class="ds-options-wrap"
    >
      <div class="ds-options">
        <div
          v-for="(d, index) in doctors"
          :key="d.id ?? index"
          class="ds-option"
          :class="{ active: activeIndex === index, shown: animatedIndices.includes(index) }"
          :style="optionSurfaceStyle(index, d)"
          @click="setActive(index)"
        >
          <div
            class="ds-option-shadow"
            :class="{ active: activeIndex === index }"
          />
          <div class="ds-label">
            <div class="ds-icon-ring">
              <span class="ds-icon">🪡</span>
            </div>
            <div class="ds-info">
              <div
                class="ds-main"
                :class="{ visible: activeIndex === index }"
              >
                {{ d.doctorName || '名医' }}
              </div>
              <div
                class="ds-sub"
                :class="{ visible: activeIndex === index }"
              >
                {{ d.doctorBrief || '' }}
              </div>
            </div>
          </div>
          <!-- 条带：非展开时也显示名字与简介摘要 -->
          <div class="ds-strip">
            <span class="ds-strip-name">{{ d.doctorName || '名医' }}</span>
            <span
              v-if="d.doctorBrief"
              class="ds-strip-brief"
            >{{ d.doctorBrief }}</span>
          </div>
        </div>
      </div>
      <p class="ds-hint">
        点击窄条展开大图；再次点击其他卡片切换
      </p>
      <button
        type="button"
        class="ds-detail-btn"
        @click="openModalForActive"
      >
        阅读「{{ activeDoctor?.doctorName || '名医' }}」详解
      </button>
    </div>

    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="modalCard"
          class="modal-overlay"
          @click="closeModal"
        >
          <div
            class="modal-container"
            @click.stop
          >
            <div class="modal-header-bar">
              <div class="modal-collect-block">
                <span class="modal-collect-hint">读完后，把技能收进背包吧</span>
                <button
                  type="button"
                  class="modal-collect-btn"
                  :disabled="collecting || modalCard.skillId == null"
                  @click.stop="collectSkill"
                >
                  {{ collecting ? '收集中…' : '采集' }}
                </button>
                <button
                  type="button"
                  class="modal-favorite-btn"
                  :disabled="collectToggling || modalCard.skillId == null"
                  @click.stop="toggleCollect"
                >
                  {{ collectToggling ? '处理中…' : (collected ? '取消收藏' : '收藏') }}
                </button>
              </div>
              <button
                type="button"
                class="modal-close"
                @click="closeModal"
              >
                <i class="fas fa-times" />
              </button>
            </div>
            <div class="modal-content">
              <div class="modal-header">
                <h3 class="modal-title">
                  <PinyinStoryText
                    :text="modalCard.title"
                    :glossary="modalGlossary"
                    :full-pinyin="fullPinyinEnabled"
                    :active-term-id="selectedGlossary?.id || ''"
                    @select-term="handleGlossarySelect"
                  />
                </h3>
                <p
                  v-if="modalCard.brief"
                  class="modal-subtitle"
                >
                  <PinyinStoryText
                    :text="modalCard.brief"
                    :glossary="modalGlossary"
                    :full-pinyin="fullPinyinEnabled"
                    :active-term-id="selectedGlossary?.id || ''"
                    @select-term="handleGlossarySelect"
                  />
                </p>
              </div>
              <div class="modal-body">
                <div class="modal-text-column">
                  <div class="modal-reading-toolbar">
                    <div class="modal-reading-copy">
                      <div class="modal-reading-title">
                        阅读辅助
                      </div>
                      <p class="modal-reading-note">
                        重点词默认带拼音，点击蓝色词语可以查看解释。
                      </p>
                    </div>
                    <button
                      type="button"
                      class="modal-pinyin-toggle"
                      :class="{ 'is-on': fullPinyinEnabled }"
                      :disabled="pinyinLoading"
                      @click="toggleFullPinyin"
                    >
                      {{ pinyinLoading ? '全文拼音：加载中' : (fullPinyinEnabled ? '全文拼音：开' : '全文拼音：关') }}
                    </button>
                  </div>
                  <div
                    class="modal-word-card"
                    :class="{ 'is-empty': !selectedGlossary }"
                  >
                    <template v-if="selectedGlossary">
                      <div class="modal-word-head">
                        <span class="modal-word-text">{{ selectedGlossary.word }}</span>
                        <span class="modal-word-pinyin">{{ selectedGlossary.pinyin }}</span>
                      </div>
                      <p class="modal-word-meaning">
                        {{ selectedGlossary.meaning }}
                      </p>
                    </template>
                    <p
                      v-else
                      class="modal-word-placeholder"
                    >
                      点击正文里带拼音的蓝色词语，就能看到儿童版解释。
                    </p>
                  </div>
                  <div class="modal-text">
                    <p
                      v-for="(para, idx) in modalCard.content"
                      :key="idx"
                    >
                      <PinyinStoryText
                        :text="para"
                        :glossary="modalGlossary"
                        :full-pinyin="fullPinyinEnabled"
                        :active-term-id="selectedGlossary?.id || ''"
                        @select-term="handleGlossarySelect"
                      />
                    </p>
                  </div>
                </div>
                <div
                  v-if="modalCard.images?.length || modalCard.videoUrl"
                  class="modal-images"
                >
                  <div
                    v-for="(src, idx) in modalCard.images"
                    :key="idx"
                    class="modal-image"
                  >
                    <img
                      :src="src"
                      :alt="`${modalCard.title}-${idx + 1}`"
                    >
                  </div>
                  <div
                    v-if="modalCard.videoUrl"
                    class="modal-video"
                  >
                    <video
                      class="modal-video-player"
                      controls
                      preload="metadata"
                      :src="modalCard.videoUrl"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listDoctorStories } from '@/api/AcupunctureApi'
import { addSkillToBackpack } from '@/api/BackpackApi'
import { addSkillToCollect, hasSkillCollect, removeSkillFromCollect } from '@/api/CollectApi'
import PinyinStoryText from '@/components/frontend/PinyinStoryText.vue'
import { getDoctorStoryGlossary } from '@/data/doctorStoryGlossary'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import { ensureStoryPinyinReady } from '@/utils/storyPinyin'
import doctorTipsAudio from '@/assets/doctortips.mp3'

const doctorStoryPreviewImages = Object.freeze([
  '/doctor-story-preview/1.jpg',
  '/doctor-story-preview/2.jpg',
  '/doctor-story-preview/3.jpg',
  '/doctor-story-preview/4.jpg',
  '/doctor-story-preview/5.jpg',
  '/doctor-story-preview/6.jpg',
  '/doctor-story-preview/7.jpg'
])

const router = useRouter()
const userStore = useUserStore()

const doctors = ref([])
const loading = ref(true)
const loadError = ref(false)
const activeIndex = ref(0)
const animatedIndices = ref([])
const modalCard = ref(null)
const collecting = ref(false)
const collectToggling = ref(false)
const collected = ref(false)
const fullPinyinEnabled = ref(false)
const pinyinLoading = ref(false)
const selectedGlossary = ref(null)
const tipsPlaying = ref(false)
const tipsAudio = typeof Audio !== 'undefined' ? new Audio(doctorTipsAudio) : null

const activeDoctor = computed(() => doctors.value[activeIndex.value] || null)
const modalGlossary = computed(() => getDoctorStoryGlossary(modalCard.value))

function goHome() {
  router.push('/landing')
}

function goLanding() {
  router.push('/landing')
}

function goShunting() {
  router.push('/shunting-game')
}

function cssBackgroundImage(path) {
  const u = resolveMediaUrl(path)
  if (!u) return 'none'
  const safe = String(u).replace(/\\/g, '/').replace(/'/g, "\\'")
  return `url('${safe}')`
}

function getDoctorStoryPreviewPath(index, story) {
  if (story?.previewPic) {
    return story.previewPic
  }

  const storyId = Number(story?.id)
  if (Number.isInteger(storyId) && storyId > 0 && storyId <= doctorStoryPreviewImages.length) {
    return doctorStoryPreviewImages[storyId - 1]
  }

  const sequence = Number(index) + 1
  if (Number.isInteger(sequence) && sequence > 0 && sequence <= doctorStoryPreviewImages.length) {
    return doctorStoryPreviewImages[sequence - 1]
  }

  return story?.doctorPic1 || ''
}

function optionSurfaceStyle(index, d) {
  const isActive = activeIndex.value === index
  const shown = animatedIndices.value.includes(index)
  return {
    backgroundImage: cssBackgroundImage(getDoctorStoryPreviewPath(index, d)),
    backgroundSize: isActive ? 'auto 100%' : 'auto 120%',
    backgroundPosition: 'center',
    flex: isActive ? '7 1 0%' : '1 1 0%',
    zIndex: isActive ? 10 : 1,
    opacity: shown ? 1 : 0,
    transform: shown ? 'translateX(0)' : 'translateX(-60px)'
  }
}

function setActive(index) {
  if (index !== activeIndex.value) {
    activeIndex.value = index
  }
}

function playDoctorTipsAudio() {
  if (!tipsAudio) return
  try {
    tipsAudio.currentTime = 0
    tipsAudio.play()
      .then(() => {
        tipsPlaying.value = true
      })
      .catch(() => {
        tipsPlaying.value = false
      })
  } catch {
    tipsPlaying.value = false
  }
}

function stopDoctorTipsAudio() {
  if (!tipsAudio) return
  try {
    tipsAudio.pause()
    tipsAudio.currentTime = 0
    tipsPlaying.value = false
  } catch {
    tipsPlaying.value = false
  }
}

function syncDoctorTipsAudioState() {
  if (!tipsAudio) return
  tipsPlaying.value = !tipsAudio.paused && !tipsAudio.ended
}

function toggleDoctorTipsAudio() {
  if (tipsPlaying.value) {
    stopDoctorTipsAudio()
    return
  }
  playDoctorTipsAudio()
}

function openModalForActive() {
  const story = activeDoctor.value
  if (!story) return
  const text = story.doctorDetail || story.doctorBrief || ''
  const paragraphs = text
    .split(/\n+/)
    .map((s) => s.trim())
    .filter(Boolean)
  const rawPics = [story.doctorPic1, story.doctorPic2, story.doctorPic3]
  const images = rawPics.map((x) => resolveMediaUrl(x)).filter(Boolean)
  const videoUrl = resolveMediaUrl(story.media) || ''
  modalCard.value = {
    storyId: story.id ?? null,
    title: story.doctorName || '',
    brief: story.doctorBrief || '',
    readingGlossary: Array.isArray(story.readingGlossary) ? story.readingGlossary : [],
    content: paragraphs.length ? paragraphs : (text ? [text] : ['暂无详解']),
    images,
    videoUrl,
    skillId: story.skillId != null ? Number(story.skillId) : null
  }
  fullPinyinEnabled.value = false
  pinyinLoading.value = false
  selectedGlossary.value = null
  loadCollectState(modalCard.value.skillId)
  document.body.style.overflow = 'hidden'
}

function closeModal() {
  modalCard.value = null
  collected.value = false
  collectToggling.value = false
  fullPinyinEnabled.value = false
  pinyinLoading.value = false
  selectedGlossary.value = null
  document.body.style.overflow = ''
}

async function toggleFullPinyin() {
  if (pinyinLoading.value) return

  if (fullPinyinEnabled.value) {
    fullPinyinEnabled.value = false
    return
  }

  pinyinLoading.value = true
  try {
    await ensureStoryPinyinReady()
    fullPinyinEnabled.value = true
  } finally {
    pinyinLoading.value = false
  }
}

function handleGlossarySelect(entry) {
  if (!entry) return
  selectedGlossary.value = selectedGlossary.value?.id === entry.id ? null : entry
}

async function collectSkill() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再收集技能')
    return
  }
  const sid = modalCard.value?.skillId
  if (sid == null || Number.isNaN(sid)) {
    message.warning('当前故事未关联技能，无法收集')
    return
  }
  collecting.value = true
  try {
    await addSkillToBackpack({ skillId: sid }, { showDefaultMsg: false })
    message.success('收集成功！技能已放入背包')
    window.dispatchEvent(new CustomEvent('backpack-refresh'))
  } catch {
    /* request 拦截器 */
  } finally {
    collecting.value = false
  }
}

async function toggleCollect() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再收藏')
    return
  }
  const sid = modalCard.value?.skillId
  if (sid == null || Number.isNaN(sid)) {
    message.warning('当前故事未关联技能，无法收藏')
    return
  }
  collectToggling.value = true
  try {
    if (collected.value) {
      await removeSkillFromCollect({ skillId: sid }, { showDefaultMsg: false })
      collected.value = false
      message.success('已取消收藏')
    } else {
      await addSkillToCollect({ skillId: sid }, { showDefaultMsg: false })
      collected.value = true
      message.success('收藏成功')
    }
    window.dispatchEvent(new CustomEvent('collect-refresh'))
  } catch {
    // request 拦截器
  } finally {
    collectToggling.value = false
  }
}

async function loadCollectState(skillId) {
  collected.value = false
  if (!userStore.isLoggedIn || skillId == null || Number.isNaN(skillId)) {
    return
  }
  try {
    const res = await hasSkillCollect({ skillId }, { showDefaultMsg: false })
    collected.value = Boolean(res)
  } catch {
    collected.value = false
  }
}

async function fetchList() {
  loading.value = true
  loadError.value = false
  try {
    const data = await listDoctorStories({}, { showDefaultMsg: false })
    doctors.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error(e)
    loadError.value = true
    doctors.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (tipsAudio) {
    tipsAudio.addEventListener('play', syncDoctorTipsAudioState)
    tipsAudio.addEventListener('pause', syncDoctorTipsAudioState)
    tipsAudio.addEventListener('ended', syncDoctorTipsAudioState)
  }
  await fetchList()
  doctors.value.forEach((_, i) => {
    setTimeout(() => {
      animatedIndices.value = [...animatedIndices.value, i]
    }, 180 * i)
  })
})

onUnmounted(() => {
  if (tipsAudio) {
    tipsAudio.removeEventListener('play', syncDoctorTipsAudioState)
    tipsAudio.removeEventListener('pause', syncDoctorTipsAudioState)
    tipsAudio.removeEventListener('ended', syncDoctorTipsAudioState)
  }
  stopDoctorTipsAudio()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500;600;700&display=swap');

.doctor-story-page {
  font-family: 'Fredoka', sans-serif;
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #f1e8e5;
  color: #fff;
  overflow-x: hidden;
}

.ds-header {
  width: 100%;
  max-width: 42rem;
  padding: 1.5rem 1.5rem 0;
  text-align: center;
}

.ds-header-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.ds-title {
  font-size: clamp(1.75rem, 5vw, 2.75rem);
  font-weight: 800;
  margin: 0 0 0.5rem;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 24px rgba(0, 0, 0, 0.45);
}

.ds-subtitle {
  margin: 0 0 1rem;
  font-size: 1rem;
  color: #c4c4c4;
  line-height: 1.5;
}

.ds-audio-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffd67c 0%, #ffe8ad 100%);
  color: #6b4c0b;
  font-family: inherit;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(180, 122, 15, 0.18);
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.ds-audio-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.ds-audio-btn.is-playing {
  background: linear-gradient(135deg, #ffc36b 0%, #ffd27d 100%);
  box-shadow: 0 12px 26px rgba(180, 122, 15, 0.24);
}

.ds-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-top: 8px;
}

.ds-btn-home {
  border-radius: 999px !important;
  font-weight: 600 !important;
}

.ds-btn-shunting {
  border-radius: 999px !important;
  color: #1a1a1a !important;
  background: linear-gradient(135deg, #f0d010 0%, #ffe566 100%) !important;
  border: none !important;
}

.ds-btn-back {
  border-radius: 999px !important;
  color: #e0e0e0 !important;
  border-color: #555 !important;
}

.ds-spacer {
  height: 1.5rem;
}

.ds-loading,
.ds-empty {
  text-align: center;
  color: #aaa;
  padding: 2rem 1rem;
}

.ds-options-wrap {
  width: 100%;
  max-width: 960px;
  padding: 0 12px 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ds-options {
  display: flex;
  width: 100%;
  min-height: 400px;
  align-items: stretch;
  overflow: hidden;
  position: relative;
}

.ds-option {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  overflow: hidden;
  cursor: pointer;
  min-width: 56px;
  min-height: 100px;
  margin: 0;
  border: 2px solid #292929;
  border-radius: 0;
  background-color: #e3ad89;
  background-repeat: no-repeat;
  transition:
    flex 0.7s ease-in-out,
    box-shadow 0.7s ease-in-out,
    background-size 0.7s ease-in-out,
    opacity 0.55s ease-out,
    transform 0.55s ease-out,
    border-color 0.35s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  will-change: flex, box-shadow, background-size;
}

.ds-option.active {
  border-color: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.ds-option-shadow {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -40px;
  height: 120px;
  pointer-events: none;
  transition: bottom 0.7s ease-in-out, box-shadow 0.7s ease-in-out;
  box-shadow: inset 0 -120px 0 -120px #000, inset 0 -120px 0 -80px #000;
}

.ds-option-shadow.active {
  bottom: 0;
  box-shadow: inset 0 -120px 120px -120px #000, inset 0 -120px 120px -80px #000;
}

.ds-label {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 0 1rem;
  z-index: 2;
  pointer-events: none;
}

.ds-icon-ring {
  min-width: 44px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(32, 32, 32, 0.85);
  backdrop-filter: blur(10px);
  border: 2px solid #444;
  flex-shrink: 0;
}

.ds-icon {
  font-size: 1.25rem;
}

.ds-info {
  flex: 1;
  min-width: 0;
  text-align: left;
}

.ds-main,
.ds-sub {
  transition: opacity 0.7s ease-in-out, transform 0.7s ease-in-out;
}

.ds-main {
  font-weight: 700;
  font-size: 1.05rem;
  opacity: 0;
  transform: translateX(25px);
  color: #fff;
}

.ds-sub {
  font-size: 0.9rem;
  color: #d0d0d0;
  margin-top: 2px;
  opacity: 0;
  transform: translateX(25px);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ds-main.visible,
.ds-sub.visible {
  opacity: 1;
  transform: translateX(0);
}

/* 底部条：始终可读名字与简介（满足「图片底部是名字和简介」） */
.ds-strip {
  position: relative;
  z-index: 3;
  padding: 10px 12px 12px;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.75) 35%, rgba(0, 0, 0, 0.88) 100%);
  display: flex;
  flex-direction: column;
  gap: 4px;
  pointer-events: none;
}

.ds-strip-name {
  font-weight: 700;
  font-size: 0.95rem;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.8);
}

.ds-strip-brief {
  font-size: 0.8rem;
  color: #e8e8e8;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ds-option.active .ds-strip {
  opacity: 0;
  height: 0;
  padding: 0;
  overflow: hidden;
}

.ds-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: #888;
  text-align: center;
}

.ds-detail-btn {
  margin-top: 16px;
  padding: 12px 28px;
  font-family: inherit;
  font-size: 16px;
  font-weight: 600;
  color: #5a4a78;
  background: linear-gradient(135deg, #b5ead7 0%, #d4f5ea 100%);
  border: none;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  transition: transform 0.2s ease, filter 0.2s ease;
}

.ds-detail-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.05);
}

@media (max-width: 640px) {
  .ds-header-copy {
    gap: 12px;
  }

  .ds-audio-btn {
    width: 100%;
    max-width: 220px;
  }

  .ds-options {
    flex-direction: column;
    min-height: auto;
  }

  .ds-option {
    min-height: 120px;
    flex: 0 0 auto !important;
    height: 120px;
  }

  .ds-option.active {
    flex: 1 1 280px !important;
    height: 280px;
    min-height: 280px;
  }
}

/* ========== 弹层（与 Landing 一致风格） ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 24px;
}

.modal-container {
  position: relative;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  background: #fff;
  border-radius: 0;
  overflow: hidden;
  box-shadow:
    0 18px 40px rgba(0, 0, 0, 0.28);
  display: flex;
  flex-direction: column;
}

.modal-header-bar {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px 0;
  flex-wrap: wrap;
}

.modal-collect-block {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  max-width: min(100%, 420px);
}

.modal-collect-hint {
  font-size: 14px;
  line-height: 1.4;
  color: #555;
  text-align: right;
}

.modal-collect-btn {
  font-size: 15px;
  font-weight: 600;
  padding: 8px 22px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #fff;
  background: #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.modal-collect-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.modal-favorite-btn {
  font-size: 15px;
  font-weight: 600;
  padding: 8px 22px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #333;
  background: #ffdce8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
}

.modal-favorite-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.modal-collect-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.modal-favorite-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.modal-close {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f3f3;
  border: none;
  border-radius: 0;
  cursor: pointer;
  font-size: 18px;
  color: #333;
  transition: transform 0.3s ease;
}

.modal-close:hover {
  transform: scale(1.08) rotate(90deg);
}

.modal-content {
  padding: 32px 40px 40px;
  overflow-y: auto;
}

.modal-header {
  margin-bottom: 24px;
}

.modal-title {
  font-size: 28px;
  font-weight: 700;
  color: #222;
  margin: 0;
}

.modal-subtitle {
  font-size: 17px;
  color: #666;
  margin: 10px 0 0;
}

.modal-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
  align-items: start;
}

.modal-text-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-reading-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  background: #f8f5ff;
  border: 1px solid #e3ddff;
  border-radius: 16px;
}

.modal-reading-copy {
  min-width: 0;
}

.modal-reading-title {
  font-size: 15px;
  font-weight: 700;
  color: #4b3f72;
}

.modal-reading-note {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.45;
  color: #6b6484;
}

.modal-pinyin-toggle {
  flex-shrink: 0;
  padding: 10px 16px;
  border: none;
  border-radius: 999px;
  background: #e8e2ff;
  color: #4b3f72;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, filter 0.2s ease, background 0.2s ease;
}

.modal-pinyin-toggle:hover {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.modal-pinyin-toggle:disabled {
  cursor: wait;
  opacity: 0.72;
  transform: none;
}

.modal-pinyin-toggle.is-on {
  background: linear-gradient(135deg, #7fc8ff 0%, #b0e0ff 100%);
  color: #164063;
}

.modal-word-card {
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff7e8;
  border: 1px solid #f4ddb1;
}

.modal-word-card.is-empty {
  background: #fafafa;
  border-color: #ececec;
}

.modal-word-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.modal-word-text {
  font-size: 20px;
  font-weight: 700;
  color: #2a2a2a;
}

.modal-word-pinyin {
  font-size: 15px;
  font-weight: 700;
  color: #b0700a;
}

.modal-word-meaning,
.modal-word-placeholder {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: #5a5344;
}

@media (max-width: 768px) {
  .modal-body {
    grid-template-columns: 1fr;
  }

  .modal-reading-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

.modal-text {
  font-size: 17px;
  line-height: 1.75;
  color: #333;
}

.modal-text p {
  margin: 0 0 14px;
}

.modal-images {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal-image {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #f7f7f7;
  border-radius: 0;
}

.modal-image img {
  max-width: 100%;
  max-height: 260px;
  object-fit: contain;
}

.modal-video {
  padding: 14px;
  background: #f7f7f7;
}

.modal-video-player {
  width: 100%;
  max-height: 260px;
  background: #000;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.35s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.94) translateY(12px);
  transition: transform 0.35s ease;
}
</style>

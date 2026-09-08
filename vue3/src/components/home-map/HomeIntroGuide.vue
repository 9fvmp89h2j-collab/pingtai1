<template>
  <section
    v-if="visible"
    ref="introRef"
    class="home-intro"
    role="dialog"
    aria-modal="true"
    aria-labelledby="home-intro-title"
    aria-describedby="home-intro-description"
    tabindex="-1"
    @keydown="handleIntroKeydown"
  >
    <div
      class="home-intro__scrim"
      aria-hidden="true"
    />

    <div
      class="home-intro__stage"
      :class="[
        `home-intro__stage--${activeStep.tone}`,
        { 'is-transitioning': isTransitioning }
      ]"
    >
      <header class="home-intro__header">
        <div class="home-intro__brand">
          <img
            :src="materialBadge"
            alt=""
            aria-hidden="true"
          >
          <span>
            <strong>小铜人侦探社</strong>
            <small>新手探案引导</small>
          </span>
        </div>

        <div
          class="home-intro__step-count"
          aria-label="当前引导进度"
        >
          <strong>第 {{ stepIndex + 1 }} 步</strong>
          <span>共 {{ introSteps.length }} 步</span>
        </div>

        <button
          type="button"
          class="home-intro__skip"
          @click="requestSkip"
        >
          跳过引导
        </button>
      </header>

      <div
        class="home-intro__map"
        :style="{ backgroundImage: `url(${mapImage})` }"
        role="img"
        :aria-label="`杏林探案地图，当前提示：${activeStep.mapTitle}`"
      >
        <div
          class="home-intro__map-shade"
          aria-hidden="true"
        />
        <span
          class="home-intro__path home-intro__path--one"
          aria-hidden="true"
        />
        <span
          class="home-intro__path home-intro__path--two"
          aria-hidden="true"
        />
        <span
          class="home-intro__path home-intro__path--three"
          aria-hidden="true"
        />

        <div class="home-intro__map-label">
          <span>杏林探案地图</span>
          <strong>足三里线索案</strong>
        </div>

        <div class="home-intro__mission-card">
          <span
            class="home-intro__target-pulse"
            aria-hidden="true"
          />
          <img
            :src="materialBambooSlip"
            alt=""
            aria-hidden="true"
          >
          <span>当前线索</span>
          <strong>{{ currentLevel?.label || '失踪竹简案' }}</strong>
        </div>

        <div class="home-intro__safety-badge">
          <img
            :src="materialSafetyBell"
            alt=""
            aria-hidden="true"
          >
          <span>只观察、只学习</span>
        </div>

        <div class="home-intro__map-tip">
          <span>{{ activeStep.mapEyebrow }}</span>
          <strong>{{ activeStep.mapTitle }}</strong>
          <small>{{ activeStep.mapHint }}</small>
        </div>
      </div>

      <div
        class="home-intro__guide"
        aria-hidden="true"
      >
        <span class="home-intro__glow" />
        <img
          :src="activeGuideImage"
          :alt="activeStep.guideAlt"
        >
      </div>

      <article class="home-intro__dialogue">
        <div
          :key="activeStep.id"
          class="home-intro__copy"
        >
          <span class="home-intro__kicker">{{ activeStep.kicker }}</span>
          <h2 id="home-intro-title">
            {{ activeStep.title }}
          </h2>
          <p id="home-intro-description">
            {{ activeStep.text }}
          </p>

          <div
            v-if="activeStep.tone === 'safe'"
            class="home-intro__safe-rule"
          >
            <img
              :src="materialSafetyBell"
              alt=""
              aria-hidden="true"
            >
            <span>
              <strong>侦探守则</strong>
              <small>只观察、只学习，不自己针刺。</small>
            </span>
          </div>
        </div>

        <nav
          class="home-intro__progress"
          aria-label="引导步骤"
        >
          <ol>
            <li
              v-for="(step, index) in introSteps"
              :key="step.id"
              :class="{
                complete: index < stepIndex,
                current: index === stepIndex
              }"
              :aria-current="index === stepIndex ? 'step' : undefined"
            >
              <span class="home-intro__step-marker">
                <img
                  v-if="index < stepIndex"
                  :src="materialBadge"
                  alt="已完成"
                >
                <span v-else>{{ index + 1 }}</span>
              </span>
              <span class="home-intro__step-label">{{ step.shortLabel }}</span>
            </li>
          </ol>
        </nav>

        <div
          class="home-intro__feedback"
          :class="{ 'is-success': feedbackMessage }"
          role="status"
          aria-live="polite"
          aria-atomic="true"
        >
          <img
            :src="feedbackMessage ? materialBadge : activeFeedbackImage"
            alt=""
            aria-hidden="true"
          >
          <span>{{ visualFeedback }}</span>
        </div>

        <div class="home-intro__actions">
          <button
            type="button"
            class="home-intro__ghost"
            :disabled="stepIndex === 0 || isTransitioning"
            @click="previousStep"
          >
            上一步
          </button>
          <button
            ref="primaryButtonRef"
            type="button"
            class="home-intro__primary"
            :disabled="isTransitioning"
            :aria-busy="isTransitioning"
            @click="advanceIntro"
          >
            <span>{{ primaryButtonLabel }}</span>
            <small>{{ isLastStep ? '进入杏林地图' : `前往第 ${stepIndex + 2} 步` }}</small>
          </button>
        </div>
      </article>

      <div
        v-if="skipConfirmVisible"
        class="home-intro__confirm"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="home-intro-skip-title"
        aria-describedby="home-intro-skip-description"
      >
        <div class="home-intro__confirm-card">
          <img
            :src="materialBambooSlip"
            alt=""
            aria-hidden="true"
          >
          <span class="home-intro__kicker">先等等，小侦探</span>
          <h3 id="home-intro-skip-title">
            要跳过新手引导吗？
          </h3>
          <p id="home-intro-skip-description">
            跳过后仍可点击首页右上角的“小铜人引导”再次查看。
          </p>
          <div class="home-intro__confirm-actions">
            <button
              ref="continueButtonRef"
              type="button"
              class="home-intro__ghost"
              :disabled="isTransitioning"
              @click="cancelSkip"
            >
              继续引导
            </button>
            <button
              type="button"
              class="home-intro__confirm-skip"
              :disabled="isTransitioning"
              @click="confirmSkip"
            >
              确认跳过
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'

const detectivePoint = generatedRewardAssets.detectiveDiscover
const detectiveSuccess = generatedRewardAssets.detectiveComplete
const detectiveWave = generatedRewardAssets.detectiveEncourage
const materialBadge = generatedRewardAssets.safetyObservationBadge
const materialBambooSlip = generatedMaterialIcons['bamboo-slip-shard']
const materialSafetyBell = generatedMaterialIcons['safety-bell']

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mapImage: {
    type: String,
    required: true
  },
  currentLevel: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['finish', 'skip'])

const introRef = ref(null)
const primaryButtonRef = ref(null)
const continueButtonRef = ref(null)
const stepIndex = ref(0)
const feedbackMessage = ref('')
const skipConfirmVisible = ref(false)
const isTransitioning = ref(false)
let feedbackTimer
let transitionTimer

const introSteps = [
  {
    id: 'welcome',
    tone: 'welcome',
    shortLabel: '认识地图',
    kicker: '杏林探案召集令',
    title: '小侦探，先找到发光的线索',
    text: '地图已经为你点亮。跟着小铜人观察路线，认出今天要调查的地方。',
    feedback: '地图已准备好，请找到发光的当前线索。',
    mapEyebrow: '第一步 · 认识地图',
    mapTitle: '寻找蓝色星光',
    mapHint: '星光会带你找到当前任务。',
    guideAlt: '小铜人侦探挥手欢迎'
  },
  {
    id: 'mission',
    tone: 'mission',
    shortLabel: '查看任务',
    kicker: '今日线索',
    title: '今天调查“失踪竹简案”',
    text: '先阅读故事寻找文化线索，再沿着地图继续认识足三里。',
    feedback: '当前任务已标记，完成后会点亮新的路线。',
    mapEyebrow: '第二步 · 查看任务',
    mapTitle: '确认当前任务',
    mapHint: '白色线索牌会显示任务名称。',
    guideAlt: '小铜人侦探指出地图线索'
  },
  {
    id: 'safety',
    tone: 'safe',
    shortLabel: '牢记安全',
    kicker: '侦探守则',
    title: '先把安全约定记牢',
    text: '我们学习针灸文化和穴位知识，但不在自己或他人的身体上尝试针刺。',
    feedback: '安全守则会一直显示在首页，忘记时随时查看。',
    mapEyebrow: '第三步 · 牢记安全',
    mapTitle: '认准安全铃',
    mapHint: '看到安全铃，就先阅读安全提醒。',
    guideAlt: '小铜人侦探提醒安全规则'
  },
  {
    id: 'start',
    tone: 'start',
    shortLabel: '开始探案',
    kicker: '线索袋准备好了',
    title: '准备完成，开始探索！',
    text: '地图、徽章和线索箱都已就绪。领取线索袋，去调查第一条线索吧。',
    feedback: '四项准备已完成，领取线索袋后即可进入地图。',
    mapEyebrow: '第四步 · 开始探案',
    mapTitle: '线索袋已就绪',
    mapHint: '完成任务可获得徽章和新线索。',
    guideAlt: '小铜人侦探完成引导'
  }
]

const activeStep = computed(() => introSteps[stepIndex.value])
const isLastStep = computed(() => stepIndex.value === introSteps.length - 1)
const visualFeedback = computed(
  () => feedbackMessage.value || activeStep.value.feedback
)
const primaryButtonLabel = computed(() => {
  if (isTransitioning.value && isLastStep.value) return '正在打开地图…'
  return isLastStep.value ? '领取线索袋' : '下一步'
})
const activeGuideImage = computed(() => {
  if (activeStep.value.tone === 'welcome') return detectiveWave
  if (activeStep.value.tone === 'start') return detectiveSuccess
  return detectivePoint
})
const activeFeedbackImage = computed(() => {
  if (activeStep.value.tone === 'safe') return materialSafetyBell
  return materialBambooSlip
})

function clearTimers() {
  window.clearTimeout(feedbackTimer)
  window.clearTimeout(transitionTimer)
}

function showFeedback(message) {
  window.clearTimeout(feedbackTimer)
  feedbackMessage.value = message
  feedbackTimer = window.setTimeout(() => {
    feedbackMessage.value = ''
  }, 2200)
}

function advanceIntro() {
  if (isTransitioning.value) return

  if (isLastStep.value) {
    isTransitioning.value = true
    feedbackMessage.value = '线索袋已领取，正在打开杏林地图。'
    transitionTimer = window.setTimeout(() => emit('finish'), 720)
    return
  }

  const completedLabel = activeStep.value.shortLabel
  isTransitioning.value = true
  stepIndex.value += 1
  showFeedback(`已完成“${completedLabel}”，进入“${activeStep.value.shortLabel}”。`)
  transitionTimer = window.setTimeout(() => {
    isTransitioning.value = false
  }, 320)
}

function previousStep() {
  if (stepIndex.value === 0 || isTransitioning.value) return
  stepIndex.value -= 1
  showFeedback(`已返回“${activeStep.value.shortLabel}”。`)
}

async function requestSkip() {
  if (skipConfirmVisible.value || isTransitioning.value) return
  skipConfirmVisible.value = true
  await nextTick()
  continueButtonRef.value?.focus()
}

async function cancelSkip() {
  skipConfirmVisible.value = false
  showFeedback('已继续引导，你的进度还在。')
  await nextTick()
  primaryButtonRef.value?.focus()
}

function confirmSkip() {
  if (isTransitioning.value) return
  isTransitioning.value = true
  skipConfirmVisible.value = false
  feedbackMessage.value = '已跳过引导，可从首页右上角再次打开。'
  transitionTimer = window.setTimeout(() => emit('skip'), 620)
}

function getFocusableElements() {
  const scope = skipConfirmVisible.value
    ? introRef.value?.querySelector('.home-intro__confirm')
    : introRef.value?.querySelector('.home-intro__stage')

  return Array.from(
    scope?.querySelectorAll('button:not([disabled])') || []
  ).filter((element) => element.offsetParent !== null)
}

function handleIntroKeydown(event) {
  if (event.key === 'Escape') {
    event.preventDefault()
    if (skipConfirmVisible.value) {
      cancelSkip()
      return
    }
    requestSkip()
    return
  }

  if (event.key !== 'Tab') return

  const focusableElements = getFocusableElements()
  if (!focusableElements.length) return

  const firstElement = focusableElements[0]
  const lastElement = focusableElements[focusableElements.length - 1]
  const activeElementIsInside = focusableElements.includes(document.activeElement)

  if (!activeElementIsInside) {
    event.preventDefault()
    const nextFocusTarget = event.shiftKey ? lastElement : firstElement
    nextFocusTarget.focus()
  } else if (event.shiftKey && document.activeElement === firstElement) {
    event.preventDefault()
    lastElement.focus()
  } else if (!event.shiftKey && document.activeElement === lastElement) {
    event.preventDefault()
    firstElement.focus()
  }
}

watch(
  () => props.visible,
  async (visible) => {
    if (!visible) {
      clearTimers()
      return
    }
    clearTimers()
    stepIndex.value = 0
    feedbackMessage.value = ''
    skipConfirmVisible.value = false
    isTransitioning.value = false
    await nextTick()
    introRef.value?.focus()
  }
)

onBeforeUnmount(clearTimers)
</script>

<style scoped>
.home-intro {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: grid;
  place-items: center;
  padding: 20px;
  overflow: auto;
  color: #3f2814;
  font-family: "Noto Sans SC", "Microsoft YaHei", sans-serif;
}

.home-intro__scrim {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(circle at 50% 42%, rgba(244, 203, 104, 0.2), transparent 40%),
    rgba(34, 24, 15, 0.72);
  backdrop-filter: blur(5px);
}

.home-intro__stage {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(340px, 0.85fr);
  grid-template-rows: auto minmax(0, 1fr);
  gap: 14px 18px;
  width: min(1180px, 100%);
  min-height: min(690px, calc(100dvh - 40px));
  padding: 18px;
  border: 2px solid rgba(239, 190, 78, 0.9);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 248, 228, 0.99), rgba(235, 204, 148, 0.98)),
    #f2d49a;
  box-shadow:
    0 30px 80px rgba(27, 17, 8, 0.46),
    inset 0 0 0 2px rgba(255, 255, 255, 0.56);
  overflow: hidden;
  isolation: isolate;
}

.home-intro__stage::before,
.home-intro__stage::after {
  content: "";
  position: absolute;
  top: 88px;
  bottom: 18px;
  z-index: -1;
  width: 18px;
  border-radius: 999px;
  background: linear-gradient(90deg, #8f4f1b, #e4ad4c 52%, #784016);
  box-shadow: inset 0 0 0 2px rgba(76, 39, 12, 0.2);
}

.home-intro__stage::before {
  left: 8px;
}

.home-intro__stage::after {
  right: 8px;
}

.home-intro__header {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  gap: 18px;
  min-height: 54px;
  padding: 0 8px 12px;
  border-bottom: 1px solid rgba(116, 72, 28, 0.2);
}

.home-intro__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.home-intro__brand img {
  width: 38px;
  height: 38px;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(70, 39, 14, 0.16));
}

.home-intro__brand span {
  display: grid;
  gap: 1px;
}

.home-intro__brand strong {
  color: #4b2b12;
  font-size: 17px;
  font-weight: 950;
}

.home-intro__brand small {
  color: #8c6337;
  font-size: 12px;
  font-weight: 800;
}

.home-intro__step-count {
  display: flex;
  align-items: baseline;
  gap: 7px;
  color: #72502b;
  font-size: 13px;
  font-weight: 800;
}

.home-intro__step-count strong {
  color: #3f2814;
  font-size: 16px;
  font-weight: 950;
}

.home-intro__skip {
  min-height: 38px;
  padding: 0 16px;
  border: 1px solid rgba(106, 64, 25, 0.28);
  border-radius: 999px;
  background: rgba(255, 251, 238, 0.72);
  color: #71491f;
  cursor: pointer;
  font-size: 14px;
  font-weight: 900;
  transition: transform 160ms ease, background 160ms ease, box-shadow 160ms ease;
}

.home-intro__map {
  position: relative;
  min-height: 570px;
  margin-left: 8px;
  border: 2px solid rgba(112, 70, 31, 0.38);
  border-radius: 18px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: 100% 100%;
  box-shadow:
    0 16px 30px rgba(70, 43, 18, 0.2),
    inset 0 0 0 1px rgba(255, 251, 232, 0.7);
  overflow: hidden;
  transform-origin: center;
  animation: intro-map-open 560ms ease both;
}

.home-intro__map-shade {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 48% 42%, transparent 0 13%, rgba(47, 31, 17, 0.08) 28%),
    linear-gradient(90deg, rgba(58, 37, 19, 0.12), transparent 24% 76%, rgba(58, 37, 19, 0.15));
  pointer-events: none;
}

.home-intro__map-label {
  position: absolute;
  top: 16px;
  left: 16px;
  display: grid;
  gap: 2px;
  max-width: 210px;
  padding: 10px 14px;
  border: 1px solid rgba(103, 61, 23, 0.26);
  border-radius: 12px;
  background: rgba(255, 249, 231, 0.9);
  box-shadow: 0 8px 18px rgba(65, 39, 16, 0.16);
}

.home-intro__map-label span {
  color: #8a5b29;
  font-size: 12px;
  font-weight: 900;
}

.home-intro__map-label strong {
  color: #482b13;
  font-size: 16px;
  font-weight: 950;
}

.home-intro__path {
  position: absolute;
  height: 10px;
  border-radius: 999px;
  background:
    linear-gradient(90deg, transparent, rgba(255, 224, 105, 0.95), transparent),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.92) 0 9px, transparent 9px 26px);
  box-shadow: 0 0 22px rgba(255, 211, 68, 0.68);
  opacity: 0.82;
  animation: intro-star-flow 2.2s linear infinite;
}

.home-intro__path--one {
  left: 15%;
  top: 27%;
  width: 49%;
  transform: rotate(4deg);
}

.home-intro__path--two {
  left: 23%;
  top: 46%;
  width: 38%;
  transform: rotate(-7deg);
}

.home-intro__path--three {
  left: 28%;
  top: 66%;
  width: 50%;
  transform: rotate(2deg);
}

.home-intro__mission-card {
  position: absolute;
  left: 46%;
  top: 38%;
  display: grid;
  grid-template-columns: 58px 1fr;
  align-items: center;
  min-width: 236px;
  padding: 10px 14px 10px 10px;
  border: 2px solid rgba(105, 65, 26, 0.36);
  border-radius: 13px;
  background: rgba(255, 251, 238, 0.96);
  box-shadow:
    0 12px 24px rgba(61, 37, 16, 0.18),
    0 0 0 8px rgba(85, 187, 225, 0.12);
  animation: intro-card-pop 500ms 180ms ease both;
}

.home-intro__mission-card img {
  grid-row: span 2;
  width: 54px;
  height: 54px;
  object-fit: contain;
}

.home-intro__mission-card > span:not(.home-intro__target-pulse) {
  color: #8a5624;
  font-size: 13px;
  font-weight: 900;
}

.home-intro__mission-card strong {
  color: #442812;
  font-size: 18px;
  font-weight: 950;
}

.home-intro__target-pulse {
  position: absolute;
  inset: -10px;
  z-index: -1;
  border: 3px solid rgba(79, 186, 226, 0.68);
  border-radius: 20px;
  animation: intro-target-pulse 1.8s ease-out infinite;
}

.home-intro__safety-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  display: grid;
  grid-template-columns: 32px auto;
  align-items: center;
  gap: 7px;
  padding: 8px 12px;
  border: 1px solid rgba(78, 115, 91, 0.3);
  border-radius: 999px;
  background: rgba(244, 255, 244, 0.92);
  box-shadow: 0 8px 16px rgba(48, 75, 53, 0.14);
  color: #3d674d;
  font-size: 13px;
  font-weight: 950;
}

.home-intro__safety-badge img {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.home-intro__map-tip {
  position: absolute;
  left: 18px;
  bottom: 18px;
  display: grid;
  gap: 3px;
  width: min(280px, calc(100% - 36px));
  padding: 13px 15px;
  border-left: 4px solid #e39a38;
  border-radius: 4px 12px 12px 4px;
  background: rgba(255, 250, 234, 0.94);
  box-shadow: 0 10px 22px rgba(67, 41, 17, 0.18);
}

.home-intro__map-tip span {
  color: #a06525;
  font-size: 12px;
  font-weight: 950;
}

.home-intro__map-tip strong {
  color: #452914;
  font-size: 18px;
  font-weight: 950;
}

.home-intro__map-tip small {
  color: #765432;
  font-size: 13px;
  font-weight: 800;
}

.home-intro__guide {
  position: absolute;
  left: calc(64% - 178px);
  bottom: 34px;
  z-index: 4;
  width: clamp(156px, 16vw, 210px);
  pointer-events: none;
  animation:
    intro-guide-enter 620ms 160ms ease both,
    intro-guide-float 3s 900ms ease-in-out infinite;
}

.home-intro__guide img {
  display: block;
  width: 100%;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 14px 14px rgba(55, 33, 14, 0.3));
}

.home-intro__glow {
  position: absolute;
  right: 10%;
  bottom: 5%;
  width: 72%;
  height: 18px;
  border-radius: 50%;
  background: rgba(83, 49, 21, 0.2);
  filter: blur(8px);
}

.home-intro__dialogue {
  position: relative;
  z-index: 3;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 570px;
  margin-right: 8px;
  padding: 26px 26px 22px;
  border: 2px solid rgba(121, 72, 28, 0.38);
  border-radius: 18px;
  background:
    radial-gradient(circle at 92% 0%, rgba(255, 225, 143, 0.7), transparent 31%),
    linear-gradient(180deg, rgba(255, 253, 242, 0.99), rgba(247, 228, 190, 0.98));
  box-shadow:
    0 16px 30px rgba(72, 45, 19, 0.17),
    inset 0 0 0 1px rgba(255, 255, 255, 0.76);
}

.home-intro__copy {
  animation: intro-copy-enter 320ms ease both;
}

.home-intro__kicker {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(72, 152, 187, 0.14);
  color: #35606d;
  font-size: 13px;
  font-weight: 950;
}

.home-intro__dialogue h2 {
  margin: 15px 0 10px;
  color: #3c2512;
  font-family: "STKaiti", "KaiTi", "Noto Serif SC", serif;
  font-size: clamp(27px, 2.3vw, 36px);
  line-height: 1.22;
  font-weight: 950;
  letter-spacing: 0.01em;
}

.home-intro__dialogue p {
  margin: 0;
  color: #684c2f;
  font-size: 16px;
  font-weight: 750;
  line-height: 1.75;
}

.home-intro__safe-rule {
  display: grid;
  grid-template-columns: 38px 1fr;
  align-items: center;
  gap: 9px;
  margin-top: 14px;
  padding: 10px 12px;
  border: 1px solid rgba(65, 139, 112, 0.38);
  border-radius: 11px;
  background: rgba(237, 255, 247, 0.82);
  color: #315e50;
}

.home-intro__safe-rule img {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.home-intro__safe-rule span {
  display: grid;
  gap: 2px;
}

.home-intro__safe-rule strong {
  font-size: 14px;
  font-weight: 950;
}

.home-intro__safe-rule small {
  font-size: 13px;
  font-weight: 800;
}

.home-intro__progress {
  margin-top: auto;
  padding-top: 20px;
}

.home-intro__progress ol {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.home-intro__progress li {
  position: relative;
  display: grid;
  justify-items: center;
  gap: 5px;
  min-width: 0;
  color: #9a8060;
  font-size: 11px;
  font-weight: 850;
  text-align: center;
}

.home-intro__progress li:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 14px;
  left: calc(50% + 17px);
  right: calc(-50% + 17px);
  height: 2px;
  background: rgba(128, 92, 51, 0.22);
}

.home-intro__progress li.complete:not(:last-child)::after {
  background: #76a365;
}

.home-intro__step-marker {
  position: relative;
  z-index: 1;
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border: 2px solid rgba(125, 88, 45, 0.28);
  border-radius: 50%;
  background: #f6e7c8;
  color: #8a6439;
  font-size: 13px;
  font-weight: 950;
  transition: transform 180ms ease, border-color 180ms ease, background 180ms ease;
}

.home-intro__step-marker img {
  width: 27px;
  height: 27px;
  object-fit: contain;
}

.home-intro__progress li.current {
  color: #4e3218;
}

.home-intro__progress li.current .home-intro__step-marker {
  border-color: #4d9ec2;
  background: #edfaff;
  color: #296d89;
  box-shadow: 0 0 0 5px rgba(77, 158, 194, 0.12);
  transform: translateY(-1px);
}

.home-intro__progress li.complete {
  color: #4e7245;
}

.home-intro__step-label {
  overflow: hidden;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.home-intro__feedback {
  display: grid;
  grid-template-columns: 30px minmax(0, 1fr);
  align-items: center;
  gap: 9px;
  min-height: 50px;
  margin-top: 16px;
  padding: 8px 11px;
  border: 1px solid rgba(177, 125, 52, 0.28);
  border-radius: 11px;
  background: rgba(255, 248, 226, 0.74);
  color: #75502a;
  font-size: 13px;
  font-weight: 850;
  line-height: 1.45;
  transition: background 180ms ease, border-color 180ms ease, color 180ms ease;
}

.home-intro__feedback img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.home-intro__feedback.is-success {
  border-color: rgba(85, 143, 77, 0.36);
  background: rgba(239, 253, 232, 0.86);
  color: #466b3e;
}

.home-intro__actions {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 10px;
  margin-top: 14px;
}

.home-intro__primary,
.home-intro__ghost,
.home-intro__confirm-skip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 950;
  transition:
    transform 150ms ease,
    box-shadow 150ms ease,
    background 150ms ease,
    opacity 150ms ease;
}

.home-intro__primary {
  display: grid;
  gap: 1px;
  min-height: 58px;
  padding: 8px 20px;
  border: 2px solid rgba(128, 70, 24, 0.34);
  background: linear-gradient(180deg, #ef9d42, #c96a24);
  box-shadow:
    inset 0 -5px 0 rgba(87, 45, 18, 0.16),
    0 9px 16px rgba(87, 46, 13, 0.2);
  color: #fff9e9;
}

.home-intro__primary > span {
  font-size: 17px;
}

.home-intro__primary small {
  color: rgba(255, 249, 233, 0.82);
  font-size: 11px;
  font-weight: 800;
}

.home-intro__ghost {
  min-width: 86px;
  min-height: 48px;
  padding: 0 15px;
  border: 1px solid rgba(111, 66, 31, 0.24);
  background: rgba(255, 252, 240, 0.72);
  color: #6b421d;
  font-size: 14px;
}

.home-intro__confirm-skip {
  min-height: 48px;
  padding: 0 18px;
  border: 1px solid rgba(135, 72, 32, 0.38);
  background: #8d5428;
  color: #fff8e6;
  font-size: 14px;
}

.home-intro__primary:hover:not(:disabled),
.home-intro__ghost:hover:not(:disabled),
.home-intro__skip:hover,
.home-intro__confirm-skip:hover:not(:disabled) {
  transform: translateY(-2px);
}

.home-intro__primary:hover:not(:disabled) {
  box-shadow:
    inset 0 -5px 0 rgba(87, 45, 18, 0.12),
    0 12px 20px rgba(87, 46, 13, 0.26);
}

.home-intro__skip:hover {
  background: rgba(255, 252, 240, 0.96);
  box-shadow: 0 7px 14px rgba(76, 45, 18, 0.12);
}

.home-intro__primary:active:not(:disabled),
.home-intro__ghost:active:not(:disabled),
.home-intro__skip:active,
.home-intro__confirm-skip:active:not(:disabled) {
  transform: translateY(1px);
}

.home-intro__primary:disabled,
.home-intro__ghost:disabled,
.home-intro__confirm-skip:disabled {
  cursor: not-allowed;
  opacity: 0.48;
  box-shadow: none;
}

.home-intro__primary:focus-visible,
.home-intro__ghost:focus-visible,
.home-intro__skip:focus-visible,
.home-intro__confirm-skip:focus-visible {
  outline: 3px solid rgba(65, 149, 190, 0.76);
  outline-offset: 3px;
}

.home-intro__stage--safe .home-intro__kicker {
  background: rgba(68, 157, 127, 0.16);
  color: #315e50;
}

.home-intro__stage--start .home-intro__kicker {
  background: rgba(224, 160, 55, 0.2);
  color: #76501e;
}

.home-intro__confirm {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(42, 28, 16, 0.54);
  backdrop-filter: blur(4px);
  animation: intro-confirm-enter 180ms ease both;
}

.home-intro__confirm-card {
  display: grid;
  justify-items: center;
  width: min(430px, 100%);
  padding: 28px;
  border: 2px solid rgba(231, 184, 86, 0.9);
  border-radius: 18px;
  background:
    radial-gradient(circle at 85% 0%, rgba(255, 220, 130, 0.58), transparent 32%),
    #fff8e5;
  box-shadow: 0 24px 60px rgba(31, 19, 8, 0.38);
  text-align: center;
}

.home-intro__confirm-card > img {
  width: 74px;
  height: 74px;
  margin-bottom: 8px;
  object-fit: contain;
}

.home-intro__confirm-card h3 {
  margin: 13px 0 8px;
  color: #3f2814;
  font-family: "STKaiti", "KaiTi", "Noto Serif SC", serif;
  font-size: 27px;
  font-weight: 950;
}

.home-intro__confirm-card p {
  max-width: 330px;
  margin: 0;
  color: #735437;
  font-size: 15px;
  font-weight: 750;
  line-height: 1.65;
}

.home-intro__confirm-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
  margin-top: 22px;
}

.is-transitioning .home-intro__copy {
  opacity: 0.84;
}

@keyframes intro-map-open {
  from {
    opacity: 0;
    transform: translateY(14px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes intro-star-flow {
  from {
    background-position: 0 50%, 0 50%;
  }
  to {
    background-position: 240px 50%, 160px 50%;
  }
}

@keyframes intro-card-pop {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.94);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes intro-target-pulse {
  0% {
    opacity: 0.8;
    transform: scale(0.96);
  }
  70%,
  100% {
    opacity: 0;
    transform: scale(1.1);
  }
}

@keyframes intro-guide-enter {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes intro-guide-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

@keyframes intro-copy-enter {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes intro-confirm-enter {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (max-width: 980px) {
  .home-intro {
    padding: 12px;
  }

  .home-intro__stage {
    grid-template-columns: 1fr;
    min-height: auto;
    max-height: calc(100dvh - 24px);
    overflow: auto;
  }

  .home-intro__map {
    min-height: 360px;
    margin: 0 8px;
  }

  .home-intro__dialogue {
    min-height: auto;
    margin: 0 8px 8px;
  }

  .home-intro__guide {
    left: auto;
    right: 34px;
    bottom: auto;
    top: 248px;
    width: 150px;
  }
}

@media (max-width: 640px) {
  .home-intro {
    padding: 0;
  }

  .home-intro__stage {
    width: 100%;
    min-height: 100dvh;
    max-height: none;
    padding: 12px;
    border: 0;
    border-radius: 0;
  }

  .home-intro__stage::before,
  .home-intro__stage::after {
    display: none;
  }

  .home-intro__header {
    grid-template-columns: 1fr auto;
    gap: 10px;
  }

  .home-intro__step-count {
    display: none;
  }

  .home-intro__brand small {
    display: none;
  }

  .home-intro__skip {
    padding: 0 12px;
  }

  .home-intro__map {
    min-height: 280px;
    margin: 0;
  }

  .home-intro__map-label {
    top: 10px;
    left: 10px;
    padding: 8px 10px;
  }

  .home-intro__safety-badge {
    top: 10px;
    right: 10px;
    grid-template-columns: 26px auto;
    padding: 6px 9px;
    font-size: 11px;
  }

  .home-intro__safety-badge img {
    width: 25px;
    height: 25px;
  }

  .home-intro__mission-card {
    left: 23%;
    top: 42%;
    min-width: 190px;
    grid-template-columns: 44px 1fr;
  }

  .home-intro__mission-card img {
    width: 42px;
    height: 42px;
  }

  .home-intro__mission-card strong {
    font-size: 15px;
  }

  .home-intro__map-tip {
    display: none;
  }

  .home-intro__guide {
    top: 235px;
    right: 12px;
    width: 105px;
  }

  .home-intro__dialogue {
    margin: 0;
    padding: 20px 17px 16px;
  }

  .home-intro__dialogue h2 {
    font-size: 27px;
  }

  .home-intro__dialogue p {
    font-size: 15px;
  }

  .home-intro__step-label {
    font-size: 10px;
  }

  .home-intro__actions {
    grid-template-columns: 92px minmax(0, 1fr);
  }

  .home-intro__confirm {
    padding: 16px;
  }

  .home-intro__confirm-card {
    padding: 24px 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-intro__map,
  .home-intro__path,
  .home-intro__mission-card,
  .home-intro__target-pulse,
  .home-intro__guide,
  .home-intro__copy,
  .home-intro__confirm {
    animation: none;
  }

  .home-intro__primary,
  .home-intro__ghost,
  .home-intro__skip,
  .home-intro__confirm-skip,
  .home-intro__step-marker,
  .home-intro__feedback {
    transition: none;
  }
}
</style>

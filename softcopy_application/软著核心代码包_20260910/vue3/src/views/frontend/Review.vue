<template>
  <main class="review-page">
    <section class="review-hero" aria-labelledby="review-title">
      <div class="review-hero__inner">
        <div class="review-hero__copy">
          <span class="review-hero__spark" aria-hidden="true"><i class="fa-solid fa-star"></i></span>
          <h1 id="review-title">星光修补册</h1>
          <p>把还没点亮的知识星星，再温柔地修补一次</p>
        </div>
        <img class="review-hero__mascot" :src="copperMascot" alt="小铜人侦探拿着放大镜">
      </div>
    </section>

    <section class="review-workspace" aria-label="星光修补任务">
      <aside class="task-book page-surface" aria-labelledby="task-list-title">
        <div class="book-links" aria-hidden="true">
          <i v-for="index in 4" :key="index" class="fa-solid fa-link"></i>
        </div>
        <header class="task-book__header">
          <h2 id="task-list-title">我的未点亮知识星</h2>
          <p>共 {{ records.length }} 个待修补</p>
        </header>

        <div class="task-list">
          <button
            v-for="record in records"
            :key="record.id"
            type="button"
            class="task-card"
            :class="{ active: activeRecord.id === record.id, repaired: repaired.includes(record.id) }"
            @click="selectRecord(record)"
          >
            <span class="task-card__icon" :class="record.iconTone">
              <i :class="record.icon"></i>
            </span>
            <span class="task-card__copy">
              <strong>{{ record.title }}</strong>
              <small>正确率 {{ record.accuracy }}%</small>
            </span>
            <i
              class="task-card__star"
              :class="repaired.includes(record.id) ? 'fa-solid fa-star' : 'fa-regular fa-star'"
              aria-hidden="true"
            ></i>
          </button>
        </div>

        <button class="outline-button" type="button" @click="showAll = true">
          <i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i>
          查看全部星星
          <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>
        </button>
      </aside>

      <section class="repair-book page-surface" aria-labelledby="repair-task-title">
        <div class="repair-ribbon">
          <i class="fa-solid fa-star" aria-hidden="true"></i>
          <h2 id="repair-task-title">修补任务：{{ activeRecord.title }}</h2>
          <i class="fa-solid fa-star" aria-hidden="true"></i>
        </div>

        <div class="repair-sheet">
          <header class="repair-sheet__header">
            <h3>再看一看，比一比，把知识星重新点亮</h3>
          </header>

          <div class="compare-row">
            <article class="compare-card">
              <h4>你之前选择的位置</h4>
              <div class="diagram-frame" :class="activeRecord.imageMode">
                <img :src="activeRecord.image" :alt="`${activeRecord.title}之前的选择示意`">
                <span class="point-marker point-marker--wrong" :style="activeRecord.wrongMarker" aria-hidden="true"></span>
              </div>
              <strong>{{ activeRecord.wrongTitle }}</strong>
              <p>{{ activeRecord.wrongDescription }}</p>
            </article>

            <div class="compare-badge" aria-hidden="true">
              <span>比一比</span>
              <i class="fa-solid fa-right-left"></i>
            </div>

            <article class="compare-card">
              <h4>正确的{{ activeRecord.title }}</h4>
              <div class="diagram-frame" :class="activeRecord.imageMode">
                <img :src="activeRecord.image" :alt="`${activeRecord.title}正确位置示意`">
                <span class="point-marker point-marker--right" :style="activeRecord.correctMarker" aria-hidden="true"></span>
              </div>
              <strong class="success-copy">{{ activeRecord.rightTitle }}</strong>
              <p>{{ activeRecord.rightDescription }}</p>
            </article>

            <aside class="tip-note">
              <div class="tip-note__title">
                <img :src="copperMascot" alt="">
                <strong>小铜人提示</strong>
              </div>
              <p>{{ activeRecord.tip }}</p>
            </aside>
          </div>

          <section class="mini-practice" aria-labelledby="practice-title">
            <h4 id="practice-title">
              <i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i>
              修补小练习：
              <span>{{ activeRecord.practice }}</span>
            </h4>
            <div class="practice-row">
              <div class="practice-target">
                <div class="hotspot-map" :class="activeRecord.imageMode">
                  <img :src="activeRecord.image" :alt="`${activeRecord.title}练习示意图`">
                  <button
                    v-for="spot in activeRecord.hotspots"
                    :key="spot.id"
                    type="button"
                    class="hotspot"
                    :class="{ selected: selectedSpot === spot.id }"
                    :style="spot.style"
                    :aria-label="spot.label"
                    @click="selectedSpot = spot.id"
                  ></button>
                </div>
                <p>点击图中合适的位置<br>{{ activeRecord.practiceHint }}</p>
              </div>
              <i class="practice-arrow fa-solid fa-arrow-right" aria-hidden="true"></i>
              <div class="star-result" aria-live="polite">
                <span>重新点亮星星</span>
                <div class="star-result__stars">
                  <i
                    v-for="index in 5"
                    :key="index"
                    class="fa-solid fa-star"
                    :class="{ lit: lastResult === 'success' && index <= 5 }"
                  ></i>
                </div>
              </div>
            </div>
          </section>

          <button class="primary-button" type="button" @click="startRepair">
            开始修补
            <i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i>
          </button>
          <p class="reward-caption">完成后可立即获得奖励</p>
        </div>
      </section>

      <aside class="review-sidebar" aria-label="修补信息">
        <section class="sidebar-card progress-card">
          <h2>修补进度</h2>
          <div class="progress-card__content">
            <div
              class="progress-ring"
              role="progressbar"
              :aria-valuenow="repaired.length"
              aria-valuemin="0"
              :aria-valuemax="totalStars"
            >
              <strong>{{ repaired.length }}/{{ totalStars }}</strong>
              <span>已修补知识星</span>
            </div>
            <dl>
              <div><dt>全部</dt><dd>{{ totalStars }} 颗</dd></div>
              <div><dt>已修补</dt><dd>{{ repaired.length }} 颗</dd></div>
              <div><dt>待修补</dt><dd>{{ totalStars - repaired.length }} 颗</dd></div>
            </dl>
          </div>
        </section>

        <section class="sidebar-card reward-card">
          <h2>本次奖励</h2>
          <div class="reward-grid">
            <div class="reward-item">
              <img :src="rewardStar" alt="经络地图线索">
              <span><strong>经络地图线索</strong><small>× 1</small></span>
            </div>
          </div>
        </section>

        <section class="sidebar-card recommend-card">
          <h2>智能推荐复习任务</h2>
          <p>为你智能安排的修补任务</p>
          <div class="recommend-list">
            <div v-for="record in recommendations" :key="record.id" class="recommend-item">
              <span class="recommend-item__icon" :class="record.iconTone">
                <i :class="record.icon"></i>
              </span>
              <span class="recommend-item__copy">
                <strong>{{ record.recommendTitle }}</strong>
                <small>正确率 {{ record.accuracy }}%</small>
              </span>
              <button type="button" @click="selectRecord(record)">去修补</button>
            </div>
          </div>
          <button class="shuffle-button" type="button" @click="shuffleRecommendations">
            换一批任务
            <i class="fa-solid fa-rotate" aria-hidden="true"></i>
          </button>
        </section>
      </aside>
    </section>

    <Teleport to="body">
      <div v-if="showAll" class="review-modal-mask" @click.self="showAll = false">
        <section class="review-modal" role="dialog" aria-modal="true" aria-labelledby="all-stars-title">
          <button class="review-modal__close" type="button" aria-label="关闭" @click="showAll = false">
            <i class="fa-solid fa-xmark"></i>
          </button>
          <h2 id="all-stars-title">全部知识星</h2>
          <p>从薄弱知识点开始，一颗一颗重新点亮。</p>
          <div class="all-star-grid">
            <button
              v-for="record in allRecords"
              :key="record.id"
              type="button"
              @click="selectRecord(record); showAll = false"
            >
              <i :class="record.icon"></i>
              <strong>{{ record.title }}</strong>
              <small>{{ repaired.includes(record.id) ? '已点亮' : `正确率 ${record.accuracy}%` }}</small>
            </button>
          </div>
        </section>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useGameState } from '@/composables/useGameState'
import copperMascot from '@/assets/characters/copper-detective-guide-512.png'
import bodyMap from '@/assets/maps/body-map-navigation-child-clean.png'
import meridianMap from '@/assets/maps/xinglin-detective-map-bg.png'
import legAnatomy from '@/assets/review/leg-anatomy-gray261.png'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'

const safetyBell = generatedMaterialIcons['safety-bell']
const rewardStar = generatedRewardAssets.meridianMapClue

const records = [
  {
    id: 'zusanli',
    title: '足三里位置',
    recommendTitle: '穴位位置辨认',
    accuracy: 40,
    icon: 'fa-solid fa-child-reaching',
    iconTone: 'tone-gold',
    image: legAnatomy,
    imageMode: 'image-mode-anatomy',
    wrongTitle: '在更靠下的位置',
    wrongDescription: '离膝盖有些远哦',
    rightTitle: '膝盖下三寸，胫骨外侧一横指',
    rightDescription: '常按这里，强身又健脾',
    tip: '数自己四个手指的宽度，从膝盖下缘开始，胫骨外侧一横指处，就是足三里哦！',
    practice: '请标出足三里的正确位置',
    practiceHint: '标出足三里',
    wrongMarker: { left: '48%', top: '78%' },
    correctMarker: { left: '43%', top: '67%' },
    correctSpot: 'middle',
    hotspots: [
      { id: 'upper', label: '选择膝盖附近', style: { left: '43%', top: '24%' } },
      { id: 'middle', label: '选择膝盖下方', style: { left: '43%', top: '55%' } },
      { id: 'lower', label: '选择小腿下方', style: { left: '48%', top: '86%' } }
    ]
  },
  {
    id: 'meridian',
    title: '经络归属',
    recommendTitle: '经络循行方向',
    accuracy: 55,
    icon: 'fa-solid fa-route',
    iconTone: 'tone-teal',
    image: meridianMap,
    imageMode: 'image-mode-map',
    wrongTitle: '只看到了一个穴位',
    wrongDescription: '还要观察它所在的路线',
    rightTitle: '足三里属于足阳明胃经',
    rightDescription: '经络把一颗颗穴位连成路线',
    tip: '穴位像星星，经络像星星之间的路线。足三里就在足阳明胃经这条路线上。',
    practice: '请找到经络延伸的正确方向',
    practiceHint: '沿着星路寻找',
    wrongMarker: { left: '35%', top: '70%' },
    correctMarker: { left: '62%', top: '45%' },
    correctSpot: 'middle',
    hotspots: [
      { id: 'upper', label: '选择左侧路线', style: { left: '35%', top: '70%' } },
      { id: 'middle', label: '选择中间路线', style: { left: '62%', top: '45%' } },
      { id: 'lower', label: '选择右侧路线', style: { left: '78%', top: '66%' } }
    ]
  },
  {
    id: 'body',
    title: '身体区域判断',
    recommendTitle: '身体区域判断',
    accuracy: 60,
    icon: 'fa-solid fa-person',
    iconTone: 'tone-coral',
    image: bodyMap,
    imageMode: 'image-mode-body-full',
    wrongTitle: '选择了大腿区域',
    wrongDescription: '足三里其实更靠下',
    rightTitle: '足三里位于小腿前外侧',
    rightDescription: '先认身体区域，再寻找穴位',
    tip: '先找到膝盖，再往下看小腿。把身体分区记清楚，找穴位会更容易。',
    practice: '请选出正确的小腿区域',
    practiceHint: '指出小腿区域',
    wrongMarker: { left: '43%', top: '55%' },
    correctMarker: { left: '43%', top: '72%' },
    correctSpot: 'middle',
    hotspots: [
      { id: 'upper', label: '选择大腿区域', style: { left: '43%', top: '22%' } },
      { id: 'middle', label: '选择小腿区域', style: { left: '43%', top: '55%' } },
      { id: 'lower', label: '选择脚部区域', style: { left: '43%', top: '88%' } }
    ]
  },
  {
    id: 'safety',
    title: '安全规则复习',
    recommendTitle: '穴位功能记忆',
    accuracy: 50,
    icon: 'fa-solid fa-shield-heart',
    iconTone: 'tone-blue',
    image: safetyBell,
    imageMode: 'image-mode-icon',
    wrongTitle: '想自己拿针尝试',
    wrongDescription: '这是不安全的做法',
    rightTitle: '只观察、触摸和学习',
    rightDescription: '针刺必须交给专业医生',
    tip: '学习穴位可以用手指轻轻触摸，但绝对不能自己拿针或尖锐物品尝试。',
    practice: '请选择安全的学习方式',
    practiceHint: '选择绿色安全点',
    wrongMarker: { left: '38%', top: '56%' },
    correctMarker: { left: '62%', top: '56%' },
    correctSpot: 'middle',
    hotspots: [
      { id: 'upper', label: '不安全方式一', style: { left: '32%', top: '56%' } },
      { id: 'middle', label: '安全学习方式', style: { left: '62%', top: '56%' } },
      { id: 'lower', label: '不安全方式二', style: { left: '82%', top: '56%' } }
    ]
  }
]

const extraRecords = [
  { ...records[0], id: 'neiguan', title: '内关穴位置', recommendTitle: '内关穴位置', accuracy: 45, icon: 'fa-solid fa-hand' },
  { ...records[1], id: 'flow', title: '经络循行方向', recommendTitle: '经络循行方向', accuracy: 50 },
  { ...records[2], id: 'function', title: '穴位功能记忆', recommendTitle: '穴位功能记忆', accuracy: 48, icon: 'fa-solid fa-brain' },
  { ...records[3], id: 'boundary', title: '学习边界判断', recommendTitle: '学习边界判断', accuracy: 62 }
]

const allRecords = [...records, ...extraRecords]
const totalStars = allRecords.length
const activeRecord = ref(records[0])
const { completeStandaloneTask, isCompleted } = useGameState()
const repaired = ref([])
const selectedSpot = ref(null)
const lastResult = ref('')
const recommendationOffset = ref(0)
const showAll = ref(false)

const recommendations = computed(() => {
  const list = extraRecords
  return Array.from({ length: 3 }, (_, index) => list[(index + recommendationOffset.value) % list.length])
})

function selectRecord(record) {
  activeRecord.value = record
  selectedSpot.value = null
  lastResult.value = repaired.value.includes(record.id) ? 'success' : ''
  window.scrollTo({ top: 120, behavior: 'smooth' })
}

async function startRepair() {
  if (!selectedSpot.value) {
    message.info('先在练习图中选择一个位置')
    return
  }
  if (selectedSpot.value !== activeRecord.value.correctSpot) {
    lastResult.value = 'retry'
    message.warning('还差一点，再对照上面的提示看一看')
    return
  }
  if (!repaired.value.includes(activeRecord.value.id)) {
    const result = await completeStandaloneTask('review-daily', [{ id: 'star-compass', count: 1 }], {
      resultCode: 'REVIEW_COMPLETE',
      idempotencyKey: `review:${activeRecord.value.id}`
    })
    if (!result.success && !result.alreadyCompleted) {
      message.error('修补奖励暂时未确认，请重试')
      return
    }
    repaired.value.push(activeRecord.value.id)
  }
  lastResult.value = 'success'
  message.success('修补成功！经络地图线索已到账')
}

function shuffleRecommendations() {
  recommendationOffset.value = (recommendationOffset.value + 1) % extraRecords.length
}

onMounted(() => {
  document.body.classList.add('review-active')
  if (isCompleted('review-daily')) repaired.value = [records[0].id]
})

onUnmounted(() => {
  document.body.classList.remove('review-active')
})
</script>

<style scoped>
.review-page {
  --ink: #4d341b;
  --muted: #806947;
  --paper: #fffaf0;
  --paper-deep: #f8e9c6;
  --gold: #d89a3b;
  --gold-deep: #b96b1f;
  --jade: #286b58;
  min-height: calc(100vh - 56px);
  padding-bottom: 26px;
  position: relative;
  isolation: isolate;
  color: var(--ink);
  background-color: #f6e7bf;
  font-family: "Noto Serif SC", "Songti SC", "STSong", serif;
}

.review-page::before {
  position: fixed;
  inset: 68px 0 0;
  z-index: -1;
  content: "";
  background-image: url('@/assets/maps/xinglin-detective-map-bg.png');
  background-position: center top;
  background-size: cover;
  opacity: 0.42;
}

button {
  font: inherit;
}

.review-hero {
  height: 164px;
  overflow: hidden;
  border-bottom: 1px solid rgba(126, 84, 35, 0.22);
  background: rgba(255, 246, 218, 0.72);
}

.review-hero__inner {
  position: relative;
  display: flex;
  width: min(1677px, 100%);
  height: 100%;
  margin: 0 auto;
  align-items: center;
}

.review-hero__copy {
  position: absolute;
  left: clamp(260px, 24vw, 402px);
  z-index: 1;
  width: 390px;
  text-align: center;
}

.review-hero__copy h1 {
  margin: 0;
  color: #1e5b42;
  font-size: clamp(40px, 4vw, 55px);
  font-weight: 950;
  letter-spacing: 0.08em;
  line-height: 1.08;
  text-shadow: 0 2px 0 rgba(255, 255, 255, 0.55);
}

.review-hero__copy p {
  margin: 13px 0 0;
  color: #246449;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.review-hero__spark {
  position: absolute;
  top: 8px;
  left: -48px;
  color: #e9b64f;
  font-size: 25px;
  transform: rotate(-12deg);
}

.review-hero__mascot {
  position: absolute;
  right: auto;
  left: clamp(650px, 48vw, 805px);
  bottom: -66px;
  width: 190px;
  height: 230px;
  object-fit: contain;
  filter: sepia(0.22) saturate(0.95) drop-shadow(0 8px 10px rgba(81, 48, 16, 0.18));
}

.review-workspace {
  position: relative;
  display: grid;
  grid-template-columns: 320px minmax(640px, 1fr) 408px;
  gap: 14px;
  width: min(1552px, calc(100% - 68px));
  margin: 0 0 0 clamp(22px, 2.4vw, 40px);
  align-items: start;
}

.page-surface,
.sidebar-card {
  border: 1px solid rgba(171, 116, 46, 0.34);
  background: rgba(255, 250, 237, 0.95);
  box-shadow: 0 12px 30px rgba(75, 49, 19, 0.14);
}

.task-book,
.repair-book {
  min-height: 690px;
  border-top: 8px solid #496f5d;
  border-bottom: 8px solid #496f5d;
}

.task-book {
  position: relative;
  padding: 26px 28px;
  border-left: 9px solid #496f5d;
  border-radius: 16px 8px 8px 16px;
}

.repair-book {
  position: relative;
  padding: 22px 26px 26px;
  border-right: 9px solid #496f5d;
  border-radius: 8px 16px 16px 8px;
}

.book-links {
  position: absolute;
  top: 70px;
  right: -15px;
  z-index: 4;
  display: flex;
  height: calc(100% - 128px);
  flex-direction: column;
  justify-content: space-between;
  color: #c7933b;
  font-size: 25px;
  transform: rotate(-24deg);
  filter: drop-shadow(0 2px 1px rgba(83, 50, 16, 0.24));
}

.task-book__header {
  text-align: center;
}

.task-book__header h2,
.sidebar-card h2 {
  margin: 0;
  color: #52371d;
  font-size: 20px;
  font-weight: 950;
}

.task-book__header p {
  margin: 7px 0 16px;
  font-family: "Microsoft YaHei", sans-serif;
  font-weight: 700;
}

.task-list {
  display: grid;
  gap: 12px;
}

.task-card {
  display: grid;
  min-height: 86px;
  grid-template-columns: 54px 1fr 24px;
  gap: 12px;
  padding: 12px 13px;
  align-items: center;
  border: 1px solid rgba(177, 124, 55, 0.28);
  border-radius: 12px;
  background: rgba(255, 253, 246, 0.82);
  color: var(--ink);
  cursor: pointer;
  text-align: left;
  transition: border-color 160ms ease, box-shadow 160ms ease, transform 160ms ease;
}

.task-card:hover,
.task-card:focus-visible {
  border-color: #d99b3f;
  box-shadow: 0 7px 16px rgba(131, 82, 26, 0.11);
  outline: none;
  transform: translateY(-1px);
}

.task-card.active {
  border-color: #d58e2a;
  background: #fff2c9;
  box-shadow: inset 0 0 0 1px rgba(222, 155, 55, 0.16);
}

.task-card__icon,
.recommend-item__icon {
  display: grid;
  place-items: center;
  border-radius: 10px;
}

.task-card__icon {
  width: 54px;
  height: 54px;
  font-size: 28px;
}

.tone-gold { color: #c97820; background: #fff0c7; }
.tone-teal { color: #28786a; background: #dff2eb; }
.tone-coral { color: #b45c42; background: #f7e4d8; }
.tone-blue { color: #356c9d; background: #e1edf8; }

.task-card__copy {
  display: grid;
  gap: 5px;
}

.task-card__copy strong {
  font-size: 17px;
}

.task-card__copy small,
.recommend-item__copy small {
  color: #735d3e;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 13px;
}

.task-card__star {
  color: #d6c9ad;
  font-size: 22px;
}

.task-card.repaired .task-card__star {
  color: #e4b24c;
}

.outline-button {
  display: flex;
  width: 100%;
  min-height: 52px;
  margin-top: 48px;
  padding: 0 18px;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d6a253;
  border-radius: 12px;
  background: #fff6dc;
  color: #8b531b;
  font-weight: 900;
  cursor: pointer;
}

.repair-ribbon {
  display: flex;
  width: min(510px, 80%);
  min-height: 38px;
  margin: -4px auto 16px;
  padding: 0 24px;
  align-items: center;
  justify-content: center;
  gap: 14px;
  border: 2px solid #d5a04e;
  border-radius: 999px;
  background: #fff2ca;
  color: #83501f;
  box-shadow: 0 4px 10px rgba(103, 62, 18, 0.14);
}

.repair-ribbon h2 {
  margin: 0;
  font-size: 19px;
}

.repair-ribbon i {
  color: #e9af3f;
  font-size: 12px;
}

.repair-sheet {
  min-height: 596px;
  padding: 16px 22px 12px;
  border: 1px solid rgba(177, 124, 55, 0.22);
  border-radius: 14px 38px 14px 14px;
  background: rgba(255, 253, 247, 0.88);
  box-shadow: 0 8px 18px rgba(77, 50, 17, 0.07);
}

.repair-sheet__header h3 {
  margin: 0 0 12px;
  text-align: center;
  font-size: 18px;
}

.compare-row {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) 54px minmax(160px, 1fr) minmax(140px, 0.85fr);
  gap: 10px;
  align-items: center;
}

.compare-card {
  min-width: 0;
  padding: 12px;
  text-align: center;
  border: 1px solid rgba(174, 125, 62, 0.25);
  border-radius: 12px;
  background: #fffdf8;
  box-shadow: 0 6px 12px rgba(80, 51, 18, 0.07);
}

.compare-card h4 {
  margin: 0 0 8px;
  font-size: 14px;
}

.compare-card strong {
  display: block;
  margin-top: 7px;
  font-size: 13px;
}

.compare-card p {
  margin: 3px 0 0;
  color: #856d4d;
  font-size: 12px;
  line-height: 1.45;
}

.compare-card .success-copy {
  color: #2c6d55;
}

.diagram-frame,
.hotspot-map {
  position: relative;
  overflow: hidden;
  border: 1px solid #eadbc0;
  background: #fbf8ef;
}

.diagram-frame {
  height: 182px;
}

.diagram-frame img,
.hotspot-map img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-mode-body img {
  height: 185%;
  object-position: center 91%;
  transform: translateY(-40%);
}

.image-mode-body-full img {
  object-fit: contain;
}

.image-mode-anatomy img {
  padding: 6px;
  object-fit: contain;
  filter: sepia(0.4) saturate(0.65) contrast(0.88);
}

.image-mode-map img {
  object-position: center;
}

.image-mode-icon img {
  padding: 18px;
  object-fit: contain;
}

.point-marker {
  position: absolute;
  width: 14px;
  height: 14px;
  border: 2px solid #fff5dd;
  border-radius: 50%;
  box-shadow: 0 2px 5px rgba(72, 37, 7, 0.25);
  transform: translate(-50%, -50%);
}

.point-marker--wrong { background: #e95136; }
.point-marker--right { background: #5aa734; }

.compare-badge {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 50%;
  background: #fff1c4;
  color: #9a5e1c;
  font-family: "Microsoft YaHei", sans-serif;
  box-shadow: 0 4px 10px rgba(96, 56, 15, 0.12);
}

.compare-badge span {
  margin-top: 6px;
  font-size: 13px;
  font-weight: 900;
}

.compare-badge i {
  margin-top: -6px;
  font-size: 12px;
}

.tip-note {
  min-height: 170px;
  padding: 12px;
  border: 1px solid #d4ad67;
  border-radius: 8px 8px 18px 8px;
  background: #fff2cd;
  box-shadow: 0 7px 12px rgba(76, 44, 13, 0.12);
  transform: rotate(1.5deg);
}

.tip-note__title {
  display: flex;
  align-items: center;
  gap: 7px;
}

.tip-note__title img {
  width: 38px;
  height: 38px;
  object-fit: contain;
}

.tip-note p {
  margin: 8px 0 0;
  color: #694820;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.7;
}

.mini-practice {
  margin-top: 14px;
  padding: 0 14px 12px;
  border: 1px solid rgba(210, 165, 92, 0.34);
  border-radius: 12px;
  background: rgba(255, 249, 232, 0.72);
}

.mini-practice h4 {
  display: inline-block;
  margin: -10px 0 8px;
  padding: 0 8px;
  background: #fffaf0;
  font-size: 14px;
}

.mini-practice h4 i {
  margin-right: 6px;
  color: #e8b54d;
}

.mini-practice h4 span {
  margin-left: 8px;
  color: #6e5a3e;
  font-weight: 600;
}

.practice-row {
  display: grid;
  grid-template-columns: 1fr 42px 1fr;
  gap: 14px;
  align-items: center;
}

.practice-target,
.star-result {
  display: flex;
  min-height: 82px;
  padding: 8px 12px;
  align-items: center;
  border: 1px solid #ead8b8;
  border-radius: 12px;
  background: #fffdf7;
}

.hotspot-map {
  width: 96px;
  height: 82px;
  flex: 0 0 auto;
  border: none;
}

.hotspot-map.image-mode-body img {
  height: 210%;
  transform: translateY(-45%);
}

.hotspot {
  position: absolute;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 2px solid rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  background: rgba(199, 146, 53, 0.38);
  box-shadow: 0 0 0 1px rgba(134, 81, 22, 0.25);
  cursor: pointer;
  transform: translate(-50%, -50%);
}

.hotspot:hover,
.hotspot:focus-visible,
.hotspot.selected {
  background: #db8b29;
  box-shadow: 0 0 0 4px rgba(219, 139, 41, 0.2);
  outline: none;
}

.practice-target p {
  margin: 0 auto;
  color: #705a3c;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.7;
  text-align: center;
}

.practice-arrow {
  color: #d39634;
  font-size: 24px;
  text-align: center;
}

.star-result {
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  color: #725b3d;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 12px;
  font-weight: 700;
}

.star-result__stars {
  display: flex;
  gap: 7px;
  color: #d8d0c0;
  font-size: 22px;
}

.star-result__stars .lit {
  color: #e5ad3f;
  filter: drop-shadow(0 2px 2px rgba(125, 73, 14, 0.18));
}

.primary-button,
.shuffle-button {
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  color: #fffaf0;
  font-family: "Microsoft YaHei", sans-serif;
  font-weight: 900;
  cursor: pointer;
  box-shadow: 0 7px 14px rgba(104, 56, 17, 0.2);
}

.primary-button {
  min-width: 250px;
  min-height: 50px;
  margin: 12px auto 0;
  gap: 12px;
  border-radius: 999px;
  background: #db7d22;
  font-size: 23px;
}

.primary-button:hover,
.primary-button:focus-visible {
  background: #c86d17;
  outline: 3px solid rgba(218, 126, 34, 0.24);
}

.reward-caption {
  margin: 5px 0 0;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

.review-sidebar {
  display: grid;
  gap: 12px;
  padding-top: 0;
}

.sidebar-card {
  padding: 18px 22px;
  border-radius: 16px;
}

.progress-card__content {
  display: grid;
  grid-template-columns: 148px 1fr;
  gap: 18px;
  margin-top: 10px;
  align-items: center;
}

.progress-ring {
  display: grid;
  width: 118px;
  height: 118px;
  margin: 0 auto;
  place-content: center;
  border: 12px solid #efe0bd;
  border-top-color: #3f8c4e;
  border-right-color: #71aa4c;
  border-radius: 50%;
  text-align: center;
}

.progress-ring strong {
  font-size: 31px;
  line-height: 1;
}

.progress-ring span {
  margin-top: 7px;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 11px;
}

.progress-card dl {
  display: grid;
  gap: 12px;
  margin: 0;
  font-family: "Microsoft YaHei", sans-serif;
  font-weight: 700;
}

.progress-card dl div {
  display: flex;
  justify-content: space-between;
}

.progress-card dt,
.progress-card dd {
  margin: 0;
}

.reward-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 12px;
}

.reward-item {
  display: flex;
  min-height: 64px;
  padding: 8px 10px;
  align-items: center;
  gap: 10px;
  border: 1px solid rgba(181, 125, 47, 0.22);
  border-radius: 12px;
  background: rgba(255, 252, 242, 0.84);
}

.reward-item img {
  width: 46px;
  height: 46px;
  object-fit: contain;
}

.reward-item span {
  display: grid;
  gap: 2px;
  font-family: "Microsoft YaHei", sans-serif;
}

.reward-item small {
  color: #3f2e1c;
  font-weight: 700;
}

.recommend-card > p {
  margin: 4px 0 10px;
  color: #765f43;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 14px;
}

.recommend-list {
  display: grid;
  gap: 7px;
}

.recommend-item {
  display: grid;
  grid-template-columns: 44px 1fr 78px;
  gap: 10px;
  align-items: center;
}

.recommend-item__icon {
  width: 44px;
  height: 44px;
  font-size: 20px;
}

.recommend-item__copy {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.recommend-item__copy strong {
  overflow: hidden;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-item button {
  height: 36px;
  border: 1px solid #dfb775;
  border-radius: 999px;
  background: #fff7e5;
  color: #7b4b1c;
  font-weight: 900;
  cursor: pointer;
}

.shuffle-button {
  width: 76%;
  min-height: 50px;
  margin: 14px auto 0;
  gap: 10px;
  border-radius: 999px;
  background: #37765d;
  font-size: 18px;
}

.shuffle-button:hover,
.shuffle-button:focus-visible {
  background: #286349;
  outline: 3px solid rgba(55, 118, 93, 0.2);
}

.review-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 2100;
  display: grid;
  padding: 24px;
  place-items: center;
  background: rgba(47, 31, 14, 0.44);
  backdrop-filter: blur(4px);
}

.review-modal {
  position: relative;
  width: min(720px, 100%);
  padding: 30px;
  border: 2px solid #d2a35b;
  border-radius: 22px;
  background: #fffaf0;
  box-shadow: 0 26px 60px rgba(47, 31, 14, 0.3);
}

.review-modal h2 {
  margin: 0;
  color: #1e5b42;
  font-size: 28px;
}

.review-modal > p {
  margin: 7px 0 20px;
  color: #755e42;
}

.review-modal__close {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 50%;
  background: #f4e7cb;
  color: #744d24;
  cursor: pointer;
}

.all-star-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.all-star-grid button {
  display: grid;
  grid-template-columns: 38px 1fr;
  gap: 4px 10px;
  padding: 14px;
  border: 1px solid #ead3aa;
  border-radius: 14px;
  background: #fffdf7;
  color: #50371f;
  cursor: pointer;
  text-align: left;
}

.all-star-grid button i {
  grid-row: span 2;
  align-self: center;
  color: #c9852f;
  font-size: 24px;
  text-align: center;
}

.all-star-grid button small {
  color: #806b4e;
}

@media (max-width: 1380px) {
  .review-workspace {
    grid-template-columns: 280px minmax(580px, 1fr) 330px;
    width: min(1328px, calc(100% - 28px));
  }

  .task-book {
    padding-inline: 20px;
  }

  .repair-book {
    padding-inline: 18px;
  }

  .repair-sheet {
    padding-inline: 15px;
  }

  .compare-row {
    grid-template-columns: minmax(150px, 1fr) 44px minmax(150px, 1fr);
  }

  .tip-note {
    grid-column: 1 / -1;
    min-height: auto;
    transform: none;
  }
}

@media (max-width: 1080px) {
  .review-workspace {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .review-sidebar {
    grid-column: 1 / -1;
    grid-template-columns: repeat(3, 1fr);
  }

  .task-book,
  .repair-book {
    min-height: auto;
  }
}

@media (max-width: 760px) {
  .review-hero {
    height: 150px;
  }

  .review-hero__inner {
    justify-content: flex-start;
  }

  .review-hero__copy {
    position: relative;
    left: auto;
    width: auto;
    text-align: left;
  }

  .review-hero__copy h1 {
    font-size: 37px;
  }

  .review-hero__copy p {
    width: 230px;
    font-size: 14px;
  }

  .review-hero__spark {
    display: none;
  }

  .review-hero__mascot {
    left: auto;
    right: -10px;
    width: 145px;
  }

  .review-workspace {
    display: block;
    width: calc(100% - 18px);
  }

  .task-book,
  .repair-book {
    margin-bottom: 12px;
    border: 5px solid #496f5d;
    border-radius: 14px;
  }

  .book-links {
    display: none;
  }

  .task-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .task-card {
    grid-template-columns: 42px 1fr;
    min-height: 72px;
    padding: 9px;
  }

  .task-card__icon {
    width: 42px;
    height: 42px;
    font-size: 20px;
  }

  .task-card__star {
    display: none;
  }

  .outline-button {
    margin-top: 18px;
  }

  .repair-book {
    padding: 16px 10px;
  }

  .repair-ribbon {
    width: 94%;
    padding-inline: 10px;
  }

  .repair-ribbon h2 {
    font-size: 15px;
  }

  .repair-sheet {
    padding-inline: 10px;
  }

  .compare-row {
    grid-template-columns: 1fr 34px 1fr;
  }

  .compare-card {
    padding: 8px;
  }

  .compare-card h4 {
    min-height: 38px;
  }

  .diagram-frame {
    height: 146px;
  }

  .compare-badge {
    width: 34px;
    height: 34px;
  }

  .compare-badge span {
    display: none;
  }

  .practice-row {
    grid-template-columns: 1fr;
  }

  .practice-arrow {
    transform: rotate(90deg);
  }

  .review-sidebar {
    display: grid;
    grid-template-columns: 1fr;
    margin-bottom: 20px;
  }

  .all-star-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<style>
body.review-active .xinglin-navbar {
  height: 68px;
}

body.review-active .frontend-layout .main-content {
  min-height: calc(100vh - 68px);
  margin-top: 68px;
}

body.review-active .backpack-entry {
  top: 92px;
  right: 22px;
}

body.review-active .mailbox-fab-entry {
  top: 148px;
  right: 22px;
}

body.review-active .robot-fab-entry {
  top: 204px;
  right: 22px;
}

body.review-active .backpack-fab,
body.review-active .mailbox-fab,
body.review-active .robot-fab {
  min-width: 126px;
  height: 44px;
  border-color: rgba(115, 91, 58, 0.12);
  border-radius: 999px;
  background: #fff;
  color: #3e3651;
  box-shadow: 0 7px 16px rgba(66, 49, 29, 0.16);
}

body.review-active .safety-bell {
  top: 260px;
  right: 22px;
  bottom: auto;
  min-width: 126px;
  height: 44px;
  padding: 4px 13px 4px 7px;
  justify-content: flex-start;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 7px 16px rgba(66, 49, 29, 0.16);
}

body.review-active .safety-bell__button {
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  border: none;
  background: transparent;
  box-shadow: none;
}

body.review-active .safety-bell__sprite {
  width: 32px;
  height: 32px;
}

body.review-active .safety-bell__tip {
  padding: 0;
  border: none;
  background: transparent;
  color: #3e3651;
  font-size: 14px;
  box-shadow: none;
}

@media (max-width: 1180px) {
  body.review-active .backpack-entry,
  body.review-active .mailbox-fab-entry,
  body.review-active .robot-fab-entry,
  body.review-active .safety-bell {
    display: none;
  }
}
</style>

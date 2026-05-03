<template>
  <main class="xinglin-home">
    <section
      class="hero"
      aria-labelledby="hero-title"
    >
      <div class="hero__story">
        <p class="story-label">
          杏林谷今日探险
        </p>
        <h1
          id="hero-title"
          class="hero-title"
        >
          <span>杏林小药师</span>
          <b>经络探险记</b>
        </h1>
        <p class="hero__lead">
          杏林谷被迷雾遮住了，小铜人老师身上的穴位星光也变暗了。今天你要成为“杏林小药师”，完成小任务，收集材料，帮杏林谷重新亮起来。
        </p>

        <div class="hero__quest">
          <span>今日主线</span>
          <strong>点亮小手星图</strong>
          <p>认识小手区域，找到“合谷”穴位，再完成一次安全问答。</p>
          <div class="hero__rewards">
            <i>穴位星珠 × 2</i>
            <i>铜片 × 1</i>
            <i>安全铃铛 × 1</i>
          </div>
        </div>

        <div class="hero__actions">
          <a-button
            type="primary"
            size="large"
            class="hero__action-primary"
            @click="go('/shuxue')"
          >
            开始今日探险
          </a-button>
          <a-button
            size="large"
            class="hero__action-secondary"
            @click="go('/shuxue')"
          >
            进入小铜人馆
          </a-button>
          <a-button
            size="large"
            class="hero__action-secondary"
            @click="openBackpack"
          >
            查看我的材料袋
          </a-button>
          <a-button
            size="large"
            class="hero__action-secondary"
            @click="scrollToMap"
          >
            打开杏林地图
          </a-button>
        </div>
      </div>

      <div class="hero__scene">
        <div class="scene-card">
          <span class="scene-sun" />
          <span class="scene-cloud scene-cloud--one" />
          <span class="scene-cloud scene-cloud--two" />
          <span class="scene-hill scene-hill--back" />
          <span class="scene-hill scene-hill--front" />
          <span class="scene-path" />
          <div class="copper-guide">
            <img
              class="copper-guide__image"
              :src="copperTeacherSprite"
              alt="小铜人老师"
            >
            <span class="copper-guide__star copper-guide__star--one" />
            <span class="copper-guide__star copper-guide__star--two" />
            <span class="copper-guide__star copper-guide__star--three" />
          </div>
          <img
            class="hero-apprentice"
            :src="apprenticeSprite"
            alt="杏林小药师"
          >
          <div class="speech-bubble">
            <strong>小铜人老师</strong>
            <span>先从“小手地图”开始，找到合谷就能获得穴位星珠。</span>
          </div>
        </div>

        <div class="scene-status">
          <div>
            <span>杏林谷星光</span>
            <strong>34%</strong>
          </div>
          <div class="storybook-progress">
            <i style="width: 34%" />
          </div>
          <p>再完成 2 个任务，今天的小手星图就能亮起来。</p>
        </div>
      </div>
    </section>

    <section class="section entrance-section">
      <div class="section-title">
        <p>小药师工具台</p>
        <h2>你想先去哪一站？</h2>
        <span>入口用儿童能理解的任务语言重新整理，原来的页面跳转都保留。</span>
      </div>

      <div class="entrance-board">
        <button
          v-for="entry in featureEntries"
          :key="entry.key"
          type="button"
          class="entrance-sticker"
          :class="`entrance-sticker--${entry.tone}`"
          @click="handleEntry(entry)"
        >
          <span>{{ entry.mark }}</span>
          <strong>{{ entry.label }}</strong>
          <small>{{ entry.childText }}</small>
        </button>
      </div>
    </section>

    <section
      ref="taskSection"
      class="section today-section"
    >
      <div class="section-title section-title--left">
        <p>今日任务卡</p>
        <h2>完成任务，收集修复医馆的材料</h2>
      </div>

      <div class="today-layout">
        <article class="main-task-scroll">
          <span class="task-ribbon">进行中</span>
          <h3>点亮小手星图</h3>
          <p>跟着小铜人老师认识小手区域，点亮“合谷”穴位。这里是文化科普学习，不提供实际针刺操作指导。</p>
          <div class="task-steps">
            <span>认识小手区域</span>
            <span>找到合谷星点</span>
            <span>完成安全问答</span>
          </div>
          <div class="storybook-progress">
            <i style="width: 34%" />
          </div>
          <a-button
            type="primary"
            @click="go('/shuxue')"
          >
            继续点亮
          </a-button>
        </article>

        <div class="mini-tasks">
          <article
            v-for="task in dailyTasks"
            :key="task.title"
            class="mini-task"
            :class="`mini-task--${task.tone}`"
          >
            <div class="mini-task__top">
              <span>{{ task.mark }}</span>
              <b>{{ task.status }}</b>
            </div>
            <h3>{{ task.title }}</h3>
            <p>{{ task.text }}</p>
            <div class="mini-task__reward">
              {{ task.reward }}
            </div>
            <button
              type="button"
              @click="handleEntry(task.entry)"
            >
              {{ task.button }}
            </button>
          </article>
        </div>
      </div>
    </section>

    <section
      ref="mapSection"
      class="section map-section"
    >
      <div class="section-title">
        <p>杏林谷探险地图</p>
        <h2>走完一站，杏林谷就亮一点</h2>
      </div>

      <div class="map-board map-board--image">
        <img
          class="map-board__bg"
          :src="adventureMapBackground"
          alt=""
          aria-hidden="true"
        >
        <button
          v-for="(node, index) in mapNodes"
          :key="node.name"
          type="button"
          class="map-stop"
          :class="`map-stop--${node.state}`"
          :style="{ left: `${node.x}%`, top: `${node.y}%` }"
          @click="go(node.path)"
        >
          <span class="map-stop__num">
            <span class="map-stop__order">{{ index + 1 }}</span>
            <span class="map-stop__mark">{{ node.mark }}</span>
          </span>
          <small class="map-stop__badge">{{ node.status }}</small>
          <strong>{{ node.name }}</strong>
          <p>{{ node.childText }}</p>
        </button>
      </div>
    </section>

    <section class="section resource-section">
      <div class="backpack-panel">
        <div class="section-title section-title--left">
          <p>材料袋预览</p>
          <h2>今天能收集这些材料</h2>
        </div>
        <div class="material-grid">
          <div
            v-for="item in materials"
            :key="item.name"
            class="material-token"
          >
            <img
              v-if="item.image"
              :src="item.image"
              :alt="item.name"
            >
            <span v-else>{{ item.mark }}</span>
            <strong>{{ item.name }}</strong>
            <small>× {{ item.count }}</small>
          </div>
        </div>
        <a-button @click="openBackpack">
          打开材料袋
        </a-button>
      </div>

      <div class="clinic-panel">
        <div class="clinic-picture">
          <img
            :src="treasureBox"
            alt="材料宝箱"
          >
          <span class="clinic-roof" />
          <span class="clinic-house" />
          <span class="clinic-door" />
        </div>
        <div>
          <span class="task-ribbon">小医馆成长</span>
          <h2>下一目标：修复艾草灯笼</h2>
          <p>还需要艾绒 × 2。完成安全铃铛挑战后，就可以把灯笼挂到小医馆门口。</p>
          <div class="clinic-level">
            <span>修复进度</span>
            <strong>35%</strong>
          </div>
          <a-button
            type="primary"
            @click="go('/myworld')"
          >
            去小医馆
          </a-button>
        </div>
      </div>
    </section>

    <section class="section guide-section">
      <div class="section-title">
        <p>角色引导</p>
        <h2>今天有谁陪你探险？</h2>
      </div>

      <div class="role-board">
        <article
          v-for="role in roles"
          :key="role.name"
          class="role-card"
        >
          <img
            v-if="role.image"
            :src="role.image"
            :alt="role.name"
          >
          <span v-else>{{ role.mark }}</span>
          <div>
            <strong>{{ role.name }}</strong>
            <p>{{ role.text }}</p>
          </div>
        </article>
      </div>
    </section>

    <section class="section safety-section">
      <div class="safety-note">
        <img
          class="safety-note__sprite"
          :src="safetyBellSprite"
          alt="安全铃铛精灵"
        >
        <div>
          <p>安全铃铛提醒</p>
          <h2>这里是文化科普和穴位认知学习</h2>
          <strong>小朋友不能自己拿针扎穴位，也不能模仿视频给自己或同学做针灸。身体不舒服要告诉家长和医生。</strong>
          <a-button
            type="primary"
            @click="go('/quiz-game')"
          >
            进入安全课堂
          </a-button>
        </div>
      </div>
      <div class="teacher-note">
        <h3>家长 / 教师说明</h3>
        <p>本系统不提供医疗诊断、治疗方案、针刺深度、针刺角度或实际操作指导，不能替代专业医务人员诊疗。</p>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import apprenticeSprite from '@/assets/xinglin-herbal-apprentice-actions.gif'
import copperTeacherSprite from '@/assets/copper-teacher-actions.gif'
import grandpaSprite from '@/assets/clinic-grandpa-actions.gif'
import safetyBellSprite from '@/assets/safety-bell-sprite-actions.gif'
import mugwortSprite from '@/assets/mugwort-sprite-actions.gif'
import materialBadge from '@/assets/material-badge.png'
import materialBambooSlip from '@/assets/material-bamboo-slip.png'
import materialCopperModel from '@/assets/material-copper-model.png'
import materialMugwort from '@/assets/material-mugwort.png'
import materialSafetyBell from '@/assets/material-safety-bell.png'
import treasureBox from '@/assets/home_baoxiang.png'
import adventureMapBackground from '@/assets/xinglin-adventure-map-bg.png'

const router = useRouter()
const route = useRoute()
const taskSection = ref(null)
const mapSection = ref(null)

const featureEntries = [
  { key: 'home', mark: '谷', label: '杏林谷首页', childText: '回到今日探险起点', type: 'route', path: '/landing', tone: 'green' },
  { key: 'map', mark: '图', label: '探险地图', childText: '看看下一站在哪里', type: 'scroll', target: 'map', tone: 'gold' },
  { key: 'story', mark: '故', label: '针灸故事馆', childText: '听古代小铜人的故事', type: 'route', path: '/doctor-story', tone: 'apricot' },
  { key: 'body', mark: '身', label: '身体小地图', childText: '认识小手、肚肚和小腿', type: 'route', path: '/shuxue', tone: 'mint' },
  { key: 'meridian', mark: '络', label: '经络星河', childText: '把穴位星星连成路线', type: 'route', path: '/jingluo', tone: 'teal' },
  { key: 'copper', mark: '铜', label: '小铜人馆', childText: '点亮小铜人的穴位星点', type: 'route', path: '/shuxue', tone: 'bronze' },
  { key: 'safety', mark: '安', label: '安全课堂', childText: '记住不能自己尝试针刺', type: 'route', path: '/quiz-game', tone: 'gold' },
  { key: 'clinic', mark: '馆', label: '杏林小医馆', childText: '用材料修复小医馆', type: 'route', path: '/myworld', tone: 'green' },
  { key: 'bag', mark: '袋', label: '材料袋', childText: '看看今天收集了什么', type: 'backpack', tone: 'apricot' },
  { key: 'badge', mark: '章', label: '徽章墙', childText: '查看成长和徽章', type: 'route', path: '/myworld', tone: 'mint' }
]

const dailyTasks = computed(() => [
  {
    mark: '故',
    title: '读一个针灸小故事',
    status: '未开始',
    text: '去故事竹林听杏林爷爷讲“小铜人”的来历。',
    reward: '竹简碎片 × 3',
    button: '去故事馆',
    tone: 'story',
    entry: featureEntries.find((item) => item.key === 'story')
  },
  {
    mark: '安',
    title: '安全铃铛挑战',
    status: '每日开放',
    text: '完成 3 道安全判断题，学会保护自己。',
    reward: '安全铃铛 × 2',
    button: '去挑战',
    tone: 'safe',
    entry: featureEntries.find((item) => item.key === 'safety')
  },
  {
    mark: '星',
    title: '修补星光',
    status: '复习任务',
    text: '复习还没点亮的穴位星点，让星图更完整。',
    reward: '星图碎片 × 1',
    button: '去复习',
    tone: 'review',
    entry: featureEntries.find((item) => item.key === 'copper')
  }
])

const mapNodes = [
  { mark: '故', name: '故事竹林', status: '已开启', childText: '收集竹简碎片', state: 'done', path: '/doctor-story', x: 10.8, y: 54.5 },
  { mark: '身', name: '身体山谷', status: '今日任务', childText: '认识小手地图', state: 'active', path: '/shuxue', x: 23.2, y: 35.2 },
  { mark: '络', name: '经络星河', status: '可探索', childText: '把穴位连成星路', state: 'open', path: '/jingluo', x: 39.0, y: 53.2 },
  { mark: '铜', name: '小铜人馆', status: '重点关卡', childText: '点亮穴位星点', state: 'open', path: '/shuxue', x: 57.7, y: 47.5 },
  { mark: '安', name: '艾草安全屋', status: '每日开放', childText: '完成安全判断', state: 'safe', path: '/quiz-game', x: 77.1, y: 35.2 },
  { mark: '馆', name: '杏林小医馆', status: '可修复', childText: '兑换温暖装饰', state: 'clinic', path: '/myworld', x: 89.1, y: 50.9 }
]

const materials = [
  { image: materialMugwort, name: '艾草包', count: 8 },
  { image: materialBambooSlip, name: '故事竹简', count: 3 },
  { image: materialSafetyBell, name: '安全铃铛', count: 2 },
  { image: materialBadge, name: '成长徽章', count: 1 },
  { image: materialCopperModel, name: '铜人模型', count: 4 }
]

const roles = [
  { name: '小铜人老师', image: copperTeacherSprite, text: '带你认识穴位星点，只做文化科普学习。' },
  { name: '杏林爷爷', image: grandpaSprite, text: '讲故事、发布任务，告诉你下一站在哪里。' },
  { name: '安全铃铛精灵', image: safetyBellSprite, text: '提醒你不能自己针刺，安全学习最重要。' },
  { name: '杏林小精灵', image: mugwortSprite, text: '帮你整理材料袋，发现艾草和竹简里的小线索。' },
  { name: '杏林小药师', image: apprenticeSprite, text: '完成每日探险，收集材料修复小医馆。' }
]

function go(path) {
  router.push(path).catch(() => {})
}

function openBackpack() {
  window.dispatchEvent(new Event('backpack-open'))
  message.success('材料袋已打开')
}

function scrollToTask() {
  taskSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function scrollToMap() {
  mapSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function handleEntry(entry) {
  if (!entry) return
  if (entry.type === 'backpack') {
    openBackpack()
    return
  }
  if (entry.type === 'scroll') {
    entry.target === 'map' ? scrollToMap() : scrollToTask()
    return
  }
  go(entry.path)
}

function handleSectionFromRoute() {
  nextTick(() => {
    if (route.query.section === 'map') scrollToMap()
    if (route.query.section === 'tasks') scrollToTask()
    if (route.query.panel === 'backpack') openBackpack()
  })
}

watch(() => route.fullPath, handleSectionFromRoute)

onMounted(handleSectionFromRoute)
</script>

<style scoped>
.xinglin-home {
  --cream: #fff8e8;
  --paper: #fffdf3;
  --green: #2f7d68;
  --deep-green: #24584b;
  --mint: #dff2d8;
  --gold: #ffd35a;
  --apricot: #f4c995;
  --bronze: #b8863b;
  --teal: #7ec9b1;
  --ink: #263f37;
  --soft-text: #64746d;
  min-height: 100vh;
  color: var(--ink);
  background:
    linear-gradient(180deg, #f6ffe6 0%, var(--cream) 42%, #fffaf0 100%);
}

.hero,
.section {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.9fr);
  gap: 34px;
  min-height: calc(100vh - 64px);
  padding: 72px 0 42px;
  align-items: center;
}

.story-label,
.section-title p {
  margin: 0 0 10px;
  color: var(--bronze);
  font-size: 15px;
  font-weight: 900;
}

.hero-title {
  display: grid;
  gap: 10px;
  margin: 0;
  line-height: 1;
}

.hero-title span {
  color: var(--deep-green);
  font-size: clamp(46px, 5.6vw, 76px);
  font-weight: 950;
  white-space: nowrap;
}

.hero-title b {
  width: fit-content;
  padding: 8px 18px 10px;
  color: #6a4910;
  font-size: clamp(30px, 3.6vw, 46px);
  font-weight: 950;
  background: #fff3bd;
  border: 2px solid rgba(184, 134, 59, 0.24);
  border-radius: 8px;
  box-shadow: 0 10px 20px rgba(184, 134, 59, 0.12);
}

.hero__lead {
  max-width: 660px;
  margin: 20px 0 0;
  color: #4f6259;
  font-size: 18px;
  line-height: 1.8;
}

.hero__quest {
  max-width: 680px;
  margin-top: 24px;
  padding: 18px;
  background: var(--paper);
  border: 2px dashed rgba(184, 134, 59, 0.28);
  border-radius: 8px;
  box-shadow: 0 14px 30px rgba(47, 93, 71, 0.12);
}

.hero__quest span,
.scene-status span,
.clinic-level span {
  color: var(--soft-text);
  font-size: 13px;
  font-weight: 800;
}

.hero__quest strong {
  display: block;
  margin-top: 4px;
  color: var(--green);
  font-size: 25px;
}

.hero__quest p {
  margin: 8px 0 12px;
  color: var(--soft-text);
}

.hero__rewards,
.task-steps {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero__rewards i,
.task-steps span,
.mini-task__reward {
  padding: 7px 10px;
  color: #684913;
  font-style: normal;
  font-weight: 900;
  background: #fff3bd;
  border-radius: 999px;
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

:deep(.ant-btn) {
  border-radius: 999px;
  font-weight: 900;
}

:deep(.ant-btn-primary) {
  background: var(--green);
  border-color: var(--green);
  box-shadow: 0 10px 18px rgba(47, 125, 104, 0.2);
}

:deep(.ant-btn-primary:hover),
:deep(.ant-btn-primary:focus) {
  background: #276c5a;
  border-color: #276c5a;
}

.hero__scene {
  display: grid;
  gap: 14px;
}

.scene-card {
  position: relative;
  min-height: 460px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff5bf 0%, #e5f6d4 54%, #9bd28f 100%);
  border: 2px solid rgba(47, 125, 104, 0.16);
  border-radius: 8px;
  box-shadow: 0 24px 48px rgba(46, 83, 63, 0.16);
}

.scene-sun {
  position: absolute;
  top: 30px;
  right: 48px;
  width: 72px;
  height: 72px;
  background: var(--gold);
  border: 8px solid rgba(255, 255, 255, 0.58);
  border-radius: 50%;
}

.scene-cloud {
  position: absolute;
  height: 38px;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 999px;
}

.scene-cloud--one {
  left: 32px;
  top: 62px;
  width: 150px;
  animation: float-cloud 7s ease-in-out infinite;
}

.scene-cloud--two {
  right: 72px;
  top: 130px;
  width: 126px;
  animation: float-cloud 8s ease-in-out infinite reverse;
}

.scene-hill {
  position: absolute;
  left: -8%;
  right: -8%;
  bottom: 0;
  height: 150px;
  border-radius: 50% 50% 0 0;
}

.scene-hill--back {
  bottom: 86px;
  background: #bddf91;
}

.scene-hill--front {
  bottom: -28px;
  background: #6cab76;
}

.scene-path {
  position: absolute;
  left: 44%;
  bottom: -18px;
  width: 86px;
  height: 220px;
  background: #f8db9f;
  clip-path: polygon(28% 0, 68% 0, 100% 100%, 0 100%);
}

.copper-guide {
  position: absolute;
  left: 44%;
  bottom: 72px;
  width: 150px;
  height: 250px;
  transform: translateX(-50%);
  filter: drop-shadow(0 16px 18px rgba(91, 61, 24, 0.25));
}

.copper-guide__image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.copper-guide__star {
  position: absolute;
  width: 12px;
  height: 12px;
  background: var(--gold);
  border-radius: 50%;
  box-shadow: 0 0 18px rgba(255, 211, 90, 0.92);
  animation: star-pulse 2s ease-in-out infinite;
}

.copper-guide__star--one {
  left: 69px;
  top: 116px;
}

.copper-guide__star--two {
  left: 54px;
  top: 157px;
  animation-delay: 0.25s;
}

.copper-guide__star--three {
  right: 49px;
  top: 193px;
  animation-delay: 0.5s;
}

.hero-apprentice {
  position: absolute;
  right: 18px;
  bottom: 26px;
  width: 164px;
  max-height: 230px;
  object-fit: contain;
  filter: drop-shadow(0 14px 18px rgba(43, 80, 48, 0.25));
}

.speech-bubble {
  position: absolute;
  left: 22px;
  top: 20px;
  display: grid;
  gap: 4px;
  max-width: 250px;
  padding: 14px;
  background: rgba(255, 253, 243, 0.94);
  border: 2px solid rgba(184, 134, 59, 0.24);
  border-radius: 8px;
}

.speech-bubble strong {
  color: var(--bronze);
}

.speech-bubble span {
  color: #4e6659;
  line-height: 1.55;
}

.scene-status,
.main-task-scroll,
.mini-task,
.backpack-panel,
.clinic-panel,
.role-card,
.safety-note,
.teacher-note {
  background: var(--paper);
  border: 2px solid rgba(47, 125, 104, 0.12);
  border-radius: 8px;
  box-shadow: 0 14px 30px rgba(46, 83, 63, 0.1);
}

.scene-status {
  padding: 16px;
}

.scene-status > div:first-child,
.clinic-level {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.scene-status strong,
.clinic-level strong {
  color: var(--green);
  font-size: 24px;
}

.scene-status p {
  margin: 10px 0 0;
  color: var(--soft-text);
}

.storybook-progress {
  height: 11px;
  margin-top: 10px;
  overflow: hidden;
  background: #e6f0da;
  border-radius: 999px;
}

.storybook-progress i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--green), var(--gold));
  border-radius: inherit;
}

.section {
  padding: 44px 0;
}

.section-title {
  margin-bottom: 22px;
  text-align: center;
}

.section-title--left {
  text-align: left;
}

.section-title h2 {
  margin: 0;
  color: var(--deep-green);
  font-size: 30px;
}

.section-title span {
  display: block;
  margin-top: 8px;
  color: var(--soft-text);
}

.entrance-board {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  padding: 18px;
  background: rgba(255, 253, 243, 0.72);
  border: 2px dashed rgba(47, 125, 104, 0.18);
  border-radius: 8px;
}

.entrance-sticker {
  display: grid;
  gap: 8px;
  min-height: 136px;
  padding: 14px 10px;
  color: var(--ink);
  text-align: center;
  background: #fffdf3;
  border: 0;
  border-radius: 8px;
  box-shadow: 0 10px 20px rgba(46, 83, 63, 0.1);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.entrance-sticker:hover,
.entrance-sticker:focus-visible {
  transform: translateY(-4px) rotate(-1deg);
  box-shadow: 0 16px 28px rgba(46, 83, 63, 0.15);
  outline: 3px solid rgba(255, 211, 90, 0.48);
}

.entrance-sticker span {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  justify-self: center;
  color: #fff8e8;
  font-size: 20px;
  font-weight: 950;
  border-radius: 50%;
}

.entrance-sticker strong {
  font-size: 16px;
}

.entrance-sticker small {
  color: var(--soft-text);
  line-height: 1.45;
}

.entrance-sticker--green span,
.entrance-sticker--mint span {
  background: var(--green);
}

.entrance-sticker--gold span {
  color: #684913;
  background: var(--gold);
}

.entrance-sticker--apricot span,
.entrance-sticker--bronze span {
  background: var(--bronze);
}

.entrance-sticker--teal span {
  background: #3f8f84;
}

.today-layout,
.resource-section,
.safety-section {
  display: grid;
  grid-template-columns: 0.92fr 1.08fr;
  gap: 18px;
}

.main-task-scroll {
  position: relative;
  padding: 24px;
  background: linear-gradient(180deg, #fffdf3, #fff4d7);
}

.task-ribbon {
  display: inline-flex;
  width: fit-content;
  padding: 6px 11px;
  color: #6a4910;
  font-size: 13px;
  font-weight: 900;
  background: #fff3bd;
  border-radius: 999px;
}

.main-task-scroll h3,
.mini-task h3,
.clinic-panel h2,
.safety-note h2 {
  margin: 12px 0 8px;
  color: var(--deep-green);
  font-size: 24px;
}

.main-task-scroll p,
.mini-task p,
.clinic-panel p,
.teacher-note p {
  color: var(--soft-text);
  line-height: 1.7;
}

.main-task-scroll :deep(.ant-btn) {
  margin-top: 18px;
}

.mini-tasks {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mini-task {
  display: flex;
  min-height: 310px;
  flex-direction: column;
  padding: 18px;
}

.mini-task__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.mini-task__top span {
  display: inline-flex;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  color: #fff8e8;
  font-weight: 950;
  background: var(--green);
  border-radius: 50%;
}

.mini-task__top b {
  color: var(--bronze);
  font-size: 13px;
}

.mini-task__reward {
  width: fit-content;
  margin-top: auto;
}

.mini-task button {
  height: 38px;
  margin-top: 14px;
  color: #fff;
  font-weight: 900;
  background: var(--green);
  border: 0;
  border-radius: 999px;
  cursor: pointer;
}

.map-board {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  min-height: 292px;
  padding: 34px 20px 28px;
  overflow: hidden;
  background:
    radial-gradient(circle at 14% 18%, rgba(255, 211, 90, 0.32) 0 46px, transparent 47px),
    radial-gradient(circle at 88% 22%, rgba(255, 255, 255, 0.86) 0 42px, transparent 43px),
    linear-gradient(180deg, #f3ffe4 0%, #e5f6d4 54%, #d7eec1 100%);
  border: 2px solid rgba(47, 125, 104, 0.16);
  border-radius: 8px;
  box-shadow: inset 0 0 0 6px rgba(255, 253, 243, 0.55), 0 18px 34px rgba(46, 83, 63, 0.12);
}

.map-cloud {
  position: absolute;
  height: 28px;
  background: rgba(255, 253, 243, 0.82);
  border-radius: 999px;
  filter: drop-shadow(0 8px 10px rgba(80, 112, 80, 0.08));
}

.map-cloud::before,
.map-cloud::after {
  content: "";
  position: absolute;
  bottom: 8px;
  width: 36px;
  height: 36px;
  background: inherit;
  border-radius: 50%;
}

.map-cloud::before {
  left: 18px;
}

.map-cloud::after {
  right: 22px;
}

.map-cloud--left {
  top: 24px;
  left: 46px;
  width: 132px;
}

.map-cloud--right {
  right: 58px;
  bottom: 34px;
  width: 156px;
}

.map-trail {
  position: absolute;
  inset: 24px 34px 26px;
  z-index: 0;
  width: calc(100% - 68px);
  height: calc(100% - 50px);
  pointer-events: none;
}

.map-trail__shadow,
.map-trail__line {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.map-trail__shadow {
  stroke: rgba(138, 93, 42, 0.14);
  stroke-width: 24;
}

.map-trail__line {
  stroke: #d6a65b;
  stroke-width: 8;
  stroke-dasharray: 18 14;
}

.map-stop {
  position: relative;
  z-index: 1;
  display: grid;
  min-height: 188px;
  align-content: start;
  justify-items: center;
  padding: 14px 12px 16px;
  text-align: center;
  background: rgba(255, 253, 243, 0.94);
  border: 2px solid rgba(47, 125, 104, 0.15);
  border-radius: 8px;
  box-shadow: 0 12px 24px rgba(46, 83, 63, 0.12);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.map-stop:nth-of-type(2),
.map-stop:nth-of-type(4),
.map-stop:nth-of-type(6) {
  margin-top: 48px;
}

.map-stop:hover {
  border-color: rgba(47, 125, 104, 0.35);
  box-shadow: 0 18px 30px rgba(46, 83, 63, 0.16);
  transform: translateY(-7px) rotate(-1deg);
}

.map-stop__num {
  position: relative;
  display: inline-flex;
  width: 68px;
  height: 68px;
  align-items: center;
  justify-content: center;
  margin-top: -28px;
  color: #fffaf0;
  background: linear-gradient(160deg, #42a186, #2f7d68);
  border: 5px solid var(--paper);
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(47, 125, 104, 0.14), 0 10px 18px rgba(47, 125, 104, 0.18);
}

.map-stop__order {
  position: absolute;
  top: -6px;
  right: -6px;
  display: inline-flex;
  width: 24px;
  height: 24px;
  align-items: center;
  justify-content: center;
  color: #684913;
  font-size: 12px;
  font-weight: 950;
  background: var(--gold);
  border: 3px solid var(--paper);
  border-radius: 50%;
}

.map-stop__mark {
  font-size: 25px;
  font-weight: 950;
}

.map-stop__badge {
  display: inline-flex;
  width: fit-content;
  margin-top: 10px;
  padding: 4px 9px;
  color: #6a4910;
  font-size: 12px;
  font-weight: 950;
  background: #fff3bd;
  border-radius: 999px;
}

.map-stop strong {
  display: block;
  margin-top: 10px;
  color: var(--deep-green);
  font-size: 17px;
}

.map-stop p {
  margin: 8px 0 0;
  color: var(--soft-text);
  line-height: 1.5;
}

.map-stop--active {
  background: #fff4d7;
  border-color: rgba(184, 134, 59, 0.28);
}

.map-stop--safe .map-stop__num,
.map-stop--clinic .map-stop__num {
  color: #684913;
  background: linear-gradient(160deg, #ffe485, var(--gold));
}

.map-stop--done .map-stop__num {
  background: linear-gradient(160deg, #d6a65b, #9a6a2c);
}

.map-stop--active .map-stop__num {
  animation: map-node-pulse 2.2s ease-in-out infinite;
}

.map-board--image {
  display: block;
  aspect-ratio: 2048 / 819;
  min-height: 0;
  padding: 0;
  overflow: hidden;
  background: #fff8e8;
  border: 2px solid rgba(184, 134, 59, 0.18);
  box-shadow: 0 18px 34px rgba(46, 83, 63, 0.1);
}

.map-board--image .map-board__bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
}

.map-board--image .map-stop {
  position: absolute;
  z-index: 2;
  display: grid;
  width: clamp(96px, 9vw, 126px);
  min-height: 0;
  height: clamp(96px, 9vw, 126px);
  align-content: center;
  justify-items: center;
  gap: 3px;
  margin-top: 0 !important;
  padding: 10px;
  color: var(--ink);
  background: rgba(255, 253, 243, 0.82);
  border: 2px solid rgba(255, 211, 90, 0.76);
  border-radius: 50%;
  box-shadow:
    inset 0 0 0 4px rgba(255, 248, 222, 0.78),
    0 0 18px rgba(255, 211, 90, 0.55),
    0 12px 22px rgba(118, 139, 83, 0.12);
  transform: translate(-50%, -50%);
  backdrop-filter: blur(2px);
}

.map-board--image .map-stop:hover,
.map-board--image .map-stop:focus-visible {
  border-color: rgba(255, 211, 90, 0.98);
  box-shadow:
    inset 0 0 0 4px rgba(255, 248, 222, 0.9),
    0 0 24px rgba(255, 211, 90, 0.72),
    0 16px 28px rgba(118, 139, 83, 0.18);
  transform: translate(-50%, -54%) scale(1.04);
  outline: none;
}

.map-board--image .map-stop__num {
  width: auto;
  height: auto;
  margin-top: 0;
  color: var(--green);
  background: transparent;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.map-board--image .map-stop__order {
  top: -14px;
  right: -34px;
  width: 24px;
  height: 24px;
  color: #6a4910;
  background: var(--gold);
  border: 2px solid #fffdf3;
}

.map-board--image .map-stop__mark {
  font-size: clamp(22px, 2.1vw, 30px);
  line-height: 1;
}

.map-board--image .map-stop__badge {
  margin-top: 1px;
  padding: 2px 7px;
  color: #8a5d2a;
  font-size: 11px;
  background: rgba(255, 243, 189, 0.82);
}

.map-board--image .map-stop strong {
  margin-top: 1px;
  color: var(--deep-green);
  font-size: clamp(13px, 1.15vw, 16px);
  line-height: 1.15;
}

.map-board--image .map-stop p {
  display: none;
}

.map-board--image .map-stop--active {
  background: rgba(255, 246, 211, 0.9);
  border-color: rgba(47, 125, 104, 0.58);
  animation: map-button-pulse 2.2s ease-in-out infinite;
}

.map-board--image .map-stop--active .map-stop__num {
  animation: none;
}

.backpack-panel,
.clinic-panel,
.safety-note,
.teacher-note {
  padding: 22px;
}

.material-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 18px 0;
}

.material-token {
  display: grid;
  grid-template-columns: 54px 1fr auto;
  gap: 12px;
  align-items: center;
  min-height: 72px;
  padding: 10px 12px;
  background: #f7fbef;
  border-radius: 8px;
}

.material-token img {
  width: 50px;
  height: 50px;
  object-fit: contain;
  filter: drop-shadow(0 8px 10px rgba(47, 83, 63, 0.14));
}

.material-token span {
  display: inline-flex;
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  color: #fff8e8;
  font-weight: 950;
  background: var(--bronze);
  border-radius: 50%;
}

.material-token small {
  color: var(--green);
  font-weight: 950;
}

.clinic-panel {
  display: grid;
  grid-template-columns: 190px 1fr;
  gap: 18px;
  align-items: center;
  background: linear-gradient(180deg, #fffdf3, #fff4d7);
}

.clinic-picture {
  position: relative;
  min-height: 210px;
}

.clinic-picture img {
  position: absolute;
  left: 4px;
  bottom: 0;
  width: 92px;
  height: 92px;
  object-fit: contain;
}

.clinic-roof,
.clinic-house,
.clinic-door {
  position: absolute;
  left: 58%;
  transform: translateX(-50%);
}

.clinic-roof {
  top: 30px;
  width: 130px;
  height: 52px;
  background: var(--bronze);
  clip-path: polygon(50% 0, 100% 100%, 0 100%);
}

.clinic-house {
  bottom: 32px;
  width: 118px;
  height: 82px;
  background: #f6d19e;
  border: 3px solid rgba(113, 75, 26, 0.18);
  border-radius: 8px;
}

.clinic-door {
  bottom: 32px;
  width: 34px;
  height: 50px;
  background: #8a5d2a;
  border-radius: 8px 8px 0 0;
}

.clinic-level {
  padding: 10px 12px;
  margin: 14px 0;
  background: var(--paper);
  border-radius: 8px;
}

.role-board {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.role-card {
  display: grid;
  gap: 12px;
  justify-items: center;
  min-height: 246px;
  padding: 18px;
  text-align: center;
}

.role-card img {
  width: 118px;
  height: 118px;
  object-fit: contain;
  filter: drop-shadow(0 10px 14px rgba(47, 83, 63, 0.14));
}

.role-card > span {
  display: inline-flex;
  width: 92px;
  height: 92px;
  align-items: center;
  justify-content: center;
  color: #fff8e8;
  font-size: 30px;
  font-weight: 950;
  background: linear-gradient(160deg, #d6a65b, #8b6532);
  border-radius: 50%;
}

.role-card strong {
  color: var(--deep-green);
  font-size: 17px;
}

.role-card p {
  margin: 6px 0 0;
  color: var(--soft-text);
  line-height: 1.6;
}

.safety-section {
  padding-bottom: 72px;
}

.safety-note {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 16px;
  background: #fff4d7;
}

.safety-note__sprite {
  width: 72px;
  height: 72px;
  align-self: start;
  object-fit: contain;
  filter: drop-shadow(0 10px 14px rgba(120, 74, 0, 0.2));
  animation: bell-swing 2.8s ease-in-out infinite;
}

.safety-note p {
  margin: 0;
  color: var(--bronze);
  font-weight: 950;
}

.safety-note strong {
  display: block;
  margin: 0 0 16px;
  color: #5f563f;
  line-height: 1.7;
}

.teacher-note h3 {
  margin: 0 0 10px;
  color: var(--deep-green);
}

@keyframes float-cloud {
  0%,
  100% {
    transform: translateX(-10px);
  }

  50% {
    transform: translateX(12px);
  }
}

@keyframes star-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.78;
  }

  50% {
    transform: scale(1.38);
    opacity: 1;
  }
}

@keyframes bell-swing {
  0%,
  86%,
  100% {
    transform: rotate(0);
  }

  90% {
    transform: rotate(6deg);
  }

  94% {
    transform: rotate(-6deg);
  }
}

@keyframes map-node-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 3px rgba(47, 125, 104, 0.14), 0 10px 18px rgba(47, 125, 104, 0.18);
  }

  50% {
    box-shadow: 0 0 0 8px rgba(255, 211, 90, 0.28), 0 12px 24px rgba(184, 134, 59, 0.24);
  }
}

@keyframes map-button-pulse {
  0%,
  100% {
    box-shadow:
      inset 0 0 0 4px rgba(255, 248, 222, 0.78),
      0 0 18px rgba(255, 211, 90, 0.55),
      0 12px 22px rgba(118, 139, 83, 0.12);
  }

  50% {
    box-shadow:
      inset 0 0 0 4px rgba(255, 248, 222, 0.92),
      0 0 30px rgba(255, 211, 90, 0.78),
      0 16px 30px rgba(184, 134, 59, 0.18);
  }
}

@media (max-width: 1100px) {
  .hero,
  .today-layout,
  .resource-section,
  .safety-section {
    grid-template-columns: 1fr;
  }

  .entrance-board {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .mini-tasks {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .map-board {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .map-trail {
    display: none;
  }

  .map-stop:nth-of-type(2),
  .map-stop:nth-of-type(4),
  .map-stop:nth-of-type(6) {
    margin-top: 0;
  }

  .role-board {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hero,
  .section {
    width: min(100% - 24px, 1180px);
  }

  .hero {
    gap: 20px;
    min-height: auto;
    padding-top: 34px;
    padding-bottom: 26px;
  }

  .hero-title span {
    font-size: 36px;
    white-space: normal;
  }

  .hero-title b {
    padding: 6px 14px 8px;
    font-size: 26px;
  }

  .hero__lead {
    margin-top: 14px;
    font-size: 16px;
    line-height: 1.7;
  }

  .hero__quest {
    margin-top: 16px;
    padding: 14px;
  }

  .hero__actions {
    margin-top: 16px;
    gap: 10px;
  }

  .hero__actions :deep(.ant-btn) {
    min-height: 44px;
    padding-inline: 18px;
  }

  .hero__actions .hero__action-primary {
    width: 100%;
    order: 1;
  }

  .hero__actions .hero__action-secondary {
    display: none;
  }

  .scene-card {
    min-height: 340px;
  }

  .speech-bubble {
    left: 14px;
    right: 14px;
    max-width: none;
  }

  .hero-apprentice {
    width: 118px;
  }

  .section {
    padding: 34px 0;
  }

  .section-title h2 {
    font-size: 24px;
    line-height: 1.35;
  }

  .section-title span {
    font-size: 14px;
  }

  .entrance-board,
  .mini-tasks,
  .material-grid,
  .clinic-panel,
  .role-board {
    grid-template-columns: 1fr;
  }

  .entrance-sticker {
    min-height: 112px;
    padding: 14px 12px;
  }

  .entrance-sticker strong {
    font-size: 17px;
  }

  .mini-task {
    min-height: auto;
  }

  .mini-task button {
    min-height: 44px;
  }

  .map-section {
    overflow-x: visible;
    padding-bottom: 20px;
  }

  .map-board--image {
    width: 100%;
    min-width: 0;
    aspect-ratio: 16 / 9;
  }

  .map-board--image .map-stop {
    width: clamp(74px, 18vw, 96px);
    height: clamp(74px, 18vw, 96px);
    padding: 8px;
  }

  .map-board--image .map-stop__mark {
    font-size: clamp(18px, 4.8vw, 24px);
  }

  .map-board--image .map-stop strong {
    font-size: clamp(11px, 2.5vw, 13px);
  }

  .map-board--image .map-stop__order {
    top: -10px;
    right: -22px;
    width: 20px;
    height: 20px;
    font-size: 11px;
  }

  .map-board--image .map-stop__badge {
    margin-top: 0;
    font-size: 10px;
  }

  .material-token {
    min-height: 76px;
    padding: 10px;
  }

  .role-card {
    min-height: auto;
    padding: 16px;
  }

  .role-card strong {
    font-size: 16px;
  }

  .safety-note {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .hero,
  .section {
    width: min(100% - 18px, 1180px);
  }

  .hero {
    padding-top: 22px;
    gap: 16px;
  }

  .story-label,
  .section-title p {
    font-size: 13px;
  }

  .hero-title span {
    font-size: 30px;
  }

  .hero-title b {
    font-size: 22px;
  }

  .hero__lead {
    font-size: 15px;
  }

  .hero__quest strong {
    font-size: 22px;
  }

  .scene-card {
    min-height: 300px;
  }

  .copper-guide {
    width: 126px;
    height: 210px;
  }

  .map-section {
    overflow-x: auto;
    padding-bottom: 26px;
  }

  .map-board--image {
    width: 760px;
    min-width: 760px;
    max-width: none;
  }

  .map-board--image .map-stop {
    width: 82px;
    height: 82px;
  }

  .map-board--image .map-stop strong {
    font-size: 12px;
  }

  .map-board--image .map-stop__badge {
    padding: 1px 5px;
  }

  .section-title h2 {
    font-size: 22px;
  }

  .main-task-scroll,
  .mini-task,
  .backpack-panel,
  .clinic-panel,
  .role-card,
  .safety-note,
  .teacher-note {
    padding: 16px;
  }

  .main-task-scroll h3,
  .mini-task h3,
  .clinic-panel h2,
  .safety-note h2 {
    font-size: 20px;
  }
}
</style>

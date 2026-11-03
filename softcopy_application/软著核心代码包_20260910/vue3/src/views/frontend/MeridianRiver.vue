<template>
  <main class="meridian-page">
    <StarRiverDecoration />
    <section class="river-hero">
      <div>
        <span class="river-eyebrow" v-html="pinyinHtml('经络星河 · 侦探闯关')"></span>
        <h1 v-html="pinyinHtml('当经络侦探，点亮璀璨星河')"></h1>
        <p v-html="pinyinHtml('你是经络小侦探，需要完成任务解锁经络，按顺序点亮星点，学习经络走向和穴位归属。这里是儿童中医文化科普学习，不展示真实针刺，也不替代医生指导。')"></p>
      </div>
      <div class="river-reward">
        <span v-html="pinyinHtml('完成一条经络奖励')"></span>
        <strong v-html="pinyinHtml('每条路线：星砂 × 1')"></strong>
        <strong v-html="pinyinHtml('全部完成：星砂 × 5、铜片 × 2')"></strong>
      </div>
      <button type="button" class="tutorial-replay" @click="startTutorial">
        <BookOutlined aria-hidden="true" />
        <span v-html="pinyinHtml('使用教学')"></span>
      </button>
      <button type="button" class="match-game-btn" @click="$router.push('/meridian-match')" v-html="pinyinHtml('🎮 经络消消看')"></button>
      <button type="button" class="match-game-btn sort-btn" @click="$router.push('/acupoint-sort')" v-html="pinyinHtml('📏 穴位排序')"></button>
    </section>

    <section class="game-mode-switch" v-if="showQuestionModal">
      <div class="question-overlay" @click="closeQuestionModal"></div>
      <div class="question-modal">
        <div class="question-header">
          <span class="question-badge" v-html="pinyinHtml('任务关卡')"></span>
          <h3 v-html="pinyinHtml(currentQuestion?.title || '')"></h3>
          <button type="button" class="question-close" @click="closeQuestionModal">×</button>
        </div>
        <div class="question-body">
          <p class="question-text" v-html="pinyinHtml(currentQuestion?.question || '')"></p>
          <div class="question-options">
            <button
              v-for="(option, index) in currentQuestion?.options"
              :key="index"
              type="button"
              class="option-btn"
              :class="{ 'option-correct': answered && option === currentQuestion.correctAnswer, 'option-wrong': answered && option !== currentQuestion.correctAnswer }"
              :disabled="answered"
              @click="checkAnswer(option)"
              v-html="pinyinHtml(option)"
            >
            </button>
          </div>
        </div>
        <div class="question-footer">
          <div class="question-buttons">
            <a-button @click="skipQuestion"><span v-html="pinyinHtml('暂不回答，退出题目')"></span></a-button>
          </div>
          <div v-if="answered" class="feedback" :class="{ correct: isAnswerCorrect, wrong: !isAnswerCorrect }">
            <strong v-html="pinyinHtml(isAnswerCorrect ? '答对啦！' : '再想想，这颗穴位属于哪条经络呢？')"></strong>
            <p v-html="pinyinHtml(feedbackText)"></p>
          </div>
          <div v-if="answered" class="question-buttons">
            <a-button @click="closeQuestionModal"><span v-html="pinyinHtml('退出关卡')"></span></a-button>
            <a-button type="primary" size="large" @click="continueAfterQuestion"><span v-html="pinyinHtml('继续点亮')"></span></a-button>
          </div>
        </div>
      </div>
    </section>

    <section class="unlock-chain">
      <div class="unlock-chain-header">
        <span class="unlock-chain-label" v-html="pinyinHtml('解锁顺序')"></span>
        <span class="unlock-chain-progress">{{ completedRouteCount }}/14</span>
      </div>
      <div class="unlock-chain-track">
        <div class="unlock-chain-line"></div>
        <div class="unlock-chain-fill" :style="{ width: (completedRouteCount / 14 * 100) + '%' }"></div>
        <div
          v-for="(route, idx) in routes"
          :key="route.id"
          class="unlock-chain-dot"
          :class="{
            done: isRouteDone(route.id),
            current: selectedRoute.id === route.id && !isRouteDone(route.id),
            locked: !isRouteUnlocked(route.id) && !isRouteDone(route.id)
          }"
          :style="{ left: (idx / (routes.length - 1) * 100) + '%' }"
          @click="selectRoute(route.id)"
        >
          <span class="unlock-chain-mark">{{ route.mark }}</span>
        </div>
      </div>
    </section>

    <section class="river-layout">
      <aside class="route-picker" aria-label="选择经络星路">
        <div class="route-intro">
          <h4 v-html="pinyinHtml('经络星图')"></h4>
          <p v-html="pinyinHtml('找到经络，成为侦探，点亮星河')"></p>
        </div>
        <template v-for="group in groupedRoutes" :key="group.region">
          <div class="route-group-header" v-html="pinyinHtml(group.region)"></div>
          <button
            v-for="route in group.routes"
            :key="route.id"
            type="button"
            :class="{
              active: selectedRoute.id === route.id,
              done: isRouteDone(route.id),
              unlocked: isRouteUnlocked(route.id),
              locked: !isRouteUnlocked(route.id)
            }"
            @click="selectRoute(route.id)"
            :disabled="!isRouteUnlocked(route.id)"
          >
            <span v-html="pinyinHtml(route.mark)"></span>
            <div>
              <strong v-html="pinyinHtml(route.name)"></strong>
              <small v-html="pinyinHtml(routeStatusText(route))"></small>
            </div>
          </button>
        </template>
      </aside>

      <div class="star-board" :class="{
        complete: routeCompleted,
        'detective-mode': detectiveModeActive,
        'show-relationship': showRelationshipInfo
      }">
        <div class="star-board__sky" aria-hidden="true">
          <i v-for="spark in sparks" :key="spark.index" :class="`star-spark star-spark--${spark.size}`" :style="sparkStyle(spark.index)" />
        </div>

        <svg class="star-lines" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
          <defs>
            <filter id="river-glow" x="-45%" y="-45%" width="190%" height="190%">
              <feGaussianBlur stdDeviation="2.4" result="blur" />
              <feMerge>
                <feMergeNode in="blur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
          </defs>
          <polyline :points="linePoints" class="star-line star-line--base" fill="none" />
          <polyline
            :points="linePoints"
            class="star-line star-line--lit"
            fill="none"
            :style="{ strokeDasharray: lineLength, strokeDashoffset: lineDashOffset }"
          />
        </svg>

        <button
          v-for="(point, index) in selectedRoute.points"
          :key="point.name"
          type="button"
          class="star-point"
          :class="{
            lit: isLit(index),
            current: index === currentIndex && !routeCompleted,
            locked: !canTapPoint(index),
            wrong: wrongPoint === point.name && !routeCompleted,
            'belonging-highlight': showRelationshipInfo && isCurrentPoint(point.name)
          }"
          :style="{ left: `${point.x}%`, top: `${point.y}%` }"
          @click="tapPoint(point, index)"
          :disabled="!canTapPoint(index)"
        >
          <div class="planet-orbital" aria-hidden="true"></div>
          <div class="planet-body">
            <span class="planet-num">{{ index + 1 }}</span>
          </div>
          <strong v-html="pinyinHtml(point.name)"></strong>
          <small v-html="pinyinHtml(starPointText(index))"></small>
        </button>

        <div class="river-guide">
          <span v-html="pinyinHtml(selectedRoute.mark)"></span>
          <div>
            <strong v-html="pinyinHtml(selectedRoute.name)"></strong>
            <p v-html="pinyinHtml(guideText)"></p>
            <div class="meridian-info" v-if="selectedRoute.description">
              <span class="info-label"><BookOutlined aria-hidden="true" /><span v-html="pinyinHtml('知识')"></span></span>
              <span class="info-text" v-html="pinyinHtml(selectedRoute.description)"></span>
            </div>
            <div class="body-region-info" v-if="selectedRoute.bodyRegion">
              <span class="info-label"><EnvironmentOutlined aria-hidden="true" /><span v-html="pinyinHtml('区域')"></span></span>
              <span class="info-text" v-html="pinyinHtml(selectedRoute.bodyRegion)"></span>
            </div>
          </div>
        </div>

        <div class="detective-hint" v-if="detectiveModeActive && !routeCompleted">
          <div class="hint-badge"><SearchOutlined aria-hidden="true" /><span v-html="pinyinHtml('侦探提示')"></span></div>
          <p v-html="pinyinHtml('请按照经络循行顺序点击星点，错了没关系，会帮你记录在星光修补册。')"></p>
        </div>

        <div class="relationship-card" v-if="showRelationshipInfo">
          <div class="relationship-header">
            <strong v-html="pinyinHtml('穴位归属关系')"></strong>
          </div>
          <div class="relationship-chain">
            <div class="chain-item body">
              <span class="chain-label" v-html="pinyinHtml('身体区域')"></span>
              <span class="chain-value" v-html="pinyinHtml(selectedRoute.bodyRegion || '未知')"></span>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-item meridian">
              <span class="chain-label" v-html="pinyinHtml('经络')"></span>
              <span class="chain-value" v-html="pinyinHtml(selectedRoute.name)"></span>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-item acupoint">
              <span class="chain-label" v-html="pinyinHtml('穴位')"></span>
              <span class="chain-value" v-html="pinyinHtml(currentRelationPoint || '点击星点查看')"></span>
            </div>
          </div>
          <p class="relationship-desc" v-html="pinyinHtml('每颗星星都属于一条经络，每条经络属于一片身体区域。')"></p>
        </div>
      </div>
    </section>

    <section class="game-controls">
      <div class="control-group">
        <label class="control-label">
          <input type="checkbox" v-model="detectiveModeActive" />
          <span class="checkmark"></span>
          <SearchOutlined aria-hidden="true" />
          侦探闯关模式（需要按顺序点击）
        </label>
        <label class="control-label">
          <input type="checkbox" v-model="showRelationshipInfo" />
          <span class="checkmark"></span>
          <TagsOutlined aria-hidden="true" />
          显示归属关系（穴位-经络-区域）
        </label>
      </div>
    </section>

    <section class="river-footer" :class="{ complete: routeCompleted, 'in-progress': currentIndex > 0 && !routeCompleted }">
      <div>
        <span v-html="pinyinHtml(routeCompleted ? '整条星河已点亮' : (currentIndex > 0 ? '任务进行中…' : '当前任务进度'))"></span>
        <h2 v-html="pinyinHtml(footerTitle)"></h2>
        <p v-html="pinyinHtml(footerText)"></p>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          <span class="progress-text">{{ currentIndex }}/{{ selectedRoute.points.length }}</span>
        </div>
      </div>
      <div class="footer-actions">
        <a-button type="default" size="large" :disabled="currentIndex === 0" @click="resetProgress"><span v-html="pinyinHtml('重新开始')"></span></a-button>
        <a-button type="default" size="large" :disabled="currentIndex === 0 || routeCompleted || rewardClaimed" @click="showExitDialog = true"><span v-html="pinyinHtml('退出并保存进度')"></span></a-button>
        <a-button type="primary" size="large" :loading="claimingReward" :disabled="!routeCompleted || rewardClaimed || claimingReward" @click="claimReward"><span v-html="pinyinHtml(rewardClaimed ? '奖励已领取' : '领取星河奖励')"></span></a-button>
      </div>
    </section>

    <section class="exit-dialog" v-if="showExitDialog">
      <div class="exit-overlay" @click="showExitDialog = false"></div>
      <div class="exit-modal">
        <div class="exit-header">
          <h3 v-html="pinyinHtml('退出任务')"></h3>
          <button type="button" class="exit-close" @click="showExitDialog = false">×</button>
        </div>
        <div class="exit-body">
          <p>你已经点亮了 <strong>{{ currentIndex }}/{{ selectedRoute.points.length }}</strong> 颗星点。</p>
          <p v-html="pinyinHtml('进度会自动保存，下次回来可以继续。')"></p>
          <div class="exit-reward-preview">
            <span>现在退出不会发放奖励：</span>
            <strong>完成整条路线后可领取星砂 × 1、铜片 × 1</strong>
          </div>
        </div>
        <div class="exit-footer">
          <a-button @click="abandonTask"><span v-html="pinyinHtml('放弃任务，清空本条经络')"></span></a-button>
          <a-button @click="saveAndExit"><span v-html="pinyinHtml('保存进度，下次继续')"></span></a-button>
        </div>
      </div>
    </section>

    <section class="continue-dialog" v-if="showContinueDialog">
      <div class="exit-overlay" @click="showContinueDialog = false"></div>
      <div class="exit-modal">
        <div class="exit-header">
          <h3 v-html="pinyinHtml('继续任务')"></h3>
          <span class="continue-badge">有未完成的任务</span>
        </div>
        <div class="exit-body">
          <p>你之前在这条经络上点亮了 <strong>{{ currentIndex }}/{{ selectedRoute.points.length }}</strong> 颗星点。</p>
          <p>要接着上次的进度继续吗？</p>
        </div>
        <div class="exit-footer">
          <a-button @click="resetProgress"><span v-html="pinyinHtml('从头开始')"></span></a-button>
          <a-button type="primary" @click="showContinueDialog = false"><span v-html="pinyinHtml('继续任务')"></span></a-button>
        </div>
      </div>
    </section>
  <section
    class="tutorial-overlay"
    :class="{ 'tutorial-overlay--no-target': tutorialRect.width === 0 && tutorialRect.height === 0 }"
    v-if="showTutorial"
  >
      <div class="tutorial-spotlight" :style="spotlightStyle"></div>
      <div class="tutorial-tooltip" :style="tooltipStyle">
        <div class="tutorial-step-indicator">
          <span v-for="(step, si) in tutorialSteps" :key="si" class="tutorial-dot" :class="{ active: tutorialStep === si, done: tutorialStep > si }"></span>
        </div>
        <div class="tutorial-content">
          <component :is="tutorialSteps[tutorialStep].icon" class="tutorial-emoji" aria-hidden="true" />
          <h3 v-html="pinyinHtml(tutorialSteps[tutorialStep].title)"></h3>
          <p v-html="pinyinHtml(tutorialSteps[tutorialStep].desc)"></p>
        </div>
        <div class="tutorial-nav">
          <a-button v-if="tutorialStep > 0" @click="prevTutorialStep"><span v-html="pinyinHtml('上一步')"></span></a-button>
          <a-button @click="skipTutorial"><span v-html="pinyinHtml('跳过教学')"></span></a-button>
          <a-button v-if="tutorialStep < tutorialSteps.length - 1" type="primary" @click="nextTutorialStep"><span v-html="pinyinHtml('下一步')"></span></a-button>
          <a-button v-else type="primary" @click="finishTutorial"><span v-html="pinyinHtml('开始探索')"></span></a-button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import StarRiverDecoration from '@/components/StarRiverDecoration.vue'
import {
  AimOutlined,
  BookOutlined,
  CompassOutlined,
  EnvironmentOutlined,
  FlagOutlined,
  GiftOutlined,
  QuestionCircleOutlined,
  SearchOutlined,
  StarFilled,
  TagsOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'
import { useGameState } from '@/composables/useGameState'

const STORAGE_KEY = 'xinglin-meridian-river-progress-v4'
const CLAIMED_KEY = 'xinglin-meridian-river-claimed-v4'
const TUTORIAL_SEEN_KEY = 'xinglin-meridian-tutorial-seen'
const lineLength = 100

const { completeStandaloneTask, recordTaskProgress, resetTaskProgress, addReviewRecord, isCompleted, getTaskProgress, isRewardClaimed } = useGameState()
const userStore = useUserStore()
const claimingReward = ref(false)
const savingProgress = ref(false)

const tutorialSteps = [
  {
    icon: StarFilled,
    title: '欢迎来到经络星河',
    desc: '你将成为一名经络小侦探，按顺序解锁14条经络，点亮所有星点，学习穴位和经络知识。',
    target: null,
    position: 'center'
  },
  {
    icon: CompassOutlined,
    title: '看懂解锁顺序',
    desc: '这条金色进度条展示了14条经络的解锁顺序，必须从左到右逐条完成，不能跳过哦。',
    target: '.unlock-chain',
    position: 'bottom'
  },
  {
    icon: AimOutlined,
    title: '选择经络星路',
    desc: '在左边的列表里点选经络，深蓝色底代表你正在探索的经络，灰色代表尚未解锁。',
    target: '.route-picker',
    position: 'right'
  },
  {
    icon: StarFilled,
    title: '按顺序点亮星点',
    desc: '每颗星球就是一个穴位，必须按经络走行顺序点击点亮。点错了没关系，多多尝试哦！',
    target: '.star-board',
    position: 'left'
  },
  {
    icon: QuestionCircleOutlined,
    title: '闯关问答环节',
    desc: '每点亮一颗星都会弹出一道题目，答对后才能继续；答错可以再想想，暂时退出不会丢失已有进度。',
    target: '.game-controls',
    position: 'top'
  },
  {
    icon: GiftOutlined,
    title: '领取你的奖励',
    desc: '全部点亮后领取本条路线奖励；中途退出只保存进度，完成全部路线后还有一份星河奖励。',
    target: '.river-footer',
    position: 'top'
  },
  {
    icon: FlagOutlined,
    title: '准备好了吗？',
    desc: '点击开始探索，成为经络侦探，点亮整个人体星河吧！',
    target: null,
    position: 'center'
  }
]

const showTutorial = ref(false)
const tutorialStep = ref(0)
const tutorialSeen = ref(false)
const tutorialRect = ref({ left: 0, top: 0, width: 0, height: 0 })

function readClaimed() {
  try {
    const localClaimed = JSON.parse(window.localStorage.getItem(CLAIMED_KEY) || '{}')
    return routes.reduce((result, route) => {
      result[route.id] = Boolean(localClaimed[route.id] || isRewardClaimed(route.id))
      return result
    }, {})
  } catch {
    return {}
  }
}

const routes = [
  {
    id: 'meridian-lung-route',
    mark: '手',
    name: '手太阴肺经星路',
    bodyRegion: '上肢',
    description: '从胸部走到手指尖，和肺、呼吸系统相关，是经络循环的起点。',
    unlockCondition: '默认解锁',
    unlockTaskId: 'daily-light-hand-stars',
    points: [
      { name: '中府', x: 18, y: 28 },
      { name: '尺泽', x: 38, y: 40 },
      { name: '列缺', x: 55, y: 48 },
      { name: '太渊', x: 68, y: 55 },
      { name: '少商', x: 82, y: 62 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '少商穴是手太阴肺经的最后一个穴位，它位于哪个位置？',
        options: ['拇指桡侧指甲角旁', '食指指尖', '掌心', '手腕内侧'],
        correctAnswer: '拇指桡侧指甲角旁',
        explanation: '少商穴位于拇指桡侧指甲角附近，是肺经路线末端的传统穴位名称。这里只学习位置与归属。'
      },
      {
        title: '经络走向关卡',
        question: '手太阴肺经的循行方向是怎样的？',
        options: ['从胸部走向手指', '从手指走向胸部', '从头走向脚', '从脚走向头'],
        correctAnswer: '从胸部走向手指',
        explanation: '手太阴肺经起于中焦（中府穴），向下联络大肠，向上经过胸部、手臂内侧，到达拇指端的少商穴。'
      }
    ]
  },
  {
    id: 'meridian-large-intestine-route',
    mark: '手',
    name: '手阳明大肠经星路',
    bodyRegion: '上肢',
    description: '从食指尖开始，沿上肢外侧一路走到鼻翼旁，帮助我们观察手与面部之间的经络路线。',
    unlockCondition: '完成手太阴肺经星路',
    unlockTaskId: 'meridian-lung-route',
    points: [
      { name: '商阳', x: 16, y: 68 },
      { name: '合谷', x: 34, y: 49 },
      { name: '曲池', x: 58, y: 38 },
      { name: '迎香', x: 80, y: 22 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '合谷穴是手阳明大肠经上的重要穴位，它位于哪个身体区域？',
        options: ['头部', '手掌', '腿部', '腹部'],
        correctAnswer: '手掌',
        explanation: '合谷穴位于手背第一、二掌骨之间，是手阳明大肠经路线上的一个观察点。'
      },
      {
        title: '经络走向关卡',
        question: '手阳明大肠经的循行方向是从身体哪个部位开始？',
        options: ['从手指尖开始', '从头顶开始', '从脚底开始', '从腹部开始'],
        correctAnswer: '从手指尖开始',
        explanation: '手阳明大肠经从食指桡侧端的商阳穴开始，向上经过手臂，到达头面部的迎香穴。'
      }
    ]
  },
  {
    id: 'meridian-stomach-route',
    mark: '足',
    name: '足阳明胃经星路',
    bodyRegion: '下肢',
    description: '从面部走到脚趾，是身体最长的一条经络，和胃、消化系统有关，包含足三里等重要穴位。',
    unlockCondition: '完成手阳明大肠经星路',
    unlockTaskId: 'meridian-large-intestine-route',
    points: [
      { name: '四白', x: 13, y: 25 },
      { name: '地仓', x: 30, y: 34 },
      { name: '天枢', x: 48, y: 52 },
      { name: '足三里', x: 68, y: 66 },
      { name: '内庭', x: 86, y: 78 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '足三里穴是足阳明胃经上的重要穴位，它属于哪个身体区域？',
        options: ['头部', '手掌', '小腿', '足底'],
        correctAnswer: '小腿',
        explanation: '足三里位于小腿前外侧，是足阳明胃经路线上的传统穴位名称。这里只进行位置观察。'
      },
      {
        title: '经络走向关卡',
        question: '足阳明胃经的循行方向是怎么走的？',
        options: ['从面部走到脚趾', '从脚趾走到面部', '从手臂走到胸部', '从背部走到腹部'],
        correctAnswer: '从面部走到脚趾',
        explanation: '足阳明胃经起于鼻翼旁的承泣穴，沿面部下行，经过胸部、腹部，一直到脚的第二趾外侧端。'
      }
    ]
  },
  {
    id: 'meridian-spleen-route',
    mark: '足',
    name: '足太阴脾经星路',
    bodyRegion: '下肢',
    description: '从脚趾走向胸部，是传统经络图中的一条路线，三阴交是路线上的观察点。',
    unlockCondition: '完成足阳明胃经星路',
    unlockTaskId: 'meridian-stomach-route',
    points: [
      { name: '隐白', x: 82, y: 82 },
      { name: '三阴交', x: 70, y: 72 },
      { name: '阴陵泉', x: 58, y: 60 },
      { name: '血海', x: 48, y: 50 },
      { name: '大包', x: 28, y: 38 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '三阴交是传统经络图中的一个穴位名称，它位于哪个区域？',
        options: ['小腿内侧', '大腿外侧', '手臂内侧', '腹部'],
        correctAnswer: '小腿内侧',
        explanation: '三阴交位于小腿内侧，是传统经络图中三条阴经交会的观察点。这里只学习文化知识。'
      },
      {
        title: '经络走向关卡',
        question: '足太阴脾经的循行方向是怎样的？',
        options: ['从脚趾走向胸部', '从胸部走向脚趾', '从头部走向腹部', '从手臂走向腿部'],
        correctAnswer: '从脚趾走向胸部',
        explanation: '足太阴脾经起于大趾端的隐白穴，沿小腿内侧上行，经过腹部，到达胸部的大包穴。'
      }
    ]
  },
  {
    id: 'meridian-heart-route',
    mark: '手',
    name: '手少阴心经星路',
    bodyRegion: '上肢',
    description: '从胸部走向小指，是传统经络图中的一条路线，神门是路线上的观察点。',
    unlockCondition: '完成足太阴脾经星路',
    unlockTaskId: 'meridian-spleen-route',
    points: [
      { name: '极泉', x: 18, y: 26 },
      { name: '少海', x: 38, y: 42 },
      { name: '神门', x: 60, y: 55 },
      { name: '少冲', x: 80, y: 60 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '神门穴是手少阴心经上的一个观察点，它位于身体的哪个部位？',
        options: ['手腕内侧', '脚踝外侧', '头部', '背部'],
        correctAnswer: '手腕内侧',
        explanation: '神门穴位于腕部、腕掌侧横纹尺侧端，是手少阴心经路线上的一个观察点。'
      },
      {
        title: '经络走向关卡',
        question: '手少阴心经的循行方向是怎样的？',
        options: ['从心脏走向小指', '从小指走向心脏', '从头部走向手指', '从脚底走向心脏'],
        correctAnswer: '从心脏走向小指',
        explanation: '手少阴心经起于心中，向下联络小肠，沿手臂内侧后缘到达小指端的少冲穴。'
      }
    ]
  },
  {
    id: 'meridian-small-intestine-route',
    mark: '手',
    name: '手太阳小肠经星路',
    bodyRegion: '上肢',
    description: '从小指走向上肢后侧，再经过肩部到达面部，帮助我们观察手、肩与面部的连接。',
    unlockCondition: '完成手少阴心经星路',
    unlockTaskId: 'meridian-heart-route',
    points: [
      { name: '少泽', x: 80, y: 65 },
      { name: '后溪', x: 68, y: 55 },
      { name: '养老', x: 52, y: 45 },
      { name: '听宫', x: 22, y: 18 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '后溪穴是手太阳小肠经上的一个观察点，它位于哪里？',
        options: ['手掌尺侧', '脚底', '头部', '背部'],
        correctAnswer: '手掌尺侧',
        explanation: '后溪穴位于手掌尺侧，微握拳时第5掌指关节后的远侧掌横纹头赤白肉际处，是八脉交会穴之一。'
      },
      {
        title: '经络走向关卡',
        question: '手太阳小肠经的循行方向是怎样的？',
        options: ['从小指走向面部', '从面部走向小指', '从胸部走向手指', '从脚趾走向头部'],
        correctAnswer: '从小指走向面部',
        explanation: '手太阳小肠经起于小指端的少泽穴，沿手臂外侧上行，经过肩胛，到达面部的听宫穴。'
      }
    ]
  },
  {
    id: 'meridian-bladder-route',
    mark: '足',
    name: '足太阳膀胱经星路',
    bodyRegion: '下肢',
    description: '从眼睛走到脚趾，是身体最长穴位最多的经络，背部有重要的背俞穴。',
    unlockCondition: '完成手太阳小肠经星路',
    unlockTaskId: 'meridian-small-intestine-route',
    points: [
      { name: '睛明', x: 18, y: 15 },
      { name: '攒竹', x: 24, y: 20 },
      { name: '肾俞', x: 50, y: 52 },
      { name: '委中', x: 65, y: 68 },
      { name: '至阴', x: 88, y: 88 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '睛明穴是眼保健操中的重要穴位，它位于哪里？',
        options: ['目内眦角稍上方', '眉毛中间', '太阳穴', '鼻子两侧'],
        correctAnswer: '目内眦角稍上方',
        explanation: '睛明穴位于面部目内眦角稍上方凹陷处，是足太阳膀胱经路线的起始观察点。'
      },
      {
        title: '经络走向关卡',
        question: '足太阳膀胱经的循行方向是怎样的？',
        options: ['从眼睛走向脚趾', '从脚趾走向眼睛', '从手臂走向背部', '从胸部走向腿部'],
        correctAnswer: '从眼睛走向脚趾',
        explanation: '足太阳膀胱经起于目内眦的睛明穴，经过头部、背部，沿下肢后侧下行，到达小趾端的至阴穴。'
      }
    ]
  },
  {
    id: 'meridian-kidney-route',
    mark: '足',
    name: '足少阴肾经星路',
    bodyRegion: '下肢',
    description: '从脚底走向胸部，是传统经络图中的一条路线，涌泉是路线起始处的观察点。',
    unlockCondition: '完成足太阳膀胱经星路',
    unlockTaskId: 'meridian-bladder-route',
    points: [
      { name: '涌泉', x: 85, y: 85 },
      { name: '太溪', x: 72, y: 72 },
      { name: '照海', x: 68, y: 68 },
      { name: '复溜', x: 55, y: 52 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '涌泉是足少阴肾经路线上的传统穴位名称，它位于哪个区域？',
        options: ['脚底前部凹陷处', '小腿内侧', '膝盖后方', '腰部'],
        correctAnswer: '脚底前部凹陷处',
        explanation: '涌泉位于足底前部，是足少阴肾经路线的起始观察点。这里只学习位置与归属。'
      },
      {
        title: '经络走向关卡',
        question: '足少阴肾经的循行方向是怎样的？',
        options: ['从脚底走向胸部', '从胸部走向脚底', '从头部走向脚底', '从手臂走向肾脏'],
        correctAnswer: '从脚底走向胸部',
        explanation: '足少阴肾经起于足底的涌泉穴，沿小腿内侧后缘上行，经过腹部，到达胸部。'
      }
    ]
  },
  {
    id: 'meridian-pericardium-route',
    mark: '手',
    name: '手厥阴心包经星路',
    bodyRegion: '上肢',
    description: '从胸部走向中指，是传统经络图中的一条路线，内关是路线上的观察点。',
    unlockCondition: '完成足少阴肾经星路',
    unlockTaskId: 'meridian-kidney-route',
    points: [
      { name: '天池', x: 22, y: 30 },
      { name: '曲泽', x: 42, y: 42 },
      { name: '内关', x: 58, y: 48 },
      { name: '中冲', x: 78, y: 55 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '内关穴是手厥阴心包经上的一个观察点，它位于哪里？',
        options: ['手腕内侧', '手臂外侧', '膝盖下方', '脚踝上方'],
        correctAnswer: '手腕内侧',
        explanation: '内关穴位于前臂掌侧、腕横纹上方，是手厥阴心包经路线上的一个观察点。'
      },
      {
        title: '经络走向关卡',
        question: '手厥阴心包经的循行方向是怎样的？',
        options: ['从胸部走向中指', '从中指走向胸部', '从头部走向手掌', '从脚底走向心脏'],
        correctAnswer: '从胸部走向中指',
        explanation: '手厥阴心包经起于胸中，向下联络三焦，沿手臂内侧中间到达中指端的中冲穴。'
      }
    ]
  },
  {
    id: 'meridian-sanjiao-route',
    mark: '手',
    name: '手少阳三焦经星路',
    bodyRegion: '上肢',
    description: '从无名指走向上肢外侧，再到耳旁与眉梢，帮助我们观察手臂和头面部的连接。',
    unlockCondition: '完成手厥阴心包经星路',
    unlockTaskId: 'meridian-pericardium-route',
    points: [
      { name: '关冲', x: 80, y: 62 },
      { name: '中渚', x: 68, y: 52 },
      { name: '外关', x: 55, y: 45 },
      { name: '翳风', x: 35, y: 20 },
      { name: '丝竹空', x: 20, y: 12 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '外关穴是手少阳三焦经上的一个观察点，它位于哪里？',
        options: ['前臂外侧', '小腿内侧', '头部后方', '背部'],
        correctAnswer: '前臂外侧',
        explanation: '外关穴位于前臂背侧、腕背横纹上方，是手少阳三焦经路线上的一个观察点。'
      },
      {
        title: '经络走向关卡',
        question: '手少阳三焦经的循行方向是怎样的？',
        options: ['从无名指走向眉梢', '从眉梢走向无名指', '从胸部走向手掌', '从脚趾走向头部'],
        correctAnswer: '从无名指走向眉梢',
        explanation: '手少阳三焦经起于无名指端的关冲穴，沿手臂外侧中间上行，绕耳部，到达眉梢的丝竹空穴。'
      }
    ]
  },
  {
    id: 'meridian-gallbladder-route',
    mark: '足',
    name: '足少阳胆经星路',
    bodyRegion: '下肢',
    description: '从眼旁经过头侧、身体侧面一路走到脚趾，帮助我们观察一条较长的身体侧面路线。',
    unlockCondition: '完成手少阳三焦经星路',
    unlockTaskId: 'meridian-sanjiao-route',
    points: [
      { name: '瞳子髎', x: 18, y: 12 },
      { name: '风池', x: 28, y: 22 },
      { name: '肩井', x: 35, y: 30 },
      { name: '环跳', x: 52, y: 58 },
      { name: '阳陵泉', x: 65, y: 72 },
      { name: '足临泣', x: 85, y: 82 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '风池穴是足少阳胆经上的一个观察点，它位于哪里？',
        options: ['后颈部', '前额', '手臂', '小腿'],
        correctAnswer: '后颈部',
        explanation: '风池穴位于后颈部、枕骨之下，是足少阳胆经路线上的一个观察点。'
      },
      {
        title: '经络走向关卡',
        question: '足少阳胆经的循行方向是怎样的？',
        options: ['从眼睛走向脚趾', '从脚趾走向眼睛', '从手臂走向头部', '从胸部走向腿部'],
        correctAnswer: '从眼睛走向脚趾',
        explanation: '足少阳胆经起于目外眦的瞳子髎穴，沿头部两侧下行，经过肩部、胁肋，沿下肢外侧到达足第4趾。'
      }
    ]
  },
  {
    id: 'meridian-liver-route',
    mark: '足',
    name: '足厥阴肝经星路',
    bodyRegion: '下肢',
    description: '从大脚趾沿下肢内侧走向腹部与胸部，帮助我们观察足部和躯干之间的经络路线。',
    unlockCondition: '完成足少阳胆经星路',
    unlockTaskId: 'meridian-gallbladder-route',
    points: [
      { name: '大敦', x: 88, y: 85 },
      { name: '太冲', x: 75, y: 72 },
      { name: '曲泉', x: 58, y: 55 },
      { name: '期门', x: 30, y: 35 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '太冲穴是足厥阴肝经上的一个观察点，它位于哪里？',
        options: ['足背', '手掌', '小腿外侧', '头部'],
        correctAnswer: '足背',
        explanation: '太冲穴位于足背侧、第1与第2跖骨结合部之前，是足厥阴肝经路线上的一个观察点。'
      },
      {
        title: '经络走向关卡',
        question: '足厥阴肝经的循行方向是怎样的？',
        options: ['从脚趾走向胸部', '从胸部走向脚趾', '从头部走向腹部', '从手臂走向腿部'],
        correctAnswer: '从脚趾走向胸部',
        explanation: '足厥阴肝经起于大趾端的大敦穴，沿小腿内侧上行，经过腹部，到达胸部的期门穴。'
      }
    ]
  },
  {
    id: 'meridian-ren-route',
    mark: '任',
    name: '任脉星路',
    bodyRegion: '躯干',
    description: '位于身体前正中线，传统文献称为“阴脉之海”，关元是路线上的观察点。',
    unlockCondition: '完成足厥阴肝经星路',
    unlockTaskId: 'meridian-liver-route',
    points: [
      { name: '关元', x: 48, y: 62 },
      { name: '气海', x: 48, y: 54 },
      { name: '神阙', x: 48, y: 46 },
      { name: '中脘', x: 48, y: 38 },
      { name: '膻中', x: 48, y: 28 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '神阙穴是人体一个很特别的穴位，它位于哪里？',
        options: ['肚脐中央', '胸口正中', '背部', '头顶'],
        correctAnswer: '肚脐中央',
        explanation: '神阙是传统经络图中位于肚脐中央的位置名称，这里只观察它在任脉路线上的位置。'
      },
      {
        title: '经络走向关卡',
        question: '任脉被称为"阴脉之海"，它的循行方向是怎样的？',
        options: ['从下腹部走向面部', '从面部走向下腹部', '从背部走向头部', '从手臂走向胸部'],
        correctAnswer: '从下腹部走向面部',
        explanation: '任脉起于小腹内，沿身体前正中线向上，经过腹部、胸部，到达下唇下方的承浆穴。'
      }
    ]
  },
  {
    id: 'meridian-du-route',
    mark: '督',
    name: '督脉星路',
    bodyRegion: '躯干',
    description: '位于身体后正中线，总管全身阳经，被称为"阳脉之海"，百会穴是全身阳气汇聚之处。',
    unlockCondition: '完成任脉星路',
    unlockTaskId: 'meridian-ren-route',
    points: [
      { name: '长强', x: 52, y: 82 },
      { name: '命门', x: 52, y: 66 },
      { name: '大椎', x: 52, y: 28 },
      { name: '风府', x: 52, y: 20 },
      { name: '百会', x: 52, y: 10 }
    ],
    questions: [
      {
        title: '穴位归属关卡',
        question: '百会穴是全身阳气汇聚之处，它位于哪里？',
        options: ['头顶正中', '两眉之间', '后脑勺', '鼻尖'],
        correctAnswer: '头顶正中',
        explanation: '百会位于头顶正中区域，是督脉路线上的传统穴位名称。这里只学习位置与经络归属。'
      },
      {
        title: '经络走向关卡',
        question: '督脉被称为"阳脉之海"，它的循行方向是怎样的？',
        options: ['从尾骨走向头部', '从头部走向尾骨', '从手臂走向背部', '从脚底走向头顶'],
        correctAnswer: '从尾骨走向头部',
        explanation: '督脉起于小腹内，向下出会阴，沿脊柱后面上行，经过背部、颈部，到达头顶的百会穴，再向前到上唇。'
      }
    ]
  }
]

function readProgress() {
  const completedProgress = routes.reduce((acc, route) => {
    if (isCompleted(route.id)) {
      acc[route.id] = route.points.length
    } else {
      acc[route.id] = 0
    }
    return acc
  }, {})

  if (typeof window === 'undefined') return completedProgress

  try {
    const parsed = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '{}')
    return routes.reduce((acc, route) => {
      const saved = Number(parsed[route.id] || 0)
      const serverProgress = getTaskProgress(route.id)
      acc[route.id] = Math.max(completedProgress[route.id], Math.min(route.points.length, saved), Math.min(route.points.length, serverProgress))
      return acc
    }, {})
  } catch {
    return completedProgress
  }
}

const selectedId = ref(routes[0].id)
const claimedByRoute = ref(readClaimed())
const progressByRoute = ref(readProgress())
const wrongPoint = ref('')
const currentRelationPoint = ref('')
const detectiveModeActive = ref(true)
const showRelationshipInfo = ref(true)
const showQuestionModal = ref(false)
const showExitDialog = ref(false)
const showContinueDialog = ref(false)
const answered = ref(false)
const isAnswerCorrect = ref(false)
const currentQuestion = ref(null)
const questionIndex = ref(0)
const sparks = Array.from({ length: 120 }, (_, index) => ({
  index,
  size: index % 7 === 0 ? 'lg' : index % 4 === 0 ? 'md' : 'sm'
}))

onMounted(async () => {
  const seen = window.localStorage.getItem(TUTORIAL_SEEN_KEY)
  tutorialSeen.value = !!seen
  showTutorial.value = !seen
  if (userStore.isLoggedIn && userStore.userId) {
    await userStore.loadGameState({ showDefaultMsg: false }).catch(() => {})
    progressByRoute.value = readProgress()
    claimedByRoute.value = readClaimed()
  }
  window.addEventListener('scroll', updateTutorialRect, { passive: true })
  nextTick(() => updateTutorialRect())

  window.addEventListener('resize', updateTutorialRect)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateTutorialRect)
  window.removeEventListener('scroll', updateTutorialRect)
  clearHighlight()
})

function startTutorial() {
  tutorialStep.value = 0
  showTutorial.value = true
  window.addEventListener('scroll', updateTutorialRect, { passive: true })
  nextTick(() => updateTutorialRect())
}

function nextTutorialStep() {
  if (tutorialStep.value < tutorialSteps.length - 1) {
    clearHighlight()
    tutorialStep.value++
    nextTick(() => updateTutorialRect())
  }
}

function prevTutorialStep() {
  if (tutorialStep.value > 0) {
    clearHighlight()
    tutorialStep.value--
    nextTick(() => updateTutorialRect())
  }
}

function skipTutorial() {
  clearHighlight()
  showTutorial.value = false
  window.removeEventListener('scroll', updateTutorialRect)
  window.localStorage.setItem(TUTORIAL_SEEN_KEY, '1')
  tutorialSeen.value = true
}

function finishTutorial() {
  clearHighlight()
  showTutorial.value = false
  window.removeEventListener('scroll', updateTutorialRect)
  window.localStorage.setItem(TUTORIAL_SEEN_KEY, '1')
  tutorialSeen.value = true
}

function updateTutorialRect() {
  const step = tutorialSteps[tutorialStep.value]
  if (!step.target) {
    tutorialRect.value = { left: 0, top: 0, width: 0, height: 0 }
    return
  }
  const el = document.querySelector(step.target)
  if (!el) {
    tutorialRect.value = { left: 0, top: 0, width: 0, height: 0 }
    return
  }
  el.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  const rect = el.getBoundingClientRect()
  tutorialRect.value = {
    left: rect.left,
    top: rect.top,
    width: rect.width,
    height: rect.height
  }
  el.classList.add('tutorial-highlight')
}

function clearHighlight() {
  const prev = document.querySelector('.tutorial-highlight')
  if (prev) prev.classList.remove('tutorial-highlight')
}

const spotlightStyle = computed(() => {
  const rect = tutorialRect.value
  if (rect.width === 0 && rect.height === 0) {
    return { display: 'none' }
  }
  return {
    left: `${rect.left - 8}px`,
    top: `${rect.top - 8}px`,
    width: `${rect.width + 16}px`,
    height: `${rect.height + 16}px`
  }
})

const tooltipStyle = computed(() => {
  const step = tutorialSteps[tutorialStep.value]
  if (step.position === 'center') {
    return {
      position: 'fixed',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)'
    }
  }
  const rect = tutorialRect.value
  if (rect.width === 0 && rect.height === 0) {
    return {
      position: 'fixed',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)'
    }
  }
  const vw = window.innerWidth
  const vh = window.innerHeight
  const gap = 20
  switch (step.position) {
    case 'bottom':
      return {
        position: 'fixed',
        top: `${rect.top + rect.height + gap}px`,
        left: `${Math.max(20, Math.min(rect.left + rect.width / 2, vw - 280))}px`,
        transform: 'translateX(-50%)'
      }
    case 'top':
      return {
        position: 'fixed',
        bottom: `${vh - rect.top + gap}px`,
        left: `${Math.max(20, Math.min(rect.left + rect.width / 2, vw - 280))}px`,
        transform: 'translateX(-50%)'
      }
    case 'left':
      return {
        position: 'fixed',
        top: `${Math.max(20, Math.min(rect.top + rect.height / 2, vh - 200))}px`,
        right: `${vw - rect.left + gap}px`
      }
    case 'right':
      return {
        position: 'fixed',
        top: `${Math.max(20, Math.min(rect.top + rect.height / 2, vh - 200))}px`,
        left: `${rect.left + rect.width + gap}px`
      }
    default:
      return {
        position: 'fixed',
        top: `${rect.top + rect.height + gap}px`,
        left: `${Math.max(20, Math.min(rect.left + rect.width / 2, vw - 280))}px`,
        transform: 'translateX(-50%)'
      }
  }
})

const PINYIN_TERMS = {
  经络星河: 'jīng luò xīng hé',
  手太阴肺经: 'shǒu tài yīn fèi jīng',
  手阳明大肠经: 'shǒu yáng míng dà cháng jīng',
  足阳明胃经: 'zú yáng míng wèi jīng',
  足太阴脾经: 'zú tài yīn pí jīng',
  手少阴心经: 'shǒu shào yīn xīn jīng',
  手太阳小肠经: 'shǒu tài yáng xiǎo cháng jīng',
  足太阳膀胱经: 'zú tài yáng páng guāng jīng',
  足少阴肾经: 'zú shào yīn shèn jīng',
  手厥阴心包经: 'shǒu jué yīn xīn bāo jīng',
  手少阳三焦经: 'shǒu shào yáng sān jiāo jīng',
  足少阳胆经: 'zú shào yáng dǎn jīng',
  足厥阴肝经: 'zú jué yīn gān jīng',
  任脉: 'rèn mài',
  督脉: 'dū mài',
  经络: 'jīng luò',
  穴位: 'xué wèi',
  循行: 'xún xíng',
  星点: 'xīng diǎn',
  星路: 'xīng lù'
}

const PINYIN_PATTERN = new RegExp(Object.keys(PINYIN_TERMS).sort((a, b) => b.length - a.length).join('|'), 'g')

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[char])
}

function pinyinHtml(text) {
  if (!text) return ''
  return escapeHtml(text).replace(PINYIN_PATTERN, (term) => `<ruby>${term}<rt>${PINYIN_TERMS[term]}</rt></ruby>`)
}

watch(
  progressByRoute,
  (value) => {
    if (typeof window !== 'undefined' && !(window.localStorage.getItem('token') && window.localStorage.getItem('userInfo'))) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
    }
  },
  { deep: true }
)

watch(
  claimedByRoute,
  (value) => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(CLAIMED_KEY, JSON.stringify(value))
    }
  },
  { deep: true }
)

const selectedRoute = computed(() => routes.find((route) => route.id === selectedId.value) || routes[0])
const currentIndex = computed(() => progressByRoute.value[selectedRoute.value.id] || 0)

const groupedRoutes = computed(() => {
  const order = ['上肢', '下肢', '躯干']
  const groups = {}
  for (const route of routes) {
    const region = route.bodyRegion || '其他'
    if (!groups[region]) groups[region] = []
    groups[region].push(route)
  }
  return order.filter((r) => groups[r]).map((region) => ({
    region,
    routes: groups[region]
  }))
})
const completedRouteCount = computed(() => routes.filter((r) => isRouteDone(r.id)).length)
const routeCompleted = computed(() => currentIndex.value >= selectedRoute.value.points.length)
const rewardClaimed = computed(() => !!claimedByRoute.value[selectedRoute.value.id])
const linePoints = computed(() => selectedRoute.value.points.map((point) => `${point.x},${point.y}`).join(' '))
const progressRatio = computed(() => {
  const segmentCount = Math.max(selectedRoute.value.points.length - 1, 1)
  return Math.min(Math.max(currentIndex.value - 1, 0) / segmentCount, 1)
})
const progressPercent = computed(() => {
  if (selectedRoute.value.points.length === 0) return 0
  return Math.round((currentIndex.value / selectedRoute.value.points.length) * 100)
})
const lineDashOffset = computed(() => lineLength - lineLength * progressRatio.value)

const guideText = computed(() => {
  if (routeCompleted.value) return '整条星河已经亮起来了，可以领取奖励。'
  const nextPoint = selectedRoute.value.points[currentIndex.value]
  if (!nextPoint) return '全部点亮'
  return `下一颗星点：${nextPoint.name}`
})

const footerTitle = computed(() => routeCompleted.value
  ? '星河路线完成'
  : `已点亮 ${currentIndex.value}/${selectedRoute.value.points.length} 颗星`
)

const footerText = computed(() => {
  if (routeCompleted.value) return '领取奖励后，材料袋、地图状态和经络探险家徽章进度会同步更新。'
  return '按照经络循行顺序点击星点。点错时不会扣分，会帮你记录在星光修补册。'
})

const feedbackText = computed(() => {
  if (isAnswerCorrect.value && currentQuestion.value) {
    return currentQuestion.value.explanation
  }
  return '正确的回答能帮你更好地理解经络的奥秘哦！'
})

function isRouteUnlocked(routeId) {
  const route = routes.find((r) => r.id === routeId)
  if (!route) return false
  if (!route.unlockTaskId) return true
  if (routeId === 'meridian-lung-route') return true
  const prevRouteId = route.unlockTaskId
  const prevRoute = routes.find((r) => r.id === prevRouteId)
  const prevProgress = progressByRoute.value[prevRouteId] || 0
  if (prevRoute && prevProgress >= prevRoute.points.length) return true
  return !!claimedByRoute.value[prevRouteId]
}

function selectRoute(routeId) {
  if (!isRouteUnlocked(routeId)) {
    const route = routes.find((r) => r.id === routeId)
    message.info(`需要先${route?.unlockCondition || '完成前置任务'}才能解锁这条经络哦。`)
    return
  }

  const previousProgress = progressByRoute.value[routeId] || 0
  const routeObj = routes.find((r) => r.id === routeId)
  const isComplete = !!claimedByRoute.value[routeId]

  selectedId.value = routeId
  wrongPoint.value = ''
  currentRelationPoint.value = ''
  closeQuestionModal()

  if (previousProgress > 0 && !isComplete && previousProgress < (routeObj?.points.length || 0)) {
    showContinueDialog.value = true
  }
}

function isLit(index) {
  return index < currentIndex.value
}

function isRouteDone(routeId) {
  return !!claimedByRoute.value[routeId]
}

function canTapPoint(index) {
  if (!detectiveModeActive.value) return true
  return index === currentIndex.value
}

function isCurrentPoint(pointName) {
  return currentRelationPoint.value === pointName
}

function routeStatusText(route) {
  if (isRouteDone(route.id)) return '奖励已领取'
  if (!isRouteUnlocked(route.id)) return '未解锁'
  const progress = progressByRoute.value[route.id] || 0
  if (progress >= route.points.length) return '可领取奖励'
  return `${progress}/${route.points.length} 颗星点`
}

function starPointText(index) {
  if (index < currentIndex.value) return '已点亮'
  if (index === currentIndex.value && !routeCompleted.value) return '待点亮'
  return '等待中'
}

function sparkStyle(index) {
  return {
    left: `${7 + ((index * 23) % 87)}%`,
    top: `${8 + ((index * 37) % 80)}%`,
    animationDelay: `${(index % 9) * 0.28}s`
  }
}

function tapPoint(point, index) {
  if (savingProgress.value) return
  if (routeCompleted.value) {
    message.info('这条星河已经全部点亮啦。')
    return
  }

  wrongPoint.value = ''

  if (showRelationshipInfo.value) {
    currentRelationPoint.value = point.name
  }

  if (detectiveModeActive.value && index !== currentIndex.value) {
    wrongPoint.value = point.name
    const correctPoint = selectedRoute.value.points[currentIndex.value]
    addReviewRecord({
      id: `meridian-order-${selectedRoute.value.id}-${point.name}-${Date.now()}`,
      type: 'meridian',
      title: `${selectedRoute.value.name} 顺序星`,
      prompt: '这一步应该先点亮哪颗星？',
      correctAnswer: correctPoint?.name || '',
      choices: selectedRoute.value.points.map((item) => item.name),
      sourcePath: '/jingluo'
    })
    message.info('侦探模式中，请按经络循行顺序点亮。正在闪光的星点就是下一颗。')
    window.setTimeout(() => {
      if (wrongPoint.value === point.name) wrongPoint.value = ''
    }, 900)
    return
  }

  const questionPool = selectedRoute.value.questions || []
  if (questionPool.length > 0 && questionIndex.value < questionPool.length) {
    currentQuestion.value = questionPool[questionIndex.value]
    showQuestionModal.value = true
    answered.value = false
    isAnswerCorrect.value = false
    return
  }

  advanceProgress(point)
}

async function advanceProgress(point) {
  if (savingProgress.value) return false
  savingProgress.value = true
  const nextIndex = currentIndex.value + 1
  if (userStore.isLoggedIn && userStore.userId) {
    const result = await recordTaskProgress(selectedRoute.value.id, 1, {
      resultCode: 'ROUTE_POINT_COMPLETE',
      idempotencyKey: `game:${userStore.userId}:${selectedRoute.value.id}:point:${nextIndex}`
    })
    if (!result.success) {
      savingProgress.value = false
      message.error('进度保存失败，请检查网络后重试。')
      return false
    }
  }
  progressByRoute.value = {
    ...progressByRoute.value,
    [selectedRoute.value.id]: nextIndex
  }
  message.success(`${point.name} 已点亮`)

  if (nextIndex >= selectedRoute.value.points.length) {
    message.success('太棒了！整条星河全部点亮！')
  }

  savingProgress.value = false
  return true
}

function checkAnswer(option) {
  if (answered.value) return
  answered.value = true
  isAnswerCorrect.value = option === currentQuestion.value.correctAnswer

  if (isAnswerCorrect.value) {
    message.success('回答正确！知识储备 +1')
  } else {
    message.info('再想想，这颗穴位属于哪条经络呢？')
  }
}

function continueAfterQuestion() {
  const point = selectedRoute.value.points[currentIndex.value]
  if (isAnswerCorrect.value) {
    questionIndex.value++
    void advanceProgress(point)
  } else {
    message.info('先记住这个提示，想好后再回来回答。')
  }
  showQuestionModal.value = false
  answered.value = false
  currentQuestion.value = null
}

function closeQuestionModal() {
  showQuestionModal.value = false
  answered.value = false
  currentQuestion.value = null
}

function skipQuestion() {
  showQuestionModal.value = false
  answered.value = false
  currentQuestion.value = null
  message.info('这道题暂时保留，答对后才能点亮当前星点。')
}

async function claimReward() {
  if (!routeCompleted.value || rewardClaimed.value || claimingReward.value) return

  claimingReward.value = true
  const result = await completeStandaloneTask(selectedRoute.value.id, [
    { id: 'meridian-star-sand', count: 1 },
    { id: 'copper-token', count: 1 }
  ], {
    resultCode: 'ROUTE_COMPLETE',
    allowCompletedClaim: true,
    idempotencyKey: `game:${userStore.userId || 'guest'}:${selectedRoute.value.id}:claim`
  })

  if (result.success) {
    claimedByRoute.value = { ...claimedByRoute.value, [selectedRoute.value.id]: true }
    message.success('路线奖励已确认并放入材料袋。')
  } else {
    message.error('奖励领取失败，请稍后重试。')
  }
  claimingReward.value = false
}

function saveAndExit() {
  if (savingProgress.value) return
  showExitDialog.value = false
  message.success('进度已保存，下次回到这条经络可以继续点亮。')
}

async function abandonTask() {
  const routeId = selectedRoute.value.id
  if (rewardClaimed.value || isCompleted(routeId)) {
    showExitDialog.value = false
    message.info('已完成路线不能撤销奖励。')
    return
  }
  if (userStore.isLoggedIn && userStore.userId) {
    const result = await resetTaskProgress(routeId, {
      idempotencyKey: `game:${userStore.userId}:${routeId}:reset:${currentIndex.value}`
    })
    if (!result.success) {
      message.error('进度重置失败，请稍后重试。')
      return
    }
  }
  progressByRoute.value = { ...progressByRoute.value, [routeId]: 0 }
  showExitDialog.value = false
  message.info('任务已重置，这条经络的星点恢复为未点亮状态。')
}

function resetProgress() {
  const routeId = selectedRoute.value.id
  if (rewardClaimed.value || isCompleted(routeId)) {
    showContinueDialog.value = false
    message.info('已完成路线不能重新清除。')
    return
  }
  progressByRoute.value = { ...progressByRoute.value, [routeId]: 0 }
  showContinueDialog.value = false
  message.info('已从头开始，请重新点亮星点。')
}
</script>

<style scoped>
.meridian-page {
  position: relative;
  isolation: isolate;
  min-height: 100vh;
  padding: 34px clamp(18px, 4vw, 56px) 56px;
  color: var(--site-ink);
  background:
    radial-gradient(ellipse at 18% 16%, rgba(84, 182, 161, 0.11), transparent 32%),
    radial-gradient(ellipse at 84% 12%, rgba(244, 201, 93, 0.14), transparent 30%),
    radial-gradient(ellipse at 50% 78%, rgba(90, 167, 216, 0.08), transparent 38%),
    linear-gradient(155deg, var(--site-paper-deep) 0%, var(--site-background) 48%, #fffaf0 100%);
  font-family: var(--site-body-font), sans-serif;
}

.river-hero,
.unlock-chain,
.river-layout,
.game-controls,
.river-footer {
  position: relative;
  z-index: 1;
}

.river-hero,
.river-layout,
.river-footer,
.game-controls {
  max-width: 1180px;
  margin: 0 auto 24px;
}

.river-hero {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: end;
}

.river-eyebrow,
.river-reward span,
.river-footer span {
  color: var(--site-copper);
  font-weight: 900;
}

.river-hero h1 {
  max-width: 800px;
  margin: 12px 0;
  color: var(--site-primary-dark);
  font-family: var(--site-title-font), serif;
  font-size: clamp(34px, 4.8vw, 64px);
  line-height: 1.08;
  letter-spacing: 0;
}

.river-hero p {
  max-width: 760px;
  margin: 0;
  color: var(--site-muted-ink);
  font-size: 16px;
  line-height: 1.8;
}

.river-reward {
  display: grid;
  gap: 10px;
  min-width: 220px;
  padding: 18px;
  border: 1px solid rgba(184, 115, 51, 0.34);
  border-radius: 8px;
  background: rgba(255, 247, 223, 0.88);
  box-shadow: 0 10px 26px rgba(111, 66, 31, 0.08), inset 0 0 24px rgba(244, 201, 93, 0.12);
}

.river-reward strong {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(244, 201, 93, 0.2);
  color: var(--site-copper-dark);
}

.river-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 22px;
}

.route-picker {
  display: grid;
  gap: 8px;
  align-content: start;
  max-height: calc(100vh - 260px);
  overflow-y: auto;
  padding-right: 6px;
  scrollbar-width: thin;
  scrollbar-color: rgba(184, 115, 51, 0.35) transparent;
}

.route-group-header {
  padding: 6px 10px 4px;
  margin-top: 2px;
  font-size: 13px;
  font-weight: 700;
  color: var(--site-copper-dark);
  letter-spacing: 0.08em;
  border-bottom: 1px solid rgba(184, 115, 51, 0.25);
}

.route-picker::-webkit-scrollbar {
  width: 5px;
}

.route-picker::-webkit-scrollbar-track {
  background: transparent;
}

.route-picker::-webkit-scrollbar-thumb {
  background: rgba(184, 115, 51, 0.35);
  border-radius: 3px;
}

.route-intro {
  padding: 8px 4px;
  margin-bottom: 4px;
}

.route-intro h4 {
  margin: 0 0 4px;
  color: var(--site-primary-dark);
  font-size: 18px;
}

.route-intro p {
  margin: 0;
  color: var(--site-muted-ink);
  font-size: 13px;
}

.unlock-chain {
  max-width: 1200px;
  margin: 0 auto 28px;
  padding: 18px 24px 20px;
  background: rgba(255, 247, 223, 0.78);
  border: 1px solid rgba(184, 115, 51, 0.2);
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(111, 66, 31, 0.06);
}

.unlock-chain-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.unlock-chain-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--site-copper-dark);
  letter-spacing: 0.04em;
}

.unlock-chain-progress {
  font-size: 15px;
  font-weight: 700;
  color: var(--site-muted-ink);
}

.unlock-chain-track {
  position: relative;
  height: 52px;
  padding: 0 20px;
}

.unlock-chain-line {
  position: absolute;
  top: 50%;
  left: 20px;
  right: 20px;
  height: 4px;
  background: rgba(184, 115, 51, 0.16);
  transform: translateY(-50%);
  border-radius: 2px;
}

.unlock-chain-fill {
  position: absolute;
  top: 50%;
  left: 20px;
  height: 4px;
  background: linear-gradient(90deg, rgba(84, 182, 161, 0.68), var(--site-copper));
  transform: translateY(-50%);
  border-radius: 2px;
  transition: width 0.5s ease;
}

.unlock-chain-dot {
  position: absolute;
  top: 50%;
  width: 36px;
  height: 36px;
  transform: translate(-50%, -50%);
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--site-paper);
  border: 2.5px solid rgba(184, 115, 51, 0.28);
  transition: all 0.3s;
  cursor: pointer;
}

.unlock-chain-dot.done {
  background: rgba(244, 201, 93, 0.28);
  border-color: var(--site-copper);
  box-shadow: 0 0 12px rgba(244, 201, 93, 0.32);
}

.unlock-chain-dot.current {
  background: rgba(84, 182, 161, 0.22);
  border-color: var(--site-primary);
  box-shadow: 0 0 16px rgba(84, 182, 161, 0.35);
  transform: translate(-50%, -50%) scale(1.2);
}

.unlock-chain-dot.locked {
  opacity: 0.35;
  cursor: not-allowed;
}

.unlock-chain-mark {
  font-size: 13px;
  font-weight: 800;
  color: var(--site-primary-dark);
}

.route-picker button {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 6px 12px;
  min-height: 104px;
  padding: 16px;
  text-align: left;
  color: var(--site-ink);
  background: rgba(255, 247, 223, 0.86);
  border: 1px solid rgba(184, 115, 51, 0.24);
  border-radius: 8px;
  cursor: pointer;
  transition: transform 180ms ease, border-color 180ms ease, background 180ms ease;
}

.route-picker button:hover:not(:disabled),
.route-picker button:focus-visible:not(:disabled),
.route-picker button.active {
  border-color: rgba(184, 115, 51, 0.72);
  background: rgba(244, 201, 93, 0.2);
  outline: none;
  transform: translateY(-2px);
}

.route-picker button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.route-picker button.done {
  border-color: var(--site-accent);
}

.route-picker button.locked {
  border-style: dashed;
  border-color: rgba(184, 115, 51, 0.14);
  background: rgba(255, 247, 223, 0.5);
}

.route-picker span {
  display: grid;
  grid-row: span 2;
  width: 46px;
  height: 46px;
  place-items: center;
  color: var(--site-paper);
  font-size: 22px;
  font-weight: 950;
  background: var(--site-primary);
  border-radius: 50%;
}

.route-picker strong {
  color: var(--site-primary-dark);
  font-size: 17px;
  line-height: 1.25;
}

.route-picker small {
  color: var(--site-muted-ink);
}

.star-board {
  position: relative;
  min-height: 560px;
  overflow: hidden;
  border: 1px solid rgba(184, 115, 51, 0.46);
  border-radius: 8px;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(84, 182, 161, 0.18), transparent 35%),
    radial-gradient(ellipse at 70% 60%, rgba(90, 167, 216, 0.12), transparent 30%),
    radial-gradient(ellipse at 50% 85%, rgba(244, 201, 93, 0.11), transparent 35%),
    radial-gradient(ellipse at 15% 75%, rgba(47, 125, 104, 0.16), transparent 28%),
    linear-gradient(145deg, #1f4f48, #173b38 55%, #214b46);
  box-shadow: 0 24px 60px rgba(36, 59, 52, 0.22), inset 0 0 100px rgba(244, 201, 93, 0.08);
  transition: box-shadow 0.6s ease;
}

.star-board.complete {
  box-shadow: 0 0 70px rgba(255, 216, 109, 0.34), inset 0 0 110px rgba(255, 216, 109, 0.2);
}

.star-board.detective-mode {
  border-color: rgba(244, 201, 93, 0.7);
}

.star-board__sky {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.star-spark {
  position: absolute;
  border-radius: 50%;
  animation: star-twinkle 3.2s ease-in-out infinite;
}

.star-spark--sm {
  width: 2px;
  height: 2px;
  background: rgba(255, 241, 194, 0.58);
  box-shadow: 0 0 6px rgba(244, 201, 93, 0.35);
}

.star-spark--md {
  width: 3px;
  height: 3px;
  background: rgba(255, 247, 223, 0.72);
  box-shadow: 0 0 12px rgba(244, 201, 93, 0.48);
}

.star-spark--lg {
  width: 5px;
  height: 5px;
  background: rgba(255, 247, 223, 0.88);
  box-shadow: 0 0 20px rgba(244, 201, 93, 0.72), 0 0 40px rgba(244, 201, 93, 0.22);
}

.star-board.complete .star-spark--sm {
  background: #ffe8b8;
  box-shadow: 0 0 10px rgba(255, 216, 109, 0.6);
}

.star-board.complete .star-spark--md {
  background: #ffe28a;
  box-shadow: 0 0 16px rgba(255, 216, 109, 0.8);
}

.star-board.complete .star-spark--lg {
  background: #fff2c0;
  box-shadow: 0 0 28px rgba(255, 216, 109, 0.9), 0 0 56px rgba(255, 200, 80, 0.4);
}

.star-lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.star-line {
  stroke-linecap: round;
  stroke-linejoin: round;
}

.star-line--base {
  stroke: rgba(216, 238, 207, 0.32);
  stroke-dasharray: 3 3;
  stroke-width: 1.8;
}

.star-line--lit {
  filter: url('#river-glow');
  stroke: var(--site-accent);
  stroke-width: 2.4;
  transition: stroke-dashoffset 520ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

.star-board.complete .star-line--lit {
  animation: route-breath 1.9s ease-in-out infinite;
}

.star-point {
  position: absolute;
  z-index: 2;
  display: grid;
  width: 80px;
  min-height: 72px;
  place-items: center;
  gap: 4px;
  padding: 10px 8px 8px;
  color: #e8f0df;
  background: transparent;
  border: none;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: transform 240ms ease, filter 240ms ease;
}

.star-point::before {
  content: '';
  position: absolute;
  inset: 2px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 35%, rgba(120, 205, 184, 0.24), rgba(18, 61, 55, 0.72) 80%);
  box-shadow: 0 0 20px rgba(84, 182, 161, 0.2);
  transition: box-shadow 320ms ease, background 320ms ease;
  z-index: -1;
}

.star-point:hover:not(:disabled),
.star-point:focus-visible:not(:disabled) {
  outline: none;
  transform: translate(-50%, -54%);
}

.star-point:hover:not(:disabled)::before,
.star-point:focus-visible:not(:disabled)::before {
  box-shadow: 0 0 36px rgba(84, 182, 161, 0.42);
}

.star-point:disabled {
  cursor: not-allowed;
}

.star-point.locked {
  filter: grayscale(0.5) brightness(0.45);
}

.star-point.lit.locked {
  filter: none;
}

.star-point.locked::before {
  background: radial-gradient(circle at 40% 35%, rgba(120, 137, 125, 0.12), rgba(20, 52, 48, 0.64) 80%);
  box-shadow: 0 0 8px rgba(18, 61, 55, 0.18);
}

.star-point.current::before {
  background: radial-gradient(circle at 40% 35%, rgba(244, 201, 93, 0.36), rgba(24, 75, 67, 0.72) 80%);
  box-shadow: 0 0 0 4px rgba(244, 201, 93, 0.25), 0 0 45px rgba(244, 201, 93, 0.32);
  animation: planet-pulse 2s ease-in-out infinite;
}

.star-point.lit::before {
  background: radial-gradient(circle at 40% 35%, rgba(255, 247, 223, 0.68), rgba(244, 201, 93, 0.58) 80%);
  box-shadow: 0 0 35px rgba(244, 201, 93, 0.8), 0 0 70px rgba(184, 115, 51, 0.42), 0 0 100px rgba(184, 115, 51, 0.2);
}

.star-point.wrong {
  animation: wrong-shake 0.4s ease;
}

.star-point.wrong::before {
  background: radial-gradient(circle at 40% 35%, rgba(203, 130, 103, 0.34), rgba(74, 47, 39, 0.7) 80%);
  box-shadow: 0 0 30px rgba(184, 115, 51, 0.35);
}

.star-point.belonging-highlight::before {
  background: radial-gradient(circle at 40% 35%, rgba(100, 200, 170, 0.35), rgba(30, 60, 55, 0.7) 80%);
  box-shadow: 0 0 36px rgba(84, 182, 161, 0.55), 0 0 70px rgba(84, 182, 161, 0.25);
}

.planet-orbital {
  position: absolute;
  inset: -12px;
  border-radius: 50%;
  border: 2.5px solid rgba(216, 238, 207, 0.24);
  transform: rotateX(75deg);
  pointer-events: none;
  transition: border-color 320ms ease, box-shadow 320ms ease;
  box-shadow: 0 0 6px rgba(84, 182, 161, 0.16);
}

.star-point.lit .planet-orbital {
  border-color: rgba(244, 201, 93, 0.7);
  box-shadow: 0 0 16px rgba(244, 201, 93, 0.42);
}

.star-point.current .planet-orbital {
  border-color: rgba(244, 201, 93, 0.55);
  box-shadow: 0 0 10px rgba(244, 201, 93, 0.3);
}

.star-point.locked .planet-orbital {
  border-color: rgba(120, 137, 125, 0.18);
  box-shadow: none;
}

.planet-body {
  position: relative;
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 50%;
  background: radial-gradient(circle at 38% 32%, rgba(120, 205, 184, 0.5), rgba(18, 61, 55, 0.84) 75%);
  box-shadow: inset 0 -3px 6px rgba(0, 0, 0, 0.3), 0 0 12px rgba(84, 182, 161, 0.25);
  transition: background 320ms ease, box-shadow 320ms ease;
}

.star-point.lit .planet-body {
  background: radial-gradient(circle at 38% 32%, #fffbe6, #ffcc44 70%);
  box-shadow: inset 0 -2px 4px rgba(0, 0, 0, 0.08), 0 0 24px rgba(244, 201, 93, 0.9), 0 0 50px rgba(184, 115, 51, 0.5), 0 0 80px rgba(184, 115, 51, 0.24);
}

.star-point.current .planet-body {
  background: radial-gradient(circle at 38% 32%, rgba(244, 201, 93, 0.62), rgba(24, 75, 67, 0.82) 75%);
  box-shadow: inset 0 -3px 6px rgba(0, 0, 0, 0.25), 0 0 18px rgba(244, 201, 93, 0.42);
}

.star-point.belonging-highlight .planet-body {
  background: radial-gradient(circle at 38% 32%, rgba(120, 210, 180, 0.5), rgba(25, 60, 50, 0.75) 75%);
  box-shadow: inset 0 -3px 6px rgba(0, 0, 0, 0.25), 0 0 20px rgba(84, 182, 161, 0.5);
}

.planet-body::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: -16px;
  right: -16px;
  height: 14px;
  border-radius: 0 0 50% 50%;
  border: 2.5px solid rgba(216, 238, 207, 0.24);
  border-top: none;
  pointer-events: none;
  transition: border-color 320ms ease;
}

.star-point.lit .planet-body::after {
  border-color: rgba(244, 201, 93, 0.65);
}

.star-point.current .planet-body::after {
  border-color: rgba(244, 201, 93, 0.5);
}

.star-point.locked .planet-body::after {
  border-color: rgba(120, 137, 125, 0.18);
}

.planet-num {
  color: #e8f0df;
  font-size: 14px;
  font-weight: 950;
  text-shadow: 0 0 8px rgba(84, 182, 161, 0.45);
  transition: color 320ms ease, text-shadow 320ms ease;
}

.star-point.lit .planet-num {
  color: #fff;
  text-shadow: 0 0 14px rgba(255, 240, 180, 1), 0 0 28px rgba(244, 201, 93, 0.8);
}

.star-point strong {
  color: #e8f0df;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.5px;
  transition: color 320ms ease;
}

.star-point.lit strong {
  color: #fff1c2;
}

.star-point small {
  color: #b9d0c1;
  font-size: 11px;
  font-weight: 700;
  transition: color 320ms ease;
}

.star-point.lit small {
  color: #f4c95d;
}

@keyframes planet-pulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(255, 216, 109, 0.25), 0 0 50px rgba(255, 200, 100, 0.35); }
  50% { box-shadow: 0 0 0 8px rgba(255, 216, 109, 0.1), 0 0 70px rgba(255, 220, 130, 0.5); }
}

.detective-hint {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 3;
  max-width: 280px;
  padding: 14px 18px;
  background: rgba(244, 201, 93, 0.16);
  border: 1px solid rgba(184, 115, 51, 0.42);
  border-radius: 8px;
  backdrop-filter: blur(8px);
}

.hint-badge {
  font-size: 14px;
  font-weight: 800;
  color: var(--site-copper);
  margin-bottom: 6px;
}

.detective-hint p {
  margin: 0;
  font-size: 13px;
  color: #f7efd2;
  line-height: 1.6;
}

.relationship-card {
  position: absolute;
  right: 18px;
  bottom: 120px;
  z-index: 3;
  max-width: 320px;
  padding: 18px;
  background: rgba(255, 249, 232, 0.95);
  border: 1px solid rgba(84, 182, 161, 0.48);
  border-radius: 10px;
  color: #14322d;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(8px);
}

.relationship-header {
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(84, 182, 161, 0.24);
}

.relationship-header strong {
  font-size: 15px;
  color: var(--site-primary);
}

.relationship-chain {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.chain-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 6px;
  min-width: 70px;
}

.chain-item.body {
  background: rgba(84, 182, 161, 0.12);
  border: 1px solid rgba(84, 182, 161, 0.3);
}

.chain-item.meridian {
  background: rgba(244, 201, 93, 0.16);
  border: 1px solid rgba(244, 201, 93, 0.42);
}

.chain-item.acupoint {
  background: rgba(184, 115, 51, 0.12);
  border: 1px solid rgba(184, 115, 51, 0.3);
}

.chain-label {
  font-size: 10px;
  font-weight: 700;
  color: #8c8c8c;
  text-transform: uppercase;
}

.chain-value {
  font-size: 13px;
  font-weight: 800;
  color: #14322d;
}

.chain-arrow {
  font-size: 16px;
  color: var(--site-safe);
  font-weight: 900;
}

.relationship-desc {
  margin: 0;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
}

.river-guide {
  position: absolute;
  left: 24px;
  bottom: 24px;
  z-index: 3;
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 12px;
  max-width: 420px;
  padding: 16px 18px;
  border: 1px solid rgba(184, 115, 51, 0.36);
  border-radius: 8px;
  background: rgba(255, 247, 223, 0.94);
  color: var(--site-primary-dark);
}

.river-guide > span {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  color: var(--site-paper);
  background: var(--site-primary);
  border-radius: 50%;
  font-weight: 950;
}

.river-guide p {
  margin: 6px 0 0;
  color: var(--site-muted-ink);
}

.meridian-info,
.body-region-info {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  align-items: flex-start;
}

.info-label {
  font-size: 12px;
  font-weight: 800;
  color: var(--site-primary);
  white-space: nowrap;
}

.info-text {
  font-size: 12px;
  color: var(--site-muted-ink);
  line-height: 1.5;
}

.game-controls {
  display: flex;
  justify-content: center;
  padding: 0;
}

.control-group {
  display: flex;
  gap: 28px;
  padding: 16px 28px;
  background: rgba(255, 247, 223, 0.84);
  border: 1px solid rgba(184, 115, 51, 0.24);
  box-shadow: 0 8px 22px rgba(111, 66, 31, 0.06);
  border-radius: 8px;
}

.control-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  color: var(--site-ink);
  user-select: none;
}

.control-label input[type="checkbox"] {
  display: none;
}

.checkmark {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(184, 115, 51, 0.5);
  border-radius: 4px;
  background: transparent;
  transition: background 0.2s ease, border-color 0.2s ease;
  flex-shrink: 0;
}

.control-label input:checked + .checkmark {
  background: var(--site-primary);
  border-color: var(--site-primary);
}

.river-footer {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 22px 24px;
  border: 1px solid rgba(184, 115, 51, 0.32);
  border-radius: 8px;
  background: rgba(255, 247, 223, 0.86);
  box-shadow: 0 10px 26px rgba(111, 66, 31, 0.07), inset 0 0 32px rgba(244, 201, 93, 0.08);
}

.river-footer.complete {
  background: linear-gradient(135deg, rgba(244, 201, 93, 0.28), rgba(84, 182, 161, 0.18));
}

.river-footer.in-progress {
  border-color: rgba(184, 115, 51, 0.48);
  background: linear-gradient(135deg, rgba(244, 201, 93, 0.16), rgba(84, 182, 161, 0.1));
}

.river-footer h2 {
  margin: 6px 0;
  color: var(--site-primary-dark);
}

.river-footer p {
  margin: 0;
  color: var(--site-muted-ink);
}

.progress-bar {
  position: relative;
  height: 10px;
  margin-top: 14px;
  background: rgba(184, 115, 51, 0.12);
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--site-safe), var(--site-accent));
  border-radius: 999px;
  transition: width 0.5s ease;
}

.progress-text {
  position: absolute;
  right: 0;
  top: -20px;
  font-size: 12px;
  font-weight: 800;
  color: var(--site-copper);
}

.game-mode-switch {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
}

.question-overlay {
  position: absolute;
  inset: 0;
  background: rgba(36, 59, 52, 0.68);
  backdrop-filter: blur(6px);
}

.question-modal {
  position: relative;
  z-index: 2;
  width: min(90vw, 520px);
  max-height: 85vh;
  overflow-y: auto;
  padding: 32px;
  background: linear-gradient(160deg, var(--site-paper), var(--site-paper-deep));
  border: 1px solid rgba(184, 115, 51, 0.52);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(36, 59, 52, 0.24);
  color: var(--site-primary-dark);
}

.question-header {
  text-align: center;
  margin-bottom: 24px;
}

.question-badge {
  display: inline-block;
  padding: 4px 16px;
  font-size: 13px;
  font-weight: 800;
  color: var(--site-primary);
  background: rgba(84, 182, 161, 0.14);
  border-radius: 999px;
  margin-bottom: 12px;
}

.question-header h3 {
  margin: 0;
  font-size: 22px;
  color: var(--site-primary-dark);
}

.question-body {
  margin-bottom: 24px;
}

.question-text {
  font-size: 16px;
  line-height: 1.7;
  color: var(--site-ink);
  margin: 0 0 20px;
  text-align: center;
}

.question-options {
  display: grid;
  gap: 10px;
}

.option-btn {
  display: block;
  width: 100%;
  padding: 14px 18px;
  font-size: 15px;
  text-align: left;
  color: var(--site-ink);
  background: #fffdf5;
  border: 2px solid rgba(84, 182, 161, 0.24);
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.option-btn:hover:not(:disabled) {
  border-color: rgba(84, 182, 161, 0.68);
  background: rgba(84, 182, 161, 0.08);
}

.option-btn:disabled {
  cursor: default;
}

.option-btn.option-correct {
  border-color: var(--site-safe);
  background: rgba(84, 182, 161, 0.14);
  color: var(--site-primary);
}

.option-btn.option-wrong {
  border-color: #c7831c;
  background: rgba(244, 201, 93, 0.12);
}

.question-footer {
  text-align: center;
}

.feedback {
  padding: 14px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.feedback.correct {
  background: rgba(84, 182, 161, 0.1);
  border: 1px solid rgba(84, 182, 161, 0.34);
}

.feedback.wrong {
  background: rgba(244, 201, 93, 0.1);
  border: 1px solid rgba(184, 115, 51, 0.3);
}

.feedback strong {
  display: block;
  font-size: 15px;
  margin-bottom: 6px;
  color: var(--site-primary-dark);
}

.feedback p {
  margin: 0;
  font-size: 13px;
  color: var(--site-muted-ink);
  line-height: 1.6;
}

.question-close {
  position: absolute;
  top: 12px;
  right: 16px;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  font-size: 20px;
  color: #8c8c8c;
  background: none;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.question-close:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--site-primary-dark);
}

.question-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.footer-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.meridian-page :deep(.ant-btn) {
  border-color: rgba(184, 115, 51, 0.32);
  border-radius: 12px;
  color: var(--site-ink);
  background: var(--site-paper);
  box-shadow: 0 4px 12px rgba(111, 66, 31, 0.06);
}

.meridian-page :deep(.ant-btn:hover),
.meridian-page :deep(.ant-btn:focus-visible) {
  color: var(--site-primary-dark);
  border-color: var(--site-copper);
  background: var(--site-highlight);
}

.meridian-page :deep(.ant-btn-primary) {
  color: var(--site-paper);
  border-color: var(--site-primary);
  background: var(--site-primary);
}

.meridian-page :deep(.ant-btn-primary:hover),
.meridian-page :deep(.ant-btn-primary:focus-visible) {
  color: var(--site-paper);
  border-color: var(--site-primary-dark);
  background: var(--site-primary-dark);
}

.exit-dialog,
.continue-dialog {
  position: fixed;
  inset: 0;
  z-index: 110;
  display: flex;
  align-items: center;
  justify-content: center;
}

.exit-overlay {
  position: absolute;
  inset: 0;
  background: rgba(36, 59, 52, 0.68);
  backdrop-filter: blur(6px);
}

.exit-modal {
  position: relative;
  z-index: 2;
  width: min(90vw, 460px);
  padding: 28px;
  background: linear-gradient(160deg, var(--site-paper), var(--site-paper-deep));
  border: 1px solid rgba(184, 115, 51, 0.52);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(36, 59, 52, 0.24);
  color: var(--site-primary-dark);
}

.exit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(84, 182, 161, 0.24);
}

.exit-header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--site-primary-dark);
}

.continue-badge {
  padding: 3px 12px;
  font-size: 12px;
  font-weight: 700;
  color: var(--site-copper-dark);
  background: rgba(244, 201, 93, 0.16);
  border: 1px solid rgba(184, 115, 51, 0.3);
  border-radius: 999px;
}

.exit-close {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  font-size: 20px;
  color: #8c8c8c;
  background: none;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.exit-close:hover {
  background: rgba(0, 0, 0, 0.06);
  color: #333;
}

.exit-body {
  margin-bottom: 24px;
}

.exit-body p {
  margin: 0 0 8px;
  font-size: 15px;
  color: var(--site-ink);
  line-height: 1.6;
}

.exit-body strong {
  color: var(--site-primary);
}

.exit-reward-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 16px;
  background: rgba(244, 201, 93, 0.14);
  border: 1px solid rgba(184, 115, 51, 0.3);
  border-radius: 8px;
}

.exit-reward-preview span {
  font-size: 13px;
  color: #8c8c8c;
}

.exit-reward-preview strong {
  font-size: 15px;
  color: var(--site-copper-dark);
}

.exit-footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@keyframes star-twinkle {
  0%, 100% { opacity: 0.25; transform: scale(0.7); }
  30% { opacity: 0.9; transform: scale(1.4); }
  60% { opacity: 0.45; transform: scale(0.85); }
  85% { opacity: 1; transform: scale(1.2); }
}

@keyframes route-breath {
  0%, 100% { stroke-width: 2.4; }
  50% { stroke-width: 3.4; }
}

@keyframes wrong-shake {
  0%, 100% { transform: translate(-50%, -50%); }
  25% { transform: translate(-54%, -50%); }
  75% { transform: translate(-46%, -50%); }
}

@media (max-width: 900px) {
  .river-hero,
  .river-layout {
    grid-template-columns: 1fr;
  }

  .river-hero {
    padding-bottom: 112px;
  }

  .route-picker {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    max-height: 360px;
    overflow-y: auto;
  }

  .star-board {
    min-height: 480px;
  }

  .control-group {
    flex-direction: column;
    gap: 14px;
  }

  .relationship-card {
    position: relative;
    right: auto;
    bottom: auto;
    max-width: none;
    margin: 14px;
  }

  .detective-hint {
    position: relative;
    top: auto;
    right: auto;
    max-width: none;
    margin: 14px;
  }
}

@media (max-width: 640px) {
  .meridian-page {
    padding-inline: 14px;
    padding-top: 24px;
  }

  .river-hero {
    gap: 16px;
    padding-bottom: 100px;
  }

  .route-picker {
    grid-template-columns: 1fr;
    max-height: 320px;
    overflow-y: auto;
  }

  .star-board {
    width: 100%;
    min-width: 0;
    min-height: 420px;
  }

  .river-layout {
    min-width: 0;
    overflow-x: hidden;
  }

  .river-hero h1 {
    font-size: clamp(34px, 10vw, 44px);
  }

  .river-hero p {
    font-size: 14px;
    line-height: 1.7;
  }

  .river-reward {
    min-width: 0;
  }

  .match-game-btn,
  .match-game-btn.sort-btn {
    top: auto;
    bottom: 16px;
    width: calc(50% - 8px);
    padding: 9px 10px;
    font-size: 13px;
    letter-spacing: 0.2px;
    white-space: nowrap;
  }

  .match-game-btn {
    right: 14px;
  }

  .match-game-btn.sort-btn {
    right: auto;
    left: 14px;
  }

  .meridian-page :deep(rt) {
    display: none;
  }

  .river-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .river-footer > div:first-child {
    min-width: 0;
  }

  .footer-actions {
    display: grid;
    width: 100%;
    grid-template-columns: 1fr;
  }

  .footer-actions :deep(.ant-btn) {
    width: 100%;
    min-width: 0;
    white-space: normal;
  }

  .tutorial-tooltip {
    width: calc(100vw - 28px);
    max-height: min(76vh, 520px);
    padding: 20px 16px 16px;
  }

  .tutorial-nav {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .tutorial-nav .ant-btn {
    min-width: 0;
    width: 100%;
  }
}

/* ========== 拼音注音样式 ========== */
.meridian-page ruby {
  ruby-align: center;
}

.meridian-page rt {
  font-size: 0.52em;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: 0.02em;
  color: var(--site-copper);
  user-select: none;
}

/* hero 区域拼音颜色适配 */
.river-hero ruby rt,
.river-hero rt {
  color: var(--site-copper);
}

/* 侧边栏拼音颜色 */
.route-picker ruby rt,
.route-picker rt {
  color: var(--site-muted-blue);
}

/* 星点标签拼音 */
.star-point ruby rt {
  font-size: 0.48em;
  color: #c5e1d4;
}

/* 底部拼音 */
.river-footer ruby rt,
.river-footer rt {
  color: #6d9382;
}

/* 弹窗拼音 */
.exit-modal ruby rt {
  color: #c7831c;
}

.question-modal ruby rt {
  color: #c7831c;
}

/* 归属关系卡片拼音 */
.relationship-card ruby rt {
  color: #d4a84b;
}

/* 侦探提示拼音 */
.detective-hint ruby rt {
  color: #e8c86a;
}

/* ========== 使用教学按钮 ========== */
.tutorial-replay {
  position: absolute;
  top: 18px;
  right: 18px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--site-copper-dark);
  background: rgba(255, 247, 223, 0.78);
  border: 1px solid rgba(184, 115, 51, 0.24);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.tutorial-replay:hover {
  color: var(--site-primary-dark);
  background: var(--site-highlight);
  border-color: rgba(184, 115, 51, 0.48);
}

.match-game-btn {
  position: absolute;
  top: 52px;
  right: 18px;
  z-index: 10;
  padding: 10px 22px;
  font-size: 16px;
  font-weight: 700;
  color: var(--site-paper);
  background: linear-gradient(135deg, rgba(47, 125, 104, 0.98), rgba(36, 91, 80, 0.98));
  border: 2px solid rgba(244, 201, 93, 0.72);
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s;
  letter-spacing: 1px;
  box-shadow: 0 8px 18px rgba(36, 91, 80, 0.2);
}

.match-game-btn:hover {
  color: var(--site-paper);
  background: linear-gradient(135deg, var(--site-primary), var(--site-primary-dark));
  border-color: var(--site-accent);
  box-shadow: 0 10px 24px rgba(36, 91, 80, 0.28);
  transform: translateY(-2px);
}

.match-game-btn.sort-btn {
  top: 100px;
  color: var(--site-copper-dark);
  background: linear-gradient(135deg, rgba(244, 201, 93, 0.95), rgba(184, 115, 51, 0.92));
  border-color: rgba(111, 66, 31, 0.36);
  box-shadow: 0 8px 18px rgba(111, 66, 31, 0.16);
}

.match-game-btn.sort-btn:hover {
  color: var(--site-copper-dark);
  background: linear-gradient(135deg, #ffe29a, var(--site-copper));
  border-color: var(--site-copper-dark);
  box-shadow: 0 10px 24px rgba(111, 66, 31, 0.24);
}

/* ========== 聚光灯教学引导 ========== */
.tutorial-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
}

.tutorial-overlay--no-target::before {
  content: '';
  position: fixed;
  inset: 0;
  z-index: 0;
  background: rgba(36, 59, 52, 0.68);
  pointer-events: auto;
}

.tutorial-spotlight {
  position: fixed;
  background: transparent;
  border-radius: 8px;
  box-shadow: 0 0 0 9999px rgba(36, 59, 52, 0.68);
  z-index: 1;
  pointer-events: none;
}

.tutorial-spotlight::after {
  content: '';
  position: absolute;
  inset: -8px;
  border: 2px solid rgba(184, 115, 51, 0.86);
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(244, 201, 93, 0.5);
}

.tutorial-tooltip {
  position: fixed;
  z-index: 2;
  pointer-events: auto;
  width: 90%;
  max-width: 520px;
  padding: 24px 24px 20px;
  max-height: min(82vh, 520px);
  overflow-y: auto;
  background: linear-gradient(160deg, rgba(255, 247, 223, 0.99), rgba(248, 237, 210, 0.99));
  border: 1px solid rgba(184, 115, 51, 0.5);
  border-radius: 16px;
  box-shadow: 0 18px 52px rgba(36, 59, 52, 0.24), 0 0 0 1px rgba(244, 201, 93, 0.12);
  text-align: center;
  animation: tutorialSlideIn 0.3s ease;
}

@keyframes tutorialSlideIn {
  from {
    opacity: 0;
    filter: blur(4px);
  }
  to {
    opacity: 1;
    filter: blur(0);
  }
}

.tutorial-step-indicator {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-bottom: 18px;
}

.tutorial-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(184, 115, 51, 0.2);
  transition: all 0.3s;
}

.tutorial-dot.active {
  background: var(--site-copper);
  box-shadow: 0 0 6px rgba(244, 201, 93, 0.52);
  transform: scale(1.3);
}

.tutorial-dot.done {
  background: rgba(184, 115, 51, 0.46);
}

.tutorial-content {
  margin-bottom: 20px;
}

.tutorial-emoji {
  display: block;
  font-size: 42px;
  margin-bottom: 8px;
  line-height: 1;
}

.tutorial-content h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--site-primary-dark);
  font-weight: 800;
}

.tutorial-content p {
  margin: 0;
  font-size: 14px;
  color: var(--site-muted-ink);
  line-height: 1.6;
}

.tutorial-nav {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tutorial-nav .ant-btn {
  min-width: 76px;
  font-size: 13px;
}

/* 高亮目标元素 */
.tutorial-highlight {
  position: relative;
  z-index: 10000 !important;
}

/* 教学卡片拼音 */
.tutorial-tooltip ruby rt {
  color: var(--site-copper);
}

@media (prefers-reduced-motion: reduce) {
  .meridian-page :deep(*) {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>

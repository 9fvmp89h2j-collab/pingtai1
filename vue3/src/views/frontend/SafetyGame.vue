<template>
  <main class="safety-game" :style="{ '--safety-background': `url(${safetyBackground})` }">
    <div class="particles-layer">
      <div v-for="p in particles" :key="p.id" class="particle" :class="p.type" :style="{ left: p.x + '%', top: p.y + '%', '--dx': p.dx + 'px', '--dy': p.dy + 'px' }"></div>
    </div>

    <section v-if="gamePhase === 'intro'" class="phase-intro">
      <div class="intro-shell">
        <div class="intro-card parchment-panel">
          <img class="intro-guardian" :src="detectiveSafety" alt="小铜人安全守护者" />
          <p class="intro-eyebrow"><SafetyCertificateOutlined /> 安全守护者训练营</p>
          <h1 class="intro-title"><span>安全</span>小课堂</h1>
          <p class="intro-desc">亲爱的小侦探，危险可能藏在生活的每个角落。<br />一起学习安全知识，成为守护自己和他人的<strong>安全小达人</strong>吧！</p>

          <div class="intro-stages" aria-label="安全训练四个阶段">
            <template v-for="(stage, index) in stageCards" :key="stage.name">
              <article class="intro-stage" :class="`stage-tone-${index + 1}`">
                <span class="stage-number">{{ index + 1 }}</span>
                <span class="stage-icon"><img :src="stage.image" :alt="`${stage.name}素材`" /></span>
                <span class="stage-name">{{ stage.name }}</span>
                <span class="stage-count">{{ stage.count }}</span>
              </article>
              <span v-if="index < stageCards.length - 1" class="intro-stage-arrow" aria-hidden="true">›</span>
            </template>
          </div>

          <div class="intro-rules">
            <div class="rule-chip"><ThunderboltOutlined /><span>答对得分，连击加成</span></div>
            <div class="rule-chip"><HeartFilled /><span>3条生命，答错扣一条</span></div>
            <div class="rule-chip"><AppstoreOutlined /><span>翻牌配对，考验记忆</span></div>
            <div class="rule-chip"><ClockCircleOutlined /><span>情境关卡，选对就得星</span></div>
          </div>

          <div v-if="quizLoadError" class="quiz-load-error" role="alert">
            <span>{{ quizLoadError }}</span>
            <button type="button" @click="loadQuizStage">重新加载</button>
          </div>
          <button class="intro-btn" :disabled="quizLoading || quizQuestions.length !== 14" @click="startGame">
            <SafetyCertificateOutlined />{{ quizLoading ? '正在准备题目…' : '开始挑战' }}
          </button>
          <p class="intro-safety-note"><SafetyCertificateOutlined /> 只观察、只学习，不自行针刺或模仿治疗</p>
        </div>

        <aside class="intro-side-panel">
          <div class="side-progress-card">
            <div class="side-card-heading"><span>本次训练进度</span><small>{{ completedStageCount }}/4</small></div>
            <div class="side-progress-track"><span :style="{ width: `${completedStageCount * 25}%` }"></span></div>
            <div class="side-badges-title">守护徽章</div>
            <div class="side-badges">
              <img :src="safetyObservationBadge" alt="安全观察徽章" />
              <img :src="bellSprite" alt="安全铃铛" />
              <span class="locked-badge"><SafetyCertificateOutlined /></span>
            </div>
          </div>
          <img class="intro-side-character" :src="detectiveEncourage" alt="鼓励孩子的小铜人" />
          <img class="intro-bell-wall" :src="safetyBellWall" alt="安全铃铛墙" />
        </aside>
      </div>
    </section>

    <section v-else-if="gamePhase === 'quiz'" class="phase-quiz">
      <div class="quiz-topbar">
        <button class="phase-back" @click="exitSafetyGame"><ArrowLeftOutlined />保存并返回</button>
        <div class="topbar-center">
          <div class="phase-badge"><BulbOutlined /><span>第一关 · 知识闯关</span></div>
          <div class="quiz-progress-wrap">
            <div class="quiz-progress-bar"><div class="quiz-progress-fill" :style="{ width: (quizIndex / quizQuestions.length) * 100 + '%' }"></div></div>
            <span class="quiz-progress-text">{{ quizIndex }} / {{ quizQuestions.length }}</span>
          </div>
        </div>
        <div class="topbar-stats">
          <div class="stat-lives"><HeartFilled v-for="i in 3" :key="i" class="life-heart" :class="{ lost: i > quizLives }" /></div>
          <div class="stat-score"><StarFilled /><span class="score-num">{{ totalScore }}</span></div>
          <div class="stat-combo" v-if="combo > 1"><ThunderboltOutlined /><span>{{ combo }}连击</span></div>
        </div>
      </div>

      <div class="quiz-card-wrap" :key="quizIndex">
        <div class="quiz-bell-character">
          <img :src="quizGuideImage" alt="小铜人安全引导员" />
          <div class="quiz-bell-speech" v-if="quizFeedback">{{ quizFeedback }}</div>
        </div>
        <div class="quiz-question-card" :class="[{ 'quiz-revealed': quizRevealed, 'quiz-correct': quizRevealed && quizIsCorrect, 'quiz-wrong': quizRevealed && !quizIsCorrect }, `quiz-layout-${currentQuiz?.type || 'default'}`]">
          <aside class="quiz-scene-panel">
            <div class="scene-kicker"><AimOutlined /> 现场观察 · 第{{ quizIndex + 1 }}题</div>
            <img v-if="currentQuiz?.sceneImage" class="quiz-scene-image" :src="currentQuiz.sceneImage" :alt="currentQuiz.sceneImageAlt" />
            <div v-else class="quiz-scene-placeholder">
              <img :src="quizGuideImage" alt="小铜人安全观察员" />
              <span>{{ currentQuiz?.type === 'scenario' ? '场景插画待补充' : '先观察，再判断' }}</span>
            </div>
            <p class="scene-caption"><BulbOutlined />{{ currentQuiz?.sceneCaption || '只观察、只学习，危险动作不模仿' }}</p>
            <div v-if="quizRevealed" class="scene-result-stamp" :class="quizIsCorrect ? 'correct' : 'wrong'">
              {{ quizIsCorrect ? '守护成功' : '安全提醒' }}
            </div>
          </aside>
          <div class="quiz-copy-panel">
          <div class="quiz-question-front" v-if="!quizRevealed">
            <div class="quiz-type-tag"><BookOutlined v-if="currentQuiz?.type === 'scenario'" /><CheckOutlined v-else />{{ currentQuiz?.type === 'scenario' ? '场景模拟' : '判断题' }}</div>
            <h2 class="quiz-q-text">{{ currentQuiz?.name }}</h2>
            <div v-if="currentQuiz?.type === 'true-false'" class="quiz-options-row">
              <button v-for="opt in currentQuiz?.options || []" :key="opt.label" class="quiz-opt-btn" :class="[{ selected: quizSelected === opt.value }, opt.value === 'A' ? 'option-yes' : 'option-no']" @click="selectQuizAnswer(opt.value)">
                <span class="answer-symbol" :class="opt.value === 'A' ? 'answer-symbol-yes' : 'answer-symbol-no'"><CheckOutlined v-if="opt.value === 'A'" /><CloseOutlined v-else /></span>{{ opt.text }}
              </button>
            </div>
            <div v-else class="quiz-options-col">
              <button v-for="opt in currentQuiz?.options || []" :key="opt.label" class="quiz-opt-btn scenario-opt" :class="{ selected: quizSelected === opt.value }" @click="selectQuizAnswer(opt.value)">
                <span class="opt-letter">{{ opt.label }}</span><span class="opt-text">{{ opt.text }}</span>
              </button>
            </div>
            <button class="quiz-submit-btn" :disabled="quizSelected === null || quizSubmitting" @click="submitQuizAnswer">{{ quizSubmitting ? '正在确认…' : '确认答案' }}</button>
          </div>
          <div class="quiz-question-back" v-else>
            <img class="quiz-result-character" :src="quizIsCorrect ? detectiveEncourage : detectiveSafety" :alt="quizIsCorrect ? '小铜人鼓励' : '小铜人安全提醒'" />
            <h2 class="quiz-result-title">{{ quizIsCorrect ? '回答正确！' : '回答错误' }}</h2>
            <p class="quiz-explanation">{{ quizExplanation }}</p>
            <div class="quiz-result-score" v-if="quizIsCorrect">+{{ quizScoreGained }} 分<span v-if="combo > 1" class="combo-bonus">（含连击加成 ×{{ combo }}）</span></div>
            <button class="quiz-next-btn" @click="nextQuizQuestion">{{ quizIndex >= quizQuestions.length - 1 ? '进入下一关' : '下一题' }}<span aria-hidden="true">›</span></button>
          </div>
          </div>
        </div>
        <img class="quiz-side-character" :src="detectiveDiscover" alt="发现安全线索的小铜人" />
        <div class="quiz-stars">
          <span v-for="i in quizQuestions.length" :key="i" class="quiz-star" :class="{ earned: quizStars[i-1] === true, wrong: quizStars[i-1] === false, current: i === quizIndex + 1 && !quizRevealed }">
            <StarFilled v-if="quizStars[i-1] === true" />
            <CloseOutlined v-else-if="quizStars[i-1] === false" />
            <StarOutlined v-else />
            <small>{{ i }}</small>
          </span>
        </div>
        <p class="quiz-reward-note"><StarFilled /> 答对可获得星星奖励，集满星星完成安全守护训练</p>
      </div>

      <div class="quiz-game-over" v-if="quizLives <= 0">
        <div class="game-over-card">
          <HeartFilled class="game-over-icon" />
          <h2>生命值耗尽！</h2>
          <p>别担心，安全知识需要反复学习。<br />再来一次吧！</p>
          <button class="game-over-retry" @click="retryQuiz"><ReloadOutlined />重新挑战</button>
        </div>
      </div>
    </section>

    <section v-else-if="gamePhase === 'memory'" class="phase-memory">
      <div class="memory-topbar">
        <button class="phase-back" @click="exitSafetyGame"><ArrowLeftOutlined />保存并返回</button>
        <div class="topbar-center"><div class="phase-badge"><AppstoreOutlined /><span>第二关 · 记忆翻牌</span></div></div>
        <div class="topbar-stats">
          <div class="stat-memory-info"><AppstoreOutlined /><span>翻牌 {{ memoryFlipCount }} 次</span></div>
          <div class="stat-memory-info"><CheckOutlined /><span>配对 {{ memoryMatchedCount }}/{{ memoryPairs.length }}</span></div>
          <div class="stat-score"><StarFilled /><span class="score-num">{{ totalScore }}</span></div>
        </div>
      </div>

      <div class="memory-play-area">
        <div class="memory-bell-guide">
          <img :src="memoryFeedback ? detectiveDiscover : detectiveThink" :alt="memoryFeedback ? '发现配对线索的小铜人' : '思考线索的小铜人'" />
          <div class="memory-bell-msg">{{ memoryFeedback || '翻开卡片，找到配对的安全知识吧！' }}</div>
        </div>
        <div class="memory-grid">
          <div v-for="(card, idx) in memoryCards" :key="idx" class="memory-card" :class="{ flipped: card.flipped, matched: card.matched, 'is-left': card.isLeft }" @click="flipMemoryCard(idx)">
            <div class="memory-card-inner">
              <div class="memory-card-front">
                <span class="memory-card-question">?</span>
              </div>
              <div class="memory-card-back" :class="card.isLeft ? 'card-left' : 'card-right'">
                <span class="card-icon" v-if="card.icon"><img :src="bellSprite" alt="安全铃铛" /></span>
                <span class="card-text">{{ card.text }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="memory-complete" v-if="memoryAllMatched">
        <div class="memory-complete-card">
          <img class="phase-result-character" :src="detectiveReward" alt="获得奖励的小铜人" />
          <h2>全部配对成功！</h2>
          <p class="memory-complete-desc">翻牌 {{ memoryFlipCount }} 次，获得了 {{ memoryScoreGained }} 分</p>
          <div class="memory-tips">
            <p class="memory-tips-title"><BookOutlined />你记住的安全知识：</p>
            <div class="memory-tip-item" v-for="pair in memoryPairs" :key="pair.id">
              <span class="tip-left">{{ pair.left }}</span><span class="tip-arrow">→</span><span class="tip-right">{{ pair.right }}</span>
            </div>
          </div>
          <button class="memory-next-btn" @click="goToSort">进入下一关<span aria-hidden="true">›</span></button>
        </div>
      </div>
    </section>

    <section v-else-if="gamePhase === 'sort'" class="phase-sort scenario-sort">
      <div class="sort-topbar">
        <button class="phase-back" @click="exitSafetyGame"><ArrowLeftOutlined />保存并返回</button>
        <div class="topbar-center"><div class="phase-badge"><AimOutlined /><span>第三关 · 情境选择</span></div></div>
        <div class="topbar-stats">
          <div class="stat-memory-info"><HeartFilled /><span>生命 {{ scenarioLives }}/3</span></div>
          <div class="stat-score"><StarFilled /><span class="score-num">{{ totalScore }}</span></div>
        </div>
      </div>
      <div class="sort-play-area">
        <div class="sort-bell-guide">
          <img :src="sortFeedback ? detectiveDiscover : detectiveSafety" :alt="sortFeedback ? '发现分类线索的小铜人' : '小铜人安全提醒'" />
          <div class="sort-bell-msg">{{ scenarioFeedback || '看看情境，选出安全行动吧！' }}</div>
        </div>
        <article class="scenario-card">
          <div class="scenario-visual"><img :src="scenario.image" alt=""><span>{{ scenarioIndex + 1 }}</span></div>
          <div class="scenario-copy"><small>生活小情境</small><h2>{{ scenario.title }}</h2><p>{{ scenario.prompt }}</p>
            <div class="scenario-options"><button type="button" class="scenario-safe" :class="{ selected: scenarioChoice === true }" :disabled="scenarioAnswered" @click="chooseScenario(true)">✓ {{ scenario.safe }}</button><button type="button" class="scenario-stop" :class="{ selected: scenarioChoice === false }" :disabled="scenarioAnswered" @click="chooseScenario(false)">✕ {{ scenario.unsafe }}</button></div>
            <div v-if="scenarioAnswered" class="scenario-feedback" :class="{ correct: scenarioCorrect }"><strong>{{ scenarioCorrect ? '太棒啦！安全小侦探 +1' : scenarioFailed ? '生命值用完，请重新开始本关' : '再想一想，保护自己最重要' }}</strong><p>{{ scenario.explanation }}</p><button v-if="!scenarioCorrect && !scenarioFailed" type="button" @click="retryScenario">重新选择</button><button v-else-if="scenarioFailed" type="button" @click="retryScenarioStage">重新挑战本关</button><button v-else type="button" @click="nextScenario">{{ scenarioIndex === scenarios.length - 1 ? '完成关卡' : '下一情境' }}</button></div>
          </div>
        </article>
      </div>
    </section>

    <section v-else-if="gamePhase === 'pledge'" class="phase-pledge">
      <div class="pledge-scene">
        <div class="pledge-header">
          <button class="phase-back" @click="exitSafetyGame"><ArrowLeftOutlined />保存并返回</button>
          <div class="phase-badge"><SafetyCertificateOutlined /><span>第四关 · 安全宣誓</span></div>
          <div class="stat-score"><StarFilled /><span class="score-num">{{ totalScore }}</span></div>
        </div>
        <div class="pledge-stage">
          <img class="pledge-bell-wall" :src="safetyBellWall" alt="安全铃铛墙" />
          <div class="pledge-bell-large"><img :src="detectiveSafety" alt="小铜人安全守护者" /><div class="pledge-rays" v-if="pledgeAllLit"></div></div>
          <div class="pledge-title-area"><h1 class="pledge-title">安全小卫士宣誓</h1><p class="pledge-subtitle">点击每一盏灯，郑重承诺遵守安全规则</p></div>
          <div class="pledge-items">
            <div v-for="(item, idx) in pledgeItems" :key="idx" class="pledge-item" :class="{ lit: item.lit, animating: item.animating }" @click="lightPledge(idx)">
              <div class="pledge-lamp"><img v-if="item.lit" :src="bellSprite" alt="已点亮安全铃铛" /><SafetyCertificateOutlined v-else /></div>
              <div class="pledge-text"><p class="pledge-item-title"><CheckOutlined v-if="item.lit" />{{ item.title }}</p><p class="pledge-item-desc">{{ item.desc }}</p></div>
              <StarFilled class="pledge-check" v-if="item.lit" />
            </div>
          </div>
          <div class="pledge-progress-wrap"><div class="pledge-progress-bar"><div class="pledge-progress-fill" :style="{ width: (pledgeLitCount / pledgeItems.length) * 100 + '%' }"></div></div><span>{{ pledgeLitCount }} / {{ pledgeItems.length }} 已点亮</span></div>
          <div class="pledge-ceremony" v-if="pledgeAllLit">
            <div class="ceremony-card">
              <img class="ceremony-character" :src="detectiveReward" alt="获得安全奖励的小铜人" />
              <img class="ceremony-shield" :src="safetyObservationBadge" alt="安全观察徽章" />
              <h2 class="ceremony-title">恭喜通关！</h2>
              <div class="ceremony-final-score"><span class="final-score-label">最终得分</span><span class="final-score-num">{{ totalScore }}</span></div>
              <p class="ceremony-text">你已正式成为<strong>杏林安全小卫士</strong>！<br />记住今天的誓言，在探索中医世界的路上，<br />始终保持安全意识。</p>
              <div class="ceremony-badge"><SafetyCertificateOutlined /><span>获得徽章：安全小卫士</span></div>
              <div class="ceremony-achievements" v-if="unlockedAchievements.length > 0">
                <p class="ach-title"><TrophyOutlined />额外成就</p>
                <div class="ach-list">
                  <div v-for="ach in unlockedAchievements" :key="ach.id" class="ach-chip">
                    <TrophyOutlined class="ach-icon" /><span>{{ ach.name }}</span>
                  </div>
                </div>
              </div>
              <div class="ceremony-rewards">
                <div class="reward-item"><img :src="bellSprite" alt="安全铃铛" class="reward-icon" /><span>安全铃铛 × 5</span></div>
                <div class="reward-item"><img :src="mugwortIcon" alt="艾绒" class="reward-icon" /><span>艾绒 × 3</span></div>
              </div>
              <button class="ceremony-btn" @click="finishGame"><CheckOutlined />完成训练</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section v-else-if="gamePhase === 'complete'" class="phase-complete">
      <div class="complete-card">
        <img class="complete-character" :src="detectiveComplete" alt="完成任务的小铜人" />
        <div class="complete-stars"><StarFilled v-for="i in 3" :key="i" class="complete-star" /></div>
        <div class="complete-cert-ribbon">证书</div>
        <h1 class="complete-title">训练完成！</h1>
        <p class="complete-desc">你已经完成了安全守护者训练营的全部关卡，<br />现在可以安全地探索杏林世界啦！</p>
        <div class="complete-stats">
          <div class="stat-item"><span class="stat-num">{{ quizCorrectCount }}/{{ quizQuestions.length }}</span><span class="stat-label">知识问答</span></div>
          <div class="stat-item"><span class="stat-num">{{ memoryMatchedCount }}/{{ memoryPairs.length }}</span><span class="stat-label">记忆翻牌</span></div>
          <div class="stat-item"><span class="stat-num"><CheckOutlined v-if="sortAllCorrect" /><span v-else>完成</span></span><span class="stat-label">行为分类</span></div>
          <div class="stat-item"><span class="stat-num"><SafetyCertificateOutlined /></span><span class="stat-label">安全宣誓</span></div>
          <div class="stat-item highlight"><span class="stat-num">{{ totalScore }}</span><span class="stat-label">总分</span></div>
        </div>
        <div class="complete-achievements" v-if="unlockedAchievements.length > 0">
          <p class="complete-ach-title"><TrophyOutlined />解锁成就</p>
          <div class="complete-ach-list">
            <div v-for="ach in unlockedAchievements" :key="ach.id" class="complete-ach-item">
              <TrophyOutlined class="complete-ach-icon" />
              <div class="complete-ach-info">
                <span class="complete-ach-name">{{ ach.name }}</span>
                <span class="complete-ach-desc">{{ ach.description }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="complete-btns">
          <button class="complete-replay-btn" @click="resetGame"><ReloadOutlined />再次训练</button>
          <button class="complete-home-btn" @click="router.push('/home-map')"><HomeOutlined />返回地图</button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  AimOutlined,
  AppstoreOutlined,
  ArrowLeftOutlined,
  BookOutlined,
  BulbOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  CloseOutlined,
  HeartFilled,
  HomeOutlined,
  ReloadOutlined,
  SafetyCertificateOutlined,
  StarFilled,
  StarOutlined,
  ThunderboltOutlined,
  TrophyOutlined
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { safetyQuestions, safetyPledge, achievements } from '@/data/safetyQuestionsGame'
import { useGameState } from '@/composables/useGameState'
import { useUserStore } from '@/store/user'
import { clearSafetyGameDraft, getSafetyGameDraft, saveSafetyGameDraft } from '@/api/GameApi'
import { getQuizStage, submitQuizStageAnswer } from '@/api/QuizApi'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import safetyBackground from '@/assets/maps/xinglin-detective-map-bg.png'

const bellSprite = generatedMaterialIcons['safety-bell']
const mugwortIcon = generatedMaterialIcons['mugwort-floss']
const detectiveSafety = generatedRewardAssets.detectiveSafety
const detectiveDiscover = generatedRewardAssets.detectiveDiscover
const detectiveThink = generatedRewardAssets.detectiveThink
const detectiveEncourage = generatedRewardAssets.detectiveEncourage
const detectiveReward = generatedRewardAssets.detectiveReward
const detectiveComplete = generatedRewardAssets.detectiveComplete
const safetyObservationBadge = generatedRewardAssets.safetyObservationBadge
const safetyBellWall = generatedRewardAssets.safetyBellWall

const router = useRouter()
const userStore = useUserStore()
const { addMaterials, completeStandaloneTask } = useGameState()

const gamePhase = ref('intro')
const totalScore = ref(0)
const combo = ref(0)
const maxCombo = ref(0)

const particles = ref([])
let particleId = 0
function spawnParticles(type, count = 8) {
  const newParticles = []
  for (let i = 0; i < count; i++) {
    newParticles.push({
      id: ++particleId,
      type,
      x: 30 + Math.random() * 40,
      y: 20 + Math.random() * 40,
      dx: (Math.random() - 0.5) * 120,
      dy: -(60 + Math.random() * 80)
    })
  }
  particles.value = [...particles.value, ...newParticles]
  setTimeout(() => { particles.value = particles.value.filter(p => !newParticles.includes(p)) }, 1200)
}

const SAFETY_STAGE_CODE = 'SAFETY_KNOWLEDGE'
const quizQuestions = ref([])
const quizStageRevision = ref(null)
const quizLoading = ref(false)
const quizLoadError = ref('')
const quizIndex = ref(0)
const quizSelected = ref(null)
const quizRevealed = ref(false)
const quizFeedback = ref('')
const quizCorrectCount = ref(0)
const quizIsCorrect = ref(false)
const quizLives = ref(3)
const quizScoreGained = ref(0)
const quizSubmitting = ref(false)
const quizExplanation = ref('')
const quizStars = ref([])
const currentQuiz = computed(() => quizQuestions.value[quizIndex.value] || null)
const quizGuideImage = computed(() => currentQuiz.value?.type === 'scenario' ? detectiveThink : detectiveSafety)

async function loadQuizStage() {
  quizLoading.value = true
  quizLoadError.value = ''
  try {
    const stage = await getQuizStage(SAFETY_STAGE_CODE, { showDefaultMsg: false })
    quizStageRevision.value = stage?.revision ?? null
    quizQuestions.value = (stage?.questions || []).map(question => ({
      id: question.id,
      code: question.questionCode,
      type: question.questionType === 'TRUE_FALSE' ? 'true-false' : 'scenario',
      name: question.title,
      sceneImage: resolveMediaUrl(question.sceneImagePath || ''),
      sceneImageAlt: question.sceneImageAlt || '安全情境插画',
      sceneCaption: question.sceneCaption || '',
      options: (question.options || []).map(option => ({
        label: option.key,
        text: option.text,
        value: option.key
      }))
    }))
    if (quizQuestions.value.length !== 14) throw new Error('第一关题库尚未配置完整')
  } catch (error) {
    quizQuestions.value = []
    quizStageRevision.value = null
    quizLoadError.value = error?.message || '题目加载失败，请稍后重试'
  } finally {
    quizLoading.value = false
  }
}

async function startGame() {
  if (quizQuestions.value.length !== 14) {
    await loadQuizStage()
    if (quizQuestions.value.length !== 14) return
  }
  await clearSafetyDraft()
  gamePhase.value = 'quiz'
  quizIndex.value = 0; quizSelected.value = null; quizRevealed.value = false
  quizFeedback.value = ''; quizCorrectCount.value = 0; quizIsCorrect.value = false
  quizLives.value = 3; quizScoreGained.value = 0; quizExplanation.value = ''; totalScore.value = 0; combo.value = 0
  maxCombo.value = 0; quizStars.value = new Array(quizQuestions.value.length).fill(null)
  particles.value = []
}
function selectQuizAnswer(value) { if (!quizRevealed.value) quizSelected.value = value }
async function submitQuizAnswer() {
  if (quizSelected.value === null || quizSubmitting.value) return
  quizSubmitting.value = true
  try {
    const result = await submitQuizStageAnswer(SAFETY_STAGE_CODE, {
      stageRevision: quizStageRevision.value,
      questionId: currentQuiz.value.id,
      userAnswer: quizSelected.value
    }, { showDefaultMsg: false })
    const correct = Boolean(result?.correct)
    quizExplanation.value = result?.explanation || '记住安全规则，遇到不确定的情况要及时告诉可信任的大人。'
    quizIsCorrect.value = correct; quizRevealed.value = true
    if (correct) {
      combo.value++
      if (combo.value > maxCombo.value) maxCombo.value = combo.value
      const base = 100; const bonus = Math.min(combo.value - 1, 5) * 20
      quizScoreGained.value = base + bonus; totalScore.value += quizScoreGained.value
      quizCorrectCount.value++; quizStars.value[quizIndex.value] = true
      quizFeedback.value = combo.value >= 3 ? `太厉害了！${combo.value}连击！` : '答对啦！你真是安全小达人！'
      spawnParticles('star', 10)
    } else {
      combo.value = 0; quizLives.value--; quizScoreGained.value = 0; quizStars.value[quizIndex.value] = false
      quizFeedback.value = quizLives.value > 0 ? '答错了，记住正确行动，下次一定能守护成功！' : '生命值耗尽了，请重新挑战。'
      spawnParticles('fail', 5)
    }
  } catch (error) {
    const text = error?.message || '答案提交失败，请重试'
    if (text.includes('题库已更新')) {
      message.warning(text)
      await clearSafetyDraft()
      await loadQuizStage()
      retryQuiz()
    } else {
      message.error(text)
    }
  } finally {
    quizSubmitting.value = false
  }
}
function nextQuizQuestion() {
  if (quizLives.value <= 0) return
  if (quizIndex.value >= quizQuestions.value.length - 1) { gamePhase.value = 'memory'; initMemory(); return }
  quizIndex.value++; quizSelected.value = null; quizRevealed.value = false; quizFeedback.value = ''; quizExplanation.value = ''
}
function retryQuiz() {
  quizIndex.value = 0; quizSelected.value = null; quizRevealed.value = false
  quizFeedback.value = ''; quizExplanation.value = ''; quizCorrectCount.value = 0; quizIsCorrect.value = false
  quizLives.value = 3; combo.value = 0; maxCombo.value = 0
  quizStars.value = new Array(quizQuestions.value.length).fill(null)
}

const memoryQuestion = computed(() => safetyQuestions.find(q => q.type === 'memory'))
const memoryPairs = computed(() => memoryQuestion.value?.pairs || [])
const memoryCards = ref([])
const memoryFlipped = ref([])
const memoryMatchedCount = ref(0)
const memoryFlipCount = ref(0)
const memoryFeedback = ref('')
const memoryScoreGained = ref(0)
let memoryLocked = false
const memoryAllMatched = computed(() => memoryMatchedCount.value === memoryPairs.value.length)

function shuffleArray(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]] }
  return a
}
function initMemory() {
  const cards = []
  memoryPairs.value.forEach(pair => {
    cards.push({ pairId: pair.id, text: pair.left, icon: pair.icon, isLeft: true, flipped: false, matched: false })
    cards.push({ pairId: pair.id, text: pair.right, icon: null, isLeft: false, flipped: false, matched: false })
  })
  memoryCards.value = shuffleArray(cards)
  memoryFlipped.value = []; memoryMatchedCount.value = 0; memoryFlipCount.value = 0
  memoryFeedback.value = ''; memoryScoreGained.value = 0; memoryLocked = false
}
function flipMemoryCard(idx) {
  if (memoryLocked) return
  const card = memoryCards.value[idx]
  if (card.flipped || card.matched) return
  if (memoryFlipped.value.length >= 2) return
  card.flipped = true; memoryFlipCount.value++
  memoryFlipped.value.push(idx)
  if (memoryFlipped.value.length === 2) {
    memoryLocked = true
    const [a, b] = memoryFlipped.value
    const cardA = memoryCards.value[a]; const cardB = memoryCards.value[b]
    if (cardA.pairId === cardB.pairId && cardA.isLeft !== cardB.isLeft) {
      cardA.matched = true; cardB.matched = true; memoryMatchedCount.value++
      totalScore.value += 80; memoryFeedback.value = '配对成功！'
      spawnParticles('star', 6)
      memoryFlipped.value = []; memoryLocked = false
      if (memoryAllMatched.value) {
        const base = 400; const bonus = Math.max(0, 30 - memoryFlipCount.value) * 10
        memoryScoreGained.value = base + bonus + memoryMatchedCount.value * 80
        totalScore.value += base + bonus
        memoryFeedback.value = '全部完成！记忆力真棒！'
        spawnParticles('star', 20)
      }
    } else {
      memoryFeedback.value = '不太对哦，再仔细想想。'
      setTimeout(() => {
        cardA.flipped = false; cardB.flipped = false
        memoryFlipped.value = []; memoryLocked = false
      }, 800)
    }
  }
}
function goToSort() { gamePhase.value = 'sort'; initSort() }

const stageCards = computed(() => [
  { name: '知识闯关', count: `${quizQuestions.value.length}题`, image: detectiveDiscover },
  { name: '记忆翻牌', count: `${memoryPairs.value.length}对`, image: detectiveThink },
  { name: '情境选择', count: '6个情境', image: detectiveSafety },
  { name: '安全宣誓', count: `${safetyPledge.items?.length || 6}条`, image: safetyObservationBadge }
])
const completedStageCount = computed(() => ({ intro: 0, quiz: 0, memory: 1, sort: 2, pledge: 3, complete: 4 }[gamePhase.value] || 0))
const sortPlacedSafe = ref([]); const sortPlacedUnsafe = ref([]); const sortPlacedNeedAdult = ref([])
const sortDraggingIdx = ref(null); const sortDragOver = ref(null)
const sortFeedback = ref(''); const sortShowResult = ref(false); const sortAllCorrect = ref(false)
const sortResultMessage = ref(''); const sortScoreGained = ref(0); const sortTimer = ref(60)
let sortTimerInterval = null
const scenarios = [
  { title: '身体有点不舒服', prompt: '肚肚不舒服了，你会怎么做？', safe: '告诉家长', unsafe: '自己找针试试', explanation: '不舒服要告诉家长，请医生来判断。', image: detectiveSafety },
  { title: '发现了小针具', prompt: '桌上有尖尖的针具，你会怎么做？', safe: '请大人收好', unsafe: '拿来玩一玩', explanation: '尖锐物品很危险，不能拿来玩。', image: detectiveDiscover },
  { title: '看安全科普动画', prompt: '学习穴位动画时，正确的做法是？', safe: '认真观察学习', unsafe: '照着给自己扎针', explanation: '动画帮助我们学习知识，不能代替专业操作。', image: detectiveSafety },
  { title: '想认识一个穴位', prompt: '你有问题时，可以向谁请教？', safe: '问老师或医生', unsafe: '自己上网乱试', explanation: '有疑问要问可信赖的大人和专业人员。', image: detectiveDiscover },
  { title: '同学说想试一试', prompt: '同学请你帮忙做治疗，你会？', safe: '礼貌拒绝并告诉老师', unsafe: '偷偷帮他操作', explanation: '小朋友不能给别人针刺或治疗。', image: detectiveSafety },
  { title: '学习身体小知识', prompt: '认识身体地图时，安全的方式是？', safe: '看图认识位置', unsafe: '模仿治疗动作', explanation: '我们只观察、只学习，不模仿治疗。', image: detectiveDiscover }
]
const scenarioIndex = ref(0); const scenarioChoice = ref(null); const scenarioScore = ref(0); const scenarioLives = ref(3); const scenarioFeedback = ref('')
const scenario = computed(() => scenarios[scenarioIndex.value]); const scenarioAnswered = computed(() => scenarioChoice.value !== null); const scenarioCorrect = computed(() => scenarioChoice.value === true); const scenarioFailed = computed(() => scenarioAnswered.value && !scenarioCorrect.value && scenarioLives.value <= 0)
function chooseScenario(value) { if (scenarioAnswered.value || scenarioLives.value <= 0) return; scenarioChoice.value = value; if (value) { scenarioScore.value += 1; totalScore.value += 100; scenarioFeedback.value = '安全行动选得好！' } else { scenarioLives.value = Math.max(0, scenarioLives.value - 1); scenarioFeedback.value = scenarioLives.value > 0 ? '小铜人提醒你再想一想～' : '生命值用完了，请重新挑战本关。' } }
function retryScenario() { scenarioChoice.value = null; scenarioFeedback.value = '' }
function retryScenarioStage() { initSort() }
function nextScenario() { if (scenarioIndex.value === scenarios.length - 1) { totalScore.value += 300; goToPledge() } else { scenarioIndex.value += 1; scenarioChoice.value = null; scenarioFeedback.value = '' } }
function initSort() {
  scenarioIndex.value = 0; scenarioChoice.value = null; scenarioScore.value = 0; scenarioLives.value = 3; scenarioFeedback.value = ''
  sortPlacedSafe.value = []; sortPlacedUnsafe.value = []; sortPlacedNeedAdult.value = []
  sortDraggingIdx.value = null; sortDragOver.value = null; sortFeedback.value = ''
  sortShowResult.value = false; sortAllCorrect.value = false; sortResultMessage.value = ''; sortScoreGained.value = 0
  sortTimer.value = 60
}
onBeforeUnmount(() => {
  clearInterval(sortTimerInterval)
  void saveSafetyDraft(true)
})

const pledgeItems = ref([
  { title: '不自行针刺', desc: '我承诺：针灸是专业医疗行为，我不会自己拿针扎自己或他人。', lit: false, animating: false },
  { title: '不模仿治疗', desc: '我承诺：学习穴位是为了了解身体，我不会模仿治疗操作。', lit: false, animating: false },
  { title: '不替代医生', desc: '我承诺：系统知识只用于学习，生病了要找专业医生。', lit: false, animating: false },
  { title: '及时求助', desc: '我承诺：身体不舒服时，第一时间告诉家长并去医院。', lit: false, animating: false },
  { title: '不碰针具', desc: '我承诺：我不捡、不碰、不玩针灸针具，发现针具立即告诉大人。', lit: false, animating: false },
  { title: '传播安全', desc: '我承诺：我会提醒身边的小朋友，针灸安全知识，一起做安全小卫士。', lit: false, animating: false }
])
const pledgeLitCount = computed(() => pledgeItems.value.filter(i => i.lit).length)
const pledgeAllLit = computed(() => pledgeLitCount.value === pledgeItems.value.length)
function lightPledge(idx) {
  if (pledgeItems.value[idx].lit) return
  pledgeItems.value[idx].animating = true; totalScore.value += 50; spawnParticles('star', 6)
  setTimeout(() => { pledgeItems.value[idx].lit = true; pledgeItems.value[idx].animating = false }, 400)
}
function goToPledge() { sortShowResult.value = false; gamePhase.value = 'pledge'; pledgeItems.value.forEach(i => { i.lit = false; i.animating = false }) }

const unlockedAchievements = computed(() => {
  const stats = {
    quizCorrectCount: quizCorrectCount.value,
    quizTotalCount: quizQuestions.value.length,
    maxCombo: maxCombo.value,
    sortTimeRemaining: sortTimer.value,
    sortAllCorrect: sortAllCorrect.value,
    quizLivesRemaining: quizLives.value,
    memoryFlips: memoryFlipCount.value
  }
  return achievements.filter(a => a.condition(stats))
})

const DRAFT_VERSION = 2
let draftReady = false
let serverDraftSaveTimer = null

function getDraftStorageKey() {
  try {
    const userInfo = JSON.parse(window.localStorage.getItem('userInfo') || 'null')
    return `xinglin-safety-game-draft-v${DRAFT_VERSION}:${userInfo?.id || 'guest'}`
  } catch {
    return `xinglin-safety-game-draft-v${DRAFT_VERSION}:guest`
  }
}

async function clearSafetyDraft() {
  if (typeof window !== 'undefined') window.localStorage.removeItem(getDraftStorageKey())
  clearTimeout(serverDraftSaveTimer)
  serverDraftSaveTimer = null
  if (!userStore.isLoggedIn || !userStore.userId) return
  try {
    await clearSafetyGameDraft()
  } catch (error) {
    console.warn('服务端安全课堂进度清除失败', error)
  }
}

function buildSafetyDraft() {
  return {
    version: DRAFT_VERSION,
    savedAt: Date.now(),
    gamePhase: gamePhase.value,
    totalScore: totalScore.value,
    combo: combo.value,
    maxCombo: maxCombo.value,
    quiz: {
      stageRevision: quizStageRevision.value,
      questionIds: quizQuestions.value.map(question => question.id),
      index: quizIndex.value,
      selected: quizSelected.value,
      revealed: quizRevealed.value,
      feedback: quizFeedback.value,
      correctCount: quizCorrectCount.value,
      isCorrect: quizIsCorrect.value,
      lives: quizLives.value,
      scoreGained: quizScoreGained.value,
      explanation: quizExplanation.value,
      stars: quizStars.value
    },
    memory: {
      cards: memoryCards.value,
      matchedCount: memoryMatchedCount.value,
      flipCount: memoryFlipCount.value,
      feedback: memoryFeedback.value,
      scoreGained: memoryScoreGained.value
    },
    sort: {
      scenarioIndex: scenarioIndex.value,
      scenarioChoice: scenarioChoice.value,
      scenarioScore: scenarioScore.value,
      scenarioLives: scenarioLives.value,
      scenarioFeedback: scenarioFeedback.value,
      safe: sortPlacedSafe.value,
      needAdult: sortPlacedNeedAdult.value,
      unsafe: sortPlacedUnsafe.value,
      feedback: sortFeedback.value,
      showResult: sortShowResult.value,
      allCorrect: sortAllCorrect.value,
      resultMessage: sortResultMessage.value,
      scoreGained: sortScoreGained.value,
      timer: sortTimer.value
    },
    pledge: pledgeItems.value.map(item => ({ ...item, animating: false }))
  }
}

function queueServerDraftSave(draft, immediate = false) {
  if (!userStore.isLoggedIn || !userStore.userId) return Promise.resolve()
  if (immediate) {
    clearTimeout(serverDraftSaveTimer)
    serverDraftSaveTimer = null
    return saveSafetyGameDraft(draft).catch(error => console.warn('服务端安全课堂进度保存失败', error))
  }
  if (serverDraftSaveTimer) return Promise.resolve()
  serverDraftSaveTimer = setTimeout(() => {
    serverDraftSaveTimer = null
    saveSafetyGameDraft(buildSafetyDraft()).catch(error => console.warn('服务端安全课堂进度保存失败', error))
  }, 1200)
  return Promise.resolve()
}

function saveSafetyDraft(immediate = false) {
  if (!draftReady || typeof window === 'undefined') return
  if (gamePhase.value === 'intro' || gamePhase.value === 'complete') {
    if (gamePhase.value === 'complete') void clearSafetyDraft()
    return
  }
  try {
    const draft = buildSafetyDraft()
    window.localStorage.setItem(getDraftStorageKey(), JSON.stringify(draft))
    return queueServerDraftSave(draft, immediate)
  } catch (error) {
    console.warn('安全课堂进度保存失败', error)
  }
}

async function restoreSafetyDraft() {
  if (typeof window === 'undefined') return false
  try {
    let draft = null
    if (userStore.isLoggedIn && userStore.userId) {
      try {
        draft = await getSafetyGameDraft()
      } catch (error) {
        console.warn('服务端安全课堂进度读取失败，尝试本地草稿', error)
      }
    }
    if (!draft) {
      const raw = window.localStorage.getItem(getDraftStorageKey())
      if (!raw) return false
      draft = JSON.parse(raw)
    }
    if (draft?.version !== DRAFT_VERSION || !['quiz', 'memory', 'sort', 'pledge'].includes(draft.gamePhase)) {
      await clearSafetyDraft()
      return false
    }
    gamePhase.value = draft.gamePhase
    totalScore.value = Number(draft.totalScore) || 0
    combo.value = Number(draft.combo) || 0
    maxCombo.value = Number(draft.maxCombo) || 0

    const quiz = draft.quiz || {}
    if (draft.gamePhase === 'quiz'
      && (quiz.stageRevision !== quizStageRevision.value
        || JSON.stringify(quiz.questionIds || []) !== JSON.stringify(quizQuestions.value.map(question => question.id)))) {
      await clearSafetyDraft()
      message.warning('安全题库已更新，第一关将从头开始')
      return false
    }
    quizIndex.value = Math.min(Math.max(Number(quiz.index) || 0, 0), quizQuestions.value.length - 1)
    quizSelected.value = quiz.selected ?? null
    quizRevealed.value = Boolean(quiz.revealed)
    quizFeedback.value = quiz.feedback || ''
    quizCorrectCount.value = Number(quiz.correctCount) || 0
    quizIsCorrect.value = Boolean(quiz.isCorrect)
    quizLives.value = Math.min(Math.max(Number(quiz.lives) || 0, 0), 3)
    quizScoreGained.value = Number(quiz.scoreGained) || 0
    quizExplanation.value = quiz.explanation || ''
    quizStars.value = Array.isArray(quiz.stars)
      ? quiz.stars.slice(0, quizQuestions.value.length)
      : new Array(quizQuestions.value.length).fill(null)
    while (quizStars.value.length < quizQuestions.value.length) quizStars.value.push(null)

    const memory = draft.memory || {}
    memoryCards.value = Array.isArray(memory.cards)
      ? memory.cards.map(card => ({ ...card, flipped: card.matched ? true : false }))
      : []
    memoryFlipped.value = []
    memoryMatchedCount.value = Number(memory.matchedCount) || 0
    memoryFlipCount.value = Number(memory.flipCount) || 0
    memoryFeedback.value = memory.feedback || ''
    memoryScoreGained.value = Number(memory.scoreGained) || 0
    memoryLocked = false
    if (draft.gamePhase === 'memory' && memoryCards.value.length === 0) initMemory()

    const sort = draft.sort || {}
    sortPlacedSafe.value = Array.isArray(sort.safe) ? sort.safe : []
    sortPlacedNeedAdult.value = Array.isArray(sort.needAdult) ? sort.needAdult : []
    sortPlacedUnsafe.value = Array.isArray(sort.unsafe) ? sort.unsafe : []
    sortFeedback.value = sort.feedback || ''
    sortShowResult.value = Boolean(sort.showResult)
    sortAllCorrect.value = Boolean(sort.allCorrect)
    sortResultMessage.value = sort.resultMessage || ''
    sortScoreGained.value = Number(sort.scoreGained) || 0
    sortTimer.value = Math.min(Math.max(Number(sort.timer) || 0, 0), 60)
    scenarioIndex.value = Math.min(Math.max(Number(sort.scenarioIndex) || 0, 0), scenarios.length - 1)
    scenarioChoice.value = sort.scenarioChoice === true || sort.scenarioChoice === false ? sort.scenarioChoice : null
    scenarioScore.value = Number(sort.scenarioScore) || 0
    scenarioLives.value = Math.min(Math.max(Number(sort.scenarioLives) || 0, 0), 3)
    scenarioFeedback.value = sort.scenarioFeedback || ''

    if (Array.isArray(draft.pledge) && draft.pledge.length === pledgeItems.value.length) {
      pledgeItems.value = draft.pledge.map((item, index) => ({
        ...pledgeItems.value[index],
        lit: Boolean(item.lit),
        animating: false
      }))
    }
    return true
  } catch (error) {
    console.warn('安全课堂进度恢复失败', error)
    await clearSafetyDraft()
    return false
  }
}

async function exitSafetyGame() {
  await saveSafetyDraft(true)
  router.push('/home-map')
}

watch([
  gamePhase, totalScore, combo, maxCombo,
  quizIndex, quizSelected, quizRevealed, quizFeedback, quizExplanation, quizCorrectCount, quizIsCorrect, quizLives, quizScoreGained, quizStars,
  memoryCards, memoryMatchedCount, memoryFlipCount, memoryFeedback, memoryScoreGained,
  sortPlacedSafe, sortPlacedNeedAdult, sortPlacedUnsafe, sortFeedback, sortShowResult, sortAllCorrect, sortResultMessage, sortScoreGained, sortTimer,
  pledgeItems
], saveSafetyDraft, { deep: true })

onMounted(async () => {
  await loadQuizStage()
  if (quizLoadError.value) {
    draftReady = true
    return
  }
  const restored = await restoreSafetyDraft()
  draftReady = true
  if (restored) message.success('已恢复上次的安全课堂进度')
})

async function finishGame() {
  const mainlineResult = await completeStandaloneTask('main-safety-case', [], {
    resultCode: 'SAFETY_COMPLETE',
    idempotencyKey: 'main-safety-case:lifetime'
  })
  if (!mainlineResult.success && !mainlineResult.alreadyCompleted) {
    message.error('安全守护案的完成状态暂时未确认，请重试')
    return
  }
  if (safetyPledge?.rewards?.length) {
    const rewardResult = await addMaterials(safetyPledge.rewards, 'daily-safety-quiz', {
      progressDelta: 3,
      resultCode: 'QUIZ_COMPLETE'
    })
    if (!rewardResult) {
      message.error('安全奖励暂时未到账，请重试后再完成')
      return
    }
  }
  await clearSafetyDraft(); spawnParticles('star', 20); gamePhase.value = 'complete'
}
async function resetGame() { await clearSafetyDraft(); gamePhase.value = 'intro'; totalScore.value = 0; combo.value = 0; maxCombo.value = 0; particles.value = []; clearInterval(sortTimerInterval) }
</script>

<style scoped>
.safety-game { position: relative; min-height: calc(100vh - 88px); background: linear-gradient(180deg, #faf3e0 0%, #f0e4c8 40%, #e8d5a8 100%); color: #3a2a1a; font-family: 'PingFang SC','Microsoft YaHei',sans-serif; overflow: hidden; }
.particles-layer { position: fixed; inset: 0; pointer-events: none; z-index: 3000; }
.particle { position: absolute; width: 12px; height: 12px; border-radius: 50%; animation: particleFly 1s ease-out forwards; opacity: 0; }
.particle.star { background: #f5a623; box-shadow: 0 0 8px #f5a623; clip-path: polygon(50% 0%,61% 35%,98% 35%,68% 57%,79% 91%,50% 70%,21% 91%,32% 57%,2% 35%,39% 35%); }
.particle.fail { background: #e57373; box-shadow: 0 0 6px #e57373; width: 8px; height: 8px; }
@keyframes particleFly { 0% { opacity: 1; transform: translate(0,0) scale(1); } 100% { opacity: 0; transform: translate(var(--dx),var(--dy)) scale(0); } }

.phase-intro { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 88px); padding: 32px 24px; }
.intro-card { background: rgba(255,252,242,0.95); border: 3px solid rgba(178,125,48,0.35); border-radius: 28px; padding: 44px 40px; max-width: 680px; width: 100%; text-align: center; box-shadow: 0 12px 48px rgba(94,61,18,0.12); }
.intro-bell-wrap { position: relative; display: inline-block; margin-bottom: 12px; }
.intro-bell { width: 120px; height: 120px; object-fit: contain; animation: bellBounce 2s ease-in-out infinite; position: relative; z-index: 1; }
.intro-bell-glow { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); width: 100px; height: 100px; border-radius: 50%; background: radial-gradient(circle, rgba(255,200,50,0.4) 0%, transparent 70%); animation: glowPulse 2s ease-in-out infinite; }
@keyframes bellBounce { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-12px); } }
@keyframes glowPulse { 0%,100% { opacity: 0.5; transform: translate(-50%,-50%) scale(1); } 50% { opacity: 1; transform: translate(-50%,-50%) scale(1.3); } }
.intro-eyebrow { color: #b87333; font-weight: 900; font-size: 16px; margin: 0 0 8px; letter-spacing: 4px; }
.intro-title { font-size: 42px; font-weight: 950; color: #3f2b16; margin: 0 0 16px; }
.intro-desc { font-size: 17px; color: #756246; line-height: 1.8; margin: 0 0 28px; }
.intro-stages { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 24px; flex-wrap: wrap; }
.intro-stage { display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 14px 18px; background: #fff8e8; border: 2px solid rgba(178,125,48,0.2); border-radius: 16px; min-width: 95px; }
.stage-icon { font-size: 28px; } .stage-name { font-size: 13px; font-weight: 800; color: #6b4f28; } .stage-count { font-size: 11px; color: #b87333; font-weight: 700; }
.intro-stage-arrow { font-size: 22px; color: #b87333; font-weight: 900; }
.intro-rules { display: flex; justify-content: center; gap: 10px; margin-bottom: 28px; flex-wrap: wrap; }
.rule-chip { display: flex; align-items: center; gap: 6px; padding: 8px 14px; background: #fef9e7; border: 1px solid rgba(178,125,48,0.2); border-radius: 999px; font-size: 13px; font-weight: 700; color: #6b4f28; }
.rule-icon { font-size: 16px; }
.intro-btn { display: inline-flex; align-items: center; gap: 10px; padding: 16px 52px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; font-size: 20px; font-weight: 900; cursor: pointer; box-shadow: 0 6px 24px rgba(200,120,30,0.35); transition: all 0.25s; }
.intro-btn:hover { transform: translateY(-3px); box-shadow: 0 10px 32px rgba(200,120,30,0.45); }
.btn-icon { font-size: 24px; }

.phase-back { padding: 8px 16px; border: 1px solid rgba(178,125,48,0.3); border-radius: 999px; background: rgba(255,250,232,0.8); color: #6b4f28; cursor: pointer; font-size: 14px; font-weight: 700; transition: all 0.2s; white-space: nowrap; }
.phase-back:hover { background: rgba(255,245,215,0.95); }
.phase-badge { display: flex; align-items: center; gap: 8px; padding: 10px 20px; background: rgba(255,250,232,0.9); border: 2px solid rgba(178,125,48,0.25); border-radius: 999px; font-weight: 900; color: #5a3514; font-size: 16px; }
.badge-icon { font-size: 22px; }
.topbar-center { display: flex; align-items: center; gap: 16px; flex: 1; flex-wrap: wrap; }
.topbar-stats { display: flex; align-items: center; gap: 14px; flex-shrink: 0; }
.stat-lives { display: flex; gap: 2px; } .life-heart { font-size: 20px; transition: all 0.3s; } .life-heart.lost { opacity: 0.25; filter: grayscale(1); }
.stat-score { display: flex; align-items: center; gap: 4px; padding: 6px 14px; background: linear-gradient(135deg,#fff8d6,#fff3c4); border: 2px solid #f5a623; border-radius: 999px; font-weight: 900; }
.score-icon { font-size: 18px; } .score-num { color: #3f2b16; font-size: 18px; }
.stat-combo { padding: 6px 12px; background: linear-gradient(135deg,#ff6b35,#e53935); color: #fff; border-radius: 999px; font-weight: 900; font-size: 14px; animation: comboPulse 0.5s ease infinite; }
@keyframes comboPulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.08); } }
.stat-timer { display: flex; align-items: center; gap: 4px; padding: 6px 14px; background: #fff; border: 2px solid #2f8068; border-radius: 999px; font-weight: 900; color: #2f8068; }
.stat-timer.urgent { border-color: #e53935; color: #e53935; animation: timerUrgent 0.5s ease infinite; }
@keyframes timerUrgent { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } }
.timer-icon { font-size: 18px; } .timer-num { font-size: 20px; min-width: 24px; text-align: center; }
.stat-memory-info { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: #fff; border: 2px solid rgba(178,125,48,0.25); border-radius: 999px; font-weight: 800; font-size: 14px; color: #6b4f28; }

.phase-quiz { padding: 20px clamp(16px,4vw,56px) 44px; }
.quiz-topbar { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
.quiz-progress-wrap { display: flex; align-items: center; gap: 10px; min-width: 160px; }
.quiz-progress-bar { flex: 1; height: 10px; background: rgba(178,125,48,0.15); border-radius: 999px; overflow: hidden; }
.quiz-progress-fill { height: 100%; background: linear-gradient(90deg,#e8a840,#d4892a); border-radius: 999px; transition: width 0.4s ease; }
.quiz-progress-text { font-weight: 900; color: #6b4f28; font-size: 14px; white-space: nowrap; }
.quiz-card-wrap { max-width: 720px; margin: 0 auto; text-align: center; }
.quiz-bell-character { position: relative; display: inline-block; margin-bottom: 16px; }
.quiz-bell-character img { width: 80px; height: 80px; object-fit: contain; }
.quiz-bell-speech { position: absolute; top: -24px; left: 50%; transform: translateX(-50%); white-space: nowrap; padding: 8px 16px; background: #fff; border: 2px solid #e8a840; border-radius: 16px; font-size: 14px; font-weight: 800; color: #5a3514; box-shadow: 0 4px 16px rgba(0,0,0,0.08); animation: speechPop 0.4s ease; }
@keyframes speechPop { 0% { opacity: 0; transform: translateX(-50%) scale(0.8); } 100% { opacity: 1; transform: translateX(-50%) scale(1); } }
.quiz-question-card { background: rgba(255,252,242,0.95); border: 3px solid rgba(178,125,48,0.3); border-radius: 24px; padding: 32px 28px; min-height: 300px; box-shadow: 0 8px 32px rgba(94,61,18,0.08); transition: all 0.3s; }
.quiz-question-card.quiz-correct { border-color: #2f8068; box-shadow: 0 0 32px rgba(47,128,104,0.15); }
.quiz-question-card.quiz-wrong { border-color: #e57373; box-shadow: 0 0 32px rgba(229,115,115,0.15); }
.quiz-type-tag { display: inline-block; padding: 4px 14px; background: #fff8e8; border: 1px solid rgba(178,125,48,0.25); border-radius: 999px; font-size: 13px; font-weight: 800; color: #b87333; margin-bottom: 16px; }
.quiz-q-text { font-size: 24px; font-weight: 900; color: #3f2b16; margin: 0 0 24px; line-height: 1.6; }
.quiz-options-row { display: flex; gap: 16px; justify-content: center; margin-bottom: 24px; flex-wrap: wrap; }
.quiz-options-col { display: flex; flex-direction: column; gap: 10px; margin-bottom: 24px; align-items: stretch; }
.quiz-opt-btn { padding: 14px 36px; border: 3px solid rgba(178,125,48,0.25); border-radius: 16px; background: #fffdf5; color: #3f2b16; font-size: 18px; font-weight: 800; cursor: pointer; transition: all 0.25s; min-width: 120px; }
.quiz-opt-btn.scenario-opt { display: flex; align-items: center; gap: 14px; padding: 14px 20px; text-align: left; min-width: auto; width: 100%; }
.opt-letter { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: 50%; background: #e8a840; color: #fff; font-weight: 900; font-size: 16px; flex-shrink: 0; }
.opt-text { flex: 1; font-size: 15px; line-height: 1.4; }
.quiz-opt-btn:hover { border-color: #e8a840; background: #fff8e8; transform: translateY(-2px); }
.quiz-opt-btn.selected { border-color: #2f8068; background: #e8f5e9; color: #2f8068; box-shadow: 0 0 0 3px rgba(47,128,104,0.15); }
.quiz-opt-btn.selected .opt-letter { background: #2f8068; }
.quiz-submit-btn { padding: 14px 40px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#2f8068,#246b56); color: #fff; font-size: 17px; font-weight: 900; cursor: pointer; transition: all 0.25s; }
.quiz-submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.quiz-submit-btn:not(:disabled):hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(47,128,104,0.3); }
.quiz-result-icon { font-size: 56px; margin-bottom: 8px; }
.quiz-result-title { font-size: 26px; font-weight: 900; margin: 0 0 14px; color: #3f2b16; }
.quiz-explanation { font-size: 15px; color: #756246; line-height: 1.7; margin: 0 0 20px; padding: 14px 18px; background: #fff8e8; border-radius: 12px; }
.quiz-result-score { font-size: 20px; font-weight: 900; color: #2f8068; margin-bottom: 20px; }
.combo-bonus { font-size: 14px; color: #e53935; font-weight: 800; }
.quiz-next-btn { padding: 14px 40px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; font-size: 17px; font-weight: 900; cursor: pointer; transition: all 0.25s; }
.quiz-next-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(200,120,30,0.35); }
.quiz-stars { display: flex; justify-content: center; gap: 6px; margin-top: 20px; flex-wrap: wrap; }
.quiz-star { font-size: 24px; transition: all 0.3s; }
.quiz-star.earned { animation: starPop 0.4s ease; }
.quiz-star.wrong { opacity: 0.5; }
.quiz-star.current { animation: starPulse 0.8s ease-in-out infinite; }
@keyframes starPop { 0% { transform: scale(0) rotate(-30deg); } 100% { transform: scale(1) rotate(0deg); } }
@keyframes starPulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.3); } }
.quiz-game-over { position: fixed; inset: 0; z-index: 2000; display: flex; justify-content: center; align-items: center; background: rgba(0,0,0,0.55); padding: 24px; }
.game-over-card { background: #fffdf5; border: 3px solid #e57373; border-radius: 24px; padding: 40px 32px; max-width: 400px; width: 100%; text-align: center; }
.game-over-icon { font-size: 56px; margin-bottom: 12px; }
.game-over-card h2 { font-size: 24px; font-weight: 900; color: #c62828; margin: 0 0 12px; }
.game-over-card p { color: #756246; line-height: 1.7; margin: 0 0 24px; }
.game-over-retry { padding: 14px 36px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; font-size: 17px; font-weight: 900; cursor: pointer; }

.phase-memory { padding: 20px clamp(16px,4vw,56px) 44px; }
.memory-topbar { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.memory-play-area { max-width: 960px; margin: 0 auto; }
.memory-bell-guide { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; justify-content: center; }
.memory-bell-guide img { width: 55px; height: 55px; object-fit: contain; }
.memory-bell-msg { padding: 10px 20px; background: #fff; border: 2px solid #e8a840; border-radius: 16px; font-weight: 800; color: #5a3514; font-size: 15px; transition: all 0.3s; }
.memory-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; max-width: 720px; margin: 0 auto; }
.memory-card { perspective: 800px; cursor: pointer; aspect-ratio: 3 / 4; }
.memory-card-inner { position: relative; width: 100%; height: 100%; transition: transform 0.5s; transform-style: preserve-3d; }
.memory-card.flipped .memory-card-inner { transform: rotateY(180deg); }
.memory-card.matched .memory-card-inner { transform: rotateY(180deg); }
.memory-card-front, .memory-card-back { position: absolute; inset: 0; backface-visibility: hidden; border-radius: 16px; display: flex; align-items: center; justify-content: center; }
.memory-card-front { background: linear-gradient(135deg,#e8a840,#d4892a); border: 3px solid #c07a20; box-shadow: 0 4px 12px rgba(178,125,48,0.2); }
.memory-card-question { font-size: 36px; color: #fff; font-weight: 900; }
.memory-card-back { transform: rotateY(180deg); flex-direction: column; gap: 6px; padding: 12px; text-align: center; }
.card-left { background: linear-gradient(135deg,#fff8e1,#fff3c4); border: 3px solid #f5a623; }
.card-right { background: linear-gradient(135deg,#e8f5e9,#c8e6c9); border: 3px solid #2f8068; }
.card-icon { font-size: 28px; }
.card-text { font-size: 14px; font-weight: 800; color: #3f2b16; line-height: 1.4; }
.memory-card.matched { opacity: 0.7; }
.memory-card.matched .card-left { border-color: #f5a623; background: #fff8d6; }
.memory-card.matched .card-right { border-color: #2f8068; background: #e8f5e9; }
.memory-complete { text-align: center; margin-top: 28px; animation: ceremonyFadeIn 0.6s ease; }
.memory-complete-card { background: rgba(255,252,242,0.95); border: 3px solid #f5a623; border-radius: 24px; padding: 32px 28px; box-shadow: 0 8px 32px rgba(245,166,35,0.15); }
.memory-complete-icon { font-size: 56px; margin-bottom: 8px; }
.memory-complete-card h2 { font-size: 26px; font-weight: 950; color: #3f2b16; margin: 0 0 8px; }
.memory-complete-desc { color: #756246; font-size: 15px; margin: 0 0 20px; }
.memory-tips { text-align: left; max-width: 500px; margin: 0 auto 24px; }
.memory-tips-title { font-weight: 900; color: #5a3514; font-size: 15px; margin: 0 0 10px; }
.memory-tip-item { display: flex; align-items: center; gap: 8px; padding: 8px 14px; background: #fff8e8; border-radius: 10px; margin-bottom: 6px; font-size: 14px; }
.tip-left { font-weight: 800; color: #b87333; flex-shrink: 0; }
.tip-arrow { color: #e8a840; font-weight: 900; }
.tip-right { color: #2f8068; font-weight: 800; }
.memory-next-btn { padding: 14px 40px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; font-size: 17px; font-weight: 900; cursor: pointer; transition: all 0.25s; }
.memory-next-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(200,120,30,0.35); }

.phase-sort { padding: 20px clamp(16px,4vw,56px) 44px; }
.sort-topbar { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.sort-play-area { max-width: 960px; margin: 0 auto; }
.sort-bell-guide { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; justify-content: center; }
.sort-bell-guide img { width: 55px; height: 55px; object-fit: contain; }
.sort-bell-msg { padding: 10px 20px; background: #fff; border: 2px solid #e8a840; border-radius: 16px; font-weight: 800; color: #5a3514; font-size: 15px; transition: all 0.3s; }
.sort-baskets { display: grid; gap: 16px; margin-bottom: 20px; }
.sort-baskets.three-baskets { grid-template-columns: 1fr 1fr 1fr; }
.sort-basket { min-height: 160px; padding: 16px; border: 3px dashed rgba(178,125,48,0.3); border-radius: 20px; transition: all 0.3s; }
.safe-basket { background: rgba(232,245,233,0.65); border-color: rgba(47,128,104,0.3); }
.adult-basket { background: rgba(255,248,225,0.65); border-color: rgba(245,166,35,0.3); }
.unsafe-basket { background: rgba(255,235,238,0.65); border-color: rgba(211,47,47,0.3); }
.basket-hover { transform: scale(1.02); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.safe-basket.basket-hover { border-color: #2f8068; background: rgba(232,245,233,0.9); }
.adult-basket.basket-hover { border-color: #f5a623; background: rgba(255,248,225,0.9); }
.unsafe-basket.basket-hover { border-color: #d32f2f; background: rgba(255,235,238,0.9); }
.basket-label { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 900; margin-bottom: 12px; }
.basket-icon { font-size: 22px; } .safe-basket .basket-label { color: #2f8068; } .adult-basket .basket-label { color: #b87333; } .unsafe-basket .basket-label { color: #c62828; }
.basket-count { margin-left: auto; padding: 2px 10px; border-radius: 999px; font-size: 13px; background: rgba(0,0,0,0.06); }
.basket-items { display: flex; flex-wrap: wrap; gap: 6px; }
.basket-item { padding: 6px 14px; border-radius: 999px; font-weight: 800; font-size: 13px; animation: itemPop 0.3s ease; }
@keyframes itemPop { 0% { transform: scale(0); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
.safe-item { background: #c8e6c9; color: #2e7d32; } .adult-item { background: #fff3c4; color: #f57f17; } .unsafe-item { background: #ffcdd2; color: #c62828; }
.basket-placeholder { text-align: center; color: #9e8b70; font-size: 13px; padding: 20px 0; font-weight: 700; }
.sort-cards-pool { margin-top: 16px; }
.sort-pool-label { font-weight: 900; color: #6b4f28; font-size: 14px; margin-bottom: 10px; text-align: center; }
.sort-pool-items { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.sort-card { padding: 10px 18px; background: #fffdf5; border: 2px solid rgba(178,125,48,0.3); border-radius: 12px; cursor: grab; font-weight: 800; font-size: 14px; color: #3f2b16; transition: all 0.2s; user-select: none; }
.sort-card:hover { border-color: #e8a840; background: #fff8e8; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(178,125,48,0.15); }
.sort-card:active { cursor: grabbing; }
.card-dragging { opacity: 0.4; }
.sort-empty-hint { width: 100%; text-align: center; color: #2f8068; font-weight: 800; font-size: 15px; padding: 12px; }
.sort-actions { text-align: center; margin-top: 20px; }
.sort-check-btn { padding: 14px 40px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#2f8068,#246b56); color: #fff; font-size: 17px; font-weight: 900; cursor: pointer; transition: all 0.25s; }
.sort-check-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(47,128,104,0.3); }
.sort-result-modal { position: fixed; inset: 0; z-index: 2000; display: flex; justify-content: center; align-items: center; background: rgba(0,0,0,0.55); padding: 24px; }
.sort-result-card { background: #fffdf5; border: 3px solid #f5a623; border-radius: 24px; padding: 36px 28px; max-width: 480px; width: 100%; text-align: center; }
.sort-result-icon { font-size: 56px; margin-bottom: 8px; }
.sort-result-card h2 { font-size: 24px; font-weight: 950; color: #3f2b16; margin: 0 0 12px; }
.sort-result-desc { color: #756246; line-height: 1.7; margin: 0 0 16px; white-space: pre-line; font-size: 14px; }
.sort-result-score { font-size: 20px; font-weight: 900; color: #2f8068; margin-bottom: 20px; }
.time-bonus { font-size: 14px; color: #b87333; font-weight: 800; }
.sort-result-btns { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
.sort-retry-btn { padding: 14px 32px; border: 2px solid #e8a840; border-radius: 999px; background: #fff; color: #b87333; font-weight: 900; font-size: 16px; cursor: pointer; }
.sort-next-btn { padding: 14px 40px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; font-size: 17px; font-weight: 900; cursor: pointer; }

.phase-pledge { padding: 20px clamp(16px,4vw,56px) 44px; }
.pledge-scene { max-width: 720px; margin: 0 auto; }
.pledge-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
.pledge-stage { text-align: center; }
.pledge-bell-large { position: relative; display: inline-block; margin-bottom: 16px; }
.pledge-bell-large img { width: 90px; height: 90px; object-fit: contain; }
.pledge-rays { position: absolute; inset: -20px; border-radius: 50%; background: conic-gradient(from 0deg,transparent,rgba(255,200,50,0.3),transparent,rgba(255,200,50,0.3),transparent); animation: raySpin 3s linear infinite; }
@keyframes raySpin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.pledge-title-area { margin-bottom: 28px; }
.pledge-title { font-size: 32px; font-weight: 950; color: #3f2b16; margin: 0 0 8px; }
.pledge-subtitle { font-size: 15px; color: #b87333; font-weight: 700; margin: 0; }
.pledge-items { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
.pledge-item { display: flex; align-items: center; gap: 14px; padding: 16px 22px; background: rgba(255,252,242,0.9); border: 2px solid rgba(178,125,48,0.2); border-radius: 18px; cursor: pointer; transition: all 0.35s; text-align: left; }
.pledge-item:hover:not(.lit) { border-color: #e8a840; transform: translateX(4px); box-shadow: 0 4px 16px rgba(178,125,48,0.12); }
.pledge-item.lit { border-color: #f5a623; background: linear-gradient(135deg,#fffdf0,#fff8d6); box-shadow: 0 0 24px rgba(245,166,35,0.2); }
.pledge-item.animating { animation: pledgeLightUp 0.4s ease; }
@keyframes pledgeLightUp { 0% { transform: scale(1); } 50% { transform: scale(1.03); background: #fffbe6; } 100% { transform: scale(1); } }
.pledge-lamp { flex-shrink: 0; } .lamp-icon { font-size: 32px; transition: all 0.3s; }
.pledge-item.lit .lamp-icon { animation: bellRing 0.5s ease; }
@keyframes bellRing { 0% { transform: rotate(0); } 25% { transform: rotate(-20deg); } 50% { transform: rotate(20deg); } 75% { transform: rotate(-10deg); } 100% { transform: rotate(0); } }
.pledge-text { flex: 1; } .pledge-item-title { font-size: 17px; font-weight: 900; color: #3f2b16; margin: 0 0 4px; }
.pledge-item-desc { font-size: 13px; color: #756246; margin: 0; line-height: 1.5; }
.pledge-check { font-size: 22px; flex-shrink: 0; }
.pledge-progress-wrap { display: flex; align-items: center; gap: 12px; margin-bottom: 28px; font-weight: 800; color: #6b4f28; font-size: 14px; }
.pledge-progress-bar { flex: 1; height: 8px; background: rgba(178,125,48,0.15); border-radius: 999px; overflow: hidden; }
.pledge-progress-fill { height: 100%; background: linear-gradient(90deg,#f5a623,#e8a840); border-radius: 999px; transition: width 0.5s ease; }
.pledge-ceremony { animation: ceremonyFadeIn 0.6s ease; }
@keyframes ceremonyFadeIn { 0% { opacity: 0; transform: translateY(20px); } 100% { opacity: 1; transform: translateY(0); } }
.ceremony-card { background: linear-gradient(135deg,#fffdf0,#fff8d6); border: 3px solid #f5a623; border-radius: 24px; padding: 36px 28px; text-align: center; box-shadow: 0 12px 48px rgba(245,166,35,0.2); }
.ceremony-shield { font-size: 64px; margin-bottom: 8px; animation: shieldBounce 1s ease infinite; }
@keyframes shieldBounce { 0%,100% { transform: scale(1); } 50% { transform: scale(1.1); } }
.ceremony-title { font-size: 28px; font-weight: 950; color: #3f2b16; margin: 0 0 16px; }
.ceremony-final-score { display: inline-flex; flex-direction: column; align-items: center; padding: 14px 32px; background: linear-gradient(135deg,#fff8d6,#fff3c4); border: 2px solid #f5a623; border-radius: 20px; margin-bottom: 20px; }
.final-score-label { font-size: 13px; color: #b87333; font-weight: 800; }
.final-score-num { font-size: 36px; font-weight: 950; color: #3f2b16; }
.ceremony-text { font-size: 15px; color: #756246; line-height: 1.8; margin: 0 0 20px; }
.ceremony-badge { display: inline-flex; align-items: center; gap: 8px; padding: 12px 24px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; border-radius: 999px; font-weight: 900; font-size: 16px; margin-bottom: 16px; }
.ceremony-badge-icon { font-size: 22px; }
.ceremony-achievements { margin-bottom: 16px; }
.ach-title { font-weight: 900; color: #5a3514; font-size: 15px; margin: 0 0 10px; }
.ach-list { display: flex; justify-content: center; gap: 8px; flex-wrap: wrap; }
.ach-chip { display: flex; align-items: center; gap: 6px; padding: 8px 16px; background: linear-gradient(135deg,#fff8d6,#fff3c4); border: 2px solid #f5a623; border-radius: 999px; font-weight: 800; font-size: 14px; color: #5a3514; animation: achPop 0.4s ease; }
@keyframes achPop { 0% { transform: scale(0); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
.ach-icon { font-size: 20px; }
.ceremony-rewards { display: flex; justify-content: center; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
.reward-item { display: flex; align-items: center; gap: 8px; padding: 8px 18px; background: #fff; border: 2px solid rgba(178,125,48,0.25); border-radius: 999px; font-weight: 800; color: #5a3514; font-size: 14px; }
.reward-icon { width: 28px; height: 28px; object-fit: contain; border-radius: 50%; }
.ceremony-btn { padding: 16px 48px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#2f8068,#246b56); color: #fff; font-size: 18px; font-weight: 900; cursor: pointer; box-shadow: 0 6px 24px rgba(47,128,104,0.3); transition: all 0.25s; }
.ceremony-btn:hover { transform: translateY(-3px); box-shadow: 0 10px 32px rgba(47,128,104,0.4); }

.phase-complete { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 88px); padding: 32px 24px; }
.complete-card { position: relative; background: rgba(255,252,242,0.95); border: 3px solid rgba(178,125,48,0.35); border-radius: 28px; padding: 48px 40px; max-width: 560px; width: 100%; text-align: center; box-shadow: 0 12px 48px rgba(94,61,18,0.12); overflow: hidden; }
.complete-cert-ribbon { position: absolute; top: 20px; right: -32px; padding: 6px 40px; background: linear-gradient(135deg,#e8a840,#d4892a); color: #fff; font-weight: 900; font-size: 14px; transform: rotate(45deg); box-shadow: 0 2px 8px rgba(0,0,0,0.15); letter-spacing: 2px; }
.complete-stars { display: flex; justify-content: center; gap: 8px; margin-bottom: 16px; }
.complete-star { font-size: 48px; animation: starPop 0.5s ease forwards; opacity: 0; }
.complete-star:nth-child(1) { animation-delay: 0.1s; }
.complete-star:nth-child(2) { animation-delay: 0.3s; }
.complete-star:nth-child(3) { animation-delay: 0.5s; }
.complete-title { font-size: 34px; font-weight: 950; color: #3f2b16; margin: 0 0 16px; }
.complete-desc { font-size: 16px; color: #756246; line-height: 1.8; margin: 0 0 28px; }
.complete-stats { display: flex; justify-content: center; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 14px 18px; background: #fff8e8; border: 2px solid rgba(178,125,48,0.2); border-radius: 16px; min-width: 82px; }
.stat-item.highlight { background: linear-gradient(135deg,#fff8d6,#fff3c4); border-color: #f5a623; }
.stat-num { font-size: 24px; font-weight: 950; color: #3f2b16; }
.stat-label { font-size: 12px; color: #b87333; font-weight: 800; }
.complete-achievements { margin-bottom: 24px; }
.complete-ach-title { font-weight: 900; color: #5a3514; font-size: 16px; margin: 0 0 12px; }
.complete-ach-list { display: flex; flex-direction: column; gap: 8px; align-items: center; }
.complete-ach-item { display: flex; align-items: center; gap: 12px; padding: 10px 18px; background: #fff8e8; border: 2px solid rgba(245,166,35,0.3); border-radius: 14px; width: 100%; max-width: 320px; animation: achPop 0.4s ease; }
.complete-ach-icon { font-size: 28px; flex-shrink: 0; }
.complete-ach-info { display: flex; flex-direction: column; text-align: left; }
.complete-ach-name { font-weight: 900; color: #3f2b16; font-size: 15px; }
.complete-ach-desc { font-size: 12px; color: #b87333; }
.complete-btns { display: flex; gap: 14px; justify-content: center; flex-wrap: wrap; }
.complete-replay-btn { padding: 14px 32px; border: 2px solid #e8a840; border-radius: 999px; background: #fff; color: #b87333; font-weight: 900; font-size: 16px; cursor: pointer; transition: all 0.2s; }
.complete-replay-btn:hover { background: #fff8e8; }
.complete-home-btn { padding: 14px 32px; border: 0; border-radius: 999px; background: linear-gradient(135deg,#2f8068,#246b56); color: #fff; font-weight: 900; font-size: 16px; cursor: pointer; transition: all 0.2s; }
.complete-home-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(47,128,104,0.3); }

/* Reference-led safety classroom polish */
.safety-game {
  --ink: #4a2c16;
  --muted: #846947;
  --bronze: #b66a1f;
  --gold: #f2a51b;
  --green: #266f59;
  --green-deep: #175443;
  --paper: rgba(255, 250, 232, 0.97);
  min-height: calc(100vh - 56px);
  background:
    linear-gradient(90deg, rgba(255, 247, 220, 0.16), rgba(255, 247, 220, 0.42) 50%, rgba(255, 247, 220, 0.12)),
    var(--safety-background) center / cover fixed;
  color: var(--ink);
  overflow-x: hidden;
}
.safety-game::before {
  content: '';
  position: fixed;
  inset: 56px 0 0;
  background: linear-gradient(180deg, rgba(255, 248, 224, 0.1), rgba(118, 73, 26, 0.08));
  pointer-events: none;
}
.phase-intro,
.phase-quiz,
.phase-memory,
.phase-sort,
.phase-pledge,
.phase-complete { position: relative; z-index: 1; }
.phase-intro {
  min-height: calc(100vh - 56px);
  padding: clamp(70px, 8vh, 112px) clamp(24px, 4vw, 72px) 30px;
  align-items: center;
}
.intro-shell {
  display: grid;
  grid-template-columns: minmax(760px, 1fr) 270px;
  align-items: center;
  gap: 24px;
  width: min(1380px, 100%);
  margin: 0 auto;
}
.intro-card {
  position: relative;
  width: 100%;
  max-width: none;
  padding: 96px clamp(34px, 4vw, 68px) 26px;
  border: 4px solid rgba(42, 112, 86, 0.82);
  border-radius: 42px 42px 34px 34px;
  background:
    linear-gradient(135deg, rgba(255,255,255,.35), transparent 32%),
    radial-gradient(circle at 50% 25%, #fffdf4, #fff6dc 72%, #f6e4bb);
  box-shadow:
    0 0 0 9px rgba(244, 204, 128, 0.85),
    0 18px 48px rgba(84, 51, 14, 0.2),
    inset 0 0 42px rgba(205, 154, 69, 0.12);
  overflow: visible;
}
.intro-card::after,
.quiz-question-card::after {
  content: '';
  position: absolute;
  inset: 13px;
  border: 1px solid rgba(188, 128, 43, 0.2);
  border-radius: inherit;
  pointer-events: none;
}
.intro-guardian {
  position: absolute;
  top: -92px;
  left: 50%;
  width: clamp(160px, 13vw, 210px);
  transform: translateX(-50%);
  filter: drop-shadow(0 12px 16px rgba(127, 72, 15, 0.24));
  z-index: 2;
}
.intro-eyebrow {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 9px;
  margin: 0 0 5px;
  color: var(--green);
  font-size: 18px;
  letter-spacing: 3px;
}
.intro-title {
  margin: 0;
  color: var(--green-deep);
  font-size: clamp(44px, 4.4vw, 68px);
  line-height: 1.08;
  letter-spacing: 5px;
  text-shadow: 0 3px #fff, 0 7px 16px rgba(65, 79, 34, 0.13);
}
.intro-title span { color: var(--green); }
.intro-desc {
  margin: 14px auto 22px;
  color: #6f5637;
  font-size: clamp(16px, 1.25vw, 20px);
  line-height: 1.7;
}
.intro-desc strong { color: #dd6f19; }
.intro-stages {
  display: grid;
  grid-template-columns: 1fr 34px 1fr 34px 1fr 34px 1fr;
  align-items: center;
  gap: 7px;
  margin: 18px auto 22px;
}
.intro-stage {
  position: relative;
  min-width: 0;
  height: 154px;
  padding: 24px 12px 12px;
  border: 2px solid #e8bd77;
  border-radius: 18px;
  background: rgba(255, 251, 239, 0.92);
  box-shadow: 0 10px 20px rgba(110, 69, 20, 0.12);
}
.stage-tone-1 { border-color: #ec9ab4; background: linear-gradient(#fff9f4, #fff0f2); }
.stage-tone-2 { border-color: #99bcde; background: linear-gradient(#fffdf3, #eef6fc); }
.stage-tone-3 { border-color: #a8c968; background: linear-gradient(#fffdf0, #f2f8dc); }
.stage-tone-4 { border-color: #efa956; background: linear-gradient(#fffdf0, #fff2dc); }
.stage-number {
  position: absolute;
  top: -12px;
  left: -10px;
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 50%;
  background: var(--green);
  color: #fff;
  font-weight: 900;
  box-shadow: 0 3px 8px rgba(62, 57, 24, 0.25);
}
.stage-tone-1 .stage-number { background: #df4c7d; }
.stage-tone-2 .stage-number { background: #4a8ac5; }
.stage-tone-3 .stage-number { background: #65a423; }
.stage-tone-4 .stage-number { background: #e7831d; }
.stage-icon {
  display: grid;
  width: 58px;
  height: 58px;
  margin: 0 auto 9px;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(145deg, #fffef7, #f5d991);
  color: var(--green);
  font-size: 31px;
  box-shadow: inset 0 0 0 2px rgba(200, 137, 38, 0.18), 0 5px 12px rgba(122, 75, 17, 0.12);
}
.stage-name { font-size: 18px; color: var(--ink); }
.stage-count { margin-top: 3px; font-size: 14px; color: #86521d; }
.intro-stage-arrow {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 50%;
  background: var(--green);
  color: #ffc341;
  font-size: 27px;
  font-weight: 900;
  box-shadow: 0 4px 10px rgba(38, 111, 89, 0.22);
}
.intro-rules {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin: 0 0 20px;
}
.rule-chip {
  min-height: 44px;
  padding: 8px 12px;
  border: 1px solid rgba(190, 128, 38, 0.3);
  background: rgba(255, 252, 240, 0.9);
  color: #604526;
  font-size: 13px;
  white-space: normal;
}
.rule-chip > svg { color: #dc7d17; font-size: 18px; }
.intro-btn,
.quiz-submit-btn,
.quiz-next-btn,
.memory-next-btn,
.sort-check-btn,
.sort-next-btn,
.ceremony-btn,
.complete-home-btn {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  border: 1px solid #d77918;
  background: linear-gradient(180deg, #ffb632, #e98718);
  box-shadow: inset 0 2px rgba(255,255,255,.35), 0 7px 0 #bb5b12, 0 13px 22px rgba(142, 78, 17, .22);
}
.intro-btn {
  min-width: 390px;
  min-height: 68px;
  padding: 0 40px;
  font-size: 31px;
  letter-spacing: 3px;
}
.intro-btn:hover,
.quiz-next-btn:hover,
.memory-next-btn:hover,
.sort-check-btn:hover,
.sort-next-btn:hover,
.ceremony-btn:hover,
.complete-home-btn:hover { transform: translateY(-2px); }
.intro-safety-note {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 7px;
  margin: 18px 0 0;
  color: #765b3a;
  font-size: 14px;
  font-weight: 800;
}
.intro-safety-note svg { color: var(--green); }
.intro-side-panel { display: flex; flex-direction: column; align-items: center; gap: 18px; }
.side-progress-card {
  width: 100%;
  padding: 22px 20px;
  border: 3px solid #d4a25e;
  border-radius: 22px;
  background: linear-gradient(145deg, rgba(71, 49, 27, .94), rgba(98, 69, 37, .9));
  color: #fff8df;
  box-shadow: 0 12px 30px rgba(65, 42, 16, .25), inset 0 0 0 4px rgba(255, 222, 157, .12);
}
.side-card-heading { display: flex; justify-content: space-between; align-items: center; font-size: 17px; font-weight: 900; }
.side-card-heading small { color: #ffd36b; font-size: 20px; }
.side-progress-track { height: 11px; margin: 15px 0 22px; border-radius: 999px; background: rgba(25, 21, 15, .65); overflow: hidden; }
.side-progress-track span { display: block; height: 100%; border-radius: inherit; background: linear-gradient(90deg, #f2a51b, #ffd458); transition: width .35s ease; }
.side-badges-title { padding-top: 17px; border-top: 1px solid rgba(255,255,255,.14); font-weight: 900; }
.side-badges { display: flex; justify-content: space-around; align-items: center; margin-top: 14px; }
.side-badges img,
.locked-badge { width: 52px; height: 52px; object-fit: contain; }
.locked-badge { display: grid; place-items: center; border: 2px dashed rgba(255,255,255,.3); border-radius: 50%; color: rgba(255,255,255,.35); font-size: 23px; }
.intro-side-character { width: min(260px, 100%); filter: drop-shadow(0 15px 18px rgba(74, 43, 13, .28)); }

.phase-quiz,
.phase-memory,
.phase-sort,
.phase-pledge { min-height: calc(100vh - 56px); padding: 22px clamp(24px, 3vw, 54px) 32px; }
.quiz-topbar,
.memory-topbar,
.sort-topbar,
.pledge-header {
  width: min(1540px, 100%);
  min-height: 70px;
  margin: 0 auto;
  padding: 8px 12px;
  border: 1px solid rgba(196, 138, 53, .25);
  border-radius: 18px;
  background: rgba(255, 248, 225, .82);
  box-shadow: 0 8px 20px rgba(93, 57, 17, .08);
  backdrop-filter: blur(8px);
}
.phase-back,
.phase-badge,
.stat-score,
.stat-memory-info,
.stat-timer,
.stat-combo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.phase-back { min-height: 44px; padding: 0 20px; font-size: 15px; font-weight: 900; }
.phase-badge { border-color: rgba(184, 115, 51, .28); color: var(--ink); font-size: 17px; }
.phase-badge > svg { color: var(--green); font-size: 20px; }
.quiz-progress-bar { height: 10px; background: rgba(178, 132, 66, .22); }
.quiz-progress-fill { background: linear-gradient(90deg, var(--green), #71ac76); box-shadow: 0 0 10px rgba(47, 128, 104, .25); }
.stat-lives { display: flex; gap: 7px; }
.life-heart { color: #ee3f67; font-size: 25px; filter: drop-shadow(0 2px 2px rgba(137, 24, 57, .2)); }
.life-heart.lost { color: #dbcbb5; filter: none; }
.stat-score { min-width: 92px; justify-content: center; color: #b46b16; font-size: 20px; }
.stat-score > svg { color: #f2a51b; }
.quiz-card-wrap { position: relative; width: min(1040px, calc(100% - 270px)); max-width: none; margin: 112px auto 0; overflow: visible; }
.quiz-bell-character { position: absolute; top: -120px; left: 50%; margin: 0; transform: translateX(-50%); z-index: 4; }
.quiz-bell-character img { width: 150px; height: 150px; object-fit: contain; filter: drop-shadow(0 8px 12px rgba(74, 43, 13, .24)); }
.quiz-bell-speech { top: 12px; left: calc(50% + 58px); max-width: 230px; border: 2px solid rgba(42, 112, 86, .3); background: #fffaf0; color: var(--green-deep); }
.quiz-question-card {
  min-height: 430px;
  padding: 54px clamp(38px, 6vw, 92px) 38px;
  border: 4px solid var(--green);
  border-radius: 40px;
  background: radial-gradient(circle at 50% 20%, #fffdf5, #fff5dc 85%);
  box-shadow: 0 0 0 9px rgba(245, 204, 128, .88), 0 20px 44px rgba(86, 50, 14, .2), inset 0 0 38px rgba(196, 137, 45, .08);
  overflow: visible;
}
.quiz-question-front,
.quiz-question-back { min-height: 330px; display: flex; flex-direction: column; justify-content: center; align-items: center; }
.quiz-type-tag { display: inline-flex; align-items: center; gap: 8px; padding: 9px 22px; border: 1px solid rgba(190, 128, 38, .36); color: var(--ink); font-size: 16px; }
.quiz-type-tag::before,
.quiz-type-tag::after { content: ''; width: 80px; height: 1px; background: rgba(211, 151, 62, .45); }
.quiz-q-text { max-width: 880px; margin: 38px auto; color: #3b220f; font-size: clamp(28px, 2.4vw, 40px); line-height: 1.35; }
.quiz-options-row { width: 100%; gap: clamp(22px, 4vw, 54px); }
.quiz-opt-btn {
  min-height: 92px;
  border-width: 3px;
  border-color: #d6a342;
  border-radius: 25px;
  background: linear-gradient(#fffdf5, #fff5df);
  color: #422815;
  box-shadow: inset 0 0 0 5px rgba(255,255,255,.7), 0 9px 18px rgba(94, 55, 15, .14);
  font-size: 28px;
}
.quiz-options-row .quiz-opt-btn { flex: 1; max-width: 360px; min-width: 250px; }
.answer-symbol { display: grid; width: 48px; height: 48px; place-items: center; border-radius: 50%; background: #ffe08e; color: #4b8a49; font-size: 23px; }
.answer-symbol svg { filter: drop-shadow(0 2px 1px rgba(111, 61, 8, .22)); }
.answer-symbol-yes { background: #ffd84f; color: #8e4b12; }
.answer-symbol-no { background: #ffb6bd; color: #a5334d; }
.option-no { border-color: #db826d; }
.option-no .answer-symbol { background: #ffd1cf; color: #b84a4d; }
.quiz-opt-btn.selected { border-color: var(--green); background: #edf8e8; box-shadow: inset 0 0 0 5px rgba(255,255,255,.8), 0 0 0 4px rgba(47, 128, 104, .18), 0 10px 20px rgba(47, 128, 104, .16); }
.option-no.selected { border-color: #c55c57; background: #fff0ed; box-shadow: inset 0 0 0 5px rgba(255,255,255,.8), 0 0 0 4px rgba(197, 92, 87, .15), 0 10px 20px rgba(156, 68, 64, .13); }
.quiz-options-col { width: min(760px, 100%); }
.scenario-opt { min-height: 64px; padding: 12px 18px; font-size: 16px; }
.quiz-submit-btn { min-width: 300px; min-height: 62px; margin-top: 28px; border-color: #4c8a76; background: linear-gradient(#91c0ad, #73aa96); box-shadow: inset 0 2px rgba(255,255,255,.3), 0 6px 0 #4e8672; font-size: 22px; }
.quiz-submit-btn:disabled { border-color: #9fbdae; background: #a9c9bc; box-shadow: 0 5px 0 #84a99a; opacity: .76; }
.quiz-result-character { width: 150px; height: 150px; object-fit: contain; margin: -30px 0 4px; }
.quiz-result-title { margin: 0 0 10px; font-size: 30px; }
.quiz-explanation { max-width: 720px; font-size: 17px; line-height: 1.7; }
.quiz-side-character { position: absolute; right: -290px; bottom: 14px; width: 300px; filter: drop-shadow(0 17px 19px rgba(72, 42, 13, .3)); z-index: 1; pointer-events: none; }
.quiz-stars { gap: 9px; margin-top: 22px; flex-wrap: nowrap; }
.quiz-star { position: relative; display: grid; width: 47px; height: 47px; place-items: center; color: #b98949; font-size: 38px; }
.quiz-star small { position: absolute; color: #764916; font-size: 12px; font-weight: 900; }
.quiz-star.earned { color: #f2a51b; }
.quiz-star.wrong { width: 42px; height: 42px; margin: 2px; border: 2px solid #cf6a62; border-radius: 50%; color: #cf6a62; font-size: 18px; }
.quiz-star.current { color: #f2a51b; transform: translateY(-3px); filter: drop-shadow(0 3px 4px rgba(185, 112, 11, .25)); }
.quiz-reward-note { display: flex; justify-content: center; align-items: center; gap: 8px; width: fit-content; margin: 12px auto 0; padding: 8px 20px; border-radius: 999px; background: rgba(255, 249, 229, .82); color: #755431; font-weight: 700; }
.quiz-reward-note svg { color: #ef9e17; }
.game-over-card,
.memory-complete-card,
.sort-result-card,
.ceremony-card,
.complete-card { border: 4px solid var(--green); background: radial-gradient(circle at top, #fffdf5, #fff2d4); box-shadow: 0 0 0 8px rgba(245, 204, 128, .75), 0 20px 50px rgba(74, 43, 13, .24); }
.game-over-icon { color: #e34d68; font-size: 48px; }

.memory-play-area,
.sort-play-area,
.pledge-stage { width: min(1280px, 100%); margin-left: auto; margin-right: auto; border-color: rgba(42, 112, 86, .62); background: rgba(255, 248, 227, .95); box-shadow: 0 18px 42px rgba(76, 44, 13, .17); }
.memory-bell-guide img,
.sort-bell-guide img { width: 118px; height: 118px; object-fit: contain; }
.memory-card-front { background: linear-gradient(145deg, var(--green), var(--green-deep)); border-color: #d8a64d; }
.memory-card-back { background: linear-gradient(#fffdf5, #fff2d6); border-color: #d9a557; }
.memory-card.matched .memory-card-back { border-color: var(--green); box-shadow: 0 0 16px rgba(47,128,104,.24); }
.card-icon { color: var(--green); }
.card-icon img { width: 30px; height: 30px; object-fit: contain; display: block; }
.phase-result-character { width: 180px; height: 180px; object-fit: contain; margin: -96px auto 4px; filter: drop-shadow(0 10px 12px rgba(74,43,13,.2)); }
.sort-basket { border-width: 3px; background: rgba(255, 253, 244, .94); }
.basket-label > svg { font-size: 24px; }
.safe-basket .basket-label > svg { color: var(--green); }
.adult-basket .basket-label > svg { color: #c9851a; }
.unsafe-basket .basket-label > svg { color: #c85555; }
.sort-pool-label,
.memory-tips-title,
.ach-title,
.complete-ach-title { display: flex; align-items: center; justify-content: center; gap: 8px; }
.sort-result-icon { color: var(--gold); font-size: 58px; }
.pledge-bell-large img { width: 180px; height: 180px; object-fit: contain; }
.pledge-lamp img { width: 40px; height: 40px; object-fit: contain; }
.pledge-lamp > svg { color: #a68150; font-size: 26px; }
.pledge-item { border-color: rgba(190, 128, 38, .3); background: #fffaf0; }
.pledge-item.lit { border-color: var(--green); background: #edf7e9; }
.pledge-item-title { display: flex; align-items: center; gap: 6px; }
.ceremony-shield { width: 96px; height: 96px; object-fit: contain; margin-bottom: 10px; }
.complete-card { max-width: 760px; padding-top: 34px; }
.complete-character { width: 210px; height: 210px; object-fit: contain; margin: -130px auto -18px; filter: drop-shadow(0 13px 15px rgba(74,43,13,.22)); }
.complete-star { color: var(--gold); }
.complete-ach-icon,
.ach-icon { color: #d78b1e; }
.complete-replay-btn,
.sort-retry-btn,
.game-over-retry { display: inline-flex; align-items: center; justify-content: center; gap: 8px; }

@media (max-width: 1280px) {
  .phase-intro { padding-left: 28px; padding-right: 28px; }
  .intro-shell { grid-template-columns: minmax(0, 1fr) 220px; }
  .intro-card { padding-left: 30px; padding-right: 30px; }
  .intro-stage { height: 144px; }
  .stage-name { font-size: 16px; }
  .rule-chip { font-size: 12px; }
  .quiz-card-wrap { width: min(900px, calc(100% - 220px)); }
  .quiz-side-character { right: -230px; width: 245px; }
}

@media (max-width: 1024px) {
  .phase-intro { padding-top: 96px; }
  .intro-shell { display: block; width: min(900px, 100%); }
  .intro-side-panel { display: none; }
  .intro-rules { grid-template-columns: repeat(2, 1fr); }
  .quiz-card-wrap { width: min(900px, 100%); }
  .quiz-side-character { display: none; }
  .quiz-topbar,
  .memory-topbar,
  .sort-topbar { grid-template-columns: auto 1fr; gap: 10px; }
  .topbar-stats { grid-column: 1 / -1; justify-content: center; }
  .quiz-type-tag::before,
  .quiz-type-tag::after { width: 38px; }
}

@media (max-width: 768px) {
  .safety-game { background-attachment: scroll; }
  .phase-intro { min-height: calc(100vh - 56px); padding: 84px 14px 24px; }
  .intro-card { padding: 32px 24px; }
  .intro-card { padding: 76px 16px 24px; border-radius: 28px; }
  .intro-guardian { top: -70px; width: 150px; }
  .intro-title { font-size: 34px; letter-spacing: 2px; }
  .intro-eyebrow { font-size: 14px; letter-spacing: 1px; }
  .intro-desc { font-size: 14px; }
  .intro-desc br { display: none; }
  .intro-stages { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .intro-stage { height: 130px; padding-top: 18px; }
  .intro-stage-arrow { display: none; }
  .stage-icon { width: 46px; height: 46px; font-size: 24px; }
  .intro-rules { grid-template-columns: 1fr; gap: 7px; }
  .intro-btn { min-width: 0; width: 100%; min-height: 58px; font-size: 23px; }
  .intro-safety-note { align-items: flex-start; font-size: 12px; }
  .phase-quiz,
  .phase-memory,
  .phase-sort,
  .phase-pledge { padding: 12px 12px 28px; }
  .quiz-topbar,
  .memory-topbar,
  .sort-topbar,
  .pledge-header { display: flex; flex-wrap: wrap; min-height: 0; padding: 8px; }
  .phase-back { min-height: 38px; padding: 0 13px; }
  .topbar-center { flex: 1; min-width: 210px; }
  .phase-badge { padding: 8px 12px; font-size: 14px; }
  .quiz-progress-wrap { min-width: 130px; }
  .topbar-stats { width: 100%; }
  .quiz-card-wrap { margin-top: 88px; }
  .quiz-bell-character { top: -88px; }
  .quiz-bell-character img { width: 112px; height: 112px; }
  .quiz-bell-speech { display: none; }
  .quiz-question-card { min-height: 390px; padding: 42px 18px 26px; border-radius: 28px; }
  .quiz-question-front,
  .quiz-question-back { min-height: 300px; }
  .quiz-type-tag::before,
  .quiz-type-tag::after { display: none; }
  .quiz-q-text { margin: 27px auto; font-size: 23px; }
  .sort-baskets.three-baskets { grid-template-columns: 1fr; }
  .memory-grid { grid-template-columns: repeat(4, 1fr); gap: 8px; }
  .memory-card { aspect-ratio: 2 / 3; }
  .card-text { font-size: 11px; }
  .quiz-options-row { flex-direction: column; align-items: stretch; gap: 14px; }
  .quiz-options-row .quiz-opt-btn { max-width: none; min-width: 0; width: 100%; min-height: 70px; font-size: 23px; }
  .answer-symbol { width: 38px; height: 38px; font-size: 18px; }
  .quiz-submit-btn { min-width: 0; width: min(280px, 100%); min-height: 54px; font-size: 19px; }
  .quiz-stars { gap: 2px; overflow-x: auto; justify-content: flex-start; padding: 3px 4px 8px; }
  .quiz-star { flex: 0 0 36px; width: 36px; height: 36px; font-size: 30px; }
  .quiz-reward-note { width: 100%; text-align: center; font-size: 12px; }
  .pledge-title { font-size: 26px; }
  .ceremony-card { padding: 28px 20px; }
  .topbar-stats { gap: 8px; }
  .complete-card { padding: 36px 24px; }
  .complete-stats { gap: 8px; }
  .stat-item { padding: 10px 14px; min-width: 70px; }
  .stat-num { font-size: 20px; }
}

/* Generated safety-classroom asset states */
.stage-icon img {
  width: 58px;
  height: 58px;
  object-fit: contain;
  filter: drop-shadow(0 5px 7px rgba(75, 44, 15, .18));
}
.intro-bell-wall {
  position: absolute;
  z-index: 0;
  right: 0;
  bottom: 0;
  width: min(230px, 92%);
  opacity: .34;
  pointer-events: none;
  filter: drop-shadow(0 12px 16px rgba(74, 43, 13, .2));
}
.intro-side-panel { position: relative; }
.intro-side-panel > :not(.intro-bell-wall) { position: relative; z-index: 1; }
.sort-result-character {
  display: block;
  width: 132px;
  height: 132px;
  margin: -74px auto 4px;
  object-fit: contain;
  filter: drop-shadow(0 10px 14px rgba(74, 43, 13, .22));
}
.pledge-stage {
  position: relative;
  isolation: isolate;
}
.pledge-bell-wall {
  position: absolute;
  z-index: 0;
  top: 86px;
  right: 3%;
  width: min(260px, 24vw);
  opacity: .18;
  pointer-events: none;
}
.pledge-stage > :not(.pledge-bell-wall) { position: relative; z-index: 1; }
.ceremony-character {
  display: block;
  width: 150px;
  height: 150px;
  margin: -90px auto 0;
  object-fit: contain;
  filter: drop-shadow(0 10px 18px rgba(74, 43, 13, .25));
}

@media (max-width: 768px) {
  .stage-icon img { width: 48px; height: 48px; }
  .pledge-bell-wall { display: none; }
}
.scenario-sort .sort-bell-msg{font-size:20px}.scenario-sort .sort-play-area{max-width:980px}.scenario-card{display:grid;grid-template-columns:38% 62%;margin:18px auto 0;background:#fff9e9;border:4px solid #2f8068;border-radius:26px;overflow:hidden;box-shadow:0 12px 0 rgba(132,87,31,.18)}.scenario-visual{display:grid;place-items:center;position:relative;min-height:390px;background:#f8e9c8}.scenario-visual img{width:72%;max-height:290px;object-fit:contain;filter:drop-shadow(0 10px 8px rgba(101,64,22,.2))}.scenario-visual span{position:absolute;top:18px;left:20px;display:grid;place-items:center;width:44px;height:44px;border-radius:50%;background:#e8a31b;color:#fff;font-size:22px;font-weight:900}.scenario-copy{padding:42px}.scenario-copy small{color:#2f8068;font-weight:900}.scenario-copy h2{margin:10px 0;font-size:40px;color:#3b2a18}.scenario-copy>p{font-size:20px;line-height:1.6;color:#775936}.scenario-options{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-top:26px}.scenario-options button{min-height:68px;border:3px solid;border-radius:17px;background:#fffdf5;font-size:19px;font-weight:900;cursor:pointer}.scenario-safe{border-color:#63a98d;color:#2f8068}.scenario-stop{border-color:#e9a1a4;color:#c64a51}.scenario-options .selected{box-shadow:0 0 0 4px #e9b33d inset}.scenario-feedback{margin-top:20px;padding:15px 18px;border-radius:15px;background:#fff0e6;color:#b25435}.scenario-feedback.correct{background:#e8f5e8;color:#287553}.scenario-feedback p{margin:6px 0 10px;line-height:1.6}.scenario-feedback button{border:1px solid #cda665;border-radius:999px;background:#fff8e8;padding:9px 18px;font-weight:800;cursor:pointer}.scenario-feedback button:last-child{background:#2f8068;color:#fff;border:0}@media(max-width:760px){.scenario-card{grid-template-columns:1fr}.scenario-visual{min-height:220px}.scenario-visual img{max-height:180px}.scenario-copy{padding:26px 20px}.scenario-copy h2{font-size:30px}.scenario-copy>p{font-size:17px}.scenario-options{grid-template-columns:1fr}.scenario-options button{min-height:56px;font-size:17px}}
</style>

<template>
  <main class="safety-game" :style="{ '--map-bg': `url(${safetyBackground})` }">
    <header v-if="phase !== 'intro'" class="game-bar">
      <button class="ghost-btn" @click="exitGame"><ArrowLeftOutlined /> 保存并返回</button>
      <div class="stage-pill">{{ stageLabel }}</div>
      <div class="score-pill"><StarFilled /> {{ totalScore }}</div>
    </header>

    <section v-if="phase === 'intro'" class="intro panel">
      <img :src="detectiveSafety" alt="小铜人安全守护者" class="hero" />
      <p class="eyebrow">杏林安全守护案</p>
      <h1>看得懂危险，也做得到安全</h1>
      <p class="lead">四关图文训练：先判断，再做情境选择，用翻牌巩固，最后完成安全宣誓。</p>
      <div class="stage-route">
        <article v-for="(item, index) in stageCards" :key="item.title">
          <span>{{ index + 1 }}</span><img :src="item.image" :alt="item.title" />
          <strong>{{ item.title }}</strong><small>{{ item.count }}</small>
        </article>
      </div>
      <p v-if="loadError" class="error-text">{{ loadError }}</p>
      <button class="primary-btn" :disabled="loading || quizQuestions.length !== 14 || !gameConfig" @click="startGame">
        {{ loading ? '正在准备训练…' : '开始安全训练' }}
      </button>
      <button v-if="loadError" class="link-btn" @click="loadAll">重新加载</button>
    </section>

    <section v-else-if="phase === 'quiz'" class="content-shell">
      <div class="progress-line"><i :style="{ width: `${(quizIndex + 1) / quizQuestions.length * 100}%` }"></i></div>
      <article class="quiz-card panel">
        <div class="media-stage">
          <video v-if="quizVideoAvailable" ref="quizVideoRef" controls playsinline preload="metadata" :poster="currentQuiz.sceneImage || undefined" @error="quizVideoFailed = true">
            <source :src="currentQuiz.sceneVideo" type="video/mp4" />
          </video>
          <img v-else :src="currentQuiz.sceneImage || detectiveThink" :alt="currentQuiz.sceneImageAlt" />
          <span class="scene-chip">第 {{ quizIndex + 1 }} / {{ quizQuestions.length }} 题</span>
        </div>
        <div class="question-copy">
          <p class="eyebrow">{{ currentQuiz.type === 'TRUE_FALSE' ? '安全判断' : '场景模拟' }}</p>
          <h2>{{ currentQuiz.title }}</h2>
          <div v-if="!quizRevealed" class="answer-list">
            <button v-for="option in currentQuiz.options" :key="option.key" :class="{ selected: quizSelected === option.key }" @click="quizSelected = option.key">
              <b>{{ option.key }}</b><span>{{ option.text }}</span>
            </button>
            <button class="primary-btn" :disabled="!quizSelected || quizSubmitting" @click="submitQuiz">确认答案</button>
          </div>
          <div v-else class="feedback" :class="quizCorrect ? 'success' : 'warning'">
            <strong>{{ quizCorrect ? '守护成功！' : '安全提醒' }}</strong>
            <p>{{ quizExplanation }}</p>
            <button class="primary-btn" @click="nextQuiz">{{ quizIndex === quizQuestions.length - 1 ? '进入第二关' : '下一题' }}</button>
          </div>
        </div>
      </article>
      <div class="lives"><HeartFilled v-for="n in 3" :key="n" :class="{ lost: n > quizLives }" /></div>
      <div v-if="quizLives <= 0" class="overlay"><div class="modal-card"><h2>先复习一下，再来挑战</h2><button class="primary-btn" @click="resetQuiz">重新开始第一关</button></div></div>
    </section>

    <section v-else-if="phase === 'scenario'" class="content-shell">
      <div class="scenario-counter">情境 {{ scenarioIndex + 1 }} / {{ scenarios.length }}</div>
      <article class="scenario-card panel">
        <div class="scenario-picture">
          <img :src="visualFor(currentScenario)" :alt="currentScenario.imageAlt" />
          <span v-if="scenarioAnswered" class="result-stamp" :class="scenarioCorrect ? 'ok' : 'tip'">{{ scenarioCorrect ? '守护成功' : '安全提醒' }}</span>
        </div>
        <div class="scenario-copy">
          <p class="eyebrow">生活安全情境</p><h2>{{ currentScenario.title }}</h2><p class="prompt">{{ currentScenario.prompt }}</p>
          <div class="action-grid">
            <button v-for="option in currentScenario.options" :key="option.id" :disabled="scenarioSubmitting || scenarioCorrect" :class="{ chosen: scenarioChoice === option.id, right: scenarioAnswered && option.id === scenarioCorrectOptionId }" @click="chooseScenario(option)">
              <img :src="visualFor(option)" :alt="option.imageAlt" /><span>{{ option.text }}</span>
            </button>
          </div>
          <div v-if="scenarioAnswered" class="feedback" :class="scenarioCorrect ? 'success' : 'warning'">
            <p>{{ scenarioExplanation }}</p>
            <button v-if="scenarioCorrect" class="primary-btn" @click="nextScenario">{{ scenarioIndex === scenarios.length - 1 ? '进入翻牌挑战' : '下一情境' }}</button>
            <button v-else class="secondary-btn" @click="retryScenario">重新选择</button>
          </div>
        </div>
      </article>
    </section>

    <section v-else-if="phase === 'memory'" class="memory-shell">
      <div v-if="!memoryStarted" class="memory-setup panel">
        <img :src="detectiveThink" alt="思考中的小铜人" class="setup-character" />
        <p class="eyebrow">纯图案翻牌</p><h1>选一个难度和玩法</h1>
        <div class="difficulty-grid">
          <button v-for="level in levelOptions" :key="level.id" :disabled="!isLevelUnlocked(level.id)" :class="{ selected: memoryLevel === level.id }" @click="memoryLevel = level.id">
            <img :src="mediaUrl(gameConfig?.memory?.levelBadges?.[level.id]) || mediaUrl(safetyGuardianAssets.badges[level.id]?.file)" :alt="`${level.name}难度徽章`" class="difficulty-badge" />
            <b>{{ level.name }}</b><span>{{ level.grid }} · {{ level.pairs }}组配对</span><small>{{ isLevelUnlocked(level.id) ? '可以挑战' : '完成上一难度后解锁' }}</small>
          </button>
        </div>
        <div class="mode-switch"><button :class="{ active: memoryMode === 'single' }" @click="memoryMode = 'single'">单人练习</button><button :class="{ active: memoryMode === 'pk' }" @click="memoryMode = 'pk'">双人PK</button></div>
        <label v-if="memoryMode === 'pk'" class="player-input">玩家2称呼<input v-model.trim="playerTwoName" maxlength="12" placeholder="例如：小杏仁" /></label>
        <button class="primary-btn" :disabled="memoryMode === 'pk' && !playerTwoName" @click="startMemory">开始翻牌</button>
      </div>

      <template v-else>
        <div class="memory-hud panel">
          <div class="memory-level-display"><img :src="activeLevelBadge" :alt="`${activeLevel.name}难度徽章`" /><span><small>难度</small><b>{{ activeLevel.name }} {{ activeLevel.grid }}</b></span></div>
          <div v-for="(player, index) in players" :key="player.name" :class="{ current: memoryMode === 'pk' && currentPlayer === index }"><small>{{ memoryMode === 'pk' && currentPlayer === index ? '当前回合' : '得分' }}</small><b>{{ player.name }} · {{ player.score }}</b></div>
          <div><small>翻牌</small><b>{{ memoryFlipCount }} 次</b></div>
        </div>
        <div class="memory-board" :style="{ '--columns': activeLevel.columns }">
          <button v-for="(card, index) in memoryCards" :key="card.cardId" class="memory-card" :class="{ flipped: card.flipped || card.matched, matched: card.matched, center: card.center }" :disabled="card.center" @click="flipCard(index)">
            <span class="card-inner"><span class="card-front">?</span><span class="card-back"><img :src="visualFor(card)" :alt="card.imageAlt" /></span></span>
          </button>
        </div>
        <div v-if="lastMatchedPair" class="match-story panel"><img :src="visualFor(lastMatchedPair.left)" :alt="lastMatchedPair.left.imageAlt" /><span>→</span><img :src="visualFor(lastMatchedPair.right)" :alt="lastMatchedPair.right.imageAlt" /><p>{{ lastMatchedPair.explanation }}</p></div>
        <p class="memory-message">{{ memoryFeedback }}</p>
        <div v-if="memoryComplete" class="overlay"><div class="modal-card">
          <img :src="detectiveReward" alt="祝贺的小铜人" /><h2>{{ memoryResultTitle }}</h2><p>完成 {{ activeLevel.name }}，共翻牌 {{ memoryFlipCount }} 次。</p>
          <div class="modal-actions"><button v-if="nextLevel && isLevelUnlocked(nextLevel.id)" class="secondary-btn" @click="challengeLevel(nextLevel.id)">挑战{{ nextLevel.name }}</button><button class="primary-btn" @click="goToPledge">进入安全宣誓</button></div>
        </div></div>
      </template>
    </section>

    <section v-else-if="phase === 'pledge'" class="pledge-shell">
      <article v-if="!pledgeDone" class="pledge-dialog panel">
        <img :src="detectiveSafety" alt="带读安全宣誓的小铜人" />
        <div class="speech" :style="{ backgroundImage: `url(${pledgeCloudUrl})` }">
          <p class="eyebrow">第 {{ pledgeIndex + 1 }} / {{ pledgeItems.length }} 句</p>
          <h2>{{ pledgeItems[pledgeIndex].title }}</h2><p>{{ pledgeItems[pledgeIndex].text }}</p>
          <button class="primary-btn" @click="confirmPledge">我读完了 <span>🔔</span></button>
        </div>
        <div class="bell-track"><span v-for="(item, index) in pledgeItems" :key="item.id" :class="{ lit: index < pledgeIndex }"><img :src="index < pledgeIndex ? (mediaUrl(gameConfig?.pledge?.bellPath) || pledgeBellLit) : bellSprite" alt="安全铃铛" /></span></div>
      </article>
      <div v-else class="certificate-wrap">
        <article ref="certificateRef" class="certificate" :style="certificateStyle">
          <img :src="certificateBadge" alt="安全小卫士徽章" class="certificate-badge" />
          <p>杏林安全守护案</p><h1>{{ certificate.title }}</h1>
          <h2>{{ userStore.displayName }}</h2><p>完成全部安全训练，授予</p><strong>{{ certificate.honor }}</strong>
          <p class="cert-blessing">{{ certificate.blessing }}</p>
          <dl><div><dt>总分</dt><dd>{{ totalScore }}</dd></div><div><dt>最高难度</dt><dd>{{ highestLevelName }}</dd></div><div><dt>游戏模式</dt><dd>{{ memoryMode === 'pk' ? '双人PK' : '单人练习' }}</dd></div></dl>
          <footer><span>{{ certificate.issuer }}</span><span>{{ certificateDate }}</span><span>编号 {{ certificateNo }}</span></footer>
          <img v-if="certificate.sealPath" :src="mediaUrl(certificate.sealPath)" alt="颁发签章" class="certificate-seal" />
        </article>
        <div class="certificate-actions"><button class="secondary-btn" @click="downloadCertificate">下载PNG证书</button><button class="secondary-btn" @click="printCertificate">打印证书</button><button class="primary-btn" @click="finishGame">完成训练</button></div>
      </div>
    </section>

    <section v-else class="complete panel">
      <img :src="detectiveComplete" alt="完成训练的小铜人" /><p class="eyebrow">训练完成</p><h1>你已经是安全小卫士啦！</h1>
      <p>记住：只观察、只学习，遇到问题及时向可信任的大人求助。</p>
      <div><button class="secondary-btn" @click="resetGame">再次训练</button><button class="primary-btn" @click="router.push('/home-map')"><HomeOutlined /> 返回地图</button></div>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, HeartFilled, HomeOutlined, StarFilled } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import html2canvas from 'html2canvas'
import { useGameState } from '@/composables/useGameState'
import { useUserStore } from '@/store/user'
import { clearSafetyGameDraft, getSafetyGameDraft, saveSafetyGameDraft } from '@/api/GameApi'
import { getQuizStage, submitQuizStageAnswer } from '@/api/QuizApi'
import { completeSafetyMemoryLevel, getSafetyGameConfig, getSafetyMemoryProgress, submitSafetyScenarioAnswer } from '@/api/SafetyGameConfigApi'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'
import { safetyPledge } from '@/data/safetyQuestionsGame'
import { actionAssetFor, memoryAssetFor, quizAssetFor, scenarioAssetFor, safetyGuardianAssets } from '@/data/safetyGuardianAssets'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import safetyBackground from '@/assets/maps/xinglin-detective-map-bg.png'

const router = useRouter()
const userStore = useUserStore()
const { addMaterials, completeStandaloneTask } = useGameState()
const bellSprite = generatedMaterialIcons['safety-bell']
const detectiveSafety = generatedRewardAssets.detectiveSafety
const detectiveDiscover = generatedRewardAssets.detectiveDiscover
const detectiveThink = generatedRewardAssets.detectiveThink
const detectiveReward = generatedRewardAssets.detectiveReward
const detectiveComplete = generatedRewardAssets.detectiveComplete
const safetyBadge = generatedRewardAssets.safetyObservationBadge
const mediaUrl = path => resolveMediaUrl(path || '')
const pledgeCloud = mediaUrl(safetyGuardianAssets.pledge.cloud.file)
const pledgeBellLit = mediaUrl(safetyGuardianAssets.pledge.bell.file)
const builtins = { 'guardian-safety': detectiveSafety, 'guardian-help': detectiveDiscover, 'guardian-stop': detectiveSafety, 'guardian-think': detectiveThink, 'guardian-teacher': detectiveDiscover, 'guardian-video': detectiveThink, 'guardian-family': detectiveSafety, 'guardian-doctor': detectiveDiscover, 'guardian-pet': detectiveThink, 'guardian-learn': detectiveDiscover, 'safety-badge': safetyBadge, 'safety-bell': bellSprite }
const visualFor = item => mediaUrl(item?.imagePath) || builtins[item?.visualKey] || detectiveSafety

function applySafetyAssetDefaults(config) {
  const target = config || {}
  target.scenarios = (target.scenarios || []).map(scenario => ({
    ...scenario,
    imagePath: scenario.imagePath || scenarioAssetFor(scenario.id)?.file || '',
    options: (scenario.options || []).map(option => ({
      ...option,
      imagePath: option.imagePath || actionAssetFor(option.id)?.file || ''
    }))
  }))
  const memory = target.memory || {}
  memory.centerBadge = {
    ...(memory.centerBadge || {}),
    imagePath: memory.centerBadge?.imagePath || safetyGuardianAssets.badges.CENTER.file,
    imageAlt: memory.centerBadge?.imageAlt || safetyGuardianAssets.badges.CENTER.alt
  }
  memory.levelBadges = {
    BEGINNER: memory.levelBadges?.BEGINNER || safetyGuardianAssets.badges.BEGINNER.file,
    INTERMEDIATE: memory.levelBadges?.INTERMEDIATE || safetyGuardianAssets.badges.INTERMEDIATE.file,
    ADVANCED: memory.levelBadges?.ADVANCED || safetyGuardianAssets.badges.ADVANCED.file
  }
  memory.pairs = (memory.pairs || []).map(pair => ({
    ...pair,
    left: { ...(pair.left || {}), imagePath: pair.left?.imagePath || memoryAssetFor(pair.id, 'left')?.file || '' },
    right: { ...(pair.right || {}), imagePath: pair.right?.imagePath || memoryAssetFor(pair.id, 'right')?.file || '' }
  }))
  target.memory = memory
  const pledge = target.pledge || {}
  pledge.dialogPath = pledge.dialogPath || safetyGuardianAssets.pledge.cloud.file
  pledge.bellPath = pledge.bellPath || safetyGuardianAssets.pledge.bell.file
  pledge.certificate = {
    ...(pledge.certificate || {}),
    backgroundPath: pledge.certificate?.backgroundPath || safetyGuardianAssets.pledge.background.file,
    badgePath: pledge.certificate?.badgePath || safetyGuardianAssets.pledge.badge.file,
    sealPath: pledge.certificate?.sealPath || safetyGuardianAssets.pledge.seal.file
  }
  target.pledge = pledge
  return target
}

const phase = ref('intro')
const totalScore = ref(0)
const loading = ref(false)
const loadError = ref('')
const gameConfig = ref(null)
const configRevision = ref(null)
const quizQuestions = ref([])
const quizRevision = ref(null)
const quizIndex = ref(0)
const quizSelected = ref(null)
const quizRevealed = ref(false)
const quizCorrect = ref(false)
const quizExplanation = ref('')
const quizLives = ref(3)
const quizSubmitting = ref(false)
const quizVideoRef = ref(null)
const quizVideoFailed = ref(false)
const currentQuiz = computed(() => quizQuestions.value[quizIndex.value] || {})
const quizVideoAvailable = computed(() => currentQuiz.value.sceneVideo && !quizVideoFailed.value)
const scenarios = computed(() => gameConfig.value?.scenarios || [])
const currentScenario = computed(() => scenarios.value[scenarioIndex.value] || { options: [] })
const stageLabel = computed(() => ({ quiz: '第一关 · 图文知识问答', scenario: '第二关 · 图文情境选择', memory: '第三关 · 图案翻牌PK', pledge: '第四关 · 安全宣誓' }[phase.value] || '安全守护案'))
const stageCards = computed(() => [
  { title: '图文知识问答', count: '14题', image: mediaUrl(quizAssetFor('safety-tell-adult')?.file) || detectiveDiscover },
  { title: '情境选择', count: `${scenarios.value.length || 6}个情境`, image: mediaUrl(scenarioAssetFor('scenario-unwell')?.file) || detectiveSafety },
  { title: '图案翻牌', count: '3档难度', image: mediaUrl(safetyGuardianAssets.badges.INTERMEDIATE.file) || detectiveThink },
  { title: '安全宣誓', count: `${gameConfig.value?.pledge?.lines?.length || 6}句`, image: mediaUrl(safetyGuardianAssets.pledge.badge.file) || safetyBadge }
])

async function loadAll() {
  loading.value = true; loadError.value = ''
  try {
    const [quiz, config, progress] = await Promise.all([getQuizStage('SAFETY_KNOWLEDGE', { showDefaultMsg: false }), getSafetyGameConfig(), getSafetyMemoryProgress().catch(() => ({ maxUnlockedLevel: 'BEGINNER' }))])
    quizRevision.value = quiz?.revision
    quizQuestions.value = (quiz?.questions || []).map(item => {
      const defaultAsset = quizAssetFor(item.questionCode)
      return { id: item.id, questionCode: item.questionCode, type: item.questionType, title: item.title, sceneImage: mediaUrl(item.sceneImagePath || defaultAsset?.file), sceneImageAlt: item.sceneImageAlt || defaultAsset?.alt || '安全题目插画', sceneVideo: mediaUrl(item.sceneVideoPath), options: (item.options || []).map(option => ({ key: option.key, text: option.text })) }
    })
    if (quizQuestions.value.length !== 14) throw new Error('第一关必须发布固定14道题')
    configRevision.value = config?.revisionId
    if (!config?.config) throw new Error('第二至第四关尚未发布配置')
    gameConfig.value = applySafetyAssetDefaults(config.config)
    maxUnlockedLevel.value = progress?.maxUnlockedLevel || 'BEGINNER'
    highestCompletedLevel.value = progress?.maxCompletedLevel || null
    buildPledgeItems()
  } catch (error) { loadError.value = error?.message || '安全训练加载失败' } finally { loading.value = false }
}

async function startGame() { await clearDraft(); resetQuiz(); totalScore.value = 0; phase.value = 'quiz' }
function stopQuizVideo() { const video = quizVideoRef.value; if (video) { video.pause(); try { video.currentTime = 0 } catch { /* noop */ } } }
async function submitQuiz() {
  if (!quizSelected.value || quizSubmitting.value) return
  quizSubmitting.value = true
  try {
    const result = await submitQuizStageAnswer('SAFETY_KNOWLEDGE', { stageRevision: quizRevision.value, questionId: currentQuiz.value.id, userAnswer: quizSelected.value }, { showDefaultMsg: false })
    quizCorrect.value = Boolean(result?.correct); quizExplanation.value = result?.explanation || '遇到不确定的情况，要及时告诉可信任的大人。'; quizRevealed.value = true
    if (quizCorrect.value) totalScore.value += 100
    else quizLives.value = Math.max(0, quizLives.value - 1)
  } catch (error) { message.error(error?.message || '答案提交失败') } finally { quizSubmitting.value = false }
}
function nextQuiz() { stopQuizVideo(); if (quizIndex.value === quizQuestions.value.length - 1) { phase.value = 'scenario'; initScenario(); return } quizIndex.value++; quizSelected.value = null; quizRevealed.value = false; quizVideoFailed.value = false }
function resetQuiz() { quizIndex.value = 0; quizSelected.value = null; quizRevealed.value = false; quizCorrect.value = false; quizExplanation.value = ''; quizLives.value = 3; quizVideoFailed.value = false }

const scenarioIndex = ref(0)
const scenarioChoice = ref(null)
const scenarioAnswered = ref(false)
const scenarioCorrect = ref(false)
const scenarioCorrectOptionId = ref(null)
const scenarioExplanation = ref('')
const scenarioSubmitting = ref(false)
function initScenario() { scenarioIndex.value = 0; retryScenario() }
async function chooseScenario(option) {
  if (scenarioSubmitting.value || scenarioCorrect.value) return
  scenarioChoice.value = option.id; scenarioSubmitting.value = true
  try {
    const result = await submitSafetyScenarioAnswer({ configRevision: configRevision.value, scenarioId: currentScenario.value.id, optionId: option.id })
    scenarioAnswered.value = true; scenarioCorrect.value = Boolean(result?.correct); scenarioCorrectOptionId.value = result?.correctOptionId; scenarioExplanation.value = result?.explanation || currentScenario.value.explanation
    if (scenarioCorrect.value) totalScore.value += 120
  } catch (error) { message.error(error?.message || '答案确认失败') } finally { scenarioSubmitting.value = false }
}
function retryScenario() { scenarioChoice.value = null; scenarioAnswered.value = false; scenarioCorrect.value = false; scenarioCorrectOptionId.value = null; scenarioExplanation.value = '' }
function nextScenario() { if (scenarioIndex.value === scenarios.value.length - 1) { phase.value = 'memory'; memoryStarted.value = false } else { scenarioIndex.value++; retryScenario() } }

const levelOptions = [
  { id: 'BEGINNER', name: '初级', grid: '2×2', pairs: 2, columns: 2 },
  { id: 'INTERMEDIATE', name: '中级', grid: '3×3', pairs: 4, columns: 3 },
  { id: 'ADVANCED', name: '高级', grid: '4×4', pairs: 8, columns: 4 }
]
const levelRank = { BEGINNER: 0, INTERMEDIATE: 1, ADVANCED: 2 }
const maxUnlockedLevel = ref('BEGINNER')
const highestCompletedLevel = ref(null)
const memoryLevel = ref('BEGINNER')
const memoryMode = ref('single')
const playerTwoName = ref('')
const memoryStarted = ref(false)
const memoryCards = ref([])
const memoryFlipCount = ref(0)
const memoryMatchedCount = ref(0)
const memoryFeedback = ref('翻开两张图，找出“情境”和“正确行动”的配对。')
const currentPlayer = ref(0)
const players = ref([])
const memoryComplete = ref(false)
const lastMatchedPair = ref(null)
let memoryLocked = false
const activeLevel = computed(() => levelOptions.find(item => item.id === memoryLevel.value) || levelOptions[0])
const activeLevelBadge = computed(() => mediaUrl(gameConfig.value?.memory?.levelBadges?.[memoryLevel.value]) || mediaUrl(safetyGuardianAssets.badges[memoryLevel.value]?.file))
const nextLevel = computed(() => levelOptions[levelRank[memoryLevel.value] + 1] || null)
const isLevelUnlocked = level => levelRank[level] <= levelRank[maxUnlockedLevel.value]
const memoryPairs = computed(() => {
  const ids = gameConfig.value?.memory?.levels?.[memoryLevel.value] || []
  return ids.map(id => gameConfig.value.memory.pairs.find(pair => pair.id === id)).filter(Boolean)
})
function shuffle(items) { const result = [...items]; for (let i = result.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [result[i], result[j]] = [result[j], result[i]] } return result }
function startMemory() {
  const cards = memoryPairs.value.flatMap(pair => [
    { ...pair.left, pairId: pair.id, side: 'left', cardId: `${pair.id}-left`, flipped: false, matched: false },
    { ...pair.right, pairId: pair.id, side: 'right', cardId: `${pair.id}-right`, flipped: false, matched: false }
  ])
  const arranged = shuffle(cards)
  if (memoryLevel.value === 'INTERMEDIATE') arranged.splice(4, 0, { ...gameConfig.value.memory.centerBadge, cardId: 'center-badge', center: true, flipped: true, matched: true })
  memoryCards.value = arranged; memoryStarted.value = true; memoryFlipCount.value = 0; memoryMatchedCount.value = 0; memoryComplete.value = false; currentPlayer.value = 0; lastMatchedPair.value = null; memoryLocked = false
  players.value = memoryMode.value === 'pk' ? [{ name: userStore.displayName, score: 0 }, { name: playerTwoName.value, score: 0 }] : [{ name: userStore.displayName, score: 0 }]
}
async function flipCard(index) {
  const card = memoryCards.value[index]
  if (memoryLocked || card.center || card.flipped || card.matched) return
  card.flipped = true; memoryFlipCount.value++
  const open = memoryCards.value.filter(item => item.flipped && !item.matched && !item.center)
  if (open.length < 2) return
  memoryLocked = true
  const [first, second] = open
  const matched = first.pairId === second.pairId && first.side !== second.side
  if (matched) {
    first.matched = true; second.matched = true; memoryMatchedCount.value++; players.value[currentPlayer.value].score++; totalScore.value += 80
    lastMatchedPair.value = gameConfig.value.memory.pairs.find(pair => pair.id === first.pairId); memoryFeedback.value = lastMatchedPair.value?.explanation || '配对成功！'
    memoryLocked = false
    if (memoryMatchedCount.value === memoryPairs.value.length) await finishMemoryLevel()
  } else {
    memoryFeedback.value = '这两张还不是一组，再观察一下图案。'
    window.setTimeout(() => { first.flipped = false; second.flipped = false; if (memoryMode.value === 'pk') currentPlayer.value = currentPlayer.value === 0 ? 1 : 0; memoryLocked = false }, 700)
  }
}
async function finishMemoryLevel() {
  totalScore.value += Math.max(100, 400 - memoryFlipCount.value * 5)
  try { const result = await completeSafetyMemoryLevel(memoryLevel.value); maxUnlockedLevel.value = result?.maxUnlockedLevel || maxUnlockedLevel.value; highestCompletedLevel.value = result?.maxCompletedLevel || memoryLevel.value } catch { highestCompletedLevel.value = memoryLevel.value }
  memoryComplete.value = true
}
const memoryResultTitle = computed(() => {
  if (memoryMode.value !== 'pk') return '全部配对成功！'
  if (players.value[0]?.score === players.value[1]?.score) return '本局平局！'
  return `${players.value[0]?.score > players.value[1]?.score ? players.value[0].name : players.value[1].name} 获胜！`
})
function challengeLevel(level) { memoryLevel.value = level; memoryStarted.value = false; nextTick(startMemory) }

const pledgeItems = ref([])
const pledgeIndex = ref(0)
const pledgeDone = computed(() => pledgeIndex.value >= pledgeItems.value.length)
const certificateRef = ref(null)
const certificateNo = ref('')
const certificate = computed(() => gameConfig.value?.pledge?.certificate || {})
const pledgeCloudUrl = computed(() => mediaUrl(gameConfig.value?.pledge?.dialogPath) || pledgeCloud)
const certificateBadge = computed(() => mediaUrl(certificate.value.badgePath) || mediaUrl(safetyGuardianAssets.pledge.badge.file) || safetyBadge)
const certificateStyle = computed(() => certificate.value.backgroundPath ? { backgroundImage: `url(${mediaUrl(certificate.value.backgroundPath)})` } : {})
const certificateDate = computed(() => new Intl.DateTimeFormat('zh-CN', { dateStyle: 'long' }).format(new Date()))
const highestLevelName = computed(() => levelOptions.find(item => item.id === highestCompletedLevel.value)?.name || '初级')
function buildPledgeItems() { pledgeItems.value = (gameConfig.value?.pledge?.lines || []).filter(item => item.enabled !== false) }
function goToPledge() { phase.value = 'pledge'; pledgeIndex.value = 0; certificateNo.value = `AQ-${new Date().getFullYear()}-${Math.random().toString(36).slice(2, 8).toUpperCase()}` }
function confirmPledge() { if (pledgeDone.value) return; pledgeIndex.value++; totalScore.value += 50 }
async function downloadCertificate() {
  try { const canvas = await html2canvas(certificateRef.value, { scale: 2, useCORS: true, backgroundColor: '#fffaf0' }); const link = document.createElement('a'); link.download = `${userStore.displayName}-安全小卫士证书.png`; link.href = canvas.toDataURL('image/png'); link.click() } catch { message.error('证书生成失败，请重试') }
}
function printCertificate() { window.print() }

const DRAFT_VERSION = 3
let draftReady = false
let saveTimer = null
const draftKey = computed(() => `xinglin-safety-game-v${DRAFT_VERSION}:${userStore.userId || 'guest'}`)
function draftPayload() { return { version: DRAFT_VERSION, configRevision: configRevision.value, gamePhase: phase.value, totalScore: totalScore.value, quiz: { revision: quizRevision.value, index: quizIndex.value, selected: quizSelected.value, revealed: quizRevealed.value, correct: quizCorrect.value, explanation: quizExplanation.value, lives: quizLives.value }, scenario: { index: scenarioIndex.value, choice: scenarioChoice.value, answered: scenarioAnswered.value, correct: scenarioCorrect.value, correctOptionId: scenarioCorrectOptionId.value, explanation: scenarioExplanation.value }, memory: { level: memoryLevel.value, mode: memoryMode.value, playerTwoName: playerTwoName.value, started: memoryStarted.value, cards: memoryCards.value, flips: memoryFlipCount.value, matched: memoryMatchedCount.value, feedback: memoryFeedback.value, currentPlayer: currentPlayer.value, players: players.value, complete: memoryComplete.value, highestCompletedLevel: highestCompletedLevel.value }, pledgeIndex: pledgeIndex.value, certificateNo: certificateNo.value } }
function saveDraft(immediate = false) {
  if (!draftReady || phase.value === 'intro' || phase.value === 'complete') return
  const draft = draftPayload(); localStorage.setItem(draftKey.value, JSON.stringify(draft))
  clearTimeout(saveTimer)
  if (userStore.isLoggedIn) saveTimer = window.setTimeout(() => saveSafetyGameDraft(draft).catch(() => {}), immediate ? 0 : 1000)
}
async function clearDraft() { localStorage.removeItem(draftKey.value); clearTimeout(saveTimer); if (userStore.isLoggedIn) await clearSafetyGameDraft().catch(() => {}) }
async function restoreDraft() {
  let draft = null
  if (userStore.isLoggedIn) draft = await getSafetyGameDraft().catch(() => null)
  if (!draft) { try { draft = JSON.parse(localStorage.getItem(draftKey.value) || 'null') } catch { draft = null } }
  if (!draft || draft.version !== DRAFT_VERSION) return false
  if (draft.quiz?.revision !== quizRevision.value) { await clearDraft(); message.warning('第一关题库已更新，训练将重新开始'); return false }
  if (draft.configRevision !== configRevision.value && ['scenario', 'memory', 'pledge'].includes(draft.gamePhase)) {
    phase.value = 'scenario'; totalScore.value = Number(draft.totalScore) || 0; initScenario(); message.warning('安全内容已更新，已保留第一关成绩并从第二关重新开始'); return true
  }
  phase.value = draft.gamePhase; totalScore.value = Number(draft.totalScore) || 0
  const q = draft.quiz || {}; quizIndex.value = q.index || 0; quizSelected.value = q.selected || null; quizRevealed.value = Boolean(q.revealed); quizCorrect.value = Boolean(q.correct); quizExplanation.value = q.explanation || ''; quizLives.value = q.lives ?? 3
  const s = draft.scenario || {}; scenarioIndex.value = s.index || 0; scenarioChoice.value = s.choice || null; scenarioAnswered.value = Boolean(s.answered); scenarioCorrect.value = Boolean(s.correct); scenarioCorrectOptionId.value = s.correctOptionId || null; scenarioExplanation.value = s.explanation || ''
  const m = draft.memory || {}; memoryLevel.value = m.level || 'BEGINNER'; memoryMode.value = m.mode || 'single'; playerTwoName.value = m.playerTwoName || ''; memoryStarted.value = Boolean(m.started); memoryCards.value = m.cards || []; memoryFlipCount.value = m.flips || 0; memoryMatchedCount.value = m.matched || 0; memoryFeedback.value = m.feedback || ''; currentPlayer.value = m.currentPlayer || 0; players.value = m.players || []; memoryComplete.value = Boolean(m.complete); highestCompletedLevel.value = m.highestCompletedLevel || highestCompletedLevel.value
  pledgeIndex.value = Math.min(draft.pledgeIndex || 0, pledgeItems.value.length); certificateNo.value = draft.certificateNo || ''
  return true
}
async function exitGame() { stopQuizVideo(); saveDraft(true); await router.push('/home-map') }
async function finishGame() {
  const result = await completeStandaloneTask('main-safety-case', [], { resultCode: 'SAFETY_COMPLETE', idempotencyKey: 'main-safety-case:lifetime' })
  if (!result.success && !result.alreadyCompleted) { message.error('完成状态暂时未确认，请重试'); return }
  if (safetyPledge?.rewards?.length) await addMaterials(safetyPledge.rewards, 'daily-safety-quiz', { progressDelta: 3, resultCode: 'QUIZ_COMPLETE' })
  await clearDraft(); phase.value = 'complete'
}
async function resetGame() { await clearDraft(); totalScore.value = 0; resetQuiz(); initScenario(); memoryStarted.value = false; pledgeIndex.value = 0; phase.value = 'intro' }

watch([phase, totalScore, quizIndex, quizSelected, quizRevealed, quizLives, scenarioIndex, scenarioChoice, scenarioAnswered, memoryCards, memoryFlipCount, currentPlayer, players, pledgeIndex], () => saveDraft(), { deep: true })
onMounted(async () => { await loadAll(); if (!loadError.value) { const restored = await restoreDraft(); if (restored) message.success('已恢复上次训练进度') } draftReady = true })
onBeforeUnmount(() => { stopQuizVideo(); saveDraft(true); clearTimeout(saveTimer) })
</script>

<style scoped>
.safety-game{min-height:calc(100vh - 88px);padding:24px;background:linear-gradient(120deg,rgba(255,248,224,.94),rgba(238,224,180,.9)),var(--map-bg) center/cover;color:#3d2a18;font-family:"PingFang SC","Microsoft YaHei",sans-serif}.panel{background:rgba(255,252,240,.96);border:1px solid #ead09a;border-radius:28px;box-shadow:0 18px 50px rgba(92,61,20,.13)}button{font:inherit}.game-bar{max-width:1180px;margin:0 auto 20px;display:flex;align-items:center;justify-content:space-between;gap:16px}.ghost-btn,.score-pill,.stage-pill{border:1px solid #e2c386;background:#fffaf0;border-radius:999px;padding:10px 18px}.ghost-btn{cursor:pointer}.stage-pill{font-weight:800;color:#176b5a}.score-pill{color:#a96300;font-weight:800}.intro{max-width:1050px;margin:30px auto;padding:48px;text-align:center;position:relative}.hero{width:130px;height:130px;object-fit:contain}.eyebrow{color:#a66a12;font-size:13px;font-weight:900;letter-spacing:.14em;text-transform:uppercase}.intro h1,.complete h1{font-size:clamp(32px,5vw,58px);margin:8px}.lead{font-size:18px;color:#725b3e}.stage-route{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin:34px 0}.stage-route article{padding:18px 12px;background:#f8efd8;border-radius:20px;display:grid;gap:7px;justify-items:center}.stage-route article>span{width:26px;height:26px;border-radius:50%;background:#d89722;color:white;display:grid;place-items:center;font-weight:800}.stage-route img{height:72px;width:72px;object-fit:contain}.stage-route small{color:#8a765b}.primary-btn,.secondary-btn,.link-btn{border:0;border-radius:15px;padding:13px 24px;font-weight:800;cursor:pointer}.primary-btn{background:linear-gradient(135deg,#16715d,#2f9a79);color:white;box-shadow:0 7px 15px rgba(22,113,93,.2)}.secondary-btn{background:#f3dfb3;color:#6a4410}.link-btn{background:transparent;color:#176b5a}.primary-btn:disabled{opacity:.5;cursor:not-allowed}.error-text{color:#b43b2f}.content-shell,.memory-shell,.pledge-shell{max-width:1180px;margin:auto}.progress-line{height:8px;background:#e8d9b8;border-radius:999px;margin-bottom:18px;overflow:hidden}.progress-line i{display:block;height:100%;background:#2c9277}.quiz-card,.scenario-card{display:grid;grid-template-columns:minmax(320px,46%) 1fr;overflow:hidden}.media-stage,.scenario-picture{position:relative;min-height:500px;background:#eee2c4;display:grid;place-items:center}.media-stage img,.media-stage video,.scenario-picture>img{width:100%;height:100%;object-fit:cover}.scene-chip,.scenario-counter{display:inline-flex;padding:8px 14px;border-radius:999px;background:#176b5a;color:white;font-weight:800}.scene-chip{position:absolute;top:18px;left:18px}.question-copy,.scenario-copy{padding:42px}.question-copy h2,.scenario-copy h2{font-size:clamp(25px,3vw,38px);line-height:1.35}.answer-list{display:grid;gap:12px}.answer-list>button:not(.primary-btn){display:flex;gap:14px;align-items:center;text-align:left;border:2px solid #ead3a3;background:white;border-radius:16px;padding:14px;cursor:pointer}.answer-list>button.selected{border-color:#2a8f74;background:#ecf8f3}.answer-list b{width:34px;height:34px;border-radius:50%;background:#e8a832;color:white;display:grid;place-items:center}.feedback{margin-top:18px;padding:18px;border-radius:18px}.feedback.success{background:#e2f5ec;color:#176b5a}.feedback.warning{background:#fff0dc;color:#8d4a18}.lives{margin:16px;text-align:right;color:#e74a68}.lives .lost{color:#cfc6b7}.scenario-counter{margin-bottom:14px}.result-stamp{position:absolute;right:22px;top:20px;padding:12px;border:4px double currentColor;border-radius:50%;font-weight:900;transform:rotate(-10deg);background:#fffaf0}.result-stamp.ok{color:#198069}.result-stamp.tip{color:#bd5c2d}.prompt{font-size:19px}.action-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px}.action-grid button{border:2px solid #e4cca0;background:white;border-radius:18px;padding:10px;cursor:pointer;display:flex;flex-direction:column;gap:8px;align-items:center}.action-grid img{width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:12px}.action-grid .chosen{border-color:#d49a28}.action-grid .right{border-color:#198069;background:#eaf7f2}.memory-setup{padding:38px;text-align:center;max-width:900px;margin:auto}.setup-character{height:110px}.difficulty-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin:28px 0}.difficulty-grid button{border:2px solid #ead3a3;background:#fffdf7;border-radius:18px;padding:20px;display:grid;gap:8px;cursor:pointer}.difficulty-grid button.selected{border-color:#218269;background:#eaf7f2}.difficulty-grid button:disabled{opacity:.45}.mode-switch{display:inline-flex;background:#eee2c7;padding:5px;border-radius:16px}.mode-switch button{border:0;background:transparent;padding:10px 20px;border-radius:12px}.mode-switch button.active{background:white;color:#176b5a;font-weight:800}.player-input{display:flex;gap:12px;justify-content:center;align-items:center;margin:18px}.player-input input{padding:10px;border:1px solid #d7bc83;border-radius:10px}.memory-hud{padding:12px 22px;display:flex;justify-content:space-around;margin-bottom:18px}.memory-hud>div{display:grid;text-align:center;padding:7px 18px;border-radius:14px}.memory-hud>div.current{background:#dff5ec;color:#176b5a}.memory-board{display:grid;grid-template-columns:repeat(var(--columns),minmax(90px,150px));justify-content:center;gap:14px}.memory-card{aspect-ratio:1;border:0;background:transparent;perspective:800px;cursor:pointer}.card-inner{display:block;width:100%;height:100%;position:relative;transform-style:preserve-3d;transition:transform .45s}.memory-card.flipped .card-inner{transform:rotateY(180deg)}.card-front,.card-back{position:absolute;inset:0;border-radius:18px;display:grid;place-items:center;backface-visibility:hidden;overflow:hidden}.card-front{background:linear-gradient(145deg,#126957,#258d71);color:#f8ca61;font-size:38px;border:5px solid #f1d795}.card-back{background:#fffaf0;border:4px solid #d5a54d;transform:rotateY(180deg)}.card-back img{width:100%;height:100%;object-fit:cover}.memory-card.matched .card-back{border-color:#35a47f;box-shadow:0 0 18px #78d5b7}.memory-card.center .card-back{background:#f7e6a7}.match-story{max-width:650px;margin:20px auto;padding:12px;display:grid;grid-template-columns:90px 30px 90px 1fr;align-items:center;gap:10px}.match-story img{width:90px;height:70px;object-fit:cover;border-radius:10px}.memory-message{text-align:center;font-weight:800}.overlay{position:fixed;inset:0;background:rgba(39,27,13,.55);display:grid;place-items:center;z-index:20;padding:20px}.modal-card{max-width:520px;text-align:center;background:#fffaf0;border-radius:28px;padding:34px}.modal-card>img{height:120px}.modal-actions,.certificate-actions{display:flex;justify-content:center;gap:12px;flex-wrap:wrap}.pledge-dialog{max-width:900px;margin:30px auto;padding:36px;display:grid;grid-template-columns:220px 1fr;gap:25px}.pledge-dialog>img{width:100%}.speech{background:#fff8df;border:2px solid #e1bd72;border-radius:24px;padding:30px;position:relative}.speech p:not(.eyebrow){font-size:23px;line-height:1.8}.bell-track{grid-column:1/-1;display:flex;justify-content:center;gap:10px}.bell-track span{width:50px;height:50px;border-radius:50%;background:#ded7c7;opacity:.35;display:grid;place-items:center}.bell-track span.lit{background:#f9d468;opacity:1}.bell-track img{width:40px}.certificate-wrap{display:grid;justify-items:center;gap:20px}.certificate{width:min(900px,100%);min-height:620px;background:#fffaf0 center/cover;border:16px double #bd8128;padding:55px;text-align:center;position:relative;box-shadow:0 18px 50px rgba(84,51,9,.2)}.certificate-badge{height:100px}.certificate h1{font-family:STKaiti,KaiTi,serif;font-size:50px;color:#7d4615}.certificate h2{font-size:35px;border-bottom:2px solid #b98131;display:inline-block;padding:0 30px}.certificate>strong{display:block;font-size:28px;color:#176b5a}.cert-blessing{max-width:650px;margin:25px auto;line-height:1.8}.certificate dl{display:flex;justify-content:center;gap:30px}.certificate dl div{background:#f2e5c7;padding:10px 20px;border-radius:12px}.certificate dt{font-size:12px;color:#876f50}.certificate dd{margin:4px 0;font-weight:800}.certificate footer{display:flex;justify-content:space-between;margin-top:40px;font-size:13px}.certificate-seal{position:absolute;right:65px;bottom:70px;width:95px}.complete{max-width:720px;margin:50px auto;padding:50px;text-align:center}.complete>img{height:160px}.complete>div{display:flex;gap:14px;justify-content:center}.overlay h2{font-size:30px}@media(max-width:760px){.safety-game{padding:12px}.game-bar{gap:6px}.ghost-btn,.stage-pill,.score-pill{padding:8px 10px;font-size:12px}.intro{padding:28px 16px}.stage-route{grid-template-columns:repeat(2,1fr)}.quiz-card,.scenario-card{grid-template-columns:1fr}.media-stage,.scenario-picture{min-height:260px}.question-copy,.scenario-copy{padding:22px}.action-grid{grid-template-columns:1fr}.action-grid button{display:grid;grid-template-columns:90px 1fr;text-align:left}.action-grid img{width:90px}.difficulty-grid{grid-template-columns:1fr}.memory-board{gap:8px;grid-template-columns:repeat(var(--columns),minmax(58px,1fr))}.memory-hud{font-size:11px;padding:8px}.memory-hud>div{padding:5px}.match-story{grid-template-columns:65px 20px 65px;}.match-story img{width:65px;height:55px}.match-story p{grid-column:1/-1}.pledge-dialog{grid-template-columns:1fr;padding:22px}.pledge-dialog>img{width:140px;margin:auto}.certificate{min-height:550px;padding:28px 16px;border-width:9px}.certificate h1{font-size:34px}.certificate dl{gap:6px}.certificate dl div{padding:8px}.certificate footer{flex-direction:column;gap:5px}}@media print{body *{visibility:hidden}.certificate,.certificate *{visibility:visible}.certificate{position:fixed;inset:0;width:100%;box-shadow:none}.game-bar,.certificate-actions{display:none!important}}
.difficulty-badge{width:76px;height:76px;margin:0 auto;object-fit:contain}
.memory-level-display{display:flex!important;align-items:center;gap:10px}
.memory-level-display img{width:44px;height:44px;object-fit:contain}
.memory-level-display span{display:grid;gap:2px}
.speech{background-color:#fff8df;background-repeat:no-repeat;background-position:center;background-size:100% 100%;border:0;padding:38px 48px}
@media(max-width:760px){.speech{padding:28px 24px}}
.media-stage,.scenario-picture{aspect-ratio:4 / 3;min-height:0}
.media-stage img,.media-stage video,.scenario-picture>img{object-fit:contain}
@media(max-width:760px){.media-stage,.scenario-picture{aspect-ratio:4 / 3;min-height:0}}
</style>

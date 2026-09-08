<template>
  <div class="quiz-game-container">
    <!-- 开始页面 -->
    <div
      v-if="gameState === 'ready'"
      class="game-ready"
    >
      <div class="ready-card">
        <div class="ready-icon">
          📝
        </div>
        <h1 class="ready-title">
          答题闯关
        </h1>
        <p class="ready-desc">
          准备好测试你的针灸知识了吗？<br>
          随机抽取<strong>5道题目</strong>，看看你能答对多少！
        </p>
        <div class="ready-rules">
          <h3>游戏规则</h3>
          <ul>
            <li>系统将随机抽取5道题目</li>
            <li>每道题有4个选项，只有1个正确答案</li>
            <li>答题完成后显示正确率</li>
            <li>正确率将记录到你的学习档案</li>
          </ul>
        </div>
        <a-button
          type="primary"
          size="large"
          class="start-btn"
          @click="startGame"
        >
          开始答题
        </a-button>
        <a-button
          size="large"
          class="back-btn"
          @click="goBack"
        >
          返回互动中心
        </a-button>
      </div>
    </div>

    <!-- 答题进行中 -->
    <div
      v-else-if="gameState === 'playing'"
      class="game-playing"
    >
      <!-- 进度条 -->
      <div class="progress-section">
        <div class="progress-info">
          <span class="progress-label">第 {{ currentQuestionIndex + 1 }} / {{ totalQuestions }} 题</span>
          <span class="progress-correct">已答对 {{ correctCount }} 题</span>
        </div>
        <a-progress
          :percent="((currentQuestionIndex + 1) / totalQuestions) * 100"
          :show-info="false"
          :stroke-color="progressColor"
        />
      </div>

      <!-- 题目卡片 -->
      <div class="question-card">
        <div class="question-header">
          <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
          <a-tag :color="difficultyColor(currentQuestion.difficulty)">
            {{ difficultyText(currentQuestion.difficulty) }}
          </a-tag>
        </div>

        <div class="question-content">
          <h2 class="question-text">
            {{ currentQuestion.title || currentQuestion.question }}
          </h2>
        </div>

        <!-- 选项列表 -->
        <div class="options-list">
          <div
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            class="option-item"
            :class="{
              'selected': selectedAnswer === index,
              'correct': showResult && index === correctIdx,
              'wrong': showResult && selectedAnswer === index && index !== correctIdx
            }"
            @click="selectAnswer(index)"
          >
            <span class="option-label">{{ optionLabels[index] }}</span>
            <span class="option-text">{{ option }}</span>
            <span
              v-if="showResult && index === correctIdx"
              class="option-icon"
            >✓</span>
            <span
              v-if="showResult && selectedAnswer === index && index !== correctIdx"
              class="option-icon"
            >✗</span>
          </div>
        </div>

        <!-- 解析（答错后显示） -->
        <div
          v-if="showResult"
          class="explanation-section"
        >
          <div
            class="explanation-header"
            :class="{ 'wrong-answer': isWrong }"
          >
            <span class="explanation-icon">{{ isWrong ? '❌' : '✅' }}</span>
            <span class="explanation-title">{{ isWrong ? '回答错误' : '回答正确' }}</span>
          </div>
          <div class="explanation-content">
            <h4>解析：</h4>
            <p>{{ explanationText }}</p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <a-button
            v-if="!showResult"
            type="primary"
            size="large"
            :disabled="selectedAnswer === null || submitting"
            @click="submitAnswer"
          >
            确认答案
          </a-button>
          <a-button
            v-else
            type="primary"
            size="large"
            @click="nextQuestion"
          >
            {{ isLastQuestion ? '查看结果' : '下一题' }}
          </a-button>
        </div>
      </div>
    </div>

    <!-- 结果页面 -->
    <div
      v-else-if="gameState === 'result'"
      class="game-result"
    >
      <div class="result-card">
        <div class="result-header">
          <div
            class="result-icon"
            :class="{ 'excellent': correctRate >= 80, 'good': correctRate >= 60 && correctRate < 80, 'fair': correctRate < 60 }"
          >
            {{ resultEmoji }}
          </div>
          <h1 class="result-title">
            答题完成！
          </h1>
        </div>

        <div class="result-stats">
          <div class="stat-item">
            <span class="stat-value">{{ correctCount }}</span>
            <span class="stat-label">正确题数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ totalQuestions - correctCount }}</span>
            <span class="stat-label">错误题数</span>
          </div>
          <div class="stat-item highlight">
            <span class="stat-value">{{ correctRate }}%</span>
            <span class="stat-label">正确率</span>
          </div>
        </div>

        <div class="result-rate">
          <a-progress
            type="circle"
            :percent="correctRate"
            :stroke-color="rateColor"
            :width="150"
          >
            <template #format>
              <div class="rate-circle-content">
                <span class="rate-percent">{{ correctRate }}%</span>
                <span class="rate-label">正确率</span>
              </div>
            </template>
          </a-progress>
        </div>

        <div class="result-message">
          <p>{{ resultMessage }}</p>
        </div>

        <div class="result-actions">
          <a-button
            type="primary"
            size="large"
            @click="startGame"
          >
            再来一次
          </a-button>
          <a-button
            size="large"
            @click="goBack"
          >
            返回互动中心
          </a-button>
        </div>

        <!-- 答题记录详情 -->
        <div class="result-details">
          <h3>答题详情</h3>
          <div class="details-list">
            <div
              v-for="(item, index) in questionResults"
              :key="index"
              class="detail-item"
              :class="{ 'correct': item.isCorrect, 'wrong': !item.isCorrect }"
            >
              <span class="detail-number">第 {{ index + 1 }} 题</span>
              <span class="detail-status">{{ item.isCorrect ? '✅ 正确' : '❌ 错误' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div
      v-if="loading"
      class="loading-overlay"
    >
      <a-spin
        size="large"
        tip="正在加载题目..."
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getQuizQuestions, submitQuizAnswer } from '@/api/QuizApi'

const router = useRouter()

const LETTERS = ['A', 'B', 'C', 'D']

// 游戏状态：ready-准备，playing-进行中，result-结果
const gameState = ref('ready')
const loading = ref(false)
const submitting = ref(false)
const lastResult = ref(null)

// 题目相关
const questions = ref([])
const currentQuestionIndex = ref(0)
const selectedAnswer = ref(null)
const showResult = ref(false)
const questionResults = ref([])

// 计算属性
const totalQuestions = computed(() => (questions.value.length ? questions.value.length : 5))
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {})
const correctCount = computed(() => questionResults.value.filter((r) => r.isCorrect).length)
const isLastQuestion = computed(() => currentQuestionIndex.value >= totalQuestions.value - 1)
const correctRate = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round((correctCount.value / totalQuestions.value) * 100)
})
const correctIdx = computed(() => {
  if (!showResult.value || !lastResult.value?.correctAnswer) return -1
  const i = LETTERS.indexOf(String(lastResult.value.correctAnswer).toUpperCase().charAt(0))
  return i >= 0 ? i : -1
})

const explanationText = computed(() => {
  const ex = lastResult.value?.explanation || currentQuestion.value?.explanation
  return ex || '本题考察针灸相关知识点，请认真学习相关知识。'
})

const isWrong = computed(() => {
  if (!showResult.value || selectedAnswer.value === null) return false
  return lastResult.value ? !lastResult.value.correct : false
})

// 选项标签
const optionLabels = ['A', 'B', 'C', 'D']

// 颜色计算
const progressColor = computed(() => {
  const rate = correctCount.value / totalQuestions.value
  if (rate >= 0.6) return '#52c41a'
  if (rate >= 0.4) return '#faad14'
  return '#ff4d4f'
})

const rateColor = computed(() => {
  if (correctRate.value >= 80) return '#52c41a'
  if (correctRate.value >= 60) return '#1890ff'
  if (correctRate.value >= 40) return '#faad14'
  return '#ff4d4f'
})

// 结果表情和消息
const resultEmoji = computed(() => {
  if (correctRate.value >= 80) return '🏆'
  if (correctRate.value >= 60) return '😊'
  if (correctRate.value >= 40) return '🤔'
  return '💪'
})

const resultMessage = computed(() => {
  if (correctRate.value >= 80) return '太棒了！你对针灸知识掌握得非常扎实！'
  if (correctRate.value >= 60) return '不错！继续努力，你会做得更好！'
  if (correctRate.value >= 40) return '还需加强学习，多复习针灸知识哦！'
  return '不要气馁！多学习多练习，你一定可以进步！'
})

// 难度颜色和文本
const difficultyColor = (difficulty) => {
  const colors = {
    'easy': 'green',
    'medium': 'orange',
    'hard': 'red'
  }
  return colors[difficulty] || 'blue'
}

const difficultyText = (difficulty) => {
  const texts = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return texts[difficulty] || '普通'
}

function mapServerQuestion(q) {
  const opts = [q.optionA, q.optionB, q.optionC, q.optionD]
  return {
    id: q.id,
    title: q.title,
    question: q.title,
    options: opts,
    difficulty: q.difficulty || 'easy',
    explanation: q.explanation
  }
}

// 开始游戏
const startGame = async () => {
  loading.value = true
  gameState.value = 'playing'
  currentQuestionIndex.value = 0
  selectedAnswer.value = null
  showResult.value = false
  questionResults.value = []
  lastResult.value = null

  try {
    const data = await getQuizQuestions({ count: 5 }, { showDefaultMsg: false })
    const list = Array.isArray(data) ? data : []
    if (list.length === 0) {
      message.error('暂无题目，请稍后再试')
      gameState.value = 'ready'
      return
    }
    questions.value = list.map(mapServerQuestion)
  } catch (error) {
    console.error('获取题目失败:', error)
    message.error('获取题目失败，请检查网络或登录状态')
    gameState.value = 'ready'
  } finally {
    loading.value = false
  }
}

// 选择答案
const selectAnswer = (index) => {
  if (showResult.value) return
  selectedAnswer.value = index
}

// 提交答案（服务端判题并写入 user_quiz_record / user_quiz_stats）
const submitAnswer = async () => {
  if (selectedAnswer.value === null) {
    message.warning('请选择一个答案')
    return
  }

  submitting.value = true
  try {
    const res = await submitQuizAnswer(
      {
        questionId: currentQuestion.value.id,
        userAnswer: LETTERS[selectedAnswer.value]
      },
      { showDefaultMsg: false }
    )
    lastResult.value = res
    questionResults.value.push({
      questionId: currentQuestion.value.id,
      isCorrect: !!res?.correct
    })
    showResult.value = true
  } catch (e) {
    message.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 下一题
const nextQuestion = () => {
  if (isLastQuestion.value) {
    // 答题结束，显示结果
    finishGame()
  } else {
    currentQuestionIndex.value++
    selectedAnswer.value = null
    showResult.value = false
    lastResult.value = null
  }
}

// 完成游戏（每题已单题提交，此处仅切换界面）
const finishGame = () => {
  gameState.value = 'result'
}

// 返回终极考验
const goBack = () => {
  router.push('/ultimate-challenge')
}

// 页面加载时检查是否已登录
onMounted(() => {
  // 可以在这里检查用户登录状态
})
</script>

<style scoped lang="less">
.quiz-game-container {
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, #f0f5ff 0%, #e6f7ff 100%);
  padding: 32px 24px;
}

/* 准备页面 */
.game-ready {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 128px);
}

.ready-card {
  background: #fff;
  border-radius: 20px;
  padding: 48px;
  max-width: 500px;
  width: 100%;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.ready-icon {
  font-size: 72px;
  margin-bottom: 24px;
}

.ready-title {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
}

.ready-desc {
  font-size: 16px;
  color: #666;
  line-height: 1.8;
  margin: 0 0 32px 0;

  strong {
    color: #1890ff;
    font-size: 20px;
  }
}

.ready-rules {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  text-align: left;

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 16px 0;
  }

  ul {
    margin: 0;
    padding-left: 20px;

    li {
      font-size: 14px;
      color: #666;
      line-height: 2;
    }
  }
}

.start-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 24px;
  margin-bottom: 12px;
}

.back-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
}

/* 答题进行中 */
.game-playing {
  max-width: 800px;
  margin: 0 auto;
}

.progress-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-label {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.progress-correct {
  font-size: 14px;
  color: #52c41a;
  font-weight: 500;
}

.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.question-number {
  font-size: 16px;
  font-weight: 500;
  color: #1890ff;
}

.question-content {
  margin-bottom: 32px;
}

.question-text {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.6;
  margin: 0;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 18px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  padding: 6px 0;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover .option-text {
    color: #1890ff;
  }

  &.selected .option-text {
    color: #1890ff;
    font-weight: 600;
  }

  &.correct .option-text {
    color: #52c41a;
    font-weight: 600;
  }

  &.wrong .option-text {
    color: #ff4d4f;
    font-weight: 600;
  }
}

.option-label {
  width: 26px;
  flex-shrink: 0;
  font-weight: 600;
  color: #333;
  margin-right: 10px;
  line-height: 1.8;
}

.option-text {
  flex: 1;
  font-size: 16px;
  color: #333;
  line-height: 1.8;
}

.option-icon {
  font-size: 18px;
  margin-left: 8px;
  line-height: 1.8;
}

/* 解析区域 */
.explanation-section {
  padding: 10px 0 0;
  margin-bottom: 22px;
  border-top: 1px dashed #d9d9d9;
}

.explanation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;

  &.wrong-answer {
    color: #ff4d4f;
  }
}

.explanation-icon {
  font-size: 18px;
}

.explanation-title {
  font-size: 15px;
  font-weight: 600;
}

.explanation-content {
  h4 {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin: 0 0 6px 0;
  }

  p {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
    margin: 0;
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;

  .ant-btn {
    min-width: 140px;
    height: 44px;
    font-size: 16px;
    border-radius: 22px;
  }
}

/* 结果页面 */
.game-result {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.result-card {
  background: #fff;
  border-radius: 20px;
  padding: 48px;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.result-header {
  text-align: center;
  margin-bottom: 32px;
}

.result-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

.result-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.result-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 32px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-item {
  text-align: center;

  &.highlight {
    .stat-value {
      color: #1890ff;
      font-size: 28px;
    }
  }
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.result-rate {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.rate-circle-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.rate-percent {
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
}

.rate-label {
  font-size: 14px;
  color: #666;
}

.result-message {
  text-align: center;
  margin-bottom: 32px;

  p {
    font-size: 18px;
    color: #666;
    margin: 0;
  }
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 32px;

  .ant-btn {
    min-width: 140px;
    height: 44px;
    font-size: 16px;
    border-radius: 22px;
  }
}

.result-details {
  border-top: 1px solid #e8e8e8;
  padding-top: 24px;

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 16px 0;
  }
}

.details-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;

  &.correct {
    background: #f6ffed;
    color: #52c41a;
  }

  &.wrong {
    background: #fff2f0;
    color: #ff4d4f;
  }
}

.detail-number {
  font-weight: 500;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 1000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .quiz-game-container {
    padding: 16px;
  }

  .ready-card,
  .result-card {
    padding: 32px 24px;
  }

  .ready-title,
  .result-title {
    font-size: 24px;
  }

  .question-card {
    padding: 24px 16px;
  }

  .question-text {
    font-size: 18px;
  }

  .option-item {
    padding: 12px 16px;
  }

  .result-stats {
    padding: 16px;
  }

  .stat-value {
    font-size: 20px;
  }

  .result-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>

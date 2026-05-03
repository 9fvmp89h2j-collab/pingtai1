<template>
  <div class="uc-page">
    <!-- 第一页：说明 + 开始 -->
    <div
      v-if="phase === 'intro'"
      class="uc-intro"
    >
      <p class="uc-intro-text">
        本关会随机出10道题目，全部答对有惊喜！
      </p>
      <button
        type="button"
        class="uc-btn-outline"
        @click="onStart"
      >
        开始答题
      </button>
    </div>

    <!-- 第二页：答题 -->
    <div
      v-else-if="phase === 'quiz'"
      class="uc-quiz"
    >
      <div class="uc-qbox">
        {{ current?.title || '题目' }}
      </div>

      <div class="uc-options">
        <button
          v-for="(opt, idx) in optionEntries"
          :key="idx"
          type="button"
          class="uc-opt"
          :class="{ selected: selectedLetter === LETTERS[idx] }"
          :disabled="submitting"
          @click="selectedLetter = LETTERS[idx]"
        >
          {{ opt || '—' }}
        </button>
      </div>

      <button
        type="button"
        class="uc-btn-outline uc-submit"
        :disabled="!selectedLetter || submitting"
        @click="onSubmit"
      >
        提交
      </button>
    </div>

    <!-- 第三页：本关小结 -->
    <div
      v-else-if="phase === 'result'"
      class="uc-result"
    >
      <div class="uc-result-box">
        <div class="uc-result-title">
          本关答题情况
        </div>
        <div>正确{{ sessionCorrect }}道</div>
        <div>错误{{ sessionWrong }}道</div>
        <div>正确率{{ sessionRateText }}%</div>
        <p
          v-if="sessionCorrect >= 8"
          class="uc-award-hint"
        >
          恭喜！你已达到小奖状条件（答对8道及以上）。
        </p>
      </div>
      <button
        type="button"
        class="uc-btn-outline"
        @click="restart"
      >
        再来一次
      </button>
    </div>

    <a-spin
      v-if="loading"
      class="uc-loading"
      tip="加载题目中…"
    />

    <!-- 反馈弹窗 -->
    <a-modal
      v-model:open="modalOpen"
      :title="modalTitle"
      :footer="null"
      :closable="false"
      centered
      width="420px"
      wrap-class-name="uc-modal-wrap"
    >
      <div class="uc-modal-body">
        <p
          v-if="lastFeedback && !lastFeedback.correct"
          class="uc-modal-wrong"
        >
          正确答案：{{ lastFeedback.correctAnswer }}（{{ correctOptionText }}）
        </p>
        <p
          v-if="lastFeedback && !lastFeedback.correct && lastFeedback.explanation"
          class="uc-modal-exp"
        >
          解析：{{ lastFeedback.explanation }}
        </p>
        <p
          v-if="lastFeedback && !lastFeedback.correct && lastFeedback.catagory"
          class="uc-modal-cat"
        >
          所属板块：{{ lastFeedback.catagory }}
        </p>
        <button
          type="button"
          class="uc-btn-outline uc-modal-next"
          @click="onModalNext"
        >
          {{ isLastQuestion ? '查看结果' : '下一题' }}
        </button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getQuizQuestions, submitQuizAnswer, submitUltimatePerfect } from '@/api/QuizApi'
import { useUserStore } from '@/store/user'

const WIN_SOUND = '/shunting/win.mp3'

const LETTERS = ['A', 'B', 'C', 'D']
const EXPECTED = 10

const router = useRouter()
const userStore = useUserStore()

const phase = ref('intro')
const loading = ref(false)
const submitting = ref(false)

const questions = ref([])
const currentIndex = ref(0)
const selectedLetter = ref(null)

const sessionCorrect = ref(0)
const sessionWrong = ref(0)

const modalOpen = ref(false)
const modalTitle = ref('')
const lastFeedback = ref(null)

const current = computed(() => questions.value[currentIndex.value] || null)

const optionEntries = computed(() => {
  const q = current.value
  if (!q) return ['', '', '', '']
  return [q.optionA, q.optionB, q.optionC, q.optionD]
})

const answeredTotal = computed(() => sessionCorrect.value + sessionWrong.value)

const isLastQuestion = computed(() => {
  const n = questions.value.length || EXPECTED
  return currentIndex.value >= n - 1
})

const sessionRate = computed(() => {
  const t = answeredTotal.value
  if (t <= 0) return 0
  return Math.round((sessionCorrect.value / t) * 100)
})

const sessionRateText = computed(() => sessionRate.value)

const correctOptionText = computed(() => {
  const q = current.value
  const fb = lastFeedback.value
  if (!q || !fb?.correctAnswer) return ''
  const i = LETTERS.indexOf(fb.correctAnswer)
  if (i < 0) return ''
  return [q.optionA, q.optionB, q.optionC, q.optionD][i] || ''
})

function playWinSound() {
  try {
    const a = new Audio(WIN_SOUND)
    a.currentTime = 0
    void a.play()
  } catch {
    // 静音失败时忽略
  }
}

async function onStart() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再参加终极考验')
    router.push('/auth/login')
    return
  }
  loading.value = true
  sessionCorrect.value = 0
  sessionWrong.value = 0
  currentIndex.value = 0
  selectedLetter.value = null
  try {
    const data = await getQuizQuestions({ count: EXPECTED }, { showDefaultMsg: false })
    const list = Array.isArray(data) ? data : []
    if (list.length < EXPECTED) {
      message.warning(`题库不足${EXPECTED}道，当前返回 ${list.length} 道，将按实际题数答题`)
    }
    if (list.length === 0) {
      message.error('暂无题目，请稍后再试')
      return
    }
    questions.value = list
    phase.value = 'quiz'
  } catch (e) {
    message.error(e?.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!selectedLetter.value || !current.value) return
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  submitting.value = true
  try {
    const res = await submitQuizAnswer(
      {
        questionId: current.value.id,
        userAnswer: selectedLetter.value
      },
      { showDefaultMsg: false }
    )
    playWinSound()
    lastFeedback.value = res
    if (res?.correct) {
      sessionCorrect.value += 1
      modalTitle.value = '太棒啦回答正确'
    } else {
      sessionWrong.value += 1
      modalTitle.value = '好可惜回答错误'
    }
    modalOpen.value = true
  } catch (e) {
    message.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function onModalNext() {
  modalOpen.value = false
  lastFeedback.value = null
  if (isLastQuestion.value) {
    if (sessionCorrect.value === (questions.value.length || EXPECTED)) {
      submitUltimatePerfect({ showDefaultMsg: false })
        .then((names) => {
          const arr = Array.isArray(names) ? names : []
          for (const n of arr) {
            message.success(`恭喜你达成成绩获得'${n}'`)
          }
        })
        .catch((e) => {
          console.error(e)
          message.warning('满分徽章结算失败')
        })
    }
    phase.value = 'result'
    return
  }
  currentIndex.value += 1
  selectedLetter.value = null
}

function restart() {
  phase.value = 'intro'
  questions.value = []
  currentIndex.value = 0
  selectedLetter.value = null
  sessionCorrect.value = 0
  sessionWrong.value = 0
}
</script>

<style scoped lang="less">
.uc-page {
  min-height: calc(110vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  background: #f6e8d3;
  position: relative;
}

.uc-intro,
.uc-quiz,
.uc-result {
  max-width: 720px;
  margin: 0 auto;
  padding: 48px 20px 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.uc-intro-text {
  font-size: 18px;
  line-height: 1.65;
  color: #111;
  margin: 0 0 40px;
  max-width: 520px;
}

.uc-btn-outline {
  border: 1px solid #c0c4cc;
  background: #fff;
  padding: 12px 36px;
  font-size: 17px;
  cursor: pointer;
  color: #111;
  min-width: 160px;
}

.uc-btn-outline:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.uc-qbox {
  width: 100%;
  min-height: 120px;
  border: 1px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  font-size: 18px;
  line-height: 1.5;
  margin-bottom: 24px;
  box-sizing: border-box;
}

.uc-options {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 28px;
}

.uc-opt {
  width: 100%;
  min-height: 48px;
  border: 1px solid #000;
  background: #fff;
  text-align: center;
  padding: 10px 12px;
  font-size: 15px;
  cursor: pointer;
  line-height: 1.4;
}

.uc-opt.selected {
  background: #f0f5ff;
  border-color: #3b5bdb;
}

.uc-submit {
  align-self: center;
}

.uc-result-box {
  width: 100%;
  max-width: 480px;
  border: 1px solid #000;
  padding: 28px 20px;
  margin-bottom: 28px;
  font-size: 17px;
  line-height: 2;
}

.uc-result-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
}

.uc-award-hint {
  margin-top: 12px;
  font-size: 15px;
  color: #2563eb;
  font-weight: 500;
}

.uc-loading {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
  z-index: 50;
}

.uc-modal-body {
  padding: 8px 0 4px;
  text-align: center;
}

.uc-modal-wrong,
.uc-modal-exp,
.uc-modal-cat {
  text-align: left;
  margin: 0 0 10px;
  line-height: 1.55;
  font-size: 14px;
}

.uc-modal-next {
  margin-top: 16px;
}
</style>

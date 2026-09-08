<template>
  <div class="mistakes-page">
    <div class="header">
      <div class="title">
        我的错题
      </div>
      <a-button @click="reload">
        刷新
      </a-button>
    </div>

    <a-alert
        v-if="items.length === 0 && !loading"
        type="info"
        show-icon
        message="暂无错题，继续加油！"
    />

    <a-spin :spinning="loading">
      <div class="list">
        <a-card
            v-for="q in items"
            :key="q.mistakeId"
            class="card"
        >
          <template #title>
            <div class="card-title">
              <span class="qid">题目 #{{ q.questionId }}</span>
              <span class="time">{{ formatTime(q.createTime) }}</span>
            </div>
          </template>

          <div class="question-title">
            {{ q.title }}
          </div>

          <div class="options">
            <button
                v-for="letter in ['A', 'B', 'C', 'D']"
                :key="`${q.mistakeId}-${letter}`"
                type="button"
                class="option-btn"
                :class="{ selected: answers[q.mistakeId] === letter }"
                @click="pickOption(q.mistakeId, letter)"
            >
              {{ letter }}. {{ optionText(q, letter) }}
            </button>
          </div>

          <div class="actions">
            <a-button
                type="primary"
                :loading="submittingId === q.mistakeId"
                :disabled="!answers[q.mistakeId]"
                @click="submitOne(q)"
            >
              提交答案
            </a-button>
          </div>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getMyMistakes, submitMistakeAnswer } from '@/api/QuizApi'

const loading = ref(false)
const items = ref([])
const answers = reactive({})
const submittingId = ref(null)

function optionText(q, letter) {
  if (letter === 'A') return q.optionA || ''
  if (letter === 'B') return q.optionB || ''
  if (letter === 'C') return q.optionC || ''
  return q.optionD || ''
}

function pickOption(mistakeId, letter) {
  answers[mistakeId] = letter
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return d.toLocaleString()
}

async function reload() {
  loading.value = true
  try {
    const data = await getMyMistakes(null, { showDefaultMsg: false })
    items.value = Array.isArray(data) ? data : []
    for (const it of items.value) {
      if (!(it.mistakeId in answers)) answers[it.mistakeId] = null
    }
  } catch (e) {
    message.warning(e?.message || e?.msg || '加载错题失败')
  } finally {
    loading.value = false
  }
}

async function submitOne(q) {
  const ans = answers[q.mistakeId]
  if (!ans) return
  submittingId.value = q.mistakeId
  try {
    const res = await submitMistakeAnswer(
        { mistakeId: q.mistakeId, questionId: q.questionId, userAnswer: ans },
        { showDefaultMsg: false }
    )
    const correct = !!res?.correct
    if (correct) {
      message.success('答对了，已从错题本移除')
      items.value = items.value.filter((x) => x.mistakeId !== q.mistakeId)
      delete answers[q.mistakeId]
    } else {
      message.warning('答错了，再试一次吧')
    }
  } catch (e) {
    message.warning(e?.message || e?.msg || '提交失败')
  } finally {
    submittingId.value = null
  }
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.mistakes-page {
  min-height: calc(100vh - 64px);
  padding: 20px 24px;
  box-sizing: border-box;
  background: #f6e8d3;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title {
  font-size: 30px;
  font-weight: 600;
}

.list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.card :deep(.ant-card-head-title) {
  width: 100%;
}

.card-title {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.qid {
  font-weight: 600;
}

.time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.question-title {
  font-size: 16px;
  margin-bottom: 10px;
}

.options {
  display: grid;
  gap: 8px;
}

.option-btn {
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  background: #ffffff;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-btn:hover {
  border-color: #69b1ff;
  background: #f0f7ff;
}

.option-btn.selected {
  border-color: #1677ff;
  background: #e6f4ff;
  color: #0b3fa8;
  font-weight: 600;
}

.actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>


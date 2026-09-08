<template>
  <div class="robot-fab-entry">
    <a-button type="default" class="robot-fab" aria-label="打开小铜人文化助手" @click="visible = true">
      <i class="fab-icon fa-solid fa-robot" aria-hidden="true"></i>
      <span class="fab-label">文化助手</span>
    </a-button>

    <a-modal
      v-model:open="visible"
      title="小铜人文化助手"
      width="520px"
      :footer="null"
      :mask-closable="false"
    >
      <div class="robot-dialog">
        <div class="robot-bubble">
          我可以陪你认识中医文化、经络名称和身体区域。身体不舒服时，要告诉家长或老师哦。
        </div>

        <div class="quick-questions" aria-label="推荐问题">
          <button v-for="question in quickQuestions" :key="question" type="button" @click="askQuickQuestion(question)">
            {{ question }}
          </button>
        </div>

        <div class="chat-window">
          <div class="messages">
            <div
              v-for="(messageItem, index) in messages"
              :key="index"
              class="msg-row"
              :class="messageItem.role"
            >
              <div class="msg-bubble">{{ messageItem.content }}</div>
            </div>
          </div>
        </div>

        <a-textarea
          v-model:value="input"
          :rows="3"
          class="input-area"
          placeholder="例如：经络是什么？"
          aria-label="输入中医文化问题"
          :maxlength="300"
          show-count
        />

        <div class="actions">
          <a-button type="primary" :loading="loading" @click="send">
            发送
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { aiChat } from '@/api'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const quickQuestions = ['经络是什么？', '小铜人为什么是铜色的？', '学习穴位要注意什么？']

function askQuickQuestion(question) {
  input.value = question
  send()
}

async function send() {
  const text = input.value.trim()
  if (!text) {
    message.warning('请输入你的问题')
    return
  }
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  try {
    const reply = await aiChat({ prompt: text }, { showDefaultMsg: false })
    messages.value.push({ role: 'assistant', content: reply || '没有获得有效回复。' })
  } catch (error) {
    message.error(error?.message || '机器人助手暂时不可用')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.robot-fab-entry {
  position: fixed;
  top: 202px;
  right: 20px;
  z-index: 1000;
}

.robot-fab {
  height: 40px;
  padding: 0 18px;
  border-radius: 20px;
}

.fab-icon {
  margin-right: 6px;
}

@media (max-width: 760px) {
  .robot-fab-entry {
    top: 176px;
    right: 10px;
  }

  .robot-fab {
    width: 44px;
    height: 44px;
    padding: 0;
  }

  .fab-icon { margin-right: 0; }
  .fab-label { display: none; }
}

.robot-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.robot-bubble {
  align-self: flex-start;
  margin-bottom: 4px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f5f5f5;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-questions button {
  min-height: 34px;
  padding: 6px 12px;
  border: 1px solid #c7a76a;
  border-radius: 999px;
  color: #614019;
  background: #fffaf0;
  cursor: pointer;
}

.quick-questions button:focus-visible {
  outline: 3px solid rgba(180, 117, 35, 0.35);
  outline-offset: 2px;
}

.chat-window {
  min-height: 140px;
  max-height: 260px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  background: #fafafa;
  padding: 8px;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg-row {
  display: flex;
}

.msg-row.user {
  justify-content: flex-end;
}

.msg-row.assistant {
  justify-content: flex-start;
}

.msg-bubble {
  max-width: 80%;
  padding: 6px 10px;
  border-radius: 8px;
  background: #e0f2fe;
}

.msg-row.user .msg-bubble {
  background: #bfdbfe;
}

.input-area {
  margin-top: 4px;
}

.actions {
  margin-top: 8px;
  text-align: right;
}
</style>

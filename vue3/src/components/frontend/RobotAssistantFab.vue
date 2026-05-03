<template>
  <div class="robot-fab-entry">
    <a-button
      type="default"
      class="robot-fab"
      @click="visible = true"
    >
      <span
        class="fab-icon"
        aria-hidden="true"
      >🤖</span>
      机器人助手
    </a-button>

    <a-modal
      v-model:open="visible"
      title="机器人助手"
      width="520px"
      :footer="null"
      :mask-closable="false"
    >
      <div class="robot-dialog">
        <div class="robot-bubble">
          你好，我是机器人助手，有问题都可以问我哦～
        </div>

        <div class="chat-window">
          <div class="messages">
            <div
              v-for="(m, idx) in messages"
              :key="idx"
              class="msg-row"
              :class="m.role"
            >
              <div class="msg-bubble">
                {{ m.content }}
              </div>
            </div>
          </div>
        </div>

        <a-textarea
          v-model:value="input"
          :rows="3"
          class="input-area"
          placeholder="输入你的问题"
        />

        <div class="actions">
          <a-button
            type="primary"
            :loading="loading"
            @click="send"
          >
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
    messages.value.push({ role: 'assistant', content: reply || '（没有获得有效回复）' })
  } catch (e) {
    message.error(e?.message || '机器人助手暂时不可用')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.robot-fab-entry {
  position: fixed;
  top: 170px;
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

.robot-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.robot-bubble {
  align-self: flex-start;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-bottom: 4px;
}

.chat-window {
  min-height: 140px;
  max-height: 260px;
  border: 1px solid #e5e7eb;
  background: #fafafa;
  padding: 8px;
  overflow-y: auto;
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
  border-radius: 6px;
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


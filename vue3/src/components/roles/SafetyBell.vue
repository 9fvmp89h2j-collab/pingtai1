<template>
  <div class="safety-bell">
    <a-button
      class="safety-bell__button"
      type="primary"
      shape="circle"
      aria-label="打开安全提醒"
      @click="openModal"
    >
      <img class="safety-bell__sprite" :src="safetyBellSprite" alt="">
    </a-button>

    <div class="safety-bell__tip">安全提醒</div>

    <a-modal
      v-model:open="visible"
      title="安全铃铛提醒"
      width="520px"
      :footer="null"
      @cancel="markRead"
    >
      <div class="safety-bell__content">
        <img class="safety-bell__avatar" :src="safetyBellSprite" alt="安全铃铛精灵">

        <div class="safety-bell__message">
          <p class="safety-bell__lead">{{ safetyDialogue.title }}</p>
          <p>{{ safetyDialogue.content }}</p>
        </div>

        <ul class="safety-bell__rules">
          <li v-for="rule in safetyDialogue.rules" :key="rule">{{ rule }}</li>
        </ul>

        <div class="safety-bell__actions">
          <a-button type="primary" @click="confirmRead">
            我知道了
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import dialogueConfig from '@/data/dialogue_config.json'
import { generatedRewardAssets } from '@/data/generatedRewardAssets'

const safetyBellSprite = generatedRewardAssets.detectiveSafety

const STORAGE_KEY = 'pediatric-acupuncture-safety-bell-read'

const props = defineProps({
  autoOpen: {
    type: Boolean,
    default: true
  }
})

const visible = ref(false)

const fallbackDialogue = {
  title: '学习穴位可以，但不能自己尝试针刺。',
  content: '本系统只用于针灸文化科普和身体认知学习，不提供实际针刺、艾灸或治疗指导。',
  rules: [
    '不能自己拿针或尖锐物品尝试针刺。',
    '身体不舒服时，要告诉家长并咨询医生。',
    '本系统不能替代医生诊断和治疗。'
  ]
}

const safetyDialogue = computed(() => {
  const config = dialogueConfig.safetyBell?.default
  if (!config || typeof config !== 'object') return fallbackDialogue
  return {
    title: config.title || fallbackDialogue.title,
    content: config.content || fallbackDialogue.content,
    rules: Array.isArray(config.rules) && config.rules.length ? config.rules : fallbackDialogue.rules
  }
})

onMounted(() => {
  if (!props.autoOpen) return
  if (localStorage.getItem(STORAGE_KEY) !== '1') {
    window.setTimeout(() => {
      visible.value = true
    }, 800)
  }
})

function openModal() {
  visible.value = true
}

function markRead() {
  localStorage.setItem(STORAGE_KEY, '1')
}

function confirmRead() {
  markRead()
  visible.value = false
}
</script>

<style scoped>
.safety-bell {
  position: fixed;
  right: 20px;
  bottom: 26px;
  z-index: 1100;
  display: flex;
  align-items: center;
  gap: 8px;
}

.safety-bell__button {
  width: 62px;
  height: 62px;
  overflow: hidden;
  border: 3px solid #e7b75a;
  background: #fff7df;
  box-shadow: 0 8px 20px rgba(94, 61, 18, 0.22);
}

.safety-bell__sprite {
  width: 58px;
  height: 58px;
  object-fit: contain;
}

.safety-bell__tip {
  padding: 8px 12px;
  border: 1px solid rgba(176, 111, 35, 0.38);
  border-radius: 999px;
  background: rgba(255, 249, 226, 0.94);
  color: #8a4f19;
  font-size: 14px;
  font-weight: 900;
  box-shadow: 0 6px 16px rgba(94, 61, 18, 0.12);
}

.safety-bell__content {
  display: grid;
  gap: 16px;
}

.safety-bell__avatar {
  width: 96px;
  height: 96px;
  margin: 0 auto;
  object-fit: contain;
}

.safety-bell__message {
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff8df;
  color: #5a3514;
  line-height: 1.7;
}

.safety-bell__lead {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 950;
}

.safety-bell__message p:last-child {
  margin: 0;
}

.safety-bell__rules {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.safety-bell__rules li {
  padding: 10px 12px;
  border-radius: 12px;
  background: #f3fbf6;
  color: #2f6f55;
  font-weight: 800;
}

.safety-bell__actions {
  text-align: center;
}
</style>

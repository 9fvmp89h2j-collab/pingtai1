<template>
  <div class="safety-bell">
    <a-button
      class="safety-bell__button"
      type="primary"
      shape="circle"
      aria-label="打开安全提醒"
      @click="openModal"
    >
      <img
        class="safety-bell__sprite"
        :src="safetyBellSprite"
        alt=""
      >
    </a-button>

    <div class="safety-bell__tip">
      安全提醒
    </div>

    <a-modal
      v-model:open="visible"
      title="安全铃铛提醒"
      width="520px"
      :footer="null"
      @cancel="markRead"
    >
      <div class="safety-bell__content">
        <img
          class="safety-bell__avatar"
          :src="safetyBellSprite"
          alt="安全铃铛精灵"
        >

        <div class="safety-bell__message">
          <p class="safety-bell__lead">
            {{ safetyDialogue.title }}
          </p>
          <p>{{ safetyDialogue.content }}</p>
        </div>

        <ul class="safety-bell__rules">
          <li
            v-for="rule in safetyDialogue.rules"
            :key="rule"
          >
            {{ rule }}
          </li>
        </ul>

        <div class="safety-bell__actions">
          <a-button
            type="primary"
            @click="confirmRead"
          >
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
import safetyBellSprite from '@/assets/safety-bell-sprite-actions.gif'

const STORAGE_KEY = 'pediatric-acupuncture-safety-bell-read'

const visible = ref(false)

const safetyDialogue = computed(() => {
  return dialogueConfig.safetyBell?.default || {
    title: '学习穴位可以，但不能自己尝试针刺。',
    content: '本系统只用于针灸文化科普和穴位认知，不提供实际针刺、艾灸或治疗指导。',
    rules: [
      '不能自己拿针或尖锐物品尝试针刺。',
      '身体不舒服时，要告诉家长并咨询医生。',
      '本系统不能替代医生诊断和治疗。'
    ]
  }
})

onMounted(() => {
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
  padding: 4px;
  background: #fff7e6;
  border-color: #ffd591;
  box-shadow: 0 10px 24px rgba(120, 74, 0, 0.28);
  animation: bell-shake 2.8s ease-in-out infinite;
}

.safety-bell__button:hover,
.safety-bell__button:focus {
  background: #fff1c2;
  border-color: #d79c28;
}

.safety-bell__sprite {
  width: 50px;
  height: 50px;
  object-fit: contain;
}

.safety-bell__tip {
  padding: 6px 10px;
  color: #613400;
  font-size: 13px;
  line-height: 1;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(120, 74, 0, 0.14);
}

.safety-bell__content {
  display: grid;
  grid-template-columns: 58px 1fr;
  gap: 14px;
}

.safety-bell__avatar {
  width: 56px;
  height: 56px;
  object-fit: contain;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 50%;
  box-shadow: 0 8px 18px rgba(120, 74, 0, 0.14);
}

.safety-bell__message {
  color: #3f2a11;
}

.safety-bell__message p {
  margin: 0 0 8px;
}

.safety-bell__lead {
  font-weight: 700;
}

.safety-bell__rules {
  grid-column: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 2px 0 0;
  padding: 10px 12px;
  color: #4a3821;
  background: #fffaf0;
  border: 1px solid #ffe7ba;
  border-radius: 8px;
}

.safety-bell__rules li {
  position: relative;
  padding-left: 16px;
}

.safety-bell__rules li::before {
  position: absolute;
  top: 0;
  left: 0;
  color: #d48806;
  content: "!";
  font-weight: 700;
}

.safety-bell__actions {
  grid-column: 1 / -1;
  margin-top: 6px;
  text-align: right;
}

@keyframes bell-shake {
  0%,
  88%,
  100% {
    transform: rotate(0deg);
  }

  90% {
    transform: rotate(7deg);
  }

  94% {
    transform: rotate(-7deg);
  }

  98% {
    transform: rotate(4deg);
  }
}

@media (max-width: 640px) {
  .safety-bell {
    right: 14px;
    bottom: 18px;
  }

  .safety-bell__tip {
    display: none;
  }
}
</style>

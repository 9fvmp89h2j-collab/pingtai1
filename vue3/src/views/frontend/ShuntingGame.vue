<template>
  <div class="shunting-page">
    <div class="shunting-frame-wrap">
      <iframe
        ref="gameFrame"
        class="shunting-iframe"
        title="调车小游戏 Inglenook Shunting"
        :src="iframeSrc"
        referrerpolicy="same-origin"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const gameFrame = ref(null)

const SHUNTING_WIN_MSG = 'beaver-shunting-win'
const REWARD_SCORE = 10
const REWARD_DEBOUNCE_MS = 2500

let lastRewardAt = 0

const iframeSrc = computed(() => {
  const base = import.meta.env.BASE_URL || '/'
  const normalized = base.endsWith('/') ? base : `${base}/`
  return `${normalized}shunting/index.html?v=20260407`
})

async function onShuntingWin() {
  const now = Date.now()
  if (now - lastRewardAt < REWARD_DEBOUNCE_MS) return
  lastRewardAt = now

  if (!userStore.isLoggedIn || !userStore.userId) {
    message.info('登录后通关可自动获得 10 点气血能量')
    return
  }

  try {
    await userStore.claimShuntingReward({ showDefaultMsg: false })
    message.success(`恭喜通关！气血能量 +${REWARD_SCORE}`)
  } catch (e) {
    console.error(e)
    const errMsg = e?.message || e?.msg || '积分发放失败，请稍后重试'
    message.error(errMsg)
  }
}

function onWindowMessage(ev) {
  if (ev.data?.type !== SHUNTING_WIN_MSG) return
  const frame = gameFrame.value
  if (!frame?.contentWindow) return
  if (ev.source !== frame.contentWindow) return
  if (ev.origin !== window.location.origin) return
  onShuntingWin()
}

onMounted(() => {
  window.addEventListener('message', onWindowMessage)
})

onUnmounted(() => {
  window.removeEventListener('message', onWindowMessage)
})
</script>

<style scoped>
.shunting-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  box-sizing: border-box;
  background: #f6e8d3;
}

.shunting-frame-wrap {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 24px;
  box-sizing: border-box;
}

.shunting-iframe {
  display: block;
  width: 100%;
  height: calc(100vh - 96px);
  min-height: 760px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

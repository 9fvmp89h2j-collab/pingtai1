<template>
  <a-modal
    :open="open"
    title="获得奖励"
    width="420px"
    :footer="null"
    @cancel="$emit('update:open', false)"
  >
    <div class="reward-popup">
      <div
        v-for="item in rewards"
        :key="item.name"
        class="reward-popup__item"
      >
        <span class="reward-popup__icon">{{ item.icon || '星' }}</span>
        <span>{{ item.name }}</span>
        <strong>x{{ item.count }}</strong>
      </div>

      <a-button
        type="primary"
        block
        @click="$emit('update:open', false)"
      >
        收下奖励
      </a-button>
    </div>
  </a-modal>
</template>

<script setup>
defineEmits(['update:open'])

defineProps({
  open: {
    type: Boolean,
    default: false
  },
  rewards: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.reward-popup {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reward-popup__item {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fffaf0;
  border: 1px solid #ffe7ba;
  border-radius: 8px;
}

.reward-popup__icon {
  display: flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  color: #ad6800;
  background: #fff7e6;
  border-radius: 50%;
}
</style>

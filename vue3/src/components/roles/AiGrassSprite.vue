<template>
  <div class="ai-grass-sprite">
    <div
      class="ai-grass-sprite__sprite"
      aria-hidden="true"
    >
      艾
    </div>
    <div class="ai-grass-sprite__bubble">
      {{ message || role.defaultLine }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import roleConfig from '@/data/role_config.json'

defineProps({
  message: {
    type: String,
    default: ''
  }
})

const role = computed(() => roleConfig.roles?.aiGrassSprite || {})
</script>

<style scoped>
.ai-grass-sprite {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-grass-sprite__sprite {
  display: flex;
  width: 50px;
  height: 50px;
  align-items: center;
  justify-content: center;
  color: #174a2a;
  font-weight: 700;
  background: linear-gradient(150deg, #b7eb8f, #52c41a);
  border: 1px solid #95de64;
  border-radius: 50%;
  animation: grass-float 2.6s ease-in-out infinite;
}

.ai-grass-sprite__bubble {
  max-width: 260px;
  padding: 8px 10px;
  color: #254117;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
}

@keyframes grass-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}
</style>

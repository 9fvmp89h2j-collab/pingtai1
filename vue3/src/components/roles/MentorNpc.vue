<template>
  <div class="mentor-npc">
    <div
      class="mentor-npc__portrait"
      :data-expression="expression"
    >
      杏
    </div>
    <div class="mentor-npc__dialogue">
      <strong>{{ role.name }}</strong>
      <p>{{ message || role.defaultLine }}</p>
      <a-button
        v-if="task"
        type="primary"
        size="small"
        @click="$emit('accept', task)"
      >
        接受任务
      </a-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import roleConfig from '@/data/role_config.json'

defineEmits(['accept'])

defineProps({
  message: {
    type: String,
    default: ''
  },
  expression: {
    type: String,
    default: 'smile'
  },
  task: {
    type: Object,
    default: null
  }
})

const role = computed(() => roleConfig.roles?.mentorNpc || {})
</script>

<style scoped>
.mentor-npc {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.mentor-npc__portrait {
  display: flex;
  width: 72px;
  height: 96px;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(180deg, #8f6b3d, #5c3b1e);
  border-radius: 36px 36px 12px 12px;
}

.mentor-npc__dialogue {
  max-width: 360px;
  padding: 12px;
  color: #3f2a11;
  background: #fffaf0;
  border: 1px solid #ffe7ba;
  border-radius: 8px;
}

.mentor-npc__dialogue p {
  margin: 4px 0 10px;
}
</style>

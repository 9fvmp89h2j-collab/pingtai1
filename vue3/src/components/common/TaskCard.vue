<template>
  <article class="task-card" :class="{ 'task-card--locked': task.locked }">
    <div class="task-card__header">
      <h3>{{ task.title }}</h3>
      <a-tag :color="task.locked ? 'default' : 'gold'">
        {{ task.locked ? '未解锁' : task.type }}
      </a-tag>
    </div>
    <p>{{ task.description }}</p>
    <div v-if="task.rewards?.length" class="task-card__rewards">
      <span
        v-for="reward in task.rewards"
        :key="reward.name"
      >
        {{ reward.name }} x{{ reward.count }}
      </span>
    </div>
    <a-button
      type="primary"
      :disabled="task.locked"
      @click="$emit('start', task)"
    >
      开始任务
    </a-button>
  </article>
</template>

<script setup>
defineEmits(['start'])

defineProps({
  task: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.task-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background: #fff;
  border: 1px solid #eee3cc;
  border-radius: 8px;
}

.task-card--locked {
  opacity: 0.62;
}

.task-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.task-card__header h3 {
  margin: 0;
  color: #3f2a11;
  font-size: 16px;
}

.task-card p {
  margin: 0;
  color: #5f4b2e;
}

.task-card__rewards {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: #8c6d1f;
  font-size: 13px;
}
</style>

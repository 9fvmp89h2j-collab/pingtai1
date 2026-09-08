<template>
  <aside class="mission-panel" :aria-label="labels.panel">
    <span class="panel-kicker">{{ labels.compass }}</span>
    <h2>{{ labels.title }}</h2>
    <ol class="mission-list">
      <li v-for="task in tasks" :key="task.title">
        <span class="task-icon">{{ task.icon }}</span>
        <div>
          <strong>{{ task.title }}</strong>
          <em>{{ task.detail }}</em>
          <small>{{ task.reward }}</small>
        </div>
        <a :href="task.route">{{ labels.go }}</a>
      </li>
    </ol>
    <div class="next-task">
      <span>{{ labels.next }}</span>
      <strong>{{ nextTask.label }}</strong>
    </div>
    <p class="refresh-time">{{ labels.refresh }}</p>
  </aside>
</template>

<script setup>
const labels = { panel: '\u4eca\u65e5\u4efb\u52a1', compass: '\u661f\u56fe\u7f57\u76d8', title: '\u4eca\u65e5\u4efb\u52a1', next: '\u4e0b\u4e00\u7ad9', go: '\u524d\u5f80', refresh: '\u5237\u65b0\u65f6\u95f4\uff1a\u6bcf\u65e5 05:00' }
defineProps({ tasks: { type: Array, required: true }, nextTask: { type: Object, required: true } })
</script>

<template>
  <button
    type="button"
    class="level-node"
    :class="[level.id, level.status, { active }]"
    :style="{ left: `${level.x}%`, top: `${level.y}%` }"
    @click="$emit('enter-level', level)"
  >
    <span class="node-ring" aria-hidden="true"></span>
    <span class="node-badge" aria-hidden="true"><i>{{ level.icon }}</i></span>
    <span class="state-seal" aria-hidden="true">{{ level.seal }}</span>
    <strong class="wood-label">{{ level.label }}</strong>
    <em>{{ level.statusText }}</em>
  </button>
</template>

<script setup>
defineProps({
  level: { type: Object, required: true },
  active: { type: Boolean, default: false }
})

defineEmits(['enter-level'])
</script>

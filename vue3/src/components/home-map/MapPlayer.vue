<template>
  <div class="map-player" :class="{ walking }" :style="{ left: `${x}px`, top: `${y}px`, backgroundPosition: spritePosition }" :aria-label="label">
    <span class="player-shadow"></span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ x: { type: Number, required: true }, y: { type: Number, required: true }, direction: { type: String, required: true }, frame: { type: Number, required: true }, walking: { type: Boolean, default: false } })
const label = '\u4e2d\u533b\u5c0f\u4fa6\u63a2'
const directionRows = { down: 0, left: 1, right: 2, up: 3 }
const spritePosition = computed(() => {
  const col = props.walking ? props.frame : 0
  const row = directionRows[props.direction] ?? 0
  return `-${col * 76}px -${row * 76}px`
})
</script>


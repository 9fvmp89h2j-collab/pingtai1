<template>
  <div class="dialogue-box" :class="`dialogue-box--${tone}`">
    <div v-if="speaker" class="dialogue-box__speaker">
      {{ speaker }}
    </div>
    <div class="dialogue-box__content">
      <slot>{{ text }}</slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  speaker: {
    type: String,
    default: ''
  },
  text: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.dialogue-box {
  padding: 12px 14px;
  color: #3f2a11;
  background: #fff;
  border: 1px solid #e8e1d2;
  border-radius: 8px;
}

.dialogue-box--warm {
  background: #fffaf0;
  border-color: #ffe7ba;
}

.dialogue-box--safe {
  background: #fff7e6;
  border-color: #ffd591;
}

.dialogue-box__speaker {
  margin-bottom: 4px;
  font-weight: 700;
}

.dialogue-box__content {
  line-height: 1.7;
}
</style>

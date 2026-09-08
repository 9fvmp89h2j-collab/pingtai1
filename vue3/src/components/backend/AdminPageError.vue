<template>
  <a-alert
    v-if="message"
    class="admin-page-error"
    type="error"
    show-icon
    :message="message"
  >
    <template #action>
      <a-button size="small" :loading="loading" @click="$emit('retry')">
        重新加载
      </a-button>
    </template>
  </a-alert>
</template>

<script setup>
defineProps({
  message: { type: String, default: '' },
  loading: { type: Boolean, default: false }
})

defineEmits(['retry'])
</script>

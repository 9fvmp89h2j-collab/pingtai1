<template>
  <div class="redirect-hint">
    正在跳转…
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  router.replace({ path: '/community', query: { ...route.query } })
})
</script>

<style scoped>
.redirect-hint {
  padding: 48px;
  text-align: center;
  color: #666;
}
</style>

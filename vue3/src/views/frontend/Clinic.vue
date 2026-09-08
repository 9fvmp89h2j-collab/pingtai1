<template>
  <main class="agency-redirect" aria-live="polite">
    正在进入侦探社...
  </main>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace('/agency')
})
</script>

<style scoped>
.agency-redirect {
  min-height: 100vh;
  display: grid;
  place-items: center;
  color: #2d6f61;
  background: #fff8e8;
  font-size: 18px;
  font-weight: 700;
}
</style>

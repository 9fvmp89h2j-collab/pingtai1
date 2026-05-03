<template>
  <div class="mailbox-fab-entry">
    <a-button
      type="primary"
      class="mailbox-fab"
      @click="goCommunity"
    >
      <span
        class="fab-icon"
        aria-hidden="true"
      >💬</span>
      信箱
    </a-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goCommunity() {
  router.push('/community')
}
</script>

<style scoped>
.mailbox-fab-entry {
  position: fixed;
  top: 120px;
  right: 20px;
  z-index: 1000;
}

.mailbox-fab {
  height: 40px;
  padding: 0 18px;
  font-weight: 600;
  border-radius: 20px;
  box-shadow: 0 4px 14px rgba(90, 74, 120, 0.25);
  background: #fff;
  border: 1px solid #d9d9d9;
  color: #3d3558;
}

.mailbox-fab:hover {
  color: #2a2440;
  filter: brightness(1.03);
}

.fab-icon {
  margin-right: 6px;
}
</style>

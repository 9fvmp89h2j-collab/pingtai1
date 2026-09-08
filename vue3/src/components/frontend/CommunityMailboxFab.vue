<template>
  <div class="mailbox-fab-entry">
    <a-button type="primary" class="mailbox-fab" aria-label="打开信箱" @click="goCommunity">
      <i class="fab-icon fa-solid fa-comment" aria-hidden="true"></i>
      <span class="fab-label">信箱</span>
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
  top: 152px;
  right: 20px;
  z-index: 1000;
}

.mailbox-fab {
  height: 40px;
  padding: 0 18px;
  border: 1px solid #d9d9d9;
  border-radius: 20px;
  background: #fff;
  color: #3d3558;
  font-weight: 700;
  box-shadow: 0 4px 14px rgba(90, 74, 120, 0.25);
}

.fab-icon {
  margin-right: 6px;
}

@media (max-width: 760px) {
  .mailbox-fab-entry {
    top: 126px;
    right: 10px;
  }

  .mailbox-fab {
    width: 44px;
    height: 44px;
    padding: 0;
  }

  .fab-icon { margin-right: 0; }
  .fab-label { display: none; }
}
</style>

<template>
  <div class="settings-page">
    <a-card
      class="settings-card"
      title="其他设置"
    >
      <div class="profile-wrap">
        <ProfilePage />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import ProfilePage from '@/views/profile/index.vue'
</script>

<style scoped>
.settings-page {
  padding: 20px 24px;
}

.settings-card {
  max-width: 1100px;
  margin: 0 auto;
}

.profile-wrap :deep(.profile-card) {
  box-shadow: none;
}
</style>


<template>
  <div class="copper-teacher-avatar">
    <img
      class="copper-teacher-avatar__face"
      :src="copperTeacherSprite"
      alt="小铜人老师"
    >
    <div class="copper-teacher-avatar__body">
      <strong>{{ role.name }}</strong>
      <p>{{ message || role.defaultLine }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import roleConfig from '@/data/role_config.json'
import copperTeacherSprite from '@/assets/copper-teacher-actions.gif'

defineProps({
  message: {
    type: String,
    default: ''
  }
})

const role = computed(() => roleConfig.roles?.copperTeacher || {})
</script>

<style scoped>
.copper-teacher-avatar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.copper-teacher-avatar__face {
  width: 54px;
  height: 54px;
  flex: 0 0 auto;
  object-fit: contain;
  background: #fff7e6;
  border-radius: 50%;
  box-shadow: 0 8px 18px rgba(89, 58, 20, 0.2);
}

.copper-teacher-avatar__body {
  min-width: 0;
}

.copper-teacher-avatar__body strong {
  color: #4a3217;
}

.copper-teacher-avatar__body p {
  margin: 2px 0 0;
  color: #604b2a;
}
</style>

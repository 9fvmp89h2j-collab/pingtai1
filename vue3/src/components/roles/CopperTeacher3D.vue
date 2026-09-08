<template>
  <section class="copper-teacher-3d">
    <div class="copper-teacher-3d__stage">
      <div class="copper-teacher-3d__model">
        铜
      </div>
      <div class="copper-teacher-3d__hint">
        3D 小铜人模型区域
      </div>
    </div>

    <div class="copper-teacher-3d__panel">
      <h3>{{ role.name }}</h3>
      <p>{{ role.description }}</p>
      <p class="copper-teacher-3d__note">
        后续接入 GLB 模型、穴位热点和 Raycaster 点击识别。
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import roleConfig from '@/data/role_config.json'

const role = computed(() => roleConfig.roles?.copperTeacher || {})
</script>

<style scoped>
.copper-teacher-3d {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 320px;
  gap: 16px;
  width: 100%;
}

.copper-teacher-3d__stage {
  display: flex;
  min-height: 360px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  background: #172026;
  border-radius: 8px;
}

.copper-teacher-3d__model {
  display: flex;
  width: 132px;
  height: 220px;
  align-items: center;
  justify-content: center;
  color: #fff7e6;
  font-size: 44px;
  font-weight: 700;
  background: linear-gradient(160deg, #b08d57, #6f4f28);
  border: 1px solid rgba(255, 236, 179, 0.36);
  border-radius: 64px 64px 28px 28px;
  box-shadow: 0 0 34px rgba(255, 196, 87, 0.22);
}

.copper-teacher-3d__hint,
.copper-teacher-3d__note {
  color: #8c6d3f;
  font-size: 13px;
}

.copper-teacher-3d__panel {
  padding: 16px;
  background: #fff;
  border: 1px solid #f0e2c1;
  border-radius: 8px;
}

.copper-teacher-3d__panel h3 {
  margin: 0 0 8px;
  color: #4a3217;
}

.copper-teacher-3d__panel p {
  margin: 0 0 8px;
}

@media (max-width: 768px) {
  .copper-teacher-3d {
    grid-template-columns: 1fr;
  }
}
</style>

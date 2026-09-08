<template>
  <a-layout class="frontend-layout">
    <!-- 顶部导航栏 -->
    <Navbar />

    <!-- 悄悄话信箱 -->
    <CommunityMailboxFab v-if="!isHomeMap && !isBodyMap" />

    <!-- 机器人助手（信箱下方常驻入口） -->
    <RobotAssistantFab v-if="!isHomeMap && !isBodyMap" />
    <SafetyBell v-if="!isHomeMap && !isBodyMap" :auto-open="!isMeridian" />

    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </a-layout-content>
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/frontend/Navbar.vue'
import CommunityMailboxFab from '@/components/frontend/CommunityMailboxFab.vue'
import RobotAssistantFab from '@/components/frontend/RobotAssistantFab.vue'
import SafetyBell from '@/components/roles/SafetyBell.vue'

const route = useRoute()
const isHomeMap = computed(() => ['HomeMapLanding', 'AgencyHome'].includes(route.name) || ['/home-map', '/agency'].includes(route.path))
const isMeridian = computed(() => route.name === 'Jingluo' || route.path === '/jingluo')
const isBodyMap = computed(() => route.name === 'BodyMap' || route.path === '/body-map')
</script>

<style scoped>
.frontend-layout {
  min-height: 100vh;
  background-color: var(--site-background, #fff8e8);
}

.main-content {
  margin-top: var(--site-nav-height);
  min-height: calc(100vh - var(--site-nav-height));
}

.content-wrapper {
  width: 100%;
  margin: 0 auto;
  padding: 0;
}

.footer {
  background-color: #001529;
  color: rgba(255, 255, 255, 0.65);
  text-align: center;
  padding: 24px 50px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-content p {
  margin: 8px 0;
}


/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 0;
  }

  .footer {
    padding: 24px 16px;
  }


}
</style>

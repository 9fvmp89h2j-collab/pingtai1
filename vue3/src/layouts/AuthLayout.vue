<template>
  <div class="auth-layout">
    <div class="auth-wrapper">
      <div class="auth-container">
        <div class="auth-header">
          <div class="logo-wrapper">
            <h1 class="site-title">
              {{ siteName }}
            </h1>
          </div>
        </div>

        <div class="auth-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import siteConfig from '@/config/site'

// 使用统一的站点配置
const siteName = siteConfig.name
</script>

<style scoped>
/* 引入思源字体 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;700&family=Noto+Sans+SC:wght@400;500&display=swap');

.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFF;
  position: relative;
  overflow: hidden;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
}

.auth-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.auth-container {
  max-width: 460px;
  width: 100%;
  margin: 0 auto;
  padding: 3rem 2.5rem;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 0;
  box-shadow: 
    0 8px 32px rgba(44, 44, 44, 0.08),
    0 2px 8px rgba(44, 44, 44, 0.04);
  position: relative;
  z-index: 1;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 0.8rem;
}



.site-title {
  margin: 0;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: #2C2C2C;
  letter-spacing: 0.3rem;
  position: relative;
}

.auth-content {
  margin-bottom: 2rem;
  animation: slideUp 0.5s ease-out 0.2s both;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .auth-container {
    max-width: 90%;
    padding: 2rem 1.5rem;
  }

  .site-title {
    font-size: 1.5rem;
    letter-spacing: 0.2rem;
  }
}
</style>

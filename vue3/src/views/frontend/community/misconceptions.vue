<template>
  <div class="misconceptions-page">
    <!-- 背景图片 -->
    <div class="background-image" />

    <!-- 装饰元素 -->
    <div class="decoration decoration-1">
      🌲
    </div>
    <div class="decoration decoration-2">
      🍄
    </div>
    <div class="decoration decoration-3">
      🌿
    </div>

    <div class="page-content">
      <div class="page-head">
        <h1 class="page-title">
          <span class="title-icon">🌲</span>
          错题森林
        </h1>
      </div>
      
      <p class="page-desc">
        点击问题探索答案，避开误区吧！
      </p>

      <a-spin :spinning="loading">
        <a-collapse
          v-model:active-key="activeKeys"
          accordion
          class="custom-collapse"
        >
          <a-collapse-panel
            v-for="item in list"
            :key="item.id"
            :header="item.question"
            class="collapse-panel"
          >
            <div class="answer-content">
              {{ item.answer || '暂无答案' }}
            </div>
          </a-collapse-panel>
        </a-collapse>
        <a-empty
          v-if="!loading && list.length === 0"
          description="森林里还没有问题哦！"
        />
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMisconceptionList } from '@/api/CommunityApi'

const list = ref([])
const loading = ref(false)
const activeKeys = ref([])

onMounted(() => {
  loading.value = true
  getMisconceptionList({
    onSuccess: (res) => {
      list.value = res || []
      loading.value = false
    },
    onError: () => { loading.value = false }
  })
})
</script>

<style scoped lang="less">
.misconceptions-page {
  min-height: 100vh;
  background: #E8F5E9;
  padding-bottom: 48px;
  position: relative;
  overflow-x: hidden;
}

/* 背景图片 */
.background-image {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/home_back.jpeg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  opacity: 0.3;
  z-index: 0;
  pointer-events: none;
}

/* 装饰元素 */
.decoration {
  position: fixed;
  font-size: 48px;
  opacity: 0.6;
  pointer-events: none;
  z-index: 0;
  animation: float 6s ease-in-out infinite;
}

.decoration-1 {
  top: 15%;
  left: 5%;
  animation-delay: 0s;
}

.decoration-2 {
  top: 40%;
  right: 8%;
  animation-delay: 1s;
}

.decoration-3 {
  bottom: 30%;
  left: 10%;
  animation-delay: 2s;
}

/* 页面内容 */
.page-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 100px 24px 24px;
  position: relative;
  z-index: 1;
}

.page-head {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInDown 0.6s ease-out;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  color: #2E7D32;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 36px;
  animation: bounce 2s infinite;
}

.page-desc {
  text-align: center;
  color: #666;
  font-size: 16px;
  margin-bottom: 24px;
  font-weight: 500;
}

/* 手风琴 */
.custom-collapse {
  background: transparent;
  border: none;
}

:deep(.custom-collapse .ant-collapse-item) {
  margin-bottom: 16px;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 2px solid #C8E6C9;
  transition: all 0.3s ease;
}

:deep(.custom-collapse .ant-collapse-item:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #81C784;
}

:deep(.custom-collapse .ant-collapse-header) {
  font-size: 17px;
  font-weight: 600;
  color: #2E7D32;
  padding: 16px 20px;
  background: #C8E6C9;
  border-radius: 14px 14px 0 0;
  position: relative;
}

:deep(.custom-collapse .ant-collapse-header::before) {
  content: '+';
  position: absolute;
  right: 20px;
  font-size: 24px;
  font-weight: 700;
  color: #2E7D32;
  transition: all 0.3s ease;
}

:deep(.custom-collapse .ant-collapse-item-active .ant-collapse-header::before) {
  content: '-';
}

:deep(.custom-collapse .ant-collapse-expand-icon) {
  display: none;
}

:deep(.custom-collapse .ant-collapse-content) {
  background: #fff;
  border-radius: 0 0 14px 14px;
  border-top: 2px solid #C8E6C9;
}

:deep(.custom-collapse .ant-collapse-content-box) {
  padding: 20px;
}

.answer-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #555;
  font-size: 15px;
}

/* 动画 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .page-content {
    padding: 80px 16px 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-desc {
    font-size: 14px;
  }

  :deep(.custom-collapse .ant-collapse-header) {
    font-size: 15px;
    padding: 14px 16px;
  }

  .decoration {
    font-size: 32px;
  }
}
</style>

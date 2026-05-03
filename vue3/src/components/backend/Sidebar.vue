<template>
  <div
    class="sidebar-container"
    :class="{ 'is-collapsed': isCollapsed }"
  >
    <div class="logo">
      <img
        :src="siteConfig.admin.logo.icon"
        alt="Logo"
        class="logo-icon"
      >
      <span
        v-show="!isCollapsed"
        class="logo-text"
      >{{ siteConfig.admin.logo.text }}</span>
    </div>
    <div class="menu-wrapper">
      <a-menu 
        v-model:selected-keys="selectedKeys" 
        :inline-collapsed="isCollapsed" 
        mode="inline" 
        class="sidebar-menu"
      >
        <!-- 固定菜单项 -->
        <a-menu-item
          key="/back/dashboard"
          @click="handleMenuClick('/back/dashboard')"
        >
          <template #icon>
            <i class="fas fa-house" />
          </template>
          <span>首页</span>
        </a-menu-item>
        
        <a-menu-item
          key="/back/user"
          @click="handleMenuClick('/back/user')"
        >
          <template #icon>
            <i class="fas fa-user" />
          </template>
          <span>用户管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/start-page"
          @click="handleMenuClick('/back/start-page')"
        >
          <template #icon>
            <i class="fas fa-book-open" />
          </template>
          <span>开始页管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/doctor-story"
          @click="handleMenuClick('/back/doctor-story')"
        >
          <template #icon>
            <i class="fas fa-user-doctor" />
          </template>
          <span>针灸名医管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/jingluo"
          @click="handleMenuClick('/back/jingluo')"
        >
          <template #icon>
            <i class="fas fa-route" />
          </template>
          <span>经络管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/train-game"
          @click="handleMenuClick('/back/train-game')"
        >
          <template #icon>
            <i class="fas fa-train" />
          </template>
          <span>小火车关卡管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/xuewei"
          @click="handleMenuClick('/back/xuewei')"
        >
          <template #icon>
            <i class="fas fa-location-dot" />
          </template>
          <span>腧穴管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/zhenfa"
          @click="handleMenuClick('/back/zhenfa')"
        >
          <template #icon>
            <i class="fas fa-needle" />
          </template>
          <span>针法管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/extracourse"
          @click="handleMenuClick('/back/extracourse')"
        >
          <template #icon>
            <i class="fas fa-leaf" />
          </template>
          <span>拓展疗法管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/illness"
          @click="handleMenuClick('/back/illness')"
        >
          <template #icon>
            <i class="fas fa-notes-medical" />
          </template>
          <span>疾病管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/skills"
          @click="handleMenuClick('/back/skills')"
        >
          <template #icon>
            <i class="fas fa-sitemap" />
          </template>
          <span>技能点管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/quiz-question"
          @click="handleMenuClick('/back/quiz-question')"
        >
          <template #icon>
            <i class="fas fa-clipboard-list" />
          </template>
          <span>题库管理</span>
        </a-menu-item>

        <a-sub-menu key="community-mgmt">
          <template #icon>
            <i class="fas fa-comments" />
          </template>
          <template #title>
            <span>社区管理</span>
          </template>

          <a-menu-item
            key="/back/community-post"
            @click="handleMenuClick('/back/community-post')"
          >
            <template #icon>
              <i class="fas fa-file-lines" />
            </template>
            <span>帖子管理</span>
          </a-menu-item>

          <a-menu-item
            key="/back/community-comment"
            @click="handleMenuClick('/back/community-comment')"
          >
            <template #icon>
              <i class="fas fa-comment-dots" />
            </template>
            <span>评论管理</span>
          </a-menu-item>

          <a-menu-item
            key="/back/community-feedback"
            @click="handleMenuClick('/back/community-feedback')"
          >
            <template #icon>
              <i class="fas fa-inbox" />
            </template>
            <span>反馈消息</span>
          </a-menu-item>
        </a-sub-menu>
        
        <a-menu-item
          key="/back/profile"
          @click="handleMenuClick('/back/profile')"
        >
          <template #icon>
            <i class="fas fa-user" />
          </template>
          <span>个人信息</span>
        </a-menu-item>
      </a-menu>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import siteConfig from '@/config/site'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const selectedKeys = ref([route.path])

// 监听路由变化更新选中的菜单
watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

// 处理菜单点击
const handleMenuClick = (path) => {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%; 
  min-height: 100vh;
  background:rgb(255, 255, 255);
  display: flex;
  flex-direction: column;
  width: 220px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.is-collapsed {
    width: 64px;
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin: 0;
      }
    }

    :deep(.ant-menu) {
      // 只隐藏文字，不隐藏图标
      .ant-menu-submenu-title > span:not(.anticon),
      .ant-menu-item > span:not(.anticon) {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }
  
  .logo {
    height: 60px;
    flex-shrink: 0;
    line-height: 60px;
    text-align: center;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    align-items: center;
    padding: 0 16px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    .logo-icon {
      width: 32px;
      height: 32px;
      margin-right: 8px;
      object-fit: contain;
      transition: margin 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .logo-text {
      color:rgb(0, 0, 0);
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.2s;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    width: 100%;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    width: 100% !important;
  }
}
</style> 
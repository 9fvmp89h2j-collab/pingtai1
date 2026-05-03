<template>
  <div class="navbar">
    <div class="left-menu">
      <div
        class="hamburger"
        @click="toggleSidebar"
      >
        <MenuUnfoldOutlined v-if="appStore.sidebarCollapsed" />
        <MenuFoldOutlined v-else />
      </div>
      <a-breadcrumb separator="/">
        <a-breadcrumb-item>
          <router-link to="/dashboard">
            首页
          </router-link>
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>
    
    <div class="right-menu">
      <div
        class="right-menu-item"
        @click="toggleFullScreen"
      >
        <FullscreenExitOutlined v-if="isFullscreen" />
        <FullscreenOutlined v-else />
      </div>
      
      <a-dropdown trigger="click">
        <div class="avatar-wrapper">
          <a-avatar
            :size="32"
            :src="avatarUrl"
          >
            {{ userInfo?.name?.charAt(0)?.toUpperCase() || userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
          </a-avatar>
          <span class="user-name">{{ userInfo?.name || userInfo?.username || '用户' }}</span>
          <DownOutlined class="dropdown-icon" />
        </div>
        <template #overlay>
          <a-menu>
            <a-menu-item
              key="logout"
              @click="handleLogout"
            >
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { Modal } from 'ant-design-vue'
import { 
  MenuUnfoldOutlined, 
  MenuFoldOutlined, 
  DownOutlined, 
  LogoutOutlined, 
  FullscreenOutlined, 
  FullscreenExitOutlined 
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const userInfo = computed(() => userStore.userInfo)
const isFullscreen = ref(false)

const toggleSidebar = () => {
  appStore.toggleSidebar()
}
const avatarUrl = computed(() => {
  return userInfo.value?.avatar;
})
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

// 监听全屏状态变化
const fullscreenChangeHandler = () => {
  isFullscreen.value = !!document.fullscreenElement
}

document.addEventListener('fullscreenchange', fullscreenChangeHandler)

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', fullscreenChangeHandler)
})

const handleLogout = () => {
  Modal.confirm({
    title: '提示',
    content: '确定要退出登录吗?',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await userStore.logout()
      router.push('/login')
    }
  })
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;

  .left-menu {
    display: flex;
    align-items: center;
    gap: 16px;

    .hamburger {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 4px;
      color: #666;
      height: 32px;
      width: 32px;
      
      &:hover {
        background: #f6f6f6;
      }
    }

    :deep(.ant-breadcrumb) {
      a {
        color: #999;
        
        &:hover {
          color: #1890ff;
        }
      }
      
      .ant-breadcrumb-separator {
        color: #999;
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 8px;

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #666;
      border-radius: 4px;
      transition: all 0.3s;
      height: 32px;
      width: 32px;
      
      &:hover {
        background: #f6f6f6;
        color: #333;
      }
    }
    
    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 8px;
      height: 32px;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s;
      
      &:hover {
        background: #f6f6f6;
      }
      
      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #666;
        line-height: 32px;
      }

      .dropdown-icon {
        color: #999;
        font-size: 12px;
      }
    }
  }

  :deep(.ant-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    height: 40px;
    
    .ant-dropdown-menu__item-icon {
      margin-right: 4px;
      display: flex;
      align-items: center;
    }
  }
}
</style>
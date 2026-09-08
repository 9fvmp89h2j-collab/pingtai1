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
        v-model:open-keys="openKeys"
        :inline-collapsed="isCollapsed" 
        mode="inline" 
        class="sidebar-menu"
      >
        <template
          v-for="item in adminNavigation"
          :key="item.key"
        >
          <a-sub-menu v-if="item.children" :key="item.key">
            <template #icon>
              <i :class="item.icon" />
            </template>
            <template #title>
              <span>{{ item.label }}</span>
            </template>
            <a-menu-item
              v-for="child in item.children"
              :key="child.path"
              @click="handleMenuClick(child.path)"
            >
              <template #icon>
                <i :class="child.icon" />
              </template>
              <span>{{ child.label }}</span>
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item
            v-else
            :key="item.path"
            @click="handleMenuClick(item.path)"
          >
            <template #icon>
              <i :class="item.icon" />
            </template>
            <span>{{ item.label }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import siteConfig from '@/config/site'
import { adminNavigation, getAdminOpenKeys } from '@/config/adminNavigation'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const selectedKeys = ref([route.path])
const openKeys = ref(getAdminOpenKeys(route.path))

// 监听路由变化更新选中的菜单
watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
  openKeys.value = getAdminOpenKeys(newPath)
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
  background: #1d403c;
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
    background: #183632;
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(246, 242, 234, 0.12);
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
      color: #f6f2ea;
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
    background: transparent;
    width: 100% !important;

    .ant-menu-item,
    .ant-menu-submenu-title {
      color: rgba(246, 242, 234, 0.78);
      border-radius: 10px;
      margin: 4px 10px;
      width: calc(100% - 20px);
    }

    .ant-menu-item:hover,
    .ant-menu-submenu-title:hover,
    .ant-menu-submenu-selected > .ant-menu-submenu-title {
      color: #f6f2ea;
      background: rgba(233, 177, 142, 0.18);
    }

    .ant-menu-item-selected {
      color: #1d403c;
      background: #e9b18e;
      font-weight: 700;
    }

    .ant-menu-item-selected::after {
      display: none;
    }

    .ant-menu-sub {
      background: #183632;
    }
  }
}
</style>

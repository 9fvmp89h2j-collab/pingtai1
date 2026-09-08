<template>
  <a-drawer
    v-if="isDesktop"
    :open="open"
    :title="title"
    :width="drawerWidth"
    :mask-closable="!loading"
    :keyboard="!loading"
    class="admin-editor-drawer"
    @close="handleCancel"
  >
    <div @input="markDirty" @change="markDirty">
      <slot />
    </div>
    <template #footer>
      <div class="admin-editor-footer">
        <a-button :disabled="loading" @click="handleCancel">
          {{ cancelText }}
        </a-button>
        <a-button type="primary" :loading="loading" @click="$emit('confirm')">
          {{ okText }}
        </a-button>
      </div>
    </template>
  </a-drawer>

  <a-modal
    v-else
    :open="open"
    :title="title"
    :width="modalWidth"
    :confirm-loading="loading"
    :ok-text="okText"
    :cancel-text="cancelText"
    @ok="$emit('confirm')"
    @cancel="handleCancel"
  >
    <slot />
  </a-modal>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Modal } from 'ant-design-vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  drawerWidth: { type: [Number, String], default: 720 },
  modalWidth: { type: [Number, String], default: 720 },
  okText: { type: String, default: '保存' },
  cancelText: { type: String, default: '取消' }
})

const emit = defineEmits(['update:open', 'confirm', 'cancel'])
const mediaQuery = window.matchMedia('(min-width: 1024px)')
const isDesktop = ref(mediaQuery.matches)
const isDirty = ref(false)

const updateViewport = (event) => {
  isDesktop.value = event.matches
}

const closeSurface = () => {
  isDirty.value = false
  emit('update:open', false)
  emit('cancel')
}

const markDirty = () => {
  isDirty.value = true
}

const handleCancel = () => {
  if (props.loading) return
  if (!isDesktop.value || !isDirty.value) {
    closeSurface()
    return
  }

  Modal.confirm({
    title: '放弃未保存的修改？',
    content: '关闭后，本次编辑内容不会被保存。',
    okText: '放弃修改',
    cancelText: '继续编辑',
    okType: 'danger',
    onOk: closeSurface
  })
}

watch(() => props.open, (open) => {
  if (open) isDirty.value = false
})

onMounted(() => mediaQuery.addEventListener('change', updateViewport))
onBeforeUnmount(() => mediaQuery.removeEventListener('change', updateViewport))
</script>

<style scoped>
.admin-editor-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

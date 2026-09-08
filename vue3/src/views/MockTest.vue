<template>
  <div class="mock-test">
    <a-card title="🎀 Mock数据测试页面">
      <a-space
        direction="vertical"
        :size="16"
        style="width: 100%"
      >
        <a-alert 
          :message="`Mock状态: ${mockEnabled ? '✅ 已启用' : '❌ 未启用'}`"
          :type="mockEnabled ? 'success' : 'warning'"
          show-icon
        />
        
        <a-divider>登录测试</a-divider>
        
        <a-form
          :model="loginForm"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 20 }"
        >
          <a-form-item label="用户名">
            <a-input
              v-model:value="loginForm.username"
              placeholder="admin 或 user001"
            />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password
              v-model:value="loginForm.password"
              placeholder="123456"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4, span: 20 }">
            <a-button
              type="primary"
              :loading="loading"
              @click="testLogin"
            >
              测试登录
            </a-button>
          </a-form-item>
        </a-form>
        
        <a-divider>测试结果</a-divider>
        
        <a-textarea
          v-model:value="result"
          :rows="10"
          placeholder="测试结果将显示在这里..."
          readonly
        />
      </a-space>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { login } from '@/api/user'

const loading = ref(false)
const result = ref('')

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

// 检查Mock是否启用
const mockEnabled = computed(() => {
  return import.meta.env.DEV && import.meta.env.VITE_USE_MOCK === 'true'
})

const testLogin = async () => {
  loading.value = true
  result.value = '🚀 开始测试登录...\n'
  
  try {
    const startTime = Date.now()
    
    await login(loginForm, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        const endTime = Date.now()
        result.value += `✅ 登录成功！耗时: ${endTime - startTime}ms\n`
        result.value += `📋 返回数据:\n${JSON.stringify(data, null, 2)}\n`
      },
      onError: (error) => {
        const endTime = Date.now()
        result.value += `❌ 登录失败！耗时: ${endTime - startTime}ms\n`
        result.value += `📋 错误信息:\n${JSON.stringify(error, null, 2)}\n`
      }
    })
  } catch (error) {
    result.value += `💥 请求异常:\n${JSON.stringify(error, null, 2)}\n`
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  result.value = `🎀 Mock测试页面已加载\n`
  result.value += `📊 环境信息:\n`
  result.value += `- DEV模式: ${import.meta.env.DEV}\n`
  result.value += `- Mock开关: ${import.meta.env.VITE_USE_MOCK}\n`
  result.value += `- Mock状态: ${mockEnabled.value ? '启用' : '禁用'}\n\n`
  result.value += `🧪 测试说明:\n`
  result.value += `- 管理员账号: admin / 123456\n`
  result.value += `- 普通用户: user001 / 123456\n`
  result.value += `- 错误测试: 使用其他用户名或密码\n\n`
})
</script>

<style scoped>
.mock-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style>

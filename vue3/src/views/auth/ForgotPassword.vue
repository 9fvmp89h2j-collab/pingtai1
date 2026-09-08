<template>
  <AuthForm
    v-if="!resetSuccess"
    :title="codeSent ? '验证邮箱' : '找回侦探身份卡'"
    :subtitle="codeSent ? codeHint : '先验证身份卡账号与家长邮箱，再设置新密码。'"
    :fields="activeFields"
    :form-data="forgotForm"
    :rules="activeRules"
    :loading="loading"
    :submit-text="codeSent ? '安全重置密码' : '发送邮箱验证码'"
    :links="authLinks"
    @update-field="({ fieldName, value }) => { forgotForm[fieldName] = value }"
    @submit="handleSubmit"
  />

  <section
    v-else
    class="success-message"
    aria-live="polite"
  >
    <CheckCircleOutlined class="success-icon" />
    <h2>身份卡密码已更新</h2>
    <p>请使用新密码登录，继续小铜人侦探任务。</p>
    <router-link
      to="/auth/login"
      class="login-btn"
    >
      返回侦探登录
    </router-link>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import {
  CheckCircleOutlined,
  LockOutlined,
  MailOutlined,
  NumberOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import AuthForm from '@/components/AuthForm.vue'
import { forgetPassword, requestPasswordResetCode } from '@/api/user'

const loading = ref(false)
const codeSent = ref(false)
const resetSuccess = ref(false)

const forgotForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const identityFields = [
  {
    prop: 'username',
    label: '侦探账号',
    placeholder: '请输入创建身份卡时的账号',
    autocomplete: 'username',
    maxlength: 50,
    icon: UserOutlined
  },
  {
    prop: 'email',
    label: '家长联系邮箱',
    placeholder: '请输入身份卡绑定的邮箱',
    autocomplete: 'email',
    maxlength: 100,
    icon: MailOutlined
  }
]

const resetFields = [
  {
    prop: 'verificationCode',
    label: '6位邮箱验证码',
    placeholder: '请输入邮件中的验证码',
    autocomplete: 'one-time-code',
    inputmode: 'numeric',
    maxlength: 6,
    icon: NumberOutlined
  },
  {
    prop: 'newPassword',
    label: '新的侦探密码',
    type: 'password',
    placeholder: '至少8个字符',
    autocomplete: 'new-password',
    maxlength: 100,
    icon: LockOutlined
  },
  {
    prop: 'confirmPassword',
    label: '再次确认密码',
    type: 'password',
    placeholder: '请再次输入新密码',
    autocomplete: 'new-password',
    maxlength: 100,
    icon: LockOutlined
  }
]

const identityRules = {
  username: [
    { required: true, message: '请输入侦探账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度应为3到50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入家长邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const resetRules = {
  verificationCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码应为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 100, message: '密码长度应为8到100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: async (_rule, value) => {
        if (value !== forgotForm.newPassword) throw new Error('两次输入的密码不一致')
      },
      trigger: 'blur'
    }
  ]
}

const activeFields = computed(() => (codeSent.value ? resetFields : identityFields))
const activeRules = computed(() => (codeSent.value ? resetRules : identityRules))
const codeHint = computed(() => `验证码已发送到 ${maskEmail(forgotForm.email)}，10分钟内有效。`)
const authLinks = [
  { text: '返回登录', to: '/auth/login' },
  { text: '还没有账号？立即注册', to: '/auth/register' }
]

const maskEmail = (email) => {
  const [name = '', domain = ''] = email.split('@')
  if (!domain) return '家长邮箱'
  const visible = name.slice(0, Math.min(2, name.length))
  return `${visible}${'*'.repeat(Math.max(2, name.length - visible.length))}@${domain}`
}

const handleSubmit = async () => {
  loading.value = true
  try {
    if (!codeSent.value) {
      await requestPasswordResetCode({
        username: forgotForm.username,
        email: forgotForm.email
      }, {
        successMsg: '如果信息匹配，验证码会发送到该邮箱',
        showDefaultMsg: true
      })
      codeSent.value = true
      return
    }

    await forgetPassword({
      username: forgotForm.username,
      email: forgotForm.email,
      verificationCode: forgotForm.verificationCode,
      newPassword: forgotForm.newPassword
    }, {
      successMsg: '密码重置成功',
      showDefaultMsg: true
    })
    resetSuccess.value = true
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.success-message {
  max-width: 520px;
  margin: 0 auto;
  padding: 48px 32px;
  text-align: center;
  color: #234735;
  background: #fffdf7;
  border: 1px solid #cfbd8c;
  border-radius: 24px;
  box-shadow: 0 20px 48px rgba(41, 63, 48, 0.14);
}

.success-icon {
  margin-bottom: 20px;
  color: #2f7654;
  font-size: 72px;
}

.success-message h2 {
  margin: 0 0 12px;
  font-size: 26px;
}

.success-message p {
  margin: 0 0 28px;
  color: #5f685f;
  line-height: 1.7;
}

.login-btn {
  display: inline-flex;
  min-height: 46px;
  align-items: center;
  justify-content: center;
  padding: 0 28px;
  color: #fff;
  background: #174f39;
  border-radius: 999px;
  font-weight: 700;
  text-decoration: none;
}

.login-btn:focus-visible {
  outline: 3px solid rgba(201, 164, 79, 0.5);
  outline-offset: 3px;
}
</style>

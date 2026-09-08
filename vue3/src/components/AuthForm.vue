<template>
  <div class="auth-form">
    <!-- 表单标题（无标题时不显示） -->
    <div
      v-if="title"
      class="form-header"
    >
      <h2>{{ title }}</h2>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>

    <!-- 表单内容 -->
    <form ref="formRef" novalidate @submit.prevent="handleSubmit">
      <!-- 动态渲染表单项 -->
      <div 
        v-for="field in fields" 
        :key="field.prop"
        class="form-item"
        :class="{ 'has-error': errors[field.prop] }"
      >
        <label :for="fieldId(field.prop)">{{ field.label || field.placeholder }}</label>
        <div class="input-wrapper">
          <span
            v-if="field.icon"
            class="input-icon"
          >
            <component :is="field.icon" />
          </span>
          <input 
            v-if="field.type !== 'password'"
            :id="fieldId(field.prop)"
            :value="formData[field.prop]"
            :type="field.type || 'text'"
            :name="field.prop"
            :autocomplete="field.autocomplete || 'off'"
            :maxlength="field.maxlength"
            :inputmode="field.inputmode"
            :placeholder="field.placeholder"
            :aria-invalid="Boolean(errors[field.prop])"
            :aria-describedby="errors[field.prop] ? errorId(field.prop) : undefined"
            @blur="validateField(field.prop)"
            @input="updateField(field.prop, $event.target.value)"
          >
          <input 
            v-else
            :id="fieldId(field.prop)"
            :value="formData[field.prop]"
            type="password"
            :name="field.prop"
            :autocomplete="field.autocomplete || 'current-password'"
            :maxlength="field.maxlength"
            :placeholder="field.placeholder"
            :aria-invalid="Boolean(errors[field.prop])"
            :aria-describedby="errors[field.prop] ? errorId(field.prop) : undefined"
            @blur="validateField(field.prop)"
            @input="updateField(field.prop, $event.target.value)"
          >
        </div>
        <small v-if="errors[field.prop]" :id="errorId(field.prop)" class="field-error" role="alert">{{ errors[field.prop] }}</small>
      </div>
      
      <!-- 提交按钮 -->
      <div class="form-item">
        <button 
          type="submit" 
          class="submit-btn"
          :disabled="loading"
        >
          <span
            v-if="loading"
            class="loading-spinner"
          />
          {{ loading ? '提交中...' : submitText }}
        </button>
      </div>
    </form>

    <!-- 底部链接 -->
    <div
      v-if="links && links.length"
      class="form-links"
    >
      <div
        v-for="link in links"
        :key="link.text"
        class="link-item"
      >
        <router-link :to="link.to">
          {{ link.text }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  fields: {
    type: Array,
    required: true
  },
  formData: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    required: true
  },
  submitText: {
    type: String,
    default: '提交'
  },
  loading: {
    type: Boolean,
    default: false
  },
  links: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['submit', 'update-field'])

const formRef = ref(null)
const errors = reactive({})

// 验证单个字段
const fieldId = (fieldName) => `auth-${fieldName}`
const errorId = (fieldName) => `auth-${fieldName}-error`

const validateField = async (fieldName) => {
  const fieldRules = props.rules[fieldName]
  if (!fieldRules) return true

  const value = props.formData[fieldName]
  
  for (const rule of fieldRules) {
    // 必填验证
    if (rule.required && (!value || value.trim() === '')) {
      errors[fieldName] = rule.message
      return false
    }
    
    // 最小长度验证
    if (rule.min && value && value.length < rule.min) {
      errors[fieldName] = rule.message
      return false
    }
    
    // 最大长度验证
    if (rule.max && value && value.length > rule.max) {
      errors[fieldName] = rule.message
      return false
    }
    
    // 正则验证
    if (rule.pattern && value && !rule.pattern.test(value)) {
      errors[fieldName] = rule.message
      return false
    }
    
    // 邮箱验证
    if (rule.type === 'email' && value) {
      const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
      if (!emailRegex.test(value)) {
        errors[fieldName] = rule.message
        return false
      }
    }
    
    // 自定义验证器 (支持同步和异步)
    if (rule.validator) {
      try {
        await rule.validator(rule, value)
      } catch (error) {
        errors[fieldName] = error.message || error
        return false
      }
    }
  }
  
  delete errors[fieldName]
  return true
}

// 清除错误
const clearError = (fieldName) => {
  delete errors[fieldName]
}

const updateField = (fieldName, value) => {
  emit('update-field', { fieldName, value })
  clearError(fieldName)
}

// 验证所有字段
const validateForm = async () => {
  const results = await Promise.all(Object.keys(props.rules).map(validateField))
  return results.every(Boolean)
}

const handleSubmit = async () => {
  if (await validateForm()) {
    emit('submit')
    return
  }

  const firstInvalidField = props.fields.find((field) => errors[field.prop])
  if (firstInvalidField) {
    document.getElementById(fieldId(firstInvalidField.prop))?.focus()
  }
}

defineExpose({
  formRef,
  validateForm
})
</script>

<style scoped>
/* 思源字体 */
.auth-form {
  width: 100%;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h2 {
  margin: 0;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
  font-size: 1.6rem;
  font-weight: 700;
  color: #2C2C2C;
  letter-spacing: 0.2rem;
}

.form-header p {
  max-width: 38ch;
  margin: 10px auto 0;
  color: #657268;
  font-size: 13px;
  line-height: 1.65;
}

/* 表单项样式 */
.form-item {
  display: grid;
  gap: 7px;
  margin-bottom: 1rem;
  position: relative;
}

.form-item > label {
  color: #405144;
  font-size: 13px;
  font-weight: 800;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #a27329;
  font-size: 16px;
  z-index: 1;
  display: flex;
  align-items: center;
}

.input-wrapper input {
  width: 100%;
  height: 52px;
  padding: 0 16px;
  padding-left: 45px;
  border: 1.5px solid #cfbd8c;
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
  color: #304435;
  background: #fffdf8;
  transition: all 0.3s ease;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #9d9584;
  transition: color 0.3s ease;
}

/* 错误状态下的占位符颜色 */
.input-wrapper input:hover {
  border-color: #af8a42;
}

.input-wrapper input:focus {
  border-color: #2f7654;
  box-shadow: 0 0 0 4px rgba(47, 118, 84, 0.11);
}

/* 错误状态 */
.form-item.has-error .input-wrapper input {
  border-color: #b84a3d;
  animation: shake 0.3s ease;
}

.field-error {
  color: #ae3d34;
  font-size: 12px;
  font-weight: 700;
}



/* 提交按钮样式 */
.submit-btn {
  width: 100%;
  height: 50px;
  border: 1px solid #c9a44f;
  border-radius: 25px;
  background: #174f39;
  color: #fff9e7;
  font-size: 1rem;
  font-weight: 800;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
  letter-spacing: 0.2rem;
  cursor: pointer;
  box-shadow: 0 4px 0 #a8792b;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.submit-btn:hover:not(:disabled) {
  background: #236548;
  transform: translateY(-1px);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载动画 */
.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.form-links {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px dashed rgba(197, 165, 114, 0.42);
}

.link-item {
  margin: 0.8rem 0;
}

.link-item a {
  color: #2f7654;
  text-decoration: none;
  font-size: 0.9rem;
  letter-spacing: 0.05rem;
  transition: all 0.3s ease;
  position: relative;
}

.link-item a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 0;
  height: 1px;
  background: #bd852d;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.link-item a:hover {
  color: #174f39;
}

:focus-visible {
  outline: 3px solid #d8a641 !important;
  outline-offset: 3px;
}

.link-item a:hover::after {
  width: 100%;
}
</style>

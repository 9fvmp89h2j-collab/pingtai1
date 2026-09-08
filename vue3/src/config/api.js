/**
 * API配置文件
 */

// 获取环境变量中的API地址，如果没有则使用默认值
export const API_BASE_URL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080'

// API端点配置
export const API_ENDPOINTS = {
  // 用户相关
  USER: {
    LOGIN: '/api/user/login',
    REGISTER: '/api/user/register',
    CURRENT: '/api/user/current',
    UPDATE: '/api/user/update',
    UPDATE_PASSWORD: '/api/user/password'
  },

  // 文件相关
  FILE: {
    UPLOAD: '/api/file/upload',
    PREVIEW: '/api/file/preview',
    DOWNLOAD: '/api/file/download',
    DELETE: '/api/file/delete'
  },

}

export default {
  API_BASE_URL,
  API_ENDPOINTS
}



/**
 * 机器人助手 AI 接口
 */
import request from '@/utils/request'

/**
 * 简单对话：传入 prompt，返回字符串回复
 * @param {{ prompt: string }} params
 */
export function aiChat(params, callbacks = {}) {
  // DeepSeek 偶发响应较慢，这里单独拉长超时并开启重试
  return request.get('/ai/chat', params, {
    timeout: 60000,
    enableRetry: true,
    retryCount: 2,
    ...callbacks
  })
}


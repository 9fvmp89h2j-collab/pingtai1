package org.example.springboot.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话消息体（兼容 deepseek 官方协议）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String role;

    private String content;
}


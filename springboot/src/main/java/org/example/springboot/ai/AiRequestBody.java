package org.example.springboot.ai;

import lombok.Data;

import java.util.List;

/**
 * DeepSeek Chat 请求体
 */
@Data
public class AiRequestBody {

    private String model;

    private List<ChatMessage> messages;
}


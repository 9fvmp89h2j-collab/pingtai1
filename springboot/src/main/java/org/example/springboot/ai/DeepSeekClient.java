package org.example.springboot.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.exception.BusinessException;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DeepSeek 简单客户端（直接 HTTP 调用）
 */
@Slf4j
@Component
public class DeepSeekClient {

    private static final int TIMEOUT_MS = 60000;
    private static final int MAX_RETRIES = 2;
    private static final String EDUCATION_ONLY_SYSTEM_PROMPT = """
            你是“小铜人中医侦探社”的儿童文化科普助手。只回答中国传统医学历史人物、经络名称、
            身体区域基础认知和安全学习规则。不得提供疾病判断、诊断、治疗建议、穴位功效、针刺、
            艾灸、按摩操作、药物、方剂、剂量或个性化医疗建议。遇到身体不适或危险问题，应请孩子
            立即告诉家长或老师，并由专业医生判断；不得替代急救服务。不得索取姓名、电话、住址等
            隐私。使用儿童能读懂的中文，语气温和，答案控制在80字以内。
            """;

    private final Environment env;

    public DeepSeekClient(Environment env) {
        this.env = env;
    }

    @Value("${ai.deepseek.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String endpoint;

    private String resolveApiKey() {
        // 兼容多种配置路径，避免因为缩进/历史配置导致取不到
        String[] keys = {
                "ai.deepseek.api-key",
                "spring.ai.deepseek.api-key",
                "file.ai.deepseek.api-key"
        };
        for (String k : keys) {
            String v = env.getProperty(k);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    public boolean isConfigured() {
        return !resolveApiKey().isBlank();
    }

    /**
     * 单轮对话（仅 user 消息）
     */
    public String chat(String prompt) {
        String apiKey = resolveApiKey();
        if (apiKey.isBlank()) {
            throw new BusinessException("未配置 DeepSeek API Key，请联系管理员");
        }
        AiRequestBody body = new AiRequestBody();
        body.setModel("deepseek-chat");
        body.setMessages(List.of(
                new ChatMessage("system", EDUCATION_ONLY_SYSTEM_PROMPT),
                new ChatMessage("user", prompt)
        ));

        Exception last = null;
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                HttpResponse response = HttpRequest.post(endpoint)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .timeout(TIMEOUT_MS)
                        .body(JSONUtil.toJsonStr(body))
                        .execute();

                if (response.getStatus() / 100 != 2) {
                    log.warn("DeepSeek API 非 2xx 响应: status={}", response.getStatus());
                    throw new BusinessException("AI 服务暂时不可用，请稍后再试");
                }

                JSONObject result = JSONUtil.parseObj(response.body());
                return result.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            } catch (BusinessException e) {
                // 业务错误不重试（如 key 缺失、返回非2xx等）
                throw e;
            } catch (Exception e) {
                last = e;
                if (i < MAX_RETRIES) {
                    try {
                        Thread.sleep(250L * (i + 1));
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        log.error("调用 DeepSeek 失败（已重试）", last);
        throw new BusinessException("AI 响应超时或网络不稳定，请稍后再试");
    }
}


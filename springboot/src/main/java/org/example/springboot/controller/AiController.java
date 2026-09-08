package org.example.springboot.controller;

import jakarta.annotation.Resource;
import org.example.springboot.ai.DeepSeekClient;
import org.example.springboot.common.Result;
import org.example.springboot.common.ResultCode;
import org.example.springboot.dto.command.AiChatRequestDTO;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.service.AiSafetyService;
import org.example.springboot.util.JwtTokenUtils;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 机器人助手简单AI对话接口
 */
@RestController
@RequestMapping("/ai")
@Validated
public class AiController {

    @Resource
    private DeepSeekClient deepSeekClient;
    @Resource
    private AiSafetyService aiSafetyService;

    @PostMapping("/chat")
    public Result<String> chat(@Valid @RequestBody AiChatRequestDTO request) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录后再使用机器人助手");
        }
        String prompt = request.getPrompt().trim();
        if (aiSafetyService.requiresSafeRefusal(prompt)) {
            return Result.success(AiSafetyService.SAFE_REFUSAL);
        }
        if (!deepSeekClient.isConfigured()) {
            return Result.success(aiSafetyService.localEducationalReply(prompt));
        }
        try {
            String reply = deepSeekClient.chat(prompt);
            return Result.success(aiSafetyService.sanitizeReply(reply));
        } catch (BusinessException error) {
            return Result.success(aiSafetyService.localEducationalReply(prompt));
        }
    }
}


package org.example.springboot.controller;

import jakarta.annotation.Resource;
import org.example.springboot.ai.DeepSeekClient;
import org.example.springboot.common.Result;
import org.example.springboot.common.ResultCode;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String prompt) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录后再使用机器人助手");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new BusinessException("请输入问题");
        }
        String reply = deepSeekClient.chat(prompt.trim());
        return Result.success(reply);
    }
}


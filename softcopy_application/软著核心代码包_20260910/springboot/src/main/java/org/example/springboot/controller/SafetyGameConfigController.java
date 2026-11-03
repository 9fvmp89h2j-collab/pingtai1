package org.example.springboot.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.SafetyMemoryProgressCommandDTO;
import org.example.springboot.dto.command.SafetyScenarioAnswerCommandDTO;
import org.example.springboot.service.SafetyGameConfigService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/game/safety")
public class SafetyGameConfigController {
    @Resource
    private SafetyGameConfigService safetyGameConfigService;

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(safetyGameConfigService.getPublicConfig());
    }

    @PostMapping("/scenario/answer")
    public Result<Map<String, Object>> scenarioAnswer(@Valid @RequestBody SafetyScenarioAnswerCommandDTO command) {
        return Result.success(safetyGameConfigService.checkScenarioAnswer(
                command.getConfigRevision(), command.getScenarioId(), command.getOptionId()));
    }

    @GetMapping("/memory-progress")
    public Result<Map<String, Object>> memoryProgress() {
        return Result.success(safetyGameConfigService.memoryProgress(JwtTokenUtils.getCurrentUserId()));
    }

    @PutMapping("/memory-progress")
    public Result<Map<String, Object>> completeMemory(@Valid @RequestBody SafetyMemoryProgressCommandDTO command) {
        return Result.success(safetyGameConfigService.completeMemoryLevel(
                JwtTokenUtils.getCurrentUserId(), command.getCompletedLevel()));
    }
}

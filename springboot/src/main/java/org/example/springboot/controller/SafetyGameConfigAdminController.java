package org.example.springboot.controller;

import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.SafetyGameConfigDraftCommandDTO;
import org.example.springboot.dto.command.SafetyGameConfigPublishCommandDTO;
import org.example.springboot.service.SafetyGameConfigService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/safety-game")
@PreAuthorize("hasRole('ADMIN')")
public class SafetyGameConfigAdminController {
    @Resource
    private SafetyGameConfigService safetyGameConfigService;

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(safetyGameConfigService.getAdminConfig());
    }

    @PutMapping("/config/draft")
    public Result<Map<String, Object>> saveDraft(@RequestBody SafetyGameConfigDraftCommandDTO command) {
        return Result.success(safetyGameConfigService.saveDraft(command, JwtTokenUtils.getCurrentUserId()));
    }

    @PostMapping("/config/draft/validate")
    public Result<Map<String, Object>> validate(@RequestBody SafetyGameConfigDraftCommandDTO command) {
        return Result.success(safetyGameConfigService.validate(command.getConfig()));
    }

    @PostMapping("/config/draft/publish")
    public Result<Map<String, Object>> publish(@RequestBody SafetyGameConfigPublishCommandDTO command) {
        return Result.success(safetyGameConfigService.publish(
                command.getRevisionId(), command.getEditVersion(), JwtTokenUtils.getCurrentUserId()));
    }
}

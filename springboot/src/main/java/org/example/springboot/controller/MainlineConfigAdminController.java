package org.example.springboot.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.MainlineConfigDraftCommandDTO;
import org.example.springboot.dto.command.MainlineConfigPublishCommandDTO;
import org.example.springboot.dto.response.MainlineAdminConfigResponseDTO;
import org.example.springboot.dto.response.MainlineConfigResponseDTO;
import org.example.springboot.dto.response.MainlineValidationResponseDTO;
import org.example.springboot.service.AuditEventService;
import org.example.springboot.service.MainlineConfigService;
import org.example.springboot.service.WriteOperationService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/mainline-config")
@PreAuthorize("hasRole('ADMIN')")
public class MainlineConfigAdminController {
    @Resource
    private MainlineConfigService mainlineConfigService;

    @Resource
    private AuditEventService auditEventService;

    @Resource
    private WriteOperationService writeOperationService;

    @GetMapping
    public Result<MainlineAdminConfigResponseDTO> getConfig() {
        return Result.success(mainlineConfigService.getAdminConfig());
    }

    @PutMapping("/draft")
    public Result<MainlineConfigResponseDTO> saveDraft(@RequestBody MainlineConfigDraftCommandDTO command) {
        Long operatorId = JwtTokenUtils.getCurrentUserId();
        MainlineConfigResponseDTO response = mainlineConfigService.saveDraft(command, operatorId);
        auditEventService.record(operatorId, null, "MAINLINE_CONFIG_SAVE_DRAFT", "GAME_CONFIG",
                String.valueOf(response.getRevisionId()), "SUCCESS", Map.of("version", response.getVersion()));
        return Result.success("主线关卡草稿已保存", response);
    }

    @PostMapping("/draft/validate")
    public Result<MainlineValidationResponseDTO> validate(@RequestBody MainlineConfigDraftCommandDTO command) {
        return Result.success(mainlineConfigService.validate(command));
    }

    @PostMapping("/draft/publish")
    public Result<MainlineConfigResponseDTO> publish(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MainlineConfigPublishCommandDTO command) {
        Long operatorId = JwtTokenUtils.getCurrentUserId();
        String key = idempotencyKey == null || idempotencyKey.isBlank()
                ? "mainline-publish:" + command.getRevisionId() + ":" + UUID.randomUUID() : idempotencyKey;
        WriteOperationService.Claim claim = writeOperationService.claim(operatorId, key, "MAINLINE_CONFIG_PUBLISH", command);
        if (claim.replay()) return Result.success(mainlineConfigService.getPublicConfig());
        MainlineConfigResponseDTO response = mainlineConfigService.publish(command.getRevisionId(), operatorId);
        auditEventService.record(operatorId, null, "MAINLINE_CONFIG_PUBLISH", "GAME_CONFIG",
                String.valueOf(command.getRevisionId()), "SUCCESS", Map.of("version", response.getVersion()));
        writeOperationService.complete(claim, response);
        return Result.success("主线关卡配置已发布", response);
    }

    @PostMapping("/versions/{version}/restore")
    public Result<MainlineConfigResponseDTO> restore(@PathVariable int version) {
        Long operatorId = JwtTokenUtils.getCurrentUserId();
        MainlineConfigResponseDTO response = mainlineConfigService.restore(version, operatorId);
        auditEventService.record(operatorId, null, "MAINLINE_CONFIG_RESTORE", "GAME_CONFIG",
                String.valueOf(response.getRevisionId()), "SUCCESS", Map.of("sourceVersion", version, "version", response.getVersion()));
        return Result.success("历史版本已恢复为新草稿", response);
    }
}

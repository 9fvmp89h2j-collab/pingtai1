package org.example.springboot.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.GameProgressEventCommandDTO;
import org.example.springboot.dto.command.AgencyArchiveExchangeCommandDTO;
import org.example.springboot.dto.command.LegacyGameStateImportCommandDTO;
import org.example.springboot.dto.command.SafetyGameDraftCommandDTO;
import org.example.springboot.dto.command.WeeklyChoiceRewardClaimCommandDTO;
import org.example.springboot.dto.response.GameMutationResponseDTO;
import org.example.springboot.dto.response.GameStateResponseDTO;
import org.example.springboot.dto.response.RewardLedgerItemResponseDTO;
import org.example.springboot.dto.response.WeeklyChoiceRewardResponseDTO;
import org.example.springboot.service.GameStateService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameController {
    @Resource
    private GameStateService gameStateService;

    @GetMapping("/state")
    public Result<GameStateResponseDTO> state() {
        return Result.success(gameStateService.getState(JwtTokenUtils.getCurrentUserId()));
    }

    @GetMapping("/weekly-choice")
    public Result<WeeklyChoiceRewardResponseDTO> weeklyChoice() {
        return Result.success(gameStateService.getWeeklyChoiceReward(
                JwtTokenUtils.getCurrentUserId(), LocalDate.now()));
    }

    @PostMapping("/weekly-choice/claim")
    public Result<GameMutationResponseDTO> claimWeeklyChoice(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody WeeklyChoiceRewardClaimCommandDTO command) {
        String key = idempotencyKey == null || idempotencyKey.isBlank()
                ? "weekly-choice:" + LocalDate.now() + ":" + UUID.randomUUID() : idempotencyKey;
        return Result.success(gameStateService.claimWeeklyChoiceReward(
                JwtTokenUtils.getCurrentUserId(), key, command.getItemCode(), LocalDate.now()));
    }

    @GetMapping("/rewards/ledger")
    public Result<List<RewardLedgerItemResponseDTO>> rewardLedger(
            @RequestParam(defaultValue = "50") Integer limit) {
        return Result.success(gameStateService.listRewardLedger(JwtTokenUtils.getCurrentUserId(), limit));
    }

    @GetMapping("/safety/draft")
    public Result<Map<String, Object>> safetyDraft() {
        return Result.success(gameStateService.getSafetyDraft(JwtTokenUtils.getCurrentUserId()));
    }

    @PutMapping("/safety/draft")
    public Result<Map<String, Object>> saveSafetyDraft(@Valid @RequestBody SafetyGameDraftCommandDTO command) {
        return Result.success(gameStateService.saveSafetyDraft(
                JwtTokenUtils.getCurrentUserId(), command.getDraft()));
    }

    @DeleteMapping("/safety/draft")
    public Result<Boolean> clearSafetyDraft() {
        gameStateService.clearSafetyDraft(JwtTokenUtils.getCurrentUserId());
        return Result.success(true);
    }

    @PostMapping("/progress/events")
    public Result<GameMutationResponseDTO> progress(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody GameProgressEventCommandDTO command) {
        String key = idempotencyKey == null || idempotencyKey.isBlank() ? UUID.randomUUID().toString() : idempotencyKey;
        return Result.success(gameStateService.applyEvent(JwtTokenUtils.getCurrentUserId(), key, command));
    }

    @PostMapping("/agency/archive-items/exchange")
    public Result<GameMutationResponseDTO> exchangeAgencyArchiveItem(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AgencyArchiveExchangeCommandDTO command) {
        String key = idempotencyKey == null || idempotencyKey.isBlank()
                ? "agency:" + command.getItemId() + ":" + UUID.randomUUID() : idempotencyKey;
        return Result.success(gameStateService.exchangeAgencyArchiveItem(
                JwtTokenUtils.getCurrentUserId(), key, command.getItemId()));
    }

    @PostMapping("/legacy-import")
    public Result<GameMutationResponseDTO> legacyImport(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody LegacyGameStateImportCommandDTO command) {
        String key = idempotencyKey == null || idempotencyKey.isBlank()
                ? "legacy-import:" + command.getSourceKey() : idempotencyKey;
        return Result.success(gameStateService.importLegacyState(JwtTokenUtils.getCurrentUserId(), key, command));
    }
}

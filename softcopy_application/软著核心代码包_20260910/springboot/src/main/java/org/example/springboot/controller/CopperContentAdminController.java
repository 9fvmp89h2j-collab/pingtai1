package org.example.springboot.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.CopperAcupointDraftCommandDTO;
import org.example.springboot.dto.command.CopperContentScheduleCommandDTO;
import org.example.springboot.dto.command.CopperStoryDraftCommandDTO;
import org.example.springboot.service.CopperContentService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/copper-content")
@PreAuthorize("hasRole('ADMIN')")
public class CopperContentAdminController {
    @Resource
    private CopperContentService copperContentService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(copperContentService.overview());
    }

    @GetMapping("/acupoints")
    public Result<Map<String, Object>> acupoints(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean modeled) {
        return Result.success(copperContentService.listAcupoints(page, size, keyword, status, modeled));
    }

    @GetMapping("/acupoints/{code}")
    public Result<Map<String, Object>> acupoint(@PathVariable String code) {
        return Result.success(copperContentService.getAcupoint(code));
    }

    @PutMapping("/acupoints/{code}/draft")
    public Result<Map<String, Object>> saveAcupointDraft(
            @PathVariable String code, @RequestBody CopperAcupointDraftCommandDTO command) {
        return Result.success("穴位草稿已保存",
                copperContentService.saveAcupointDraft(code, command, JwtTokenUtils.getCurrentUserId()));
    }

    @GetMapping("/stories")
    public Result<Map<String, Object>> stories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(copperContentService.listStories(page, size, keyword, status));
    }

    @GetMapping("/stories/{code}")
    public Result<Map<String, Object>> story(@PathVariable String code) {
        return Result.success(copperContentService.getAdminStory(code));
    }

    @PutMapping("/stories/{code}/draft")
    public Result<Map<String, Object>> saveStoryDraft(
            @PathVariable String code, @RequestBody CopperStoryDraftCommandDTO command) {
        return Result.success("故事草稿已保存",
                copperContentService.saveStoryDraft(code, command, JwtTokenUtils.getCurrentUserId()));
    }

    @PostMapping("/{type}/{key}/{action}")
    public Result<Map<String, Object>> transition(
            @PathVariable String type,
            @PathVariable String key,
            @PathVariable String action,
            @RequestBody(required = false) CopperContentScheduleCommandDTO schedule) {
        String normalizedType = "acupoints".equalsIgnoreCase(type) ? "ACUPOINT"
                : "stories".equalsIgnoreCase(type) ? "STORY" : type;
        return Result.success("内容状态已更新", copperContentService.transition(
                normalizedType, key, action,
                schedule == null ? null : schedule.getScheduledAt(), JwtTokenUtils.getCurrentUserId()));
    }

    @GetMapping("/{type}/{key}/revisions")
    public Result<List<Map<String, Object>>> revisions(@PathVariable String type, @PathVariable String key) {
        String normalizedType = "acupoints".equalsIgnoreCase(type) ? "ACUPOINT"
                : "stories".equalsIgnoreCase(type) ? "STORY" : type;
        return Result.success(copperContentService.revisions(normalizedType, key));
    }

    @PostMapping("/{type}/{key}/revisions/{version}/restore")
    public Result<Map<String, Object>> restoreRevision(
            @PathVariable String type, @PathVariable String key, @PathVariable int version) {
        String normalizedType = "acupoints".equalsIgnoreCase(type) ? "ACUPOINT"
                : "stories".equalsIgnoreCase(type) ? "STORY" : type;
        return Result.success("历史版本已复制为新草稿", copperContentService.restoreRevision(
                normalizedType, key, version, JwtTokenUtils.getCurrentUserId()));
    }

    @GetMapping("/releases")
    public Result<Map<String, Object>> releases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(copperContentService.releases(page, size));
    }
}

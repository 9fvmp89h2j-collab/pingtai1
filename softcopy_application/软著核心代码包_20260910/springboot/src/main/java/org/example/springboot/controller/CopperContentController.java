package org.example.springboot.controller;

import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.CopperStoryProgressCommandDTO;
import org.example.springboot.service.CopperContentService;
import org.example.springboot.util.JwtTokenUtils;
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
@RequestMapping("/acupuncture/copper-content")
public class CopperContentController {
    @Resource
    private CopperContentService copperContentService;

    @GetMapping("/stories")
    public Result<List<Map<String, Object>>> stories() {
        return Result.success(copperContentService.publicStories(JwtTokenUtils.getCurrentUserId()));
    }

    @GetMapping("/stories/{code}")
    public Result<Map<String, Object>> story(@PathVariable String code) {
        return Result.success(copperContentService.publicStory(code, JwtTokenUtils.getCurrentUserId()));
    }

    @PutMapping("/stories/{code}/progress")
    public Result<Map<String, Object>> saveProgress(
            @PathVariable String code, @RequestBody CopperStoryProgressCommandDTO command) {
        return Result.success("故事进度已保存", copperContentService.saveStoryProgress(
                JwtTokenUtils.getCurrentUserId(), code, command));
    }

    @GetMapping("/feed")
    public Result<Map<String, Object>> feed(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(copperContentService.feed(JwtTokenUtils.getCurrentUserId(), limit));
    }

    @PostMapping("/feed/{releaseId}/read")
    public Result<Void> markRead(@PathVariable Long releaseId) {
        copperContentService.markRead(JwtTokenUtils.getCurrentUserId(), releaseId);
        return Result.success();
    }
}

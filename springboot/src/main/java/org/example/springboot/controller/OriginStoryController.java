package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.OriginStoryResponseDTO;
import org.example.springboot.service.OriginStoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 针灸起源故事（Landing 等前台展示）
 */
@Tag(name = "针灸起源故事", description = "originstory 表数据查询")
@RequestMapping("/acupuncture/origin-story")
@RestController
@Slf4j
public class OriginStoryController {

    @Resource
    private OriginStoryService originStoryService;

    @Operation(summary = "全部故事列表", description = "用于首页卡片，条数随数据库变化")
    @GetMapping("/list")
    public Result<List<OriginStoryResponseDTO>> list() {
        log.info("查询全部针灸起源故事");
        return Result.success(originStoryService.listAll());
    }
}

package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.OriginStoryAdminCommandDTO;
import org.example.springboot.dto.response.OriginStoryResponseDTO;
import org.example.springboot.service.OriginStoryAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "开始页管理(后台)", description = "开始页故事（originstory）后台增删改查")
@RequestMapping("/admin/originstory")
@RestController
@Slf4j
@Validated
public class OriginStoryAdminController {

    @Resource
    private OriginStoryAdminService originStoryAdminService;

    @Operation(summary = "分页查询", description = "后台分页查询开始页故事")
    @GetMapping("/page")
    public Result<Page<OriginStoryResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "标题关键词") @RequestParam(required = false) String title,
            @Parameter(description = "技能ID") @RequestParam(required = false) Long skillId
    ) {
        return Result.success(originStoryAdminService.page(current, size, title, skillId));
    }

    @Operation(summary = "创建", description = "创建开始页故事")
    @PostMapping("/create")
    public Result<OriginStoryResponseDTO> create(@RequestBody OriginStoryAdminCommandDTO dto) {
        return Result.success(originStoryAdminService.create(dto));
    }

    @Operation(summary = "详情", description = "根据ID获取详情")
    @GetMapping("/{id}")
    public Result<OriginStoryResponseDTO> get(@PathVariable Long id) {
        return Result.success(originStoryAdminService.getById(id));
    }

    @Operation(summary = "更新", description = "更新开始页故事")
    @PutMapping("/{id}")
    public Result<OriginStoryResponseDTO> update(@PathVariable Long id, @RequestBody OriginStoryAdminCommandDTO dto) {
        return Result.success(originStoryAdminService.update(id, dto));
    }

    @Operation(summary = "删除", description = "删除开始页故事")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        originStoryAdminService.delete(id);
        return Result.success();
    }
}


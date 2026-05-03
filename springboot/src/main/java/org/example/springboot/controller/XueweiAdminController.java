package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.XueweiAdminCommandDTO;
import org.example.springboot.dto.response.XueweiResponseDTO;
import org.example.springboot.service.XueweiAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "腧穴管理(后台)", description = "xuewei 表后台增删改查")
@RequestMapping("/admin/xuewei")
@RestController
@Validated
public class XueweiAdminController {

    @Resource
    private XueweiAdminService xueweiAdminService;

    @Operation(summary = "分页查询腧穴")
    @GetMapping("/page")
    public Result<Page<XueweiResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "穴位名称") @RequestParam(required = false) String xueweiName,
            @Parameter(description = "分类") @RequestParam(required = false) String xueweiCatagory,
            @Parameter(description = "技能ID") @RequestParam(required = false) Integer skillId
    ) {
        return Result.success(xueweiAdminService.page(current, size, xueweiName, xueweiCatagory, skillId));
    }

    @Operation(summary = "根据ID获取腧穴详情")
    @GetMapping("/{id}")
    public Result<XueweiResponseDTO> getById(@PathVariable Long id) {
        return Result.success(xueweiAdminService.getById(id));
    }

    @Operation(summary = "创建腧穴")
    @PostMapping("/create")
    public Result<XueweiResponseDTO> create(@RequestBody XueweiAdminCommandDTO dto) {
        return Result.success(xueweiAdminService.create(dto));
    }

    @Operation(summary = "更新腧穴")
    @PutMapping("/{id}")
    public Result<XueweiResponseDTO> update(@PathVariable Long id, @RequestBody XueweiAdminCommandDTO dto) {
        return Result.success(xueweiAdminService.update(id, dto));
    }

    @Operation(summary = "删除腧穴")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        xueweiAdminService.delete(id);
        return Result.success();
    }
}


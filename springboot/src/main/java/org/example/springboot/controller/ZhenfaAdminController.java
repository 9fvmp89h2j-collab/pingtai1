package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.ZhenfaAdminCommandDTO;
import org.example.springboot.dto.response.ZhenjiuToolResponseDTO;
import org.example.springboot.service.ZhenfaAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "针法管理(后台)", description = "zhenjiutools 表后台增删改查")
@RequestMapping("/admin/zhenfa")
@RestController
@Validated
public class ZhenfaAdminController {

    @Resource
    private ZhenfaAdminService zhenfaAdminService;

    @Operation(summary = "分页查询针法")
    @GetMapping("/page")
    public Result<Page<ZhenjiuToolResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "名称") @RequestParam(required = false) String toolsName,
            @Parameter(description = "技能ID") @RequestParam(required = false) Integer skillId
    ) {
        return Result.success(zhenfaAdminService.page(current, size, toolsName, skillId));
    }

    @Operation(summary = "根据ID获取针法详情")
    @GetMapping("/{id}")
    public Result<ZhenjiuToolResponseDTO> getById(@PathVariable Integer id) {
        return Result.success(zhenfaAdminService.getById(id));
    }

    @Operation(summary = "创建针法")
    @PostMapping("/create")
    public Result<ZhenjiuToolResponseDTO> create(@RequestBody ZhenfaAdminCommandDTO dto) {
        return Result.success(zhenfaAdminService.create(dto));
    }

    @Operation(summary = "更新针法")
    @PutMapping("/{id}")
    public Result<ZhenjiuToolResponseDTO> update(@PathVariable Integer id, @RequestBody ZhenfaAdminCommandDTO dto) {
        return Result.success(zhenfaAdminService.update(id, dto));
    }

    @Operation(summary = "删除针法")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        zhenfaAdminService.delete(id);
        return Result.success();
    }
}


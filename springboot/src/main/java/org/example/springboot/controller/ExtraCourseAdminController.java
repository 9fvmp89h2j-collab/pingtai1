package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.ExtraCourseAdminCommandDTO;
import org.example.springboot.dto.response.ExtraCourseResponseDTO;
import org.example.springboot.service.ExtraCourseAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "拓展疗法管理(后台)", description = "extracourse 表后台增删改查")
@RequestMapping("/admin/extracourse")
@RestController
@Validated
public class ExtraCourseAdminController {

    @Resource
    private ExtraCourseAdminService extraCourseAdminService;

    @Operation(summary = "分页查询拓展疗法")
    @GetMapping("/page")
    public Result<Page<ExtraCourseResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "名称") @RequestParam(required = false) String name,
            @Parameter(description = "技能ID") @RequestParam(required = false) Integer skillId
    ) {
        return Result.success(extraCourseAdminService.page(current, size, name, skillId));
    }

    @Operation(summary = "根据ID获取详情")
    @GetMapping("/{id}")
    public Result<ExtraCourseResponseDTO> getById(@PathVariable Integer id) {
        return Result.success(extraCourseAdminService.getById(id));
    }

    @Operation(summary = "创建拓展疗法")
    @PostMapping("/create")
    public Result<ExtraCourseResponseDTO> create(@RequestBody ExtraCourseAdminCommandDTO dto) {
        return Result.success(extraCourseAdminService.create(dto));
    }

    @Operation(summary = "更新拓展疗法")
    @PutMapping("/{id}")
    public Result<ExtraCourseResponseDTO> update(@PathVariable Integer id, @RequestBody ExtraCourseAdminCommandDTO dto) {
        return Result.success(extraCourseAdminService.update(id, dto));
    }

    @Operation(summary = "删除拓展疗法")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        extraCourseAdminService.delete(id);
        return Result.success();
    }
}


package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.DoctorStoryAdminCommandDTO;
import org.example.springboot.dto.response.DoctorStoryResponseDTO;
import org.example.springboot.service.DoctorStoryAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "针灸名医管理(后台)", description = "doctorstory 表后台增删改查")
@RequestMapping("/admin/doctorstory")
@RestController
@Validated
public class DoctorStoryAdminController {

    @Resource
    private DoctorStoryAdminService doctorStoryAdminService;

    @Operation(summary = "分页查询名医故事")
    @GetMapping("/page")
    public Result<Page<DoctorStoryResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "故事名称") @RequestParam(required = false) String doctorName,
            @Parameter(description = "技能ID") @RequestParam(required = false) Integer skillId
    ) {
        return Result.success(doctorStoryAdminService.page(current, size, doctorName, skillId));
    }

    @Operation(summary = "根据ID获取详情")
    @GetMapping("/{id}")
    public Result<DoctorStoryResponseDTO> getById(@PathVariable Long id) {
        return Result.success(doctorStoryAdminService.getById(id));
    }

    @Operation(summary = "创建名医故事")
    @PostMapping("/create")
    public Result<DoctorStoryResponseDTO> create(@RequestBody DoctorStoryAdminCommandDTO dto) {
        return Result.success(doctorStoryAdminService.create(dto));
    }

    @Operation(summary = "更新名医故事")
    @PutMapping("/{id}")
    public Result<DoctorStoryResponseDTO> update(@PathVariable Long id, @RequestBody DoctorStoryAdminCommandDTO dto) {
        return Result.success(doctorStoryAdminService.update(id, dto));
    }

    @Operation(summary = "删除名医故事")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        doctorStoryAdminService.delete(id);
        return Result.success();
    }
}


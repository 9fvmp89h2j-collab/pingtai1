package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.SkillAdminCreateCommandDTO;
import org.example.springboot.dto.command.SkillAdminUpdateCommandDTO;
import org.example.springboot.dto.response.SkillResponseDTO;
import org.example.springboot.service.SkillAdminService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-技能点管理", description = "admin skills CRUD")
@RequestMapping("/admin/skills")
@RestController
public class SkillAdminController {

    @Resource
    private SkillAdminService skillAdminService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<SkillResponseDTO>> page(@RequestParam(defaultValue = "1") Long current,
                                               @RequestParam(defaultValue = "10") Long size,
                                               @RequestParam(required = false) String skillName,
                                               @RequestParam(required = false) String skillCategory,
                                               @RequestParam(required = false) String skillType) {
        return Result.success(skillAdminService.page(current, size, skillName, skillCategory, skillType));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SkillResponseDTO> detail(@PathVariable Integer id) {
        return Result.success(skillAdminService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<SkillResponseDTO> create(@RequestBody SkillAdminCreateCommandDTO dto) {
        return Result.success(skillAdminService.create(dto));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public Result<SkillResponseDTO> update(@PathVariable Integer id, @RequestBody SkillAdminUpdateCommandDTO dto) {
        return Result.success(skillAdminService.update(id, dto));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        skillAdminService.delete(id);
        return Result.success();
    }
}


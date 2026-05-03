package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.IllnessAdminCommandDTO;
import org.example.springboot.dto.response.IllnessResponseDTO;
import org.example.springboot.service.IllnessAdminService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-疾病管理", description = "admin illness CRUD")
@RequestMapping("/admin/illness")
@RestController
public class IllnessAdminController {

    @Resource
    private IllnessAdminService illnessAdminService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<IllnessResponseDTO>> page(@RequestParam(defaultValue = "1") Long current,
                                                 @RequestParam(defaultValue = "10") Long size,
                                                 @RequestParam(required = false) String cowtown,
                                                 @RequestParam(required = false) String illnessname) {
        return Result.success(illnessAdminService.page(current, size, cowtown, illnessname));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<IllnessResponseDTO> detail(@PathVariable Integer id) {
        return Result.success(illnessAdminService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<IllnessResponseDTO> create(@RequestBody IllnessAdminCommandDTO dto) {
        return Result.success(illnessAdminService.create(dto));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public Result<IllnessResponseDTO> update(@PathVariable Integer id, @RequestBody IllnessAdminCommandDTO dto) {
        return Result.success(illnessAdminService.update(id, dto));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        illnessAdminService.delete(id);
        return Result.success();
    }
}


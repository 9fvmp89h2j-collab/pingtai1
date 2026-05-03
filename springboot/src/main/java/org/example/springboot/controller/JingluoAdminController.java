package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.JingluoAdminCommandDTO;
import org.example.springboot.dto.response.JingluoResponseDTO;
import org.example.springboot.service.JingluoAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "经络管理(后台)", description = "jingluo 表后台增删改查")
@RequestMapping("/admin/jingluo")
@RestController
@Validated
public class JingluoAdminController {

    @Resource
    private JingluoAdminService jingluoAdminService;

    @Operation(summary = "分页查询经络")
    @GetMapping("/page")
    public Result<Page<JingluoResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "经络名称") @RequestParam(required = false) String jingluoName,
            @Parameter(description = "分类") @RequestParam(required = false) String jingluoCatagory,
            @Parameter(description = "技能ID") @RequestParam(required = false) Integer skillId
    ) {
        return Result.success(jingluoAdminService.page(current, size, jingluoName, jingluoCatagory, skillId));
    }

    @Operation(summary = "根据ID获取经络详情")
    @GetMapping("/{id}")
    public Result<JingluoResponseDTO> getById(@PathVariable Integer id) {
        return Result.success(jingluoAdminService.getById(id));
    }

    @Operation(summary = "创建经络")
    @PostMapping("/create")
    public Result<JingluoResponseDTO> create(@RequestBody JingluoAdminCommandDTO dto) {
        return Result.success(jingluoAdminService.create(dto));
    }

    @Operation(summary = "更新经络")
    @PutMapping("/{id}")
    public Result<JingluoResponseDTO> update(@PathVariable Integer id, @RequestBody JingluoAdminCommandDTO dto) {
        return Result.success(jingluoAdminService.update(id, dto));
    }

    @Operation(summary = "删除经络")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        jingluoAdminService.delete(id);
        return Result.success();
    }
}

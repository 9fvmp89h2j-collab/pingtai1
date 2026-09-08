package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.TrainGameAdminCommandDTO;
import org.example.springboot.dto.response.TrainGameResponseDTO;
import org.example.springboot.service.TrainGameAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小火车关卡管理(后台)", description = "traingame 表后台增删改查")
@RequestMapping("/admin/train-game")
@RestController
@Validated
public class TrainGameAdminController {

    @Resource
    private TrainGameAdminService trainGameAdminService;

    @Operation(summary = "分页查询小火车关卡")
    @GetMapping("/page")
    public Result<Page<TrainGameResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "经络名称") @RequestParam(required = false) String jingluoName
    ) {
        return Result.success(trainGameAdminService.page(current, size, jingluoName));
    }

    @Operation(summary = "根据ID获取小火车关卡详情")
    @GetMapping("/{id}")
    public Result<TrainGameResponseDTO> getById(@PathVariable Integer id) {
        return Result.success(trainGameAdminService.getById(id));
    }

    @Operation(summary = "创建小火车关卡")
    @PostMapping("/create")
    public Result<TrainGameResponseDTO> create(@RequestBody TrainGameAdminCommandDTO dto) {
        return Result.success(trainGameAdminService.create(dto));
    }

    @Operation(summary = "更新小火车关卡")
    @PutMapping("/{id}")
    public Result<TrainGameResponseDTO> update(@PathVariable Integer id, @RequestBody TrainGameAdminCommandDTO dto) {
        return Result.success(trainGameAdminService.update(id, dto));
    }

    @Operation(summary = "删除小火车关卡")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        trainGameAdminService.delete(id);
        return Result.success();
    }
}

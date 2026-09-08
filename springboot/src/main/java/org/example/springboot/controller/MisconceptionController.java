package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.MisconceptionCreateCommandDTO;
import org.example.springboot.dto.response.MisconceptionResponseDTO;
import org.example.springboot.service.MisconceptionService;

import java.util.List;

/**
 * 常见误区控制器
 */
@Tag(name = "常见误区", description = "问题列表与答案")
@RequestMapping("/community/misconception")
@RestController
@Slf4j
public class MisconceptionController {

    @Resource
    private MisconceptionService misconceptionService;

    @Operation(summary = "获取全部问题列表")
    @GetMapping("/list")
    public Result<List<MisconceptionResponseDTO>> list() {
        List<MisconceptionResponseDTO> result = misconceptionService.listAll();
        return Result.success(result);
    }

    @Operation(summary = "根据ID获取单条")
    @GetMapping("/{id}")
    public Result<MisconceptionResponseDTO> getById(@PathVariable Long id) {
        MisconceptionResponseDTO result = misconceptionService.getById(id);
        return Result.success(result);
    }

    @Operation(summary = "新增常见误区（管理端）")
    @PostMapping("/create")
    public Result<MisconceptionResponseDTO> create(@Valid @RequestBody MisconceptionCreateCommandDTO dto) {
        MisconceptionResponseDTO result = misconceptionService.create(dto);
        return Result.success(result);
    }
}

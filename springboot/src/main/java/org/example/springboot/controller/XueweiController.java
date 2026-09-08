package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.XueweiResponseDTO;
import org.example.springboot.service.XueweiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "穴位", description = "xuewei 表")
@RequestMapping("/acupuncture/xuewei")
@RestController
@Slf4j
public class XueweiController {

    @Resource
    private XueweiService xueweiService;

    @Operation(summary = "全部穴位列表", description = "用于腧穴页分类横向列表")
    @GetMapping("/list")
    public Result<List<XueweiResponseDTO>> list() {
        return Result.success(xueweiService.listAll());
    }

    @Operation(summary = "穴位详情", description = "弹窗展示")
    @GetMapping("/{id}")
    public Result<XueweiResponseDTO> detail(@PathVariable("id") Long id) {
        XueweiResponseDTO dto = xueweiService.getById(id);
        if (dto == null) {
            return Result.error("穴位不存在");
        }
        return Result.success(dto);
    }
}

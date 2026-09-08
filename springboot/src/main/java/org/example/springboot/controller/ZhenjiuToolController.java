package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.ZhenjiuToolResponseDTO;
import org.example.springboot.service.ZhenjiuToolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "针灸器具", description = "zhenjiutools 表")
@RequestMapping("/acupuncture/zhenjiu-tools")
@RestController
@Slf4j
public class ZhenjiuToolController {

    @Resource
    private ZhenjiuToolService zhenjiuToolService;

    @Operation(summary = "器具列表", description = "方法页旋转木马")
    @GetMapping("/list")
    public Result<List<ZhenjiuToolResponseDTO>> list() {
        return Result.success(zhenjiuToolService.listAll());
    }
}

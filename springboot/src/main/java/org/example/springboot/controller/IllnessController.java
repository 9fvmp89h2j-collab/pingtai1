package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.IllnessResponseDTO;
import org.example.springboot.service.IllnessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "历练", description = "illness 表")
@RequestMapping("/acupuncture/illness")
@RestController
@Slf4j
public class IllnessController {

    @Resource
    private IllnessService illnessService;

    @Operation(summary = "历练列表", description = "返回 illness 表所有小镇配置")
    @GetMapping("/list")
    public Result<List<IllnessResponseDTO>> list() {
        return Result.success(illnessService.listAll());
    }

}

package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.CopperManDiscoverCommandDTO;
import org.example.springboot.dto.response.AcupointKnowledgeResponseDTO;
import org.example.springboot.dto.response.CopperManDailyCaseResponseDTO;
import org.example.springboot.dto.response.CopperManDiscoverResponseDTO;
import org.example.springboot.service.CopperManService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "小铜人馆", description = "儿童3D穴位探案学习")
@RequestMapping("/acupuncture/copper-man")
@RestController
public class CopperManController {
    @Resource
    private CopperManService copperManService;

    @Operation(summary = "儿童穴位知识列表")
    @GetMapping("/acupoints")
    public Result<List<AcupointKnowledgeResponseDTO>> acupoints() {
        return Result.success(copperManService.listAcupoints(JwtTokenUtils.getCurrentUserId()));
    }

    @Operation(summary = "今日铜人探案")
    @GetMapping("/daily-case")
    public Result<CopperManDailyCaseResponseDTO> dailyCase() {
        return Result.success(copperManService.getDailyCase(JwtTokenUtils.getCurrentUserId()));
    }

    @Operation(summary = "发现今日穴位线索")
    @PostMapping("/discover")
    public Result<CopperManDiscoverResponseDTO> discover(
            @Valid @RequestBody CopperManDiscoverCommandDTO command) {
        return Result.success(copperManService.discover(
                JwtTokenUtils.getCurrentUserId(), command.getCode()));
    }
}

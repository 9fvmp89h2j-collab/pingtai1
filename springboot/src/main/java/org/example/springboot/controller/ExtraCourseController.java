package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.ExtraCourseResponseDTO;
import org.example.springboot.service.ExtraCourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "拓展疗法", description = "extracourse 表")
@RequestMapping("/acupuncture/extracourse")
@RestController
public class ExtraCourseController {

    @Resource
    private ExtraCourseService extraCourseService;

    @Operation(summary = "拓展疗法列表")
    @GetMapping("/list")
    public Result<List<ExtraCourseResponseDTO>> list() {
        return Result.success(extraCourseService.listAll());
    }

    @Operation(summary = "拓展疗法详情")
    @GetMapping("/{id}")
    public Result<ExtraCourseResponseDTO> detail(@PathVariable("id") Integer id) {
        ExtraCourseResponseDTO dto = extraCourseService.getById(id);
        if (dto == null) return Result.error("记录不存在");
        return Result.success(dto);
    }
}


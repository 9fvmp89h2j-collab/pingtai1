package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.DoctorStoryResponseDTO;
import org.example.springboot.service.DoctorStoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 针灸名医故事（doctorstory）
 */
@Tag(name = "针灸名医故事", description = "doctorstory 表数据查询")
@RequestMapping("/acupuncture/doctor-story")
@RestController
@Slf4j
public class DoctorStoryController {

    @Resource
    private DoctorStoryService doctorStoryService;

    @Operation(summary = "全部名医故事列表", description = "用于针灸故事页交互卡片")
    @GetMapping("/list")
    public Result<List<DoctorStoryResponseDTO>> list() {
        log.info("查询全部针灸名医故事");
        return Result.success(doctorStoryService.listAll());
    }
}

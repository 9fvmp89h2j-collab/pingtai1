package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.BodyMapAcupointResponseDTO;
import org.example.springboot.service.BodyMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "身体地图", description = "儿童穴位归位知识")
@RequestMapping("/acupuncture/body-map")
@RestController
public class BodyMapController {
    @Resource
    private BodyMapService bodyMapService;

    @Operation(summary = "身体地图穴位知识列表")
    @GetMapping("/acupoints")
    public Result<List<BodyMapAcupointResponseDTO>> acupoints() {
        return Result.success(bodyMapService.listAcupoints());
    }
}

package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.TrainGameResponseDTO;
import org.example.springboot.service.TrainGameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "调车小游戏关卡", description = "traingame 表")
@RequestMapping("/acupuncture/train-game")
@RestController
public class TrainGameController {

    @Resource
    private TrainGameService trainGameService;

    @Operation(summary = "调车小游戏关卡列表")
    @GetMapping("/list")
    public Result<List<TrainGameResponseDTO>> list() {
        return Result.success(trainGameService.listAll());
    }
}

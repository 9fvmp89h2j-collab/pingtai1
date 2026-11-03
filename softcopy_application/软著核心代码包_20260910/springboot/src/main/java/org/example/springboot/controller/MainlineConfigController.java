package org.example.springboot.controller;

import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.MainlineConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
public class MainlineConfigController {
    @Resource
    private MainlineConfigService mainlineConfigService;

    @GetMapping("/mainline-config")
    public Result<?> getPublicConfig() {
        return Result.success(mainlineConfigService.getPublicConfig());
    }
}

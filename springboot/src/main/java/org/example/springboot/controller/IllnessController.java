package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.IllnessResponseDTO;
import org.example.springboot.service.IllnessService;
import org.example.springboot.service.UserService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @Resource
    private UserService userService;

    @Operation(summary = "历练列表", description = "返回 illness 表所有小镇配置")
    @GetMapping("/list")
    public Result<List<IllnessResponseDTO>> list() {
        return Result.success(illnessService.listAll());
    }

    @Operation(summary = "历练通关发放对应疾病徽章")
    @PostMapping("/award/{illnessId}")
    public Result<List<String>> award(@PathVariable Integer illnessId) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error("请先登录");
        }
        return Result.success(userService.awardIllnessBadge(uid, illnessId));
    }
}


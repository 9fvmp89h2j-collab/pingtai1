package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.BackpackAddCommandDTO;
import org.example.springboot.dto.response.BackpackSkillItemResponseDTO;
import org.example.springboot.service.UserCollectService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户收藏", description = "收藏与取消收藏技能")
@RequestMapping("/user/collect")
@RestController
public class UserCollectController {

    @Resource
    private UserCollectService userCollectService;

    @Operation(summary = "我的收藏列表")
    @GetMapping
    public Result<List<BackpackSkillItemResponseDTO>> list() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(userCollectService.listMyCollect(userId));
    }

    @Operation(summary = "收藏技能")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody BackpackAddCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        userCollectService.addCollect(userId, dto.getSkillId());
        return Result.success("收藏成功", null);
    }

    @Operation(summary = "取消收藏技能")
    @PostMapping("/remove")
    public Result<Void> remove(@Valid @RequestBody BackpackAddCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        userCollectService.removeCollect(userId, dto.getSkillId());
        return Result.success("已取消收藏", null);
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/has")
    public Result<Boolean> has(@RequestParam Integer skillId) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(userCollectService.hasCollect(userId, skillId));
    }
}

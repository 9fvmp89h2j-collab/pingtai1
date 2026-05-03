package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.BackpackAddCommandDTO;
import org.example.springboot.dto.response.BackpackSkillItemResponseDTO;
import org.example.springboot.service.UserBackpackService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户技能背包
 */
@Tag(name = "技能背包", description = "收集与管理用户技能")
@RequestMapping("/user/backpack")
@RestController
@Slf4j
public class UserBackpackController {

    @Resource
    private UserBackpackService userBackpackService;

    @Operation(summary = "我的背包列表", description = "返回当前登录用户已收集的全部技能及详情")
    @GetMapping
    public Result<List<BackpackSkillItemResponseDTO>> list() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        List<BackpackSkillItemResponseDTO> list = userBackpackService.listMyBackpack(userId);
        return Result.success(list);
    }

    @Operation(summary = "收集技能", description = "将指定技能加入背包（故事关联的 skillId）")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody BackpackAddCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        userBackpackService.addSkill(userId, dto.getSkillId());
        return Result.success("收集成功", null);
    }
}

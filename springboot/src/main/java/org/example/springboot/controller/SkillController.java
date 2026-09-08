package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.SkillNameResponseDTO;
import org.example.springboot.service.SkillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "技能", description = "skills 表")
@RequestMapping("/skill")
@RestController
public class SkillController {

    @Resource
    private SkillService skillService;

    @Operation(summary = "按ID列表查询技能名称")
    @GetMapping("/names")
    public Result<List<SkillNameResponseDTO>> names(@RequestParam(name = "ids", required = false) String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        String[] arr = ids.split(",");
        List<Integer> parsed = new ArrayList<>();
        for (String s : arr) {
            try {
                parsed.add(Integer.parseInt(s.trim()));
            } catch (Exception ignored) {
            }
        }
        return Result.success(skillService.listNamesByIds(parsed));
    }

    @Operation(summary = "技能总数")
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(skillService.countAllSkills());
    }
}


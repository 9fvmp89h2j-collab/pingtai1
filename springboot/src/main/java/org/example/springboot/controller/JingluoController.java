package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.JingluoResponseDTO;
import org.example.springboot.service.JingluoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "经络", description = "jingluo 表")
@RequestMapping("/acupuncture/jingluo")
@RestController
@Slf4j
public class JingluoController {

    @Resource
    private JingluoService jingluoService;

    @Operation(summary = "全部经络列表", description = "用于经络页分类横向列表")
    @GetMapping("/list")
    public Result<List<JingluoResponseDTO>> list() {
        return Result.success(jingluoService.listAll());
    }

    @Operation(summary = "经络详情", description = "弹窗展示")
    @GetMapping("/{id}")
    public Result<JingluoResponseDTO> detail(@PathVariable("id") Integer id) {
        JingluoResponseDTO dto = jingluoService.getById(id);
        if (dto == null) {
            return Result.error("经络不存在");
        }
        return Result.success(dto);
    }
}

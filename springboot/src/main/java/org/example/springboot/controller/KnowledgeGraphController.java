package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.KnowledgeGraphAcupointDTO;
import org.example.springboot.service.KnowledgeGraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "针灸文化知识图谱", description = "面向小铜人中医侦探社的图谱查询接口")
@RestController
@RequestMapping("/acupuncture/knowledge-graph")
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @Operation(summary = "穴位图谱列表", description = "返回可用于 3D 小铜人热点展示的穴位、经络、区域、安全提示和任务关系")
    @GetMapping("/acupoints")
    public Result<List<KnowledgeGraphAcupointDTO>> listAcupoints() {
        return Result.success(knowledgeGraphService.listAcupoints());
    }

    @Operation(summary = "穴位图谱详情", description = "按图谱 id 或穴位名称查询单个穴位关联信息")
    @GetMapping("/acupoints/{id}")
    public Result<KnowledgeGraphAcupointDTO> getAcupoint(@PathVariable String id) {
        KnowledgeGraphAcupointDTO acupoint = knowledgeGraphService.getAcupoint(id);
        if (acupoint == null) {
            return Result.error("穴位图谱节点不存在");
        }
        return Result.success(acupoint);
    }
}

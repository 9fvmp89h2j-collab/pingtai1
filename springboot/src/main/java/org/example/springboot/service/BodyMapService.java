package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.BodyMapAcupointResponseDTO;
import org.example.springboot.entity.AcupointKnowledge;
import org.example.springboot.entity.BodyMapAcupointKnowledge;
import org.example.springboot.mapper.AcupointKnowledgeMapper;
import org.example.springboot.mapper.BodyMapAcupointKnowledgeMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BodyMapService {
    @Resource
    private BodyMapAcupointKnowledgeMapper bodyMapKnowledgeMapper;

    @Resource
    private AcupointKnowledgeMapper acupointKnowledgeMapper;

    public List<BodyMapAcupointResponseDTO> listAcupoints() {
        List<BodyMapAcupointKnowledge> mapKnowledge = bodyMapKnowledgeMapper.selectList(
                new LambdaQueryWrapper<BodyMapAcupointKnowledge>()
                        .eq(BodyMapAcupointKnowledge::getEnabled, true)
                        .orderByAsc(BodyMapAcupointKnowledge::getSortOrder));
        if (mapKnowledge.isEmpty()) {
            return List.of();
        }

        Map<String, AcupointKnowledge> acupoints = acupointKnowledgeMapper
                .selectBatchIds(mapKnowledge.stream().map(BodyMapAcupointKnowledge::getAcupointCode).toList())
                .stream()
                .filter(point -> Boolean.TRUE.equals(point.getEnabled()))
                .collect(Collectors.toMap(AcupointKnowledge::getCode, Function.identity()));

        return mapKnowledge.stream()
                .filter(item -> acupoints.containsKey(item.getAcupointCode()))
                .map(item -> toDto(acupoints.get(item.getAcupointCode()), item))
                .toList();
    }

    private BodyMapAcupointResponseDTO toDto(AcupointKnowledge point, BodyMapAcupointKnowledge mapKnowledge) {
        return BodyMapAcupointResponseDTO.builder()
                .code(point.getCode())
                .pointNumber(point.getPointNumber())
                .name(point.getName())
                .pinyin(point.getPinyin())
                .meridianCode(point.getMeridianCode())
                .meridianName(point.getMeridianName())
                .bodyArea(point.getBodyArea())
                .regionCode(mapKnowledge.getRegionCode())
                .bodyView(mapKnowledge.getBodyView())
                .placementHint(mapKnowledge.getPlacementHint())
                .childMapDescription(mapKnowledge.getChildMapDescription())
                .childDescription(point.getChildDescription())
                .childTraditionalUse(point.getChildTraditionalUse())
                .safetyTip(point.getSafetyTip())
                .traditionalUseSourceName(point.getTraditionalUseSourceName())
                .traditionalUseSourceLink(point.getTraditionalUseSourceLink())
                .build();
    }
}

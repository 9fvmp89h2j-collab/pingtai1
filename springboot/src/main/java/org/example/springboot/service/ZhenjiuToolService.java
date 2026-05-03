package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.ZhenjiuToolResponseDTO;
import org.example.springboot.entity.ZhenjiuTool;
import org.example.springboot.mapper.ZhenjiuToolMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZhenjiuToolService {

    @Resource
    private ZhenjiuToolMapper zhenjiuToolMapper;

    public List<ZhenjiuToolResponseDTO> listAll() {
        LambdaQueryWrapper<ZhenjiuTool> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ZhenjiuTool::getToolsId);
        return zhenjiuToolMapper.selectList(w).stream().map(this::toDto).collect(Collectors.toList());
    }

    private ZhenjiuToolResponseDTO toDto(ZhenjiuTool e) {
        ZhenjiuToolResponseDTO dto = new ZhenjiuToolResponseDTO();
        dto.setToolsId(e.getToolsId());
        dto.setToolsName(e.getToolsName());
        dto.setToolsPic1(e.getToolsPic1());
        dto.setToolsPic2(e.getToolsPic2());
        dto.setToolsPic3(e.getToolsPic3());
        dto.setToolsBrief(e.getToolsBrief());
        dto.setToolsTitle1(e.getToolsTitle1());
        dto.setToolsTitle2(e.getToolsTitle2());
        dto.setToolsTitle3(e.getToolsTitle3());
        dto.setToolsText1(e.getToolsText1());
        dto.setToolsText2(e.getToolsText2());
        dto.setToolsText3(e.getToolsText3());
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}

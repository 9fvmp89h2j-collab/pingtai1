package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.ZhenfaAdminCommandDTO;
import org.example.springboot.dto.response.ZhenjiuToolResponseDTO;
import org.example.springboot.entity.ZhenjiuTool;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.ZhenjiuToolMapper;
import org.springframework.stereotype.Service;

@Service
public class ZhenfaAdminService {

    @Resource
    private ZhenjiuToolMapper zhenjiuToolMapper;

    public Page<ZhenjiuToolResponseDTO> page(Long current, Long size, String toolsName, Integer skillId) {
        LambdaQueryWrapper<ZhenjiuTool> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(toolsName)) {
            w.like(ZhenjiuTool::getToolsName, toolsName);
        }
        if (skillId != null) {
            w.eq(ZhenjiuTool::getSkillId, skillId);
        }
        w.orderByDesc(ZhenjiuTool::getToolsId);

        Page<ZhenjiuTool> page = zhenjiuToolMapper.selectPage(new Page<>(current, size), w);
        Page<ZhenjiuToolResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public ZhenjiuToolResponseDTO getById(Integer id) {
        ZhenjiuTool e = zhenjiuToolMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("针法不存在");
        }
        return toDTO(e);
    }

    public ZhenjiuToolResponseDTO create(ZhenfaAdminCommandDTO dto) {
        ZhenjiuTool e = new ZhenjiuTool();
        apply(e, dto);
        zhenjiuToolMapper.insert(e);
        return toDTO(e);
    }

    public ZhenjiuToolResponseDTO update(Integer id, ZhenfaAdminCommandDTO dto) {
        ZhenjiuTool e = zhenjiuToolMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("针法不存在");
        }
        apply(e, dto);
        zhenjiuToolMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Integer id) {
        zhenjiuToolMapper.deleteById(id);
    }

    private void apply(ZhenjiuTool e, ZhenfaAdminCommandDTO dto) {
        e.setToolsName(dto.getToolsName());
        e.setToolsBrief(dto.getToolsBrief());
        e.setToolsTitle1(dto.getToolsTitle1());
        e.setToolsText1(dto.getToolsText1());
        e.setToolsTitle2(dto.getToolsTitle2());
        e.setToolsText2(dto.getToolsText2());
        e.setToolsTitle3(dto.getToolsTitle3());
        e.setToolsText3(dto.getToolsText3());
        e.setSkillId(dto.getSkillId());
        e.setToolsPic1(dto.getToolsPic1());
        e.setToolsPic2(dto.getToolsPic2());
        e.setToolsPic3(dto.getToolsPic3());
    }

    private ZhenjiuToolResponseDTO toDTO(ZhenjiuTool e) {
        ZhenjiuToolResponseDTO dto = new ZhenjiuToolResponseDTO();
        dto.setToolsId(e.getToolsId());
        dto.setToolsName(e.getToolsName());
        dto.setToolsBrief(e.getToolsBrief());
        dto.setToolsTitle1(e.getToolsTitle1());
        dto.setToolsText1(e.getToolsText1());
        dto.setToolsTitle2(e.getToolsTitle2());
        dto.setToolsText2(e.getToolsText2());
        dto.setToolsTitle3(e.getToolsTitle3());
        dto.setToolsText3(e.getToolsText3());
        dto.setSkillId(e.getSkillId());
        dto.setToolsPic1(e.getToolsPic1());
        dto.setToolsPic2(e.getToolsPic2());
        dto.setToolsPic3(e.getToolsPic3());
        return dto;
    }
}


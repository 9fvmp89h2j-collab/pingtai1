package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.SkillAdminCreateCommandDTO;
import org.example.springboot.dto.command.SkillAdminUpdateCommandDTO;
import org.example.springboot.dto.response.SkillResponseDTO;
import org.example.springboot.entity.Skill;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.SkillMapper;
import org.springframework.stereotype.Service;

@Service
public class SkillAdminService {

    @Resource
    private SkillMapper skillMapper;

    public Page<SkillResponseDTO> page(Long current, Long size, String skillName, String skillCategory, String skillType) {
        LambdaQueryWrapper<Skill> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(skillName)) {
            w.like(Skill::getSkillName, skillName);
        }
        if (StrUtil.isNotBlank(skillCategory)) {
            w.like(Skill::getSkillCategory, skillCategory);
        }
        if (StrUtil.isNotBlank(skillType)) {
            w.like(Skill::getSkillType, skillType);
        }
        w.orderByDesc(Skill::getSkillId);

        Page<Skill> page = skillMapper.selectPage(new Page<>(current, size), w);
        Page<SkillResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public SkillResponseDTO getById(Integer id) {
        Skill e = skillMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("技能不存在");
        }
        return toDTO(e);
    }

    public SkillResponseDTO create(SkillAdminCreateCommandDTO dto) {
        if (dto.getSkillId() == null) {
            throw new BusinessException("skillid 不能为空");
        }
        Skill exists = skillMapper.selectById(dto.getSkillId());
        if (exists != null) {
            throw new BusinessException("skillid 已存在");
        }
        Skill e = new Skill();
        e.setSkillId(dto.getSkillId());
        apply(e, dto);
        skillMapper.insert(e);
        return toDTO(e);
    }

    public SkillResponseDTO update(Integer id, SkillAdminUpdateCommandDTO dto) {
        Skill e = skillMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("技能不存在");
        }
        apply(e, dto);
        skillMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Integer id) {
        skillMapper.deleteById(id);
    }

    private void apply(Skill e, SkillAdminCreateCommandDTO dto) {
        e.setSkillName(dto.getSkillName());
        e.setSkillBriefDescription(dto.getSkillBriefDescription());
        e.setSkillDescription(dto.getSkillDescription());
        e.setSkillPic(dto.getSkillPic());
        e.setSkillCategory(dto.getSkillCategory());
        e.setSkillScore(dto.getSkillScore());
        e.setSkillType(dto.getSkillType());
    }

    private void apply(Skill e, SkillAdminUpdateCommandDTO dto) {
        e.setSkillName(dto.getSkillName());
        e.setSkillBriefDescription(dto.getSkillBriefDescription());
        e.setSkillDescription(dto.getSkillDescription());
        e.setSkillPic(dto.getSkillPic());
        e.setSkillCategory(dto.getSkillCategory());
        e.setSkillScore(dto.getSkillScore());
        e.setSkillType(dto.getSkillType());
    }

    private SkillResponseDTO toDTO(Skill e) {
        SkillResponseDTO dto = new SkillResponseDTO();
        dto.setSkillId(e.getSkillId());
        dto.setSkillName(e.getSkillName());
        dto.setSkillBriefDescription(e.getSkillBriefDescription());
        dto.setSkillDescription(e.getSkillDescription());
        dto.setSkillPic(e.getSkillPic());
        dto.setSkillCategory(e.getSkillCategory());
        dto.setSkillScore(e.getSkillScore());
        dto.setSkillType(e.getSkillType());
        return dto;
    }
}


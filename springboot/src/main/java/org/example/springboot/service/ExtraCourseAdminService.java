package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.ExtraCourseAdminCommandDTO;
import org.example.springboot.dto.response.ExtraCourseResponseDTO;
import org.example.springboot.entity.ExtraCourse;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.ExtraCourseMapper;
import org.springframework.stereotype.Service;

@Service
public class ExtraCourseAdminService {

    @Resource
    private ExtraCourseMapper extraCourseMapper;

    public Page<ExtraCourseResponseDTO> page(Long current, Long size, String name, Integer skillId) {
        LambdaQueryWrapper<ExtraCourse> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            w.like(ExtraCourse::getExtraCourseName, name);
        }
        if (skillId != null) {
            w.eq(ExtraCourse::getSkillId, skillId);
        }
        w.orderByDesc(ExtraCourse::getExtraCourseId);

        Page<ExtraCourse> page = extraCourseMapper.selectPage(new Page<>(current, size), w);
        Page<ExtraCourseResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public ExtraCourseResponseDTO getById(Integer id) {
        ExtraCourse e = extraCourseMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("拓展疗法不存在");
        }
        return toDTO(e);
    }

    public ExtraCourseResponseDTO create(ExtraCourseAdminCommandDTO dto) {
        ExtraCourse e = new ExtraCourse();
        apply(e, dto);
        extraCourseMapper.insert(e);
        return toDTO(e);
    }

    public ExtraCourseResponseDTO update(Integer id, ExtraCourseAdminCommandDTO dto) {
        ExtraCourse e = extraCourseMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("拓展疗法不存在");
        }
        apply(e, dto);
        extraCourseMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Integer id) {
        extraCourseMapper.deleteById(id);
    }

    private void apply(ExtraCourse e, ExtraCourseAdminCommandDTO dto) {
        e.setExtraCourseName(dto.getExtraCourseName());
        e.setExtraCourseBrief(dto.getExtraCourseBrief());
        e.setExtraCourseDes(dto.getExtraCourseDes());
        e.setExtraCourseIcon(dto.getExtraCourseIcon());
        e.setExtraCoursePic1(dto.getExtraCoursePic1());
        e.setExtraCoursePic2(dto.getExtraCoursePic2());
        e.setExtraCoursePic3(dto.getExtraCoursePic3());
        e.setSkillId(dto.getSkillId());
    }

    private ExtraCourseResponseDTO toDTO(ExtraCourse e) {
        ExtraCourseResponseDTO dto = new ExtraCourseResponseDTO();
        dto.setExtraCourseId(e.getExtraCourseId());
        dto.setExtraCourseName(e.getExtraCourseName());
        dto.setExtraCourseBrief(e.getExtraCourseBrief());
        dto.setExtraCourseDes(e.getExtraCourseDes());
        dto.setExtraCourseIcon(e.getExtraCourseIcon());
        dto.setExtraCoursePic1(e.getExtraCoursePic1());
        dto.setExtraCoursePic2(e.getExtraCoursePic2());
        dto.setExtraCoursePic3(e.getExtraCoursePic3());
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}


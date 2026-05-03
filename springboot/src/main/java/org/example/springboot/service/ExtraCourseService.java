package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.ExtraCourseResponseDTO;
import org.example.springboot.entity.ExtraCourse;
import org.example.springboot.mapper.ExtraCourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExtraCourseService {

    @Resource
    private ExtraCourseMapper extraCourseMapper;

    public List<ExtraCourseResponseDTO> listAll() {
        LambdaQueryWrapper<ExtraCourse> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ExtraCourse::getExtraCourseId);
        return extraCourseMapper.selectList(w).stream().map(this::toDto).collect(Collectors.toList());
    }

    public ExtraCourseResponseDTO getById(Integer id) {
        if (id == null) return null;
        ExtraCourse e = extraCourseMapper.selectById(id);
        return e == null ? null : toDto(e);
    }

    private ExtraCourseResponseDTO toDto(ExtraCourse e) {
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


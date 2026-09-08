package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.response.DoctorStoryResponseDTO;
import org.example.springboot.entity.DoctorStory;
import org.example.springboot.mapper.DoctorStoryMapper;
import org.example.springboot.util.DoctorStoryGlossaryJsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorStoryService {

    @Resource
    private DoctorStoryMapper doctorStoryMapper;

    public List<DoctorStoryResponseDTO> listAll() {
        LambdaQueryWrapper<DoctorStory> w = new LambdaQueryWrapper<>();
        w.orderByAsc(DoctorStory::getId);
        List<DoctorStory> list = doctorStoryMapper.selectList(w);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private DoctorStoryResponseDTO toDto(DoctorStory e) {
        DoctorStoryResponseDTO dto = new DoctorStoryResponseDTO();
        dto.setId(e.getId());
        dto.setDoctorName(e.getDoctorName());
        dto.setDoctorBrief(e.getDoctorBrief());
        dto.setDoctorDetail(e.getDoctorDetail());
        dto.setDoctorPic1(e.getDoctorPic1());
        dto.setDoctorPic2(e.getDoctorPic2());
        dto.setDoctorPic3(e.getDoctorPic3());
        dto.setMedia(e.getMedia());
        dto.setPreviewPic(e.getPreviewPic());
        dto.setReadingGlossary(DoctorStoryGlossaryJsonUtil.parse(e.getReadingGlossaryJson()));
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}

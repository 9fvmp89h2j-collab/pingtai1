package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.response.OriginStoryResponseDTO;
import org.example.springboot.entity.OriginStory;
import org.example.springboot.mapper.OriginStoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 针灸起源故事
 */
@Slf4j
@Service
public class OriginStoryService {

    @Resource
    private OriginStoryMapper originStoryMapper;

    public List<OriginStoryResponseDTO> listAll() {
        LambdaQueryWrapper<OriginStory> w = new LambdaQueryWrapper<>();
        w.orderByAsc(OriginStory::getId);
        List<OriginStory> list = originStoryMapper.selectList(w);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private OriginStoryResponseDTO toDto(OriginStory e) {
        OriginStoryResponseDTO dto = new OriginStoryResponseDTO();
        dto.setId(e.getId());
        dto.setStoryTitle(e.getStoryTitle());
        dto.setStorySubtitle(e.getStorySubtitle());
        dto.setStoryText(e.getStoryText());
        dto.setStoryPic1(e.getStoryPic1());
        dto.setStoryPic2(e.getStoryPic2());
        dto.setStoryPic3(e.getStoryPic3());
        dto.setMedia(e.getMedia());
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}

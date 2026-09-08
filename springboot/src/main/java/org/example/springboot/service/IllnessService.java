package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.IllnessResponseDTO;
import org.example.springboot.entity.Illness;
import org.example.springboot.mapper.IllnessMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IllnessService {

    @Resource
    private IllnessMapper illnessMapper;

    public List<IllnessResponseDTO> listAll() {
        LambdaQueryWrapper<Illness> w = new LambdaQueryWrapper<>();
        w.orderByAsc(Illness::getIllnessid);
        return illnessMapper.selectList(w).stream().map(this::toDto).collect(Collectors.toList());
    }

    private IllnessResponseDTO toDto(Illness e) {
        IllnessResponseDTO dto = new IllnessResponseDTO();
        dto.setIllnessid(e.getIllnessid());
        dto.setCowtown(e.getCowtown());
        dto.setIllnessname(e.getIllnessname());
        dto.setIllnessfeature(e.getIllnessfeature());
        dto.setIllnesspic(e.getIllnesspic());
        dto.setXueweicount(e.getXueweicount());
        dto.setToolscount(e.getToolscount());
        dto.setXuewei1(e.getXuewei1());
        dto.setXuewei2(e.getXuewei2());
        dto.setXuewei3(e.getXuewei3());
        dto.setXuewei4(e.getXuewei4());
        dto.setXuewei5(e.getXuewei5());
        dto.setTools1(e.getTools1());
        dto.setTools2(e.getTools2());
        dto.setTools3(e.getTools3());
        dto.setTools4(e.getTools4());
        dto.setIllnessbadgename(e.getIllnessbadgename());
        dto.setIllnessbadgepath(e.getIllnessbadgepath());
        return dto;
    }
}


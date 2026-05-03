package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.TrainGameResponseDTO;
import org.example.springboot.entity.TrainGame;
import org.example.springboot.mapper.TrainGameMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainGameService {

    @Resource
    private TrainGameMapper trainGameMapper;

    public List<TrainGameResponseDTO> listAll() {
        LambdaQueryWrapper<TrainGame> w = new LambdaQueryWrapper<>();
        w.orderByAsc(TrainGame::getId);
        return trainGameMapper.selectList(w).stream().map(this::toDto).collect(Collectors.toList());
    }

    private TrainGameResponseDTO toDto(TrainGame e) {
        TrainGameResponseDTO dto = new TrainGameResponseDTO();
        dto.setId(e.getId());
        dto.setJingluoName(e.getJingluoName());
        dto.setGame1(e.getGame1());
        dto.setGame2(e.getGame2());
        dto.setGame3(e.getGame3());
        dto.setGame4(e.getGame4());
        dto.setGame5(e.getGame5());
        dto.setGame1Brief(e.getGame1Brief());
        dto.setGame2Brief(e.getGame2Brief());
        dto.setGame3Brief(e.getGame3Brief());
        dto.setGame4Brief(e.getGame4Brief());
        dto.setGame5Brief(e.getGame5Brief());
        return dto;
    }
}

package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import org.example.springboot.entity.Misconception;
import org.example.springboot.mapper.MisconceptionMapper;
import org.example.springboot.dto.command.MisconceptionCreateCommandDTO;
import org.example.springboot.dto.response.MisconceptionResponseDTO;

@Slf4j
@Service
public class MisconceptionService {

    @Resource
    private MisconceptionMapper misconceptionMapper;

    public List<MisconceptionResponseDTO> listAll() {
        LambdaQueryWrapper<Misconception> w = new LambdaQueryWrapper<>();
        w.orderByAsc(Misconception::getSortOrder).orderByDesc(Misconception::getCreateTime);
        List<Misconception> list = misconceptionMapper.selectList(w);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MisconceptionResponseDTO getById(Long id) {
        Misconception e = misconceptionMapper.selectById(id);
        if (e == null) return null;
        return toDTO(e);
    }

    public MisconceptionResponseDTO create(MisconceptionCreateCommandDTO dto) {
        Misconception e = Misconception.builder()
                .question(dto.getQuestion())
                .answer(dto.getAnswer() != null ? dto.getAnswer() : "")
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        misconceptionMapper.insert(e);
        return toDTO(e);
    }

    private MisconceptionResponseDTO toDTO(Misconception e) {
        MisconceptionResponseDTO dto = new MisconceptionResponseDTO();
        dto.setId(e.getId());
        dto.setQuestion(e.getQuestion());
        dto.setAnswer(e.getAnswer());
        dto.setSortOrder(e.getSortOrder());
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}

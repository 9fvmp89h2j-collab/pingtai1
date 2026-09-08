package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.JingluoResponseDTO;
import org.example.springboot.entity.Jingluo;
import org.example.springboot.mapper.JingluoMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JingluoService {

    @Resource
    private JingluoMapper jingluoMapper;

    public List<JingluoResponseDTO> listAll() {
        LambdaQueryWrapper<Jingluo> w = new LambdaQueryWrapper<>();
        w.orderByAsc(Jingluo::getJingluoCatagory).orderByAsc(Jingluo::getJingluoId);
        return jingluoMapper.selectList(w).stream().map(this::toDto).collect(Collectors.toList());
    }

    public JingluoResponseDTO getById(Integer id) {
        if (id == null) {
            return null;
        }
        Jingluo e = jingluoMapper.selectById(id);
        return e == null ? null : toDto(e);
    }

    private JingluoResponseDTO toDto(Jingluo e) {
        JingluoResponseDTO dto = new JingluoResponseDTO();
        dto.setJingluoId(e.getJingluoId());
        dto.setJingluoName(e.getJingluoName());
        dto.setJingluoCatagory(e.getJingluoCatagory());
        dto.setJingluoOrder(e.getJingluoOrder());
        dto.setJingluoPic(e.getJingluoPic());
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}

package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.XueweiResponseDTO;
import org.example.springboot.entity.Xuewei;
import org.example.springboot.mapper.XueweiMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class XueweiService {

    @Resource
    private XueweiMapper xueweiMapper;

    public List<XueweiResponseDTO> listAll() {
        LambdaQueryWrapper<Xuewei> w = new LambdaQueryWrapper<>();
        w.orderByAsc(Xuewei::getXueweiCatagory).orderByAsc(Xuewei::getXueweiId);
        return xueweiMapper.selectList(w).stream().map(this::toDto).collect(Collectors.toList());
    }

    public XueweiResponseDTO getById(Long id) {
        if (id == null) {
            return null;
        }
        Xuewei e = xueweiMapper.selectById(id);
        return e == null ? null : toDto(e);
    }

    private XueweiResponseDTO toDto(Xuewei e) {
        XueweiResponseDTO dto = new XueweiResponseDTO();
        dto.setXueweiId(e.getXueweiId());
        dto.setXueweiName(e.getXueweiName());
        dto.setXueweiCatagory(e.getXueweiCatagory());
        dto.setPosition(e.getPosition());
        dto.setIllness(e.getIllness());
        dto.setXueweiPic1(e.getXueweiPic1());
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}

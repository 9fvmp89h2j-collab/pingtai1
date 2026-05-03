package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.XueweiAdminCommandDTO;
import org.example.springboot.dto.response.XueweiResponseDTO;
import org.example.springboot.entity.Xuewei;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.XueweiMapper;
import org.springframework.stereotype.Service;

@Service
public class XueweiAdminService {

    @Resource
    private XueweiMapper xueweiMapper;

    public Page<XueweiResponseDTO> page(Long current, Long size, String xueweiName, String xueweiCatagory, Integer skillId) {
        LambdaQueryWrapper<Xuewei> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(xueweiName)) {
            w.like(Xuewei::getXueweiName, xueweiName);
        }
        if (StrUtil.isNotBlank(xueweiCatagory)) {
            w.like(Xuewei::getXueweiCatagory, xueweiCatagory);
        }
        if (skillId != null) {
            w.eq(Xuewei::getSkillId, skillId);
        }
        w.orderByDesc(Xuewei::getXueweiId);

        Page<Xuewei> page = xueweiMapper.selectPage(new Page<>(current, size), w);
        Page<XueweiResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public XueweiResponseDTO getById(Long id) {
        Xuewei e = xueweiMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("腧穴不存在");
        }
        return toDTO(e);
    }

    public XueweiResponseDTO create(XueweiAdminCommandDTO dto) {
        Xuewei e = new Xuewei();
        e.setXueweiName(dto.getXueweiName());
        e.setXueweiCatagory(dto.getXueweiCatagory());
        e.setPosition(dto.getPosition());
        e.setIllness(dto.getIllness());
        e.setXueweiPic1(dto.getXueweiPic1());
        e.setSkillId(dto.getSkillId());
        xueweiMapper.insert(e);
        return toDTO(e);
    }

    public XueweiResponseDTO update(Long id, XueweiAdminCommandDTO dto) {
        Xuewei e = xueweiMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("腧穴不存在");
        }
        e.setXueweiName(dto.getXueweiName());
        e.setXueweiCatagory(dto.getXueweiCatagory());
        e.setPosition(dto.getPosition());
        e.setIllness(dto.getIllness());
        e.setXueweiPic1(dto.getXueweiPic1());
        e.setSkillId(dto.getSkillId());
        xueweiMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Long id) {
        xueweiMapper.deleteById(id);
    }

    private XueweiResponseDTO toDTO(Xuewei e) {
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


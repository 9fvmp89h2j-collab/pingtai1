package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.JingluoAdminCommandDTO;
import org.example.springboot.dto.response.JingluoResponseDTO;
import org.example.springboot.entity.Jingluo;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.JingluoMapper;
import org.springframework.stereotype.Service;

@Service
public class JingluoAdminService {

    @Resource
    private JingluoMapper jingluoMapper;

    public Page<JingluoResponseDTO> page(Long current, Long size, String jingluoName, String jingluoCatagory, Integer skillId) {
        LambdaQueryWrapper<Jingluo> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(jingluoName)) {
            w.like(Jingluo::getJingluoName, jingluoName);
        }
        if (StrUtil.isNotBlank(jingluoCatagory)) {
            w.like(Jingluo::getJingluoCatagory, jingluoCatagory);
        }
        if (skillId != null) {
            w.eq(Jingluo::getSkillId, skillId);
        }
        w.orderByDesc(Jingluo::getJingluoId);

        Page<Jingluo> page = jingluoMapper.selectPage(new Page<>(current, size), w);
        Page<JingluoResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public JingluoResponseDTO getById(Integer id) {
        Jingluo e = jingluoMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("经络不存在");
        }
        return toDTO(e);
    }

    public JingluoResponseDTO create(JingluoAdminCommandDTO dto) {
        Jingluo e = new Jingluo();
        e.setJingluoName(dto.getJingluoName());
        e.setJingluoCatagory(dto.getJingluoCatagory());
        e.setJingluoOrder(dto.getJingluoOrder());
        e.setIllness(dto.getIllness());
        e.setJingluoPic(dto.getJingluoPic());
        e.setSkillId(dto.getSkillId());
        jingluoMapper.insert(e);
        return toDTO(e);
    }

    public JingluoResponseDTO update(Integer id, JingluoAdminCommandDTO dto) {
        Jingluo e = jingluoMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("经络不存在");
        }
        e.setJingluoName(dto.getJingluoName());
        e.setJingluoCatagory(dto.getJingluoCatagory());
        e.setJingluoOrder(dto.getJingluoOrder());
        e.setIllness(dto.getIllness());
        e.setJingluoPic(dto.getJingluoPic());
        e.setSkillId(dto.getSkillId());
        jingluoMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Integer id) {
        jingluoMapper.deleteById(id);
    }

    private JingluoResponseDTO toDTO(Jingluo e) {
        JingluoResponseDTO dto = new JingluoResponseDTO();
        dto.setJingluoId(e.getJingluoId());
        dto.setJingluoName(e.getJingluoName());
        dto.setJingluoCatagory(e.getJingluoCatagory());
        dto.setJingluoOrder(e.getJingluoOrder());
        dto.setIllness(e.getIllness());
        dto.setJingluoPic(e.getJingluoPic());
        dto.setSkillId(e.getSkillId());
        return dto;
    }
}

package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.IllnessAdminCommandDTO;
import org.example.springboot.dto.response.IllnessResponseDTO;
import org.example.springboot.entity.Illness;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.IllnessMapper;
import org.springframework.stereotype.Service;

@Service
public class IllnessAdminService {

    @Resource
    private IllnessMapper illnessMapper;

    public Page<IllnessResponseDTO> page(Long current, Long size, String cowtown, String illnessname) {
        LambdaQueryWrapper<Illness> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(cowtown)) {
            w.like(Illness::getCowtown, cowtown);
        }
        if (StrUtil.isNotBlank(illnessname)) {
            w.like(Illness::getIllnessname, illnessname);
        }
        w.orderByDesc(Illness::getIllnessid);

        Page<Illness> page = illnessMapper.selectPage(new Page<>(current, size), w);
        Page<IllnessResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public IllnessResponseDTO getById(Integer id) {
        Illness e = illnessMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("疾病不存在");
        }
        return toDTO(e);
    }

    public IllnessResponseDTO create(IllnessAdminCommandDTO dto) {
        Illness e = new Illness();
        apply(e, dto);
        illnessMapper.insert(e);
        return toDTO(e);
    }

    public IllnessResponseDTO update(Integer id, IllnessAdminCommandDTO dto) {
        Illness e = illnessMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("疾病不存在");
        }
        apply(e, dto);
        illnessMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Integer id) {
        illnessMapper.deleteById(id);
    }

    private void apply(Illness e, IllnessAdminCommandDTO dto) {
        e.setCowtown(dto.getCowtown());
        e.setIllnessname(dto.getIllnessname());
        e.setIllnessfeature(dto.getIllnessfeature());
        e.setIllnesspic(dto.getIllnesspic());
        e.setXueweicount(dto.getXueweicount());
        e.setToolscount(dto.getToolscount());
        e.setXuewei1(dto.getXuewei1());
        e.setXuewei2(dto.getXuewei2());
        e.setXuewei3(dto.getXuewei3());
        e.setXuewei4(dto.getXuewei4());
        e.setXuewei5(dto.getXuewei5());
        e.setTools1(dto.getTools1());
        e.setTools2(dto.getTools2());
        e.setTools3(dto.getTools3());
        e.setTools4(dto.getTools4());
    }

    private IllnessResponseDTO toDTO(Illness e) {
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
        return dto;
    }
}


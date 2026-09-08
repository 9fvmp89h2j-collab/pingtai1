package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.command.OriginStoryAdminCommandDTO;
import org.example.springboot.dto.response.OriginStoryResponseDTO;
import org.example.springboot.entity.OriginStory;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.OriginStoryMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OriginStoryAdminService {

    @Resource
    private OriginStoryMapper originStoryMapper;

    public Page<OriginStoryResponseDTO> page(Long current, Long size, String title, Long skillId) {
        LambdaQueryWrapper<OriginStory> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(title)) {
            w.like(OriginStory::getStoryTitle, title);
        }
        if (skillId != null) {
            w.eq(OriginStory::getSkillId, skillId);
        }
        w.orderByDesc(OriginStory::getId);

        Page<OriginStory> page = originStoryMapper.selectPage(new Page<>(current, size), w);
        Page<OriginStoryResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDto).toList());
        return dtoPage;
    }

    public OriginStoryResponseDTO create(OriginStoryAdminCommandDTO dto) {
        OriginStory e = new OriginStory();
        e.setStoryTitle(dto.getStoryTitle());
        e.setStorySubtitle(dto.getStorySubtitle());
        e.setStoryText(dto.getStoryText());
        e.setStoryPic1(dto.getStoryPic1());
        e.setStoryPic2(dto.getStoryPic2());
        e.setStoryPic3(dto.getStoryPic3());
        e.setMedia(dto.getMedia());
        e.setSkillId(dto.getSkillId());
        originStoryMapper.insert(e);
        return toDto(e);
    }

    public OriginStoryResponseDTO update(Long id, OriginStoryAdminCommandDTO dto) {
        OriginStory e = originStoryMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("开始页故事不存在");
        }
        e.setStoryTitle(dto.getStoryTitle());
        e.setStorySubtitle(dto.getStorySubtitle());
        e.setStoryText(dto.getStoryText());
        e.setStoryPic1(dto.getStoryPic1());
        e.setStoryPic2(dto.getStoryPic2());
        e.setStoryPic3(dto.getStoryPic3());
        e.setMedia(dto.getMedia());
        e.setSkillId(dto.getSkillId());
        originStoryMapper.updateById(e);
        return toDto(e);
    }

    public void delete(Long id) {
        originStoryMapper.deleteById(id);
    }

    public OriginStoryResponseDTO getById(Long id) {
        OriginStory e = originStoryMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("开始页故事不存在");
        }
        return toDto(e);
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


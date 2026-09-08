package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.DoctorStoryGlossaryItemDTO;
import org.example.springboot.dto.command.DoctorStoryAdminCommandDTO;
import org.example.springboot.dto.response.DoctorStoryResponseDTO;
import org.example.springboot.entity.DoctorStory;
import org.example.springboot.entity.Skill;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.DoctorStoryMapper;
import org.example.springboot.mapper.SkillMapper;
import org.example.springboot.util.DoctorStoryGlossaryJsonUtil;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DoctorStoryAdminService {

    @Resource
    private DoctorStoryMapper doctorStoryMapper;
    @Resource
    private SkillMapper skillMapper;

    public Page<DoctorStoryResponseDTO> page(Long current, Long size, String doctorName, Integer skillId) {
        LambdaQueryWrapper<DoctorStory> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(doctorName)) {
            w.like(DoctorStory::getDoctorName, doctorName);
        }
        if (skillId != null) {
            w.eq(DoctorStory::getSkillId, skillId);
        }
        w.orderByDesc(DoctorStory::getId);

        Page<DoctorStory> page = doctorStoryMapper.selectPage(new Page<>(current, size), w);
        Page<DoctorStoryResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public DoctorStoryResponseDTO getById(Long id) {
        DoctorStory e = doctorStoryMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("针灸名医故事不存在");
        }
        return toDTO(e);
    }

    public DoctorStoryResponseDTO create(DoctorStoryAdminCommandDTO dto) {
        String doctorName = StrUtil.trimToNull(dto.getDoctorName());
        if (doctorName == null) {
            throw new BusinessException("故事名称不能为空");
        }
        String doctorDetail = StrUtil.trimToNull(dto.getDoctorDetail());
        validateDoctorDetailLength(doctorDetail);
        Integer skillId = normalizeSkillId(dto.getSkillId());
        validateSkillExists(skillId);
        List<DoctorStoryGlossaryItemDTO> readingGlossary = DoctorStoryGlossaryJsonUtil.normalize(dto.getReadingGlossary());

        DoctorStory e = new DoctorStory();
        e.setDoctorName(doctorName);
        e.setDoctorBrief(StrUtil.trimToNull(dto.getDoctorBrief()));
        e.setDoctorDetail(doctorDetail);
        e.setSkillId(skillId);
        e.setDoctorPic1(StrUtil.trimToNull(dto.getDoctorPic1()));
        e.setDoctorPic2(StrUtil.trimToNull(dto.getDoctorPic2()));
        e.setDoctorPic3(StrUtil.trimToNull(dto.getDoctorPic3()));
        e.setMedia(StrUtil.trimToNull(dto.getMedia()));
        e.setPreviewPic(StrUtil.trimToNull(dto.getPreviewPic()));
        e.setReadingGlossaryJson(DoctorStoryGlossaryJsonUtil.toJson(readingGlossary));
        doctorStoryMapper.insert(e);
        return toDTO(e);
    }

    public DoctorStoryResponseDTO update(Long id, DoctorStoryAdminCommandDTO dto) {
        DoctorStory e = doctorStoryMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("针灸名医故事不存在");
        }
        String doctorName = StrUtil.trimToNull(dto.getDoctorName());
        if (doctorName == null) {
            throw new BusinessException("故事名称不能为空");
        }
        String doctorDetail = StrUtil.trimToNull(dto.getDoctorDetail());
        validateDoctorDetailLength(doctorDetail);
        Integer skillId = normalizeSkillId(dto.getSkillId());
        validateSkillExists(skillId);
        List<DoctorStoryGlossaryItemDTO> readingGlossary = DoctorStoryGlossaryJsonUtil.normalize(dto.getReadingGlossary());

        e.setDoctorName(doctorName);
        e.setDoctorBrief(StrUtil.trimToNull(dto.getDoctorBrief()));
        e.setDoctorDetail(doctorDetail);
        e.setSkillId(skillId);
        e.setDoctorPic1(StrUtil.trimToNull(dto.getDoctorPic1()));
        e.setDoctorPic2(StrUtil.trimToNull(dto.getDoctorPic2()));
        e.setDoctorPic3(StrUtil.trimToNull(dto.getDoctorPic3()));
        e.setMedia(StrUtil.trimToNull(dto.getMedia()));
        e.setPreviewPic(StrUtil.trimToNull(dto.getPreviewPic()));
        e.setReadingGlossaryJson(DoctorStoryGlossaryJsonUtil.toJson(readingGlossary));
        doctorStoryMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Long id) {
        doctorStoryMapper.deleteById(id);
    }

    private DoctorStoryResponseDTO toDTO(DoctorStory e) {
        DoctorStoryResponseDTO dto = new DoctorStoryResponseDTO();
        dto.setId(e.getId());
        dto.setDoctorName(e.getDoctorName());
        dto.setDoctorBrief(e.getDoctorBrief());
        dto.setDoctorDetail(e.getDoctorDetail());
        dto.setSkillId(e.getSkillId());
        dto.setDoctorPic1(e.getDoctorPic1());
        dto.setDoctorPic2(e.getDoctorPic2());
        dto.setDoctorPic3(e.getDoctorPic3());
        dto.setMedia(e.getMedia());
        dto.setPreviewPic(e.getPreviewPic());
        dto.setReadingGlossary(DoctorStoryGlossaryJsonUtil.parse(e.getReadingGlossaryJson()));
        return dto;
    }

    private Integer normalizeSkillId(Integer skillId) {
        if (skillId == null || skillId <= 0) {
            return null;
        }
        return skillId;
    }

    private void validateSkillExists(Integer skillId) {
        if (skillId == null) {
            return;
        }
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException("技能ID不存在，请填写有效的技能ID");
        }
    }

    private void validateDoctorDetailLength(String doctorDetail) {
        if (doctorDetail == null) {
            return;
        }
        // TEXT 类型上限约 64KB，这里按 utf8mb4 字节数兜底校验，避免直接抛系统异常
        int byteLength = doctorDetail.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > 65535) {
            throw new BusinessException("故事详解内容过长，请精简后再保存");
        }
    }
}

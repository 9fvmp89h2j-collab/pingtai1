package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.BackpackSkillItemResponseDTO;
import org.example.springboot.entity.Skill;
import org.example.springboot.entity.UserCollect;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.SkillMapper;
import org.example.springboot.mapper.UserCollectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserCollectService {

    @Resource
    private UserCollectMapper userCollectMapper;

    @Resource
    private SkillMapper skillMapper;

    public void addCollect(Long userId, Integer skillId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (skillId == null) {
            throw new BusinessException("技能ID无效");
        }
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException("技能不存在");
        }
        LambdaQueryWrapper<UserCollect> dup = new LambdaQueryWrapper<>();
        dup.eq(UserCollect::getUserId, userId).eq(UserCollect::getSkillId, skillId);
        if (userCollectMapper.selectCount(dup) > 0) {
            return;
        }
        UserCollect row = UserCollect.builder()
                .userId(userId)
                .skillId(skillId)
                .createTime(LocalDateTime.now())
                .build();
        userCollectMapper.insert(row);
    }

    public void removeCollect(Long userId, Integer skillId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (skillId == null) {
            throw new BusinessException("技能ID无效");
        }
        LambdaQueryWrapper<UserCollect> w = new LambdaQueryWrapper<>();
        w.eq(UserCollect::getUserId, userId).eq(UserCollect::getSkillId, skillId);
        userCollectMapper.delete(w);
    }

    public boolean hasCollect(Long userId, Integer skillId) {
        if (userId == null || skillId == null) {
            return false;
        }
        LambdaQueryWrapper<UserCollect> w = new LambdaQueryWrapper<>();
        w.eq(UserCollect::getUserId, userId).eq(UserCollect::getSkillId, skillId);
        return userCollectMapper.selectCount(w) > 0;
    }

    public List<BackpackSkillItemResponseDTO> listMyCollect(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<UserCollect> w = new LambdaQueryWrapper<>();
        w.eq(UserCollect::getUserId, userId).orderByDesc(UserCollect::getCreateTime);
        List<UserCollect> rows = userCollectMapper.selectList(w);
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<BackpackSkillItemResponseDTO> out = new ArrayList<>();
        for (UserCollect row : rows) {
            Skill skill = skillMapper.selectById(row.getSkillId());
            if (skill == null) {
                continue;
            }
            BackpackSkillItemResponseDTO dto = new BackpackSkillItemResponseDTO();
            dto.setSkillId(skill.getSkillId());
            dto.setSkillName(skill.getSkillName());
            dto.setSkillBriefDescription(skill.getSkillBriefDescription());
            dto.setSkillDescription(skill.getSkillDescription());
            dto.setSkillPic(normalizeSkillPicForClient(skill.getSkillPic()));
            dto.setSkillCategory(skill.getSkillCategory());
            dto.setSkillScore(skill.getSkillScore());
            dto.setSkillType(skill.getSkillType());
            dto.setCollectTime(row.getCreateTime());
            out.add(dto);
        }
        return out;
    }

    private String normalizeSkillPicForClient(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String p = raw.trim().replace('\\', '/');
        if (p.startsWith("http://") || p.startsWith("https://")) {
            return p;
        }
        if (!p.startsWith("/")) {
            return "/" + p;
        }
        return p;
    }
}

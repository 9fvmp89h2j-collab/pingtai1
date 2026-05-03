package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.response.BackpackSkillItemResponseDTO;
import org.example.springboot.entity.Skill;
import org.example.springboot.entity.UserBackpack;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.SkillMapper;
import org.example.springboot.mapper.UserBackpackMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户技能背包
 */
@Slf4j
@Service
public class UserBackpackService {

    @Resource
    private UserBackpackMapper userBackpackMapper;

    @Resource
    private SkillMapper skillMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 将技能加入背包（幂等：已存在则提示）；首次收集时把 skillscore 累加到用户 score
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSkill(Long userId, Integer skillId) {
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

        LambdaQueryWrapper<UserBackpack> dup = new LambdaQueryWrapper<>();
        dup.eq(UserBackpack::getUserId, userId).eq(UserBackpack::getSkillId, skillId);
        if (userBackpackMapper.selectCount(dup) > 0) {
            throw new BusinessException("6001", "该技能已在背包中");
        }

        UserBackpack row = UserBackpack.builder()
                .userId(userId)
                .skillId(skillId)
                .collectTime(LocalDateTime.now())
                .build();
        userBackpackMapper.insert(row);

        int delta = parseSkillScoreDelta(skill.getSkillScore());
        if (delta != 0) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                int base = user.getScore() == null ? 0 : user.getScore();
                int next = base + delta;
                if (next < 0) {
                    throw new BusinessException("积分累加后不能为负数");
                }
                user.setScore(next);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
                log.info("用户 {} 收集技能 {}，score +{}", userId, skillId, delta);
            }
        } else {
            log.info("用户 {} 收集技能 {}（skillscore 未解析为数字，不加分）", userId, skillId);
        }
    }

    /**
     * 将 skills.skillscore（varchar）解析为整数变化量；无法解析则为 0
     */
    private int parseSkillScoreDelta(String skillScore) {
        if (!StringUtils.hasText(skillScore)) {
            return 0;
        }
        String s = skillScore.trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            try {
                return (int) Math.round(Double.parseDouble(s));
            } catch (NumberFormatException ignored2) {
                return 0;
            }
        }
    }

    /** 统一为浏览器可请求的 URL：files/... → /files/... */
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

    /**
     * 当前用户背包内全部技能（含详情），按收集时间倒序
     */
    public List<BackpackSkillItemResponseDTO> listMyBackpack(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<UserBackpack> w = new LambdaQueryWrapper<>();
        w.eq(UserBackpack::getUserId, userId).orderByDesc(UserBackpack::getCollectTime);
        List<UserBackpack> rows = userBackpackMapper.selectList(w);
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<BackpackSkillItemResponseDTO> out = new ArrayList<>();
        for (UserBackpack ub : rows) {
            Skill skill = skillMapper.selectById(ub.getSkillId());
            if (skill == null) {
                continue;
            }
            out.add(toItem(skill, ub.getCollectTime()));
        }
        return out;
    }

    private BackpackSkillItemResponseDTO toItem(Skill skill, LocalDateTime collectTime) {
        BackpackSkillItemResponseDTO dto = new BackpackSkillItemResponseDTO();
        dto.setSkillId(skill.getSkillId());
        dto.setSkillName(skill.getSkillName());
        dto.setSkillBriefDescription(skill.getSkillBriefDescription());
        dto.setSkillDescription(skill.getSkillDescription());
        dto.setSkillPic(normalizeSkillPicForClient(skill.getSkillPic()));
        dto.setSkillCategory(skill.getSkillCategory());
        dto.setSkillScore(skill.getSkillScore());
        dto.setSkillType(skill.getSkillType());
        dto.setCollectTime(collectTime);
        return dto;
    }
}

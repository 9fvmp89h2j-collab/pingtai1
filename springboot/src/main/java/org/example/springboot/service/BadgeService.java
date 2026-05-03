package org.example.springboot.service;

import jakarta.annotation.Resource;
import org.example.springboot.dto.response.BadgeResponseDTO;
import org.example.springboot.entity.Illness;
import org.example.springboot.mapper.BadgeMapper;
import org.example.springboot.mapper.IllnessMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BadgeService {

    @Resource
    private BadgeMapper badgeMapper;

    @Resource
    private IllnessMapper illnessMapper;

    private static final int[] CHECKIN_MILESTONES = {1, 3, 7, 30};
    private static final int[] PERFECT_MILESTONES = {1, 3, 7};

    public List<BadgeResponseDTO> listUserBadges(Long userId) {
        return badgeMapper.listByUserId(userId);
    }

    public List<String> onCheckin(Long userId) {
        ensureAchievementRow(userId);
        int days = nvl(badgeMapper.getCheckinDays(userId)) + 1;
        badgeMapper.updateCheckinDays(userId, days);
        List<String> newBadgeNames = new ArrayList<>();
        for (int ms : CHECKIN_MILESTONES) {
            if (days == ms) {
                // 占位：徽章 path/name 后续你再改
                tryAward(userId, "签到 " + ms + " 天", "E:/pingtai1/vue3/src/assets/badge/成就2.png", newBadgeNames);
            }
        }
        log.info("签到成就更新: userId={}, checkinDays={}, newBadges={}", userId, days, newBadgeNames);
        return newBadgeNames;
    }

    public List<String> onUltimatePerfect(Long userId) {
        ensureAchievementRow(userId);
        int cnt = nvl(badgeMapper.getRightCount(userId)) + 1;
        badgeMapper.updateRightCount(userId, cnt);
        List<String> newBadgeNames = new ArrayList<>();
        for (int ms : PERFECT_MILESTONES) {
            if (cnt == ms) {
                tryAward(userId, "终极挑战满分 " + ms + " 次", "E:/pingtai1/vue3/src/assets/badge/成就2.png", newBadgeNames);
            }
        }
        log.info("满分成就更新: userId={}, rightCount={}, newBadges={}", userId, cnt, newBadgeNames);
        return newBadgeNames;
    }

    public List<String> onIllnessSolved(Long userId, Integer illnessId) {
        List<String> newBadgeNames = new ArrayList<>();
        if (illnessId == null) return newBadgeNames;
        Illness ill = illnessMapper.selectById(illnessId);
        if (ill == null) return newBadgeNames;
        String name = ill.getIllnessbadgename();
        String path = ill.getIllnessbadgepath();
        if (!StringUtils.hasText(name) || !StringUtils.hasText(path)) {
            log.info("历练徽章未配置: userId={}, illnessId={}, name={}, path={}", userId, illnessId, name, path);
            return newBadgeNames;
        }
        tryAward(userId, name.trim(), path.trim(), newBadgeNames);
        return newBadgeNames;
    }

    private void ensureAchievementRow(Long userId) {
        if (badgeMapper.findAchievementId(userId) == null) {
            badgeMapper.insertAchievement(userId);
        }
    }

    private void tryAward(Long userId, String badgeName, String badgePath, List<String> out) {
        if (badgeMapper.countUserBadgeByName(userId, badgeName) > 0) return;
        int n = badgeMapper.insertBadge(userId, badgePath, badgeName);
        if (n <= 0) {
            log.warn("徽章插入失败: userId={}, badgeName={}", userId, badgeName);
            return;
        }
        out.add(badgeName);
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}


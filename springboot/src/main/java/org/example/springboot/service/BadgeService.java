package org.example.springboot.service;

import jakarta.annotation.Resource;
import org.example.springboot.dto.response.BadgeResponseDTO;
import org.example.springboot.mapper.BadgeMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BadgeService {

    @Resource
    private BadgeMapper badgeMapper;

    private static final int[] CHECKIN_MILESTONES = {1, 3, 7, 30};

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
                tryAward(userId, "签到 " + ms + " 天", null, newBadgeNames);
            }
        }
        log.info("签到成就更新: userId={}, checkinDays={}, newBadges={}", userId, days, newBadgeNames);
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


package org.example.springboot.service;

import org.example.springboot.dto.response.CopperManDailyCaseResponseDTO;
import org.example.springboot.dto.response.CopperManDiscoverResponseDTO;
import org.example.springboot.entity.AcupointKnowledge;
import org.example.springboot.entity.UserCopperManProfile;
import org.example.springboot.mapper.AcupointKnowledgeMapper;
import org.example.springboot.mapper.UserAcupointDailyProgressMapper;
import org.example.springboot.mapper.UserCopperManProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopperManServiceTest {
    @Mock
    private AcupointKnowledgeMapper acupointKnowledgeMapper;
    @Mock
    private UserAcupointDailyProgressMapper progressMapper;
    @Mock
    private UserCopperManProfileMapper profileMapper;
    @InjectMocks
    private CopperManService service;

    private final LocalDate caseDate = LocalDate.of(2026, 8, 9);

    @BeforeEach
    void setUp() {
        List<AcupointKnowledge> points = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            points.add(point("P-" + i, i));
        }
        when(acupointKnowledgeMapper.selectList(any())).thenReturn(points);
    }

    @Test
    void guestDailyCaseContainsFiveFeaturedPointsAndThreeTargets() {
        CopperManDailyCaseResponseDTO result = service.getDailyCase(null, caseDate);

        assertThat(result.getFeaturedPoints()).hasSize(5);
        assertThat(result.getTargetCodes()).hasSize(3);
        assertThat(result.getDiscoveredCodes()).isEmpty();
        assertThat(result.getPersisted()).isFalse();
    }

    @Test
    void loggedInCompletionAwardsOnlyOncePerDay() {
        List<String> targets = service.getDailyCase(null, caseDate).getTargetCodes();
        Long userId = 7L;
        UserCopperManProfile profile = new UserCopperManProfile();
        profile.setUserId(userId);
        profile.setCopperTokens(2);
        profile.setStarSand(5);
        profile.setCompletedCases(1);

        when(progressMapper.selectCodes(eq(userId), eq(caseDate))).thenReturn(targets);
        when(profileMapper.awardDailyCase(userId, caseDate)).thenReturn(1, 0);
        when(profileMapper.selectById(userId)).thenReturn(profile);

        CopperManDiscoverResponseDTO first = service.discover(userId, targets.get(0), caseDate);
        CopperManDiscoverResponseDTO second = service.discover(userId, targets.get(0), caseDate);

        assertThat(first.getCompleted()).isTrue();
        assertThat(first.getNewlyCompleted()).isTrue();
        assertThat(first.getRewardCopperTokens()).isEqualTo(2);
        assertThat(first.getRewardStarSand()).isEqualTo(5);
        assertThat(second.getNewlyCompleted()).isFalse();
        assertThat(second.getRewardCopperTokens()).isZero();
        assertThat(second.getRewardStarSand()).isZero();
    }

    private AcupointKnowledge point(String code, int order) {
        AcupointKnowledge point = new AcupointKnowledge();
        point.setCode(code);
        point.setPointNumber(order);
        point.setName("星点" + order);
        point.setPinyin("Point" + order);
        point.setMeridianCode("T");
        point.setMeridianName("测试经络");
        point.setModelId("p" + order);
        point.setBodyArea("手腕部");
        point.setChildLocation("在铜人上寻找星点");
        point.setChildDescription("认识身体地图上的文化星点");
        point.setSafetyTip("只观察和学习");
        point.setPositionX(BigDecimal.valueOf(order));
        point.setPositionY(BigDecimal.ZERO);
        point.setPositionZ(BigDecimal.ZERO);
        point.setSortOrder(order);
        point.setEnabled(true);
        return point;
    }
}

package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/** 管理员用户详情抽屉的只读聚合数据。 */
@Data
@Builder
@Schema(description = "管理员用户详情聚合数据")
public class AdminUserOverviewResponseDTO {
    private AdminUserListItemDTO user;
    private UserLevelInfoResponseDTO level;
    private UserAdventureMapResponseDTO adventureMap;
    private LearningStats learning;
    private List<BadgeResponseDTO> badges;

    @Data
    @Builder
    @Schema(description = "用户学习统计")
    public static class LearningStats {
        private int checkinDays;
        private long checkinCount;
        private LocalDate lastCheckinDate;
        private int quizTotalCount;
        private int quizCorrectCount;
        private double quizAccuracy;
        private long mistakeCount;
        private long badgeCount;
        private long backpackCount;
        private long collectCount;
        private long acupointDiscoveredCount;
        private int copperTokens;
        private int starSand;
        private int completedCases;
        private LocalDate lastCompletedCaseDate;
    }
}

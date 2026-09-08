package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 仪表板统计数据响应DTO
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表板统计数据响应")
public class DashboardStatisticsResponseDTO {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "今日新增用户数")
    private Long todayNewUsers;

    @Schema(description = "总管理员数")
    private Long totalAdmins;

    @Schema(description = "社区总发帖数")
    private Long totalPosts;

    @Schema(description = "今日新增发帖数")
    private Long todayNewPosts;

    @Schema(description = "网站总访问数")
    private Long totalVisits;

    @Schema(description = "今日访问数")
    private Long todayVisits;

    @Schema(description = "近7天访问趋势")
    private List<DailyStatistics> last7DaysVisits;

    /**
     * 每日统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日统计数据")
    public static class DailyStatistics {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "数值")
        private Long count;

    }
}

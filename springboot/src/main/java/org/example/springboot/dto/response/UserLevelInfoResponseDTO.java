package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户等级信息响应DTO
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户等级信息响应DTO")
public class UserLevelInfoResponseDTO {

    @Schema(description = "当前气血能量值")
    private Integer currentScore;

    @Schema(description = "当前等级")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "形象/文案暗示")
    private String description;

    @Schema(description = "当前等级气血能量下限")
    private Integer minScore;

    @Schema(description = "当前等级气血能量上限")
    private Integer maxScore;

    @Schema(description = "进度条百分比")
    private Double progress;
}
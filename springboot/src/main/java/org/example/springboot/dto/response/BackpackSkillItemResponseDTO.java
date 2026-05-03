package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 背包中的技能条目（含技能信息与收集时间）
 */
@Data
@Schema(description = "背包技能项")
public class BackpackSkillItemResponseDTO {

    @Schema(description = "技能ID")
    private Integer skillId;

    @Schema(description = "技能名称")
    private String skillName;

    @Schema(description = "技能简介")
    private String skillBriefDescription;

    @Schema(description = "技能详情")
    private String skillDescription;

    @Schema(description = "技能配图路径")
    private String skillPic;

    @Schema(description = "技能分类")
    private String skillCategory;

    @Schema(description = "技能积分/等级等")
    private String skillScore;

    @Schema(description = "技能类型")
    private String skillType;

    @Schema(description = "收集时间")
    private LocalDateTime collectTime;
}

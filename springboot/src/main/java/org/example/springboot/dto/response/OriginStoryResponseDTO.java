package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 针灸起源故事（列表/详情共用）
 */
@Data
@Schema(description = "针灸起源故事")
public class OriginStoryResponseDTO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "故事标题")
    private String storyTitle;

    @Schema(description = "副标题")
    private String storySubtitle;

    @Schema(description = "正文")
    private String storyText;

    @Schema(description = "配图1")
    private String storyPic1;

    @Schema(description = "配图2")
    private String storyPic2;

    @Schema(description = "配图3")
    private String storyPic3;

    @Schema(description = "媒体文件（视频等）")
    private String media;

    @Schema(description = "关联技能ID")
    private Long skillId;
}

package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "开始页管理-请求DTO")
public class OriginStoryAdminCommandDTO {

    @Schema(description = "故事标题")
    private String storyTitle;

    @Schema(description = "故事副标题")
    private String storySubtitle;

    @Schema(description = "故事正文")
    private String storyText;

    @Schema(description = "图片1地址")
    private String storyPic1;

    @Schema(description = "图片2地址")
    private String storyPic2;

    @Schema(description = "图片3地址")
    private String storyPic3;

    @Schema(description = "媒体文件地址（视频等）")
    private String media;

    @Schema(description = "技能ID")
    private Long skillId;
}

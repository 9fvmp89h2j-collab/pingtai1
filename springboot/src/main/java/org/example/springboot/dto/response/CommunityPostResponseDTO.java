package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "帖子列表响应")
public class CommunityPostResponseDTO {

    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "发帖用户ID")
    private Long userId;

    @Schema(description = "发帖用户名")
    private String username;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容摘要")
    private String contentSummary;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "板块")
    private String postType;

    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    @Schema(description = "当前用户是否已收藏")
    private Boolean collected;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "我的评论项")
public class MyPostCommentItemResponseDTO {

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "帖子标题")
    private String postTitle;

    @Schema(description = "帖子内容摘要")
    private String postContentSummary;

    @Schema(description = "我的评论内容")
    private String commentContent;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}

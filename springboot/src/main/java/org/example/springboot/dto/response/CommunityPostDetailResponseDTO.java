package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "帖子详情响应")
public class CommunityPostDetailResponseDTO {

    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "发帖用户ID")
    private Long userId;

    @Schema(description = "发帖用户名")
    private String username;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "帖子图片1")
    private String postPic1;

    @Schema(description = "帖子图片2")
    private String postPic2;

    @Schema(description = "帖子图片3")
    private String postPic3;

    @Schema(description = "帖子图片4")
    private String postPic4;

    @Schema(description = "帖子图片5")
    private String postPic5;

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

    @Schema(description = "评论列表")
    private List<PostCommentResponseDTO> comments;
}

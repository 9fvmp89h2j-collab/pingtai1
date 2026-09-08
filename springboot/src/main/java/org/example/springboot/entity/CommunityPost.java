package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 针灸答疑帖子实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("community_post")
@Schema(description = "针灸答疑帖子")
public class CommunityPost {

    @TableId(type = IdType.AUTO)
    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "发帖用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200)
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "点赞数")
    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "收藏数")
    @TableField("collect_count")
    private Integer collectCount;

    @Schema(description = "评论数")
    @TableField("comment_count")
    private Integer commentCount;

    @Schema(description = "帖子分类（库字段 postcatagory）")
    @TableField("postcatagory")
    private String postCatagory;

    @Schema(description = "板块：问答专区/分享专区/公告/反馈")
    @TableField("posttype")
    private String postType;

    @Schema(description = "帖子图片1")
    @TableField("postpic1")
    private String postPic1;

    @Schema(description = "帖子图片2")
    @TableField("postpic2")
    private String postPic2;

    @Schema(description = "帖子图片3")
    @TableField("postpic3")
    private String postPic3;

    @Schema(description = "帖子图片4")
    @TableField("postpic4")
    private String postPic4;

    @Schema(description = "帖子图片5")
    @TableField("postpic5")
    private String postPic5;

    @Schema(description = "状态：0正常 1已删除 2已举报")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

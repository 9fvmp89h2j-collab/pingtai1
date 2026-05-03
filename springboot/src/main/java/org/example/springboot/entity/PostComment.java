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

import java.time.LocalDateTime;

/**
 * 帖子评论实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post_comment")
@Schema(description = "帖子评论")
public class PostComment {

    @TableId(type = IdType.AUTO)
    @Schema(description = "评论ID")
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @Schema(description = "评论内容")
    private String content;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

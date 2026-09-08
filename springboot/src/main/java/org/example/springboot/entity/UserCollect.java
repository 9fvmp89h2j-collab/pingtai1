package org.example.springboot.entity;

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
 * 用户收藏技能
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_collect")
@Schema(description = "用户收藏技能")
public class UserCollect {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("userid")
    private Long userId;

    @TableField("skillid")
    private Integer skillId;

    @TableField("create_time")
    private LocalDateTime createTime;
}

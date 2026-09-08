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
 * 用户技能背包
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_backpack")
@Schema(description = "用户技能背包")
public class UserBackpack {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("skill_id")
    private Integer skillId;

    @TableField("collect_time")
    private LocalDateTime collectTime;
}

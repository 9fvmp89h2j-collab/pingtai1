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
import java.time.LocalDateTime;

/**
 * 常见误区问答实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("misconception")
@Schema(description = "常见误区")
public class Misconception {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "问题")
    @NotBlank(message = "问题不能为空")
    private String question;

    @Schema(description = "答案")
    private String answer;

    @Schema(description = "排序号，越小越靠前")
    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单次答题记录（表 user_quiz_record）
 */
@Data
@TableName("user_quiz_record")
public class UserQuizRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_answer")
    private String userAnswer;

    @TableField("is_correct")
    private Integer isCorrect;

    /** 0 正常 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

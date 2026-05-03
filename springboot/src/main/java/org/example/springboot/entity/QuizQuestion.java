package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库（表 quiz_question）
 */
@Data
@TableName("quiz_question")
public class QuizQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("option_a")
    private String optionA;

    @TableField("option_b")
    private String optionB;

    @TableField("option_c")
    private String optionC;

    @TableField("option_d")
    private String optionD;

    @TableField("correct_answer")
    private String correctAnswer;

    private String explanation;

    /** 对应库字段 category */
    @TableField("category")
    private String catagory;

    /** 1简单 2中等 3困难 */
    private Integer difficulty;

    /** 0禁用 1启用 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

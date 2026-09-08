package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quiz_stage_question")
public class QuizStageQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("stage_code")
    private String stageCode;
    @TableField("question_id")
    private Long questionId;
    private Integer position;
}

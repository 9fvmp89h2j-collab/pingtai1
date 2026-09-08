package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz_stage")
public class QuizStage {
    @TableId("stage_code")
    private String stageCode;
    @TableField("module_code")
    private String moduleCode;
    private String title;
    @TableField("required_question_count")
    private Integer requiredQuestionCount;
    private Long revision;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}

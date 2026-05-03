package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "题库管理-题目详情")
public class QuizQuestionAdminResponseDTO {

    private Long id;
    private String title;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    @Schema(description = "题目分类（库字段 category）")
    private String category;
    @Schema(description = "1简单 2中等 3困难")
    private Integer difficulty;
    @Schema(description = "0禁用 1启用")
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

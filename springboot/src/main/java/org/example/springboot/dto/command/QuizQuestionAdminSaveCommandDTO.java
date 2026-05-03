package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "题库管理-保存题目（新增/更新）")
public class QuizQuestionAdminSaveCommandDTO {

    @Schema(description = "题目内容")
    private String title;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @Schema(description = "正确答案 A/B/C/D")
    private String correctAnswer;

    private String explanation;

    @Schema(description = "题目分类")
    private String category;

    @Schema(description = "难度 1简单 2中等 3困难")
    private Integer difficulty;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}

package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "题库管理-保存题目（新增/更新）")
public class QuizQuestionAdminSaveCommandDTO {

    @Schema(description = "稳定题目编码；新建时可留空由服务端生成")
    private String questionCode;

    @Schema(description = "所属模块 GENERAL/SAFETY_GUARDIAN")
    private String moduleCode;

    @Schema(description = "题型 TRUE_FALSE/SINGLE_CHOICE/SCENARIO_CHOICE")
    private String questionType;

    @Schema(description = "题目内容")
    private String title;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @Schema(description = "正确答案 A/B/C/D")
    private String correctAnswer;

    private String explanation;

    private String sceneImagePath;
    private String sceneImageAlt;
    private String sceneCaption;

    @Schema(description = "题目分类")
    private String category;

    @Schema(description = "难度 1简单 2中等 3困难")
    private Integer difficulty;

    @Schema(description = "状态 0停用 1启用 2归档")
    private Integer status;
}

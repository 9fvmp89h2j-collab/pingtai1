package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizAnswerSubmitCommandDTO {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** A / B / C / D */
    @NotBlank(message = "请选择答案")
    private String userAnswer;
}

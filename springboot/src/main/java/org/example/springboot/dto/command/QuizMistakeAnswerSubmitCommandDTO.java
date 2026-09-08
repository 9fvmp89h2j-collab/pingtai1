package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 错题本重新作答提交
 */
@Data
public class QuizMistakeAnswerSubmitCommandDTO {

    @NotNull(message = "mistakeId 不能为空")
    private Long mistakeId;

    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @NotBlank(message = "userAnswer 不能为空")
    private String userAnswer;
}


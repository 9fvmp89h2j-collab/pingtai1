package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizStageAnswerCommandDTO {
    @NotNull
    private Long stageRevision;
    @NotNull
    private Long questionId;
    @NotBlank
    private String userAnswer;
}

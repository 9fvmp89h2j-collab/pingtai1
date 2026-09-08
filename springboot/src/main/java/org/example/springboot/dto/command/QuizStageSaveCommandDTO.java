package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuizStageSaveCommandDTO {
    @NotNull
    private List<Long> questionIds;
}

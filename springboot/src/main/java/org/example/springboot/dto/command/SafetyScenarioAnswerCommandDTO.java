package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SafetyScenarioAnswerCommandDTO {
    @NotNull
    private Long configRevision;
    @NotBlank
    private String scenarioId;
    @NotBlank
    private String optionId;
}

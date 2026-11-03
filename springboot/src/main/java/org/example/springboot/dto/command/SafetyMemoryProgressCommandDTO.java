package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SafetyMemoryProgressCommandDTO {
    @NotBlank
    private String completedLevel;
}

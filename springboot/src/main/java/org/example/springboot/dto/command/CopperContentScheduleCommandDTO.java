package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CopperContentScheduleCommandDTO {
    @NotNull
    private LocalDateTime scheduledAt;
}

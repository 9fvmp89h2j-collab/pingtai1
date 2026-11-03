package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MainlineConfigPublishCommandDTO {
    @NotNull
    private Long revisionId;
}

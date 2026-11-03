package org.example.springboot.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LegacyGameStateImportCommandDTO {
    @Min(1)
    @Max(10)
    private Integer schemaVersion = 3;

    @NotEmpty
    @Size(max = 64)
    private String sourceKey = "xinglin-game-state-v3";

    @Valid
    @Size(max = 64)
    private List<LegacyTaskProgressDTO> tasks = new ArrayList<>();
}

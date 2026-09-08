package org.example.springboot.dto.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GameProgressEventCommandDTO {
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
    private String gameCode;

    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String eventType;

    @NotBlank
    @Size(max = 128)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
    private String taskCode;

    @Size(max = 32)
    @Pattern(regexp = "^[a-zA-Z0-9._:-]+$")
    private String periodKey = "lifetime";

    @Size(max = 64)
    private String resultCode;

    @Min(1)
    @Max(100)
    private Integer progressDelta = 1;
}

package org.example.springboot.dto.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LegacyTaskProgressDTO {
    @NotBlank
    @Size(max = 128)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
    private String taskCode;

    @Size(max = 32)
    @Pattern(regexp = "^[a-zA-Z0-9._:-]+$")
    private String periodKey = "lifetime";

    @Min(0)
    @Max(100)
    private Integer progress = 0;

    private Boolean completed = false;

    private Boolean rewardClaimed = false;
}

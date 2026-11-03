package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WeeklyChoiceRewardClaimCommandDTO {
    @NotBlank(message = "请选择一份奖励")
    private String itemCode;
}

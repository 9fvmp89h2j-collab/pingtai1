package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "添加技能到背包")
public class BackpackAddCommandDTO {

    @NotNull(message = "技能ID不能为空")
    @Schema(description = "skills.skillid")
    private Integer skillId;
}

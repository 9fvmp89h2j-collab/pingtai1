package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新气血能量DTO
 * @author system
 */
@Data
@Schema(description = "更新气血能量DTO")
public class UserScoreUpdateCommandDTO {

    @Schema(description = "气血能量变化值（正数增加，负数减少）")
    @NotNull(message = "气血能量变化值不能为空")
    private Integer scoreChange;

    @Schema(description = "变化原因")
    private String reason;
}
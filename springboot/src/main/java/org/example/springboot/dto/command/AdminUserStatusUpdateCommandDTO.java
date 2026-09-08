package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 管理员切换用户账号状态命令。 */
@Data
@Schema(description = "管理员切换用户账号状态命令")
public class AdminUserStatusUpdateCommandDTO {

    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态只能是0或1")
    @Max(value = 1, message = "账号状态只能是0或1")
    private Integer status;
}

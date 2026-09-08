package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "常见误区创建命令")
public class MisconceptionCreateCommandDTO {

    @NotBlank(message = "问题不能为空")
    @Schema(description = "问题")
    private String question;

    @Schema(description = "答案")
    private String answer;

    @Schema(description = "排序号")
    private Integer sortOrder;
}

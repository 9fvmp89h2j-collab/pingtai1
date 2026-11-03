package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CopperManDiscoverCommandDTO {
    @NotBlank(message = "穴位编号不能为空")
    private String code;
}

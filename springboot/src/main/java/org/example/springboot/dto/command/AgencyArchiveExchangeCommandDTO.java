package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgencyArchiveExchangeCommandDTO {
    @NotBlank(message = "修复项目不能为空")
    @Size(max = 64, message = "修复项目编号过长")
    private String itemId;
}

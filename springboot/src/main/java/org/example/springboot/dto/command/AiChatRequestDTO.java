package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatRequestDTO {

    @NotBlank(message = "请输入问题")
    @Size(max = 300, message = "问题不能超过300个字")
    private String prompt;
}

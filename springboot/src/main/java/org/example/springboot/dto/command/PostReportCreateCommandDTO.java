package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "举报创建命令")
public class PostReportCreateCommandDTO {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "举报原因")
    private String reason;
}

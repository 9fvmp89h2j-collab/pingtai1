package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户重置密码命令DTO
 * @author system
 */
@Data
@Schema(description = "用户重置密码命令")
public class UserResetPasswordCommandDTO {

    @Schema(description = "用户名", example = "testuser", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;

    @Schema(description = "邮箱", example = "test@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "邮箱验证码", example = "123456", required = true)
    @NotBlank(message = "验证码不能为空")
    @jakarta.validation.constraints.Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String verificationCode;

    @Schema(description = "新密码", example = "newPassword123", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 100, message = "新密码长度必须在8到100个字符之间")
    private String newPassword;
}

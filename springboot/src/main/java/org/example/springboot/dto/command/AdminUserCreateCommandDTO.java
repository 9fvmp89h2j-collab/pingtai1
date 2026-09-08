package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员创建普通用户命令。 */
@Data
@Schema(description = "管理员创建普通用户命令")
public class AdminUserCreateCommandDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8到100个字符之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    @Size(max = 200, message = "头像路径长度不能超过200个字符")
    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String sex;

    @jakarta.validation.constraints.Min(value = 0, message = "年龄不能小于0")
    @jakarta.validation.constraints.Max(value = 150, message = "年龄不能大于150")
    private Integer age;
}

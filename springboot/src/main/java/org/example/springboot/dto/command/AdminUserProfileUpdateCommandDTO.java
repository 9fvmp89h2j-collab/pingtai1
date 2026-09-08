package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员编辑用户基本资料命令，不包含角色、积分和学习进度。 */
@Data
@Schema(description = "管理员编辑用户基本资料命令")
public class AdminUserProfileUpdateCommandDTO {

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

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

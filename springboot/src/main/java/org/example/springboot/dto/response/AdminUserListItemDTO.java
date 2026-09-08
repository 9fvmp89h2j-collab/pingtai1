package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 管理员用户列表行数据。 */
@Data
@Builder
@Schema(description = "管理员用户列表行数据")
public class AdminUserListItemDTO {
    private Long id;
    private String username;
    private String displayName;
    private String name;
    private String avatar;
    private String email;
    private String phone;
    private String sex;
    private Integer age;
    private String userType;
    private String userTypeDisplayName;
    private Integer status;
    private String statusDisplayName;
    private Integer score;
    private Integer level;
    private String levelName;
    private String honor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

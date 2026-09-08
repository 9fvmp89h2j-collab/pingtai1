package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 用户管理页顶部统计。 */
@Data
@Builder
@Schema(description = "用户管理页顶部统计")
public class AdminUserSummaryResponseDTO {
    private long totalUsers;
    private long activeUsers;
    private long disabledUsers;
    private long adminUsers;
}

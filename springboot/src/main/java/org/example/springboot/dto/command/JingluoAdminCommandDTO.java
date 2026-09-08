package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "经络管理-请求DTO")
public class JingluoAdminCommandDTO {

    @Schema(description = "经络名称")
    private String jingluoName;

    @Schema(description = "经络分类（库字段 jingluocatagory）")
    private String jingluoCatagory;

    @Schema(description = "循行/次序（库字段 jingluoorder）")
    private String jingluoOrder;

    @Schema(description = "主治病症")
    private String illness;

    @Schema(description = "经络图（库字段 jingluopic）")
    private String jingluoPic;

    @Schema(description = "技能ID")
    private Integer skillId;
}

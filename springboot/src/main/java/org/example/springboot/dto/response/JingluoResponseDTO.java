package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "经络（列表/详情）")
public class JingluoResponseDTO {

    @Schema(description = "主键 jingluoid")
    private Integer jingluoId;

    @Schema(description = "经络名称")
    private String jingluoName;

    @Schema(description = "经络分类")
    private String jingluoCatagory;

    @Schema(description = "循行/次序（腧穴页对应 position）")
    private String jingluoOrder;

    @Schema(description = "管理员维护字段；公开接口不返回")
    private String illness;

    @Schema(description = "经络图路径")
    private String jingluoPic;

    @Schema(description = "关联技能ID")
    private Integer skillId;
}

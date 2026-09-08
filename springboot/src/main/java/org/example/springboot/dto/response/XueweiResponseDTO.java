package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "穴位（列表/详情）")
public class XueweiResponseDTO {

    @Schema(description = "主键 xueweiid")
    private Long xueweiId;

    @Schema(description = "穴位名称")
    private String xueweiName;

    @Schema(description = "经络分类")
    private String xueweiCatagory;

    @Schema(description = "定位")
    private String position;

    @Schema(description = "功效")
    private String illness;

    @Schema(description = "穴位图路径")
    private String xueweiPic1;

    @Schema(description = "关联技能ID")
    private Integer skillId;
}

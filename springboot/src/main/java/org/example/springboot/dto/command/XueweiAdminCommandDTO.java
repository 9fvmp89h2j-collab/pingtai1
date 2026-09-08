package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "腧穴管理-请求DTO")
public class XueweiAdminCommandDTO {

    @Schema(description = "穴位名称")
    private String xueweiName;

    @Schema(description = "穴位分类（库字段 xueweicatagory）")
    private String xueweiCatagory;

    @Schema(description = "定位")
    private String position;

    @Schema(description = "主治病症")
    private String illness;

    @Schema(description = "穴位图")
    private String xueweiPic1;

    @Schema(description = "技能ID")
    private Integer skillId;
}

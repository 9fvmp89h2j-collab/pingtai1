package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "调车小游戏关卡")
public class TrainGameResponseDTO {

    private Integer id;

    @Schema(description = "经络名称")
    private String jingluoName;

    @Schema(description = "结点全称1")
    private String game1;

    @Schema(description = "结点全称2")
    private String game2;

    @Schema(description = "结点全称3")
    private String game3;

    @Schema(description = "结点全称4")
    private String game4;

    @Schema(description = "结点全称5")
    private String game5;

    @Schema(description = "结点简称1")
    private String game1Brief;

    @Schema(description = "结点简称2")
    private String game2Brief;

    @Schema(description = "结点简称3")
    private String game3Brief;

    @Schema(description = "结点简称4")
    private String game4Brief;

    @Schema(description = "结点简称5")
    private String game5Brief;
}

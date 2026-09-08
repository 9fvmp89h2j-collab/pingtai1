package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.springboot.dto.DoctorStoryGlossaryItemDTO;

import java.util.List;

@Data
@Schema(description = "针灸名医管理-请求DTO")
public class DoctorStoryAdminCommandDTO {

    @Schema(description = "故事名称")
    private String doctorName;

    @Schema(description = "故事简介")
    private String doctorBrief;

    @Schema(description = "故事详解")
    private String doctorDetail;

    @Schema(description = "技能ID")
    private Integer skillId;

    @Schema(description = "图片1")
    private String doctorPic1;

    @Schema(description = "图片2")
    private String doctorPic2;

    @Schema(description = "图片3")
    private String doctorPic3;

    @Schema(description = "媒体文件地址（视频等）")
    private String media;

    @Schema(description = "故事预览封面")
    private String previewPic;

    @Schema(description = "重点词词库")
    private List<DoctorStoryGlossaryItemDTO> readingGlossary;
}

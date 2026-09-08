package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.springboot.dto.DoctorStoryGlossaryItemDTO;

import java.util.List;

/**
 * 针灸名医故事（列表/详情展示）
 */
@Data
@Schema(description = "针灸名医故事")
public class DoctorStoryResponseDTO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "名医名字")
    private String doctorName;

    @Schema(description = "名医简介")
    private String doctorBrief;

    @Schema(description = "名医详解")
    private String doctorDetail;

    @Schema(description = "配图1")
    private String doctorPic1;

    @Schema(description = "配图2")
    private String doctorPic2;

    @Schema(description = "配图3")
    private String doctorPic3;

    @Schema(description = "媒体文件（视频等）")
    private String media;

    @Schema(description = "故事预览封面")
    private String previewPic;

    @Schema(description = "重点词词库")
    private List<DoctorStoryGlossaryItemDTO> readingGlossary;

    @Schema(description = "关联技能ID")
    private Integer skillId;
}

package org.example.springboot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BodyMapAcupointResponseDTO {
    private String code;
    private Integer pointNumber;
    private String name;
    private String pinyin;
    private String meridianCode;
    private String meridianName;
    private String bodyArea;
    private String regionCode;
    private String bodyView;
    private String placementHint;
    private String childMapDescription;
    private String childDescription;
    private String childTraditionalUse;
    private String safetyTip;
    private String traditionalUseSourceName;
    private String traditionalUseSourceLink;
}

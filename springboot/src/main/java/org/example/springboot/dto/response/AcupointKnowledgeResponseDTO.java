package org.example.springboot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AcupointKnowledgeResponseDTO {
    private String code;
    private Integer pointNumber;
    private String name;
    private String pinyin;
    private String meridianCode;
    private String meridianName;
    private String modelId;
    private String bodyArea;
    private String childLocation;
    private String childDescription;
    private String childTraditionalUse;
    private String safetyTip;
    private String sourceName;
    private String sourceLink;
    private String traditionalUseSourceName;
    private String traditionalUseSourceLink;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal positionZ;
    private Boolean discovered;
}

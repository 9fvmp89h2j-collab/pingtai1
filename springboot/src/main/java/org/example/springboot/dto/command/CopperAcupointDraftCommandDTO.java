package org.example.springboot.dto.command;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CopperAcupointDraftCommandDTO {
    private String code;
    private Integer pointNumber;
    private String name;
    private String pinyin;
    private String meridianCode;
    private String meridianName;
    private String meridianEnglish;
    private String modelId;
    private String bodyArea;
    private String standardLocation;
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
    private Integer sortOrder;
    private Boolean enabled;
}

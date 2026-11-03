package org.example.springboot.dto.command;

import lombok.Data;

import java.util.Map;

@Data
public class SafetyGameConfigDraftCommandDTO {
    private Long revisionId;
    private Integer editVersion;
    private Map<String, Object> config;
}

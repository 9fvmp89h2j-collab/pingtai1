package org.example.springboot.dto.command;

import lombok.Data;

@Data
public class SafetyGameConfigPublishCommandDTO {
    private Long revisionId;
    private Integer editVersion;
}

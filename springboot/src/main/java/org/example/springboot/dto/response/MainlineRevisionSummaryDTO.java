package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MainlineRevisionSummaryDTO {
    private Long revisionId;
    private Integer version;
    private String status;
    private Long createdBy;
    private Long publishedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}

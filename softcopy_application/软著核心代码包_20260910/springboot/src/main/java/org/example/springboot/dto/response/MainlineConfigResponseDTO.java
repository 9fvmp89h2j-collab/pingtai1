package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MainlineConfigResponseDTO {
    private Long revisionId;
    private String gameCode;
    private Integer version;
    private Integer editVersion;
    private String status;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<MainlineLevelConfigDTO> levels = new ArrayList<>();
}

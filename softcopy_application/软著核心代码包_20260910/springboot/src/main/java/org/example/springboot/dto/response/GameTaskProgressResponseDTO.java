package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameTaskProgressResponseDTO {
    private String gameCode;
    private String taskCode;
    private String periodKey;
    private Integer progress;
    private Integer target;
    private String status;
    private LocalDateTime completedAt;
    private LocalDateTime claimedAt;
    private Integer configVersion;
}

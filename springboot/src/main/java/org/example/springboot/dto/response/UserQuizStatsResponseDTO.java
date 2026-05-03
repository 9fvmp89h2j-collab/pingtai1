package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserQuizStatsResponseDTO {
    private Long userId;
    private Integer totalCount;
    private Integer correctCount;
    private LocalDateTime lastQuizTime;
}

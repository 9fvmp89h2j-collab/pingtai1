package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizHistoryItemDTO {
    private Long id;
    private Long questionId;
    private String userAnswer;
    /** 是否答对 */
    private boolean correct;
    private LocalDateTime createTime;
}

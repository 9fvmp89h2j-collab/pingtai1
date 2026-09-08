package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的错题列表项（不含正确答案）
 */
@Data
public class UserQuizMistakeQuestionDTO {
    private Long mistakeId;
    private Long questionId;
    private String title;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private LocalDateTime createTime;
}


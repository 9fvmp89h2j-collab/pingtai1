package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 错题本作答结果（不返回正确答案）
 */
@Data
@AllArgsConstructor
public class QuizMistakeAnswerResultDTO {
    private boolean correct;
}


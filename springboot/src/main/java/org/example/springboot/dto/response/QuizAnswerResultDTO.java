package org.example.springboot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizAnswerResultDTO {
    private boolean correct;
    /** 标准答案字母，如 A */
    private String correctAnswer;
    private String explanation;
    /** 所属板块（错题展示用） */
    private String catagory;
}

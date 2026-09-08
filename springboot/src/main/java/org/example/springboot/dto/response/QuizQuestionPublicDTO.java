package org.example.springboot.dto.response;

import lombok.Data;

/**
 * 随机抽题返回（不含答案与解析）
 */
@Data
public class QuizQuestionPublicDTO {
    private Long id;
    private String title;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}

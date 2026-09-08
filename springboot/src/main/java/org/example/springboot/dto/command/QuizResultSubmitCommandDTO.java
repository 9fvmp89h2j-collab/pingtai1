package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 兼容旧版闯关：一次提交多题（选项为 0-3 下标）
 */
@Data
public class QuizResultSubmitCommandDTO {

    private Integer correctCount;

    private Integer totalCount;

    @NotNull(message = "答题明细不能为空")
    private List<QuizResultAnswerItemDTO> answers;

    @Data
    public static class QuizResultAnswerItemDTO {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;
        /** 0=A, 1=B, 2=C, 3=D */
        @NotNull(message = "选项不能为空")
        private Integer selectedAnswer;
    }
}

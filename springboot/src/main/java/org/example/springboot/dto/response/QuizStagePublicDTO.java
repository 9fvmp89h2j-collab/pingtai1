package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuizStagePublicDTO {
    private String stageCode;
    private String title;
    private Long revision;
    private Integer requiredQuestionCount;
    private List<Question> questions = new ArrayList<>();

    @Data
    public static class Question {
        private Long id;
        private String questionCode;
        private String questionType;
        private String title;
        private String sceneImagePath;
        private String sceneImageAlt;
        private String sceneCaption;
        private List<Option> options = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String key;
        private String text;
    }
}

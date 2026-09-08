package org.example.springboot.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuizStageAdminResponseDTO {
    private String stageCode;
    private String moduleCode;
    private String title;
    private Integer requiredQuestionCount;
    private Long revision;
    private List<QuizQuestionAdminResponseDTO> questions = new ArrayList<>();
}

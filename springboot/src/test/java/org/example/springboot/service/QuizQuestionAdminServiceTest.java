package org.example.springboot.service;

import org.example.springboot.dto.command.QuizQuestionAdminSaveCommandDTO;
import org.example.springboot.entity.QuizQuestion;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.QuizQuestionMapper;
import org.example.springboot.mapper.QuizStageMapper;
import org.example.springboot.mapper.QuizStageQuestionMapper;
import org.example.springboot.mapper.UserQuizRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuizQuestionAdminServiceTest {

    @Mock
    private QuizQuestionMapper quizQuestionMapper;
    @Mock
    private QuizStageQuestionMapper quizStageQuestionMapper;
    @Mock
    private QuizStageMapper quizStageMapper;
    @Mock
    private UserQuizRecordMapper userQuizRecordMapper;
    @InjectMocks
    private QuizQuestionAdminService service;

    @Test
    void createsNewQuestionDisabledByDefault() {
        QuizQuestionAdminSaveCommandDTO command = baseCommand("TRUE_FALSE");
        command.setStatus(null);
        command.setOptionC(null);
        command.setOptionD(null);
        command.setCorrectAnswer("B");
        command.setSceneVideoPath("/files/video/safety-question.mp4");

        service.create(command);

        ArgumentCaptor<QuizQuestion> captor = ArgumentCaptor.forClass(QuizQuestion.class);
        verify(quizQuestionMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isZero();
        assertThat(captor.getValue().getQuestionCode()).startsWith("q-");
        assertThat(captor.getValue().getOptionC()).isNull();
        assertThat(captor.getValue().getSceneVideoPath()).isEqualTo("/files/video/safety-question.mp4");
    }

    @Test
    void rejectsEnabledScenarioWithoutImage() {
        QuizQuestionAdminSaveCommandDTO command = baseCommand("SCENARIO_CHOICE");
        command.setStatus(1);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("上传题图");
    }

    @Test
    void rejectsTrueFalseAnswerOutsideFirstTwoOptions() {
        QuizQuestionAdminSaveCommandDTO command = baseCommand("TRUE_FALSE");
        command.setOptionC(null);
        command.setOptionD(null);
        command.setCorrectAnswer("C");

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题型不匹配");
    }

    @Test
    void rejectsNonMp4SceneVideo() {
        QuizQuestionAdminSaveCommandDTO command = baseCommand("TRUE_FALSE");
        command.setOptionC(null);
        command.setOptionD(null);
        command.setSceneVideoPath("/files/video/safety-question.webm");

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("MP4");
    }

    private static QuizQuestionAdminSaveCommandDTO baseCommand(String type) {
        QuizQuestionAdminSaveCommandDTO command = new QuizQuestionAdminSaveCommandDTO();
        command.setModuleCode("SAFETY_GUARDIAN");
        command.setQuestionType(type);
        command.setTitle("测试安全题目");
        command.setOptionA("选项A");
        command.setOptionB("选项B");
        command.setOptionC("选项C");
        command.setOptionD("选项D");
        command.setCorrectAnswer("A");
        command.setDifficulty(1);
        command.setStatus(0);
        return command;
    }
}

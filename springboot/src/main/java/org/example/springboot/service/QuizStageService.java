package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.QuizQuestionAdminResponseDTO;
import org.example.springboot.dto.response.QuizStageAdminResponseDTO;
import org.example.springboot.dto.response.QuizStagePublicDTO;
import org.example.springboot.entity.QuizQuestion;
import org.example.springboot.entity.QuizStage;
import org.example.springboot.entity.QuizStageQuestion;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.QuizQuestionMapper;
import org.example.springboot.mapper.QuizStageMapper;
import org.example.springboot.mapper.QuizStageQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizStageService {

    @Resource
    private QuizStageMapper quizStageMapper;
    @Resource
    private QuizStageQuestionMapper quizStageQuestionMapper;
    @Resource
    private QuizQuestionMapper quizQuestionMapper;
    @Resource
    private QuizQuestionAdminService quizQuestionAdminService;

    public QuizStagePublicDTO getPublicStage(String stageCode) {
        QuizStage stage = requireStage(stageCode);
        List<QuizQuestion> questions = orderedQuestions(stage);
        if (questions.size() != stage.getRequiredQuestionCount()) {
            throw new BusinessException("关卡题目配置不完整，请联系管理员");
        }
        QuizStagePublicDTO dto = new QuizStagePublicDTO();
        dto.setStageCode(stage.getStageCode());
        dto.setTitle(stage.getTitle());
        dto.setRevision(stage.getRevision());
        dto.setRequiredQuestionCount(stage.getRequiredQuestionCount());
        dto.setQuestions(questions.stream().map(this::toPublicQuestion).toList());
        return dto;
    }

    public QuizStageAdminResponseDTO getAdminStage(String stageCode) {
        QuizStage stage = requireStage(stageCode);
        QuizStageAdminResponseDTO dto = new QuizStageAdminResponseDTO();
        dto.setStageCode(stage.getStageCode());
        dto.setModuleCode(stage.getModuleCode());
        dto.setTitle(stage.getTitle());
        dto.setRequiredQuestionCount(stage.getRequiredQuestionCount());
        dto.setRevision(stage.getRevision());
        dto.setQuestions(orderedQuestions(stage).stream().map(quizQuestionAdminService::toDTO).toList());
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public QuizStageAdminResponseDTO saveStage(String stageCode, List<Long> questionIds) {
        QuizStage stage = requireStage(stageCode);
        int required = stage.getRequiredQuestionCount() == null ? 14 : stage.getRequiredQuestionCount();
        if (questionIds == null || questionIds.size() != required) {
            throw new BusinessException("关卡必须配置" + required + "道题");
        }
        Set<Long> unique = new LinkedHashSet<>(questionIds);
        if (unique.size() != required || unique.contains(null)) {
            throw new BusinessException("关卡题目不能重复或为空");
        }
        List<QuizQuestion> questions = quizQuestionMapper.selectBatchIds(unique);
        Map<Long, QuizQuestion> byId = questions.stream().collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));
        for (Long id : questionIds) {
            QuizQuestion q = byId.get(id);
            if (q == null || !stage.getModuleCode().equals(q.getModuleCode()) || q.getStatus() == null || q.getStatus() != 1) {
                throw new BusinessException("只能编排已启用的安全守护题目");
            }
            if ("SCENARIO_CHOICE".equals(q.getQuestionType())
                    && (StrUtil.isBlank(q.getSceneImagePath()) || StrUtil.isBlank(q.getSceneImageAlt()))) {
                throw new BusinessException("场景题“" + q.getTitle() + "”尚未配置题图");
            }
        }
        quizStageQuestionMapper.delete(new LambdaQueryWrapper<QuizStageQuestion>()
                .eq(QuizStageQuestion::getStageCode, stageCode));
        for (int i = 0; i < questionIds.size(); i++) {
            QuizStageQuestion relation = new QuizStageQuestion();
            relation.setStageCode(stageCode);
            relation.setQuestionId(questionIds.get(i));
            relation.setPosition(i + 1);
            quizStageQuestionMapper.insert(relation);
        }
        stage.setRevision((stage.getRevision() == null ? 0 : stage.getRevision()) + 1);
        quizStageMapper.updateById(stage);
        return getAdminStage(stageCode);
    }

    public void validateAnswer(String stageCode, Long stageRevision, Long questionId) {
        QuizStage stage = requireStage(stageCode);
        if (stageRevision == null || !stageRevision.equals(stage.getRevision())) {
            throw new BusinessException("题库已更新，请重新开始第一关");
        }
        QuizStageQuestion relation = quizStageQuestionMapper.selectOne(new LambdaQueryWrapper<QuizStageQuestion>()
                .eq(QuizStageQuestion::getStageCode, stageCode)
                .eq(QuizStageQuestion::getQuestionId, questionId));
        if (relation == null) {
            throw new BusinessException("题目不属于当前关卡");
        }
    }

    private QuizStage requireStage(String stageCode) {
        QuizStage stage = quizStageMapper.selectById(stageCode);
        if (stage == null) {
            throw new BusinessException("关卡不存在");
        }
        return stage;
    }

    private List<QuizQuestion> orderedQuestions(QuizStage stage) {
        List<QuizStageQuestion> relations = quizStageQuestionMapper.selectList(
                new LambdaQueryWrapper<QuizStageQuestion>()
                        .eq(QuizStageQuestion::getStageCode, stage.getStageCode())
                        .orderByAsc(QuizStageQuestion::getPosition));
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = relations.stream().map(QuizStageQuestion::getQuestionId).toList();
        Map<Long, QuizQuestion> byId = quizQuestionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));
        List<QuizQuestion> ordered = new ArrayList<>();
        for (Long id : ids) {
            QuizQuestion q = byId.get(id);
            if (q == null || q.getStatus() == null || q.getStatus() != 1) {
                throw new BusinessException("关卡包含已停用或不存在的题目，请联系管理员");
            }
            ordered.add(q);
        }
        return ordered;
    }

    private QuizStagePublicDTO.Question toPublicQuestion(QuizQuestion q) {
        QuizStagePublicDTO.Question dto = new QuizStagePublicDTO.Question();
        dto.setId(q.getId());
        dto.setQuestionCode(q.getQuestionCode());
        dto.setQuestionType(q.getQuestionType());
        dto.setTitle(q.getTitle());
        dto.setSceneImagePath(q.getSceneImagePath());
        dto.setSceneImageAlt(q.getSceneImageAlt());
        dto.setSceneCaption(q.getSceneCaption());
        List<QuizStagePublicDTO.Option> options = new ArrayList<>();
        addOption(options, "A", q.getOptionA());
        addOption(options, "B", q.getOptionB());
        addOption(options, "C", q.getOptionC());
        addOption(options, "D", q.getOptionD());
        dto.setOptions(options);
        return dto;
    }

    private static void addOption(List<QuizStagePublicDTO.Option> options, String key, String text) {
        if (StrUtil.isNotBlank(text)) {
            options.add(new QuizStagePublicDTO.Option(key, text));
        }
    }
}

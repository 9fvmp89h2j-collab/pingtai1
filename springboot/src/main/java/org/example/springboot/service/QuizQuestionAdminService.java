package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.QuizQuestionAdminSaveCommandDTO;
import org.example.springboot.dto.response.QuizQuestionAdminResponseDTO;
import org.example.springboot.entity.QuizQuestion;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.QuizQuestionMapper;
import org.example.springboot.mapper.QuizStageMapper;
import org.example.springboot.mapper.QuizStageQuestionMapper;
import org.example.springboot.mapper.UserQuizRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuizQuestionAdminService {

    @Resource
    private QuizQuestionMapper quizQuestionMapper;
    @Resource
    private QuizStageQuestionMapper quizStageQuestionMapper;
    @Resource
    private QuizStageMapper quizStageMapper;
    @Resource
    private UserQuizRecordMapper userQuizRecordMapper;

    public Page<QuizQuestionAdminResponseDTO> page(Long current, Long size, String title, String category,
                                                    String moduleCode, String questionType,
                                                    Integer difficulty, Integer status) {
        LambdaQueryWrapper<QuizQuestion> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(title)) {
            w.like(QuizQuestion::getTitle, title);
        }
        if (StrUtil.isNotBlank(category)) {
            w.like(QuizQuestion::getCatagory, category);
        }
        if (StrUtil.isNotBlank(moduleCode)) {
            w.eq(QuizQuestion::getModuleCode, moduleCode.trim().toUpperCase());
        }
        if (StrUtil.isNotBlank(questionType)) {
            w.eq(QuizQuestion::getQuestionType, questionType.trim().toUpperCase());
        }
        if (difficulty != null) {
            w.eq(QuizQuestion::getDifficulty, difficulty);
        }
        if (status != null) {
            w.eq(QuizQuestion::getStatus, status);
        }
        w.orderByDesc(QuizQuestion::getId);

        Page<QuizQuestion> page = quizQuestionMapper.selectPage(new Page<>(current, size), w);
        Page<QuizQuestionAdminResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public QuizQuestionAdminResponseDTO getById(Long id) {
        QuizQuestion e = quizQuestionMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("题目不存在");
        }
        return toDTO(e);
    }

    public QuizQuestionAdminResponseDTO create(QuizQuestionAdminSaveCommandDTO dto) {
        validateSave(dto);
        QuizQuestion e = new QuizQuestion();
        fillEntity(e, dto);
        if (e.getStatus() == null) {
            e.setStatus(0);
        }
        if (e.getDifficulty() == null) {
            e.setDifficulty(1);
        }
        quizQuestionMapper.insert(e);
        return toDTO(e);
    }

    public QuizQuestionAdminResponseDTO update(Long id, QuizQuestionAdminSaveCommandDTO dto) {
        QuizQuestion e = quizQuestionMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("题目不存在");
        }
        validateSave(dto);
        fillEntity(e, dto);
        quizQuestionMapper.updateById(e);
        bumpAssignedStageRevisions(id);
        return toDTO(e);
    }

    public void delete(Long id) {
        QuizQuestion e = quizQuestionMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("题目不存在");
        }
        if (e.getStatus() == null || e.getStatus() != 0) {
            throw new BusinessException("只有停用且未使用的题目可以删除");
        }
        Long stageRefs = quizStageQuestionMapper.selectCount(new LambdaQueryWrapper<org.example.springboot.entity.QuizStageQuestion>()
                .eq(org.example.springboot.entity.QuizStageQuestion::getQuestionId, id));
        Long answerRefs = userQuizRecordMapper.selectCount(new LambdaQueryWrapper<org.example.springboot.entity.UserQuizRecord>()
                .eq(org.example.springboot.entity.UserQuizRecord::getQuestionId, id));
        if (stageRefs > 0 || answerRefs > 0) {
            throw new BusinessException("题目已有编排或答题记录，请改为归档");
        }
        quizQuestionMapper.deleteById(id);
    }

    private void validateSave(QuizQuestionAdminSaveCommandDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getTitle())) {
            throw new BusinessException("题目内容不能为空");
        }
        String type = normalizeQuestionType(dto.getQuestionType());
        dto.setQuestionType(type);
        if (StrUtil.isBlank(dto.getOptionA()) || StrUtil.isBlank(dto.getOptionB())) {
            throw new BusinessException("选项A和B均需填写");
        }
        if (!"TRUE_FALSE".equals(type) && (StrUtil.isBlank(dto.getOptionC()) || StrUtil.isBlank(dto.getOptionD()))) {
            throw new BusinessException("单选题的四个选项均需填写");
        }
        String ca = normalizeAnswer(dto.getCorrectAnswer());
        if (ca == null || ("TRUE_FALSE".equals(type) && !Set.of("A", "B").contains(ca))) {
            throw new BusinessException("正确答案与题型不匹配");
        }
        dto.setCorrectAnswer(ca);
        int status = dto.getStatus() == null ? 0 : dto.getStatus();
        if (status < 0 || status > 2) {
            throw new BusinessException("题目状态无效");
        }
        dto.setStatus(status);
        if ("SCENARIO_CHOICE".equals(type) && status == 1
                && (StrUtil.isBlank(dto.getSceneImagePath()) || StrUtil.isBlank(dto.getSceneImageAlt()))) {
            throw new BusinessException("启用场景题前请上传题图并填写图片说明");
        }
    }

    private static String normalizeQuestionType(String raw) {
        String type = StrUtil.isBlank(raw) ? "SINGLE_CHOICE" : raw.trim().toUpperCase();
        if (!Set.of("TRUE_FALSE", "SINGLE_CHOICE", "SCENARIO_CHOICE").contains(type)) {
            throw new BusinessException("不支持的题型");
        }
        return type;
    }

    private static String normalizeAnswer(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        String u = raw.trim().toUpperCase();
        if ("A".equals(u) || "B".equals(u) || "C".equals(u) || "D".equals(u)) {
            return u;
        }
        return null;
    }

    private void fillEntity(QuizQuestion e, QuizQuestionAdminSaveCommandDTO dto) {
        if (StrUtil.isBlank(e.getQuestionCode())) {
            e.setQuestionCode(StrUtil.isBlank(dto.getQuestionCode())
                    ? "q-" + UUID.randomUUID().toString().replace("-", "")
                    : dto.getQuestionCode().trim());
        }
        e.setModuleCode(StrUtil.isBlank(dto.getModuleCode()) ? "GENERAL" : dto.getModuleCode().trim().toUpperCase());
        e.setQuestionType(dto.getQuestionType());
        e.setTitle(dto.getTitle().trim());
        e.setOptionA(dto.getOptionA().trim());
        e.setOptionB(dto.getOptionB().trim());
        e.setOptionC(StrUtil.isBlank(dto.getOptionC()) ? null : dto.getOptionC().trim());
        e.setOptionD(StrUtil.isBlank(dto.getOptionD()) ? null : dto.getOptionD().trim());
        e.setCorrectAnswer(dto.getCorrectAnswer());
        e.setExplanation(StrUtil.isBlank(dto.getExplanation()) ? null : dto.getExplanation().trim());
        e.setSceneImagePath(StrUtil.isBlank(dto.getSceneImagePath()) ? null : dto.getSceneImagePath().trim());
        e.setSceneImageAlt(StrUtil.isBlank(dto.getSceneImageAlt()) ? null : dto.getSceneImageAlt().trim());
        e.setSceneCaption(StrUtil.isBlank(dto.getSceneCaption()) ? null : dto.getSceneCaption().trim());
        e.setCatagory(StrUtil.isBlank(dto.getCategory()) ? null : dto.getCategory().trim());
        e.setDifficulty(dto.getDifficulty());
        e.setStatus(dto.getStatus());
    }

    public QuizQuestionAdminResponseDTO toDTO(QuizQuestion e) {
        QuizQuestionAdminResponseDTO dto = new QuizQuestionAdminResponseDTO();
        dto.setId(e.getId());
        dto.setQuestionCode(e.getQuestionCode());
        dto.setModuleCode(e.getModuleCode());
        dto.setQuestionType(e.getQuestionType());
        dto.setTitle(e.getTitle());
        dto.setOptionA(e.getOptionA());
        dto.setOptionB(e.getOptionB());
        dto.setOptionC(e.getOptionC());
        dto.setOptionD(e.getOptionD());
        dto.setCorrectAnswer(e.getCorrectAnswer());
        dto.setExplanation(e.getExplanation());
        dto.setSceneImagePath(e.getSceneImagePath());
        dto.setSceneImageAlt(e.getSceneImageAlt());
        dto.setSceneCaption(e.getSceneCaption());
        dto.setCategory(e.getCatagory());
        dto.setDifficulty(e.getDifficulty());
        dto.setStatus(e.getStatus());
        dto.setCreateTime(e.getCreateTime());
        dto.setUpdateTime(e.getUpdateTime());
        return dto;
    }

    private void bumpAssignedStageRevisions(Long questionId) {
        List<org.example.springboot.entity.QuizStageQuestion> refs = quizStageQuestionMapper.selectList(
                new LambdaQueryWrapper<org.example.springboot.entity.QuizStageQuestion>()
                        .eq(org.example.springboot.entity.QuizStageQuestion::getQuestionId, questionId));
        refs.stream().map(org.example.springboot.entity.QuizStageQuestion::getStageCode).distinct().forEach(code -> {
            org.example.springboot.entity.QuizStage stage = quizStageMapper.selectById(code);
            if (stage != null) {
                stage.setRevision((stage.getRevision() == null ? 0 : stage.getRevision()) + 1);
                quizStageMapper.updateById(stage);
            }
        });
    }
}

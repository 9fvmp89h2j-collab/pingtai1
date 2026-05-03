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
import org.springframework.stereotype.Service;

@Service
public class QuizQuestionAdminService {

    @Resource
    private QuizQuestionMapper quizQuestionMapper;

    public Page<QuizQuestionAdminResponseDTO> page(Long current, Long size, String title, String category, Integer difficulty, Integer status) {
        LambdaQueryWrapper<QuizQuestion> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(title)) {
            w.like(QuizQuestion::getTitle, title);
        }
        if (StrUtil.isNotBlank(category)) {
            w.like(QuizQuestion::getCatagory, category);
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
            e.setStatus(1);
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
        return toDTO(e);
    }

    public void delete(Long id) {
        quizQuestionMapper.deleteById(id);
    }

    private void validateSave(QuizQuestionAdminSaveCommandDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getTitle())) {
            throw new BusinessException("题目内容不能为空");
        }
        if (StrUtil.isBlank(dto.getOptionA()) || StrUtil.isBlank(dto.getOptionB())
                || StrUtil.isBlank(dto.getOptionC()) || StrUtil.isBlank(dto.getOptionD())) {
            throw new BusinessException("四个选项均需填写");
        }
        String ca = normalizeAnswer(dto.getCorrectAnswer());
        if (ca == null) {
            throw new BusinessException("正确答案须为 A/B/C/D");
        }
        dto.setCorrectAnswer(ca);
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
        e.setTitle(dto.getTitle().trim());
        e.setOptionA(dto.getOptionA().trim());
        e.setOptionB(dto.getOptionB().trim());
        e.setOptionC(dto.getOptionC().trim());
        e.setOptionD(dto.getOptionD().trim());
        e.setCorrectAnswer(dto.getCorrectAnswer());
        e.setExplanation(StrUtil.isBlank(dto.getExplanation()) ? null : dto.getExplanation().trim());
        e.setCatagory(StrUtil.isBlank(dto.getCategory()) ? null : dto.getCategory().trim());
        e.setDifficulty(dto.getDifficulty());
        e.setStatus(dto.getStatus());
    }

    private QuizQuestionAdminResponseDTO toDTO(QuizQuestion e) {
        QuizQuestionAdminResponseDTO dto = new QuizQuestionAdminResponseDTO();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setOptionA(e.getOptionA());
        dto.setOptionB(e.getOptionB());
        dto.setOptionC(e.getOptionC());
        dto.setOptionD(e.getOptionD());
        dto.setCorrectAnswer(e.getCorrectAnswer());
        dto.setExplanation(e.getExplanation());
        dto.setCategory(e.getCatagory());
        dto.setDifficulty(e.getDifficulty());
        dto.setStatus(e.getStatus());
        dto.setCreateTime(e.getCreateTime());
        dto.setUpdateTime(e.getUpdateTime());
        return dto;
    }
}

package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.QuizResultSubmitCommandDTO;
import org.example.springboot.dto.response.QuizAnswerResultDTO;
import org.example.springboot.dto.response.QuizQuestionPublicDTO;
import org.example.springboot.dto.response.QuizMistakeAnswerResultDTO;
import org.example.springboot.dto.response.UserQuizMistakeQuestionDTO;
import org.example.springboot.dto.response.UserQuizStatsResponseDTO;
import org.example.springboot.entity.QuizQuestion;
import org.example.springboot.dto.response.QuizHistoryItemDTO;
import org.example.springboot.entity.UserQuizRecord;
import org.example.springboot.entity.UserQuizMistake;
import org.example.springboot.entity.UserQuizStats;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.QuizQuestionMapper;
import org.example.springboot.mapper.UserQuizRecordMapper;
import org.example.springboot.mapper.UserQuizMistakeMapper;
import org.example.springboot.mapper.UserQuizStatsMapper;
import org.example.springboot.service.BadgeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final String[] LETTERS = {"A", "B", "C", "D"};

    @Resource
    private QuizQuestionMapper quizQuestionMapper;
    @Resource
    private UserQuizStatsMapper userQuizStatsMapper;
    @Resource
    private UserQuizRecordMapper userQuizRecordMapper;
    @Resource
    private UserQuizMistakeMapper userQuizMistakeMapper;
    @Resource
    private BadgeService badgeService;

    public List<QuizQuestionPublicDTO> listRandomQuestions(int count) {
        int n = Math.min(Math.max(count, 1), 50);
        LambdaQueryWrapper<QuizQuestion> w = new LambdaQueryWrapper<>();
        w.and(x -> x.eq(QuizQuestion::getStatus, 1).or().isNull(QuizQuestion::getStatus));
        w.last("ORDER BY RAND() LIMIT " + n);
        List<QuizQuestion> list = quizQuestionMapper.selectList(w);
        return list.stream().map(this::toPublic).collect(Collectors.toList());
    }

    private QuizQuestionPublicDTO toPublic(QuizQuestion q) {
        QuizQuestionPublicDTO dto = new QuizQuestionPublicDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setOptionA(q.getOptionA());
        dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC());
        dto.setOptionD(q.getOptionD());
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public QuizAnswerResultDTO submitAnswer(Long userId, Long questionId, String userAnswerRaw) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        QuizQuestion q = quizQuestionMapper.selectById(questionId);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        if (q.getStatus() != null && q.getStatus() == 0) {
            throw new BusinessException("该题目已禁用");
        }
        String userLetter = normalizeUserLetter(userAnswerRaw);
        if (userLetter.isEmpty()) {
            throw new BusinessException("答案格式无效，请提交 A/B/C/D");
        }
        String correctLetter = resolveCorrectLetter(q);
        boolean ok = userLetter.equals(correctLetter);

        UserQuizRecord rec = new UserQuizRecord();
        rec.setUserId(userId);
        rec.setQuestionId(questionId);
        rec.setUserAnswer(userLetter);
        rec.setIsCorrect(ok ? 1 : 0);
        rec.setStatus(0);
        userQuizRecordMapper.insert(rec);

        bumpStats(userId, ok);
        syncMistakeTable(userId, questionId, ok);

        QuizAnswerResultDTO.QuizAnswerResultDTOBuilder b = QuizAnswerResultDTO.builder()
                .correct(ok)
                .correctAnswer(correctLetter)
                .explanation(q.getExplanation())
                .catagory(q.getCatagory());
        return b.build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitLegacyBatch(Long userId, QuizResultSubmitCommandDTO dto) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new BusinessException("答题明细为空");
        }
        for (QuizResultSubmitCommandDTO.QuizResultAnswerItemDTO item : dto.getAnswers()) {
            int idx = item.getSelectedAnswer();
            if (idx < 0 || idx > 3) {
                throw new BusinessException("选项下标无效");
            }
            submitAnswer(userId, item.getQuestionId(), LETTERS[idx]);
        }
    }

    /**
     * 我的错题列表（不含正确答案）
     */
    public List<UserQuizMistakeQuestionDTO> listMyMistakes(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<UserQuizMistake> w = new LambdaQueryWrapper<>();
        w.eq(UserQuizMistake::getUserId, userId).orderByDesc(UserQuizMistake::getCreateTime);
        List<UserQuizMistake> mistakes = userQuizMistakeMapper.selectList(w);
        if (mistakes == null || mistakes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> qids = mistakes.stream().map(UserQuizMistake::getQuestionId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<QuizQuestion> qw = new LambdaQueryWrapper<>();
        qw.in(QuizQuestion::getId, qids);
        qw.and(x -> x.eq(QuizQuestion::getStatus, 1).or().isNull(QuizQuestion::getStatus));
        List<QuizQuestion> qs = quizQuestionMapper.selectList(qw);
        Map<Long, QuizQuestion> qMap = new LinkedHashMap<>();
        for (QuizQuestion q : qs) {
            qMap.put(q.getId(), q);
        }

        List<UserQuizMistakeQuestionDTO> out = new ArrayList<>();
        for (UserQuizMistake m : mistakes) {
            QuizQuestion q = qMap.get(m.getQuestionId());
            if (q == null) {
                continue;
            }
            UserQuizMistakeQuestionDTO dto = new UserQuizMistakeQuestionDTO();
            dto.setMistakeId(m.getId());
            dto.setQuestionId(q.getId());
            dto.setTitle(q.getTitle());
            dto.setOptionA(q.getOptionA());
            dto.setOptionB(q.getOptionB());
            dto.setOptionC(q.getOptionC());
            dto.setOptionD(q.getOptionD());
            dto.setCreateTime(m.getCreateTime());
            out.add(dto);
        }
        return out;
    }

    /**
     * 错题本重新作答：不返回正确答案；做对则移除错题，做错则保留
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizMistakeAnswerResultDTO submitMistakeAnswer(Long userId, Long mistakeId, Long questionId, String userAnswerRaw) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (mistakeId == null || questionId == null) {
            throw new BusinessException("参数错误");
        }
        // 校验该错题归属当前用户
        UserQuizMistake m = userQuizMistakeMapper.selectById(mistakeId);
        if (m == null || !userId.equals(m.getUserId()) || !questionId.equals(m.getQuestionId())) {
            throw new BusinessException("错题不存在");
        }

        QuizQuestion q = quizQuestionMapper.selectById(questionId);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        if (q.getStatus() != null && q.getStatus() == 0) {
            throw new BusinessException("该题目已禁用");
        }
        String userLetter = normalizeUserLetter(userAnswerRaw);
        if (userLetter.isEmpty()) {
            throw new BusinessException("答案格式无效，请提交 A/B/C/D");
        }
        String correctLetter = resolveCorrectLetter(q);
        boolean ok = userLetter.equals(correctLetter);

        UserQuizRecord rec = new UserQuizRecord();
        rec.setUserId(userId);
        rec.setQuestionId(questionId);
        rec.setUserAnswer(userLetter);
        rec.setIsCorrect(ok ? 1 : 0);
        rec.setStatus(0);
        userQuizRecordMapper.insert(rec);
        bumpStats(userId, ok);

        if (ok) {
            userQuizMistakeMapper.deleteById(mistakeId);
        }
        return new QuizMistakeAnswerResultDTO(ok);
    }

    public UserQuizStatsResponseDTO getUserStats(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<UserQuizStats> w = new LambdaQueryWrapper<>();
        w.eq(UserQuizStats::getUserId, userId);
        UserQuizStats s = userQuizStatsMapper.selectOne(w);
        UserQuizStatsResponseDTO dto = new UserQuizStatsResponseDTO();
        dto.setUserId(userId);
        if (s == null) {
            dto.setTotalCount(0);
            dto.setCorrectCount(0);
            dto.setLastQuizTime(null);
        } else {
            dto.setTotalCount(s.getTotalCount() != null ? s.getTotalCount() : 0);
            dto.setCorrectCount(s.getCorrectCount() != null ? s.getCorrectCount() : 0);
            dto.setLastQuizTime(s.getLastQuizTime());
        }
        return dto;
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<QuizHistoryItemDTO> pageHistory(Long userId, long current, long size) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<UserQuizRecord> w = new LambdaQueryWrapper<>();
        w.eq(UserQuizRecord::getUserId, userId).orderByDesc(UserQuizRecord::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserQuizRecord> page =
                userQuizRecordMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), w);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<QuizHistoryItemDTO> out =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<QuizHistoryItemDTO> list = new ArrayList<>();
        for (UserQuizRecord r : page.getRecords()) {
            QuizHistoryItemDTO it = new QuizHistoryItemDTO();
            it.setId(r.getId());
            it.setQuestionId(r.getQuestionId());
            it.setUserAnswer(r.getUserAnswer());
            it.setCorrect(r.getIsCorrect() != null && r.getIsCorrect() == 1);
            it.setCreateTime(r.getCreateTime());
            list.add(it);
        }
        out.setRecords(list);
        return out;
    }

    private void bumpStats(Long userId, boolean correct) {
        LambdaQueryWrapper<UserQuizStats> w = new LambdaQueryWrapper<>();
        w.eq(UserQuizStats::getUserId, userId);
        UserQuizStats s = userQuizStatsMapper.selectOne(w);
        LocalDateTime now = LocalDateTime.now();
        if (s == null) {
            s = new UserQuizStats();
            s.setUserId(userId);
            s.setTotalCount(1);
            s.setCorrectCount(correct ? 1 : 0);
            s.setLastQuizTime(now);
            userQuizStatsMapper.insert(s);
        } else {
            int tc = (s.getTotalCount() != null ? s.getTotalCount() : 0) + 1;
            int cc = (s.getCorrectCount() != null ? s.getCorrectCount() : 0) + (correct ? 1 : 0);
            s.setTotalCount(tc);
            s.setCorrectCount(cc);
            s.setLastQuizTime(now);
            userQuizStatsMapper.updateById(s);
        }
    }

    private void syncMistakeTable(Long userId, Long questionId, boolean correct) {
        if (userId == null || questionId == null) {
            return;
        }
        LambdaQueryWrapper<UserQuizMistake> w = new LambdaQueryWrapper<>();
        w.eq(UserQuizMistake::getUserId, userId).eq(UserQuizMistake::getQuestionId, questionId);

        if (correct) {
            userQuizMistakeMapper.delete(w);
            return;
        }
        UserQuizMistake exists = userQuizMistakeMapper.selectOne(w);
        if (exists != null) {
            return;
        }
        UserQuizMistake m = new UserQuizMistake();
        m.setUserId(userId);
        m.setQuestionId(questionId);
        userQuizMistakeMapper.insert(m);
    }

    /** 将库中 correct_answer 规范为 A/B/C/D */
    public String resolveCorrectLetter(QuizQuestion q) {
        String ca = q.getCorrectAnswer();
        if (ca == null || ca.trim().isEmpty()) {
            return "A";
        }
        String t = ca.trim();
        char c0 = Character.toUpperCase(t.charAt(0));
        if (c0 >= 'A' && c0 <= 'D') {
            return String.valueOf(c0);
        }
        if (q.getOptionA() != null && q.getOptionA().trim().equals(t)) {
            return "A";
        }
        if (q.getOptionB() != null && q.getOptionB().trim().equals(t)) {
            return "B";
        }
        if (q.getOptionC() != null && q.getOptionC().trim().equals(t)) {
            return "C";
        }
        if (q.getOptionD() != null && q.getOptionD().trim().equals(t)) {
            return "D";
        }
        return "A";
    }

    private static String normalizeUserLetter(String raw) {
        if (raw == null) {
            return "";
        }
        String u = raw.trim().toUpperCase();
        if (u.isEmpty()) {
            return "";
        }
        char c = u.charAt(0);
        if (c >= 'A' && c <= 'D') {
            return String.valueOf(c);
        }
        return "";
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> onUltimatePerfect(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return badgeService.onUltimatePerfect(userId);
    }
}

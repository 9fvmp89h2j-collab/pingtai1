package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.common.ResultCode;
import org.example.springboot.dto.command.QuizAnswerSubmitCommandDTO;
import org.example.springboot.dto.command.QuizStageAnswerCommandDTO;
import org.example.springboot.dto.command.QuizMistakeAnswerSubmitCommandDTO;
import org.example.springboot.dto.command.QuizResultSubmitCommandDTO;
import org.example.springboot.dto.response.QuizAnswerResultDTO;
import org.example.springboot.dto.response.QuizHistoryItemDTO;
import org.example.springboot.dto.response.QuizMistakeAnswerResultDTO;
import org.example.springboot.dto.response.QuizQuestionPublicDTO;
import org.example.springboot.dto.response.QuizStagePublicDTO;
import org.example.springboot.dto.response.UserQuizMistakeQuestionDTO;
import org.example.springboot.dto.response.UserQuizStatsResponseDTO;
import org.example.springboot.entity.QuizQuestion;
import org.example.springboot.mapper.QuizQuestionMapper;
import org.example.springboot.service.QuizService;
import org.example.springboot.service.QuizStageService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "答题", description = "题库随机抽题、作答记录与统计")
@RequestMapping("/quiz")
@RestController
@Validated
public class QuizController {

    @Resource
    private QuizService quizService;
    @Resource
    private QuizQuestionMapper quizQuestionMapper;
    @Resource
    private QuizStageService quizStageService;

    @Operation(summary = "随机题目（不含答案）")
    @GetMapping("/questions/random")
    public Result<List<QuizQuestionPublicDTO>> randomQuestions(
            @RequestParam(defaultValue = "10") Integer count) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return Result.success(quizService.listRandomQuestions(count));
    }

    @Operation(summary = "提交单题答案（终极考验等）")
    @PostMapping("/answer/submit")
    public Result<QuizAnswerResultDTO> submitAnswer(@Valid @RequestBody QuizAnswerSubmitCommandDTO dto) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return Result.success(quizService.submitAnswer(uid, dto.getQuestionId(), dto.getUserAnswer()));
    }

    @Operation(summary = "获取固定关卡题目（不含答案）")
    @GetMapping("/stages/{stageCode}")
    public Result<QuizStagePublicDTO> stage(@PathVariable String stageCode) {
        return Result.success(quizStageService.getPublicStage(stageCode));
    }

    @Operation(summary = "提交固定关卡单题答案")
    @PostMapping("/stages/{stageCode}/answer")
    public Result<QuizAnswerResultDTO> submitStageAnswer(@PathVariable String stageCode,
                                                         @Valid @RequestBody QuizStageAnswerCommandDTO dto) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        quizStageService.validateAnswer(stageCode, dto.getStageRevision(), dto.getQuestionId());
        return Result.success(quizService.submitAnswer(uid, dto.getQuestionId(), dto.getUserAnswer()));
    }

    @Operation(summary = "批量提交（兼容旧版闯关，选项为0-3）")
    @PostMapping("/result/submit")
    public Result<Void> submitLegacy(@Valid @RequestBody QuizResultSubmitCommandDTO dto) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        quizService.submitLegacyBatch(uid, dto);
        return Result.success();
    }

    @Operation(summary = "用户答题累计统计")
    @GetMapping("/stats/user")
    public Result<UserQuizStatsResponseDTO> userStats() {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return Result.success(quizService.getUserStats(uid));
    }

    @Operation(summary = "答题记录分页")
    @GetMapping("/history")
    public Result<Page<QuizHistoryItemDTO>> history(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return Result.success(quizService.pageHistory(uid, current, size));
    }

    @Operation(summary = "题目解析（详情页用）")
    @GetMapping("/question/{questionId}/explanation")
    public Result<Map<String, Object>> explanation(@PathVariable Long questionId) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        QuizQuestion q = quizQuestionMapper.selectById(questionId);
        if (q == null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "题目不存在");
        }
        if (q.getStatus() != null && q.getStatus() == 0) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "题目不可用");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("explanation", q.getExplanation());
        m.put("catagory", q.getCatagory());
        return Result.success(m);
    }

    @Operation(summary = "我的错题列表（不含正确答案）")
    @GetMapping("/mistakes")
    public Result<List<UserQuizMistakeQuestionDTO>> myMistakes() {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return Result.success(quizService.listMyMistakes(uid));
    }

    @Operation(summary = "错题本重新作答（做对移除，做错保留；不返回正确答案）")
    @PostMapping("/mistakes/answer")
    public Result<QuizMistakeAnswerResultDTO> submitMistakeAnswer(@Valid @RequestBody QuizMistakeAnswerSubmitCommandDTO dto) {
        Long uid = JwtTokenUtils.getCurrentUserId();
        if (uid == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return Result.success(quizService.submitMistakeAnswer(uid, dto.getMistakeId(), dto.getQuestionId(), dto.getUserAnswer()));
    }

}

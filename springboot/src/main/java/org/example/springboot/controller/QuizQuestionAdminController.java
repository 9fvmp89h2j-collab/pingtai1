package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.QuizQuestionAdminSaveCommandDTO;
import org.example.springboot.dto.command.QuizStageSaveCommandDTO;
import org.example.springboot.dto.response.QuizQuestionAdminResponseDTO;
import org.example.springboot.dto.response.QuizStageAdminResponseDTO;
import org.example.springboot.service.QuizQuestionAdminService;
import org.example.springboot.service.QuizStageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "题库管理(后台)", description = "quiz_question 表")
@RequestMapping("/admin/quiz-question")
@RestController
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class QuizQuestionAdminController {

    @Resource
    private QuizQuestionAdminService quizQuestionAdminService;
    @Resource
    private QuizStageService quizStageService;

    @Operation(summary = "分页查询题目")
    @GetMapping("/page")
    public Result<Page<QuizQuestionAdminResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "题目关键词") @RequestParam(required = false) String title,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "所属模块") @RequestParam(required = false) String moduleCode,
            @Parameter(description = "题型") @RequestParam(required = false) String questionType,
            @Parameter(description = "难度") @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return Result.success(quizQuestionAdminService.page(current, size, title, category, moduleCode, questionType, difficulty, status));
    }

    @Operation(summary = "题目详情")
    @GetMapping("/{id}")
    public Result<QuizQuestionAdminResponseDTO> getById(@PathVariable Long id) {
        return Result.success(quizQuestionAdminService.getById(id));
    }

    @Operation(summary = "新增题目")
    @PostMapping("/create")
    public Result<QuizQuestionAdminResponseDTO> create(@RequestBody QuizQuestionAdminSaveCommandDTO dto) {
        return Result.success(quizQuestionAdminService.create(dto));
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    public Result<QuizQuestionAdminResponseDTO> update(@PathVariable Long id, @RequestBody QuizQuestionAdminSaveCommandDTO dto) {
        return Result.success(quizQuestionAdminService.update(id, dto));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        quizQuestionAdminService.delete(id);
        return Result.success();
    }

    @Operation(summary = "查询关卡题目编排")
    @GetMapping("/stage/{stageCode}")
    public Result<QuizStageAdminResponseDTO> getStage(@PathVariable String stageCode) {
        return Result.success(quizStageService.getAdminStage(stageCode));
    }

    @Operation(summary = "保存关卡题目编排")
    @PutMapping("/stage/{stageCode}")
    public Result<QuizStageAdminResponseDTO> saveStage(@PathVariable String stageCode,
                                                        @Valid @RequestBody QuizStageSaveCommandDTO dto) {
        return Result.success(quizStageService.saveStage(stageCode, dto.getQuestionIds()));
    }
}

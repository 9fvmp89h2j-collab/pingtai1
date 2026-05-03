package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.command.QuizQuestionAdminSaveCommandDTO;
import org.example.springboot.dto.response.QuizQuestionAdminResponseDTO;
import org.example.springboot.service.QuizQuestionAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "题库管理(后台)", description = "quiz_question 表")
@RequestMapping("/admin/quiz-question")
@RestController
@Validated
public class QuizQuestionAdminController {

    @Resource
    private QuizQuestionAdminService quizQuestionAdminService;

    @Operation(summary = "分页查询题目")
    @GetMapping("/page")
    public Result<Page<QuizQuestionAdminResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "题目关键词") @RequestParam(required = false) String title,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "难度") @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return Result.success(quizQuestionAdminService.page(current, size, title, category, difficulty, status));
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
}

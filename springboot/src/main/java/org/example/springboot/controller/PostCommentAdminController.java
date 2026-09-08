package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.response.PostCommentAdminResponseDTO;
import org.example.springboot.service.PostCommentAdminService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-评论管理", description = "admin post_comment")
@RequestMapping("/admin/post-comment")
@RestController
public class PostCommentAdminController {

    @Resource
    private PostCommentAdminService postCommentAdminService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<PostCommentAdminResponseDTO>> page(@RequestParam(defaultValue = "1") Long current,
                                                         @RequestParam(defaultValue = "10") Long size,
                                                         @RequestParam(required = false) Long postId,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) String keyword) {
        return Result.success(postCommentAdminService.page(current, size, postId, userId, keyword));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<PostCommentAdminResponseDTO> detail(@PathVariable Long id) {
        return Result.success(postCommentAdminService.getById(id));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postCommentAdminService.delete(id);
        return Result.success();
    }
}


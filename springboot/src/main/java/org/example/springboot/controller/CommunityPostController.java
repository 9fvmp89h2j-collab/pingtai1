package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.example.springboot.common.Result;
import org.example.springboot.dto.command.CommunityPostCreateCommandDTO;
import org.example.springboot.dto.command.PostCommentCreateCommandDTO;
import org.example.springboot.dto.command.PostReportCreateCommandDTO;
import org.example.springboot.dto.response.CommunityPostDetailResponseDTO;
import org.example.springboot.dto.response.CommunityPostResponseDTO;
import org.example.springboot.dto.response.MyPostCommentItemResponseDTO;
import org.example.springboot.dto.response.PostCommentResponseDTO;
import org.example.springboot.service.CommunityPostService;
import org.example.springboot.util.JwtTokenUtils;

import java.util.List;

/**
 * 针灸答疑（社区帖子）控制器
 */
@Tag(name = "针灸答疑", description = "发帖、评论、点赞、收藏、举报")
@RequestMapping("/community/post")
@RestController
@Slf4j
@Validated
public class CommunityPostController {

    @Resource
    private CommunityPostService communityPostService;

    @Operation(summary = "发帖")
    @PostMapping("/create")
    public Result<CommunityPostDetailResponseDTO> create(@Valid @RequestBody CommunityPostCreateCommandDTO dto) {
        CommunityPostDetailResponseDTO result = communityPostService.createPost(dto);
        return Result.success(result);
    }

    @Operation(summary = "分页查询帖子")
    @GetMapping("/page")
    public Result<Page<CommunityPostResponseDTO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "标题关键词") @RequestParam(required = false) String title,
            @Parameter(description = "板块：问答专区/分享专区/公告/反馈") @RequestParam(required = false) String postType) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        Page<CommunityPostResponseDTO> result = communityPostService.getPostPage(current, size, title, postType, userId);
        return Result.success(result);
    }

    @Operation(summary = "帖子详情")
    @GetMapping("/{postId}")
    public Result<CommunityPostDetailResponseDTO> detail(
            @Parameter(description = "帖子ID") @PathVariable Long postId) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        CommunityPostDetailResponseDTO result = communityPostService.getPostDetail(postId, userId);
        return Result.success(result);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{postId}/like")
    public Result<Void> like(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        communityPostService.like(postId);
        return Result.success();
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/{postId}/collect")
    public Result<Void> collect(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        communityPostService.collect(postId);
        return Result.success();
    }

    @Operation(summary = "举报帖子")
    @PostMapping("/report")
    public Result<Void> report(@Valid @RequestBody PostReportCreateCommandDTO dto) {
        communityPostService.report(dto);
        return Result.success();
    }

    @Operation(summary = "评论")
    @PostMapping("/comment")
    public Result<PostCommentResponseDTO> comment(@Valid @RequestBody PostCommentCreateCommandDTO dto) {
        PostCommentResponseDTO result = communityPostService.addComment(dto);
        return Result.success(result);
    }

    @Operation(summary = "最新帖子（首页展示）")
    @GetMapping("/latest")
    public Result<List<CommunityPostResponseDTO>> latest(
            @Parameter(description = "条数") @RequestParam(defaultValue = "5") Integer limit,
            @Parameter(description = "板块（可选）") @RequestParam(required = false) String postType) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        List<CommunityPostResponseDTO> result = communityPostService.listLatest(limit, postType, userId);
        return Result.success(result);
    }

    @Operation(summary = "我发的帖子")
    @GetMapping("/mine/posts")
    public Result<List<CommunityPostResponseDTO>> minePosts() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(communityPostService.listMyPosts(userId));
    }

    @Operation(summary = "我收藏的帖子")
    @GetMapping("/mine/collects")
    public Result<List<CommunityPostResponseDTO>> mineCollects() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(communityPostService.listMyCollectedPosts(userId));
    }

    @Operation(summary = "我点赞过的帖子")
    @GetMapping("/mine/likes")
    public Result<List<CommunityPostResponseDTO>> mineLikes() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(communityPostService.listMyLikedPosts(userId));
    }

    @Operation(summary = "我发过的评论")
    @GetMapping("/mine/comments")
    public Result<List<MyPostCommentItemResponseDTO>> mineComments() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(communityPostService.listMyComments(userId));
    }
}

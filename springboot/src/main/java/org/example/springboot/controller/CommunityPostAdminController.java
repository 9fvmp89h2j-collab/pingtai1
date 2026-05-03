package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import jakarta.validation.Valid;
import org.example.springboot.dto.command.CommunityPostAdminCreateCommandDTO;
import org.example.springboot.dto.response.CommunityPostAdminResponseDTO;
import org.example.springboot.service.CommunityPostAdminService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-帖子管理", description = "admin community_post")
@RequestMapping("/admin/community-post")
@RestController
public class CommunityPostAdminController {

    @Resource
    private CommunityPostAdminService communityPostAdminService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<CommunityPostAdminResponseDTO>> page(@RequestParam(defaultValue = "1") Long current,
                                                           @RequestParam(defaultValue = "10") Long size,
                                                           @RequestParam(required = false) Long userId,
                                                           @RequestParam(required = false) String title,
                                                           @RequestParam(required = false) String postCatagory,
                                                           @RequestParam(required = false) String postType,
                                                           @RequestParam(required = false) Integer status) {
        return Result.success(communityPostAdminService.page(current, size, userId, title, postCatagory, postType, status));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<CommunityPostAdminResponseDTO> detail(@PathVariable Long id) {
        return Result.success(communityPostAdminService.getById(id));
    }

    @Operation(summary = "管理员发帖")
    @PostMapping
    public Result<CommunityPostAdminResponseDTO> create(@Valid @RequestBody CommunityPostAdminCreateCommandDTO dto) {
        return Result.success(communityPostAdminService.create(dto));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        communityPostAdminService.delete(id);
        return Result.success();
    }
}


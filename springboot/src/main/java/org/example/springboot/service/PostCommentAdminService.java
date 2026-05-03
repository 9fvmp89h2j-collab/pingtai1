package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.PostCommentAdminResponseDTO;
import org.example.springboot.entity.PostComment;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.PostCommentMapper;
import org.springframework.stereotype.Service;

@Service
public class PostCommentAdminService {

    @Resource
    private PostCommentMapper postCommentMapper;

    public Page<PostCommentAdminResponseDTO> page(Long current, Long size, Long postId, Long userId, String keyword) {
        LambdaQueryWrapper<PostComment> w = new LambdaQueryWrapper<>();
        if (postId != null) {
            w.eq(PostComment::getPostId, postId);
        }
        if (userId != null) {
            w.eq(PostComment::getUserId, userId);
        }
        if (StrUtil.isNotBlank(keyword)) {
            w.like(PostComment::getContent, keyword);
        }
        w.orderByDesc(PostComment::getCreateTime);

        Page<PostComment> page = postCommentMapper.selectPage(new Page<>(current, size), w);
        Page<PostCommentAdminResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDto).toList());
        return dtoPage;
    }

    public PostCommentAdminResponseDTO getById(Long id) {
        PostComment e = postCommentMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("评论不存在");
        }
        return toDto(e);
    }

    public void delete(Long id) {
        postCommentMapper.deleteById(id);
    }

    private PostCommentAdminResponseDTO toDto(PostComment e) {
        PostCommentAdminResponseDTO dto = new PostCommentAdminResponseDTO();
        dto.setId(e.getId());
        dto.setPostId(e.getPostId());
        dto.setUserId(e.getUserId());
        dto.setContent(e.getContent());
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}


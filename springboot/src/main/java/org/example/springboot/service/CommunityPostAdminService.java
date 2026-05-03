package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.constant.CommunityPostType;
import org.example.springboot.dto.command.CommunityPostAdminCreateCommandDTO;
import org.example.springboot.dto.response.CommunityPostAdminResponseDTO;
import org.example.springboot.entity.CommunityPost;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.CommunityPostMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityPostAdminService {

    @Resource
    private CommunityPostMapper communityPostMapper;

    public Page<CommunityPostAdminResponseDTO> page(Long current,
                                                   Long size,
                                                   Long userId,
                                                   String title,
                                                   String postCatagory,
                                                   String postType,
                                                   Integer status) {
        LambdaQueryWrapper<CommunityPost> w = new LambdaQueryWrapper<>();
        if (userId != null) {
            w.eq(CommunityPost::getUserId, userId);
        }
        if (StrUtil.isNotBlank(title)) {
            final String kw = title.trim();
            w.and(x -> x.like(CommunityPost::getTitle, kw).or().like(CommunityPost::getContent, kw));
        }
        if (StrUtil.isNotBlank(postCatagory)) {
            w.like(CommunityPost::getPostCatagory, postCatagory);
        }
        if (StrUtil.isNotBlank(postType)) {
            w.eq(CommunityPost::getPostType, postType.trim());
        }
        if (status != null) {
            w.eq(CommunityPost::getStatus, status);
        }
        w.orderByDesc(CommunityPost::getCreateTime);

        Page<CommunityPost> page = communityPostMapper.selectPage(new Page<>(current, size), w);
        Page<CommunityPostAdminResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDto).toList());
        return dtoPage;
    }

    public CommunityPostAdminResponseDTO getById(Long id) {
        CommunityPost e = communityPostMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("帖子不存在");
        }
        return toDto(e);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityPostAdminResponseDTO create(CommunityPostAdminCreateCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null || !JwtTokenUtils.isAdmin()) {
            throw new BusinessException("无权限");
        }
        String pt = dto.getPostType().trim();
        if (!CommunityPostType.isAnyKnown(pt)) {
            throw new BusinessException("板块类型无效");
        }
        CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent() != null ? dto.getContent() : "")
                .likeCount(0)
                .collectCount(0)
                .commentCount(0)
                .postType(pt)
                .postCatagory(StrUtil.isNotBlank(dto.getPostCatagory()) ? dto.getPostCatagory().trim() : null)
                .status(0)
                .build();
        communityPostMapper.insert(post);
        return toDto(post);
    }

    public void delete(Long id) {
        communityPostMapper.deleteById(id);
    }

    private CommunityPostAdminResponseDTO toDto(CommunityPost e) {
        CommunityPostAdminResponseDTO dto = new CommunityPostAdminResponseDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setTitle(e.getTitle());
        dto.setContent(e.getContent());
        dto.setLikeCount(e.getLikeCount());
        dto.setCollectCount(e.getCollectCount());
        dto.setCommentCount(e.getCommentCount());
        dto.setPostCatagory(e.getPostCatagory());
        dto.setPostType(e.getPostType());
        dto.setStatus(e.getStatus());
        dto.setCreateTime(e.getCreateTime());
        dto.setUpdateTime(e.getUpdateTime());
        return dto;
    }
}


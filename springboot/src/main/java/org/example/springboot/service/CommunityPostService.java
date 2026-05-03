package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.springboot.constant.CommunityPostType;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.example.springboot.dto.command.*;
import org.example.springboot.dto.response.*;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.util.JwtTokenUtils;

/**
 * 针灸答疑帖子服务
 */
@Slf4j
@Service
public class CommunityPostService {

    private static final int NORMAL_STATUS = 0;
    private static final int MAX_CONTENT_SUMMARY_LEN = 150;

    @Resource
    private CommunityPostMapper postMapper;
    @Resource
    private PostCommentMapper commentMapper;
    @Resource
    private PostLikeMapper likeMapper;
    @Resource
    private PostCollectMapper collectMapper;
    @Resource
    private PostReportMapper reportMapper;
    @Resource
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public CommunityPostDetailResponseDTO createPost(CommunityPostCreateCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录后再发帖");
        }
        String resolvedType = resolvePostTypeForCreate(dto.getPostType());
        CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent() != null ? dto.getContent() : "")
                .postPic1(dto.getPostPic1())
                .postPic2(dto.getPostPic2())
                .postPic3(dto.getPostPic3())
                .postPic4(dto.getPostPic4())
                .postPic5(dto.getPostPic5())
                .likeCount(0)
                .collectCount(0)
                .commentCount(0)
                .postType(resolvedType)
                .status(NORMAL_STATUS)
                .build();
        postMapper.insert(post);
        log.info("发帖成功: postId={}, userId={}", post.getId(), userId);
        return getPostDetail(post.getId(), userId);
    }

    public Page<CommunityPostResponseDTO> getPostPage(Long current, Long size, String title, String postType, Long currentUserId) {
        Page<CommunityPost> page = new Page<>(current, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getStatus, NORMAL_STATUS)
                .orderByDesc(CommunityPost::getCreateTime);
        if (StrUtil.isNotBlank(postType)) {
            wrapper.eq(CommunityPost::getPostType, postType.trim());
        }
        if (StrUtil.isNotBlank(title)) {
            final String kw = title.trim();
            wrapper.and(w -> w.like(CommunityPost::getTitle, kw).or().like(CommunityPost::getContent, kw));
        }
        Page<CommunityPost> result = postMapper.selectPage(page, wrapper);
        List<CommunityPostResponseDTO> list = result.getRecords().stream()
                .map(p -> toResponseDTO(p, currentUserId))
                .collect(Collectors.toList());
        Page<CommunityPostResponseDTO> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(list);
        return responsePage;
    }

    public CommunityPostDetailResponseDTO getPostDetail(Long postId, Long currentUserId) {
        CommunityPost post = postMapper.selectById(postId);
        //if (post == null || !NORMAL_STATUS.equals(post.getStatus())) {
        // 修复后
        if (post == null || post.getStatus() != NORMAL_STATUS) {
            throw new BusinessException("帖子不存在或已删除");
        }
        CommunityPostDetailResponseDTO dto = new CommunityPostDetailResponseDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPostPic1(post.getPostPic1());
        dto.setPostPic2(post.getPostPic2());
        dto.setPostPic3(post.getPostPic3());
        dto.setPostPic4(post.getPostPic4());
        dto.setPostPic5(post.getPostPic5());
        dto.setLikeCount(post.getLikeCount() != null ? post.getLikeCount() : 0);
        dto.setCollectCount(post.getCollectCount() != null ? post.getCollectCount() : 0);
        dto.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() : 0);
        dto.setPostType(post.getPostType());
        dto.setCreateTime(post.getCreateTime());
        User user = userMapper.selectById(post.getUserId());
        dto.setUsername(user != null ? user.getUsername() : "匿名");
        dto.setLiked(currentUserId != null && hasLiked(postId, currentUserId));
        dto.setCollected(currentUserId != null && hasCollected(postId, currentUserId));

        LambdaQueryWrapper<PostComment> cw = new LambdaQueryWrapper<>();
        cw.eq(PostComment::getPostId, postId).orderByAsc(PostComment::getCreateTime);
        List<PostComment> comments = commentMapper.selectList(cw);
        List<PostCommentResponseDTO> commentDTOs = new ArrayList<>();
        for (PostComment c : comments) {
            PostCommentResponseDTO cd = new PostCommentResponseDTO();
            cd.setId(c.getId());
            cd.setPostId(c.getPostId());
            cd.setUserId(c.getUserId());
            cd.setContent(c.getContent());
            cd.setCreateTime(c.getCreateTime());
            User cu = userMapper.selectById(c.getUserId());
            cd.setUsername(cu != null ? cu.getUsername() : "匿名");
            commentDTOs.add(cd);
        }
        dto.setComments(commentDTOs);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void like(Long postId) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        CommunityPost post = postMapper.selectById(postId);
        //if (post == null || !NORMAL_STATUS.equals(post.getStatus())) {
        // 修复后
        if (post == null || post.getStatus() != NORMAL_STATUS) {
            throw new BusinessException("帖子不存在或已删除");
        }
        LambdaQueryWrapper<PostLike> w = new LambdaQueryWrapper<>();
        w.eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId);
        if (likeMapper.selectCount(w) > 0) {
            likeMapper.delete(w);
            post.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0 : post.getLikeCount()) - 1));
            postMapper.updateById(post);
            log.info("取消点赞: postId={}, userId={}", postId, userId);
        } else {
            likeMapper.insert(PostLike.builder().postId(postId).userId(userId).build());
            post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
            postMapper.updateById(post);
            log.info("点赞: postId={}, userId={}", postId, userId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void collect(Long postId) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        CommunityPost post = postMapper.selectById(postId);
        //if (post == null || !NORMAL_STATUS.equals(post.getStatus())) {
        // 修复后
        if (post == null || post.getStatus() != NORMAL_STATUS) {
            throw new BusinessException("帖子不存在或已删除");
        }
        LambdaQueryWrapper<PostCollect> w = new LambdaQueryWrapper<>();
        w.eq(PostCollect::getPostId, postId).eq(PostCollect::getUserId, userId);
        if (collectMapper.selectCount(w) > 0) {
            collectMapper.delete(w);
            post.setCollectCount(Math.max(0, (post.getCollectCount() == null ? 0 : post.getCollectCount()) - 1));
            postMapper.updateById(post);
        } else {
            collectMapper.insert(PostCollect.builder().postId(postId).userId(userId).build());
            post.setCollectCount((post.getCollectCount() == null ? 0 : post.getCollectCount()) + 1);
            postMapper.updateById(post);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void report(PostReportCreateCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        CommunityPost post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        PostReport report = PostReport.builder()
                .postId(dto.getPostId())
                .reporterUserId(userId)
                .reason(dto.getReason() != null ? dto.getReason() : "")
                .status(0)
                .build();
        reportMapper.insert(report);
        log.info("举报帖子: postId={}, reporterId={}", dto.getPostId(), userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PostCommentResponseDTO addComment(PostCommentCreateCommandDTO dto) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录后再评论");
        }
        CommunityPost post = postMapper.selectById(dto.getPostId());
        //if (post == null || !NORMAL_STATUS.equals(post.getStatus())) {
        // 修复后
        if (post == null || post.getStatus() != NORMAL_STATUS) {
            throw new BusinessException("帖子不存在或已删除");
        }
        PostComment comment = PostComment.builder()
                .postId(dto.getPostId())
                .userId(userId)
                .content(dto.getContent() != null ? dto.getContent() : "")
                .build();
        commentMapper.insert(comment);
        post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        postMapper.updateById(post);
        PostCommentResponseDTO res = new PostCommentResponseDTO();
        res.setId(comment.getId());
        res.setPostId(comment.getPostId());
        res.setUserId(comment.getUserId());
        res.setContent(comment.getContent());
        res.setCreateTime(comment.getCreateTime());
        User user = userMapper.selectById(userId);
        res.setUsername(user != null ? user.getUsername() : "匿名");
        return res;
    }

    public List<CommunityPostResponseDTO> listLatest(int limit, String postType, Long currentUserId) {
        LambdaQueryWrapper<CommunityPost> w = new LambdaQueryWrapper<>();
        w.eq(CommunityPost::getStatus, NORMAL_STATUS);
        if (StrUtil.isNotBlank(postType)) {
            w.eq(CommunityPost::getPostType, postType.trim());
        }
        w.orderByDesc(CommunityPost::getCreateTime).last("LIMIT " + limit);
        List<CommunityPost> list = postMapper.selectList(w);
        return list.stream().map(p -> toResponseDTO(p, currentUserId)).collect(Collectors.toList());
    }

    public List<CommunityPostResponseDTO> listMyPosts(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<CommunityPost> w = new LambdaQueryWrapper<>();
        w.eq(CommunityPost::getUserId, userId)
                .eq(CommunityPost::getStatus, NORMAL_STATUS)
                .orderByDesc(CommunityPost::getCreateTime);
        List<CommunityPost> posts = postMapper.selectList(w);
        return posts.stream().map(p -> toResponseDTO(p, userId)).collect(Collectors.toList());
    }

    public List<CommunityPostResponseDTO> listMyCollectedPosts(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<PostCollect> cw = new LambdaQueryWrapper<>();
        cw.eq(PostCollect::getUserId, userId).orderByDesc(PostCollect::getCreateTime);
        List<PostCollect> rows = collectMapper.selectList(cw);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = rows.stream().map(PostCollect::getPostId).collect(Collectors.toList());
        return mapPostsByOrderedIds(postIds, userId);
    }

    public List<CommunityPostResponseDTO> listMyLikedPosts(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<PostLike> lw = new LambdaQueryWrapper<>();
        lw.eq(PostLike::getUserId, userId).orderByDesc(PostLike::getCreateTime);
        List<PostLike> rows = likeMapper.selectList(lw);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = rows.stream().map(PostLike::getPostId).collect(Collectors.toList());
        return mapPostsByOrderedIds(postIds, userId);
    }

    public List<MyPostCommentItemResponseDTO> listMyComments(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<PostComment> cw = new LambdaQueryWrapper<>();
        cw.eq(PostComment::getUserId, userId).orderByDesc(PostComment::getCreateTime);
        List<PostComment> comments = commentMapper.selectList(cw);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = comments.stream().map(PostComment::getPostId).distinct().collect(Collectors.toList());
        Map<Long, CommunityPost> postMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<CommunityPost> posts = postMapper.selectBatchIds(postIds);
            for (CommunityPost post : posts) {
                if (post != null && post.getStatus() == NORMAL_STATUS) {
                    postMap.put(post.getId(), post);
                }
            }
        }
        List<MyPostCommentItemResponseDTO> out = new ArrayList<>();
        for (PostComment c : comments) {
            CommunityPost post = postMap.get(c.getPostId());
            if (post == null) {
                continue;
            }
            MyPostCommentItemResponseDTO dto = new MyPostCommentItemResponseDTO();
            dto.setCommentId(c.getId());
            dto.setPostId(post.getId());
            dto.setPostTitle(post.getTitle());
            dto.setPostContentSummary(toContentSummary(post.getContent()));
            dto.setCommentContent(c.getContent());
            dto.setCreateTime(c.getCreateTime());
            out.add(dto);
        }
        return out;
    }

    private List<CommunityPostResponseDTO> mapPostsByOrderedIds(List<Long> orderedPostIds, Long currentUserId) {
        if (orderedPostIds == null || orderedPostIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<CommunityPost> posts = postMapper.selectBatchIds(orderedPostIds);
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, CommunityPost> postMap = new HashMap<>();
        for (CommunityPost post : posts) {
            if (post != null && post.getStatus() == NORMAL_STATUS) {
                postMap.put(post.getId(), post);
            }
        }
        List<CommunityPostResponseDTO> out = new ArrayList<>();
        for (Long postId : orderedPostIds) {
            CommunityPost post = postMap.get(postId);
            if (post == null) {
                continue;
            }
            out.add(toResponseDTO(post, currentUserId));
        }
        return out;
    }

    private String resolvePostTypeForCreate(String requested) {
        String t = StrUtil.isBlank(requested) ? null : requested.trim();
        // 意见箱：任意登录用户可提交「反馈」
        if (CommunityPostType.FEEDBACK.equals(t)) {
            return CommunityPostType.FEEDBACK;
        }
        // 「公告」仅管理员可发（用户端发帖弹窗对普通用户不展示此项）
        if (CommunityPostType.ANNOUNCE.equals(t)) {
            if (!JwtTokenUtils.isAdmin()) {
                throw new BusinessException("只有管理员可以发布公告");
            }
            return CommunityPostType.ANNOUNCE;
        }
        if (JwtTokenUtils.isAdmin()) {
            if (StrUtil.isBlank(t)) {
                return CommunityPostType.SHARE;
            }
            if (!CommunityPostType.isAnyKnown(t)) {
                throw new BusinessException("板块类型无效，请选择：分享专区、问答专区、公告、反馈");
            }
            return t;
        }
        if (StrUtil.isBlank(t)) {
            return CommunityPostType.SHARE;
        }
        if (!CommunityPostType.isAllowedForUserNormalPost(t)) {
            throw new BusinessException("只能选择分享专区或问答专区");
        }
        return t;
    }

    private CommunityPostResponseDTO toResponseDTO(CommunityPost p, Long currentUserId) {
        CommunityPostResponseDTO dto = new CommunityPostResponseDTO();
        dto.setId(p.getId());
        dto.setUserId(p.getUserId());
        dto.setTitle(p.getTitle());
        dto.setContentSummary(toContentSummary(p.getContent()));
        dto.setLikeCount(p.getLikeCount() != null ? p.getLikeCount() : 0);
        dto.setCollectCount(p.getCollectCount() != null ? p.getCollectCount() : 0);
        dto.setCommentCount(p.getCommentCount() != null ? p.getCommentCount() : 0);
        dto.setPostType(p.getPostType());
        dto.setCreateTime(p.getCreateTime());
        User user = userMapper.selectById(p.getUserId());
        dto.setUsername(user != null ? user.getUsername() : "匿名");
        dto.setLiked(currentUserId != null && hasLiked(p.getId(), currentUserId));
        dto.setCollected(currentUserId != null && hasCollected(p.getId(), currentUserId));
        return dto;
    }

    private String toContentSummary(String content) {
        if (content != null && content.length() > MAX_CONTENT_SUMMARY_LEN) {
            return content.substring(0, MAX_CONTENT_SUMMARY_LEN) + "...";
        }
        return content;
    }

    private boolean hasLiked(Long postId, Long userId) {
        LambdaQueryWrapper<PostLike> w = new LambdaQueryWrapper<>();
        w.eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId);
        return likeMapper.selectCount(w) > 0;
    }

    private boolean hasCollected(Long postId, Long userId) {
        LambdaQueryWrapper<PostCollect> w = new LambdaQueryWrapper<>();
        w.eq(PostCollect::getPostId, postId).eq(PostCollect::getUserId, userId);
        return collectMapper.selectCount(w) > 0;
    }
}

package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostCommentAdminResponseDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
}


package org.example.springboot.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityPostAdminResponseDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private String postCatagory;
    private String postType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


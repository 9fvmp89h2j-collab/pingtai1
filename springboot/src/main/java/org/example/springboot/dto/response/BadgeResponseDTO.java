package org.example.springboot.dto.response;

import lombok.Data;

@Data
public class BadgeResponseDTO {
    private Long id;
    private Long userId;
    private String badgePath;
    private String badgeName;
}


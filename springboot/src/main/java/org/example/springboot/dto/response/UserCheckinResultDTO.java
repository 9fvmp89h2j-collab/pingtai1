package org.example.springboot.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserCheckinResultDTO {
    private UserDetailResponseDTO userInfo;
    private List<String> earnedBadgeNames;
}


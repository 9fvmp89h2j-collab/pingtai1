package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardLedgerItemResponseDTO {
    private Long id;
    private String source;
    private String taskCode;
    private String rewardType;
    private String itemCode;
    private Integer amount;
    private Integer scoreDelta;
    private LocalDateTime createdAt;
}

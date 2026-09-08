package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainlineRewardConfigDTO {
    private String rewardType;
    private String itemCode;
    private Integer amount;
    private Integer scoreDelta;
}

package org.example.springboot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CopperManDiscoverResponseDTO {
    private List<String> discoveredCodes;
    private Boolean completed;
    private Boolean newlyCompleted;
    private Boolean persisted;
    private Integer copperTokens;
    private Integer starSand;
    private Integer completedCases;
    private Integer rewardCopperTokens;
    private Integer rewardStarSand;
}

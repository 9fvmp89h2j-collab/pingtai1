package org.example.springboot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CopperManDailyCaseResponseDTO {
    private LocalDate caseDate;
    private List<AcupointKnowledgeResponseDTO> featuredPoints;
    private List<String> targetCodes;
    private List<String> discoveredCodes;
    private Integer totalTargets;
    private Integer copperTokens;
    private Integer starSand;
    private Integer completedCases;
    private Boolean persisted;
}

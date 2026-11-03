package org.example.springboot.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class GameStateResponseDTO {
    private Long userId;
    private Integer currentScore;
    private Integer userLevel;
    private String levelName;
    private String currentLevelId;
    private String currentMapKey;
    private List<GameMapLevelStateResponseDTO> levels = new ArrayList<>();
    private List<GameTaskProgressResponseDTO> tasks = new ArrayList<>();
    private Map<String, Integer> materials = new LinkedHashMap<>();
    private Integer copperTokens = 0;
    private Integer starSand = 0;
    private Integer completedCases = 0;
    private List<String> agencyArchiveIds = new ArrayList<>();
    private Boolean legacyImportCompleted = false;
    private Long stateVersion = 0L;
}

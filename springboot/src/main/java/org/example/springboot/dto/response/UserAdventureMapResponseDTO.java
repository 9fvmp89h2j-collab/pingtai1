package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User adventure map state")
public class UserAdventureMapResponseDTO {

    @Schema(description = "Current score")
    private Integer currentScore;

    @Schema(description = "Current user level")
    private Integer userLevel;

    @Schema(description = "Current active map level id")
    private String currentLevelId;

    @Schema(description = "Current map background asset key")
    private String currentMapKey;

    @Schema(description = "All map levels")
    private List<UserAdventureMapLevelResponseDTO> levels;
}

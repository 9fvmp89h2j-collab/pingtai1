package org.example.springboot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Adventure map level state")
public class UserAdventureMapLevelResponseDTO {

    @Schema(description = "Frontend level id")
    private String id;

    @Schema(description = "Display order")
    private Integer order;

    @Schema(description = "Display label")
    private String label;

    @Schema(description = "Unlocked map asset key")
    private String unlockedMapKey;

    @Schema(description = "Route opened by this level")
    private String route;

    @Schema(description = "Required user level")
    private Integer requiredLevel;

    @Schema(description = "Whether this level is unlocked")
    private Boolean unlocked;

    @Schema(description = "Whether this level is completed")
    private Boolean completed;

    @Schema(description = "Whether this is the current active level")
    private Boolean current;

    @Schema(description = "Canonical level state")
    private String status;

    @Schema(description = "Aggregated progress")
    private Integer progress;

    @Schema(description = "Aggregated target")
    private Integer target;

    @Schema(description = "Tasks required to complete this level")
    @Builder.Default
    private List<String> requiredTaskIds = new ArrayList<>();

    @Schema(description = "Next level id")
    private String nextLevelId;
}

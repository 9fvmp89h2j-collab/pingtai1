package org.example.springboot.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameMapLevelStateResponseDTO {
    private String id;
    private Integer order;
    private String label;
    private String route;
    private String status;
    private Boolean unlocked;
    private Boolean completed;
    private Boolean current;
    private Integer progress;
    private Integer target;
    private List<String> requiredTaskIds = new ArrayList<>();
    private String nextLevelId;
}

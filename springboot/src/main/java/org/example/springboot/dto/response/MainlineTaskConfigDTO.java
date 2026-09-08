package org.example.springboot.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MainlineTaskConfigDTO {
    private String taskCode;
    private String levelId;
    private String name;
    private String description;
    private String taskType;
    private String route;
    private Integer target;
    private Boolean editable;
    private List<MainlineRewardConfigDTO> rewards = new ArrayList<>();
}

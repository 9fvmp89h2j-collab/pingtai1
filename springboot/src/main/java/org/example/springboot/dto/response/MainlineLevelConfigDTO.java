package org.example.springboot.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class MainlineLevelConfigDTO {
    private String id;
    private Integer order;
    private String label;
    private String statusText;
    private String description;
    private String icon;
    private String seal;
    private BigDecimal x;
    private BigDecimal y;
    private String route;
    private String mapKey;
    private String coverPath;
    private Boolean publicLevel = true;
    private List<String> requiredTaskIds = new ArrayList<>();
    private List<String> prerequisiteLevelIds = new ArrayList<>();
    private List<MainlineTaskConfigDTO> tasks = new ArrayList<>();
}

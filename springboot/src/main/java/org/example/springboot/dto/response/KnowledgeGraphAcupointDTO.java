package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphAcupointDTO {
    private String id;
    private String name;
    private String meridian;
    private String region;
    private String childLocation;
    private String story;
    private String safetyTip;
    private List<String> confusedWith;
    private List<String> tasks;
    private List<String> rewards;
    private Double x;
    private Double y;
    private Double z;
}

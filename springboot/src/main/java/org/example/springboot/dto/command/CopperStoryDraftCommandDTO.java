package org.example.springboot.dto.command;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CopperStoryDraftCommandDTO {
    private String storyCode;
    private String title;
    private String subtitle;
    private String summary;
    private String coverPath;
    private Integer estimatedMinutes;
    private List<String> linkedAcupoints;
    private List<Map<String, Object>> pages;
    private List<Map<String, Object>> clues;
    private Map<String, Object> reasoning;
    private Map<String, Object> safety;
    private List<Map<String, Object>> rewards;
}

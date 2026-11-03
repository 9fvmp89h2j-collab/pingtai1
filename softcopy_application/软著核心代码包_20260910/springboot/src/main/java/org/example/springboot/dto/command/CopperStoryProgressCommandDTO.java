package org.example.springboot.dto.command;

import lombok.Data;

import java.util.Map;

@Data
public class CopperStoryProgressCommandDTO {
    private Map<String, Object> progress;
    private Boolean completed;
}

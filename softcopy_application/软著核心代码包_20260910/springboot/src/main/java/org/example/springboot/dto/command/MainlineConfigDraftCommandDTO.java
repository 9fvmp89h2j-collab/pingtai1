package org.example.springboot.dto.command;

import lombok.Data;
import org.example.springboot.dto.response.MainlineLevelConfigDTO;

import java.util.ArrayList;
import java.util.List;

@Data
public class MainlineConfigDraftCommandDTO {
    private Long revisionId;
    private Integer expectedVersion;
    private Integer expectedEditVersion;
    private List<MainlineLevelConfigDTO> levels = new ArrayList<>();
}

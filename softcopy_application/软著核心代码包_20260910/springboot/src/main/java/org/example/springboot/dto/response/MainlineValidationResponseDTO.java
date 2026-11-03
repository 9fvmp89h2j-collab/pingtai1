package org.example.springboot.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MainlineValidationResponseDTO {
    private boolean valid;
    private Long revisionId;
    private Integer version;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
}

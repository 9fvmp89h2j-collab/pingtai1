package org.example.springboot.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MainlineAdminConfigResponseDTO {
    private MainlineConfigResponseDTO config;
    private List<MainlineRevisionSummaryDTO> versions = new ArrayList<>();
}

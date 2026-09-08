package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyChoiceRewardResponseDTO {
    private String weekKey;
    private Integer progress;
    private Integer target;
    private Boolean eligible;
    private Boolean claimed;
    private String selectedItemCode;
    private List<WeeklyChoiceOptionResponseDTO> options;
}

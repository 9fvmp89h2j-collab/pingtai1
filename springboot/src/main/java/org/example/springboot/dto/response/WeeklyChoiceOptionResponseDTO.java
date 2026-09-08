package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyChoiceOptionResponseDTO {
    private String itemCode;
    private Integer amount;
}

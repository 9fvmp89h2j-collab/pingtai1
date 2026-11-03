package org.example.springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMutationResponseDTO {
    private String operationId;
    private Boolean idempotentReplay;
    private Long stateVersion;
    private GameStateResponseDTO state;
}

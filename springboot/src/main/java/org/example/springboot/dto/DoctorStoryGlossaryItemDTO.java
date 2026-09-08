package org.example.springboot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "名医故事重点词词条")
public class DoctorStoryGlossaryItemDTO {

    @Schema(description = "重点词")
    private String word;

    @Schema(description = "拼音")
    private String pinyin;

    @Schema(description = "儿童版解释")
    private String meaning;
}

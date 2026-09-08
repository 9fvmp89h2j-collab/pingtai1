package org.example.springboot.dto.response;

import lombok.Data;

@Data
public class ExtraCourseResponseDTO {
    private Integer extraCourseId;
    private String extraCourseName;
    private String extraCourseBrief;
    private String extraCourseDes;
    private String extraCourseIcon;
    private String extraCoursePic1;
    private String extraCoursePic2;
    private String extraCoursePic3;
    private Integer skillId;
}


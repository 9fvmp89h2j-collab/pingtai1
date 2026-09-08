package org.example.springboot.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommunityPostAdminCreateCommandDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200)
    private String title;

    private String content;

    /** 问答专区 / 分享专区 / 公告 / 反馈 */
    @NotBlank(message = "请选择板块")
    private String postType;

    private String postCatagory;
}

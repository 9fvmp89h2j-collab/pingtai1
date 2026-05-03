package org.example.springboot.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "发帖创建命令")
public class CommunityPostCreateCommandDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200)
    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "板块：普通用户可 分享专区/问答专区/反馈(意见箱)；管理员还可选 公告；不传默认 分享专区")
    private String postType;

    @Schema(description = "帖子图片1")
    private String postPic1;

    @Schema(description = "帖子图片2")
    private String postPic2;

    @Schema(description = "帖子图片3")
    private String postPic3;

    @Schema(description = "帖子图片4")
    private String postPic4;

    @Schema(description = "帖子图片5")
    private String postPic5;
}

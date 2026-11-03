package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("body_map_acupoint_knowledge")
public class BodyMapAcupointKnowledge {
    @TableId(value = "acupoint_code", type = IdType.INPUT)
    private String acupointCode;
    private String regionCode;
    private String bodyView;
    private String placementHint;
    private String childMapDescription;
    private Integer sortOrder;
    private Boolean enabled;
}

package org.example.springboot.service;

import org.example.springboot.dto.response.BodyMapAcupointResponseDTO;
import org.example.springboot.entity.AcupointKnowledge;
import org.example.springboot.entity.BodyMapAcupointKnowledge;
import org.example.springboot.mapper.AcupointKnowledgeMapper;
import org.example.springboot.mapper.BodyMapAcupointKnowledgeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BodyMapServiceTest {
    @Mock
    private BodyMapAcupointKnowledgeMapper bodyMapKnowledgeMapper;

    @Mock
    private AcupointKnowledgeMapper acupointKnowledgeMapper;

    @InjectMocks
    private BodyMapService service;

    @Test
    void returnsBodyMapSpecificPlacementKnowledgeInMapOrder() {
        BodyMapAcupointKnowledge mapKnowledge = new BodyMapAcupointKnowledge();
        mapKnowledge.setAcupointCode("LU-1");
        mapKnowledge.setRegionCode("chest");
        mapKnowledge.setBodyView("front");
        mapKnowledge.setPlacementHint("先看左边的正面人物，再点击“胸部”圆圈。沿着箭头把“中府星”送回家。");
        mapKnowledge.setChildMapDescription("“中府”在身体地图中属于胸部。");
        mapKnowledge.setSortOrder(1);
        mapKnowledge.setEnabled(true);

        AcupointKnowledge point = new AcupointKnowledge();
        point.setCode("LU-1");
        point.setPointNumber(1);
        point.setName("中府");
        point.setPinyin("Zhongfu");
        point.setMeridianCode("LU");
        point.setMeridianName("手太阴肺经");
        point.setBodyArea("胸部");
        point.setChildDescription("认识经络文化地图上的中府星。");
        point.setChildTraditionalUse("传统认识中的身体文化知识。");
        point.setSafetyTip("只观察图片，不在自己或同学身上寻找。");
        point.setEnabled(true);

        when(bodyMapKnowledgeMapper.selectList(any())).thenReturn(List.of(mapKnowledge));
        when(acupointKnowledgeMapper.selectBatchIds(anyList())).thenReturn(List.of(point));

        List<BodyMapAcupointResponseDTO> result = service.listAcupoints();

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.getCode()).isEqualTo("LU-1");
            assertThat(item.getRegionCode()).isEqualTo("chest");
            assertThat(item.getBodyView()).isEqualTo("front");
            assertThat(item.getPlacementHint()).contains("左边的正面人物", "胸部", "中府星");
            assertThat(item.getPlacementHint()).doesNotContain("转动3D小铜人");
        });
    }
}

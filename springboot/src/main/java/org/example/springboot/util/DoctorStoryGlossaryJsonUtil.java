package org.example.springboot.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.example.springboot.dto.DoctorStoryGlossaryItemDTO;
import org.example.springboot.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class DoctorStoryGlossaryJsonUtil {

    private static final Logger log = LoggerFactory.getLogger(DoctorStoryGlossaryJsonUtil.class);
    private static final TypeReference<List<DoctorStoryGlossaryItemDTO>> GLOSSARY_LIST_TYPE = new TypeReference<>() {
    };

    private DoctorStoryGlossaryJsonUtil() {
    }

    public static List<DoctorStoryGlossaryItemDTO> parse(String json) {
        if (StrUtil.isBlank(json)) {
            return List.of();
        }

        try {
            List<DoctorStoryGlossaryItemDTO> list = JSON.parseObject(json, GLOSSARY_LIST_TYPE);
            return normalize(list);
        } catch (Exception e) {
            log.warn("Failed to parse doctor story glossary json", e);
            return List.of();
        }
    }

    public static String toJson(List<DoctorStoryGlossaryItemDTO> list) {
        List<DoctorStoryGlossaryItemDTO> normalized = normalize(list);
        if (normalized.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(normalized);
    }

    public static List<DoctorStoryGlossaryItemDTO> normalize(List<DoctorStoryGlossaryItemDTO> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<String, DoctorStoryGlossaryItemDTO> deduped = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            DoctorStoryGlossaryItemDTO item = list.get(i);
            if (item == null) {
                continue;
            }

            String word = StrUtil.trimToNull(item.getWord());
            String pinyin = StrUtil.trimToNull(item.getPinyin());
            String meaning = StrUtil.trimToNull(item.getMeaning());
            if (word == null && pinyin == null && meaning == null) {
                continue;
            }

            int rowNumber = i + 1;
            if (word == null) {
                throw new BusinessException("第" + rowNumber + "个重点词缺少词语");
            }
            if (pinyin == null) {
                throw new BusinessException("第" + rowNumber + "个重点词缺少拼音");
            }
            if (meaning == null) {
                throw new BusinessException("第" + rowNumber + "个重点词缺少解释");
            }

            deduped.put(word, new DoctorStoryGlossaryItemDTO(word, pinyin, meaning));
        }

        return new ArrayList<>(deduped.values());
    }
}

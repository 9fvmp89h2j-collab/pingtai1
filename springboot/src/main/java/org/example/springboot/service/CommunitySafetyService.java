package org.example.springboot.service;

import org.example.springboot.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class CommunitySafetyService {

    private static final Pattern MEDICAL_ADVICE = Pattern.compile(
            "(建议|应该|可以|试试|需要).{0,18}(吃药|用药|针刺|扎针|艾灸|按摩|按压|按穴|治疗|治愈|取穴|配穴)|" +
            "(处方|方剂|剂量|针法|灸法|治疗方案)"
    );
    private static final Pattern PERSONAL_CONTACT = Pattern.compile(
            "(微信|QQ|手机号|电话|住址|家庭地址|学校|班级).{0,20}([0-9A-Za-z_-]{4,})|1[3-9]\\d{9}"
    );

    public void validatePost(String title, String content) {
        validateText((title == null ? "" : title) + "\n" + (content == null ? "" : content));
    }

    public void validateComment(String content) {
        validateText(content == null ? "" : content);
    }

    private void validateText(String text) {
        if (MEDICAL_ADVICE.matcher(text).find()) {
            throw new BusinessException("社区只交流文化和学习体验，不能发布诊断、治疗或操作建议");
        }
        if (PERSONAL_CONTACT.matcher(text).find()) {
            throw new BusinessException("为了保护隐私，请不要发布电话、住址、学校或社交账号");
        }
    }
}

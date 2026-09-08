package org.example.springboot.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AiSafetyService {

    public static final String SAFE_REFUSAL =
            "这个问题需要由家长和专业医生来判断。我只能陪你学习中医文化、身体区域和安全知识，" +
            "不能提供诊断、治疗或操作建议。身体不舒服时，请马上告诉家长或老师。";

    private static final Pattern HIGH_RISK_QUESTION = Pattern.compile(
            "(怎么|如何|应该|能不能|可以不可以|帮我).{0,16}(治|诊断|用药|吃药|扎针|针刺|艾灸|按摩|按穴|急救)|" +
            "(处方|方剂|剂量|药量|取穴|配穴|针法|灸法|按哪个穴位)|" +
            "(发烧|高热|出血|晕倒|昏迷|呼吸困难|胸痛|剧痛|过敏|中毒|自残|自杀).{0,20}(怎么办|怎么处理|怎么治)?"
    );

    private static final Pattern UNSAFE_REPLY = Pattern.compile(
            "(可以|能够|建议|推荐|适合).{0,18}(治疗|治愈|缓解|改善|调理|针刺|艾灸|按压|按摩|用药)|" +
            "(每天|每日|一次|每次).{0,12}(分钟|毫升|克|粒|次)|" +
            "(取穴|配穴|进针|留针|针刺深度|处方|剂量)"
    );

    public boolean requiresSafeRefusal(String prompt) {
        return prompt != null && HIGH_RISK_QUESTION.matcher(prompt.trim()).find();
    }

    public String sanitizeReply(String reply) {
        if (reply == null || reply.isBlank()) {
            return "我暂时没有找到合适的文化知识回答，请换一个问题试试。";
        }
        String normalized = reply.trim();
        return UNSAFE_REPLY.matcher(normalized).find() ? SAFE_REFUSAL : normalized;
    }

    public String localEducationalReply(String prompt) {
        String text = prompt == null ? "" : prompt.trim();
        if (text.contains("经络")) {
            return "经络是传统中医文化中描述身体路线关系的概念。在这里，我们用星点和连线来观察它。";
        }
        if (text.contains("小铜人") || text.contains("铜色") || text.contains("铜人")) {
            return "古代铜人常被用作医学教学模型。铜色来自它的青铜文化形象，我们只在模型上观察学习。";
        }
        if (text.contains("穴位")) {
            return "穴位是传统中医文化中的身体位置名称。学习时只认名称、区域和路线，不在自己身上操作。";
        }
        if (text.contains("安全") || text.contains("注意")) {
            return "记住侦探守则：只观察、只学习，不自己针刺；身体不舒服时马上告诉家长或老师。";
        }
        if (text.contains("故事") || text.contains("历史")) {
            return "你可以去故事馆认识古代铜人、医学人物和文化典籍，再把发现写进侦探档案。";
        }
        return "我可以回答经络名称、小铜人历史、身体区域和安全规则。换一个文化问题试试吧。";
    }
}

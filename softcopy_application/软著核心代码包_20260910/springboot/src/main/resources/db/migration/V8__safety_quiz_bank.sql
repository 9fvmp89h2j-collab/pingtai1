ALTER TABLE `quiz_question`
  ADD COLUMN `question_code` varchar(64) NULL COMMENT '稳定题目编码' AFTER `id`,
  ADD COLUMN `module_code` varchar(32) NOT NULL DEFAULT 'GENERAL' COMMENT '所属题库模块' AFTER `question_code`,
  ADD COLUMN `question_type` varchar(32) NOT NULL DEFAULT 'SINGLE_CHOICE' COMMENT 'TRUE_FALSE/SINGLE_CHOICE/SCENARIO_CHOICE' AFTER `module_code`,
  ADD COLUMN `scene_image_path` varchar(512) NULL COMMENT '场景插画地址' AFTER `explanation`,
  ADD COLUMN `scene_image_alt` varchar(255) NULL COMMENT '场景插画替代文本' AFTER `scene_image_path`,
  ADD COLUMN `scene_caption` varchar(255) NULL COMMENT '场景观察提示' AFTER `scene_image_alt`,
  MODIFY COLUMN `option_c` varchar(200) NULL COMMENT '选项C',
  MODIFY COLUMN `option_d` varchar(200) NULL COMMENT '选项D';

UPDATE `quiz_question`
SET `question_code` = CONCAT('legacy-', `id`)
WHERE `question_code` IS NULL OR `question_code` = '';

ALTER TABLE `quiz_question`
  MODIFY COLUMN `question_code` varchar(64) NOT NULL COMMENT '稳定题目编码',
  ADD UNIQUE KEY `uk_quiz_question_code` (`question_code`),
  ADD KEY `idx_quiz_module_status` (`module_code`, `status`),
  ADD KEY `idx_quiz_type` (`question_type`);

CREATE TABLE `quiz_stage` (
  `stage_code` varchar(64) NOT NULL,
  `module_code` varchar(32) NOT NULL,
  `title` varchar(100) NOT NULL,
  `required_question_count` int NOT NULL DEFAULT 14,
  `revision` bigint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`stage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库关卡配置';

CREATE TABLE `quiz_stage_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stage_code` varchar(64) NOT NULL,
  `question_id` bigint NOT NULL,
  `position` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_stage_position` (`stage_code`, `position`),
  UNIQUE KEY `uk_quiz_stage_question` (`stage_code`, `question_id`),
  KEY `idx_quiz_stage_question_id` (`question_id`),
  CONSTRAINT `fk_quiz_stage_question_stage` FOREIGN KEY (`stage_code`) REFERENCES `quiz_stage` (`stage_code`) ON DELETE CASCADE,
  CONSTRAINT `fk_quiz_stage_question_question` FOREIGN KEY (`question_id`) REFERENCES `quiz_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关卡题目编排';

INSERT INTO `quiz_stage` (`stage_code`, `module_code`, `title`, `required_question_count`, `revision`)
VALUES ('SAFETY_KNOWLEDGE', 'SAFETY_GUARDIAN', '第一关 · 知识闯关', 14, 1);

INSERT INTO `quiz_question`
(`question_code`, `module_code`, `question_type`, `title`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `explanation`, `scene_image_path`, `scene_image_alt`, `scene_caption`, `category`, `difficulty`, `status`)
VALUES
('safety-self-needle', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '小朋友可以自己拿针扎穴位吗？', '可以', '不可以', NULL, NULL, 'B', '不可以。针灸是专业医疗行为，儿童不能自行尝试；看到针具时要远离并告诉可信任的大人。', NULL, NULL, NULL, '安全判断', 1, 1),
('safety-copy-video', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '可以照着网络视频给自己针灸吗？', '可以', '不可以', NULL, NULL, 'B', '不可以。网络视频不能替代专业训练和医生判断；不要模仿治疗动作。', NULL, NULL, NULL, '安全判断', 1, 1),
('safety-tell-adult', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '身体不舒服时，应该告诉家长、老师或医生吗？', '应该', '不用', NULL, NULL, 'A', '应该。身体不舒服要及时告诉可信任的大人，并由专业医务人员判断。', NULL, NULL, NULL, '求助意识', 1, 1),
('safety-not-doctor', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '这个学习系统可以替代医生看病吗？', '可以', '不可以', NULL, NULL, 'B', '不可以。本系统只用于认识身体和学习中医文化，不能代替诊断或治疗。', NULL, NULL, NULL, '系统边界', 1, 1),
('safety-learn-purpose', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '学习穴位知识是为了认识身体和了解中医文化吗？', '是', '不是', NULL, NULL, 'A', '是。我们学习名称、位置和文化知识，不进行真实针刺或治疗。', NULL, NULL, NULL, '学习目的', 1, 1),
('safety-sterile-needles', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '正规针灸治疗会使用一次性无菌针具吗？', '会', '不会', NULL, NULL, 'A', '会。正规医疗需要严格的卫生和专业操作，这也是不能在家自行尝试的原因。', NULL, NULL, NULL, '卫生安全', 1, 1),
('safety-no-sharing', 'SAFETY_GUARDIAN', 'TRUE_FALSE', '一根针灸针可以给不同的人共同使用吗？', '可以', '不可以', NULL, NULL, 'B', '不可以。共用针具存在感染风险；任何针具都不能当作玩具或与他人共用。', NULL, NULL, NULL, '卫生安全', 1, 1),
('safety-stop-classmate', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '同学拿针准备给另一位同学扎穴位，你应该怎么做？', '觉得有趣，一起参与', '假装没看见，悄悄离开', '立即制止，并告诉老师', '站在旁边观看', 'C', '正确行动是立即制止并告诉老师。儿童不能自行针刺，也不能围观或鼓励危险行为。', NULL, '教室里，一名孩子阻止同学拿针靠近另一位同学', '发现危险行为，先阻止，再告诉老师', '校园安全', 1, 1),
('safety-family-treatment', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '肚子不舒服时，家人准备在家用针处理，你应该怎么回应？', '同意在家尝试', '劝阻家人，并去正规医疗机构', '自己查看穴位图', '忍着不说', 'B', '正确行动是劝阻并寻求专业医疗帮助。即使是家人，也不能在没有专业资质和条件时自行针刺。', NULL, '家中女孩劝阻妈妈打开针具盒并准备寻求专业帮助', '关心家人，也要坚持专业医疗原则', '家庭安全', 1, 1),
('safety-online-video', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '网络主播说学会视频里的步骤就能在家针灸，你应该怎么做？', '认真记下操作步骤', '分享给同学一起学习', '购买针具回来练习', '不模仿并关闭视频', 'D', '正确行动是不模仿并关闭视频。网络内容不能替代专业医疗训练，遇到健康问题应询问可信任的大人和医生。', NULL, '孩子关闭展示错误针灸教学的平板电脑', '网络内容要辨别，危险操作不模仿', '网络安全', 1, 1),
('safety-playground-refuse', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '操场上有同学提议用捡到的针玩“针灸游戏”，你应该怎么做？', '坚决拒绝，并告诉老师', '参与游戏，轮流尝试', '站在旁边看', '帮忙查找穴位图', 'A', '正确行动是拒绝、远离并告诉老师。捡到的尖锐物可能造成伤害或感染，绝不能拿来游戏。', NULL, '操场上孩子们远离地面的针状物并向老师求助', '不触碰、不参与，马上告诉老师', '校园安全', 1, 1),
('safety-help-grandma', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '奶奶头疼，准备自己拿针处理，你应该怎么做？', '帮奶奶寻找穴位图', '觉得奶奶有经验，不用管', '劝阻奶奶并陪她就医', '离开去做自己的事', 'C', '正确行动是温柔劝阻并陪同就医。自行针刺可能造成损伤或感染，应交给专业医务人员判断。', NULL, '孩子劝阻头疼的奶奶打开针具盒并准备陪她就医', '温柔劝阻，陪家人寻求专业帮助', '家庭安全', 1, 1),
('safety-found-needle-kit', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '在路边发现一个装有针具的包，你应该怎么做？', '打开看看里面有什么', '带回家收藏', '叫同学一起来看', '不要触碰，告诉大人处理', 'D', '正确行动是保持距离并告诉大人。来历不明的针具可能被污染，不能触碰或带走。', NULL, '社区步道上孩子与针具包保持距离并呼喊家长', '保持距离，让大人安全处理', '公共安全', 1, 1),
('safety-pet-vet', 'SAFETY_GUARDIAN', 'SCENARIO_CHOICE', '小狗身体不舒服，有人想照着图片给它针灸，应该怎么做？', '照着图片给小狗扎针', '带小狗去宠物医院', '请同学帮忙按住小狗', '先在自己身上练习', 'B', '正确行动是带宠物去正规宠物医院。动物治疗也需要专业兽医，儿童不能给宠物或自己针刺。', NULL, '孩子关闭平板并与家长准备带小狗去宠物医院', '爱护宠物，要把治疗交给专业兽医', '宠物安全', 1, 1);

INSERT INTO `quiz_stage_question` (`stage_code`, `question_id`, `position`)
SELECT 'SAFETY_KNOWLEDGE', q.`id`, ordered.`position`
FROM (
  SELECT 'safety-self-needle' code, 1 position UNION ALL
  SELECT 'safety-copy-video', 2 UNION ALL
  SELECT 'safety-tell-adult', 3 UNION ALL
  SELECT 'safety-not-doctor', 4 UNION ALL
  SELECT 'safety-learn-purpose', 5 UNION ALL
  SELECT 'safety-sterile-needles', 6 UNION ALL
  SELECT 'safety-no-sharing', 7 UNION ALL
  SELECT 'safety-stop-classmate', 8 UNION ALL
  SELECT 'safety-family-treatment', 9 UNION ALL
  SELECT 'safety-online-video', 10 UNION ALL
  SELECT 'safety-playground-refuse', 11 UNION ALL
  SELECT 'safety-help-grandma', 12 UNION ALL
  SELECT 'safety-found-needle-kit', 13 UNION ALL
  SELECT 'safety-pet-vet', 14
) ordered
JOIN `quiz_question` q ON q.`question_code` = ordered.code;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `post_collect`;
TRUNCATE TABLE `post_comment`;
TRUNCATE TABLE `post_like`;
TRUNCATE TABLE `post_report`;
TRUNCATE TABLE `community_post`;
TRUNCATE TABLE `badge`;
TRUNCATE TABLE `user_achievement`;
TRUNCATE TABLE `user_backpack`;
TRUNCATE TABLE `user_checkin`;
TRUNCATE TABLE `user_collect`;
TRUNCATE TABLE `user_quiz_record`;
TRUNCATE TABLE `user_quiz_stats`;
TRUNCATE TABLE `user_quize_mistakes`;
TRUNCATE TABLE `quiz_question`;
TRUNCATE TABLE `sys_file_info`;
TRUNCATE TABLE `site_visit`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `quiz_question`
(`title`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `explanation`, `category`, `difficulty`, `status`)
VALUES
('在小铜人馆里，我们可以做什么？', '观察身体区域和文化标记', '模仿针刺动作', '替同学判断病情', '照着页面自行操作', 'A', '小铜人馆用于观察、空间认知和中医文化学习，不提供实际操作指导。', '安全学习', 1, 1),
('身体不舒服时，最安全的做法是什么？', '自己寻找穴位处理', '告诉家长或老师并请医生判断', '模仿视频操作', '忍着不说', 'B', '身体不舒服要及时告诉可信任的大人，并由专业医务人员判断。', '安全学习', 1, 1),
('经络星河在本系统中主要帮助我们认识什么？', '穴位之间的文化路线关系', '治疗步骤', '个人病情', '用针方法', 'A', '经络星河把抽象路线做成可观察的星点连接，帮助理解文化概念。', '文化观察', 1, 1),
('看到穴位名称时，下面哪种说法正确？', '只把它当作身体位置和文化知识来认识', '可以马上在自己身上操作', '能用来替别人看病', '一定能解决身体不适', 'A', '儿童页面只介绍名称、身体区域和文化关系，不作诊断或治疗承诺。', '安全学习', 1, 1),
('在社区分享学习体验时，不应该填写什么？', '喜欢的故事', '完成的任务', '电话、住址或学校名称', '文化问题', 'C', '公开交流要保护个人隐私，不发布联系方式、住址、学校和社交账号。', '隐私保护', 1, 1),
('小铜人模型为什么可以旋转？', '方便从不同方向观察身体区域', '方便练习真实操作', '用于判断疾病', '用于代替医生', 'A', '旋转模型是为了训练空间观察能力，帮助从不同角度认识身体区域。', '空间观察', 1, 1),
('古代铜人模型在文化学习中有什么作用？', '帮助展示身体标记和知识位置', '替儿童进行诊断', '教儿童自行治疗', '保证每个人得到相同效果', 'A', '铜人是医学文化史中的教学载体，本系统只介绍其历史与观察价值。', '文化历史', 2, 1),
('向小铜人文化助手提问时，哪类问题最合适？', '一个文化故事讲了什么', '我得了什么病', '该怎样给自己治疗', '应该使用什么药', 'A', '文化助手回答历史、故事和身体认知问题，不提供诊断、用药或个体化治疗建议。', '安全学习', 1, 1),
('观察头面等敏感区域时应该怎么做？', '只看页面说明，不随便按压或模仿操作', '用尖锐物品寻找位置', '给同学做实验', '越用力越好', 'A', '敏感身体区域只用于观察学习，不触碰、不模仿操作。', '身体认知', 1, 1),
('完成探案任务后获得的铜片和星砂有什么用途？', '用于游戏中的收集和侦探社修补', '用于真实治疗', '用于购买药品', '用于判断健康状况', 'A', '这些材料都是游戏化学习奖励，只用于页面内的收集、解锁和修补。', '游戏规则', 1, 1);

CREATE TABLE IF NOT EXISTS `app_migration` (
  `migration_key` varchar(100) NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`migration_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `app_migration` (`migration_key`)
VALUES ('production_seed_sanitized_v1');

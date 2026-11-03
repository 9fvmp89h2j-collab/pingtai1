CREATE TABLE `safety_game_config_revision` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `version_no` int NOT NULL,
  `base_version` int NOT NULL DEFAULT 0,
  `edit_version` int NOT NULL DEFAULT 0,
  `status` varchar(16) NOT NULL,
  `payload_json` json NOT NULL,
  `created_by` bigint NULL,
  `updated_by` bigint NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `published_at` datetime NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_safety_game_version` (`version_no`),
  KEY `idx_safety_game_status` (`status`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全守护案第二至第四关修订配置';

CREATE TABLE `user_safety_memory_progress` (
  `user_id` bigint NOT NULL,
  `max_unlocked_level` varchar(16) NOT NULL DEFAULT 'BEGINNER',
  `max_completed_level` varchar(16) NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户安全翻牌难度解锁';

INSERT INTO `safety_game_config_revision`
(`version_no`, `base_version`, `edit_version`, `status`, `payload_json`, `published_at`)
VALUES
(1, 0, 0, 'PUBLISHED', '{
  "scenarios": [
    {"id":"scenario-unwell","title":"身体有点不舒服","prompt":"肚肚不舒服了，哪一种行动最安全？","imagePath":"","imageAlt":"孩子捂着肚子向家长求助","visualKey":"guardian-help","correctOptionId":"tell-parent","explanation":"身体不舒服要及时告诉家长，由可信任的大人和医生帮助判断。","enabled":true,"options":[{"id":"tell-parent","text":"马上告诉家长","imagePath":"","imageAlt":"孩子告诉家长身体不舒服","visualKey":"guardian-help"},{"id":"hide-it","text":"忍着不告诉别人","imagePath":"","imageAlt":"孩子独自忍耐不适","visualKey":"guardian-think"},{"id":"self-try","text":"自己找工具试一试","imagePath":"","imageAlt":"孩子准备自行处理","visualKey":"guardian-stop"}]},
    {"id":"scenario-classmate","title":"同学想自己试一试","prompt":"同学准备做危险尝试，你应该怎么守护大家？","imagePath":"","imageAlt":"孩子阻止同学靠近危险物品","visualKey":"guardian-stop","correctOptionId":"tell-teacher","explanation":"先明确制止，再告诉老师；不要参与，也不要站在旁边围观。","enabled":true,"options":[{"id":"watch","text":"站在旁边看","imagePath":"","imageAlt":"孩子在旁边围观","visualKey":"guardian-think"},{"id":"tell-teacher","text":"制止并告诉老师","imagePath":"","imageAlt":"孩子制止同学并告诉老师","visualKey":"guardian-teacher"},{"id":"join","text":"觉得有趣一起试","imagePath":"","imageAlt":"孩子准备参与危险游戏","visualKey":"guardian-stop"}]},
    {"id":"scenario-video","title":"网上的错误教学","prompt":"看到让小朋友模仿治疗的视频，应该怎么做？","imagePath":"","imageAlt":"孩子警觉地看着平板电脑","visualKey":"guardian-video","correctOptionId":"close-video","explanation":"网络内容不能替代专业训练。关闭错误内容，并向家长或老师询问。","enabled":true,"options":[{"id":"copy","text":"照着步骤模仿","imagePath":"","imageAlt":"孩子准备模仿视频动作","visualKey":"guardian-stop"},{"id":"share","text":"分享给同学尝试","imagePath":"","imageAlt":"孩子准备分享错误视频","visualKey":"guardian-think"},{"id":"close-video","text":"关闭并询问大人","imagePath":"","imageAlt":"孩子关闭视频并询问家长","visualKey":"guardian-help"}]},
    {"id":"scenario-playground","title":"操场发现尖锐物","prompt":"地上出现不明尖锐物品，哪一种做法正确？","imagePath":"","imageAlt":"孩子们与地面的尖锐物品保持距离","visualKey":"guardian-safety","correctOptionId":"keep-away","explanation":"不要触碰或捡拾，和它保持距离并通知老师处理。","enabled":true,"options":[{"id":"pick-up","text":"捡起来仔细看","imagePath":"","imageAlt":"孩子准备捡起尖锐物品","visualKey":"guardian-stop"},{"id":"keep-away","text":"远离并告诉老师","imagePath":"","imageAlt":"孩子远离物品并呼喊老师","visualKey":"guardian-teacher"},{"id":"take-home","text":"带回家收藏","imagePath":"","imageAlt":"孩子准备把物品带回家","visualKey":"guardian-think"}]},
    {"id":"scenario-grandma","title":"奶奶头疼了","prompt":"奶奶准备自己处理头痛，你可以怎样帮助她？","imagePath":"","imageAlt":"孩子关心头痛的奶奶","visualKey":"guardian-family","correctOptionId":"seek-doctor","explanation":"温柔劝阻奶奶，陪她寻求专业医疗帮助，不自行尝试治疗。","enabled":true,"options":[{"id":"find-map","text":"帮忙查操作图","imagePath":"","imageAlt":"孩子准备查询操作图片","visualKey":"guardian-video"},{"id":"ignore","text":"假装没有看见","imagePath":"","imageAlt":"孩子转身离开奶奶","visualKey":"guardian-think"},{"id":"seek-doctor","text":"劝阻并陪她就医","imagePath":"","imageAlt":"孩子陪奶奶寻求医生帮助","visualKey":"guardian-doctor"}]},
    {"id":"scenario-pet","title":"小狗不舒服","prompt":"小狗身体不舒服，应该把治疗交给谁？","imagePath":"","imageAlt":"孩子陪在不舒服的小狗身边","visualKey":"guardian-pet","correctOptionId":"visit-vet","explanation":"宠物需要由专业兽医检查和治疗，小朋友不能自行操作。","enabled":true,"options":[{"id":"online-copy","text":"照着网络图片尝试","imagePath":"","imageAlt":"孩子准备照着平板图片模仿","visualKey":"guardian-stop"},{"id":"ask-friend","text":"请同学一起研究","imagePath":"","imageAlt":"孩子邀请同学自行研究","visualKey":"guardian-think"},{"id":"visit-vet","text":"请家长带去宠物医院","imagePath":"","imageAlt":"家长准备带小狗去宠物医院","visualKey":"guardian-doctor"}]}
  ],
  "memory": {
    "centerBadge":{"imagePath":"","imageAlt":"安全守护徽章","visualKey":"safety-badge"},
    "levels":{"BEGINNER":["pair-unwell","pair-sharp"],"INTERMEDIATE":["pair-unwell","pair-sharp","pair-video","pair-classmate"],"ADVANCED":["pair-unwell","pair-sharp","pair-video","pair-classmate","pair-pet","pair-learn","pair-sterile","pair-dispose"]},
    "pairs":[
      {"id":"pair-unwell","name":"身体不舒服","enabled":true,"explanation":"身体不舒服要告诉家长。","left":{"imagePath":"","imageAlt":"身体不舒服的孩子","visualKey":"guardian-think"},"right":{"imagePath":"","imageAlt":"孩子向家长求助","visualKey":"guardian-help"}},
      {"id":"pair-sharp","name":"发现尖锐物品","enabled":true,"explanation":"保持距离，请大人安全处理。","left":{"imagePath":"","imageAlt":"远处的不明尖锐物品","visualKey":"guardian-stop"},"right":{"imagePath":"","imageAlt":"孩子请老师处理","visualKey":"guardian-teacher"}},
      {"id":"pair-video","name":"网络错误教学","enabled":true,"explanation":"关闭错误内容并询问大人。","left":{"imagePath":"","imageAlt":"平板上的错误教学","visualKey":"guardian-video"},"right":{"imagePath":"","imageAlt":"孩子关闭平板并求助","visualKey":"guardian-help"}},
      {"id":"pair-classmate","name":"同学危险尝试","enabled":true,"explanation":"拒绝参与并及时告诉老师。","left":{"imagePath":"","imageAlt":"同学提出危险尝试","visualKey":"guardian-stop"},"right":{"imagePath":"","imageAlt":"孩子制止并告诉老师","visualKey":"guardian-teacher"}},
      {"id":"pair-pet","name":"宠物不舒服","enabled":true,"explanation":"宠物不舒服要去宠物医院。","left":{"imagePath":"","imageAlt":"身体不舒服的小狗","visualKey":"guardian-pet"},"right":{"imagePath":"","imageAlt":"家长带小狗寻找兽医","visualKey":"guardian-doctor"}},
      {"id":"pair-learn","name":"学习穴位文化","enabled":true,"explanation":"只观察图册和模型，不模仿治疗。","left":{"imagePath":"","imageAlt":"孩子观察身体知识图册","visualKey":"guardian-learn"},"right":{"imagePath":"","imageAlt":"小铜人举盾提醒只学习","visualKey":"guardian-safety"}},
      {"id":"pair-sterile","name":"正规医疗用品","enabled":true,"explanation":"正规医疗用品保持密封并由专业人员使用。","left":{"imagePath":"","imageAlt":"保持密封的医疗用品包装","visualKey":"safety-badge"},"right":{"imagePath":"","imageAlt":"专业医务人员检查用品","visualKey":"guardian-doctor"}},
      {"id":"pair-dispose","name":"使用后安全回收","enabled":true,"explanation":"一次性用品使用后要放入专业回收容器。","left":{"imagePath":"","imageAlt":"独立使用的一次性用品","visualKey":"safety-bell"},"right":{"imagePath":"","imageAlt":"关闭式专业回收容器","visualKey":"guardian-safety"}}
    ]
  },
  "pledge": {
    "lines":[
      {"id":"pledge-no-self","title":"不自行尝试","text":"我承诺，不自己拿尖锐物品尝试针刺，也不让同学这样做。","enabled":true},
      {"id":"pledge-no-copy","title":"不模仿治疗","text":"我承诺，只学习身体知识，不模仿网络或视频里的治疗动作。","enabled":true},
      {"id":"pledge-doctor","title":"相信专业帮助","text":"我知道，学习系统不能替代医生，身体不舒服要寻求专业帮助。","enabled":true},
      {"id":"pledge-help","title":"及时告诉大人","text":"我承诺，遇到不舒服或危险情况，会及时告诉家长或老师。","enabled":true},
      {"id":"pledge-tools","title":"不碰危险物品","text":"我承诺，不捡、不碰、不玩来历不明的针具或尖锐物品。","enabled":true},
      {"id":"pledge-friends","title":"守护身边伙伴","text":"我愿意提醒身边的小朋友，一起做懂安全、会求助的小卫士。","enabled":true}
    ],
    "certificate":{"title":"杏林安全小卫士证书","honor":"杏林安全小卫士","issuer":"杏林奇境安全课堂","blessing":"愿你始终记住：只观察、只学习，遇到问题及时向可信任的大人求助。","backgroundPath":"","badgePath":"","sealPath":""}
  }
}', CURRENT_TIMESTAMP);

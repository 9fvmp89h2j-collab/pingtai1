CREATE TABLE IF NOT EXISTS acupoint_knowledge (
  code VARCHAR(16) NOT NULL,
  point_number INT NOT NULL,
  name VARCHAR(64) NOT NULL,
  pinyin VARCHAR(128) NULL,
  meridian_code VARCHAR(8) NOT NULL,
  meridian_name VARCHAR(64) NOT NULL,
  meridian_english VARCHAR(128) NULL,
  model_id VARCHAR(32) NULL,
  body_area VARCHAR(64) NOT NULL,
  standard_location TEXT NULL,
  child_location VARCHAR(500) NOT NULL,
  child_description VARCHAR(500) NOT NULL,
  safety_tip VARCHAR(255) NOT NULL,
  source_name VARCHAR(255) NULL,
  source_link VARCHAR(512) NULL,
  position_x DECIMAL(12,6) NULL,
  position_y DECIMAL(12,6) NULL,
  position_z DECIMAL(12,6) NULL,
  sort_order INT NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code),
  UNIQUE KEY uk_acupoint_model_id (model_id),
  KEY idx_acupoint_meridian_sort (meridian_code, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='3D小铜人儿童穴位知识库';

CREATE TABLE IF NOT EXISTS copper_acupoint_revision (
  id BIGINT NOT NULL AUTO_INCREMENT,
  acupoint_code VARCHAR(16) NOT NULL,
  version_no INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  payload_json JSON NOT NULL,
  created_by BIGINT NULL,
  reviewed_by BIGINT NULL,
  published_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  reviewed_at DATETIME NULL,
  scheduled_at DATETIME NULL,
  published_at DATETIME NULL,
  publish_error VARCHAR(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_copper_acupoint_version (acupoint_code, version_no),
  KEY idx_copper_acupoint_status (status, scheduled_at),
  KEY idx_copper_acupoint_code (acupoint_code, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小铜人穴位内容修订';

CREATE TABLE IF NOT EXISTS copper_story_revision (
  id BIGINT NOT NULL AUTO_INCREMENT,
  story_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  title VARCHAR(128) NOT NULL,
  summary VARCHAR(500) NOT NULL,
  cover_path VARCHAR(512) NULL,
  payload_json JSON NOT NULL,
  created_by BIGINT NULL,
  reviewed_by BIGINT NULL,
  published_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  reviewed_at DATETIME NULL,
  scheduled_at DATETIME NULL,
  published_at DATETIME NULL,
  publish_error VARCHAR(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_copper_story_version (story_code, version_no),
  KEY idx_copper_story_status (status, scheduled_at),
  KEY idx_copper_story_code (story_code, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小铜人探案故事修订';

CREATE TABLE IF NOT EXISTS copper_content_release (
  id BIGINT NOT NULL AUTO_INCREMENT,
  content_type VARCHAR(16) NOT NULL,
  content_key VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  title VARCHAR(128) NOT NULL,
  summary VARCHAR(500) NOT NULL,
  route VARCHAR(255) NOT NULL,
  published_by BIGINT NULL,
  published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  retracted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_copper_release (content_type, content_key, version_no),
  KEY idx_copper_release_time (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小铜人内容发布与站内推送';

CREATE TABLE IF NOT EXISTS user_content_release_read (
  user_id BIGINT NOT NULL,
  release_id BIGINT NOT NULL,
  read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, release_id),
  KEY idx_release_read (release_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户内容推送已读记录';

CREATE TABLE IF NOT EXISTS user_copper_story_progress (
  user_id BIGINT NOT NULL,
  story_code VARCHAR(64) NOT NULL,
  progress_json JSON NOT NULL,
  completed TINYINT(1) NOT NULL DEFAULT 0,
  completed_at DATETIME NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, story_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户小铜人故事进度';

INSERT INTO copper_acupoint_revision
  (acupoint_code, version_no, status, payload_json, published_at)
SELECT a.code, 1, 'PUBLISHED', JSON_OBJECT(
  'code', a.code, 'pointNumber', a.point_number, 'name', a.name, 'pinyin', a.pinyin,
  'meridianCode', a.meridian_code, 'meridianName', a.meridian_name,
  'meridianEnglish', a.meridian_english, 'modelId', a.model_id, 'bodyArea', a.body_area,
  'standardLocation', a.standard_location, 'childLocation', a.child_location,
  'childDescription', a.child_description, 'safetyTip', a.safety_tip,
  'sourceName', a.source_name, 'sourceLink', a.source_link,
  'positionX', a.position_x, 'positionY', a.position_y, 'positionZ', a.position_z,
  'sortOrder', a.sort_order, 'enabled', a.enabled
), CURRENT_TIMESTAMP
FROM acupoint_knowledge a
WHERE NOT EXISTS (
  SELECT 1 FROM copper_acupoint_revision r
  WHERE r.acupoint_code = a.code AND r.version_no = 1
);

INSERT INTO copper_story_revision
  (story_code, version_no, status, title, summary, cover_path, payload_json, published_at)
VALUES
('missing-bamboo', 1, 'PUBLISHED', '失踪竹简案', '风把竹简藏到了书架后，请跟着竹叶和铜片找到它。', '/doctor-story-preview/1.jpg',
 JSON_OBJECT(
  'storyCode','missing-bamboo','title','失踪竹简案','subtitle','一卷竹简不见了，小侦探从窗边开始寻找。','estimatedMinutes',5,
  'coverPath','/doctor-story-preview/1.jpg','linkedAcupoints',JSON_ARRAY(),
  'pages',JSON_ARRAY(
    JSON_OBJECT('id','scene','title','竹简架空了一格','image','/doctor-story-preview/1.jpg','text','清晨，小铜人老师发现一卷竹简不见了。桌边有一枚铜片，窗边有一片竹叶。','tip','先看现场，再猜答案。'),
    JSON_OBJECT('id','clues','title','小风留下了线索','image','/doctor-story-preview/2.jpg','text','竹叶轻轻摇，铜片滚到桌角。原来，窗口刚才吹进一阵风。','tip','把竹叶、铜片和空架连起来。'),
    JSON_OBJECT('id','answer','title','竹简在书架后','image','/doctor-story-preview/3.jpg','text','小侦探在书架后找到了竹简。它是被风吹落的，没有人把它拿走。','tip','好推理要有线索帮忙。')
  ),
  'clues',JSON_ARRAY(
    JSON_OBJECT('id','empty-shelf','name','空竹简架','description','这里原来放着竹简。'),
    JSON_OBJECT('id','bamboo-leaf','name','窗边竹叶','description','竹叶告诉我们：刚才有风。'),
    JSON_OBJECT('id','copper-piece','name','圆圆铜片','description','铜片被风推到了桌角。')
  ),
  'reasoning',JSON_OBJECT('prompt','竹简去了哪里？','options',JSON_ARRAY('被风吹到书架后','自己跑出了门'),'answer',0,'explanation','竹叶和铜片都指向书架后。'),
  'safety',JSON_OBJECT('prompt','看到穴位知识时怎么做？','options',JSON_ARRAY('只观察，请老师讲解','在同学身上试一试'),'answer',0,'message','我们只学文化知识，不做身体操作。'),
  'rewards',JSON_ARRAY(JSON_OBJECT('code','bamboo-slip-shard','name','竹简碎片','amount',3))
 ), CURRENT_TIMESTAMP),
('exam-copper-man', 1, 'PUBLISHED', '会考试的小铜人', '跟着王惟一走进北宋课堂，看看铜人怎样帮大家学习。', '/doctor-story-preview/5.jpg',
 JSON_OBJECT(
  'storyCode','exam-copper-man','title','会考试的小铜人','subtitle','铜人不会说话，却能帮古人学习身体地图。','estimatedMinutes',5,'coverPath','/doctor-story-preview/5.jpg','linkedAcupoints',JSON_ARRAY('LU-1','LU-5','LU-9'),
  'pages',JSON_ARRAY(
    JSON_OBJECT('id','workshop','title','王惟一的铜人课堂','image','/doctor-story-preview/5.jpg','text','北宋时，王惟一与工匠制作了铜人模型。模型上有许多小标记。','tip','铜人是学习模型，不是玩具。'),
    JSON_OBJECT('id','lesson','title','星点变成学习题','image','/doctor-story-preview/5.jpg','text','学生先记名字，再在铜人上找标记。找对了，就完成一道古代学习题。','tip','我们也用3D铜人只做观察。'),
    JSON_OBJECT('id','legacy','title','老铜人来到新课堂','image','/doctor-story-preview/5.jpg','text','今天，铜人变成了屏幕里的3D文化地图。我们可以转一转、看一看。','tip','文化知识要和安全规则一起学。')
  ),
  'clues',JSON_ARRAY(JSON_OBJECT('id','marks','name','铜人标记','description','小标记帮学生认识名字和路线。'),JSON_OBJECT('id','book','name','课堂记录','description','记录里写着北宋的铜人故事。')),
  'reasoning',JSON_OBJECT('prompt','古人为什么制作铜人？','options',JSON_ARRAY('帮助观察和学习','让铜人自己看病'),'answer',0,'explanation','铜人是认识身体地图的教学模型。'),
  'safety',JSON_OBJECT('prompt','我们怎样使用3D铜人？','options',JSON_ARRAY('转动模型观察','拿工具模仿'),'answer',0,'message','只观察、只学习。'),
  'rewards',JSON_ARRAY(JSON_OBJECT('code','copper-token','name','铜片','amount',2))
 ), CURRENT_TIMESTAMP),
('broken-star-river', 1, 'PUBLISHED', '星河断线案', '三颗文化星迷了路，请把它们送回同一条经络星河。', '/doctor-story-preview/3.jpg',
 JSON_OBJECT(
  'storyCode','broken-star-river','title','星河断线案','subtitle','线索不是孤零零的点，它们会连成路线。','estimatedMinutes',4,'coverPath','/doctor-story-preview/3.jpg','linkedAcupoints',JSON_ARRAY('LU-1','LU-5','LU-9'),
  'pages',JSON_ARRAY(
    JSON_OBJECT('id','dark','title','星河忽然暗了','image','/doctor-story-preview/3.jpg','text','中府星、尺泽星和太渊星都亮着，它们之间的光线却不见了。','tip','看看三颗星的经络名称。'),
    JSON_OBJECT('id','route','title','三颗星找到同一条路','image','/doctor-story-preview/3.jpg','text','它们都属于手太阴肺经。名字像路牌，帮它们排到同一条路线上。','tip','经络可以当成文化地图的路线名。'),
    JSON_OBJECT('id','light','title','星河又亮了','image','/doctor-story-preview/3.jpg','text','小侦探按照路线名称连好星点。经络星河又变得明亮了。','tip','今天记住的是名字和路线。')
  ),
  'clues',JSON_ARRAY(JSON_OBJECT('id','same-name','name','相同路线名','description','三颗星的档案都写着手太阴肺经。'),JSON_OBJECT('id','order','name','星点编号','description','编号帮我们整理文化地图。')),
  'reasoning',JSON_OBJECT('prompt','哪个线索能把星点连起来？','options',JSON_ARRAY('相同的经络名称','卡片的颜色'),'answer',0,'explanation','经络名称是这张文化地图的路线牌。'),
  'safety',JSON_OBJECT('prompt','学习路线时要记住什么？','options',JSON_ARRAY('只看图和模型','在身上试操作'),'answer',0,'message','我们只认识文化路线。'),
  'rewards',JSON_ARRAY(JSON_OBJECT('code','meridian-star-sand','name','经络星砂','amount',3))
 ), CURRENT_TIMESTAMP),
('silent-safety-bell', 1, 'PUBLISHED', '安全铃铛失声案', '安全铃铛不响了，请找回“只观察”的金色声音。', '/doctor-story-preview/4.jpg',
 JSON_OBJECT(
  'storyCode','silent-safety-bell','title','安全铃铛失声案','subtitle','一句错误的模仿口令，让安全铃铛失去了声音。','estimatedMinutes',4,'coverPath','/doctor-story-preview/4.jpg','linkedAcupoints',JSON_ARRAY(),
  'pages',JSON_ARRAY(
    JSON_OBJECT('id','quiet','title','铃铛怎么不响了','image','/doctor-story-preview/4.jpg','text','小药师听到一句“跟着视频试一试”。安全铃铛立刻安静了。','tip','这句话安全吗？'),
    JSON_OBJECT('id','rules','title','三张安全卡','image','/doctor-story-preview/4.jpg','text','第一张写“只观察”，第二张写“问成人”，第三张写“不模仿”。','tip','三张卡都是安全线索。'),
    JSON_OBJECT('id','ring','title','金色声音回来了','image','/doctor-story-preview/4.jpg','text','小侦探大声说：“只观察、不模仿，有问题找老师和家长！”铃铛又响了。','tip','记住这句安全口令。')
  ),
  'clues',JSON_ARRAY(JSON_OBJECT('id','observe','name','只观察卡','description','看3D模型和文化图卡是安全的。'),JSON_OBJECT('id','adult','name','问成人卡','description','不舒服或有疑问时，找家长、老师或医生。'),JSON_OBJECT('id','no-copy','name','不模仿卡','description','不照着图片或视频做身体操作。')),
  'reasoning',JSON_OBJECT('prompt','哪句话能让安全铃铛响起来？','options',JSON_ARRAY('只观察，有问题问成人','跟着视频自己试'),'answer',0,'explanation','学文化知识时，安全规则永远排在第一位。'),
  'safety',JSON_OBJECT('prompt','身体不舒服时应该怎么做？','options',JSON_ARRAY('告诉家长并询问医生','自己找方法试'),'answer',0,'message','不舒服要告诉可信任的成人。'),
  'rewards',JSON_ARRAY(JSON_OBJECT('code','safety-bell','name','安全铃铛','amount',1))
 ), CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE story_code = VALUES(story_code);

INSERT INTO copper_content_release
  (content_type, content_key, version_no, title, summary, route, published_at)
SELECT 'STORY', s.story_code, s.version_no, s.title, s.summary,
       CONCAT('/doctor-story?story=', s.story_code), COALESCE(s.published_at, CURRENT_TIMESTAMP)
FROM copper_story_revision s
WHERE s.status = 'PUBLISHED'
ON DUPLICATE KEY UPDATE title = VALUES(title), summary = VALUES(summary), route = VALUES(route), retracted_at = NULL;

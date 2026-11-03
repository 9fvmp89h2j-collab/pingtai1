CREATE TABLE IF NOT EXISTS game_config_revision (
  id BIGINT NOT NULL AUTO_INCREMENT,
  game_code VARCHAR(64) NOT NULL,
  version_no INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  base_version INT NULL,
  edit_version INT NOT NULL DEFAULT 0,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  published_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  published_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_game_config_version (game_code, version_no),
  KEY idx_game_config_status (game_code, status, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏配置版本';

CREATE TABLE IF NOT EXISTS game_level_revision (
  revision_id BIGINT NOT NULL,
  level_id VARCHAR(64) NOT NULL,
  order_no INT NOT NULL,
  label VARCHAR(128) NOT NULL,
  status_text VARCHAR(128) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  icon VARCHAR(32) NOT NULL,
  seal VARCHAR(32) NOT NULL,
  position_x DECIMAL(8,3) NOT NULL,
  position_y DECIMAL(8,3) NOT NULL,
  route VARCHAR(128) NOT NULL,
  map_key VARCHAR(64) NOT NULL,
  cover_path VARCHAR(512) NULL,
  public_flag TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (revision_id, level_id),
  UNIQUE KEY uk_game_level_order (revision_id, order_no),
  KEY idx_game_level_revision (revision_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏主线关卡配置';

CREATE TABLE IF NOT EXISTS game_task_revision (
  revision_id BIGINT NOT NULL,
  task_code VARCHAR(128) NOT NULL,
  level_id VARCHAR(64) NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  task_type VARCHAR(64) NOT NULL,
  route VARCHAR(128) NOT NULL,
  target INT NOT NULL DEFAULT 1,
  editable_flag TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (revision_id, task_code),
  KEY idx_game_task_level (revision_id, level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏任务配置';

CREATE TABLE IF NOT EXISTS game_task_reward_revision (
  revision_id BIGINT NOT NULL,
  task_code VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  reward_type VARCHAR(32) NOT NULL,
  item_code VARCHAR(128) NULL,
  amount INT NOT NULL DEFAULT 0,
  score_delta INT NOT NULL DEFAULT 0,
  PRIMARY KEY (revision_id, task_code, sort_order),
  KEY idx_game_task_reward (revision_id, task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏任务奖励配置';

CREATE TABLE IF NOT EXISTS game_level_prerequisite_revision (
  revision_id BIGINT NOT NULL,
  level_id VARCHAR(64) NOT NULL,
  prerequisite_level_id VARCHAR(64) NOT NULL,
  PRIMARY KEY (revision_id, level_id, prerequisite_level_id),
  KEY idx_game_level_prerequisite (revision_id, prerequisite_level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏关卡前置关系';

ALTER TABLE user_task_progress
  ADD COLUMN config_version INT NOT NULL DEFAULT 1;

INSERT INTO game_config_revision
  (id, game_code, version_no, status, base_version, created_at, updated_at, published_at)
VALUES
  (1, 'xinglin-mainline', 1, 'PUBLISHED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO game_level_revision
  (revision_id, level_id, order_no, label, status_text, description, icon, seal, position_x, position_y, route, map_key, public_flag)
VALUES
  (1, 'checkin', 1, '报到处', '入社报到', '完成侦探社报到，领取第一枚观察徽章。', '1', '今', 13.600, 9.500, '/checkin', 'checkin', 1),
  (1, 'safe-start', 2, '安全守护案', '安全课堂', '先学会只观察、不针刺的安全规则，再开始后续调查。', '2', '锁', 33.200, 11.800, '/safety', 'safe-start', 1),
  (1, 'bamboo', 3, '失踪竹简案', '故事馆任务', '阅读故事、搜集证据、完成推理和安全判断。', '3', '锁', 39.000, 35.000, '/doctor-story', 'bamboo', 1),
  (1, 'body', 4, '身体地图追踪案', '身体地图', '完成区域学习和穴位归位，只做观察与文化知识学习。', '4', '锁', 8.800, 40.000, '/body-map', 'body', 1),
  (1, 'meridian', 5, '经络星河密令', '经络学习', '完成 14 条经络路线并领取星河聚合奖励。', '5', '锁', 29.000, 60.000, '/jingluo', 'meridian', 1),
  (1, 'archive', 6, '铜人档案室', '小铜人馆', '完成首次铜人观察案件，后续案件作为每日重复内容。', '6', '锁', 57.500, 47.000, '/copper-man', 'archive', 1),
  (1, 'secret-room', 7, '星光修补册', '复习修补', '完成首次知识星点修补，后续错题作为复习任务。', '7', '锁', 59.500, 70.000, '/review', 'secret-room', 1),
  (1, 'agency', 8, '侦探社修复计划', '侦探社修复', '修复第一项门牌即完成主线，其余项目用于持续收集。', '8', '锁', 14.800, 74.000, '/agency', 'agency', 1);

INSERT INTO game_task_revision
  (revision_id, task_code, level_id, name, description, task_type, route, target, editable_flag)
VALUES
  (1, 'map.checkin', 'checkin', '完成侦探社报到', '完成侦探社报到并领取观察徽章。', 'checkin', '/checkin', 1, 0),
  (1, 'main-safety-case', 'safe-start', '完成安全守护案', '完成安全学习与安全宣誓，记住只观察、只学习、不自行针刺。', 'safety', '/safety', 1, 1),
  (1, 'main-mist-in-xinglin', 'bamboo', '杏林谷起雾', '完成故事馆任务，找回第一束杏林谷星光。', 'story', '/doctor-story', 1, 1),
  (1, 'main-hand-star-map', 'body', '点亮身体地图', '认识身体区域，找到穴位星点，并完成一次安全问答。', 'acupoint', '/body-map', 10, 1),
  (1, 'meridian-river-completion', 'meridian', '完成经络星河', '完成经络路线并领取星河聚合奖励。', 'meridian', '/jingluo', 14, 1),
  (1, 'copper-man-daily-case', 'archive', '完成铜人观察案', '完成首次铜人观察案件。', 'copper-man', '/copper-man', 1, 1),
  (1, 'review-daily', 'secret-room', '完成星光修补', '完成首次知识星点修补。', 'review', '/review', 1, 1),
  (1, 'main-repair-agency', 'agency', '整理侦探社线索墙', '收集线索并修复侦探社档案墙。', 'agency', '/agency', 1, 1),
  (1, 'daily-read-copper-story', NULL, '阅读一个针灸小故事', '去故事馆完成《失踪竹简案》，回答故事后的安全小问题。', 'story', '/doctor-story', 1, 0),
  (1, 'daily-light-hand-stars', NULL, '点亮 3 个穴位星点', '跟着小铜人老师认识身体地图，找到 3 颗穴位星点。', 'acupoint', '/body-map', 3, 0),
  (1, 'daily-safety-quiz', NULL, '完成 3 道安全判断题', '听安全铃铛提醒，判断哪些行为可以做。', 'safety', '/safety', 3, 0),
  (1, 'shunting-daily', NULL, '完成经络小火车', '完成一次经络小火车观察挑战。', 'shunting', '/shunting-game', 1, 0);

INSERT INTO game_task_reward_revision
  (revision_id, task_code, sort_order, reward_type, item_code, amount, score_delta)
VALUES
  (1, 'daily-read-copper-story', 0, 'MATERIAL', 'bamboo-slip-shard', 3, 0),
  (1, 'daily-read-copper-story', 1, 'MATERIAL', 'apricot-kernel', 2, 0),
  (1, 'daily-light-hand-stars', 0, 'MATERIAL', 'acupoint-star-pearl', 3, 0),
  (1, 'daily-light-hand-stars', 1, 'MATERIAL', 'copper-token', 1, 0),
  (1, 'daily-safety-quiz', 0, 'MATERIAL', 'safety-bell', 1, 0),
  (1, 'daily-safety-quiz', 1, 'MATERIAL', 'mugwort-floss', 1, 0),
  (1, 'main-mist-in-xinglin', 0, 'MATERIAL', 'bamboo-slip-shard', 3, 0),
  (1, 'main-hand-star-map', 0, 'MATERIAL', 'acupoint-star-pearl', 2, 0),
  (1, 'main-hand-star-map', 1, 'MATERIAL', 'copper-token', 1, 0),
  (1, 'main-hand-star-map', 2, 'MATERIAL', 'safety-bell', 1, 0),
  (1, 'meridian-river-completion', 0, 'MATERIAL', 'meridian-star-sand', 5, 0),
  (1, 'meridian-river-completion', 1, 'MATERIAL', 'copper-token', 2, 0),
  (1, 'copper-man-daily-case', 0, 'MATERIAL', 'copper-token', 2, 0),
  (1, 'copper-man-daily-case', 1, 'MATERIAL', 'meridian-star-sand', 5, 0),
  (1, 'review-daily', 0, 'MATERIAL', 'star-compass', 1, 0);

INSERT INTO game_level_prerequisite_revision (revision_id, level_id, prerequisite_level_id)
VALUES
  (1, 'safe-start', 'checkin'),
  (1, 'bamboo', 'safe-start'),
  (1, 'body', 'bamboo'),
  (1, 'meridian', 'body'),
  (1, 'archive', 'meridian'),
  (1, 'secret-room', 'archive'),
  (1, 'agency', 'secret-room');

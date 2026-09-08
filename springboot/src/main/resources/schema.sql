CREATE TABLE IF NOT EXISTS `acupoint_knowledge` (
  `code` varchar(16) NOT NULL COMMENT '标准穴位编号，例如 LU-1',
  `point_number` int NOT NULL,
  `name` varchar(64) NOT NULL,
  `pinyin` varchar(128) DEFAULT NULL,
  `meridian_code` varchar(8) NOT NULL,
  `meridian_name` varchar(64) NOT NULL,
  `meridian_english` varchar(128) DEFAULT NULL,
  `model_id` varchar(32) DEFAULT NULL,
  `body_area` varchar(64) NOT NULL,
  `standard_location` text,
  `child_location` varchar(500) NOT NULL,
  `child_description` varchar(500) NOT NULL,
  `child_traditional_use` varchar(500) DEFAULT NULL,
  `safety_tip` varchar(255) NOT NULL,
  `source_name` varchar(255) DEFAULT NULL,
  `source_link` varchar(512) DEFAULT NULL,
  `traditional_use_source_name` varchar(255) DEFAULT NULL,
  `traditional_use_source_link` varchar(512) DEFAULT NULL,
  `position_x` decimal(12,6) DEFAULT NULL,
  `position_y` decimal(12,6) DEFAULT NULL,
  `position_z` decimal(12,6) DEFAULT NULL,
  `sort_order` int NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`),
  UNIQUE KEY `uk_acupoint_model_id` (`model_id`),
  KEY `idx_acupoint_meridian_sort` (`meridian_code`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='3D小铜人儿童穴位知识库';

CREATE TABLE IF NOT EXISTS `body_map_acupoint_knowledge` (
  `acupoint_code` varchar(16) NOT NULL,
  `region_code` varchar(16) NOT NULL,
  `body_view` varchar(8) NOT NULL,
  `placement_hint` varchar(500) NOT NULL,
  `child_map_description` varchar(500) NOT NULL,
  `sort_order` int NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`acupoint_code`),
  KEY `idx_body_map_region_sort` (`region_code`, `sort_order`),
  CONSTRAINT `fk_body_map_acupoint`
    FOREIGN KEY (`acupoint_code`) REFERENCES `acupoint_knowledge` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身体地图儿童穴位归位知识库';

CREATE TABLE IF NOT EXISTS `user_acupoint_daily_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `case_date` date NOT NULL,
  `acupoint_code` varchar(16) NOT NULL,
  `discovered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_case_acupoint` (`user_id`, `case_date`, `acupoint_code`),
  KEY `idx_user_case_date` (`user_id`, `case_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户每日铜人探案进度';

CREATE TABLE IF NOT EXISTS `user_copper_man_profile` (
  `user_id` bigint NOT NULL,
  `copper_tokens` int NOT NULL DEFAULT 0,
  `star_sand` int NOT NULL DEFAULT 0,
  `completed_cases` int NOT NULL DEFAULT 0,
  `last_completed_case_date` date DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户小铜人馆材料与成长档案';

CREATE TABLE IF NOT EXISTS `user_agency_archive` (
  `user_id` bigint NOT NULL,
  `archive_item_id` varchar(64) NOT NULL,
  `repaired_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `archive_item_id`),
  KEY `idx_agency_archive_repaired` (`user_id`, `repaired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户侦探社修复档案';

CREATE TABLE IF NOT EXISTS `user_game_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `game_code` varchar(64) NOT NULL,
  `reward_date` date NOT NULL,
  `score_awarded` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game_reward_day` (`user_id`, `game_code`, `reward_date`),
  KEY `idx_game_reward_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户小游戏每日奖励领取记录';

CREATE TABLE IF NOT EXISTS `copper_acupoint_revision` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `acupoint_code` varchar(16) NOT NULL,
  `version_no` int NOT NULL,
  `status` varchar(16) NOT NULL,
  `payload_json` json NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `published_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reviewed_at` datetime DEFAULT NULL,
  `scheduled_at` datetime DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `publish_error` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_copper_acupoint_version` (`acupoint_code`, `version_no`),
  KEY `idx_copper_acupoint_status` (`status`, `scheduled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小铜人穴位内容修订';

CREATE TABLE IF NOT EXISTS `copper_story_revision` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `story_code` varchar(64) NOT NULL,
  `version_no` int NOT NULL,
  `status` varchar(16) NOT NULL,
  `title` varchar(128) NOT NULL,
  `summary` varchar(500) NOT NULL,
  `cover_path` varchar(512) DEFAULT NULL,
  `payload_json` json NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `published_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reviewed_at` datetime DEFAULT NULL,
  `scheduled_at` datetime DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `publish_error` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_copper_story_version` (`story_code`, `version_no`),
  KEY `idx_copper_story_status` (`status`, `scheduled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小铜人探案故事修订';

CREATE TABLE IF NOT EXISTS `copper_content_release` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_type` varchar(16) NOT NULL,
  `content_key` varchar(64) NOT NULL,
  `version_no` int NOT NULL,
  `title` varchar(128) NOT NULL,
  `summary` varchar(500) NOT NULL,
  `route` varchar(255) NOT NULL,
  `published_by` bigint DEFAULT NULL,
  `published_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `retracted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_copper_release` (`content_type`, `content_key`, `version_no`),
  KEY `idx_copper_release_time` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小铜人内容发布与站内推送';

CREATE TABLE IF NOT EXISTS `user_content_release_read` (
  `user_id` bigint NOT NULL,
  `release_id` bigint NOT NULL,
  `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `release_id`),
  KEY `idx_release_read` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户内容推送已读记录';

CREATE TABLE IF NOT EXISTS `user_copper_story_progress` (
  `user_id` bigint NOT NULL,
  `story_code` varchar(64) NOT NULL,
  `progress_json` json NOT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT 0,
  `completed_at` datetime DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `story_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户小铜人故事进度';

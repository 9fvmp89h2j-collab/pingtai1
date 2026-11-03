CREATE TABLE IF NOT EXISTS `user_agency_archive` (
  `user_id` bigint NOT NULL,
  `archive_item_id` varchar(64) NOT NULL,
  `repaired_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `archive_item_id`),
  KEY `idx_agency_archive_repaired` (`user_id`, `repaired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户侦探社修复档案';

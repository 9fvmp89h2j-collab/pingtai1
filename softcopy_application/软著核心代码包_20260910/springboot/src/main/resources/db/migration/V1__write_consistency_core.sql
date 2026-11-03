CREATE TABLE IF NOT EXISTS write_operation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  operation_key VARCHAR(128) NOT NULL,
  request_hash CHAR(64) NOT NULL,
  operation_type VARCHAR(96) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PROCESSING',
  response_json LONGTEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_write_operation_user_key (user_id, operation_key),
  KEY idx_write_operation_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='幂等写入操作记录';

CREATE TABLE IF NOT EXISTS audit_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  actor_user_id BIGINT NULL,
  request_id VARCHAR(64) NULL,
  action VARCHAR(96) NOT NULL,
  resource_type VARCHAR(96) NOT NULL,
  resource_id VARCHAR(96) NULL,
  outcome VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',
  details_json LONGTEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_actor_created (actor_user_id, created_at),
  KEY idx_audit_resource (resource_type, resource_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务写入审计事件';

CREATE TABLE IF NOT EXISTS auth_session (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_id VARCHAR(64) NOT NULL,
  issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME NOT NULL,
  last_seen_at DATETIME NULL,
  revoked_at DATETIME NULL,
  revoke_reason VARCHAR(128) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_session_token (token_id),
  KEY idx_auth_session_user (user_id, revoked_at, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可撤销登录会话';

CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_key VARCHAR(128) NOT NULL,
  event_type VARCHAR(96) NOT NULL,
  aggregate_type VARCHAR(96) NOT NULL,
  aggregate_id VARCHAR(96) NOT NULL,
  payload_json LONGTEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  available_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  processed_at DATETIME NULL,
  last_error VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_outbox_event_key (event_key),
  KEY idx_outbox_status_available (status, available_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事务外发事件';

CREATE TABLE IF NOT EXISTS user_task_progress (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  game_code VARCHAR(64) NOT NULL,
  task_code VARCHAR(128) NOT NULL,
  period_key VARCHAR(32) NOT NULL DEFAULT 'lifetime',
  progress INT NOT NULL DEFAULT 0,
  target INT NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'IN_PROGRESS',
  source VARCHAR(32) NOT NULL DEFAULT 'SERVER',
  completed_at DATETIME NULL,
  claimed_at DATETIME NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_task_period (user_id, game_code, task_code, period_key),
  KEY idx_user_task_status (user_id, status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户游戏任务进度';

CREATE TABLE IF NOT EXISTS reward_ledger (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  grant_key VARCHAR(160) NOT NULL,
  source VARCHAR(64) NOT NULL,
  task_code VARCHAR(128) NULL,
  reward_type VARCHAR(32) NOT NULL,
  item_code VARCHAR(128) NULL,
  amount INT NOT NULL DEFAULT 0,
  score_delta INT NOT NULL DEFAULT 0,
  idempotency_key VARCHAR(128) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reward_grant (user_id, grant_key),
  KEY idx_reward_user_created (user_id, created_at),
  KEY idx_reward_idempotency (user_id, idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分与材料奖励流水';

CREATE TABLE IF NOT EXISTS user_material_balance (
  user_id BIGINT NOT NULL,
  material_code VARCHAR(128) NOT NULL,
  amount INT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, material_code),
  KEY idx_material_balance_user (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户材料余额';

CREATE TABLE IF NOT EXISTS user_game_migration (
  user_id BIGINT NOT NULL,
  source_key VARCHAR(64) NOT NULL,
  source_version INT NOT NULL,
  state_hash CHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'COMPLETED',
  imported_at DATETIME NULL,
  last_error VARCHAR(1000) NULL,
  PRIMARY KEY (user_id, source_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旧客户端状态迁移记录';

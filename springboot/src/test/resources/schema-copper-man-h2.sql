DROP TABLE IF EXISTS user_game_migration;
DROP TABLE IF EXISTS user_safety_game_draft;
DROP TABLE IF EXISTS game_level_prerequisite_revision;
DROP TABLE IF EXISTS game_task_reward_revision;
DROP TABLE IF EXISTS game_task_revision;
DROP TABLE IF EXISTS game_level_revision;
DROP TABLE IF EXISTS game_config_revision;
DROP TABLE IF EXISTS user_material_balance;
DROP TABLE IF EXISTS reward_ledger;
DROP TABLE IF EXISTS user_task_progress;
DROP TABLE IF EXISTS outbox_event;
DROP TABLE IF EXISTS auth_session;
DROP TABLE IF EXISTS audit_event;
DROP TABLE IF EXISTS write_operation;
DROP TABLE IF EXISTS user_checkin;
DROP TABLE IF EXISTS user_game_reward;
DROP TABLE IF EXISTS user_acupoint_daily_progress;
DROP TABLE IF EXISTS user_copper_man_profile;
DROP TABLE IF EXISTS user_agency_archive;
DROP TABLE IF EXISTS acupoint_knowledge;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  id bigint PRIMARY KEY,
  username varchar(50) NOT NULL,
  password varchar(100) NOT NULL,
  email varchar(100) NOT NULL,
  phone varchar(20),
  user_type varchar(16) NOT NULL,
  name varchar(50),
  avatar varchar(200),
  status int NOT NULL,
  created_at timestamp,
  updated_at timestamp,
  sex varchar(16),
  score int,
  age int,
  honor varchar(512)
);

INSERT INTO `user` (id, username, password, email, user_type, status, score)
VALUES (99001, 'qa_child', 'not-used', 'qa@example.test', 'USER', 1, 0);

CREATE TABLE acupoint_knowledge (
  code varchar(16) PRIMARY KEY,
  point_number int NOT NULL,
  name varchar(64) NOT NULL,
  pinyin varchar(128),
  meridian_code varchar(8) NOT NULL,
  meridian_name varchar(64) NOT NULL,
  meridian_english varchar(128),
  model_id varchar(32) UNIQUE,
  body_area varchar(64) NOT NULL,
  standard_location varchar(1000),
  child_location varchar(500) NOT NULL,
  child_description varchar(500) NOT NULL,
  child_traditional_use varchar(500),
  safety_tip varchar(255) NOT NULL,
  source_name varchar(255),
  source_link varchar(512),
  traditional_use_source_name varchar(255),
  traditional_use_source_link varchar(512),
  position_x decimal(12,6),
  position_y decimal(12,6),
  position_z decimal(12,6),
  sort_order int NOT NULL,
  enabled boolean NOT NULL DEFAULT true
);

CREATE TABLE user_acupoint_daily_progress (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  case_date date NOT NULL,
  acupoint_code varchar(16) NOT NULL,
  discovered_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_user_case_acupoint UNIQUE (user_id, case_date, acupoint_code)
);

CREATE TABLE user_copper_man_profile (
  user_id bigint PRIMARY KEY,
  copper_tokens int NOT NULL DEFAULT 0,
  star_sand int NOT NULL DEFAULT 0,
  completed_cases int NOT NULL DEFAULT 0,
  last_completed_case_date date
);

CREATE TABLE user_agency_archive (
  user_id bigint NOT NULL,
  archive_item_id varchar(64) NOT NULL,
  repaired_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, archive_item_id)
);

CREATE TABLE user_checkin (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  checkin_date date NOT NULL,
  created_at timestamp,
  CONSTRAINT uk_h2_user_checkin UNIQUE (user_id, checkin_date)
);

CREATE TABLE user_game_reward (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  game_code varchar(64) NOT NULL,
  reward_date date NOT NULL,
  score_awarded int NOT NULL,
  created_at timestamp,
  CONSTRAINT uk_h2_game_reward UNIQUE (user_id, game_code, reward_date)
);

CREATE TABLE write_operation (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint,
  operation_key varchar(160) NOT NULL,
  request_hash varchar(64) NOT NULL,
  operation_type varchar(80) NOT NULL,
  status varchar(24) NOT NULL,
  response_json clob,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at timestamp,
  CONSTRAINT uk_h2_write_operation UNIQUE (user_id, operation_key)
);

CREATE TABLE audit_event (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  actor_user_id bigint,
  request_id varchar(128),
  action varchar(100) NOT NULL,
  resource_type varchar(100),
  resource_id varchar(128),
  outcome varchar(32) NOT NULL,
  details_json clob,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_session (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  token_id varchar(128) NOT NULL,
  issued_at timestamp NOT NULL,
  expires_at timestamp NOT NULL,
  last_seen_at timestamp,
  revoked_at timestamp,
  revoke_reason varchar(255),
  CONSTRAINT uk_h2_auth_session_token UNIQUE (token_id)
);

CREATE TABLE outbox_event (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  event_key varchar(160) NOT NULL,
  event_type varchar(100) NOT NULL,
  aggregate_type varchar(100),
  aggregate_id varchar(128),
  payload_json clob NOT NULL,
  status varchar(24) NOT NULL,
  retry_count int NOT NULL DEFAULT 0,
  available_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  processed_at timestamp,
  last_error varchar(1000),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_h2_outbox_event_key UNIQUE (event_key)
);

CREATE TABLE user_task_progress (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  game_code varchar(64) NOT NULL,
  task_code varchar(100) NOT NULL,
  period_key varchar(64) NOT NULL,
  progress int NOT NULL DEFAULT 0,
  target int NOT NULL DEFAULT 1,
  status varchar(24) NOT NULL DEFAULT 'IN_PROGRESS',
  source varchar(32) NOT NULL DEFAULT 'SERVER',
  completed_at timestamp,
  claimed_at timestamp,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  config_version int NOT NULL DEFAULT 1,
  CONSTRAINT uk_h2_task_progress UNIQUE (user_id, game_code, task_code, period_key)
);

CREATE TABLE game_config_revision (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  game_code varchar(64) NOT NULL,
  version_no int NOT NULL,
  status varchar(16) NOT NULL,
  base_version int,
  edit_version int NOT NULL DEFAULT 0,
  created_by bigint,
  updated_by bigint,
  published_by bigint,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at timestamp,
  CONSTRAINT uk_h2_game_config_version UNIQUE (game_code, version_no)
);

CREATE TABLE game_level_revision (
  revision_id bigint NOT NULL,
  level_id varchar(64) NOT NULL,
  order_no int NOT NULL,
  label varchar(128) NOT NULL,
  status_text varchar(128) NOT NULL,
  description varchar(1000) NOT NULL,
  icon varchar(32) NOT NULL,
  seal varchar(32) NOT NULL,
  position_x decimal(8,3) NOT NULL,
  position_y decimal(8,3) NOT NULL,
  route varchar(128) NOT NULL,
  map_key varchar(64) NOT NULL,
  cover_path varchar(512),
  public_flag boolean NOT NULL DEFAULT true,
  PRIMARY KEY (revision_id, level_id)
);

CREATE TABLE game_task_revision (
  revision_id bigint NOT NULL,
  task_code varchar(128) NOT NULL,
  level_id varchar(64),
  name varchar(128) NOT NULL,
  description varchar(1000) NOT NULL,
  task_type varchar(64) NOT NULL,
  route varchar(128) NOT NULL,
  target int NOT NULL DEFAULT 1,
  editable_flag boolean NOT NULL DEFAULT false,
  PRIMARY KEY (revision_id, task_code)
);

CREATE TABLE game_task_reward_revision (
  revision_id bigint NOT NULL,
  task_code varchar(128) NOT NULL,
  sort_order int NOT NULL DEFAULT 0,
  reward_type varchar(32) NOT NULL,
  item_code varchar(128),
  amount int NOT NULL DEFAULT 0,
  score_delta int NOT NULL DEFAULT 0,
  PRIMARY KEY (revision_id, task_code, sort_order)
);

CREATE TABLE game_level_prerequisite_revision (
  revision_id bigint NOT NULL,
  level_id varchar(64) NOT NULL,
  prerequisite_level_id varchar(64) NOT NULL,
  PRIMARY KEY (revision_id, level_id, prerequisite_level_id)
);

CREATE TABLE reward_ledger (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id bigint NOT NULL,
  grant_key varchar(180) NOT NULL,
  source varchar(64) NOT NULL,
  task_code varchar(100),
  reward_type varchar(32) NOT NULL,
  item_code varchar(100),
  amount int NOT NULL DEFAULT 0,
  score_delta int NOT NULL DEFAULT 0,
  idempotency_key varchar(160),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_h2_reward_grant UNIQUE (user_id, grant_key)
);

CREATE TABLE user_material_balance (
  user_id bigint NOT NULL,
  material_code varchar(100) NOT NULL,
  amount int NOT NULL DEFAULT 0,
  version bigint NOT NULL DEFAULT 0,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, material_code)
);

CREATE TABLE user_game_migration (
  user_id bigint NOT NULL,
  source_key varchar(160) NOT NULL,
  source_version varchar(64),
  state_hash varchar(64) NOT NULL,
  status varchar(24) NOT NULL,
  imported_at timestamp,
  last_error varchar(1000),
  PRIMARY KEY (user_id, source_key)
);

CREATE TABLE user_safety_game_draft (
  user_id bigint NOT NULL PRIMARY KEY,
  draft_json clob NOT NULL,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

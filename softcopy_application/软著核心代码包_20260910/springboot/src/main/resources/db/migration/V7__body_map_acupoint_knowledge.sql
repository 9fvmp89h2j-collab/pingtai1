CREATE TABLE IF NOT EXISTS body_map_acupoint_knowledge (
  acupoint_code VARCHAR(16) NOT NULL,
  region_code VARCHAR(16) NOT NULL,
  body_view VARCHAR(8) NOT NULL,
  placement_hint VARCHAR(500) NOT NULL,
  child_map_description VARCHAR(500) NOT NULL,
  sort_order INT NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (acupoint_code),
  KEY idx_body_map_region_sort (region_code, sort_order),
  CONSTRAINT fk_body_map_acupoint
    FOREIGN KEY (acupoint_code) REFERENCES acupoint_knowledge (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身体地图儿童穴位归位知识库';

INSERT INTO body_map_acupoint_knowledge
  (acupoint_code, region_code, body_view, placement_hint, child_map_description, sort_order, enabled)
SELECT
  a.code,
  CASE a.body_area
    WHEN '头面与颈部' THEN 'head'
    WHEN '胸部' THEN 'chest'
    WHEN '腹部' THEN 'belly'
    WHEN '肩臂部' THEN 'shoulder'
    WHEN '前臂与肘部' THEN 'forearm'
    WHEN '手腕部' THEN 'wrist'
    WHEN '髋腿部' THEN 'leg'
    WHEN '踝足部' THEN 'ankle'
  END,
  CASE
    WHEN a.body_area IN ('肩臂部', '前臂与肘部', '髋腿部') THEN 'back'
    ELSE 'front'
  END,
  CONCAT(
    '先看',
    CASE WHEN a.body_area IN ('肩臂部', '前臂与肘部', '髋腿部') THEN '右边的背面人物' ELSE '左边的正面人物' END,
    '，再点击“', a.body_area, '”圆圈。沿着圆圈箭头看过去，把“', a.name, '星”送回这个身体区域。'
  ),
  CONCAT('“', a.name, '”在身体地图中属于', a.body_area, '。这里只判断身体大区域，不需要在自己或同学身上寻找穴位。'),
  a.sort_order,
  a.enabled
FROM acupoint_knowledge a
WHERE a.body_area IN ('头面与颈部', '胸部', '腹部', '肩臂部', '前臂与肘部', '手腕部', '髋腿部', '踝足部')
ON DUPLICATE KEY UPDATE
  region_code = VALUES(region_code),
  body_view = VALUES(body_view),
  placement_hint = VALUES(placement_hint),
  child_map_description = VALUES(child_map_description),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

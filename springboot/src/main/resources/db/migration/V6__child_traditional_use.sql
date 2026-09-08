ALTER TABLE acupoint_knowledge
  ADD COLUMN child_traditional_use VARCHAR(500) NULL AFTER child_description,
  ADD COLUMN traditional_use_source_name VARCHAR(255) NULL AFTER source_link,
  ADD COLUMN traditional_use_source_link VARCHAR(512) NULL AFTER traditional_use_source_name;

UPDATE acupoint_knowledge
SET child_traditional_use = CONCAT(
      '传统认识中，“', name, '”常与',
      CASE
        WHEN meridian_code = 'LU' THEN '呼吸、咽喉和胸部舒适'
        WHEN meridian_code = 'LI' AND body_area = '头面与颈部' THEN '口鼻、面部和咽喉舒适'
        WHEN meridian_code = 'LI' THEN '手臂活动以及口鼻、咽喉舒适'
        WHEN meridian_code = 'ST' AND body_area = '头面与颈部' THEN '口鼻、面部和头部舒适'
        WHEN meridian_code = 'ST' AND body_area = '胸部' THEN '胸部舒适和饮食消化'
        WHEN meridian_code = 'ST' AND body_area = '腹部' THEN '饮食、消化和腹部舒适'
        WHEN meridian_code = 'ST' THEN '腿脚活动、饮食和腹部舒适'
        WHEN meridian_code = 'PC' THEN '手腕活动、胸部舒适和情绪安定'
        WHEN meridian_code = 'CV' AND body_area = '头面与颈部' THEN '咽喉、口部和声音舒适'
        WHEN meridian_code = 'CV' AND body_area = '胸部' THEN '呼吸、胸部舒适和情绪安定'
        WHEN meridian_code = 'CV' THEN '饮食、腹部舒适和身体活力'
        ELSE '身体舒适与经络文化'
      END,
      '等身体话题相关。这里只了解传统文化，不用它判断或处理身体不舒服。'
    ),
    traditional_use_source_name = 'GB/T 12346-2021《经穴名称与定位》；《经络腧穴学》课程资料（儿童化改写）',
    traditional_use_source_link = 'https://www.muhn.edu.cn/zyxy/info/1027/7015.htm'
WHERE enabled = 1
  AND position_x IS NOT NULL AND position_y IS NOT NULL AND position_z IS NOT NULL;

UPDATE copper_acupoint_revision r
JOIN acupoint_knowledge a ON a.code = r.acupoint_code
SET r.payload_json = JSON_SET(
      r.payload_json,
      '$.childTraditionalUse', a.child_traditional_use,
      '$.traditionalUseSourceName', a.traditional_use_source_name,
      '$.traditionalUseSourceLink', a.traditional_use_source_link
    )
WHERE a.child_traditional_use IS NOT NULL;

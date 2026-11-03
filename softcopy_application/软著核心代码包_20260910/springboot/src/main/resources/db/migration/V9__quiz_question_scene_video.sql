ALTER TABLE `quiz_question`
  ADD COLUMN `scene_video_path` varchar(512) NULL COMMENT '题目场景视频地址' AFTER `scene_caption`;

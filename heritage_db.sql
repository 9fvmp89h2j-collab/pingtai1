/*
 Navicat MySQL Data Transfer

 Source Server         : db
 Source Server Type    : MySQL
 Source Server Version : 80033
 Source Host           : localhost:3306
 Source Schema         : heritage_db

 Target Server Type    : MySQL
 Target Server Version : 80033
 File Encoding         : 65001

 Date: 23/04/2026 10:29:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for badge
-- ----------------------------
DROP TABLE IF EXISTS `badge`;
CREATE TABLE `badge`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `badgepath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `badgename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of badge
-- ----------------------------
INSERT INTO `badge` VALUES (9, 1, 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '签到 1 天');
INSERT INTO `badge` VALUES (10, 1, 'E:/pingtai1/vue3/src/assets/badge/小镇1.png', '抵抗风寒感冒');
INSERT INTO `badge` VALUES (11, 4, 'E:/pingtai1/vue3/src/assets/badge/小镇1.png', '抵抗风寒感冒');
INSERT INTO `badge` VALUES (12, 4, 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '签到 1 天');
INSERT INTO `badge` VALUES (13, 6, 'E:/pingtai1/vue3/src/assets/badge/小镇1.png', '抵抗风寒感冒');
INSERT INTO `badge` VALUES (14, 6, 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '签到 1 天');
INSERT INTO `badge` VALUES (15, 2, 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '签到 1 天');
INSERT INTO `badge` VALUES (16, 7, 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '签到 1 天');
INSERT INTO `badge` VALUES (17, 7, 'E:/pingtai1/vue3/src/assets/badge/小镇1.png', '抵抗风寒感冒');
INSERT INTO `badge` VALUES (18, 8, 'E:/pingtai1/vue3/src/assets/badge/小镇1.png', '抵抗风寒感冒');
INSERT INTO `badge` VALUES (19, 8, 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '签到 1 天');

-- ----------------------------
-- Table structure for community_post
-- ----------------------------
DROP TABLE IF EXISTS `community_post`;
CREATE TABLE `community_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count` int NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `postcatagory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 0 COMMENT '0正常 1已删除 2已举报',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `posttype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `postpic1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `postpic2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `postpic3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `postpic4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `postpic5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status_create`(`status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 119 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '针灸答疑帖子' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of community_post
-- ----------------------------
INSERT INTO `community_post` VALUES (111, 1, '测试', '测试测试测试测试测试', 9, 19, 22, NULL, 0, '2026-02-03 05:43:32', '2026-04-07 09:15:51', '分享专区', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `community_post` VALUES (112, 1, '原来合谷穴是 “止痛小开关”', '今天不小心磕到了手，妈妈教我按了合谷穴，真的没那么疼了！针灸的知识太有用啦，大家也要记住这个神奇的穴位哦！🤚', 1, 1, 1, NULL, 0, '2026-02-26 19:26:02', '2026-04-07 09:15:58', '分享专区', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `community_post` VALUES (113, 1, '【反馈】很满意', '很满意', 0, 0, 0, NULL, 0, '2026-04-07 09:21:18', '2026-04-07 09:32:21', '反馈', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `community_post` VALUES (114, 2, '2026.4.7网站维护公告', '本网站计划于2026.4.7 22:00-24:00进行维护', 0, 0, 0, NULL, 0, '2026-04-07 19:27:33', '2026-04-07 19:27:33', '公告', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `community_post` VALUES (116, 1, '获得了徽章', '', 0, 0, 0, NULL, 0, '2026-04-15 22:22:15', '2026-04-15 22:22:15', '分享专区', '/files/bussiness/post_content/1776262922843.png', '', '', '', '');
INSERT INTO `community_post` VALUES (117, 1, '最近获得好多成就徽章~', '终极挑战满分 1 次\n签到 3 天\n抵抗风寒感冒\n抵抗咳嗽\n终极挑战满分 3 次', 0, 0, 0, NULL, 0, '2026-04-15 22:33:51', '2026-04-15 22:33:51', '分享专区', 'E:/pingtai1/vue3/src/assets/badge/成就2.png', 'E:/pingtai1/vue3/src/assets/badge/成就2.png', 'E:/pingtai1/vue3/src/assets/badge/小镇6.png', 'E:/pingtai1/vue3/src/assets/badge/小镇6.png', 'E:/pingtai1/vue3/src/assets/badge/成就2.png');
INSERT INTO `community_post` VALUES (118, 7, '最近获得好多成就徽章~', '抵抗风寒感冒\n签到 1 天', 0, 0, 0, NULL, 0, '2026-04-22 17:17:31', '2026-04-22 17:17:31', '分享专区', 'E:/pingtai1/vue3/src/assets/badge/小镇1.png', 'E:/pingtai1/vue3/src/assets/badge/成就2.png', '', '', '');

-- ----------------------------
-- Table structure for doctorstory
-- ----------------------------
DROP TABLE IF EXISTS `doctorstory`;
CREATE TABLE `doctorstory`  (
  `doctorid` int NOT NULL AUTO_INCREMENT,
  `doctorname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `doctorbrief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `doctordetail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '名医详解',
  `skillid` int NULL DEFAULT NULL,
  `doctorpic1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `doctorpic2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `doctorpic3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `media` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `previewpic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '故事预览封面',
  PRIMARY KEY (`doctorid`) USING BTREE,
  INDEX `doctor_skillid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `doctor_skillid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctorstory
-- ----------------------------
INSERT INTO `doctorstory` (`doctorid`, `doctorname`, `doctorbrief`, `doctordetail`, `skillid`, `doctorpic1`, `doctorpic2`, `doctorpic3`, `media`, `previewpic`) VALUES (1, '神医扁鹊的传奇故事', '神医扁鹊竟然可以令人起死回生！', '在两千多年前的春秋战国时期，有一位大名鼎鼎的神医，名叫扁鹊。\n \n扁鹊从小就特别聪明好学，他跟着一位叫长桑君的良医学习医术。他每天认真观察、刻苦钻研，把老师的治病经验都牢牢记在心里，还自己摸索出了很多新方法。学成之后，他背着药箱走遍各国，免费给老百姓看病，不管是穷人还是富人，他都一视同仁，用心救治。\n \n有一次，扁鹊路过虢国，正好赶上虢国太子“去世”，全城都在办丧事。扁鹊仔细观察了太子的“尸体”，发现太子还有微弱的呼吸，只是得了一种叫“尸厥”的病，看起来像死了，其实只是暂时昏迷。他立刻拿出砭石和银针，在太子的百会穴等穴位上扎针，又用热敷的方法调理。没过多久，太子竟然慢慢睁开了眼睛，醒了过来！这件事传开后，大家都惊叹扁鹊能“起死回生”，但扁鹊却谦虚地说：“我只是让本来能活的人恢复健康而已。”\n \n还有一次，扁鹊去见齐国的齐桓侯。他第一次见面就说：“您的身体里有小病，在皮肤里，不治疗会加重。”齐桓侯觉得自己没病，根本不信。过了几天，扁鹊又见到他，说：“病已经到了血脉里，再不治就晚了。”齐桓侯还是不理他。又过了几天，扁鹊说：“病已经到了肠胃里，再不治就没救了！”齐桓侯依旧生气地拒绝了。最后一次见面，扁鹊远远看到齐桓侯，转身就走，因为他知道，病已经深入骨髓，再也治不好了。果然，没过几天，齐桓侯就病死了。\n \n扁鹊用高超的医术救了无数人，他还把针灸、望闻问切等方法发扬光大，被后世尊为“医学祖师”。虽然他最后被坏人害死，但他的故事和医术，永远被人们铭记。', 6, '/files/bussiness/start/1775551111826.jpeg', '/files/bussiness/start/1775551123485.png', '/files/bussiness/start/1775551129067.jpeg', 'files/bussiness/media/屏幕录制 2026-04-07195420.mp4', '/doctor-story-preview/1.jpg');
INSERT INTO `doctorstory` (`doctorid`, `doctorname`, `doctorbrief`, `doctordetail`, `skillid`, `doctorpic1`, `doctorpic2`, `doctorpic3`, `media`, `previewpic`) VALUES (2, '外科圣手华佗的针灸传奇', '华佗真的太惨了', '东汉末年，有一位家喻户晓的神医，名叫华佗。他不仅发明了世界上最早的麻醉药“麻沸散”，还是一位针灸高手！\n \n华佗从小就立志学医，救死扶伤。他走遍名山大川，采集草药，钻研医术，尤其精通针灸和外科手术。他的针灸技术特别神奇，不管是头疼脑热，还是腰腿疼痛，只要他扎上几针，病人很快就能痊愈。\n \n有一次，一位患者得了严重的脚病，双脚疼得无法走路，家人用轿子把他抬到华佗家求治。华佗仔细检查后，在他背上的穴位上扎了针，又用艾灸熏烤。没过多久，患者的脚就不疼了，竟然能自己走路回家了！还有一位将军的妻子怀了双胞胎，生孩子时遇到了难产，第一个孩子生下来后，第二个孩子卡在肚子里出不来，生命垂危。华佗给她诊脉后，在她的“至阴穴”扎了一针，又给她喝了催产的汤药。很快，第二个孩子也顺利生了下来，母子平安！\n \n华佗的针灸技术还有一个特别厉害的地方：他扎针的时候，会告诉患者“针感传到哪里，病就好到哪里”，而且他敢在别人不敢扎的胸、背部穴位下针，还能扎得很深，效果却特别好。他还发明了“夹脊相去三寸”的新取穴方法，直到今天还在使用。\n \n可惜的是，华佗因为不愿意给曹操当私人医生，被曹操残忍杀害，他的医书也没能流传下来。但他的医术和仁心，永远被人们怀念，成为了中医史上的传奇。', 7, 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', '/files/bussiness/start/1775482394001.png', '/files/bussiness/start/1775551129067.jpeg', 'files/bussiness/media/屏幕录制 2026-04-07195420.mp4', '/doctor-story-preview/2.jpg');
INSERT INTO `doctorstory` (`doctorid`, `doctorname`, `doctorbrief`, `doctordetail`, `skillid`, `doctorpic1`, `doctorpic2`, `doctorpic3`, `media`, `previewpic`) VALUES (3, '针灸经典的编写者皇甫谧', '《针灸甲乙经》', '在西晋时期，有一位叫皇甫谧的学者，他用毕生心血，编写了中国第一部针灸学专著《针灸甲乙经》，被后世尊为“针灸鼻祖”。\n \n皇甫谧小时候特别调皮，不爱读书，整天游手好闲，叔叔婶婶都为他发愁。有一次，他摘了野果子给叔母吃，叔母却哭着对他说：“你都这么大了，还不努力读书，将来怎么能有出息呢？我养你不是为了让你孝顺我，是希望你能成才啊！”皇甫谧听了特别羞愧，从此下定决心，发愤读书。\n \n因为家里穷，买不起书，他就到处借书抄，一边种地一边读书，把书带在身边，一有空就看。功夫不负有心人，他成了一位博学多才的大学者。可不幸的是，中年时他得了风痹症，四肢麻木，耳朵也聋了，还误服了药物，差点丧命。在病痛中，他深深体会到医学的重要性，于是开始刻苦钻研针灸，把古代所有和针灸相关的医书都搜集起来，反复对比、整理，删掉重复的内容，纠正错误的地方，花了好几年时间，终于编成了《针灸甲乙经》。\n \n这本书就像针灸的“百科全书”，里面详细记载了349个穴位的位置、功效，还有针灸的操作方法、禁忌，甚至把错误的穴位位置都一一纠正了。它不仅是中国第一部针灸学专著，还传到了朝鲜、日本等国家，成为了全世界针灸医生的必读经典。\n \n皇甫谧虽然一生病痛缠身，但他用顽强的毅力，为中医针灸学做出了巨大的贡献，他的故事也告诉我们：只要努力，就算身处逆境，也能创造奇迹！', 8, NULL, NULL, NULL, NULL, '/doctor-story-preview/3.jpg');
INSERT INTO `doctorstory` (`doctorid`, `doctorname`, `doctorbrief`, `doctordetail`, `skillid`, `doctorpic1`, `doctorpic2`, `doctorpic3`, `media`, `previewpic`) VALUES (4, '孙思邈：妙手回春的 “药王”', '孙思邈是我国唐代超级厉害的神医，被大家尊称为 “药王”。他不仅医术高超，还特别善良，一辈子免费给老百姓看病，还写下了很多医学巨著，是针灸史上的大名人。', '孙思邈从小身体就不好，经常生病，家里为了给他治病花光了钱。可他特别爱学习，20 岁就精通了各种医学知识，开始给乡亲们看病。他一辈子不慕名利，拒绝了皇帝给的高官厚禄，背着药箱、拿着金针，到处给老百姓治病。\n有一次，他在路上看到人们抬着一口棺材，后面跟着哭得伤心的老妇人。孙思邈发现棺材缝里滴出了血，赶紧拦住大家说：“快打开棺材！人还活着！” 原来这是一位难产的妈妈，孩子没生下来，妈妈也没了气息。孙思邈立刻给她扎了几针，没过多久，胖娃娃 “哇” 地一声哭了出来，妈妈也睁开了眼睛！大家都惊呼他是 “妙手回春” 的神医。\n孙思邈还写下了《备急千金要方》《千金翼方》两部巨著，里面记载了超多针灸知识。他发明了 “同身寸取穴法”，用自己的手指当尺子，不管大人小孩都能精准找到穴位；还画了彩色的针灸挂图，把人体经络标得清清楚楚，让针灸变得好学又好用。他说 “人命至重，贵于千金”，一辈子把病人的生命放在第一位，是真正的仁心神医。', 9, NULL, NULL, NULL, NULL, '/doctor-story-preview/4.jpg');
INSERT INTO `doctorstory` (`doctorid`, `doctorname`, `doctorbrief`, `doctordetail`, `skillid`, `doctorpic1`, `doctorpic2`, `doctorpic3`, `media`, `previewpic`) VALUES (5, '神奇的针灸铜人', '针灸铜人是北宋时期，由医学家王惟一所设计铸造的两座青铜人体模型，是世界上最早的针灸教学模型，就像古代的 “3D 穴位教科书”，超级厉害！', '北宋时期，很多针灸书因为传抄太久，穴位位置都写错了，医生治病很容易出错。医学家王惟一特别着急，决心把穴位位置统一、纠正错误。他参考了《黄帝内经》等很多古书，结合自己的临床经验，写成了《铜人腧穴针灸图经》，还设计铸造了两座和真人一样大的青铜针灸铜人。\n这两座铜人可太神奇了！铜人能分成前后两半，里面有五脏六腑的模型，体表刻满了经络和 354 个穴位，每个穴位都钻了小孔。考试的时候，考官会在铜人外面涂一层黄蜡，把穴位盖住，再往铜人肚子里灌满水银。学生扎针的时候，如果扎中了穴位，拔针时水银就会流出来；如果扎偏了，水银就出不来，特别精准！\n皇帝特别喜欢这两座铜人，把一座放在官方医学院 “医官院”，让医生们学习；另一座放在大相国寺，供老百姓参观。可惜后来北宋被金兵打败，铜人也下落不明，其中一座流落到了国外，成了我们民族的一大损失。但针灸铜人永远是中国古代医学智慧的骄傲！', 10, NULL, NULL, NULL, NULL, '/doctor-story-preview/5.jpg');
INSERT INTO `doctorstory` (`doctorid`, `doctorname`, `doctorbrief`, `doctordetail`, `skillid`, `doctorpic1`, `doctorpic2`, `doctorpic3`, `media`, `previewpic`) VALUES (6, '针灸界的 “百科全书”', '《针灸大成》是明代针灸大师杨继洲写的医学巨著，是集古代针灸学精华于一身的 “针灸百科全书”，直到今天还是针灸医生的必读书。', '杨继洲出生在医学世家，从小就跟着家里人学医，一辈子行医 40 多年，治好了无数疑难杂症。他博览群书，把明代以前所有的针灸知识都收集起来，结合自己的临床经验，写成了《针灸大成》。\n这本书里不仅有经络穴位、针灸手法的详细讲解，还有超多治病案例。比如有个官员胃里长了个大痞块，杨继洲用针灸扎了几次，痞块就消失了；还有个病人腿疼了十年，吃了好多药都没用，杨继洲扎了几针，十天就好了；甚至有个病人病危，脉搏都快没了，杨继洲用艾灸救回了他的命。\n杨继洲还特别厉害，他不迷信书本，纠正了很多古代针灸书里的错误，还发明了很多实用的针灸手法，写了针灸歌诀，让针灸变得更好学。《针灸大成》就像一座针灸知识的宝库，把古代针灸的精华都保存了下来，让后世的医生有了最好的学习资料。', 11, NULL, NULL, NULL, NULL, '/doctor-story-preview/6.jpg');

ALTER TABLE `doctorstory`
  ADD COLUMN `readingglossary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '重点词词库JSON' AFTER `previewpic`;

UPDATE `doctorstory`
SET `readingglossary` = '[{"word":"针灸","pinyin":"zhēn jiǔ","meaning":"一种用针和艾灸来帮助身体恢复的传统疗法。"},{"word":"神医","pinyin":"shén yī","meaning":"大家很佩服、医术非常高明的医生。"},{"word":"扁鹊","pinyin":"biǎn què","meaning":"战国时期很有名的医生，常被大家称作神医。"},{"word":"长桑君","pinyin":"cháng sāng jūn","meaning":"传说中教扁鹊学医的老师。"},{"word":"虢国","pinyin":"guó guó","meaning":"古代一个国家的名字。"},{"word":"春秋战国","pinyin":"chūn qiū zhàn guó","meaning":"中国古代历史上的一个时期。"},{"word":"起死回生","pinyin":"qǐ sǐ huí shēng","meaning":"形容把非常危险的人从生死边缘救回来。"}]'
WHERE `doctorid` = 1;

UPDATE `doctorstory`
SET `readingglossary` = '[{"word":"针灸","pinyin":"zhēn jiǔ","meaning":"一种用针和艾灸来帮助身体恢复的传统疗法。"},{"word":"神医","pinyin":"shén yī","meaning":"大家很佩服、医术非常高明的医生。"},{"word":"华佗","pinyin":"huà tuó","meaning":"东汉末年的名医，擅长外科和针灸。"},{"word":"麻沸散","pinyin":"má fèi sǎn","meaning":"古代用来减轻手术疼痛的一种麻醉药。"},{"word":"东汉","pinyin":"dōng hàn","meaning":"中国古代的一个朝代。"},{"word":"外科","pinyin":"wài kē","meaning":"主要处理伤口、骨折、手术等问题的医学领域。"}]'
WHERE `doctorid` = 2;

UPDATE `doctorstory`
SET `readingglossary` = '[{"word":"针灸","pinyin":"zhēn jiǔ","meaning":"一种用针和艾灸来帮助身体恢复的传统疗法。"},{"word":"腧穴","pinyin":"shù xué","meaning":"人体上可以按、灸、针刺的关键点，也常叫穴位。"},{"word":"皇甫谧","pinyin":"huáng fǔ mì","meaning":"西晋时期的学者和医学家。"},{"word":"针灸甲乙经","pinyin":"zhēn jiǔ jiǎ yǐ jīng","meaning":"中国很早的一部针灸学专著。"},{"word":"西晋","pinyin":"xī jìn","meaning":"中国古代的一个朝代。"},{"word":"鼻祖","pinyin":"bí zǔ","meaning":"指某个领域里很早、很重要的开创者。"}]'
WHERE `doctorid` = 3;

UPDATE `doctorstory`
SET `readingglossary` = '[{"word":"针灸","pinyin":"zhēn jiǔ","meaning":"一种用针和艾灸来帮助身体恢复的传统疗法。"},{"word":"神医","pinyin":"shén yī","meaning":"大家很佩服、医术非常高明的医生。"},{"word":"孙思邈","pinyin":"sūn sī miǎo","meaning":"唐代著名医学家，大家常叫他药王。"},{"word":"药王","pinyin":"yào wáng","meaning":"大家对医术高明、品德也很好医生的一种尊称。"},{"word":"妙手回春","pinyin":"miào shǒu huí chūn","meaning":"形容医生医术高明，能让病人很快好起来。"},{"word":"难产","pinyin":"nán chǎn","meaning":"生产时遇到很大困难的情况。"}]'
WHERE `doctorid` = 4;

UPDATE `doctorstory`
SET `readingglossary` = '[{"word":"针灸","pinyin":"zhēn jiǔ","meaning":"一种用针和艾灸来帮助身体恢复的传统疗法。"},{"word":"腧穴","pinyin":"shù xué","meaning":"人体上可以按、灸、针刺的关键点，也常叫穴位。"},{"word":"王惟一","pinyin":"wáng wéi yī","meaning":"北宋时期研究针灸教学的重要医学家。"},{"word":"铜人","pinyin":"tóng rén","meaning":"用铜做成的人体模型，能帮助大家学习穴位。"},{"word":"北宋","pinyin":"běi sòng","meaning":"中国古代的一个朝代。"},{"word":"黄帝内经","pinyin":"huáng dì nèi jīng","meaning":"中国古代非常重要的一部医学经典。"},{"word":"针灸铜人","pinyin":"zhēn jiǔ tóng rén","meaning":"专门用来学习经络和穴位的人体模型。"}]'
WHERE `doctorid` = 5;

UPDATE `doctorstory`
SET `readingglossary` = '[{"word":"针灸","pinyin":"zhēn jiǔ","meaning":"一种用针和艾灸来帮助身体恢复的传统疗法。"},{"word":"针灸大成","pinyin":"zhēn jiǔ dà chéng","meaning":"明代一部非常重要的针灸医学著作。"},{"word":"杨继洲","pinyin":"yáng jì zhōu","meaning":"明代有名的针灸大师。"},{"word":"艾灸","pinyin":"ài jiǔ","meaning":"把艾草点燃后用热力帮助身体调理的方法。"}]'
WHERE `doctorid` = 6;

-- ----------------------------
-- Table structure for extracourse
-- ----------------------------
DROP TABLE IF EXISTS `extracourse`;
CREATE TABLE `extracourse`  (
  `extracourseid` int NOT NULL AUTO_INCREMENT,
  `extracoursename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extracoursebrief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extracoursedes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extracourseicon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extracoursepic1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extracoursepic2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extracoursepic3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillid` int NULL DEFAULT NULL,
  PRIMARY KEY (`extracourseid`) USING BTREE,
  INDEX `extracourse_skillid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `extracourse_skillid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of extracourse
-- ----------------------------
INSERT INTO `extracourse` VALUES (1, '灸法', '用艾草燃烧的热量熏烤穴位，给身体温暖和能量。', '灸法不扎针，只用艾绒或艾条烤穴位，暖暖的很舒服。能温通经络、驱寒暖身，适合怕冷、肚子疼、拉肚子、感冒、体质虚寒的人，使用时要注意防火防烫。', NULL, NULL, NULL, NULL, 45);
INSERT INTO `extracourse` VALUES (2, '灸法材料', '艾灸主要用艾草制成的艾绒，还有姜片、蒜片等辅助材料。', '艾草晒干捣碎成艾绒，可做成艾条、艾柱。隔姜灸、隔蒜灸能加强效果，更温和安全。优质艾绒烟小、热力柔和，适合日常保健使用。', NULL, NULL, NULL, NULL, 46);
INSERT INTO `extracourse` VALUES (3, '拔罐法', '用罐子吸在皮肤上，帮助排出湿气寒气、放松肌肉。', '利用负压让罐子吸住皮肤，使局部皮肤充血，疏通经络、祛除寒湿。常用于感冒、咳嗽、腰酸背痛、疲劳乏力等，时间不宜过长，避免损伤皮肤。', NULL, NULL, NULL, NULL, 47);
INSERT INTO `extracourse` VALUES (4, '拔罐法-火罐法', '用火加热罐内空气，使罐子吸附在皮肤上的传统拔罐。', '用燃烧的酒精棉 briefly 加热罐内，迅速扣在皮肤上，利用热气形成吸力。热力强、驱寒效果好，但有明火，必须由专业人员操作，儿童慎用。', NULL, NULL, NULL, NULL, 48);
INSERT INTO `extracourse` VALUES (5, '拔罐法-走罐法', '罐子吸住后在皮肤上推拉移动，像拔罐加按摩。', '先涂润滑油，将罐吸住后上下左右推动，覆盖更大面积经络。适合大面积肌肉僵硬、腰背酸痛、感冒发热等，力度要轻柔，保护皮肤不被擦伤。', NULL, NULL, NULL, NULL, 49);
INSERT INTO `extracourse` VALUES (6, '砭石法', '用特殊石头刮擦、按摩身体，是很古老的中医外治法。', '用光滑的砭石工具推揉、刮擦皮肤和穴位，不用针也不用火，安全无痛。可以疏通气血、缓解积食、感冒、肌肉酸痛，适合小朋友保健。', NULL, NULL, NULL, NULL, 50);
INSERT INTO `extracourse` VALUES (7, '刮痧法', '用刮痧板刮擦皮肤，排出体内火气和湿气。', '涂润滑介质后，用刮痧板沿经络刮擦，皮肤会出现红色痧痕，代表邪气排出。能快速缓解感冒发烧、喉咙痛、积食、浑身酸痛，力度要轻，刮后注意保暖。', NULL, NULL, NULL, NULL, 51);

-- ----------------------------
-- Table structure for illness
-- ----------------------------
DROP TABLE IF EXISTS `illness`;
CREATE TABLE `illness`  (
  `illnessid` int NOT NULL AUTO_INCREMENT,
  `cowtown` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illnessname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illnessfeature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xueweicount` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolscount` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xuewei1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xuewei2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xuewei3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xuewei4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xuewei5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tools1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tools2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tools3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tools4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illnesspic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illnessbadgename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illnessbadgepath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`illnessid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of illness
-- ----------------------------
INSERT INTO `illness` VALUES (1, '灵草小镇', '风寒感冒', '怕冷、流清鼻涕、打喷嚏、浑身酸痛、没力气，就像被冬天的冷风冻透了一样', '1', '1', '55', '', '', '', '', '35', '', '', '', '/files/bussiness/illness/感冒.png', '抵抗风寒感冒', 'E:/pingtai1/vue3/src/assets/badge/小镇1.png');
INSERT INTO `illness` VALUES (2, '时蔬小镇', '风热感冒', '发烧、嗓子疼、流黄鼻涕、口干舌燥、头胀乎乎的，就像身体里着了小火', '2', '2', '55', '63', '', '', '', '35', '38', '', '', '/files/bussiness/illness/感冒.png', '抵抗风热感冒', 'E:/pingtai1/vue3/src/assets/badge/小镇2.png');
INSERT INTO `illness` VALUES (3, '开心小镇', '头痛', '脑袋疼，有的像针扎一样突突跳，有的胀乎乎的，累了、感冒了、熬夜了都可能犯', '1', '2', '65', '', '', '', '', '35', '38', '', '', '', '抵抗头痛', 'E:/pingtai1/vue3/src/assets/badge/小镇3.png');

-- ----------------------------
-- Table structure for jingluo
-- ----------------------------
DROP TABLE IF EXISTS `jingluo`;
CREATE TABLE `jingluo`  (
  `jingluoid` int NOT NULL AUTO_INCREMENT,
  `jingluoname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillid` int NULL DEFAULT NULL,
  `jingluocatagory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jingluoorder` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illness` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jingluopic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`jingluoid`) USING BTREE,
  INDEX `jingluo_skillid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `jingluo_skillid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jingluo
-- ----------------------------
INSERT INTO `jingluo` VALUES (1, '手太阴肺经（简称：肺经）', 17, '十二经脉', '从肚子里的胃开始，往上穿过横膈膜，连到肺脏；再顺着气管、喉咙到肩膀，沿着手臂内侧最前面的那条线，一直走到大拇指指尖。\n还有一条小分支：从手腕附近（列缺穴）走到食指指尖，和下一条大肠经连起来。', '咳嗽、气喘、嗓子疼、感冒、胸闷，还有这条经络经过的胳膊、肩膀疼。', '/files/bussiness/jingluo/1775707209070.png');
INSERT INTO `jingluo` VALUES (2, '手阳明大肠经（简称：大肠经）', 18, '十二经脉', '从食指指尖开始，顺着手指、手背、手臂外侧的前缘，一直走到肩膀；再从肩膀绕到脖子、脸上，穿过鼻子旁边，最后到对侧的鼻子旁（迎香穴），和胃经相连。', '牙疼、脖子疼、嗓子疼、流鼻涕、拉肚子、肚子痛，还有脸、五官的小毛病。', '');
INSERT INTO `jingluo` VALUES (3, '足阳明胃经（简称：胃经）', 19, '十二经脉', '从鼻子旁边（迎香穴）往上到额头，再绕到下巴、脖子，顺着胸口、肚子往下走；到大腿、小腿外侧，一直走到第二个脚趾尖。\n还有分支：从胃里往上连到眼睛，从脚走到大脚趾，和脾经相连。', '胃痛、肚子胀、拉肚子、牙疼、脸肿、嗓子疼，还有这条经络经过的腿、脚疼。', '');
INSERT INTO `jingluo` VALUES (4, '足太阴脾经（简称：脾经）', 20, '十二经脉', '从大脚趾尖开始，顺着脚内侧、小腿内侧往上走，到大腿内侧，再穿过肚子、胸口，最后连到舌头、舌根。\n还有分支：从胃往上走到心脏，和心经相连。', '胃痛、肚子胀、拉肚子、没力气、手脚凉，还有妇科病、小便不舒服。', '');
INSERT INTO `jingluo` VALUES (5, '手少阴心经（简称：心经）', 21, '十二经脉', '从心脏出发，穿过横膈膜连到小肠；再顺着手臂内侧最靠后的那条线，一直走到小拇指指尖。\n还有分支：从心脏往上连到眼睛、喉咙。', '心慌、心痛、心烦、睡不着、嗓子干、口渴，还有胳膊内侧疼。', '');
INSERT INTO `jingluo` VALUES (6, '手太阳小肠经（简称：小肠经', 28, '十二经脉', '从小拇指指尖开始，顺着手臂外侧的后缘，走到肩膀；再绕到脖子、脸上，最后到眼睛内角（睛明穴），和膀胱经相连。\n还有分支：从脸上绕到耳朵，再到眼睛外角。', '耳朵疼、眼睛疼、嗓子疼、脸肿、肩膀疼、腰背痛，还有小肠相关的肚子不舒服。', '');
INSERT INTO `jingluo` VALUES (7, '足太阳膀胱经（简称：膀胱经）', 22, '十二经脉', '从眼睛内角（睛明穴）往上到头顶，再顺着后背脊柱两侧往下走，到大腿、小腿后侧，一直走到小脚趾尖。\n还有分支：从头顶绕到耳朵，从后背连到肾脏、膀胱。', '头疼、脖子疼、腰背痛、眼睛疼、小便不舒服，还有感冒、发烧。', '');
INSERT INTO `jingluo` VALUES (8, '足少阴肾经（简称：肾经）', 23, '十二经脉', '从脚底板（涌泉穴）开始，顺着脚内侧、小腿内侧往上走，到大腿内侧，再穿过肚子、胸口，最后连到肺、喉咙、舌头。\n还有分支：从肺往上走到心脏，和心包经相连。', '腰疼、水肿、拉肚子、气喘、嗓子疼，还有妇科病、牙齿松动、没力气。', '');
INSERT INTO `jingluo` VALUES (9, '手厥阴心包经（简称：心包经）', 24, '十二经脉', '从心脏外面的“保护罩”心包出发，穿过横膈膜连到上、中、下三焦；再顺着手臂内侧中间的那条线，一直走到中指尖。\n还有分支：从中指绕到无名指，和三焦经相连。', '心慌、心痛、胸闷、心烦、睡不着，还有胳膊内侧疼、胃疼。', '');
INSERT INTO `jingluo` VALUES (10, '手少阳三焦经（简称：三焦经）', 25, '十二经脉', '从无名指指尖开始，顺着手臂外侧中间的那条线，走到肩膀；再绕到脖子、耳朵、脸上，最后到眼睛外角（丝竹空穴），和胆经相连。\n还有分支：从耳朵绕到眼睛内角。', '耳朵疼、眼睛疼、嗓子疼、脸肿、肩膀疼、胳膊疼，还有腹胀、水肿。', '');
INSERT INTO `jingluo` VALUES (11, '足少阳胆经（简称：胆经）', 26, '十二经脉', '从眼睛外角（瞳子髎穴）开始，绕着耳朵、脸、头侧往下走，到肩膀、胸口、肚子；再顺着大腿、小腿外侧，一直走到第四个脚趾尖。\n还有分支：从脚走到大脚趾，和肝经相连。', '头疼、耳朵疼、眼睛疼、口苦、嗓子疼、肩膀疼、腿疼，还有肝胆不舒服、烦躁。', '');
INSERT INTO `jingluo` VALUES (12, '足厥阴肝经（简称：肝经）  ', 27, '十二经脉', '从大脚趾尖开始，顺着脚内侧、小腿内侧往上走，到大腿内侧，再绕着生殖器、肚子，连到肝脏、胆囊；再往上穿过横膈膜，连到眼睛、额头，最后到头顶。\n还有分支：从肝往上走到肺，和肺经相连，完成十二经络的大循环。', '头疼、眼睛疼、口苦、胸闷、烦躁，还有妇科病、肚子疼、疝气。', '');

-- ----------------------------
-- Table structure for misconception
-- ----------------------------
DROP TABLE IF EXISTS `misconception`;
CREATE TABLE `misconception`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答案',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '常见误区' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of misconception
-- ----------------------------
INSERT INTO `misconception` VALUES (1, '针灸的针很疼，会流血？', '才不会呢！针灸用的针超级细，比头发丝粗不了多少，专业操作下只会有一点点麻麻的、酸酸的感觉，几乎不疼，而且规范针灸是不会流血的哦！', 10, '2026-02-03 05:49:26');
INSERT INTO `misconception` VALUES (2, '所有人都能随便扎针灸吗？', '不对哦！针灸是 “魔法” 也是医术，小朋友、孕妇、身体特别虚弱的人，都不能随便扎针，必须找专业的医生来操作，自己可不能乱试！', 0, '2026-02-26 19:28:39');

-- ----------------------------
-- Table structure for originstory
-- ----------------------------
DROP TABLE IF EXISTS `originstory`;
CREATE TABLE `originstory`  (
  `originstoryID` int NOT NULL AUTO_INCREMENT,
  `storytitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storysubtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storytext` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storypic1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storypic2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storypic3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `media` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillid` int NULL DEFAULT NULL,
  PRIMARY KEY (`originstoryID`) USING BTREE,
  INDEX `skillid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `skillid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10000003 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of originstory
-- ----------------------------
INSERT INTO `originstory` VALUES (1, '艾灸的奇妙诞生', '温暖的火光', '在很久很久以前，人类还生活在原始的山洞里，没有高楼大厦，没有医院药房，更没有各种各样的药品。人们每天靠打猎、采摘野果为生，日子过得简单又辛苦。一旦生病或者受伤，只能默默忍受，没有任何办法缓解痛苦。\r\n \r\n冬天来临，山洞里阴冷潮湿，很多老人和猎人都会出现腰酸背痛、关节僵硬的问题，走路都变得十分困难。有一位年迈的猎人，腰腿疼痛了很多年，每到阴雨天就疼得睡不着觉。一天傍晚，他像往常一样坐在篝火旁取暖，跳动的火苗散发着暖暖的热气，包裹着他的身体。\r\n \r\n过了一会儿，老人惊奇地发现，自己疼了很久的腰腿居然轻松了很多，僵硬的关节也灵活了不少。他又惊又喜，连忙把这件事告诉了部落里的其他人。大家纷纷围坐在篝火边尝试，发现温热的火光确实能让身体舒服很多。\r\n \r\n后来，人们又发现，一种叫作艾草的植物晒干后点燃，会散发出淡淡的清香，而且燃烧时的温度温和持久，比普通柴火更适合用来温暖身体。人们把艾草搓成艾条，点燃后对着身体不舒服的地方熏烤，疼痛慢慢消失，寒气也被赶走了。就这样，充满智慧的祖先们，发明了温暖又神奇的艾灸。', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', '/files/video/1775742040580.mp4', 1);
INSERT INTO `originstory` VALUES (2, '针灸针的老祖先', '会治病的石头', '在原始部落里，人们经常需要外出打猎、采摘果实，难免会被尖锐的石头、树枝划伤皮肤。有一次，一个年轻的族人在山林里奔跑时，不小心被一块锋利的石头划破了脚趾，伤口流出了少量鲜血。他原本十分慌张，可没过多久，他突然发现，自己一直困扰的头痛竟然减轻了很多。\r\n \r\n这件奇怪的事很快在部落里传开了。又过了一段时间，一位妇女因为吃了生冷的野果，肚子疼痛难忍，在地上蜷缩着身体。她随手捡起一块圆润的小石头，在自己的腹部轻轻按压、揉搓，没想到过了一会儿，腹痛居然慢慢缓解了。\r\n \r\n族人们开始留意这些奇妙的现象，不断尝试后发现，不同形状的石头能解决不同的问题。尖尖的小石头，可以刺破肿胀的地方，排出淤血；圆润光滑的石头，可以用来按摩身体，缓解肌肉酸痛；扁平的石头，还能刮擦皮肤，让人感觉浑身舒畅。\r\n \r\n这些用来治病的石头，被古人称为“砭石”。它们虽然普通，却是针灸针最早的雏形，是古人对抗病痛的第一个小帮手。', '/files/bussiness/start/1775481627450.png', '/files/bussiness/start/1775481228370.jpeg', '', 'files/bussiness/media/屏幕录制 2026-04-07195420.mp4', 2);
INSERT INTO `originstory` VALUES (3, '中华针灸第一针，来自远古的智慧', '传说中的神医 —— 伏羲制九针', '在很久很久以前的远古时代，人们还不懂怎么科学治病，一旦身体不舒服、关节疼痛或者生了疮肿，只能默默忍受。那时候有一位非常聪明又善良的部落首领，他就是伏羲氏。看到大家常常被病痛折磨，伏羲心里特别难过，于是下定决心，一定要找到帮助人们减少痛苦的方法。\n他常常仰望天空观察日月运行，低头查看大地河流走向，还仔细观察花草树木和动物的习性，慢慢发现大自然的规律和人体有着奇妙的联系。他想到，既然尖锐的石头可以刺破脓包、舒缓肿痛，细长的树枝可以按压身体缓解酸痛，不如制作专门的工具，更安全、更精准地帮人们治病。\n于是，伏羲根据不同病症的需要，精心设计了九种形状、大小、用途都不一样的针具，这就是著名的 “九针”。有的针头圆润，用来按摩疏通；有的针头细长，用来深入穴位调理气血；有的针头扁平，用来排除淤积。九种针各有所长，能应对各种各样的病痛。伏羲把制针和用针的方法教给人们，从此针灸开始慢慢传承，成为守护中华民族健康千年的神奇医术。', NULL, NULL, NULL, 'files/bussiness/media/屏幕录制 2026-04-07195420.mp4', 3);
INSERT INTO `originstory` VALUES (4, '一根小银针，走过几千年', '针灸学从远古到今天的成长故事', '针灸并不是一夜之间出现的，它像一棵小树，在一代代中国人的呵护下，慢慢长成了枝繁叶茂的大树。最早的时候，我们的祖先使用磨得光滑的石头，叫作 “砭石”，用来刺破脓包、按压痛点，这就是针灸最原始的模样。\n随着时代进步，人们学会了冶炼金属，针具也从石头变成了青铜针、铁针、银针，最后变成了现在光滑、干净、韧性好的不锈钢毫针。针法越来越丰富，人们不仅知道扎哪里，还总结出了经络、穴位、补法、泻法等一整套完整的知识。\n在古代，许多名医不断总结经验，写下了《黄帝内经》《针灸甲乙经》《针灸大成》等重要书籍，让针灸从简单的止痛工具，变成了一套系统、科学、能治疗多种疾病的学问。到了现代，针灸不仅在中国家喻户晓，还走出国门，被世界许多国家学习使用，成为中华传统文化中最闪亮的瑰宝之一。\n', NULL, NULL, NULL, NULL, 4);
INSERT INTO `originstory` VALUES (5, '沉睡两千年的针灸秘籍', '马王堆西汉帛书里的经络故事', '在 1973 年，我国的考古学家在湖南长沙马王堆的西汉古墓里，有了一个震惊世界的大发现。除了精美的文物，他们还找到了一批写在柔软丝织品上的古书，这些丝书被称为 “帛书”，距离今天已经有两千两百多年的历史。\n在这些古老帛书中，有好几篇专门记载了人体经络和治病方法，比如《足臂十一脉灸经》《阴阳十一脉灸经》。这些书里清晰地描述了人体十一条重要经脉的走向，以及每条经脉能调理哪些部位的病痛，是目前我国发现的最早记录经络与针灸的文字资料。\n这些帛书虽然没有后来的针灸典籍完整，却真实记录了西汉早期人们对经络和针灸的认识，比《黄帝内经》的部分内容还要古老。它们像一封来自遥远古代的信，静静沉睡千年，告诉后人：早在汉代，我们的祖先就已经掌握了非常成熟的经络知识，针灸文化的深厚与古老，远远超出了人们的想象。\n', '/files/bussiness/start/1776849124638.png', '', '', '/files/video/1776849133516.mp4', 5);

-- ----------------------------
-- Table structure for post_collect
-- ----------------------------
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 109 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_collect
-- ----------------------------
INSERT INTO `post_collect` VALUES (108, 112, 1, '2026-04-15 21:50:32');

-- ----------------------------
-- Table structure for post_comment
-- ----------------------------
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_comment
-- ----------------------------
INSERT INTO `post_comment` VALUES (102, 111, 1, '测试', '2026-02-03 05:45:06');
INSERT INTO `post_comment` VALUES (103, 111, 1, '', '2026-02-03 06:04:28');
INSERT INTO `post_comment` VALUES (104, 111, 1, '', '2026-02-26 07:14:56');
INSERT INTO `post_comment` VALUES (105, 112, 1, '学到啦！', '2026-04-15 21:49:41');

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 109 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_like
-- ----------------------------
INSERT INTO `post_like` VALUES (108, 112, 1, '2026-04-15 21:50:06');

-- ----------------------------
-- Table structure for post_report
-- ----------------------------
DROP TABLE IF EXISTS `post_report`;
CREATE TABLE `post_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `reporter_user_id` bigint NOT NULL,
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '举报原因',
  `status` int NULL DEFAULT 0 COMMENT '0待处理 1已处理 2已驳回',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子举报' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_report
-- ----------------------------
INSERT INTO `post_report` VALUES (1, 112, 1, '', 0, '2026-04-06 23:32:29');

-- ----------------------------
-- Table structure for quiz_question
-- ----------------------------
DROP TABLE IF EXISTS `quiz_question`;
CREATE TABLE `quiz_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `option_a` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项A',
  `option_b` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项B',
  `option_c` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项C',
  `option_d` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项D',
  `correct_answer` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案(A/B/C/D)',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答案解析',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'general' COMMENT '题目分类',
  `difficulty` int NULL DEFAULT 1 COMMENT '难度等级(1简单 2中等 3困难)',
  `status` int NULL DEFAULT 1 COMMENT '状态(0禁用 1启用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答题闯关题目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quiz_question
-- ----------------------------
INSERT INTO `quiz_question` VALUES (1, '针灸疗法中，\"得气\"是指什么？', '针刺入皮肤', '针刺入穴位后产生的酸、麻、胀、重感', '针刺后出血', '针刺后疼痛', 'B', '得气是针刺后经气产生的感应，表现为酸、麻、胀、重等感觉，是针灸疗效的关键。', '针灸基础', 1, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (2, '人体有多少个正经穴位？', '365个', '152个', '209个', '720个', 'A', '根据中医理论，人体正经穴位共有365个，对应一年365天。', '穴位知识', 1, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (3, '哪个穴位被称为\"长寿穴\"？', '足三里', '合谷穴', '内关穴', '涌泉穴', 'A', '足三里是足阳明胃经的合穴，被称为\"长寿穴\"，常用于保健养生。', '穴位知识', 2, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (4, '针灸治疗失眠常选用哪个穴位？', '太阳穴', '神门穴', '风池穴', '太冲穴', 'B', '神门穴是心经的原穴，具有宁心安神的作用，是治疗失眠的常用穴位。', '穴位应用', 2, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (5, '人体最大的穴位是哪个？', '至阴穴', '足三里', '涌泉穴', '百会穴', 'D', '百会穴位于头顶正中，是人体最大的穴位，也是诸阳之会。', '穴位知识', 1, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (6, '哪个穴位可以治疗晕车晕船？', '内关穴', '外关穴', '中脘穴', '天枢穴', 'A', '内关穴具有和胃降逆、宁心安神的功效，是治疗晕车的要穴。', '穴位应用', 2, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (7, '针灸手法中，\"捻转补泻\"主要作用于什么？', '针的深度', '针的角度', '针的旋转方向和力度', '针的频率', 'C', '捻转补泻是通过针的旋转方向和力度来区分补泻手法的一种方法。', '针灸手法', 2, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (8, '哪个穴位被誉为\"急救穴\"？', '人中穴', '合谷穴', '曲池穴', '列缺穴', 'A', '人中穴（水沟穴）是常用的急救穴位，可用于昏迷、晕厥等急症。', '穴位应用', 1, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (9, '足少阴肾经的井穴是哪个？', '涌泉穴', '太溪穴', '照海穴', '复溜穴', 'A', '涌泉穴是足少阴肾经的井穴，位于足底前部凹陷处。', '穴位知识', 2, 1, '2026-02-03 07:29:57', '2026-02-03 07:29:57');
INSERT INTO `quiz_question` VALUES (11, '九针是哪位神医发明的', '扁鹊', '华佗', '伏羲', '皇甫谧', 'C', '伏羲制九针', 'general', 1, 1, '2026-04-07 19:25:24', '2026-04-16 09:15:36');

-- ----------------------------
-- Table structure for site_visit
-- ----------------------------
DROP TABLE IF EXISTS `site_visit`;
CREATE TABLE `site_visit`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `visit_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 94 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of site_visit
-- ----------------------------
INSERT INTO `site_visit` VALUES (1, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:15');
INSERT INTO `site_visit` VALUES (2, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 19:58:27');
INSERT INTO `site_visit` VALUES (3, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:38');
INSERT INTO `site_visit` VALUES (4, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:38');
INSERT INTO `site_visit` VALUES (5, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:38');
INSERT INTO `site_visit` VALUES (6, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:38');
INSERT INTO `site_visit` VALUES (7, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:42');
INSERT INTO `site_visit` VALUES (8, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:42');
INSERT INTO `site_visit` VALUES (9, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:42');
INSERT INTO `site_visit` VALUES (10, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 19:58:42');
INSERT INTO `site_visit` VALUES (11, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:00:04');
INSERT INTO `site_visit` VALUES (12, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:00:05');
INSERT INTO `site_visit` VALUES (13, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:00:05');
INSERT INTO `site_visit` VALUES (14, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:00:12');
INSERT INTO `site_visit` VALUES (15, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:00:12');
INSERT INTO `site_visit` VALUES (16, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:00:28');
INSERT INTO `site_visit` VALUES (17, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:06:17');
INSERT INTO `site_visit` VALUES (18, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:06:17');
INSERT INTO `site_visit` VALUES (19, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:06:17');
INSERT INTO `site_visit` VALUES (20, '/api/heritage/doctor-story/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:06:20');
INSERT INTO `site_visit` VALUES (21, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:06:25');
INSERT INTO `site_visit` VALUES (22, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:06:36');
INSERT INTO `site_visit` VALUES (23, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:17');
INSERT INTO `site_visit` VALUES (24, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:17');
INSERT INTO `site_visit` VALUES (25, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:17');
INSERT INTO `site_visit` VALUES (26, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:17');
INSERT INTO `site_visit` VALUES (27, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:10:31');
INSERT INTO `site_visit` VALUES (28, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:31');
INSERT INTO `site_visit` VALUES (29, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:31');
INSERT INTO `site_visit` VALUES (30, '/api/heritage/doctor-story/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:33');
INSERT INTO `site_visit` VALUES (31, '/api/heritage/origin-story/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:10:34');
INSERT INTO `site_visit` VALUES (32, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:10:46');
INSERT INTO `site_visit` VALUES (33, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:12:09');
INSERT INTO `site_visit` VALUES (34, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 20:12:09');
INSERT INTO `site_visit` VALUES (35, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:12:09');
INSERT INTO `site_visit` VALUES (36, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:12:09');
INSERT INTO `site_visit` VALUES (37, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:13:04');
INSERT INTO `site_visit` VALUES (38, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:13:05');
INSERT INTO `site_visit` VALUES (39, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:13:05');
INSERT INTO `site_visit` VALUES (40, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:13:19');
INSERT INTO `site_visit` VALUES (41, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:14:23');
INSERT INTO `site_visit` VALUES (42, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:14:23');
INSERT INTO `site_visit` VALUES (43, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:14:23');
INSERT INTO `site_visit` VALUES (44, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:14:43');
INSERT INTO `site_visit` VALUES (45, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:14:47');
INSERT INTO `site_visit` VALUES (46, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:19:26');
INSERT INTO `site_visit` VALUES (47, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:19:28');
INSERT INTO `site_visit` VALUES (48, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:19:52');
INSERT INTO `site_visit` VALUES (49, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:20:00');
INSERT INTO `site_visit` VALUES (50, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:20:00');
INSERT INTO `site_visit` VALUES (51, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 20:20:00');
INSERT INTO `site_visit` VALUES (52, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:20:00');
INSERT INTO `site_visit` VALUES (53, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:24:28');
INSERT INTO `site_visit` VALUES (54, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:24:29');
INSERT INTO `site_visit` VALUES (55, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:24:29');
INSERT INTO `site_visit` VALUES (56, '/api/heritage/illness/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:24:30');
INSERT INTO `site_visit` VALUES (57, '/api/skill/names', 'GET', '127.0.0.1', NULL, '2026-04-06 20:24:30');
INSERT INTO `site_visit` VALUES (58, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:24:33');
INSERT INTO `site_visit` VALUES (59, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:24:33');
INSERT INTO `site_visit` VALUES (60, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:24:45');
INSERT INTO `site_visit` VALUES (61, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:21');
INSERT INTO `site_visit` VALUES (62, '/api/heritage/illness/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:28');
INSERT INTO `site_visit` VALUES (63, '/api/skill/names', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:28');
INSERT INTO `site_visit` VALUES (64, '/api/heritage/zhenjiu-tools/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:29');
INSERT INTO `site_visit` VALUES (65, '/api/heritage/extracourse/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:29');
INSERT INTO `site_visit` VALUES (66, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:28:39');
INSERT INTO `site_visit` VALUES (67, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:47');
INSERT INTO `site_visit` VALUES (68, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:28:58');
INSERT INTO `site_visit` VALUES (69, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:58');
INSERT INTO `site_visit` VALUES (70, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:28:58');
INSERT INTO `site_visit` VALUES (71, '/api/user/level/1', 'GET', '127.0.0.1', NULL, '2026-04-06 20:29:05');
INSERT INTO `site_visit` VALUES (72, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:29:05');
INSERT INTO `site_visit` VALUES (73, '/api/user/backpack', 'GET', '127.0.0.1', NULL, '2026-04-06 20:29:28');
INSERT INTO `site_visit` VALUES (74, '/api/heritage/origin-story/list', 'GET', '127.0.0.1', NULL, '2026-04-06 20:29:29');
INSERT INTO `site_visit` VALUES (75, '/api/user/login', 'POST', '127.0.0.1', NULL, '2026-04-06 20:32:25');
INSERT INTO `site_visit` VALUES (76, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:32:29');
INSERT INTO `site_visit` VALUES (77, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:32:30');
INSERT INTO `site_visit` VALUES (78, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:32:30');
INSERT INTO `site_visit` VALUES (79, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 20:32:30');
INSERT INTO `site_visit` VALUES (80, '/api/user/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:34:39');
INSERT INTO `site_visit` VALUES (81, '/api/files/bussiness/user_avatar/1770112599068.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:34:40');
INSERT INTO `site_visit` VALUES (82, '/api/files/bussiness/user_avatar/1759645666149.jpg', 'GET', '127.0.0.1', NULL, '2026-04-06 20:34:40');
INSERT INTO `site_visit` VALUES (83, '/api/files/bussiness/user_avatar/1770112554663.png', 'GET', '127.0.0.1', NULL, '2026-04-06 20:34:40');
INSERT INTO `site_visit` VALUES (84, '/api/activity/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:37:48');
INSERT INTO `site_visit` VALUES (85, '/api/course/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:37:49');
INSERT INTO `site_visit` VALUES (86, '/api/file/119', 'DELETE', '127.0.0.1', NULL, '2026-04-06 20:37:56');
INSERT INTO `site_visit` VALUES (87, '/api/file/upload', 'POST', '127.0.0.1', NULL, '2026-04-06 20:38:01');
INSERT INTO `site_visit` VALUES (88, '/api/course/CRS-2025-006', 'PUT', '127.0.0.1', NULL, '2026-04-06 20:38:03');
INSERT INTO `site_visit` VALUES (89, '/api/course/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:38:03');
INSERT INTO `site_visit` VALUES (90, '/api/activity/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:38:13');
INSERT INTO `site_visit` VALUES (91, '/api/heritage-item/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:38:14');
INSERT INTO `site_visit` VALUES (92, '/api/heritage-item/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:46:56');
INSERT INTO `site_visit` VALUES (93, '/api/course/page', 'GET', '127.0.0.1', NULL, '2026-04-06 20:47:17');

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills`  (
  `skillid` int NOT NULL,
  `skillname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillbriefdescription` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skilldescription` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillpic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillcategory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillscore` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skilltype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`skillid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES (1, '灸法起源', '艾灸的奇妙诞生', '生活在原始的山洞里的人们，坐在篝火旁取暖，发现温热的火光确实能让身体舒服很多', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', '秘籍残页', '5', '针灸起源');
INSERT INTO `skills` VALUES (2, '针灸针的老祖先', '会治病的石头——砭石', '用来治病的石头，被古人称为“砭石”。它们虽然普通，却是针灸针最早的雏形，是古人对抗病痛的第一个小帮手', '/files/bussiness/skills/1775489483465.png', '秘籍残页', '5', '针灸起源');
INSERT INTO `skills` VALUES (3, ' 伏羲制九针', '一位非常聪明又善良的部落首领，他就是伏羲氏', '伏羲根据不同病症的需要，精心设计了九种形状、大小、用途都不一样的针具，这就是著名的 “九针”。', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', '秘籍残页', '5', '针灸起源');
INSERT INTO `skills` VALUES (4, '针灸学的成长', '针灸学的成长', '针灸并不是一夜之间出现的，它像一棵小树，在一代代中国人的呵护下，慢慢长成了枝繁叶茂的大树。', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', '秘籍残页', '5', '针灸起源');
INSERT INTO `skills` VALUES (5, '马王堆西汉帛书', '一批写在柔软丝织品上的古书', '这些帛书虽然没有后来的针灸典籍完整，却真实记录了西汉早期人们对经络和针灸的认识', 'files/bussiness/activity/屏幕截图 2026-02-03 184254.png', '秘籍残页', '5', '针灸起源');
INSERT INTO `skills` VALUES (6, '神医扁鹊', '神医扁鹊竟然可以令人起死回生', '扁鹊用高超的医术救了无数人，他还把针灸、望闻问切等方法发扬光大，被后世尊为“医学祖师”。', NULL, '秘籍残页', '5', '针灸名医');
INSERT INTO `skills` VALUES (7, '外科圣手华佗', '外科圣手华佗', '不管是头疼脑热，还是腰腿疼痛，只要华佗扎上几针，病人很快就能痊愈。', NULL, '秘籍残页', '5', '针灸名医');
INSERT INTO `skills` VALUES (8, '皇甫谧《针灸甲乙经》', '针灸经典', '他用毕生心血，编写了中国第一部针灸学专著《针灸甲乙经》，被后世尊为“针灸鼻祖”。', '', '秘籍残页', '5', '针灸名医');
INSERT INTO `skills` VALUES (9, '孙思邈', NULL, NULL, NULL, '秘籍残页', '5', '针灸名医');
INSERT INTO `skills` VALUES (10, '王惟一与针灸铜人', NULL, NULL, NULL, '秘籍残页', '5', '针灸名医');
INSERT INTO `skills` VALUES (11, '杨继洲与《针灸大成》', NULL, NULL, NULL, '秘籍残页', '5', '针灸名医');
INSERT INTO `skills` VALUES (12, '经络简介', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (13, '经络与五脏六腑', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (14, '经络的发现', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (15, '经络的超能力', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (16, '经络大家庭', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (17, '手太阴肺经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (18, '手阳明大肠经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (19, '足阳明胃经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (20, '足太阴脾经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (21, '手少阴心经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (22, '足太阳膀胱经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (23, '足少阴肾经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (24, '手厥阴心包经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (25, '手少阳三焦经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (26, '足少阳胆经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (27, '足厥阴肝经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (28, '手太阳小肠经', NULL, NULL, NULL, '能量通路', '5', '经络');
INSERT INTO `skills` VALUES (29, '腧穴简介', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (30, '腧穴家族', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (31, '找腧穴', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (32, '经络的超能力', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (33, '常见腧穴', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (34, '毫针', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (35, '毫针浅刺', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (36, '毫针深刺', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (37, '针刺补法', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (38, '针刺泄法', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (39, '特种针法-火针', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (40, '特种针法-皮肤针法', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (41, '特种针法-水针', NULL, NULL, NULL, '神奇法器', '5', NULL);
INSERT INTO `skills` VALUES (42, '微针疗法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (43, '微针疗法-头针', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (44, '微针疗法-耳针', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (45, '灸法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (46, '灸法材料', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (47, '拔罐法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (48, '拔罐法-火罐法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (49, '拔罐法-走罐法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (50, '砭石法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (51, '刮痧法', '', '', '', '神奇法器', '5', '');
INSERT INTO `skills` VALUES (52, '中府', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (53, '尺泽', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (54, '孔最', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (55, '列缺', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (56, '经渠', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (57, '太渊', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (58, '鱼际', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (59, '少商', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (60, '云门', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (61, '天府', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (62, '阳商', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (63, '合谷', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (64, '手三里', NULL, NULL, NULL, '能量晶石', '5', NULL);
INSERT INTO `skills` VALUES (65, '曲池', NULL, NULL, NULL, '能量晶石', '5', NULL);

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE `sys_file_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件访问路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型(IMG/PDF/TXT/AUDIO/VIDEO)',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `business_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务对象ID(支持数字ID和UUID)',
  `business_field` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务字段名',
  `upload_user_id` bigint NULL DEFAULT NULL COMMENT '上传用户ID',
  `is_temp` tinyint(1) NULL DEFAULT 0 COMMENT '是否临时文件(0:否 1:是)',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态(0:删除 1:正常)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间(临时文件)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_business_field`(`business_type` ASC, `business_id` ASC, `business_field` ASC) USING BTREE,
  INDEX `idx_upload_user`(`upload_user_id` ASC) USING BTREE,
  INDEX `idx_is_temp`(`is_temp` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_file_path`(`file_path` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 129 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件信息表-精简版' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_info
-- ----------------------------
INSERT INTO `sys_file_info` VALUES (1, 'Order_17_2025-08-05.pdf', '/files/bussiness/commom/1754455568488.pdf', 2475, 'PDF', 'POST_CONTENT', '1', NULL, NULL, 0, 1, '2025-08-06 12:46:08', NULL);
INSERT INTO `sys_file_info` VALUES (2, 'Order_17_2025-08-05.pdf', '/files/temp/1754455575039.pdf', 2475, 'PDF', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 12:46:15', '2025-08-07 12:46:15');
INSERT INTO `sys_file_info` VALUES (3, '00ed38237e96399fb9b285e4e084a8cc.jpg', '/files/bussiness/commom/1754457218549.jpg', 110223, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 13:13:39', NULL);
INSERT INTO `sys_file_info` VALUES (4, '00ed38237e96399fb9b285e4e084a8cc.jpg', '/files/bussiness/user_avatar/1754457262346.jpg', 110223, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 13:14:22', NULL);
INSERT INTO `sys_file_info` VALUES (5, 'dfed78e1ed4e5273f58f32e4de64978f.jpg', '/files/bussiness/user_avatar/1754492097639.jpg', 1450982, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 22:54:58', NULL);
INSERT INTO `sys_file_info` VALUES (6, 'dfed78e1ed4e5273f58f32e4de64978f.jpg', '/files/bussiness/user_avatar/1754492222711.jpg', 1450982, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 22:57:03', NULL);
INSERT INTO `sys_file_info` VALUES (7, 'dfed78e1ed4e5273f58f32e4de64978f.jpg', '/files/bussiness/user_avatar/1754492428708.jpg', 1450982, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:00:29', NULL);
INSERT INTO `sys_file_info` VALUES (8, 'ef501eba9cc3b9f54b23ed69042525ab.jpg', '/files/bussiness/user_avatar/1754492449186.jpg', 1529255, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:00:49', NULL);
INSERT INTO `sys_file_info` VALUES (9, 'dfed78e1ed4e5273f58f32e4de64978f.jpg', '/files/bussiness/user_avatar/1754492704318.jpg', 1450982, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:05:04', NULL);
INSERT INTO `sys_file_info` VALUES (10, 'dfed78e1ed4e5273f58f32e4de64978f.jpg', '/files/bussiness/user_avatar/1754492705906.jpg', 1450982, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:05:06', NULL);
INSERT INTO `sys_file_info` VALUES (11, 'ef501eba9cc3b9f54b23ed69042525ab.jpg', '/files/bussiness/user_avatar/1754492751318.jpg', 1529255, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:05:51', NULL);
INSERT INTO `sys_file_info` VALUES (12, 'ef501eba9cc3b9f54b23ed69042525ab.jpg', '/files/bussiness/user_avatar/1754492773457.jpg', 1529255, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:06:13', NULL);
INSERT INTO `sys_file_info` VALUES (13, 'ef501eba9cc3b9f54b23ed69042525ab.jpg', '/files/bussiness/user_avatar/1754493860374.jpg', 1529255, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:24:20', NULL);
INSERT INTO `sys_file_info` VALUES (14, '00ed38237e96399fb9b285e4e084a8cc.jpg', '/files/bussiness/user_avatar/1754493867481.jpg', 110223, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:24:27', NULL);
INSERT INTO `sys_file_info` VALUES (15, '00ed38237e96399fb9b285e4e084a8cc.jpg', '/files/bussiness/user_avatar/1754493907293.jpg', 110223, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:25:07', NULL);
INSERT INTO `sys_file_info` VALUES (16, '00ed38237e96399fb9b285e4e084a8cc.jpg', '/files/bussiness/user_avatar/1754493908326.jpg', 110223, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:25:08', NULL);
INSERT INTO `sys_file_info` VALUES (17, '00ed38237e96399fb9b285e4e084a8cc.jpg', '/files/bussiness/user_avatar/1754493909188.jpg', 110223, 'IMG', 'USER_AVATAR', '1', NULL, NULL, 0, 0, '2025-08-06 23:25:09', NULL);
INSERT INTO `sys_file_info` VALUES (21, '172472bb-5d07-4b3c-b2bf-3517d129faf1.jpg', '/files/bussiness/inheritor/1760421025095.jpg', 283191, 'IMG', 'INHERITOR', 'b9f4f98d-fc80-4dc2-8e42-34aeec6cfc95', 'avatar', 2, 0, 1, '2025-10-14 13:50:25', NULL);
INSERT INTO `sys_file_info` VALUES (22, '屏幕录制 2025-10-13 202510.mp4', '/files/bussiness/course_chapter/1760427875549.mp4', 73573187, 'VIDEO', 'COURSE_CHAPTER', '25', 'video', 2, 0, 0, '2025-10-14 15:44:36', NULL);
INSERT INTO `sys_file_info` VALUES (23, '屏幕录制 2025-10-14 092537.mp4', '/files/bussiness/course_chapter/1760428148233.mp4', 74430819, 'VIDEO', 'COURSE_CHAPTER', '25', 'video', 2, 0, 0, '2025-10-14 15:49:08', NULL);
INSERT INTO `sys_file_info` VALUES (27, '微信图片_20251014211713_24_2.jpg', '/files/bussiness/shop_product/1760504727280.jpg', 125110, 'IMG', 'SHOP_PRODUCT', 'SP2025001', 'cover', 2, 0, 1, '2025-10-15 13:05:27', NULL);
INSERT INTO `sys_file_info` VALUES (29, 'u=1058160184,2964467746&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597694529.jpg', 50541, 'IMG', 'SHOP_PRODUCT', 'SP2025001', 'cover', 2, 0, 1, '2025-10-16 14:54:55', NULL);
INSERT INTO `sys_file_info` VALUES (30, 'u=2307684370,1837460434&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597718913.jpg', 109953, 'IMG', 'SHOP_PRODUCT', 'SP2025001', 'images', 2, 0, 1, '2025-10-16 14:55:19', NULL);
INSERT INTO `sys_file_info` VALUES (31, 'u=1149171703,1570388871&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597720939.jpg', 94718, 'IMG', 'SHOP_PRODUCT', 'SP2025001', 'images', 2, 0, 1, '2025-10-16 14:55:21', NULL);
INSERT INTO `sys_file_info` VALUES (32, 'u=1058160184,2964467746&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597723165.jpg', 50541, 'IMG', 'SHOP_PRODUCT', 'SP2025001', 'images', 2, 0, 1, '2025-10-16 14:55:23', NULL);
INSERT INTO `sys_file_info` VALUES (33, 'u=2015612878,2668435713&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597758961.jpg', 153027, 'IMG', 'SHOP_PRODUCT', 'SP2025002', 'cover', 2, 0, 1, '2025-10-16 14:55:59', NULL);
INSERT INTO `sys_file_info` VALUES (34, 'u=1599688236,2992641828&fm=253&fmt=auto&app=138&f=JPEG.webp', '/files/bussiness/shop_product/1760597762614.webp', 35496, 'IMG', 'SHOP_PRODUCT', 'SP2025002', 'cover', 2, 0, 1, '2025-10-16 14:56:03', NULL);
INSERT INTO `sys_file_info` VALUES (35, 'u=2015612878,2668435713&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597765526.jpg', 153027, 'IMG', 'SHOP_PRODUCT', 'SP2025002', 'images', 2, 0, 1, '2025-10-16 14:56:06', NULL);
INSERT INTO `sys_file_info` VALUES (36, 'u=1875615521,3084869711&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597768150.jpg', 143618, 'IMG', 'SHOP_PRODUCT', 'SP2025002', 'images', 2, 0, 1, '2025-10-16 14:56:08', NULL);
INSERT INTO `sys_file_info` VALUES (37, 'u=1169721685,2026102244&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597771179.jpg', 127215, 'IMG', 'SHOP_PRODUCT', 'SP2025002', 'images', 2, 0, 1, '2025-10-16 14:56:11', NULL);
INSERT INTO `sys_file_info` VALUES (38, 'u=1797272469,1127044554&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597808959.jpg', 64709, 'IMG', 'SHOP_PRODUCT', 'SP2025003', 'cover', 2, 0, 1, '2025-10-16 14:56:49', NULL);
INSERT INTO `sys_file_info` VALUES (39, 'u=372820068,2424521360&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597824982.jpg', 87109, 'IMG', 'SHOP_PRODUCT', 'SP2025003', 'cover', 2, 0, 1, '2025-10-16 14:57:05', NULL);
INSERT INTO `sys_file_info` VALUES (40, 'u=3513569692,3593762330&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597828789.jpg', 53536, 'IMG', 'SHOP_PRODUCT', 'SP2025003', 'images', 2, 0, 1, '2025-10-16 14:57:09', NULL);
INSERT INTO `sys_file_info` VALUES (41, 'u=714462183,2775695629&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597831506.jpg', 75668, 'IMG', 'SHOP_PRODUCT', 'SP2025003', 'images', 2, 0, 1, '2025-10-16 14:57:12', NULL);
INSERT INTO `sys_file_info` VALUES (42, 'u=1797272469,1127044554&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760597833746.jpg', 64709, 'IMG', 'SHOP_PRODUCT', 'SP2025003', 'images', 2, 0, 1, '2025-10-16 14:57:14', NULL);
INSERT INTO `sys_file_info` VALUES (43, '3.png', '/files/bussiness/shop_product/1760597866871.png', 944546, 'IMG', 'SHOP_PRODUCT', 'SP2025004', 'cover', 2, 0, 1, '2025-10-16 14:57:47', NULL);
INSERT INTO `sys_file_info` VALUES (44, '2.png', '/files/bussiness/shop_product/1760597868940.png', 2804646, 'IMG', 'SHOP_PRODUCT', 'SP2025004', 'images', 2, 0, 1, '2025-10-16 14:57:49', NULL);
INSERT INTO `sys_file_info` VALUES (45, '1.png', '/files/bussiness/shop_product/1760597871093.png', 2555651, 'IMG', 'SHOP_PRODUCT', 'SP2025004', 'images', 2, 0, 1, '2025-10-16 14:57:51', NULL);
INSERT INTO `sys_file_info` VALUES (46, '0.png', '/files/bussiness/shop_product/1760597873336.png', 1722011, 'IMG', 'SHOP_PRODUCT', 'SP2025004', 'images', 2, 0, 1, '2025-10-16 14:57:53', NULL);
INSERT INTO `sys_file_info` VALUES (47, 'fdBng0OovlVaQD3pnlIdKoEAVRqFafBCH1qQO5J9wDEAaGLFK2UZX6tD3t_NHVhu.jpg', '/files/bussiness/shop_product/1760597902056.jpg', 781626, 'IMG', 'SHOP_PRODUCT', 'SP2025005', 'cover', 2, 0, 1, '2025-10-16 14:58:22', NULL);
INSERT INTO `sys_file_info` VALUES (48, 'VBmqWPJ7TDttlVqcM2cGtJXR4fdnhlVxKTdxgEKUyvcllHYhj0p-pkM0f8Jv7b4e.jpg', '/files/bussiness/shop_product/1760597903989.jpg', 772943, 'IMG', 'SHOP_PRODUCT', 'SP2025005', 'images', 2, 0, 1, '2025-10-16 14:58:24', NULL);
INSERT INTO `sys_file_info` VALUES (49, '-TDNNK3_vYil8-JXt6kWHhd9RtxaKEWRPkcgXVbf0r05bOnZKOdSCotT5V-jF35h.jpg', '/files/bussiness/shop_product/1760597906139.jpg', 1029666, 'IMG', 'SHOP_PRODUCT', 'SP2025005', 'images', 2, 0, 1, '2025-10-16 14:58:26', NULL);
INSERT INTO `sys_file_info` VALUES (50, 'd6xFNJmYBCdmmCxMvJJzk7KzPEmzEhFvKg0zeblwQIFt8HHXZBGD9g9R-cfMaIzW.jpg', '/files/bussiness/shop_product/1760597908206.jpg', 683277, 'IMG', 'SHOP_PRODUCT', 'SP2025005', 'images', 2, 0, 1, '2025-10-16 14:58:28', NULL);
INSERT INTO `sys_file_info` VALUES (51, 'u=962324951,2247606859&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760597935291.jpg', 104829, 'IMG', 'SHOP_PRODUCT', 'SP2025006', 'cover', 2, 0, 1, '2025-10-16 14:58:55', NULL);
INSERT INTO `sys_file_info` VALUES (52, 'u=2958312170,3525211253&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760597937266.jpg', 53155, 'IMG', 'SHOP_PRODUCT', 'SP2025006', 'images', 2, 0, 1, '2025-10-16 14:58:57', NULL);
INSERT INTO `sys_file_info` VALUES (53, 'u=3900500435,147031880&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760597939288.jpg', 34543, 'IMG', 'SHOP_PRODUCT', 'SP2025006', 'images', 2, 0, 1, '2025-10-16 14:58:59', NULL);
INSERT INTO `sys_file_info` VALUES (54, 'u=707150258,3569977151&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760597942015.jpg', 71558, 'IMG', 'SHOP_PRODUCT', 'SP2025006', 'images', 2, 0, 1, '2025-10-16 14:59:02', NULL);
INSERT INTO `sys_file_info` VALUES (55, '3 (1).png', '/files/bussiness/shop_product/1760597974026.png', 689588, 'IMG', 'SHOP_PRODUCT', 'SP2025007', 'cover', 2, 0, 1, '2025-10-16 14:59:34', NULL);
INSERT INTO `sys_file_info` VALUES (56, '2 (1).png', '/files/bussiness/shop_product/1760597976301.png', 842782, 'IMG', 'SHOP_PRODUCT', 'SP2025007', 'images', 2, 0, 1, '2025-10-16 14:59:36', NULL);
INSERT INTO `sys_file_info` VALUES (57, '1 (1).png', '/files/bussiness/shop_product/1760597978445.png', 1328310, 'IMG', 'SHOP_PRODUCT', 'SP2025007', 'images', 2, 0, 1, '2025-10-16 14:59:38', NULL);
INSERT INTO `sys_file_info` VALUES (58, '0 (1).png', '/files/bussiness/shop_product/1760597980399.png', 827147, 'IMG', 'SHOP_PRODUCT', 'SP2025007', 'images', 2, 0, 1, '2025-10-16 14:59:40', NULL);
INSERT INTO `sys_file_info` VALUES (59, 'u=2347389562,2110877568&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598008801.jpg', 137855, 'IMG', 'SHOP_PRODUCT', 'SP2025008', 'cover', 2, 0, 1, '2025-10-16 15:00:09', NULL);
INSERT INTO `sys_file_info` VALUES (60, 'u=2426535412,3146361557&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598010928.jpg', 170877, 'IMG', 'SHOP_PRODUCT', 'SP2025008', 'images', 2, 0, 1, '2025-10-16 15:00:11', NULL);
INSERT INTO `sys_file_info` VALUES (61, 'u=138349232,1982119062&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598013118.jpg', 160143, 'IMG', 'SHOP_PRODUCT', 'SP2025008', 'images', 2, 0, 1, '2025-10-16 15:00:13', NULL);
INSERT INTO `sys_file_info` VALUES (62, 'u=1037108904,2682126211&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598015070.jpg', 136785, 'IMG', 'SHOP_PRODUCT', 'SP2025008', 'images', 2, 0, 1, '2025-10-16 15:00:15', NULL);
INSERT INTO `sys_file_info` VALUES (63, 'u=3527869794,3824443034&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760598050407.jpg', 66082, 'IMG', 'SHOP_PRODUCT', 'SP2025009', 'cover', 2, 0, 1, '2025-10-16 15:00:50', NULL);
INSERT INTO `sys_file_info` VALUES (64, 'u=4089257857,3836838977&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760598052293.jpg', 65550, 'IMG', 'SHOP_PRODUCT', 'SP2025009', 'images', 2, 0, 1, '2025-10-16 15:00:52', NULL);
INSERT INTO `sys_file_info` VALUES (65, 'u=2643923843,1057721444&fm=224&app=112&f=JPEG.jpg', '/files/bussiness/shop_product/1760598054418.jpg', 51018, 'IMG', 'SHOP_PRODUCT', 'SP2025009', 'images', 2, 0, 1, '2025-10-16 15:00:54', NULL);
INSERT INTO `sys_file_info` VALUES (66, 'u=705049400,564628817&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598097217.jpg', 51039, 'IMG', 'SHOP_PRODUCT', 'SP2025010', 'cover', 2, 0, 1, '2025-10-16 15:01:37', NULL);
INSERT INTO `sys_file_info` VALUES (67, 'u=4127414167,574391162&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598099587.jpg', 40030, 'IMG', 'SHOP_PRODUCT', 'SP2025010', 'images', 2, 0, 1, '2025-10-16 15:01:40', NULL);
INSERT INTO `sys_file_info` VALUES (68, 'u=664695959,3855990336&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598102053.jpg', 49921, 'IMG', 'SHOP_PRODUCT', 'SP2025010', 'images', 2, 0, 1, '2025-10-16 15:01:42', NULL);
INSERT INTO `sys_file_info` VALUES (69, 'u=2877670200,3730110822&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598107549.jpg', 48566, 'IMG', 'SHOP_PRODUCT', 'SP2025010', 'images', 2, 0, 1, '2025-10-16 15:01:48', NULL);
INSERT INTO `sys_file_info` VALUES (70, 'u=1545163734,3388738620&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598135594.jpg', 28674, 'IMG', 'SHOP_PRODUCT', 'SP2025011', 'cover', 2, 0, 1, '2025-10-16 15:02:16', NULL);
INSERT INTO `sys_file_info` VALUES (71, 'u=1470117578,3434681847&fm=3074&app=3074&f=JPEG.jpg', '/files/bussiness/shop_product/1760598137691.jpg', 151370, 'IMG', 'SHOP_PRODUCT', 'SP2025011', 'images', 2, 0, 1, '2025-10-16 15:02:18', NULL);
INSERT INTO `sys_file_info` VALUES (72, 'u=2460199516,2440964382&fm=3074&app=3074&f=JPEG.jpg', '/files/bussiness/shop_product/1760598139852.jpg', 192933, 'IMG', 'SHOP_PRODUCT', 'SP2025011', 'images', 2, 0, 1, '2025-10-16 15:02:20', NULL);
INSERT INTO `sys_file_info` VALUES (73, 'u=588014868,1846841068&fm=3074&app=3074&f=JPEG.jpg', '/files/bussiness/shop_product/1760598142224.jpg', 141064, 'IMG', 'SHOP_PRODUCT', 'SP2025011', 'images', 2, 0, 1, '2025-10-16 15:02:22', NULL);
INSERT INTO `sys_file_info` VALUES (74, '2 (2).png', '/files/bussiness/shop_product/1760598172034.png', 1223737, 'IMG', 'SHOP_PRODUCT', 'SP2025012', 'cover', 2, 0, 1, '2025-10-16 15:02:52', NULL);
INSERT INTO `sys_file_info` VALUES (75, '1 (2).png', '/files/bussiness/shop_product/1760598174318.png', 1069326, 'IMG', 'SHOP_PRODUCT', 'SP2025012', 'images', 2, 0, 1, '2025-10-16 15:02:54', NULL);
INSERT INTO `sys_file_info` VALUES (76, '0 (2).png', '/files/bussiness/shop_product/1760598176456.png', 989101, 'IMG', 'SHOP_PRODUCT', 'SP2025012', 'images', 2, 0, 1, '2025-10-16 15:02:56', NULL);
INSERT INTO `sys_file_info` VALUES (77, '2 (3).png', '/files/bussiness/shop_product/1760598203203.png', 1700160, 'IMG', 'SHOP_PRODUCT', 'SP2025013', 'cover', 2, 0, 1, '2025-10-16 15:03:23', NULL);
INSERT INTO `sys_file_info` VALUES (78, '1 (3).png', '/files/bussiness/shop_product/1760598205306.png', 1940970, 'IMG', 'SHOP_PRODUCT', 'SP2025013', 'images', 2, 0, 1, '2025-10-16 15:03:25', NULL);
INSERT INTO `sys_file_info` VALUES (79, '0 (3).png', '/files/bussiness/shop_product/1760598207280.png', 1830200, 'IMG', 'SHOP_PRODUCT', 'SP2025013', 'images', 2, 0, 1, '2025-10-16 15:03:27', NULL);
INSERT INTO `sys_file_info` VALUES (80, 'u=2662163293,1074052787&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598231579.jpg', 37352, 'IMG', 'SHOP_PRODUCT', 'SP2025014', 'cover', 2, 0, 1, '2025-10-16 15:03:52', NULL);
INSERT INTO `sys_file_info` VALUES (81, 'u=3006659512,203468639&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598233437.jpg', 52556, 'IMG', 'SHOP_PRODUCT', 'SP2025014', 'images', 2, 0, 1, '2025-10-16 15:03:53', NULL);
INSERT INTO `sys_file_info` VALUES (82, 'u=1767503272,2517600863&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598235561.jpg', 48212, 'IMG', 'SHOP_PRODUCT', 'SP2025014', 'images', 2, 0, 1, '2025-10-16 15:03:56', NULL);
INSERT INTO `sys_file_info` VALUES (83, 'u=544359031,1141617650&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598260412.jpg', 83201, 'IMG', 'SHOP_PRODUCT', 'SP2025015', 'cover', 2, 0, 1, '2025-10-16 15:04:20', NULL);
INSERT INTO `sys_file_info` VALUES (84, 'u=3431837821,1464737300&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598263906.jpg', 71164, 'IMG', 'SHOP_PRODUCT', 'SP2025015', 'cover', 2, 0, 1, '2025-10-16 15:04:24', NULL);
INSERT INTO `sys_file_info` VALUES (85, 'u=544359031,1141617650&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598266456.jpg', 83201, 'IMG', 'SHOP_PRODUCT', 'SP2025015', 'images', 2, 0, 1, '2025-10-16 15:04:26', NULL);
INSERT INTO `sys_file_info` VALUES (86, 'u=1658410349,2878783203&fm=253&app=138&f=JPEG.jpg', '/files/bussiness/shop_product/1760598270406.jpg', 69348, 'IMG', 'SHOP_PRODUCT', 'SP2025015', 'images', 2, 0, 1, '2025-10-16 15:04:30', NULL);
INSERT INTO `sys_file_info` VALUES (87, 'e30bed8c0f0b460fa6c805b916703f6d.webp', '/files/bussiness/activity/1760598310097.webp', 60936, 'IMG', 'ACTIVITY', 'ACT-2025-006', 'cover', 2, 0, 1, '2025-10-16 15:05:10', NULL);
INSERT INTO `sys_file_info` VALUES (88, 'fd128c57bddd89bf1b719e7b101e5faf.jpeg', '/files/bussiness/activity/1760598331426.jpeg', 4172534, 'IMG', 'ACTIVITY', 'ACT-2025-005', 'cover', 2, 0, 1, '2025-10-16 15:05:31', NULL);
INSERT INTO `sys_file_info` VALUES (89, 'f01b31be8a06417e9001ebcb64acc4a7.webp', '/files/bussiness/activity/1760598348109.webp', 48468, 'IMG', 'ACTIVITY', 'ACT-2025-004', 'cover', 2, 0, 1, '2025-10-16 15:05:48', NULL);
INSERT INTO `sys_file_info` VALUES (90, '202405141004053462.jpeg', '/files/bussiness/activity/1760598369296.jpeg', 1804381, 'IMG', 'ACTIVITY', 'ACT-2025-001', 'cover', 2, 0, 1, '2025-10-16 15:06:09', NULL);
INSERT INTO `sys_file_info` VALUES (91, '6f072ea964f04262aa07e46ae31d8eb3.webp', '/files/bussiness/activity/1760598388955.webp', 377088, 'IMG', 'ACTIVITY', 'ACT-2025-002', 'cover', 2, 0, 1, '2025-10-16 15:06:29', NULL);
INSERT INTO `sys_file_info` VALUES (92, '37d12f2eb9389b503db8d6378a35e5dde7116e0b.jpg', '/files/bussiness/activity/1760598425660.jpg', 4091273, 'IMG', 'ACTIVITY', 'ACT-2025-003', 'cover', 2, 0, 1, '2025-10-16 15:07:06', NULL);
INSERT INTO `sys_file_info` VALUES (94, 'e86eaf2735635662af8f030fdbae6383.jpeg', '/files/bussiness/inheritor/1760598546765.jpeg', 74306, 'IMG', 'INHERITOR', 'INH-2025-004', 'avatar', 2, 0, 1, '2025-10-16 15:09:07', NULL);
INSERT INTO `sys_file_info` VALUES (95, 'a4d52a0022cfeaf6299ab0663fa669dd.jpeg', '/files/bussiness/inheritor/1760598551991.jpeg', 55551, 'IMG', 'INHERITOR', 'INH-2025-005', 'avatar', 2, 0, 1, '2025-10-16 15:09:12', NULL);
INSERT INTO `sys_file_info` VALUES (96, 'dd662c1ab5c182b2b66c7ce8fc2b798b.jpeg', '/files/bussiness/inheritor/1760598567833.jpeg', 1486586, 'IMG', 'INHERITOR', 'INH-2025-003', 'avatar', 2, 0, 1, '2025-10-16 15:09:28', NULL);
INSERT INTO `sys_file_info` VALUES (97, '11aa3ed9571d3e7bc5f92f8796141416.jpeg', '/files/bussiness/inheritor/1760598584591.jpeg', 11799, 'IMG', 'INHERITOR', 'INH-2025-002', 'avatar', 2, 0, 1, '2025-10-16 15:09:45', NULL);
INSERT INTO `sys_file_info` VALUES (98, '3c406445c21f5e8d8b9622bdbb872b0e.jpeg', '/files/bussiness/inheritor/1760598599968.jpeg', 95508, 'IMG', 'INHERITOR', 'INH-2025-001', 'avatar', 2, 0, 1, '2025-10-16 15:10:00', NULL);
INSERT INTO `sys_file_info` VALUES (99, '3c406445c21f5e8d8b9622bdbb872b0e.jpeg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 95508, 'IMG', 'HERITAGE_ITEM', '4616153a-3092-447d-ad6b-1f7bf3b10da6', 'media', 2, 0, 1, '2025-10-16 15:34:19', NULL);
INSERT INTO `sys_file_info` VALUES (100, '3c406445c21f5e8d8b9622bdbb872b0e.jpeg', '/files/bussiness/heritage_item/1760600076669.jpeg', 95508, 'IMG', 'HERITAGE_ITEM', 'HI-2025-006', 'media', 2, 0, 1, '2025-10-16 15:34:37', NULL);
INSERT INTO `sys_file_info` VALUES (101, '3c406445c21f5e8d8b9622bdbb872b0e.jpeg', '/files/bussiness/heritage_item/1760600084068.jpeg', 95508, 'IMG', 'HERITAGE_ITEM', '205a7e4a-1cf9-41a1-ade9-1b29350eccfd', 'media', 2, 0, 1, '2025-10-16 15:34:44', NULL);
INSERT INTO `sys_file_info` VALUES (102, 'db710bf684c4b3a5fe1010590fe8a108.jpeg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 41111, 'IMG', 'HERITAGE_ITEM', 'HI-2025-006', 'cover', 2, 0, 1, '2025-10-16 16:12:56', NULL);
INSERT INTO `sys_file_info` VALUES (103, 'b3d059f20dbae7c5bd2c84ed10861387.jpeg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 92147, 'IMG', 'HERITAGE_ITEM', 'HI-2025-005', 'cover', 2, 0, 1, '2025-10-16 16:14:46', NULL);
INSERT INTO `sys_file_info` VALUES (104, '2bf20db71f38b180a77d1bc51fc4181e.jpeg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 118389, 'IMG', 'HERITAGE_ITEM', 'HI-2025-004', 'cover', 2, 0, 1, '2025-10-16 16:15:06', NULL);
INSERT INTO `sys_file_info` VALUES (105, 'ce401338cb1dec62812679b57622735d.jpeg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 46994, 'IMG', 'HERITAGE_ITEM', 'HI-2025-003', 'cover', 2, 0, 1, '2025-10-16 16:15:28', NULL);
INSERT INTO `sys_file_info` VALUES (106, '37f9da1e7e9aac050cd1c4a331b535c1.jpeg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 230822, 'IMG', 'HERITAGE_ITEM', 'HI-2025-002', 'cover', 2, 0, 1, '2025-10-16 16:15:49', NULL);
INSERT INTO `sys_file_info` VALUES (107, '8d4143048f738a0babfb6263ca5a6db2.jpg', '/files/bussiness/heritage_item/屏幕截图 2026-02-03 184254.png', 128487, 'IMG', 'HERITAGE_ITEM', 'HI-2025-001', 'cover', 2, 0, 1, '2025-10-16 16:16:15', NULL);
INSERT INTO `sys_file_info` VALUES (111, '青花瓷.mp4', '/files/bussiness/course_chapter/1763348694888.mp4', 13917801, 'VIDEO', 'COURSE_CHAPTER', '29', 'video', 2, 0, 1, '2025-11-17 11:04:55', NULL);
INSERT INTO `sys_file_info` VALUES (113, '699af499fc6fc65e36760be123dbac8b.jpg', '/files/bussiness/course/1763349443267.jpg', 1072148, 'IMG', 'COURSE', 'CRS-2025-004', 'cover', 2, 0, 1, '2025-11-17 11:17:23', NULL);
INSERT INTO `sys_file_info` VALUES (118, '意想不到的针法起源.mp4', '/files/bussiness/course_chapter/1770115297712.mp4', 48421171, 'VIDEO', 'COURSE_CHAPTER', '24', 'video', 2, 0, 1, '2026-02-03 18:41:38', NULL);
INSERT INTO `sys_file_info` VALUES (120, '屏幕截图 2026-02-23 014915.png', '/files/bussiness/heritage_item/1772045689822.png', 123796, 'IMG', 'HERITAGE_ITEM', 'HI-2025-006', 'media', 2, 0, 1, '2026-02-26 02:54:50', NULL);
INSERT INTO `sys_file_info` VALUES (121, 'cat_zhenjiu.png', '/files/bussiness/course/1775479081310.png', 4875981, 'IMG', 'COURSE', 'CRS-2025-006', 'cover', 2, 0, 1, '2026-04-06 20:38:01', NULL);
INSERT INTO `sys_file_info` VALUES (124, 'fengmian7.jpeg', '/files/bussiness/user_avatar/1775639796715.jpeg', 23316, 'IMG', 'USER_AVATAR', '2', 'avatar', 2, 0, 1, '2026-04-08 17:16:37', NULL);
INSERT INTO `sys_file_info` VALUES (125, 'home_cat.png', '/files/bussiness/user_avatar/1775706807440.png', 145102, 'IMG', 'USER_AVATAR', '1', 'avatar', 1, 0, 1, '2026-04-09 11:53:27', NULL);
INSERT INTO `sys_file_info` VALUES (126, '成就2.png', '/files/bussiness/post_content/1776262922843.png', 10385, 'IMG', 'POST_CONTENT', 'post-1776262910097', 'content', 1, 0, 1, '2026-04-15 22:22:03', NULL);
INSERT INTO `sys_file_info` VALUES (127, '成就2.png', '/files/bussiness/user_avatar/1776305185952.png', 10385, 'IMG', 'USER_AVATAR', '4', 'avatar', 4, 0, 1, '2026-04-16 10:06:26', NULL);
INSERT INTO `sys_file_info` VALUES (128, 'fengmian3.png', '/files/bussiness/user_avatar/1776849272099.png', 249782, 'IMG', 'USER_AVATAR', '7', 'avatar', 7, 0, 1, '2026-04-22 17:14:32', NULL);

-- ----------------------------
-- Table structure for traingame
-- ----------------------------
DROP TABLE IF EXISTS `traingame`;
CREATE TABLE `traingame`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `jingluoname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game1brief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game2brief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game3brief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game4brief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `game5brief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of traingame
-- ----------------------------
INSERT INTO `traingame` VALUES (1, '肺经', '肺经1', '肺1', '肺经2', '肺2', '肺经3', '肺3', '肺经4', '肺4', '肺经5', '肺5');
INSERT INTO `traingame` VALUES (2, '胃经', '胃经1', '胃1', '胃经2', '胃2', '胃经3', '胃3', '胃经4', '胃4', '胃经5', '胃5');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密存储)',
  `age` int NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `user_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色code',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:正常)',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `parents` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `score` int NULL DEFAULT NULL COMMENT '用户积分',
  `honor` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'test', '$2a$10$iul6jocLsH.A4gN1QUpgDexDq6KO89syHjUkRD3NbA1L6CTVrNRMO', NULL, '1796145118@qq.com', '15345678119', 'USER', '小米', '/files/bussiness/user_avatar/1775706807440.png', 1, '2025-05-14 10:03:12', '2026-04-23 10:27:24', '1234', '女', 90, '/files/bussiness/user_avatar/1776911244488.jpg');
INSERT INTO `user` VALUES (2, 'admin', '$2a$10$JXCy/159QjA5hJBzy6DYmeOhQSb00nmjeMPJdrfUIUc1HUPYZ98ea', NULL, '123456789@qq.com', '13345678910', 'ADMIN', 'admin', '/files/bussiness/user_avatar/1775639796715.jpeg', 1, '2025-05-14 11:05:08', '2026-04-16 12:09:00', '1234', '女', 55, NULL);
INSERT INTO `user` VALUES (4, 'test1', '$2a$10$WM6WQHi9AwRkORfy9TE8PerZ/vRnkK81WUv1d.3KvKvdEzUqqGTiy', NULL, '1111@qq.com', '13123456789', 'USER', '小明', '/files/bussiness/user_avatar/1776305185952.png', 1, '2025-10-05 14:27:30', '2026-04-16 10:06:31', NULL, '男', 85, '/files/bussiness/user_avatar/1776305158768.jpg');
INSERT INTO `user` VALUES (5, '小明2026', '$2a$10$im0T.PZVEcS9PS32iXEQQ.5LL.sJfKfRLEQCkRoc.yXaDTvzzhybC', NULL, 'mgchenguang557@2925.com', '17864706961', 'USER', NULL, NULL, 1, '2026-04-09 11:03:30', '2026-04-09 11:03:30', NULL, '男', NULL, NULL);
INSERT INTO `user` VALUES (6, 'test2', '$2a$10$ExousWVjaoGY1KEtrn5zi.80OHeGArnUKMWHPeC61dKJ4/aqktMVC', NULL, '3052666353@qq.com', '13458693256', 'USER', '小米', NULL, 1, '2026-04-16 10:09:07', '2026-04-22 17:08:48', NULL, '男', 130, '/files/bussiness/user_avatar/1776310197343.jpg');
INSERT INTO `user` VALUES (7, 'test3', '$2a$10$kKB0stIFOYgFdRJ9FHqyQ.YaHKnUwpeGFhJCmeLtlWvyHVaRTW70a', NULL, 'mgche@2925.com', '17864706961', 'USER', NULL, '/files/bussiness/user_avatar/1776849272099.png', 1, '2026-04-22 17:11:41', '2026-04-22 17:17:05', NULL, '女', 80, '/files/bussiness/user_avatar/1776849424686.jpg');
INSERT INTO `user` VALUES (8, 'test5', '$2a$10$O7g4ceKDrIM.i0ycCSgx5O1K33/e8/yVW1NdkLBFih6HEv6WQZGjC', NULL, 'mgchen@2925.com', '12345678910', 'USER', '小明', NULL, 1, '2026-04-22 17:18:43', '2026-04-22 18:04:47', NULL, NULL, 80, '/files/bussiness/user_avatar/1776852287422.jpg');

-- ----------------------------
-- Table structure for user_achievement
-- ----------------------------
DROP TABLE IF EXISTS `user_achievement`;
CREATE TABLE `user_achievement`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `checkin_days` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `rightcount` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_achievement
-- ----------------------------
INSERT INTO `user_achievement` VALUES (1, 1, '2', '0');
INSERT INTO `user_achievement` VALUES (2, 4, '1', '0');
INSERT INTO `user_achievement` VALUES (3, 6, '2', '0');
INSERT INTO `user_achievement` VALUES (4, 2, '1', '0');
INSERT INTO `user_achievement` VALUES (5, 7, '1', '0');
INSERT INTO `user_achievement` VALUES (6, 8, '1', '0');

-- ----------------------------
-- Table structure for user_backpack
-- ----------------------------
DROP TABLE IF EXISTS `user_backpack`;
CREATE TABLE `user_backpack`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `skill_id` int NOT NULL COMMENT 'skills.skillid',
  `collect_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收集时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_skill`(`user_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 121 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_backpack
-- ----------------------------
INSERT INTO `user_backpack` VALUES (39, 2, 1, '2026-04-16 09:47:50');
INSERT INTO `user_backpack` VALUES (40, 2, 2, '2026-04-16 09:47:59');
INSERT INTO `user_backpack` VALUES (41, 2, 6, '2026-04-16 09:49:17');
INSERT INTO `user_backpack` VALUES (42, 2, 12, '2026-04-16 09:49:36');
INSERT INTO `user_backpack` VALUES (43, 2, 13, '2026-04-16 09:49:39');
INSERT INTO `user_backpack` VALUES (44, 2, 17, '2026-04-16 09:49:45');
INSERT INTO `user_backpack` VALUES (45, 1, 1, '2026-04-16 09:50:46');
INSERT INTO `user_backpack` VALUES (46, 1, 2, '2026-04-16 09:50:52');
INSERT INTO `user_backpack` VALUES (47, 1, 7, '2026-04-16 09:51:27');
INSERT INTO `user_backpack` VALUES (48, 1, 8, '2026-04-16 09:51:33');
INSERT INTO `user_backpack` VALUES (49, 1, 12, '2026-04-16 09:51:47');
INSERT INTO `user_backpack` VALUES (50, 1, 13, '2026-04-16 09:51:50');
INSERT INTO `user_backpack` VALUES (51, 1, 14, '2026-04-16 09:52:38');
INSERT INTO `user_backpack` VALUES (52, 1, 15, '2026-04-16 09:52:40');
INSERT INTO `user_backpack` VALUES (53, 1, 16, '2026-04-16 09:52:42');
INSERT INTO `user_backpack` VALUES (54, 1, 35, '2026-04-16 09:54:13');
INSERT INTO `user_backpack` VALUES (55, 1, 38, '2026-04-16 09:54:16');
INSERT INTO `user_backpack` VALUES (56, 1, 45, '2026-04-16 09:54:22');
INSERT INTO `user_backpack` VALUES (57, 1, 48, '2026-04-16 09:54:30');
INSERT INTO `user_backpack` VALUES (58, 1, 55, '2026-04-16 09:55:10');
INSERT INTO `user_backpack` VALUES (59, 1, 34, '2026-04-16 09:59:35');
INSERT INTO `user_backpack` VALUES (60, 1, 36, '2026-04-16 09:59:38');
INSERT INTO `user_backpack` VALUES (61, 4, 1, '2026-04-16 10:03:55');
INSERT INTO `user_backpack` VALUES (62, 4, 2, '2026-04-16 10:03:59');
INSERT INTO `user_backpack` VALUES (63, 4, 6, '2026-04-16 10:04:08');
INSERT INTO `user_backpack` VALUES (64, 4, 7, '2026-04-16 10:04:13');
INSERT INTO `user_backpack` VALUES (65, 4, 12, '2026-04-16 10:04:19');
INSERT INTO `user_backpack` VALUES (66, 4, 13, '2026-04-16 10:04:21');
INSERT INTO `user_backpack` VALUES (67, 4, 14, '2026-04-16 10:04:26');
INSERT INTO `user_backpack` VALUES (68, 4, 15, '2026-04-16 10:04:27');
INSERT INTO `user_backpack` VALUES (69, 4, 30, '2026-04-16 10:04:31');
INSERT INTO `user_backpack` VALUES (70, 4, 31, '2026-04-16 10:04:33');
INSERT INTO `user_backpack` VALUES (71, 4, 34, '2026-04-16 10:04:36');
INSERT INTO `user_backpack` VALUES (72, 4, 35, '2026-04-16 10:04:39');
INSERT INTO `user_backpack` VALUES (73, 4, 55, '2026-04-16 10:05:02');
INSERT INTO `user_backpack` VALUES (74, 4, 37, '2026-04-16 10:05:54');
INSERT INTO `user_backpack` VALUES (75, 6, 1, '2026-04-16 11:07:29');
INSERT INTO `user_backpack` VALUES (76, 6, 2, '2026-04-16 11:10:26');
INSERT INTO `user_backpack` VALUES (77, 6, 6, '2026-04-16 11:11:23');
INSERT INTO `user_backpack` VALUES (78, 6, 7, '2026-04-16 11:12:03');
INSERT INTO `user_backpack` VALUES (79, 6, 12, '2026-04-16 11:12:31');
INSERT INTO `user_backpack` VALUES (80, 6, 13, '2026-04-16 11:12:33');
INSERT INTO `user_backpack` VALUES (81, 6, 17, '2026-04-16 11:12:48');
INSERT INTO `user_backpack` VALUES (82, 6, 14, '2026-04-16 11:18:19');
INSERT INTO `user_backpack` VALUES (83, 6, 29, '2026-04-16 11:18:58');
INSERT INTO `user_backpack` VALUES (84, 6, 30, '2026-04-16 11:18:59');
INSERT INTO `user_backpack` VALUES (85, 6, 62, '2026-04-16 11:19:15');
INSERT INTO `user_backpack` VALUES (86, 6, 36, '2026-04-16 11:21:06');
INSERT INTO `user_backpack` VALUES (87, 6, 45, '2026-04-16 11:21:39');
INSERT INTO `user_backpack` VALUES (88, 6, 55, '2026-04-16 11:22:21');
INSERT INTO `user_backpack` VALUES (89, 6, 35, '2026-04-16 11:22:46');
INSERT INTO `user_backpack` VALUES (90, 2, 31, '2026-04-16 12:08:58');
INSERT INTO `user_backpack` VALUES (91, 2, 32, '2026-04-16 12:09:00');
INSERT INTO `user_backpack` VALUES (92, 6, 5, '2026-04-22 17:06:56');
INSERT INTO `user_backpack` VALUES (93, 6, 9, '2026-04-22 17:07:04');
INSERT INTO `user_backpack` VALUES (94, 6, 43, '2026-04-22 17:08:00');
INSERT INTO `user_backpack` VALUES (95, 7, 5, '2026-04-22 17:14:55');
INSERT INTO `user_backpack` VALUES (96, 7, 9, '2026-04-22 17:15:03');
INSERT INTO `user_backpack` VALUES (97, 7, 8, '2026-04-22 17:15:09');
INSERT INTO `user_backpack` VALUES (98, 7, 12, '2026-04-22 17:15:15');
INSERT INTO `user_backpack` VALUES (99, 7, 13, '2026-04-22 17:15:16');
INSERT INTO `user_backpack` VALUES (100, 7, 14, '2026-04-22 17:15:23');
INSERT INTO `user_backpack` VALUES (101, 7, 15, '2026-04-22 17:15:24');
INSERT INTO `user_backpack` VALUES (102, 7, 29, '2026-04-22 17:15:28');
INSERT INTO `user_backpack` VALUES (103, 7, 30, '2026-04-22 17:15:30');
INSERT INTO `user_backpack` VALUES (104, 7, 34, '2026-04-22 17:15:34');
INSERT INTO `user_backpack` VALUES (105, 7, 35, '2026-04-22 17:15:38');
INSERT INTO `user_backpack` VALUES (106, 7, 55, '2026-04-22 17:15:55');
INSERT INTO `user_backpack` VALUES (107, 7, 10, '2026-04-22 17:17:01');
INSERT INTO `user_backpack` VALUES (108, 8, 1, '2026-04-22 17:59:33');
INSERT INTO `user_backpack` VALUES (109, 8, 2, '2026-04-22 17:59:43');
INSERT INTO `user_backpack` VALUES (110, 8, 7, '2026-04-22 18:00:01');
INSERT INTO `user_backpack` VALUES (111, 8, 8, '2026-04-22 18:00:06');
INSERT INTO `user_backpack` VALUES (112, 8, 12, '2026-04-22 18:00:16');
INSERT INTO `user_backpack` VALUES (113, 8, 13, '2026-04-22 18:00:18');
INSERT INTO `user_backpack` VALUES (114, 8, 17, '2026-04-22 18:00:38');
INSERT INTO `user_backpack` VALUES (115, 8, 14, '2026-04-22 18:02:20');
INSERT INTO `user_backpack` VALUES (116, 8, 29, '2026-04-22 18:02:28');
INSERT INTO `user_backpack` VALUES (117, 8, 30, '2026-04-22 18:02:29');
INSERT INTO `user_backpack` VALUES (118, 8, 62, '2026-04-22 18:02:39');
INSERT INTO `user_backpack` VALUES (119, 8, 55, '2026-04-22 18:02:46');
INSERT INTO `user_backpack` VALUES (120, 8, 35, '2026-04-22 18:03:11');

-- ----------------------------
-- Table structure for user_checkin
-- ----------------------------
DROP TABLE IF EXISTS `user_checkin`;
CREATE TABLE `user_checkin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `checkin_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_day`(`user_id` ASC, `checkin_date` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户每日签到记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_checkin
-- ----------------------------
INSERT INTO `user_checkin` VALUES (21, 1, '2026-04-16', '2026-04-16 10:02:12');
INSERT INTO `user_checkin` VALUES (22, 4, '2026-04-16', '2026-04-16 10:06:15');
INSERT INTO `user_checkin` VALUES (23, 6, '2026-04-16', '2026-04-16 11:29:45');
INSERT INTO `user_checkin` VALUES (24, 2, '2026-04-16', '2026-04-16 12:08:37');
INSERT INTO `user_checkin` VALUES (25, 6, '2026-04-22', '2026-04-22 17:08:48');
INSERT INTO `user_checkin` VALUES (26, 7, '2026-04-22', '2026-04-22 17:14:46');
INSERT INTO `user_checkin` VALUES (27, 8, '2026-04-22', '2026-04-22 18:04:34');
INSERT INTO `user_checkin` VALUES (28, 1, '2026-04-23', '2026-04-23 10:27:21');

-- ----------------------------
-- Table structure for user_collect
-- ----------------------------
DROP TABLE IF EXISTS `user_collect`;
CREATE TABLE `user_collect`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `userid` int NULL DEFAULT NULL,
  `skillid` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `collectid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `collectid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_collect
-- ----------------------------
INSERT INTO `user_collect` VALUES (3, 1, 3, '2026-04-15 21:16:15');
INSERT INTO `user_collect` VALUES (4, 1, 6, '2026-04-15 21:24:59');
INSERT INTO `user_collect` VALUES (5, 1, 57, '2026-04-15 21:30:34');
INSERT INTO `user_collect` VALUES (6, 2, 6, '2026-04-16 09:49:19');
INSERT INTO `user_collect` VALUES (7, 2, 17, '2026-04-16 09:49:46');
INSERT INTO `user_collect` VALUES (8, 1, 1, '2026-04-16 09:50:48');
INSERT INTO `user_collect` VALUES (9, 1, 2, '2026-04-16 09:50:53');
INSERT INTO `user_collect` VALUES (10, 1, 7, '2026-04-16 09:51:28');
INSERT INTO `user_collect` VALUES (11, 1, 8, '2026-04-16 09:51:33');
INSERT INTO `user_collect` VALUES (12, 4, 1, '2026-04-16 10:03:56');
INSERT INTO `user_collect` VALUES (13, 4, 2, '2026-04-16 10:03:59');
INSERT INTO `user_collect` VALUES (14, 4, 6, '2026-04-16 10:04:09');
INSERT INTO `user_collect` VALUES (15, 4, 7, '2026-04-16 10:04:13');
INSERT INTO `user_collect` VALUES (18, 8, 1, '2026-04-22 17:59:35');
INSERT INTO `user_collect` VALUES (19, 8, 7, '2026-04-22 18:00:02');
INSERT INTO `user_collect` VALUES (20, 8, 17, '2026-04-22 18:00:40');

-- ----------------------------
-- Table structure for user_quiz_record
-- ----------------------------
DROP TABLE IF EXISTS `user_quiz_record`;
CREATE TABLE `user_quiz_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户答案',
  `is_correct` tinyint NULL DEFAULT NULL COMMENT '是否正确(0错误 1正确)',
  `status` int NULL DEFAULT 1 COMMENT '状态(0删除 1正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户答题记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_quiz_record
-- ----------------------------
INSERT INTO `user_quiz_record` VALUES (1, 1, 6, 'A', 1, 0, '2026-04-07 10:25:18');
INSERT INTO `user_quiz_record` VALUES (2, 1, 4, 'B', 1, 0, '2026-04-07 10:25:22');
INSERT INTO `user_quiz_record` VALUES (3, 1, 7, 'A', 0, 0, '2026-04-07 10:25:25');
INSERT INTO `user_quiz_record` VALUES (4, 1, 10, 'C', 0, 0, '2026-04-07 10:25:34');
INSERT INTO `user_quiz_record` VALUES (5, 1, 8, 'B', 0, 0, '2026-04-07 10:25:37');
INSERT INTO `user_quiz_record` VALUES (6, 1, 5, 'A', 0, 0, '2026-04-07 10:25:39');
INSERT INTO `user_quiz_record` VALUES (7, 1, 1, 'D', 0, 0, '2026-04-07 10:25:41');
INSERT INTO `user_quiz_record` VALUES (8, 1, 3, 'B', 0, 0, '2026-04-07 10:25:43');
INSERT INTO `user_quiz_record` VALUES (9, 1, 2, 'C', 0, 0, '2026-04-07 10:25:46');
INSERT INTO `user_quiz_record` VALUES (10, 1, 9, 'D', 0, 0, '2026-04-07 10:25:48');
INSERT INTO `user_quiz_record` VALUES (11, 1, 1, 'B', 1, 0, '2026-04-07 11:45:52');
INSERT INTO `user_quiz_record` VALUES (12, 1, 2, 'C', 0, 0, '2026-04-07 11:45:55');
INSERT INTO `user_quiz_record` VALUES (13, 1, 4, 'D', 0, 0, '2026-04-07 11:45:58');
INSERT INTO `user_quiz_record` VALUES (14, 1, 10, 'C', 0, 0, '2026-04-07 11:46:01');
INSERT INTO `user_quiz_record` VALUES (15, 1, 8, 'A', 1, 0, '2026-04-07 11:46:03');
INSERT INTO `user_quiz_record` VALUES (16, 1, 9, 'C', 0, 0, '2026-04-07 11:46:06');
INSERT INTO `user_quiz_record` VALUES (17, 1, 3, 'B', 0, 0, '2026-04-07 11:46:08');
INSERT INTO `user_quiz_record` VALUES (18, 1, 6, 'D', 0, 0, '2026-04-07 11:46:10');
INSERT INTO `user_quiz_record` VALUES (19, 1, 7, 'D', 0, 0, '2026-04-07 11:46:12');
INSERT INTO `user_quiz_record` VALUES (20, 1, 5, 'C', 0, 0, '2026-04-07 11:46:15');
INSERT INTO `user_quiz_record` VALUES (21, 1, 5, 'A', 0, 0, '2026-04-07 19:14:06');
INSERT INTO `user_quiz_record` VALUES (22, 1, 6, 'A', 1, 0, '2026-04-08 18:22:40');
INSERT INTO `user_quiz_record` VALUES (23, 1, 11, 'B', 1, 0, '2026-04-08 18:22:44');
INSERT INTO `user_quiz_record` VALUES (24, 1, 3, 'B', 0, 0, '2026-04-08 18:22:46');
INSERT INTO `user_quiz_record` VALUES (25, 1, 9, 'A', 1, 0, '2026-04-08 18:22:50');
INSERT INTO `user_quiz_record` VALUES (26, 1, 7, 'B', 0, 0, '2026-04-08 18:22:54');
INSERT INTO `user_quiz_record` VALUES (27, 1, 4, 'C', 0, 0, '2026-04-08 18:22:58');
INSERT INTO `user_quiz_record` VALUES (28, 1, 1, 'B', 1, 0, '2026-04-08 18:23:02');
INSERT INTO `user_quiz_record` VALUES (29, 1, 5, 'B', 0, 0, '2026-04-08 18:23:05');
INSERT INTO `user_quiz_record` VALUES (30, 1, 2, 'A', 1, 0, '2026-04-08 18:23:08');
INSERT INTO `user_quiz_record` VALUES (31, 1, 8, 'A', 1, 0, '2026-04-08 18:23:13');
INSERT INTO `user_quiz_record` VALUES (32, 1, 3, 'C', 0, 0, '2026-04-08 18:23:27');
INSERT INTO `user_quiz_record` VALUES (33, 1, 5, 'D', 1, 0, '2026-04-08 18:23:35');
INSERT INTO `user_quiz_record` VALUES (34, 1, 6, 'A', 1, 0, '2026-04-08 18:23:41');
INSERT INTO `user_quiz_record` VALUES (35, 1, 4, 'B', 1, 0, '2026-04-08 18:23:51');
INSERT INTO `user_quiz_record` VALUES (36, 1, 11, 'B', 1, 0, '2026-04-08 18:23:54');
INSERT INTO `user_quiz_record` VALUES (37, 1, 2, 'A', 1, 0, '2026-04-08 18:23:57');
INSERT INTO `user_quiz_record` VALUES (38, 1, 7, 'C', 1, 0, '2026-04-08 18:24:01');
INSERT INTO `user_quiz_record` VALUES (39, 1, 1, 'B', 1, 0, '2026-04-08 18:24:03');
INSERT INTO `user_quiz_record` VALUES (40, 1, 9, 'A', 1, 0, '2026-04-08 18:24:08');
INSERT INTO `user_quiz_record` VALUES (41, 1, 8, 'A', 1, 0, '2026-04-08 18:24:11');
INSERT INTO `user_quiz_record` VALUES (42, 1, 11, 'A', 0, 0, '2026-04-08 21:20:37');
INSERT INTO `user_quiz_record` VALUES (43, 1, 8, 'A', 1, 0, '2026-04-08 21:20:41');
INSERT INTO `user_quiz_record` VALUES (44, 1, 1, 'B', 1, 0, '2026-04-08 21:20:49');
INSERT INTO `user_quiz_record` VALUES (45, 1, 6, 'A', 1, 0, '2026-04-08 21:20:55');
INSERT INTO `user_quiz_record` VALUES (46, 1, 5, 'C', 0, 0, '2026-04-08 21:21:09');
INSERT INTO `user_quiz_record` VALUES (47, 1, 3, 'D', 0, 0, '2026-04-08 21:21:17');
INSERT INTO `user_quiz_record` VALUES (48, 1, 2, 'A', 1, 0, '2026-04-08 21:21:21');
INSERT INTO `user_quiz_record` VALUES (49, 1, 11, 'B', 1, 0, '2026-04-08 21:21:27');
INSERT INTO `user_quiz_record` VALUES (50, 1, 6, 'A', 1, 0, '2026-04-08 21:21:33');
INSERT INTO `user_quiz_record` VALUES (51, 1, 5, 'B', 0, 0, '2026-04-08 21:21:39');
INSERT INTO `user_quiz_record` VALUES (52, 1, 8, 'A', 1, 0, '2026-04-08 21:21:46');
INSERT INTO `user_quiz_record` VALUES (53, 1, 5, 'D', 1, 0, '2026-04-08 21:21:54');
INSERT INTO `user_quiz_record` VALUES (54, 1, 11, 'B', 1, 0, '2026-04-08 21:21:57');
INSERT INTO `user_quiz_record` VALUES (55, 1, 6, 'A', 1, 0, '2026-04-08 21:22:02');
INSERT INTO `user_quiz_record` VALUES (56, 1, 2, 'A', 1, 0, '2026-04-08 21:22:08');
INSERT INTO `user_quiz_record` VALUES (57, 1, 7, 'C', 1, 0, '2026-04-08 21:22:10');
INSERT INTO `user_quiz_record` VALUES (58, 1, 3, 'A', 1, 0, '2026-04-08 21:22:14');
INSERT INTO `user_quiz_record` VALUES (59, 1, 9, 'A', 1, 0, '2026-04-08 21:22:23');
INSERT INTO `user_quiz_record` VALUES (60, 1, 4, 'B', 1, 0, '2026-04-08 21:22:25');
INSERT INTO `user_quiz_record` VALUES (61, 1, 1, 'B', 1, 0, '2026-04-08 21:22:28');
INSERT INTO `user_quiz_record` VALUES (62, 1, 9, 'A', 1, 0, '2026-04-08 22:28:49');
INSERT INTO `user_quiz_record` VALUES (63, 1, 7, 'C', 1, 0, '2026-04-08 22:28:54');
INSERT INTO `user_quiz_record` VALUES (64, 1, 2, 'A', 1, 0, '2026-04-08 22:28:56');
INSERT INTO `user_quiz_record` VALUES (65, 1, 11, 'B', 1, 0, '2026-04-08 22:29:00');
INSERT INTO `user_quiz_record` VALUES (66, 1, 3, 'A', 1, 0, '2026-04-08 22:29:02');
INSERT INTO `user_quiz_record` VALUES (67, 1, 6, 'A', 1, 0, '2026-04-08 22:29:06');
INSERT INTO `user_quiz_record` VALUES (68, 1, 5, 'B', 0, 0, '2026-04-08 22:29:09');
INSERT INTO `user_quiz_record` VALUES (69, 1, 5, 'D', 1, 0, '2026-04-08 22:29:18');
INSERT INTO `user_quiz_record` VALUES (70, 1, 6, 'A', 1, 0, '2026-04-08 22:29:22');
INSERT INTO `user_quiz_record` VALUES (71, 1, 8, 'A', 1, 0, '2026-04-08 22:29:26');
INSERT INTO `user_quiz_record` VALUES (72, 1, 9, 'A', 1, 0, '2026-04-08 22:29:30');
INSERT INTO `user_quiz_record` VALUES (73, 1, 1, 'B', 1, 0, '2026-04-08 22:29:33');
INSERT INTO `user_quiz_record` VALUES (74, 1, 2, 'A', 1, 0, '2026-04-08 22:29:35');
INSERT INTO `user_quiz_record` VALUES (75, 1, 11, 'B', 1, 0, '2026-04-08 22:29:37');
INSERT INTO `user_quiz_record` VALUES (76, 1, 7, 'C', 1, 0, '2026-04-08 22:29:39');
INSERT INTO `user_quiz_record` VALUES (77, 1, 4, 'B', 1, 0, '2026-04-08 22:29:42');
INSERT INTO `user_quiz_record` VALUES (78, 1, 3, 'A', 1, 0, '2026-04-08 22:29:45');
INSERT INTO `user_quiz_record` VALUES (79, 1, 6, 'A', 1, 0, '2026-04-09 12:04:55');
INSERT INTO `user_quiz_record` VALUES (80, 1, 3, 'A', 1, 0, '2026-04-09 17:26:31');
INSERT INTO `user_quiz_record` VALUES (81, 1, 4, 'A', 0, 0, '2026-04-09 17:26:34');
INSERT INTO `user_quiz_record` VALUES (82, 1, 1, 'B', 1, 0, '2026-04-09 17:26:39');
INSERT INTO `user_quiz_record` VALUES (83, 1, 8, 'A', 1, 0, '2026-04-09 17:26:42');
INSERT INTO `user_quiz_record` VALUES (84, 1, 2, 'A', 1, 0, '2026-04-09 17:26:45');
INSERT INTO `user_quiz_record` VALUES (85, 1, 11, 'B', 1, 0, '2026-04-09 17:26:49');
INSERT INTO `user_quiz_record` VALUES (86, 1, 6, 'B', 0, 0, '2026-04-09 17:26:53');
INSERT INTO `user_quiz_record` VALUES (87, 1, 7, 'C', 1, 0, '2026-04-09 17:26:56');
INSERT INTO `user_quiz_record` VALUES (88, 1, 6, 'A', 1, 0, '2026-04-09 17:27:16');
INSERT INTO `user_quiz_record` VALUES (89, 1, 4, 'B', 1, 0, '2026-04-09 17:27:27');
INSERT INTO `user_quiz_record` VALUES (90, 1, 9, 'B', 0, 0, '2026-04-09 17:28:29');
INSERT INTO `user_quiz_record` VALUES (91, 1, 11, 'A', 0, 0, '2026-04-09 17:28:31');
INSERT INTO `user_quiz_record` VALUES (92, 1, 8, 'D', 0, 0, '2026-04-09 17:28:35');
INSERT INTO `user_quiz_record` VALUES (93, 1, 8, 'A', 1, 0, '2026-04-09 17:50:39');
INSERT INTO `user_quiz_record` VALUES (94, 1, 11, 'B', 1, 0, '2026-04-09 18:00:03');
INSERT INTO `user_quiz_record` VALUES (95, 1, 9, 'A', 1, 0, '2026-04-09 18:00:08');
INSERT INTO `user_quiz_record` VALUES (96, 1, 4, 'A', 0, 0, '2026-04-09 18:00:12');
INSERT INTO `user_quiz_record` VALUES (97, 1, 11, 'B', 1, 0, '2026-04-15 21:20:39');
INSERT INTO `user_quiz_record` VALUES (98, 1, 2, 'A', 1, 0, '2026-04-15 21:20:42');
INSERT INTO `user_quiz_record` VALUES (99, 1, 3, 'A', 1, 0, '2026-04-15 21:20:45');
INSERT INTO `user_quiz_record` VALUES (100, 1, 6, 'A', 1, 0, '2026-04-15 21:20:48');
INSERT INTO `user_quiz_record` VALUES (101, 1, 5, 'C', 0, 0, '2026-04-15 21:20:54');
INSERT INTO `user_quiz_record` VALUES (102, 1, 8, 'A', 1, 0, '2026-04-15 21:20:57');
INSERT INTO `user_quiz_record` VALUES (103, 1, 7, 'C', 1, 0, '2026-04-15 21:21:01');
INSERT INTO `user_quiz_record` VALUES (104, 1, 4, 'B', 1, 0, '2026-04-15 21:21:09');
INSERT INTO `user_quiz_record` VALUES (105, 1, 9, 'A', 1, 0, '2026-04-15 21:21:13');
INSERT INTO `user_quiz_record` VALUES (106, 1, 1, 'B', 1, 0, '2026-04-15 21:21:16');
INSERT INTO `user_quiz_record` VALUES (107, 1, 9, 'A', 1, 0, '2026-04-15 21:21:23');
INSERT INTO `user_quiz_record` VALUES (108, 1, 6, 'A', 1, 0, '2026-04-15 21:21:28');
INSERT INTO `user_quiz_record` VALUES (109, 1, 7, 'C', 1, 0, '2026-04-15 21:21:30');
INSERT INTO `user_quiz_record` VALUES (110, 1, 5, 'D', 1, 0, '2026-04-15 21:21:36');
INSERT INTO `user_quiz_record` VALUES (111, 1, 8, 'A', 1, 0, '2026-04-15 21:21:39');
INSERT INTO `user_quiz_record` VALUES (112, 1, 4, 'B', 1, 0, '2026-04-15 21:21:44');
INSERT INTO `user_quiz_record` VALUES (113, 1, 2, 'A', 1, 0, '2026-04-15 21:21:46');
INSERT INTO `user_quiz_record` VALUES (114, 1, 11, 'B', 1, 0, '2026-04-15 21:21:49');
INSERT INTO `user_quiz_record` VALUES (115, 1, 1, 'B', 1, 0, '2026-04-15 21:21:51');
INSERT INTO `user_quiz_record` VALUES (116, 1, 3, 'A', 1, 0, '2026-04-15 21:21:56');
INSERT INTO `user_quiz_record` VALUES (117, 1, 2, 'B', 0, 0, '2026-04-15 22:21:26');
INSERT INTO `user_quiz_record` VALUES (118, 4, 8, 'A', 1, 0, '2026-04-16 10:05:27');
INSERT INTO `user_quiz_record` VALUES (119, 4, 7, 'B', 0, 0, '2026-04-16 10:05:30');
INSERT INTO `user_quiz_record` VALUES (120, 4, 7, 'C', 1, 0, '2026-04-16 10:05:42');
INSERT INTO `user_quiz_record` VALUES (121, 6, 9, 'A', 1, 0, '2026-04-16 11:24:00');
INSERT INTO `user_quiz_record` VALUES (122, 6, 3, 'D', 0, 0, '2026-04-16 11:24:17');
INSERT INTO `user_quiz_record` VALUES (123, 6, 11, 'C', 1, 0, '2026-04-22 17:08:33');
INSERT INTO `user_quiz_record` VALUES (124, 6, 3, 'A', 1, 0, '2026-04-22 17:09:15');
INSERT INTO `user_quiz_record` VALUES (125, 7, 5, 'C', 0, 0, '2026-04-22 17:16:31');
INSERT INTO `user_quiz_record` VALUES (126, 7, 5, 'D', 1, 0, '2026-04-22 17:16:49');
INSERT INTO `user_quiz_record` VALUES (127, 8, 4, 'B', 1, 0, '2026-04-22 18:04:04');
INSERT INTO `user_quiz_record` VALUES (128, 8, 2, 'B', 0, 0, '2026-04-22 18:04:07');
INSERT INTO `user_quiz_record` VALUES (129, 8, 2, 'A', 1, 0, '2026-04-22 18:04:18');
INSERT INTO `user_quiz_record` VALUES (130, 6, 7, 'A', 0, 0, '2026-04-23 10:28:22');

-- ----------------------------
-- Table structure for user_quiz_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_quiz_stats`;
CREATE TABLE `user_quiz_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_count` int NULL DEFAULT 0 COMMENT '总答题数',
  `correct_count` int NULL DEFAULT 0 COMMENT '正确答题数',
  `last_quiz_time` datetime NULL DEFAULT NULL COMMENT '最近答题时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户答题统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_quiz_stats
-- ----------------------------
INSERT INTO `user_quiz_stats` VALUES (1, 1, 117, 82, '2026-04-15 22:21:26', '2026-04-07 10:25:18', '2026-04-07 10:25:18');
INSERT INTO `user_quiz_stats` VALUES (2, 4, 3, 2, '2026-04-16 10:05:42', '2026-04-16 10:05:27', '2026-04-16 10:05:27');
INSERT INTO `user_quiz_stats` VALUES (3, 6, 5, 3, '2026-04-23 10:28:22', '2026-04-16 11:24:00', '2026-04-16 11:24:00');
INSERT INTO `user_quiz_stats` VALUES (4, 7, 2, 1, '2026-04-22 17:16:49', '2026-04-22 17:16:31', '2026-04-22 17:16:31');
INSERT INTO `user_quiz_stats` VALUES (5, 8, 3, 2, '2026-04-22 18:04:18', '2026-04-22 18:04:04', '2026-04-22 18:04:04');

-- ----------------------------
-- Table structure for user_quize_mistakes
-- ----------------------------
DROP TABLE IF EXISTS `user_quize_mistakes`;
CREATE TABLE `user_quize_mistakes`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `question_id` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_quize_mistakes
-- ----------------------------
INSERT INTO `user_quize_mistakes` VALUES (8, 1, 2, '2026-04-15 22:21:26');
INSERT INTO `user_quize_mistakes` VALUES (13, 6, 7, '2026-04-23 10:28:22');

-- ----------------------------
-- Table structure for xuewei
-- ----------------------------
DROP TABLE IF EXISTS `xuewei`;
CREATE TABLE `xuewei`  (
  `xueweiid` int NOT NULL AUTO_INCREMENT,
  `xueweiname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xueweicatagory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `position` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `illness` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `xueweipic1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillid` int NULL DEFAULT NULL,
  PRIMARY KEY (`xueweiid`) USING BTREE,
  INDEX `xuewei_skillid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `xuewei_skillid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xuewei
-- ----------------------------
INSERT INTO `xuewei` VALUES (1, '中府', '肺经', '在我们胸口旁边，靠近肩膀下方的位置', '咳嗽、气喘、胸口闷闷的时候，按一按这里会舒服很多', NULL, 52);
INSERT INTO `xuewei` VALUES (2, '尺泽', '肺经', '手肘轻轻弯曲，肘横纹外侧的小凹陷里', '喉咙痛、咳嗽或者手肘酸酸软软时，这个穴位特别管用', NULL, 53);
INSERT INTO `xuewei` VALUES (3, '孔最', '肺经', '在手腕到肘弯之间靠上的地方', '咳嗽厉害、流鼻血、嗓子疼时，刺激这里能缓解不适', NULL, 54);
INSERT INTO `xuewei` VALUES (4, '列缺', '肺经', '手腕横纹往上大约一指半的地方', '头痛、脖子僵硬、嗓子干干的，都可以找这个穴位帮忙', NULL, 55);
INSERT INTO `xuewei` VALUES (5, '经渠', '肺经', '手腕横纹上，轻轻一摸能感觉到脉搏跳动的地方', '咳嗽、胸口发闷、呼吸不太顺时可以用它', NULL, 56);
INSERT INTO `xuewei` VALUES (6, '太渊', '肺经', '手腕靠近大拇指一侧，能摸到脉搏的位置', '经常咳嗽、气喘、浑身没力气，按这里会有帮助', NULL, 57);
INSERT INTO `xuewei` VALUES (7, '鱼际', '肺经', '大拇指下面手掌肉肉鼓鼓的中间地方', '嗓子干痛、说话沙哑、一直咳嗽可以按这里', NULL, 58);
INSERT INTO `xuewei` VALUES (8, '少商', '肺经', '大拇指指甲外侧旁边的小角落', '喉咙肿痛、发烧难受、咳嗽不止时特别有用', NULL, 59);
INSERT INTO `xuewei` VALUES (9, '云门', '肺经', '锁骨下方的小凹陷处', '咳嗽、胸口疼、肩膀酸酸的都可以调理', NULL, 60);
INSERT INTO `xuewei` VALUES (10, '天府', '肺经', '手臂内侧，从腋下往下大约三指宽的地方', '咳嗽、流鼻血、上臂酸痛时能派上用场', NULL, 61);
INSERT INTO `xuewei` VALUES (11, '侠白', '肺经', '天府穴再往下一点的手臂内侧', '咳嗽、有点恶心、上臂不舒服时可以按一按', NULL, NULL);
INSERT INTO `xuewei` VALUES (12, '商阳', '大肠经', '食指指甲外侧的小角落', '上火牙痛、喉咙痛、耳朵不舒服时很管用', NULL, 62);
INSERT INTO `xuewei` VALUES (13, '合谷', '大肠经', '在手背虎口那里，就是大拇指和食指两根骨头中间的地方', '头痛、牙痛、感冒发烧、肚子痛和手麻时，这个穴位就发挥大作用了', NULL, 63);
INSERT INTO `xuewei` VALUES (14, '手三里', '大肠经', '手肘弯曲处往下大约两指的位置', '手肘伸不直、牙痛、手臂酸酸的都能改善', NULL, 64);
INSERT INTO `xuewei` VALUES (15, '曲池', '大肠经', '手肘横纹最外侧的小凹陷里', '发烧、喉咙痛、皮肤痒痒、手臂疼都可以用它', NULL, 65);
INSERT INTO `xuewei` VALUES (16, '肩髃', '大肠经', '肩膀平举时前面出现的小凹陷处', '肩膀疼、手臂抬不起来、皮肤发痒很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (17, '迎香', '大肠经', '鼻子两边、鼻唇沟软软的地方', '鼻子堵塞、流鼻涕、闻不到味道时按一按就舒服啦', NULL, NULL);
INSERT INTO `xuewei` VALUES (18, '二间', '大肠经', '食指关节前面靠近手掌的边上', '牙痛、喉咙痛、流鼻血、上火时很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (19, '三间', '大肠经', '食指关节后面的小凹陷处', '肚子胀胀的、牙痛、手指不舒服可以调理', NULL, NULL);
INSERT INTO `xuewei` VALUES (20, '阳溪', '大肠经', '手腕背面外侧的小坑坑里', '头痛、牙痛、手腕疼、喉咙不舒服都能缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (21, '偏历', '大肠经', '手腕背面往上大约三指的地方', '耳朵嗡嗡响、手臂酸痛、脸肿都可以用', NULL, NULL);
INSERT INTO `xuewei` VALUES (22, '温溜', '大肠经', '手腕背面往上大约五指的地方', '头痛、喉咙痛、肩膀后背酸酸的很管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (23, '下廉', '大肠经', '手肘往下大约四指的手臂外侧', '头晕头痛、手肘手臂疼时会舒服很多', NULL, NULL);
INSERT INTO `xuewei` VALUES (24, '上廉', '大肠经', '手肘往下大约三指的手臂外侧', '肩膀手臂疼、肚子胀、肚子痛可以改善', NULL, NULL);
INSERT INTO `xuewei` VALUES (25, '扶突', '大肠经', '脖子侧面和喉结差不多高的地方', '喉咙痛、声音哑、脖子硬硬的很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (26, '口禾髎', '大肠经', '鼻子外侧下方靠近人中的位置', '鼻塞、流鼻血、嘴巴不太舒服都能用', NULL, NULL);
INSERT INTO `xuewei` VALUES (27, '承泣', '胃经', '眼睛正下方眼眶边上的位置', '眼睛红红的、总流眼泪、看东西不舒服可以按这里', NULL, NULL);
INSERT INTO `xuewei` VALUES (28, '四白', '胃经', '眼睛正下方小小的凹陷处', '眼睛痛、眼皮总跳、眼睛干干的特别管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (29, '地仓', '胃经', '嘴角旁边、眼睛直直往下的地方', '总流口水、嘴巴不舒服、眼皮跳可以调理', NULL, NULL);
INSERT INTO `xuewei` VALUES (30, '颊车', '胃经', '下巴角上方，咬紧牙会鼓起一块的地方', '牙痛、嘴巴张不开、脸肿都能缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (31, '头维', '胃经', '额头两侧、发际线拐弯的地方', '头痛、眼睛痛、风一吹就流泪很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (32, '人迎', '胃经', '脖子两侧、喉结旁边的位置', '胸闷咳嗽、喉咙痛、脖子不舒服可以改善', NULL, NULL);
INSERT INTO `xuewei` VALUES (33, '天枢', '胃经', '肚脐旁边大约两指宽的地方', '肚子痛、拉肚子、便秘、肚子胀胀的都管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (34, '归来', '胃经', '肚脐往下四指、再往旁边两指的地方', '小肚子闷闷痛、月经不舒服可以缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (35, '梁丘', '胃经', '膝盖外侧上方大约两指的凹陷处', '膝盖疼、胃痛、小腿不舒服很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (36, '足三里', '胃经', '膝盖下方三指、小腿骨外侧的地方', '消化不良、肚子痛、拉肚子、没力气都靠它', NULL, NULL);
INSERT INTO `xuewei` VALUES (37, '上巨虚', '胃经', '足三里再往下大约三指的地方', '肚子胀、拉肚子、便秘、腿不舒服可以用', NULL, NULL);
INSERT INTO `xuewei` VALUES (38, '丰隆', '胃经', '膝盖到脚踝中间小腿外侧的地方', '咳嗽痰多、头晕、肚子痛、腿麻麻的很管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (39, '内庭', '胃经', '第二根和第三根脚趾缝中间的地方', '牙痛、肚子痛、拉肚子、上火都能缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (40, '隐白', '脾经', '大脚趾指甲内侧旁边的小角落', '肚子胀、拉肚子、睡不好、月经不舒服可以调理', NULL, NULL);
INSERT INTO `xuewei` VALUES (41, '太白', '脾经', '大脚趾根部后面脚掌边上的地方', '身体沉沉的、肚子胀、胃痛拉肚子很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (42, '公孙', '脾经', '大脚趾根部后方骨头旁边的位置', '胃痛、想吐、肚子胀、不消化都能改善', NULL, NULL);
INSERT INTO `xuewei` VALUES (43, '三阴交', '脾经', '内脚踝往上三指、小腿骨后面的地方', '脾胃虚弱、睡不好、月经不调都很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (44, '地机', '脾经', '小腿内侧，阴陵泉往下一点的地方', '肚子痛、拉肚子、腰痛、月经不舒服管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (45, '阴陵泉', '脾经', '小腿内侧膝盖下方的凹陷处', '肚子胀、身体水肿、小便不太顺可以缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (46, '血海', '脾经', '膝盖内侧上方大约两指的地方', '月经痛、皮肤痒痒、湿疹红红的都能用', NULL, NULL);
INSERT INTO `xuewei` VALUES (47, '大横', '脾经', '肚脐旁边大约四指宽的地方', '拉肚子、便秘、小腹痛都可以改善', NULL, NULL);
INSERT INTO `xuewei` VALUES (48, '极泉', '心经', '腋窝最中间的位置', '心慌慌、胸口闷、肩膀手臂麻很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (49, '青灵', '心经', '手肘上方三指手臂内侧的地方', '头痛、肩膀手臂痛、胁肋不舒服可以调理', NULL, NULL);
INSERT INTO `xuewei` VALUES (50, '少海', '心经', '手肘横纹内侧尽头的小凹陷', '心慌、睡不好、手臂麻、脖子痛都管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (51, '灵道', '心经', '手腕横纹往上一指半的地方', '心慌心跳快、说话不太顺、手腕痛可以用', NULL, NULL);
INSERT INTO `xuewei` VALUES (52, '通里', '心经', '手腕横纹往上大约一指的地方', '心慌、头晕、月经不舒服、手臂痛很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (53, '阴郄', '心经', '手腕横纹往上半指的地方', '心慌、夜里出汗多、睡不踏实可以缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (54, '神门', '心经', '手腕横纹内侧靠近小拇指的地方', '失眠、健忘、心慌、总睡不好特别管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (55, '少府', '心经', '手掌心第四五根骨头中间的地方', '心慌、胸口痛、小便不舒服、手指抽筋可以用', NULL, NULL);
INSERT INTO `xuewei` VALUES (56, '少冲', '心经', '小拇指指甲外侧旁边的角落', '发烧、心慌、胸口不舒服、小昏迷很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (57, '少泽', '小肠经', '小拇指指甲外侧旁边的小角落', '发烧、喉咙痛、头痛、奶水不足可以调理', NULL, NULL);
INSERT INTO `xuewei` VALUES (58, '前谷', '小肠经', '小拇指关节前面靠近手掌的边上', '发烧头痛、眼睛红红、喉咙痛都管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (59, '后溪', '小肠经', '小拇指根部后面手掌边上的地方', '脖子硬硬的、手指痛、头痛盗汗很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (60, '腕骨', '小肠经', '手腕背面内侧的小凹陷处', '头痛脖子痛、眼睛不舒服、发烧可以缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (61, '阳谷', '小肠经', '手腕背面内侧靠近小骨头的地方', '头痛头晕、耳朵嗡嗡响、发烧都能用', NULL, NULL);
INSERT INTO `xuewei` VALUES (62, '养老', '小肠经', '手腕内侧小骨头旁边的凹陷处', '眼睛模糊、肩膀痛、腰痛、手臂酸很管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (63, '支正', '小肠经', '手腕往上大约五指的地方', '脖子僵硬、手指痛、头痛睡不好可以改善', NULL, NULL);
INSERT INTO `xuewei` VALUES (64, '小海', '小肠经', '手肘后方内侧的凹陷处', '手肘痛、手臂痛、耳朵不舒服很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (65, '肩贞', '小肠经', '肩膀后方腋下纹往上一指的地方', '肩膀痛、手臂抬不起来、脖子痛管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (66, '臑俞', '小肠经', '肩膀后方肩胛骨下方的凹陷处', '肩膀手臂痛、不舒服可以缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (67, '天宗', '小肠经', '肩胛骨中间圆圆的凹陷处', '肩胛骨痛、肩膀痛、气喘都很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (68, '秉风', '小肠经', '肩胛骨上方的凹陷处', '肩膀痛、手臂酸麻、抬不起来可以改善', NULL, NULL);
INSERT INTO `xuewei` VALUES (69, '曲垣', '小肠经', '肩胛骨内侧上方的凹陷处', '肩膀痛、肩胛骨不舒服很适合', NULL, NULL);
INSERT INTO `xuewei` VALUES (70, '肩外俞', '小肠经', '后背上部脊柱旁边三指的地方', '肩膀痛、后背痛、脖子僵硬管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (71, '肩中俞', '小肠经', '脖子下方脊柱旁边两指的地方', '咳嗽、气喘、肩膀后背痛可以缓解', NULL, NULL);
INSERT INTO `xuewei` VALUES (72, '天窗', '小肠经', '脖子侧面喉结高度的后方', '耳朵嗡嗡响、喉咙痛、脖子硬硬的很有用', NULL, NULL);
INSERT INTO `xuewei` VALUES (73, '天容', '小肠经', '脖子侧面下颌角后方的凹陷处', '耳朵响、喉咙痛、脖子不舒服管用', NULL, NULL);
INSERT INTO `xuewei` VALUES (74, '颧髎', '小肠经', '眼睛外角下方颧骨下面的地方', '脸痛、牙痛、眼皮跳、嘴巴不舒服可以用', NULL, NULL);
INSERT INTO `xuewei` VALUES (75, '听宫', '小肠经', '耳朵前面张嘴巴会凹进去的地方', '耳朵嗡嗡响、听不清、喉咙痛都很适合', NULL, NULL);

-- ----------------------------
-- Table structure for zhenjiutools
-- ----------------------------
DROP TABLE IF EXISTS `zhenjiutools`;
CREATE TABLE `zhenjiutools`  (
  `toolsid` int NOT NULL AUTO_INCREMENT,
  `toolsname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolspic1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolspic2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolspic3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolsbrief` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolstitle1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolstitle2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolstitle3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolstext1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolstext2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `toolstext3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `skillid` int NULL DEFAULT NULL,
  PRIMARY KEY (`toolsid`) USING BTREE,
  INDEX `zhenjiutools_skillid`(`skillid` ASC) USING BTREE,
  CONSTRAINT `zhenjiutools_skillid` FOREIGN KEY (`skillid`) REFERENCES `skills` (`skillid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of zhenjiutools
-- ----------------------------
INSERT INTO `zhenjiutools` VALUES (1, '毫针', NULL, NULL, NULL, '毫针是中医针灸最常用的细针，细细长长，用来扎穴位帮助身体恢复健康。', '针的样子和材料', '扎针的基本用法', '适合的小毛病', '毫针大多用不锈钢做，不容易生锈，又细又光滑。它分为针尖、针身、针柄等部分，尖尖的方便轻轻扎进皮肤。', '医生会找准人体穴位，把毫针扎进去，再轻轻转动，让身体产生酸酸胀胀的感觉，这种感觉叫“得气”。', '毫针可以帮助缓解头痛、肚子痛、脖子酸、睡不着等很多不舒服的情况，是很安全的小针具。', 34);
INSERT INTO `zhenjiutools` VALUES (2, '毫针浅刺', NULL, NULL, NULL, '毫针浅刺就是把细针扎得很浅，只在皮肤表层，轻轻刺激，特别温和。', '扎得非常浅', '适合小朋友和老人', '主要调理小问题', '浅刺只扎进皮肤一点点，不会扎到肌肉深处，进针又轻又快，几乎不疼。', '这种方法刺激很小，适合小朋友、体质弱的人和脸上、皮肤薄的地方使用。', '可以缓解皮肤痒、轻微麻木、小范围疼痛，帮助疏通身体表面的气血，让人舒服一些。', 35);
INSERT INTO `zhenjiutools` VALUES (3, '毫针深刺', NULL, NULL, NULL, '毫针深刺是把针扎得深一些，到达肌肉里面，针对更深的不舒服。', '扎针位置更深', '适合较明显的疼痛', '需要医生专业操作', '深刺会扎进肌肉层甚至更深的地方，刺激更强，能作用到身体内部的问题。', '常用于腰酸、腿痛、关节疼等比较深、比较重的不舒服，效果更明显。', '深刺对手法要求高，必须由专业医生找准位置，不能自己随便尝试，保证安全。', 36);
INSERT INTO `zhenjiutools` VALUES (4, '针刺补法', NULL, NULL, NULL, '针刺补法是用轻柔的针法帮身体“补充能量”，让虚弱的人更有精神。', '手法轻轻柔柔', '适合身体虚弱的人', '作用是扶正变强', '补法动作慢、力度轻，慢慢捻针，不强烈刺激，像给身体轻轻打气。', '经常累、没力气、爱出汗、体质差的人，适合用补法调理。', '补法可以帮助身体恢复正气，增强抵抗力，让人慢慢变得更有精神、更健康。', 37);
INSERT INTO `zhenjiutools` VALUES (5, '针刺泻法', NULL, NULL, NULL, '针刺泻法是用稍强的针法把身体里多余的“火气、邪气”排出去。', '手法稍快稍强', '适合上火、胀痛', '作用是排邪通畅', '泻法进针快、捻转幅度大，刺激比补法明显，用来疏通堵塞。', '嗓子疼、上火、肚子胀、疼痛厉害、不通畅的情况常用泻法。', '泻法可以把不好的邪气排出去，让气血顺畅，胀痛和不舒服很快减轻。', 38);
INSERT INTO `zhenjiutools` VALUES (6, '特种针法-火针', NULL, NULL, NULL, '火针是把针烧热后快速扎进穴位的针法，暖暖的，能驱寒止痛。', '针会先加热', '能赶走寒气', '要专业医生操作', '医生把特制的粗针在火上烧红，然后快速扎进穴位，速度非常快。', '特别适合怕冷、关节凉、肚子冷痛的人，热量能进到身体里驱寒。', '火针温度高，必须由经验丰富的医生操作，扎得准又快，不会烫伤皮肤。', 39);
INSERT INTO `zhenjiutools` VALUES (7, '特种针法-皮肤针法', NULL, NULL, NULL, '皮肤针法是用小针轻轻敲打皮肤，不深深扎入，像轻轻按摩一样。', '像小锤子敲皮肤', '刺激面积更广', '适合放松调理', '工具头上有很多小细针，轻轻敲打皮肤表面，不会扎很深。', '不是扎一个点，而是敲一片区域，让局部皮肤微微发红。', '可以缓解疲劳、头痛、腰酸，让身体放松，小朋友也比较容易接受。', 40);
INSERT INTO `zhenjiutools` VALUES (8, '特种针法-水针', NULL, NULL, NULL, '水针也叫穴位注射，把少量药水打到穴位里，针药一起起作用。', '打针加穴位', '作用更持久', '适用多种小毛病', '用很小的针头，把安全的药水注射到穴位里，结合打针和针灸。', '药水留在穴位里慢慢发挥作用，效果比普通扎针更久。', '常用于腰腿痛、关节不舒服、咳嗽等，见效比较快。', 41);
INSERT INTO `zhenjiutools` VALUES (9, '微针疗法', NULL, NULL, NULL, '微针疗法是在身体很小的区域、特殊部位用针调理，精准又方便。', '针对小区域治疗', '刺激小很安全', '通过小区域调全身', '不像普通扎针那样全身取穴，只在耳朵、头等小地方治疗。', '用针更细更轻，刺激很小，大人小孩都比较适合。', '中医认为耳朵、头能对应全身，扎这些小地方就能调理整个身体。', 42);
INSERT INTO `zhenjiutools` VALUES (10, '微针疗法-头针', NULL, NULL, NULL, '头针是在头皮上扎针，用来调理和大脑、神经相关的不舒服。', '在头皮上扎针', '帮助大脑和神经', '扎针很轻很快', '按照头皮上的特定区域扎针，位置都在头发下面的头皮上。', '对头晕、反应慢、肢体活动不舒服等情况有帮助。', '头针操作很快，刺激轻柔，不会很疼，配合活动效果更好。', 43);
INSERT INTO `zhenjiutools` VALUES (11, '微针疗法-耳针', '/files/bussiness/zhenfa/1776267448776.png', '', '', '耳针是在耳朵上扎针或贴小珠子，耳朵像倒过来的小人，对应全身。', '耳朵对应全身', '可以贴小珠子', '方便又好用', '中医认为耳朵像一个倒立的宝宝，每个位置对应身体不同器官。', '不扎针也可以用小胶布贴磁珠在耳朵上，按压就能起作用。', '适合调理失眠、胃口不好、压力大，平时上课、走路都不影响。', 44);

SET FOREIGN_KEY_CHECKS = 1;

# 儿科针灸教育系统（小铜人中医侦探社）项目分析报告

**项目根目录**：`D:\总\Pediatric Acupuncture Education System\pingtai1`
**系统名称**：《"针"相只有一个！小铜人中医侦探社》——面向 5–8 岁儿童的针灸文化科普互动平台（明确非医疗诊疗系统）

---

## 一、数据库全貌

### 1.1 heritage_db.sql（历史主库导出，154KB，1314 行）

Navicat 导出的 MySQL 8.0.33 全量备份（`heritage_db`，utf8mb4，导出时间 2026-04-23），共 **27 张表**，按表归类如下：

| # | 表名 | 用途 | 关键字段 | 数据量 |
|---|------|------|---------|--------|
| 1 | `badge` | 用户徽章 | id, user_id, badgepath, badgename | 11 条 |
| 2 | `community_post` | 针灸答疑社区帖子（分享/公告/反馈） | user_id, title, content, like_count, collect_count, comment_count, postcatagory, status(0正常/1删除/2举报), posttype, postpic1-5 | 7 条 |
| 3 | `doctorstory` | 名医故事 | doctorid, doctorname, doctorbrief, doctordetail(longtext), skillid(FK→skills), doctorpic1-3, media, previewpic, **readingglossary**(重点词词库 JSON) | 6 条 |
| 4 | `extracourse` | 课外延伸课程 | extracourseid, extracoursename, brief, des, icon, pic1-3, skillid(FK) | 7 条 |
| 5 | `illness` | 病症关卡 | illnessid, cowtown(所属小镇), illnessname, illnessfeature, xueweicount, toolscount, xuewei1-5, tools1-4, illnesspic, illnessbadgename/path | 3 条 |
| 6 | `jingluo` | 经络 | jingluoid, jingluoname, skillid(FK), jingluocatagory, jingluoorder(走向), illness(主治), jingluopic | 12 条 |
| 7 | `misconception` | 常见误区问答 | id, question, answer, sort_order | 2 条 |
| 8 | `originstory` | 针灸起源故事 | originstoryID, storytitle, storysubtitle, storytext, storypic1-3, media, skillid(FK) | 5 条 |
| 9 | `post_collect` | 帖子收藏 | post_id+user_id 唯一键 | 1 条 |
| 10 | `post_comment` | 帖子评论 | post_id, user_id, content | 4 条 |
| 11 | `post_like` | 帖子点赞 | post_id+user_id 唯一键 | 1 条 |
| 12 | `post_report` | 帖子举报 | post_id, reporter_user_id, reason, status(0待处理/1已处理/2驳回) | 1 条 |
| 13 | `quiz_question` | 答题闯关题库 | title, option_a-d, correct_answer, explanation, category(针灸基础/穴位知识/穴位应用/针灸手法/general), difficulty(1-3), status | 10 条 |
| 14 | `site_visit` | 站点访问日志 | path, method, ip, user_id, visit_time | 93 条 |
| 15 | `skills` | 技能/知识点总表（核心内容枢纽） | skillid, skillname, brief, description, skillpic, skillcategory(**秘籍残页/能量通路/能量晶石/神奇法器**), skillscore(5), skilltype(针灸起源/针灸名医/经络等) | **65 条** |
| 16 | `sys_file_info` | 文件信息表（精简版） | original_name, file_path, file_size, file_type(IMG/PDF/VIDEO), business_type(POST_CONTENT/USER_AVATAR/SHOP_PRODUCT/COURSE/ACTIVITY/HERITAGE_ITEM 等), business_id, is_temp, status | 128 条 |
| 17 | `traingame` | 训练小游戏 | id, jingluoname(肺经/胃经), game1-5 + game1brief-5brief | 2 条 |
| 18 | `user` | 用户表（BCrypt 密码） | username(唯一), password, age, email(唯一), phone, user_type(USER/ADMIN), name, avatar, status, parents, sex, score(积分), honor(证书) | 7 条 |
| 19 | `user_achievement` | 成就统计 | user_id, checkin_days, rightcount | 6 条 |
| 20 | `user_backpack` | 用户技能收集背包 | user_id+skill_id 唯一键, collect_time | 82 条 |
| 21 | `user_checkin` | 每日签到 | user_id+checkin_date 唯一键 | 8 条 |
| 22 | `user_collect` | 技能收藏 | userid, skillid(FK) | 18 条 |
| 23 | `user_quiz_record` | 用户答题记录 | user_id, question_id, user_answer, is_correct, status, create_time | 130 条 |
| 24 | `user_quiz_stats` | 用户答题统计（每用户一行） | total_count, correct_count, last_quiz_time | 5 条 |
| 25 | `user_quize_mistakes` | 用户错题本 | user_id, question_id | 2 条 |
| 26 | `xuewei` | 儿童版穴位知识库 | xueweiid, xueweiname, xueweicatagory(肺经/大肠经/胃经/脾经/心经/小肠经), position(儿童化表述), illness, skillid(FK) | **75 条** |
| 27 | `zhenjiutools` | 针灸工具 | toolsid, toolsname(毫针/浅刺/深刺/补法/泻法/火针/皮肤针/水针/微针/头针/耳针), toolspic1-3, toolsbrief, toolstitle1-3, toolstext1-3, skillid(FK) | 11 条 |

**要点**：
- 表内嵌了两次 `ALTER TABLE doctorstory ADD COLUMN`（`previewpic`、`readingglossary`），属一次性手工迁移，正式迁移见 `sql/doctorstory_readingglossary_migration.sql`。
- `doctorstory` 的 6 条名医故事：扁鹊（起死回生）、华佗（麻沸散/外科）、皇甫谧（《针灸甲乙经》）、孙思邈（同身寸取穴法）、王惟一（针灸铜人 354 穴）、杨继洲（《针灸大成》），每条均带 `readingglossary` 重点词 JSON（词 + 拼音 + 释义）。
- `originstory` 5 条：艾灸起源（篝火）、砭石（会治病的石头）、伏羲制九针、针灸学成长史、马王堆帛书（《足臂十一脉灸经》《阴阳十一脉灸经》）。
- `skills` 是内容枢纽：skillid 1–5 针灸起源、6–11 针灸名医、12–28 经络（12 经脉 + 经络简介等）、29–33 腧穴、34–51 针灸针法/灸法/拔罐/砭石/刮痧、52–65 具体穴位（中府/尺泽/合谷/曲池等），被 doctorstory、extracourse、jingluo、xuewei、zhenjiutools、user_backpack、user_collect 外键引用。
- `site_visit` 记录暴露了完整 API 面：`/api/heritage/doctor-story|origin-story|illness|zhenjiu-tools|extracourse|jingluo`、`/api/user/backpack|page|login|level`、`/api/skill/names`、`/api/course`、`/api/activity`、`/api/heritage-item`、`/api/file/upload` 等。
- **注意**：本导出不含管理端 CMS 表（course、activity、heritage_item、shop_product、inheritor 等），但这些业务类型出现在 `sys_file_info` 中，说明管理端数据表不在本 dump 内；`quiz_question` 记录中出现 id=10 的引用但该行未导出。

### 1.2 schema.sql（Spring Boot 启动建表，145 行，10 张"小铜人"新表）

`CREATE TABLE IF NOT EXISTS`，全部 utf8mb4_unicode_ci，均为 3D 小铜人馆新功能表：

| 表名 | 用途 |
|------|------|
| `acupoint_knowledge` | 3D 小铜人儿童穴位知识库（code 主键，如 LU-1；含标准定位、儿童文案、安全提示、3D 坐标、GB/T 12346-2021 来源） |
| `user_acupoint_daily_progress` | 用户每日铜人探案进度（user+date+acupoint 唯一） |
| `user_copper_man_profile` | 用户小铜人馆材料与成长档案（copper_tokens 铜片、star_sand 星砂、completed_cases） |
| `user_agency_archive` | 用户侦探社修复档案（user+archive_item_id） |
| `user_game_reward` | 用户小游戏每日奖励领取记录（user+game_code+reward_date 唯一） |
| `copper_acupoint_revision` | 小铜人穴位内容修订（版本化 + 审核/发布状态机） |
| `copper_story_revision` | 小铜人探案故事修订 |
| `copper_content_release` | 小铜人内容发布与站内推送 |
| `user_content_release_read` | 用户内容推送已读记录 |
| `user_copper_story_progress` | 用户小铜人故事进度（progress_json） |

### 1.3 data.sql（种子数据，由脚本生成）

- 头部注释明确：**361 个穴位全部录入**，其中 **100 个具备 3D 坐标 + GB/T 12346-2021 教学元数据**，261 个仅为"图鉴星"（无 3D 坐标）。
- 每条 INSERT 使用 `ON DUPLICATE KEY UPDATE` 幂等写入，字段含：code、point_number、name、pinyin、meridian_code/name/english、model_id（如 p1、ig1）、body_area（手腕部/前臂与肘部/肩臂部/头面与颈部等）、standard_location（国家标准定位）、child_location（儿童观察提示，如"转动3D小铜人，看向胸部。找到'中府星'的发光点"）、child_description（文化记忆线索）、safety_tip（固定"只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长"）、position_x/y/z、sort_order。
- 末尾追加 `copper_acupoint_revision` 的 v1 全量发布种子（SELECT ... WHERE NOT EXISTS）。

### 1.4 production-seed-cleanup.sql（生产净化脚本，42 行）

- 关闭外键检查后 **TRUNCATE 17 张表**：post_collect、post_comment、post_like、post_report、community_post、badge、user_achievement、user_backpack、user_checkin、user_collect、user_quiz_record、user_quiz_stats、user_quize_mistakes、quiz_question、sys_file_info、site_visit、user —— 即清空全部演示用户、联系方式、社区帖与上传记录。
- 重新插入 **10 道安全向题目**（安全学习/文化观察/隐私保护/空间观察/文化历史/身体认知/游戏规则分类），主题全部围绕"只观察不针刺、身体不适找成人、保护隐私、铜人历史文化"，保证题库健康。
- 创建 `app_migration` 表并写入 `production_seed_sanitized_v1` 标记，供部署脚本校验"生产净化已完成"。

---

## 二、数据库迁移机制（Flyway，`db/migration/`）

| 版本 | 文件名 | 内容 |
|------|--------|------|
| **V1** | `V1__write_consistency_core.sql` | **写入一致性核心 8 张表**：`write_operation`（幂等写入记录，user+operation_key 唯一 + 请求哈希）、`audit_event`（业务写入审计）、`auth_session`（可撤销登录会话）、`outbox_event`（事务外发事件，PENDING/重试）、`user_task_progress`（游戏任务进度，含 version 乐观锁）、`reward_ledger`（积分与材料奖励流水，grant_key 幂等）、`user_material_balance`（材料余额 + version）、`user_game_migration`（旧客户端状态迁移记录） |
| **V2** | `V2__agency_archive_persistence.sql` | 仅 1 张表：`user_agency_archive`（侦探社修复档案持久化） |
| **V3** | `V3__mainline_config_revision.sql` | **主线关卡配置版本化 5 张表**：`game_config_revision`、`game_level_revision`、`game_task_revision`、`game_task_reward_revision`、`game_level_prerequisite_revision`；给 `user_task_progress` 加 `config_version` 列；并**种子化 xinglin-mainline v1 主线**——8 个关卡（报到处→安全守护案→失踪竹简案→身体地图追踪案→经络星河密令→铜人档案室→星光修补册→侦探社修复计划，含地图坐标/路由/印章）、12 个任务（4 主线 + 4 每日 + 安全问答 + 经络小火车等）、15 条奖励配置（材料如 bamboo-slip-shard 竹简碎片、apricot-kernel 杏核、acupoint-star-pearl 穴位星珠、copper-token 铜片、safety-bell 安全铃铛、mugwort-floss 艾绒、meridian-star-sand 经络星砂、star-compass 星盘）、7 条前置依赖链 |
| **V4** | `V4__copper_content_center.sql` | **小铜人内容中心 6 张表**（acupoint_knowledge、copper_acupoint_revision、copper_story_revision、copper_content_release、user_content_release_read、user_copper_story_progress）；种子：从 acupoint_knowledge 幂等发布 361 条穴位 v1 修订；**4 个完整探案故事**（`missing-bamboo` 失踪竹简案、`exam-copper-man` 会考试的小铜人、`broken-star-river` 星河断线案、`silent-safety-bell` 安全铃铛失声案），每故事含 pages 场景、clues 线索、reasoning 推理题、safety 安全题、rewards 奖励的完整 JSON；并向 `copper_content_release` 生成 STORY 推送记录（路由 `/doctor-story?story=xxx`） |

另有一份非 Flyway 的迁移脚本 `sql/doctorstory_readingglossary_migration.sql`（95 行）：用 information_schema 探测 + PREPARE/EXECUTE 动态 SQL 实现**幂等加列**（`previewpic`、`readingglossary`），并给 6 条名医故事回填预览封面 `/doctor-story-preview/N.jpg` 与**净化版**重点词词库（释义改写为"本馆只介绍历史和文化，不教授操作"等安全表述）。

**机制总结**：V1–V4 由 Flyway 在 Spring Boot 启动时按版本号顺序执行（docker 环境 `SQL_INIT_MODE=always`），全部使用 `CREATE TABLE IF NOT EXISTS`/`INSERT ... ON DUPLICATE KEY UPDATE`/`WHERE NOT EXISTS` 保证幂等；V3/V4 内置种子数据，V4 依赖 V3 之前已存在的 schema.sql 中的 acupoint_knowledge（说明 schema.sql + data.sql 与 Flyway 配合完成初始化）。

---

## 三、Docker 部署架构（docker-compose.yml，138 行）

项目名 `pediatric-acupuncture`，单内部桥接网络 `internal`，**5 个服务**：

| 服务 | 镜像/构建 | 端口映射 | 数据卷 | 健康检查 | 备注 |
|------|-----------|----------|--------|----------|------|
| `mysql` | `mysql:8.4` | 无对外端口 | `mysql_data:/var/lib/mysql` + 两个只读初始化脚本：`runtime/heritage-public.sql`（净化版内容）→`001-heritage.sql`、`production-seed-cleanup.sql`→`002-production-sanitize.sql` | mysqladmin ping（10s/20 次） | utf8mb4、时区 +08:00；首次启动自动导入内容 + 执行净化 |
| `redis` | `redis:7.4-alpine` | 无对外 | `redis_data:/data` | redis-cli ping | appendonly 开启、requirepass |
| `backend` | 构建 `./springboot`（Dockerfile） | 无对外 | `./runtime/files:/app/files`、`./logs:/app/logs` | TCP 探测 8889（20s/10 次） | 以 `APP_UID:APP_GID`（默认 1000）非 root 运行 |
| `frontend` | 构建 `./vue3`（Nginx） | **`${HTTP_PORT:-80}:80`**（唯一对外端口） | 无 | 依赖 backend healthy | Nginx 静态托管 + 反代 |
| `neo4j` | `neo4j:5.26-community` | 无对外 | `neo4j_data`、`neo4j_logs` | wget 7474 | **profile: `knowledge-graph`，默认不启动** |

**backend 环境变量**（全部来自 .env）：`SPRING_PROFILES_ACTIVE=prod`、`DB_URL/DB_USERNAME/DB_PASSWORD`（jdbc:mysql://mysql:3306/heritage_db，allowMultiQueries + allowPublicKeyRetrieval）、`REDIS_HOST/PORT/PASSWORD`、`JWT_SECRET`、`JWT_EXPIRATION`(默认 86400000=1 天)、`JWT_REFRESH_EXPIRATION`(604800000=7 天)、`BASIC_AUTH_PASSWORD`、SMTP 邮件（默认 smtp.qq.com:465 SSL）、`ADMIN_BOOTSTRAP_USERNAME/EMAIL/PASSWORD`（生产管理员引导）、`DEEPSEEK_API_KEY`（可选 AI 助手）、`NEO4J_ENABLED`（默认 false）、`SWAGGER_ENABLED/KNIFE4J_ENABLED`（默认关闭，KNIFE4J 需密码）、`SQL_INIT_MODE=always`、`FILE_UPLOAD_PATH=/app/files`、`JAVA_OPTS`(-Xms256m -Xmx1024m)。

**安全设计**：MySQL/Redis/后端/Neo4j 均不暴露宿主机端口，只有前端 80 端口对外；数据库命名卷持久化。

**.env.example（36 行）**：所有敏感项（MYSQL_PASSWORD、MYSQL_ROOT_PASSWORD、REDIS_PASSWORD、JWT_SECRET、BASIC_AUTH_PASSWORD、KNIFE4J_PASSWORD、ADMIN_BOOTSTRAP_PASSWORD 等）均为 `CHANGE_ME` 占位，要求替换为强随机值（推荐 `openssl rand -base64 36`）；`.env` 不入库不提交。

---

## 四、部署 / 启动流程要点

### 4.1 deploy.sh（生产部署，59 行）
1. **前置校验**：`.env` 必须存在；不允许残留 `CHANGE_ME`；强制校验 8 个必填键（MYSQL_PASSWORD、MYSQL_ROOT_PASSWORD、REDIS_PASSWORD、JWT_SECRET、ADMIN_BOOTSTRAP_* 三个、MAIL_USERNAME、MAIL_PASSWORD）；APP_UID/GID 必须为数字。
2. **准备运行时目录**：`runtime/files/bussiness`、`runtime/files/video`、`logs`。
3. **生成生产内容包** `runtime/heritage-public.sql`：用 awk 从 heritage_db.sql 中**剔除 17 张用户/社区/文件表的 INSERT**（user、user_backpack、badge、community_post、site_visit、sys_file_info 等），只保留文化内容；从 `springboot/files/bussiness/` 拷贝 8 类素材目录（extracourse/illness/inheritor/jingluo/media/shuxue/skills/start/zhenfa）到 runtime/files，视频同理，避免演示头像/帖子图进入生产。
4. **权限修正**：`chown APP_UID:APP_GID runtime/files logs`；`docker compose config --quiet` 校验配置。
5. **安全门禁**：先只启动 mysql+redis；在容器内查 `app_migration` 中 `production_seed_sanitized_v1`（hex 编码 0x70726f64756374696f6e5f736565645f73616e6974697a65645f7631）标记——**标记缺失则立即停止，拒绝启动应用**，要求人工备份审计旧数据卷（防止对真实生产用户执行破坏性清库）。
6. 通过后 `docker compose up -d --build` 构建启动全部服务并 `docker compose ps` 展示状态。

### 4.2 backup.sh（备份，11 行）
- 时间戳目录 `backups/YYYYmmdd-HHMMSS/`；`mysqldump --single-transaction --routines --triggers` 全库导出 `heritage_db.sql`；`tar -czf files.tar.gz -C runtime files` 打包上传文件；提示定期同步到异机/对象存储。

### 4.3 start.ps1（本地开发一键启动，540 行）
1. 参数：`-SkipBuild`、`-NoKill`；UTF-8 输出；日志统一入 `logs/`（backend/frontend/maven-build/mysql 各 .log 与 .err.log）。
2. 端口工具：Test-PortFree/Open、Clear-Port（netstat + Get-NetTCPConnection 双法找占用 PID 并强制释放 8889/8800）。
3. **检查 Java 17+** → **启动 MySQL**（优先 Windows 服务，其次按候选路径找 mysqld.exe 直接后台拉起，等待 3306 就绪，含进程提前退出诊断）。
4. 检查 npm/node → 释放端口 → **Maven 构建后端**（`mvn -DskipTests package`，maven 仓库隔离在 `.m2\repository`，日志入 maven-build.log）。
5. 启动后端：`java -jar target/springboot-0.0.1-SNAPSHOT.jar --spring.sql.init.mode=always`（:8889，等待 45s）。
6. **前端依赖自检**：用 node 执行 `require('./node_modules/vite/node_modules/esbuild').transformSync(...)` 验证 esbuild 二进制可用（防止复制/升级导致的损坏）；失败则按 package-lock.json 用 `npm ci`（无锁则 install）修复并复检。
7. 启动 Vite dev server：`vite --mode development --host 127.0.0.1 --port 8800 --strictPort`（:8800，等待 30s）。
8. 成功提示：前端 `http://127.0.0.1:8800/#/home-map`、后端 `:8889`、Swagger `:8889/swagger-ui.html`、默认管理员 admin/123456；自动打开浏览器。
9. **进程守护**：while 循环监控前后端进程，任一退出即打印最后 20 行日志；finally 中清理子进程。

### 4.4 start.bat 与 一键启动全部服务.bat
- **start.bat**（16 行）：chcp 65001，检查 start.ps1 存在后以 `-ExecutionPolicy Bypass` 调用，失败 pause。
- **一键启动全部服务.bat**（39 行）：先 netstat 探测 3306/8889/8800 三个端口是否全部 LISTENING——**全在则直接打开 `http://127.0.0.1:8800/index.html#/home-map` 退出**；否则调用 start.ps1 启动三件套，失败提示查看 logs 文件夹。

---

## 五、项目文档核心内容

### 5.1 PROJECTS_REQUIREMENTS.md（项目需求，55 行，全文要点）
- **定位**：儿童针灸智能科普互动平台《"针"相只有一个！小铜人中医侦探社》；**明确非医疗系统**，只做文化科普、身体区域认知、经络穴位启蒙、安全教育；**禁止提供**针刺深度/进针角度/操作手法/治疗处方/诊疗建议。
- **目标用户**：儿童、家长、教师、中医药文化科普活动组织者。
- **核心学习路径**：故事导入 → 身体认知 → 经络探索 → 铜人互动 → 安全闯关 → 线索奖励 → 星光修补。
- **11 个功能模块**：小铜人侦探社首页、侦探任务地图、针灸故事馆、身体小地图、经络线索图、3D 小铜人互动馆、安全小课堂、线索包、侦探社基地、侦探徽章墙、星光修补册。
- **第一版技术栈**：Vue3 + Vite + TypeScript、Vue Router、Pinia（进度/线索/徽章/修补册）、Tailwind CSS、Three.js（3D 铜人）、首版用本地 mock 数据与 `graphTriples.ts` 模拟知识图谱（不连 Neo4j）。
- **界面风格**：儿童友好、温暖明亮、卡片式、大按钮、少文字多图标多任务引导、每页有安全提示、不展示危险操作。
- **安全边界**：所有页面必须体现"本平台仅用于中医药文化科普和身体认知学习，不提供真实针刺指导，不替代医生诊疗。"

### 5.2 部署说明.md（107 行）
- 服务器建议 Ubuntu 22.04/24.04、4 核 8GB/60GB；安全组先开 22/80，配 HTTPS 后开 443；目录 `/opt/pediatric-acupuncture`。
- 步骤：装 Docker（docker.io + compose-v2）→ clone + `cp .env.example .env` + 替换 CHANGE_ME（openssl 生成强随机）→ `./deploy.sh` 首次启动（自动生成无用户数据的内容包、净化校验、写入标记、创建唯一生产管理员）→ 用 ADMIN_BOOTSTRAP 账号登录并立即改密。
- 更新：`git pull && ./deploy.sh`；老数据卷无净化标记时部署会安全停止，需备份审计。
- 备份 `./backup.sh` → `backups/日期时间/`；Neo4j 可选 `docker compose --profile knowledge-graph up -d --build`；HTTPS 后续配置；**禁止对外开放 3306/6379/7474/7687/8889**；**严禁 `docker compose down -v`**（会删库卷）。

### 5.3 四份 design-qa 评审文档

| 文档 | 评审对象 | 关注点 | 结论 |
|------|----------|--------|------|
| **design-qa.md**（35 行） | 3D 小铜人观察界面（前/侧/后三视图） | **图片保真度**：实现直接用源图无损裁切（前视图 475×940 与源图逐像素一致），Three.js 仅作透明交互/热区层；正面/侧面/背面按钮切换、OrbitControls 相机角度分类（左侧镜像右侧）、缩放保真；对比历史：早期程序化几何体无法还原面部/手/足/青铜材质（P1）→ 修复为直接贴源图资产 | **passed** |
| **design-qa-agency.md**（41 行） | 侦探社修复计划页（/agency） | 桌面 1706×960 + 移动 390×844 双端对比：顶部牌匾、左侧线索盒、中央侦探社场景、右侧进度/任务面板、底部操作栏；移动端纵向堆叠、材料盒单列；页面用真实项目素材（羊皮纸地图、侦探社建筑、Font Awesome 图标）；遗留 P3：中央建筑图与参考图室内修复场景非同一原画 | **passed**（含 1 个 P3 待办） |
| **design-qa-doctor-story.md**（60 行） | 针灸故事馆（/doctor-story） | 1668×941 三栏信息架构（章节卡/主视觉+简介/任务+奖励+安全提醒）、暖色纸张背景、绿色标题、阅读进度 35%、阅读弹层（dialog 语义 + Esc + 回车提交）、移动端 390×844 入口重排；修复了进度百分比、Teleport 弹层 CSS 变量透明背景、主视觉加小铜人素材、移动端首屏遮挡；遗留 P3：中央插画非参考图同一原画 | **passed**（含 1 个 P3 待办） |
| **design-qa-homepage-layout.md**（35 行） | 首页布局（任务地图 + 右侧栏） | 1920×1000 无溢出；修复历史：P1 任务卡内容 835px > 可视 754px 裁掉主 CTA → 改为可收缩轨道；P1 右侧栏 1154px 把修补计划挤出首屏 → 减密度至 858px；P2 引导地图 cover 裁边 → fit 全图；1366×768 / 1024×768 断点均无横向溢出；`npm run lint / build / validate:game` 全通过 | **passed** |

四份评审共同点：均以"源视觉真相 vs 实现截图"逐像素/几何对比，全部 **passed**，P0/P1/P2 全部清零，仅各留 1 个 P3 级"原画一致性"待办（等待图像生成服务恢复后替换横幅/场景资产）。

### 5.4 springboot/HELP.md
Spring Boot 3.4.1 脚手架自带的 Maven/Spring 参考文档模板（Spring Data JDBC、Spring Security、OAuth2、LDAP 等官方链接），无项目定制内容。

---

## 六、三个工具脚本

### 6.1 scripts/generate-copper-man-seed.mjs（生成种子，178 行）
- **输入**：`vue3/public/assets/copper-man/data/` 下 3 个前端数据文件——`acupoints-data.js`（361 穴目录）、`modeled-acupoint-locations.js`（100 穴 3D 教学元数据 + GB/T 12346-2021 来源）、`acupoint-positions.js`（3D 坐标，按 modelId 关联），用 `node:vm` 在沙箱 window 上下文里加载。
- **校验**：强制 361 个穴位、100 个建模穴、code 无重复、bodyArea 分类完备（否则抛错终止）。
- **逻辑**：正则把 view/standard 映射为 8 类身体区域（手腕部/前臂与肘部/肩臂部/踝足部/髋腿部/腹部/胸部/头面与颈部）；用确定性伪随机（code+name 字符码和取模）从 12 套"建模文案模板"与 8 套"图鉴文案模板"中挑选文化记忆线索（星光路牌/文化车站/地图坐标/经络小旗/星河门牌/探案印章等比喻）；无 3D 坐标的穴位生成"住在图鉴里"文案。
- **输出**：`springboot/src/main/resources/data.sql`——361 条 `INSERT ... ON DUPLICATE KEY UPDATE`（幂等）+ 文件头注释（儿童文案排除治疗宣称与针刺指导）+ `copper_acupoint_revision` v1 发布种子（SELECT ... WHERE NOT EXISTS）。

### 6.2 scripts/validate-copper-content.mjs（校验器，36 行）
- **输入**：data.sql + V4 迁移 SQL。
- **断言**：恰 361 条 INSERT（100 条含"转动3D小铜人"=3D 就绪，261 条含"还没有3D坐标"=仅图鉴）；每条必须含固定安全提醒"只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长"；儿童文案**禁止**出现危险词（按揉/按摩/刺激穴位/针刺/扎针/治疗/治愈/疗效/自己取穴）；4 个故事 code 全在且每个含 pages/clues/reasoning/safety/rewards 五段。
- **输出**：通过时打印 "Copper content validation passed: 361 acupoints (100 3D-ready + 261 atlas-only), 4 complete stories."，任一断言失败抛错。

### 6.3 scripts/build-copper-content-docx.py（文档生成器，582 行）
- **输入**：解析 data.sql 的 361 条 INSERT（自写 SQL 值解析器，处理转义引号）；内置 4 个故事的完整元数据；项目吉祥物图 `vue3/src/assets/copper-detective-2026/detective-guide.png`。
- **输出**：`output/3d-copper-content/小铜人内容中心_穴位与故事内容汇编.docx`（python-docx 生成，带"杏林品牌"视觉体系：深青 1F5A50 / 金 B47A2E / 米白 FFF8E8，Noto Sans SC）。
- **结构**：①封面（361 穴位文化星 / 100 3D 已就绪 / 4 完整故事三格指标 + 永久安全边界说明，适龄 5–8 岁）；②内容中心总览（专业层/儿童层/3D 规则三层内容分层、儿童写作四规则、草稿→待审核→已审核→发布→归档五步工作流、管理端 vs 儿童端功能对照）；③首批 4 个探案故事详解（场景表/线索/推理题/安全题/奖励）；④**361 条穴位清单横向附录**（按经络分组，编号/名称拼音/身体区域/专业定位+来源/儿童观察提示/文化记忆线索/状态"3D可探案 or 仅图鉴"，无坐标行浅色底纹）；⑤来源与使用说明。

---

## 七、其他目录检查结果

| 项目 | 结果 |
|------|------|
| `vue-cert-poster-master/` | **非空**（35 项）：第三方组件库源码 `vue-cert-poster`（证书/海报生成 Vue 组件，含 packages/cert-poster、lib/ 预构建 UMD/CommonJS、examples、README/LICENSE/yarn.lock）——属引用/备份的第三方库 |
| `vue3(1)/` | **非空**（43,997 项）：是主前端 `vue3` 的**完整重复副本**（`vue3(1)\vue3` 内含 src、dist、node_modules、Dockerfile、package.json、vite.config.js、.env 系列与启动脚本），带 .idea 项目文件，应为历史备份/冗余目录，非实际运行目录 |
| 根目录 README* | **不存在**（glob/pwsh 均无匹配） |

---

## 八、整体架构总结

```
┌─ 儿童端 Vue3 (vite:8800 本地 / Nginx:80 生产) ─ Three.js 3D小铜人 + Pinia + Tailwind
│         │ REST /api
┌────────▼────────┐   ┌──────────┐   ┌─────────┐
│ SpringBoot :8889 │──▶│ MySQL 8.4 │──▶│ Redis 7 │
│ (JWT/Basic认证,   │   │ heritage_db│   │ 会话/缓存│
│  Flyway V1-V4,    │   │ 27旧表+新表 │   └─────────┘
│  SQL_INIT=always) │   └──────────┘
└────────┬────────┘   （可选 Neo4j 知识图谱 profile: knowledge-graph）
         │ 上传文件 runtime/files（挂载卷）
```

- **三层数据体系**：① 历史主库 heritage_db（27 表，内容+用户+社区，全量导出）；② 管理端 CMS 表（course/activity/heritage_item 等，存在于运行库但未在本 dump）；③ 小铜人新体系（schema.sql/data.sql + Flyway V1–V4：写入一致性、任务/奖励/材料、主线关卡配置、内容中心 361 穴 + 4 故事）。
- **安全红线贯穿全链路**：需求层（非医疗定位）→ 内容层（穴位文案禁治疗词、固定安全提醒、净化题库）→ 数据层（production-seed-cleanup 清演示数据 + app_migration 标记门禁）→ 部署层（deploy.sh 校验标记、内容包剥离用户数据、内网隔离端口）→ 前端层（每页安全提示、儿童叙事"只观察不操作"）。
- **两套启动路径**：Windows 本地开发（start.ps1：Java+MySQL+npm+Maven+Vite 全自动拉起与进程守护）与 Linux 生产（deploy.sh + docker compose，可重复幂等部署，备份 backup.sh）。

# 儿科针灸教育平台（小铜人中医侦探社）后端技术报告

> 项目根目录：`D:\总\Pediatric Acupuncture Education System\pingtai1`
> 源码根：`springboot\src\main\java\org\example\springboot\`（共 **265 个 .java 文件**，已全部读取覆盖）
> 数据库：MySQL（库名 `heritage_db`）；定位：面向儿童的"中医文化 + 经络穴位安全认知"游戏化教育平台。
> 报告范围：技术栈与依赖、安全与鉴权、完整 API 端点清单（35 个 Controller 约 160 端点）、数据库实体清单（27 实体 + 29 Mapper）、核心业务逻辑（45 服务）、数据初始化机制、文件上传、风险点。

---

## 1. 技术栈与依赖

### 1.1 构建与运行环境

| 项 | 值 |
|---|---|
| 构建工具 | Maven（`spring-boot-maven-plugin`） |
| Spring Boot | **3.5.14**（`spring-boot-starter-parent`，Java **17**） |
| 应用坐标 | `org.example:springboot:0.0.1-SNAPSHOT` |
| 服务端口 | **8889**，`context-path: /` |
| 启动类 | `SpringbootApplication`：`@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)` + `@EnableScheduling`（**排除默认 UserDetailsService**，认证完全走 JWT 过滤器） |

### 1.2 依赖清单（pom.xml）

**基础 Starter**：`spring-boot-starter-web`、`data-jdbc`、`jdbc`、`security`、`websocket`、`validation`、`aop`、`mail`、`test`、`security-test`。

**数据库 / ORM / 迁移**：
- `com.mysql:mysql-connector-j`（runtime，MySQL 驱动）
- `com.baomidou:mybatis-plus:3.5.7` + `mybatis-plus-spring-boot3-starter:3.5.7`（MyBatis-Plus 3.5.7，无 XML Mapper，SQL 全注解/Java 代码）
- `org.flywaydb:flyway-core` + `flyway-mysql`（**版本化数据库迁移，生产权威**）
- `com.h2database:h2`（仅 test）
- `org.neo4j.driver:neo4j-java-driver`（Neo4j 图库驱动，`@ConditionalOnProperty(neo4j.enabled=true)` 才注册）

**安全 / 鉴权**：`com.auth0:java-jwt:4.4.0`（JWT，HMAC256）、`spring-security`、`jakarta.servlet-api:6.0.0`(provided)。

**业务 / 工具**：`cn.hutool:hutool-all:5.8.25`、`com.alibaba:fastjson2:2.0.45`、`com.alipay.sdk:alipay-sdk-java:4.39.185.ALL`（支付宝 SDK，当前无实际调用）、`knife4j-openapi3-jakarta-spring-boot-starter:4.3.0`、`spring-boot-starter-data-redis`（依赖存在，**RedisTemplate Bean 已配置但全库未实际使用**）、`jakarta.persistence-api`（仅 API）、`lombok`。

**关键配置值（application.yml）**：
- 数据源：`jdbc:mysql://localhost:3306/heritage_db?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true`，环境变量 `DB_URL/DB_USERNAME/DB_PASSWORD` 覆盖。
- `sql.init.mode: ${SQL_INIT_MODE:never}`（默认关，开发兼容旧表）；Flyway `enabled: ${FLYWAY_ENABLED:true}`、`baseline-on-migrate: true`、`baseline-version: 0`、`validate-on-migrate: true`、`clean-disabled: true`。
- JWT：`secret` 默认 `local-development-secret-change-before-production-2026`（**≥32 字符，否则启动抛异常**）；`expiration: 86400000`（24h）；`refresh-expiration: 604800000`（7d）；header `Authorization`、前缀 `Bearer `。
- 文件：`file.upload.path: ./files`、`maxSize: 100MB`；multipart 上限 100MB。
- AI：`ai.deepseek.api-key: ${DEEPSEEK_API_KEY:}`、endpoint `https://api.deepseek.com/v1/chat/completions`。
- 邮件：QQ SMTP（465/SSL/TLSv1.2）。
- MyBatis-Plus：`mapper-locations: classpath:/mapper/*.xml`（**该目录实际不存在**）、`map-underscore-to-camel-case: true`、`log-impl` 默认 NoLoggingImpl。
- SpringDoc/Knife4j：默认 `SWAGGER_ENABLED=false`、`KNIFE4J_ENABLED=false`（生产强制关闭）。
- Jackson：`default-property-inclusion: non_empty`；`LocalDateTimeConfig` 统一 `yyyy-MM-dd HH:mm:ss` 序列化/反序列化。

**application-prod.yml 差异**：数据源/JWT 密钥全部必填环境变量；`sql.init.mode` 固定 `never`；关闭文档；新增 `bootstrap.admin.{username,email,password}`（管理员引导账号，密码≥12 位）。

---

## 2. 安全与鉴权

### 2.1 认证链路（无状态 JWT）

1. **JWT 生成（`util/JwtTokenUtils`）**：`HMAC256(secret)` 签名；claims：`userId`、`username`、`roleType`、`jwtId(tokenId)`、`expiresAt`、`issuedAt`；issuer 固定 `xinglin-detective-platform`；密钥长度 <32 直接启动失败；过期时间来自 `jwt.expiration`（默认 24h）。注释"7 天"与实际 24h 不符（文档瑕疵）。
2. **请求过滤（`config/JwtAuthenticationFilter`，`OncePerRequestFilter`）**：仅接受 `Authorization: Bearer <token>`；`verifyToken` 校验签名+issuer → 取 `userId` → 过期检查 → **会话有效性检查**（`AuthSessionService.isActive(tokenId)`，查 `auth_session` 表：`revoked_at IS NULL AND expires_at > NOW()`）→ `UserService.getUserById` 验证用户仍存在且 `status==NORMAL` → 写入 SecurityContext（authority=`ROLE_<userType>`）与请求属性 `currentUser/currentUserId/currentTokenId` → `authSessionService.touch(tokenId)` 刷新 `last_seen_at`。任何校验失败只 `clearContext()` 并放行（由 SecurityConfig 决定 401/403）。
3. **会话管理（`AuthSessionService`）**：登录时 `create`（写 `auth_session` 行 + 审计 LOGIN）；登出 `revoke`（置 `revoked_at` + 审计 LOGOUT）；`isActive` 对空 tokenId 返回 true（宽松）。**会话可撤销**——JWT 本身无状态，靠 DB 会话表实现主动吊销。
4. **当前用户读取**：Controller 统一用 `JwtTokenUtils.getCurrentUserId()/getCurrentUser()/getCurrentTokenId()/isAdmin()`（从请求属性读取，非解析 token）。

### 2.2 授权规则（`config/SecurityConfig`）

- `csrf.disable()`；`SessionCreationPolicy.STATELESS`；`@EnableMethodSecurity`（启用 `@PreAuthorize`）；CORS 仅放行 `OPTIONS /**`。
- **PUBLIC_PATHS（全放行）**：`/`、`/health`、`/favicon.ico`、`/api/user/auth`、`/api/user/login`、`/api/user/register`、`/api/user/forget`、`/api/user/forget/code`、`/api/user/add`、`/static/**`、`/files/**`、`/*.html`、`/file-test.html`。
- **PUBLIC_GET_PATHS（GET 公开）**：`/api/game/mainline-config`、`/game/mainline-config`、`/api/acupuncture/train-game/**`、`/api/acupuncture/copper-man/acupoints`、`/api/acupuncture/copper-man/daily-case`、`/api/acupuncture/copper-content/stories/**`、`/api/skill/names`、`/api/skill/count`。
- **`hasRole("ADMIN")` 路径组**：`/api/admin/**`、`/api/user/admin/**`、`/api/dashboard/**`、`/api/acupuncture/illness/**`、`/api/acupuncture/xuewei/**`、`/api/acupuncture/zhenjiu-tools/**`、`/api/acupuncture/extracourse/**`、`/api/acupuncture/doctor-story/**`、`/api/acupuncture/jingluo/**`、`/api/acupuncture/origin-story/**`。
- **兜底**：`anyRequest().authenticated()`。
- **401/403 语义**：带 Authorization 头但未认证 → 401"登录已过期，请重新登录"；未带 → 403"无权限访问"。
- ⚠️ **重要机制**：`WebConfig.configurePathMatch` 为**所有 `@RestController` 自动添加 `/api` 前缀**（排除 springfox/swagger/doc 包）。因此所有 Controller 的真实访问 URL = `/api` + 类级 `@RequestMapping` + 方法路径。例如后台管理控制器 `/admin/community-post/...` 实际对外是 `/api/admin/community-post/...`，正好命中 `hasRole("ADMIN")`。**下述端点清单均按真实 `/api/...` URL 标注权限。**

### 2.3 登录限流（`LoginAttemptService`）

- **纯进程内 `ConcurrentHashMap`，非 Redis**（RedisTemplate 已配置但未用）。多实例部署下不共享、重启清零。
- 规则：**5 次失败 / 15 分钟滑动窗口**（从第一次失败起算）；key=`IP\n用户名小写`（支持 X-Real-IP/X-Forwarded-For 取真实 IP）。
- `checkAllowed`（登录前）：窗口过期自动清除放行；失败数≥5 → `BusinessException("登录尝试过多，请15分钟后再试")`。
- `recordFailure`（密码错误后）、`clear`（登录成功后）。

### 2.4 密码安全与重置

- **BCrypt**（`BCryptPasswordEncoder`），注册/登录/改密/管理员重置全链路使用。
- 密码重置（`PasswordResetCodeService`）：**邮箱 6 位一次性验证码**（SecureRandom），10 分钟有效、60 秒重发冷却、5 次尝试上限；同样**进程内 ConcurrentHashMap 存储**；发送前按 username+email 查账号，**不存在静默返回**（防账号枚举）；`verifyAndConsume` 校验通过立即消费删除。
- 重置接口 `/api/user/forget`：先验码再改密（防枚举差异）。
- 生产管理员引导：`ProductionAdminBootstrap`（@Profile("prod")）——启动时若无 ADMIN 用户，用 `bootstrap.admin` 环境变量创建首个管理员，校验用户名 `[a-zA-Z0-9_]{3,50}`、邮箱格式、**密码≥12 位**。

### 2.5 内容安全（正则过滤，无 XSS 过滤器）

- **`AiSafetyService`**（AI 对话双闸）：输入 `HIGH_RISK_QUESTION` 正则（治/诊断/用药/扎针/针刺/艾灸/按摩/取穴/配穴/处方/剂量 + 发烧/出血/晕倒/昏迷/呼吸困难/胸痛/过敏/中毒/自残等）命中 → 直接返回固定 `SAFE_REFUSAL`；输出 `UNSAFE_REPLY` 正则（治疗承诺、剂量单位、取穴操作等）命中 → 整体替换为拒绝话术。
- **`CommunitySafetyService`**（社区内容）：`MEDICAL_ADVICE` 正则（建议吃药/针刺/艾灸/治疗/处方等）与 `PERSONAL_CONTACT` 正则（微信/QQ/手机号/住址/学校等 + 手机号 `1[3-9]\d{9}`）——发帖/评论前校验，命中抛 `BusinessException`。
- 全工程**无 XSS 过滤过滤器**；无 `@TableLogic`/`@Version`（逻辑删除靠业务 status 字段，乐观锁在 `game_config_revision.edit_version` 与 `user_task_progress.version` 上手工实现）。

### 2.6 审计日志（`AuditEventService`）

写 `audit_event` 表：`actor_user_id/request_id/action/resource_type/resource_id/outcome/details_json`。已用于：登录/登出（AUTH_SESSION）、游戏进度（GAME_PROGRESS / GAME_PROGRESS_RESET）、旧数据导入（GAME_LEGACY_IMPORT）、侦探社兑换（AGENCY_ARCHIVE_EXCHANGE）、初始化（INITIALIZE_GAME_STATE）、主线配置（MAINLINE_CONFIG_SAVE_DRAFT / PUBLISH / RESTORE）。**明确不存密码、Token、验证码。**

---

## 3. 完整 API 端点清单（共 35 个 Controller，约 160 个端点）

> 约定：所有路径前缀已含 WebConfig 自动注入的 `/api`。权限列依据 SecurityConfig 路径规则 + `@PreAuthorize` + 方法内 `requireAdmin()` 三重判定。响应统一包装 `Result<T>{code,msg,data}`。

### 3.1 用户模块 —— `UserController`（`/api/user`，24 个端点）

| 方法 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /api/user/login | 登录（IP+用户名限流 5 次/15 分钟；用户名或邮箱均可） | 公开 |
| POST | /api/user/add | 注册（仅普通用户；初始分 +10；初始化游戏状态） | 公开 |
| POST | /api/user/logout | 退出并撤销当前会话（auth_session.revoke） | 需登录 |
| POST | /api/user/forget/code | 发送邮箱密码重置验证码（防枚举） | 公开 |
| POST | /api/user/forget | 邮箱验证码重置密码 | 公开 |
| GET | /api/user/current | 当前登录用户信息 | 需登录 |
| PUT | /api/user/{id} | 更新资料（仅本人，管理员可改他人） | 需登录 |
| PUT | /api/user/password/{id} | 修改密码（仅本人，校验旧密码） | 需登录 |
| GET | /api/user/page | 管理员分页查用户（keyword/username/email/name/userType/status） | ADMIN（方法内 requireAdmin） |
| GET | /api/user/admin/summary | 用户统计（总/活跃/禁用/管理员数） | ADMIN |
| POST | /api/user/admin/create | 管理员创建普通用户（强制 USER 类型） | ADMIN |
| GET | /api/user/admin/overview/{id} | 管理员查看用户学习档案（等级/探险图/答题/铜人/徽章/签到/背包等） | ADMIN |
| PUT | /api/user/admin/{id}/profile | 管理员编辑用户资料 | ADMIN |
| PUT | /api/user/admin/{id}/status | 管理员启/禁用户（禁止禁自己） | ADMIN |
| PUT | /api/user/admin/{id}/password | 管理员重置密码 | ADMIN |
| GET | /api/user/admin/checkins/{id} | 管理员看指定用户签到日历 | ADMIN |
| GET | /api/user/admin/quiz-history/{id} | 管理员看用户答题历史 | ADMIN |
| POST | /api/user/rewards/shunting | 领取今日调车挑战奖励（+10 分，幂等） | 需登录 |
| GET | /api/user/level/{id} | 气血能量等级（九级） | 需登录（本人） |
| GET | /api/user/adventure-map/{id} | 探险地图解锁状态（服务端任务进度为准） | 需登录（本人） |
| POST | /api/user/certificate | 上传九级荣誉证书（存 bussiness/user_avatar，写 user.honor） | 需登录 |
| POST | /api/user/checkin/today | 每日签到（+5 分 + 签到徽章，幂等） | 需登录 |
| GET | /api/user/checkin/month | 某月签到日期列表 | 需登录 |
| GET | /api/user/badges | 我的徽章 | 需登录 |

### 3.2 游戏模块 —— `GameController`（`/api/game`，4 个端点）

| 方法 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /api/game/state | 完整游戏状态（分数/任务进度/材料/铜人档案/地图关卡/stateVersion） | 需登录 |
| POST | /api/game/progress/events | 提交游戏进度事件（TASK_PROGRESS/COMPLETED/RESET/CHECKIN/SHUNTING），支持 `Idempotency-Key` 头 | 需登录 |
| POST | /api/game/agency/archive-items/exchange | 侦探社档案修复兑换（校验前置+扣材料） | 需登录 |
| POST | /api/game/legacy-import | 旧版游戏状态导入（迁移任务进度/奖励） | 需登录 |

### 3.3 AI 模块 —— `AiController`（`/api/ai`，1 个端点）

| 方法 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /api/ai/chat | 儿童教育向 AI 对话：高危提问→固定拒绝；无 Key→本地教育话术；调用 DeepSeek（儿童安全系统提示词、80 字内、60s 超时、重试 2 次）；输出再过滤 | 需登录（方法内校验 userId） |

### 3.4 答题模块 —— `QuizController`（`/api/quiz`，8 个端点）

| 方法 | 路径 | 功能 | 权限 |
|---|---|---|---|
| GET | /api/quiz/questions/random | 随机题目（1-50 道，不返回答案） | 需登录 |
| POST | /api/quiz/answer/submit | 提交单题答案（判分+写记录+统计+错题同步） | 需登录 |
| POST | /api/quiz/result/submit | 批量提交（旧版闯关兼容，选项下标 0-3） | 需登录 |
| GET | /api/quiz/stats/user | 答题累计统计 | 需登录 |
| GET | /api/quiz/history | 答题记录分页 | 需登录 |
| GET | /api/quiz/question/{questionId}/explanation | 题目解析（explanation+catagory） | 需登录 |
| GET | /api/quiz/mistakes | 我的错题（不含答案） | 需登录 |
| POST | /api/quiz/mistakes/answer | 错题重做（做对移除、做错保留） | 需登录 |

### 3.5 文件模块 —— `FileController`（`/api/file`，17 个端点）

| 方法 | 路径 | 功能 | 权限 |
|---|---|---|---|
| POST | /api/file/simple/upload/image | 简单图片上传（不落库，返回访问路径） | ADMIN（@PreAuthorize） |
| POST | /api/file/simple/upload/start-image | 开始页图片上传（bussiness/start） | ADMIN |
| POST | /api/file/simple/upload | 通用简单文件上传 | ADMIN |
| POST | /api/file/simple/upload/multiple | 批量上传（≤10，部分成功） | ADMIN |
| DELETE | /api/file/simple/delete/{filename} | 按文件名删除 | ADMIN |
| GET | /api/file/simple/info/{filename} | 文件信息 | ADMIN |
| GET | /api/file/simple/download/{filename} | 返回下载路径字符串（非流式） | ADMIN |
| POST | /api/file/upload | 业务文件上传并绑定业务对象（可替换旧文件）；**非管理员仅限 USER_AVATAR(本人) 与 POST_CONTENT(`post-{userId}-...`) 作用域** | 需登录 + 作用域校验 |
| POST | /api/file/upload/temp | 临时文件上传（24h 过期） | ADMIN |
| POST | /api/file/upload/temp-business | 临时业务文件上传（businessId="0"） | ADMIN |
| PUT | /api/file/confirm/{tempFileId} | 临时文件转正并绑定业务 | ADMIN |
| GET | /api/file/business/{businessType}/{businessId} | 按业务取文件列表 | ADMIN |
| GET | /api/file/business/{businessType}/{businessId}/{businessField} | 按业务+字段取文件 | ADMIN |
| DELETE | /api/file/{fileId} | 删除文件（仅上传者本人） | 需登录 |
| DELETE | /api/file/business/{businessType}/{businessId} | 按业务批量删除 | ADMIN |
| GET | /api/file/upload/config | 业务上传配置（允许类型/扩展/大小） | 需登录 |
| POST | /api/file/cleanup/temp | 清理过期临时文件 | ADMIN |

### 3.6 社区模块

**`CommunityPostController`（`/api/community/post`，12 个端点）**：均需登录。

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | /api/community/post/create | 发帖（内容安全校验；板块规则：普通用户限分享/问答，公告仅管理员，反馈人人可发） |
| GET | /api/community/post/page | 分页（current/size/title/postType），status=0 且时间倒序 |
| GET | /api/community/post/{postId} | 详情（含评论、点赞/收藏状态） |
| POST | /api/community/post/{postId}/like | 点赞/取消点赞（toggle） |
| POST | /api/community/post/{postId}/collect | 收藏/取消收藏（toggle） |
| POST | /api/community/post/report | 举报帖子（status=0 待处理） |
| POST | /api/community/post/comment | 评论（安全校验；commentCount+1） |
| GET | /api/community/post/latest | 首页最新（limit 默认 5） |
| GET | /api/community/post/mine/posts | 我发的帖子 |
| GET | /api/community/post/mine/collects | 我收藏的帖子 |
| GET | /api/community/post/mine/likes | 我点赞的帖子 |
| GET | /api/community/post/mine/comments | 我发过的评论 |

**`MisconceptionController`（`/api/community/misconception`，3 个端点）**：需登录。
- GET `/list` 全部误区；GET `/{id}` 详情；POST `/create` 新增（Swagger 标"管理端"但**无 ADMIN 校验**，任意登录用户可调——安全隐患）。

### 3.7 铜人模块

**`CopperManController`（`/api/acupuncture/copper-man`，3 个端点）**：
- GET `/acupoints` 穴位知识列表（带 discovered 标记）——**公开 GET**
- GET `/daily-case` 今日探案（5 展示穴/3 目标穴）——**公开 GET**
- POST `/discover` 发现今日穴位线索（须属今日目标；集齐 3 穴发奖）——需登录

**`CopperContentController`（`/api/acupuncture/copper-content`，5 个端点）**：需登录。
- GET `/stories` 公开故事列表（**公开 GET**）；GET `/stories/{code}` 故事详情（含用户进度）；PUT `/stories/{code}/progress` 保存故事进度（幂等 upsert）；GET `/feed` 内容流（未读计数，limit 默认 20）；POST `/feed/{releaseId}/read` 标记已读。

### 3.8 知识图谱 —— `KnowledgeGraphController`（`/api/acupuncture/knowledge-graph`，2 个端点）

- GET `/acupoints` 图谱节点列表（穴位/经络/区域/安全提示/易混穴/任务/奖励/3D 坐标）——需登录
- GET `/acupoints/{id}` 图谱详情（按 id 或名称）——需登录

### 3.9 前台内容展示模块（SecurityConfig 限定 ADMIN，无 @PreAuthorize）

| Controller | 前缀 | 端点 | 权限 |
|---|---|---|---|
| DoctorStoryController | /api/acupuncture/doctor-story | GET /list | ADMIN（路径规则） |
| ExtraCourseController | /api/acupuncture/extracourse | GET /list、GET /{id} | ADMIN |
| IllnessController | /api/acupuncture/illness | GET /list | ADMIN |
| JingluoController | /api/acupuncture/jingluo | GET /list、GET /{id} | ADMIN |
| OriginStoryController | /api/acupuncture/origin-story | GET /list | ADMIN |
| XueweiController | /api/acupuncture/xuewei | GET /list、GET /{id} | ADMIN |
| ZhenjiuToolController | /api/acupuncture/zhenjiu-tools | GET /list | ADMIN |
| TrainGameController | /api/acupuncture/train-game | GET /list | **公开 GET**（PUBLIC_GET_PATHS） |
| SkillController | /api/skill | GET /names（ids 逗号分隔）、GET /count | **公开 GET** |
| DashboardController | /api/dashboard | GET /statistics（用户/帖子/访问统计） | ADMIN |

> ⚠️ 值得注意：illness/xuewei/zhenjiu-tools/extracourse/doctor-story/jingluo/origin-story 从业务定位是**前台学习内容**，但 SecurityConfig 把它们限定为 ADMIN 角色——普通用户无法访问，疑似配置缺陷或由网关另行处理。

### 3.10 后台管理模块（14 个 Controller，全部 `@RequestMapping("/admin/...")`，真实 URL `/api/admin/...` → `hasRole("ADMIN")`）

| Controller | 前缀 | 端点 | 说明 |
|---|---|---|---|
| CommunityPostAdminController | /api/admin/community-post | GET /page、GET /{id}、POST（管理员发帖）、DELETE /{id} | 分页过滤 userId/title/postCatagory/postType/status |
| PostCommentAdminController | /api/admin/post-comment | GET /page、GET /{id}、DELETE /{id} | 过滤 postId/userId/keyword |
| SkillAdminController | /api/admin/skills | GET /page、GET /{id}、POST、PUT /{id}、DELETE /{id} | 过滤 skillName/Category/Type |
| DoctorStoryAdminController | /api/admin/doctorstory | GET /page、GET /{id}、POST /create、PUT /{id}、DELETE /{id} | 过滤 doctorName/skillId；含故事详情长度+技能存在校验 |
| ExtraCourseAdminController | /api/admin/extracourse | 同上 5 端点 | 过滤 name/skillId |
| IllnessAdminController | /api/admin/illness | 同上 5 端点 | 过滤 cowtown/illnessname |
| JingluoAdminController | /api/admin/jingluo | 同上 5 端点 | 过滤 jingluoName/Catagory/skillId |
| OriginStoryAdminController | /api/admin/originstory | GET /page、POST /create、GET /{id}、PUT /{id}、DELETE /{id} | 过滤 title/skillId |
| QuizQuestionAdminController | /api/admin/quiz-question | 同上 5 端点 | 过滤 title/category/difficulty/status；四选项+答案 A-D 必填校验 |
| TrainGameAdminController | /api/admin/train-game | 同上 5 端点 | 过滤 jingluoName |
| XueweiAdminController | /api/admin/xuewei | 同上 5 端点 | 过滤 xueweiName/Catagory/skillId |
| ZhenfaAdminController | /api/admin/zhenfa | 同上 5 端点 | 管理实体为 zhenjiutools，过滤 toolsName/skillId |
| **CopperContentAdminController** | /api/admin/copper-content | GET /overview、GET /acupoints（分页 keyword/status/modeled）、GET /acupoints/{code}、PUT /acupoints/{code}/draft、GET /stories、GET /stories/{code}、PUT /stories/{code}/draft、POST /{type}/{key}/{action}（状态流转）、GET /{type}/{key}/revisions、POST /revisions/{version}/restore、GET /releases | **类级 @PreAuthorize("hasRole('ADMIN')")**；内容草稿/审核/排期/发布/版本恢复；取当前用户做操作者 |
| **MainlineConfigAdminController** | /api/admin/mainline-config | GET（管理端配置）、PUT /draft（草稿）、POST /draft/validate（校验）、POST /draft/publish（**幂等发布，Idempotency-Key 头 + WriteOperationService**）、POST /versions/{version}/restore | **类级 @PreAuthorize("hasRole('ADMIN')")**；写操作记审计 |

> 说明：14 个 Admin Controller 因 WebConfig 自动加 `/api` 前缀，实际全部落入 `hasRole("ADMIN")`；CopperContent/Mainline 两个额外有方法级 `@PreAuthorize` 双重保障。普通 CRUD Admin 控制器**无 @Valid 参数校验**（仅个别类 @Validated）。

### 3.11 其他

- `SpringbootApplication` 无端点；静态资源 `/files/**` 映射 `file:./files/`（上传文件直出），`/static/**` 映射 classpath。
- Knife4j 文档（生产关闭）：`/doc.html`、`/swagger-ui.html`、`/v3/api-docs`。

---

## 4. 数据库实体清单

### 4.1 实体类（27 个，全部 Lombok + @TableName）

| 实体 | 表名 | 主键 | 关键字段 |
|---|---|---|---|
| User | user | id (AUTO) | username、password(BCrypt)、email、phone、userType(USER/ADMIN)、status(0/1)、score(气血值)、age、sex、honor(九级证书路径)、createdAt/updatedAt(自动填充) |
| AcupointKnowledge | acupoint_knowledge | code (INPUT, 如 LU-1) | pointNumber、name、pinyin、meridianCode/Name、modelId、bodyArea、standardLocation、childLocation、childDescription、safetyTip、sourceName/Link、positionX/Y/Z(3D 坐标)、sortOrder、enabled |
| CommunityPost | community_post | id | userId、title、content、likeCount/collectCount/commentCount、postCatagory、postType(分享/问答/公告/反馈)、postPic1-5、status(0 正常/1 删除/2 举报)、createTime |
| PostComment | post_comment | id | postId、userId、content、createTime |
| PostLike | post_like | id | postId、userId、createTime |
| PostCollect | post_collect | id | postId、userId、createTime |
| PostReport | post_report | id | postId、reporterUserId、reason、status(0 待处理/1 已处理/2 驳回) |
| QuizQuestion | quiz_question | id | title、optionA-D、correctAnswer(A-D)、explanation、catagory、difficulty(1-3)、status(0/1) |
| Skill | skills | skillId (INPUT) | skillName、skillBriefDescription、skillDescription、skillPic、skillCategory、skillScore、skillType |
| DoctorStory | doctorstory | doctorid (AUTO) | doctorName、doctorBrief、doctorDetail、doctorPic1-3、media、previewPic、readingGlossaryJson(JSON 重点词库)、skillId |
| ExtraCourse | extracourse | extracourseid | extraCourseName/Brief/Des/Icon/Pic1-3、skillId |
| Illness | illness | illnessid | cowtown、illnessname、illnessfeature、illnesspic、xueweicount/toolscount、xuewei1-5、tools1-4、illnessbadgename/path |
| Jingluo | jingluo | jingluoid | jingluoName、jingluoCatagory、jingluoOrder、illness、jingluoPic、skillId |
| OriginStory | originstory | originstoryID | storyTitle/Subtitle/Text/Pic1-3、media、skillId |
| Misconception | misconception | id | question、answer、sortOrder |
| Xuewei | xuewei | xueweiid | xueweiName、xueweiCatagory、`position`(反引号避开保留字)、illness、xueweiPic1、skillId |
| ZhenjiuTool | zhenjiutools | toolsid | toolsName、toolsPic1-3、toolsBrief、toolsTitle1-3、toolsText1-3、skillId |
| TrainGame | traingame | id | jingluoName、game1-5、game1Brief-5Brief |
| SysFileInfo | sys_file_info | id | originalName、filePath、fileSize、fileType、businessType/Id/Field、uploadUserId、isTemp、status、expireTime |
| UserAddress | user_address | id | userId、receiver、phone、province、city、district、detail、isDefault |
| UserBackpack | user_backpack | id | userId、skillId、collectTime |
| UserCollect | user_collect | id | userid、skillid |
| UserQuizRecord | user_quiz_record | id | userId、questionId、userAnswer、isCorrect、status |
| UserQuizStats | user_quiz_stats | id | userId、totalCount、correctCount、lastQuizTime |
| UserQuizMistake | user_quize_mistakes | id | userId、questionId（**表名拼写错误为 quize**） |
| UserAcupointDailyProgress | user_acupoint_daily_progress | id | userId、caseDate、acupointCode（唯一键防重复发现） |
| UserCopperManProfile | user_copper_man_profile | userId (INPUT) | copperTokens、starSand、completedCases、lastCompletedCaseDate |

### 4.2 无实体表（JdbcTemplate / 注解 SQL 操作）

- **V1 写入一致性 8 表**：`write_operation`（幂等写：`uk(user_id,operation_key)`+request_hash）、`audit_event`、`auth_session`、`outbox_event`（事务外发，当前未消费）、`user_task_progress`（`uk(user_id,game_code,task_code,period_key)`+version）、`reward_ledger`（`uk(user_id,grant_key)`+idempotency_key）、`user_material_balance`（9 种材料余额）、`user_game_migration`。
- **V2**：`user_agency_archive`（侦探社 7 项目修复记录）。
- **V3**：`game_config_revision`、`game_level_revision`、`game_task_revision`、`game_task_reward_revision`、`game_level_prerequisite_revision`（主线配置版本化 5 表）。
- **V4**：`copper_acupoint_revision`、`copper_story_revision`、`copper_content_release`、`user_content_release_read`、`user_copper_story_progress`。
- **注解 SQL 表**：`user_checkin`、`user_game_reward`（UserMapper）、`badge`、`user_achievement`（BadgeMapper）、`site_visit`（DashboardService 统计）。

### 4.3 Mapper（29 个）

- 27 个与实体一一对应且继承 `BaseMapper<T>`，无自定义方法（`CommunityPostMapper`、`QuizQuestionMapper`、`XueweiMapper` 等）。
- 带自定义注解 SQL 的：`UserMapper`（签到/游戏奖励/`selectCountByRawSql(${sql})`——**有 SQL 注入风险**）、`UserAcupointDailyProgressMapper`（insertIgnore/selectCodes/selectDistinctCodes）、`UserCopperManProfileMapper`（ensureProfile/awardDailyCase）、`BadgeMapper`（**不继承 BaseMapper**，操作 badge/user_achievement）。
- ⚠️ `AcupointMarkerMapper.java` 为 **0 行空文件**（无接口、无实体，占位）。
- 无 MyBatis XML（`mapper-locations: classpath:/mapper/*.xml` 指向的目录实际不存在）。

### 4.4 DTO 总览（97 个）

- **command 43 个**：登录/注册/改密/重置验证码/管理员建用户等认证类；发帖/评论/举报/误区创建等社区类；答题提交/错题重做/批量提交等测验类；内容管理类（Skill/Illness/Zhenfa/ExtraCourse/题库/名医/起源故事/铜人草稿等，多为字段直传）；游戏类（GameProgressEvent/LegacyGameStateImport/LegacyTaskProgress/AgencyArchiveExchange）；主线配置类（Draft/Publish）；铜人内容类（CopperAcupointDraft/CopperStoryDraft/CopperContentSchedule/CopperStoryProgress）。
- **response 51 个**：用户认证 6、社区 6、测验 7、内容资源 11、铜人/游戏 10、主线配置 7、管理端统计 4。嵌套链：GameMutationResponse→GameStateResponse→GameMapLevelState/GameTaskProgress；MainlineAdminConfig→MainlineConfig→MainlineLevelConfig→MainlineTaskConfig→MainlineRewardConfig；AdminUserOverview 聚合用户/等级/地图/徽章/学习统计（含静态内部类 LearningStats）；DashboardStatistics 含 DailyStatistics。
- **根目录 5 个**：BussinessFileUploadConfig、FileInfoDTO、FileUploadDTO、SimpleFileInfoDTO、DoctorStoryGlossaryItemDTO。
- 全部 Lombok @Data + jakarta.validation；无 @JsonProperty/@JsonFormat（时间靠全局 Jackson）；老表 snake_case 字段原样保留（illnessid/cowtown/xuewei1-5/tools1-4）；"catagory" 错拼出现 4 处（postCatagory/catagory/xueweiCatagory/jingluoCatagory）。

---

## 5. 核心业务逻辑要点（45 个服务）

### 5.1 GameStateService（游戏进度总控，873 行）——重点

- **模型**：`user_task_progress`（user×game×task×period_key 唯一）+ `reward_ledger`（grant_key 唯一）+ `user_material_balance` + `user_copper_man_profile` + `user_agency_archive`。
- **主线 8 关串行链**：报到处(checkin)→安全守护案(safe-start)→失踪竹简案(bamboo)→身体地图(body)→经络星河(meridian)→铜人档案室(archive)→星光修补册(secret-room)→侦探社修复(agency)；注册/登录时 `initializeUser` 把"报到"作为首个真实完成事件。
- **14 条经络路线**：`MERIDIAN_ROUTE_TARGETS`（肺5/大肠4/胃5/脾5/心4/小肠4/膀胱5/肾4/心包4/三焦5/胆6/肝4/任5/督5），全领取后 `syncMeridianAggregate` 发放星河聚合奖（target=14）。
- **事件校验**（`validateCommand`）：事件类型限 `TASK_PROGRESS/TASK_COMPLETED/TASK_RESET/CHECKIN_COMPLETED/SHUNTING_COMPLETED`；单次 delta≤1；resultCode 按任务白名单强校验（如安全问答只收 QUIZ_COMPLETE/CLIENT_COMPLETION）；旧任务码 `meridian-route` 已停用；聚合任务不可直接提交。
- **奖励**：积分（user.score）、9 种材料（bamboo-slip-shard/apricot-kernel/herbal-leaf/meridian-star-sand/acupoint-star-pearl/copper-token/mugwort-floss/safety-bell/star-compass）、侦探社收藏、铜人档案成长。任务奖励优先读主线配置（按 config_version 快照），经络/消消看/排序为内置规则；消消看里程碑 {50,100,200,350,500,750,1000}（hard×1.5）、排序 {50,100,150,200,250,300}。
- **侦探社修复**（7 项目）：gate→star-wall/herb-cabinet/bell-wall→display→archive→roof，前置+材料成本双校验，条件扣减防透支。
- **等级体系**：用户等级=已完成主线关卡数+1（杏林金牌侦探≥9/经络追踪侦探≥5/身体地图侦探≥3/铜人见习侦探）；`stateVersion`=任务 completed_at 最大时间戳（前端刷新依据）。

**幂等机制（重点，纯 DB 无 Redis）**：
1. `WriteOperationService.claim/complete`：`write_operation` 表 `UNIQUE(user_id, operation_key)` + 请求体 JSON **SHA-256 指纹**。重复键时：指纹不同→409"同一幂等键不能提交不同内容"；未完成→409"正在处理"；已完成同指纹→replay 直接返回上次结果。Controller 缺省生成 key：progress→UUID、agency→`agency:{itemId}:{uuid}`、legacy→`legacy-import:{sourceKey}`、publish→`mainline-publish:{revisionId}:{uuid}`。
2. `user_task_progress`：INSERT IGNORE 建行 + `FOR UPDATE` 行锁 + ON DUPLICATE `GREATEST` 推进。
3. `reward_ledger`：`UNIQUE(user_id, grant_key)` + INSERT IGNORE，**奖励保证只发一次**。
4. 每日类任务用 **periodKey 带日期**（`checkin:{day}`、案件日期）天然分日，无定时清零；同天重复被幂等/唯一键拦截。
5. 旧数据导入：`user_game_migration(user_id, source_key)` 主键防重导入，逐任务 `GREATEST` 合并，奖励幂等键 `legacy:{sourceKey}`。

### 5.2 QuizService（答题/错题/统计，339 行）

- 随机抽题 1-50 道（`ORDER BY RAND()`），仅返回题干+选项不泄露答案。
- 判题：`resolveCorrectLetter` 把 correct_answer 规范为 A-D（首字母→选项文本比对→默认 A）；写记录→`bumpStats` 统计→`syncMistakeTable`（**答对删错题、答错去重插入**）。
- 错题本：列表不含答案；重做校验归属（mistakeId 属当前用户且题号匹配），做对移除、做错保留。
- 统计：总数/正确数/最后答题时间（正确率前端自算）；历史分页。
- 与游戏任务**无代码联动**（"每日 3 题安全问答"由 GameStateService 的 target=3 承担）。

### 5.3 CommunityPostService（社区，439 行）

- 发帖：登录→`CommunitySafetyService.validatePost`（医疗建议/隐私正则）→板块解析（反馈人人可发、公告仅管理员、普通用户限分享/问答）→计数归零、status=0。
- 详情/列表：只展示 status==0；**用户名脱敏**（"小侦探"+id%10000 四位补零，不存在→"匿名侦探"）——儿童隐私保护设计。
- 点赞/收藏：toggle（查记录→删/插 + 计数 ±1，`Math.max(0,…)` 防负）。
- 举报：不要求帖子 status==0（已删也能举报）；后台无独立举报处理端点（CommunityPostAdminController 直接物理删除帖子）。
- 评论：安全校验 + commentCount+1。
- 管理端（CommunityPostAdminService）：分页过滤 + 管理员发帖 + **物理删除**。

### 5.4 CopperManService（铜人每日病例/穴位发现，199 行）

- 每日病例**无状态排期**：从具 3D 坐标的启用穴位（361 个）按 `floorMod(caseDate.toEpochDay(), 总数)` 轮转取 5 个展示穴，前 3 个为目标穴；跨天自动轮换。
- 发现：code 必须属今日目标（否则 400）；`insertIgnore`（`(user_id,case_date,acupoint_code)` 唯一键防重复发现）；**集齐 3 穴→`GameStateService.completeCopperManCase`（幂等）→首次集齐发 +2 铜片/+5 星砂**（档案条件更新天然防重）；未登录返回本地结果不发奖；对 GameStateService 为可选依赖（降级到 profileMapper.awardDailyCase）。

### 5.5 CopperContentService（铜人内容中心，627 行）

- 穴位/故事内容走 **修订版本表**（`copper_acupoint_revision`/`copper_story_revision`，JSON payload，(code,version_no) 唯一）。
- **状态机**：DRAFT→SUBMIT→IN_REVIEW→APPROVE→APPROVED→SCHEDULE(晚于 now)→SCHEDULED→PUBLISH→PUBLISHED；ARCHIVE；同 code 同时仅一个活动态；发布时旧版 ARCHIVED + upsert `acupoint_knowledge` + 生成 `copper_content_release` 站内推送。
- **`@Scheduled(fixedDelay=60_000)` 定时发布**：扫 SCHEDULED 且到期（限 50 条），失败写 publish_error。
- 儿童文案正则拦截"主治/治疗/按揉/针刺"等词汇；故事校验 pages≥3/clues≥2/rewards≥1。
- 状态流转 `updateStatus` 带期望状态条件更新，冲突→409"已被其他管理员修改"（乐观并发）。
- 用户故事进度 upsert 只升不降、completed_at 首次置时间（幂等）。

### 5.6 MainlineConfigService（主线配置草稿/发布/校验，813 行）

- **版本化修订模型**：`game_config_revision`（version_no 单调、status、base_version、**edit_version 乐观锁**）+ 4 张明细表按 revision_id 关联；至多一个 PUBLISHED、一个 DRAFT。
- 状态机：DRAFT →(publish)→ PUBLISHED →(再次发布)→ ARCHIVED；`restore` 按 version 复制为全新 DRAFT。
- `saveDraft`：新建取 nextVersion；更新要求 DRAFT 且 `expectedEditVersion` 匹配，UPDATE 带版本条件影响行数≠1 →409"草稿已被其他管理员修改"。
- `publish`：草稿必须 DRAFT+校验通过；旧 PUBLISHED→ARCHIVED、新→PUBLISHED（记发布人/时间）。**发布即生效**（GameStateService 每次动态读最新快照，无缓存无定时）。
- **校验规则极严格**：恰 8 关；id/order/mapKey/坐标/图标/印章/路由/任务绑定全部锁定不可改；每关恰好 1 个固定任务；checkin 无前置、其余必须有前置且序在前；前置环检测（DFS）；目标 1-999（星河总目标≤14）；奖励仅 MATERIAL/SCORE、材料白名单、数量 1-999、不可重复。
- 表未初始化时回退内置 `defaultSnapshot()`（8 关种子）兼容旧测试库；生产由 Flyway V3 初始化。
- 内置 8 关定义：报到处→安全守护案→失踪竹简案→身体地图追踪案→经络星河密令→铜人档案室→星光修补册→侦探社修复计划（含地图坐标/路由/任务/奖励）。

### 5.7 AiSafetyService + DeepSeekClient（AI 安全对话）

- `DeepSeekClient`：Hutool HTTP 直连 `deepseek-chat` 模型，60s 超时、2 次重试（指数退避 250ms×i）、Bearer Key（兼容 `ai.deepseek.api-key`/`spring.ai.deepseek.api-key`/`file.ai.deepseek.api-key` 三路径）；固定儿童安全系统提示词（只答文化科普、禁诊断治疗操作、限 80 字、不索隐私）；非 2xx 不重试。
- `AiSafetyService`：输入高危正则→固定拒绝话术；输出不安全正则→替换拒绝话术；无 Key 或调用失败→本地关键词教育话术（经络/铜人/穴位/安全/故事）。

### 5.8 UserService（注册/登录/用户管理，1008 行）

- 登录：用户名或邮箱→BCrypt 校验→状态检查→`initializeUser`→签发带 tokenId 的 JWT→`AuthSessionService.create`。
- 注册：两次密码一致、仅 USER 类型、用户名/邮箱唯一、初始分 +10、初始化游戏状态。
- **QiBloodLevel 九级**：L1 杏林见习侦探(0-10)…L9 杏林金牌侦探(90-∞)，`getLevelByScore` 线性匹配。
- 签到 +5 分（幂等：主路径 GameStateService.checkinToday，兜底 user_checkin 唯一键）+ `BadgeService.onCheckin` 徽章（1/3/7/30 天里程碑，**异常不阻断签到**）。
- 调车挑战每日奖励 +10（幂等：`user_game_reward` 唯一键）。
- 荣誉证书：仅 L9、已有 honor 不覆盖（幂等）、FileUtil 存盘写 user.honor。
- 管理员：禁删管理员、禁禁用自己、建用户强制 USER 类型；`getAdminUserOverview` 聚合学习档案。

### 5.9 BadgeService（徽章）

- `badge`/`user_achievement` 表；签到天数 1/3/7/30 发徽章（`tryAward` 按名幂等）。

### 5.10 KnowledgeGraphService（Neo4j，176 行）

- `@ConditionalOnProperty(neo4j.enabled=true)` 才注册驱动；`ObjectProvider<Driver>` 可缺席 → 降级 5 个硬编码 demo 穴（合谷/足三里/百会/中脘/三阴交）。
- Cypher：`MATCH (a:Acupoint)` + 5 个 OPTIONAL MATCH（BELONGS_TO→Meridian、LOCATED_IN→BodyRegion、HAS_SAFETY_TIP→SafetyTip、CONFUSED_WITH→Acupoint、`<-[:TARGETS]-(LearningTask)-[:REWARDS]->Reward`），collect 聚合；`executeRead` 只读事务；异常静默降级。

### 5.11 其他服务

- **AuditEventService / AuthSessionService / WriteOperationService / LoginAttemptService / PasswordResetCodeService / AiSafetyService / CommunitySafetyService**：见第 2 节。
- `DashboardService`：用户/今日新增/管理员/帖子/访问统计（COUNT + 近 7 天访问趋势），单项异常降级默认值不阻断；`selectCountByRawSql` 拼接 SQL（内部常量，建议参数化）。
- `FileService`/`SimpleFileService`/`BussinessFileValidationService`：见第 7 节。
- 常规内容 CRUD 服务（Illness/Xuewei/Jingluo/ExtraCourse/DoctorStory/OriginStory/Zhenfa/TrainGame/Skill/QuizQuestion/Misconception 及其 Admin 版）：统一"客户端只读（listAll/getById 查无返回 null）+ 管理端 CRUD（分页+详情+增删改，查无抛 BusinessException）"模式；`DoctorStoryAdminService` 校验最全（TEXT 64KB 字节校验、技能存在校验、词汇表规范化）；`UserAddressService` 是唯一带归属权限校验与完整事务的常规服务。
- **convert 2 个**（UserConvert/UserAddressConvert）：普通 static 类、手写映射，非 MapStruct。
- 服务层共 45 个文件（43 服务 + 2 convert），无接口/实现结构；三类数据访问风格：MyBatis-Plus（大多数）、JdbcTemplate 手写 SQL（审计/会话/幂等/内容后台/主线/游戏状态）、Neo4j；内存态（限流/验证码/AI/社区安全）。

---

## 6. 数据初始化机制

### 6.1 Flyway 迁移（生产权威，`db/migration/`）

- **V1__write_consistency_core.sql**：建 8 张写入一致性表（write_operation/audit_event/auth_session/outbox_event/user_task_progress/reward_ledger/user_material_balance/user_game_migration），详见 4.2。
- **V2__agency_archive_persistence.sql**：`user_agency_archive`。
- **V3__mainline_config_revision.sql**：主线配置 5 表 + `user_task_progress` 加列 `config_version`；种子主线 v1：1 条 config、8 关卡、12 任务（主线 8+每日 4）、15 条奖励、7 条关卡前置。
- **V4__copper_content_center.sql**：内容中心 6 表 + 回填 361 个穴位修订 v1 + **种子 4 个探案故事**（失踪竹简案/会考试的小铜人/星河断线案/安全铃铛失声案，JSON 含 pages/clues/reasoning/safety/rewards）+ 发布记录（route=`/doctor-story?story=xxx`）。

### 6.2 schema.sql / data.sql（仅开发 `SQL_INIT_MODE=always` 兼容路径，生产关闭）

- schema.sql：10 张表（acupoint_knowledge、user_acupoint_daily_progress、user_copper_man_profile、user_agency_archive、user_game_reward、copper_acupoint_revision、copper_story_revision、copper_content_release、user_content_release_read、user_copper_story_progress）。
- data.sql（577KB）：**361 条穴位知识种子**（幂等 upsert，由 `scripts/generate-copper-man-seed.mjs` 生成；其中 100 个带 3D 坐标与 GB/T 12346-2021 教学元数据；14 条经络：BL 67/ST 45/GB 44/GV 28/KI 27/CV 24/TE 23/SP 21/LI 20/SI 19/LR 14/LU 11/HT 9/PC 9；儿童向文案排除治疗功效与针刺操作）+ 1 条穴位修订初始化。**不含管理员/用户/题目**。

### 6.3 生产引导与清洗脚本

- **`ProductionAdminBootstrap`**（@Profile("prod")）：无 ADMIN 时按环境变量创建首个管理员（强校验）。
- `sql/doctorstory_readingglossary_migration.sql`：旧 doctorstory 表条件化加列 previewpic/readingglossary + 按 doctorid 1-6 回填封面与生词 JSON（扁鹊/华佗/皇甫谧/孙思邈/王惟一/杨继洲，统一"只介绍历史和文化，不教授操作"口径）。
- `production-seed-cleanup.sql`：TRUNCATE 17 张业务表 + 插入 10 道安全/文化判断题（quiz_question）+ `app_migration` 标记 `production_seed_sanitized_v1` 防重。

### 6.4 日志配置

- log4j.xml 与 log4j2.yml 两套 Log4j2 配置：Console(DEBUG+) + application.log（按天/100MB 滚动/gz/保留 10 份）+ error.log（仅 yml 版）；`org.example.springboot` DEBUG。log4j.xml 引用了未定义的 ErrorAppender（配置告警）；Spring Boot 3 默认 Logback，pom 未排除 logback，需确认实际生效实现。

---

## 7. 文件上传

### 7.1 链路与存储

- **目录**：`FileUtil.FILE_BASE_PATH = user.dir + "/files/"`；静态映射 `/files/**` 直出。
- **两套体系**：
  1. `SimpleFileService`（简单上传）：**不落库**，`FileUtil.saveFile` 存盘返回访问路径；目录解析含特殊映射（START→bussiness/start、SHUXUE/JINGLUO/ZHENFA/EXTRACOURSE/ILLNESS/SKILLS 等）；批量≤10。
  2. `FileService`（业务上传）：落 `sys_file_info` 表，绑定 businessType/businessId/businessField/uploadUserId/isTemp；支持临时文件（24h 过期）→确认转正；`cleanupExpiredTempFiles` 每日凌晨 3 点定时清理（FileCleanupScheduler cron `0 0 3 * * ?`，8 点存储监控为 TODO 占位）+ 手动端点。

### 7.2 校验（BussinessFileValidationService + FileUtil）

- **扩展名识别**（FileTypeEnum）：IMG(jpg/jpeg/png/gif/bmp/webp)、PDF、DOC、XLS、PPT、TXT、AUDIO、VIDEO、ZIP、OTHER。
- **业务类型→允许类型**（FileBusinessTypeEnum）：USER_AVATAR{IMG}、TEMP_FILE{IMG,PDF,DOC,TXT}、SYSTEM_NOTICE{IMG}、ACTIVITY{IMG}、COURSE{IMG}、COURSE_CHAPTER{IMG,VIDEO,AUDIO}、START{IMG}、POST_CONTENT{IMG}；不匹配抛"业务类型不支持文件类型"。
- **图片三重校验**：≤10MB（总上限 100MB）→ Content-Type 须 `image/*` → **魔数签名校验**（jpg `FFD8FF`、png `89504E47`、gif `GIF87a/89a`、bmp `BM`、webp `RIFF....WEBP`），"图片内容与扩展名不一致"直接拒绝。
- **路径安全**：`validateName` 拒绝 `..` `\` `:` `*` `?` `"` `<` `>` `|`；删除时校验绝对路径以 FILE_BASE_PATH 开头（防路径穿越）。
- **业务权限**：`validateUploadScope`——ADMIN 全放行；普通用户仅 `USER_AVATAR`（businessId==本人）与 `POST_CONTENT`（businessId 形如 `post-{userId}-{8~80位字母数字}`）。

### 7.3 已知风险点（全报告汇总）

1. `UserMapper.selectCountByRawSql(${sql})` 为 `${}` 拼接（内部常量调用，但存在注入面）。
2. `BussinessFileValidationService.validateBusinessPermission` 为恒 true 占位实现。
3. `CommunityPostService.listLatest` 的 limit 直接拼 SQL（调用方传入，默认 5）。
4. `CommunityPostService.report` 不校验帖子状态。
5. 登录限流/密码验证码为**进程内 Map**，多实例失效；RedisTemplate 已配置但未使用。
6. `AcupointMarkerMapper` 空文件；`ResultCode.VALIDATE_FAILED` 复用 HTTP 404 语义；`JwtTokenUtils` 注释（7 天）与配置（24h）不一致。
7. `MisconceptionController.create` 无 ADMIN 校验（任意登录用户可新增"常见误区"）。
8. 前台内容接口（illness/xuewei/zhenjiu-tools/extracourse/doctor-story/jingluo/origin-story/dashboard）被 SecurityConfig 限定 ADMIN，与"用户端"定位不符（疑似配置缺陷）。
9. `submitLegacyBatch` 自调用 submitAnswer 依赖 Spring 自调用同事务的实现细节；KnowledgeGraphService 异常静默降级（排障日志缺失）；CopperManService 对 GameStateService 可选依赖降级。
10. V3 迁移中 `game_config_revision` 种子以固定 id=1 插入，重复执行需注意幂等性；log4j.xml 引用未定义 ErrorAppender。

---

**报告完毕。** 覆盖范围：config(10)/common(2)/exception(3)/enums(5)/constant(1)/util(4)/ai(3)/controller(35)/service(45+2 convert)/entity(27)/mapper(29)/dto(97)/resources(pom、yml×2、schema、data、Flyway V1-V4、SQL 脚本、日志配置) 共 265 个 Java 文件及全部相关配置。

# 儿童针灸教育科普网站（Vue 3）前台页面组件技术报告

**项目根目录**：`D:\总\Pediatric Acupuncture Education System\pingtai1`
**读取范围**：`vue3\src\views\` 下 34 个前台相关文件（超长文件已分段完整读取，非截断）
**路由依据**：`vue3\src\router\index.js`（62 条路由规则，前台布局 `FrontendLayout.vue` 承载 `/home-map` 等页面，`AuthLayout.vue` 承载 `/auth/*`）

---

## 〇、全局架构与设计总览

- **世界观主题**：全站采用"小铜人中医侦探社"世界观，儿童以"小侦探"身份完成故事探案、身体地图、经络星河、安全课堂等关卡，换取"材料"（竹简碎片、杏林叶、经络星砂、穴位星珠、铜片、安全铃铛等）用于"侦探社修复"。
- **通用安全口号**（贯穿几乎所有教育页面）：**"只观察、只学习，不自己针刺"**，并反复声明"本系统用于中医针灸文化科普，不能替代医生，不提供自行针刺/治疗指导"。
- **共享基础设施**：
  - Composable：`@/composables/useGameState`（材料 `materials`、徽章 `badges`、故事归档 `storyArchiveIds`、每日任务 `dailyTasks`、进度记录 `recordTaskProgress/getTaskProgress/resetTaskProgress`、独立任务完成 `completeStandaloneTask`、材料发放 `addMaterials`、复习记录 `addReviewRecord`、故事进度 `getStoryProgress/saveStoryProgress/completeStoryCase`、侦探社兑换 `exchangeAgencyArchiveItem` 等）；
  - Store：`@/store/user`（`useUserStore`：登录、角色 `isAdmin`、等级 `getLevelInfo`、游戏态 `loadGameState`、调车奖励 `claimShuntingReward` 等）；
  - 静态数据：`@/data/generatedRewardAssets`（图标/徽章/装饰图）、`@/data/storyCases`（故事案件）、`@/data/safetyQuestionsGame`（安全题/宣誓/成就）；
  - API 模块：`@/api/user`、`@/api/AcupunctureApi`、`@/api/BackpackApi`、`@/api/QuizApi`、`@/api/CommunityApi`、`@/api/CopperContentApi`、`@/api/GameApi`、`@/api/FileApi`。
- **页面风格**：多数页面用 `ant-design-vue` 组件 + Font Awesome + 手绘风 CSS（宣纸米黄/青绿/古铜色调），超长 `<style scoped>` 为视觉定制。

---

## 一、认证模块（`vue3\src\views\auth\`）

### 1. Login.vue（102 行）
1. **路由/用途**：`/auth/login`（`AuthLayout`），侦探账号登录。
2. **交互**：复用 `AuthForm` 组件，两个字段（侦探账号/侦探密码），提交时调用登录 API；按角色分流：`roleCode !== 'USER'` → `/back/dashboard`（后台），普通用户 → `redirect` 参数或默认 `/home-map`。
3. **API/Store**：`@/api/user.login`；`useUserStore.setUserInfo`；`useRouter/useRoute`。
4. **硬编码**：字段文案"侦探账号/侦探密码/身份卡"；跳转逻辑与角色判断；链接"忘记密码？""还没有账号？立即注册"。
5. **安全**：无独立安全提示（认证页）。

### 2. Register.vue（392 行）
1. **路由/用途**：`/auth/register`（另有 `/checkin`、`/register` 重定向到它），"侦探身份组装局"四步注册。
2. **交互**：四步向导（顶部进度点可回跳）：① 选形象——3 个角色（**明察探探**"细节观察"、**草木寻踪**"草本辨认"、**经络星探**"经络探索"）；② 建档案——账号（3–20 位字母数字下划线实时校验）、**家长邮箱**、密码（强度条：等待输入/还差一点/安全合格/很可靠）；③ 写昵称——2–8 字、字数计数、"摇一摇名字"随机动画、灵感纸条（**杏林小神探/铜铃小侦探/草木寻踪家/经络小星探/青囊小助手**）；④ 盖章——核对清单 + 勾选安全守则后提交。右侧实时预览 3D 翻面"见习侦探身份卡"（编号 `XTR-年份-账号后4位`），注册成功后盖章动画、三份见面礼解锁、撒花庆祝。
3. **API/Store**：`@/api/user.register`；成功后自动 `userStore.login`（失败则提示手动登录）；`localStorage` 记录所选形象。
4. **硬编码**：三形象/昵称候选/身份卡文案/见面礼（小侦探身份卡、初识草本徽章、探险小卷轴，图标来自 `generatedRewardAssets`）；誓言"**只观察 · 只学习 · 不自己针刺**"。
5. **安全**：明确要求"请和家长一起填写。邮箱只用于账号找回和安全通知"；勾选条款"**和家长一起确认：我只观察、只学习，不自己针刺；同意使用家长邮箱进行账号找回和安全通知**"（未勾选不能注册）。

### 3. ForgotPassword.vue（230 行）
1. **路由/用途**：`/auth/forgot-password`，找回密码。
2. **交互**：两阶段复用 `AuthForm`：阶段一输入账号+家长邮箱发送验证码；阶段二输入 6 位验证码（10 分钟有效提示，邮箱做掩码 `xi**@domain`）、新密码、确认密码；成功后进入"身份卡密码已更新"成功页（可返回登录）。
3. **API**：`@/api/user.requestPasswordResetCode`、`forgetPassword`。
4. **硬编码**：字段与规则（验证码 `^\d{6}$`、密码 8–100 位、两次一致校验）。
5. **安全**：验证码时效提示；无针刺相关内容（纯账号安全）。

---

## 二、frontend 主页面（`vue3\src\views\frontend\`）

### 4. HomeMapLanding.vue（7 行）
1. **路由/用途**：`/home-map`（前台首页/主地图，`/landing`、`/index.html` 重定向至此）。
2. **交互**：**纯包装组件**，仅 `<HomeMap />` 渲染 `@/components/home-map/HomeMap.vue`（真正的杏林探险地图实现在该组件内，不在本次清单）。
3. **API/Store**：无（委托给子组件）。
4. **硬编码**：无。
5. **安全**：无（见 HomeMap 子组件）。

### 5. Agency.vue（1404 行）
1. **路由/用途**：`/agency`（名称 AgencyHome，"小铜人侦探社"；`/clinic` 重定向到此），"侦探社修复计划"收集材料修复场景。
2. **交互**：三栏布局——左"侦探线索箱"（6 种材料格 + "已点亮星光 x/y"卡片）；中"侦探社场景"（建筑底图 + 7 个可点击热区标签 + 修复后叠加装饰图）；右"修复任务清单"（进度条 + 1–3 星评级 + 7 个任务卡，每项显示材料消耗、单选/立即修复）；底部"本次可修复 N 项"统计 + 查看奖励 / 一键修复 / 返回地图。带 toast 反馈与奖励弹窗（今日侦探奖励 3 项）。
3. **API/Store**：`useGameState`（`materials`、`agencyArchiveIds`、`exchangeAgencyArchiveItem`——逐项调用服务端兑换扣除材料）；`useRouter`。
4. **硬编码**：6 种材料（竹简碎片/杏林叶/经络星砂/穴位星珠/铜片/安全铃铛）；7 项修复任务及坐标与消耗：
   - 门牌（竹简碎片×3+铜片×1）、经络星空墙（经络星砂×4）、草药小柜（杏林叶×5）、小铜人展示台（穴位星珠×2+铜片×2）、安全铃铛墙（安全铃铛×2）、档案角（竹简碎片×3+星图地图线索×1）、屋顶（竹简碎片×5+经络星砂×4，列表隐藏项）；
   - 奖励：侦探社徽章（修复 5 项）、安全观察星（安全铃铛墙）、经络地图线索（星空墙）。
5. **安全**：页头常驻"只观察、只学习，不自己针刺。"

### 6. Badges.vue（910 行）
1. **路由/用途**：`/badges`（"故事收藏墙"），成长徽章与案件归档展示。
2. **交互**：Hero 区统计"已获得/进行中/未解锁"；**故事收藏墙**（按 `storyChapterCatalog` 渲染案件卡：第 N 案、封面、3 星、状态"已归档/待调查/内容准备中/完成上一案后解锁"，点击跳故事馆再次调查）；**徽章墙**（左粘性侧栏：成长进度环形、状态图例、下一枚推荐；右侧徽章网格，按 tier 分色、显示进度条与获得条件）。
3. **API/Store**：`useGameState`（`badges`、`storyArchiveIds`、`storyProgress`）；`@/data/storyCases`（`storyCases`、`storyChapterCatalog`）；`generatedRewardAssets`。
4. **硬编码**：徽章等级文案（青铜/白银/金色/杏林/传说徽章）；状态文案（已获得/进行中/可挑战/未解锁）；首页文案"这里记录的是小药师的成长，不是考试排名"。
5. **安全**：无显式针刺提示（强调非排名、成长导向）。

### 7. Bag.vue（782 行）
1. **路由/用途**：`/bag`（requiresAuth，"材料背包"），查看已收集材料与兑换配方。
2. **交互**：Hero 统计（已拥有/总数/可兑换提醒）+ 宝箱图；主体左侧"基础材料"与"稀有材料"卡片网格（图标、×数量、来源、用途、锁定态）；右侧"可兑换提醒"配方卡（需求材料 已拥有/需要 高亮、缺项提示、按钮跳侦探社）。
3. **API/Store**：`useGameState.materials`；`generatedRewardAssets/generatedMaterialIcons`。
4. **硬编码**：4 个配方——**艾草灯笼**（艾绒×5+杏林叶×3）、**古风竹简墙**（竹简碎片×8）、**安全护符**（安全铃铛×8）、**穴位提示卡**（穴位星珠×8）；稀有度文案（基础/稀有/珍贵/传说材料）。
5. **安全**："安全铃铛提醒：**材料只用于学习任务和游戏化修复。本系统用于中医针灸文化科普，不能替代医生，也不能自行针刺。**"

### 8. BodyMap.vue（354 行）
1. **路由/用途**：`/body-map`（另有 `/shuxue` 重定向），"身体地图追踪案"。
2. **交互**：左侧儿童身体图带 5 个区域热区（点击切换，已学变绿）；右侧任务卡（当前区域说明 + 安全提醒 + "点亮这个区域"按钮 + 进度 x/10）；下方**穴位归位小游戏**——5 张穴位星卡，先选卡再点对应身体区域，正确则"归位成功"并保存进度，错误给出温柔提示；完成全部提示语。
3. **API/Store**：`useGameState.recordTaskProgress('main-hand-star-map', 1, {resultCode, idempotencyKey})`、`getTaskProgress`；`ant-design-vue message`。
4. **硬编码**：5 区域——头面/小手/肚腹/后背/小腿（含 x/y 坐标、描述、**每区安全提醒**）；5 穴位——**百会→头、合谷→手、中脘→腹、肺俞→背、足三里→腿**。
5. **安全**：每区域单独安全语（如"头面区域很敏感，只能学习观察，不能随便按压或模仿针刺""手上穴位只能作为文化知识学习，不能自己拿尖锐物尝试""学习足三里是文化认知，不代表可以自己针刺或治疗"）；页首"这里只做中医文化科普和身体认知学习，不做真实针刺"。

### 9. Clinic.vue（28 行）
1. **路由/用途**：`/clinic` → `onMounted` 即 `router.replace('/agency')`，"正在进入侦探社..."占位页。
2. **交互**：无（纯重定向）。
3. **API/Store**：`useRouter`。
4. **硬编码**：占位文案。
5. **安全**：无。

### 10. CopperMan.vue（2851 行，已分段完整读取）
1. **路由/用途**：`/copper-man`（requiresAuth，"小铜人馆 · 人体探案实验室"），**Three.js 3D 交互学习页**。
2. **交互**：
   - **3D 模型**：`createChibiCopperMan`（`@/utils/chibiCopperMan`）程序化生成 Q 版小铜人；`OrbitControls` 拖拽 360° 旋转/滚轮缩放/限制极角；正面/背面/侧面三视角按钮；放大/缩小/重置控制；光线（半球+环境+主光+轮廓光）、底座（圆柱基座）。
   - **每日探案**：后端下发当日 3 个目标穴位星点（`getCopperManDailyCase`），星点按 `mapAcupointToChibi` 映射到模型位置；点击发光星点（Raycaster）"发现线索"→ 右侧线索档案显示名称/拼音/经络/身体区域/儿童化小知识/安全提示 → "收集这条线索"（`discoverCopperManAcupoint`）→ 奖励**铜片×2、经络星砂×5**；3 条全收集触发"档案修复完成"弹窗；未登录/登录失效跳登录并提示。
   - **百穴图鉴（Atlas 弹窗）**：`listCopperManAcupoints` 拉全量穴位（约 361 颗"文化星"）；搜索（名称/编号/拼音/区域/经络）、经络下拉、已发现/待探案筛选、每页 24 颗"再展开 24 颗"；详情侧栏含拼音、身体区域、观察提示、文化星小档案、安全提示；支持 `?acupoint=` URL 直达。
   - **侦探头衔**：按完成案件数动态变化（0 铜人见习侦探 / ≥3 星点观察员 / ≥7 铜人档案师 / ≥14 杏林金牌侦探）。
3. **API/Store**：`@/api/AcupunctureApi`（`getCopperManDailyCase`、`discoverCopperManAcupoint`、`listCopperManAcupoints`）；`useUserStore`（登录态 + `loadGameState` 刷新）；`useRoute`。
4. **硬编码**：三视角/图鉴筛选选项、奖励数量、头衔阈值、欢迎弹窗与完成弹窗文案、提示语（"点击星点发现线索/拖动旋转 360° 观察"）。
5. **安全**：右栏常驻"**这里只进行观察学习，不进行针刺操作**"；浮窗"小铜人提示：只观察、只学习，不自己针刺哦！"；欢迎弹窗底部"只观察、只学习，不自己针刺"；图鉴内每穴位带 `safetyTip` 且页眉写"只观察、只学习，不在自己身上寻找"。

### 11. DoctorStory.vue（2943 行，已分段完整读取）
1. **路由/用途**：`/doctor-story`（"针灸故事馆/儿童文化探案"），绘本式故事闯关。
2. **交互**：五步导航（阅读→找线索→推理→安全→奖励）；左侧章节栏（案件卡：第 N 案、3 星、锁定/已归档、"查看故事收藏墙"按钮）；中间主舞台：
   - **阅读**：3 页绘本（弹层阅读器，页码圆点、Esc/焦点管理），读完解锁下一关；
   - **找线索**：调查现场图 4 个脉动热区 + 线索卡（两路均可点击），找齐 4 条点亮星；
   - **推理**：证据板（线索可拖拽/点击排入 4 槽）+ 四选一推理题（答错给提示"再看看窗边的竹叶和书架后的卷轴"，可继续）；
   - **安全判断**：安全题（答错提示"不能自己拿尖锐物品尝试针刺"）；
   - **奖励**：星级（阅读 1 星+线索 1 星+推理与安全 1 星）、奖励列表、"再调查一次/返回探险地图"。
   - 右侧任务栏：本案目标 4 项进度、线索背包、奖励进度（不计时·不扣分）、安全提醒。
3. **API/Store**：`@/api/CopperContentApi`（`listPublishedCopperStories` 服务端故事、`saveCopperStoryProgress` 保存进度）；`useGameState`（`storyArchiveIds`、`dailyTasks`、`getStoryProgress/saveStoryProgress/completeStoryCase`）；兜底静态 `@/data/storyCases`（`normalizePublishedStory` 规范化服务端字段：页码、线索热区默认坐标 `[{17,22},{72,28},{31,74},{82,70}]`、推理/安全选项字符串化、奖励映射 dailyTaskId=`daily-read-copper-story`、mainTaskId=`main-mist-in-xinglin`）。
4. **硬编码**：默认案件"**医馆里少了一卷竹简**"（CASE 01，缺失竹简案）；案件星级规则、提示语、反馈语（如"安全铃铛提醒你：不能自己拿尖锐物品尝试针刺"）；章节中文序号（一~六）。
5. **安全**：安全横幅"**这里学习的是中医文化和身体认知，不提供自行针刺或治疗指导**"；安全判断是强制关卡；侧栏安全提醒（"故事馆只用于中医文化科普和身体认知，不提供自行针刺或治疗指导"）。

### 12. ExtraCourseDetail.vue（210 行）
1. **路由/用途**：设计上供 `/fangfa/extra/:id` 使用（**但路由表中该路径已重定向到 `/safety`，当前为孤立页面**），课外课程详情 + 技能采集。
2. **交互**：左列三张图（toolsPic1–3，空则"暂无图片"），右列课程名/简介/长描述 + "采集"按钮（把关联技能加入背包）。
3. **API/Store**：`@/api/AcupunctureApi.getExtraCourseDetail`；`@/api/BackpackApi.addSkillToBackpack`；`useUserStore`（未登录拦截）；`resolveMediaUrl`。
4. **硬编码**：占位文案、采集提示。
5. **安全**：无。

### 13. Lianyan.vue（650 行）
1. **路由/用途**：路由表中**未挂载**（孤立页面；`/lianyan` 重定向 `/safety`），"练眼/炼药"小镇挑战玩法，跳转 `/ultimate-challenge`（也已重定向）。
2. **交互**：左栏小镇列表（最多 8 个小镇）；中上部剧情文案"XX 小镇上的居民被 XX 攻击了…"；居民特征 + 病症图；**选穴配药棋盘**：穴位槽（xueweicount 个）与手法槽（toolscount 个），点击槽位触发 `backpack-open` 事件打开全局背包选择技能（`backpack-skill-selected`/`backpack-close` 事件监听回填）；"开始"提交判定（所选技能 ID 集合与后端要求的 xuewei1-5/tools1-4 比对）；通过则发徽章（`awardIllnessBadge`）+ 播放 win.mp3 + "你真棒，恭喜通过本关，获得 10 分"弹窗，可"继续前进"下一镇；底部入口"前往终极考验"。
3. **API/Store**：`@/api/AcupunctureApi`（`listIllness`、`listSkillNamesByIds`、`awardIllnessBadge`）；`useUserStore`；`resolveMediaUrl`。
4. **硬编码**：小镇文案模板、通关奖励 10 分、终极考验入口文案（"随机 10 题检验综合知识"）。
5. **安全**："旧档案中记录了……等历史名称。**本页仅作文化档案辨认，不提供治疗或操作指导。**"

### 14. MeridianMatchGame.vue（1496 行，已分段完整读取）
1. **路由/用途**：`/meridian-match`（"经络消消看"，经络星河页内按钮直达），三消消除类游戏。
2. **交互**：10×10 棋盘，格子上显示穴位名（如"中府"）+ 经络简称，按经络颜色着色；**交换相邻两格**，3 个同经络连成一线即消除（含判定-回滚、下落重力、补格、连锁、无解自动洗牌）；4–5 连生成"行/列清除"特殊格，集齐某条经络全部穴位得"5×5 爆破"道具（左栏背包槽点击使用）；计分（每消 1 穴 +1 分 + 连击加成）、右栏连击显示与玩法规则；难度三档（简单 6 条/普通 10 条/困难 14 条经络，困难奖励 ×1.5）；奖励里程碑（50→经络星砂×2 … 1000→杏林叶×5，乘难度系数），达标弹窗领取。
3. **API/Store**：`@/api/GameApi.submitGameProgressEvent`（`gameCode:'meridian-match'`、`eventType:'TASK_COMPLETED'`、taskCode 含难度与分数、幂等键；成功后派发 `game-state-refresh` 事件）；`useUserStore.loadGameState`；未登录写 `localStorage('xinglin-game-state-v2')`；`useSoundEffects`。
4. **硬编码**：14 条经络数据（id/名称/简称/颜色/穴位序列：肺经[中府尺泽列缺太渊少商]、大肠经[商阳合谷曲池迎香]、胃经[四白地仓天枢足三里内庭]、脾经[隐白三阴交阴陵泉血海大包]、心经[极泉少海神门少冲]、小肠经[少泽后溪养老听宫]、膀胱经[睛明攒竹肾俞委中至阴]、肾经[涌泉太溪照海复溜]、心包经[天池曲泽内关中冲]、三焦经[关冲中渚外关翳风丝竹空]、胆经[瞳子髎风池肩井环跳阳陵泉足临泣]、肝经[大敦太冲曲泉期门]、任脉[关元气海神阙中脘膻中]、督脉[长强命门大椎风府百会]）；规则文案。
5. **安全**：无针刺内容（纯游戏化经络记忆）。

### 15. MeridianRiver.vue（3027 行，已分段完整读取）
1. **路由/用途**：`/jingluo`（"经络星河 · 侦探闯关"），主线经络学习页。
2. **交互**：
   - 顶部"解锁顺序"链条（14 个圆点，从左到右逐条解锁，完成数 x/14 金色填充）；
   - 左栏经络选择器按身体区域分组（上肢/下肢/躯干），锁定/解锁/已完成状态；
   - 中央星空板：SVG 连线（base/lit 双折线 + 发光滤镜）、120 颗闪烁星光、穴位星点（行星样式，编号/名称/拼音），侦探模式按循行顺序点亮（点错抖动并记入"星光修补册" `addReviewRecord`）；
   - **每点一颗星弹出问答关卡**（每条经络 2 题：穴位归属 + 经络走向，答对才能继续，可暂不回答退出）；
   - 模式开关（侦探闯关模式、显示归属关系"身体区域→经络→穴位"链）；
   - 底部：进度条 + 重新开始 / 退出保存 / 领取星河奖励（每条路线 星砂×1+铜片×1，全部完成另有 星砂×5+铜片×2）；
   - 7 步聚光灯新手教学（spotlight 高亮 + 定位 tooltip，首次进入播放，可重看"使用教学"）；
   - 全程拼音注音：`pinyinHtml()` 用 `<ruby>` 为经络名词加注音（如 手太阴肺经→shǒu tài yīn fèi jīng）。
3. **API/Store**：`useGameState`（`completeStandaloneTask`、`recordTaskProgress`（`resultCode:'ROUTE_POINT_COMPLETE'`）、`resetTaskProgress`、`addReviewRecord`、`isCompleted`、`getTaskProgress`、`isRewardClaimed`）；`useUserStore`；`localStorage` 进度键 `xinglin-meridian-river-progress-v4` / 领取键 `-claimed-v4` / 教学键 `xinglin-meridian-tutorial-seen`。
4. **硬编码**（大量）：14 条经络星路的完整数据——id、mark、名称、身体区域、简介、解锁条件（默认解锁或"完成前置经络"）、穴位坐标序列、**每条 2 道题（题干/4 选项/答案/解析）**：
   - 肺经：中府→尺泽→列缺→太渊→少商（少商位置/走向"从胸部走向手指"）；
   - 大肠经：商阳→合谷→曲池→迎香；胃经：四白→地仓→天枢→足三里→内庭；脾经：隐白→三阴交→阴陵泉→血海→大包；心经：极泉→少海→神门→少冲；小肠经：少泽→后溪→养老→听宫；膀胱经：睛明→攒竹→肾俞→委中→至阴；肾经：涌泉→太溪→照海→复溜；心包经：天池→曲泽→内关→中冲；三焦经：关冲→中渚→外关→翳风→丝竹空；胆经：瞳子髎→风池→肩井→环跳→阳陵泉→足临泣；肝经：大敦→太冲→曲泉→期门；任脉（阴脉之海）：关元→气海→神阙→中脘→膻中；督脉（阳脉之海）：长强→命门→大椎→风府→百会。
   - 解析中反复强调"这里只学习位置与归属""只进行位置观察"。
5. **安全**：页首声明"这里是儿童中医文化科普学习，**不展示真实针刺，也不替代医生指导**"。

### 16. Method.vue（1080 行）
1. **路由/用途**：路由表**未挂载**（孤立页面；`/fangfa` 已重定向 `/safety`），"针灸方法/器具"科普页。
2. **交互**：顶部导语；左侧垂直芯片列表（3D 环绕滚动：相对位置/透明度计算，悬停暂停自动轮播，每 3 秒自动切换）+ 右侧卡片堆叠（prev/next/active 缩放旋转）；点击主卡打开详情弹窗——内嵌三张图三页文本（`toolsPic1-3/toolsTitle1-3/toolsText1-3`）的 3D 翻页浏览 + "采集"技能入背包；下方"针灸还有更多神奇疗法"Bento 网格（extraCourse 卡片，最后一张"敬请期待"占位，点击跳 `/fangfa/extra/:id`）。
3. **API/Store**：`@/api/AcupunctureApi`（`listZhenjiuTools`、`listExtraCourse`）；`@/api/BackpackApi.addSkillToBackpack`；`useUserStore`；`resolveMediaUrl`。
4. **硬编码**：导语"针灸必不可少的就是'针'了，小朋友们，在这里我们将学习神奇的毫针、微针、特种针法器！"；轮播参数（AUTO 3000ms、ITEM_H 58px）。
5. **安全**：无显式免责声明（该页为遗留页，展示器具图片，未加"不自行操作"提示——**值得注意的缺口**）。

### 17. MyMistakes.vue（223 行）
1. **路由/用途**：`/my-mistakes`（requiresAuth，"我的错题"）。
2. **交互**：错题卡片列表（题目 #id、时间、题干、A–D 选项单选、提交答案）；答对自动从列表移除并提示，答错提示再试；空态"暂无错题，继续加油！"。
3. **API**：`@/api/QuizApi`（`getMyMistakes`、`submitMistakeAnswer`）。
4. **硬编码**：A–D 选项渲染逻辑、文案。
5. **安全**：无。

### 18. MyPosts.vue（303 行）
1. **路由/用途**：`/my-posts`（requiresAuth，"我的发帖"）。
2. **交互**：四个顶部 Tab（我的发帖/我的收藏/我赞过/我的评论），卡片列表（标题/作者·时间/摘要/我的评论内容），操作（❤️ 点赞、⭐ 收藏、💬 跳详情）。
3. **API**：`@/api/CommunityApi`（`getMyPostList`、`getMyCollectedPostList`、`getMyLikedPostList`、`getMyCommentList`、`postLike`、`postCollect`）。
4. **硬编码**：Tab 文案、时间格式化。
5. **安全**：无。

### 19. MyWorld.vue（1052 行）
1. **路由/用途**：`/myworld`（requiresAuth，"小侦探荣誉档案"）。
2. **交互**：左栏——档案卡（称号"铜人见习侦探"/"铜镜发光的新秀"（Lv≥3）、星光 x/50 进度、徽章墙 8 个展示位"查看全部"、徽章收集进度 x/18 + 荣誉宝箱图）；中栏——"本次探案报告"（汇总文案、推荐探索区域 5 个图标、当前材料袋前 4 种、推荐下一步"整理侦探社 · 点亮线索墙"跳 /agency）；右栏——学习数据（点亮星光、当前等级 Lv+名称+经验条、已记录探索、完成任务数）+ 下一步建议（经络星河/身体小地图/安全闯关）。
3. **API/Store**：`@/api/user`（`getCurrentUser`、`getMyBadges`）；`@/api/QuizApi.getUserQuizStats`；`useUserStore.getLevelInfo`；`useGameState`（`completedTaskIds`、`materials`、`badges`、`storyArchiveIds`）。
4. **硬编码**：档案总数 18；头衔文案；推荐任务文案；fallback 徽章（初探入门/线索达人/经络小能手/安全先锋/故事爱好者/侦探社档案员 + 6 个"未解锁"占位）。
5. **安全**：无针刺提示（纯荣誉展示）。

### 20. Review.vue（1516 行）
1. **路由/用途**：`/review`（"星光修补册"），错题温习/补星。
2. **交互**：左"我的未点亮知识星"列表（正确率、已修补星标）；中"修补任务"——对比卡（你之前选择的位置 vs 正确位置，图上红/绿标记点）+ 小铜人提示 + **修补小练习**（在示意图热点上点选正确位置）+ "开始修补"（答对调用奖励接口，点亮 5 星动画）；右栏修补进度环 + 本次奖励（经络地图线索 ×1）+ 智能推荐任务（3 条轮换"换一批任务"）；弹窗"全部知识星"。
3. **API/Store**：`useGameState.completeStandaloneTask('review-daily', [{id:'star-compass',count:1}], {resultCode:'REVIEW_COMPLETE', idempotencyKey})`；`isCompleted`。
4. **硬编码**：8 条复习记录——4 条主体：**足三里位置**（正确点"膝盖下三寸，胫骨外侧一横指"，手指量法提示）、**经络归属**（足三里属足阳明胃经）、**身体区域判断**（足三里在小腿前外侧）、**安全规则复习**；4 条扩展：**内关穴位置/经络循行方向/穴位功能记忆/学习边界判断**；每条的错/对标记坐标、3 个练习热点、提示语；`body.review-active` 时重排右侧 FAB（背包/邮箱/机器人/安全铃铛）样式。
5. **安全**：安全复习条目标配"**只观察、触摸和学习，针刺必须交给专业医生**""绝对不能自己拿针或尖锐物品尝试"；修复完成后文案"安全知识要记在心里"。

### 21. Safety.vue（152 行）
1. **路由/用途**：**未挂载孤立页**（`/safety` 实际渲染 SafetyGame.vue），"安全守护案"静态入口页。
2. **交互**：Hero（"只观察、只学习，不自己针刺"+ 安全铃铛图）→ 3 张规则卡 → 安全问答入口按钮跳 `/quiz-game`。
3. **API/Store**：`useRouter`；`generatedRewardAssets.detectiveSafety`。
4. **硬编码**：3 条规则——**不能自己针刺**（"针刺必须由专业人员操作，儿童不能自己尝试，也不能给同学尝试"）、**只观察和学习**、**不替代医生**（"身体不舒服时要告诉家长，并由医生判断和处理"）。
5. **安全**：本页即安全教育的纯内容页。

### 22. SafetyGame.vue（1324 行，已分段完整读取）
1. **路由/用途**：`/safety`（"安全小课堂 · 安全守护者训练营"），四关安全游戏。
2. **交互**（四关串联，intro 页展示关卡流程）：
   - **第一关·知识闯关**：判断题 + 场景模拟题（数据来自 `@/data/safetyQuestionsGame`），3 条生命（答错扣 1）、连击加成（100 基础分+连击×20，上限 ×5）、星星进度条；生命耗尽出现"生命值耗尽"重试卡；
   - **第二关·记忆翻牌**：记忆配对（`safetyQuestions` 中 memory 型 `pairs` 左右配对），翻牌计数、配对成功 +80 分、全部完成 +400+（30-翻牌数）×10；
   - **第三关·行为分类**：60 秒倒计时，把行为卡拖拽/点击分入**安全行为/需成人帮助/不安全行为**三个篮子，检查结果（正确 +400+剩余秒×5，错误列出错项可重分）；
   - **第四关·安全宣誓**：逐条点亮 6 盏灯（每条 +50 分），全亮后"安全小卫士"颁证（含成就列表、最终得分、奖励**安全铃铛×5 + 艾绒×3**）；最终完成页统计 + 解锁成就 + 再次训练/返回地图。
3. **API/Store**：`useGameState`（`completeStandaloneTask('main-safety-case', [], {resultCode:'SAFETY_COMPLETE', idempotencyKey:'main-safety-case:lifetime'})`；`addMaterials(safetyPledge.rewards, 'daily-safety-quiz', {progressDelta:3, resultCode:'QUIZ_COMPLETE'})`）；`generatedMaterialIcons/generatedRewardAssets` 素材。
4. **硬编码**：四关规则文案（"答对得分，连击加成/3条生命/翻牌配对/分类关卡限时60秒"）；6 条宣誓（**不自行针刺/不模仿治疗/不替代医生/及时求助/不碰针具/传播安全**，每条有完整承诺语）；成就条件逻辑（`achievements` 数据在 data 模块）；粒子特效。
5. **安全**：整页即安全教育——开场页底部"只观察、只学习，不自行针刺或模仿治疗"；分类关结果解释明确"不安全行为：绝对不能做的事——自己扎针、模仿治疗、玩针具"；宣誓关逐条强化安全边界。

### 23. Settings.vue（32 行）
1. **路由/用途**：`/settings`（requiresAuth，"其他设置"）。
2. **交互**：`a-card` 包裹并直接内嵌 `@/views/profile/index.vue`（`<ProfilePage />`）。
3. **API/Store**：委托给 profile/index.vue。
4. **硬编码**：无。
5. **安全**：无。

### 24. ShuntingGame.vue（103 行）
1. **路由/用途**：`/shunting-game`（"经络小火车"）。
2. **交互**：`<iframe>` 内嵌独立静态小游戏 `<base>/shunting/index.html?v=20260407`（同源，`referrerpolicy="same-origin"`）；监听 `message` 事件（校验来源 frame 与 origin）收到 `beaver-shunting-win` 后调用奖励接口（2.5 秒防抖）——登录用户**气血能量 +10**。
3. **API/Store**：`useUserStore.claimShuntingReward`。
4. **硬编码**：消息类型 `beaver-shunting-win`、奖励 10、防抖 2500ms、iframe URL 版本号。
5. **安全**：未登录提示"登录后通关可自动获得 10 点气血能量"。

### 25. UltimateChallenge.vue（420 行）
1. **路由/用途**：**未挂载孤立页**（`/ultimate-challenge` 重定向 `/safety`；但 quiz-game 的"返回互动中心"仍指向它），"终极考验"随机 10 题综合测验。
2. **交互**：intro（"随机出 10 道题目，全部答对有惊喜"）→ 答题（题干框 + A–D 选项 + 提交，服务端判题）→ 每题弹窗（答错显示正确答案/解析/所属板块）→ 结果小结（正确/错误/正确率，≥8 题提示"已达到小奖状条件"）→ 再来一次；播放 `/shunting/win.mp3` 音效。
3. **API/Store**：`@/api/QuizApi`（`getQuizQuestions({count:10})`、`submitQuizAnswer`）；`useUserStore`（未登录拦截跳 `/auth/login`）。
4. **硬编码**：题数 10、小奖状阈值 8、选项字母 A–D、文案。
5. **安全**：无。

### 26. AcupointSortGame.vue（1308 行）
1. **路由/用途**：`/acupoint-sort`（"穴位排序大师"），经络星河页内按钮直达。
2. **交互**：共 5 轮、每轮随机一条经络、每轮 60 秒限时；把打乱的穴位卡按循行顺序点入路径空槽（点卡→点槽，可移除/清空重填）；**提示**按钮自动把首个错误槽位填对（每轮一次）；提交后逐槽对错判定（✓/✗ + 正确名）；计分：每正确穴位 +10、剩余秒×2、全对额外 +20；结果页逐轮明细 + 奖励里程碑（50→经络星砂×1、100→草药叶×2、150→杏林叶×1、200→竹简碎片×2、250→经络星砂×3、300→草药叶×3），达标即发。
3. **API/Store**：`@/api/GameApi.submitGameProgressEvent`（`gameCode:'meridian-sort'`、taskCode `meridian-sort-{score}`、幂等键、派发 `game-state-refresh`）；`useUserStore.loadGameState`（同步服务端已领里程碑）；未登录写 `localStorage('xinglin-game-state-v2')`；`useSoundEffects`。
4. **硬编码**：14 经络完整数据（与消消看一致，含颜色）；计分常量（`TOTAL_ROUNDS=5`、`TIME_PER_ROUND=60`、`POINTS_PER_CORRECT=10`、`TIME_BONUS_MULTIPLIER=2`、`PERFECT_BONUS=20`）；结果表情/称号（≥250 穴位大师 🏆 等）。
5. **安全**：无（纯游戏化记忆训练）。

---

## 三、社区模块（`vue3\src\views\frontend\community\`）

### 27. community/index.vue（972 行）
1. **路由/用途**：`/community`（"悄悄话信箱"），儿童社区发帖/问答/反馈。
2. **交互**：顶部三个板块 Tab（**问答专区 / 分享专区 / 公告与反馈**）；左侧帖子列表（搜索、分页、点赞 ❤️、收藏 ⭐、评论 💬、举报 🚩）；"公告与反馈"Tab 额外显示右侧**意见箱**（textarea 提交，自动转标题 `【反馈】...` 发布为"反馈"类型帖子）；发帖弹窗（板块单选——管理员才可选"公告"、标题 ≤200、正文、最多 5 张图上传）；详情抽屉（正文+图+点赞/收藏/举报+评论区）；支持 `?postId=`、`?type=`、`?keyword=`、`?openPublish=1`（含 sessionStorage 预设）URL 直达。
3. **API/Store**：`@/api/CommunityApi`（`getPostPage`、`getPostDetail`、`createPost`、`postLike`、`postCollect`、`postReport`、`addComment`）；`@/api/FileApi.uploadBusinessFile`（POST_CONTENT）；`useUserStore`（登录态/`isAdmin`）。
4. **硬编码**：板块常量（`问答专区/分享专区/公告与反馈`、API 侧 `公告/反馈`）；相对时间格式化（刚刚/N 分钟前/N 小时前/日期）。
5. **安全**（儿童隐私保护重点）：发帖弹窗内置警示"**只分享文化学习体验：不要发布诊断、治疗或操作建议，也不要填写电话、住址、学校和社交账号**"；评论区提示"**评论区只交流学习体验，请保护个人隐私，不提供医疗建议**"；举报机制。

### 28. community/misconceptions.vue（291 行）
1. **路由/用途**：`/community/misconceptions` → 路由重定向 `/community`（页面本身为"误区森林"问答手风琴）。
2. **交互**：`a-collapse` 手风琴展示"问题→答案"列表，数据来自接口。
3. **API**：`@/api/CommunityApi.getMisconceptionList`。
4. **硬编码**：标题"误区森林"、描述"点击问题探索答案，避开误区吧！"、装饰 emoji。
5. **安全**：无；**注意**：该文件多处中文呈乱码（UTF-8 编码损坏，如"閿欓妫灄"/"鏆傛棤绛旀"），属历史遗留编码问题。

### 29. community/qa.vue（25 行）
1. **路由/用途**：`/community/qa`，纯重定向——`onMounted` 跳 `router.replace({path:'/community', query:{...route.query}})`，"正在跳转…"占位。
2. **API/Store**：`useRoute/useRouter`。
3. **硬编码**：占位文案。
4. **安全**：无。

---

## 四、其他页面与文件

### 30. frontend/quiz-game/index.vue（927 行）
1. **路由/用途**：`/quiz-game`（requiresAuth，"答题闯关"安全问答）。
2. **交互**：ready 页（规则：随机 5 题、4 选 1、记录正确率）→ 答题（进度条、难度标签 简单/中等/困难、A–D 选项、提交后服务端判题并显示 ✓/✗ 与解析，答错显示解析）→ 结果页（正确率环形图、🏆/😊/🤔/💪 表情、逐题对错明细）；"返回互动中心"→ `/ultimate-challenge`。
3. **API**：`@/api/QuizApi`（`getQuizQuestions({count:5})`、`submitQuizAnswer`——写入 user_quiz_record/stats）。
4. **硬编码**：题数 5、难度映射、结果文案（"太棒了！你对针灸知识掌握得非常扎实！"等）。
5. **安全**：作为安全课堂的问答环节存在（题目数据由题库提供，多为安全判断题）。

### 31. profile/index.vue（1254 行）
1. **路由/用途**：`/profile`（requiresAuth 前台"个人设置"）与后台 `/back/profile`（"管理员信息"）共用；`Settings.vue` 亦内嵌本页。
2. **交互**：Tab1 基本信息——头像（自定义上传：`POST /file/upload`，businessType=USER_AVATAR、businessId、businessField=avatar、replaceOld=true，JPG/PNG ≤2MB 校验，上传后调 `updateUser` 保存并更新 store）、用户名（禁用）、姓名、性别（男/女）、邮箱、手机号（`^1[3-9]\d{9}$` 校验）、保存修改；Tab2 修改密码——旧密码/新密码/确认（≥6 位、两次一致），成功后 `Modal.info` 提示"为了您的账户安全，请重新登录"，`logout()` 后跳 `/auth/login`（3 秒自动跳转兜底）。
3. **API/Store**：`@/api/user`（`getCurrentUser`、`updateUser`、`updatePassword`）；`@/api/QuizApi.getUserQuizStats`（答题统计加载，但对应 Tab 已移除，仅保留计算逻辑）；`request` 直连 `/file/upload`；`useUserStore`。
4. **硬编码**：校验规则、上传约束、密码规则；CSS 中残留"我的作品/收货地址/我的数据"样式（功能已注释移除，注释注明"已移除旧版作品个人管理能力"）。
5. **安全**：无针刺内容；含账户安全（改密后强制重新登录）。

### 32. error/404.vue（22 行）
1. **路由/用途**：`/404`，且 `/:pathMatch(.*)*` 兜底重定向至此。
2. **交互**：404 + "页面未找到" + "返回首页"链接（`to="/"`）。
3. **API/Store**：无。
4. **硬编码**：文案。
5. **安全**：无。

### 33. MockTest.vue（121 行）
1. **路由/用途**：**未挂载**（开发用"Mock 数据测试页面"）。
2. **交互**：显示 Mock 状态（`import.meta.env.DEV && VITE_USE_MOCK==='true'`）；登录测试表单（默认 admin/123456）调用 `login` 并把耗时/返回 JSON 写入结果区；页载显示环境信息与测试说明（管理员 admin/123456、普通用户 user001/123456、错误测试用其他账号）。
3. **API**：`@/api/user.login`。
4. **硬编码**：测试账号。
5. **安全**：无。

### 34. deepseekai.txt（71 行）
1. **路由/用途**：**非页面**——后端 SpringBoot 接入 DeepSeek 的备忘文档（放在前端 views 目录下的文本文件）。
2. **内容**：Maven 依赖（Hutool 5.8.20）；`application.yml`（`ai.deepseek.api-key/endpoint: https://api.deepseek.com/v1/chat/completions`，含 qwen 配置）；`ChatMessage`/`AiRequestBody` 参数类；`DeepSeekClient`（HttpRequest POST + Bearer 鉴权 + 解析 `choices[0].message.content`）；`AiController /api/ai/chat?prompt=` 示例。
3. **安全**：无儿童相关。

---

## 五、总体结论与发现

1. **安全设计体系**：教育页面普遍三层安全处理——① 常驻标语/横幅（"只观察、只学习，不自己针刺"）；② 每个学习区域附针对性安全语（身体地图按区域、故事馆安全关、安全游戏宣誓、图鉴 safetyTip）；③ 注册环节强制"和家长一起确认"+家长邮箱绑定。社区页额外含儿童隐私保护（不发诊断/治疗建议、不填电话住址学校、举报机制）。
2. **孤儿/遗留页面**：`Method.vue`、`Lianyan.vue`、`UltimateChallenge.vue`、`Safety.vue`、`MockTest.vue`、`ExtraCourseDetail.vue` 在 `router/index.js` 中未挂载或已被重定向（如 `/fangfa`、`/lianyan`、`/ultimate-challenge` 均重定向到 `/safety`），属于保留代码；其中 **Method.vue 展示针灸器具却缺少"不自行操作"免责声明**，若重新启用需补安全提示。
3. **数据分布**：经络/穴位/题目/故事/徽章/材料/成就等大量内容硬编码在页面 script 或 `@/data/*`（storyCases、safetyQuestionsGame、generatedRewardAssets）中；运行期知识内容（铜人每日探案、图鉴、课外课程、题库、社区帖）来自后端 API。
4. **进度与奖励**：统一走 `useGameState`（任务代码如 `main-hand-star-map`、`main-safety-case`、`review-daily`、`daily-read-copper-story`、路由 ID 等）+ 幂等键（idempotencyKey）防重复发放，材料通过 `completeStandaloneTask/addMaterials/recordTaskProgress` 结算，未登录时降级到 localStorage。
5. **技术栈**：Vue 3 `<script setup>` + vue-router 4 + Pinia（user store）+ ant-design-vue + Font Awesome + Three.js（小铜人 3D）+ 原生 Canvas/SVG/CSS 动画 + 音效（win.mp3、useSoundEffects）+ 拼音 `<ruby>` 注音。

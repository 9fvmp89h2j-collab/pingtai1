# 《小铜人中医侦探社》（杏林小药师）Vue 3 前端项目技术总结报告

**项目根目录**：`D:\总\Pediatric Acupuncture Education System\pingtai1\vue3`
**项目定位**：面向儿童的中医文化（针灸/经络/穴位）科普与互动学习平台，含前台儿童端（故事探案、经络游戏、安全问答、社区）与后台运营管理端（内容库、社区管理、用户管理）。报告基于全部指定文件的完整源码逐文件分析。

---

## 一、技术栈与构建配置

### 1.1 核心依赖（`vue3\package.json`）

| 类别 | 依赖 | 版本 | 用途 |
|---|---|---|---|
| 框架 | `vue` | ^3.2.13 | 前端框架（Composition API + `<script setup>`） |
| 路由 | `vue-router` | ^4.0.3 | 路由（hash 模式） |
| 状态管理 | `pinia` | ^3.0.0 | 全局状态（app / user 两个 store） |
| | `vuex` | ^4.1.0 | **已安装但未使用**（遗留依赖） |
| UI 组件库 | `ant-design-vue` | ^4.0.0 | 后台管理 + 部分前台组件（`a-layout`、`a-config-provider`、`message`、`Modal` 等） |
| 图标 | `@ant-design/icons-vue` | ^7.0.1 | AntD 图标 |
| | `@fortawesome/fontawesome-free` | ^7.1.0 | FontAwesome 图标（后台导航图标 `fas fa-*`） |
| HTTP | `axios` | ^1.7.9 | 请求库（封装在 `utils/request.js`） |
| 3D | `three` | ^0.178.0 | 小铜人 3D 模型（`utils/chibiCopperMan.js` 程序化建模） |
| 图表 | `echarts` | ^6.0.0 | 后台仪表盘统计图表 |
| 图片生成 | `html2canvas` | ^1.4.1 | 荣誉证书海报生成（`utils/userCertificate.js`） |
| Markdown | `marked` | ^16.4.0 | 富文本渲染 |
| SSE | `@microsoft/fetch-event-source` | ^2.0.1 | 流式请求（预留） |
| 其他 | `core-js` / `esbuild` | ^3.8.3 / ^0.25.11 | Polyfill / 构建 |

**开发依赖**：`vite` ^4.5.14、`@vitejs/plugin-vue` ^4.6.2、`unplugin-vue-components` ^32.1.0（按需引入 AntD 组件）、`eslint` ^8.57.0 + `eslint-plugin-vue` ^9.22.0（`src/views/frontend/shunting/**` 被 eslintIgnore）、`sass` ^1.89.2、`less` ^4.4.2。

**脚本命令**：
- `dev`：`vite --mode development`
- `build` / `build:staging`：production / staging 模式构建
- `validate:game`：`node scripts/validate-game-consistency.mjs`（游戏数据一致性校验）
- `validate:content`：`node ../scripts/validate-copper-content.mjs`（铜人内容校验）
- `lint` / `lint:fix`：ESLint 检查

### 1.2 Vite 构建配置（`vue3\vite.config.mjs`）

- **插件**：`vue()` + `Components({ dirs: [], resolvers: [AntDesignVueResolver({ importStyle: false })] })` —— 组件按需自动引入，不自动扫描本地组件目录。
- **别名**：`@` → `<root>/src`（`resolve.alias`）。
- **开发服务器**：host `127.0.0.1`、端口 `8800`、`open: true`、`compress: false`。
- **代理**（关键）：
  - `/api` → `http://localhost:8889`（后端服务端口 8889）
  - `/files` → `http://localhost:8889`（媒体文件访问）
- **SCSS 配置**：使用 `modern-compiler` API，忽略 legacy-js-api 弃用警告。
- **define 注入**：`import.meta.env.VITE_APP_BASE_API` 固定为 `'/api'`（所有接口请求以此为 baseURL 前缀）。

### 1.3 HTML 入口（`vue3\index.html`）

- 中文站点（`lang="zh-CN"`），标题「小铜人中医侦探社」，含儿童平台描述 SEO 元信息，`<noscript>` 提示需开启 JavaScript，挂载点 `#app`，入口 `/src/main.js`。

### 1.4 应用入口与根组件

**`src/main.js`**：`createApp(App)` → 注册 `pinia` → 注册 `router` → **全局替换 `console.error` 以静默过滤 `ResizeObserver loop` 警告** → `app.mount('#app')`。引入 `ant-design-vue/dist/reset.css` 与 FontAwesome 全量 CSS。**注意：`src/styles/global.css` 并未在 main.js 中显式全局引入**，而由各页面/布局自行引入。

**`src/App.vue`**：仅 `<router-view />` 根路由出口，样式为空壳（`#app {}`）。

---

## 二、路由表完整清单（`src/router/index.js`）

使用 `createWebHashHistory()`（**hash 模式**，URL 形如 `/#/home-map`）。`scrollBehavior`：保存位置优先 → 锚点平滑滚动 → 回顶部。路由分三组拼接：`frontendRoutes` + `backendRoutes` + `errorRoutes`。`backendRoutes` 是具名导出的常量（`export const backendRoutes`），供外部复用。

### 2.1 后台路由（`/back`，父组件 `BackendLayout`，重定向 `/back/dashboard`）

| path | name | 组件（懒加载） | meta.title | meta.icon |
|---|---|---|---|---|
| `/back` → redirect `/back/dashboard` | — | BackendLayout | — | — |
| dashboard | Dashboard | views/backend/Dashboard.vue | 首页 | HomeFilled |
| user | UserManagement | views/backend/user/index.vue | 用户管理 | User |
| start-page | StartPageManagement | views/backend/start-page/index.vue | 开场故事管理 | BookOutlined |
| doctor-story | DoctorStoryManagement | views/backend/doctor-story/index.vue | 名医故事管理 | BookOutlined |
| jingluo | JingluoManagement | views/backend/jingluo/index.vue | 经络管理 | BookOutlined |
| mainline-level | MainlineLevelManagement | views/backend/mainline-level/index.vue | 主线关卡 | BookOutlined |
| train-game | TrainGameManagement | views/backend/train-game/index.vue | 小火车关卡管理 | BookOutlined |
| xuewei | XueweiManagement | views/backend/xuewei/index.vue | 小铜人内容中心 | BookOutlined |
| quiz-question | QuizQuestionManagement | views/backend/quiz-question/index.vue | 题库管理 | BookOutlined |
| community-post | CommunityPostManagement | views/backend/community-post/index.vue | 帖子管理 | BookOutlined |
| community-comment | CommunityCommentManagement | views/backend/community-comment/index.vue | 评论管理 | BookOutlined |
| community-feedback | CommunityFeedbackManagement | views/backend/community-feedback/index.vue | 反馈消息 | BookOutlined |
| profile | BackendProfile | views/profile/index.vue | 管理员信息 | UserFilled |

### 2.2 前台路由（`/`，父组件 `FrontendLayout`，重定向 `/home-map`）

| path | name | 组件 | meta.title | 备注 |
|---|---|---|---|---|
| /landing | LegacyLandingRedirect | — | 杏林小药师 | redirect → /home-map |
| home-map | HomeMapLanding | HomeMapLanding.vue | 杏林探险地图 | 主页地图 |
| agency | AgencyHome | Agency.vue | 小铜人侦探社 | |
| /index.html | IndexLanding | — | — | redirect → /home-map |
| doctor-story | DoctorStory | DoctorStory.vue | 故事馆 | |
| body-map | BodyMap | BodyMap.vue | 身体地图 | |
| jingluo | Jingluo | MeridianRiver.vue | 经络星河 | |
| meridian-match | MeridianMatch | MeridianMatchGame.vue | 经络消消看 | |
| acupoint-sort | AcupointSort | AcupointSortGame.vue | 穴位排序大师 | |
| shunting-game | ShuntingGame | ShuntingGame.vue | 经络小火车 | |
| copper-man | CopperMan | CopperMan.vue | 小铜人馆 | **requiresAuth: true** |
| shuxue / fangfa / fangfa/extra/:id / lianyan / ultimate-challenge | — | — | 安全课堂 | 均 redirect → /safety |
| safety | Safety | SafetyGame.vue | 安全课堂 | |
| /quiz-game | QuizGame | quiz-game/index.vue | 安全问答 | **requiresAuth: true** |
| review | Review | Review.vue | 星光修补册 | |
| clinic | — | — | 侦探社 | redirect → /agency |
| bag | Bag | Bag.vue | 材料背包 | **requiresAuth: true** |
| badges | Badges | Badges.vue | 故事收藏墙 | |
| /community | Community | community/index.vue | 悄悄话信箱 | |
| /community/qa | CommunityQa | community/qa.vue | 社区问答 | |
| /community/misconceptions、/community/guide、/community/guide/:id | — | — | — | redirect → /community |
| profile | Profile | views/profile/index.vue | 个人设置 | **requiresAuth: true** |
| myworld | MyWorld | MyWorld.vue | 我的信息 | **requiresAuth: true** |
| my-posts | MyPosts | MyPosts.vue | 我的发帖 | **requiresAuth: true** |
| my-mistakes | MyMistakes | MyMistakes.vue | 我的错题 | **requiresAuth: true** |
| settings | Settings | Settings.vue | 其他设置 | **requiresAuth: true** |

### 2.3 认证路由（`/auth`，父组件 `AuthLayout`）

| path | name | 组件 | meta.title |
|---|---|---|---|
| /auth/login | Login | views/auth/Login.vue | 登录 |
| /auth/register | Register | views/auth/Register.vue | 注册 |
| /auth/forgot-password | ForgotPassword | views/auth/ForgotPassword.vue | 找回密码 |

另有三个顶层重定向：`/login → /auth/login`、`/checkin → /auth/register`、`/register → /auth/register`。

### 2.4 错误路由

`/404`（name `'404'`，views/error/404.vue）与通配 `/:pathMatch(.*)*` → redirect `/404`。

### 2.5 全局前置守卫 `router.beforeEach`（权限与解锁逻辑，重点）

执行顺序：
1. **页面标题**：`to.meta.title` 存在时设置 `document.title = "${title} - 杏林小药师"`。
2. **后台权限**：`to.path.startsWith('/back')` 时：
   - 未登录 → `next('/auth/login?redirect=to.fullPath')`；
   - 已登录但非管理员（`!userStore.isAdmin`）→ `next('/home-map')`；
   - 否则放行。
3. **requiresAuth 检查**：目标路由匹配链上任意 `meta.requiresAuth` 且未登录 → 跳登录页（带 redirect 回跳参数）。
4. **主线关卡解锁检查**（`utils/navUnlock.js` 的 `findNavUnlockByPath`）：若命中解锁规则且非管理员、非公开页：
   - 未登录 → 提示「请先登录后再访问该功能」并跳登录；
   - 调用 `userStore.loadGameState({ showDefaultMsg: false })` 拉取服务端权威游戏状态，失败则提示「地图状态暂时无法确认」回 `/home-map`；
   - 找到 `gameState.levels` 中 `id === navRule.levelId` 的关卡，若 `!currentLevel?.unlocked` → 提示「请先完成『前置关卡』」并回 `/home-map`。
5. **已登录访问登录页**：跳回 `/back/dashboard`（管理员）或 `/home-map`（普通用户）。
6. 默认 `next()` 放行。

**动态路由**：本项目中未使用 `router.addRoute` 动态注册，路由为静态全量注册；「动态」体现在后台导航数据（`config/adminNavigation.js`）由配置驱动渲染，而非动态路由注入。

---

## 三、Pinia Store

### 3.1 `src/store/app.js`（useAppStore）

- **state**：`sidebarCollapsed: false`（后台侧边栏折叠状态）。
- **actions**：`toggleSidebar()` 翻转折叠状态。
- 无 getters。极简全局 UI 状态。

### 3.2 `src/store/user.js`（useUserStore）——核心用户/认证/游戏状态

自带安全 localStorage 包装 `storage`（get 解析失败自动清除损坏数据并返回 null）。

**state**：
- `userInfo`：从 localStorage `userInfo` 初始化；
- `token`：从 localStorage `token` 初始化（默认 `''`）；
- `cachedLevelInfo`：气血等级缓存（接口 `/user/level/{id}`），用于导航解锁与「我的小世界」；
- `gameState`：服务端权威游戏状态缓存。

**getters**：
- `isLoggedIn`：`!!token && !!userInfo`（登录判定基准）；
- `userType`：`userInfo?.userType || ''`；
- `isAdmin`：`userType === 'ADMIN'`；
- `isUser`：`userType === 'USER'`；
- `displayName`：nickname → username → '用户'（未登录返回'未登录'）；
- `avatar`、`userId`、`userScore`（气血能量，即 `score`）。

**actions**（均为 async）：
- `initialize()`：未登录则清空本地用户信息。
- `setUserInfo(data)`：校验 `data.token` 非空，写入 `userInfo`/`token` 并双写 localStorage。
- `updateUserInfo(data)`：合并 `{...userInfo, ...data}`（不更新 token），同步 localStorage。
- `clearUserInfo()`：清空 userInfo/token/cachedLevelInfo/gameState 及 localStorage。
- `login(loginForm)`：调 `api/user.login` → 校验 token → `setUserInfo` → **`migrateLegacyGameState()`（游客本地进度迁移）** → `loadGameState()` → `ensureLevelInfo()`；失败则 `clearUserInfo()` 并抛错。
- `logout()`：有 token 时调 `api/user.logout`（失败吞掉），无论如何 `clearUserInfo()`。
- `claimShuntingReward(requestConfig)`：领取调车挑战每日固定奖励；响应 `id === userId` 时合并更新用户信息，随后 `ensureLevelInfo()` 并派发 `window` 级 `user-level-refresh` 自定义事件（供导航栏刷新等级）。
- `ensureLevelInfo(requestConfig)`：拉取 `/user/level/{id}` 缓存到 `cachedLevelInfo`，同步 `userInfo.score`；失败回退旧缓存。
- `loadGameState(requestConfig)`：调 `GameApi.getGameState` 存 `gameState`，派发 `game-state-refresh` 事件（detail 为 state），并同步 score。
- `migrateLegacyGameState()`：读取 localStorage `xinglin-game-state-v3`/`v2`、`xinglin-meridian-river-progress-v4`/`claimed-v4`、`meridian-match-claimed`、`meridian-sort-claimed`、`copper-man-case:*` 等旧键，汇总为任务清单 `tasks[]`（含每日阅读、安全问答、十四经脉路线进度、消消看/排序挑战分数档位、铜人每日探案等），调用 `importLegacyGameState({ schemaVersion: 4, sourceKey: 'xinglin-game-state-v4', tasks }, { idempotencyKey: 'legacy-import:{userId}:v4' })` 一次性导入，保证幂等。
- `getLevelInfo()`：同 `ensureLevelInfo` 但不带静默配置（会抛错给调用方）。

---

## 四、配置模块（`src/config/`）

### 4.1 `api.js`
- `API_BASE_URL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080'`（Vite 已注入 `/api`）。
- `API_ENDPOINTS`：定义了 USER（login/register/current/update/password）与 FILE（upload/preview/download/delete）两组端点常量。**注意：这是较旧的端点常量，实际业务接口大多直接写在各 `api/*.js` 中（路径不带 `/api` 前缀，由 axios baseURL 统一加前缀），此文件当前更多是历史/兜底配置。**

### 4.2 `site.js`（站点全局配置）
- 站点名「杏林小药师：经络探险记」、短名「杏林小药师」、slogan、logo（`/src/assets/home_cat.png`）。
- 后台品牌「杏林运营台 / 运营台」。
- 版权（© 2026）、联系方式、footerLinks（关于我们/隐私政策/用户协议/联系我们）、SEO 关键词。
- **主题色板**（CSS 变量同源）：主色 `#2F7D68`、次色 `#DFF2D8`、强调 `#FFD35A`、背景 `#FFF8E8`、纸色 `#FFF7DF`、铜色 `#B87333`/`#6F421F`、安全绿 `#54B6A1`、蓝 `#5AA7D8`、三级文字色（`#243B34`/`#66756D`/`#8A958E`）。
- 工具函数：`getCopyright()`、`getSiteTitle()`、`getAdminTitle()`。

### 4.3 `adminNavigation.js`（后台导航配置 + 工具）
- 导航树：首页（dashboard）、用户管理（user）、**内容库**（start-page 开场故事 / doctor-story 名医资料 / jingluo 经络 / xuewei 小铜人内容中心 / train-game 小火车关卡 / mainline-level 主线关卡 / quiz-question 题库）、**社区管理**（community-post 帖子 / community-comment 评论 / community-feedback 反馈消息）、管理员信息（profile）。图标均为 FontAwesome `fas fa-*`。
- 工具函数：`findAdminNavigation(path)`（含父子查找）、`getAdminBreadcrumbs(path)`（面包屑：首页 + 父级 + 当前项）、`getAdminOpenKeys(path)`（展开的 SubMenu key）。

---

## 五、布局组件

### 5.1 `AuthLayout.vue`（登录/注册/找回密码外壳）
- **注册页特殊处理**：`route.name === 'Register'` 时 `v-if` 直接渲染 `<router-view />`（注册表单独立全屏，不加装饰）。
- 登录/找回密码共用双栏布局：顶栏（品牌「小铜人·中医侦探社」+「身份核验处」徽章 + 切换链接）+ 左侧故事引导卡（`pageCopy` 随页面动态切换文案，含 CheckCircleFilled/SafetyCertificateFilled 三点安全提示）+ 右侧表单容器（`<router-view />`）+「暂不登录，返回探险地图」链接。
- 背景使用地图底图 + 渐变蒙层 + backdrop-filter；`prefers-reduced-motion` 降低动效；提供 860px / 560px 响应式断点与 `:focus-visible` 无障碍焦点样式。
- 引用了 `@/assets/copper-detective-2026/detective-standard.png`、`detective-welcome.png` 两张 IP 形象图。

### 5.2 `BackendLayout.vue`（后台外壳）
- `<a-config-provider :locale="zhCN">` 提供 AntD 中文语言包。
- 结构：`<Sidebar />`（`components/backend/Sidebar.vue`）+ 主区域（`<Navbar @logout="handleLogout" />` + `<router-view>` 带 `fade out-in` 过渡）。
- 引入 `@/styles/admin-desktop.scss`。
- `handleLogout`：`userStore.clearUserInfo()` + `router.push('/login')`（注意：直接清本地，不走 store.logout 的 API）。
- 自带 SCSS：flex 布局 100vh、`.content-container` 滚动区（自定义 6px 细滚动条）。

### 5.3 `FrontendLayout.vue`（前台外壳）
- `<a-layout>` 内按 `!isHomeMap` 条件渲染：顶部 `Navbar`、`CommunityMailboxFab`（悄悄话信箱悬浮球）、`RobotAssistantFab`（机器人助手常驻入口）、`SafetyBell`（安全铃，`auto-open` 在非经络星河页为 true）。
- `isHomeMap`：route.name ∈ {HomeMapLanding, AgencyHome} 或 path ∈ {/home-map, /agency}（地图页隐藏导航，沉浸式全屏）。
- `isMeridian`：经络星河页（Jingluo）时安全铃不自动展开。
- 内容区 `margin-top: 56px` 留出导航高度，地图页 `margin-top: 0` 全屏；`--site-background` 变量控制底色。其余为响应式 + footer 遗留样式。

---

## 六、工具模块（`src/utils/`）逐文件说明

### 6.1 `auth.js`（认证工具）
- `initAuth()`：应用启动调用 `userStore.initialize()`；若 token 存在但 `isLoggedIn` 为假（token 失效）→ 清空并 `redirectToLogin()`。
- `redirectToLogin()`：已在 `/auth/` 下不重复跳转，否则 `router.push('/auth/login?redirect=当前fullPath')`。
- `requireAuth()`：未登录跳登录页并返回 false（组件/路由守卫用）。
- `safeLogout()`：调 `userStore.logout()`，finally 一律跳 `/auth/login`。
- `getCurrentUser()`：安全包装，返回 `{id, name, userType, avatar, isAdmin, isUser}`。
- `roleCheck`：`isAdmin()` / `isUser()` / `hasUserType(type)`。

### 6.2 `chibiCopperMan.js`（Three.js 程序化 3D 小铜人）
- 纯代码建模：`scaledSphere`/`torus`/`flattenedCylinder`/`organicLimb`（CatmullRomCurve3 + Frenet 帧管状肢体）/`LatheGeometry` 躯干，组装头（耳、发际线、五官、微笑嘴）、躯干、手臂（含手指、护腕 torus）、腿脚（脚趾、脚踝环）。
- 材质：青铜色 `MeshPhysicalMaterial`（金属度 0.34）+ 深棕、铜绿 `patina`、眼白/虹膜/黑瞳 `MeshStandardMaterial`。
- `createChibiCopperMan({ bodyVisible })` 返回可旋转 `THREE.Group`，支持 `bodyVisible` 开关整体显隐。
- `mapAcupointToChibi(position)`：把标准穴位坐标按头（y≥58）/躯干臂（y≥-12，|x|>15 判臂）/下肢三段映射到 Q 版模型的局部坐标（含前向 z 偏移 `Math.sign(z || FRONT)`），供穴位标注贴合模型。

### 6.3 `dateUtils.js`（日期工具）
- `DateUtils` 静态类：`format(date, 'YYYY-MM-DD')` 支持 YYYY/MM/DD/HH/mm/ss 占位替换与补零、无效日期返回空串；`formatYearMonth` / `formatDate` / `formatDateTime` 便捷方法。
- 独立函数 `formatLocalDate(date)`：避免时区问题的本地日期 `YYYY-MM-DD` 格式化。

### 6.4 `navUnlock.js`（导航解锁规则）
- `MAIN_NAV_UNLOCK_ITEMS`：8 个主导航项 → 关卡映射（home-map 公开；doctor-story→bamboo；body-map→body；meridian→meridian；copper-man→archive；safety→safe-start；review→secret-room；agency→agency）。
- `findNavUnlockByPath(path)`：优先查 `@/data/mainline` 的 `mainlineLevelForPath(path)`（主线关卡路由规则，含 `public`/`levelId`），否则按完整路径/前缀匹配导航项。
- `pathForUnlockedAtLevel(level)`：关卡 order → 对应路由。
- `isItemUnlocked(item, { isLoggedIn, gameState })`：公开项直接解锁；否则要求登录且 `gameState.levels` 中该 `levelId` 的 `unlocked === true`（Navbar 等处使用）。

### 6.5 `request.js`（axios 增强封装）——详见第七节

### 6.6 `resolveMediaUrl.js`（媒体 URL 解析）
- 构建期用 `import.meta.glob('@/assets/**/*.{png,jpg,jpeg,gif,webp,svg}', { as: 'url', eager: true })` 建立「文件名 → 打包后 URL」映射。
- `resolveMediaUrl(path)` 解析顺序：空值返回 ''；http(s) 原样返回；反斜杠转正斜杠；剥掉历史遗留的 `/api/files/` 前缀（保持相对当前主机）；**后端上传路径（`files/...`）不参与 assets 匹配**；非服务端路径按 basename 匹配 assets（兼容数据库里误填的 `src/assets` 路径）；开发机绝对路径（`/^[a-z]:\//i`）返回空串交给调用方兜底；最后逐段 `encodeURIComponent` 编码返回。

### 6.7 `storyPinyin.js`（故事注音分段引擎）
- 懒加载 `pinyin` 库（`ensureStoryPinyinReady`，tone 风格 + 分词）。
- `buildStoryReadingSegments(text, glossary, fullPinyin)`：glossary 术语表按词长降序，贪心最长匹配术语（`term` 段带 `pinyin` 与 `entry`）；非术语区间在 `fullPinyin` 模式下逐字产出 `char` 段（`getRunPinyin` 逐字注音，非法字符归一化为空），否则整段 `text` 段。每个段带稳定 `key`，供阅读组件渲染注音气泡/点击朗读。

### 6.8 `userCertificate.js`（九级荣誉证书流程）
- `renderCertificateToBlob({displayName, levelName, userId})`：用 DOM 构建 600×460 证书（渐变宣纸底、双线金框、标题「气血能量·九级荣誉证书」、授予语、证书编号 `QB-{userId}-{yyyymmdd}`、平台落款、日期），`html2canvas`（scale 2、useCORS）导出 JPEG Blob（0.92 质量）。
- `previewCertificateImage(url)`：`resolveMediaUrl` 解析后用 `Modal.info` 预览。
- `openUserCertificateFlow(userStore)`：先查 `userInfo.honor`（没有再拉 `getCurrentUser`）；有则直接预览；无则校验 `level >= 9` 后生成 → `uploadUserCertificate`（multipart）→ 更新用户信息并预览；全程 loading 提示与错误兜底。

### 6.9 `uuidUtils.js`（UUID 工具族）
- `generateUUID()`：优先 `crypto.randomUUID()`，降级 Math.random v4。
- `isValidUUID` / `isNumericId` / `getIdType(id)`（uuid|numeric|invalid）。
- `generateBusinessUUID(businessType)`、`uuidCache`（Map 缓存）、`BusinessUUIDManager`（生成/获取/设置/重置/状态标记，支持「外部设置」与「自动生成」区分）、`createBusinessUUIDManager` 工厂。
- `BUSINESS_TYPES` 常量（HERITAGE_ITEM/ACTIVITY/COURSE/SHOP_PRODUCT/INHERITOR/USER_AVATAR）与 `supportsUUIDStrategyB`（前 5 种支持「前端预生成 UUID 文件上传策略 B」）。

---

## 七、请求/响应拦截器与错误处理（`src/utils/request.js`）

**实例**：`axios.create({ baseURL: VITE_APP_BASE_API || '/api', timeout: 15000, headers: {'Content-Type': 'application/json;charset=utf-8'} })`。

**错误类型枚举**：`NETWORK / BUSINESS / HTTP / TIMEOUT / CANCEL`。

**请求拦截器**：
- 生成自增 `config.requestId` + 记录 `config.requestTime`；
- 从 user store 取 token，以标准 `Authorization: Bearer <token>` 注入；
- 若 `config.idempotencyKey` 则注入 `Idempotency-Key` 头（幂等）；
- 控制台打印 `📤` 请求日志。

**响应拦截器（成功）** → `handleResponse(data, config)`：
- 若 `enableCache` 且 GET 且 `code === "200"`，以 `method:url:params:data` 为 key 写入 `requestCache`（Map，记录 timestamp）；
- `code === "200"`：`successMsg` 优先，否则非 GET 且 `showDefaultMsg !== false` 时提示「操作成功」；执行 `onSuccess(data.data)`；**返回 `data.data`（已解包业务数据层）**；
- 非 200：构造 `{type: BUSINESS, code, message: data.msg || '请求失败', data, requestId}`；**`code === "401"` 且非登录接口 → `handleTokenExpired()`（清空用户信息并 `window.location.href = '/auth/login'`）**；按 `errorMsg` / `showDefaultMsg` 弹 `message.error`；执行 `onError`；`Promise.reject(errorInfo)`。

**响应拦截器（异常）**：
- 有 `error.response`：按 HTTP 状态码映射中文消息（400 请求参数错误 / 401 未授权 / 403 拒绝访问 / 404 资源不存在 / 408 请求超时 / 500 服务器内部错误 / 502 网关错误 / 503 服务不可用 / 504 网关超时），优先取后端 `data.msg`；401 且非登录接口再次触发 `handleTokenExpired()`；
- `ECONNABORTED` → TIMEOUT「请求超时，请检查网络连接」；
- `Network Error` → NETWORK「网络连接失败」；
- 其余 → 原始 message；
- 统一弹错（5 秒时长）并执行 `onError` 后 reject。

**扩展请求方法**（`request` 对象默认导出）：
- `get`（默认 `enableCache: true`）/ `post` / `put` / `delete`；
- `retry(url, {method, retryCount=3, ...})`：带重试的请求（标记 `enableRetry`）；
- `cancelable(url, ...)`：基于 `axios.CancelToken.source()` 的可取消请求（`promise.cancel = source.cancel`）；
- `clearCache(pattern?)` / `getCacheInfo()`：缓存管理。

**配置项约定**：`showDefaultMsg`（默认 true）、`successMsg`/`errorMsg`、`onSuccess`/`onError`、`enableRetry`/`retryCount`、`enableCache`/`cacheTime`、`idempotencyKey`——各 API 模块普遍以 `config`/`callbacks` 透传这些选项。

---

## 八、API 模块全清单（`src/api/`，26 个文件）

> 约定：所有路径均相对 `baseURL=/api`；`config`/`callbacks` 透传拦截器配置。

### 8.1 前台（用户端）API

**`index.js`**：统一出口，`export *` 自 user / AcupunctureApi / FileApi / CommunityApi / AiApi / SkillApi。

**`user.js`（用户/认证，25 个函数）**
| 函数 | 方法+路径 | 用途 |
|---|---|---|
| login | POST /user/login | 登录，返回 {userInfo, token} |
| register | POST /user/add | 注册 |
| getCurrentUser | GET /user/current | 当前用户信息 |
| getUserById | GET /user/{id} | 按 ID 查用户 |
| updateUser | PUT /user/{id} | 更新资料 |
| updatePassword | PUT /user/password/{id} | 改密码（oldPassword/newPassword） |
| forgetPassword | POST /user/forget | 三要素找回密码 |
| requestPasswordResetCode | POST /user/forget/code | 获取重置验证码 |
| logout | POST /user/logout | 退出（后端暂缺，前端直接清本地兜底） |
| getUserPage | GET /user/page | 分页用户列表（管理用） |
| getAdminUserSummary | GET /user/admin/summary | 管理员用户统计 |
| createAdminUser | POST /user/admin/create | 管理员创建用户 |
| getAdminUserOverview | GET /user/admin/overview/{id} | 学习档案总览 |
| updateAdminUserProfile | PUT /user/admin/{id}/profile | 编辑用户资料 |
| updateAdminUserStatus | PUT /user/admin/{id}/status | 启用/禁用账号 |
| resetAdminUserPassword | PUT /user/admin/{id}/password | 重置密码 |
| getAdminUserCheckins | GET /user/admin/checkins/{id} | 签到日历 |
| getAdminUserQuizHistory | GET /user/admin/quiz-history/{id} | 答题记录 |
| claimShuntingReward | POST /user/rewards/shunting | 调车挑战每日固定奖励 |
| getUserLevelInfo | GET /user/level/{id} | 气血等级信息 |
| getUserAdventureMap | GET /user/adventure-map/{id} | 冒险地图 |
| uploadUserCertificate | POST /user/certificate（multipart） | 上传九级荣誉证书 |
| checkinToday | POST /user/checkin/today | 每日签到（+5 分） |
| getCheckinMonth | GET /user/checkin/month | 某月签到日期 |
| getMyBadges | GET /user/badges | 我的徽章 |

**`AcupunctureApi.js`（针灸科普）**：listOriginStories GET /acupuncture/origin-story/list；listDoctorStories GET /acupuncture/doctor-story/list；listXuewei GET /acupuncture/xuewei/list；getXueweiDetail GET /acupuncture/xuewei/{id}；listJingluo GET /acupuncture/jingluo/list；getJingluoDetail GET /acupuncture/jingluo/{id}；listZhenjiuTools GET /acupuncture/zhenjiu-tools/list；listIllness GET /acupuncture/illness/list；awardIllnessBadge POST /acupuncture/illness/award/{illnessId}；listSkillNamesByIds GET /skill/names（按 ids 批量取名）；listExtraCourse GET /acupuncture/extracourse/list；getExtraCourseDetail GET /acupuncture/extracourse/{id}；listCopperManAcupoints GET /acupuncture/copper-man/acupoints；getCopperManDailyCase GET /acupuncture/copper-man/daily-case；discoverCopperManAcupoint POST /acupuncture/copper-man/discover（提交探索码）。全部 `enableCache: false`。

**`AiApi.js`**：aiChat POST /ai/chat——机器人助手对话，**单独 60s 超时 + 重试 2 次**（应对 DeepSeek 偶发慢响应）。

**`BackpackApi.js`（技能背包）**：getMyBackpack GET /user/backpack；addSkillToBackpack POST /user/backpack/add（成功后派发 `user-level-refresh` 事件刷新等级/升级提示）。

**`CollectApi.js`（收藏）**：getMyCollect GET /user/collect；addSkillToCollect POST /user/collect/add；removeSkillFromCollect POST /user/collect/remove；hasSkillCollect GET /user/collect/has。

**`CommunityApi.js`（社区：悄悄话信箱）**：帖子——getPostPage GET /community/post/page；getPostDetail GET /community/post/{postId}；createPost POST /community/post/create；postLike POST /community/post/{id}/like；postCollect POST /community/post/{id}/collect；postReport POST /community/post/report；addComment POST /community/post/comment；getPostLatest GET /community/post/latest（支持数字 limit 或 {limit, postType}）；getMyPostList / getMyCollectedPostList / getMyLikedPostList / getMyCommentList（GET /community/post/mine/{posts|collects|likes|comments}）；误区——getMisconceptionList GET /community/misconception/list；getMisconceptionById GET /community/misconception/{id}。

**`GameApi.js`（服务端权威游戏状态）**：getGameState GET /game/state；submitGameProgressEvent POST /game/progress/events（受后端白名单约束的行为结果，如 TASK_COMPLETED/TASK_PROGRESS/TASK_RESET）；importLegacyGameState POST /game/legacy-import（游客数据一次性导入，带 idempotencyKey）；exchangeAgencyArchiveItem POST /game/agency/archive-items/exchange（服务端扣材料兑换侦探社档案）。

**`QuizApi.js`（安全问答）**：getQuizQuestions GET /quiz/questions/random（随机 5 题）；submitQuizResult POST /quiz/result/submit；submitQuizAnswer POST /quiz/answer/submit（单题作答）；getUserQuizStats GET /quiz/stats/user；getMyMistakes GET /quiz/mistakes（不含答案）；submitMistakeAnswer POST /quiz/mistakes/answer（做对即移出错题）；getQuizHistory GET /quiz/history；getQuestionExplanation GET /quiz/question/{questionId}/explanation。

**`SkillApi.js`**：getSkillCount GET /skill/count。

**`FileApi.js`（文件）**——**前台与后台共用**：
- 简单上传：uploadImage POST /file/simple/upload/image；uploadSimpleFile POST /file/simple/upload（带 type）；uploadMultipleFiles POST /file/simple/upload/multiple；deleteSimpleFile DELETE /file/simple/delete/{filename}；getSimpleFileInfo GET /file/simple/info/{filename}；getDownloadPath GET /file/simple/download/{filename}。
- 业务上传：uploadBusinessFile POST /file/upload（businessType/businessId/businessField/replaceOld）；uploadBusinessFileWithUUID（策略 B 前端预生成 UUID）；uploadTempFile POST /file/upload/temp；uploadTempBusinessFile POST /file/upload/temp-business；confirmTempFile PUT /file/confirm/{tempFileId}。
- 查询：getFilesByBusiness GET /file/business/{type}/{id}；getFilesByBusinessField GET /file/business/{type}/{id}/{field}。
- 删除：deleteBusinessFile / deleteFile DELETE /file/{fileId}；deleteFilesByBusiness DELETE /file/business/{type}/{id}。
- 配置：getUploadConfig GET /file/upload/config；cleanupExpiredTempFiles POST /file/cleanup/temp。
- 常量：`FILE_TYPES`（COMMON/IMG/VIDEO/AUDIO/PDF）、`BUSINESS_TYPES`、`FILE_SIZE_LIMITS`（图片 10MB/视频 100MB/音频 50MB/PDF 20MB/通用 50MB）。

**`CopperContentApi.js`（铜人内容：发布流，前后台混用）**：后台态——getCopperContentOverview GET /admin/copper-content/overview；getCopperAcupointPage GET /admin/copper-content/acupoints；getCopperAcupoint GET /admin/copper-content/acupoints/{code}；saveCopperAcupointDraft PUT /admin/copper-content/acupoints/{code}/draft；getCopperStoryPage GET /admin/copper-content/stories；getCopperStory GET /admin/copper-content/stories/{code}；saveCopperStoryDraft PUT /admin/copper-content/stories/{code}/draft；transitionCopperContent POST /admin/copper-content/{type}/{key}/{action}（状态流转，带 idempotencyKey）；getCopperContentReleases GET /admin/copper-content/releases；getCopperContentRevisions GET /admin/copper-content/{type}/{key}/revisions；restoreCopperContentRevision POST /admin/copper-content/{type}/{key}/revisions/{version}/restore。前台态——listPublishedCopperStories GET /acupuncture/copper-content/stories；getPublishedCopperStory GET /acupuncture/copper-content/stories/{code}；saveCopperStoryProgress PUT /acupuncture/copper-content/stories/{code}/progress；getCopperContentFeed GET /acupuncture/copper-content/feed?limit=；markCopperContentRead POST /acupuncture/copper-content/feed/{releaseId}/read。

**`MainlineConfigApi.js`（主线配置，前后台混用）**：前台 getMainlineConfig GET /game/mainline-config；后台 getAdminMainlineConfig GET /admin/mainline-config；saveMainlineDraft PUT /admin/mainline-config/draft；validateMainlineDraft POST /admin/mainline-config/draft/validate；publishMainlineDraft POST /admin/mainline-config/draft/publish；restoreMainlineVersion POST /admin/mainline-config/versions/{version}/restore。全部 `showDefaultMsg: false`（草稿保存等静默化）。

### 8.2 后台管理 API（`/admin/*`）

| 模块 | 文件 | 接口（方法 路径） |
|---|---|---|
| 仪表盘 | dashboard.js | getDashboardStatistics GET /dashboard/statistics（用户/管理员/帖子/访问统计，`enableCache:false`） |
| 社区帖子 | CommunityPostAdminApi.js | pageCommunityPosts GET /admin/community-post/page；getCommunityPostDetail GET /admin/community-post/{id}；createCommunityPost POST /admin/community-post；deleteCommunityPost DELETE /admin/community-post/{id} |
| 评论 | PostCommentAdminApi.js | pagePostComments GET /admin/post-comment/page；getPostCommentDetail GET /admin/post-comment/{id}；deletePostComment DELETE /admin/post-comment/{id} |
| 名医故事 | DoctorStoryAdminApi.js | GET /admin/doctorstory/page；GET /admin/doctorstory/{id}；POST /admin/doctorstory/create；PUT /admin/doctorstory/{id}；DELETE /admin/doctorstory/{id} |
| 附加课程 | ExtraCourseAdminApi.js | GET /admin/extracourse/page；GET /admin/extracourse/{id}；POST /admin/extracourse/create；PUT /admin/extracourse/{id}；DELETE /admin/extracourse/{id} |
| 疾病 | IllnessAdminApi.js | GET /admin/illness/page；GET /admin/illness/{id}；POST /admin/illness；PUT /admin/illness/{id}；DELETE /admin/illness/{id} |
| 经络 | JingluoAdminApi.js | GET /admin/jingluo/page；GET /admin/jingluo/{id}；POST /admin/jingluo/create；PUT /admin/jingluo/{id}；DELETE /admin/jingluo/{id} |
| 开场故事 | OriginStoryAdminApi.js | GET /admin/originstory/page；POST /admin/originstory/create；PUT /admin/originstory/{id}；DELETE /admin/originstory/{id} |
| 题库 | QuizQuestionAdminApi.js | GET /admin/quiz-question/page；GET /admin/quiz-question/{id}；POST /admin/quiz-question/create；PUT /admin/quiz-question/{id}；DELETE /admin/quiz-question/{id} |
| 技能 | SkillAdminApi.js | GET /admin/skills/page；GET /admin/skills/{id}；POST /admin/skills；PUT /admin/skills/{id}；DELETE /admin/skills/{id} |
| 小火车关卡 | TrainGameAdminApi.js | GET /admin/train-game/page；GET /admin/train-game/{id}；POST /admin/train-game/create；PUT /admin/train-game/{id}；DELETE /admin/train-game/{id} |
| 穴位 | XueweiAdminApi.js | GET /admin/xuewei/page；GET /admin/xuewei/{id}；POST /admin/xuewei/create；PUT /admin/xuewei/{id}；DELETE /admin/xuewei/{id} |
| 针法 | ZhenfaAdminApi.js | GET /admin/zhenfa/page；GET /admin/zhenfa/{id}；POST /admin/zhenfa/create；PUT /admin/zhenfa/{id}；DELETE /admin/zhenfa/{id} |

> 注：用户管理相关管理接口（`/user/admin/*`、`/user/page`、`/user/admin/summary`）定义在 `user.js` 中（见 8.1）。

---

## 九、样式体系（`src/styles/`）

### 9.1 `global.css`（基础重置 + CSS 变量）
- `*` 重置 margin/padding/box-sizing；移除列表样式、链接下划线；图片 max-width 100%。
- `:root` 定义全站 CSS 变量：`--site-primary #2f7d68`、`--site-primary-dark`、`--site-secondary`、`--site-accent #ffd35a`、`--site-background #fff8e8`、`--site-paper/-deep`、`--site-highlight`、`--site-copper #b87333`、`--site-copper-dark`、`--site-safe #54b6a1`、`--site-muted-blue`、三档文字色与标题/正文字体（思源宋体/黑体）。工具类 `.text-ellipsis`、`.pointer`。

### 9.2 `admin-desktop.scss`（后台桌面端统一视觉）
仅 `@media (min-width: 1024px)` 生效，覆盖范围包括：
- 后台主题变量：`--admin-primary #1f5a50`、sidebar `#173a35`、canvas `#f5f7f6`、border `#dde4e1` 等；
- 侧边栏固定 232px（折叠 72px）、logo 区 64px、菜单圆角 8px、选中项绿色高亮 + 左侧 3px 指示条；
- 导航栏 64px；按钮/输入框统一 8px 圆角、40px 最小高度；
- 内容页容器：搜索面板与表格面板「上圆角/下圆角」拼接卡片、表头 48px 浅灰底、行 hover 高亮、分页主色；
- 管理页 `.page`、`.profile-card`、编辑抽屉 `.admin-editor-drawer`（吸底 footer）等细节打磨；`:focus-visible` 无障碍焦点。

### 9.3 `scroll-header.css`（传统卷轴头部样式）
- `.scroll-header`：上下棕色卷轴边 + 宣纸白背景 + 内阴影卷曲效果；`:before/:after` 左右 40px 立体木质轴杆（径向渐变 + 内阴影 + 描边）。
- `.scroll-header-title`：56px 宋体标题 + 左右装饰线 + 16px 字距；`.scroll-header-subtitle` 副标题；`.scroll-header-seal` 朱砂红印章（竖排文字）。
- 响应式断点 768/600/480px 逐级缩小轴杆与字号。
- `README-scroll-header.md`：使用指南（引入方式、HTML 结构、类名表、自定义颜色变量 `--seal-color`/`--wood-color`、断点说明、已应用页面如 /heritage 非遗等、统一样式规范）。说明该样式源自早期「非遗传承」项目模板，当前中医平台部分页面沿用。

---

## 十、组合式函数（`src/composables/`）

### 10.1 `useBusinessUUID.js`
- `useBusinessUUID(businessType, {autoGenerate, initialUUID})`：包装 `BusinessUUIDManager` 为响应式（`uuid`/`isGenerated`/`isLoading`/`error` 均 `readonly`），提供 `generateUUID`/`setUUID`/`resetUUID`/`getOrGenerateUUID`，计算属性 `hasUUID`/`isUUIDValid`/`businessId`；组件卸载自动 reset；不支持策略 B 的类型给出 warn。
- `useFileUploadUUID`：叠加上传状态（`uploadedFiles`/`uploadProgress`/`isUploading`）与 `prepareUpload`/`addUploadedFile`/`removeUploadedFile`/`clearUploadedFiles`/`resetUploadState`。
- `useFormUUID`：watch UUID 变化自动回写 `formData.id`；`prepareFormSubmit` 返回带 id 的表单；`resetForm` 清空非 id 字段。
- 另导出 `generateUUID` 便捷函数与 `BUSINESS_TYPES`。

### 10.2 `useDesktopViewport.js`
- `window.matchMedia('(min-width: 1024px)')` 响应式桌面视口检测，`onMounted` 监听 change、`onBeforeUnmount` 移除，返回布尔 ref（后台桌面端自适应依据）。

### 10.3 `useGameState.js`（游戏状态核心，773 行）
- **模块级单例 `gameState = ref(readState())`**：从 localStorage 读 `xinglin-game-state-v3`（旧 v2 自动迁移并写回 v3）、故事状态 `xinglin-story-state-v1`；结构含 `completedTaskIds / taskProgress / taskClaims / agencyArchiveIds / storyArchiveIds / storyProgress / reviewRecords / materialCounts`。
- **服务端会话检测** `hasServerSession()`：localStorage 有 token+userInfo；`applyServerState(state)` 把服务端 tasks/materials/agencyArchiveIds 映射到本地结构（含 server↔local 任务码转换：`serverTaskCode` / `localTaskIdForServerTask`，如 `meridian-route-lung` ↔ `meridian-lung-route`、`copper-man-star-*` → `copper-man-daily-case`）。
- **`submitServerTask`**：登录态下以 `gameCode: 'meridian-river'|'xinglin'` + `eventType`（TASK_COMPLETED/TASK_PROGRESS/TASK_RESET）+ `resultCode` 提交 `submitGameProgressEvent`，幂等键 `game:{userId}:{taskCode}:{periodKey}`；受 `isSupportedServerTask` 白名单约束（每日任务、主任务、十四经脉、消消看/排序分数档）。
- 任务 API：`recordTaskProgress`（游客本地/登录态服务端双路径，进度+奖励发放）、`resetTaskProgress`、`completeTask`、`completeStandaloneTask`、`completeStoryCase`（星级、主线/每日任务奖励、三星奖励、归档）、`addMaterials`。
- 故事进度：`createStoryProgress`/`getStoryProgress`/`saveStoryProgress`（阅读页、线索、证据顺序、推理/安全答案、星级、奖励领取标记）。
- 侦探社档案：`exchangeAgencyArchiveItem`（前置校验 + 材料扣减 + 服务端兑换或本地模拟，可完成主线任务 `main-repair-agency`）、`isAgencyArchiveItemOwned`、`hasMaterials`。
- 星光修补册：`addReviewRecord`（重复记录累计 wrongCount）、`repairReviewRecord`（奖励 + 状态 repaired）。
- 徽章计算 `badgeProgressFor`：9 类徽章按完成数/档案数/故事数/错题修复数/铜人星数推导进度；`badgeStatusFor` 得 earned/locked/available/in-progress。
- 地图关卡 `mapStateFor`：按 `requiredTaskIds` 完成度推导 `已完成/当前关卡/已解锁/待解锁`（含 progress/target）。
- **持久化 watch**：深监听 gameState → 写故事状态到 v1 键；无服务端会话时写 `xinglin-game-state-v3`。监听 `game-state-refresh` 事件实时应用服务端状态。
- 导出 `useGameState()` 与 `applyServerState`。外部数据依赖：`@/data/tasks`、`@/data/materials`、`@/data/badges`、`@/data/mapLevels`、`useMainlineConfig`、`GameApi`。

### 10.4 `useMainlineConfig.js`（主线关卡配置：服务端优先 + 本地兜底）
- 模块级 `config = ref(createFallbackConfig())`：8 个关卡（fallback 自 `@/data/mainline`，带 `prerequisiteLevelIds` 与任务）。
- `loadMainlineConfig({force})`：防并发（`pendingRequest` 复用），调 `getMainlineConfig({showDefaultMsg:false})`，`normalizeConfig` 校验 8 关卡、按 order 排序、合并 fallback 字段、归一化任务（target/rewards/scoreDelta/奖励名）；**失败回退本地基线配置**（游戏仍可玩）。
- `getMainlineTask(taskCode)`：遍历配置关卡找任务，找不到回落静态任务表；`useMainlineConfig()` 暴露 `config/levels/loading/loadMainlineConfig`；另导出 `mainlineConfig` ref 供 useGameState 使用。

### 10.5 `useSoundEffects.js`（Web Audio 合成音效）
- 单例 `AudioContext`（含 webkit 前缀兼容、suspended 自动 resume）。
- 底层：`playTone(freq, duration, type, volume, ramp)`（指数衰减包络）、`playChord`（多音等比分压）、`playMelody`（时序音符序列）。
- 20 个游戏音效：click / select / place / remove / success / error / match(count) / comboSound(combo) / special / warning / hint / clearAll / roundComplete / victory / collect / boardDrop / boardSwap / buttonHover / pageTransition。全部用正弦/三角/方波/锯齿波程序化合成，无需音频资源文件。

---

## 十一、登录注册、权限控制与用户信息管理逻辑汇总

1. **登录流程**：Login.vue（`/auth/login`）→ `userStore.login(form)` → `api.login` POST `/user/login` → 校验 token → `setUserInfo`（写 store + localStorage `userInfo`/`token`）→ 依次 `migrateLegacyGameState()`（游客本地进度导入服务端，幂等键 `legacy-import:{userId}:v4`）、`loadGameState()`（拉 `/game/state`）、`ensureLevelInfo()`（拉 `/user/level/{id}`）→ 路由守卫自动把已登录用户从登录页导向 `/back/dashboard`（管理员）或 `/home-map`。
2. **注册/找回密码**：Register（`/auth/register`，`api.register` POST `/user/add`）；ForgotPassword（`requestPasswordResetCode` POST `/user/forget/code` → `forgetPassword` POST `/user/forget`）。
3. **鉴权体系**：
   - **token**：localStorage 持久化，请求拦截器注入 `Authorization: Bearer`；401（业务码或 HTTP 状态）且非登录接口 → 清用户信息 + 整页跳 `/auth/login`；
   - **角色**：`userType` 字段，`ADMIN` 判定 `isAdmin`，后台 `/back/*` 需登录+管理员双条件；
   - **requiresAuth 路由**：铜人馆/安全问答/背包/个人设置/我的信息/我的发帖/我的错题/其他设置；
   - **游戏解锁**：主导航与部分页面由「主线关卡解锁」控制（`navUnlock.js` + 守卫中 `loadGameState` 校验 `levels[].unlocked` + `isItemUnlocked` 供导航渲染）。
4. **登出**：`userStore.logout()`（静默调 `/user/logout` + 清本地）；BackendLayout 的 `handleLogout` 直接 `clearUserInfo()` + 跳 `/login`；`utils/auth.js` 的 `safeLogout` 供前台使用。
5. **用户信息管理**：`getCurrentUser` 刷新、`updateUserInfo` 合并更新、`updateUser`/`updatePassword` 修改资料与密码、`uploadUserCertificate` 九级证书上传、`checkinToday`/`getCheckinMonth`/`getMyBadges` 签到徽章；管理员侧 `user.js` 的 `/user/admin/*` 系列做用户档案/状态/密码/签到/答题历史管理。
6. **事件总线（window CustomEvent）**：`user-level-refresh`（等级/气血变化后通知导航栏刷新）、`game-state-refresh`（服务端游戏状态同步）贯穿 store、API、composable，实现跨组件状态联动。

---

## 十二、其他要点与观察

- **双状态源并存**：`useGameState` 维护「游客本地态」，`userStore` 维护「登录态」；登录后通过 `applyServerState`/`migrateLegacyGameState` 完成本地→服务端收敛，未登录时继续本地持久化（`xinglin-game-state-v3` 等键）。
- **幂等设计**：游戏事件、遗留导入、铜人内容流转、档案兑换均带 `Idempotency-Key` 头，防重复提交。
- **错误静默约定**：游戏/草稿类接口普遍 `showDefaultMsg: false`，由业务层自行提示；AI 对话单独 60s 超时。
- **前后台 API 命名差异**：新后台接口统一 `/admin/*`（RESTful，`/create` 后缀与无后缀混用，如 `/admin/skills` 无 create 后缀而 `/admin/doctorstory/create` 有）；前台接口多语义化路径（`/user/backpack`、`/community/post/*`、`/game/*`）。
- **遗留痕迹**：`config/api.js` 的 `API_ENDPOINTS`、`vuex` 依赖、`scroll-header`（非遗模板）与 `README` 中 `/heritage` 等旧页面、FontAwesome 与 AntD 图标并存——均不影响当前主流程。

---

报告完毕。所有列出的文件均已完整读取（未发现空文件；`App.vue` 与 `AuthLayout` 中 `isForgotPassword` 等均为有效逻辑）。

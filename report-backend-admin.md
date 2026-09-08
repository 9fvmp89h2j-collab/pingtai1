# 儿童针灸教育科普网站后台管理页面 — 详尽中文技术报告

## 一、报告范围与项目概况

本报告基于项目根目录 `D:\总\Pediatric Acupuncture Education System\pingtai1` 下的 Vue 3 前端工程（`vue3` 目录），对后台管理（backend）的全部 **16 个页面组件** 逐一进行了完整阅读分析（含超长文件分段读取），并核对了相关 API 模块的真实接口路径，以及前台页面目录（`vue3/src/views/frontend`）以确认前后台对应关系。

### 技术栈概况
- **框架**：Vue 3（`<script setup>` 组合式 API）+ Vite
- **UI 组件库**：Ant Design Vue（`a-card`、`a-table`、`a-form`、`a-modal`、`a-drawer`、`a-tabs` 等）
- **图标**：`@ant-design/icons-vue` 与 FontAwesome（`fas` 类）
- **图表**：ECharts（按需引入 `echarts/core`）
- **HTTP 层**：`@/utils/request` 统一封装（支持 `showDefaultMsg`、`onSuccess/onError` 回调、`enableCache`、`idempotencyKey` 等配置），API 模块位于 `vue3/src/api/`
- **公共后端组件**：
  - `AdminPageError.vue`：统一错误提示 + 加载态 + 重试按钮（`@retry` 事件）
  - `AdminEditorSurface.vue`：统一编辑弹层外壳（支持 modal/drawer 两种形态、`ok-text`、`loading` 等）
  - `useDesktopViewport` 组合式函数：响应式判断桌面视口，控制表格横向滚动

### 公共设计模式（多处复用）
1. **CRUD 列表页模式**：`searchForm + a-table + pagination + 新增/编辑 modal + popconfirm 删除`，翻页/搜索统一 `current=1` 后重载。
2. **图片/视频上传模式**：`a-upload` 的 `before-upload` 拦截 → 调 `uploadSimpleFile(file, 业务类型)` → 拿到服务器返回路径写入表单字段 → `hydrate*List()` 回填 `file-list`；前端校验图片 ≤5MB、视频 ≤80/200MB，仅校验 `type.startsWith('image/'|'video/')`。
3. **接口风格分两代**：早期页面（skills、zhenfa、extracourse、illness）使用 `onSuccess/onError` 回调式调用；较新页面（doctor-story、jingluo、quiz-question、mainline-level、user、xuewei）使用 `await` + `showDefaultMsg:false` 的 Promise 风格。

---

## 二、逐页详细分析

---

### 1. Dashboard.vue — 后台仪表盘（数据总览）

**文件**：`vue3\src\views\backend\Dashboard.vue`（505 行）

#### 1.1 管理功能概述
后台登录后的首页工作台，面向**系统管理员**，提供全站核心指标的"驾驶舱"视图：欢迎卡片（当前登录用户头像、姓名、角色标签、实时时间）+ 四大统计卡片 + 近 7 天访问趋势折线图。

#### 1.2 页面结构
- **欢迎卡片**：`a-avatar`（显示用户头像或姓名首字）、欢迎语"欢迎回来，{name/username}"、实时时钟（每分钟刷新一次，`zh-CN` 长格式日期）、角色标签（`ADMIN→系统管理员`、`USER→普通用户`）。
- **统计卡片**（`a-statistic`，带图标前缀）：
  - 普通用户数 `totalUsers`（绿）+ 今日新增 `todayNewUsers`
  - 总发帖数 `totalPosts`（蓝）+ 今日新增 `todayNewPosts`
  - 总管理员数 `totalAdmins`（紫）
  - 网站总访问数 `totalVisits`（红）+ 今日访问 `todayVisits`
- **图表区**：ECharts 折线图（近 7 天访问趋势，`last7DaysVisits` 数组的 `date/count`），平滑曲线 + 渐变面积填充，主题色 `#1f5a50`（项目品牌墨绿）。

#### 1.3 支持的操作
- 仅**只读展示**：加载失败时通过 `AdminPageError` 提供"重试"；窗口 resize 时图表自动 `resize()`；组件卸载时清理定时器与图表实例（防泄漏）。

#### 1.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/dashboard` | `getDashboardStatistics` | `/dashboard/statistics` | GET |

#### 1.5 与前台对应关系
无直接前台对应，是后台运营数据汇总；统计口径对应前台用户注册、社区发帖、站点访问埋点。

#### 1.6 特殊逻辑
- 基于 `userStore.userInfo`（Pinia 用户 store）展示当前登录者，角色映射表仅含 ADMIN/USER 两类。
- 图表使用 ECharts 按需注册（`LineChart + GridComponent + TooltipComponent + CanvasRenderer`），避免全量引入。
- 布局响应式：桌面端（≥1024px）使用无衬线卡片风格并透明化背景适配后台壳；移动端单列堆叠。

---

### 2. community-comment/index.vue — 评论管理

**文件**：`vue3\src\views\backend\community-comment\index.vue`（182 行）

#### 2.1 管理功能概述
检索并处理社区帖子的评论，面向管理员治理不适合继续展示的内容（文案："检索社区评论并处理不适合继续展示的内容"）。该页**无新增/编辑**，定位为纯审核治理页。

#### 2.2 页面结构
- **搜索区**（inline 表单）：
  - 帖子编号 `postId`（`a-input-number`）
  - 用户编号 `userId`（`a-input-number`）
  - 关键词 `keyword`（评论内容模糊匹配，`a-input` allow-clear）
- **表格列**：编号 `id` / 帖子编号 `postId` / 用户编号 `userId` / 评论内容 `content`（ellipsis 省略）/ 创建时间 `createTime` / 操作（删除，fixed right）。
- **分页**：`current/pageSize/total`，支持切换每页条数与快速跳页，桌面端表格横向滚动阈值 `x:1180`。

#### 2.3 支持的操作
- **查询/重置**：查询重置后回到第 1 页重载。
- **删除评论**：`a-popconfirm` 二次确认（"确认删除该评论吗？"红色），删除成功后若当前页删空则自动回退一页。
- **刷新数据**：工具栏按钮。

#### 2.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/PostCommentAdminApi` | `pagePostComments` | `/admin/post-comment/page` | GET |
| `@/api/PostCommentAdminApi` | `deletePostComment` | `/admin/post-comment/{id}` | DELETE |

（模块内还有 `getPostCommentDetail` → `GET /admin/post-comment/{id}` 未被本页使用）

#### 2.5 与前台对应关系
对应前台社区帖子详情页中的**评论列表与发表评论**功能（前台 `CommunityApi` 侧的评论接口），后台删除即前台评论下线。

#### 2.6 特殊逻辑
- 纯治理型页面：无状态字段展示、无审核流转，删除为硬删。
- 空页回退逻辑（`tableData.length === 1 && current > 1` 时 current-1）在其他删除类页面中反复出现，是项目统一约定。

---

### 3. community-feedback/index.vue — 反馈消息（意见箱）

**文件**：`vue3\src\views\backend\community-feedback\index.vue`（202 行）

#### 3.1 管理功能概述
集中查看用户通过"意见箱"提交的问题与建议。**关键设计**：反馈与社区帖子共用同一张表（`community_post`），页面用 `postType='反馈'` 固定过滤，仅展示反馈类帖子，不展示公告（页面顶部有明确提示文案）。

#### 3.2 页面结构
- **搜索区**：用户编号 `userId`（数字框）、关键词 `title`（标题/内容模糊搜索）。
- **表格列**：编号 `id` / 用户编号 `userId` / 标题 `title`（宽 220 ellipsis）/ 反馈内容 `content`（ellipsis）/ 类型 `postType` / 状态 `status`（tag 着色）/ 提交时间 `createTime` / 操作（删除）。
- **状态映射**：`0→正常(绿)`、`1→已删除(default)`、`2→已举报(橙)`，其余显示原值。

#### 3.3 支持的操作
查询/重置、删除（popconfirm）、刷新；无新增编辑。

#### 3.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/CommunityPostAdminApi` | `pageCommunityPosts` | `/admin/community-post/page` | GET |
| `@/api/CommunityPostAdminApi` | `deleteCommunityPost` | `/admin/community-post/{id}` | DELETE |

（与帖子管理共用同一 API 模块，仅查询参数 `postType` 不同）

#### 3.5 与前台对应关系
对应前台"意见箱/反馈提交"入口（前台用户提交 `postType='反馈'` 的帖子），后台在此集中查看与治理。

#### 3.6 特殊逻辑
- 单表多视图：通过固定 `postType` 查询参数区分"帖子管理/反馈消息"两个后台页面，体现数据模型复用。
- 状态字段 `status` 语义与前台社区一致（正常/已删除/已举报）。

---

### 4. community-post/index.vue — 帖子管理（社区 + 公告）

**文件**：`vue3\src\views\backend\community-post\index.vue`（358 行）

#### 4.1 管理功能概述
社区内容总控台：查看全部社区内容、按板块/状态检索、**以管理员身份发布公告**、删除需下线的帖子（文案："查看社区内容、发布公告并处理需要下线的帖子"）。

#### 4.2 页面结构
- **搜索/筛选区**：
  - 用户编号 `userId`（数字框）
  - 关键词 `title`（标题/内容）
  - 分类 `postCatagory`（输入框）
  - 板块 `postType`（下拉：问答专区 / 分享专区 / 公告 / 反馈）
  - 状态 `status`（下拉：0 正常 / 1 已删除 / 2 已举报）
- **表格列**（13 列，桌面端横向滚动 `x:1680`）：编号 `id` / 用户编号 `userId` / 标题 `title` / 内容摘要 `content` / 点赞数 `likeCount` / 收藏数 `collectCount` / 评论数 `commentCount` / 分类 `postCatagory` / 板块 `postType` / 状态（tag）/ 创建时间 `createTime` / 更新时间 `updateTime` / 操作（删除，fixed right）。
- **发布公告弹窗**（`a-modal` 宽 720，destroy-on-close）：
  - 板块 `postType`（下拉，默认"公告"）
  - 标题 `title`（必填，`maxlength=200` 带字数统计）
  - 内容 `content`（`a-textarea` 8 行）
  - 分类 `postCatagory`（选填）

#### 4.3 支持的操作
查询/重置、刷新、**发布公告（create）**、删除（popconfirm）；无编辑功能（帖子内容不可后台修改）。

#### 4.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/CommunityPostAdminApi` | `pageCommunityPosts` | `/admin/community-post/page` | GET |
| `@/api/CommunityPostAdminApi` | `createCommunityPost` | `/admin/community-post` | POST |
| `@/api/CommunityPostAdminApi` | `deleteCommunityPost` | `/admin/community-post/{id}` | DELETE |

（`getCommunityPostDetail` 存在但本页未用）

#### 4.5 与前台对应关系
对应前台社区板块（`frontend/community/index.vue`、`qa.vue` 问答专区、`misconceptions.vue` 分享/误区等）与首页公告栏；管理员发布的"公告"帖子直接在前台公告区展示。

#### 4.6 特殊逻辑
- 板块（postType）与状态（status）用下拉枚举做**精确筛选**，与反馈页的固定过滤形成对照。
- 发帖主体为管理员身份（接口走 admin 域），无需指定 userId（后端取当前登录管理员）。

---

### 5. doctor-story/index.vue — 名医故事管理

**文件**：`vue3\src\views\backend\doctor-story\index.vue`（781 行）

#### 5.1 管理功能概述
维护"名医故事"内容：故事正文（简介+详解）、儿童阅读重点词词库（拼音+儿童版解释）、封面大图、三张配图、讲解视频，并支持关联技能点。面向儿童端"名医故事"阅读卡片的内容运营。

#### 5.2 页面结构
- **搜索区**：故事名称 `doctorName`（输入框）、关联技能 `skillId`（数字框）。
- **表格列**：编号 `id` / 故事名称 `doctorName` / 封面（缩略图 `previewPic`）/ 故事简介 `doctorBrief`（ellipsis）/ 关联技能 `skillId` / 视频（`media` 有→绿色"已上传视频"）/ 配图（`doctorPic1~3` 三张缩略图）/ 操作（编辑/删除），桌面端横向滚动 `x:1380`。
- **编辑弹层**（`AdminEditorSurface` modal，宽 860）：
  - 故事名称 `doctorName`（必填）
  - 故事简介 `doctorBrief`（textarea 3 行）
  - 故事详解 `doctorDetail`（textarea 5 行）
  - 关联技能 `skillId`（数字框，可为空）
  - 预览封面 `previewPic`（picture-card 单图上传，注释"用在前台名医故事卡片的大封面"）
  - **重点词词库 `readingGlossary`**（数组，每项：词语 `word` + 拼音 `pinyin` + 儿童版解释 `meaning`；动态增删行，"添加重点词"虚线按钮；有解释提示"给孩子阅读时需要解释的词"）
  - 图片1/2/3 `doctorPic1~3`（picture-card 单图上传）
  - 故事视频 `media`（`video/*` 单文件上传，上限 80MB，提示"没有配图时前台也会单独显示这个视频"）

#### 5.3 支持的操作
**新增 / 编辑 / 删除（popconfirm）**、搜索/重置/刷新；新增时自动预置一个空重点词行，编辑时若无词条也补一行。

#### 5.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/DoctorStoryAdminApi` | `getDoctorStoryPage` | `/admin/doctorstory/page` | GET |
| `@/api/DoctorStoryAdminApi` | `createDoctorStory` | `/admin/doctorstory/create` | POST |
| `@/api/DoctorStoryAdminApi` | `updateDoctorStory` | `/admin/doctorstory/{id}` | PUT |
| `@/api/DoctorStoryAdminApi` | `deleteDoctorStory` | `/admin/doctorstory/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

上传类型：图片用 `'START'`（实际为业务归类参数，各页不同）与 `'VIDEO'`。校验：图片 ≤5MB、视频 ≤80MB。

#### 5.5 与前台对应关系
对应前台 `frontend/DoctorStory.vue`（名医故事列表/详情阅读页）：`previewPic` 为列表大封面，`doctorPic1~3` 为正文插图，`media` 为可选讲解视频，`readingGlossary` 供前台**点击词语弹出拼音+儿童版解释**的阅读辅助。

#### 5.6 特殊逻辑
- **结构化词库编辑器**：`readingGlossary` 以数组形式与表单双向绑定，提交前过滤空行并校验每行"词/拼音/解释"三者齐全，缺一即提示"第 N 个重点词请完整填写"。
- 上传工具函数：`toImg()` 统一把相对路径补 `/` 前缀、http 直通；`extractFileName()` 解析 Windows/Unix 分隔符取文件名；`createUploadListItem()` 生成 a-upload 回显项。
- 编辑回填采用**字段级复制**（`cloneGlossaryItems` 深拷贝词库，避免直接引用响应对象）。

---

### 6. extracourse/index.vue — 拓展疗法管理

**文件**：`vue3\src\views\backend\extracourse\index.vue`（397 行）

#### 6.1 管理功能概述
维护"拓展疗法"内容条目：名称、简介、详细介绍、图标、三张配图，并可关联技能点 `skillId`。对应前台拓展疗法详情页的学习内容。

#### 6.2 页面结构
- **搜索区**：名称 `name`、技能 ID `skillId`。
- **表格列**：`extraCourseId`（ID）/ 名称 `extraCourseName` / 简介 `extraCourseBrief`（ellipsis）/ `skillId` / 图标 `extraCourseIcon`（缩略图）/ 配图（`extraCoursePic1~3` 缩略图）/ 操作（编辑/删除）。
- **编辑弹窗**（`a-modal` 宽 920）：
  - 名称 `extraCourseName`（必填）
  - 简介 `extraCourseBrief`（3 行）、详细介绍 `extraCourseDes`（5 行）
  - 技能 ID `skillId`（数字框）
  - 图标 `extraCourseIcon`、配图 1/2/3（picture-card 单图上传，≤5MB）

#### 6.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置；上传走 `uploadSimpleFile(file,'EXTRACOURSE')`。

#### 6.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/ExtraCourseAdminApi` | `getExtraCoursePage` | `/admin/extracourse/page` | GET |
| `@/api/ExtraCourseAdminApi` | `createExtraCourse` | `/admin/extracourse/create` | POST |
| `@/api/ExtraCourseAdminApi` | `updateExtraCourse` | `/admin/extracourse/{id}` | PUT |
| `@/api/ExtraCourseAdminApi` | `deleteExtraCourse` | `/admin/extracourse/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

#### 6.5 与前台对应关系
对应前台 `frontend/ExtraCourseDetail.vue` 与 `frontend/Method.vue` 中的"拓展疗法"学习内容卡片/详情。

#### 6.6 特殊逻辑
- 回调式 API 风格（`onSuccess/onError`），表单整体 `Object.assign(formData, record)` 直接回填（含多余字段，提交时按白名单组装 payload）。
- 无 `AdminPageError` 组件包裹（相对早期的页面，错误仅通过 `message` 提示）。

---

### 7. illness/index.vue — 疾病管理

**文件**：`vue3\src\views\backend\illness\index.vue`（500 行）

#### 7.1 管理功能概述
维护"小镇诊所"疾病档案：疾病名称、表现、图片、主穴数量/手法数量，以及**疾病与技能点的关联表**（5 个主穴技能 `xuewei1~5` + 4 个手法技能 `tools1~4`），面向铜人探案小镇的诊所剧情内容。

#### 7.2 页面结构
- **搜索区**：小镇名称 `cowtown`、疾病名称 `illnessname`；右侧"新增疾病"按钮。
- **表格列**：`illnessid` / 小镇名称 `cowtown` / 疾病名称 `illnessname` / 疾病表现 `illnessfeature`（ellipsis）/ 图片 `illnesspic`（缩略图）/ 主穴数量 `xueweicount` / 手法数量 `toolscount` / 操作（编辑/删除）。
- **编辑弹窗**（`a-modal` 宽 880，带 `a-form` rules 校验，必填：小镇名称、疾病名称）：
  - 基本信息：小镇名称、疾病名称、疾病表现（textarea 3 行）、疾病图片（单图上传，`ILLNESS` 类型）
  - 计数：主穴数量 `xueweicount`、手法数量 `toolscount`
  - 分组区（divider）：主穴技能 ID `xuewei1~5`（5 个输入框）、手法技能 ID `tools1~4`（4 个输入框）——即疾病治疗方案的技能绑定

#### 7.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、查询/重置；表单提交前 `formRef.validate()`。

#### 7.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/IllnessAdminApi` | `pageIllness` | `/admin/illness/page` | GET |
| `@/api/IllnessAdminApi` | `createIllness` | `/admin/illness` | POST |
| `@/api/IllnessAdminApi` | `updateIllness` | `/admin/illness/{id}` | PUT |
| `@/api/IllnessAdminApi` | `deleteIllness` | `/admin/illness/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

#### 7.5 与前台对应关系
对应前台 `frontend/Clinic.vue`（小镇诊所）与铜人探案剧情：疾病卡片展示 `cowtown`/`illnessfeature`/图片，主穴与手法技能 ID 关联到技能点表，驱动"病例→技能学习"闭环。

#### 7.6 特殊逻辑
- 9 个技能关联字段以**扁平字符串表单**呈现（非动态列表），字段名即数据库列名。
- 主穴/手法数量与关联字段数量需人工保持一致（无自动联动校验）。

---

### 8. jingluo/index.vue — 经络管理

**文件**：`vue3\src\views\backend\jingluo\index.vue`（451 行）

#### 8.1 管理功能概述
维护经络知识库：经络名称、分类（如十二经脉/十二经别/奇经八脉）、循行次序说明、主治病症说明、经络展示图，并可关联技能点。面向儿童端经络认知学习内容。

#### 8.2 页面结构
- **搜索区**：经络名称 `jingluoName`、分类 `jingluoCatagory`、关联技能 `skillId`。
- **表格列**：编号 `jingluoId` / 经络名称 / 分类 / 循行次序 `jingluoOrder`（ellipsis）/ 内容说明 `illness`（ellipsis）/ 配图 `jingluoPic`（缩略图）/ 关联技能 `skillId` / 操作（编辑/删除），横向滚动 `x:1320`。
- **编辑弹层**（`AdminEditorSurface` modal，宽 820）：
  - 经络名称（必填）、经络分类（必填，占位示例"十二经脉、十二经别、奇经八脉"）
  - 循行/次序 `jingluoOrder`（3 行）、主治病症 `illness`（4 行）
  - 关联技能 `skillId`（数字框，可为空）
  - 经络图 `jingluoPic`（picture-card 单图，≤5MB，`JINGLUO` 类型）

#### 8.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置/刷新；`AdminPageError` 兜底加载失败重试。

#### 8.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/JingluoAdminApi` | `getJingluoPage` | `/admin/jingluo/page` | GET |
| `@/api/JingluoAdminApi` | `createJingluo` | `/admin/jingluo/create` | POST |
| `@/api/JingluoAdminApi` | `updateJingluo` | `/admin/jingluo/{id}` | PUT |
| `@/api/JingluoAdminApi` | `deleteJingluo` | `/admin/jingluo/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

#### 8.5 与前台对应关系
对应前台经络认知玩法：`frontend/MeridianRiver.vue`（经络河——经络路线学习）与 `frontend/CopperMan.vue`、`BodyMap.vue` 等；`jingluoPic` 即儿童端展示的经络图。

#### 8.6 特殊逻辑
- 字段 `illness` 语义为"主治病症"（说明文本，非疾病外键），与 illness 页无直接引用关系。
- 上传防重入：`uploading` 标志避免并发上传；服务器未返回路径时报"服务器未返回文件路径"。

---

### 9. mainline-level/index.vue — 主线关卡内容台（主线探案）

**文件**：`vue3\src\views\backend\mainline-level\index.vue`（1242 行，本项目最复杂的配置型页面之一）

#### 9.1 管理功能概述
管理儿童端**主线探案地图的 8 个关卡**的展示内容（标题/说明/封面/图标/印章）、解锁规则（前置关卡）、每关任务与奖励规则，并具备完整的**草稿-发布-版本回滚**内容管线。属"配置化工作台"而非传统表格 CRUD。

#### 9.2 页面结构
- **头部操作区**：版本状态标签（`DRAFT→草稿 v{n}` 橙 / 其他→已发布绿）、刷新、**保存草稿**、**发布配置**（发布前弹确认框，说明"已有任务进度保留原目标与奖励"）。
- **三栏工作台布局**：
  1. **关卡目录（左侧 rail）**：列出 8 关（`checkin` 等固定 id），显示顺序号、label、状态文案；提示"任务编码、路由和玩法绑定受保护"。
  2. **编辑面板（中间）**，按 fieldset 分组：
     - 展示内容：关卡标题 `label`（≤32 字）、状态文案 `statusText`、关卡说明 `description`（≤160 字）、节点图标 `icon`（8 枚徽章 1-8）、节点印章 `seal`（今/锁/案/星/修/阅/行/启）、关卡封面 `coverPath`（图片上传，JPG/PNG/GIF/BMP/WebP ≤10MB，可移除）。
     - 解锁规则：只读显示固定 ID/前端路由/地图键；**前置关卡 `prerequisiteLevelIds`**（多选，仅可选排在本关之前的关卡，首关禁用）。
     - 本关任务与奖励：只读显示任务编码/玩法类型/玩法入口；任务名称 `name`、任务说明 `description`、**完成目标 `target`**（`meridian-river-completion` 上限 14，其余 ≤999）；**奖励列表 `rewards`**（动态行：类型 MATERIAL 材料 / SCORE 积分，材料从 `@/data/materials` 常量中排除 collectible 后供选择，数量/分值 1-999）。
  3. **儿童端预览（右侧）**：模拟任务卡（封面、第 N 关、状态文案、标题、说明、任务名+目标、安全提示"只观察、只学习，不自己针刺"）；下方**版本记录**（最近 5 版，历史版本可"恢复为草稿"）。

#### 9.3 支持的操作
- 编辑 8 关展示与解锁/奖励配置
- **保存草稿**（保存前先调校验接口）
- **发布配置**（幂等 key `mainline-publish:{revisionId}:{editVersion}`）
- **恢复历史版本为新草稿**
- 封面上传（`uploadImage` → `coverPath`）
- 奖励行动态增删

#### 9.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/MainlineConfigApi` | `getAdminMainlineConfig` | `/admin/mainline-config` | GET |
| `@/api/MainlineConfigApi` | `saveMainlineDraft` | `/admin/mainline-config/draft` | PUT |
| `@/api/MainlineConfigApi` | `validateMainlineDraft` | `/admin/mainline-config/draft/validate` | POST |
| `@/api/MainlineConfigApi` | `publishMainlineDraft` | `/admin/mainline-config/draft/publish` | POST |
| `@/api/MainlineConfigApi` | `restoreMainlineVersion` | `/admin/mainline-config/versions/{version}/restore` | POST |
| `@/api/FileApi` | `uploadImage` | `/file/simple/upload/image` | POST |
| `@/data/materials` | — | 前端常量（奖励材料选项） | — |

（前台读取接口为 `getMainlineConfig` → `GET /game/mainline-config`）

#### 9.5 与前台对应关系
对应前台 `frontend/HomeMapLanding.vue`（主线探案地图页）与各关卡任务页（`route`/`mapKey` 即前台路由与地图键）；发布后标题/说明/封面立即生效，任务目标与奖励规则影响前台任务进度与发放。

#### 9.6 特殊逻辑
- **草稿/发布双状态机**：加载已发布数据时置为 `LOCAL_DRAFT`（清空 revisionId/editVersion），首次保存才生成新 revision；发布前必先有 revisionId。
- **服务端校验**：保存/发布前调用 `validateMainlineDraft`，错误展示在黄色 warning 横幅（"配置尚未通过发布校验"+错误列表）。
- **只读保护字段**：id/route/mapKey/taskCode/taskType 只读展示，防止历史进度断链。
- 深层 payload 规范化：`toPayload()` 对每关/每任务/每奖励做字段白名单 + 类型归一（reward 按类型只保留对应字段）。
- 响应式三栏 → 双栏 → 单栏；`prefers-reduced-motion` 降级。

---

### 10. quiz-question/index.vue — 题库管理

**文件**：`vue3\src\views\backend\quiz-question\index.vue`（516 行）

#### 10.1 管理功能概述
维护"安全课堂与学习复习"使用的**四选一选择题库**：题目、A-D 选项、正确答案、解析、分类、难度（1 简单/2 中等/3 困难）、启停状态。

#### 10.2 页面结构
- **搜索区**：题目关键词 `title`、分类 `category`、难度（下拉：简单/中等/困难）、状态（下拉：禁用/启用）。
- **表格列**：编号 `id` / 题目内容 `title`（宽 260 ellipsis）/ 正确答案 `correctAnswer` / 分类 `category` / 难度（标签化显示）/ 状态（`1→启用绿`、`0→禁用`）/ 创建时间 / 操作（编辑/删除），横向滚动 `x:1200`。
- **编辑弹层**（`AdminEditorSurface` modal，宽 720）：
  - 题目内容 `title`（textarea，≤500 字，必填）
  - 选项 A/B/C/D `optionA~D`（各 ≤200 字，必填）
  - 正确答案 `correctAnswer`（radio-button A/B/C/D）
  - 答案解析 `explanation`（可选，4 行）
  - 题目分类 `category`（可选）
  - 难度 `difficulty`（下拉 1/2/3）
  - 状态 `status`（radio 1 启用 / 0 禁用）

#### 10.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置/刷新；表单校验：题目与四个选项均必填。

#### 10.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/QuizQuestionAdminApi` | `getQuizQuestionPage` | `/admin/quiz-question/page` | GET |
| `@/api/QuizQuestionAdminApi` | `createQuizQuestion` | `/admin/quiz-question/create` | POST |
| `@/api/QuizQuestionAdminApi` | `updateQuizQuestion` | `/admin/quiz-question/{id}` | PUT |
| `@/api/QuizQuestionAdminApi` | `deleteQuizQuestion` | `/admin/quiz-question/{id}` | DELETE |

#### 10.5 与前台对应关系
对应前台 `frontend/quiz-game/index.vue`（安全课堂答题）、`frontend/Review.vue`（复习）、`frontend/MyMistakes.vue`（错题本）：启用状态的题目进入抽题池；后台"禁用"即从前台抽题中剔除；`status` 0/1 直接控制可见性。

#### 10.6 特殊逻辑
- 难度/状态均有"搜索选项"与"表单选项"两套常量（搜索带"全部/禁用"等空态语义）。
- 提交 payload 中 `explanation`/`category` 空值转 `undefined`（不提交脏数据）。

---

### 11. skills/index.vue — 技能点管理

**文件**：`vue3\src\views\backend\skills\index.vue`（457 行）

#### 11.1 管理功能概述
维护全站"技能点"主数据：`skillId`（业务主键，**手动指定且唯一**）、名称、类别、所属板块、积分、图片、简介、详细介绍。技能点是整个学习体系的核心关联键（名医故事/拓展疗法/经络/疾病/针法均通过 `skillId` 挂靠）。

#### 11.2 页面结构
- **搜索区**：技能名称 `skillName`、类别 `skillCategory`、所属板块 `skillType`。
- **表格列**：`skillId` / `skillName` / `skillCategory` / `skillType` / `skillScore` / `skillPic`（缩略图）/ 操作（编辑/删除）。
- **编辑弹窗**（`a-modal` 宽 920，双列布局）：
  - `skillId`（`a-input-number`，**编辑时 disabled 不可改**，必填且唯一）
  - 技能名称 `skillName`（必填）
  - 类别 `skillCategory`、所属板块 `skillType`
  - 积分 `skillScore`（文本输入，示例"10"）
  - 图片 `skillPic`（picture-card 单图，≤5MB 校验 + image 类型校验，`SKILLS` 类型）
  - 简介 `skillBriefDescription`（3 行）、详细介绍 `skillDescription`（5 行）

#### 11.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置；回调式 API。

#### 11.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/SkillAdminApi` | `pageSkills` | `/admin/skills/page` | GET |
| `@/api/SkillAdminApi` | `createSkill` | `/admin/skills` | POST |
| `@/api/SkillAdminApi` | `updateSkill` | `/admin/skills/{id}` | PUT |
| `@/api/SkillAdminApi` | `deleteSkill` | `/admin/skills/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

#### 11.5 与前台对应关系
技能点是前台"技能背包/收藏"（`frontend/Bag.vue`、`frontend/MyWorld.vue`）、身体地图（`frontend/BodyMap.vue`）、安全学习（`frontend/Safety.vue`）等玩法的基础数据源；`skillScore` 关联学习积分，`skillPic` 为前台技能图标。

#### 11.6 特殊逻辑
- `skillId` 由运营手动录入并唯一（编辑锁定），说明其被其他表大量引用（外键语义）。
- 学习行为数据（积分/等级/徽章）由系统自动生成，此处仅维护技能本身的积分展示属性。

---

### 12. start-page/index.vue — 开场故事管理

**文件**：`vue3\src\views\backend\start-page\index.vue`（587 行）

#### 12.1 管理功能概述
维护孩子**进入杏林探险前看到的故事**（开场引导）：标题、副标题、正文、三张图片、引导视频，并可关联技能点。

#### 12.2 页面结构
- **搜索区**：标题 `title`（关键词）、关联技能 `skillId`。
- **表格列**：编号 `id` / 标题 `storyTitle` / 副标题 `storySubtitle` / 关联技能 `skillId` / 图片（`storyPic1~3` 缩略图）/ 媒体（有→蓝色"有"标签 + **预览**按钮弹视频 modal）/ 操作（编辑/删除），横向滚动 `x:1180`。
- **编辑弹层**（`AdminEditorSurface` modal，宽 820）：
  - 标题 `storyTitle`（必填）、副标题 `storySubtitle`
  - 正文 `storyText`（5 行）
  - 关联技能 `skillId`（数字框，可为空）
  - 图片1/2/3 `storyPic1~3`（picture-card，≤5MB，`START` 类型）
  - 媒体文件 `media`（`video/*`，**≤200MB**，上传后表单内嵌 `<video controls>` 实时预览）

#### 12.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置/刷新、**媒体（视频）在线预览**（列表行与表单内均可）。

#### 12.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/OriginStoryAdminApi` | `getOriginStoryPage` | `/admin/originstory/page` | GET |
| `@/api/OriginStoryAdminApi` | `createOriginStory` | `/admin/originstory/create` | POST |
| `@/api/OriginStoryAdminApi` | `updateOriginStory` | `/admin/originstory/{id}` | PUT |
| `@/api/OriginStoryAdminApi` | `deleteOriginStory` | `/admin/originstory/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

#### 12.5 与前台对应关系
对应儿童端进入探险前的开场引导页（注册/登录后的故事导入），`storyPic1~3` 顺序展示插图、`media` 播放引导视频，`skillId` 可关联初始技能引导。

#### 12.6 特殊逻辑
- 视频上限 200MB（全站最大），表单内置原生 `<video>` 预览。
- 列表行"预览"通过独立 `a-modal`（footer=null）播放视频。

---

### 13. train-game/index.vue — 小火车关卡管理

**文件**：`vue3\src\views\backend\train-game\index.vue`（328 行）

#### 13.1 管理功能概述
维护"经络小火车"游戏的**五个学习节点**：每条经络记录对应 5 个结点全称（左侧说明）与 5 个结点简称（车厢展示）。

#### 13.2 页面结构
- **搜索区**：经络名称 `jingluoName`。
- **表格列**（13 列，横向滚动 `x:1700`）：编号 `id`（fixed left）/ 经络名称 `jingluoName`（fixed left）/ 节点一~五 `game1~game5` / 简称一~五 `game1Brief~game5Brief` / 操作（编辑/删除，fixed right）。
- **编辑弹层**（`AdminEditorSurface` modal，宽 920）：
  - 经络名称 `jingluoName`（必填）
  - divider"结点全称（左侧说明）"：`game1~game5` 五个输入框
  - divider"结点简称（车厢展示）"：`game1Brief~game5Brief` 五个输入框

#### 13.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置/刷新；无图片上传。

#### 13.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/TrainGameAdminApi` | `getTrainGamePage` | `/admin/train-game/page` | GET |
| `@/api/TrainGameAdminApi` | `createTrainGame` | `/admin/train-game/create` | POST |
| `@/api/TrainGameAdminApi` | `updateTrainGame` | `/admin/train-game/{id}` | PUT |
| `@/api/TrainGameAdminApi` | `deleteTrainGame` | `/admin/train-game/{id}` | DELETE |

#### 13.5 与前台对应关系
对应前台 `frontend/ShuntingGame.vue`（经络小火车/调车游戏）：节点全称用于左侧学习说明，简称用于火车车厢上的短标签。

#### 13.6 特殊逻辑
- 纯文本配置页，数据模型固定为"1 经络 + 5 全称 + 5 简称"，字段与数据库列一一对应。

---

### 14. user/index.vue — 用户数据管理（最复杂的运营台之一）

**文件**：`vue3\src\views\backend\user\index.vue`（1362 行）

#### 14.1 管理功能概述
"杏林运营台 · 用户管理"：从账号状态到学习档案，集中维护每位小侦探的成长轨迹。功能面最广：**统计总览 + 用户列表检索 + 成长档案抽屉（4 个标签页）+ 账号操作（新增/编辑/启停/重置密码）**。

#### 14.2 页面结构
- **统计卡片（summary-grid）**：全部用户 `totalUsers` / 正常账号 `activeUsers` / 已禁用 `disabledUsers`（学习数据保留）/ 管理员 `adminUsers`。
- **筛选区**：关键词 `keyword`（用户名/姓名/邮箱模糊）、用户类型 `userType`（USER/ADMIN）、账号状态 `status`（1 正常/0 已禁用）。
- **表格列**（横向滚动 `x:1120`）：
  - 账号档案 `account`（头像 + displayName/name/username + @username）
  - 联系方式 `contact`（邮箱 + 手机号）
  - 身份信息 `identity`（角色 tag + 年龄·性别）
  - 状态 `status`（圆点胶囊：正常绿/已禁用橙）
  - 成长进度 `progress`（能量 `score` + `Lv.{level} · {levelName}`）
  - 最近更新 `updatedAt`（回退 createdAt）
  - 操作 `action`（档案按钮 + 更多下拉：编辑资料/禁用启用/重置密码；**当前登录管理员不可自禁**）
- **用户成长档案抽屉**（`a-drawer` 宽 820/100%，4 个 tab）：
  1. **基本资料**：姓名/年龄/性别/用户类型/邮箱/手机号 + 只读提示"积分、等级、徽章和学习记录由学习行为自动生成，管理员只能查看"。
  2. **学习概览**：等级横幅（当前等级名、能量分、等级进度条）、学习统计（签到天数、答题正确率+次数、徽章数、完成案件数）、探险地图节点轨道（当前/完成/锁定）、学习资产（技能背包/收藏/身体星点/错题记录）、小铜人档案（铜片/星砂/最近案件日期）。
  3. **学习记录**：**签到日历**（`a-calendar`，按年月请求，签到日渲染"签"角标）+ **答题记录表**（questionId/userAnswer/答对答错 tag/时间，独立分页）。
  4. **徽章与荣誉**：徽章网格（badgePath 头像 + 名称）+ 九级荣誉证书卡片（`detailUser.honor` 链接预览）。
- **三个操作弹窗**：
  - 新增普通用户：用户名（3-50 位字母数字下划线，必填）/姓名/邮箱（必填+格式）/手机号（`^1[3-9]\d{9}$`）/年龄（0-150）/性别/登录密码（≥8 位）+确认密码；顶部提示"新账号默认普通用户、初始状态正常、初始能量 10"。
  - 编辑用户资料：姓名/年龄/性别/用户类型（只读）/邮箱（必填）/手机号/头像路径（可填 `/files/...`）。
  - 重置密码：新密码（≥8 位）+确认；提示"新密码只写入账号，不记录操作日志"。

#### 14.3 支持的操作
查询/重置/刷新、**新增普通用户**、**编辑资料**、**禁用/启用账号**（`Modal.confirm` 二次确认，禁用按钮 danger）、**重置密码**、查看成长档案（含签到/答题/徽章/证书）。

#### 14.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/user` | `getUserPage` | `/user/page` | GET |
| `@/api/user` | `getAdminUserSummary` | `/user/admin/summary` | GET |
| `@/api/user` | `createAdminUser` | `/user/admin/create` | POST |
| `@/api/user` | `getAdminUserOverview` | `/user/admin/overview/{id}` | GET |
| `@/api/user` | `updateAdminUserProfile` | `/user/admin/{id}/profile` | PUT |
| `@/api/user` | `updateAdminUserStatus` | `/user/admin/{id}/status` | PUT |
| `@/api/user` | `resetAdminUserPassword` | `/user/admin/{id}/password` | PUT |
| `@/api/user` | `getAdminUserCheckins` | `/user/admin/checkins/{id}` | GET |
| `@/api/user` | `getAdminUserQuizHistory` | `/user/admin/quiz-history/{id}` | GET |

（`useUserStore` 提供当前登录者 id 用于自禁保护；`resolveMediaUrl` 统一解析头像/徽章/证书路径）

#### 14.5 与前台对应关系
对应前台用户端：`frontend/MyWorld.vue`（我的世界/成长档案）、`frontend/Settings.vue`（资料设置）、`frontend/Badges.vue`（徽章）、`frontend/MyMistakes.vue`（错题）、签到（`user/checkin/today`）与九级荣誉证书提交（`user/certificate`）。后台档案数据即前台学习行为的汇总视图。

#### 14.6 特殊逻辑
- **权限护栏**：`record.id === currentUserId && status===1` 时禁用"禁用账号"菜单项；`toggleStatus` 对当前登录管理员再拦截一次。
- **懒加载**：档案抽屉打开时只拉 overview；切到"学习记录"tab 才并行拉签到与答题历史；切月触发 `getAdminUserCheckins({year, month})`。
- 提交后联动刷新：`fetchUserList + fetchSummary + reloadDetail` 三路并行，保持列表/统计/抽屉一致。
- 表单校验失败通过 `error.errorFields` 区分（antd 校验错误不弹 message 顶格提示）。

---

### 15. xuewei/index.vue — 小铜人内容中心（穴位星图 + 探案故事工坊）

**文件**：`vue3\src\views\backend\xuewei\index.vue`（127 行但为高度压缩写法，功能密度全站最高）

#### 15.1 管理功能概述
"人体探案实验室 · 内容运营"统一内容中心，四个标签页：
1. **穴位星图**：穴位图鉴 CRUD + 3D 就绪状态 + 发布状态流转；
2. **故事工坊**：儿童探案故事（多场景+线索+推理+安全回顾+奖励）的构建器；
3. **发布队列**：待审核/待发布/定时/发布失败内容集中处理；
4. **推送记录**：站内内容推送列表（每次成功发布生成一条推送，含已读统计与撤回标记）。

页面头部明确 5-8 岁内容边界："只做文化观察，不提供诊断、疗效或身体操作指导"。顶部分类指标条：穴位图鉴总数 `acupointTotal`、3D 已就绪 `modeledTotal`、已发布故事 `storyPublished`、待处理（审核+定时）数。

#### 15.2 页面结构（按 tab）
- **穴位星图 tab**：
  - 筛选：关键词（名称/编号/经络）、状态（ALL/DRAFT/IN_REVIEW/APPROVED/SCHEDULED/PUBLISHED/ARCHIVED）、3D 就绪状态（全部/3D可探案/仅图鉴）。
  - 表格列：穴位星（name+code+pinyin）/ 经络 `meridian_name` / 身体区域 `body_area` / 3D 状态（"3D可探案"绿/"仅图鉴"）/ 发布状态（tag 色映射）/ 儿童文化档案 `child_description` / 操作（编辑、版本、按状态出现：草稿→提交审核、待审核→通过、已审核/定时/已发布→下拉"发布操作"：立即发布/定时发布/归档撤下）。
  - **穴位编辑器**（`AdminEditorSurface` drawer 宽 980，"保存草稿"）：三栏布局——
    - 专业档案：标准编号 `code`（编辑锁定）、顺序号 `pointNumber`、名称、拼音、经络编号/名称、身体区域、标准定位（仅管理员可见）、内容来源、来源链接；
    - 5-8 岁儿童卡片：模型观察提示 `childLocation`（≤120 字）、文化星小档案 `childDescription`（≤160 字）、安全提醒 `safetyTip`（默认"只看3D铜人和文化图卡…"）+ 实时儿童卡片预览；
    - 3D 就绪检查：modelId、X/Y/Z 坐标（三者齐全→`hasCoordinates` 计算属性→"可进入每日探案"，否则"仅在图鉴发布"）、图鉴排序 `sortOrder`。
- **故事工坊 tab**：
  - 筛选：关键词（故事名称）、状态。
  - 故事卡片网格：封面（cover_path 背景图）+ 状态 tag + `story_code · v{version_no}` + 标题 + 简介 + "N 个场景 · M 条线索" + 操作（编辑预览、版本记录、提交审核/审核通过/发布操作）。
  - **故事构建器**（drawer 宽 1040）：基本信息（故事编号锁定/名称/分钟 2-15/副标题/推送简介 ≤160/封面路径）+ 四个 BuilderHeading 区块：
    1. 发生事件→观察场景→揭晓答案：**场景 pages 动态列表**（≥3 个：标题/图片路径/正文短句/观察提示）；
    2. 收集线索：**线索 clues 动态列表**（≥2 条：名称/描述）；
    3. 做出判断：推理问题 prompt + 正确/干扰选项 + 解释；
    4. 安全回顾与奖励：安全问题 + 选项 + 结案口令 + 奖励（名称/材料编号/数量）；
    - 右侧实时儿童端预览（封面/标题/副标题/场景大纲/安全口令）。
- **发布队列 tab**：表格（类型：探案故事/穴位星图、内容、版本、状态含"发布失败"红 tag+错误信息、定时时间、操作：审核通过/立即发布/设定时间）。
- **推送记录 tab**：表格（类型 tag：新故事/新穴位/已撤回、推送内容 title+summary、目标路由 route、已读人数 read_count、发布时间）。
- **版本记录弹窗**：版本号/状态/操作留痕（提交人·审核人·发布人）/更新时间/操作（非草稿版可"复制为新草稿"）。

#### 15.3 支持的操作
- 穴位：新增/编辑（保存草稿）、**状态流转**（submit 提交审核 → approve 审核通过 → publish 立即发布 / schedule 定时发布 / archive 归档撤下）、版本查看与恢复。
- 故事：新建/编辑（保存草稿）、同样五态流转、版本恢复。
- 队列：集中审核通过、立即发布、设定定时时间。
- 定时发布弹窗（默认 30 分钟后，必须未来时间）。

#### 15.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/CopperContentApi` | `getCopperContentOverview` | `/admin/copper-content/overview` | GET |
| `@/api/CopperContentApi` | `getCopperAcupointPage` | `/admin/copper-content/acupoints` | GET |
| `@/api/CopperContentApi` | `getCopperAcupoint` | `/admin/copper-content/acupoints/{code}` | GET |
| `@/api/CopperContentApi` | `saveCopperAcupointDraft` | `/admin/copper-content/acupoints/{code}/draft` | PUT |
| `@/api/CopperContentApi` | `getCopperStoryPage` | `/admin/copper-content/stories` | GET |
| `@/api/CopperContentApi` | `getCopperStory` | `/admin/copper-content/stories/{code}` | GET |
| `@/api/CopperContentApi` | `saveCopperStoryDraft` | `/admin/copper-content/stories/{code}/draft` | PUT |
| `@/api/CopperContentApi` | `transitionCopperContent` | `/admin/copper-content/{type}/{key}/{action}` | POST |
| `@/api/CopperContentApi` | `getCopperContentReleases` | `/admin/copper-content/releases` | GET |
| `@/api/CopperContentApi` | `getCopperContentRevisions` | `/admin/copper-content/{type}/{key}/revisions` | GET |
| `@/api/CopperContentApi` | `restoreCopperContentRevision` | `/admin/copper-content/{type}/{key}/revisions/{version}/restore` | POST |

（前台侧另有 `/acupuncture/copper-content/stories`、`feed`、`progress`、`read` 等只读/进度接口）

#### 15.5 与前台对应关系
对应前台 `frontend/CopperMan.vue`（小铜人/穴位星图 3D 观察）、`frontend/BodyMap.vue`（身体地图穴位）、`frontend/AcupointSortGame.vue`（穴位排序游戏）、每日探案（3D 任务池取 `modeled` 穴位）；故事工坊内容对应前台探案故事播放；推送记录对应前台"站内推送 feed"（`getCopperContentFeed` / `markCopperContentRead`）。

#### 15.6 特殊逻辑
- **完整的内容生命周期状态机**：DRAFT → IN_REVIEW → APPROVED →（PUBLISHED | SCHEDULED），任意阶段可 ARCHIVED；发布生成推送，归档=撤回（`retracted_at` 红 tag）。
- **草稿隔离**：已发布内容编辑后生成新草稿（编辑器提示"不影响儿童端当前版本"），恢复历史版本 = 复制为新草稿重新走审核。
- **并发安全**：`transitionCopperContent` 携带幂等 key（`copper-content:{type}:{key}:{action}:{ts}`）。
- 发布队列用 `Promise.all` 并发拉取 6 个分页接口（穴位+故事 × IN_REVIEW/APPROVED/SCHEDULED）合并展示。
- 本地临时 id（`localId`）支撑场景/线索动态增删，保存时归一为 `scene-N`/`clue-N` 并剥离本地字段。
- 组件级局部注册：`ContentActions`（下拉发布操作）、`PanelHeading`、`BuilderHeading` 三个 `defineComponent` 渲染函数组件。

---

### 16. zhenfa/index.vue — 针法管理

**文件**：`vue3\src\views\backend\zhenfa\index.vue`（496 行）

#### 16.1 管理功能概述
维护"针法"学习内容：名称、简介、**三段式结构化正文**（每段含标题+内容：`toolsTitle1~3` / `toolsText1~3`）、三张配图，并可关联技能点。注意：项目定位为"文化观察/知识科普"，针法内容仅作认知学习材料。

#### 16.2 页面结构
- **搜索区**：名称 `toolsName`、技能 ID `skillId`。
- **表格列**：`toolsId` / `toolsName` / `toolsBrief`（ellipsis）/ `skillId` / 图片（`toolsPic1~3` 缩略图）/ 操作（编辑/删除）。
- **编辑弹窗**（`a-modal` 宽 920）：
  - 名称 `toolsName`（必填）
  - 简介 `toolsBrief`（3 行）
  - 第一部分标题/内容 `toolsTitle1`/`toolsText1`、第二部分、第三部分（各 3 行 textarea）
  - 技能 ID `skillId`
  - 图片1/2/3（picture-card，≤5MB，`ZHENFA` 类型）

#### 16.3 支持的操作
新增 / 编辑 / 删除（popconfirm）、搜索/重置；回调式 API + 白名单 payload。

#### 16.4 调用的 API
| API 模块 | 函数 | 接口路径 | 方法 |
|---|---|---|---|
| `@/api/ZhenfaAdminApi` | `getZhenfaPage` | `/admin/zhenfa/page` | GET |
| `@/api/ZhenfaAdminApi` | `createZhenfa` | `/admin/zhenfa/create` | POST |
| `@/api/ZhenfaAdminApi` | `updateZhenfa` | `/admin/zhenfa/{id}` | PUT |
| `@/api/ZhenfaAdminApi` | `deleteZhenfa` | `/admin/zhenfa/{id}` | DELETE |
| `@/api/FileApi` | `uploadSimpleFile` | `/file/simple/upload` | POST |

#### 16.5 与前台对应关系
对应前台 `frontend/Method.vue`（针法/手法学习页）与 `frontend/Clinic.vue`（诊所疾病→手法技能绑定 `tools1~4`），`toolsPic1~3` 为分步图解。

#### 16.6 特殊逻辑
- 三段式正文的标题/内容成对结构（`toolsTitleN`+`toolsTextN`），前台按段落渲染；与"拓展疗法"页结构高度相似（同一批早期模板代码）。

---

## 三、API 接口总表（后台域）

| 页面 | 列表/分页 | 新增 | 编辑 | 删除 | 其他 |
|---|---|---|---|---|---|
| Dashboard | `GET /dashboard/statistics` | — | — | — | — |
| 评论管理 | `GET /admin/post-comment/page` | — | — | `DELETE /admin/post-comment/{id}` | — |
| 反馈消息 | `GET /admin/community-post/page`(postType=反馈) | — | — | `DELETE /admin/community-post/{id}` | — |
| 帖子管理 | `GET /admin/community-post/page` | `POST /admin/community-post` | — | `DELETE /admin/community-post/{id}` | — |
| 名医故事 | `GET /admin/doctorstory/page` | `POST /admin/doctorstory/create` | `PUT /admin/doctorstory/{id}` | `DELETE /admin/doctorstory/{id}` | 上传 `/file/simple/upload` |
| 拓展疗法 | `GET /admin/extracourse/page` | `POST /admin/extracourse/create` | `PUT /admin/extracourse/{id}` | `DELETE /admin/extracourse/{id}` | 上传 |
| 疾病管理 | `GET /admin/illness/page` | `POST /admin/illness` | `PUT /admin/illness/{id}` | `DELETE /admin/illness/{id}` | 上传 |
| 经络管理 | `GET /admin/jingluo/page` | `POST /admin/jingluo/create` | `PUT /admin/jingluo/{id}` | `DELETE /admin/jingluo/{id}` | 上传 |
| 主线关卡 | `GET /admin/mainline-config` | `PUT /admin/mainline-config/draft` | 同上 | — | `POST …/draft/validate`、`POST …/draft/publish`、`POST …/versions/{v}/restore` |
| 题库管理 | `GET /admin/quiz-question/page` | `POST /admin/quiz-question/create` | `PUT /admin/quiz-question/{id}` | `DELETE /admin/quiz-question/{id}` | — |
| 技能点 | `GET /admin/skills/page` | `POST /admin/skills` | `PUT /admin/skills/{id}` | `DELETE /admin/skills/{id}` | 上传 |
| 开场故事 | `GET /admin/originstory/page` | `POST /admin/originstory/create` | `PUT /admin/originstory/{id}` | `DELETE /admin/originstory/{id}` | 上传 |
| 小火车 | `GET /admin/train-game/page` | `POST /admin/train-game/create` | `PUT /admin/train-game/{id}` | `DELETE /admin/train-game/{id}` | — |
| 用户管理 | `GET /user/page` | `POST /user/admin/create` | `PUT /user/admin/{id}/profile` | —（仅禁用） | `GET /user/admin/summary`、`GET /user/admin/overview/{id}`、`PUT /user/admin/{id}/status`、`PUT /user/admin/{id}/password`、`GET /user/admin/checkins/{id}`、`GET /user/admin/quiz-history/{id}` |
| 小铜人内容中心 | `GET /admin/copper-content/acupoints`、`…/stories` | `PUT …/acupoints/{code}/draft`、`PUT …/stories/{code}/draft` | 同上 | —（归档替代） | `GET …/overview`、`POST …/{type}/{key}/{action}`(submit/approve/publish/schedule/archive)、`GET …/releases`、`GET …/revisions`、`POST …/revisions/{v}/restore` |
| 针法管理 | `GET /admin/zhenfa/page` | `POST /admin/zhenfa/create` | `PUT /admin/zhenfa/{id}` | `DELETE /admin/zhenfa/{id}` | 上传 |

---

## 四、横向总结：特殊逻辑与架构特征

1. **内容安全边界**：作为儿童教育科普平台，页面文案反复强调"只观察、只学习，不自己针刺""不提供诊断、疗效或身体操作指导"（mainline-level、xuewei 等），运营侧有明确的内容红线提示。
2. **两套内容治理范式**：
   - 传统 CRUD（帖子/评论/反馈/技能/经络/针法/拓展疗法/疾病/开场故事/小火车/题库/名医故事）：即时生效，删除即下线。
   - **版本化发布管线**（主线关卡、小铜人穴位/故事）：DRAFT→校验→发布/定时→归档，支持历史版本恢复，编辑不覆盖线上版本，发布带幂等 key——这是全站最成熟的内容工程能力。
3. **上传体系统一**：全部经由 `uploadSimpleFile(file, 业务类型)` → `POST /file/simple/upload`（multipart，携带 type），业务类型按页面区分（START/VIDEO/EXTRACOURSE/ILLNESS/JINGLUO/SKILLS/ZHENFA 等），大小限制前端拦截（图片 5MB、视频 80~200MB）。
4. **错误处理两级化**：较新页面用 `AdminPageError`（整页错误+重试，不打断编辑表单），较旧页面仅 `message` 提示；多数页面统一 `showDefaultMsg:false` 后自行弹 success/error，保证交互反馈精确。
5. **删除回退约定**：删除后若当前页仅剩一条且非第一页，自动 `current-1`（12 个删除型页面统一实现）。
6. **响应式**：桌面端表格横向滚动 + 后台壳透明背景适配；移动端单列、抽屉全宽；ECharts 随窗口 resize。
7. **前后台关系**：后台 16 个页面 ≈ 前台学习玩法的"内容供给侧"——技能点是全局关联枢纽（skillId 贯穿名医故事/经络/疾病/针法/拓展疗法/开场故事），题库与用户档案是学习闭环的数据底座，社区三页是 UGC 治理面，主线关卡/小铜人/小火车/开场故事分别驱动地图、3D 探案、经络游戏与开场引导等前台体验。

---

### 附：文件读取清单（16/16 全部完整读取）
`Dashboard.vue`(505 行)、`community-comment/index.vue`(182)、`community-feedback/index.vue`(202)、`community-post/index.vue`(358)、`doctor-story/index.vue`(781)、`extracourse/index.vue`(397)、`illness/index.vue`(500)、`jingluo/index.vue`(451)、`mainline-level/index.vue`(1242)、`quiz-question/index.vue`(516)、`skills/index.vue`(457)、`start-page/index.vue`(587)、`train-game/index.vue`(328)、`user/index.vue`(1362，分段读取)、`xuewei/index.vue`(127)、`zhenfa/index.vue`(496)；并核对 13 个相关 API 模块的真实接口路径。

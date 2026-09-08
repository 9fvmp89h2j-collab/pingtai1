# 后端素材审计（2026-08-30）

## 审计范围与标记

- 范围：`springboot/files` 与 `springboot/src/main/resources` 内的图片、音视频和文档。
- 共发现 30 个媒体文件，合计 207,544,691 字节（约 197.9 MiB）：28 张图片、2 个 MP4。
- `springboot/src/main/resources` 没有媒体文件。
- 当前本机 MySQL 未运行，数据库引用依据仓库中的 `heritage_db.sql` 判断；删除前仍应在实际数据库再次查询。
- 🔴：仓库中没有任何引用，可直接列为优先删除候选。
- 🟠：旧版、错配或已经退出当前路由的业务素材，但仍有数据库记录；必须先替换/清理数据库引用。
- 🟡：用户上传内容或运行期生成内容，不属于系统视觉素材；是否删除取决于对应用户/帖子是否保留。
- 🟢：当前内容仍引用，或没有足够证据认定为旧素材。

## 🔴 优先删除候选：3 个，约 29.7 MiB

| 文件 | 大小 | 依据 |
|---|---:|---|
| `springboot/files/bussiness/extracourse/1775485874035.png` | 145,102 B | 源码、SQL 均未引用；与旧猫图完全重复。 |
| `springboot/files/bussiness/start/1776913259489.jpeg` | 23,316 B | 源码、SQL 均未引用；与旧“鼻衄三穴”图完全重复。 |
| `springboot/files/video/1776913293560.mp4` | 30,814,493 B | 源码、SQL 均未引用，占空间最大。 |

## 🟠 明显不是当前视觉体系：建议替换引用后删除

### 旧猫形象：4 个仍被引用的副本

这套猫形象与当前“小铜人侦探”角色体系不一致。下列四个文件内容完全相同：

| 文件 | 当前引用 |
|---|---|
| `springboot/files/bussiness/skills/1775489483465.png` | `skills` 表第 2 条记录 |
| `springboot/files/bussiness/start/1775481627450.png` | `originstory` 第 2 条记录 |
| `springboot/files/bussiness/start/1775551123485.png` | `doctorstory` 第 1 条记录 |
| `springboot/files/bussiness/user_avatar/1775706807440.png` | 用户 1（test）的头像 |

建议：前三个业务内容换成当前角色/故事插画后删除；用户头像需先确认是否保留测试用户。

### 旧动漫“鼻衄三穴”图：3 个仍被引用的副本

该图使用明显不同的动漫人物画风，且不符合当前原创儿童科普视觉体系。下列三个文件内容完全相同：

| 文件 | 当前引用 |
|---|---|
| `springboot/files/bussiness/start/1775481228370.jpeg` | `originstory` 第 2 条记录 |
| `springboot/files/bussiness/start/1775551129067.jpeg` | `doctorstory` 第 1、2 条记录 |
| `springboot/files/bussiness/user_avatar/1775639796715.jpeg` | 用户 2（admin）的头像 |

建议：业务记录替换为当前版插画；管理员头像是否删除由账号保留情况决定。

### 内容错配或旧模块素材

| 文件 | 标记理由 | 删除前动作 |
|---|---|---|
| `springboot/files/bussiness/jingluo/1775707209070.png` | 当前数据库把它作为“手太阴肺经”图片，但画面是耳部医疗操作，内容明显错配且画风不统一。 | 在经络管理中上传正确的新版肺经图，再删除。 |
| `springboot/files/bussiness/zhenfa/1776267448776.png` | “微针疗法-耳针”却使用英文 `30days` 火炬徽章，内容错配；针法页面已不在当前路由。 | 清理 `zhenjiutools` 第 11 条或替换图片。 |
| `springboot/files/bussiness/illness/感冒.png` | 疾病小镇功能已不在当前前台/后台路由；图片仍被两条 `illness` 记录引用。 | 确认退役疾病小镇后，先清理两条记录引用。 |
| `springboot/files/bussiness/start/1775551111826.jpeg` | 名医故事仍引用的针刺手法示意图，视觉陈旧，并与当前“只观察、只学习、不自行针刺”的儿童安全表达不一致。 | 在名医故事第 1 条换成新版安全插画后删除。 |
| `springboot/files/bussiness/start/1776849124638.png` | 旧开场故事的简笔身体图，与当前身体地图角色不一致；开场故事后台仍存在，但前台 `/landing` 已重定向到新地图。 | 若退役旧开场故事，先清理 `originstory` 第 5 条。 |
| `springboot/files/video/1776849133516.mp4` | 约 155.5 MiB，仅由旧开场故事第 5 条引用；当前前台不展示开场故事。 | 确认退役该故事后清理引用，再删除；这是最大的可回收空间。 |

### 传承人旧模块：6 个

当前路由和管理导航没有传承人入口，以下文件只在旧 `sys_file_info` 中登记。它们是历史照片/装饰图，不一定“画风错误”，但属于当前产品未启用模块：

- `springboot/files/bussiness/inheritor/1760421025095.jpg`
- `springboot/files/bussiness/inheritor/1760598546765.jpeg`
- `springboot/files/bussiness/inheritor/1760598551991.jpeg`
- `springboot/files/bussiness/inheritor/1760598567833.jpeg`
- `springboot/files/bussiness/inheritor/1760598584591.jpeg`
- `springboot/files/bussiness/inheritor/1760598599968.jpeg`

建议：若确认不再恢复“传承人”模块，先删除实际数据库中的对应业务记录和 `sys_file_info`，再删除文件。

## 🟡 用户或社区数据：不要按系统素材直接删除

| 文件 | 类型/归属 |
|---|---|
| `springboot/files/bussiness/post_content/1776262922843.png` | 社区帖子 116 的旧徽章图片；与下面用户 4 的头像完全重复。 |
| `springboot/files/bussiness/user_avatar/1776305185952.png` | 用户 4（test1）的头像，实际是旧徽章图片。 |
| `springboot/files/bussiness/user_avatar/1776305158768.jpg` | 用户 4 的荣誉证书。 |
| `springboot/files/bussiness/user_avatar/1776310197343.jpg` | 用户 6 的荣誉证书。 |
| `springboot/files/bussiness/user_avatar/1776849272099.png` | 用户 7 的头像（旧人物插画）。 |
| `springboot/files/bussiness/user_avatar/1776849424686.jpg` | 用户 7 的荣誉证书。 |
| `springboot/files/bussiness/user_avatar/1776852287422.jpg` | 用户 8 的荣誉证书。 |
| `springboot/files/bussiness/user_avatar/1776911244488.jpg` | 用户 1 的荣誉证书。 |

这些素材看起来都来自测试账号或历史用户，但仅凭仓库无法确认实际账号是否可删。若决定清理测试用户，应由“删除测试账号及其帖子/证书”的数据清理流程统一处理，避免留下断链。

## 重复文件组

1. 旧猫图，共 5 份：`extracourse/1775485874035.png`、`skills/1775489483465.png`、`start/1775481627450.png`、`start/1775551123485.png`、`user_avatar/1775706807440.png`。
2. “鼻衄三穴”图，共 4 份：`start/1775481228370.jpeg`、`start/1775551129067.jpeg`、`start/1776913259489.jpeg`、`user_avatar/1775639796715.jpeg`。
3. 旧徽章图，共 2 份：`post_content/1776262922843.png`、`user_avatar/1776305185952.png`。

## 建议清理顺序

1. 先删除 3 个无引用文件，可回收约 29.7 MiB。
2. 在后台替换肺经图、名医故事旧图和开场故事旧图，确认页面显示正常。
3. 决定是否正式退役传承人、疾病小镇、针法和旧开场故事模块；同步删除数据库业务记录与 `sys_file_info`。
4. 最后处理测试用户、旧帖子和荣誉证书，避免误删真实用户数据。
5. 删除前对实际运行数据库再次执行媒体路径查询；本报告不能替代线上数据库核对。

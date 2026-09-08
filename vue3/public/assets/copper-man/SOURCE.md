# 小铜人模型来源记录

## 当前使用模型

- 模型：`modelo/corpo.obj`
- SHA-256：`65EA8D57217B4F79A0EA25FBB22E6AEC64F418062781A6E8568A2C1EB7794A0E`
- OBJ 文件头：`Blender v2.74 (sub 0) OBJ File: 'corpo.blend'`
- 首次仓库记录：提交 `a7ef84dfccc641f42f4271dc7130dcc4b5e68b9d`
- 接入页面：`src/views/frontend/CopperMan.vue`
- 场景位置：模型保持原始比例，Y 轴下移 95 个单位
- 穴位适配：直接使用成人模型的原始穴位坐标

页面使用 Three.js `OBJLoader` 加载模型，并为所有网格统一应用纯铜色物理材质。原有的 `textura/UV_Grid_Sm.jpg` 不再使用，避免网格测试纹理出现在铜人表面。

仓库中没有对应的 `corpo.blend`、建模记录、作者信息、下载地址或许可证，仅凭现有文件无法确认模型的原始作者、获取渠道或可用授权。

## 备用模型

- 超精修 Q 版 OBJ：`modelo/xiaotongren-ultra-refined.obj`
- 精修 Q 版 OBJ：`modelo/xiaotongren-refined.obj`
- 真实 Q 版 OBJ：`modelo/xiaotongren-real.obj`
- 实现：`src/utils/chibiCopperMan.js`
- 类型：项目内使用 Three.js 基础几何体与程序化曲面绘制

当 OBJ 加载失败时，页面会自动显示该备用模型。

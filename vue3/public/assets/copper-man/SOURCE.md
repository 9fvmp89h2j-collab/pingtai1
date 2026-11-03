# 小铜人模型来源记录

## 当前使用模型

- 模型：`modelo/corpo.obj`
- SHA-256：`51859A1DDD49FA9B6D3A8760B5DF5B38352A81A9F22E33A513DE9D66FD562302`
- OBJ 文件头：`Blender v2.74 (sub 0) OBJ File: 'corpo.blend'`
- 首次仓库记录：提交 `a7ef84dfccc641f42f4271dc7130dcc4b5e68b9d`
- 接入页面：`src/views/frontend/CopperMan.vue`
- 场景位置：模型保持原始比例，Y 轴下移 95 个单位
- 穴位适配：直接使用成人模型的原始穴位坐标

页面使用 Three.js `OBJLoader` 加载模型，并为所有网格统一应用纯铜色物理材质。原有的 `textura/UV_Grid_Sm.jpg` 不再使用，避免网格测试纹理出现在铜人表面。

仓库中没有对应的 `corpo.blend`、建模记录、作者信息、下载地址或许可证，仅凭现有文件无法确认模型的原始作者、获取渠道或可用授权。

页面只保留并加载上述成人模型；模型载入失败时会显示重试提示。

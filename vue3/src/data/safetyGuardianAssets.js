const asset = (file, label, alt, group) => ({ file, label, alt, group })

export const safetyGuardianAssets = {
  quiz: {
    'safety-self-needle': asset('safety-judge-no-self-needling.webp', '不自行针刺', '孩子看到关闭的针具收纳盒后退并向妈妈求助，小铜人举盾阻止靠近。', '第一关 · 判断题'),
    'safety-copy-video': asset('safety-judge-no-copy-video.webp', '不模仿网络视频', '孩子关闭平板并把设备推远，小铜人举盾提醒不要模仿危险动作。', '第一关 · 判断题'),
    'safety-tell-adult': asset('safety-judge-tell-adult.webp', '告诉可信任的大人', '孩子在教室向老师说明身体不舒服，老师弯腰认真倾听。', '第一关 · 判断题'),
    'safety-not-doctor': asset('safety-judge-learning-not-doctor.webp', '学习不能替代医生', '家长陪孩子在专业医生面前问诊，关闭的学习平板放在桌边。', '第一关 · 判断题'),
    'safety-learn-purpose': asset('safety-judge-learn-culture.webp', '学习身体与文化', '老师带孩子观察健康知识图册和铜人模型，只进行文化学习。', '第一关 · 判断题'),
    'safety-sterile-needles': asset('safety-judge-sterile-single-use.webp', '一次性无菌包装', '戴手套的医务人员检查完全密封的不透明独立医疗包装。', '第一关 · 判断题'),
    'safety-no-sharing': asset('safety-judge-no-sharing.webp', '不共用针具', '洁净诊室内两份独立密封医疗包装分开放置，小铜人举盾形成界线。', '第一关 · 判断题'),
    'safety-stop-classmate': asset('safety-scene-stop-classmate.webp', '制止同学危险尝试', '教室里孩子劝阻同学靠近危险工具，并准备告诉老师。', '第一关 · 情境题'),
    'safety-family-treatment': asset('safety-scene-family-treatment.webp', '劝阻家人自行处理', '孩子劝阻家人自行治疗，家长准备陪同前往专业医疗机构。', '第一关 · 情境题'),
    'safety-online-video': asset('safety-scene-online-video.webp', '关闭错误教学视频', '孩子关闭网络视频并向家长求助，小铜人守护在旁。', '第一关 · 情境题'),
    'safety-playground-refuse': asset('safety-scene-playground-refuse.webp', '拒绝操场危险游戏', '孩子在操场拒绝同伴用尖锐物品玩危险游戏，并远离现场。', '第一关 · 情境题'),
    'safety-help-grandma': asset('safety-scene-help-grandma.webp', '陪奶奶寻求专业帮助', '孩子劝阻头痛的奶奶自行处理，并准备陪她去看医生。', '第一关 · 情境题'),
    'safety-found-needle-kit': asset('safety-scene-found-needle-kit.webp', '远离来历不明的工具包', '孩子在社区路边与关闭的旧工具包保持距离并向家长呼喊。', '第一关 · 情境题'),
    'safety-pet-vet': asset('safety-scene-pet-vet.webp', '带宠物去兽医处', '孩子劝阻给宠物自行操作，家长准备带小狗去宠物医院。', '第一关 · 情境题')
  },
  scenarios: {
    'scenario-unwell': asset('scenario-unwell-main.webp', '身体不舒服主场景', '孩子捂着肚子向家长求助。', '第二关 · 主场景'),
    'scenario-classmate': asset('scenario-classmate-main.webp', '同学危险尝试主场景', '孩子在教室阻止同学靠近危险工具。', '第二关 · 主场景'),
    'scenario-video': asset('scenario-video-main.webp', '网络错误教学主场景', '孩子警觉地看着平板上的模糊错误教学内容。', '第二关 · 主场景'),
    'scenario-playground': asset('scenario-playground-main.webp', '操场尖锐物主场景', '孩子们在操场与地面的不明尖锐物保持距离。', '第二关 · 主场景'),
    'scenario-grandma': asset('scenario-grandma-main.webp', '奶奶头痛主场景', '孩子关心头痛的奶奶并阻止她自行处理。', '第二关 · 主场景'),
    'scenario-pet': asset('scenario-pet-main.webp', '宠物不舒服主场景', '孩子陪着不舒服的小狗，准备寻求专业帮助。', '第二关 · 主场景')
  },
  actions: {
    'tell-parent': asset('scenario-unwell-tell-parent.webp', '告诉家长', '孩子告诉家长身体不舒服。', '第二关 · 行动选项'),
    'hide-it': asset('scenario-unwell-hide.webp', '忍着不说', '孩子独自忍耐身体不适。', '第二关 · 行动选项'),
    'self-try': asset('scenario-unwell-self-try.webp', '自己找工具尝试', '孩子准备自行处理身体问题。', '第二关 · 行动选项'),
    watch: asset('scenario-classmate-watch.webp', '站在旁边观看', '孩子站在旁边围观同学的危险尝试。', '第二关 · 行动选项'),
    'tell-teacher': asset('scenario-classmate-tell-teacher.webp', '制止并告诉老师', '孩子举手制止同学并招呼老师来帮助。', '第二关 · 行动选项'),
    join: asset('scenario-classmate-join.webp', '加入危险尝试', '孩子准备参与同伴提出的危险游戏。', '第二关 · 行动选项'),
    copy: asset('scenario-video-copy.webp', '照着视频模仿', '孩子准备模仿平板上的危险教学动作。', '第二关 · 行动选项'),
    share: asset('scenario-video-share.webp', '分享错误视频', '孩子准备把错误教学视频分享给同学。', '第二关 · 行动选项'),
    'close-video': asset('scenario-video-close.webp', '关闭并询问大人', '孩子关闭视频并向家长询问。', '第二关 · 行动选项'),
    'pick-up': asset('scenario-playground-pick.webp', '捡起来查看', '孩子准备捡起地面的不明尖锐物品。', '第二关 · 行动选项'),
    'keep-away': asset('scenario-playground-report.webp', '远离并告诉老师', '孩子远离不明物品并向老师报告。', '第二关 · 行动选项'),
    'take-home': asset('scenario-playground-take-home.webp', '带回家收藏', '孩子准备把不明物品带回家。', '第二关 · 行动选项'),
    'find-map': asset('scenario-grandma-search.webp', '查找操作图', '孩子准备帮奶奶查找自行操作的图片。', '第二关 · 行动选项'),
    ignore: asset('scenario-grandma-ignore.webp', '假装没有看见', '孩子转身离开正在不舒服的奶奶。', '第二关 · 行动选项'),
    'seek-doctor': asset('scenario-grandma-doctor.webp', '劝阻并陪她就医', '孩子陪奶奶寻求医生帮助。', '第二关 · 行动选项'),
    'online-copy': asset('scenario-pet-copy.webp', '照图自行尝试', '孩子准备照着网络图片给宠物自行操作。', '第二关 · 行动选项'),
    'ask-friend': asset('scenario-pet-friends.webp', '请同学一起研究', '孩子邀请同学一起研究如何自行处理宠物问题。', '第二关 · 行动选项'),
    'visit-vet': asset('scenario-pet-vet.webp', '请家长带去宠物医院', '家长准备带小狗去宠物医院。', '第二关 · 行动选项')
  },
  memory: {
    'pair-unwell-left': asset('memory-unwell-situation.webp', '身体不舒服情境', '孩子轻捂肚子露出轻微不适，小铜人在旁关注。', '第三关 · 情境图'),
    'pair-unwell-right': asset('memory-unwell-action.webp', '告诉家长行动', '孩子主动告诉家长肚子不舒服，家长蹲下认真倾听。', '第三关 · 安全行动图'),
    'pair-sharp-left': asset('memory-sharp-situation.webp', '发现尖锐物情境', '孩子与地面远处的不明细长异物保持安全距离。', '第三关 · 情境图'),
    'pair-sharp-right': asset('memory-sharp-action.webp', '呼喊老师行动', '孩子在安全距离外呼喊老师，老师准备安全收纳。', '第三关 · 安全行动图'),
    'pair-video-left': asset('memory-video-situation.webp', '网络视频情境', '孩子警觉地看着平板上的模糊人物轮廓。', '第三关 · 情境图'),
    'pair-video-right': asset('memory-video-action.webp', '关闭视频行动', '孩子合上平板并向家长询问。', '第三关 · 安全行动图'),
    'pair-classmate-left': asset('memory-classmate-situation.webp', '同学危险尝试情境', '孩子坐在关闭的小工具盒旁准备危险尝试，另一孩子警觉后退。', '第三关 · 情境图'),
    'pair-classmate-right': asset('memory-classmate-action.webp', '制止并求助行动', '孩子在教室举手做停止手势并招呼老师。', '第三关 · 安全行动图'),
    'pair-pet-left': asset('memory-pet-situation.webp', '宠物不舒服情境', '孩子担心地看着安静趴在软垫上的小狗。', '第三关 · 情境图'),
    'pair-pet-right': asset('memory-pet-action.webp', '陪同就医行动', '家长提着装有小狗的宠物外出包准备就医。', '第三关 · 安全行动图'),
    'pair-learn-left': asset('memory-learn-situation.webp', '学习身体知识情境', '孩子翻看儿童健康知识图册，小铜人在旁持放大镜。', '第三关 · 情境图'),
    'pair-learn-right': asset('memory-learn-action.webp', '只观察学习行动', '孩子用放大镜观察穿衣铜人模型和无字健康图册。', '第三关 · 安全行动图'),
    'pair-sterile-left': asset('memory-sterile-situation.webp', '密封用品情境', '高处托盘放着完整密封的不透明医疗包装。', '第三关 · 情境图'),
    'pair-sterile-right': asset('memory-sterile-action.webp', '专业人员检查行动', '戴手套的医务人员检查完全密封的独立医疗包装。', '第三关 · 安全行动图'),
    'pair-dispose-left': asset('memory-dispose-situation.webp', '使用后回收情境', '诊室托盘旁有抽象医疗用品和专用封闭回收容器。', '第三关 · 情境图'),
    'pair-dispose-right': asset('memory-dispose-action.webp', '专业回收行动', '戴手套的医务人员将抽象医疗用品放入专业回收容器。', '第三关 · 安全行动图')
  },
  badges: {
    BEGINNER: asset('badge-beginner.png', '初级徽章', '墨绿色安全徽章上有一颗金色星星。', '第三关 · 难度徽章'),
    INTERMEDIATE: asset('badge-intermediate.png', '中级徽章', '墨绿色安全徽章上有两颗金色星星。', '第三关 · 难度徽章'),
    ADVANCED: asset('badge-advanced.png', '高级徽章', '墨绿色安全徽章上有三颗金色星星和放大镜。', '第三关 · 难度徽章'),
    CENTER: asset('badge-center-guardian.png', '中心守护徽章', '墨绿色盾牌拥抱金色安全铃铛。', '第三关 · 中心徽章')
  },
  pledge: {
    background: asset('certificate-safety-guardian.png', '证书背景', '奶油色安全守护案证书背景，中央留白用于叠加证书文字。', '第四关 · 证书'),
    badge: asset('certificate-guardian-badge.png', '安全小卫士徽章', '金色小铜人抱着墨绿色安全盾牌，周围有铃铛光环。', '第四关 · 证书'),
    seal: asset('certificate-safety-seal.png', '安全签章', '朱砂红与柔金组成的抽象安全签章，没有文字。', '第四关 · 证书'),
    cloud: asset('pledge-dialog-cloud.png', '祥云对话框', '透明背景祥云对话框装饰，中间留白用于宣誓文字。', '第四关 · 宣誓'),
    bell: asset('pledge-bell-lit.png', '点亮安全铃铛', '透明背景点亮的金色安全铃铛。', '第四关 · 宣誓')
  }
}

export const safetyAssetOptions = Object.values(safetyGuardianAssets)
  .flatMap(group => Object.values(group))
  .map(item => ({ value: item.file, label: `${item.label} · ${item.file}`, imageAlt: item.alt, group: item.group }))

export function safetyAssetByFile(file) {
  return safetyAssetOptions.find(item => item.value === file) || null
}

export function quizAssetFor(questionCode) {
  return safetyGuardianAssets.quiz[questionCode] || null
}

export function scenarioAssetFor(id) {
  return safetyGuardianAssets.scenarios[id] || null
}

export function actionAssetFor(id) {
  return safetyGuardianAssets.actions[id] || null
}

export function memoryAssetFor(pairId, side) {
  return safetyGuardianAssets.memory[`${pairId}-${side}`] || null
}

import { generatedMaterialIcons } from './generatedRewardAssets'

const rewardSafetyBell = generatedMaterialIcons['safety-bell']
const materialMugwort = generatedMaterialIcons['mugwort-floss']

export const safetyQuestions = [
  {
    id: 'safe-q-self-needle',
    name: '小朋友可以自己拿针扎穴位吗？',
    description: '判断是否可以自行尝试针刺。',
    status: 'available',
    type: 'true-false',
    answer: false,
    options: [
      { label: '可以', value: true },
      { label: '不可以', value: false }
    ],
    explanation: '不可以。针灸必须由专业人员操作，儿童不能自己尝试。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-q-copy-video',
    name: '可以模仿视频给自己针灸吗？',
    description: '判断是否可以照着视频做针灸。',
    status: 'available',
    type: 'true-false',
    answer: false,
    options: [
      { label: '可以', value: true },
      { label: '不可以', value: false }
    ],
    explanation: '不可以。视频不能替代医生和专业训练，不能模仿治疗。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-q-tell-adult',
    name: '身体不舒服应该告诉家长和医生吗？',
    description: '判断身体不舒服时应该怎么做。',
    status: 'available',
    type: 'true-false',
    answer: true,
    options: [
      { label: '应该', value: true },
      { label: '不用', value: false }
    ],
    explanation: '应该。身体不舒服要及时告诉家长，并由医生判断。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'mugwort-floss', name: '艾绒', count: 1, icon: materialMugwort }]
  },
  {
    id: 'safe-q-replace-doctor',
    name: '这个系统可以替代医生看病吗？',
    description: '判断本系统是否能作为医疗诊断或治疗。',
    status: 'available',
    type: 'true-false',
    answer: false,
    options: [
      { label: '可以', value: true },
      { label: '不可以', value: false }
    ],
    explanation: '不可以。本系统只用于针灸文化科普和穴位认知学习。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-q-learn-body',
    name: '学习穴位可以帮助我们认识身体吗？',
    description: '判断穴位认知学习的安全目标。',
    status: 'available',
    type: 'true-false',
    answer: true,
    options: [
      { label: '可以', value: true },
      { label: '不可以', value: false }
    ],
    explanation: '可以。我们学习的是身体认知和文化知识，不进行实际针刺。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'mugwort-floss', name: '艾绒', count: 1, icon: materialMugwort }]
  },
  {
    id: 'safe-q-acupuncture-pain',
    name: '针灸治疗时，医生会使用消毒过的一次性针具吗？',
    description: '了解针灸操作的安全规范。',
    status: 'available',
    type: 'true-false',
    answer: true,
    options: [
      { label: '会', value: true },
      { label: '不会', value: false }
    ],
    explanation: '会的。正规针灸治疗必须使用一次性无菌针具，确保卫生安全。这也是为什么不能自己在家操作的原因之一。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-q-share-needle',
    name: '可以和别人共用一根针灸针吗？',
    description: '判断共用针具的危险性。',
    status: 'available',
    type: 'true-false',
    answer: false,
    options: [
      { label: '可以', value: true },
      { label: '不可以', value: false }
    ],
    explanation: '绝对不可以。共用针具可能传播疾病，非常危险。针灸针必须一人一针，用完即弃。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-stop-friend',
    name: '小明看到同桌拿针要给小红扎合谷穴，小明应该怎么做？',
    description: '场景模拟：遇到同学自行针刺。',
    status: 'available',
    type: 'scenario',
    answer: 2,
    options: [
      { label: 'A', text: '觉得很有趣，一起玩', value: 0 },
      { label: 'B', text: '假装没看见，走开', value: 1 },
      { label: 'C', text: '立即制止，并告诉老师', value: 2 },
      { label: 'D', text: '在旁边观看学习', value: 3 }
    ],
    explanation: '正确的做法是立即制止并告诉老师。针灸是专业医疗行为，小朋友绝对不能自行操作，也不能旁观鼓励。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-stomach-ache',
    name: '小美肚子疼，妈妈说"我用针给你扎一下足三里就好了"，小美应该怎么回应？',
    description: '场景模拟：家人提出自行治疗。',
    status: 'available',
    type: 'scenario',
    answer: 1,
    options: [
      { label: 'A', text: '乖乖让妈妈扎针', value: 0 },
      { label: 'B', text: '告诉妈妈应该去医院找医生', value: 1 },
      { label: 'C', text: '自己查穴位图找穴位', value: 2 },
      { label: 'D', text: '忍着不说', value: 3 }
    ],
    explanation: '应该告诉妈妈去医院找医生。即使是最亲近的家人，也不能自行进行针刺治疗，必须由专业医生操作。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-online-video',
    name: '小华在网上看到一个"教你针灸"的视频，主播说"学会了自己在家就能治病"，小华应该怎么做？',
    description: '场景模拟：网络误导信息。',
    status: 'available',
    type: 'scenario',
    answer: 3,
    options: [
      { label: 'A', text: '认真学习，记下步骤', value: 0 },
      { label: 'B', text: '分享给同学一起学', value: 1 },
      { label: 'C', text: '买针回来练习', value: 2 },
      { label: 'D', text: '不信这种说法，关闭视频', value: 3 }
    ],
    explanation: '应该关闭视频，不信这种说法。网络上的"教学"不能替代专业医疗培训，绝不能照着视频自行操作。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-playground',
    name: '操场上，几个同学捡到一根针，有人提议"我们来玩针灸游戏，互相扎穴位"，你应该怎么做？',
    description: '场景模拟：同伴压力下的安全选择。',
    status: 'available',
    type: 'scenario',
    answer: 0,
    options: [
      { label: 'A', text: '坚决拒绝，告诉他们很危险', value: 0 },
      { label: 'B', text: '参与游戏，轮流扎', value: 1 },
      { label: 'C', text: '站在旁边看他们玩', value: 2 },
      { label: 'D', text: '帮他们找穴位图', value: 3 }
    ],
    explanation: '应该坚决拒绝并告诉他们很危险。任何时候都不能拿针扎自己或他人，这是非常危险的行为。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-grandma',
    name: '奶奶说头疼，拿出家里的针说要自己扎一下太阳穴，你应该怎么做？',
    description: '场景模拟：家人自行针刺。',
    status: 'available',
    type: 'scenario',
    answer: 2,
    options: [
      { label: 'A', text: '帮奶奶找穴位图', value: 0 },
      { label: 'B', text: '觉得奶奶很厉害', value: 1 },
      { label: 'C', text: '劝阻奶奶，陪她去医院', value: 2 },
      { label: 'D', text: '不管，去做自己的事', value: 3 }
    ],
    explanation: '应该劝阻奶奶并陪她去医院。太阳穴是危险部位，自行针刺非常危险，一定要让专业医生来处理。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-store-needle',
    name: '你在路边捡到一个针灸包，里面有几根针，你应该怎么做？',
    description: '场景模拟：捡到医疗器具。',
    status: 'available',
    type: 'scenario',
    answer: 3,
    options: [
      { label: 'A', text: '打开看看，很好奇', value: 0 },
      { label: 'B', text: '带回家当玩具', value: 1 },
      { label: 'C', text: '分给同学一起玩', value: 2 },
      { label: 'D', text: '不要碰，告诉大人处理', value: 3 }
    ],
    explanation: '不要碰，告诉大人处理。捡到的针可能被污染，有感染风险。而且针具不是玩具，绝不能拿来玩。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-scenario-pet',
    name: '小刚想给家里的小狗"针灸"治病，在网上查了穴位图，他应该怎么做？',
    description: '场景模拟：给宠物操作。',
    status: 'available',
    type: 'scenario',
    answer: 1,
    options: [
      { label: 'A', text: '按照穴位图给小狗扎针', value: 0 },
      { label: 'B', text: '带小狗去宠物医院', value: 1 },
      { label: 'C', text: '让同学帮忙按住小狗', value: 2 },
      { label: 'D', text: '自己先在自己身上练习', value: 3 }
    ],
    explanation: '应该带小狗去宠物医院。动物针灸需要兽医操作，小朋友不能给宠物扎针，也不能给自己扎针练习。',
    icon: rewardSafetyBell,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell }]
  },
  {
    id: 'safe-memory-match',
    name: '记忆翻牌',
    description: '翻开卡片，找到配对的安全知识。',
    status: 'available',
    type: 'memory',
    pairs: [
      { id: 'pair-1', left: '自己扎针', right: '绝对不能做！很危险', icon: 'safety-bell' },
      { id: 'pair-2', left: '身体不舒服', right: '告诉家长，去看医生', icon: 'safety-bell' },
      { id: 'pair-3', left: '学习穴位知识', right: '认识身体，了解文化', icon: 'safety-bell' },
      { id: 'pair-4', left: '同学要给我扎针', right: '拒绝并立刻告诉老师', icon: 'safety-bell' },
      { id: 'pair-5', left: '网络针灸教学', right: '不能模仿，找专业医生', icon: 'safety-bell' },
      { id: 'pair-6', left: '捡到针灸针', right: '不要碰，告诉大人', icon: 'safety-bell' },
      { id: 'pair-7', left: '针灸治疗', right: '必须由专业医生操作', icon: 'safety-bell' },
      { id: 'pair-8', left: '针灸针具', right: '一人一针，用完即弃', icon: 'safety-bell' }
    ],
    rewards: [
      { id: 'safety-bell', name: '安全铃铛', count: 3, icon: rewardSafetyBell },
      { id: 'mugwort-floss', name: '艾绒', count: 2, icon: materialMugwort }
    ]
  },
  {
    id: 'safe-sort-behaviors',
    name: '安全行为分类',
    description: '把行为放进"安全行为"、"需要成人帮助"和"不安全行为"三个篮子。',
    status: 'available',
    type: 'sort',
    answer: {
      safe: [
        '告诉家长身体不舒服',
        '学习身体知识',
        '观看科普动画',
        '学习中医文化',
        '认识穴位名称',
        '了解安全规则',
        '阅读健康绘本',
        '记住急救电话'
      ],
      needAdult: [
        '配合医生治疗',
        '在家长陪同下学习',
        '请医生扎针治疗',
        '去中医院参观',
        '咨询中医知识',
        '按时吃药',
        '让家长带去看医生',
        '询问医生健康问题'
      ],
      unsafe: [
        '自己扎针',
        '拿尖东西戳身体',
        '模仿治疗',
        '给同学针灸',
        '捡针玩',
        '给宠物扎针',
        '共用针具',
        '网上买针'
      ]
    },
    options: [
      '告诉家长身体不舒服', '自己扎针', '学习身体知识', '拿尖东西戳身体',
      '观看科普动画', '模仿治疗', '学习中医文化', '给同学针灸',
      '按时吃药', '捡针玩', '让家长带去看医生', '给宠物扎针',
      '配合医生治疗', '在家长陪同下学习', '认识穴位名称', '了解安全规则',
      '请医生扎针治疗', '阅读健康绘本', '共用针具', '网上买针',
      '去中医院参观', '咨询中医知识', '记住急救电话', '询问医生健康问题'
    ],
    explanation: '安全行为：自己独立就能做的——学习知识、了解身体、记住安全规则。\n需成人帮助：正确的事，但需要大人或医生参与——吃药、看医生、去医院。\n不安全行为：绝对不能做的事——自己扎针、模仿治疗、玩针具。',
    icon: rewardSafetyBell,
    rewards: [
      { id: 'safety-bell', name: '安全铃铛', count: 4, icon: rewardSafetyBell },
      { id: 'mugwort-floss', name: '艾绒', count: 3, icon: materialMugwort }
    ]
  }
]

export const safetyPledge = {
  id: 'safety-pledge',
  name: '安全宣誓',
  description: '我知道：本系统只用于学习中医针灸文化和认识穴位，不能自己针刺，也不能替代医生看病。',
  status: 'available',
  icon: rewardSafetyBell,
  rewards: [
    { id: 'safety-bell', name: '安全铃铛', count: 5, icon: rewardSafetyBell },
    { id: 'mugwort-floss', name: '艾绒', count: 3, icon: materialMugwort },
    { id: 'safe-little-healer', name: '安全小医者徽章', count: 1, icon: rewardSafetyBell }
  ]
}

export const achievements = [
  {
    id: 'ach-perfect-quiz',
    name: '知识达人',
    description: '知识闯关全部答对',
    icon: '🧠',
    condition: (stats) => stats.quizCorrectCount === stats.quizTotalCount
  },
  {
    id: 'ach-combo-master',
    name: '连击大师',
    description: '达成5连击以上',
    icon: '🔥',
    condition: (stats) => stats.maxCombo >= 5
  },
  {
    id: 'ach-speed-sort',
    name: '闪电分类',
    description: '在30秒内完成分类且全部正确',
    icon: '⚡',
    condition: (stats) => stats.sortTimeRemaining >= 15 && stats.sortAllCorrect
  },
  {
    id: 'ach-full-hearts',
    name: '满血通关',
    description: '知识闯关没有损失任何生命',
    icon: '❤️',
    condition: (stats) => stats.quizLivesRemaining === 3
  },
  {
    id: 'ach-memory-king',
    name: '记忆王者',
    description: '记忆翻牌少于10次翻牌完成',
    icon: '👑',
    condition: (stats) => stats.memoryFlips <= 20
  }
]

export default {
  safetyQuestions,
  safetyPledge,
  achievements
}

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
    explanation: '不可以。针刺必须由专业人员操作，儿童不能自己尝试。',
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
    id: 'safe-sort-behaviors',
    name: '安全行为分类',
    description: '把行为放进“安全行为”和“不安全行为”两个篮子。',
    status: 'available',
    type: 'sort',
    answer: {
      safe: ['告诉家长', '询问医生', '学习身体知识', '观看科普动画'],
      unsafe: ['自己扎针', '拿尖东西戳身体', '模仿治疗', '给同学针灸']
    },
    options: [
      '告诉家长',
      '自己扎针',
      '询问医生',
      '拿尖东西戳身体',
      '学习身体知识',
      '模仿治疗',
      '观看科普动画',
      '给同学针灸'
    ],
    explanation: '安全行为是学习和求助；不安全行为是自行针刺、模仿治疗或给别人操作。',
    icon: rewardSafetyBell,
    rewards: [
      { id: 'safety-bell', name: '安全铃铛', count: 2, icon: rewardSafetyBell },
      { id: 'mugwort-floss', name: '艾绒', count: 1, icon: materialMugwort }
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

export default {
  safetyQuestions,
  safetyPledge
}

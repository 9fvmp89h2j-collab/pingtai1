import fengmian1 from '@/assets/courses/fengmian1.png'
import fengmian2 from '@/assets/courses/fengmian2.png'
import fengmian3 from '@/assets/courses/fengmian3.png'
import fengmian4 from '@/assets/courses/fengmian4.png'
import { generatedMaterialIcons, generatedRewardAssets } from './generatedRewardAssets'

const materialBambooSlip = generatedMaterialIcons['bamboo-slip-shard']

export const stories = [
  {
    id: 'exam-copper-human',
    name: '会考试的小铜人',
    description: '古人制作针灸铜人，帮助学习者认识经络和穴位，也用于考核穴位知识。',
    status: 'available',
    category: '铜人文化',
    estimatedMinutes: 5,
    icon: fengmian1,
    cover: fengmian1,
    route: '/doctor-story',
    rewards: [
      { id: 'bamboo-slip-shard', name: '竹简碎片', count: 3, icon: materialBambooSlip },
      { id: 'apricot-kernel', name: '杏林叶', count: 2, icon: generatedMaterialIcons['apricot-kernel'] }
    ],
    quiz: {
      question: '古代针灸铜人主要用来做什么？',
      options: ['玩游戏', '学习和考核穴位', '做饭', '种草药'],
      answer: 1,
      explanation: '针灸铜人能帮助学习者认识穴位，也能用于考核。'
    }
  },
  {
    id: 'meridian-starry-road',
    name: '星星连成的经络路',
    description: '穴位不是孤零零的点，它们像星星一样连成路线，这些路线就像经络星河。',
    status: 'available',
    category: '经络启蒙',
    estimatedMinutes: 4,
    icon: fengmian2,
    cover: fengmian2,
    route: '/jingluo',
    rewards: [
      { id: 'meridian-star-sand', name: '经络星砂', count: 3, icon: generatedMaterialIcons['meridian-star-sand'] }
    ],
    quiz: {
      question: '经络星河想告诉我们什么？',
      options: ['穴位可以连成路线', '星星只能在天上', '经络是玩具', '可以自己针刺'],
      answer: 0,
      explanation: '经络学习是帮助我们认识穴位路线，本系统不提供实际针刺指导。'
    }
  },
  {
    id: 'herbal-safety-bell',
    name: '安全铃铛响叮当',
    description: '安全铃铛提醒小药师：学习穴位可以，但不能自己拿针尝试。',
    status: 'locked',
    category: '安全课堂',
    estimatedMinutes: 4,
    icon: fengmian3,
    cover: fengmian3,
    route: '/quiz-game',
    rewards: [
      { id: 'safety-bell', name: '安全铃铛', count: 2, icon: generatedMaterialIcons['safety-bell'] }
    ],
    quiz: {
      question: '身体不舒服时应该怎么做？',
      options: ['自己扎针', '告诉家长并咨询医生', '模仿视频治疗', '给同学针灸'],
      answer: 1,
      explanation: '身体不舒服要告诉家长和医生，不能自行针刺。'
    }
  },
  {
    id: 'repair-agency-archive',
    name: '修复侦探社档案室',
    description: '小侦探把收集到的材料带回侦探社，整理故事墙、安全标识和经络星幕。',
    status: 'locked',
    category: '侦探社建设',
    estimatedMinutes: 5,
    icon: fengmian4,
    cover: fengmian4,
    route: '/agency',
    rewards: [
      { id: 'agency-progress', name: '侦探社档案进度', count: 10, icon: generatedRewardAssets.detectiveAgencyBadge }
    ],
    quiz: {
      question: '材料袋里的材料主要可以用来做什么？',
      options: ['整理侦探社档案', '替代医生看病', '自己治疗', '藏起来不用'],
      answer: 0,
      explanation: '材料用于游戏化修复和学习展示，不用于实际治疗。'
    }
  }
]

export default stories

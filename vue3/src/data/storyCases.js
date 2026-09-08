import detectiveGuide from '@/assets/characters/copper-detective-guide-512.png'
import missingBambooScene from '@/assets/maps/xinglin-adventure-map-bg.png'
import archiveBadgeIcon from '@/assets/badge/1.png'
import emptyShelfIcon from '@/assets/materials/material-bamboo-slip.png'
import copperPaperweightIcon from '@/assets/materials/material-copper-model.png'
import bambooLeafIcon from '@/assets/materials/material-mugwort.png'
import hiddenScrollIcon from '@/assets/materials/material-badge.png'
import { generatedMaterialIcons, generatedRewardAssets } from './generatedRewardAssets'

const bambooSlipIcon = generatedMaterialIcons['bamboo-slip-shard']

export const storyCases = [
  {
    id: 'missing-bamboo',
    number: '一',
    caseCode: '01',
    title: '失踪竹简案',
    subtitle: '医馆里少了一卷竹简，线索藏在每一个小细节里。',
    cover: bambooSlipIcon,
    sceneImage: missingBambooScene,
    guideImage: detectiveGuide,
    available: true,
    pages: [
      {
        id: 'clinic-morning',
        title: '医馆里少了一卷竹简',
        image: '/doctor-story-preview/1.jpg',
        text: '清晨，小铜人老师整理医馆时，发现存放古代医籍的竹简架空了一格。桌上留着一枚圆圆的压书铜片，窗边还躺着一片新落下的竹叶。',
        tip: '先观察现场，再把看到的细节记进线索册。'
      },
      {
        id: 'bamboo-lesson',
        title: '竹简上的古老智慧',
        image: '/doctor-story-preview/2.jpg',
        text: '竹简记录着古人观察身体、认识经络和穴位的文化知识。我们可以从故事中了解历史，但不需要也不能自己尝试针刺。',
        tip: '学习文化知识时，安全边界也要一起记住。'
      },
      {
        id: 'hidden-scroll',
        title: '线索指向书架后方',
        image: '/doctor-story-preview/3.jpg',
        text: '顺着竹叶和压书铜片留下的方向，小侦探在书架后发现了金色卷轴。原来竹简被一阵小风吹落，藏在了卷轴后面。',
        tip: '把线索拼在一起，你就能说出完整的故事。'
      }
    ],
    clues: [
      {
        id: 'empty-shelf',
        name: '空着的竹简架',
        description: '架上少了一格，说明竹简曾经放在这里。',
        pageId: 'clinic-morning',
        icon: emptyShelfIcon,
        hotspot: { left: 16, top: 18 }
      },
      {
        id: 'copper-paperweight',
        name: '圆圆的压书铜片',
        description: '桌边的铜片可能是竹简被风吹动时留下的线索。',
        pageId: 'clinic-morning',
        icon: copperPaperweightIcon,
        hotspot: { left: 66, top: 76 }
      },
      {
        id: 'bamboo-leaf',
        name: '窗边的竹叶',
        description: '新落下的竹叶提示我们：窗边刚刚有风吹过。',
        pageId: 'bamboo-lesson',
        icon: bambooLeafIcon,
        hotspot: { left: 73, top: 35 }
      },
      {
        id: 'hidden-scroll',
        name: '书架后的金色卷轴',
        description: '卷轴后藏着失踪的竹简，故事终于有了答案。',
        pageId: 'hidden-scroll',
        icon: hiddenScrollIcon,
        hotspot: { left: 91, top: 53 }
      }
    ],
    reasoning: {
      title: '你认为竹简去了哪里？',
      prompt: '把线索放在一起，最合理的解释是哪一个？',
      options: [
        {
          id: 'wind',
          title: '小风把竹简吹到了书架后',
          detail: '竹叶、空架和书架后的卷轴刚好能连成一条线。',
          correct: true
        },
        {
          id: 'theft',
          title: '有人偷偷拿走了竹简',
          detail: '现场没有发现脚印或打开的门锁，线索并不支持这个猜想。',
          correct: false
        }
      ],
      explanation: '答对啦！从竹叶、空架到书架后的卷轴，线索说明竹简是被风吹落后藏起来的。'
    },
    safety: {
      title: '安全小判断',
      prompt: '看到针灸文化故事或身体知识时，我们应该怎么做？',
      options: [
        {
          id: 'learn-safely',
          title: '只学习文化知识，身体不舒服要告诉家长或医生',
          detail: '故事馆用于科普学习，不提供自行针刺或治疗指导。',
          correct: true
        },
        {
          id: 'try-it',
          title: '照着故事或视频，自己拿尖东西试一试',
          detail: '尖锐物品和针刺操作不能由儿童自行尝试。',
          correct: false
        }
      ],
      explanation: '安全星点亮！我们只学习文化和身体认知，遇到不舒服要及时告诉大人。'
    },
    rewards: {
      dailyTaskId: 'daily-read-copper-story',
      mainTaskId: 'main-mist-in-xinglin',
      threeStars: [
        { id: 'story-archive-badge', name: '三星故事档案', count: 1, icon: archiveBadgeIcon }
      ]
    },
    unlocks: ['copper-origin']
  }
]

export const storyChapterCatalog = [
  {
    id: 'missing-bamboo',
    number: '一',
    title: '失踪竹简案',
    image: bambooSlipIcon
  },
  {
    id: 'copper-origin',
    number: '二',
    title: '铜人来历',
    image: generatedRewardAssets.copperDisplayStand
  },
  {
    id: 'meridian-secret',
    number: '三',
    title: '经络小秘密',
    image: generatedRewardAssets.meridianMapClue
  },
  {
    id: 'safety-story',
    number: '四',
    title: '安全小故事',
    image: generatedRewardAssets.detectiveSafety
  }
]

export default storyCases

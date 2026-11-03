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
    gameType: 'missing-bamboo',
    ageModes: {
      family: { id: 'family', label: '亲子启蒙', ages: '4–7岁', differenceCount: 3, puzzlePieces: 6, timed: false },
      challenge: { id: 'challenge', label: '自主挑战', ages: '8–12岁', differenceCount: 5, puzzlePieces: 9, timed: true }
    },
    spotDifference: {
      beforeImage: '/doctor-story-preview/1.jpg',
      afterImage: '/doctor-story-preview/2.jpg',
      assetNote: '临时占位图：正式素材替换为 spot-before.png / spot-after.png',
      differences: [
        { id: 'empty-shelf', name: '竹简架空了一格', level: 'family', hotspot: { left: 19, top: 31 } },
        { id: 'moved-copper', name: '铜片移动了位置', level: 'family', hotspot: { left: 58, top: 76 } },
        { id: 'fallen-leaf', name: '窗边多了一片竹叶', level: 'family', hotspot: { left: 83, top: 25 } },
        { id: 'curtain-direction', name: '帘角改变了方向', level: 'challenge', hotspot: { left: 73, top: 18 } },
        { id: 'scroll-edge', name: '书架后露出卷轴', level: 'challenge', hotspot: { left: 31, top: 57 } }
      ]
    },
    evidenceSequence: {
      main: [
        { id: 'bamboo-leaf', name: '窗边竹叶', slot: '起因', description: '竹叶提示窗边刚有风。' },
        { id: 'copper-paperweight', name: '移位铜片', slot: '经过', description: '铜片的位置发生了变化。' },
        { id: 'shelf-scroll-gap', name: '空架与卷轴缝隙', slot: '结果', description: '竹简可能落到了书架后。' }
      ],
      bonus: [
        { id: 'bamboo-cord', name: '竹简编绳', description: '竹片削平后，可以用绳编联成册。' },
        { id: 'copper-pattern', name: '铜片纹样', description: '铜片也是观察古代器物的文化线索。' }
      ],
      windFrames: [
        { id: 'still', label: '无风', image: '/doctor-story-preview/1.jpg', description: '竹简稳稳放在架上。' },
        { id: 'gust', label: '起风', image: '/doctor-story-preview/2.jpg', description: '帘角和竹叶被风吹动。' },
        { id: 'found', label: '风停', image: '/doctor-story-preview/3.jpg', description: '竹简落到了书架后。' }
      ],
      assetNote: '临时占位图：正式素材替换为 wind-01-still.png / wind-02-gust.png / wind-03-found.png'
    },
    progressiveReveal: {
      image: '/doctor-story-preview/3.jpg',
      regions: ['窗边', '桌面', '书架']
    },
    knowledgeCards: [
      { id: 'bamboo-making', title: '竹简是什么', text: '古人把竹片削平、整理好，再用绳子编在一起，用来记录知识。' },
      { id: 'medical-books', title: '古代医籍', text: '古代医籍保存了不同时期认识身体与生活的经验，不能直接当作今天的诊疗建议。' }
    ],
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
    unlocks: ['exam-copper-man']
  },
  {
    id: 'exam-copper-man',
    number: '二',
    caseCode: '02',
    title: '会考试的小铜人',
    subtitle: '跟着王惟一走进北宋课堂，把铜人和穴位星送回正确位置。',
    cover: generatedRewardAssets.copperDisplayStand,
    sceneImage: '/doctor-story-preview/5.jpg',
    guideImage: detectiveGuide,
    available: true,
    gameType: 'exam-copper-man',
    ageModes: {
      family: { id: 'family', label: '亲子启蒙', ages: '4–7岁', differenceCount: 3, puzzlePieces: 6, timed: false },
      challenge: { id: 'challenge', label: '自主挑战', ages: '8–12岁', differenceCount: 5, puzzlePieces: 9, timed: true }
    },
    pages: [
      {
        id: 'workshop',
        title: '王惟一整理图经',
        image: '/doctor-story-preview/5.jpg',
        text: '北宋时，医家王惟一整理针灸知识，并参与设计铜人教学模型，帮助学习者观察穴位名称与身体区域。',
        tip: '铜人是历史上的教学模型，本游戏只学习文化知识。'
      },
      {
        id: 'song-class',
        title: '铜人成为课堂教具',
        image: '/doctor-story-preview/6.jpg',
        text: '铜人身上标有经络与穴位信息，古代学习者可以借它检查自己是否记住名称和位置。',
        tip: '不同史料和标准的穴位统计口径可能不同，要看清来源。'
      },
      {
        id: 'digital-class',
        title: '来到今天的数字课堂',
        image: '/doctor-story-preview/7.jpg',
        text: '今天，我们把铜人变成屏幕里的身体文化地图，只做拼图、观察和名称匹配。',
        tip: '不在自己或他人身上模仿按压、针刺或艾灸。'
      }
    ],
    puzzle: {
      image: generatedRewardAssets.copperDisplayStand,
      assetNote: '临时占位图：正式素材替换为 copper-man-full.png',
      acupoints: [
        { id: 'LU-1', name: '中府', region: '胸部区域', color: '#d99a35', target: { left: 43, top: 29 } },
        { id: 'LU-5', name: '尺泽', region: '肘部区域', color: '#4e8d73', target: { left: 32, top: 47 } },
        { id: 'LU-9', name: '太渊', region: '腕部区域', color: '#b85548', target: { left: 24, top: 58 } }
      ]
    },
    knowledgeCards: [
      { id: 'wang-weiyi', title: '王惟一与铜人', text: '北宋医家王惟一主持整理《铜人腧穴针灸图经》，并参与设计铜人教学模型。' },
      { id: 'lung-route', title: '手太阴肺经', text: '在中医理论里，经络可以看作身体文化地图的路线，穴位像路线上的小站点。' }
    ],
    clues: [
      { id: 'copper-puzzle', name: '完整铜人', description: '拼合铜人，观察身体区域。', icon: generatedRewardAssets.copperDisplayStand, hotspot: { left: 50, top: 48 } },
      { id: 'three-points', name: '三颗穴位星', description: '认识中府、尺泽和太渊的大致区域。', icon: bambooSlipIcon, hotspot: { left: 64, top: 38 } }
    ],
    reasoning: {
      title: '古人为什么制作针灸铜人？',
      prompt: '根据故事，选择最合理的文化用途。',
      options: [
        { id: 'teaching', title: '帮助观察、学习和考核穴位知识', detail: '铜人是一种直观的教学模型。', correct: true },
        { id: 'self-treatment', title: '让铜人替大家看病', detail: '铜人不会诊断，也不能替代医生。', correct: false }
      ],
      explanation: '答对啦！铜人帮助古代学习者观察和学习穴位知识。'
    },
    safety: {
      title: '安全小判断',
      prompt: '完成穴位星归位后，我们还可以做什么？',
      options: [
        { id: 'observe', title: '继续在屏幕和模型上观察', detail: '只做文化知识和身体区域认知。', correct: true },
        { id: 'imitate', title: '拿尖锐物品在身上试一试', detail: '儿童不能模仿针刺或其他治疗操作。', correct: false }
      ],
      explanation: '安全星点亮！我们只观察模型，不在身体上模仿操作。'
    },
    rewards: {
      dailyTaskId: 'daily-read-copper-story',
      mainTaskId: null,
      threeStars: [
        { id: 'copper-token', name: '铜片', count: 2, icon: generatedMaterialIcons['copper-token'] }
      ]
    },
    unlocks: ['meridian-secret']
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
    id: 'exam-copper-man',
    number: '二',
    title: '会考试的小铜人',
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

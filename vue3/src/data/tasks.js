import { generatedMaterialIcons, generatedRewardAssets } from './generatedRewardAssets'

const rewardAcupointStarPearl = generatedMaterialIcons['acupoint-star-pearl']
const rewardCopperToken = generatedMaterialIcons['copper-token']
const rewardSafetyBell = generatedMaterialIcons['safety-bell']
const materialBambooSlip = generatedMaterialIcons['bamboo-slip-shard']
const materialXinglinLeaf = generatedMaterialIcons['apricot-kernel']

export const dailyTasks = [
  {
    id: 'daily-read-copper-story',
    name: '阅读一个针灸小故事',
    description: '去故事馆完成《失踪竹简案》，回答故事后的安全小问题。',
    type: 'story',
    status: 'todo',
    progress: 0,
    total: 1,
    route: '/doctor-story',
    icon: materialBambooSlip,
    rewards: [
      { id: 'bamboo-slip-shard', name: '竹简碎片', count: 3, icon: materialBambooSlip },
      { id: 'apricot-kernel', name: '杏林叶', count: 2, icon: materialXinglinLeaf }
    ]
  },
  {
    id: 'daily-light-hand-stars',
    name: '点亮 3 个穴位星点',
    description: '跟着小铜人老师认识身体地图，找到合谷等 3 颗穴位星点。',
    type: 'acupoint',
    status: 'active',
    progress: 1,
    total: 3,
    route: '/body-map',
    icon: rewardAcupointStarPearl,
    rewards: [
      { id: 'acupoint-star-pearl', name: '穴位星珠', count: 3, icon: rewardAcupointStarPearl },
      { id: 'copper-token', name: '铜片', count: 1, icon: rewardCopperToken }
    ]
  },
  {
    id: 'daily-safety-quiz',
    name: '完成 3 道安全判断题',
    description: '听安全铃铛提醒，判断哪些行为可以做，哪些行为不能做。',
    type: 'safety',
    status: 'todo',
    progress: 0,
    total: 3,
    route: '/safety',
    icon: rewardSafetyBell,
    rewards: [
      { id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell },
      { id: 'mugwort-floss', name: '艾绒', count: 1, icon: generatedMaterialIcons['mugwort-floss'] }
    ]
  }
]

export const mainTasks = [
  {
    id: 'main-safety-case',
    name: '完成安全守护案',
    description: '完成安全学习与安全宣誓，记住只观察、只学习、不自行针刺。',
    status: 'active',
    unlocks: ['safe-start'],
    route: '/safety',
    icon: rewardSafetyBell,
    rewards: []
  },
  {
    id: 'main-mist-in-xinglin',
    name: '杏林谷起雾',
    description: '完成故事馆任务，找回第一束杏林谷星光。',
    status: 'active',
    unlocks: ['body'],
    route: '/doctor-story',
    icon: materialBambooSlip,
    rewards: [
      { id: 'bamboo-slip-shard', name: '竹简碎片', count: 3, icon: materialBambooSlip }
    ]
  },
  {
    id: 'main-hand-star-map',
    name: '点亮身体地图',
    description: '认识身体区域，找到穴位星点，并完成一次安全问答。',
    status: 'locked',
    total: 10,
    unlocks: ['meridian'],
    route: '/body-map',
    icon: rewardAcupointStarPearl,
    rewards: [
      { id: 'acupoint-star-pearl', name: '穴位星珠', count: 2, icon: rewardAcupointStarPearl },
      { id: 'copper-token', name: '铜片', count: 1, icon: rewardCopperToken },
      { id: 'safety-bell', name: '安全铃铛', count: 1, icon: rewardSafetyBell }
    ]
  },
  {
    id: 'main-repair-agency',
    name: '整理侦探社线索墙',
    description: '收集竹简碎片和经络星砂，把新的线索贴到侦探社档案墙。',
    status: 'locked',
    unlocks: ['agency'],
    route: '/agency',
    icon: generatedRewardAssets.starClueWall,
    rewards: []
  }
]

export default {
  dailyTasks,
  mainTasks
}

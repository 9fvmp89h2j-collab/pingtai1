import {
  generatedAgencyDecorations,
  generatedMaterialIcons,
  generatedRewardAssets
} from './generatedRewardAssets'

const decorationById = generatedAgencyDecorations.reduce((result, item) => {
  result[item.id] = item
  return result
}, {})

function cost(id, name, count) {
  return { id, name, count, icon: generatedMaterialIcons[id] }
}

export const agencyRepairRecipes = [
  {
    id: 'gate',
    title: '修复侦探社门牌',
    displayName: '侦探社门牌',
    shortTitle: '门牌',
    description: '先挂好侦探社徽章，让所有小侦探找到集合地点。',
    waitLabel: '待修复',
    doneLabel: '已修复',
    position: { x: 44, y: 34 },
    prerequisites: [],
    costs: [
      cost('bamboo-slip-shard', '竹简碎片', 3),
      cost('copper-token', '铜片', 1)
    ],
    result: '解锁侦探社长期玩法'
  },
  {
    id: 'herb-cabinet',
    title: '整理侦探社书架',
    displayName: '草药书架',
    shortTitle: '书架',
    description: '把草药观察记录和故事档案分层收好。',
    waitLabel: '待整理',
    doneLabel: '已整理',
    position: { x: 73, y: 52 },
    prerequisites: ['gate'],
    costs: [cost('herbal-leaf', '草药叶', 5)],
    result: '场景增加草药书架'
  },
  {
    id: 'star-wall',
    title: '点亮星光线索墙',
    displayName: '星光线索墙',
    shortTitle: '线索墙',
    description: '把经络星砂放回墙面，让探索路线重新发光。',
    waitLabel: '待点亮',
    doneLabel: '已点亮',
    position: { x: 27, y: 56 },
    prerequisites: ['gate'],
    costs: [cost('meridian-star-sand', '经络星砂', 4)],
    result: '场景点亮星光路线'
  },
  {
    id: 'bell-wall',
    title: '点亮安全铃铛墙',
    displayName: '安全铃铛墙',
    shortTitle: '安全铃铛墙',
    description: '把安全规则留在最醒目的位置。',
    waitLabel: '待点亮',
    doneLabel: '已点亮',
    position: { x: 25, y: 81 },
    prerequisites: ['gate'],
    costs: [cost('safety-bell', '安全铃铛', 2)],
    result: '场景增加安全提醒装置'
  },
  {
    id: 'display',
    title: '升级小铜人展示台',
    displayName: '小铜人展示台',
    shortTitle: '展示台',
    description: '把观察到的穴位文化线索放进展示台。',
    waitLabel: '待升级',
    doneLabel: '已升级',
    position: { x: 48, y: 74 },
    prerequisites: ['star-wall'],
    costs: [
      cost('acupoint-star-pearl', '穴位星珠', 2),
      cost('copper-token', '铜片', 2)
    ],
    result: '解锁展示台升级效果'
  },
  {
    id: 'archive',
    title: '整理故事档案角',
    displayName: '故事档案角',
    shortTitle: '档案角',
    description: '把故事档案卡和经络地图线索收入收藏册。',
    waitLabel: '待整理',
    doneLabel: '已整理',
    position: { x: 74, y: 79 },
    prerequisites: ['herb-cabinet'],
    costs: [
      cost('bamboo-slip-shard', '竹简碎片', 3),
      cost('star-compass', '经络地图线索', 1)
    ],
    result: '解锁故事收藏区域'
  },
  {
    id: 'roof',
    title: '点亮星光屋顶',
    displayName: '星光屋顶',
    shortTitle: '星光屋顶',
    description: '完成最后修复，让整个侦探社在地图上亮起来。',
    waitLabel: '最终修复',
    doneLabel: '已点亮',
    position: { x: 54, y: 19 },
    completesMainline: true,
    prerequisites: ['display', 'bell-wall', 'archive'],
    costs: [
      cost('bamboo-slip-shard', '竹简碎片', 5),
      cost('meridian-star-sand', '经络星砂', 4)
    ],
    result: '全部结案，开启星光宝箱'
  }
].map((recipe) => ({
  ...recipe,
  icon: decorationById[recipe.id]?.image,
  decorationName: decorationById[recipe.id]?.name || recipe.shortTitle
}))

export const agencyRepairMilestones = [
  {
    count: 1,
    name: '第一张线索贴纸',
    detail: '完成第一次修复后点亮',
    icon: generatedRewardAssets.clueStickerPack
  },
  {
    count: 3,
    name: '安全观察徽章',
    detail: '完成 3 项修复后点亮',
    icon: generatedRewardAssets.safetyObservationBadge
  },
  {
    count: 5,
    name: '侦探社徽章',
    detail: '完成 5 项修复后点亮',
    icon: generatedRewardAssets.detectiveAgencyBadge
  },
  {
    count: 7,
    name: '侦探社星光宝箱',
    detail: '全部修复后开启',
    icon: generatedRewardAssets.rewardChestOpen
  }
]

export function unmetAgencyPrerequisites(recipe, repairedIds = []) {
  return (recipe?.prerequisites || []).filter((id) => !repairedIds.includes(id))
}

export default agencyRepairRecipes

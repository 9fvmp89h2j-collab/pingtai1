import { generatedMaterialIcons } from './generatedRewardAssets'

export const materials = [
  {
    id: 'bamboo-slip-shard',
    name: '竹简碎片',
    description: '故事任务里找到的竹简线索，用来修复门牌、档案角和星光屋顶。',
    status: 'owned',
    count: 6,
    rarity: 'common',
    source: ['故事任务', '故事问答'],
    usage: ['修复侦探社门牌', '故事档案角', '星光屋顶'],
    rewards: [],
    icon: generatedMaterialIcons['bamboo-slip-shard']
  },
  {
    id: 'apricot-kernel',
    name: '杏林叶',
    description: '文化任务里收集的杏林叶，用来装饰杏林观察区域。',
    status: 'owned',
    count: 4,
    rarity: 'common',
    source: ['文化任务', '故事馆章节'],
    usage: ['杏林观察装饰', '每周奖励收藏'],
    rewards: [],
    icon: generatedMaterialIcons['apricot-kernel']
  },
  {
    id: 'story-archive-badge',
    kind: 'collectible',
    name: '三星故事档案',
    description: '完成故事案件三星归档后获得的故事徽章。',
    status: 'locked',
    count: 0,
    rarity: 'rare',
    source: ['故事馆三星案件'],
    usage: ['故事收藏成就', '解锁高级外观'],
    rewards: [],
    icon: generatedMaterialIcons['story-archive-badge']
  },
  {
    id: 'herbal-leaf',
    name: '草药叶',
    description: '身体地图任务里长出的草药叶，用来整理侦探社草药书架。',
    status: 'owned',
    count: 8,
    rarity: 'common',
    source: ['身体地图任务', '每日任务'],
    usage: ['修复草药书架'],
    rewards: [],
    icon: generatedMaterialIcons['herbal-leaf']
  },
  {
    id: 'meridian-star-sand',
    name: '经络星砂',
    description: '经络路线任务里收集的星砂，用来点亮线索墙和星光屋顶。',
    status: 'owned',
    count: 5,
    rarity: 'rare',
    source: ['经络连线', '点亮经络星路'],
    usage: ['星光线索墙', '星光屋顶'],
    rewards: [],
    icon: generatedMaterialIcons['meridian-star-sand']
  },
  {
    id: 'acupoint-star-pearl',
    name: '穴位星珠',
    description: '找对穴位后出现的小光球，是解锁穴位卡的重要奖励。',
    status: 'owned',
    count: 3,
    rarity: 'rare',
    source: ['穴位识别', '小铜人闯关'],
    usage: ['升级小铜人展示台'],
    rewards: [],
    icon: generatedMaterialIcons['acupoint-star-pearl']
  },
  {
    id: 'copper-token',
    name: '铜片',
    description: '小铜人馆任务奖励的青铜小片，可用于升级小铜人站台。',
    status: 'owned',
    count: 2,
    rarity: 'rare',
    source: ['铜人点穴', '经络任务'],
    usage: ['侦探社门牌', '小铜人展示台'],
    rewards: [],
    icon: generatedMaterialIcons['copper-token']
  },
  {
    id: 'mugwort-floss',
    name: '艾绒',
    description: '安全课堂里获得的金色小团，用来兑换侦探社安全提醒。',
    status: 'owned',
    count: 3,
    rarity: 'common',
    source: ['安全任务', '艾草知识'],
    usage: ['安全主题装饰'],
    rewards: [],
    icon: generatedMaterialIcons['mugwort-floss']
  },
  {
    id: 'safety-bell',
    name: '安全铃铛',
    description: '完成安全问答后获得的小铃铛，提醒大家不能自行针刺。',
    status: 'owned',
    count: 1,
    rarity: 'rare',
    source: ['安全问答', '安全宣誓'],
    usage: ['安全铃铛墙'],
    rewards: [],
    icon: generatedMaterialIcons['safety-bell']
  },
  {
    id: 'gold-needle-badge-shard',
    kind: 'collectible',
    name: '金针徽章碎片',
    description: '连续答对题目后出现的稀有碎片，可合成高级徽章。',
    status: 'locked',
    count: 0,
    rarity: 'epic',
    source: ['连续答对 5 题'],
    usage: ['合成高级徽章'],
    rewards: [],
    icon: generatedMaterialIcons['gold-needle-badge-shard']
  },
  {
    id: 'copper-core',
    kind: 'collectible',
    name: '铜人核心',
    description: '完成整条经络挑战后获得的核心材料，可解锁铜人高级外观。',
    status: 'locked',
    count: 0,
    rarity: 'epic',
    source: ['整条经络挑战'],
    usage: ['解锁铜人高级外观'],
    rewards: [],
    icon: generatedMaterialIcons['copper-core']
  },
  {
    id: 'star-compass',
    kind: 'consumable',
    name: '经络地图线索',
    description: '星光修补册重新点亮星星后获得的小碎片，收集后可修复星图路线。',
    status: 'locked',
    count: 0,
    rarity: 'legendary',
    source: ['星光修补册'],
    usage: ['故事档案角', '地图提示'],
    rewards: [],
    icon: generatedMaterialIcons['star-compass']
  }
]

export default materials

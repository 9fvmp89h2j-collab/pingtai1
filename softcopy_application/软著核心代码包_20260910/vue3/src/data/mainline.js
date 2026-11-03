// The only frontend definition of the eight mainline levels.
// Practice routes may point at a level, but they never create another level.
import { generatedMaterialIcons, generatedRewardAssets } from './generatedRewardAssets'

const reward = (itemCode, name, amount, icon = generatedMaterialIcons[itemCode]) => ({ itemCode, name, amount, icon })

export const mainlineLevels = [
  {
    id: 'checkin', order: 1, label: '报到处', route: '/checkin', mapKey: 'checkin',
    icon: '1', seal: '今', x: 13.6, y: 9.5, statusText: '入社报到',
    description: '完成侦探社报到，领取第一枚观察徽章。',
    rewards: [reward('story-archive-badge', '观察徽章', 1, generatedRewardAssets.clueStickerPack), reward('bamboo-slip-shard', '竹简碎片', 1)],
    requiredTaskIds: ['map.checkin'], target: 1, public: true
  },
  {
    id: 'safe-start', order: 2, label: '安全守护案', route: '/safety', mapKey: 'safe-start',
    icon: '2', seal: '锁', x: 33.2, y: 11.8, statusText: '安全课堂',
    description: '先学会只观察、不针刺的安全规则，再开始后续调查。',
    rewards: [reward('safety-bell', '安全铃铛', 2), reward('mugwort-floss', '艾绒', 2)],
    requiredTaskIds: ['main-safety-case'], target: 1
  },
  {
    id: 'bamboo', order: 3, label: '失踪竹简案', route: '/doctor-story', mapKey: 'bamboo',
    icon: '3', seal: '锁', x: 39.0, y: 35.0, statusText: '故事馆任务',
    description: '阅读故事、搜集证据、完成推理和安全判断。',
    rewards: [reward('bamboo-slip-shard', '竹简碎片', 3), reward('apricot-kernel', '杏林叶', 2)],
    requiredTaskIds: ['main-mist-in-xinglin'], target: 1
  },
  {
    id: 'body', order: 4, label: '身体地图追踪案', route: '/body-map', mapKey: 'body',
    icon: '4', seal: '锁', x: 8.8, y: 40.0, statusText: '身体地图',
    description: '完成区域学习和穴位归位，只做观察与文化知识学习。',
    rewards: [reward('acupoint-star-pearl', '穴位星珠', 2), reward('copper-token', '铜片', 1), reward('safety-bell', '安全铃铛', 1)],
    requiredTaskIds: ['main-hand-star-map'], target: 10
  },
  {
    id: 'meridian', order: 5, label: '经络星河密令', route: '/jingluo', mapKey: 'meridian',
    icon: '5', seal: '锁', x: 29.0, y: 60.0, statusText: '经络学习',
    description: '完成 14 条经络路线并领取星河聚合奖励。',
    rewards: [reward('meridian-star-sand', '经络星砂', 5), reward('copper-token', '铜片', 2)],
    requiredTaskIds: ['meridian-river-completion'], target: 14
  },
  {
    id: 'archive', order: 6, label: '铜人档案室', route: '/copper-man', mapKey: 'archive',
    icon: '6', seal: '锁', x: 57.5, y: 47.0, statusText: '小铜人馆',
    description: '完成首次铜人观察案件，后续案件作为每日重复内容。',
    rewards: [reward('copper-token', '铜片', 2), reward('meridian-star-sand', '经络星砂', 5)],
    requiredTaskIds: ['copper-man-daily-case'], target: 1
  },
  {
    id: 'secret-room', order: 7, label: '星光修补册', route: '/review', mapKey: 'secret-room',
    icon: '7', seal: '锁', x: 59.5, y: 70.0, statusText: '复习修补',
    description: '完成首次知识星点修补，后续错题作为复习任务。',
    rewards: [reward('star-compass', '经络地图线索', 1)],
    requiredTaskIds: ['review-daily'], target: 1
  },
  {
    id: 'agency', order: 8, label: '侦探社修复计划', route: '/agency', mapKey: 'agency',
    icon: '8', seal: '锁', x: 14.8, y: 74.0, statusText: '侦探社修复',
    description: '修复第一项门牌即完成主线，其余项目用于持续收集。',
    rewards: [reward('story-archive-badge', '侦探社徽章', 1, generatedRewardAssets.detectiveAgencyBadge), reward('star-compass', '线索贴纸', 1, generatedRewardAssets.clueStickerPack)],
    requiredTaskIds: ['main-repair-agency'], target: 1
  }
]

const aliases = [
  { paths: ['/quiz-game'], levelId: 'safe-start' },
  { paths: ['/meridian-match', '/acupoint-sort', '/shunting-game'], levelId: 'meridian' },
  { paths: ['/copper-man'], levelId: 'archive' },
  { paths: ['/review'], levelId: 'secret-room' },
  { paths: ['/agency'], levelId: 'agency' }
]

export function mainlineLevelById(id) {
  return mainlineLevels.find((level) => level.id === id) || null
}

export function mainlineLevelForPath(path) {
  const normalized = String(path || '').split('?')[0].replace(/\/$/, '') || '/'
  const direct = mainlineLevels.find((level) => normalized === level.route)
  if (direct) return direct
  const alias = aliases.find((item) => item.paths.some((itemPath) => normalized === itemPath || normalized.startsWith(`${itemPath}/`)))
  return alias ? mainlineLevelById(alias.levelId) : null
}

export default mainlineLevels

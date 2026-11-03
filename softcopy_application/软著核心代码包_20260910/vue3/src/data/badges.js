import { generatedMaterialIcons, generatedRewardAssets } from './generatedRewardAssets'

export const badges = [
  {
    id: 'copper-apprentice',
    name: '铜人小学徒',
    description: '点亮 10 个穴位星点，成为小铜人老师的学习伙伴。',
    status: 'available',
    progress: 0,
    total: 10,
    condition: '点亮 10 个穴位星点',
    tier: 'bronze',
    icon: generatedRewardAssets.copperDisplayStand,
    rewards: [{ id: 'copper-token', name: '铜片', count: 2, icon: generatedMaterialIcons['copper-token'] }]
  },
  {
    id: 'acupoint-detective',
    name: '穴位小侦探',
    description: '连续找对 5 个穴位，获得星光侦探称号。',
    status: 'locked',
    progress: 0,
    total: 5,
    condition: '连续找对 5 个穴位',
    tier: 'silver',
    icon: generatedMaterialIcons['acupoint-star-pearl'],
    rewards: [{ id: 'acupoint-star-pearl', name: '穴位星珠', count: 3, icon: generatedMaterialIcons['acupoint-star-pearl'] }]
  },
  {
    id: 'meridian-adventurer',
    name: '经络探险家',
    description: '完成 3 条经络路线，把经络星河重新连起来。',
    status: 'locked',
    progress: 0,
    total: 3,
    condition: '完成 3 条经络路线',
    tier: 'gold',
    icon: generatedMaterialIcons['meridian-star-sand'],
    rewards: [{ id: 'meridian-star-sand', name: '经络星砂', count: 5, icon: generatedMaterialIcons['meridian-star-sand'] }]
  },
  {
    id: 'story-collector',
    name: '故事收藏家',
    description: '解锁 4 个针灸文化故事，点亮故事收藏墙。',
    status: 'locked',
    progress: 0,
    total: 4,
    condition: '解锁 4 个针灸文化故事',
    tier: 'silver',
    icon: generatedMaterialIcons['story-archive-badge'],
    rewards: [{ id: 'bamboo-slip-shard', name: '竹简碎片', count: 5, icon: generatedMaterialIcons['bamboo-slip-shard'] }]
  },
  {
    id: 'safe-little-healer',
    name: '安全小医者',
    description: '安全问答满分，记住学习边界，不自行针刺，不替代医生。',
    status: 'available',
    progress: 0,
    total: 1,
    condition: '安全问答满分',
    tier: 'gold',
    icon: generatedRewardAssets.safetyObservationBadge,
    rewards: [{ id: 'safety-bell', name: '安全铃铛', count: 5, icon: generatedMaterialIcons['safety-bell'] }]
  },
  {
    id: 'starlight-repairer',
    name: '星光修补师',
    description: '完成星光修补册复习，让暗下去的星点重新发光。',
    status: 'locked',
    progress: 0,
    total: 1,
    condition: '完成所有错题复习',
    tier: 'gold',
    icon: generatedRewardAssets.meridianMapClue,
    rewards: [{ id: 'star-compass', name: '经络地图线索', count: 1, icon: generatedMaterialIcons['star-compass'] }]
  },
  {
    id: 'xinglin-little-herbalist',
    name: '杏林小药师',
    description: '完成全部基础任务，修复杏林谷的第一轮星光。',
    status: 'locked',
    progress: 0,
    total: 6,
    condition: '完成全部基础任务',
    tier: 'platinum',
    icon: generatedMaterialIcons['apricot-kernel'],
    rewards: [{ id: 'apricot-kernel', name: '杏林叶', count: 1, icon: generatedMaterialIcons['apricot-kernel'] }]
  },
  {
    id: 'xinglin-heritage-messenger',
    name: '杏林传承小使者',
    description: '完成最终综合挑战，获得杏林谷最高认证。',
    status: 'locked',
    progress: 0,
    total: 1,
    condition: '完成最终综合挑战',
    tier: 'legendary',
    icon: generatedRewardAssets.detectiveAgencyBadge,
    rewards: [{ id: 'great-healer-heart', name: '大医之心', count: 1, icon: generatedRewardAssets.detectiveAgencyBadge }]
  }
]

export default badges

import rewardStarBurst from '@/assets/素材/1.星光奖励粒子.png'
import clueDiscoveryBurst from '@/assets/素材/2. 发现线索效果.png'
import clueStickerPack from '@/assets/素材/3. 线索贴纸组.png'
import rewardChoiceCard from '@/assets/素材/4. 三选一奖励卡.png'
import rewardChestClosed from '@/assets/素材/5. 奖励宝箱关闭状态.png'
import rewardChestOpen from '@/assets/素材/6. 奖励宝箱打开状态.png'
import copperToken from '@/assets/素材/7. 铜片.png'
import bambooSlipShard from '@/assets/素材/8. 竹简碎片.png'
import acupointStarPearl from '@/assets/素材/9. 穴位星珠.png'
import meridianStarSand from '@/assets/素材/10. 经络星砂.png'
import safetyBell from '@/assets/素材/11. 安全铃铛.png'
import xinglinLeaf from '@/assets/素材/12. 杏林叶.png'
import starClueWall from '@/assets/素材/13. 星光线索墙.png'
import archiveBookshelf from '@/assets/素材/14. 侦探社书架.png'
import roofStar from '@/assets/素材/15. 星光屋顶装饰.png'
import safetyBellWall from '@/assets/素材/16. 安全铃铛墙.png'
import copperDisplayStand from '@/assets/素材/17. 铜人展示台.png'
import storyArchiveCard from '@/assets/素材/18. 故事档案卡.png'
import bodyMapPuzzle from '@/assets/素材/19. 身体地图拼图.png'
import safetyObservationBadge from '@/assets/素材/20. 安全观察徽章.png'
import detectiveAgencyBadge from '@/assets/素材/21. 侦探社徽章.png'
import meridianMapClue from '@/assets/素材/22. 经络地图线索.png'
import detectiveDiscover from '@/assets/素材/23. 发现线索.png'
import detectiveReward from '@/assets/素材/24. 获得奖励.png'
import detectiveThink from '@/assets/素材/25. 思考线索.png'
import detectiveEncourage from '@/assets/素材/26. 鼓励孩子.png'
import detectiveComplete from '@/assets/素材/27. 任务完成.png'
import detectiveSafety from '@/assets/素材/28. 安全提醒.png'
import legacyMugwort from '@/assets/materials/material-mugwort.png'
import legacyBadgeShard from '@/assets/materials/material-badge.png'
import legacyCopperCore from '@/assets/materials/material-copper-model.png'
import legacyHerbalShelf from '@/assets/clinic/clinic-decor-herb-shelf.png'

export const generatedRewardAssets = {
  rewardStarBurst,
  clueDiscoveryBurst,
  clueStickerPack,
  rewardChoiceCard,
  rewardChestClosed,
  rewardChestOpen,
  copperToken,
  bambooSlipShard,
  acupointStarPearl,
  meridianStarSand,
  safetyBell,
  xinglinLeaf,
  starClueWall,
  archiveBookshelf,
  roofStar,
  safetyBellWall,
  copperDisplayStand,
  storyArchiveCard,
  bodyMapPuzzle,
  safetyObservationBadge,
  detectiveAgencyBadge,
  meridianMapClue,
  detectiveDiscover,
  detectiveReward,
  detectiveThink,
  detectiveEncourage,
  detectiveComplete,
  detectiveSafety
}

export const generatedMaterialIcons = {
  'bamboo-slip-shard': bambooSlipShard,
  'apricot-kernel': xinglinLeaf,
  'story-archive-badge': storyArchiveCard,
  'herbal-leaf': legacyHerbalShelf,
  'meridian-star-sand': meridianStarSand,
  'acupoint-star-pearl': acupointStarPearl,
  'copper-token': copperToken,
  'mugwort-floss': legacyMugwort,
  'safety-bell': safetyBell,
  'gold-needle-badge-shard': legacyBadgeShard,
  'copper-core': legacyCopperCore,
  'star-compass': meridianMapClue
}

export const generatedAgencyDecorations = [
  { id: 'gate', name: '侦探社门牌徽章', image: detectiveAgencyBadge },
  { id: 'star-wall', name: '星光线索墙', image: starClueWall },
  { id: 'herb-cabinet', name: '侦探社书架', image: archiveBookshelf },
  { id: 'display', name: '铜人展示台', image: copperDisplayStand },
  { id: 'bell-wall', name: '安全铃铛墙', image: safetyBellWall },
  { id: 'archive', name: '故事档案卡', image: storyArchiveCard },
  { id: 'roof', name: '星光屋顶装饰', image: roofStar }
]

export default generatedRewardAssets

<template>
  <section class="rail-card continuous-reward-box" aria-labelledby="continuous-reward-title">
    <div class="rail-card-heading">
      <h2 id="continuous-reward-title">星光奖励线</h2>
      <span>随手收集</span>
    </div>

    <div class="continuous-reward-hero">
      <img :src="generatedRewardAssets.rewardChestClosed" alt="奖励宝箱" />
      <div>
        <strong>{{ nextRewardTitle }}</strong>
        <span>{{ nextRewardMessage }}</span>
      </div>
    </div>

    <div class="weekly-progress" aria-label="本周学习奖励进度">
      <div><span>本周三选一</span><strong>{{ weekly.progress }}/{{ weekly.target }} 次</strong></div>
      <div class="weekly-progress__track" role="progressbar" :aria-valuenow="weekly.progress" aria-valuemin="0" :aria-valuemax="weekly.target">
        <span :style="{ width: `${weeklyPercent}%` }"></span>
      </div>
      <small>{{ weekly.claimed ? '本周奖励已领取，下周一重新开始' : weeklyHint }}</small>
    </div>

    <div class="continuous-reward-track" aria-label="贯穿式奖励进度">
      <div v-for="item in rewardSteps" :key="item.id" class="continuous-reward-step" :class="{ complete: item.complete }">
        <span class="continuous-reward-step__icon">
          <img :src="item.image" :alt="item.name" />
        </span>
        <span class="continuous-reward-step__copy">
          <strong>{{ item.name }}</strong>
          <small>{{ item.detail }}</small>
        </span>
        <i v-if="item.complete" class="fa-solid fa-circle-check" aria-label="已完成"></i>
      </div>
    </div>

    <button type="button" class="continuous-reward-action" @click="$emit('start')">
      发现下一条线索
      <span aria-hidden="true">→</span>
    </button>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useGameState } from '@/composables/useGameState'
import { generatedRewardAssets } from '@/data/generatedRewardAssets'
import { getWeeklyChoiceReward } from '@/api/GameApi'

defineEmits(['start'])

const { materials, agencyArchiveIds } = useGameState()
const weekly = ref({ progress: 0, target: 5, claimed: false })

const materialCount = (id) => Number(materials.value.find((item) => item.id === id)?.count || 0)

const rewardSteps = computed(() => [
  {
    id: 'clue',
    name: '发现线索',
    detail: `${materialCount('bamboo-slip-shard')} 张竹简`,
    image: generatedRewardAssets.clueStickerPack,
    complete: materialCount('bamboo-slip-shard') > 0
  },
  {
    id: 'map',
    name: '点亮地图',
    detail: `${materialCount('meridian-star-sand')} 份星砂`,
    image: generatedRewardAssets.bodyMapPuzzle,
    complete: materialCount('meridian-star-sand') > 0
  },
  {
    id: 'agency',
    name: '修复侦探社',
    detail: `${agencyArchiveIds.value.length}/7 项已修复`,
    image: generatedRewardAssets.starClueWall,
    complete: agencyArchiveIds.value.length > 0
  }
])

const weeklyPercent = computed(() => Math.min(100, Math.round((weekly.value.progress / Math.max(1, weekly.value.target)) * 100)))
const weeklyHint = computed(() => {
  const remaining = Math.max(0, Number(weekly.value.target || 5) - Number(weekly.value.progress || 0))
  return remaining ? `还差 ${remaining} 次有效学习，就能选择一份材料包` : '三张奖励卡已经点亮，去奖励站选择材料包'
})

const nextRewardTitle = computed(() => {
  if (!rewardSteps.value[0].complete) return '下一件奖励：竹简线索'
  if (!rewardSteps.value[1].complete) return '下一件奖励：地图星光'
  if (!rewardSteps.value[2].complete) return '下一件奖励：侦探社装饰'
  return '奖励路线继续发光'
})

const nextRewardMessage = computed(() => {
  if (!rewardSteps.value[0].complete) return '完成一次故事阅读，就能点亮第一颗星。'
  if (!rewardSteps.value[1].complete) return '完成一次探索，地图就会出现新的路径。'
  if (!rewardSteps.value[2].complete) return '收集材料后，侦探社会马上发生变化。'
  return '继续观察、阅读和判断，收集更多档案。'
})

onMounted(async () => {
  try {
    const data = await getWeeklyChoiceReward()
    if (data) weekly.value = { ...weekly.value, ...data }
  } catch {
    // 首页仍可使用本地奖励轨道，登录后奖励站会显示服务端进度。
  }
})
</script>

<style scoped>
.continuous-reward-box {
  border-color: rgba(69, 157, 142, 0.58);
  background:
    radial-gradient(circle at 84% 6%, rgba(255, 211, 90, 0.38), transparent 28%),
    linear-gradient(180deg, rgba(239, 255, 226, 0.98), rgba(255, 246, 213, 0.96));
}

.continuous-reward-hero {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.48);
}

.continuous-reward-hero img {
  width: 58px;
  height: 58px;
  object-fit: contain;
  filter: drop-shadow(0 5px 7px rgba(95, 58, 18, 0.2));
}

.continuous-reward-hero div,
.continuous-reward-step__copy {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.continuous-reward-hero strong,
.continuous-reward-step__copy strong {
  color: #4e3216;
  font-size: 12px;
  font-weight: 950;
}

.continuous-reward-hero span,
.continuous-reward-step__copy small {
  color: #6a7656;
  font-size: 10px;
  font-weight: 800;
  line-height: 1.35;
}

.weekly-progress { display: grid; gap: 5px; margin-top: 8px; padding: 8px 9px; border: 1px solid rgba(131, 153, 92, 0.28); border-radius: 9px; background: rgba(255, 252, 231, 0.72); }
.weekly-progress > div:first-child { display: flex; justify-content: space-between; gap: 8px; color: #5e4526; font-size: 11px; font-weight: 900; }
.weekly-progress > div:first-child strong { color: #b16d20; }
.weekly-progress__track { height: 7px; overflow: hidden; border-radius: 999px; background: #e2e8d3; }
.weekly-progress__track span { display: block; height: 100%; border-radius: inherit; background: linear-gradient(90deg, #53a87a, #f1bd3c); transition: width .3s ease; }
.weekly-progress small { color: #6a7656; font-size: 10px; font-weight: 800; line-height: 1.35; }

.continuous-reward-track {
  display: grid;
  gap: 5px;
  margin-top: 8px;
}

.continuous-reward-step {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  align-items: center;
  gap: 7px;
  min-height: 42px;
  padding: 4px 7px;
  border: 1px solid rgba(131, 153, 92, 0.25);
  border-radius: 9px;
  background: rgba(255, 252, 231, 0.72);
}

.continuous-reward-step.complete {
  border-color: rgba(70, 159, 135, 0.44);
  background: rgba(226, 249, 220, 0.78);
}

.continuous-reward-step__icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
}

.continuous-reward-step__icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.continuous-reward-step > i {
  color: #429b6f;
  font-size: 15px;
}

.continuous-reward-action {
  display: flex;
  width: 100%;
  min-height: 38px;
  margin-top: 8px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid rgba(157, 89, 27, 0.5);
  border-radius: 9px;
  background: linear-gradient(180deg, #f5b047, #cb6f27);
  color: #fff9e5;
  font-size: 12px;
  font-weight: 950;
  cursor: pointer;
}

.continuous-reward-action:hover {
  filter: brightness(1.05);
  transform: translateY(-1px);
}

@media (min-width: 1201px) {
  .continuous-reward-hero {
    grid-template-columns: 48px minmax(0, 1fr);
    padding: 6px;
  }

  .continuous-reward-hero img {
    width: 48px;
    height: 48px;
  }

  .continuous-reward-track {
    gap: 4px;
    margin-top: 6px;
  }

  .continuous-reward-step {
    grid-template-columns: 30px minmax(0, 1fr) auto;
    min-height: 36px;
    padding: 3px 6px;
  }

  .continuous-reward-step__icon {
    width: 28px;
    height: 28px;
  }

  .continuous-reward-action {
    min-height: 34px;
    margin-top: 6px;
  }
}
</style>

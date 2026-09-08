<template>
  <section class="agency-page" :style="pageStyle">
    <div class="agency-shell">
      <header class="agency-header">
        <div class="safety-note">
          <i class="fa-solid fa-shield-halved" aria-hidden="true"></i>
          <span>只观察、只学习，不自己针刺。</span>
        </div>
        <div class="title-plaque">
          <div class="title-plaque__knot" aria-hidden="true"></div>
          <h1>侦探社修复计划</h1>
          <div class="title-ribbon">
            <span>用线索材料修复侦探社吧！</span>
          </div>
        </div>
      </header>

      <section class="agency-summary" aria-label="侦探社修复总览">
        <div class="summary-copy">
          <span class="summary-kicker">长期玩法已记录在案</span>
          <strong>{{ repairedCount }}/{{ repairItems.length }} 项修复完成</strong>
          <p v-if="nextRepair">下一步：{{ nextRepair.displayName }} · {{ nextRepair.result }}</p>
          <p v-else>侦探社完整结案，星光宝箱已经开启。</p>
        </div>
        <div class="summary-track" role="progressbar" :aria-valuenow="repairedCount" aria-valuemin="0" :aria-valuemax="repairItems.length" aria-label="侦探社修复项目进度">
          <span :style="{ width: `${progressPercent}%` }"></span>
        </div>
        <div class="milestone-strip" aria-label="修复里程碑">
          <span v-for="milestone in rewards" :key="milestone.count" :class="{ unlocked: milestone.unlocked }">
            <img :src="milestone.icon" :alt="milestone.name" />
            <b>{{ milestone.count }}项</b>
            <small>{{ milestone.unlocked ? '已点亮' : `还差 ${Math.max(0, milestone.count - repairedCount)} 项` }}</small>
          </span>
        </div>
      </section>

      <main class="agency-grid" aria-label="小铜人侦探社修复页面">
        <aside class="clue-panel" aria-label="侦探线索箱">
          <div class="panel-title">侦探线索箱</div>
          <div class="materials-grid">
            <button
              v-for="material in materialBox"
              :key="material.id"
              class="material-tile"
              type="button"
              @click="focusMaterial(material.id)"
            >
              <img :src="material.icon" :alt="material.name" />
              <strong>x{{ inventory[material.id] }}</strong>
              <span>{{ material.name }}</span>
            </button>
          </div>
          <button class="repair-star-card" type="button" @click="openRewards">
            <img :src="repairStarIcon" alt="修补星" />
            <span>已点亮星光</span>
            <strong>{{ repairedCount }}/{{ repairItems.length }}</strong>
          </button>
        </aside>

        <section class="agency-scene" aria-label="小铜人侦探社场景">
          <div class="scene-stage">
            <div class="scene-lights" aria-hidden="true"></div>
            <img class="agency-building" :src="agencyScene" alt="小铜人侦探社" />
            <img
              v-for="decor in visibleAgencyDecorations"
              :key="decor.id"
              class="scene-reward-decor"
              :src="decor.image"
              :alt="decor.name"
              :style="decor.style"
            />
            <button
              v-for="item in repairItems"
              :key="item.id"
              class="scene-label"
              :class="sceneLabelClass(item)"
              :style="{ left: `${item.position.x}%`, top: `${item.position.y}%` }"
              type="button"
              @click="selectItem(item.id)"
            >
              <strong>{{ item.shortTitle }}</strong>
              <span>{{ isRepaired(item.id) ? item.doneLabel : item.waitLabel }}</span>
              <i
                v-if="isRepaired(item.id)"
                class="fa-solid fa-circle-check"
                aria-hidden="true"
              ></i>
              <i v-else class="fa-solid fa-arrow-up" aria-hidden="true"></i>
            </button>
          </div>
        </section>

        <aside class="task-panel" aria-label="修复任务清单">
          <section class="progress-card">
            <span>侦探社修复进度</span>
            <strong>{{ progressPercent }}%</strong>
            <div class="progress-track" aria-hidden="true">
              <div class="progress-fill" :style="progressStyle"></div>
            </div>
            <div class="star-row" :aria-label="`${progressLevel}星修复评级`">
              <i
                v-for="star in 3"
                :key="star"
                class="fa-solid fa-star"
                :class="{ on: star <= progressLevel }"
                aria-hidden="true"
              ></i>
            </div>
          </section>

          <section class="repair-list">
            <div class="repair-list__title">修复任务清单</div>
            <article
              v-for="(item, index) in visibleTasks"
              :key="item.id"
              class="repair-task"
              :class="{ selected: selectedItemId === item.id, done: isRepaired(item.id) }"
            >
              <button class="task-main" type="button" @click="selectItem(item.id)">
                <span class="task-index">{{ index + 1 }}</span>
                <span class="task-copy">
                  <strong>{{ item.title }}</strong>
                  <small>
                    消耗：
                    <span
                      v-for="cost in item.costs"
                      :key="cost.id"
                      class="cost-chip"
                    >
                      <img :src="cost.icon" :alt="cost.name" />
                      x{{ cost.count }}
                    </span>
                  </small>
                  <em>{{ item.result }}</em>
                </span>
              </button>
              <button
                class="repair-btn"
                type="button"
                :disabled="isRepaired(item.id) || !canRepair(item) || Boolean(repairingItemId) || repairingAll"
                @click="repairItem(item)"
              >
                {{ repairButtonText(item) }}
              </button>
              <small class="repair-reason" v-if="!isRepaired(item.id)">{{ repairReason(item) }}</small>
            </article>
          </section>
        </aside>
      </main>

      <footer class="agency-actions">
        <div class="success-card">
          <span class="success-ribbon">修复成功！</span>
          <p>本次可修复</p>
          <strong>{{ repairableCount }}</strong>
          <span>项</span>
          <i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i>
        </div>

        <button class="large-action large-action--green" type="button" @click="openRewards">
          <i class="fa-solid fa-gift" aria-hidden="true"></i>
          <span>查看奖励</span>
        </button>
        <button class="large-action large-action--gold" type="button" :disabled="Boolean(repairingItemId) || repairingAll" @click="repairAll">
          <i class="fa-solid fa-hammer" aria-hidden="true"></i>
          <span>一键修复可修复项</span>
          <b v-if="repairableCount">{{ repairableCount }}</b>
        </button>
        <button class="large-action large-action--paper" type="button" @click="goHomeMap">
          <i class="fa-solid fa-map-location-dot" aria-hidden="true"></i>
          <span>返回地图</span>
        </button>
      </footer>
    </div>

    <transition name="agency-toast">
      <div v-if="feedback" class="feedback-toast" role="status">
        <i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i>
        <span>{{ feedback }}</span>
      </div>
    </transition>

    <transition name="agency-modal">
      <div v-if="rewardOpen" class="reward-mask" @click.self="rewardOpen = false">
        <section class="reward-dialog" role="dialog" aria-modal="true" aria-labelledby="reward-title">
          <button class="reward-close" type="button" aria-label="关闭奖励弹窗" @click="rewardOpen = false">
            <i class="fa-solid fa-xmark" aria-hidden="true"></i>
          </button>
          <h2 id="reward-title">修复里程碑</h2>
          <p>每个里程碑只点亮一次，完成修复就能解锁新的线索档案和场景装饰。</p>
          <div class="reward-grid">
            <div
              v-for="reward in rewards"
              :key="reward.name"
              class="reward-item"
              :class="{ 'is-locked': !reward.unlocked }"
            >
              <img :src="reward.icon" :alt="reward.name" />
              <strong>{{ reward.name }}</strong>
              <span>{{ reward.unlocked ? '已经点亮' : reward.detail }}</span>
            </div>
          </div>
          <button class="reward-confirm" type="button" @click="rewardOpen = false">
            知道了
          </button>
        </section>
      </div>
    </transition>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameState } from '@/composables/useGameState'
import mapParchment from '@/assets/maps/xinglin-detective-map-bg.png'
import agencyScene from '@/assets/侦探社/1.png'
import {
  generatedAgencyDecorations,
  generatedMaterialIcons,
  generatedRewardAssets
} from '@/data/generatedRewardAssets'
import {
  agencyRepairMilestones,
  agencyRepairRecipes,
  unmetAgencyPrerequisites
} from '@/data/agencyRewards'

const router = useRouter()
const route = useRoute()
const { materials, agencyArchiveIds, exchangeAgencyArchiveItem } = useGameState()
const repairStarIcon = generatedRewardAssets.rewardStarBurst
const repairedIds = agencyArchiveIds
const inventory = computed(() => materials.value.reduce((acc, item) => {
  acc[item.id] = Number(item.count || 0)
  return acc
}, {}))
const requestedItemId = String(route.query.item || '')
const selectedItemId = ref(agencyRepairRecipes.some((item) => item.id === requestedItemId) ? requestedItemId : 'gate')
const rewardOpen = ref(false)
const feedback = ref('')
const repairingItemId = ref('')
const repairingAll = ref(false)
let feedbackTimer = 0

const materialBox = [
  { id: 'bamboo-slip-shard', name: '竹简碎片', icon: generatedMaterialIcons['bamboo-slip-shard'] },
  { id: 'herbal-leaf', name: '草药叶', icon: generatedMaterialIcons['herbal-leaf'] },
  { id: 'meridian-star-sand', name: '经络星砂', icon: generatedMaterialIcons['meridian-star-sand'] },
  { id: 'acupoint-star-pearl', name: '穴位星珠', icon: generatedMaterialIcons['acupoint-star-pearl'] },
  { id: 'copper-token', name: '铜片', icon: generatedMaterialIcons['copper-token'] },
  { id: 'safety-bell', name: '安全铃铛', icon: generatedMaterialIcons['safety-bell'] }
]

const repairItems = agencyRepairRecipes
const rewards = computed(() => agencyRepairMilestones.map((reward) => ({
  ...reward,
  unlocked: repairedCount.value >= reward.count
})))

const agencyDecorations = generatedAgencyDecorations.map((decor) => {
  const styles = {
    gate: { left: '43%', top: '24%', width: '14%' },
    'star-wall': { left: '4%', top: '16%', width: '37%' },
    'herb-cabinet': { left: '61%', top: '23%', width: '28%' },
    display: { left: '34%', top: '43%', width: '32%' },
    'bell-wall': { left: '4%', top: '55%', width: '31%' },
    archive: { left: '65%', top: '56%', width: '23%' },
    roof: { left: '25%', top: '0%', width: '52%' }
  }
  return { ...decor, style: styles[decor.id] }
})
const visibleAgencyDecorations = computed(() => agencyDecorations.filter((decor) => isRepaired(decor.id)))

const pageStyle = computed(() => ({
  '--map-bg': `url(${mapParchment})`
}))

const visibleTasks = computed(() => repairItems.filter((item) => item.showInList !== false))
const repairedCount = computed(() => repairedIds.value.length)
const progressPercent = computed(() => Math.round((repairedCount.value / repairItems.length) * 100))
const nextRepair = computed(() => repairItems.find((item) => !isRepaired(item.id)) || null)
const progressStyle = computed(() => ({ width: `${progressPercent.value}%` }))
const progressLevel = computed(() => {
  if (progressPercent.value === 0) return 0
  if (progressPercent.value >= 92) return 3
  if (progressPercent.value >= 68) return 2
  return 1
})

const repairableCount = computed(() => repairItems.filter((item) => !isRepaired(item.id) && canRepair(item)).length)

function isRepaired(itemId) {
  return repairedIds.value.includes(itemId)
}

function hasEnough(costs) {
  return costs.every((cost) => Number(inventory.value[cost.id] || 0) >= cost.count)
}

function canRepair(item) {
  return isUnlocked(item) && hasEnough(item.costs)
}

function isUnlocked(item) {
  return unmetAgencyPrerequisites(item, repairedIds.value).length === 0
}

function repairButtonText(item) {
  if (isRepaired(item.id)) return '已修复'
  if (!isUnlocked(item)) return '先修复前一项'
  if (!hasEnough(item.costs)) return '材料还不够'
  return '立即修复'
}

function repairReason(item) {
  if (isRepaired(item.id)) return '已记录到侦探社档案'
  const missingPrerequisites = unmetAgencyPrerequisites(item, repairedIds.value)
  if (missingPrerequisites.length) {
    const names = missingPrerequisites
      .map((id) => repairItems.find((target) => target.id === id)?.displayName || id)
      .join('、')
    return `前置未完成：先修复 ${names}`
  }
  const missingCosts = item.costs
    .map((cost) => ({ ...cost, owned: inventory.value[cost.id] || 0 }))
    .filter((cost) => cost.owned < cost.count)
  if (missingCosts.length) {
    return `还差 ${missingCosts.map((cost) => `${cost.name} ${cost.count - cost.owned}`).join('、')}`
  }
  return `材料齐全 · 修复后${item.result}`
}

function sceneLabelClass(item) {
  return {
    fixed: isRepaired(item.id),
    waiting: !isRepaired(item.id),
    selected: selectedItemId.value === item.id
  }
}

function selectItem(itemId) {
  selectedItemId.value = itemId
  const item = repairItems.find((target) => target.id === itemId)
  if (item) showFeedback(isRepaired(item.id) ? `${item.shortTitle}已经完成` : `${item.shortTitle}等待修复`)
}

function focusMaterial(materialId) {
  const related = repairItems.find((item) => !isRepaired(item.id) && item.costs.some((cost) => cost.id === materialId))
  if (related) {
    selectedItemId.value = related.id
    showFeedback(`${related.shortTitle}可以使用该材料`)
    return
  }
  showFeedback('这种线索材料已经准备充足')
}

async function repairItem(item) {
  selectedItemId.value = item.id
  if (isRepaired(item.id)) {
    showFeedback(`${item.shortTitle}已经修复完成`)
    return
  }
  if (!isUnlocked(item)) {
    showFeedback(`${item.shortTitle}需要先完成前面的修复`)
    return
  }
  if (!canRepair(item)) {
    showFeedback(`${item.shortTitle}还缺少线索材料`)
    return
  }
  repairingItemId.value = item.id
  try {
    const repaired = await exchangeAgencyArchiveItem(item)
    showFeedback(repaired ? `${item.shortTitle}修复完成` : `${item.shortTitle}暂时无法修复`)
  } catch (error) {
    showFeedback(error.message || `${item.shortTitle}暂时无法修复`)
  } finally {
    repairingItemId.value = ''
  }
}

async function repairAll() {
  if (repairingAll.value || repairingItemId.value) return
  repairingAll.value = true
  let fixedNow = 0

  try {
    for (const item of repairItems) {
      if (isRepaired(item.id) || !canRepair(item)) continue
      if (await exchangeAgencyArchiveItem(item)) fixedNow += 1
    }
    showFeedback(fixedNow ? `本次修复完成 ${fixedNow} 项` : '当前没有可一键修复的项目')
  } catch (error) {
    showFeedback(error.message || '修复请求暂时失败，请稍后再试')
  } finally {
    repairingAll.value = false
  }
}

function openRewards() {
  rewardOpen.value = true
}

function goHomeMap() {
  router.push('/home-map')
}

function showFeedback(text) {
  feedback.value = text
  window.clearTimeout(feedbackTimer)
  feedbackTimer = window.setTimeout(() => {
    feedback.value = ''
  }, 1800)
}
</script>

<style scoped>
.agency-page {
  min-height: 100vh;
  overflow-x: hidden;
  color: #4b2a0d;
  background:
    radial-gradient(circle at 50% 22%, rgba(255, 248, 211, 0.82) 0 28%, rgba(232, 184, 101, 0.2) 62%, rgba(102, 58, 19, 0.18) 100%),
    var(--map-bg) center / cover no-repeat;
  font-family: "STKaiti", "KaiTi", "Ma Shan Zheng", "Microsoft YaHei", sans-serif;
}

.agency-shell {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 6px;
  width: min(1740px, 100%);
  min-height: 100vh;
  margin: 0 auto;
  padding: 8px clamp(18px, 2.8vw, 58px) 10px;
}

.agency-header {
  display: grid;
  justify-items: center;
  gap: 4px;
}

.safety-note {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 28px;
  color: #3b2a18;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: clamp(15px, 1.25vw, 21px);
  font-weight: 900;
  letter-spacing: 0;
}

.safety-note i {
  color: #4f6f73;
  filter: drop-shadow(0 1px 0 rgba(255, 255, 255, 0.7));
}

.title-plaque {
  position: relative;
  display: grid;
  justify-items: center;
  min-width: min(720px, 90vw);
  padding: 13px 44px 21px;
  border: 4px solid #9c6427;
  border-radius: 36px 36px 22px 22px;
  background:
    linear-gradient(180deg, rgba(255, 247, 207, 0.96), rgba(232, 182, 104, 0.96)),
    radial-gradient(circle, rgba(255, 255, 255, 0.45), transparent 58%);
  box-shadow:
    inset 0 0 0 3px rgba(255, 233, 165, 0.8),
    0 7px 0 #6f4118,
    0 12px 22px rgba(91, 49, 16, 0.32);
}

.title-plaque h1 {
  margin: 0;
  color: #4a2708;
  font-size: clamp(42px, 5.4vw, 78px);
  font-weight: 950;
  line-height: 0.98;
  text-shadow: 0 3px 0 rgba(255, 229, 164, 0.9), 0 7px 10px rgba(72, 34, 7, 0.18);
}

.title-ribbon {
  position: absolute;
  bottom: -22px;
  left: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: min(510px, 78vw);
  min-height: 42px;
  padding: 7px 28px;
  color: #fff5bc;
  border: 3px solid #d7ad51;
  border-radius: 18px;
  background: linear-gradient(180deg, #667f38, #3f6735);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.25), 0 4px 0 rgba(78, 48, 15, 0.4);
  transform: translateX(-50%);
}

.title-ribbon span {
  font-size: clamp(18px, 1.8vw, 28px);
  font-weight: 950;
}

.agency-summary {
  display: grid;
  grid-template-columns: minmax(230px, 0.9fr) minmax(220px, 1.1fr);
  gap: 8px 24px;
  align-items: center;
  margin: 30px auto 0;
  padding: 14px 20px;
  border: 2px solid rgba(154, 96, 33, 0.55);
  border-radius: 16px;
  background: rgba(255, 248, 218, 0.9);
  box-shadow: 0 5px 12px rgba(101, 59, 22, 0.12);
}

.summary-copy { display: grid; gap: 3px; min-width: 0; }
.summary-kicker { color: #866125; font-size: 12px; font-weight: 900; }
.summary-copy strong { color: #4d2d0e; font-size: 22px; }
.summary-copy p { margin: 0; color: #72562e; font-size: 13px; line-height: 1.4; }
.summary-track { height: 13px; padding: 2px; overflow: hidden; border: 1px solid #9e641f; border-radius: 999px; background: #6d3d12; }
.summary-track span { display: block; height: 100%; border-radius: inherit; background: linear-gradient(90deg, #f8b626, #ffe267); transition: width .3s ease; }
.milestone-strip { grid-column: 1 / -1; display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.milestone-strip span { display: grid; grid-template-columns: 34px 1fr; grid-template-rows: auto auto; column-gap: 7px; align-items: center; min-width: 0; padding: 6px 8px; border: 1px solid rgba(154, 96, 33, 0.25); border-radius: 10px; background: rgba(255, 252, 231, 0.64); }
.milestone-strip img { grid-row: 1 / 3; width: 31px; height: 31px; object-fit: contain; filter: grayscale(.8); opacity: .65; }
.milestone-strip .unlocked { border-color: rgba(70, 145, 83, .5); background: rgba(228, 248, 214, .72); }
.milestone-strip .unlocked img { filter: none; opacity: 1; }
.milestone-strip b { color: #5a3816; font-size: 12px; }
.milestone-strip small { color: #836c4a; font-size: 11px; white-space: nowrap; }

.agency-grid {
  display: grid;
  grid-template-columns: minmax(230px, 300px) minmax(520px, 1fr) minmax(315px, 390px);
  gap: clamp(14px, 1.5vw, 24px);
  align-items: stretch;
  padding-top: 30px;
}

.agency-grid > * {
  min-width: 0;
}

.clue-panel,
.task-panel,
.progress-card,
.repair-list {
  border: 3px solid rgba(154, 96, 33, 0.72);
  background:
    linear-gradient(180deg, rgba(255, 243, 203, 0.95), rgba(238, 200, 128, 0.9)),
    repeating-linear-gradient(45deg, rgba(120, 77, 25, 0.05) 0 1px, transparent 1px 10px);
  box-shadow:
    inset 0 0 0 2px rgba(255, 250, 222, 0.86),
    0 8px 18px rgba(101, 59, 22, 0.18);
}

.clue-panel {
  align-self: start;
  padding: 22px 18px 18px;
  border-radius: 28px 28px 20px 20px;
}

.panel-title,
.repair-list__title {
  display: grid;
  min-height: 42px;
  margin: 0 auto 14px;
  place-items: center;
  color: #56300e;
  font-size: clamp(20px, 1.8vw, 29px);
  font-weight: 950;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.material-tile {
  display: grid;
  min-height: 126px;
  padding: 10px 6px 8px;
  color: #4a2d11;
  text-align: center;
  border: 2px solid rgba(163, 104, 37, 0.42);
  border-radius: 14px;
  background: rgba(255, 247, 220, 0.68);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.58);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.material-tile:hover,
.material-tile:focus-visible,
.repair-star-card:hover,
.repair-star-card:focus-visible {
  border-color: rgba(83, 126, 52, 0.78);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.65), 0 6px 16px rgba(89, 51, 17, 0.16);
  outline: none;
  transform: translateY(-2px);
}

.material-tile img {
  width: min(64px, 72%);
  height: 58px;
  margin: 0 auto;
  object-fit: contain;
  filter: drop-shadow(0 4px 4px rgba(82, 48, 14, 0.22));
}

.material-tile strong {
  margin-top: -4px;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 23px;
  font-weight: 950;
}

.material-tile span {
  font-size: 17px;
  font-weight: 900;
  line-height: 1.15;
}

.repair-star-card {
  display: grid;
  grid-template-columns: 68px 1fr auto;
  gap: 9px;
  align-items: center;
  width: 100%;
  min-height: 78px;
  margin-top: 14px;
  padding: 10px 14px;
  color: #543012;
  border: 2px solid rgba(163, 104, 37, 0.42);
  border-radius: 16px;
  background: rgba(255, 247, 220, 0.7);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.repair-star-card img {
  width: 62px;
  height: 62px;
  object-fit: contain;
}

.repair-star-card span,
.repair-star-card strong {
  font-size: 20px;
  font-weight: 950;
}

.agency-scene {
  display: grid;
  min-height: 560px;
  place-items: center;
}

.scene-stage {
  position: relative;
  width: 100%;
  max-width: 800px;
  aspect-ratio: 1.38;
}

.scene-lights {
  position: absolute;
  inset: 9% 4% 4%;
  z-index: 1;
  border-radius: 40px;
  background:
    radial-gradient(circle at 20% 42%, rgba(255, 234, 117, 0.48) 0 2px, transparent 3px),
    radial-gradient(circle at 74% 34%, rgba(255, 238, 142, 0.5) 0 2px, transparent 4px),
    radial-gradient(circle at 48% 58%, rgba(255, 224, 93, 0.52) 0 3px, transparent 6px);
  filter: drop-shadow(0 0 10px rgba(255, 202, 71, 0.88));
  pointer-events: none;
  animation: shimmer 3s ease-in-out infinite;
}

.agency-building {
  position: absolute;
  inset: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  mix-blend-mode: multiply;
  -webkit-mask-image: radial-gradient(ellipse at center, #000 0 62%, rgba(0, 0, 0, 0.84) 72%, transparent 89%);
  mask-image: radial-gradient(ellipse at center, #000 0 62%, rgba(0, 0, 0, 0.84) 72%, transparent 89%);
  filter: drop-shadow(0 22px 22px rgba(71, 41, 17, 0.28));
}

.scene-reward-decor {
  position: absolute;
  z-index: 1;
  height: auto;
  object-fit: contain;
  pointer-events: none;
  filter: drop-shadow(0 7px 8px rgba(71, 41, 17, 0.2));
  animation: agency-decor-arrive 0.45s ease both;
}

.scene-label {
  position: absolute;
  z-index: 2;
  display: grid;
  min-width: 132px;
  min-height: 58px;
  padding: 8px 38px 8px 14px;
  color: #60380e;
  text-align: left;
  border: 2px solid #b88335;
  border-radius: 12px;
  background: rgba(255, 242, 195, 0.94);
  box-shadow: 0 4px 0 rgba(117, 67, 18, 0.28), inset 0 2px 0 rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transform: translate(-50%, -50%);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.scene-label:hover,
.scene-label:focus-visible,
.scene-label.selected {
  border-color: #f0b632;
  outline: none;
  box-shadow: 0 0 0 4px rgba(255, 213, 79, 0.26), 0 5px 0 rgba(117, 67, 18, 0.25);
  transform: translate(-50%, -55%);
}

.scene-label.fixed {
  border-color: #7c9f42;
}

.scene-label strong {
  font-size: clamp(15px, 1.3vw, 20px);
  font-weight: 950;
  line-height: 1.1;
  white-space: nowrap;
}

.scene-label span {
  display: inline-grid;
  min-height: 24px;
  margin-top: 4px;
  padding: 2px 10px;
  place-items: center;
  color: #fff9d8;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 13px;
  font-weight: 900;
  border-radius: 999px;
  background: #7b5a22;
}

.scene-label.fixed span {
  background: linear-gradient(180deg, #6fa63c, #477e31);
}

.scene-label.waiting span {
  color: #6c4215;
  background: #f6dd9f;
}

.scene-label i {
  position: absolute;
  right: -13px;
  top: 50%;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  color: #fffde4;
  border: 2px solid rgba(255, 246, 194, 0.9);
  border-radius: 50%;
  background: linear-gradient(180deg, #75ad3e, #40772e);
  box-shadow: 0 3px 8px rgba(71, 42, 13, 0.22);
  transform: translateY(-50%);
}

.scene-label.waiting i {
  background: linear-gradient(180deg, #d9912f, #a85f1d);
}

.task-panel {
  display: grid;
  align-self: start;
  gap: 12px;
  padding: 14px;
  border-color: transparent;
  background: transparent;
  box-shadow: none;
}

.progress-card {
  padding: 16px 18px 14px;
  text-align: center;
  border-radius: 18px;
}

.progress-card span {
  display: block;
  color: #5a3816;
  font-size: 17px;
  font-weight: 900;
}

.progress-card strong {
  display: block;
  margin: 3px 0 7px;
  color: #a65018;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 42px;
  font-weight: 950;
  line-height: 1;
}

.progress-track {
  width: 100%;
  height: 18px;
  padding: 3px;
  border: 2px solid #9e641f;
  border-radius: 999px;
  background: #6d3d12;
  box-shadow: inset 0 2px 4px rgba(55, 29, 5, 0.35);
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #f8b626, #ffe267);
  box-shadow: 0 0 9px rgba(255, 213, 82, 0.7);
  transition: width 0.3s ease;
}

.star-row {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  color: #9a8a78;
  font-size: 24px;
}

.star-row .on {
  color: #ffc83b;
  text-shadow: 0 2px 0 #9a5f1e, 0 0 8px rgba(255, 210, 83, 0.7);
}

.repair-list {
  overflow: hidden;
  border-radius: 18px;
}

.repair-list__title {
  min-height: 40px;
  margin-bottom: 0;
  border-bottom: 2px solid rgba(145, 89, 26, 0.25);
  background: rgba(173, 112, 36, 0.11);
  font-size: 22px;
}

.repair-task {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 12px 12px;
  border-bottom: 1px solid rgba(145, 89, 26, 0.22);
  transition: background 0.18s ease;
}

.repair-task:last-child {
  border-bottom: none;
}

.repair-task.selected,
.repair-task:hover {
  background: rgba(255, 239, 186, 0.62);
}

.repair-task.done {
  background: rgba(230, 247, 205, 0.38);
}

.task-main {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 10px;
  align-items: center;
  min-width: 0;
  padding: 0;
  color: inherit;
  text-align: left;
  border: none;
  background: transparent;
  cursor: pointer;
}

.task-main:focus-visible,
.repair-btn:focus-visible,
.large-action:focus-visible,
.reward-close:focus-visible,
.reward-confirm:focus-visible {
  outline: 3px solid rgba(69, 126, 49, 0.45);
  outline-offset: 2px;
}

.task-index {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  color: #fff6d5;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 20px;
  font-weight: 950;
  border-radius: 50%;
  background: #79501d;
}

.task-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.task-copy strong {
  overflow: hidden;
  color: #4d2d0e;
  font-size: 17px;
  font-weight: 950;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-copy small {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  align-items: center;
  color: #6a431b;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 13px;
  font-weight: 800;
}

.task-copy em {
  overflow: hidden;
  color: #886d46;
  font-size: 11px;
  font-style: normal;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cost-chip {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.cost-chip img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.repair-btn {
  min-width: 92px;
  min-height: 40px;
  padding: 7px 14px;
  color: #fff9d9;
  border: 2px solid #d2ab55;
  border-radius: 999px;
  background: linear-gradient(180deg, #78a546, #47782d);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.25), 0 3px 0 #7d581f;
  font-size: 15px;
  font-weight: 950;
  cursor: pointer;
}

.repair-reason {
  grid-column: 1 / -1;
  margin: -4px 0 0 46px;
  color: #8b5b24;
  font-size: 11px;
  line-height: 1.3;
}

.repair-btn:disabled {
  color: #f1e5c2;
  background: linear-gradient(180deg, #b2a27c, #82715c);
  box-shadow: none;
  cursor: default;
}

.agency-actions {
  display: grid;
  grid-template-columns: minmax(210px, 315px) minmax(190px, 1fr) minmax(260px, 1.25fr) minmax(190px, 1fr);
  gap: clamp(10px, 1.5vw, 22px);
  align-items: center;
}

.success-card,
.large-action {
  min-height: 68px;
  border: 3px solid rgba(151, 91, 27, 0.7);
  border-radius: 20px;
  box-shadow: inset 0 0 0 2px rgba(255, 250, 222, 0.82), 0 7px 15px rgba(87, 48, 14, 0.18);
}

.success-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto auto 42px;
  gap: 8px;
  align-items: end;
  padding: 22px 18px 12px;
  color: #5c310e;
  background: rgba(255, 244, 204, 0.92);
}

.success-ribbon {
  position: absolute;
  top: -18px;
  left: 38px;
  padding: 5px 24px 7px;
  color: #fff4ce;
  border-radius: 8px 8px 13px 13px;
  background: linear-gradient(180deg, #c85e25, #9b3515);
  box-shadow: 0 3px 0 rgba(92, 46, 15, 0.38);
  font-size: 19px;
  font-weight: 950;
}

.success-card p,
.success-card span:not(.success-ribbon) {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
}

.success-card strong {
  color: #b54e16;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 40px;
  font-weight: 950;
  line-height: 0.9;
}

.success-card i {
  color: #89652d;
  font-size: 31px;
}

.large-action {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 20px;
  font-size: clamp(19px, 1.9vw, 29px);
  font-weight: 950;
  cursor: pointer;
  transition: transform 0.18s ease, filter 0.18s ease;
}

.large-action:hover {
  filter: brightness(1.04);
  transform: translateY(-2px);
}

.large-action i {
  font-size: 28px;
}

.large-action--green {
  color: #fff6cf;
  background: linear-gradient(180deg, #769548, #456d32);
}

.large-action--gold {
  color: #fff8cf;
  background: linear-gradient(180deg, #ebb83e, #b86e20);
}

.large-action--paper {
  color: #5a3514;
  background: linear-gradient(180deg, #fff0bd, #e2b96d);
}

.large-action b {
  position: absolute;
  top: -16px;
  right: -12px;
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  color: #fff7d9;
  border: 2px solid #ffe0a0;
  border-radius: 50%;
  background: #be4525;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 18px;
}

.feedback-toast {
  position: fixed;
  left: 50%;
  bottom: 104px;
  z-index: 40;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  max-width: min(92vw, 420px);
  padding: 12px 20px;
  color: #fff8d8;
  border: 2px solid rgba(255, 232, 161, 0.78);
  border-radius: 999px;
  background: rgba(79, 49, 17, 0.94);
  box-shadow: 0 10px 28px rgba(61, 32, 8, 0.24);
  font-family: "Microsoft YaHei", sans-serif;
  font-weight: 900;
  transform: translateX(-50%);
}

.reward-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  padding: 24px;
  place-items: center;
  background: rgba(52, 31, 9, 0.42);
  backdrop-filter: blur(4px);
}

.reward-dialog {
  position: relative;
  width: min(620px, 100%);
  padding: 30px 28px 26px;
  text-align: center;
  border: 4px solid #9e6423;
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 247, 212, 0.98), rgba(235, 194, 118, 0.98)),
    repeating-linear-gradient(45deg, rgba(120, 77, 25, 0.06) 0 1px, transparent 1px 10px);
  box-shadow: inset 0 0 0 3px rgba(255, 252, 224, 0.86), 0 22px 60px rgba(53, 29, 8, 0.36);
}

.reward-close {
  position: absolute;
  top: 12px;
  right: 12px;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  color: #5b3512;
  border: 2px solid rgba(132, 82, 24, 0.36);
  border-radius: 50%;
  background: rgba(255, 245, 207, 0.82);
  cursor: pointer;
}

.reward-dialog h2 {
  margin: 0 0 8px;
  color: #4c2a0d;
  font-size: 34px;
  font-weight: 950;
}

.reward-dialog p {
  margin: 0 auto 20px;
  max-width: 440px;
  color: #6b431b;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.65;
}

.reward-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.reward-item {
  display: grid;
  gap: 8px;
  justify-items: center;
  min-height: 178px;
  padding: 16px 10px;
  border: 2px solid rgba(145, 89, 26, 0.28);
  border-radius: 16px;
  background: rgba(255, 247, 218, 0.72);
}

.reward-item.is-locked {
  filter: grayscale(0.58) saturate(0.6);
  opacity: 0.64;
}

.reward-item img {
  width: 78px;
  height: 78px;
  object-fit: contain;
}

.reward-item strong {
  color: #4c2c10;
  font-size: 17px;
  font-weight: 950;
}

.reward-item span {
  color: #6a431b;
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.4;
}

.reward-confirm {
  min-width: 160px;
  min-height: 48px;
  margin-top: 20px;
  color: #fff8cf;
  border: 2px solid #d7ad51;
  border-radius: 999px;
  background: linear-gradient(180deg, #79a548, #476f32);
  font-size: 17px;
  font-weight: 950;
  cursor: pointer;
}

.agency-toast-enter-active,
.agency-toast-leave-active,
.agency-modal-enter-active,
.agency-modal-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.agency-toast-enter-from,
.agency-toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 12px);
}

.agency-modal-enter-from,
.agency-modal-leave-to {
  opacity: 0;
}

@keyframes shimmer {
  0%, 100% {
    opacity: 0.68;
  }
  50% {
    opacity: 1;
  }
}

@keyframes agency-decor-arrive {
  from {
    opacity: 0;
    transform: scale(0.82);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@media (max-width: 1180px) {
  .agency-summary { grid-template-columns: 1fr; }
  .summary-track { grid-row: 2; }

  .agency-shell {
    min-height: auto;
  }

  .agency-grid {
    grid-template-columns: minmax(220px, 280px) 1fr;
  }

  .task-panel {
    grid-column: 1 / -1;
    grid-template-columns: minmax(280px, 0.72fr) 1fr;
  }

  .agency-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 760px) {
  .agency-page {
    width: 100vw;
    max-width: 100vw;
    overflow-x: hidden;
  }

  .agency-shell {
    width: 100vw;
    max-width: 100vw;
    overflow-x: hidden;
    padding: 8px 12px 18px;
  }

  .agency-grid {
    width: 100%;
    max-width: 100%;
  }

  .title-plaque {
    min-width: 0;
    width: 100%;
    padding: 13px 12px 22px;
  }

  .title-plaque h1 {
    font-size: clamp(32px, 9.4vw, 40px);
    white-space: nowrap;
  }

  .title-ribbon {
    min-width: min(420px, 84vw);
  }

  .title-ribbon span {
    font-size: clamp(15px, 4.3vw, 18px);
    white-space: nowrap;
  }

  .agency-grid,
  .task-panel,
  .agency-actions,
  .reward-grid {
    grid-template-columns: 1fr;
  }

  .agency-summary { margin-top: 26px; padding: 13px 14px; }
  .milestone-strip { grid-template-columns: repeat(2, 1fr); }
  .summary-copy strong { font-size: 19px; }

  .agency-scene {
    display: block;
    order: 1;
    min-height: 310px;
    width: 100%;
    min-width: 0;
  }

  .clue-panel {
    order: 2;
    width: 100%;
    max-width: calc(100vw - 24px);
    padding: 18px 16px 16px;
    justify-self: center;
  }

  .materials-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .material-tile {
    min-height: 94px;
    padding: 8px 4px;
  }

  .material-tile img {
    height: 48px;
  }

  .material-tile strong {
    font-size: 21px;
  }

  .material-tile span {
    font-size: 12px;
  }

  .scene-stage {
    width: 100%;
    min-width: 0;
    justify-self: stretch;
  }

  .agency-scene {
    overflow: hidden;
    justify-content: center;
    padding-bottom: 0;
  }

  .scene-label {
    min-width: 86px;
    min-height: 44px;
    padding: 6px 20px 6px 8px;
  }

  .scene-label strong {
    font-size: 12px;
  }

  .scene-label span {
    min-height: 20px;
    padding-inline: 6px;
    font-size: 10px;
  }

  .scene-label i {
    right: -9px;
    width: 28px;
    height: 28px;
  }

  .task-panel {
    order: 3;
  }

  .repair-task {
    grid-template-columns: 1fr;
  }

  .repair-btn {
    width: 100%;
  }
}
.agency-page,
.agency-shell { min-height: calc(100vh - var(--site-nav-height)); }
.agency-page { font-family: var(--site-body-font), "Microsoft YaHei", sans-serif; }
.title-plaque h1, .panel-title, .repair-list__title { font-family: var(--site-title-font), "Microsoft YaHei", serif; }
</style>

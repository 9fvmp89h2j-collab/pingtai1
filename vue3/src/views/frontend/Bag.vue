<template>
  <main class="bag-page">
    <section class="bag-hero">
      <div class="bag-hero__copy">
        <p class="eyebrow">
          杏林小药师 · 材料袋
        </p>
        <h1>今天收集到的星光材料</h1>
        <p class="lead">
          每完成一个故事、身体地图、经络星河或安全任务，材料袋里都会多一点光。带着这些材料回到侦探社，就能整理故事墙、安全标识和经络星幕。
        </p>
        <div class="bag-stats">
          <span><b>{{ ownedCount }}</b> 已拥有材料</span>
          <span><b>{{ totalCount }}</b> 材料总数</span>
          <span><b>{{ exchangeableCount }}</b> 可兑换提醒</span>
        </div>
        <router-link class="reward-center-link" to="/rewards">
          <span aria-hidden="true">★</span>
          去星光奖励站选本周奖励
        </router-link>
      </div>

      <div
        class="bag-hero__pouch"
        aria-hidden="true"
      >
        <img
          :src="treasureBox"
          alt=""
        >
        <span class="spark spark--one" />
        <span class="spark spark--two" />
        <span class="spark spark--three" />
      </div>
    </section>

    <section class="bag-layout">
      <div class="materials-zone">
        <div class="section-head">
          <p>基础材料</p>
          <h2>整理侦探社的小材料</h2>
          <span>这些材料来自故事馆、身体地图和安全课堂，可以用来完善侦探社档案。</span>
        </div>
        <div class="material-grid">
          <article
            v-for="item in basicMaterials"
            :key="item.id"
            class="material-card"
            :class="[`material-card--${item.rarity}`, { 'is-locked': item.status === 'locked' }]"
          >
            <div class="material-card__top">
              <div class="material-card__icon">
                <img
                  :src="item.icon"
                  :alt="item.name"
                >
              </div>
              <span class="material-card__count">× {{ item.count }}</span>
            </div>
            <div class="material-card__body">
              <p>{{ rarityText(item.rarity) }}</p>
              <h3>{{ item.name }}</h3>
              <span>{{ item.description }}</span>
            </div>
            <div class="material-card__meta">
              <div>
                <b>来源</b>
                <span>{{ item.source.join('、') }}</span>
              </div>
              <div>
                <b>用途</b>
                <span>{{ item.usage.join('、') }}</span>
              </div>
            </div>
          </article>
        </div>

        <div class="section-head section-head--rare">
          <p>稀有材料</p>
          <h2>解锁隐藏路线的闪光道具</h2>
          <span>稀有材料会在连续答对、整条经络挑战或星光修补册里出现。</span>
        </div>
        <div class="material-grid material-grid--rare">
          <article
            v-for="item in rareMaterials"
            :key="item.id"
            class="material-card"
            :class="[`material-card--${item.rarity}`, { 'is-locked': item.status === 'locked' }]"
          >
            <div class="material-card__top">
              <div class="material-card__icon">
                <img
                  :src="item.icon"
                  :alt="item.name"
                >
              </div>
              <span class="material-card__count">× {{ item.count }}</span>
            </div>
            <div class="material-card__body">
              <p>{{ rarityText(item.rarity) }}</p>
              <h3>{{ item.name }}</h3>
              <span>{{ item.description }}</span>
            </div>
            <div class="material-card__meta">
              <div>
                <b>来源</b>
                <span>{{ item.source.join('、') }}</span>
              </div>
              <div>
                <b>用途</b>
                <span>{{ item.usage.join('、') }}</span>
              </div>
            </div>
          </article>
        </div>
      </div>

      <aside class="exchange-panel">
        <div class="exchange-panel__head">
          <p>可兑换提醒</p>
          <h2>侦探社还差哪些材料？</h2>
        </div>

        <div class="exchange-list">
          <article
            v-for="recipe in exchangeRecipes"
            :key="recipe.id"
            class="exchange-card"
            :class="{ 'is-ready': recipe.ready, 'is-repaired': recipe.repaired, 'is-locked': !recipe.unlocked }"
          >
            <div class="exchange-card__title">
              <span>
                <img :src="recipe.icon" :alt="recipe.name" />
              </span>
              <div>
                <h3>{{ recipe.name }}</h3>
                <p>{{ recipe.description }}</p>
              </div>
            </div>
            <div class="recipe-needs">
              <span
                v-for="need in recipe.needs"
                :key="need.id"
                :class="{ 'is-enough': need.owned >= need.count }"
              >
                {{ need.name }} {{ need.owned }}/{{ need.count }}
              </span>
            </div>
            <button
              type="button"
              :disabled="recipe.repaired || !recipe.ready"
              @click="goAgency(recipe.id)"
            >
              {{ recipe.repaired ? '已经修复' : recipe.ready ? '去侦探社修复' : recipe.tip }}
            </button>
          </article>
        </div>

        <div class="safety-note">
          <strong>安全铃铛提醒</strong>
          <span>材料只用于学习任务和游戏化修复。本系统用于中医针灸文化科普，不能替代医生，也不能自行针刺。</span>
        </div>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGameState } from '@/composables/useGameState'
import { generatedRewardAssets } from '@/data/generatedRewardAssets'
import { agencyRepairRecipes, unmetAgencyPrerequisites } from '@/data/agencyRewards'

const router = useRouter()
const { materials, agencyArchiveIds } = useGameState()
const treasureBox = generatedRewardAssets.rewardChestOpen

const basicMaterials = computed(() => materials.value.filter((item) => item.rarity === 'common'))
const rareMaterials = computed(() => materials.value.filter((item) => item.rarity !== 'common'))
const ownedCount = computed(() => materials.value.filter((item) => item.count > 0).length)
const totalCount = computed(() => materials.value.reduce((sum, item) => sum + Number(item.count || 0), 0))

const materialMap = computed(() => {
  return materials.value.reduce((acc, item) => {
    acc[item.id] = item
    return acc
  }, {})
})

const exchangeRecipes = computed(() => {
  return agencyRepairRecipes.map((recipe) => {
    const needs = recipe.costs.map((need) => {
      const material = materialMap.value[need.id]
      return {
        ...need,
        name: material?.name || need.id,
        owned: material?.count || 0
      }
    })
    const repaired = agencyArchiveIds.value.includes(recipe.id)
    const missingPrerequisites = unmetAgencyPrerequisites(recipe, agencyArchiveIds.value)
    const unlocked = missingPrerequisites.length === 0
    const ready = !repaired && unlocked && needs.every((need) => need.owned >= need.count)
    const lacking = needs
      .filter((need) => need.owned < need.count)
      .map((need) => `${need.name}还差 ${need.count - need.owned}`)
      .join('，')
    return {
      ...recipe,
      name: recipe.decorationName,
      needs,
      repaired,
      unlocked,
      ready,
      tip: unlocked ? (lacking || '继续探险') : '先完成前一项修复'
    }
  })
})

const exchangeableCount = computed(() => exchangeRecipes.value.filter((recipe) => recipe.ready).length)

function rarityText(rarity) {
  const map = {
    common: '基础材料',
    rare: '稀有材料',
    epic: '珍贵材料',
    legendary: '传说材料'
  }
  return map[rarity] || '材料'
}

function goAgency(itemId) {
  router.push({ path: '/agency', query: { item: itemId } })
}
</script>

<style scoped>
.bag-page {
  --cream: #fff8e8;
  --paper: #fffdf3;
  --green: #2f7d68;
  --deep-green: #24584b;
  --mint: #dff2d6;
  --gold: #ffd35a;
  --bronze: #b8863b;
  --soft-text: #65766f;
  min-height: calc(100vh - 64px);
  padding: 34px 0 56px;
  color: #263f37;
  background:
    radial-gradient(circle at 10% 8%, rgba(255, 211, 90, 0.2), transparent 24%),
    linear-gradient(180deg, #f6ffe6 0%, var(--cream) 48%, #fffaf0 100%);
}

.bag-hero,
.bag-layout {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
}

.reward-center-link {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  margin-top: 18px;
  padding: 11px 18px;
  color: #fff;
  font-weight: 800;
  text-decoration: none;
  background: var(--green);
  border: 2px solid rgba(255, 255, 255, 0.78);
  border-radius: 999px;
  box-shadow: 0 7px 0 var(--deep-green);
  transition: transform 160ms ease, box-shadow 160ms ease;
}

.reward-center-link:hover {
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 9px 0 var(--deep-green);
}

.bag-hero {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 28px;
  align-items: center;
  min-height: 310px;
  padding: 34px 42px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(255, 253, 243, 0.96), rgba(223, 242, 214, 0.76)),
    radial-gradient(circle at 82% 20%, rgba(255, 211, 90, 0.34), transparent 28%);
  border: 1px solid rgba(184, 134, 59, 0.16);
  border-radius: 8px;
  box-shadow:
    inset 0 0 0 8px rgba(255, 248, 232, 0.45),
    0 24px 48px rgba(46, 83, 63, 0.12);
}

.bag-hero::before {
  content: "";
  position: absolute;
  inset: 14px;
  pointer-events: none;
  border: 1px dashed rgba(184, 134, 59, 0.22);
  border-radius: 8px;
}

.bag-hero__copy {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.eyebrow,
.section-head p,
.exchange-panel__head p {
  margin: 0 0 10px;
  color: var(--bronze);
  font-size: 15px;
  font-weight: 900;
}

.bag-hero h1 {
  max-width: 720px;
  margin: 0;
  color: var(--deep-green);
  font-size: clamp(34px, 5vw, 60px);
  font-weight: 950;
  line-height: 1.08;
  text-shadow: 0 4px 0 rgba(255, 243, 189, 0.82);
}

.lead {
  max-width: 690px;
  margin: 18px 0 0;
  color: #50665d;
  font-size: 17px;
  line-height: 1.8;
}

.bag-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}

.bag-stats span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 13px;
  color: #24584b;
  font-weight: 900;
  background: rgba(255, 253, 243, 0.82);
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 999px;
}

.bag-stats b {
  color: #8a5d1d;
  font-size: 20px;
}

.bag-hero__pouch {
  position: relative;
  z-index: 1;
  min-height: 230px;
}

.bag-hero__pouch img {
  position: absolute;
  right: 0;
  bottom: -4px;
  width: min(290px, 100%);
  filter: drop-shadow(0 22px 24px rgba(91, 61, 24, 0.2));
}

.spark {
  position: absolute;
  width: 12px;
  height: 12px;
  background: var(--gold);
  border: 2px solid rgba(255, 253, 243, 0.95);
  border-radius: 50%;
  box-shadow: 0 0 18px rgba(255, 211, 90, 0.92);
  animation: sparkle 2.2s ease-in-out infinite;
}

.spark--one {
  top: 34px;
  right: 64px;
}

.spark--two {
  top: 92px;
  right: 244px;
  animation-delay: 0.3s;
}

.spark--three {
  right: 30px;
  bottom: 76px;
  animation-delay: 0.6s;
}

.bag-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  gap: 22px;
  align-items: start;
  margin-top: 24px;
}

.materials-zone,
.exchange-panel {
  background: rgba(255, 253, 243, 0.84);
  border: 2px solid rgba(47, 125, 104, 0.12);
  border-radius: 8px;
  box-shadow: 0 16px 34px rgba(46, 83, 63, 0.1);
}

.materials-zone {
  padding: 24px;
}

.section-head {
  margin-bottom: 16px;
}

.section-head--rare {
  margin-top: 30px;
}

.section-head h2,
.exchange-panel__head h2 {
  margin: 0;
  color: var(--deep-green);
  font-size: 26px;
  font-weight: 950;
}

.section-head span {
  display: block;
  margin-top: 8px;
  color: var(--soft-text);
}

.material-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.material-card {
  position: relative;
  display: grid;
  gap: 12px;
  min-height: 286px;
  padding: 16px;
  overflow: hidden;
  background:
    radial-gradient(circle at 90% 12%, rgba(255, 211, 90, 0.2), transparent 26%),
    linear-gradient(180deg, #fffdf3 0%, #fff8e8 100%);
  border: 2px solid rgba(214, 166, 91, 0.18);
  border-radius: 8px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.material-card:hover {
  box-shadow: 0 16px 26px rgba(36, 88, 75, 0.13);
  transform: translateY(-3px);
}

.material-card--rare,
.material-card--epic,
.material-card--legendary {
  border-color: rgba(47, 125, 104, 0.22);
  background:
    radial-gradient(circle at 88% 10%, rgba(223, 242, 214, 0.62), transparent 26%),
    linear-gradient(180deg, #fffdf3 0%, #eef8e8 100%);
}

.material-card.is-locked {
  filter: saturate(0.72);
}

.material-card.is-locked::after {
  content: "未解锁";
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 9px;
  color: #fff8e8;
  font-size: 12px;
  font-weight: 900;
  background: rgba(36, 88, 75, 0.74);
  border-radius: 999px;
}

.material-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.material-card__icon {
  display: grid;
  width: 76px;
  height: 76px;
  place-items: center;
  background: #fff3bd;
  border: 2px solid rgba(214, 166, 91, 0.18);
  border-radius: 8px;
  box-shadow: inset 0 -5px 0 rgba(184, 134, 59, 0.08);
}

.material-card__icon img {
  width: 58px;
  height: 58px;
  object-fit: contain;
  filter: drop-shadow(0 5px 6px rgba(154, 106, 44, 0.16));
}

.material-card__count {
  padding: 6px 10px;
  color: #684913;
  font-size: 15px;
  font-weight: 950;
  background: rgba(255, 243, 189, 0.92);
  border: 1px solid rgba(154, 106, 44, 0.14);
  border-radius: 999px;
}

.material-card__body p {
  margin: 0 0 4px;
  color: var(--bronze);
  font-size: 12px;
  font-weight: 900;
}

.material-card__body h3 {
  margin: 0;
  color: var(--deep-green);
  font-size: 21px;
  font-weight: 950;
}

.material-card__body span {
  display: block;
  margin-top: 8px;
  color: #566b62;
  line-height: 1.55;
}

.material-card__meta {
  display: grid;
  gap: 8px;
  align-self: end;
}

.material-card__meta div {
  padding: 9px 10px;
  background: rgba(223, 242, 214, 0.54);
  border: 1px solid rgba(47, 125, 104, 0.08);
  border-radius: 8px;
}

.material-card__meta b,
.material-card__meta span {
  display: block;
}

.material-card__meta b {
  color: #24584b;
  font-size: 12px;
}

.material-card__meta span {
  margin-top: 2px;
  color: #607269;
  font-size: 12px;
  line-height: 1.45;
}

.exchange-panel {
  position: sticky;
  top: 88px;
  padding: 20px;
}

.exchange-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.exchange-card {
  padding: 14px;
  background: #fff8e8;
  border: 2px solid rgba(214, 166, 91, 0.16);
  border-radius: 8px;
}

.exchange-card.is-ready {
  border-color: rgba(47, 125, 104, 0.42);
  box-shadow: 0 10px 20px rgba(47, 125, 104, 0.12);
}

.exchange-card__title {
  display: flex;
  gap: 10px;
}

.exchange-card__title > span {
  display: inline-flex;
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  color: #fff8e8;
  font-weight: 950;
  background: #b8863b;
  border-radius: 50%;
}

.exchange-card__title > span img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.exchange-card h3 {
  margin: 0;
  color: var(--deep-green);
  font-size: 17px;
}

.exchange-card p {
  margin: 4px 0 0;
  color: var(--soft-text);
  line-height: 1.5;
}

.recipe-needs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 12px 0;
}

.recipe-needs span {
  padding: 5px 8px;
  color: #7b5a22;
  font-size: 12px;
  font-weight: 900;
  background: #fff3bd;
  border-radius: 999px;
}

.recipe-needs .is-enough {
  color: #24584b;
  background: #dff2d6;
}

.exchange-card button {
  width: 100%;
  min-height: 40px;
  color: #fff8e8;
  font-weight: 950;
  background: var(--green);
  border: 0;
  border-radius: 999px;
  cursor: pointer;
}

.exchange-card button:disabled {
  color: #7b5a22;
  background: rgba(255, 211, 90, 0.42);
  cursor: default;
}

.safety-note {
  display: grid;
  gap: 6px;
  margin-top: 16px;
  padding: 14px;
  color: #24584b;
  background: rgba(223, 242, 214, 0.68);
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 8px;
}

.safety-note strong {
  color: #8a5d1d;
}

.safety-note span {
  line-height: 1.6;
}

@keyframes sparkle {
  0%,
  100% {
    opacity: 0.45;
    transform: scale(0.82);
  }

  50% {
    opacity: 1;
    transform: scale(1.12);
  }
}

@media (max-width: 1100px) {
  .bag-hero,
  .bag-layout {
    width: min(100% - 24px, 1180px);
  }

  .bag-layout {
    grid-template-columns: 1fr;
  }

  .exchange-panel {
    position: static;
  }
}

@media (max-width: 860px) {
  .bag-hero {
    grid-template-columns: 1fr;
    padding: 28px 22px;
  }

  .bag-hero__pouch {
    min-height: 190px;
  }

  .bag-hero__pouch img {
    left: 50%;
    right: auto;
    width: 240px;
    transform: translateX(-50%);
  }

  .material-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .bag-page {
    padding-top: 22px;
  }

  .bag-hero h1 {
    font-size: 32px;
  }

  .materials-zone {
    padding: 16px;
  }

  .material-grid {
    grid-template-columns: 1fr;
  }
}
</style>

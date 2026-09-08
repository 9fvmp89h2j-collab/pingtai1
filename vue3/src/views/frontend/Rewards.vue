<template>
  <main class="rewards-page">
    <section class="reward-hero">
      <div class="reward-hero__copy">
        <p class="eyebrow">小铜人探案社 · 每周奖励</p>
        <h1>星光奖励站</h1>
        <p>学习路上每一次认真观察都会留下星光。集满 5 次，就能自己挑一份最想要的材料包。</p>
        <div class="progress-card" :aria-label="`本周完成 ${weekly.progress} 次，共需 ${weekly.target} 次`">
          <div class="progress-card__top">
            <strong>{{ statusTitle }}</strong>
            <span>{{ weekly.progress }}/{{ weekly.target }} 次</span>
          </div>
          <div class="progress-track"><i :style="{ width: `${progressPercent}%` }" /></div>
          <small>{{ statusHint }}</small>
        </div>
      </div>
      <img class="reward-hero__art" :src="choiceArt" alt="三张闪闪发光的奖励卡" />
    </section>

    <section class="choice-section" aria-labelledby="choice-title">
      <header class="section-title">
        <div>
          <p>本周只选一份</p>
          <h2 id="choice-title">你想带走哪一份？</h2>
        </div>
        <router-link to="/bag">看看我的材料袋 →</router-link>
      </header>

      <div v-if="loading" class="state-card">小铜人正在清点星光……</div>
      <div v-else-if="loadError" class="state-card state-card--error">
        <span>奖励站暂时没打开。</span>
        <button type="button" @click="loadPage">再试一次</button>
      </div>
      <template v-else>
        <div class="choice-grid">
          <button
            v-for="option in optionCards"
            :key="option.itemCode"
            class="choice-card"
            :class="{
              'is-selected': selectedCode === option.itemCode,
              'is-claimed': weekly.claimed && weekly.selectedItemCode === option.itemCode,
              'is-muted': weekly.claimed && weekly.selectedItemCode !== option.itemCode
            }"
            type="button"
            :disabled="weekly.claimed || !weekly.eligible"
            @click="selectedCode = option.itemCode"
          >
            <span class="choice-card__badge">{{ option.kicker }}</span>
            <span class="choice-card__icon"><img :src="option.icon" :alt="option.name" /></span>
            <strong>{{ option.name }}</strong>
            <b>× {{ option.amount }}</b>
            <small>{{ option.description }}</small>
            <i v-if="weekly.claimed && weekly.selectedItemCode === option.itemCode">本周已领取</i>
            <i v-else-if="selectedCode === option.itemCode">已经选中</i>
          </button>
        </div>

        <div class="claim-row">
          <p v-if="!weekly.eligible && !weekly.claimed">还差 <b>{{ remaining }}</b> 次学习星光，继续探案就能选择。</p>
          <p v-else-if="weekly.claimed">选择已经装进材料袋，下周一会出现新的三选一。</p>
          <p v-else>{{ selectedCode ? '再看一眼，确认后本周不能换哦。' : '先点一张你喜欢的奖励卡。' }}</p>
          <button
            type="button"
            :disabled="!selectedCode || !weekly.eligible || claiming"
            @click="claimSelected"
          >
            {{ claiming ? '正在装进材料袋…' : weekly.claimed ? '本周已经领取' : '确认选择这份奖励' }}
          </button>
        </div>
      </template>
    </section>

    <section class="ledger-section" aria-labelledby="ledger-title">
      <header class="section-title">
        <div>
          <p>每一份都有来处</p>
          <h2 id="ledger-title">我的星光记录</h2>
        </div>
        <span class="ledger-count">最近 {{ ledger.length }} 条</span>
      </header>
      <div v-if="!loading && !ledger.length" class="empty-ledger">
        <img :src="rewardFriend" alt="小铜人鼓励你继续学习" />
        <div><strong>第一颗星光正在路上</strong><span>去完成故事、身体地图、经络或安全任务吧。</span></div>
      </div>
      <ol v-else class="ledger-list">
        <li v-for="entry in ledger" :key="entry.id" class="ledger-item" :class="{ 'is-spend': entry.isSpend }">
          <span class="ledger-item__icon">
            <img v-if="entry.icon" :src="entry.icon" :alt="entry.itemName" />
            <b v-else>星</b>
          </span>
          <div class="ledger-item__copy">
            <strong>{{ entry.title }}</strong>
            <span>{{ entry.sourceLabel }} · {{ formatTime(entry.createdAt) }}</span>
          </div>
          <b class="ledger-item__amount">{{ entry.amountText }}</b>
        </li>
      </ol>
    </section>

    <p class="safety-foot">材料只用于文化学习和游戏任务。请只观察、只学习，不自行针刺；身体不舒服要告诉家长并咨询医生。</p>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { claimWeeklyChoiceReward, getRewardLedger, getWeeklyChoiceReward } from '@/api/GameApi'
import { applyServerState } from '@/composables/useGameState'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'
import { materials } from '@/data/materials'

const loading = ref(true)
const loadError = ref(false)
const claiming = ref(false)
const selectedCode = ref('')
const weekly = ref({ progress: 0, target: 5, eligible: false, claimed: false, selectedItemCode: null, options: [] })
const ledger = ref([])
const choiceArt = generatedRewardAssets.rewardChoiceCard
const rewardFriend = generatedRewardAssets.detectiveEncourage

const materialMap = new Map(materials.map((item) => [item.id, item]))
const optionCopy = {
  'bamboo-slip-shard': { kicker: '故事线索包', description: '适合继续修复故事档案和屋顶。' },
  'apricot-kernel': { kicker: '杏林观察包', description: '把侦探社门口装点得更有生机。' },
  'meridian-star-sand': { kicker: '经络星光包', description: '点亮星光墙和经络路线。' }
}
const sourceCopy = {
  TASK: '完成学习任务',
  WEEKLY_CHOICE: '每周三选一',
  AGENCY_EXCHANGE: '侦探社修复',
  CHECKIN: '每日签到',
  SHUNTING: '经络小火车',
  COMPATIBILITY: '历史奖励补记'
}

const progressPercent = computed(() => Math.min(100, Math.round((weekly.value.progress / Math.max(1, weekly.value.target)) * 100)))
const remaining = computed(() => Math.max(0, weekly.value.target - weekly.value.progress))
const statusTitle = computed(() => weekly.value.claimed ? '本周奖励已装袋' : weekly.value.eligible ? '三张奖励卡已点亮！' : '正在收集本周星光')
const statusHint = computed(() => weekly.value.claimed ? '下周一重新开始收集' : weekly.value.eligible ? '现在可以挑一份最需要的材料' : `再完成 ${remaining.value} 次有效学习任务`)

const optionCards = computed(() => {
  const order = ['bamboo-slip-shard', 'apricot-kernel', 'meridian-star-sand']
  const options = new Map((weekly.value.options || []).map((item) => [item.itemCode, item]))
  return order.map((itemCode) => {
    const material = materialMap.get(itemCode) || {}
    return {
      itemCode,
      amount: Number(options.get(itemCode)?.amount || 0),
      name: material.name || itemCode,
      icon: generatedMaterialIcons[itemCode],
      ...optionCopy[itemCode]
    }
  })
})

function decorateLedger(entry) {
  const material = materialMap.get(entry.itemCode)
  const amount = Number(entry.amount || 0)
  const score = Number(entry.scoreDelta || 0)
  const isSpend = amount < 0
  const itemName = material?.name || (score ? '侦探积分' : '星光奖励')
  return {
    ...entry,
    isSpend,
    itemName,
    icon: generatedMaterialIcons[entry.itemCode],
    sourceLabel: sourceCopy[entry.source] || '探案奖励',
    title: isSpend ? `用 ${itemName} 修复了侦探社` : `获得 ${itemName}`,
    amountText: score ? `${score > 0 ? '+' : ''}${score} 分` : `${amount > 0 ? '+' : ''}${amount}`
  }
}

async function loadPage() {
  loading.value = true
  loadError.value = false
  try {
    const [weeklyData, ledgerData] = await Promise.all([
      getWeeklyChoiceReward(),
      getRewardLedger(50)
    ])
    weekly.value = { ...weekly.value, ...(weeklyData || {}) }
    selectedCode.value = weekly.value.claimed ? weekly.value.selectedItemCode : ''
    ledger.value = Array.isArray(ledgerData) ? ledgerData.map(decorateLedger) : []
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function claimSelected() {
  if (!selectedCode.value || !weekly.value.eligible || claiming.value) return
  claiming.value = true
  try {
    const response = await claimWeeklyChoiceReward(selectedCode.value, {
      idempotencyKey: `weekly-choice:${weekly.value.weekKey}:${selectedCode.value}`
    })
    if (response?.state) applyServerState(response.state)
    message.success('选好啦！奖励已经装进材料袋')
    await loadPage()
  } catch (error) {
    message.error(error?.message || '奖励暂时没有装进去，请再试一次')
  } finally {
    claiming.value = false
  }
}

function formatTime(value) {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ')
  return new Intl.DateTimeFormat('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(date)
}

onMounted(loadPage)
</script>

<style scoped>
.rewards-page {
  --ink: #23473d;
  --muted: #668077;
  --green: #237461;
  --deep: #164f43;
  --gold: #f6bd32;
  min-height: calc(100vh - 56px);
  padding: 38px 0 54px;
  color: var(--ink);
  background: radial-gradient(circle at 8% 5%, #fff1a8 0, transparent 22%), linear-gradient(180deg, #eaffdc, #fff8e8 45%, #f7f1df);
}

.reward-hero, .choice-section, .ledger-section, .safety-foot { width: min(1120px, calc(100% - 32px)); margin-inline: auto; }
.reward-hero { display: grid; grid-template-columns: 1.1fr 0.9fr; align-items: center; min-height: 360px; padding: 30px 44px; overflow: hidden; background: #fffdf3; border: 3px solid #e7c779; border-radius: 34px; box-shadow: 0 16px 0 rgba(72, 99, 65, .09); }
.eyebrow, .section-title p { margin: 0 0 7px; color: #b16d20; font-weight: 900; letter-spacing: .08em; }
.reward-hero h1 { margin: 0; font-family: "STKaiti", "KaiTi", serif; font-size: clamp(42px, 6vw, 72px); line-height: 1; color: var(--deep); }
.reward-hero__copy > p:not(.eyebrow) { max-width: 570px; margin: 20px 0; font-size: 18px; line-height: 1.8; }
.reward-hero__art { width: 100%; max-height: 325px; object-fit: contain; filter: drop-shadow(0 18px 16px rgba(90, 60, 20, .14)); }
.progress-card { max-width: 560px; padding: 16px 18px; background: #f3f8df; border: 2px solid #b8d89a; border-radius: 18px; }
.progress-card__top { display: flex; justify-content: space-between; gap: 16px; }
.progress-card__top span { color: #9b651e; font-weight: 900; }
.progress-track { height: 13px; margin: 11px 0 8px; overflow: hidden; background: #dce6cd; border-radius: 999px; }
.progress-track i { display: block; height: 100%; background: linear-gradient(90deg, #55b98b, #ffd14f); border-radius: inherit; transition: width .4s ease; }
.progress-card small { color: var(--muted); font-weight: 700; }

.choice-section, .ledger-section { margin-top: 34px; padding: 30px; background: rgba(255, 253, 243, .96); border: 2px solid #ead7a3; border-radius: 28px; }
.section-title { display: flex; justify-content: space-between; align-items: end; gap: 20px; margin-bottom: 22px; }
.section-title h2 { margin: 0; font-family: "STKaiti", "KaiTi", serif; font-size: 34px; color: var(--deep); }
.section-title a { color: var(--green); font-weight: 800; }
.choice-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.choice-card { position: relative; display: flex; flex-direction: column; align-items: center; min-height: 310px; padding: 18px; color: var(--ink); background: linear-gradient(180deg, #fffdf3, #fff5d6); border: 3px solid #d8bd75; border-radius: 24px; box-shadow: 0 9px 0 #c69c4d; cursor: pointer; transition: transform .18s ease, border-color .18s ease; }
.choice-card:not(:disabled):hover { transform: translateY(-5px); }
.choice-card.is-selected, .choice-card.is-claimed { border-color: #1b8d70; box-shadow: 0 9px 0 #17604f, 0 0 0 5px rgba(65, 190, 145, .18); transform: translateY(-3px); }
.choice-card.is-muted { opacity: .48; }
.choice-card__badge { align-self: flex-start; padding: 5px 10px; color: #8f5a13; font-size: 13px; font-weight: 900; background: #ffe59a; border-radius: 999px; }
.choice-card__icon { display: grid; place-items: center; width: 126px; height: 126px; margin: 9px 0 4px; }
.choice-card__icon img { width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 8px 7px rgba(90, 60, 20, .14)); }
.choice-card strong { font-size: 22px; }
.choice-card > b { color: #bd6c1d; font-size: 24px; }
.choice-card small { margin-top: 5px; color: var(--muted); line-height: 1.55; }
.choice-card i { margin-top: auto; padding-top: 8px; color: var(--green); font-style: normal; font-weight: 900; }
.claim-row { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-top: 27px; padding: 18px 20px; background: #edf5df; border-radius: 18px; }
.claim-row p { margin: 0; color: var(--muted); font-weight: 700; }
.claim-row button, .state-card button { min-height: 46px; padding: 0 22px; color: #fff; font-weight: 900; background: var(--green); border: 0; border-radius: 14px; box-shadow: 0 5px 0 var(--deep); cursor: pointer; }
.claim-row button:disabled { cursor: not-allowed; opacity: .48; box-shadow: none; }
.state-card { padding: 34px; text-align: center; color: var(--muted); font-weight: 800; background: #f7f2df; border-radius: 18px; }
.state-card--error { display: flex; align-items: center; justify-content: center; gap: 20px; }

.ledger-count { color: var(--muted); font-weight: 800; }
.ledger-list { display: grid; gap: 12px; margin: 0; padding: 0; list-style: none; }
.ledger-item { display: grid; grid-template-columns: 58px 1fr auto; align-items: center; gap: 14px; padding: 13px 18px; background: #f4f8e8; border: 1px solid #d7e4c8; border-radius: 17px; }
.ledger-item.is-spend { background: #fff4e3; border-color: #ecd0a2; }
.ledger-item__icon { display: grid; place-items: center; width: 54px; height: 54px; background: #fff; border: 2px solid #e7cd8a; border-radius: 16px; }
.ledger-item__icon img { width: 46px; height: 46px; object-fit: contain; }
.ledger-item__icon b { color: #cc8c18; font-size: 22px; }
.ledger-item__copy { display: flex; flex-direction: column; gap: 3px; }
.ledger-item__copy strong { font-size: 17px; }
.ledger-item__copy span { color: var(--muted); font-size: 13px; }
.ledger-item__amount { color: #168065; font-size: 21px; }
.ledger-item.is-spend .ledger-item__amount { color: #b26426; }
.empty-ledger { display: flex; align-items: center; justify-content: center; gap: 22px; padding: 24px; background: #f4f8e8; border-radius: 20px; }
.empty-ledger img { width: 110px; height: 110px; object-fit: contain; }
.empty-ledger div { display: flex; flex-direction: column; gap: 6px; }
.empty-ledger strong { font-size: 20px; }
.empty-ledger span { color: var(--muted); }
.safety-foot { margin-top: 24px; color: #6d776e; font-size: 13px; line-height: 1.7; text-align: center; }

@media (max-width: 820px) {
  .reward-hero { grid-template-columns: 1fr; padding: 26px; }
  .reward-hero__art { order: -1; max-height: 210px; }
  .choice-grid { grid-template-columns: 1fr; }
  .choice-card { min-height: 250px; }
  .claim-row, .section-title { align-items: stretch; flex-direction: column; }
}

@media (max-width: 520px) {
  .rewards-page { padding-top: 18px; }
  .reward-hero, .choice-section, .ledger-section { width: calc(100% - 20px); padding: 20px; border-radius: 22px; }
  .reward-hero h1 { font-size: 44px; }
  .ledger-item { grid-template-columns: 48px 1fr auto; padding: 10px; }
  .ledger-item__icon { width: 46px; height: 46px; }
  .ledger-item__icon img { width: 40px; height: 40px; }
}

@media (prefers-reduced-motion: reduce) {
  .choice-card, .progress-track i { transition: none; }
}
</style>

<template>
  <main class="sort-page">
    <StarRiverDecoration />
    <div class="sort-stars"></div>

    <section class="sort-hero">
      <button class="sort-back" @click="$router.push('/jingluo')">← 返回经络星河</button>
      <div class="sort-hero-text">
        <h1>穴位排序大师</h1>
        <p>将穴位按正确顺序排列，检验你对经络的掌握程度！</p>
      </div>
      <div class="sort-score-area">
        <div class="sort-score-box">
          <span class="score-label">当前得分</span>
          <span class="score-value">{{ score }}</span>
        </div>
        <div class="sort-round-box">
          <span class="round-label">回合</span>
          <span class="round-value">{{ currentRound }} / {{ totalRounds }}</span>
        </div>
        <div class="sort-timer-box" :class="{ urgent: timeLeft <= 10 }">
          <span class="timer-label">剩余时间</span>
          <span class="timer-value">{{ timeLeft }}s</span>
        </div>
      </div>
    </section>

    <section class="sort-layout" v-if="gameState === 'playing'">
      <div class="sort-meridian-info">
        <div class="meridian-badge" :style="{ background: currentMeridian.color + '22', borderColor: currentMeridian.color }">
          <span class="meridian-color-dot" :style="{ background: currentMeridian.color }"></span>
          <span class="meridian-full-name">{{ currentMeridian.name }}</span>
          <span class="meridian-short-name">{{ currentMeridian.shortName }}</span>
        </div>
        <p class="sort-instruction">点击下方穴位卡片，再点击经络路径上的空槽填入</p>
      </div>

      <div class="sort-path-area">
        <div class="meridian-path-line" :style="{ background: 'linear-gradient(90deg, ' + currentMeridian.color + '44, ' + currentMeridian.color + ', ' + currentMeridian.color + '44)' }"></div>
        <div class="sort-slots">
          <div
            v-for="(slot, idx) in slotContents"
            :key="'slot-' + idx"
            class="sort-slot"
            :class="{
              filled: slot !== null,
              empty: slot === null,
              correct: submitted && slotResults[idx] && slotResults[idx].correct,
              wrong: submitted && slotResults[idx] && !slotResults[idx].correct,
              clickable: !submitted && selectedPool !== null
            }"
            @click="onSlotClick(idx)"
          >
            <span class="slot-number" v-if="!submitted || !slot">{{ idx + 1 }}</span>
            <span class="slot-name" v-if="slot">{{ slot }}</span>
            <span class="slot-result" v-if="submitted && slotResults[idx]">
              {{ slotResults[idx].correct ? '✓' : '✗' }}
            </span>
            <span class="slot-correct-hint" v-if="submitted && slotResults[idx] && !slotResults[idx].correct">
              → {{ slotResults[idx].correctName }}
            </span>
          </div>
        </div>
      </div>

      <div class="sort-pool-area" v-if="!submitted">
        <p class="pool-label">📦 穴位卡片（点击选择，再点击空槽填入）</p>
        <div class="pool-cards">
          <div
            v-for="(name, idx) in availablePool"
            :key="'pool-' + idx"
            class="pool-card"
            :class="{ selected: selectedPool === idx }"
            :style="selectedPool === idx ? { borderColor: currentMeridian.color } : {}"
            @click="onPoolClick(idx)"
          >
            <span class="pool-card-name">{{ name }}</span>
          </div>
          <div class="pool-empty" v-if="availablePool.length === 0 && !submitted">
            <span>所有穴位已填入！</span>
          </div>
        </div>
      </div>

      <div class="sort-actions">
        <button class="sort-btn-hint" @click="showHint" :disabled="hintUsed || submitted">
          💡 提示（{{ hintUsed ? '已用' : '可用' }}）
        </button>
        <button class="sort-btn-clear" @click="clearAll" :disabled="submitted || allSlotsEmpty">
          🔄 清空重填
        </button>
        <button
          class="sort-btn-submit"
          @click="submitAnswer"
          :disabled="submitted || !allSlotsFilled"
        >
          {{ submitted ? '已提交' : allSlotsFilled ? '✓ 提交答案' : '请填入所有穴位' }}
        </button>
        <button class="sort-btn-next" @click="nextRound" v-if="submitted" :disabled="rewardSubmitting">
          {{ currentRound < totalRounds ? '▶ 下一轮' : '🏆 查看结果' }}
        </button>
      </div>
    </section>

    <section class="sort-start" v-if="gameState === 'start'">
      <div class="start-card">
        <span class="start-emoji">📏</span>
        <h2>穴位排序大师</h2>
        <p>将打乱的穴位按经络循行顺序排列正确！</p>
        <div class="start-rules">
          <div class="rule-item">
            <span class="rule-icon">🎯</span>
            <span>共 <strong>{{ totalRounds }}</strong> 轮，每轮一条经络</span>
          </div>
          <div class="rule-item">
            <span class="rule-icon">⏱️</span>
            <span>每轮限时 <strong>60秒</strong></span>
          </div>
          <div class="rule-item">
            <span class="rule-icon">⭐</span>
            <span>每个正确穴位 +<strong>10分</strong>，剩余时间秒数 ×2 加分</span>
          </div>
          <div class="rule-item">
            <span class="rule-icon">🎁</span>
            <span>全部正确额外 +<strong>20分</strong>，累计得分可领奖励</span>
          </div>
        </div>
        <button class="start-btn" @click="startGame">开始挑战</button>
      </div>
    </section>

    <section class="sort-result" v-if="gameState === 'result'">
      <div class="result-card">
        <span class="result-emoji">{{ finalEmoji }}</span>
        <h2>{{ finalTitle }}</h2>
        <p class="result-score">最终得分：<strong>{{ score }}</strong> 分</p>
        <div class="result-detail">
          <div class="result-row" v-for="(r, i) in roundResults" :key="i">
            <span class="result-round-label">第{{ i + 1 }}轮</span>
            <span class="result-meridian-name">{{ r.meridianName }}</span>
            <span class="result-correct-count">{{ r.correctCount }}/{{ r.totalCount }} 正确</span>
            <span class="result-round-score">+{{ r.roundScore }}分</span>
          </div>
        </div>
        <div class="result-rewards" v-if="earnedRewards.length > 0">
          <p class="reward-title">获得奖励：</p>
          <div class="reward-item" v-for="rw in earnedRewards" :key="rw.id">
            <img class="reward-item-icon" :src="rw.icon" :alt="rw.name" />
            <span>{{ rw.name }} ×{{ rw.count }}</span>
          </div>
        </div>
        <div class="result-actions">
          <button class="result-btn-replay" @click="startGame">🔄 再来一局</button>
          <button class="result-btn-back" @click="$router.push('/jingluo')">← 返回经络星河</button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, onUnmounted, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import StarRiverDecoration from '@/components/StarRiverDecoration.vue'
import { useSoundEffects } from '@/composables/useSoundEffects'
import { useUserStore } from '@/store/user'
import { submitGameProgressEvent } from '@/api/GameApi'
import { generatedMaterialIcons } from '@/data/generatedRewardAssets'

const STORAGE_KEY = 'xinglin-game-state-v2'
const CLAIMED_KEY = 'meridian-sort-claimed'
const TOTAL_ROUNDS = 5
const TIME_PER_ROUND = 60
const POINTS_PER_CORRECT = 10
const TIME_BONUS_MULTIPLIER = 2
const PERFECT_BONUS = 20

const MERIDIAN_DATA = [
  { id: 'lung', name: '手太阴肺经', shortName: '肺经', color: '#FF6B6B', acupoints: ['中府', '尺泽', '列缺', '太渊', '少商'] },
  { id: 'large-intestine', name: '手阳明大肠经', shortName: '大肠经', color: '#FFA94D', acupoints: ['商阳', '合谷', '曲池', '迎香'] },
  { id: 'stomach', name: '足阳明胃经', shortName: '胃经', color: '#FFD43B', acupoints: ['四白', '地仓', '天枢', '足三里', '内庭'] },
  { id: 'spleen', name: '足太阴脾经', shortName: '脾经', color: '#69DB7C', acupoints: ['隐白', '三阴交', '阴陵泉', '血海', '大包'] },
  { id: 'heart', name: '手少阴心经', shortName: '心经', color: '#FF8787', acupoints: ['极泉', '少海', '神门', '少冲'] },
  { id: 'small-intestine', name: '手太阳小肠经', shortName: '小肠经', color: '#DA77F2', acupoints: ['少泽', '后溪', '养老', '听宫'] },
  { id: 'bladder', name: '足太阳膀胱经', shortName: '膀胱经', color: '#4DABF7', acupoints: ['睛明', '攒竹', '肾俞', '委中', '至阴'] },
  { id: 'kidney', name: '足少阴肾经', shortName: '肾经', color: '#20C997', acupoints: ['涌泉', '太溪', '照海', '复溜'] },
  { id: 'pericardium', name: '手厥阴心包经', shortName: '心包经', color: '#F783AC', acupoints: ['天池', '曲泽', '内关', '中冲'] },
  { id: 'sanjiao', name: '手少阳三焦经', shortName: '三焦经', color: '#FF922B', acupoints: ['关冲', '中渚', '外关', '翳风', '丝竹空'] },
  { id: 'gallbladder', name: '足少阳胆经', shortName: '胆经', color: '#845EF7', acupoints: ['瞳子髎', '风池', '肩井', '环跳', '阳陵泉', '足临泣'] },
  { id: 'liver', name: '足厥阴肝经', shortName: '肝经', color: '#339AF0', acupoints: ['大敦', '太冲', '曲泉', '期门'] },
  { id: 'ren', name: '任脉', shortName: '任脉', color: '#F06595', acupoints: ['关元', '气海', '神阙', '中脘', '膻中'] },
  { id: 'du', name: '督脉', shortName: '督脉', color: '#E599F7', acupoints: ['长强', '命门', '大椎', '风府', '百会'] }
]

const REWARD_MILESTONES = [
  { score: 50, rewards: [{ id: 'meridian-star-sand', name: '经络星砂', count: 1, icon: generatedMaterialIcons['meridian-star-sand'] }] },
  { score: 100, rewards: [{ id: 'herbal-leaf', name: '草药叶', count: 2, icon: generatedMaterialIcons['herbal-leaf'] }] },
  { score: 150, rewards: [{ id: 'apricot-kernel', name: '杏林叶', count: 1, icon: generatedMaterialIcons['apricot-kernel'] }] },
  { score: 200, rewards: [{ id: 'bamboo-slip-shard', name: '竹简碎片', count: 2, icon: generatedMaterialIcons['bamboo-slip-shard'] }] },
  { score: 250, rewards: [{ id: 'meridian-star-sand', name: '经络星砂', count: 3, icon: generatedMaterialIcons['meridian-star-sand'] }] },
  { score: 300, rewards: [{ id: 'herbal-leaf', name: '草药叶', count: 3, icon: generatedMaterialIcons['herbal-leaf'] }] }
]

const gameState = ref('start')
const score = ref(0)
const currentRound = ref(1)
const totalRounds = ref(TOTAL_ROUNDS)
const timeLeft = ref(TIME_PER_ROUND)
const submitted = ref(false)
const hintUsed = ref(false)
const currentMeridian = ref(MERIDIAN_DATA[0])
const slotContents = ref([])
const availablePool = ref([])
const selectedPool = ref(null)
const slotResults = ref([])
const roundResults = ref([])
const earnedRewards = ref([])
const claimedScores = ref([])
const rewardSubmitting = ref(false)
const userStore = useUserStore()

const sfx = useSoundEffects()

const allSlotsFilled = computed(function() {
  return slotContents.value.every(function(s) { return s !== null })
})

const allSlotsEmpty = computed(function() {
  return slotContents.value.every(function(s) { return s === null })
})

let timerInterval = null
let usedMeridianIds = []

function shuffle(arr) {
  var a = arr.slice()
  for (var i = a.length - 1; i > 0; i--) {
    var j = Math.floor(Math.random() * (i + 1))
    var tmp = a[i]; a[i] = a[j]; a[j] = tmp
  }
  return a
}

function pickMeridian() {
  var available = MERIDIAN_DATA.filter(function(m) {
    return usedMeridianIds.indexOf(m.id) === -1
  })
  if (available.length === 0) {
    usedMeridianIds = []
    available = MERIDIAN_DATA.slice()
  }
  var idx = Math.floor(Math.random() * available.length)
  var m = available[idx]
  usedMeridianIds.push(m.id)
  return m
}

function initRound() {
  currentMeridian.value = pickMeridian()
  var acupoints = currentMeridian.value.acupoints
  var count = acupoints.length

  slotContents.value = new Array(count).fill(null)
  slotResults.value = new Array(count).fill(null)

  var shuffled = shuffle(acupoints)
  while (arraysEqual(shuffled, acupoints) && count > 1) {
    shuffled = shuffle(acupoints)
  }
  availablePool.value = shuffled
  selectedPool.value = null
  submitted.value = false
  hintUsed.value = false
  timeLeft.value = TIME_PER_ROUND
  startTimer()
}

function arraysEqual(a, b) {
  if (a.length !== b.length) return false
  for (var i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false
  }
  return true
}

function startTimer() {
  stopTimer()
  timerInterval = setInterval(function() {
    timeLeft.value--
    if (timeLeft.value <= 10 && timeLeft.value > 0) {
      sfx.warning()
    }
    if (timeLeft.value <= 0) {
      stopTimer()
      if (!submitted.value) {
        void submitAnswer()
        message.warning('时间到！已自动提交')
      }
    }
  }, 1000)
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

function startGame() {
  score.value = 0
  currentRound.value = 1
  roundResults.value = []
  earnedRewards.value = []
  usedMeridianIds = []
  gameState.value = 'playing'
  sfx.pageTransition()
  initRound()
}

async function submitAnswer() {
  if (submitted.value) return
  if (!allSlotsFilled.value) {
    message.warning('请先将所有穴位卡片填入槽位')
    return
  }
  submitted.value = true
  stopTimer()

  var correctOrder = currentMeridian.value.acupoints
  var correctCount = 0
  var results = []

  for (var i = 0; i < slotContents.value.length; i++) {
    var placed = slotContents.value[i]
    var correct = placed === correctOrder[i]
    if (correct) correctCount++
    results.push({
      correct: correct,
      correctName: correct ? '' : correctOrder[i]
    })
  }
  slotResults.value = results

  var roundScore = correctCount * POINTS_PER_CORRECT
  var timeBonus = timeLeft.value * TIME_BONUS_MULTIPLIER
  if (correctCount === correctOrder.length) {
    roundScore += PERFECT_BONUS
  }
  roundScore += timeBonus
  score.value += roundScore

  roundResults.value.push({
    meridianName: currentMeridian.value.name,
    meridianColor: currentMeridian.value.color,
    correctCount: correctCount,
    totalCount: correctOrder.length,
    roundScore: roundScore,
    perfect: correctCount === correctOrder.length
  })

  await checkRewards()

  if (correctCount === correctOrder.length) {
    sfx.success()
  } else if (correctCount > 0) {
    sfx.roundComplete()
  } else {
    sfx.error()
  }
}

async function checkRewards() {
  if (rewardSubmitting.value) return
  rewardSubmitting.value = true
  var newRewards = []
  for (const milestone of REWARD_MILESTONES) {
    if (claimedScores.value.indexOf(milestone.score) !== -1 || score.value < milestone.score) continue
    const taskCode = `meridian-sort-${milestone.score}`
    if (userStore.isLoggedIn && userStore.userId) {
      try {
        const response = await submitGameProgressEvent({
          gameCode: 'meridian-sort',
          eventType: 'TASK_COMPLETED',
          taskCode,
          periodKey: 'lifetime',
          resultCode: 'SORT_MILESTONE',
          progressDelta: 1
        }, { idempotencyKey: `game:${userStore.userId}:${taskCode}` })
        if (response?.state && typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('game-state-refresh', { detail: response.state }))
        }
      } catch (error) {
        message.error(error.message || '奖励提交失败，请稍后重试')
        break
      }
    } else if (!addMaterialsToStorage(milestone.rewards)) {
      message.error('奖励保存失败，请稍后重试')
      break
    }
    claimedScores.value.push(milestone.score)
    persistClaimedScores()
    newRewards = newRewards.concat(milestone.rewards)
  }
  if (newRewards.length > 0) {
    earnedRewards.value = earnedRewards.value.concat(newRewards)
    sfx.collect()
  }
  rewardSubmitting.value = false
}

function addMaterialsToStorage(rewards) {
  if (!Array.isArray(rewards) || rewards.length === 0) return false
  if (userStore.isLoggedIn && userStore.userId) return false
  try {
    var raw = window.localStorage.getItem(STORAGE_KEY)
    var state = raw ? JSON.parse(raw) : {}
    var materialCounts = state.materialCounts || {}
    rewards.forEach(function(reward) {
      if (!reward || !reward.id) return
      materialCounts[reward.id] = Number(materialCounts[reward.id] || 0) + Number(reward.count || 0)
    })
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(Object.assign({}, state, { materialCounts: materialCounts })))
    return true
  } catch (e) {
    console.warn('addMaterials failed', e)
    return false
  }
}

function readClaimedScores() {
  try {
    const raw = window.localStorage.getItem(CLAIMED_KEY)
    return raw ? JSON.parse(raw) : []
  } catch (e) {
    return []
  }
}

function persistClaimedScores() {
  try {
    window.localStorage.setItem(CLAIMED_KEY, JSON.stringify(claimedScores.value))
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  claimedScores.value = readClaimedScores()
  if (userStore.isLoggedIn && userStore.userId) {
    const state = await userStore.loadGameState({ showDefaultMsg: false }).catch(() => null)
    const serverClaims = (state?.tasks || [])
      .filter((task) => /^meridian-sort-(50|100|150|200|250|300)$/.test(task.taskCode) && task.status === 'COMPLETED')
      .map((task) => Number(task.taskCode.split('-').pop()))
    claimedScores.value = Array.from(new Set([...claimedScores.value, ...serverClaims]))
  }
})

function nextRound() {
  if (currentRound.value < totalRounds.value) {
    currentRound.value++
    initRound()
  } else {
    gameState.value = 'result'
    sfx.victory()
  }
}

function showHint() {
  if (hintUsed.value || submitted.value) return
  hintUsed.value = true
  var correctOrder = currentMeridian.value.acupoints

  var firstWrongIdx = -1
  for (var i = 0; i < slotContents.value.length; i++) {
    if (slotContents.value[i] !== correctOrder[i]) {
      firstWrongIdx = i
      break
    }
  }

  if (firstWrongIdx >= 0) {
    var correctName = correctOrder[firstWrongIdx]

    if (slotContents.value[firstWrongIdx] !== null) {
      var oldName = slotContents.value[firstWrongIdx]
      availablePool.value.push(oldName)
    }

    var poolIdx = availablePool.value.indexOf(correctName)
    if (poolIdx >= 0) {
      availablePool.value.splice(poolIdx, 1)
    } else {
      for (var j = 0; j < slotContents.value.length; j++) {
        if (slotContents.value[j] === correctName && j !== firstWrongIdx) {
          slotContents.value[j] = null
          break
        }
      }
    }

    slotContents.value[firstWrongIdx] = correctName
    slotContents.value = slotContents.value.slice()
    selectedPool.value = null
    sfx.hint()
    message.info('已将 "' + correctName + '" 填入第 ' + (firstWrongIdx + 1) + ' 个槽位')
  }
}

const finalEmoji = computed(function() {
  if (score.value >= 250) return '🏆'
  if (score.value >= 150) return '🌟'
  if (score.value >= 80) return '👍'
  return '💪'
})

const finalTitle = computed(function() {
  if (score.value >= 250) return '穴位大师！'
  if (score.value >= 150) return '非常出色！'
  if (score.value >= 80) return '表现不错！'
  return '继续加油！'
})

function onPoolClick(idx) {
  if (submitted.value) return
  if (selectedPool.value === idx) {
    selectedPool.value = null
    return
  }
  selectedPool.value = idx
  sfx.select()
}

function onSlotClick(idx) {
  if (submitted.value) return

  if (slotContents.value[idx] !== null) {
    var name = slotContents.value[idx]
    slotContents.value[idx] = null
    availablePool.value.push(name)
    selectedPool.value = null
    sfx.remove()
    return
  }

  if (selectedPool.value !== null) {
    var poolName = availablePool.value[selectedPool.value]
    availablePool.value.splice(selectedPool.value, 1)
    slotContents.value[idx] = poolName
    selectedPool.value = null
    sfx.place()
  }
}

function clearAll() {
  if (submitted.value) return
  var contents = slotContents.value
  for (var i = 0; i < contents.length; i++) {
    if (contents[i] !== null) {
      availablePool.value.push(contents[i])
      contents[i] = null
    }
  }
  slotContents.value = contents.slice()
  selectedPool.value = null
  sfx.clearAll()
}

onUnmounted(function() {
  stopTimer()
})
</script>

<style scoped>
.sort-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #0a0a1e 0%, #0d0d2b 20%, #0e0e35 45%, #0c0a28 70%, #09091f 100%);
  color: #e0dcc8;
  padding: 20px 24px 40px;
  position: relative;
  overflow-x: hidden;
}

.sort-stars {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  background:
    radial-gradient(1px 1px at 10% 15%, rgba(255,255,255,0.7), transparent),
    radial-gradient(1px 1px at 25% 35%, rgba(255,255,255,0.5), transparent),
    radial-gradient(1.5px 1.5px at 40% 20%, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 55% 45%, rgba(255,255,255,0.4), transparent),
    radial-gradient(1.5px 1.5px at 70% 30%, rgba(255,255,255,0.5), transparent),
    radial-gradient(1px 1px at 85% 50%, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 15% 60%, rgba(255,255,255,0.5), transparent),
    radial-gradient(1.5px 1.5px at 30% 75%, rgba(255,255,255,0.4), transparent),
    radial-gradient(1px 1px at 50% 70%, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 65% 65%, rgba(255,255,255,0.5), transparent),
    radial-gradient(1.5px 1.5px at 80% 80%, rgba(255,255,255,0.4), transparent),
    radial-gradient(1px 1px at 90% 25%, rgba(255,255,255,0.5), transparent);
}

.sort-hero {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  max-width: 800px;
  margin: 0 auto 24px;
  padding: 16px 20px;
  background: rgba(18, 24, 52, 0.6);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 12px;
}

.sort-back {
  background: transparent;
  border: 1px solid rgba(255, 216, 109, 0.3);
  color: #ffd86d;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-back:hover {
  background: rgba(255, 216, 109, 0.1);
  border-color: #ffd86d;
}

.sort-hero-text h1 {
  font-size: 22px;
  color: #ffd86d;
  margin: 0 0 4px;
}

.sort-hero-text p {
  font-size: 13px;
  color: #a8a4c8;
  margin: 0;
}

.sort-score-area {
  display: flex;
  gap: 16px;
  align-items: center;
}

.sort-score-box,
.sort-round-box,
.sort-timer-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 14px;
  background: rgba(255, 216, 109, 0.06);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 8px;
  min-width: 70px;
}

.sort-timer-box.urgent {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.4);
  animation: timerPulse 0.5s infinite;
}

@keyframes timerPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 107, 107, 0.3); }
  50% { box-shadow: 0 0 0 6px rgba(255, 107, 107, 0); }
}

.score-label,
.round-label,
.timer-label {
  font-size: 10px;
  color: #a8a4c8;
}

.score-value {
  font-size: 24px;
  font-weight: 900;
  color: #ffd86d;
}

.round-value {
  font-size: 16px;
  font-weight: 700;
  color: #e0dcc8;
}

.timer-value {
  font-size: 20px;
  font-weight: 900;
  color: #ffd86d;
}

.sort-timer-box.urgent .timer-value {
  color: #ff6b6b;
}

.sort-layout {
  position: relative;
  z-index: 1;
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sort-meridian-info {
  text-align: center;
}

.meridian-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid;
  border-radius: 20px;
  transition: all 0.3s;
}

.meridian-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.meridian-full-name {
  font-size: 18px;
  font-weight: 700;
  color: #e0dcc8;
}

.meridian-short-name {
  font-size: 12px;
  color: #a8a4c8;
  background: rgba(255,255,255,0.08);
  padding: 2px 8px;
  border-radius: 10px;
}

.sort-instruction {
  font-size: 13px;
  color: #a8a4c8;
  margin: 8px 0 0;
}

.sort-path-area {
  position: relative;
  padding: 30px 20px 40px;
  background: rgba(10, 14, 32, 0.5);
  border: 1px solid rgba(255, 216, 109, 0.12);
  border-radius: 16px;
}

.meridian-path-line {
  position: absolute;
  top: 50%;
  left: 40px;
  right: 40px;
  height: 3px;
  border-radius: 2px;
  transform: translateY(-50%);
  opacity: 0.5;
}

.sort-slots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.sort-slot {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: default;
  transition: all 0.25s;
  position: relative;
  gap: 2px;
}

.sort-slot.empty {
  background: rgba(255,255,255,0.03);
  border: 2px dashed rgba(255,255,255,0.2);
  cursor: default;
}

.sort-slot.empty.clickable {
  border-color: rgba(255, 216, 109, 0.4);
  cursor: pointer;
  animation: slotPulse 1.5s infinite;
}

.sort-slot.empty.clickable:hover {
  border-color: #ffd86d;
  background: rgba(255, 216, 109, 0.08);
  transform: scale(1.08);
  box-shadow: 0 0 16px rgba(255, 216, 109, 0.2);
}

@keyframes slotPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 216, 109, 0.15); }
  50% { box-shadow: 0 0 0 8px rgba(255, 216, 109, 0); }
}

.sort-slot.filled {
  background: rgba(255,255,255,0.06);
  border: 2px solid rgba(255,255,255,0.25);
  cursor: pointer;
}

.sort-slot.filled:hover {
  border-color: #ff6b6b;
  background: rgba(255, 107, 107, 0.08);
}

.sort-slot.correct {
  border-color: #69DB7C;
  background: rgba(105, 219, 124, 0.1);
}

.sort-slot.wrong {
  border-color: #ff6b6b;
  background: rgba(255, 107, 107, 0.08);
  animation: shake 0.4s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  75% { transform: translateX(6px); }
}

.slot-number {
  font-size: 12px;
  color: rgba(255,255,255,0.3);
  font-weight: 600;
  position: absolute;
  top: 6px;
  right: 8px;
}

.sort-slot.filled .slot-number {
  display: none;
}

.slot-name {
  font-size: 18px;
  font-weight: 700;
  color: #e0dcc8;
  letter-spacing: 2px;
}

.sort-slot.correct .slot-name {
  color: #69DB7C;
}

.sort-slot.wrong .slot-name {
  color: #ff6b6b;
}

.slot-result {
  font-size: 14px;
  font-weight: 900;
}

.sort-slot.correct .slot-result {
  color: #69DB7C;
}

.sort-slot.wrong .slot-result {
  color: #ff6b6b;
}

.slot-correct-hint {
  font-size: 10px;
  color: #69DB7C;
  position: absolute;
  bottom: -18px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.sort-pool-area {
  background: rgba(10, 14, 32, 0.5);
  border: 1px solid rgba(255, 216, 109, 0.12);
  border-radius: 16px;
  padding: 16px;
}

.pool-label {
  font-size: 13px;
  color: #a8a4c8;
  margin: 0 0 12px;
  text-align: center;
}

.pool-cards {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
  min-height: 60px;
  align-items: center;
}

.pool-card {
  padding: 12px 20px;
  background: rgba(18, 24, 52, 0.8);
  border: 2px solid rgba(255,255,255,0.15);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.pool-card:hover {
  border-color: rgba(255, 216, 109, 0.5);
  background: rgba(18, 24, 52, 0.95);
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.3);
}

.pool-card.selected {
  border-color: #ffd86d;
  background: rgba(255, 216, 109, 0.12);
  transform: translateY(-3px);
  box-shadow: 0 0 20px rgba(255, 216, 109, 0.25);
  animation: cardSelected 0.8s infinite;
}

@keyframes cardSelected {
  0%, 100% { box-shadow: 0 0 12px rgba(255, 216, 109, 0.2); }
  50% { box-shadow: 0 0 24px rgba(255, 216, 109, 0.4); }
}

.pool-card-name {
  font-size: 18px;
  font-weight: 700;
  color: #e0dcc8;
  letter-spacing: 2px;
}

.pool-empty {
  font-size: 14px;
  color: #69DB7C;
}

.sort-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.sort-btn-hint,
.sort-btn-clear,
.sort-btn-submit,
.sort-btn-next {
  padding: 10px 24px;
  font-size: 15px;
  font-weight: 700;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  border: none;
}

.sort-btn-hint {
  background: rgba(255, 255, 255, 0.06);
  color: #a8a4c8;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.sort-btn-hint:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  color: #e0dcc8;
}

.sort-btn-hint:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.sort-btn-clear {
  background: rgba(255, 107, 107, 0.08);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.25);
}

.sort-btn-clear:hover:not(:disabled) {
  background: rgba(255, 107, 107, 0.15);
}

.sort-btn-clear:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.sort-btn-submit {
  background: linear-gradient(135deg, #ffd86d, #ff9f43);
  color: #1a1a2e;
  box-shadow: 0 0 20px rgba(255, 216, 109, 0.2);
}

.sort-btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 0 30px rgba(255, 216, 109, 0.35);
}

.sort-btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sort-btn-next {
  background: rgba(105, 219, 124, 0.15);
  color: #69DB7C;
  border: 1px solid rgba(105, 219, 124, 0.3);
}

.sort-btn-next:hover {
  background: rgba(105, 219, 124, 0.25);
  transform: translateY(-2px);
}

.sort-start {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

.start-card {
  background: rgba(18, 24, 52, 0.8);
  border: 1px solid rgba(255, 216, 109, 0.2);
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  max-width: 460px;
  width: 100%;
}

.start-emoji {
  font-size: 56px;
  display: block;
  margin-bottom: 12px;
}

.start-card h2 {
  font-size: 26px;
  color: #ffd86d;
  margin: 0 0 8px;
}

.start-card > p {
  font-size: 14px;
  color: #a8a4c8;
  margin: 0 0 24px;
}

.start-rules {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 28px;
  text-align: left;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #d0cbb8;
  padding: 8px 12px;
  background: rgba(255,255,255,0.03);
  border-radius: 8px;
}

.rule-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.rule-item strong {
  color: #ffd86d;
}

.start-btn {
  padding: 14px 48px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  background: linear-gradient(135deg, #ffd86d, #ff9f43);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 0 30px rgba(255, 216, 109, 0.25);
}

.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 0 40px rgba(255, 216, 109, 0.4);
}

.sort-result {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

.result-card {
  background: rgba(18, 24, 52, 0.8);
  border: 1px solid rgba(255, 216, 109, 0.2);
  border-radius: 20px;
  padding: 36px;
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.result-emoji {
  font-size: 56px;
  display: block;
  margin-bottom: 8px;
}

.result-card h2 {
  font-size: 26px;
  color: #ffd86d;
  margin: 0 0 8px;
}

.result-score {
  font-size: 16px;
  color: #a8a4c8;
  margin: 0 0 20px;
}

.result-score strong {
  font-size: 32px;
  color: #ffd86d;
  font-weight: 900;
}

.result-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 20px;
}

.result-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(255,255,255,0.03);
  border-radius: 8px;
  font-size: 13px;
}

.result-round-label {
  color: #ffd86d;
  font-weight: 700;
  flex-shrink: 0;
}

.result-meridian-name {
  color: #e0dcc8;
  flex: 1;
  text-align: left;
}

.result-correct-count {
  color: #a8a4c8;
}

.result-round-score {
  color: #ffd86d;
  font-weight: 700;
}

.result-rewards {
  background: rgba(255, 216, 109, 0.06);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 20px;
}

.reward-title {
  font-size: 14px;
  color: #ffd86d;
  margin: 0 0 8px;
}

.reward-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  color: #e0dcc8;
  padding: 4px 0;
}

.reward-item-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.result-btn-replay,
.result-btn-back {
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 700;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  border: none;
}

.result-btn-replay {
  background: linear-gradient(135deg, #ffd86d, #ff9f43);
  color: #1a1a2e;
}

.result-btn-replay:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 20px rgba(255, 216, 109, 0.3);
}

.result-btn-back {
  background: rgba(255,255,255,0.06);
  color: #a8a4c8;
  border: 1px solid rgba(255,255,255,0.15);
}

.result-btn-back:hover {
  background: rgba(255,255,255,0.1);
  color: #e0dcc8;
}

@media (max-width: 640px) {
  .sort-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .sort-score-area {
    width: 100%;
    justify-content: space-around;
  }

  .sort-slot {
    width: 64px;
    height: 64px;
  }

  .slot-name {
    font-size: 15px;
  }

  .pool-card-name {
    font-size: 15px;
  }

  .sort-slots {
    gap: 8px;
  }

  .start-card {
    padding: 24px;
  }
}
</style>

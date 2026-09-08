<template>
  <main class="match-page">
    <StarRiverDecoration />
    <div class="match-stars"></div>

    <section class="match-hero">
      <button class="match-back" @click="$router.push('/jingluo')">← 返回经络星河</button>
      <div class="match-hero-text">
        <h1>经络消消看</h1>
        <p>交换相邻穴位，三个同经络穴位连成一线即可消除得分！</p>
      </div>
      <div class="match-difficulty">
        <span class="diff-label">难度：</span>
        <button
          v-for="d in DIFFICULTY_OPTIONS"
          :key="d.key"
          class="diff-btn"
          :class="{ active: difficulty === d.key }"
          :style="difficulty === d.key ? { borderColor: d.color, color: d.color, background: d.color + '18' } : {}"
          @click="setDifficulty(d.key)"
        >
          {{ d.label }} <span class="diff-desc">{{ d.desc }}</span>
        </button>
      </div>
      <div class="match-score-area">
        <div class="match-score-box">
          <span class="score-label">当前得分</span>
          <span class="score-value">{{ score }}</span>
        </div>
        <div class="match-reward-box">
          <span class="reward-label">奖励进度{{ difficultyRewardMultiplier > 1 ? '（×' + difficultyRewardMultiplier + '）' : '' }}</span>
          <div class="reward-progress-bar">
            <div class="reward-progress-fill" :style="{ width: rewardProgress + '%' }"></div>
          </div>
          <span class="reward-text">{{ nextRewardText }}</span>
        </div>
      </div>
    </section>

    <section class="match-layout">
      <div class="match-sidebar-left">
        <div class="match-inventory">
          <h3>🎒 道具背包</h3>
          <div class="inventory-slot" :class="{ empty: rowClearCount === 0, pulse: rowClearCount > 0 }" @click="useRowClear">
            <span class="item-icon">↔️</span>
            <span class="item-name">行/列清除</span>
            <span class="item-count">×{{ rowClearCount }}</span>
          </div>
          <div class="inventory-slot" :class="{ empty: bombCount === 0, pulse: bombCount > 0 }" @click="useBomb">
            <span class="item-icon">💣</span>
            <span class="item-name">5×5 爆破</span>
            <span class="item-count">×{{ bombCount }}</span>
          </div>
          <div class="inventory-hint" v-if="pendingSpecial">点击棋盘上的穴位使用道具</div>
        </div>

        <div class="match-meridian-progress">
          <h3>📊 经络收集</h3>
          <div class="progress-list">
            <div v-for="m in activeMeridians" :key="m.id" class="progress-item" :class="{ completed: meridianCollected[m.id] }">
              <span class="progress-color" :style="{ background: m.color }"></span>
              <span class="progress-name">{{ m.shortName }}</span>
              <span class="progress-count">{{ meridianLeftCount[m.id] || 0 }}/{{ meridianTotalCount[m.id] || 0 }}</span>
              <div class="progress-mini-bar">
                <div class="progress-mini-fill" :style="{ width: progressPercent(m.id) + '%', background: m.color }"></div>
              </div>
              <span class="progress-done" v-if="meridianCollected[m.id]">✅</span>
            </div>
          </div>
        </div>
      </div>

      <div class="match-board-wrap">
        <div class="match-board">
          <div v-for="(row, ri) in board" :key="ri" class="board-row">
            <div
              v-for="(cell, ci) in row"
              :key="ri + '-' + ci"
              class="board-cell"
              :class="cellClasses(ri, ci, cell)"
              :style="cellStyle(cell)"
              @click="onCellClick(ri, ci)"
            >
              <span v-if="cell && !cell.removing" class="cell-icon">{{ cellIcon(cell) }}</span>
              <span v-if="cell && !cell.removing" class="cell-name">{{ getMeridianShortName(cell.meridianId) }}</span>
            </div>
          </div>
        </div>
        <div class="match-hint" v-if="showShuffleHint">
          <span>😵 没有可消除的组合了，正在重新洗牌...</span>
        </div>
      </div>

      <div class="match-sidebar-right">
        <div class="match-combo">
          <h3>🔥 连击</h3>
          <div class="combo-display" :class="{ active: combo > 1 }">
            <span class="combo-num">{{ combo }}</span>
            <span class="combo-label">连击</span>
          </div>
          <div class="combo-bonus" v-if="combo > 1">+{{ combo - 1 }} 额外分</div>
        </div>

        <div class="match-rules">
          <h3>📋 玩法规则</h3>
          <ul>
            <li>交换相邻穴位，使 <strong>3个</strong> 同经络穴位连成一线消除</li>
            <li><strong>4-5个</strong> 连成一线可获得 <strong>行列清除道具</strong></li>
            <li>集齐一条经络全部穴位可获得 <strong>5×5爆破道具</strong></li>
            <li>每消除一个穴位得 <strong>1分</strong></li>
            <li>累计得分可领取 <strong>材料奖励</strong></li>
          </ul>
        </div>

        <div class="match-actions">
          <a-button class="match-btn-restart" @click="restartGame">重新开始</a-button>
          <a-button class="match-btn-claim" type="primary" @click="claimReward" :loading="claimingReward" :disabled="!canClaimReward || claimingReward">领取奖励</a-button>
        </div>
      </div>
    </section>

    <section class="match-reward-modal" v-if="showRewardModal">
      <div class="reward-modal-card">
        <img class="reward-modal-character" :src="generatedRewardAssets.detectiveReward" alt="小铜人侦探展示奖励" />
        <h2>恭喜！</h2>
        <p>你获得了以下材料奖励：</p>
        <div class="reward-items">
          <div v-for="r in currentReward" :key="r.id" class="reward-item">
            <img class="reward-item-icon" :src="r.icon" :alt="r.name" />
            <span class="reward-item-name">{{ r.name }}</span>
            <span class="reward-item-count">×{{ r.count }}</span>
          </div>
        </div>
        <a-button type="primary" @click="showRewardModal = false">继续游戏</a-button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import StarRiverDecoration from '@/components/StarRiverDecoration.vue'
import { useSoundEffects } from '@/composables/useSoundEffects'
import { useUserStore } from '@/store/user'
import { submitGameProgressEvent } from '@/api/GameApi'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'

const STORAGE_KEY = 'xinglin-game-state-v2'
const BOARD_SIZE = 10
const MIN_MATCH = 3
const PENDING_KEY = 'meridian-match-score'
const CLAIMED_KEY = 'meridian-match-claimed'

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

const DIFFICULTY_KEY = 'meridian-match-difficulty'

const DIFFICULTY_LEVELS = {
  easy: {
    label: '简单',
    desc: '6条经络',
    meridianIds: ['lung', 'large-intestine', 'stomach', 'spleen', 'heart', 'kidney'],
    rewardMultiplier: 1,
    color: '#69DB7C'
  },
  normal: {
    label: '普通',
    desc: '10条经络',
    meridianIds: ['lung', 'large-intestine', 'stomach', 'spleen', 'heart', 'small-intestine', 'bladder', 'kidney', 'pericardium', 'liver'],
    rewardMultiplier: 1,
    color: '#FFD43B'
  },
  hard: {
    label: '困难',
    desc: '14条经络',
    meridianIds: ['lung', 'large-intestine', 'stomach', 'spleen', 'heart', 'small-intestine', 'bladder', 'kidney', 'pericardium', 'sanjiao', 'gallbladder', 'liver', 'ren', 'du'],
    rewardMultiplier: 1.5,
    color: '#FF6B6B'
  }
}

const DIFFICULTY_OPTIONS = Object.keys(DIFFICULTY_LEVELS).map(function(k) {
  return Object.assign({ key: k }, DIFFICULTY_LEVELS[k])
})

const BASE_REWARD_MILESTONES = [
  { score: 50, rewards: [{ id: 'meridian-star-sand', name: '经络星砂', count: 2, icon: generatedMaterialIcons['meridian-star-sand'] }] },
  { score: 100, rewards: [{ id: 'herbal-leaf', name: '草药叶', count: 3, icon: generatedMaterialIcons['herbal-leaf'] }] },
  { score: 200, rewards: [{ id: 'apricot-kernel', name: '杏林叶', count: 2, icon: generatedMaterialIcons['apricot-kernel'] }] },
  { score: 350, rewards: [{ id: 'bamboo-slip-shard', name: '竹简碎片', count: 2, icon: generatedMaterialIcons['bamboo-slip-shard'] }] },
  { score: 500, rewards: [{ id: 'meridian-star-sand', name: '经络星砂', count: 5, icon: generatedMaterialIcons['meridian-star-sand'] }] },
  { score: 750, rewards: [{ id: 'herbal-leaf', name: '草药叶', count: 5, icon: generatedMaterialIcons['herbal-leaf'] }] },
  { score: 1000, rewards: [{ id: 'apricot-kernel', name: '杏林叶', count: 5, icon: generatedMaterialIcons['apricot-kernel'] }] }
]

const score = ref(0)
const combo = ref(1)
const board = ref([])
const selectedCell = ref(null)
const matchingKeys = ref([])
const processing = ref(false)
const rowClearCount = ref(0)
const bombCount = ref(0)
const pendingSpecial = ref(null)
const meridianLeftCount = ref({})
const meridianTotalCount = ref({})
const meridianCollected = ref({})
const showRewardModal = ref(false)
const currentReward = ref([])
const claimedMilestones = ref([])
const claimingReward = ref(false)
const showShuffleHint = ref(false)
const difficulty = ref('normal')
const userStore = useUserStore()

const sfx = useSoundEffects()

const activeMeridians = computed(function() {
  var ids = DIFFICULTY_LEVELS[difficulty.value].meridianIds
  return MERIDIAN_DATA.filter(function(m) { return ids.indexOf(m.id) !== -1 })
})

const difficultyRewardMultiplier = computed(function() {
  return DIFFICULTY_LEVELS[difficulty.value].rewardMultiplier
})

const rewardMilestones = computed(function() {
  var mult = difficultyRewardMultiplier.value
  return BASE_REWARD_MILESTONES.map(function(m) {
    return {
      score: m.score,
      rewards: m.rewards.map(function(r) {
        return Object.assign({}, r, { count: Math.round(r.count * mult) })
      })
    }
  })
})

const nextReward = computed(() => rewardMilestones.value.find(m => !claimedMilestones.value.includes(m.score)))

const nextRewardText = computed(() => {
  if (!nextReward.value) return '全部已领取！'
  return '再得 ' + (nextReward.value.score - score.value) + ' 分 → ' + nextReward.value.rewards.map(r => r.name).join('、')
})

const rewardProgress = computed(() => {
  if (!nextReward.value) return 100
  var milestones = rewardMilestones.value
  var idx = milestones.findIndex(function(m) { return m.score === nextReward.value.score })
  var prevScore = idx > 0 ? milestones[idx - 1].score : 0
  var range = nextReward.value.score - prevScore
  if (range <= 0) return 0
  return Math.min(100, Math.max(0, ((score.value - prevScore) / range) * 100))
})

const canClaimReward = computed(() => nextReward.value && score.value >= nextReward.value.score)

function progressPercent(id) {
  const total = meridianTotalCount.value[id] || 0
  const left = meridianLeftCount.value[id] || 0
  if (total === 0) return 0
  return Math.min(100, Math.max(0, (left / total) * 100))
}

function cellStyle(cell) {
  if (!cell) return { '--cell-color': '#1a1a3a' }
  return {
    '--cell-color': getMeridianColor(cell.meridianId),
    '--cell-delay': cell.fallDelay || 0
  }
}

function cellClasses(ri, ci, cell) {
  if (!cell) return {}
  return {
    selected: selectedCell.value && selectedCell.value.r === ri && selectedCell.value.c === ci,
    matching: matchingKeys.value.includes(ri + '-' + ci),
    falling: cell.falling,
    'special-row': cell.special === 'row',
    'special-col': cell.special === 'col',
    'special-bomb': cell.special === 'bomb',
    'pending-special': pendingSpecial.value && selectedCell.value && selectedCell.value.r === ri && selectedCell.value.c === ci
  }
}

function cellIcon(cell) {
  if (cell.special === 'bomb') return '💣'
  if (cell.special === 'row') return '↔️'
  if (cell.special === 'col') return '↕️'
  return cell.acupointName || '?'
}

function getMeridianColor(id) { return MERIDIAN_DATA.find(m => m.id === id)?.color || '#888' }
function getMeridianShortName(id) { return MERIDIAN_DATA.find(m => m.id === id)?.shortName || '' }

function randomMeridianId() {
  var meridians = activeMeridians.value
  return meridians[Math.floor(Math.random() * meridians.length)].id
}

function createCell(meridianId, special) {
  var m = MERIDIAN_DATA.find(function(x) { return x.id === meridianId })
  var acupoints = m ? m.acupoints : ['?']
  var acupointName = acupoints[Math.floor(Math.random() * acupoints.length)]
  return { meridianId: meridianId, acupointName: acupointName, special: special || null, removing: false, falling: false, fallDelay: 0 }
}

function initBoard() {
  var newBoard = []
  for (var r = 0; r < BOARD_SIZE; r++) {
    var row = []
    for (var c = 0; c < BOARD_SIZE; c++) {
      var id = randomMeridianId()
      var attempts = 0
      while (wouldMatch(newBoard, row, r, c, id) && attempts < 50) {
        id = randomMeridianId()
        attempts++
      }
      row.push(createCell(id))
    }
    newBoard.push(row)
  }
  board.value = newBoard
  recalcMeridianCounts()
}

function wouldMatch(boardArr, currentRow, r, c, id) {
  if (c >= 2 &&
    currentRow[c - 1] && currentRow[c - 1].meridianId === id &&
    currentRow[c - 2] && currentRow[c - 2].meridianId === id) return true
  if (r >= 2 &&
    boardArr[r - 1] && boardArr[r - 1][c] && boardArr[r - 1][c].meridianId === id &&
    boardArr[r - 2] && boardArr[r - 2][c] && boardArr[r - 2][c].meridianId === id) return true
  return false
}

function recalcMeridianCounts() {
  var counts = {}
  var totals = {}
  var meridians = activeMeridians.value
  meridians.forEach(function(m) {
    counts[m.id] = 0
    totals[m.id] = 0
  })
  var b = board.value
  for (var r = 0; r < BOARD_SIZE; r++) {
    for (var c = 0; c < BOARD_SIZE; c++) {
      var cell = b[r] && b[r][c]
      if (cell && !cell.removing) {
        counts[cell.meridianId] = (counts[cell.meridianId] || 0) + 1
      }
      if (cell) {
        totals[cell.meridianId] = (totals[cell.meridianId] || 0) + 1
      }
    }
  }
  meridianLeftCount.value = Object.assign({}, counts)
  meridianTotalCount.value = Object.assign({}, totals)
}

function onCellClick(r, c) {
  if (processing.value) return
  var cell = board.value[r] && board.value[r][c]
  if (!cell || cell.removing) return

  if (pendingSpecial.value) {
    applySpecial(r, c)
    return
  }

  if (selectedCell.value) {
    var sr = selectedCell.value.r
    var sc = selectedCell.value.c
    if (sr === r && sc === c) {
      selectedCell.value = null
      return
    }
    var isAdjacent = (Math.abs(r - sr) + Math.abs(c - sc)) === 1

    if (isAdjacent) {
      sfx.boardSwap()
      swapCells(sr, sc, r, c)
      var matches = findMatches()
      if (matches.length > 0) {
        selectedCell.value = null
        processMatches(matches)
      } else {
        swapCells(sr, sc, r, c)
        selectedCell.value = null
      }
    } else {
      selectedCell.value = { r: r, c: c }
      sfx.select()
    }
  } else {
    selectedCell.value = { r: r, c: c }
    sfx.select()
  }
}

function swapCells(r1, c1, r2, c2) {
  var tmp = board.value[r1][c1]
  board.value[r1][c1] = board.value[r2][c2]
  board.value[r2][c2] = tmp
}

function findMatches() {
  var b = board.value
  if (!b || b.length === 0) return []
  var matches = new Set()

  for (var r = 0; r < BOARD_SIZE; r++) {
    for (var c = 0; c < BOARD_SIZE; c++) {
      var cell = b[r] && b[r][c]
      if (!cell || cell.removing) continue

      if (c <= BOARD_SIZE - MIN_MATCH) {
        var count = 1
        while (c + count < BOARD_SIZE &&
          b[r][c + count] && b[r][c + count].meridianId === cell.meridianId &&
          !b[r][c + count].removing) count++
        if (count >= MIN_MATCH) {
          for (var i = 0; i < count; i++) matches.add(r + '-' + (c + i))
        }
      }

      if (r <= BOARD_SIZE - MIN_MATCH) {
        var count2 = 1
        while (r + count2 < BOARD_SIZE &&
          b[r + count2] && b[r + count2][c] &&
          b[r + count2][c].meridianId === cell.meridianId &&
          !b[r + count2][c].removing) count2++
        if (count2 >= MIN_MATCH) {
          for (var j = 0; j < count2; j++) matches.add((r + j) + '-' + c)
        }
      }
    }
  }

  return Array.from(matches)
}

function getMatchGroups(matches) {
  var matchSet = new Set(matches)
  var b = board.value
  var groups = []

  for (var r = 0; r < BOARD_SIZE; r++) {
    var c = 0
    while (c < BOARD_SIZE) {
      if (matchSet.has(r + '-' + c)) {
        var group = []
        var cell = b[r] && b[r][c]
        var mid = cell ? cell.meridianId : null
        while (c < BOARD_SIZE && matchSet.has(r + '-' + c) && b[r][c] && b[r][c].meridianId === mid) {
          group.push(r + '-' + c)
          c++
        }
        if (group.length >= MIN_MATCH) groups.push({ type: 'row', cells: group, meridianId: mid, length: group.length })
      } else { c++ }
    }
  }

  for (var col = 0; col < BOARD_SIZE; col++) {
    var row = 0
    while (row < BOARD_SIZE) {
      if (matchSet.has(row + '-' + col)) {
        var group2 = []
        var cell2 = b[row] && b[row][col]
        var mid2 = cell2 ? cell2.meridianId : null
        while (row < BOARD_SIZE && matchSet.has(row + '-' + col) && b[row][col] && b[row][col].meridianId === mid2) {
          group2.push(row + '-' + col)
          row++
        }
        if (group2.length >= MIN_MATCH) groups.push({ type: 'col', cells: group2, meridianId: mid2, length: group2.length })
      } else { row++ }
    }
  }

  return groups
}

async function processMatches(matches) {
  processing.value = true
  matchingKeys.value = matches.slice()
  var b = board.value
  var groups = getMatchGroups(matches)

  await sleep(300)

  var totalEliminated = 0

  for (var gi = 0; gi < groups.length; gi++) {
    var group = groups[gi]
    for (var ki = 0; ki < group.cells.length; ki++) {
      var parts = group.cells[ki].split('-')
      var r = Number(parts[0])
      var c = Number(parts[1])
      if (b[r] && b[r][c] && !b[r][c].removing) {
        b[r][c].removing = true
        totalEliminated++
      }
    }

    if (group.length >= 4) {
      sfx.special()
      var lastParts = group.cells[group.cells.length - 1].split('-')
      var lr = Number(lastParts[0])
      var lc = Number(lastParts[1])
      if (b[lr] && b[lr][lc]) {
        b[lr][lc] = createCell(group.meridianId, group.type === 'row' ? 'row' : 'col')
        totalEliminated--
      }
    }
  }

  var comboBonus = Math.max(0, combo.value - 1)
  score.value += totalEliminated + comboBonus
  combo.value++

  if (combo.value > 2) {
    sfx.comboSound(combo.value)
  } else {
    sfx.match(totalEliminated)
  }

  recalcMeridianCounts()
  checkMeridianCompletion()
  matchingKeys.value = []

  await sleep(200)
  applyGravity()
  fillEmpty()
  await sleep(500)

  var newMatches = findMatches()
  if (newMatches.length > 0) {
    await processMatches(newMatches)
  } else {
    combo.value = 1
    if (!hasValidMovesNow()) {
      showShuffleHint.value = true
      await sleep(500)
      shuffleBoard()
    } else {
      processing.value = false
    }
  }
}

function hasValidMovesNow() {
  var b = board.value
  if (!b || b.length === 0) return false
  for (var r = 0; r < BOARD_SIZE; r++) {
    for (var c = 0; c < BOARD_SIZE; c++) {
      if (c < BOARD_SIZE - 1) {
        swapCells(r, c, r, c + 1)
        var m1 = findMatches()
        swapCells(r, c, r, c + 1)
        if (m1.length > 0) return true
      }
      if (r < BOARD_SIZE - 1) {
        swapCells(r, c, r + 1, c)
        var m2 = findMatches()
        swapCells(r, c, r + 1, c)
        if (m2.length > 0) return true
      }
    }
  }
  return false
}

function checkMeridianCompletion() {
  var b = board.value
  var meridians = activeMeridians.value
  for (var i = 0; i < meridians.length; i++) {
    var m = meridians[i]
    if (meridianCollected.value[m.id]) continue
    var remaining = 0
    for (var r = 0; r < BOARD_SIZE; r++) {
      for (var c = 0; c < BOARD_SIZE; c++) {
        var cell = b[r] && b[r][c]
        if (cell && !cell.removing && cell.meridianId === m.id) remaining++
      }
    }
    if (remaining === 0 && meridianTotalCount.value[m.id] > 0) {
      meridianCollected.value[m.id] = true
      bombCount.value++
      sfx.collect()
      message.success('🎉 集齐' + m.shortName + '全部穴位！获得 5×5 爆破道具！')
    }
  }
}

function applyGravity() {
  var b = board.value
  for (var c = 0; c < BOARD_SIZE; c++) {
    var remaining = []
    for (var r = 0; r < BOARD_SIZE; r++) {
      if (b[r][c] && !b[r][c].removing) {
        remaining.push(Object.assign({}, b[r][c]))
      }
    }
    for (var r2 = 0; r2 < BOARD_SIZE; r2++) {
      var idx = r2 - (BOARD_SIZE - remaining.length)
      if (idx >= 0 && idx < remaining.length) {
        var cell = remaining[idx]
        b[r2][c] = Object.assign({}, cell, { falling: r2 !== idx, fallDelay: r2 * 0.03 })
      } else {
        b[r2][c] = null
      }
    }
  }
  sfx.boardDrop()
}

function fillEmpty() {
  var b = board.value
  for (var c = 0; c < BOARD_SIZE; c++) {
    for (var r = 0; r < BOARD_SIZE; r++) {
      if (!b[r][c]) {
        b[r][c] = createCell(randomMeridianId())
        b[r][c].falling = true
        b[r][c].fallDelay = r * 0.03
      }
    }
  }
  recalcMeridianCounts()
}

function shuffleBoard() {
  var b = board.value
  var cells = []
  for (var r = 0; r < BOARD_SIZE; r++) {
    for (var c = 0; c < BOARD_SIZE; c++) {
      cells.push(b[r][c] ? Object.assign({}, b[r][c]) : null)
    }
  }
  for (var i = cells.length - 1; i > 0; i--) {
    var j = Math.floor(Math.random() * (i + 1));
    var tmp = cells[i]; cells[i] = cells[j]; cells[j] = tmp
  }
  var idx = 0
  for (var r2 = 0; r2 < BOARD_SIZE; r2++) {
    for (var c2 = 0; c2 < BOARD_SIZE; c2++) {
      b[r2][c2] = cells[idx++] || createCell(randomMeridianId())
      if (b[r2][c2]) {
        b[r2][c2].falling = true
        b[r2][c2].fallDelay = (r2 + c2) * 0.02
      }
    }
  }
  recalcMeridianCounts()
  setTimeout(function() {
    for (var r3 = 0; r3 < BOARD_SIZE; r3++) {
      for (var c3 = 0; c3 < BOARD_SIZE; c3++) {
        if (b[r3][c3]) {
          b[r3][c3].falling = false
          b[r3][c3].fallDelay = 0
        }
      }
    }
    showShuffleHint.value = false
    var newMatches = findMatches()
    if (newMatches.length > 0) {
      processMatches(newMatches)
    } else {
      processing.value = false
    }
  }, 400)
}

function useRowClear() {
  if (rowClearCount.value <= 0) return
  pendingSpecial.value = 'row'
  message.info('请点击棋盘上你想清除的一行')
}

function useBomb() {
  if (bombCount.value <= 0) return
  pendingSpecial.value = 'bomb'
  message.info('请点击棋盘上你想爆破的位置')
}

function applySpecial(r, c) {
  var type = pendingSpecial.value
  pendingSpecial.value = null
  processing.value = true
  var b = board.value

  if (type === 'row') {
    rowClearCount.value--
    var eliminated = 0
    for (var cc = 0; cc < BOARD_SIZE; cc++) {
      if (b[r][cc] && !b[r][cc].removing) {
        b[r][cc].removing = true
        eliminated++
      }
    }
    score.value += eliminated
    message.success('清除了 ' + eliminated + ' 个穴位！')
  } else if (type === 'bomb') {
    bombCount.value--
    var eliminated2 = 0
    for (var dr = -2; dr <= 2; dr++) {
      for (var dc = -2; dc <= 2; dc++) {
        var nr = r + dr
        var nc = c + dc
        if (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE && b[nr][nc] && !b[nr][nc].removing) {
          b[nr][nc].removing = true
          eliminated2++
        }
      }
    }
    score.value += eliminated2
    message.success('💣 5×5 爆破！清除了 ' + eliminated2 + ' 个穴位！')
  }

  recalcMeridianCounts()
  checkMeridianCompletion()

  setTimeout(function() {
    applyGravity()
    fillEmpty()
    setTimeout(function() {
      for (var r2 = 0; r2 < BOARD_SIZE; r2++) {
        for (var c2 = 0; c2 < BOARD_SIZE; c2++) {
          if (b[r2][c2]) {
            b[r2][c2].falling = false
            b[r2][c2].fallDelay = 0
          }
        }
      }
      var newMatches = findMatches()
      if (newMatches.length > 0) {
        processMatches(newMatches)
      } else {
        processing.value = false
      }
    }, 400)
  }, 300)
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

function readClaimedMilestones() {
  try {
    const saved = JSON.parse(window.localStorage.getItem(CLAIMED_KEY) || '{}')
    if (Array.isArray(saved)) return saved
    return Array.isArray(saved[difficulty.value]) ? saved[difficulty.value] : []
  } catch (e) {
    return []
  }
}

function persistClaimedMilestones() {
  try {
    const saved = JSON.parse(window.localStorage.getItem(CLAIMED_KEY) || '{}')
    const byDifficulty = Array.isArray(saved) ? {} : saved
    byDifficulty[difficulty.value] = claimedMilestones.value
    window.localStorage.setItem(CLAIMED_KEY, JSON.stringify(byDifficulty))
  } catch (e) { /* ignore */ }
}

async function claimReward() {
  if (!canClaimReward.value || !nextReward.value || claimingReward.value) return
  var milestone = nextReward.value
  var taskCode = `meridian-match-${difficulty.value}-${milestone.score}`
  claimingReward.value = true
  if (userStore.isLoggedIn && userStore.userId) {
    try {
      const response = await submitGameProgressEvent({
        gameCode: 'meridian-match',
        eventType: 'TASK_COMPLETED',
        taskCode: taskCode,
        periodKey: 'lifetime',
        resultCode: 'MATCH_MILESTONE',
        progressDelta: 1
      }, {
        idempotencyKey: `game:${userStore.userId}:${taskCode}`
      })
      if (response?.state && typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('game-state-refresh', { detail: response.state }))
      }
    } catch (error) {
      claimingReward.value = false
      message.error(error.message || '奖励提交失败，请稍后重试')
      return
    }
  } else {
    if (!addMaterialsToStorage(milestone.rewards)) {
      claimingReward.value = false
      message.error('奖励保存失败，请稍后重试')
      return
    }
  }
  claimedMilestones.value = claimedMilestones.value.concat([milestone.score])
  currentReward.value = milestone.rewards.slice()
  showRewardModal.value = true
  persistClaimedMilestones()
  claimingReward.value = false
}

function setDifficulty(level) {
  if (difficulty.value === level) return
  difficulty.value = level
  claimedMilestones.value = readClaimedMilestonesFor(level)
  try {
    window.localStorage.setItem(DIFFICULTY_KEY, level)
  } catch (e) { /* ignore */ }
  restartGame()
  message.success('已切换为' + DIFFICULTY_LEVELS[level].label + '模式')
}

function restartGame() {
  score.value = 0
  combo.value = 1
  rowClearCount.value = 0
  bombCount.value = 0
  pendingSpecial.value = null
  selectedCell.value = null
  matchingKeys.value = []
  processing.value = false
  meridianCollected.value = {}
  showShuffleHint.value = false
  sfx.pageTransition()
  initBoard()
  message.success('游戏已重置')
}

function sleep(ms) {
  return new Promise(function(resolve) { setTimeout(resolve, ms) })
}

function readClaimedMilestonesFor(level) {
  try {
    const saved = JSON.parse(window.localStorage.getItem(CLAIMED_KEY) || '{}')
    if (Array.isArray(saved)) return level === 'normal' ? saved : []
    return Array.isArray(saved[level]) ? saved[level] : []
  } catch (e) {
    return []
  }
}

onMounted(async function() {
  try {
    var savedDiff = window.localStorage.getItem(DIFFICULTY_KEY)
    if (savedDiff && DIFFICULTY_LEVELS[savedDiff]) difficulty.value = savedDiff
    var saved = window.localStorage.getItem(PENDING_KEY)
    if (saved) score.value = parseInt(saved) || 0
    claimedMilestones.value = readClaimedMilestones()
  } catch (e) { /* ignore */ }
  if (userStore.isLoggedIn && userStore.userId) {
    const state = await userStore.loadGameState({ showDefaultMsg: false }).catch(() => null)
    const serverClaims = (state?.tasks || [])
      .filter((task) => task.taskCode?.startsWith(`meridian-match-${difficulty.value}-`) && task.status === 'COMPLETED')
      .map((task) => Number(task.taskCode.split('-').pop()))
    claimedMilestones.value = Array.from(new Set([...claimedMilestones.value, ...serverClaims]))
  }
  initBoard()
})

watch(score, function(val) {
  try { window.localStorage.setItem(PENDING_KEY, String(val)) } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.match-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #0a0a1e 0%, #0d0d2b 20%, #0e0e35 45%, #0c0a28 70%, #09091f 100%);
  color: #e0dcc8;
  padding: 20px 24px 40px;
  position: relative;
  overflow-x: hidden;
}

.match-stars {
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

.match-hero {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto 20px;
  padding: 16px 20px;
  background: rgba(18, 24, 52, 0.6);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 12px;
}

.match-back {
  background: transparent;
  border: 1px solid rgba(255, 216, 109, 0.3);
  color: #ffd86d;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.match-back:hover {
  background: rgba(255, 216, 109, 0.1);
  border-color: #ffd86d;
}

.match-hero-text h1 {
  font-size: 24px;
  color: #ffd86d;
  margin: 0 0 4px;
}

.match-hero-text p {
  font-size: 13px;
  color: #a8a4c8;
  margin: 0;
}

.match-difficulty {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.diff-label {
  font-size: 13px;
  color: #a8a4c8;
  font-weight: 600;
}

.diff-btn {
  padding: 5px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #a8a4c8;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s;
}

.diff-btn:hover {
  background: rgba(255,255,255,0.08);
  border-color: rgba(255,255,255,0.3);
  color: #e0dcc8;
}

.diff-btn.active {
  font-weight: 700;
  box-shadow: 0 0 10px rgba(255, 216, 109, 0.15);
}

.diff-desc {
  font-size: 10px;
  font-weight: 400;
  opacity: 0.7;
  margin-left: 2px;
}

.match-score-area {
  display: flex;
  gap: 20px;
  align-items: center;
}

.match-score-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  background: rgba(255, 216, 109, 0.08);
  border: 1px solid rgba(255, 216, 109, 0.2);
  border-radius: 8px;
}

.score-label {
  font-size: 11px;
  color: #a8a4c8;
}

.score-value {
  font-size: 28px;
  font-weight: 900;
  color: #ffd86d;
  font-variant-numeric: tabular-nums;
}

.match-reward-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reward-label {
  font-size: 11px;
  color: #a8a4c8;
}

.reward-progress-bar {
  width: 180px;
  height: 8px;
  background: rgba(255,255,255,0.1);
  border-radius: 4px;
  overflow: hidden;
}

.reward-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd86d, #ff9f43);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.reward-text {
  font-size: 11px;
  color: #a8a4c8;
}

.match-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 200px 1fr 220px;
  gap: 20px;
  max-width: 1100px;
  margin: 0 auto;
  align-items: start;
}

.match-sidebar-left,
.match-sidebar-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.match-inventory {
  background: rgba(18, 24, 52, 0.6);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 10px;
  padding: 14px;
}

.match-inventory h3 {
  font-size: 14px;
  color: #ffd86d;
  margin: 0 0 10px;
}

.inventory-slot {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 6px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.inventory-slot:hover:not(.empty) {
  background: rgba(255, 216, 109, 0.1);
  border-color: rgba(255, 216, 109, 0.3);
}

.inventory-slot.empty {
  opacity: 0.4;
  cursor: not-allowed;
}

.inventory-slot.pulse {
  animation: slotPulse 2s infinite;
}

@keyframes slotPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 216, 109, 0.3); }
  50% { box-shadow: 0 0 0 4px rgba(255, 216, 109, 0); }
}

.item-icon { font-size: 18px; }
.item-name { font-size: 12px; color: #d0cbb8; flex: 1; }
.item-count { font-size: 13px; font-weight: 700; color: #ffd86d; }

.inventory-hint {
  font-size: 11px;
  color: #ff9f43;
  text-align: center;
  margin-top: 6px;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.match-meridian-progress {
  background: rgba(18, 24, 52, 0.6);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 10px;
  padding: 14px;
}

.match-meridian-progress h3 {
  font-size: 14px;
  color: #ffd86d;
  margin: 0 0 10px;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.progress-item.completed {
  background: rgba(255, 216, 109, 0.08);
}

.progress-color {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.progress-name {
  font-size: 11px;
  color: #c0bca0;
  width: 40px;
  flex-shrink: 0;
}

.progress-count {
  font-size: 10px;
  color: #888;
  width: 30px;
  text-align: right;
}

.progress-mini-bar {
  flex: 1;
  height: 4px;
  background: rgba(255,255,255,0.08);
  border-radius: 2px;
  overflow: hidden;
}

.progress-mini-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.4s ease;
}

.progress-done {
  font-size: 12px;
  flex-shrink: 0;
}

.match-board-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.match-board {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 12px;
  background: rgba(10, 14, 32, 0.7);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 12px;
  box-shadow: 0 0 40px rgba(80, 60, 180, 0.15);
}

.board-row {
  display: flex;
  gap: 3px;
}

.board-cell {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: var(--cell-color, #1a1a3a);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.15s, opacity 0.3s, box-shadow 0.15s;
  position: relative;
  border: 2px solid rgba(255,255,255,0.12);
  user-select: none;
  gap: 1px;
}

.board-cell:hover {
  transform: scale(1.08);
  box-shadow: 0 0 14px rgba(255,255,255,0.2);
  z-index: 2;
}

.board-cell.selected {
  transform: scale(1.1);
  box-shadow: 0 0 0 3px #ffd86d, 0 0 20px rgba(255, 216, 109, 0.5);
  z-index: 3;
}

.board-cell.matching {
  animation: matchFlash 0.5s ease;
  opacity: 0.3;
  transform: scale(0.85);
}

@keyframes matchFlash {
  0% { transform: scale(1); opacity: 1; }
  30% { transform: scale(1.2); opacity: 1; box-shadow: 0 0 20px rgba(255,255,255,0.5); }
  100% { transform: scale(0.85); opacity: 0.3; }
}

.board-cell.falling {
  animation: fallIn 0.4s ease-out both;
  animation-delay: calc(var(--cell-delay, 0) * 1s);
}

@keyframes fallIn {
  from { transform: translateY(-60px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.board-cell.special-row {
  border: 3px solid #ffd86d;
  box-shadow: 0 0 12px rgba(255, 216, 109, 0.5);
}

.board-cell.special-col {
  border: 3px solid #ff9f43;
  box-shadow: 0 0 12px rgba(255, 159, 67, 0.5);
}

.board-cell.special-bomb {
  border: 3px solid #ff6b6b;
  box-shadow: 0 0 12px rgba(255, 107, 107, 0.5);
}

.board-cell.pending-special {
  animation: pendingPulse 0.8s infinite;
}

@keyframes pendingPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 216, 109, 0.6); }
  50% { box-shadow: 0 0 0 6px rgba(255, 216, 109, 0); }
}

.cell-icon {
  font-size: 15px;
  line-height: 1.1;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 0 4px rgba(0,0,0,0.5), 0 1px 2px rgba(0,0,0,0.7);
}

.cell-name {
  font-size: 9px;
  color: rgba(255,255,255,0.75);
  font-weight: 500;
  line-height: 1;
  text-shadow: 0 0 2px rgba(0,0,0,0.4);
}

.match-hint {
  font-size: 13px;
  color: #ff9f43;
  text-align: center;
  padding: 8px;
}

.match-combo {
  background: rgba(18, 24, 52, 0.6);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 10px;
  padding: 14px;
  text-align: center;
}

.match-combo h3 {
  font-size: 14px;
  color: #ffd86d;
  margin: 0 0 8px;
}

.combo-display {
  font-size: 32px;
  font-weight: 900;
  color: #666;
  transition: color 0.3s;
}

.combo-display.active {
  color: #ff9f43;
  animation: comboBounce 0.4s ease;
}

@keyframes comboBounce {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.combo-num { display: block; line-height: 1; }
.combo-label { font-size: 12px; font-weight: 400; color: #888; display: block; margin-top: 2px; }
.combo-bonus { font-size: 12px; color: #ffd86d; margin-top: 4px; }

.match-rules {
  background: rgba(18, 24, 52, 0.6);
  border: 1px solid rgba(255, 216, 109, 0.15);
  border-radius: 10px;
  padding: 14px;
}

.match-rules h3 {
  font-size: 14px;
  color: #ffd86d;
  margin: 0 0 8px;
}

.match-rules ul {
  margin: 0;
  padding-left: 16px;
  font-size: 11px;
  color: #a8a4c8;
  line-height: 1.8;
}

.match-rules strong { color: #ffd86d; }

.match-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.match-btn-restart,
.match-btn-claim {
  width: 100%;
}

.match-reward-modal {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(5, 8, 22, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
}

.reward-modal-card {
  background: linear-gradient(160deg, rgba(20, 28, 58, 0.98), rgba(12, 18, 42, 0.98));
  border: 1px solid rgba(255, 216, 109, 0.3);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  max-width: 400px;
  box-shadow: 0 0 60px rgba(140, 110, 230, 0.2);
}

.reward-modal-character {
  width: 84px;
  height: 84px;
  object-fit: contain;
  display: block;
  margin: 0 auto 12px;
}

.reward-modal-card h2 {
  color: #ffd86d;
  margin: 0 0 8px;
  font-size: 22px;
}

.reward-modal-card p {
  color: #a8a4c8;
  margin: 0 0 16px;
  font-size: 14px;
}

.reward-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(255,255,255,0.04);
  border-radius: 8px;
}

.reward-item-icon { width: 32px; height: 32px; object-fit: contain; }
.reward-item-name { flex: 1; text-align: left; color: #d0cbb8; font-size: 14px; }
.reward-item-count { color: #ffd86d; font-weight: 700; font-size: 16px; }

@media (max-width: 960px) {
  .match-layout {
    grid-template-columns: 1fr;
  }
  .match-sidebar-left,
  .match-sidebar-right {
    flex-direction: row;
    flex-wrap: wrap;
  }
  .board-cell {
    width: 36px;
    height: 36px;
  }
  .cell-icon { font-size: 11px; }
  .cell-name { font-size: 7px; }
}
</style>

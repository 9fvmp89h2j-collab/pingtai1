<template>
  <div class="story-game">
    <header class="game-toolbar">
      <div>
        <span class="game-kicker">{{ caseData.gameType === 'missing-bamboo' ? '观察与推理' : '铜人文化拼图' }}</span>
        <h3>{{ phaseTitle }}</h3>
      </div>
      <div class="toolbar-actions">
        <div class="mode-switch" aria-label="年龄模式">
          <button
            v-for="mode in modes"
            :key="mode.id"
            type="button"
            :class="{ active: ageMode === mode.id }"
            :aria-pressed="ageMode === mode.id"
            @click="setMode(mode.id)"
          >
            {{ mode.label }} <small>{{ mode.ages }}</small>
          </button>
        </div>
        <button type="button" class="voice-btn" :aria-pressed="voiceEnabled" @click="voiceEnabled = !voiceEnabled">
          <i :class="voiceEnabled ? 'fa-solid fa-volume-high' : 'fa-solid fa-volume-xmark'" aria-hidden="true" />
          {{ voiceEnabled ? '旁白开' : '旁白关' }}
        </button>
      </div>
    </header>

    <p class="mode-note">
      {{ currentMode.timed ? '挑战模式记录完成用时，不会超时失败，也不会扣除奖励。' : '启蒙模式不限时，找错和放错都可以继续尝试。' }}
    </p>

    <template v-if="caseData.gameType === 'missing-bamboo'">
      <section v-if="phase === 'spot'" class="game-section">
        <div class="section-heading">
          <div><span>第一关</span><h4>比较两幅现场图</h4><p>在右图点击与左图不同的位置。</p></div>
          <strong>{{ foundDifferences.length }} / {{ requiredDifferences.length }}</strong>
        </div>
        <div class="spot-grid">
          <figure class="scene-frame"><img :src="caseData.spotDifference.beforeImage" alt="整理前的医馆临时素材" /><figcaption>图A · 整理前</figcaption></figure>
          <figure class="scene-frame scene-frame--interactive" @click="recordMiss">
            <img :src="caseData.spotDifference.afterImage" alt="竹简失踪后的医馆临时素材" />
            <button
              v-for="difference in requiredDifferences"
              :key="difference.id"
              type="button"
              class="difference-hotspot"
              :class="{ found: foundDifferences.includes(difference.id), hint: showRangeHint && !foundDifferences.includes(difference.id) }"
              :style="pointStyle(difference.hotspot)"
              :aria-label="foundDifferences.includes(difference.id) ? `已找到：${difference.name}` : '检查这个位置'"
              @click.stop="findDifference(difference)"
            >
              <i :class="foundDifferences.includes(difference.id) ? 'fa-solid fa-check' : 'fa-solid fa-magnifying-glass'" aria-hidden="true" />
            </button>
            <figcaption>图B · 失踪现场</figcaption>
          </figure>
        </div>
        <p class="asset-note"><i class="fa-solid fa-image" aria-hidden="true" /> {{ caseData.spotDifference.assetNote }}</p>
        <div class="found-strip" aria-live="polite">
          <span v-for="item in requiredDifferences" :key="item.id" :class="{ found: foundDifferences.includes(item.id) }">
            <i :class="foundDifferences.includes(item.id) ? 'fa-solid fa-check' : 'fa-solid fa-question'" aria-hidden="true" /> {{ foundDifferences.includes(item.id) ? item.name : '待发现' }}
          </span>
        </div>
        <div class="game-actions"><p>{{ feedback }}</p><button type="button" class="game-primary" :disabled="!spotComplete" @click="goPhase('evidence')">进入线索归位 <i class="fa-solid fa-arrow-right" /></button></div>
      </section>

      <section v-else-if="phase === 'evidence'" class="game-section">
        <div class="section-heading"><div><span>第二关</span><h4>把证据放回故事顺序</h4><p>先选证据，再点“起因、经过、结果”槽位。</p></div><strong>{{ sequence.length }} / 3</strong></div>
        <div class="evidence-layout">
          <div class="evidence-workbench">
            <div class="evidence-pool">
              <button
                v-for="item in caseData.evidenceSequence.main"
                :key="item.id"
                type="button"
                :class="{ selected: selectedEvidence === item.id, used: sequence.includes(item.id) }"
                @click="selectedEvidence = item.id"
              ><i class="fa-solid fa-fingerprint" /> <strong>{{ item.name }}</strong><small>{{ item.description }}</small></button>
            </div>
            <div class="sequence-slots">
              <button v-for="(label, index) in ['起因', '经过', '结果']" :key="label" type="button" :class="{ filled: sequence[index] }" @click="placeEvidence(index)">
                <span>{{ index + 1 }}</span><small>{{ label }}</small><strong>{{ evidenceName(sequence[index]) || '点击放入' }}</strong>
              </button>
            </div>
            <div class="bonus-row">
              <button v-for="bonus in caseData.evidenceSequence.bonus" :key="bonus.id" type="button" :class="{ found: bonusIds.includes(bonus.id) }" @click="toggleBonus(bonus)">
                <i class="fa-solid fa-star" /> {{ bonus.name }}<small>{{ bonusIds.includes(bonus.id) ? bonus.description : '彩蛋知识' }}</small>
              </button>
            </div>
          </div>
          <div class="reveal-board">
            <div class="reveal-image" role="img" :aria-label="`案件图已揭示 ${revealStage}/3`">
              <div v-for="(region, index) in caseData.progressiveReveal.regions" :key="region" class="reveal-band" :class="{ visible: revealStage > index }" :style="revealBandStyle(index)"><span>{{ revealStage > index ? region : `${index + 1}/3` }}</span></div>
            </div>
            <p>已揭示 {{ revealStage }}/3：{{ revealStage ? caseData.progressiveReveal.regions.slice(0, revealStage).join('、') : '等待第一条证据' }}</p>
          </div>
        </div>
        <div class="knowledge-strip"><article v-for="card in caseData.knowledgeCards" :key="card.id"><i class="fa-solid fa-scroll" /><div><strong>{{ card.title }}</strong><p>{{ card.text }}</p></div></article></div>
        <div class="game-actions"><p>{{ feedback }}</p><button type="button" class="game-primary" :disabled="sequence.length < 3" @click="goPhase('wind')">查看风势变化 <i class="fa-solid fa-arrow-right" /></button></div>
      </section>

      <section v-else class="game-section">
        <div class="section-heading"><div><span>第三关</span><h4>排出风的故事</h4><p>依次选择“无风—起风—风停”的三幅图。</p></div><strong>{{ windOrder.length }} / 3</strong></div>
        <div class="wind-slots"><div v-for="(id, index) in [windOrder[0], windOrder[1], windOrder[2]]" :key="index"><span>{{ index + 1 }}</span><strong>{{ windFrame(id)?.label || '待选择' }}</strong></div></div>
        <div class="wind-grid">
          <button v-for="frame in shuffledWindFrames" :key="frame.id" type="button" :disabled="windOrder.includes(frame.id)" @click="chooseWind(frame)">
            <img :src="frame.image" :alt="`${frame.label}场景临时素材`" /><strong>{{ frame.label }}</strong><small>{{ frame.description }}</small>
          </button>
        </div>
        <p class="asset-note"><i class="fa-solid fa-image" /> {{ caseData.evidenceSequence.assetNote }}</p>
        <div class="game-actions"><p>{{ feedback }}</p><button type="button" class="game-primary" :disabled="windOrder.length < 3" @click="submitWind">完成调查 <i class="fa-solid fa-check" /></button></div>
      </section>
    </template>

    <template v-else>
      <section v-if="phase === 'puzzle'" class="game-section copper-game">
        <div class="section-heading"><div><span>第一关</span><h4>拼出完整教学铜人</h4><p>选择任意拼图片，让它自动吸附到铜人轮廓中的正确位置。</p></div><strong>{{ puzzlePieces.length }} / {{ pieceCount }}</strong></div>
        <div class="puzzle-layout">
          <div class="puzzle-board" :style="puzzleGridStyle()"><button v-for="index in pieceCount" :key="index" type="button" disabled :class="{ placed: puzzlePieces.includes(index - 1) }" :style="puzzlePieceStyle(index - 1)"><span>{{ puzzlePieces.includes(index - 1) ? '' : index }}</span></button></div>
          <div class="puzzle-pool" :style="puzzleGridStyle()"><button v-for="index in shuffledPieces" v-show="!puzzlePieces.includes(index)" :key="index" type="button" :style="puzzlePieceStyle(index)" :aria-label="`拼图片 ${index + 1}`" @click="placePuzzlePiece(index)" /></div>
        </div>
        <p class="asset-note"><i class="fa-solid fa-image" /> {{ caseData.puzzle.assetNote }}</p>
        <div class="knowledge-strip"><article v-for="card in caseData.knowledgeCards" :key="card.id"><i class="fa-solid fa-landmark" /><div><strong>{{ card.title }}</strong><p>{{ card.text }}</p></div></article></div>
        <div class="game-actions"><p>{{ feedback }}</p><button type="button" class="game-primary" :disabled="puzzlePieces.length < pieceCount" @click="finishPuzzle">开始穴位星归位 <i class="fa-solid fa-arrow-right" /></button></div>
      </section>

      <section v-else class="game-section copper-game">
        <div class="section-heading"><div><span>第二关</span><h4>把三颗穴位星放回铜人</h4><p>先选择穴位星，再点击铜人上的身体区域。</p></div><strong>{{ acupointIds.length }} / 3</strong></div>
        <div class="acupoint-layout">
          <div class="copper-board">
            <img :src="caseData.puzzle.image" alt="教学铜人临时素材" />
            <button v-for="point in caseData.puzzle.acupoints" :key="point.id" type="button" :class="{ placed: acupointIds.includes(point.id) }" :style="pointStyle(point.target)" :aria-label="`${point.region}目标`" @click="placeAcupoint(point)"><i class="fa-solid fa-location-dot" /><span>{{ point.region }}</span></button>
          </div>
          <div class="acupoint-stars">
            <button v-for="point in caseData.puzzle.acupoints" :key="point.id" type="button" :disabled="acupointIds.includes(point.id)" :class="{ selected: selectedAcupoint === point.id }" :style="{ '--point-color': point.color }" @click="selectedAcupoint = point.id"><i class="fa-solid fa-star" /><strong>{{ point.name }}</strong><small>手太阴肺经 · {{ point.region }}</small></button>
            <article><strong>只认图，不操作</strong><p>穴位是传统医学术语。本游戏只在铜人图上认识名称与大致区域，不在身体上按压或模仿针刺。</p></article>
          </div>
        </div>
        <div class="game-actions"><p>{{ feedback }}</p><button type="button" class="game-primary" :disabled="acupointIds.length < 3" @click="completeGame">完成铜人挑战 <i class="fa-solid fa-check" /></button></div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'

const props = defineProps({ caseData: { type: Object, required: true }, progress: { type: Object, required: true } })
const emit = defineEmits(['progress', 'complete'])

const ageMode = ref(props.progress.ageMode || 'family')
const voiceEnabled = ref((props.progress.ageMode || 'family') === 'family')
const phase = ref(props.progress.interactivePhase || (props.caseData.gameType === 'missing-bamboo' ? 'spot' : 'puzzle'))
const selectedEvidence = ref('')
const selectedAcupoint = ref('')
const feedback = ref('选择一个位置开始调查。')
const missedClicks = ref(Number(props.progress.wrongDifferenceCount || 0))
const bonusIds = ref(Array.isArray(props.progress.bonusEvidenceIds) ? [...props.progress.bonusEvidenceIds] : [])

const modes = computed(() => Object.values(props.caseData.ageModes || {}))
const currentMode = computed(() => props.caseData.ageModes?.[ageMode.value] || modes.value[0] || {})
const foundDifferences = ref(Array.isArray(props.progress.differenceIds) ? [...props.progress.differenceIds] : [])
const sequence = ref(Array.isArray(props.progress.sequenceSlotIds) ? [...props.progress.sequenceSlotIds] : [])
const windOrder = ref(Array.isArray(props.progress.windOrder) ? [...props.progress.windOrder] : [])
const puzzlePieces = ref(Array.isArray(props.progress.puzzlePieceIds) ? [...props.progress.puzzlePieceIds] : [])
const acupointIds = ref(Array.isArray(props.progress.acupointPlacementIds) ? [...props.progress.acupointPlacementIds] : [])
const requiredDifferences = computed(() => props.caseData.spotDifference?.differences.slice(0, currentMode.value.differenceCount || 3) || [])
const spotComplete = computed(() => requiredDifferences.value.every((item) => foundDifferences.value.includes(item.id)))
const revealStage = computed(() => sequence.value.length)
const showRangeHint = computed(() => ageMode.value === 'family' || missedClicks.value >= 3)
const pieceCount = computed(() => currentMode.value.puzzlePieces || 6)
const puzzleColumns = computed(() => 3)
const puzzleRows = computed(() => pieceCount.value / puzzleColumns.value)
const shuffledPieces = computed(() => pieceCount.value === 6 ? [4, 1, 5, 0, 3, 2] : [7, 2, 5, 0, 8, 3, 1, 6, 4])
const shuffledWindFrames = computed(() => {
  const frames = props.caseData.evidenceSequence?.windFrames || []
  return [frames[1], frames[0], frames[2]].filter(Boolean)
})
const phaseTitle = computed(() => ({ spot: '双图找茬', evidence: '线索归位', wind: '风势排序', puzzle: '铜人拼图', acupoints: '穴位星归位' }[phase.value] || '互动调查'))

function speak(text) {
  feedback.value = text
  if (!voiceEnabled.value || !('speechSynthesis' in window)) return
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.9
  window.speechSynthesis.speak(utterance)
}
function persist(patch) { emit('progress', patch) }
function setMode(mode) {
  if (mode === ageMode.value) return
  ageMode.value = mode
  voiceEnabled.value = mode === 'family'
  foundDifferences.value = []
  sequence.value = []
  windOrder.value = []
  puzzlePieces.value = []
  acupointIds.value = []
  phase.value = props.caseData.gameType === 'missing-bamboo' ? 'spot' : 'puzzle'
  persist({ ageMode: mode, differenceIds: [], sequenceSlotIds: [], windOrder: [], puzzlePieceIds: [], acupointPlacementIds: [], interactivePhase: phase.value, interactiveComplete: false, startedAt: Date.now() })
  speak(mode === 'family' ? '已切换亲子启蒙模式。' : '已切换自主挑战模式，计时只记录成绩。')
}
function goPhase(next) { phase.value = next; persist({ interactivePhase: next }); speak(phaseTitle.value) }
function pointStyle(point) { return { left: `${point?.left ?? 50}%`, top: `${point?.top ?? 50}%` } }
function findDifference(item) {
  if (foundDifferences.value.includes(item.id)) return
  foundDifferences.value.push(item.id)
  persist({ differenceIds: [...foundDifferences.value] })
  speak(`找到了，${item.name}。`)
}
function recordMiss() {
  missedClicks.value += 1
  persist({ wrongDifferenceCount: missedClicks.value })
  speak(missedClicks.value >= 3 ? '观察发光的范围提示，再试一次。' : '这里没有变化，再比较一下两幅图。')
}
function evidenceName(id) { return props.caseData.evidenceSequence?.main.find((item) => item.id === id)?.name }
function placeEvidence(index) {
  if (!selectedEvidence.value) return speak('请先选择一条证据。')
  if (index !== sequence.value.length) return speak('请按照起因、经过、结果依次归位。')
  const expected = props.caseData.evidenceSequence.main[index]
  if (selectedEvidence.value !== expected.id) return speak(`这条证据不属于${expected.slot}，再想一想。`)
  sequence.value.push(selectedEvidence.value)
  selectedEvidence.value = ''
  persist({ sequenceSlotIds: [...sequence.value], revealStage: sequence.value.length })
  speak(`归位正确，揭开${sequence.value.length === 1 ? '三分之一' : sequence.value.length === 2 ? '三分之二' : '完整'}画面。`)
}
function toggleBonus(bonus) {
  if (!bonusIds.value.includes(bonus.id)) bonusIds.value.push(bonus.id)
  persist({ bonusEvidenceIds: [...bonusIds.value] })
  speak(bonus.description)
}
function revealBandStyle(index) {
  return { backgroundImage: `url(${props.caseData.progressiveReveal.image})`, backgroundSize: '300% 100%', backgroundPosition: `${index * 50}% center` }
}
function windFrame(id) { return props.caseData.evidenceSequence?.windFrames.find((item) => item.id === id) }
function chooseWind(frame) {
  if (windOrder.value.includes(frame.id)) return
  windOrder.value.push(frame.id)
  persist({ windOrder: [...windOrder.value] })
  speak(`已选择${frame.label}。`)
}
function submitWind() {
  if (windOrder.value.join(',') !== 'still,gust,found') {
    windOrder.value = []
    persist({ windOrder: [] })
    return speak('顺序还不对，想想风是怎样来到又停下的。')
  }
  completeGame()
}
function puzzleGridStyle() { return { gridTemplateColumns: `repeat(${puzzleColumns.value}, 1fr)`, gridTemplateRows: `repeat(${puzzleRows.value}, 1fr)` } }
function puzzlePieceStyle(index) {
  const column = index % puzzleColumns.value
  const row = Math.floor(index / puzzleColumns.value)
  return { backgroundImage: `url(${props.caseData.puzzle.image})`, backgroundSize: `${puzzleColumns.value * 100}% ${puzzleRows.value * 100}%`, backgroundPosition: `${column * (100 / (puzzleColumns.value - 1))}% ${row * (100 / Math.max(1, puzzleRows.value - 1))}%` }
}
function placePuzzlePiece(index) {
  if (puzzlePieces.value.includes(index)) return
  puzzlePieces.value = [...puzzlePieces.value, index]
  speak(puzzlePieces.value.length === pieceCount.value ? '完整铜人拼好啦。' : `第${puzzlePieces.value.length}块归位正确。`)
}
function finishPuzzle() {
  phase.value = 'acupoints'
  persist({ interactivePhase: 'acupoints', puzzlePieceIds: [...puzzlePieces.value] })
  speak('铜人拼图完成，请把三颗穴位星放回大致身体区域。')
}
function placeAcupoint(target) {
  if (!selectedAcupoint.value) return speak('请先选择一颗穴位星。')
  if (selectedAcupoint.value !== target.id) return speak('区域还不匹配，再观察身体区域。')
  if (!acupointIds.value.includes(target.id)) acupointIds.value = [...acupointIds.value, target.id]
  selectedAcupoint.value = ''
  speak(`${target.name}的${target.region}匹配正确。`)
}
function completeGame() {
  window.speechSynthesis?.cancel()
  emit('complete', {
    interactivePhase: 'complete',
    puzzlePieceIds: [...puzzlePieces.value],
    acupointPlacementIds: [...acupointIds.value],
    elapsedSeconds: props.progress.startedAt ? Math.max(1, Math.round((Date.now() - props.progress.startedAt) / 1000)) : 0
  })
}
onBeforeUnmount(() => window.speechSynthesis?.cancel())
</script>

<style scoped>
.story-game { color: #32453b; }
.game-toolbar,.section-heading,.game-actions { display:flex; align-items:center; justify-content:space-between; gap:16px; }
.game-toolbar h3,.section-heading h4 { margin:4px 0 0; color:#224f3d; font-family:"KaiTi","STKaiti",serif; }
.game-toolbar h3 { font-size:25px; }.section-heading h4{font-size:22px}.section-heading p{margin:4px 0 0;color:#718078}
.game-kicker,.section-heading span { color:#ad6e2e; font-size:12px; font-weight:800; }
.toolbar-actions,.mode-switch { display:flex; align-items:center; gap:8px; }
.mode-switch { padding:4px; background:#efe8d8; border-radius:8px; }
.mode-switch button,.voice-btn { border:0; min-height:40px; padding:7px 12px; background:transparent; color:#5e6b64; cursor:pointer; border-radius:6px; font-weight:700; }
.mode-switch button.active { background:#fffdf7; color:#255843; box-shadow:0 2px 7px rgba(70,52,30,.12); }.mode-switch small{display:block;font-size:10px}.voice-btn{border:1px solid #d8caa9;background:#fffaf0}
.mode-note,.asset-note { margin:14px 0; padding:9px 12px; background:#fff7df; border-left:3px solid #d99a35; color:#765c32; font-size:13px; }
.game-section{padding-top:4px}.section-heading>strong{min-width:72px;text-align:center;padding:9px 12px;border:1px solid #d8caa9;border-radius:7px;background:#fffdf8;color:#ad6e2e}
.spot-grid,.wind-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px;margin-top:18px}.scene-frame{position:relative;margin:0;overflow:hidden;border:2px solid #d9c7a1;border-radius:8px;background:#e8ddc8}.scene-frame img{display:block;width:100%;aspect-ratio:8/5;object-fit:cover}.scene-frame figcaption{position:absolute;left:10px;bottom:10px;padding:5px 9px;background:rgba(255,253,247,.92);border-radius:5px;font-weight:800}.difference-hotspot{position:absolute;transform:translate(-50%,-50%);width:44px;height:44px;border:2px dashed #fff6c8;border-radius:50%;background:rgba(168,89,48,.72);color:#fff;cursor:pointer}.difference-hotspot.found{background:#2f7a58;border-style:solid}.difference-hotspot.hint{animation:pulse 1.3s infinite}.found-strip{display:flex;flex-wrap:wrap;gap:8px;margin:12px 0}.found-strip span{padding:7px 10px;border:1px dashed #cdbf9f;border-radius:6px;color:#7c817e}.found-strip span.found{border-style:solid;background:#edf7ef;color:#2f6c50}
.game-actions{margin-top:16px;padding-top:14px;border-top:1px solid #e4dac6}.game-actions p{margin:0;color:#66766e}.game-primary{min-height:44px;padding:10px 16px;border:0;border-radius:7px;background:#b86f2d;color:white;font-weight:800;cursor:pointer}.game-primary:disabled{opacity:.42;cursor:not-allowed}
.evidence-layout,.puzzle-layout,.acupoint-layout{display:grid;grid-template-columns:minmax(0,1.1fr) minmax(260px,.9fr);gap:18px;margin-top:18px}.evidence-pool,.sequence-slots,.bonus-row,.acupoint-stars{display:grid;gap:9px}.evidence-pool{grid-template-columns:repeat(3,1fr)}.evidence-pool button,.bonus-row button,.acupoint-stars button{min-height:74px;padding:10px;border:1px solid #d8caa9;border-radius:7px;background:#fffdf8;color:#32453b;text-align:left;cursor:pointer}.evidence-pool button.selected,.acupoint-stars button.selected{outline:3px solid #d99a35}.evidence-pool button.used{opacity:.48}.evidence-pool small,.bonus-row small,.acupoint-stars small{display:block;margin-top:4px;color:#728078}.sequence-slots{grid-template-columns:repeat(3,1fr);margin-top:12px}.sequence-slots button{min-height:90px;border:2px dashed #cbb991;border-radius:7px;background:#f7f1e4;cursor:pointer}.sequence-slots button.filled{border-style:solid;background:#e9f4e9}.sequence-slots span{display:block}.bonus-row{grid-template-columns:repeat(2,1fr);margin-top:12px}.bonus-row button.found{background:#fff4cd}
.reveal-board{padding:12px;border:1px solid #dacaaa;background:#f5eddf;border-radius:8px}.reveal-image{display:grid;grid-template-columns:repeat(3,1fr);aspect-ratio:16/9;overflow:hidden;background:#d8ccb7;border-radius:6px}.reveal-band{position:relative;filter:sepia(.5) blur(6px);opacity:.2;transition:.7s}.reveal-band.visible{filter:none;opacity:1}.reveal-band span{position:absolute;left:8px;bottom:8px;padding:4px 7px;background:rgba(255,255,255,.85);border-radius:4px;font-size:12px}.reveal-board>p{text-align:center;margin:9px 0 0}.knowledge-strip{display:grid;grid-template-columns:repeat(2,1fr);gap:10px;margin-top:14px}.knowledge-strip article{display:flex;gap:10px;padding:12px;background:#edf4eb;border-left:3px solid #4e8d73}.knowledge-strip p{margin:3px 0 0;font-size:13px;line-height:1.55}
.wind-slots{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin:16px 0}.wind-slots div{padding:10px;text-align:center;border:1px dashed #cab98f;background:#fffaf0}.wind-slots span{display:block;color:#a46a31}.wind-grid{grid-template-columns:repeat(3,1fr)}.wind-grid button{padding:0 0 10px;border:1px solid #d8caa9;background:#fffdf8;border-radius:7px;overflow:hidden;cursor:pointer}.wind-grid button:disabled{opacity:.45}.wind-grid img{display:block;width:100%;aspect-ratio:16/9;object-fit:cover}.wind-grid strong,.wind-grid small{display:block;margin:7px 9px 0}.wind-grid small{color:#718078}
.puzzle-board,.puzzle-pool{display:grid;aspect-ratio:3/2;gap:3px;padding:5px;background:#a47647;border:4px solid #735130;border-radius:8px}.puzzle-board button,.puzzle-pool button{min-width:0;border:1px solid rgba(255,255,255,.65);background-color:#e6dcc7;background-repeat:no-repeat;cursor:pointer}.puzzle-board button:not(.placed){background-image:none!important}.puzzle-board span{display:grid;place-items:center;width:100%;height:100%;font-weight:900;color:#9c8e79}.puzzle-pool button:disabled{opacity:.28}.puzzle-pool span{display:grid;place-items:center;height:100%;color:#fff;background:rgba(42,99,70,.65)}
.copper-board{position:relative;min-height:460px;display:grid;place-items:center;border:1px solid #d8caa9;background:#f2eadc;border-radius:8px;overflow:hidden}.copper-board>img{width:100%;height:460px;object-fit:contain}.copper-board>button{position:absolute;transform:translate(-50%,-50%);min-width:58px;min-height:58px;border:2px dashed #a96b2c;border-radius:50%;background:rgba(255,249,226,.9);color:#8d5726;cursor:pointer}.copper-board>button.placed{background:#347254;color:#fff;border-style:solid}.copper-board>button span{display:block;font-size:10px}.acupoint-stars button i{color:var(--point-color);font-size:20px;margin-right:7px}.acupoint-stars article{padding:14px;background:#fff4d6;border-left:3px solid #c47b32}.acupoint-stars article p{margin:6px 0 0;line-height:1.6}
button:focus-visible{outline:3px solid #1e6f5c;outline-offset:2px}@keyframes pulse{50%{box-shadow:0 0 0 10px rgba(217,154,53,.22)}}
@media(max-width:760px){.game-toolbar,.section-heading,.game-actions{align-items:stretch;flex-direction:column}.toolbar-actions{align-items:stretch;flex-direction:column}.mode-switch button{flex:1}.spot-grid,.evidence-layout,.puzzle-layout,.acupoint-layout{grid-template-columns:1fr}.evidence-pool,.sequence-slots,.wind-grid{grid-template-columns:1fr}.knowledge-strip{grid-template-columns:1fr}.spot-grid{gap:10px}.game-primary{width:100%}.copper-board{min-height:390px}.copper-board>img{height:390px}}
@media(prefers-reduced-motion:reduce){.difference-hotspot.hint{animation:none}.reveal-band{transition:none}}
</style>

<template>
  <div class="lianyan-page">
    <div
      v-if="loading"
      class="lianyan-loading"
    >
      加载中…
    </div>
    <div
      v-else-if="error"
      class="lianyan-loading"
    >
      数据加载失败
    </div>
    <div
      v-else
      class="lianyan-frame"
    >
      <aside class="lianyan-sidebar">
        <button
          v-for="(t, idx) in towns"
          :key="t.illnessid ?? idx"
          type="button"
          class="lianyan-town-btn"
          :class="{ active: activeIllness?.illnessid === t.illnessid }"
          @click="activeIllness = t"
        >
          {{ t.cowtown || `第${idx + 1}个小镇` }}
        </button>
      </aside>

      <section class="lianyan-right">
        <div class="lianyan-intro">
          <span class="prompt-strong">{{ activeIllness?.cowtown || '—' }}</span>
          <span class="prompt-normal">上的居民被</span>
          <span class="prompt-strong">{{ activeIllness?.illnessname || '—' }}</span>
          <span class="prompt-normal">攻击了，快发挥你的聪明才智帮助他们！</span>
        </div>

        <div class="lianyan-row">
          <div class="lianyan-row-left">
            <span class="prompt-normal">居民们</span>
            <span class="prompt-strong">{{ activeIllness?.illnessfeature || '—' }}</span>
          </div>
          <div class="lianyan-row-right">
            <img
              v-if="resolveMediaUrl(activeIllness?.illnesspic)"
              :src="resolveMediaUrl(activeIllness?.illnesspic)"
              class="lianyan-illness-img"
              alt=""
            >
            <div
              v-else
              class="lianyan-illness-img-ph"
            >
              暂无图
            </div>
          </div>
        </div>

        <div class="lianyan-game">
          <div class="lianyan-game-title">
            <span class="prompt-normal">秘箱记载，选取</span>
            <span class="prompt-strong">{{ xueweiNameText || '—' }}</span>
            <span class="prompt-normal">，配合</span>
            <span class="prompt-strong">{{ toolNameText || '—' }}</span>
            <span class="prompt-normal">可以治愈这种疾病！快动手帮助他们吧</span>
          </div>

          <div class="lianyan-board">
            <div class="lianyan-col">
              <div class="lianyan-col-title">
                穴位
              </div>
              <div class="lianyan-slots">
                <button
                  v-for="(s, idx) in xueweiSkills"
                  :key="'x' + idx"
                  type="button"
                  class="lianyan-slot"
                  @click="pickSkill('xuewei', idx)"
                >
                  <img
                    v-if="s && resolveMediaUrl(s.skillPic)"
                    :src="resolveMediaUrl(s.skillPic)"
                    class="lianyan-slot-img"
                    alt=""
                  >
                  <span
                    v-if="s?.skillName"
                    class="lianyan-slot-name"
                  >
                    {{ s.skillName }}
                  </span>
                </button>
              </div>
            </div>

            <div class="lianyan-col">
              <div class="lianyan-col-title">
                手法
              </div>
              <div class="lianyan-slots">
                <button
                  v-for="(s, idx) in toolsSkills"
                  :key="'t' + idx"
                  type="button"
                  class="lianyan-slot"
                  @click="pickSkill('tool', idx)"
                >
                  <img
                    v-if="s && resolveMediaUrl(s.skillPic)"
                    :src="resolveMediaUrl(s.skillPic)"
                    class="lianyan-slot-img"
                    alt=""
                  >
                  <span
                    v-if="s?.skillName"
                    class="lianyan-slot-name"
                  >
                    {{ s.skillName }}
                  </span>
                </button>
              </div>
            </div>
          </div>

          <div class="lianyan-submit-row">
            <button
              type="button"
              class="lianyan-submit"
              @click="submitJudge"
            >
              开始
            </button>
          </div>
        </div>

        <div class="lianyan-ultimate-row">
          <p class="lianyan-ultimate-tip">
            完成小镇挑战之余，可参加<strong>终极考验</strong>，随机10题检验综合知识。
          </p>
          <button
            type="button"
            class="lianyan-ultimate-btn"
            @click="goUltimate"
          >
            前往终极考验
          </button>
        </div>
      </section>
    </div>

    <a-modal
      v-model:open="passModalOpen"
      :footer="null"
      :closable="false"
      width="420px"
      centered
    >
      <div class="lianyan-pass-modal">
        <div class="lianyan-pass-title">
          你真棒
        </div>
        <div class="lianyan-pass-text">
          恭喜通过本关，获得10分！
        </div>
        <button
          type="button"
          class="lianyan-next-btn"
          @click="goNextTown"
        >
          继续前进
        </button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listIllness, listSkillNamesByIds, awardIllnessBadge } from '@/api/AcupunctureApi'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import winMp3 from '@/assets/win.mp3'

const userStore = useUserStore()
const router = useRouter()

function goUltimate() {
  router.push('/ultimate-challenge')
}

const loading = ref(true)
const error = ref(false)
const illnessList = ref([])
const activeIllness = ref(null)

const xueweiSkills = ref([])
const toolsSkills = ref([])
const selecting = ref(null)
const skillNameMap = ref({})
const passModalOpen = ref(false)

const towns = computed(() => (Array.isArray(illnessList.value) ? illnessList.value.slice(0, 8) : []))

function resetBoard() {
  const i = activeIllness.value
  const xRaw = Number(i?.xueweicount)
  const tRaw = Number(i?.toolscount)
  const xCount = Number.isFinite(xRaw) ? xRaw : 0
  const tCount = Number.isFinite(tRaw) ? tRaw : 0
  xueweiSkills.value = Array.from({ length: xCount }, () => null)
  toolsSkills.value = Array.from({ length: tCount }, () => null)
  selecting.value = null
}

const requiredXueweiIds = computed(() => {
  const i = activeIllness.value
  if (!i) return []
  return [i.xuewei1, i.xuewei2, i.xuewei3, i.xuewei4, i.xuewei5]
    .map((x) => Number(x))
    .filter((x) => Number.isFinite(x))
})

const requiredToolIds = computed(() => {
  const i = activeIllness.value
  if (!i) return []
  return [i.tools1, i.tools2, i.tools3, i.tools4]
    .map((x) => Number(x))
    .filter((x) => Number.isFinite(x))
})

const xueweiNameText = computed(() => {
  const names = requiredXueweiIds.value
    .map((id) => skillNameMap.value[id] || String(id))
    .filter((x) => x)
  return names.join(' ')
})

const toolNameText = computed(() => {
  const names = requiredToolIds.value
    .map((id) => skillNameMap.value[id] || String(id))
    .filter((x) => x)
  return names.join(' ')
})

async function loadSkillNames() {
  const ids = [...new Set([...requiredXueweiIds.value, ...requiredToolIds.value])]
  if (!ids.length) {
    skillNameMap.value = {}
    return
  }
  try {
    const data = await listSkillNamesByIds(ids.join(','), { showDefaultMsg: false })
    const m = {}
    for (const it of (Array.isArray(data) ? data : [])) {
      if (it?.skillId != null) {
        m[Number(it.skillId)] = it.skillName || String(it.skillId)
      }
    }
    skillNameMap.value = m
  } catch {
    skillNameMap.value = {}
  }
}

function pickSkill(type, index) {
  selecting.value = { type, index }
  window.dispatchEvent(new Event('backpack-open'))
}

function onBackpackSkillSelected(e) {
  if (!selecting.value) return
  const skill = e?.detail
  if (!skill) return
  const { type, index } = selecting.value
  if (type === 'xuewei') {
    xueweiSkills.value[index] = skill
  } else {
    toolsSkills.value[index] = skill
  }
  selecting.value = null
  window.dispatchEvent(new Event('backpack-close'))
}

function onBackpackClosed() {
  selecting.value = null
}

function playSfx() {
  try {
    const audio = new Audio(winMp3)
    audio.currentTime = 0
    audio.play()
  } catch {}
}

async function load() {
  loading.value = true
  error.value = false
  try {
    const data = await listIllness({}, { showDefaultMsg: false })
    illnessList.value = Array.isArray(data) ? data : []
    activeIllness.value = towns.value[0] || null
  } catch (e) {
    console.error(e)
    error.value = true
    illnessList.value = []
    activeIllness.value = null
  } finally {
    loading.value = false
  }
}

async function submitJudge() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再提交')
    return
  }
  if (!activeIllness.value) return
  if (xueweiSkills.value.some((s) => !s) || toolsSkills.value.some((s) => !s)) {
    message.warning('请先完成所有空格选择')
    return
  }

  const selectedXIds = xueweiSkills.value.map((s) => Number(s?.skillId)).filter((x) => x != null)
  const selectedTIds = toolsSkills.value.map((s) => Number(s?.skillId)).filter((x) => x != null)

  const requiredX = requiredXueweiIds.value
  const requiredT = requiredToolIds.value

  let okX = false
  if (selectedXIds.length === requiredX.length) {
    const sx = new Set(selectedXIds)
    if (sx.size === requiredX.length) {
      okX = requiredX.every((id) => sx.has(id))
    }
  }

  let okT = false
  if (selectedTIds.length === requiredT.length) {
    const st = new Set(selectedTIds)
    if (st.size === requiredT.length) {
      okT = requiredT.every((id) => st.has(id))
    }
  }

  if (okX && okT) {
    await userStore.updateScore(10, '历练得分', { showDefaultMsg: false })
    let names = []
    try {
      names = await awardIllnessBadge(Number(activeIllness.value?.illnessid), { showDefaultMsg: false })
    } catch (e) {
      console.error(e)
      message.error(e?.message || e?.msg || '徽章发放接口调用失败')
      names = []
    }
    for (const n of (Array.isArray(names) ? names : [])) {
      message.success(`恭喜你达成成绩获得'${n}'`)
    }
    playSfx()
    passModalOpen.value = true
  } else {
    playSfx()
    message.error('未通过本关，请再试试')
  }
}

function goNextTown() {
  passModalOpen.value = false
  const currentId = activeIllness.value?.illnessid
  const idx = towns.value.findIndex((x) => x.illnessid === currentId)
  const next = idx >= 0 && idx < towns.value.length - 1 ? towns.value[idx + 1] : towns.value[0]
  if (next) activeIllness.value = next
}

watch(activeIllness, () => {
  resetBoard()
  loadSkillNames()
})

onMounted(() => {
  load()
  window.addEventListener('backpack-skill-selected', onBackpackSkillSelected)
  window.addEventListener('backpack-close', onBackpackClosed)
})

onUnmounted(() => {
  window.removeEventListener('backpack-skill-selected', onBackpackSkillSelected)
  window.removeEventListener('backpack-close', onBackpackClosed)
})
</script>

<style scoped>
.lianyan-page {
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  background: #f6e8d3;
}

.lianyan-loading {
  text-align: center;
  padding: 60px 16px;
  color: #666;
}

.lianyan-frame {
  max-width: 1480px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
  padding: 28px;
  box-sizing: border-box;
  background: #f6e8d3;
  border: 2px solid rgba(0, 0, 0, 0.85);
  border-radius: 0;
  display: flex;
  gap: 18px;
}

.lianyan-sidebar {
  width: 190px;
  padding: 12px 6px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lianyan-town-btn {
  padding: 12px 10px;
  border: 1px solid rgba(0, 0, 0, 0.75);
  background: #fff;
  cursor: pointer;
  text-align: left;
  font-size: 14px;
  border-radius: 0;
}

.lianyan-town-btn.active {
  background: #f0f5ff;
  border-color: #3b82f6;
}

.lianyan-right {
  flex: 1;
  padding: 12px 10px 18px;
  box-sizing: border-box;
}

.lianyan-intro {
  font-size: 28px;
  margin: 6px 0 18px;
  line-height: 1.35;
  font-weight: 400;
}

.lianyan-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.lianyan-row-left {
  font-size: 28px;
  font-weight: 400;
  color: #000;
  padding-left: 20px;
}

.lianyan-row-right {
  width: 240px;
  padding-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lianyan-illness-img {
  width: 180px;
  height: 120px;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.35);
  background: #f3f4f6;
}

.lianyan-illness-img-ph {
  width: 180px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #d1d5db;
  color: #6b7280;
  font-size: 14px;
  border: 1px solid rgba(0, 0, 0, 0.35);
}

.lianyan-game {
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-top: 14px;
}

.lianyan-game-title {
  font-size: 20px;
  margin: 6px 0 24px;
  line-height: 1.35;
  padding-left: 6px;
  font-weight: 400;
}

.prompt-normal {
  font-weight: 400;
}

.prompt-strong {
  font-weight: 700;
}

.lianyan-board {
  display: flex;
  gap: 60px;
  padding-left: 30px;
}

.lianyan-col {
  flex: 1;
}

.lianyan-col-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #000;
}

.lianyan-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.lianyan-slot {
  width: 72px;
  min-height: 92px;
  border: 1px solid rgba(0, 0, 0, 0.35);
  background: #9ca3af;
  padding: 6px 4px 4px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  border-radius: 0;
  box-sizing: border-box;
}

.lianyan-slot-img {
  width: 52px;
  height: 52px;
  object-fit: cover;
  flex-shrink: 0;
}

.lianyan-slot-name {
  font-size: 12px;
  line-height: 1.2;
  color: #111;
  text-align: center;
  word-break: break-word;
}

.lianyan-submit-row {
  display: flex;
  justify-content: flex-end;
  padding-right: 30px;
  margin-top: 26px;
}

.lianyan-submit {
  width: 120px;
  height: 44px;
  border: none;
  background: #1d4ed8;
  color: #fff;
  cursor: pointer;
  font-size: 16px;
  border-radius: 0;
}

.lianyan-ultimate-row {
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid rgba(0, 0, 0, 0.12);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px 24px;
}

.lianyan-ultimate-tip {
  margin: 0;
  flex: 1;
  min-width: 220px;
  font-size: 18px;
  line-height: 1.45;
  color: #111;
}

.lianyan-ultimate-btn {
  border: 1px solid rgba(0, 0, 0, 0.75);
  background: #fff;
  padding: 10px 22px;
  font-size: 16px;
  cursor: pointer;
  color: #111;
  white-space: nowrap;
}

.lianyan-pass-modal {
  text-align: center;
  padding: 14px 8px 8px;
}

.lianyan-pass-title {
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 10px;
}

.lianyan-pass-text {
  font-size: 18px;
  margin-bottom: 20px;
}

.lianyan-next-btn {
  width: 140px;
  height: 42px;
  border: 1px solid #1d4ed8;
  background: #1d4ed8;
  color: #fff;
  font-size: 16px;
  border-radius: 0;
  cursor: pointer;
}
</style>


<template>
  <div class="backpack-entry">
    <a-badge
      :count="itemCount"
      :overflow-count="999"
      :number-style="{ backgroundColor: '#c7ceea' }"
    >
      <a-button
        type="primary"
        class="backpack-fab"
        @click="openModal"
      >
        <span
          class="fab-icon"
          aria-hidden="true"
        >🎒</span>
        背包
      </a-button>
    </a-badge>

    <a-modal
      v-model:open="visible"
      title="我的技能背包"
      width="min(1120px, 96vw)"
      :footer="null"
      destroy-on-close
      wrap-class-name="backpack-modal-wrap"
      @cancel="visible = false"
    >
      <div
        v-if="!userStore.isLoggedIn"
        class="bp-login-tip"
      >
        请先登录后查看背包。
      </div>
      <div
        v-else-if="loading"
        class="bp-loading"
      >
        加载中…
      </div>
      <div
        v-else
        class="bp-layout"
      >
        <aside class="bp-col bp-categories">
          <div class="bp-col-title">
            分类
          </div>
          <div class="bp-cat-list">
            <button
              v-for="cat in categories"
              :key="cat || '_empty'"
              type="button"
              class="bp-cat-item"
              :class="{ active: selectedCategory === cat }"
              @click="selectedCategory = cat"
            >
              {{ cat || '未分类' }}
            </button>
          </div>
        </aside>
        <section class="bp-col bp-grid-wrap">
          <div class="bp-col-title">
            技能
          </div>
          <div
            v-if="!filteredSkills.length"
            class="bp-empty"
          >
            该分类下暂无技能
          </div>
          <div
            v-else
            class="bp-skill-grid"
          >
            <button
              v-for="item in filteredSkills"
              :key="item.skillId"
              type="button"
              class="bp-skill-card"
              :class="{ active: selectedSkill?.skillId === item.skillId }"
              @click="selectSkill(item)"
            >
              <div class="bp-skill-pic-wrap">
                <img
                  v-if="picUrl(item.skillPic)"
                  :src="picUrl(item.skillPic)"
                  :alt="item.skillName || ''"
                >
                <div
                  v-else
                  class="bp-skill-pic-ph"
                >
                  无图
                </div>
              </div>
              <div class="bp-skill-name">
                {{ item.skillName }}
              </div>
            </button>
          </div>
        </section>
        <aside class="bp-col bp-detail">
          <div class="bp-col-title">
            详情
          </div>
          <template v-if="selectedSkill">
            <h3 class="bp-detail-name">
              {{ selectedSkill.skillName }}
            </h3>
            <div class="bp-detail-row">
              <div class="bp-detail-pic">
                <img
                  v-if="picUrl(selectedSkill.skillPic)"
                  :src="picUrl(selectedSkill.skillPic)"
                  :alt="selectedSkill.skillName || ''"
                >
                <div
                  v-else
                  class="bp-skill-pic-ph lg"
                >
                  无图
                </div>
              </div>
              <div class="bp-detail-score">
                <span class="bp-label">技能积分</span>
                <div class="bp-score-val">
                  {{ selectedSkill.skillScore || '—' }}
                </div>
              </div>
            </div>
            <div class="bp-brief">
              <span class="bp-label">简介</span>
              <p>{{ selectedSkill.skillBriefDescription || '—' }}</p>
            </div>
            <div class="bp-desc">
              <span class="bp-label">详细介绍</span>
              <p>{{ selectedSkill.skillDescription || '—' }}</p>
            </div>
          </template>
          <div
            v-else
            class="bp-empty"
          >
            请从中间选择一个技能
          </div>
        </aside>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { getMyBackpack } from '@/api/BackpackApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const userStore = useUserStore()
const visible = ref(false)
const loading = ref(false)
const items = ref([])
const selectedCategory = ref(null)
const selectedSkill = ref(null)

function picUrl(p) {
  return resolveMediaUrl(p)
}

const categories = computed(() => {
  const set = new Set()
  for (const it of items.value) {
    set.add(it.skillCategory || '')
  }
  const arr = [...set].sort((a, b) => a.localeCompare(b, 'zh-CN'))
  return arr
})

const filteredSkills = computed(() => {
  if (selectedCategory.value === null) return items.value
  return items.value.filter((it) => (it.skillCategory || '') === selectedCategory.value)
})

watch(categories, (cats) => {
  if (!cats.length) {
    selectedCategory.value = null
    selectedSkill.value = null
    return
  }
  if (selectedCategory.value === null || !cats.includes(selectedCategory.value)) {
    selectedCategory.value = cats[0]
  }
})

watch(filteredSkills, (list) => {
  if (!list.length) {
    selectedSkill.value = null
    return
  }
  if (!selectedSkill.value || !list.some((x) => x.skillId === selectedSkill.value.skillId)) {
    selectedSkill.value = list[0]
  }
})

const itemCount = computed(() => items.value.length)

async function loadBackpack() {
  if (!userStore.isLoggedIn) {
    items.value = []
    return
  }
  loading.value = true
  try {
    const data = await getMyBackpack({ showDefaultMsg: false })
    items.value = Array.isArray(data) ? data : []
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

function selectSkill(item) {
  selectedSkill.value = item
  window.dispatchEvent(new CustomEvent('backpack-skill-selected', { detail: item }))
}

function openModal() {
  visible.value = true
  loadBackpack()
}

function onRefresh() {
  loadBackpack()
}

function openFromEvent() {
  visible.value = true
  selectedCategory.value = null
  selectedSkill.value = null
  loadBackpack()
}

function closeFromEvent() {
  visible.value = false
}

onMounted(() => {
  loadBackpack()
  window.addEventListener('backpack-refresh', onRefresh)
  window.addEventListener('backpack-open', openFromEvent)
  window.addEventListener('backpack-close', closeFromEvent)
})
onUnmounted(() => {
  window.removeEventListener('backpack-refresh', onRefresh)
  window.removeEventListener('backpack-open', openFromEvent)
  window.removeEventListener('backpack-close', closeFromEvent)
})
</script>

<style scoped>
.backpack-entry {
  position: fixed;
  top: 72px;
  right: 20px;
  z-index: 1000;
}

.backpack-fab {
  height: 40px;
  padding: 0 18px;
  font-weight: 600;
  border-radius: 20px;
  box-shadow: 0 4px 14px rgba(90, 74, 120, 0.25);
  background: #fff;
  border: 1px solid #d9d9d9;
  color: #3d3558;
}

.backpack-fab:hover {
  color: #2a2440;
  filter: brightness(1.03);
}

.fab-icon {
  margin-right: 6px;
}

.bp-layout {
  display: grid;
  grid-template-columns: minmax(140px, 18%) minmax(260px, 42%) minmax(220px, 40%);
  gap: 16px;
  min-height: 420px;
  max-height: min(70vh, 640px);
}

@media (max-width: 900px) {
  .bp-layout {
    grid-template-columns: 1fr;
    max-height: none;
  }
}

.bp-col-title {
  font-size: 13px;
  font-weight: 600;
  color: #6b5b7a;
  margin-bottom: 10px;
}

.bp-categories {
  border-right: 1px solid #f0f0f0;
  padding-right: 12px;
}

.bp-cat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 360px;
  overflow-y: auto;
}

.bp-cat-item {
  text-align: left;
  padding: 10px 12px;
  border: 1px solid #e8e4f0;
  border-radius: 0;
  background: #faf9fc;
  cursor: pointer;
  font-size: 14px;
  color: #5a4a78;
  transition: background 0.2s, border-color 0.2s;
}

.bp-cat-item:hover {
  background: #f3f0fa;
}

.bp-cat-item.active {
  background: #e8f4ff;
  border-color: #c7ceea;
  font-weight: 600;
}

.bp-grid-wrap {
  border-right: 1px solid #f0f0f0;
  padding-right: 12px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.bp-skill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-bottom: 4px;
}

.bp-skill-card {
  border: 1px solid #e8e4f0;
  border-radius: 0;
  padding: 8px;
  background: #fff;
  cursor: pointer;
  text-align: center;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.bp-skill-card:hover {
  box-shadow: 0 2px 8px rgba(90, 74, 120, 0.12);
}

.bp-skill-card.active {
  border-color: #c7ceea;
  box-shadow: 0 0 0 2px rgba(199, 206, 234, 0.5);
}

.bp-skill-pic-wrap {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f3fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
}

.bp-skill-pic-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bp-skill-pic-ph {
  font-size: 12px;
  color: #a89bbd;
}

.bp-skill-pic-ph.lg {
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.bp-skill-name {
  font-size: 12px;
  line-height: 1.3;
  color: #5a4a78;
  word-break: break-all;
}

.bp-detail {
  min-height: 0;
  overflow-y: auto;
}

.bp-detail-name {
  margin: 0 0 12px;
  font-size: 18px;
  color: #3d3558;
}

.bp-detail-row {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 12px;
  align-items: start;
  margin-bottom: 16px;
}

.bp-detail-pic {
  aspect-ratio: 1;
  max-width: 200px;
  background: #f5f3fa;
  overflow: hidden;
}

.bp-detail-pic img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bp-detail-score {
  padding: 8px 0;
}

.bp-label {
  display: block;
  font-size: 12px;
  color: #9a8aad;
  margin-bottom: 4px;
}

.bp-score-val {
  font-size: 20px;
  font-weight: 700;
  color: #5a4a78;
}

.bp-brief p,
.bp-desc p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #5a4a78;
}

.bp-desc {
  margin-top: 12px;
}

.bp-empty,
.bp-loading,
.bp-login-tip {
  padding: 40px 16px;
  text-align: center;
  color: #8b7aa0;
}
</style>

<style>
/* 背包弹层需高于固定「背包」按钮，避免被遮挡 */
.backpack-modal-wrap.ant-modal-wrap {
  z-index: 10040 !important;
}
</style>

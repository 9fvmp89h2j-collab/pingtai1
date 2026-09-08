<template>
  <a-modal
    v-model:open="visible"
    title="我的技能收藏"
    width="min(1120px, 96vw)"
    :footer="null"
    destroy-on-close
    wrap-class-name="backpack-modal-wrap"
    @cancel="visible = false"
  >
    <div v-if="!userStore.isLoggedIn" class="bp-login-tip">请先登录后查看收藏。</div>
    <div v-else-if="loading" class="bp-loading">加载中...</div>
    <div v-else class="bp-layout">
      <aside class="bp-col bp-categories">
        <div class="bp-col-title">分类</div>
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
        <div class="bp-col-title">收藏技能</div>
        <div v-if="!filteredSkills.length" class="bp-empty">这个分类下暂无收藏技能。</div>
        <div v-else class="bp-skill-grid">
          <button
            v-for="item in filteredSkills"
            :key="item.skillId"
            type="button"
            class="bp-skill-card"
            :class="{ active: selectedSkill?.skillId === item.skillId }"
            @click="selectedSkill = item"
          >
            <div class="bp-skill-pic-wrap">
              <img v-if="picUrl(item.skillPic)" :src="picUrl(item.skillPic)" :alt="item.skillName || ''">
              <div v-else class="bp-skill-pic-ph">无图</div>
            </div>
            <div class="bp-skill-name">{{ item.skillName }}</div>
          </button>
        </div>
        <div class="collect-actions">
          <a-button danger :disabled="!selectedSkill || cancelSubmitting" @click="cancelCollect">
            {{ cancelSubmitting ? '取消中...' : '取消收藏' }}
          </a-button>
        </div>
      </section>

      <aside class="bp-col bp-detail">
        <div class="bp-col-title">详情</div>
        <template v-if="selectedSkill">
          <h3 class="bp-detail-name">{{ selectedSkill.skillName }}</h3>
          <div class="bp-detail-row">
            <div class="bp-detail-pic">
              <img v-if="picUrl(selectedSkill.skillPic)" :src="picUrl(selectedSkill.skillPic)" :alt="selectedSkill.skillName || ''">
              <div v-else class="bp-skill-pic-ph lg">无图</div>
            </div>
            <div class="bp-detail-score">
              <span class="bp-label">技能积分</span>
              <div class="bp-score-val">{{ selectedSkill.skillScore || '-' }}</div>
            </div>
          </div>
          <div class="bp-brief">
            <span class="bp-label">简介</span>
            <p>{{ selectedSkill.skillBriefDescription || '-' }}</p>
          </div>
          <div class="bp-desc">
            <span class="bp-label">详细介绍</span>
            <p>{{ selectedSkill.skillDescription || '-' }}</p>
          </div>
        </template>
        <div v-else class="bp-empty">请从中间选择一个技能。</div>
      </aside>
    </div>
  </a-modal>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import { getMyCollect, removeSkillFromCollect } from '@/api/CollectApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const userStore = useUserStore()
const visible = ref(false)
const loading = ref(false)
const cancelSubmitting = ref(false)
const items = ref([])
const selectedCategory = ref(null)
const selectedSkill = ref(null)

function picUrl(path) {
  return resolveMediaUrl(path)
}

const categories = computed(() => {
  const set = new Set()
  for (const item of items.value) {
    set.add(item.skillCategory || '')
  }
  return [...set].sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

const filteredSkills = computed(() => {
  if (selectedCategory.value === null) return items.value
  return items.value.filter((item) => (item.skillCategory || '') === selectedCategory.value)
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
  if (!selectedSkill.value || !list.some((item) => item.skillId === selectedSkill.value.skillId)) {
    selectedSkill.value = list[0]
  }
})

async function loadCollect() {
  if (!userStore.isLoggedIn) {
    items.value = []
    return
  }
  loading.value = true
  try {
    const data = await getMyCollect({ showDefaultMsg: false })
    items.value = Array.isArray(data) ? data : []
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function cancelCollect() {
  const skillId = selectedSkill.value?.skillId
  if (!skillId) return
  cancelSubmitting.value = true
  try {
    await removeSkillFromCollect({ skillId }, { showDefaultMsg: false })
    message.success('已取消收藏')
    await loadCollect()
    window.dispatchEvent(new CustomEvent('collect-refresh'))
  } finally {
    cancelSubmitting.value = false
  }
}

function openFromEvent() {
  visible.value = true
  selectedCategory.value = null
  selectedSkill.value = null
  loadCollect()
}

function closeFromEvent() {
  visible.value = false
}

onMounted(() => {
  window.addEventListener('collect-open', openFromEvent)
  window.addEventListener('collect-close', closeFromEvent)
  window.addEventListener('collect-refresh', loadCollect)
})

onUnmounted(() => {
  window.removeEventListener('collect-open', openFromEvent)
  window.removeEventListener('collect-close', closeFromEvent)
  window.removeEventListener('collect-refresh', loadCollect)
})
</script>

<style scoped>
.bp-layout {
  display: grid;
  grid-template-columns: minmax(140px, 18%) minmax(260px, 42%) minmax(220px, 40%);
  gap: 16px;
  min-height: 420px;
  max-height: min(70vh, 640px);
}

.bp-col-title {
  margin-bottom: 10px;
  color: #6b5b7a;
  font-size: 13px;
  font-weight: 700;
}

.bp-categories,
.bp-grid-wrap {
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

.bp-cat-item,
.bp-skill-card {
  border: 1px solid #e8e4f0;
  background: #fff;
  cursor: pointer;
}

.bp-cat-item {
  padding: 10px 12px;
  color: #5a4a78;
  text-align: left;
}

.bp-cat-item.active,
.bp-skill-card.active {
  border-color: #c7ceea;
  background: #e8f4ff;
  font-weight: 700;
}

.bp-grid-wrap {
  display: flex;
  min-height: 0;
  flex-direction: column;
}

.bp-skill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  overflow-y: auto;
}

.bp-skill-card {
  padding: 8px;
  text-align: center;
}

.bp-skill-pic-wrap,
.bp-detail-pic {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  aspect-ratio: 1;
  background: #f5f3fa;
}

.bp-skill-pic-wrap img,
.bp-detail-pic img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bp-skill-pic-ph {
  color: #a89bbd;
  font-size: 12px;
}

.bp-skill-name {
  margin-top: 6px;
  color: #5a4a78;
  font-size: 12px;
  line-height: 1.3;
}

.collect-actions {
  display: flex;
  justify-content: center;
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.bp-detail {
  overflow-y: auto;
}

.bp-detail-name {
  margin: 0 0 12px;
  color: #3d3558;
  font-size: 18px;
}

.bp-detail-row {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 12px;
  margin-bottom: 16px;
}

.bp-detail-pic {
  max-width: 200px;
}

.bp-label {
  display: block;
  margin-bottom: 4px;
  color: #9a8aad;
  font-size: 12px;
}

.bp-score-val {
  color: #5a4a78;
  font-size: 20px;
  font-weight: 800;
}

.bp-brief p,
.bp-desc p {
  margin: 0;
  color: #5a4a78;
  font-size: 14px;
  line-height: 1.6;
}

.bp-desc {
  margin-top: 12px;
}

.bp-empty,
.bp-loading,
.bp-login-tip {
  padding: 40px 16px;
  color: #8b7aa0;
  text-align: center;
}

@media (max-width: 900px) {
  .bp-layout {
    grid-template-columns: 1fr;
    max-height: none;
  }
}
</style>

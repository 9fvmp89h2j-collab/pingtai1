<template>
  <div class="extra-detail-page">
    <div
      v-if="loading"
      class="extra-loading"
    >
      加载中…
    </div>
    <div
      v-else-if="error || !detail"
      class="extra-loading"
    >
      加载失败
    </div>
    <div
      v-else
      class="extra-wrap"
    >
      <div class="extra-left">
        <div
          v-for="(p, idx) in pics"
          :key="idx"
          class="extra-pic-box"
        >
          <img
            v-if="resolveMediaUrl(p)"
            :src="resolveMediaUrl(p)"
            alt=""
          >
          <div
            v-else
            class="extra-pic-ph"
          >
            暂无图片
          </div>
        </div>
      </div>
      <div class="extra-right">
        <h2 class="extra-name">
          {{ detail.extraCourseName || '—' }}
        </h2>
        <p class="extra-brief">
          {{ detail.extraCourseBrief || '—' }}
        </p>
        <p class="extra-des">
          {{ detail.extraCourseDes || '—' }}
        </p>
        <button
          type="button"
          class="extra-collect-btn"
          :disabled="collecting || detail.skillId == null"
          @click="collectSkill"
        >
          {{ collecting ? '采集中…' : '采集' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getExtraCourseDetail } from '@/api/AcupunctureApi'
import { addSkillToBackpack } from '@/api/BackpackApi'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const error = ref(false)
const detail = ref(null)
const collecting = ref(false)

const pics = computed(() => {
  const d = detail.value || {}
  return [d.extraCoursePic1, d.extraCoursePic2, d.extraCoursePic3]
})

async function load() {
  loading.value = true
  error.value = false
  try {
    const data = await getExtraCourseDetail(route.params.id, { showDefaultMsg: false })
    detail.value = data || null
  } catch {
    error.value = true
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function collectSkill() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再采集')
    return
  }
  const sid = detail.value?.skillId
  if (sid == null) {
    message.warning('未关联技能')
    return
  }
  collecting.value = true
  try {
    await addSkillToBackpack({ skillId: sid }, { showDefaultMsg: false })
    message.success('已加入背包')
  } catch (e) {
    message.error(e?.message || '采集失败')
  } finally {
    collecting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.extra-detail-page {
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  background: #f0f2f5;
}

.extra-loading {
  text-align: center;
  padding: 60px 16px;
}

.extra-wrap {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  display: grid;
  grid-template-columns: 42% 58%;
  gap: 24px;
}

.extra-left {
  display: grid;
  gap: 14px;
}

.extra-pic-box {
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.extra-pic-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.extra-pic-ph {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.extra-right {
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.extra-name {
  margin: 0 0 12px;
  font-size: 32px;
}

.extra-brief {
  margin: 0 0 14px;
  color: #334155;
  font-size: 18px;
}

.extra-des {
  margin: 0 0 24px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.extra-collect-btn {
  width: 120px;
  height: 42px;
  border: none;
  border-radius: 0;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

.extra-collect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>


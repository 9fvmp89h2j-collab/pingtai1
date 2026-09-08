<template>
  <div class="my-post-page">
    <div class="my-post-inner">
      <div class="top-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="top-tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="content-panel">
        <div
          v-if="loading"
          class="state-tip"
        >
          加载中…
        </div>
        <div
          v-else-if="!cardList.length"
          class="state-tip"
        >
          暂无内容
        </div>
        <template v-else>
          <div
            v-for="item in cardList"
            :key="item.key"
            class="post-card"
          >
            <h3 class="card-title">
              {{ item.title || '未命名帖子' }}
            </h3>
            <p class="card-meta">
              {{ item.meta }}
            </p>
            <p class="card-content">
              {{ item.content || '—' }}
            </p>
            <p
              v-if="item.commentContent"
              class="my-comment"
            >
              我的评论：{{ item.commentContent }}
            </p>
            <div class="card-actions">
              <span
                v-if="item.postId"
                class="action-btn"
                :class="{ active: item.liked }"
                @click="toggleLike(item)"
              >❤️</span>
              <span
                v-if="item.postId"
                class="action-btn"
                :class="{ active: item.collected }"
                @click="toggleCollect(item)"
              >⭐</span>
              <span
                v-if="item.postId"
                class="action-btn"
                @click="openPostDetail(item.postId)"
              >💬</span>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  getMyPostList,
  getMyCollectedPostList,
  getMyLikedPostList,
  getMyCommentList,
  postLike,
  postCollect
} from '@/api/CommunityApi'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('my-posts')

const postList = ref([])
const collectList = ref([])
const likedList = ref([])
const commentList = ref([])

const tabs = [
  { key: 'my-posts', label: '我的发帖' },
  { key: 'my-collects', label: '我的收藏' },
  { key: 'my-likes', label: '我赞过' },
  { key: 'my-comments', label: '我的评论' }
]

const cardList = computed(() => {
  if (activeTab.value === 'my-posts') {
    return (postList.value || []).map((x) => ({
      key: `p-${x.id}`,
      postId: x.id,
      title: x.title,
      content: x.contentSummary,
      meta: `${x.username || '我'}·${formatTime(x.createTime)}`,
      liked: Boolean(x.liked),
      collected: Boolean(x.collected)
    }))
  }
  if (activeTab.value === 'my-collects') {
    return (collectList.value || []).map((x) => ({
      key: `c-${x.id}`,
      postId: x.id,
      title: x.title,
      content: x.contentSummary,
      meta: `${x.username || '匿名'}·${formatTime(x.createTime)}`,
      liked: Boolean(x.liked),
      collected: Boolean(x.collected)
    }))
  }
  if (activeTab.value === 'my-likes') {
    return (likedList.value || []).map((x) => ({
      key: `l-${x.id}`,
      postId: x.id,
      title: x.title,
      content: x.contentSummary,
      meta: `${x.username || '匿名'}·${formatTime(x.createTime)}`,
      liked: Boolean(x.liked),
      collected: Boolean(x.collected)
    }))
  }
  return (commentList.value || []).map((x) => ({
    key: `m-${x.commentId}`,
    postId: x.postId,
    title: x.postTitle,
    content: x.postContentSummary,
    commentContent: x.commentContent,
    meta: `评论于 ${formatTime(x.createTime)}`,
    liked: false,
    collected: false
  }))
})

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()}`
}

async function switchTab(tabKey) {
  activeTab.value = tabKey
  await loadCurrentTabData()
}

async function loadCurrentTabData() {
  loading.value = true
  try {
    if (activeTab.value === 'my-posts') {
      postList.value = (await getMyPostList({ showDefaultMsg: false })) || []
      return
    }
    if (activeTab.value === 'my-collects') {
      collectList.value = (await getMyCollectedPostList({ showDefaultMsg: false })) || []
      return
    }
    if (activeTab.value === 'my-likes') {
      likedList.value = (await getMyLikedPostList({ showDefaultMsg: false })) || []
      return
    }
    commentList.value = (await getMyCommentList({ showDefaultMsg: false })) || []
  } catch (e) {
    message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openPostDetail(postId) {
  router.push({ path: '/community', query: { postId } }).catch(() => {})
}

async function toggleLike(item) {
  if (!item.postId) return
  await postLike(item.postId, { showDefaultMsg: false })
  item.liked = !item.liked
}

async function toggleCollect(item) {
  if (!item.postId) return
  await postCollect(item.postId, { showDefaultMsg: false })
  item.collected = !item.collected
}

onMounted(() => {
  loadCurrentTabData()
})
</script>

<style scoped>
.my-post-page {
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  background: #f6e8d3;
}

.my-post-inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 20px 16px 28px;
}

.top-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #bfbfbf;
}

.top-tab-btn {
  min-width: 120px;
  height: 42px;
  padding: 0 20px;
  border: 1px solid #c9c9c9;
  border-bottom: none;
  background: #f5f5f5;
  font-size: 16px;
  cursor: pointer;
}

.top-tab-btn.active {
  background: #fff;
  font-weight: 600;
}

.content-panel {
  border: 1px solid #cfcfcf;
  border-top: none;
  background: #fff;
  padding: 14px;
  min-height: 360px;
}

.state-tip {
  text-align: center;
  color: #888;
  padding: 40px 0;
}

.post-card {
  border: 1px solid #d8d8d8;
  background: #f8f8f8;
  padding: 14px 14px 8px;
  margin-bottom: 12px;
}

.card-title {
  margin: 0 0 4px;
  font-size: 20px;
  color: #202020;
}

.card-meta {
  margin: 0 0 10px;
  color: #666;
  font-size: 14px;
}

.card-content {
  margin: 0 0 10px;
  color: #333;
  line-height: 1.6;
}

.my-comment {
  margin: 0 0 8px;
  color: #5a4a78;
  font-size: 14px;
}

.card-actions {
  border-top: 1px solid #e9e9e9;
  padding-top: 8px;
  display: flex;
  gap: 14px;
}

.action-btn {
  cursor: pointer;
}

.action-btn.active {
  transform: scale(1.08);
}
</style>

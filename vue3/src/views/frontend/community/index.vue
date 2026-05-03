<template>
  <div class="community-page">
    <div class="background-image" />

    <div class="page-inner">
      <!-- 顶部板块切换 -->
      <div class="type-tabs">
        <button
          v-for="t in postTypes"
          :key="t"
          type="button"
          class="type-tab"
          :class="{ active: activeType === t }"
          @click="setActiveType(t)"
        >
          {{ t }}
        </button>
      </div>

      <div
        class="main-layout"
        :class="{ 'with-sidebar': activeType === TYPE_NOTICE_TAB }"
      >
        <!-- 左侧：帖子列表 -->
        <div class="post-column">
          <div class="toolbar">
            <a-input-search
              v-model:value="keyword"
              placeholder="搜索标题或内容..."
              allow-clear
              class="search-input"
              @search="loadPage(1)"
            />
            <a-button
              type="primary"
              class="btn-publish"
              @click="openPublish"
            >
              发帖
            </a-button>
          </div>

          <a-spin :spinning="loading">
            <div
              v-for="item in list"
              :key="item.id"
              class="post-card"
              @click="openDetail(item.id)"
            >
              <h3 class="card-title">
                {{ item.title }}
              </h3>
              <p class="card-meta">
                <span class="author">{{ item.username || '匿名' }}</span>
                <span class="dot">·</span>
                <span class="time">{{ formatTime(item.createTime) }}</span>
              </p>
              <p class="card-content">
                {{ item.contentSummary || '—' }}
              </p>
              <div
                class="card-actions"
                @click.stop
              >
                <span
                  class="action-btn"
                  :class="{ active: item.liked }"
                  @click="toggleLike(item)"
                >❤️</span>
                <span
                  class="action-btn"
                  :class="{ active: item.collected }"
                  @click="toggleCollect(item)"
                >⭐</span>
                <span
                  class="action-btn"
                  @click="openDetail(item.id)"
                >💬</span>
                <span
                  class="action-btn"
                  @click="reportPost(item)"
                >🚩</span>
              </div>
            </div>
            <a-empty
              v-if="!loading && list.length === 0"
              description="该板块暂无帖子"
            />
          </a-spin>

          <a-pagination
            v-if="total > 0"
            v-model:current="current"
            v-model:page-size="pageSize"
            :total="total"
            show-size-changer
            show-total
            class="pagination"
            @change="onPageChange"
          />
        </div>

        <!-- 右侧：意见箱（仅「公告与反馈」Tab；左侧列表仅展示公告类帖子） -->
        <aside
          v-if="activeType === TYPE_NOTICE_TAB"
          class="feedback-sidebar"
        >
          <div class="sidebar-title">
            意见与建议
          </div>
          <a-textarea
            v-model:value="feedbackContent"
            placeholder="输入您的意见与建议"
            :rows="12"
            class="feedback-textarea"
          />
          <a-button
            type="primary"
            block
            class="feedback-submit"
            :loading="feedbackSubmitting"
            @click="submitFeedback"
          >
            提交反馈
          </a-button>
        </aside>
      </div>
    </div>

    <!-- 发帖 -->
    <a-modal
      v-model:open="showPublish"
      title="发帖"
      ok-text="发布"
      cancel-text="取消"
      @ok="submitPost"
    >
      <a-form layout="vertical">
        <a-form-item
          label="板块"
          required
        >
          <a-radio-group v-model:value="publishForm.postType">
            <a-radio value="分享专区">
              分享专区
            </a-radio>
            <a-radio value="问答专区">
              问答专区
            </a-radio>
            <a-radio
              v-if="userStore.isAdmin"
              value="公告"
            >
              公告
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          label="标题"
          required
        >
          <a-input
            v-model:value="publishForm.title"
            placeholder="请输入标题"
            maxlength="200"
            show-count
          />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea
            v-model:value="publishForm.content"
            placeholder="请输入正文"
            :rows="6"
          />
        </a-form-item>
        <a-form-item label="图片（最多5张）">
          <a-upload
            v-model:file-list="publishImageList"
            list-type="picture-card"
            :custom-request="customUploadPostImage"
            :before-upload="beforeUploadPostImage"
            :max-count="5"
          >
            <div v-if="publishImageList.length < 5">
              <i class="fas fa-upload" />
              <div style="margin-top: 8px">
                上传图片
              </div>
            </div>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailOpen"
      title="帖子详情"
      width="min(600px, 96vw)"
    >
      <template v-if="detail">
        <h2>{{ detail.title }}</h2>
        <p class="detail-meta">
          {{ detail.username }} · {{ formatTime(detail.createTime) }}
          <span
            v-if="detail.postType"
            class="detail-type"
          > · {{ detail.postType }}</span>
        </p>
        <div class="detail-content">
          {{ detail.content }}
        </div>
        <div
          v-if="detailImages.length"
          class="detail-images"
        >
          <img
            v-for="(src, idx) in detailImages"
            :key="idx"
            :src="src"
            :alt="`post-image-${idx + 1}`"
          >
        </div>
        <div class="detail-actions">
          <a-button
            :type="detail.liked ? 'primary' : 'default'"
            @click="toggleLikeById"
          >
            ❤️ {{ detail.liked ? '已赞' : '赞' }} ({{ detail.likeCount }})
          </a-button>
          <a-button
            :type="detail.collected ? 'primary' : 'default'"
            @click="toggleCollectById"
          >
            ⭐ {{ detail.collected ? '已收藏' : '收藏' }}
          </a-button>
          <a-button
            danger
            @click="reportPost(detail)"
          >
            举报
          </a-button>
        </div>
        <div class="comment-section">
          <h4>评论 ({{ detail.commentCount }})</h4>
          <a-textarea
            v-model:value="commentContent"
            placeholder="写下你的评论..."
            :rows="3"
          />
          <a-button
            type="primary"
            class="mt-8"
            @click="submitComment"
          >
            发表评论
          </a-button>
          <div
            v-for="c in detail.comments"
            :key="c.id"
            class="comment-item"
          >
            <strong>{{ c.username }}</strong>: {{ c.content }}
            <span class="comment-time">{{ formatTime(c.createTime) }}</span>
          </div>
        </div>
      </template>
    </a-drawer>

    <a-modal
      v-model:open="reportOpen"
      title="举报"
      ok-text="提交"
      @ok="submitReport"
    >
      <a-textarea
        v-model:value="reportReason"
        placeholder="举报原因（选填）"
        :rows="3"
      />
    </a-modal>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  getPostPage,
  getPostDetail,
  createPost,
  postLike,
  postCollect,
  postReport,
  addComment
} from '@/api/CommunityApi'
import { uploadBusinessFile } from '@/api/FileApi'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const TYPE_QA = '问答专区'
const TYPE_SHARE = '分享专区'
/** 第三 Tab 展示名称（左侧列表按「公告」筛选） */
const TYPE_NOTICE_TAB = '公告与反馈'
const API_TYPE_ANNOUNCE = '公告'
const API_TYPE_FEEDBACK = '反馈'
const postTypes = [TYPE_QA, TYPE_SHARE, TYPE_NOTICE_TAB]

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeType = ref(TYPE_QA)
const list = ref([])
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

const showPublish = ref(false)
const publishImageList = ref([])
const publishBusinessId = ref(`post-${Date.now()}`)
const publishForm = ref({
  title: '',
  content: '',
  postType: TYPE_SHARE
})

const detailOpen = ref(false)
const detail = ref(null)
const commentContent = ref('')

const feedbackContent = ref('')
const feedbackSubmitting = ref(false)

const reportOpen = ref(false)
const reportReason = ref('')
const reportTargetId = ref(null)

const detailImages = computed(() => {
  const d = detail.value
  if (!d) return []
  return [d.postPic1, d.postPic2, d.postPic3, d.postPic4, d.postPic5]
    .map((x) => resolveMediaUrl(x))
    .filter(Boolean)
})

function setActiveType(t) {
  activeType.value = t
  router.replace({ query: { ...route.query, type: t } })
  loadPage(1)
}

function loadPage(page) {
  current.value = page || current.value
  loading.value = true
  const postTypeParam =
    activeType.value === TYPE_NOTICE_TAB ? API_TYPE_ANNOUNCE : activeType.value
  getPostPage(
    {
      current: current.value,
      size: pageSize.value,
      title: keyword.value || undefined,
      postType: postTypeParam
    },
    {
      onSuccess: (res) => {
        list.value = res?.records || []
        total.value = res?.total || 0
        loading.value = false
      },
      onError: () => { loading.value = false }
    }
  )
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleDateString()
}

function openDetail(postId) {
  getPostDetail(postId, {
    onSuccess: (res) => {
      detail.value = res
      commentContent.value = ''
      detailOpen.value = true
    }
  })
}

function toggleLike(item) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  postLike(item.id, {
    onSuccess: () => {
      item.liked = !item.liked
      item.likeCount += item.liked ? 1 : -1
    },
    onError: (e) => message.error(e?.message || '操作失败')
  })
}

function toggleCollect(item) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  postCollect(item.id, {
    onSuccess: () => {
      item.collected = !item.collected
      item.collectCount += item.collected ? 1 : -1
    }
  })
}

function toggleLikeById() {
  if (!detail.value || !userStore.isLoggedIn) return
  postLike(detail.value.id, {
    onSuccess: () => {
      detail.value.liked = !detail.value.liked
      detail.value.likeCount += detail.value.liked ? 1 : -1
    }
  })
}

function toggleCollectById() {
  if (!detail.value || !userStore.isLoggedIn) return
  postCollect(detail.value.id, {
    onSuccess: () => {
      detail.value.collected = !detail.value.collected
      detail.value.collectCount += detail.value.collected ? 1 : -1
    }
  })
}

function openPublish() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再发帖')
    return
  }
  const t = activeType.value
  let defaultPt = t === TYPE_QA ? TYPE_QA : TYPE_SHARE
  if (userStore.isAdmin && t === TYPE_NOTICE_TAB) {
    defaultPt = API_TYPE_ANNOUNCE
  }
  publishForm.value = {
    title: '',
    content: '',
    postType: defaultPt
  }
  publishImageList.value = []
  publishBusinessId.value = `post-${Date.now()}`
  showPublish.value = true
}

function parsePublishPreset() {
  try {
    const raw = sessionStorage.getItem('community-publish-preset')
    if (!raw) return null
    sessionStorage.removeItem('community-publish-preset')
    const obj = JSON.parse(raw)
    if (!obj || typeof obj !== 'object') return null
    return {
      postType: obj.postType || TYPE_SHARE,
      title: obj.title || '',
      content: obj.content || '',
      images: Array.isArray(obj.images) ? obj.images.filter(Boolean).slice(0, 5) : []
    }
  } catch {
    return null
  }
}

function openPublishWithPreset(preset) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再发帖')
    return
  }
  publishBusinessId.value = `post-${Date.now()}`
  publishForm.value = {
    title: preset?.title || '',
    content: preset?.content || '',
    postType: preset?.postType || TYPE_SHARE
  }
  publishImageList.value = (preset?.images || []).map((path, idx) => ({
    uid: `preset-${idx}`,
    name: `badge-${idx + 1}.png`,
    status: 'done',
    url: resolveMediaUrl(path),
    response: { filePath: path }
  }))
  showPublish.value = true
}

function beforeUploadPostImage(file) {
  const isImage = file.type?.startsWith('image/')
  if (!isImage) {
    message.warning('只能上传图片文件')
    return false
  }
  if (publishImageList.value.length >= 5) {
    message.warning('最多上传5张图片')
    return false
  }
  return true
}

async function customUploadPostImage(options) {
  const { file, onSuccess, onError } = options
  try {
    const res = await uploadBusinessFile(
      file,
      {
        businessType: 'POST_CONTENT',
        businessId: publishBusinessId.value,
        businessField: 'content'
      },
      false,
      { showDefaultMsg: false }
    )
    onSuccess?.({ filePath: res?.filePath || '' })
  } catch (e) {
    onError?.(e)
  }
}

function submitPost() {
  if (!publishForm.value.title?.trim()) {
    message.warning('请输入标题')
    return
  }
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  createPost(
    {
      title: publishForm.value.title.trim(),
      content: publishForm.value.content || '',
      postType: publishForm.value.postType,
      postPic1: publishImageList.value[0]?.response?.filePath || '',
      postPic2: publishImageList.value[1]?.response?.filePath || '',
      postPic3: publishImageList.value[2]?.response?.filePath || '',
      postPic4: publishImageList.value[3]?.response?.filePath || '',
      postPic5: publishImageList.value[4]?.response?.filePath || ''
    },
    {
      onSuccess: () => {
        message.success('发布成功')
        showPublish.value = false
        publishImageList.value = []
        loadPage(1)
      },
      onError: (e) => message.error(e?.message || '发布失败')
    }
  )
}

async function submitFeedback() {
  const text = feedbackContent.value?.trim()
  if (!text) {
    message.warning('请输入意见与建议')
    return
  }
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再提交')
    return
  }
  feedbackSubmitting.value = true
  const title = text.length <= 80 ? text : `${text.slice(0, 80)}…`
  try {
    await createPost(
      {
        title: `【反馈】${title}`,
        content: text,
        postType: API_TYPE_FEEDBACK
      },
      { showDefaultMsg: false }
    )
    message.success('感谢您的反馈')
    feedbackContent.value = ''
    loadPage(1)
  } catch (e) {
    message.error(e?.message || '提交失败')
  } finally {
    feedbackSubmitting.value = false
  }
}

function submitComment() {
  if (!detail.value || !userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  addComment(
    { postId: detail.value.id, content: commentContent.value || '' },
    {
      onSuccess: (res) => {
        if (!detail.value.comments) detail.value.comments = []
        detail.value.comments.push(res)
        detail.value.commentCount++
        commentContent.value = ''
        message.success('评论成功')
      }
    }
  )
}

function reportPost(item) {
  reportTargetId.value = item.id
  reportReason.value = ''
  reportOpen.value = true
}

function submitReport() {
  if (!reportTargetId.value) return
  postReport(
    { postId: reportTargetId.value, reason: reportReason.value },
    {
      onSuccess: () => {
        message.success('举报已提交')
        reportOpen.value = false
        reportTargetId.value = null
      }
    }
  )
}

function onPageChange(page) {
  loadPage(page)
}

watch(
  () => route.query.type,
  (q) => {
    if (typeof q === 'string' && postTypes.includes(q)) {
      activeType.value = q
    }
  }
)

onMounted(() => {
  const q = route.query.type
  if (typeof q === 'string' && postTypes.includes(q)) {
    activeType.value = q
  }
  const postId = route.query.postId
  if (postId) {
    openDetail(Number(postId))
  }
  const kw = route.query.keyword
  if (kw) keyword.value = kw
  loadPage(1)
  if (route.query.openPublish === '1') {
    const preset = parsePublishPreset()
    openPublishWithPreset(preset)
  }
})
</script>

<style scoped lang="less">
.community-page {
  min-height: calc(100vh - 64px);
  margin-top: -64px;
  padding-top: 64px;
  position: relative;
  background: #e8ecf1;
}

.background-image {
  position: fixed;
  inset: 0;
  background-image: url('@/assets/home_back.jpeg');
  background-size: cover;
  background-position: center;
  opacity: 0.22;
  z-index: 0;
  pointer-events: none;
}

.page-inner {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px 48px;
}

.type-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 0;
  border-bottom: 2px solid rgba(0, 0, 0, 0.85);
}

.type-tab {
  flex: 1;
  max-width: 200px;
  padding: 14px 20px;
  font-size: 16px;
  border: 1px solid rgba(0, 0, 0, 0.75);
  border-bottom: none;
  background: #fff;
  cursor: pointer;
  margin-right: -1px;
}

.type-tab.active {
  background: #d1d5db;
  font-weight: 600;
}

.main-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-top: 0;
  padding-top: 20px;
}

.main-layout.with-sidebar .post-column {
  flex: 1;
  min-width: 0;
}

.post-column {
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 2px solid rgba(0, 0, 0, 0.85);
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.btn-publish {
  flex-shrink: 0;
}

.post-card {
  border: 1px solid rgba(0, 0, 0, 0.35);
  padding: 16px 18px;
  margin-bottom: 14px;
  cursor: pointer;
  background: #fafafa;
  transition: background 0.2s;
}

.post-card:hover {
  background: #f0f4ff;
}

.card-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: #111;
}

.card-meta {
  margin: 0 0 10px;
  font-size: 14px;
  color: #555;
}

.card-content {
  margin: 0 0 12px;
  font-size: 15px;
  color: #333;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  display: flex;
  gap: 14px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.action-btn {
  cursor: pointer;
  opacity: 0.85;
}

.action-btn.active {
  opacity: 1;
  transform: scale(1.08);
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.feedback-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border: 2px solid rgba(0, 0, 0, 0.85);
  padding: 16px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.feedback-textarea {
  margin-bottom: 12px;
}

.feedback-submit {
  height: 40px;
}

.detail-meta {
  color: #666;
  font-size: 14px;
}

.detail-type {
  color: #2563eb;
}

.detail-content {
  margin: 16px 0;
  white-space: pre-wrap;
  line-height: 1.6;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 0 0 16px;
}

.detail-images img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border: 1px solid #ddd;
  background: #f3f3f3;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.comment-section {
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.comment-item {
  margin-top: 10px;
  padding: 8px;
  background: #f9fafb;
  border-radius: 4px;
}

.comment-time {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

.mt-8 {
  margin-top: 8px;
}

@media (max-width: 900px) {
  .main-layout.with-sidebar {
    flex-direction: column;
  }

  .feedback-sidebar {
    width: 100%;
  }

  .type-tab {
    max-width: none;
    font-size: 14px;
    padding: 10px 8px;
  }
}
</style>

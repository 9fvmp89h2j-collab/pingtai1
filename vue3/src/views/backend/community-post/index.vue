<template>
  <div class="page">
    <a-card
      title="帖子管理"
      :bordered="false"
    >
      <a-form
        layout="inline"
        class="toolbar"
      >
        <a-form-item>
          <a-button
            type="primary"
            @click="openCreate"
          >
            发公告
          </a-button>
        </a-form-item>
        <a-form-item label="用户ID">
          <a-input-number
            v-model:value="query.userId"
            placeholder="user_id"
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input
            v-model:value="query.title"
            placeholder="标题/内容关键词"
            allow-clear
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="分类">
          <a-input
            v-model:value="query.postCatagory"
            placeholder="postcatagory"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="板块">
          <a-select
            v-model:value="query.postType"
            allow-clear
            placeholder="posttype"
            style="width: 200px"
          >
            <a-select-option value="问答专区">
              问答专区
            </a-select-option>
            <a-select-option value="分享专区">
              分享专区
            </a-select-option>
            <a-select-option value="公告">
              公告
            </a-select-option>
            <a-select-option value="反馈">
              反馈
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="query.status"
            allow-clear
            placeholder="status"
            style="width: 140px"
          >
            <a-select-option :value="0">
              0 正常
            </a-select-option>
            <a-select-option :value="1">
              1 已删除
            </a-select-option>
            <a-select-option :value="2">
              2 已举报
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button
              type="primary"
              @click="handleSearch"
            >
              查询
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :data-source="tableData"
        :columns="columns"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <span>{{ statusText(record.status) }}</span>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-popconfirm
              title="确认删除该帖子吗？"
              @confirm="onDelete(record)"
            >
              <a style="color: #ff4d4f">删除</a>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="createOpen"
      title="发帖"
      :confirm-loading="saving"
      width="720px"
      destroy-on-close
      @ok="onCreateSubmit"
      @cancel="createOpen = false"
    >
      <a-form
        :model="createForm"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item
          label="板块"
          required
        >
          <a-select
            v-model:value="createForm.postType"
            style="width: 100%"
          >
            <a-select-option value="公告">
              公告
            </a-select-option>
            <a-select-option value="问答专区">
              问答专区
            </a-select-option>
            <a-select-option value="分享专区">
              分享专区
            </a-select-option>
            <a-select-option value="反馈">
              反馈
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item
          label="标题"
          required
        >
          <a-input
            v-model:value="createForm.title"
            placeholder="标题"
            maxlength="200"
            show-count
          />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea
            v-model:value="createForm.content"
            :rows="8"
            placeholder="正文"
          />
        </a-form-item>
        <a-form-item label="分类">
          <a-input
            v-model:value="createForm.postCatagory"
            placeholder="postcatagory（选填）"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createCommunityPost, deleteCommunityPost, pageCommunityPosts } from '@/api/CommunityPostAdminApi'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])

const query = reactive({
  userId: null,
  title: '',
  postCatagory: '',
  postType: undefined,
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (t) => `共 ${t} 条`
})

const columns = [
  { title: 'id', dataIndex: 'id', width: 90 },
  { title: 'user_id', dataIndex: 'userId', width: 110 },
  { title: 'title', dataIndex: 'title', ellipsis: true, width: 200 },
  { title: 'content', dataIndex: 'content', ellipsis: true },
  { title: 'like_count', dataIndex: 'likeCount', width: 110 },
  { title: 'collect_count', dataIndex: 'collectCount', width: 120 },
  { title: 'comment_count', dataIndex: 'commentCount', width: 130 },
  { title: 'postcatagory', dataIndex: 'postCatagory', width: 120, ellipsis: true },
  { title: 'posttype', dataIndex: 'postType', width: 120, ellipsis: true },
  { title: 'status', key: 'status', width: 110 },
  { title: 'create_time', dataIndex: 'createTime', width: 180 },
  { title: 'update_time', dataIndex: 'updateTime', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 100 }
]

const createOpen = ref(false)
const createForm = reactive({
  title: '',
  content: '',
  postCatagory: '',
  postType: '公告'
})

function statusText(v) {
  if (v === 0) return '0 正常'
  if (v === 1) return '1 已删除'
  if (v === 2) return '2 已举报'
  return String(v ?? '-')
}

function openCreate() {
  createForm.title = ''
  createForm.content = ''
  createForm.postCatagory = ''
  createForm.postType = '公告'
  createOpen.value = true
}

async function onCreateSubmit() {
  if (!createForm.title?.trim()) {
    message.warning('请输入标题')
    return
  }
  saving.value = true
  try {
    await createCommunityPost(
      {
        title: createForm.title.trim(),
        content: createForm.content || '',
        postType: createForm.postType,
        postCatagory: createForm.postCatagory || undefined
      },
      { showDefaultMsg: false }
    )
    message.success('发布成功')
    createOpen.value = false
    loadData()
  } catch (e) {
    message.error(e?.message || '发布失败')
  } finally {
    saving.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const page = await pageCommunityPosts({
      current: pagination.current,
      size: pagination.pageSize,
      userId: query.userId ?? undefined,
      title: query.title || undefined,
      postCatagory: query.postCatagory || undefined,
      postType: query.postType || undefined,
      status: query.status ?? undefined
    }, { showDefaultMsg: false })
    tableData.value = page?.records || []
    pagination.total = page?.total || 0
  } catch (e) {
    message.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  query.userId = null
  query.title = ''
  query.postCatagory = ''
  query.postType = undefined
  query.status = undefined
  pagination.current = 1
  loadData()
}

function handleTableChange(p) {
  pagination.current = p.current
  pagination.pageSize = p.pageSize
  loadData()
}

async function onDelete(record) {
  try {
    await deleteCommunityPost(record.id, { showDefaultMsg: false })
    message.success('删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    loadData()
  } catch (e) {
    message.error('删除失败')
  }
}

loadData()
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
</style>

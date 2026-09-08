<template>
  <div class="page">
    <div class="admin-list-header">
      <div>
        <h1>评论管理</h1>
        <p class="page-description">检索社区评论并处理不适合继续展示的内容。</p>
      </div>
    </div>
    <AdminPageError :message="loadError" :loading="loading" @retry="loadData" />
    <a-card
      :bordered="false"
    >
      <a-form
        layout="inline"
        class="toolbar"
      >
        <a-form-item label="帖子编号">
          <a-input-number
            v-model:value="query.postId"
            placeholder="输入帖子编号"
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="用户编号">
          <a-input-number
            v-model:value="query.userId"
            placeholder="输入用户编号"
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input
            v-model:value="query.keyword"
            placeholder="内容关键词"
            allow-clear
            style="width: 240px"
          />
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

      <div class="admin-table-toolbar">
        <span>共 {{ pagination.total }} 条评论</span>
        <a-button type="text" :loading="loading" @click="loadData">刷新数据</a-button>
      </div>

      <a-table
        :data-source="tableData"
        :columns="columns"
        :loading="loading"
        row-key="id"
        :scroll="isDesktopViewport ? { x: 1180 } : undefined"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'actions'">
            <a-popconfirm
              title="确认删除该评论吗？"
              @confirm="onDelete(record)"
            >
              <a style="color: #ff4d4f">删除</a>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { deletePostComment, pagePostComments } from '@/api/PostCommentAdminApi'
import AdminPageError from '@/components/backend/AdminPageError.vue'
import { useDesktopViewport } from '@/composables/useDesktopViewport'

const isDesktopViewport = useDesktopViewport()
const loading = ref(false)
const loadError = ref('')
const tableData = ref([])

const query = reactive({
  postId: null,
  userId: null,
  keyword: ''
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
  { title: '编号', dataIndex: 'id', width: 80 },
  { title: '帖子编号', dataIndex: 'postId', width: 100 },
  { title: '用户编号', dataIndex: 'userId', width: 100 },
  { title: '评论内容', dataIndex: 'content', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', width: 170 },
  { title: '操作', key: 'actions', fixed: 'right', width: 100 }
]

async function loadData() {
  loading.value = true
  loadError.value = ''
  try {
    const page = await pagePostComments({
      current: pagination.current,
      size: pagination.pageSize,
      postId: query.postId ?? undefined,
      userId: query.userId ?? undefined,
      keyword: query.keyword || undefined
    }, { showDefaultMsg: false })
    tableData.value = page?.records || []
    pagination.total = page?.total || 0
  } catch (e) {
    loadError.value = e?.message || '获取评论列表失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  query.postId = null
  query.userId = null
  query.keyword = ''
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
    await deletePostComment(record.id, { showDefaultMsg: false })
    message.success('删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    await loadData()
  } catch (e) {
    message.error(e?.message || '删除失败')
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

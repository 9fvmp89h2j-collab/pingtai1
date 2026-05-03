<template>
  <div class="page">
    <a-card
      title="反馈消息"
      :bordered="false"
    >
      <p class="hint">
        仅展示「反馈」类帖子（用户意见箱提交的内容）；与帖子管理为同一张表，此处不展示公告。
      </p>
      <a-form
        layout="inline"
        class="toolbar"
      >
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
              title="确认删除该反馈吗？"
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
import { deleteCommunityPost, pageCommunityPosts } from '@/api/CommunityPostAdminApi'

const POST_TYPE_FEEDBACK = '反馈'

const loading = ref(false)
const tableData = ref([])

const query = reactive({
  userId: null,
  title: ''
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
  { title: 'title', dataIndex: 'title', ellipsis: true, width: 220 },
  { title: 'content', dataIndex: 'content', ellipsis: true },
  { title: 'posttype', dataIndex: 'postType', width: 100 },
  { title: 'status', key: 'status', width: 110 },
  { title: 'create_time', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 100 }
]

function statusText(v) {
  if (v === 0) return '0 正常'
  if (v === 1) return '1 已删除'
  if (v === 2) return '2 已举报'
  return String(v ?? '-')
}

async function loadData() {
  loading.value = true
  try {
    const page = await pageCommunityPosts({
      current: pagination.current,
      size: pagination.pageSize,
      userId: query.userId ?? undefined,
      title: query.title || undefined,
      postType: POST_TYPE_FEEDBACK
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
.hint {
  color: #666;
  font-size: 13px;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
</style>

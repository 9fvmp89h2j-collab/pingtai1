<template>
  <div class="page">
    <a-card
      title="评论管理"
      :bordered="false"
    >
      <a-form
        layout="inline"
        class="toolbar"
      >
        <a-form-item label="post_id">
          <a-input-number
            v-model:value="query.postId"
            placeholder="post_id"
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="user_id">
          <a-input-number
            v-model:value="query.userId"
            placeholder="user_id"
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

      <a-table
        :data-source="tableData"
        :columns="columns"
        :loading="loading"
        row-key="id"
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

const loading = ref(false)
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
  { title: 'id', dataIndex: 'id', width: 90 },
  { title: 'post_id', dataIndex: 'postId', width: 110 },
  { title: 'user_id', dataIndex: 'userId', width: 110 },
  { title: 'content', dataIndex: 'content', ellipsis: true },
  { title: 'create_time', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 100 }
]

async function loadData() {
  loading.value = true
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

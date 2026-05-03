<template>
  <div class="quiz-question-management">
    <div class="page-header">
      <h2>题库管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增题目
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="题目关键词">
          <a-input
            v-model:value="searchForm.title"
            allow-clear
            placeholder="题目内容"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="分类">
          <a-input
            v-model:value="searchForm.category"
            allow-clear
            placeholder="分类"
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="难度">
          <a-select
            v-model:value="searchForm.difficulty"
            allow-clear
            placeholder="全部"
            style="width: 120px"
            :options="difficultySearchOptions"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            allow-clear
            placeholder="全部"
            style="width: 120px"
            :options="statusSearchOptions"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button
              type="primary"
              @click="handleSearch"
            >
              <template #icon>
                <i class="fas fa-search" />
              </template>搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <i class="fas fa-redo" />
              </template>重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'difficulty'">
            {{ difficultyLabel(record.difficulty) }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                title="确定删除该题目吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button
                  type="link"
                  danger
                  size="small"
                >
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="isModalVisible"
      :title="modalTitle"
      width="720px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item
          label="题目内容"
          required
        >
          <a-textarea
            v-model:value="formData.title"
            :rows="3"
            placeholder="题目内容（最多500字）"
            :maxlength="500"
            show-count
          />
        </a-form-item>
        <a-form-item
          label="选项A"
          required
        >
          <a-input
            v-model:value="formData.optionA"
            placeholder="选项A"
            :maxlength="200"
          />
        </a-form-item>
        <a-form-item
          label="选项B"
          required
        >
          <a-input
            v-model:value="formData.optionB"
            placeholder="选项B"
            :maxlength="200"
          />
        </a-form-item>
        <a-form-item
          label="选项C"
          required
        >
          <a-input
            v-model:value="formData.optionC"
            placeholder="选项C"
            :maxlength="200"
          />
        </a-form-item>
        <a-form-item
          label="选项D"
          required
        >
          <a-input
            v-model:value="formData.optionD"
            placeholder="选项D"
            :maxlength="200"
          />
        </a-form-item>
        <a-form-item
          label="正确答案"
          required
        >
          <a-radio-group
            v-model:value="formData.correctAnswer"
            button-style="solid"
          >
            <a-radio-button value="A">
              A
            </a-radio-button>
            <a-radio-button value="B">
              B
            </a-radio-button>
            <a-radio-button value="C">
              C
            </a-radio-button>
            <a-radio-button value="D">
              D
            </a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="答案解析">
          <a-textarea
            v-model:value="formData.explanation"
            :rows="4"
            placeholder="可选"
          />
        </a-form-item>
        <a-form-item label="题目分类">
          <a-input
            v-model:value="formData.category"
            placeholder="可选"
            :maxlength="100"
          />
        </a-form-item>
        <a-form-item label="难度">
          <a-select
            v-model:value="formData.difficulty"
            style="width: 100%"
            :options="difficultyFormOptions"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">
              启用
            </a-radio>
            <a-radio :value="0">
              禁用
            </a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  getQuizQuestionPage,
  createQuizQuestion,
  updateQuizQuestion,
  deleteQuizQuestion
} from '@/api/QuizQuestionAdminApi'

const difficultySearchOptions = [
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '困难', value: 3 }
]

const difficultyFormOptions = [
  { label: '1 简单', value: 1 },
  { label: '2 中等', value: 2 },
  { label: '3 困难', value: 3 }
]

const statusSearchOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 }
]

const searchForm = reactive({
  title: '',
  category: '',
  difficulty: undefined,
  status: undefined
})

const tableData = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: total => `共 ${total} 条数据`
})

const columns = [
  { title: 'id', dataIndex: 'id', key: 'id', width: 80 },
  { title: '题目内容', dataIndex: 'title', key: 'title', ellipsis: true, width: 260 },
  { title: '正确答案', dataIndex: 'correctAnswer', key: 'correctAnswer', width: 88 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120, ellipsis: true },
  { title: '难度', key: 'difficulty', width: 88 },
  { title: '状态', key: 'status', width: 88 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

function difficultyLabel(v) {
  if (v === 1) return '简单'
  if (v === 2) return '中等'
  if (v === 3) return '困难'
  return v == null ? '—' : String(v)
}

const isModalVisible = ref(false)
const modalTitle = ref('新增题目')
const isEdit = ref(false)
const formData = reactive({
  id: null,
  title: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctAnswer: 'A',
  explanation: '',
  category: '',
  difficulty: 1,
  status: 1
})

const loadData = () => {
  loading.value = true
  getQuizQuestionPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      title: searchForm.title || undefined,
      category: searchForm.category || undefined,
      difficulty: searchForm.difficulty,
      status: searchForm.status
    },
    {
      onSuccess: (data) => {
        tableData.value = data.records || []
        pagination.total = data.total || 0
        loading.value = false
      },
      onError: () => {
        loading.value = false
      }
    }
  )
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.category = ''
  searchForm.difficulty = undefined
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.optionA = ''
  formData.optionB = ''
  formData.optionC = ''
  formData.optionD = ''
  formData.correctAnswer = 'A'
  formData.explanation = ''
  formData.category = ''
  formData.difficulty = 1
  formData.status = 1
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增题目'
  resetForm()
  isModalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑题目'
  Object.assign(formData, {
    id: record.id,
    title: record.title || '',
    optionA: record.optionA || '',
    optionB: record.optionB || '',
    optionC: record.optionC || '',
    optionD: record.optionD || '',
    correctAnswer: record.correctAnswer || 'A',
    explanation: record.explanation || '',
    category: record.category || '',
    difficulty: record.difficulty != null ? record.difficulty : 1,
    status: record.status != null ? record.status : 1
  })
  isModalVisible.value = true
}

const buildPayload = () => ({
  title: formData.title,
  optionA: formData.optionA,
  optionB: formData.optionB,
  optionC: formData.optionC,
  optionD: formData.optionD,
  correctAnswer: formData.correctAnswer,
  explanation: formData.explanation || undefined,
  category: formData.category || undefined,
  difficulty: formData.difficulty,
  status: formData.status
})

const handleModalOk = () => {
  if (!formData.title?.trim()) {
    message.error('请填写题目内容')
    return
  }
  if (!formData.optionA?.trim() || !formData.optionB?.trim() || !formData.optionC?.trim() || !formData.optionD?.trim()) {
    message.error('请填写四个选项')
    return
  }
  const payload = buildPayload()

  if (isEdit.value) {
    updateQuizQuestion(formData.id, payload, {
      onSuccess: () => {
        message.success('更新成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  } else {
    createQuizQuestion(payload, {
      onSuccess: () => {
        message.success('创建成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  }
}

const handleModalCancel = () => {
  isModalVisible.value = false
  resetForm()
}

const handleDelete = (id) => {
  deleteQuizQuestion(id, {
    onSuccess: () => {
      message.success('删除成功')
      loadData()
    },
    successMsg: false
  })
}

onMounted(loadData)
</script>

<style scoped lang="less">
.quiz-question-management {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 4px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.search-section {
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.table-section {
  padding: 24px;
  background: #fff;
  border-radius: 4px;
}
</style>

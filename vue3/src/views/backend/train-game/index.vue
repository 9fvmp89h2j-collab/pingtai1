<template>
  <div class="train-game-management">
    <div class="page-header">
      <h2>小火车关卡管理</h2>
      <a-button type="primary" @click="showCreateModal">
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增关卡
      </a-button>
    </div>

    <div class="search-section">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="经络名称">
          <a-input
            v-model:value="searchForm.jingluoName"
            allow-clear
            placeholder="请输入经络名称"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <i class="fas fa-search" />
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <i class="fas fa-redo" />
              </template>
              重置
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
        :scroll="{ x: 1700 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定删除这条记录吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small">
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
      width="920px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="经络名称" required>
          <a-input v-model:value="formData.jingluoName" placeholder="请输入经络名称" />
        </a-form-item>

        <a-divider orientation="left">结点全称（左侧说明）</a-divider>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="结点1"><a-input v-model:value="formData.game1" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结点2"><a-input v-model:value="formData.game2" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结点3"><a-input v-model:value="formData.game3" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结点4"><a-input v-model:value="formData.game4" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结点5"><a-input v-model:value="formData.game5" /></a-form-item></a-col>
        </a-row>

        <a-divider orientation="left">结点简称（车厢展示）</a-divider>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="简称1"><a-input v-model:value="formData.game1Brief" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="简称2"><a-input v-model:value="formData.game2Brief" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="简称3"><a-input v-model:value="formData.game3Brief" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="简称4"><a-input v-model:value="formData.game4Brief" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="简称5"><a-input v-model:value="formData.game5Brief" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  getTrainGamePage,
  createTrainGame,
  updateTrainGame,
  deleteTrainGame
} from '@/api/TrainGameAdminApi'

const searchForm = reactive({
  jingluoName: ''
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
  { title: 'id', dataIndex: 'id', key: 'id', width: 80, fixed: 'left' },
  { title: 'jingluoname', dataIndex: 'jingluoName', key: 'jingluoName', width: 120, fixed: 'left' },
  { title: 'game1', dataIndex: 'game1', key: 'game1', width: 120 },
  { title: 'game2', dataIndex: 'game2', key: 'game2', width: 120 },
  { title: 'game3', dataIndex: 'game3', key: 'game3', width: 120 },
  { title: 'game4', dataIndex: 'game4', key: 'game4', width: 120 },
  { title: 'game5', dataIndex: 'game5', key: 'game5', width: 120 },
  { title: 'game1brief', dataIndex: 'game1Brief', key: 'game1Brief', width: 120 },
  { title: 'game2brief', dataIndex: 'game2Brief', key: 'game2Brief', width: 120 },
  { title: 'game3brief', dataIndex: 'game3Brief', key: 'game3Brief', width: 120 },
  { title: 'game4brief', dataIndex: 'game4Brief', key: 'game4Brief', width: 120 },
  { title: 'game5brief', dataIndex: 'game5Brief', key: 'game5Brief', width: 120 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增关卡')
const isEdit = ref(false)

const emptyForm = () => ({
  id: null,
  jingluoName: '',
  game1: '',
  game2: '',
  game3: '',
  game4: '',
  game5: '',
  game1Brief: '',
  game2Brief: '',
  game3Brief: '',
  game4Brief: '',
  game5Brief: ''
})
const formData = reactive(emptyForm())

const loadData = () => {
  loading.value = true
  getTrainGamePage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      jingluoName: searchForm.jingluoName || undefined
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
  searchForm.jingluoName = ''
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const resetForm = () => {
  Object.assign(formData, emptyForm())
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增关卡'
  resetForm()
  isModalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑关卡'
  Object.assign(formData, record)
  isModalVisible.value = true
}

const toPayload = () => ({
  jingluoName: formData.jingluoName,
  game1: formData.game1,
  game2: formData.game2,
  game3: formData.game3,
  game4: formData.game4,
  game5: formData.game5,
  game1Brief: formData.game1Brief,
  game2Brief: formData.game2Brief,
  game3Brief: formData.game3Brief,
  game4Brief: formData.game4Brief,
  game5Brief: formData.game5Brief
})

const handleModalOk = () => {
  if (!formData.jingluoName) {
    message.error('请填写经络名称')
    return
  }

  if (isEdit.value) {
    updateTrainGame(formData.id, toPayload(), {
      onSuccess: () => {
        message.success('更新成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  } else {
    createTrainGame(toPayload(), {
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
  deleteTrainGame(id, {
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
.train-game-management {
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

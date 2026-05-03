<template>
  <div class="jingluo-management">
    <div class="page-header">
      <h2>经络管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增经络
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="经络名称">
          <a-input
            v-model:value="searchForm.jingluoName"
            allow-clear
            placeholder="请输入经络名称"
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="分类">
          <a-input
            v-model:value="searchForm.jingluoCatagory"
            allow-clear
            placeholder="请输入分类"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="技能ID">
          <a-input-number
            v-model:value="searchForm.skillId"
            placeholder="技能ID"
            style="width: 160px"
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
        row-key="jingluoId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'jingluoPic'">
            <img
              v-if="record.jingluoPic"
              :src="toImg(record.jingluoPic)"
              class="pic-thumb"
            >
            <span v-else>-</span>
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
                title="确定删除这条记录吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.jingluoId)"
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
      width="820px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item
          label="经络名称"
          required
        >
          <a-input
            v-model:value="formData.jingluoName"
            placeholder="请输入经络名称"
          />
        </a-form-item>
        <a-form-item
          label="经络分类"
          required
        >
          <a-input
            v-model:value="formData.jingluoCatagory"
            placeholder="如：十二经脉、十二经别、奇经八脉"
          />
        </a-form-item>
        <a-form-item label="循行/次序">
          <a-textarea
            v-model:value="formData.jingluoOrder"
            :rows="3"
            placeholder="对应库字段 jingluoorder（腧穴页「定位」）"
          />
        </a-form-item>
        <a-form-item label="主治病症">
          <a-textarea
            v-model:value="formData.illness"
            :rows="4"
            placeholder="请输入主治病症"
          />
        </a-form-item>
        <a-form-item label="技能ID">
          <a-input-number
            v-model:value="formData.skillId"
            style="width: 100%"
            placeholder="可为空"
          />
        </a-form-item>
        <a-form-item label="经络图">
          <a-upload
            v-model:file-list="picList"
            :before-upload="beforeUploadPic"
            :remove="removePic"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="picList.length < 1">
              <i class="fas fa-upload" /><div style="margin-top:8px">
                上传
              </div>
            </div>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { uploadSimpleFile } from '@/api/FileApi'
import { getJingluoPage, createJingluo, updateJingluo, deleteJingluo } from '@/api/JingluoAdminApi'

const searchForm = reactive({
  jingluoName: '',
  jingluoCatagory: '',
  skillId: null
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
  { title: 'jingluoid', dataIndex: 'jingluoId', key: 'jingluoId', width: 100 },
  { title: 'jingluoname', dataIndex: 'jingluoName', key: 'jingluoName', width: 140 },
  { title: 'jingluocatagory', dataIndex: 'jingluoCatagory', key: 'jingluoCatagory', width: 140 },
  { title: 'jingluoorder', dataIndex: 'jingluoOrder', key: 'jingluoOrder', ellipsis: true },
  { title: 'illness', dataIndex: 'illness', key: 'illness', ellipsis: true },
  { title: 'jingluopic', key: 'jingluoPic', width: 110 },
  { title: 'skillid', dataIndex: 'skillId', key: 'skillId', width: 100 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增经络')
const isEdit = ref(false)
const formData = reactive({
  jingluoId: null,
  jingluoName: '',
  jingluoCatagory: '',
  jingluoOrder: '',
  illness: '',
  jingluoPic: '',
  skillId: null
})
const picList = ref([])

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}

const hydratePicList = () => {
  picList.value = formData.jingluoPic
    ? [{ uid: 'pic1', name: '经络图', status: 'done', url: formData.jingluoPic }]
    : []
}

const loadData = () => {
  loading.value = true
  getJingluoPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      jingluoName: searchForm.jingluoName || undefined,
      jingluoCatagory: searchForm.jingluoCatagory || undefined,
      skillId: searchForm.skillId || undefined
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
  searchForm.jingluoCatagory = ''
  searchForm.skillId = null
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const resetForm = () => {
  formData.jingluoId = null
  formData.jingluoName = ''
  formData.jingluoCatagory = ''
  formData.jingluoOrder = ''
  formData.illness = ''
  formData.jingluoPic = ''
  formData.skillId = null
  picList.value = []
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增经络'
  resetForm()
  isModalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑经络'
  Object.assign(formData, record)
  hydratePicList()
  isModalVisible.value = true
}

const beforeUploadPic = async (file) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { message.error('图片大小不能超过 5MB'); return false }
  if (!file.type.startsWith('image/')) { message.error('只能上传图片'); return false }

  await uploadSimpleFile(file, 'JINGLUO', {
    onSuccess: (path) => {
      formData.jingluoPic = path
      hydratePicList()
      message.success('上传成功')
    },
    onError: () => message.error('上传失败'),
    successMsg: false
  })
  return false
}

const removePic = () => {
  formData.jingluoPic = ''
  hydratePicList()
  return true
}

const handleModalOk = () => {
  if (!formData.jingluoName || !formData.jingluoCatagory) {
    message.error('请填写经络名称和分类')
    return
  }
  const payload = {
    jingluoName: formData.jingluoName,
    jingluoCatagory: formData.jingluoCatagory,
    jingluoOrder: formData.jingluoOrder,
    illness: formData.illness,
    jingluoPic: formData.jingluoPic,
    skillId: formData.skillId
  }

  if (isEdit.value) {
    updateJingluo(formData.jingluoId, payload, {
      onSuccess: () => {
        message.success('更新成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  } else {
    createJingluo(payload, {
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
  deleteJingluo(id, {
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
.jingluo-management {
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

.pic-thumb {
  width: 46px;
  height: 46px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}
</style>

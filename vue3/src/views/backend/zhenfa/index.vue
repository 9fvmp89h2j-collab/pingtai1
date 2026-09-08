<template>
  <div class="zhenfa-management">
    <div class="page-header">
      <h2>针法管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增针法
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="名称">
          <a-input
            v-model:value="searchForm.toolsName"
            allow-clear
            placeholder="请输入名称"
            style="width: 220px"
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
        row-key="toolsId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'pics'">
            <a-space>
              <img
                v-for="(p, idx) in picList(record)"
                :key="idx"
                :src="toImg(p)"
                class="pic-thumb"
              >
              <span v-if="picList(record).length === 0">-</span>
            </a-space>
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
                @confirm="handleDelete(record.toolsId)"
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
      width="920px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item
          label="名称"
          required
        >
          <a-input
            v-model:value="formData.toolsName"
            placeholder="请输入名称"
          />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea
            v-model:value="formData.toolsBrief"
            :rows="3"
            placeholder="请输入简介"
          />
        </a-form-item>
        <a-form-item label="第一部分标题">
          <a-input
            v-model:value="formData.toolsTitle1"
            placeholder="请输入第一部分标题"
          />
        </a-form-item>
        <a-form-item label="第一部分内容">
          <a-textarea
            v-model:value="formData.toolsText1"
            :rows="3"
            placeholder="请输入第一部分内容"
          />
        </a-form-item>
        <a-form-item label="第二部分标题">
          <a-input
            v-model:value="formData.toolsTitle2"
            placeholder="请输入第二部分标题"
          />
        </a-form-item>
        <a-form-item label="第二部分内容">
          <a-textarea
            v-model:value="formData.toolsText2"
            :rows="3"
            placeholder="请输入第二部分内容"
          />
        </a-form-item>
        <a-form-item label="第三部分标题">
          <a-input
            v-model:value="formData.toolsTitle3"
            placeholder="请输入第三部分标题"
          />
        </a-form-item>
        <a-form-item label="第三部分内容">
          <a-textarea
            v-model:value="formData.toolsText3"
            :rows="3"
            placeholder="请输入第三部分内容"
          />
        </a-form-item>
        <a-form-item label="技能ID">
          <a-input-number
            v-model:value="formData.skillId"
            style="width: 100%"
            placeholder="可为空"
          />
        </a-form-item>

        <a-form-item label="图片1">
          <a-upload
            v-model:file-list="pic1List"
            :before-upload="(f)=>beforeUploadPic(f,'toolsPic1')"
            :remove="()=>removePic('toolsPic1')"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="pic1List.length < 1">
              <i class="fas fa-upload" /><div style="margin-top:8px">
                上传
              </div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="图片2">
          <a-upload
            v-model:file-list="pic2List"
            :before-upload="(f)=>beforeUploadPic(f,'toolsPic2')"
            :remove="()=>removePic('toolsPic2')"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="pic2List.length < 1">
              <i class="fas fa-upload" /><div style="margin-top:8px">
                上传
              </div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="图片3">
          <a-upload
            v-model:file-list="pic3List"
            :before-upload="(f)=>beforeUploadPic(f,'toolsPic3')"
            :remove="()=>removePic('toolsPic3')"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="pic3List.length < 1">
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
import { getZhenfaPage, createZhenfa, updateZhenfa, deleteZhenfa } from '@/api/ZhenfaAdminApi'

const searchForm = reactive({
  toolsName: '',
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
  { title: 'toolsid', dataIndex: 'toolsId', key: 'toolsId', width: 100 },
  { title: 'toolsname', dataIndex: 'toolsName', key: 'toolsName', width: 160 },
  { title: 'toolsbrief', dataIndex: 'toolsBrief', key: 'toolsBrief', ellipsis: true },
  { title: 'skillid', dataIndex: 'skillId', key: 'skillId', width: 100 },
  { title: '图片', key: 'pics', width: 220 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增针法')
const isEdit = ref(false)
const formData = reactive({
  toolsId: null,
  toolsName: '',
  toolsBrief: '',
  toolsTitle1: '',
  toolsText1: '',
  toolsTitle2: '',
  toolsText2: '',
  toolsTitle3: '',
  toolsText3: '',
  skillId: null,
  toolsPic1: '',
  toolsPic2: '',
  toolsPic3: ''
})

const pic1List = ref([])
const pic2List = ref([])
const pic3List = ref([])

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}
const picList = (record) => [record.toolsPic1, record.toolsPic2, record.toolsPic3].filter(Boolean)

const hydrateUploadLists = () => {
  pic1List.value = formData.toolsPic1 ? [{ uid: 'p1', name: '图片1', status: 'done', url: formData.toolsPic1 }] : []
  pic2List.value = formData.toolsPic2 ? [{ uid: 'p2', name: '图片2', status: 'done', url: formData.toolsPic2 }] : []
  pic3List.value = formData.toolsPic3 ? [{ uid: 'p3', name: '图片3', status: 'done', url: formData.toolsPic3 }] : []
}

const loadData = () => {
  loading.value = true
  getZhenfaPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      toolsName: searchForm.toolsName || undefined,
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
  searchForm.toolsName = ''
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
  formData.toolsId = null
  formData.toolsName = ''
  formData.toolsBrief = ''
  formData.toolsTitle1 = ''
  formData.toolsText1 = ''
  formData.toolsTitle2 = ''
  formData.toolsText2 = ''
  formData.toolsTitle3 = ''
  formData.toolsText3 = ''
  formData.skillId = null
  formData.toolsPic1 = ''
  formData.toolsPic2 = ''
  formData.toolsPic3 = ''
  pic1List.value = []
  pic2List.value = []
  pic3List.value = []
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增针法'
  resetForm()
  isModalVisible.value = true
}
const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑针法'
  Object.assign(formData, record)
  hydrateUploadLists()
  isModalVisible.value = true
}

const beforeUploadPic = async (file, field) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { message.error('图片大小不能超过 5MB'); return false }
  if (!file.type.startsWith('image/')) { message.error('只能上传图片'); return false }
  await uploadSimpleFile(file, 'ZHENFA', {
    onSuccess: (path) => {
      formData[field] = path
      hydrateUploadLists()
      message.success('上传成功')
    },
    onError: () => message.error('上传失败'),
    successMsg: false
  })
  return false
}
const removePic = (field) => {
  formData[field] = ''
  hydrateUploadLists()
  return true
}

const handleModalOk = () => {
  if (!formData.toolsName) {
    message.error('请填写名称')
    return
  }
  const payload = {
    toolsName: formData.toolsName,
    toolsBrief: formData.toolsBrief,
    toolsTitle1: formData.toolsTitle1,
    toolsText1: formData.toolsText1,
    toolsTitle2: formData.toolsTitle2,
    toolsText2: formData.toolsText2,
    toolsTitle3: formData.toolsTitle3,
    toolsText3: formData.toolsText3,
    skillId: formData.skillId,
    toolsPic1: formData.toolsPic1,
    toolsPic2: formData.toolsPic2,
    toolsPic3: formData.toolsPic3
  }
  if (isEdit.value) {
    updateZhenfa(formData.toolsId, payload, {
      onSuccess: () => {
        message.success('更新成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  } else {
    createZhenfa(payload, {
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
  deleteZhenfa(id, {
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
.zhenfa-management {
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
  h2 { margin: 0; font-size: 20px; font-weight: 500; }
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


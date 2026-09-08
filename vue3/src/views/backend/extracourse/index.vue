<template>
  <div class="extracourse-management">
    <div class="page-header">
      <h2>拓展疗法管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增拓展疗法
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="名称">
          <a-input
            v-model:value="searchForm.name"
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
        row-key="extraCourseId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'icon'">
            <img
              v-if="record.extraCourseIcon"
              :src="toImg(record.extraCourseIcon)"
              class="pic-thumb"
            >
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'pics'">
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
                @confirm="handleDelete(record.extraCourseId)"
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
            v-model:value="formData.extraCourseName"
            placeholder="请输入名称"
          />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea
            v-model:value="formData.extraCourseBrief"
            :rows="3"
            placeholder="请输入简介"
          />
        </a-form-item>
        <a-form-item label="详细介绍">
          <a-textarea
            v-model:value="formData.extraCourseDes"
            :rows="5"
            placeholder="请输入详细介绍"
          />
        </a-form-item>
        <a-form-item label="技能ID">
          <a-input-number
            v-model:value="formData.skillId"
            style="width: 100%"
            placeholder="可为空"
          />
        </a-form-item>

        <a-form-item label="图标">
          <a-upload
            v-model:file-list="iconList"
            :before-upload="(f)=>beforeUploadPic(f,'extraCourseIcon')"
            :remove="()=>removePic('extraCourseIcon')"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="iconList.length < 1">
              <i class="fas fa-upload" /><div style="margin-top:8px">
                上传
              </div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="配图1">
          <a-upload
            v-model:file-list="pic1List"
            :before-upload="(f)=>beforeUploadPic(f,'extraCoursePic1')"
            :remove="()=>removePic('extraCoursePic1')"
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
        <a-form-item label="配图2">
          <a-upload
            v-model:file-list="pic2List"
            :before-upload="(f)=>beforeUploadPic(f,'extraCoursePic2')"
            :remove="()=>removePic('extraCoursePic2')"
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
        <a-form-item label="配图3">
          <a-upload
            v-model:file-list="pic3List"
            :before-upload="(f)=>beforeUploadPic(f,'extraCoursePic3')"
            :remove="()=>removePic('extraCoursePic3')"
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
import { getExtraCoursePage, createExtraCourse, updateExtraCourse, deleteExtraCourse } from '@/api/ExtraCourseAdminApi'

const searchForm = reactive({ name: '', skillId: null })

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
  { title: 'extracourseid', dataIndex: 'extraCourseId', key: 'extraCourseId', width: 120 },
  { title: '名称', dataIndex: 'extraCourseName', key: 'extraCourseName', width: 160 },
  { title: '简介', dataIndex: 'extraCourseBrief', key: 'extraCourseBrief', ellipsis: true },
  { title: 'skillid', dataIndex: 'skillId', key: 'skillId', width: 100 },
  { title: '图标', key: 'icon', width: 110 },
  { title: '配图', key: 'pics', width: 220 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增拓展疗法')
const isEdit = ref(false)
const formData = reactive({
  extraCourseId: null,
  extraCourseName: '',
  extraCourseBrief: '',
  extraCourseDes: '',
  extraCourseIcon: '',
  extraCoursePic1: '',
  extraCoursePic2: '',
  extraCoursePic3: '',
  skillId: null
})

const iconList = ref([])
const pic1List = ref([])
const pic2List = ref([])
const pic3List = ref([])

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}
const picList = (record) => [record.extraCoursePic1, record.extraCoursePic2, record.extraCoursePic3].filter(Boolean)

const hydrateUploadLists = () => {
  iconList.value = formData.extraCourseIcon ? [{ uid: 'i', name: '图标', status: 'done', url: formData.extraCourseIcon }] : []
  pic1List.value = formData.extraCoursePic1 ? [{ uid: 'p1', name: '配图1', status: 'done', url: formData.extraCoursePic1 }] : []
  pic2List.value = formData.extraCoursePic2 ? [{ uid: 'p2', name: '配图2', status: 'done', url: formData.extraCoursePic2 }] : []
  pic3List.value = formData.extraCoursePic3 ? [{ uid: 'p3', name: '配图3', status: 'done', url: formData.extraCoursePic3 }] : []
}

const loadData = () => {
  loading.value = true
  getExtraCoursePage(
    { current: pagination.current, size: pagination.pageSize, name: searchForm.name || undefined, skillId: searchForm.skillId || undefined },
    {
      onSuccess: (data) => {
        tableData.value = data.records || []
        pagination.total = data.total || 0
        loading.value = false
      },
      onError: () => { loading.value = false }
    }
  )
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchForm.name = ''; searchForm.skillId = null; pagination.current = 1; loadData() }
const handleTableChange = (pag) => { pagination.current = pag.current; pagination.pageSize = pag.pageSize; loadData() }

const resetForm = () => {
  formData.extraCourseId = null
  formData.extraCourseName = ''
  formData.extraCourseBrief = ''
  formData.extraCourseDes = ''
  formData.extraCourseIcon = ''
  formData.extraCoursePic1 = ''
  formData.extraCoursePic2 = ''
  formData.extraCoursePic3 = ''
  formData.skillId = null
  iconList.value = []
  pic1List.value = []
  pic2List.value = []
  pic3List.value = []
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增拓展疗法'
  resetForm()
  isModalVisible.value = true
}
const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑拓展疗法'
  Object.assign(formData, record)
  hydrateUploadLists()
  isModalVisible.value = true
}

const beforeUploadPic = async (file, field) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { message.error('图片大小不能超过 5MB'); return false }
  if (!file.type.startsWith('image/')) { message.error('只能上传图片'); return false }
  await uploadSimpleFile(file, 'EXTRACOURSE', {
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
  if (!formData.extraCourseName) { message.error('请填写名称'); return }
  const payload = {
    extraCourseName: formData.extraCourseName,
    extraCourseBrief: formData.extraCourseBrief,
    extraCourseDes: formData.extraCourseDes,
    extraCourseIcon: formData.extraCourseIcon,
    extraCoursePic1: formData.extraCoursePic1,
    extraCoursePic2: formData.extraCoursePic2,
    extraCoursePic3: formData.extraCoursePic3,
    skillId: formData.skillId
  }
  if (isEdit.value) {
    updateExtraCourse(formData.extraCourseId, payload, { onSuccess: () => { message.success('更新成功'); isModalVisible.value = false; loadData() }, successMsg: false })
  } else {
    createExtraCourse(payload, { onSuccess: () => { message.success('创建成功'); isModalVisible.value = false; loadData() }, successMsg: false })
  }
}
const handleModalCancel = () => { isModalVisible.value = false; resetForm() }
const handleDelete = (id) => { deleteExtraCourse(id, { onSuccess: () => { message.success('删除成功'); loadData() }, successMsg: false }) }

onMounted(loadData)
</script>

<style scoped lang="less">
.extracourse-management { padding: 24px; background: #f0f2f5; min-height: 100vh; }
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:24px; padding:16px 24px; background:#fff; border-radius:4px;
  h2 { margin:0; font-size:20px; font-weight:500; } }
.search-section { padding:24px; background:#fff; border-radius:4px; margin-bottom:16px; }
.table-section { padding:24px; background:#fff; border-radius:4px; }
.pic-thumb { width:46px; height:46px; object-fit:cover; border-radius:4px; border:1px solid #f0f0f0; }
</style>


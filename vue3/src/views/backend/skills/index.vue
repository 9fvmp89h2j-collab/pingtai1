<template>
  <div class="skills-management">
    <div class="page-header">
      <h2>技能点管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增技能
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="技能名称">
          <a-input
            v-model:value="searchForm.skillName"
            allow-clear
            placeholder="请输入技能名称"
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="类别">
          <a-input
            v-model:value="searchForm.skillCategory"
            allow-clear
            placeholder="请输入类别"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="所属板块">
          <a-input
            v-model:value="searchForm.skillType"
            allow-clear
            placeholder="请输入所属板块"
            style="width: 200px"
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
        row-key="skillId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'skillPic'">
            <img
              v-if="record.skillPic"
              :src="toImg(record.skillPic)"
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
                @confirm="handleDelete(record.skillId)"
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
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="skillid"
              required
            >
              <a-input-number
                v-model:value="formData.skillId"
                style="width: 100%"
                :disabled="isEdit"
                placeholder="请输入 skillid（必须唯一）"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="技能名称"
              required
            >
              <a-input
                v-model:value="formData.skillName"
                placeholder="请输入技能名称"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="类别">
              <a-input
                v-model:value="formData.skillCategory"
                placeholder="请输入类别（skillcategory）"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="所属板块">
              <a-input
                v-model:value="formData.skillType"
                placeholder="请输入所属板块（skilltype）"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="积分">
              <a-input
                v-model:value="formData.skillScore"
                placeholder="例如：10"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图片">
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
          </a-col>
        </a-row>

        <a-form-item label="简介">
          <a-textarea
            v-model:value="formData.skillBriefDescription"
            :rows="3"
            placeholder="请输入简介"
          />
        </a-form-item>
        <a-form-item label="详细介绍">
          <a-textarea
            v-model:value="formData.skillDescription"
            :rows="5"
            placeholder="请输入详细介绍"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { uploadSimpleFile } from '@/api/FileApi'
import { createSkill, deleteSkill, pageSkills, updateSkill } from '@/api/SkillAdminApi'

const searchForm = reactive({
  skillName: '',
  skillCategory: '',
  skillType: ''
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
  { title: 'skillid', dataIndex: 'skillId', key: 'skillId', width: 110 },
  { title: 'skillname', dataIndex: 'skillName', key: 'skillName', width: 180, ellipsis: true },
  { title: 'skillcategory', dataIndex: 'skillCategory', key: 'skillCategory', width: 160, ellipsis: true },
  { title: 'skilltype', dataIndex: 'skillType', key: 'skillType', width: 160, ellipsis: true },
  { title: 'skillscore', dataIndex: 'skillScore', key: 'skillScore', width: 110 },
  { title: 'skillpic', key: 'skillPic', width: 110 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增技能')
const isEdit = ref(false)
const formData = reactive({
  skillId: null,
  skillName: '',
  skillBriefDescription: '',
  skillDescription: '',
  skillPic: '',
  skillCategory: '',
  skillScore: '',
  skillType: ''
})

const picList = ref([])

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}

const hydratePicList = () => {
  picList.value = formData.skillPic
    ? [{ uid: 'skillPic', name: '技能图', status: 'done', url: formData.skillPic }]
    : []
}

const beforeUploadPic = async (file) => {
  const isImg = file?.type?.startsWith('image/')
  if (!isImg) {
    message.error('只能上传图片文件')
    return false
  }
  try {
    const url = await uploadSimpleFile(file, 'SKILLS', { showDefaultMsg: false })
    if (!url) throw new Error('no url')
    formData.skillPic = url
    hydratePicList()
    message.success('上传成功')
  } catch {
    message.error('上传失败')
  }
  return false
}

const removePic = () => {
  formData.skillPic = ''
  picList.value = []
  return true
}

const loadData = () => {
  loading.value = true
  pageSkills(
    {
      current: pagination.current,
      size: pagination.pageSize,
      skillName: searchForm.skillName || undefined,
      skillCategory: searchForm.skillCategory || undefined,
      skillType: searchForm.skillType || undefined
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
  searchForm.skillName = ''
  searchForm.skillCategory = ''
  searchForm.skillType = ''
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const resetForm = () => {
  formData.skillId = null
  formData.skillName = ''
  formData.skillBriefDescription = ''
  formData.skillDescription = ''
  formData.skillPic = ''
  formData.skillCategory = ''
  formData.skillScore = ''
  formData.skillType = ''
  picList.value = []
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增技能'
  resetForm()
  isModalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑技能'
  Object.assign(formData, record)
  hydratePicList()
  isModalVisible.value = true
}

const handleModalOk = async () => {
  if (!formData.skillId) {
    message.error('请输入 skillid')
    return
  }
  if (!formData.skillName) {
    message.error('请输入技能名称')
    return
  }
  try {
    if (isEdit.value) {
      await updateSkill(formData.skillId, { ...formData })
      message.success('更新成功')
    } else {
      await createSkill({ ...formData })
      message.success('新增成功')
    }
    isModalVisible.value = false
    loadData()
  } catch {
    message.error('保存失败')
  }
}

const handleModalCancel = () => {
  isModalVisible.value = false
}

const handleDelete = async (id) => {
  try {
    await deleteSkill(id)
    message.success('删除成功')
    loadData()
  } catch {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.skills-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 20px;
  font-weight: 600;
}

.search-section {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.table-section {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
}

.pic-thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.15);
  background: #f5f5f5;
}
</style>


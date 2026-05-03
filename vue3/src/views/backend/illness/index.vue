<template>
  <div class="page">
    <a-card
      title="疾病管理"
      :bordered="false"
    >
      <a-form
        layout="inline"
        class="toolbar"
      >
        <a-form-item label="小镇名称">
          <a-input
            v-model:value="query.cowtown"
            placeholder="请输入小镇名称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="疾病名称">
          <a-input
            v-model:value="query.illnessname"
            placeholder="请输入疾病名称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button
              type="primary"
              @click="loadData"
            >
              查询
            </a-button>
            <a-button @click="resetQuery">
              重置
            </a-button>
          </a-space>
        </a-form-item>
        <div style="flex: 1" />
        <a-button
          type="primary"
          @click="openCreate"
        >
          新增疾病
        </a-button>
      </a-form>

      <a-table
        :data-source="tableData"
        :columns="columns"
        :loading="loading"
        row-key="illnessid"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'illnesspic'">
            <img
              v-if="record.illnesspic"
              :src="toImg(record.illnesspic)"
              class="pic-thumb"
            >
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a-popconfirm
                title="确认删除该疾病吗？"
                @confirm="onDelete(record)"
              >
                <a style="color: #ff4d4f">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="modalTitle"
      :confirm-loading="saving"
      width="880px"
      destroy-on-close
      @ok="onSubmit"
      @cancel="onCancel"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="小镇名称"
              name="cowtown"
            >
              <a-input
                v-model:value="form.cowtown"
                placeholder="请输入小镇名称"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="疾病名称"
              name="illnessname"
            >
              <a-input
                v-model:value="form.illnessname"
                placeholder="请输入疾病名称"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item
          label="疾病表现"
          name="illnessfeature"
        >
          <a-textarea
            v-model:value="form.illnessfeature"
            placeholder="请输入疾病表现"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="疾病图片">
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

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="主穴数量"
              name="xueweicount"
            >
              <a-input
                v-model:value="form.xueweicount"
                placeholder="例如：3"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="手法数量"
              name="toolscount"
            >
              <a-input
                v-model:value="form.toolscount"
                placeholder="例如：2"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">
          主穴技能ID（xuewei1-5）
        </a-divider>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item
              label="xuewei1"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.xuewei1" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="xuewei2"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.xuewei2" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="xuewei3"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.xuewei3" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="xuewei4"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.xuewei4" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="xuewei5"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.xuewei5" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">
          手法技能ID（tools1-4）
        </a-divider>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item
              label="tools1"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.tools1" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="tools2"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.tools2" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="tools3"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.tools3" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item
              label="tools4"
              :label-col="{ span: 10 }"
              :wrapper-col="{ span: 14 }"
            >
              <a-input v-model:value="form.tools4" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createIllness, deleteIllness, pageIllness, updateIllness } from '@/api/IllnessAdminApi'
import { uploadSimpleFile } from '@/api/FileApi'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])

const query = reactive({
  cowtown: '',
  illnessname: ''
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
  { title: 'ID', dataIndex: 'illnessid', width: 80 },
  { title: '小镇名称', dataIndex: 'cowtown', ellipsis: true },
  { title: '疾病名称', dataIndex: 'illnessname', ellipsis: true },
  { title: '疾病表现', dataIndex: 'illnessfeature', ellipsis: true },
  { title: '图片', key: 'illnesspic', width: 110 },
  { title: '主穴数量', dataIndex: 'xueweicount', width: 100 },
  { title: '手法数量', dataIndex: 'toolscount', width: 100 },
  { title: '操作', key: 'actions', fixed: 'right', width: 140 }
]

const modalOpen = ref(false)
const modalTitle = ref('新增疾病')
const editingId = ref(null)
const formRef = ref()

const emptyForm = () => ({
  cowtown: '',
  illnessname: '',
  illnessfeature: '',
  illnesspic: '',
  xueweicount: '',
  toolscount: '',
  xuewei1: '',
  xuewei2: '',
  xuewei3: '',
  xuewei4: '',
  xuewei5: '',
  tools1: '',
  tools2: '',
  tools3: '',
  tools4: ''
})

const form = reactive(emptyForm())

const picList = ref([])

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}

const hydratePicList = () => {
  picList.value = form.illnesspic
    ? [{ uid: 'illnesspic', name: '疾病图片', status: 'done', url: form.illnesspic }]
    : []
}

const beforeUploadPic = async (file) => {
  const isImg = file?.type?.startsWith('image/')
  if (!isImg) {
    message.error('只能上传图片文件')
    return false
  }
  try {
    const url = await uploadSimpleFile(file, 'ILLNESS', { showDefaultMsg: false })
    if (!url) throw new Error('no url')
    form.illnesspic = url
    hydratePicList()
    message.success('上传成功')
  } catch (e) {
    message.error('上传失败')
  }
  return false
}

const removePic = () => {
  form.illnesspic = ''
  picList.value = []
  return true
}

const rules = {
  cowtown: [{ required: true, message: '请输入小镇名称', trigger: 'blur' }],
  illnessname: [{ required: true, message: '请输入疾病名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const page = await pageIllness({
      current: pagination.current,
      size: pagination.pageSize,
      cowtown: query.cowtown || undefined,
      illnessname: query.illnessname || undefined
    })
    tableData.value = page?.records || []
    pagination.total = page?.total || 0
  } catch (e) {
    message.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.cowtown = ''
  query.illnessname = ''
  pagination.current = 1
  loadData()
}

function handleTableChange(p) {
  pagination.current = p.current
  pagination.pageSize = p.pageSize
  loadData()
}

function openCreate() {
  modalTitle.value = '新增疾病'
  editingId.value = null
  Object.assign(form, emptyForm())
  hydratePicList()
  modalOpen.value = true
}

function openEdit(record) {
  modalTitle.value = '编辑疾病'
  editingId.value = record.illnessid
  Object.assign(form, {
    cowtown: record.cowtown || '',
    illnessname: record.illnessname || '',
    illnessfeature: record.illnessfeature || '',
    illnesspic: record.illnesspic || '',
    xueweicount: record.xueweicount || '',
    toolscount: record.toolscount || '',
    xuewei1: record.xuewei1 || '',
    xuewei2: record.xuewei2 || '',
    xuewei3: record.xuewei3 || '',
    xuewei4: record.xuewei4 || '',
    xuewei5: record.xuewei5 || '',
    tools1: record.tools1 || '',
    tools2: record.tools2 || '',
    tools3: record.tools3 || '',
    tools4: record.tools4 || ''
  })
  hydratePicList()
  modalOpen.value = true
}

async function onSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateIllness(editingId.value, { ...form })
      message.success('更新成功')
    } else {
      await createIllness({ ...form })
      message.success('新增成功')
    }
    modalOpen.value = false
    loadData()
  } catch (e) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

function onCancel() {
  modalOpen.value = false
}

async function onDelete(record) {
  try {
    await deleteIllness(record.illnessid)
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

.pic-thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.15);
  background: #f5f5f5;
}
</style>


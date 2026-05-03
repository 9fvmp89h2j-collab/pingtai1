<template>
  <div class="doctor-story-management">
    <div class="page-header">
      <h2>针灸名医管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增名医故事
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="故事名称">
          <a-input
            v-model:value="searchForm.doctorName"
            allow-clear
            placeholder="请输入故事名称"
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
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preview'">
            <img
              v-if="record.previewPic"
              :src="toImg(record.previewPic)"
              class="cover-thumb"
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
          <template v-else-if="column.key === 'media'">
            <span
              class="media-cell"
              :class="{ 'is-ready': record.media }"
            >
              {{ record.media ? '已上传视频' : '-' }}
            </span>
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
      width="860px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item
          label="故事名称"
          required
        >
          <a-input
            v-model:value="formData.doctorName"
            placeholder="请输入故事名称"
          />
        </a-form-item>
        <a-form-item label="故事简介">
          <a-textarea
            v-model:value="formData.doctorBrief"
            :rows="3"
            placeholder="请输入故事简介"
          />
        </a-form-item>
        <a-form-item label="故事详解">
          <a-textarea
            v-model:value="formData.doctorDetail"
            :rows="5"
            placeholder="请输入故事详解"
          />
        </a-form-item>
        <a-form-item label="技能ID">
          <a-input-number
            v-model:value="formData.skillId"
            style="width: 100%"
            placeholder="可为空"
          />
        </a-form-item>
        <a-form-item label="预览封面">
          <div class="upload-block">
            <a-upload
              v-model:file-list="previewList"
              :before-upload="(f)=>beforeUploadPic(f,'previewPic')"
              :remove="()=>removePic('previewPic')"
              accept="image/*"
              :max-count="1"
              list-type="picture-card"
              :show-upload-list="{ showPreviewIcon: false }"
            >
              <div v-if="previewList.length < 1">
                <i class="fas fa-upload" /><div style="margin-top:8px">
                  上传
                </div>
              </div>
            </a-upload>
            <div class="upload-help">
              用在前台“名医故事”卡片的大封面。
            </div>
          </div>
        </a-form-item>
        <a-form-item label="重点词词库">
          <div class="glossary-editor">
            <div class="glossary-toolbar">
              <span class="glossary-tip">给孩子阅读时需要解释的词，补上拼音和儿童版解释。</span>
              <a-button
                type="dashed"
                @click="addGlossaryItem"
              >
                <template #icon>
                  <i class="fas fa-plus" />
                </template>
                添加重点词
              </a-button>
            </div>

            <div
              v-if="!formData.readingGlossary.length"
              class="glossary-empty"
            >
              暂未添加重点词。
            </div>

            <div
              v-for="(item, index) in formData.readingGlossary"
              :key="`glossary-${index}`"
              class="glossary-item"
            >
              <div class="glossary-row">
                <a-input
                  v-model:value="item.word"
                  placeholder="词语，例如 扁鹊"
                />
                <a-input
                  v-model:value="item.pinyin"
                  placeholder="拼音，例如 biǎn què"
                />
              </div>
              <div class="glossary-row glossary-row-bottom">
                <a-textarea
                  v-model:value="item.meaning"
                  :rows="2"
                  placeholder="儿童版解释"
                />
                <a-button
                  danger
                  @click="removeGlossaryItem(index)"
                >
                  删除
                </a-button>
              </div>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="图片1">
          <a-upload
            v-model:file-list="pic1List"
            :before-upload="(f)=>beforeUploadPic(f,'doctorPic1')"
            :remove="()=>removePic('doctorPic1')"
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
            :before-upload="(f)=>beforeUploadPic(f,'doctorPic2')"
            :remove="()=>removePic('doctorPic2')"
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
            :before-upload="(f)=>beforeUploadPic(f,'doctorPic3')"
            :remove="()=>removePic('doctorPic3')"
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
        <a-form-item label="故事视频">
          <div class="upload-block">
            <a-upload
              v-model:file-list="mediaList"
              :before-upload="beforeUploadVideo"
              :remove="removeMedia"
              accept="video/*"
              :max-count="1"
              list-type="text"
              :show-upload-list="{ showPreviewIcon: false, showDownloadIcon: false }"
            >
              <a-button v-if="mediaList.length < 1">
                <template #icon>
                  <i class="fas fa-upload" />
                </template>
                上传视频
              </a-button>
            </a-upload>
            <div class="upload-help">
              可选。没有配图时，前台也会单独显示这个视频。
            </div>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { uploadSimpleFile } from '@/api/FileApi'
import { getDoctorStoryPage, createDoctorStory, updateDoctorStory, deleteDoctorStory } from '@/api/DoctorStoryAdminApi'

const searchForm = reactive({
  doctorName: '',
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
  { title: 'doctorid', dataIndex: 'id', key: 'id', width: 100 },
  { title: '故事名称', dataIndex: 'doctorName', key: 'doctorName' },
  { title: '预览封面', key: 'preview', width: 90 },
  { title: '故事简介', dataIndex: 'doctorBrief', key: 'doctorBrief', ellipsis: true },
  { title: 'skillid', dataIndex: 'skillId', key: 'skillId', width: 100 },
  { title: '视频', key: 'media', width: 110 },
  { title: '图片', key: 'pics', width: 220 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增名医故事')
const isEdit = ref(false)
const formData = reactive({
  id: null,
  doctorName: '',
  doctorBrief: '',
  doctorDetail: '',
  skillId: null,
  previewPic: '',
  readingGlossary: [],
  doctorPic1: '',
  doctorPic2: '',
  doctorPic3: '',
  media: ''
})

const previewList = ref([])
const pic1List = ref([])
const pic2List = ref([])
const pic3List = ref([])
const mediaList = ref([])

const createEmptyGlossaryItem = () => ({
  word: '',
  pinyin: '',
  meaning: ''
})

const cloneGlossaryItems = (items = []) => {
  if (!Array.isArray(items)) return []
  return items.map((item) => ({
    word: item?.word || '',
    pinyin: item?.pinyin || '',
    meaning: item?.meaning || ''
  }))
}

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}

const extractFileName = (path) => {
  const normalized = String(path || '').replace(/\\/g, '/')
  return normalized.split('/').pop() || '已上传文件'
}

const createUploadListItem = (uid, name, url) => (
  url
    ? [{ uid, name, status: 'done', url: toImg(url) }]
    : []
)

const picList = (record) => [record.doctorPic1, record.doctorPic2, record.doctorPic3].filter(Boolean)

const loadData = () => {
  loading.value = true
  getDoctorStoryPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      doctorName: searchForm.doctorName || undefined,
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
  searchForm.doctorName = ''
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
  formData.id = null
  formData.doctorName = ''
  formData.doctorBrief = ''
  formData.doctorDetail = ''
  formData.skillId = null
  formData.previewPic = ''
  formData.readingGlossary = []
  formData.doctorPic1 = ''
  formData.doctorPic2 = ''
  formData.doctorPic3 = ''
  formData.media = ''
  previewList.value = []
  pic1List.value = []
  pic2List.value = []
  pic3List.value = []
  mediaList.value = []
}

const hydrateUploadLists = () => {
  previewList.value = createUploadListItem('preview', '预览封面', formData.previewPic)
  pic1List.value = formData.doctorPic1 ? [{ uid: 'p1', name: '图片1', status: 'done', url: formData.doctorPic1 }] : []
  pic2List.value = formData.doctorPic2 ? [{ uid: 'p2', name: '图片2', status: 'done', url: formData.doctorPic2 }] : []
  pic3List.value = formData.doctorPic3 ? [{ uid: 'p3', name: '图片3', status: 'done', url: formData.doctorPic3 }] : []
  mediaList.value = createUploadListItem('media', extractFileName(formData.media), formData.media)
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增名医故事'
  resetForm()
  addGlossaryItem()
  isModalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑名医故事'
  formData.id = record.id ?? null
  formData.doctorName = record.doctorName || ''
  formData.doctorBrief = record.doctorBrief || ''
  formData.doctorDetail = record.doctorDetail || ''
  formData.skillId = record.skillId ?? null
  formData.previewPic = record.previewPic || ''
  formData.readingGlossary = cloneGlossaryItems(record.readingGlossary)
  formData.doctorPic1 = record.doctorPic1 || ''
  formData.doctorPic2 = record.doctorPic2 || ''
  formData.doctorPic3 = record.doctorPic3 || ''
  formData.media = record.media || ''
  if (!formData.readingGlossary.length) {
    addGlossaryItem()
  }
  hydrateUploadLists()
  isModalVisible.value = true
}

const addGlossaryItem = () => {
  formData.readingGlossary.push(createEmptyGlossaryItem())
}

const removeGlossaryItem = (index) => {
  formData.readingGlossary.splice(index, 1)
}

const beforeUploadPic = async (file, field) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { message.error('图片大小不能超过 5MB'); return false }
  if (!file.type.startsWith('image/')) { message.error('只能上传图片'); return false }

  await uploadSimpleFile(file, 'START', {
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

const beforeUploadVideo = async (file) => {
  const isLt80M = file.size / 1024 / 1024 < 80
  if (!isLt80M) { message.error('视频大小不能超过 80MB'); return false }
  if (!file.type.startsWith('video/')) { message.error('只能上传视频'); return false }

  await uploadSimpleFile(file, 'VIDEO', {
    onSuccess: (path) => {
      formData.media = path
      hydrateUploadLists()
      message.success('视频上传成功')
    },
    onError: () => message.error('视频上传失败'),
    successMsg: false
  })
  return false
}

const removePic = (field) => {
  formData[field] = ''
  hydrateUploadLists()
  return true
}

const removeMedia = () => {
  formData.media = ''
  hydrateUploadLists()
  return true
}

const handleModalOk = () => {
  if (!formData.doctorName) {
    message.error('请填写故事名称')
    return
  }

  const readingGlossary = cloneGlossaryItems(formData.readingGlossary)
    .filter(item => item.word || item.pinyin || item.meaning)

  const invalidGlossaryIndex = readingGlossary.findIndex(item => !item.word || !item.pinyin || !item.meaning)
  if (invalidGlossaryIndex !== -1) {
    message.error(`第 ${invalidGlossaryIndex + 1} 个重点词请完整填写词语、拼音和解释`)
    return
  }

  const payload = {
    doctorName: formData.doctorName,
    doctorBrief: formData.doctorBrief,
    doctorDetail: formData.doctorDetail,
    skillId: formData.skillId,
    previewPic: formData.previewPic,
    readingGlossary,
    doctorPic1: formData.doctorPic1,
    doctorPic2: formData.doctorPic2,
    doctorPic3: formData.doctorPic3,
    media: formData.media
  }

  if (isEdit.value) {
    updateDoctorStory(formData.id, payload, {
      onSuccess: () => {
        message.success('更新成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  } else {
    createDoctorStory(payload, {
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
  deleteDoctorStory(id, {
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
.doctor-story-management {
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

.glossary-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-help {
  color: #666;
  line-height: 1.5;
}

.glossary-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.glossary-tip {
  color: #666;
  line-height: 1.5;
}

.glossary-empty {
  padding: 12px 14px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  color: #999;
  background: #fafafa;
}

.glossary-item {
  padding: 14px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafcff;
}

.glossary-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.glossary-row + .glossary-row {
  margin-top: 12px;
}

.glossary-row-bottom {
  grid-template-columns: 1fr auto;
  align-items: start;
}

.pic-thumb {
  width: 46px;
  height: 46px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.cover-thumb {
  width: 62px;
  height: 46px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.media-cell {
  color: #999;
}

.media-cell.is-ready {
  color: #237804;
  font-weight: 600;
}

@media (max-width: 768px) {
  .glossary-row,
  .glossary-row-bottom {
    grid-template-columns: 1fr;
  }
}
</style>


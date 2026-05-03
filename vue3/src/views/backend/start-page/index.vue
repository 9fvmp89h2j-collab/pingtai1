<template>
  <div class="start-page-management">
    <div class="page-header">
      <h2>开始页管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增故事
      </a-button>
    </div>

    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="标题">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入标题关键词"
            allow-clear
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
          <template v-else-if="column.key === 'media'">
            <a-space>
              <a-tag v-if="record.media" color="blue">有</a-tag>
              <span v-else>-</span>
              <a-button
                v-if="record.media"
                type="link"
                size="small"
                @click="openMediaPreview(record.media)"
              >
                预览
              </a-button>
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
                title="确定删除这条故事吗？"
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
      width="820px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item
          label="标题"
          required
        >
          <a-input
            v-model:value="formData.storyTitle"
            placeholder="请输入标题"
          />
        </a-form-item>
        <a-form-item label="副标题">
          <a-input
            v-model:value="formData.storySubtitle"
            placeholder="请输入副标题"
          />
        </a-form-item>
        <a-form-item label="正文">
          <a-textarea
            v-model:value="formData.storyText"
            :rows="5"
            placeholder="请输入正文"
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
            :before-upload="(f)=>beforeUploadPic(f,'storyPic1')"
            :remove="()=>removePic('storyPic1')"
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
            :before-upload="(f)=>beforeUploadPic(f,'storyPic2')"
            :remove="()=>removePic('storyPic2')"
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
            :before-upload="(f)=>beforeUploadPic(f,'storyPic3')"
            :remove="()=>removePic('storyPic3')"
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

        <a-form-item label="媒体文件">
          <a-upload
            v-model:file-list="mediaList"
            :before-upload="beforeUploadMedia"
            :remove="removeMedia"
            accept="video/*"
            :max-count="1"
          >
            <a-button>
              <i class="fas fa-upload" />
              上传视频
            </a-button>
          </a-upload>
          <div
            v-if="formData.media"
            class="media-preview"
          >
            <video
              class="media-player"
              controls
              preload="metadata"
              :src="toImg(formData.media)"
            />
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="mediaPreviewOpen"
      title="媒体预览"
      width="760px"
      :footer="null"
    >
      <video
        v-if="mediaPreviewUrl"
        class="media-player"
        controls
        preload="metadata"
        :src="mediaPreviewUrl"
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { uploadSimpleFile } from '@/api/FileApi'
import { getOriginStoryPage, createOriginStory, updateOriginStory, deleteOriginStory } from '@/api/OriginStoryAdminApi'

const searchForm = reactive({
  title: '',
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
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '标题', dataIndex: 'storyTitle', key: 'storyTitle' },
  { title: '副标题', dataIndex: 'storySubtitle', key: 'storySubtitle' },
  { title: '技能ID', dataIndex: 'skillId', key: 'skillId', width: 100 },
  { title: '图片', key: 'pics', width: 220 },
  { title: '媒体', key: 'media', width: 120 },
  { title: '操作', key: 'action', fixed: 'right', width: 140 }
]

const isModalVisible = ref(false)
const modalTitle = ref('新增故事')
const isEdit = ref(false)
const formData = reactive({
  id: null,
  storyTitle: '',
  storySubtitle: '',
  storyText: '',
  storyPic1: '',
  storyPic2: '',
  storyPic3: '',
  media: '',
  skillId: null
})

const pic1List = ref([])
const pic2List = ref([])
const pic3List = ref([])
const mediaList = ref([])

const mediaPreviewOpen = ref(false)
const mediaPreviewUrl = ref('')

const toImg = (p) => {
  if (!p) return ''
  if (p.startsWith('http')) return p
  return p.startsWith('/') ? p : '/' + p
}

const picList = (record) => [record.storyPic1, record.storyPic2, record.storyPic3].filter(Boolean)

const loadData = () => {
  loading.value = true
  getOriginStoryPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      title: searchForm.title || undefined,
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
  searchForm.title = ''
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
  formData.storyTitle = ''
  formData.storySubtitle = ''
  formData.storyText = ''
  formData.storyPic1 = ''
  formData.storyPic2 = ''
  formData.storyPic3 = ''
  formData.media = ''
  formData.skillId = null
  pic1List.value = []
  pic2List.value = []
  pic3List.value = []
  mediaList.value = []
}

const hydrateUploadLists = () => {
  pic1List.value = formData.storyPic1 ? [{ uid: 'p1', name: '图片1', status: 'done', url: formData.storyPic1 }] : []
  pic2List.value = formData.storyPic2 ? [{ uid: 'p2', name: '图片2', status: 'done', url: formData.storyPic2 }] : []
  pic3List.value = formData.storyPic3 ? [{ uid: 'p3', name: '图片3', status: 'done', url: formData.storyPic3 }] : []
  mediaList.value = formData.media ? [{ uid: 'm1', name: '媒体', status: 'done', url: formData.media }] : []
}

const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增故事'
  resetForm()
  isModalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑故事'
  Object.assign(formData, record)
  hydrateUploadLists()
  isModalVisible.value = true
}

const beforeUploadPic = async (file, field) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { message.error('图片大小不能超过 5MB'); return false }
  if (!file.type.startsWith('image/')) { message.error('只能上传图片'); return false }

  // 上传到 files/bussiness/start（后端已支持 START 类型）
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

const removePic = (field) => {
  formData[field] = ''
  hydrateUploadLists()
  return true
}

const beforeUploadMedia = async (file) => {
  const isLt200M = file.size / 1024 / 1024 < 200
  if (!isLt200M) { message.error('视频大小不能超过 200MB'); return false }
  if (!file.type.startsWith('video/')) { message.error('只能上传视频'); return false }

  await uploadSimpleFile(file, 'VIDEO', {
    onSuccess: (path) => {
      formData.media = path
      hydrateUploadLists()
      message.success('上传成功')
    },
    onError: () => message.error('上传失败'),
    successMsg: false
  })
  return false
}

const removeMedia = () => {
  formData.media = ''
  hydrateUploadLists()
  return true
}

function openMediaPreview(mediaPath) {
  mediaPreviewUrl.value = toImg(mediaPath)
  mediaPreviewOpen.value = true
}

const handleModalOk = () => {
  if (!formData.storyTitle) {
    message.error('请填写标题')
    return
  }

  const payload = {
    storyTitle: formData.storyTitle,
    storySubtitle: formData.storySubtitle,
    storyText: formData.storyText,
    storyPic1: formData.storyPic1,
    storyPic2: formData.storyPic2,
    storyPic3: formData.storyPic3,
    media: formData.media,
    skillId: formData.skillId
  }

  if (isEdit.value) {
    updateOriginStory(formData.id, payload, {
      onSuccess: () => {
        message.success('更新成功')
        isModalVisible.value = false
        loadData()
      },
      successMsg: false
    })
  } else {
    createOriginStory(payload, {
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
  deleteOriginStory(id, {
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
.start-page-management {
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

.media-preview {
  margin-top: 10px;
}

.media-player {
  width: 100%;
  max-height: 360px;
  background: #000;
}
</style>


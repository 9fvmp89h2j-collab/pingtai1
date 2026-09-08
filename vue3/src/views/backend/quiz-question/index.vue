<template>
  <div class="quiz-question-management">
    <div class="page-header">
      <div>
        <h1>题库管理</h1>
        <p class="page-description">维护安全课堂与学习复习使用的题目、答案和解析。</p>
      </div>
      <a-space>
        <a-button @click="openStageComposer"><i class="fas fa-list-ol" /> 第一关编排</a-button>
        <a-button type="primary" @click="showCreateModal"><i class="fas fa-plus" /> 新增题目</a-button>
      </a-space>
    </div>

    <AdminPageError :message="loadError" :loading="loading" @retry="loadData" />

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
        <a-form-item label="模块">
          <a-select v-model:value="searchForm.moduleCode" allow-clear placeholder="全部" style="width: 150px" :options="moduleOptions" />
        </a-form-item>
        <a-form-item label="题型">
          <a-select v-model:value="searchForm.questionType" allow-clear placeholder="全部" style="width: 150px" :options="questionTypeOptions" />
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
      <div class="admin-table-toolbar">
        <span>共 {{ pagination.total }} 道题目</span>
        <a-button type="text" :loading="loading" @click="loadData">刷新数据</a-button>
      </div>
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
          <template v-else-if="column.key === 'moduleCode'">
            <a-tag :color="record.moduleCode === 'SAFETY_GUARDIAN' ? 'green' : 'blue'">{{ moduleLabel(record.moduleCode) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'questionType'">
            {{ questionTypeLabel(record.questionType) }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : record.status === 2 ? 'orange' : 'default'">
              {{ statusLabel(record.status) }}
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

    <AdminEditorSurface
      v-model:open="isModalVisible"
      :title="modalTitle"
      :loading="saving"
      :modal-width="720"
      @confirm="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item label="所属模块" required>
          <a-select v-model:value="formData.moduleCode" :options="moduleOptions" />
        </a-form-item>
        <a-form-item label="题型" required>
          <a-select v-model:value="formData.questionType" :options="questionTypeOptions" @change="handleQuestionTypeChange" />
        </a-form-item>
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
        <a-form-item v-if="formData.questionType !== 'TRUE_FALSE'"
          label="选项C"
          required
        >
          <a-input
            v-model:value="formData.optionC"
            placeholder="选项C"
            :maxlength="200"
          />
        </a-form-item>
        <a-form-item v-if="formData.questionType !== 'TRUE_FALSE'"
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
            <a-radio-button v-if="formData.questionType !== 'TRUE_FALSE'" value="C">
              C
            </a-radio-button>
            <a-radio-button v-if="formData.questionType !== 'TRUE_FALSE'" value="D">
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
        <template v-if="formData.questionType === 'SCENARIO_CHOICE'">
          <a-form-item label="场景题图" required>
            <div class="scene-upload-row">
              <div class="scene-preview" :class="{ empty: !formData.sceneImagePath }">
                <img v-if="formData.sceneImagePath" :src="resolveMediaUrl(formData.sceneImagePath)" :alt="formData.sceneImageAlt || '场景题图预览'" />
                <span v-else>4:3 场景图</span>
              </div>
              <a-space direction="vertical">
                <a-upload accept="image/png,image/jpeg,image/webp" :show-upload-list="false" :custom-request="handleSceneUpload">
                  <a-button :loading="uploadingScene">{{ formData.sceneImagePath ? '替换图片' : '上传图片' }}</a-button>
                </a-upload>
                <a-button v-if="formData.sceneImagePath" type="link" danger @click="formData.sceneImagePath = ''">移除图片</a-button>
              </a-space>
            </div>
          </a-form-item>
          <a-form-item label="图片说明" required>
            <a-input v-model:value="formData.sceneImageAlt" :maxlength="255" placeholder="描述画面内容，供无障碍阅读使用" />
          </a-form-item>
          <a-form-item label="观察提示">
            <a-input v-model:value="formData.sceneCaption" :maxlength="255" placeholder="例如：发现危险行为，先阻止，再告诉老师" />
          </a-form-item>
        </template>
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
              停用
            </a-radio>
            <a-radio :value="2">归档</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </AdminEditorSurface>

    <a-modal v-model:open="stageOpen" title="第一关 · 14题编排" width="920px" :confirm-loading="stageSaving" ok-text="保存编排" @ok="saveStageComposer">
      <div class="stage-composer-head">
        <span>拖动题目调整顺序，也可以直接替换槽位。</span>
        <a-tag color="green">修订 {{ stageRevision }}</a-tag>
      </div>
      <div class="stage-slots">
        <div v-for="(id, index) in stageQuestionIds" :key="index" class="stage-slot" draggable="true" @dragstart="dragStageIndex = index" @dragover.prevent @drop="dropStageSlot(index)">
          <span class="stage-position">{{ index + 1 }}</span>
          <i class="fas fa-grip-vertical drag-handle" aria-hidden="true" />
          <a-select v-model:value="stageQuestionIds[index]" show-search option-filter-prop="label" style="flex: 1" :options="stageQuestionOptions" placeholder="选择已启用的安全题目" />
          <a-space>
            <a-button size="small" :disabled="index === 0" aria-label="上移" @click="moveStageSlot(index, -1)">↑</a-button>
            <a-button size="small" :disabled="index === stageQuestionIds.length - 1" aria-label="下移" @click="moveStageSlot(index, 1)">↓</a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import AdminEditorSurface from '@/components/backend/AdminEditorSurface.vue'
import AdminPageError from '@/components/backend/AdminPageError.vue'
import { uploadImage } from '@/api/FileApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import {
  getQuizQuestionPage, createQuizQuestion, updateQuizQuestion, deleteQuizQuestion,
  getQuizStage, updateQuizStage
} from '@/api/QuizQuestionAdminApi'

const SAFETY_STAGE_CODE = 'SAFETY_KNOWLEDGE'
const moduleOptions = [
  { label: '通用题库', value: 'GENERAL' },
  { label: '安全守护案', value: 'SAFETY_GUARDIAN' }
]
const questionTypeOptions = [
  { label: '判断题', value: 'TRUE_FALSE' },
  { label: '普通单选', value: 'SINGLE_CHOICE' },
  { label: '图文场景题', value: 'SCENARIO_CHOICE' }
]
const difficultySearchOptions = [
  { label: '简单', value: 1 }, { label: '中等', value: 2 }, { label: '困难', value: 3 }
]
const difficultyFormOptions = [
  { label: '1 简单', value: 1 }, { label: '2 中等', value: 2 }, { label: '3 困难', value: 3 }
]
const statusSearchOptions = [
  { label: '停用', value: 0 }, { label: '启用', value: 1 }, { label: '归档', value: 2 }
]

const searchForm = reactive({
  title: '', category: '', moduleCode: undefined, questionType: undefined,
  difficulty: undefined, status: undefined
})
const tableData = ref([])
const loading = ref(false)
const loadError = ref('')
const saving = ref(false)
const uploadingScene = ref(false)
const pagination = reactive({
  current: 1, pageSize: 10, total: 0, showSizeChanger: true,
  showTotal: total => `共 ${total} 条数据`
})
const columns = [
  { title: '编号', dataIndex: 'id', key: 'id', width: 72 },
  { title: '题目内容', dataIndex: 'title', key: 'title', ellipsis: true, width: 280 },
  { title: '模块', dataIndex: 'moduleCode', key: 'moduleCode', width: 120 },
  { title: '题型', dataIndex: 'questionType', key: 'questionType', width: 110 },
  { title: '答案', dataIndex: 'correctAnswer', key: 'correctAnswer', width: 72 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 110, ellipsis: true },
  { title: '难度', key: 'difficulty', width: 78 },
  { title: '状态', key: 'status', width: 78 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

function difficultyLabel(v) {
  return ({ 1: '简单', 2: '中等', 3: '困难' })[v] || (v == null ? '—' : String(v))
}
function moduleLabel(v) {
  return moduleOptions.find(item => item.value === v)?.label || v || '通用题库'
}
function questionTypeLabel(v) {
  return questionTypeOptions.find(item => item.value === v)?.label || v || '普通单选'
}
function statusLabel(v) {
  return v === 1 ? '启用' : v === 2 ? '归档' : '停用'
}

const isModalVisible = ref(false)
const modalTitle = ref('新增题目')
const isEdit = ref(false)
const formData = reactive({
  id: null, questionCode: '', moduleCode: 'GENERAL', questionType: 'SINGLE_CHOICE', title: '',
  optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A', explanation: '',
  sceneImagePath: '', sceneImageAlt: '', sceneCaption: '', category: '', difficulty: 1, status: 0
})

async function loadData() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await getQuizQuestionPage({
      current: pagination.current, size: pagination.pageSize,
      title: searchForm.title || undefined, category: searchForm.category || undefined,
      moduleCode: searchForm.moduleCode, questionType: searchForm.questionType,
      difficulty: searchForm.difficulty, status: searchForm.status
    }, { showDefaultMsg: false })
    tableData.value = data?.records || []
    pagination.total = data?.total || 0
  } catch (error) {
    loadError.value = error?.message || '获取题库失败'
  } finally {
    loading.value = false
  }
}
function handleSearch() { pagination.current = 1; loadData() }
function handleReset() {
  Object.assign(searchForm, { title: '', category: '', moduleCode: undefined, questionType: undefined, difficulty: undefined, status: undefined })
  pagination.current = 1
  loadData()
}
function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}
function resetForm() {
  Object.assign(formData, {
    id: null, questionCode: '', moduleCode: 'GENERAL', questionType: 'SINGLE_CHOICE', title: '',
    optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A', explanation: '',
    sceneImagePath: '', sceneImageAlt: '', sceneCaption: '', category: '', difficulty: 1, status: 0
  })
}
function showCreateModal() {
  isEdit.value = false
  modalTitle.value = '新增题目'
  resetForm()
  isModalVisible.value = true
}
function handleEdit(record) {
  isEdit.value = true
  modalTitle.value = '编辑题目'
  Object.assign(formData, {
    id: record.id, questionCode: record.questionCode || '', moduleCode: record.moduleCode || 'GENERAL',
    questionType: record.questionType || 'SINGLE_CHOICE', title: record.title || '',
    optionA: record.optionA || '', optionB: record.optionB || '', optionC: record.optionC || '', optionD: record.optionD || '',
    correctAnswer: record.correctAnswer || 'A', explanation: record.explanation || '',
    sceneImagePath: record.sceneImagePath || '', sceneImageAlt: record.sceneImageAlt || '', sceneCaption: record.sceneCaption || '',
    category: record.category || '', difficulty: record.difficulty ?? 1, status: record.status ?? 0
  })
  isModalVisible.value = true
}
function handleQuestionTypeChange(type) {
  if (type === 'TRUE_FALSE') {
    formData.optionC = ''
    formData.optionD = ''
    if (!['A', 'B'].includes(formData.correctAnswer)) formData.correctAnswer = 'A'
  }
  if (type !== 'SCENARIO_CHOICE') {
    formData.sceneImagePath = ''
    formData.sceneImageAlt = ''
    formData.sceneCaption = ''
  }
}
const buildPayload = () => ({
  questionCode: formData.questionCode || undefined, moduleCode: formData.moduleCode, questionType: formData.questionType,
  title: formData.title, optionA: formData.optionA, optionB: formData.optionB,
  optionC: formData.optionC || undefined, optionD: formData.optionD || undefined,
  correctAnswer: formData.correctAnswer, explanation: formData.explanation || undefined,
  sceneImagePath: formData.sceneImagePath || undefined, sceneImageAlt: formData.sceneImageAlt || undefined,
  sceneCaption: formData.sceneCaption || undefined, category: formData.category || undefined,
  difficulty: formData.difficulty, status: formData.status
})
async function handleModalOk() {
  if (saving.value) return
  if (!formData.title?.trim()) return message.error('请填写题目内容')
  if (!formData.optionA?.trim() || !formData.optionB?.trim()
    || (formData.questionType !== 'TRUE_FALSE' && (!formData.optionC?.trim() || !formData.optionD?.trim()))) {
    return message.error(formData.questionType === 'TRUE_FALSE' ? '请填写两个判断选项' : '请填写四个选项')
  }
  if (formData.questionType === 'SCENARIO_CHOICE' && formData.status === 1
    && (!formData.sceneImagePath || !formData.sceneImageAlt?.trim())) {
    return message.error('启用场景题前请上传题图并填写图片说明')
  }
  saving.value = true
  try {
    if (isEdit.value) await updateQuizQuestion(formData.id, buildPayload(), { showDefaultMsg: false })
    else await createQuizQuestion(buildPayload(), { showDefaultMsg: false })
    message.success(isEdit.value ? '更新成功' : '创建成功')
    isModalVisible.value = false
    await loadData()
  } catch (error) {
    message.error(error?.message || '保存失败')
  } finally { saving.value = false }
}
function handleModalCancel() { isModalVisible.value = false; resetForm() }
async function handleDelete(id) {
  try {
    await deleteQuizQuestion(id, { showDefaultMsg: false })
    message.success('删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) pagination.current -= 1
    await loadData()
  } catch (error) { message.error(error?.message || '删除失败') }
}
async function handleSceneUpload({ file, onSuccess, onError }) {
  uploadingScene.value = true
  try {
    const path = await uploadImage(file, { showDefaultMsg: false })
    formData.sceneImagePath = typeof path === 'string' ? path : (path?.url || path?.filePath || '')
    onSuccess?.(path)
    message.success('场景图上传成功')
  } catch (error) {
    onError?.(error)
    message.error(error?.message || '场景图上传失败')
  } finally { uploadingScene.value = false }
}

const stageOpen = ref(false)
const stageSaving = ref(false)
const stageRevision = ref(1)
const stageQuestionIds = ref([])
const stageQuestionOptions = ref([])
const dragStageIndex = ref(null)
async function openStageComposer() {
  try {
    const [stage, page] = await Promise.all([
      getQuizStage(SAFETY_STAGE_CODE, { showDefaultMsg: false }),
      getQuizQuestionPage({ current: 1, size: 200, moduleCode: 'SAFETY_GUARDIAN', status: 1 }, { showDefaultMsg: false })
    ])
    stageRevision.value = stage?.revision || 1
    stageQuestionIds.value = (stage?.questions || []).map(item => item.id)
    while (stageQuestionIds.value.length < 14) stageQuestionIds.value.push(undefined)
    stageQuestionIds.value = stageQuestionIds.value.slice(0, 14)
    stageQuestionOptions.value = (page?.records || []).map(item => ({ value: item.id, label: `${questionTypeLabel(item.questionType)} · ${item.title}` }))
    stageOpen.value = true
  } catch (error) { message.error(error?.message || '读取第一关编排失败') }
}
function moveStageSlot(index, delta) {
  const target = index + delta
  if (target < 0 || target >= stageQuestionIds.value.length) return
  const ids = [...stageQuestionIds.value]
  ;[ids[index], ids[target]] = [ids[target], ids[index]]
  stageQuestionIds.value = ids
}
function dropStageSlot(targetIndex) {
  const sourceIndex = dragStageIndex.value
  dragStageIndex.value = null
  if (sourceIndex == null || sourceIndex === targetIndex) return
  const ids = [...stageQuestionIds.value]
  const [moved] = ids.splice(sourceIndex, 1)
  ids.splice(targetIndex, 0, moved)
  stageQuestionIds.value = ids
}
async function saveStageComposer() {
  const ids = stageQuestionIds.value.filter(Boolean)
  if (ids.length !== 14 || new Set(ids).size !== 14) return message.error('请配置14道不重复的已启用题目')
  stageSaving.value = true
  try {
    const stage = await updateQuizStage(SAFETY_STAGE_CODE, ids, { showDefaultMsg: false })
    stageRevision.value = stage?.revision || stageRevision.value + 1
    stageOpen.value = false
    message.success('第一关编排已更新')
  } catch (error) { message.error(error?.message || '保存编排失败') }
  finally { stageSaving.value = false }
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

.scene-upload-row {
  display: flex;
  align-items: center;
  gap: 18px;
}

.scene-preview {
  display: grid;
  width: 240px;
  aspect-ratio: 4 / 3;
  place-items: center;
  overflow: hidden;
  border: 1px solid #d9d9d9;
  border-radius: 12px;
  background: #f6f8f7;
  color: #8c8c8c;
}

.scene-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stage-composer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  color: #66736e;
}

.stage-slots {
  display: grid;
  gap: 8px;
  max-height: 62vh;
  overflow-y: auto;
  padding-right: 6px;
}

.stage-slot {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e4e9e6;
  border-radius: 10px;
  background: #fbfcfb;
}

.stage-slot:hover {
  border-color: #8eb8a7;
  background: #f4faf7;
}

.stage-position {
  display: grid;
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  place-items: center;
  border-radius: 50%;
  background: #246b56;
  color: #fff;
  font-weight: 700;
}

.drag-handle {
  color: #9aa7a2;
  cursor: grab;
}
</style>

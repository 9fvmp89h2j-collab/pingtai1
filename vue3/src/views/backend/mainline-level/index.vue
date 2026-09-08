<template>
  <main class="mainline-management" aria-labelledby="mainline-page-title">
    <header class="page-header">
      <div>
        <span class="eyebrow">内容库 / 主线探案</span>
        <h1 id="mainline-page-title">主线关卡内容台</h1>
        <p>管理 8 个主线案件的展示内容、解锁目标和奖励规则。</p>
      </div>
      <div class="header-actions">
        <a-tag v-if="draft?.status === 'DRAFT'" color="orange">草稿 v{{ draft.version }}</a-tag>
        <a-tag v-else color="green">已发布 v{{ draft?.version || 1 }}</a-tag>
        <a-button :loading="loading" @click="loadData">
          <template #icon><i class="fas fa-rotate-right" /></template>
          刷新
        </a-button>
        <a-button type="primary" :loading="saving" :disabled="saving || publishing" @click="handleSave">
          <template #icon><i class="fas fa-floppy-disk" /></template>
          保存草稿
        </a-button>
        <a-button
          type="primary"
          class="publish-button"
          :loading="publishing"
          :disabled="saving || publishing || !draft"
          @click="openPublishConfirm"
        >
          <template #icon><i class="fas fa-paper-plane" /></template>
          发布配置
        </a-button>
      </div>
    </header>

    <a-alert
      v-if="loadError"
      class="page-alert"
      type="error"
      show-icon
      :message="loadError"
      description="请检查登录状态或服务端配置表，然后重试。当前表单不会被清空。"
      closable
      @close="loadError = ''"
    />

    <a-alert
      v-if="validationErrors.length"
      class="page-alert"
      type="warning"
      show-icon
      message="配置尚未通过发布校验"
      :description="validationErrors.join('；')"
      closable
      @close="validationErrors = []"
    />

    <section v-if="draft" class="workbench" aria-label="主线关卡编辑工作台">
      <aside class="level-rail" aria-label="主线关卡列表">
        <div class="rail-heading">
          <div>
            <span class="eyebrow">关卡目录</span>
            <h2>8 件主线案件</h2>
          </div>
          <span class="rail-count">{{ draft.levels.length }}/8</span>
        </div>
        <button
          v-for="level in draft.levels"
          :key="level.id"
          type="button"
          class="level-choice"
          :class="{ active: level.id === selectedLevelId }"
          :aria-current="level.id === selectedLevelId ? 'step' : undefined"
          @click="selectedLevelId = level.id"
        >
          <span class="level-number">{{ level.order }}</span>
          <span class="level-choice-copy">
            <strong>{{ level.label }}</strong>
            <small>{{ level.statusText }}</small>
          </span>
          <i class="fas fa-chevron-right" aria-hidden="true" />
        </button>

        <div class="rail-note">
          <i class="fas fa-shield-heart" aria-hidden="true" />
          <span>任务编码、路由和玩法绑定受保护，避免历史进度断链。</span>
        </div>
      </aside>

      <section v-if="selectedLevel" class="editor-panel" aria-labelledby="editor-title">
        <div class="editor-heading">
          <div>
            <span class="eyebrow">第 {{ selectedLevel.order }} 关 / 编辑中</span>
            <h2 id="editor-title">{{ selectedLevel.label }}</h2>
          </div>
          <a-tag :color="selectedLevel.publicLevel === false ? 'default' : 'green'">
            {{ selectedLevel.publicLevel === false ? '暂不公开' : '公开展示' }}
          </a-tag>
        </div>

        <a-form layout="vertical" class="editor-form">
          <fieldset>
            <legend>展示内容</legend>
            <div class="form-grid two-columns">
              <a-form-item label="关卡标题" required>
                <a-input v-model:value="selectedLevel.label" :maxlength="32" show-count />
              </a-form-item>
              <a-form-item label="状态文案" required>
                <a-input v-model:value="selectedLevel.statusText" :maxlength="32" show-count />
              </a-form-item>
            </div>
            <a-form-item label="关卡说明" required>
              <a-textarea v-model:value="selectedLevel.description" :rows="3" :maxlength="160" show-count />
            </a-form-item>
            <div class="form-grid two-columns">
              <a-form-item label="节点图标">
                <a-select v-model:value="selectedLevel.icon" aria-label="节点图标">
                  <a-select-option v-for="icon in iconOptions" :key="icon" :value="icon">第 {{ icon }} 枚徽章</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="节点印章">
                <a-select v-model:value="selectedLevel.seal" aria-label="节点印章">
                  <a-select-option v-for="seal in sealOptions" :key="seal" :value="seal">{{ seal }}</a-select-option>
                </a-select>
              </a-form-item>
            </div>
            <a-form-item label="关卡封面">
              <div class="cover-uploader">
                <div v-if="selectedLevel.coverPath" class="cover-preview">
                  <img :src="resolveMediaUrl(selectedLevel.coverPath)" alt="当前关卡封面预览" />
                  <button type="button" aria-label="移除关卡封面" @click="selectedLevel.coverPath = ''">移除</button>
                </div>
                <a-upload
                  accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                  :show-upload-list="false"
                  :before-upload="handleCoverUpload"
                >
                  <a-button :loading="uploading" :disabled="uploading">
                    <template #icon><i class="fas fa-image" /></template>
                    {{ selectedLevel.coverPath ? '替换封面' : '上传封面' }}
                  </a-button>
                </a-upload>
                <span class="helper-text">支持 JPG、PNG、GIF、BMP、WebP，单张不超过 10MB。</span>
              </div>
            </a-form-item>
          </fieldset>

          <fieldset>
            <legend>解锁规则</legend>
            <div class="readonly-strip">
              <span><small>固定 ID</small><code>{{ selectedLevel.id }}</code></span>
              <span><small>前端路由</small><code>{{ selectedLevel.route }}</code></span>
              <span><small>地图键</small><code>{{ selectedLevel.mapKey }}</code></span>
            </div>
            <a-form-item label="前置关卡" :required="selectedLevel.order > 1">
              <a-select
                v-model:value="selectedLevel.prerequisiteLevelIds"
                mode="multiple"
                :disabled="selectedLevel.order === 1"
                placeholder="选择完成后才解锁本关的关卡"
              >
                <a-select-option
                  v-for="level in previousLevels"
                  :key="level.id"
                  :value="level.id"
                >
                  第 {{ level.order }} 关 · {{ level.label }}
                </a-select-option>
              </a-select>
              <span class="helper-text">只能选择排在本关之前的关卡，系统会阻止循环前置。</span>
            </a-form-item>
          </fieldset>

          <fieldset v-if="selectedTask">
            <legend>本关任务与奖励</legend>
            <div class="readonly-strip task-binding">
              <span><small>任务编码</small><code>{{ selectedTask.taskCode }}</code></span>
              <span><small>玩法类型</small><strong>{{ selectedTask.taskType }}</strong></span>
              <span><small>玩法入口</small><code>{{ selectedTask.route }}</code></span>
            </div>
            <a-form-item label="任务名称" required>
              <a-input v-model:value="selectedTask.name" :maxlength="48" show-count />
            </a-form-item>
            <a-form-item label="任务说明" required>
              <a-textarea v-model:value="selectedTask.description" :rows="3" :maxlength="160" show-count />
            </a-form-item>
            <a-form-item label="完成目标" required>
              <a-input-number v-model:value="selectedTask.target" :min="1" :max="selectedTask.taskCode === 'meridian-river-completion' ? 14 : 999" />
              <span class="inline-helper">{{ selectedTask.taskCode === 'meridian-river-completion' ? '最多 14 条经络路线' : '正整数，最多 999' }}</span>
            </a-form-item>

            <div class="reward-heading">
              <div>
                <h3>完成奖励</h3>
                <p>只允许现有材料或积分，已完成任务不会因发布而重复发奖。</p>
              </div>
              <a-button type="dashed" @click="addReward">
                <template #icon><i class="fas fa-plus" /></template>
                添加奖励
              </a-button>
            </div>
            <div v-if="selectedTask.rewards?.length" class="reward-list">
              <div v-for="(reward, index) in selectedTask.rewards" :key="`${reward.rewardType}-${index}`" class="reward-row">
                <a-select v-model:value="reward.rewardType" class="reward-type" aria-label="奖励类型">
                  <a-select-option value="MATERIAL">材料</a-select-option>
                  <a-select-option value="SCORE">积分</a-select-option>
                </a-select>
                <a-select v-if="reward.rewardType === 'MATERIAL'" v-model:value="reward.itemCode" class="reward-item" aria-label="奖励材料">
                  <a-select-option v-for="item in materialOptions" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
                </a-select>
                <a-input-number v-if="reward.rewardType === 'MATERIAL'" v-model:value="reward.amount" :min="1" :max="999" class="reward-amount" />
                <a-input-number v-else v-model:value="reward.scoreDelta" :min="1" :max="999" class="reward-amount" />
                <span class="reward-unit">{{ reward.rewardType === 'MATERIAL' ? '份' : '分' }}</span>
                <a-button type="text" danger aria-label="删除奖励" @click="removeReward(index)">
                  <i class="fas fa-trash-can" aria-hidden="true" />
                </a-button>
              </div>
            </div>
            <a-empty v-else :image="false" description="本关暂无奖励" />
          </fieldset>
        </a-form>
      </section>

      <aside v-if="selectedLevel" class="preview-panel" aria-label="儿童端预览">
        <div class="preview-heading">
          <span class="eyebrow">儿童端效果</span>
          <h2>儿童端预览</h2>
          <p>这里展示发布后地图任务卡的主要信息。</p>
        </div>
        <article class="mission-preview">
          <div class="preview-cover" :class="{ empty: !selectedLevel.coverPath }">
            <img v-if="selectedLevel.coverPath" :src="resolveMediaUrl(selectedLevel.coverPath)" alt="关卡封面" />
            <i v-else class="fas fa-map-location-dot" aria-hidden="true" />
            <span>第 {{ selectedLevel.order }} 关</span>
          </div>
          <div class="preview-body">
            <span class="preview-kicker">{{ selectedLevel.statusText }}</span>
            <h3>{{ selectedLevel.label }}</h3>
            <p>{{ selectedLevel.description }}</p>
            <div v-if="selectedTask" class="preview-task">
              <div>
                <small>今日案件</small>
                <strong>{{ selectedTask.name }}</strong>
              </div>
              <b>{{ selectedTask.target }}<small>目标</small></b>
            </div>
            <div class="preview-safety"><i class="fas fa-shield-heart" aria-hidden="true" />只观察、只学习，不自己针刺。</div>
          </div>
        </article>

        <section class="version-history" aria-labelledby="history-title">
          <div class="history-heading">
            <h2 id="history-title">版本记录</h2>
            <span>{{ versions.length }} 个版本</span>
          </div>
          <div v-for="version in versions.slice(0, 5)" :key="version.revisionId || version.version" class="history-row">
            <span>
              <strong>v{{ version.version }}</strong>
              <small>{{ version.status === 'PUBLISHED' ? '已发布' : version.status === 'DRAFT' ? '草稿' : '历史版本' }}</small>
            </span>
            <a-button
              v-if="version.status !== 'DRAFT' && Number(version.version) !== Number(draft.version)"
              type="link"
              size="small"
              :loading="restoringVersion === version.version"
              @click="restoreVersion(version.version)"
            >
              恢复为草稿
            </a-button>
          </div>
        </section>
      </aside>
    </section>

    <a-empty v-else-if="!loading" class="empty-page" description="暂无主线配置" />
    <div v-if="loading" class="loading-page" role="status" aria-live="polite">
      <a-spin tip="正在读取主线配置…" />
    </div>

    <a-modal
      v-model:open="publishModalOpen"
      title="确认发布主线配置"
      ok-text="确认发布"
      cancel-text="继续编辑"
      :confirm-loading="publishing"
      @ok="handlePublish"
    >
      <p>发布 v{{ draft?.version || '新版本' }} 后，标题、说明和封面会立即更新。</p>
      <p>已有任务进度保留原目标与奖励；尚未开始的任务使用新规则。历史版本仍可恢复为新草稿。</p>
    </a-modal>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  getAdminMainlineConfig,
  publishMainlineDraft,
  restoreMainlineVersion,
  saveMainlineDraft,
  validateMainlineDraft
} from '@/api/MainlineConfigApi'
import { uploadImage } from '@/api/FileApi'
import { materials } from '@/data/materials'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const uploading = ref(false)
const restoringVersion = ref(null)
const loadError = ref('')
const validationErrors = ref([])
const selectedLevelId = ref('checkin')
const publishModalOpen = ref(false)
const draft = ref(null)
const versions = ref([])

const iconOptions = ['1', '2', '3', '4', '5', '6', '7', '8']
const sealOptions = ['今', '锁', '案', '星', '修', '阅', '行', '启']
const materialOptions = materials.filter((item) => item.kind !== 'collectible').map((item) => ({ id: item.id, name: item.name }))

const selectedLevel = computed(() => draft.value?.levels?.find((level) => level.id === selectedLevelId.value) || draft.value?.levels?.[0] || null)
const selectedTask = computed(() => selectedLevel.value?.tasks?.[0] || null)
const previousLevels = computed(() => (draft.value?.levels || []).filter((level) => Number(level.order) < Number(selectedLevel.value?.order)))

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function applyResponse(response) {
  const config = response?.config || response
  if (!config?.levels?.length) throw new Error('服务端没有返回 8 个主线关卡')
  draft.value = clone(config)
  // Published data is the base for a new draft. The first save creates a new revision.
  if (draft.value.status !== 'DRAFT') {
    draft.value.revisionId = null
    draft.value.editVersion = null
    draft.value.status = 'LOCAL_DRAFT'
  }
  versions.value = response?.versions || []
  if (!draft.value.levels.some((level) => level.id === selectedLevelId.value)) selectedLevelId.value = draft.value.levels[0].id
  validationErrors.value = []
}

async function loadData() {
  if (loading.value) return
  loading.value = true
  loadError.value = ''
  try {
    applyResponse(await getAdminMainlineConfig({ showDefaultMsg: false }))
  } catch (error) {
    loadError.value = error?.message || '读取主线配置失败，请重试'
  } finally {
    loading.value = false
  }
}

function toPayload() {
  return {
    revisionId: draft.value?.revisionId || null,
    expectedEditVersion: draft.value?.editVersion ?? null,
    levels: (draft.value?.levels || []).map((level) => ({
      id: level.id,
      order: level.order,
      label: level.label,
      statusText: level.statusText,
      description: level.description,
      icon: level.icon,
      seal: level.seal,
      x: level.x,
      y: level.y,
      route: level.route,
      mapKey: level.mapKey,
      coverPath: level.coverPath || '',
      publicLevel: level.publicLevel !== false,
      requiredTaskIds: [...(level.requiredTaskIds || [])],
      prerequisiteLevelIds: [...(level.prerequisiteLevelIds || [])],
      tasks: (level.tasks || []).map((task) => ({
        taskCode: task.taskCode,
        levelId: task.levelId || level.id,
        name: task.name,
        description: task.description,
        taskType: task.taskType,
        route: task.route,
        target: Number(task.target || 1),
        editable: task.editable !== false,
        rewards: (task.rewards || []).map((reward) => ({
          rewardType: reward.rewardType,
          itemCode: reward.rewardType === 'MATERIAL' ? reward.itemCode : null,
          amount: reward.rewardType === 'MATERIAL' ? Number(reward.amount || 0) : 0,
          scoreDelta: reward.rewardType === 'SCORE' ? Number(reward.scoreDelta || 0) : 0
        }))
      }))
    }))
  }
}

async function validateDraft(showMessage = true) {
  validationErrors.value = []
  try {
    const result = await validateMainlineDraft(toPayload(), { showDefaultMsg: false })
    validationErrors.value = result?.errors || []
    if (!result?.valid) {
      if (showMessage) message.error(validationErrors.value[0] || '配置校验未通过')
      return false
    }
    if (showMessage) message.success('配置校验通过')
    return true
  } catch (error) {
    validationErrors.value = [error?.message || '配置校验失败']
    if (showMessage) message.error(validationErrors.value[0])
    return false
  }
}

async function handleSave(options = {}) {
  if (saving.value || (!options.allowWhilePublishing && publishing.value) || !draft.value) return false
  if (!await validateDraft(!options.silent)) return false
  saving.value = true
  try {
    const saved = await saveMainlineDraft(toPayload(), { showDefaultMsg: false })
    draft.value = clone(saved)
    message.success('主线关卡草稿已保存')
    return true
  } catch (error) {
    message.error(error?.message || '保存草稿失败，请重试')
    return false
  } finally {
    saving.value = false
  }
}

function openPublishConfirm() {
  publishModalOpen.value = true
}

async function handlePublish() {
  if (publishing.value || !draft.value) return
  publishing.value = true
  try {
    const saved = draft.value.revisionId ? true : await handleSave({ silent: true, allowWhilePublishing: true })
    if (!saved || !draft.value.revisionId) return
    if (!await validateDraft(false)) return
    const published = await publishMainlineDraft({ revisionId: draft.value.revisionId }, {
      showDefaultMsg: false,
      idempotencyKey: `mainline-publish:${draft.value.revisionId}:${draft.value.editVersion || 0}`
    })
    draft.value = clone(published)
    draft.value.status = 'LOCAL_DRAFT'
    draft.value.revisionId = null
    versions.value = (await getAdminMainlineConfig({ showDefaultMsg: false }))?.versions || versions.value
    publishModalOpen.value = false
    message.success('主线关卡配置已发布')
  } catch (error) {
    message.error(error?.message || '发布失败，请刷新后重试')
  } finally {
    publishing.value = false
  }
}

async function handleCoverUpload(file) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (!allowed.includes(file.type) || file.size > 10 * 1024 * 1024) {
    message.error('封面必须是 JPG、PNG、GIF、BMP 或 WebP，且不超过 10MB')
    return false
  }
  uploading.value = true
  try {
    const path = await uploadImage(file, { showDefaultMsg: false })
    selectedLevel.value.coverPath = path
    message.success('封面上传成功，保存草稿后生效')
  } catch (error) {
    message.error(error?.message || '封面上传失败，请重试')
  } finally {
    uploading.value = false
  }
  return false
}

function addReward() {
  if (!selectedTask.value) return
  selectedTask.value.rewards = selectedTask.value.rewards || []
  selectedTask.value.rewards.push({ rewardType: 'MATERIAL', itemCode: materialOptions[0]?.id, amount: 1, scoreDelta: 0 })
}

function removeReward(index) {
  selectedTask.value?.rewards?.splice(index, 1)
}

async function restoreVersion(version) {
  if (restoringVersion.value) return
  restoringVersion.value = version
  try {
    const restored = await restoreMainlineVersion(version, { showDefaultMsg: false })
    draft.value = clone(restored)
    versions.value = (await getAdminMainlineConfig({ showDefaultMsg: false }))?.versions || versions.value
    message.success(`v${version} 已恢复为新草稿`)
  } catch (error) {
    message.error(error?.message || '恢复版本失败，请重试')
  } finally {
    restoringVersion.value = null
  }
}

onMounted(loadData)
</script>

<style scoped lang="less">
.mainline-management {
  --ink: #1d403c;
  --ink-soft: #47716a;
  --paper: #f6f2ea;
  --paper-deep: #eee7db;
  --apricot: #e9b18e;
  --gold: #c6944e;
  --line: rgba(29, 64, 60, 0.14);
  min-height: 100vh;
  padding: 24px;
  background: var(--paper);
  color: var(--ink);
}

.page-header,
.workbench,
.page-alert,
.empty-page,
.loading-page {
  max-width: 1500px;
  margin-left: auto;
  margin-right: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
  margin-bottom: 20px;
}

.eyebrow {
  display: block;
  color: var(--gold);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

h1,
h2,
h3,
p {
  margin-top: 0;
}

h1 {
  margin-bottom: 8px;
  color: var(--ink);
  font-size: clamp(24px, 3vw, 34px);
  letter-spacing: -0.04em;
}

.page-header p,
.preview-heading p,
.reward-heading p {
  margin-bottom: 0;
  color: var(--ink-soft);
  line-height: 1.6;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
  align-items: center;
}

.publish-button {
  background: var(--ink);
  border-color: var(--ink);
}

.page-alert {
  margin-bottom: 16px;
}

.workbench {
  display: grid;
  grid-template-columns: 245px minmax(0, 1fr) 310px;
  gap: 16px;
  align-items: start;
}

.level-rail,
.editor-panel,
.preview-panel {
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 14px 40px rgba(29, 64, 60, 0.08);
}

.level-rail {
  position: sticky;
  top: 18px;
  padding: 16px;
}

.rail-heading,
.editor-heading,
.history-heading,
.reward-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.rail-heading {
  padding: 4px 4px 14px;
  border-bottom: 1px solid var(--line);
}

.rail-heading h2,
.preview-heading h2,
.editor-heading h2 {
  margin: 4px 0 0;
  font-size: 19px;
}

.rail-count {
  color: var(--gold);
  font-size: 13px;
  font-weight: 800;
}

.level-choice {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 58px;
  gap: 10px;
  padding: 8px;
  border: 0;
  border-bottom: 1px solid rgba(29, 64, 60, 0.08);
  background: transparent;
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: background 180ms ease, transform 180ms ease;
}

.level-choice:hover,
.level-choice:focus-visible {
  background: rgba(233, 177, 142, 0.18);
  outline: none;
}

.level-choice:focus-visible {
  box-shadow: inset 0 0 0 2px var(--gold);
}

.level-choice.active {
  border-radius: 10px;
  background: var(--ink);
  color: var(--paper);
}

.level-number {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 50%;
  background: var(--apricot);
  color: var(--ink);
  font-weight: 800;
}

.level-choice-copy {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 2px;
}

.level-choice-copy strong,
.level-choice-copy small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.level-choice-copy small {
  opacity: 0.68;
}

.rail-note {
  display: flex;
  gap: 8px;
  margin-top: 14px;
  padding: 11px;
  border-radius: 12px;
  background: var(--paper-deep);
  color: var(--ink-soft);
  font-size: 12px;
  line-height: 1.5;
}

.editor-panel {
  padding: 24px;
}

.editor-heading {
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.editor-form fieldset {
  min-width: 0;
  margin: 0 0 24px;
  padding: 0;
  border: 0;
}

.editor-form legend {
  width: 100%;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--line);
  color: var(--ink);
  font-size: 16px;
  font-weight: 800;
}

.form-grid {
  display: grid;
  gap: 12px;
}

.two-columns {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.readonly-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px;
  border-radius: 10px;
  background: var(--paper-deep);
}

.readonly-strip span {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.readonly-strip small {
  color: var(--ink-soft);
  font-size: 11px;
}

.readonly-strip code {
  overflow: hidden;
  color: var(--ink);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.helper-text,
.inline-helper {
  display: block;
  margin-top: 6px;
  color: var(--ink-soft);
  font-size: 12px;
  line-height: 1.5;
}

.inline-helper {
  display: inline-block;
  margin: 0 0 0 8px;
}

.cover-uploader {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.cover-preview {
  position: relative;
  width: 150px;
  height: 86px;
  overflow: hidden;
  border-radius: 10px;
  background: var(--paper-deep);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-preview button {
  position: absolute;
  right: 4px;
  bottom: 4px;
  min-height: 28px;
  padding: 2px 7px;
  border: 0;
  border-radius: 6px;
  background: rgba(29, 64, 60, 0.88);
  color: #fff;
  cursor: pointer;
}

.task-binding {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.reward-heading {
  align-items: flex-end;
  margin: 20px 0 10px;
}

.reward-heading h3 {
  margin-bottom: 4px;
  font-size: 15px;
}

.reward-list {
  display: grid;
  gap: 8px;
}

.reward-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid var(--line);
  border-radius: 10px;
}

.reward-type {
  width: 80px;
}

.reward-item {
  min-width: 0;
  flex: 1;
}

.reward-amount {
  width: 96px;
}

.reward-unit {
  color: var(--ink-soft);
  font-size: 12px;
}

.preview-panel {
  position: sticky;
  top: 18px;
  padding: 18px;
}

.preview-heading {
  margin-bottom: 14px;
}

.preview-heading h2 {
  margin-bottom: 6px;
}

.mission-preview {
  overflow: hidden;
  border: 1px solid rgba(29, 64, 60, 0.14);
  border-radius: 16px;
  background: #fffaf2;
}

.preview-cover {
  position: relative;
  display: grid;
  height: 132px;
  place-items: center;
  overflow: hidden;
  background: var(--ink);
  color: var(--apricot);
}

.preview-cover.empty {
  background: linear-gradient(135deg, var(--ink), var(--ink-soft));
}

.preview-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-cover > i {
  font-size: 34px;
}

.preview-cover span {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(255, 250, 242, 0.9);
  color: var(--ink);
  font-size: 12px;
  font-weight: 800;
}

.preview-body {
  padding: 16px;
}

.preview-kicker {
  color: var(--gold);
  font-size: 11px;
  font-weight: 800;
}

.preview-body h3 {
  margin: 5px 0 8px;
  color: var(--ink);
  font-size: 21px;
}

.preview-body p {
  min-height: 64px;
  margin-bottom: 14px;
  color: var(--ink-soft);
  line-height: 1.6;
}

.preview-task {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 10px;
  background: var(--paper-deep);
}

.preview-task div {
  display: grid;
  gap: 3px;
}

.preview-task small {
  color: var(--ink-soft);
  font-size: 11px;
}

.preview-task b {
  display: grid;
  place-items: center;
  color: var(--gold);
  font-size: 20px;
}

.preview-safety {
  display: flex;
  gap: 7px;
  margin-top: 14px;
  color: var(--ink-soft);
  font-size: 11px;
  line-height: 1.5;
}

.version-history {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid var(--line);
}

.history-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-heading h2 {
  margin: 0;
  font-size: 15px;
}

.history-heading span,
.history-row small {
  color: var(--ink-soft);
  font-size: 12px;
}

.history-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  min-height: 44px;
  border-bottom: 1px solid rgba(29, 64, 60, 0.08);
}

.history-row span {
  display: grid;
  gap: 2px;
}

.empty-page,
.loading-page {
  min-height: 320px;
  display: grid;
  place-items: center;
}

@media (min-width: 1024px) {
  .mainline-management {
    --ink: #17211e;
    --ink-soft: #66736f;
    --paper: transparent;
    --paper-deep: #f1f4f3;
    --apricot: #d8e7e3;
    --gold: #1f5a50;
    --line: #dde4e1;
    min-height: calc(100vh - 112px);
    padding: 0;
    background: transparent;
  }

  .page-header,
  .workbench,
  .page-alert,
  .empty-page,
  .loading-page {
    max-width: none;
  }

  .page-header {
    align-items: flex-start;
    margin-bottom: 18px;
  }

  h1 {
    margin-bottom: 6px;
    font-size: 24px;
    font-weight: 680;
    letter-spacing: -.02em;
  }

  .eyebrow {
    color: #66736f;
    font-weight: 550;
    letter-spacing: .04em;
    text-transform: none;
  }

  .publish-button {
    background: #174b43;
    border-color: #174b43;
  }

  .workbench {
    grid-template-columns: 232px minmax(0, 1fr) 320px;
    gap: 16px;
  }

  .level-rail,
  .editor-panel,
  .preview-panel {
    border-color: #dde4e1;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: none;
  }

  .level-rail {
    padding: 14px;
  }

  .rail-heading h2,
  .preview-heading h2,
  .editor-heading h2 {
    font-size: 17px;
    font-weight: 680;
  }

  .level-choice {
    min-height: 54px;
    border-bottom-color: #e8eeeb;
  }

  .level-choice:hover,
  .level-choice:focus-visible {
    background: #edf3f1;
  }

  .level-choice:focus-visible {
    box-shadow: inset 0 0 0 2px #3d8175;
  }

  .level-choice.active {
    color: #ffffff;
    background: #1f5a50;
  }

  .level-number {
    border-radius: 6px;
    color: #1f5a50;
    background: #dfece8;
  }

  .level-choice.active .level-number {
    color: #1f5a50;
    background: #ffffff;
  }

  .rail-note,
  .readonly-strip {
    border-radius: 6px;
    background: #f1f4f3;
  }

  .editor-panel {
    padding: 22px;
  }

  .editor-form legend {
    font-weight: 680;
  }
}

@media (max-width: 1180px) {
  .workbench {
    grid-template-columns: 220px minmax(0, 1fr);
  }

  .preview-panel {
    position: static;
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: minmax(220px, 0.8fr) minmax(280px, 1.2fr);
    gap: 18px;
  }

  .version-history {
    grid-column: 1 / -1;
  }
}

@media (max-width: 760px) {
  .mainline-management {
    padding: 14px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    justify-content: flex-start;
  }

  .workbench {
    display: block;
  }

  .level-rail,
  .preview-panel {
    position: static;
    margin-bottom: 14px;
  }

  .level-rail {
    overflow-x: auto;
  }

  .rail-heading {
    min-width: 240px;
  }

  .level-rail > .level-choice {
    display: inline-flex;
    width: 190px;
    margin-right: 6px;
    vertical-align: top;
    border: 1px solid var(--line);
    border-radius: 10px;
  }

  .rail-note {
    min-width: 260px;
  }

  .editor-panel {
    padding: 16px;
    margin-bottom: 14px;
  }

  .two-columns,
  .readonly-strip,
  .task-binding,
  .preview-panel {
    grid-template-columns: 1fr;
  }

  .reward-row {
    flex-wrap: wrap;
  }

  .reward-item {
    flex-basis: calc(100% - 90px);
  }

  .reward-amount {
    flex: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .level-choice {
    transition: none;
  }
}
</style>

<template>
  <div class="safety-config-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">SAFETY GUARDIAN</p>
        <h1>安全守护案配置</h1>
        <p>维护第二关情境、第三关翻牌和第四关宣誓证书。保存草稿后再发布，不会把半成品直接展示给儿童。</p>
      </div>
      <a-space wrap>
        <a-tag :color="draftMeta.status === 'DRAFT' ? 'orange' : 'green'">{{ draftMeta.status || '已发布版本' }}</a-tag>
        <a-button :loading="validating" @click="validateDraft">校验配置</a-button>
        <a-button :loading="saving" @click="saveDraft">保存草稿</a-button>
        <a-button type="primary" :loading="publishing" @click="publishDraft">发布配置</a-button>
      </a-space>
    </header>

    <AdminPageError :message="loadError" :loading="loading" @retry="loadConfig" />

    <a-alert v-if="validationErrors.length" type="error" show-icon class="validation-alert">
      <template #message>发布前还需处理 {{ validationErrors.length }} 项</template>
      <template #description><ul><li v-for="error in validationErrors" :key="error">{{ error }}</li></ul></template>
    </a-alert>

    <a-tabs v-if="configData" v-model:active-key="activeTab" class="config-tabs">
      <a-tab-pane key="scenarios" tab="第二关 · 情境选择">
        <section class="section-heading"><div><h2>三选一图文情境</h2><p>主场景负责讲故事，三个行动卡负责做选择。</p></div><a-button type="primary" @click="addScenario">新增情境</a-button></section>
        <div class="editor-list">
          <article v-for="(scenario, index) in configData.scenarios" :key="scenario.id" class="editor-card">
            <div class="card-toolbar">
              <strong>{{ index + 1 }}. {{ scenario.title || '未命名情境' }}</strong>
              <a-space><a-switch v-model:checked="scenario.enabled" checked-children="启用" un-checked-children="停用" /><a-button size="small" :disabled="index === 0" @click="move(configData.scenarios, index, -1)">↑</a-button><a-button size="small" :disabled="index === configData.scenarios.length - 1" @click="move(configData.scenarios, index, 1)">↓</a-button><a-button size="small" danger @click="removeAt(configData.scenarios, index)">删除</a-button></a-space>
            </div>
            <div class="form-grid two">
              <a-form-item label="稳定编号"><a-input v-model:value="scenario.id" /></a-form-item>
              <a-form-item label="标题"><a-input v-model:value="scenario.title" /></a-form-item>
            </div>
            <a-form-item label="题干"><a-textarea v-model:value="scenario.prompt" :rows="2" /></a-form-item>
            <div class="media-row">
              <MediaPicker label="主场景图" :model="scenario" :visual-options="visualOptions" :asset-options="assetOptions.scenario" @upload="args => uploadAsset(args, scenario)" />
              <div class="media-copy"><a-form-item label="图片说明"><a-input v-model:value="scenario.imageAlt" /></a-form-item><div class="mini-preview"><img :src="previewSource(scenario)" :alt="scenario.imageAlt || '主场景预览'" /></div></div>
            </div>
            <h3 class="subheading">行动选项</h3>
            <div class="option-grid">
              <div v-for="option in scenario.options" :key="option.id" class="option-editor" :class="{ correct: scenario.correctOptionId === option.id }">
                <a-radio :checked="scenario.correctOptionId === option.id" @change="scenario.correctOptionId = option.id">正确答案</a-radio>
                <a-input v-model:value="option.id" placeholder="选项编号" />
                <a-input v-model:value="option.text" placeholder="行动文字" />
                <a-input v-model:value="option.imageAlt" placeholder="图片说明" />
                <a-select v-model:value="option.visualKey" :options="visualOptions" placeholder="内置图案" allow-clear />
                <a-select v-model:value="option.imagePath" :options="assetOptions.action" show-search option-filter-prop="label" placeholder="安全行动素材" allow-clear />
                <a-upload accept="image/png,image/jpeg,image/webp" :show-upload-list="false" :custom-request="args => uploadAsset(args, option)"><a-button block>{{ option.imagePath ? '替换小图' : '上传小图' }}</a-button></a-upload>
                <a-button v-if="option.imagePath" danger block @click="option.imagePath = ''">移除上传图</a-button>
                <img class="option-preview" :src="previewSource(option)" :alt="option.imageAlt || '行动卡预览'" />
              </div>
            </div>
            <a-form-item label="答案解析"><a-textarea v-model:value="scenario.explanation" :rows="2" /></a-form-item>
          </article>
        </div>
      </a-tab-pane>

      <a-tab-pane key="memory" tab="第三关 · 翻牌PK">
        <section class="section-heading"><div><h2>纯图案安全配对</h2><p>卡片不显示文字，配对成功后才展示知识解释。</p></div><a-button type="primary" @click="addPair">新增配对</a-button></section>
        <div class="level-builder">
          <a-form-item label="初级 2×2"><a-select v-model:value="configData.memory.levels.BEGINNER" mode="multiple" :options="pairOptions" /></a-form-item>
          <a-form-item label="中级 3×3"><a-select v-model:value="configData.memory.levels.INTERMEDIATE" mode="multiple" :options="pairOptions" /></a-form-item>
          <a-form-item label="高级 4×4"><a-select v-model:value="configData.memory.levels.ADVANCED" mode="multiple" :options="pairOptions" /></a-form-item>
          <div class="center-badge"><MediaPicker label="中心守护徽章" :model="configData.memory.centerBadge" :visual-options="visualOptions" :asset-options="assetOptions.centerBadge" @upload="args => uploadAsset(args, configData.memory.centerBadge)" /><a-input v-model:value="configData.memory.centerBadge.imageAlt" placeholder="徽章图片说明" /></div>
          <div v-for="level in memoryLevels" :key="level.id" class="level-badge-editor"><a-form-item :label="level.label"><a-select v-model:value="configData.memory.levelBadges[level.id]" :options="assetOptions.badge" show-search option-filter-prop="label" allow-clear placeholder="选择难度徽章" /></a-form-item><img :src="resolveMediaUrl(configData.memory.levelBadges[level.id])" :alt="level.label" /></div>
        </div>
        <div class="editor-list">
          <article v-for="(pair, index) in configData.memory.pairs" :key="pair.id" class="editor-card pair-card">
            <div class="card-toolbar"><strong>{{ index + 1 }}. {{ pair.name || '未命名配对' }}</strong><a-space><a-switch v-model:checked="pair.enabled" /><a-button size="small" :disabled="index === 0" @click="move(configData.memory.pairs, index, -1)">↑</a-button><a-button size="small" :disabled="index === configData.memory.pairs.length - 1" @click="move(configData.memory.pairs, index, 1)">↓</a-button><a-button size="small" danger @click="removeAt(configData.memory.pairs, index)">删除</a-button></a-space></div>
            <div class="form-grid two"><a-form-item label="稳定编号"><a-input v-model:value="pair.id" /></a-form-item><a-form-item label="配对名称"><a-input v-model:value="pair.name" /></a-form-item></div>
            <div class="pair-visuals">
              <div v-for="side in ['left', 'right']" :key="side" class="pair-side">
                <strong>{{ side === 'left' ? '情境图' : '安全行动图' }}</strong>
                <MediaPicker :label="side === 'left' ? '情境图' : '行动图'" :model="pair[side]" :visual-options="visualOptions" :asset-options="side === 'left' ? assetOptions.memorySituation : assetOptions.memoryAction" @upload="args => uploadAsset(args, pair[side])" />
                <a-input v-model:value="pair[side].imageAlt" placeholder="无障碍图片说明" />
                <img :src="previewSource(pair[side])" :alt="pair[side].imageAlt || '配对预览'" />
              </div>
            </div>
            <a-form-item label="配对解释"><a-input v-model:value="pair.explanation" /></a-form-item>
          </article>
        </div>
      </a-tab-pane>

      <a-tab-pane key="pledge" tab="第四关 · 宣誓证书">
        <section class="section-heading"><div><h2>逐句朗读</h2><p>孩子读完一句后点击确认，全部完成才展开证书。</p></div><a-button type="primary" @click="addPledgeLine">新增宣誓句</a-button></section>
        <div class="pledge-list">
          <article v-for="(line, index) in configData.pledge.lines" :key="line.id" class="pledge-editor">
            <span>{{ index + 1 }}</span><a-input v-model:value="line.title" placeholder="短标题" /><a-textarea v-model:value="line.text" :rows="2" placeholder="朗读内容" /><a-switch v-model:checked="line.enabled" /><a-space><a-button size="small" :disabled="index === 0" @click="move(configData.pledge.lines, index, -1)">↑</a-button><a-button size="small" :disabled="index === configData.pledge.lines.length - 1" @click="move(configData.pledge.lines, index, 1)">↓</a-button><a-button size="small" danger @click="removeAt(configData.pledge.lines, index)">删除</a-button></a-space>
          </article>
        </div>
        <section class="pledge-decoration-editor">
          <h2>宣誓装饰</h2>
          <div class="decoration-grid">
            <a-form-item label="祥云对话框"><a-select v-model:value="configData.pledge.dialogPath" :options="assetOptions.cloud" show-search option-filter-prop="label" allow-clear /><img :src="resolveMediaUrl(configData.pledge.dialogPath)" alt="祥云对话框预览" /></a-form-item>
            <a-form-item label="点亮铃铛"><a-select v-model:value="configData.pledge.bellPath" :options="assetOptions.bell" show-search option-filter-prop="label" allow-clear /><img :src="resolveMediaUrl(configData.pledge.bellPath)" alt="点亮铃铛预览" /></a-form-item>
          </div>
        </section>
        <section class="certificate-editor">
          <div class="certificate-form">
            <h2>证书内容</h2>
            <a-form layout="vertical"><a-form-item label="证书标题"><a-input v-model:value="configData.pledge.certificate.title" /></a-form-item><a-form-item label="荣誉称号"><a-input v-model:value="configData.pledge.certificate.honor" /></a-form-item><a-form-item label="颁发方"><a-input v-model:value="configData.pledge.certificate.issuer" /></a-form-item><a-form-item label="祝福语"><a-textarea v-model:value="configData.pledge.certificate.blessing" :rows="3" /></a-form-item></a-form>
            <div class="certificate-assets"><div v-for="field in certificateAssetFields" :key="field.key"><a-select v-model:value="configData.pledge.certificate[field.key]" :options="assetOptions[field.key]" show-search option-filter-prop="label" allow-clear :placeholder="`选择${field.label}`" /><a-upload accept="image/png,image/jpeg,image/webp" :show-upload-list="false" :custom-request="args => uploadCertificateAsset(args, field.key)"><a-button>{{ field.label }}</a-button></a-upload><a-button v-if="configData.pledge.certificate[field.key]" size="small" danger @click="configData.pledge.certificate[field.key] = ''">移除</a-button></div></div>
          </div>
          <div class="certificate-preview" :style="certificatePreviewStyle"><small>{{ configData.pledge.certificate.issuer }}</small><h2>{{ configData.pledge.certificate.title }}</h2><p>授予 <strong>小侦探</strong></p><h3>{{ configData.pledge.certificate.honor }}</h3><p>{{ configData.pledge.certificate.blessing }}</p><span>示例预览 · 2026年9月8日</span></div>
        </section>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref, resolveComponent } from 'vue'
import { message } from 'ant-design-vue'
import AdminPageError from '@/components/backend/AdminPageError.vue'
import { uploadImage } from '@/api/FileApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import { getAdminSafetyGameConfig, publishSafetyGameDraft, saveSafetyGameDraft, validateSafetyGameDraft } from '@/api/SafetyGameConfigApi'
import builtInPreview from '@/assets/copper-detective-guide-512.png'
import badgePreview from '@/assets/素材/20. 安全观察徽章.png'
import { actionAssetFor, memoryAssetFor, scenarioAssetFor, safetyAssetOptions, safetyGuardianAssets } from '@/data/safetyGuardianAssets'

const visualOptions = [
  'guardian-safety', 'guardian-help', 'guardian-stop', 'guardian-think', 'guardian-teacher', 'guardian-video',
  'guardian-family', 'guardian-doctor', 'guardian-pet', 'guardian-learn', 'safety-badge', 'safety-bell'
].map(value => ({ value, label: value }))

const MediaPicker = defineComponent({
  props: { label: String, model: Object, visualOptions: Array, assetOptions: Array },
  emits: ['upload'],
  setup(props, { emit }) {
    const ASelect = resolveComponent('a-select')
    const AUpload = resolveComponent('a-upload')
    const AButton = resolveComponent('a-button')
    function updateVisual(value) {
      // eslint-disable-next-line vue/no-mutating-props
      props.model.visualKey = value
    }
    function removeImage() {
      // eslint-disable-next-line vue/no-mutating-props
      props.model.imagePath = ''
    }
    function updateAsset(value) {
      // eslint-disable-next-line vue/no-mutating-props
      props.model.imagePath = value || ''
    }
    return () => h('div', { class: 'media-picker' }, [
      h('strong', props.label),
      h(ASelect, { value: props.model.visualKey, 'onUpdate:value': updateVisual, options: props.visualOptions, allowClear: true, placeholder: '内置图案' }),
      props.assetOptions?.length ? h(ASelect, { value: props.model.imagePath, 'onUpdate:value': updateAsset, options: props.assetOptions, allowClear: true, showSearch: true, optionFilterProp: 'label', placeholder: '安全守护案素材' }) : null,
      h(AUpload, { accept: 'image/png,image/jpeg,image/webp', showUploadList: false, customRequest: args => emit('upload', args) }, { default: () => h(AButton, null, { default: () => props.model.imagePath ? '替换图片' : '上传图片' }) }),
      props.model.imagePath ? h(AButton, { danger: true, onClick: removeImage }, { default: () => '移除上传图' }) : null
    ])
  }
})

const loading = ref(false); const saving = ref(false); const publishing = ref(false); const validating = ref(false)
const loadError = ref(''); const activeTab = ref('scenarios'); const validationErrors = ref([])
const configData = ref(null)
const draftMeta = ref({ revisionId: null, editVersion: null, status: null })
const certificateAssetFields = [{ key: 'backgroundPath', label: '上传证书背景' }, { key: 'badgePath', label: '上传徽章' }, { key: 'sealPath', label: '上传签章' }]
const memoryLevels = [{ id: 'BEGINNER', label: '初级徽章' }, { id: 'INTERMEDIATE', label: '中级徽章' }, { id: 'ADVANCED', label: '高级徽章' }]
const toAssetOptions = items => items.map(item => ({ value: item.value, label: item.label }))
const assetOptions = {
  scenario: toAssetOptions(safetyAssetOptions.filter(item => item.group === '第二关 · 主场景')),
  action: toAssetOptions(safetyAssetOptions.filter(item => item.group === '第二关 · 行动选项')),
  memorySituation: toAssetOptions(safetyAssetOptions.filter(item => item.group === '第三关 · 情境图')),
  memoryAction: toAssetOptions(safetyAssetOptions.filter(item => item.group === '第三关 · 安全行动图')),
  badge: toAssetOptions(safetyAssetOptions.filter(item => item.group === '第三关 · 难度徽章')),
  centerBadge: toAssetOptions(safetyAssetOptions.filter(item => item.group === '第三关 · 中心徽章')),
  cloud: toAssetOptions([safetyGuardianAssets.pledge.cloud]),
  bell: toAssetOptions([safetyGuardianAssets.pledge.bell]),
  backgroundPath: toAssetOptions([safetyGuardianAssets.pledge.background]),
  badgePath: toAssetOptions([safetyGuardianAssets.pledge.badge]),
  sealPath: toAssetOptions([safetyGuardianAssets.pledge.seal])
}

const pairOptions = computed(() => (configData.value?.memory?.pairs || []).filter(pair => pair.enabled !== false).map(pair => ({ value: pair.id, label: pair.name || pair.id })))
const certificatePreviewStyle = computed(() => configData.value?.pledge?.certificate?.backgroundPath ? { backgroundImage: `url(${resolveMediaUrl(configData.value.pledge.certificate.backgroundPath)})` } : {})
const clone = value => JSON.parse(JSON.stringify(value))

async function loadConfig() {
  loading.value = true; loadError.value = ''
  try {
    const data = await getAdminSafetyGameConfig()
    const source = data?.draft || data?.published
    configData.value = applySafetyAssetDefaults(clone(source?.config || {}))
    draftMeta.value = { revisionId: data?.draft?.revisionId || null, editVersion: data?.draft?.editVersion || null, status: data?.draft?.status || null }
  } catch (error) { loadError.value = error?.message || '安全守护案配置加载失败' } finally { loading.value = false }
}

function applySafetyAssetDefaults(config) {
  const target = config || {}
  target.scenarios = (target.scenarios || []).map(scenario => ({
    ...scenario,
    imagePath: scenario.imagePath || scenarioAssetFor(scenario.id)?.file || '',
    options: (scenario.options || []).map(option => ({
      ...option,
      imagePath: option.imagePath || actionAssetFor(option.id)?.file || ''
    }))
  }))
  target.memory = target.memory || {}
  target.memory.centerBadge = { ...(target.memory.centerBadge || {}), imagePath: target.memory.centerBadge?.imagePath || safetyGuardianAssets.badges.CENTER.file, imageAlt: target.memory.centerBadge?.imageAlt || safetyGuardianAssets.badges.CENTER.alt }
  target.memory.levelBadges = { BEGINNER: target.memory.levelBadges?.BEGINNER || safetyGuardianAssets.badges.BEGINNER.file, INTERMEDIATE: target.memory.levelBadges?.INTERMEDIATE || safetyGuardianAssets.badges.INTERMEDIATE.file, ADVANCED: target.memory.levelBadges?.ADVANCED || safetyGuardianAssets.badges.ADVANCED.file }
  target.memory.pairs = (target.memory.pairs || []).map(pair => ({
    ...pair,
    left: { ...(pair.left || {}), imagePath: pair.left?.imagePath || memoryAssetFor(pair.id, 'left')?.file || '' },
    right: { ...(pair.right || {}), imagePath: pair.right?.imagePath || memoryAssetFor(pair.id, 'right')?.file || '' }
  }))
  target.pledge = target.pledge || {}
  target.pledge.dialogPath = target.pledge.dialogPath || safetyGuardianAssets.pledge.cloud.file
  target.pledge.bellPath = target.pledge.bellPath || safetyGuardianAssets.pledge.bell.file
  target.pledge.certificate = { ...(target.pledge.certificate || {}), backgroundPath: target.pledge.certificate?.backgroundPath || safetyGuardianAssets.pledge.background.file, badgePath: target.pledge.certificate?.badgePath || safetyGuardianAssets.pledge.badge.file, sealPath: target.pledge.certificate?.sealPath || safetyGuardianAssets.pledge.seal.file }
  return target
}

async function saveDraft() {
  if (!configData.value || saving.value) return
  saving.value = true
  try {
    const result = await saveSafetyGameDraft({ ...draftMeta.value, config: configData.value })
    draftMeta.value = { revisionId: result.revisionId, editVersion: result.editVersion, status: result.status }
    message.success('草稿已保存')
  } catch (error) { message.error(error?.message || '草稿保存失败') } finally { saving.value = false }
}
async function validateDraft() {
  validating.value = true
  try { const result = await validateSafetyGameDraft({ config: configData.value }); validationErrors.value = result?.errors || []; if (result?.valid) message.success('配置校验通过') } catch (error) { message.error(error?.message || '校验失败') } finally { validating.value = false }
}
async function publishDraft() {
  if (!draftMeta.value.revisionId) { message.warning('请先保存草稿'); return }
  publishing.value = true
  try {
    const validation = await validateSafetyGameDraft({ config: configData.value })
    validationErrors.value = validation?.errors || []
    if (!validation?.valid) return
    await publishSafetyGameDraft({ revisionId: draftMeta.value.revisionId, editVersion: draftMeta.value.editVersion })
    message.success('安全守护案配置已发布'); await loadConfig()
  } catch (error) { message.error(error?.message || '发布失败') } finally { publishing.value = false }
}

function move(list, index, delta) { const target = index + delta; if (target < 0 || target >= list.length) return; [list[index], list[target]] = [list[target], list[index]] }
function removeAt(list, index) { list.splice(index, 1) }
function uid(prefix) { return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}` }
function addScenario() { configData.value.scenarios.push({ id: uid('scenario'), title: '', prompt: '', imagePath: '', imageAlt: '', visualKey: 'guardian-safety', correctOptionId: '', explanation: '', enabled: false, options: [0, 1, 2].map(() => ({ id: uid('option'), text: '', imagePath: '', imageAlt: '', visualKey: 'guardian-think' })) }) }
function addPair() { configData.value.memory.pairs.push({ id: uid('pair'), name: '', enabled: false, explanation: '', left: { imagePath: '', imageAlt: '', visualKey: 'guardian-think' }, right: { imagePath: '', imageAlt: '', visualKey: 'guardian-help' } }) }
function addPledgeLine() { configData.value.pledge.lines.push({ id: uid('pledge'), title: '', text: '', enabled: false }) }
function previewSource(model) { return model?.imagePath ? resolveMediaUrl(model.imagePath) : model?.visualKey === 'safety-badge' ? badgePreview : builtInPreview }
async function uploadAsset({ file, onSuccess, onError }, model) { try { const path = await uploadImage(file, { showDefaultMsg: false }); model.imagePath = typeof path === 'string' ? path : path?.url || path?.filePath || ''; onSuccess?.(path); message.success('图片上传成功') } catch (error) { onError?.(error); message.error(error?.message || '图片上传失败') } }
function uploadCertificateAsset(args, field) { return uploadAsset(args, { get imagePath() { return configData.value.pledge.certificate[field] }, set imagePath(value) { configData.value.pledge.certificate[field] = value } }) }

onMounted(loadConfig)
</script>

<style scoped>
.safety-config-page{min-height:100%;padding:28px;background:#f4f7f5;color:#263b34}.page-header{display:flex;justify-content:space-between;gap:24px;align-items:flex-start;padding:28px 30px;border:1px solid #dbe6e0;border-radius:22px;background:linear-gradient(135deg,#fffdf4,#edf7f1);box-shadow:0 12px 32px rgba(35,81,64,.08)}.page-header h1{margin:2px 0 8px;font-size:30px}.page-header p{margin:0;max-width:720px;color:#65756e}.eyebrow{color:#a66b1d!important;font-size:12px;font-weight:900;letter-spacing:3px}.validation-alert,.config-tabs{margin-top:20px}.config-tabs{padding:24px;border-radius:20px;background:#fff}.section-heading,.card-toolbar{display:flex;justify-content:space-between;gap:16px;align-items:center}.section-heading{margin-bottom:18px}.section-heading h2{margin:0 0 4px}.section-heading p{margin:0;color:#78867f}.editor-list{display:grid;gap:16px}.editor-card{padding:20px;border:1px solid #dce6e1;border-radius:18px;background:#fbfdfc}.card-toolbar{margin-bottom:18px}.form-grid.two{display:grid;grid-template-columns:1fr 1fr;gap:16px}.media-row{display:grid;grid-template-columns:260px 1fr;gap:18px;padding:14px;border-radius:14px;background:#f2f7f4}.media-picker{display:grid;align-content:start;gap:10px}.media-copy{display:grid;grid-template-columns:1fr 180px;gap:14px}.mini-preview,.option-preview,.pair-side img{overflow:hidden;border-radius:12px;background:#e8efeb}.mini-preview img,.option-preview,.pair-side img{width:100%;height:120px;object-fit:cover}.subheading{margin:18px 0 10px}.option-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:16px}.option-editor{display:grid;gap:9px;padding:12px;border:2px solid #e4eae7;border-radius:14px;background:#fff}.option-editor.correct{border-color:#4a987b;background:#f1faf6}.level-builder{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-bottom:20px;padding:18px;border-radius:16px;background:#f4f8f6}.center-badge{grid-column:1/-1;display:grid;grid-template-columns:260px 1fr;gap:14px}.pair-visuals{display:grid;grid-template-columns:1fr 1fr;gap:16px;margin:14px 0}.pair-side{display:grid;gap:9px;padding:14px;border-radius:14px;background:#f2f7f4}.pair-side img{height:170px}.pledge-list{display:grid;gap:10px}.pledge-editor{display:grid;grid-template-columns:36px 180px 1fr auto auto;gap:10px;align-items:center;padding:12px;border:1px solid #e1e8e4;border-radius:14px}.pledge-editor>span{display:grid;width:32px;height:32px;place-items:center;border-radius:50%;background:#27715a;color:#fff;font-weight:900}.certificate-editor{display:grid;grid-template-columns:1fr 1fr;gap:24px;margin-top:24px}.certificate-form,.certificate-preview{padding:24px;border-radius:18px;background:#f4f8f6}.certificate-assets{display:flex;gap:8px;flex-wrap:wrap}.certificate-preview{display:flex;min-height:360px;flex-direction:column;justify-content:center;align-items:center;border:8px double #c28a37;background-color:#fff8e8;background-position:center;background-size:cover;text-align:center}.certificate-preview h2{font-size:30px;color:#7c4e16}.certificate-preview h3{font-size:25px;color:#246b56}@media(max-width:900px){.page-header,.section-heading{align-items:stretch;flex-direction:column}.option-grid,.level-builder,.certificate-editor{grid-template-columns:1fr}.form-grid.two,.pair-visuals,.media-row{grid-template-columns:1fr}.media-copy{grid-template-columns:1fr}.center-badge{grid-column:auto;grid-template-columns:1fr}.pledge-editor{grid-template-columns:36px 1fr}.pledge-editor textarea{grid-column:1/-1}}
.level-badge-editor{display:grid;grid-template-columns:1fr 88px;gap:12px;align-items:center;padding:10px 0}.level-badge-editor img{width:78px;height:78px;object-fit:contain}.certificate-assets>div{display:grid;grid-template-columns:minmax(220px,1fr) auto auto;gap:8px;align-items:center}.pledge-decoration-editor{margin-top:24px;padding:20px;border-radius:18px;background:#f4f8f6}.pledge-decoration-editor h2{margin-top:0}.decoration-grid{display:grid;grid-template-columns:1fr 1fr;gap:18px}.decoration-grid img{display:block;width:100%;height:90px;object-fit:contain;margin-top:8px;background:#fff;border-radius:10px}@media(max-width:900px){.decoration-grid{grid-template-columns:1fr}}
</style>

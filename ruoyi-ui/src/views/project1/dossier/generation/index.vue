<template>
  <div class="app-container dossier-generation-page">
    <div class="page-head">
      <div>
        <h2>卷宗生成控制</h2>
        <div class="subline">当前：数字卷宗 / 卷宗生成管理 / 卷宗生成控制</div>
      </div>
      <div class="actions">
        <el-button icon="Refresh" :loading="loading" @click="refreshPage">刷新数据</el-button>
        <el-button icon="Back" @click="goDossierInstance">返回卷宗实例管理</el-button>
        <el-button type="primary" plain icon="CircleCheck" :loading="checking" :disabled="!selectedAircraftId" @click="handlePrecheck">
          生成前检查
        </el-button>
        <el-button type="primary" icon="CaretRight" :loading="generating" :disabled="!canStartGeneration" @click="handleStartGeneration">
          开始生成
        </el-button>
      </div>
    </div>

    <div class="overview">
      <div class="overview-main">
        <div class="overview-title">
          <span>{{ aircraftName }}</span>
          <el-tag type="primary" size="small">{{ activeTemplate.templateVersion || 'V1.0' }}</el-tag>
          <el-tag type="success" size="small">{{ generationStateLabel }}</el-tag>
        </div>
        <div class="selector-row">
          <el-select v-model="selectedModelId" placeholder="选择机型" filterable @change="handleModelChange">
            <el-option v-for="item in modelOptions" :key="item.modelId" :label="modelLabel(item)" :value="item.modelId" />
          </el-select>
          <el-select v-model="selectedAircraftId" placeholder="选择机号" filterable :disabled="!selectedModelId" @change="loadPrepareData">
            <el-option v-for="item in aircraftOptions" :key="item.aircraftId" :label="aircraftLabel(item)" :value="item.aircraftId" />
          </el-select>
          <el-select v-model="selectedTemplateId" placeholder="选择模板" filterable @change="loadPrepareData">
            <el-option
              v-for="item in templateOptions"
              :key="item.templateId"
              :label="templateLabel(item)"
              :value="item.templateId"
            >
              <span>{{ templateLabel(item) }}</span>
            </el-option>
          </el-select>
        </div>
        <div v-if="selectedAircraftId" class="selection-summary">
          <span>机号：{{ selectedAircraft.tailNumber || '-' }}</span>
          <span>MSN：{{ selectedAircraft.msn || '-' }}</span>
          <span>模板版本：{{ activeTemplate.templateVersion || '-' }}</span>
          <span>状态：{{ selectedAircraft.operationalStatus || '-' }}</span>
        </div>
      </div>
      <div class="metric"><strong>{{ metricValue('chapterCount') }}</strong><span>模板章节</span></div>
      <div class="metric"><strong>{{ metricValue('sourceCount') }}</strong><span>数据来源</span></div>
      <div class="metric"><strong>{{ metricValue('ruleCount') }}</strong><span>检查规则</span></div>
    </div>

    <div v-loading="loading" class="workspace">
      <section class="console-panel">
        <div class="panel-head">
          <span>生成流程</span>
          <el-tag :type="stepTagType" size="small">{{ generationStateLabel }}</el-tag>
        </div>
        <el-steps :active="flowActiveStep" finish-status="success" process-status="process" simple class="generation-steps">
          <el-step title="选择飞机" />
          <el-step title="生成前检查" />
          <el-step title="开始生成" />
          <el-step title="生成结果" />
        </el-steps>

        <el-tabs v-model="activeTab" class="console-tabs">
          <el-tab-pane v-if="hasPrecheckResult" label="生成前检查" name="check">
            <div class="check-summary">
              <div class="summary-item success">
                <strong>{{ checkSummary.passCount || 0 }}</strong>
                <span>通过</span>
              </div>
              <div class="summary-item warning">
                <strong>{{ checkSummary.warningCount || 0 }}</strong>
                <span>提示</span>
              </div>
              <div class="summary-item danger">
                <strong>{{ checkSummary.blockingCount || 0 }}</strong>
                <span>阻断</span>
              </div>
              <div class="summary-item">
                <strong>{{ checkSummary.checkedObjectCount || 0 }}</strong>
                <span>检查对象</span>
              </div>
            </div>
            <el-table :data="checks" border>
              <el-table-column label="检查项" prop="name" min-width="150" />
              <el-table-column label="结果" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="对象数" prop="checkedCount" width="90" align="right" />
              <el-table-column label="问题数" prop="issueCount" width="90" align="right" />
              <el-table-column label="检查结论" prop="message" min-width="280" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="模板数据来源" name="source">
            <el-table :data="chapterSources" border height="430">
              <el-table-column label="章节" min-width="260" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ chapterSourceName(row) }}
                </template>
              </el-table-column>
              <el-table-column label="适用对象" prop="applyObjectType" width="110" align="center">
                <template #default="{ row }">
                  {{ applyObjectTypeLabel(row.applyObjectType) }}
                </template>
              </el-table-column>
              <el-table-column label="来源表" prop="sourceTable" width="190" show-overflow-tooltip />
              <el-table-column label="来源名称" prop="sourceName" min-width="220" show-overflow-tooltip />
              <el-table-column label="是否必填" prop="requiredFlag" width="96" align="center">
                <template #default="{ row }">
                  <el-tag :type="Number(row.requiredFlag) === 1 ? 'danger' : 'info'" size="small">
                    {{ Number(row.requiredFlag) === 1 ? '必填' : '可选' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane v-if="hasGenerationTask" label="生成任务" name="job">
            <div v-if="jobInfo.jobId || canOpenDetail" class="job-layout">
              <div class="job-main">
                <div class="job-title">
                  <div>
                    <strong>{{ jobInfo.jobCode }}</strong>
                    <span>{{ outputInfo.fileName }}</span>
                  </div>
                  <el-tag :type="jobStatusType(jobInfo.jobStatus)" size="small">{{ jobStatusLabel(jobInfo.jobStatus) }}</el-tag>
                </div>
                <el-progress :percentage="Number(jobInfo.progressPercent || 0)" :stroke-width="12" />
                <el-table :data="jobLogs" border class="job-log-table">
                  <el-table-column label="时间" prop="logTime" width="170" />
                  <el-table-column label="阶段" prop="message" min-width="220" />
                  <el-table-column label="进度" prop="progress" width="90" align="right" />
                  <el-table-column label="详情" prop="detail" min-width="220" show-overflow-tooltip />
                </el-table>
              </div>
              <div class="output-panel">
                <div class="panel-head">
                  <span>生成结果</span>
                  <el-tag type="success" size="small">{{ outputInfo.exportFormat || 'PDF+ZIP' }}</el-tag>
                </div>
                <div class="info-grid single">
                  <div class="info-item">
                    <label>文件数</label>
                    <strong>{{ outputInfo.fileCount || 0 }}</strong>
                  </div>
                  <div class="info-item">
                    <label>记录数</label>
                    <strong>{{ outputInfo.sourceRecordCount || 0 }}</strong>
                  </div>
                  <div class="info-item">
                    <label>弯管记录</label>
                    <strong>{{ outputInfo.tubeRecordCount || outputInfo.keyNodeRecordCount || 0 }}</strong>
                  </div>
                </div>
                <div class="output-path">{{ outputInfo.outputPath }}</div>
                <div class="result-actions">
                  <el-alert
                    v-if="isDuplicateResult"
                    title="已有相同数据和模板生成的卷宗，未重复生成新版本。"
                    type="warning"
                    show-icon
                    :closable="false"
                  />
                  <el-button type="primary" icon="View" :disabled="!canOpenDetail" @click="goDossierDetail">
                    查看卷宗详情
                  </el-button>
                </div>
                <el-table :data="outputInfo.sections || []" border>
                  <el-table-column label="章节" prop="name" min-width="140" />
                </el-table>
              </div>
            </div>
            <el-empty v-else description="暂无生成任务" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<script setup name="DossierGeneration">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  listGenerationModels,
  listGenerationAircraft,
  listGenerationTemplates,
  prepareGeneration,
  runGenerationPrecheck,
  startGeneration
} from '@/api/project1/dossier/generation'

const router = useRouter()
const loading = ref(false)
const checking = ref(false)
const generating = ref(false)
const activeTab = ref('source')
const modelOptions = ref([])
const aircraftOptions = ref([])
const templateOptions = ref([])
const selectedModelId = ref('')
const selectedAircraftId = ref('')
const selectedTemplateId = ref('')
const prepareData = ref({})
const precheckResult = ref(null)
const generationResult = ref(null)

const selectedAircraft = computed(() => prepareData.value.aircraft || {})
const activeTemplate = computed(() => {
  return prepareData.value.template
    || templateOptions.value.find(item => item.templateId === selectedTemplateId.value)
    || {}
})
const metrics = computed(() => prepareData.value.metrics || {})
const chapterSources = computed(() => {
  return sortChapterSourcesByHierarchy(prepareData.value.chapterSources || [], prepareData.value.chapters || [])
})
const hasPrecheckResult = computed(() => !!precheckResult.value)
const checks = computed(() => {
  if (precheckResult.value && precheckResult.value.checks) {
    return precheckResult.value.checks
  }
  return []
})
const checkSummary = computed(() => {
  if (precheckResult.value && precheckResult.value.summary) {
    return precheckResult.value.summary
  }
  return {}
})
const jobInfo = computed(() => {
  return (generationResult.value && generationResult.value.job) || {}
})
const hasGenerationTask = computed(() => !!jobInfo.value.jobId || !!generationResult.value)
const jobLogs = computed(() => {
  return (generationResult.value && generationResult.value.logs) || []
})
const outputInfo = computed(() => {
  return (generationResult.value && generationResult.value.output) || (jobInfo.value.output || {})
})
const isDuplicateResult = computed(() => !!(generationResult.value && generationResult.value.duplicated))
const hasFinishedGeneration = computed(() => {
  return !!generationResult.value && (isDuplicateResult.value || jobInfo.value.jobStatus === 'succeeded')
})
const resultInstanceId = computed(() => {
  return (generationResult.value && (generationResult.value.instanceId || generationResult.value.existingInstanceId)) || ''
})
const resultVersionId = computed(() => {
  if (!generationResult.value) {
    return ''
  }
  return generationResult.value.versionId
    || generationResult.value.existingVersionId
    || (generationResult.value.version && generationResult.value.version.versionId)
    || jobInfo.value.versionId
    || ''
})
const canOpenDetail = computed(() => !!resultInstanceId.value && !!resultVersionId.value)
const canStartGeneration = computed(() => {
  return selectedAircraftId.value
    && selectedTemplateId.value
    && precheckResult.value
    && !generating.value
    && !hasFinishedGeneration.value
    && (checkSummary.value.blockingCount || 0) === 0
})
const flowActiveStep = computed(() => {
  if (jobInfo.value.jobStatus === 'succeeded') {
    return 4
  }
  if (generationResult.value) {
    return 3
  }
  if (precheckResult.value) {
    return 2
  }
  return selectedAircraftId.value ? 1 : 0
})
const aircraftName = computed(() => {
  if (!selectedAircraft.value.tailNumber) {
    return '单台份飞机综合卷宗'
  }
  return `${selectedAircraft.value.tailNumber} ${activeTemplate.value.templateName || '单台份飞机综合卷宗'}`
})
const generationStateLabel = computed(() => {
  if (isDuplicateResult.value) {
    return '已有卷宗'
  }
  if (jobInfo.value.jobStatus === 'succeeded') {
    return '已生成'
  }
  if (generationResult.value) {
    return '生成中'
  }
  if (precheckResult.value) {
    return '检查通过'
  }
  return '待检查'
})
const stepTagType = computed(() => {
  if (jobInfo.value.jobStatus === 'succeeded') {
    return 'success'
  }
  if (precheckResult.value) {
    return 'warning'
  }
  return 'info'
})

async function initPage() {
  loading.value = true
  try {
    await loadPageData(false)
  } finally {
    loading.value = false
  }
}

async function refreshPage() {
  loading.value = true
  try {
    await loadPageData(true)
  } finally {
    loading.value = false
  }
}

async function loadPageData(preserveSelection) {
  const previousModelId = preserveSelection ? selectedModelId.value : ''
  const previousAircraftId = preserveSelection ? selectedAircraftId.value : ''
  const previousTemplateId = preserveSelection ? selectedTemplateId.value : ''
  const [modelRes, templateRes] = await Promise.all([
    listGenerationModels(),
    listGenerationTemplates()
  ])
  modelOptions.value = modelRes.data || []
  templateOptions.value = templateRes.data || []
  selectedTemplateId.value = resolveTemplateId(previousTemplateId)
  selectedModelId.value = resolveModelId(previousModelId)
  await loadAircraftOptions(previousAircraftId)
}

function resolveModelId(preferredId) {
  if (preferredId && modelOptions.value.some(item => item.modelId === preferredId)) {
    return preferredId
  }
  const defaultModel = modelOptions.value.find(item => item.isDefault === 1 || item.defaultFlag === 1)
    || modelOptions.value[0]
  return defaultModel ? defaultModel.modelId : ''
}

function resolveTemplateId(preferredId) {
  if (preferredId && templateOptions.value.some(item => item.templateId === preferredId)) {
    return preferredId
  }
  const defaultTemplate = templateOptions.value.find(item => item.isDefault === 1 && item.status === 'active')
    || templateOptions.value.find(item => item.status === 'active')
    || templateOptions.value[0]
  return defaultTemplate ? defaultTemplate.templateId : ''
}

async function handleModelChange() {
  await loadAircraftOptions()
}

async function loadAircraftOptions(preferredAircraftId = '') {
  if (!selectedModelId.value) {
    aircraftOptions.value = []
    selectedAircraftId.value = ''
    await loadPrepareData()
    return
  }
  const aircraftRes = await listGenerationAircraft({ modelId: selectedModelId.value })
  aircraftOptions.value = aircraftRes.data || []
  selectedAircraftId.value = resolveAircraftId(preferredAircraftId)
  await loadPrepareData()
}

function resolveAircraftId(preferredAircraftId) {
  if (preferredAircraftId && aircraftOptions.value.some(item => item.aircraftId === preferredAircraftId)) {
    return preferredAircraftId
  }
  const defaultAircraft = aircraftOptions.value.find(item => item.isDefault === 1 || item.defaultFlag === 1)
    || aircraftOptions.value[0]
  return defaultAircraft ? defaultAircraft.aircraftId : ''
}

async function loadPrepareData() {
  precheckResult.value = null
  generationResult.value = null
  activeTab.value = 'source'
  if (!selectedAircraftId.value || !selectedTemplateId.value) {
    prepareData.value = {}
    return
  }
  const res = await prepareGeneration(selectedAircraftId.value, { templateId: selectedTemplateId.value })
  prepareData.value = res.data || {}
}

async function handlePrecheck() {
  checking.value = true
  try {
    const res = await runGenerationPrecheck({
      aircraftId: selectedAircraftId.value,
      templateId: selectedTemplateId.value
    })
    precheckResult.value = res.data || {}
    activeTab.value = 'check'
    ElMessage.success('生成前检查完成')
  } finally {
    checking.value = false
  }
}

async function handleStartGeneration() {
  generating.value = true
  try {
    const res = await startGeneration({
      aircraftId: selectedAircraftId.value,
      templateId: selectedTemplateId.value,
      precheckRunId: precheckResult.value && precheckResult.value.runId
    })
    generationResult.value = res.data || {}
    activeTab.value = 'job'
    if (generationResult.value.duplicated) {
      ElMessage.warning(generationResult.value.message || '已有相同卷宗，未重复生成')
    } else {
      ElMessage.success('卷宗生成完成')
    }
  } finally {
    generating.value = false
  }
}

function goDossierDetail() {
  if (!canOpenDetail.value) {
    return
  }
  router.push({
    path: '/dossier/manage/detail',
    query: {
      instanceId: resultInstanceId.value,
      versionId: resultVersionId.value
    }
  })
}

function goDossierInstance() {
  router.push('/dossier/manage/instance')
}

function metricValue(key) {
  return metrics.value[key] || 0
}

function modelLabel(item) {
  return `${item.modelCode} / ${item.modelName || item.modelCode}`
}

function aircraftLabel(item) {
  return `${item.tailNumber} / MSN ${item.msn || '-'}`
}

function templateLabel(item) {
  const statusText = item.status === 'draft' ? '草稿' : '启用'
  const defaultText = item.isDefault === 1 ? ' / 默认' : ''
  return `${item.templateName} / ${item.templateVersion} / ${statusText}${defaultText}`
}

function chapterSourceName(row) {
  return row.chapterDisplayName || row.chapterName || '-'
}

function sortChapterSourcesByHierarchy(sources, chapters) {
  if (!Array.isArray(sources)) {
    return []
  }
  const hierarchyMap = buildChapterHierarchyMap(chapters)
  return sources.map(item => {
    const chapterInfo = hierarchyMap[item.chapterId]
    return {
      ...item,
      chapterDisplayName: item.chapterDisplayName || chapterInfo?.displayName || item.chapterName,
      chapterHierarchyOrder: item.chapterHierarchyOrder || chapterInfo?.order || '9999',
      chapterDepth: item.chapterDepth || chapterInfo?.depth
    }
  }).sort((left, right) => {
    const chapterCompare = String(left.chapterHierarchyOrder || '9999').localeCompare(String(right.chapterHierarchyOrder || '9999'))
    if (chapterCompare !== 0) {
      return chapterCompare
    }
    const sourceCompare = Number(left.sortOrder || 0) - Number(right.sortOrder || 0)
    if (sourceCompare !== 0) {
      return sourceCompare
    }
    return String(left.sourceId || '').localeCompare(String(right.sourceId || ''))
  })
}

function buildChapterHierarchyMap(chapters) {
  const hierarchyMap = {}
  if (!Array.isArray(chapters)) {
    return hierarchyMap
  }
  const nodeMap = {}
  const roots = []
  chapters.forEach(item => {
    const id = item.chapterId || item.id
    if (id) {
      nodeMap[id] = { ...item, id, children: [] }
    }
  })
  Object.values(nodeMap).forEach(node => {
    if (node.parentId && nodeMap[node.parentId]) {
      nodeMap[node.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })
  walkChapterTree(roots, hierarchyMap, '', '', 1)
  return hierarchyMap
}

function walkChapterTree(nodes, hierarchyMap, parentName, parentOrder, depth) {
  nodes.sort(compareChapterNode).forEach((node, index) => {
    const name = node.chapterName || node.id
    const order = parentOrder ? `${parentOrder}.${orderSegment(index)}` : orderSegment(index)
    const displayName = parentName ? `${parentName} / ${name}` : name
    hierarchyMap[node.id] = {
      displayName,
      order,
      depth
    }
    walkChapterTree(node.children || [], hierarchyMap, displayName, order, depth + 1)
  })
}

function compareChapterNode(left, right) {
  const sortCompare = Number(left.sortOrder || 0) - Number(right.sortOrder || 0)
  if (sortCompare !== 0) {
    return sortCompare
  }
  const codeCompare = String(left.chapterCode || '').localeCompare(String(right.chapterCode || ''))
  if (codeCompare !== 0) {
    return codeCompare
  }
  return String(left.chapterName || '').localeCompare(String(right.chapterName || ''))
}

function orderSegment(index) {
  return String(index + 1).padStart(4, '0')
}

function applyObjectTypeLabel(type) {
  const map = {
    all: '全部',
    aircraft: '整机',
    system: '系统',
    subsystem: '子系统',
    equipment: '设备',
    component: '组件',
    part: '零件',
    tube: '弯管',
    document: '文档',
    bom_node: 'BOM节点',
    node: '节点'
  }
  return map[type] || type || '-'
}

function statusType(status) {
  if (status === 'pass') return 'success'
  if (status === 'warning') return 'warning'
  if (status === 'error') return 'danger'
  return 'info'
}

function statusLabel(status) {
  if (status === 'pass') return '通过'
  if (status === 'warning') return '提示'
  if (status === 'error') return '阻断'
  return '待检查'
}

function jobStatusType(status) {
  if (status === 'succeeded') return 'success'
  if (status === 'failed') return 'danger'
  if (status === 'running') return 'warning'
  return 'info'
}

function jobStatusLabel(status) {
  if (status === 'succeeded') return '完成'
  if (status === 'failed') return '失败'
  if (status === 'running') return '运行中'
  return '等待'
}

onMounted(() => {
  initPage()
})
</script>

<style scoped lang="scss">
.dossier-generation-page {
  background: #f5f7fb;
  min-height: calc(100vh - 84px);
}

.page-head,
.overview,
.workspace {
  max-width: 1480px;
  margin-left: auto;
  margin-right: auto;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    color: #1f2937;
    font-size: 22px;
    font-weight: 650;
  }

  .subline {
    margin-top: 4px;
    color: #6b7280;
    font-size: 13px;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.overview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) repeat(3, 120px);
  gap: 10px;
  margin-bottom: 12px;
}

.overview-main,
.metric,
.console-panel {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
}

.overview-main {
  padding: 14px;
}

.overview-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #111827;
  font-size: 16px;
  font-weight: 650;
}

.selector-row {
  display: grid;
  grid-template-columns: 180px 220px minmax(260px, 1fr);
  gap: 10px;
}

.selection-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: #4b5563;
  font-size: 13px;
}

.metric {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 78px;
  padding: 12px;
  text-align: center;

  strong {
    color: #111827;
    font-size: 24px;
    line-height: 1;
  }

  span {
    margin-top: 8px;
    color: #6b7280;
    font-size: 13px;
  }
}

.workspace {
  min-width: 0;
}

.console-panel {
  min-width: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 46px;
  padding: 0 14px;
  border-bottom: 1px solid #edf0f5;
  color: #1f2937;
  font-weight: 650;

  &.sub {
    margin-top: 12px;
    border-top: 1px solid #edf0f5;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 14px;

  &.single {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    padding: 0;
    margin-bottom: 12px;
  }
}

.info-item {
  min-height: 64px;
  padding: 10px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #fafbfc;

  label {
    display: block;
    margin-bottom: 8px;
    color: #6b7280;
    font-size: 12px;
  }

  strong {
    color: #111827;
    font-size: 15px;
  }
}

.console-panel {
  padding-bottom: 14px;
}

.generation-steps {
  margin: 12px 14px;
  padding: 10px 12px;
  border: 1px solid #edf0f5;
  border-radius: 4px;
  background: #fafbfc;
}

.console-panel :deep(.el-steps--simple) {
  padding: 10px 12px;
}

.console-panel :deep(.el-step__title) {
  font-size: 13px;
  font-weight: 500;
}

.console-tabs {
  padding: 0 14px;
}

.check-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.summary-item {
  min-height: 64px;
  padding: 12px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #fafbfc;

  strong {
    display: block;
    color: #111827;
    font-size: 24px;
    line-height: 1;
  }

  span {
    display: block;
    margin-top: 8px;
    color: #6b7280;
    font-size: 13px;
  }

  &.success strong {
    color: #16a34a;
  }

  &.warning strong {
    color: #d97706;
  }

  &.danger strong {
    color: #dc2626;
  }
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;

  strong {
    color: #111827;
  }

  span:last-child {
    color: #6b7280;
  }
}

.detail-sections {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.detail-section {
  padding: 10px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #fafbfc;

  label {
    display: block;
    margin-bottom: 8px;
    color: #1f2937;
    font-weight: 650;
  }

  ul {
    margin: 0;
    padding-left: 18px;
    color: #4b5563;
    line-height: 1.75;
  }
}

.job-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 12px;
}

.job-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  strong,
  span {
    display: block;
  }

  strong {
    color: #111827;
    font-size: 16px;
  }

  span {
    margin-top: 4px;
    color: #6b7280;
  }
}

.job-log-table {
  margin-top: 12px;
}

.output-panel {
  min-width: 0;
  padding: 0 0 12px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
}

.output-panel .panel-head {
  margin-bottom: 12px;
}

.output-panel .info-grid,
.output-panel .output-path,
.output-panel :deep(.el-table) {
  margin-left: 12px;
  margin-right: 12px;
}

.output-path {
  margin-bottom: 12px;
  padding: 10px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  color: #4b5563;
  background: #fafbfc;
  word-break: break-all;
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 12px 12px;
}

.result-actions :deep(.el-alert) {
  flex: 1;
  min-width: 0;
}

@media (max-width: 1180px) {
  .overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .overview-main {
    grid-column: 1 / -1;
  }

  .job-layout {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 720px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .selector-row,
  .check-summary,
  .detail-sections,
  .info-grid,
  .result-actions {
    grid-template-columns: 1fr;
  }

  .result-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>

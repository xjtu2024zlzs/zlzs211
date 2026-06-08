<template>
  <div class="app-container dossier-instance-page">
    <div class="page-head">
      <div>
        <h2>卷宗实例管理</h2>
        <div class="subline">单台份飞机综合卷宗</div>
      </div>
      <div class="actions">
        <el-button type="primary" icon="Plus" @click="goGeneration()">新建生成</el-button>
        <el-button icon="Files" @click="goTemplate">模板管理</el-button>
        <el-button icon="Refresh" :loading="loading" @click="refreshPage">刷新</el-button>
      </div>
    </div>

    <el-form :model="queryParams" :inline="true" class="query-panel" @submit.prevent>
      <el-form-item label="机型">
        <el-select v-model="queryParams.modelId" placeholder="全部" clearable filterable style="width: 150px">
          <el-option v-for="item in modelOptions" :key="item.modelId" :label="item.modelCode" :value="item.modelId" />
        </el-select>
      </el-form-item>
      <el-form-item label="机号">
        <el-input v-model="queryParams.tailNumber" placeholder="B-1234" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="模板">
        <el-select v-model="queryParams.templateId" placeholder="全部模板" clearable filterable style="width: 220px">
          <el-option v-for="item in templateOptions" :key="item.id" :label="templateLabel(item)" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 128px">
          <el-option label="已生成" value="ready" />
          <el-option label="已发布" value="published" />
          <el-option label="生成中" value="building" />
          <el-option label="生成失败" value="failed" />
          <el-option label="已归档" value="archived" />
        </el-select>
      </el-form-item>
      <el-form-item label="生成时间">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 250px"
        />
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="queryParams.keyword" placeholder="编号 / 名称 / 任务 / 文件" clearable style="width: 260px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="summary-strip">
      <div v-for="item in summaryCards" :key="item.label" class="metric-cell">
        <strong :class="item.type">{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <div class="workspace">
      <section class="list-panel">
        <div class="panel-head">
          <span>卷宗列表</span>
          <div class="actions">
            <el-tag size="small" type="primary">已选 {{ selectedRows.length }}</el-tag>
            <el-button size="small" icon="Sort" @click="toggleTimeSort">{{ timeSortLabel }}</el-button>
          </div>
        </div>
        <div class="panel-body">
          <el-table
            v-loading="loading"
            :data="instanceList"
            height="520"
            border
            row-key="instanceId"
            highlight-current-row
            :default-sort="{ prop: 'generateTime', order: 'descending' }"
            @row-click="selectInstance"
            @expand-change="handleExpandChange"
            @selection-change="handleSelectionChange"
            @sort-change="handleSortChange"
          >
            <el-table-column type="expand" width="44">
              <template #default="{ row }">
                <div class="version-history">
                  <div class="version-head">
                    <div>
                      <strong>版本历史</strong>
                      <span>{{ row.instanceCode }} / {{ row.tailNumber }}</span>
                    </div>
                    <el-button link type="primary" icon="Refresh" :loading="isVersionLoading(row)" @click.stop="loadVersions(row, true)">刷新版本</el-button>
                  </div>
                  <el-table
                    v-loading="isVersionLoading(row)"
                    :data="versionRows(row)"
                    border
                    size="small"
                    empty-text="暂无版本记录"
                    class="version-table"
                  >
                    <el-table-column label="卷宗版本" prop="versionLabel" width="96" align="center">
                      <template #default="{ row: version }">
                        <el-tag :type="Number(version.isCurrent) === 1 ? 'success' : 'info'" size="small">
                          {{ version.versionLabel || '-' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="模板版本" prop="templateVersion" width="96" align="center" />
                    <el-table-column label="版本类型" prop="versionReason" width="112" align="center">
                      <template #default="{ row: version }">
                        {{ versionReasonLabel(version.versionReason) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="数据记录" width="96" align="right">
                      <template #default="{ row: version }">
                        {{ versionSourceCount(version) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="生成任务" prop="generationJobCode" min-width="180" show-overflow-tooltip />
                    <el-table-column label="生成状态" prop="generationJobStatus" width="96" align="center">
                      <template #default="{ row: version }">
                        <el-tag :type="jobStatusType(version.generationJobStatus)" size="small">
                          {{ jobStatusLabel(version.generationJobStatus) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="生成时间" prop="createdAt" min-width="160" />
                    <el-table-column label="说明" prop="changeSummary" min-width="260" show-overflow-tooltip />
                    <el-table-column label="操作" width="120" fixed="right">
                      <template #default="{ row: version }">
                        <el-button link type="primary" @click.stop="goVersionDetail(row, version)">查看详情</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </template>
            </el-table-column>
            <el-table-column type="selection" width="44" align="center" />
            <el-table-column label="卷宗编号" prop="instanceCode" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="goDetail(row)">{{ row.instanceCode || '-' }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="卷宗名称" prop="instanceName" min-width="220" show-overflow-tooltip />
            <el-table-column label="机型" prop="modelCode" width="86" />
            <el-table-column label="机号" prop="tailNumber" width="96" />
            <el-table-column label="模板" prop="templateName" min-width="180" show-overflow-tooltip />
            <el-table-column label="模板版本" prop="templateVersion" width="94" align="center" />
            <el-table-column label="卷宗版本" prop="currentVersionLabel" width="94" align="center" />
            <el-table-column label="状态" prop="statusName" width="96" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row)" size="small">{{ row.statusName || statusLabel(row.instanceStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="页数" prop="pageCount" width="76" align="right" />
            <el-table-column label="文件" prop="fileCount" width="76" align="right" />
            <el-table-column label="数据记录" prop="dataRecordCount" width="92" align="right" />
            <el-table-column label="生成时间" prop="generateTime" min-width="160" sortable="custom" />
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="goDetail(row)">详情</el-button>
                <el-button link type="primary" @click.stop="goGeneration(row)">新版本</el-button>
                <el-button v-if="canPublish(row)" link type="primary" @click.stop="handlePublish(row)">发布</el-button>
                <el-button v-if="canArchive(row)" link type="primary" @click.stop="handleArchive(row)">归档</el-button>
                <el-button v-if="row.pdfFileName" link type="primary" @click.stop="showFile(row.pdfFileName)">PDF</el-button>
                <el-button v-if="row.zipFileName" link type="primary" @click.stop="showFile(row.zipFileName)">ZIP</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
          />
        </div>
      </section>

    </div>
  </div>
</template>

<script setup name="DossierInstance">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  archiveDossierInstance,
  getDossierInstanceSummary,
  listDossierInstanceVersions,
  listDossierInstances,
  publishDossierInstance
} from '@/api/project1/dossier/instance'
import { listGenerationModels } from '@/api/project1/dossier/generation'
import { listTemplate } from '@/api/project1/dossier/template'

const router = useRouter()
const loading = ref(false)
const instanceList = ref([])
const selectedRows = ref([])
const selectedInstance = ref({})
const modelOptions = ref([])
const templateOptions = ref([])
const dateRange = ref([])
const total = ref(0)
const summary = ref({})
const versionMap = reactive({})
const versionLoadingMap = reactive({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  modelId: '',
  tailNumber: '',
  templateId: '',
  status: '',
  keyword: '',
  sortOrder: 'desc'
})

const timeSortLabel = computed(() => queryParams.sortOrder === 'desc' ? '生成时间倒序' : '生成时间正序')

const summaryCards = computed(() => {
  return [
    { label: '全部卷宗', value: numberText(summary.value.totalCount), type: 'primary' },
    { label: '已发布', value: numberText(summary.value.publishedCount), type: 'success' },
    { label: '已生成', value: numberText(summary.value.readyCount), type: '' },
    { label: '生成中', value: numberText(summary.value.buildingCount), type: 'warning' },
    { label: '生成失败', value: numberText(summary.value.failedCount), type: 'danger' },
    { label: '本月新版本', value: numberText(summary.value.monthNewCount), type: '' }
  ]
})

async function initPage() {
  loading.value = true
  try {
    await Promise.all([loadModels(), loadTemplates()])
    await getList()
  } finally {
    loading.value = false
  }
}

async function loadModels() {
  const res = await listGenerationModels()
  modelOptions.value = res.data || []
}

async function loadTemplates() {
  const res = await listTemplate({ pageNum: 1, pageSize: 100 })
  templateOptions.value = res.rows || []
}

async function getList() {
  loading.value = true
  try {
    const query = buildQuery()
    const [listRes, summaryRes] = await Promise.all([
      listDossierInstances(query),
      getDossierInstanceSummary(query)
    ])
    const data = listRes.data || {}
    instanceList.value = data.rows || []
    total.value = data.total || 0
    summary.value = summaryRes.data || {}
    pruneVersionCache()
    keepSelected()
  } finally {
    loading.value = false
  }
}

function buildQuery() {
  const query = { ...queryParams }
  if (dateRange.value && dateRange.value.length === 2) {
    query.beginTime = dateRange.value[0]
    query.endTime = dateRange.value[1]
  }
  return query
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.pageNum = 1
  queryParams.modelId = ''
  queryParams.tailNumber = ''
  queryParams.templateId = ''
  queryParams.status = ''
  queryParams.keyword = ''
  queryParams.sortOrder = 'desc'
  dateRange.value = []
  getList()
}

function refreshPage() {
  getList()
}

function keepSelected() {
  if (!instanceList.value.length) {
    selectedInstance.value = {}
    return
  }
  const currentId = selectedInstance.value.instanceId
  const matched = instanceList.value.find(item => item.instanceId === currentId)
  selectInstance(matched || instanceList.value[0])
}

function selectInstance(row) {
  selectedInstance.value = row || {}
}

function handleExpandChange(row, expandedRows) {
  if (!row || !row.instanceId) {
    return
  }
  const expanded = Array.isArray(expandedRows) && expandedRows.some(item => item.instanceId === row.instanceId)
  if (expanded) {
    loadVersions(row)
  }
}

function handleSelectionChange(selection) {
  selectedRows.value = selection
}

function handleSortChange({ prop, order }) {
  if (prop !== 'generateTime') {
    return
  }
  queryParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  getList()
}

function toggleTimeSort() {
  queryParams.sortOrder = queryParams.sortOrder === 'desc' ? 'asc' : 'desc'
  getList()
}

function goTemplate() {
  router.push('/dossier/manage/template')
}

function goGeneration(row) {
  const query = row && row.aircraftId ? { aircraftId: row.aircraftId, templateId: row.templateId, instanceId: row.instanceId } : {}
  router.push({ path: '/dossier/manage/generation', query })
}

function goDetail(row) {
  if (!row || !row.instanceId) {
    return
  }
  router.push({
    path: '/dossier/manage/detail',
    query: {
      instanceId: row.instanceId,
      versionId: row.currentVersionId
    }
  })
}

function goVersionDetail(instanceRow, versionRow) {
  if (!instanceRow || !instanceRow.instanceId || !versionRow || !versionRow.versionId) {
    return
  }
  router.push({
    path: '/dossier/manage/detail',
    query: {
      instanceId: instanceRow.instanceId,
      versionId: versionRow.versionId
    }
  })
}

async function loadVersions(row, force = false) {
  if (!row || !row.instanceId) {
    return
  }
  if (!force && versionMap[row.instanceId]) {
    return
  }
  versionLoadingMap[row.instanceId] = true
  try {
    const res = await listDossierInstanceVersions(row.instanceId)
    versionMap[row.instanceId] = res.data || []
  } finally {
    versionLoadingMap[row.instanceId] = false
  }
}

async function handlePublish(row) {
  await ElMessageBox.confirm(`确认发布 ${row.instanceCode}？`, '发布卷宗', { type: 'warning' })
  await publishDossierInstance(row.instanceId)
  ElMessage.success('发布成功')
  getList()
}

async function handleArchive(row) {
  await ElMessageBox.confirm(`确认归档 ${row.instanceCode}？`, '归档卷宗', { type: 'warning' })
  await archiveDossierInstance(row.instanceId)
  ElMessage.success('归档成功')
  getList()
}

function canPublish(row) {
  return row.instanceStatus === 'ready' && row.generationJobStatus !== 'failed'
}

function canArchive(row) {
  return ['ready', 'published'].includes(row.instanceStatus) && !['running', 'failed'].includes(row.generationJobStatus)
}

function showFile(fileName) {
  ElMessage.info(fileName)
}

function versionRows(row) {
  if (!row || !row.instanceId) {
    return []
  }
  return versionMap[row.instanceId] || []
}

function isVersionLoading(row) {
  return !!(row && row.instanceId && versionLoadingMap[row.instanceId])
}

function pruneVersionCache() {
  const activeIds = new Set(instanceList.value.map(item => item.instanceId))
  Object.keys(versionMap).forEach(instanceId => {
    if (!activeIds.has(instanceId)) {
      delete versionMap[instanceId]
    }
  })
  Object.keys(versionLoadingMap).forEach(instanceId => {
    if (!activeIds.has(instanceId)) {
      delete versionLoadingMap[instanceId]
    }
  })
}

function versionReasonLabel(value) {
  if (value === 'initial') return '首次生成'
  if (value === 'data_update') return '数据更新'
  if (value === 'template_change') return '模板调整'
  return value || '-'
}

function versionSourceCount(version) {
  const output = version.output || {}
  const contentSummary = version.contentSummary || {}
  const resultSummary = version.resultSummary || {}
  return output.sourceRecordCount || contentSummary.sourceRecordCount || resultSummary.sourceRecordCount || '-'
}

function jobStatusType(status) {
  if (status === 'succeeded') return 'success'
  if (status === 'failed') return 'danger'
  if (['queued', 'running'].includes(status)) return 'warning'
  return 'info'
}

function jobStatusLabel(status) {
  if (status === 'succeeded') return '成功'
  if (status === 'failed') return '失败'
  if (status === 'queued') return '排队'
  if (status === 'running') return '生成中'
  return status || '-'
}

function templateLabel(item) {
  return `${item.name} / ${item.templateVersion || 'V1.0'}`
}

function statusType(row) {
  if (row.generationJobStatus === 'failed') return 'danger'
  if (row.instanceStatus === 'published') return 'success'
  if (row.instanceStatus === 'ready') return 'primary'
  if (row.instanceStatus === 'building') return 'warning'
  if (row.instanceStatus === 'archived') return 'info'
  return 'info'
}

function statusLabel(status) {
  if (status === 'published') return '已发布'
  if (status === 'ready') return '已生成'
  if (status === 'building') return '生成中'
  if (status === 'archived') return '已归档'
  return '草稿'
}

function numberText(value) {
  return value || 0
}

onMounted(() => {
  initPage()
})
</script>

<style scoped lang="scss">
.dossier-instance-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fb;
}

.page-head,
.query-panel,
.summary-strip,
.workspace {
  max-width: 1500px;
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
    color: #111827;
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

.query-panel,
.list-panel,
.metric-cell {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
}

.query-panel {
  margin-bottom: 12px;
  padding: 14px 14px 0;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.metric-cell {
  min-height: 64px;
  padding: 12px;

  strong {
    display: block;
    color: #111827;
    font-size: 24px;
    line-height: 1;

    &.primary {
      color: #409eff;
    }

    &.success {
      color: #16a34a;
    }

    &.warning {
      color: #d97706;
    }

    &.danger {
      color: #dc2626;
    }
  }

  span {
    display: block;
    margin-top: 8px;
    color: #6b7280;
    font-size: 13px;
  }
}

.workspace {
  display: block;
}

.list-panel {
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
}

.panel-body {
  padding: 12px;
}

.dossier-instance-page :deep(.el-table) {
  font-size: 13px;
}

.version-history {
  padding: 4px 10px 12px 52px;
  background: #f8fafc;
}

.version-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 34px;
  margin-bottom: 8px;
  color: #1f2937;

  strong {
    margin-right: 10px;
    font-weight: 650;
  }

  span {
    color: #6b7280;
    font-size: 12px;
  }
}

.version-table {
  background: #ffffff;
}

.dossier-instance-page :deep(.pagination-container) {
  margin-bottom: 0;
  padding-bottom: 0;
}

@media (max-width: 1280px) {
  .summary-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

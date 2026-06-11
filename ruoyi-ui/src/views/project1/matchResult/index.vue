<template>
  <div class="app-container match-result-page">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
      label-width="72px"
      class="compact-query"
    >
      <el-form-item label="任务" prop="taskId">
        <el-select
          v-model="queryParams.taskId"
          placeholder="请选择任务"
          clearable
          filterable
          style="width: 220px"
          @change="handleTaskChange"
        >
          <el-option v-for="item in taskOptions" :key="item.taskId" :label="item.taskName" :value="item.taskId" />
        </el-select>
      </el-form-item>
      <el-form-item label="运行记录" prop="recordId">
        <el-select
          v-model="queryParams.recordId"
          placeholder="请选择运行记录"
          clearable
          style="width: 180px"
          @change="handleRecordChange"
        >
          <el-option v-for="item in recordOptions" :key="item.recordId" :label="recordLabel(item)" :value="item.recordId" />
        </el-select>
      </el-form-item>
      <el-form-item label="结果集" prop="resultSetId">
        <el-select
          v-model="queryParams.resultSetId"
          placeholder="请选择结果集"
          clearable
          filterable
          style="width: 260px"
          @change="handleResultSetChange"
        >
          <el-option v-for="item in resultSetOptions" :key="item.resultSetId" :label="resultSetLabel(item)" :value="item.resultSetId" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" placeholder="全部" style="width: 140px" @change="handleReviewFilterChange">
          <el-option v-for="item in reviewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核人" prop="reviewedBy">
        <el-select v-model="queryParams.reviewedBy" placeholder="全部" clearable style="width: 140px" @change="handleReviewFilterChange">
          <el-option label="全部" value="all" />
          <el-option label="系统自动" value="system" />
          <el-option label="当前用户" :value="currentReviewer" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Check" :disabled="!queryParams.resultSetId" @click="handleSetDefault" v-hasPermi="['project1:matchResult:edit']">
          设为默认结果集
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Select" :disabled="selectedRows.length === 0" @click="handleBatchApprove" v-hasPermi="['project1:matchResult:edit']">
          批量通过
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Close" :disabled="selectedRows.length === 0" @click="handleBatchReject" v-hasPermi="['project1:matchResult:edit']">
          批量驳回
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="MagicStick" :disabled="!queryParams.resultSetId" @click="handleAutoApprove" v-hasPermi="['project1:matchResult:edit']">
          自动通过
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadResultVisual" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="pagedReviewRows"
      @selection-change="handleSelectionChange"
      border
      class="compact-table"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="源表" prop="sourceTable" min-width="170" :show-overflow-tooltip="true" />
      <el-table-column label="源列" prop="sourceColumn" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="目标表" prop="targetTable" min-width="170" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-input
            v-if="scope.row.reviewStatus === 'pending'"
            v-model="scope.row.targetTable"
            size="small"
            placeholder="请输入目标表"
          />
          <span v-else>{{ scope.row.targetTable || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="目标列" prop="targetColumn" min-width="150" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-input
            v-if="scope.row.reviewStatus === 'pending'"
            v-model="scope.row.targetColumn"
            size="small"
            placeholder="请输入目标列"
          />
          <span v-else>{{ scope.row.targetColumn || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分数" prop="score" width="90" align="center">
        <template #default="scope">{{ formatScore(scope.row.score) }}</template>
      </el-table-column>
      <el-table-column label="审核状态" prop="reviewStatus" width="120" align="center">
        <template #default="scope">
          <el-tag :type="reviewStatusTagType(scope.row.reviewStatus)">{{ reviewStatusLabel(scope.row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" prop="reviewer" width="110" align="center" />
      <el-table-column label="审核时间" prop="reviewedAt" width="160" align="center">
        <template #default="scope">{{ parseTime(scope.row.reviewedAt, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button
            link
            type="primary"
            icon="Select"
            :disabled="!scope.row.reviewId || scope.row.reviewStatus === 'approved' || scope.row.reviewStatus === 'auto_approved'"
            @click="handleApprove(scope.row)"
            v-hasPermi="['project1:matchResult:edit']"
          >
            通过
          </el-button>
          <el-button
            link
            type="danger"
            icon="Close"
            :disabled="!scope.row.reviewId || scope.row.reviewStatus === 'rejected'"
            @click="handleReject(scope.row)"
            v-hasPermi="['project1:matchResult:edit']"
          >
            驳回
          </el-button>
          <el-button link type="primary" icon="Clock" :disabled="!scope.row.reviewId" @click="handleHistory(scope.row)">历史</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="handlePagination"
    />

    <el-card class="mt16 metric-visual-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>评估指标可视化</span>
          <span class="muted" v-if="metricPayload.resultDir">运行目录：{{ metricPayload.resultDir }}</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :lg="13">
          <div class="metric-panel">
            <div class="metric-panel-title">候选结果集 F1 对比</div>
            <div v-if="candidateRows.length" class="candidate-list">
              <div v-for="item in candidateRows" :key="item.resultSetId" class="candidate-row">
                <div class="candidate-name">{{ item.label }}</div>
                <div class="candidate-score">{{ formatMetricValue(item.f1Score) }}</div>
                <div class="candidate-track">
                  <div
                    class="candidate-bar"
                    :class="{ current: item.isCurrent, all: item.variant === 'all' }"
                    :style="{ width: item.barPercent + '%' }"
                  />
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无候选结果集指标" :image-size="80" />
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :lg="11">
          <div class="metric-panel">
            <div class="metric-panel-title">当前结果集指标</div>
            <div class="metric-card-grid">
              <div class="metric-card" v-for="metric in metricCards" :key="metric.key || metric.label">
                <div class="metric-value">{{ formatMetricValue(metric.value) }}</div>
                <div class="metric-label">{{ metric.label }}</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card v-if="false" class="mt16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>评估指标可视化</span>
          <span class="muted" v-if="metricPayload.resultDir">运行目录：{{ metricPayload.resultDir }}</span>
        </div>
      </template>
      <el-row :gutter="12">
        <el-col :span="6" v-for="metric in metricCards" :key="metric.label">
          <div class="metric-card">
            <div class="metric-value">{{ formatMetricValue(metric.value) }}</div>
            <div class="metric-label">{{ metric.label }}</div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="12" class="mt12">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>匹配分数分布</template>
            <EChartPanel :option="scoreChartOption" height="220px" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>目标表 Top 分布</template>
            <EChartPanel :option="targetChartOption" height="220px" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog title="字段匹配结果详情" v-model="detailOpen" width="680px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="源表">{{ detailRow.sourceTable }}</el-descriptions-item>
        <el-descriptions-item label="源列">{{ detailRow.sourceColumn }}</el-descriptions-item>
        <el-descriptions-item label="推荐目标表">{{ detailRow.proposedTargetTable }}</el-descriptions-item>
        <el-descriptions-item label="推荐目标列">{{ detailRow.proposedTargetColumn }}</el-descriptions-item>
        <el-descriptions-item label="确认目标表">{{ detailRow.targetTable }}</el-descriptions-item>
        <el-descriptions-item label="确认目标列">{{ detailRow.targetColumn }}</el-descriptions-item>
        <el-descriptions-item label="匹配分数">{{ formatScore(detailRow.score) }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ reviewStatusLabel(detailRow.reviewStatus) }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailRow.reviewer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ parseTime(detailRow.reviewedAt, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-drawer title="审核历史" v-model="historyOpen" size="560px" append-to-body>
      <el-table :data="historyRows" border>
        <el-table-column label="操作时间" prop="createTime" width="160" align="center">
          <template #default="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}</template>
        </el-table-column>
        <el-table-column label="动作" prop="actionType" width="110" align="center">
          <template #default="scope">{{ actionTypeLabel(scope.row.actionType) }}</template>
        </el-table-column>
        <el-table-column label="原状态" prop="oldStatus" width="110" align="center">
          <template #default="scope">{{ reviewStatusLabel(scope.row.oldStatus) }}</template>
        </el-table-column>
        <el-table-column label="新状态" prop="newStatus" width="110" align="center">
          <template #default="scope">{{ reviewStatusLabel(scope.row.newStatus) }}</template>
        </el-table-column>
        <el-table-column label="操作人" prop="actor" width="120" align="center" />
        <el-table-column label="备注" prop="remark" min-width="160" :show-overflow-tooltip="true" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup name="MatchResult">
import { ElMessageBox } from 'element-plus'
import EChartPanel from '../components/EChartPanel.vue'
import { listMatchTask, listMatchTaskRecord } from '@/api/project1/matchTask'
import {
  listMatchResult,
  setDefaultMatchResult,
  getMatchResultDetail,
  getMatchResultMetrics,
  approveMatchResult,
  rejectMatchResult,
  autoApproveMatchResult,
  getReviewHistory
} from '@/api/project1/matchResult'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const taskOptions = ref([])
const recordOptions = ref([])
const resultSetOptions = ref([])
const reviewRows = ref([])
const selectedRows = ref([])
const total = ref(0)
const emptyMetricPayload = () => ({
  currentCards: [],
  cards: [],
  candidateF1Comparison: [],
  scoreDistribution: [],
  targetDistribution: []
})
const metricPayload = ref(emptyMetricPayload())
const detailOpen = ref(false)
const detailRow = ref({})
const historyOpen = ref(false)
const historyRows = ref([])
const currentReviewer = ref('system')

const queryParams = reactive({
  taskId: undefined,
  recordId: undefined,
  resultSetId: undefined,
  reviewStatus: 'all',
  reviewedBy: 'all',
  pageNum: 1,
  pageSize: 10
})

const reviewStatusOptions = [
  { label: '全部', value: 'all' },
  { label: '待审核', value: 'pending' },
  { label: '自动通过', value: 'auto_approved' },
  { label: '审核通过', value: 'approved' },
  { label: '审核未通过', value: 'rejected' }
]

const metricCards = computed(() => metricPayload.value.currentCards || metricPayload.value.cards || [])

const candidateRows = computed(() => {
  const rows = (metricPayload.value.candidateF1Comparison || []).filter(item => !isPresetResultSet(item))
  const maxScore = rows.reduce((max, row) => {
    const value = Number(row.f1Score ?? row.value ?? 0)
    return Number.isNaN(value) ? max : Math.max(max, value)
  }, 0)
  return rows.map(row => {
    const score = Number(row.f1Score ?? row.value ?? 0)
    return {
      ...row,
      f1Score: row.f1Score ?? row.value,
      barPercent: maxScore > 0 && !Number.isNaN(score) ? Math.round((score / maxScore) * 100) : 0
    }
  })
})

const pagedReviewRows = computed(() => {
  const pageNum = Number(queryParams.pageNum || 1)
  const pageSize = Number(queryParams.pageSize || 10)
  const start = (pageNum - 1) * pageSize
  return reviewRows.value.slice(start, start + pageSize)
})

const scoreChartOption = computed(() => {
  const rows = metricPayload.value.scoreDistribution || []
  return buildBarOption(rows.map(chartLabel), rows.map(chartValue), false)
})

const targetChartOption = computed(() => {
  const rows = metricPayload.value.targetDistribution || []
  return buildBarOption(rows.map(chartLabel), rows.map(chartValue), true)
})

function chartLabel(item) {
  if (!item) {
    return '-'
  }
  return item.label || item.targetTable || item.target_table || item.name || item.key || '-'
}

function chartValue(item) {
  if (!item) {
    return 0
  }
  const value = item.value ?? item.count ?? item.total ?? 0
  return Number(value || 0)
}

function resetReviewPage() {
  queryParams.pageNum = 1
  selectedRows.value = []
}

function isPresetResultSet(item) {
  if (!item) {
    return false
  }
  const method = String(item.method || '').toLowerCase()
  const variant = String(item.variant || '').toLowerCase()
  const remark = String(item.remark || '')
  return method === 'groundtruth' || variant === 'preset' || remark.includes('[PRESET_EXECUTABLE]')
}

onMounted(() => {
  loadTasks()
  loadResultSetOptions()
})

function loadTasks() {
  listMatchTask({ pageNum: 1, pageSize: 100 }).then(response => {
    taskOptions.value = response.rows || []
  })
}

function loadResultSetOptions() {
  const params = { pageNum: 1, pageSize: 100 }
  if (queryParams.taskId) {
    params.taskId = queryParams.taskId
  }
  if (queryParams.recordId) {
    params.recordId = queryParams.recordId
  }
  return listMatchResult(params).then(response => {
    resultSetOptions.value = (response.rows || []).filter(item => !isPresetResultSet(item))
    if (!resultSetOptions.value.length) {
      queryParams.resultSetId = undefined
      reviewRows.value = []
      total.value = 0
      selectedRows.value = []
      metricPayload.value = emptyMetricPayload()
      return
    }
    const current = resultSetOptions.value.find(item => item.resultSetId === queryParams.resultSetId)
    if (!current) {
      const defaultItem = resultSetOptions.value.find(item => item.isDefault === 1) || resultSetOptions.value[0]
      queryParams.resultSetId = defaultItem.resultSetId
    }
    loadResultVisual()
  })
}

function handleTaskChange(taskId) {
  resetReviewPage()
  queryParams.recordId = undefined
  queryParams.resultSetId = undefined
  recordOptions.value = []
  if (!taskId) {
    loadResultSetOptions()
    return
  }
  listMatchTaskRecord(taskId).then(response => {
    recordOptions.value = response.data?.rows || []
  }).finally(() => loadResultSetOptions())
}

function handleRecordChange() {
  resetReviewPage()
  queryParams.resultSetId = undefined
  loadResultSetOptions()
}

function handleResultSetChange() {
  resetReviewPage()
  loadResultVisual()
}

function handleReviewFilterChange() {
  resetReviewPage()
  loadReviewRows()
}

function handleQuery() {
  resetReviewPage()
  if (queryParams.resultSetId) {
    loadResultVisual()
  } else {
    loadResultSetOptions()
  }
}

function resetQuery() {
  resetReviewPage()
  queryParams.taskId = undefined
  queryParams.recordId = undefined
  queryParams.resultSetId = undefined
  queryParams.reviewStatus = 'all'
  queryParams.reviewedBy = 'all'
  recordOptions.value = []
  loadResultSetOptions()
}

function loadResultVisual() {
  if (!queryParams.resultSetId) {
    reviewRows.value = []
    total.value = 0
    selectedRows.value = []
    metricPayload.value = emptyMetricPayload()
    return
  }
  loading.value = true
  Promise.all([loadReviewRows(), loadMetrics()]).finally(() => {
    loading.value = false
  })
}

function loadReviewRows() {
  if (!queryParams.resultSetId) {
    reviewRows.value = []
    total.value = 0
    selectedRows.value = []
    return Promise.resolve()
  }
  return getMatchResultDetail(queryParams.resultSetId, {
    reviewStatus: queryParams.reviewStatus,
    reviewedBy: queryParams.reviewedBy
  }).then(response => {
    reviewRows.value = response.data?.rows || []
    total.value = reviewRows.value.length
    selectedRows.value = []
  })
}

function loadMetrics() {
  if (!queryParams.resultSetId) {
    return Promise.resolve()
  }
  return getMatchResultMetrics(queryParams.resultSetId).then(response => {
    metricPayload.value = response.data || emptyMetricPayload()
  })
}

function handleSelectionChange(selection) {
  selectedRows.value = selection || []
}

function handlePagination() {
  selectedRows.value = []
}

function handleSetDefault() {
  if (!queryParams.resultSetId) {
    proxy.$modal.msgWarning('请先选择结果集')
    return
  }
  setDefaultMatchResult(queryParams.resultSetId).then(response => {
    proxy.$modal.msgSuccess(response.data?.message || '已设为默认结果集')
    loadResultSetOptions()
  })
}

function handleBatchApprove() {
  const reviewIds = selectedRows.value.map(row => row.reviewId).filter(Boolean)
  if (!reviewIds.length) {
    proxy.$modal.msgWarning('请选择可审核的字段匹配结果')
    return
  }
  approveMatchResult({ reviewIds }).then(() => {
    proxy.$modal.msgSuccess('批量通过完成')
    loadResultVisual()
  })
}

function handleBatchReject() {
  const reviewIds = selectedRows.value.map(row => row.reviewId).filter(Boolean)
  if (!reviewIds.length) {
    proxy.$modal.msgWarning('请选择可驳回的字段匹配结果')
    return
  }
  ElMessageBox.prompt('请输入驳回原因', '批量驳回', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return rejectMatchResult({ reviewIds, remark: value })
  }).then(() => {
    proxy.$modal.msgSuccess('批量驳回完成')
    loadResultVisual()
  })
}

function handleApprove(row) {
  approveMatchResult({
    reviewIds: [row.reviewId],
    targetTable: row.targetTable,
    targetColumn: row.targetColumn,
    remark: '人工审核通过'
  }).then(() => {
    proxy.$modal.msgSuccess('审核通过完成')
    loadResultVisual()
  })
}

function handleReject(row) {
  ElMessageBox.prompt('请输入驳回原因', '驳回字段匹配', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return rejectMatchResult({ reviewIds: [row.reviewId], remark: value })
  }).then(() => {
    proxy.$modal.msgSuccess('驳回完成')
    loadResultVisual()
  })
}

function handleAutoApprove() {
  autoApproveMatchResult(queryParams.resultSetId).then(response => {
    proxy.$modal.msgSuccess(`自动通过完成，更新 ${response.data?.changed || 0} 条`)
    loadResultVisual()
  })
}

function handleView(row) {
  detailRow.value = { ...row }
  detailOpen.value = true
}

function handleHistory(row) {
  getReviewHistory(row.reviewId).then(response => {
    historyRows.value = response.data || []
    historyOpen.value = true
  })
}

function resultSetLabel(item) {
  const displaySuffix = item.isDefault === 1 ? ' / 默认' : ''
  const displayF1 = item.f1Score !== null && item.f1Score !== undefined ? ` / F1 ${formatMetricValue(item.f1Score)}` : ''
  return `${item.resultSetName || item.resultSetId}${displayF1} / ${item.totalRows || 0}条${displaySuffix}`
}

function recordLabel(item) {
  return `#${item.recordId} / ${executionStatusLabel(item.executionStatus)}`
}

function reviewStatusLabel(status) {
  const map = {
    pending: '待审核',
    auto_approved: '自动通过',
    approved: '审核通过',
    rejected: '审核未通过'
  }
  return map[status] || status || '-'
}

function reviewStatusTagType(status) {
  const map = {
    pending: 'warning',
    auto_approved: 'success',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

function actionTypeLabel(actionType) {
  const map = {
    approve: '通过',
    reject: '驳回',
    auto_approve: '自动通过',
    modify: '修改'
  }
  return map[actionType] || actionType || '-'
}

function executionStatusLabel(status) {
  const map = {
    pending: '待运行',
    running: '运行中',
    success: '成功',
    failed: '失败',
    partial: '部分成功'
  }
  return map[status] || status || '-'
}

function formatScore(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return Number(value).toFixed(3)
}

function formatMetricValue(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  const number = Number(value)
  if (Number.isNaN(number)) {
    return value
  }
  return number >= 10 ? number.toFixed(0) : number.toFixed(3)
}

function buildBarOption(labels, values, horizontal) {
  return {
    grid: { left: horizontal ? 120 : 36, right: 24, top: 28, bottom: horizontal ? 24 : 40 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: horizontal
      ? { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#edf2f7' } } }
      : { type: 'category', data: labels, axisTick: { show: false } },
    yAxis: horizontal
      ? { type: 'category', data: labels, axisTick: { show: false }, axisLine: { show: false } }
      : { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#edf2f7' } } },
    series: [
      {
        type: 'bar',
        data: values,
        barWidth: horizontal ? 12 : 22,
        itemStyle: { color: '#409eff', borderRadius: horizontal ? [0, 8, 8, 0] : [4, 4, 0, 0] },
        label: { show: true, position: horizontal ? 'right' : 'top' }
      }
    ]
  }
}
</script>

<style scoped>
.match-result-page {
  padding-bottom: 24px;
}

.compact-query :deep(.el-form-item) {
  margin-bottom: 12px;
}

.compact-table :deep(.el-table__cell) {
  padding: 7px 0;
}

.mt12 {
  margin-top: 12px;
}

.mt16 {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.muted {
  color: #909399;
  font-size: 12px;
}

.metric-visual-card :deep(.el-card__body) {
  padding: 16px 20px 18px;
}

.metric-panel {
  min-height: 270px;
  padding: 18px 22px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}

.metric-panel-title {
  margin-bottom: 18px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.candidate-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.candidate-row {
  display: grid;
  grid-template-columns: 160px 58px minmax(180px, 1fr);
  align-items: center;
  gap: 12px;
}

.candidate-name {
  color: #606266;
  line-height: 18px;
  word-break: break-word;
}

.candidate-score {
  color: #303133;
  font-weight: 600;
  text-align: right;
}

.candidate-track {
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: #edf2f7;
}

.candidate-bar {
  height: 100%;
  border-radius: 999px;
  background: #409eff;
}

.candidate-bar.all {
  background: #e6a23c;
}

.candidate-bar.current {
  background: #409eff;
}

.metric-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 96px;
  padding: 20px 22px;
  background: #fafafa;
}

.metric-value {
  color: #303133;
  font-size: 28px;
  font-weight: 600;
  line-height: 34px;
}

.metric-label {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.chart-card {
  height: 292px;
}

@media (max-width: 1200px) {
  .candidate-row {
    grid-template-columns: 130px 52px minmax(140px, 1fr);
  }
}

@media (max-width: 768px) {
  .candidate-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .candidate-score {
    text-align: left;
  }

  .metric-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>

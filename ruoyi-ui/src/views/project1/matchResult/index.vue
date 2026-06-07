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
          @change="loadResultVisual"
        >
          <el-option v-for="item in resultSetOptions" :key="item.resultSetId" :label="resultSetLabel(item)" :value="item.resultSetId" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" placeholder="全部" style="width: 140px" @change="loadReviewRows">
          <el-option v-for="item in reviewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核人" prop="reviewedBy">
        <el-select v-model="queryParams.reviewedBy" placeholder="全部" clearable style="width: 140px" @change="loadReviewRows">
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
      :data="reviewRows"
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

    <el-card class="mt16" shadow="never">
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
const metricPayload = ref({ cards: [], scoreDistribution: [], targetDistribution: [] })
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
  reviewedBy: 'all'
})

const reviewStatusOptions = [
  { label: '全部', value: 'all' },
  { label: '待审核', value: 'pending' },
  { label: '自动通过', value: 'auto_approved' },
  { label: '审核通过', value: 'approved' },
  { label: '审核未通过', value: 'rejected' }
]

const metricCards = computed(() => metricPayload.value.cards || [])

const scoreChartOption = computed(() => {
  const rows = metricPayload.value.scoreDistribution || []
  return buildBarOption(rows.map(item => item.label), rows.map(item => Number(item.value || 0)), false)
})

const targetChartOption = computed(() => {
  const rows = metricPayload.value.targetDistribution || []
  return buildBarOption(rows.map(item => item.label), rows.map(item => Number(item.value || 0)), true)
})

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
    resultSetOptions.value = response.rows || []
    if (!resultSetOptions.value.length) {
      queryParams.resultSetId = undefined
      reviewRows.value = []
      metricPayload.value = { cards: [], scoreDistribution: [], targetDistribution: [] }
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
  queryParams.resultSetId = undefined
  loadResultSetOptions()
}

function handleQuery() {
  if (queryParams.resultSetId) {
    loadResultVisual()
  } else {
    loadResultSetOptions()
  }
}

function resetQuery() {
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
    metricPayload.value = { cards: [], scoreDistribution: [], targetDistribution: [] }
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
    return Promise.resolve()
  }
  return getMatchResultDetail(queryParams.resultSetId, {
    reviewStatus: queryParams.reviewStatus,
    reviewedBy: queryParams.reviewedBy
  }).then(response => {
    reviewRows.value = response.data?.rows || []
  })
}

function loadMetrics() {
  if (!queryParams.resultSetId) {
    return Promise.resolve()
  }
  return getMatchResultMetrics(queryParams.resultSetId).then(response => {
    metricPayload.value = response.data || { cards: [], scoreDistribution: [], targetDistribution: [] }
  })
}

function handleSelectionChange(selection) {
  selectedRows.value = selection || []
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
  const suffix = item.isDefault === 1 ? ' / 默认' : ''
  return `${item.resultSetName || item.resultSetId} / ${item.totalRows || 0}条${suffix}`
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
  return Number(value).toFixed(2)
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

.metric-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 14px 16px;
  background: #fff;
}

.metric-value {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  line-height: 30px;
}

.metric-label {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.chart-card {
  height: 292px;
}
</style>

<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item>
        <el-button icon="Back" @click="goBack">返回</el-button>
      </el-form-item>
      <el-form-item label="接入计划" prop="accessPlanId">
        <el-select
          v-model="queryParams.accessPlanId"
          placeholder="请选择接入计划"
          clearable
          filterable
          style="width: 260px"
          @change="handlePlanChange"
        >
          <el-option
            v-for="item in accessPlanOptions"
            :key="item.accessPlanId"
            :label="item.planName"
            :value="item.accessPlanId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="执行状态" prop="resultStatus">
        <el-select v-model="queryParams.resultStatus" placeholder="全部" clearable style="width: 170px" @change="handleQuery">
          <el-option v-for="item in resultStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="执行批次" prop="accessBatchId">
        <el-select v-model="queryParams.accessBatchId" placeholder="选择执行批次" clearable style="width: 220px" @change="handleQuery">
          <el-option label="全部执行批次" :value="undefined" />
          <el-option v-for="item in batchOptions" :key="item.batchId" :label="item.batchName" :value="item.batchId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-descriptions class="mb12" :column="5" border>
      <el-descriptions-item label="接入计划">{{ currentPlan.planName || "-" }}</el-descriptions-item>
      <el-descriptions-item label="来源数据源">{{ datasourceName(currentPlan.sourceDatasourceId) }}</el-descriptions-item>
      <el-descriptions-item label="接入方式">{{ accessModeLabel(currentPlan.accessMode) }}</el-descriptions-item>
      <el-descriptions-item label="接入类型">{{ accessTypeLabel(currentPlan.accessType) }}</el-descriptions-item>
      <el-descriptions-item label="更新周期">{{ currentPlan.accessType === "continuous" ? `每 ${currentPlan.cycleHours || 1} 小时` : "-" }}</el-descriptions-item>
    </el-descriptions>

    <div class="stat-grid mb12">
      <div class="stat-card">
        <div class="stat-value">{{ summary.totalSuccess }}</div>
        <div class="stat-label">当前计划累计成功数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ summary.lastInserted }}</div>
        <div class="stat-label">当前计划最近新增数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ summary.lastUpdated }}</div>
        <div class="stat-label">当前计划最近更新数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ summary.lastFailed }}</div>
        <div class="stat-label">当前计划最近失败数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ summary.totalFailed }}</div>
        <div class="stat-label">当前计划累计失败数</div>
      </div>
    </div>

    <el-card shadow="never" class="mb12">
      <template #header>
        <span>接入统计分析</span>
      </template>
      <el-row :gutter="12">
        <el-col :span="12">
          <div class="viz-panel">
            <div class="viz-title">近五次接入增量堆叠柱状图</div>
            <EChartPanel :option="accessIncrementChartOption" height="270px" />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="viz-panel">
            <div class="viz-title">目标表写入量 Top 10 横向条形图</div>
            <EChartPanel :option="topTableChartOption" height="270px" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-table v-loading="loading" :data="accessResultList">
      <el-table-column label="目标表" align="left" prop="targetTable" min-width="170" :show-overflow-tooltip="true" />
      <el-table-column label="源库/源表" align="left" prop="sourceTable" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="执行批次" align="center" prop="accessBatchId" min-width="150">
        <template #default="scope">{{ batchName(scope.row.accessBatchId) }}</template>
      </el-table-column>
      <el-table-column label="执行状态" align="center" prop="resultStatus" width="110">
        <template #default="scope">
          <el-tag :type="resultStatusTagType(scope.row.resultStatus)">{{ resultStatusLabel(scope.row.resultStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="源端读取数" align="center" prop="readCount" width="110" />
      <el-table-column label="中间库暂存数" align="center" prop="stagedCount" width="120" />
      <el-table-column label="本次新增数" align="center" prop="insertedCount" width="110" />
      <el-table-column label="本次更新数" align="center" prop="updatedCount" width="110" />
      <el-table-column label="本次成功数" align="center" prop="successCount" width="110" />
      <el-table-column label="本次失败数" align="center" prop="failedCount" width="110" />
      <el-table-column label="累计成功数" align="center" prop="totalSuccessCount" width="110" />
      <el-table-column label="最近执行时间" align="center" prop="lastExecuteTime" width="170">
        <template #default="scope">{{ parseTime(scope.row.lastExecuteTime, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}</template>
      </el-table-column>
      <el-table-column label="说明" align="left" prop="message" min-width="220" :show-overflow-tooltip="true" />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="AccessResult">
import EChartPanel from "../components/EChartPanel.vue"
import { listAccessResult, getAccessResultSummary, getAccessResultDashboard } from "@/api/project1/accessResult"
import { listAccessPlan } from "@/api/project1/accessPlan"
import { listDatasource } from "@/api/project1/datasource"

const route = useRoute()
const router = useRouter()

const accessResultList = ref([])
const accessPlanOptions = ref([])
const datasourceOptions = ref([])
const resultSummary = ref({})
const dashboardData = ref({ recentBatches: [], topTables: [], batchOptions: [] })
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)

const accessModeOptions = [
  { label: "数据库直连", value: "db_direct" },
  { label: "API 拉取", value: "api_pull" },
  { label: "API 推送", value: "api_push" }
]

const accessTypeOptions = [
  { label: "一次性接入", value: "once" },
  { label: "持续接入", value: "continuous" }
]

const resultStatusOptions = [
  { label: "待执行", value: "pending" },
  { label: "执行中", value: "running" },
  { label: "成功", value: "success" },
  { label: "部分成功", value: "partial" },
  { label: "失败", value: "failed" },
  { label: "已取消", value: "canceled" }
]

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    accessPlanId: route.query.accessPlanId ? Number(route.query.accessPlanId) : undefined,
    accessBatchId: undefined,
    resultStatus: undefined
  }
})

const { queryParams } = toRefs(data)

const currentPlan = computed(() => {
  return accessPlanOptions.value.find(item => item.accessPlanId === queryParams.value.accessPlanId) || {}
})

const summary = computed(() => {
  if (resultSummary.value && resultSummary.value.accessPlanId) {
    return {
      totalSuccess: Number(resultSummary.value.totalSuccess || 0),
      totalFailed: Number(resultSummary.value.totalFailed || 0),
      lastInserted: Number(resultSummary.value.lastInserted || 0),
      lastUpdated: Number(resultSummary.value.lastUpdated || 0),
      lastFailed: Number(resultSummary.value.lastFailed || 0)
    }
  }
  const rows = accessResultList.value
  return {
    totalSuccess: rows.reduce((sum, item) => sum + Number(item.totalSuccessCount || 0), 0),
    totalFailed: rows.reduce((sum, item) => sum + Number(item.totalFailedCount || 0), 0),
    lastInserted: rows.reduce((sum, item) => sum + Number(item.insertedCount || 0), 0),
    lastUpdated: rows.reduce((sum, item) => sum + Number(item.updatedCount || 0), 0),
    lastFailed: rows.reduce((sum, item) => sum + Number(item.failedCount || 0), 0)
  }
})

const batchOptions = computed(() => dashboardData.value.batchOptions || [])

const accessIncrementChartOption = computed(() => {
  const rows = dashboardData.value.recentBatches || []
  return {
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    legend: { bottom: 0 },
    grid: { left: 36, right: 20, top: 26, bottom: 46, containLabel: true },
    xAxis: { type: "category", data: rows.map(item => item.batchName), axisTick: { show: false }, axisLabel: { color: "#606266" } },
    yAxis: { type: "value", axisLine: { show: false }, splitLine: { lineStyle: { color: "#edf2f7" } } },
    series: [
      { name: "新增", type: "bar", stack: "total", barWidth: 28, itemStyle: { color: "#409eff" }, data: rows.map(item => item.insertedCount || 0) },
      { name: "更新", type: "bar", stack: "total", barWidth: 28, itemStyle: { color: "#67c23a" }, data: rows.map(item => item.updatedCount || 0) },
      { name: "失败", type: "bar", stack: "total", barWidth: 28, itemStyle: { color: "#f56c6c" }, data: rows.map(item => item.failedCount || 0) }
    ]
  }
})

const topTableChartOption = computed(() => {
  const rows = dashboardData.value.topTables || []
  return {
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: { left: 130, right: 36, top: 12, bottom: 16, containLabel: true },
    xAxis: { type: "value", axisLine: { show: false }, splitLine: { lineStyle: { color: "#edf2f7" } } },
    yAxis: {
      type: "category",
      inverse: true,
      data: rows.map(item => item.label || item.targetTable || "-"),
      axisTick: { show: false },
      axisLabel: { color: "#606266" }
    },
    series: [{
      type: "bar",
      barWidth: 12,
      data: rows.map(item => item.value || item.count || 0),
      itemStyle: { color: "#409eff", borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: "right", color: "#606266" }
    }]
  }
})

function getList() {
  loading.value = true
  listAccessResult(queryParams.value).then(response => {
    accessResultList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })

  if (queryParams.value.accessPlanId) {
    getAccessResultSummary(queryParams.value.accessPlanId).then(response => {
      resultSummary.value = response.data || {}
    })
    getAccessResultDashboard(queryParams.value.accessPlanId).then(response => {
      dashboardData.value = response.data || { recentBatches: [], topTables: [], batchOptions: [] }
    })
  } else {
    resultSummary.value = {}
    dashboardData.value = { recentBatches: [], topTables: [], batchOptions: [] }
  }
}

function loadAccessPlanOptions() {
  listAccessPlan({ pageNum: 1, pageSize: 200 }).then(response => {
    accessPlanOptions.value = response.rows || []
    if (!queryParams.value.accessPlanId && accessPlanOptions.value.length) {
      queryParams.value.accessPlanId = accessPlanOptions.value[0].accessPlanId
    }
    getList()
  })
}

function loadDatasourceOptions() {
  listDatasource({ pageNum: 1, pageSize: 200 }).then(response => {
    datasourceOptions.value = response.rows || []
  })
}

function handlePlanChange() {
  queryParams.value.pageNum = 1
  queryParams.value.accessBatchId = undefined
  getList()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function goBack() {
  router.push({ path: "/access/accessPlan" })
}

function batchName(accessBatchId) {
  const batch = batchOptions.value.find(item => item.batchId === accessBatchId)
  return batch ? batch.batchName : (accessBatchId ? `BATCH-${String(accessBatchId).padStart(3, "0")}` : "-")
}

function accessModeLabel(value) {
  return getOptionLabel(accessModeOptions, value)
}

function accessTypeLabel(value) {
  return getOptionLabel(accessTypeOptions, value)
}

function resultStatusLabel(value) {
  return getOptionLabel(resultStatusOptions, value)
}

function datasourceName(datasourceId) {
  const datasource = datasourceOptions.value.find(item => item.datasourceId === datasourceId)
  return datasource ? datasource.datasourceName : datasourceId || "-"
}

function getOptionLabel(options, value) {
  const option = options.find(item => item.value === value)
  return option ? option.label : value || "-"
}

function resultStatusTagType(value) {
  return value === "success" ? "success" : value === "running" ? "warning" : value === "partial" ? "warning" : value === "failed" ? "danger" : "info"
}

loadDatasourceOptions()
loadAccessPlanOptions()
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 76px;
  padding: 14px 16px;
}

.stat-value {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  line-height: 28px;
}

.stat-label {
  color: #909399;
  margin-top: 8px;
}

.viz-panel {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  height: 320px;
  padding: 14px 16px;
}

.viz-title {
  color: #303133;
  font-weight: 600;
  margin-bottom: 12px;
}
</style>

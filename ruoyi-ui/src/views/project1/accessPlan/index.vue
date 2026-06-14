<template>
  <div class="app-container access-plan-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="96px">
      <el-form-item label="接入计划名称" prop="planName">
        <el-input
          v-model="queryParams.planName"
          placeholder="请输入接入计划名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源数据源" prop="sourceDatasourceId">
        <el-select v-model="queryParams.sourceDatasourceId" placeholder="全部" clearable style="width: 220px">
          <el-option v-for="item in datasourceOptions" :key="item.datasourceId" :label="item.datasourceName" :value="item.datasourceId" />
        </el-select>
      </el-form-item>
      <el-form-item label="接入类型" prop="accessType">
        <el-select v-model="queryParams.accessType" placeholder="全部" clearable style="width: 170px">
          <el-option v-for="item in accessTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="接入状态" prop="useStatus">
        <el-select v-model="queryParams.useStatus" placeholder="全部" clearable style="width: 170px">
          <el-option v-for="item in useStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['project1:accessPlan:add']">新增接入计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['project1:accessPlan:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['project1:accessPlan:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="VideoPlay" :disabled="single" @click="handleExecute()" v-hasPermi="['project1:accessPlan:edit']">手动执行</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="VideoPause" :disabled="multiple" @click="handleBatchPause" v-hasPermi="['project1:accessPlan:edit']">批量暂停</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="RefreshRight" :disabled="multiple" @click="handleBatchResume" v-hasPermi="['project1:accessPlan:edit']">批量恢复</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="accessPlanList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column label="接入计划名称" align="left" prop="planName" min-width="190" :show-overflow-tooltip="true" />
      <el-table-column label="来源数据源" align="left" min-width="130" :show-overflow-tooltip="true">
        <template #default="scope">{{ scope.row.sourceDatasourceName || datasourceName(scope.row.sourceDatasourceId) }}</template>
      </el-table-column>
      <el-table-column label="接入方式" align="center" prop="accessMode" width="115">
        <template #default="scope">
          <el-tag :type="accessModeTagType(scope.row.accessMode)">{{ accessModeLabel(scope.row.accessMode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接入类型" align="center" prop="accessType" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.accessType === 'continuous' ? 'warning' : 'primary'">{{ accessTypeLabel(scope.row.accessType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新周期" align="center" prop="cycleHours" width="110">
        <template #default="scope">{{ scope.row.accessType === "continuous" ? `每 ${scope.row.cycleHours || 1} 小时` : "-" }}</template>
      </el-table-column>
      <el-table-column label="默认结果集" align="left" prop="defaultResultSetName" min-width="190" :show-overflow-tooltip="true">
        <template #default="scope">{{ scope.row.defaultResultSetName || "-" }}</template>
      </el-table-column>
      <el-table-column label="当前状态" align="center" prop="displayStatus" width="110">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.displayStatus || scope.row.useStatus)">
            {{ displayStatusLabel(scope.row.displayStatus || scope.row.useStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="累计成功数" align="center" prop="totalSuccessCount" width="105" />
      <el-table-column label="最近新增数" align="center" prop="lastInsertedCount" width="105" />
      <el-table-column label="最近更新数" align="center" prop="lastUpdatedCount" width="105" />
      <el-table-column label="最近失败数" align="center" prop="lastFailedCount" width="105" />
      <el-table-column label="累计失败数" align="center" prop="totalFailedCount" width="105" />
      <el-table-column label="最近执行时间" align="center" prop="lastExecuteTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.lastExecuteTime, '{y}-{m}-{d} {h}:{i}:{s}') || "-" }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="290" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" @click="handleExecute(scope.row)" v-hasPermi="['project1:accessPlan:edit']">手动执行</el-button>
          <el-button v-if="scope.row.useStatus === 'paused'" link type="primary" @click="handleResume(scope.row)" v-hasPermi="['project1:accessPlan:edit']">恢复</el-button>
          <el-button v-else link type="primary" @click="handlePause(scope.row)" v-hasPermi="['project1:accessPlan:edit']">暂停</el-button>
          <el-button link type="primary" @click="handleCancelRun(scope.row)" v-hasPermi="['project1:accessPlan:edit']">取消当前</el-button>
          <el-button link type="primary" @click="handleViewResult(scope.row)">查看结果</el-button>
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

    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="accessPlanRef" :model="form" :rules="rules" label-width="128px" class="access-plan-form">
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="form.planName" placeholder="请输入接入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源数据源" prop="sourceDatasourceId">
              <el-select v-model="form.sourceDatasourceId" placeholder="请选择来源数据源" style="width: 100%" @change="handleSourceChange">
                <el-option v-for="item in datasourceOptions" :key="item.datasourceId" :label="item.datasourceName" :value="item.datasourceId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接入方式" prop="accessMode">
              <el-input :model-value="accessModeLabel(form.accessMode)" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接入类型" prop="accessType">
              <el-select v-model="form.accessType" placeholder="请选择接入类型" style="width: 100%" @change="handleAccessTypeChange">
                <el-option v-for="item in accessTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="form.accessType === 'continuous'" :span="12">
            <el-form-item label="更新周期（小时）" prop="cycleHours">
              <el-input-number v-model="form.cycleHours" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="接入计划详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border size="small" v-if="detailRow">
        <el-descriptions-item label="接入计划">{{ detailRow.planName }}</el-descriptions-item>
        <el-descriptions-item label="来源数据源">{{ detailRow.sourceDatasourceName || datasourceName(detailRow.sourceDatasourceId) }}</el-descriptions-item>
        <el-descriptions-item label="接入方式">{{ accessModeLabel(detailRow.accessMode) }}</el-descriptions-item>
        <el-descriptions-item label="接入类型">{{ accessTypeLabel(detailRow.accessType) }}</el-descriptions-item>
        <el-descriptions-item label="更新周期">{{ detailRow.accessType === "continuous" ? `每 ${detailRow.cycleHours || 1} 小时` : "-" }}</el-descriptions-item>
        <el-descriptions-item label="默认结果集">{{ detailRow.defaultResultSetName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ displayStatusLabel(detailRow.displayStatus || detailRow.useStatus) }}</el-descriptions-item>
        <el-descriptions-item label="最近执行时间">{{ parseTime(detailRow.lastExecuteTime, '{y}-{m}-{d} {h}:{i}:{s}') || "-" }}</el-descriptions-item>
        <el-descriptions-item label="累计成功数">{{ detailRow.totalSuccessCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="累计失败数">{{ detailRow.totalFailedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最近新增数">{{ detailRow.lastInsertedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最近更新数">{{ detailRow.lastUpdatedCount || 0 }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AccessPlan">
import { listAccessPlan, getAccessPlan, delAccessPlan, addAccessPlan, updateAccessPlan, executeAccessPlan, pauseAccessPlan, resumeAccessPlan, cancelAccessPlan } from "@/api/project1/accessPlan"
import { listDatasource } from "@/api/project1/datasource"

const { proxy } = getCurrentInstance()
const router = useRouter()

const accessPlanList = ref([])
const datasourceOptions = ref([])
const open = ref(false)
const detailOpen = ref(false)
const detailRow = ref(null)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const accessModeOptions = [
  { label: "数据库直连", value: "db_direct" },
  { label: "API 拉取", value: "api_pull" },
  { label: "API 推送", value: "api_push" }
]

const accessTypeOptions = [
  { label: "一次性接入", value: "once" },
  { label: "持续接入", value: "continuous" }
]

const useStatusOptions = [
  { label: "启用", value: "enabled" },
  { label: "暂停", value: "paused" },
  { label: "停用", value: "disabled" },
  { label: "暂时不可用", value: "unavailable" }
]

const statusOptions = [
  ...useStatusOptions,
  { label: "已完成", value: "success" },
  { label: "执行中", value: "running" },
  { label: "部分成功", value: "partial" },
  { label: "失败", value: "failed" },
  { label: "已取消", value: "canceled" },
  { label: "阻断", value: "blocked" },
  { label: "待执行", value: "pending" }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planName: undefined,
    sourceDatasourceId: undefined,
    accessType: undefined,
    useStatus: undefined
  },
  rules: {
    planName: [{ required: true, message: "计划名称不能为空", trigger: "blur" }],
    sourceDatasourceId: [{ required: true, message: "来源数据源不能为空", trigger: "change" }],
    accessMode: [{ required: true, message: "接入方式不能为空", trigger: "change" }],
    accessType: [{ required: true, message: "接入类型不能为空", trigger: "change" }],
    cycleHours: [{ required: true, message: "更新周期不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAccessPlan(queryParams.value).then(response => {
    accessPlanList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  })
}

function loadDatasourceOptions() {
  listDatasource({ pageNum: 1, pageSize: 200 }).then(response => {
    datasourceOptions.value = response.rows || []
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    accessPlanId: undefined,
    planName: undefined,
    sourceDatasourceId: undefined,
    accessMode: undefined,
    accessType: "once",
    cycleHours: undefined,
    specSetId: undefined,
    useStatus: "enabled",
    currentBatchId: undefined,
    lastSuccessCount: 0,
    lastFailedCount: 0,
    lastInsertedCount: 0,
    lastUpdatedCount: 0,
    totalSuccessCount: 0,
    totalFailedCount: 0,
    lastExecuteTime: undefined,
    status: "0",
    delFlag: "0"
  }
  proxy.resetForm("accessPlanRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.accessPlanId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增接入计划"
}

function handleUpdate(row) {
  reset()
  const accessPlanId = row.accessPlanId || ids.value
  getAccessPlan(accessPlanId).then(response => {
    form.value = {
      ...response.data,
      cycleHours: response.data?.accessType === "continuous" ? (response.data?.cycleHours || 1) : undefined
    }
    open.value = true
    title.value = "修改接入计划"
  })
}

function submitForm() {
  proxy.$refs["accessPlanRef"].validate(valid => {
    if (!valid) {
      return
    }
    if (form.value.accessType !== "continuous") {
      form.value.cycleHours = undefined
    } else if (!form.value.cycleHours) {
      form.value.cycleHours = 1
    }

    const request = form.value.accessPlanId != null ? updateAccessPlan(form.value) : addAccessPlan(form.value)
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.accessPlanId != null ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const accessPlanIds = row.accessPlanId || ids.value
  proxy.$modal.confirm('是否确认删除接入计划编号为"' + accessPlanIds + '"的数据项？').then(() => {
    return delAccessPlan(accessPlanIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleSourceChange(datasourceId) {
  const datasource = datasourceOptions.value.find(item => item.datasourceId === datasourceId)
  form.value.accessMode = datasource ? datasource.accessMode : undefined
}

function handleAccessTypeChange(accessType) {
  if (accessType === "continuous" && !form.value.cycleHours) {
    form.value.cycleHours = 1
  }
  if (accessType !== "continuous") {
    form.value.cycleHours = undefined
  }
}

function handleExecute(row) {
  const target = row || accessPlanList.value.find(item => item.accessPlanId === ids.value[0])
  if (!target) {
    proxy.$modal.msgWarning("请选择一个接入计划")
    return
  }
  executeAccessPlan(target.accessPlanId).then(response => {
    proxy.$modal.msgSuccess(formatAccessExecutionMessage(response.data?.message))
    getList()
  })
}

function formatAccessExecutionMessage(message) {
  if (!message) {
    return "接入执行完成"
  }
  if (message.includes("Access execution has started")) {
    return "接入执行已开始，请稍后刷新或查看结果"
  }
  if (message.includes("Access execution is already running")) {
    return "接入执行正在运行，请稍后刷新或查看结果"
  }
  return message
}

function handlePause(row) {
  pauseAccessPlan(row.accessPlanId).then(() => {
    proxy.$modal.msgSuccess("已暂停")
    getList()
  })
}

function handleResume(row) {
  resumeAccessPlan(row.accessPlanId).then(() => {
    proxy.$modal.msgSuccess("已恢复")
    getList()
  })
}

function handleCancelRun(row) {
  cancelAccessPlan(row.accessPlanId).then(() => {
    proxy.$modal.msgSuccess("已取消当前执行")
    getList()
  })
}

function handleBatchPause() {
  updateSelectedStatus(pauseAccessPlan, "批量暂停完成")
}

function handleBatchResume() {
  updateSelectedStatus(resumeAccessPlan, "批量恢复完成")
}

function updateSelectedStatus(action, message) {
  const selectedRows = accessPlanList.value.filter(item => ids.value.includes(item.accessPlanId))
  Promise.all(selectedRows.map(item => action(item.accessPlanId))).then(() => {
    proxy.$modal.msgSuccess(message)
    getList()
  })
}

function handleViewResult(row) {
  router.push({ path: "/dossier/access/accessResult", query: { accessPlanId: row.accessPlanId } })
}

function handleDetail(row) {
  detailRow.value = row
  detailOpen.value = true
}

function datasourceName(datasourceId) {
  const datasource = datasourceOptions.value.find(item => item.datasourceId === datasourceId)
  return datasource ? datasource.datasourceName : datasourceId || "-"
}

function accessModeLabel(value) {
  return getOptionLabel(accessModeOptions, value)
}

function accessTypeLabel(value) {
  return getOptionLabel(accessTypeOptions, value)
}

function displayStatusLabel(value) {
  return getOptionLabel(statusOptions, value)
}

function getOptionLabel(options, value) {
  const option = options.find(item => item.value === value)
  return option ? option.label : value || "-"
}

function accessModeTagType(value) {
  return value === "db_direct" ? "primary" : value === "api_pull" ? "warning" : "success"
}

function statusTagType(value) {
  if (["enabled", "success"].includes(value)) return "success"
  if (["paused", "running", "partial"].includes(value)) return "warning"
  if (["disabled", "canceled"].includes(value)) return "info"
  if (["unavailable", "blocked", "failed"].includes(value)) return "danger"
  return "info"
}

loadDatasourceOptions()
getList()
</script>

<style scoped>
.access-plan-page :deep(.el-table td.el-table__cell) {
  padding: 7px 0;
}

.access-plan-page :deep(.el-table th.el-table__cell) {
  padding: 8px 0;
}

.access-plan-form :deep(.el-form-item) {
  margin-bottom: 22px;
}
</style>

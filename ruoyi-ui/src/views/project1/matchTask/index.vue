<template>
  <div class="app-container match-task-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="任务名称" prop="taskName">
        <el-input
          v-model="queryParams.taskName"
          placeholder="请输入任务名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="源数据源" prop="sourceDatasourceId">
        <el-select v-model="queryParams.sourceDatasourceId" placeholder="全部" clearable style="width: 220px">
          <el-option
            v-for="item in datasourceOptions"
            :key="item.datasourceId"
            :label="item.datasourceName"
            :value="item.datasourceId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['project1:matchTask:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['project1:matchTask:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['project1:matchTask:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['project1:matchTask:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="matchTaskList" @selection-change="handleSelectionChange" row-key="taskId">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="任务名称" prop="taskName" min-width="190" :show-overflow-tooltip="true" />
      <el-table-column label="源数据源" prop="sourceDatasourceId" min-width="160" :show-overflow-tooltip="true">
        <template #default="scope">{{ datasourceName(scope.row.sourceDatasourceId) }}</template>
      </el-table-column>
      <el-table-column label="当前版本" prop="currentVersionNo" width="100" align="center">
        <template #default="scope">
          <el-tag class="version-tag" type="info">V{{ scope.row.currentVersionNo || 1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="编码模式" prop="currentEncodingModeKey" min-width="150" :show-overflow-tooltip="true">
        <template #default="scope">{{ encodingModeLabel(scope.row.currentEncodingModeKey) }}</template>
      </el-table-column>
      <el-table-column label="嵌入模型" prop="currentEmbeddingModelKey" min-width="130" :show-overflow-tooltip="true">
        <template #default="scope">{{ embeddingModelLabel(scope.row.currentEmbeddingModelKey) }}</template>
      </el-table-column>
      <el-table-column label="任务状态" prop="lifecycleStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="lifecycleTagType(scope.row.lifecycleStatus)">{{ lifecycleLabel(scope.row.lifecycleStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最近运行状态" prop="lastRecordStatus" width="150" align="center">
        <template #default="scope">
          <div v-if="scope.row.lastRecordStatus === 'running'" class="run-progress">
            <el-progress :percentage="Number(scope.row.lastRecordProgress || 0)" :stroke-width="8" :show-text="false" />
            <span>{{ Number(scope.row.lastRecordProgress || 0) }}%</span>
          </div>
          <el-tag v-else :type="recordStatusTagType(scope.row.lastRecordStatus)">
            {{ recordStatusLabel(scope.row.lastRecordStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="运行阶段" prop="lastRecordStage" min-width="190" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" width="120" align="center">
        <template #default="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="310" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="VideoPlay" @click="handleRun(scope.row)" v-hasPermi="['project1:matchTask:edit']">运行</el-button>
          <el-button link type="primary" icon="Tickets" @click="handleVersion(scope.row)">版本</el-button>
          <el-button link type="primary" icon="Document" @click="handleRecord(scope.row)">运行记录</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['project1:matchTask:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['project1:matchTask:remove']">删除</el-button>
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
      <el-form ref="matchTaskRef" :model="form" :rules="rules" label-width="118px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源数据源" prop="sourceDatasourceId">
              <el-select v-model="form.sourceDatasourceId" placeholder="请选择源数据源" style="width: 100%">
                <el-option
                  v-for="item in datasourceOptions"
                  :key="item.datasourceId"
                  :label="item.datasourceName"
                  :value="item.datasourceId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="列编码模式" prop="currentVersion.encodingModeKey">
              <el-select v-model="form.currentVersion.encodingModeKey" style="width: 100%">
                <el-option v-for="item in encodingModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="嵌入模型" prop="currentVersion.embeddingModelKey">
              <el-select v-model="form.currentVersion.embeddingModelKey" style="width: 100%">
                <el-option v-for="item in embeddingModelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评估模式" prop="currentVersion.evalModeKey">
              <el-select v-model="form.currentVersion.evalModeKey" style="width: 100%">
                <el-option v-for="item in evalModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="自动通过阈值" prop="currentVersion.autoApproveThreshold">
              <el-input-number
                v-model="form.currentVersion.autoApproveThreshold"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="2"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="llm-section">
          <div class="section-title">LLM 配置</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Provider" prop="currentVersion.llmProvider">
                <el-select v-model="form.currentVersion.llmProvider" style="width: 100%">
                  <el-option v-for="item in providerOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Model" prop="currentVersion.llmModel">
                <el-select v-model="form.currentVersion.llmModel" style="width: 100%">
                  <el-option v-for="item in modelOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="任务版本参数配置" v-model="versionOpen" width="980px" append-to-body>
      <el-table :data="versionRows" border>
        <el-table-column label="版本" width="90" align="center">
          <template #default="scope">
            <el-tag type="info">V{{ scope.row.versionNo }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.current" type="success">当前</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="源数据源" min-width="150" :show-overflow-tooltip="true">
          <template #default="scope">{{ datasourceName(scope.row.sourceDatasourceId) }}</template>
        </el-table-column>
        <el-table-column label="编码模式" min-width="150" :show-overflow-tooltip="true">
          <template #default="scope">{{ encodingModeLabel(scope.row.encodingModeKey) }}</template>
        </el-table-column>
        <el-table-column label="嵌入模型" min-width="130" :show-overflow-tooltip="true">
          <template #default="scope">{{ embeddingModelLabel(scope.row.embeddingModelKey) }}</template>
        </el-table-column>
        <el-table-column label="评估模式" min-width="130" :show-overflow-tooltip="true">
          <template #default="scope">{{ evalModeLabel(scope.row.evalModeKey) }}</template>
        </el-table-column>
        <el-table-column label="Provider" prop="llmProvider" width="120" />
        <el-table-column label="Model" prop="llmModel" min-width="140" :show-overflow-tooltip="true" />
        <el-table-column label="阈值" prop="autoApproveThreshold" width="90" align="center" />
        <el-table-column label="变更说明" prop="changeSummary" min-width="180" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" prop="createTime" width="160" align="center">
          <template #default="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="运行记录" v-model="recordOpen" width="920px" append-to-body>
      <el-table :data="recordRows" border>
        <el-table-column label="运行记录编号" prop="recordLabel" width="130" align="center" />
        <el-table-column label="执行版本" width="100" align="center">
          <template #default="scope">
            <el-tag type="info">V{{ scope.row.versionNo || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="运行状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="recordStatusTagType(scope.row.executionStatus)">{{ recordStatusLabel(scope.row.executionStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="150" align="center">
          <template #default="scope">
            <el-progress :percentage="Number(scope.row.progress || 0)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="阶段" prop="currentStage" min-width="190" :show-overflow-tooltip="true" />
        <el-table-column label="开始时间" width="160" align="center">
          <template #default="scope">{{ parseTime(scope.row.startedAt, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="160" align="center">
          <template #default="scope">{{ parseTime(scope.row.finishedAt, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
        <el-table-column label="错误信息" prop="errorMessage" min-width="160" :show-overflow-tooltip="true">
          <template #default="scope">{{ scope.row.errorMessage || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="MatchTask">
import { listMatchTask, getMatchTask, delMatchTask, addMatchTask, updateMatchTask, runMatchTask, listMatchTaskVersion, listMatchTaskRecord } from "@/api/project1/matchTask"
import { listDatasource } from "@/api/project1/datasource"

const { proxy } = getCurrentInstance()

const matchTaskList = ref([])
const datasourceOptions = ref([])
const open = ref(false)
const versionOpen = ref(false)
const recordOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const versionRows = ref([])
const recordRows = ref([])

const encodingModeOptions = [
  { label: "header_values_default", value: "header_values_default" },
  { label: "header_only", value: "header_only" }
]

const embeddingModelOptions = [
  { label: "mpnet", value: "mpnet" },
  { label: "mpnet-finetuned", value: "mpnet-finetuned" }
]

const evalModeOptions = [
  { label: "global_unknown", value: "global_unknown" },
  { label: "known_table", value: "known_table" }
]

const providerOptions = [
  { label: "deepseek", value: "deepseek" },
  { label: "openai", value: "openai" },
  { label: "none", value: "none" }
]

const modelOptions = [
  { label: "deepseek-chat", value: "deepseek-chat" },
  { label: "gpt-4o-mini", value: "gpt-4o-mini" },
  { label: "none", value: "none" }
]

const lifecycleOptions = [
  { label: "草稿", value: "draft" },
  { label: "启用", value: "active" },
  { label: "停用", value: "disabled" },
  { label: "归档", value: "archived" }
]

const recordStatusOptions = [
  { label: "未运行", value: "pending" },
  { label: "排队", value: "queued" },
  { label: "运行中", value: "running" },
  { label: "成功", value: "success" },
  { label: "失败", value: "failed" },
  { label: "已取消", value: "canceled" },
  { label: "阻断", value: "blocked" },
  { label: "已完成", value: "completed" }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taskName: undefined,
    sourceDatasourceId: undefined
  },
  rules: {
    taskName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
    sourceDatasourceId: [{ required: true, message: "源数据源不能为空", trigger: "change" }],
    "currentVersion.encodingModeKey": [{ required: true, message: "列编码模式不能为空", trigger: "change" }],
    "currentVersion.embeddingModelKey": [{ required: true, message: "嵌入模型不能为空", trigger: "change" }],
    "currentVersion.evalModeKey": [{ required: true, message: "评估模式不能为空", trigger: "change" }],
    "currentVersion.llmProvider": [{ required: true, message: "Provider 不能为空", trigger: "change" }],
    "currentVersion.llmModel": [{ required: true, message: "Model 不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function defaultVersion() {
  return {
    encodingModeKey: "header_values_default",
    embeddingModelKey: "mpnet",
    evalModeKey: "global_unknown",
    autoApproveThreshold: 0.9,
    llmProvider: "deepseek",
    llmModel: "deepseek-chat",
    algorithmParamsJson: undefined,
    changeSummary: undefined
  }
}

function getList() {
  loading.value = true
  listMatchTask(queryParams.value).then(response => {
    matchTaskList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
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
    taskId: undefined,
    taskName: undefined,
    sourceDatasourceId: undefined,
    currentVersion: defaultVersion(),
    remark: undefined
  }
  proxy.resetForm("matchTaskRef")
}

function normalizeForm(row) {
  const currentVersion = Object.assign(defaultVersion(), row.currentVersion || {})
  return {
    taskId: row.taskId,
    taskName: row.taskName,
    sourceDatasourceId: row.sourceDatasourceId,
    currentVersion,
    remark: row.remark
  }
}

function buildSubmitPayload() {
  return {
    taskId: form.value.taskId,
    taskName: form.value.taskName,
    sourceDatasourceId: form.value.sourceDatasourceId,
    remark: form.value.remark,
    currentVersion: Object.assign({}, defaultVersion(), form.value.currentVersion)
  }
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
  ids.value = selection.map(item => item.taskId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增模式映射任务"
}

function handleUpdate(row) {
  reset()
  const taskId = row.taskId || ids.value[0]
  getMatchTask(taskId).then(response => {
    form.value = normalizeForm(response.data || {})
    open.value = true
    title.value = "修改模式映射任务"
  })
}

function submitForm() {
  proxy.$refs["matchTaskRef"].validate(valid => {
    if (!valid) {
      return
    }
    const payload = buildSubmitPayload()
    if (payload.taskId != null) {
      updateMatchTask(payload).then(() => {
        proxy.$modal.msgSuccess("修改成功，已生成新的任务版本")
        open.value = false
        getList()
      })
    } else {
      addMatchTask(payload).then(() => {
        proxy.$modal.msgSuccess("新增成功，已生成 V1")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const taskIds = row.taskId || ids.value
  proxy.$modal.confirm('是否确认删除模式映射任务编号为 "' + taskIds + '" 的数据项？').then(() => {
    return delMatchTask(taskIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("project1/matchTask/export", {
    ...queryParams.value
  }, `matchTask_${new Date().getTime()}.xlsx`)
}

function handleRun(row) {
  runMatchTask(row.taskId).then(response => {
    proxy.$modal.msgSuccess(response.data?.message || "任务运行完成")
    getList()
  })
}

function handleVersion(row) {
  listMatchTaskVersion(row.taskId).then(response => {
    versionRows.value = response.data?.rows || []
    versionOpen.value = true
  })
}

function handleRecord(row) {
  listMatchTaskRecord(row.taskId).then(response => {
    recordRows.value = response.data?.rows || []
    recordOpen.value = true
  })
}

function datasourceName(datasourceId) {
  const datasource = datasourceOptions.value.find(item => item.datasourceId === datasourceId)
  return datasource ? datasource.datasourceName : datasourceId || "-"
}

function encodingModeLabel(value) {
  return getOptionLabel(encodingModeOptions, value)
}

function embeddingModelLabel(value) {
  return getOptionLabel(embeddingModelOptions, value)
}

function evalModeLabel(value) {
  return getOptionLabel(evalModeOptions, value)
}

function lifecycleLabel(value) {
  return getOptionLabel(lifecycleOptions, value)
}

function recordStatusLabel(value) {
  return getOptionLabel(recordStatusOptions, value)
}

function getOptionLabel(options, value) {
  const option = options.find(item => item.value === value)
  return option ? option.label : value || "-"
}

function lifecycleTagType(value) {
  return value === "active" ? "success" : value === "draft" ? "info" : value === "archived" ? "warning" : "danger"
}

function recordStatusTagType(value) {
  return value === "success" || value === "completed" ? "success" : value === "running" || value === "queued" ? "warning" : value === "failed" || value === "blocked" ? "danger" : "info"
}

loadDatasourceOptions()
getList()
</script>

<style scoped>
.match-task-page :deep(.el-table .cell) {
  line-height: 22px;
}

.match-task-page :deep(.el-table__row td) {
  padding: 8px 0;
}

.version-tag {
  min-width: 42px;
}

.run-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.run-progress :deep(.el-progress) {
  flex: 1;
  min-width: 74px;
}

.llm-section {
  margin: 4px 0 18px;
  padding: 14px 14px 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.section-title {
  margin-bottom: 12px;
  color: #606266;
  font-weight: 600;
}
</style>

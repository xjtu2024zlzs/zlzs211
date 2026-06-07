<template>
  <div class="app-container datasource-page">
    <el-form
      v-show="showSearch"
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      label-width="82px"
      class="compact-query"
    >
      <el-form-item label="数据源名称" prop="datasourceName">
        <el-input
          v-model="queryParams.datasourceName"
          placeholder="请输入数据源名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接入方式" prop="accessMode">
        <el-select v-model="queryParams.accessMode" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in accessModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="使用状态" prop="useStatus">
        <el-select v-model="queryParams.useStatus" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in useStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="连接状态" prop="connectionStatus">
        <el-select v-model="queryParams.connectionStatus" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in connectionStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['project1:datasource:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['project1:datasource:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['project1:datasource:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['project1:datasource:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="datasourceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="数据源名称" align="left" prop="datasourceName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="接入方式" align="center" prop="accessMode" width="120">
        <template #default="scope">
          <el-tag :type="accessModeTagType(scope.row.accessMode)">{{ accessModeLabel(scope.row.accessMode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="使用状态" align="center" prop="useStatus" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.useStatus === 'unavailable'" type="info">暂时不可用</el-tag>
          <el-tag v-else-if="scope.row.useStatus === 'paused'" type="warning">暂停</el-tag>
          <el-switch
            v-else
            v-model="scope.row.useStatus"
            active-value="enabled"
            inactive-value="disabled"
            active-text="启用"
            inactive-text="停用"
            inline-prompt
            @change="handleUseStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="连接状态" align="center" prop="connectionStatus" width="110">
        <template #default="scope">
          <el-tag :type="connectionStatusTagType(scope.row.connectionStatus)">
            {{ connectionStatusLabel(scope.row.connectionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最新测试时间" align="center" prop="lastTestTime" width="155">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastTestTime, '{y}-{m}-{d}') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="模式状态" align="center" prop="latestSchemaSnapshotId" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.latestSchemaSnapshotId ? 'success' : 'info'">
            {{ scope.row.latestSchemaSnapshotId ? '已读取' : '未读取' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="测试结果" align="left" prop="lastTestMessage" min-width="190" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="330" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Connection" @click="handleTestConnection(scope.row)" v-hasPermi="['project1:datasource:edit']">测试连接</el-button>
          <el-button link type="primary" icon="Reading" @click="handleReadSchema(scope.row)" v-hasPermi="['project1:datasource:edit']">模式读取</el-button>
          <el-button link type="primary" icon="View" @click="handleViewSchema(scope.row)">模式查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['project1:datasource:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['project1:datasource:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="760px" append-to-body class="datasource-dialog">
      <el-form ref="datasourceRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="名称" prop="datasourceName">
              <el-input v-model="form.datasourceName" placeholder="请输入数据源名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接入方式" prop="accessMode">
              <el-select v-model="form.accessMode" placeholder="请选择接入方式" style="width: 100%" @change="handleAccessModeChange">
                <el-option v-for="item in accessModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.accessMode === 'db_direct'" :gutter="24">
          <el-col :span="12">
            <el-form-item label="数据库类型">
              <el-select v-model="form.dbDetail.dbType" placeholder="请选择数据库类型" style="width: 100%">
                <el-option v-for="item in dbTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主机地址">
              <el-input v-model="form.dbDetail.host" placeholder="请输入主机地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口">
              <el-input-number v-model="form.dbDetail.port" :min="1" :max="65535" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据库名">
              <el-input v-model="form.dbDetail.databaseName" placeholder="请输入数据库名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.dbDetail.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码">
              <el-input v-model="form.dbDetail.passwordEnc" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Schema">
              <el-input v-model="form.dbDetail.schemaName" placeholder="请输入 Schema" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="连接参数">
              <el-input v-model="form.dbDetail.connectionParams" placeholder="例如 sslmode=prefer" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.accessMode === 'api_pull'" :gutter="24">
          <el-col :span="12">
            <el-form-item label="基础地址">
              <el-input v-model="form.apiPullDetail.baseUrl" placeholder="例如 http://127.0.0.1:9002" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拉取端点">
              <el-input v-model="form.apiPullDetail.pullEndpoint" placeholder="例如 /api/pull" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求方法">
              <el-select v-model="form.apiPullDetail.requestMethod" placeholder="请选择请求方法" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证方式">
              <el-select v-model="form.apiPullDetail.authType" placeholder="请选择认证方式" style="width: 100%">
                <el-option label="Bearer Token" value="Bearer Token" />
                <el-option label="Basic Auth" value="Basic Auth" />
                <el-option label="无" value="None" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康检查端点">
              <el-input v-model="form.apiPullDetail.healthEndpoint" placeholder="例如 /api/health" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接口密钥">
              <el-input v-model="form.apiPullDetail.apiKeyEnc" type="password" show-password placeholder="请输入接口密钥" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.accessMode === 'api_push'" :gutter="24">
          <el-col :span="12">
            <el-form-item label="监听路径">
              <el-input v-model="form.apiPushDetail.listenPath" placeholder="例如 /api/trigger-push" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据格式">
              <el-select v-model="form.apiPushDetail.payloadFormat" placeholder="请选择数据格式" style="width: 100%">
                <el-option label="JSON" value="JSON" />
                <el-option label="XML" value="XML" />
                <el-option label="CSV" value="CSV" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="鉴权方式">
              <el-select v-model="form.apiPushDetail.authType" placeholder="请选择鉴权方式" style="width: 100%">
                <el-option label="签名 Header" value="签名 Header" />
                <el-option label="Bearer Token" value="Bearer Token" />
                <el-option label="无" value="None" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签名 Header">
              <el-input v-model="form.apiPushDetail.signatureHeader" placeholder="例如 X-Dossier-Signature" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回调密钥">
              <el-input v-model="form.apiPushDetail.pushSecretEnc" type="password" show-password placeholder="请输入回调密钥" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="IP 白名单">
              <el-input v-model="form.apiPushDetail.ipWhitelist" placeholder="例如 127.0.0.1,10.0.0.0/24" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="说明" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入数据源说明" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" icon="Check" @click="submitForm">确定</el-button>
          <el-button icon="Close" @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="数据源模式查看" v-model="schemaOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="数据源名称">{{ schemaSource.datasourceName }}</el-descriptions-item>
        <el-descriptions-item label="接入方式">{{ accessModeLabel(schemaSource.accessMode) }}</el-descriptions-item>
        <el-descriptions-item label="模式状态">已读取</el-descriptions-item>
        <el-descriptions-item label="读取时间">{{ parseTime(schemaSource.updateTime || schemaSource.createTime, '{y}-{m}-{d}') || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="schemaPreviewRows" class="mt16" border>
        <el-table-column label="实体/表名" prop="entityName" min-width="180" />
        <el-table-column label="字段数" prop="fieldCount" width="100" align="center" />
        <el-table-column label="样例字段" prop="sampleFields" min-width="260" :show-overflow-tooltip="true" />
        <el-table-column label="说明" prop="remark" min-width="180" :show-overflow-tooltip="true" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="Datasource">
import {
  listDatasource,
  getDatasource,
  delDatasource,
  addDatasource,
  updateDatasource,
  testDatasourceConnection,
  readDatasourceSchema,
  getDatasourceSchema
} from "@/api/project1/datasource"

const { proxy } = getCurrentInstance()

const datasourceList = ref([])
const open = ref(false)
const schemaOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const schemaSource = ref({})

const accessModeOptions = [
  { label: "数据库直连", value: "db_direct" },
  { label: "API 拉取", value: "api_pull" },
  { label: "API 推送", value: "api_push" }
]

const useStatusOptions = [
  { label: "启用", value: "enabled" },
  { label: "暂停", value: "paused" },
  { label: "停用", value: "disabled" },
  { label: "暂时不可用", value: "unavailable" }
]

const connectionStatusOptions = [
  { label: "未测试", value: "untested" },
  { label: "正常", value: "success" },
  { label: "异常", value: "failed" }
]

const dbTypeOptions = ["PostgreSQL", "MySQL", "SQL Server", "Oracle"]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    datasourceName: undefined,
    accessMode: undefined,
    useStatus: undefined,
    connectionStatus: undefined
  },
  rules: {
    datasourceName: [{ required: true, message: "数据源名称不能为空", trigger: "blur" }],
    accessMode: [{ required: true, message: "接入方式不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const schemaPreviewRows = computed(() => schemaSource.value.rows || [])

function getList() {
  loading.value = true
  listDatasource(queryParams.value).then(response => {
    datasourceList.value = response.rows || []
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    datasourceId: undefined,
    datasourceName: undefined,
    accessMode: "api_pull",
    useStatus: "enabled",
    connectionStatus: "untested",
    status: "0",
    delFlag: "0",
    remark: undefined,
    dbDetail: defaultDbDetail(),
    apiPullDetail: defaultApiPullDetail(),
    apiPushDetail: defaultApiPushDetail()
  }
  proxy.resetForm("datasourceRef")
}

function defaultDbDetail() {
  return {
    dbType: "PostgreSQL",
    host: "localhost",
    port: 5463,
    databaseName: "aviation_mes",
    schemaName: "public",
    username: "mes_reader",
    passwordEnc: "",
    savePassword: 1,
    connectionParams: "sslmode=prefer"
  }
}

function defaultApiPullDetail() {
  return {
    baseUrl: "http://127.0.0.1:9002",
    pullEndpoint: "/api/pull",
    schemaEndpoint: "/api/schema",
    requestMethod: "GET",
    authType: "Bearer Token",
    healthEndpoint: "/api/health",
    apiKeyEnc: "",
    headersConfig: undefined,
    extraConfig: undefined
  }
}

function defaultApiPushDetail() {
  return {
    listenPath: "/api/trigger-push",
    payloadFormat: "JSON",
    authType: "签名 Header",
    signatureHeader: "X-Dossier-Signature",
    pushSecretEnc: "",
    ipWhitelist: "127.0.0.1,10.0.0.0/24",
    healthEndpoint: "/api/health",
    headersConfig: undefined,
    extraConfig: undefined
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
  ids.value = selection.map(item => item.datasourceId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增数据源"
}

function handleUpdate(row) {
  reset()
  const datasourceId = row.datasourceId || ids.value
  getDatasource(datasourceId).then(response => {
    form.value = normalizeForm(response.data || {})
    open.value = true
    title.value = "修改数据源"
  })
}

function normalizeForm(data) {
  return {
    datasourceId: data.datasourceId,
    datasourceName: data.datasourceName,
    accessMode: data.accessMode || "api_pull",
    useStatus: data.useStatus || "enabled",
    connectionStatus: data.connectionStatus || "untested",
    lastTestTime: data.lastTestTime,
    lastTestMessage: data.lastTestMessage,
    latestSchemaSnapshotId: data.latestSchemaSnapshotId,
    status: data.status || "0",
    delFlag: data.delFlag || "0",
    remark: data.remark,
    dbDetail: { ...defaultDbDetail(), ...(data.dbDetail || {}) },
    apiPullDetail: { ...defaultApiPullDetail(), ...(data.apiPullDetail || {}) },
    apiPushDetail: { ...defaultApiPushDetail(), ...(data.apiPushDetail || {}) }
  }
}

function handleAccessModeChange() {
  form.value.dbDetail = { ...defaultDbDetail(), ...(form.value.dbDetail || {}) }
  form.value.apiPullDetail = { ...defaultApiPullDetail(), ...(form.value.apiPullDetail || {}) }
  form.value.apiPushDetail = { ...defaultApiPushDetail(), ...(form.value.apiPushDetail || {}) }
}

function submitForm() {
  proxy.$refs["datasourceRef"].validate(valid => {
    if (!valid || !validateModeDetail()) {
      return
    }
    const payload = buildSubmitPayload()
    const request = payload.datasourceId != null ? updateDatasource(payload) : addDatasource(payload)
    request.then(() => {
      proxy.$modal.msgSuccess(payload.datasourceId != null ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function buildSubmitPayload() {
  const payload = {
    datasourceId: form.value.datasourceId,
    datasourceName: form.value.datasourceName,
    accessMode: form.value.accessMode,
    useStatus: form.value.useStatus || "enabled",
    connectionStatus: form.value.connectionStatus || "untested",
    status: form.value.status || "0",
    delFlag: form.value.delFlag || "0",
    remark: form.value.remark
  }
  if (form.value.accessMode === "db_direct") {
    payload.dbDetail = { ...form.value.dbDetail }
  } else if (form.value.accessMode === "api_pull") {
    payload.apiPullDetail = { ...form.value.apiPullDetail }
  } else if (form.value.accessMode === "api_push") {
    payload.apiPushDetail = { ...form.value.apiPushDetail }
  }
  return payload
}

function validateModeDetail() {
  if (form.value.accessMode === "db_direct") {
    const detail = form.value.dbDetail
    if (!detail.dbType || !detail.host || !detail.port || !detail.databaseName || !detail.username) {
      proxy.$modal.msgError("请完整填写数据库直连配置")
      return false
    }
  }
  if (form.value.accessMode === "api_pull") {
    const detail = form.value.apiPullDetail
    if (!detail.baseUrl || !detail.pullEndpoint || !detail.requestMethod) {
      proxy.$modal.msgError("请完整填写 API 拉取配置")
      return false
    }
  }
  if (form.value.accessMode === "api_push") {
    const detail = form.value.apiPushDetail
    if (!detail.listenPath || !detail.payloadFormat || !detail.authType) {
      proxy.$modal.msgError("请完整填写 API 推送配置")
      return false
    }
  }
  return true
}

function handleDelete(row) {
  const datasourceIds = row.datasourceId || ids.value
  proxy.$modal.confirm('是否确认删除数据源编号为"' + datasourceIds + '"的数据项？').then(() => {
    return delDatasource(datasourceIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("project1/datasource/export", {
    ...queryParams.value
  }, `datasource_${new Date().getTime()}.xlsx`)
}

function handleUseStatusChange(row) {
  updateDatasource({
    datasourceId: row.datasourceId,
    useStatus: row.useStatus
  }).then(() => {
    proxy.$modal.msgSuccess("使用状态已更新")
  }).catch(() => {
    getList()
  })
}

function handleTestConnection(row) {
  testDatasourceConnection(row.datasourceId).then(response => {
    proxy.$modal.msgSuccess(response.data?.lastTestMessage || "连接测试通过")
    getList()
  })
}

function handleReadSchema(row) {
  if (row.latestSchemaSnapshotId) {
    proxy.$modal.msgWarning("模式已读取")
    return
  }
  readDatasourceSchema(row.datasourceId).then(response => {
    const message = response.data?.message || "模式读取完成"
    if (response.data?.alreadyRead) {
      proxy.$modal.msgWarning(message)
    } else {
      proxy.$modal.msgSuccess(message)
    }
    getList()
  })
}

function handleViewSchema(row) {
  if (!row.latestSchemaSnapshotId) {
    proxy.$modal.msgWarning("请先进行模式读取")
    return
  }
  getDatasourceSchema(row.datasourceId).then(response => {
    schemaSource.value = { ...row, ...(response.data || {}) }
    schemaOpen.value = true
  })
}

function accessModeLabel(value) {
  return getOptionLabel(accessModeOptions, value)
}

function connectionStatusLabel(value) {
  return getOptionLabel(connectionStatusOptions, value)
}

function getOptionLabel(options, value) {
  const option = options.find(item => item.value === value)
  return option ? option.label : value || "-"
}

function accessModeTagType(value) {
  return value === "db_direct" ? "primary" : value === "api_pull" ? "warning" : "success"
}

function connectionStatusTagType(value) {
  return value === "success" ? "success" : value === "failed" ? "danger" : "info"
}

getList()
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}

.compact-query {
  margin-bottom: 4px;
}

.datasource-page :deep(.el-table__cell) {
  padding: 8px 0;
}

.datasource-page :deep(.el-table .cell) {
  line-height: 22px;
}

.datasource-page :deep(.el-dialog__body) {
  padding-top: 14px;
  padding-bottom: 8px;
}
</style>

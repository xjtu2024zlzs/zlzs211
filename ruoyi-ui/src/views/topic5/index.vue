<template>
  <div class="app-container topic5-trace-page">

    <!-- 顶部流程 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课题五追溯流程</span>
          <span class="header-tip">当前选择任务：{{ currentTrace.traceNo || '未选择' }}</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="问题填报" />
        <el-step title="数据处理" />
        <el-step title="第一部分算法运行" />
        <el-step title="知识图谱导入" />
        <el-step title="第二部分算法运行" />
        <el-step title="进行溯源" />
      </el-steps>
    </el-card>

    <!-- 查询区域 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>追溯问题查询</span>
      </template>

      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="90px">
        <el-form-item label="任务编号">
          <el-input
            v-model="queryParams.traceNo"
            placeholder="请输入追溯任务编号"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="架次">
          <el-input
            v-model="queryParams.aircraftNo"
            placeholder="请输入架次"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="发生部位">
          <el-input
            v-model="queryParams.partName"
            placeholder="请输入发生部位"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史追溯问题展示区 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>历史追溯问题</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">
            填写新的追溯问题
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="traceList"
        border
        highlight-current-row
        @row-click="handleRowClick"
      >
        <el-table-column prop="traceNo" label="追溯任务编号" min-width="170" />
        <el-table-column prop="eventTime" label="发生时间" min-width="170" />
        <el-table-column prop="aircraftNo" label="架次" min-width="120" />
        <el-table-column prop="partName" label="发生部位" min-width="150" />
        <el-table-column prop="problemType" label="问题类型" min-width="120" />

        <el-table-column prop="status" label="任务状态" min-width="130">
          <template #default="scope">
            <el-tag :type="traceStatusTagType(scope.row.status)">
              {{ scope.row.status || '未处理' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="知识图谱重构" min-width="150">
          <template #default="scope">
            <el-button
              v-if="isGenerated(scope.row.kgRebuildStatus)"
              link
              type="primary"
              @click.stop="openKnowledgeGraph(scope.row)"
            >
              查看知识图谱
            </el-button>
            <el-tag v-else type="info">未生成</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="溯源结果" min-width="150">
          <template #default="scope">
            <el-button
              v-if="isGenerated(scope.row.traceReportStatus)"
              link
              type="success"
              @click.stop="openTraceReport(scope.row)"
            >
              打开Word
            </el-button>
            <el-tag v-else type="info">未生成</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="selectTrace(scope.row)">
              选择
            </el-button>
            <el-button link type="primary" @click.stop="viewTrace(scope.row)">
              查看
            </el-button>
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
    </el-card>

    <!-- 当前选择任务信息 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>当前追溯任务信息</span>
      </template>

      <el-empty v-if="!currentTrace.id" description="请先从历史追溯问题表中选择一条追溯任务" />

      <el-descriptions v-else :column="3" border>
        <el-descriptions-item label="追溯任务编号">
          {{ currentTrace.traceNo }}
        </el-descriptions-item>

        <el-descriptions-item label="发生时间">
          {{ currentTrace.eventTime }}
        </el-descriptions-item>

        <el-descriptions-item label="架次">
          {{ currentTrace.aircraftNo }}
        </el-descriptions-item>

        <el-descriptions-item label="发生部位">
          {{ currentTrace.partName }}
        </el-descriptions-item>

        <el-descriptions-item label="问题类型">
          {{ currentTrace.problemType }}
        </el-descriptions-item>

        <el-descriptions-item label="当前流程">
          {{ workflowName(currentTrace.workflowStage) }}
        </el-descriptions-item>

        <el-descriptions-item label="附件保存位置" :span="3">
          {{ currentTrace.attachmentSavePath || '未设置' }}
        </el-descriptions-item>

        <el-descriptions-item label="问题描述" :span="3">
          {{ currentTrace.problemDescription }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 数据处理区：数字卷宗调用 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>数据处理：课题一数字卷宗传感器数据调用</span>
      </template>

      <el-row :gutter="12" align="middle">
        <el-col :span="8">
          <el-select
            v-model="selectedTraceId"
            placeholder="请选择追溯任务"
            style="width: 100%"
            @change="handleTraceChange"
          >
            <el-option
              v-for="item in traceList"
              :key="item.id"
              :label="item.traceNo + ' - ' + item.partName"
              :value="item.id"
            />
          </el-select>
        </el-col>

        <el-col :span="5">
          <el-button type="primary" icon="Connection" @click="handleImportDossier">
            调用数字卷宗数据
          </el-button>
        </el-col>

        <el-col :span="5">
          <el-button type="success" icon="FolderChecked" @click="handleSaveAttachments">
            保存附件
          </el-button>
        </el-col>
      </el-row>

      <el-alert
        class="mt15"
        title="点击“保存附件”后，将弹出保存位置设置窗口。确认后，保存位置会同步写入追溯问题表，并用于后续 Python 算法读取数据。"
        type="info"
        show-icon
        :closable="false"
      />

      <el-table :data="attachmentList" border class="mt15">
        <el-table-column prop="fileName" label="附件名称" min-width="220" />
        <el-table-column prop="sensorType" label="传感器类型" min-width="130" />
        <el-table-column prop="fileType" label="文件类型" min-width="100" />
        <el-table-column prop="fileSize" label="文件大小" min-width="100" />
        <el-table-column prop="fileSource" label="来源" min-width="160" />
        <el-table-column prop="fileUrl" label="文件路径" min-width="320" />
      </el-table>
    </el-card>

    <!-- 数据处理区：课题四协同处理 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>数据处理：课题四协同处理与结果回填</span>
          <span class="header-tip">课题四结果回填完成后，“数据处理”流程变绿</span>
        </div>
      </template>

      <el-row :gutter="12" align="middle">
        <el-col :span="4">
          <el-button type="primary" icon="Upload" @click="handlePushTopic4">
            推送给课题四
          </el-button>
        </el-col>

        <el-col :span="5">
          <el-button type="warning" icon="Promotion" @click="handleMockTopic4Callback">
            模拟课题四返回
          </el-button>
        </el-col>

        <el-col :span="5">
          <el-button type="success" icon="EditPen" @click="handleFillTopic4Result">
            回填课题四结果
          </el-button>
        </el-col>

        <el-col :span="8">
          <el-alert
            :title="topic4StatusText"
            :type="topic4AlertType"
            show-icon
            :closable="false"
          />
        </el-col>
      </el-row>

      <el-form label-width="120px" class="mt15">
        <el-form-item label="课题四结果">
          <el-input
            type="textarea"
            :rows="4"
            v-model="currentTrace.topic4Result"
            placeholder="课题四返回结果将在此显示"
            readonly
          />
        </el-form-item>

        <el-form-item label="建议原因">
          <el-input v-model="currentTrace.suggestedCause" readonly />
        </el-form-item>

        <el-form-item label="风险等级">
          <el-input v-model="currentTrace.riskLevel" readonly />
        </el-form-item>

        <el-form-item label="建议措施">
          <el-input
            type="textarea"
            :rows="3"
            v-model="currentTrace.suggestedAction"
            readonly
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 第一部分算法运行区 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>第一部分算法运行区</span>
          <span class="header-tip">算法完成后，“第一部分算法运行”流程变绿</span>
        </div>
      </template>

      <el-alert
        title="当前为第一部分算法运行区域。后续可接入 Python FastAPI 算法服务，读取已保存的附件路径并完成第一阶段追溯分析。"
        type="info"
        show-icon
        :closable="false"
      />

      <div class="mt15">
        <el-button type="warning" icon="Cpu" @click="handleRunAlgorithm">
          运行第一部分算法，当前为模拟
        </el-button>
      </div>

      <el-form label-width="120px" class="mt15">
        <el-form-item label="算法状态">
          <el-tag :type="algorithmTagType(currentTrace.algorithmStatus)">
            {{ algorithmStatusName(currentTrace.algorithmStatus) }}
          </el-tag>
        </el-form-item>

        <el-form-item label="算法结果">
          <el-input
            type="textarea"
            :rows="4"
            v-model="currentTrace.algorithmResult"
            placeholder="第一部分算法结果将在此显示"
            readonly
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 后续流程提示区 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>后续页面关联</span>
      </template>

      <el-alert
        title="本页面完成问题填报、数据处理和第一部分算法运行。后续将在第二个页面继续完成知识图谱导入、第二部分算法运行，并在第三个页面完成最终溯源。"
        type="success"
        show-icon
        :closable="false"
      />
    </el-card>

    <!-- 设置附件保存位置弹窗 -->
    <el-dialog
      title="设置附件保存位置"
      v-model="savePathOpen"
      width="680px"
      append-to-body
    >
      <el-alert
        title="请填写 Java 后端和后续 Python 算法都能访问到的文件夹路径。确认保存后，该路径会同步写入 trace_problem 表。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 15px;"
      />

      <el-form :model="savePathForm" label-width="120px">
        <el-form-item label="保存位置">
          <el-input
            v-model="savePathForm.attachmentSavePath"
            placeholder="例如 D:/topic5_data/trace_1/"
          />
        </el-form-item>

        <el-form-item label="路径示例">
          <div class="path-example">
            D:/topic5_data/trace_{{ selectedTraceId }}/<br />
            D:/追溯算法数据/trace_{{ selectedTraceId }}/<br />
            E:/python_trace_data/task_{{ selectedTraceId }}/
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="savePathOpen = false">取消</el-button>
          <el-button type="primary" @click="submitSaveAttachmentsWithPath">
            确认保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建追溯问题弹窗 -->
    <el-dialog
      title="填写新的追溯问题"
      v-model="open"
      width="720px"
      append-to-body
    >
      <el-form
        ref="traceRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="发生时间" prop="eventTime">
          <el-date-picker
            v-model="form.eventTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择发生时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="架次" prop="aircraftNo">
          <el-input v-model="form.aircraftNo" placeholder="请输入架次编号，例如 AC001" />
        </el-form-item>

        <el-form-item label="发生部位" prop="partName">
          <el-input v-model="form.partName" placeholder="请输入发生部位，例如 起落架液压作动筒" />
        </el-form-item>

        <el-form-item label="部件编号">
          <el-input v-model="form.partCode" placeholder="请输入部件编号，可选" />
        </el-form-item>

        <el-form-item label="具体位置">
          <el-input v-model="form.occurrencePosition" placeholder="请输入具体发生位置，可选" />
        </el-form-item>

        <el-form-item label="问题类型" prop="problemType">
          <el-select v-model="form.problemType" placeholder="请选择问题类型" style="width: 100%">
            <el-option label="质量异常" value="质量异常" />
            <el-option label="装配异常" value="装配异常" />
            <el-option label="传感器异常" value="传感器异常" />
            <el-option label="液压异常" value="液压异常" />
            <el-option label="结构异常" value="结构异常" />
          </el-select>
        </el-form-item>

        <el-form-item label="严重程度" prop="severityLevel">
          <el-select v-model="form.severityLevel" placeholder="请选择严重程度" style="width: 100%">
            <el-option label="一般" value="一般" />
            <el-option label="重要" value="重要" />
            <el-option label="严重" value="严重" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题描述" prop="problemDescription">
          <el-input
            type="textarea"
            :rows="4"
            v-model="form.problemDescription"
            placeholder="请输入追溯问题描述"
          />
        </el-form-item>

        <el-form-item label="填报人">
          <el-input v-model="form.reporter" placeholder="请输入填报人，可选" />
        </el-form-item>

        <el-form-item label="问题来源">
          <el-select v-model="form.source" placeholder="请选择问题来源" style="width: 100%">
            <el-option label="人工填报" value="人工填报" />
            <el-option label="系统检测" value="系统检测" />
            <el-option label="质量反馈" value="质量反馈" />
            <el-option label="课题四反馈" value="课题四反馈" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            type="textarea"
            :rows="3"
            v-model="form.remark"
            placeholder="请输入备注，可选"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      title="追溯问题详情"
      v-model="detailOpen"
      width="820px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="追溯任务编号">
          {{ detail.traceNo }}
        </el-descriptions-item>

        <el-descriptions-item label="发生时间">
          {{ detail.eventTime }}
        </el-descriptions-item>

        <el-descriptions-item label="架次">
          {{ detail.aircraftNo }}
        </el-descriptions-item>

        <el-descriptions-item label="发生部位">
          {{ detail.partName }}
        </el-descriptions-item>

        <el-descriptions-item label="问题类型">
          {{ detail.problemType }}
        </el-descriptions-item>

        <el-descriptions-item label="严重程度">
          {{ detail.severityLevel }}
        </el-descriptions-item>

        <el-descriptions-item label="当前流程">
          {{ workflowName(detail.workflowStage) }}
        </el-descriptions-item>

        <el-descriptions-item label="课题四状态">
          {{ topic4StatusName(detail.topic4Status) }}
        </el-descriptions-item>

        <el-descriptions-item label="附件保存位置" :span="2">
          {{ detail.attachmentSavePath || '未设置' }}
        </el-descriptions-item>

        <el-descriptions-item label="知识图谱重构">
          <el-button
            v-if="isGenerated(detail.kgRebuildStatus)"
            link
            type="primary"
            @click="openKnowledgeGraph(detail)"
          >
            查看知识图谱
          </el-button>
          <el-tag v-else type="info">未生成</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="溯源结果">
          <el-button
            v-if="isGenerated(detail.traceReportStatus)"
            link
            type="success"
            @click="openTraceReport(detail)"
          >
            打开Word
          </el-button>
          <el-tag v-else type="info">未生成</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="问题描述" :span="2">
          {{ detail.problemDescription }}
        </el-descriptions-item>

        <el-descriptions-item label="课题四结果" :span="2">
          {{ detail.topic4Result }}
        </el-descriptions-item>

        <el-descriptions-item label="第一部分算法结果" :span="2">
          {{ detail.algorithmResult }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import {
  listTrace,
  getTrace,
  addTrace,
  importDossierFiles,
  saveAttachmentsWithPath,
  pushTopic4,
  topic4Callback,
  fillTopic4Result,
  runAlgorithm
} from '@/api/topic5/trace'

const { proxy } = getCurrentInstance()
const router = useRouter()

const loading = ref(false)
const open = ref(false)
const detailOpen = ref(false)
const savePathOpen = ref(false)

const traceList = ref([])
const attachmentList = ref([])
const selectedTraceId = ref(null)
const currentTrace = ref({})
const detail = ref({})

const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  traceNo: null,
  aircraftNo: null,
  partName: null
})

const form = reactive({
  id: null,
  traceNo: null,
  eventTime: null,
  aircraftNo: null,
  partName: null,
  partCode: null,
  occurrencePosition: null,
  problemType: null,
  severityLevel: null,
  problemDescription: null,
  reporter: null,
  source: '人工填报',
  remark: null
})

const savePathForm = reactive({
  attachmentSavePath: ''
})

const rules = {
  eventTime: [
    { required: true, message: '发生时间不能为空', trigger: 'change' }
  ],
  aircraftNo: [
    { required: true, message: '架次不能为空', trigger: 'blur' }
  ],
  partName: [
    { required: true, message: '发生部位不能为空', trigger: 'blur' }
  ],
  problemType: [
    { required: true, message: '问题类型不能为空', trigger: 'change' }
  ],
  severityLevel: [
    { required: true, message: '严重程度不能为空', trigger: 'change' }
  ],
  problemDescription: [
    { required: true, message: '问题描述不能为空', trigger: 'blur' }
  ]
}

// 新流程：1 问题填报，2 数据处理，3 第一部分算法运行，4 知识图谱导入，5 第二部分算法运行，6 进行溯源
const activeStep = computed(() => {
  return Number(currentTrace.value.workflowStage || 0)
})
const algorithmResultObj = computed(() => {
  return parseAlgorithmResult(currentTrace.value.algorithmResult)
})

const topComponentDiagnostics = computed(() => {
  const list = algorithmResultObj.value?.componentDiagnostics || []

  if (!Array.isArray(list)) {
    return []
  }

  return list.slice(0, 6)
})

const componentStatusList = computed(() => {
  const status = algorithmResultObj.value?.componentStatus || {}
  const severity = algorithmResultObj.value?.severityScores || {}

  const nameMap = {
    cooler: '冷却器',
    valve: '阀芯/方向阀',
    pump: '液压泵',
    accumulator: '蓄能器',
    stability: '系统稳定性'
  }

  return Object.keys(status).map(key => {
    return {
      key: key,
      component: nameMap[key] || key,
      status: status[key],
      severity: severity[key]
    }
  })
})
const topic4StatusText = computed(() => {
  return '课题四状态：' + topic4StatusName(currentTrace.value.topic4Status)
})

const topic4AlertType = computed(() => {
  const status = Number(currentTrace.value.topic4Status)
  if (status === 0) return 'info'
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  if (status === 3) return 'error'
  return 'info'
})

// 查询列表
function getList() {
  loading.value = true
  listTrace(queryParams).then(res => {
    traceList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询按钮
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
function resetQuery() {
  queryParams.traceNo = null
  queryParams.aircraftNo = null
  queryParams.partName = null
  queryParams.pageNum = 1
  getList()
}

// 重置表单
function resetFormData() {
  form.id = null
  form.traceNo = null
  form.eventTime = null
  form.aircraftNo = null
  form.partName = null
  form.partCode = null
  form.occurrencePosition = null
  form.problemType = null
  form.severityLevel = null
  form.problemDescription = null
  form.reporter = null
  form.source = '人工填报'
  form.remark = null
}

// 打开新增弹窗
function handleAdd() {
  resetFormData()
  open.value = true
}
function parseAlgorithmResult(value) {
  if (!value) {
    return null
  }

  if (typeof value === 'object') {
    return value
  }

  try {
    const obj = JSON.parse(value)

    if (obj && obj.displayType === 'PYTHON_AAGCN_RCA_V1') {
      return obj
    }

    return null
  } catch (e) {
    return null
  }
}

function formatConfidence(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  const num = Number(value)

  if (Number.isNaN(num)) {
    return value
  }

  if (num <= 1) {
    return (num * 100).toFixed(2) + '%'
  }

  return num.toFixed(2)
}

function formatNumber(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  const num = Number(value)

  if (Number.isNaN(num)) {
    return value
  }

  return num.toFixed(4)
}

function formatEvolutionChain(chain) {
  if (!chain) {
    return '-'
  }

  if (Array.isArray(chain)) {
    return chain.join(' → ')
  }

  return chain
}

function faultLevelTagType(row) {
  if (!row) {
    return 'info'
  }

  if (row.is_fault === true) {
    return 'danger'
  }

  const severity = Number(row.severity || 0)

  if (severity >= 0.3) {
    return 'warning'
  }

  return 'success'
}
// 提交新增追溯问题
function submitForm() {
  proxy.$refs.traceRef.validate(valid => {
    if (!valid) return

    addTrace({ ...form }).then(() => {
      proxy.$modal.msgSuccess('新增追溯问题成功')
      open.value = false
      getList()
    })
  })
}

// 点击表格行
function handleRowClick(row) {
  selectTrace(row)
}

// 选择追溯任务
function selectTrace(row) {
  selectedTraceId.value = row.id
  getTrace(row.id).then(res => {
    currentTrace.value = res.data || {}
  })
}

// 下拉框切换追溯任务
function handleTraceChange(id) {
  if (!id) return

  getTrace(id).then(res => {
    currentTrace.value = res.data || {}
  })
}

// 查看详情
function viewTrace(row) {
  getTrace(row.id).then(res => {
    detail.value = res.data || {}
    detailOpen.value = true
  })
}

// 调用课题一数字卷宗数据
function handleImportDossier() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  importDossierFiles(selectedTraceId.value).then(res => {
    attachmentList.value = res.data || []
    proxy.$modal.msgSuccess('已模拟从课题一数字卷宗调用传感器数据')
    refreshCurrentTrace()
  })
}

// 点击保存附件：先弹出保存位置窗口
function handleSaveAttachments() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (!attachmentList.value || attachmentList.value.length === 0) {
    proxy.$modal.msgWarning('请先调用数字卷宗数据，再保存附件')
    return
  }

  savePathForm.attachmentSavePath = currentTrace.value.attachmentSavePath
    || `D:/topic5_data/trace_${selectedTraceId.value}/`

  savePathOpen.value = true
}

// 确认保存附件并同步保存位置
function submitSaveAttachmentsWithPath() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (!savePathForm.attachmentSavePath || savePathForm.attachmentSavePath.trim() === '') {
    proxy.$modal.msgWarning('请填写附件保存位置')
    return
  }

  saveAttachmentsWithPath(selectedTraceId.value, {
    attachmentSavePath: savePathForm.attachmentSavePath
  }).then(() => {
    proxy.$modal.msgSuccess('附件保存成功，保存位置已同步到追溯问题表')
    savePathOpen.value = false

    const basePath = normalizePath(savePathForm.attachmentSavePath)

    attachmentList.value = attachmentList.value.map(item => {
      return {
        ...item,
        fileUrl: basePath + item.fileName,
        fileSource: '用户指定保存位置',
        savedFlag: 1
      }
    })

    refreshCurrentTrace()
  })
}

// 推送课题四
function handlePushTopic4() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  pushTopic4(selectedTraceId.value).then(() => {
    proxy.$modal.msgSuccess('已推送给课题四处理')
    refreshCurrentTrace()
  })
}

// 模拟课题四返回结果
function handleMockTopic4Callback() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  const data = {
    traceId: selectedTraceId.value,
    topic4Status: 2,
    topic4Result: '课题四识别到该异常与液压压力波动、装配间隙异常以及密封状态退化有关。',
    suggestedCause: '液压管路压力异常',
    riskLevel: '较高',
    suggestedAction: '建议检查作动筒密封状态、液压管路连接状态和相关传感器采集数据。'
  }

  topic4Callback(data).then(() => {
    proxy.$modal.msgSuccess('已模拟接收课题四返回结果')
    refreshCurrentTrace()
  })
}

// 回填课题四结果
function handleFillTopic4Result() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  fillTopic4Result(selectedTraceId.value).then(() => {
    proxy.$modal.msgSuccess('课题四结果已回填，数据处理阶段完成')
    refreshCurrentTrace()
  })
}

// 模拟运行第一部分算法
function handleRunAlgorithm() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  runAlgorithm(selectedTraceId.value).then(() => {
    proxy.$modal.msgSuccess('第一部分算法运行完成')
    refreshCurrentTrace()
  })
}

// 刷新当前任务和列表
function refreshCurrentTrace() {
  if (!selectedTraceId.value) return

  getTrace(selectedTraceId.value).then(res => {
    currentTrace.value = res.data || {}
    getList()
  })
}

// 判断知识图谱或报告是否已经生成
function isGenerated(value) {
  return Number(value) === 1
}

// 打开第二部分页面生成的知识图谱
function openKnowledgeGraph(row) {
  if (!row || !row.id) {
    proxy.$modal.msgWarning('未获取到追溯任务信息')
    return
  }

  if (row.kgGraphUrl) {
    const url = buildFileUrl(row.kgGraphUrl)
    window.open(url, '_blank')
    return
  }

  router.push({
    path: '/topic5/knowledgeGraph',
    query: {
      traceId: row.id
    }
  })
}

// 打开第三部分最终生成的 Word 溯源报告
function openTraceReport(row) {
  if (!row || !row.id) {
    proxy.$modal.msgWarning('未获取到追溯任务信息')
    return
  }

  if (!row.traceReportUrl) {
    proxy.$modal.msgWarning('当前追溯任务尚未生成溯源报告')
    return
  }

  const url = buildFileUrl(row.traceReportUrl)
  window.open(url, '_blank')
}

// 兼容 /profile/... 和 http... 地址
function buildFileUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url

  const baseApi = import.meta.env.VITE_APP_BASE_API || ''
  return baseApi + url
}

// 路径规范化
function normalizePath(path) {
  if (!path) return ''
  let result = path.replace(/\\/g, '/')
  if (!result.endsWith('/')) {
    result += '/'
  }
  return result
}

// 任务状态颜色
function traceStatusTagType(status) {
  if (status === '未处理') return 'info'
  if (status === '处理中') return 'warning'
  if (status === '第一部分算法完成') return 'success'
  if (status === '已完成') return 'success'
  return 'info'
}

// 课题四状态文字
function topic4StatusName(status) {
  const value = Number(status)
  if (value === 0) return '未推送'
  if (value === 1) return '正在处理'
  if (value === 2) return '已处理完成'
  if (value === 3) return '处理失败'
  return '未选择追溯任务'
}

// 算法状态文字
function algorithmStatusName(status) {
  const value = Number(status)
  if (value === 0) return '未运行'
  if (value === 1) return '运行中'
  if (value === 2) return '已完成'
  if (value === 3) return '运行失败'
  return '未运行'
}

// 算法状态颜色
function algorithmTagType(status) {
  const value = Number(status)
  if (value === 0) return 'info'
  if (value === 1) return 'warning'
  if (value === 2) return 'success'
  if (value === 3) return 'danger'
  return 'info'
}

// 新流程阶段名称
function workflowName(stage) {
  const value = Number(stage)
  if (value === 1) return '问题填报'
  if (value === 2) return '数据处理'
  if (value === 3) return '第一部分算法运行'
  if (value === 4) return '知识图谱导入'
  if (value === 5) return '第二部分算法运行'
  if (value === 6) return '进行溯源'
  return '未开始'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.topic5-trace-page {
  padding-bottom: 24px;
}

.mt15 {
  margin-top: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-tip {
  font-size: 13px;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}

.path-example {
  line-height: 24px;
  color: #606266;
  font-size: 13px;
}
.algorithm-result-panel {
  margin-top: 15px;
}

.summary-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
  padding: 14px 16px;
  min-height: 76px;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}

.sub-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
</style>
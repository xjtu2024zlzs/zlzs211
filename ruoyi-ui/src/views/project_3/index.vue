<template>
  <div class="app-container quality-page">
    <el-card shadow="never" class="quality-hero">
      <template #header>
        <span class="quality-title">生命周期质量监管与故障预防系统</span>
      </template>

      <div class="quality-desc">
        面向设计、制造、服役全过程的质量数据管理、异常监管、故障识别预测和周期性巡检。
      </div>

    </el-card>

    <!-- 当前质量问题显示区域 -->
    <el-card
      class="quality-mb16 current-quality-card"
      shadow="hover"
      v-loading="qualityTaskLoading"
    >
      <template #header>
        <div class="current-quality-header">
          <div class="current-quality-header-left">
            <div class="current-quality-title">当前质量问题</div>
            <div class="current-quality-subtitle">
              接收质量问题管理中心最新分派至{{ MODULE_NAME }}的处理任务
            </div>
          </div>

          <el-button
            size="small"
            type="primary"
            plain
            class="refresh-task-button"
            @click="loadCurrentQualityTask"
          >
            刷新任务
          </el-button>
        </div>
      </template>

      <el-empty
        v-if="!currentQualityTask"
        description="暂无质量问题管理中心分派给本模块的处理中任务"
        :image-size="90"
      />

      <template v-else>
        <el-descriptions :column="2" border class="quality-descriptions">
          <el-descriptions-item label="问题编号">
            {{ currentQualityTask.problemCode || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="问题标题">
            {{ currentQualityTask.problemTitle || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="产品型号">
            {{ currentQualityTask.productModel || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="涉及系统">
            {{ currentQualityTask.involvedSystem || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="发生部件">
            {{ currentQualityTask.occurPart || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="部件编号">
            {{ currentQualityTask.componentCode || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="分派时间">
            {{ currentQualityTask.dispatchTime || currentQualityTask.createTime || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="分派说明">
            {{ currentQualityTask.dispatchOpinion || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="问题描述" :span="2">
            <div class="description-in-table">
              {{ currentQualityTask.description || '暂无问题描述' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div
          v-if="currentQualityTask.lifecycleReportFile"
          class="lifecycle-report-box"
        >
          <div class="lifecycle-report-header">
            <div>
              <div class="lifecycle-report-title">
                全生命周期数字质量自反馈与追溯报告
              </div>
            </div>

            <el-tag type="success">
              已关联
            </el-tag>
          </div>

          <div class="lifecycle-report-info">
            <span>报告文件：</span>
            <strong>{{ getLifecycleReportFileName(currentQualityTask.lifecycleReportFile) }}</strong>
          </div>

          <div
            v-if="currentQualityTask.lifecycleReportSubmitTime"
            class="lifecycle-report-info"
          >
            <span>返回时间：</span>
            <strong>{{ currentQualityTask.lifecycleReportSubmitTime }}</strong>
          </div>

          <div class="lifecycle-report-actions">
            <el-button type="primary" plain @click="previewLifecycleReport">
              预览报告
            </el-button>

            <el-button type="success" plain @click="downloadLifecycleReport">
              下载报告
            </el-button>

            <el-button plain @click="openLifecycleReportInNewWindow">
              新窗口打开
            </el-button>
          </div>
        </div>

        <div class="current-quality-actions">
          <el-button
            type="success"
            :loading="finishQualityTaskLoading"
            :disabled="!currentQualityTask"
            @click="finishCurrentQualityTask"
          >
            完成任务并回填结果
          </el-button>
        </div>

        <div v-if="currentQualityTaskResult" class="quality-result-panel">
          <div class="quality-result-title">最近一次算法结果</div>
          <div class="quality-result-grid">
            <div
              v-for="section in qualityResultSections"
              :key="section.key"
              class="quality-result-card"
            >
              <div class="quality-result-card-title">{{ section.label }}</div>

              <div v-if="section.empty" class="quality-result-empty">
                暂无最近一次成功计算结果
              </div>

              <template v-else>
                <div class="quality-result-fields">
                  <div
                    v-for="field in section.fields"
                    :key="field.label"
                    class="quality-result-field"
                  >
                    <span class="quality-result-label">{{ field.label }}</span>
                    <span class="quality-result-value">{{ field.value }}</span>
                  </div>
                </div>

                <div
                  v-for="list in section.lists"
                  :key="list.label"
                  class="quality-result-list"
                >
                  <div class="quality-result-list-title">{{ list.label }}</div>
                  <div
                    v-for="(item, index) in list.items"
                    :key="index"
                    class="quality-result-list-item"
                  >
                    <span class="quality-result-rank">{{ index + 1 }}</span>
                    <span>{{ item }}</span>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </template>
    </el-card>

    <el-row :gutter="16" class="quality-mb16">
      <el-col :span="4">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.aircraft_count) }}</div>
          <div class="quality-stat-label">飞机数量</div>
        </el-card>
      </el-col>

      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.subsystem_count) }}</div>
          <div class="quality-stat-label">分系统数量</div>
        </el-card>
      </el-col>

      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.device_count) }}</div>
          <div class="quality-stat-label">设备数量</div>
        </el-card>
      </el-col>

      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.component_count) }}</div>
          <div class="quality-stat-label">组件数量</div>
        </el-card>
      </el-col>

      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.part_count) }}</div>
          <div class="quality-stat-label">零件数量</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="go('/project_3/lifecycle-monitor')">
          <div class="module-title">生命周期质量统一监测</div>
          <div class="module-desc">
            进入模块级联管理、零件/组件信息管理、数据导入、数据预处理、关键质量特性挖掘页面。
          </div>
          <el-button type="primary" link>进入模块</el-button>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="go('/project_3/feedback/warning')">
          <div class="module-title">全域制造过程反馈监管</div>
          <div class="module-desc">
            进入关键工序异常信号预警、设备异常预警、制造过程预防性检测页面。
          </div>
          <el-button type="primary" link>进入模块</el-button>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="go('/project_3/service/identify')">
          <div class="module-title">服役性能周期故障预防</div>
          <div class="module-desc">
            进入故障识别、故障预防页面。
          </div>
          <el-button type="primary" link>进入模块</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog
      :title="lifecycleReportPreviewTitle"
      v-model="lifecycleReportPreviewOpen"
      width="90%"
      append-to-body
      destroy-on-close
      @closed="clearLifecycleReportPreview"
    >
      <div class="word-preview-wrapper">
        <VueOfficeDocx
          v-if="lifecycleReportPreviewUrl"
          :src="lifecycleReportPreviewUrl"
          style="height: 100%;"
        />
      </div>

      <template #footer>
        <el-button @click="lifecycleReportPreviewOpen = false">
          关闭
        </el-button>

        <el-button type="primary" @click="openLifecycleReportInNewWindow">
          新窗口打开
        </el-button>

        <el-button type="success" @click="downloadLifecycleReport">
          下载报告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import '@/views/project_3/common.css'
import { reactive, ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import VueOfficeDocx from '@vue-office/docx'
import '@vue-office/docx/lib/index.css'

import request from '@/utils/request'
import { getHomeOverview } from '@/api/project_3/home'
import { listKqcMiningResults, listWarningDetectResults } from '@/api/project_3/feedback'
import { listFaultIdentifyResults, listKeyProcessResults } from '@/api/project_3/service'
import { listTask, updateTask } from '@/api/quality/task'
import { getProblem, updateProblem } from '@/api/quality/problem'
import { addLog } from '@/api/quality/log'


defineOptions({
  name: 'Home'
})

const router = useRouter()

const summary = reactive({
  aircraft_count: null,
  subsystem_count: null,
  device_count: null,
  component_count: null,
  part_count: null
})

const currentQualityTask = ref(null)
const qualityTaskList = ref([])
const qualityTaskLoading = ref(false)
const finishQualityTaskLoading = ref(false)
const currentQualityTaskResult = ref(null)

const lifecycleReportPreviewOpen = ref(false)
const lifecycleReportPreviewUrl = ref('')
const lifecycleReportPreviewTitle = ref('全生命周期数字质量自反馈与追溯模块报告预览')

const MODULE_CODE = 'PROJECT_3'
const MODULE_NAME = '复杂产品质量监管与故障预防'

const RELATED_REPORT_MODULE_CODE = 'PROJECT_5'
const RELATED_REPORT_MODULE_NAME = '全生命周期数字质量自反馈与追溯模块'

const QMS_TASK_EVENT_NAME = 'qms-current-task-change'
const QMS_TASK_EVENT_KEY = 'qms_current_task_change'

const QMS_FLOW_EVENT_NAME = 'qms-flow-change'
const QMS_FLOW_EVENT_KEY = 'qms_flow_change'

const qualityResultSections = computed(() => {
  const result = currentQualityTaskResult.value
  if (!result) {
    return []
  }

  return [
    buildResultSection('keyQualityCharacteristics', '关键质量特性挖掘结果', result.keyQualityCharacteristics, [
      ['关键质量特性', 'targetKqc'],
      ['识别数量', 'selectedFeatureCount'],
      ['影响特性数量', 'influenceCount'],
      ['评分', 'score'],
      ['结果摘要', 'summary']
    ], [
      ['主要影响特性', 'topInfluences', formatTopInfluence]
    ]),
    buildResultSection('keyProcess', '关键工序识别结果', result.keyProcess, [
      ['关键工序编号', 'keyProcessCode'],
      ['关键工序名称', 'keyProcessName'],
      ['置信度', 'confidence'],
      ['评分', 'score'],
      ['识别依据', 'reason'],
      ['处理建议', 'suggestion'],
      ['结果摘要', 'summary']
    ], [
      ['候选关键工序', 'candidateProcesses', formatCandidateProcess]
    ]),
    buildResultSection('processAnomaly', '工序异常检测结果', result.processAnomaly, [
      ['是否异常', 'isAbnormal', formatBoolean],
      ['异常等级', 'abnormalLevel'],
      ['异常分数', 'abnormalScore'],
      ['异常时间', 'abnormalTime'],
      ['处理建议', 'suggestion'],
      ['结果摘要', 'summary']
    ]),
    buildResultSection('earlyDegradationPoint', '早期识别点结果', result.earlyDegradationPoint, [
      ['识别到的退化点', 'degradationPoint'],
      ['单位', 'unit'],
      ['关联特征', 'featureName'],
      ['增长率', 'increaseRate'],
      ['结果摘要', 'summary']
    ]),
    buildResultSection('faultPrediction', '故障预测结果', result.faultPrediction, [
      ['风险等级', 'riskLevel'],
      ['风险分数', 'riskScore'],
      ['预测剩余寿命', 'predictedRemainingLife'],
      ['剩余寿命', 'remainingLife'],
      ['寿命单位', 'rulUnit'],
      ['维修建议', 'maintenanceAdvice', formatAdvice],
      ['结果摘要', 'summary']
    ])
  ]
})

onMounted(() => {
  getOverview()
  loadCurrentQualityTask()

  window.addEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.addEventListener('storage', handleStorageChange)
  window.addEventListener('focus', loadCurrentQualityTask)
})

onBeforeUnmount(() => {
  window.removeEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.removeEventListener('storage', handleStorageChange)
  window.removeEventListener('focus', loadCurrentQualityTask)

  clearLifecycleReportPreview()
})

function go(path) {
  router.push(path)
}

function displayValue(value) {
  return value === null || value === undefined ? '暂无数据' : value
}

function buildResultSection(key, label, result, fieldDefs, listDefs = []) {
  const source = result && typeof result === 'object' ? result : {}
  const fields = fieldDefs
    .map(([fieldLabel, prop, formatter]) => {
      const rawValue = source[prop]
      const value = formatter ? formatter(rawValue) : formatResultValue(rawValue)
      return value ? { label: fieldLabel, value } : null
    })
    .filter(Boolean)

  const lists = listDefs
    .map(([listLabel, prop, formatter]) => {
      const rows = Array.isArray(source[prop]) ? source[prop] : []
      const items = rows
        .map(item => (formatter ? formatter(item) : formatResultValue(item)))
        .filter(Boolean)
      return items.length ? { label: listLabel, items } : null
    })
    .filter(Boolean)

  return {
    key,
    label,
    fields,
    lists,
    empty: fields.length === 0 && lists.length === 0
  }
}

function formatResultValue(value) {
  if (value === undefined || value === null || value === '') return ''
  if (Array.isArray(value)) {
    return value.map(item => formatResultValue(item)).filter(Boolean).join('；')
  }
  if (typeof value === 'object') {
    return Object.entries(value)
      .map(([, item]) => formatResultValue(item))
      .filter(Boolean)
      .join('，')
  }
  return String(value)
}

function formatBoolean(value) {
  if (value === undefined || value === null || value === '') return ''
  if (value === true || value === 'true' || value === 'TRUE' || value === 1 || value === '1') return '是'
  if (value === false || value === 'false' || value === 'FALSE' || value === 0 || value === '0') return '否'
  return String(value)
}

function formatAdvice(value) {
  if (Array.isArray(value)) {
    return value.map(item => formatResultValue(item)).filter(Boolean).join('；')
  }
  return formatResultValue(value)
}

function formatTopInfluence(item) {
  if (!item || typeof item !== 'object') return formatResultValue(item)
  const parts = []
  if (item.feature) parts.push(item.feature)
  if (item.weight !== undefined) parts.push(`权重 ${item.weight}`)
  if (item.frequency !== undefined) parts.push(`频次 ${item.frequency}`)
  if (item.score !== undefined) parts.push(`评分 ${item.score}`)
  return parts.join('，')
}

function formatCandidateProcess(item) {
  if (!item || typeof item !== 'object') return formatResultValue(item)
  const title = item.processName || item.processCode || '候选工序'
  const parts = [title]
  if (item.processCode && item.processName) parts.push(`编号 ${item.processCode}`)
  if (item.score !== undefined) parts.push(`评分 ${item.score}`)
  if (item.confidence !== undefined) parts.push(`置信度 ${item.confidence}`)
  return parts.join('，')
}

function getOverview() {
  getHomeOverview()
    .then((res) => {
      const data = res.data || {}
      const overview = data.summary || {}

      summary.aircraft_count = overview.aircraft_count
      summary.subsystem_count = overview.subsystem_count
      summary.device_count = overview.device_count
      summary.component_count = overview.component_count
      summary.part_count = overview.part_count
    })
    .catch(() => {
      summary.aircraft_count = null
      summary.subsystem_count = null
      summary.device_count = null
      summary.component_count = null
      summary.part_count = null
    })
}

function getNowTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')

  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

function getTimeValue(row) {
  const time =
    row.dispatchTime ||
    row.createTime ||
    row.submitTime ||
    row.updateTime ||
    row.confirmTime

  return time ? new Date(time).getTime() : 0
}

function getLatestRow(rows) {
  if (!Array.isArray(rows) || rows.length === 0) {
    return null
  }

  return rows
    .slice()
    .sort((a, b) => {
      const timeCompare = getTimeValue(b) - getTimeValue(a)

      if (timeCompare !== 0) {
        return timeCompare
      }

      return Number(b.taskId || 0) - Number(a.taskId || 0)
    })[0]
}

function normalizeQualityTask(item) {
  return {
    ...item,
    taskId: item.taskId,
    problemId: item.problemId,
    problemCode: item.problemCode || '',
    moduleCode: item.moduleCode || MODULE_CODE,
    moduleName: item.moduleName || MODULE_NAME,
    taskStatus: item.taskStatus || '',
    dispatchOpinion: item.dispatchOpinion || '',
    processResult: item.processResult || '',
    processFile: item.processFile || '',
    dispatchTime: item.dispatchTime || item.createTime || '',
    createTime: item.createTime || '',
    submitTime: item.submitTime || '',

    problemTitle: '',
    occurTime: '',
    productModel: '',
    involvedSystem: '',
    occurPart: '',
    componentCode: '',
    severity: '',
    source: '',
    reporter: '',
    description: '',
    influenceScope: '',

    lifecycleReportFile: '',
    lifecycleReportResult: '',
    lifecycleReportSubmitTime: '',
    lifecycleReportTaskId: ''
  }
}

function normalizeLifecycleReportFilePath(filePath) {
  if (!filePath) return ''

  let path = filePath.replace(/\\/g, '/')

  if (path.startsWith('/topic5/profile/')) {
    path = path.replace('/topic5/profile/', '/profile/')
  }

  if (path.startsWith('/project5/profile/')) {
    path = path.replace('/project5/profile/', '/profile/')
  }

  return path
}

function isWordFile(url) {
  if (!url) return false

  const lower = url.toLowerCase()
  return lower.endsWith('.doc') || lower.endsWith('.docx')
}

function getLifecycleReportFileName(filePath) {
  if (!filePath) {
    return ''
  }

  const normalizedPath = normalizeLifecycleReportFilePath(filePath)
  const fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1)

  return fileName || ''
}

async function loadLatestLifecycleReportByProblemId(problemId) {
  if (!problemId) {
    return null
  }

  try {
    const res = await listTask({
      problemId,
      moduleCode: RELATED_REPORT_MODULE_CODE
    })

    const rows = Array.isArray(res?.rows)
      ? res.rows
      : Array.isArray(res?.data)
        ? res.data
        : []

    const reportTasks = rows.filter((item) => {
      const file = item.processFile || ''

      if (!file) {
        return false
      }

      const lowerFile = file.toLowerCase()

      if (!lowerFile.endsWith('.doc') && !lowerFile.endsWith('.docx')) {
        return false
      }

      return ['SUBMITTED', 'CONFIRMED'].includes(item.taskStatus)
    })

    return getLatestRow(reportTasks)
  } catch (error) {
    console.error(`查询${RELATED_REPORT_MODULE_NAME}最新运行报告失败：`, error)
    return null
  }
}

async function loadQualityProblemInfo(task) {
  if (!task || !task.problemId) {
    return task
  }

  try {
    const [problemRes, lifecycleReportTask] = await Promise.all([
      getProblem(task.problemId),
      loadLatestLifecycleReportByProblemId(task.problemId)
    ])

    const problem = problemRes?.data || {}

    return {
      ...task,
      problemTitle: problem.problemTitle || problem.title || '',
      occurTime: problem.occurTime || '',
      productModel: problem.productModel || '',
      involvedSystem: problem.involvedSystem || '',
      occurPart: problem.occurPart || '',
      componentCode: problem.componentCode || '',
      severity: problem.severity || '',
      source: problem.source || '',
      reporter: problem.reporter || '',
      description: problem.description || '',
      influenceScope: problem.influenceScope || '',

      lifecycleReportFile: lifecycleReportTask?.processFile || '',
      lifecycleReportResult: lifecycleReportTask?.processResult || '',
      lifecycleReportSubmitTime:
        lifecycleReportTask?.submitTime ||
        lifecycleReportTask?.updateTime ||
        lifecycleReportTask?.confirmTime ||
        lifecycleReportTask?.dispatchTime ||
        lifecycleReportTask?.createTime ||
        '',
      lifecycleReportTaskId: lifecycleReportTask?.taskId || ''
    }
  } catch (error) {
    console.error('加载质量问题填报信息和关联报告失败：', error)
    return task
  }
}

async function fetchLifecycleReportBlob(filePath) {
  if (!filePath) {
    throw new Error(`${RELATED_REPORT_MODULE_NAME}报告文件为空`)
  }

  if (!isWordFile(filePath)) {
    throw new Error(`${RELATED_REPORT_MODULE_NAME}报告不是Word文档`)
  }

  const normalizedPath = normalizeLifecycleReportFilePath(filePath)

  const data = await request({
    url: '/topic5/trace/report/downloadByPath',
    method: 'get',
    params: {
      filePath: normalizedPath
    },
    responseType: 'blob'
  })

  if (data instanceof Blob) {
    return data
  }

  return new Blob([data], {
    type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  })
}

async function previewLifecycleReport() {
  if (!currentQualityTask.value || !currentQualityTask.value.lifecycleReportFile) {
    ElMessage.warning(`当前质量问题暂无${RELATED_REPORT_MODULE_NAME}报告`)
    return
  }

  try {
    const blob = await fetchLifecycleReportBlob(currentQualityTask.value.lifecycleReportFile)
    const objectUrl = window.URL.createObjectURL(blob)

    if (lifecycleReportPreviewUrl.value && lifecycleReportPreviewUrl.value.startsWith('blob:')) {
      window.URL.revokeObjectURL(lifecycleReportPreviewUrl.value)
    }

    lifecycleReportPreviewUrl.value = objectUrl
    lifecycleReportPreviewTitle.value =
      `${currentQualityTask.value.problemCode || ''} ${RELATED_REPORT_MODULE_NAME}报告预览`
    lifecycleReportPreviewOpen.value = true
  } catch (error) {
    console.error(`${RELATED_REPORT_MODULE_NAME}报告预览失败：`, error)
    ElMessage.error(error.message || `${RELATED_REPORT_MODULE_NAME}报告预览失败`)
  }
}

async function downloadLifecycleReport() {
  if (!currentQualityTask.value || !currentQualityTask.value.lifecycleReportFile) {
    ElMessage.warning(`当前质量问题暂无${RELATED_REPORT_MODULE_NAME}报告`)
    return
  }

  try {
    const filePath = currentQualityTask.value.lifecycleReportFile
    const blob = await fetchLifecycleReportBlob(filePath)

    const fileName = getLifecycleReportFileName(filePath)
    const objectUrl = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = objectUrl
    link.download = fileName
    link.style.display = 'none'

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    window.URL.revokeObjectURL(objectUrl)
  } catch (error) {
    console.error(`${RELATED_REPORT_MODULE_NAME}报告下载失败：`, error)
    ElMessage.error(error.message || `${RELATED_REPORT_MODULE_NAME}报告下载失败`)
  }
}

async function openLifecycleReportInNewWindow() {
  if (!currentQualityTask.value || !currentQualityTask.value.lifecycleReportFile) {
    ElMessage.warning(`当前质量问题暂无${RELATED_REPORT_MODULE_NAME}报告`)
    return
  }

  try {
    const blob = await fetchLifecycleReportBlob(currentQualityTask.value.lifecycleReportFile)
    const objectUrl = window.URL.createObjectURL(blob)

    window.open(objectUrl, '_blank')

    setTimeout(() => {
      window.URL.revokeObjectURL(objectUrl)
    }, 60000)
  } catch (error) {
    console.error(`${RELATED_REPORT_MODULE_NAME}报告打开失败：`, error)
    ElMessage.error(error.message || `${RELATED_REPORT_MODULE_NAME}报告打开失败`)
  }
}

function clearLifecycleReportPreview() {
  if (lifecycleReportPreviewUrl.value && lifecycleReportPreviewUrl.value.startsWith('blob:')) {
    window.URL.revokeObjectURL(lifecycleReportPreviewUrl.value)
  }

  lifecycleReportPreviewUrl.value = ''
}


function getQualityTaskStatusText(status) {
  const map = {
    PROCESSING: '处理中',
    SUBMITTED: '待确认',
    CONFIRMED: '已确认'
  }

  return map[status] || status || '-'
}

function getResponsePayload(response) {
  return response?.data || response || {}
}

function getPageRows(response) {
  const payload = getResponsePayload(response)
  const rows = payload?.rows || payload?.data?.rows || []
  return Array.isArray(rows) ? rows : []
}

function latestRow(rows) {
  return Array.isArray(rows) && rows.length > 0 ? rows[0] : null
}

function rowResult(row) {
  if (!row || typeof row !== 'object') return {}
  const result = row.result && typeof row.result === 'object' ? row.result : {}
  return result.result && typeof result.result === 'object' ? result.result : result
}

function pickValue(source, names) {
  if (!source || typeof source !== 'object') return undefined
  for (const name of names) {
    const value = source[name]
    if (value !== undefined && value !== null && value !== '') return value
  }
  const containers = [source.result, source.data, source.summary, source.metrics, source.prediction, source.degradation]
  for (const item of containers) {
    const value = pickValue(item, names)
    if (value !== undefined && value !== null && value !== '') return value
  }
  return undefined
}

function setPicked(target, source, targetKey, sourceKeys, normalize) {
  const value = pickValue(source, sourceKeys)
  if (value === undefined || value === null || value === '') return
  const nextValue = normalize ? normalize(value) : value
  if (nextValue === undefined || nextValue === null || nextValue === '') return
  if (Array.isArray(nextValue) && nextValue.length === 0) return
  target[targetKey] = nextValue
}

function compactObject(source) {
  return Object.fromEntries(
    Object.entries(source || {}).filter(([, value]) => {
      if (value === undefined || value === null || value === '') return false
      if (Array.isArray(value)) return value.length > 0
      if (typeof value === 'object') return Object.keys(value).length > 0
      return true
    })
  )
}

function normalizeTopInfluences(value) {
  if (!Array.isArray(value)) return undefined
  return value.slice(0, 20).map(item => compactObject({
    feature: item?.feature || item?.name || item?.featureName || item?.feature_name || item?.source_variable,
    weight: item?.weight,
    frequency: item?.frequency,
    score: item?.score
  })).filter(item => Object.keys(item).length)
}

function normalizeCandidateProcesses(value) {
  if (!Array.isArray(value)) return undefined
  return value.slice(0, 20).map(item => compactObject({
    rank: item?.rank,
    processCode: item?.processCode || item?.process_code || item?.code,
    processName: item?.processName || item?.process_name || item?.name,
    score: item?.score,
    confidence: item?.confidence
  })).filter(item => Object.keys(item).length)
}

function buildKqcResult(row) {
  const source = { ...rowResult(row), ...(row || {}) }
  const result = {}
  setPicked(result, source, 'targetKqc', ['targetKqc', 'target_kqc', 'topFeature', 'top_feature', 'featureName', 'feature_name'])
  setPicked(result, source, 'selectedFeatureCount', ['selectedFeatureCount', 'selected_feature_count', 'kqcCount', 'kqc_count'])
  setPicked(result, source, 'influenceCount', ['influenceCount', 'influence_count'])
  setPicked(result, source, 'topInfluences', ['topInfluences', 'top_influences', 'candidateKqcs', 'candidate_kqcs'], normalizeTopInfluences)
  setPicked(result, source, 'score', ['cvScore', 'cv_score', 'score'])
  setPicked(result, source, 'summary', ['summary', 'message'])
  return compactObject(result)
}

function buildKeyProcessResult(row) {
  const source = { ...rowResult(row), ...(row || {}) }
  const result = {}
  setPicked(result, source, 'keyProcessCode', ['keyProcessCode', 'key_process_code', 'processCode', 'process_code', 'code'])
  setPicked(result, source, 'keyProcessName', ['keyProcessName', 'key_process_name', 'processName', 'process_name', 'name'])
  setPicked(result, source, 'confidence', ['confidence'])
  setPicked(result, source, 'score', ['score'])
  setPicked(result, source, 'reason', ['reason'])
  setPicked(result, source, 'suggestion', ['suggestion'])
  setPicked(result, source, 'candidateProcesses', ['candidateProcesses', 'candidate_processes'], normalizeCandidateProcesses)
  setPicked(result, source, 'summary', ['summary', 'message'])
  return compactObject(result)
}

function buildProcessAnomalyResult(row) {
  const source = { ...rowResult(row), ...(row || {}) }
  const result = {}
  setPicked(result, source, 'isAbnormal', ['isAbnormal', 'is_abnormal', 'abnormal'])
  setPicked(result, source, 'abnormalLevel', ['abnormalLevel', 'abnormal_level', 'level'])
  setPicked(result, source, 'abnormalScore', ['abnormalScore', 'abnormal_score', 'score'])
  setPicked(result, source, 'abnormalTime', ['abnormalTime', 'abnormal_time', 'timePoint', 'time_point'])
  setPicked(result, source, 'suggestion', ['suggestion'])
  setPicked(result, source, 'summary', ['summary', 'message'])
  return compactObject(result)
}

function buildEarlyDegradationResult(row) {
  const source = { ...rowResult(row), ...(row || {}) }
  const result = {}
  setPicked(result, source, 'degradationPoint', [
    'earlyDegradationPoint',
    'early_degradation_point',
    'earlyDegradationTime',
    'early_degradation_time',
    'degradationPoint',
    'degradation_point',
    'degradationTime',
    'degradation_time',
    'time'
  ])
  setPicked(result, source, 'unit', ['degradationPointUnit', 'degradation_point_unit', 'unit'])
  setPicked(result, source, 'featureName', ['featureName', 'feature_name'])
  setPicked(result, source, 'increaseRate', ['increaseRate', 'increase_rate'])
  setPicked(result, source, 'summary', ['summary', 'message'])
  return compactObject(result)
}

function buildFaultPredictionResult(row) {
  const source = { ...rowResult(row), ...(row || {}) }
  const result = {}
  setPicked(result, source, 'riskLevel', ['riskLevel', 'risk_level', 'risk'])
  setPicked(result, source, 'riskScore', ['riskScore', 'risk_score', 'score'])
  setPicked(result, source, 'predictedRemainingLife', ['predictedRemainingLife', 'predicted_remaining_life'])
  setPicked(result, source, 'remainingLife', ['remainingLife', 'remaining_life', 'life'])
  setPicked(result, source, 'rulUnit', ['rulUnit', 'rul_unit', 'unit'])
  setPicked(result, source, 'maintenanceAdvice', ['maintenanceAdvice', 'maintenance_advice', 'advice', 'suggestion'])
  setPicked(result, source, 'summary', ['summary', 'message'])
  return compactObject(result)
}

async function loadLatestProject3AlgorithmResults() {
  const [
    kqcResponse,
    keyProcessResponse,
    processAnomalyResponse,
    degradationResponse,
    faultPredictionResponse
  ] = await Promise.all([
    listKqcMiningResults({ status: 'SUCCESS', page_num: 1, page_size: 1 }),
    listKeyProcessResults({ status: 'SUCCESS', page_num: 1, page_size: 1 }),
    listWarningDetectResults({ status: 'SUCCESS', page_num: 1, page_size: 1 }),
    listFaultIdentifyResults({
      task_type: 'EARLY_DEGRADATION_POINT_DETECT',
      status: 'SUCCESS',
      page_num: 1,
      page_size: 1
    }),
    listFaultIdentifyResults({
      task_type: 'FAULT_PREDICT',
      status: 'SUCCESS',
      page_num: 1,
      page_size: 1
    })
  ])

  return {
    moduleCode: 'PROJECT_3',
    resultType: 'LATEST_ALGORITHM_RESULT',
    generateTime: getNowTime(),
    keyQualityCharacteristics: buildKqcResult(latestRow(getPageRows(kqcResponse))),
    keyProcess: buildKeyProcessResult(latestRow(getPageRows(keyProcessResponse))),
    processAnomaly: buildProcessAnomalyResult(latestRow(getPageRows(processAnomalyResponse))),
    earlyDegradationPoint: buildEarlyDegradationResult(latestRow(getPageRows(degradationResponse))),
    faultPrediction: buildFaultPredictionResult(latestRow(getPageRows(faultPredictionResponse)))
  }
}

function buildTaskPayload(task, override = {}) {
  const merged = {
    ...task,
    ...override
  }

  return {
    taskId: merged.taskId,
    problemId: merged.problemId,
    problemCode: merged.problemCode,
    moduleCode: merged.moduleCode,
    moduleName: merged.moduleName,
    taskStatus: merged.taskStatus,
    dispatchOpinion: merged.dispatchOpinion || '',
    processResult: merged.processResult || '',
    processFile: merged.processFile || '',
    dispatchUserId: merged.dispatchUserId,
    dispatchUserName: merged.dispatchUserName,
    dispatchTime: merged.dispatchTime,
    submitUserId: merged.submitUserId,
    submitUserName: merged.submitUserName,
    submitTime: merged.submitTime,
    confirmUserId: merged.confirmUserId,
    confirmUserName: merged.confirmUserName,
    confirmOpinion: merged.confirmOpinion,
    confirmTime: merged.confirmTime,
    createBy: merged.createBy,
    createTime: merged.createTime,
    updateBy: merged.updateBy,
    updateTime: merged.updateTime,
    delFlag: merged.delFlag || '0'
  }
}

function notifyQmsFlowChanged(payload = {}) {
  const eventData = {
    moduleCode: MODULE_CODE,
    problemId: payload.problemId || '',
    problemCode: payload.problemCode || '',
    taskId: payload.taskId || '',
    action: payload.action || 'SUBMIT',
    time: Date.now()
  }

  window.dispatchEvent(
    new CustomEvent(QMS_FLOW_EVENT_NAME, {
      detail: eventData
    })
  )

  localStorage.setItem(QMS_FLOW_EVENT_KEY, JSON.stringify(eventData))
}

async function loadCurrentQualityTask() {
  qualityTaskLoading.value = true

  try {
    const res = await listTask({
      moduleCode: MODULE_CODE,
      taskStatus: 'PROCESSING'
    })

    const rows = Array.isArray(res?.rows)
      ? res.rows
      : Array.isArray(res?.data)
        ? res.data
        : []

    const latestTask = rows
      .map((item) => normalizeQualityTask(item))
      .sort((a, b) => {
        const timeCompare = getTimeValue(b) - getTimeValue(a)

        if (timeCompare !== 0) {
          return timeCompare
        }

        return Number(b.taskId || 0) - Number(a.taskId || 0)
      })[0]

    if (!latestTask) {
      qualityTaskList.value = []
      currentQualityTask.value = null
      currentQualityTaskResult.value = null
      return
    }

    const latestTaskWithProblemInfo = await loadQualityProblemInfo(latestTask)

    qualityTaskList.value = latestTaskWithProblemInfo ? [latestTaskWithProblemInfo] : []
    currentQualityTask.value = latestTaskWithProblemInfo || null
    currentQualityTaskResult.value = null
  } catch (error) {
    console.error('加载最新质量问题失败：', error)
    qualityTaskList.value = []
    currentQualityTask.value = null
    currentQualityTaskResult.value = null
  } finally {
    qualityTaskLoading.value = false
  }
}

async function finishCurrentQualityTask() {
  if (!currentQualityTask.value) {
    ElMessage.warning('当前没有需要处理的质量问题')
    return
  }

  finishQualityTaskLoading.value = true

  const now = getNowTime()
  const task = currentQualityTask.value

  try {
    currentQualityTaskResult.value = await loadLatestProject3AlgorithmResults()
    const returnResult = JSON.stringify(currentQualityTaskResult.value, null, 2)

    await updateTask(
      buildTaskPayload(task, {
        taskStatus: 'SUBMITTED',
        processResult: returnResult,
        submitTime: now
      })
    )

    await updateProblem({
      problemId: task.problemId,
      problemCode: task.problemCode,
      status: 'WAIT_CONFIRM',
      currentModuleCode: '',
      currentModuleName: ''
    })

    await addLog({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      actionType: 'SUBMIT',
      actionName: '质量监管与故障预防任务完成',
      operatorName: MODULE_NAME,
      fromStatus: 'PROCESSING',
      toStatus: 'WAIT_CONFIRM',
      actionContent: `${MODULE_NAME}已完成结果回填。处理结果：${returnResult}`,
      createTime: now
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'SUBMIT'
    })

    await loadCurrentQualityTask()

    ElMessage.success('算法结果已回填，当前质量问题已从首页清空')
  } catch (error) {
    console.error('结果回填失败：', error)

    const realMsg =
      error?.response?.data?.msg ||
      error?.data?.msg ||
      error?.msg ||
      error?.message ||
      String(error)

    ElMessage.error(`结果回填失败：${realMsg}`)
  } finally {
    finishQualityTaskLoading.value = false
  }
}

function handleQmsTaskChange(event) {
  const data = event.detail || {}

  if (data.moduleCode === MODULE_CODE) {
    loadCurrentQualityTask()
  }
}

function handleStorageChange(event) {
  if (event.key !== QMS_TASK_EVENT_KEY || !event.newValue) {
    return
  }

  try {
    const data = JSON.parse(event.newValue)

    if (data.moduleCode === MODULE_CODE) {
      loadCurrentQualityTask()
    }
  } catch (error) {
    console.error('解析质量问题分派事件失败：', error)
  }
}
</script>

<style scoped>
.module-card {
  min-height: 210px;
  cursor: pointer;
  border-radius: 10px;
}

.module-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1f2d3d;
}



.current-quality-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.current-quality-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}



.quality-descriptions {
  margin-bottom: 14px;
}

.description-in-table {
  line-height: 1.8;
  white-space: pre-wrap;
  color: #303133;
}

.current-quality-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.lifecycle-report-box {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fafafa;
}

.lifecycle-report-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.lifecycle-report-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.lifecycle-report-info {
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
  word-break: break-all;
}

.lifecycle-report-info strong {
  color: #303133;
  font-weight: 500;
}

.lifecycle-report-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.word-preview-wrapper {
  height: 75vh;
  overflow: auto;
  background: #f5f7fa;
  padding: 12px;
  box-sizing: border-box;
}

.module-desc {
  min-height: 104px;
  color: #607081;
  line-height: 1.8;
}

.current-quality-card {
  border-left: 4px solid #e6a23c;
}

.current-quality-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.current-quality-header-left {
  min-width: 0;
}

.refresh-task-button {
  margin-left: auto;
  flex-shrink: 0;
}

.quality-result-panel {
  margin-top: 16px;
}

.quality-result-title {
  margin-bottom: 10px;
  font-weight: 600;
  color: #303133;
}

.quality-result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.quality-result-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.quality-result-card-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.quality-result-empty {
  color: #909399;
  font-size: 13px;
}

.quality-result-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quality-result-field {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 8px;
  font-size: 13px;
  line-height: 1.6;
}

.quality-result-label {
  color: #909399;
}

.quality-result-value {
  color: #303133;
  word-break: break-word;
}

.quality-result-list {
  margin-top: 10px;
}

.quality-result-list-title {
  margin-bottom: 6px;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.quality-result-list-item {
  display: flex;
  gap: 8px;
  padding: 6px 0;
  border-top: 1px dashed #dcdfe6;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
}

.quality-result-rank {
  flex: 0 0 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}
</style>

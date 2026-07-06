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

    <!-- 当前质量问题显示区域：按课题二首页风格修改 -->
    <el-card
      class="quality-mb16 current-quality-card"
      shadow="hover"
      v-loading="qualityTaskLoading"
    >
      <template #header>
        <div class="current-quality-header">
          <div>
            <div class="current-quality-title">当前质量问题</div>
            <div class="current-quality-subtitle">
              接收质量问题管理中心分派至{{ MODULE_NAME }}的处理任务
            </div>
          </div>

          <div class="current-quality-header-actions">

            <el-button
              size="small"
              type="primary"
              plain
              @click="loadCurrentQualityTask"
            >
              刷新任务
            </el-button>
          </div>
        </div>
      </template>

      <el-empty
        v-if="!currentQualityTask"
        description="暂无质量问题管理中心分派给本模块的处理中任务"
        :image-size="90"
      />

      <template v-else>
        <div
          v-if="qualityTaskList.length > 1"
          class="quality-task-switch"
        >
          <span class="switch-label">当前质量问题：</span>

          <el-select
            :model-value="currentQualityTask.taskId"
            size="small"
            style="width: 520px"
            @change="selectQualityTask"
          >
            <el-option
              v-for="item in qualityTaskList"
              :key="item.taskId"
              :label="`${item.problemCode || '-'}｜${item.problemTitle || '未命名问题'}`"
              :value="item.taskId"
            />
          </el-select>
        </div>

        <el-descriptions
          :column="2"
          border
          class="quality-descriptions"
        >
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

          <el-descriptions-item label="问题描述" :span="2">
            <div class="description-in-table">
              {{ currentQualityTask.description || '暂无问题描述' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>



        <!-- 只有课题五已经返回报告时才显示 -->
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
            <el-button
              type="primary"
              plain
              @click="previewLifecycleReport"
            >
              预览报告
            </el-button>

            <el-button
              type="success"
              plain
              @click="downloadLifecycleReport"
            >
              下载报告
            </el-button>

            <el-button
              plain
              @click="openLifecycleReportInNewWindow"
            >
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
        <el-card shadow="hover" class="module-card" @click="go('/project_3/monitor')">
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
            进入故障识别、故障预防、健康基准建立和周期性巡检页面。
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
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import VueOfficeDocx from '@vue-office/docx'
import '@vue-office/docx/lib/index.css'

import request from '@/utils/request'
import { getHomeOverview } from '@/api/project_3/home'
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
    row.submitTime ||
    row.updateTime ||
    row.confirmTime ||
    row.dispatchTime ||
    row.createTime

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
    dispatchUserName: item.dispatchUserName || '',
    submitTime: item.submitTime || '',

    problemTitle: '',
    productModel: '',
    involvedSystem: '',
    occurPart: '',
    componentCode: '',
    description: '',

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
    return '追溯分析报告.docx'
  }

  const normalizedPath = normalizeLifecycleReportFilePath(filePath)
  const fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1)

  return fileName || '追溯分析报告.docx'
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
      productModel: problem.productModel || '',
      involvedSystem: problem.involvedSystem || '',
      occurPart: problem.occurPart || '',
      componentCode: problem.componentCode || '',
      description: problem.description || '',

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

    const normalizedRows = rows
      .map((item) => normalizeQualityTask(item))
      .sort((a, b) => {
        const timeCompare = getTimeValue(b) - getTimeValue(a)

        if (timeCompare !== 0) {
          return timeCompare
        }

        return Number(b.taskId || 0) - Number(a.taskId || 0)
      })

    const rowsWithProblemInfo = await Promise.all(
      normalizedRows.map((task) => loadQualityProblemInfo(task))
    )

    qualityTaskList.value = rowsWithProblemInfo
    currentQualityTask.value = rowsWithProblemInfo.length > 0 ? rowsWithProblemInfo[0] : null
  } catch (error) {
    console.error('加载复杂产品质量监管与故障预防当前质量问题失败：', error)
    qualityTaskList.value = []
    currentQualityTask.value = null
  } finally {
    qualityTaskLoading.value = false
  }
}

function selectQualityTask(taskId) {
  const task = qualityTaskList.value.find((item) => item.taskId === taskId)

  if (task) {
    currentQualityTask.value = task
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
//-----------------------------------结果返回------------------------------------------------------------
function buildProject3ReturnResult() {
  return JSON.stringify(
    {
      moduleCode: MODULE_CODE,
      moduleName: MODULE_NAME,

      qualityFeature: currentQualityTask.value?.qualityFeature || '',
      abnormalType: currentQualityTask.value?.abnormalType || '',
      riskLevel: currentQualityTask.value?.riskLevel || '',
      warningResult: currentQualityTask.value?.warningResult || '',
      preventionSuggestion: currentQualityTask.value?.preventionSuggestion || '',

      generateTime: getNowTime()
    },
    null,
    2
  )
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
    moduleCode: merged.moduleCode || MODULE_CODE,
    moduleName: merged.moduleName || MODULE_NAME,
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

async function finishCurrentQualityTask() {
  if (!currentQualityTask.value) {
    ElMessage.warning('当前没有需要处理的质量问题')
    return
  }

  finishQualityTaskLoading.value = true

  const now = getNowTime()
  const task = currentQualityTask.value
  const returnResult = buildProject3ReturnResult()

  try {
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
      actionContent: `复杂产品质量监管与故障预防模块已完成结果回填。处理结果：${returnResult}`,
      createTime: now
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'SUBMIT'
    })

    await loadCurrentQualityTask()

    ElMessage.success('结果已回填，当前质量问题已从首页清空')
  } catch (error) {
    console.error('结果回填失败：', error)

    const realMsg =
      error?.response?.data?.msg ||
      error?.data?.msg ||
      error?.msg ||
      error?.message ||
      String(error)

    ElMessage.error(`模拟结果回填失败：${realMsg}`)
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

.module-desc {
  min-height: 104px;
  color: #607081;
  line-height: 1.8;
}

.current-quality-card {
  border-left: 4px solid #e6a23c;
  border-radius: 10px;
}

.current-quality-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.current-quality-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quality-task-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.switch-label {
  font-size: 13px;
  color: #606266;
}

.quality-descriptions {
  margin-bottom: 14px;
}

.description-in-table {
  line-height: 1.8;
  white-space: pre-wrap;
  color: #303133;
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

.lifecycle-report-desc {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.lifecycle-report-result {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #ffffff;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
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

.current-quality-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.word-preview-wrapper {
  height: 75vh;
  overflow: auto;
  background: #f5f7fa;
  padding: 12px;
  box-sizing: border-box;
}
</style>
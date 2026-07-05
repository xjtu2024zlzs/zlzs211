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
    <el-card v-if="currentQualityTask" class="quality-mb16 current-quality-card" shadow="hover">
      <template #header>
        <div class="current-quality-header">
          <span>当前质量问题</span>
          <el-tag type="warning" style="margin-left: 10px">
            {{ currentQualityTask.moduleName || '课题三' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="问题编号">
          {{ currentQualityTask.problemCode || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="任务状态">
          {{ getQualityTaskStatusText(currentQualityTask.taskStatus) }}
        </el-descriptions-item>

        <el-descriptions-item label="分派说明" :span="2">
          {{ currentQualityTask.dispatchOpinion || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="分派时间" :span="2">
          {{ currentQualityTask.dispatchTime || currentQualityTask.createTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="current-quality-actions">
        <el-button
          type="success"
          :loading="finishQualityTaskLoading"
          :disabled="!currentQualityTask"
          @click="finishCurrentQualityTask"
        >
          完成任务并回填模拟结果
        </el-button>
      </div>
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import '@/views/project_3/common.css'
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'

import { getHomeOverview } from '@/api/project_3/home'
import { listTask, updateTask } from '@/api/quality/task'
import { updateProblem } from '@/api/quality/problem'
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
const finishQualityTaskLoading = ref(false)

const MODULE_CODE = 'PROJECT_3'

const QMS_TASK_EVENT_NAME = 'qms-current-task-change'
const QMS_TASK_EVENT_KEY = 'qms_current_task_change'

const QMS_FLOW_EVENT_NAME = 'qms-flow-change'
const QMS_FLOW_EVENT_KEY = 'qms_flow_change'

onMounted(() => {
  getOverview()
  loadCurrentQualityTask()

  window.addEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.addEventListener('storage', handleStorageChange)
})

onBeforeUnmount(() => {
  window.removeEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.removeEventListener('storage', handleStorageChange)
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

function getQualityTaskStatusText(status) {
  const map = {
    PROCESSING: '处理中',
    SUBMITTED: '待确认',
    CONFIRMED: '已确认'
  }

  return map[status] || status || '-'
}

function buildMockProject3Result() {
  return JSON.stringify(
    {
      moduleCode: 'PROJECT_3',
      moduleName: '复杂产品质量监管与故障预防',
      resultType: 'SIMULATION_RESULT',
      conclusion: '课题三已完成生命周期质量监管与故障预防分析，当前质量问题已形成模拟处理结果。',
      qualityMonitoringResult: {
        relatedQualityFeature: '关键质量特性已完成模拟关联分析',
        abnormalScore: 0.78,
        riskLevel: '中等',
        suspectedStage: '制造过程 / 服役过程',
        suspectedCause: '可能与制造过程异常波动、关键部件质量特性偏移或服役阶段性能退化有关',
        status: '已完成模拟监管分析'
      },
      preventionSuggestion: [
        '建议对相关零件、组件和分系统的质量数据进行复核。',
        '建议对关键工序异常信号进行重点巡检。',
        '建议后续接入真实质量监管算法，将异常分数、关键工序、早期故障点和预防建议写入该字段。'
      ],
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
  try {
    const res = await listTask({
      moduleCode: MODULE_CODE,
      taskStatus: 'PROCESSING'
    })

    const rows = Array.isArray(res?.rows) ? res.rows : []

    currentQualityTask.value = rows.length > 0 ? rows[0] : null
  } catch (error) {
    console.error('加载课题三当前质量问题失败：', error)
    currentQualityTask.value = null
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
  const mockResult = buildMockProject3Result()

  try {
    await updateTask(
      buildTaskPayload(task, {
        taskStatus: 'SUBMITTED',
        processResult: mockResult,
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
      actionName: '课题三任务完成',
      operatorName: '课题三',
      fromStatus: 'PROCESSING',
      toStatus: 'WAIT_CONFIRM',
      actionContent: `课题三已完成模拟结果回填。处理结果：${mockResult}`,
      createTime: now
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'SUBMIT'
    })

    currentQualityTask.value = null

    ElMessage.success('课题三模拟结果已回填，当前质量问题已从首页清空')
  } catch (error) {
    console.error('课题三模拟结果回填失败：', error)

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
}

.current-quality-header {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.current-quality-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
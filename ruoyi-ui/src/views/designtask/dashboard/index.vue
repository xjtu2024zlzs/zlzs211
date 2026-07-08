<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <h1 class="platform-title">首页（任务看板）</h1>
        </div>

        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>进行中任务</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot"></span>
            <span>Flowable 驱动</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>与本角色相关</span>
          </div>
        </div>
      </section>

      <!-- 当前质量问题显示区域 -->
      <el-card v-if="currentQualityTasks.length" class="mb20 quality-task-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>当前质量问题</span>
            <el-tag type="warning" style="margin-left: 10px">
              {{ currentQualityTask.moduleName || '设计制造协同优化平台' }}
            </el-tag>
            <el-select
              v-if="currentQualityTasks.length > 1"
              v-model="selectedQualityTaskId"
              class="quality-task-select"
              placeholder="选择问题编号"
            >
              <el-option
                v-for="item in currentQualityTasks"
                :key="item.taskId"
                :label="qualityTaskOptionLabel(item)"
                :value="item.taskId"
              />
            </el-select>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="问题编号">
            {{ currentQualityTask.problemCode || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="问题名称">
            {{ currentQualityTask.problemTitle || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="任务状态">
            {{ getQualityTaskStatusText(currentQualityTask.taskStatus) }}
          </el-descriptions-item>

          <el-descriptions-item label="严重程度">
            {{ currentQualityTask.severity || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="问题描述" :span="2">
            {{ currentQualityTask.problemDescription || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="分派说明" :span="2">
            {{ currentQualityTask.dispatchOpinion || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="分派时间" :span="2">
            {{ currentQualityTask.dispatchTime || currentQualityTask.createTime || '-' }}
          </el-descriptions-item>

          <el-descriptions-item v-if="hasCurrentQualityReport" label="结果报告" :span="2">
            <el-button
              link
              type="primary"
              icon="Document"
              :loading="previewQualityReportLoading"
              @click="previewCurrentQualityReport"
            >
              预览Word报告
            </el-button>
          </el-descriptions-item>
        </el-descriptions>

        <div class="quality-task-actions">
          <el-button
            v-if="isCurrentQualityTaskUnstarted"
            type="primary"
            icon="VideoPlay"
            :loading="startQualityTaskLoading"
            @click="startCurrentQualityTask"
          >
            开始问题处置
          </el-button>

          <el-button
            v-else-if="isCurrentQualityTaskProcessing"
            type="primary"
            plain
            icon="Position"
            @click="continueCurrentQualityTask"
          >
            继续问题处置
          </el-button>

          <el-button
            type="success"
            :loading="finishQualityTaskLoading"
            :disabled="!isCurrentQualityTaskReadyToBackfill"
            @click="finishCurrentQualityTask"
          >
            完成任务并回填模拟结果
          </el-button>
        </div>
      </el-card>

      <section class="section-block">
        <div class="section-header">
          <div>
            <h2 class="section-title">任务总览</h2>
          </div>

          <el-radio-group v-model="query.scope" @change="loadData">
            <el-radio-button label="">全部任务</el-radio-button>
            <el-radio-button label="related">与我相关</el-radio-button>
          </el-radio-group>
        </div>

        <div class="metric-grid mt-12" v-loading="loading">
          <div v-for="item in statCards" :key="item.label" class="metric-card">
            <div class="metric-card__label">{{ item.label }}</div>
            <div class="metric-card__value">{{ item.value }}</div>
            <div class="metric-card__footer">{{ item.desc }}</div>
          </div>
        </div>
      </section>

      <div class="content-grid">
        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">设计优化任务</h2>
            </div>

            <el-select
              v-model="query.status"
              placeholder="状态筛选"
              clearable
              style="width: 190px"
              @change="loadData"
            >
              <el-option label="目标约束选择" value="OBJECTIVE_SELECTING" />
              <el-option label="模型解耦求解" value="SOLVING" />
              <el-option label="仿真 / 审批" value="APPROVING" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
          </div>

          <div class="table-shell">
            <el-table
              v-loading="loading"
              :data="tasks"
              stripe
              highlight-current-row
              class="platform-table"
              :row-class-name="taskRowClassName"
              @row-click="selectTask"
            >
              <el-table-column
                label="任务名称"
                prop="taskName"
                min-width="220"
                show-overflow-tooltip
              />

              <el-table-column
                label="当前节点"
                prop="currentNodeName"
                min-width="180"
                show-overflow-tooltip
              />

              <el-table-column
                label="负责人"
                prop="ownerUserName"
                width="120"
                show-overflow-tooltip
              />

              <el-table-column label="进度" width="190">
                <template #default="{ row }">
                  <el-progress :percentage="row.progress || 0" />
                </template>
              </el-table-column>

              <el-table-column label="状态" width="140">
                <template #default="{ row }">
                  <el-tag>{{ statusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="130" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="taskAction(row).mode === 'enter'"
                    link
                    type="primary"
                    icon="Position"
                    @click.stop="enterTask(row)"
                  >
                    进入处置
                  </el-button>

                  <el-button
                    v-else-if="taskAction(row).mode === 'view'"
                    link
                    type="info"
                    icon="View"
                    @click.stop="enterTask(row)"
                  >
                    查看
                  </el-button>

                  <span v-else class="wait-action">
                    {{ taskAction(row).label || '等待' }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

        <aside class="side-stack" v-loading="detailLoading">
          <section class="section-block">
            <div class="section-header">
              <div>
                <h2 class="section-title">目标约束进度</h2>
                <p class="section-hint">{{ selectedTaskTitle }}</p>
              </div>
            </div>

            <el-timeline v-if="selectedTask" class="mt-12">
              <el-timeline-item
                v-for="item in selectedDisciplineProgress"
                :key="item.name"
                :type="timeType(item.status)"
              >
                {{ item.name }}：{{ progressLabel(item.status) }}
              </el-timeline-item>
            </el-timeline>

            <el-empty v-else description="未选择任务" />
          </section>

          <section class="section-block">
            <div class="section-header">
              <div>
                <h2 class="section-title">解耦子任务状态</h2>
                <p class="section-hint">{{ selectedTaskTitle }}</p>
              </div>
            </div>

            <div v-if="showSubtasks">
              <div
                v-for="item in selectedSubtaskProgress"
                :key="item.name"
                class="subtask-line soft-panel mt-12"
              >
                <span>{{ item.name }}</span>
                <el-tag :type="item.status === 'DONE' ? 'success' : 'info'">
                  {{ statusText(item.status) }}
                </el-tag>
              </div>
            </div>

            <el-empty
              v-else
              :description="selectedTask ? '未完成任务解耦' : '未选择任务'"
            />
          </section>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  getDashboard,
  getDesignReportTask,
  getDesignTask,
  getDesignTaskByQualityTask,
  getTaskAttachmentFile
} from '@/api/designtask/optimization'
import { listTask, updateTask } from '@/api/quality/task'
import { getProblem, listProblem, updateProblem } from '@/api/quality/problem'
import { addLog } from '@/api/quality/log'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const query = ref({ scope: '', status: '' })
const stats = ref({})
const tasks = ref([])
const selectedTask = ref(null)
const selectedDetail = ref(null)
const detailLoading = ref(false)

const currentQualityTasks = ref([])
const selectedQualityTaskId = ref(null)
const qualityProblemMap = ref({})
const startQualityTaskLoading = ref(false)
const finishQualityTaskLoading = ref(false)
const previewQualityReportLoading = ref(false)

const MODULE_CODE = 'PROJECT_2'
const QMS_TASK_EVENT_NAME = 'qms-current-task-change'
const QMS_TASK_EVENT_KEY = 'qms_current_task_change'

const QMS_FLOW_EVENT_NAME = 'qms-flow-change'
const QMS_FLOW_EVENT_KEY = 'qms_flow_change'

const qualityTaskVisibleStatuses = ['UNSTARTED', 'DISPATCHED', 'PROCESSING', 'PENDING_BACKFILL', 'SUBMITTED']

const currentQualityTask = computed(() => {
  return currentQualityTasks.value.find((item) => Number(item.taskId) === Number(selectedQualityTaskId.value)) || currentQualityTasks.value[0] || null
})

const isCurrentQualityTaskUnstarted = computed(() => {
  return ['UNSTARTED', 'DISPATCHED'].includes(currentQualityTask.value?.taskStatus)
})

const isCurrentQualityTaskProcessing = computed(() => {
  return currentQualityTask.value?.taskStatus === 'PROCESSING'
})

const isCurrentQualityTaskReadyToBackfill = computed(() => {
  return ['PENDING_BACKFILL', 'PROCESSING'].includes(currentQualityTask.value?.taskStatus) && hasCurrentQualityReport.value
})

const hasCurrentQualityReport = computed(() => {
  const payload = buildDesignReportPayloadFromQualityTask(currentQualityTask.value)
  return Boolean(payload.designTaskId || payload.reportFileId || payload.reportFilePath)
})

const objectiveNodeOrder = [
  'structure_select',
  'layout_select',
  'aero_select',
  'hydraulic_select',
  'manufacturing_select'
]

const objectiveNodeNames = [
  { key: 'structure_select', name: '结构' },
  { key: 'layout_select', name: '布局' },
  { key: 'aero_select', name: '气动' },
  { key: 'hydraulic_select', name: '液压' },
  { key: 'manufacturing_select', name: '制造' }
]

const statCards = computed(() => [
  {
    label: '进行中',
    value: stats.value.running || 0,
    desc: '正在流转的设计优化任务'
  },
  {
    label: '已完成',
    value: stats.value.completed || 0,
    desc: '审批通过并归档的任务'
  },
  {
    label: '与我相关',
    value: stats.value.related || 0,
    desc: '我发起、负责或待处理'
  },
  {
    label: '历史任务',
    value: stats.value.history || 0,
    desc: '可回溯的历史任务记录'
  }
])

const selectedTaskTitle = computed(() => {
  return selectedTask.value ? selectedTask.value.taskName : '未选择任务'
})

const selectedDisciplineProgress = computed(() => {
  const currentKey = selectedDetail.value?.nodeKey || selectedTask.value?.currentNodeKey
  const currentIndex = objectiveNodeOrder.indexOf(currentKey)

  return objectiveNodeNames.map((item) => {
    const itemIndex = objectiveNodeOrder.indexOf(item.key)

    let status = 'WAIT'

    if (currentIndex < 0) {
      status = 'DONE'
    } else if (itemIndex < currentIndex) {
      status = 'DONE'
    } else if (itemIndex === currentIndex) {
      status = 'DOING'
    }

    return {
      name: item.name,
      status
    }
  })
})

const showSubtasks = computed(() => {
  return Boolean(selectedTask.value && selectedDetail.value?.decomposed)
})

const selectedSubtaskProgress = computed(() => {
  if (!showSubtasks.value) {
    return []
  }

  return (selectedDetail.value?.subtasks || []).map((item) => {
    return {
      name: item.subtaskName || item.name,
      status: item.status || 'READY'
    }
  })
})

const getNowTime = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')

  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

const getQualityTaskStatusText = (status) => {
  const map = {
    UNSTARTED: '未开始',
    DISPATCHED: '未开始',
    PROCESSING: '处理中',
    PENDING_BACKFILL: '已完成待回填',
    SUBMITTED: '待确认',
    CONFIRMED: '已确认'
  }

  return map[status] || status || '-'
}

const qualityTaskOptionLabel = (task) => {
  const title = task?.problemTitle || '未命名质量问题'
  return `${task?.problemCode || '-'}｜${title}`
}

const routeQualityTaskId = () => {
  const value = route.query.qmsTaskId || route.query.qualityTaskId
  const number = Number(value)
  return Number.isFinite(number) && number > 0 ? number : null
}

const compareQualityTaskTimeDesc = (a, b) => {
  const at = new Date(a.dispatchTime || a.createTime || 0).getTime()
  const bt = new Date(b.dispatchTime || b.createTime || 0).getTime()
  if (bt !== at) return bt - at
  return Number(b.taskId || 0) - Number(a.taskId || 0)
}

const buildMockProject2Result = (reportInfo = null) => {
  const result = {
    moduleCode: 'PROJECT_2',
    moduleName: '复杂产品设计制造协同优化平台',
    resultType: 'SIMULATION_RESULT',
    conclusion: '设计制造协同优化平台已完成分析，当前质量问题已形成模拟处理结果。',
    optimizationResult: {
      recommendedAction: '建议对相关设计参数、制造约束和工艺方案进行协同优化校核。',
      affectedStage: '设计制造协同优化阶段',
      riskLevel: '中等',
      status: '已完成模拟分析'
    },
    suggestion: '后续可接入真实优化算法输出，将算法结果、优化参数、约束冲突信息和推荐方案写入该字段。',
    generateTime: getNowTime()
  }

  if (reportInfo) {
    result.designReport = {
      submitted: true,
      taskId: reportInfo.taskId,
      taskName: reportInfo.taskName,
      reportId: reportInfo.reportId,
      reportCode: reportInfo.reportCode,
      reportTitle: reportInfo.reportTitle,
      reportFileId: reportInfo.reportFileId,
      reportFilePath: reportInfo.reportFilePath,
      reportFileName: reportInfo.reportFileName,
      submitTime: reportInfo.submitTime
    }
  }

  return JSON.stringify(
    result,
    null,
    2
  )
}

const parseQualityProcessResult = (value) => {
  if (!value) return {}
  if (typeof value === 'object') return value
  if (typeof value !== 'string') return {}

  try {
    return JSON.parse(value)
  } catch {
    return {}
  }
}

const buildDesignReportPayloadFromQualityTask = (task = {}) => {
  const result = parseQualityProcessResult(task?.processResult)
  const designReport = result.designReport || result.reportSubmission || {}
  const isDesignResult = result.moduleCode === MODULE_CODE ||
    String(result.moduleName || '').includes('设计制造协同优化') ||
    Boolean(designReport.taskId || result.designTaskId)

  return {
    designTaskId: designReport.taskId || result.designTaskId || '',
    reportFileId: designReport.reportFileId || result.reportFileId || '',
    reportFilePath: isDesignResult ? (designReport.reportFilePath || result.reportFilePath || '') : '',
    reportFileName: designReport.reportFileName || result.reportFileName || '设计制造协同优化方案报告.doc',
    reportTitle: designReport.reportTitle || result.reportTitle || `${task?.problemCode || ''} 设计制造协同优化方案报告`
  }
}

const localSubmittedReportFromQualityTask = (task = {}) => {
  const payload = buildDesignReportPayloadFromQualityTask(task)
  const reportFilePath = payload.reportFilePath || ''
  const hasLocalReport = Boolean(payload.reportFileId || reportFilePath)

  if (!hasLocalReport) return null

  return {
    taskId: payload.designTaskId || '',
    taskName: '',
    reportId: '',
    reportCode: '',
    reportTitle: payload.reportTitle || `${task?.problemCode || ''} 设计制造协同优化方案报告`,
    reportHtml: '',
    reportFileId: payload.reportFileId || '',
    reportFilePath,
    reportFileName: payload.reportFileName || '设计制造协同优化方案报告.doc',
    submitTime: ''
  }
}

const previewReportByFileId = async (fileId, fileName = '设计制造协同优化方案报告.doc') => {
  const data = await getTaskAttachmentFile(fileId)
  const blob = new Blob([data], {
    type: fileName.toLowerCase().endsWith('.docx')
      ? 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      : 'application/msword'
  })
  const objectUrl = window.URL.createObjectURL(blob)
  window.open(objectUrl, '_blank')
  setTimeout(() => {
    window.URL.revokeObjectURL(objectUrl)
  }, 60000)
}

const buildTaskPayload = (task, override = {}) => {
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

const notifyQmsFlowChanged = (payload = {}) => {
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

const loadCurrentQualityTask = async () => {
  try {
    const [taskRes, problemRes] = await Promise.all([
      listTask({
        moduleCode: MODULE_CODE,
        pageNum: 1,
        pageSize: 9999
      }),
      listProblem({
        pageNum: 1,
        pageSize: 9999
      })
    ])

    const problemRows = Array.isArray(problemRes?.rows) ? problemRes.rows : []
    qualityProblemMap.value = problemRows.reduce((map, item) => {
      map[item.problemId] = item
      return map
    }, {})

    const rows = Array.isArray(taskRes?.rows) ? taskRes.rows : []
    const visibleRows = rows
      .filter((item) => item.moduleCode === MODULE_CODE)
      .filter((item) => qualityTaskVisibleStatuses.includes(item.taskStatus))
      .sort(compareQualityTaskTimeDesc)

    const missingProblemIds = [...new Set(
      visibleRows
        .map((item) => item.problemId)
        .filter((problemId) => problemId && !qualityProblemMap.value[problemId])
    )]

    await Promise.all(missingProblemIds.map(async (problemId) => {
      try {
        const problemRes = await getProblem(problemId)
        if (problemRes?.data) {
          qualityProblemMap.value[problemId] = problemRes.data
        }
      } catch (error) {
        console.warn('加载质量问题详情失败：', problemId, error)
      }
    }))

    const mergedQualityTasks = visibleRows.map((item) => {
      const problem = qualityProblemMap.value[item.problemId] || {}
      return {
        ...item,
        problemTitle: problem.title || item.problemTitle || '',
        problemDescription: problem.description || item.problemDescription || '',
        severity: problem.severity || item.severity || '',
        involvedSystem: problem.involvedSystem || item.involvedSystem || '',
        occurPart: problem.occurPart || item.occurPart || ''
      }
    })
    currentQualityTasks.value = await Promise.all(mergedQualityTasks.map(syncQualityTaskReadyToBackfill))

    const queryTaskId = routeQualityTaskId()
    const hasQueryTask = currentQualityTasks.value.some((item) => Number(item.taskId) === queryTaskId)
    const stillSelected = currentQualityTasks.value.some((item) => Number(item.taskId) === Number(selectedQualityTaskId.value))

    if (hasQueryTask) {
      selectedQualityTaskId.value = queryTaskId
    } else if (!stillSelected) {
      selectedQualityTaskId.value = currentQualityTasks.value[0]?.taskId || null
    }
  } catch (error) {
    console.error('加载设计制造协同优化平台当前质量问题失败：', error)
    currentQualityTasks.value = []
    selectedQualityTaskId.value = null
  }
}

const normalizeSubmittedReport = (task, report) => {
  if (!report || (!report.submitted && !report.reportFileId && !report.reportHtml)) {
    return null
  }

  return {
    taskId: report.taskId || task.taskId,
    taskName: task.taskName || report.taskName || '',
    reportId: report.reportId || '',
    reportCode: report.reportCode || '',
    reportTitle: report.reportTitle || `${task.taskName || '设计制造协同优化'}方案报告`,
    reportHtml: report.reportHtml || '',
    reportFileId: report.reportFileId || '',
    reportFilePath: report.reportFilePath || '',
    reportFileName: report.reportFileName || '设计制造协同优化方案报告.doc',
    submitTime: report.submitTime || ''
  }
}

const findSubmittedDesignReportByQualityTask = async (qualityTask) => {
  if (!qualityTask?.taskId) return null

  const linkRes = await getDesignTaskByQualityTask(qualityTask.taskId)
  const linkData = linkRes?.data || {}
  const designTask = linkData.task || {}
  const designTaskId = designTask.taskId || linkData.qualityTaskLink?.designTaskId

  if (!designTaskId) return null

  const reportRes = await getDesignReportTask(designTaskId)
  return normalizeSubmittedReport(designTask, reportRes?.data || {})
}

const qualityMatchScore = (qualityTask, detail = {}) => {
  const link = detail.qualityTaskLink || {}
  if (Number(link.qualityTaskId) === Number(qualityTask.taskId)) return 100
  return 0
}

const findSubmittedDesignReportFromTaskList = async (qualityTask) => {
  const candidateTasks = [
    selectedTask.value,
    ...tasks.value.filter((item) => item.taskId !== selectedTask.value?.taskId)
  ].filter((item) => item?.taskId)

  const submittedReports = []

  for (const task of candidateTasks) {
    try {
      const [detailRes, reportRes] = await Promise.all([
        getDesignTask(task.taskId).catch(() => ({ data: {} })),
        getDesignReportTask(task.taskId).catch(() => ({ data: { submitted: false } }))
      ])
      const detail = detailRes?.data || {}
      const designTask = { ...task, ...(detail.task || {}) }
      const reportInfo = normalizeSubmittedReport(designTask, reportRes?.data || {})
      if (!reportInfo) continue

      submittedReports.push({
        reportInfo,
        score: qualityMatchScore(qualityTask, detail)
      })
    } catch (error) {
      console.warn('从设计任务列表匹配设计制造协同优化报告失败：', error)
    }
  }

  const matched = submittedReports
    .filter((item) => item.score > 0)
    .sort((a, b) => b.score - a.score)[0]

  return matched?.reportInfo || null
}

const findSubmittedDesignReport = async () => {
  if (currentQualityTask.value?.taskId) {
    const localReport = localSubmittedReportFromQualityTask(currentQualityTask.value)
    if (localReport) {
      return localReport
    }

    const taskListReport = await findSubmittedDesignReportFromTaskList(currentQualityTask.value)
    if (taskListReport) {
      return taskListReport
    }

    try {
      const reportInfo = await findSubmittedDesignReportByQualityTask(currentQualityTask.value)
      if (reportInfo) {
        return reportInfo
      }
    } catch (error) {
      console.warn('按质量任务查询设计报告失败，继续尝试设计任务列表：', error)
    }
  }

  return null
}

const syncQualityTaskReadyToBackfill = async (task) => {
  if (!task?.taskId || task.taskStatus !== 'PROCESSING') return task

  try {
    const reportInfo = localSubmittedReportFromQualityTask(task) ||
      await findSubmittedDesignReportByQualityTask(task) ||
      await findSubmittedDesignReportFromTaskList(task)
    if (!reportInfo) return task

    const processResult = buildMockProject2Result(reportInfo)
    const updatedTask = {
      ...task,
      taskStatus: 'PENDING_BACKFILL',
      processResult,
      processFile: reportInfo.reportFilePath || ''
    }

    await updateTask(buildTaskPayload(task, {
      taskStatus: 'PENDING_BACKFILL',
      processResult,
      processFile: updatedTask.processFile
    }))

    await addLog({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      actionType: 'REPORT_READY',
      actionName: '设计报告待回填',
      operatorName: '设计制造协同优化平台',
      fromStatus: 'PROCESSING',
      toStatus: 'PENDING_BACKFILL',
      actionContent: `设计制造协同优化平台已形成可回填的最终设计方案报告：${reportInfo.reportTitle || '设计制造协同优化方案报告'}。`,
      createTime: getNowTime()
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'REPORT_READY'
    })

    return updatedTask
  } catch (error) {
    console.warn('同步设计制造协同优化平台质量任务待回填状态失败：', error)
    return task
  }
}

const openDesignTaskForQualityTask = async (qualityTask, fallbackToMechanism = true) => {
  if (!qualityTask?.taskId) return

  try {
    const res = await getDesignTaskByQualityTask(qualityTask.taskId)
    const data = res?.data || {}
    const designTask = data.task || {}

    if (data.linked && designTask.taskId) {
      router.push({
        path: data.stageRoute || '/designtask/dashboard',
        query: {
          taskId: designTask.taskId,
          qmsTaskId: qualityTask.taskId
        }
      })
      return
    }
  } catch (error) {
    console.warn('查询质量任务关联的设计任务失败：', error)
  }

  if (fallbackToMechanism) {
    router.push({
      path: '/designtask/mechanism',
      query: {
        qmsTaskId: qualityTask.taskId,
        problemId: qualityTask.problemId,
        problemCode: qualityTask.problemCode
      }
    })
  }
}

const startCurrentQualityTask = async () => {
  const task = currentQualityTask.value
  if (!task) return

  startQualityTaskLoading.value = true
  const now = getNowTime()

  try {
    await updateTask(
      buildTaskPayload(task, {
        taskStatus: 'PROCESSING'
      })
    )

    await addLog({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      actionType: 'START',
      actionName: '设计制造协同优化平台开始处置',
      operatorName: '设计制造协同优化平台',
      fromStatus: task.taskStatus || 'UNSTARTED',
      toStatus: 'PROCESSING',
      actionContent: `设计制造协同优化平台已开始处理质量问题：${qualityTaskOptionLabel(task)}。`,
      createTime: now
    })

    task.taskStatus = 'PROCESSING'
    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'START'
    })

    await openDesignTaskForQualityTask(task)
  } catch (error) {
    console.error('开始设计制造协同优化平台质量问题处置失败：', error)
    ElMessage.error('开始问题处置失败，请检查质量任务状态')
  } finally {
    startQualityTaskLoading.value = false
  }
}

const continueCurrentQualityTask = () => {
  if (!currentQualityTask.value) return
  openDesignTaskForQualityTask(currentQualityTask.value)
}

const previewCurrentQualityReport = async () => {
  const task = currentQualityTask.value
  if (!task) return

  previewQualityReportLoading.value = true
  try {
    let payload = buildDesignReportPayloadFromQualityTask(task)

    if (!payload.reportFileId && task.taskId) {
      const linkRes = await getDesignTaskByQualityTask(task.taskId)
      const designTaskId = linkRes?.data?.task?.taskId || linkRes?.data?.qualityTaskLink?.designTaskId
      if (designTaskId) {
        const reportRes = await getDesignReportTask(designTaskId)
        const report = normalizeSubmittedReport(linkRes?.data?.task || {}, reportRes?.data || {})
        payload = {
          ...payload,
          reportFileId: report?.reportFileId || payload.reportFileId,
          reportFileName: report?.reportFileName || payload.reportFileName
        }
      }
    }

    if (!payload.reportFileId) {
      ElMessage.warning('当前任务暂无可预览的设计方案报告文件')
      return
    }

    await previewReportByFileId(payload.reportFileId, payload.reportFileName)
  } catch (error) {
    console.error('预览设计制造协同优化方案报告失败：', error)
    ElMessage.error('预览Word报告失败')
  } finally {
    previewQualityReportLoading.value = false
  }
}

const finishCurrentQualityTask = async () => {
  if (!currentQualityTask.value) {
    ElMessage.warning('当前没有需要处理的质量问题')
    return
  }

  finishQualityTaskLoading.value = true

  const now = getNowTime()
  const task = currentQualityTask.value

  try {
    const reportInfo = await findSubmittedDesignReport()

    if (!reportInfo) {
      ElMessage.warning('当前质量任务未找到已绑定的设计制造协同优化方案报告，暂不能回填')
      return
    }

    const mockResult = buildMockProject2Result(reportInfo)
    const reportText = reportInfo?.reportTitle ? `，已关联优化方案报告：${reportInfo.reportTitle}` : ''

    await updateTask(
      buildTaskPayload(task, {
        taskStatus: 'SUBMITTED',
        processResult: mockResult,
        processFile: reportInfo.reportFilePath || '',
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
      actionName: '设计制造协同优化平台任务完成',
      operatorName: '设计制造协同优化平台',
      fromStatus: 'PROCESSING',
      toStatus: 'WAIT_CONFIRM',
      actionContent: `设计制造协同优化平台已完成模拟结果回填${reportText}。处理结果：${mockResult}`,
      createTime: now
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'SUBMIT'
    })

    await loadCurrentQualityTask()

    ElMessage.success(reportInfo ? '模拟结果已回填，并已关联优化方案报告' : '模拟结果已回填，当前质量问题已从首页清空')
  } catch (error) {
    console.error('设计制造协同优化平台模拟结果回填失败：', error)

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

const handleQmsTaskChange = (event) => {
  const data = event.detail || {}

  if (data.moduleCode === MODULE_CODE) {
    if (data.taskId) {
      selectedQualityTaskId.value = Number(data.taskId)
    }
    loadCurrentQualityTask()
  }
}

const handleStorageChange = (event) => {
  if (event.key !== QMS_TASK_EVENT_KEY || !event.newValue) {
    return
  }

  try {
    const data = JSON.parse(event.newValue)

    if (data.moduleCode === MODULE_CODE) {
      if (data.taskId) {
        selectedQualityTaskId.value = Number(data.taskId)
      }
      loadCurrentQualityTask()
    }
  } catch (error) {
    console.error('解析质量问题分派事件失败：', error)
  }
}

function loadData() {
  loading.value = true

  return getDashboard(query.value)
    .then((res) => {
      const data = res.data || {}

      stats.value = data.stats || {}
      tasks.value = data.tasks || []

      const stillVisible = tasks.value.find((item) => {
        return item.taskId === selectedTask.value?.taskId
      })

      selectedTask.value = stillVisible || tasks.value[0] || null
      selectedDetail.value = null

      if (selectedTask.value) {
        loadSelectedDetail(selectedTask.value)
      }
    })
    .finally(() => {
      loading.value = false
    })
}

function selectTask(row) {
  selectedTask.value = row
  loadSelectedDetail(row)
}

function loadSelectedDetail(row) {
  if (!row?.taskId) {
    return
  }

  detailLoading.value = true

  getDesignTask(row.taskId)
    .then((res) => {
      if (selectedTask.value?.taskId === row.taskId) {
        selectedDetail.value = res.data || {}
      }
    })
    .finally(() => {
      detailLoading.value = false
    })
}

function enterTask(row) {
  const action = taskAction(row)

  if (action.mode === 'wait') {
    return
  }

  getDesignTask(row.taskId).then((res) => {
    const routePath = completedTask(row)
      ? '/designtask/archive'
      : res.data.stageRoute || '/designtask/dashboard'

    router.push({
      path: routePath,
      query: {
        taskId: row.taskId,
        mode: action.mode
      }
    })
  })
}

function taskRowClassName({ row }) {
  return row.taskId === selectedTask.value?.taskId ? 'is-selected-task' : ''
}

function taskAction(row) {
  if (completedTask(row)) {
    return {
      mode: 'view',
      label: '查看',
      reason: '任务已完成'
    }
  }

  return row.action || {
    mode: 'wait',
    label: '等待'
  }
}

function completedTask(row) {
  return row?.status === 'COMPLETED' || row?.currentNodeKey === 'end'
}

function statusLabel(status) {
  const map = {
    OBJECTIVE_SELECTING: '目标约束选择',
    CONFLICT_CHECKING: '冲突校验',
    SOLVING: '模型解耦求解',
    SIMULATING: '仿真确认',
    APPROVING: '审批中',
    COMPLETED: '已完成',
    REWORK: '返工'
  }

  return map[status] || status || '-'
}

function progressLabel(status) {
  const map = {
    DONE: '已完成',
    DOING: '进行中',
    WAIT: '等待',
    READY: '已准备'
  }

  return map[status] || status
}

function statusText(status) {
  const map = {
    DONE: '已完成',
    READY: '已解耦',
    SOLVING: '求解中',
    WAIT: '等待'
  }

  return map[status] || status || '-'
}

function timeType(status) {
  const map = {
    DONE: 'success',
    DOING: 'primary',
    WAIT: 'info'
  }

  return map[status] || 'info'
}

onMounted(() => {
  loadData().finally(() => {
    loadCurrentQualityTask()
  })

  window.addEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.addEventListener('storage', handleStorageChange)
})

onBeforeUnmount(() => {
  window.removeEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.mb20 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.quality-task-select {
  width: 320px;
  margin-left: auto;
}

.quality-task-card {
  border-left: 4px solid #e6a23c;
}

.quality-task-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.section-hint {
  margin: 4px 0 0;
  color: #667085;
  font-size: 13px;
}

:deep(.platform-table .is-selected-task td) {
  background: #eef6ff !important;
}
</style>

<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">DESIGN OPTIMIZATION / TASK BOARD</p>
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
      <el-card class="mb20 quality-task-card" shadow="hover" v-loading="qualityTaskLoading">
        <template #header>
          <div class="card-header quality-card-header">
            <div>
              <span>当前质量问题</span>
              <el-tag type="warning" style="margin-left: 10px">
                质量中心分派
              </el-tag>
            </div>

            <el-button size="small" type="primary" plain @click="loadCurrentQualityTask">
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
          <div v-if="qualityTaskList.length > 1" class="quality-task-switch">
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

          <el-descriptions :column="2" border>
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
              {{ currentQualityTask.description || '-' }}
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

          <div class="quality-task-actions">
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

      <section class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">OVERVIEW</p>
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
              <p class="section-label">TASK LIST</p>
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
                <p class="section-label">DISCIPLINE</p>
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
                <p class="section-label">SUBTASK</p>
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
import { computed, onActivated, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import VueOfficeDocx from '@vue-office/docx'
import '@vue-office/docx/lib/index.css'

import request from '@/utils/request'
import { getDashboard, getDesignTask } from '@/api/designtask/optimization'
import { listTask, updateTask } from '@/api/quality/task'
import { getProblem, updateProblem } from '@/api/quality/problem'
import { addLog } from '@/api/quality/log'

const router = useRouter()

const loading = ref(false)
const query = ref({ scope: '', status: '' })
const stats = ref({})
const tasks = ref([])
const selectedTask = ref(null)
const selectedDetail = ref(null)
const detailLoading = ref(false)

const currentQualityTask = ref(null)
const qualityTaskList = ref([])
const qualityTaskLoading = ref(false)
const finishQualityTaskLoading = ref(false)

const lifecycleReportPreviewOpen = ref(false)
const lifecycleReportPreviewUrl = ref('')
const lifecycleReportPreviewTitle = ref('全生命周期数字质量自反馈与追溯模块报告预览')

const MODULE_CODE = 'PROJECT_2'
const MODULE_NAME = '设计制造协同优化平台'

const RELATED_REPORT_MODULE_CODE = 'PROJECT_5'
const RELATED_REPORT_MODULE_NAME = '全生命周期数字质量自反馈与追溯模块'

const QMS_TASK_EVENT_NAME = 'qms-current-task-change'
const QMS_TASK_EVENT_KEY = 'qms_current_task_change'

const QMS_FLOW_EVENT_NAME = 'qms-flow-change'
const QMS_FLOW_EVENT_KEY = 'qms_flow_change'

const objectiveNodeOrder = [
  'structure_select',
  'layout_select',
  'aero_select',
  'hydraulic_select',
  'manufacturing_select'
]

const getLifecycleReportFileName = (filePath) => {
  if (!filePath) {
    return ''
  }

  const normalizedPath = normalizeLifecycleReportFilePath(filePath)
  const fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1)

  return fileName || ''
}

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

const normalizeQualityTask = (item) => {
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

const loadLatestLifecycleReportByProblemId = async (problemId) => {
  if (!problemId) {
    return null
  }

  try {
    const res = await listTask({
      problemId,
      moduleCode: RELATED_REPORT_MODULE_CODE
    })

    const rows = Array.isArray(res?.rows) ? res.rows : []

    const reportTasks = rows
      .filter((item) => {
        const file = item.processFile || ''

        // 必须已经有报告文件
        if (!file) {
          return false
        }

        // 必须是 Word 报告
        const lowerFile = file.toLowerCase()
        if (!lowerFile.endsWith('.doc') && !lowerFile.endsWith('.docx')) {
          return false
        }

        return ['SUBMITTED', 'CONFIRMED'].includes(item.taskStatus)
      })
      .sort((a, b) => {
        const at =
          a.submitTime ||
          a.updateTime ||
          a.confirmTime ||
          a.dispatchTime ||
          a.createTime ||
          ''

        const bt =
          b.submitTime ||
          b.updateTime ||
          b.confirmTime ||
          b.dispatchTime ||
          b.createTime ||
          ''

        const timeCompare = bt.localeCompare(at)

        if (timeCompare !== 0) {
          return timeCompare
        }

        return Number(b.taskId || 0) - Number(a.taskId || 0)
      })

    return reportTasks.length > 0 ? reportTasks[0] : null
  } catch (error) {
    console.error(`查询${RELATED_REPORT_MODULE_NAME}最新运行报告失败：`, error)
    return null
  }
}

const loadQualityProblemInfo = async (task) => {
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
      problemTitle: problem.title || '',
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

      // 这里取的是同一个质量问题下，全生命周期数字质量自反馈与追溯模块最新一次运行返回的报告
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

const loadCurrentQualityTask = async () => {
  qualityTaskLoading.value = true

  try {
    const res = await listTask({
      moduleCode: MODULE_CODE,
      taskStatus: 'PROCESSING'
    })

    const rows = Array.isArray(res?.rows) ? res.rows : []

    const normalizedRows = rows
      .map((item) => normalizeQualityTask(item))
      .sort((a, b) => {
        const at = a.dispatchTime || a.createTime || ''
        const bt = b.dispatchTime || b.createTime || ''
        return bt.localeCompare(at)
      })

    const rowsWithProblemInfo = await Promise.all(
      normalizedRows.map((task) => loadQualityProblemInfo(task))
    )

    qualityTaskList.value = rowsWithProblemInfo
    currentQualityTask.value = rowsWithProblemInfo.length > 0 ? rowsWithProblemInfo[0] : null
  } catch (error) {
    console.error('加载设计制造协同优化平台当前质量问题失败：', error)
    qualityTaskList.value = []
    currentQualityTask.value = null
  } finally {
    qualityTaskLoading.value = false
  }
}

const selectQualityTask = (taskId) => {
  const task = qualityTaskList.value.find((item) => item.taskId === taskId)

  if (task) {
    currentQualityTask.value = task
  }
}

const isWordFile = (url) => {
  if (!url) return false

  const lower = url.toLowerCase()
  return lower.endsWith('.doc') || lower.endsWith('.docx')
}

const normalizeLifecycleReportFilePath = (filePath) => {
  if (!filePath) return ''

  let path = filePath.replace(/\\/g, '/')

  if (path.startsWith('/topic5/profile/')) {
    path = path.replace('/topic5/profile/', '/profile/')
  }

  return path
}

const fetchLifecycleReportBlob = async (filePath) => {
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

const previewLifecycleReport = async () => {
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

const downloadLifecycleReport = async () => {
  if (!currentQualityTask.value || !currentQualityTask.value.lifecycleReportFile) {
    ElMessage.warning(`当前质量问题暂无${RELATED_REPORT_MODULE_NAME}报告`)
    return
  }

  try {
    const filePath = currentQualityTask.value.lifecycleReportFile
    const blob = await fetchLifecycleReportBlob(filePath)

    const normalizedPath = filePath.replace(/\\/g, '/')
    const fileName =
      normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1) ||
      `${RELATED_REPORT_MODULE_NAME}报告.docx`

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

const openLifecycleReportInNewWindow = async () => {
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

const clearLifecycleReportPreview = () => {
  if (lifecycleReportPreviewUrl.value && lifecycleReportPreviewUrl.value.startsWith('blob:')) {
    window.URL.revokeObjectURL(lifecycleReportPreviewUrl.value)
  }

  lifecycleReportPreviewUrl.value = ''
}

const buildMockProject2Result = () => {
  return '设计制造协同优化平台已完成设计制造协同优化分析，当前质量问题已形成模拟处理结果。'
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

const finishCurrentQualityTask = async () => {
  if (!currentQualityTask.value) {
    ElMessage.warning('当前没有需要处理的质量问题')
    return
  }

  finishQualityTaskLoading.value = true

  const now = getNowTime()
  const task = currentQualityTask.value

  try {
    const reportFilePath = getProject2ReportFilePath(task)

    if (!reportFilePath) {
      throw new Error('课题二报告路径为空，请检查报告路径配置')
    }

    const returnResult = buildProject2ReturnResult()

    await updateTask(
      buildTaskPayload(task, {
        taskStatus: 'SUBMITTED',
        processResult: returnResult,
        processFile: reportFilePath,
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
      actionContent: `设计制造协同优化平台已完成任务处理，并返回Word报告：${reportFilePath}`,
      createTime: now
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'SUBMIT'
    })

    await loadCurrentQualityTask()

    ElMessage.success('设计制造协同优化平台处理结果和Word报告已回填质量问题管理中心')
  } catch (error) {
    console.error('设计制造协同优化平台结果回填失败：', error)

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

const handleQmsTaskChange = (event) => {
  const data = event.detail || {}

  if (data.moduleCode === MODULE_CODE) {
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
      loadCurrentQualityTask()
    }
  } catch (error) {
    console.error('解析质量问题分派事件失败：', error)
  }
}

const buildProject2ReturnResult = () => {
  return '设计制造协同优化平台已完成质量问题处理，并生成Word报告，详细结果请查看处理结果文件。'
}

/**
 * 获取课题二本次任务对应的报告路径
 *
 * 当前测试阶段固定返回 000.docx。
 * 后续课题二真实报告文件名确定后，只需要改这个函数。
 */
const getProject2ReportFilePath = (task) => {
  return '/profile/topic2/report/QF-20260705-52435.docx'
}

function loadData() {
  loading.value = true

  getDashboard(query.value)
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
  loadData()
  loadCurrentQualityTask()

  window.addEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.addEventListener('storage', handleStorageChange)
  window.addEventListener('focus', loadCurrentQualityTask)
})

onActivated(() => {
  loadCurrentQualityTask()
})

onBeforeUnmount(() => {
  window.removeEventListener(QMS_TASK_EVENT_NAME, handleQmsTaskChange)
  window.removeEventListener('storage', handleStorageChange)
  window.removeEventListener('focus', loadCurrentQualityTask)
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

.quality-card-header {
  justify-content: space-between;
}

.quality-task-card {
  border-left: 4px solid #e6a23c;
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

.quality-task-actions {
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

.lifecycle-report-desc {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
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
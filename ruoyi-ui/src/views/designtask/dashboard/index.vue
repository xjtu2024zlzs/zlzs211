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
      <el-card v-if="currentQualityTask" class="mb20 quality-task-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>当前质量问题</span>
            <el-tag type="warning" style="margin-left: 10px">
              {{ currentQualityTask.moduleName || '课题二' }}
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

        <div class="quality-task-actions">
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
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getDashboard, getDesignTask } from '@/api/designtask/optimization'
import { listTask, updateTask } from '@/api/quality/task'
import { updateProblem } from '@/api/quality/problem'
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
const finishQualityTaskLoading = ref(false)

const MODULE_CODE = 'PROJECT_2'
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
    PROCESSING: '处理中',
    SUBMITTED: '待确认',
    CONFIRMED: '已确认'
  }

  return map[status] || status || '-'
}

const buildMockProject2Result = () => {
  return JSON.stringify(
    {
      moduleCode: 'PROJECT_2',
      moduleName: '复杂产品设计制造协同优化平台',
      resultType: 'SIMULATION_RESULT',
      conclusion: '课题二已完成设计制造协同优化分析，当前质量问题已形成模拟处理结果。',
      optimizationResult: {
        recommendedAction: '建议对相关设计参数、制造约束和工艺方案进行协同优化校核。',
        affectedStage: '设计制造协同优化阶段',
        riskLevel: '中等',
        status: '已完成模拟分析'
      },
      suggestion: '后续可接入真实优化算法输出，将算法结果、优化参数、约束冲突信息和推荐方案写入该字段。',
      generateTime: getNowTime()
    },
    null,
    2
  )
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
    const res = await listTask({
      moduleCode: MODULE_CODE,
      taskStatus: 'PROCESSING'
    })

    const rows = Array.isArray(res?.rows) ? res.rows : []

    currentQualityTask.value = rows.length > 0 ? rows[0] : null
  } catch (error) {
    console.error('加载课题二当前质量问题失败：', error)
    currentQualityTask.value = null
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
  const mockResult = buildMockProject2Result()

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
      actionName: '课题二任务完成',
      operatorName: '课题二',
      fromStatus: 'PROCESSING',
      toStatus: 'WAIT_CONFIRM',
      actionContent: `课题二已完成模拟结果回填。处理结果：${mockResult}`,
      createTime: now
    })

    notifyQmsFlowChanged({
      problemId: task.problemId,
      problemCode: task.problemCode,
      taskId: task.taskId,
      action: 'SUBMIT'
    })

    currentQualityTask.value = null

    ElMessage.success('课题二模拟结果已回填，当前质量问题已从首页清空')
  } catch (error) {
    console.error('课题二模拟结果回填失败：', error)

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
<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">DESIGN OPTIMIZATION / TASK BOARD</p>
          <h1 class="platform-title">首页（任务看板）</h1>
          <p class="platform-subtitle">
            面向起落架舱门协同优化任务，集中查看流程进度、学科协同状态、模型求解结果与仿真验证结论。
          </p>
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
            <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 190px" @change="loadData">
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
              <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
              <el-table-column label="当前节点" prop="currentNodeName" min-width="180" show-overflow-tooltip />
              <el-table-column label="负责人" prop="ownerUserName" width="120" show-overflow-tooltip />
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
                    @click.stop="enterTask(row)"
                  >
                    进入处置
                  </el-button>
                  <el-button
                    v-else-if="taskAction(row).mode === 'view'"
                    link
                    type="info"
                    @click.stop="enterTask(row)"
                  >
                    查看
                  </el-button>
                  <span v-else class="wait-action">{{ taskAction(row).label || '等待' }}</span>
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
              <el-timeline-item v-for="item in selectedDisciplineProgress" :key="item.name" :type="timeType(item.status)">
                {{ item.name }}：{{ progressLabel(item.status) }}
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="请先在左侧选择任务" />
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
              <div v-for="item in selectedSubtaskProgress" :key="item.name" class="subtask-line soft-panel mt-12">
                <span>{{ item.name }}</span>
                <el-tag :type="item.status === 'DONE' ? 'success' : 'info'">{{ statusText(item.status) }}</el-tag>
              </div>
            </div>
            <el-empty v-else :description="selectedTask ? '当前任务尚未完成任务解耦' : '请先在左侧选择任务'" />
          </section>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard, getDesignTask } from '@/api/designtask/optimization'

const router = useRouter()
const loading = ref(false)
const query = ref({ scope: '', status: '' })
const stats = ref({})
const tasks = ref([])
const selectedTask = ref(null)
const selectedDetail = ref(null)
const detailLoading = ref(false)

const objectiveNodeOrder = ['structure_select', 'layout_select', 'aero_select', 'hydraulic_select', 'manufacturing_select']
const objectiveNodeNames = [
  { key: 'structure_select', name: '结构' },
  { key: 'layout_select', name: '布局' },
  { key: 'aero_select', name: '气动' },
  { key: 'hydraulic_select', name: '液压' },
  { key: 'manufacturing_select', name: '制造' }
]

const statCards = computed(() => [
  { label: '进行中', value: stats.value.running || 0, desc: '正在流转的设计优化任务' },
  { label: '已完成', value: stats.value.completed || 0, desc: '审批通过并归档的任务' },
  { label: '与我相关', value: stats.value.related || 0, desc: '我发起、负责或待处理' },
  { label: '历史任务', value: stats.value.history || 0, desc: '可回溯的历史任务记录' }
])

const selectedTaskTitle = computed(() => selectedTask.value ? selectedTask.value.taskName : '未选择任务')

const selectedDisciplineProgress = computed(() => {
  const currentKey = selectedDetail.value?.nodeKey || selectedTask.value?.currentNodeKey
  const currentIndex = objectiveNodeOrder.indexOf(currentKey)
  return objectiveNodeNames.map(item => {
    const itemIndex = objectiveNodeOrder.indexOf(item.key)
    let status = 'WAIT'
    if (currentIndex < 0) {
      status = 'DONE'
    } else if (itemIndex < currentIndex) {
      status = 'DONE'
    } else if (itemIndex === currentIndex) {
      status = 'DOING'
    }
    return { name: item.name, status }
  })
})

const showSubtasks = computed(() => Boolean(selectedTask.value && selectedDetail.value?.decomposed))

const selectedSubtaskProgress = computed(() => {
  if (!showSubtasks.value) return []
  return (selectedDetail.value?.subtasks || []).map(item => ({
    name: item.subtaskName || item.name,
    status: item.status || 'READY'
  }))
})

function loadData() {
  loading.value = true
  getDashboard(query.value).then(res => {
    const data = res.data || {}
    stats.value = data.stats || {}
    tasks.value = data.tasks || []
    const stillVisible = tasks.value.find(item => item.taskId === selectedTask.value?.taskId)
    selectedTask.value = stillVisible || tasks.value[0] || null
    selectedDetail.value = null
    if (selectedTask.value) {
      loadSelectedDetail(selectedTask.value)
    }
  }).finally(() => {
    loading.value = false
  })
}

function selectTask(row) {
  selectedTask.value = row
  loadSelectedDetail(row)
}

function loadSelectedDetail(row) {
  if (!row?.taskId) return
  detailLoading.value = true
  getDesignTask(row.taskId).then(res => {
    if (selectedTask.value?.taskId === row.taskId) {
      selectedDetail.value = res.data || {}
    }
  }).finally(() => {
    detailLoading.value = false
  })
}

function enterTask(row) {
  const action = taskAction(row)
  if (action.mode === 'wait') return
  getDesignTask(row.taskId).then(res => {
    const routePath = completedTask(row) ? '/designtask/archive' : (res.data.stageRoute || '/designtask/dashboard')
    router.push({
      path: routePath,
      query: { taskId: row.taskId, mode: action.mode }
    })
  })
}

function taskRowClassName({ row }) {
  return row.taskId === selectedTask.value?.taskId ? 'is-selected-task' : ''
}

function taskAction(row) {
  if (completedTask(row)) {
    return { mode: 'view', label: '查看', reason: '任务已完成' }
  }
  return row.action || { mode: 'wait', label: '等待' }
}

function completedTask(row) {
  return row?.status === 'COMPLETED' || row?.currentNodeKey === 'end'
}

function statusLabel(status) {
  return {
    OBJECTIVE_SELECTING: '目标约束选择',
    CONFLICT_CHECKING: '冲突校验',
    SOLVING: '模型解耦求解',
    SIMULATING: '仿真确认',
    APPROVING: '审批中',
    COMPLETED: '已完成',
    REWORK: '返工'
  }[status] || status || '-'
}

function progressLabel(status) {
  return { DONE: '已完成', DOING: '进行中', WAIT: '等待', READY: '已准备' }[status] || status
}

function statusText(status) {
  return { DONE: '已完成', READY: '已解耦', SOLVING: '求解中', WAIT: '等待' }[status] || status || '-'
}

function timeType(status) {
  return { DONE: 'success', DOING: 'primary', WAIT: 'info' }[status] || 'info'
}

onMounted(loadData)
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

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

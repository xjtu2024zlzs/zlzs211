<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">MODEL DECOMPOSITION & SOLVING</p>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '模型解耦求解' }}</h1>
          <p v-if="!hasTask" class="platform-subtitle">
            请选择一个模型解耦求解相关任务。
          </p>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>{{ hasTask ? accessLabel : tabLabel }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--orange"></span>
            <span>解耦与求解</span>
          </div>
        </div>
      </section>

      <section v-if="!hasTask" class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">TASK INBOX</p>
            <h2 class="section-title">我的模型解耦求解任务</h2>
          </div>
          <el-button plain :loading="inboxLoading" @click="loadInbox">刷新</el-button>
        </div>

        <el-tabs v-model="activeTab" class="mt-12" @tab-change="changeTab">
          <el-tab-pane label="待处理" name="pending" />
          <el-tab-pane label="已处理" name="handled" />
          <el-tab-pane label="相关任务" name="related" />
        </el-tabs>

        <div class="table-shell">
          <el-table v-loading="inboxLoading" :data="visibleInboxTasks" stripe class="platform-table">
            <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
            <el-table-column label="当前节点" prop="currentNodeName" min-width="180" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="actionType(row.action?.mode)">{{ row.action?.label || (activeTab === 'pending' ? '等待' : '查看') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="说明" min-width="180">
              <template #default="{ row }">{{ row.action?.reason || row.handledReason || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.action?.mode !== 'wait' || activeTab !== 'pending'"
                  link
                  :type="row.action?.mode === 'enter' ? 'primary' : 'info'"
                  @click="openTask(row)"
                >
                  {{ activeTab === 'pending' ? row.action?.label || '查看' : '查看' }}
                </el-button>
                <span v-else class="wait-action">等待</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>

      <template v-else>
        <el-alert
          v-if="access.mode === 'wait'"
          class="mb-16"
          title="当前任务未流转到你，暂不能执行校验、解耦或求解。"
          type="info"
          :closable="false"
          show-icon
        />

        <div class="content-grid">
          <section class="section-block">
            <div class="section-header">
              <div>
                <p class="section-label">SUMMARY</p>
                <h2 class="section-title">目标约束汇总</h2>
              </div>
              <el-button plain @click="backToInbox">返回任务列表</el-button>
            </div>

            <el-collapse class="mt-12">
              <el-collapse-item v-for="group in detail.objectiveConstraints || []" :key="group.discipline" :title="group.disciplineName">
                <el-tag
                  v-for="item in group.items"
                  :key="item.itemCode"
                  class="tag-item"
                  :type="item.itemType === 'objective' ? 'success' : 'warning'"
                >
                  {{ item.itemName }}
                </el-tag>
              </el-collapse-item>
            </el-collapse>
          </section>

          <section class="section-block">
            <div class="section-header">
              <div>
                <p class="section-label">CONFLICT CHECK</p>
                <h2 class="section-title">目标约束冲突校验</h2>
              </div>
              <div class="action-row">
                <el-button :disabled="readonlyMode" @click="check(false)">模拟不通过</el-button>
                <el-button type="primary" :disabled="readonlyMode" @click="check(true)">执行校验</el-button>
              </div>
            </div>

            <el-result
              v-if="conflict.passed"
              icon="success"
              title="目标约束校验通过"
              sub-title="可进入模型解耦求解。"
            />
            <div v-else class="mt-12">
              <el-alert type="warning" title="发现目标 / 约束冲突" show-icon :closable="false" />
              <div class="table-shell">
                <el-table :data="conflict.conflicts || []" stripe class="platform-table">
                  <el-table-column label="冲突项" prop="title" min-width="170" />
                  <el-table-column label="影响学科" prop="disciplines" width="140" />
                  <el-table-column label="调整建议" prop="suggestion" min-width="220" />
                </el-table>
              </div>
            </div>
          </section>
        </div>

        <section v-if="showVariableSection" class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">DESIGN VARIABLES</p>
              <h2 class="section-title">设计变量统一选择</h2>
            </div>
            <div class="action-row">
              <el-button plain :disabled="readonlyMode" :loading="variableLoading" @click="loadVariableCatalogs">刷新变量</el-button>
              <el-button type="primary" :disabled="readonlyMode || !selectedVariableCount" :loading="variableSaving" @click="saveVariables">
                保存设计变量
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="readonlyMode"
            class="mb-16"
            :title="readonlyReason"
            type="info"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else-if="!selectedVariableCount"
            class="mb-16"
            title="请至少选择一个设计变量，保存后才能进行模型求解。"
            type="warning"
            :closable="false"
            show-icon
          />

          <el-collapse class="mt-12">
            <el-collapse-item v-for="group in variablesBySubtask" :key="group.subtaskCode" :title="group.subtaskName">
              <div class="table-shell">
                <el-table :data="group.items" stripe class="platform-table">
                  <el-table-column v-if="!readonlyMode" width="60">
                    <template #default="{ row }">
                      <el-checkbox v-model="row.checked" />
                    </template>
                  </el-table-column>
                  <el-table-column label="学科" prop="disciplineName" width="100" />
                  <el-table-column label="变量名称" prop="variableName" min-width="180" show-overflow-tooltip />
                  <el-table-column label="类型" width="100">
                    <template #default="{ row }">{{ variableTypeLabel(row.variableType) }}</template>
                  </el-table-column>
                  <el-table-column label="初始值" width="130">
                    <template #default="{ row }">
                      <el-input v-model="row.initialValue" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="下限" width="120">
                    <template #default="{ row }">
                      <el-input v-model="row.lowerBound" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="上限" width="120">
                    <template #default="{ row }">
                      <el-input v-model="row.upperBound" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="步长" width="110">
                    <template #default="{ row }">
                      <el-input v-model="row.stepValue" :disabled="readonlyMode" />
                    </template>
                  </el-table-column>
                  <el-table-column label="单位" prop="unit" width="90" />
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">SUBTASK DECOMPOSITION</p>
              <h2 class="section-title">解耦子任务</h2>
            </div>
            <div class="action-row">
              <el-button :disabled="readonlyMode || !variablesSaved || !selectedVariableCount" @click="decompose">任务解耦</el-button>
            </div>
          </div>

          <el-alert
            v-if="!variablesSaved"
            class="mb-16"
            title="请先完成并保存设计变量选择，再执行任务解耦。"
            type="info"
            :closable="false"
            show-icon
          />

          <div class="content-grid content-grid--balanced mt-12">
            <div v-for="subtask in subtasks" :key="subtask.subtaskCode" class="soft-panel">
              <div class="card-head">
                <h3 class="section-title">{{ subtask.subtaskName }}</h3>
                <el-tag>推荐 {{ subtask.recommended || '待求解' }}</el-tag>
              </div>
              <div class="subtask-group">
                <p class="subtask-group__title">归类目标</p>
                <el-tag
                  v-for="item in subtaskItems(subtask, 'objective')"
                  :key="`${subtask.subtaskCode}-${item.itemCode}-objective`"
                  class="tag-item"
                  type="success"
                >
                  {{ item.itemName }}
                </el-tag>
                <span v-if="!subtaskItems(subtask, 'objective').length" class="muted-text">暂无目标</span>
              </div>
              <div class="subtask-group">
                <p class="subtask-group__title">归类约束</p>
                <el-tag
                  v-for="item in subtaskItems(subtask, 'constraint')"
                  :key="`${subtask.subtaskCode}-${item.itemCode}-constraint`"
                  class="tag-item"
                  :type="item.shared ? 'danger' : 'warning'"
                >
                  {{ item.itemName }}{{ item.shared ? ' / 同时约束多个子任务' : '' }}
                </el-tag>
                <span v-if="!subtaskItems(subtask, 'constraint').length" class="muted-text">暂无约束</span>
              </div>
              <div class="subtask-group">
                <p class="subtask-group__title">关联设计变量</p>
                <el-tag
                  v-for="item in subtaskVariables(subtask)"
                  :key="`${subtask.subtaskCode}-${item.variableCode}`"
                  class="tag-item"
                  :type="item.shared ? 'danger' : 'info'"
                >
                  {{ item.variableName }}{{ item.shared ? ' / 共享变量' : '' }}
                </el-tag>
                <span v-if="!subtaskVariables(subtask).length" class="muted-text">保存变量后执行解耦</span>
              </div>
            </div>
          </div>
        </section>

        <section v-if="decomposed || hasSurrogateResult" class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">SOLVING RESULT</p>
              <h2 class="section-title">代理模型优化求解</h2>
            </div>
            <div class="action-row">
              <el-tag :type="surrogateStatusType">{{ surrogateSolve.statusLabel || '未提交' }}</el-tag>
              <el-button plain :loading="surrogateRefreshing" @click="refreshSurrogateSolve">刷新状态</el-button>
              <el-button type="primary" :disabled="readonlyMode || !canSolve" :loading="surrogateSubmitting" @click="solve">
                {{ hasSurrogateResult ? '重新优化' : '启动代理模型优化' }}
              </el-button>
              <el-button type="success" :disabled="readonlyMode || !canConfirmSurrogate" :loading="surrogateConfirming" @click="confirmSurrogate">
                确认最优方案
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="!canSolve"
            class="mb-16"
            :title="solveHint"
            type="info"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else-if="surrogateSolve.status === 'FAILED'"
            class="mb-16"
            :title="surrogateSolve.errorMessage || '代理模型求解失败'"
            type="error"
            :closable="false"
            show-icon
          />

          <div class="surrogate-grid mt-12">
            <div class="soft-panel">
              <div class="card-head">
                <h3 class="section-title">代理模型信息</h3>
                <el-tag>{{ surrogateSolve.objectiveUnit || 'MPa' }}</el-tag>
              </div>
              <div class="metric-list">
                <div><span>模型文件</span><strong>{{ surrogateSolve.modelName || 'aero_pipe_kriging.pkl' }}</strong></div>
                <div><span>模型类型</span><strong>{{ surrogateSolve.modelType || 'Kriging / Gaussian Process' }}</strong></div>
                <div><span>优化目标</span><strong>冲击应力最小</strong></div>
                <div><span>迭代次数</span><strong>{{ surrogateSolve.iterations || 0 }}</strong></div>
              </div>
            </div>

            <div class="soft-panel best-solution-panel">
              <div class="card-head">
                <h3 class="section-title">当前最优方案</h3>
                <el-tag :type="surrogateSolve.confirmed ? 'success' : 'info'">{{ surrogateSolve.confirmed ? '已确认' : '待确认' }}</el-tag>
              </div>
              <div v-if="bestSolutionReady" class="best-solution">
                <div><span>L1</span><strong>{{ best.L1 }}</strong><em>mm</em></div>
                <div><span>L2</span><strong>{{ best.L2 }}</strong><em>mm</em></div>
                <div><span>θ1</span><strong>{{ best.theta1 }}</strong><em>°</em></div>
                <div><span>θ2</span><strong>{{ best.theta2 }}</strong><em>°</em></div>
                <div><span>R</span><strong>{{ best.R }}</strong><em>mm</em></div>
                <div class="stress"><span>预测应力</span><strong>{{ best.predictedStress }}</strong><em>MPa</em></div>
              </div>
              <el-empty v-else description="请先启动代理模型优化" :image-size="80" />
            </div>
          </div>

          <div class="content-grid content-grid--balanced mt-12">
            <div class="soft-panel">
              <div class="card-head">
                <h3 class="section-title">Top 候选方案</h3>
                <el-tag>{{ (surrogateSolve.candidates || []).length }} 项</el-tag>
              </div>
              <div class="table-shell">
                <el-table :data="surrogateSolve.candidates || []" stripe class="platform-table">
                  <el-table-column label="排序" prop="rank" width="70" />
                  <el-table-column label="L1" prop="L1" width="90" />
                  <el-table-column label="L2" prop="L2" width="90" />
                  <el-table-column label="θ1" prop="theta1" width="80" />
                  <el-table-column label="θ2" prop="theta2" width="80" />
                  <el-table-column label="R" prop="R" width="80" />
                  <el-table-column label="预测应力 / MPa" prop="predictedStress" min-width="140" />
                </el-table>
              </div>
            </div>

            <div class="soft-panel">
              <div class="card-head">
                <h3 class="section-title">收敛历史</h3>
                <el-tag>{{ (surrogateSolve.history || []).length }} 次记录</el-tag>
              </div>
              <div v-if="(surrogateSolve.history || []).length" class="convergence-list">
                <div v-for="item in surrogateSolve.history.slice(-10)" :key="item.iteration" class="convergence-row">
                  <span>#{{ item.iteration }}</span>
                  <div><i :style="{ width: convergenceWidth(item.bestStress) }"></i></div>
                  <strong>{{ item.bestStress }}</strong>
                </div>
              </div>
              <el-empty v-else description="暂无迭代历史" :image-size="80" />
            </div>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  decomposeTask,
  getDashboard,
  getDesignTask,
  getDesignVariableCatalog,
  getSurrogateSolveTask,
  confirmSurrogateSolveTask,
  runConflictCheck,
  saveDesignVariables,
  submitSurrogateSolveTask
} from '@/api/designtask/optimization'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('模型解耦求解')
const detail = ref({})
const access = ref({ mode: 'wait', label: '等待' })
const conflict = ref({ passed: true, conflicts: [] })
const subtasks = ref([])
const surrogateSolve = ref({
  status: 'NOT_SUBMITTED',
  statusLabel: '未提交',
  modelName: 'aero_pipe_kriging.pkl',
  modelType: 'Kriging / Gaussian Process',
  objectiveName: 'predictedStress',
  objectiveUnit: 'MPa',
  bestSolution: {},
  candidates: [],
  history: [],
  iterations: 0,
  errorMessage: '',
  confirmed: false
})
const surrogateSubmitting = ref(false)
const surrogateRefreshing = ref(false)
const surrogateConfirming = ref(false)
let surrogatePollTimer = null
const decomposed = ref(false)
const designVariables = ref([])
const variablesSaved = ref(false)
const variableLoading = ref(false)
const variableSaving = ref(false)
const inboxLoading = ref(false)
const pendingTasks = ref([])
const handledTasks = ref([])
const relatedTasks = ref([])
const activeTab = ref(['handled', 'related'].includes(route.query.tab) ? route.query.tab : 'pending')

const hasTask = computed(() => !!taskId.value)
const readonlyMode = computed(() => access.value.mode !== 'enter' || route.query.mode === 'view')
const accessLabel = computed(() => access.value.label || (access.value.mode === 'enter' ? '可处理' : '查看'))
const readonlyReason = computed(() => {
  if (route.query.mode === 'view') return '当前以查看方式打开任务，设计变量只能查看，不能编辑。'
  return access.value.reason || '当前任务不在可处理状态，设计变量只能查看，不能编辑。'
})
const tabLabel = computed(() => ({ pending: '待处理', handled: '已处理', related: '相关任务' }[activeTab.value] || '我的任务'))
const visibleInboxTasks = computed(() => {
  if (activeTab.value === 'pending') return pendingTasks.value
  if (activeTab.value === 'handled') return handledTasks.value
  return relatedTasks.value
})
const selectedVariableCount = computed(() => designVariables.value.filter(item => item.checked).length)
const canSolve = computed(() => decomposed.value && variablesSaved.value && selectedVariableCount.value > 0)
const showVariableSection = computed(() => hasTask.value)
const hasSolutions = computed(() => subtasks.value.some(item => (item.solutions || []).length > 0))
const hasSurrogateResult = computed(() => surrogateSolve.value.status && surrogateSolve.value.status !== 'NOT_SUBMITTED')
const best = computed(() => surrogateSolve.value.bestSolution || {})
const bestSolutionReady = computed(() => Object.keys(best.value).length > 0)
const canConfirmSurrogate = computed(() => surrogateSolve.value.status === 'SUCCESS' && bestSolutionReady.value && !surrogateSolve.value.confirmed)
const surrogateStatusType = computed(() => {
  return {
    QUEUED: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    CONFIRMED: 'success',
    FAILED: 'danger'
  }[surrogateSolve.value.status] || 'info'
})
const solveHint = computed(() => {
  if (!variablesSaved.value || selectedVariableCount.value === 0) return '请先保存设计变量，再执行任务解耦和模型求解。'
  if (!decomposed.value) return '请先执行任务解耦，再进行模型求解。'
  return ''
})
const subtaskLabelFallback = {
  hydraulic_impact: '液压弯管抗冲击性能优化',
  cable_pipe_layout: '弯管路径几何与制造约束优化'
}
const variablesBySubtask = computed(() => {
  const subtaskNameMap = new Map(subtasks.value.map(item => [item.subtaskCode, item.subtaskName]))
  const groups = new Map()
  designVariables.value.forEach(item => {
    variableSubtaskCodes(item).forEach(subtaskCode => {
      if (!groups.has(subtaskCode)) {
        groups.set(subtaskCode, {
          subtaskCode,
          subtaskName: subtaskNameMap.get(subtaskCode) || subtaskLabelFallback[subtaskCode] || '未归类变量',
          items: []
        })
      }
      item.shared = variableSubtaskCodes(item).length > 1
      groups.get(subtaskCode).items.push(item)
    })
  })
  return Array.from(groups.values())
})

const disciplines = [
  { value: 'structure', label: '结构' },
  { value: 'layout', label: '布局' },
  { value: 'aero', label: '气动' },
  { value: 'hydraulic', label: '液压' },
  { value: 'manufacturing', label: '制造' }
]

function loadInbox() {
  inboxLoading.value = true
  if (activeTab.value === 'pending') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      pendingTasks.value = rows.filter(row => ['conflict_check', 'model_decompose_solve'].includes(row.currentNodeKey))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  if (activeTab.value === 'related') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      relatedTasks.value = rows
        .filter(row => ['conflict_check', 'model_decompose_solve', 'simulation_confirm', 'leader_approve', 'end'].includes(row.currentNodeKey))
        .map(row => ({
          ...row,
          action: { mode: 'view', label: '查看', reason: relatedReason(row.currentNodeKey) }
        }))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  getDashboard({ scope: 'related' }).then(async res => {
    const rows = res.data?.tasks || []
    const handled = []
    for (const row of rows) {
      const detailData = await getDesignTask(row.taskId).then(result => result.data || {}).catch(() => null)
      if (isSolvedByCurrentUser(detailData)) {
        handled.push({
          ...row,
          action: { mode: 'view', label: '查看', reason: '已完成模型解耦求解' },
          handledReason: '已完成模型解耦求解'
        })
      }
    }
    handledTasks.value = handled
  }).finally(() => {
    inboxLoading.value = false
  })
}

function relatedReason(nodeKey) {
  if (['simulation_confirm', 'leader_approve', 'end'].includes(nodeKey)) {
    return '可查看已生成的解耦求解结果'
  }
  if (nodeKey === 'model_decompose_solve') {
    return '解耦求解阶段，可查看当前归类结果'
  }
  return '等待解耦求解结果'
}

function isSolvedByCurrentUser(detailData) {
  if (!detailData?.task) return false
  const task = detailData.task
  const currentUserId = Number(detailData.currentUserId)
  const afterSolveNodes = ['simulation_confirm', 'leader_approve', 'end']
  return Number(task.ownerUserId) === currentUserId && afterSolveNodes.includes(task.currentNodeKey)
}

function changeTab() {
  router.replace({ path: '/designtask/solve', query: { tab: activeTab.value } })
  loadInbox()
}

function openTask(row) {
  const viewOnly = activeTab.value !== 'pending'
  router.push({
    path: '/designtask/solve',
    query: { taskId: row.taskId, mode: viewOnly ? 'view' : row.action?.mode || 'view', tab: activeTab.value }
  })
}

function backToInbox() {
  router.push({ path: '/designtask/solve', query: { tab: activeTab.value } })
}

function actionType(mode) {
  return { enter: 'primary', view: 'info', wait: 'info' }[mode] || 'info'
}

function loadDetail() {
  if (!taskId.value) {
    loadInbox()
    return
  }
  getDesignTask(taskId.value).then(res => {
    detail.value = res.data || {}
    taskTitle.value = detail.value.task?.taskName || '模型解耦求解'
    access.value = detail.value.access || { mode: 'wait', label: '等待' }
    conflict.value = detail.value.conflictCheck || conflict.value
    subtasks.value = detail.value.subtasks || []
    surrogateSolve.value = detail.value.surrogateSolve || surrogateSolve.value
    syncSurrogatePolling()
    loadSelectedVariables(detail.value)
    decomposed.value = Boolean(detail.value.decomposed)
    if (!readonlyMode.value) {
      loadVariableCatalogs()
    }
  })
}

function check(passed) {
  runConflictCheck(taskId.value, { passed }).then(res => {
    conflict.value = res.data || {}
    ElMessage[passed ? 'success' : 'warning'](passed ? '冲突校验通过' : '已模拟冲突校验不通过')
    loadDetail()
  })
}

function decompose() {
  if (!variablesSaved.value || selectedVariableCount.value === 0) {
    ElMessage.warning('请先保存设计变量，再执行任务解耦。')
    return
  }
  decomposeTask(taskId.value).then(res => {
    subtasks.value = res.data.subtasks || []
    decomposed.value = true
    loadVariableCatalogs()
    ElMessage.success('已解耦为两个固定子任务')
  })
}

function solve() {
  if (!canSolve.value) {
    ElMessage.warning(solveHint.value || '请先完成设计变量选择和任务解耦。')
    return
  }
  surrogateSubmitting.value = true
  submitSurrogateSolveTask(taskId.value, {
    maxIterations: 80,
    populationSize: 15,
    seed: 42
  }).then(res => {
    surrogateSolve.value = res.data || surrogateSolve.value
    ElMessage.success('代理模型优化任务已提交')
    syncSurrogatePolling()
  }).finally(() => {
    surrogateSubmitting.value = false
  })
}

function refreshSurrogateSolve() {
  if (!taskId.value) return
  surrogateRefreshing.value = true
  getSurrogateSolveTask(taskId.value).then(res => {
    surrogateSolve.value = res.data || surrogateSolve.value
    syncSurrogatePolling()
  }).finally(() => {
    surrogateRefreshing.value = false
  })
}

function confirmSurrogate() {
  if (!canConfirmSurrogate.value) {
    ElMessage.warning('请先完成代理模型优化求解。')
    return
  }
  surrogateConfirming.value = true
  confirmSurrogateSolveTask(taskId.value).then(res => {
    surrogateSolve.value = res.data || surrogateSolve.value
    ElMessage.success('已确认最优方案，流程进入仿真验证确认')
    stopSurrogatePolling()
    loadDetail()
  }).finally(() => {
    surrogateConfirming.value = false
  })
}

function syncSurrogatePolling() {
  if (['QUEUED', 'RUNNING'].includes(surrogateSolve.value.status)) {
    startSurrogatePolling()
  } else {
    stopSurrogatePolling()
  }
}

function startSurrogatePolling() {
  if (surrogatePollTimer || !taskId.value) return
  surrogatePollTimer = window.setInterval(() => {
    getSurrogateSolveTask(taskId.value).then(res => {
      surrogateSolve.value = res.data || surrogateSolve.value
      if (!['QUEUED', 'RUNNING'].includes(surrogateSolve.value.status)) {
        stopSurrogatePolling()
      }
    }).catch(() => {
      stopSurrogatePolling()
    })
  }, 2500)
}

function stopSurrogatePolling() {
  if (surrogatePollTimer) {
    window.clearInterval(surrogatePollTimer)
    surrogatePollTimer = null
  }
}

function convergenceWidth(value) {
  const history = surrogateSolve.value.history || []
  if (!history.length) return '12%'
  const values = history.map(item => Number(item.bestStress)).filter(item => Number.isFinite(item))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const current = Number(value)
  if (!Number.isFinite(current) || max === min) return '80%'
  const ratio = 1 - (current - min) / (max - min)
  return `${Math.max(12, Math.min(100, 18 + ratio * 82))}%`
}

function subtaskItems(subtask, itemType) {
  return (subtask.items || []).filter(item => item.itemType === itemType)
}

function subtaskVariables(subtask) {
  return designVariables.value
    .filter(item => item.checked && variableSubtaskCodes(item).includes(subtask.subtaskCode))
    .map(item => ({
      ...item,
      shared: variableSubtaskCodes(item).length > 1
    }))
}

function variableSubtaskCodes(item) {
  if (item.variableCode === 'PIPE_BEND_RADIUS' || item.subtaskCode === 'shared') {
    return ['hydraulic_impact', 'cable_pipe_layout']
  }
  return [item.subtaskCode || 'unassigned']
}

function loadSelectedVariables(data) {
  designVariables.value = (data.designVariables || []).flatMap(group => {
    const disciplineName = group.disciplineName || disciplineLabel(group.discipline)
    return (group.items || []).map(item => ({
      ...item,
      discipline: group.discipline,
      disciplineName,
      checked: true,
      initialValue: item.initialValue || item.defaultValue || ''
    }))
  })
  variablesSaved.value = designVariables.value.length > 0
}

function loadVariableCatalogs() {
  variableLoading.value = true
  const selectedMap = new Map(designVariables.value.map(item => [item.variableCode, item]))
  const hadSavedVariables = variablesSaved.value
  Promise.all(disciplines.map(item => getDesignVariableCatalog(item.value).then(res => ({
    discipline: item.value,
    disciplineName: item.label,
    rows: res.data || []
  })))).then(results => {
    designVariables.value = results.flatMap(group => group.rows.map(row => {
      const selected = selectedMap.get(row.variableCode)
      return {
        ...row,
        ...selected,
        discipline: group.discipline,
        disciplineName: group.disciplineName,
        checked: Boolean(selected?.checked)
      }
    }))
    variablesSaved.value = hadSavedVariables
  }).finally(() => {
    variableLoading.value = false
  })
}

function saveVariables() {
  const selected = Array.from(new Map(
    designVariables.value.filter(item => item.checked).map(item => [item.variableCode, item])
  ).values())
  if (!selected.length) {
    ElMessage.warning('请至少选择一个设计变量。')
    return
  }
  variableSaving.value = true
  saveDesignVariables(taskId.value, {
    designVariables: selected
  }).then(res => {
    detail.value = res.data || detail.value
    loadSelectedVariables(detail.value)
    decomposed.value = Boolean(detail.value.decomposed) || decomposed.value
    variablesSaved.value = true
    ElMessage.success('设计变量已保存，可以执行任务解耦。')
  }).finally(() => {
    variableSaving.value = false
  })
}

function disciplineLabel(value) {
  return disciplines.find(item => item.value === value)?.label || value
}

function variableTypeLabel(value) {
  return { continuous: '连续型', discrete: '离散型', enum: '枚举型' }[value] || value || '-'
}

onMounted(loadDetail)

onBeforeUnmount(() => {
  stopSurrogatePolling()
})

watch(() => route.query.taskId, value => {
  stopSurrogatePolling()
  taskId.value = value ? Number(value) : null
  activeTab.value = ['handled', 'related'].includes(route.query.tab) ? route.query.tab : activeTab.value
  loadDetail()
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.subtask-group {
  margin: 12px 0;
}

.subtask-group__title {
  margin: 0 0 8px;
  color: #5c6680;
  font-size: 13px;
  font-weight: 700;
}

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.surrogate-grid {
  display: grid;
  grid-template-columns: minmax(280px, 0.8fr) minmax(420px, 1.2fr);
  gap: 18px;
}

.metric-list {
  display: grid;
  gap: 12px;

  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 10px 12px;
    border: 1px solid rgba(128, 158, 195, 0.18);
    border-radius: 8px;
    background: rgba(248, 251, 255, 0.72);
  }

  span {
    color: #708198;
    font-size: 13px;
  }

  strong {
    min-width: 0;
    overflow: hidden;
    color: #24324f;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.best-solution {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;

  div {
    min-height: 82px;
    padding: 12px;
    border: 1px solid rgba(128, 158, 195, 0.18);
    border-radius: 8px;
    background: #f8fbff;
  }

  span,
  em {
    display: block;
    color: #708198;
    font-style: normal;
    font-size: 12px;
  }

  strong {
    display: block;
    margin: 5px 0;
    color: #1f2a44;
    font-size: 22px;
  }

  .stress {
    border-color: rgba(95, 147, 224, 0.35);
    background: #eef6ff;
  }
}

.convergence-list {
  display: grid;
  gap: 10px;
}

.convergence-row {
  display: grid;
  grid-template-columns: 54px 1fr 90px;
  gap: 10px;
  align-items: center;
  color: #5c6680;
  font-size: 13px;

  div {
    height: 8px;
    overflow: hidden;
    border-radius: 999px;
    background: #e8eef8;
  }

  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, #6fa7f7, #58c4a8);
  }

  strong {
    color: #24324f;
    text-align: right;
  }
}

@media (max-width: 1100px) {
  .surrogate-grid {
    grid-template-columns: 1fr;
  }

  .best-solution {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

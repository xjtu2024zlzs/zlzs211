<template>
  <div class="design-platform-view solve-industrial-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">MODEL DECOMPOSITION & SOLVING</p>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '模型解耦求解' }}</h1>
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
          <el-button plain icon="Refresh" :loading="inboxLoading" @click="loadInbox">刷新</el-button>
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
            <el-table-column label="备注" min-width="180">
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

        <div class="content-grid solve-command-grid">
          <section class="section-block objective-workbench">
            <div class="section-header">
              <div>
                <p class="section-label">SUMMARY</p>
                <h2 class="section-title">目标与约束归口确认</h2>
              </div>
              <div class="action-row">
                <el-tag type="success">{{ objectiveCount }} 个目标</el-tag>
                <el-tag type="warning">{{ constraintCount }} 个约束</el-tag>
                <el-tag type="info">权重 {{ objectiveWeightTotal }} / 10</el-tag>
              </div>
            </div>

            <div class="objective-workbench-toolbar">
              <span>目标权重 {{ objectiveWeightTotal }} / 10</span>
              <div>
                <el-button plain icon="Back" class="btn-soft-blue" @click="backToInbox">返回任务列表</el-button>
                <el-button plain icon="Operation" class="btn-soft-purple" :disabled="readonlyMode || !objectiveWeightRows.length" @click="normalizeObjectiveWeights">归一到 10</el-button>
                <el-button type="primary" icon="Check" class="btn-strong-blue" :disabled="!canEditObjectiveWeights || !objectiveWeightRows.length" :loading="objectiveWeightSaving" @click="saveObjectiveWeightSettings">
                  保存目标权重
                </el-button>
              </div>
            </div>

            <div class="objective-constraint-columns">
              <div class="oc-panel oc-panel--objective">
                <div class="oc-panel__head">
                  <strong><span class="panel-icon panel-icon--objective">O</span>优化目标池</strong>
                  <span class="panel-badge panel-badge--objective">{{ objectiveCount }} 个目标</span>
                </div>
                <div class="oc-card-list">
                  <div v-for="row in objectiveWeightRows" :key="`${row.discipline}-${row.itemCode}`" class="oc-item oc-item--objective">
                    <div class="oc-item__top">
                      <div>
                        <div class="oc-item__title">{{ row.itemName }}</div>
                        <div class="oc-item__meta">
                          <span :class="['discipline-pill', disciplinePillClass(row.discipline)]">{{ row.disciplineName }}</span>
                          <span>优化类型：{{ row.direction || '-' }}</span>
                        </div>
                      </div>
                      <span :class="['priority-badge', objectivePriorityClass(row.weight)]">{{ objectivePriorityLabel(row.weight) }}</span>
                    </div>
                    <p v-if="row.description || row.remark" class="oc-item__desc">{{ row.description || row.remark }}</p>
                    <div class="oc-item__footer">
                      <span class="selection-badge">已选择</span>
                      <div class="weight-editor weight-editor--card">
                        <span>权重</span>
                        <el-slider v-model="row.weight" :min="0" :max="10" :disabled="!canEditObjectiveWeights" />
                        <el-input-number v-model="row.weight" :min="0" :max="10" :precision="0" :disabled="!canEditObjectiveWeights" controls-position="right" />
                      </div>
                    </div>
                  </div>
                  <el-empty v-if="!objectiveWeightRows.length" description="暂无已选目标" :image-size="70" />
                </div>
              </div>

              <div class="oc-panel oc-panel--constraint">
                <div class="oc-panel__head">
                  <strong><span class="panel-icon panel-icon--constraint">C</span>设计约束池</strong>
                  <span class="panel-badge panel-badge--constraint">{{ constraintCount }} 个约束</span>
                </div>
                <div class="oc-card-list">
                  <div v-for="row in constraintRows" :key="`${row.discipline}-${row.itemCode}`" class="oc-item oc-item--constraint">
                    <div class="oc-item__top">
                      <div>
                        <div class="oc-item__title">{{ row.itemName }}</div>
                        <div class="oc-item__meta">
                          <span :class="['discipline-pill', disciplinePillClass(row.discipline)]">{{ row.disciplineName }}</span>
                          <span>约束关系：{{ row.direction || '-' }}</span>
                        </div>
                      </div>
                      <span class="constraint-value">{{ row.limitValue || '-' }} {{ row.unit || '' }}</span>
                    </div>
                    <p v-if="row.description || row.remark" class="oc-item__desc">{{ row.description || row.remark }}</p>
                    <div class="oc-item__footer">
                      <span class="selection-badge selection-badge--constraint">已选择</span>
                      <span class="compatibility-note">与前置节点约束兼容</span>
                    </div>
                  </div>
                  <el-empty v-if="!constraintRows.length" description="暂无已选约束" :image-size="70" />
                </div>
              </div>
            </div>
          </section>

          <section class="section-block conflict-panel">
            <div class="section-header">
              <div>
                <p class="section-label">CONFLICT CHECK</p>
                <h2 class="section-title">目标约束冲突校验</h2>
              </div>
              <div class="action-row">
                <el-button icon="Warning" class="btn-soft-amber" :disabled="!canCheckConflict" @click="check(false)">模拟不通过</el-button>
                <el-button type="success" icon="CircleCheck" :disabled="!canCheckConflict" @click="check(true)">执行校验</el-button>
              </div>
            </div>

            <el-result
              v-if="!conflict.checked"
              icon="info"
              title="待执行目标约束校验"
            />
            <el-result
              v-else-if="conflict.passed"
              icon="success"
              title="目标约束校验通过"
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

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">FIXED INPUTS</p>
              <h2 class="section-title">管段原始设计参数</h2>
            </div>
            <el-tag v-if="faultPipeParameters.setCode">{{ faultPipeParameters.setCode }}</el-tag>
          </div>

          <div v-if="faultPipeSummaryItems.length" class="fixed-input-meta">
            <div v-for="item in faultPipeSummaryItems" :key="item.label" class="fixed-input-meta__item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>

          <el-alert
            v-else
            class="mt-12"
            title="未读取到任务绑定的管段参数。"
            type="info"
            :closable="false"
            show-icon
          />

          <el-collapse v-if="faultPipeParameterGroups.length" class="mt-12">
            <el-collapse-item v-for="group in faultPipeParameterGroups" :key="group.groupCode" :title="group.groupName">
              <div class="table-shell">
                <el-table :data="group.items || []" stripe class="platform-table">
                  <el-table-column label="参数" prop="paramName" min-width="170" show-overflow-tooltip />
                  <el-table-column label="值 / 表达式" min-width="320" show-overflow-tooltip>
                    <template #default="{ row }">{{ row.formulaText || row.paramValue || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="单位" prop="paramUnit" width="110" />
                  <el-table-column label="说明" prop="description" min-width="220" show-overflow-tooltip />
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
              <el-button icon="Connection" class="btn-soft-green" :disabled="readonlyMode || decomposed" @click="decompose">{{ decomposed ? '已解耦' : '任务解耦' }}</el-button>
            </div>
          </div>

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
                <span v-if="!subtaskVariables(subtask).length" class="muted-text">解耦后在下方选择设计变量</span>
              </div>
            </div>
          </div>
        </section>

        <section v-if="showVariableSection" class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">DESIGN VARIABLES</p>
              <h2 class="section-title">设计变量统一选择</h2>
            </div>
            <div class="action-row">
              <el-button plain icon="Refresh" class="btn-soft-blue" :disabled="readonlyMode" :loading="variableLoading" @click="loadVariableCatalogs">刷新变量</el-button>
              <el-button type="primary" icon="Check" class="btn-strong-blue" :disabled="readonlyMode || !selectedVariableCount" :loading="variableSaving" @click="saveVariables">
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

        <section v-if="decomposed || hasSurrogateResult" class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">SOLVING RESULT</p>
              <h2 class="section-title">代理模型优化求解</h2>
            </div>
            <div class="action-row">
              <el-tag :type="surrogateStatusType">{{ surrogateSolve.statusLabel || '未提交' }}</el-tag>
              <el-button plain icon="Refresh" class="btn-soft-blue" :loading="surrogateRefreshing" @click="refreshSurrogateSolve">刷新状态</el-button>
              <el-button type="primary" icon="CaretRight" :disabled="readonlyMode || !canSolve" :loading="surrogateSubmitting" @click="solve">
                {{ hasSurrogateResult ? '重新优化' : '启动代理模型优化' }}
              </el-button>
              <el-button type="success" icon="Select" :disabled="readonlyMode || !canConfirmSurrogate" :loading="surrogateConfirming" @click="confirmSurrogate">
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
              <el-empty v-else description="暂无优化结果" :image-size="80" />
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
  saveObjectiveWeights,
  saveDesignVariables,
  submitSurrogateSolveTask
} from '@/api/designtask/optimization'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('模型解耦求解')
const detail = ref({})
const access = ref({ mode: 'wait', label: '等待' })
const conflict = ref({ checked: false, passed: false, conflicts: [] })
const subtasks = ref([])
const faultPipeParameters = ref({ groups: [] })
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
const objectiveWeightRows = ref([])
const objectiveWeightSaving = ref(false)
const inboxLoading = ref(false)
const pendingTasks = ref([])
const handledTasks = ref([])
const relatedTasks = ref([])
const activeTab = ref(['handled', 'related'].includes(route.query.tab) ? route.query.tab : 'pending')

const hasTask = computed(() => !!taskId.value)
const readonlyMode = computed(() => access.value.mode !== 'enter' || route.query.mode === 'view')
const currentNodeKey = computed(() => detail.value.nodeKey || detail.value.task?.currentNodeKey || '')
const canCheckConflict = computed(() => !readonlyMode.value && currentNodeKey.value === 'conflict_check')
const canEditObjectiveWeights = computed(() => !readonlyMode.value && ['conflict_check', 'model_decompose_solve'].includes(currentNodeKey.value))
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
const showVariableSection = computed(() => hasTask.value && decomposed.value)
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
  if (!decomposed.value) return '请先执行任务解耦，再进行模型求解。'
  if (!variablesSaved.value || selectedVariableCount.value === 0) return '请先按解耦子任务选择并保存设计变量，再进行模型求解。'
  return ''
})
const subtaskLabelFallback = {
  hydraulic_impact: '液压弯管抗冲击性能优化',
  cable_pipe_layout: '线缆管路布局设计'
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
const faultPipeParameterGroups = computed(() => faultPipeParameters.value.groups || [])
const faultPipeSummaryItems = computed(() => {
  return [
    { label: '参数集', value: faultPipeParameters.value.setName },
    { label: '管段编号', value: faultPipeParameters.value.faultSegmentName },
    { label: '材料', value: faultPipeParameters.value.materialName }
  ].filter(item => item.value)
})
const objectiveWeightTotal = computed(() => objectiveWeightRows.value.reduce((sum, item) => sum + Number(item.weight || 0), 0))
const objectiveSummaryGroups = computed(() => {
  return (detail.value.objectiveConstraints || []).map(group => {
    const items = group.items || []
    return {
      ...group,
      objectives: items.filter(item => item.itemType === 'objective'),
      constraints: items.filter(item => item.itemType === 'constraint')
    }
  })
})
const objectiveCount = computed(() => objectiveSummaryGroups.value.reduce((sum, group) => sum + group.objectives.length, 0))
const constraintCount = computed(() => objectiveSummaryGroups.value.reduce((sum, group) => sum + group.constraints.length, 0))
const constraintRows = computed(() => {
  return objectiveSummaryGroups.value.flatMap(group => {
    const disciplineName = group.disciplineName || disciplineLabel(group.discipline)
    return group.constraints.map(item => ({
      ...item,
      discipline: group.discipline,
      disciplineName
    }))
  })
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
    faultPipeParameters.value = detail.value.faultPipeParameters || { groups: [] }
    surrogateSolve.value = detail.value.surrogateSolve || surrogateSolve.value
    loadObjectiveWeights(detail.value)
    syncSurrogatePolling()
    loadSelectedVariables(detail.value)
    decomposed.value = Boolean(detail.value.decomposed)
    if (!readonlyMode.value && decomposed.value) {
      loadVariableCatalogs()
    }
  })
}

function check(passed) {
  if (!canCheckConflict.value) {
    ElMessage.warning('当前节点不可执行目标约束校验。')
    return
  }
  runConflictCheck(taskId.value, { passed }).then(res => {
    conflict.value = res.data || {}
    ElMessage[passed ? 'success' : 'warning'](passed ? '冲突校验通过' : '已模拟冲突校验不通过')
    loadDetail()
  })
}

function decompose() {
  decomposeTask(taskId.value).then(res => {
    subtasks.value = res.data.subtasks || []
    decomposed.value = true
    loadVariableCatalogs()
    ElMessage.success('已解耦为两个子任务，请继续选择设计变量。')
  })
}

function loadObjectiveWeights(data) {
  objectiveWeightRows.value = (data.objectiveConstraints || []).flatMap(group => {
    return (group.items || [])
      .filter(item => item.itemType === 'objective')
      .map(item => ({
        ...item,
        discipline: group.discipline,
        disciplineName: group.disciplineName || disciplineLabel(group.discipline),
        weight: normalizeDisplayWeight(item.weight)
      }))
  })
}

function normalizeObjectiveWeights() {
  const rows = objectiveWeightRows.value
  if (!rows.length) return
  const base = Math.floor(10 / rows.length)
  let remain = 10 - base * rows.length
  rows.forEach(row => {
    row.weight = base + (remain > 0 ? 1 : 0)
    remain -= 1
  })
}

function saveObjectiveWeightSettings() {
  if (!canEditObjectiveWeights.value) {
    ElMessage.warning('当前节点不可归口目标权重。')
    return
  }
  objectiveWeightSaving.value = true
  saveObjectiveWeights(taskId.value, {
    items: objectiveWeightRows.value.map(item => ({
      discipline: item.discipline,
      itemCode: item.itemCode,
      weight: normalizeDisplayWeight(item.weight)
    }))
  }).then(res => {
    detail.value = res.data || detail.value
    loadObjectiveWeights(detail.value)
    ElMessage.success('目标权重已统一保存。')
  }).finally(() => {
    objectiveWeightSaving.value = false
  })
}

function normalizeDisplayWeight(value) {
  const number = Number(value)
  if (!Number.isFinite(number)) return 5
  if (number > 10) return Math.max(0, Math.min(10, Math.round(number / 10)))
  return Math.max(0, Math.min(10, Math.round(number)))
}

function objectivePriorityLabel(weight) {
  const value = Number(weight)
  if (value >= 8) return '高优先级'
  if (value >= 5) return '中优先级'
  return '低优先级'
}

function objectivePriorityClass(weight) {
  const value = Number(weight)
  if (value >= 8) return 'priority-badge--high'
  if (value >= 5) return 'priority-badge--medium'
  return 'priority-badge--low'
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
  if (item.subtaskCode === 'shared') {
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
    ElMessage.success('设计变量已保存，可以执行模型求解。')
  }).finally(() => {
    variableSaving.value = false
  })
}

function disciplineLabel(value) {
  return disciplines.find(item => item.value === value)?.label || value
}

function disciplinePillClass(value) {
  return `discipline-pill--${value || 'default'}`
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

.solve-industrial-view {
  background:
    linear-gradient(90deg, rgba(18, 73, 119, 0.08), transparent 260px),
    linear-gradient(180deg, #f6f8fb 0%, #edf2f7 100%);

  .design-platform-shell {
    gap: 14px;
  }

  .platform-topbar {
    min-height: 104px;
    padding: 20px 24px;
    border: 1px solid #d8e0ea;
    border-radius: 18px;
    background: linear-gradient(135deg, #ffffff 0%, #f7fbff 100%);
    box-shadow: 0 8px 22px rgba(38, 65, 92, 0.08);
  }

  .platform-eyebrow,
  .section-label {
    margin-bottom: 6px;
    color: #176db6;
    letter-spacing: 0.06em;
  }

  .platform-title {
    font-size: 28px;
  }

  .meta-chip {
    min-height: 34px;
    border-radius: 999px;
    background: #f7f9fc;
  }

  .section-block {
    padding: 15px;
    border-color: #d8e0ea;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: 0 4px 14px rgba(49, 76, 108, 0.06);
  }

  .section-header {
    min-height: 38px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e6ebf1;
  }

  .section-title {
    font-size: 18px;
  }

  .table-shell {
    border-color: #e1e7ef;
    border-radius: 6px;
    background: #ffffff;
  }

  .soft-panel {
    position: relative;
    overflow: hidden;
    border-color: #dfe6ef;
    border-radius: 6px;
    background: #ffffff;

    &::before {
      position: absolute;
      top: 0;
      right: 0;
      left: 0;
      height: 3px;
      background: #4f8edc;
      content: "";
    }
  }

  :deep(.el-button) {
    border-radius: 4px;
  }

  :deep(.el-tag) {
    border-radius: 4px;
  }

  :deep(.el-alert) {
    border-radius: 6px;
  }

  :deep(.el-collapse) {
    --el-collapse-header-bg-color: #ffffff;
    --el-collapse-content-bg-color: #ffffff;
    border-top: 0;
  }

  :deep(.el-collapse-item__header) {
    padding: 0 10px;
    border-bottom-color: #e6ebf1;
    color: #273d5b;
    font-weight: 600;
  }

  :deep(.el-collapse-item__content) {
    padding: 10px;
  }

  :deep(.el-table__cell) {
    padding: 8px 0;
  }
}

.content-grid--balanced {
  gap: 14px;
}

.card-head {
  min-height: 44px;
  margin: -2px -2px 12px;
  padding: 10px 12px;
  border-bottom: 1px solid #e6ebf1;
  background: #f8fafc;

  .section-title {
    font-size: 15px;
  }
}

.subtask-group {
  margin: 10px 0;
  padding: 0 2px;
}

.subtask-group__title {
  margin: 0 0 7px;
  color: #40556f;
  font-size: 12px;
  font-weight: 700;
}

.tag-item {
  margin: 0 6px 6px 0;
  border-radius: 4px;
}

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.solve-command-grid {
  grid-template-columns: minmax(720px, 1.42fr) minmax(360px, 0.58fr);
  align-items: start;
}

.objective-workbench {
  min-width: 0;
}

.objective-workbench-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 10px 0 12px;
  padding: 9px 12px;
  border: 1px solid #e2e8f0;
  border-left: 3px solid #4f8edc;
  border-radius: 6px;
  background: #f8fafc;

  span {
    color: #52667a;
    font-size: 13px;
  }

  div {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }
}

.objective-constraint-columns {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(0, 0.88fr);
  gap: 14px;
}

.oc-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  border: 1px solid #dfe6ef;
  border-radius: 6px;
  background: #ffffff;
}

.oc-panel--objective {
  border-top: 3px solid #2f80d8;
}

.oc-panel--constraint {
  border-top: 3px solid #d9822b;
}

.oc-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-bottom: 1px solid #e6ebf1;
  background: #f8fafc;

  strong {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #19375a;
    font-size: 15px;
  }

  span {
    color: #75869b;
    font-size: 12px;
  }
}

.panel-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 800;
}

.panel-icon--objective {
  background: #2f80d8;
}

.panel-icon--constraint {
  background: #d9822b;
}

.panel-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.panel-badge--objective {
  color: #176db6;
  background: #eaf4ff;
}

.panel-badge--constraint {
  color: #9a5a10;
  background: #fff3dd;
}

.oc-card-list {
  display: grid;
  gap: 9px;
  max-height: 368px;
  padding: 10px 12px 12px;
  overflow-y: auto;
  scrollbar-gutter: stable;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-thumb {
    border: 2px solid #f8fafc;
    border-radius: 999px;
    background: #c7d6e8;
  }
}

.oc-item {
  position: relative;
  padding: 10px 12px 10px 14px;
  border: 1px solid #e6ebf1;
  border-left-width: 4px;
  border-radius: 6px;
  background: #fbfcfe;
  color: #32415f;
  transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    border-color: #c7d6e8;
    background: #ffffff;
    box-shadow: 0 6px 16px rgba(36, 63, 94, 0.08);
  }
}

.oc-item--objective {
  border-left-color: #2f80d8;
}

.oc-item--constraint {
  border-left-color: #d9822b;
}

.oc-item__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.oc-item__title {
  color: #19375a;
  font-size: 14px;
  font-weight: 700;
}

.oc-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 5px;
  color: #64748b;
  font-size: 12px;
}

.oc-item__desc {
  display: -webkit-box;
  margin: 7px 0 0;
  overflow: hidden;
  color: #5e6c7d;
  font-size: 12px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.oc-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.priority-badge,
.constraint-value,
.selection-badge {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.priority-badge--high {
  color: #c73535;
  background: #fff0f0;
}

.priority-badge--medium {
  color: #a56511;
  background: #fff5dc;
}

.priority-badge--low {
  color: #16814f;
  background: #edf9f1;
}

.constraint-value {
  color: #9a5a10;
  background: #fff3dd;
}

.selection-badge {
  color: #176db6;
  background: #eaf4ff;
}

.selection-badge--constraint {
  color: #9a5a10;
  background: #fff3dd;
}

.compatibility-note {
  color: #76879b;
  font-size: 12px;
}

.discipline-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  min-width: 52px;
  max-width: 76px;
  padding: 3px 8px;
  overflow: hidden;
  border: 1px solid #cfd8e3;
  border-radius: 999px;
  color: #415166;
  background: #f7f9fc;
  font-size: 12px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.discipline-pill--structure {
  border-color: #bfd8ff;
  color: #176db6;
  background: #eef6ff;
}

.discipline-pill--layout {
  border-color: #b8e3df;
  color: #08766d;
  background: #edfafa;
}

.discipline-pill--aero {
  border-color: #d8ccff;
  color: #6547b8;
  background: #f5f1ff;
}

.discipline-pill--hydraulic {
  border-color: #f3d0a2;
  color: #9a5a10;
  background: #fff5e8;
}

.discipline-pill--manufacturing {
  border-color: #bfe5c8;
  color: #16814f;
  background: #effaf2;
}

.discipline-pill--default {
  border-color: #cfd8e3;
  color: #415166;
  background: #f7f9fc;
}

.btn-soft-blue {
  border-color: #c9ddff;
  color: #176db6;
  background: #f1f7ff;
}

.btn-soft-green {
  border-color: #c8e8d7;
  color: #16814f;
  background: #f0fbf5;
}

.btn-soft-amber {
  border-color: #f4d6a7;
  color: #a56511;
  background: #fff8ed;
}

.btn-soft-purple {
  border-color: #d9ccff;
  color: #6b4cc2;
  background: #f6f2ff;
}

.btn-strong-blue {
  border-color: #176db6;
  background: #176db6;
}

.fixed-input-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin-top: 12px;
  padding: 9px 12px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

.fixed-input-meta__item {
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 0 18px;
  border-right: 1px solid #e2e8f0;

  &:first-child {
    padding-left: 0;
  }

  &:last-child {
    border-right: 0;
  }

  span {
    flex-shrink: 0;
    margin-right: 8px;
    color: #708198;
    font-size: 12px;
  }

  strong {
    min-width: 0;
    overflow: hidden;
    color: #24324f;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.weight-editor {
  display: grid;
  grid-template-columns: auto minmax(100px, 150px) 76px;
  gap: 8px;
  align-items: center;
  min-width: 238px;

  span {
    color: #52667a;
    font-size: 12px;
    font-weight: 700;
  }

  :deep(.el-slider) {
    --el-slider-main-bg-color: #176db6;
  }

  :deep(.el-input-number) {
    width: 76px;
  }
}

.weight-editor--card {
  flex: 1;
  max-width: 310px;
}

.surrogate-grid {
  display: grid;
  grid-template-columns: minmax(280px, 0.8fr) minmax(420px, 1.2fr);
  gap: 14px;
}

.metric-list {
  display: grid;
  gap: 12px;

  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 9px 12px;
    border: 1px solid #e6ebf1;
    border-left: 3px solid #4f8edc;
    border-radius: 6px;
    background: #fbfcfe;
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
    padding: 11px 12px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #fbfcfe;
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
    border-left: 3px solid #4f8edc;
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
  padding: 8px 10px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  color: #5c6680;
  font-size: 13px;
  background: #fbfcfe;

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
  .solve-command-grid,
  .objective-constraint-columns {
    grid-template-columns: 1fr;
  }

  .objective-workbench-toolbar {
    align-items: flex-start;
    flex-direction: column;

    div {
      justify-content: flex-start;
    }
  }

  .surrogate-grid {
    grid-template-columns: 1fr;
  }

  .best-solution {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .weight-editor {
    grid-template-columns: 1fr;
  }
}
</style>

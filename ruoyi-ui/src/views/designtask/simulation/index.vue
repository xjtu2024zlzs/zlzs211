<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">SIMULATION CONFIRMATION</p>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '仿真验证确认' }}</h1>
          <p v-if="!hasTask" class="platform-subtitle">
            请选择一个仿真确认或领导审批相关任务。
          </p>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>{{ hasTask ? accessLabel : tabLabel }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>仿真与审批</span>
          </div>
        </div>
      </section>

      <section v-if="!hasTask" class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">TASK INBOX</p>
            <h2 class="section-title">我的仿真验证确认任务</h2>
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
          title="当前任务未流转到你，暂不能执行仿真确认或审批。"
          type="info"
          :closable="false"
          show-icon
        />

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">CAD MODELING</p>
              <h2 class="section-title">CAD 参数建模验证</h2>
            </div>
            <div class="action-row">
              <el-tag :type="cadStatusType(cadModel.status)" effect="light">{{ cadModel.statusLabel || '未提交' }}</el-tag>
              <el-button plain :loading="cadRefreshing" @click="refreshCadModel">刷新状态</el-button>
              <el-button type="primary" :loading="cadSubmitting" :disabled="!canEditCad" @click="submitCadModel">
                {{ cadModel.status === 'SUCCESS' ? '重新生成' : '生成 CAD 模型' }}
              </el-button>
            </div>
          </div>

          <div class="cad-model-grid">
            <div class="cad-form-panel">
              <el-form :model="cadForm" label-width="120px" class="cad-param-form">
                <el-form-item label="L1 / mm">
                  <el-input-number v-model="cadForm.L1" :min="1" :precision="2" :step="10" :disabled="!canEditCad" controls-position="right" />
                </el-form-item>
                <el-form-item label="L2 / mm">
                  <el-input-number v-model="cadForm.L2" :min="1" :precision="2" :step="10" :disabled="!canEditCad" controls-position="right" />
                </el-form-item>
                <el-form-item label="R / mm">
                  <el-input-number v-model="cadForm.R" :min="1" :precision="2" :step="1" :disabled="!canEditCad" controls-position="right" />
                </el-form-item>
                <el-form-item label="θ1 / °">
                  <el-input-number v-model="cadForm.theta1" :precision="2" :step="5" :disabled="!canEditCad" controls-position="right" />
                </el-form-item>
                <el-form-item label="θ2 / °">
                  <el-input-number v-model="cadForm.theta2" :precision="2" :step="5" :disabled="!canEditCad" controls-position="right" />
                </el-form-item>
              </el-form>

              <div class="cad-constraint-list">
                <div><span>水平总长</span><strong>600 mm</strong></div>
                <div><span>竖直总高</span><strong>300 mm</strong></div>
                <div><span>管径</span><strong>30 mm</strong></div>
              </div>
            </div>

            <div class="cad-result-panel">
              <div class="cad-result-grid">
                <div>
                  <span>L3</span>
                  <strong>{{ valueOrDash(cadModel.l3, ' mm') }}</strong>
                </div>
                <div>
                  <span>起始方向角</span>
                  <strong>{{ valueOrDash(cadModel.initialAngle, ' °') }}</strong>
                </div>
                <div>
                  <span>几何闭合状态</span>
                  <strong>{{ cadModel.closureStatus || '-' }}</strong>
                </div>
              </div>

              <el-alert
                v-if="cadModel.status === 'FAILED'"
                class="mt-12"
                :title="cadModel.errorMessage || 'CAD 建模失败'"
                type="error"
                show-icon
                :closable="false"
              />

              <div class="cad-file-actions">
                <el-button :disabled="!cadModel.files?.sldprt" @click="downloadCadFile('sldprt')">下载 SLDPRT</el-button>
                <el-button :disabled="!cadModel.files?.stl" @click="downloadCadFile('stl')">下载 STL</el-button>
              </div>

              <cad-stl-viewer :model-data="cadStlData" :empty-text="cadEmptyText" />
            </div>
          </div>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">ANSYS SIMULATION</p>
              <h2 class="section-title">ANSYS 应力仿真结果</h2>
            </div>
            <div class="action-row">
              <el-tag :type="ansysStatusType(ansysSimulation.status)">{{ ansysSimulation.statusLabel || '未提交' }}</el-tag>
              <el-button plain :loading="ansysRefreshing" @click="refreshAnsysSimulation">刷新状态</el-button>
              <el-button type="primary" :loading="ansysSubmitting" :disabled="!canSimulate" @click="startAnsysSimulation">
                开始 ANSYS 仿真
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="ansysSimulation.placeholder && ansysSimulation.status === 'SUCCESS'"
            class="mb-16"
            title="当前显示的是预置应力云图；接入 ANSYS Worker 后将替换为真实仿真结果。"
            type="info"
            :closable="false"
            show-icon
          />
          <el-alert
            v-if="ansysSimulation.status === 'FAILED'"
            class="mb-16"
            :title="ansysSimulation.errorMessage || 'ANSYS 仿真失败'"
            type="error"
            :closable="false"
            show-icon
          />

          <div class="ansys-result-grid">
            <div class="table-shell">
              <el-table :data="ansysSimulation.metrics || []" stripe class="platform-table">
                <el-table-column label="指标" prop="name" min-width="160" />
                <el-table-column label="数值" prop="value" width="130" />
                <el-table-column label="单位" prop="unit" width="90" />
                <el-table-column label="来源" prop="source" min-width="160" show-overflow-tooltip />
              </el-table>
            </div>
            <div class="ansys-image-panel">
              <img
                v-if="ansysSimulation.status && ansysSimulation.status !== 'NOT_SUBMITTED'"
                :src="ansysStressImage"
                alt="ANSYS stress contour placeholder"
              />
              <span v-else>点击“开始 ANSYS 仿真”后展示应力云图</span>
            </div>
          </div>
        </section>

        <div class="content-grid">
          <section class="section-block">
            <div class="section-header">
              <div>
                <p class="section-label">SIMULATION INPUTS</p>
                <h2 class="section-title">故障管段原始设计参数</h2>
              </div>
              <el-tag>{{ faultPipeParameters.setCode || 'FAULT_PIPE_DEFAULT_001' }}</el-tag>
            </div>

            <div class="fixed-input-summary">
              <div><span>参数集</span><strong>{{ faultPipeParameters.setName || '-' }}</strong></div>
              <div><span>故障管段</span><strong>{{ faultPipeParameters.faultSegmentName || '-' }}</strong></div>
              <div><span>材料</span><strong>{{ faultPipeParameters.materialName || '-' }}</strong></div>
            </div>

            <el-collapse class="mt-12">
              <el-collapse-item v-for="group in faultPipeParameterGroups" :key="group.groupCode" :title="group.groupName">
                <div class="table-shell">
                  <el-table :data="group.items || []" stripe class="platform-table">
                    <el-table-column label="参数" prop="paramName" min-width="160" show-overflow-tooltip />
                    <el-table-column label="值 / 表达式" min-width="300" show-overflow-tooltip>
                      <template #default="{ row }">{{ row.formulaText || row.paramValue || '-' }}</template>
                    </el-table-column>
                    <el-table-column label="单位" prop="paramUnit" width="100" />
                  </el-table>
                </div>
              </el-collapse-item>
            </el-collapse>
          </section>

          <section class="section-block">
            <div class="section-header">
              <div>
                <p class="section-label">METRICS</p>
                <h2 class="section-title">优化前后指标对比</h2>
              </div>
              <div class="action-row">
                <el-button plain @click="backToInbox">返回任务列表</el-button>
                <el-button type="primary" :disabled="!canSimulate" @click="simulate">Mock 仿真</el-button>
              </div>
            </div>

            <div class="table-shell">
              <el-table :data="simulation.metrics || []" stripe class="platform-table">
                <el-table-column label="指标" prop="name" min-width="160" />
                <el-table-column label="优化前" prop="before" />
                <el-table-column label="优化后" prop="after" />
                <el-table-column label="单位" prop="unit" />
                <el-table-column label="趋势" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.trend === 'up' ? 'success' : 'primary'">{{ row.trend === 'up' ? '提升' : '降低' }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>

          <aside class="side-stack">
            <section class="section-block">
              <div class="section-header">
                <div>
                  <p class="section-label">CONCLUSION</p>
                  <h2 class="section-title">验证结论</h2>
                </div>
              </div>
              <el-result
                :icon="simulation.passed ? 'success' : 'warning'"
                :title="simulation.passed ? '仿真验证通过' : '仿真验证未通过'"
                :sub-title="simulation.conclusion"
              />
            </section>
          </aside>
        </div>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">APPROVAL</p>
              <h2 class="section-title">领导审批</h2>
            </div>
          </div>

          <el-form :model="approval" label-width="90px" class="mt-12">
            <el-form-item label="审批结论">
              <el-radio-group v-model="approval.approved" :disabled="!canApprove">
                <el-radio :value="true">通过</el-radio>
                <el-radio :value="false">退回模型解耦</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见">
              <el-input v-model="approval.comment" type="textarea" :rows="4" :disabled="!canApprove" />
            </el-form-item>
            <el-button type="primary" :disabled="!canApprove" @click="submitApproval">提交审批</el-button>
          </el-form>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  approveTask,
  getAnsysSimulationTask,
  getCadModelFile,
  getCadModelTask,
  getDashboard,
  getDesignTask,
  runSimulation,
  submitAnsysSimulationTask,
  submitCadModelTask
} from '@/api/designtask/optimization'
import CadStlViewer from './CadStlViewer.vue'
import ansysStressPlaceholder from '@/assets/designtask/ansys-stress-placeholder.png'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('仿真验证确认')
const currentNodeKey = ref('')
const access = ref({ mode: 'wait', label: '等待' })
const simulation = ref({ passed: true, metrics: [], conclusion: '' })
const ansysSimulation = ref({ status: 'NOT_SUBMITTED', statusLabel: '未提交', metrics: [], placeholder: true })
const faultPipeParameters = ref({ groups: [] })
const approval = ref({ approved: true, comment: '仿真验证结果满足设计要求，同意通过。' })
const cadForm = ref({ L1: 280, L2: 150, R: 20, theta1: 110, theta2: 120 })
const cadModel = ref({ status: 'NOT_SUBMITTED', statusLabel: '未提交', params: {}, files: {} })
const cadStlData = ref(null)
const cadSubmitting = ref(false)
const cadRefreshing = ref(false)
const ansysSubmitting = ref(false)
const ansysRefreshing = ref(false)
let cadPollTimer = null
const inboxLoading = ref(false)
const pendingTasks = ref([])
const handledTasks = ref([])
const relatedTasks = ref([])
const activeTab = ref(['handled', 'related'].includes(route.query.tab) ? route.query.tab : 'pending')

const hasTask = computed(() => !!taskId.value)
const accessLabel = computed(() => access.value.label || (access.value.mode === 'enter' ? '可处理' : '查看'))
const tabLabel = computed(() => ({ pending: '待处理', handled: '已处理', related: '相关任务' }[activeTab.value] || '我的任务'))
const visibleInboxTasks = computed(() => {
  if (activeTab.value === 'pending') return pendingTasks.value
  if (activeTab.value === 'handled') return handledTasks.value
  return relatedTasks.value
})
const viewOnly = computed(() => route.query.mode === 'view')
const canSimulate = computed(() => !viewOnly.value && access.value.mode === 'enter' && currentNodeKey.value === 'simulation_confirm')
const canEditCad = computed(() => !viewOnly.value && access.value.mode === 'enter')
const canApprove = computed(() => !viewOnly.value && access.value.mode === 'enter' && currentNodeKey.value === 'leader_approve')
const faultPipeParameterGroups = computed(() => faultPipeParameters.value.groups || [])
const ansysStressImage = computed(() => ansysSimulation.value.stressImageUrl || ansysStressPlaceholder)
const cadEmptyText = computed(() => {
  if (cadModel.value.status === 'FAILED') return cadModel.value.errorMessage || 'CAD 建模失败'
  if (['QUEUED', 'RUNNING'].includes(cadModel.value.status)) return 'CAD 模型生成中'
  return '请先生成 CAD 模型'
})

function loadInbox() {
  inboxLoading.value = true
  if (activeTab.value === 'pending') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      pendingTasks.value = rows.filter(row => ['simulation_confirm', 'leader_approve'].includes(row.currentNodeKey))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  if (activeTab.value === 'related') {
    getDashboard({ scope: 'related' }).then(res => {
      const rows = res.data?.tasks || []
      relatedTasks.value = rows
        .filter(row => ['simulation_confirm', 'leader_approve', 'end'].includes(row.currentNodeKey))
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
      const reason = handledReason(detailData)
      if (reason) {
        handled.push({
          ...row,
          action: { mode: 'view', label: '查看', reason },
          handledReason: reason
        })
      }
    }
    handledTasks.value = handled
  }).finally(() => {
    inboxLoading.value = false
  })
}

function relatedReason(nodeKey) {
  if (nodeKey === 'end') {
    return '可查看最终仿真与审批结果'
  }
  if (nodeKey === 'leader_approve') {
    return '可查看仿真确认结果，等待审批完成'
  }
  return '仿真确认阶段，可查看当前指标结果'
}

function handledReason(detailData) {
  if (!detailData?.task) return ''
  const task = detailData.task
  const currentUserId = Number(detailData.currentUserId)
  if (Number(task.manufacturingUserId) === currentUserId && ['leader_approve', 'end'].includes(task.currentNodeKey)) {
    return '已完成仿真确认'
  }
  if (Number(task.leaderUserId) === currentUserId && task.currentNodeKey === 'end') {
    return '已完成领导审批'
  }
  return ''
}

function changeTab() {
  router.replace({ path: '/designtask/simulation', query: { tab: activeTab.value } })
  loadInbox()
}

function openTask(row) {
  const forceView = activeTab.value !== 'pending'
  router.push({
    path: '/designtask/simulation',
    query: { taskId: row.taskId, mode: forceView ? 'view' : row.action?.mode || 'view', tab: activeTab.value }
  })
}

function backToInbox() {
  router.push({ path: '/designtask/simulation', query: { tab: activeTab.value } })
}

function actionType(mode) {
  return { enter: 'primary', view: 'info', wait: 'info' }[mode] || 'info'
}

function loadData() {
  if (!taskId.value) {
    loadInbox()
    return
  }
  getDesignTask(taskId.value).then(res => {
    const data = res.data || {}
    taskTitle.value = data.task?.taskName || '仿真验证确认'
    currentNodeKey.value = data.nodeKey || data.task?.currentNodeKey || ''
    access.value = data.access || { mode: 'wait', label: '等待' }
    simulation.value = data.simulation || simulation.value
    ansysSimulation.value = data.ansysSimulation || ansysSimulation.value
    faultPipeParameters.value = data.faultPipeParameters || { groups: [] }
    applyCadModel(data.cadModel)
    applySurrogateSolution(data.surrogateSolve)
  })
}

function applyCadModel(model) {
  cadModel.value = model || { status: 'NOT_SUBMITTED', statusLabel: '未提交', params: {}, files: {} }
  if (cadModel.value.params && Object.keys(cadModel.value.params).length) {
    cadForm.value = {
      L1: Number(cadModel.value.params.L1 ?? cadForm.value.L1),
      L2: Number(cadModel.value.params.L2 ?? cadForm.value.L2),
      R: Number(cadModel.value.params.R ?? cadForm.value.R),
      theta1: Number(cadModel.value.params.theta1 ?? cadForm.value.theta1),
      theta2: Number(cadModel.value.params.theta2 ?? cadForm.value.theta2)
    }
  }
  if (cadModel.value.status === 'SUCCESS' && cadModel.value.files?.stl) {
    loadCadStl()
  } else if (cadModel.value.status !== 'SUCCESS') {
    cadStlData.value = null
  }
  if (['QUEUED', 'RUNNING'].includes(cadModel.value.status)) {
    startCadPolling()
  } else {
    stopCadPolling()
  }
}

function applySurrogateSolution(surrogateSolve) {
  const best = surrogateSolve?.bestSolution || {}
  const shouldUseSurrogate = surrogateSolve?.confirmed && cadModel.value.status === 'NOT_SUBMITTED' && Object.keys(best).length
  if (!shouldUseSurrogate) return
  cadForm.value = {
    L1: Number(best.L1 ?? cadForm.value.L1),
    L2: Number(best.L2 ?? cadForm.value.L2),
    R: Number(best.R ?? cadForm.value.R),
    theta1: Number(best.theta1 ?? cadForm.value.theta1),
    theta2: Number(best.theta2 ?? cadForm.value.theta2)
  }
}

function startAnsysSimulation() {
  if (!taskId.value) return
  ansysSubmitting.value = true
  submitAnsysSimulationTask(taskId.value, {
    simulationMode: 'placeholder_transient_structural'
  }).then(res => {
    ansysSimulation.value = res.data || ansysSimulation.value
    ElMessage.success('ANSYS 仿真占位任务已完成')
  }).finally(() => {
    ansysSubmitting.value = false
  })
}

function refreshAnsysSimulation() {
  if (!taskId.value) return
  ansysRefreshing.value = true
  getAnsysSimulationTask(taskId.value).then(res => {
    ansysSimulation.value = res.data || ansysSimulation.value
  }).finally(() => {
    ansysRefreshing.value = false
  })
}

function ansysStatusType(status) {
  return {
    NOT_SUBMITTED: 'info',
    QUEUED: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }[status] || 'info'
}

function submitCadModel() {
  if (!taskId.value) return
  cadSubmitting.value = true
  submitCadModelTask(taskId.value, cadForm.value).then(res => {
    applyCadModel(res.data)
    ElMessage.success('CAD 建模任务已提交')
  }).finally(() => {
    cadSubmitting.value = false
  })
}

function refreshCadModel() {
  if (!taskId.value) return
  cadRefreshing.value = true
  getCadModelTask(taskId.value).then(res => {
    applyCadModel(res.data)
  }).finally(() => {
    cadRefreshing.value = false
  })
}

function startCadPolling() {
  if (cadPollTimer) return
  cadPollTimer = window.setInterval(refreshCadModel, 3000)
}

function stopCadPolling() {
  if (!cadPollTimer) return
  window.clearInterval(cadPollTimer)
  cadPollTimer = null
}

function loadCadStl() {
  if (!taskId.value) return
  getCadModelFile(taskId.value, 'stl').then(data => {
    cadStlData.value = data
  }).catch(() => {
    cadStlData.value = null
  })
}

function downloadCadFile(kind) {
  if (!taskId.value) return
  getCadModelFile(taskId.value, kind).then(data => {
    const file = cadModel.value.files?.[kind]
    const filename = file?.fileName || `pipe_model.${kind === 'sldprt' ? 'SLDPRT' : 'stl'}`
    saveAs(new Blob([data]), filename)
  })
}

function cadStatusType(status) {
  return {
    NOT_SUBMITTED: 'info',
    QUEUED: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }[status] || 'info'
}

function valueOrDash(value, suffix = '') {
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  return Number.isFinite(number) ? `${number.toFixed(3)}${suffix}` : `${value}${suffix}`
}

function simulate() {
  runSimulation(taskId.value).then(res => {
    simulation.value = res.data || {}
    ElMessage.success('Mock 仿真完成')
    loadData()
  })
}

function submitApproval() {
  approveTask(taskId.value, approval.value).then(() => {
    ElMessage.success(approval.value.approved ? '审批通过，任务完成' : '已退回模型解耦求解')
    loadData()
  })
}

onMounted(loadData)
onBeforeUnmount(() => {
  stopCadPolling()
})

watch(() => route.query.taskId, value => {
  taskId.value = value ? Number(value) : null
  activeTab.value = ['handled', 'related'].includes(route.query.tab) ? route.query.tab : activeTab.value
  loadData()
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.fixed-input-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;

  div {
    min-width: 0;
    padding: 12px;
    border: 1px solid rgba(128, 158, 195, 0.18);
    border-radius: 8px;
    background: rgba(248, 251, 255, 0.72);
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    min-width: 0;
    margin-top: 5px;
    overflow: hidden;
    color: #24324f;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.ansys-result-grid {
  display: grid;
  grid-template-columns: minmax(360px, 0.9fr) minmax(520px, 1.1fr);
  gap: 18px;
  align-items: stretch;
}

.ansys-image-panel {
  display: flex;
  min-height: 260px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid rgba(128, 158, 195, 0.22);
  border-radius: 8px;
  background: #eef3fb;

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.cad-model-grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.cad-form-panel,
.cad-result-panel {
  min-width: 0;
}

.cad-param-form {
  padding: 14px 14px 2px;
  border: 1px solid rgba(128, 158, 195, 0.22);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.68);

  :deep(.el-input-number) {
    width: 100%;
  }
}

.cad-constraint-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;

  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border: 1px solid rgba(128, 158, 195, 0.2);
    border-radius: 12px;
    color: #65788d;
    background: rgba(255, 255, 255, 0.62);
  }

  strong {
    color: #233955;
  }
}

.cad-result-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;

  div {
    min-height: 72px;
    padding: 12px;
    border: 1px solid rgba(128, 158, 195, 0.22);
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.68);
  }

  span {
    display: block;
    color: #718399;
    font-size: 13px;
  }

  strong {
    display: block;
    margin-top: 10px;
    color: #233955;
    font-size: 18px;
  }
}

.cad-file-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 12px 0;
}

@media (max-width: 1200px) {
  .cad-model-grid {
    grid-template-columns: 1fr;
  }

  .cad-result-grid {
    grid-template-columns: 1fr;
  }
}
</style>

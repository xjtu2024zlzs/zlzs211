<template>
  <div class="design-platform-view archive-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">TASK ARCHIVE</p>
          <h1 class="platform-title">任务归档文档</h1>
          <p class="platform-subtitle">
            汇总目标约束、模型解耦、设计变量、代理模型、CAD 建模、仿真验证和审批记录，形成已完成任务的归档快照。
          </p>
        </div>
        <div class="topbar-meta">
          <el-tag type="success">{{ archive.archiveStatus || 'ARCHIVED' }}</el-tag>
          <el-button plain @click="goBack">返回看板</el-button>
          <el-button type="primary" @click="printArchive">打印 / 导出</el-button>
        </div>
      </section>

      <div v-loading="loading">
        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">BASIC INFO</p>
              <h2 class="section-title">{{ archive.archiveTitle || task.taskName || '设计任务归档文档' }}</h2>
            </div>
            <el-tag>{{ archive.archiveCode || '-' }}</el-tag>
          </div>
          <div class="info-grid mt-12">
            <div><span>任务名称</span><strong>{{ task.taskName || '-' }}</strong></div>
            <div><span>任务编号</span><strong>{{ task.taskNo || '-' }}</strong></div>
            <div><span>负责人</span><strong>{{ task.ownerUserName || task.ownerUserId || '-' }}</strong></div>
            <div><span>当前节点</span><strong>{{ task.currentNodeName || '-' }}</strong></div>
            <div><span>任务状态</span><strong>{{ statusLabel(task.status) }}</strong></div>
            <div><span>归档时间</span><strong>{{ archive.archiveTime || '-' }}</strong></div>
          </div>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">OVERVIEW</p>
              <h2 class="section-title">问题概览</h2>
            </div>
          </div>
          <p class="archive-paragraph">{{ archive.problemOverview || '-' }}</p>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">OBJECTIVES & CONSTRAINTS</p>
              <h2 class="section-title">目标与约束选择归档</h2>
            </div>
          </div>
          <el-collapse class="mt-12">
            <el-collapse-item v-for="group in objectiveGroups" :key="group.discipline" :title="group.disciplineName">
              <el-table :data="group.items || []" stripe class="platform-table">
                <el-table-column label="类型" width="90">
                  <template #default="{ row }">{{ row.itemType === 'objective' ? '目标' : '约束' }}</template>
                </el-table-column>
                <el-table-column label="名称" prop="itemName" min-width="220" show-overflow-tooltip />
                <el-table-column label="方向 / 关系" width="120">
                  <template #default="{ row }">{{ row.direction || row.operatorCode || '-' }}</template>
                </el-table-column>
                <el-table-column label="阈值" width="120">
                  <template #default="{ row }">{{ row.limitValue || row.thresholdValue || '-' }}</template>
                </el-table-column>
                <el-table-column label="单位" prop="unit" width="90" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">FIXED INPUTS</p>
              <h2 class="section-title">故障管段原始设计参数</h2>
            </div>
          </div>
          <el-collapse class="mt-12">
            <el-collapse-item v-for="group in parameterGroups" :key="group.groupCode" :title="group.groupName">
              <el-table :data="group.items || []" stripe class="platform-table">
                <el-table-column label="参数" prop="paramName" min-width="180" />
                <el-table-column label="数值 / 表达式" min-width="360" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.formulaText || row.paramValue || '-' }}</template>
                </el-table-column>
                <el-table-column label="单位" prop="paramUnit" width="110" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">DECOMPOSITION</p>
              <h2 class="section-title">模型解耦结果</h2>
            </div>
          </div>
          <div class="content-grid content-grid--balanced mt-12">
            <div v-for="subtask in subtasks" :key="subtask.subtaskCode" class="soft-panel archive-panel">
              <h3>{{ subtask.subtaskName }}</h3>
              <p>求解方式：{{ subtask.solverType || '-' }}</p>
              <div class="tag-line">
                <el-tag v-for="item in subtaskItems(subtask, 'objective')" :key="item.itemCode" type="success">{{ item.itemName }}</el-tag>
                <el-tag v-for="item in subtaskItems(subtask, 'constraint')" :key="item.itemCode" type="warning">{{ item.itemName }}</el-tag>
              </div>
            </div>
          </div>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">DESIGN VARIABLES</p>
              <h2 class="section-title">设计变量归口结果</h2>
            </div>
          </div>
          <el-collapse class="mt-12">
            <el-collapse-item v-for="group in variableGroups" :key="group.discipline" :title="group.disciplineName">
              <el-table :data="group.items || []" stripe class="platform-table">
                <el-table-column label="子任务" prop="subtaskCode" width="170" />
                <el-table-column label="变量" prop="variableName" min-width="220" />
                <el-table-column label="初始值" prop="initialValue" width="120" />
                <el-table-column label="下限" prop="lowerBound" width="120" />
                <el-table-column label="上限" prop="upperBound" width="120" />
                <el-table-column label="单位" prop="unit" width="90" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">SOLVER RESULT</p>
              <h2 class="section-title">代理模型优化结果</h2>
            </div>
            <el-tag :type="surrogate.status === 'SUCCESS' || surrogate.confirmed ? 'success' : 'info'">{{ surrogate.statusLabel || surrogate.status || '未提交' }}</el-tag>
          </div>
          <div class="info-grid mt-12">
            <div><span>模型文件</span><strong>{{ surrogate.modelName || '-' }}</strong></div>
            <div><span>模型类型</span><strong>{{ surrogate.modelType || '-' }}</strong></div>
            <div><span>优化目标</span><strong>{{ surrogate.objectiveName || '-' }}</strong></div>
            <div><span>迭代次数</span><strong>{{ surrogate.iterations || 0 }}</strong></div>
          </div>
          <el-table :data="solutionRows" stripe class="platform-table mt-12">
            <el-table-column label="指标" prop="name" />
            <el-table-column label="数值" prop="value" />
            <el-table-column label="单位" prop="unit" width="100" />
          </el-table>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">CAD & SIMULATION</p>
              <h2 class="section-title">CAD 建模与 ANSYS 仿真结果</h2>
            </div>
          </div>
          <div class="content-grid content-grid--balanced mt-12">
            <div class="soft-panel archive-panel">
              <h3>CAD 建模</h3>
              <p>状态：{{ cad.statusLabel || cad.status || '未提交' }}</p>
              <p>L3：{{ cad.l3 || '-' }}</p>
              <p>几何闭合：{{ cad.closureStatus || '-' }}</p>
            </div>
            <div class="soft-panel archive-panel">
              <h3>ANSYS 应力仿真</h3>
              <p>状态：{{ ansys.statusLabel || ansys.status || '未提交' }}</p>
              <p v-for="item in ansysMetrics" :key="item.metricCode || item.metricName">
                {{ item.metricName }}：{{ item.metricValue }} {{ item.metricUnit }}
              </p>
            </div>
          </div>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">APPROVAL</p>
              <h2 class="section-title">审批记录</h2>
            </div>
            <el-tag type="success">{{ approval.result || '通过' }}</el-tag>
          </div>
          <div class="info-grid mt-12">
            <div><span>审批人</span><strong>{{ approval.approveBy || '-' }}</strong></div>
            <div><span>审批时间</span><strong>{{ approval.approveTime || '-' }}</strong></div>
            <div class="info-grid__wide"><span>审批意见</span><strong>{{ approval.comment || '-' }}</strong></div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDesignTaskArchive } from '@/api/designtask/optimization'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const loading = ref(false)
const archive = ref({})

const task = computed(() => archive.value.task || {})
const objectiveGroups = computed(() => archive.value.objectiveConstraints || [])
const parameterGroups = computed(() => archive.value.faultPipeParameters?.groups || [])
const subtasks = computed(() => archive.value.subtasks || [])
const variableGroups = computed(() => archive.value.designVariables || [])
const surrogate = computed(() => archive.value.surrogateSolve || {})
const cad = computed(() => archive.value.cadModel || {})
const ansys = computed(() => archive.value.ansysSimulation || {})
const approval = computed(() => archive.value.approval || {})
const ansysMetrics = computed(() => ansys.value.metrics || [])

const solutionRows = computed(() => {
  const best = surrogate.value.bestSolution || {}
  return [
    { name: 'L1', value: best.L1 ?? '-', unit: 'mm' },
    { name: 'L2', value: best.L2 ?? '-', unit: 'mm' },
    { name: 'R', value: best.R ?? '-', unit: 'mm' },
    { name: 'θ1', value: best.theta1 ?? '-', unit: 'deg' },
    { name: 'θ2', value: best.theta2 ?? '-', unit: 'deg' },
    { name: '预测最大应力', value: best.predictedStress ?? best.stress ?? '-', unit: surrogate.value.objectiveUnit || 'MPa' }
  ]
})

function loadArchive() {
  if (!taskId.value) {
    ElMessage.warning('缺少任务 ID，无法查看归档。')
    return
  }
  loading.value = true
  getDesignTaskArchive(taskId.value).then(res => {
    archive.value = res.data || {}
  }).finally(() => {
    loading.value = false
  })
}

function subtaskItems(subtask, itemType) {
  return (subtask.items || []).filter(item => item.itemType === itemType)
}

function statusLabel(status) {
  return {
    OBJECTIVE_SELECTING: '目标约束选择',
    CONFLICT_CHECKING: '冲突校验',
    SOLVING: '模型解耦求解',
    SIMULATING: '仿真确认',
    APPROVING: '审批中',
    COMPLETED: '已完成'
  }[status] || status || '-'
}

function goBack() {
  router.push({ path: '/designtask/dashboard', query: { status: 'COMPLETED' } })
}

function printArchive() {
  window.print()
}

onMounted(loadArchive)

watch(() => route.query.taskId, value => {
  taskId.value = value ? Number(value) : null
  loadArchive()
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.archive-view {
  .archive-paragraph {
    margin: 0;
    color: #344054;
    line-height: 1.9;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;

    > div {
      padding: 12px 14px;
      border: 1px solid #e4edf7;
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.72);
      display: flex;
      flex-direction: column;
      gap: 6px;
      min-width: 0;
    }

    span {
      color: #667085;
      font-size: 13px;
    }

    strong {
      color: #1d2b44;
      font-weight: 700;
      overflow-wrap: anywhere;
    }
  }

  .info-grid__wide {
    grid-column: span 3;
  }

  .archive-panel {
    h3 {
      margin: 0 0 10px;
      color: #1d2b44;
      font-size: 16px;
    }

    p {
      margin: 6px 0;
      color: #475467;
    }
  }

  .tag-line {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

@media print {
  .topbar-meta {
    display: none;
  }

  .design-platform-view {
    background: #fff;
  }

  .section-block {
    break-inside: avoid;
    box-shadow: none !important;
  }
}

@media (max-width: 900px) {
  .archive-view .info-grid {
    grid-template-columns: 1fr;
  }

  .archive-view .info-grid__wide {
    grid-column: auto;
  }
}
</style>

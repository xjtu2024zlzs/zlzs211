<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '目标与约束选择' }}</h1>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot"></span>
            <span>{{ hasTask ? currentLabel : activeTab === 'pending' ? '待处理' : '已处理' }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>{{ hasTask ? accessLabel : '任务入口' }}</span>
          </div>
        </div>
      </section>

      <section v-if="!hasTask" class="section-block">
        <div class="section-header">
          <div>
            <h2 class="section-title">我的目标约束任务</h2>
          </div>
          <el-button plain icon="Refresh" :loading="inboxLoading" @click="loadInbox">刷新</el-button>
        </div>

        <el-tabs v-model="activeTab" class="mt-12" @tab-change="changeTab">
          <el-tab-pane label="待处理" name="pending" />
          <el-tab-pane label="已处理" name="handled" />
        </el-tabs>

        <div class="table-shell">
          <el-table v-loading="inboxLoading" :data="visibleInboxTasks" stripe class="platform-table">
            <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
            <el-table-column label="当前节点" prop="currentNodeName" min-width="180" show-overflow-tooltip />
            <el-table-column v-if="activeTab === 'handled'" label="已处理学科" prop="handledDisciplineName" width="130" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="actionType(row.action?.mode)">{{ row.action?.label || (activeTab === 'handled' ? '查看' : '等待') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.action?.reason || row.handledRemark || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.action?.mode !== 'wait' || activeTab === 'handled'"
                  link
                  :type="row.action?.mode === 'enter' ? 'primary' : 'info'"
                  @click="openTask(row)"
                >
                  {{ activeTab === 'handled' ? '查看' : row.action?.label || '查看' }}
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
          title="流程还没有流转到当前用户，本节点暂不能处理。"
          type="info"
          :closable="false"
          show-icon
        />

        <section v-if="attachments.length || overviewDescription" class="section-block fault-attachment-card">
          <div class="section-header">
            <div>
              <h2 class="section-title">当前问题概览</h2>
            </div>
            <el-button v-if="attachments.length" plain icon="Picture" @click="openAttachmentViewer(previewAttachment || attachments[0])">查看示意图</el-button>
          </div>

          <div class="fault-overview-body">
            <div v-if="overviewDescription" class="fault-task-description">
              <b>当前问题详情</b>
              <p>{{ overviewDescription }}</p>
            </div>
            <div v-if="attachments.length" class="fault-attachment-entry" @click="openAttachmentViewer(previewAttachment || attachments[0])">
              <div class="fault-attachment-icon">PNG</div>
              <div class="fault-attachment-meta">
                <strong>{{ previewAttachment?.fileName || attachments[0]?.fileName || '问题示意图' }}</strong>
                <span>任务附件</span>
              </div>
              <el-button type="primary" plain icon="View">查看示意图</el-button>
            </div>
          </div>
        </section>

        <template v-if="isFrameBeamTask">
          <CrackInputPanel :task-id="taskId" />
          <LoadSpectrumPanel :task-id="taskId" />
        </template>

        <div v-if="!isFrameBeamTask" class="content-grid">
          <section class="section-block">
            <div class="section-header">
              <div>
                <h2 class="section-title">{{ currentLabel }}{{ readonlyMode ? '已定义项' : '优化问题定义' }}</h2>
              </div>
              <el-select
                v-model="discipline"
                style="width: 190px"
                :disabled="!canSwitchDiscipline"
                @change="loadCatalog"
              >
                <el-option v-for="item in disciplines" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </div>
            <div v-if="!readonlyMode" class="catalog-hint">
              当前任务已自动推荐 {{ catalogMatchStats.recommended }} 项并默认勾选，其余 {{ catalogMatchStats.optional }} 项作为备选目标/约束，可按任务需要补选。
            </div>

            <div class="table-shell">
              <h3 class="block-title">优化目标</h3>
              <el-table
                :data="objectives"
                stripe
                class="platform-table"
                :row-key="rowExpandKey"
                :expand-row-keys="expandedObjectiveKeys"
                @expand-change="handleObjectiveExpandChange"
              >
                <el-table-column type="expand" width="44">
                  <template #default="{ row }">
                    <div class="definition-expand">
                      <el-form label-width="120px" class="definition-form">
                        <el-form-item label="目标值">
                          <el-input v-model="row.targetValue" placeholder="可选，例如 0 或目标评分" :disabled="readonlyMode" />
                        </el-form-item>
                        <el-form-item label="容差">
                          <el-input v-model="row.tolerance" placeholder="可选" :disabled="readonlyMode" />
                        </el-form-item>
                      </el-form>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column v-if="!readonlyMode" width="60">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.checked" />
                  </template>
                </el-table-column>
                <el-table-column label="名称" min-width="260" show-overflow-tooltip>
                  <template #default="{ row }">
                    <div class="item-name-cell">
                      <div class="item-name-main">
                        <el-tag size="small" :type="recommendTagType(row)">{{ recommendLabel(row) }}</el-tag>
                        <span>{{ row.itemName }}</span>
                      </div>
                      <div class="item-name-desc">{{ itemDescription(row) }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="优化类型" prop="direction" width="120" />
                <el-table-column label="目标设置" min-width="160" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span :class="objectiveTargetText(row) === '按优化类型' ? 'definition-empty' : 'definition-value'">
                      {{ objectiveTargetText(row) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="table-shell mt-16">
              <h3 class="block-title">约束条件</h3>
              <el-table
                :data="constraints"
                stripe
                class="platform-table"
                :row-key="rowExpandKey"
                :expand-row-keys="expandedConstraintKeys"
                @expand-change="handleConstraintExpandChange"
              >
                <el-table-column type="expand" width="44">
                  <template #default="{ row }">
                    <div class="definition-expand">
                      <el-form label-width="120px" class="definition-form">
                        <el-form-item label="约束级别">
                          <el-radio-group v-model="row.constraintLevel" :disabled="readonlyMode">
                            <el-radio-button label="hard">硬约束</el-radio-button>
                            <el-radio-button label="soft">软约束</el-radio-button>
                          </el-radio-group>
                        </el-form-item>
                        <template v-if="isNumericConstraint(row)">
                        <el-form-item label="单值限值">
                          <el-input v-model="row.limitValue" :placeholder="row.unit ? `可选，例如 ${row.unit}` : '可选，单一限制值'" :disabled="readonlyMode" />
                        </el-form-item>
                        <el-form-item label="下界">
                          <el-input v-model="row.lowerBound" placeholder="可选" :disabled="readonlyMode" />
                        </el-form-item>
                        <el-form-item label="上界">
                          <el-input v-model="row.upperBound" placeholder="可选" :disabled="readonlyMode" />
                        </el-form-item>
                        <el-form-item label="容差">
                          <el-input v-model="row.tolerance" placeholder="可选" :disabled="readonlyMode" />
                        </el-form-item>
                        </template>
                        <div v-else class="definition-auto-check">
                          该约束由后续模型或规则自动校核，无需在当前步骤填写限值。
                        </div>
                      </el-form>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column v-if="!readonlyMode" width="60">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.checked" />
                  </template>
                </el-table-column>
                <el-table-column label="名称" min-width="280" show-overflow-tooltip>
                  <template #default="{ row }">
                    <div class="item-name-cell">
                      <div class="item-name-main">
                        <el-tag size="small" :type="recommendTagType(row)">{{ recommendLabel(row) }}</el-tag>
                        <span>{{ row.itemName }}</span>
                      </div>
                      <div class="item-name-desc">{{ itemDescription(row) }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="关系" width="120">
                  <template #default="{ row }">{{ directionLabel(row.direction) }}</template>
                </el-table-column>
                <el-table-column label="级别" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.constraintLevel === 'soft' ? 'warning' : 'danger'">{{ constraintLevelLabel(row.constraintLevel) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="限值设置" min-width="220" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span :class="constraintLimitText(row) === '未设置' ? 'definition-empty' : 'definition-value'">
                      {{ constraintLimitText(row) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="单位" width="100">
                  <template #default="{ row }">{{ displayUnit(row) }}</template>
                </el-table-column>
                <el-table-column label="校核方式" min-width="150" show-overflow-tooltip>
                  <template #default="{ row }">{{ checkMethodText(row) }}</template>
                </el-table-column>
              </el-table>
            </div>
          </section>

          <aside class="side-stack">
            <section class="section-block">
              <div class="section-header">
                <div>
                  <h2 class="section-title">{{ readonlyMode ? '查看信息' : '提交信息' }}</h2>
                </div>
              </div>
              <el-form label-width="90px" class="mt-12">
                <el-form-item label="任务 ID">
                  <el-input-number v-model="taskId" :min="1" disabled />
                </el-form-item>
                <el-form-item label="完整性">
                  <div class="definition-summary">
                    <el-tag type="success">{{ definitionStats.objectiveCount }} 个目标</el-tag>
                    <el-tag type="danger">{{ definitionStats.hardCount }} 个硬约束</el-tag>
                    <el-tag type="warning">{{ definitionStats.softCount }} 个软约束</el-tag>
                    <el-tag :type="definitionStats.issueCount ? 'danger' : 'success'">{{ definitionStats.issueCount }} 个问题</el-tag>
                  </div>
                </el-form-item>
                <el-alert
                  v-if="definitionIssues.length"
                  class="mb-16"
                  type="warning"
                  :closable="false"
                  show-icon
                >
                  <template #title>
                    <div class="definition-issues">
                      <div v-for="issue in definitionIssues.slice(0, 4)" :key="issue">{{ issue }}</div>
                    </div>
                  </template>
                </el-alert>
                <el-form-item label="备注">
                  <el-input v-model="remark" type="textarea" :rows="5" :disabled="readonlyMode" />
                </el-form-item>
                <el-button v-if="!readonlyMode" plain icon="CircleCheck" @click="checkDefinition">校验定义</el-button>
                <el-button v-if="!readonlyMode" type="primary" icon="Check" :loading="saving" @click="submit">
                  提交当前学科
                </el-button>
                <el-button v-else plain icon="Back" @click="backToInbox">返回任务列表</el-button>
              </el-form>
            </section>

            <section class="section-block">
              <div class="section-header">
                <div>
                  <h2 class="section-title">流转顺序</h2>
                </div>
              </div>
              <el-timeline class="flow-timeline mt-12">
                <el-timeline-item
                  v-for="item in disciplines"
                  :key="item.value"
                  :type="flowNodeType(item.value)"
                  :class="`flow-node--${flowNodeState(item.value)}`"
                >
                  <div class="flow-node-content">
                    <span>{{ item.label }}</span>
                    <el-tag size="small" :type="flowNodeTagType(item.value)" effect="light">
                      {{ flowNodeStatusLabel(item.value) }}
                    </el-tag>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </section>
          </aside>
        </div>

        <el-dialog v-model="attachmentViewerVisible" title="查看任务附件" width="72%" class="attachment-viewer-dialog">
          <div v-if="selectedAttachment" class="attachment-viewer">
            <img
              v-if="isImageAttachment(selectedAttachment) && attachmentPreviewUrl(selectedAttachment)"
              :src="attachmentPreviewUrl(selectedAttachment)"
              :alt="selectedAttachment.fileName"
            />
            <div v-else class="fault-file-placeholder">
              <strong>{{ selectedAttachment.fileName }}</strong>
              <span>当前附件不是可直接预览的图片，或文件地址不可访问。</span>
            </div>
          </div>
        </el-dialog>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getDashboard,
  getDesignTask,
  getObjectiveCatalog,
  getTaskAttachmentFile,
  saveObjectiveConstraints
} from '@/api/designtask/optimization'
import CrackInputPanel from '@/views/designtask/frameBeam/CrackInputPanel.vue'
import LoadSpectrumPanel from '@/views/designtask/frameBeam/LoadSpectrumPanel.vue'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('目标与约束选择')
const taskType = ref('')
const discipline = ref(route.query.discipline || 'structure')
const catalog = ref([])
const saving = ref(false)
const inboxLoading = ref(false)
const pendingTasks = ref([])
const handledTasks = ref([])
const activeTab = ref(route.query.tab === 'handled' ? 'handled' : 'pending')
const remark = ref('已完成本学科目标与约束选择。')
const access = ref({ mode: 'wait', label: '等待' })
const attachments = ref([])
const taskDescription = ref('')
const attachmentPreviewUrls = ref({})
const attachmentViewerVisible = ref(false)
const selectedAttachment = ref(null)
const currentNodeKey = ref('')
const taskStatus = ref('')
const isPlatformAdmin = ref(false)
const expandedObjectiveKeys = ref([])
const expandedConstraintKeys = ref([])

const disciplines = [
  { value: 'structure', label: '结构工程师' },
  { value: 'layout', label: '布局工程师' },
  { value: 'aero', label: '气动工程师' },
  { value: 'hydraulic', label: '液压工程师' },
  { value: 'manufacturing', label: '制造工程师' }
]

const hasTask = computed(() => !!taskId.value)
const isFrameBeamTask = computed(() => taskType.value === 'FRAME_BEAM_CRACK_LIFE_PREDICTION')
const readonlyMode = computed(() => access.value.mode !== 'enter' || route.query.mode === 'view')
const canSwitchDiscipline = computed(() => isPlatformAdmin.value && !readonlyMode.value)
const currentLabel = computed(() => disciplines.find(item => item.value === discipline.value)?.label || '')
const accessLabel = computed(() => access.value.label || (access.value.mode === 'enter' ? '可处理' : '查看'))
const visibleInboxTasks = computed(() => activeTab.value === 'pending' ? pendingTasks.value : handledTasks.value)
const objectives = computed(() => catalog.value.filter(item => item.itemType === 'objective'))
const constraints = computed(() => catalog.value.filter(item => item.itemType === 'constraint'))
const previewAttachment = computed(() => attachments.value.find(file => isImageAttachment(file) && attachmentPreviewUrl(file)))
const overviewDescription = computed(() => normalizeProblemOverview(taskDescription.value))
const hiddenObjectiveItemCodes = new Set([
  'STR_FORBIDDEN_ZONE',
  'LAY_HYDRAULIC_PIPE_FIXED',
  'LAY_DOOR_ENVELOPE_AVOID',
  'LAY_PIPE_ENDPOINT_FIXED',
  'LAY_PIPE_HORIZONTAL_SPAN',
  'LAY_PIPE_VERTICAL_SPAN',
  'HYD_MIN_BEND_RADIUS',
  'HYD_BEND_RADIUS',
  'HYD_IMPACT_LOAD',
  'HYD_PRESSURE_VELOCITY_INPUT',
  'HYD_VALVE_CLOSE_TIME',
  'HYD_PIPE_DIAMETER',
  'MFG_MAINTENANCE_ACCESS_MAX'
])

const objectiveItemPresentationOverrides = {
  HYD_CLAMP_VALID: {
    itemName: '卡箍强度/连接完整性满足要求',
    direction: 'meet',
    unit: '',
    ruleType: 'strength_check',
    operatorCode: 'pass'
  }
}
const definitionIssues = computed(() => validateDefinitionItems(false))
const catalogMatchStats = computed(() => {
  const rows = catalog.value || []
  return {
    recommended: rows.filter(item => item.recommended).length,
    optional: rows.filter(item => !item.recommended).length
  }
})
const definitionStats = computed(() => {
  const checked = selectedDefinitionItems()
  const constraints = checked.filter(item => item.itemType === 'constraint')
  return {
    objectiveCount: checked.filter(item => item.itemType === 'objective').length,
    hardCount: constraints.filter(item => item.constraintLevel !== 'soft').length,
    softCount: constraints.filter(item => item.constraintLevel === 'soft').length,
    issueCount: definitionIssues.value.length
  }
})
const nodeDisciplineMap = {
  structure_select: 'structure',
  layout_select: 'layout',
  aero_select: 'aero',
  hydraulic_select: 'hydraulic',
  manufacturing_select: 'manufacturing'
}

const objectiveNodeOrder = ['structure_select', 'layout_select', 'aero_select', 'hydraulic_select', 'manufacturing_select']
const objectiveDisciplineOrder = ['structure', 'layout', 'aero', 'hydraulic', 'manufacturing']

const taskUserDisciplineMap = [
  ['structureUserId', 'structure'],
  ['layoutUserId', 'layout'],
  ['aeroUserId', 'aero'],
  ['hydraulicUserId', 'hydraulic'],
  ['manufacturingUserId', 'manufacturing']
]

const itemDescriptionMap = {
  STR_DEFORMATION_RISK_MIN: '降低管线变形后与结构、机构或舱内空间发生碰撞的可能性。',
  STR_FORBIDDEN_ZONE: '校核管路、线缆等路径是否进入结构禁止布置区域。',
  STR_INTERFACE_FIXED: '保证铰链、锁机构、作动器等关键接口位置不被优化过程改变。',
  STR_PIPE_CLEARANCE: '校核管道最大变形后是否仍与结构件保持安全距离。',
  STR_CLAMP_SUPPORT_VALID: '校核卡箍支撑点是否具备足够结构强度和安装条件。',
  LAY_INTERFERENCE_RISK_MIN: '降低线缆、液压管和结构之间的空间干涉风险。',
  LAY_CABLE_LENGTH_MIN: '缩短线缆路径，减少绕行和不必要长度。',
  LAY_MAINTAINABILITY_MAX: '提高检查、拆装和维护操作的可达性。',
  LAY_PIPE_CLEARANCE_LIMIT: '保证线缆与液压管之间保留安全隔离距离。',
  LAY_FORBIDDEN_ZONE_AVOID: '校核线缆路径是否避开结构禁布区域。',
  LAY_CABLE_BEND_RADIUS_LIMIT: '保证线缆弯曲半径不小于允许下限，避免过度弯折。',
  LAY_CLAMP_SPACING_LIMIT: '限制线夹或管夹间距，避免支撑不足。',
  LAY_SERVICE_MARGIN_LIMIT: '保证检修操作所需的最小空间余量。',
  AERO_ENVELOPE_IMPACT_MIN: '降低内部管线和附件布置对舱门外形包络的影响。',
  AERO_OUTER_ENVELOPE: '校核管线与附件是否仍位于舱门外形包络之内。',
  AERO_DOOR_GAP_CLEARANCE: '保证舱门缝隙和开闭间隙不被内部布置影响。',
  HYD_STRESS_MIN: '降低液压冲击工况下管路最大等效应力。',
  HYD_DEFORMATION_MIN: '降低液压冲击工况下管路最大变形量。',
  HYD_PRESSURE_DROP_MIN: '降低液压管路局部压降，减小系统能量损失。',
  HYD_STRESS_LIMIT: '校核液压管路最大等效应力是否不超过许用应力。',
  HYD_DEFORMATION_LIMIT: '校核液压管路最大变形量是否不超过允许变形。',
  HYD_MIN_BEND_RADIUS: '保证液压管弯曲半径满足制造和使用下限。',
  HYD_CLAMP_VALID: '校核液压冲击下卡箍是否保持完好、不发生断裂。',
  HYD_PRESSURE_VELOCITY_INPUT: '用于定义液压冲击仿真的入口压力和流速时间历程。',
  HYD_VALVE_CLOSE_TIME: '用于定义阀门关闭或作动器卡滞引起的冲击工况。',
  MFG_PROCESS_COMPLEXITY_MIN: '降低弯管、装夹和加工过程复杂度。',
  MFG_ASSEMBLY_EFFICIENCY_MAX: '提高装配过程效率，减少装配调整工作量。',
  MFG_MAINTENANCE_ACCESS_MAX: '提高后续检修和拆装操作的可达性。',
  MFG_BEND_RADIUS_LIMIT: '保证弯曲半径满足制造工艺下限。',
  MFG_BEND_ANGLE_RANGE: '保证弯曲角度处于可加工范围内。',
  MFG_WALL_THICKNESS_LIMIT: '保证管道壁厚满足加工和强度要求。',
  MFG_CLAMP_INSTALLABLE: '校核管夹或线夹是否具备安装空间和装配条件。',
  MFG_TOOL_ACCESS: '校核工具是否具备必要操作空间。'
}

const checkMethodMap = {
  STR_FORBIDDEN_ZONE: '模型几何校核',
  STR_INTERFACE_FIXED: '接口一致性校核',
  STR_PIPE_CLEARANCE: '模型几何校核',
  STR_CLAMP_SUPPORT_VALID: '结构强度校核',
  LAY_FORBIDDEN_ZONE_AVOID: '模型几何校核',
  LAY_DOOR_ENVELOPE_AVOID: '模型几何校核',
  AERO_DOOR_GAP_CLEARANCE: '运动间隙校核',
  HYD_CLAMP_VALID: '强度状态校核',
  HYD_PRESSURE_VELOCITY_INPUT: '工况输入检查',
  MFG_CLAMP_INSTALLABLE: '装配可达性校核',
  MFG_TOOL_ACCESS: '工具可达性校核'
}

const directionLabelMap = {
  min: '越小越好',
  max: '越大越好',
  avoid: '避让',
  fixed: '固定',
  meet: '满足',
  false: '不得发生',
  true: '必须满足',
  input: '工况输入',
  range: '范围',
  '>=': '不小于',
  '<=': '不超过',
  '=': '等于'
}

function loadInbox() {
  inboxLoading.value = true
  if (activeTab.value === 'pending') {
    getDashboard({ scope: 'related', status: 'OBJECTIVE_SELECTING' }).then(res => {
      const rows = res.data?.tasks || []
      pendingTasks.value = rows.filter(row => String(row.currentNodeKey || '').endsWith('_select'))
    }).finally(() => {
      inboxLoading.value = false
    })
    return
  }

  getDashboard({ scope: 'related' }).then(async res => {
    const rows = res.data?.tasks || []
    const handled = []
    for (const row of rows) {
      const detail = await getDesignTask(row.taskId).then(result => result.data || {}).catch(() => null)
      const handledDiscipline = detail ? currentUserSubmittedDiscipline(detail) : null
      if (handledDiscipline) {
        handled.push({
          ...row,
          action: { mode: 'view', label: '查看', reason: '已提交目标与约束' },
          handledDiscipline: handledDiscipline.value,
          handledDisciplineName: handledDiscipline.label,
          handledRemark: handledDiscipline.remark
        })
      }
    }
    handledTasks.value = handled
  }).finally(() => {
    inboxLoading.value = false
  })
}

function changeTab() {
  router.replace({ path: '/designtask/objective', query: { tab: activeTab.value } })
  loadInbox()
}

function currentUserSubmittedDiscipline(detail) {
  const currentUserId = Number(detail.currentUserId)
  const task = detail.task || {}
  const groups = detail.objectiveConstraints || []
  for (const [userKey, disciplineValue] of taskUserDisciplineMap) {
    if (Number(task[userKey]) !== currentUserId) continue
    const group = groups.find(item => item.discipline === disciplineValue && (item.items || []).length)
    if (group) {
      return {
        value: disciplineValue,
        label: group.disciplineName || disciplineLabel(disciplineValue),
        remark: group.items?.[0]?.remark || ''
      }
    }
  }
  return null
}

function openTask(row) {
  router.push({
    path: '/designtask/objective',
    query: {
      taskId: row.taskId,
      mode: activeTab.value === 'handled' ? 'view' : row.action?.mode || 'view',
      discipline: row.handledDiscipline || undefined,
      tab: activeTab.value
    }
  })
}

function backToInbox() {
  router.push({ path: '/designtask/objective', query: { tab: activeTab.value } })
}

function actionType(mode) {
  return { enter: 'primary', view: 'info', wait: 'info' }[mode] || 'info'
}

function disciplineLabel(value) {
  return disciplines.find(item => item.value === value)?.label || value
}

function flowNodeState(value) {
  if (taskStatus.value === 'COMPLETED' || currentNodeKey.value === 'end') {
    return 'done'
  }
  const currentNodeIndex = objectiveNodeOrder.indexOf(currentNodeKey.value)
  const itemIndex = objectiveDisciplineOrder.indexOf(value)
  if (itemIndex < 0) return 'pending'
  if (currentNodeIndex < 0) {
    return currentNodeKey.value ? 'done' : (value === discipline.value ? 'current' : 'pending')
  }
  if (itemIndex < currentNodeIndex) return 'done'
  if (itemIndex === currentNodeIndex) return 'current'
  return 'pending'
}

function flowNodeType(value) {
  const state = flowNodeState(value)
  if (state === 'done') return 'success'
  if (state === 'current') return 'primary'
  return 'info'
}

function flowNodeTagType(value) {
  return flowNodeType(value)
}

function flowNodeStatusLabel(value) {
  const state = flowNodeState(value)
  if (state === 'done') return '已完成'
  if (state === 'current') return '进行中'
  return '待处理'
}

function constraintLevelLabel(value) {
  return value === 'soft' ? '软约束' : '硬约束'
}

function normalizeProblemOverview(description) {
  const text = String(description || '').trim()
  const prematureDecomposition =
    text.includes('任务拆分为') ||
    text.includes('两个子任务') ||
    text.includes('布局子任务') ||
    text.includes('固定边界')
  if (!text || prematureDecomposition) {
    return '基于前置故障追因结论，当前阶段由各专业工程师选择与舱门管线问题相关的优化目标、约束条件和必要设计变量。任务解耦将在目标与约束选择完成后执行，解耦前不预先固定子任务关系或上下游边界。'
  }
  return text
}

function loadTaskContext() {
  if (!taskId.value) {
    loadInbox()
    return
  }
  getDesignTask(taskId.value).then(res => {
    const data = res.data || {}
    const nodeKey = data.nodeKey || data.task?.currentNodeKey
    const currentDiscipline = resolveTaskDiscipline(data, nodeKey)
    taskTitle.value = data.task?.taskName || '目标与约束选择'
    taskType.value = data.task?.taskType || ''
    access.value = data.access || { mode: 'wait', label: '等待' }
    currentNodeKey.value = nodeKey || ''
    taskStatus.value = data.task?.status || ''
    isPlatformAdmin.value = Boolean(data.isPlatformAdmin)
    taskDescription.value = data.task?.description || ''
    attachments.value = data.attachments || []
    prepareAttachmentPreviews(attachments.value)
    if (currentDiscipline) {
      discipline.value = currentDiscipline
    }
    if (readonlyMode.value) {
      loadSelectedItems(data)
    } else {
      loadCatalog()
    }
  })
}

function resolveTaskDiscipline(data, nodeKey) {
  const nodeDiscipline = nodeDisciplineMap[nodeKey]
  const routeDiscipline = route.query.discipline
  const canAdminSwitch = Boolean(data.isPlatformAdmin) && data.access?.mode === 'enter' && route.query.mode !== 'view'
  if (canAdminSwitch && routeDiscipline) {
    return routeDiscipline
  }
  if (data.access?.mode === 'enter') {
    return nodeDiscipline || routeDiscipline || discipline.value
  }
  return routeDiscipline || nodeDiscipline || discipline.value
}

function isImageAttachment(file) {
  const type = String(file?.fileType || '').toLowerCase()
  const suffix = String(file?.fileSuffix || file?.fileName || '').toLowerCase()
  return type.startsWith('image/') || /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(suffix)
}

function attachmentUrl(file) {
  const previewUrl = attachmentPreviewUrl(file)
  if (previewUrl) {
    return previewUrl
  }
  if (file?.fileId) {
    return `${import.meta.env.VITE_APP_BASE_API}/designtask/task/attachment/${file.fileId}`
  }
  const path = file?.filePath || file?.url || ''
  if (!path || String(path).startsWith('pending://')) return ''
  if (/^(https?:|data:|blob:)/i.test(path)) return path
  if (path.startsWith('/')) return `${import.meta.env.VITE_APP_BASE_API}${path}`
  return `${import.meta.env.VITE_APP_BASE_API}/${path}`
}

function attachmentPreviewUrl(file) {
  if (!file) return ''
  return attachmentPreviewUrls.value[previewKey(file)] || ''
}

function previewKey(file) {
  return file?.fileId || file?.filePath || file?.fileName
}

function revokeAttachmentPreviews() {
  Object.values(attachmentPreviewUrls.value).forEach(url => {
    if (url) URL.revokeObjectURL(url)
  })
  attachmentPreviewUrls.value = {}
}

async function prepareAttachmentPreviews(files = []) {
  revokeAttachmentPreviews()
  const urls = {}
  for (const file of files) {
    if (!file?.fileId) continue
    try {
      const data = await getTaskAttachmentFile(file.fileId)
      const blob = new Blob([data], { type: file.fileType || 'application/octet-stream' })
      urls[previewKey(file)] = URL.createObjectURL(blob)
    } catch (e) {
      console.warn('Attachment preview failed', file.fileId, e)
    }
  }
  attachmentPreviewUrls.value = urls
}

async function openAttachmentViewer(file) {
  if (!file) return
  if (file.fileId && !attachmentPreviewUrl(file)) {
    await prepareAttachmentPreviews(attachments.value)
  }
  selectedAttachment.value = file
  attachmentViewerVisible.value = true
  const url = attachmentPreviewUrl(file) || attachmentUrl(file)
  if (!isImageAttachment(file) && url) {
    window.open(url, '_blank')
  } else if (!url) {
    ElMessage.warning('当前附件没有可访问的文件地址，请确认任务发起时已完成上传。')
  }
}

function loadSelectedItems(detail) {
  const group = (detail.objectiveConstraints || []).find(item => item.discipline === discipline.value)
  const items = filterObjectiveItems(group?.items || [])
  catalog.value = items.map(item => normalizeDefinitionItem(item, true))
  pruneExpandedRows()
  remark.value = items[0]?.remark || '已完成本学科目标与约束选择。'
  getObjectiveCatalog(discipline.value, {
    taskId: taskId.value,
    taskType: taskType.value
  }).then(res => {
    const recommendedCodes = new Set(filterObjectiveItems(res.data || [])
      .filter(item => item.recommended)
      .map(item => item.itemCode))
    catalog.value = catalog.value.map(item => ({
      ...item,
      recommended: Boolean(item.recommended) || recommendedCodes.has(item.itemCode),
      matchLevel: recommendedCodes.has(item.itemCode) ? 'recommended' : (item.matchLevel || 'optional'),
      matchReason: recommendedCodes.has(item.itemCode)
        ? '匹配当前任务类型，建议默认启用。'
        : (item.matchReason || '备选目标/约束，可按当前任务需要补选。')
    }))
    pruneExpandedRows()
  }).catch(() => {})
}

function loadCatalog() {
  getObjectiveCatalog(discipline.value, {
    taskId: taskId.value,
    taskType: taskType.value
  }).then(res => {
    catalog.value = filterObjectiveItems(res.data || []).map(item => normalizeDefinitionItem(item, Boolean(item.recommended)))
    pruneExpandedRows()
  })
}

function filterObjectiveItems(items) {
  return items.filter(item => !hiddenObjectiveItemCodes.has(item.itemCode))
}

function normalizeDefinitionItem(item, checked = false) {
  const payload = parseRulePayload(item.rulePayload)
  const presentation = objectiveItemPresentationOverrides[item.itemCode] || {}
  const normalized = {
    ...item,
    ...payload,
    ...presentation,
    checked,
    weight: normalizeWeight(item.weight ?? payload.weight),
    limitValue: item.limitValue || payload.limitValue || item.thresholdValue || '',
    dataType: payload.dataType || inferDataType(item),
    constraintLevel: payload.constraintLevel || (item.itemType === 'constraint' ? 'hard' : ''),
    lowerBound: payload.lowerBound || '',
    upperBound: payload.upperBound || '',
    targetValue: payload.targetValue || item.thresholdValue || item.limitValue || '',
    tolerance: payload.tolerance || '',
    sourceType: payload.sourceType || 'manual',
    sourceName: payload.sourceName || '',
    sourceVersion: payload.sourceVersion || '',
    applicableObjectType: payload.applicableObjectType || '',
    applicableObjectCode: payload.applicableObjectCode || '',
    conditionCode: payload.conditionCode || '',
    conditionName: payload.conditionName || '',
    unitDimension: payload.unitDimension || item.unit || '',
    allowedUnits: payload.allowedUnits || item.unit || '',
    ruleExpression: item.ruleExpression || payload.ruleExpression || '',
    targetField: item.targetField || payload.targetField || '',
    referenceField: item.referenceField || payload.referenceField || ''
  }
  return normalized
}

function parseRulePayload(payload) {
  if (!payload) return {}
  if (typeof payload === 'object') return payload
  try {
    const parsed = JSON.parse(payload)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (e) {
    return {}
  }
}

function inferDataType(item) {
  const direction = String(item.direction || '')
  const unit = String(item.unit || '')
  if (['avoid', 'inside', 'fixed'].includes(direction)) return 'geometry'
  if (['meet', 'pass', 'false', 'true'].includes(direction)) return 'boolean'
  if (unit === 'score') return 'score'
  if (unit === 'risk') return 'risk'
  return 'numeric'
}

function normalizeWeight(value) {
  const number = Number(value)
  if (!Number.isFinite(number)) return 5
  if (number > 10) return Math.max(0, Math.min(10, Math.round(number / 10)))
  return Math.max(0, Math.min(10, Math.round(number)))
}

function selectedDefinitionItems() {
  return catalog.value.filter(item => item.checked)
}

function itemDescription(row) {
  return itemDescriptionMap[row?.itemCode] || row?.remark || '用于当前学科对优化问题进行约束或评价。'
}

function recommendLabel(row) {
  return row?.recommended ? '推荐' : '备选'
}

function recommendTagType(row) {
  return row?.recommended ? 'success' : 'info'
}

function displayUnit(row) {
  return row?.unit || '-'
}

function directionLabel(value) {
  return directionLabelMap[value] || value || '-'
}

function isNumericConstraint(row) {
  const direction = String(row?.direction || '')
  const unit = String(row?.unit || '')
  if (['avoid', 'fixed', 'meet', 'false', 'true'].includes(direction)) return false
  if (['score', 'risk'].includes(unit)) return false
  return Boolean(unit) || ['>=', '<=', '=', 'range'].includes(direction)
}

function requiresManualLimit(row) {
  if (!isNumericConstraint(row) || row?.itemType !== 'constraint') return false
  const sourceType = String(row?.sourceType || '').toLowerCase()
  const ruleDriven = Boolean(
    String(row?.ruleType || '').trim() ||
    String(row?.ruleExpression || '').trim() ||
    String(row?.targetField || '').trim() ||
    String(row?.referenceField || '').trim() ||
    String(row?.operatorCode || '').trim()
  )
  return !ruleDriven && !['model', 'rule', 'auto', 'system', 'derived'].includes(sourceType)
}

function checkMethodText(row) {
  if (checkMethodMap[row?.itemCode]) return checkMethodMap[row.itemCode]
  if (isNumericConstraint(row)) return '数值边界校核'
  return '模型/规则校核'
}

function valueUnit(row) {
  const unit = row?.unit || ''
  if (!unit || ['score', 'risk'].includes(unit)) return ''
  return ` ${unit}`
}

function objectiveTargetText(row) {
  const parts = []
  if (String(row.targetValue || '').trim()) {
    parts.push(`目标 ${row.targetValue}${valueUnit(row)}`)
  }
  if (String(row.tolerance || '').trim()) {
    parts.push(`容差 ${row.tolerance}`)
  }
  return parts.length ? parts.join(' / ') : '按优化类型'
}

function constraintLimitText(row) {
  if (!isNumericConstraint(row)) {
    return '由模型/规则校核'
  }
  const parts = []
  const unit = valueUnit(row)
  if (String(row.limitValue || '').trim()) {
    parts.push(`限值 ${row.limitValue}${unit}`)
  }
  if (String(row.lowerBound || '').trim()) {
    parts.push(`>= ${row.lowerBound}${unit}`)
  }
  if (String(row.upperBound || '').trim()) {
    parts.push(`<= ${row.upperBound}${unit}`)
  }
  if (String(row.tolerance || '').trim()) {
    parts.push(`容差 ${row.tolerance}`)
  }
  return parts.length ? parts.join(' / ') : '未设置'
}

function rowExpandKey(row) {
  return row?.itemCode || row?.id || row?.itemName || ''
}

function handleObjectiveExpandChange(row, expandedRows) {
  expandedObjectiveKeys.value = (expandedRows || []).map(rowExpandKey).filter(Boolean)
}

function handleConstraintExpandChange(row, expandedRows) {
  expandedConstraintKeys.value = (expandedRows || []).map(rowExpandKey).filter(Boolean)
}

function pruneExpandedRows() {
  const objectiveKeys = new Set(objectives.value.map(rowExpandKey))
  const constraintKeys = new Set(constraints.value.map(rowExpandKey))
  expandedObjectiveKeys.value = expandedObjectiveKeys.value.filter(key => objectiveKeys.has(key))
  expandedConstraintKeys.value = expandedConstraintKeys.value.filter(key => constraintKeys.has(key))
}

function checkDefinition() {
  const issues = validateDefinitionItems(false)
  if (issues.length) {
    ElMessage.warning(`发现 ${issues.length} 个定义待完善项，请查看右侧完整性提示。`)
    return
  }
  ElMessage.success('当前目标与约束定义完整。')
}

function validateDefinitionItems(includeDisabled) {
  const items = includeDisabled ? catalog.value : selectedDefinitionItems()
  const issues = []
  items.forEach(item => {
    if (!item.checked && !includeDisabled) return
    const name = item.itemName || item.itemCode
    if (item.itemType === 'constraint' && !item.constraintLevel) issues.push(`${name} 未定义硬/软约束`)
    if (isNumericConstraint(item)) {
      const hasLimit = Boolean(String(item.limitValue || '').trim() || String(item.lowerBound || '').trim() || String(item.upperBound || '').trim())
      if (requiresManualLimit(item) && !hasLimit) issues.push(`${name} 缺少数值边界或阈值`)
      if (String(item.lowerBound || '').trim() && String(item.upperBound || '').trim()) {
        const lower = Number(item.lowerBound)
        const upper = Number(item.upperBound)
        if (Number.isFinite(lower) && Number.isFinite(upper) && lower > upper) {
          issues.push(`${name} 下界不能大于上界`)
        }
      }
    }
    const allowedUnits = String(item.allowedUnits || '').split(/[,，/]/).map(unit => unit.trim()).filter(Boolean)
    if (item.unit && allowedUnits.length && !allowedUnits.includes(item.unit)) {
      issues.push(`${name} 单位 ${item.unit} 不在允许单位内`)
    }
  })
  return issues
}

function buildSubmitItems() {
  return selectedDefinitionItems().map(item => {
    const payload = {
      dataType: item.dataType,
      constraintLevel: item.constraintLevel,
      limitValue: item.limitValue,
      lowerBound: item.lowerBound,
      upperBound: item.upperBound,
      targetValue: item.targetValue,
      tolerance: item.tolerance,
      sourceType: item.sourceType,
      sourceName: item.sourceName,
      sourceVersion: item.sourceVersion,
      applicableObjectType: item.applicableObjectType,
      applicableObjectCode: item.applicableObjectCode,
      conditionCode: item.conditionCode,
      conditionName: item.conditionName,
      unitDimension: item.unitDimension,
      allowedUnits: item.allowedUnits,
      ruleExpression: item.ruleExpression,
      targetField: item.targetField,
      referenceField: item.referenceField
    }
    return {
      ...item,
      rulePayload: JSON.stringify(payload)
    }
  })
}

function submit() {
  if (access.value.mode !== 'enter') {
    ElMessage.warning('当前节点未流转到你，暂不能提交。')
    return
  }
  const nodeDiscipline = nodeDisciplineMap[currentNodeKey.value]
  if (!isPlatformAdmin.value && nodeDiscipline && discipline.value !== nodeDiscipline) {
    discipline.value = nodeDiscipline
    loadCatalog()
    ElMessage.warning(`当前节点只能提交${disciplineLabel(nodeDiscipline)}目标与约束。`)
    return
  }
  saving.value = true
  saveObjectiveConstraints(taskId.value, {
    discipline: discipline.value,
    items: buildSubmitItems(),
    remark: remark.value
  }).then(() => {
    ElMessage.success('已提交，流程将进入下一学科或冲突校验。')
    loadTaskContext()
  }).finally(() => {
    saving.value = false
  })
}

onMounted(loadTaskContext)
onBeforeUnmount(revokeAttachmentPreviews)

watch(() => route.query.taskId, value => {
  taskId.value = value ? Number(value) : null
  discipline.value = route.query.discipline || discipline.value
  activeTab.value = route.query.tab === 'handled' ? 'handled' : activeTab.value
  loadTaskContext()
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.block-title {
  display: inline-flex;
  align-items: center;
  margin: 0 0 12px 4px;
  padding-left: 10px;
  color: #1f2a44;
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  border-left: 3px solid #4f8edc;
}

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.fault-attachment-card {
  margin-bottom: 14px;
}

.definition-expand {
  padding: 14px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

.definition-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-form-item__content) {
    display: block;
  }

  :deep(.el-select),
  :deep(.el-input),
  :deep(.el-textarea),
  :deep(.el-input-number) {
    width: 100%;
  }
}

.definition-form__wide {
  grid-column: 1 / -1;
}

.definition-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.definition-value {
  color: #1f2a44;
  font-weight: 600;
}

.definition-empty {
  color: #98a2b3;
}

.definition-auto-check {
  grid-column: 1 / -1;
  padding: 10px 12px;
  color: #5f6f84;
  font-size: 13px;
  line-height: 1.6;
  border: 1px dashed #cbd9ea;
  border-radius: 6px;
  background: #f8fbff;
}

.catalog-hint {
  margin: 10px 0 14px;
  padding: 9px 12px;
  color: #5f6f84;
  font-size: 13px;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #fbfcfe;
}

.flow-timeline {
  margin-left: 12px;
}

.flow-node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 32px;
  color: #1f2a44;
  font-size: 16px;
  font-weight: 600;
}

.flow-node--done {
  :deep(.el-timeline-item__node) {
    background-color: #67c23a;
  }

  :deep(.el-timeline-item__tail) {
    border-left-color: #67c23a;
  }
}

.flow-node--current {
  :deep(.el-timeline-item__node) {
    background-color: #409eff;
  }
}

.flow-node--pending {
  .flow-node-content {
    color: #606a78;
  }

  :deep(.el-timeline-item__node) {
    background-color: #a8abb2;
  }
}

.item-name-cell {
  min-width: 0;
  padding: 4px 0;
}

.item-name-main {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
  overflow: hidden;
  color: #1f2a44;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;

  span:last-child {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.item-name-desc {
  overflow: hidden;
  margin-top: 4px;
  color: #7a8798;
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.definition-issues {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.5;
}

.fault-overview-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.fault-task-description {
  padding: 12px 14px;
  border: 1px solid #e6ebf1;
  border-left: 3px solid #4f8edc;
  border-radius: 6px;
  background: #fbfcfe;

  b {
    display: block;
    margin-bottom: 6px;
    color: #2b5f9f;
    font-size: 13px;
  }

  p {
    margin: 0;
    color: #3e4c5f;
    font-size: 14px;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.fault-attachment-entry {
  display: grid;
  grid-template-columns: 56px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  cursor: pointer;
  border: 1px solid #e6ebf1;
  border-radius: 6px;
  background: #ffffff;
  transition: border-color 0.2s ease, background-color 0.2s ease;

  &:hover {
    border-color: #c7d6e8;
    background: #fbfcfe;
  }
}

.fault-attachment-icon {
  display: flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  color: #2f66b2;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid #c8daf4;
  border-radius: 6px;
  background: #f4f8ff;
}

.fault-attachment-meta {
  min-width: 0;

  strong {
    display: block;
    overflow: hidden;
    color: #1f2a44;
    font-size: 15px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    display: block;
    margin-top: 4px;
    color: #6f7f93;
    font-size: 13px;
  }
}

.fault-file-placeholder {
  display: flex;
  min-height: 180px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  text-align: center;
  border: 1px dashed #cbd9ea;
  border-radius: 6px;
  background: #f8fbff;

  strong {
    color: #1f2a44;
    font-size: 18px;
  }

  span {
    color: #7a8da3;
    line-height: 1.7;
  }
}

.attachment-viewer {
  display: flex;
  min-height: 420px;
  align-items: center;
  justify-content: center;
  background: #eef4fb;

  img {
    display: block;
    max-width: 100%;
    max-height: 72vh;
    object-fit: contain;
  }
}

@media (max-width: 1100px) {
  .definition-form {
    grid-template-columns: 1fr;
  }

  .fault-attachment-entry {
    grid-template-columns: 1fr;
  }
}
</style>

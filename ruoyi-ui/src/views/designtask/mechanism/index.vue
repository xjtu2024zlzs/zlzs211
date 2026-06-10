<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">COLLABORATIVE MECHANISM</p>
          <h1 class="platform-title">协同机制生成</h1>
          <p class="platform-subtitle">
            任务负责人选择已部署的 Flowable 工作流模板，指定各学科工程师与审批领导，发起起落架舱门优化任务。
          </p>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>发起人即负责人</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot"></span>
            <span>{{ definitions.length ? '已读取 Flowable 模板' : '等待 Flowable 模板' }}</span>
          </div>
        </div>
      </section>

      <div class="content-grid">
        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">TASK INFO</p>
              <h2 class="section-title">任务信息</h2>
            </div>
            <div class="section-actions">
              <el-button plain :loading="deploying" @click="deployTemplate">部署默认模板</el-button>
              <el-button type="primary" :loading="submitting" @click="submitTask">发起任务</el-button>
            </div>
          </div>

          <el-form :model="form" label-width="130px" class="mt-12">
            <el-form-item label="流程模板">
              <el-select
                v-model="form.processDefinitionId"
                placeholder="请选择 Flowable 已部署流程"
                style="width: 100%"
                :loading="loadingDefinitions"
                clearable
              >
                <el-option
                  v-for="item in definitions"
                  :key="item.definitionId"
                  :label="`${item.processName}（v${item.version}）`"
                  :value="item.definitionId"
                >
                  <span>{{ item.processName }}</span>
                  <span class="option-meta">{{ item.processKey }} / v{{ item.version }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-alert
              v-if="!loadingDefinitions && !definitions.length"
              class="template-alert"
              title="未读取到 Flowable 流程模板，请先启动 ruoyi-flowable，或点击部署默认模板后再选择。"
              type="warning"
              :closable="false"
              show-icon
            />
            <el-form-item label="任务名称">
              <el-input v-model="form.taskName" placeholder="请输入任务名称" />
            </el-form-item>
            <el-form-item label="计划时间">
              <div class="time-range">
                <el-date-picker
                  v-model="form.plannedStartTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="任务开始时间"
                  style="width: 100%"
                />
                <el-date-picker
                  v-model="form.plannedEndTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="预计结束时间"
                  style="width: 100%"
                />
              </div>
            </el-form-item>
            <el-form-item label="优先级">
              <el-radio-group v-model="form.priority">
                <el-radio :value="1">低</el-radio>
                <el-radio :value="2">中</el-radio>
                <el-radio :value="3">高</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="任务需求">
              <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入任务需求" />
            </el-form-item>
            <el-form-item label="任务附件">
              <el-upload
                v-model:file-list="fileList"
                class="attachment-upload"
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-remove="handleFileChange"
              >
                <el-button plain>
                  <el-icon><Upload /></el-icon>
                  提交附件
                </el-button>
                <template #tip>
                  <div class="upload-tip">可先选择需求说明、参数表或模型文件，发起任务时会随任务一起记录。</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>
        </section>

        <section class="section-block">
          <div class="section-header">
            <div>
              <p class="section-label">ASSIGNEE</p>
              <h2 class="section-title">处理人配置</h2>
            </div>
          </div>

          <el-form :model="form" label-width="130px" class="mt-12">
            <el-form-item v-for="role in roles" :key="role.key" :label="role.label">
              <el-select
                v-model="form[role.key]"
                filterable
                clearable
                :loading="loadingAssignees"
                :placeholder="rolePlaceholder(role)"
                style="width: 100%"
              >
                <el-option
                  v-for="user in assigneeOptions[role.key] || []"
                  :key="user.userId"
                  :label="userLabel(user)"
                  :value="Number(user.userId)"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </section>
      </div>

      <section class="section-block template-block">
        <div class="section-header">
          <div>
            <p class="section-label">FLOWABLE TEMPLATE</p>
            <h2 class="section-title">工作流模板内容</h2>
          </div>
          <el-button plain :loading="loadingDefinitions" @click="loadDefinitions">刷新流程模板</el-button>
        </div>

        <div v-if="!selectedDefinition" class="template-placeholder">
          <strong>请选择流程模板</strong>
          <span>选择后系统会读取 Flowable 节点，并检查是否能驱动当前协同优化流程。</span>
        </div>

        <template v-else>
          <div class="template-summary">
            <div>
              <span class="summary-label">流程名称</span>
              <strong>{{ selectedDefinition.processName }}</strong>
            </div>
            <div>
              <span class="summary-label">流程 Key</span>
              <strong>{{ selectedDefinition.processKey }}</strong>
            </div>
            <div>
              <span class="summary-label">版本</span>
              <strong>v{{ selectedDefinition.version }}</strong>
            </div>
            <div>
              <span class="summary-label">部署 ID</span>
              <strong>{{ selectedDefinition.deploymentId || '-' }}</strong>
            </div>
          </div>

          <el-alert
            v-if="missingNodeKeys.length"
            class="node-alert"
            :title="`当前流程模板缺少节点 Key：${missingNodeKeys.join('、')}`"
            type="warning"
            :closable="false"
            show-icon
          />

          <el-alert
            v-if="!missingNodeKeys.length && workflowNodes.length"
            class="node-alert"
            title="当前流程模板节点完整，可用于驱动起落架舱门优化任务。"
            type="success"
            :closable="false"
            show-icon
          />

          <div class="workflow-node-table">
            <div class="workflow-node-table__head">
              <span>序号</span>
              <span>节点名称</span>
              <span>节点 Key</span>
              <span>处理人表达式</span>
              <span>状态</span>
            </div>
            <div v-for="(node, index) in workflowNodes" :key="node.nodeKey || index" class="workflow-node-table__row">
              <span>{{ index + 1 }}</span>
              <strong>{{ node.nodeName }}</strong>
              <code>{{ node.nodeKey }}</code>
              <span>{{ node.assignee || '-' }}</span>
              <el-tag size="small" :type="expectedNodeKeys.includes(node.nodeKey) ? 'success' : 'info'">
                {{ expectedNodeKeys.includes(node.nodeKey) ? '已匹配' : '扩展节点' }}
              </el-tag>
            </div>
            <div v-if="!loadingNodes && !workflowNodes.length" class="workflow-node-empty">
              当前模板没有读取到用户任务节点，请检查 Flowable 流程定义。
            </div>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  deployDefaultProcess,
  listAssigneeOptions,
  listProcessDefinitionNodes,
  listProcessDefinitions,
  startDesignTask,
  uploadDesignTaskFile
} from '@/api/designtask/optimization'

const router = useRouter()
const submitting = ref(false)
const deploying = ref(false)
const loadingDefinitions = ref(false)
const loadingNodes = ref(false)
const loadingAssignees = ref(false)
const definitions = ref([])
const workflowNodes = ref([])
const assigneeOptions = ref({})
const fileList = ref([])

const form = ref({
  processDefinitionId: '',
  taskName: '液压弯管抗冲击设计制造协同优化任务',
  taskType: 'LANDING_GEAR_DOOR',
  plannedStartTime: '',
  plannedEndTime: '',
  priority: 3,
  description: '基于前置故障追因结论，液压弯管路径几何参数是影响抗冲击性能的关键因素。本任务在起落架舱门设计制造协同优化背景下，固定管道起点和终点，水平总长 600 mm，竖直总高 300 mm，关键设计变量设定为 L1、L2、θ1、θ2、R。',
  structureUserId: undefined,
  layoutUserId: undefined,
  aeroUserId: undefined,
  hydraulicUserId: undefined,
  manufacturingUserId: undefined,
  leaderUserId: undefined
})

const roles = [
  { key: 'structureUserId', label: '结构工程师' },
  { key: 'layoutUserId', label: '布局工程师' },
  { key: 'aeroUserId', label: '气动工程师' },
  { key: 'hydraulicUserId', label: '液压工程师' },
  { key: 'manufacturingUserId', label: '制造工程师' },
  { key: 'leaderUserId', label: '审批领导' }
]

const nodes = [
  { key: 'structure_select', name: '结构目标约束' },
  { key: 'layout_select', name: '布局目标约束' },
  { key: 'aero_select', name: '气动目标约束' },
  { key: 'hydraulic_select', name: '液压目标约束' },
  { key: 'manufacturing_select', name: '制造目标约束' },
  { key: 'conflict_check', name: '目标约束冲突校验' },
  { key: 'model_decompose_solve', name: '模型解耦求解' },
  { key: 'simulation_confirm', name: '仿真验证确认' },
  { key: 'leader_approve', name: '领导审批' }
]

const expectedNodeKeys = nodes.map(item => item.key)
const selectedDefinition = computed(() => definitions.value.find(item => item.definitionId === form.value.processDefinitionId))
const missingNodeKeys = computed(() => {
  const actualKeys = workflowNodes.value.map(item => item.nodeKey)
  return expectedNodeKeys.filter(key => !actualKeys.includes(key))
})

function userLabel(user) {
  const nickName = user.nickName || user.userName
  return `${nickName}（${user.userName} / ID ${user.userId}）`
}

function rolePlaceholder(role) {
  const users = assigneeOptions.value[role.key] || []
  return users.length ? `请选择${role.label}` : `该角色暂无可选用户`
}

function loadAssignees() {
  loadingAssignees.value = true
  listAssigneeOptions().then(res => {
    assigneeOptions.value = res.data || {}
  }).finally(() => {
    loadingAssignees.value = false
  })
}

function loadDefinitions() {
  loadingDefinitions.value = true
  listProcessDefinitions().then(res => {
    definitions.value = res.data || []
  }).finally(() => {
    loadingDefinitions.value = false
  })
}

function loadWorkflowNodes(processDefinitionId) {
  workflowNodes.value = []
  if (!processDefinitionId) return
  loadingNodes.value = true
  listProcessDefinitionNodes(processDefinitionId).then(res => {
    workflowNodes.value = res.data || []
  }).finally(() => {
    loadingNodes.value = false
  })
}

function deployTemplate() {
  deploying.value = true
  deployDefaultProcess().then(res => {
    ElMessage.success(`已部署流程模板：${res.data?.processName || '起落架舱门优化流程'}`)
    loadDefinitions()
  }).finally(() => {
    deploying.value = false
  })
}

function handleFileChange() {
  fileList.value = [...fileList.value]
}

async function buildAttachments() {
  const attachments = []
  for (const file of fileList.value) {
    let filePath = file.url || ''
    let storedName = file.name
    if (!filePath && file.raw) {
      const res = await uploadDesignTaskFile(file.raw)
      filePath = res.data?.url || ''
      storedName = res.data?.name || file.name
    }
    attachments.push({
      fileName: file.name || storedName,
      filePath,
      fileSize: file.size || file.raw?.size,
      fileType: file.raw?.type || file.type || ''
    })
  }
  return attachments
}

function validateBeforeSubmit() {
  if (!form.value.processDefinitionId) {
    ElMessage.warning('请先选择 Flowable 已部署流程模板')
    return false
  }
  if (!form.value.taskName) {
    ElMessage.warning('请填写任务名称')
    return false
  }
  if (form.value.plannedStartTime && form.value.plannedEndTime && form.value.plannedStartTime > form.value.plannedEndTime) {
    ElMessage.warning('预计结束时间不能早于任务开始时间')
    return false
  }
  const missingRole = roles.find(role => !form.value[role.key])
  if (missingRole) {
    ElMessage.warning(`请选择${missingRole.label}`)
    return false
  }
  return true
}

async function submitTask() {
  if (!validateBeforeSubmit()) return
  submitting.value = true
  try {
    const attachments = await buildAttachments()
    const res = await startDesignTask({
      ...form.value,
      attachments
    })
    const taskId = res.data?.task?.taskId
    ElMessage.success('任务已发起，流程进入结构工程师目标约束选择')
    router.push({ path: '/designtask/dashboard', query: { taskId } })
  } finally {
    submitting.value = false
  }
}

watch(() => form.value.processDefinitionId, loadWorkflowNodes)
onMounted(() => {
  loadAssignees()
  loadDefinitions()
})
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.option-meta {
  float: right;
  color: #8a95a8;
  font-size: 12px;
}

.template-alert {
  margin: -4px 0 18px 130px;
  width: calc(100% - 130px);
}

.time-range {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
}

.attachment-upload {
  width: 100%;
}

.upload-tip {
  color: #8a95a8;
  font-size: 12px;
  line-height: 1.7;
}

.template-block {
  margin-top: 24px;
}

.template-placeholder {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 96px;
  justify-content: center;
  padding: 18px 20px;
  border: 1px dashed #cfd8e6;
  border-radius: 8px;
  background: #f8fafd;
  color: #6b7688;

  strong {
    color: #253047;
    font-size: 15px;
  }
}

.template-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin: 12px 0 18px;

  > div {
    min-width: 0;
    padding: 12px 14px;
    border: 1px solid #e5e9f2;
    border-radius: 8px;
    background: #f8fafd;
  }

  strong {
    display: block;
    margin-top: 4px;
    color: #172033;
    font-size: 14px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.summary-label {
  color: #6b7688;
  font-size: 12px;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-alert {
  margin: 12px 0 16px;
}

.workflow-node-table {
  border: 1px solid #e5e9f2;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.workflow-node-table__head,
.workflow-node-table__row {
  display: grid;
  grid-template-columns: 64px minmax(160px, 1.1fr) minmax(180px, 1fr) minmax(160px, 1fr) 96px;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.workflow-node-table__head {
  background: #f6f8fb;
  color: #6b7688;
  font-size: 12px;
  font-weight: 600;
}

.workflow-node-table__row {
  border-top: 1px solid #edf1f7;
  color: #253047;
  font-size: 13px;

  strong,
  code,
  span {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  code {
    color: #2454a6;
    background: #eef5ff;
    border-radius: 4px;
    padding: 3px 6px;
  }
}

.workflow-node-empty {
  padding: 22px;
  color: #8a95a8;
  text-align: center;
}

@media (max-width: 1200px) {
  .template-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .workflow-node-table__head,
  .workflow-node-table__row {
    grid-template-columns: 48px minmax(140px, 1fr) minmax(150px, 1fr);

    span:nth-child(4),
    span:nth-child(5),
    .el-tag {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .template-alert {
    margin-left: 0;
    width: 100%;
  }

  .time-range,
  .template-summary {
    grid-template-columns: 1fr;
  }
}
</style>

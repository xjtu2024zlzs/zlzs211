<template>
  <div class="app-container task-create-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <span>流程发起</span>
          <span class="tip">按步骤创建设计任务</span>
        </div>
      </template>

      <el-steps :active="step" finish-status="success" align-center class="steps">
        <el-step title="选择类型" />
        <el-step title="配置流程" />
        <el-step title="填写信息" />
        <el-step title="确认提交" />
      </el-steps>

      <div class="wizard-body">
        <div v-show="step === 0">
          <el-divider content-position="left">选择任务类型</el-divider>
          <el-row :gutter="16">
            <el-col v-for="type in taskTypes" :key="type.taskType" :xs="24" :sm="12" :md="8">
              <el-card
                  shadow="hover"
                  :class="['type-card', { active: form.taskType === type.taskType }]"
                  @click="selectTaskType(type)"
              >
                <div class="type-title">{{ type.typeName }}</div>
                <div class="type-desc">{{ type.typeDesc }}</div>
                <div class="type-footer">
                  <el-tag size="small" effect="plain">负责人：{{ getResponsibleRoleName(type.taskType) }}</el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div v-show="step === 1">
          <el-divider content-position="left">配置流程模板</el-divider>
          <el-form label-width="120px">
            <el-form-item label="流程模板">
              <el-select
                  v-model="form.templateId"
                  placeholder="请选择流程模板"
                  style="width: 380px"
                  @change="handleTemplateChange"
              >
                <el-option
                    v-for="template in flowTemplates"
                    :key="template.templateId"
                    :label="template.templateName"
                    :value="template.templateId"
                />
              </el-select>
            </el-form-item>
          </el-form>

          <el-card v-if="selectedTemplate" shadow="never" class="preview-card">
            <template #header>
              <div class="card-header">
                <span>流程预览</span>
                <span class="tip">{{ selectedTemplate.templateName }}</span>
              </div>
            </template>
            <div class="flow-list">
              <div v-for="(node, index) in selectedTemplate.nodes" :key="node.nodeCode || index" class="flow-node">
                <div class="flow-index">{{ index + 1 }}</div>
                <div>
                  <div class="flow-name">{{ node.nodeName }}</div>
                  <div class="flow-tags">
                    <el-tag v-if="node.collaborators && node.collaborators.length" size="small" type="warning">
                      协作 {{ node.collaborators.length }} 人
                    </el-tag>
                    <el-tag v-if="node.parallelGroup" size="small" type="info">并行节点</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <div v-show="step === 2">
          <el-divider content-position="left">填写任务信息</el-divider>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" placeholder="请输入任务名称" maxlength="200" show-word-limit />
            </el-form-item>

            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="form.priority">
                <el-radio :label="1">低</el-radio>
                <el-radio :label="2">中</el-radio>
                <el-radio :label="3">高</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="问题描述" prop="description">
              <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="6"
                  placeholder="请详细描述需要解决的问题"
                  maxlength="2000"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item label="附件上传">
              <el-upload
                  action="#"
                  :auto-upload="false"
                  :file-list="fileList"
                  :on-change="handleFileChange"
                  :on-remove="handleFileRemove"
                  :limit="10"
              >
                <el-button type="primary">选择文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 PDF、Word、Excel、图片和模型文件</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>
        </div>

        <div v-show="step === 3">
          <el-divider content-position="left">确认提交</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务类型">{{ getSelectedTypeName() }}</el-descriptions-item>
            <el-descriptions-item label="流程模板">{{ getSelectedTemplateName() }}</el-descriptions-item>
            <el-descriptions-item label="任务名称">{{ form.taskName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityTag(form.priority)">{{ getPriorityLabel(form.priority) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="负责人">{{ getResponsibleRoleName(form.taskType) }}</el-descriptions-item>
            <el-descriptions-item label="协作角色">{{ getCollaboratorNames(form.taskType) }}</el-descriptions-item>
            <el-descriptions-item label="问题描述" :span="2">{{ form.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="附件数量">{{ fileList.length }} 个文件</el-descriptions-item>
          </el-descriptions>
          <el-alert title="提交提示" type="info" :closable="false" show-icon class="mt-16">
            确认无误后点击“提交任务”。
          </el-alert>
        </div>
      </div>

      <div class="footer-actions">
        <el-button v-if="step > 0" @click="handlePrev">上一步</el-button>
        <el-button v-if="step < 3" type="primary" :disabled="!canNext" @click="handleNext">下一步</el-button>
        <el-button v-if="step === 3" @click="handleSaveDraft">保存草稿</el-button>
        <el-button v-if="step === 3" type="primary" :loading="submitLoading" @click="handleSubmit">
          提交任务
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const step = ref(0)
const submitLoading = ref(false)
const formRef = ref()
const fileList = ref([])
const selectedTemplate = ref(null)

const form = reactive({
  taskType: '',
  templateId: '',
  taskName: '',
  priority: 2,
  description: '',
  collaboratorRoles: []
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const taskTypes = ref([
  {
    taskType: 'HYDRAULIC_BEND_IMPACT_OPTIMIZATION',
    typeName: '液压弯管抗冲击优化',
    typeDesc: '面向液压弯管在冲击载荷下的结构优化与抗冲击性能提升',
    collaboratorRoles: 'STRUCTURE,LAYOUT,MANUFACTURE'
  },
  {
    taskType: 'FRAME_BEAM_FATIGUE_AND_REPAIR',
    typeName: '框梁疲劳预测及维修决策推荐',
    typeDesc: '面向框梁结构疲劳寿命评估、损伤预测与维修决策推荐',
    collaboratorRoles: 'STRUCTURE,MANUFACTURE,HYDRAULIC'
  },
  {
    taskType: 'CABLE_PIPELINE_JOINT_LAYOUT',
    typeName: '线缆管路联合布局',
    typeDesc: '面向线缆与管路的协同布局、干涉校核与空间约束优化',
    collaboratorRoles: 'STRUCTURE,HYDRAULIC,MANUFACTURE'
  }
])

const flowTemplates = ref([
  {
    templateId: 1,
    templateName: '液压弯管抗冲击优化流程',
    templateDesc: '适用于液压弯管抗冲击性能优化任务',
    templateConfig: JSON.stringify([
      { nodeCode: 'N1', nodeName: '任务创建', collaborators: ['STRUCTURE'] },
      { nodeCode: 'N2', nodeName: '结构分析', collaborators: ['LAYOUT', 'MANUFACTURE'] },
      { nodeCode: 'N3', nodeName: '优化设计', collaborators: ['STRUCTURE'] },
      { nodeCode: 'N4', nodeName: '结果确认', collaborators: [] }
    ])
  },
  {
    templateId: 2,
    templateName: '框梁疲劳预测与维修决策流程',
    templateDesc: '适用于框梁疲劳寿命预测和维修推荐任务',
    templateConfig: JSON.stringify([
      { nodeCode: 'N1', nodeName: '任务创建', collaborators: ['STRUCTURE'] },
      { nodeCode: 'N2', nodeName: '疲劳评估', collaborators: ['STRUCTURE', 'MANUFACTURE'] },
      { nodeCode: 'N3', nodeName: '维修决策分析', collaborators: ['HYDRAULIC'] },
      { nodeCode: 'N4', nodeName: '结果确认', collaborators: [] }
    ])
  },
  {
    templateId: 3,
    templateName: '线缆管路联合布局流程',
    templateDesc: '适用于线缆与管路联合布置和干涉检查任务',
    templateConfig: JSON.stringify([
      { nodeCode: 'N1', nodeName: '任务创建', collaborators: ['LAYOUT'] },
      { nodeCode: 'N2', nodeName: '联合布局设计', collaborators: ['STRUCTURE', 'HYDRAULIC'] },
      { nodeCode: 'N3', nodeName: '干涉校核', collaborators: ['MANUFACTURE'] },
      { nodeCode: 'N4', nodeName: '结果确认', collaborators: [] }
    ])
  }
])

const canNext = computed(() => {
  if (step.value === 0) return !!form.taskType
  if (step.value === 1) return !!form.templateId
  if (step.value === 2) return !!form.taskName && !!form.description
  return true
})

function selectTaskType(type) {
  form.taskType = type.taskType
  form.collaboratorRoles = type.collaboratorRoles ? type.collaboratorRoles.split(',') : []
}

function handleTemplateChange(templateId) {
  const template = flowTemplates.value.find(item => item.templateId === templateId)
  if (!template) {
    selectedTemplate.value = null
    return
  }
  selectedTemplate.value = { ...template, nodes: [] }
  try {
    selectedTemplate.value.nodes = JSON.parse(template.templateConfig || '[]')
  } catch (e) {
    selectedTemplate.value.nodes = []
  }
}

function getSelectedTypeName() {
  const type = taskTypes.value.find(item => item.taskType === form.taskType)
  return type ? type.typeName : '-'
}

function getSelectedTemplateName() {
  const template = flowTemplates.value.find(item => item.templateId === form.templateId)
  return template ? template.templateName : '-'
}

function getResponsibleRoleName(taskType) {
  return {
    HYDRAULIC_BEND_IMPACT_OPTIMIZATION: '液压设计工程师',
    FRAME_BEAM_FATIGUE_AND_REPAIR: '结构分析工程师',
    CABLE_PIPELINE_JOINT_LAYOUT: '布局设计工程师'
  }[taskType] || '待配置'
}

function getCollaboratorNames(taskType) {
  const type = taskTypes.value.find(item => item.taskType === taskType)
  if (!type || !type.collaboratorRoles) return '无'
  const map = {
    STRUCTURE: '结构设计工程师',
    LAYOUT: '布局设计工程师',
    MANUFACTURE: '工艺设计工程师',
    HYDRAULIC: '液压设计工程师'
  }
  return type.collaboratorRoles.split(',').map(role => map[role] || role).join('、')
}

function getPriorityLabel(priority) {
  return { 1: '低', 2: '中', 3: '高' }[priority] || '中'
}

function getPriorityTag(priority) {
  return { 1: 'info', 2: 'warning', 3: 'danger' }[priority] || 'info'
}

function handlePrev() {
  if (step.value > 0) step.value -= 1
}

function handleNext() {
  if (step.value < 3) step.value += 1
}

function handleSaveDraft() {
  ElMessage.success('草稿已保存')
}

function handleSubmit() {
  formRef.value?.validate?.(valid => {
    if (!valid) return
    submitLoading.value = true
    setTimeout(() => {
      submitLoading.value = false
      ElMessage.success('提交成功')
    }, 400)
  })
}

function handleFileChange(file, list) {
  fileList.value = list
}

function handleFileRemove(file, list) {
  fileList.value = list
}
</script>

<style scoped>
.task-create-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-card {
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.tip {
  color: #909399;
  font-size: 13px;
}

.steps {
  margin-bottom: 24px;
}

.wizard-body {
  min-height: 420px;
}

.type-card {
  cursor: pointer;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
}

.type-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.type-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.type-desc {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
}

.type-footer {
  margin-top: 12px;
}

.preview-card {
  margin-top: 12px;
}

.flow-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.flow-node {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.flow-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.flow-name {
  font-weight: 600;
  color: #303133;
}

.flow-tags {
  display: flex;
  gap: 8px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.footer-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.mt-16 {
  margin-top: 16px;
}
</style>
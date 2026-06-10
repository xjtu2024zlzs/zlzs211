<template>
  <div class="app-container designtask-page">
    <el-card shadow="never" class="page-header-card">
      <div class="page-header">
        <div>
          <div class="page-title">课题二 · 设计任务平台</div>
          <div class="page-desc">统一管理任务管理与流程发起模块，保持与若依平台一致的视觉和交互风格。</div>
        </div>
        <div class="page-actions">
          <el-button @click="activeTab = 'manage'">任务管理</el-button>
          <el-button type="primary" @click="activeTab = 'create'">流程发起</el-button>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="task-tabs" @tab-click="handleTabClick">
      <el-tab-pane label="任务管理" name="manage">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">全部任务</div>
              <div class="stat-value">{{ taskList.length }}</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">进行中</div>
              <div class="stat-value stat-primary">{{ stats.running }}</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">已完成</div>
              <div class="stat-value stat-success">{{ stats.finished }}</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">待处理</div>
              <div class="stat-value stat-warning">{{ stats.pending }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header-row">
              <span>任务列表</span>
              <div class="header-tools">
                <el-input v-model="query.keyword" placeholder="搜索任务名称" clearable style="width: 240px" @keyup.enter="handleSearch" />
                <el-select v-model="query.status" placeholder="任务状态" clearable style="width: 160px">
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="进行中" value="RUNNING" />
                  <el-option label="已完成" value="FINISHED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
              </div>
            </div>
          </template>

          <el-table :data="filteredTaskList" stripe border v-loading="loading">
            <el-table-column label="任务名称" prop="taskName" min-width="200" show-overflow-tooltip />
            <el-table-column label="任务类型" prop="taskTypeName" width="160" />
            <el-table-column label="流程模板" prop="templateName" width="180" />
            <el-table-column label="优先级" width="100">
              <template #default="scope">
                <el-tag :type="getPriorityTag(scope.row.priority)">{{ getPriorityLabel(scope.row.priority) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建人" prop="createBy" width="120" />
            <el-table-column label="创建时间" prop="createTime" width="180" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
                <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="removeTask(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="流程发起" name="create">
        <el-card shadow="never" class="wizard-card">
          <el-steps :active="step" finish-status="success" align-center class="wizard-steps">
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
                    <div class="type-card-head">
                      <div class="type-icon" :style="{ backgroundColor: type.color || '#409EFF' }">{{ type.icon || 'T' }}</div>
                      <div>
                        <div class="type-name">{{ type.typeName }}</div>
                        <div class="type-desc">{{ type.typeDesc }}</div>
                      </div>
                    </div>
                    <div class="type-meta">
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
                  <el-select v-model="form.templateId" placeholder="请选择流程模板" style="width: 380px" @change="handleTemplateChange">
                    <el-option v-for="template in flowTemplates" :key="template.templateId" :label="template.templateName" :value="template.templateId" />
                  </el-select>
                  <el-button text type="primary" class="ml-12" @click="showTemplateDialog = true">查看模板</el-button>
                  <el-button text type="primary">新建模板</el-button>
                </el-form-item>
              </el-form>
              <el-card v-if="selectedTemplate" shadow="never" class="preview-card">
                <template #header>
                  <div class="card-header-row">
                    <span>流程预览</span>
                    <span class="muted-text">{{ selectedTemplate.templateName }}</span>
                  </div>
                </template>
                <div class="flow-list">
                  <div v-for="(node, index) in selectedTemplate.nodes" :key="node.nodeCode || index" class="flow-node">
                    <div class="flow-index">{{ index + 1 }}</div>
                    <div class="flow-content">
                      <div class="flow-name">{{ node.nodeName }}</div>
                      <div class="flow-tags">
                        <el-tag v-if="node.collaborators && node.collaborators.length" size="small" type="warning">协作 {{ node.collaborators.length }} 人</el-tag>
                        <el-tag v-if="node.parallelGroup" size="small" type="info">并行节点</el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>

            <div v-show="step === 2">
              <el-divider content-position="left">填写任务信息</el-divider>
              <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form-grid">
                <el-form-item label="任务名称" prop="taskName">
                  <el-input v-model="form.taskName" placeholder="请输入任务名称" maxlength="200" show-word-limit />
                </el-form-item>
                <el-form-item label="优先级" prop="priority">
                  <el-radio-group v-model="form.priority">
                    <el-radio :value="1">低</el-radio>
                    <el-radio :value="2">中</el-radio>
                    <el-radio :value="3">高</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="问题描述" prop="description">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="6"
                    placeholder="请详细描述需要解决的问题，包括背景、目标和约束条件"
                    maxlength="2000"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="附件上传">
                  <el-upload action="#" :auto-upload="false" :file-list="fileList" :on-change="handleFileChange" :on-remove="handleFileRemove" :limit="10">
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
                确认无误后点击“提交任务”，任务将进入流程创建阶段。
              </el-alert>
            </div>
          </div>

          <div class="wizard-footer">
            <el-button v-if="step > 0" @click="handlePrev">上一步</el-button>
            <el-button v-if="step < 3" type="primary" :disabled="!canNext" @click="handleNext">下一步</el-button>
            <el-button v-if="step === 3" @click="handleSaveDraft">保存草稿</el-button>
            <el-button v-if="step === 3" type="primary" :loading="submitLoading" @click="handleSubmit">提交任务</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showTemplateDialog" title="流程模板预览" width="720px" append-to-body>
      <el-table :data="flowTemplates" border max-height="360">
        <el-table-column label="模板名称" prop="templateName" min-width="180" />
        <el-table-column label="模板描述" prop="templateDesc" min-width="240" show-overflow-tooltip />
        <el-table-column label="节点数" width="90">
          <template #default="scope">
            {{ getTemplateNodeCount(scope.row) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showTemplateDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="任务详情" width="720px" append-to-body>
      <el-descriptions v-if="currentTask" :column="2" border>
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ getStatusLabel(currentTask.status) }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ currentTask.taskTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程模板">{{ currentTask.templateName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentTask.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ currentTask.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DesignTaskPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addTask, listFlowTemplate, listTask, listTaskTypes } from '@/api/designtask/task'

const activeTab = ref('manage')
const loading = ref(false)
const submitLoading = ref(false)
const step = ref(0)
const showTemplateDialog = ref(false)
const showDetailDialog = ref(false)
const currentTask = ref(null)
const formRef = ref()
const flowTemplates = ref([])
const taskTypes = ref([])
const selectedTemplate = ref(null)
const taskList = ref([
  { taskName: '结构优化设计任务', taskTypeName: '结构设计', templateName: '标准流程A', priority: 3, status: 'RUNNING', createBy: 'admin', createTime: '2026-06-02 14:30:00' },
  { taskName: '布局评审任务', taskTypeName: '布局设计', templateName: '标准流程B', priority: 2, status: 'PENDING', createBy: 'user1', createTime: '2026-06-02 13:20:00' },
  { taskName: '工艺校核任务', taskTypeName: '工艺设计', templateName: '标准流程C', priority: 1, status: 'FINISHED', createBy: 'user2', createTime: '2026-06-01 18:00:00' }
])
const fileList = ref([])
const query = reactive({ keyword: '', status: '' })
const form = reactive({ taskType: '', templateId: '', taskName: '', priority: 2, description: '', collaboratorRoles: [] })
const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const stats = computed(() => ({
  running: taskList.value.filter(item => item.status === 'RUNNING').length,
  finished: taskList.value.filter(item => item.status === 'FINISHED').length,
  pending: taskList.value.filter(item => item.status === 'PENDING').length
}))

const filteredTaskList = computed(() => {
  const keyword = (query.keyword || '').trim().toLowerCase()
  return taskList.value.filter(item => {
    const matchedKeyword = !keyword || [item.taskName, item.taskTypeName, item.templateName].some(v => (v || '').toLowerCase().includes(keyword))
    const matchedStatus = !query.status || item.status === query.status
    return matchedKeyword && matchedStatus
  })
})

const canNext = computed(() => {
  if (step.value === 0) return !!form.taskType
  if (step.value === 1) return !!form.templateId
  if (step.value === 2) return !!form.taskName && !!form.description
  return true
})

function initMockData() {
  // taskTypes 和 flowTemplates 已改为真实接口加载，此函数不再需要
}

function loadTaskTypes() {
  listTaskTypes().then(res => {
    if (res && res.data) {
      taskTypes.value = res.data
    }
  })
}

function loadFlowTemplates() {
  listFlowTemplate({}).then(res => {
    if (res && res.rows) {
      flowTemplates.value = res.rows
    }
  })
}

function handleTabClick(pane) {
  if (pane.props?.name === 'manage') {
    loadTaskList()
  }
}

function loadTaskList() {
  loading.value = true
  listTask({}).then(res => {
    if (res && res.rows) {
      taskList.value = res.rows
    }
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  if (activeTab.value === 'manage') loadTaskList()
}

function resetQuery() {
  query.keyword = ''
  query.status = ''
}

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

function handleViewTemplate() {
  showTemplateDialog.value = true
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
  const map = { STRUCTURE: '结构设计工程师', LAYOUT: '布局设计工程师', MANUFACTURE: '工艺设计工程师' }
  return map[taskType] || '待配置'
}

function getCollaboratorNames(taskType) {
  const type = taskTypes.value.find(item => item.taskType === taskType)
  if (!type || !type.collaboratorRoles) return '无'
  const map = { STRUCTURE: '结构设计工程师', LAYOUT: '布局设计工程师', MANUFACTURE: '工艺设计工程师', HYDRAULIC: '液压设计工程师' }
  return type.collaboratorRoles.split(',').map(role => map[role] || role).join('、')
}

function getTaskDescription(taskType) {
  const type = taskTypes.value.find(item => item.taskType === taskType)
  return type ? type.typeDesc : '无'
}

function getRoleName(role) {
  const map = { STRUCTURE: '结构设计工程师', LAYOUT: '布局设计工程师', MANUFACTURE: '工艺设计工程师', HYDRAULIC: '液压设计工程师' }
  return map[role] || role
}

function getRoleTagType(role) {
  const map = { STRUCTURE: 'success', LAYOUT: 'primary', MANUFACTURE: 'warning', HYDRAULIC: 'info' }
  return map[role] || 'info'
}

function getPriorityLabel(priority) {
  return ({ 1: '低', 2: '中', 3: '高' })[priority] || '中'
}

function getPriorityTag(priority) {
  return ({ 1: 'info', 2: 'warning', 3: 'danger' })[priority] || 'info'
}

function getStatusLabel(status) {
  return ({ PENDING: '待处理', RUNNING: '进行中', FINISHED: '已完成', CANCELLED: '已取消' })[status] || status || '-'
}

function getStatusTag(status) {
  return ({ PENDING: 'warning', RUNNING: 'primary', FINISHED: 'success', CANCELLED: 'info' })[status] || 'info'
}

function getTemplateNodeCount(row) {
  try {
    return JSON.parse(row.templateConfig || '[]').length
  } catch (e) {
    return 0
  }
}

function getAllCollaborators() {
  if (!selectedTemplate.value || !selectedTemplate.value.nodes) return []
  const set = new Set()
  selectedTemplate.value.nodes.forEach(node => {
    ;(node.collaborators || []).forEach(role => set.add(role))
  })
  return Array.from(set)
}

function handleFileChange(file, fileListData) {
  fileList.value = fileListData
}

function handleFileRemove(file, fileListData) {
  fileList.value = fileListData
}

function handlePrev() {
  if (step.value > 0) step.value -= 1
}

function handleNext() {
  if (step.value < 3) step.value += 1
}

function handleSaveDraft() {
  ElMessage.success('草稿已保存（当前为前端演示模式）')
}

function openDetail(row) {
  currentTask.value = row
  showDetailDialog.value = true
}

function openEdit(row) {
  ElMessage.info(`编辑任务：${row.taskName}`)
}

function removeTask(row) {
  ElMessageBox.confirm(`确认删除任务「${row.taskName}」吗？`, '提示', { type: 'warning' }).then(() => {
    taskList.value = taskList.value.filter(item => item !== row)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleSubmit() {
  formRef.value?.validate?.(valid => {
    if (!valid) return
    submitLoading.value = true
    addTask({
      taskType: form.taskType,
      templateId: form.templateId,
      taskName: form.taskName,
      priority: form.priority,
      description: form.description,
      collaboratorRoles: form.collaboratorRoles.join(',')
    }).then(() => {
      ElMessage.success('提交成功')
      step.value = 0
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

onMounted(() => {
  loadTaskTypes()
  loadFlowTemplates()
})
</script>

<style scoped>
.designtask-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header-card {
  margin-bottom: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-desc {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.task-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 16px;
  background: #fff;
  border-radius: 8px 8px 0 0;
}

.stat-card,
.table-card,
.wizard-card,
.preview-card {
  margin-bottom: 16px;
}

.stat-label {
  color: #909399;
  font-size: 13px;
}

.stat-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-primary { color: #409eff; }
.stat-success { color: #67c23a; }
.stat-warning { color: #e6a23c; }

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.muted-text {
  color: #909399;
  font-size: 13px;
}

.ml-12 { margin-left: 12px; }
.mt-16 { margin-top: 16px; }

.wizard-card {
  padding-bottom: 8px;
}

.wizard-steps {
  margin-bottom: 24px;
}

.wizard-body {
  min-height: 420px;
}

.wizard-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.type-card {
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
  margin-bottom: 16px;
}

.type-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.type-card-head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex: none;
}

.type-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.type-desc {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.type-meta {
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

.form-grid :deep(.el-input),
.form-grid :deep(.el-textarea) {
  width: 100%;
  max-width: 720px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-actions,
  .header-tools,
  .wizard-footer {
    width: 100%;
  }
}
</style>

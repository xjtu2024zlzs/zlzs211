<template>
  <div class="app-container task-create-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <span>���̷���</span>
          <span class="tip">��ҳ��д���������Ϣ�����貽���л�</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="single-form">
        <el-divider content-position="left">1. ѡ����������</el-divider>
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
                <el-tag size="small" effect="plain">�����ˣ�{{ getResponsibleRoleName(type.taskType) }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider content-position="left">2. ��������ģ��</el-divider>
        <el-form-item label="����ģ��" prop="templateId">
          <el-select v-model="form.templateId" placeholder="��ѡ������ģ��" style="width: 380px" @change="handleTemplateChange">
            <el-option v-for="template in flowTemplates" :key="template.templateId" :label="template.templateName" :value="template.templateId" />
          </el-select>
          <el-button text type="primary" class="ml-12" @click="showTemplateDialog = true">�鿴ģ��</el-button>
          <el-button text type="primary" class="ml-12" @click="resetTemplatePreview">���Ԥ��</el-button>
        </el-form-item>

        <el-card v-if="selectedTemplate" shadow="never" class="preview-card">
          <template #header>
            <div class="card-header-row">
              <span>����Ԥ��</span>
              <span class="muted-text">{{ selectedTemplate.templateName }}</span>
            </div>
          </template>
          <div class="flow-list">
            <div v-for="(node, index) in selectedTemplate.nodes" :key="node.nodeCode || index" class="flow-node">
              <div class="flow-index">{{ index + 1 }}</div>
              <div class="flow-content">
                <div class="flow-name">{{ node.nodeName }}</div>
                <div class="flow-tags">
                  <el-tag v-if="node.collaborators && node.collaborators.length" size="small" type="warning">
                    Э�� {{ node.collaborators.length }} ��
                  </el-tag>
                  <el-tag v-if="node.parallelGroup" size="small" type="info">���нڵ�</el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <el-divider content-position="left">3. ��д������Ϣ</el-divider>
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="��������" prop="taskName">
              <el-input v-model="form.taskName" placeholder="��������������" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="���ȼ�" prop="priority">
              <el-radio-group v-model="form.priority">
                <el-radio :label="1">��</el-radio>
                <el-radio :label="2">��</el-radio>
                <el-radio :label="3">��</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="��������" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="����ϸ������Ҫ��������⣬����������Ŀ���Լ������"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="������">
          <el-input :model-value="getResponsibleRoleName(form.taskType)" disabled />
        </el-form-item>

        <el-form-item label="Э����ɫ">
          <el-input :model-value="getCollaboratorNames(form.taskType)" disabled />
        </el-form-item>

        <el-divider content-position="left">4. �����ϴ�</el-divider>
        <el-form-item label="�����ϴ�">
          <el-upload
            action="#"
            :auto-upload="false"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="10"
          >
            <el-button type="primary">ѡ���ļ�</el-button>
            <template #tip>
              <div class="el-upload__tip">֧�� PDF��Word��Excel��ͼƬ��ģ���ļ�</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-divider content-position="left">5. �ύԤ��</el-divider>
        <el-descriptions :column="2" border class="summary-descriptions">
          <el-descriptions-item label="��������">{{ getSelectedTypeName() }}</el-descriptions-item>
          <el-descriptions-item label="����ģ��">{{ getSelectedTemplateName() }}</el-descriptions-item>
          <el-descriptions-item label="��������">{{ form.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="���ȼ�">
            <el-tag :type="getPriorityTag(form.priority)">{{ getPriorityLabel(form.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="������">{{ getResponsibleRoleName(form.taskType) }}</el-descriptions-item>
          <el-descriptions-item label="Э����ɫ">{{ getCollaboratorNames(form.taskType) }}</el-descriptions-item>
          <el-descriptions-item label="��������" :span="2">{{ form.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="��������">{{ fileList.length }} ���ļ�</el-descriptions-item>
        </el-descriptions>

        <el-alert title="�ύ��ʾ" type="info" :closable="false" show-icon class="mt-16">
          ȷ������������ύ���񡱣�����ֱ�ӽ��봴�����̡�
        </el-alert>
      </el-form>

      <div class="footer-actions">
        <el-button @click="handleSaveDraft">����ݸ�</el-button>
        <el-button @click="handleReset">����</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">�ύ����</el-button>
      </div>
    </el-card>

    <el-dialog v-model="showTemplateDialog" title="����ģ��Ԥ��" width="720px" append-to-body>
      <el-table :data="flowTemplates" border max-height="360">
        <el-table-column label="ģ������" prop="templateName" min-width="180" />
        <el-table-column label="ģ������" prop="templateDesc" min-width="240" show-overflow-tooltip />
        <el-table-column label="�ڵ���" width="90">
          <template #default="scope">
            {{ getTemplateNodeCount(scope.row) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showTemplateDialog = false">�ر�</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DesignTaskCreate2">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addTask, listFlowTemplate, listTaskTypes } from '@/api/designtask/task'

const submitLoading = ref(false)
const showTemplateDialog = ref(false)
const formRef = ref()
const flowTemplates = ref([])
const taskTypes = ref([])
const selectedTemplate = ref(null)
const fileList = ref([])

const form = reactive({
  taskType: '',
  templateId: '',
  taskName: '',
  priority: 2,
  description: '',
  collaboratorRoles: []
})

const rules = {
  taskType: [{ required: true, message: '��ѡ����������', trigger: 'change' }],
  templateId: [{ required: true, message: '��ѡ������ģ��', trigger: 'change' }],
  taskName: [{ required: true, message: '��������������', trigger: 'blur' }],
  description: [{ required: true, message: '��������������', trigger: 'blur' }]
}

const defaultTaskTypes = [
  {
    taskType: 'STRUCTURE',
    typeName: '�ṹ���',
    typeDesc: '�ṹ�Ż���ǿ�ȷ����ͽṹУ��',
    color: '#409EFF',
    icon: 'S',
    collaboratorRoles: 'LAYOUT,MANUFACTURE'
  },
  {
    taskType: 'LAYOUT',
    typeName: '�������',
    typeDesc: '�������֡�װ���ϵ�Ϳռ�Լ��',
    color: '#67C23A',
    icon: 'L',
    collaboratorRoles: 'STRUCTURE,HYDRAULIC'
  },
  {
    taskType: 'MANUFACTURE',
    typeName: '�������',
    typeDesc: '���칤�ա��ӹ�Լ���͹���·��',
    color: '#E6A23C',
    icon: 'M',
    collaboratorRoles: 'STRUCTURE,LAYOUT'
  }
]

const defaultFlowTemplates = [
  {
    templateId: 1,
    templateName: '��׼����A',
    templateDesc: '�ʺϳ����������',
    templateConfig: JSON.stringify([
      { nodeCode: 'N1', nodeName: '���񴴽�', collaborators: ['LAYOUT'] },
      { nodeCode: 'N2', nodeName: '�������', collaborators: ['STRUCTURE', 'MANUFACTURE'] },
      { nodeCode: 'N3', nodeName: '���ȷ��', collaborators: [] }
    ])
  },
  {
    templateId: 2,
    templateName: '��׼����B',
    templateDesc: '�ʺϿ�רҵЭͬ����',
    templateConfig: JSON.stringify([
      { nodeCode: 'N1', nodeName: '����ȷ��', collaborators: ['STRUCTURE'] },
      { nodeCode: 'N2', nodeName: 'Эͬ���', collaborators: ['LAYOUT', 'HYDRAULIC'] },
      { nodeCode: 'N3', nodeName: '�鵵�ύ', collaborators: [] }
    ])
  }
]

const selectedTaskType = computed(() => taskTypes.value.find(item => item.taskType === form.taskType) || null)

function initMockData() {
  taskTypes.value = defaultTaskTypes
  flowTemplates.value = defaultFlowTemplates
}

function loadTaskTypes() {
  listTaskTypes({}).then(res => {
    if (res && res.rows && res.rows.length) {
      taskTypes.value = res.rows.map(item => ({
        taskType: item.taskType || item.typeCode || item.roleCode || item.value,
        typeName: item.typeName || item.taskTypeName || item.label || item.name,
        typeDesc: item.typeDesc || item.remark || '��������',
        color: item.color || '#409EFF',
        icon: item.icon || 'T',
        collaboratorRoles: item.collaboratorRoles || ''
      }))
    } else {
      initMockData()
    }
  }).catch(() => {
    initMockData()
  })
}

function loadFlowTemplates() {
  listFlowTemplate({}).then(res => {
    if (res && res.rows && res.rows.length) {
      flowTemplates.value = res.rows.map(item => ({
        templateId: item.templateId,
        templateName: item.templateName,
        templateDesc: item.templateDesc || '��������',
        templateConfig: item.templateConfig || '[]'
      }))
    } else if (!flowTemplates.value.length) {
      flowTemplates.value = defaultFlowTemplates
    }
  }).catch(() => {
    if (!flowTemplates.value.length) {
      flowTemplates.value = defaultFlowTemplates
    }
  })
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

function resetTemplatePreview() {
  form.templateId = ''
  selectedTemplate.value = null
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
    STRUCTURE: '�ṹ��ƹ���ʦ',
    LAYOUT: '������ƹ���ʦ',
    MANUFACTURE: '������ƹ���ʦ'
  }[taskType] || '������'
}

function getCollaboratorNames(taskType) {
  const type = taskTypes.value.find(item => item.taskType === taskType)
  if (!type || !type.collaboratorRoles) return '��'
  const map = {
    STRUCTURE: '�ṹ��ƹ���ʦ',
    LAYOUT: '������ƹ���ʦ',
    MANUFACTURE: '������ƹ���ʦ',
    HYDRAULIC: 'Һѹ��ƹ���ʦ'
  }
  return type.collaboratorRoles.split(',').map(role => map[role] || role).join('��')
}

function getPriorityLabel(priority) {
  return { 1: '��', 2: '��', 3: '��' }[priority] || '��'
}

function getPriorityTag(priority) {
  return { 1: 'info', 2: 'warning', 3: 'danger' }[priority] || 'info'
}

function getTemplateNodeCount(row) {
  try {
    return JSON.parse(row.templateConfig || '[]').length
  } catch (e) {
    return 0
  }
}

function handleFileChange(file, list) {
  fileList.value = list
}

function handleFileRemove(file, list) {
  fileList.value = list
}

function handleSaveDraft() {
  ElMessage.success('�ݸ��ѱ���')
}

function handleReset() {
  form.taskType = ''
  form.templateId = ''
  form.taskName = ''
  form.priority = 2
  form.description = ''
  form.collaboratorRoles = []
  fileList.value = []
  selectedTemplate.value = null
  formRef.value?.clearValidate?.()
}

function handleSubmit() {
  formRef.value?.validate?.(valid => {
    if (!valid) return
    submitLoading.value = true

    const payload = {
      ...form,
      attachmentCount: fileList.value.length
    }

    addTask(payload)
      .then(() => {
        ElMessage.success('�ύ�ɹ�')
        handleReset()
      })
      .finally(() => {
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

.single-form {
  width: 100%;
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

.type-card-head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.type-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex: none;
}

.type-name {
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

.type-meta {
  margin-top: 12px;
}

.preview-card {
  margin: 8px 0 20px;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.muted-text {
  color: #909399;
  font-size: 13px;
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

.flow-content {
  flex: 1;
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

.summary-descriptions {
  margin-top: 6px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.ml-12 {
  margin-left: 12px;
}

.mt-16 {
  margin-top: 16px;
}
</style>

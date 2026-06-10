<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">OBJECTIVE / CONSTRAINT</p>
          <h1 class="platform-title">{{ hasTask ? taskTitle : '目标与约束选择' }}</h1>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot"></span>
            <span>{{ hasTask ? currentLabel : activeTab === 'pending' ? '待处理' : '已处理' }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>{{ hasTask ? accessLabel : '选择后进入' }}</span>
          </div>
        </div>
      </section>

      <section v-if="!hasTask" class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">TASK INBOX</p>
            <h2 class="section-title">我的目标约束任务</h2>
          </div>
          <el-button plain :loading="inboxLoading" @click="loadInbox">刷新</el-button>
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
            <el-table-column label="说明" min-width="180">
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

        <section v-if="attachments.length || taskDescription" class="section-block fault-attachment-card">
          <div class="section-header">
            <div>
              <p class="section-label">PROBLEM OVERVIEW</p>
              <h2 class="section-title">当前问题概览</h2>
            </div>
            <el-button v-if="attachments.length" plain @click="openAttachmentViewer(previewAttachment || attachments[0])">查看示意图</el-button>
          </div>

          <div class="fault-overview-body">
            <div v-if="taskDescription" class="fault-task-description">
              <b>当前问题详情</b>
              <p>{{ taskDescription }}</p>
            </div>
            <div v-if="attachments.length" class="fault-attachment-entry" @click="openAttachmentViewer(previewAttachment || attachments[0])">
              <div class="fault-attachment-icon">PNG</div>
              <div class="fault-attachment-meta">
                <strong>{{ previewAttachment?.fileName || attachments[0]?.fileName || '问题示意图' }}</strong>
                <span>点击查看任务负责人提交的故障示意图</span>
              </div>
              <el-button type="primary" plain>查看示意图</el-button>
            </div>
          </div>
        </section>

        <div class="content-grid">
          <section class="section-block">
            <div class="section-header">
              <div>
                <p class="section-label">CATALOG</p>
                <h2 class="section-title">{{ currentLabel }}{{ readonlyMode ? '已选择项' : '候选项' }}</h2>
              </div>
              <el-select
                v-model="discipline"
                style="width: 190px"
                :disabled="readonlyMode"
                @change="loadCatalog"
              >
                <el-option v-for="item in disciplines" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </div>

            <div class="table-shell">
              <h3 class="block-title">优化目标</h3>
              <el-table :data="objectives" stripe class="platform-table">
                <el-table-column v-if="!readonlyMode" width="60">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.checked" />
                  </template>
                </el-table-column>
                <el-table-column label="名称" prop="itemName" min-width="220" show-overflow-tooltip />
                <el-table-column label="方向" prop="direction" width="120" />
                <el-table-column label="权重" width="210">
                  <template #default="{ row }">
                    <el-slider v-model="row.weight" :min="0" :max="100" :disabled="readonlyMode" />
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="table-shell mt-16">
              <h3 class="block-title">约束条件</h3>
              <el-table :data="constraints" stripe class="platform-table">
                <el-table-column v-if="!readonlyMode" width="60">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.checked" />
                  </template>
                </el-table-column>
                <el-table-column label="名称" prop="itemName" min-width="220" show-overflow-tooltip />
                <el-table-column label="关系" prop="direction" width="120" />
                <el-table-column label="阈值 / 输入" width="190">
                  <template #default="{ row }">
                    <el-input v-model="row.limitValue" :placeholder="row.unit" :disabled="readonlyMode" />
                  </template>
                </el-table-column>
                <el-table-column label="单位" prop="unit" width="100" />
              </el-table>
            </div>
          </section>

          <aside class="side-stack">
            <section class="section-block">
              <div class="section-header">
                <div>
                  <p class="section-label">SUBMIT</p>
                  <h2 class="section-title">{{ readonlyMode ? '查看信息' : '提交信息' }}</h2>
                </div>
              </div>
              <el-form label-width="90px" class="mt-12">
                <el-form-item label="任务 ID">
                  <el-input-number v-model="taskId" :min="1" disabled />
                </el-form-item>
                <el-form-item label="说明">
                  <el-input v-model="remark" type="textarea" :rows="5" :disabled="readonlyMode" />
                </el-form-item>
                <el-button v-if="!readonlyMode" type="primary" :loading="saving" @click="submit">
                  提交当前学科
                </el-button>
                <el-button v-else plain @click="backToInbox">返回任务列表</el-button>
              </el-form>
            </section>

            <section class="section-block">
              <div class="section-header">
                <div>
                  <p class="section-label">FLOW ORDER</p>
                  <h2 class="section-title">流转顺序</h2>
                </div>
              </div>
              <el-timeline class="mt-12">
                <el-timeline-item v-for="item in disciplines" :key="item.value" :type="item.value === discipline ? 'primary' : 'info'">
                  {{ item.label }}
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

const route = useRoute()
const router = useRouter()
const taskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const taskTitle = ref('目标与约束选择')
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

const disciplines = [
  { value: 'structure', label: '结构工程师' },
  { value: 'layout', label: '布局工程师' },
  { value: 'aero', label: '气动工程师' },
  { value: 'hydraulic', label: '液压工程师' },
  { value: 'manufacturing', label: '制造工程师' }
]

const hasTask = computed(() => !!taskId.value)
const readonlyMode = computed(() => access.value.mode !== 'enter' || route.query.mode === 'view')
const currentLabel = computed(() => disciplines.find(item => item.value === discipline.value)?.label || '')
const accessLabel = computed(() => access.value.label || (access.value.mode === 'enter' ? '可处理' : '查看'))
const visibleInboxTasks = computed(() => activeTab.value === 'pending' ? pendingTasks.value : handledTasks.value)
const objectives = computed(() => catalog.value.filter(item => item.itemType === 'objective'))
const constraints = computed(() => catalog.value.filter(item => item.itemType === 'constraint'))
const previewAttachment = computed(() => attachments.value.find(file => isImageAttachment(file) && attachmentPreviewUrl(file)))

const nodeDisciplineMap = {
  structure_select: 'structure',
  layout_select: 'layout',
  aero_select: 'aero',
  hydraulic_select: 'hydraulic',
  manufacturing_select: 'manufacturing'
}

const taskUserDisciplineMap = [
  ['structureUserId', 'structure'],
  ['layoutUserId', 'layout'],
  ['aeroUserId', 'aero'],
  ['hydraulicUserId', 'hydraulic'],
  ['manufacturingUserId', 'manufacturing']
]

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

function loadTaskContext() {
  if (!taskId.value) {
    loadInbox()
    return
  }
  getDesignTask(taskId.value).then(res => {
    const data = res.data || {}
    const nodeKey = data.nodeKey || data.task?.currentNodeKey
    const currentDiscipline = route.query.discipline || nodeDisciplineMap[nodeKey]
    taskTitle.value = data.task?.taskName || '目标与约束选择'
    access.value = data.access || { mode: 'wait', label: '等待' }
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
  const items = group?.items || []
  catalog.value = items.map(item => ({
    ...item,
    checked: true,
    limitValue: item.limitValue || ''
  }))
  remark.value = items[0]?.remark || '已完成本学科目标与约束选择。'
}

function loadCatalog() {
  getObjectiveCatalog(discipline.value).then(res => {
    catalog.value = (res.data || []).map(item => ({
      ...item,
      checked: true,
      limitValue: item.limitValue || ''
    }))
  })
}

function submit() {
  if (access.value.mode !== 'enter') {
    ElMessage.warning('当前节点未流转到你，暂不能提交。')
    return
  }
  saving.value = true
  saveObjectiveConstraints(taskId.value, {
    discipline: discipline.value,
    items: catalog.value.filter(item => item.checked),
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
  margin: 0 0 10px;
  color: #1f2a44;
  font-size: 15px;
  font-weight: 700;
}

.mb-16 {
  margin-bottom: 16px;
}

.wait-action {
  color: #98a2b3;
  font-size: 13px;
}

.fault-attachment-card {
  margin-bottom: 24px;
}

.fault-overview-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.fault-task-description {
  padding: 16px 18px;
  border: 1px solid rgba(128, 158, 195, 0.22);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);

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
  gap: 14px;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  border: 1px solid rgba(128, 158, 195, 0.24);
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(248, 251, 255, 0.96), rgba(239, 246, 255, 0.88));
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    border-color: rgba(74, 126, 210, 0.38);
    box-shadow: 0 10px 24px rgba(63, 94, 140, 0.12);
    transform: translateY(-1px);
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
  border-radius: 8px;
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
  border-radius: 12px;
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
  .fault-attachment-entry {
    grid-template-columns: 1fr;
  }
}
</style>

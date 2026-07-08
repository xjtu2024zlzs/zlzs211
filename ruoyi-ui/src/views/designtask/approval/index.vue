<template>
  <div class="design-platform-view approval-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <h1 class="platform-title">优化方案审批</h1>
          <p class="platform-subtitle">选择已生成正式报告的设计优化任务，查看模型解耦阶段形成的最终设计方案并完成审批。</p>
        </div>
        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>待审批 {{ pendingReports.length }}</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>已处理 {{ handledReports.length }}</span>
          </div>
          <el-button plain icon="Refresh" :loading="loading" @click="loadReports">刷新</el-button>
        </div>
      </section>

      <template v-if="!selectedTaskId">
        <section class="section-block">
          <div class="section-header">
            <div>
              <h2 class="section-title">正式报告任务</h2>
              <p class="section-hint">仅展示设计工程师已经提交正式设计方案报告的任务。</p>
            </div>
            <el-radio-group v-model="activeTab" @change="handleTabChange">
              <el-radio-button label="pending">待审批</el-radio-button>
              <el-radio-button label="handled">已审批</el-radio-button>
              <el-radio-button label="all">全部相关报告</el-radio-button>
            </el-radio-group>
          </div>

          <div class="metric-grid mt-12">
            <div class="metric-card">
              <div class="metric-card__label">待审批报告</div>
              <div class="metric-card__value">{{ pendingReports.length }}</div>
              <div class="metric-card__footer">当前流转到优化方案审批节点</div>
            </div>
            <div class="metric-card">
              <div class="metric-card__label">已处理报告</div>
              <div class="metric-card__value">{{ handledReports.length }}</div>
              <div class="metric-card__footer">已审批或已完成归档</div>
            </div>
            <div class="metric-card">
              <div class="metric-card__label">全部正式报告</div>
              <div class="metric-card__value">{{ reports.length }}</div>
              <div class="metric-card__footer">与当前账号相关</div>
            </div>
            <div class="metric-card">
              <div class="metric-card__label">当前筛选</div>
              <div class="metric-card__value metric-card__value--text">{{ activeTabLabel }}</div>
              <div class="metric-card__footer">点击表格行查看报告</div>
            </div>
          </div>

          <div class="table-shell">
            <el-table
              v-loading="loading"
              :data="visibleReports"
              stripe
              highlight-current-row
              class="platform-table"
              @row-click="selectReport"
            >
              <el-table-column label="报告编号" prop="reportCode" width="190" show-overflow-tooltip />
              <el-table-column label="任务名称" prop="taskName" min-width="260" show-overflow-tooltip />
              <el-table-column label="提交人" prop="submitBy" width="130" show-overflow-tooltip />
              <el-table-column label="提交时间" width="180">
                <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
              </el-table-column>
              <el-table-column label="当前节点" prop="currentNodeName" min-width="160" show-overflow-tooltip />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="reportTagType(row)">{{ reportStatusLabel(row) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="110" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" icon="View" @click.stop="selectReport(row)">查看报告</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-if="!loading && !visibleReports.length" class="report-empty" description="暂无对应状态的正式报告" :image-size="100" />
        </section>
      </template>

      <template v-else>
        <section class="section-block report-detail-head">
          <div class="section-header">
            <div>
              <h2 class="section-title">{{ selectedReportTitle }}</h2>
              <p class="section-hint">{{ selectedReportSubtitle }}</p>
            </div>
            <div class="action-row">
              <el-button plain icon="Back" @click="backToList">返回报告列表</el-button>
              <el-button plain icon="Download" :disabled="!selectedReport.reportFileId" @click="downloadReport">下载报告</el-button>
            </div>
          </div>
          <div class="report-meta-strip">
            <div v-for="item in approvalMetaItems" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </section>

        <section class="section-block report-reader-section" v-loading="detailLoading">
          <div v-if="selectedReport.submitted && selectedReport.reportHtml" class="report-paper" v-html="selectedReport.reportHtml"></div>
          <div v-else-if="selectedReport.submitted" class="report-paper report-paper--empty">
            <el-result icon="info" title="报告正文未保存" sub-title="当前任务已有正式报告记录，但未读取到报告正文，可下载正式报告文件查看。" />
          </div>
          <div v-else class="report-paper report-paper--empty">
            <el-result icon="warning" title="当前任务尚未提交正式报告" sub-title="设计工程师提交最终设计方案报告后，优化方案审批页面才会展示报告正文。" />
          </div>

          <div class="approval-footer">
            <div>
              <h3>审批结论</h3>
              <p>{{ approvalHint }}</p>
            </div>
            <el-form :model="approvalForm" class="approval-form">
              <el-form-item label="结论">
                <el-radio-group v-model="approvalForm.approved" :disabled="!canApproveSelected">
                  <el-radio :value="true">审批通过</el-radio>
                  <el-radio :value="false">退回模型解耦求解</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="意见">
                <el-input
                  v-model="approvalForm.comment"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  :disabled="!canApproveSelected"
                  placeholder="请输入审批意见"
                />
              </el-form-item>
              <div class="approval-actions">
                <el-button plain icon="Close" :disabled="!canApproveSelected || submitting" @click="approvalForm.approved = false">标记退回</el-button>
                <el-button plain icon="Check" :disabled="!canApproveSelected || submitting" @click="approvalForm.approved = true">标记通过</el-button>
                <el-button
                  type="primary"
                  icon="Select"
                  :disabled="!canApproveSelected"
                  :loading="submitting"
                  v-hasPermi="['designtask:task:approve']"
                  @click="submitApproval"
                >
                  提交审批
                </el-button>
              </div>
            </el-form>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  approveTask,
  getDashboard,
  getDesignReportTask,
  getDesignTask,
  getTaskAttachmentFile
} from '@/api/designtask/optimization'
import { updateTask as updateQualityTask } from '@/api/quality/task'
import { addLog as addQualityLog } from '@/api/quality/log'

const route = useRoute()
const router = useRouter()

const MODULE_CODE = 'PROJECT_2'
const MODULE_NAME = '设计制造协同优化平台'
const QMS_FLOW_EVENT_NAME = 'qms-flow-change'
const QMS_FLOW_EVENT_KEY = 'qms_flow_change'

const loading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const reports = ref([])
const selectedTaskId = ref(route.query.taskId ? Number(route.query.taskId) : null)
const selectedTask = ref({})
const selectedDetail = ref({})
const selectedReport = ref({ submitted: false })
const activeTab = ref(['pending', 'handled', 'all'].includes(route.query.tab) ? route.query.tab : 'pending')
const approvalForm = ref({
  approved: true,
  comment: '设计方案报告完整，模型解耦形成的最终设计方案满足当前任务要求，同意通过。'
})

const pendingReports = computed(() => reports.value.filter(item => item.currentNodeKey === 'leader_approve'))
const handledReports = computed(() => reports.value.filter(item => item.currentNodeKey !== 'leader_approve'))
const visibleReports = computed(() => {
  if (activeTab.value === 'pending') return pendingReports.value
  if (activeTab.value === 'handled') return handledReports.value
  return reports.value
})
const activeTabLabel = computed(() => ({ pending: '待审批', handled: '已审批', all: '全部' }[activeTab.value] || '待审批'))
const selectedStatusLabel = computed(() => selectedTaskId.value ? reportStatusLabel(selectedTask.value) : '未选择')
const selectedReportTitle = computed(() => selectedReport.value.reportTitle || selectedTask.value.taskName || '设计方案报告')
const selectedReportSubtitle = computed(() => {
  if (!selectedTaskId.value) return '请选择一份正式报告'
  return `${selectedReport.value.reportCode || '-'} / ${formatTime(selectedReport.value.submitTime)}`
})
const approvalHint = computed(() => {
  if (!selectedTaskId.value) return '请选择待审批报告。'
  if (!selectedReport.value.submitted) return '当前任务尚未提交正式报告。'
  if (canApproveSelected.value) return '请阅读报告后，在下方选择是否审批通过。'
  return '当前报告已处理或当前账号没有审批权限。'
})
const canApproveSelected = computed(() => {
  return selectedReport.value.submitted &&
    selectedTask.value.currentNodeKey === 'leader_approve' &&
    selectedDetail.value.access?.mode === 'enter'
})
const approvalMetaItems = computed(() => [
  { label: '报告编号', value: selectedReport.value.reportCode || '-' },
  { label: '任务名称', value: selectedTask.value.taskName || '-' },
  { label: '提交人', value: selectedReport.value.submitBy || '-' },
  { label: '提交时间', value: formatTime(selectedReport.value.submitTime) },
  { label: '当前节点', value: selectedTask.value.currentNodeName || selectedTask.value.currentNodeKey || '-' },
  { label: '审批状态', value: selectedStatusLabel.value }
])

function loadReports() {
  loading.value = true
  return getDashboard({ scope: 'related' }).then(async res => {
    const rows = res.data?.tasks || []
    const loaded = await Promise.all(rows.map(loadReportListItem))
    reports.value = loaded
      .filter(Boolean)
      .sort((a, b) => `${b.submitTime || ''}`.localeCompare(`${a.submitTime || ''}`))
    await ensureRouteTaskInList()
    selectDefaultReport()
  }).finally(() => {
    loading.value = false
  })
}

function loadReportListItem(task) {
  return getDesignReportTask(task.taskId).then(res => {
    const report = res.data || {}
    if (!report.submitted) return null
    return {
      ...task,
      ...report,
      taskName: task.taskName,
      currentNodeKey: task.currentNodeKey,
      currentNodeName: task.currentNodeName,
      status: task.status,
      action: task.action
    }
  }).catch(() => null)
}

async function ensureRouteTaskInList() {
  const routeTaskId = route.query.taskId ? Number(route.query.taskId) : null
  if (!routeTaskId || reports.value.some(item => Number(item.taskId) === routeTaskId)) return
  const detail = await getDesignTask(routeTaskId).then(res => res.data || {}).catch(() => null)
  const report = await getDesignReportTask(routeTaskId).then(res => res.data || {}).catch(() => null)
  if (detail?.task && report?.submitted) {
    reports.value.unshift({
      ...detail.task,
      ...report,
      taskId: detail.task.taskId,
      taskName: detail.task.taskName,
      currentNodeKey: detail.task.currentNodeKey,
      currentNodeName: detail.task.currentNodeName,
      status: detail.task.status,
      action: detail.access
    })
  }
}

function selectDefaultReport() {
  const routeTaskId = route.query.taskId ? Number(route.query.taskId) : null
  if (routeTaskId) {
    const preferred = reports.value.find(item => Number(item.taskId) === routeTaskId)
    if (preferred) {
      selectReport(preferred, false)
      return
    }
    loadStandaloneRouteTask(routeTaskId)
  }
}

function loadStandaloneRouteTask(routeTaskId) {
  detailLoading.value = true
  Promise.all([
    getDesignTask(routeTaskId).then(res => res.data || {}).catch(() => ({})),
    getDesignReportTask(routeTaskId).then(res => res.data || {}).catch(() => ({ submitted: false }))
  ]).then(([detail, report]) => {
    selectedTaskId.value = routeTaskId
    selectedDetail.value = detail
    selectedTask.value = detail.task || { taskId: routeTaskId }
    selectedReport.value = report || { submitted: false }
  }).finally(() => {
    detailLoading.value = false
  })
}

function selectReport(item, pushRoute = true) {
  selectedTaskId.value = Number(item.taskId)
  selectedTask.value = item
  detailLoading.value = true
  Promise.all([
    getDesignTask(item.taskId).then(res => res.data || {}).catch(() => ({})),
    getDesignReportTask(item.taskId).then(res => res.data || {}).catch(() => ({ submitted: false }))
  ]).then(([detail, report]) => {
    selectedDetail.value = detail
    selectedTask.value = { ...item, ...(detail.task || {}) }
    selectedReport.value = report || { submitted: false }
    if (pushRoute) {
      router.replace({ path: '/designtask/approval', query: { taskId: item.taskId, tab: activeTab.value } })
    }
  }).finally(() => {
    detailLoading.value = false
  })
}

function backToList() {
  selectedTaskId.value = null
  selectedTask.value = {}
  selectedDetail.value = {}
  selectedReport.value = { submitted: false }
  router.replace({ path: '/designtask/approval', query: { tab: activeTab.value } })
}

function handleTabChange() {
  router.replace({ path: '/designtask/approval', query: { tab: activeTab.value } })
}

function downloadReport() {
  if (!selectedReport.value.reportFileId) {
    ElMessage.warning('当前报告没有可下载的正式文件。')
    return
  }
  getTaskAttachmentFile(selectedReport.value.reportFileId).then(data => {
    const filename = selectedReport.value.reportFileName || `${selectedReport.value.reportCode || 'design-report'}.doc`
    saveAs(new Blob([data]), filename)
  })
}

function formatDateTime(value = new Date()) {
  const date = value instanceof Date ? value : new Date(value)
  const pad = (number) => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function notifyQmsFlowChanged(payload = {}) {
  const eventData = {
    moduleCode: MODULE_CODE,
    problemId: payload.problemId || '',
    problemCode: payload.problemCode || '',
    taskId: payload.taskId || '',
    action: payload.action || 'APPROVE',
    time: Date.now()
  }

  window.dispatchEvent(new CustomEvent(QMS_FLOW_EVENT_NAME, { detail: eventData }))
  localStorage.setItem(QMS_FLOW_EVENT_KEY, JSON.stringify(eventData))
}

function buildQualityApprovalResult() {
  const report = selectedReport.value || {}
  const task = selectedTask.value || {}
  return JSON.stringify({
    moduleCode: MODULE_CODE,
    moduleName: MODULE_NAME,
    resultType: 'DESIGN_APPROVAL_RESULT',
    conclusion: '设计制造协同优化平台已完成优化方案审批，最终设计方案报告可回填至质量问题管理中心。',
    designTaskId: task.taskId || selectedTaskId.value,
    designTaskName: task.taskName || '',
    designReport: {
      submitted: Boolean(report.submitted || report.reportFileId),
      taskId: report.taskId || task.taskId || selectedTaskId.value,
      reportId: report.reportId || '',
      reportCode: report.reportCode || '',
      reportTitle: report.reportTitle || task.taskName || '设计制造协同优化方案报告',
      reportFileId: report.reportFileId || '',
      reportFilePath: report.reportFilePath || '',
      reportFileName: report.reportFileName || '设计制造协同优化方案报告.doc',
      submitTime: report.submitTime || ''
    },
    approval: {
      approved: approvalForm.value.approved,
      comment: approvalForm.value.comment || '',
      approveTime: formatDateTime()
    },
    generateTime: formatDateTime()
  }, null, 2)
}

async function syncQualityTaskAfterApproval() {
  if (!approvalForm.value.approved) return

  const link = selectedDetail.value?.qualityTaskLink || {}
  const qualityTaskId = link.qualityTaskId

  if (!qualityTaskId) return

  try {
    await updateQualityTask({
      taskId: qualityTaskId,
      problemId: link.qualityProblemId,
      problemCode: link.qualityProblemCode,
      moduleCode: MODULE_CODE,
      moduleName: MODULE_NAME,
      taskStatus: 'PENDING_BACKFILL',
      processResult: buildQualityApprovalResult(),
      processFile: selectedReport.value?.reportFilePath || ''
    })

    await addQualityLog({
      problemId: link.qualityProblemId,
      problemCode: link.qualityProblemCode,
      taskId: qualityTaskId,
      actionType: 'REPORT_APPROVED',
      actionName: '设计报告审批通过',
      operatorName: MODULE_NAME,
      fromStatus: 'PROCESSING',
      toStatus: 'PENDING_BACKFILL',
      actionContent: `设计制造协同优化平台优化方案审批通过，最终设计方案报告已进入待回填状态：${selectedReport.value?.reportTitle || '设计制造协同优化方案报告'}。`,
      createTime: formatDateTime()
    })

    notifyQmsFlowChanged({
      problemId: link.qualityProblemId,
      problemCode: link.qualityProblemCode,
      taskId: qualityTaskId,
      action: 'REPORT_APPROVED'
    })
  } catch (error) {
    console.warn('同步质量任务待回填状态失败：', error)
    ElMessage.warning('审批已完成，但同步质量任务待回填状态失败，请回到任务看板刷新后重试')
  }
}

function submitApproval() {
  if (!canApproveSelected.value) {
    ElMessage.warning('当前报告不可审批。')
    return
  }
  submitting.value = true
  approveTask(selectedTaskId.value, approvalForm.value).then(async () => {
    await syncQualityTaskAfterApproval()
    ElMessage.success(approvalForm.value.approved ? '审批通过，任务已完成。' : '已退回模型解耦求解。')
    activeTab.value = 'handled'
    backToList()
    return loadReports()
  }).finally(() => {
    submitting.value = false
  })
}

function reportStatusLabel(item) {
  if (!item || !item.taskId) return '未选择'
  if (item.currentNodeKey === 'leader_approve') return '待审批'
  if (item.currentNodeKey === 'end' || item.status === 'COMPLETED') return '已通过'
  return '已处理'
}

function reportTagType(item) {
  if (!item || !item.taskId) return 'info'
  if (item.currentNodeKey === 'leader_approve') return 'warning'
  if (item.currentNodeKey === 'end' || item.status === 'COMPLETED') return 'success'
  return 'info'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

watch(() => route.query.taskId, value => {
  const nextId = value ? Number(value) : null
  if (nextId && nextId !== selectedTaskId.value) {
    const existing = reports.value.find(item => Number(item.taskId) === nextId)
    if (existing) {
      selectReport(existing, false)
    } else {
      loadReports()
    }
  }
})

onMounted(loadReports)
</script>

<style scoped lang="scss">
@use "../platform-theme.scss";

.section-hint {
  margin: 4px 0 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.6;
}

.metric-card__value--text {
  font-size: 22px;
}

.report-empty {
  padding: 28px 0 8px;
}

.report-detail-head {
  .section-header {
    align-items: flex-start;
  }
}

.report-meta-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;

  div {
    min-width: 0;
    padding: 10px 12px;
    border: 1px solid #e6ebf1;
    border-radius: 6px;
    background: #fbfcfe;
  }

  span {
    display: block;
    color: #708198;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 4px;
    overflow: hidden;
    color: #24324f;
    font-size: 13px;
    line-height: 1.5;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.report-reader-section {
  padding: 18px;
}

.report-paper {
  max-width: 1180px;
  min-height: 58vh;
  margin: 0 auto;
  padding: 30px 34px;
  overflow: auto;
  border: 1px solid #dce5ef;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(31, 42, 68, 0.06);
}

.report-paper--empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.approval-footer {
  max-width: 1180px;
  margin: 16px auto 0;
  padding: 16px;
  border: 1px solid #dfe6ef;
  border-radius: 6px;
  background: #fbfcfe;

  h3 {
    margin: 0;
    color: #233955;
    font-size: 18px;
  }

  p {
    margin: 6px 0 12px;
    color: #667085;
    font-size: 13px;
  }
}

.approval-form {
  :deep(.el-form-item__label) {
    color: #46617d;
    font-weight: 700;
  }
}

.approval-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1100px) {
  .report-meta-strip,
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .platform-topbar,
  .section-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .report-meta-strip,
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>

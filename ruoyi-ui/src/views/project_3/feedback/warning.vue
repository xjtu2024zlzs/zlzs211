<template>
  <div class="app-container quality-page warning-page">
    <el-card shadow="never" class="quality-hero feedback-hero">
      <template #header>
        <span class="quality-title">全域制造过程反馈监管</span>
      </template>
      <div class="quality-desc">查看零件、工序和设备异常状态，并发起异常检测算法分析。</div>
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <span class="quality-section-title">工序序列关键异常信号预警</span>
      </template>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">零件模板列表</span>
            </template>
            <el-form :inline="true" class="quality-mb16">
              <el-form-item>
                <el-input v-model="partQuery.keyword" placeholder="零件编号 / 名称" clearable />
              </el-form-item>
              <el-form-item>
                <el-select v-model="partQuery.sort_by" placeholder="排序方式" style="width: 160px" @change="queryPart">
                  <el-option label="按编号排序" value="part_code" />
                  <el-option label="按零件名称排序" value="part_name" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="partQuery.sort_order" placeholder="排序方向" style="width: 120px" @change="queryPart">
                  <el-option label="升序" value="asc" />
                  <el-option label="降序" value="desc" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryPart">查询</el-button>
                <el-button @click="resetPart">重置</el-button>
              </el-form-item>
            </el-form>

            <el-table
              v-loading="partLoading"
              :data="partList"
              border
              highlight-current-row
              @row-click="pickPartTemplate"
            >
              <el-table-column prop="part_code" label="编号" min-width="120" />
              <el-table-column prop="part_name" label="零件名称" min-width="130" show-overflow-tooltip />
              <el-table-column prop="material" label="材料" min-width="100" show-overflow-tooltip />
              <el-table-column prop="spec_model" label="规格型号" min-width="110" show-overflow-tooltip />
              <el-table-column prop="instance_count" label="实例数" width="90" />
              <el-table-column prop="risk_level" label="风险等级" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="riskTag(row.risk_level)">{{ row.risk_level || '无' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <pagination
              v-show="partTotal > 0"
              v-model:page="partQuery.page_num"
              v-model:limit="partQuery.page_size"
              :total="partTotal"
              @pagination="loadParts"
            />

            <div class="instance-panel">
              <div class="instance-header">
                <span class="inner-title sub-title">零件实例子列表</span>
                <span class="instance-meta">{{ selectedTemplate?.part_code || '未选择模板' }}</span>
              </div>
              <el-table
                v-loading="partInstanceLoading"
                :data="partInstanceList"
                border
                highlight-current-row
                max-height="280"
                @row-click="pickPart"
              >
                <el-table-column prop="part_instance_id" label="实例ID" min-width="120" show-overflow-tooltip />
                <el-table-column prop="serial_number" label="序列号" min-width="110" show-overflow-tooltip />
                <el-table-column prop="batch_number" label="批次号" min-width="100" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="90" />
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="danger" @click.stop="keyProc(row)">关键工序识别</el-button>
                    <el-button link type="info" @click.stop="showPartDetail(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <pagination
                v-show="partInstanceTotal > 0"
                v-model:page="partInstanceQuery.page_num"
                v-model:limit="partInstanceQuery.page_size"
                :total="partInstanceTotal"
                @pagination="loadPartInstances(selectedTemplate)"
              />
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">对应制造工序</span>
            </template>
            <div class="selected-info">
              <div class="selected-path">来源：{{ selectedPart?.path_text || '暂无数据' }}</div>
              <div class="info-box quality-mt16">
                <div class="info-label">零件实例</div>
                <div class="info-value">{{ selectedPart?.part_code || '暂无数据' }}</div>
                <div class="info-desc">{{ selectedPart?.part_instance_id || selectedPart?.serial_number || '暂无数据' }}</div>
              </div>
            </div>

            <div v-loading="processLoading" class="process-list">
              <div v-for="item in processList" :key="item.process_exec_id" class="process-card">
                <div class="process-main">
                  <div class="process-title">{{ item.process_order }}. {{ item.process_name }}</div>
                  <div class="process-desc">{{ item.process_desc || '暂无异常描述' }}</div>
                  <div class="process-device">加工设备：{{ item.device_name || item.device_id || '暂无数据' }}</div>
                </div>
                <div class="process-actions">
                  <el-tag size="small" type="success">{{ item.status || '未知' }}</el-tag>
                  <el-tag size="small" :type="riskTag(item.risk_level)">{{ item.risk_level || '无' }}</el-tag>
                  <el-button plain round @click.stop="procDetect(item)">工序异常检测</el-button>
                </div>
              </div>
              <el-empty v-if="!processLoading && processList.length === 0" description="暂无工序数据" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <span class="quality-section-title">设备异常信号预警</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">设备列表</span>
            </template>
            <el-form :inline="true" class="quality-mb16">
              <el-form-item>
                <el-input v-model="deviceQuery.keyword" placeholder="设备编号 / 名称" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryDev">查询</el-button>
                <el-button @click="resetDev">重置</el-button>
              </el-form-item>
            </el-form>

            <el-table v-loading="deviceLoading" :data="deviceList" border highlight-current-row @row-click="pickDev">
              <el-table-column prop="device_code" label="编号" min-width="120" />
              <el-table-column prop="device_name" label="设备名称" min-width="160" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" min-width="120" />
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never" class="inner-card">
            <template #header>
              <span class="inner-title">设备加工零件列表</span>
            </template>
            <div class="info-box quality-mb16">
              <div class="info-label">设备编号</div>
              <div class="info-value">{{ selectedDevice?.device_code || '暂无数据' }}</div>
              <div class="info-desc">{{ selectedDevice?.device_name || '暂无数据' }}</div>
            </div>

            <el-table v-loading="devicePartLoading" :data="devicePartList" border max-height="320">
              <el-table-column prop="part_code" label="编号" min-width="120" />
              <el-table-column prop="part_name" label="零件名称" min-width="100" show-overflow-tooltip />
              <el-table-column label="加工工序" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ formatProcess(row) }}
                </template>
              </el-table-column>
            </el-table>
            <pagination
              v-show="devicePartTotal > 0"
              v-model:page="devicePartQuery.page_num"
              v-model:limit="devicePartQuery.page_size"
              :total="devicePartTotal"
              @pagination="loadDevPart(selectedDevice)"
            />
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="quality-card warning-section">
      <template #header>
        <div class="card-header-row">
          <span class="quality-section-title">历史异常检测结果</span>
          <el-button @click="loadDetectResults">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="resultLoading" :data="resultRows" border>
        <el-table-column prop="taskId" label="任务ID" width="190" show-overflow-tooltip />
        <el-table-column prop="targetName" label="检测对象" min-width="140" show-overflow-tooltip />
        <el-table-column label="数据文件" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link v-if="row.dataFileUrl" type="primary" :underline="false" @click.stop="downloadDataFile(row)">
              {{ row.dataFile || '--' }}
            </el-link>
            <span v-else>{{ row.dataFile || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="abnormalScore" label="异常分数" width="110" />
        <el-table-column prop="abnormalTime" label="异常时间" width="170" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
      <pagination
        v-show="resultTotal > 0"
        v-model:page="resultQuery.page_num"
        v-model:limit="resultQuery.page_size"
        :total="resultTotal"
        @pagination="loadDetectResults"
      />
    </el-card>

    <el-dialog v-model="detectDialog.visible" :title="detectDialogTitle" width="1000px" :close-on-click-modal="false" @close="closeDetectDialog">
      <div class="detect-summary">
        <span>检测对象：{{ detectTargetText }}</span>
        <span>已选择 {{ selectedSampleIds.length }} 个文件</span>
      </div>
      <div class="file-toolbar">
        <el-input
          v-model="fileQuery.keyword"
          placeholder="搜索文件名"
          clearable
          style="width: 240px"
          @keyup.enter="loadSamples"
          @clear="loadSamples"
        />
        <el-button @click="loadSamples">查询</el-button>
        <el-button @click="clearSamples">清空选择</el-button>
      </div>
      <el-table
        ref="sampleTableRef"
        v-loading="fileQuery.loading"
        :data="fileQuery.samples"
        row-key="id"
        height="360"
        border
        @selection-change="onSampleSelection"
      >
        <el-table-column type="selection" width="48" reserve-selection />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="conditionLabel" label="对象类型" width="130" />
        <el-table-column prop="bearingCode" label="对象编号" width="120" />
        <el-table-column prop="fileSize" label="文件大小" width="120" />
        <el-table-column prop="createTime" label="导入时间" width="170" />
      </el-table>
      <pagination
        v-show="fileQuery.total > 0"
        v-model:page="fileQuery.pageNum"
        v-model:limit="fileQuery.pageSize"
        :total="fileQuery.total"
        @pagination="loadSamples"
      />
      <div class="detect-result" v-if="status || error || Object.keys(result).length">
        <el-descriptions v-if="isKeyMode" :column="3" border>
          <el-descriptions-item label="任务ID">{{ taskId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ status || '--' }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ val(keyField('confidence')) }}</el-descriptions-item>
          <el-descriptions-item label="关键工序">{{ val(keyField('name')) }}</el-descriptions-item>
          <el-descriptions-item label="工序编码">{{ val(keyField('code')) }}</el-descriptions-item>
          <el-descriptions-item label="分数">{{ val(keyField('score')) }}</el-descriptions-item>
          <el-descriptions-item label="原因" :span="3">{{ val(keyField('reason')) }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ val(keyField('suggestion')) }}</el-descriptions-item>
          <el-descriptions-item v-if="error" label="失败原因" :span="3">{{ error }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="isKeyMode && keyProcessRows.length" :data="keyProcessRows" border height="180" class="quality-mt16">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="processCode" label="工序编码" />
          <el-table-column prop="processName" label="工序名称" />
          <el-table-column prop="score" label="分数" width="120" />
        </el-table>
        <el-descriptions v-if="!isKeyMode" :column="3" border>
          <el-descriptions-item label="任务ID">{{ taskId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ status || '--' }}</el-descriptions-item>
          <el-descriptions-item label="是否异常">{{ abnormalText }}</el-descriptions-item>
          <el-descriptions-item label="异常分数">{{ val(result.abnormalScore) }}</el-descriptions-item>
          <el-descriptions-item label="异常时间">{{ val(result.abnormalTime) }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="3">{{ val(result.suggestion) }}</el-descriptions-item>
          <el-descriptions-item v-if="error" label="失败原因" :span="3">{{ error }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="!isKeyMode && curveRows.length" :data="curveRows" border height="180" class="quality-mt16">
          <el-table-column prop="x" label="x" />
          <el-table-column prop="value" label="value" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="closeDetectDialog">关闭</el-button>
        <el-button type="primary" :loading="detectDialog.loading" :disabled="detectDialog.loading" @click="submitDetectDialog">
          {{ detectDialogSubmitText }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="partDetailDialog.visible" title="零件详细信息" width="920px" class="part-detail-dialog">
      <div class="part-detail-layout">
        <div class="part-image-panel">
          <div class="part-image-title">零件图片</div>
          <el-upload
            class="part-image-uploader"
            accept="image/*"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="onPartImageChange"
          >
            <div v-loading="partDetailUploading" class="part-image-box">
              <img v-if="partDetailImage" :src="partDetailImage" alt="零件图片" />
              <div v-else class="part-image-empty">
                <span class="part-image-plus">+</span>
                <span>点击插入图片</span>
                <small>支持 JPG / PNG / BMP</small>
              </div>
            </div>
          </el-upload>
          <el-button v-if="partDetailImage" size="small" plain type="danger" :loading="partDetailUploading" @click="removePartImage">
            移除图片
          </el-button>
        </div>
        <div class="part-info-panel">
          <el-table :data="partDetailRows" border>
            <el-table-column prop="label" label="字段" width="150" />
            <el-table-column prop="value" label="内容" min-width="360" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getWarningKeyProcessTask,
  deleteWarningPartImage,
  getWarningDetectTask,
  listWarningDetectResults,
  listWarningDeviceParts,
  listWarningDevices,
  listWarningPartInstances,
  listWarningPartProcesses,
  listWarningParts,
  uploadWarningPartImage,
  startWarningKeyProcessTask,
  startWarningDetectTask
} from '@/api/project_3/feedback'
import { listFaultIdenSamples } from '@/api/project_3/service'
import { getApiPayload, getErrorMessage, useQualityAlgorithmTask } from '@/utils/useQualityAlgorithmTask'
import '@/views/project_3/common.css'

defineOptions({ name: 'FeedbackWarning' })

const partLoading = ref(false)
const partInstanceLoading = ref(false)
const processLoading = ref(false)
const deviceLoading = ref(false)
const devicePartLoading = ref(false)
const resultLoading = ref(false)
const partDetailUploading = ref(false)
const partList = ref([])
const partInstanceList = ref([])
const processList = ref([])
const selectedTemplate = ref(null)
const selectedPart = ref(null)
const partTotal = ref(0)
const partInstanceTotal = ref(0)
const deviceList = ref([])
const devicePartList = ref([])
const selectedDevice = ref(null)
const devicePartTotal = ref(0)
const resultRows = ref([])
const resultTotal = ref(0)
const selectedSamples = ref([])
const sampleTableRef = ref(null)
const taskId = ref('')
const status = ref('')
const error = ref('')
const result = ref({})
const task = useQualityAlgorithmTask({
  taskId,
  status,
  error,
  result,
  query: (id, context) => context.mode === 'key' ? getWarningKeyProcessTask(id) : getWarningDetectTask(id),
  onDone: (taskStatus, data, context) => {
    if (taskStatus === 'SUCCESS' && context.mode === 'detect') loadDetectResults()
  },
  onError: msg => ElMessage.error(msg),
  onMissingTaskId: () => ElMessage.warning('任务已提交，但未返回 taskId')
})

const partQuery = reactive({ keyword: '', sort_by: 'part_code', sort_order: 'asc', page_num: 1, page_size: 6 })
const partInstanceQuery = reactive({ page_num: 1, page_size: 5 })
const deviceQuery = reactive({ keyword: '', risk_level: '', page_num: 1, page_size: 10 })
const devicePartQuery = reactive({ page_num: 1, page_size: 6 })
const resultQuery = reactive({ page_num: 1, page_size: 10 })
const fileQuery = reactive({ keyword: '', samples: [], pageNum: 1, pageSize: 20, total: 0, loading: false })
const detectDialog = reactive({ visible: false, loading: false, mode: 'detect', part: null, process: null })
const partDetailDialog = reactive({ visible: false, row: null })

const selectedSampleIds = computed(() => selectedSamples.value.map(item => item.id))
const isKeyMode = computed(() => detectDialog.mode === 'key')
const detectDialogTitle = computed(() => isKeyMode.value ? '关键工序识别' : '异常检测')
const detectDialogSubmitText = computed(() => isKeyMode.value ? '开始关键工序识别' : '开始异常检测')
const detectTargetText = computed(() => {
  const part = detectDialog.part
  const proc = detectDialog.process
  if (!part) return '--'
  return proc ? `${part.part_name || part.part_code} / ${proc.process_name || proc.process_id}` : (part.part_name || part.part_code || part.part_id)
})
const abnormalText = computed(() => {
  const value = result.value?.isAbnormal ?? result.value?.is_abnormal
  if (value === true || value === 'true') return '是'
  if (value === false || value === 'false') return '否'
  return '--'
})
const curveRows = computed(() => {
  const rows = result.value?.curveData || result.value?.curve_data || []
  return Array.isArray(rows) ? rows.map(item => ({ x: item.x, value: item.value ?? item.y ?? item.riskScore })) : []
})
const keyProcessRows = computed(() => {
  const rows = result.value?.candidateProcesses || result.value?.candidate_processes || []
  return Array.isArray(rows)
    ? rows.map(item => ({
      ...item,
      rank: item.rank ?? item.order,
      processCode: item.processCode ?? item.process_code ?? item.keyProcessCode ?? item.key_process_code,
      processName: item.processName ?? item.process_name ?? item.keyProcessName ?? item.key_process_name,
      score: item.score ?? item.confidence
    }))
    : []
})
const partDetailRows = computed(() => {
  const row = partDetailDialog.row || {}
  return [
    { label: '零件实例ID', value: val(row.part_id) },
    { label: '零件模板ID', value: val(row.part_template_id) },
    { label: '零件编号', value: val(row.part_code) },
    { label: '零件名称', value: val(row.part_name) },
    { label: '序列号', value: val(row.serial_number) },
    { label: '批次号', value: val(row.batch_number) },
    { label: '制造商', value: val(row.manufacturer) },
    { label: '生产日期', value: val(row.production_date) },
    { label: '材料', value: val(row.material) },
    { label: '规格型号', value: val(row.spec_model) },
    { label: '当前状态', value: val(row.status) },
    { label: '当前工序', value: val(row.current_process) },
    { label: '风险等级', value: val(row.risk_level) },
    { label: '结构路径', value: val(row.path_text) }
  ]
})
const partDetailImage = computed(() => {
  return imageSrc(partDetailDialog.row?.image_url || partDetailDialog.row?.imageUrl)
})

function riskTag(level) {
  if (level === '高' || level === 'HIGH') return 'danger'
  if (level === '中' || level === 'MEDIUM') return 'warning'
  if (level === '低' || level === 'LOW') return 'success'
  return 'info'
}

async function loadParts() {
  partLoading.value = true
  try {
    const res = await listWarningParts(partQuery)
    const payload = res?.data || {}
    const rows = Array.isArray(payload.rows) ? payload.rows : []
    partList.value = rows
    partTotal.value = Number(payload.total || 0)
    const currentTemplateId = selectedTemplate.value?.part_template_id
    const currentInPage = rows.some(item => item.part_template_id === currentTemplateId)
    if (rows.length && !currentInPage) await pickPartTemplate(rows[0])
    if (!rows.length) {
      selectedTemplate.value = null
      selectedPart.value = null
      partInstanceList.value = []
      partInstanceTotal.value = 0
      processList.value = []
    }
  } finally {
    partLoading.value = false
  }
}

async function pickPartTemplate(row) {
  selectedTemplate.value = row || null
  selectedPart.value = null
  processList.value = []
  partInstanceQuery.page_num = 1
  await loadPartInstances(row)
}

async function loadPartInstances(row) {
  const templateId = row?.part_template_id
  if (!templateId) {
    partInstanceList.value = []
    partInstanceTotal.value = 0
    selectedPart.value = null
    processList.value = []
    return
  }
  partInstanceLoading.value = true
  try {
    const res = await listWarningPartInstances(templateId, partInstanceQuery)
    const payload = res?.data || {}
    const rows = Array.isArray(payload.rows) ? payload.rows : []
    partInstanceList.value = rows
    partInstanceTotal.value = Number(payload.total || 0)
    if (rows.length) {
      await pickPart(rows[0])
    } else {
      selectedPart.value = null
      processList.value = []
    }
  } finally {
    partInstanceLoading.value = false
  }
}

async function pickPart(row) {
  selectedPart.value = row || null
  await loadPartProc(row)
}

async function loadPartProc(row) {
  if (!row?.part_id) {
    processList.value = []
    return
  }
  processLoading.value = true
  try {
    const res = await listWarningPartProcesses(row.part_id)
    processList.value = Array.isArray(res?.data) ? res.data : []
  } finally {
    processLoading.value = false
  }
}

function queryPart() {
  partQuery.page_num = 1
  selectedTemplate.value = null
  selectedPart.value = null
  partInstanceQuery.page_num = 1
  loadParts()
}

function resetPart() {
  partQuery.keyword = ''
  partQuery.sort_by = 'part_code'
  partQuery.sort_order = 'asc'
  partQuery.page_num = 1
  partInstanceQuery.page_num = 1
  selectedTemplate.value = null
  selectedPart.value = null
  loadParts()
}

function showPartDetail(row) {
  partDetailDialog.row = row || null
  partDetailDialog.visible = true
}

async function onPartImageChange(file) {
  const raw = file?.raw
  const id = partDetailDialog.row?.part_id
  if (!id || !raw) return
  if (!raw.type?.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  const data = new FormData()
  data.append('file', raw)
  partDetailUploading.value = true
  try {
    const res = await uploadWarningPartImage(id, data)
    const imageUrl = res?.image_url || res?.imageUrl || res?.data?.image_url || res?.data?.imageUrl
    updatePartImage(id, imageUrl)
    ElMessage.success('图片上传成功')
  } finally {
    partDetailUploading.value = false
  }
}

async function removePartImage() {
  const id = partDetailDialog.row?.part_id
  if (!id) return
  partDetailUploading.value = true
  try {
    await deleteWarningPartImage(id)
    updatePartImage(id, '')
    ElMessage.success('图片已移除')
  } finally {
    partDetailUploading.value = false
  }
}

function updatePartImage(partId, imageUrl) {
  if (partDetailDialog.row?.part_id === partId) {
    partDetailDialog.row.image_url = imageUrl
  }
  const row = partInstanceList.value.find(item => item.part_id === partId)
  if (row) row.image_url = imageUrl
  if (selectedPart.value?.part_id === partId) {
    selectedPart.value.image_url = imageUrl
  }
}

function imageSrc(url) {
  if (!url) return ''
  return /^https?:\/\//i.test(url) ? url : `${import.meta.env.VITE_APP_BASE_API}${url}`
}

function procDetect(row) {
  if (!selectedPart.value) {
    ElMessage.warning('请先选择零件')
    return
  }
  openDetect(selectedPart.value, row)
}

function keyProc(row) {
  openKeyProcess(row)
}

async function openKeyProcess(part) {
  if (!part?.part_id) {
    ElMessage.warning('识别对象不能为空')
    return
  }
  task.clearPoll()
  detectDialog.mode = 'key'
  detectDialog.part = part
  detectDialog.process = null
  detectDialog.visible = true
  taskId.value = ''
  status.value = ''
  error.value = ''
  result.value = {}
  clearSamples()
  fileQuery.pageNum = 1
  await loadSamples()
}

async function openDetect(part, process) {
  if (!part?.part_id) {
    ElMessage.warning('检测对象不能为空')
    return
  }
  if (!(process?.process_exec_id || process?.process_id)) {
    ElMessage.warning('请先选择工序')
    return
  }
  task.clearPoll()
  detectDialog.mode = 'detect'
  detectDialog.part = part
  detectDialog.process = process
  detectDialog.visible = true
  taskId.value = ''
  status.value = ''
  error.value = ''
  result.value = {}
  clearSamples()
  fileQuery.pageNum = 1
  await loadSamples()
}

async function loadSamples() {
  fileQuery.loading = true
  try {
    const res = await listFaultIdenSamples({
      dataUsage: 'PROCESS_ANOMALY',
      keyword: fileQuery.keyword,
      pageNum: fileQuery.pageNum,
      pageSize: fileQuery.pageSize
    })
    fileQuery.samples = Array.isArray(res?.rows) ? res.rows : []
    fileQuery.total = Number(res?.total || 0)
  } finally {
    fileQuery.loading = false
  }
}

function onSampleSelection(rows) {
  const map = new Map(selectedSamples.value.map(item => [item.id, item]))
  fileQuery.samples.forEach(item => map.delete(item.id))
  rows.forEach(item => map.set(item.id, item))
  selectedSamples.value = Array.from(map.values())
}

function clearSamples() {
  selectedSamples.value = []
  sampleTableRef.value?.clearSelection?.()
}

function submitDetectDialog() {
  if (isKeyMode.value) {
    startKeyProcess()
    return
  }
  startDetect()
}

function closeDetectDialog() {
  task.clearPoll()
  detectDialog.visible = false
}

async function startKeyProcess() {
  if (detectDialog.loading) return
  if (!detectDialog.part?.part_id) {
    ElMessage.warning('识别对象不能为空')
    return
  }
  if (!selectedSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV/TXT 文件')
    return
  }
  task.clearPoll()
  detectDialog.loading = true
  status.value = 'RUNNING'
  error.value = ''
  result.value = {}
  try {
    const part = detectDialog.part
    const partTplId = part.part_template_id || part.partTemplateId
    if (!partTplId) {
      throw new Error('识别对象缺少零件模板ID')
    }
    const targetNodeId = `part_template:${partTplId}`
    const selectedObject = {
      id: targetNodeId,
      level: 'part',
      objectLevel: 'part',
      name: part.part_name || part.part_code || part.part_id,
      path: part.path_text
    }
    const params = {
      dataSelectionMode: 'SELECTED_FILES',
      dataUsage: 'PROCESS_ANOMALY',
      sampleIds: selectedSampleIds.value,
      selected_object: selectedObject,
      targetNodeId,
      targetType: 'part',
      targetLevel: 'part',
      targetId: partTplId,
      aircraftId: part.aircraft_id,
      subsystemId: part.subsystem_id,
      equipmentId: part.equipment_id,
      componentId: part.component_id,
      partId: partTplId,
      partInstanceId: part.part_id,
      partCode: part.part_code,
      partName: part.part_name,
      pathText: part.path_text,
      topN: 3,
      threshold: 0.7
    }
    const res = await startWarningKeyProcessTask({
      import_record_id: targetNodeId,
      dataSelectionMode: 'SELECTED_FILES',
      sampleIds: selectedSampleIds.value,
      params
    })
    if (!detectDialog.visible) return
    task.acceptTask(res, { mode: 'key' })
    ElMessage.success('关键工序识别任务已提交')
  } catch (e) {
    if (!detectDialog.visible) return
    const msg = getErrorMessage(e, '关键工序识别任务启动失败')
    status.value = 'FAILED'
    error.value = msg
    ElMessage.error(msg)
  } finally {
    detectDialog.loading = false
  }
}

async function startDetect() {
  if (detectDialog.loading) return
  if (!detectDialog.part?.part_id) {
    ElMessage.warning('检测对象不能为空')
    return
  }
  if (!(detectDialog.process?.process_exec_id || detectDialog.process?.process_id)) {
    ElMessage.warning('请先选择工序')
    return
  }
  if (!selectedSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV/TXT 文件')
    return
  }
  task.clearPoll()
  detectDialog.loading = true
  status.value = 'RUNNING'
  error.value = ''
  result.value = {}
  try {
    const part = detectDialog.part
    const proc = detectDialog.process || {}
    const res = await startWarningDetectTask({
      partId: part.part_id,
      partCode: part.part_code,
      partName: part.part_name,
      pathText: part.path_text,
      processId: proc.process_exec_id || proc.process_id,
      processName: proc.process_name,
      processOrder: proc.process_order,
      deviceId: proc.device_id,
      deviceName: proc.device_name,
      sampleIds: selectedSampleIds.value
    })
    if (!detectDialog.visible) return
    task.acceptTask(res, { mode: 'detect' })
    ElMessage.success('异常检测任务已提交')
    loadDetectResults()
  } catch (e) {
    if (!detectDialog.visible) return
    const msg = getErrorMessage(e, '异常检测任务启动失败')
    status.value = 'FAILED'
    error.value = msg
    ElMessage.error(msg)
  } finally {
    detectDialog.loading = false
  }
}

function keyField(type) {
  const keys = {
    code: ['keyProcessCode', 'key_process_code', 'processCode', 'process_code'],
    name: ['keyProcessName', 'key_process_name', 'processName', 'process_name'],
    confidence: ['confidence', 'score'],
    score: ['score', 'confidence'],
    reason: ['reason'],
    suggestion: ['suggestion']
  }
  return pickValue(result.value, keys[type] || [type])
}

function pickValue(source, keys) {
  if (!source || typeof source !== 'object') return undefined
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && source[key] !== '') return source[key]
  }
  return undefined
}

async function loadDetectResults() {
  resultLoading.value = true
  try {
    const res = await listWarningDetectResults(resultQuery)
    const data = getApiPayload(res)
    resultRows.value = Array.isArray(data.rows) ? data.rows : []
    resultTotal.value = Number(data.total || 0)
  } finally {
    resultLoading.value = false
  }
}

function downloadDataFile(row) {
  const url = row?.dataFileUrl || row?.data_file_url
  if (url) window.open(url, '_blank')
}

async function loadDevs() {
  deviceLoading.value = true
  try {
    const res = await listWarningDevices(deviceQuery)
    const payload = res?.data || {}
    const rows = Array.isArray(payload.rows) ? payload.rows : []
    deviceList.value = rows
    if (!selectedDevice.value && rows.length) await pickDev(rows[0])
  } finally {
    deviceLoading.value = false
  }
}

async function loadDevPart(deviceRow) {
  const deviceId = deviceRow?.device_id
  if (!deviceId) {
    devicePartList.value = []
    devicePartTotal.value = 0
    return
  }
  devicePartLoading.value = true
  try {
    const res = await listWarningDeviceParts(deviceId)
    const allRows = Array.isArray(res?.data) ? res.data : []
    devicePartTotal.value = allRows.length
    const start = (devicePartQuery.page_num - 1) * devicePartQuery.page_size
    devicePartList.value = allRows.slice(start, start + devicePartQuery.page_size)
  } finally {
    devicePartLoading.value = false
  }
}

async function pickDev(row) {
  selectedDevice.value = row || null
  devicePartQuery.page_num = 1
  await loadDevPart(row)
}

function queryDev() {
  deviceQuery.page_num = 1
  loadDevs()
}

function resetDev() {
  deviceQuery.keyword = ''
  deviceQuery.risk_level = ''
  deviceQuery.page_num = 1
  loadDevs()
}

function val(value) {
  return value === undefined || value === null || value === '' ? '--' : value
}

function formatProcess(row) {
  const order = row?.process_order
  const name = row?.process_name
  if (order !== undefined && order !== null && order !== '' && name) return `${order}. ${name}`
  return name || '--'
}

onMounted(() => {
  loadParts()
  loadDevs()
  loadDetectResults()
})
</script>

<style scoped>
.feedback-hero {
  border-top: 4px solid #4c8fdc;
}

.warning-section {
  margin-bottom: 18px;
  border-top: 4px solid #7fb0ea;
}

.inner-card {
  min-height: 480px;
  border-radius: 12px;
}

.inner-title {
  position: relative;
  padding-left: 12px;
  font-size: 17px;
  font-weight: 700;
  color: #1f2d3d;
}

.inner-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  width: 4px;
  height: 18px;
  background: #4c8fdc;
  border-radius: 4px;
}

.selected-info {
  margin-bottom: 14px;
}

.instance-panel {
  margin-top: 18px;
}

.instance-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.sub-title {
  font-size: 15px;
}

.instance-meta {
  color: #607081;
  font-size: 13px;
}

.selected-path,
.process-desc,
.process-device,
.info-desc {
  color: #607081;
  line-height: 1.8;
}

.info-box {
  padding: 14px 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
}

.info-label {
  color: #607081;
  font-size: 13px;
  margin-bottom: 6px;
}

.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 455px;
  overflow-y: auto;
  padding-right: 6px;
}

.process-card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
}

.process-main {
  min-width: 0;
  flex: 1;
}

.process-title {
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 6px;
}

.process-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.card-header-row,
.detect-summary,
.file-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.file-toolbar {
  justify-content: flex-start;
  margin: 12px 0;
}

.detect-result {
  margin-top: 16px;
}

.part-detail-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.part-image-panel {
  padding: 16px;
  border: 1px solid #dbe4ee;
  border-radius: 10px;
  background: #fbfdff;
}

.part-image-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.part-image-uploader,
.part-image-uploader :deep(.el-upload) {
  width: 100%;
}

.part-image-box {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border: 1px dashed #b8c7d9;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.part-image-box img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #f6f8fb;
}

.part-image-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #607081;
}

.part-image-plus {
  width: 34px;
  height: 34px;
  line-height: 30px;
  border: 1px solid #9bb8da;
  border-radius: 50%;
  color: #4c8fdc;
  font-size: 26px;
  text-align: center;
}

.part-info-panel {
  min-width: 0;
}

@media (max-width: 900px) {
  .part-detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>

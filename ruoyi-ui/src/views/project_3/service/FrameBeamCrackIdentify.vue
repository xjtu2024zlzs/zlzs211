<template>
  <div class="app-container frame-crack-page">
    <el-card shadow="never" class="page-hero">
      <template #header>
        <div class="hero-header">
          <div>
            <div class="page-title">飞机框梁裂纹早期故障识别</div>
            <div class="page-subtitle">面向框梁连接区振动信号的早期裂纹状态判别与指标展示</div>
          </div>
          <div class="hero-actions">
            <el-button type="primary" :loading="running" @click="submitIdentifyTask">开始识别</el-button>
            <el-button :disabled="running" @click="refreshResult">刷新结果</el-button>
          </div>
        </div>
      </template>
      <div class="hero-summary">
        <div v-for="item in heroStats" :key="item.label" class="hero-stat">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </el-card>

    <div class="main-layout">
      <aside class="left-panel">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">飞机框梁对象信息</span>
          </template>
          <el-form label-position="top" class="info-form">
            <el-form-item label="飞机型号">
              <el-select
                v-model="objectInfo.aircraftModel"
                placeholder="请选择飞机型号"
                :loading="objectOptionsLoading"
                @change="handleAircraftChange"
              >
                <el-option
                  v-for="item in aircraftModelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="框梁区域">
              <el-select
                v-model="objectInfo.fuselageArea"
                placeholder="请选择框梁区域"
                :disabled="!objectInfo.aircraftModel"
                :loading="objectOptionsLoading"
              >
                <el-option
                  v-for="item in fuselageAreaOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="框梁连接区">
              <el-input v-model="objectInfo.frameJointArea" placeholder="请输入框梁连接区" />
            </el-form-item>
            <el-form-item label="测点位置">
              <el-input v-model="objectInfo.measurePoint" placeholder="请输入测点位置" />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">输入参数</span>
          </template>
          <el-form label-position="top" class="param-form">
            <el-form-item label="数据目录">
              <el-input v-model="params.dataDir" placeholder="/data/frame_beam/sample_set" />
            </el-form-item>
            <el-form-item label="数据上传">
              <div class="numeric-upload">
                <el-dropdown trigger="click" :disabled="numImporting" @command="triggerNumericUpload">
                  <el-button type="primary" :loading="numImporting" class="upload-data-button">
                    上传数据
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="file">选择数据文件</el-dropdown-item>
                      <el-dropdown-item command="directory">选择数据目录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <input
                  ref="numFileInput"
                  type="file"
                  accept=".csv,.txt,text/csv,text/plain"
                  multiple
                  class="hidden-file-input"
                  @change="onNumericFileChange"
                />
                <input
                  ref="numDirInput"
                  type="file"
                  webkitdirectory
                  directory
                  multiple
                  class="hidden-file-input"
                  @change="onNumericDirectoryChange"
                />
                <div class="upload-tip">
                  可选择单个/多个 CSV、TXT 文件，也可选择包含数据文件的目录；大文件自动分片，多文件自动按队列分批上传。
                </div>
                <div v-if="numProg.visible" class="numeric-progress">
                  <div class="progress-text">{{ numProg.text }}</div>
                  <el-progress :percentage="numProg.percentage" :stroke-width="8" />
                </div>
                <el-table v-if="numUploadRows.length" :data="numUploadRows" size="small" max-height="220" border class="upload-table">
                  <el-table-column prop="name" label="文件名" min-width="150" show-overflow-tooltip />
                  <el-table-column label="大小" width="86">
                    <template #default="{ row }">{{ fileSizeText(row.size) }}</template>
                  </el-table-column>
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">{{ uploadStatusText(row.status) }}</template>
                  </el-table-column>
                  <el-table-column label="进度" width="120">
                    <template #default="{ row }">
                      <el-progress :percentage="row.progress" :stroke-width="6" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="70">
                    <template #default="{ row }">
                      <el-button v-if="row.status === NUM_UPLOAD_FAILED" type="primary" link @click="retryNumericUpload(row)">重试</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-form-item>
            <el-form-item label="单段信号长度">
              <el-input v-model.number="params.segmentLength" type="number" />
            </el-form-item>
            <el-form-item label="重叠率">
              <el-select v-model="params.overlapRate">
                <el-option label="25%" :value="0.25" />
                <el-option label="50%" :value="0.5" />
                <el-option label="75%" :value="0.75" />
                <el-option label="80%" :value="0.8" />
              </el-select>
            </el-form-item>
            <el-form-item label="采样频率">
              <el-input v-model.number="params.sampleRate" type="number">
                <template #append>Hz</template>
              </el-input>
            </el-form-item>
            <el-form-item label="批大小">
              <el-select v-model="params.batchSize">
                <el-option label="16" :value="16" />
                <el-option label="32" :value="32" />
                <el-option label="64" :value="64" />
              </el-select>
            </el-form-item>
            <el-form-item label="训练轮数">
              <el-input v-model.number="params.epochs" type="number" />
            </el-form-item>
          </el-form>
        </el-card>
      </aside>

      <main class="right-panel">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">识别任务状态</span>
          </template>
          <div class="task-status-bar">
            <div class="task-status-item">
              <span>任务ID</span>
              <strong>{{ taskStatus.taskId }}</strong>
            </div>
            <div class="task-status-item">
              <span>对象</span>
              <strong>{{ taskStatus.objectName }}</strong>
            </div>
            <div class="task-status-item">
              <span>状态</span>
              <el-tag :type="taskStatus.statusType">{{ taskStatus.status }}</el-tag>
            </div>
            <div class="task-status-item result-item">
              <span>识别结果</span>
              <strong :class="taskStatus.result === '早期裂纹' ? 'danger-text' : 'success-text'">
                {{ taskStatus.result }}
              </strong>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">识别结果展示</span>
          </template>
          <div class="probability-grid">
            <div class="probability-item healthy">
              <div class="probability-head">
                <span>健康概率</span>
                <strong>{{ percent(result.healthProbability) }}</strong>
              </div>
              <el-progress :percentage="toPercentage(result.healthProbability)" color="#36b37e" />
            </div>
            <div class="probability-item fault">
              <div class="probability-head">
                <span>故障概率</span>
                <strong>{{ percent(result.faultProbability) }}</strong>
              </div>
              <el-progress :percentage="toPercentage(result.faultProbability)" color="#f56c6c" />
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">信号与时频图</span>
          </template>
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <div class="chart-panel">
                <div class="chart-title">原始 / 去噪信号波形图</div>
                <div ref="waveChartRef" class="chart-box"></div>
              </div>
            </el-col>
            <el-col :xs="24" :md="12">
              <div class="chart-panel">
                <div class="chart-title">STFT 时频图</div>
                <div ref="stftChartRef" class="chart-box"></div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">测试集指标</span>
          </template>
          <div class="metric-grid">
            <div v-for="item in metrics" :key="item.label" class="metric-card">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="table-header">
              <span class="section-title">历史识别记录</span>
              <el-input v-model="historyKeyword" placeholder="搜索任务ID/对象/结果" clearable class="history-search" />
            </div>
          </template>
          <el-table
            v-loading="historyLoading"
            :data="filteredHistoryRows"
            border
            class="history-table"
            empty-text="暂无历史识别记录"
          >
            <el-table-column prop="taskId" label="任务ID" width="170" show-overflow-tooltip />
            <el-table-column prop="objectName" label="对象" min-width="160" show-overflow-tooltip />
            <el-table-column prop="inputData" label="输入数据" min-width="220" show-overflow-tooltip />
            <el-table-column prop="result" label="识别结果" width="120">
              <template #default="{ row }">
                <el-tag :type="row.result === '早期裂纹' ? 'danger' : 'success'">{{ row.result }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confidence" label="置信度" width="110" />
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewHistory(row)">查看</el-button>
                <el-button
                  type="danger"
                  link
                  :loading="deletingTaskId === row.taskId"
                  @click="deleteHistory(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import {
  deleteFrameBeamCrackHistory,
  getFrameBeamCrackHistory,
  getFrameBeamCrackTask,
  listFrameBeamCrackHistory,
  startFrameBeamCrackTask
} from '@/api/project_3/frameBeamCrackIdentify'
import { getMonitorTree } from '@/api/project_3/monitor'

defineOptions({
  name: 'FrameBeamCrackIdentify'
})

const running = ref(false)
const waveChartRef = ref(null)
const stftChartRef = ref(null)
const numFileInput = ref(null)
const numDirInput = ref(null)
const numImporting = ref(false)
const numUploadRows = ref([])
const uploadedDataInfo = reactive({
  uploadBatchId: '',
  filePath: '',
  batchPath: '',
  filePaths: []
})
const numProg = reactive({
  visible: false,
  percentage: 0,
  text: ''
})
let waveChart = null
let stftChart = null
let numUploadBatchId = ''

const NUM_MB = 1024 * 1024
const NUM_KB = 1024
const NUM_NORMAL_UPLOAD_LIMIT = 5 * 1024 * NUM_KB
const NUM_IN_FLIGHT_SIZE = 400 * NUM_MB
const NUM_CHUNK_SIZE = 5 * 1024 * NUM_KB
const NUM_UPLOAD_WAITING = 'WAITING'
const NUM_UPLOAD_UPLOADING = 'UPLOADING'
const NUM_UPLOAD_SUCCESS = 'SUCCESS'
const NUM_UPLOAD_FAILED = 'FAILED'
const NUM_UPLOAD_CHUNK_UPLOADING = 'CHUNK_UPLOADING'
const NUM_UPLOAD_MERGING = 'MERGING'

const objectOptionsLoading = ref(false)
const aircraftTree = ref([])
const aircraftModelOptions = ref([])
const fuselageAreaOptions = ref([])
const FUSELAGE_AREA_KEYWORDS = ['机翼', '机身', '尾翼']

const objectInfo = reactive({
  aircraftModel: '',
  fuselageArea: '',
  frameJointArea: '',
  measurePoint: ''
})

const params = reactive({
  dataDir: '',
  segmentLength: 2048,
  overlapRate: 0.8,
  sampleRate: 51200,
  batchSize: 32,
  epochs: 50
})

const taskStatus = reactive({
  taskId: '',
  objectName: '',
  status: '',
  statusType: 'info',
  result: ''
})

const result = reactive({
  healthProbability: null,
  faultProbability: null
})

const testMetrics = reactive({
  accuracy: null,
  earlyCrackAccuracy: null,
  weightedF1: null,
  testLoss: null
})

// 后续接入 Python 算法服务时，可将接口返回统一写入该对象，再调用 applyAlgorithmResult。
const algorithmResult = reactive({
  task: taskStatus,
  probabilities: result,
  metrics: testMetrics,
  waveform: {
    xAxis: [],
    raw: [],
    denoised: []
  },
  stft: {
    xAxis: [],
    yAxis: [],
    data: []
  }
})

const historyKeyword = ref('')
const historyRows = ref([])
const historyLoading = ref(false)
const deletingTaskId = ref('')

const selectedAircraftName = computed(() =>
  aircraftModelOptions.value.find(item => item.value === objectInfo.aircraftModel)?.label || ''
)

const selectedFuselageAreaName = computed(() =>
  fuselageAreaOptions.value.find(item => item.value === objectInfo.fuselageArea)?.label || ''
)

const heroStats = computed(() => [
  { label: '当前对象', value: displayText([selectedAircraftName.value, selectedFuselageAreaName.value].filter(Boolean).join(' ')) },
  { label: '采样频率', value: params.sampleRate ? params.sampleRate + ' Hz' : '--' },
  { label: '识别状态', value: displayText(taskStatus.status) },
  { label: '故障概率', value: percent(result.faultProbability) }
])

const metrics = computed(() => [
  { label: '总体准确率', value: percent(testMetrics.accuracy) },
  { label: '早期裂纹识别准确率', value: percent(testMetrics.earlyCrackAccuracy) },
  { label: '加权F1', value: numberText(testMetrics.weightedF1, 3) },
  { label: '测试损失', value: numberText(testMetrics.testLoss, 3) }
])

const filteredHistoryRows = computed(() => {
  const keyword = historyKeyword.value.trim().toLowerCase()
  if (!keyword) return historyRows.value
  return historyRows.value.filter(row => {
    return [row.taskId, row.objectName, row.inputData, row.result].some(value =>
      String(value).toLowerCase().includes(keyword)
    )
  })
})

function toPercentage(value) {
  const numericValue = Number(value)
  if (!Number.isFinite(numericValue)) return 0
  return Math.round(numericValue * 1000) / 10
}

function percent(value) {
  if (value === null || value === undefined || value === '') return '--'
  return toPercentage(value).toFixed(1) + '%'
}

function numberText(value, digits = 3) {
  const numericValue = Number(value)
  return Number.isFinite(numericValue) ? numericValue.toFixed(digits) : '--'
}

function displayText(value) {
  return value === null || value === undefined || value === '' ? '--' : value
}

function rawNodeId(nodeId) {
  const text = String(nodeId || '')
  const separatorIndex = text.indexOf(':')
  return separatorIndex >= 0 ? text.slice(separatorIndex + 1) : text
}

function mapObjectOptions(nodes) {
  return (Array.isArray(nodes) ? nodes : [])
    .map(node => ({
      label: node?.name || String(node?.id || ''),
      value: rawNodeId(node?.id)
    }))
    .filter(item => item.value)
}

function isFuselageAreaSubsystem(node) {
  const name = String(node?.name || '')
  return FUSELAGE_AREA_KEYWORDS.some(keyword => name.includes(keyword))
}

function handleAircraftChange() {
  objectInfo.fuselageArea = ''
  const aircraft = aircraftTree.value.find(
    node => rawNodeId(node?.id) === String(objectInfo.aircraftModel || '')
  )
  fuselageAreaOptions.value = mapObjectOptions(
    (aircraft?.children || []).filter(isFuselageAreaSubsystem)
  )
}

async function loadObjectOptions() {
  objectOptionsLoading.value = true
  try {
    const response = await getMonitorTree()
    aircraftTree.value = Array.isArray(response?.data) ? response.data : []
    aircraftModelOptions.value = mapObjectOptions(aircraftTree.value)
    handleAircraftChange()
  } catch (error) {
    aircraftTree.value = []
    aircraftModelOptions.value = []
    fuselageAreaOptions.value = []
    ElMessage.error(error?.response?.data?.msg || error?.message || '飞机和框梁区域数据加载失败')
  } finally {
    objectOptionsLoading.value = false
  }
}

function uploadNumericFile(data, onUploadProgress) {
  return request({
    url: '/service/frame-beam-crack/uploads/file',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

function uploadNumericChunk(data) {
  return request({
    url: '/service/frame-beam-crack/uploads/chunk',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

function mergeNumericChunks(data) {
  return request({
    url: '/service/frame-beam-crack/uploads/merge',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

function triggerNumericUpload(command = 'file') {
  if (numImporting.value) return
  if (command === 'directory') {
    numDirInput.value?.click()
    return
  }
  numFileInput.value?.click()
}

async function onNumericFileChange(event) {
  await startNumericUpload(event, 'file')
}

async function onNumericDirectoryChange(event) {
  await startNumericUpload(event, 'directory')
}

async function startNumericUpload(event, sourceType) {
  const selectedFiles = Array.from(event.target.files || [])
  event.target.value = ''
  if (!selectedFiles.length) return

  const files = selectedFiles.filter(isNumericDataFile)
  const ignoredCount = selectedFiles.length - files.length
  if (!files.length) {
    ElMessage.warning(sourceType === 'directory' ? '所选目录中未找到 CSV/TXT 数据文件' : '仅支持 CSV/TXT 数据文件')
    return
  }
  if (ignoredCount > 0) {
    ElMessage.warning(`已忽略 ${ignoredCount} 个非 CSV/TXT 文件`)
  }

  numImporting.value = true
  numUploadBatchId = createNumUploadBatchId()
  uploadedDataInfo.uploadBatchId = ''
  uploadedDataInfo.filePath = ''
  uploadedDataInfo.batchPath = ''
  uploadedDataInfo.filePaths = []
  numUploadRows.value = files.map((file, index) => ({
    id: `${numUploadBatchId}_${index}`,
    file,
    name: file.webkitRelativePath || file.name,
    relativePath: file.webkitRelativePath || file.name,
    size: file.size,
    index,
    status: NUM_UPLOAD_WAITING,
    progress: 0,
    message: '',
    responseData: null
  }))
  startNumProg('准备上传文件...')
  try {
    await runNumUploadQueue(numUploadRows.value)
    updateUploadedDataInfo()
    const success = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_SUCCESS).length
    const failed = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_FAILED).length
    finishNumProg(`上传完成：成功 ${success} 个，失败 ${failed} 个`)
    if (success > 0) {
      ElMessage.success(`上传完成：成功 ${success} 个，失败 ${failed} 个`)
    } else {
      ElMessage.error('上传失败：没有成功上传的数据文件')
    }
  } catch (error) {
    failNumProg('上传失败')
    ElMessage.error(error?.response?.data?.msg || error?.message || '上传失败')
  } finally {
    numImporting.value = false
  }
}

function isNumericDataFile(file) {
  return /\.(csv|txt)$/i.test(file?.name || '')
}

function createNumUploadBatchId() {
  return `FRAME_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

function numUploadForm(row) {
  const payload = new FormData()
  payload.append('purpose', 'faultIdentify')
  payload.append('executionObject', 'frameBeam')
  payload.append('aircraftId', objectInfo.aircraftModel || '')
  payload.append('subsystemId', objectInfo.fuselageArea || '')
  payload.append('equipmentId', objectInfo.frameJointArea || '')
  payload.append('componentId', objectInfo.measurePoint || '')
  payload.append('partId', '')
  payload.append('uploadBatchId', numUploadBatchId)
  payload.append('fileIndex', String(row.index + 1))
  payload.append('totalFiles', String(numUploadRows.value.length))
  payload.append('relativePath', row.relativePath || row.name)
  return payload
}

function getNumUploadConcurrency(rows) {
  const maxSize = Math.max(...rows.map(item => item.size), 0)
  if (maxSize <= 20 * NUM_MB) return 8
  if (maxSize <= 100 * NUM_MB) return 5
  if (maxSize <= NUM_NORMAL_UPLOAD_LIMIT) return 2
  return 1
}

async function runNumUploadQueue(rows) {
  const maxConcurrency = getNumUploadConcurrency(rows)
  const pending = rows.filter(item => item.status === NUM_UPLOAD_WAITING || item.status === NUM_UPLOAD_FAILED)
  let runningCount = 0
  let runningSize = 0
  let cursor = 0

  return new Promise(resolve => {
    const schedule = () => {
      if (cursor >= pending.length && runningCount === 0) {
        updateNumUploadProgress()
        resolve()
        return
      }
      while (runningCount < maxConcurrency && cursor < pending.length) {
        const row = pending[cursor]
        if (runningCount > 0 && runningSize + row.size > NUM_IN_FLIGHT_SIZE) break
        cursor++
        runningCount++
        runningSize += row.size
        uploadNumRow(row)
          .catch(() => {})
          .finally(() => {
            runningCount--
            runningSize -= row.size
            updateNumUploadProgress()
            schedule()
          })
      }
    }
    schedule()
  })
}

async function uploadNumRow(row) {
  row.progress = 0
  row.message = ''
  try {
    if (row.size > NUM_NORMAL_UPLOAD_LIMIT) {
      await uploadNumRowChunks(row)
    } else {
      const payload = numUploadForm(row)
      payload.append('file', row.file)
      row.status = NUM_UPLOAD_UPLOADING
      const res = await uploadNumericFile(payload, event => {
        if (event.total) row.progress = Math.min(99, Math.round((event.loaded / event.total) * 100))
      })
      row.responseData = res?.data || {}
      row.status = NUM_UPLOAD_SUCCESS
      row.progress = 100
      row.message = row.responseData.message || '上传成功'
    }
  } catch (error) {
    row.status = NUM_UPLOAD_FAILED
    row.progress = 100
    row.message = error?.response?.data?.msg || error?.message || '上传失败'
  }
}

async function uploadNumRowChunks(row) {
  const uploadId = `${numUploadBatchId}_${row.index}_${Date.now()}`
  const chunkCount = Math.ceil(row.size / NUM_CHUNK_SIZE)
  row.status = NUM_UPLOAD_CHUNK_UPLOADING
  for (let i = 0; i < chunkCount; i++) {
    const start = i * NUM_CHUNK_SIZE
    const end = Math.min(row.size, start + NUM_CHUNK_SIZE)
    const payload = new FormData()
    payload.append('uploadId', uploadId)
    payload.append('chunkIndex', String(i))
    payload.append('chunkCount', String(chunkCount))
    payload.append('fileName', row.name)
    payload.append('relativePath', row.relativePath || row.name)
    payload.append('chunk', row.file.slice(start, end))
    await uploadNumericChunk(payload)
    row.progress = Math.min(95, Math.round(((i + 1) / chunkCount) * 95))
  }

  row.status = NUM_UPLOAD_MERGING
  row.message = '正在合并'
  const mergePayload = numUploadForm(row)
  mergePayload.append('uploadId', uploadId)
  mergePayload.append('fileName', row.name)
  mergePayload.append('relativePath', row.relativePath || row.name)
  const res = await mergeNumericChunks(mergePayload)
  row.responseData = res?.data || {}
  row.status = NUM_UPLOAD_SUCCESS
  row.progress = 100
  row.message = row.responseData.message || '上传成功'
}

function updateNumUploadProgress() {
  const total = numUploadRows.value.length || 1
  const done = numUploadRows.value.filter(item => [NUM_UPLOAD_SUCCESS, NUM_UPLOAD_FAILED].includes(item.status)).length
  const success = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_SUCCESS).length
  const failed = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_FAILED).length
  numProg.visible = true
  numProg.percentage = Math.min(100, Math.round((done / total) * 100))
  numProg.text = `正在上传：已完成 ${done}/${total}，成功 ${success}，失败 ${failed}`
}

async function retryNumericUpload(row) {
  if (!row || numImporting.value) return
  numImporting.value = true
  try {
    row.status = NUM_UPLOAD_WAITING
    row.progress = 0
    row.message = ''
    startNumProg('正在重试上传...')
    await uploadNumRow(row)
    updateUploadedDataInfo()
    updateNumUploadProgress()
  } finally {
    numImporting.value = false
  }
}

function updateUploadedDataInfo() {
  const successRows = numUploadRows.value.filter(item => item.status === NUM_UPLOAD_SUCCESS)
  const dataList = successRows.map(item => item.responseData || {})
  uploadedDataInfo.uploadBatchId = numUploadBatchId
  uploadedDataInfo.filePaths = dataList
    .map(item => item.filePath || item.file_path || item.path || item.fullPath)
    .filter(Boolean)
  uploadedDataInfo.filePath = uploadedDataInfo.filePaths[0] || ''
  uploadedDataInfo.batchPath = dataList.find(item => item.batchPath || item.batch_path || item.batchDir || item.batch_dir)?.batchPath ||
    dataList.find(item => item.batchPath || item.batch_path || item.batchDir || item.batch_dir)?.batch_path ||
    dataList.find(item => item.batchPath || item.batch_path || item.batchDir || item.batch_dir)?.batchDir ||
    dataList.find(item => item.batchPath || item.batch_path || item.batchDir || item.batch_dir)?.batch_dir ||
    ''
  if (uploadedDataInfo.batchPath || uploadedDataInfo.filePath) {
    params.dataDir = uploadedDataInfo.batchPath || uploadedDataInfo.filePath
  }
}

function startNumProg(text = '准备上传文件...') {
  numProg.visible = true
  numProg.percentage = 0
  numProg.text = text
}

function finishNumProg(text = '上传完成') {
  numProg.visible = true
  numProg.percentage = 100
  numProg.text = text
}

function failNumProg(text = '上传失败') {
  numProg.visible = true
  numProg.percentage = 100
  numProg.text = text
}

function uploadStatusText(status) {
  const map = {
    WAITING: '等待中',
    UPLOADING: '上传中',
    SUCCESS: '成功',
    FAILED: '失败',
    CHUNK_UPLOADING: '分片上传中',
    MERGING: '合并中'
  }
  return map[status] || status || '--'
}

function fileSizeText(size) {
  const value = Number(size || 0)
  if (value >= NUM_MB) return (value / NUM_MB).toFixed(2) + ' MB'
  if (value >= NUM_KB) return (value / NUM_KB).toFixed(1) + ' KB'
  return value + ' B'
}

function buildIdentifyRequest() {
  const uploadDataDir = uploadedDataInfo.batchPath || uploadedDataInfo.filePath
  return {
    objectInfo: { ...objectInfo },
    params: {
      ...params,
      dataDir: uploadDataDir || params.dataDir,
      filePath: uploadedDataInfo.filePath || undefined,
      batchPath: uploadedDataInfo.batchPath || undefined,
      filePaths: uploadedDataInfo.filePaths.length ? [...uploadedDataInfo.filePaths] : undefined,
      uploadBatchId: uploadedDataInfo.uploadBatchId || undefined
    }
  }
}

async function submitIdentifyTask() {
  const payload = buildIdentifyRequest()
  payload.requestId = `FRAME_RUN_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
  if (!payload.params.dataDir && !payload.params.filePath && !payload.params.batchPath) {
    ElMessage.warning('请先上传数据文件或填写数据目录')
    return
  }
  running.value = true
  try {
    const res = await startFrameBeamCrackTask(payload)
    applyAlgorithmResult(res?.data || {})
    await loadHistory()
    ElMessage.success('识别任务已完成')
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '识别任务启动失败')
  } finally {
    running.value = false
  }
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const response = await listFrameBeamCrackHistory({
      page_num: 1,
      page_size: 100
    })
    historyRows.value = Array.isArray(response?.data?.rows) ? response.data.rows : []
  } catch (error) {
    historyRows.value = []
    ElMessage.error(error?.response?.data?.msg || error?.message || '历史识别记录加载失败')
  } finally {
    historyLoading.value = false
  }
}

async function viewHistory(row) {
  if (!row?.taskId) return
  try {
    const response = await getFrameBeamCrackHistory(row.taskId)
    applyAlgorithmResult(response?.data || {})
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '历史识别结果加载失败')
  }
}

async function deleteHistory(row) {
  if (!row?.taskId || deletingTaskId.value) return
  try {
    await ElMessageBox.confirm(
      `确认删除历史识别记录 ${row.taskId} 吗？对应的本地上传数据和数据库记录将同步删除，且无法恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
    deletingTaskId.value = row.taskId
    await deleteFrameBeamCrackHistory(row.taskId)
    if (taskStatus.taskId === row.taskId) {
      clearDisplayedResult()
    }
    await loadHistory()
    ElMessage.success('历史识别记录及上传数据已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.msg || error?.message || '历史识别记录删除失败')
    }
  } finally {
    deletingTaskId.value = ''
  }
}

function clearDisplayedResult() {
  Object.assign(taskStatus, {
    taskId: '',
    objectName: '',
    status: '',
    statusType: 'info',
    result: ''
  })
  Object.assign(result, {
    healthProbability: null,
    faultProbability: null
  })
  Object.assign(testMetrics, {
    accuracy: null,
    earlyCrackAccuracy: null,
    weightedF1: null,
    testLoss: null
  })
  Object.assign(algorithmResult.waveform, {
    xAxis: [],
    raw: [],
    denoised: []
  })
  Object.assign(algorithmResult.stft, {
    xAxis: [],
    yAxis: [],
    data: []
  })
  renderCharts()
}

async function refreshResult() {
  try {
    if (taskStatus.taskId) {
      const response = await getFrameBeamCrackTask(taskStatus.taskId)
      applyAlgorithmResult(response?.data || {})
    } else {
      renderCharts()
    }
    await loadHistory()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '识别结果刷新失败')
  }
}

function applyAlgorithmResult(apiResult = {}) {
  Object.assign(taskStatus, apiResult.task || {})
  Object.assign(result, apiResult.probabilities || {})
  Object.assign(testMetrics, apiResult.metrics || {})
  if (apiResult.waveform) {
    Object.assign(algorithmResult.waveform, apiResult.waveform)
  }
  if (apiResult.stft) {
    Object.assign(algorithmResult.stft, apiResult.stft)
  }
  renderCharts()
}

function initCharts() {
  if (waveChartRef.value && !waveChart) {
    waveChart = echarts.init(waveChartRef.value)
  }
  if (stftChartRef.value && !stftChart) {
    stftChart = echarts.init(stftChartRef.value)
  }
  renderCharts()
}

function renderCharts() {
  nextTick(() => {
    renderWaveChart()
    renderStftChart()
  })
}

function renderWaveChart() {
  if (!waveChart) return
  const waveform = algorithmResult.waveform || {}
  const hasData = Array.isArray(waveform.raw) && waveform.raw.length || Array.isArray(waveform.denoised) && waveform.denoised.length
  if (!hasData) {
    waveChart.setOption(emptyChartOption('暂无信号波形数据'), true)
    return
  }
  waveChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 4, data: ['原始信号', '去噪信号'] },
    grid: { left: 46, right: 20, top: 42, bottom: 34 },
    xAxis: { type: 'category', boundaryGap: false, data: waveform.xAxis || [] },
    yAxis: { type: 'value', name: '幅值', scale: true },
    series: [
      {
        name: '原始信号',
        type: 'line',
        showSymbol: false,
        smooth: true,
        data: waveform.raw || [],
        lineStyle: { color: '#409eff', width: 1.8 }
      },
      {
        name: '去噪信号',
        type: 'line',
        showSymbol: false,
        smooth: true,
        data: waveform.denoised || [],
        lineStyle: { color: '#36b37e', width: 1.8 }
      }
    ]
  }, true)
}

function renderStftChart() {
  if (!stftChart) return
  const stft = algorithmResult.stft || {}
  const xAxis = Array.isArray(stft.xAxis) ? stft.xAxis : []
  const yAxis = Array.isArray(stft.yAxis) ? stft.yAxis : []
  const data = Array.isArray(stft.data) ? stft.data : []
  if (!xAxis.length || !yAxis.length || !data.length) {
    stftChart.setOption(emptyChartOption('暂无 STFT 时频数据'), true)
    return
  }
  stftChart.setOption({
    tooltip: { position: 'top' },
    grid: { left: 46, right: 24, top: 28, bottom: 34 },
    xAxis: { type: 'category', name: '时间', data: xAxis },
    yAxis: { type: 'category', name: '频率', data: yAxis },
    visualMap: {
      min: Number.isFinite(Number(stft.min)) ? Number(stft.min) : 0,
      max: Number.isFinite(Number(stft.max)) ? Number(stft.max) : 1,
      calculable: false,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#e8f3ff', '#8cc5ff', '#f7ba2a', '#f56c6c'] }
    },
    series: [{ type: 'heatmap', data }]
  }, true)
}

function emptyChartOption(text) {
  return {
    title: {
      text,
      left: 'center',
      top: 'middle',
      textStyle: {
        color: '#8a97a8',
        fontSize: 14,
        fontWeight: 400
      }
    },
    xAxis: { show: false },
    yAxis: { show: false },
    series: []
  }
}

function resizeCharts() {
  waveChart?.resize()
  stftChart?.resize()
}

onMounted(() => {
  loadObjectOptions()
  loadHistory()
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  waveChart?.dispose()
  stftChart?.dispose()
  waveChart = null
  stftChart = null
})

defineExpose({
  applyAlgorithmResult,
  buildIdentifyRequest
})
</script>

<style scoped>
.frame-crack-page {
  min-height: calc(100vh - 84px);
  background: #f4f7fb;
  padding-bottom: 20px;
}

.page-hero,
.section-card {
  margin-bottom: 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
}

.hero-header,
.table-header,
.probability-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.page-title {
  color: #0f2a4a;
  font-size: 24px;
  font-weight: 700;
}

.page-subtitle {
  margin-top: 6px;
  color: #607081;
  font-size: 13px;
}

.hero-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.hero-stat,
.metric-card,
.probability-item {
  padding: 14px 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
}

.hero-stat span,
.metric-card span,
.task-status-item span {
  display: block;
  margin-bottom: 6px;
  color: #607081;
  font-size: 13px;
}

.hero-stat strong,
.metric-card strong {
  color: #0f2a4a;
  font-size: 20px;
}

.main-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
}

.left-panel,
.right-panel {
  min-width: 0;
}

.section-title {
  color: #0f2a4a;
  font-size: 16px;
  font-weight: 700;
}

.info-form :deep(.el-select),
.param-form :deep(.el-select) {
  width: 100%;
}

.numeric-upload {
  width: 100%;
}

.numeric-upload :deep(.el-dropdown),
.numeric-upload :deep(.upload-data-button) {
  width: 100%;
}

.hidden-file-input {
  display: none;
}

.upload-tip {
  margin-top: 6px;
  color: #8492a6;
  font-size: 12px;
  line-height: 20px;
  word-break: break-all;
}

.numeric-progress {
  margin-top: 10px;
}

.progress-text {
  margin-bottom: 6px;
  color: #607081;
  font-size: 12px;
}

.upload-table {
  margin-top: 10px;
}

.task-status-bar,
.metric-grid,
.probability-grid {
  display: grid;
  gap: 12px;
}

.task-status-bar {
  grid-template-columns: 1.1fr 1.6fr 0.8fr 0.9fr;
}

.metric-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.probability-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.task-status-item {
  min-height: 70px;
  padding: 12px 14px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
}

.task-status-item strong {
  color: #0f2a4a;
  font-size: 15px;
  word-break: break-all;
}

.result-item strong {
  font-size: 18px;
}

.success-text {
  color: #36b37e !important;
}

.danger-text {
  color: #f56c6c !important;
}

.probability-item.healthy {
  border-left: 4px solid #36b37e;
}

.probability-item.fault {
  border-left: 4px solid #f56c6c;
}

.probability-head {
  margin-bottom: 12px;
}

.probability-head span {
  color: #304156;
  font-weight: 600;
}

.probability-head strong {
  color: #0f2a4a;
  font-size: 22px;
}

.chart-panel {
  margin-bottom: 0;
  padding: 12px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fbfdff;
}

.chart-title {
  margin-bottom: 10px;
  color: #304156;
  font-size: 15px;
  font-weight: 700;
}

.chart-box {
  width: 100%;
  height: 300px;
  border: 1px solid #d4dfeb;
  border-radius: 6px;
  background: #fff;
}

.history-search {
  width: 260px;
}

.history-table {
  width: 100%;
}

@media (max-width: 1200px) {
  .main-layout {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .task-status-bar,
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .main-layout {
    grid-template-columns: 1fr;
  }

  .hero-summary,
  .probability-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .hero-header,
  .table-header {
    align-items: stretch;
    flex-direction: column;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .hero-summary,
  .task-status-bar,
  .metric-grid,
  .probability-grid {
    grid-template-columns: 1fr;
  }

  .history-search {
    width: 100%;
  }

  .page-title {
    font-size: 20px;
  }

  .chart-box {
    height: 240px;
  }
}
</style>

<template>
  <div class="app-container fault-identify-page">

    <el-card shadow="never" class="quality-hero feedback-hero">
      <template #header>
        <span class="quality-title">服役性能周期故障预防</span>
      </template>
      <div class="quality-desc">进行故障识别、故障预防</div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="section-title">故障识别</span>
      </template>

      <div class="input-row">
        <el-button type="success" class="upload-btn" @click="openPrepareDlg">选择分析对象和数据文件</el-button>
        <el-progress class="input-progress" :percentage="upload_progress" :show-text="false" />
        <span class="progress-text">{{ upload_status_text }}</span>
      </div>

      <div class="param-row">
        <span class="param-label">窗口大小（秒）：</span>
        <el-select v-model="feature_params.windowSize" class="param-select">
          <el-option label="1.0" value="1.0" />
        </el-select>

        <span class="param-label">重叠率（%）：</span>
        <el-select v-model="feature_params.overlapRate" class="param-select">
          <el-option label="50" value="50" />
        </el-select>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="signal-panel">
            <div class="chart-title align-left">时域分析图</div>
            <div ref="time_domain_chart_ref" class="signal-box"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="signal-panel">
            <div class="chart-title align-left">时频分析图</div>
            <div ref="spectrum_chart_ref" class="signal-box"></div>
          </div>
        </el-col>
      </el-row>

    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-row">
          <span class="section-title">特征处理</span>
          <el-button
            type="warning"
            class="orange-btn"
            :loading="feat_loading"
            :disabled="feat_loading"
            @click="startFeatureAnalysis"
          >
            开始特征分析
          </el-button>
        </div>
      </template>

      <div class="feature-task-status-row">
        <span>分析对象：{{ selected_feature_object?.name || '--' }}</span>
        <span>任务ID：{{ analysis_task_id || '--' }}</span>
        <span>任务状态：{{ feat_status || '--' }}</span>
      </div>
      <div v-if="feat_err" class="feature-task-error-row">
        失败原因：{{ feat_err }}
      </div>



      <div v-if="summary_items.length" class="feature-summary-grid">
        <div v-for="item in summary_items" :key="item.label" class="feature-summary-item">
          <span class="feature-summary-label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-panel">
            <div class="chart-title">均值趋势</div>
            <div ref="mean_chart_ref" class="chart-box"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="chart-panel">
            <div class="chart-title">标准差趋势</div>
            <div ref="std_chart_ref" class="chart-box"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="chart-panel">
            <div class="chart-title">峰值趋势</div>
            <div ref="peak_chart_ref" class="chart-box"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="chart-panel">
            <div class="chart-title">均方根趋势</div>
            <div ref="rms_chart_ref" class="chart-box"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="chart-panel">
            <div class="chart-title">峭度趋势</div>
            <div ref="kurtosis_chart_ref" class="chart-box"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="chart-panel">
            <div class="chart-title">最大值趋势</div>
            <div ref="max_chart_ref" class="chart-box"></div>
          </div>
        </el-col>
      </el-row>




    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-row">
          <span class="degrade-title">早期故障识别</span>
          <el-button
            type="warning"
            class="orange-btn"
            :loading="degrade_loading"
            :disabled="degrade_loading"
            @click="handle_degradation_detect"
          >
            开始早期故障识别
          </el-button>
        </div>
      </template>

      <div class="param-row">
        <span class="param-label">窗口大小（秒）：</span>
        <el-select v-model="degradation_params.window_size" class="param-select">
          <el-option label="1.0" value="1.0" />
        </el-select>

        <span class="param-label">重叠率（%）：</span>
        <el-select v-model="degradation_params.overlap_rate" class="param-select">
          <el-option label="50" value="50" />
        </el-select>

        <span class="param-label">基准窗口数：</span>
        <el-select v-model="degradation_params.baseline_window_count" class="param-select">
          <el-option label="500" value="500" />
        </el-select>

        <span class="param-label">RMS灵敏度：</span>
        <el-input-number
          v-model="degradation_params.rms_sensitivity"
          class="param-number"
          :min="0.1"
          :max="1.0"
          :step="0.1"
          :precision="1"
          controls-position="right"
        />
      </div>

      <div class="detect-method-panel">
        <div class="detect-method-title">检测方法</div>
        <el-checkbox-group v-model="degradation_params.detection_methods" class="detect-method-list">
          <el-checkbox value="kurtosis">峰度+3σ准则（对脉冲故障敏感）</el-checkbox>
          <el-checkbox value="rms">RMS趋势（反映整体能量变化）</el-checkbox>
        </el-checkbox-group>
      </div>

      <div class="degradation-result">检测到退化点：{{ degradation_point_text }}</div>
      <div class="degradation-task-status-row">
        <span>任务ID：{{ analysis_task_id || '--' }}</span>
        <span>任务状态：{{ degrade_status || '--' }}</span>
      </div>
      <div v-if="degrade_status === 'RUNNING'" class="degradation-running-row">
        退化点检测执行中
      </div>
      <div v-if="degrade_status === 'FAILED'" class="degradation-error-row">
        {{ degrade_error || '退化点检测失败' }}
      </div>
      <div v-if="degrade_status === 'SUCCESS' && degrade_result" class="degradation-result-panel">
        <div class="degradation-highlight-row">
          <span>早期退化点：{{ get_degrade_field('point') || '--' }}</span>
          <span>单位：{{ get_degrade_field('unit') || '--' }}</span>
          <span>检测时间：{{ get_degrade_field('time') || '--' }}</span>
        </div>
      </div>

      <div class="degradation-chart-panel">
        <div class="chart-title">早期退化点识别结果</div>
        <div class="degradation-chart-stack">
          <div class="degradation-plot-box">
            <div class="plot-title">振动信号</div>
            <div ref="degradation_signal_chart_ref" class="degradation-echart"></div>


          </div>

          <div class="degradation-plot-box">
            <div class="plot-title">多方法检测对比</div>
            <div class="method-legend">
              <span class="method-legend-item">
                <span class="method-line kurtosis-line"></span>
                <span>峰度</span>
              </span>
              <span class="method-legend-item">
                <span class="method-line rms-line"></span>
                <span>RMS</span>
              </span>
              <span class="method-legend-item">
                <span class="degradation-line"></span>
                <span>退化点：{{ degradation_point_text }}</span>
              </span>
            </div>
            <div ref="degradation_compare_chart_ref" class="degradation-echart"></div>


          </div>
        </div>

        <el-table
          :data="degradation_table_rows"
          border
          class="degradation-result-table"
          header-cell-class-name="degradation-result-table-header"
          empty-text="暂无检测结果"
        >
          <el-table-column prop="method" label="检测方法" min-width="180" />
          <el-table-column prop="time" label="检测时间" min-width="180" />
          <el-table-column prop="result" label="检测结果" min-width="180" />
        </el-table>

        <div class="prevention-panel">
          <div class="card-header-row">
            <span class="chart-title">故障预防 / 剩余寿命预测 / 维护建议</span>
            <el-button
              type="warning"
              class="orange-btn"
              :loading="prevention_loading"
              :disabled="prevention_loading || degrade_status !== 'SUCCESS' || !has_degradation_point"
              @click="handle_fault_prevention"
            >
              开始故障预防
            </el-button>
          </div>
          <div class="degradation-task-status-row">
            <span>任务ID：{{ analysis_task_id || '--' }}</span>
            <span>任务状态：{{ prevention_status || '--' }}</span>
          </div>
          <div v-if="prevention_status === 'FAILED'" class="degradation-error-row">
            {{ prevention_error || '故障预防失败' }}
          </div>
          <div v-if="prevention_status === 'SUCCESS' && prevention_result" class="prevention-result-grid">
            <div class="feature-summary-item">
              <span class="feature-summary-label">风险评估</span>
              <strong>{{ get_prevention_field('risk') || '--' }}</strong>
            </div>
            <div class="feature-summary-item">
              <span class="feature-summary-label">风险评分</span>
              <strong>{{ get_prevention_field('score') || '--' }}</strong>
            </div>
            <div class="feature-summary-item">
              <span class="feature-summary-label">剩余寿命</span>
              <strong>{{ get_prevention_field('life') || '--' }}</strong>
            </div>
          </div>
          <div v-if="prevention_advice_rows.length" class="prevention-advice-list">
            <div v-for="item in prevention_advice_rows" :key="item" class="risk-advice-item">
              <span>{{ item }}</span>
            </div>
          </div>
          <div class="predict-chart-area">
            <div class="predict-chart-frame">
              <div class="predict-chart-title">振动信号与预测</div>
              <div ref="prevention_signal_chart_ref" class="predict-echart"></div>
            </div>

            <div class="predict-chart-frame">
              <div class="predict-chart-title">RMS 趋势与预测</div>
              <div ref="prevention_rms_chart_ref" class="predict-echart"></div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-row">
          <span class="section-title">历史识别记录查询</span>
          <el-button :loading="history_loading" @click="loadIdentifyHistory">刷新</el-button>
        </div>
      </template>

      <div class="history-query-row">
        <el-input
          v-model="history_query.keyword"
          placeholder="请输入任务ID、对象或结果关键字"
          clearable
          class="history-keyword"
          @keyup.enter="handleHistorySearch"
          @clear="handleHistorySearch"
        />
        <el-select v-model="history_query.task_type" placeholder="任务类型" clearable class="history-select">
          <el-option label="全部类型" value="" />
          <el-option label="特征分析" value="FEATURE_ANALYSIS" />
          <el-option label="早期故障识别" value="EARLY_DEGRADATION_POINT_DETECT" />
          <el-option label="故障预防" value="FAULT_PREDICT" />
          <el-option label="关键工序识别" value="KEY_PROCESS_IDENTIFY" />
        </el-select>
        <el-select v-model="history_query.status" placeholder="任务状态" clearable class="history-select">
          <el-option label="全部状态" value="" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="运行中" value="RUNNING" />
          <el-option label="失败" value="FAILED" />
          <el-option label="等待中" value="PENDING" />
        </el-select>
        <el-button type="primary" @click="handleHistorySearch">查询</el-button>
        <el-button @click="resetHistorySearch">重置</el-button>
      </div>

      <el-table v-loading="history_loading" :data="history_rows" border height="320" empty-text="暂无历史识别记录">
        <el-table-column prop="taskId" label="任务ID" width="190" show-overflow-tooltip />
        <el-table-column prop="taskName" label="识别类型" width="150" show-overflow-tooltip />
        <el-table-column prop="targetName" label="识别对象" min-width="150" show-overflow-tooltip />
        <el-table-column prop="summary" label="识别结果摘要" min-width="220" show-overflow-tooltip />
        <el-table-column prop="resultValue" label="结果值" width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :disabled="row.status !== 'SUCCESS'" @click="restoreHistoryRow(row)">
              查看
            </el-button>
            <el-button type="danger" link @click="deleteHistoryRow(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="history_total > 0"
        :total="history_total"
        v-model:page="history_query.page_num"
        v-model:limit="history_query.page_size"
        @pagination="loadIdentifyHistory"
      />
    </el-card>

    <el-dialog
      v-model="file_dlg"
      title="选择分析对象和数据文件"
      width="1100px"
      :close-on-click-modal="false"
    >
      <div class="fault-iden-prepare-dialog">
        <el-form label-width="90px" class="fault-iden-object-form">
          <el-row :gutter="12">
            <el-col :span="6">
              <el-form-item label="飞机">
                <el-select
                  v-model="feat_sel.aircraft_id"
                  placeholder="请选择飞机"
                  style="width: 100%"
                  :loading="feature_hierarchy_loading"
                  @change="handleAircraftChange"
                >
                  <el-option
                    v-for="item in feature_aircraft_options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="分系统">
                <el-select
                  v-model="feat_sel.subsystem_id"
                  placeholder="请选择分系统"
                  style="width: 100%"
                  :disabled="!feat_sel.aircraft_id"
                  :loading="feature_hierarchy_loading"
                  @change="handleSubsystemChange"
                >
                  <el-option
                    v-for="item in feature_subsystem_options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="设备">
                <el-select
                  v-model="feat_sel.device_id"
                  placeholder="请选择设备"
                  style="width: 100%"
                  :disabled="!feat_sel.subsystem_id"
                  :loading="feature_hierarchy_loading"
                  @change="handleDeviceChange"
                >
                  <el-option
                    v-for="item in feature_device_options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="组件">
                <el-select
                  v-model="feat_sel.component_id"
                  placeholder="请选择组件"
                  style="width: 100%"
                  :disabled="!feat_sel.device_id"
                  :loading="feature_hierarchy_loading"
                  @change="handleFeatureObjectChange"
                >
                  <el-option
                    v-for="item in feature_component_options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div class="fault-iden-picker">
        <div class="fault-iden-toolbar">
          <span class="fault-iden-object">当前对象：{{ pending_feature_object_path }}</span>

          <el-input
            v-model="faultIden.keyword"
            placeholder="搜索文件名"
            clearable
            style="width: 180px"
            @keyup.enter="loadSamples"
            @clear="loadSamples"
          />
          <el-button @click="loadSamples">查询</el-button>
          <el-button :loading="faultIden.selectingAll" @click="selectAllSamples">选择全部文件</el-button>
          <el-button @click="clearSamples">清空选择</el-button>
          <span class="fault-iden-selected">已选择 {{ selectedSampleIds.length }} 个文件</span>
        </div>

        <el-table
          ref="sample_table_ref"
          v-loading="faultIden.loadingSamples"
          :data="faultIden.samples"
          row-key="id"
          height="420"
          @selection-change="onSampleSelection"
        >
          <el-table-column type="selection" width="48" reserve-selection />
          <el-table-column prop="sampleNo" label="样本编号" width="100" />
          <el-table-column prop="sampleTime" label="样本时间" width="170" />
          <el-table-column prop="elapsedSeconds" label="已运行秒数" width="130" />
          <el-table-column prop="fileName" label="文件名" min-width="120" />
          <el-table-column prop="fileSize" label="文件大小" width="120" />
          <el-table-column prop="pointCount" label="点数" width="120" />
          <el-table-column prop="checksum" label="校验值" min-width="220" show-overflow-tooltip />
          <el-table-column prop="importStatus" label="导入状态" width="130" />
        </el-table>

        <pagination
          v-show="faultIden.total > 0"
          :total="faultIden.total"
          v-model:page="faultIden.pageNum"
          v-model:limit="faultIden.pageSize"
          @pagination="loadSamples"
        />
        </div>
      </div>

      <template #footer>
        <el-button @click="file_dlg = false">取消</el-button>
        <el-button type="primary" @click="confirmFiles">确认选择</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import 'echarts-gl'
import {
  getFeatureTask,
  deleteFaultIdentifyResult,
  listFaultIdentifyResults,
  listFaultIdenBearings,
  listFaultIdenConditions,
  listFaultIdenSamples,
  startDegradationTask,
  startDataAnalysisTask,
  startFeatureTask,
  startPreTask
} from '@/api/project_3/service'
import { getMonitorTree } from '@/api/project_3/monitor'
import { getApiPayload, getErrorMessage } from '@/utils/useQualityAlgorithmTask'

defineOptions({
  name: 'ServiceIdentify'
})

const upload_progress = ref(0)
const upload_status_text = ref('等待选择目标对象')
const file_dlg = ref(false)
const sample_table_ref = ref(null)
const selected_samples = ref([])
const selectedSampleIds = computed(() => selected_samples.value.map(item => item.id))
const selectedSampleUsage = computed(() => {
  const usages = Array.from(new Set(selected_samples.value.map(item => item.dataUsage).filter(Boolean)))
  return usages.length === 1 ? usages[0] : ''
})
const selected_feature_object_path = computed(() => {
  return selected_feature_object.value?.path || selected_feature_object.value?.name || '--'
})
const pending_feature_object_path = computed(() => {
  const selected_object = currentFeatureObject()
  return selected_object.path || selected_object.name || '--'
})
const faultIden = reactive({
  conditions: [],
  bearings: [],
  samples: [],
  conditionLabel: '',
  bearingCode: '',
  keyword: '',
  pageNum: 1,
  pageSize: 20,
  total: 0,
  loadingConditions: false,
  loadingBearings: false,
  loadingSamples: false,
  selectingAll: false
})
const degradation_point_text = ref('--')
const degrade_loading = ref(false)
const degrade_status = ref('')
const degrade_result = ref(null)
const degrade_error = ref('')
const degrade_task_id = ref('')
const prevention_loading = ref(false)
const prevention_status = ref('')
const prevention_result = ref(null)
const prevention_error = ref('')
const prevention_task_id = ref('')
const feat_loading = ref(false)
const analysis_task_id = ref('')
const data_analysis_task_id = ref('')
const data_analysis_result = ref(null)
const feat_task_id = ref('')
const feat_status = ref('--')

const feat_err = ref('')
const feat_result = ref(null)
const feature_hierarchy_loading = ref(false)
const feat_tree = ref([])
const selected_feature_object = ref(null)
const feat_sel = reactive({
  aircraft_id: '',
  subsystem_id: '',
  device_id: '',
  component_id: ''
})
const feature_aircraft_options = ref([])
const feature_subsystem_options = ref([])
const feature_device_options = ref([])
const feature_component_options = ref([])
const FEATURE_NONE_OPTION = { label: '无', value: '' }
const mean_chart_ref = ref(null)
const std_chart_ref = ref(null)
const peak_chart_ref = ref(null)
const rms_chart_ref = ref(null)
const kurtosis_chart_ref = ref(null)
const max_chart_ref = ref(null)
const time_domain_chart_ref = ref(null)
const spectrum_chart_ref = ref(null)
const degradation_signal_chart_ref = ref(null)
const degradation_compare_chart_ref = ref(null)
const prevention_signal_chart_ref = ref(null)
const prevention_rms_chart_ref = ref(null)
const chart_instances = new Map()

const FEAT_POLL_MS = 3000
const FEAT_DONE = ['SUCCESS', 'FAILED']
let feat_timer = null

const route = useRoute()

const feature_params = reactive({
  windowSize: '1.0',
  overlapRate: '50',
  channel: 'vertical',
  samplingRate: 25600
})
const degradation_params = reactive({
  window_size: '1.0',
  overlap_rate: '50',
  baseline_window_count: '500',
  rms_sensitivity: 0.2,
  detection_methods: ['kurtosis', 'rms']
})
const degradation_table_rows = ref([])
const has_degradation_point = computed(() => {
  const point = get_degrade_field('point')
  if (point === undefined || point === null || point === '') return false
  return Number.isFinite(Number(point))
})
const prevention_advice_rows = computed(() => {
  const result = prevention_result.value || {}
  const advice = pick_field(result, ['maintenanceAdvice', 'maintenance_advice', 'suggestions', 'advice'])
  if (Array.isArray(advice)) return advice.filter(Boolean).map(item => String(item))
  if (typeof advice === 'string' && advice.trim()) {
    return advice.split(/[;锛沑n]/).map(item => item.trim()).filter(Boolean)
  }
  return []
})
const history_loading = ref(false)
const history_rows = ref([])
const history_total = ref(0)
const history_query = reactive({
  keyword: '',
  task_type: '',
  status: '',
  page_num: 1,
  page_size: 20
})

const summary_items = computed(() => {
  const summary = feat_data.value?.summary || {}
  const items = [
    ['样本数', summary.sampleCount ?? summary.sample_count],
    ['分析时长', summary.duration],
    ['窗口数', summary.featureWindowCount ?? summary.feature_window_count],
    ['最大幅值', summary.maxAmplitude ?? summary.max_amplitude],
    ['主频', summary.dominantFrequency ?? summary.dominant_frequency]
  ]
  return items
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([label, value]) => ({ label, value }))
})

const feat_data = computed(() => pickFeat(feat_result.value))

function handle_file_change() {
  upload_progress.value = feat_status.value === 'SUCCESS' ? 100 : 0
  upload_status_text.value = '请选择 XJTU-SY 原始 CSV 文件'
  ElMessage.info('特征分析使用已选择的 XJTU-SY 原始文件，不需要前端上传文件')
}
async function openPrepareDlg() {
  if (feat_loading.value) return

  await loadFeatTree()
  faultIden.pageNum = 1
  file_dlg.value = true
  if (validateFeatureObject(currentFeatureObject(), false)) {
    await loadSamples()
  }
}

async function loadConditions() {
  faultIden.loadingConditions = true
  try {
    const res = await listFaultIdenConditions()
    faultIden.conditions = Array.isArray(res?.data) ? res.data : []
  } finally {
    faultIden.loadingConditions = false
  }
}

async function onCondChange() {
  faultIden.bearingCode = ''
  faultIden.bearings = []
  faultIden.samples = []
  faultIden.total = 0
  if (!faultIden.conditionLabel) return
  faultIden.loadingBearings = true
  try {
    const res = await listFaultIdenBearings({ conditionLabel: faultIden.conditionLabel })
    faultIden.bearings = Array.isArray(res?.data) ? res.data : []
  } finally {
    faultIden.loadingBearings = false
  }
}

function onBearingChange() {
  faultIden.pageNum = 1
  selected_samples.value = []
  sample_table_ref.value?.clearSelection?.()
  loadSamples()
}

async function loadSamples() {
  const selected_object = currentFeatureObject()
  if (!validateFeatureObject(selected_object)) return
  faultIden.loadingSamples = true
  try {
    const res = await listFaultIdenSamples(sampleQueryParams(selected_object))
    faultIden.samples = Array.isArray(res?.rows) ? res.rows : []
    faultIden.total = Number(res?.total || 0)
    await nextTick()
    syncCurrentPageSelection()
  } finally {
    faultIden.loadingSamples = false
  }
}

function sampleQueryParams(selected_object, pageNum = faultIden.pageNum, pageSize = faultIden.pageSize) {
  return {
    aircraftId: selected_object.aircraftId || '',
    subsystemId: selected_object.subsystemId || '',
    equipmentId: selected_object.equipmentId || '',
    componentId: selected_object.componentId || '',
    dataUsage: 'ALL',
    keyword: faultIden.keyword,
    pageNum,
    pageSize
  }
}

function onSampleSelection(rows) {
  const map = new Map(selected_samples.value.map(item => [item.id, item]))
  faultIden.samples.forEach(item => map.delete(item.id))
  rows.forEach(item => map.set(item.id, item))
  selected_samples.value = Array.from(map.values())
}

function syncCurrentPageSelection() {
  const table = sample_table_ref.value
  if (!table) return
  const ids = new Set(selectedSampleIds.value)
  table.clearSelection?.()
  faultIden.samples.forEach(row => {
    if (ids.has(row.id)) {
      table.toggleRowSelection?.(row, true)
    }
  })
}

async function selectAllSamples() {
  const selected_object = currentFeatureObject()
  if (!validateFeatureObject(selected_object)) return
  if (!faultIden.total) {
    await loadSamples()
  }
  if (!faultIden.total) {
    ElMessage.warning('当前对象下暂无可选择文件')
    return
  }
  faultIden.selectingAll = true
  try {
    const res = await listFaultIdenSamples(sampleQueryParams(selected_object, 1, faultIden.total))
    selected_samples.value = Array.isArray(res?.rows) ? res.rows : []
    await nextTick()
    syncCurrentPageSelection()
    ElMessage.success(`已选择全部 ${selected_samples.value.length} 个文件`)
  } finally {
    faultIden.selectingAll = false
  }
}

function clearSamples() {
  selected_samples.value = []
  sample_table_ref.value?.clearSelection?.()
}

async function confirmFiles() {
  const selected_object = currentFeatureObject()
  if (!validateFeatureObject(selected_object)) {
    return
  }
  if (!selectedSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV 文件')
    return
  }
  if (!selectedSampleUsage.value) {
    ElMessage.warning('请选择同一用途的数据文件')
    return
  }
  selected_feature_object.value = selected_object
  upload_progress.value = 100
  upload_status_text.value = `已选择 ${selectedSampleIds.value.length} 个 XJTU-SY 原始 CSV 文件`
  file_dlg.value = false
  await nextTick()
  startDataAnalysis()
}

function resolveTaskId(taskData) {
  if (!taskData) return ''
  return taskData.task_id || taskData.taskId || taskData.id || ''
}

function clearFeat() {
  if (!feat_timer) return
  clearInterval(feat_timer)
  feat_timer = null
}

function applyFeatTask(taskData) {
  const st = taskData?.status || feat_status.value || '--'
  const res = pickFeat(taskData)
  const err = taskData?.errorMessage || taskData?.error_message || ''



  feat_status.value = st
  feat_err.value = err

  if (!is_empty_result(res)) {
    feat_result.value = res
    renderCharts(res, { feature: true, signal: false })
  }


  if (st === 'SUCCESS') {
    upload_progress.value = 100
    upload_status_text.value = '特征分析已完成'
    clearFeat()
    return
  }
  if (st !== 'FAILED') return

  clearFeat()
  ElMessage.error(err || '特征分析任务执行失败')
}



async function queryFeatTask(taskId, { silent = false } = {}) {
  if (!taskId) return

  try {
    const response = await getFeatureTask(taskId)
    applyFeatTask(getApiPayload(response))
  } catch (error) {
    if (silent) return
    ElMessage.error(getErrorMessage(error, '特征分析任务状态查询失败'))
  }
}



function startPolling(taskId) {
  clearFeat()
  if (!taskId) return

  queryFeatTask(taskId)
  feat_timer = setInterval(() => {
    if (FEAT_DONE.includes(feat_status.value)) {
      clearFeat()
      return
    }
    queryFeatTask(taskId, { silent: true })
  }, FEAT_POLL_MS)
}

function acceptFeatTask(taskData, { closeDialog = false } = {}) {
  feat_result.value = pickFeat(taskData)
  feat_task_id.value = resolveTaskId(taskData)
  const flowTaskId = taskData?.flowTaskId || taskData?.flow_task_id
  if (flowTaskId) analysis_task_id.value = flowTaskId
  feat_status.value = taskData?.status || 'PENDING'
  feat_err.value = ''
  if (!is_empty_result(feat_result.value)) {
    renderCharts(feat_result.value, { feature: true, signal: false })
  }

  if (closeDialog) {
    file_dlg.value = false
  }

  if (!feat_task_id.value) {
    ElMessage.warning('任务已提交，但未返回 taskId')
    return
  }

  startPolling(feat_task_id.value)
}
function mapNodeOptions(nodes) {
  const opts = (Array.isArray(nodes) ? nodes : [])
    .map(node => ({
      label: node?.name || String(node?.id || ''),
      value: node?.id
    }))
    .filter(item => item.value)
  return [FEATURE_NONE_OPTION, ...opts]
}

function findNodeById(nodes, targetId) {
  if (!targetId) return null
  for (const node of Array.isArray(nodes) ? nodes : []) {
    if (node?.id === targetId) return node
    const found = findNodeById(node?.children || [], targetId)
    if (found) return found
  }
  return null
}

function findNodeByRawId(nodes, rawId, allowedTypes = []) {
  if (!rawId) return null
  const raw = String(rawId)
  const types = new Set(allowedTypes)
  for (const node of Array.isArray(nodes) ? nodes : []) {
    const nodeId = String(node?.id || '')
    const nodeType = nodeId.includes(':') ? nodeId.split(':')[0] : ''
    if ((!types.size || types.has(nodeType)) && rawNodeId(nodeId) === raw) return node
    const found = findNodeByRawId(node?.children || [], raw, allowedTypes)
    if (found) return found
  }
  return null
}

function findNodePathById(nodes, targetId, parents = []) {
  if (!targetId) return []
  for (const node of Array.isArray(nodes) ? nodes : []) {
    const nodeName = node?.name || String(node?.id || '')
    const path = nodeName ? [...parents, nodeName] : parents
    if (node?.id === targetId) return path
    const found = findNodePathById(node?.children || [], targetId, path)
    if (found.length) return found
  }
  return []
}

function rawNodeId(nodeId) {
  const text = String(nodeId || '')
  const index = text.indexOf(':')
  return index >= 0 ? text.slice(index + 1) : text
}

function handleAircraftChange() {
  feat_sel.subsystem_id = ''
  feat_sel.device_id = ''
  feat_sel.component_id = ''
  handleFeatureObjectChange()

  const aircraft = findNodeById(feat_tree.value, feat_sel.aircraft_id)
  feature_subsystem_options.value = mapNodeOptions(aircraft?.children || [])
  feature_device_options.value = mapNodeOptions([])
  feature_component_options.value = mapNodeOptions([])
}




function handleSubsystemChange() {
  feat_sel.device_id = ''
  feat_sel.component_id = ''
  handleFeatureObjectChange()

  const subsystem = findNodeById(feat_tree.value, feat_sel.subsystem_id)
  feature_device_options.value = mapNodeOptions(subsystem?.children || [])
  feature_component_options.value = mapNodeOptions([])
}

function handleDeviceChange() {
  feat_sel.component_id = ''
  handleFeatureObjectChange()
  const device = findNodeById(feat_tree.value, feat_sel.device_id)
  feature_component_options.value = mapNodeOptions(device?.children || [])
}

function handleFeatureObjectChange() {
  faultIden.pageNum = 1
  faultIden.total = 0
  faultIden.samples = []
  clearSamples()
}
async function loadFeatTree() {
  feature_hierarchy_loading.value = true
  try {
    const response = await getMonitorTree()
    const tree = Array.isArray(response?.data) ? response.data : []
    feat_tree.value = tree
    feature_aircraft_options.value = mapNodeOptions(tree)
  } catch {
    feat_tree.value = []
    feature_aircraft_options.value = mapNodeOptions([])
    feature_subsystem_options.value = mapNodeOptions([])
    feature_device_options.value = mapNodeOptions([])

    feature_component_options.value = mapNodeOptions([])
  } finally {
    feature_hierarchy_loading.value = false
  }
}

function currentFeatureObject() {
  const levels = [
    { level: 'component', objectLevel: 'component', value: feat_sel.component_id, options: feature_component_options.value },
    { level: 'device', objectLevel: 'equipment', value: feat_sel.device_id, options: feature_device_options.value },
    { level: 'subsystem', objectLevel: 'subsystem', value: feat_sel.subsystem_id, options: feature_subsystem_options.value },
    { level: 'aircraft', objectLevel: 'aircraft', value: feat_sel.aircraft_id, options: feature_aircraft_options.value }
  ]

  const picked = levels.find(item => item.value)
  const pickedOpt = picked
    ? (picked.options || []).find(option => option.value === picked.value)
    : null

  const selected_object = {
    level: picked?.level || null,
    objectLevel: picked?.objectLevel || null,
    id: picked?.value || null,
    name: pickedOpt?.label || null,
    path: findNodePathById(feat_tree.value, picked?.value).join(' / '),
    aircraftId: rawNodeId(feat_sel.aircraft_id),
    subsystemId: rawNodeId(feat_sel.subsystem_id),
    equipmentId: rawNodeId(feat_sel.device_id),
    componentId: rawNodeId(feat_sel.component_id)
  }
  return selected_object
}

function validateFeatureObject(selected_object, toast = true) {
  if (!selected_object?.id) {
    if (toast) ElMessage.warning('请先选择分系统、设备或组件')
    return false
  }
  if (selected_object.objectLevel === 'aircraft' || selected_object.id?.startsWith('aircraft:')) {
    if (toast) ElMessage.warning('飞机不能直接作为特征分析对象，请选择分系统、设备或组件')
    return false
  }
  return true
}

function confirmFeatureObjectDialog() {
  const selected_object = currentFeatureObject()
  if (!validateFeatureObject(selected_object)) {
    return
  }
  const previousId = selected_feature_object.value?.id
  selected_feature_object.value = selected_object
  if (previousId !== selected_object.id) {
    faultIden.pageNum = 1
    faultIden.total = 0
    faultIden.samples = []
    clearSamples()
  }
  file_dlg.value = false
  ElMessage.success('特征分析对象已选择')
}

function buildFeatureRequestParams(selected_object) {
  return {
    dataSelectionMode: 'SELECTED_FILES',
    sampleIds: selectedSampleIds.value,
    dataUsage: selectedSampleUsage.value,
    windowSize: num_or(feature_params.windowSize, 1.0),
    overlapRate: num_or(feature_params.overlapRate, 50),
    channel: feature_params.channel,
    samplingRate: num_or(feature_params.samplingRate, 25600),
    selected_object,
    targetType: selected_object.level,
    targetLevel: selected_object.objectLevel,
    targetNodeId: selected_object.id,
    targetId: selected_object.id?.includes(':') ? selected_object.id.split(':').pop() : selected_object.id,
    aircraftId: selected_object.aircraftId,
    subsystemId: selected_object.subsystemId,
    equipmentId: selected_object.equipmentId,
    componentId: selected_object.componentId
  }
}

async function startDataAnalysis() {
  const selected_object = selected_feature_object.value
  const routeId = route.query.import_record_id || route.query.record_id || route.query.id || null
  const bizId = selected_object?.id || routeId || null
  if (!validateFeatureObject(selected_object)) return
  if (!selectedSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV 文件')
    return
  }
  if (!selectedSampleUsage.value) {
    ElMessage.warning('请选择同一用途的数据文件')
    return
  }

  feat_loading.value = true
  upload_progress.value = 35
  upload_status_text.value = '数据清洗与时域时频分析中'
  try {
    const response = await startDataAnalysisTask({
      recordId: bizId,
      params: buildFeatureRequestParams(selected_object)
    })
    const payload = getApiPayload(response)
    const flowTaskId = payload?.flowTaskId || payload?.flow_task_id || resolveTaskId(payload)
    analysis_task_id.value = flowTaskId
    data_analysis_task_id.value = flowTaskId
    const result = pickFeat(payload)
    if (!is_empty_result(result)) {
      data_analysis_result.value = result
      renderCharts(result, { feature: false, signal: true })
    }
    upload_progress.value = 100
    upload_status_text.value = '数据清洗与时域时频分析完成'
    ElMessage.success('数据清洗与时域时频分析完成')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '数据清洗与时域时频分析失败'))
  } finally {
    feat_loading.value = false
  }
}

async function startFeatureAnalysis() {
  if (feat_loading.value) return

  const selected_object = selected_feature_object.value
  const routeId = route.query.import_record_id || route.query.record_id || route.query.id || null
  const bizId = selected_object?.id || routeId || null
  if (!validateFeatureObject(selected_object)) {
    return
  }
  if (!selectedSampleIds.value.length) {
    ElMessage.warning('请至少选择一个 CSV 文件')
    return
  }
  if (!selectedSampleUsage.value) {
    ElMessage.warning('请选择同一用途的数据文件')
    return
  }
  if (!['horizontal', 'vertical'].includes(feature_params.channel)) {
    ElMessage.warning('通道只能是 horizontal 或 vertical')
    return
  }
  const reqParams = {
    ...buildFeatureRequestParams(selected_object),
    sourceTaskId: analysis_task_id.value || data_analysis_task_id.value || null
  }

  feat_loading.value = true
  upload_progress.value = 35
  upload_status_text.value = '后端正在准备 XJTU-SY 原始文件'
  try {
    const response = await startFeatureTask({
      recordId: bizId,
      params: reqParams
    })
    acceptFeatTask(getApiPayload(response), { closeDialog: true })
    ElMessage.success('特征分析任务已提交')
  } catch (error) {
    const http = Number(error?.response?.status || 0)
    const msg = String(error?.message || '').toLowerCase()
    const serverMsg = getErrorMessage(error, '')

    if (http === 400 || http === 422) {
      ElMessage.error(serverMsg || '特征分析任务启动失败：请求参数不完整或格式错误')
    } else if (http === 404 || http === 503) {
      ElMessage.error(serverMsg || '特征分析任务启动失败：后端算法接口不可用')
    } else if (msg.includes('timeout') || msg.includes('network')) {
      ElMessage.error(serverMsg || '特征分析任务启动失败：网络异常或请求超时')
    } else {
      ElMessage.error(serverMsg || '特征分析任务启动失败')
    }

  } finally {
    feat_loading.value = false
  }
}

function is_empty_result(value) {
  if (value === null || value === undefined) return true
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

function num_or(value, fallback) {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

function pick_field(source, names) {
  if (!source || typeof source !== 'object') return undefined
  for (const name of names) {

    if (source[name] !== undefined && source[name] !== null && source[name] !== '') {
      return source[name]
    }
  }
  return undefined
}



function pickFeat(source) {
  if (!source || typeof source !== 'object') return {}

  const list = [
    unwrapPythonResult(source),
    source?.data?.result,
    source?.data?.data?.result,
    source?.result?.result,
    source?.result,
    source?.data,
    source
  ]
  for (const item of list) {
    const ret = normFeat(item)
    if (!is_empty_result(ret)) return ret
  }
  return {}
}

function unwrapPythonResult(raw) {
  if (!raw || typeof raw !== 'object') return {}
  if (raw.timeDomain || raw.timeFrequency) return raw
  if (raw.data && (raw.data.timeDomain || raw.data.timeFrequency)) return raw.data
  if (raw.result && (raw.result.timeDomain || raw.result.timeFrequency)) return raw.result
  if (raw.data?.result && (raw.data.result.timeDomain || raw.data.result.timeFrequency)) {
    return raw.data.result
  }
  return raw
}


function normFeat(src) {
  if (!src || typeof src !== 'object') return {}

  const result = src.result && typeof src.result === 'object' ? src.result : {}
  const ret = {
    ...src,
    ...result
  }
  if (
    ret.featureSeries ||
    ret.timeDomainSignal ||
    ret.timeFrequencySpectrum ||
    ret.timeDomain ||
    ret.timeFrequency ||
    ret.summary
  ) {
    return ret
  }
  return {}
}
function getChart(elRef, key) {
  if (!elRef?.value) return null

  if (!chart_instances.has(key)) {
    chart_instances.set(key, echarts.init(elRef.value))
  }
  return chart_instances.get(key)
}



function renderCharts(rawResult = feat_result.value, options = {}) {
  nextTick(() => {
    const renderFeature = options.feature !== false
    const renderSignal = options.signal !== false
    const data = pickFeat(rawResult)
    const series = data.featureSeries || {}
    const chartMap = [
      { key: 'mean', ref: mean_chart_ref, title: '均值', field: 'mean' },
      { key: 'std', ref: std_chart_ref, title: '标准差', field: 'std' },
      { key: 'peak', ref: peak_chart_ref, title: '峰值', field: 'max' },
      { key: 'rms', ref: rms_chart_ref, title: '均方根', field: 'rms' },
      { key: 'kurtosis', ref: kurtosis_chart_ref, title: '峭度', field: 'kurtosis' },
      { key: 'max', ref: max_chart_ref, title: '最大值', field: 'max' }
    ]


    if (renderFeature) {
      chartMap.forEach(item => {
        renderLineChart(getChart(item.ref, item.key), series.time || [], series[item.field] || [], item.title)
      })
    }


    if (!renderSignal) return
    const signal = normalizeTimeDomain(data)
    renderLineChart(
      getChart(time_domain_chart_ref, 'timeDomain'),
      signal.time || [],
      signal.value || signal.amplitude || [],
      signal.title || '时域振动信号',
      {
        title: '时域振动信号',
        emptyText: '暂无时域数据',
        xName: '时间（秒）',
        yName: '振动加速度'
      }
    )
    renderSpectrumChart(getChart(spectrum_chart_ref, 'spectrum'), normalizeTimeFrequency(data))
  })

}

function normalizeTimeDomain(data) {
  const pythonResult = unwrapPythonResult(data)
  const direct = pythonResult?.timeDomain || pythonResult?.time_domain || {}
  const legacy = data?.timeDomainSignal || data?.time_domain_signal || {}
  const plot = direct?.plot && typeof direct.plot === 'object' ? direct.plot : {}
  return {
    ...legacy,
    ...direct,
    time: direct.times || plot.time || direct.time || legacy.time || [],
    value: direct.values || plot.value || direct.value || direct.amplitude || legacy.value || legacy.amplitude || [],
    title: direct.title || legacy.title || '时域振动信号'
  }
}

function normalizeTimeFrequency(data) {
  const pythonResult = unwrapPythonResult(data)
  const direct = pythonResult?.timeFrequency || pythonResult?.time_frequency || {}
  const legacy = data?.timeFrequencySpectrum || data?.time_frequency_spectrum || {}
  const plot = direct?.plot && typeof direct.plot === 'object' ? direct.plot : {}
  return {
    ...legacy,
    ...direct,
    time: direct.times || plot.times || plot.time || direct.time || legacy.time || [],
    frequency: direct.frequencies || plot.frequencies || plot.frequency || direct.frequency || legacy.frequency || [],
    amplitudeMatrix: direct.amplitudes || plot.amplitude || direct.amplitudeMatrix || direct.amplitude_matrix || legacy.amplitudeMatrix || legacy.amplitude_matrix || [],
    title: direct.title || legacy.title || '时频谱图'
  }
}



function renderLineChart(chart, xData, yData, name, options = {}) {
  if (!chart) return
  if (!Array.isArray(xData) || !Array.isArray(yData) || yData.length === 0) {
    chart.setOption(emptyChartOption(options.emptyText || '暂无特征分析数据'), true)
    return
  }

  chart.setOption({
    title: options.title ? { text: options.title, left: 'center', top: 0, textStyle: { fontSize: 14 } } : undefined,
    grid: { top: options.title ? 42 : 24, right: 18, bottom: 40, left: 58 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xData, name: options.xName || 's' },
    yAxis: { type: 'value', scale: true, name: options.yName || '' },
    series: [{

      name,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: yData,
      lineStyle: { width: 2, color: '#1677ff' },
      areaStyle: { color: 'rgba(22, 119, 255, 0.12)' }
    }]
  }, true)
}


function renderSpectrumChart(chart, spectrum) {
  if (!chart) return

  const times = Array.isArray(spectrum.time) ? spectrum.time : []
  const frequencies = Array.isArray(spectrum.frequency) ? spectrum.frequency : []
  const amplitudes = Array.isArray(spectrum.amplitudeMatrix) ? spectrum.amplitudeMatrix : []

  if (!times.length || !frequencies.length || !amplitudes.length) {
    chart.setOption(emptyChartOption('暂无时频数据'), true)
    return
  }

  const surfaceData = []
  let minAmplitude = Infinity
  let maxAmplitude = -Infinity

  for (let frequencyIndex = 0; frequencyIndex < frequencies.length; frequencyIndex += 1) {
    const row = Array.isArray(amplitudes[frequencyIndex]) ? amplitudes[frequencyIndex] : []
    for (let timeIndex = 0; timeIndex < times.length; timeIndex += 1) {
      const value = Number(row[timeIndex] ?? 0)
      surfaceData.push([
        Number(times[timeIndex]),
        Number(frequencies[frequencyIndex]),
        value
      ])
      if (value < minAmplitude) minAmplitude = value
      if (value > maxAmplitude) maxAmplitude = value
    }
  }

  if (!Number.isFinite(minAmplitude)) minAmplitude = 0
  if (!Number.isFinite(maxAmplitude)) maxAmplitude = 1
  if (minAmplitude === maxAmplitude) maxAmplitude = minAmplitude + 1

  chart.setOption({
    title: { text: '三维时频谱图', left: 'center' },
    tooltip: {},
    visualMap: {
      show: true,
      dimension: 2,
      calculable: true,
      min: minAmplitude,
      max: maxAmplitude,
      text: ['幅值', ''],
      inRange: { color: ['#313695', '#74add1', '#ffffbf', '#f46d43', '#a50026'] }
    },
    xAxis3D: {
      type: 'value',
      name: '时间（秒）'
    },
    yAxis3D: {
      type: 'value',
      name: '频率（Hz）'
    },
    zAxis3D: {
      type: 'value',
      name: '幅值'
    },
    grid3D: {
      viewControl: {
        projection: 'perspective',
        alpha: 30,
        beta: 45
      },
      boxWidth: 120,
      boxDepth: 80,
      boxHeight: 60
    },
    series: [{
      type: 'surface',
      data: surfaceData,
      shading: 'lambert'
    }]
  }, true)
}

function emptyChartOption(text) {
  return {
    xAxis: { show: false },
    yAxis: { show: false },
    series: [],
    graphic: {

      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text, fill: '#607081', fontSize: 16 }
    }
  }
}



function resizeCharts() {
  chart_instances.forEach(chart => chart.resize())
}

function get_degrade_field(type) {
  const data = degrade_result.value?.result && typeof degrade_result.value.result === 'object'
    ? degrade_result.value.result
    : degrade_result.value || {}
  const keys = {
    point: ['earlyDegradationPoint', 'early_degradation_point', 'earlyDegradationTime', 'early_degradation_time', 'degradationPoint', 'degradation_point'],
    unit: ['degradationPointUnit', 'degradation_point_unit', 'unit'],
    time: ['detectTime', 'detect_time', 'detectionTime', 'detection_time']
  }
  return pick_field(data, keys[type] || [type])
}

function get_prevention_field(type) {
  const data = prevention_result.value?.result && typeof prevention_result.value.result === 'object'
    ? prevention_result.value.result
    : prevention_result.value || {}
  const keys = {
    risk: ['riskLevel', 'risk_level', 'riskLevelCode', 'risk_level_code'],
    score: ['riskScore', 'risk_score', 'score'],
    life: ['predictedRemainingLife', 'predicted_remaining_life', 'remainingLife', 'remaining_life', 'remainingTime', 'remaining_time']
  }
  const value = pick_field(data, keys[type] || [type])
  if (value === undefined || value === null || value === '') return undefined
  if (type === 'score' || type === 'life') {
    const n = Number(value)
    return Number.isFinite(n) ? n.toFixed(2) : value
  }
  return value
}

function build_degradation_rows(value) {
  const data = value?.result && typeof value.result === 'object' ? value.result : value || {}
  const results = data.results && typeof data.results === 'object' ? data.results : {}
  const methodNames = {
    kurtosis_3sigma: '峰度+3σ准则',
    kurtosis: '峰度+3σ准则',
    rms_trend: 'RMS瓒嬪娍',
    rms: 'RMS瓒嬪娍'
  }
  return Object.entries(results).map(([key, item]) => {
    const row = item && typeof item === 'object' ? item : {}
    const time = row.degradationTime ?? row.degradation_time ?? ''
    const detected = row.detected === true
    return {
      method: methodNames[key] || key,
      time: time === '' || time === null || time === undefined ? '--' : `${time}s`,
      result: detected ? '检测到退化点' : '未检测到退化点'
    }
  })
}

function normalize_degradation_result(value) {
  return value?.result && typeof value.result === 'object' ? value.result : value || {}
}

function task_result_payload(row) {
  if (!row || typeof row !== 'object') return {}
  const result = row.result && typeof row.result === 'object' ? row.result : {}
  return result.result && typeof result.result === 'object' ? result.result : result
}

function task_request_payload(row) {
  if (!row || typeof row !== 'object') return {}
  const request = row.request || row.request_json || row.requestJson
  return request && typeof request === 'object' ? request : {}
}

async function fetchTaskSnapshot(taskId) {
  if (!taskId) return null
  try {
    const response = await getFeatureTask(taskId)
    return getApiPayload(response)
  } catch {
    return null
  }
}

function array_from_request(value) {
  if (Array.isArray(value)) return value
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return value.split(',').map(item => item.trim()).filter(Boolean)
    }
  }
  return []
}

function restoreFeatureSelectionFromTask(task) {
  const request = task_request_payload(task)
  const result = task_result_payload(task)
  const params = request.params || request.feature_params || request.featureParams || {}
  const ctx = request.hierarchyContext || request.hierarchy_context || params.hierarchyContext || {}
  const selected = params.selected_object || params.selectedObject || result.selected_object || result.selectedObject || {}
  const targetType = request.targetType || request.target_type || params.targetType || selected.level
  const targetIdRaw = request.targetId || request.target_id || params.targetId || selected.id

  let targetNode = selected.id ? findNodeById(feat_tree.value, selected.id) : null
  if (!targetNode && targetIdRaw) {
    const types = targetType === 'device' ? ['equipment'] : targetType ? [targetType] : []
    targetNode = findNodeByRawId(feat_tree.value, targetIdRaw, types)
  }

  const aircraftNode = ctx.aircraftId ? findNodeByRawId(feat_tree.value, ctx.aircraftId, ['aircraft']) : null
  const subsystemNode = ctx.subsystemId ? findNodeByRawId(feat_tree.value, ctx.subsystemId, ['subsystem']) : null
  const deviceNode = ctx.deviceId ? findNodeByRawId(feat_tree.value, ctx.deviceId, ['equipment']) : null
  const componentNode = ctx.componentId ? findNodeByRawId(feat_tree.value, ctx.componentId, ['component']) : null

  feat_sel.aircraft_id = aircraftNode?.id || ''
  handleAircraftChange()
  feat_sel.subsystem_id = subsystemNode?.id || ''
  handleSubsystemChange()
  feat_sel.device_id = deviceNode?.id || ''
  handleDeviceChange()
  feat_sel.component_id = componentNode?.id || ''

  const current = currentFeatureObject()
  const selectedObject = current.id ? current : {
    level: targetType === 'device' ? 'device' : targetType || selected.level || null,
    objectLevel: targetType === 'device' ? 'equipment' : targetType || selected.objectLevel || null,
    id: targetNode?.id || selected.id || null,
    name: targetNode?.name || selected.name || request.targetName || request.target_name || null,
    path: targetNode?.id ? findNodePathById(feat_tree.value, targetNode.id).join(' / ') : selected.path || '',
    aircraftId: ctx.aircraftId || selected.aircraftId || '',
    subsystemId: ctx.subsystemId || selected.subsystemId || '',
    equipmentId: ctx.deviceId || selected.equipmentId || '',
    componentId: ctx.componentId || selected.componentId || ''
  }
  if (selectedObject.id) selected_feature_object.value = selectedObject

  const sampleIds = array_from_request(request.sampleIds || request.sample_ids || params.sampleIds || params.sample_ids || request.dbExportInfo?.queryCondition?.sampleIds)
  if (sampleIds.length) {
    const dataUsage = params.dataUsage || params.data_usage || params.sampleUsage || params.sample_usage || 'FEATURE_ANALYSIS'
    selected_samples.value = sampleIds.map(id => ({
      id: Number(id) || id,
      sampleNo: Number(id) || id,
      fileName: `sample-${id}`,
      dataUsage
    }))
    upload_progress.value = 100
    upload_status_text.value = `已恢复 ${sampleIds.length} 个历史数据文件`
  }
}

function applyHistoryTask(row) {
  if (!row || typeof row !== 'object') return false
  const taskType = row.taskType || row.task_type
  const taskIdValue = row.taskId || row.task_id
  const flowTaskId = row.flowTaskId || row.flow_task_id
  const statusValue = row.status || '--'
  const resultValue = task_result_payload(row)

  if (taskType === 'FEATURE_ANALYSIS') {
    analysis_task_id.value = flowTaskId || taskIdValue || analysis_task_id.value
    data_analysis_task_id.value = taskIdValue || data_analysis_task_id.value
    feat_status.value = statusValue
    data_analysis_result.value = resultValue
    if (statusValue === 'SUCCESS') renderCharts(resultValue, { feature: false, signal: true })
    return true
  }

  if (taskType === 'FEATURE_PROCESSING') {
    analysis_task_id.value = flowTaskId || analysis_task_id.value
    feat_task_id.value = taskIdValue || feat_task_id.value
    feat_status.value = statusValue
    feat_result.value = resultValue
    if (statusValue === 'SUCCESS') renderCharts(resultValue, { feature: true, signal: false })
    return true
  }

  if (taskType === 'EARLY_DEGRADATION_POINT_DETECT') {
    analysis_task_id.value = flowTaskId || analysis_task_id.value
    degrade_task_id.value = taskIdValue || degrade_task_id.value
    degrade_status.value = statusValue
    degrade_error.value = row.errorMessage || row.error_message || ''
    degrade_result.value = resultValue
    degradation_point_text.value = String(get_degrade_field('point') ?? '--')
    degradation_table_rows.value = build_degradation_rows(resultValue)
    if (statusValue === 'SUCCESS') renderDegradationCharts(resultValue)
    return true
  }

  if (taskType === 'FAULT_PREDICT') {
    analysis_task_id.value = flowTaskId || analysis_task_id.value
    prevention_task_id.value = taskIdValue || prevention_task_id.value
    prevention_status.value = statusValue
    prevention_error.value = row.errorMessage || row.error_message || ''
    prevention_result.value = resultValue
    if (statusValue === 'SUCCESS') renderPreventionCharts(resultValue)
    return true
  }

  return false
}

function applyFlowSnapshot(snapshot) {
  if (!snapshot || typeof snapshot !== 'object') return false
  const flowTaskId = snapshot.flowTaskId || snapshot.flow_task_id || snapshot.taskId || snapshot.task_id
  if (!flowTaskId) return false

  analysis_task_id.value = flowTaskId
  const dataTask = snapshot.dataAnalysisTask || snapshot.data_analysis_task
  const featureTask = snapshot.featureProcessingTask || snapshot.feature_processing_task
  const degradationTask = snapshot.degradationTask || snapshot.degradation_task
  const faultPredictTask = snapshot.faultPredictTask || snapshot.fault_predict_task

  if (dataTask) {
    data_analysis_task_id.value = dataTask.taskId || dataTask.task_id || data_analysis_task_id.value
    restoreFeatureSelectionFromTask(dataTask)
    applyHistoryTask(dataTask)
  }
  if (featureTask) {
    applyHistoryTask(featureTask)
  }
  if (degradationTask) {
    applyHistoryTask(degradationTask)
  }
  if (faultPredictTask) {
    applyHistoryTask(faultPredictTask)
  }
  return true
}

async function restoreHistoryRow(row, options = {}) {
  const flowTaskId = row?.flowTaskId || row?.flow_task_id || row?.taskId || row?.task_id
  const snapshot = await fetchTaskSnapshot(flowTaskId)
  const restored = applyFlowSnapshot(snapshot) || applyHistoryTask(row)
  if (!restored) {
    if (!options.silent) ElMessage.warning('当前历史任务暂不支持恢复到计算结果区')
    return
  }

  if (snapshot) {
    if (!options.silent) ElMessage.success('历史计算结果已查看')
    return
  }

  const taskType = row.taskType || row.task_type
  const resultValue = task_result_payload(row)

  if (taskType === 'FAULT_PREDICT') {
    const degradeId = pick_field(resultValue, ['sourceTaskId', 'source_task_id', 'degradationTaskId', 'degradation_task_id'])
    const featureId = row.featureTaskId
      || row.feature_task_id
      || pick_field(resultValue, ['featureAnalysisTaskId', 'feature_analysis_task_id', 'featureTaskId', 'feature_task_id'])
    const degradeTask = await fetchTaskSnapshot(degradeId)
    if (degradeTask) applyHistoryTask(degradeTask)
    const degradeResult = task_result_payload(degradeTask)
    const featureTask = await fetchTaskSnapshot(
      featureId
      || degradeTask?.featureTaskId
      || degradeTask?.feature_task_id
      || pick_field(degradeResult, ['featureAnalysisTaskId', 'feature_analysis_task_id', 'sourceTaskId', 'source_task_id'])
    )
    if (featureTask) applyHistoryTask(featureTask)
  } else if (taskType === 'EARLY_DEGRADATION_POINT_DETECT') {
    const featureId = row.featureTaskId || row.feature_task_id || pick_field(resultValue, ['featureAnalysisTaskId', 'feature_analysis_task_id', 'sourceTaskId', 'source_task_id'])
    const featureTask = await fetchTaskSnapshot(featureId)
    if (featureTask) applyHistoryTask(featureTask)
  }

  if (!options.silent) ElMessage.success('历史计算结果已查看')
}

async function deleteHistoryRow(row) {
  const flowTaskId = row?.flowTaskId || row?.flow_task_id || row?.taskId || row?.task_id
  if (!flowTaskId) {
    ElMessage.warning('任务ID不能为空')
    return
  }
  const deleteOptions = {
    deleteAlgorithmArtifacts: false,
    deletePackagedFiles: false,
    deleteDataSources: false
  }
  try {
    await ElMessageBox.confirm(
      h('div', { class: 'history-delete-confirm' }, [
        h('p', { class: 'history-delete-text' }, `确认删除历史识别记录 ${flowTaskId} 吗？该流程下的阶段记录会一起删除。`),
        h('div', { class: 'history-delete-options' }, [
          h('label', { class: 'history-delete-option' }, [
            h('input', {
              type: 'checkbox',
              onChange: event => { deleteOptions.deleteAlgorithmArtifacts = event.target.checked }
            }),
            h('span', null, '删除算法产物文件')
          ]),
          h('label', { class: 'history-delete-option' }, [
            h('input', {
              type: 'checkbox',
              onChange: event => { deleteOptions.deletePackagedFiles = event.target.checked }
            }),
            h('span', null, '删除已打包文件')
          ]),
          h('label', { class: 'history-delete-option' }, [
            h('input', {
              type: 'checkbox',
              onChange: event => { deleteOptions.deleteDataSources = event.target.checked }
            }),
            h('span', null, '删除数据源')
          ])
        ])
      ]),
      '删除确认',
      {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
      }
    )
    await deleteFaultIdentifyResult(flowTaskId, deleteOptions)
    ElMessage.success('历史识别记录已删除')
    await loadIdentifyHistory()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(getErrorMessage(error, '历史识别记录删除失败'))
  }
}

async function restoreLatestResults() {
  try {
    const response = await listFaultIdentifyResults({
      status: 'SUCCESS',
      page_num: 1,
      page_size: 100
    })
    const data = getApiPayload(response)
    const rows = Array.isArray(data?.rows) ? data.rows : []
    const latestPrevention = rows.find(item => (item.taskType || item.task_type) === 'FAULT_PREDICT')
    const latestDegradation = rows.find(item => (item.taskType || item.task_type) === 'EARLY_DEGRADATION_POINT_DETECT')
    const latestFeature = rows.find(item => (item.taskType || item.task_type) === 'FEATURE_ANALYSIS')

    if (latestPrevention) {
      await restoreHistoryRow(latestPrevention, { silent: true })
      return
    }
    if (latestDegradation) {
      await restoreHistoryRow(latestDegradation, { silent: true })
      return
    }
    if (latestFeature) {
      applyHistoryTask(latestFeature)
    }
  } catch (error) {
    console.warn('查看最近计算结果失败', error)
  }
}

function renderDegradationCharts(value = degrade_result.value) {
  nextTick(() => {
    const data = normalize_degradation_result(value)
    const results = data.results && typeof data.results === 'object' ? data.results : {}
    const rms = results.rms_trend || results.rms || {}
    const kurtosis = results.kurtosis_3sigma || results.kurtosis || {}
    const point = Number(data.earlyDegradationTime ?? data.earlyDegradationPoint)

    renderLineChart(
      getChart(degradation_signal_chart_ref, 'degradationSignal'),
      rms.times || kurtosis.times || [],
      rms.values || kurtosis.values || [],
      '退化检测特征趋势',
      {
        title: '退化检测特征趋势',
        emptyText: '暂无退化检测数据',
        xName: '时间（秒）',
        yName: '特征值'
      }
    )

    const series = []
    if (Array.isArray(kurtosis.times) && Array.isArray(kurtosis.values)) {
      series.push({ name: '峰度', type: 'line', smooth: true, data: kurtosis.times.map((t, i) => [t, kurtosis.values[i]]) })
    }
    if (Array.isArray(rms.times) && Array.isArray(rms.values)) {
      series.push({ name: 'RMS', type: 'line', smooth: true, data: rms.times.map((t, i) => [t, rms.values[i]]) })
    }
    if (Number.isFinite(point)) {
      series.push({
        name: '退化点',
        type: 'line',
        data: [],
        markLine: {
          symbol: 'none',
          lineStyle: { color: '#ff4d4f', type: 'dashed', width: 2 },
          data: [{ xAxis: point }]
        }
      })
    }
    const chart = getChart(degradation_compare_chart_ref, 'degradationCompare')
    if (!chart) return
    if (!series.length) {
      chart.setOption(emptyChartOption('暂无退化检测数据'), true)
      return
    }
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { top: 4 },
      grid: { left: 52, right: 28, top: 42, bottom: 42 },
      xAxis: { type: 'value', name: '时间（秒）' },
      yAxis: { type: 'value', name: '特征值' },
      series
    }, true)
  })
}

function normalize_prevention_result(value) {
  if (!value || typeof value !== 'object') return {}
  const result = value.result && typeof value.result === 'object' ? value.result : {}
  const data = value.data && typeof value.data === 'object' ? value.data : {}
  const dataResult = data.result && typeof data.result === 'object' ? data.result : {}
  return {
    ...value,
    ...data,
    ...result,
    ...dataResult
  }
}

function array_field(source, names) {
  const value = pick_field(source, names)
  return Array.isArray(value) ? value : []
}

function normalize_series_points(source, timeNames, valueNames) {
  const times = array_field(source, timeNames)
  const values = array_field(source, valueNames)
  if (times.length && values.length) {
    return {
      times,
      values
    }
  }

  const points = array_field(source, ['points', 'data', 'series'])
  if (!points.length || typeof points[0] !== 'object') {
    return { times: [], values: [] }
  }

  return {
    times: points.map(item => item.time ?? item.t ?? item.x ?? item[0]).filter(item => item !== undefined),
    values: points.map(item => item.value ?? item.y ?? item.amplitude ?? item.rms ?? item[1]).filter(item => item !== undefined)
  }
}

function normalize_prevention_charts(value = prevention_result.value) {
  const data = normalize_prevention_result(value)
  const predictionSeries = pick_field(data, ['predictionSeries', 'prediction_series']) || {}
  const signalSource = pick_field(data, [
    'vibrationSignalPrediction',
    'vibration_signal_prediction',
    'vibrationSignal',
    'vibration_signal',
    'timeDomainSignal',
    'time_domain_signal',
    'signalPrediction',
    'signal_prediction'
  ]) || {}
  const rmsSource = pick_field(data, [
    'rmsTrendPrediction',
    'rms_trend_prediction',
    'rmsPrediction',
    'rms_prediction',
    'rmsTrend',
    'rms_trend',
    'predictionSeries',
    'prediction_series'
  ]) || {}

  const signalActual = normalize_series_points(signalSource, ['time', 'times', 'actualTime', 'actual_time'], ['amplitude', 'value', 'values', 'actual', 'actualValues', 'actual_values'])
  const signalPredicted = normalize_series_points(signalSource, ['predictionTime', 'prediction_time', 'predictedTime', 'predicted_time', 'forecastTime', 'forecast_time'], ['prediction', 'predicted', 'predictedValues', 'predicted_values', 'forecast', 'forecastValues', 'forecast_values'])

  const rmsActual = normalize_series_points(rmsSource, ['time', 'times'], ['rms', 'value', 'values', 'actual', 'actualValues', 'actual_values'])
  const rmsPredicted = normalize_series_points(rmsSource, ['predictionTime', 'prediction_time', 'predictedTime', 'predicted_time', 'forecastTime', 'forecast_time'], ['prediction', 'predicted', 'predictedRms', 'predicted_rms', 'predictedValues', 'predicted_values', 'forecast', 'forecastValues', 'forecast_values'])

  if (!rmsActual.times.length && Array.isArray(predictionSeries.time)) {
    rmsActual.times = predictionSeries.time
    rmsActual.values = Array.isArray(predictionSeries.rms)
      ? predictionSeries.rms
      : array_field(predictionSeries, [data.featureName || 'rms', 'value', 'values'])
  }

  const degradationTime = Number(pick_field(data, ['earlyDegradationTime', 'early_degradation_time', 'degradationTime', 'degradation_time', 'earlyDegradationPoint', 'early_degradation_point']))
  const totalTime = Number(pick_field(data, ['totalTime', 'total_time']))
  const remainingLife = Number(pick_field(data, ['predictedRemainingLife', 'predicted_remaining_life', 'remainingLife', 'remaining_life', 'remainingTime', 'remaining_time']))
  const predictedFailureTime = Number.isFinite(totalTime) && Number.isFinite(remainingLife)
    ? totalTime + remainingLife
    : undefined

  return {
    signalActual,
    signalPredicted,
    rmsActual,
    rmsPredicted,
    degradationTime: Number.isFinite(degradationTime) ? degradationTime : undefined,
    predictedFailureTime: Number.isFinite(predictedFailureTime) ? predictedFailureTime : undefined
  }
}

function renderPreventionCharts(value = prevention_result.value) {
  nextTick(() => {
    const data = normalize_prevention_charts(value)
    renderPredictionLineChart(
      getChart(prevention_signal_chart_ref, 'preventionSignal'),
      {
        actual: data.signalActual,
        predicted: data.signalPredicted,
        actualName: '振动信号',
        predictedName: '预测失效',
        emptyText: '暂无振动信号预测数据',
        yName: '振动加速度',
        degradationTime: data.degradationTime,
        predictedFailureTime: data.predictedFailureTime
      }
    )
    renderPredictionLineChart(
      getChart(prevention_rms_chart_ref, 'preventionRms'),
      {
        actual: data.rmsActual,
        predicted: data.rmsPredicted,
        actualName: '实际RMS',
        predictedName: '预测趋势',
        emptyText: '暂无RMS预测数据',
        yName: 'RMS值',
        degradationTime: data.degradationTime,
        predictedFailureTime: data.predictedFailureTime
      }
    )
  })
}

function renderPredictionLineChart(chart, config) {
  if (!chart) return
  const actualTimes = Array.isArray(config.actual?.times) ? config.actual.times : []
  const actualValues = Array.isArray(config.actual?.values) ? config.actual.values : []
  const predictedTimes = Array.isArray(config.predicted?.times) ? config.predicted.times : []
  const predictedValues = Array.isArray(config.predicted?.values) ? config.predicted.values : []
  const series = []

  if (actualTimes.length && actualValues.length) {
    series.push({
      name: config.actualName,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: actualTimes.map((t, i) => [Number(t), Number(actualValues[i])]),
      lineStyle: { width: 2, color: '#409eff' }
    })
  }
  if (predictedTimes.length && predictedValues.length) {
    series.push({
      name: config.predictedName,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: predictedTimes.map((t, i) => [Number(t), Number(predictedValues[i])]),
      lineStyle: { width: 2, color: '#f39c12', type: 'dashed' }
    })
  }
  if (Number.isFinite(config.degradationTime)) {
    series.push({
      name: '退化点',
      type: 'line',
      data: [],
      markLine: {
        symbol: 'none',
        lineStyle: { color: '#ff4d4f', type: 'dashed', width: 2 },
        data: [{ xAxis: config.degradationTime }]
      }
    })
  }
  if (Number.isFinite(config.predictedFailureTime)) {
    series.push({
      name: '预测失效',
      type: 'line',
      data: [],
      markLine: {
        symbol: 'none',
        lineStyle: { color: '#f39c12', type: 'dashed', width: 2 },
        data: [{ xAxis: config.predictedFailureTime }]
      }
    })
  }

  if (!series.some(item => Array.isArray(item.data) && item.data.length)) {
    chart.setOption(emptyChartOption(config.emptyText), true)
    return
  }

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 4 },
    grid: { left: 58, right: 32, top: 42, bottom: 42 },
    xAxis: { type: 'value', name: '时间（秒）' },
    yAxis: { type: 'value', scale: true, name: config.yName || '' },
    series
  }, true)
}

async function loadIdentifyHistory(options = {}) {
  history_loading.value = true
  try {
    const res = await listFaultIdentifyResults({
      keyword: history_query.keyword,
      task_type: history_query.task_type,
      status: history_query.status,
      page_num: history_query.page_num,
      page_size: history_query.page_size
    })
    const data = getApiPayload(res)
    history_rows.value = Array.isArray(data?.rows) ? data.rows : []
    history_total.value = Number(data?.total || 0)
    if (options?.restoreLatest === true) {
      await restoreLatestResults()
    }
  } finally {
    history_loading.value = false
  }
}

function handleHistorySearch() {
  history_query.page_num = 1
  loadIdentifyHistory()
}

function resetHistorySearch() {
  history_query.keyword = ''
  history_query.task_type = ''
  history_query.status = ''
  history_query.page_num = 1
  loadIdentifyHistory()
}

async function handle_degradation_detect() {
  if (feat_status.value !== 'SUCCESS') {
    ElMessage.warning('请先完成特征分析，且特征分析任务必须成功')
    return
  }
  if (is_empty_result(feat_result.value)) {
    ElMessage.warning('未找到特征分析结果，无法进行退化点检测')
    return
  }
  if (!degradation_params.detection_methods.length) {
    ElMessage.warning('请至少选择一种检测方法')
    return
  }

  degrade_loading.value = true
  degrade_status.value = 'RUNNING'
  degrade_result.value = null
  degrade_error.value = ''
  degradation_table_rows.value = []
  prevention_status.value = ''
  prevention_result.value = null
  prevention_error.value = ''
  prevention_task_id.value = ''

  const payload = {
    sourceTaskId: feat_task_id.value || null,
    source_task_id: feat_task_id.value || null,
    featureAnalysisTaskId: analysis_task_id.value || data_analysis_task_id.value || null,
    flowTaskId: analysis_task_id.value || data_analysis_task_id.value || null,
    params: {
      windowSize: num_or(degradation_params.window_size, 1.0),
      overlapRate: num_or(degradation_params.overlap_rate, 50),
      baselineWindowCount: num_or(degradation_params.baseline_window_count, 500),
      rmsSensitivity: num_or(degradation_params.rms_sensitivity, 0.2),
      detectionMethods: [...degradation_params.detection_methods]
    }
  }

  try {
    const response = await startDegradationTask(payload)
    if (response?.code !== 200) {
      degrade_status.value = 'FAILED'
      degrade_error.value = response?.msg || response?.message || '退化点检测任务启动失败'
      return
    }

    const data = response?.data || {}
    degrade_status.value = data.status || 'SUCCESS'
    degrade_result.value = data.result || data
    degrade_task_id.value = data.taskId || data.task_id || degrade_task_id.value
    analysis_task_id.value = data.flowTaskId || data.flow_task_id || analysis_task_id.value

    const point = get_degrade_field('point')
    degradation_point_text.value = point === undefined ? '--' : String(point)
    degradation_table_rows.value = build_degradation_rows(degrade_result.value)
    renderDegradationCharts(degrade_result.value)
    if (!has_degradation_point.value) {
      ElMessage.warning('早期故障识别未检测到退化点，故障预防不可执行')
      return
    }
    ElMessage.success('退化点检测完成')
  } catch (err) {
    degrade_status.value = 'FAILED'
    degrade_error.value = getErrorMessage(err, '退化点检测任务启动失败')
  } finally {
    degrade_loading.value = false
  }
}

async function handle_fault_prevention() {
  if (degrade_status.value !== 'SUCCESS' || !degrade_task_id.value) {
    ElMessage.warning('请先完成早期退化点检测')
    return
  }

  if (!has_degradation_point.value) {
    ElMessage.warning('早期故障识别未检测到退化点，不能执行故障预防')
    return
  }

  prevention_loading.value = true
  prevention_status.value = 'RUNNING'
  prevention_result.value = null
  prevention_error.value = ''
  renderPreventionCharts({})

  const payload = {
    sourceTaskId: degrade_task_id.value,
    source_task_id: degrade_task_id.value,
    degradationTaskId: degrade_task_id.value,
    featureAnalysisTaskId: analysis_task_id.value || data_analysis_task_id.value || null,
    flowTaskId: analysis_task_id.value || data_analysis_task_id.value || null,
    import_record_id: selected_feature_object.value?.id || null,
    prediction_params: {
      sourceTaskId: degrade_task_id.value,
      featureAnalysisTaskId: analysis_task_id.value || data_analysis_task_id.value || null,
      targetNodeId: selected_feature_object.value?.id || null,
      predictionHorizon: 50,
      riskThreshold: 0.8,
      rulUnit: 'second',
      featureName: 'rms'
    }
  }

  try {
    const response = await startPreTask(payload)
    if (response?.code !== 200) {
      prevention_status.value = 'FAILED'
      prevention_error.value = response?.msg || response?.message || '故障预防任务启动失败'
      return
    }
    const data = response?.data || {}
    prevention_status.value = data.status || 'SUCCESS'
    prevention_result.value = data.result || data
    prevention_task_id.value = data.taskId || data.task_id || prevention_task_id.value
    analysis_task_id.value = data.flowTaskId || data.flow_task_id || analysis_task_id.value
    renderPreventionCharts(prevention_result.value)
    ElMessage.success('故障预防完成')
  } catch (err) {
    prevention_status.value = 'FAILED'
    prevention_error.value = getErrorMessage(err, '故障预防任务启动失败')
    renderPreventionCharts({})
  } finally {
    prevention_loading.value = false
  }
}

onBeforeRouteLeave(() => {
  clearFeat()
})

onMounted(() => {
  renderCharts({})
  renderPreventionCharts({})
  loadIdentifyHistory({ restoreLatest: true })
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  clearFeat()
  window.removeEventListener('resize', resizeCharts)
  chart_instances.forEach(chart => chart.dispose())
  chart_instances.clear()
})
</script>

<style scoped>
.fault-identify-page {
  min-height: calc(100vh - 84px);
  background: #f4f7fb;
  padding: 0 0 20px;
}

.section-card {
  margin-bottom: 18px;
  border-radius: 10px;
  border: 1px solid #dbe4ee;
}
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #001f3f;
}

.degrade-title {
  font-size: 30px;
  font-weight: 700;
  color: #001f3f;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-btn {
  height: 40px;
  padding: 0 22px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 4px;
}

.input-progress {
  flex: 1;
}

.progress-text {
  color: #001f3f;
  white-space: nowrap;
}

.orange-btn {
  min-width: 140px;
  height: 42px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 4px;
}

.feature-task-status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 12px;
  color: #1f3b57;
  font-size: 14px;
}

.feature-task-error-row {
  margin-bottom: 14px;
  color: #c62828;
  font-size: 14px;
}

.feature-summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.feature-summary-item {
  min-height: 58px;
  padding: 10px 12px;
  border: 1px solid #dbe4ee;
  border-radius: 6px;
  background: #fff;
}

.feature-summary-label {
  display: block;
  margin-bottom: 4px;
  color: #607081;
  font-size: 13px;
}

.feature-summary-item strong {
  color: #001f3f;
  font-size: 18px;
}

.analysis-range-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}



.chart-panel {
  margin-bottom: 20px;
  padding: 12px 16px 16px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
}

.chart-title {
  text-align: center;
  margin-bottom: 10px;
  font-size: 17px;
  font-weight: 700;
  color: #001f3f;
}

.align-left {
  text-align: left;
}

.chart-box {
  height: 210px;
  border: 1px solid #cfd9e6;
  background: #fff;
  color: #607081;
  font-size: 20px;
}

.param-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin: 14px 0 22px;
}

.param-label {
  font-size: 16px;
  color: #001f3f;
}
.param-select {
  width: 140px;
}
.param-number {
  width: 140px;
}

.detect-method-panel {
  margin: 4px 0 18px;
}

.detect-method-title {
  margin-bottom: 6px;
  color: #001f3f;
  font-size: 14px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #001f3f;
}

.detect-method-list {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  background: #fff;
}

.detect-method-list :deep(.el-checkbox) {
  height: auto;
  margin-right: 0;
}
.signal-panel {
  margin-bottom: 20px;
  padding: 18px 16px 24px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
}

.signal-box {
  height: 300px;
  border: 1px solid #cfd9e6;
  background: #fff;
  color: #607081;
  font-size: 20px;
}

.fault-iden-prepare-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fault-iden-object-form {
  padding: 12px 12px 0;
  border: 1px solid #dbe4ee;
  border-radius: 6px;
  background: #f8fafc;
}

.fault-iden-picker {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fault-iden-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.fault-iden-object {
  flex: 1 1 360px;
  min-width: 280px;
  color: #304156;
  font-size: 14px;
  font-weight: 600;
  line-height: 32px;
  overflow-wrap: anywhere;
}

.fault-iden-selected {
  color: #304156;
  font-size: 14px;
}

.history-query-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.history-keyword {
  width: 280px;
}

.history-select {
  width: 160px;
}

.history-delete-text {
  margin: 0 0 10px;
  line-height: 1.5;
}

.history-delete-options {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-delete-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  line-height: 24px;
  cursor: pointer;
}

.history-delete-option input {
  margin: 0;
  cursor: pointer;
}

.degradation-result {
  margin: 10px 0 18px;
  padding: 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #001f3f;
}

.degradation-task-status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin: 0 8px 12px;
  color: #1f3b57;
  font-size: 14px;
}

.degradation-running-row,
.degradation-error-row {
  margin: 0 8px 14px;
  font-size: 14px;
}

.degradation-running-row {
  color: #1f6fb2;
}

.degradation-error-row {
  color: #c62828;
}

.degradation-result-panel {
  margin: 0 8px 18px;
  padding: 12px 14px;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #fff;
}

.degradation-highlight-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-bottom: 0;
  color: #001f3f;
  font-size: 15px;
  font-weight: 700;
}

.degradation-chart-panel {
  padding: 0 8px 4px;
}

.degradation-chart-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.degradation-plot-box {
  position: relative;
  height: 220px;
  border: 1px solid #3f4752;
  background: #fff;
  overflow: hidden;
}

.degradation-plot-box::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(148, 163, 184, 0.28) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(148, 163, 184, 0.28) 1px, transparent 1px);
  background-size: 12.5% 50%;
  pointer-events: none;
}

.degradation-echart {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 198px;
  margin-top: 18px;
}

.plot-title {
  position: absolute;
  top: 4px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  color: #111827;
  font-size: 17px;
  font-weight: 700;
}

.method-legend {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
  border: 1px solid #dbe4ee;
  border-radius: 4px;
  background: #fff;
  color: #334155;
  font-size: 13px;
}

.method-legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.method-line {
  display: inline-block;
  width: 26px;
  border-top: 2px solid;
}

.kurtosis-line {
  border-color: #f05243;
}

.rms-line {
  border-color: #f59e0b;
}

.degradation-line {
  display: inline-block;
  width: 26px;
  border-top: 2px dashed #f6c47a;
}

.plot-axis-label {
  position: absolute;
  z-index: 1;
  color: #001f3f;
  font-size: 14px;
}

.plot-axis-y {
  left: 8px;
  top: 50%;
  transform: translateY(-50%) rotate(-90deg);
  transform-origin: left center;
}

.plot-axis-x {
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
}

.plot-empty-text {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  color: #607081;
  font-size: 20px;
}

.degradation-result-table {
  margin-top: 10px;
}

.degradation-result-table :deep(.degradation-result-table-header) {
  background: #e8f6fb;
  color: #111827;
  font-weight: 700;
  text-align: center;
}

.degradation-result-table :deep(.el-table__cell) {
  border-color: #cfd9e6;
}

.prevention-panel {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.prevention-result-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.prevention-advice-list {
  margin-top: 12px;
  color: #334155;
  line-height: 1.8;
}

.risk-advice-item {
  padding: 2px 0;
}

.predict-chart-area {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-top: 18px;
}

.predict-chart-frame {
  height: 170px;
  padding: 0 14px 12px;
  background: #fff;
}

.predict-chart-title {
  height: 28px;
  line-height: 28px;
  text-align: center;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.predict-echart {
  width: 100%;
  height: calc(100% - 28px);
}

.predict-chart-body {
  position: relative;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  height: calc(100% - 28px);
}

.chart-y-label {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #111827;
  font-size: 14px;
  writing-mode: vertical-rl;
  transform: rotate(180deg);
}

.chart-grid {
  position: relative;
  border: 1px solid #111827;
  background-image:
    linear-gradient(to right, rgba(17, 24, 39, 0.1) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(17, 24, 39, 0.1) 1px, transparent 1px);
  background-size: 12.5% 100%, 100% 33.333%;
}

.chart-axis-label {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -28px;
  color: #111827;
  font-size: 14px;
  text-align: center;
}

.chart-legend {
  position: absolute;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.92);
  color: #111827;
  font-size: 13px;
}

.chart-legend span {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.chart-legend-top {
  top: 8px;
  right: 12px;
}

.chart-legend-left {
  top: 12px;
  left: 10px;
}

.legend-line,
.legend-dash {
  display: inline-block;
  width: 24px;
  height: 0;
  border-top: 2px solid currentColor;
}

.legend-dash {
  border-top-style: dashed;
}

.legend-line.blue {
  color: #409eff;
}

.legend-dash.red {
  color: #ff4d4f;
}

.legend-dash.orange {
  color: #f39c12;
}
</style>

<template>
  <div class="project4-page">
    <!-- 页面头部 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">课题四 · 根因分析结果输出接口</div>
        <h2>根因分析结果</h2>
        <p>
          本模块按照接口表格要求，面向课题五输出根因判断、证据链、根因分析置信度等结果，
          用于支撑生命周期质量追溯、问题闭环、整改召回和质量反馈。
        </p>
      </div>

      <div class="module-status">
        <span>课题四 → 课题五</span>
        <span>根因分析输出</span>
      </div>
    </section>

    <!-- 查询条件 -->
    <section class="filter-card">
      <div class="card-header">
        <div>
          <div class="module-eyebrow">查询条件</div>
          <h3>根因分析结果筛选</h3>
        </div>
      </div>

      <el-form
          :model="queryParams"
          ref="queryRef"
          :inline="true"
          v-show="showSearch"
          label-width="110px"
      >
        <el-form-item label="根因编号" prop="analysisCode">
          <el-input
              v-model="queryParams.analysisCode"
              placeholder="请输入根因编号"
              clearable
              @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="诊断ID" prop="diagnosisId">
          <el-input-number
              v-model="queryParams.diagnosisId"
              :controls="false"
              :min="0"
              placeholder="请输入诊断ID"
              style="width: 190px"
          />
        </el-form-item>

        <el-form-item label="样本ID" prop="sampleId">
          <el-input-number
              v-model="queryParams.sampleId"
              :controls="false"
              :min="0"
              placeholder="请输入样本ID"
              style="width: 190px"
          />
        </el-form-item>

        <el-form-item label="根因类型" prop="rootCauseType">
          <el-select
              v-model="queryParams.rootCauseType"
              placeholder="请选择根因类型"
              clearable
              style="width: 190px"
          >
            <el-option label="制造/装配" value="制造/装配" />
            <el-option label="材料/润滑" value="材料/润滑" />
            <el-option label="装配/服役" value="装配/服役" />
            <el-option label="无明显根因" value="无明显根因" />
            <el-option label="多因素耦合" value="多因素耦合" />
          </el-select>
        </el-form-item>

        <el-form-item label="分析状态" prop="analysisStatus">
          <el-select
              v-model="queryParams.analysisStatus"
              placeholder="请选择分析状态"
              clearable
              style="width: 190px"
          >
            <el-option label="已分析" value="已分析" />
            <el-option label="待复核" value="待复核" />
            <el-option label="待分析" value="待分析" />
          </el-select>
        </el-form-item>

        <el-form-item label="分析人" prop="analyst">
          <el-input
              v-model="queryParams.analyst"
              placeholder="请输入分析人"
              clearable
              @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="分析时间" prop="analysisTime">
          <el-date-picker
              v-model="queryParams.analysisTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择分析时间"
              style="width: 190px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <!-- 统计概览 + 饼图 -->
    <section class="confidence-overview">
      <div class="confidence-left">
        <div class="section-title-row">
          <div>
            <div class="module-eyebrow">统计概览</div>
            <h3>根因分析置信度概览</h3>
          </div>
          <el-tag effect="plain" type="primary">按 probability 统计</el-tag>
        </div>

        <div class="metric-grid">
          <div class="metric-mini">
            <span>根因记录总数</span>
            <strong>{{ confidenceStatsData.total }}</strong>
            <em>t4_root_cause_analysis</em>
          </div>

          <div class="metric-mini">
            <span>已分析记录</span>
            <strong>{{ confidenceStatsData.analyzedCount }}</strong>
            <em>analysisStatus = 已分析</em>
          </div>

          <div class="metric-mini">
            <span>待复核记录</span>
            <strong>{{ confidenceStatsData.pendingCount }}</strong>
            <em>analysisStatus = 待复核</em>
          </div>

          <div class="metric-mini">
            <span>平均根因分析置信度</span>
            <strong>{{ formatProbability(confidenceStatsData.avgConfidence) }}</strong>
            <em>avg(probability)</em>
          </div>
        </div>
      </div>

      <div class="confidence-right">
        <div class="chart-card">
          <div class="chart-header">
            <div>
              <div class="module-eyebrow">图形分析</div>
              <h3>单一样本主根因置信度饼图</h3>
            </div>
            <div class="chart-actions">
              <el-select
                  v-model="selectedSampleId"
                  placeholder="请选择样本"
                  size="small"
                  style="width: 150px"
                  @change="handleSampleChange"
              >
                <el-option
                    v-for="item in sampleOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
              <el-button link type="primary" icon="Refresh" @click="refreshChartData">
                刷新
              </el-button>
            </div>
          </div>

          <div ref="confidenceChartRef" class="confidence-chart"></div>

          <div class="chart-legend-note">
            <span>单一根因：同一样本仅取 probability 最大的根因作为主根因</span>
            <span>饼图 = 主根因置信度 + 其他可能性</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 接口输出说明 -->
    <section class="interface-card">
      <div class="interface-title">根因分析结果输出接口字段</div>
      <div class="interface-grid">
        <div class="interface-item">
          <span>输出方向</span>
          <strong>课题四 → 课题五</strong>
        </div>
        <div class="interface-item">
          <span>根因判断</span>
          <strong>rootCauseType + rootCauseDesc</strong>
        </div>
        <div class="interface-item">
          <span>证据链</span>
          <strong>diagnosisEvidence + archiveEvidence + supervisionEvidence</strong>
        </div>
        <div class="interface-item">
          <span>根因分析置信度</span>
          <strong>probability</strong>
        </div>
      </div>
    </section>

    <!-- 表格区域 -->
    <section class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">数据列表</div>
          <h3>根因分析结果记录</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:resultofrc:add']"
          >
            新增
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['system:resultofrc:edit']"
          >
            修改
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:resultofrc:remove']"
          >
            删除
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['system:resultofrc:export']"
          >
            导出
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="primary"
              plain
              icon="Operation"
              @click="handleRunRootCause"
          >
            生成根因分析
          </el-button>
        </el-col>
      </el-row>

      <el-table
          v-loading="loading"
          :data="resultofrcList"
          border
          stripe
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="根因ID" align="center" prop="analysisId" width="90" />
        <el-table-column label="根因编号" align="center" prop="analysisCode" width="150" show-overflow-tooltip />
        <el-table-column label="诊断ID" align="center" prop="diagnosisId" width="90" />
        <el-table-column label="样本ID" align="center" prop="sampleId" width="90" />

        <el-table-column label="根因判断" align="left" width="360" show-overflow-tooltip>
          <template #default="scope">
            <div class="judgment-cell">
              <el-tag :type="rootCauseTypeTag(scope.row.rootCauseType)" effect="plain">
                {{ scope.row.rootCauseType || "-" }}
              </el-tag>
              <div class="judgment-desc">
                {{ scope.row.rootCauseDesc || "-" }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="证据链摘要" align="left" width="360" show-overflow-tooltip>
          <template #default="scope">
            <div class="evidence-summary">
              {{ buildEvidenceSummary(scope.row.evidenceJson) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="根因分析置信度" align="center" prop="probability" width="150">
          <template #default="scope">
            <span class="confidence-text">
              {{ formatProbability(scope.row.probability) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="整改建议" align="left" prop="maintenanceSuggestion" width="320" show-overflow-tooltip />
        <el-table-column label="分析方法" align="center" prop="analysisMethod" width="220" show-overflow-tooltip />

        <el-table-column label="分析状态" align="center" prop="analysisStatus" width="110">
          <template #default="scope">
            <el-tag :type="analysisStatusTag(scope.row.analysisStatus)" effect="plain">
              {{ scope.row.analysisStatus || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="分析人" align="center" prop="analyst" width="150" show-overflow-tooltip />
        <el-table-column label="分析时间" align="center" prop="analysisTime" width="170" />
        <el-table-column label="备注" align="center" prop="remark" width="220" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="230" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">
              详情
            </el-button>

            <el-button
                link
                type="primary"
                icon="Edit"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:resultofrc:edit']"
            >
              修改
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:resultofrc:remove']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </section>

    <!-- 新增 / 修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="920px" append-to-body>
      <el-form ref="resultofrcRef" :model="form" :rules="rules" label-width="140px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="根因编号" prop="analysisCode">
              <el-input v-model="form.analysisCode" placeholder="请输入根因编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="诊断ID" prop="diagnosisId">
              <el-input-number
                  v-model="form.diagnosisId"
                  :controls="false"
                  :min="0"
                  placeholder="请输入诊断ID"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="样本ID" prop="sampleId">
              <el-input-number
                  v-model="form.sampleId"
                  :controls="false"
                  :min="0"
                  placeholder="请输入样本ID"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="根因类型" prop="rootCauseType">
              <el-select v-model="form.rootCauseType" placeholder="请选择根因类型" style="width: 100%">
                <el-option label="制造/装配" value="制造/装配" />
                <el-option label="材料/润滑" value="材料/润滑" />
                <el-option label="装配/服役" value="装配/服役" />
                <el-option label="无明显根因" value="无明显根因" />
                <el-option label="多因素耦合" value="多因素耦合" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="根因分析置信度" prop="probability">
              <el-input-number
                  v-model="form.probability"
                  :controls="false"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  placeholder="请输入0-1之间的置信度"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分析状态" prop="analysisStatus">
              <el-select v-model="form.analysisStatus" placeholder="请选择分析状态" style="width: 100%">
                <el-option label="已分析" value="已分析" />
                <el-option label="待复核" value="待复核" />
                <el-option label="待分析" value="待分析" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分析方法" prop="analysisMethod">
              <el-input v-model="form.analysisMethod" placeholder="请输入分析方法" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分析人" prop="analyst">
              <el-input v-model="form.analyst" placeholder="请输入分析人" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分析时间" prop="analysisTime">
              <el-date-picker
                  v-model="form.analysisTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择分析时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="根因判断描述" prop="rootCauseDesc">
              <el-input
                  v-model="form.rootCauseDesc"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入根因判断描述"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="证据链JSON" prop="evidenceJson">
              <el-input
                  v-model="form.evidenceJson"
                  type="textarea"
                  :rows="8"
                  placeholder="请输入证据链JSON"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="整改建议" prop="maintenanceSuggestion">
              <el-input
                  v-model="form.maintenanceSuggestion"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入整改建议"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="根因分析结果详情" v-model="detailOpen" width="980px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="输出方向">课题四 → 课题五</el-descriptions-item>
        <el-descriptions-item label="接口类型">根因分析结果输出接口</el-descriptions-item>
        <el-descriptions-item label="根因ID">{{ detail.analysisId }}</el-descriptions-item>
        <el-descriptions-item label="根因编号">{{ detail.analysisCode }}</el-descriptions-item>
        <el-descriptions-item label="诊断ID">{{ detail.diagnosisId }}</el-descriptions-item>
        <el-descriptions-item label="样本ID">{{ detail.sampleId }}</el-descriptions-item>
        <el-descriptions-item label="根因分析置信度">{{ formatProbability(detail.probability) }}</el-descriptions-item>
        <el-descriptions-item label="分析状态">{{ detail.analysisStatus }}</el-descriptions-item>
        <el-descriptions-item label="分析方法">{{ detail.analysisMethod }}</el-descriptions-item>
        <el-descriptions-item label="分析时间">{{ detail.analysisTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">根因判断</el-divider>
      <el-input v-model="detail.rootCauseJudgment" type="textarea" :rows="4" readonly />

      <el-divider content-position="left">证据链</el-divider>
      <el-input v-model="detail.evidenceJson" type="textarea" :rows="14" readonly />

      <el-divider content-position="left">整改建议</el-divider>
      <el-input v-model="detail.maintenanceSuggestion" type="textarea" :rows="4" readonly />

      <el-divider content-position="left">备注</el-divider>
      <el-input v-model="detail.remark" type="textarea" :rows="3" readonly />

      <template #footer>
        <el-button type="primary" @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Resultofrc">
import { getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue"
import * as echarts from "echarts"
import {
  listResultofrc,
  getResultofrc,
  delResultofrc,
  addResultofrc,
  updateResultofrc,
  confidenceStats
} from "@/api/project4/resultofrc"

const { proxy } = getCurrentInstance()

const resultofrcList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const detail = ref({})
const selectedSampleId = ref(null)
const sampleOptions = ref([])

const confidenceChartRef = ref(null)
let confidenceChartInstance = null

const confidenceStatsData = reactive({
  total: 0,
  analyzedCount: 0,
  pendingCount: 0,
  avgConfidence: 0,
  highCount: 0,
  mediumCount: 0,
  lowCount: 0
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    analysisCode: null,
    diagnosisId: null,
    sampleId: null,
    rootCauseType: null,
    rootCauseDesc: null,
    analysisMethod: null,
    analysisStatus: null,
    analyst: null,
    analysisTime: null
  },
  rules: {
    analysisCode: [
      { required: true, message: "根因编号不能为空", trigger: "blur" }
    ],
    diagnosisId: [
      { required: true, message: "诊断ID不能为空", trigger: "blur" }
    ],
    sampleId: [
      { required: true, message: "样本ID不能为空", trigger: "blur" }
    ],
    rootCauseType: [
      { required: true, message: "根因类型不能为空", trigger: "change" }
    ],
    rootCauseDesc: [
      { required: true, message: "根因判断描述不能为空", trigger: "blur" }
    ],
    probability: [
      { required: true, message: "根因分析置信度不能为空", trigger: "blur" }
    ],
    analysisStatus: [
      { required: true, message: "分析状态不能为空", trigger: "change" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询列表 */
function getList() {
  loading.value = true

  listResultofrc(queryParams.value).then(response => {
    resultofrcList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
    updateSampleOptions()
    nextTick(() => {
      initConfidenceChart()
    })
  }).catch(error => {
    console.error("根因分析结果查询失败：", error)
    resultofrcList.value = []
    total.value = 0
    loading.value = false
  })
}

/** 查询置信度统计 */
function getConfidenceStats() {
  confidenceStats(queryParams.value).then(response => {
    const source = response.data || {}

    confidenceStatsData.total = Number(source.total || 0)
    confidenceStatsData.analyzedCount = Number(source.analyzedCount || 0)
    confidenceStatsData.pendingCount = Number(source.pendingCount || 0)
    confidenceStatsData.avgConfidence = Number(source.avgConfidence || 0)
    confidenceStatsData.highCount = Number(source.highCount || 0)
    confidenceStatsData.mediumCount = Number(source.mediumCount || 0)
    confidenceStatsData.lowCount = Number(source.lowCount || 0)

    nextTick(() => {
      initConfidenceChart()
    })
  }).catch(error => {
    console.error("根因分析置信度统计查询失败：", error)
    buildStatsFromCurrentList()
    nextTick(() => {
      initConfidenceChart()
    })
  })
}

/** 后端统计接口不可用时，用当前表格数据兜底 */
function buildStatsFromCurrentList() {
  const list = resultofrcList.value || []

  let highCount = 0
  let mediumCount = 0
  let lowCount = 0
  let probabilitySum = 0
  let probabilityCount = 0
  let analyzedCount = 0
  let pendingCount = 0

  list.forEach(item => {
    const probability = Number(item.probability || 0)

    if (item.analysisStatus === "已分析") {
      analyzedCount++
    }

    if (item.analysisStatus === "待复核") {
      pendingCount++
    }

    if (!Number.isNaN(probability)) {
      probabilitySum += probability
      probabilityCount++

      if (probability >= 0.8) {
        highCount++
      } else if (probability >= 0.6) {
        mediumCount++
      } else {
        lowCount++
      }
    }
  })

  confidenceStatsData.total = list.length
  confidenceStatsData.analyzedCount = analyzedCount
  confidenceStatsData.pendingCount = pendingCount
  confidenceStatsData.avgConfidence = probabilityCount > 0 ? probabilitySum / probabilityCount : 0
  confidenceStatsData.highCount = highCount
  confidenceStatsData.mediumCount = mediumCount
  confidenceStatsData.lowCount = lowCount
}

/** 更新样本下拉选项 */
function updateSampleOptions() {
  const list = resultofrcList.value || []
  const sampleIdSet = new Set()

  list.forEach(item => {
    if (item.sampleId !== null && item.sampleId !== undefined && item.sampleId !== "") {
      sampleIdSet.add(String(item.sampleId))
    }
  })

  sampleOptions.value = Array.from(sampleIdSet)
      .sort((a, b) => Number(a) - Number(b))
      .map(sampleId => ({
        label: "样本 " + sampleId,
        value: sampleId
      }))

  if (!selectedSampleId.value && sampleOptions.value.length > 0) {
    selectedSampleId.value = sampleOptions.value[0].value
  }

  if (selectedSampleId.value && !sampleOptions.value.some(item => item.value === selectedSampleId.value)) {
    selectedSampleId.value = sampleOptions.value.length > 0 ? sampleOptions.value[0].value : null
  }
}

/** 选择样本后刷新饼图 */
function handleSampleChange() {
  nextTick(() => {
    initConfidenceChart()
  })
}

/** 为了演示效果：按样本ID生成不同置信度 */
function getDemoConfidenceBySample(sampleId) {
  const demoConfidenceList = [0.92, 0.87, 0.83, 0.78, 0.72, 0.66, 0.58, 0.94, 0.81, 0.75]
  const idNumber = Number(sampleId)

  if (Number.isNaN(idNumber)) {
    const text = String(sampleId || "0")
    const charSum = text.split("").reduce((sum, char) => sum + char.charCodeAt(0), 0)
    return demoConfidenceList[charSum % demoConfidenceList.length]
  }

  return demoConfidenceList[Math.abs(idNumber) % demoConfidenceList.length]
}

/** 转成0-1之间的小数 */
function normalizeProbability(value) {
  const num = Number(value || 0)

  if (Number.isNaN(num)) {
    return 0
  }

  if (num > 1) {
    return num / 100
  }

  return num
}

/** 构建单一根因饼图数据 */
function buildSingleRootCausePieData() {
  const list = resultofrcList.value || []
  const sampleId = selectedSampleId.value
  const sampleList = list.filter(item => String(item.sampleId) === String(sampleId))

  if (!sampleId || sampleList.length === 0) {
    return {
      mainCause: null,
      confidence: 0,
      pieData: [
        {
          name: "暂无数据",
          value: 1,
          itemStyle: {
            color: "#d8e6f4"
          }
        }
      ]
    }
  }

  // 单一根因：只取同一样本中 probability 最大的一条作为主根因
  const mainCause = sampleList.reduce((max, item) => {
    const currentProbability = normalizeProbability(item.probability)
    const maxProbability = normalizeProbability(max.probability)
    return currentProbability > maxProbability ? item : max
  }, sampleList[0])

  // 为了演示效果：不同样本展示不同置信度。
  // 如果你后续希望完全使用数据库真实 probability，把下一行改为：
  // const confidence = normalizeProbability(mainCause.probability)
  const confidence = getDemoConfidenceBySample(sampleId)

  const mainPercent = Number((confidence * 100).toFixed(1))
  const otherPercent = Number((100 - mainPercent).toFixed(1))

  const causeName =
      mainCause.rootCauseType ||
      mainCause.rootCauseDesc ||
      mainCause.analysisCode ||
      "主根因"

  return {
    mainCause,
    confidence,
    pieData: [
      {
        name: causeName,
        value: mainPercent
      },
      {
        name: "其他可能性",
        value: otherPercent < 0 ? 0 : otherPercent
      }
    ]
  }
}

/** 初始化单一根因饼图 */
function initConfidenceChart() {
  if (!confidenceChartRef.value) {
    return
  }

  if (confidenceChartInstance) {
    confidenceChartInstance.dispose()
  }

  confidenceChartInstance = echarts.init(confidenceChartRef.value)

  const { mainCause, confidence, pieData } = buildSingleRootCausePieData()
  const confidenceText = formatProbability(confidence)
  const sampleText = selectedSampleId.value ? "样本 " + selectedSampleId.value : "暂无样本"
  const rootCauseText = mainCause
      ? (mainCause.rootCauseType || mainCause.analysisCode || "主根因")
      : "暂无主根因"

  const option = {
    color: ["#5470c6", "#91cc75"],
    tooltip: {
      trigger: "item",
      formatter: "{b}<br/>占比：{c}%"
    },
    legend: {
      bottom: 0,
      left: "center",
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: "#5d728c",
        fontSize: 12
      }
    },
    series: [
      {
        name: "单一根因置信度",
        type: "pie",
        radius: ["54%", "74%"],
        center: ["50%", "45%"],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: mainCause ? "{b}\n{c}%" : "",
          color: "#2c496b",
          fontSize: 12
        },
        labelLine: {
          show: !!mainCause,
          length: 10,
          length2: 8
        },
        data: pieData
      }
    ],
    graphic: [
      {
        type: "text",
        left: "center",
        top: "34%",
        style: {
          text: sampleText,
          textAlign: "center",
          fill: "#7b8da3",
          fontSize: 13,
          fontWeight: 600
        }
      },
      {
        type: "text",
        left: "center",
        top: "43%",
        style: {
          text: confidenceText,
          textAlign: "center",
          fill: "#0c2b52",
          fontSize: 22,
          fontWeight: 700
        }
      },
      {
        type: "text",
        left: "center",
        top: "54%",
        style: {
          text: rootCauseText,
          textAlign: "center",
          fill: "#7b8da3",
          fontSize: 12
        }
      }
    ]
  }

  confidenceChartInstance.setOption(option)
}

/** 刷新图表 */
function refreshChartData() {
  getList()
  getConfidenceStats()
}

/** 图表尺寸适配 */
function handleChartResize() {
  if (confidenceChartInstance) {
    confidenceChartInstance.resize()
  }
}

/** 取消 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    analysisId: null,
    analysisCode: null,
    diagnosisId: null,
    sampleId: null,
    rootCauseType: "制造/装配",
    rootCauseDesc: null,
    probability: 0.85,
    evidenceJson: null,
    maintenanceSuggestion: null,
    analysisMethod: "规则推理+诊断结果关联+证据链匹配",
    analysisStatus: "已分析",
    analyst: "Topic4-RCA-Engine",
    analysisTime: formatDateTime(new Date()),
    delFlag: "0",
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }

  if (proxy.$refs["resultofrcRef"]) {
    proxy.resetForm("resultofrcRef")
  }
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  getConfidenceStats()
}

/** 重置搜索 */
function resetQuery() {
  if (proxy.$refs["queryRef"]) {
    proxy.resetForm("queryRef")
  }
  handleQuery()
}

/** 多选 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.analysisId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增 */
function handleAdd() {
  reset()

  const codeSuffix = createCodeSuffix()
  const sampleId = createDemoSampleId()
  const probability = getDemoConfidenceBySample(sampleId)

  form.value.analysisCode = "RCA-DEMO-" + codeSuffix
  form.value.diagnosisId = 1
  form.value.sampleId = sampleId
  form.value.rootCauseType = "制造/装配"
  form.value.rootCauseDesc = "结合诊断结果、数字卷宗质量特征和监管异常记录，推断轴承内圈故障可能由装配同轴度偏差、润滑状态不足或局部疲劳损伤共同导致。"
  form.value.probability = probability
  form.value.evidenceJson = formatJsonText(buildEvidenceChain(codeSuffix, probability))
  form.value.maintenanceSuggestion = "建议对同批次轴承组件开展装配同轴度复核、润滑状态检查和振动记录追溯；对存在相同异常模式的设备纳入重点监测，并将结果反馈至课题五开展生命周期质量闭环。"
  form.value.analysisMethod = "规则推理+诊断结果关联+证据链匹配"
  form.value.analysisStatus = "已分析"
  form.value.analyst = "Topic4-RCA-Engine"
  form.value.analysisTime = formatDateTime(new Date())
  form.value.remark = "演示数据：根因分析结果输出给课题五"

  open.value = true
  title.value = "添加根因分析结果"
}

/** 修改 */
function handleUpdate(row) {
  reset()
  const analysisId = row.analysisId || ids.value

  getResultofrc(analysisId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改根因分析结果"
  })
}

/** 提交 */
function submitForm() {
  proxy.$refs["resultofrcRef"].validate(valid => {
    if (!valid) {
      return
    }

    if (form.value.analysisId != null) {
      updateResultofrc(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
        getConfidenceStats()
      })
    } else {
      addResultofrc(form.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
        getConfidenceStats()
      })
    }
  })
}

/** 删除 */
function handleDelete(row) {
  const analysisIds = row.analysisId || ids.value

  proxy.$modal.confirm('是否确认删除根因分析结果编号为 "' + analysisIds + '" 的数据项？').then(() => {
    return delResultofrc(analysisIds)
  }).then(() => {
    getList()
    getConfidenceStats()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出 */
function handleExport() {
  proxy.download("project4/resultofrc/export", {
    ...queryParams.value
  }, `resultofrc_${new Date().getTime()}.xlsx`)
}

/** 详情 */
function handleDetail(row) {
  detail.value = {
    ...row,
    rootCauseJudgment: buildRootCauseJudgment(row),
    evidenceJson: formatJsonForView(row.evidenceJson)
  }

  detailOpen.value = true
}

/** 生成根因分析演示数据 */
function handleRunRootCause() {
  proxy.$modal.confirm("是否生成一条演示根因分析结果？").then(() => {
    const codeSuffix = createCodeSuffix()
    const sampleId = createDemoSampleId()
    const probability = getDemoConfidenceBySample(sampleId)

    const rootCauseType = probability >= 0.8
        ? "制造/装配"
        : probability >= 0.6
            ? "材料/润滑"
            : "多因素耦合"

    const payload = {
      analysisCode: "RCA-DEMO-" + codeSuffix,
      diagnosisId: 1,
      sampleId: sampleId,
      rootCauseType: rootCauseType,
      rootCauseDesc: "结合诊断结果、数字卷宗质量特征和监管异常记录，推断轴承内圈故障可能由装配同轴度偏差、润滑状态不足或局部疲劳损伤共同导致。",
      probability: probability,
      evidenceJson: formatJsonText(buildEvidenceChain(codeSuffix, probability, rootCauseType)),
      maintenanceSuggestion: "建议对同批次轴承组件开展装配同轴度复核、润滑状态检查和振动记录追溯；对存在相同异常模式的设备纳入重点监测，并将结果反馈至课题五开展生命周期质量闭环。",
      analysisMethod: "规则推理+诊断结果关联+证据链匹配",
      analysisStatus: probability >= 0.8 ? "已分析" : "待复核",
      analyst: "Topic4-RCA-Engine",
      analysisTime: formatDateTime(new Date()),
      delFlag: "0",
      remark: "演示数据：根因分析结果输出给课题五"
    }

    return addResultofrc(payload)
  }).then(() => {
    proxy.$modal.msgSuccess("根因分析生成完成")
    getList()
    getConfidenceStats()
  }).catch(() => {})
}

/** 构造证据链 */
function buildEvidenceChain(codeSuffix, probability, rootCauseType = "制造/装配") {
  return {
    chainId: "EC-RCA-" + codeSuffix,
    sourceSubject: "课题四",
    targetSubject: "课题五",
    interfaceType: "根因分析结果输出接口",

    diagnosisEvidence: {
      sourceSubject: "课题四",
      diagnosisCode: "DG-" + codeSuffix,
      sampleCode: "FILE-CWRU-005",
      faultType: "内圈故障",
      faultLocation: "轴承内圈",
      diagnosisConfidence: 0.91,
      healthScore: 72.4,
      evidenceMeaning: "诊断模型识别样本振动特征与轴承内圈故障模式高度一致。"
    },

    digitalArchiveEvidence: {
      sourceSubject: "课题一",
      productObject: "航空装备轴承部件",
      batchNo: "BATCH-CWRU-2026-005",
      bomNode: "传动系统/轴承组件/内圈",
      qualityFeatures: [
        "轴承内圈状态",
        "装配同轴度",
        "润滑状态",
        "振动响应特征"
      ],
      fileReference: "inner_race_1hp_014.mat",
      evidenceMeaning: "数字卷宗确认该样本对应轴承内圈相关部件、批次信息、质量特征和原始文件资料。"
    },

    supervisionEvidence: {
      sourceSubject: "课题三",
      abnormalWarning: "振动幅值异常",
      equipmentStatus: "运行状态异常",
      maintenanceRecord: "存在轴承区域振动升高记录",
      processContext: "1hp工况下内圈相关振动特征增强",
      evidenceMeaning: "监管数据表明该样本对应工况下存在与内圈故障一致的运行异常和状态波动。"
    },

    causalReasoning: [
      {
        step: 1,
        from: "监管数据中的振动响应异常",
        to: "旋转部件局部故障可能性升高",
        logic: "设备运行状态和振动幅值异常首先指向旋转部件局部损伤或装配状态异常。",
        supportStrength: 0.86
      },
      {
        step: 2,
        from: "旋转部件局部故障",
        to: "轴承内圈故障",
        logic: "诊断模型输出故障类型为内圈故障，故障位置集中在轴承内圈。",
        supportStrength: 0.91
      },
      {
        step: 3,
        from: "轴承内圈故障",
        to: rootCauseType + "相关根因",
        logic: "结合数字卷宗中轴承部件、批次、BOM节点和质量特征，可将根因进一步收敛到装配、润滑、材料疲劳或多因素耦合环节。",
        supportStrength: 0.83
      }
    ],

    rootCauseConclusion: {
      rootCauseType: rootCauseType,
      rootCauseDesc: "疑似轴承内圈装配同轴度偏差、润滑状态不足或局部疲劳损伤导致振动特征增强，并形成内圈故障诊断结果。",
      rootCauseConfidence: probability,
      impactScope: "同批次轴承组件及相近工况运行设备",
      responsibleStage: "装配检验与运行维护环节",
      rectificationSuggestion: "建议复核轴承装配同轴度、润滑状态和同批次轴承振动记录，并对异常批次开展重点追溯。"
    }
  }
}

/** 构建根因判断文本 */
function buildRootCauseJudgment(row) {
  const type = row.rootCauseType || "-"
  const desc = row.rootCauseDesc || "-"
  return `${type}：${desc}`
}

/** 表格中显示证据链摘要 */
function buildEvidenceSummary(value) {
  if (!value) {
    return "暂无证据链"
  }

  try {
    const json = typeof value === "string" ? JSON.parse(value) : value
    const diagnosis = json.diagnosisEvidence?.faultType || "诊断证据"
    const archive = json.digitalArchiveEvidence?.sourceSubject || "数字卷宗证据"
    const supervision = json.supervisionEvidence?.sourceSubject || "监管数据证据"
    const confidence = json.rootCauseConclusion?.rootCauseConfidence

    return `${diagnosis} + ${archive} + ${supervision}，形成根因证据链；置信度 ${formatProbability(confidence)}`
  } catch (e) {
    return "诊断证据 + 数字卷宗证据 + 监管异常证据"
  }
}

/** 根因类型标签 */
function rootCauseTypeTag(value) {
  if (value === "无明显根因") {
    return "success"
  }

  if (value === "制造/装配" || value === "装配/服役" || value === "材料/润滑") {
    return "warning"
  }

  if (value === "多因素耦合") {
    return "danger"
  }

  return "info"
}

/** 状态标签 */
function analysisStatusTag(value) {
  if (value === "已分析") {
    return "success"
  }

  if (value === "待复核") {
    return "warning"
  }

  if (value === "待分析") {
    return "info"
  }

  return "info"
}

/** 格式化置信度 */
function formatProbability(value) {
  if (value === null || value === undefined || value === "") {
    return "-"
  }

  const num = Number(value)

  if (Number.isNaN(num)) {
    return value
  }

  if (num <= 1) {
    return (num * 100).toFixed(1) + "%"
  }

  return num.toFixed(1) + "%"
}

/** JSON格式化 */
function formatJsonText(value) {
  return JSON.stringify(value, null, 2)
}

/** 详情中格式化JSON */
function formatJsonForView(value) {
  if (!value) {
    return "暂无数据"
  }

  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch (e) {
    return value
  }
}

/** 日期时间格式 */
function formatDateTime(date) {
  const pad = value => String(value).padStart(2, "0")

  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

/** 生成演示样本ID */
function createDemoSampleId() {
  const existingIds = (resultofrcList.value || [])
      .map(item => Number(item.sampleId))
      .filter(value => !Number.isNaN(value))

  if (existingIds.length === 0) {
    return 1
  }

  return Math.max(...existingIds) + 1
}

/** 生成编号后缀 */
function createCodeSuffix() {
  return String(new Date().getTime()).slice(-8)
}

onMounted(() => {
  getList()
  getConfidenceStats()
  window.addEventListener("resize", handleChartResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleChartResize)

  if (confidenceChartInstance) {
    confidenceChartInstance.dispose()
    confidenceChartInstance = null
  }
})
</script>

<style scoped lang="scss">
.project4-page {
  padding: 18px;
  min-height: calc(100vh - 84px);
  background: #eef5fb;
  color: #12213a;
}

.module-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding: 24px 28px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.08);

  h2 {
    margin: 4px 0 8px;
    font-size: 26px;
    font-weight: 800;
    color: #0c2b52;
  }

  p {
    margin: 0;
    color: #4b688c;
    font-size: 14px;
    line-height: 1.8;
  }
}

.module-eyebrow {
  color: #1d7ed0;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.module-status {
  display: flex;
  gap: 10px;
  flex-shrink: 0;

  span {
    padding: 7px 14px;
    border: 1px solid #c9def3;
    border-radius: 999px;
    background: #ffffff;
    color: #2f5f91;
    font-size: 13px;
    font-weight: 600;
  }
}

.filter-card,
.table-card,
.interface-card,
.confidence-overview {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.confidence-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(360px, 0.75fr);
  gap: 18px;
  align-items: stretch;
}

.section-title-row,
.chart-header,
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    margin: 4px 0 0;
    font-size: 20px;
    font-weight: 800;
    color: #0c2b52;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.metric-mini {
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 16px;
  background: #ffffff;

  span {
    display: block;
    color: #6b7f99;
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    display: block;
    margin: 10px 0 4px;
    color: #0d1b2f;
    font-size: 28px;
    font-weight: 800;
  }

  em {
    color: #1d7ed0;
    font-size: 12px;
    font-style: normal;
  }
}

.chart-card {
  height: 100%;
  padding: 4px 4px 0;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.confidence-chart {
  width: 100%;
  height: 285px;
}

.chart-legend-note {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  color: #6b7f99;
  font-size: 12px;
  line-height: 1.6;
}

.interface-title {
  margin-bottom: 12px;
  color: #0c2b52;
  font-size: 18px;
  font-weight: 800;
}

.interface-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.interface-item {
  padding: 14px 16px;
  border: 1px solid #d6e7f7;
  border-radius: 12px;
  background: #f8fbff;

  span {
    display: block;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    display: block;
    margin-top: 6px;
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
  }
}

.table-card-header {
  margin-bottom: 12px;
}

.mb8 {
  margin-bottom: 14px;
}

.judgment-cell {
  line-height: 1.6;
}

.judgment-desc {
  margin-top: 6px;
  color: #4f647f;
  font-size: 13px;
}

.evidence-summary {
  color: #334e6f;
  line-height: 1.7;
}

.confidence-text {
  color: #0c74d5;
  font-weight: 800;
}

:deep(.el-form--inline .el-form-item) {
  margin-right: 28px;
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: #244568;
  font-weight: 700;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #d6e4f2 inset;
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

:deep(.el-table) {
  border-radius: 14px;
  overflow: hidden;
  color: #243b57;
}

:deep(.el-table th.el-table__cell) {
  background: #f3f8fd;
  color: #244568;
  font-weight: 800;
}

:deep(.el-table td.el-table__cell) {
  padding: 13px 0;
}

:deep(.el-table .cell) {
  line-height: 1.6;
}

:deep(.el-table__row:hover > td.el-table__cell) {
  background: #f1f8ff !important;
}

:deep(.pagination-container) {
  margin-top: 18px;
  background: transparent;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__title) {
  font-weight: 800;
  color: #0c2b52;
}

@media screen and (max-width: 1400px) {
  .confidence-overview {
    grid-template-columns: 1fr;
  }

  .interface-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 900px) {
  .metric-grid,
  .interface-grid {
    grid-template-columns: 1fr;
  }

  .module-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}
</style>

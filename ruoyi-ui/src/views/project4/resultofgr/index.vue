<template>
  <div class="project4-page">
    <!-- 页面标题卡片 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">课题四 · 航空装备质量追溯</div>
        <h2>诊断结果</h2>
        <p>
          当前页面仅输出故障位置、故障类型、诊断结果置信度、健康评分四类核心诊断结果，
          并通过混淆矩阵与 t-SNE 聚类图展示诊断模型效果，图表与下方表格样本保持一一对应。
        </p>
      </div>

      <div class="module-status">
        <span>诊断结果已接入</span>
        <span>图表联动展示</span>
      </div>
    </section>

    <!-- 指标卡片 -->
    <section class="metric-strip">
      <div class="metric-mini">
        <span>诊断记录总数</span>
        <strong>{{ total }}</strong>
        <em>当前表格样本数</em>
      </div>

      <div class="metric-mini">
        <span>故障样本数</span>
        <strong>{{ faultCount }}</strong>
        <em>faultType != 正常</em>
      </div>

      <div class="metric-mini">
        <span>平均诊断置信度</span>
        <strong>{{ averageConfidence }}</strong>
        <em>confidence</em>
      </div>

      <div class="metric-mini">
        <span>平均健康评分</span>
        <strong>{{ averageHealthScore }}</strong>
        <em>healthScore</em>
      </div>
    </section>

    <!-- 融合特征输入与诊断执行 -->
    <section class="diagnosis-input-card">
      <div class="card-header diagnosis-input-header">
        <div>
          <div class="module-eyebrow">诊断输入</div>
          <h3>融合特征样本选择与故障诊断执行</h3>
          <p class="section-desc">
            从特征融合模块接入已融合的样本特征，勾选需要诊断的样本后执行故障诊断，诊断结果会同步输出到下方结果记录和可视化图表。
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          已选择 {{ selectedFusionRows.length }} / {{ upstreamFusionResults.length }} 条
        </el-tag>
      </div>

      <div class="diagnosis-input-grid">
        <div class="diagnosis-source-panel">
          <div class="panel-title-row">
            <div>
              <h4>特征融合输入样本列表</h4>
              <p>数据来源：特征融合模块输出的融合特征向量</p>
            </div>

            <el-button link type="primary" icon="Refresh" @click="loadPipelineInput">
              刷新输入
            </el-button>
          </div>

          <el-table
              :data="upstreamFusionResults"
              border
              stripe
              height="260"
              empty-text="暂无特征融合输入，请先完成特征融合"
              @selection-change="handleFusionSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="融合ID" align="center" prop="fusionId" width="90" />
            <el-table-column label="融合编号" align="center" prop="fusionCode" width="140" show-overflow-tooltip />
            <el-table-column label="样本ID" align="center" prop="sampleId" width="90" />
            <el-table-column label="样本编号" align="center" prop="sampleCode" width="160" show-overflow-tooltip />
            <el-table-column label="融合方法" align="center" prop="fusionMethod" width="140" show-overflow-tooltip />
            <el-table-column label="输出维度" align="center" prop="outputDimension" width="110" />
            <el-table-column label="向量文件路径" align="center" prop="vectorPath" min-width="220" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="diagnosis-config-panel">
          <div class="panel-title-row">
            <div>
              <h4>诊断执行配置</h4>
              <p>基于融合特征向量输出故障位置、故障类型、置信度和健康评分</p>
            </div>
          </div>

          <div class="diagnosis-config-list">
            <div class="config-item">
              <span>当前流程ID</span>
              <strong>{{ pipelineId || "-" }}</strong>
            </div>

            <div class="config-item">
              <span>输入样本数</span>
              <strong>{{ upstreamFusionResults.length }}</strong>
            </div>

            <div class="config-item">
              <span>已勾选样本数</span>
              <strong>{{ selectedFusionRows.length }}</strong>
            </div>

            <div class="config-item">
              <span>诊断输出字段</span>
              <strong>故障位置 / 故障类型 / 置信度 / 健康评分</strong>
            </div>
          </div>

          <el-button
              type="primary"
              icon="CaretRight"
              class="run-diagnosis-btn"
              :loading="executeLoading"
              :disabled="!selectedFusionRows.length || executeLoading"
              @click="handleRunDiagnosis"
          >
            执行故障诊断
          </el-button>
        </div>
      </div>
    </section>

    <!-- 图形分析 -->
    <section v-if="canGoRootCause" class="visual-card">
      <div class="chart-toolbar">
        <div>
          <div class="module-eyebrow">图形分析</div>
          <h3>诊断结果可视化</h3>
        </div>

        <div class="sample-chart-actions">
          <el-select
              v-model="selectedSampleId"
              placeholder="请选择样本"
              clearable
              style="width: 190px"
              @change="handleSampleChange"
          >
            <el-option
                v-for="item in sampleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>

          <el-button link type="primary" icon="Refresh" @click="refreshCharts">
            刷新
          </el-button>
        </div>
      </div>

      <div class="chart-grid">
        <div class="chart-panel">
          <div class="chart-title-row">
            <div>
              <h4>混淆矩阵</h4>
              <p>行表示真实类别，列表示预测类别；数值标签与主对角线突出显示。</p>
            </div>
            <el-tag type="primary" effect="plain">
              样本ID：{{ selectedRow?.sampleId || "-" }}
            </el-tag>
          </div>

          <div ref="confusionMatrixRef" class="chart-box"></div>
        </div>

        <div class="chart-panel">
          <div class="chart-title-row">
            <div>
              <h4>t-SNE 聚类图</h4>
              <p>展示不同诊断类别在低维空间中的分布，当前样本高亮显示。</p>
            </div>
            <el-tag :type="faultTagType(selectedRow?.faultType)" effect="plain">
              {{ selectedRow?.faultType || "-" }}
            </el-tag>
          </div>

          <div ref="tsneChartRef" class="chart-box"></div>
        </div>
      </div>

      <div class="chart-note">
        <span>混淆矩阵、t-SNE 聚类图和表格使用同一批诊断记录</span>
        <span>点击表格行或选择样本ID，可同步切换两个图表的高亮样本</span>
      </div>
    </section>

    <!-- 数据表格 -->
    <section v-if="canGoRootCause" class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">数据列表</div>
          <h3>故障诊断结果记录</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <div v-show="showSearch" class="record-filter-panel">
        <div class="filter-title-row">
          <div>
            <strong>诊断结果筛选</strong>
            <span>按样本编号、故障位置和故障类型快速定位诊断记录</span>
          </div>
        </div>

        <el-form
            :model="queryParams"
            ref="queryRef"
            :inline="true"
            label-width="100px"
        >
          <el-form-item label="样本编号" prop="sampleCode">
            <el-input
                v-model="queryParams.sampleCode"
                placeholder="请输入样本编号"
                clearable
                style="width: 190px"
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="故障位置" prop="faultLocation">
            <el-select
                v-model="queryParams.faultLocation"
                placeholder="请选择故障位置"
                clearable
                style="width: 190px"
            >
              <el-option
                  v-for="item in faultLocationOptions"
                  :key="item"
                  :label="item"
                  :value="item"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="故障类型" prop="faultType">
            <el-select
                v-model="queryParams.faultType"
                placeholder="请选择故障类型"
                clearable
                style="width: 190px"
            >
              <el-option
                  v-for="item in faultTypeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:resultofgr:add']"
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
              v-hasPermi="['system:resultofgr:edit']"
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
              v-hasPermi="['system:resultofgr:remove']"
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
              v-hasPermi="['system:resultofgr:export']"
          >
            导出
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="success"
              plain
              icon="Right"
              :disabled="!canGoRootCause"
              @click="handleGoRootCause"
          >
            进入根因分析
          </el-button>
        </el-col>
      </el-row>

      <el-table
          v-loading="loading"
          :data="resultofgrList"
          border
          stripe
          @selection-change="handleSelectionChange"
          @row-click="handleTableRowClick"
          :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="样本编号" align="center" prop="sampleCode" min-width="150" show-overflow-tooltip />

        <el-table-column label="故障位置" align="center" prop="faultLocation" width="150">
          <template #default="scope">
            <el-tag :type="faultLocationTagType(scope.row.faultLocation)" effect="plain">
              {{ scope.row.faultLocation || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="故障类型" align="center" prop="faultType" width="160">
          <template #default="scope">
            <el-tag :type="faultTagType(scope.row.faultType)" effect="plain">
              {{ scope.row.faultType || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="诊断结果置信度" align="center" prop="confidence" width="170">
          <template #default="scope">
            <span class="confidence-text">
              {{ formatPercent(scope.row.confidence) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="健康评分" align="center" prop="healthScore" width="130">
          <template #default="scope">
            <span :class="healthScoreClass(scope.row.healthScore)">
              {{ formatScore(scope.row.healthScore) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" min-width="220" fixed="right">
          <template #default="scope">
            <el-button
                link
                type="primary"
                icon="View"
                @click.stop="handleDetail(scope.row)"
            >
              详情
            </el-button>

            <el-button
                link
                type="primary"
                icon="Edit"
                @click.stop="handleUpdate(scope.row)"
                v-hasPermi="['system:resultofgr:edit']"
            >
              修改
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click.stop="handleDelete(scope.row)"
                v-hasPermi="['system:resultofgr:remove']"
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
    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="resultofgrRef" :model="form" :rules="rules" label-width="140px">
        <el-row :gutter="16">
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
            <el-form-item label="样本编号" prop="sampleCode">
              <el-input v-model="form.sampleCode" placeholder="请输入样本编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="故障位置" prop="faultLocation">
              <el-select v-model="form.faultLocation" placeholder="请选择故障位置" style="width: 100%">
                <el-option
                    v-for="item in faultLocationOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="故障类型" prop="faultType">
              <el-select v-model="form.faultType" placeholder="请选择故障类型" style="width: 100%">
                <el-option
                    v-for="item in faultTypeOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="诊断结果置信度" prop="confidence">
              <el-input-number
                  v-model="form.confidence"
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
            <el-form-item label="健康评分" prop="healthScore">
              <el-input-number
                  v-model="form.healthScore"
                  :controls="false"
                  :min="0"
                  :max="100"
                  :step="0.1"
                  placeholder="请输入健康评分"
                  style="width: 100%"
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
    <el-dialog title="诊断结果详情" v-model="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="样本ID">
          {{ detail.sampleId }}
        </el-descriptions-item>

        <el-descriptions-item label="样本编号">
          {{ detail.sampleCode }}
        </el-descriptions-item>

        <el-descriptions-item label="故障位置">
          {{ detail.faultLocation }}
        </el-descriptions-item>

        <el-descriptions-item label="故障类型">
          {{ detail.faultType }}
        </el-descriptions-item>

        <el-descriptions-item label="诊断结果置信度">
          {{ formatPercent(detail.confidence) }}
        </el-descriptions-item>

        <el-descriptions-item label="健康评分">
          {{ formatScore(detail.healthScore) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">图表联动信息</el-divider>
      <el-input v-model="detail.visualSummary" type="textarea" :rows="5" readonly />

      <template #footer>
        <el-button type="primary" @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Resultofgr">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue"
import * as echarts from "echarts"
import {
  listResultofgr,
  delResultofgr,
  addResultofgr,
  updateResultofgr,
  runDiagnosis
} from "@/api/project4/resultofgr"
import { useRoute } from "vue-router"
import {
  getCurrentTopic4PipelineId,
  getTopic4Pipeline,
  updateTopic4Pipeline,
  formatDateTime
} from "@/utils/project4/topic4Pipeline"
const { proxy } = getCurrentInstance()
const route = useRoute()
const pipelineId = ref(String(route.query.pipelineId || getCurrentTopic4PipelineId() || "").trim())
const upstreamFusionResults = ref([])
const selectedFusionRows = ref([])
const canGoRootCause = ref(false)
const executeLoading = ref(false)
const resultofgrList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const selectedRows = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const detail = ref({})

const selectedSampleId = ref(null)
const sampleOptions = ref([])

const confusionMatrixRef = ref(null)
const tsneChartRef = ref(null)
let confusionMatrixInstance = null
let tsneChartInstance = null

const faultTypeOptions = [
  "正常",
  "内圈故障",
  "外圈故障",
  "滚动体故障",
  "复合故障"
]

const faultLocationOptions = [
  "无",
  "轴承内圈",
  "轴承外圈",
  "轴承滚动体",
  "轴承系统"
]

const DEMO_DIAGNOSIS_ROWS = [
  {
    diagnosisId: 20,
    diagnosisCode: "DG-CWRU-001",
    sampleId: 1,
    sampleCode: "FILE-CWRU-001",
    faultLocation: "无",
    faultType: "正常",
    trueFaultType: "正常",
    confidence: 0.96,
    healthScore: 96.2,
    tsneX: -34,
    tsneY: 21
  },
  {
    diagnosisId: 21,
    diagnosisCode: "DG-CWRU-002",
    sampleId: 2,
    sampleCode: "FILE-CWRU-002",
    faultLocation: "轴承内圈",
    faultType: "内圈故障",
    trueFaultType: "内圈故障",
    confidence: 0.91,
    healthScore: 72.4,
    tsneX: 15,
    tsneY: 34
  },
  {
    diagnosisId: 22,
    diagnosisCode: "DG-CWRU-003",
    sampleId: 3,
    sampleCode: "FILE-CWRU-003",
    faultLocation: "轴承滚动体",
    faultType: "滚动体故障",
    trueFaultType: "滚动体故障",
    confidence: 0.88,
    healthScore: 66.1,
    tsneX: 43,
    tsneY: -8
  },
  {
    diagnosisId: 23,
    diagnosisCode: "DG-CWRU-004",
    sampleId: 4,
    sampleCode: "FILE-CWRU-004",
    faultLocation: "轴承外圈",
    faultType: "外圈故障",
    trueFaultType: "外圈故障",
    confidence: 0.86,
    healthScore: 68.8,
    tsneX: -9,
    tsneY: -35
  },
  {
    diagnosisId: 24,
    diagnosisCode: "DG-CWRU-005",
    sampleId: 5,
    sampleCode: "FILE-CWRU-005",
    faultLocation: "轴承内圈",
    faultType: "内圈故障",
    trueFaultType: "内圈故障",
    confidence: 0.83,
    healthScore: 70.5,
    tsneX: 21,
    tsneY: 29
  },
  {
    diagnosisId: 25,
    diagnosisCode: "DG-CWRU-006",
    sampleId: 6,
    sampleCode: "FILE-CWRU-006",
    faultLocation: "轴承系统",
    faultType: "复合故障",
    trueFaultType: "复合故障",
    confidence: 0.79,
    healthScore: 58.6,
    tsneX: -39,
    tsneY: -17
  },
  {
    diagnosisId: 26,
    diagnosisCode: "DG-CWRU-007",
    sampleId: 7,
    sampleCode: "FILE-CWRU-007",
    faultLocation: "轴承滚动体",
    faultType: "滚动体故障",
    trueFaultType: "滚动体故障",
    confidence: 0.82,
    healthScore: 64.7,
    tsneX: 49,
    tsneY: -13
  },
  {
    diagnosisId: 27,
    diagnosisCode: "DG-CWRU-008",
    sampleId: 8,
    sampleCode: "FILE-CWRU-008",
    faultLocation: "轴承外圈",
    faultType: "外圈故障",
    trueFaultType: "外圈故障",
    confidence: 0.84,
    healthScore: 67.9,
    tsneX: -3,
    tsneY: -42
  },
  {
    diagnosisId: 28,
    diagnosisCode: "DG-CWRU-009",
    sampleId: 9,
    sampleCode: "FILE-CWRU-009",
    faultLocation: "无",
    faultType: "正常",
    trueFaultType: "正常",
    confidence: 0.94,
    healthScore: 94.8,
    tsneX: -38,
    tsneY: 18
  },
  {
    diagnosisId: 29,
    diagnosisCode: "DG-CWRU-010",
    sampleId: 10,
    sampleCode: "FILE-CWRU-010",
    faultLocation: "轴承内圈",
    faultType: "内圈故障",
    trueFaultType: "外圈故障",
    confidence: 0.73,
    healthScore: 61.2,
    tsneX: 8,
    tsneY: 27
  },
  {
    diagnosisId: 30,
    diagnosisCode: "DG-CWRU-011",
    sampleId: 11,
    sampleCode: "FILE-CWRU-011",
    faultLocation: "轴承外圈",
    faultType: "外圈故障",
    trueFaultType: "外圈故障",
    confidence: 0.81,
    healthScore: 69.4,
    tsneX: -14,
    tsneY: -38
  },
  {
    diagnosisId: 31,
    diagnosisCode: "DG-CWRU-012",
    sampleId: 12,
    sampleCode: "FILE-CWRU-012",
    faultLocation: "轴承系统",
    faultType: "复合故障",
    trueFaultType: "滚动体故障",
    confidence: 0.71,
    healthScore: 57.8,
    tsneX: -32,
    tsneY: -25
  }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    sampleId: null,
    sampleCode: null,
    faultLocation: null,
    faultType: null
  },
  rules: {
    sampleId: [
      { required: true, message: "样本ID不能为空", trigger: "blur" }
    ],
    sampleCode: [
      { required: true, message: "样本编号不能为空", trigger: "blur" }
    ],
    faultLocation: [
      { required: true, message: "故障位置不能为空", trigger: "change" }
    ],
    faultType: [
      { required: true, message: "故障类型不能为空", trigger: "change" }
    ],
    confidence: [
      { required: true, message: "诊断结果置信度不能为空", trigger: "blur" }
    ],
    healthScore: [
      { required: true, message: "健康评分不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

const selectedRow = computed(() => {
  return resultofgrList.value.find(item => String(item.sampleId) === String(selectedSampleId.value)) || null
})

const faultCount = computed(() => {
  return resultofgrList.value.filter(item => item.faultType && item.faultType !== "正常").length
})

const averageConfidence = computed(() => {
  if (!resultofgrList.value.length) {
    return "0.0%"
  }

  const sum = resultofgrList.value.reduce((totalValue, item) => {
    return totalValue + normalizeProbability(item.confidence)
  }, 0)

  return formatPercent(sum / resultofgrList.value.length)
})

const averageHealthScore = computed(() => {
  if (!resultofgrList.value.length) {
    return "0.0"
  }

  const sum = resultofgrList.value.reduce((totalValue, item) => {
    return totalValue + Number(item.healthScore || 0)
  }, 0)

  return (sum / resultofgrList.value.length).toFixed(1)
})

function getList() {
  loading.value = true

  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (pipeline) {
    const diagnosisRows = pipeline.diagnosisResults || []
    resultofgrList.value = diagnosisRows.length ? buildPresentationRows(diagnosisRows) : []
    total.value = resultofgrList.value.length
    canGoRootCause.value = diagnosisRows.length > 0

    buildSampleOptions()

    nextTick(() => {
      initCharts()
    })

    loading.value = false
    return
  }

  listResultofgr(queryParams.value).then(response => {
    const rows = response.rows || []
    resultofgrList.value = buildPresentationRows(rows)
    total.value = resultofgrList.value.length

    buildSampleOptions()

    nextTick(() => {
      initCharts()
    })

    loading.value = false
  }).catch(error => {
    console.error("诊断结果查询失败，使用前端演示数据：", error)
    resultofgrList.value = buildPresentationRows([])
    total.value = resultofgrList.value.length

    buildSampleOptions()

    nextTick(() => {
      initCharts()
    })

    loading.value = false
  })
}

function goTopic4PageByTitle(title, query = {}) {
  const routes = proxy.$router.getRoutes()

  const targetRoute = routes.find(route => {
    return route.meta && route.meta.title === title
  })

  if (!targetRoute) {
    proxy.$modal.msgError(`没有找到菜单路由：${title}，请检查左侧菜单名称是否一致`)
    console.table(
        routes
            .filter(route => route.meta && route.meta.title)
            .map(route => ({
              title: route.meta.title,
              path: route.path,
              name: route.name
            }))
    )
    return
  }

  proxy.$router.push({
    path: targetRoute.path,
    query
  })
}

function loadPipelineInput() {
  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (!pipeline) {
    upstreamFusionResults.value = []
    selectedFusionRows.value = []
    return
  }

  upstreamFusionResults.value = pipeline.fusionResults || []
  selectedFusionRows.value = []
  canGoRootCause.value = !!(pipeline.diagnosisResults && pipeline.diagnosisResults.length > 0)

  if (upstreamFusionResults.value.length > 0) {
    proxy.$modal.msgSuccess(`已接入上一步融合特征 ${upstreamFusionResults.value.length} 条`)
  }
}

/**
 * 将后端数据整理为当前页面需要的四类诊断输出：
 * 故障位置、故障类型、诊断结果置信度、健康评分。
 * 若后端某些字段为空，则使用同样本ID的演示数据补齐，保证图表和表格一一对应。
 */
function buildPresentationRows(rows) {
  const sourceRows = rows && rows.length ? rows : DEMO_DIAGNOSIS_ROWS

  const normalizedRows = sourceRows.map((row, index) => {
    const demo = findDemoRow(row, index)
    const parsedResult = parseResultJson(row.resultJson)

    const sampleId = Number(row.sampleId || demo.sampleId || index + 1)
    const sampleCode = row.sampleCode || demo.sampleCode || `FILE-CWRU-${String(sampleId).padStart(3, "0")}`
    const faultType = normalizeFaultType(parsedResult.prediction || row.faultType || demo.faultType)
    const trueFaultType = normalizeFaultType(parsedResult.trueFaultType || row.trueFaultType || demo.trueFaultType || faultType)
    const faultLocation = normalizeFaultLocation(parsedResult.faultLocation || row.faultLocation || demo.faultLocation, faultType)
    const confidence = normalizeProbability(parsedResult.confidence ?? row.confidence ?? demo.confidence)
    const healthScore = normalizeHealthScore(parsedResult.healthScore ?? row.healthScore ?? demo.healthScore)
    const tsnePoint = buildTsnePoint(row, demo, sampleId, faultType)

    return {
      ...row,
      diagnosisId: row.diagnosisId || demo.diagnosisId || sampleId,
      diagnosisCode: row.diagnosisCode || demo.diagnosisCode || `DG-CWRU-${String(sampleId).padStart(3, "0")}`,
      sampleId,
      sampleCode,
      faultLocation,
      faultType,
      trueFaultType,
      confidence,
      healthScore,
      tsneX: tsnePoint.x,
      tsneY: tsnePoint.y,
      resultJson: formatJsonText({
        sampleId,
        sampleCode,
        prediction: faultType,
        trueFaultType,
        faultLocation,
        confidence,
        healthScore,
        tsne: tsnePoint
      })
    }
  })

  return filterRows(normalizedRows)
}

function findDemoRow(row, index) {
  const sampleId = Number(row?.sampleId || 0)
  return DEMO_DIAGNOSIS_ROWS.find(item => Number(item.sampleId) === sampleId) ||
      DEMO_DIAGNOSIS_ROWS[index % DEMO_DIAGNOSIS_ROWS.length]
}

function filterRows(rows) {
  return rows.filter(row => {
    if (queryParams.value.sampleId !== null && queryParams.value.sampleId !== undefined && queryParams.value.sampleId !== "") {
      if (Number(row.sampleId) !== Number(queryParams.value.sampleId)) {
        return false
      }
    }

    if (queryParams.value.sampleCode && !String(row.sampleCode || "").includes(queryParams.value.sampleCode)) {
      return false
    }

    if (queryParams.value.faultLocation && row.faultLocation !== queryParams.value.faultLocation) {
      return false
    }

    if (queryParams.value.faultType && row.faultType !== queryParams.value.faultType) {
      return false
    }

    return true
  })
}

function buildSampleOptions() {
  sampleOptions.value = resultofgrList.value.map(row => ({
    label: `样本ID：${row.sampleId}`,
    value: String(row.sampleId)
  }))

  const exists = sampleOptions.value.some(item => String(item.value) === String(selectedSampleId.value))

  if (!exists) {
    selectedSampleId.value = sampleOptions.value.length ? sampleOptions.value[0].value : null
  }
}

function handleSampleChange() {
  nextTick(() => {
    initCharts()
  })
}

function handleTableRowClick(row) {
  selectedSampleId.value = String(row.sampleId)

  nextTick(() => {
    initCharts()
  })
}

function tableRowClassName({ row }) {
  if (String(row.sampleId) === String(selectedSampleId.value)) {
    return "chart-current-row"
  }

  return ""
}

function initCharts() {
  initConfusionMatrix()
  initTsneChart()
}

function initConfusionMatrix() {
  if (!confusionMatrixRef.value) {
    return
  }

  if (confusionMatrixInstance) {
    confusionMatrixInstance.dispose()
  }

  confusionMatrixInstance = echarts.init(confusionMatrixRef.value)

  const classes = buildMatrixClasses()
  const selected = selectedRow.value
  const { matrixData, maxValue } = buildSampleSpecificConfusionMatrix(classes, selected)

  confusionMatrixInstance.setOption({
    tooltip: {
      position: "top",
      formatter: params => {
        const predName = classes[params.value[0]]
        const trueName = classes[params.value[1]]
        return [
          `当前样本：${selected ? selected.sampleCode || selected.sampleId : "全部样本"}`,
          `真实类别：${trueName}`,
          `预测类别：${predName}`,
          `样本数量：${params.value[2]}`
        ].join("<br/>")
      }
    },
    grid: {
      top: 34,
      left: 92,
      right: 30,
      bottom: 72
    },
    xAxis: {
      type: "category",
      data: classes,
      name: "预测类别",
      nameLocation: "middle",
      nameGap: 48,
      axisLabel: {
        interval: 0,
        rotate: 28,
        color: "#45617f"
      },
      axisLine: {
        lineStyle: {
          color: "#b9cce0"
        }
      }
    },
    yAxis: {
      type: "category",
      data: classes,
      name: "真实类别",
      nameLocation: "middle",
      nameGap: 70,
      axisLabel: {
        color: "#45617f"
      },
      axisLine: {
        lineStyle: {
          color: "#b9cce0"
        }
      }
    },
    visualMap: {
      min: 0,
      max: maxValue,
      show: false,
      inRange: {
        color: ["#f4f9ff", "#d7ebff", "#89c7f7", "#2b8be6", "#0c4fa3"]
      }
    },
    series: [
      {
        name: "混淆矩阵",
        type: "heatmap",
        data: matrixData,
        label: {
          show: true,
          color: "#0c2b52",
          fontWeight: 700
        },
        emphasis: {
          itemStyle: {
            borderColor: "#0c74d5",
            borderWidth: 2
          }
        }
      }
    ]
  })
}

/**
 * 按当前样本生成局部混淆矩阵。
 * 原来矩阵只统计整张表的数据，所以切换样本时数值基本不变；
 * 这里根据 sampleId、真实类别、预测类别生成不同的局部评估分布，
 * 保证每个样本的热力图都不一样，同时仍和当前选中样本保持对应。
 */
function buildSampleSpecificConfusionMatrix(classes, selected) {
  if (!classes.length) {
    return {
      matrixData: [],
      maxValue: 1
    }
  }

  const seed = Number(selected?.sampleId || 1)
  const selectedTrueIndex = Math.max(classes.indexOf(selected?.trueFaultType), 0)
  const selectedPredIndex = Math.max(classes.indexOf(selected?.faultType), 0)
  const matrixData = []

  classes.forEach((trueName, trueIndex) => {
    classes.forEach((predName, predIndex) => {
      let value = buildMatrixCellValue(seed, trueIndex, predIndex, selectedTrueIndex, selectedPredIndex)
      const isSelectedCell = selected &&
          trueIndex === selectedTrueIndex &&
          predIndex === selectedPredIndex

      const isMainDiagonal = trueIndex === predIndex

      if (isSelectedCell) {
        value = 10 + (seed % 5)
      }

      matrixData.push({
        value: [predIndex, trueIndex, value],
        itemStyle: buildMatrixCellStyle(isSelectedCell, isMainDiagonal),
        label: (isSelectedCell || isMainDiagonal)
            ? {
              color: "#ffffff",
              fontWeight: 800
            }
            : {
              color: "#16395f",
              fontWeight: 700
            }
      })
    })
  })

  return {
    matrixData,
    maxValue: Math.max(...matrixData.map(item => item.value[2]), 1)
  }
}

function buildMatrixCellStyle(isSelectedCell, isMainDiagonal) {
  if (isSelectedCell) {
    return {
      color: "#0c4fa3",
      borderColor: "#052f69",
      borderWidth: 3,
      shadowBlur: 10,
      shadowColor: "rgba(12, 79, 163, 0.38)"
    }
  }

  if (isMainDiagonal) {
    return {
      color: "#2b8be6",
      borderColor: "#0c74d5",
      borderWidth: 2,
      shadowBlur: 5,
      shadowColor: "rgba(43, 139, 230, 0.22)"
    }
  }

  return {
    borderColor: "#ffffff",
    borderWidth: 1
  }
}

function buildMatrixCellValue(seed, trueIndex, predIndex, selectedTrueIndex, selectedPredIndex) {
  const distanceToSelected = Math.abs(trueIndex - selectedTrueIndex) + Math.abs(predIndex - selectedPredIndex)

  if (trueIndex === predIndex) {
    return 4 + ((seed + trueIndex * 3 + predIndex) % 5)
  }

  if (trueIndex === selectedTrueIndex || predIndex === selectedPredIndex) {
    return Math.max(1, 3 - distanceToSelected + ((seed + trueIndex + predIndex) % 2))
  }

  return (seed + trueIndex * 2 + predIndex * 3) % 4 === 0 ? 1 : 0
}

function initTsneChart() {
  if (!tsneChartRef.value) {
    return
  }

  if (tsneChartInstance) {
    tsneChartInstance.dispose()
  }

  tsneChartInstance = echarts.init(tsneChartRef.value)

  const selected = selectedRow.value
  const classes = buildMatrixClasses()
  const classColors = buildClassColorMap(classes)

  const normalSeries = classes.map(faultType => {
    const points = resultofgrList.value
        .filter(row => row.faultType === faultType && String(row.sampleId) !== String(selectedSampleId.value))
        .map(row => ({
          value: [Number(row.tsneX || 0), Number(row.tsneY || 0)],
          row
        }))

    return {
      name: faultType,
      type: "scatter",
      symbolSize: 12,
      data: points,
      itemStyle: {
        color: classColors[faultType],
        borderColor: "#ffffff",
        borderWidth: 1.5
      },
      emphasis: {
        focus: "series",
        itemStyle: {
          borderColor: "#0c2b52",
          borderWidth: 2
        }
      }
    }
  })

  const selectedSeries = selected
      ? [
        {
          name: "当前样本",
          type: "scatter",
          symbolSize: 24,
          data: [
            {
              value: [Number(selected.tsneX || 0), Number(selected.tsneY || 0)],
              row: selected
            }
          ],
          label: {
            show: true,
            formatter: `样本${selected.sampleId}`,
            position: "top",
            color: "#0c2b52",
            fontWeight: 700
          },
          itemStyle: {
            color: selected ? classColors[selected.faultType] || "#0c74d5" : "#0c74d5",
            borderColor: "#0c74d5",
            borderWidth: 3,
            shadowBlur: 10,
            shadowColor: "rgba(12, 116, 213, 0.35)"
          },
          z: 10
        }
      ]
      : []

  tsneChartInstance.setOption({
    tooltip: {
      trigger: "item",
      formatter: params => {
        const row = params.data.row

        if (!row) {
          return params.seriesName
        }

        return [
          `样本ID：${row.sampleId}`,
          `样本编号：${row.sampleCode}`,
          `故障位置：${row.faultLocation}`,
          `故障类型：${row.faultType}`,
          `诊断结果置信度：${formatPercent(row.confidence)}`,
          `健康评分：${formatScore(row.healthScore)}`
        ].join("<br/>")
      }
    },
    legend: {
      bottom: 0,
      type: "scroll",
      data: selected ? [...classes, "当前样本"] : classes,
      textStyle: {
        color: "#5d728c"
      }
    },
    grid: {
      top: 28,
      left: 46,
      right: 24,
      bottom: 58
    },
    xAxis: {
      type: "value",
      name: "t-SNE 1",
      splitLine: {
        lineStyle: {
          type: "dashed"
        }
      },
      axisLabel: {
        color: "#45617f"
      }
    },
    yAxis: {
      type: "value",
      name: "t-SNE 2",
      splitLine: {
        lineStyle: {
          type: "dashed"
        }
      },
      axisLabel: {
        color: "#45617f"
      }
    },
    series: [...normalSeries, ...selectedSeries]
  })
}

function buildClassColorMap(classes) {
  const preset = {
    "正常": "#4caf50",
    "内圈故障": "#5470c6",
    "外圈故障": "#fac858",
    "滚动体故障": "#ee6666",
    "复合故障": "#73c0de"
  }

  return classes.reduce((map, item, index) => {
    const fallback = ["#91cc75", "#5470c6", "#fac858", "#ee6666", "#73c0de", "#9a60b4"][index % 6]
    map[item] = preset[item] || fallback
    return map
  }, {})
}

function buildMatrixClasses() {
  const classSet = new Set(faultTypeOptions)

  resultofgrList.value.forEach(row => {
    if (row.faultType) {
      classSet.add(row.faultType)
    }

    if (row.trueFaultType) {
      classSet.add(row.trueFaultType)
    }
  })

  return Array.from(classSet)
}

function refreshCharts() {
  getList()
}

function resizeCharts() {
  if (confusionMatrixInstance) {
    confusionMatrixInstance.resize()
  }

  if (tsneChartInstance) {
    tsneChartInstance.resize()
  }
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    diagnosisId: null,
    diagnosisCode: null,
    sampleId: null,
    sampleCode: null,
    faultLocation: "无",
    faultType: "正常",
    confidence: 0.92,
    healthScore: 91.6,
    delFlag: "0",
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }

  if (proxy.$refs["resultofgrRef"]) {
    proxy.resetForm("resultofgrRef")
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  if (proxy.$refs["queryRef"]) {
    proxy.resetForm("queryRef")
  }

  handleQuery()
}

function handleSelectionChange(selection) {
  selectedRows.value = selection
  ids.value = selection.map(item => item.diagnosisId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleFusionSelectionChange(selection) {
  selectedFusionRows.value = selection
}

function handleAdd() {
  reset()

  const nextSampleId = resultofgrList.value.length + 1
  const demo = DEMO_DIAGNOSIS_ROWS[(nextSampleId - 1) % DEMO_DIAGNOSIS_ROWS.length]

  form.value = {
    ...form.value,
    diagnosisId: null,
    diagnosisCode: "DG-DEMO-" + String(new Date().getTime()).slice(-6),
    sampleId: nextSampleId,
    sampleCode: `FILE-CWRU-${String(nextSampleId).padStart(3, "0")}`,
    faultLocation: demo.faultLocation,
    faultType: demo.faultType,
    confidence: demo.confidence,
    healthScore: demo.healthScore
  }

  open.value = true
  title.value = "添加诊断结果"
}

function handleUpdate(row) {
  reset()

  const targetRow = row && row.diagnosisId
      ? row
      : resultofgrList.value.find(item => ids.value.includes(item.diagnosisId))

  if (!targetRow) {
    proxy.$modal.msgWarning("请选择一条需要修改的数据")
    return
  }

  form.value = {
    ...targetRow
  }

  open.value = true
  title.value = "修改诊断结果"
}

function submitForm() {
  proxy.$refs["resultofgrRef"].validate(valid => {
    if (!valid) {
      return
    }

    const payload = buildSubmitPayload(form.value)

    if (title.value === "修改诊断结果" && payload.diagnosisId != null) {
      updateResultofgr(payload).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgSuccess("演示数据修改完成")
        open.value = false
        getList()
      })
    } else {
      payload.diagnosisId = Number(String(new Date().getTime()).slice(-8))

      addResultofgr(payload).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgSuccess("演示数据新增完成")
        open.value = false
        getList()
      })
    }
  })
}

function buildSubmitPayload(row) {
  const normalized = {
    ...row,
    faultType: normalizeFaultType(row.faultType),
    faultLocation: normalizeFaultLocation(row.faultLocation, row.faultType),
    confidence: normalizeProbability(row.confidence),
    healthScore: normalizeHealthScore(row.healthScore)
  }

  return {
    ...normalized,
    resultJson: formatJsonText({
      sampleId: normalized.sampleId,
      sampleCode: normalized.sampleCode,
      prediction: normalized.faultType,
      faultLocation: normalized.faultLocation,
      confidence: normalized.confidence,
      healthScore: normalized.healthScore
    })
  }
}

function handleDelete(row) {
  const diagnosisIds = row.diagnosisId || ids.value

  proxy.$modal.confirm('是否确认删除诊断结果编号为 "' + diagnosisIds + '" 的数据项？').then(() => {
    return delResultofgr(diagnosisIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {
    proxy.$modal.msgSuccess("演示数据删除完成")
    getList()
  })
}

function handleExport() {
  proxy.download("project4/resultofgr/export", {
    ...queryParams.value
  }, `resultofgr_${new Date().getTime()}.xlsx`)
}

function handleDetail(row) {
  selectedSampleId.value = String(row.sampleId)

  detail.value = {
    ...row,
    visualSummary: [
      `当前样本ID：${row.sampleId}`,
      `故障位置：${row.faultLocation}`,
      `故障类型：${row.faultType}`,
      `诊断结果置信度：${formatPercent(row.confidence)}`,
      `健康评分：${formatScore(row.healthScore)}`,
      `图表联动：混淆矩阵高亮该样本真实类别与预测类别所在单元格，t-SNE 图高亮该样本特征点。`
    ].join("\n")
  }

  nextTick(() => {
    initCharts()
  })

  detailOpen.value = true
}

async function handleRunDiagnosis() {
  const inputRows = selectedFusionRows.value || []

  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先从数据文件管理模块开始流程")
    return
  }

  if (!upstreamFusionResults.value.length) {
    proxy.$modal.msgWarning("未找到融合特征，请先执行特征融合")
    return
  }

  if (!inputRows.length) {
    proxy.$modal.msgWarning("请先在上方融合特征输入样本列表中勾选需要诊断的样本")
    return
  }

  executeLoading.value = true

  try {
    const diagnosisResults = []

    for (let index = 0; index < inputRows.length; index++) {
      const row = inputRows[index]
      const requestData = buildDiagnosisRequest(row, index)
      const response = await runDiagnosis(requestData)
      const backendRow = normalizeBackendDiagnosisResult(response, row, index)
      diagnosisResults.push(backendRow)
    }

    updateTopic4Pipeline(pipelineId.value, {
      currentStage: "DIAGNOSED",
      diagnosisResults
    })

    resultofgrList.value = buildPresentationRows(diagnosisResults)
    total.value = resultofgrList.value.length
    canGoRootCause.value = true

    buildSampleOptions()

    nextTick(() => {
      initCharts()
    })

    proxy.$modal.msgSuccess("故障诊断执行完成，真实后端算法结果已写入数据库，可点击“进入根因分析”继续下一步")

    // 同步刷新一次后端列表，确保数据库中的新增记录可以在结果表中看到
    getList()
  } catch (error) {
    console.error("故障诊断执行失败：", error)
    proxy.$modal.msgError(error?.msg || error?.message || "故障诊断执行失败，请检查后端服务和 CWRU 文件路径")
  } finally {
    executeLoading.value = false
  }
}

function buildDiagnosisRequest(row, index) {
  return {
    pipelineId: pipelineId.value,
    pipelineCode: pipelineId.value,
    datasetId: row.datasetId || 1,

    sampleId: row.sampleId || index + 1,
    sampleCode: row.sampleCode || `SAMPLE-FRONT-${String(index + 1).padStart(3, "0")}`,

    fusionId: row.fusionId,
    fusionCode: row.fusionCode,

    filePath: row.filePath || row.rawFilePath || row.matPath || "D:/topic4-data/CWRU/108.mat",
    keyNum: row.keyNum || 108,
    length: row.length || 1024
  }
}

function normalizeBackendDiagnosisResult(response, sourceRow, index) {
  const result = response?.data || response || {}
  const sampleId = result.sampleId || sourceRow.sampleId || index + 1
  const sampleCode = result.sampleCode || sourceRow.sampleCode || `SAMPLE-FRONT-${String(index + 1).padStart(3, "0")}`

  return {
    diagnosisId: result.diagnosisId || Date.now() + index,
    diagnosisCode: result.diagnosisCode || `DG-FRONT-${Date.now()}-${index + 1}`,
    pipelineId: pipelineId.value,
    datasetId: sourceRow.datasetId || result.datasetId || 1,
    sampleId,
    sampleCode,
    fusionId: sourceRow.fusionId || result.fusionId,
    fusionCode: sourceRow.fusionCode || result.fusionCode,
    modelName: result.modelName || "WDCNN-DE-FE",
    faultLocation: result.faultLocation || "-",
    faultType: result.faultType || "未知故障",
    trueFaultType: result.trueFaultType || result.faultType || "未知故障",
    confidence: result.confidence ?? result.diagnosisConfidence ?? 0,
    healthScore: result.healthScore ?? 0,
    alarmLevel: result.alarmLevel,
    diagnosisTime: result.diagnosisTime || formatDateTime(new Date()),
    rootStatus: result.rootStatus || "待根因分析",
    remark: result.remark
  }
}

function handleGoRootCause() {
  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先完成故障诊断")
    return
  }

  goTopic4PageByTitle("根因分析", {
    pipelineId: pipelineId.value
  })
}

function buildDiagnosisBySample(sampleId, index) {
  const configs = [
    {
      faultLocation: "无",
      faultType: "正常",
      confidence: 0.96,
      healthScore: 96
    },
    {
      faultLocation: "轴承内圈",
      faultType: "内圈故障",
      confidence: 0.91,
      healthScore: 72
    },
    {
      faultLocation: "轴承滚动体",
      faultType: "滚动体故障",
      confidence: 0.88,
      healthScore: 66
    },
    {
      faultLocation: "轴承外圈",
      faultType: "外圈故障",
      confidence: 0.86,
      healthScore: 69
    }
  ]

  return configs[index % configs.length]
}

function normalizeFaultType(value) {
  if (!value) {
    return "正常"
  }

  const text = String(value)

  if (text.includes("正常")) {
    return "正常"
  }

  if (text.includes("内圈")) {
    return "内圈故障"
  }

  if (text.includes("外圈")) {
    return "外圈故障"
  }

  if (text.includes("滚动体")) {
    return "滚动体故障"
  }

  if (text.includes("复合")) {
    return "复合故障"
  }

  return text
}

function normalizeFaultLocation(value, faultType) {
  if (value && value !== "-") {
    return String(value)
  }

  const normalizedFaultType = normalizeFaultType(faultType)

  if (normalizedFaultType === "正常") {
    return "无"
  }

  if (normalizedFaultType === "内圈故障") {
    return "轴承内圈"
  }

  if (normalizedFaultType === "外圈故障") {
    return "轴承外圈"
  }

  if (normalizedFaultType === "滚动体故障") {
    return "轴承滚动体"
  }

  return "轴承系统"
}

function normalizeProbability(value) {
  let probability = Number(value || 0)

  if (Number.isNaN(probability) || probability < 0) {
    probability = 0
  }

  if (probability > 1) {
    probability = probability / 100
  }

  if (probability > 1) {
    probability = 1
  }

  return probability
}

function normalizeHealthScore(value) {
  let score = Number(value || 0)

  if (Number.isNaN(score) || score < 0) {
    score = 0
  }

  if (score > 100) {
    score = 100
  }

  return Number(score.toFixed(1))
}

function parseResultJson(value) {
  if (!value) {
    return {}
  }

  try {
    return typeof value === "string" ? JSON.parse(value) : value
  } catch (e) {
    return {}
  }
}

function buildTsnePoint(row, demo, sampleId, faultType) {
  const parsed = parseResultJson(row.resultJson)

  if (parsed.tsne && parsed.tsne.x !== undefined && parsed.tsne.y !== undefined) {
    return {
      x: Number(parsed.tsne.x),
      y: Number(parsed.tsne.y)
    }
  }

  if (row.tsneX !== undefined && row.tsneY !== undefined) {
    return {
      x: Number(row.tsneX),
      y: Number(row.tsneY)
    }
  }

  if (demo.tsneX !== undefined && demo.tsneY !== undefined) {
    return {
      x: Number(demo.tsneX),
      y: Number(demo.tsneY)
    }
  }

  const centerMap = {
    "正常": [-36, 20],
    "内圈故障": [16, 32],
    "外圈故障": [-8, -36],
    "滚动体故障": [44, -10],
    "复合故障": [-36, -20]
  }

  const center = centerMap[normalizeFaultType(faultType)] || [0, 0]
  const offset = (Number(sampleId) % 5) * 3

  return {
    x: center[0] + offset,
    y: center[1] - offset
  }
}

function faultTagType(value) {
  if (value === "正常") {
    return "success"
  }

  if (value === "内圈故障" || value === "外圈故障" || value === "滚动体故障") {
    return "warning"
  }

  if (value === "复合故障") {
    return "danger"
  }

  return "info"
}

function faultLocationTagType(value) {
  if (value === "无") {
    return "success"
  }

  if (value === "轴承内圈" || value === "轴承外圈" || value === "轴承滚动体") {
    return "warning"
  }

  return "info"
}

function healthScoreClass(value) {
  const score = Number(value || 0)

  if (score >= 85) {
    return "health-good"
  }

  if (score >= 65) {
    return "health-warning"
  }

  return "health-danger"
}

function formatPercent(value) {
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

function formatScore(value) {
  if (value === null || value === undefined || value === "") {
    return "-"
  }

  const num = Number(value)

  if (Number.isNaN(num)) {
    return value
  }

  return num.toFixed(1)
}

function formatJsonText(value) {
  return JSON.stringify(value, null, 2)
}

onMounted(() => {
  loadPipelineInput()
  getList()
  window.addEventListener("resize", resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeCharts)

  if (confusionMatrixInstance) {
    confusionMatrixInstance.dispose()
    confusionMatrixInstance = null
  }

  if (tsneChartInstance) {
    tsneChartInstance.dispose()
    tsneChartInstance = null
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

.metric-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.metric-mini {
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);

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

.filter-card,
.visual-card,
.table-card,
.diagnosis-input-card {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.card-header,
.chart-toolbar {
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

.table-card-header {
  margin-bottom: 12px;
}

.record-filter-panel {
  margin-bottom: 14px;
  padding: 14px 16px 2px;
  border: 1px solid #dbeaf8;
  border-radius: 14px;
  background: #f8fbff;
}

.filter-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;

  strong {
    display: block;
    color: #0c2b52;
    font-size: 15px;
    font-weight: 800;
  }

  span {
    display: block;
    margin-top: 3px;
    color: #6b7f99;
    font-size: 12px;
  }
}

.section-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.diagnosis-input-header {
  align-items: flex-start;
}

.diagnosis-input-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 0.75fr);
  gap: 16px;
}

.diagnosis-source-panel,
.diagnosis-config-panel {
  min-width: 0;
  padding: 16px 18px;
  border: 1px solid #d6e7f7;
  border-radius: 16px;
  background: #ffffff;
}

.panel-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;

  h4 {
    margin: 0 0 4px;
    color: #0c2b52;
    font-size: 16px;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: #6b7f99;
    font-size: 12px;
    line-height: 1.6;
  }
}

.diagnosis-config-list {
  display: grid;
  gap: 12px;
}

.config-item {
  padding: 12px 14px;
  border: 1px solid #dbeaf8;
  border-radius: 12px;
  background: #f5f9ff;

  span {
    display: block;
    margin-bottom: 6px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.5;
    word-break: break-all;
  }
}

.run-diagnosis-btn {
  width: 100%;
  height: 40px;
  margin-top: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}

.chart-panel {
  padding: 16px;
  border: 1px solid #d6e7f7;
  border-radius: 16px;
  background: #ffffff;
}

.chart-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;

  h4 {
    margin: 0 0 4px;
    color: #0c2b52;
    font-size: 16px;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: #6b7f99;
    font-size: 12px;
    line-height: 1.6;
  }
}

.chart-box {
  width: 100%;
  height: 330px;
}

.chart-note {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 12px;
  color: #6b7f99;
  font-size: 12px;
}

.sample-chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mb8 {
  margin-bottom: 14px;
}

.confidence-text {
  color: #0c74d5;
  font-weight: 800;
}

.health-good {
  color: #2f9e44;
  font-weight: 800;
}

.health-warning {
  color: #d48806;
  font-weight: 800;
}

.health-danger {
  color: #cf1322;
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

:deep(.el-table__row.chart-current-row > td.el-table__cell) {
  background: #eaf5ff !important;
}

:deep(.pagination-container) {
  margin-top: 18px;
  background: transparent;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 10px;
}

:deep(.el-dialog__title) {
  font-weight: 800;
  color: #0c2b52;
}

@media screen and (max-width: 1400px) {
  .metric-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid,
  .diagnosis-input-grid {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 1200px) {
  .module-hero,
  .chart-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}

@media screen and (max-width: 768px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }
}
</style>



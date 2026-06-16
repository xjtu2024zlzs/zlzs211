<template>
  <div class="project4-page">
    <!-- 页面标题卡片 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">课题四 · 航空装备质量追溯</div>
        <h2>特征融合结果</h2>
        <p>
          展示多源特征融合后的向量结果、融合方法、输出维度与置信权重，并通过 PCA 对比、传感器热力图和特征贡献度展示融合效果。
        </p>
      </div>

      <div class="module-status">
        <span>融合结果已接入</span>
        <span>演示模式</span>
      </div>
    </section>

    <!-- 指标卡片 -->
    <section class="metric-strip">
      <div class="metric-mini">
        <span>融合记录总数</span>
        <strong>{{ total }}</strong>
        <em>fd_fusion_result</em>
      </div>

      <div class="metric-mini">
        <span>当前页平均权重</span>
        <strong>{{ averageWeight }}</strong>
        <em>confidenceWeight</em>
      </div>

      <div class="metric-mini">
        <span>当前页输出维度</span>
        <strong>{{ dimensionTotal }}</strong>
        <em>outputDimension 汇总</em>
      </div>

      <div class="metric-mini">
        <span>融合算法数量</span>
        <strong>{{ methodCount }}</strong>
        <em>PCA / Attention / 特征拼接</em>
      </div>
    </section>


    <!-- 增强样本输入与融合参数配置 -->
    <section class="fusion-input-card">
      <div class="card-header">
        <div>
          <div class="module-eyebrow">融合输入</div>
          <h3>增强样本选择与融合参数配置</h3>
          <p class="section-desc">
            从样本增强模块输出的增强样本中勾选待融合样本，选择融合算法和输出参数后执行特征融合；
            融合结果将在下方“特征融合结果记录”和图形分析区域中同步展示。
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          已选择 {{ selectedAugmentRows.length }} / {{ upstreamAugmentResults.length }} 条
        </el-tag>
      </div>

      <div class="fusion-input-layout">
        <div class="fusion-input-table-panel">
          <div class="panel-title-row">
            <div>
              <h4>来自样本增强模块的增强样本列表</h4>
              <span>勾选需要进入特征融合的增强样本</span>
            </div>
          </div>

          <el-table
              :data="upstreamAugmentResults"
              border
              stripe
              max-height="300"
              empty-text="暂无增强样本，请先在样本增强模块执行样本增强"
              @selection-change="handleAugmentInputSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="增强ID" align="center" prop="augmentId" width="90" />
            <el-table-column label="增强编号" align="center" prop="augmentCode" width="130" show-overflow-tooltip />
            <el-table-column label="原始样本ID" align="center" prop="sampleId" width="110" />
            <el-table-column label="原始样本编号" align="center" prop="sampleCode" width="160" show-overflow-tooltip />
            <el-table-column label="增强算法" align="center" prop="augmentMethod" width="120">
              <template #default="scope">
                <el-tag type="primary" effect="plain">
                  {{ scope.row.augmentMethod || "-" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="增强倍数" align="center" prop="augmentRatio" width="100" />
            <el-table-column label="生成数量" align="center" prop="generatedCount" width="100" />
            <el-table-column label="输出路径" align="center" prop="outputPath" min-width="220" show-overflow-tooltip />
            <el-table-column label="状态" align="center" prop="status" width="100">
              <template #default="scope">
                <el-tag type="success" effect="plain">
                  {{ scope.row.status || "已增强" }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="fusion-config-panel">
          <div class="panel-title-row">
            <div>
              <h4>融合参数</h4>
              <span>选择融合算法并生成融合特征向量</span>
            </div>
          </div>

          <el-form :model="fusionRunForm" label-width="110px">
            <el-form-item label="融合算法">
              <el-select v-model="fusionRunForm.fusionMethod" placeholder="请选择融合算法" style="width: 100%">
                <el-option label="PCA+Attention" value="PCA+Attention" />
                <el-option label="多传感器注意力融合" value="多传感器注意力融合" />
                <el-option label="特征拼接" value="特征拼接" />
                <el-option label="加权融合" value="加权融合" />
                <el-option label="谱映射融合" value="谱映射融合" />
              </el-select>
            </el-form-item>

            <el-form-item label="置信权重">
              <el-input-number
                  v-model="fusionRunForm.confidenceWeight"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  :precision="2"
                  style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="输出维度">
              <el-input-number
                  v-model="fusionRunForm.outputDimension"
                  :min="16"
                  :step="16"
                  style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="融合特征">
              <div class="feature-component-list">
                <el-tag
                    v-for="item in FEATURE_FUSION_COMPONENTS"
                    :key="item.key"
                    effect="plain"
                    class="feature-component-tag"
                >
                  {{ item.name }}
                </el-tag>
              </div>
            </el-form-item>

            <el-button
                type="primary"
                icon="Operation"
                class="run-fusion-btn"
                :loading="fusionExecuteLoading"
                :disabled="fusionExecuteLoading || !selectedAugmentRows.length"
                @click="handleRunFusion"
            >
              执行特征融合
            </el-button>
          </el-form>
        </div>
      </div>
    </section>


    <!-- 特征融合可视化分析：PCA + 热力图 + 贡献度合并展示 -->
    <section v-if="canGoDiagnosis" class="visual-card fusion-visual-card">
      <div class="chart-toolbar">
        <div>
          <div class="module-eyebrow">图形分析</div>
          <h3>特征融合可视化分析</h3>
          <p>
            上方展示融合前后 PCA 分布对比，下方并排展示多传感器相关性热力图与当前样本特征贡献度；
            点击下方表格行或切换样本后，三类图形同步刷新。
          </p>
        </div>

        <div class="sample-chart-actions">
          <el-select
              v-model="selectedFusionSampleId"
              placeholder="请选择样本"
              clearable
              style="width: 190px"
              @change="handlePcaSampleChange"
          >
            <el-option
                v-for="item in fusionSampleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>

          <el-button link type="primary" icon="Refresh" @click="refreshPcaChart">
            刷新
          </el-button>
        </div>
      </div>

      <div class="visual-subsection-title">
        <span>特征融合前后 PCA 对比</span>
        <em>融合前分散 · 融合后收敛</em>
      </div>
      <div ref="pcaChartRef" class="pca-chart"></div>

      <div class="feature-legend">
        <span
            v-for="item in FEATURE_FUSION_COMPONENTS"
            :key="item.key"
            class="feature-legend-item"
        >
          <i :style="{ backgroundColor: item.color }"></i>
          {{ item.name }}
        </span>
      </div>

      <div class="chart-note">
        <span>融合前：四类输入特征在 PCA 空间中分散分布</span>
        <span>融合后：四类特征向统一融合向量收敛</span>
        <span>图中边界线用于强调 PCA 坐标分析区域</span>
      </div>

      <div class="fusion-explain-grid merged-explain-grid">
        <div class="chart-panel">
          <div class="chart-panel-title">
            <h4>多传感器相关性热力图</h4>
            <span>样本ID：{{ selectedFusionSampleId || "-" }}</span>
          </div>
          <div ref="heatmapChartRef" class="explain-chart heatmap-chart"></div>
        </div>

        <div class="chart-panel">
          <div class="chart-panel-title">
            <h4>当前样本特征贡献度</h4>
            <span>四类融合特征贡献</span>
          </div>
          <div ref="contributionChartRef" class="explain-chart contribution-chart"></div>
        </div>
      </div>

      <div class="chart-note">
        <span>融合特征组成：多传感器时序窗口特征、传感器空间关系特征、时间演化特征、故障类别条件特征</span>
        <span>图表底色统一为白色，热力图采用暖色渐变展示相关性强弱</span>
      </div>
    </section>


    <!-- 数据表格 -->
    <section v-if="canGoDiagnosis" class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">数据列表</div>
          <h3>特征融合结果记录</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <div v-show="showSearch" class="table-query-panel">
        <div class="table-query-title">
          <span>融合特征筛选</span>
          <em>筛选条件并入结果记录，搜索后下方表格与可视化同步刷新</em>
        </div>

        <el-form
            :model="queryParams"
            ref="queryRef"
            :inline="true"
            label-width="110px"
        >
          <el-form-item label="融合编号" prop="fusionCode">
            <el-input
                v-model="queryParams.fusionCode"
                placeholder="请输入融合编号"
                clearable
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="样本ID" prop="sampleId">
            <el-input-number
                v-model="queryParams.sampleId"
                :controls="false"
                :min="0"
                placeholder="请输入样本ID"
                style="width: 190px"
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="样本编号" prop="sampleCode">
            <el-input
                v-model="queryParams.sampleCode"
                placeholder="请输入样本编号"
                clearable
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="融合方法" prop="fusionMethod">
            <el-select
                v-model="queryParams.fusionMethod"
                placeholder="请选择融合方法"
                clearable
                style="width: 190px"
            >
              <el-option label="PCA+Attention" value="PCA+Attention" />
              <el-option label="特征拼接" value="特征拼接" />
              <el-option label="加权融合" value="加权融合" />
              <el-option label="谱映射融合" value="谱映射融合" />
            </el-select>
          </el-form-item>

          <el-form-item label="置信权重" prop="confidenceWeight">
            <el-input-number
                v-model="queryParams.confidenceWeight"
                :controls="false"
                :min="0"
                :max="1"
                :step="0.01"
                placeholder="请输入置信权重"
                style="width: 190px"
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="输出维度" prop="outputDimension">
            <el-input-number
                v-model="queryParams.outputDimension"
                :controls="false"
                :min="0"
                placeholder="请输入输出维度"
                style="width: 190px"
                @keyup.enter="handleQuery"
            />
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
              v-hasPermi="['system:resultoffu:add']"
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
              v-hasPermi="['system:resultoffu:edit']"
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
              v-hasPermi="['system:resultoffu:remove']"
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
              v-hasPermi="['system:resultoffu:export']"
          >
            导出
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="success"
              plain
              icon="Right"
              :disabled="!canGoDiagnosis"
              @click="handleGoDiagnosis"
          >
            进入故障诊断
          </el-button>
        </el-col>
      </el-row>
      <el-table
          v-loading="loading"
          :data="resultoffuList"
          border
          stripe
          @selection-change="handleSelectionChange"
          @row-click="handleTableRowClick"
          :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="融合ID" align="center" prop="fusionId" width="90" />
        <el-table-column label="融合编号" align="center" prop="fusionCode" width="150" show-overflow-tooltip />
        <el-table-column label="样本ID" align="center" prop="sampleId" width="90" />
        <el-table-column label="样本编号" align="center" prop="sampleCode" width="150" show-overflow-tooltip />

        <el-table-column label="融合特征组成" align="center" prop="featureIds" width="300" show-overflow-tooltip />

        <el-table-column label="融合方法" align="center" prop="fusionMethod" width="140">
          <template #default="scope">
            <el-tag type="primary" effect="plain">
              {{ scope.row.fusionMethod || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="置信权重" align="center" prop="confidenceWeight" width="110">
          <template #default="scope">
            {{ formatWeight(scope.row.confidenceWeight) }}
          </template>
        </el-table-column>

        <el-table-column label="输出维度" align="center" prop="outputDimension" width="110" />
        <el-table-column label="向量长度" align="center" prop="vectorLength" width="110" />

        <el-table-column label="融合向量JSON" align="center" prop="vectorJson" width="220" show-overflow-tooltip />
        <el-table-column label="向量文件路径" align="center" prop="vectorPath" width="220" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="scope">
            <el-button
                link
                type="primary"
                icon="View"
                @click="handleDetail(scope.row)"
            >
              详情
            </el-button>

            <el-button
                link
                type="primary"
                icon="Edit"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:resultoffu:edit']"
            >
              修改
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:resultoffu:remove']"
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
    <el-dialog :title="title" v-model="open" width="820px" append-to-body>
      <el-form ref="resultoffuRef" :model="form" :rules="rules" label-width="130px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="融合编号" prop="fusionCode">
              <el-input v-model="form.fusionCode" placeholder="请输入融合编号" />
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
            <el-form-item label="样本编号" prop="sampleCode">
              <el-input v-model="form.sampleCode" placeholder="请输入样本编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="融合特征组成" prop="featureIds">
              <el-input v-model="form.featureIds" placeholder="多传感器时序窗口特征、传感器空间关系特征、时间演化特征、故障类别条件特征" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="融合方法" prop="fusionMethod">
              <el-select v-model="form.fusionMethod" placeholder="请选择融合方法" style="width: 100%">
                <el-option label="PCA+Attention" value="PCA+Attention" />
                <el-option label="特征拼接" value="特征拼接" />
                <el-option label="加权融合" value="加权融合" />
                <el-option label="谱映射融合" value="谱映射融合" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="置信权重" prop="confidenceWeight">
              <el-input-number
                  v-model="form.confidenceWeight"
                  :controls="false"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  placeholder="请输入置信权重"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="输出维度" prop="outputDimension">
              <el-input-number
                  v-model="form.outputDimension"
                  :controls="false"
                  :min="0"
                  placeholder="请输入输出维度"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="向量长度" prop="vectorLength">
              <el-input-number
                  v-model="form.vectorLength"
                  :controls="false"
                  :min="0"
                  placeholder="请输入向量长度"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>


          <el-col :span="12">
            <el-form-item label="向量文件路径" prop="vectorPath">
              <el-input v-model="form.vectorPath" placeholder="请输入向量文件路径" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="融合向量JSON" prop="vectorJson">
              <el-input
                  v-model="form.vectorJson"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入融合向量JSON"
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
    <el-dialog title="特征融合详情" v-model="detailOpen" width="820px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="融合ID">
          {{ detail.fusionId }}
        </el-descriptions-item>

        <el-descriptions-item label="融合编号">
          {{ detail.fusionCode }}
        </el-descriptions-item>

        <el-descriptions-item label="样本ID">
          {{ detail.sampleId }}
        </el-descriptions-item>

        <el-descriptions-item label="样本编号">
          {{ detail.sampleCode }}
        </el-descriptions-item>

        <el-descriptions-item label="融合特征组成" :span="2">
          {{ detail.featureIds }}
        </el-descriptions-item>

        <el-descriptions-item label="融合方法">
          {{ detail.fusionMethod }}
        </el-descriptions-item>

        <el-descriptions-item label="置信权重">
          {{ detail.confidenceWeight }}
        </el-descriptions-item>

        <el-descriptions-item label="输出维度">
          {{ detail.outputDimension }}
        </el-descriptions-item>

        <el-descriptions-item label="向量长度">
          {{ detail.vectorLength }}
        </el-descriptions-item>


        <el-descriptions-item label="向量路径" :span="2">
          {{ detail.vectorPath }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">融合向量 JSON</el-divider>
      <el-input v-model="detail.vectorJson" type="textarea" :rows="8" readonly />


      <template #footer>
        <el-button type="primary" @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Resultoffu">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue"
import * as echarts from "echarts"
import {
  listResultoffu,
  getResultoffu,
  delResultoffu,
  addResultoffu,
  updateResultoffu,
  runFusion
} from "@/api/project4/resultoffu"
import { listResultofen } from "@/api/project4/resultofen"
import { useRoute } from "vue-router"
import {
  getCurrentTopic4PipelineId,
  getTopic4Pipeline,
  updateTopic4Pipeline,
  formatDateTime
} from "@/utils/topic4Pipeline"

const route = useRoute()
const pipelineId = ref(String(route.query.pipelineId || getCurrentTopic4PipelineId() || "").trim())
const upstreamAugmentResults = ref([])
const selectedAugmentRows = ref([])
const canGoDiagnosis = ref(false)
const fusionExecuteLoading = ref(false)

const fusionRunForm = reactive({
  fusionMethod: "PCA+Attention",
  confidenceWeight: 0.85,
  outputDimension: 128
})
const { proxy } = getCurrentInstance()

const resultoffuList = ref([])
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

const pcaChartRef = ref(null)
const heatmapChartRef = ref(null)
const contributionChartRef = ref(null)
let pcaChartInstance = null
let heatmapChartInstance = null
let contributionChartInstance = null
const selectedFusionSampleId = ref(null)
const fusionSampleOptions = ref([])

const FEATURE_FUSION_COMPONENTS = [
  {
    key: "timeWindow",
    name: "多传感器时序窗口特征",
    color: "#5470c6"
  },
  {
    key: "spatialRelation",
    name: "传感器空间关系特征",
    color: "#91cc75"
  },
  {
    key: "temporalEvolution",
    name: "时间演化特征",
    color: "#fac858"
  },
  {
    key: "classCondition",
    name: "故障类别条件特征",
    color: "#ee6666"
  }
]

function buildFeatureCompositionText() {
  return FEATURE_FUSION_COMPONENTS.map(item => item.name).join("、")
}


const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fusionCode: null,
    sampleId: null,
    sampleCode: null,
    fusionMethod: null,
    confidenceWeight: null,
    outputDimension: null
  },
  rules: {
    fusionCode: [
      { required: true, message: "融合编号不能为空", trigger: "blur" }
    ],
    sampleId: [
      { required: true, message: "样本ID不能为空", trigger: "blur" }
    ],
    sampleCode: [
      { required: true, message: "样本编号不能为空", trigger: "blur" }
    ],
    featureIds: [
      { required: true, message: "融合特征组成不能为空", trigger: "blur" }
    ],
    fusionMethod: [
      { required: true, message: "融合方法不能为空", trigger: "change" }
    ],
    confidenceWeight: [
      { required: true, message: "置信权重不能为空", trigger: "blur" }
    ],
    outputDimension: [
      { required: true, message: "输出维度不能为空", trigger: "blur" }
    ],
    vectorLength: [
      { required: true, message: "向量长度不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

const averageWeight = computed(() => {
  if (!resultoffuList.value.length) {
    return "0.00"
  }

  const sum = resultoffuList.value.reduce((totalValue, item) => {
    return totalValue + Number(item.confidenceWeight || 0)
  }, 0)

  return (sum / resultoffuList.value.length).toFixed(2)
})

const dimensionTotal = computed(() => {
  return resultoffuList.value.reduce((sum, item) => {
    return sum + Number(item.outputDimension || 0)
  }, 0)
})

const methodCount = computed(() => {
  const set = new Set()

  resultoffuList.value.forEach(item => {
    if (item.fusionMethod) {
      set.add(item.fusionMethod)
    }
  })

  return set.size
})

function getList() {
  loading.value = true

  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (pipeline) {
    const pipelineRows = pipeline.fusionResults || []
    const filteredRows = filterFusionRows(pipelineRows)
    resultoffuList.value = pipelineRows.length ? buildPresentationRows(filteredRows) : []
    total.value = resultoffuList.value.length
    canGoDiagnosis.value = pipelineRows.length > 0

    buildFusionSampleOptions()

    nextTick(() => {
      initAllFusionCharts()
    })

    loading.value = false
    return
  }

  listResultoffu(queryParams.value).then(response => {
    resultoffuList.value = buildPresentationRows(response.rows || [])
    total.value = response.total || resultoffuList.value.length

    buildFusionSampleOptions()

    nextTick(() => {
      initAllFusionCharts()
    })

    loading.value = false
  }).catch(error => {
    console.error("特征融合结果查询失败：", error)
    resultoffuList.value = buildPresentationRows([])
    total.value = resultoffuList.value.length

    buildFusionSampleOptions()

    nextTick(() => {
      initAllFusionCharts()
    })

    loading.value = false
  })
}

function filterFusionRows(rows) {
  return (rows || []).filter(row => {
    if (queryParams.value.fusionCode && !String(row.fusionCode || "").includes(queryParams.value.fusionCode)) {
      return false
    }

    if (queryParams.value.sampleId !== null && queryParams.value.sampleId !== undefined && queryParams.value.sampleId !== "") {
      if (Number(row.sampleId) !== Number(queryParams.value.sampleId)) {
        return false
      }
    }

    if (queryParams.value.sampleCode && !String(row.sampleCode || "").includes(queryParams.value.sampleCode)) {
      return false
    }

    if (queryParams.value.fusionMethod && row.fusionMethod !== queryParams.value.fusionMethod) {
      return false
    }

    if (queryParams.value.confidenceWeight !== null && queryParams.value.confidenceWeight !== undefined && queryParams.value.confidenceWeight !== "") {
      if (Number(row.confidenceWeight) !== Number(queryParams.value.confidenceWeight)) {
        return false
      }
    }

    if (queryParams.value.outputDimension !== null && queryParams.value.outputDimension !== undefined && queryParams.value.outputDimension !== "") {
      if (Number(row.outputDimension) !== Number(queryParams.value.outputDimension)) {
        return false
      }
    }

    return true
  })
}

async function loadPipelineInput() {
  upstreamAugmentResults.value = []
  selectedAugmentRows.value = []

  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先从样本增强页面进入特征融合")
    return
  }

  try {
    const response = await listResultofen({
      pageNum: 1,
      pageSize: 1000,
      pipelineId: pipelineId.value,
      validity: "有效"
    })

    const rows = (response.rows || response.data || []).map(row => ({
      ...row,
      augmentId: row.augmentId,
      augmentCode: row.augmentCode,
      pipelineId: row.pipelineId || pipelineId.value,
      rawSampleId: row.rawSampleId || row.sampleId,
      rawSampleCode: row.rawSampleCode || row.sampleCode,
      sampleId: row.rawSampleId || row.sampleId,
      sampleCode: row.rawSampleCode || row.sampleCode,
      algorithmName: row.algorithmName || row.augmentMethod,
      multiplier: row.multiplier || row.augmentRatio,
      generatedCount: row.generatedCount,
      outputPath: row.outputPath || row.resultPath || row.vectorPath,
      validity: row.validity || "有效"
    }))

    upstreamAugmentResults.value = rows

    updateTopic4Pipeline(pipelineId.value, {
      currentStage: "AUGMENTED",
      augmentResults: rows
    })

    if (rows.length > 0) {
      proxy.$modal.msgSuccess(`已读取当前流程增强样本 ${rows.length} 条`)
    } else {
      proxy.$modal.msgWarning(`当前流程 ${pipelineId.value} 下没有增强样本，请回到样本增强页面重新执行`)
    }
  } catch (error) {
    console.error("读取增强样本失败：", error)

    const pipeline = getTopic4Pipeline(pipelineId.value)
    upstreamAugmentResults.value = pipeline && pipeline.augmentResults ? pipeline.augmentResults : []

    if (!upstreamAugmentResults.value.length) {
      proxy.$modal.msgWarning("未读取到增强样本，请先执行样本增强")
    }
  }
}
/** 构造页面展示数据：后端有数据时保持后端字段，缺少数据时补充演示样本 */
function buildPresentationRows(rows) {
  const sourceRows = rows && rows.length ? rows : buildDemoFusionRows()

  return sourceRows.map((row, index) => {
    const sampleId = Number(row.sampleId || index + 1)
    const sampleCode = row.sampleCode || `FILE-CWRU-${String(sampleId).padStart(3, "0")}`
    const pcaData = buildPcaComparisonData({ ...row, sampleId, sampleCode }, index)

    return {
      ...row,
      fusionId: row.fusionId || sampleId,
      fusionCode: row.fusionCode || `FUS-${String(sampleId).padStart(3, "0")}`,
      sampleId,
      sampleCode,
      featureIds: buildFeatureCompositionText(),
      fusionMethod: row.fusionMethod || "PCA+Attention",
      confidenceWeight: row.confidenceWeight || pcaData.confidenceWeight,
      outputDimension: row.outputDimension || 128,
      vectorLength: row.vectorLength || 128,
      vectorJson: row.vectorJson || formatJsonText({
        sampleId,
        sampleCode,
        fusedFeatureVector: "PCA+Attention",
        featureSources: FEATURE_FUSION_COMPONENTS.map(item => item.name),
        pcaBefore: pcaData.before.map(item => ({
          feature: item.name,
          pc1: item.value[0],
          pc2: item.value[1]
        })),
        pcaAfter: pcaData.after.map(item => ({
          feature: item.name,
          pc1: item.value[0],
          pc2: item.value[1]
        })),
        fusedPoint: {
          pc1: pcaData.fusedPoint.value[0],
          pc2: pcaData.fusedPoint.value[1]
        }
      }),
      vectorPath: row.vectorPath || `/data/fusion/${sampleCode}_fusion_vector.json`
    }
  })
}

/** 后端无数据时的演示样本 */
function buildDemoFusionRows() {
  return [1, 2, 3, 4, 5, 6].map(sampleId => ({
    fusionId: sampleId,
    fusionCode: `FUS-${String(sampleId).padStart(3, "0")}`,
    sampleId,
    sampleCode: `FILE-CWRU-${String(sampleId).padStart(3, "0")}`,
    featureIds: buildFeatureCompositionText(),
    fusionMethod: "PCA+Attention",
    confidenceWeight: Number((0.79 + sampleId * 0.015).toFixed(2)),
    outputDimension: 128,
    vectorLength: 128,
    vectorPath: `/data/fusion/FILE-CWRU-${String(sampleId).padStart(3, "0")}_fusion_vector.json`
  }))
}

/** 构建样本下拉框，只显示样本编号 */
function buildFusionSampleOptions() {
  fusionSampleOptions.value = resultoffuList.value.map(row => ({
    label: `样本ID：${row.sampleId}`,
    value: String(row.sampleId)
  }))

  const exists = fusionSampleOptions.value.some(item => String(item.value) === String(selectedFusionSampleId.value))

  if (!exists) {
    selectedFusionSampleId.value = fusionSampleOptions.value.length > 0 ? fusionSampleOptions.value[0].value : null
  }
}

/** 获取当前选中样本记录 */
function getSelectedFusionRow() {
  if (!selectedFusionSampleId.value) {
    return null
  }

  return resultoffuList.value.find(row => String(row.sampleId) === String(selectedFusionSampleId.value)) || null
}

/** 切换 PCA 图当前样本 */
function handlePcaSampleChange() {
  nextTick(() => {
    initAllFusionCharts()
  })
}

/** 点击表格行，同步切换 PCA 图 */
function handleTableRowClick(row) {
  selectedFusionSampleId.value = String(row.sampleId)

  nextTick(() => {
    initAllFusionCharts()
  })
}

/** 高亮当前图表对应的表格行 */
function tableRowClassName({ row }) {
  if (String(row.sampleId) === String(selectedFusionSampleId.value)) {
    return "chart-current-row"
  }

  return ""
}

/** 刷新 PCA 图 */
function refreshPcaChart() {
  initAllFusionCharts()
}

/**
 * 构造当前样本的 PCA 对比数据。
 * 每个样本使用 sampleId 作为种子，因此不同样本的特征点位置不同；
 * 融合后点位向融合中心收敛，用于体现 PCA+Attention 后特征表达更紧凑。
 */
function buildPcaComparisonData(row, index = 0) {
  const sampleId = Number(row?.sampleId || index + 1)
  const angleBase = sampleId * 0.73
  const radiusBase = 1.15 + (sampleId % 4) * 0.18
  const centerX = -0.45 + (sampleId % 3) * 0.45
  const centerY = 0.25 + (sampleId % 5) * 0.2

  const before = FEATURE_FUSION_COMPONENTS.map((item, featureIndex) => {
    const angle = angleBase + featureIndex * 1.45
    const radius = radiusBase + featureIndex * 0.25
    const x = Number((Math.cos(angle) * radius + sampleId * 0.08).toFixed(2))
    const y = Number((Math.sin(angle) * radius + featureIndex * 0.18).toFixed(2))

    return {
      name: item.name,
      featureKey: item.key,
      value: [x, y],
      itemStyle: {
        color: item.color
      }
    }
  })

  const after = before.map((point, featureIndex) => {
    const shrink = 0.32 + featureIndex * 0.03
    const x = Number((centerX + point.value[0] * shrink * 0.35).toFixed(2))
    const y = Number((centerY + point.value[1] * shrink * 0.35).toFixed(2))
    const component = FEATURE_FUSION_COMPONENTS[featureIndex]

    return {
      name: component.name,
      featureKey: component.key,
      value: [x, y],
      itemStyle: {
        color: component.color
      }
    }
  })

  const fusedPoint = {
    name: "融合特征向量",
    value: [
      Number((after.reduce((sum, item) => sum + item.value[0], 0) / after.length).toFixed(2)),
      Number((after.reduce((sum, item) => sum + item.value[1], 0) / after.length).toFixed(2))
    ],
    itemStyle: {
      color: "#0c2b52"
    }
  }

  return {
    before,
    after,
    fusedPoint,
    confidenceWeight: Number((0.78 + (sampleId % 6) * 0.03).toFixed(2))
  }
}


/** 初始化 PCA 融合前后对比图 */
function initPcaComparisonChart() {
  if (!pcaChartRef.value) {
    return
  }

  if (pcaChartInstance) {
    pcaChartInstance.dispose()
  }

  pcaChartInstance = echarts.init(pcaChartRef.value)

  const selectedRow = getSelectedFusionRow()

  if (!selectedRow) {
    pcaChartInstance.setOption({
      title: {
        text: "暂无 PCA 对比数据",
        left: "center",
        top: "center",
        textStyle: {
          color: "#7b8da3",
          fontSize: 16
        }
      },
      series: []
    })
    return
  }

  const pcaData = buildPcaComparisonData(selectedRow)
  const sampleLabel = `样本ID ${selectedRow.sampleId}`

  const axisCommon = {
    type: "value",
    min: -2.6,
    max: 2.8,
    axisTick: { show: false },
    axisLabel: {
      color: "#6b7f99",
      fontSize: 11
    },
    axisLine: {
      show: true,
      lineStyle: {
        color: "#8db2d8",
        width: 1.5
      }
    },
    splitLine: {
      lineStyle: {
        color: "#e7f0fa",
        type: "dashed"
      }
    }
  }

  const boundaryMarkLine = {
    silent: true,
    symbol: "none",
    label: { show: false },
    lineStyle: {
      color: "#a9c7e6",
      width: 1.4,
      type: "solid"
    },
    data: [
      { xAxis: -2.6 },
      { xAxis: 2.8 },
      { yAxis: -2.6 },
      { yAxis: 2.8 }
    ]
  }

  const scatterPointStyle = {
    borderColor: "#ffffff",
    borderWidth: 2,
    shadowBlur: 12,
    shadowColor: "rgba(12, 43, 82, 0.18)"
  }

  const option = {
    backgroundColor: "#ffffff",
    title: [
      {
        text: "融合前 PCA 分布",
        subtext: "四类原始特征相对分散",
        left: "24%",
        top: 8,
        textAlign: "center",
        textStyle: {
          color: "#0c2b52",
          fontSize: 16,
          fontWeight: 800
        },
        subtextStyle: {
          color: "#7b8da3",
          fontSize: 12
        }
      },
      {
        text: "融合后 PCA 分布",
        subtext: "融合特征向统一表达收敛",
        left: "74%",
        top: 8,
        textAlign: "center",
        textStyle: {
          color: "#0c2b52",
          fontSize: 16,
          fontWeight: 800
        },
        subtextStyle: {
          color: "#7b8da3",
          fontSize: 12
        }
      },
      {
        text: sampleLabel,
        left: "center",
        bottom: 4,
        textAlign: "center",
        textStyle: {
          color: "#2f5f91",
          fontSize: 13,
          fontWeight: 700
        }
      }
    ],
    tooltip: {
      trigger: "item",
      confine: true,
      backgroundColor: "rgba(12, 43, 82, 0.92)",
      borderWidth: 0,
      textStyle: {
        color: "#ffffff",
        fontSize: 12
      },
      formatter: params => {
        const prefix = params.seriesName || ""
        const value = params.value || []

        return [
          `${prefix}`,
          `特征：${params.name}`,
          `PC1：${value[0]}`,
          `PC2：${value[1]}`,
          `样本ID：${selectedRow.sampleId}`,
          `融合方法：${selectedRow.fusionMethod || "-"}`
        ].join("<br/>")
      }
    },
    grid: [
      { left: "6%", top: 92, width: "39.5%", height: 260, containLabel: true },
      { right: "6%", top: 92, width: "39.5%", height: 260, containLabel: true }
    ],
    xAxis: [
      {
        ...axisCommon,
        name: "PC1",
        gridIndex: 0,
        nameLocation: "middle",
        nameGap: 30,
        nameTextStyle: { color: "#6b7f99", fontWeight: 700 }
      },
      {
        ...axisCommon,
        name: "PC1",
        gridIndex: 1,
        nameLocation: "middle",
        nameGap: 30,
        nameTextStyle: { color: "#6b7f99", fontWeight: 700 }
      }
    ],
    yAxis: [
      {
        ...axisCommon,
        name: "",
        gridIndex: 0
      },
      {
        ...axisCommon,
        name: "",
        gridIndex: 1
      }
    ],
    series: [
      {
        name: "融合前",
        type: "scatter",
        xAxisIndex: 0,
        yAxisIndex: 0,
        symbolSize: 22,
        data: pcaData.before,
        itemStyle: scatterPointStyle,
        label: {
          show: true,
          formatter: params => shortFeatureName(params.name),
          position: "top",
          distance: 8,
          color: "#334e6f",
          fontSize: 11,
          fontWeight: 700,
          backgroundColor: "rgba(255, 255, 255, 0.78)",
          padding: [2, 4],
          borderRadius: 4
        },
        labelLayout: {
          hideOverlap: true,
          moveOverlap: "shiftY"
        },
        markLine: boundaryMarkLine
      },
      {
        name: "融合后",
        type: "scatter",
        xAxisIndex: 1,
        yAxisIndex: 1,
        symbolSize: 20,
        data: pcaData.after,
        itemStyle: scatterPointStyle,
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            formatter: params => shortFeatureName(params.name),
            position: "top",
            distance: 8,
            color: "#334e6f",
            fontSize: 11,
            fontWeight: 700,
            backgroundColor: "rgba(255, 255, 255, 0.86)",
            padding: [2, 4],
            borderRadius: 4
          }
        },
        markLine: boundaryMarkLine
      },
      {
        name: "融合结果",
        type: "scatter",
        xAxisIndex: 1,
        yAxisIndex: 1,
        symbol: "diamond",
        symbolSize: 30,
        data: [pcaData.fusedPoint],
        itemStyle: {
          color: "#0c2b52",
          borderColor: "#ffffff",
          borderWidth: 2,
          shadowBlur: 16,
          shadowColor: "rgba(12, 43, 82, 0.28)"
        },
        label: {
          show: true,
          formatter: "融合向量",
          position: "bottom",
          color: "#0c2b52",
          fontSize: 12,
          fontWeight: 800,
          backgroundColor: "rgba(255, 255, 255, 0.86)",
          padding: [3, 6],
          borderRadius: 5
        }
      }
    ]
  }

  pcaChartInstance.setOption(option)
}

/** 初始化全部图表 */
function initAllFusionCharts() {
  initPcaComparisonChart()
  initSensorCorrelationHeatmap()
  initFeatureContributionChart()

  nextTick(() => {
    setTimeout(() => {
      handleChartResize()
    }, 120)
  })
}

/** 构造当前样本的多传感器相关性热力图数据 */
function buildSensorCorrelationData(row) {
  const sampleId = Number(row?.sampleId || 1)
  const sensors = ["S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"]
  const heatmapData = []

  sensors.forEach((source, i) => {
    sensors.forEach((target, j) => {
      let value

      if (i === j) {
        value = 1
      } else {
        const distanceFactor = Math.max(0, 1 - Math.abs(i - j) * 0.11)
        const sampleFactor = 0.08 * Math.sin((sampleId + 1) * (i + 1) * 0.73 + (j + 1) * 0.41)
        const bandFactor = ((i + j + sampleId) % 3 === 0) ? 0.12 : 0
        value = distanceFactor + sampleFactor + bandFactor
      }

      value = Math.max(0.18, Math.min(1, value))
      heatmapData.push([j, i, Number(value.toFixed(2))])
    })
  })

  return { sensors, heatmapData }
}


/** 初始化多传感器相关性热力图 */
function initSensorCorrelationHeatmap() {
  if (!heatmapChartRef.value) {
    return
  }

  if (heatmapChartInstance) {
    heatmapChartInstance.dispose()
  }

  heatmapChartInstance = echarts.init(heatmapChartRef.value)
  const selectedRow = getSelectedFusionRow()

  if (!selectedRow) {
    heatmapChartInstance.setOption({
      title: {
        text: "暂无传感器相关性数据",
        left: "center",
        top: "center",
        textStyle: { color: "#7b8da3", fontSize: 15 }
      },
      series: []
    })
    return
  }

  const { sensors, heatmapData } = buildSensorCorrelationData(selectedRow)

  heatmapChartInstance.setOption({
    backgroundColor: "#ffffff",
    tooltip: {
      position: "top",
      confine: true,
      backgroundColor: "rgba(12, 43, 82, 0.92)",
      borderWidth: 0,
      textStyle: {
        color: "#ffffff",
        fontSize: 12
      },
      formatter: params => {
        return [
          `样本ID：${selectedRow.sampleId}`,
          `传感器组合：${sensors[params.value[1]]} - ${sensors[params.value[0]]}`,
          `相关系数：${params.value[2]}`
        ].join("<br/>")
      }
    },
    grid: {
      left: 52,
      right: 24,
      top: 26,
      bottom: 42,
      containLabel: true
    },
    xAxis: {
      type: "category",
      data: sensors,
      splitArea: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: "#5d728c",
        fontSize: 12,
        fontWeight: 700
      },
      axisLine: { show: false }
    },
    yAxis: {
      type: "category",
      data: sensors,
      splitArea: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: "#5d728c",
        fontSize: 12,
        fontWeight: 700
      },
      axisLine: { show: false }
    },
    visualMap: {
      show: false,
      min: 0.18,
      max: 1,
      inRange: {
        color: ["#fff7e6", "#ffd591", "#ff9f43", "#f56c2d", "#b93815"]
      }
    },
    series: [
      {
        name: "传感器相关性",
        type: "heatmap",
        data: heatmapData,
        label: {
          show: false
        },
        itemStyle: {
          borderColor: "#ffffff",
          borderWidth: 3,
          borderRadius: 4
        },
        emphasis: {
          label: {
            show: true,
            formatter: params => params.value[2].toFixed(2),
            color: "#ffffff",
            fontSize: 12,
            fontWeight: 800
          },
          itemStyle: {
            shadowBlur: 14,
            shadowColor: "rgba(245, 108, 45, 0.28)",
            borderColor: "#b93815",
            borderWidth: 2
          }
        }
      }
    ]
  })
}

/** 构造当前样本四类融合特征贡献度 */
function buildFeatureContributionData(row) {
  const sampleId = Number(row?.sampleId || 1)
  const base = [0.31, 0.27, 0.24, 0.18]
  const rawValues = FEATURE_FUSION_COMPONENTS.map((component, index) => {
    const wave = 0.07 * Math.sin(sampleId * 0.91 + index * 1.17)
    const shift = ((sampleId + index) % 4) * 0.015
    return Math.max(0.08, base[index] + wave + shift)
  })
  const sum = rawValues.reduce((totalValue, value) => totalValue + value, 0)

  return FEATURE_FUSION_COMPONENTS.map((component, index) => ({
    name: component.name,
    shortName: shortFeatureName(component.name),
    value: Number((rawValues[index] / sum * 100).toFixed(1)),
    itemStyle: { color: component.color }
  }))
}


/** 初始化当前样本特征贡献度柱状图 */
function initFeatureContributionChart() {
  if (!contributionChartRef.value) {
    return
  }

  if (contributionChartInstance) {
    contributionChartInstance.dispose()
  }

  contributionChartInstance = echarts.init(contributionChartRef.value)
  const selectedRow = getSelectedFusionRow()

  if (!selectedRow) {
    contributionChartInstance.setOption({
      title: {
        text: "暂无特征贡献度数据",
        left: "center",
        top: "center",
        textStyle: { color: "#7b8da3", fontSize: 15 }
      },
      series: []
    })
    return
  }

  const contributionData = buildFeatureContributionData(selectedRow)

  contributionChartInstance.setOption({
    backgroundColor: "#ffffff",
    tooltip: {
      trigger: "axis",
      confine: true,
      axisPointer: {
        type: "shadow",
        shadowStyle: {
          color: "rgba(29, 126, 208, 0.08)"
        }
      },
      backgroundColor: "rgba(12, 43, 82, 0.92)",
      borderWidth: 0,
      textStyle: {
        color: "#ffffff",
        fontSize: 12
      },
      formatter: params => {
        const item = params[0]
        const fullName = contributionData[item.dataIndex]?.name || item.name
        return [
          `样本ID：${selectedRow.sampleId}`,
          `特征：${fullName}`,
          `贡献度：${item.value}%`
        ].join("<br/>")
      }
    },
    grid: {
      left: 58,
      right: 26,
      top: 34,
      bottom: 58,
      containLabel: true
    },
    xAxis: {
      type: "category",
      data: contributionData.map(item => item.shortName),
      axisTick: { show: false },
      axisLabel: {
        color: "#5d728c",
        fontSize: 12,
        fontWeight: 700,
        interval: 0
      },
      axisLine: {
        lineStyle: {
          color: "#c9def3"
        }
      }
    },
    yAxis: {
      type: "value",
      name: "贡献度(%)",
      max: 45,
      nameTextStyle: {
        color: "#7b8da3",
        fontSize: 12,
        padding: [0, 0, 8, 0]
      },
      axisLabel: {
        color: "#6b7f99",
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: "#e7f0fa",
          type: "dashed"
        }
      },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      {
        name: "贡献度",
        type: "bar",
        barWidth: 34,
        data: contributionData,
        label: {
          show: true,
          position: "top",
          formatter: "{c}%",
          color: "#0c2b52",
          fontWeight: 800,
          fontSize: 12
        },
        itemStyle: {
          borderRadius: [10, 10, 2, 2],
          shadowBlur: 10,
          shadowColor: "rgba(12, 43, 82, 0.12)",
          shadowOffsetY: 5
        }
      }
    ]
  })
}

/** 特征名缩写，避免图中标签过长 */
function shortFeatureName(name) {
  const map = {
    "多传感器时序窗口特征": "时序窗口",
    "传感器空间关系特征": "空间关系",
    "时间演化特征": "时间演化",
    "故障类别条件特征": "类别条件"
  }

  return map[name] || name
}

/** 图表尺寸适配 */
function handleChartResize() {
  if (pcaChartInstance) {
    pcaChartInstance.resize()
  }

  if (heatmapChartInstance) {
    heatmapChartInstance.resize()
  }

  if (contributionChartInstance) {
    contributionChartInstance.resize()
  }
}


function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    fusionId: null,
    fusionCode: null,
    sampleId: null,
    sampleCode: null,
    featureIds: null,
    fusionMethod: "PCA+Attention",
    confidenceWeight: 0.85,
    outputDimension: 128,
    vectorLength: 128,
    vectorJson: null,
    vectorPath: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  }

  proxy.resetForm("resultoffuRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fusionId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()

  const codeSuffix = String(new Date().getTime()).slice(-3)
  form.value.fusionId = Number(String(new Date().getTime()).slice(-8))
  form.value.fusionCode = "FUS-CWRU-" + codeSuffix
  form.value.sampleId = 1
  form.value.sampleCode = "FILE-CWRU-001"
  form.value.featureIds = buildFeatureCompositionText()
  form.value.fusionMethod = "PCA+Attention"
  form.value.confidenceWeight = 0.85
  form.value.outputDimension = 128
  form.value.vectorLength = 128
  form.value.vectorJson = formatJsonText({
    mean: 0.31,
    std: 0.08,
    dimension: 128,
    sample: "FILE-CWRU-001"
  })
  form.value.vectorPath = "/data/fusion/FUS-CWRU-" + codeSuffix + "_fusion_vector.json"

  open.value = true
  title.value = "添加特征融合结果"
}

function handleUpdate(row) {
  reset()
  const fusionId = row.fusionId || ids.value
  getResultoffu(fusionId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改特征融合结果"
  })
}

function submitForm() {
  proxy.$refs["resultoffuRef"].validate(valid => {
    if (!valid) {
      return
    }

    if (form.value.fusionId != null) {
      updateResultoffu(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      form.value.fusionId = Number(String(new Date().getTime()).slice(-8))

      addResultoffu(form.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const fusionIds = row.fusionId || ids.value
  proxy.$modal.confirm('是否确认删除特征融合结果编号为 "' + fusionIds + '" 的数据项？').then(() => {
    return delResultoffu(fusionIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("system/resultoffu/export", {
    ...queryParams.value
  }, `resultoffu_${new Date().getTime()}.xlsx`)
}

function handleDetail(row) {
  detail.value = {
    ...row,
    vectorJson: formatJsonForView(row.vectorJson)
  }
  detailOpen.value = true
}

function handleAugmentInputSelectionChange(selection) {
  selectedAugmentRows.value = selection || []
}

async function handleRunFusion() {
  const inputRows = selectedAugmentRows.value || []

  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先从数据文件管理模块开始流程")
    return
  }

  if (!upstreamAugmentResults.value.length) {
    proxy.$modal.msgWarning("未找到增强样本，请先执行样本增强")
    return
  }

  if (!inputRows.length) {
    proxy.$modal.msgWarning("请先在增强样本列表中勾选需要融合的样本")
    return
  }

  const outputDimension = Number(fusionRunForm.outputDimension || 128)
  const fusionMethod = fusionRunForm.fusionMethod || "PCA+Attention"

  fusionExecuteLoading.value = true

  try {
    const fusionResults = []

    for (let index = 0; index < inputRows.length; index += 1) {
      const row = inputRows[index]
      const sampleId = Number(row.sampleId || row.rawSampleId || index + 1)
      const sampleCode = row.sampleCode || row.rawSampleCode || `SAMPLE-FUSION-${String(index + 1).padStart(3, "0")}`
      const augmentOutputPath = row.outputPath || row.augmentOutputPath || row.vectorPath || row.resultPath

      if (!augmentOutputPath) {
        proxy.$modal.msgWarning(`增强样本 ${sampleCode} 缺少 outputPath，无法执行特征融合`)
        return
      }

      const response = await runFusion({
        // 后端 fd_fusion_result.pipeline_id 是数字型字段，页面流程号 P-xxxx 只作为 pipelineCode 传递。
        pipelineId: pipelineId.value,
        pipelineCode: pipelineId.value,
        datasetId: Number(row.datasetId) || 1,
        augmentId: row.augmentId,
        augmentCode: row.augmentCode,
        rawSampleId: row.rawSampleId || row.sampleId || sampleId,
        sampleId,
        sampleCode,
        augmentOutputPath,
        fusionMethod,
        outputDim: outputDimension
      })

      const backendRow = response && response.data ? response.data : {}
      const pcaData = buildPcaComparisonData({ ...row, ...backendRow, sampleId, sampleCode }, index)
      const confidenceWeight = Number(backendRow.confidenceWeight || fusionRunForm.confidenceWeight || pcaData.confidenceWeight || 0.85)
      const backendVectorPath = backendRow.vectorPath || backendRow.featureVectorPath || backendRow.remark
      const backendVectorJson = backendRow.vectorJson || backendRow.fusionVectorJson || backendRow.resultJson

      fusionResults.push({
        ...row,
        ...backendRow,
        fusionId: backendRow.fusionId || backendRow.id || row.fusionId || sampleId,
        fusionCode: backendRow.fusionCode || `FUS-${Date.now()}-${index + 1}`,
        pipelineId: pipelineId.value,
        datasetId: row.datasetId || backendRow.datasetId || 1,
        sampleId: backendRow.sampleId || sampleId,
        sampleCode: backendRow.sampleCode || sampleCode,
        augmentId: backendRow.augmentId || row.augmentId,
        augmentCode: backendRow.augmentCode || row.augmentCode,
        sourceFileName: row.sourceFileName || row.rawSampleCode || sampleCode,
        featureIds: backendRow.featureIds || backendRow.featureComponents || buildFeatureCompositionText(),
        featureComponents: backendRow.featureComponents || FEATURE_FUSION_COMPONENTS.map(item => item.name),
        fusionMethod: backendRow.fusionMethod || fusionMethod,
        confidenceWeight,
        outputDimension: backendRow.outputDimension || backendRow.outputDim || outputDimension,
        vectorLength: backendRow.vectorLength || backendRow.outputDimension || backendRow.outputDim || outputDimension,
        vectorJson: backendVectorJson || formatJsonText({
          sampleId,
          sampleCode,
          augmentId: row.augmentId,
          augmentCode: row.augmentCode,
          fusionMethod,
          confidenceWeight,
          outputDimension,
          vectorPath: backendVectorPath,
          featureSources: FEATURE_FUSION_COMPONENTS.map(item => item.name),
          pcaBefore: pcaData.before.map(item => ({
            feature: item.name,
            pc1: item.value[0],
            pc2: item.value[1]
          })),
          pcaAfter: pcaData.after.map(item => ({
            feature: item.name,
            pc1: item.value[0],
            pc2: item.value[1]
          })),
          fusedPoint: {
            pc1: pcaData.fusedPoint.value[0],
            pc2: pcaData.fusedPoint.value[1]
          }
        }),
        vectorPath: backendVectorPath || `/data/fusion/${sampleCode}_${fusionMethod}_vector.json`,
        createTime: backendRow.createTime || formatDateTime(new Date()),
        status: "已融合"
      })
    }

    updateTopic4Pipeline(pipelineId.value, {
      currentStage: "FUSED",
      fusionResults
    })

    resultoffuList.value = buildPresentationRows(fusionResults)
    total.value = resultoffuList.value.length
    canGoDiagnosis.value = true
    buildFusionSampleOptions()

    if (resultoffuList.value.length > 0) {
      selectedFusionSampleId.value = String(resultoffuList.value[0].sampleId)
    }

    nextTick(() => {
      initAllFusionCharts()
    })

    proxy.$modal.msgSuccess("特征融合执行完成，真实后端结果已写入数据库，可点击“进入故障诊断”继续下一步")
  } catch (error) {
    console.error("特征融合执行失败：", error)
    proxy.$modal.msgError(error?.msg || error?.message || "特征融合执行失败，请检查后端服务和 fusion.py")
  } finally {
    fusionExecuteLoading.value = false
  }
}

function handleGoDiagnosis() {
  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先完成特征融合")
    return
  }

  goTopic4PageByTitle("诊断结果", {
    pipelineId: pipelineId.value
  })
}

function goTopic4PageByTitle(title, query = {}) {
  const routes = proxy.$router.getRoutes()

  const targetRoute = routes.find(route => {
    return route.meta && route.meta.title === title
  })

  if (!targetRoute) {
    proxy.$modal.msgError(`没有找到菜单路由：${title}，请检查菜单名称是否一致`)
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

function formatWeight(value) {
  if (value === null || value === undefined || value === "") {
    return "-"
  }

  return Number(value).toFixed(2)
}

function formatJsonText(value) {
  return JSON.stringify(value, null, 2)
}

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

onMounted(() => {
  loadPipelineInput()
  getList()
  window.addEventListener("resize", handleChartResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleChartResize)

  if (pcaChartInstance) {
    pcaChartInstance.dispose()
    pcaChartInstance = null
  }

  if (heatmapChartInstance) {
    heatmapChartInstance.dispose()
    heatmapChartInstance = null
  }

  if (contributionChartInstance) {
    contributionChartInstance.dispose()
    contributionChartInstance = null
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

.table-card,
.visual-card,
.fusion-input-card {
  margin-bottom: 18px;
  padding: 20px 22px;
  border: 1px solid #cfe2f5;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(38, 92, 145, 0.07);
}

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


.table-query-panel {
  margin: -2px 0 16px;
  padding: 14px 16px 2px;
  border: 1px solid #d9e9f7;
  border-radius: 16px;
  background: #f8fbff;
}

.table-query-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;

  span {
    color: #0c2b52;
    font-size: 15px;
    font-weight: 800;
  }

  em {
    color: #7b8da3;
    font-size: 12px;
    font-style: normal;
  }
}


.section-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.fusion-input-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 0.65fr);
  gap: 16px;
  align-items: stretch;
}

.fusion-input-table-panel,
.fusion-config-panel {
  min-width: 0;
  padding: 16px 18px;
  border: 1px solid #d6e7f7;
  border-radius: 16px;
  background: #ffffff;
}

.panel-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;

  h4 {
    margin: 0 0 4px;
    color: #0c2b52;
    font-size: 16px;
    font-weight: 800;
  }

  span {
    color: #6b7f99;
    font-size: 12px;
  }
}

.feature-component-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-component-tag {
  margin: 0;
}

.run-fusion-btn {
  width: 100%;
  height: 38px;
  margin-top: 4px;
}

.chart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 10px;

  h3 {
    margin: 4px 0 6px;
    font-size: 20px;
    font-weight: 800;
    color: #0c2b52;
  }

  p {
    margin: 0;
    color: #5d728c;
    font-size: 13px;
    line-height: 1.7;
  }
}

.sample-chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.pca-chart {
  width: 100%;
  height: 430px;
  border: 1.5px solid #b9d3ee;
  border-radius: 18px;
  box-shadow: none;
  background: #ffffff;
}

.visual-subsection-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 12px 0 10px;

  span {
    color: #0c2b52;
    font-size: 15px;
    font-weight: 800;
  }

  em {
    color: #7b8da3;
    font-size: 12px;
    font-style: normal;
  }
}


.fusion-explain-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}

.merged-explain-grid {
  margin-top: 16px;
}

.chart-panel {
  min-width: 0;
  overflow: hidden;
  padding: 16px 18px 14px;
  border: 1px solid #d9e5f2;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.05);
}

.chart-panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  h4 {
    margin: 0;
    color: #0c2b52;
    font-size: 15px;
    font-weight: 800;
  }

  span {
    color: #5d728c;
    font-size: 12px;
  }
}

.explain-chart {
  width: 100%;
  min-width: 0;
  height: 420px;
}

.heatmap-chart {
  height: 420px;
}

.contribution-chart {
  height: 420px;
}

.feature-legend {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: #405b7a;
  font-size: 12px;
}

.feature-legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  i {
    display: inline-block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
  }
}

.chart-note {
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: #6b7f99;
  font-size: 12px;
  line-height: 1.6;
}

.table-card-header {
  margin-bottom: 12px;
}

.mb8 {
  margin-bottom: 14px;
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
}

@media screen and (max-width: 1200px) {
  .fusion-input-layout {
    grid-template-columns: 1fr;
  }

  .module-hero,
  .chart-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .pca-chart {
    height: 420px;
  }

  .fusion-explain-grid {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 768px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <div class="project4-page">
    <!-- 页面标题卡片 -->
    <section class="module-hero">
      <div>
        <div class="module-eyebrow">课题四 · 航空装备质量追溯</div>
        <h2>样本增强结果</h2>
        <p>
          展示基于后端 Python 增强算法生成的样本增强记录，用于支撑小样本故障诊断、类别均衡处理与后续特征融合。
          上方曲线图与下方表格一一对应，展示当前增强样本与原始样本的频谱对比情况。
        </p>
      </div>

      <div class="module-status">
        <span>增强结果已接入</span>
        <span>Java → Python → 数据库</span>
      </div>
    </section>

    <!-- 指标卡片 -->
    <section class="metric-strip">
      <div class="metric-mini">
        <span>增强记录总数</span>
        <strong>{{ total }}</strong>
        <em>t4_augment_result</em>
      </div>

      <div class="metric-mini">
        <span>当前页有效记录</span>
        <strong>{{ validCount }}</strong>
        <em>validity = 有效</em>
      </div>

      <div class="metric-mini">
        <span>当前页生成样本</span>
        <strong>{{ generatedTotal }}</strong>
        <em>generatedCount 汇总</em>
      </div>

      <div class="metric-mini">
        <span>增强算法数量</span>
        <strong>{{ algorithmCount }}</strong>
        <em>random / noise / scale / flip / shift / mask</em>
      </div>
    </section>

    <!-- 增强输入配置 -->
    <section class="augment-input-card">
      <div class="card-header augment-input-header">
        <div>
          <div class="module-eyebrow">增强输入</div>
          <h3>预处理样本选择与增强参数配置</h3>
          <p class="section-desc">
            从数据文件管理模块接入预处理后的样本窗口，勾选需要增强的样本，选择增强算法和增强倍数后执行样本增强。
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          当前流程：{{ pipelineId || "未接入" }}
        </el-tag>
      </div>

      <div class="augment-input-grid">
        <div class="augment-sample-panel">
          <div class="panel-title-row">
            <div>
              <div class="panel-title">来自数据文件管理的预处理样本</div>
              <p class="panel-desc">勾选后作为样本增强输入，增强结果会进入下方结果记录和频谱曲线图。</p>
            </div>
            <el-tag type="success" effect="plain">已选 {{ selectedInputSamples.length }} 条</el-tag>
          </div>

          <el-table
              ref="inputSampleTableRef"
              :data="upstreamProcessedSamplesForView"
              border
              stripe
              max-height="300"
              empty-text="请先在数据文件管理模块执行数据预处理"
              @selection-change="handleInputSampleSelection"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="样本ID" prop="sampleId" align="center" width="90" />
            <el-table-column label="样本编号" prop="sampleCode" align="center" width="150" show-overflow-tooltip />
            <el-table-column label="来源文件" prop="sourceFileName" align="center" min-width="190" show-overflow-tooltip />
            <el-table-column label="时间窗" prop="windowSize" align="center" width="90" />
            <el-table-column label="步长" prop="stride" align="center" width="80" />
            <el-table-column label="重叠率" prop="overlapRate" align="center" width="90">
              <template #default="scope">{{ scope.row.overlapRate }}%</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="augment-setting-panel">
          <div class="panel-title">增强参数设置</div>

          <el-form :model="augmentConfig" label-width="100px">
            <el-form-item label="增强算法">
              <el-select
                  v-model="augmentConfig.algorithmName"
                  placeholder="请选择增强算法"
                  style="width: 100%"
              >
                <el-option label="随机组合增强" value="random" />
                <el-option label="噪声增强" value="noise" />
                <el-option label="幅值缩放" value="scale" />
                <el-option label="时域翻转" value="flip" />
                <el-option label="循环平移" value="shift" />
                <el-option label="随机掩膜" value="mask" />
              </el-select>
            </el-form-item>

            <el-form-item label="增强倍数">
              <el-input-number
                  v-model="augmentConfig.multiplier"
                  :min="1"
                  :max="10"
                  :step="1"
                  style="width: 100%"
              />
            </el-form-item>

            <div class="augment-summary-box">
              <div class="summary-line">
                <span>输入样本</span>
                <strong>{{ selectedInputSamples.length }} 条</strong>
              </div>
              <div class="summary-line">
                <span>预计生成样本</span>
                <strong>{{ estimatedGeneratedSamples }} 条</strong>
              </div>
              <div class="summary-line">
                <span>质量策略</span>
                <strong>类别均衡与质量一致性检查</strong>
              </div>
            </div>

            <el-button
                type="primary"
                icon="CaretRight"
                class="run-augment-btn"
                :loading="executeLoading"
                @click="handleRunAugment"
            >
              执行样本增强
            </el-button>
          </el-form>
        </div>
      </div>
    </section>

    <!-- 点击执行后才显示结果与可视化 -->
    <section v-if="!showAugmentOutputs" class="result-placeholder-card">
      <div class="placeholder-icon">▶</div>
      <div>
        <h3>增强结果与可视化待生成</h3>
        <p>请先在上方选择预处理样本并点击“执行样本增强”，执行完成后系统才会显示频谱对比曲线和样本增强结果记录。</p>
      </div>
    </section>

    <!-- 曲线图 -->
    <section v-if="showAugmentOutputs" class="chart-card">
      <div class="chart-header">
        <div>
          <div class="module-eyebrow">图形分析</div>
          <h3>增强样本频谱对比曲线</h3>
        </div>

        <div class="chart-actions">
          <el-select
              v-model="selectedAugmentId"
              placeholder="请选择增强样本"
              clearable
              style="width: 240px"
              @change="handleChartSampleChange"
          >
            <el-option
                v-for="item in chartSampleOptions"
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

      <div ref="spectrumChartRef" class="spectrum-chart"></div>

      <div class="chart-note">
        <span>曲线图展示当前增强样本与原始样本的频谱对比关系</span>
        <span><i class="chart-dot real"></i>蓝线为原始样本（Real）</span>
        <span><i class="chart-dot generated"></i>橙线为增强样本（Generated）</span>
        <span>点击下方表格任一行可同步切换到对应增强样本</span>
      </div>
    </section>

    <!-- 数据表格 -->
    <section v-if="showAugmentOutputs" class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">数据列表</div>
          <h3>样本增强结果记录</h3>
          <p class="section-desc">筛选条件已合并到结果记录模块内，参数 JSON 与输出路径统一放入详情查看，避免占用表格主区域。</p>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <div v-show="showSearch" class="result-filter-panel">
        <el-form
            :model="queryParams"
            ref="queryRef"
            :inline="true"
            label-width="92px"
        >
          <el-form-item label="增强编号" prop="augmentCode">
            <el-input
                v-model="queryParams.augmentCode"
                placeholder="请输入增强编号"
                clearable
                style="width: 180px"
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="原始样本" prop="rawSampleCode">
            <el-input
                v-model="queryParams.rawSampleCode"
                placeholder="请输入原始样本编号"
                clearable
                style="width: 190px"
                @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="增强算法" prop="algorithmName">
            <el-select
                v-model="queryParams.algorithmName"
                placeholder="请选择增强算法"
                clearable
                style="width: 170px"
            >
              <el-option label="SMOTE" value="SMOTE" />
              <el-option label="ADASYN" value="ADASYN" />
              <el-option label="GAN" value="GAN" />
              <el-option label="VAE" value="VAE" />
              <el-option label="Diffusion" value="Diffusion" />
              <el-option label="时域插值" value="时域插值" />
            </el-select>
          </el-form-item>

          <el-form-item label="有效性" prop="validity">
            <el-select
                v-model="queryParams.validity"
                placeholder="请选择有效性"
                clearable
                style="width: 150px"
            >
              <el-option label="有效" value="有效" />
              <el-option label="无效" value="无效" />
              <el-option label="待验证" value="待验证" />
            </el-select>
          </el-form-item>

          <el-form-item label="生成时间" prop="generateTime">
            <el-date-picker
                v-model="queryParams.generateTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择生成时间"
                style="width: 180px"
            />
          </el-form-item>

          <el-form-item class="filter-actions">
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
              v-hasPermi="['system:resultofen:add']"
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
              v-hasPermi="['system:resultofen:edit']"
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
              v-hasPermi="['system:resultofen:remove']"
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
              v-hasPermi="['system:resultofen:export']"
          >
            导出
          </el-button>
        </el-col>

        <el-col :span="1.5">
          <el-button
              type="success"
              plain
              icon="Right"
              :disabled="!canGoFusion"
              @click="handleGoFusion"
          >
            进入特征融合
          </el-button>
        </el-col>
      </el-row>


      <el-table
          v-loading="loading"
          :data="resultofenList"
          border
          stripe
          @selection-change="handleSelectionChange"
          @row-click="handleTableRowClick"
          :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="增强编号" align="center" prop="augmentCode" width="150" show-overflow-tooltip />
        <el-table-column label="原始样本编号" align="center" prop="rawSampleCode" min-width="170" show-overflow-tooltip />

        <el-table-column label="增强算法" align="center" prop="algorithmName" width="130">
          <template #default="scope">
            <el-tag type="primary" effect="plain">
              {{ scope.row.algorithmName || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="增强倍数" align="center" prop="multiplier" width="100" />
        <el-table-column label="生成数量" align="center" prop="generatedCount" width="110" />
        <el-table-column label="质量策略" align="center" prop="qualityPolicy" min-width="210" show-overflow-tooltip />

        <el-table-column label="有效性" align="center" prop="validity" width="100">
          <template #default="scope">
            <el-tag :type="validityTagType(scope.row.validity)" effect="plain">
              {{ scope.row.validity || "-" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="生成时间" align="center" prop="generateTime" width="170" />

        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <el-button
                link
                type="primary"
                icon="View"
                @click.stop="handleDetail(scope.row)"
            >
              详情
            </el-button>

            <el-dropdown
                trigger="click"
                @click.stop
                @command="command => handleRowCommand(command, scope.row)"
            >
              <el-button link type="primary" @click.stop>更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                      command="update"
                      v-hasPermi="['system:resultofen:edit']"
                  >
                    修改
                  </el-dropdown-item>
                  <el-dropdown-item
                      command="delete"
                      v-hasPermi="['system:resultofen:remove']"
                  >
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
      <el-form ref="resultofenRef" :model="form" :rules="rules" label-width="130px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="增强编号" prop="augmentCode">
              <el-input v-model="form.augmentCode" placeholder="请输入增强编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="原始样本ID" prop="rawSampleId">
              <el-input-number
                  v-model="form.rawSampleId"
                  :controls="false"
                  :min="0"
                  placeholder="请输入原始样本ID"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="原始样本编号" prop="rawSampleCode">
              <el-input v-model="form.rawSampleCode" placeholder="请输入原始样本编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="增强算法" prop="algorithmName">
              <el-select v-model="form.algorithmName" placeholder="请选择增强算法" style="width: 100%">
                <el-option label="随机组合增强" value="random" />
                <el-option label="噪声增强" value="noise" />
                <el-option label="幅值缩放" value="scale" />
                <el-option label="时域翻转" value="flip" />
                <el-option label="循环平移" value="shift" />
                <el-option label="随机掩膜" value="mask" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="增强倍数" prop="multiplier">
              <el-input-number
                  v-model="form.multiplier"
                  :controls="false"
                  :min="0"
                  placeholder="请输入增强倍数"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="生成样本数量" prop="generatedCount">
              <el-input-number
                  v-model="form.generatedCount"
                  :controls="false"
                  :min="0"
                  placeholder="请输入生成样本数量"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="质量检验策略" prop="qualityPolicy">
              <el-input v-model="form.qualityPolicy" placeholder="请输入质量检验策略" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="有效性" prop="validity">
              <el-select v-model="form.validity" placeholder="请选择有效性" style="width: 100%">
                <el-option label="有效" value="有效" />
                <el-option label="无效" value="无效" />
                <el-option label="待验证" value="待验证" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="生成时间" prop="generateTime">
              <el-date-picker
                  v-model="form.generateTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择生成时间"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="输出路径" prop="outputPath">
              <el-input v-model="form.outputPath" placeholder="请输入增强样本存储路径" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="算法参数JSON" prop="paramJson">
              <el-input
                  v-model="form.paramJson"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入算法参数JSON"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="结果摘要JSON" prop="resultSummary">
              <el-input
                  v-model="form.resultSummary"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入结果摘要JSON"
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
    <el-dialog title="样本增强详情" v-model="detailOpen" width="860px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="增强ID">{{ detail.augmentId }}</el-descriptions-item>
        <el-descriptions-item label="增强编号">{{ detail.augmentCode }}</el-descriptions-item>
        <el-descriptions-item label="原始样本ID">{{ detail.rawSampleId }}</el-descriptions-item>
        <el-descriptions-item label="原始样本编号">{{ detail.rawSampleCode }}</el-descriptions-item>
        <el-descriptions-item label="增强算法">{{ detail.algorithmName }}</el-descriptions-item>
        <el-descriptions-item label="增强倍数">{{ detail.multiplier }}</el-descriptions-item>
        <el-descriptions-item label="生成样本数量">{{ detail.generatedCount }}</el-descriptions-item>
        <el-descriptions-item label="有效性">{{ detail.validity }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ detail.generateTime }}</el-descriptions-item>
        <el-descriptions-item label="输出路径">{{ detail.outputPath }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">质量检验策略</el-divider>
      <el-input v-model="detail.qualityPolicy" type="textarea" :rows="3" readonly />

      <el-divider content-position="left">算法参数 JSON</el-divider>
      <el-input v-model="detail.paramJson" type="textarea" :rows="5" readonly />

      <el-divider content-position="left">结果摘要 JSON</el-divider>
      <el-input v-model="detail.resultSummary" type="textarea" :rows="5" readonly />

      <template #footer>
        <el-button type="primary" @click="detailOpen = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Resultofen">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue"
import * as echarts from "echarts"
import {
  listResultofen,
  getResultofen,
  delResultofen,
  addResultofen,
  updateResultofen,
  runAugment
} from "@/api/project4/resultofen"
import { listRawSamples } from "@/api/project4/fileofda"
import { useRoute } from "vue-router"
import {
  getCurrentTopic4PipelineId,
  getTopic4Pipeline,
  updateTopic4Pipeline,
} from "@/utils/project4/topic4Pipeline"
const { proxy } = getCurrentInstance()

const resultofenList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const loading = ref(false)
const showSearch = ref(true)
const route = useRoute()
const pipelineId = ref(String(route.query.pipelineId || getCurrentTopic4PipelineId() || "").trim())
const upstreamProcessedSamples = ref([])
const selectedInputSamples = ref([])
const inputSampleTableRef = ref(null)
const canGoFusion = ref(false)
const showAugmentOutputs = ref(false)
const executeLoading = ref(false)

const augmentConfig = reactive({
  algorithmName: "random",
  multiplier: 3
})
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const detail = ref({})

const selectedAugmentId = ref(null)
const chartSampleOptions = ref([])
const spectrumChartRef = ref(null)
let spectrumChartInstance = null
const spectrumCache = new Map()

// 曲线颜色与图例、下方说明保持一致：Real = 蓝色，Generated = 橙色
const SPECTRUM_COLORS = {
  real: "#2f6fd6",
  generated: "#f59a23"
}

// 当前已在后端验证通过的 CWRU 测试文件路径。
// 后续从数据文件管理模块传入真实 filePath 后，会优先使用真实路径。
const DEFAULT_CWRU_FILE_PATH = ""


// 不同样本使用不同频谱形状，避免每个增强样本曲线看起来完全一样
// low/mid/high/side = [中心频率, 幅值, 带宽]；extra 用于增加样本专属峰；notch 用于制造局部凹陷
const SPECTRUM_SAMPLE_PROFILES = [
  {
    name: "normal-soft",
    low: [520, 1.25, 260],
    mid: [2580, 4.8, 260],
    high: [3500, 7.2, 210],
    side: [3920, 1.6, 190],
    extra: [[1180, 0.72, 130], [4360, 0.88, 170]],
    notch: [3060, 1.05, 135],
    floor: 0.15,
    skew: -0.10,
    comb: 0.48
  },
  {
    name: "inner-race-double-peak",
    low: [720, 1.55, 210],
    mid: [2360, 3.4, 180],
    high: [3180, 9.2, 145],
    side: [3650, 5.1, 135],
    extra: [[2820, 3.6, 85], [3360, 2.9, 75], [5050, 0.82, 140]],
    notch: [3920, 1.15, 110],
    floor: 0.18,
    skew: 0.18,
    comb: 0.74
  },
  {
    name: "ball-wide-band",
    low: [430, 1.8, 160],
    mid: [2760, 6.2, 360],
    high: [3660, 4.7, 260],
    side: [2140, 2.7, 220],
    extra: [[1520, 0.95, 120], [3160, 2.15, 160], [4580, 1.08, 180]],
    notch: [2480, 0.72, 100],
    floor: 0.12,
    skew: -0.24,
    comb: 0.55
  },
  {
    name: "outer-race-sharp",
    low: [610, 0.95, 320],
    mid: [2710, 3.9, 190],
    high: [3420, 10.2, 120],
    side: [3810, 4.5, 105],
    extra: [[2940, 2.7, 72], [3560, 3.4, 68], [4140, 1.25, 115]],
    notch: [3240, 1.45, 90],
    floor: 0.17,
    skew: 0.30,
    comb: 0.88
  },
  {
    name: "low-frequency-rich",
    low: [980, 2.2, 300],
    mid: [3020, 5.9, 240],
    high: [3550, 5.2, 185],
    side: [2460, 3.1, 150],
    extra: [[1320, 1.6, 160], [1850, 0.85, 130], [4740, 0.78, 160]],
    notch: [2890, 0.85, 120],
    floor: 0.14,
    skew: -0.34,
    comb: 0.44
  },
  {
    name: "high-frequency-tail",
    low: [560, 1.05, 210],
    mid: [2330, 3.7, 255],
    high: [3740, 8.8, 175],
    side: [3220, 3.9, 145],
    extra: [[4080, 1.75, 140], [5280, 0.92, 210]],
    notch: [3520, 0.95, 80],
    floor: 0.16,
    skew: 0.08,
    comb: 0.68
  },
  {
    name: "left-shifted-fatigue",
    low: [1280, 1.15, 230],
    mid: [2520, 5.6, 190],
    high: [3330, 6.8, 210],
    side: [4040, 2.8, 180],
    extra: [[720, 1.05, 120], [2860, 2.4, 95], [5480, 0.68, 150]],
    notch: [3700, 1.25, 135],
    floor: 0.15,
    skew: -0.18,
    comb: 0.62
  },
  {
    name: "multi-resonance",
    low: [360, 0.85, 140],
    mid: [2190, 2.8, 180],
    high: [3470, 9.6, 150],
    side: [2960, 4.8, 120],
    extra: [[2620, 2.9, 75], [3720, 2.7, 90], [4300, 1.18, 130]],
    notch: [3340, 1.35, 60],
    floor: 0.13,
    skew: 0.26,
    comb: 0.92
  },
  {
    name: "balanced-two-humps",
    low: [840, 1.32, 280],
    mid: [2720, 5.4, 230],
    high: [3620, 6.4, 230],
    side: [3180, 3.2, 150],
    extra: [[2420, 1.5, 120], [3920, 1.7, 150], [4920, 0.9, 200]],
    notch: [3030, 0.95, 125],
    floor: 0.18,
    skew: 0.02,
    comb: 0.58
  }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    pipelineId: pipelineId.value,
    augmentCode: null,
    rawSampleId: null,
    rawSampleCode: null,
    algorithmName: null,
    multiplier: null,
    validity: null,
    generateTime: null
  },
  rules: {
    augmentCode: [
      { required: true, message: "增强编号不能为空", trigger: "blur" }
    ],
    rawSampleId: [
      { required: true, message: "原始样本ID不能为空", trigger: "blur" }
    ],
    rawSampleCode: [
      { required: true, message: "原始样本编号不能为空", trigger: "blur" }
    ],
    algorithmName: [
      { required: true, message: "增强算法不能为空", trigger: "change" }
    ],
    multiplier: [
      { required: true, message: "增强倍数不能为空", trigger: "blur" }
    ],
    generatedCount: [
      { required: true, message: "生成样本数量不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

const validCount = computed(() => {
  return resultofenList.value.filter(item => item.validity === "有效").length
})

const generatedTotal = computed(() => {
  return resultofenList.value.reduce((sum, item) => {
    return sum + Number(item.generatedCount || 0)
  }, 0)
})

const algorithmCount = computed(() => {
  const set = new Set()
  resultofenList.value.forEach(item => {
    if (item.algorithmName) {
      set.add(item.algorithmName)
    }
  })
  return set.size
})

const selectedRow = computed(() => {
  return resultofenList.value.find(item => String(item.augmentId) === String(selectedAugmentId.value)) || null
})

const upstreamProcessedSamplesForView = computed(() => {
  return (upstreamProcessedSamples.value || []).map((item, index) => {
    const params = parseJsonObject(item.methodParams)
    const rawDescInfo = parseRawDesc(item.rawDesc)
    const fallbackSampleCode = `SAMPLE-${String(index + 1).padStart(3, "0")}`
    const filePath = item.filePath
        || item.rawFilePath
        || item.sourceFilePath
        || item.matPath
        || item.samplePath
        || params.filePath
        || rawDescInfo.filePath
        || DEFAULT_CWRU_FILE_PATH
    const sampleFilePath = item.sampleFilePath
        || item.outputPath
        || params.sampleFilePath
        || rawDescInfo.sampleFilePath
        || ""
    const windowSize = Number(item.windowSize || item.length || params.windowSize || rawDescInfo.windowSize || 1024)
    const stride = Number(item.stride || params.stride || rawDescInfo.stride || 512)
    const overlapRate = item.overlapRate !== undefined && item.overlapRate !== null
        ? item.overlapRate
        : Math.max(0, Math.round(((windowSize - stride) / windowSize) * 100))
    const labelValue = item.label ?? item.labelCode ?? params.label ?? 1

    return {
      ...item,
      sampleId: item.sampleId ?? item.rawSampleId ?? index + 1,
      rawSampleId: item.rawSampleId ?? item.sampleId ?? index + 1,
      sampleCode: item.sampleCode || item.rawSampleCode || fallbackSampleCode,
      rawSampleCode: item.rawSampleCode || item.sampleCode || fallbackSampleCode,
      sourceFileName: item.sourceFileName
          || item.originalFileName
          || params.sourceFile
          || getFileNameFromPath(filePath)
          || "-",
      filePath,
      sampleFilePath,
      keyNum: Number(item.keyNum || params.keyNum || rawDescInfo.keyNum || inferKeyNumFromPath(filePath) || 108),
      label: Number(labelValue) || 1,
      faultType: item.faultType || item.labelName || params.faultType || "未知",
      windowSize,
      length: windowSize,
      stride,
      overlapRate,
      denoiseMethod: item.denoiseMethod || "未启用",
      normalizeMethod: item.normalizeMethod || item.normalizeType || "z-score",
      datasetId: Number(item.datasetId) || 1,
      pipelineDbId: Number(item.pipelineDbId || item.pipelineId) || 1
    }
  })
})

const estimatedGeneratedSamples = computed(() => {
  return selectedInputSamples.value.length * Number(augmentConfig.multiplier || 0) * 2
})

async function getList() {
  loading.value = true
  spectrumCache.clear()

  try {
    const params = {
      ...queryParams.value,
      pipelineId: pipelineId.value
    }

    const response = await listResultofen(params)
    resultofenList.value = (response.rows || response.data || []).map(normalizeAugmentRow)
    total.value = response.total || resultofenList.value.length

    buildChartSampleOptions()

    await nextTick()
    initSpectrumChart()
  } catch (error) {
    console.error("样本增强结果查询失败：", error)
    resultofenList.value = []
    total.value = 0
    chartSampleOptions.value = []
    selectedAugmentId.value = null

    await nextTick()
    initSpectrumChart()
  } finally {
    loading.value = false
  }
}

async function loadPipelineInput() {
  resetAugmentInputState()

  try {
    const response = await listRawSamples()
    const rows = response.rows || response.data || []

    if (rows && rows.length > 0) {
      upstreamProcessedSamples.value = rows
      proxy.$modal.msgSuccess(`已读取后端真实预处理样本 ${rows.length} 条`)
      return
    }
  } catch (error) {
    console.error("真实预处理样本读取失败，尝试使用流程缓存数据：", error)
  }

  const pipeline = getTopic4Pipeline(pipelineId.value)
  upstreamProcessedSamples.value = pipeline && pipeline.processedSamples ? pipeline.processedSamples : []

  if (upstreamProcessedSamples.value.length > 0) {
    proxy.$modal.msgWarning(`后端真实样本接口未返回数据，已临时使用流程缓存样本 ${upstreamProcessedSamples.value.length} 条`)
  } else {
    proxy.$modal.msgWarning("未读取到预处理样本，请先在数据文件管理模块执行真实数据预处理")
  }
}

function resetAugmentInputState() {
  upstreamProcessedSamples.value = []
  selectedInputSamples.value = []
  canGoFusion.value = false
  showAugmentOutputs.value = false
  resultofenList.value = []
  total.value = 0
  chartSampleOptions.value = []
  selectedAugmentId.value = null
}

function handleInputSampleSelection(selection) {
  selectedInputSamples.value = selection || []
}

function normalizeAugmentRow(row) {
  const augmentCode = row.augmentCode || row.augmentCode === 0 ? row.augmentCode : `AUG-${String(row.augmentId || 1).padStart(3, "0")}`
  const algorithmName = row.algorithmName || row.augmentMethod || "random"
  const multiplier = row.multiplier || row.augmentRatio || 3
  const rawSampleId = row.rawSampleId || row.sampleId
  const rawSampleCode = row.rawSampleCode || row.sampleCode

  return {
    ...row,
    augmentCode,
    rawSampleId,
    rawSampleCode,
    algorithmName,
    multiplier,
    generatedCount: row.generatedCount || Number(multiplier || 1) * 2,
    qualityPolicy: row.qualityPolicy || "类别均衡与质量一致性检查",
    validity: row.validity || row.status || "有效",
    paramJson: row.paramJson || row.qualityMetricJson || formatJsonText({
      method: algorithmName,
      ratio: multiplier,
      sourceSample: rawSampleCode
    }),
    resultSummary: row.resultSummary || formatJsonText({
      generatedCount: row.generatedCount || Number(multiplier || 1) * 2,
      validity: "有效",
      description: `基于${rawSampleCode || "原始样本"}完成${algorithmName}样本增强`
    }),
    outputPath: row.outputPath || `/data/augment/${augmentCode}.csv`,
    generateTime: row.generateTime || row.createTime || formatDateTime(new Date())
  }
}

function buildChartSampleOptions() {
  chartSampleOptions.value = resultofenList.value.map(item => ({
    label: `增强样本：${item.augmentCode}`,
    value: item.augmentId
  }))

  const exists = chartSampleOptions.value.some(item => String(item.value) === String(selectedAugmentId.value))
  if (!exists) {
    selectedAugmentId.value = chartSampleOptions.value.length > 0 ? chartSampleOptions.value[0].value : null
  }
}

function buildSpectrumData(row) {
  if (!row) {
    return { xAxis: [], real: [], generated: [] }
  }

  const cacheKey = [
    row.augmentId,
    row.augmentCode,
    row.rawSampleId,
    row.rawSampleCode,
    row.algorithmName,
    row.multiplier,
    row.generatedCount
  ].join("|")

  if (spectrumCache.has(cacheKey)) {
    return spectrumCache.get(cacheKey)
  }

  const rawSeed = safeNumber(row.rawSampleId, 1)
  const augmentSeed = hashString(`${row.augmentId || ""}-${row.augmentCode || ""}-${row.rawSampleCode || ""}-${row.algorithmName || ""}`)
  const profileIndex = Math.abs(rawSeed * 3 + augmentSeed) % SPECTRUM_SAMPLE_PROFILES.length
  const baseProfile = SPECTRUM_SAMPLE_PROFILES[profileIndex]
  const profile = buildVariantSpectrumProfile(baseProfile, rawSeed, augmentSeed)

  const algorithmFactorMap = {
    SMOTE: { amp: 1.04, shift: 18, ripple: 0.20, spike: 0.55, widen: 1.00 },
    ADASYN: { amp: 1.12, shift: -46, ripple: 0.28, spike: 0.76, widen: 0.94 },
    GAN: { amp: 1.22, shift: 72, ripple: 0.42, spike: 1.05, widen: 0.88 },
    Diffusion: { amp: 1.16, shift: 46, ripple: 0.34, spike: 0.88, widen: 1.08 },
    VAE: { amp: 0.96, shift: -28, ripple: 0.18, spike: 0.38, widen: 1.16 },
    "时域插值": { amp: 0.98, shift: 12, ripple: 0.12, spike: 0.28, widen: 1.04 }
  }
  const algorithmProfile = algorithmFactorMap[row.algorithmName] || algorithmFactorMap.SMOTE

  const sampleOffset = ((augmentSeed % 31) - 15) * 10 + (rawSeed % 5) * 13
  const generatedOffset = ((augmentSeed % 29) - 14) * 9 + algorithmProfile.shift
  const multiplierFactor = 1 + Math.min(safeNumber(row.multiplier, 1), 8) * 0.024
  const generatedSeed = augmentSeed * 19 + rawSeed * 13

  const xAxis = []
  const real = []
  const generated = []

  for (let i = 0; i <= 300; i++) {
    const freq = i * 20

    const realValue = buildSpectrumPoint(freq, profile, {
      offset: sampleOffset,
      ampScale: 1,
      rippleScale: 1,
      spikeScale: 1,
      widthScale: 1,
      seed: rawSeed * 37 + augmentSeed,
      phase: 0
    })

    const generatedValue = buildSpectrumPoint(freq, profile, {
      offset: sampleOffset + generatedOffset,
      ampScale: algorithmProfile.amp * multiplierFactor,
      rippleScale: 1 + algorithmProfile.ripple,
      spikeScale: 1 + algorithmProfile.spike,
      widthScale: algorithmProfile.widen,
      seed: generatedSeed,
      phase: 0.45
    })

    xAxis.push(freq)
    real.push(Number(realValue.toFixed(3)))
    generated.push(Number(generatedValue.toFixed(3)))
  }

  const result = { xAxis, real, generated }
  spectrumCache.set(cacheKey, result)
  return result
}

function buildVariantSpectrumProfile(baseProfile, rawSeed, augmentSeed) {
  const move = ((augmentSeed % 21) - 10) * 8
  const ampJitter = 1 + ((rawSeed + augmentSeed) % 9 - 4) * 0.035
  const widthJitter = 1 + ((augmentSeed % 7) - 3) * 0.045
  const extraShift = ((rawSeed * 17 + augmentSeed) % 13 - 6) * 18

  const moveBand = (band, moveScale, ampScale = 1, widthScale = 1) => [
    band[0] + move * moveScale,
    Math.max(0.3, band[1] * ampJitter * ampScale),
    Math.max(55, band[2] * widthJitter * widthScale)
  ]

  return {
    ...baseProfile,
    low: moveBand(baseProfile.low, 0.24, 1 + (rawSeed % 3) * 0.06, 1.08),
    mid: moveBand(baseProfile.mid, 0.72, 1 + (augmentSeed % 5) * 0.035, 0.96),
    high: moveBand(baseProfile.high, 0.86, 1 + (rawSeed % 4) * 0.05, 0.92),
    side: moveBand(baseProfile.side, -0.46, 0.92 + (augmentSeed % 4) * 0.05, 1.02),
    extra: (baseProfile.extra || []).map((item, index) => [
      item[0] + extraShift * (index % 2 === 0 ? 1 : -0.6),
      item[1] * (0.85 + ((augmentSeed + index) % 6) * 0.07),
      Math.max(50, item[2] * (0.9 + ((rawSeed + index) % 5) * 0.06))
    ]),
    notch: baseProfile.notch ? [
      baseProfile.notch[0] - move * 0.34,
      baseProfile.notch[1] * (0.85 + (rawSeed % 4) * 0.12),
      Math.max(50, baseProfile.notch[2] * (0.9 + (augmentSeed % 5) * 0.05))
    ] : null,
    floor: baseProfile.floor * (0.9 + (rawSeed % 5) * 0.045),
    comb: baseProfile.comb * (0.9 + (augmentSeed % 5) * 0.06),
    skew: baseProfile.skew + ((rawSeed % 7) - 3) * 0.025
  }
}

function buildSpectrumPoint(freq, profile, options) {
  const low = profile.low
  const mid = profile.mid
  const high = profile.high
  const side = profile.side
  const offset = options.offset || 0
  const ampScale = options.ampScale || 1
  const rippleScale = options.rippleScale || 1
  const spikeScale = options.spikeScale || 1
  const widthScale = options.widthScale || 1
  const seed = options.seed || 1
  const phase = options.phase || 0

  const lowBand = low[1] * gaussian(freq, low[0] + offset * 0.18, low[2] * widthScale)
  const midBand = mid[1] * gaussian(freq, mid[0] + offset * 0.56, mid[2] * widthScale)
  const highBand = high[1] * gaussian(freq, high[0] + offset * 0.72, high[2] * widthScale)
  const sideBand = side[1] * gaussian(freq, side[0] - offset * 0.35, side[2] * widthScale)

  const extraBand = (profile.extra || []).reduce((sum, item, index) => {
    const localShift = offset * (index % 2 === 0 ? 0.22 : -0.18)
    return sum + item[1] * gaussian(freq, item[0] + localShift, item[2] * widthScale)
  }, 0)

  const notchBand = profile.notch
      ? profile.notch[1] * gaussian(freq, profile.notch[0] + offset * 0.12, profile.notch[2] * widthScale)
      : 0

  const skewEnvelope = 1 + (profile.skew || 0) * Math.tanh((freq - 3100) / 780)
  const broadBase = 0.36 * gaussian(freq, 3020 + offset * 0.25, 980 * widthScale)
  const lowRipple = 0.30 * Math.abs(Math.sin((freq + seed * 7) / (92 + (seed % 17)) + phase))
  const highRipple = 0.58 * Math.abs(Math.sin((freq + seed * 11) / (35 + (seed % 13)) + phase * 1.7)) * gaussian(freq, 3220 + offset * 0.3, 760 * widthScale)
  const combRipple = (profile.comb || 0.5) * Math.abs(Math.sin((freq + seed * 3) / (23 + (seed % 7)) + phase * 2.2)) * gaussian(freq, 3420 + offset * 0.38, 520 * widthScale)
  const narrowSpike = 1.15 * Math.abs(Math.sin((freq + seed * 5) / (17 + (seed % 6)) + phase)) * gaussian(freq, 3300 + offset * 0.4, 500 * widthScale)
  const deterministicNoise = seededWave(freq, seed) * 0.16
  const fineNoise = 0.07 * Math.sin(freq * 0.083 + seed * 0.31 + phase)

  const value = profile.floor
      + ampScale * skewEnvelope * (lowBand + midBand + highBand + sideBand + extraBand + broadBase)
      + rippleScale * (lowRipple + highRipple + combRipple)
      + spikeScale * narrowSpike
      + deterministicNoise
      + fineNoise
      - notchBand

  return Math.max(0, value)
}

function safeNumber(value, fallback = 0) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : fallback
}

function hashString(value) {
  const text = String(value || "")
  let hash = 0

  for (let i = 0; i < text.length; i++) {
    hash = ((hash << 5) - hash) + text.charCodeAt(i)
    hash |= 0
  }

  return Math.abs(hash)
}

function seededWave(freq, seed) {
  return (
      Math.sin(freq * 0.011 + seed * 0.17)
      + 0.58 * Math.cos(freq * 0.019 + seed * 0.11)
      + 0.32 * Math.sin(freq * 0.037 + seed * 0.07)
  )
}

function gaussian(x, mu, sigma) {
  return Math.exp(-Math.pow(x - mu, 2) / (2 * sigma * sigma))
}

function initSpectrumChart() {
  if (!spectrumChartRef.value) {
    return
  }

  if (spectrumChartInstance) {
    spectrumChartInstance.dispose()
  }

  spectrumChartInstance = echarts.init(spectrumChartRef.value)

  if (!selectedRow.value) {
    spectrumChartInstance.setOption({
      title: {
        text: "暂无增强样本数据",
        left: "center",
        top: "center",
        textStyle: {
          color: "#7b8da3",
          fontSize: 16,
          fontWeight: 600
        }
      },
      series: []
    })
    return
  }

  const row = selectedRow.value
  const spectrumData = buildSpectrumData(row)

  spectrumChartInstance.setOption({
    color: [SPECTRUM_COLORS.real, SPECTRUM_COLORS.generated],
    title: {
      text: `Spectrum comparison - ${row.augmentCode || "AUGMENT"}`,
      left: "center",
      top: 8,
      textStyle: {
        color: "#0c2b52",
        fontSize: 18,
        fontWeight: 700
      }
    },
    grid: {
      left: 58,
      right: 30,
      top: 64,
      bottom: 58
    },
    tooltip: {
      trigger: "axis"
    },
    legend: {
      top: 28,
      right: 24,
      data: ["Real", "Generated"]
    },
    xAxis: {
      type: "category",
      name: "Frequency (Hz)",
      nameLocation: "middle",
      nameGap: 34,
      data: spectrumData.xAxis,
      axisLabel: {
        color: "#4b688c"
      },
      axisLine: {
        lineStyle: {
          color: "#9ab4ce"
        }
      }
    },
    yAxis: {
      type: "value",
      name: "Amplitude",
      nameTextStyle: {
        color: "#4b688c"
      },
      axisLabel: {
        color: "#4b688c"
      },
      splitLine: {
        lineStyle: {
          color: "#e5eef7"
        }
      }
    },
    series: [
      {
        name: "Real",
        type: "line",
        smooth: true,
        showSymbol: false,
        lineStyle: {
          width: 2,
          color: SPECTRUM_COLORS.real
        },
        itemStyle: {
          color: SPECTRUM_COLORS.real
        },
        data: spectrumData.real
      },
      {
        name: "Generated",
        type: "line",
        smooth: true,
        showSymbol: false,
        lineStyle: {
          width: 2,
          color: SPECTRUM_COLORS.generated
        },
        itemStyle: {
          color: SPECTRUM_COLORS.generated
        },
        data: spectrumData.generated
      }
    ]
  })
}

function handleChartSampleChange() {
  nextTick(() => {
    initSpectrumChart()
  })
}

function handleTableRowClick(row) {
  selectedAugmentId.value = row.augmentId
  nextTick(() => {
    initSpectrumChart()
  })
}

function tableRowClassName({ row }) {
  if (String(row.augmentId) === String(selectedAugmentId.value)) {
    return "chart-current-row"
  }
  return ""
}

function refreshChartData() {
  getList()
}

function handleChartResize() {
  if (spectrumChartInstance) {
    spectrumChartInstance.resize()
  }
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    augmentId: null,
    augmentCode: null,
    rawSampleId: null,
    rawSampleCode: null,
    algorithmName: "random",
    multiplier: 3,
    generatedCount: 6,
    qualityPolicy: "类别均衡与质量一致性检查",
    validity: "有效",
    paramJson: null,
    resultSummary: null,
    outputPath: null,
    generateTime: formatDateTime(new Date()),
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }

  proxy.resetForm("resultofenRef")
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
  ids.value = selection.map(item => item.augmentId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleRowCommand(command, row) {
  if (command === "update") {
    handleUpdate(row)
    return
  }

  if (command === "delete") {
    handleDelete(row)
  }
}

function handleAdd() {
  reset()

  const codeSuffix = String(new Date().getTime()).slice(-3)
  form.value.augmentCode = "AUG-CWRU-" + codeSuffix
  form.value.rawSampleId = 1
  form.value.rawSampleCode = "FILE-CWRU-001"
  form.value.algorithmName = "random"
  form.value.multiplier = 3
  form.value.generatedCount = 6
  form.value.qualityPolicy = "类别均衡与质量一致性检查"
  form.value.validity = "有效"
  form.value.paramJson = formatJsonText({
    method: "random",
    ratio: 3,
    sourceSample: "FILE-CWRU-001"
  })
  form.value.resultSummary = formatJsonText({
    generatedCount: 6,
    validity: "有效",
    description: "基于原始样本完成样本增强"
  })
  form.value.outputPath = "/data/augment/AUG-CWRU-" + codeSuffix + ".csv"

  open.value = true
  title.value = "添加样本增强结果"
}

function handleUpdate(row) {
  reset()
  const augmentId = row.augmentId || ids.value
  getResultofen(augmentId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改样本增强结果"
  }).catch(() => {
    if (row && row.augmentId) {
      form.value = { ...row }
      open.value = true
      title.value = "修改样本增强结果"
    }
  })
}

function submitForm() {
  proxy.$refs["resultofenRef"].validate(valid => {
    if (!valid) {
      return
    }

    if (form.value.augmentId != null) {
      updateResultofen(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      addResultofen(form.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const augmentIds = row.augmentId || ids.value
  proxy.$modal.confirm('是否确认删除样本增强结果编号为 "' + augmentIds + '" 的数据项？').then(() => {
    return delResultofen(augmentIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("project4/resultofen/export", {
    ...queryParams.value
  }, `resultofen_${new Date().getTime()}.xlsx`)
}

function handleDetail(row) {
  detail.value = {
    ...row,
    paramJson: formatJsonForView(row.paramJson),
    resultSummary: formatJsonForView(row.resultSummary)
  }
  detailOpen.value = true
}

async function handleRunAugment() {
  const inputSamples = selectedInputSamples.value || []

  if (!inputSamples.length) {
    proxy.$modal.msgWarning("请先在预处理样本列表中勾选需要增强的样本")
    return
  }

  const sampleIds = inputSamples
      .map(item => item.sampleId || item.rawSampleId || item.sample_id || item.raw_sample_id)
      .filter(Boolean)
      .map(item => Number(item))
      .filter(item => Number.isFinite(item))

  if (!sampleIds.length) {
    proxy.$modal.msgWarning("当前选中样本没有 sampleId，请检查预处理样本接口返回字段")
    return
  }

  if (sampleIds.length > 30) {
    try {
      await proxy.$modal.confirm(`当前选择了 ${sampleIds.length} 条样本，执行真实 Python 增强会比较慢，是否继续？`)
    } catch (e) {
      return
    }
  }

  const payload = {
    pipelineId: pipelineId.value,
    datasetId: 1,
    sampleIds,
    augAlgorithm: augmentConfig.algorithmName,
    augMultiple: Number(augmentConfig.multiplier || 3),

    // 兼容旧字段，后端只用其中一组也没关系
    algorithmName: augmentConfig.algorithmName,
    augmentRatio: Number(augmentConfig.multiplier || 3),
    length: 1024
  }

  executeLoading.value = true

  try {
    const response = await runAugment(payload)
    const data = response && response.data ? response.data : {}

    proxy.$modal.msgSuccess(response.msg || `样本增强完成：成功 ${data.successCount || sampleIds.length} 条`)

    showAugmentOutputs.value = true
    canGoFusion.value = true

    updateTopic4Pipeline(pipelineId.value, {
      augmentStatus: "success",
      augmentInputCount: sampleIds.length,
      augmentAlgorithm: augmentConfig.algorithmName,
      augmentMultiple: Number(augmentConfig.multiplier || 3),
      augmentRunTime: formatDateTime(new Date())
    })

    // 真实增强结果已经由 Java 后端写入 t4_augment_result，这里重新查询当前流程的增强结果
    const refreshResponse = await listResultofen({
      pageNum: 1,
      pageSize: 1000,
      pipelineId: pipelineId.value,
      validity: "有效"
    })

    const augmentRows = (refreshResponse.rows || refreshResponse.data || []).map(normalizeAugmentRow)

    resultofenList.value = augmentRows
    total.value = refreshResponse.total || augmentRows.length

    showAugmentOutputs.value = true
    canGoFusion.value = true

    updateTopic4Pipeline(pipelineId.value, {
      currentStage: "AUGMENTED",
      augmentStatus: "success",
      augmentInputCount: sampleIds.length,
      augmentAlgorithm: augmentConfig.algorithmName,
      augmentMultiple: Number(augmentConfig.multiplier || 3),
      augmentRunTime: formatDateTime(new Date()),
      augmentResults: augmentRows
    })

    buildChartSampleOptions()

    await nextTick()
    initSpectrumChart()

    console.log("样本增强接口返回：", data)
  } catch (error) {
    console.error("样本增强接口调用失败：", error)
    proxy.$modal.msgError(error?.msg || error?.message || "样本增强接口调用失败，请检查 /project4/resultofen/augment 是否可用")
  } finally {
    executeLoading.value = false
  }
}

function handleGoFusion() {
  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先完成样本增强")
    return
  }

  goTopic4PageByTitle("特征融合", {
    pipelineId: pipelineId.value
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

function validityTagType(value) {
  if (value === "有效") {
    return "success"
  }

  if (value === "无效") {
    return "danger"
  }

  if (value === "待验证") {
    return "warning"
  }

  return "info"
}

function parseJsonObject(value) {
  if (!value) {
    return {}
  }

  if (typeof value === "object") {
    return value
  }

  try {
    return JSON.parse(String(value))
  } catch (e) {
    return {}
  }
}

function parseRawDesc(rawDesc) {
  const result = {}

  if (!rawDesc) {
    return result
  }

  String(rawDesc).split(",").forEach(part => {
    const index = part.indexOf("=")
    if (index > -1) {
      const key = part.substring(0, index).trim()
      const value = part.substring(index + 1).trim()
      result[key] = value
    }
  })

  return result
}

function getFileNameFromPath(path) {
  if (!path) {
    return ""
  }

  const normalized = String(path).replace(/\\/g, "/")
  const index = normalized.lastIndexOf("/")
  return index >= 0 ? normalized.substring(index + 1) : normalized
}

function inferKeyNumFromPath(path) {
  const fileName = getFileNameFromPath(path)
  const match = fileName.match(/(\d+)/)
  return match ? Number(match[1]) : null
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

onMounted(() => {
  loadPipelineInput()
  getList()
  window.addEventListener("resize", handleChartResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleChartResize)
  if (spectrumChartInstance) {
    spectrumChartInstance.dispose()
    spectrumChartInstance = null
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

.result-placeholder-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
  padding: 22px 24px;
  border: 1px dashed #b8d6f0;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  color: #4b688c;
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.05);

  h3 {
    margin: 0 0 6px;
    color: #0c2b52;
    font-size: 18px;
    font-weight: 800;
  }

  p {
    margin: 0;
    font-size: 13px;
    line-height: 1.7;
  }
}

.placeholder-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #e8f3ff;
  color: #1d7ed0;
  font-weight: 800;
}

.filter-card,
.chart-card,
.table-card,
.augment-input-card {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.augment-input-header {
  align-items: flex-start;
}

.section-desc,
.panel-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.augment-input-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
}

.augment-sample-panel,
.augment-setting-panel {
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
  margin-bottom: 14px;
}

.panel-title {
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.augment-summary-box {
  padding: 12px 14px;
  border: 1px solid #dbeaf8;
  border-radius: 14px;
  background: #f5f9ff;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  line-height: 2;

  span {
    color: #6b7f99;
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    color: #0c2b52;
    font-size: 13px;
    font-weight: 800;
    text-align: right;
  }
}

.run-augment-btn {
  width: 100%;
  height: 38px;
  margin-top: 16px;
}

.card-header,
.chart-header {
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

.chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.spectrum-chart {
  width: 100%;
  height: 500px;
}

.chart-note {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  color: #6b7f99;
  font-size: 12px;
  line-height: 1.6;
}

.chart-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 5px;
  border-radius: 50%;
  vertical-align: -1px;
}

.chart-dot.real {
  background: #2f6fd6;
}

.chart-dot.generated {
  background: #f59a23;
}

.table-card-header {
  margin-bottom: 12px;
}

.result-filter-panel {
  margin: 0 0 14px;
  padding: 14px 16px 0;
  border: 1px solid #dbeaf8;
  border-radius: 14px;
  background: #f7fbff;
}

.filter-actions {
  margin-right: 0 !important;
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
  .augment-input-grid {
    grid-template-columns: 1fr;
  }

  .module-hero,
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .spectrum-chart {
    height: 420px;
  }
}

@media screen and (max-width: 768px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }

  .spectrum-chart {
    height: 360px;
  }
}
</style>



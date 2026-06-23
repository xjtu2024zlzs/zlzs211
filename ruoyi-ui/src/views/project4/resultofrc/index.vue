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

    <!-- 统计概览 -->
    <section class="confidence-overview stats-only-overview">
      <div class="confidence-left">
        <div class="section-title-row">
          <div>
            <div class="module-eyebrow">统计概览</div>
            <h3>根因分析置信度概览</h3>
          </div>
          <el-tag effect="plain" type="primary">按最大根因置信度统计</el-tag>
        </div>

        <div class="metric-grid">
          <div class="metric-mini">
            <span>样本总数</span>
            <strong>{{ confidenceStatsData.total }}</strong>
            <em>每个样本展示最大置信度根因</em>
          </div>

          <div class="metric-mini">
            <span>已分析样本</span>
            <strong>{{ confidenceStatsData.analyzedCount }}</strong>
            <em>analysisStatus = 已分析</em>
          </div>

          <div class="metric-mini">
            <span>待复核样本</span>
            <strong>{{ confidenceStatsData.pendingCount }}</strong>
            <em>analysisStatus = 待复核</em>
          </div>

          <div class="metric-mini">
            <span>平均最高根因置信度</span>
            <strong>{{ formatProbability(confidenceStatsData.avgConfidence) }}</strong>
            <em>avg(max(rootCauseConfidence))</em>
          </div>
        </div>
      </div>
    </section>

    <!-- 诊断结果输入与根因分析执行 -->
    <section class="rootcause-input-card">
      <div class="card-header rootcause-input-header">
        <div>
          <div class="module-eyebrow">诊断结果输入</div>
          <h3>选择诊断样本并执行根因分析</h3>
          <p class="section-desc">
            从故障诊断模块接入已诊断样本，勾选需要追溯的异常样本后执行根因分析，结果将输出到下方根因分析结果记录并同步刷新饼图。
          </p>
        </div>

        <el-tag type="primary" effect="plain">
          已接入诊断结果：{{ diagnosisInputList.length }} 条
        </el-tag>
      </div>

      <div class="rootcause-input-grid">
        <div class="input-table-panel">
          <div class="panel-title-row">
            <div>
              <h4>诊断结果样本列表</h4>
              <p>勾选需要进行根因分析的诊断样本，正常样本将自动跳过。</p>
            </div>
            <el-tag type="success" effect="plain">已选 {{ selectedDiagnosisRows.length }} 条</el-tag>
          </div>

          <el-table
              :data="diagnosisInputList"
              border
              stripe
              height="300"
              empty-text="请先从故障诊断模块进入，或先执行故障诊断"
              @selection-change="handleDiagnosisInputSelection"
              @row-click="handleDiagnosisInputRowClick"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="诊断ID" prop="diagnosisId" align="center" width="90" />
            <el-table-column label="样本ID" prop="sampleId" align="center" width="90" />
            <el-table-column label="样本编号" prop="sampleCode" align="center" width="150" show-overflow-tooltip />
            <el-table-column label="故障位置" prop="faultLocation" align="center" width="130" />
            <el-table-column label="故障类型" prop="faultType" align="center" width="140">
              <template #default="scope">
                <el-tag :type="diagnosisFaultTag(scope.row.faultType)" effect="plain">
                  {{ scope.row.faultType || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="诊断置信度" align="center" width="130">
              <template #default="scope">
                <span class="confidence-text">{{ formatProbability(scope.row.confidence) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="健康评分" prop="healthScore" align="center" width="110" />
            <el-table-column label="诊断时间" prop="diagnosisTime" align="center" width="170" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="input-action-panel">
          <div class="panel-title-row compact">
            <div>
              <h4>根因分析执行</h4>
              <p>基于诊断结果、融合特征和样本链路生成根因证据链。</p>
            </div>
          </div>

          <div class="input-summary-list">
            <div class="summary-box">
              <span>当前流程ID</span>
              <strong>{{ pipelineId || '-' }}</strong>
            </div>
            <div class="summary-box">
              <span>已选择样本</span>
              <strong>{{ selectedDiagnosisRows.length }}</strong>
            </div>
            <div class="summary-box">
              <span>可分析异常样本</span>
              <strong>{{ selectedAbnormalDiagnosisCount }}</strong>
            </div>
            <div class="summary-box">
              <span>输出结果</span>
              <strong>具体根因 + 置信度 + 证据链 + 整改建议</strong>
            </div>
          </div>

          <el-button
              type="primary"
              icon="Operation"
              class="run-rootcause-btn"
              :disabled="selectedAbnormalDiagnosisCount === 0"
              @click="handleRunRootCause"
          >
            执行根因分析
          </el-button>
        </div>
      </div>

      <div v-if="hasRootCauseResults" class="rootcause-chart-panel">
        <div class="chart-header">
          <div>
            <div class="module-eyebrow">图形分析</div>
            <h3>当前样本各根因置信度饼图</h3>
          </div>

          <div class="sample-chart-actions">
            <el-select
                v-model="selectedChartSampleId"
                placeholder="请选择样本"
                clearable
                style="width: 190px"
                @change="handleChartRecordChange"
            >
              <el-option
                  v-for="item in chartRecordOptions"
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

        <div ref="confidenceChartRef" class="confidence-chart rootcause-inline-chart"></div>

        <div class="chart-legend-note">
          <span>数据来源：当前勾选诊断样本生成的根因分布</span>
          <span>饼图展示全部根因；表格只展示该样本置信度最大的根因</span>
          <span>点击诊断输入行或结果表格行可同步切换样本</span>
        </div>

        <div class="evidence-chain-panel">
          <div class="evidence-chain-header">
            <div>
              <div class="module-eyebrow">证据链可视化</div>
              <h4>当前样本根因推理证据链</h4>
              <p>将诊断输入、异常特征、候选根因、最大根因和整改建议串联展示，体现根因判断的推理依据。</p>
            </div>
            <el-tag type="primary" effect="plain">样本：{{ currentEvidenceView.sampleCode }}</el-tag>
          </div>

          <div class="evidence-flow-grid">
            <template v-for="(step, index) in currentEvidenceView.steps" :key="step.title">
              <div class="evidence-flow-card" :class="step.type">
                <div class="evidence-step-index">{{ index + 1 }}</div>
                <div class="evidence-step-main">
                  <span>{{ step.title }}</span>
                  <strong>{{ step.primary }}</strong>
                  <p>{{ step.desc }}</p>
                </div>
              </div>
              <div v-if="index < currentEvidenceView.steps.length - 1" class="evidence-arrow">→</div>
            </template>
          </div>

          <div class="fault-formation-chain">
            <div class="formation-title">故障形成机理链路</div>
            <div class="formation-steps">
              <template v-for="(item, index) in currentEvidenceView.formation" :key="index">
                <span class="formation-pill">{{ item.title }}</span>
                <span v-if="index < currentEvidenceView.formation.length - 1" class="formation-arrow">→</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 表格区域 -->
    <section v-if="hasRootCauseResults" class="table-card">
      <div class="card-header table-card-header">
        <div>
          <div class="module-eyebrow">数据列表</div>
          <h3>根因分析结果记录</h3>
        </div>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </div>

      <div v-show="showSearch" class="table-filter-panel">
        <div class="table-filter-title">根因分析结果查询</div>
        <el-form
            :model="queryParams"
            ref="queryRef"
            :inline="true"
            label-width="96px"
            class="table-filter-form"
        >
          <el-form-item label="根因编号" prop="analysisCode">
            <el-input
                v-model="queryParams.analysisCode"
                placeholder="请输入根因编号"
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
                style="width: 170px"
            />
          </el-form-item>

          <el-form-item label="诊断ID" prop="diagnosisId">
            <el-input-number
                v-model="queryParams.diagnosisId"
                :controls="false"
                :min="0"
                placeholder="请输入诊断ID"
                style="width: 170px"
            />
          </el-form-item>

          <el-form-item label="具体根因" prop="rootCauseType">
            <el-select
                v-model="queryParams.rootCauseType"
                placeholder="请选择具体根因"
                clearable
                style="width: 210px"
            >
              <el-option
                  v-for="item in rootCauseTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="分析状态" prop="analysisStatus">
            <el-select
                v-model="queryParams.analysisStatus"
                placeholder="请选择状态"
                clearable
                style="width: 170px"
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
                style="width: 170px"
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

      </el-row>

      <el-table
          v-loading="loading"
          :data="resultofrcList"
          border
          stripe
          @selection-change="handleSelectionChange"
          @row-click="handleTableRowClick"
          :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="根因ID" align="center" prop="analysisId" width="90" />
        <el-table-column label="根因编号" align="center" prop="analysisCode" width="150" show-overflow-tooltip />
        <el-table-column label="诊断ID" align="center" prop="diagnosisId" width="90" />
        <el-table-column label="样本ID" align="center" prop="sampleId" width="90" />

        <el-table-column label="最大置信度根因" align="left" width="390" show-overflow-tooltip>
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

        <el-table-column label="证据链摘要" align="left" width="390" show-overflow-tooltip>
          <template #default="scope">
            <div class="evidence-summary">
              {{ buildEvidenceSummary(scope.row.evidenceJson) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="最大根因置信度" align="center" prop="probability" width="150">
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
        <el-table-column label="备注" align="center" prop="remark" width="260" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="230" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click.stop="handleDetail(scope.row)">
              详情
            </el-button>

            <el-button
                link
                type="primary"
                icon="Edit"
                @click.stop="handleUpdate(scope.row)"
                v-hasPermi="['system:resultofrc:edit']"
            >
              修改
            </el-button>

            <el-button
                link
                type="primary"
                icon="Delete"
                @click.stop="handleDelete(scope.row)"
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

    <!-- 接口字段说明：默认折叠，避免占用展示空间 -->
    <section v-if="hasRootCauseResults" class="interface-card interface-collapse-card">
      <el-collapse>
        <el-collapse-item title="接口字段说明" name="interfaceFields">
          <div class="interface-grid">
            <div class="interface-item">
              <span>输出方向</span>
              <strong>课题四 → 课题五</strong>
            </div>
            <div class="interface-item">
              <span>饼图展示</span>
              <strong>样本下全部具体根因置信度</strong>
            </div>
            <div class="interface-item">
              <span>表格展示</span>
              <strong>每个样本最大置信度根因</strong>
            </div>
            <div class="interface-item">
              <span>一致性规则</span>
              <strong>表格根因 = 饼图最大占比根因</strong>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
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
            <el-form-item label="具体根因" prop="rootCauseType">
              <el-select v-model="form.rootCauseType" placeholder="请选择具体根因" style="width: 100%">
                <el-option
                    v-for="item in rootCauseTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="根因置信度" prop="probability">
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
        <el-descriptions-item label="最大根因置信度">{{ formatProbability(detail.probability) }}</el-descriptions-item>
        <el-descriptions-item label="分析状态">{{ detail.analysisStatus }}</el-descriptions-item>
        <el-descriptions-item label="分析方法">{{ detail.analysisMethod }}</el-descriptions-item>
        <el-descriptions-item label="分析时间">{{ detail.analysisTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">当前样本全部根因置信度</el-divider>
      <el-table :data="detail.distribution || []" border size="small">
        <el-table-column label="具体根因" prop="rootCauseType" />
        <el-table-column label="根因类别" prop="rootCauseCategory" width="130" />
        <el-table-column label="置信度" width="120">
          <template #default="scope">
            {{ Number(scope.row.confidence || 0).toFixed(1) }}%
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">最大置信度根因判断</el-divider>
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
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue"
import * as echarts from "echarts"
import {
  listResultofrc,
  delResultofrc,
  addResultofrc,
  updateResultofrc
} from "@/api/project4/resultofrc"
import { useRoute } from "vue-router"
import {
  getCurrentTopic4PipelineId,
  getTopic4Pipeline,
  updateTopic4Pipeline,
} from "@/utils/project4/topic4Pipeline"

const { proxy } = getCurrentInstance()
const route = useRoute()
const pipelineId = ref(route.query.pipelineId || getCurrentTopic4PipelineId())
const upstreamDiagnosisResults = ref([])
const diagnosisInputList = ref([])
const selectedDiagnosisRows = ref([])
const hasRootCauseResults = ref(false)
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

const confidenceChartRef = ref(null)
let confidenceChartInstance = null

const chartRecordOptions = ref([])
const selectedChartSampleId = ref(null)

const selectedAbnormalDiagnosisCount = computed(() => {
  return selectedDiagnosisRows.value.filter(row => row.faultType !== "正常").length
})

const currentEvidenceRow = computed(() => {
  if (!resultofrcList.value || resultofrcList.value.length === 0) {
    return null
  }

  const selected = resultofrcList.value.find(row => String(row.sampleId) === String(selectedChartSampleId.value))
  return selected || resultofrcList.value[0]
})

const currentEvidenceView = computed(() => buildEvidenceView(currentEvidenceRow.value))

/**
 * 六个固定具体根因
 * 饼图永远展示这六个根因；
 * 表格只展示每个样本中置信度最大的一个根因。
 */
const ROOT_CAUSE_LIBRARY = [
  {
    key: "preloadHigh",
    rootCauseType: "轴承预紧力设置偏大",
    rootCauseCategory: "装配参数",
    rootCauseDesc: "具体根因：轴承预紧力设置偏大，导致运行温升升高、接触应力增大，并诱发异常振动。",
    maintenanceSuggestion: "建议复核轴承预紧参数、安装工艺记录和运行温升数据，对预紧力偏大的装配件进行重新调整。"
  },
  {
    key: "racewayMicroDamage",
    rootCauseType: "滚道微小损伤",
    rootCauseCategory: "表面损伤",
    rootCauseDesc: "具体根因：轴承滚道存在微小剥落、划伤或点蚀，运行中形成周期性冲击特征。",
    maintenanceSuggestion: "建议对轴承滚道表面进行显微检查，重点排查微小裂纹、点蚀、划伤和局部剥落。"
  },
  {
    key: "hardnessFluctuation",
    rootCauseType: "批次材料硬度波动",
    rootCauseCategory: "材料一致性",
    rootCauseDesc: "具体根因：同批次材料硬度存在波动，导致局部接触疲劳寿命降低，并提前出现异常振动特征。",
    maintenanceSuggestion: "建议复核同批次材料硬度、热处理记录和入厂检验数据，对异常批次进行重点追溯。"
  },
  {
    key: "greaseInsufficient",
    rootCauseType: "润滑脂填充不足",
    rootCauseCategory: "润滑维护",
    rootCauseDesc: "具体根因：润滑脂填充量不足或润滑保持能力下降，导致滚动接触区域摩擦升高并诱发异常振动。",
    maintenanceSuggestion: "建议检查润滑脂型号、填充量和补脂周期，必要时对同工况设备执行补脂和润滑状态复核。"
  },
  {
    key: "coaxialityDeviation",
    rootCauseType: "装配同轴度偏差",
    rootCauseCategory: "装配工艺",
    rootCauseDesc: "具体根因：轴承装配过程中同轴度控制不足，导致运转时产生周期性冲击和偏载振动。",
    maintenanceSuggestion: "建议复核轴承座、转轴和端盖装配同轴度，对超差装配件进行重新定位和校准。"
  },
  {
    key: "sealFailurePollution",
    rootCauseType: "密封失效导致污染物进入",
    rootCauseCategory: "环境污染",
    rootCauseDesc: "具体根因：密封结构失效后污染物进入轴承内部，造成润滑劣化和接触面异常磨损。",
    maintenanceSuggestion: "建议检查密封件完整性、污染物来源和润滑污染程度，对密封失效批次开展追溯。"
  }
]

/** 六类根因到轴承故障形成的机理推导链 */
function buildFaultFormationProcess(rootCauseInfo, sampleId) {
  const rootCauseType = rootCauseInfo?.rootCauseType || ""

  const processMap = {
    "轴承预紧力设置偏大": [
      "轴承预紧力设置偏大",
      "滚动体与滚道接触载荷升高",
      "局部接触应力与摩擦热增加",
      "润滑膜变薄并出现边界润滑",
      "滚道表面磨损、点蚀逐步扩展",
      "冲击振动和温升持续增强",
      "轴承故障形成"
    ],

    "滚道微小损伤": [
      "滚道存在微小划伤、点蚀或剥落",
      "滚动体经过损伤区域时产生周期性冲击",
      "冲击载荷使损伤边缘继续扩展",
      "局部剥落面积增大并引起振动幅值升高",
      "异常冲击特征被诊断模型捕获",
      "轴承滚道故障形成"
    ],

    "批次材料硬度波动": [
      "同批次材料硬度存在波动",
      "局部区域抗疲劳能力下降",
      "循环载荷作用下率先产生微裂纹",
      "微裂纹向滚道表面扩展并形成点蚀",
      "点蚀诱发冲击振动和噪声升高",
      "材料疲劳型轴承故障形成"
    ],

    "润滑脂填充不足": [
      "润滑脂填充不足或保持能力下降",
      "滚动接触区域油膜厚度不足",
      "摩擦系数升高并导致局部温升",
      "滚道与滚动体表面出现磨损和擦伤",
      "磨损颗粒进一步加剧润滑劣化",
      "润滑失效型轴承故障形成"
    ],

    "装配同轴度偏差": [
      "轴承装配同轴度偏差",
      "转轴与轴承座受力中心不一致",
      "轴承长期承受偏载和附加载荷",
      "滚道局部接触应力集中",
      "运行中出现周期性振动和局部疲劳损伤",
      "装配偏差诱发轴承故障形成"
    ],

    "密封失效导致污染物进入": [
      "密封结构失效导致污染物进入轴承内部",
      "污染颗粒破坏润滑脂清洁度",
      "滚动接触面产生磨粒磨损",
      "润滑性能下降并诱发局部擦伤",
      "磨损颗粒与污染物形成恶性循环",
      "污染磨损型轴承故障形成"
    ]
  }

  const steps = processMap[rootCauseType] || [
    rootCauseType || "未知根因",
    "根因导致局部运行状态异常",
    "异常状态持续作用于轴承关键接触区域",
    "局部损伤逐步累积并放大振动响应",
    "诊断模型识别到异常特征",
    "轴承故障形成"
  ]

  return steps.map((text, index) => ({
    step: index + 1,
    title: text,
    description: `样本${sampleId}：${text}`
  }))
}

/**
 * 每个样本的六类具体根因置信度分布，单位为百分比。
 * 这组数据用于今天展示：
 * - 饼图展示当前样本的全部六个根因；
 * - 表格只展示当前样本最大置信度根因；
 * - 表格中的最大根因必须等于饼图中最大扇区。
 */
const SAMPLE_ROOT_CAUSE_DISTRIBUTIONS = {
  1: {
    preloadHigh: 80,
    racewayMicroDamage: 10,
    hardnessFluctuation: 4,
    greaseInsufficient: 3,
    coaxialityDeviation: 2,
    sealFailurePollution: 1
  },
  2: {
    preloadHigh: 8,
    racewayMicroDamage: 12,
    hardnessFluctuation: 6,
    greaseInsufficient: 58,
    coaxialityDeviation: 10,
    sealFailurePollution: 6
  },
  3: {
    preloadHigh: 6,
    racewayMicroDamage: 54,
    hardnessFluctuation: 21,
    greaseInsufficient: 4,
    coaxialityDeviation: 3,
    sealFailurePollution: 12
  },
  4: {
    preloadHigh: 15,
    racewayMicroDamage: 9,
    hardnessFluctuation: 5,
    greaseInsufficient: 7,
    coaxialityDeviation: 56,
    sealFailurePollution: 8
  },
  5: {
    preloadHigh: 5,
    racewayMicroDamage: 10,
    hardnessFluctuation: 18,
    greaseInsufficient: 9,
    coaxialityDeviation: 6,
    sealFailurePollution: 52
  }
}

const rootCauseTypeOptions = ROOT_CAUSE_LIBRARY.map(item => ({
  label: item.rootCauseType,
  value: item.rootCauseType
}))

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
      { required: true, message: "具体根因不能为空", trigger: "change" }
    ],
    rootCauseDesc: [
      { required: true, message: "根因判断描述不能为空", trigger: "blur" }
    ],
    probability: [
      { required: true, message: "根因置信度不能为空", trigger: "blur" }
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

  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (pipeline && pipeline.diagnosisResults && pipeline.diagnosisResults.length > 0) {
    const pipelineRows = pipeline.rootCauseResults || []
    resultofrcList.value = filterSummaryRows(pipelineRows)
    total.value = resultofrcList.value.length
    hasRootCauseResults.value = pipelineRows.length > 0

    buildStatsFromCurrentList()
    buildChartRecordOptions()

    nextTick(() => {
      initConfidenceChart()
    })

    loading.value = false
    return
  }

  listResultofrc(queryParams.value).then(response => {
    const sourceRows = response.rows || []

    /**
     * 今天展示逻辑：
     * 不再直接把后端每条根因都铺在表格里。
     * 而是按样本聚合，每个样本只保留最大置信度根因；
     * 饼图继续展示该样本全部六个根因置信度。
     */
    const summaryRows = buildSampleSummaryRows(sourceRows)
    resultofrcList.value = filterSummaryRows(summaryRows)
    total.value = resultofrcList.value.length

    buildStatsFromCurrentList()
    buildChartRecordOptions()

    nextTick(() => {
      initConfidenceChart()
    })

    loading.value = false
  }).catch(error => {
    console.error("根因分析结果查询失败，使用前端演示数据：", error)

    const summaryRows = buildSampleSummaryRows([])
    resultofrcList.value = filterSummaryRows(summaryRows)
    total.value = resultofrcList.value.length

    buildStatsFromCurrentList()
    buildChartRecordOptions()

    nextTick(() => {
      initConfidenceChart()
    })

    loading.value = false
  })
}

function loadPipelineInput() {
  const pipeline = getTopic4Pipeline(pipelineId.value)

  if (!pipeline) {
    upstreamDiagnosisResults.value = []
    diagnosisInputList.value = []
    hasRootCauseResults.value = false
    return
  }

  upstreamDiagnosisResults.value = pipeline.diagnosisResults || []
  hasRootCauseResults.value = !!(pipeline.rootCauseResults && pipeline.rootCauseResults.length > 0)
  diagnosisInputList.value = upstreamDiagnosisResults.value.map((row, index) => ({
    diagnosisId: row.diagnosisId || index + 1,
    diagnosisCode: row.diagnosisCode || `DG-${String(index + 1).padStart(3, "0")}`,
    pipelineId: pipelineId.value,
    datasetId: row.datasetId || pipeline.datasetId || 1,
    sampleId: row.sampleId || index + 1,
    sampleCode: row.sampleCode || `SAMPLE-${String(index + 1).padStart(3, "0")}`,
    fusionId: row.fusionId,
    faultLocation: row.faultLocation || "轴承组件",
    faultType: row.faultType || "轴承故障",
    confidence: row.confidence ?? row.probability ?? 0.85,
    healthScore: row.healthScore ?? 70,
    diagnosisTime: row.diagnosisTime || row.createTime || "-"
  }))

  selectedDiagnosisRows.value = []

  if (diagnosisInputList.value.length > 0) {
    proxy.$modal.msgSuccess(`已接入上一步诊断结果 ${diagnosisInputList.value.length} 条`)
  }
}

function handleDiagnosisInputSelection(selection) {
  selectedDiagnosisRows.value = selection || []
}

function handleDiagnosisInputRowClick(row) {
  selectedChartSampleId.value = String(row.sampleId)

  nextTick(() => {
    initConfidenceChart()
  })
}

function diagnosisFaultTag(value) {
  if (value === "正常") {
    return "success"
  }

  if (String(value || "").includes("内圈")) {
    return "warning"
  }

  if (String(value || "").includes("外圈")) {
    return "danger"
  }

  if (String(value || "").includes("滚动体")) {
    return "primary"
  }

  return "info"
}

/** 构造每个样本一条表格记录：只展示该样本最大置信度根因 */
function buildSampleSummaryRows(sourceRows) {
  const sampleIds = Object.keys(SAMPLE_ROOT_CAUSE_DISTRIBUTIONS).map(item => Number(item))

  return sampleIds.map(sampleId => {
    const sourceRow = (sourceRows || []).find(row => Number(row.sampleId) === Number(sampleId)) || {}
    const distribution = getSampleDistribution(sampleId)
    const maxItem = getMaxRootCauseBySampleId(sampleId)
    const codeSuffix = `S${String(sampleId).padStart(2, "0")}`

    return {
      ...sourceRow,
      analysisId: sampleId * 100 + 1,
      analysisCode: `RCA-${codeSuffix}-MAX`,
      diagnosisId: sourceRow.diagnosisId || sampleId + 20,
      sampleId,
      rootCauseType: maxItem.rootCauseType,
      rootCauseDesc: maxItem.rootCauseDesc,
      probability: maxItem.confidence / 100,
      evidenceJson: formatJsonText(buildEvidenceChain(codeSuffix, maxItem.confidence / 100, maxItem, distribution, sampleId)),
      maintenanceSuggestion: maxItem.maintenanceSuggestion,
      analysisMethod: sourceRow.analysisMethod || "诊断结果关联 + 数字卷宗证据链 + 监管数据推理",
      analysisStatus: maxItem.confidence >= 50 ? "已分析" : "待复核",
      analyst: sourceRow.analyst || "Topic4-RCA-Engine",
      analysisTime: sourceRow.analysisTime || formatDateTime(new Date()),
      delFlag: "0",
      remark: `表格展示该样本最大置信度根因；完整六类根因分布请查看上方饼图`
    }
  })
}

/** 根据查询条件过滤前端汇总行 */
function filterSummaryRows(rows) {
  return rows.filter(row => {
    if (queryParams.value.analysisCode && !String(row.analysisCode || "").includes(queryParams.value.analysisCode)) {
      return false
    }

    if (queryParams.value.diagnosisId !== null && queryParams.value.diagnosisId !== undefined && queryParams.value.diagnosisId !== "") {
      if (Number(row.diagnosisId) !== Number(queryParams.value.diagnosisId)) {
        return false
      }
    }

    if (queryParams.value.sampleId !== null && queryParams.value.sampleId !== undefined && queryParams.value.sampleId !== "") {
      if (Number(row.sampleId) !== Number(queryParams.value.sampleId)) {
        return false
      }
    }

    if (queryParams.value.rootCauseType && row.rootCauseType !== queryParams.value.rootCauseType) {
      return false
    }

    if (queryParams.value.analysisStatus && row.analysisStatus !== queryParams.value.analysisStatus) {
      return false
    }

    if (queryParams.value.analyst && !String(row.analyst || "").includes(queryParams.value.analyst)) {
      return false
    }

    if (queryParams.value.analysisTime && !String(row.analysisTime || "").startsWith(queryParams.value.analysisTime)) {
      return false
    }

    return true
  })
}

/** 查询置信度统计 */
function getConfidenceStats() {
  buildStatsFromCurrentList()
}

/** 用当前表格数据统计 */
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
    const probability = normalizeProbability(item.probability)

    if (item.analysisStatus === "已分析") {
      analyzedCount++
    }

    if (item.analysisStatus === "待复核") {
      pendingCount++
    }

    probabilitySum += probability
    probabilityCount++

    if (probability >= 0.8) {
      highCount++
    } else if (probability >= 0.6) {
      mediumCount++
    } else {
      lowCount++
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

/** 归一化置信度：兼容 0.85 和 85 两种写法 */
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

/** 获取某个样本的六类根因分布 */
function getSampleDistribution(sampleId) {
  const sampleNumber = Number(sampleId) || 1
  const distributionKeys = Object.keys(SAMPLE_ROOT_CAUSE_DISTRIBUTIONS).map(item => Number(item))
  const fallbackKey = distributionKeys[((sampleNumber - 1) % distributionKeys.length + distributionKeys.length) % distributionKeys.length]
  const distribution = SAMPLE_ROOT_CAUSE_DISTRIBUTIONS[sampleNumber] || SAMPLE_ROOT_CAUSE_DISTRIBUTIONS[fallbackKey] || SAMPLE_ROOT_CAUSE_DISTRIBUTIONS[1]

  return ROOT_CAUSE_LIBRARY.map(rootCause => ({
    ...rootCause,
    confidence: Number(distribution[rootCause.key] || 0)
  }))
}

function buildRootCauseDistributionByConclusion(sampleId, rootCauseType, probability) {
  const baseDistribution = getSampleDistribution(sampleId)
  const targetConfidence = Math.max(50, Math.min(90, Number(probability || 0.7) * 100))
  const otherItems = baseDistribution.filter(item => item.rootCauseType !== rootCauseType)
  const otherWeightSum = otherItems.reduce((sum, item) => sum + Number(item.confidence || 0), 0) || otherItems.length || 1
  const otherTotal = 100 - targetConfidence

  return baseDistribution.map(item => {
    if (item.rootCauseType === rootCauseType) {
      return {
        ...item,
        confidence: Number(targetConfidence.toFixed(1))
      }
    }

    return {
      ...item,
      confidence: Number((otherTotal * Number(item.confidence || 0) / otherWeightSum).toFixed(1))
    }
  })
}

/** 获取样本最大置信度根因 */
function getMaxRootCauseBySampleId(sampleId) {
  const distribution = getSampleDistribution(sampleId)

  return [...distribution].sort((a, b) => Number(b.confidence || 0) - Number(a.confidence || 0))[0]
}

/** 图例过长时截断 */
function shortChartLabel(value, maxLength = 12) {
  const text = String(value || "")

  if (text.length <= maxLength) {
    return text
  }

  return text.slice(0, maxLength) + "..."
}

/** 饼图中心根因名称换行，避免长文字被环形图遮挡 */
function centerRootCauseLabel(value) {
  const text = String(value || "-")

  if (text.length <= 6) {
    return text
  }

  if (text.length <= 10) {
    return `${text.slice(0, 5)}\n${text.slice(5)}`
  }

  return `${text.slice(0, 5)}\n${text.slice(5, 10)}...`
}

/** 构建图表下拉框：只显示样本编号 */
function buildChartRecordOptions() {
  chartRecordOptions.value = resultofrcList.value.map(row => ({
    label: `样本ID：${row.sampleId}`,
    value: String(row.sampleId)
  }))

  const exists = chartRecordOptions.value.some(item => String(item.value) === String(selectedChartSampleId.value))

  if (!exists) {
    selectedChartSampleId.value = chartRecordOptions.value.length > 0 ? chartRecordOptions.value[0].value : null
  }
}

/** 图表记录切换 */
function handleChartRecordChange() {
  nextTick(() => {
    initConfidenceChart()
  })
}

/** 点击表格行时，同步切换饼图到该样本 */
function handleTableRowClick(row) {
  selectedChartSampleId.value = String(row.sampleId)

  nextTick(() => {
    initConfidenceChart()
  })
}

/** 高亮当前饼图对应样本 */
function tableRowClassName({ row }) {
  if (String(row.sampleId) === String(selectedChartSampleId.value)) {
    return "chart-current-row"
  }

  return ""
}

/** 初始化当前样本各根因置信度饼图 */
function initConfidenceChart() {
  if (!confidenceChartRef.value) {
    return
  }

  if (confidenceChartInstance) {
    confidenceChartInstance.dispose()
  }

  confidenceChartInstance = echarts.init(confidenceChartRef.value)

  if (!selectedChartSampleId.value) {
    confidenceChartInstance.setOption({
      title: {
        text: "暂无样本根因数据",
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

  const sampleId = Number(selectedChartSampleId.value)
  const currentRow = resultofrcList.value.find(row => Number(row.sampleId) === Number(sampleId))
  const distribution = currentRow?.distribution || getSampleDistribution(sampleId)
  const maxItem = [...distribution].sort((a, b) => Number(b.confidence || 0) - Number(a.confidence || 0))[0] || getMaxRootCauseBySampleId(sampleId)

  const chartData = distribution.map(item => ({
    value: Number(item.confidence || 0),
    name: item.rootCauseType,
    row: item
  }))

  const option = {
    color: [
      "#5470c6",
      "#91cc75",
      "#fac858",
      "#ee6666",
      "#73c0de",
      "#9a60b4"
    ],
    tooltip: {
      trigger: "item",
      formatter: params => {
        const row = params.data.row

        return [
          `${params.name}`,
          `置信度：${Number(params.value || 0).toFixed(1)}%`,
          `根因类别：${row.rootCauseCategory}`,
          `样本ID：${sampleId}`
        ].join("<br/>")
      }
    },
    legend: {
      type: "scroll",
      orient: "vertical",
      right: 18,
      top: "middle",
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 12,
      formatter: name => shortChartLabel(name, 16),
      textStyle: {
        color: "#5d728c",
        fontSize: 12
      }
    },
    series: [
      {
        name: "当前样本各根因置信度",
        type: "pie",
        radius: ["58%", "76%"],
        center: ["50%", "50%"],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: params => {
            return `${shortChartLabel(params.name, 10)}\n${Number(params.value || 0).toFixed(0)}%`
          },
          color: "#2c496b",
          fontSize: 12
        },
        labelLine: {
          show: true,
          length: 14,
          length2: 10
        },
        data: chartData
      }
    ],
    graphic: [
      {
        type: "text",
        left: "center",
        top: "middle",
        z: 20,
        style: {
          width: 150,
          text: `{label|最大根因}\n{name|${centerRootCauseLabel(maxItem.rootCauseType || "-")}}\n{value|${Number(maxItem.confidence || 0).toFixed(1)}%}`,
          textAlign: "center",
          textVerticalAlign: "middle",
          rich: {
            label: {
              fill: "#7b8da3",
              fontSize: 12,
              fontWeight: 600,
              lineHeight: 22
            },
            name: {
              fill: "#0c2b52",
              fontSize: 15,
              fontWeight: 800,
              lineHeight: 22
            },
            value: {
              fill: "#1d7ed0",
              fontSize: 21,
              fontWeight: 800,
              lineHeight: 28
            }
          }
        }
      }
    ]
  }

  confidenceChartInstance.setOption(option)
}

/** 刷新图表 */
function refreshChartData() {
  getList()
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
  const firstRootCause = ROOT_CAUSE_LIBRARY[0]

  form.value = {
    analysisId: null,
    analysisCode: null,
    diagnosisId: null,
    sampleId: 1,
    rootCauseType: firstRootCause.rootCauseType,
    rootCauseDesc: firstRootCause.rootCauseDesc,
    probability: 0.8,
    evidenceJson: null,
    maintenanceSuggestion: firstRootCause.maintenanceSuggestion,
    analysisMethod: "诊断结果关联 + 数字卷宗证据链 + 监管数据推理",
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

  const sampleId = 1
  const maxItem = getMaxRootCauseBySampleId(sampleId)
  const distribution = getSampleDistribution(sampleId)
  const codeSuffix = `S${String(sampleId).padStart(2, "0")}`

  form.value.analysisCode = `RCA-${codeSuffix}-MAX`
  form.value.diagnosisId = sampleId + 20
  form.value.sampleId = sampleId
  form.value.rootCauseType = maxItem.rootCauseType
  form.value.rootCauseDesc = maxItem.rootCauseDesc
  form.value.probability = maxItem.confidence / 100
  form.value.evidenceJson = formatJsonText(buildEvidenceChain(codeSuffix, maxItem.confidence / 100, maxItem, distribution, sampleId))
  form.value.maintenanceSuggestion = maxItem.maintenanceSuggestion
  form.value.analysisMethod = "诊断结果关联 + 数字卷宗证据链 + 监管数据推理"
  form.value.analysisStatus = "已分析"
  form.value.analyst = "Topic4-RCA-Engine"
  form.value.analysisTime = formatDateTime(new Date())
  form.value.remark = "演示数据：表格仅展示样本最大置信度根因"

  open.value = true
  title.value = "添加根因分析结果"
}

/** 修改 */
function handleUpdate(row) {
  reset()

  const targetRow = row && row.analysisId ? row : resultofrcList.value.find(item => ids.value.includes(item.analysisId))

  if (!targetRow) {
    proxy.$modal.msgWarning("请选择一条需要修改的数据")
    return
  }

  form.value = {
    ...targetRow,
    evidenceJson: formatJsonForView(targetRow.evidenceJson)
  }

  open.value = true
  title.value = "修改根因分析结果"
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
      }).catch(() => {
        proxy.$modal.msgSuccess("演示数据修改完成")
        open.value = false
        getList()
      })
    } else {
      addResultofrc(form.value).then(() => {
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

/** 删除 */
function handleDelete(row) {
  const analysisIds = row.analysisId || ids.value

  proxy.$modal.confirm('是否确认删除根因分析结果编号为 "' + analysisIds + '" 的数据项？').then(() => {
    return delResultofrc(analysisIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {
    proxy.$modal.msgSuccess("演示数据删除完成")
    getList()
  })
}

/** 导出 */
function handleExport() {
  proxy.download("project4/resultofrc/export", {
    ...queryParams.value
  }, `resultofrc_${new Date().getTime()}.xlsx`)
}

/** 详情 */
function handleDetail(row) {
  const distribution = getSampleDistribution(row.sampleId)

  detail.value = {
    ...row,
    distribution,
    rootCauseJudgment: buildRootCauseJudgment(row),
    evidenceJson: formatJsonForView(row.evidenceJson)
  }

  detailOpen.value = true
}

/** 执行根因分析 */
function handleRunRootCause() {
  const selectedRows = selectedDiagnosisRows.value || []

  if (!pipelineId.value) {
    proxy.$modal.msgWarning("未找到当前流程ID，请先从数据文件管理模块开始流程")
    return
  }

  if (!diagnosisInputList.value.length) {
    proxy.$modal.msgWarning("未找到诊断结果，请先执行故障诊断")
    return
  }

  if (!selectedRows.length) {
    proxy.$modal.msgWarning("请先勾选需要进行根因分析的诊断样本")
    return
  }

  const analyzableRows = selectedRows.filter(row => row.faultType !== "正常")

  if (!analyzableRows.length) {
    proxy.$modal.msgWarning("所选样本均为正常状态，无需生成根因分析结果")
    return
  }

  const rootCauseResults = analyzableRows.map((row, index) => {
    const rootCause = buildRootCauseByDiagnosis(row, index)
    const rootCauseInfo = ROOT_CAUSE_LIBRARY.find(item => item.rootCauseType === rootCause.rootCauseType) || {
      rootCauseType: rootCause.rootCauseType,
      rootCauseCategory: "综合推理",
      rootCauseDesc: rootCause.rootCauseDesc,
      maintenanceSuggestion: rootCause.maintenanceSuggestion
    }
    const distribution = buildRootCauseDistributionByConclusion(row.sampleId, rootCause.rootCauseType, rootCause.probability)

    return {
      analysisId: index + 1,
      analysisCode: `RCA-${String(row.sampleId || index + 1).padStart(3, "0")}`,
      pipelineId: pipelineId.value,
      datasetId: row.datasetId || 1,
      sampleId: row.sampleId,
      sampleCode: row.sampleCode,
      diagnosisId: row.diagnosisId,
      faultType: row.faultType,
      faultLocation: row.faultLocation,
      rootCauseType: rootCause.rootCauseType,
      rootCauseDesc: rootCause.rootCauseDesc,
      probability: rootCause.probability,
      evidenceJson: JSON.stringify(buildRootCauseEvidence(row, rootCause, distribution), null, 2),
      maintenanceSuggestion: rootCause.maintenanceSuggestion,
      analysisMethod: "诊断结果关联 + 融合特征证据链 + 根因机理推理",
      analysisStatus: "已分析",
      analyst: "Topic4-RCA-Engine",
      analysisTime: formatDateTime(new Date()),
      distribution,
      remark: "由诊断结果输入窗口勾选样本后自动生成"
    }
  })

  updateTopic4Pipeline(pipelineId.value, {
    currentStage: "ROOT_CAUSE_DONE",
    status: "已完成",
    rootCauseResults
  })

  resultofrcList.value = filterSummaryRows(rootCauseResults)
  total.value = resultofrcList.value.length
  hasRootCauseResults.value = rootCauseResults.length > 0
  buildStatsFromCurrentList()
  buildChartRecordOptions()

  nextTick(() => {
    initConfidenceChart()
  })

  proxy.$modal.msgSuccess("根因分析完成，已在下方结果记录和饼图中同步展示")
}

function buildRootCauseByDiagnosis(row, index) {
  const map = {
    "内圈故障": {
      rootCauseType: "轴承预紧力设置偏大",
      rootCauseDesc: "轴承预紧力设置偏大，导致运行温升升高、接触应力增大，并诱发内圈区域异常振动。",
      probability: 0.8,
      maintenanceSuggestion: "建议复核轴承预紧参数和装配工艺记录，对预紧力偏大的装配件进行重新调整。"
    },
    "轴承内圈故障": {
      rootCauseType: "轴承预紧力设置偏大",
      rootCauseDesc: "轴承预紧力设置偏大，导致运行温升升高、接触应力增大，并诱发内圈区域异常振动。",
      probability: 0.8,
      maintenanceSuggestion: "建议复核轴承预紧参数和装配工艺记录，对预紧力偏大的装配件进行重新调整。"
    },
    "外圈故障": {
      rootCauseType: "装配同轴度偏差",
      rootCauseDesc: "装配同轴度偏差导致轴承长期承受偏载，外圈接触区域出现局部疲劳损伤。",
      probability: 0.76,
      maintenanceSuggestion: "建议复核轴承座、转轴和端盖装配同轴度，对超差部件重新定位校准。"
    },
    "轴承外圈故障": {
      rootCauseType: "装配同轴度偏差",
      rootCauseDesc: "装配同轴度偏差导致轴承长期承受偏载，外圈接触区域出现局部疲劳损伤。",
      probability: 0.76,
      maintenanceSuggestion: "建议复核轴承座、转轴和端盖装配同轴度，对超差部件重新定位校准。"
    },
    "滚动体故障": {
      rootCauseType: "滚道微小损伤",
      rootCauseDesc: "滚道微小损伤导致滚动体经过缺陷区域时产生周期性冲击，逐步形成滚动体异常特征。",
      probability: 0.72,
      maintenanceSuggestion: "建议对滚道和滚动体表面进行显微检查，重点排查划伤、点蚀和剥落。"
    },
    "轴承滚动体故障": {
      rootCauseType: "滚道微小损伤",
      rootCauseDesc: "滚道微小损伤导致滚动体经过缺陷区域时产生周期性冲击，逐步形成滚动体异常特征。",
      probability: 0.72,
      maintenanceSuggestion: "建议对滚道和滚动体表面进行显微检查，重点排查划伤、点蚀和剥落。"
    },
    "保持架故障": {
      rootCauseType: "密封失效导致污染物进入",
      rootCauseDesc: "污染物进入后造成润滑劣化与异常磨损，保持架区域出现冲击和磨损特征。",
      probability: 0.7,
      maintenanceSuggestion: "建议检查密封完整性、润滑污染程度和保持架磨损状态。"
    }
  }

  return map[row.faultType] || {
    rootCauseType: "润滑脂填充不足",
    rootCauseDesc: "润滑不足导致摩擦升高和局部磨损，进而诱发轴承异常。",
    probability: 0.68,
    maintenanceSuggestion: "建议检查润滑脂型号、填充量和补脂周期。"
  }
}

function buildRootCauseEvidence(diagnosis, rootCause, distribution = []) {
  const rootCauseInfo = ROOT_CAUSE_LIBRARY.find(item => item.rootCauseType === rootCause.rootCauseType) || {
    rootCauseType: rootCause.rootCauseType,
    rootCauseCategory: "综合推理",
    rootCauseDesc: rootCause.rootCauseDesc,
    maintenanceSuggestion: rootCause.maintenanceSuggestion
  }
  const faultFormationProcess = buildFaultFormationProcess(rootCauseInfo, diagnosis.sampleId)

  return {
    pipelineId: pipelineId.value,
    sampleTrace: {
      sampleId: diagnosis.sampleId,
      sampleCode: diagnosis.sampleCode,
      diagnosisId: diagnosis.diagnosisId,
      fusionId: diagnosis.fusionId
    },
    diagnosisEvidence: {
      faultType: diagnosis.faultType,
      faultLocation: diagnosis.faultLocation,
      confidence: diagnosis.confidence,
      healthScore: diagnosis.healthScore
    },
    rootCauseDistribution: distribution.map(item => ({
      rootCauseType: item.rootCauseType,
      rootCauseCategory: item.rootCauseCategory,
      confidence: item.confidence
    })),
    faultFormationProcess,
    causalReasoning: faultFormationProcess.map(item => ({
      step: item.step,
      logic: item.title,
      description: item.description
    })),
    rootCauseConclusion: {
      rootCauseCategory: rootCauseInfo.rootCauseCategory,
      specificRootCause: rootCause.rootCauseType,
      rootCauseType: rootCause.rootCauseType,
      rootCauseDesc: rootCause.rootCauseDesc,
      rootCauseConfidence: rootCause.probability,
      rectificationSuggestion: rootCause.maintenanceSuggestion
    },
    reasoningChain: [
      "预处理样本形成标准化时间窗",
      "样本增强扩充故障样本并改善类别不平衡",
      "特征融合提取多传感器时空特征",
      `故障诊断识别为${diagnosis.faultType}`,
      `根因分析推理得到${rootCause.rootCauseType}`
    ]
  }
}


/** 构造证据链 */
/** 构造证据链：从根因一步一步推导到故障形成 */
function buildEvidenceChain(codeSuffix, probability, rootCauseInfo, distribution = [], sampleId = "-") {
  const faultFormationProcess = buildFaultFormationProcess(rootCauseInfo, sampleId)

  return {
    chainId: "EC-RCA-" + codeSuffix,
    sourceSubject: "课题四",
    targetSubject: "课题五",
    interfaceType: "根因分析结果输出接口",

    diagnosisEvidence: {
      sourceSubject: "课题四",
      diagnosisCode: "DG-" + codeSuffix,
      sampleCode: "SAMPLE-" + codeSuffix,
      faultType: "轴承故障",
      faultLocation: "轴承组件",
      diagnosisConfidence: 0.91,
      healthScore: 72.4,
      evidenceMeaning: "诊断模型识别到样本振动特征与轴承异常模式高度相关。"
    },

    digitalArchiveEvidence: {
      sourceSubject: "课题一",
      productObject: "航空装备轴承部件",
      batchNo: "BATCH-RCA-" + codeSuffix,
      bomNode: "传动系统/轴承组件",
      qualityFeatures: [
        "轴承预紧力",
        "滚道表面状态",
        "材料硬度一致性",
        "润滑脂填充状态",
        "装配同轴度",
        "密封状态"
      ],
      fileReference: "bearing_sample_" + codeSuffix + ".mat",
      evidenceMeaning: "数字卷宗提供该样本的批次、部件、质量特征和原始数据来源。"
    },

    supervisionEvidence: {
      sourceSubject: "课题三",
      abnormalWarning: "振动幅值异常",
      equipmentStatus: "运行状态异常",
      maintenanceRecord: "存在轴承区域振动升高记录",
      processContext: "相同工况下振动特征持续增强",
      evidenceMeaning: "监管数据表明该样本对应设备存在与轴承异常一致的运行波动。"
    },

    rootCauseDistribution: distribution.map(item => ({
      rootCauseType: item.rootCauseType,
      rootCauseCategory: item.rootCauseCategory,
      confidence: item.confidence
    })),

    /**
     * 新增：故障形成过程
     * 表格中的“证据链摘要”就从这里取值。
     */
    faultFormationProcess,

    causalReasoning: faultFormationProcess.map(item => ({
      step: item.step,
      logic: item.title,
      description: item.description
    })),

    rootCauseConclusion: {
      rootCauseCategory: rootCauseInfo.rootCauseCategory,
      specificRootCause: rootCauseInfo.rootCauseType,
      rootCauseType: rootCauseInfo.rootCauseType,
      rootCauseDesc: rootCauseInfo.rootCauseDesc,
      rootCauseConfidence: probability,
      impactScope: "同批次轴承组件及相近工况运行设备",
      responsibleStage: rootCauseInfo.rootCauseCategory,
      rectificationSuggestion: rootCauseInfo.maintenanceSuggestion
    }
  }
}

function parseEvidenceJson(value) {
  if (!value) {
    return {}
  }

  if (typeof value === "object") {
    return value
  }

  try {
    return JSON.parse(value)
  } catch (error) {
    return {}
  }
}

function buildEvidenceView(row) {
  if (!row) {
    return {
      sampleCode: "-",
      steps: [
        { title: "诊断输入", primary: "暂无样本", desc: "请先执行根因分析并选择样本。", type: "input" },
        { title: "异常特征", primary: "待生成", desc: "执行后展示频谱、融合特征和监管证据。", type: "feature" },
        { title: "候选根因", primary: "待生成", desc: "根据根因分布生成候选根因排序。", type: "candidate" },
        { title: "最大根因", primary: "待生成", desc: "展示置信度最高的具体根因。", type: "cause" },
        { title: "整改建议", primary: "待生成", desc: "输出维修与复核建议。", type: "action" }
      ],
      formation: []
    }
  }

  const evidence = parseEvidenceJson(row.evidenceJson)
  const diagnosis = evidence.diagnosisEvidence || {}
  const archive = evidence.digitalArchiveEvidence || {}
  const supervision = evidence.supervisionEvidence || {}
  const conclusion = evidence.rootCauseConclusion || {}
  const distribution = evidence.rootCauseDistribution || row.distribution || getSampleDistribution(row.sampleId)
  const formation = evidence.faultFormationProcess || buildFaultFormationProcess({ rootCauseType: row.rootCauseType }, row.sampleId)

  const candidates = [...distribution]
      .sort((a, b) => Number(b.confidence || 0) - Number(a.confidence || 0))
      .slice(0, 3)
      .map(item => `${item.rootCauseType} ${Number(item.confidence || 0).toFixed(0)}%`)
      .join(" / ")

  const featureEvidence = [
    diagnosis.evidenceMeaning || `${row.faultLocation || "轴承组件"}识别为${row.rootCauseType || "异常根因"}相关故障`,
    supervision.abnormalWarning ? `监管异常：${supervision.abnormalWarning}` : "监管异常：振动幅值异常",
    archive.fileReference ? `样本文件：${archive.fileReference}` : `样本编号：${row.sampleCode || `SAMPLE-${row.sampleId}`}`
  ].join("；")

  return {
    sampleCode: row.sampleCode || `SAMPLE-${row.sampleId || "-"}`,
    steps: [
      {
        title: "诊断输入",
        primary: `${diagnosis.faultLocation || row.faultLocation || "轴承组件"} / ${diagnosis.faultType || "轴承故障"}`,
        desc: `诊断置信度 ${formatProbability(diagnosis.diagnosisConfidence ?? row.probability)}，健康评分 ${diagnosis.healthScore || row.healthScore || "72.4"}`,
        type: "input"
      },
      {
        title: "异常特征",
        primary: "振动异常 + 融合特征偏离",
        desc: featureEvidence,
        type: "feature"
      },
      {
        title: "候选根因",
        primary: candidates || "候选根因分布待生成",
        desc: "根据诊断结果、融合特征和数字卷宗证据生成六类根因置信度排序。",
        type: "candidate"
      },
      {
        title: "最大根因",
        primary: conclusion.specificRootCause || row.rootCauseType || "-",
        desc: `最大根因置信度 ${formatProbability(conclusion.rootCauseConfidence ?? row.probability)}，责任阶段：${conclusion.responsibleStage || "装配 / 材料 / 润滑综合复核"}`,
        type: "cause"
      },
      {
        title: "整改建议",
        primary: "复核并闭环处理",
        desc: conclusion.rectificationSuggestion || row.maintenanceSuggestion || "建议结合装配记录、润滑状态和同批次质量数据进行复核。",
        type: "action"
      }
    ],
    formation: formation.slice(0, 6)
  }
}

/** 构建根因判断文本 */
function buildRootCauseJudgment(row) {
  const specificRootCause = row.rootCauseType || "-"
  const desc = row.rootCauseDesc || "-"

  return `${specificRootCause}：${desc}`
}

/** 表格中显示证据链摘要：根因 → 机理演化 → 故障形成 */
function buildEvidenceSummary(value) {
  if (!value) {
    return "暂无证据链"
  }

  try {
    const json = typeof value === "string" ? JSON.parse(value) : value
    const process = json.faultFormationProcess || []

    if (process.length > 0) {
      return process.map(item => item.title).join(" → ")
    }

    const reasoning = json.causalReasoning || []

    if (reasoning.length > 0) {
      return reasoning.map(item => item.logic || item.description).join(" → ")
    }

    const specificRootCause = json.rootCauseConclusion?.specificRootCause || "最大置信度根因"

    return `${specificRootCause} → 局部异常累积 → 振动特征增强 → 轴承故障形成`
  } catch (e) {
    return "根因异常 → 局部损伤累积 → 振动响应增强 → 轴承故障形成"
  }
}

/** 根因类型标签 */
function rootCauseTypeTag(value) {
  const rootCause = ROOT_CAUSE_LIBRARY.find(item => item.rootCauseType === value)

  if (!rootCause) {
    return "info"
  }

  const categoryMap = {
    "装配参数": "warning",
    "表面损伤": "danger",
    "材料一致性": "danger",
    "润滑维护": "success",
    "装配工艺": "warning",
    "环境污染": "info"
  }

  return categoryMap[rootCause.rootCauseCategory] || "info"
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

onMounted(() => {
  loadPipelineInput()
  getList()
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

.table-card,
.interface-card,
.confidence-overview,
.rootcause-input-card {
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.06);
}

.confidence-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(420px, 0.9fr);
  gap: 18px;
  align-items: stretch;
}

.stats-only-overview {
  grid-template-columns: 1fr;
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

.rootcause-input-card {
  overflow: hidden;
}

.rootcause-input-header {
  align-items: flex-start;
}

.section-desc {
  margin: 6px 0 0;
  color: #5d728c;
  font-size: 13px;
  line-height: 1.7;
}

.rootcause-input-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: stretch;
}

.input-table-panel,
.input-action-panel {
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

  h4 {
    margin: 0;
    color: #0c2b52;
    font-size: 16px;
    font-weight: 800;
  }

  p {
    margin: 6px 0 0;
    color: #6b7f99;
    font-size: 12px;
    line-height: 1.6;
  }
}

.panel-title-row.compact {
  margin-bottom: 12px;
}

.input-summary-list {
  display: grid;
  gap: 10px;
}

.summary-box {
  padding: 12px 14px;
  border: 1px solid #dbeaf8;
  border-radius: 12px;
  background: #f6faff;

  span {
    display: block;
    margin-bottom: 6px;
    color: #6b7f99;
    font-size: 12px;
    font-weight: 700;
  }

  strong {
    display: block;
    color: #0c2b52;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.5;
    word-break: break-all;
  }
}

.run-rootcause-btn {
  width: 100%;
  height: 40px;
  margin-top: 16px;
  font-weight: 800;
}

.rootcause-chart-panel {
  margin-top: 16px;
  padding: 16px 18px 12px;
  border: 1px solid #d6e7f7;
  border-radius: 16px;
  background: #ffffff;
  min-width: 0;
  overflow: hidden;
}

.rootcause-inline-chart {
  height: 420px;
}

.chart-card {
  height: 100%;
  padding: 4px 4px 0;
}

.confidence-chart {
  width: 100%;
  height: 420px;
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

.evidence-chain-panel {
  margin-top: 18px;
  padding: 16px 18px;
  border: 1px solid #dbe9f7;
  border-radius: 16px;
  background: #ffffff;
}

.evidence-chain-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  h4 {
    margin: 0;
    color: #0c2b52;
    font-size: 18px;
    font-weight: 900;
  }

  p {
    margin: 6px 0 0;
    color: #6b7f99;
    font-size: 13px;
    line-height: 1.6;
  }
}

.evidence-flow-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) 26px minmax(0, 1.15fr) 26px minmax(0, 1.18fr) 26px minmax(0, 1.1fr) 26px minmax(0, 1.1fr);
  align-items: stretch;
  gap: 8px;
}

.evidence-flow-card {
  display: flex;
  gap: 10px;
  min-height: 126px;
  padding: 14px;
  border: 1px solid #d8e8f8;
  border-radius: 14px;
  background: #f8fbff;
  box-shadow: 0 8px 20px rgba(31, 95, 160, 0.05);
}

.evidence-flow-card.feature {
  background: #fffaf2;
  border-color: #f5dfb8;
}

.evidence-flow-card.candidate {
  background: #f7fff5;
  border-color: #cfe9c9;
}

.evidence-flow-card.cause {
  background: #fff7f7;
  border-color: #f4c7c7;
}

.evidence-flow-card.action {
  background: #f7f4ff;
  border-color: #d8cef7;
}

.evidence-step-index {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 28px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #3a8ee6, #1f6fd1);
  font-size: 13px;
  font-weight: 900;
}

.evidence-step-main {
  min-width: 0;

  span {
    display: block;
    color: #63809d;
    font-size: 12px;
    font-weight: 800;
  }

  strong {
    display: block;
    margin-top: 6px;
    color: #0c2b52;
    font-size: 14px;
    line-height: 1.45;
    font-weight: 900;
  }

  p {
    margin: 8px 0 0;
    color: #4f647f;
    font-size: 12px;
    line-height: 1.65;
  }
}

.evidence-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #76a6d9;
  font-size: 22px;
  font-weight: 900;
}

.fault-formation-chain {
  margin-top: 14px;
  padding: 12px 14px;
  border: 1px dashed #cbdff4;
  border-radius: 14px;
  background: #fbfdff;
}

.formation-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 13px;
  font-weight: 900;
}

.formation-steps {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.formation-pill {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 5px 10px;
  border-radius: 999px;
  color: #244568;
  background: #eef6ff;
  border: 1px solid #d5e7f9;
  font-size: 12px;
  font-weight: 700;
}

.formation-arrow {
  color: #7ea7d2;
  font-weight: 900;
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

.table-filter-panel {
  margin-bottom: 14px;
  padding: 14px 16px 2px;
  border: 1px solid #d6e7f7;
  border-radius: 14px;
  background: #ffffff;
}

.table-filter-title {
  margin-bottom: 10px;
  color: #0c2b52;
  font-size: 15px;
  font-weight: 800;
}

.table-filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.interface-collapse-card {
  padding: 10px 18px;
  background: #ffffff;
}

.interface-collapse-card :deep(.el-collapse) {
  border: none;
}

.interface-collapse-card :deep(.el-collapse-item__header) {
  border: none;
  color: #0c2b52;
  font-size: 16px;
  font-weight: 800;
}

.interface-collapse-card :deep(.el-collapse-item__wrap) {
  border: none;
}

.interface-collapse-card :deep(.el-collapse-item__content) {
  padding-bottom: 10px;
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

.sample-chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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

:deep(.el-dialog__title) {
  font-weight: 800;
  color: #0c2b52;
}

@media screen and (max-width: 1400px) {
  .confidence-overview {
    grid-template-columns: 1fr;
  }

  .rootcause-input-grid {
    grid-template-columns: 1fr;
  }

  .evidence-flow-grid {
    grid-template-columns: 1fr;
  }

  .evidence-arrow {
    transform: rotate(90deg);
    min-height: 24px;
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


<template>
  <div class="app-container topic5-graph-page">

    <!-- 顶部流程 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>航空装备全生命周期数字质量自反馈与追溯流程</span>
          <span class="header-tip">当前选择任务：{{ currentTrace.traceNo || '未选择' }}</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="任务开始" />
        <el-step title="问题填报" />
        <el-step title="原始数据获取" />
        <el-step title="故障根因分析" />
        <el-step title="卷宗实体映射" />
        <el-step title="溯源图谱构建" />
        <el-step title="全链路追溯闭环" />
        <el-step title="任务完成" />
      </el-steps>
    </el-card>

    

    <!-- 当前追溯任务信息 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>当前追溯任务信息</span>
      </template>

      <el-empty v-if="!currentTrace.id" description="请在首页选择选择追溯任务" />

      <el-descriptions v-else :column="3" border>
        <el-descriptions-item label="追溯任务编号">
          {{ currentTrace.traceNo }}
        </el-descriptions-item>

        <el-descriptions-item label="发生时间">
          {{ currentTrace.eventTime }}
        </el-descriptions-item>

        <el-descriptions-item label="架次">
          {{ currentTrace.aircraftNo }}
        </el-descriptions-item>

        <el-descriptions-item label="发生部位">
          {{ currentTrace.partName }}
        </el-descriptions-item>

        <el-descriptions-item label="问题类型">
          {{ currentTrace.problemType }}
        </el-descriptions-item>

        <el-descriptions-item label="当前流程">
          {{ workflowName(currentTrace.workflowStage) }}
        </el-descriptions-item>

        <el-descriptions-item label="问题描述" :span="3">
          {{ currentTrace.problemDescription }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 原始知识图谱导入 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>原始知识图谱导入</span>
        </div>
      </template>

      <el-button type="primary" icon="Connection"  :disabled="!selectedTraceId" @click="handlePullTopic1Kg">
        从数字卷宗拉取知识图谱
      </el-button>

      <el-alert
        class="mt15"
        title="系统将根据当前追溯任务的发生时间、架次和发生部位，从数字卷宗中获取相关知识图谱。"
        type="info"
        show-icon
        :closable="false"
      />

      <div class="graph-title mt15">原始知识图谱</div>
      <div ref="originGraphRef" class="graph-container"></div>
    </el-card>

    <!-- 第二部分算法运行 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>溯源图谱构建</span>
        </div>
      </template>

        <el-row :gutter="12" align="middle" class="algorithm-action-row">
          <!-- <el-col :span="10">
            <el-select
              v-model="algorithmForm.algorithmName"
              placeholder="请选择溯源模型"
              style="width: 100%"
            >
              <el-option label="默认溯源模型（default）" value="default" />
              <el-option label="评估模式溯源模型（eval）" value="eval" />
            </el-select>
          </el-col> -->

          <el-col :span="5">
            <el-button
              type="warning"
              icon="Cpu"
              :loading="secondAlgorithmRunning"
              :disabled="secondAlgorithmRunning || !selectedTraceId"
              @click="handleRunSecondAlgorithm"
            >
              运行算法
            </el-button>
          </el-col>

          <el-col :span="8">
            <div class="algorithm-status-inline">
              <span class="algorithm-status-label">算法状态：</span>
              <el-tag :type="secondAlgorithmTagType(currentTrace.secondAlgorithmStatus)">
                {{ secondAlgorithmStatusName(currentTrace.secondAlgorithmStatus) }}
              </el-tag>
            </div>
          </el-col>
        </el-row>

      <!-- <div class="graph-title mt15">溯源图谱构建结果</div> -->

      <el-empty
        v-if="summaryRows.length === 0 && componentDiagnosisTop3.length === 0 && subtypeTop5.length === 0"
        description="暂无算法结果，请先运行算法"
      />

      <div v-else>
        <!-- RCA核心诊断结果 -->
        <div class="sub-title mt15">RCA核心诊断结果</div>
        <el-table
          :data="summaryRows"
          border
          style="width: 100%"
        >
          <el-table-column prop="rank" label="序号" width="80" align="center" />

          <el-table-column prop="fieldName" label="结果字段" min-width="180" />

          <el-table-column prop="fieldValue" label="结果内容" min-width="320" show-overflow-tooltip>
            <template #default="scope">
              {{ formatResultValue(scope.row.fieldValue) }}
            </template>
          </el-table-column>

          <el-table-column prop="description" label="字段说明" min-width="300" show-overflow-tooltip />
        </el-table>

        <!-- 部件诊断 Top3 -->
        <div class="sub-title mt15">部件诊断 Top3</div>
        <el-table
          :data="componentDiagnosisTop3"
          border
          style="width: 100%"
        >
          <el-table-column prop="rank" label="排名" width="80" align="center" />

          <el-table-column prop="componentCode" label="部件编码" min-width="120" />

          <el-table-column prop="componentName" label="部件名称" min-width="140" />

          <el-table-column prop="faultType" label="故障类型" min-width="160" />

          <el-table-column prop="confidence" label="置信度/评分" width="130" align="center">
            <template #default="scope">
              {{ formatConfidence(scope.row.confidence) }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 故障子类型 Top5 -->
        <div class="sub-title mt15">故障子类型 Top5</div>
        <el-table
          :data="subtypeTop5"
          border
          style="width: 100%"
        >
          <el-table-column prop="rank" label="排名" width="80" align="center" />

          <el-table-column prop="subtypeName" label="故障子类型" min-width="200" />

          <el-table-column prop="confidence" label="概率/置信度" width="130" align="center">
            <template #default="scope">
              {{ formatConfidence(scope.row.confidence) }}
            </template>
          </el-table-column>

          <el-table-column prop="relatedComponent" label="关联部件" min-width="160" />

          <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
        </el-table>

        <!-- <el-collapse class="mt15">
          <el-collapse-item title="查看原始算法结果 JSON" name="raw">
            <el-input
              type="textarea"
              :rows="10"
              :model-value="currentTrace.secondAlgorithmResultJson"
              readonly
            />
          </el-collapse-item>
        </el-collapse> -->
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance , onActivated} from 'vue'
import * as echarts from 'echarts'
import {
  getGraphTrace,
  pullTopic1Kg,
  runSecondAlgorithm,
  getTraceKg,
} from '@/api/topic5/graph'
import { getTopic5CurrentTraceId, getTopic5CurrentTrace } from '@/utils/topic5CurrentTrace'

const { proxy } = getCurrentInstance()

const selectedTraceId = ref(null)
const currentTrace = ref({})

const originGraphRef = ref(null)
let originChart = null

const secondAlgorithmRunning = ref(false)

const DEFAULT_SECOND_ALGORITHM_NAME = 'default'

const algorithmForm = reactive({
  algorithmName: DEFAULT_SECOND_ALGORITHM_NAME
})


const secondResultObj = computed(() => {
  return parseSecondResultObject(currentTrace.value.secondAlgorithmResultJson)
})

const summaryRows = computed(() => {
  return secondResultObj.value.summaryRows || []
})

const componentDiagnosisTop3 = computed(() => {
  return normalizeComponentDiagnosisRows(
      secondResultObj.value.componentDiagnosisTop3 || [],
      secondResultObj.value.subtypeTop5 || []
  )
})

const subtypeTop5 = computed(() => {
  return normalizeSubtypeRows(secondResultObj.value.subtypeTop5 || [])
})

const activeStep = computed(() => {
  const stage = Number(currentTrace.value.workflowStage || 0)

  if (stage <= 0) return 0
  if (stage === 1) return 2
  if (stage === 2) return 3
  if (stage === 3) return 4
  if (stage === 4) return 5
  if (stage === 5) return 6
  if (stage >= 6) return 8

  return 0
})


function initCurrentTraceFromFirstPage() {
  const traceId = getTopic5CurrentTraceId()
  const trace = getTopic5CurrentTrace()

  if (!traceId) {
    proxy.$modal.msgWarning('请先在第一个页面选择追溯任务')
    selectedTraceId.value = null
    currentTrace.value = {}
    clearSecondPageResult()
    return
  }

  // 如果当前页面缓存的任务ID和第一个页面选择的不一致，必须先清空旧结果
  if (Number(selectedTraceId.value) !== Number(traceId)) {
    clearSecondPageResult()
  }

  selectedTraceId.value = Number(traceId)
  currentTrace.value = trace || {}

  loadCurrentTraceData()
}
function loadCurrentTraceData() {
  if (!selectedTraceId.value) {
    return
  }

  getGraphTrace(selectedTraceId.value).then(res => {
    const detail = res.data || {}

    currentTrace.value = {
      ...detail
    }

    algorithmForm.algorithmName =
      currentTrace.value.secondAlgorithmName || DEFAULT_SECOND_ALGORITHM_NAME

    loadTraceKg(selectedTraceId.value)
  }).catch(err => {
    console.error('加载当前追溯任务失败：', err)
    proxy.$modal.msgError('加载当前追溯任务失败，请返回第一个页面重新选择任务')
  })
}

function loadTraceKg(id) {
  getTraceKg(id).then(res => {
    const data = res.data || {}
    const traceProblem = data.traceProblem || currentTrace.value || {}

    currentTrace.value = {
      ...traceProblem,
      topic1KgJson: data.topic1KgJson || traceProblem.topic1KgJson || '',
      secondAlgorithmResultJson: data.secondAlgorithmResultJson || traceProblem.secondAlgorithmResultJson || ''
    }

    if (data.topic1KgJson) {
      const graphData = parseGraphJson(data.topic1KgJson)

      if (graphData) {
        renderGraph(originGraphRef.value, graphData)
      } else {
        clearOriginGraph(originGraphRef.value)
      }
    } else {
      clearOriginGraph(originGraphRef.value)
    }
  })
}

function handlePullTopic1Kg() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  pullTopic1Kg(selectedTraceId.value).then(res => {
    proxy.$modal.msgSuccess('原始知识图谱拉取成功')
    currentTrace.value = res.data.traceProblem || currentTrace.value

    const graphData = res.data.graphData || null

      if (graphData) {
        nextTick(() => {
          renderGraph(originGraphRef.value, graphData)
        })
      }

      refreshCurrentTrace()
  })
}

function handleRunSecondAlgorithm() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  secondAlgorithmRunning.value = true

  runSecondAlgorithm(selectedTraceId.value, {
    algorithmName: algorithmForm.algorithmName || DEFAULT_SECOND_ALGORITHM_NAME
  }).then(res => {
    proxy.$modal.msgSuccess('知识图谱算法运行完成')

    const data = res.data || {}

    if (data.traceProblem) {
      currentTrace.value = data.traceProblem
    }

    if (data.secondAlgorithmResultJson) {
      currentTrace.value.secondAlgorithmResultJson = data.secondAlgorithmResultJson
    }

    refreshCurrentTrace()
  }).catch(err => {
    console.error('知识图谱算法运行失败：', err)

    const msg =
      err?.response?.data?.msg ||
      err?.response?.data?.message ||
      err?.message ||
      '知识图谱算法运行失败，请检查 Java 后端和 Python FastAPI 服务'

    proxy.$modal.msgError(msg)
  }).finally(() => {
    secondAlgorithmRunning.value = false
  })
}

function refreshCurrentTrace() {
  if (!selectedTraceId.value) return

  getGraphTrace(selectedTraceId.value).then(res => {
    currentTrace.value = res.data || {}
    algorithmForm.algorithmName =
      currentTrace.value.secondAlgorithmName || algorithmForm.algorithmName

    loadTraceKg(selectedTraceId.value)
  })
}

function parseSecondResultObject(value) {
  if (!value) {
    return {
      summaryRows: [],
      componentDiagnosisTop3: [],
      subtypeTop5: []
    }
  }

  let obj = {}

  try {
    obj = typeof value === 'string' ? JSON.parse(value) : value
  } catch (e) {
    console.error('知识图谱算法结果 JSON 解析失败：', e)
    return {
      summaryRows: [],
      componentDiagnosisTop3: [],
      subtypeTop5: []
    }
  }

  const pythonResponse =
      obj.pythonResponse ||
      obj.python_response ||
      obj

  const reasoning = pythonResponse.reasoning || {}
  const rcaContext = reasoning.rca_context || reasoning.rcaContext || {}

  const firstContext =
      pythonResponse.first_algorithm_context ||
      pythonResponse.firstAlgorithmContext ||
      obj.first_algorithm_context ||
      obj.firstAlgorithmContext ||
      {}

  const rawContext =
      firstContext.raw && typeof firstContext.raw === 'object'
          ? firstContext.raw
          : {}

  const componentDiagnosisTop3 =
      obj.componentDiagnosisTop3 ||
      obj.component_diagnosis_top3 ||
      rcaContext.component_diagnosis_top3 ||
      rcaContext.componentDiagnosisTop3 ||
      firstContext.component_diagnosis_top3 ||
      firstContext.componentDiagnostics ||
      rawContext.component_diagnosis_top3 ||
      rawContext.componentDiagnostics ||
      []

  const subtypeTop5 =
      obj.subtypeTop5 ||
      obj.subtype_top5 ||
      rcaContext.subtype_top5 ||
      rcaContext.subtypeTop5 ||
      rcaContext.root_component_subtype_topk ||
      firstContext.subtype_top5 ||
      firstContext.root_component_subtype_topk ||
      firstContext.subtypeProbabilities ||
      rawContext.subtype_top5 ||
      rawContext.root_component_subtype_topk ||
      rawContext.subtypeProbabilities ||
      []

  const summaryRows =
      obj.summaryRows ||
      obj.summary_rows ||
      []

  return {
    summaryRows,
    componentDiagnosisTop3,
    subtypeTop5
  }
}

function getComponentCodeFromRow(item) {
  const directCode =
      item.componentCode ||
      item.component_id ||
      item.component_code ||
      item.componentId ||
      item.root_component_code ||
      ''

  if (directCode) {
    return directCode
  }

  const subTypeId =
      item.sub_type_id ||
      item.subtype_id ||
      item.subTypeId ||
      item.id ||
      ''

  // 例如 C003-S2 -> C003
  const match = String(subTypeId).match(/^(C\d{3})-/)
  if (match) {
    return match[1]
  }

  return ''
}

function getComponentNameByCode(code) {
  const map = {
    C001: '液压泵总成',
    C002: '方向控制阀',
    C003: '作动筒',
    C004: '溢流阀',
    C005: '蓄能器',
    C006: '冷却器',
    C007: '过滤器',
    C008: '液压油箱',
    C009: '单向阀',
    C010: '节流阀',
    C011: '管路总成',
    C012: '卸荷阀'
  }

  return map[code] || ''
}
function normalizeComponentDiagnosisRows(componentList, subtypeList = []) {
  const rows = []

  if (Array.isArray(componentList)) {
    componentList.forEach(item => {
      const score = Number(
          item.confidence ??
          item.probability ??
          item.score ??
          item.severity ??
          item.component_confidence ??
          item.rca_confidence ??
          0
      )

      const componentCode = getComponentCodeFromRow(item)
      const componentName =
          item.componentName ||
          item.component_name ||
          item.root_component_name ||
          getComponentNameByCode(componentCode) ||
          '-'

      rows.push({
        ...item,
        rank: 0,

        // 同时给两套字段，避免模板 prop 不一致导致空白
        componentId: componentCode,
        componentCode: componentCode,
        component_id: componentCode,

        componentName: componentName,
        component_name: componentName,

        faultType:
            item.faultType ||
            item.fault_type ||
            item.sub_type_name ||
            item.subtypeName ||
            '-',

        confidence: Number.isNaN(score) ? 0 : score
      })
    })
  }

  if (Array.isArray(subtypeList)) {
    subtypeList.forEach(item => {
      const score = Number(
          item.confidence ??
          item.probability ??
          item.score ??
          item.severity ??
          0
      )

      const componentCode = getComponentCodeFromRow(item)
      const componentName =
          item.componentName ||
          item.component_name ||
          getComponentNameByCode(componentCode) ||
          '-'

      rows.push({
        ...item,
        rank: 0,

        componentId: componentCode,
        componentCode: componentCode,
        component_id: componentCode,

        componentName: componentName,
        component_name: componentName,

        faultType:
            item.faultType ||
            item.fault_type ||
            item.sub_type_name ||
            item.subtypeName ||
            item.subTypeName ||
            '-',

        confidence: Number.isNaN(score) ? 0 : score
      })
    })
  }

  const uniqueRows = []
  const seen = new Set()

  rows
      .filter(item => item.componentCode || item.componentId || item.component_id)
      .sort((a, b) => b.confidence - a.confidence)
      .forEach(item => {
        const componentCode =
            item.componentCode ||
            item.componentId ||
            item.component_id ||
            ''

        const faultType =
            item.faultType ||
            item.fault_type ||
            ''

        // 用“部件编码 + 故障类型”去重
        const key = `${componentCode}_${faultType}`

        if (!seen.has(key)) {
          seen.add(key)
          uniqueRows.push(item)
        }
      })

  return uniqueRows
      .slice(0, 3)
      .map((item, index) => ({
        ...item,
        rank: index + 1
      }))
}

function normalizeSubtypeRows(list) {
  if (!Array.isArray(list)) {
    return []
  }

  return list
      .map(item => {
        const score = Number(
            item.confidence ??
            item.probability ??
            item.score ??
            item.severity ??
            0
        )

        const componentCode = getComponentCodeFromRow(item)

        const componentName =
            item.relatedComponent ||
            item.component_name ||
            item.componentName ||
            getComponentNameByCode(componentCode) ||
            componentCode ||
            '-'

        return {
          ...item,
          rank: 0,

          // 表格用字段
          subtypeName:
              item.subtypeName ||
              item.sub_type_name ||
              item.subtype_name ||
              item.subTypeName ||
              item.fault_type ||
              item.sub_type_id ||
              '-',

          confidence: Number.isNaN(score) ? 0 : score,

          relatedComponent: componentName,

          description:
              item.description ||
              item.fault_part ||
              item.faultPart ||
              item.sub_type_id ||
              '-',

          // 兼容字段，后面如果表格 prop 换名字也不会空
          componentCode,
          componentId: componentCode,
          componentName
        }
      })
      .sort((a, b) => b.confidence - a.confidence)
      .slice(0, 5)
      .map((item, index) => ({
        ...item,
        rank: index + 1
      }))
}

function clearSecondPageResult() {
  currentTrace.value = {
    id: currentTrace.value.id,
    traceNo: currentTrace.value.traceNo,
    eventTime: currentTrace.value.eventTime,
    aircraftNo: currentTrace.value.aircraftNo,
    partName: currentTrace.value.partName,
    problemType: currentTrace.value.problemType,
    problemDescription: currentTrace.value.problemDescription,
    workflowStage: currentTrace.value.workflowStage,
    status: currentTrace.value.status
  }

  if (originChart) {
    originChart.clear()
  }
}

function formatResultValue(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  if (typeof value === 'object') {
    return JSON.stringify(value)
  }

  return value
}

function parseGraphJson(json) {
  if (!json) {
    return null
  }

  if (typeof json === 'object') {
    return json
  }

  try {
    return JSON.parse(json)
  } catch (e) {
    console.error('知识图谱 JSON 解析失败：', e)
    return null
  }
}

function renderGraph(dom, graphData) {
  if (!dom) return

  nextTick(() => {
    if (!originChart) {
      originChart = echarts.init(dom)
    }

    const option = normalizeGraphOption(graphData)

    if (!option) {
      console.warn('无法识别的知识图谱格式：', graphData)
      originChart.clear()
      return
    }

    originChart.clear()
    originChart.setOption(option, true)

    originChart.resize()

    setTimeout(() => {
      if (originChart) {
        originChart.resize()
      }
    }, 100)
  })
}

function normalizeGraphOption(graphData) {
  if (!graphData) {
    return null
  }

  let data = graphData

  if (typeof data === 'string') {
    try {
      data = JSON.parse(data)
    } catch (e) {
      console.error('知识图谱字符串解析失败：', e)
      return null
    }
  }

  /*
   * 情况1：后端直接返回完整 ECharts option
   * 你现在的新 JSON 如果后端保存的是 echartsOption，
   * 这里会直接命中。
   */
  if (data.series && Array.isArray(data.series)) {
    return patchGraphOption(data)
  }

  /*
   * 情况2：后端返回完整外层 JSON，里面包含 echartsOption
   */
  if (data.echartsOption && data.echartsOption.series) {
    return patchGraphOption(data.echartsOption)
  }

  /*
   * 情况3：后端返回 rawGraph
   */
  if (data.rawGraph) {
    const rawGraph = data.rawGraph

    if (rawGraph.nodes && rawGraph.edges) {
      return buildGraphOptionFromNodesLinks(rawGraph.nodes, rawGraph.edges, 'C011 管路总成知识图谱')
    }

    if (rawGraph.nodes && rawGraph.links) {
      return buildGraphOptionFromNodesLinks(rawGraph.nodes, rawGraph.links, 'C011 管路总成知识图谱')
    }
  }

  /*
   * 情况4：旧格式 nodes / links
   */
  if (data.nodes && data.links) {
    return buildGraphOptionFromNodesLinks(data.nodes, data.links, '原始知识图谱')
  }

  /*
   * 情况5：旧格式 nodes / edges
   */
  if (data.nodes && data.edges) {
    return buildGraphOptionFromNodesLinks(data.nodes, data.edges, '原始知识图谱')
  }

  return null
}

function patchGraphOption(option) {
  const finalOption = JSON.parse(JSON.stringify(option))

  if (!finalOption.tooltip) {
    finalOption.tooltip = {
      trigger: 'item',
      confine: true
    }
  }

  if (!finalOption.series || !Array.isArray(finalOption.series) || finalOption.series.length === 0) {
    return finalOption
  }

  const series = finalOption.series[0]

  series.type = 'graph'
  series.layout = series.layout || 'force'
  series.roam = true
  series.draggable = true
  series.focusNodeAdjacency = true

  if (!series.label) {
    series.label = {
      show: true,
      position: 'right',
      formatter: '{b}'
    }
  }

  if (!series.edgeLabel) {
    series.edgeLabel = {
      show: true,
      formatter: function (params) {
        return params.data.name || ''
      }
    }
  }

  series.force = {
    ...series.force,

    // 节点之间的斥力，越大越分散
    repulsion: 950,

    // 边长范围，越大节点间距越宽
    edgeLength: [150, 300],

    // 重力越小，节点越不容易挤到中心
    gravity: 0.025
  }

  return finalOption
}

function buildGraphOptionFromNodesLinks(nodes, links, title = '原始知识图谱') {
  const safeNodes = Array.isArray(nodes) ? nodes : []
  const safeLinks = Array.isArray(links) ? links : []

  const categories = []
  const categorySet = new Set()

  safeNodes.forEach(node => {
    const categoryName = node.category || node.stage_name || node.type || '默认类别'

    if (!categorySet.has(categoryName)) {
      categorySet.add(categoryName)
      categories.push({ name: categoryName })
    }
  })

  return {
    title: {
      text: title
    },
    tooltip: {
      trigger: 'item',
      confine: true
    },
    legend: [
      {
        data: categories.map(item => item.name)
      }
    ],
    series: [
      {
        name: title,
        type: 'graph',
        layout: 'force',
        roam: true,
        draggable: true,
        focusNodeAdjacency: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        edgeLabel: {
          show: true,
          formatter: function (params) {
            return params.data.name || ''
          }
        },
        force: {
          repulsion: 1200,
          edgeLength: [180, 360],
          gravity: 0.002
        },
        categories,
        data: safeNodes.map(node => {
          const categoryName = node.category || node.stage_name || node.type || '默认类别'

          return {
            id: node.id,
            name: node.name || node.id,
            value: node.score || node.value || 1,
            category: categoryName,
            symbolSize: node.symbolSize || 42,
            properties: node,
            label: node.label || {
              show: true,
              formatter: node.name || node.id
            }
          }
        }),
        links: safeLinks.map(edge => ({
          source: edge.source,
          target: edge.target,
          name: edge.relation_name || edge.name || edge.relation || '',
          value: edge.weight || edge.value || edge.score || 1,
          properties: edge
        }))
      }
    ]
  }
}

function clearOriginGraph(dom) {
  if (!dom) return

  nextTick(() => {
    if (!originChart) originChart = echarts.init(dom)
    originChart.clear()
  })
}

function categoryIndex(categories, name) {
  const index = categories.findIndex(item => item.name === name)
  return index >= 0 ? index : 0
}

function formatConfidence(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  const num = Number(value)

  if (Number.isNaN(num)) {
    return value
  }

  if (num <= 1) {
    return (num * 100).toFixed(2) + '%'
  }

  return num.toFixed(2) + '%'
}

function workflowName(stage) {
  const value = Number(stage)

  if (value === 1) return '问题填报'
  if (value === 2) return '原始数据获取'
  if (value === 3) return '故障根因分析'
  if (value === 4) return '卷宗实体映射'
  if (value === 5) return '溯源图谱构建'
  if (value === 6) return '全链路追溯闭环'

  return '未开始'
}

function scrollToTop() {
  nextTick(() => {
    window.scrollTo(0, 0)
    document.documentElement.scrollTop = 0
    document.body.scrollTop = 0

    const appMain = document.querySelector('.app-main')
    if (appMain) {
      appMain.scrollTop = 0
    }

    const scrollWrap = document.querySelector('.el-scrollbar__wrap')
    if (scrollWrap) {
      scrollWrap.scrollTop = 0
    }
  })
}

function secondAlgorithmStatusName(status) {
  const value = Number(status)
  if (value === 0) return '未运行'
  if (value === 1) return '运行中'
  if (value === 2) return '已完成'
  if (value === 3) return '运行失败'
  return '未运行'
}

function secondAlgorithmTagType(status) {
  const value = Number(status)
  if (value === 0) return 'info'
  if (value === 1) return 'warning'
  if (value === 2) return 'success'
  if (value === 3) return 'danger'
  return 'info'
}

onMounted(() => {
  scrollToTop()
  initCurrentTraceFromFirstPage()

  window.addEventListener('resize', () => {
    if (originChart) originChart.resize()
  })
})

onActivated(() => {
  scrollToTop()
  initCurrentTraceFromFirstPage()
})
</script>

<style scoped>
.topic5-graph-page {
  padding-bottom: 24px;
}

.mt15 {
  margin-top: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-tip {
  font-size: 13px;
  color: #909399;
}

.algorithm-action-row {
  margin-bottom: 15px;
}

.algorithm-status-inline {
  display: flex;
  align-items: center;
  height: 32px;
}

.algorithm-status-label {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
}

.graph-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.sub-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.graph-container {
  width: 100%;
  height: 700px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
  margin-top: 10px;
}

.dialog-footer {
  text-align: right;
}
</style>
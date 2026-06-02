<template>
  <div class="app-container lifecycle-model-page">
    <!-- 页面说明 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>全生命周期关联模型构建</span>
        </div>
      </template>

      <el-alert
        title="本模块以故障零部件ID为索引，融合模拟数字卷宗中的设计、材料、制造、检测、装配、服役与维修数据，构建面向故障追溯的全生命周期图网络关联模型。"
        type="info"
        show-icon
        :closable="false"
      />
    </el-card>

    <!-- 第一部分：追溯案例与故障零件信息 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>一、追溯案例与故障零件信息</span>
        </div>
      </template>

      <el-form label-width="140px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="追溯案例">
              <el-select
                v-model="selectedCaseId"
                filterable
                clearable
                placeholder="请选择已完成零件定位的追溯案例"
                style="width: 100%"
                @change="handleCaseChange"
              >
                <el-option
                  v-for="item in traceCaseOptions"
                  :key="item.caseId"
                  :label="formatCaseLabel(item)"
                  :value="item.caseId"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="案例状态">
              <el-tag :type="caseStatusType(currentCase.caseStatus)">
                {{ caseStatusLabel(currentCase.caseStatus) }}
              </el-tag>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="追溯案例ID">
              <el-input v-model="currentCase.caseId" disabled placeholder="请选择追溯案例" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="案例编号">
              <el-input v-model="currentCase.caseNo" disabled placeholder="-" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="案例名称">
              <el-input v-model="currentCase.caseName" disabled placeholder="-" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="故障零件ID">
              <el-input v-model="currentCase.finalPartId" disabled placeholder="尚未定位" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="故障零件编号">
              <el-input v-model="currentCase.finalPartNo" disabled placeholder="尚未定位" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="故障零件名称">
              <el-input v-model="currentCase.finalPartName" disabled placeholder="尚未定位" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="故障现象">
              <el-input
                v-model="currentCase.faultPhenomenon"
                type="textarea"
                :rows="2"
                disabled
                placeholder="-"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 第二部分：算法选择与建模操作 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>二、建模算法选择与运行</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form label-width="140px">
            <el-form-item label="建模算法">
              <el-select
                v-model="selectedAlgorithmId"
                placeholder="请选择全生命周期关联建模算法"
                style="width: 100%"
                @change="handleAlgorithmChange"
              >
                <el-option
                  v-for="item in algorithmOptions"
                  :key="item.algorithmId"
                  :label="item.algorithmName"
                  :value="item.algorithmId"
                >
                  <span>{{ item.algorithmName }}</span>
                  <span class="algorithm-version">{{ item.algorithmVersion }}</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="算法说明">
              <el-input
                v-model="selectedAlgorithmDesc"
                type="textarea"
                :rows="4"
                disabled
                placeholder="选择算法后显示算法说明"
              />
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :span="12">
          <div class="operate-panel">
            <el-button
              type="primary"
              :loading="caseLoading"
              @click="loadTraceCases"
            >
              刷新案例列表
            </el-button>

            <el-button
              type="success"
              :loading="buildLoading"
              :disabled="!currentCase.caseId || !currentCase.finalPartId"
              @click="handleBuildGraph"
            >
              构建全生命周期关联模型
            </el-button>

            <el-button
              :disabled="!currentGraph.graphId"
              @click="loadGraphData"
            >
              刷新图网络
            </el-button>
          </div>

          <el-divider />

          <el-descriptions
            title="当前图模型构建结果"
            :column="1"
            border
          >
            <el-descriptions-item label="图模型ID">
              <el-tag v-if="currentGraph.graphId" type="success">
                {{ currentGraph.graphId }}
              </el-tag>
              <span v-else>尚未构建</span>
            </el-descriptions-item>

            <el-descriptions-item label="图模型名称">
              {{ currentGraph.graphName || '尚未构建' }}
            </el-descriptions-item>

            <el-descriptions-item label="节点数量">
              {{ currentGraph.nodeCount ?? '-' }}
            </el-descriptions-item>

            <el-descriptions-item label="关系边数量">
              {{ currentGraph.edgeCount ?? '-' }}
            </el-descriptions-item>

            <el-descriptions-item label="构建状态">
              <el-tag :type="buildStatusType(currentGraph.buildStatus)">
                {{ buildStatusLabel(currentGraph.buildStatus) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </el-card>

    <!-- 第三部分：模拟数字卷宗数据预览 -->
    <el-card class="mb16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>三、模拟数字卷宗数据预览</span>
          <el-button
            type="primary"
            link
            :disabled="!currentCase.finalPartId"
            @click="loadDossierRecords"
          >
            刷新卷宗数据
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="dossierLoading"
        :data="dossierList"
        border
        height="320"
      >
        <el-table-column label="阶段" prop="lifecycleStage" width="130" align="center">
          <template #default="scope">
            <el-tag :type="stageTagType(scope.row.lifecycleStage)">
              {{ stageLabel(scope.row.lifecycleStage) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="阶段顺序" prop="stageOrder" width="90" align="center" />
        <el-table-column label="记录类型" prop="recordType" width="130" align="center" />
        <el-table-column label="记录名称" prop="recordName" min-width="180" show-overflow-tooltip />
        <el-table-column label="关联对象" prop="objectName" min-width="160" show-overflow-tooltip />
        <el-table-column label="参数名称" prop="parameterName" width="130" align="center" />
        <el-table-column label="参数值" prop="parameterValue" width="130" align="center" />
        <el-table-column label="标准值" prop="standardValue" width="130" align="center" />

        <el-table-column label="结果状态" prop="resultStatus" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.resultStatus === '1'" type="danger">异常</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="风险等级" prop="riskLevel" width="110" align="center">
          <template #default="scope">
            <el-tag :type="riskTagType(scope.row.riskLevel)">
              {{ riskLabel(scope.row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="记录描述" prop="recordDesc" min-width="260" show-overflow-tooltip />
      </el-table>

      <el-empty
        v-if="!dossierLoading && dossierList.length === 0"
        description="暂无该故障零件对应的模拟数字卷宗数据"
      />
    </el-card>

    <!-- 第四部分：图网络展示 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>四、全生命周期图网络模型展示</span>
          <div>
            <el-tag type="info">节点：{{ graphNodes.length }}</el-tag>
            <el-tag type="info" class="ml8">关系：{{ graphLinks.length }}</el-tag>
          </div>
        </div>
      </template>

      <div v-if="graphNodes.length > 0" ref="graphChartRef" class="graph-chart"></div>

      <el-empty
        v-else
        description="暂无图网络数据，请先构建全生命周期关联模型"
      />
    </el-card>
  </div>
</template>

<script setup name="LifecycleModel">
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const selectedCaseId = ref(null)
const selectedAlgorithmId = ref(null)
const selectedAlgorithmDesc = ref('')

const traceCaseOptions = ref([])
const algorithmOptions = ref([])
const dossierList = ref([])

const caseLoading = ref(false)
const algorithmLoading = ref(false)
const dossierLoading = ref(false)
const buildLoading = ref(false)
const graphLoading = ref(false)

const graphChartRef = ref(null)
let graphChart = null

const graphNodes = ref([])
const graphLinks = ref([])
const graphCategories = ref([])

const currentCase = reactive({
  caseId: null,
  caseNo: '',
  caseName: '',
  faultPhenomenon: '',
  finalPartId: null,
  finalPartNo: '',
  finalPartName: '',
  caseStatus: '0',
  graphId: null,
  graphName: ''
})

const currentGraph = reactive({
  graphId: null,
  graphNo: '',
  graphName: '',
  caseId: null,
  partId: null,
  partNo: '',
  partName: '',
  nodeCount: null,
  edgeCount: null,
  buildStatus: '',
  buildTime: '',
  graphSummary: ''
})

onMounted(() => {
  loadTraceCases()
  loadAlgorithmOptions()
  window.addEventListener('resize', resizeGraph)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeGraph)
  if (graphChart) {
    graphChart.dispose()
    graphChart = null
  }
})

/**
 * 加载已完成零件定位的追溯案例
 */
function loadTraceCases() {
  caseLoading.value = true

  request({
    url: '/topic5/traceCase/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 100
    }
  }).then(res => {
    const rows = res.rows || []

    // 优先展示已经有 finalPartId 的案例
    traceCaseOptions.value = rows.filter(item => item.finalPartId !== null && item.finalPartId !== undefined)

    if (traceCaseOptions.value.length === 0) {
      ElMessage.warning('当前暂无已完成故障零件定位的追溯案例，请先完成第一个模块。')
    }
  }).finally(() => {
    caseLoading.value = false
  })
}

/**
 * 加载全生命周期建模算法
 */
function loadAlgorithmOptions() {
  algorithmLoading.value = true

  request({
    url: '/topic5/algorithmConfig/list',
    method: 'get',
    params: {
      algorithmType: 'kg_build',
      status: '0',
      pageNum: 1,
      pageSize: 100
    }
  }).then(res => {
    algorithmOptions.value = res.rows || []

    const defaultAlgorithm = algorithmOptions.value.find(item => item.defaultFlag === '1')
    if (defaultAlgorithm) {
      selectedAlgorithmId.value = defaultAlgorithm.algorithmId
      selectedAlgorithmDesc.value = defaultAlgorithm.algorithmDesc || ''
    }
  }).finally(() => {
    algorithmLoading.value = false
  })
}

/**
 * 选择追溯案例
 */
function handleCaseChange(caseId) {
  clearGraph()

  const item = traceCaseOptions.value.find(c => c.caseId === caseId)

  if (!item) {
    resetCurrentCase()
    return
  }

  Object.assign(currentCase, item)

  loadDossierRecords()
  loadLatestGraphByCase(caseId)
}

/**
 * 选择建模算法
 */
function handleAlgorithmChange(algorithmId) {
  const item = algorithmOptions.value.find(a => a.algorithmId === algorithmId)
  selectedAlgorithmDesc.value = item ? (item.algorithmDesc || '') : ''
}

/**
 * 加载模拟数字卷宗数据
 */
function loadDossierRecords() {
  if (!currentCase.finalPartId) {
    return
  }

  dossierLoading.value = true

  request({
    url: '/topic5/dossierSampleRecord/list',
    method: 'get',
    params: {
      partId: currentCase.finalPartId,
      pageNum: 1,
      pageSize: 100
    }
  }).then(res => {
    const rows = res.rows || []
    dossierList.value = rows.sort((a, b) => {
      const x = a.stageOrder || 0
      const y = b.stageOrder || 0
      return x - y
    })

    if (dossierList.value.length === 0) {
      ElMessage.warning('当前零件暂无模拟数字卷宗数据，请先插入样例数据。')
    }
  }).finally(() => {
    dossierLoading.value = false
  })
}

/**
 * 查询当前案例最新图模型
 */
function loadLatestGraphByCase(caseId) {
  if (!caseId) {
    return
  }

  request({
    url: `/topic5/lifecycleGraph/latestByCase/${caseId}`,
    method: 'get'
  }).then(res => {
    if (res.data && res.data.graphId) {
      Object.assign(currentGraph, res.data)
      loadGraphData()
    } else {
      resetCurrentGraph()
    }
  }).catch(() => {
    resetCurrentGraph()
  })
}

/**
 * 构建全生命周期关联图模型
 */
function handleBuildGraph() {
  if (!currentCase.caseId) {
    ElMessage.warning('请先选择追溯案例')
    return
  }

  if (!currentCase.finalPartId) {
    ElMessage.warning('当前案例尚未完成故障零件定位，请先完成第一个模块')
    return
  }

  if (!selectedAlgorithmId.value) {
    ElMessage.warning('请选择全生命周期关联建模算法')
    return
  }

  buildLoading.value = true

  request({
    url: `/topic5/lifecycleGraph/build/${currentCase.caseId}`,
    method: 'post',
    params: {
      algorithmId: selectedAlgorithmId.value
    }
  }).then(res => {
    ElMessage.success('全生命周期关联模型构建完成')

    if (res.data && res.data.graphId) {
      Object.assign(currentGraph, res.data)
      currentCase.graphId = res.data.graphId
      currentCase.graphName = res.data.graphName
      currentCase.caseStatus = '2'
      loadGraphData()
    } else {
      loadLatestGraphByCase(currentCase.caseId)
    }
  }).finally(() => {
    buildLoading.value = false
  })
}

/**
 * 获取图网络数据
 */
function loadGraphData() {
  if (!currentGraph.graphId) {
    return
  }

  graphLoading.value = true

  request({
    url: `/topic5/lifecycleGraph/graphData/${currentGraph.graphId}`,
    method: 'get'
  }).then(res => {
    const data = res.data || {}

    const parsed = normalizeGraphData(data)

    graphNodes.value = parsed.nodes
    graphLinks.value = parsed.links
    graphCategories.value = parsed.categories

    nextTick(() => {
      renderGraph()
    })
  }).finally(() => {
    graphLoading.value = false
  })
}

/**
 * 兼容两种后端返回格式：
 * 1. 推荐格式：{ nodes: [], links: [], categories: [] }
 * 2. 数据库格式：{ nodeList: [], edgeList: [] }
 */
function normalizeGraphData(data) {
  if (Array.isArray(data.nodes) && Array.isArray(data.links)) {
    return {
      nodes: data.nodes,
      links: data.links,
      categories: data.categories || defaultCategories()
    }
  }

  const nodeList = data.nodeList || data.nodes || []
  const edgeList = data.edgeList || data.links || []

  const categories = defaultCategories()

  const nodes = nodeList.map(item => {
    const category = item.displayCategory || item.nodeType || item.lifecycleStage || 'record'

    return {
      id: String(item.nodeId),
      name: item.nodeName,
      category,
      value: item.riskScore || item.nodeWeight || 1,
      symbolSize: getNodeSize(item),
      itemStyle: item.abnormalFlag === '1' ? { color: '#F56C6C' } : undefined,
      raw: item
    }
  })

  const links = edgeList.map(item => {
    return {
      source: String(item.sourceNodeId),
      target: String(item.targetNodeId),
      name: item.relationName || item.relationType || '关联',
      value: item.edgeWeight || item.confidence || 1,
      raw: item
    }
  })

  return {
    nodes,
    links,
    categories
  }
}

/**
 * 渲染图网络
 */
function renderGraph() {
  if (!graphChartRef.value || graphNodes.value.length === 0) {
    return
  }

  if (!graphChart) {
    graphChart = echarts.init(graphChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        if (params.dataType === 'node') {
          const raw = params.data.raw || {}
          return [
            `<b>${params.data.name}</b>`,
            `节点类型：${raw.nodeType || params.data.category || '-'}`,
            `生命周期阶段：${stageLabel(raw.lifecycleStage) || '-'}`,
            `异常标记：${raw.abnormalFlag === '1' ? '异常' : '正常'}`,
            `风险分数：${raw.riskScore ?? '-'}`
          ].join('<br/>')
        }

        if (params.dataType === 'edge') {
          return [
            `<b>${params.data.name || '关联关系'}</b>`,
            `起点：${params.data.source}`,
            `终点：${params.data.target}`
          ].join('<br/>')
        }

        return params.name
      }
    },
    legend: [
      {
        data: graphCategories.value.map(c => c.name),
        top: 0
      }
    ],
    series: [
      {
        type: 'graph',
        layout: 'force',
        roam: true,
        draggable: true,
        focusNodeAdjacency: true,
        categories: graphCategories.value,
        data: graphNodes.value,
        links: graphLinks.value,
        edgeSymbol: ['none', 'arrow'],
        edgeSymbolSize: 8,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        edgeLabel: {
          show: true,
          formatter: function (params) {
            return params.data.name || ''
          },
          fontSize: 10
        },
        force: {
          repulsion: 420,
          edgeLength: [80, 180],
          gravity: 0.08
        },
        lineStyle: {
          width: 1.5,
          curveness: 0.15,
          opacity: 0.8
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 3
          }
        }
      }
    ]
  }

  graphChart.setOption(option, true)
  resizeGraph()
}

function resizeGraph() {
  if (graphChart) {
    graphChart.resize()
  }
}

function clearGraph() {
  graphNodes.value = []
  graphLinks.value = []
  graphCategories.value = []
  if (graphChart) {
    graphChart.clear()
  }
}

function resetCurrentCase() {
  Object.assign(currentCase, {
    caseId: null,
    caseNo: '',
    caseName: '',
    faultPhenomenon: '',
    finalPartId: null,
    finalPartNo: '',
    finalPartName: '',
    caseStatus: '0',
    graphId: null,
    graphName: ''
  })
}

function resetCurrentGraph() {
  Object.assign(currentGraph, {
    graphId: null,
    graphNo: '',
    graphName: '',
    caseId: null,
    partId: null,
    partNo: '',
    partName: '',
    nodeCount: null,
    edgeCount: null,
    buildStatus: '',
    buildTime: '',
    graphSummary: ''
  })
  clearGraph()
}

function formatCaseLabel(item) {
  const part = item.finalPartName ? `｜${item.finalPartName}` : ''
  return `${item.caseNo || item.caseId}｜${item.caseName || '未命名案例'}${part}`
}

function defaultCategories() {
  return [
    { name: 'part' },
    { name: 'stage' },
    { name: 'record' },
    { name: 'parameter' },
    { name: 'event' },
    { name: 'DESIGN' },
    { name: 'MATERIAL' },
    { name: 'MANUFACTURING' },
    { name: 'INSPECTION' },
    { name: 'ASSEMBLY' },
    { name: 'OPERATION' },
    { name: 'MAINTENANCE' }
  ]
}

function getNodeSize(item) {
  if (item.nodeType === 'part') return 70
  if (item.nodeType === 'stage') return 55
  if (item.abnormalFlag === '1') return 50
  if (item.nodeType === 'parameter') return 38
  return 42
}

function caseStatusLabel(status) {
  const map = {
    '0': '待定位',
    '1': '已完成故障零件定位',
    '2': '已构建全生命周期关联模型',
    '3': '已完成根因分析',
    '4': '失败'
  }
  return map[String(status)] || '未知'
}

function caseStatusType(status) {
  const map = {
    '0': 'info',
    '1': 'success',
    '2': 'warning',
    '3': 'danger',
    '4': 'danger'
  }
  return map[String(status)] || 'info'
}

function buildStatusLabel(status) {
  const map = {
    '0': '待构建',
    '1': '构建中',
    '2': '构建成功',
    '3': '构建失败'
  }
  return map[String(status)] || '尚未构建'
}

function buildStatusType(status) {
  const map = {
    '0': 'info',
    '1': 'warning',
    '2': 'success',
    '3': 'danger'
  }
  return map[String(status)] || 'info'
}

function stageLabel(stage) {
  const map = {
    DESIGN: '设计',
    MATERIAL: '材料',
    MANUFACTURING: '制造',
    INSPECTION: '检测',
    ASSEMBLY: '装配',
    OPERATION: '服役',
    MAINTENANCE: '维修'
  }
  return map[stage] || stage || '-'
}

function stageTagType(stage) {
  const map = {
    DESIGN: 'info',
    MATERIAL: 'success',
    MANUFACTURING: 'warning',
    INSPECTION: 'danger',
    ASSEMBLY: 'primary',
    OPERATION: 'warning',
    MAINTENANCE: 'danger'
  }
  return map[stage] || 'info'
}

function riskLabel(risk) {
  const map = {
    low: '低',
    medium: '中',
    high: '高'
  }
  return map[risk] || risk || '-'
}

function riskTagType(risk) {
  const map = {
    low: 'success',
    medium: 'warning',
    high: 'danger'
  }
  return map[risk] || 'info'
}
</script>

<style scoped>
.lifecycle-model-page {
  padding: 16px;
}

.mb16 {
  margin-bottom: 16px;
}

.ml8 {
  margin-left: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.operate-panel {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.algorithm-version {
  float: right;
  color: #8492a6;
  font-size: 13px;
  margin-left: 16px;
}

.graph-chart {
  width: 100%;
  height: 620px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
</style>
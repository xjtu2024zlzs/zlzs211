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
        <el-step title="质量问题填报" />
        <el-step title="多元特征提取" />
        <el-step title="故障根因分析" />
        <el-step title="卷宗实体映射" />
        <el-step title="溯源图谱构建" />
        <el-step title="全链路追溯闭环" />
        <el-step title="任务完成" />
      </el-steps>
    </el-card>

    <!-- 选择追溯任务 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>追溯任务选择</span>
      </template>

      <el-row :gutter="12" align="middle">
        <el-col :span="10">
          <el-select
            v-model="selectedTraceId"
            placeholder="请选择追溯任务"
            style="width: 100%"
            @change="handleTraceChange"
          >
            <el-option
              v-for="item in traceList"
              :key="item.id"
              :label="item.traceNo + ' - ' + item.partName"
              :value="item.id"
            />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-button type="primary" @click="getTraceList">
            刷新任务
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 当前追溯任务信息 -->
    <el-card class="box-card mt15">
      <template #header>
        <span>当前追溯任务信息</span>
      </template>

      <el-empty v-if="!currentTrace.id" description="请先选择追溯任务" />

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

      <el-button type="primary" icon="Connection" @click="handlePullTopic1Kg">
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
          <span>溯源图谱构建算法运行</span>
          <span class="header-tip">结果由 Java 后端调用 Python FastAPI 后写入数据库</span>
        </div>
      </template>

      <el-row :gutter="12" align="middle">
        <el-col :span="10">
          <el-select
            v-model="algorithmForm.algorithmName"
            placeholder="请选择溯源模型"
            style="width: 100%"
          >
            <el-option label="默认溯源模型（default）" value="default" />
            <el-option label="评估模式溯源模型（eval）" value="eval" />
          </el-select>
        </el-col>

        <el-col :span="5">
          <el-button
            type="warning"
            icon="Cpu"
            :loading="secondAlgorithmRunning"
            :disabled="secondAlgorithmRunning"
            @click="handleRunSecondAlgorithm"
          >
            运行算法
          </el-button>
        </el-col>
      </el-row>

      <el-form label-width="120px" class="mt15">
        <el-form-item label="算法状态">
          <el-tag :type="secondAlgorithmTagType(currentTrace.secondAlgorithmStatus)">
            {{ secondAlgorithmStatusName(currentTrace.secondAlgorithmStatus) }}
          </el-tag>
        </el-form-item>

        <el-form-item label="已选模型">
          <el-input
            v-model="currentTrace.secondAlgorithmName"
            readonly
            placeholder="尚未运行算法"
          />
        </el-form-item>

        <el-form-item label="人工确认状态">
          <el-tag :type="confirmStatusTagType(currentTrace.secondAlgorithmConfirmStatus)">
            {{ confirmStatusName(currentTrace.secondAlgorithmConfirmStatus) }}
          </el-tag>
        </el-form-item>

        <el-form-item label="人工确认备注">
          <el-input
            v-model="currentTrace.secondAlgorithmConfirmRemark"
            readonly
            placeholder="暂无人工确认备注"
          />
        </el-form-item>
      </el-form>

      <div class="graph-title mt15">第二部分算法运行结果</div>

      <el-empty
        v-if="summaryRows.length === 0 && componentDiagnosisTop3.length === 0 && subtypeTop5.length === 0"
        description="暂无第二部分算法结果，请先运行算法"
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

        <el-collapse class="mt15">
          <el-collapse-item title="查看原始算法结果 JSON" name="raw">
            <el-input
              type="textarea"
              :rows="10"
              :model-value="currentTrace.secondAlgorithmResultJson"
              readonly
            />
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>

    <!-- 人工判定区域 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>人工判定</span>
          <span class="header-tip">请确认是否保存算法输出结果</span>
        </div>
      </template>

      <el-alert
        title="如果选择保存，则保留当前算法输出结果；如果选择不保存，则删除当前算法输出结果，并提示需要重新运行算法。"
        type="warning"
        show-icon
        :closable="false"
      />

      <div class="mt15">
        <el-button type="primary" icon="Check" @click="handleOpenConfirm">
          人工判定
        </el-button>

        <el-tag
          style="margin-left: 12px;"
          :type="confirmStatusTagType(currentTrace.secondAlgorithmConfirmStatus)"
        >
          {{ confirmStatusName(currentTrace.secondAlgorithmConfirmStatus) }}
        </el-tag>
      </div>
    </el-card>

    <!-- 人工判定弹窗 -->
    <el-dialog
      title="人工判定第二部分算法结果"
      v-model="confirmOpen"
      width="560px"
      append-to-body
    >
      <el-alert
        title="请选择是否保存当前第二部分算法输出结果。选择“不保存”后，系统会删除当前算法输出结果，需要重新运行算法。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 15px;"
      />

      <el-form :model="confirmForm" label-width="120px">
        <el-form-item label="是否保存">
          <el-radio-group v-model="confirmForm.saveFlag">
            <el-radio :label="true">保存结果</el-radio>
            <el-radio :label="false">不保存，重新运行</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="判定备注">
          <el-input
            v-model="confirmForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入人工判定说明，可选"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="confirmOpen = false">取消</el-button>
          <el-button type="primary" @click="submitConfirmSecondAlgorithm">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import {
  listGraphTrace,
  getGraphTrace,
  pullTopic1Kg,
  runSecondAlgorithm,
  getTraceKg,
  confirmSecondAlgorithm
} from '@/api/topic5/graph'

const { proxy } = getCurrentInstance()
const route = useRoute()

const traceList = ref([])
const selectedTraceId = ref(null)
const currentTrace = ref({})

const originGraphRef = ref(null)
let originChart = null

const secondAlgorithmRunning = ref(false)
const confirmOpen = ref(false)

const algorithmForm = reactive({
  algorithmName: null
})

const confirmForm = reactive({
  saveFlag: true,
  remark: ''
})

const secondResultObj = computed(() => {
  return parseSecondResultObject(currentTrace.value.secondAlgorithmResultJson)
})

const summaryRows = computed(() => {
  return secondResultObj.value.summaryRows || []
})

const componentDiagnosisTop3 = computed(() => {
  return normalizeComponentDiagnosisRows(secondResultObj.value.componentDiagnosisTop3 || [])
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

function getTraceList() {
  listGraphTrace({}).then(res => {
    traceList.value = res.data || res.rows || []
  })
}

function handleTraceChange(id) {
  if (!id) return

  getGraphTrace(id).then(res => {
    currentTrace.value = res.data || {}

    if (Number(currentTrace.value.secondAlgorithmConfirmStatus) === 2) {
      algorithmForm.algorithmName = null
      currentTrace.value.secondAlgorithmResultJson = null
    } else {
      algorithmForm.algorithmName = currentTrace.value.secondAlgorithmName || null
    }

    loadTraceKg(id)
  })
}

function loadTraceKg(id) {
  getTraceKg(id).then(res => {
    const data = res.data || {}
    const traceProblem = data.traceProblem || currentTrace.value || {}

    currentTrace.value = {
      ...currentTrace.value,
      ...traceProblem,
      topic1KgJson: data.topic1KgJson || traceProblem.topic1KgJson,
      secondAlgorithmResultJson: data.secondAlgorithmResultJson || traceProblem.secondAlgorithmResultJson
    }

    if (data.topic1KgJson) {
      renderGraph(originGraphRef.value, parseGraphJson(data.topic1KgJson))
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
      renderGraph(originGraphRef.value, graphData)
    }

    refreshCurrentTrace()
  })
}

function handleRunSecondAlgorithm() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (!algorithmForm.algorithmName) {
    proxy.$modal.msgWarning('请选择溯源模型')
    return
  }

  secondAlgorithmRunning.value = true

  runSecondAlgorithm(selectedTraceId.value, {
    algorithmName: algorithmForm.algorithmName
  }).then(res => {
    proxy.$modal.msgSuccess('第二部分算法运行完成')

    const data = res.data || {}

    if (data.traceProblem) {
      currentTrace.value = data.traceProblem
    }

    if (data.secondAlgorithmResultJson) {
      currentTrace.value.secondAlgorithmResultJson = data.secondAlgorithmResultJson
    }

    refreshCurrentTrace()
  }).catch(err => {
    console.error('第二部分算法运行失败：', err)

    const msg =
      err?.response?.data?.msg ||
      err?.response?.data?.message ||
      err?.message ||
      '第二部分算法运行失败，请检查 Java 后端和 Python FastAPI 服务'

    proxy.$modal.msgError(msg)
  }).finally(() => {
    secondAlgorithmRunning.value = false
  })
}

function handleOpenConfirm() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (Number(currentTrace.value.secondAlgorithmStatus) !== 2) {
    proxy.$modal.msgWarning('第二部分算法尚未运行完成，不能进行人工判定')
    return
  }

  confirmForm.saveFlag = true
  confirmForm.remark = ''
  confirmOpen.value = true
}

function submitConfirmSecondAlgorithm() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  confirmSecondAlgorithm(selectedTraceId.value, {
    saveFlag: confirmForm.saveFlag,
    remark: confirmForm.remark
  }).then(() => {
    confirmOpen.value = false

    if (confirmForm.saveFlag) {
      proxy.$modal.msgSuccess('人工确认完成，算法结果已保存')
      refreshCurrentTrace()
      loadTraceKg(selectedTraceId.value)
    } else {
      proxy.$modal.msgWarning('已删除当前算法输出结果，请重新运行第二部分算法')

      algorithmForm.algorithmName = null
      currentTrace.value.secondAlgorithmName = null
      currentTrace.value.secondAlgorithmStatus = 0
      currentTrace.value.secondAlgorithmResultJson = null
      currentTrace.value.secondAlgorithmConfirmStatus = 2
      currentTrace.value.secondAlgorithmConfirmRemark = confirmForm.remark
      currentTrace.value.workflowStage = 4

      refreshCurrentTrace()
    }
  })
}

function refreshCurrentTrace() {
  if (!selectedTraceId.value) return

  getGraphTrace(selectedTraceId.value).then(res => {
    currentTrace.value = res.data || {}

    if (Number(currentTrace.value.secondAlgorithmConfirmStatus) === 2) {
      algorithmForm.algorithmName = null
      currentTrace.value.secondAlgorithmResultJson = null
    } else {
      algorithmForm.algorithmName = currentTrace.value.secondAlgorithmName || algorithmForm.algorithmName
    }

    getTraceList()
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

  if (typeof value === 'object') {
    return {
      summaryRows: value.summaryRows || value.rows || [],
      componentDiagnosisTop3: value.componentDiagnosisTop3 || [],
      subtypeTop5: value.subtypeTop5 || []
    }
  }

  try {
    const obj = JSON.parse(value)

    if (Array.isArray(obj)) {
      return {
        summaryRows: obj,
        componentDiagnosisTop3: [],
        subtypeTop5: []
      }
    }

    return {
      summaryRows: obj.summaryRows || obj.rows || [],
      componentDiagnosisTop3: obj.componentDiagnosisTop3 || [],
      subtypeTop5: obj.subtypeTop5 || []
    }
  } catch (e) {
    return {
      summaryRows: [],
      componentDiagnosisTop3: [],
      subtypeTop5: []
    }
  }
}

function normalizeComponentDiagnosisRows(list) {
  if (!Array.isArray(list)) {
    return []
  }

  return list.map((item, index) => {
    if (!item || typeof item !== 'object') {
      return {
        rank: index + 1,
        componentCode: '-',
        componentName: String(item || '-'),
        faultType: '-',
        confidence: null
      }
    }

    return {
      rank: item.rank || item.top || index + 1,
      componentCode: item.componentCode || item.component_code || item.component_id || item.code || '-',
      componentName: item.componentName || item.component_name || item.name || item.part_name || '-',
      faultType: item.faultType || item.fault_type || item.type || item.reason || '-',
      confidence: item.confidence !== undefined
        ? item.confidence
        : (item.score !== undefined
          ? item.score
          : (item.probability !== undefined
            ? item.probability
            : (item.severity !== undefined ? item.severity : null)))
    }
  })
}

function normalizeSubtypeRows(list) {
  if (!Array.isArray(list)) {
    return []
  }

  return list.map((item, index) => {
    if (!item || typeof item !== 'object') {
      return {
        rank: index + 1,
        subtypeName: String(item || '-'),
        confidence: null,
        relatedComponent: '-',
        description: '-'
      }
    }

    const subtypeName =
      item.subtype_name ||
      item.subtypeName ||
      item.name ||
      item.fault_type ||
      item.type ||
      '-'

    return {
      rank: item.rank || item.top || index + 1,
      subtypeName: subtypeName,
      confidence: item.confidence !== undefined
        ? item.confidence
        : (item.score !== undefined
          ? item.score
          : (item.probability !== undefined
            ? item.probability
            : (item.prob !== undefined ? item.prob : null))),
      relatedComponent: item.relatedComponent || item.related_component || item.component || item.component_name || '-',

      // 说明列只展示 Python 返回中的 subtype_name 字段
      description: item.subtype_name || '-'
    }
  })
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
    return { nodes: [], links: [] }
  }

  if (typeof json === 'object') {
    return json
  }

  try {
    return JSON.parse(json)
  } catch (e) {
    return { nodes: [], links: [] }
  }
}

function renderGraph(dom, graphData) {
  if (!dom) return

  nextTick(() => {
    if (!originChart) {
      originChart = echarts.init(dom)
    }

    const nodes = graphData.nodes || []
    const links = graphData.links || []

    const categories = []
    const categorySet = new Set()

    nodes.forEach(node => {
      if (node.category && !categorySet.has(node.category)) {
        categorySet.add(node.category)
        categories.push({ name: node.category })
      }
    })

    originChart.setOption({
      tooltip: {},
      legend: [
        {
          data: categories.map(item => item.name)
        }
      ],
      series: [
        {
          type: 'graph',
          layout: 'force',
          roam: true,
          draggable: true,
          symbolSize: 52,
          categories,
          label: {
            show: true,
            position: 'right'
          },
          edgeLabel: {
            show: true,
            formatter: function (params) {
              return params.data.name || ''
            }
          },
          force: {
            repulsion: 500,
            edgeLength: 130
          },
          data: nodes.map(node => ({
            ...node,
            category: categoryIndex(categories, node.category)
          })),
          links
        }
      ]
    }, true)

    originChart.resize()
  })
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

  return num.toFixed(2)
}

function workflowName(stage) {
  const value = Number(stage)

  if (value === 1) return '质量问题填报'
  if (value === 2) return '多元特征提取'
  if (value === 3) return '故障根因分析'
  if (value === 4) return '卷宗实体映射'
  if (value === 5) return '溯源图谱构建'
  if (value === 6) return '全链路追溯闭环'

  return '未开始'
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

function confirmStatusName(status) {
  const value = Number(status)
  if (value === 0) return '未人工确认'
  if (value === 1) return '已确认保存'
  if (value === 2) return '已驳回，需重跑'
  return '未人工确认'
}

function confirmStatusTagType(status) {
  const value = Number(status)
  if (value === 0) return 'info'
  if (value === 1) return 'success'
  if (value === 2) return 'danger'
  return 'info'
}

onMounted(() => {
  getTraceList()

  const traceId = route.query.traceId
  if (traceId) {
    selectedTraceId.value = Number(traceId)
    handleTraceChange(Number(traceId))
  }

  window.addEventListener('resize', () => {
    if (originChart) originChart.resize()
  })
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
  height: 460px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
  margin-top: 10px;
}

.dialog-footer {
  text-align: right;
}
</style>
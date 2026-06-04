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

    <!-- 课题一知识图谱导入 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>课题一知识图谱导入</span>
          <span class="header-tip">拉取完成后，“知识图谱导入”流程变绿</span>
        </div>
      </template>

      <el-button type="primary" icon="Connection" @click="handlePullTopic1Kg">
        从课题一拉取知识图谱
      </el-button>

      <el-alert
        class="mt15"
        title="系统将根据当前追溯任务的发生时间、架次和发生部位，从课题一数字卷宗中获取相关知识图谱。"
        type="info"
        show-icon
        :closable="false"
      />

      <div class="graph-title mt15">课题一原始知识图谱</div>
      <div ref="originGraphRef" class="graph-container"></div>
    </el-card>

    <!-- 第二部分算法运行 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>第二部分算法运行</span>
          <span class="header-tip">运行完成后，“第二部分算法运行”流程变绿</span>
        </div>
      </template>

      <el-row :gutter="12" align="middle">
        <el-col :span="10">
          <el-select
            v-model="algorithmForm.algorithmName"
            placeholder="请选择第二部分算法"
            style="width: 100%"
          >
            <el-option label="知识图谱结构增强算法" value="KG_STRUCTURE_ENHANCE" />
            <el-option label="TransH 关系推理算法" value="TRANSH_REASONING" />
            <el-option label="故障链路补全算法" value="FAULT_CHAIN_COMPLETION" />
          </el-select>
        </el-col>

        <el-col :span="5">
          <el-button type="warning" icon="Cpu" @click="handleRunSecondAlgorithm">
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

        <el-form-item label="已选算法">
          <el-input
            v-model="currentTrace.secondAlgorithmName"
            readonly
            placeholder="尚未运行第二部分算法"
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

      <div class="graph-title mt15">第二部分算法输出知识图谱</div>
      <div ref="resultGraphRef" class="graph-container"></div>
    </el-card>

    <!-- 人工判定区域 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>人工判定</span>
          <span class="header-tip">请确认是否保存第二部分算法输出结果</span>
        </div>
      </template>

      <el-alert
        title="如果选择保存，则保留当前算法输出结果；如果选择不保存，则删除当前算法输出结果，并提示需要重新运行第二部分算法。"
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
const resultGraphRef = ref(null)

let originChart = null
let resultChart = null

const confirmOpen = ref(false)

const algorithmForm = reactive({
  algorithmName: null
})

const confirmForm = reactive({
  saveFlag: true,
  remark: ''
})

const activeStep = computed(() => {
  const stage = Number(currentTrace.value.workflowStage || 0)

  // 顶部流程显示为：
  // 1 任务开始
  // 2 质量问题填报
  // 3 多元特征提取
  // 4 故障根因分析
  // 5 卷宗实体映射
  // 6 溯源图谱构建
  // 7 全链路追溯闭环
  // 8 任务完成
  //
  // 数据库 workflowStage 仍为：
  // 1 质量问题填报
  // 2 多元特征提取
  // 3 故障根因分析
  // 4 卷宗实体映射
  // 5 溯源图谱构建
  // 6 全链路追溯闭环

  if (stage <= 0) {
    return 0
  }

  // 质量问题填报完成时，“任务开始”和“质量问题填报”同步完成
  if (stage === 1) {
    return 2
  }

  // 多元特征提取完成
  if (stage === 2) {
    return 3
  }

  // 故障根因分析完成
  if (stage === 3) {
    return 4
  }

  // 卷宗实体映射完成
  if (stage === 4) {
    return 5
  }

  // 溯源图谱构建完成
  if (stage === 5) {
    return 6
  }

  // 全链路追溯闭环完成时，“任务完成”同步完成
  if (stage >= 6) {
    return 8
  }

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
      clearGraph(resultGraphRef.value, 'result')
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

    if (data.topic1KgJson) {
      renderGraph(originGraphRef.value, 'origin', parseGraphJson(data.topic1KgJson))
    } else {
      clearGraph(originGraphRef.value, 'origin')
    }

    const secondStatus = Number(traceProblem.secondAlgorithmStatus)
    const confirmStatus = Number(traceProblem.secondAlgorithmConfirmStatus)

    if (
      data.secondAlgorithmResultJson &&
      secondStatus === 2 &&
      confirmStatus !== 2
    ) {
      renderGraph(resultGraphRef.value, 'result', parseGraphJson(data.secondAlgorithmResultJson))
    } else {
      clearGraph(resultGraphRef.value, 'result')
    }
  })
}

function handlePullTopic1Kg() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  pullTopic1Kg(selectedTraceId.value).then(res => {
    proxy.$modal.msgSuccess('课题一知识图谱拉取成功')
    currentTrace.value = res.data.traceProblem || currentTrace.value

    const graphData = res.data.graphData || null
    if (graphData) {
      renderGraph(originGraphRef.value, 'origin', graphData)
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
    proxy.$modal.msgWarning('请先选择第二部分算法')
    return
  }

  runSecondAlgorithm(selectedTraceId.value, {
    algorithmName: algorithmForm.algorithmName
  }).then(res => {
    proxy.$modal.msgSuccess('第二部分算法运行完成')
    currentTrace.value = res.data.traceProblem || currentTrace.value

    const graphData = res.data.graphData || null
    if (graphData) {
      renderGraph(resultGraphRef.value, 'result', graphData)
    }

    refreshCurrentTrace()
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

      clearGraph(resultGraphRef.value, 'result')

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
      clearGraph(resultGraphRef.value, 'result')
    } else {
      algorithmForm.algorithmName = currentTrace.value.secondAlgorithmName || algorithmForm.algorithmName
    }

    getTraceList()
  })
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

function renderGraph(dom, type, graphData) {
  if (!dom) return

  nextTick(() => {
    let chart = null

    if (type === 'origin') {
      if (!originChart) originChart = echarts.init(dom)
      chart = originChart
    } else {
      if (!resultChart) resultChart = echarts.init(dom)
      chart = resultChart
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

    chart.setOption({
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

    chart.resize()
  })
}

function clearGraph(dom, type) {
  if (!dom) return

  nextTick(() => {
    if (type === 'origin') {
      if (!originChart) originChart = echarts.init(dom)
      originChart.clear()
    } else {
      if (!resultChart) resultChart = echarts.init(dom)
      resultChart.clear()
    }
  })
}

function categoryIndex(categories, name) {
  const index = categories.findIndex(item => item.name === name)
  return index >= 0 ? index : 0
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
    if (resultChart) resultChart.resize()
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
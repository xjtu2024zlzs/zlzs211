<template>
  <div class="app-container topic5-source-page">

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

    <!-- 追溯任务选择 -->
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

        <el-descriptions-item label="第二部分算法状态">
          <el-tag :type="secondAlgorithmTagType(currentTrace.secondAlgorithmStatus)">
            {{ secondAlgorithmStatusName(currentTrace.secondAlgorithmStatus) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="人工确认状态">
          <el-tag :type="confirmStatusTagType(currentTrace.secondAlgorithmConfirmStatus)">
            {{ confirmStatusName(currentTrace.secondAlgorithmConfirmStatus) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="报告状态">
          <el-tag :type="reportStatusTagType(currentTrace.traceReportStatus)">
            {{ reportStatusName(currentTrace.traceReportStatus) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="问题描述" :span="3">
          {{ currentTrace.problemDescription }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 最终溯源算法运行 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>全链路追溯闭环算法运行</span>
          <span class="header-tip">运行后输出溯源知识图谱和原因表单</span>
        </div>
      </template>

      <el-alert
        title="该算法将调用后端，由后端调用 Python FastAPI，并读取 API 返回的故障增强知识图谱。"
        type="info"
        show-icon
        :closable="false"
      />

      <el-row :gutter="12" align="middle" class="mt15">
        <el-col :span="10">
          <el-select
            v-model="algorithmForm.algorithmName"
            placeholder="请选择全链路追溯闭环算法"
            style="width: 100%"
          >
            <el-option label="最终溯源综合推理算法" value="SOURCE_REASONING" />
            <el-option label="知识图谱路径溯源算法" value="KG_PATH_SOURCE" />
            <el-option label="多证据融合溯源算法" value="MULTI_EVIDENCE_FUSION" />
            <el-option label="TransH 溯源排序算法" value="TRANSH_SOURCE_RANKING" />
          </el-select>
        </el-col>

        <el-col :span="5">
          <el-button
            type="warning"
            icon="Cpu"
            :loading="sourceRunning"
            :disabled="sourceRunning"
            @click="handleRunSourceAlgorithm"
          >
            运行溯源算法
          </el-button>
        </el-col>
      </el-row>

      <el-form label-width="130px" class="mt15">
        <el-form-item label="算法状态">
          <el-tag :type="sourceAlgorithmTagType(currentTrace.sourceAlgorithmStatus)">
            {{ sourceAlgorithmStatusName(currentTrace.sourceAlgorithmStatus) }}
          </el-tag>
        </el-form-item>

        <el-form-item label="已选算法">
          <el-input
            v-model="currentTrace.sourceAlgorithmName"
            readonly
            placeholder="尚未运行全链路追溯闭环算法"
          />
        </el-form-item>

        <el-form-item label="溯源结论摘要">
          <el-input
            v-model="sourceSummary"
            type="textarea"
            :rows="4"
            readonly
            placeholder="全链路追溯闭环算法运行后将在此显示结论摘要"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 溯源知识图谱 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>全链路追溯闭环知识图谱</span>
          <span class="header-tip">展示 Python API 返回的故障增强知识图谱</span>
        </div>
      </template>

      <el-empty
        v-if="!hasSourceGraph"
        description="暂无溯源知识图谱，请先运行最终溯源算法"
      />

      <div
        v-show="hasSourceGraph"
        ref="sourceGraphRef"
        class="graph-container"
      ></div>
    </el-card>

    <!-- 溯源原因表单 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>溯源原因表单</span>
          <span class="header-tip">展示最终溯源候选原因及证据</span>
        </div>
      </template>

      <el-empty
        v-if="reasonList.length === 0"
        description="暂无溯源原因表单，请先运行最终溯源算法"
      />

      <el-table
        v-else
        :data="reasonList"
        border
        style="width: 100%"
      >
        <el-table-column prop="rank" label="排名" width="80" align="center" />
        <el-table-column prop="reasonType" label="原因阶段/类型" min-width="130" />
        <el-table-column prop="relatedPart" label="原因名称" min-width="180" />
        <el-table-column prop="reasonName" label="关联对象" min-width="160" />
        <el-table-column prop="evidence" label="关键证据" min-width="260" show-overflow-tooltip />
        <el-table-column prop="confidence" label="置信度" min-width="100" align="center">
          <template #default="scope">
            {{ formatConfidence(scope.row.confidence) }}
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议措施" min-width="260" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 报告导出与推送 -->
    <el-card class="box-card mt15">
      <template #header>
        <div class="card-header">
          <span>报告导出与结果推送</span>
          <span class="header-tip">导出报告后，可推送至课题二或课题三</span>
        </div>
      </template>

      <el-alert
        title="导出报告成功后，总流程进入“全链路追溯闭环”阶段。推送课题二和课题三当前可先使用模拟接口，后续再替换为真实课题接口。"
        type="success"
        show-icon
        :closable="false"
      />

      <el-row :gutter="12" align="middle" class="mt15">
        <el-col :span="4">
          <el-button
            type="primary"
            icon="Document"
            :loading="exporting"
            @click="handleExportReport"
          >
            导出报告
          </el-button>
        </el-col>

        <el-col :span="4">
          <el-button
            type="success"
            icon="Upload"
            @click="handlePushTopic2"
          >
            推送课题二
          </el-button>
        </el-col>

        <el-col :span="4">
          <el-button
            type="success"
            icon="Upload"
            @click="handlePushTopic3"
          >
            推送课题三
          </el-button>
        </el-col>

        <el-col :span="6">
          <el-button
            v-if="currentTrace.traceReportUrl"
            link
            type="primary"
            @click="openReport"
          >
            查看已导出报告
          </el-button>
        </el-col>
      </el-row>

      <el-descriptions :column="3" border class="mt15">
        <el-descriptions-item label="报告状态">
          <el-tag :type="reportStatusTagType(currentTrace.traceReportStatus)">
            {{ reportStatusName(currentTrace.traceReportStatus) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="报告路径" :span="2">
          {{ currentTrace.traceReportUrl || '未生成' }}
        </el-descriptions-item>

        <el-descriptions-item label="课题二推送状态">
          <el-tag :type="pushStatusTagType(currentTrace.topic2PushStatus)">
            {{ pushStatusName(currentTrace.topic2PushStatus) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="课题二推送时间" :span="2">
          {{ currentTrace.topic2PushTime || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="课题三推送状态">
          <el-tag :type="pushStatusTagType(currentTrace.topic3PushStatus)">
            {{ pushStatusName(currentTrace.topic3PushStatus) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="课题三推送时间" :span="2">
          {{ currentTrace.topic3PushTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import {
  listSourceTrace,
  getSourceTrace,
  getSourceResult,
  runSourceAlgorithm,
  exportSourceReport,
  downloadSourceReport,
  pushTopic2,
  pushTopic3
} from '@/api/topic5/source'

const { proxy } = getCurrentInstance()
const route = useRoute()

const traceList = ref([])
const selectedTraceId = ref(null)
const currentTrace = ref({})

const sourceGraphRef = ref(null)
let sourceChart = null

const reasonList = ref([])
const sourceSummary = ref('')

const sourceRunning = ref(false)
const exporting = ref(false)

const algorithmForm = reactive({
  algorithmName: null
})

const hasSourceGraph = computed(() => {
  return !!currentTrace.value.sourceGraphJson
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
  listSourceTrace({}).then(res => {
    traceList.value = res.data || res.rows || []
  })
}

function handleTraceChange(id) {
  if (!id) return

  getSourceTrace(id).then(res => {
    currentTrace.value = res.data || {}
    algorithmForm.algorithmName = currentTrace.value.sourceAlgorithmName || null
    loadSourceResult(id)
  })
}

function loadSourceResult(id) {
  getSourceResult(id).then(res => {
    const data = res.data || {}
    const traceProblem = data.traceProblem || currentTrace.value || {}

    currentTrace.value = {
      ...currentTrace.value,
      ...traceProblem,
      sourceGraphJson: data.sourceGraphJson || traceProblem.sourceGraphJson,
      sourceReasonTableJson: data.reasonTableJson || traceProblem.sourceReasonTableJson,
      sourceResultSummary: data.summary || traceProblem.sourceResultSummary
    }

    sourceSummary.value = data.summary || currentTrace.value.sourceResultSummary || ''

    if (data.reasonList) {
      reasonList.value = data.reasonList
    } else {
      reasonList.value = parseReasonJson(data.reasonTableJson || currentTrace.value.sourceReasonTableJson)
    }

    const graphJson = data.sourceGraphJson || currentTrace.value.sourceGraphJson

    if (graphJson) {
      currentTrace.value.sourceGraphJson = graphJson
      nextTick(() => {
        renderSourceGraph(graphJson)
      })
    } else if (data.graphData) {
      nextTick(() => {
        renderSourceGraph(data.graphData)
      })
    } else {
      clearSourceGraph()
    }
  })
}

function handleRunSourceAlgorithm() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (!algorithmForm.algorithmName) {
    proxy.$modal.msgWarning('请先选择最终溯源算法')
    return
  }

  sourceRunning.value = true

  runSourceAlgorithm(selectedTraceId.value, {
    algorithmName: algorithmForm.algorithmName
  }).then(res => {
    proxy.$modal.msgSuccess('最终溯源算法运行完成')

    const data = res.data || {}
    const traceProblem = data.traceProblem || currentTrace.value || {}

    currentTrace.value = {
      ...currentTrace.value,
      ...traceProblem,
      sourceGraphJson: data.sourceGraphJson || traceProblem.sourceGraphJson,
      sourceReasonTableJson: data.reasonTableJson || traceProblem.sourceReasonTableJson,
      sourceResultSummary: data.summary || traceProblem.sourceResultSummary
    }

    sourceSummary.value = data.summary || currentTrace.value.sourceResultSummary || ''
    reasonList.value = data.reasonList || parseReasonJson(data.reasonTableJson || currentTrace.value.sourceReasonTableJson)

    const graphJson = data.sourceGraphJson || currentTrace.value.sourceGraphJson

    if (graphJson) {
      currentTrace.value.sourceGraphJson = graphJson
      nextTick(() => {
        renderSourceGraph(graphJson)
      })
    } else if (data.graphData) {
      nextTick(() => {
        renderSourceGraph(data.graphData)
      })
    } else {
      clearSourceGraph()
    }

    refreshCurrentTrace(false)
  }).catch(err => {
    console.error('最终溯源算法运行失败：', err)

    const msg =
      err?.response?.data?.msg ||
      err?.response?.data?.message ||
      err?.message ||
      '最终溯源算法运行失败，请检查 Java 后端和 Python FastAPI 服务'

    proxy.$modal.msgError(msg)
  }).finally(() => {
    sourceRunning.value = false
  })
}

function handleExportReport() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (Number(currentTrace.value.sourceAlgorithmStatus) !== 2) {
    proxy.$modal.msgWarning('请先运行最终溯源算法，再导出报告')
    return
  }

  exporting.value = true

  exportSourceReport(selectedTraceId.value).then(res => {
    proxy.$modal.msgSuccess('最终溯源报告导出成功')

    const data = res.data || {}
    if (data.reportUrl) {
      currentTrace.value.traceReportUrl = data.reportUrl
      currentTrace.value.traceReportStatus = 1
      currentTrace.value.workflowStage = 6
      currentTrace.value.status = '溯源完成'
    }

    refreshCurrentTrace(false)
  }).catch(err => {
    console.error('导出报告失败：', err)
    proxy.$modal.msgError('导出报告失败')
  }).finally(() => {
    exporting.value = false
  })
}

function handlePushTopic2() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (Number(currentTrace.value.traceReportStatus) !== 1) {
    proxy.$modal.msgWarning('请先导出最终溯源报告，再推送课题二')
    return
  }

  pushTopic2(selectedTraceId.value).then(() => {
    proxy.$modal.msgSuccess('已推送课题二')
    refreshCurrentTrace(false)
  })
}

function handlePushTopic3() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (Number(currentTrace.value.traceReportStatus) !== 1) {
    proxy.$modal.msgWarning('请先导出最终溯源报告，再推送课题三')
    return
  }

  pushTopic3(selectedTraceId.value).then(() => {
    proxy.$modal.msgSuccess('已推送课题三')
    refreshCurrentTrace(false)
  })
}

function refreshCurrentTrace(needReloadSourceResult = true) {
  if (!selectedTraceId.value) return

  getSourceTrace(selectedTraceId.value).then(res => {
    currentTrace.value = {
      ...currentTrace.value,
      ...(res.data || {})
    }

    algorithmForm.algorithmName = currentTrace.value.sourceAlgorithmName || algorithmForm.algorithmName
    getTraceList()

    if (needReloadSourceResult) {
      loadSourceResult(selectedTraceId.value)
    }
  })
}

function parseReasonJson(json) {
  if (!json) {
    return []
  }

  if (Array.isArray(json)) {
    return json
  }

  try {
    const obj = JSON.parse(json)
    return Array.isArray(obj) ? obj : []
  } catch (e) {
    return []
  }
}

function renderSourceGraph(graphInput) {
  if (!graphInput) {
    clearSourceGraph()
    return
  }

  nextTick(() => {
    if (!sourceGraphRef.value) {
      return
    }

    const option = buildEchartsOption(graphInput)

    if (!sourceChart) {
      sourceChart = echarts.init(sourceGraphRef.value)
    }

    if (!option || !option.series || option.series.length === 0) {
      sourceChart.clear()
      return
    }

    sourceChart.setOption(option, true)
    sourceChart.resize()
  })
}

function buildEchartsOption(graphInput) {
  let graphData = graphInput

  if (typeof graphInput === 'string') {
    try {
      graphData = JSON.parse(graphInput)
    } catch (e) {
      console.error('图谱 JSON 解析失败：', e)
      return { series: [] }
    }
  }

  if (!graphData) {
    return { series: [] }
  }

  // 情况1：后端保存的是 Python 返回的完整 ECharts option，直接使用
  if (graphData.series && Array.isArray(graphData.series)) {
    return graphData
  }

  // 情况2：后端返回的是 nodes / links 或 nodes / edges，前端兜底转换
  if (graphData.nodes || graphData.links || graphData.edges) {
    return convertNodesLinksToOption(graphData)
  }

  return { series: [] }
}

function convertNodesLinksToOption(graphData) {
  const nodes = graphData.nodes || []
  const links = graphData.links || graphData.edges || []

  if (nodes.length === 0) {
    return { series: [] }
  }

  const categories = []
  const categorySet = new Set()

  nodes.forEach(node => {
    const categoryName = node.category || node.stage_name || node.type || '未知类型'
    if (!categorySet.has(categoryName)) {
      categorySet.add(categoryName)
      categories.push({ name: categoryName })
    }
  })

  return {
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: function (params) {
        if (params.dataType === 'node') {
          return `
            <div>
              <b>${params.data.name || params.data.id || '-'}</b><br/>
              类型：${params.data.type || params.data.category || params.data.stage_name || '-'}<br/>
              分数：${params.data.score || params.data.value || '-'}
            </div>
          `
        }

        return params.data.name || params.data.relation_name || params.data.relation || ''
      }
    },
    legend: [
      {
        top: 0,
        data: categories.map(item => item.name)
      }
    ],
    series: [
      {
        name: '全链路追溯闭环知识图谱',
        type: 'graph',
        layout: 'force',
        roam: true,
        draggable: true,
        focusNodeAdjacency: true,
        categories,
        label: {
          show: true,
          position: 'right'
        },
        edgeLabel: {
          show: true,
          formatter: function (params) {
            return params.data.name || params.data.relation_name || params.data.relation || ''
          }
        },
        force: {
          repulsion: 650,
          edgeLength: 150
        },
        data: nodes.map(node => {
          const categoryName = node.category || node.stage_name || node.type || '未知类型'

          return {
            ...node,
            id: node.id,
            name: node.name || node.id,
            category: categoryIndex(categories, categoryName),
            value: node.score || node.value || 1,
            symbolSize: node.is_feedback
              ? 68
              : (node.is_target_object
                ? 62
                : (node.is_top_candidate ? 56 : 42))
          }
        }),
        links: links.map(edge => ({
          ...edge,
          source: edge.source,
          target: edge.target,
          name: edge.name || edge.relation_name || edge.relation || '',
          value: edge.weight || edge.score || 1
        }))
      }
    ]
  }
}

function clearSourceGraph() {
  nextTick(() => {
    if (!sourceGraphRef.value) {
      return
    }

    if (!sourceChart) {
      sourceChart = echarts.init(sourceGraphRef.value)
    }

    sourceChart.clear()
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

  const numberValue = Number(value)

  if (Number.isNaN(numberValue)) {
    return value
  }

  if (numberValue <= 1) {
    return (numberValue * 100).toFixed(1) + '%'
  }

  return numberValue.toFixed(2)
}

function openReport() {
  if (!selectedTraceId.value) {
    proxy.$modal.msgWarning('请先选择追溯任务')
    return
  }

  if (!currentTrace.value.traceReportUrl) {
    proxy.$modal.msgWarning('当前追溯任务尚未生成报告')
    return
  }

  downloadSourceReport(selectedTraceId.value).then(res => {
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    })

    const fileName = getReportFileName(currentTrace.value.traceReportUrl)

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')

    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }).catch(err => {
    console.error('下载报告失败：', err)
    proxy.$modal.msgError('下载报告失败，请检查报告文件是否存在')
  })
}

function getReportFileName(reportUrl) {
  if (!reportUrl) {
    return '最终溯源报告.docx'
  }

  const index = reportUrl.lastIndexOf('/')
  if (index >= 0) {
    return reportUrl.substring(index + 1)
  }

  return reportUrl
}

function buildFileUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url

  const baseApi = import.meta.env.VITE_APP_BASE_API || ''
  return baseApi + url
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

function sourceAlgorithmStatusName(status) {
  const value = Number(status)
  if (value === 0) return '未运行'
  if (value === 1) return '运行中'
  if (value === 2) return '已完成'
  if (value === 3) return '运行失败'
  return '未运行'
}

function sourceAlgorithmTagType(status) {
  const value = Number(status)
  if (value === 0) return 'info'
  if (value === 1) return 'warning'
  if (value === 2) return 'success'
  if (value === 3) return 'danger'
  return 'info'
}

function reportStatusName(status) {
  const value = Number(status)
  if (value === 0) return '未生成'
  if (value === 1) return '已生成'
  return '未生成'
}

function reportStatusTagType(status) {
  const value = Number(status)
  if (value === 0) return 'info'
  if (value === 1) return 'success'
  return 'info'
}

function pushStatusName(status) {
  const value = Number(status)
  if (value === 0) return '未推送'
  if (value === 1) return '已推送'
  if (value === 2) return '推送失败'
  return '未推送'
}

function pushStatusTagType(status) {
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
    if (sourceChart) {
      sourceChart.resize()
    }
  })
})
</script>

<style scoped>
.topic5-source-page {
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

.graph-container {
  width: 100%;
  height: 560px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}
</style>
<template>
  <div class="home-view">
    <div class="home-shell">
      <!-- 顶部总入口 -->
      <section class="home-hero">
        <div class="hero-left">
          <h1 class="home-title">航空装备生命周期质量自反馈追溯系统</h1>
          <p class="home-subtitle">
            面向航空装备质量问题填报、异构数据关联、智能算法分析、知识图谱追溯与质量报告生成的综合软件平台。
          </p>

          <div class="hero-tags">
            <div class="meta-chip">
              <span class="meta-dot meta-dot--green"></span>
              <span>系统在线</span>
            </div>
            <div class="meta-chip">
              <span class="meta-dot meta-dot--blue"></span>
              <span>数据接入正常</span>
            </div>
            <div class="meta-chip">
              <span class="meta-dot meta-dot--cyan"></span>
              <span>知识图谱已加载</span>
            </div>
          </div>
        </div>

        <div class="hero-action">
          <div class="start-card">
            <h3 class="start-card__title">新建质量问题追溯任务</h3>
            <p class="start-card__desc">
              开始质量问题填报，后续进行算法分析、问题溯源。
            </p>
            <el-button type="primary" size="large" class="start-button" @click="startSystem">
              开始质量问题填报
            </el-button>
          </div>
        </div>
      </section>

      <!-- 课题一统计指标 -->
      <section class="section-block project-one-section">
        <div class="section-header">
          <div>
            <p class="section-label">全域异构信息集成系统</p>
            <h2 class="section-title">数字卷宗</h2>
            <p class="section-desc">
              汇总相关的核心运行指标
            </p>
          </div>

          <el-button type="primary" size="large" plain @click="navigateTo('/project_1')">
            进入模块
          </el-button>
        </div>

        <div class="metric-grid" v-loading="loading">
          <div v-for="metric in projectOneMetrics" :key="metric.key" class="metric-card">
            <div class="metric-card__header">
              <span class="metric-card__label">{{ metric.label }}</span>
              <span class="metric-card__icon">{{ metric.icon }}</span>
            </div>
            <div class="metric-card__value">{{ metric.value }}</div>
            <div class="metric-card__footer">
              <span class="metric-card__trend" :class="metric.trendClass">
                {{ metric.change }}
              </span>
              <span class="metric-card__unit">{{ metric.unit }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 课题二三四 -->
      <section class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">Project Modules</p>
            <h2 class="section-title">课题二、课题三、课题四模块概览</h2>
            <p class="section-desc">
              以模块卡片形式展示各课题的核心信息、运行状态和图表摘要，点击按钮可进入对应软件模块。
            </p>
          </div>
        </div>

        <div class="project-card-grid">
          <article
            v-for="project in middleProjects"
            :key="project.key"
            class="project-card"
          >
            <div class="project-card__top">
              <div class="project-card__icon" :class="project.iconClass">
                {{ project.icon }}
              </div>
              <div>
                <p class="project-card__label">{{ project.label }}</p>
                <h3 class="project-card__title">{{ project.title }}</h3>
              </div>
            </div>

            <p class="project-card__desc">
              {{ project.description }}
            </p>

            <div class="mini-chart">
              <div class="mini-chart__header">
                <span>{{ project.chartTitle }}</span>
                <strong>{{ project.chartValue }}</strong>
              </div>

              <div class="bar-chart">
                <div
                  v-for="(bar, index) in project.chartData"
                  :key="index"
                  class="bar-chart__item"
                >
                  <div class="bar-chart__bar">
                    <span :style="{ height: bar + '%' }"></span>
                  </div>
                  <p>{{ project.chartLabels[index] }}</p>
                </div>
              </div>
            </div>

            <div class="project-card__meta">
              <div v-for="item in project.meta" :key="item.name" class="meta-item">
                <span>{{ item.name }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>

            <el-button type="primary" class="module-button" @click="navigateTo(project.route)">
              进入{{ project.label }}模块
            </el-button>
          </article>
        </div>
      </section>

      <!-- 课题五知识图谱 -->
      <section class="section-block knowledge-section">
        <div class="knowledge-left">
          <div class="section-header section-header--plain">
            <div>
              <p class="section-label">生命周期质量知识图谱与智能追溯系统</p>
              <h2 class="section-title">知识图谱追溯</h2>
              <p class="section-desc">
                围绕质量反馈事件，融合零部件、传感器、制造、装配、检测、运行和质量反馈等生命周期数据，
                构建质量知识图谱，实现候选根因推理、追溯链展示和报告自动生成。
              </p>
            </div>
          </div>

          <div class="kg-stat-grid">
            <div v-for="stat in knowledgeStats" :key="stat.label" class="kg-stat">
              <p>{{ stat.label }}</p>
              <strong>{{ stat.value }}</strong>
            </div>
          </div>

          <div class="knowledge-status">
            <span>知识图谱已加载</span>
            <span>算法模型在线</span>
            <span>追溯链可生成</span>
            <span>报告模板可用</span>
          </div>

          <div v-if="selectedKgNode" class="kg-node-detail">
            <p class="kg-node-detail__label">当前选中节点</p>
            <h4>{{ selectedKgNode.name }}</h4>
            <p>{{ selectedKgNode.desc }}</p>
          </div>

          <div class="knowledge-actions">
            <el-button type="primary" @click="navigateTo('/project_5')">
              进入模块
            </el-button>

          </div>
        </div>

        <div class="knowledge-graph-card">
          <div class="kg-card-header">
            <div>
              <p>质量问题驱动型知识图谱</p>
              <h3>压力异常反馈 QF-00001</h3>
            </div>

          </div>

          <div ref="knowledgeGraphRef" class="knowledge-echart"></div>

          <div class="trace-chain">
            <span>当前追溯链</span>
            <p>质量反馈 → 关联部件 → 异常证据 → 候选根因 → 追溯报告</p>
          </div>
        </div>
      </section>

      <!-- 最近任务 -->
      <section class="section-block activity-block">
        <div class="section-header">
          <div>
            <p class="section-label">Recent Tasks</p>
            <h2 class="section-title">最近质量追溯任务</h2>
            <p class="section-desc">
              展示近期问题填报、算法分析、知识追溯和报告生成记录。
            </p>
          </div>

          <div class="activity-summary">
            <span class="summary-pill">{{ recentRecords.length }} 条记录</span>
            <span class="summary-pill summary-pill--accent">模拟数据</span>
          </div>
        </div>

        <div class="table-shell">
          <el-table
            :data="pagedRecords"
            stripe
            highlight-current-row
            class="home-table"
            @row-click="openRecordDetail"
          >
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column prop="problemCode" label="问题编号" width="150" />
            <el-table-column prop="module" label="关联模块" width="150" />
            <el-table-column prop="summary" label="任务摘要" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.statusType">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="owner" label="责任人" width="120" />
          </el-table>

          <div class="table-footer">
            <div class="table-footer__hint">点击任意行查看追溯任务详情</div>
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 15]"
              layout="total, sizes, prev, pager, next"
              :total="recentRecords.length"
              @size-change="handlePageSizeChange"
            />
          </div>
        </div>
      </section>

      
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="追溯任务详情"
      width="760px"
      custom-class="track-dialog"
    >
      <div v-if="selectedRecord" class="detail-dialog">
        <div class="detail-dialog__header">
          <div>
            <p class="detail-dialog__eyebrow">{{ selectedRecord.module }}</p>
            <h3 class="detail-dialog__title">{{ selectedRecord.summary }}</h3>
          </div>
          <el-tag size="small" :type="selectedRecord.statusType">
            {{ selectedRecord.status }}
          </el-tag>
        </div>

        <div class="detail-grid">
          <div class="detail-block">
            <p class="detail-label">问题编号</p>
            <p class="detail-value">{{ selectedRecord.problemCode }}</p>
          </div>
          <div class="detail-block">
            <p class="detail-label">追溯时间</p>
            <p class="detail-value">{{ selectedRecord.time }}</p>
          </div>
          <div class="detail-block">
            <p class="detail-label">责任人</p>
            <p class="detail-value">{{ selectedRecord.owner }}</p>
          </div>
          <div class="detail-block">
            <p class="detail-label">关联模块</p>
            <p class="detail-value">{{ selectedRecord.module }}</p>
          </div>
          <div class="detail-block detail-block--wide">
            <p class="detail-label">追溯说明</p>
            <p class="detail-value detail-value--muted">
              {{ selectedRecord.detail }}
            </p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()

const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(5)
const detailVisible = ref(false)
const selectedRecord = ref(null)

const projectOneMetrics = ref([])
const recentRecords = ref([])

const knowledgeGraphRef = ref(null)
const knowledgeGraphChart = ref(null)
const selectedKgNode = ref(null)

const middleProjects = ref([
  {
    key: 'project-2',
    label: '课题二',
    title: '工艺过程质量分析模块',
    icon: '二',
    iconClass: 'project-card__icon--blue',
    description: '展示工艺参数、过程波动、关键工序状态和异常趋势，用于支撑过程质量分析。',
    chartTitle: '工艺稳定度',
    chartValue: '91.2%',
    chartLabels: ['1月', '2月', '3月', '4月', '5月', '6月'],
    chartData: [62, 68, 74, 79, 84, 91],
    route: '/project_2',
    meta: [
      { name: '接入工序', value: '18' },
      { name: '异常批次', value: '6' },
      { name: '分析任务', value: '42' }
    ]
  },
  {
    key: 'project-3',
    label: '课题三',
    title: '故障诊断与状态识别模块',
    icon: '三',
    iconClass: 'project-card__icon--orange',
    description: '展示设备状态识别、故障类型分布和诊断结果，为质量异常定位提供依据。',
    chartTitle: '故障识别率',
    chartValue: '88.5%',
    chartLabels: ['泵', '阀', '缸', '管', '传', '控'],
    chartData: [72, 81, 64, 77, 88, 92],
    route: '/project_3',
    meta: [
      { name: '故障类型', value: '12' },
      { name: '诊断记录', value: '35' },
      { name: '待复核', value: '4' }
    ]
  },
  {
    key: 'project-4',
    label: '课题四',
    title: '试验检测与质量评价模块',
    icon: '四',
    iconClass: 'project-card__icon--purple',
    description: '汇总试验检测结果、评价等级和质量波动趋势，用于形成质量评价支撑信息。',
    chartTitle: '检测通过率',
    chartValue: '94.7%',
    chartLabels: ['A', 'B', 'C', 'D', 'E', 'F'],
    chartData: [86, 82, 90, 76, 94, 88],
    route: '/project_4',
    meta: [
      { name: '检测项目', value: '26' },
      { name: '合格记录', value: '168' },
      { name: '预警项', value: '5' }
    ]
  }
])

const knowledgeStats = ref([
  { label: '图谱节点数', value: '1,286' },
  { label: '关系数量', value: '3,942' },
  { label: '追溯任务', value: '58' },
  { label: '报告数量', value: '31' }
])

/**
 * 课题五首页知识图谱 JSON
 * 后续如果要从后端读取，只需要让接口返回同样结构：
 * {
 *   categories: [],
 *   nodes: [],
 *   links: []
 * }
 */
const qualityKgJson = {
  categories: [
    { name: '质量反馈', color: '#126fd3' },
    { name: '零部件', color: '#2d8ce4' },
    { name: '传感器', color: '#27b7d8' },
    { name: '制造过程', color: '#ff9a4b' },
    { name: '装配过程', color: '#8d7bff' },
    { name: '检测记录', color: '#28b97a' },
    { name: '根因推理', color: '#f36f65' },
    { name: '追溯报告', color: '#4cc08a' }
  ],
    nodes: [
  {
    id: 'QF-00001',
    name: '压力异常反馈',
    displayName: '压力异常\n反馈',
    category: 0,
    symbolSize: 82,
    x: 0,
    y: 0,
    fixed: false,
    desc: '用户填报的质量反馈事件，是课题五智能追溯流程的入口。'
  },
  {
    id: 'C010',
    name: '节流阀 C010',
    displayName: '节流阀\nC010',
    category: 1,
    symbolSize: 62,
    x: -330,
    y: -90,
    fixed: false,
    desc: '与压力异常反馈关联的关键零部件，用于定位质量问题影响对象。'
  },
  {
    id: 'M-2026',
    name: '加工批次 M2026',
    displayName: '加工批次\nM2026',
    category: 3,
    symbolSize: 62,
    x: -350,
    y: 110,
    fixed: false,
    desc: '关联制造阶段的工艺批次、加工参数和过程质量记录。'
  },
  {
    id: 'S-P01',
    name: '压力传感器 P01',
    displayName: '压力传感器\nP01',
    category: 2,
    symbolSize: 66,
    x: 310,
    y: -95,
    fixed: false,
    desc: '采集压力波动数据的异常传感器，为追溯提供动态证据。'
  },
  {
    id: 'E-Pressure',
    name: '压力波动超限',
    displayName: '压力波动\n超限',
    category: 5,
    symbolSize: 66,
    x: 270,
    y: -250,
    fixed: false,
    desc: '从传感器与检测数据中提取的关键异常证据。'
  },
  {
    id: 'T-Pressure',
    name: '压力测试记录',
    displayName: '压力测试\n记录',
    category: 5,
    symbolSize: 64,
    x: -20,
    y: -250,
    fixed: false,
    desc: '压力测试数据记录，用于支撑异常判断和证据链构建。'
  },
  {
    id: 'A-2026',
    name: '装配批次 A2026',
    displayName: '装配批次\nA2026',
    category: 4,
    symbolSize: 62,
    x: 360,
    y: 90,
    fixed: false,
    desc: '关联装配阶段的装配人员、装配时间和装配记录。'
  },
  {
    id: 'R-Stuck',
    name: '阀芯卡滞',
    displayName: '阀芯\n卡滞',
    category: 6,
    symbolSize: 62,
    x: -120,
    y: 235,
    fixed: false,
    desc: '算法推理得到的候选根因之一，表示节流阀内部阀芯可能存在卡滞。'
  },
  {
    id: 'R-Leak',
    name: '管路泄漏',
    displayName: '管路\n泄漏',
    category: 6,
    symbolSize: 58,
    x: 140,
    y: 240,
    fixed: false,
    desc: '算法推理得到的候选根因之一，表示管路密封或连接可能存在泄漏。'
  },
  {
    id: 'DOC-00001',
    name: '追溯报告',
    displayName: '追溯\n报告',
    category: 7,
    symbolSize: 58,
    x: 370,
    y: 270,
    fixed: false,
    desc: '根据质量反馈、证据链、候选根因和追溯结果自动生成的报告。'
  }
],
    links: [
      {
        source: 'QF-00001',
        target: 'C010',
        name: '关联部件',
        highlight: true
      },
      {
        source: 'C010',
        target: 'M-2026',
        name: '制造记录'
      },
      {
        source: 'QF-00001',
        target: 'S-P01',
        name: '关联传感器',
        highlight: true
      },
      {
        source: 'S-P01',
        target: 'E-Pressure',
        name: '采集证据',
        highlight: true
      },
      {
        source: 'QF-00001',
        target: 'T-Pressure',
        name: '关联检测'
      },
      {
        source: 'QF-00001',
        target: 'A-2026',
        name: '装配记录'
      },
      {
        source: 'T-Pressure',
        target: 'E-Pressure',
        name: '证据融合'
      },
      {
        source: 'E-Pressure',
        target: 'R-Stuck',
        name: '推理根因',
        highlight: true
      },
      {
        source: 'E-Pressure',
        target: 'R-Leak',
        name: '候选根因'
      },
      {
        source: 'R-Stuck',
        target: 'DOC-00001',
        name: '写入报告',
        highlight: true
      },
      {
        source: 'R-Leak',
        target: 'DOC-00001',
        name: '写入报告'
      }
    ]
  
}

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return recentRecords.value.slice(start, start + pageSize.value)
})

const navigateTo = (path) => {
  router.push(path)
}

const startSystem = () => {
  // 这里改成你的“问题填报页面”真实路由
  router.push('/problem/report')
}

const openRecordDetail = (row) => {
  selectedRecord.value = row
  detailVisible.value = true
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const initKnowledgeGraph = () => {
  if (!knowledgeGraphRef.value) {
    return
  }

  if (!knowledgeGraphChart.value) {
    knowledgeGraphChart.value = echarts.init(knowledgeGraphRef.value)
  }

  const categories = qualityKgJson.categories.map((item) => ({
    name: item.name,
    itemStyle: {
      color: item.color
    }
  }))

  const nodes = qualityKgJson.nodes.map((node) => {
  const category = qualityKgJson.categories[node.category]

  return {
    ...node,
    draggable: true,
    label: {
      show: true,
      color: '#ffffff',
      fontSize: node.symbolSize >= 76 ? 13 : 11,
      lineHeight: node.symbolSize >= 76 ? 17 : 15,
      fontWeight: 700,
      align: 'center',
      verticalAlign: 'middle',
      formatter: () => node.displayName || node.name
    },
    itemStyle: {
      color: category.color,
      shadowBlur: 16,
      shadowColor: 'rgba(34, 83, 132, 0.22)'
    }
  }
})

  const links = qualityKgJson.links.map((link) => ({
    ...link,
    lineStyle: {
      width: link.highlight ? 3 : 1.5,
      color: link.highlight ? '#126fd3' : 'rgba(72, 117, 166, 0.38)',
      curveness: 0.08
    },
    label: {
      show: false
    }
  }))

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      confine: true,
      borderWidth: 0,
      backgroundColor: 'rgba(17, 40, 68, 0.92)',
      textStyle: {
        color: '#ffffff',
        fontSize: 12
      },
      formatter: (params) => {
  if (params.dataType === 'node') {
    const data = params.data
    return `
      <div style="font-weight:700;margin-bottom:6px;">${data.name}</div>
      <div style="line-height:1.6;">${data.desc || ''}</div>
    `
  }

  if (params.dataType === 'edge') {
    return `
      <div style="font-weight:700;margin-bottom:4px;">${params.data.name}</div>
      <div>${params.data.source} → ${params.data.target}</div>
    `
  }

  return params.name
}
    },
    legend: {
        top: 12,
        right: 18,
        orient: 'horizontal',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 8,
        textStyle: {
          color: '#52677f',
          fontSize: 10
        },
        data: categories.map((item) => item.name)
      },
    series: [
  {
    name: '课题五知识图谱',
    type: 'graph',
    layout: 'none',
    categories,
    data: nodes,
    links,
    roam: true,
    draggable: true,
    focusNodeAdjacency: true,
    edgeSymbol: ['none', 'arrow'],
    edgeSymbolSize: [0, 8],
    left: '2%',
    right: '2%',
    top: '18%',
    bottom: '18%',
    scaleLimit: {
      min: 0.45,
      max: 2.8
    },
    label: {
      show: true,
      position: 'inside',
      color: '#ffffff',
      fontWeight: 700
    },
    edgeLabel: {
      show: false
    },
    lineStyle: {
      opacity: 0.8,
      curveness: 0.12
    },
    emphasis: {
      focus: 'adjacency',
      lineStyle: {
        width: 4
      },
      label: {
        show: true
      },
      edgeLabel: {
        show: true,
        color: '#355d8a',
        fontSize: 10,
        formatter: (params) => params.data.name
      }
    },
    animationDuration: 800,
    animationEasingUpdate: 'quinticInOut'
  }
]
  }

  knowledgeGraphChart.value.setOption(option)

  knowledgeGraphChart.value.off('click')
  knowledgeGraphChart.value.on('click', (params) => {
    if (params.dataType === 'node') {
      selectedKgNode.value = {
        name: params.data.name,
        desc: params.data.desc || '暂无节点说明。'
      }
    }
  })

  selectedKgNode.value = {
    name: '压力异常反馈',
    desc: '用户填报的质量反馈事件，是课题五智能追溯流程的入口。'
  }
}

const resizeKnowledgeGraph = () => {
  if (knowledgeGraphChart.value) {
    knowledgeGraphChart.value.resize()
  }
}

const loadHomeData = async () => {
  loading.value = true

  await new Promise((resolve) => setTimeout(resolve, 500))

  projectOneMetrics.value = [
    {
      key: 'data-access',
      label: '数据接入量',
      value: '12,860',
      change: '+8.6%',
      trendClass: 'positive',
      icon: 'D',
      unit: '条记录'
    },
    {
      key: 'task-count',
      label: '分析任务数',
      value: '246',
      change: '+16',
      trendClass: 'positive',
      icon: 'T',
      unit: '项任务'
    },
    {
      key: 'abnormal-count',
      label: '异常识别数',
      value: '32',
      change: '-12.5%',
      trendClass: 'negative',
      icon: 'A',
      unit: '待处理'
    },
    {
      key: 'module-status',
      label: '模块运行状态',
      value: '正常',
      change: '在线',
      trendClass: 'positive',
      icon: 'S',
      unit: '稳定运行'
    }
  ]

  recentRecords.value = [
    {
      time: '2026-06-12 09:20',
      problemCode: 'QF-20260612-001',
      module: '知识图谱追溯',
      summary: '液压系统压力异常问题已完成知识图谱追溯',
      detail: '系统根据问题填报信息关联零部件、传感器、装配过程和检测记录，生成候选根因并形成追溯链。',
      status: '已完成',
      owner: '李工',
      statusType: 'success'
    },
    {
      time: '2026-06-11 16:40',
      problemCode: 'QF-20260611-004',
      module: '课题三',
      summary: '作动筒响应迟滞故障进入诊断复核流程',
      detail: '诊断模型输出疑似液压泄漏和阀控异常两类原因，目前等待人工复核。',
      status: '复核中',
      owner: '张工',
      statusType: 'warning'
    },
    {
      time: '2026-06-11 14:05',
      problemCode: 'QF-20260611-002',
      module: '课题二',
      summary: '关键工序参数波动已触发过程质量预警',
      detail: '系统检测到工艺参数连续波动，已关联对应批次和设备状态记录。',
      status: '分析中',
      owner: '王工',
      statusType: 'danger'
    },
    {
      time: '2026-06-10 18:30',
      problemCode: 'QF-20260610-006',
      module: '课题四',
      summary: '试验检测数据已完成质量评价并归档',
      detail: '检测数据通过质量评价模型计算后，形成评价等级并同步至质量档案。',
      status: '已归档',
      owner: '赵工',
      statusType: 'success'
    },
    {
      time: '2026-06-10 11:15',
      problemCode: 'QF-20260610-003',
      module: '课题一',
      summary: '基础数据统计看板完成本周数据更新',
      detail: '课题一数据接入量、任务数量和异常识别统计已完成更新。',
      status: '已更新',
      owner: '刘工',
      statusType: 'success'
    },
    {
      time: '2026-06-09 17:25',
      problemCode: 'QF-20260609-005',
      module: '知识图谱追溯',
      summary: '质量追溯报告已导出并写入报告记录',
      detail: '系统已根据追溯链、候选根因、算法分析结果和任务信息生成 Word 报告。',
      status: '已导出',
      owner: '周工',
      statusType: 'success'
    }
  ]

  loading.value = false
}

onMounted(async () => {
  await loadHomeData()
  await nextTick()
  initKnowledgeGraph()
  window.addEventListener('resize', resizeKnowledgeGraph)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeKnowledgeGraph)

  if (knowledgeGraphChart.value) {
    knowledgeGraphChart.value.dispose()
    knowledgeGraphChart.value = null
  }
})
</script>

<style scoped lang="scss">
.home-view {
  min-height: calc(100vh - 24px);
  padding: 12px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at top left, rgba(78, 148, 224, 0.22), transparent 28%),
    radial-gradient(circle at top right, rgba(66, 196, 219, 0.16), transparent 26%),
    linear-gradient(180deg, #f3f8ff 0%, #e7eff9 46%, #dfe8f4 100%);
}

.home-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) 420px;
  gap: 18px;
  padding: 22px;
  border-radius: 24px;
  border: 1px solid rgba(89, 145, 202, 0.28);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(240, 248, 255, 0.92)),
    rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 44px rgba(76, 112, 150, 0.18);
}

.hero-left {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.home-title {
  margin: 0;
  color: #112844;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.home-subtitle {
  margin: 12px 0 0;
  max-width: 820px;
  color: #5f718c;
  line-height: 1.7;
  font-size: 14px;
}

.hero-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 999px;
  border: 1px solid rgba(100, 154, 212, 0.32);
  background: rgba(255, 255, 255, 0.84);
  color: #4c638c;
  font-size: 12px;
}

.meta-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.meta-dot--green {
  background: #20b26f;
}

.meta-dot--blue {
  background: #1f8df0;
}

.meta-dot--cyan {
  background: #10afdb;
}

.hero-action {
  display: flex;
  align-items: stretch;
}

.start-card {
  width: 100%;
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(44, 132, 218, 0.28);
  background:
    linear-gradient(180deg, rgba(233, 246, 255, 0.96), rgba(255, 255, 255, 0.96)),
    rgba(255, 255, 255, 0.88);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.62);
}

.start-card__title {
  margin: 0;
  color: #122641;
  font-size: 20px;
  font-weight: 800;
}

.start-card__desc {
  margin: 12px 0 18px;
  color: #65768e;
  font-size: 13px;
  line-height: 1.6;
}

.start-button {
  width: 100%;
  height: 44px;
  font-weight: 700;
}

.section-block {
  border-radius: 24px;
  border: 1px solid rgba(118, 152, 197, 0.32);
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(6px);
  box-shadow: 0 18px 44px rgba(70, 97, 132, 0.14);
  padding: 18px 18px 20px;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.section-header--plain {
  margin-bottom: 12px;
}

.section-label {
  margin: 0 0 4px;
  color: #2364aa;
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.24em;
}

.section-title {
  margin: 0;
  color: #122641;
  font-size: 19px;
  font-weight: 800;
}

.section-desc {
  margin: 8px 0 0;
  color: #667892;
  font-size: 13px;
  line-height: 1.6;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(112, 154, 205, 0.4);
  background:
    linear-gradient(180deg, #ffffff, #f5f8fe),
    rgba(244, 248, 255, 0.96);
  box-shadow: 0 12px 24px rgba(72, 106, 149, 0.12);
}

.metric-card__header,
.metric-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.metric-card__label {
  color: #6b7f98;
  font-size: 12px;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 9px;
  background: linear-gradient(180deg, #127fd5, #0e5aa4);
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.metric-card__value {
  margin: 12px 0 10px;
  color: #091a2f;
  font-size: 30px;
  font-weight: 800;
}

.metric-card__trend {
  font-size: 12px;
  font-weight: 700;
}

.metric-card__trend.positive {
  color: #1f9d6b;
}

.metric-card__trend.negative {
  color: #f29733;
}

.metric-card__unit {
  color: #6f7f97;
  font-size: 11px;
}

.project-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.project-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 380px;
  padding: 16px;
  border-radius: 22px;
  border: 1px solid rgba(103, 145, 194, 0.34);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(245, 249, 255, 0.94)),
    rgba(255, 255, 255, 0.9);
  box-shadow: 0 12px 28px rgba(68, 100, 142, 0.12);
}

.project-card__top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  flex: 0 0 46px;
  border-radius: 15px;
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}

.project-card__icon--blue {
  background: linear-gradient(180deg, #2d8ce4, #0f5f9e);
}

.project-card__icon--orange {
  background: linear-gradient(180deg, #ff9a4b, #df6d22);
}

.project-card__icon--purple {
  background: linear-gradient(180deg, #8d7bff, #6754d9);
}

.project-card__label {
  margin: 0 0 4px;
  color: #2a72bb;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.project-card__title {
  margin: 0;
  color: #132b45;
  font-size: 17px;
  font-weight: 800;
}

.project-card__desc {
  margin: 0;
  color: #64758e;
  font-size: 13px;
  line-height: 1.65;
}

.mini-chart {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(104, 148, 197, 0.26);
  background: rgba(246, 250, 255, 0.96);
}

.mini-chart__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #53677e;
  font-size: 12px;
}

.mini-chart__header strong {
  color: #10395f;
  font-size: 18px;
}

.bar-chart {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  align-items: end;
  gap: 8px;
  height: 130px;
  margin-top: 12px;
}

.bar-chart__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}

.bar-chart__bar {
  position: relative;
  width: 100%;
  height: 96px;
  border-radius: 999px;
  background: #e1edf8;
  overflow: hidden;
}

.bar-chart__bar span {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 999px;
  background: linear-gradient(180deg, #39a6f6, #126ab8);
}

.bar-chart__item p {
  margin: 6px 0 0;
  color: #6c7d90;
  font-size: 10px;
}

.project-card__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.meta-item {
  padding: 10px;
  border-radius: 14px;
  background: #f4f8fe;
  border: 1px solid rgba(104, 148, 197, 0.22);
}

.meta-item span {
  display: block;
  color: #728195;
  font-size: 11px;
}

.meta-item strong {
  display: block;
  margin-top: 5px;
  color: #142d48;
  font-size: 17px;
}

.module-button {
  margin-top: auto;
  width: 100%;
}

.knowledge-section {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(520px, 1.1fr);
  gap: 16px;
  align-items: stretch;
}

.knowledge-left {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.kg-stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 12px 0 14px;
}

.kg-stat {
  padding: 14px;
  border-radius: 16px;
  background: #f5f9ff;
  border: 1px solid rgba(104, 148, 197, 0.26);
}

.kg-stat p {
  margin: 0 0 6px;
  color: #667992;
  font-size: 12px;
}

.kg-stat strong {
  color: #102d4b;
  font-size: 22px;
  font-weight: 800;
}

.knowledge-status {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.knowledge-status span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(228, 241, 255, 0.92);
  border: 1px solid rgba(92, 145, 202, 0.25);
  color: #255f9c;
  font-size: 11px;
  font-weight: 700;
}

.kg-node-detail {
  padding: 12px 14px;
  margin-bottom: 16px;
  border-radius: 16px;
  background: #f6faff;
  border: 1px solid rgba(92, 145, 202, 0.24);
}

.kg-node-detail__label {
  margin: 0 0 4px;
  color: #2364aa;
  font-size: 10px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.kg-node-detail h4 {
  margin: 0;
  color: #122641;
  font-size: 15px;
  font-weight: 800;
}

.kg-node-detail p {
  margin: 6px 0 0;
  color: #64758e;
  font-size: 12px;
  line-height: 1.55;
}

.knowledge-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.knowledge-graph-card {
  position: relative;
  min-height: 520px;
  border-radius: 22px;
  border: 1px solid rgba(95, 139, 190, 0.34);
  background:
    radial-gradient(circle at center, rgba(54, 143, 227, 0.16), transparent 30%),
    linear-gradient(180deg, #f7fbff, #edf5ff);
  overflow: hidden;
}

.knowledge-graph-card::before {
  content: '';
  position: absolute;
  inset: 18px;
  border-radius: 20px;
  border: 1px dashed rgba(45, 120, 194, 0.22);
  pointer-events: none;
}

.kg-card-header {
  position: absolute;
  left: 18px;
  right: 18px;
  top: 14px;
  z-index: 3;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  pointer-events: none;
}

.kg-card-header p {
  margin: 0 0 4px;
  color: #2364aa;
  font-size: 10px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.kg-card-header h3 {
  margin: 0;
  color: #122641;
  font-size: 16px;
  font-weight: 800;
}

.kg-card-tips {
  padding: 6px 10px;
  border-radius: 999px;
  color: #1d5c9d;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(86, 143, 204, 0.28);
  font-size: 10px;
  font-weight: 700;
}

.knowledge-echart {
  position: absolute;
  inset: 0;
  z-index: 2;
  width: 100%;
  height: 100%;
}

.trace-chain {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 18px;
  z-index: 3;
  padding: 11px 13px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(80, 135, 196, 0.26);
  box-shadow: 0 8px 20px rgba(48, 92, 142, 0.12);
  pointer-events: none;
}

.trace-chain span {
  color: #1c6db4;
  font-size: 11px;
  font-weight: 800;
}

.trace-chain p {
  margin: 5px 0 0;
  color: #475d76;
  font-size: 12px;
  line-height: 1.5;
}

.activity-summary {
  display: flex;
  gap: 8px;
}

.summary-pill {
  padding: 6px 9px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: #556679;
  font-size: 10px;
  border: 1px solid rgba(109, 147, 194, 0.28);
}

.summary-pill--accent {
  color: #1b79bb;
  border-color: rgba(28, 132, 208, 0.34);
}

.table-shell {
  border-radius: 18px;
  border: 1px solid rgba(98, 136, 186, 0.28);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
}

.home-table {
  :deep(.el-table__header) {
    background: #eaf2fb;
    color: #223b57;
  }

  :deep(th.el-table__cell) {
    background: #eaf2fb;
    color: #223b57;
    border-bottom-color: rgba(77, 118, 168, 0.24);
  }

  :deep(td.el-table__cell) {
    background: #fff;
    color: #1a2f43;
    border-bottom-color: rgba(76, 113, 154, 0.18);
  }

  :deep(.el-table__row:hover > td) {
    background: #f4f8fe;
  }
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-top: 1px solid rgba(98, 136, 186, 0.24);
}

.table-footer__hint {
  color: #60718e;
  font-size: 11px;
}

.bottom-start-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border-radius: 24px;
  border: 1px solid rgba(44, 132, 218, 0.28);
  background:
    linear-gradient(135deg, rgba(24, 123, 211, 0.94), rgba(13, 78, 151, 0.96));
  color: #fff;
  box-shadow: 0 18px 44px rgba(45, 95, 155, 0.2);
}

.bottom-start-section__label {
  margin: 0 0 4px;
  font-size: 10px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  opacity: 0.82;
}

.bottom-start-section h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
}

.bottom-start-section p {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.detail-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
}

.detail-dialog__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.detail-dialog__eyebrow {
  margin: 0 0 4px;
  color: #2674bc;
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.detail-dialog__title {
  margin: 0;
  color: #15233d;
  font-size: 18px;
  font-weight: 700;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.detail-block {
  padding: 12px 13px;
  border-radius: 14px;
  border: 1px solid rgba(71, 119, 171, 0.28);
  background: #f8fbff;
}

.detail-block--wide {
  grid-column: 1 / span 2;
}

.detail-label {
  margin: 0 0 6px;
  color: #5e7f9f;
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
}

.detail-value {
  margin: 0;
  color: #16314c;
  font-size: 13px;
  line-height: 1.5;
}

.detail-value--muted {
  color: #597189;
}

@media (max-width: 1200px) {
  .home-hero {
    grid-template-columns: 1fr;
  }

  .knowledge-section {
    grid-template-columns: 1fr;
  }

  .knowledge-graph-card {
    min-height: 500px;
  }
}

@media (max-width: 1100px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .project-card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 780px) {
  .home-view {
    padding: 8px;
  }

  .home-title {
    font-size: 24px;
  }

  .section-header,
  .table-footer,
  .bottom-start-section {
    align-items: flex-start;
    flex-direction: column;
  }

  .metric-grid,
  .kg-stat-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-block--wide {
    grid-column: auto;
  }

  .knowledge-graph-card {
    min-height: 460px;
  }
}
</style>
<template>
  <div class="dashboard-view">
    <div class="dashboard-shell">
      <section class="dashboard-topbar">
        <div>
          <p class="eyebrow">航空科技 · 质量追溯中心</p>
          <h1 class="dashboard-title">智能质量运营仪表盘</h1>
          <p class="dashboard-subtitle">
            面向关键工序、质量闭环与追溯活动的实时监控中心，支持专题切换、异常预警及追溯详情回溯。
          </p>
        </div>

        <div class="topbar-meta">
          <div class="meta-chip">
            <span class="meta-dot meta-dot--green"></span>
            <span>系统在线</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--blue"></span>
            <span>实时采集</span>
          </div>
          <div class="meta-chip">
            <span class="meta-dot meta-dot--cyan"></span>
            <span>追溯完备</span>
          </div>
        </div>
      </section>

      <section class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">关键指标</p>
            <h2 class="section-title">运营概览</h2>
          </div>
          <el-tag round size="small" type="success" class="live-tag">模拟数据 · 2026</el-tag>
        </div>

        <div class="metric-grid" v-loading="loading">
          <div v-for="metric in keyMetrics" :key="metric.key" class="metric-card">
            <div class="metric-card__header">
              <span class="metric-card__label">{{ metric.label }}</span>
              <span class="metric-card__icon">{{ metric.icon }}</span>
            </div>
            <div class="metric-card__value">{{ metric.value }}</div>
            <div class="metric-card__footer">
              <span class="metric-card__trend" :class="metric.trendClass">{{ metric.change }}</span>
              <span class="metric-card__unit">{{ metric.unit }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="section-block mix-zone">
        <div class="mix-zone__left">
          <div class="section-header">
            <div>
              <p class="section-label">专题切换</p>
              <h2 class="section-title">质量趋势</h2>
            </div>
            <div class="insight-badge">{{ activeTopic.title }}</div>
          </div>

          <div class="split-layout">
            <aside class="topic-nav" aria-label="子课题导航">
              <div
                v-for="item in topicOptions"
                :key="item.key"
                class="topic-nav__item"
                :class="{ active: selectedTopic === item.key }"
                @click="navigateToTopic(item)"
              >
                <div class="topic-nav__icon">{{ item.icon }}</div>
                <div class="topic-nav__content">
                  <p class="topic-nav__title">{{ item.title }}</p>
                  <p class="topic-nav__desc">{{ item.summary }}</p>
                </div>
                <span class="topic-nav__chevron">→</span>
              </div>
            </aside>

            <article class="chart-panel">
              <div class="chart-panel__header">
                <div>
                  <p class="chart-panel__label">{{ activeTopic.subtitle }}</p>
                  <h3 class="chart-panel__title">{{ activeTopic.title }}趋势</h3>
                </div>
                <div class="chart-panel__status">
                  <span class="status-ring"></span>
                  追溯联动中
                </div>
              </div>

              <TrendChart :x-axis-data="chartData.xAxis" :series-data="chartData.series" />

              <div class="chart-caption">
                <span class="caption-dot"></span>
                当前专题质量波动由设备状态、工序参数与检验结果联合驱动，已接入AI预警模型。
              </div>
            </article>
          </div>
        </div>

        <div class="mix-zone__right">
          <div class="section-header">
            <div>
              <p class="section-label">快捷入口</p>
              <h2 class="section-title">五大子课题</h2>
            </div>
          </div>

          <div class="shortcut-grid">
            <ShortcutCard
              v-for="entry in shortcutEntries"
              :key="entry.key"
              :item="entry"
              @navigate="navigateToTopic"
            />
          </div>
        </div>
      </section>

      <section class="section-block activity-block">
        <div class="section-header">
          <div>
            <p class="section-label">最新活动</p>
            <h2 class="section-title">最近追溯活动</h2>
          </div>
          <div class="activity-summary">
            <span class="summary-pill">{{ recentRecords.length }} 条记录</span>
            <span class="summary-pill summary-pill--accent">分页浏览</span>
          </div>
        </div>

        <div class="table-shell">
          <el-table
            :data="pagedRecords"
            stripe
            highlight-current-row
            @row-click="openRecordDetail"
            class="dashboard-table"
          >
            <el-table-column prop="time" label="时间" width="150" />
            <el-table-column prop="project" label="项目" width="170" />

            <el-table-column prop="summary" label="追溯摘要" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.statusType">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="owner" label="责任人" width="120" />
          </el-table>

          <div class="table-footer">
            <div class="table-footer__hint">点击任意行查看详情</div>
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

    <el-dialog v-model="detailVisible" title="追溯详情" width="760px" custom-class="track-dialog">
      <div v-if="selectedRecord" class="detail-dialog">
        <div class="detail-dialog__header">
          <div>
            <p class="detail-dialog__eyebrow">{{ selectedRecord.project }}</p>
            <h3 class="detail-dialog__title">{{ selectedRecord.summary }}</h3>
          </div>
          <el-tag size="small" :type="selectedRecord.statusType">{{ selectedRecord.status }}</el-tag>
        </div>

        <div class="detail-grid">
          <div class="detail-block">
            <p class="detail-label">追溯时间</p>
            <p class="detail-value">{{ selectedRecord.time }}</p>
          </div>
          <div class="detail-block">
            <p class="detail-label">责任人</p>
            <p class="detail-value">{{ selectedRecord.owner }}</p>
          </div>
          <div class="detail-block detail-block--wide">
            <p class="detail-label">关联子课题</p>
            <p class="detail-value">{{ selectedRecord.topic }}</p>
          </div>
          <div class="detail-block detail-block--wide">
            <p class="detail-label">追溯说明</p>
            <p class="detail-value detail-value--muted">{{ selectedRecord.detail }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TrendChart from '@/components/dashboard/TrendChart.vue'
import ShortcutCard from '@/components/dashboard/ShortcutCard.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const selectedTopic = ref('quality')
const currentPage = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)
const selectedRecord = ref(null)

const keyMetrics = ref([])
const recentRecords = ref([])

const topicOptions = [
  {
    key: 'quality',
    title: '质量追溯',
    icon: 'Q',
    summary: '检验指标与批次关联',
    subtitle: '质量追溯链路',
    route: '/index?topic=quality'
  },
  {
    key: 'process',
    title: '工艺治理',
    icon: 'G',
    summary: '工序偏差与治理',
    subtitle: '工艺治理监测',
    route: '/index?topic=process'
  },
  {
    key: 'fault',
    title: '故障分析',
    icon: 'F',
    summary: '异常模式识别',
    subtitle: '故障分析看板',
    route: '/index?topic=fault'
  },
  {
    key: 'equipment',
    title: '设备监测',
    icon: 'E',
    summary: '设备健康状态',
    subtitle: '设备监测趋势',
    route: '/index?topic=equipment'
  },
  {
    key: 'archive',
    title: '数据归档',
    icon: 'D',
    summary: '归档、审计与留痕',
    subtitle: '数据归档分析',
    route: '/index?topic=archive'
  }
]

const shortcutEntries = [
  {
    key: 'project-1',
    title: '课题一',
    icon: '一',
    badge: '课题',
    tagType: 'success',
    description: '进入课题一专题页面查看详情与运行看板。',
    meta: '专题 1',
    route: '/project_1'
  },
  {
    key: 'project-2',
    title: '课题二',
    icon: '二',
    badge: '课题',
    tagType: 'primary',
    description: '进入课题二专题页面查看重点数据与状态。',
    meta: '专题 2',
    route: '/project_2'
  },
  {
    key: 'project-3',
    title: '课题三',
    icon: '三',
    badge: '课题',
    tagType: 'danger',
    description: '进入课题三专题页面查看异常与治理情况。',
    meta: '专题 3',
    route: '/project_3'
  },
  {
    key: 'project-4',
    title: '课题四',
    icon: '四',
    badge: '课题',
    tagType: 'info',
    description: '进入课题四专题页面查看设备与监测汇总。',
    meta: '专题 4',
    route: '/project_4'
  },
  {
    key: 'project-5',
    title: '课题五',
    icon: '五',
    badge: '课题',
    tagType: 'warning',
    description: '进入课题五专题页面查看归档与汇总结果。',
    meta: '专题 5',
    route: '/project_5'
  }
]

const chartDataset = {
  quality: {
    xAxis: ['一月', '二月', '三月', '四月', '五月', '六月'],
    series: [
      { name: '综合质量', color: '#19b5ff', data: [86, 89, 91, 94, 92, 96] },
      { name: '批次一致性', color: '#5dd4a4', data: [79, 84, 86, 88, 90, 93] }
    ]
  },
  process: {
    xAxis: ['一月', '二月', '三月', '四月', '五月', '六月'],
    series: [
      { name: '工艺稳定度', color: '#23b8ff', data: [74, 76, 79, 81, 84, 88] },
      { name: '工艺稳定度', color: '#6fd9f9', data: [68, 71, 74, 78, 82, 86] }
    ]
  },
  fault: {
    xAxis: ['一月', '二月', '三月', '四月', '五月', '六月'],
    series: [
      { name: '异常识别率', color: '#ff8f5c', data: [70, 73, 76, 79, 82, 85] },
      { name: '处置效率', color: '#ffa756', data: [64, 67, 69, 73, 76, 79] }
    ]
  },
  equipment: {
    xAxis: ['一月', '二月', '三月', '四月', '五月', '六月'],
    series: [
      { name: '设备健康', color: '#32c1ff', data: [82, 84, 87, 89, 91, 94] },
      { name: '预防性维护', color: '#8fd2ff', data: [75, 79, 81, 84, 88, 91] }
    ]
  },
  archive: {
    xAxis: ['一月', '二月', '三月', '四月', '五月', '六月'],
    series: [
      { name: '档案完整率', color: '#9b87ff', data: [88, 90, 91, 92, 94, 95] },
      { name: '审计通过率', color: '#c4adff', data: [83, 85, 88, 90, 92, 94] }
    ]
  }
}

const activeTopic = computed(() => topicOptions.find((item) => item.key === selectedTopic.value) || topicOptions[0])

const chartData = computed(() => chartDataset[selectedTopic.value] || chartDataset.quality)

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return recentRecords.value.slice(start, start + pageSize.value)
})

const navigateToTopic = (topic) => {
  const target = typeof topic === 'string' ? { key: topic } : topic
  const key = target.key

  if (target.route) {
    router.push(target.route)
    return
  }

  selectedTopic.value = key
  router.push({ path: '/index', query: { topic: key } })
}

const openRecordDetail = (row) => {
  selectedRecord.value = row
  detailVisible.value = true
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const loadDashboardData = async () => {
  loading.value = true
  await new Promise((resolve) => setTimeout(resolve, 650))

  keyMetrics.value = [
    { key: 'quality-score', label: '质量合格率', value: '96.4%', change: '+2.8%', trendClass: 'positive', icon: '◎', unit: '连续达标' },
    { key: 'trace-rate', label: '追溯覆盖率', value: '99.1%', change: '+1.4%', trendClass: 'positive', icon: '?', unit: '全链路' },
    { key: 'alarm-count', label: '预警处置', value: '24', change: '-11%', trendClass: 'negative', icon: '?', unit: '待处置' },
    { key: 'trend-score', label: '工艺稳定度', value: '91.6', change: '+4.3%', trendClass: 'positive', icon: '↗', unit: '评分' }
  ]

  recentRecords.value = [
    {
      time: '2026-05-29 09:20',
      project: '航电系统',
      topic: '质量追溯',
      summary: 'A-12批次抽检通过并完成闭环记录',
      detail: '从原材料入库、试验记录到终检报告全部关联，异常波动已归档，责任人已确认。',
      status: '已完成',
      owner: '李工',
      statusType: 'success',
      tagType: 'success'
    },
    {
      time: '2026-05-29 08:10',
      project: '发动机舱',
      topic: '工艺治理',
      summary: '工序温度超限已执行整改并复测',
      detail: '工艺微调策略已提交，设备回路校准完成，第二轮监测结果正常。',
      status: '整改中',
      owner: '张工',
      statusType: 'warning',
      tagType: 'primary'
    },
    {
      time: '2026-05-28 17:45',
      project: '雷达单元',
      topic: '故障分析',
      summary: '信号异常更新为典型振荡故障模式',
      detail: '异常路径已追踪至传感器供电波动，推荐更换滤波模块并排查接线。',
      status: '分析中',
      owner: '周工',
      statusType: 'danger',
      tagType: 'danger'
    },
    {
      time: '2026-05-28 15:30',
      project: '机载导航',
      topic: '设备监测',
      summary: '振动监测数据正常，设备运行稳定',
      detail: '最新一轮运行时长健康评分达到94分，设备保持轻载稳定运行。',
      status: '正常',
      owner: '王工',
      statusType: 'success',
      tagType: 'info'
    },
    {
      time: '2026-05-28 13:20',
      project: '机载系统',
      topic: '数据归档',
      summary: '归档包已完成审计并更新留痕',
      detail: '对应项目归档版本已提交档案库，审计记录与责任链完整同步。',
      status: '已归档',
      owner: '刘工',
      statusType: 'success',
      tagType: 'warning'
    },
    {
      time: '2026-05-27 10:50',
      project: '电源模块',
      topic: '工艺治理',
      summary: '工艺参数回归监测完成',
      detail: '工艺管控调整后的复测结果稳定在预期区间内，建议继续跟踪。',
      status: '已完成',
      owner: '陈工',
      statusType: 'success',
      tagType: 'primary'
    },
    {
      time: '2026-05-27 09:05',
      project: '飞控系统',
      topic: '故障分析',
      summary: '回归工单已送达运维复核',
      detail: '根因分析摘要已附带整改建议，等待运维复核并进入二次验证。',
      status: '待复核',
      owner: '邓工',
      statusType: 'warning',
      tagType: 'danger'
    },
    {
      time: '2026-05-26 16:30',
      project: '传感器阵列',
      topic: '质量追溯',
      summary: '异常样本已标记并进入二次检验',
      detail: '样本编号已同步到追溯系统，追溯链路覆盖到设备运行记录。',
      status: '复检中',
      owner: '孙工',
      statusType: 'warning',
      tagType: 'success'
    },
    {
      time: '2026-05-26 14:10',
      project: '机载传感',
      topic: '设备监测',
      summary: '传感器预警阈值已更新',
      detail: '设备监测阈值已滚动调整，结合历史数据重新生成阈值区间。',
      status: '已更新',
      owner: '赵工',
      statusType: 'success',
      tagType: 'info'
    },
    {
      time: '2026-05-25 11:40',
      project: '结构件',
      topic: '数据归档',
      summary: '年度归档索引已重新生成',
      detail: '下载与留痕记录完成自动归档，索引已更新至主数据仓库。',
      status: '已归档',
      owner: '吴工',
      statusType: 'success',
      tagType: 'warning'
    }
  ]

  loading.value = false
}

watch(
  () => route.query.topic,
  (newTopic) => {
    if (newTopic) {
      selectedTopic.value = String(newTopic)
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.dashboard-view {
  min-height: calc(100vh - 24px);
  padding: 12px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at top, rgba(93, 149, 212, 0.24), transparent 24%),
    linear-gradient(180deg, #f3f8ff 0%, #e4edf8 42%, #dfe8f4 100%);
}

.dashboard-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.dashboard-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(89, 145, 202, 0.28);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(243, 248, 255, 0.92));
  box-shadow: 0 18px 44px rgba(76, 112, 150, 0.18);
}

.eyebrow {
  margin: 0 0 6px;
  color: #2f6cb3;
  text-transform: uppercase;
  letter-spacing: 0.24em;
  font-size: 10px;
}

.dashboard-title {
  margin: 0;
  color: #172c4e;
  font-size: 30px;
  font-weight: 800;
}

.dashboard-subtitle {
  margin: 8px 0 0;
  max-width: 760px;
  color: #60728f;
  line-height: 1.5;
}

.topbar-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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
  font-size: 11px;
}

.meta-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.meta-dot--green { background: #20b26f; }
.meta-dot--blue { background: #1f8df0; }
.meta-dot--cyan { background: #10afdb; }

.section-block {
  border-radius: 24px;
  border: 1px solid rgba(118, 152, 197, 0.32);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(6px);
  box-shadow: 0 18px 44px rgba(70, 97, 132, 0.16);
  padding: 18px 18px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
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
  font-size: 18px;
  font-weight: 700;
}

.live-tag {
  border: 0;
  background: rgba(16, 177, 107, 0.14);
  color: #11774d;
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
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: linear-gradient(180deg, #127fd5, #0e5aa4);
  color: #fff;
  font-size: 12px;
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

.metric-card__trend.positive { color: #1f9d6b; }
.metric-card__trend.negative { color: #f29733; }

.metric-card__unit {
  color: #6f7f97;
  font-size: 11px;
}

.mix-zone {
  display: grid;
  grid-template-columns: 1.55fr 0.95fr;
  gap: 16px;
}

.mix-zone__left,
.mix-zone__right {
  display: flex;
  flex-direction: column;
}

.split-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 14px;
  min-height: 360px;
}

.topic-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.topic-nav__item {
  display: grid;
  grid-template-columns: 32px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(107, 149, 198, 0.34);
  background: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  transition: all 0.22s ease;
}

.topic-nav__item:hover,
.topic-nav__item.active {
  border-color: rgba(27, 129, 215, 0.72);
  box-shadow: 0 0 0 1px rgba(27, 129, 215, 0.24), 0 12px 24px rgba(67, 109, 153, 0.16);
  transform: translateX(2px);
}

.topic-nav__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(180deg, #2d8ce4, #0f5f9e);
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}

.topic-nav__content {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.topic-nav__title {
  margin: 0;
  color: #132b45;
  font-size: 13px;
  font-weight: 700;
}

.topic-nav__desc {
  margin: 0;
  color: #6e7890;
  font-size: 10px;
}

.topic-nav__chevron {
  color: #1a72c2;
  font-size: 14px;
}

.chart-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  border-radius: 20px;
  border: 1px solid rgba(101, 145, 193, 0.3);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(244, 249, 255, 0.9)),
    rgba(249, 251, 255, 0.96);
}

.chart-panel__header,
.chart-panel__status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.chart-panel__label {
  margin: 0 0 4px;
  color: #2b78ca;
  font-size: 10px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.chart-panel__title {
  margin: 0;
  color: #152842;
  font-size: 18px;
  font-weight: 700;
}

.chart-panel__status {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(225, 241, 255, 0.9);
  color: #4f6f8e;
  font-size: 10px;
}

.status-ring {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #13b16d;
  box-shadow: 0 0 0 4px rgba(19, 177, 109, 0.16);
}

.insight-badge {
  padding: 8px 10px;
  border-radius: 999px;
  color: #1d5c9d;
  background: rgba(219, 238, 255, 0.92);
  border: 1px solid rgba(86, 143, 204, 0.32);
  font-size: 10px;
  font-weight: 700;
}

.chart-caption {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: #5f6f80;
}

.caption-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(180deg, #2ea2f8, #1273c5);
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.activity-block {
  display: flex;
  flex-direction: column;
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

.dashboard-table {
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

@media (max-width: 1100px) {
  .mix-zone {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 780px) {
  .dashboard-topbar,
  .section-header,
  .table-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .split-layout {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .shortcut-grid {
    grid-template-columns: 1fr;
  }
}
</style>



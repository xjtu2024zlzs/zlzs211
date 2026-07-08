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
      <section class="section-block project-one-section dossier-home-section" v-loading="projectOneLoading">
        <header class="dossier-band-header">
          <div class="dossier-band-copy">
            <p class="dossier-band-label">构建单台份数字卷宗</p>
            <h2 class="dossier-band-title">全域异构信息集成系统</h2>
            <p class="dossier-band-desc">
              围绕多源异构系统执行模式映射算法，实现异构信息集成、异构数据接入及单台份数据卷宗构建。
            </p>
          </div>

          <div class="dossier-summary-strip">
            <div class="dossier-summary-chip dossier-summary-chip--ok">
              <span>数据源</span>
              <strong>{{ projectOneSummary.status.datasourceConnectionText }}</strong>
            </div>
            <div class="dossier-summary-chip">
              <span>接入成功</span>
              <strong>{{ formatInteger(projectOneSummary.access.successAccessRecordTotal) }}</strong>
            </div>
            <div class="dossier-summary-chip">
              <span>目录节点</span>
              <strong>{{ formatInteger(projectOneSummary.dossier.directoryNodeCount) }}</strong>
            </div>
            <div class="dossier-summary-chip">
              <span>当前版本</span>
              <strong>{{ projectOneSummary.dossier.currentVersion }}</strong>
            </div>
          </div>
        </header>

        <div class="dossier-module-grid">
          <article class="dossier-module-card">
            <div class="dossier-card-head">
              <h3>异构信息集成</h3>
              <el-button size="small" plain @click="navigateTo('/dossier/integration/datasource')">
                模式映射
              </el-button>
            </div>
            <p class="dossier-card-desc">多源字段识别、语义匹配和结构关系汇聚。</p>
            <div class="dossier-info-box">
              <div class="dossier-info-title">
                <strong>{{ formatInteger(projectOneSummary.integration.fieldMappingResultTotal) }}</strong>
                <span>字段映射结果</span>
              </div>
              <div class="dossier-meta-grid">
                <div class="dossier-meta-item">
                  <span>异构数据源</span>
                  <strong>{{ formatInteger(projectOneSummary.integration.datasourceCount) }}</strong>
                </div>
                <div class="dossier-meta-item">
                  <span>模式映射任务</span>
                  <strong>{{ formatInteger(projectOneSummary.integration.matchTaskCount) }}</strong>
                </div>
                <div class="dossier-meta-item">
                  <span>F1分数</span>
                  <strong>{{ formatDecimal(projectOneSummary.integration.f1Average, 3) }}</strong>
                </div>
              </div>
            </div>
          </article>

          <article class="dossier-module-card">
            <div class="dossier-card-head">
              <h3>异构数据接入</h3>
              <el-button size="small" plain @click="navigateTo('/dossier/access/accessPlan')">
                计划执行
              </el-button>
            </div>
            <p class="dossier-card-desc">接入计划执行、结果落库和失败记录追踪。</p>
            <div class="dossier-info-box">
              <div class="dossier-info-title">
                <strong>{{ formatInteger(projectOneSummary.access.successAccessRecordTotal) }}</strong>
                <span>成功接入记录</span>
              </div>
              <div class="dossier-meta-grid">
                <div class="dossier-meta-item">
                  <span>启用计划</span>
                  <strong>{{ formatInteger(projectOneSummary.access.enabledPlanCount) }}</strong>
                </div>
                <div class="dossier-meta-item">
                  <span>失败记录</span>
                  <strong>{{ formatInteger(projectOneSummary.access.failedRecordTotal) }}</strong>
                </div>
                <div class="dossier-meta-item">
                  <span>成功率</span>
                  <strong>{{ formatPercent(projectOneSummary.access.successRate) }}</strong>
                </div>
              </div>
            </div>
          </article>

          <article class="dossier-module-card">
            <div class="dossier-card-head">
              <h3>单台份数字卷宗</h3>
              <el-button size="small" plain @click="navigateTo('/dossier/manage/instance')">
                查看卷宗
              </el-button>
            </div>
            <p class="dossier-card-desc">呈现整机卷宗、版本、目录和内容规模。</p>
            <div class="dossier-info-box">
              <div class="dossier-info-title">
                <strong>{{ projectOneSummary.dossier.aircraftLabel }}</strong>
                <span>当前版本 {{ projectOneSummary.dossier.currentVersion }}</span>
              </div>
              <div class="dossier-meta-grid">
                <div class="dossier-meta-item">
                  <span>卷宗实例</span>
                  <strong>{{ formatInteger(projectOneSummary.dossier.instanceCount) }}</strong>
                </div>
                <div class="dossier-meta-item">
                  <span>卷宗版本</span>
                  <strong>{{ formatInteger(projectOneSummary.dossier.versionCount) }}</strong>
                </div>
                <div class="dossier-meta-item">
                  <span>生成任务</span>
                  <strong>{{ formatInteger(projectOneSummary.dossier.generationTaskCount) }}</strong>
                </div>
              </div>
            </div>
          </article>

          <article class="dossier-module-card dossier-directory-card">
            <div class="dossier-directory-head">
              <h3>卷宗目录预览</h3>
              <div class="dossier-directory-actions">
                <span>
                  {{ formatInteger(projectOneSummary.dossier.directoryNodeCount) }} 个目录节点 /
                  {{ formatInteger(projectOneSummary.dossier.contentItemCount) }} 条内容
                </span>
                <el-button size="small" plain @click="navigateTo('/dossier/manage/detail')">
                  详情可视化
                </el-button>
              </div>
            </div>
            <div class="dossier-directory-grid">
              <section v-for="item in dossierDirectoryPreview" :key="item.no" class="dossier-directory-item">
                <div class="dossier-directory-row">
                  <span>{{ item.no }}</span>
                  <strong>{{ item.name }}</strong>
                </div>
                <p>{{ item.desc }}</p>
                <em>{{ item.tag }}</em>
              </section>
            </div>
          </article>
        </div>
      </section>

      <!-- 课题二三四 -->
      <section class="section-block">
        <div class="section-header">
          <div>
            <p class="section-label">Project Modules</p>


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

            <div v-if="project.stats" class="project-stats">
              <div class="project-stats__header">
                <span>{{ project.statsTitle }}</span>
                <strong>{{ project.statsValue }}</strong>
              </div>

              <div class="project-stats__grid">
                <div
                  v-for="item in project.stats"
                  :key="item.name"
                  class="project-stats__item"
                >
                  <span>{{ item.name }}</span>
                  <strong>
                    {{ item.value }}
                    <em v-if="item.unit">{{ item.unit }}</em>
                  </strong>
                </div>
              </div>
            </div>

            <div v-else class="mini-chart">
              <div class="mini-chart__header">
                <span>{{ project.chartTitle }}</span>
                <strong>{{ project.chartValue }}</strong>
              </div>

              <!-- <div v-if="project.chartType === 'faultPie'" class="fault-pie-panel">
                <div class="fault-pie" :style="buildPieStyle(project.faultDistribution)">
                  <div class="fault-pie__center">
                    <strong>{{ project.chartValue }}</strong>
                    <span>故障占比</span>
                  </div>
                </div>

                <div class="fault-pie-legend">
                  <div
                    v-for="item in project.faultDistribution"
                    :key="item.name"
                    class="fault-pie-legend__item"
                  >
                    <span class="fault-pie-legend__dot" :style="{ background: item.color }"></span>
                    <span class="fault-pie-legend__name">{{ item.name }}</span>
                    <strong>{{ item.value }}%</strong>
                  </div>
                </div>
              </div> -->
              <div v-if="project.chartType === 'faultPie'" class="fault-pie-panel">
                <div class="fault-pie" :style="buildPieStyle(project.faultDistribution)">
                  <div class="fault-pie__center">
                    <strong>{{ project.chartValue }}</strong>
                    <span>故障占比</span>
                  </div>
                </div>

                <div class="fault-pie-legend">
                  <div
                    v-for="item in project.faultDistribution"
                    :key="item.name"
                    class="fault-pie-legend__item"
                  >
                    <span class="fault-pie-legend__dot" :style="{ background: item.color }"></span>
                    <span class="fault-pie-legend__name">{{ item.name }}</span>
                    <strong>{{ formatOneDecimalPercent(item.value) }}</strong>
                  </div>
                </div>
              </div>

              <div v-else class="bar-chart">
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

            <el-button
              v-if="project.key === 'project-2'"
              type="primary"
              class="module-button"
              @click="navigateTo('/designtask/dashboard')"
            >
              进入{{ project.label }}平台
            </el-button>

            <el-button
              v-else-if="project.key === 'project-3'"
              type="primary"
              class="module-button"
              @click="navigateTo('/project_3/index')"
            >
              进入{{ project.label }}平台
            </el-button>

            <el-button
              v-else-if="project.key === 'project-4'"
              type="primary"
              class="module-button"
              @click="navigateTo('/project_4/project4')"
            >
              进入{{ project.label }}平台
            </el-button>
          </article>
        </div>
      </section>

      <!-- 课题五知识图谱 -->
      <section class="section-block knowledge-section">
        <div class="knowledge-left">
          <div class="section-header section-header--plain">
            <div>
              <p class="section-label">构建知识图谱溯源</p>
              <h2 class="section-title">全生命周期数字质量自反馈与追溯</h2>
              <p class="section-desc">
                围绕质量反馈事件，融合零部件、传感器、制造、装配、检测、运行和质量反馈等生命周期数据，
                构建质量知识图谱，实现候选根因推理、追溯链展示和报告生成。
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
            <el-button type="primary" @click="navigateTo('/topic_5/trace')">
              进入全生命周期数字质量自反馈与追溯平台
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
            <span class="summary-pill summary-pill--accent">质量问题记录</span>
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
            <div class="table-footer__hint">点击任意行查看质量问题详情</div>
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[5, 10, 15]"
              layout="total, sizes, prev, pager, next"
              :total="recentRecords.length"
              @update:current-page="currentPage = $event"
              @update:page-size="pageSize = $event"
            />
          </div>
        </div>
      </section>


    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="质量问题详情"
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

            <div class="detail-line-list">
              <div
                v-for="(line, index) in getDetailLines(selectedRecord.detail)"
                :key="index"
                class="detail-line-item"
              >
                {{ line }}
              </div>
            </div>
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
import { listProblem } from '@/api/quality/problem'
import { getDossierHomeSummary } from '@/api/project1/home'
import { listTask as listQualityTask } from '@/api/quality/task'

const router = useRouter()

// const buildPieStyle = (distribution = []) => {
//   const total = distribution.reduce((sum, item) => sum + Number(item.value || 0), 0)

//   if (!total) {
//     return {
//       background: '#dce6f5'
//     }
//   }

//   let current = 0
//   const segments = distribution.map((item) => {
//     const start = current
//     current += (Number(item.value || 0) / total) * 100
//     return `${item.color} ${start}% ${current}%`
//   })

//   return {
//     background: `conic-gradient(${segments.join(', ')})`
//   }
// }


// const randomInt = (min, max) => {
//   return Math.floor(Math.random() * (max - min + 1)) + min
// }

// const randomFloat = (min, max) => {
//   return Math.random() * (max - min) + min
// }

// const generateProject4RandomData = () => {
//   // 控制在合理演示范围内：故障占比 74%～86%，总文件数 150～180，根因完成数不超过故障文件数
//   const detectedFiles = randomInt(150, 180)
//   const normalRate = randomInt(14, 26)
//   const faultRate = 100 - normalRate

//   let innerFault = Math.round(faultRate * randomFloat(0.46, 0.56))
//   let outerFault = Math.round(faultRate * randomFloat(0.25, 0.34))
//   let ballFault = faultRate - innerFault - outerFault

//   // 防止个别随机情况下滚动体故障占比过低或过高
//   if (ballFault < 8) {
//     const diff = 8 - ballFault
//     innerFault -= diff
//     ballFault = 8
//   }
//   if (ballFault > 22) {
//     const diff = ballFault - 22
//     innerFault += diff
//     ballFault = 22
//   }

//   const faultFiles = Math.round(detectedFiles * faultRate / 100)
//   const minRcaFiles = Math.max(1, Math.round(faultFiles * 0.56))
//   const maxRcaFiles = Math.max(minRcaFiles, Math.round(faultFiles * 0.78))
//   const rcaFiles = randomInt(minRcaFiles, Math.min(faultFiles, maxRcaFiles))

//   return {
//     faultRate,
//     detectedFiles,
//     faultFiles,
//     rcaFiles,
//     distribution: [
//       { name: '内圈故障', value: innerFault, color: '#6b5cf6' },
//       { name: '外圈故障', value: outerFault, color: '#3f8cff' },
//       { name: '滚动体故障', value: ballFault, color: '#28c7a0' },
//       { name: '正常样本', value: normalRate, color: '#dce6f5' }
//     ]
//   }
// }

// const refreshProject4RandomData = () => {
//   const project4 = middleProjects.value.find((item) => item.key === 'project-4')
//   if (!project4) return

//   const randomData = generateProject4RandomData()

//   project4.chartValue = `${randomData.faultRate}.0%`
//   project4.faultDistribution = randomData.distribution
//   project4.chartData = randomData.distribution.map((item) => item.value)
//   project4.meta = [
//     { name: '已检测文件数', value: String(randomData.detectedFiles) },
//     { name: '故障文件数', value: String(randomData.faultFiles) },
//     { name: '已完成根因分析文件数', value: String(randomData.rcaFiles) }
//   ]
// }
const toOneDecimal = (value) => {
  return Math.round(Number(value || 0) * 10) / 10
}

const formatOneDecimal = (value) => {
  return Number(value || 0).toFixed(1)
}

const formatOneDecimalPercent = (value) => {
  return `${formatOneDecimal(value)}%`
}

const buildPieStyle = (distribution = []) => {
  const total = distribution.reduce((sum, item) => sum + Number(item.value || 0), 0)

  if (!total) {
    return {
      background: '#dce6f5'
    }
  }

  let current = 0
  const segments = distribution.map((item, index) => {
    const start = current
    const value = Number(item.value || 0)

    if (index === distribution.length - 1) {
      current = 100
    } else {
      current = toOneDecimal(current + (value / total) * 100)
    }

    return `${item.color} ${start}% ${current}%`
  })

  return {
    background: `conic-gradient(${segments.join(', ')})`
  }
}

const randomInt = (min, max) => {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

const randomFloat = (min, max) => {
  return Math.random() * (max - min) + min
}

const randomFloatOneDecimal = (min, max) => {
  return toOneDecimal(randomFloat(min, max))
}

const generateProject4RandomData = () => {
  // 控制在合理演示范围内：故障占比 74.0%～86.0%，总文件数 150～180，根因完成数不超过故障文件数
  const detectedFiles = randomInt(150, 180)

  const normalRate = randomFloatOneDecimal(14, 26)
  const faultRate = toOneDecimal(100 - normalRate)

  let innerFault = toOneDecimal(faultRate * randomFloat(0.46, 0.56))
  let outerFault = toOneDecimal(faultRate * randomFloat(0.25, 0.34))
  let ballFault = toOneDecimal(faultRate - innerFault - outerFault)

  // 防止个别随机情况下滚动体故障占比过低或过高
  if (ballFault < 8) {
    const diff = toOneDecimal(8 - ballFault)
    innerFault = toOneDecimal(innerFault - diff)
    ballFault = 8.0
  }

  if (ballFault > 22) {
    const diff = toOneDecimal(ballFault - 22)
    innerFault = toOneDecimal(innerFault + diff)
    ballFault = 22.0
  }

  // 修正小数四舍五入误差，保证三类故障之和等于 faultRate
  const faultSum = toOneDecimal(innerFault + outerFault + ballFault)
  const correction = toOneDecimal(faultRate - faultSum)
  innerFault = toOneDecimal(innerFault + correction)

  const faultFiles = Math.round(detectedFiles * faultRate / 100)
  const minRcaFiles = Math.max(1, Math.round(faultFiles * 0.56))
  const maxRcaFiles = Math.max(minRcaFiles, Math.round(faultFiles * 0.78))
  const rcaFiles = randomInt(minRcaFiles, Math.min(faultFiles, maxRcaFiles))

  return {
    faultRate,
    detectedFiles,
    faultFiles,
    rcaFiles,
    distribution: [
      { name: '内圈故障', value: innerFault, color: '#6b5cf6' },
      { name: '外圈故障', value: outerFault, color: '#3f8cff' },
      { name: '滚动体故障', value: ballFault, color: '#28c7a0' },
      { name: '正常样本', value: normalRate, color: '#dce6f5' }
    ]
  }
}

const refreshProject4RandomData = () => {
  const project4 = middleProjects.value.find((item) => item.key === 'project-4')
  if (!project4) {
    return
  }

  const randomData = generateProject4RandomData()

  project4.chartValue = formatOneDecimalPercent(randomData.faultRate)
  project4.faultDistribution = randomData.distribution
  project4.chartData = randomData.distribution.map((item) => item.value)
  project4.meta = [
    { name: '已检测文件数', value: String(randomData.detectedFiles) },
    { name: '故障文件数', value: String(randomData.faultFiles) },
    { name: '已完成根因分析文件数', value: String(randomData.rcaFiles) }
  ]
}

const projectOneLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(5)
const detailVisible = ref(false)
const selectedRecord = ref(null)

const createProjectOneSummary = () => ({
  integration: {
    fieldMappingResultTotal: 0,
    datasourceCount: 0,
    matchTaskCount: 0,
    f1Average: 0
  },
  access: {
    successAccessRecordTotal: 0,
    failedRecordTotal: 0,
    enabledPlanCount: 0,
    successRate: 0
  },
  dossier: {
    aircraftLabel: '-',
    currentVersion: '-',
    instanceCount: 0,
    versionCount: 0,
    generationTaskCount: 0,
    directoryNodeCount: 0,
    contentItemCount: 0
  },
  status: {
    datasourceConnectionText: '待检测'
  }
})

const projectOneSummary = ref(createProjectOneSummary())
const recentRecords = ref([])

const dossierDirectoryPreview = [
  { no: '01', name: '飞机基本信息', desc: '基本/交付记录', tag: '概要表' },
  { no: '02', name: '构型 / BOM', desc: '构型清单', tag: 'BOM' },
  { no: '03', name: '设计数据', desc: '图样规范', tag: '概要表' },
  { no: '04', name: '制造数据', desc: '工艺检验', tag: '时间线' },
  { no: '05', name: '服役数据', desc: '飞行维修', tag: '时间线' },
  { no: '06', name: '故障维修', desc: '故障闭环', tag: '维修单' },
  { no: '07', name: '技术状态', desc: '状态版本', tag: '概要表' },
  { no: '08', name: '附件材料', desc: '文件索引', tag: '文件清单' }
]

const knowledgeGraphRef = ref(null)
const knowledgeGraphChart = ref(null)
const selectedKgNode = ref(null)

const DESIGN_MODULE_CODE = 'PROJECT_2'
const DESIGN_TASK_VISIBLE_STATUSES = ['UNSTARTED', 'DISPATCHED', 'PROCESSING', 'PENDING_BACKFILL', 'SUBMITTED']
const DESIGN_TASK_FINISHED_STATUSES = ['PENDING_BACKFILL', 'SUBMITTED', 'CONFIRMED', 'FINISHED']

const middleProjects = ref([
  {
    key: 'project-2',
    label: '设计制造协同优化',
    title: '设计制造协同优化平台',
    icon: '二',
    iconClass: 'project-card__icon--blue',
    description: '承接质量问题任务，完成协同机制生成、目标约束确认、模型解耦求解、参数化建模、ANSYS 仿真验证与报告回填。',
    statsTitle: '任务运行统计',
    statsValue: '暂无任务',
    stats: [
      { name: '当前任务', value: '0', unit: '项' },
      { name: '处理中', value: '0', unit: '项' },
      { name: '待回填', value: '0', unit: '项' },
      { name: '平均时长', value: '-', unit: '' }
    ],
    route: '/designtask/dashboard',
    meta: [
      { name: '未开始', value: '0' },
      { name: '处理中', value: '0' },
      { name: '待确认', value: '0' }
    ]
  },
  {
    key: 'project-3',
    label: '质量监管与故障预防',
    title: '复杂产品生命周期质量监管与故障预防',
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
    label: '故障诊断与根源性分析',
    title: '航空轴承智能故障诊断与根因分析',
    icon: '四',
    iconClass: 'project-card__icon--purple',
    description: '面向航空轴承振动信号，统计不同故障类型识别结果及根因分析完成情况，支撑异常文件快速定位与质量追溯。',
    chartType: 'faultPie',
    chartTitle: '故障类型占比',
    chartValue: '80.0%',
    faultDistribution: [
      { name: '内圈故障', value: 42.3, color: '#6b5cf6' },
      { name: '外圈故障', value: 24.1,color: '#3f8cff' },
      { name: '滚动体故障', value: 14.2, color: '#28c7a0' },
      { name: '正常样本', value: 19.4, color: '#dce6f5' }
    ],
    chartLabels: ['内圈', '外圈', '滚动体', '正常'],
    chartData: [42, 24, 14, 20],
    route: '/project4/diagnose',
    meta: [
      { name: '已检测文件数', value: '161' },
      { name: '故障文件数', value: '129' },
      { name: '已完成根因分析文件数', value: '86' }
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
    desc: '用户填报的质量反馈事件，是智能追溯流程的入口。'
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
const getProblemStatusText = (status) => {
  const map = {
    CREATED: '已填报',
    DISPATCHING: '待分派',
    PROCESSING: '处理中',
    WAIT_CONFIRM: '待确认',
    FINISHED: '已结束'
  }
  return map[status] || status || '未知'
}

const getProblemStatusType = (status) => {
  const map = {
    CREATED: 'info',
    DISPATCHING: 'warning',
    PROCESSING: 'primary',
    WAIT_CONFIRM: 'warning',
    FINISHED: 'success'
  }
  return map[status] || 'info'
}

const getProblemModuleName = (item) => {
  return (
    item.currentModuleName ||
    item.currentModule ||
    item.moduleName ||
    item.involvedSystem ||
    '待分派'
  )
}

const getProblemTime = (item) => {
  return item.createTime || item.occurTime || item.updateTime || '-'
}

const buildProblemSummary = (item) => {
  const title = item.title || '未命名质量问题'
  const system = item.involvedSystem ? `｜${item.involvedSystem}` : ''
  const severity = item.severity ? `｜${item.severity}` : ''
  return `${title}${system}${severity}`
}

const buildProblemDetail = (item) => {
  const lines = [
    `问题标题：${item.title || '-'}`,
    `发生时间：${item.occurTime || '-'}`,
    `产品型号：${item.productModel || '-'}`,
    `涉及系统：${item.involvedSystem || '-'}`,
    `发生部位：${item.occurPart || '-'}`,
    `部件编号：${item.componentCode || '-'}`,
    `严重程度：${item.severity || '-'}`,
    `问题来源：${item.source || '-'}`,
    `问题描述：${item.description || '-'}`,
    `影响范围：${item.influenceScope || '-'}`
  ]

  return lines.join('\n')
}

const mapProblemToRecentRecord = (item) => {
  return {
    time: getProblemTime(item),
    problemCode: item.problemCode || '-',
    module: getProblemModuleName(item),
    summary: buildProblemSummary(item),
    detail: buildProblemDetail(item),
    status: getProblemStatusText(item.status),
    owner: item.reporter || item.createBy || '超级管理员',
    statusType: getProblemStatusType(item.status),
    raw: item
  }
}

const loadRecentQualityProblems = async () => {
  try {
    const res = await listProblem({})
    const rows = Array.isArray(res?.rows) ? res.rows : []

    recentRecords.value = rows
      .map((item) => mapProblemToRecentRecord(item))
      .sort((a, b) => {
        const at = a.time || ''
        const bt = b.time || ''
        return bt.localeCompare(at)
      })

    currentPage.value = 1
  } catch (error) {
    console.error('加载最近质量问题记录失败：', error)
    recentRecords.value = []
  }
}

const parseTaskTime = (value) => {
  const time = value ? new Date(value).getTime() : 0
  return Number.isFinite(time) ? time : 0
}

const formatTaskDuration = (duration) => {
  if (!duration) return '-'
  const minutes = Math.max(1, Math.round(duration / 60000))
  if (minutes < 60) return `${minutes}分钟`
  const hours = duration / 3600000
  if (hours < 24) return `${hours < 10 ? hours.toFixed(1) : Math.round(hours)}小时`
  const days = duration / 86400000
  return `${days < 10 ? days.toFixed(1) : Math.round(days)}天`
}

const getAverageTaskDuration = (tasks) => {
  const now = Date.now()
  const durations = tasks
    .filter((item) => !['UNSTARTED', 'DISPATCHED'].includes(item.taskStatus))
    .map((item) => {
      const start = parseTaskTime(item.dispatchTime || item.createTime)
      if (!start) return 0
      const finished = DESIGN_TASK_FINISHED_STATUSES.includes(item.taskStatus)
      const end = finished ? (parseTaskTime(item.updateTime) || now) : now
      return end > start ? end - start : 0
    })
    .filter((item) => item > 0)

  if (!durations.length) return '-'
  const total = durations.reduce((sum, item) => sum + item, 0)
  return formatTaskDuration(total / durations.length)
}

const updateDesignProjectStats = (stats) => {
  const index = middleProjects.value.findIndex((item) => item.key === 'project-2')
  if (index < 0) return

  const currentProject = middleProjects.value[index]
  middleProjects.value[index] = {
    ...currentProject,
    statsValue: stats.total ? `${stats.total} 项当前任务` : '暂无任务',
    stats: [
      { name: '当前任务', value: String(stats.total), unit: '项' },
      { name: '处理中', value: String(stats.processing), unit: '项' },
      { name: '待回填', value: String(stats.pendingBackfill), unit: '项' },
      { name: '平均时长', value: stats.averageDuration, unit: '' }
    ],
    meta: [
      { name: '未开始', value: String(stats.waiting) },
      { name: '处理中', value: String(stats.processing) },
      { name: '待确认', value: String(stats.submitted) }
    ]
  }
}

const loadDesignTaskStats = async () => {
  try {
    const res = await listQualityTask({
      moduleCode: DESIGN_MODULE_CODE,
      pageNum: 1,
      pageSize: 9999
    })
    const rows = Array.isArray(res?.rows) ? res.rows : []
    const designTasks = rows
      .filter((item) => item.moduleCode === DESIGN_MODULE_CODE)
      .filter((item) => DESIGN_TASK_VISIBLE_STATUSES.includes(item.taskStatus))

    updateDesignProjectStats({
      total: designTasks.length,
      waiting: designTasks.filter((item) => ['UNSTARTED', 'DISPATCHED'].includes(item.taskStatus)).length,
      processing: designTasks.filter((item) => item.taskStatus === 'PROCESSING').length,
      pendingBackfill: designTasks.filter((item) => item.taskStatus === 'PENDING_BACKFILL').length,
      submitted: designTasks.filter((item) => item.taskStatus === 'SUBMITTED').length,
      averageDuration: getAverageTaskDuration(designTasks)
    })
  } catch (error) {
    console.warn('加载设计制造协同优化平台任务统计失败：', error)
  }
}

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return recentRecords.value.slice(start, start + pageSize.value)
})

const navigateTo = (path) => {
  router.push(path)
}

const startSystem = () => {
  router.push('/quality')
}

const openRecordDetail = (row) => {
  selectedRecord.value = row
  detailVisible.value = true
}

const getDetailLines = (detail) => {
  if (!detail) {
    return ['暂无追溯说明']
  }

  return String(detail)
    .split(/\n|；|;/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
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
    desc: '用户填报的质量反馈事件，是智能追溯流程的入口。'
  }
}

const resizeKnowledgeGraph = () => {
  if (knowledgeGraphChart.value) {
    knowledgeGraphChart.value.resize()
  }
}

const normalizeProjectOneSummary = (data = {}) => {
  const defaults = createProjectOneSummary()
  return {
    integration: {
      ...defaults.integration,
      ...(data.integration || {})
    },
    access: {
      ...defaults.access,
      ...(data.access || {})
    },
    dossier: {
      ...defaults.dossier,
      ...(data.dossier || {})
    },
    status: {
      ...defaults.status,
      ...(data.status || {})
    }
  }
}

const formatInteger = (value) => {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toLocaleString('en-US') : '0'
}

const formatDecimal = (value, digits = 3) => {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(digits) : Number(0).toFixed(digits)
}

const formatPercent = (value) => `${formatDecimal(value, 2)}%`

const loadProjectOneSummary = async () => {
  projectOneLoading.value = true
  try {
    const res = await getDossierHomeSummary()
    projectOneSummary.value = normalizeProjectOneSummary(res?.data)
  } catch (error) {
    console.error('加载全域异构信息集成系统首页摘要失败：', error)
    projectOneSummary.value = createProjectOneSummary()
  } finally {
    projectOneLoading.value = false
  }
}

const loadHomeData = async () => {
  await Promise.all([
    loadProjectOneSummary(),
    loadRecentQualityProblems(),
    loadDesignTaskStats()
  ])
}

onMounted(async () => {
  refreshProject4RandomData()
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


.dossier-home-section {
  padding: 14px 14px 9px;
  overflow: hidden;
}

.dossier-band-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 78px;
  margin-bottom: 10px;
}

.dossier-band-copy {
  min-width: 0;
  flex: 1 1 auto;
}

.dossier-band-label {
  margin: 0 0 5px;
  color: #2364aa;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.dossier-band-title {
  margin: 0;
  color: #102742;
  font-size: 26px;
  line-height: 1.25;
  font-weight: 800;
}

.dossier-band-desc {
  max-width: 860px;
  margin: 7px 0 0;
  color: #647894;
  font-size: 13px;
  line-height: 1.35;
}

.dossier-summary-strip {
  flex: 0 0 630px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.dossier-summary-chip {
  min-width: 0;
  height: 58px;
  padding: 8px 10px;
  border: 1px solid #dcebf9;
  border-radius: 10px;
  background: #f8fbff;
}

.dossier-summary-chip span {
  display: block;
  color: #647894;
  font-size: 11px;
  line-height: 1.2;
  white-space: nowrap;
}

.dossier-summary-chip strong {
  display: block;
  margin-top: 6px;
  color: #10233f;
  font-size: 14px;
  line-height: 1.2;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dossier-summary-chip--ok strong {
  color: #18a76f;
}

.dossier-module-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(520px, 1.72fr);
  gap: 10px;
}

.dossier-module-card {
  position: relative;
  min-width: 0;
  height: 203px;
  padding: 11px;
  border: 1px solid #b8d5f4;
  border-radius: 12px;
  background: #ffffff;
  overflow: hidden;
}

.dossier-card-head,
.dossier-directory-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.dossier-card-head h3,
.dossier-directory-head h3 {
  margin: 0;
  color: #102742;
  font-size: 18px;
  line-height: 1.25;
  font-weight: 800;
}

.dossier-card-desc {
  margin: 0;
  color: #647894;
  font-size: 12px;
  line-height: 1.35;
}

.dossier-card-head :deep(.el-button),
.dossier-directory-actions :deep(.el-button) {
  height: 28px;
  padding: 0 12px;
  border-color: #a9d0ff;
  border-radius: 4px;
  color: #237de0;
  background: #ffffff;
  font-size: 12px;
  font-weight: 700;
}

.dossier-info-box {
  position: absolute;
  left: 11px;
  right: 11px;
  bottom: 10px;
  height: 104px;
  padding: 8px;
  border: 1px solid #dcebf9;
  border-radius: 10px;
  background: #f8fbff;
}

.dossier-info-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: 3px 0 12px;
}

.dossier-info-title strong {
  min-width: 0;
  color: #0b213b;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dossier-info-title span {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
  height: 24px;
  padding: 0 8px;
  border: 1px solid #c7ead5;
  border-radius: 999px;
  background: #eaf7ef;
  color: #18a76f;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.dossier-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.dossier-meta-item {
  min-width: 0;
  height: 43px;
  padding: 5px 7px;
  border: 1px solid #dcebf9;
  border-radius: 8px;
  background: #ffffff;
}

.dossier-meta-item span {
  display: block;
  color: #647894;
  font-size: 10px;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dossier-meta-item strong {
  display: block;
  margin-top: 3px;
  color: #0b213b;
  font-size: 13px;
  line-height: 1.2;
}

.dossier-directory-card {
  padding: 9px;
}

.dossier-directory-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dossier-directory-actions span {
  color: #0d6fd1;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.dossier-directory-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 5px;
}

.dossier-directory-item {
  min-width: 0;
  height: 70px;
  padding: 6px;
  border: 1px solid #dcebf9;
  border-radius: 9px;
  background: #f7fbff;
  overflow: hidden;
}

.dossier-directory-row {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
  margin-bottom: 2px;
}

.dossier-directory-row span {
  flex: 0 0 auto;
  color: #8ca4bf;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.dossier-directory-row strong {
  min-width: 0;
  color: #10233f;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dossier-directory-item p {
  margin: 0 0 2px;
  color: #647894;
  font-size: 11px;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dossier-directory-item em {
  display: inline-flex;
  align-items: center;
  height: 17px;
  padding: 0 6px;
  border: 1px solid #d0e5fa;
  border-radius: 4px;
  color: #3f86c6;
  background: #edf6ff;
  font-size: 11px;
  font-style: normal;
  white-space: nowrap;
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

.mini-chart--pie {
  min-height: 206px;
}

.fault-pie-panel {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  margin-top: 14px;
}

.fault-pie {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1px rgba(100, 128, 176, 0.08), 0 12px 24px rgba(82, 105, 152, 0.14);
}

.fault-pie::after {
  content: '';
  position: absolute;
  inset: 36px;
  border-radius: 50%;
  background: linear-gradient(180deg, #ffffff, #f4f8ff);
  box-shadow: inset 0 0 0 1px rgba(95, 139, 196, 0.16);
}

.fault-pie__center {
  position: absolute;
  inset: 43px;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.fault-pie__center strong {
  color: #162f55;
  font-size: 20px;
  line-height: 1.1;
}

.fault-pie__center span {
  margin-top: 4px;
  color: #70839d;
  font-size: 10px;
}

.fault-pie-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fault-pie-legend__item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(106, 148, 199, 0.18);
}

.fault-pie-legend__dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
}

.fault-pie-legend__name {
  color: #536982;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fault-pie-legend__item strong {
  color: #152d4d;
  font-size: 12px;
}

.project-stats {
  min-height: 172px;
  padding: 14px;
  box-sizing: border-box;
  border-radius: 18px;
  border: 1px solid rgba(104, 148, 197, 0.26);
  background: rgba(246, 250, 255, 0.96);
}

.project-stats__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #53677e;
  font-size: 12px;
}

.project-stats__header strong {
  color: #10395f;
  font-size: 18px;
}

.project-stats__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.project-stats__item {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 58px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(104, 148, 197, 0.18);
  background: rgba(255, 255, 255, 0.82);
}

.project-stats__item span {
  color: #728195;
  font-size: 11px;
}

.project-stats__item strong {
  display: block;
  margin-top: 8px;
  color: #17375b;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
}

.project-stats__item em {
  margin-left: 3px;
  color: #6d7e94;
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
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

.detail-line-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.detail-line-item {
  position: relative;
  padding-left: 12px;
  color: #597189;
  font-size: 13px;
  line-height: 1.65;
  word-break: break-all;
}

.detail-line-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #2674bc;
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

  .dossier-band-header,
  .table-footer,
  .bottom-start-section {
    align-items: flex-start;
    flex-direction: column;
  }

  .dossier-summary-strip,
  .dossier-module-grid,
  .dossier-directory-grid,
  .metric-grid,
  .kg-stat-grid {
    grid-template-columns: 1fr;
  }

  .dossier-module-card {
    height: auto;
    min-height: 210px;
  }

  .dossier-info-box {
    position: static;
    margin-top: 12px;
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

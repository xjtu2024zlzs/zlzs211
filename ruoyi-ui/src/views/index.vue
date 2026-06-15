<template>
  <div class="home-page">
    <!-- 顶部标题区 -->
    <section class="hero-card">
      <div>
        <div class="eyebrow">航空科技 · 质量追溯中心</div>
        <h1>智能质量运营仪表盘</h1>
        <p>
          面向关键工序、质量闭环与追溯活动的实时监控中心，支持专题切换、异常预警及追溯详情回溯。
        </p>
      </div>

      <div class="status-group">
        <span class="status-pill">
          <i class="dot green"></i>
          系统在线
        </span>
        <span class="status-pill">
          <i class="dot blue"></i>
          实时采集
        </span>
        <span class="status-pill">
          <i class="dot cyan"></i>
          追溯完备
        </span>
      </div>
    </section>

    <!-- 运营概览 -->
    <section class="panel-card">
      <div class="section-header">
        <div>
          <div class="eyebrow">关键指标</div>
          <h2>运营概览</h2>
        </div>
        <span class="mock-tag">模拟数据 · 2026</span>
      </div>

      <div class="metric-grid">
        <div class="metric-card">
          <div class="metric-top">
            <span>质量合格率</span>
            <span class="mini-icon">●</span>
          </div>
          <div class="metric-value">96.4%</div>
          <div class="metric-bottom">
            <span class="up">+2.8%</span>
            <span>连续达标</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span>追溯覆盖率</span>
            <span class="mini-icon">?</span>
          </div>
          <div class="metric-value">99.1%</div>
          <div class="metric-bottom">
            <span class="up">+1.4%</span>
            <span>全链路</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span>预警处置</span>
            <span class="mini-icon">?</span>
          </div>
          <div class="metric-value">24</div>
          <div class="metric-bottom">
            <span class="down">-11%</span>
            <span>待处置</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span>工艺稳定度</span>
            <span class="mini-icon">↗</span>
          </div>
          <div class="metric-value">91.6</div>
          <div class="metric-bottom">
            <span class="up">+4.3%</span>
            <span>评分</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 中部内容 -->
    <section class="middle-grid">
      <!-- 左侧质量趋势 -->
      <div class="panel-card trend-panel">
        <div class="section-header">
          <div>
            <div class="eyebrow">专题切换</div>
            <h2>质量趋势</h2>
          </div>
          <button class="small-pill" @click="goProject4">质量追溯</button>
        </div>

        <div class="trend-content">
          <div class="trend-menu">
            <div class="trend-item active" @click="goProject4">
              <div class="letter">Q</div>
              <div>
                <strong>质量追溯</strong>
                <span>检验指标与批次关联</span>
              </div>
              <em>→</em>
            </div>

            <div class="trend-item" @click="goProject4">
              <div class="letter">G</div>
              <div>
                <strong>工艺治理</strong>
                <span>工序偏差与治理</span>
              </div>
              <em>→</em>
            </div>

            <div class="trend-item" @click="goProject4">
              <div class="letter">F</div>
              <div>
                <strong>故障分析</strong>
                <span>异常模式识别</span>
              </div>
              <em>→</em>
            </div>

            <div class="trend-item" @click="goProject4">
              <div class="letter">E</div>
              <div>
                <strong>设备监测</strong>
                <span>设备健康状态</span>
              </div>
              <em>→</em>
            </div>

            <div class="trend-item" @click="goProject4">
              <div class="letter">D</div>
              <div>
                <strong>数据归档</strong>
                <span>归档、审计与留痕</span>
              </div>
              <em>→</em>
            </div>
          </div>

          <div class="chart-card">
            <div class="chart-title">
              <div>
                <div class="eyebrow">质量追溯链路</div>
                <h3>质量追溯趋势</h3>
              </div>
              <span class="running-dot">
                <i></i>
                追溯联动中
              </span>
            </div>

            <div class="fake-chart">
              <div class="chart-line line-main"></div>
              <div class="chart-line line-sub"></div>

              <div class="chart-grid">
                <span>100</span>
                <span>80</span>
                <span>60</span>
                <span>40</span>
                <span>20</span>
              </div>

              <div class="month-axis">
                <span>一月</span>
                <span>二月</span>
                <span>三月</span>
                <span>四月</span>
                <span>五月</span>
                <span>六月</span>
              </div>
            </div>

            <p class="chart-note">
              当前专题质量波动由设备状态、工序参数与检验结果联合驱动，已接入质量追溯模型。
            </p>
          </div>
        </div>
      </div>

      <!-- 右侧五大子课题 -->
      <div class="panel-card subject-panel">
        <div class="section-header">
          <div>
            <div class="eyebrow">快速入口</div>
            <h2>五大子课题</h2>
          </div>
        </div>

        <div class="subject-grid">
          <div class="subject-card" @click="goSubject('/project1')">
            <div class="subject-icon">一</div>
            <span class="subject-badge green">课题</span>
            <h3>课题一</h3>
            <p>进入课题一专题页面查看详情与运行看板。</p>
            <strong>专题 1 →</strong>
          </div>

          <div class="subject-card" @click="goSubject('/project2')">
            <div class="subject-icon">二</div>
            <span class="subject-badge blue">课题</span>
            <h3>课题二</h3>
            <p>进入课题二专题页面查看重点数据与状态。</p>
            <strong>专题 2 →</strong>
          </div>

          <div class="subject-card" @click="goSubject('/project3')">
            <div class="subject-icon">三</div>
            <span class="subject-badge red">课题</span>
            <h3>课题三</h3>
            <p>进入课题三专题页面查看异常与治理情况。</p>
            <strong>专题 3 →</strong>
          </div>

          <!-- 课题四：重点修改处，点击直接进入课题四数据文件管理 -->
          <div class="subject-card project4-card" @click="goProject4">
            <div class="subject-icon">四</div>
            <span class="subject-badge gray">课题</span>
            <h3>课题四</h3>
            <p>进入课题四专题页面查看质量追溯、样本增强、特征融合与诊断结果。</p>
            <strong>专题 4 →</strong>
          </div>

          <div class="subject-card" @click="goSubject('/project5')">
            <div class="subject-icon">五</div>
            <span class="subject-badge orange">课题</span>
            <h3>课题五</h3>
            <p>进入课题五专题页面查看归档与汇总结果。</p>
            <strong>专题 5 →</strong>
          </div>
        </div>
      </div>
    </section>

    <!-- 最近活动 -->
    <section class="panel-card activity-panel">
      <div class="section-header">
        <div>
          <div class="eyebrow">最新活动</div>
          <h2>最近追溯活动</h2>
        </div>

        <div class="activity-actions">
          <button>10 条记录</button>
          <button>分页浏览</button>
        </div>
      </div>

      <table class="activity-table">
        <thead>
        <tr>
          <th>时间</th>
          <th>项目</th>
          <th>追溯摘要</th>
          <th>状态</th>
          <th>责任人</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>2026-05-29 09:20</td>
          <td>航电系统</td>
          <td>A-12 批次抽检通过并完成闭环记录</td>
          <td><span class="success-tag">已完成</span></td>
          <td>李工</td>
        </tr>
        <tr>
          <td>2026-05-29 08:40</td>
          <td>轴承系统</td>
          <td>故障诊断结果已完成根因分析</td>
          <td><span class="success-tag">已完成</span></td>
          <td>张工</td>
        </tr>
        <tr>
          <td>2026-05-28 17:35</td>
          <td>质量追溯</td>
          <td>样本增强与特征融合结果已归档</td>
          <td><span class="warning-tag">待复核</span></td>
          <td>王工</td>
        </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup name="Index">
import { useRouter } from "vue-router"

const router = useRouter()

/**
 * 首页课题四快捷入口
 * 当前跳转到：课题四 -> 数据文件管理
 */
function goProject4() {
  router.push("/project4/fileofda")
}

/**
 * 其他课题入口。
 * 当前未迁移完整模块时，点击会先尝试跳转。
 */
function goSubject(path) {
  router.push(path)
}
</script>

<style scoped lang="scss">
.home-page {
  padding: 18px;
  background: #eef5fb;
  min-height: calc(100vh - 84px);
  color: #12213a;
}

.hero-card,
.panel-card {
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(38, 92, 145, 0.08);
}

.hero-card {
  padding: 28px 34px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;

  h1 {
    margin: 4px 0 8px;
    font-size: 32px;
    font-weight: 800;
    letter-spacing: 0.5px;
    color: #0c2b52;
  }

  p {
    margin: 0;
    color: #4b688c;
    font-size: 16px;
  }
}

.eyebrow {
  color: #1d7ed0;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.status-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid #c9def3;
  border-radius: 999px;
  background: #ffffff;
  color: #2f5f91;
  font-size: 14px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.green {
  background: #20c997;
}

.blue {
  background: #2d8cf0;
}

.cyan {
  background: #00a3c4;
}

.panel-card {
  padding: 20px 22px;
  margin-bottom: 18px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h2 {
    margin: 3px 0 0;
    font-size: 22px;
    color: #0c2b52;
  }
}

.mock-tag {
  padding: 5px 12px;
  border-radius: 999px;
  background: #e9fff5;
  color: #189b6b;
  font-weight: 700;
  font-size: 13px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 18px;
  border: 1px solid #bdd7f1;
  border-radius: 16px;
  background: #fbfdff;
}

.metric-top,
.metric-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-top {
  color: #6b7f99;
  font-size: 13px;
}

.mini-icon {
  background: #1976c9;
  color: #fff;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.metric-value {
  margin: 18px 0 8px;
  font-size: 32px;
  font-weight: 800;
  color: #0d1b2f;
}

.metric-bottom {
  font-size: 13px;
  color: #6b7f99;
}

.up {
  color: #11a36a;
  font-weight: 800;
}

.down {
  color: #ff8a00;
  font-weight: 800;
}

.middle-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 18px;
}

.trend-content {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 18px;
}

.trend-menu {
  background: #f1f6fb;
  border-radius: 14px;
  padding: 12px;
}

.trend-item {
  display: grid;
  grid-template-columns: 44px 1fr 20px;
  align-items: center;
  gap: 12px;
  padding: 14px;
  margin-bottom: 10px;
  border: 1px solid #d7e5f3;
  border-radius: 14px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #5fa7e8;
  }

  &.active {
    border: 2px solid #49a3ff;
    box-shadow: 0 8px 18px rgba(45, 140, 240, 0.12);
  }

  strong {
    display: block;
    color: #173b63;
    margin-bottom: 5px;
  }

  span {
    color: #7c8fa6;
    font-size: 12px;
  }

  em {
    color: #1976c9;
    font-style: normal;
  }
}

.letter,
.subject-icon {
  background: linear-gradient(135deg, #1a91f0, #0a63b7);
  color: #fff;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.letter {
  width: 40px;
  height: 40px;
}

.chart-card {
  border: 1px solid #cfe2f5;
  border-radius: 18px;
  padding: 18px;
  background: #fbfdff;
}

.chart-title {
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    margin: 2px 0 0;
    font-size: 20px;
  }
}

.running-dot {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eaf8f3;
  color: #158b65;
  font-size: 12px;

  i {
    width: 8px;
    height: 8px;
    background: #18c78a;
    border-radius: 50%;
  }
}

.fake-chart {
  position: relative;
  height: 270px;
  margin-top: 20px;
  border-left: 1px solid #b9d4ef;
  border-bottom: 1px solid #b9d4ef;
  background:
      linear-gradient(to bottom, transparent 19%, #d9e8f8 20%, transparent 21%),
      linear-gradient(to bottom, transparent 39%, #d9e8f8 40%, transparent 41%),
      linear-gradient(to bottom, transparent 59%, #d9e8f8 60%, transparent 61%),
      linear-gradient(to bottom, transparent 79%, #d9e8f8 80%, transparent 81%),
      linear-gradient(180deg, rgba(54, 179, 232, 0.08), rgba(54, 179, 232, 0.18));
}

.chart-line {
  position: absolute;
  left: 8%;
  right: 5%;
  height: 3px;
  border-radius: 999px;
  transform-origin: left center;
}

.line-main {
  top: 35%;
  background: #18aaf5;
  transform: rotate(-4deg);
}

.line-sub {
  top: 43%;
  background: #55d28d;
  transform: rotate(-5deg);
}

.chart-grid {
  position: absolute;
  left: -38px;
  top: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #7d93ad;
  font-size: 12px;
}

.month-axis {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -28px;
  display: flex;
  justify-content: space-around;
  color: #7d93ad;
  font-size: 12px;
}

.chart-note {
  margin-top: 42px;
  color: #5b7490;
  font-size: 13px;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.subject-card {
  position: relative;
  min-height: 150px;
  padding: 20px;
  border: 1px solid #c7ddf4;
  border-radius: 18px;
  background: #fbfdff;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    border-color: #2d8cf0;
    box-shadow: 0 14px 26px rgba(45, 140, 240, 0.16);
  }

  h3 {
    margin: 15px 0 8px;
    color: #0c2b52;
    font-size: 20px;
  }

  p {
    margin: 0 0 12px;
    color: #617897;
    line-height: 1.6;
    font-size: 14px;
  }

  strong {
    color: #1976c9;
    font-size: 14px;
  }
}

.project4-card {
  border-color: #90c2f5;
  background: linear-gradient(180deg, #ffffff 0%, #f0f8ff 100%);
}

.subject-icon {
  width: 38px;
  height: 38px;
}

.subject-badge {
  position: absolute;
  right: 16px;
  top: 18px;
  padding: 4px 10px;
  border-radius: 6px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;

  &.green {
    background: #67c23a;
  }

  &.blue {
    background: #409eff;
  }

  &.red {
    background: #f56c6c;
  }

  &.gray {
    background: #909399;
  }

  &.orange {
    background: #e6a23c;
  }
}

.small-pill {
  border: 1px solid #b9d6f3;
  background: #eaf5ff;
  color: #1976c9;
  border-radius: 999px;
  padding: 7px 14px;
  cursor: pointer;
  font-weight: 700;
}

.activity-panel {
  overflow: hidden;
}

.activity-actions {
  display: flex;
  gap: 10px;

  button {
    border: 1px solid #c7ddf4;
    background: #fff;
    color: #1976c9;
    border-radius: 999px;
    padding: 6px 12px;
    cursor: pointer;
  }
}

.activity-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;

  th,
  td {
    padding: 14px 16px;
    border-bottom: 1px solid #e7eef7;
    text-align: left;
    color: #324b67;
  }

  th {
    background: #f4f8fc;
    color: #25476c;
    font-weight: 800;
  }
}

.success-tag,
.warning-tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.success-tag {
  background: #ecf9f1;
  color: #19a66a;
}

.warning-tag {
  background: #fff7e6;
  color: #e69a00;
}

@media screen and (max-width: 1400px) {
  .middle-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 900px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 18px;
  }

  .trend-content {
    grid-template-columns: 1fr;
  }

  .subject-grid,
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
<template>
  <div class="subject-page">
    <div class="subject-page__shell">
      <div class="subject-page__hero">
        <div>
          <p class="subject-eyebrow">专题页面</p>
          <h1 class="subject-title">{{ subjectTitle }}</h1>
          <p class="subject-summary">
            该专题页面用于承接快捷入口跳转，支持独立展示当前课题的运行态、关键数据和追溯信息。
          </p>
        </div>
        <el-tag round size="large" class="subject-tag">{{ subjectCode }}</el-tag>
      </div>

      <div class="subject-grid">
        <el-card class="subject-card" shadow="hover">
          <template #header>
            <div class="card-header">专题概览</div>
          </template>
          <div class="stat-list">
            <div class="stat-item">
              <span class="stat-label">当前状态</span>
              <span class="stat-value">运行中</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">关联数据</span>
              <span class="stat-value">128 条</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">预警等级</span>
              <span class="stat-value">一般</span>
            </div>
          </div>
        </el-card>

        <el-card class="subject-card" shadow="hover">
          <template #header>
            <div class="card-header">重点动作</div>
          </template>
          <ul class="action-list">
            <li>质量闭环分析</li>
            <li>日常复核与校准</li>
            <li>异常告警处置</li>
            <li>阶段成果归档</li>
          </ul>
        </el-card>
      </div>

      <el-card class="subject-card subject-card--wide" shadow="hover">
        <template #header>
          <div class="card-header">专题数据看板</div>
        </template>
        <div class="mini-board">
          <div class="mini-board__item">
            <p class="mini-board__label">完成率</p>
            <p class="mini-board__value">96.8%</p>
          </div>
          <div class="mini-board__item">
            <p class="mini-board__label">累计工单</p>
            <p class="mini-board__value">286</p>
          </div>
          <div class="mini-board__item">
            <p class="mini-board__label">异常数</p>
            <p class="mini-board__value">9</p>
          </div>
          <div class="mini-board__item">
            <p class="mini-board__label">整改完成</p>
            <p class="mini-board__value">92%</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const subjectMap = {
  1: { title: '课题一', code: 'KT-001' },
  2: { title: '课题二', code: 'KT-002' },
  3: { title: '课题三', code: 'KT-003' },
  4: { title: '课题四', code: 'KT-004' },
  5: { title: '课题五', code: 'KT-005' }
}

const subjectKey = computed(() => route.params.id || '1')
const subjectInfo = computed(() => subjectMap[subjectKey.value] || subjectMap[1])
const subjectTitle = computed(() => subjectInfo.value.title)
const subjectCode = computed(() => subjectInfo.value.code)
</script>

<style scoped lang="scss">
.subject-page {
  min-height: calc(100vh - 24px);
  padding: 12px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at top, rgba(86, 136, 193, 0.24), transparent 24%),
    linear-gradient(180deg, #f7fbff 0%, #edf4fb 40%, #dfebf4 100%);
}

.subject-page__shell {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.subject-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 20px;
  border-radius: 24px;
  border: 1px solid rgba(107, 148, 197, 0.28);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 16px 38px rgba(76, 104, 142, 0.16);
}

.subject-eyebrow {
  margin: 0 0 6px;
  color: #2b6cb3;
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.22em;
}

.subject-title {
  margin: 0;
  color: #13284b;
  font-size: 30px;
  font-weight: 800;
}

.subject-summary {
  margin: 8px 0 0;
  max-width: 720px;
  color: #60728f;
  line-height: 1.5;
}

.subject-tag {
  background: linear-gradient(180deg, #2b8ee9, #0d60a4);
  color: #fff;
  border: 0;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.subject-card {
  border-radius: 20px;
  border: 1px solid rgba(97, 141, 197, 0.28);
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 12px 24px rgba(74, 105, 143, 0.12);
}

.subject-card--wide {
  width: 100%;
}

:deep(.el-card__header) {
  background: none;
  border-bottom: 1px solid rgba(95, 135, 186, 0.16);
}

.card-header {
  color: #16304f;
  font-size: 16px;
  font-weight: 700;
}

.stat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 13px;
  border-radius: 12px;
  background: #f5f9ff;
  border: 1px solid rgba(95, 140, 199, 0.24);
}

.stat-label {
  color: #64809f;
  font-size: 12px;
}

.stat-value {
  color: #0f2948;
  font-size: 14px;
  font-weight: 700;
}

.action-list {
  margin: 0;
  padding-left: 18px;
  color: #49617f;
  line-height: 1.8;
}

.mini-board {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.mini-board__item {
  padding: 12px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff, #ebf4fd);
  border: 1px solid rgba(86, 132, 184, 0.24);
  text-align: center;
}

.mini-board__label {
  margin: 0 0 8px;
  color: #6a7f97;
  font-size: 11px;
}

.mini-board__value {
  margin: 0;
  color: #0f2948;
  font-size: 20px;
  font-weight: 800;
}

@media (max-width: 780px) {
  .subject-grid {
    grid-template-columns: 1fr;
  }

  .mini-board {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

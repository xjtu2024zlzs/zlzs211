<template>
  <div class="project-page">
    <div class="project-page__header">
      <div>
        <p class="project-eyebrow">专题入口</p>
        <h1 class="project-title">课题二</h1>
        <p class="project-summary">课题二对应的独立页面，用于承接首页快捷入口跳转。</p>
      </div>
    </div>
    <el-card class="project-card" shadow="hover">
      <div class="project-content">
        <div class="stat-box">
          <span class="stat-label">当前状态</span>
          <span class="stat-value">监督中</span>
        </div>
        <div class="stat-box">
          <span class="stat-label">重点数据</span>
          <span class="stat-value">96 条</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.project-page {
  min-height: calc(100vh - 24px);
  padding: 16px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f3f9ff 0%, #e1edf8 100%);
}

.project-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255,255,255,0.78);
  border: 1px solid rgba(88, 136, 188, 0.26);
}

.project-eyebrow {
  margin: 0 0 6px;
  color: #2b6cb3;
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.project-title {
  margin: 0;
  color: #122f53;
  font-size: 28px;
}

.project-summary {
  margin: 8px 0 0;
  color: #60728f;
  line-height: 1.5;
}

.project-card {
  border-radius: 20px;
  border: 1px solid rgba(90, 140, 197, 0.26);
  background: rgba(255,255,255,0.84);
}

.project-content {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f6fbff;
  border: 1px solid rgba(95, 138, 193, 0.24);
}

.stat-label {
  color: #68809f;
  font-size: 12px;
}

.stat-value {
  color: #0f2948;
  font-size: 16px;
  font-weight: 700;
}
</style>

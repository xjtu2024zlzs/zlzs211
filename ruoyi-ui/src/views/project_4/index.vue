<template>
  <div class="project-page">
    <div class="project-page__header">
      <div>
        <p class="project-eyebrow">专题入口</p>
        <h1 class="project-title">课题四</h1>
        <p class="project-summary">课题四对应的独立页面，用于承接首页快捷入口跳转。</p>
      </div>
    </div>
    <el-card class="project-card" shadow="hover">
      <div class="project-content">
        <div class="stat-box">
          <span class="stat-label">当前状态</span>
          <span class="stat-value">在线监测</span>
        </div>
        <div class="stat-box">
          <span class="stat-label">重点数据</span>
          <span class="stat-value">43 台</span>
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
  background: linear-gradient(180deg, #f1fbff 0%, #dff2fb 100%);
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
  border: 1px solid rgba(84, 147, 182, 0.26);
}

.project-eyebrow {
  margin: 0 0 6px;
  color: #2284ba;
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.project-title {
  margin: 0;
  color: #0f3d5b;
  font-size: 28px;
}

.project-summary {
  margin: 8px 0 0;
  color: #54748f;
  line-height: 1.5;
}

.project-card {
  border-radius: 20px;
  border: 1px solid rgba(80, 143, 174, 0.28);
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
  background: #f6fcff;
  border: 1px solid rgba(78, 148, 181, 0.22);
}

.stat-label {
  color: #5f8192;
  font-size: 12px;
}

.stat-value {
  color: #0b3048;
  font-size: 16px;
  font-weight: 700;
}
</style>

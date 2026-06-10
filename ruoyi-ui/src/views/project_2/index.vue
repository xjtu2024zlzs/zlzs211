<template>
  <div class="app-container project-2-page">
    <el-card shadow="never" class="header-card">
      <div class="header-row">
        <div>
          <div class="title">课题二</div>
          <div class="desc">设计任务模块</div>
        </div>
        <div class="header-tip">通过左侧菜单的子节点进入“任务管理”或“流程发起”</div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="12">
        <el-card shadow="hover" class="menu-card" @click="$router.push('/project_2/designtask-manage')">
          <div class="menu-title">任务管理</div>
          <div class="menu-desc">查看任务列表、状态、详情与基础操作</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12">
        <el-card shadow="hover" class="menu-card" @click="$router.push('/project_2/designtask-create')">
          <div class="menu-title">流程发起</div>
          <div class="menu-desc">按步骤创建任务并选择流程模板</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.project-2-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.header-card {
  margin-bottom: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.desc {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.header-tip {
  color: #606266;
  font-size: 13px;
}

.menu-card {
  cursor: pointer;
  min-height: 140px;
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.menu-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}
</style>

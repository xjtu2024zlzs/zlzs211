<template>
  <div class="app-container task-manage-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <span>任务管理</span>
          <span class="tip">用于查看、筛选和管理设计任务</span>
        </div>
      </template>

      <el-form :model="query" inline class="query-form">
        <el-form-item label="任务名称">
          <el-input
              v-model="query.keyword"
              placeholder="请输入任务名称"
              clearable
              style="width: 220px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="RUNNING" />
            <el-option label="已完成" value="FINISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="filteredList" border stripe>
        <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
        <el-table-column label="任务类型" prop="taskTypeName" width="160" show-overflow-tooltip />
        <el-table-column label="流程模板" prop="templateName" width="180" show-overflow-tooltip />
        <el-table-column label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityTag(scope.row.priority)">{{ getPriorityLabel(scope.row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" prop="createBy" width="120" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="removeTask(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="任务详情" width="720px" append-to-body>
      <el-descriptions v-if="currentTask" :column="2" border>
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ getStatusLabel(currentTask.status) }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ currentTask.taskTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程模板">{{ currentTask.templateName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentTask.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ currentTask.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const detailVisible = ref(false)
const currentTask = ref(null)
const query = ref({ keyword: '', status: '' })

const taskList = ref([
  {
    taskName: '结构优化设计任务',
    taskTypeName: '结构设计',
    templateName: '标准流程A',
    priority: 3,
    status: 'RUNNING',
    createBy: 'admin',
    createTime: '2026-06-02 14:30:00',
    description: '对关键结构进行优化设计。'
  },
  {
    taskName: '布局评审任务',
    taskTypeName: '布局设计',
    templateName: '标准流程B',
    priority: 2,
    status: 'PENDING',
    createBy: 'user1',
    createTime: '2026-06-02 13:20:00',
    description: '检查布局合理性并进行评审。'
  },
  {
    taskName: '工艺校核任务',
    taskTypeName: '工艺设计',
    templateName: '标准流程C',
    priority: 1,
    status: 'FINISHED',
    createBy: 'user2',
    createTime: '2026-06-01 18:00:00',
    description: '完成工艺路线核查。'
  }
])

const filteredList = computed(() => {
  const keyword = (query.value.keyword || '').trim().toLowerCase()
  return taskList.value.filter(item => {
    const matchedKeyword =
        !keyword ||
        [item.taskName, item.taskTypeName, item.templateName].some(v => (v || '').toLowerCase().includes(keyword))
    const matchedStatus = !query.value.status || item.status === query.value.status
    return matchedKeyword && matchedStatus
  })
})

function handleQuery() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 200)
}

function resetQuery() {
  query.value.keyword = ''
  query.value.status = ''
}

function openDetail(row) {
  currentTask.value = row
  detailVisible.value = true
}

function openEdit(row) {
  ElMessage.info(`当前仅为页面预览，编辑任务：${row.taskName}`)
}

function removeTask(row) {
  ElMessageBox.confirm(`确认删除任务「${row.taskName}」吗？`, '提示', { type: 'warning' })
      .then(() => {
        taskList.value = taskList.value.filter(item => item !== row)
        ElMessage.success('删除成功')
      })
      .catch(() => {})
}

function getPriorityLabel(priority) {
  return { 1: '低', 2: '中', 3: '高' }[priority] || '中'
}

function getPriorityTag(priority) {
  return { 1: 'info', 2: 'warning', 3: 'danger' }[priority] || 'info'
}

function getStatusLabel(status) {
  return { PENDING: '待处理', RUNNING: '进行中', FINISHED: '已完成', CANCELLED: '已取消' }[status] || status || '-'
}

function getStatusTag(status) {
  return { PENDING: 'warning', RUNNING: 'primary', FINISHED: 'success', CANCELLED: 'info' }[status] || 'info'
}
</script>

<style scoped>
.task-manage-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-card {
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.tip {
  color: #909399;
  font-size: 13px;
}

.query-form {
  margin-bottom: 12px;
}
</style>
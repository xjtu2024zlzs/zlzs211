<template>
  <div class="app-container quality-page">
    <el-card shadow="never" class="quality-hero">
      <template #header>
        <span class="quality-title">生命周期质量监管与故障预防系统</span>
      </template>

      <div class="quality-desc">
        面向设计、制造、服役全过程的质量数据管理、异常监管、故障识别预测和周期性巡检。
      </div>
    </el-card>

    <el-row :gutter="16" class="quality-mb16">
      <el-col :span="4">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.aircraft_count) }}</div>
          <div class="quality-stat-label">飞机数量</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.subsystem_count) }}</div>
          <div class="quality-stat-label">分系统数量</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.device_count) }}</div>
          <div class="quality-stat-label">设备数量</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.component_count) }}</div>
          <div class="quality-stat-label">组件数量</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never">
          <div class="quality-stat-value">{{ displayValue(summary.part_count) }}</div>
          <div class="quality-stat-label">零件数量</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="go('/project_3/monitor')">
          <div class="module-title">生命周期质量统一监测</div>
          <div class="module-desc">
            进入模块级联管理、零件/组件信息管理、数据导入、数据预处理、关键质量特性挖掘页面。
          </div>
          <el-button type="primary" link>进入模块</el-button>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="go('/project_3/feedback/warning')">
          <div class="module-title">全域制造过程反馈监管</div>
          <div class="module-desc">
            进入关键工序异常信号预警、设备异常预警、制造过程预防性检测页面。
          </div>
          <el-button type="primary" link>进入模块</el-button>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="module-card" @click="go('/project_3/service/identify')">
          <div class="module-title">服役性能周期故障预防</div>
          <div class="module-desc">
            进入故障识别、故障预防、健康基准建立和周期性巡检页面。
          </div>
          <el-button type="primary" link>进入模块</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import '@/views/project_3/common.css'
import { reactive, onMounted } from 'vue'
import { getHomeOverview } from '@/api/project_3/home'

defineOptions({
  name: 'Home'
})

const router = useRouter()

const summary = reactive({
  aircraft_count: null,
  subsystem_count: null,
  device_count: null,
  component_count: null,
  part_count: null
})

onMounted(() => {
  getOverview()
})

function go(path) {
  router.push(path)
}

function displayValue(value) {
  return value === null || value === undefined ? '暂无数据' : value
}

function getOverview() {
  getHomeOverview().then(res => {
    const data = res.data || {}
    const overview = data.summary || {}

    summary.aircraft_count = overview.aircraft_count
    summary.subsystem_count = overview.subsystem_count
    summary.device_count = overview.device_count
    summary.component_count = overview.component_count
    summary.part_count = overview.part_count
  }).catch(() => {
    summary.aircraft_count = null
    summary.subsystem_count = null
    summary.device_count = null
    summary.component_count = null
    summary.part_count = null
  })
}


</script>

<style scoped>
.module-card {
  min-height: 210px;
  cursor: pointer;
  border-radius: 10px;
}

.module-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1f2d3d;
}

.module-desc {
  min-height: 104px;
  color: #607081;
  line-height: 1.8;
}
</style>

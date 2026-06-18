<template>
  <section class="section-block">
    <div class="section-header">
      <div>
        <p class="section-label">LIFE PREDICTION</p>
        <h2 class="section-title">裂纹扩展与剩余寿命</h2>
      </div>
      <div class="action-row">
        <el-tag :type="riskType">{{ prediction.riskLevel || prediction.status || 'NOT_SUBMITTED' }}</el-tag>
        <el-button plain :loading="loading" @click="load">刷新</el-button>
        <el-button type="primary" :loading="running" @click="run">调用 9822 代理模型</el-button>
      </div>
    </div>

    <el-alert
      v-if="prediction.status === 'FAILED'"
      class="mb-16"
      :title="prediction.errorMessage || '寿命预测失败'"
      type="error"
      :closable="false"
      show-icon
    />

    <div class="metric-grid">
      <div><span>剩余循环</span><strong>{{ prediction.predictedRemainingCycles ?? '-' }}</strong></div>
      <div><span>剩余飞行小时</span><strong>{{ prediction.predictedRemainingFlightHours ?? '-' }}</strong></div>
      <div><span>临界裂纹长度</span><strong>{{ prediction.criticalCrackLength ?? '-' }} mm</strong></div>
      <div><span>模型置信度</span><strong>{{ prediction.confidence ?? '-' }}</strong></div>
    </div>

    <el-table :data="prediction.growthCurve || []" stripe class="platform-table mt-12">
      <el-table-column label="循环数" prop="cycle" />
      <el-table-column label="裂纹长度 / mm" prop="crackLength" />
    </el-table>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getFrameBeamLifePrediction, runFrameBeamLifePrediction } from '@/api/designtask/optimization'

const props = defineProps({ taskId: { type: Number, required: true } })
const emit = defineEmits(['predicted'])
const loading = ref(false)
const running = ref(false)
const prediction = ref({ status: 'NOT_SUBMITTED', growthCurve: [] })

const riskType = computed(() => ({
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'danger',
  CRITICAL: 'danger',
  SUCCESS: 'success',
  FAILED: 'danger'
}[prediction.value.riskLevel || prediction.value.status] || 'info')

function load() {
  if (!props.taskId) return
  loading.value = true
  getFrameBeamLifePrediction(props.taskId).then(res => {
    prediction.value = res.data || prediction.value
  }).finally(() => {
    loading.value = false
  })
}

function run() {
  running.value = true
  runFrameBeamLifePrediction(props.taskId).then(res => {
    prediction.value = res.data || prediction.value
    ElMessage.success('寿命预测已完成')
    emit('predicted', prediction.value)
  }).finally(() => {
    running.value = false
  })
}

watch(() => props.taskId, load, { immediate: true })
</script>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.metric-grid > div {
  padding: 14px;
  border: 1px solid #e4edf7;
  border-radius: 8px;
  background: #fff;
}
.metric-grid span {
  display: block;
  color: #667085;
  font-size: 13px;
}
.metric-grid strong {
  display: block;
  margin-top: 6px;
  color: #1d2b44;
  font-size: 18px;
}
@media (max-width: 900px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>

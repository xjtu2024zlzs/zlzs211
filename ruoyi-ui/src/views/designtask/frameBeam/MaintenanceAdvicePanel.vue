<template>
  <section class="section-block">
    <div class="section-header">
      <div>
        <p class="section-label">MAINTENANCE ADVICE</p>
        <h2 class="section-title">维修建议与确认</h2>
      </div>
      <el-button type="success" :loading="confirming" @click="confirm">确认维修建议</el-button>
    </div>

    <el-form :model="form" label-width="140px">
      <el-form-item label="风险等级"><el-input v-model="form.riskLevel" /></el-form-item>
      <el-form-item label="建议类型"><el-input v-model="form.adviceType" /></el-form-item>
      <el-form-item label="建议措施"><el-input v-model="form.summary" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="复检周期"><el-input-number v-model="form.inspectionIntervalCycles" :min="0" /><span class="unit">cycles</span></el-form-item>
      <el-form-item label="允许继续服役"><el-switch v-model="form.continueServiceAllowed" /></el-form-item>
      <el-form-item label="需要限制载荷"><el-switch v-model="form.loadRestrictionRequired" /></el-form-item>
      <el-form-item label="需要专家评审"><el-switch v-model="form.expertReviewRequired" /></el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { confirmFrameBeamMaintenanceAdvice } from '@/api/designtask/optimization'

const props = defineProps({
  taskId: { type: Number, required: true },
  advice: { type: Object, default: () => ({}) }
})
const confirming = ref(false)
const form = reactive({
  riskLevel: '',
  adviceType: '',
  summary: '',
  inspectionIntervalCycles: 0,
  continueServiceAllowed: true,
  loadRestrictionRequired: false,
  expertReviewRequired: false
})

function syncAdvice() {
  Object.assign(form, props.advice || {})
}

function confirm() {
  confirming.value = true
  confirmFrameBeamMaintenanceAdvice(props.taskId, { ...form }).then(res => {
    Object.assign(form, res.data || {})
    ElMessage.success('维修建议已确认')
  }).finally(() => {
    confirming.value = false
  })
}

watch(() => props.advice, syncAdvice, { immediate: true, deep: true })
</script>

<style scoped>
.unit {
  margin-left: 8px;
  color: #667085;
}
</style>

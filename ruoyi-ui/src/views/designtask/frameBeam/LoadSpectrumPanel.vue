<template>
  <section class="section-block">
    <div class="section-header">
      <div>
        <p class="section-label">LOAD SPECTRUM</p>
        <h2 class="section-title">载荷谱与工况输入</h2>
      </div>
      <el-button type="primary" :loading="saving" @click="save">保存载荷谱</el-button>
    </div>

    <el-form :model="form" label-width="130px" class="frame-form">
      <el-form-item label="载荷谱名称"><el-input v-model="form.spectrumName" /></el-form-item>
      <el-form-item label="载荷谱等级">
        <el-select v-model="form.loadSpectrumLevel">
          <el-option label="常规服役" value="NORMAL" />
          <el-option label="高强度" value="HIGH" />
          <el-option label="恶劣环境" value="SEVERE" />
        </el-select>
      </el-form-item>
      <el-form-item label="最大应力"><el-input-number v-model="form.maxStress" :precision="2" /><span class="unit">MPa</span></el-form-item>
      <el-form-item label="最小应力"><el-input-number v-model="form.minStress" :precision="2" /><span class="unit">MPa</span></el-form-item>
      <el-form-item label="循环次数"><el-input-number v-model="form.cycles" :min="1" /></el-form-item>
      <el-form-item label="累计飞行小时"><el-input-number v-model="form.currentFlightHours" :min="0" /></el-form-item>
      <el-form-item label="文件名"><el-input v-model="form.fileName" placeholder="可记录 CSV / Excel 文件名" /></el-form-item>
      <el-form-item label="文件路径"><el-input v-model="form.filePath" placeholder="可记录上传后的文件路径" /></el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { saveFrameBeamLoadSpectrum } from '@/api/designtask/optimization'

const props = defineProps({ taskId: { type: Number, required: true } })
const emit = defineEmits(['saved'])
const saving = ref(false)
const form = reactive({
  spectrumName: '常规服役载荷谱',
  loadSpectrumLevel: 'NORMAL',
  maxStress: 120,
  minStress: 20,
  cycles: 10000,
  currentFlightHours: 1800,
  fileName: '',
  filePath: ''
})

function save() {
  saving.value = true
  saveFrameBeamLoadSpectrum(props.taskId, { ...form }).then(res => {
    Object.assign(form, res.data || {})
    ElMessage.success('载荷谱已保存')
    emit('saved', { ...form })
  }).finally(() => {
    saving.value = false
  })
}
</script>

<style scoped>
.frame-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}
.unit {
  margin-left: 8px;
  color: #667085;
}
@media (max-width: 900px) {
  .frame-form {
    grid-template-columns: 1fr;
  }
}
</style>

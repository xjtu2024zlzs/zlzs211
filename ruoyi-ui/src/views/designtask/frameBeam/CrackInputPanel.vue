<template>
  <section class="section-block">
    <div class="section-header">
      <div>
        <p class="section-label">FRAME BEAM INPUT</p>
        <h2 class="section-title">裂纹信息与结构参数</h2>
      </div>
      <el-button type="primary" :loading="saving" @click="save">保存裂纹信息</el-button>
    </div>

    <el-form :model="form" label-width="130px" class="frame-form">
      <el-form-item label="框梁编号"><el-input v-model="form.frameBeamCode" /></el-form-item>
      <el-form-item label="裂纹位置"><el-input v-model="form.crackLocation" placeholder="WEB / FLANGE / HOLE_EDGE" /></el-form-item>
      <el-form-item label="裂纹类型"><el-input v-model="form.crackType" placeholder="SURFACE / THROUGH / HOLE_EDGE" /></el-form-item>
      <el-form-item label="初始裂纹长度"><el-input-number v-model="form.initialCrackLength" :min="0" :precision="2" /><span class="unit">mm</span></el-form-item>
      <el-form-item label="初始裂纹深度"><el-input-number v-model="form.initialCrackDepth" :min="0" :precision="2" /><span class="unit">mm</span></el-form-item>
      <el-form-item label="临界裂纹长度"><el-input-number v-model="form.criticalCrackLength" :min="0" :precision="2" /><span class="unit">mm</span></el-form-item>
      <el-form-item label="材料牌号"><el-input v-model="form.material" placeholder="AL7075" /></el-form-item>
      <el-form-item label="结构厚度"><el-input-number v-model="form.thickness" :min="0" :precision="2" /><span class="unit">mm</span></el-form-item>
      <el-form-item label="检测置信度"><el-slider v-model="form.inspectionConfidence" :min="0" :max="1" :step="0.01" /></el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getFrameBeamCrackInput, saveFrameBeamCrackInput } from '@/api/designtask/optimization'

const props = defineProps({ taskId: { type: Number, required: true } })
const emit = defineEmits(['saved'])
const saving = ref(false)
const form = reactive({
  frameBeamCode: '',
  crackLocation: 'WEB',
  crackType: 'SURFACE',
  initialCrackLength: 3.2,
  initialCrackDepth: 0.8,
  criticalCrackLength: 12,
  material: 'AL7075',
  thickness: 6,
  inspectionConfidence: 0.85
})

function load() {
  if (!props.taskId) return
  getFrameBeamCrackInput(props.taskId).then(res => {
    Object.assign(form, res.data || {})
  })
}

function save() {
  saving.value = true
  saveFrameBeamCrackInput(props.taskId, { ...form }).then(res => {
    Object.assign(form, res.data || {})
    ElMessage.success('裂纹信息已保存')
    emit('saved', { ...form })
  }).finally(() => {
    saving.value = false
  })
}

watch(() => props.taskId, load, { immediate: true })
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

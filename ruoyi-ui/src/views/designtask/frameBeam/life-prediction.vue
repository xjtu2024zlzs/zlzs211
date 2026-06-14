<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <p class="platform-eyebrow">FRAME BEAM</p>
          <h1 class="platform-title">寿命预测评估</h1>
          <p class="platform-subtitle">框梁裂纹任务专用页面，仅处理 FRAME_BEAM_CRACK_LIFE_PREDICTION。</p>
        </div>
        <el-button plain @click="goDashboard">返回看板</el-button>
      </section>
      <CrackInputPanel v-if="taskId" :task-id="taskId" />
      <LoadSpectrumPanel v-if="taskId" :task-id="taskId" />
      <LifePredictionPanel v-if="taskId" :task-id="taskId" />
      <el-empty v-else description="请从任务看板带 taskId 进入寿命预测评估" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CrackInputPanel from './CrackInputPanel.vue'
import LoadSpectrumPanel from './LoadSpectrumPanel.vue'
import LifePredictionPanel from './LifePredictionPanel.vue'
import '../platform-theme.scss'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => route.query.taskId ? Number(route.query.taskId) : null)

function goDashboard() {
  router.push({ path: '/designtask/dashboard' })
}
</script>

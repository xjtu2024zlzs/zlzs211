<template>
  <div class="design-platform-view">
    <div class="design-platform-shell">
      <section class="platform-topbar">
        <div>
          <h1 class="platform-title">决策建议确认</h1>
        </div>
        <el-button plain @click="goDashboard">返回看板</el-button>
      </section>
      <LifePredictionPanel v-if="taskId" :task-id="taskId" @predicted="setPrediction" />
      <MaintenanceAdvicePanel v-if="taskId" :task-id="taskId" :advice="advice" />
      <el-empty v-else description="未选择任务" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import LifePredictionPanel from './LifePredictionPanel.vue'
import MaintenanceAdvicePanel from './MaintenanceAdvicePanel.vue'
import '../platform-theme.scss'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => route.query.taskId ? Number(route.query.taskId) : null)
const advice = ref({})

function setPrediction(prediction) {
  advice.value = prediction?.maintenanceAdvice || {}
}

function goDashboard() {
  router.push({ path: '/designtask/dashboard' })
}
</script>

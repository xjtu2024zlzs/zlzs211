<template>
  <div ref="chartRef" class="project1-chart" :style="{ height }"></div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue"
import * as echarts from "echarts"

const props = defineProps({
  option: {
    type: Object,
    default: () => ({})
  },
  height: {
    type: String,
    default: "280px"
  }
})

const chartRef = ref(null)
let chartInstance = null

function renderChart() {
  if (!chartRef.value) {
    return
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(props.option || {}, true)
  chartInstance.resize()
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(() => renderChart())
  window.addEventListener("resize", handleResize)
})

watch(
  () => props.option,
  () => nextTick(() => renderChart()),
  { deep: true }
)

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.project1-chart {
  width: 100%;
}
</style>

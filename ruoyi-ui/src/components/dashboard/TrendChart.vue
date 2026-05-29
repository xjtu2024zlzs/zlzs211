<template>
  <div ref="chartRef" class="trend-chart"></div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  xAxisData: {
    type: Array,
    default: () => []
  },
  seriesData: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref(null)
let chartInstance = null

const renderChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        lineStyle: {
          color: '#35c9ff',
          width: 1
        }
      }
    },
    legend: {
      top: 6,
      textStyle: {
        color: '#9cb7d5',
        fontSize: 11
      },
      itemWidth: 10,
      itemHeight: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '16%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#244c74'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#8aa3bb',
        fontSize: 11
      },
      data: props.xAxisData
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(36, 76, 116, 0.55)'
        }
      },
      axisLabel: {
        color: '#8aa3bb',
        fontSize: 11
      }
    },
    series: props.seriesData.map((item) => ({
      name: item.name,
      type: 'line',
      smooth: true,
      symbolSize: 6,
      lineStyle: {
        width: 3,
        color: item.color
      },
      itemStyle: {
        color: item.color
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: item.color + '55' },
          { offset: 1, color: item.color + '10' }
        ])
      },
      data: item.data
    }))
  }

  chartInstance.setOption(option, true)
  chartInstance.resize()
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(() => renderChart())
  window.addEventListener('resize', handleResize)
})

watch(
  () => [props.xAxisData, props.seriesData],
  () => {
    nextTick(() => renderChart())
  },
  { deep: true }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.trend-chart {
  width: 100%;
  height: 320px;
  min-height: 280px;
}
</style>

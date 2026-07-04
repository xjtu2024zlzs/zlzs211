export function finiteNumber(value, fallback = null) {
  if (value === null || value === undefined || value === '') return fallback
  const number = Number(value)
  return Number.isFinite(number) ? number : fallback
}

export function getKqcPayload(response) {
  return response?.data || response?.payload || response || {}
}

export function getKqcResult(data) {
  const result = data?.result && typeof data.result === 'object' ? data.result : data
  return result && typeof result === 'object' ? result : {}
}

export function formatChartValue(value) {
  const number = finiteNumber(value)
  if (number === null) return '--'
  return Math.abs(number) >= 100 ? number.toFixed(1) : number.toFixed(4)
}

export function algorithmNumberText(value, digits = 3) {
  const number = finiteNumber(value)
  return number === null ? '--' : number.toFixed(digits)
}

export function formatKqcMetric(value, precision) {
  const number = finiteNumber(value)
  return number === null ? '--' : number.toFixed(precision)
}

export function buildAnomalyChartOption(rows = [], threshold = null) {
  const safeRows = Array.isArray(rows)
    ? rows.map((row, index) => ({
      x: row?.x ?? index + 1,
      value: finiteNumber(row?.value, 0)
    }))
    : []
  const safeThreshold = finiteNumber(threshold)

  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, top: 28, bottom: 36 },
    xAxis: { type: 'category', data: safeRows.map(item => item.x) },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      name: 'anomaly score',
      data: safeRows.map(item => item.value),
      smooth: true,
      showSymbol: false,
      markLine: safeThreshold === null ? undefined : {
        symbol: 'none',
        data: [{ yAxis: safeThreshold, name: 'threshold' }],
        lineStyle: { color: '#f56c6c', type: 'dashed' },
        label: { formatter: 'threshold' }
      }
    }]
  }
}

export function buildKqcRankingChartOption(rows = []) {
  const safeRows = Array.isArray(rows)
    ? rows
      .map(row => ({
        source_variable: String(row?.source_variable || row?.name || '--'),
        station: row?.station || '--',
        weight: finiteNumber(row?.weight, 0),
        frequency: finiteNumber(row?.frequency, null)
      }))
      .slice(0, 15)
      .reverse()
    : []

  if (!safeRows.length) {
    return {
      title: { text: 'No ranking data', left: 'center', top: 'center', textStyle: { color: '#8a97a8', fontSize: 14, fontWeight: 400 } },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }
  }

  return {
    grid: { left: 110, right: 28, top: 24, bottom: 42 },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        const point = params?.[0]
        const row = safeRows[point?.dataIndex]
        if (!row) return ''
        return `${row.source_variable}<br/>station: ${row.station || '--'}<br/>weight: ${formatKqcMetric(row.weight, 6)}<br/>frequency: ${formatKqcMetric(row.frequency, 3)}`
      }
    },
    xAxis: {
      type: 'value',
      name: 'weight',
      nameLocation: 'middle',
      nameGap: 28,
      axisLabel: { formatter: value => Number(value).toExponential(1) },
      splitLine: { lineStyle: { color: '#edf1f5' } }
    },
    yAxis: {
      type: 'category',
      data: safeRows.map(row => row.source_variable),
      axisLabel: { width: 96, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      barMaxWidth: 22,
      data: safeRows.map(row => ({
        value: row.weight,
        itemStyle: { color: row.weight >= 0 ? '#e56a67' : '#4c8fdc' }
      })),
      label: {
        show: true,
        position: 'right',
        formatter: params => Number(params.value).toExponential(2),
        color: '#526273',
        fontSize: 10
      }
    }]
  }
}

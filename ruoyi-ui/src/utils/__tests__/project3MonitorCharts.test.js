import { describe, expect, it } from 'vitest'
import {
  algorithmNumberText,
  buildAnomalyChartOption,
  buildKqcRankingChartOption,
  formatChartValue,
  formatKqcMetric,
  getKqcPayload,
  getKqcResult
} from '../project3MonitorCharts'

describe('project3 monitor chart utilities', () => {
  it('normalizes wrapped task payloads and result objects', () => {
    expect(getKqcPayload({ data: { taskId: 'T1' } })).toEqual({ taskId: 'T1' })
    expect(getKqcPayload({ payload: { taskId: 'T2' } })).toEqual({ taskId: 'T2' })
    expect(getKqcResult({ result: { score: 1 } })).toEqual({ score: 1 })
    expect(getKqcResult(null)).toEqual({})
  })

  it('formats invalid chart and algorithm numbers as placeholders', () => {
    expect(formatChartValue(Number.NaN)).toBe('--')
    expect(formatChartValue(Number.POSITIVE_INFINITY)).toBe('--')
    expect(formatChartValue(120)).toBe('120.0')
    expect(algorithmNumberText(undefined, 2)).toBe('--')
    expect(formatKqcMetric('0.1234567', 3)).toBe('0.123')
  })

  it('builds anomaly options without NaN or Infinity values', () => {
    const option = buildAnomalyChartOption([
      { x: 'A', value: 1.25 },
      { x: 'B', value: Number.NaN },
      { value: Number.POSITIVE_INFINITY }
    ], Number.POSITIVE_INFINITY)

    expect(option.xAxis.data).toEqual(['A', 'B', 3])
    expect(option.series[0].data).toEqual([1.25, 0, 0])
    expect(option.series[0].markLine).toBeUndefined()
  })

  it('builds empty and populated KQC ranking options safely', () => {
    const empty = buildKqcRankingChartOption([])
    expect(empty.series).toEqual([])
    expect(empty.title.text).toBe('No ranking data')

    const option = buildKqcRankingChartOption([
      { source_variable: 'a', station: 'S1', weight: Number.NaN, frequency: Number.POSITIVE_INFINITY },
      { source_variable: 'b', station: 'S2', weight: -0.2, frequency: 0.8 }
    ])

    expect(option.yAxis.data).toEqual(['b', 'a'])
    expect(option.series[0].data.map(item => item.value)).toEqual([-0.2, 0])
    expect(option.tooltip.formatter([{ dataIndex: 1 }])).toContain('frequency: --')
  })
})

import { describe, expect, it } from 'vitest'
import {
  buildFaultIdentifyTaskPayload,
  buildFrameBeamUploadMeta,
  buildPreventiveMaintenancePayload,
  finitePairs,
  finiteValues,
  validateUploadFile,
  validateUploadFiles
} from '../project3Validation'

describe('finite chart data', () => {
  it('filters invalid values and accepts numeric strings', () => {
    expect(finiteValues([1, '2.5', NaN, Infinity, -Infinity, null, undefined])).toEqual([1, 2.5])
    expect(finiteValues([])).toEqual([])
    expect(finitePairs([1, '2', 3], [4, Infinity, undefined])).toEqual([[1, 4]])
  })
})

describe('upload validation', () => {
  const options = {
    allowedExtensions: ['csv', 'txt'],
    allowedMimeTypes: ['text/csv', 'text/plain'],
    maxSize: 1024
  }

  it('rejects missing, empty, oversized and invalid files', () => {
    expect(validateUploadFile(null, options).reason).toBe('EMPTY_FILE')
    expect(validateUploadFile({ name: 'a.csv', size: 0, type: 'text/csv' }, options).reason).toBe('EMPTY_FILE')
    expect(validateUploadFile({ name: 'a.csv', size: 2048, type: 'text/csv' }, options).reason).toBe('FILE_TOO_LARGE')
    expect(validateUploadFile({ name: 'a.exe', size: 10, type: 'text/plain' }, options).reason).toBe('INVALID_EXTENSION')
    expect(validateUploadFile({ name: 'a.csv', size: 10, type: 'application/pdf' }, options).reason).toBe('MIME_MISMATCH')
  })

  it('reports partial multi-file failures', () => {
    const results = validateUploadFiles([
      { name: 'a.csv', size: 10, type: 'text/csv' },
      { name: 'b.exe', size: 10, type: 'text/plain' }
    ], options)
    expect(results.map(item => item.valid)).toEqual([true, false])
  })
})

describe('API payload builders', () => {
  it('normalizes frame beam upload metadata', () => {
    expect(buildFrameBeamUploadMeta({
      uploadBatchId: 'B1',
      fileIndex: '2',
      totalFiles: '3',
      relativePath: 'dir/a.csv'
    })).toEqual({
      uploadBatchId: 'B1',
      fileIndex: 2,
      totalFiles: 3,
      relativePath: 'dir/a.csv'
    })
  })

  it('normalizes preventive maintenance and fault identify payloads', () => {
    expect(buildPreventiveMaintenancePayload({ T1: '90', N1: '2', taskNo: 123 }))
      .toEqual({ T1: 90, N1: 2, taskNo: '123' })
    expect(buildFaultIdentifyTaskPayload({ taskId: 1, taskType: 'FEATURE', params: { window: 10 } }))
      .toEqual({ taskId: '1', taskType: 'FEATURE', sourceTaskId: '', params: { window: 10 } })
  })
})

export {
  validateUploadFile,
  validateUploadFiles
} from './project3UploadValidation'

export function finiteValues(values) {
  return (Array.isArray(values) ? values : [])
    .filter(value => value !== null && value !== undefined && value !== '')
    .map(value => Number(value))
    .filter(value => Number.isFinite(value))
}

export function finitePairs(left, right) {
  if (!Array.isArray(left) || !Array.isArray(right)) return []
  return left
    .filter((value, index) =>
      value !== null && value !== undefined && value !== ''
      && right[index] !== null && right[index] !== undefined && right[index] !== ''
    )
    .map((value, index) => [Number(value), Number(right[index])])
    .filter(([x, y]) => Number.isFinite(x) && Number.isFinite(y))
}

export function buildFrameBeamUploadMeta(input = {}) {
  return {
    uploadBatchId: String(input.uploadBatchId || ''),
    fileIndex: Number(input.fileIndex || 0),
    totalFiles: Number(input.totalFiles || 0),
    relativePath: String(input.relativePath || '')
  }
}

export function buildPreventiveMaintenancePayload(input = {}) {
  const keys = ['T1', 'T2', 'T3', 'N1', 'N2', 'N3', 'sampleCount', 'population', 'iterations', 'Rm', 'attackThreshold']
  const payload = {}
  keys.forEach(key => {
    if (input[key] !== undefined) payload[key] = Number(input[key])
  })
  ;['equipmentId', 'partInstanceId', 'processExecutionId', 'taskNo'].forEach(key => {
    if (input[key] !== undefined) payload[key] = String(input[key])
  })
  return payload
}

export function buildFaultIdentifyTaskPayload(input = {}) {
  return {
    taskId: String(input.taskId || ''),
    taskType: String(input.taskType || ''),
    sourceTaskId: String(input.sourceTaskId || ''),
    params: input.params && typeof input.params === 'object' ? { ...input.params } : {}
  }
}

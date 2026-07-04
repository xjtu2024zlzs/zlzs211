import request from '@/utils/request'

export function listFaultIdentifyResults(query) {
  return request({
    url: '/service/identify/results',
    method: 'get',
    params: query
  })
}

export function listKeyProcessResults(query) {
  return request({
    url: '/service/identify/key_process_results',
    method: 'get',
    params: query
  })
}

export function deleteFaultIdentifyResult(taskId, options = {}) {
  return request({
    url: `/service/identify/results/${encodeURIComponent(taskId)}`,
    method: 'delete',
    data: options
  })
}

export function startFeatureTask({ recordId, params } = {}) {
  return request({
    url: '/service/identify/feature_tasks',
    method: 'post',
    data: {
      import_record_id: recordId,
      sourceTaskId: params?.sourceTaskId,
      source_task_id: params?.sourceTaskId,
      dataSelectionMode: params?.dataSelectionMode,
      sampleIds: params?.sampleIds,
      feature_params: params
    }
  })
}

export function startDataAnalysisTask({ recordId, params } = {}) {
  return request({
    url: '/service/identify/start_analysis',
    method: 'post',
    data: {
      import_record_id: recordId,
      dataSelectionMode: params?.dataSelectionMode,
      sampleIds: params?.sampleIds,
      feature_params: params
    }
  })
}

export function getFeatureTask(taskId) {
  return request({
    url: '/service/identify/task_status',
    method: 'get',
    params: {
      task_id: taskId
    }
  })
}

export function getTimeDomainWindow(params) {
  return request({
    url: '/service/identify/time_domain_window',
    method: 'get',
    params
  })
}

export function getTimeDomainOverview(params) {
  return request({
    url: '/service/identify/time_domain_overview',
    method: 'get',
    params
  })
}

export function getTimeDomainGlobalRawPreview(params) {
  return request({
    url: '/service/identify/time_domain_global_raw_preview',
    method: 'get',
    params
  })
}

export function listFaultIdenConditions() {
  return request({
    url: '/quality/fault-iden/conditions',
    method: 'get'
  })
}

export function listFaultIdenBearings(query) {
  return request({
    url: '/quality/fault-iden/bearings',
    method: 'get',
    params: query
  })
}

export function listFaultIdenSamples(query) {
  return request({
    url: '/quality/fault-iden/samples',
    method: 'get',
    params: query
  })
}

export function uploadNumericFile(data, onUploadProgress) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-file',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

export function uploadNumericChunk(data, onUploadProgress) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-chunk',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

export function uploadNumericApi(data) {
  return request({
    url: '/quality/fault-iden/catalog/upload-numeric-api',
    method: 'post',
    data,
    timeout: 300000
  })
}

export function validateNumericTaskName(data) {
  return request({
    url: '/quality/fault-iden/catalog/validate-task-name',
    method: 'post',
    data
  })
}

export function mergeNumericChunks(data) {
  return request({
    url: '/quality/fault-iden/catalog/merge-numeric-chunks',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

export function listFaultIdenSampleTasks(query) {
  return request({
    url: '/quality/fault-iden/sample-tasks',
    method: 'get',
    params: query
  })
}

export function updateFaultIdenSampleDataUsage(sampleId, dataUsage) {
  return request({
    url: `/quality/fault-iden/samples/${sampleId}/data-usage`,
    method: 'put',
    data: { dataUsage }
  })
}

export function deleteFaultIdenSample(sampleId) {
  return request({
    url: `/quality/fault-iden/samples/${sampleId}`,
    method: 'delete'
  })
}

export function startKeyProcessTask(data) {
  return request({
    url: '/service/identify/key_process_tasks',
    method: 'post',
    data
  })
}

export function getKeyProcessTask(taskId) {
  return request({
    url: `/service/identify/key_process_tasks/${encodeURIComponent(taskId)}/status`,
    method: 'get'
  })
}

export function cancelKeyProcessTask(taskId) {
  return request({
    url: `/service/identify/key_process_tasks/${encodeURIComponent(taskId)}/cancel`,
    method: 'post'
  })
}

export function startDegradationTask(data) {
  return request({
    url: '/service/identify/degradation_tasks',
    method: 'post',
    data
  })
}

export function startPreTask(data) {
  return request({
    url: '/service/predict/tasks',
    method: 'post',
    data
  })
}

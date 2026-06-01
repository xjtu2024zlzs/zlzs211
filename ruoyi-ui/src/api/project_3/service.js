import request from '@/utils/request'

export function listFaultIdentifyResults(query) {
  return request({
    url: '/service/identify/results',
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

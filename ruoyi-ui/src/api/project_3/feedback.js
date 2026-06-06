import request from '@/utils/request'

export function listWarningParts(query) {
  return request({
    url: '/feedback/warning/parts',
    method: 'get',
    params: query
  })
}

export function listWarningPartProcesses(partId) {
  return request({
    url: `/feedback/warning/parts/${partId}/processes`,
    method: 'get'
  })
}

export function uploadWarningDetectFile(data, onUploadProgress) {
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

export function listWarningPartInstances(partTemplateId, query) {
  return request({
    url: `/feedback/warning/parts/${partTemplateId}/instances`,
    method: 'get',
    params: query
  })
}

export function createWarningProcessExecution(data) {
  return request({
    url: '/feedback/warning/process-executions',
    method: 'post',
    data
  })
}

export function updateWarningProcessExecution(processExecId, data) {
  return request({
    url: `/feedback/warning/process-executions/${encodeURIComponent(processExecId)}`,
    method: 'put',
    data
  })
}

export function deleteWarningProcessExecution(processExecId) {
  return request({
    url: `/feedback/warning/process-executions/${encodeURIComponent(processExecId)}`,
    method: 'delete'
  })
}

export function uploadWarningPartImage(partId, data) {
  return request({
    url: `/feedback/warning/parts/${partId}/image`,
    method: 'post',
    headers: { repeatSubmit: false },
    data
  })
}

export function deleteWarningPartImage(partId) {
  return request({
    url: `/feedback/warning/parts/${partId}/image`,
    method: 'delete'
  })
}

export function listWarningDevices(query) {
  return request({
    url: '/feedback/warning/devices',
    method: 'get',
    params: query
  })
}

export function listWarningDeviceParts(deviceId) {
  return request({
    url: `/feedback/warning/devices/${deviceId}/parts`,
    method: 'get'
  })
}

export function startWarningDetectTask(data) {
  return request({
    url: '/feedback/warning/detect/tasks',
    method: 'post',
    data
  })
}

export function getWarningDetectTask(taskId) {
  return request({
    url: `/feedback/warning/detect/tasks/${taskId}/status`,
    method: 'get'
  })
}

export function cancelWarningDetectTask(taskId) {
  return request({
    url: `/feedback/warning/detect/tasks/${taskId}/cancel`,
    method: 'post'
  })
}

export function listWarningDetectResults(query) {
  return request({
    url: '/feedback/warning/detect/results',
    method: 'get',
    params: query
  })
}

export function deleteWarningAlgorithmResult(taskId) {
  return request({
    url: `/feedback/warning/results/${encodeURIComponent(taskId)}`,
    method: 'delete'
  })
}

export function startKqcMiningTask(data) {
  return request({
    url: '/feedback/warning/kqc-mining',
    method: 'post',
    data
  })
}

export function getKqcMiningTask(taskId) {
  return request({
    url: `/feedback/warning/kqc-mining/tasks/${encodeURIComponent(taskId)}/status`,
    method: 'get'
  })
}

export function cancelKqcMiningTask(taskId) {
  return request({
    url: `/feedback/warning/kqc-mining/tasks/${encodeURIComponent(taskId)}/cancel`,
    method: 'post'
  })
}

export function listKqcMiningResults(query) {
  return request({
    url: '/feedback/warning/kqc-mining/results',
    method: 'get',
    params: query
  })
}

export function listWarningDetectLogs(taskId) {
  return request({
    url: `/feedback/warning/detect/tasks/${taskId}/logs`,
    method: 'get'
  })
}

export function startWarningKeyProcessTask(data) {
  return request({
    url: '/service/identify/key_process_tasks',
    method: 'post',
    data
  })
}

export function getWarningKeyProcessTask(taskId) {
  return request({
    url: `/service/identify/key_process_tasks/${taskId}/status`,
    method: 'get'
  })
}

export function cancelWarningKeyProcessTask(taskId) {
  return request({
    url: `/service/identify/key_process_tasks/${taskId}/cancel`,
    method: 'post'
  })
}

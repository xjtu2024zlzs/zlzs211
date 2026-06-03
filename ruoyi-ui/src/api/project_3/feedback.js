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

export function listWarningPartInstances(partTemplateId, query) {
  return request({
    url: `/feedback/warning/parts/${partTemplateId}/instances`,
    method: 'get',
    params: query
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

export function listWarningDetectResults(query) {
  return request({
    url: '/feedback/warning/detect/results',
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

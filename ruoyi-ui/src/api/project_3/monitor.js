import request from '@/utils/request'

// 级联树
export function getMonitorTree(query) {
  return request({
    url: '/monitor/tree',
    method: 'get',
    params: query
  })
}

// 零件列表
export function listMonitorParts(query) {
  return request({
    url: '/monitor/parts',
    method: 'get',
    params: query
  })
}

export function getPartQualityTables(partInstanceId) {
  return request({
    url: `/monitor/parts/${encodeURIComponent(partInstanceId)}/quality`,
    method: 'get'
  })
}

export function createMonitorModule(data) {
  return request({
    url: '/monitor/module',
    method: 'post',
    data
  })
}

export function deleteMonitorModule(nodeId) {
  return request({
    url: `/monitor/module/${encodeURIComponent(nodeId)}`,
    method: 'delete'
  })
}

export function updateMonitorModuleName(nodeId, data) {
  return request({
    url: `/monitor/module/${encodeURIComponent(nodeId)}/name`,
    method: 'put',
    data
  })
}

export function createMonitorPart(data) {
  return request({
    url: '/monitor/parts',
    method: 'post',
    data
  })
}

export function updateMonitorPart(partInstanceId, data) {
  return request({
    url: `/monitor/parts/${encodeURIComponent(partInstanceId)}`,
    method: 'put',
    data
  })
}

export function deleteMonitorPart(partInstanceId) {
  return request({
    url: `/monitor/parts/${encodeURIComponent(partInstanceId)}`,
    method: 'delete'
  })
}

export function createMonitorPartTemplate(data) {
  return request({
    url: '/monitor/part-template',
    method: 'post',
    data
  })
}

export function getMonitorPartTemplate(partTemplateId) {
  return request({
    url: `/monitor/part-template/${encodeURIComponent(partTemplateId)}`,
    method: 'get'
  })
}

export function updateMonitorPartTemplate(partTemplateId, data) {
  return request({
    url: `/monitor/part-template/${encodeURIComponent(partTemplateId)}`,
    method: 'put',
    data
  })
}

export function downloadPartProcessTemplate(params) {
  return request({
    url: '/monitor/text/process/template',
    method: 'get',
    params,
    responseType: 'blob',
    timeout: 300000
  })
}

export function downloadHierarchyTemplate() {
  return request({
    url: '/monitor/text/hierarchy/template',
    method: 'get',
    responseType: 'blob',
    timeout: 300000
  })
}

export function importHierarchyData(data) {
  return request({
    url: '/monitor/text/hierarchy/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

export function importProcessTextData(data) {
  return request({
    url: '/monitor/text/process/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

export function importProcessTextApiData(data) {
  return request({
    url: '/monitor/text/api/import',
    method: 'post',
    data,
    timeout: 300000
  })
}

export function getMonitorNodeView(nodeId) {
  return request({
    url: `/monitor/node/${encodeURIComponent(nodeId)}/view`,
    method: 'get'
  })
}

export function getLifecycleRows(url, params = {}) {
  return request({
    url,
    method: 'get',
    params
  })
}

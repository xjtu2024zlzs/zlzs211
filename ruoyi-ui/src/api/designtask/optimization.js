import request from '@/utils/request'

export function getDashboard(query) {
  return request({
    url: '/designtask/dashboard',
    method: 'get',
    params: query
  })
}

export function listProcessDefinitions() {
  return request({
    url: '/designtask/process/definitions',
    method: 'get'
  })
}

export function deployDefaultProcess() {
  return request({
    url: '/designtask/process/deploy-default',
    method: 'post'
  })
}

export function listProcessDefinitionNodes(processDefinitionId) {
  return request({
    url: `/designtask/process/definition/${encodeURIComponent(processDefinitionId)}/nodes`,
    method: 'get'
  })
}

export function listAssigneeOptions() {
  return request({
    url: '/designtask/assignee-options',
    method: 'get'
  })
}

export function startDesignTask(data) {
  return request({
    url: '/designtask/task/start',
    method: 'post',
    data
  })
}

export function uploadDesignTaskFile(file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: '/designtask/task/upload-attachment',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    }
  })
}

export function getDesignTask(taskId) {
  return request({
    url: `/designtask/task/detail/${taskId}`,
    method: 'get'
  })
}

export function getTaskAttachmentFile(fileId) {
  return request({
    url: `/designtask/task/attachment/${fileId}`,
    method: 'get',
    responseType: 'arraybuffer'
  })
}

export function getObjectiveCatalog(discipline) {
  return request({
    url: `/designtask/objective/catalog/${discipline}`,
    method: 'get'
  })
}

export function getDesignVariableCatalog(discipline) {
  return request({
    url: `/designtask/design-variable/catalog/${discipline}`,
    method: 'get'
  })
}

export function saveObjectiveConstraints(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/objective-constraints`,
    method: 'post',
    data
  })
}

export function saveDesignVariables(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/design-variables`,
    method: 'post',
    data
  })
}

export function runConflictCheck(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/conflict-check`,
    method: 'post',
    data
  })
}

export function decomposeTask(taskId) {
  return request({
    url: `/designtask/task/${taskId}/decompose`,
    method: 'post'
  })
}

export function solveTask(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/solve`,
    method: 'post',
    data
  })
}

export function submitSurrogateSolveTask(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/surrogate-solve`,
    method: 'post',
    data
  })
}

export function getSurrogateSolveTask(taskId) {
  return request({
    url: `/designtask/task/${taskId}/surrogate-solve`,
    method: 'get'
  })
}

export function confirmSurrogateSolveTask(taskId) {
  return request({
    url: `/designtask/task/${taskId}/surrogate-solve/confirm`,
    method: 'post'
  })
}

export function runSimulation(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/simulation`,
    method: 'post',
    data
  })
}

export function submitCadModelTask(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/cad-model`,
    method: 'post',
    data
  })
}

export function getCadModelTask(taskId) {
  return request({
    url: `/designtask/task/${taskId}/cad-model`,
    method: 'get'
  })
}

export function getCadModelFile(taskId, kind) {
  return request({
    url: `/designtask/task/${taskId}/cad-model/file/${kind}`,
    method: 'get',
    responseType: 'arraybuffer'
  })
}

export function approveTask(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/approve`,
    method: 'post',
    data
  })
}

export function listDesignResource(query) {
  return request({
    url: '/designtask/resource/list',
    method: 'get',
    params: query
  })
}

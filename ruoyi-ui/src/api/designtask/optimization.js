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

export function getDesignTaskByQualityTask(qualityTaskId) {
  return request({
    url: `/designtask/task/quality-task/${qualityTaskId}`,
    method: 'get'
  })
}

export function getDesignTaskArchive(taskId) {
  return request({
    url: `/designtask/task/${taskId}/archive`,
    method: 'get'
  })
}

export function deleteArchivedTaskData(taskId) {
  return request({
    url: `/designtask/task/${taskId}/archive-data`,
    method: 'delete'
  })
}

export function getTaskAttachmentFile(fileId) {
  return request({
    url: `/designtask/task/attachment/${fileId}`,
    method: 'get',
    responseType: 'arraybuffer'
  })
}

export function getObjectiveCatalog(discipline, params = {}) {
  return request({
    url: `/designtask/objective/catalog/${discipline}`,
    method: 'get',
    params
  })
}

export function getDesignVariableCatalog(discipline) {
  return request({
    url: `/designtask/design-variable/catalog/${discipline}`,
    method: 'get'
  })
}

export function getDefaultFaultPipeParameters() {
  return request({
    url: '/designtask/fault-pipe-parameters/default',
    method: 'get'
  })
}

export function listFaultPipeParameterOptions() {
  return request({
    url: '/designtask/fault-pipe-parameters/options',
    method: 'get'
  })
}

export function getTaskFaultPipeParameters(taskId) {
  return request({
    url: `/designtask/task/${taskId}/fault-pipe-parameters`,
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

export function saveObjectiveWeights(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/objective-weights`,
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

export function getSurrogateModels() {
  return request({
    url: '/designtask/surrogate-models',
    method: 'get'
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

export function submitDesignReportTask(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/design-report`,
    method: 'post',
    data,
    headers: {
      repeatSubmit: false
    }
  })
}

export function getDesignReportTask(taskId) {
  return request({
    url: `/designtask/task/${taskId}/design-report`,
    method: 'get'
  })
}

export function submitAnsysSimulationTask(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation`,
    method: 'post',
    data
  })
}

export function saveAnsysSimulationParams(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation/params`,
    method: 'post',
    data
  })
}

export function openAnsysSimulationTask(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation/open`,
    method: 'post',
    data
  })
}

export function importAnsysSimulationResult(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation/import-result`,
    method: 'post',
    data
  })
}

export function importAnsysResultFile(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation/import-result-file`,
    method: 'post',
    data
  })
}

export function getAnsysSimulationTask(taskId, params = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation`,
    method: 'get',
    params
  })
}

export function getAnsysSimulationImage(taskId, params = {}) {
  return request({
    url: `/designtask/task/${taskId}/ansys-simulation/image`,
    method: 'get',
    params,
    responseType: 'arraybuffer'
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

export function getFrameBeamCrackInput(taskId) {
  return request({
    url: `/designtask/task/${taskId}/frame-beam-crack`,
    method: 'get'
  })
}

export function saveFrameBeamCrackInput(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/frame-beam-crack`,
    method: 'post',
    data
  })
}

export function saveFrameBeamLoadSpectrum(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/frame-beam-load-spectrum`,
    method: 'post',
    data
  })
}

export function getFrameBeamLifePrediction(taskId) {
  return request({
    url: `/designtask/task/${taskId}/frame-beam-life-prediction`,
    method: 'get'
  })
}

export function runFrameBeamLifePrediction(taskId) {
  return request({
    url: `/designtask/task/${taskId}/frame-beam-life-prediction`,
    method: 'post'
  })
}

export function confirmFrameBeamMaintenanceAdvice(taskId, data = {}) {
  return request({
    url: `/designtask/task/${taskId}/frame-beam-maintenance-advice/confirm`,
    method: 'post',
    data
  })
}

export function submitStandaloneFatiguePredict(data) {
  return request({
    url: '/designtask/fatigue-predict',
    method: 'post',
    data
  })
}

export function submitFatiguePredictBatch(taskId, data) {
  return request({
    url: `/designtask/task/${taskId}/fatigue-predict/batch`,
    method: 'post',
    data
  })
}

export function submitStandaloneFatiguePredictBatch(data) {
  return request({
    url: '/designtask/fatigue-predict/batch',
    method: 'post',
    data
  })
}

export function importFatiguePredictExcel(taskId, formData) {
  return request({
    url: `/designtask/task/${taskId}/fatigue-predict/import`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function importStandaloneFatiguePredictExcel(formData) {
  return request({
    url: '/designtask/fatigue-predict/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getFatiguePredict(taskId) {
  return request({
    url: `/designtask/task/${taskId}/fatigue-predict`,
    method: 'get'
  })
}

export function confirmFatiguePredict(taskId) {
  return request({
    url: `/designtask/task/${taskId}/fatigue-predict/confirm`,
    method: 'post'
  })
}

export function listDesignResource(query) {
  return request({
    url: '/designtask/resource/list',
    method: 'get',
    params: query
  })
}

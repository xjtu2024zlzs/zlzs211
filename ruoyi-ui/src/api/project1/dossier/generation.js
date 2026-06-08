import request from '@/utils/request'

export function listGenerationModels() {
  return request({
    url: '/project1/dossier/generate/model/list',
    method: 'get'
  })
}

export function listGenerationAircraft(query) {
  return request({
    url: '/project1/dossier/generate/aircraft/list',
    method: 'get',
    params: query
  })
}

export function getActiveGenerationTemplate() {
  return request({
    url: '/project1/dossier/generate/template/active',
    method: 'get'
  })
}

export function prepareGeneration(aircraftId) {
  return request({
    url: '/project1/dossier/generate/prepare/' + aircraftId,
    method: 'get'
  })
}

export function runGenerationPrecheck(data) {
  return request({
    url: '/project1/dossier/generate/precheck',
    method: 'post',
    data: data
  })
}

export function startGeneration(data) {
  return request({
    url: '/project1/dossier/generate/start',
    method: 'post',
    data: data
  })
}

export function getGenerationJob(jobId) {
  return request({
    url: '/project1/dossier/generate/job/' + jobId,
    method: 'get'
  })
}

export function getGenerationJobLogs(jobId) {
  return request({
    url: '/project1/dossier/generate/job/' + jobId + '/logs',
    method: 'get'
  })
}

export function getGenerationJobOutput(jobId) {
  return request({
    url: '/project1/dossier/generate/job/' + jobId + '/output',
    method: 'get'
  })
}

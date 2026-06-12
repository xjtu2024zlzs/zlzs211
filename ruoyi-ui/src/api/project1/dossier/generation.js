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

export function listGenerationTemplates() {
  return request({
    url: '/project1/dossier/generate/template/list',
    method: 'get'
  })
}

export function prepareGeneration(aircraftId, query) {
  return request({
    url: '/project1/dossier/generate/prepare/' + aircraftId,
    method: 'get',
    params: query
  })
}

export function runGenerationPrecheck(data) {
  return request({
    url: '/project1/dossier/generate/precheck',
    method: 'post',
    data: data,
    timeout: 60000
  })
}

export function startGeneration(data) {
  return request({
    url: '/project1/dossier/generate/start',
    method: 'post',
    data: data,
    timeout: 120000
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
    method: 'get',
    timeout: 60000
  })
}

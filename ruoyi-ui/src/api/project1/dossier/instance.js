import request from '@/utils/request'

export function listDossierInstances(query) {
  return request({
    url: '/project1/dossier/instance/list',
    method: 'get',
    params: query
  })
}

export function getDossierInstanceSummary(query) {
  return request({
    url: '/project1/dossier/instance/summary',
    method: 'get',
    params: query
  })
}

export function getDossierInstance(instanceId) {
  return request({
    url: '/project1/dossier/instance/' + instanceId,
    method: 'get'
  })
}

export function listDossierInstanceVersions(instanceId) {
  return request({
    url: '/project1/dossier/instance/' + instanceId + '/versions',
    method: 'get'
  })
}

export function listDossierInstanceJobs(instanceId) {
  return request({
    url: '/project1/dossier/instance/' + instanceId + '/jobs',
    method: 'get'
  })
}

export function listDossierInstanceFiles(instanceId, query) {
  return request({
    url: '/project1/dossier/instance/' + instanceId + '/files',
    method: 'get',
    params: query
  })
}

export function publishDossierInstance(instanceId) {
  return request({
    url: '/project1/dossier/instance/' + instanceId + '/publish',
    method: 'post'
  })
}

export function archiveDossierInstance(instanceId) {
  return request({
    url: '/project1/dossier/instance/' + instanceId + '/archive',
    method: 'post'
  })
}

export function exportDossierPdf(instanceId, query) {
  return exportDossier(instanceId, 'pdf', query)
}

export function exportDossierZip(instanceId, query) {
  return exportDossier(instanceId, 'zip', query)
}

export function exportDossier(instanceId, format, query) {
  return request({
    url: '/project1/dossier/instance/' + instanceId + '/export/' + format,
    method: 'get',
    params: query,
    responseType: 'blob',
    timeout: 60000
  })
}

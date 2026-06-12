import request from '@/utils/request'

export function getCurrentDossierDetail(query) {
  return request({
    url: '/project1/dossier/detail/current',
    method: 'get',
    params: query
  })
}

export function getDossierNodeDetail(query) {
  return request({
    url: '/project1/dossier/detail/node',
    method: 'get',
    params: query
  })
}

export function listBomChildren(query) {
  return request({
    url: '/project1/dossier/detail/bom/children',
    method: 'get',
    params: query
  })
}

export function searchBomNodes(query) {
  return request({
    url: '/project1/dossier/detail/bom/search',
    method: 'get',
    params: query
  })
}

export function getBomPath(nodeId, query) {
  return request({
    url: '/project1/dossier/detail/bom/path/' + nodeId,
    method: 'get',
    params: query
  })
}

export function previewDossierFile(documentEntryId) {
  return request({
    url: '/project1/dossier/detail/files/' + documentEntryId + '/preview',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportDossierFiles(documentEntryIds) {
  return request({
    url: '/project1/dossier/detail/files/export',
    method: 'post',
    data: documentEntryIds,
    responseType: 'blob'
  })
}

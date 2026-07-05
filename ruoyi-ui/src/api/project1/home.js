import request from '@/utils/request'

export function getDossierHomeSummary() {
  return request({
    url: '/project1/home/dossier-summary',
    method: 'get'
  })
}

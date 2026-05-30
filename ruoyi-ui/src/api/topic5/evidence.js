import request from '@/utils/request'

// 查询课题五-诊断证据列表
export function listEvidence(query) {
  return request({
    url: '/topic5/evidence/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-诊断证据详细
export function getEvidence(evidenceId) {
  return request({
    url: '/topic5/evidence/' + evidenceId,
    method: 'get'
  })
}

// 新增课题五-诊断证据
export function addEvidence(data) {
  return request({
    url: '/topic5/evidence',
    method: 'post',
    data: data
  })
}

// 修改课题五-诊断证据
export function updateEvidence(data) {
  return request({
    url: '/topic5/evidence',
    method: 'put',
    data: data
  })
}

// 删除课题五-诊断证据
export function delEvidence(evidenceId) {
  return request({
    url: '/topic5/evidence/' + evidenceId,
    method: 'delete'
  })
}

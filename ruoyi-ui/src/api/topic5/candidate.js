import request from '@/utils/request'

// 查询课题五-候选零部件定位结果列表
export function listCandidate(query) {
  return request({
    url: '/topic5/candidate/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-候选零部件定位结果详细
export function getCandidate(candidateId) {
  return request({
    url: '/topic5/candidate/' + candidateId,
    method: 'get'
  })
}

// 新增课题五-候选零部件定位结果
export function addCandidate(data) {
  return request({
    url: '/topic5/candidate',
    method: 'post',
    data: data
  })
}

// 修改课题五-候选零部件定位结果
export function updateCandidate(data) {
  return request({
    url: '/topic5/candidate',
    method: 'put',
    data: data
  })
}

// 删除课题五-候选零部件定位结果
export function delCandidate(candidateId) {
  return request({
    url: '/topic5/candidate/' + candidateId,
    method: 'delete'
  })
}

import request from '@/utils/request'

// 查询课题五-算法运行记录列表
export function listAlgorithmRun(query) {
  return request({
    url: '/topic5/algorithmRun/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-算法运行记录详细
export function getAlgorithmRun(runId) {
  return request({
    url: '/topic5/algorithmRun/' + runId,
    method: 'get'
  })
}

// 新增课题五-算法运行记录
export function addAlgorithmRun(data) {
  return request({
    url: '/topic5/algorithmRun',
    method: 'post',
    data: data
  })
}

// 修改课题五-算法运行记录
export function updateAlgorithmRun(data) {
  return request({
    url: '/topic5/algorithmRun',
    method: 'put',
    data: data
  })
}

// 删除课题五-算法运行记录
export function delAlgorithmRun(runId) {
  return request({
    url: '/topic5/algorithmRun/' + runId,
    method: 'delete'
  })
}

import request from '@/utils/request'

// 查询追溯案例列表
export function listTraceCase(query) {
  return request({
    url: '/topic5/traceCase/list',
    method: 'get',
    params: query
  })
}

// 查询追溯案例详细
export function getTraceCase(partId) {
  return request({
    url: '/topic5/traceCase/' + partId,
    method: 'get'
  })
}

// 新增追溯案例
export function addTraceCase(data) {
  return request({
    url: '/topic5/traceCase',
    method: 'post',
    data: data
  })
}

// 修改追溯案例
export function updateTraceCase(data) {
  return request({
    url: '/topic5/traceCase',
    method: 'put',
    data: data
  })
}

// 删除追溯案例
export function delTraceCase(partId) {
  return request({
    url: '/topic5/traceCase/' + partId,
    method: 'delete'
  })
}

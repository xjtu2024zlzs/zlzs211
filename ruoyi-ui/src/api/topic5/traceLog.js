import request from '@/utils/request'

// 查询追溯流程记录列表
export function listTraceLog(query) {
  return request({
    url: '/topic5/traceLog/list',
    method: 'get',
    params: query
  })
}

// 查询追溯流程记录详细
export function getTraceLog(id) {
  return request({
    url: '/topic5/traceLog/' + id,
    method: 'get'
  })
}

// 新增追溯流程记录
export function addTraceLog(data) {
  return request({
    url: '/topic5/traceLog',
    method: 'post',
    data: data
  })
}

// 修改追溯流程记录
export function updateTraceLog(data) {
  return request({
    url: '/topic5/traceLog',
    method: 'put',
    data: data
  })
}

// 删除追溯流程记录
export function delTraceLog(id) {
  return request({
    url: '/topic5/traceLog/' + id,
    method: 'delete'
  })
}

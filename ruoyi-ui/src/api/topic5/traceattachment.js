import request from '@/utils/request'

// 查询追溯附件列表
export function listTraceattachment(query) {
  return request({
    url: '/topic5/traceattachment/list',
    method: 'get',
    params: query
  })
}

// 查询追溯附件详细
export function getTraceattachment(id) {
  return request({
    url: '/topic5/traceattachment/' + id,
    method: 'get'
  })
}

// 新增追溯附件
export function addTraceattachment(data) {
  return request({
    url: '/topic5/traceattachment',
    method: 'post',
    data: data
  })
}

// 修改追溯附件
export function updateTraceattachment(data) {
  return request({
    url: '/topic5/traceattachment',
    method: 'put',
    data: data
  })
}

// 删除追溯附件
export function delTraceattachment(id) {
  return request({
    url: '/topic5/traceattachment/' + id,
    method: 'delete'
  })
}

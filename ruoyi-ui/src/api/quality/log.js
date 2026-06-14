import request from '@/utils/request'

// 查询质量问题流程日志列表
export function listLog(query) {
  return request({
    url: '/qms/log/list',
    method: 'get',
    params: query
  })
}

// 查询质量问题流程日志详细
export function getLog(logId) {
  return request({
    url: '/qms/log/' + logId,
    method: 'get'
  })
}

// 新增质量问题流程日志
export function addLog(data) {
  return request({
    url: '/qms/log',
    method: 'post',
    data: data
  })
}

// 修改质量问题流程日志
export function updateLog(data) {
  return request({
    url: '/qms/log',
    method: 'put',
    data: data
  })
}

// 删除质量问题流程日志
export function delLog(logId) {
  return request({
    url: '/qms/log/' + logId,
    method: 'delete'
  })
}

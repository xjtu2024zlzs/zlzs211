import request from '@/utils/request'

// 查询模式映射任务创建列表
export function listMatchTask(query) {
  return request({
    url: '/project1/matchTask/list',
    method: 'get',
    params: query
  })
}

// 查询模式映射任务创建详细
export function getMatchTask(taskId) {
  return request({
    url: '/project1/matchTask/' + taskId,
    method: 'get'
  })
}

// 新增模式映射任务创建
export function addMatchTask(data) {
  return request({
    url: '/project1/matchTask',
    method: 'post',
    data: data
  })
}

// 修改模式映射任务创建
export function updateMatchTask(data) {
  return request({
    url: '/project1/matchTask',
    method: 'put',
    data: data
  })
}

// 删除模式映射任务创建
export function delMatchTask(taskId) {
  return request({
    url: '/project1/matchTask/' + taskId,
    method: 'delete'
  })
}

// Run matching task without algorithm dependency
export function runMatchTask(taskId) {
  return request({
    url: '/project1/matchTask/run/' + taskId,
    method: 'post'
  })
}

// Query task versions
export function listMatchTaskVersion(taskId) {
  return request({
    url: '/project1/matchTask/version/' + taskId,
    method: 'get'
  })
}

// Query task run records
export function listMatchTaskRecord(taskId) {
  return request({
    url: '/project1/matchTask/record/' + taskId,
    method: 'get'
  })
}

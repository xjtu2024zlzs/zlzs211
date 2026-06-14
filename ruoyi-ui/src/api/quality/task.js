import request from '@/utils/request'

// 查询质量问题模块处理任务列表
export function listTask(query) {
  return request({
    url: '/qms/task/list',
    method: 'get',
    params: query
  })
}

// 查询质量问题模块处理任务详细
export function getTask(taskId) {
  return request({
    url: '/qms/task/' + taskId,
    method: 'get'
  })
}

// 新增质量问题模块处理任务
export function addTask(data) {
  return request({
    url: '/qms/task',
    method: 'post',
    data: data
  })
}

// 修改质量问题模块处理任务
export function updateTask(data) {
  return request({
    url: '/qms/task',
    method: 'put',
    data: data
  })
}

// 删除质量问题模块处理任务
export function delTask(taskId) {
  return request({
    url: '/qms/task/' + taskId,
    method: 'delete'
  })
}

import request from '@/utils/request'

// 查询课题五-追溯任务主列表
export function listTask(query) {
  return request({
    url: '/topic5/task/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-追溯任务主详细
export function getTask(taskId) {
  return request({
    url: '/topic5/task/' + taskId,
    method: 'get'
  })
}

// 新增课题五-追溯任务主
export function addTask(data) {
  return request({
    url: '/topic5/task',
    method: 'post',
    data: data
  })
}

// 修改课题五-追溯任务主
export function updateTask(data) {
  return request({
    url: '/topic5/task',
    method: 'put',
    data: data
  })
}

// 删除课题五-追溯任务主
export function delTask(taskId) {
  return request({
    url: '/topic5/task/' + taskId,
    method: 'delete'
  })
}

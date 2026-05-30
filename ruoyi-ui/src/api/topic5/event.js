import request from '@/utils/request'

// 查询课题五-零部件生命周期事件列表
export function listEvent(query) {
  return request({
    url: '/topic5/event/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-零部件生命周期事件详细
export function getEvent(eventId) {
  return request({
    url: '/topic5/event/' + eventId,
    method: 'get'
  })
}

// 新增课题五-零部件生命周期事件
export function addEvent(data) {
  return request({
    url: '/topic5/event',
    method: 'post',
    data: data
  })
}

// 修改课题五-零部件生命周期事件
export function updateEvent(data) {
  return request({
    url: '/topic5/event',
    method: 'put',
    data: data
  })
}

// 删除课题五-零部件生命周期事件
export function delEvent(eventId) {
  return request({
    url: '/topic5/event/' + eventId,
    method: 'delete'
  })
}

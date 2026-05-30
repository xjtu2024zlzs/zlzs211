import request from '@/utils/request'

// 查询课题五-零部件实例列表
export function listInstance(query) {
  return request({
    url: '/topic5/instance/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-零部件实例详细
export function getInstance(partId) {
  return request({
    url: '/topic5/instance/' + partId,
    method: 'get'
  })
}

// 新增课题五-零部件实例
export function addInstance(data) {
  return request({
    url: '/topic5/instance',
    method: 'post',
    data: data
  })
}

// 修改课题五-零部件实例
export function updateInstance(data) {
  return request({
    url: '/topic5/instance',
    method: 'put',
    data: data
  })
}

// 删除课题五-零部件实例
export function delInstance(partId) {
  return request({
    url: '/topic5/instance/' + partId,
    method: 'delete'
  })
}

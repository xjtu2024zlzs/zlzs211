import request from '@/utils/request'

// 查询零部件实例列表
export function listPartInstance(query) {
  return request({
    url: '/topic5/partInstance/list',
    method: 'get',
    params: query
  })
}

// 查询零部件实例详细
export function getPartInstance(caseId) {
  return request({
    url: '/topic5/partInstance/' + caseId,
    method: 'get'
  })
}

// 新增零部件实例
export function addPartInstance(data) {
  return request({
    url: '/topic5/partInstance',
    method: 'post',
    data: data
  })
}

// 修改零部件实例
export function updatePartInstance(data) {
  return request({
    url: '/topic5/partInstance',
    method: 'put',
    data: data
  })
}

// 删除零部件实例
export function delPartInstance(caseId) {
  return request({
    url: '/topic5/partInstance/' + caseId,
    method: 'delete'
  })
}

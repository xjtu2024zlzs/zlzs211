import request from '@/utils/request'

// 查询全生命周期图节点列表
export function listLifecycleNode(query) {
  return request({
    url: '/topic5/lifecycleNode/list',
    method: 'get',
    params: query
  })
}

// 查询全生命周期图节点详细
export function getLifecycleNode(nodeId) {
  return request({
    url: '/topic5/lifecycleNode/' + nodeId,
    method: 'get'
  })
}

// 新增全生命周期图节点
export function addLifecycleNode(data) {
  return request({
    url: '/topic5/lifecycleNode',
    method: 'post',
    data: data
  })
}

// 修改全生命周期图节点
export function updateLifecycleNode(data) {
  return request({
    url: '/topic5/lifecycleNode',
    method: 'put',
    data: data
  })
}

// 删除全生命周期图节点
export function delLifecycleNode(nodeId) {
  return request({
    url: '/topic5/lifecycleNode/' + nodeId,
    method: 'delete'
  })
}

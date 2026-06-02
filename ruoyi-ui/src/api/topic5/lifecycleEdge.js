import request from '@/utils/request'

// 查询全生命周期图关系边列表
export function listLifecycleEdge(query) {
  return request({
    url: '/topic5/lifecycleEdge/list',
    method: 'get',
    params: query
  })
}

// 查询全生命周期图关系边详细
export function getLifecycleEdge(edgeId) {
  return request({
    url: '/topic5/lifecycleEdge/' + edgeId,
    method: 'get'
  })
}

// 新增全生命周期图关系边
export function addLifecycleEdge(data) {
  return request({
    url: '/topic5/lifecycleEdge',
    method: 'post',
    data: data
  })
}

// 修改全生命周期图关系边
export function updateLifecycleEdge(data) {
  return request({
    url: '/topic5/lifecycleEdge',
    method: 'put',
    data: data
  })
}

// 删除全生命周期图关系边
export function delLifecycleEdge(edgeId) {
  return request({
    url: '/topic5/lifecycleEdge/' + edgeId,
    method: 'delete'
  })
}

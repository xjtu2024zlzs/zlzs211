import request from '@/utils/request'

// 查询全生命周期关联模型列表
export function listLifecycleGraph(query) {
  return request({
    url: '/topic5/lifecycleGraph/list',
    method: 'get',
    params: query
  })
}

// 查询全生命周期关联模型详细
export function getLifecycleGraph(graphId) {
  return request({
    url: '/topic5/lifecycleGraph/' + graphId,
    method: 'get'
  })
}

// 新增全生命周期关联模型
export function addLifecycleGraph(data) {
  return request({
    url: '/topic5/lifecycleGraph',
    method: 'post',
    data: data
  })
}

// 修改全生命周期关联模型
export function updateLifecycleGraph(data) {
  return request({
    url: '/topic5/lifecycleGraph',
    method: 'put',
    data: data
  })
}

// 删除全生命周期关联模型
export function delLifecycleGraph(graphId) {
  return request({
    url: '/topic5/lifecycleGraph/' + graphId,
    method: 'delete'
  })
}

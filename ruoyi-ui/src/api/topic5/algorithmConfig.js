import request from '@/utils/request'

// 查询算法配置列表
export function listAlgorithmConfig(query) {
  return request({
    url: '/topic5/algorithmConfig/list',
    method: 'get',
    params: query
  })
}

// 查询算法配置详细
export function getAlgorithmConfig(locateId) {
  return request({
    url: '/topic5/algorithmConfig/' + locateId,
    method: 'get'
  })
}

// 新增算法配置
export function addAlgorithmConfig(data) {
  return request({
    url: '/topic5/algorithmConfig',
    method: 'post',
    data: data
  })
}

// 修改算法配置
export function updateAlgorithmConfig(data) {
  return request({
    url: '/topic5/algorithmConfig',
    method: 'put',
    data: data
  })
}

// 删除算法配置
export function delAlgorithmConfig(locateId) {
  return request({
    url: '/topic5/algorithmConfig/' + locateId,
    method: 'delete'
  })
}

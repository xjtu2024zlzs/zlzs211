import request from '@/utils/request'

// 查询零件定位结果列表
export function listPartLocateResult(query) {
  return request({
    url: '/topic5/partLocateResult/list',
    method: 'get',
    params: query
  })
}

// 查询零件定位结果详细
export function getPartLocateResult(algorithmId) {
  return request({
    url: '/topic5/partLocateResult/' + algorithmId,
    method: 'get'
  })
}

// 新增零件定位结果
export function addPartLocateResult(data) {
  return request({
    url: '/topic5/partLocateResult',
    method: 'post',
    data: data
  })
}

// 修改零件定位结果
export function updatePartLocateResult(data) {
  return request({
    url: '/topic5/partLocateResult',
    method: 'put',
    data: data
  })
}

// 删除零件定位结果
export function delPartLocateResult(algorithmId) {
  return request({
    url: '/topic5/partLocateResult/' + algorithmId,
    method: 'delete'
  })
}

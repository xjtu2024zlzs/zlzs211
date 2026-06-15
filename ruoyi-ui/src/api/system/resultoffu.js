import request from '@/utils/request'

// 查询故障诊断-融合征结果列表
export function listResultoffu(query) {
  return request({
    url: '/system/resultoffu/list',
    method: 'get',
    params: query
  })
}

// 查询故障诊断-融合征结果详细
export function getResultoffu(fusionId) {
  return request({
    url: '/system/resultoffu/' + fusionId,
    method: 'get'
  })
}

// 新增故障诊断-融合征结果
export function addResultoffu(data) {
  return request({
    url: '/system/resultoffu',
    method: 'post',
    data: data
  })
}

// 修改故障诊断-融合征结果
export function updateResultoffu(data) {
  return request({
    url: '/system/resultoffu',
    method: 'put',
    data: data
  })
}

// 删除故障诊断-融合征结果
export function delResultoffu(fusionId) {
  return request({
    url: '/system/resultoffu/' + fusionId,
    method: 'delete'
  })
}
export function runFusion(data) {
  return request({
    url: '/system/resultoffu/run',
    method: 'post',
    data: data
  })
}
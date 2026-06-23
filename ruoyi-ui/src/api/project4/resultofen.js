import request from '@/utils/request'

// 查询故障诊断-样本增强结果列表
export function listResultofen(query) {
  return request({
    url: '/project4/resultofen/list',
    method: 'get',
    params: query
  })
}

// 查询故障诊断-样本增强结果详细
export function getResultofen(augmentId) {
  return request({
    url: '/project4/resultofen/' + augmentId,
    method: 'get'
  })
}

// 新增故障诊断-样本增强结果
export function addResultofen(data) {
  return request({
    url: '/project4/resultofen',
    method: 'post',
    data: data
  })
}

// 修改故障诊断-样本增强结果
export function updateResultofen(data) {
  return request({
    url: '/project4/resultofen',
    method: 'put',
    data: data
  })
}

// 删除故障诊断-样本增强结果
export function delResultofen(augmentId) {
  return request({
    url: '/project4/resultofen/' + augmentId,
    method: 'delete'
  })
}

// 执行样本增强
export function runAugment(data) {
  return request({
    url: '/project4/resultofen/augment',
    method: 'post',
    data: data,
    timeout: 300000
  })
}
import request from '@/utils/request'

// 查询数据接入管理列表
export function listAccessPlan(query) {
  return request({
    url: '/project1/accessPlan/list',
    method: 'get',
    params: query
  })
}

// 查询数据接入管理详细
export function getAccessPlan(accessPlanId) {
  return request({
    url: '/project1/accessPlan/' + accessPlanId,
    method: 'get'
  })
}

// 新增数据接入管理
export function addAccessPlan(data) {
  return request({
    url: '/project1/accessPlan',
    method: 'post',
    data: data
  })
}

// 修改数据接入管理
export function updateAccessPlan(data) {
  return request({
    url: '/project1/accessPlan',
    method: 'put',
    data: data
  })
}

// 删除数据接入管理
export function delAccessPlan(accessPlanId) {
  return request({
    url: '/project1/accessPlan/' + accessPlanId,
    method: 'delete'
  })
}

// Execute access plan
export function executeAccessPlan(accessPlanId) {
  return request({
    url: '/project1/accessPlan/execute/' + accessPlanId,
    method: 'post'
  })
}

// Pause access plan
export function pauseAccessPlan(accessPlanId) {
  return request({
    url: '/project1/accessPlan/pause/' + accessPlanId,
    method: 'post'
  })
}

// Resume access plan
export function resumeAccessPlan(accessPlanId) {
  return request({
    url: '/project1/accessPlan/resume/' + accessPlanId,
    method: 'post'
  })
}

// Cancel current access execution
export function cancelAccessPlan(accessPlanId) {
  return request({
    url: '/project1/accessPlan/cancel/' + accessPlanId,
    method: 'post'
  })
}

import request from '@/utils/request'

// 查询数据接入结果展示列表
export function listAccessResult(query) {
  return request({
    url: '/project1/accessResult/list',
    method: 'get',
    params: query
  })
}

// 查询数据接入结果展示详细
export function getAccessResult(tableResultId) {
  return request({
    url: '/project1/accessResult/' + tableResultId,
    method: 'get'
  })
}

// 新增数据接入结果展示
export function addAccessResult(data) {
  return request({
    url: '/project1/accessResult',
    method: 'post',
    data: data
  })
}

// 修改数据接入结果展示
export function updateAccessResult(data) {
  return request({
    url: '/project1/accessResult',
    method: 'put',
    data: data
  })
}

// 删除数据接入结果展示
export function delAccessResult(tableResultId) {
  return request({
    url: '/project1/accessResult/' + tableResultId,
    method: 'delete'
  })
}

// Summarize access result by plan
export function getAccessResultSummary(accessPlanId) {
  return request({
    url: '/project1/accessResult/summary/' + accessPlanId,
    method: 'get'
  })
}

// Query access result dashboard data
export function getAccessResultDashboard(accessPlanId) {
  return request({
    url: '/project1/accessResult/dashboard/' + accessPlanId,
    method: 'get'
  })
}

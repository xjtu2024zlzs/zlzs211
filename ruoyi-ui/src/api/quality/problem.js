import request from '@/utils/request'

// 查询质量问题列表
export function listProblem(query) {
  return request({
    url: '/qms/problem/list',
    method: 'get',
    params: query
  })
}

// 查询质量问题详细
export function getProblem(problemId) {
  return request({
    url: '/qms/problem/' + problemId,
    method: 'get'
  })
}

// 新增质量问题
export function addProblem(data) {
  return request({
    url: '/qms/problem',
    method: 'post',
    data: data
  })
}

// 修改质量问题
export function updateProblem(data) {
  return request({
    url: '/qms/problem',
    method: 'put',
    data: data
  })
}

// 删除质量问题
export function delProblem(problemId) {
  return request({
    url: '/qms/problem/' + problemId,
    method: 'delete'
  })
}

// 导出质量问题 Word 报告
export function exportProblemReport(problemId) {
  return request({
    url: '/qms/problem/report/' + problemId,
    method: 'get',
    responseType: 'blob'
  })
}

import request from '@/utils/request'

// 查询追溯问题列表
export function listTraceProblem(query) {
  return request({
    url: '/topic5/traceProblem/list',
    method: 'get',
    params: query
  })
}

// 查询追溯问题详细
export function getTraceProblem(id) {
  return request({
    url: '/topic5/traceProblem/' + id,
    method: 'get'
  })
}

// 新增追溯问题
export function addTraceProblem(data) {
  return request({
    url: '/topic5/traceProblem',
    method: 'post',
    data: data
  })
}

// 修改追溯问题
export function updateTraceProblem(data) {
  return request({
    url: '/topic5/traceProblem',
    method: 'put',
    data: data
  })
}

// 删除追溯问题
export function delTraceProblem(id) {
  return request({
    url: '/topic5/traceProblem/' + id,
    method: 'delete'
  })
}

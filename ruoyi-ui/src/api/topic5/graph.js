import request from '@/utils/request'

// 获取追溯任务列表
export function listGraphTrace(query) {
  return request({
    url: '/topic5/graph/trace/options',
    method: 'get',
    params: query
  })
}

// 获取追溯任务详情
export function getGraphTrace(id) {
  return request({
    url: '/topic5/graph/trace/' + id,
    method: 'get'
  })
}

// 从课题一拉取知识图谱
export function pullTopic1Kg(id) {
  return request({
    url: '/topic5/graph/' + id + '/pull-topic1-kg',
    method: 'post'
  })
}

// 运行第二部分算法
export function runSecondAlgorithm(id, data) {
  return request({
    url: '/topic5/graph/' + id + '/run-second-algorithm',
    method: 'post',
    data: data
  })
}

// 获取图谱信息
export function getTraceKg(id) {
  return request({
    url: '/topic5/graph/' + id + '/kg',
    method: 'get'
  })
}

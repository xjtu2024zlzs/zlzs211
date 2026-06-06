import request from '@/utils/request'

// 获取追溯任务列表
export function listSourceTrace(query) {
  return request({
    url: '/topic5/source/trace/options',
    method: 'get',
    params: query
  })
}

// 获取追溯任务详情
export function getSourceTrace(id) {
  return request({
    url: '/topic5/source/trace/' + id,
    method: 'get'
  })
}

// 获取溯源结果
export function getSourceResult(id) {
  return request({
    url: '/topic5/source/' + id + '/result',
    method: 'get'
  })
}

// 运行最终溯源算法
export function runSourceAlgorithm(id, data) {
  return request({
    url: '/topic5/source/' + id + '/run-source-algorithm',
    method: 'post',
    data: data
  })
}

// 导出溯源报告
export function exportSourceReport(id) {
  return request({
    url: '/topic5/source/' + id + '/export-report',
    method: 'post'
  })
}

// 推送课题二
export function pushTopic2(id) {
  return request({
    url: '/topic5/source/' + id + '/push-topic2',
    method: 'post'
  })
}

// 推送课题三
export function pushTopic3(id) {
  return request({
    url: '/topic5/source/' + id + '/push-topic3',
    method: 'post'
  })
}
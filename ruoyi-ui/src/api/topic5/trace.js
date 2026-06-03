import request from '@/utils/request'

// 查询追溯问题列表
export function listTrace(query) {
  return request({
    url: '/topic5/trace/list',
    method: 'get',
    params: query
  })
}

// 查询追溯问题详细
export function getTrace(id) {
  return request({
    url: '/topic5/trace/' + id,
    method: 'get'
  })
}

// 新增追溯问题
export function addTrace(data) {
  return request({
    url: '/topic5/trace',
    method: 'post',
    data: data
  })
}

// 修改追溯问题
export function updateTrace(data) {
  return request({
    url: '/topic5/trace',
    method: 'put',
    data: data
  })
}

// 删除追溯问题
export function delTrace(id) {
  return request({
    url: '/topic5/trace/' + id,
    method: 'delete'
  })
}

// 获取追溯任务下拉框
export function traceOptions() {
  return request({
    url: '/topic5/trace/options',
    method: 'get'
  })
}

// 从课题一数字卷宗调用传感器附件
export function importDossierFiles(id) {
  return request({
    url: '/topic5/trace/' + id + '/dossier/import',
    method: 'post'
  })
}

// 保存传感器附件
export function saveAttachments(id) {
  return request({
    url: '/topic5/trace/' + id + '/attachment/save',
    method: 'post'
  })
}

// 推送给课题四
export function pushTopic4(id) {
  return request({
    url: '/topic5/trace/' + id + '/push-topic4',
    method: 'post'
  })
}

// 模拟课题四返回结果
export function topic4Callback(data) {
  return request({
    url: '/topic5/trace/topic4/callback',
    method: 'post',
    data: data
  })
}

// 回填课题四结果
export function fillTopic4Result(id) {
  return request({
    url: '/topic5/trace/' + id + '/fill-topic4-result',
    method: 'post'
  })
}

// 查询流程阶段
export function getWorkflow(id) {
  return request({
    url: '/topic5/trace/' + id + '/workflow',
    method: 'get'
  })
}

// 模拟运行 Python 追溯算法
export function runAlgorithm(id) {
  return request({
    url: '/topic5/trace/' + id + '/run-algorithm',
    method: 'post'
  })
}
// 保存附件并同步保存位置
export function saveAttachmentsWithPath(id, data) {
  return request({
    url: '/topic5/trace/' + id + '/attachment/save-with-path',
    method: 'post',
    data: data
  })
}
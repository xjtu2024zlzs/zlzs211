import request from '@/utils/request'

// 查询故障诊断-诊断结果列表
export function listResultofgr(query) {
  return request({
    url: '/resultofgr/list',
    method: 'get',
    params: query
  })
}

// 查询故障诊断-诊断结果详细
export function getResultofgr(diagnosisId) {
  return request({
    url: '/resultofgr/' + diagnosisId,
    method: 'get'
  })
}

// 新增故障诊断-诊断结果
export function addResultofgr(data) {
  return request({
    url: '/resultofgr',
    method: 'post',
    data: data
  })
}

// 修改故障诊断-诊断结果
export function updateResultofgr(data) {
  return request({
    url: '/resultofgr',
    method: 'put',
    data: data
  })
}

// 删除故障诊断-诊断结果
export function delResultofgr(diagnosisId) {
  return request({
    url: '/resultofgr/' + diagnosisId,
    method: 'delete'
  })
}

// 测试 Java 后端调用 Python 算法服务
export function pythonTest() {
  return request({
    url: '/resultofgr/pythonTest',
    method: 'get'
  })
}

// 执行模拟诊断并保存诊断结果
export function runMockDiagnosis() {
  return request({
    url: '/resultofgr/runMock',
    method: 'get'
  })
}

// 根据原始数据文件执行诊断
export function runDiagnosisByFile(data) {
  return request({
    url: '/resultofgr/runByFile',
    method: 'post',
    data: data
  })
}

// 故障根因分析
export function rootCauseAnalysis(diagnosisId) {
  return request({
    url: '/resultofgr/rootCause/' + diagnosisId,
    method: 'post'
  })
}
export function runDiagnosis(data) {
  return request({
    url: '/resultofgr/run',
    method: 'post',
    data: data
  })
}

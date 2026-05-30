import request from '@/utils/request'

// 查询课题五-故障阶段诊断结果列表
export function listDiagnosis(query) {
  return request({
    url: '/topic5/diagnosis/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-故障阶段诊断结果详细
export function getDiagnosis(diagnosisId) {
  return request({
    url: '/topic5/diagnosis/' + diagnosisId,
    method: 'get'
  })
}

// 新增课题五-故障阶段诊断结果
export function addDiagnosis(data) {
  return request({
    url: '/topic5/diagnosis',
    method: 'post',
    data: data
  })
}

// 修改课题五-故障阶段诊断结果
export function updateDiagnosis(data) {
  return request({
    url: '/topic5/diagnosis',
    method: 'put',
    data: data
  })
}

// 删除课题五-故障阶段诊断结果
export function delDiagnosis(diagnosisId) {
  return request({
    url: '/topic5/diagnosis/' + diagnosisId,
    method: 'delete'
  })
}

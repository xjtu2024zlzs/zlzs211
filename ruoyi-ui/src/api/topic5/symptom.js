import request from '@/utils/request'

// 查询课题五-故障征输入列表
export function listSymptom(query) {
  return request({
    url: '/topic5/symptom/list',
    method: 'get',
    params: query
  })
}

// 查询课题五-故障征输入详细
export function getSymptom(symptomId) {
  return request({
    url: '/topic5/symptom/' + symptomId,
    method: 'get'
  })
}

// 新增课题五-故障征输入
export function addSymptom(data) {
  return request({
    url: '/topic5/symptom',
    method: 'post',
    data: data
  })
}

// 修改课题五-故障征输入
export function updateSymptom(data) {
  return request({
    url: '/topic5/symptom',
    method: 'put',
    data: data
  })
}

// 删除课题五-故障征输入
export function delSymptom(symptomId) {
  return request({
    url: '/topic5/symptom/' + symptomId,
    method: 'delete'
  })
}

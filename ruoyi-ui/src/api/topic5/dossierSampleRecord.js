import request from '@/utils/request'

// 查询数字卷宗模拟数据列表
export function listDossierSampleRecord(query) {
  return request({
    url: '/topic5/dossierSampleRecord/list',
    method: 'get',
    params: query
  })
}

// 查询数字卷宗模拟数据详细
export function getDossierSampleRecord(recordId) {
  return request({
    url: '/topic5/dossierSampleRecord/' + recordId,
    method: 'get'
  })
}

// 新增数字卷宗模拟数据
export function addDossierSampleRecord(data) {
  return request({
    url: '/topic5/dossierSampleRecord',
    method: 'post',
    data: data
  })
}

// 修改数字卷宗模拟数据
export function updateDossierSampleRecord(data) {
  return request({
    url: '/topic5/dossierSampleRecord',
    method: 'put',
    data: data
  })
}

// 删除数字卷宗模拟数据
export function delDossierSampleRecord(recordId) {
  return request({
    url: '/topic5/dossierSampleRecord/' + recordId,
    method: 'delete'
  })
}

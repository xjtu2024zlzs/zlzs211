import request from '@/utils/request'

// 查询故障诊断-原始数据文件列表
export function listFileofda(query) {
  return request({
    url: '/project4/fileofda/list',
    method: 'get',
    params: query
  })
}

// 兼容另一种命名：listFdDataFile
export function listFdDataFile(query) {
  return listFileofda(query)
}

// 查询故障诊断-原始数据文件详细
export function getFileofda(fileId) {
  return request({
    url: '/project4/fileofda/' + fileId,
    method: 'get'
  })
}

// 兼容另一种命名：getFdDataFile
export function getFdDataFile(fileId) {
  return getFileofda(fileId)
}

// 新增故障诊断-原始数据文件
export function addFileofda(data) {
  return request({
    url: '/project4/fileofda',
    method: 'post',
    data: data
  })
}

// 兼容另一种命名：addFdDataFile
export function addFdDataFile(data) {
  return addFileofda(data)
}

// 修改故障诊断-原始数据文件
export function updateFileofda(data) {
  return request({
    url: '/project4/fileofda',
    method: 'put',
    data: data
  })
}

// 兼容另一种命名：updateFdDataFile
export function updateFdDataFile(data) {
  return updateFileofda(data)
}

// 删除故障诊断-原始数据文件
export function delFileofda(fileId) {
  return request({
    url: '/project4/fileofda/' + fileId,
    method: 'delete'
  })
}

// 兼容另一种命名：delFdDataFile
export function delFdDataFile(fileId) {
  return delFileofda(fileId)
}

// 执行数据预处理
export function executePreprocess(data) {
  return request({
    url: '/project4/fileofda/preprocess',
    method: 'post',
    data: data,
    timeout: 300000
  })
}

// 查询预处理后样本 / 原始样本列表
export function listRawSamples(query) {
  return request({
    url: '/project4/fileofda/rawSamples',
    method: 'get',
    params: query
  })
}


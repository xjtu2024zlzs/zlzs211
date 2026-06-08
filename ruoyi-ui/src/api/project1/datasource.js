import request from '@/utils/request'

// 查询数据源管理列表
export function listDatasource(query) {
  return request({
    url: '/project1/datasource/list',
    method: 'get',
    params: query
  })
}

// 查询数据源管理详细
export function getDatasource(datasourceId) {
  return request({
    url: '/project1/datasource/' + datasourceId,
    method: 'get'
  })
}

// 新增数据源管理
export function addDatasource(data) {
  return request({
    url: '/project1/datasource',
    method: 'post',
    data: data
  })
}

// 修改数据源管理
export function updateDatasource(data) {
  return request({
    url: '/project1/datasource',
    method: 'put',
    data: data
  })
}

// 删除数据源管理
export function delDatasource(datasourceId) {
  return request({
    url: '/project1/datasource/' + datasourceId,
    method: 'delete'
  })
}

// Test datasource connection
export function testDatasourceConnection(datasourceId) {
  return request({
    url: '/project1/datasource/test/' + datasourceId,
    method: 'post'
  })
}

// Read datasource schema
export function readDatasourceSchema(datasourceId) {
  return request({
    url: '/project1/datasource/schema/read/' + datasourceId,
    method: 'post'
  })
}

// Get datasource schema preview
export function getDatasourceSchema(datasourceId) {
  return request({
    url: '/project1/datasource/schema/' + datasourceId,
    method: 'get'
  })
}

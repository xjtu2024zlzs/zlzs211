import request from '@/utils/request'

// 查询质量问题工作模块配置列表
export function listModule(query) {
  return request({
    url: '/qms/module/list',
    method: 'get',
    params: query
  })
}

// 查询质量问题工作模块配置详细
export function getModule(moduleId) {
  return request({
    url: '/qms/module/' + moduleId,
    method: 'get'
  })
}

// 新增质量问题工作模块配置
export function addModule(data) {
  return request({
    url: '/qms/module',
    method: 'post',
    data: data
  })
}

// 修改质量问题工作模块配置
export function updateModule(data) {
  return request({
    url: '/qms/module',
    method: 'put',
    data: data
  })
}

// 删除质量问题工作模块配置
export function delModule(moduleId) {
  return request({
    url: '/qms/module/' + moduleId,
    method: 'delete'
  })
}

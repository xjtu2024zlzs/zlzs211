import request from '@/utils/request'

// 查询卷宗模板列表
export function listTemplate(query) {
  return request({
    url: '/project1/dossier/template/list',
    method: 'get',
    params: query
  })
}

// 查询卷宗模板详情
export function getTemplate(id) {
  return request({
    url: '/project1/dossier/template/' + id,
    method: 'get'
  })
}

// 新增卷宗模板
export function addTemplate(data) {
  return request({
    url: '/project1/dossier/template',
    method: 'post',
    data: data
  })
}

// 修改卷宗模板
export function updateTemplate(data) {
  return request({
    url: '/project1/dossier/template',
    method: 'put',
    data: data
  })
}

// 删除卷宗模板
export function delTemplate(ids) {
  return request({
    url: '/project1/dossier/template/' + ids,
    method: 'delete'
  })
}

// 复制卷宗模板
export function copyTemplate(id) {
  return request({
    url: '/project1/dossier/template/' + id + '/copy',
    method: 'post'
  })
}

// 生成模板新版本
export function createTemplateVersion(id) {
  return request({
    url: '/project1/dossier/template/' + id + '/version',
    method: 'post'
  })
}

// 设置默认模板
export function setDefaultTemplate(id) {
  return request({
    url: '/project1/dossier/template/' + id + '/default',
    method: 'put'
  })
}

// 修改模板状态
export function updateTemplateStatus(id, status) {
  return request({
    url: '/project1/dossier/template/' + id + '/status/' + status,
    method: 'put'
  })
}

// 检查模板配置
export function checkTemplate(id) {
  return request({
    url: '/project1/dossier/template/' + id + '/check',
    method: 'get'
  })
}

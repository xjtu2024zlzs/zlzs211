import request from '@/utils/request'

// 级联树
export function getMonitorTree(query) {
  return request({
    url: '/monitor/tree',
    method: 'get',
    params: query
  })
}

// 零件列表
export function listMonitorParts(query) {
  return request({
    url: '/monitor/parts',
    method: 'get',
    params: query
  })
}
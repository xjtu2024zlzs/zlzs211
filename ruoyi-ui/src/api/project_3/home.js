import request from '@/utils/request'

export function getHomeOverview() {
  return request({
    url: '/home/overview',
    method: 'get'
  })
}
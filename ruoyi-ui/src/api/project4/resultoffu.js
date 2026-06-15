import request from '@/utils/request'

// 鏌ヨ鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滃垪琛?
export function listResultoffu(query) {
  return request({
    url: '/project4/resultoffu/list',
    method: 'get',
    params: query
  })
}

// 鏌ヨ鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋滆缁?
export function getResultoffu(fusionId) {
  return request({
    url: '/project4/resultoffu/' + fusionId,
    method: 'get'
  })
}

// 鏂板鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
export function addResultoffu(data) {
  return request({
    url: '/project4/resultoffu',
    method: 'post',
    data: data
  })
}

// 淇敼鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
export function updateResultoffu(data) {
  return request({
    url: '/project4/resultoffu',
    method: 'put',
    data: data
  })
}

// 鍒犻櫎鏁呴殰璇婃柇-铻嶅悎寰佺粨鏋?
export function delResultoffu(fusionId) {
  return request({
    url: '/project4/resultoffu/' + fusionId,
    method: 'delete'
  })
}
export function runFusion(data) {
  return request({
    url: '/project4/resultoffu/run',
    method: 'post',
    data: data
  })
}

import request from '@/utils/request'

// 鏌ヨ鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉鍒楄〃
export function listResultofen(query) {
  return request({
    url: '/project4/resultofen/list',
    method: 'get',
    params: query
  })
}

// 鏌ヨ鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉璇︾粏
export function getResultofen(augmentId) {
  return request({
    url: '/project4/resultofen/' + augmentId,
    method: 'get'
  })
}

// 鏂板鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
export function addResultofen(data) {
  return request({
    url: '/project4/resultofen',
    method: 'post',
    data: data
  })
}

// 淇敼鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
export function updateResultofen(data) {
  return request({
    url: '/project4/resultofen',
    method: 'put',
    data: data
  })
}

// 鍒犻櫎鏁呴殰璇婃柇-鏍锋湰澧炲己缁撴灉
export function delResultofen(augmentId) {
  return request({
    url: '/project4/resultofen/' + augmentId,
    method: 'delete'
  })
}

// 鎵ц鏍锋湰澧炲己
export function runAugment(data) {
  return request({
    url: '/project4/resultofen/augment',
    method: 'post',
    data: data
  })
}

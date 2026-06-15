import request from '@/utils/request'

// 鏌ヨ鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢鍒楄〃
export function listFileofda(query) {
  return request({
    url: '/project4/fileofda/list',
    method: 'get',
    params: query
  })
}

// 鍏煎鍙︿竴绉嶅懡鍚嶏細listFdDataFile
export function listFdDataFile(query) {
  return listFileofda(query)
}

// 鏌ヨ鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢璇︾粏
export function getFileofda(fileId) {
  return request({
    url: '/project4/fileofda/' + fileId,
    method: 'get'
  })
}

// 鍏煎鍙︿竴绉嶅懡鍚嶏細getFdDataFile
export function getFdDataFile(fileId) {
  return getFileofda(fileId)
}

// 鏂板鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
export function addFileofda(data) {
  return request({
    url: '/project4/fileofda',
    method: 'post',
    data: data
  })
}

// 鍏煎鍙︿竴绉嶅懡鍚嶏細addFdDataFile
export function addFdDataFile(data) {
  return addFileofda(data)
}

// 淇敼鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
export function updateFileofda(data) {
  return request({
    url: '/project4/fileofda',
    method: 'put',
    data: data
  })
}

// 鍏煎鍙︿竴绉嶅懡鍚嶏細updateFdDataFile
export function updateFdDataFile(data) {
  return updateFileofda(data)
}

// 鍒犻櫎鏁呴殰璇婃柇-鍘熷鏁版嵁鏂囦欢
export function delFileofda(fileId) {
  return request({
    url: '/project4/fileofda/' + fileId,
    method: 'delete'
  })
}

// 鍏煎鍙︿竴绉嶅懡鍚嶏細delFdDataFile
export function delFdDataFile(fileId) {
  return delFileofda(fileId)
}

// 鎵ц鏁版嵁棰勫鐞?
export function executePreprocess(data) {
  return request({
    url: '/project4/fileofda/preprocess',
    method: 'post',
    data: data
  })
}

// 鏌ヨ棰勫鐞嗗悗鏍锋湰 / 鍘熷鏍锋湰鍒楄〃
export function listRawSamples(query) {
  return request({
    url: '/project4/fileofda/rawSamples',
    method: 'get',
    params: query
  })
}

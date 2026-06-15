import request from '@/utils/request'

// 鏌ヨ鏍瑰洜鍒嗘瀽缁撴灉鍒楄〃
export function listResultofrc(query) {
  console.log('褰撳墠璋冪敤鐨勬槸鏂扮増 resultofrc.js锛?system/resultofrc/list')
  return request({
    url: '/project4/resultofrc/list',
    method: 'get',
    params: query
  })
}

// 鏌ヨ鏍瑰洜鍒嗘瀽缁撴灉璇︾粏
export function getResultofrc(analysisId) {
  return request({
    url: '/project4/resultofrc/' + analysisId,
    method: 'get'
  })
}

// 鏂板鏍瑰洜鍒嗘瀽缁撴灉
export function addResultofrc(data) {
  return request({
    url: '/project4/resultofrc',
    method: 'post',
    data: data
  })
}

// 淇敼鏍瑰洜鍒嗘瀽缁撴灉
export function updateResultofrc(data) {
  return request({
    url: '/project4/resultofrc',
    method: 'put',
    data: data
  })
}

// 鍒犻櫎鏍瑰洜鍒嗘瀽缁撴灉
export function delResultofrc(analysisId) {
  return request({
    url: '/project4/resultofrc/' + analysisId,
    method: 'delete'
  })
}

// 鏌ヨ鏍瑰洜鍒嗘瀽缃俊搴︾粺璁★細鍘熸潵鐨勬€讳綋缁熻楗煎浘
export function confidenceStats(query) {
  return request({
    url: '/project4/resultofrc/confidenceStats',
    method: 'get',
    params: query
  })
}

// 鏌ヨ姣忎釜鏍锋湰鐨勫悇涓牴鍥犵疆淇″害楗煎浘鏁版嵁
export function sampleConfidencePie(query) {
  return request({
    url: '/project4/resultofrc/sampleConfidencePie',
    method: 'get',
    params: query
  })
}

// 鏌ヨ璇鹃鍥涘悜璇鹃浜旇緭鍑虹殑鏍瑰洜鍒嗘瀽缁撴灉鍒楄〃
export function outputListResultofrc(query) {
  return request({
    url: '/project4/resultofrc/output/list',
    method: 'get',
    params: query
  })
}

// 鏌ヨ璇鹃鍥涘悜璇鹃浜旇緭鍑虹殑鍗曟潯鏍瑰洜鍒嗘瀽缁撴灉
export function outputInfoResultofrc(analysisId) {
  return request({
    url: '/project4/resultofrc/output/' + analysisId,
    method: 'get'
  })
}

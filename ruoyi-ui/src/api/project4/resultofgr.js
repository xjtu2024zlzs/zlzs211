import request from '@/utils/request'

// 鏌ヨ鏁呴殰璇婃柇-璇婃柇缁撴灉鍒楄〃
export function listResultofgr(query) {
  return request({
    url: '/project4/resultofgr/list',
    method: 'get',
    params: query
  })
}

// 鏌ヨ鏁呴殰璇婃柇-璇婃柇缁撴灉璇︾粏
export function getResultofgr(diagnosisId) {
  return request({
    url: '/project4/resultofgr/' + diagnosisId,
    method: 'get'
  })
}

// 鏂板鏁呴殰璇婃柇-璇婃柇缁撴灉
export function addResultofgr(data) {
  return request({
    url: '/project4/resultofgr',
    method: 'post',
    data: data
  })
}

// 淇敼鏁呴殰璇婃柇-璇婃柇缁撴灉
export function updateResultofgr(data) {
  return request({
    url: '/project4/resultofgr',
    method: 'put',
    data: data
  })
}

// 鍒犻櫎鏁呴殰璇婃柇-璇婃柇缁撴灉
export function delResultofgr(diagnosisId) {
  return request({
    url: '/project4/resultofgr/' + diagnosisId,
    method: 'delete'
  })
}

// 娴嬭瘯 Java 鍚庣璋冪敤 Python 绠楁硶鏈嶅姟
export function pythonTest() {
  return request({
    url: '/project4/resultofgr/pythonTest',
    method: 'get'
  })
}

// 鎵ц妯℃嫙璇婃柇骞朵繚瀛樿瘖鏂粨鏋?
export function runMockDiagnosis() {
  return request({
    url: '/project4/resultofgr/runMock',
    method: 'get'
  })
}

// 鏍规嵁鍘熷鏁版嵁鏂囦欢鎵ц璇婃柇
export function runDiagnosisByFile(data) {
  return request({
    url: '/project4/resultofgr/runByFile',
    method: 'post',
    data: data
  })
}

// 鏁呴殰鏍瑰洜鍒嗘瀽
export function rootCauseAnalysis(diagnosisId) {
  return request({
    url: '/project4/resultofgr/rootCause/' + diagnosisId,
    method: 'post'
  })
}
export function runDiagnosis(data) {
  return request({
    url: '/project4/resultofgr/run',
    method: 'post',
    data: data
  })
}

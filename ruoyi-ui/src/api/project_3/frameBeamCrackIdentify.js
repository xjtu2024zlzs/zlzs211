import request from '@/utils/request'

export function startFrameBeamCrackTask(data) {
  return request({
    url: '/service/frame-beam-crack/tasks',
    method: 'post',
    data,
    timeout: 3600000
  })
}

export function importAndStartFrameBeamCrack(data) {
  return request({
    url: '/service/frame-beam-crack/import-and-start',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 600000
  })
}

export function uploadFrameBeamCrackFile(data, onUploadProgress) {
  return request({
    url: '/service/frame-beam-crack/uploads/file',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000,
    onUploadProgress
  })
}

export function uploadFrameBeamCrackChunk(data) {
  return request({
    url: '/service/frame-beam-crack/uploads/chunk',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

export function mergeFrameBeamCrackChunks(data) {
  return request({
    url: '/service/frame-beam-crack/uploads/merge',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

export function getFrameBeamCrackTask(taskId) {
  return request({
    url: `/service/frame-beam-crack/tasks/${encodeURIComponent(taskId)}`,
    method: 'get'
  })
}

export function listFrameBeamCrackHistory(query) {
  return request({
    url: '/service/frame-beam-crack/history',
    method: 'get',
    params: query
  })
}

export function getFrameBeamCrackHistory(taskId) {
  return request({
    url: `/service/frame-beam-crack/history/${encodeURIComponent(taskId)}`,
    method: 'get'
  })
}

export function deleteFrameBeamCrackHistory(taskId) {
  return request({
    url: `/service/frame-beam-crack/history/${encodeURIComponent(taskId)}`,
    method: 'delete'
  })
}

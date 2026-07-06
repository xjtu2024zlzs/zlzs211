import request from '@/utils/request'

export function importPartQuality(data) {
  return request({
    url: '/quality/part-quality/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    timeout: 300000
  })
}

export function importPartQualityApi(data) {
  return request({
    url: '/quality/part-quality/api/import',
    method: 'post',
    data,
    timeout: 300000
  })
}

export function downloadPartQualityTemplate() {
  return request({
    url: '/quality/part-quality/template',
    method: 'get',
    responseType: 'blob',
    timeout: 300000
  })
}

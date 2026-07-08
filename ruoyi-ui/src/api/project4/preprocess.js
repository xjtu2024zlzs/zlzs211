import request from '@/utils/request'

export function listCwruFiles() {
    return request({
        url: '/project4/bearing/preprocess/cwru-files',
        method: 'get'
    })
}

export function runPreprocess(data) {
    return request({
        url: '/project4/bearing/preprocess/run',
        method: 'post',
        data
    })
}
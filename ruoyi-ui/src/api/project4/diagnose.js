import request from '@/utils/request'

/**
 * 4 执行故障诊断
 * @param rawDataId 原始数据ID
 * @param data 诊断参数DTO
 */
export function runDiagnose(rawDataId, data) {
    return request({
        url: '/project4/bearing/diagnose/run',
        method: 'post',
        params: { rawDataId },
        data
    })
}
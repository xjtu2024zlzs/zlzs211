import request from '@/utils/request'

/**
 * 3 执行特征融合
 * @param preprocessId 预处理记录ID
 * @param data 融合参数DTO
 */
export function runFusion(preprocessId, data) {
    return request({
        url: '/project4/bearing/fusion/run',
        method: 'post',
        params: { preprocessId },
        data
    })
}

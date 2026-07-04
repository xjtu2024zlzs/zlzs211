import request from '@/utils/request'


/**
 * 2 执行数据增强
 * @param preprocessId 预处理记录ID
 * @param data 增强参数DTO
 */
export function runAugment(preprocessId, data) {
    return request({
        url: '/project4/bearing/augment/run',
        method: 'post',
        params: { preprocessId },
        data
    })
}


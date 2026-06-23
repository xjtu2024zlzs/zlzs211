import request from '@/utils/request'

// 查询根因分析结果列表
export function listResultofrc(query) {
return request({
url: '/project4/resultofrc/list',
method: 'get',
params: query
})
}

// 查询根因分析结果详细
export function getResultofrc(analysisId) {
return request({
url: '/project4/resultofrc/' + analysisId,
method: 'get'
})
}

// 新增根因分析结果
export function addResultofrc(data) {
return request({
url: '/project4/resultofrc',
method: 'post',
data: data,
timeout: 600000
})
}

// 修改根因分析结果
export function updateResultofrc(data) {
return request({
url: '/project4/resultofrc',
method: 'put',
data: data,
timeout: 600000
})
}

// 删除根因分析结果
export function delResultofrc(analysisId) {
return request({
url: '/project4/resultofrc/' + analysisId,
method: 'delete'
})
}

// 查询根因分析置信度统计：总体统计饼图
export function confidenceStats(query) {
return request({
url: '/project4/resultofrc/confidenceStats',
method: 'get',
params: query,
timeout: 600000
})
}

// 查询每个样本的各个根因置信度饼图数据
export function sampleConfidencePie(query) {
return request({
url: '/project4/resultofrc/sampleConfidencePie',
method: 'get',
params: query,
timeout: 600000
})
}

// 查询课题四向课题五输出的根因分析结果列表
export function outputListResultofrc(query) {
return request({
url: '/project4/resultofrc/output/list',
method: 'get',
params: query,
timeout: 600000
})
}

// 查询课题四向课题五输出的单条根因分析结果
export function outputInfoResultofrc(analysisId) {
return request({
url: '/project4/resultofrc/output/' + analysisId,
method: 'get',
timeout: 600000
})
}

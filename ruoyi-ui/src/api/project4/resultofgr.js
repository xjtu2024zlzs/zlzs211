import request from '@/utils/request'

// 查询故障诊断-诊断结果列表
export function listResultofgr(query) {
return request({
url: '/project4/resultofgr/list',
method: 'get',
params: query
})
}

// 查询故障诊断-诊断结果详细
export function getResultofgr(diagnosisId) {
return request({
url: '/project4/resultofgr/' + diagnosisId,
method: 'get'
})
}

// 新增故障诊断-诊断结果
export function addResultofgr(data) {
return request({
url: '/project4/resultofgr',
method: 'post',
data: data
})
}

// 修改故障诊断-诊断结果
export function updateResultofgr(data) {
return request({
url: '/project4/resultofgr',
method: 'put',
data: data
})
}

// 删除故障诊断-诊断结果
export function delResultofgr(diagnosisId) {
return request({
url: '/project4/resultofgr/' + diagnosisId,
method: 'delete'
})
}

// 测试 Java 后端调用 Python 算法服务
export function pythonTest() {
return request({
url: '/project4/resultofgr/pythonTest',
method: 'get',
timeout: 300000
})
}

// 执行模拟诊断并保存诊断结果
export function runMockDiagnosis() {
return request({
url: '/project4/resultofgr/runMock',
method: 'get',
timeout: 300000
})
}

// 根据原始数据文件执行诊断
export function runDiagnosisByFile(data) {
return request({
url: '/project4/resultofgr/runByFile',
method: 'post',
data: data,
timeout: 300000
})
}

// 故障根因分析
export function rootCauseAnalysis(diagnosisId) {
return request({
url: '/project4/resultofgr/rootCause/' + diagnosisId,
method: 'post',
timeout: 600000
})
}

// 执行故障诊断
export function runDiagnosis(data) {
return request({
url: '/project4/resultofgr/run',
method: 'post',
data: data,
timeout: 300000
})
}

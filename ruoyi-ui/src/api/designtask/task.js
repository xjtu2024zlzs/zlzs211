import request from '@/utils/request'

// ==================== ������� ====================

// ��ѯ�����б�
export function listTask(query) {
  return request({
    url: '/designtask/task/list',
    method: 'get',
    params: query
  })
}

// ��ѯ���񿴰�����
export function listTaskKanban(query) {
  return request({
    url: '/designtask/task/kanban',
    method: 'get',
    params: query
  })
}

// ��ѯ�ҵĴ�������
export function listMyTasks(query) {
  return request({
    url: '/designtask/task/my-tasks',
    method: 'get',
    params: query
  })
}

// ��ѯ��ʷ����
export function listTaskHistory(query) {
  return request({
    url: '/designtask/task/history',
    method: 'get',
    params: query
  })
}

// ��ѯ������ϸ
export function getTask(taskId) {
  return request({
    url: '/designtask/task/' + taskId,
    method: 'get'
  })
}

// ��������
export function addTask(data) {
  return request({
    url: '/designtask/task',
    method: 'post',
    data: data
  })
}

// �޸�����
export function updateTask(data) {
  return request({
    url: '/designtask/task',
    method: 'put',
    data: data
  })
}

// ɾ������
export function delTask(taskId) {
  return request({
    url: '/designtask/task/' + taskId,
    method: 'delete'
  })
}

// ȡ������
export function cancelTask(taskId, reason) {
  return request({
    url: '/designtask/task/' + taskId + '/cancel',
    method: 'put',
    data: { reason }
  })
}

// ��ȡ�������������б�
export function listTaskTypes() {
  return request({
    url: '/designtask/task/types',
    method: 'get'
  })
}

// ==================== ���̹��� ====================

// ��ѯ����ģ���б�
export function listFlowTemplate(query) {
  return request({
    url: '/designtask/flow/template/list',
    method: 'get',
    params: query
  })
}

// ��ѯ����ģ����ϸ
export function getFlowTemplate(templateId) {
  return request({
    url: '/designtask/flow/template/' + templateId,
    method: 'get'
  })
}

// ��������ģ��
export function addFlowTemplate(data) {
  return request({
    url: '/designtask/flow/template',
    method: 'post',
    data: data
  })
}

// �޸�����ģ��
export function updateFlowTemplate(data) {
  return request({
    url: '/designtask/flow/template',
    method: 'put',
    data: data
  })
}

// ɾ������ģ��
export function delFlowTemplate(templateId) {
  return request({
    url: '/designtask/flow/template/' + templateId,
    method: 'delete'
  })
}

// Ԥ������ģ��
export function previewFlowTemplate(templateId) {
  return request({
    url: '/designtask/flow/template/' + templateId + '/preview',
    method: 'get'
  })
}

// ��ѯ���̽ڵ��б�
export function listFlowNode(flowId) {
  return request({
    url: '/designtask/flow/node/list/' + flowId,
    method: 'get'
  })
}

// ��ѯ�ڵ���ϸ
export function getFlowNode(nodeId) {
  return request({
    url: '/designtask/flow/node/' + nodeId,
    method: 'get'
  })
}

// ���½ڵ�״̬
export function updateFlowNode(data) {
  return request({
    url: '/designtask/flow/node/' + data.nodeId,
    method: 'put',
    data: data
  })
}

// ��ѯ�ڵ�Э����¼
export function listNodeCollaborations(nodeId) {
  return request({
    url: '/designtask/flow/node/' + nodeId + '/collaborations',
    method: 'get'
  })
}

// �ύ�ڵ㴦�����
export function submitNodeResult(data) {
  return request({
    url: '/designtask/flow/node/' + data.nodeId + '/submit',
    method: 'post',
    data: data
  })
}

// ==================== ������ ====================

// ��ѯ�Ż�Ŀ���б�
export function listGoals(taskId) {
  return request({
    url: '/designtask/process/goals/' + taskId,
    method: 'get'
  })
}

// ����/ѡ���Ż�Ŀ��
export function addGoals(data) {
  return request({
    url: '/designtask/process/goals',
    method: 'post',
    data: data
  })
}

// �����Ż�Ŀ��
export function updateGoal(data) {
  return request({
    url: '/designtask/process/goals/' + data.goalId,
    method: 'put',
    data: data
  })
}

// ɾ���Ż�Ŀ��
export function delGoal(goalId) {
  return request({
    url: '/designtask/process/goals/' + goalId,
    method: 'delete'
  })
}

// ��ȡĿ��ģ��
export function getGoalTemplate(taskType) {
  return request({
    url: '/designtask/process/goals/template/' + taskType,
    method: 'get'
  })
}

// ��ѯԼ���б�
export function listConstraints(taskId) {
  return request({
    url: '/designtask/process/constraints/' + taskId,
    method: 'get'
  })
}

// ����/ѡ��Լ��
export function addConstraints(data) {
  return request({
    url: '/designtask/process/constraints',
    method: 'post',
    data: data
  })
}

// ����Լ��
export function updateConstraint(data) {
  return request({
    url: '/designtask/process/constraints/' + data.constraintId,
    method: 'put',
    data: data
  })
}

// ɾ��Լ��
export function delConstraint(constraintId) {
  return request({
    url: '/designtask/process/constraints/' + constraintId,
    method: 'delete'
  })
}

// ��ȡԼ��ģ��
export function getConstraintTemplate(taskType) {
  return request({
    url: '/designtask/process/constraints/template/' + taskType,
    method: 'get'
  })
}

// ��ȡ��׼Լ����
export function listStandardConstraints(query) {
  return request({
    url: '/designtask/process/constraints/standards',
    method: 'get',
    params: query
  })
}

// �����Ż�����
export function startOptimization(data) {
  return request({
    url: '/designtask/process/optimize',
    method: 'post',
    data: data
  })
}

// ����������֤
export function startSimulation(data) {
  return request({
    url: '/designtask/process/simulate',
    method: 'post',
    data: data
  })
}

// ��ѯ�Ż�����б�
export function listOptimizationResults(taskId) {
  return request({
    url: '/designtask/process/results/' + taskId,
    method: 'get'
  })
}

// ��ѯ�Ż��������
export function getOptimizationResult(resultId) {
  return request({
    url: '/designtask/process/results/' + resultId,
    method: 'get'
  })
}

// �ύ��������
export function submitForApproval(data) {
  return request({
    url: '/designtask/process/approve',
    method: 'post',
    data: data
  })
}

// ==================== �Ż����� ====================

// ��ȡ����ģ���б�
export function listProxyModels(query) {
  return request({
    url: '/designtask/optimize/models',
    method: 'get',
    params: query
  })
}

// ��ȡģ������
export function getProxyModel(modelId) {
  return request({
    url: '/designtask/optimize/models/' + modelId,
    method: 'get'
  })
}

// ѵ��ģ��
export function trainProxyModel(data) {
  return request({
    url: '/designtask/optimize/models/' + data.modelId + '/train',
    method: 'post',
    data: data
  })
}

// �����Ż�����
export function generateSolutions(data) {
  return request({
    url: '/designtask/optimize/generate',
    method: 'post',
    data: data
  })
}

// ��������������
export function evaluateFeasibility(data) {
  return request({
    url: '/designtask/optimize/evaluate',
    method: 'post',
    data: data
  })
}

// �����Ա�
export function compareSolutions(resultIds) {
  return request({
    url: '/designtask/optimize/compare',
    method: 'get',
    params: { resultIds }
  })
}

// ==================== ������֤ ====================

// ��ȡ���湤���б�
export function listSimulationTools(query) {
  return request({
    url: '/designtask/simulate/tools',
    method: 'get',
    params: query
  })
}

// �ύ��������
export function submitSimulation(data) {
  return request({
    url: '/designtask/simulate/submit',
    method: 'post',
    data: data
  })
}

// ��ѯ����״̬
export function getSimulationStatus(simId) {
  return request({
    url: '/designtask/simulate/status/' + simId,
    method: 'get'
  })
}

// ��ȡ������
export function getSimulationResult(simId) {
  return request({
    url: '/designtask/simulate/result/' + simId,
    method: 'get'
  })
}

// ��ȡ���ӻ�����
export function getSimulationVisualization(simId) {
  return request({
    url: '/designtask/simulate/visualization/' + simId,
    method: 'get'
  })
}

// ���ط��汨��
export function downloadSimulationReport(simId) {
  return request({
    url: '/designtask/simulate/report/' + simId,
    method: 'get',
    responseType: 'blob'
  })
}

// ==================== �������� ====================

// ��ѯ�������б�
export function listPendingApprovals(query) {
  return request({
    url: '/designtask/approval/pending',
    method: 'get',
    params: query
  })
}

// ��ѯ��������
export function getApproval(approvalId) {
  return request({
    url: '/designtask/approval/' + approvalId,
    method: 'get'
  })
}

// �ύ�������
export function submitApproval(data) {
  return request({
    url: '/designtask/approval/' + data.approvalId,
    method: 'post',
    data: data
  })
}

// ��ѯ������ʷ
export function listApprovalHistory(taskId) {
  return request({
    url: '/designtask/approval/history/' + taskId,
    method: 'get'
  })
}

// ==================== ��Դ���� ====================

// ��ѯ��Դ�б�
export function listResource(query) {
  return request({
    url: '/designtask/resource/list',
    method: 'get',
    params: query
  })
}

// ��ѯ��Դ����
export function getResource(resourceId) {
  return request({
    url: '/designtask/resource/' + resourceId,
    method: 'get'
  })
}

// �ϴ���Դ
export function uploadResource(data) {
  return request({
    url: '/designtask/resource',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ������Դ
export function updateResource(data) {
  return request({
    url: '/designtask/resource',
    method: 'put',
    data: data
  })
}

// ɾ����Դ
export function delResource(resourceId) {
  return request({
    url: '/designtask/resource/' + resourceId,
    method: 'delete'
  })
}

// ��ȡ��Դ������
export function listResourceCategory(type) {
  return request({
    url: '/designtask/resource/category/tree',
    method: 'get',
    params: { type }
  })
}

// ������Դ����
export function addResourceCategory(data) {
  return request({
    url: '/designtask/resource/category',
    method: 'post',
    data: data
  })
}

// ������Դ����
export function updateResourceCategory(data) {
  return request({
    url: '/designtask/resource/category/' + data.categoryId,
    method: 'put',
    data: data
  })
}

// ������Դ
export function downloadResource(resourceId) {
  return request({
    url: '/designtask/resource/download/' + resourceId,
    method: 'get',
    responseType: 'blob'
  })
}

// Ԥ����Դ
export function previewResource(resourceId) {
  return request({
    url: '/designtask/resource/preview/' + resourceId,
    method: 'get'
  })
}

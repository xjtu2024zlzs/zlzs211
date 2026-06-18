import request from '@/utils/request'

export function startPreventiveMaintenanceTask(data) {
  return request({
    url: '/service/preventive-maintenance/tasks',
    method: 'post',
    data
  })
}

export function getPreventiveMaintenanceTask(taskId) {
  return request({
    url: `/service/preventive-maintenance/tasks/${encodeURIComponent(taskId)}`,
    method: 'get'
  })
}

export function listPreventiveMaintenanceHistory(params) {
  return request({
    url: '/service/preventive-maintenance/history',
    method: 'get',
    params
  })
}

export function getPreventiveMaintenanceHistory(taskId) {
  return request({
    url: `/service/preventive-maintenance/history/${encodeURIComponent(taskId)}`,
    method: 'get'
  })
}

export function deletePreventiveMaintenanceHistory(taskId) {
  return request({
    url: `/service/preventive-maintenance/history/${encodeURIComponent(taskId)}`,
    method: 'delete'
  })
}

export function cancelPreventiveMaintenanceTask(taskId) {
  return request({
    url: `/service/preventive-maintenance/tasks/${encodeURIComponent(taskId)}/cancel`,
    method: 'post'
  })
}

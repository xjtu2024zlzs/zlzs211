import request from '@/utils/request'

export function listOpenApiDossiers(query) {
  return request({
    url: '/project1/dossier/openapi/dossiers',
    method: 'get',
    params: query
  })
}

export function listOpenApiProductDossiers(query) {
  return request({
    url: '/project1/dossier/openapi/product-dossiers',
    method: 'get',
    params: query
  })
}

export function getOpenApiDossier(instanceId) {
  return request({
    url: '/project1/dossier/openapi/dossiers/' + instanceId,
    method: 'get'
  })
}

export function getOpenApiProductDossier(instanceId) {
  return request({
    url: '/project1/dossier/openapi/product-dossiers/' + instanceId,
    method: 'get'
  })
}

export function listOpenApiDossierVersions(instanceId) {
  return request({
    url: '/project1/dossier/openapi/dossiers/' + instanceId + '/versions',
    method: 'get'
  })
}

export function getOpenApiDossierSummary(instanceId) {
  return request({
    url: '/project1/dossier/openapi/dossiers/' + instanceId + '/summary',
    method: 'get'
  })
}

export function getOpenApiProductStructure(productId, query) {
  return request({
    url: '/project1/dossier/openapi/products/' + productId + '/structure',
    method: 'get',
    params: query
  })
}

export function listOpenApiProductBom(productId, query) {
  return request({
    url: '/project1/dossier/openapi/products/' + productId + '/bom',
    method: 'get',
    params: query
  })
}

export function listOpenApiBomChildren(aircraftId, query) {
  return request({
    url: '/project1/dossier/openapi/aircraft/' + aircraftId + '/bom/children',
    method: 'get',
    params: query
  })
}

export function searchOpenApiBomNodes(aircraftId, query) {
  return request({
    url: '/project1/dossier/openapi/aircraft/' + aircraftId + '/bom/search',
    method: 'get',
    params: query
  })
}

export function getOpenApiBomNode(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId,
    method: 'get'
  })
}

export function getOpenApiBomPath(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/path',
    method: 'get'
  })
}

export function listOpenApiBomRelations(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/relations',
    method: 'get'
  })
}

export function listOpenApiDictionaries() {
  return request({
    url: '/project1/dossier/openapi/dictionaries',
    method: 'get'
  })
}

export function listOpenApiQualityCharacteristics() {
  return request({
    url: '/project1/dossier/openapi/quality-characteristics',
    method: 'get'
  })
}

export function listOpenApiNodeQualityCharacteristics(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/quality-characteristics',
    method: 'get'
  })
}

export function listOpenApiProductQualityFeatures(productId, query) {
  return request({
    url: '/project1/dossier/openapi/products/' + productId + '/quality-features',
    method: 'get',
    params: query
  })
}

export function listOpenApiDataStandards() {
  return request({
    url: '/project1/dossier/openapi/data-standards',
    method: 'get'
  })
}

export function listOpenApiNodeDesignData(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/design-data',
    method: 'get'
  })
}

export function listOpenApiNodeManufacturingData(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/manufacturing-data',
    method: 'get'
  })
}

export function listOpenApiManufacturingProcessData(query) {
  return request({
    url: '/project1/dossier/openapi/manufacturing/process-data',
    method: 'get',
    params: query
  })
}

export function listOpenApiProductManufacturingProcessData(productId, query) {
  return request({
    url: '/project1/dossier/openapi/products/' + productId + '/manufacturing-process-data',
    method: 'get',
    params: query
  })
}

export function listOpenApiNodeInspectionRecords(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/inspection-records',
    method: 'get'
  })
}

export function listOpenApiNodeInstallationRecords(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/installation-records',
    method: 'get'
  })
}

export function listOpenApiNodeServiceRecords(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/service-records',
    method: 'get'
  })
}

export function listOpenApiOperationMaintenanceData(query) {
  return request({
    url: '/project1/dossier/openapi/operation-maintenance/data',
    method: 'get',
    params: query
  })
}

export function listOpenApiProductOperationMaintenanceData(productId, query) {
  return request({
    url: '/project1/dossier/openapi/products/' + productId + '/operation-maintenance-data',
    method: 'get',
    params: query
  })
}

export function listOpenApiNodeFaultRecords(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/fault-records',
    method: 'get'
  })
}

export function listOpenApiNodeFiles(nodeId) {
  return request({
    url: '/project1/dossier/openapi/bom/nodes/' + nodeId + '/files',
    method: 'get'
  })
}

export function listOpenApiFilesAndModels(query) {
  return request({
    url: '/project1/dossier/openapi/files-models',
    method: 'get',
    params: query
  })
}

export function listOpenApiProductFilesAndModels(productId, query) {
  return request({
    url: '/project1/dossier/openapi/products/' + productId + '/files-models',
    method: 'get',
    params: query
  })
}

export function writebackOpenApiOptimizationResults(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/optimization-results',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiDesignOptimization(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/design-optimization',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiQualityMonitoringResults(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/quality-monitoring-results',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiQualitySupervision(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/quality-supervision',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiFaultDiagnosisResults(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/fault-diagnosis-results',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiFaultDiagnosis(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/fault-diagnosis',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiTraceabilityResults(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/traceability-results',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiTraceability(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/traceability',
    method: 'post',
    data: data
  })
}

export function writebackOpenApiDataCompletionResults(data) {
  return request({
    url: '/project1/dossier/openapi/writeback/data-completion-results',
    method: 'post',
    data: data
  })
}

export function listOpenApiChanges(query) {
  return request({
    url: '/project1/dossier/openapi/changes',
    method: 'get',
    params: query
  })
}

export function listOpenApiDataChangeNotifications(query) {
  return request({
    url: '/project1/dossier/openapi/data-change/notifications',
    method: 'get',
    params: query
  })
}

export function getOpenApiChange(changeId) {
  return request({
    url: '/project1/dossier/openapi/changes/' + changeId,
    method: 'get'
  })
}

export function addOpenApiSubscription(data) {
  return request({
    url: '/project1/dossier/openapi/subscriptions',
    method: 'post',
    data: data
  })
}

export function addOpenApiDataChangeSubscription(data) {
  return request({
    url: '/project1/dossier/openapi/data-change/subscriptions',
    method: 'post',
    data: data
  })
}

export function delOpenApiSubscription(subscriptionId) {
  return request({
    url: '/project1/dossier/openapi/subscriptions/' + subscriptionId,
    method: 'delete'
  })
}

export function getOpenApiAccessInfo() {
  return request({
    url: '/project1/dossier/openapi/access/current',
    method: 'get'
  })
}

export function checkOpenApiAccess(query) {
  return request({
    url: '/project1/dossier/openapi/access/check',
    method: 'get',
    params: query
  })
}

export function listOpenApiOperationLogs(query) {
  return request({
    url: '/project1/dossier/openapi/operation-logs',
    method: 'get',
    params: query
  })
}

export function getOpenApiOperationLog(logId) {
  return request({
    url: '/project1/dossier/openapi/operation-logs/' + logId,
    method: 'get'
  })
}

export function listOpenApiCatalog() {
  return request({
    url: '/project1/dossier/openapi/catalog',
    method: 'get'
  })
}

export function getOpenApiHealth() {
  return request({
    url: '/project1/dossier/openapi/health',
    method: 'get'
  })
}

export function getOpenApiVersion() {
  return request({
    url: '/project1/dossier/openapi/version',
    method: 'get'
  })
}

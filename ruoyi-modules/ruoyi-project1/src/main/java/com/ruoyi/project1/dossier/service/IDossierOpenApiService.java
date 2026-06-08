package com.ruoyi.project1.dossier.service;

import java.util.List;
import java.util.Map;

public interface IDossierOpenApiService
{
    public Map<String, Object> selectDossierPage(Map<String, Object> query);

    public Map<String, Object> selectDossierDetail(String instanceId);

    public List<Map<String, Object>> selectDossierVersions(String instanceId);

    public Map<String, Object> selectDossierSummary(String instanceId);

    public Map<String, Object> selectProductStructure(String productId, Map<String, Object> query);

    public List<Map<String, Object>> selectBomChildren(String aircraftId, String parentId);

    public List<Map<String, Object>> searchBomNodes(String aircraftId, String keyword);

    public Map<String, Object> selectBomNode(String nodeId);

    public List<Map<String, Object>> selectBomPath(String nodeId);

    public List<Map<String, Object>> selectBomRelations(String nodeId);

    public List<Map<String, Object>> selectDictionaries();

    public List<Map<String, Object>> selectQualityCharacteristics();

    public List<Map<String, Object>> selectNodeQualityCharacteristics(String nodeId);

    public List<Map<String, Object>> selectProductQualityFeatures(String productId, Map<String, Object> query);

    public List<Map<String, Object>> selectDataStandards();

    public List<Map<String, Object>> selectNodeLifecycleData(String nodeId, String dataType);

    public List<Map<String, Object>> selectManufacturingProcessData(Map<String, Object> query);

    public List<Map<String, Object>> selectOperationMaintenanceData(Map<String, Object> query);

    public List<Map<String, Object>> selectFilesAndModels(Map<String, Object> query);

    public List<Map<String, Object>> selectNodeFiles(String nodeId);

    public Map<String, Object> writebackResult(String resultType, Map<String, Object> request);

    public Map<String, Object> selectChangePage(Map<String, Object> query);

    public Map<String, Object> selectChangeDetail(String changeId);

    public Map<String, Object> selectAccessInfo();

    public Map<String, Object> checkAccess(Map<String, Object> query);

    public Map<String, Object> selectOperationLogPage(Map<String, Object> query);

    public Map<String, Object> selectOperationLogDetail(String logId);

    public Map<String, Object> createSubscription(Map<String, Object> request);

    public int deleteSubscription(String subscriptionId);

    public List<Map<String, Object>> selectCatalog();

    public Map<String, Object> selectHealth();

    public Map<String, Object> selectVersion();
}

package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface DossierOpenApiMapper
{
    public int selectDossierCount(Map<String, Object> params);

    public List<Map<String, Object>> selectDossierList(Map<String, Object> params);

    public Map<String, Object> selectDossierDetail(@Param("instanceId") String instanceId);

    public List<Map<String, Object>> selectDossierVersions(@Param("instanceId") String instanceId);

    public Map<String, Object> selectDossierSummary(@Param("instanceId") String instanceId);

    public Map<String, Object> selectBomNode(@Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectBomChildren(@Param("aircraftId") String aircraftId,
            @Param("parentId") String parentId);

    public List<Map<String, Object>> searchBomNodes(@Param("aircraftId") String aircraftId,
            @Param("keyword") String keyword);

    public List<Map<String, Object>> selectBomPath(@Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectBomRelations(@Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectNodeQualityCharacteristics(@Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectProductQualityFeatures(Map<String, Object> params);

    public List<Map<String, Object>> selectNodeLifecycleData(@Param("nodeId") String nodeId,
            @Param("dataType") String dataType);

    public List<Map<String, Object>> selectLifecycleDataByProduct(Map<String, Object> params);

    public List<Map<String, Object>> selectNodeFiles(@Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectFilesAndModels(Map<String, Object> params);

    public Map<String, Object> selectCurrentDossierByBomNode(@Param("nodeId") String nodeId);

    public int insertAnalysisTask(Map<String, Object> params);

    public int insertAnalysisResult(Map<String, Object> params);

    public int insertOperationLog(Map<String, Object> params);

    public int selectChangeCount(Map<String, Object> params);

    public List<Map<String, Object>> selectChangeList(Map<String, Object> params);

    public Map<String, Object> selectChangeDetail(@Param("changeId") String changeId);

    public int selectOperationLogCount(Map<String, Object> params);

    public List<Map<String, Object>> selectOperationLogList(Map<String, Object> params);

    public Map<String, Object> selectOperationLogDetail(@Param("logId") String logId);

    public int insertSubscription(Map<String, Object> params);

    public int disableSubscription(@Param("subscriptionId") String subscriptionId,
            @Param("updatedBy") String updatedBy);
}

package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface DossierGenerationMapper
{
    public List<Map<String, Object>> selectModelList();

    public List<Map<String, Object>> selectAircraftList(@Param("modelId") String modelId);

    public Map<String, Object> selectAircraftById(@Param("aircraftId") String aircraftId);

    public Map<String, Object> selectActiveTemplate();

    public List<Map<String, Object>> selectGenerationTemplates();

    public Map<String, Object> selectTemplateById(@Param("templateId") String templateId);

    public Map<String, Object> selectTemplateMetrics(@Param("templateId") String templateId);

    public List<Map<String, Object>> selectTemplateChapters(@Param("templateId") String templateId);

    public List<Map<String, Object>> selectChapterSources(@Param("templateId") String templateId);

    public List<Map<String, Object>> selectTemplateRules(@Param("templateId") String templateId);

    public List<Map<String, Object>> selectTemplateParams(@Param("templateId") String templateId);

    public Integer countRuleRows(Map<String, Object> params);

    public List<String> selectSourceTableColumns(@Param("tableName") String tableName);

    public List<Map<String, Object>> selectTemplateSourceRows(Map<String, Object> params);

    public List<Map<String, Object>> selectKeyNodeChain(@Param("aircraftId") String aircraftId);

    public Map<String, Object> selectGenerationDataFingerprint(@Param("aircraftId") String aircraftId);

    public Map<String, Object> selectNodeDataRollup(@Param("aircraftId") String aircraftId,
            @Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectNodeLifecycleSnapshotRows(@Param("aircraftId") String aircraftId,
            @Param("nodeId") String nodeId, @Param("partNumber") String partNumber);

    public List<Map<String, Object>> selectNodeDocumentSourceRows(@Param("aircraftId") String aircraftId,
            @Param("nodeId") String nodeId, @Param("partNumber") String partNumber,
            @Param("partInstanceId") String partInstanceId);

    public Map<String, Object> selectDossierInstance(@Param("aircraftId") String aircraftId, @Param("templateId") String templateId);

    public int insertDossierInstance(Map<String, Object> params);

    public int updateDossierInstanceIdentity(Map<String, Object> params);

    public Map<String, Object> selectLatestVersion(@Param("instanceId") String instanceId);

    public Map<String, Object> selectDuplicateVersion(Map<String, Object> params);

    public Map<String, Object> selectGeneratedVersionByPrecheckRunId(@Param("instanceId") String instanceId,
            @Param("precheckRunId") String precheckRunId);

    public int updateVersionContentHash(Map<String, Object> params);

    public int updateDossierVersionSummary(Map<String, Object> params);

    public int clearCurrentVersion(@Param("instanceId") String instanceId);

    public int insertDossierVersion(Map<String, Object> params);

    public int updateDossierInstanceCurrent(Map<String, Object> params);

    public int insertInspectionRun(Map<String, Object> params);

    public int insertDataSnapshot(Map<String, Object> params);

    public int insertGenerationJob(Map<String, Object> params);

    public int updateGenerationJobVersion(Map<String, Object> params);

    public int finishGenerationJob(Map<String, Object> params);

    public String selectDocumentCategoryId(@Param("categoryName") String categoryName);

    public int insertStructureNode(Map<String, Object> params);

    public int insertContentItem(Map<String, Object> params);

    public int insertDocumentFileAsset(Map<String, Object> params);

    public int insertDocumentFileRelation(Map<String, Object> params);

    public int insertOperationLog(Map<String, Object> params);

    public Integer countDocumentEntries(@Param("versionId") String versionId);

    public Map<String, Object> selectJob(@Param("jobId") String jobId);

    public Map<String, Object> selectJobOutput(@Param("jobId") String jobId);
}

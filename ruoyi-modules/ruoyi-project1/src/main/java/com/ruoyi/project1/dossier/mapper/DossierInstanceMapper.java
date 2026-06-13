package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface DossierInstanceMapper
{
    public int selectInstanceCount(Map<String, Object> params);

    public List<Map<String, Object>> selectInstanceList(Map<String, Object> params);

    public Map<String, Object> selectInstanceSummary(Map<String, Object> params);

    public Map<String, Object> selectInstanceDetail(@Param("instanceId") String instanceId);

    public List<Map<String, Object>> selectVersionList(@Param("instanceId") String instanceId);

    public List<Map<String, Object>> selectJobList(@Param("instanceId") String instanceId);

    public List<Map<String, Object>> selectFileList(@Param("instanceId") String instanceId, @Param("versionId") String versionId);

    public int updateInstanceStatus(Map<String, Object> params);

    public int updateCurrentVersionPublished(@Param("instanceId") String instanceId);

    public int clearInstanceCurrentVersion(@Param("instanceId") String instanceId);

    public int deleteVersionDataIssues(Map<String, Object> params);

    public int deleteVersionDataSupportItems(Map<String, Object> params);

    public int deleteVersionFileRelations(Map<String, Object> params);

    public int deleteVersionFileCategories(Map<String, Object> params);

    public int deleteVersionCompletenessSummaries(Map<String, Object> params);

    public int deleteVersionContentItems(Map<String, Object> params);

    public int deleteVersionDiffs(Map<String, Object> params);

    public int deleteVersionDataRelationNodes(Map<String, Object> params);

    public int deleteVersionSearchIndexRecords(Map<String, Object> params);

    public int deleteVersionOperationLogs(Map<String, Object> params);

    public int deleteVersionExportFiles(Map<String, Object> params);

    public int deleteVersionExportJobs(Map<String, Object> params);

    public int deleteVersionGenerationJobItems(Map<String, Object> params);

    public int deleteVersionGenerationJobs(Map<String, Object> params);

    public int deleteVersionInspectionRuns(Map<String, Object> params);

    public int deleteVersionSnapshots(Map<String, Object> params);

    public int deleteVersionStructureNodes(Map<String, Object> params);

    public int deleteVersion(Map<String, Object> params);

    public int deleteInstanceDataIssues(Map<String, Object> params);

    public int deleteInstanceDataSupportItems(Map<String, Object> params);

    public int deleteInstanceDataRelationEdges(Map<String, Object> params);

    public int deleteInstanceDataRelationNodes(Map<String, Object> params);

    public int deleteInstanceMonitoringRuns(Map<String, Object> params);

    public int deleteInstanceUpdateRequests(Map<String, Object> params);

    public int deleteInstanceFileRelations(Map<String, Object> params);

    public int deleteInstanceFileCategories(Map<String, Object> params);

    public int deleteInstanceCompletenessSummaries(Map<String, Object> params);

    public int deleteInstanceContentItems(Map<String, Object> params);

    public int deleteInstanceDiffs(Map<String, Object> params);

    public int deleteInstanceSearchIndexRecords(Map<String, Object> params);

    public int deleteInstanceQaSessions(Map<String, Object> params);

    public int deleteInstanceOperationLogs(Map<String, Object> params);

    public int deleteInstanceExportFiles(Map<String, Object> params);

    public int deleteInstanceExportJobs(Map<String, Object> params);

    public int deleteInstanceGenerationJobItems(Map<String, Object> params);

    public int deleteInstanceGenerationJobs(Map<String, Object> params);

    public int deleteInstanceInspectionRuns(Map<String, Object> params);

    public int deleteInstanceSnapshots(Map<String, Object> params);

    public int deleteInstanceStructureNodes(Map<String, Object> params);

    public int deleteInstanceVersions(Map<String, Object> params);

    public int deleteInstance(Map<String, Object> params);

    public int insertExportJob(Map<String, Object> params);

    public int insertExportFile(Map<String, Object> params);
}

package com.ruoyi.project1.dossier.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface DossierDetailMapper
{
    public Map<String, Object> selectLatestInstance(@Param("aircraftId") String aircraftId,
            @Param("instanceId") String instanceId);

    public Map<String, Object> selectVersion(@Param("instanceId") String instanceId,
            @Param("versionId") String versionId);

    public List<Map<String, Object>> selectVersionList(@Param("instanceId") String instanceId);

    public Map<String, Object> selectBomMetrics(@Param("aircraftId") String aircraftId);

    public Map<String, Object> selectRootBomNode(@Param("aircraftId") String aircraftId);

    public Map<String, Object> selectBomNode(@Param("nodeId") String nodeId);

    public List<Map<String, Object>> selectBomChildren(@Param("aircraftId") String aircraftId,
            @Param("parentId") String parentId);

    public List<Map<String, Object>> searchBomNodes(@Param("aircraftId") String aircraftId,
            @Param("keyword") String keyword);

    public List<Map<String, Object>> selectBomPath(@Param("nodeId") String nodeId);

    public Map<String, Object> selectStructureBomMetrics(@Param("versionId") String versionId);

    public Map<String, Object> selectRootStructureBomNode(@Param("versionId") String versionId);

    public Map<String, Object> selectStructureBomNode(@Param("versionId") String versionId,
            @Param("bomNodeId") String bomNodeId);

    public List<Map<String, Object>> selectStructureBomChildren(@Param("versionId") String versionId,
            @Param("parentId") String parentId);

    public List<Map<String, Object>> searchStructureBomNodes(@Param("versionId") String versionId,
            @Param("keyword") String keyword);

    public List<Map<String, Object>> selectStructureBomPath(@Param("versionId") String versionId,
            @Param("bomNodeId") String bomNodeId);

    public Map<String, Object> selectStructureNodeByBom(@Param("versionId") String versionId,
            @Param("bomNodeId") String bomNodeId);

    public List<Map<String, Object>> selectStructureNodes(@Param("versionId") String versionId);

    public List<Map<String, Object>> selectContentItems(@Param("versionId") String versionId,
            @Param("bomNodeId") String bomNodeId);

    public List<Map<String, Object>> selectDocuments(@Param("versionId") String versionId,
            @Param("structureNodeId") String structureNodeId);

    public List<Map<String, Object>> selectAllDocuments(@Param("versionId") String versionId);

    public List<Map<String, Object>> selectDataSources(@Param("versionId") String versionId);

    public List<Map<String, Object>> selectOperationLogs(@Param("instanceId") String instanceId,
            @Param("versionId") String versionId);
}

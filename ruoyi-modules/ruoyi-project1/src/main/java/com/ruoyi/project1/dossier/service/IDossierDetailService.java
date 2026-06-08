package com.ruoyi.project1.dossier.service;

import java.util.List;
import java.util.Map;

public interface IDossierDetailService
{
    public Map<String, Object> selectCurrentDetail(String aircraftId, String instanceId, String versionId);

    public Map<String, Object> selectNodeDetail(String instanceId, String versionId, String bomNodeId);

    public List<Map<String, Object>> selectBomChildren(String aircraftId, String parentId, String instanceId,
            String versionId);

    public List<Map<String, Object>> searchBomNodes(String aircraftId, String keyword, String instanceId,
            String versionId);

    public List<Map<String, Object>> selectBomPath(String nodeId, String versionId);
}

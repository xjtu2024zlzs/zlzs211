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

    public int insertExportJob(Map<String, Object> params);

    public int insertExportFile(Map<String, Object> params);
}

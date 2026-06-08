package com.ruoyi.project1.dossier.service;

import java.util.List;
import java.util.Map;

public interface IDossierInstanceService
{
    public Map<String, Object> selectInstancePage(Map<String, Object> query);

    public Map<String, Object> selectInstanceSummary(Map<String, Object> query);

    public Map<String, Object> selectInstanceDetail(String instanceId);

    public List<Map<String, Object>> selectVersionList(String instanceId);

    public List<Map<String, Object>> selectJobList(String instanceId);

    public List<Map<String, Object>> selectFileList(String instanceId, String versionId);

    public void publishInstance(String instanceId);

    public void archiveInstance(String instanceId);
}

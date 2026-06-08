package com.ruoyi.project1.dossier.service;

import java.util.List;
import java.util.Map;

public interface IDossierGenerationService
{
    public List<Map<String, Object>> selectModelList();

    public List<Map<String, Object>> selectAircraftList(String modelId);

    public Map<String, Object> selectActiveTemplate();

    public Map<String, Object> prepareGeneration(String aircraftId);

    public Map<String, Object> precheck(Map<String, Object> request);

    public Map<String, Object> startGeneration(Map<String, Object> request);

    public Map<String, Object> selectJob(String jobId);

    public List<Map<String, Object>> selectJobLogs(String jobId);

    public Map<String, Object> selectJobOutput(String jobId);
}

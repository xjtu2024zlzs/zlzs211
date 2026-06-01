package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.assembler.algorithm.FeatureReqAssembler;
import com.ruoyi.project3.assembler.algorithm.KeyProcessReqAssembler;
import com.ruoyi.project3.client.PythonAlgorithmClient;
import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.domain.algorithm.DegradationParams;
import com.ruoyi.project3.domain.algorithm.DegradationTaskRequest;
import com.ruoyi.project3.domain.algorithm.FaultIdentifyStartRequest;
import com.ruoyi.project3.domain.algorithm.feature.FeatureReq;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import com.ruoyi.project3.mapper.FaultIdenFilePackageMapper;
import com.ruoyi.project3.mapper.FaultIdenSampleMapper;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.service.FaultIdentifyService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FaultIdentifyServiceImpl implements FaultIdentifyService
{
    private static final Logger log = LoggerFactory.getLogger(FaultIdentifyServiceImpl.class);

    private static final String TASK_STATUS_PENDING = "PENDING";
    private static final String TASK_STATUS_RUNNING = "RUNNING";
    private static final String TASK_STATUS_SUCCESS = "SUCCESS";
    private static final String TASK_STATUS_FAILED = "FAILED";
    private static final String TASK_TYPE_FEATURE_ANALYSIS = "FEATURE_ANALYSIS";
    private static final String TASK_TYPE_FEATURE_PROCESSING = "FEATURE_PROCESSING";
    private static final String TASK_TYPE_DEGRADATION_DETECT = "EARLY_DEGRADATION_POINT_DETECT";
    private static final String TASK_TYPE_KEY_PROCESS_IDENTIFY = "KEY_PROCESS_IDENTIFY";
    private static final String ALG_FEATURE = "FEATURE_ANALYSIS";
    private static final String ALG_FEATURE_PROCESSING = "FEATURE_PROCESSING";
    private static final String ALG_DEGRADE = TASK_TYPE_DEGRADATION_DETECT;
    private static final String ALG_KEY_PROCESS = "KEY_PROCESS_IDENTIFY";
    private static final String NAME_KEY_PROCESS = "KEY_PROCESS_IDENTIFY";
    private static final String NAME_FEATURE = "FEATURE_ANALYSIS";
    private static final String NAME_DEGRADE = "DEGRADATION_DETECT";


    private static final String DEGRADATION_AUTH_CODE = "ALGO_FAULT_IDENTIFICATION_EXECUTE";
    private static final String DEGRADATION_ALGORITHM_VERSION = "v1.0";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TASK_TIME_PATTERN = "yyyyMMddHHmmssSSS";

    private final ConcurrentMap<String, Map<String, Object>> taskStatusStore = new ConcurrentHashMap<>();

    @Resource
    private AlgTaskMapper algTaskMapper;

    @Resource
    private FaultIdenFilePackageMapper faultIdenFilePackageMapper;

    @Resource
    private FaultIdenSampleMapper faultIdenSampleMapper;

    @Resource
    private FaultIdenFileProps faultIdenFileProps;

    @Resource
    private PythonAlgorithmClient pythonAlgorithmClient;

    @Resource
    private FeatureReqAssembler featureReqAssembler;

    @Resource
    private KeyProcessReqAssembler keyProcessReqAssembler;

    @Override
    public PageRows get_import_record(String keyword, Integer page_num, Integer page_size)
    {
        PageRows pageRows = new PageRows();
        pageRows.set_rows(Collections.emptyList());
        pageRows.set_total(0L);
        return pageRows;
    }

    @Override
    public PageRows get_identify_result(String keyword, String taskType, String status, Integer page_num, Integer page_size)
    {
        int p = page_num == null || page_num < 1 ? 1 : page_num;
        int s = page_size == null || page_size < 1 ? 10 : Math.min(page_size, 100);
        int offset = (p - 1) * s;

        List<AlgTaskResult> list = algTaskMapper.getIdentifyResult(
                to_text(keyword),
                to_text(taskType),
                to_text(status),
                offset,
                s
        );
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (AlgTaskResult item : list)
        {
            rows.add(identifyRow(item));
        }

        PageRows pageRows = new PageRows();
        pageRows.set_rows(rows);
        pageRows.set_total(algTaskMapper.countIdentifyResult(to_text(keyword), to_text(taskType), to_text(status)));
        return pageRows;
    }

    @Override
    public PageRows get_feature_result(String keyword, String status, Integer page_num, Integer page_size)
    {
        PageRows pageRows = new PageRows();
        pageRows.set_rows(Collections.emptyList());
        pageRows.set_total(0L);
        return pageRows;
    }

    @Override
    public PageRows get_degradation_result(String keyword, String status, Integer page_num, Integer page_size)
    {
        PageRows pageRows = new PageRows();
        pageRows.set_rows(Collections.emptyList());
        pageRows.set_total(0L);
        return pageRows;
    }

    @Override
    public PageRows get_key_process_result(String keyword, String status, String targetType, String targetId, Integer page_num, Integer page_size)
    {
        int p = page_num == null || page_num < 1 ? 1 : page_num;
        int s = page_size == null || page_size < 1 ? 10 : Math.min(page_size, 100);
        int offset = (p - 1) * s;
        List<AlgTaskResult> list = algTaskMapper.getKeyProcessResult(
                to_text(keyword),
                to_text(status),
                to_text(targetType),
                to_text(targetId),
                offset,
                s
        );
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (AlgTaskResult item : list)
        {
            rows.add(keyRow(item));
        }
        PageRows pageRows = new PageRows();
        pageRows.set_rows(rows);
        pageRows.set_total(algTaskMapper.countKeyProcessResult(to_text(keyword), to_text(status), to_text(targetType), to_text(targetId)));
        return pageRows;
    }

    @Override
    public Map<String, Object> start_analysis(FaultIdentifyStartRequest request_data)
    {
        Map<String, Object> reqMap = normReq(request_data);
        checkReq(reqMap);

        Map<String, Object> task = newFeatTask(reqMap);
        String taskId = str(task, "task_id");
        String bizId = str(task, "business_object_id");
        log.info("Fault identify stage1 created, taskId={}, flowTaskId={}, taskType={}, bizId={}",
                taskId, task.get("feature_analysis_task_id"), task.get("task_type"), bizId);
        log.info("开始特征分析任务，任务ID={}，业务对象ID={}", taskId, bizId);

        try
        {
            FeatureReq py = featureReqAssembler.assemble(request_data, taskId);
            task.put("request_id", py.getRequestId());
            task.put("algorithm_params", py.getParams());
            cacheTask(task, null, null);
            insAlg(newFeat(task, py));

            setStatus(task, TASK_STATUS_RUNNING);
            cacheTask(task, null, null);
            runFeat(task, json(py));
            log.info("调用Python特征分析服务，任务ID={}", taskId);

            Map<String, Object> res = pythonAlgorithmClient.runFeature(py);
            log.info(
                    "收到Python特征分析响应，任务ID={}，状态={}",
                    taskId,
                    statusText(res)
            );
            Map<String, Object> ret = normFeat(res, taskId);
            setStatus(task, str(ret, "status") == null ? TASK_STATUS_SUCCESS : str(ret, "status"));
            cacheTask(task, ret, null);
            okAlg(
                    taskId,
                    json(res),
                    featSum(ret),
                    featVal(ret),
                    null
            );
            log.info("特征分析任务完成，任务ID={}，状态={}", taskId, TASK_STATUS_SUCCESS);
            return startResp(task, ret);
        }
        catch (ServiceException e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String err = e.getMessage() == null
                    ? "data analysis completed"
                    : e.getMessage();
            cacheTask(task, null, err);
            failAlg(taskId, err);
            log.warn("特征分析任务失败，任务ID={}，状态={}，错误={}", taskId, TASK_STATUS_FAILED, err);
            throw e;
        }
        catch (Exception e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String err = "特征分析任务启动失败";
            cacheTask(task, null, err);
            failAlg(taskId, err);
            log.error("特征分析任务失败，任务ID={}，状态={}，错误={}", taskId, TASK_STATUS_FAILED, e.getMessage(), e);
            throw new ServiceException(err);
        }
    }

    @Override
    public Map<String, Object> get_task(String task_id)
    {
        if (task_id == null || task_id.trim().isEmpty())
        {
            throw new ServiceException("任务ID不能为空");
        }

        String taskId = task_id.trim();
        Map<String, Object> taskStatusSnapshot = taskId.startsWith("FY") ? dbTaskSnapshot(taskId) : taskStatusStore.get(taskId);
        if (taskStatusSnapshot == null)
        {
            taskStatusSnapshot = taskId.startsWith("FY") ? taskStatusStore.get(taskId) : dbTaskSnapshot(taskId);
        }
        if (taskStatusSnapshot == null)
        {
            throw new ServiceException("任务不存在");
        }
        return new LinkedHashMap<>(taskStatusSnapshot);
    }

    @Override
    public Map<String, Object> delete_identify_result(String flowTaskId, Map<String, Object> options)
    {
        String taskId = to_text(flowTaskId);
        if (taskId == null)
        {
            throw new ServiceException("任务ID不能为空");
        }

        boolean deleteAlgorithmArtifacts = boolOption(options, "deleteAlgorithmArtifacts", "delete_algorithm_artifacts");
        boolean deletePackagedFiles = boolOption(options, "deletePackagedFiles", "delete_packaged_files");
        boolean deleteDataSources = boolOption(options, "deleteDataSources", "delete_data_sources");
        List<AlgTaskResult> tasks = algTaskMapper.getTasksByFlowTaskId(taskId);
        List<String> stageTaskIds = stageTaskIds(tasks);
        List<FaultIdenFilePackage> packages = stageTaskIds.isEmpty()
                ? Collections.emptyList()
                : faultIdenFilePackageMapper.selectByTaskIds(stageTaskIds);

        int deletedAlgorithmFiles = deleteAlgorithmArtifacts ? deleteAlgorithmArtifactFiles(tasks) : 0;
        int deletedPackagedFiles = deletePackagedFiles ? deletePackagedFiles(packages) : 0;
        int deletedSourceFiles = 0;
        int deletedSamples = 0;
        int deletedPackages = 0;
        if (deleteDataSources)
        {
            Set<Long> sampleIds = sampleIdsFromTasksAndPackages(tasks, packages);
            if (!sampleIds.isEmpty())
            {
                List<FaultIdenSampleFile> samples = faultIdenSampleMapper.selectSamplesByIds(new ArrayList<>(sampleIds), null);
                deletedSourceFiles = deleteSourceFiles(samples);
                deletedSamples = faultIdenSampleMapper.deleteSamplesByIds(new ArrayList<>(sampleIds));
            }
            List<Long> packageIds = packageIds(packages);
            if (!packageIds.isEmpty())
            {
                deletedPackages = faultIdenFilePackageMapper.deleteByIds(packageIds);
            }
        }

        int deleted = algTaskMapper.deleteByFlowTaskId(taskId);
        taskStatusStore.remove(taskId);
        for (AlgTaskResult task : tasks == null ? Collections.<AlgTaskResult>emptyList() : tasks)
        {
            taskStatusStore.remove(task.getTaskId());
        }

        log.info("Delete fault identify history, flowTaskId={}, stageCount={}, deletedCount={}, deleteAlgorithmArtifacts={}, deletePackagedFiles={}, deleteDataSources={}",
                taskId, tasks == null ? 0 : tasks.size(), deleted, deleteAlgorithmArtifacts, deletePackagedFiles, deleteDataSources);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("task_id", taskId);
        result.put("deleted", deleted);
        result.put("deletedCount", deleted);
        result.put("deleted_count", deleted);
        result.put("deletedAlgorithmFiles", deletedAlgorithmFiles);
        result.put("deleted_algorithm_files", deletedAlgorithmFiles);
        result.put("deletedPackagedFiles", deletedPackagedFiles);
        result.put("deleted_packaged_files", deletedPackagedFiles);
        result.put("deletedSourceFiles", deletedSourceFiles);
        result.put("deleted_source_files", deletedSourceFiles);
        result.put("deletedSamples", deletedSamples);
        result.put("deleted_samples", deletedSamples);
        result.put("deletedPackages", deletedPackages);
        result.put("deleted_packages", deletedPackages);
        return result;
    }

    private boolean boolOption(Map<String, Object> options, String camelKey, String snakeKey)
    {
        if (options == null)
        {
            return false;
        }
        Object value = options.get(camelKey);
        if (value == null)
        {
            value = options.get(snakeKey);
        }
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private List<String> stageTaskIds(List<AlgTaskResult> tasks)
    {
        List<String> ids = new ArrayList<>();
        for (AlgTaskResult task : tasks == null ? Collections.<AlgTaskResult>emptyList() : tasks)
        {
            String id = to_text(task.getTaskId());
            if (id != null && !ids.contains(id))
            {
                ids.add(id);
            }
        }
        return ids;
    }

    private List<Long> packageIds(List<FaultIdenFilePackage> packages)
    {
        List<Long> ids = new ArrayList<>();
        for (FaultIdenFilePackage pack : packages == null ? Collections.<FaultIdenFilePackage>emptyList() : packages)
        {
            if (pack.getId() != null && !ids.contains(pack.getId()))
            {
                ids.add(pack.getId());
            }
        }
        return ids;
    }

    private Set<Long> sampleIdsFromTasksAndPackages(List<AlgTaskResult> tasks, List<FaultIdenFilePackage> packages)
    {
        Set<Long> ids = new LinkedHashSet<>();
        for (AlgTaskResult task : tasks == null ? Collections.<AlgTaskResult>emptyList() : tasks)
        {
            collectSampleIdsFromRequest(parseJsonObject(task.getReqJson()), ids);
        }
        for (FaultIdenFilePackage pack : packages == null ? Collections.<FaultIdenFilePackage>emptyList() : packages)
        {
            collectSampleIdValue(pack.getSelectedSampleIds(), ids);
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private void collectSampleIdsFromRequest(Object value, Set<Long> ids)
    {
        if (value == null)
        {
            return;
        }
        if (value instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String key = entry.getKey();
                if ("sampleIds".equals(key) || "sample_ids".equals(key) || "selectedSampleIds".equals(key) || "selected_sample_ids".equals(key))
                {
                    collectSampleIdValue(entry.getValue(), ids);
                }
                else
                {
                    collectSampleIdsFromRequest(entry.getValue(), ids);
                }
            }
        }
    }

    private void collectSampleIdValue(Object value, Set<Long> ids)
    {
        if (value == null)
        {
            return;
        }
        if (value instanceof Iterable)
        {
            for (Object item : (Iterable<?>) value)
            {
                collectSampleIdValue(item, ids);
            }
            return;
        }
        String text = to_text(value);
        for (String part : text.split(","))
        {
            try
            {
                String item = part.trim();
                if (!item.isEmpty())
                {
                    ids.add(Long.valueOf(item));
                }
            }
            catch (NumberFormatException ignored)
            {
            }
        }
    }

    private int deleteAlgorithmArtifactFiles(List<AlgTaskResult> tasks)
    {
        Set<String> paths = new LinkedHashSet<>();
        for (AlgTaskResult task : tasks == null ? Collections.<AlgTaskResult>emptyList() : tasks)
        {
            collectArtifactPaths(parseJsonObject(task.getResJson()), paths);
        }
        return deleteFiles(paths, true);
    }

    @SuppressWarnings("unchecked")
    private void collectArtifactPaths(Object value, Set<String> paths)
    {
        if (value == null)
        {
            return;
        }
        if (value instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String key = entry.getKey();
                Object item = entry.getValue();
                if (isArtifactPathKey(key))
                {
                    String path = to_text(item);
                    if (path != null)
                    {
                        paths.add(path);
                    }
                }
                collectArtifactPaths(item, paths);
            }
            return;
        }
        if (value instanceof Iterable)
        {
            for (Object item : (Iterable<?>) value)
            {
                collectArtifactPaths(item, paths);
            }
        }
    }

    private boolean isArtifactPathKey(String key)
    {
        if (key == null)
        {
            return false;
        }
        String lower = key.toLowerCase();
        if ("sourcefile".equals(lower) || "source_file".equals(lower) || lower.endsWith("url"))
        {
            return false;
        }
        return lower.endsWith("path") || lower.endsWith("file") || lower.endsWith("filepath") || lower.endsWith("filename");
    }

    private int deletePackagedFiles(List<FaultIdenFilePackage> packages)
    {
        Set<String> paths = new LinkedHashSet<>();
        for (FaultIdenFilePackage pack : packages == null ? Collections.<FaultIdenFilePackage>emptyList() : packages)
        {
            String path = to_text(pack.getFilePath());
            if (path != null)
            {
                paths.add(path);
            }
        }
        return deleteFiles(paths, true);
    }

    private int deleteSourceFiles(List<FaultIdenSampleFile> samples)
    {
        Set<String> paths = new LinkedHashSet<>();
        for (FaultIdenSampleFile sample : samples == null ? Collections.<FaultIdenSampleFile>emptyList() : samples)
        {
            String path = to_text(sample.getSourceFile());
            if (path != null)
            {
                paths.add(path);
            }
        }
        return deleteFiles(paths, false);
    }

    private int deleteFiles(Set<String> paths, boolean allowDirectory)
    {
        int deleted = 0;
        for (String item : paths == null ? Collections.<String>emptySet() : paths)
        {
            Path path = safeDeletePath(item);
            if (path == null || !Files.exists(path))
            {
                continue;
            }
            try
            {
                if (Files.isDirectory(path))
                {
                    if (!allowDirectory)
                    {
                        continue;
                    }
                    try (java.util.stream.Stream<Path> stream = Files.walk(path))
                    {
                        List<Path> targets = stream.sorted(Comparator.reverseOrder()).toList();
                        for (Path target : targets)
                        {
                            Files.deleteIfExists(target);
                        }
                    }
                }
                else
                {
                    Files.deleteIfExists(path);
                }
                deleted++;
            }
            catch (IOException e)
            {
                log.warn("Delete fault identify file failed, path={}", path, e);
            }
        }
        return deleted;
    }

    private Path safeDeletePath(String text)
    {
        String value = to_text(text);
        if (value == null || value.startsWith("http://") || value.startsWith("https://"))
        {
            return null;
        }
        Path path = Paths.get(value).toAbsolutePath().normalize();
        if (isUnderRoot(path, faultIdenFileProps.getSourceRoot()) || isUnderRoot(path, faultIdenFileProps.getExportDir()))
        {
            return path;
        }
        log.warn("Skip deleting file outside configured roots, path={}", path);
        return null;
    }

    private boolean isUnderRoot(Path path, String rootText)
    {
        String rootValue = to_text(rootText);
        if (rootValue == null)
        {
            return false;
        }
        Path root = Paths.get(rootValue).toAbsolutePath().normalize();
        return path.startsWith(root);
    }

    @Override
    public Map<String, Object> submit_feature(String request_body)
    {
        return submit_feature_processing(request_body);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> submit_feature_processing(String request_body)
    {
        if (request_body == null || request_body.trim().isEmpty())
        {
            throw new ServiceException("请求体为空");
        }

        Map<String, Object> rawRequest;
        FaultIdentifyStartRequest featureTaskRequest;
        try
        {
            rawRequest = JSON.parseObject(request_body, Map.class);
            featureTaskRequest = JSON.parseObject(request_body, FaultIdentifyStartRequest.class);
        }
        catch (Exception parseException)
        {
            throw new ServiceException("特征处理请求参数不是合法 JSON");
        }

        Map<String, Object> reqMap = normReq(featureTaskRequest);
        Map<String, Object> params = (Map<String, Object>) reqMap.get("algorithm_params");
        if (params == null)
        {
            params = Collections.emptyMap();
        }

        AlgTaskResult sourceTask = resolveFeatureDataTask(rawRequest, reqMap);
        Map<String, Object> sourceResult = parseJsonObject(sourceTask.getResJson());
        String datasetId = to_text(findVal(sourceResult, 0, "datasetId", "dataset_id"));
        String combinedDataPath = to_text(findVal(sourceResult, 0, "combinedDataPath", "combined_data_path"));
        Object samplingFrequency = findVal(sourceResult, 0, "samplingFrequency", "samplingRate", "sampling_frequency", "sampling_rate");
        if (samplingFrequency == null)
        {
            samplingFrequency = pickVal(params, "samplingFrequency", "samplingRate", "sampling_frequency", "sampling_rate");
        }
        if (datasetId == null || combinedDataPath == null)
        {
            throw new ServiceException("特征处理缺少数据分析结果中的 datasetId 或 combinedDataPath");
        }

        String flowTaskId = flowTaskId(sourceTask);
        Map<String, Object> task = newFeatureProcessingTask(reqMap, sourceTask.getTaskId(), flowTaskId);
        String taskId = str(task, "task_id");
        log.info("Fault identify stage2 created, taskId={}, sourceTaskId={}, flowTaskId={}, taskType={}",
                taskId, sourceTask.getTaskId(), task.get("feature_analysis_task_id"), task.get("task_type"));
        Map<String, Object> py = new LinkedHashMap<>();
        py.put("taskId", taskId);
        py.put("taskType", TASK_TYPE_FEATURE_PROCESSING);
        py.put("sourceTaskId", sourceTask.getTaskId());
        py.put("datasetId", datasetId);
        py.put("combinedDataPath", combinedDataPath);
        py.put("samplingFrequency", samplingFrequency == null ? 25600 : samplingFrequency);
        Object windowSize = pickVal(params, "windowSize", "window_size");
        Object overlap = pickVal(params, "overlapPercent", "overlapRate", "overlap_percent", "overlap_rate");
        py.put("windowSize", windowSize == null ? 1.0D : windowSize);
        py.put("overlapPercent", overlap == null ? 50.0D : overlap);

        try
        {
            cacheTask(task, null, null);
            insAlg(newFeatureProcessing(task, sourceTask));
            setStatus(task, TASK_STATUS_RUNNING);
            cacheTask(task, null, null);
            runAlg(taskId, json(py));
            log.info("调用Python特征处理服务，任务ID={}，源任务ID={}，数据集ID={}", taskId, sourceTask.getTaskId(), datasetId);

            Map<String, Object> res = pythonAlgorithmClient.runFeatureProcessing(py);
            Map<String, Object> result = normFeatureProcessing(res, taskId);
            setStatus(task, str(result, "status") == null ? TASK_STATUS_SUCCESS : str(result, "status"));
            cacheTask(task, result, null);
            okAlg(taskId, json(res), featureProcessingSum(result), featureProcessingVal(result), null);
            return startResp(task, result);
        }
        catch (ServiceException e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String err = e.getMessage() == null ? "特征处理失败" : e.getMessage();
            cacheTask(task, null, err);
            failAlg(taskId, err);
            throw e;
        }
        catch (Exception e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String err = "特征处理任务启动失败";
            cacheTask(task, null, err);
            failAlg(taskId, err);
            log.error("特征处理任务失败，任务ID={}，状态={}，错误={}", taskId, TASK_STATUS_FAILED, e.getMessage(), e);
            throw new ServiceException(err);
        }
    }

    @Override
    public Map<String, Object> submit_key_process(FaultIdentifyStartRequest request)
    {
        Map<String, Object> reqMap = normReq(request);
        checkReq(reqMap);

        Map<String, Object> task = newKeyTask(reqMap);
        String taskId = str(task, "task_id");
        String requestId = str(task, "request_id");

        try
        {
            Map<String, Object> py = keyProcessReqAssembler.assemble(request, taskId, requestId);
            task.put("algorithm_params", py.get("params"));
            task.put("business_object_id", py.get("targetNodeId"));
            cacheKey(task, null, null);
            insAlg(newKey(task, py));

            setStatus(task, TASK_STATUS_RUNNING);
            cacheKey(task, null, null);
            String reqJson = json(py);
            runAlg(taskId, reqJson);
            log.info("调用Python关键工序识别服务，任务ID={}，目标={}:{}", taskId, py.get("targetType"), py.get("targetId"));

            Map<String, Object> res = pythonAlgorithmClient.identify_process(py);
            Map<String, Object> result = normKey(res, taskId);
            if (result.isEmpty())
            {
                throw new ServiceException("关键工序识别算法返回空结果");
            }
            setStatus(task, str(result, "status") == null ? TASK_STATUS_SUCCESS : str(result, "status"));
            cacheKey(task, result, null);
            okAlg(
                    taskId,
                    json(result),
                    keySum(result),
                    keyVal(result),
                    null
            );
            return startResp(task, result);
        }
        catch (ServiceException e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String err = e.getMessage() == null ? "关键工序识别失败" : e.getMessage();
            cacheKey(task, null, err);
            failAlg(taskId, err);
            throw e;
        }
        catch (Exception e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String err = "关键工序识别任务启动失败";
            cacheKey(task, null, err);
            failAlg(taskId, err);
            log.error("关键工序识别任务失败，任务ID={}，状态={}，错误={}", taskId, TASK_STATUS_FAILED, e.getMessage(), e);
            throw new ServiceException(err);
        }
    }

    @Override
    public Map<String, Object> submit_degradation(DegradationTaskRequest req)
    {
        if (req == null)
        {
            throw new ServiceException("退化点检测请求为空");
        }

        String featId = to_text(req.getFeatureTaskId());
        if (featId == null)
        {
            throw new ServiceException("缺少特征处理任务ID");
        }

        AlgTaskResult featureTask = algTaskMapper.getByTaskId(featId);
        if (featureTask == null)
        {
            throw new ServiceException("未找到特征处理任务: " + featId);
        }
        if (!ALG_FEATURE_PROCESSING.equals(featureTask.getTaskType()) || !TASK_STATUS_SUCCESS.equals(featureTask.getStatus()))
        {
            throw new ServiceException("特征处理任务未成功完成");
        }

        Map<String, Object> featRes = parseJsonObject(featureTask.getResJson());
        String featurePath = to_text(findVal(featRes, 0, "featurePath", "feature_path"));
        String featureSetId = to_text(findVal(featRes, 0, "featureSetId", "feature_set_id"));
        String datasetId = to_text(findVal(featRes, 0, "datasetId", "dataset_id"));
        Object samplingFrequency = findVal(featRes, 0, "samplingFrequency", "samplingRate", "sampling_frequency", "sampling_rate");
        Object windowCount = findVal(featRes, 0, "windowCount", "window_count");
        if (featurePath == null)
        {
            throw new ServiceException("特征处理结果缺少 featurePath");
        }
        if (featureSetId == null)
        {
            featureSetId = "FS_" + (datasetId == null ? featId : datasetId);
        }
        if (datasetId == null)
        {
            datasetId = featId;
        }

        String taskId = to_text(req.getTaskId());
        if (taskId == null)
        {
            taskId = newDegId();
        }
        String requestId = new_req_id();

        String flowTaskId = to_text(req.getFlowTaskId());
        if (flowTaskId == null)
        {
            flowTaskId = flowTaskId(featureTask);
        }

        Map<String, Object> task = newDegTask(taskId, requestId, featId, flowTaskId);
        log.info("Fault identify stage3 created, taskId={}, sourceTaskId={}, featureAnalysisTaskId={}, taskType={}",
                taskId, featId, task.get("feature_analysis_task_id"), task.get("task_type"));
        cacheDeg(task, null, null);
        insAlg(newDeg(task, req, featRes));

        DegradationParams params = req.getParams();
        double win = params != null && params.getWindow() != null ? params.getWindow() : 1.0D;
        int overlap = params != null && params.getOverlap() != null ? params.getOverlap() : 50;
        int baseCnt = params != null && params.getBaseline() != null ? params.getBaseline() : 500;
        double rmsSensitivity = params != null && params.getRmsSensitivity() != null ? params.getRmsSensitivity() : 0.2D;
        List<String> detectionMethods = params != null && params.getDetectionMethods() != null && !params.getDetectionMethods().isEmpty()
                ? params.getDetectionMethods()
                : List.of("kurtosis", "rms");

        Map<String, Object> algReq = new LinkedHashMap<>();
        algReq.put("authCode", DEGRADATION_AUTH_CODE);
        algReq.put("requestId", requestId);
        algReq.put("taskId", taskId);
        algReq.put("taskType", TASK_TYPE_DEGRADATION_DETECT);
        algReq.put("sourceTaskId", featId);
        algReq.put("algorithmVersion", DEGRADATION_ALGORITHM_VERSION);
        algReq.put("requestTime", fmtNow(DATE_TIME_PATTERN));
        algReq.put("datasetId", datasetId);
        algReq.put("featureSetId", featureSetId);
        algReq.put("featurePath", featurePath);
        algReq.put("samplingFrequency", samplingFrequency == null ? 25600 : samplingFrequency);
        algReq.put("windowSize", win);
        algReq.put("overlapPercent", overlap);
        algReq.put("overlapRate", overlap);
        algReq.put("baselineWindows", baseCnt);
        algReq.put("baselineWindowCount", baseCnt);
        algReq.put("rmsSensitivity", rmsSensitivity);
        algReq.put("methods", detectionMethods);
        algReq.put("detectionMethods", detectionMethods);
        algReq.put("fusionStrategy", "earliest");
        algReq.put("detectionId", "DET_" + taskId);
        algReq.put("windowCount", windowCount);
        String reqJson = json(algReq);

        try
        {
            setStatus(task, TASK_STATUS_RUNNING);
            cacheDeg(task, null, null);
            runAlg(taskId, reqJson);
            log.info("调用Python退化点检测服务，任务ID={}，特征任务ID={}，数据集ID={}，特征文件路径={}", taskId, featId, datasetId, featurePath);

            Map<String, Object> res = pythonAlgorithmClient.detect_degradation(algReq);
            if (res == null || res.isEmpty())
            {
                throw new ServiceException("退化点检测算法返回空结果");
            }
            if (!degOk(res))
            {
                throw new ServiceException(algMsg(res));
            }

            Map<String, Object> result = normDeg(res, taskId);
            setStatus(task, TASK_STATUS_SUCCESS);
            cacheDeg(task, result, null);
            okAlg(taskId, json(res), degSum(result), degVal(result), degUnit(result));
            log.info("退化点检测任务完成，任务ID={}，状态={}", taskId, TASK_STATUS_SUCCESS);
            return startResp(task, result);
        }
        catch (ServiceException e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            cacheDeg(task, null, e.getMessage());
            failAlg(taskId, e.getMessage(), reqJson);
            throw e;
        }
        catch (Exception e)
        {
            setStatus(task, TASK_STATUS_FAILED);
            String msg = "退化点检测算法调用失败：" + e.getMessage();
            cacheDeg(task, null, msg);
            failAlg(taskId, msg, reqJson);
            log.error("退化点检测任务失败，任务ID={}，状态={}，错误={}", taskId, TASK_STATUS_FAILED, e.getMessage(), e);
            throw new ServiceException(msg);
        }
    }
    private Map<String, Object> normReq(FaultIdentifyStartRequest requestData)
    {
        FaultIdentifyStartRequest safe = requestData == null ? new FaultIdentifyStartRequest() : requestData;

        Object params = safe.getAlgoParams();
        if (params == null)
        {
            params = safe.getFeatureParams();
        }
        if (params == null)
        {
            params = safe.getParams();
        }

        Object bizId = safe.getBizId();
        if (bizId == null)
        {
            bizId = safe.getImportId();
        }
        if (bizId == null)
        {
            bizId = safe.getObject_id();
        }
        if (bizId == null)
        {
            bizId = selId(params);
        }
        if (bizId == null)
        {
            bizId = selId(safe.getSelected());
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("business_object_id", bizId == null ? null : String.valueOf(bizId).trim());
        out.put("algorithm_params", params instanceof Map ? params : Collections.emptyMap());
        out.put("raw_request", safe);
        return out;
    }

    private void checkReq(Map<String, Object> reqMap)
    {
        String bizId = str(reqMap, "business_object_id");
        if (bizId == null || bizId.isEmpty())
        {
            throw new ServiceException("业务对象ID不能为空");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) reqMap.get("algorithm_params");
        if (params == null || params.isEmpty())
        {
            throw new ServiceException("算法参数不能为空");
        }
    }

    private Map<String, Object> newFeatTask(Map<String, Object> reqMap)
    {
        Map<String, Object> task = new LinkedHashMap<>();
        String taskId = "FY" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        task.put("task_id", taskId);
        task.put("task_type", TASK_TYPE_FEATURE_ANALYSIS);
        task.put("status", TASK_STATUS_PENDING);
        task.put("request_id", UUID.randomUUID().toString().replace("-", ""));
        task.put("feature_analysis_task_id", taskId);
        task.put("business_object_id", reqMap.get("business_object_id"));
        task.put("algorithm_params", reqMap.get("algorithm_params"));
        task.put("created_at", System.currentTimeMillis());
        task.put("updated_at", System.currentTimeMillis());
        return task;
    }

    private Map<String, Object> newFeatureProcessingTask(Map<String, Object> reqMap, String sourceTaskId, String flowTaskId)
    {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("task_id", "FP" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date()));
        task.put("task_type", TASK_TYPE_FEATURE_PROCESSING);
        task.put("status", TASK_STATUS_PENDING);
        task.put("request_id", UUID.randomUUID().toString().replace("-", ""));
        task.put("source_task_id", sourceTaskId);
        task.put("feature_analysis_task_id", flowTaskId == null ? sourceTaskId : flowTaskId);
        task.put("business_object_id", reqMap.get("business_object_id"));
        task.put("algorithm_params", reqMap.get("algorithm_params"));
        task.put("created_at", System.currentTimeMillis());
        task.put("updated_at", System.currentTimeMillis());
        return task;
    }

    private Map<String, Object> newKeyTask(Map<String, Object> reqMap)
    {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("task_id", "KP" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date()));
        task.put("task_type", TASK_TYPE_KEY_PROCESS_IDENTIFY);
        task.put("status", TASK_STATUS_PENDING);
        task.put("request_id", UUID.randomUUID().toString().replace("-", ""));
        task.put("business_object_id", reqMap.get("business_object_id"));
        task.put("algorithm_params", reqMap.get("algorithm_params"));
        task.put("created_at", System.currentTimeMillis());
        task.put("updated_at", System.currentTimeMillis());
        return task;
    }

    private void setStatus(Map<String, Object> task, String status)
    {
        task.put("status", status);
        task.put("updated_at", System.currentTimeMillis());
    }

    private void cacheTask(
            Map<String, Object> task,
            Map<String, Object> res,
            String err
    )
    {
        String taskId = str(task, "task_id");

        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("taskId", taskId);
        snap.put("task_id", taskId);
        snap.put("status", task.get("status"));
        snap.put("result", res);
        snap.put("logs", res == null ? null : res.get("logs"));
        snap.put("errorMessage", err);
        snap.put("error_message", err);
        snap.put("updatedAt", task.get("updated_at"));
        snap.put("createdAt", task.get("created_at"));
        taskStatusStore.put(taskId, snap);
    }

    private void cacheKey(
            Map<String, Object> task,
            Map<String, Object> result,
            String errMsg
    )
    {
        String taskId = str(task, "task_id");

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("taskId", taskId);
        snapshot.put("task_id", taskId);
        snapshot.put("taskType", task.get("task_type"));
        snapshot.put("task_type", task.get("task_type"));
        snapshot.put("status", task.get("status"));
        snapshot.put("result", result);
        snapshot.put("logs", result == null ? null : result.get("logs"));
        snapshot.put("errorMessage", errMsg);
        snapshot.put("error_message", errMsg);
        snapshot.put("updatedAt", task.get("updated_at"));
        snapshot.put("createdAt", task.get("created_at"));
        taskStatusStore.put(taskId, snapshot);
    }

    private Map<String, Object> newDegTask(String taskId, String requestId, String featId, String flowTaskId)
    {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("task_id", taskId);
        task.put("task_type", TASK_TYPE_DEGRADATION_DETECT);
        task.put("status", TASK_STATUS_PENDING);
        task.put("request_id", requestId);
        task.put("source_task_id", featId);
        task.put("feature_analysis_task_id", flowTaskId == null ? featId : flowTaskId);
        task.put("created_at", System.currentTimeMillis());
        task.put("updated_at", System.currentTimeMillis());
        return task;
    }

    private void cacheDeg(
            Map<String, Object> task,
            Map<String, Object> result,
            String errMsg
    )
    {
        String taskId = str(task, "task_id");

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("taskId", taskId);
        snapshot.put("task_id", taskId);
        snapshot.put("taskType", task.get("task_type"));
        snapshot.put("task_type", task.get("task_type"));
        snapshot.put("status", task.get("status"));
        snapshot.put("result", result);
        snapshot.put("errorMessage", errMsg);
        snapshot.put("error_message", errMsg);
        snapshot.put("updatedAt", task.get("updated_at"));
        snapshot.put("createdAt", task.get("created_at"));
        taskStatusStore.put(taskId, snapshot);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normDeg(Object data, String taskId)
    {
        Map<String, Object> result;
        if (data instanceof Map)
        {
            result = new LinkedHashMap<>((Map<String, Object>) data);
        }
        else
        {
            result = new LinkedHashMap<>();
            result.put("result", data);
        }
        result.putIfAbsent("taskId", taskId);
        result.putIfAbsent("task_id", taskId);
        result.putIfAbsent("status", TASK_STATUS_SUCCESS);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normFeat(Map<String, Object> response, String fallbackTaskId)
    {
        Map<String, Object> data = response == null ? Collections.emptyMap() : asMap(response.get("data"));
        if (data.isEmpty() && response != null && "SUCCESS".equalsIgnoreCase(String.valueOf(response.get("status"))))
        {
            data = new LinkedHashMap<>(response);
        }
        Map<String, Object> result = asMap(data.get("result"));
        if (result.isEmpty() && !data.isEmpty())
        {
            result = new LinkedHashMap<>(data);
            result.remove("logs");
            result.remove("status");
            result.remove("taskId");
            result.remove("task_id");
            result.remove("requestId");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        Object taskId = data.get("taskId") == null ? fallbackTaskId : data.get("taskId");
        out.put("taskId", taskId);
        out.put("task_id", taskId);
        out.put("status", data.get("status") == null ? TASK_STATUS_SUCCESS : data.get("status"));
        out.put("result", result);
        out.put("logs", data.get("logs"));
        out.put("featureSeries", result.get("featureSeries"));
        out.put("timeDomainSignal", result.get("timeDomainSignal"));
        out.put("timeFrequencySpectrum", result.get("timeFrequencySpectrum"));
        out.put("timeDomain", result.get("timeDomain"));
        out.put("timeFrequency", result.get("timeFrequency"));
        out.put("summary", result.get("summary"));
        out.put("datasetId", result.get("datasetId"));
        out.put("combinedDataPath", result.get("combinedDataPath"));
        out.put("metadataPath", result.get("metadataPath"));
        out.put("totalSamples", result.get("totalSamples"));
        out.put("totalTime", result.get("totalTime"));
        out.put("fileReports", result.get("fileReports"));
        out.put("rawResponse", response);

        if (result.containsKey("sourceFile"))
        {
            out.put("sourceFile", result.get("sourceFile"));
        }
        return out;
    }

    private Map<String, Object> normFeatureProcessing(Map<String, Object> response, String fallbackTaskId)
    {
        Map<String, Object> data = response == null ? Collections.emptyMap() : asMap(response.get("data"));
        if (data.isEmpty() && response != null && "SUCCESS".equalsIgnoreCase(String.valueOf(response.get("status"))))
        {
            data = new LinkedHashMap<>(response);
        }
        Map<String, Object> result = asMap(data.get("result"));
        if (result.isEmpty() && !data.isEmpty())
        {
            result = new LinkedHashMap<>(data);
        }
        if (result.isEmpty() && response != null)
        {
            result = new LinkedHashMap<>(response);
        }
        result.putIfAbsent("taskId", fallbackTaskId);
        result.putIfAbsent("task_id", fallbackTaskId);
        result.putIfAbsent("status", TASK_STATUS_SUCCESS);
        return result;
    }

    private AlgTaskResult resolveFeatureDataTask(Map<String, Object> rawRequest, Map<String, Object> reqMap)
    {
        String sourceTaskId = to_text(findVal(rawRequest, 0, "sourceTaskId", "source_task_id", "taskId", "task_id", "featureAnalysisTaskId", "feature_analysis_task_id"));
        AlgTaskResult sourceTask;
        if (sourceTaskId != null)
        {
            sourceTask = algTaskMapper.getByTaskId(sourceTaskId);
        }
        else
        {
            String bizId = str(reqMap, "business_object_id");
            sourceTask = algTaskMapper.getLatestFeatureDataTask(bizId, null);
        }

        if (sourceTask == null)
        {
            throw new ServiceException("未找到成功的数据分析任务，无法获取 combinedDataPath");
        }
        if (!ALG_FEATURE.equals(sourceTask.getTaskType()) || !TASK_STATUS_SUCCESS.equals(sourceTask.getStatus()))
        {
            throw new ServiceException("源任务不是成功的特征分析任务");
        }
        Map<String, Object> result = parseJsonObject(sourceTask.getResJson());
        Object combinedDataPath = findVal(result, 0, "combinedDataPath", "combined_data_path");
        if (combinedDataPath == null)
        {
            throw new ServiceException("数据分析结果缺少 combinedDataPath");
        }
        return sourceTask;
    }

    private Map<String, Object> normKey(Map<String, Object> response, String fallbackTaskId)
    {
        Map<String, Object> data = response == null ? Collections.emptyMap() : asMap(response.get("data"));
        Map<String, Object> result = asMap(data.get("result"));
        if (result.isEmpty())
        {
            result = asMap(response == null ? null : response.get("payload"));
        }
        if (result.isEmpty() && data != null && !data.isEmpty())
        {
            result = new LinkedHashMap<>(data);
            result.remove("logs");
            result.remove("status");
            result.remove("taskId");
            result.remove("task_id");
            result.remove("requestId");
        }
        if (result.isEmpty())
        {
            return Collections.emptyMap();
        }

        Object taskId = data.get("taskId") == null ? fallbackTaskId : data.get("taskId");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("task_id", taskId);
        out.put("status", data.get("status") == null ? TASK_STATUS_SUCCESS : data.get("status"));
        out.put("result", result);
        out.putAll(result);
        out.put("logs", data.get("logs"));
        out.put("rawResponse", response);
        return out;
    }

    private Map<String, Object> dbTaskSnapshot(String taskId)
    {
        try
        {
            List<AlgTaskResult> flowTasks = algTaskMapper.getTasksByFlowTaskId(taskId);
            if (flowTasks != null && !flowTasks.isEmpty())
            {
                log.info("Restore fault identify flow, flowTaskId={}, stageCount={}", taskId, flowTasks.size());
                return flowTaskSnapshot(taskId, flowTasks);
            }

            AlgTaskResult record = algTaskMapper.getByTaskId(taskId);
            if (record == null)
            {
                return null;
            }

            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("taskId", record.getTaskId());
            snapshot.put("task_id", record.getTaskId());
            snapshot.put("taskType", record.getTaskType());
            snapshot.put("task_type", record.getTaskType());
            snapshot.put("status", record.getStatus());
            snapshot.put("errorMessage", record.getErrMsg());
            snapshot.put("error_message", record.getErrMsg());

            Map<String, Object> result = parseJsonObject(record.getResJson());
            if (ALG_FEATURE.equals(record.getTaskType()) && result != null && !result.isEmpty())
            {
                Map<String, Object> ret = normFeat(result, record.getTaskId());
                snapshot.put("result", ret);
                snapshot.put("logs", ret.get("logs"));
            }
            else if (ALG_KEY_PROCESS.equals(record.getTaskType()) && result != null && !result.isEmpty())
            {
                Map<String, Object> ret = normKey(result, record.getTaskId());
                snapshot.put("result", ret);
                snapshot.put("logs", ret.get("logs"));
            }
            else
            {
                snapshot.put("result", result);
                snapshot.put("logs", result == null ? null : result.get("logs"));
            }
            return snapshot;
        }
        catch (Exception e)
        {
            log.warn("构建最新结果快照失败，任务ID={}", taskId, e);
            return null;
        }
    }

    private Map<String, Object> flowTaskSnapshot(String flowTaskId, List<AlgTaskResult> records)
    {
        AlgTaskResult dataAnalysis = pickStage(records, ALG_FEATURE);
        AlgTaskResult featureProcessing = pickStage(records, ALG_FEATURE_PROCESSING);
        AlgTaskResult degradation = pickStage(records, ALG_DEGRADE);
        AlgTaskResult faultPredict = pickStage(records, "FAULT_PREDICT");
        AlgTaskResult display = faultPredict != null ? faultPredict
                : degradation != null ? degradation
                : featureProcessing != null ? featureProcessing
                : dataAnalysis;

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("taskId", flowTaskId);
        snapshot.put("task_id", flowTaskId);
        snapshot.put("flowTaskId", flowTaskId);
        snapshot.put("flow_task_id", flowTaskId);
        snapshot.put("status", display == null ? null : display.getStatus());
        snapshot.put("taskType", display == null ? null : display.getTaskType());
        snapshot.put("task_type", snapshot.get("taskType"));
        snapshot.put("result", display == null ? null : stageResult(display));
        snapshot.put("errorMessage", display == null ? null : display.getErrMsg());
        snapshot.put("error_message", snapshot.get("errorMessage"));
        snapshot.put("dataAnalysisTask", stageSnapshot(dataAnalysis));
        snapshot.put("data_analysis_task", snapshot.get("dataAnalysisTask"));
        snapshot.put("featureProcessingTask", stageSnapshot(featureProcessing));
        snapshot.put("feature_processing_task", snapshot.get("featureProcessingTask"));
        snapshot.put("degradationTask", stageSnapshot(degradation));
        snapshot.put("degradation_task", snapshot.get("degradationTask"));
        snapshot.put("faultPredictTask", stageSnapshot(faultPredict));
        snapshot.put("fault_predict_task", snapshot.get("faultPredictTask"));
        return snapshot;
    }

    private AlgTaskResult pickStage(List<AlgTaskResult> records, String taskType)
    {
        AlgTaskResult latest = null;
        AlgTaskResult latestSuccess = null;
        for (AlgTaskResult item : records == null ? Collections.<AlgTaskResult>emptyList() : records)
        {
            if (!taskType.equals(item.getTaskType()))
            {
                continue;
            }
            latest = item;
            if (TASK_STATUS_SUCCESS.equals(item.getStatus()))
            {
                latestSuccess = item;
            }
        }
        return latestSuccess == null ? latest : latestSuccess;
    }

    private Map<String, Object> stageSnapshot(AlgTaskResult record)
    {
        if (record == null)
        {
            return null;
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("taskId", record.getTaskId());
        snapshot.put("task_id", record.getTaskId());
        snapshot.put("flowTaskId", flowTaskId(record));
        snapshot.put("flow_task_id", flowTaskId(record));
        snapshot.put("taskType", record.getTaskType());
        snapshot.put("task_type", record.getTaskType());
        snapshot.put("status", record.getStatus());
        snapshot.put("featureTaskId", record.getFeatTaskId());
        snapshot.put("feature_task_id", record.getFeatTaskId());
        snapshot.put("errorMessage", record.getErrMsg());
        snapshot.put("error_message", record.getErrMsg());
        Map<String, Object> request = parseJsonObject(record.getReqJson());
        snapshot.put("request", request);
        snapshot.put("request_json", request);
        Map<String, Object> result = stageResult(record);
        snapshot.put("result", result);
        snapshot.put("logs", result == null ? null : result.get("logs"));
        return snapshot;
    }

    private Map<String, Object> stageResult(AlgTaskResult record)
    {
        Map<String, Object> result = parseJsonObject(record.getResJson());
        if (ALG_FEATURE.equals(record.getTaskType()) && result != null && !result.isEmpty())
        {
            return normFeat(result, record.getTaskId());
        }
        if (ALG_KEY_PROCESS.equals(record.getTaskType()) && result != null && !result.isEmpty())
        {
            return normKey(result, record.getTaskId());
        }
        return result;
    }

    private AlgTaskResult newFeat(Map<String, Object> task, FeatureReq request)
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(str(task, "task_id"));
        r.setTaskType(ALG_FEATURE);
        r.setTaskName(NAME_FEATURE);
        r.setStatus(TASK_STATUS_PENDING);
        r.setRequestId(request == null ? str(task, "request_id") : request.getRequestId());
        r.setAlgVersion(request == null ? DEGRADATION_ALGORITHM_VERSION : request.getAlgorithmVersion());
        r.setFeatTaskId(str(task, "feature_analysis_task_id"));
        if (request != null)
        {
            r.setAircraftId(to_text(request.getAircraftId()));
            if ("subsystem".equals(request.getTargetType()))
            {
                r.setSubsystemId(to_text(request.getTargetId()));
            }
            else if ("device".equals(request.getTargetType()))
            {
                r.setEquipmentId(to_text(request.getTargetId()));
            }
            else if ("component".equals(request.getTargetType()))
            {
                r.setComponentId(to_text(request.getTargetId()));
            }
            r.setBizId(request.getTargetType() == null || request.getTargetId() == null
                    ? str(task, "business_object_id")
                    : bizNodeId(request.getTargetType(), request.getTargetId()));
            r.setBizLevel("device".equals(request.getTargetType()) ? "equipment" : request.getTargetType());
            r.setBizName(request.getTargetName());
        }
        return r;
    }

    private AlgTaskResult newFeatureProcessing(Map<String, Object> task, AlgTaskResult sourceTask)
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(str(task, "task_id"));
        r.setTaskType(ALG_FEATURE_PROCESSING);
        r.setTaskName("FEATURE_PROCESSING");
        r.setStatus(TASK_STATUS_PENDING);
        r.setRequestId(str(task, "request_id"));
        r.setAlgVersion(DEGRADATION_ALGORITHM_VERSION);
        r.setFeatTaskId(str(task, "feature_analysis_task_id"));
        if (sourceTask != null)
        {
            r.setAircraftId(sourceTask.getAircraftId());
            r.setSubsystemId(sourceTask.getSubsystemId());
            r.setEquipmentId(sourceTask.getEquipmentId());
            r.setComponentId(sourceTask.getComponentId());
            r.setBizId(sourceTask.getBizId());
            r.setBizLevel(sourceTask.getBizLevel());
            r.setBizName(sourceTask.getBizName());
        }
        return r;
    }

    private AlgTaskResult newKey(Map<String, Object> task, Map<String, Object> request)
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(str(task, "task_id"));
        r.setTaskType(ALG_KEY_PROCESS);
        r.setTaskName(NAME_KEY_PROCESS);
        r.setStatus(TASK_STATUS_PENDING);
        r.setRequestId(to_text(request == null ? null : request.get("requestId")));
        r.setAlgVersion(to_text(request == null ? null : request.get("algorithmVersion")));
        if (request != null)
        {
            Map<String, Object> ctx = asMap(request.get("hierarchyContext"));
            String targetType = to_text(request.get("targetType"));
            String targetId = to_text(request.get("targetId"));
            r.setAircraftId(to_text(ctx.get("aircraftId")));
            r.setSubsystemId(to_text(ctx.get("subsystemId")));
            r.setEquipmentId(to_text(ctx.get("equipmentId")));
            r.setComponentId(to_text(ctx.get("componentId")));
            r.setBizId(keyNodeId(targetType, targetId));
            r.setBizLevel(lvl(targetType, r.getBizId()));
            r.setBizName(to_text(request.get("targetName")));
        }
        return r;
    }

    private AlgTaskResult newDeg(
            Map<String, Object> task,
            DegradationTaskRequest req,
            Map<String, Object> featRes
    )
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(str(task, "task_id"));
        r.setTaskType(ALG_DEGRADE);
        r.setTaskName(NAME_DEGRADE);
        r.setStatus(TASK_STATUS_PENDING);
        r.setRequestId(str(task, "request_id"));
        r.setAlgVersion(DEGRADATION_ALGORITHM_VERSION);
        r.setFeatTaskId(str(task, "feature_analysis_task_id"));

        if (!cpBiz(r, req == null ? null : req.getFeatureTaskId()))
        {
            selBiz(r, findVal(featRes, 0, "selected_object", "selectedObject"));
            if (r.getBizId() == null)
            {
                Object id = findVal(featRes, 0, "businessObjectId", "business_object_id", "objectId", "object_id");
                r.setBizId(to_text(id));
            }
        }
        return r;
    }

    private void runFeat(Map<String, Object> task, String reqJson)
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(str(task, "task_id"));
        r.setStatus(TASK_STATUS_RUNNING);
        r.setStartAt(new Date());
        r.setReqJson(reqJson);
        updAlg(r);
    }

    private void runAlg(String taskId, String reqJson)
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(taskId);
        r.setStatus(TASK_STATUS_RUNNING);
        r.setStartAt(new Date());
        r.setReqJson(reqJson);
        updAlg(r);
    }

    private void okAlg(
            String taskId,
            String resJson,
            String summary,
            String val,
            String resultUnit
    )
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(taskId);
        r.setStatus(TASK_STATUS_SUCCESS);
        r.setResJson(resJson);
        r.setEndAt(new Date());
        r.setSummary(cut(summary, 512));
        r.setResultVal(cut(val, 128));
        r.setResultUnit(cut(resultUnit, 64));
        updAlg(r);
    }

    private void failAlg(String taskId, String err)
    {
        failAlg(taskId, err, null);
    }

    private void failAlg(String taskId, String err, String reqJson)
    {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(taskId);
        r.setStatus(TASK_STATUS_FAILED);
        r.setErrMsg(err);
        r.setEndAt(new Date());
        r.setReqJson(reqJson);
        updAlg(r);
    }

    private void insAlg(AlgTaskResult r)
    {
        if (r == null || r.getTaskId() == null || r.getTaskId().trim().isEmpty())
        {
            return;
        }
        try
        {
            algTaskMapper.insertTask(r);
        }
        catch (Exception e)
        {
            log.error("新增算法任务失败，任务ID={}", r.getTaskId(), e);
        }
    }

    private void updAlg(AlgTaskResult r)
    {
        if (r == null || r.getTaskId() == null || r.getTaskId().trim().isEmpty())
        {
            return;
        }
        try
        {
            int rows = algTaskMapper.updateTask(r);
            if (rows == 0)
            {
                log.warn("算法任务更新影响0行，任务ID={}，状态={}", r.getTaskId(), r.getStatus());
            }
        }
        catch (Exception e)
        {
            log.error("更新算法任务失败，任务ID={}，状态={}", r.getTaskId(), r.getStatus(), e);
        }
    }

    private boolean cpBiz(AlgTaskResult dst, String featId)
    {
        if (dst == null || featId == null || featId.trim().isEmpty())
        {
            return false;
        }
        try
        {
            AlgTaskResult src = algTaskMapper.getByTaskId(featId);
            if (src == null)
            {
                return false;
            }
            dst.setBizId(src.getBizId());
            dst.setBizLevel(src.getBizLevel());
            dst.setBizName(src.getBizName());
            dst.setAircraftId(src.getAircraftId());
            dst.setSubsystemId(src.getSubsystemId());
            dst.setEquipmentId(src.getEquipmentId());
            dst.setComponentId(src.getComponentId());
            return true;
        }
        catch (Exception e)
        {
            log.error("复制特征请求字段失败，特征任务ID={}", featId, e);
            return false;
        }
    }
    @SuppressWarnings("unchecked")
    private void selBiz(AlgTaskResult record, Object selected)
    {
        if (record == null || !(selected instanceof Map))
        {
            return;
        }
        Map<String, Object> selectedMap = (Map<String, Object>) selected;
        Object id = pickVal(selectedMap, "id", "object_id", "objectId", "business_object_id", "businessObjectId");


        Object level = pickVal(selectedMap, "level", "node_type", "nodeType", "business_object_level", "businessObjectLevel");
        Object name = pickVal(selectedMap, "name", "label", "business_object_name", "businessObjectName");

        if (record.getBizId() == null)
        {
            record.setBizId(to_text(id));
        }
        if (record.getBizLevel() == null)
        {
            record.setBizLevel(lvl(to_text(level), record.getBizId()));
        }
        if (record.getBizName() == null)
        {
            record.setBizName(to_text(name));
        }
    }

    private Map<String, Object> keyRow(AlgTaskResult record)
    {
        Map<String, Object> result = parseJsonObject(record.getResJson());
        Map<String, Object> data = keyPayload(result);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("taskId", record.getTaskId());
        row.put("task_id", record.getTaskId());
        row.put("status", record.getStatus());
        row.put("targetType", record.getBizLevel());
        row.put("target_type", record.getBizLevel());
        row.put("targetId", record.getBizId());
        row.put("target_id", record.getBizId());
        row.put("targetName", record.getBizName());
        row.put("target_name", record.getBizName());
        row.put("keyProcessCode", pickVal(data, "keyProcessCode", "key_process_code", "processCode", "process_code"));
        row.put("key_process_code", row.get("keyProcessCode"));
        row.put("keyProcessName", pickVal(data, "keyProcessName", "key_process_name", "processName", "process_name"));
        row.put("key_process_name", row.get("keyProcessName"));
        row.put("confidence", pickVal(data, "confidence", "score"));
        row.put("score", pickVal(data, "score", "confidence"));
        row.put("reason", pickVal(data, "reason"));
        row.put("suggestion", pickVal(data, "suggestion"));
        row.put("candidateProcesses", pickVal(data, "candidateProcesses", "candidate_processes"));
        row.put("candidate_processes", row.get("candidateProcesses"));
        row.put("summary", record.getSummary());
        row.put("errorMessage", record.getErrMsg());
        row.put("createTime", record.getCreateTime());
        row.put("updateTime", record.getUpdateTime());
        row.put("result", result);
        return row;
    }

    private Map<String, Object> identifyRow(AlgTaskResult record)
    {
        Map<String, Object> result = parseJsonObject(record.getResJson());
        String flowTaskId = flowTaskId(record);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("taskId", flowTaskId);
        row.put("task_id", flowTaskId);
        row.put("flowTaskId", flowTaskId);
        row.put("flow_task_id", flowTaskId);
        row.put("internalTaskId", record.getTaskId());
        row.put("internal_task_id", record.getTaskId());
        row.put("stageTaskId", record.getTaskId());
        row.put("stage_task_id", record.getTaskId());
        row.put("taskType", record.getTaskType());
        row.put("task_type", record.getTaskType());
        row.put("taskName", record.getTaskName());
        row.put("task_name", record.getTaskName());
        row.put("status", record.getStatus());
        row.put("targetType", record.getBizLevel());
        row.put("target_type", record.getBizLevel());
        row.put("targetId", record.getBizId());
        row.put("target_id", record.getBizId());
        row.put("targetName", record.getBizName());
        row.put("target_name", record.getBizName());
        row.put("featureTaskId", record.getFeatTaskId());
        row.put("feature_task_id", record.getFeatTaskId());
        row.put("summary", record.getSummary());
        row.put("resultValue", record.getResultVal());
        row.put("result_value", record.getResultVal());
        row.put("resultUnit", record.getResultUnit());
        row.put("result_unit", record.getResultUnit());
        row.put("errorMessage", record.getErrMsg());
        row.put("error_message", record.getErrMsg());
        row.put("startTime", record.getStartAt());
        row.put("start_time", record.getStartAt());
        row.put("finishTime", record.getEndAt());
        row.put("finish_time", record.getEndAt());
        row.put("createTime", record.getCreateTime());
        row.put("updateTime", record.getUpdateTime());
        row.put("result", result);
        return row;
    }

    private String flowTaskId(AlgTaskResult record)
    {
        if (record == null)
        {
            return null;
        }
        String flowTaskId = to_text(record.getFeatTaskId());
        return flowTaskId == null ? record.getTaskId() : flowTaskId;
    }

    private Map<String, Object> keyPayload(Map<String, Object> source)
    {
        Map<String, Object> result = asMap(source == null ? null : source.get("result"));
        if (!result.isEmpty())
        {
            return result;
        }
        return source == null ? Collections.emptyMap() : source;
    }

    private String keySum(Map<String, Object> result)
    {
        Map<String, Object> data = keyPayload(result);
        String code = to_text(pickVal(data, "keyProcessCode", "key_process_code", "processCode", "process_code"));
        String name = to_text(pickVal(data, "keyProcessName", "key_process_name", "processName", "process_name"));
        String conf = to_text(pickVal(data, "confidence", "score"));
        if (name == null && code == null && conf == null)
        {
            return "关键工序识别完成";
        }

        StringBuilder sum = new StringBuilder("key process: ");
        sum.append(name == null ? "--" : name);
        if (code != null)
        {
            sum.append("(").append(code).append(")");
        }
        if (conf != null)
        {
            sum.append(", confidence: ").append(conf);
        }
        return sum.toString();
    }

    private String keyVal(Map<String, Object> result)
    {
        Map<String, Object> data = keyPayload(result);
        String code = to_text(pickVal(data, "keyProcessCode", "key_process_code", "processCode", "process_code"));
        if (code != null)
        {
            return code;
        }
        return to_text(pickVal(data, "confidence", "score"));
    }

    private String featSum(Map<String, Object> res)
    {
        Map<String, Object> summary = asMap(res == null ? null : res.get("summary"));
        if (!summary.isEmpty())
        {
            Object sampleCount = pickVal(summary, "sampleCount", "sample_count");
            Object duration = pickVal(summary, "duration");
            Object dominantFrequency = pickVal(summary, "dominantFrequency", "dominant_frequency");
            StringBuilder builder = new StringBuilder("feature analysis completed");
            if (sampleCount != null)
            {
                builder.append(", sample count: ").append(sampleCount);
            }
            if (duration != null)
            {
                builder.append("，分析时长：").append(duration);
            }
            if (dominantFrequency != null)
            {
                builder.append("，主频：").append(dominantFrequency);
            }
            return builder.toString();
        }

        Object totalSamples = pickVal(res, "totalSamples", "total_samples");
        Object totalTime = pickVal(res, "totalTime", "total_time");
        Object combinedDataPath = pickVal(res, "combinedDataPath", "combined_data_path");
        if (totalSamples != null || totalTime != null || combinedDataPath != null)
        {
            StringBuilder builder = new StringBuilder("data analysis completed");
            if (totalSamples != null)
            {
                builder.append(", total samples: ").append(totalSamples);
            }
            if (totalTime != null)
            {
                builder.append(", total time: ").append(totalTime).append("s");
            }
            if (combinedDataPath != null)
            {
                builder.append(", data_combined: ").append(combinedDataPath);
            }
            return builder.toString();
        }

        Map<String, Object> payload = asMap(res == null ? null : res.get("payload"));

        String code = to_text(pickVal(payload, "keyProcessCode", "key_process_code"));
        String name = to_text(pickVal(payload, "keyProcessName", "key_process_name"));
        String conf = to_text(pickVal(payload, "confidence"));



        if (name != null || code != null || conf != null)
        {
            StringBuilder sum = new StringBuilder("key process: ");
            sum.append(name == null ? "--" : name);
            if (code != null)
            {
                sum.append("(").append(code).append(")");
            }
            if (conf != null)

            {
                sum.append(", confidence: ").append(conf);
            }
            return sum.toString();
        }

        String msg = str(res, "message");
        return msg == null || msg.trim().isEmpty() ? "feature analysis completed" : msg;
    }


    private String featVal(Map<String, Object> res)
    {
        Map<String, Object> summary = asMap(res == null ? null : res.get("summary"));
        Object maxAmplitude = pickVal(summary, "maxAmplitude", "max_amplitude");
        if (maxAmplitude != null)
        {
            return String.valueOf(maxAmplitude);
        }

        Object totalSamples = pickVal(res, "totalSamples", "total_samples");
        if (totalSamples != null)
        {
            return String.valueOf(totalSamples);
        }
        Object totalTime = pickVal(res, "totalTime", "total_time");
        if (totalTime != null)
        {
            return String.valueOf(totalTime);
        }

        Map<String, Object> payload = asMap(res == null ? null : res.get("payload"));
        Object conf = pickVal(payload, "confidence");
        if (conf != null)
        {
            return String.valueOf(conf);
        }
        return to_text(pickVal(payload, "keyProcessCode", "key_process_code"));
    }

    private String featureProcessingSum(Map<String, Object> res)
    {
        Object windowCount = findVal(res, 0, "windowCount", "window_count");
        if (windowCount != null)
        {
            return "特征处理窗口数：" + windowCount;
        }
        String msg = to_text(findVal(res, 0, "message", "msg"));
        return msg == null ? "feature analysis completed" : msg;
    }

    private String featureProcessingVal(Map<String, Object> res)
    {
        Object windowCount = findVal(res, 0, "windowCount", "window_count");
        return windowCount == null ? null : String.valueOf(windowCount);
    }

    private String degSum(Map<String, Object> result)
    {
        String point = degVal(result);
        String unit = degUnit(result);

        if (point == null)
        {
            return "退化点检测完成";

        }
        return "早期退化点：" + point + (unit == null ? "" : " " + unit);
    }

    private String degVal(Map<String, Object> result)
    {
        return to_text(findVal(
                result,

                0,
                "earlyDegradationPoint",
                "early_degradation_point",
                "earlyDegradationTime",
                "early_degradation_time",
                "degradationPoint",
                "degradationTime",
                "degradation_point"
        ));
    }
    private String degUnit(Map<String, Object> result)
    {
        String unit = to_text(findVal(result, 0, "degradationPointUnit", "degradation_point_unit", "unit"));
        return unit == null ? "second" : unit;
    }

    private String json(Object obj)
    {

        if (obj == null)

        {
            return null;
        }
        try
        {
            return JSON.toJSONString(obj);
        }
        catch (Exception e)
        {
            log.warn("算法结果JSON序列化失败", e);

            return String.valueOf(obj);

        }
    }

    private Map<String, Object> parseJsonObject(String json)
    {
        if (json == null || json.trim().isEmpty())
        {
            return Collections.emptyMap();
        }
        try
        {
            JSONObject object = JSON.parseObject(json);
            return object == null ? Collections.emptyMap() : new LinkedHashMap<>(object);
        }
        catch (Exception e)
        {
            log.warn("算法任务JSON解析失败", e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value)
    {
        return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
    }


    private String lvl(String level, String nodeId)
    {
        String lv = level == null || level.trim().isEmpty() ? nodeType(nodeId) : level.trim();
        if ("device".equals(lv))
        {
            return "equipment";
        }

        return lv == null || lv.isEmpty() ? null : lv;
    }

    private String nodeType(String nodeId)
    {
        if (nodeId == null)
        {
            return null;
        }
        int idx = nodeId.indexOf(':');
        return idx <= 0 ? null : nodeId.substring(0, idx);
    }
    private String rawId(String nodeId)
    {
        if (nodeId == null)
        {
            return null;
        }
        int idx = nodeId.indexOf(':');
        return idx < 0 || idx == nodeId.length() - 1 ? nodeId : nodeId.substring(idx + 1);
    }

    private String bizNodeId(String targetType, Object targetId)
    {
        if (targetType == null || targetId == null)
        {
            return null;
        }
        if ("device".equals(targetType))
        {
            return "equipment:" + targetId;
        }
        return targetType + ":" + targetId;
    }

    private String keyNodeId(String targetType, Object targetId)
    {
        if (targetType == null || targetId == null)
        {
            return null;
        }
        if ("device".equals(targetType))
        {
            return "equipment:" + targetId;
        }
        if ("part".equals(targetType))
        {
            return "part_template:" + targetId;
        }
        return targetType + ":" + targetId;
    }




    private String to_text(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String cut(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private Map<String, Object> startResp(
            Map<String, Object> task,
            Map<String, Object> res
    )
    {
        Map<String, Object> out = new LinkedHashMap<>();
        Object taskId = task.get("task_id");
        Object flowTaskId = task.get("feature_analysis_task_id") == null ? taskId : task.get("feature_analysis_task_id");
        out.put("taskId", taskId);
        out.put("task_id", taskId);
        out.put("flowTaskId", flowTaskId);
        out.put("flow_task_id", flowTaskId);
        out.put("status", task.get("status"));
        out.put("result", res);
        out.put("errorMessage", null);
        out.put("error_message", null);
        return out;
    }

    private String str(Map<String, Object> source, String key)
    {
        Object v = source == null ? null : source.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Object pickVal(Map<String, Object> source, String... aliases)
    {
        if (source == null || aliases == null)
        {
            return null;
        }
        for (String alias : aliases)
        {
            if (source.containsKey(alias))
            {
                return source.get(alias);
            }
        }
        return null;
    }

    private Long to_long(Object value)
    {
        if (value == null || String.valueOf(value).trim().isEmpty())
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.parseLong(String.valueOf(value).trim());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object findVal(Object source, int depth, String... aliases)
    {
        if (source == null || depth > 6)
        {
            return null;
        }
        if (source instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>) source;
            Object v = pickVal(map, aliases);
            if (v != null)
            {
                return v;
            }
            for (Object item : map.values())
            {
                Object hit = findVal(item, depth + 1, aliases);
                if (hit != null)
                {
                    return hit;
                }
            }
        }
        if (source instanceof List)
        {
            for (Object item : (List<?>) source)
            {
                Object hit = findVal(item, depth + 1, aliases);
                if (hit != null)
                {
                    return hit;
                }
            }
        }
        return null;
    }

    private boolean degOk(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return false;
        }
        Object status = findVal(response, 0, "status");
        if ("SUCCESS".equalsIgnoreCase(String.valueOf(status)))
        {
            return true;
        }
        return degOk(response.get("code"));
    }

    private boolean degOk(Object code)
    {
        if (code instanceof Number)
        {
            return ((Number) code).intValue() == 1;
        }
        return "1".equals(String.valueOf(code));
    }

    private String algMsg(Map<String, Object> res)
    {
        String msg = str(res, "msg");
        if (msg != null && !msg.trim().isEmpty())
        {
            return msg;
        }
        msg = str(res, "message");
        if (msg != null && !msg.trim().isEmpty())
        {
            return msg;
        }
        return "退化点检测算法返回失败";
    }

    private String newDegId()
    {
        return "DG" + fmtNow(TASK_TIME_PATTERN);
    }

    private String new_req_id()
    {
        return "REQ" + fmtNow(TASK_TIME_PATTERN);
    }

    private String fmtNow(String pattern)
    {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    @SuppressWarnings("unchecked")
    private Object selId(Object params)
    {
        if (!(params instanceof Map))
        {
            return null;
        }

        Map<String, Object> map = (Map<String, Object>) params;

        // Compatible with older nested selected_object payloads.
        Object sel = map.get("selected_object");
        if (!(sel instanceof Map))
        {
            sel = map;
        }
        if (!(sel instanceof Map))
        {
            return null;
        }

        Map<String, Object> selMap = (Map<String, Object>) sel;
        Object id = selMap.get("id");
        if (id == null)
        {
            id = selMap.get("object_id");
        }
        return id;
    }

    private String statusText(Map<String, Object> res)
    {
        if (res == null || res.isEmpty())
        {
            return "空响应";
        }

        Object success = res.get("success");
        if (success != null)
        {
            return "成功=" + success;
        }

        Object accepted = res.get("accepted");
        if (accepted != null)
        {
            return "已受理=" + accepted;
        }

        Object code = res.get("code");
        if (code != null)
        {
            return "响应码=" + code;
        }

        return "未知";
    }
}

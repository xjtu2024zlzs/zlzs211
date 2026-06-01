package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.client.PythonAlgorithmClient;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import com.ruoyi.project3.service.FaultPredictService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FaultPredictServiceImpl implements FaultPredictService
{
    private static final Logger log = LoggerFactory.getLogger(FaultPredictServiceImpl.class);

    private static final String TASK_TYPE = "FAULT_PREDICT";
    private static final String TASK_NAME = "故障预防";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String TASK_TIME_PATTERN = "yyyyMMddHHmmssSSS";

    @Resource
    private AlgTaskMapper algTaskMapper;

    @Resource
    private PythonAlgorithmClient pythonAlgorithmClient;

    @Override
    public Map<String, Object> start_pre_task(String request_body)
    {
        Map<String, Object> reqMap = reqMap(request_body);
        String taskId = "FP" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        String requestId = "REQ" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());

        AlgTaskResult record = new AlgTaskResult();
        record.setTaskId(taskId);
        record.setTaskType(TASK_TYPE);
        record.setTaskName(TASK_NAME);
        record.setStatus(STATUS_RUNNING);
        record.setRequestId(requestId);
        record.setStartAt(new Date());
        record.setReqJson(request_body);
        algTaskMapper.insertTask(record);

        try
        {
            Map<String, Object> pythonReq = assemblePredictRequest(reqMap, taskId, requestId);
            AlgTaskResult running = new AlgTaskResult();
            running.setTaskId(taskId);
            running.setStatus(STATUS_RUNNING);
            running.setReqJson(json(pythonReq));
            running.setStartAt(new Date());
            AlgTaskResult degradationTask = sourceTask(txt(pythonReq.get("sourceTaskId")));
            applyBusinessMeta(running, degradationTask);
            String flowTaskId = txt(pythonReq.get("featureAnalysisTaskId"));
            running.setFeatTaskId(flowTaskId == null ? degradationTask.getFeatTaskId() : flowTaskId);
            algTaskMapper.updateTask(running);
            log.info("Fault identify stage4 created, taskId={}, sourceTaskId={}, featureAnalysisTaskId={}, taskType={}",
                    taskId, pythonReq.get("sourceTaskId"), pythonReq.get("featureAnalysisTaskId"), TASK_TYPE);

            log.info("调用Python故障预防算法，任务ID={}，来源退化检测任务ID={}", taskId, pythonReq.get("sourceTaskId"));
            Map<String, Object> res = pythonAlgorithmClient.runPredict(pythonReq);
            Map<String, Object> result = unwrapResult(res);
            if (result.isEmpty())
            {
                throw new ServiceException("故障预防算法没有返回结果");
            }

            AlgTaskResult ok = new AlgTaskResult();
            ok.setTaskId(taskId);
            ok.setStatus(STATUS_SUCCESS);
            ok.setResJson(json(result));
            ok.setEndAt(new Date());
            ok.setSummary(cut(summary(result), 512));
            ok.setResultVal(cut(resultValue(result), 128));
            algTaskMapper.updateTask(ok);

            log.info("故障预防任务完成，任务ID={}，风险等级={}，剩余寿命={}", taskId, pick(result, "riskLevel", "risk_level"), resultValue(result));
            return resp(taskId, STATUS_SUCCESS, result, null);
        }
        catch (ServiceException e)
        {
            fail(taskId, e.getMessage());
            throw e;
        }
        catch (Exception e)
        {
            String msg = "故障预防算法执行失败: " + e.getMessage();
            fail(taskId, msg);
            log.error("故障预防任务失败，任务ID={}，原因={}", taskId, e.getMessage(), e);
            throw new ServiceException(msg);
        }
    }

    private Map<String, Object> assemblePredictRequest(Map<String, Object> reqMap, String taskId, String requestId)
    {
        Map<String, Object> params = firstMap(reqMap, "prediction_params", "predictionParams", "params");
        String degradationTaskId = firstText(reqMap, params, "sourceTaskId", "source_task_id", "degradationTaskId", "degradation_task_id");
        if (degradationTaskId == null)
        {
            throw new ServiceException("请先完成早期退化点检测，缺少接口三任务ID");
        }

        AlgTaskResult degradationTask = sourceTask(degradationTaskId);
        if (!STATUS_SUCCESS.equals(degradationTask.getStatus()))
        {
            throw new ServiceException("接口三退化检测任务尚未成功，不能执行故障预防");
        }
        Map<String, Object> detection = unwrapResult(jsonMap(degradationTask.getResJson()));
        String datasetId = txt(findVal(detection, 0, "datasetId", "dataset_id"));
        String detectionId = txt(findVal(detection, 0, "detectionId", "detection_id"));
        String detectionPath = txt(findVal(detection, 0, "detectionPath", "detection_path"));
        Object degradationTime = findVal(detection, 0, "earlyDegradationTime", "early_degradation_time", "earlyDegradationPoint", "early_degradation_point", "degradationTime", "degradation_time");

        String flowTaskId = firstText(reqMap, params, "featureAnalysisTaskId", "feature_analysis_task_id", "flowTaskId", "flow_task_id");
        if (flowTaskId == null)
        {
            flowTaskId = degradationTask.getFeatTaskId();
        }
        String featureTaskId = firstText(reqMap, params, "featureTaskId", "feature_task_id");
        if (featureTaskId == null)
        {
            featureTaskId = txt(findVal(detection, 0, "sourceTaskId", "source_task_id", "featureTaskId", "feature_task_id"));
        }
        if (featureTaskId == null && flowTaskId != null)
        {
            AlgTaskResult latestFeatureProcessing = algTaskMapper.getLatestFeatureProcessingByFlowTaskId(flowTaskId);
            if (latestFeatureProcessing != null)
            {
                featureTaskId = latestFeatureProcessing.getTaskId();
            }
        }

        String featurePath = txt(findVal(detection, 0, "featurePath", "feature_path"));
        String featureSetId = txt(findVal(detection, 0, "featureSetId", "feature_set_id"));
        Object samplingFrequency = findVal(detection, 0, "samplingFrequency", "sampling_frequency", "samplingRate", "sampling_rate");

        Map<String, Object> feature = Collections.emptyMap();
        if (featurePath == null || featureSetId == null || samplingFrequency == null)
        {
            if (featureTaskId == null)
            {
                throw new ServiceException("接口三结果缺少featurePath，且无法回溯接口二任务");
            }
            AlgTaskResult featureTask = sourceTask(featureTaskId);
            feature = unwrapResult(jsonMap(featureTask.getResJson()));
            if (flowTaskId == null)
            {
                flowTaskId = txt(featureTask.getFeatTaskId());
            }
            if (featurePath == null)
            {
                featurePath = txt(findVal(feature, 0, "featurePath", "feature_path"));
            }
            if (featureSetId == null)
            {
                featureSetId = txt(findVal(feature, 0, "featureSetId", "feature_set_id"));
            }
            if (datasetId == null)
            {
                datasetId = txt(findVal(feature, 0, "datasetId", "dataset_id"));
            }
            if (samplingFrequency == null)
            {
                samplingFrequency = findVal(feature, 0, "samplingFrequency", "sampling_frequency", "samplingRate", "sampling_rate");
            }
        }

        if (datasetId == null)
        {
            throw new ServiceException("接口三结果缺少datasetId");
        }
        if (featurePath == null)
        {
            throw new ServiceException("未找到featurePath，请先完成接口二特征分析");
        }
        if (detectionPath == null)
        {
            throw new ServiceException("接口三结果缺少detectionPath");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("taskType", TASK_TYPE);
        out.put("requestId", requestId);
        out.put("sourceTaskId", degradationTaskId);
        out.put("degradationTaskId", degradationTaskId);
        out.put("featureAnalysisTaskId", flowTaskId == null ? featureTaskId : flowTaskId);
        out.put("featureTaskId", featureTaskId);
        out.put("datasetId", datasetId);
        out.put("featureSetId", featureSetId);
        out.put("featurePath", featurePath);
        out.put("detectionId", detectionId);
        out.put("detectionPath", detectionPath);
        out.put("earlyDegradationTime", degradationTime);
        out.put("samplingFrequency", samplingFrequency == null ? 25600 : samplingFrequency);
        out.put("predictionHorizon", firstOrDefault(reqMap, params, 50, "predictionHorizon", "prediction_horizon"));
        out.put("riskThreshold", firstOrDefault(reqMap, params, 0.8, "riskThreshold", "risk_threshold"));
        out.put("rulUnit", firstOrDefault(reqMap, params, "second", "rulUnit", "rul_unit"));
        out.put("featureName", firstOrDefault(reqMap, params, "rms", "featureName", "feature_name"));

        String targetNodeId = firstText(reqMap, params, "targetNodeId", "target_node_id", "import_record_id");
        if (targetNodeId == null)
        {
            targetNodeId = degradationTask.getBizId();
        }
        out.put("targetNodeId", targetNodeId);
        out.put("detectionResult", detection);
        if (!feature.isEmpty())
        {
            out.put("featureResult", feature);
        }
        return out;
    }

    private AlgTaskResult sourceTask(String taskId)
    {
        String id = txt(taskId);
        if (id == null)
        {
            throw new ServiceException("来源任务ID不能为空");
        }
        AlgTaskResult record = algTaskMapper.getByTaskId(id);
        if (record == null)
        {
            throw new ServiceException("未找到来源任务: " + id);
        }
        return record;
    }

    private void applyBusinessMeta(AlgTaskResult target, AlgTaskResult source)
    {
        if (target == null || source == null)
        {
            return;
        }
        target.setAircraftId(source.getAircraftId());
        target.setSubsystemId(source.getSubsystemId());
        target.setEquipmentId(source.getEquipmentId());
        target.setComponentId(source.getComponentId());
        target.setBizId(source.getBizId());
        target.setBizLevel(source.getBizLevel());
        target.setBizName(source.getBizName());
    }

    private void fail(String taskId, String msg)
    {
        AlgTaskResult fail = new AlgTaskResult();
        fail.setTaskId(taskId);
        fail.setStatus(STATUS_FAILED);
        fail.setErrMsg(msg);
        fail.setEndAt(new Date());
        algTaskMapper.updateTask(fail);
        log.warn("故障预防任务失败，任务ID={}，原因={}", taskId, msg);
    }

    private Map<String, Object> resp(String taskId, String status, Map<String, Object> result, String err)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("task_id", taskId);
        Object flowTaskId = result == null ? null : findVal(result, 0, "featureAnalysisTaskId", "feature_analysis_task_id", "flowTaskId", "flow_task_id");
        out.put("flowTaskId", flowTaskId);
        out.put("flow_task_id", flowTaskId);
        out.put("status", status);
        out.put("result", result == null ? Collections.emptyMap() : result);
        out.put("errorMessage", err);
        out.put("error_message", err);
        return out;
    }

    private String summary(Map<String, Object> result)
    {
        String riskLevel = txt(findVal(result, 0, "riskLevel", "risk_level"));
        String riskScore = txt(findVal(result, 0, "riskScore", "risk_score"));
        String remaining = resultValue(result);
        StringBuilder sb = new StringBuilder("故障预防完成");
        if (riskLevel != null) sb.append("，风险等级: ").append(riskLevel);
        if (riskScore != null) sb.append("，评分: ").append(riskScore);
        if (remaining != null) sb.append("，剩余寿命: ").append(remaining);
        return sb.toString();
    }

    private String resultValue(Map<String, Object> result)
    {
        return txt(findVal(
                result,
                0,
                "predictedRemainingLife",
                "predicted_remaining_life",
                "remainingLife",
                "remaining_life",
                "remainingTime",
                "remaining_time"
        ));
    }

    private Map<String, Object> reqMap(String body)
    {
        if (body == null || body.trim().isEmpty())
        {
            throw new ServiceException("请求体不能为空");
        }
        try
        {
            JSONObject obj = JSON.parseObject(body);
            return obj == null ? Collections.emptyMap() : new LinkedHashMap<>(obj);
        }
        catch (Exception e)
        {
            throw new ServiceException("请求体不是合法JSON");
        }
    }

    private String json(Object obj)
    {
        try
        {
            return obj == null ? null : JSON.toJSONString(obj);
        }
        catch (Exception e)
        {
            return String.valueOf(obj);
        }
    }

    private Map<String, Object> jsonMap(String json)
    {
        if (json == null || json.trim().isEmpty())
        {
            return Collections.emptyMap();
        }
        try
        {
            JSONObject obj = JSON.parseObject(json);
            return obj == null ? Collections.emptyMap() : new LinkedHashMap<>(obj);
        }
        catch (Exception e)
        {
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value)
    {
        return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
    }

    private Map<String, Object> unwrapResult(Map<String, Object> raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return Collections.emptyMap();
        }
        if (raw.containsKey("riskScore") || raw.containsKey("preventionId") || raw.containsKey("featurePath"))
        {
            return new LinkedHashMap<>(raw);
        }
        Map<String, Object> data = asMap(raw.get("data"));
        if (!data.isEmpty())
        {
            Map<String, Object> result = asMap(data.get("result"));
            if (!result.isEmpty())
            {
                return new LinkedHashMap<>(result);
            }
            if (data.containsKey("riskScore") || data.containsKey("preventionId") || data.containsKey("featurePath"))
            {
                return new LinkedHashMap<>(data);
            }
        }
        Map<String, Object> result = asMap(raw.get("result"));
        if (!result.isEmpty())
        {
            return new LinkedHashMap<>(result);
        }
        return new LinkedHashMap<>(raw);
    }

    private Map<String, Object> firstMap(Map<String, Object> source, String... names)
    {
        for (String name : names)
        {
            Object value = pick(source, name);
            if (value instanceof Map)
            {
                return asMap(value);
            }
        }
        return Collections.emptyMap();
    }

    private String firstText(Map<String, Object> a, Map<String, Object> b, String... names)
    {
        Object value = first(a, names);
        if (value == null)
        {
            value = first(b, names);
        }
        return txt(value);
    }

    private Object firstOrDefault(Map<String, Object> a, Map<String, Object> b, Object def, String... names)
    {
        Object value = first(a, names);
        if (value == null)
        {
            value = first(b, names);
        }
        return value == null ? def : value;
    }

    private Object first(Map<String, Object> source, String... names)
    {
        if (source == null)
        {
            return null;
        }
        for (String name : names)
        {
            if (source.containsKey(name))
            {
                return source.get(name);
            }
        }
        return null;
    }

    private Object pick(Map<String, Object> source, String... names)
    {
        return first(source, names);
    }

    @SuppressWarnings("unchecked")
    private Object findVal(Object source, int depth, String... names)
    {
        if (source == null || depth > 8)
        {
            return null;
        }
        if (source instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>) source;
            Object v = pick(map, names);
            if (v != null)
            {
                return v;
            }
            for (Object item : map.values())
            {
                Object hit = findVal(item, depth + 1, names);
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
                Object hit = findVal(item, depth + 1, names);
                if (hit != null)
                {
                    return hit;
                }
            }
        }
        return null;
    }

    private String txt(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String cut(String value, int max)
    {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}

package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.assembler.algorithm.WarningDetectReqAssembler;
import com.ruoyi.project3.client.PythonAlgorithmClient;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.domain.algorithm.warning.WarningDetectReq;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import com.ruoyi.project3.mapper.FeedbackWarningMapper;
import com.ruoyi.project3.service.FeedbackWarningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
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

@Service
public class FeedbackWarningServiceImpl implements FeedbackWarningService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackWarningServiceImpl.class);

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;
    private static final String TASK_TYPE = "PROCESS_ANOMALY_DETECT";
    private static final String KQC_TASK_TYPE = "KQC_MINING";
    private static final String KQC_TASK_NAME = "关键质量特性挖掘";
    private static final String STATUS_PENDING = "PENDING";
    private static final String TASK_NAME = "工序异常检测";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String TASK_TIME_PATTERN = "yyyyMMddHHmmssSSS";

    private final FeedbackWarningMapper warningMapper;
    private final AlgTaskMapper algTaskMapper;
    private final WarningDetectReqAssembler reqAssembler;
    private final PythonAlgorithmClient pythonAlgorithmClient;
    private final FaultIdenFileProps faultIdenFileProps;

    public FeedbackWarningServiceImpl(
            FeedbackWarningMapper warningMapper,
            AlgTaskMapper algTaskMapper,
            WarningDetectReqAssembler reqAssembler,
            PythonAlgorithmClient pythonAlgorithmClient,
            FaultIdenFileProps faultIdenFileProps
    ) {
        this.warningMapper = warningMapper;
        this.algTaskMapper = algTaskMapper;
        this.reqAssembler = reqAssembler;
        this.pythonAlgorithmClient = pythonAlgorithmClient;
        this.faultIdenFileProps = faultIdenFileProps;
    }

    @Override
    public PageRows partPage(String keyword, String riskLevel, String sortBy, String sortOrder, Integer pageNum, Integer pageSize) {
        int p = pageNum(pageNum);
        int s = pageSize(pageSize);

        List<Map<String, Object>> rows = warningMapper.partList(keyword, riskLevel, sortBy, sortOrder, p, s);
        Long total = warningMapper.partCount(keyword, riskLevel);
        return page(rows, total);
    }

    @Override
    public PageRows partInstances(String partTemplateId, Integer pageNum, Integer pageSize) {
        if (blank(partTemplateId)) {
            return page(Collections.emptyList(), 0L);
        }
        int p = pageNum(pageNum);
        int s = pageSize(pageSize);

        List<Map<String, Object>> rows = warningMapper.partInstanceList(partTemplateId.trim(), p, s);
        Long total = warningMapper.partInstanceCount(partTemplateId.trim());
        return page(rows, total);
    }

    @Override
    public List<Map<String, Object>> partProc(String partId) {
        if (blank(partId)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> rows = warningMapper.partProc(partId.trim());
        return rows == null ? Collections.emptyList() : rows;
    }

    @Override
    public Map<String, Object> createProcessExecution(Map<String, Object> row) {
        Map<String, Object> data = processExecutionRow(row);
        if (blank(txt(data.get("process_exec_id")))) {
            data.put("process_exec_id", "PE" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date()) + UUID.randomUUID().toString().substring(0, 6));
        }
        requireProcessExecution(data);
        warningMapper.insertProcessExecution(data);
        return data;
    }

    @Override
    public void updateProcessExecution(String processExecId, Map<String, Object> row) {
        if (blank(processExecId)) {
            throw new ServiceException("工序执行ID不能为空");
        }
        Map<String, Object> data = processExecutionRow(row);
        data.put("process_exec_id", processExecId.trim());
        requireProcessExecution(data);
        int rows = warningMapper.updateProcessExecution(data);
        if (rows <= 0) {
            throw new ServiceException("未找到工序执行记录");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessExecution(String processExecId) {
        if (blank(processExecId)) {
            throw new ServiceException("工序执行ID不能为空");
        }
        warningMapper.deleteProcessAnomaliesByExecution(processExecId.trim());
        warningMapper.deleteProcessingQualityDataByExecution(processExecId.trim());
        int rows = warningMapper.deleteProcessExecution(processExecId.trim());
        if (rows <= 0) {
            throw new ServiceException("未找到工序执行记录");
        }
    }

    @Override
    public String partImage(String partId) {
        if (blank(partId)) {
            return null;
        }
        return txt(warningMapper.partImage(partId.trim()));
    }

    @Override
    public void updatePartImage(String partId, String imageUrl) {
        if (blank(partId)) {
            throw new ServiceException("零件ID不能为空");
        }
        int rows = warningMapper.updPartImage(partId.trim(), txt(imageUrl));
        if (rows <= 0) {
            throw new ServiceException("未找到零件，图片保存失败");
        }
    }

    @Override
    public PageRows devPage(String keyword, String riskLevel, Integer pageNum, Integer pageSize) {
        int p = pageNum(pageNum);
        int s = pageSize(pageSize);

        List<Map<String, Object>> rows = warningMapper.devList(keyword, riskLevel, p, s);
        Long total = warningMapper.devCount(keyword, riskLevel);
        return page(rows, total);
    }

    @Override
    public List<Map<String, Object>> devPart(String deviceId) {
        if (blank(deviceId)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> rows = warningMapper.devPart(deviceId.trim());
        return rows == null ? Collections.emptyList() : rows;
    }

    @Override
    public Map<String, Object> startDetect(String requestBody) {
        Map<String, Object> reqMap = reqMap(requestBody);
        String fingerprint = requestFingerprint(reqMap);
        AlgTaskResult existing = algTaskMapper.getRunningTaskByRemark(TASK_TYPE, fingerprint);
        if (existing != null) {
            AlgTaskResult synced = syncPythonTask(existing);
            return resp(synced.getTaskId(), synced.getStatus(), jsonMap(synced.getResJson()), kqcError(synced.getErrMsg()));
        }
        String taskId = "WA" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        String requestId = "REQ" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());

        AlgTaskResult record = new AlgTaskResult();
        record.setTaskId(taskId);
        record.setTaskType(TASK_TYPE);
        record.setTaskName(TASK_NAME);
        record.setStatus(STATUS_RUNNING);
        record.setRequestId(requestId);
        record.setStartAt(new Date());
        record.setReqJson(requestBody);
        record.setRemark(fingerprint);
        algTaskMapper.insertTask(record);

        try {
            WarningDetectReq req = reqAssembler.assemble(reqMap, taskId, requestId);
            algTaskMapper.updateTask(meta(req));

            if (usePythonTaskMode()) {
                Map<String, Object> pyTask = pythonAlgorithmClient.submitPythonTask("PROCESS_ANOMALY", req);
                if (Boolean.FALSE.equals(pyTask.get("success"))) {
                    throw new ServiceException(txt(pyTask.get("message")));
                }
                String pythonTaskId = txt(pyTask.get("taskId"));
                if (blank(pythonTaskId)) {
                    throw new ServiceException("Python算法任务提交失败：未返回pythonTaskId");
                }
                Map<String, Object> state = pythonState("PROCESS_ANOMALY", pyTask);
                AlgTaskResult running = new AlgTaskResult();
                running.setTaskId(taskId);
                running.setStatus(STATUS_RUNNING);
                running.setResJson(json(state));
                running.setSummary(cut(txt(pyTask.get("message")), 512));
                running.setRemark(fingerprint);
                algTaskMapper.updateTask(running);
                return resp(taskId, STATUS_RUNNING, state, null);
            }

            Map<String, Object> res = pythonAlgorithmClient.runWarningDetect(req);
            Map<String, Object> result = norm(res, taskId);
            if (result.isEmpty()) {
                throw new ServiceException("异常检测算法没有返回结果");
            }

            AlgTaskResult ok = new AlgTaskResult();
            ok.setTaskId(taskId);
            ok.setStatus(STATUS_SUCCESS);
            ok.setResJson(json(result));
            ok.setEndAt(new Date());
            ok.setSummary(cut(summary(result), 512));
            ok.setResultVal(cut(mainVal(result), 128));
            algTaskMapper.updateTask(ok);
            return resp(taskId, STATUS_SUCCESS, result, null);
        } catch (ServiceException e) {
            fail(taskId, e.getMessage());
            throw e;
        } catch (Exception e) {
            String msg = "异常检测算法执行失败：" + e.getMessage();
            fail(taskId, msg);
            throw new ServiceException(msg);
        }
    }

    @Override
    public Map<String, Object> kqcMining(String requestBody) {
        Map<String, Object> reqMap = reqMap(requestBody);
        if (blank(txt(pick(reqMap, "trainNumericPath", "train_numeric_path", "boschTrainNumericPath", "bosch_train_numeric_path")))) {
            throw new ServiceException("trainNumericPath不能为空");
        }
        String fingerprint = kqcFingerprint(reqMap);
        AlgTaskResult existing = algTaskMapper.getRunningTaskByRemark(KQC_TASK_TYPE, fingerprint);
        if (existing != null) {
            AlgTaskResult synced = syncKqcMiningTask(existing);
            return resp(synced.getTaskId(), synced.getStatus(), jsonMap(synced.getResJson()), synced.getErrMsg());
        }

        String taskId = "KQC" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        String requestId = "REQ" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        reqMap.put("taskId", taskId);

        AlgTaskResult record = new AlgTaskResult();
        record.setTaskId(taskId);
        record.setTaskType(KQC_TASK_TYPE);
        record.setTaskName(KQC_TASK_NAME);
        record.setStatus(STATUS_PENDING);
        record.setRequestId(requestId);
        record.setReqJson(json(reqMap));
        record.setResJson(json(kqcPythonState(KQC_TASK_TYPE, null)));
        record.setRemark(fingerprint);
        algTaskMapper.insertTask(record);

        try {
            Map<String, Object> pyTask = pythonAlgorithmClient.submitPythonTask(KQC_TASK_TYPE, reqMap);
            if (Boolean.FALSE.equals(pyTask.get("success"))) {
                throw new ServiceException(txt(pyTask.get("message")));
            }
            String pythonTaskId = txt(pyTask.get("taskId"));
            if (blank(pythonTaskId)) {
                throw new ServiceException("Python算法任务提交失败：未返回pythonTaskId");
            }
            Map<String, Object> state = kqcPythonState(KQC_TASK_TYPE, pyTask);
            AlgTaskResult running = new AlgTaskResult();
            running.setTaskId(taskId);
            running.setStatus(txt(pyTask.get("status")) == null ? STATUS_PENDING : txt(pyTask.get("status")));
            running.setStartAt(new Date());
            running.setResJson(json(state));
            running.setSummary(cut(txt(pyTask.get("message")), 512));
            running.setRemark(fingerprint);
            algTaskMapper.updateTask(running);
            return resp(taskId, running.getStatus(), state, null);
        } catch (ServiceException e) {
            fail(taskId, e.getMessage());
            throw e;
        } catch (Exception e) {
            String msg = "关键质量特性挖掘任务提交失败：" + e.getMessage();
            fail(taskId, msg);
            throw new ServiceException(msg);
        }
    }

    @Override
    public Map<String, Object> kqcMiningStatus(String taskId) {
        AlgTaskResult record = syncKqcMiningTask(kqcTask(taskId));
        return resp(record.getTaskId(), record.getStatus(), jsonMap(record.getResJson()), kqcError(record.getErrMsg()));
    }

    @Override
    public Map<String, Object> cancelKqcMining(String taskId) {
        AlgTaskResult record = kqcTask(taskId);
        if (STATUS_CANCELED.equals(record.getStatus())) {
            return resp(record.getTaskId(), STATUS_CANCELED, jsonMap(record.getResJson()), null);
        }
        if (STATUS_SUCCESS.equals(record.getStatus()) || STATUS_FAILED.equals(record.getStatus())) {
            Map<String, Object> out = resp(record.getTaskId(), record.getStatus(), jsonMap(record.getResJson()), kqcError(record.getErrMsg()));
            out.put("success", false);
            out.put("message", "任务已结束，不能取消");
            return out;
        }
        Map<String, Object> state = jsonMap(record.getResJson());
        String pythonTaskId = txt(pick(state, "pythonTaskId", "python_task_id"));
        Map<String, Object> py = Collections.emptyMap();
        if (!blank(pythonTaskId)) {
            try {
                py = pythonAlgorithmClient.cancelPythonTask(pythonTaskId);
            } catch (Exception e) {
                log.warn("取消Python关键质量特性挖掘任务失败，taskId={}, pythonTaskId={}, error={}", record.getTaskId(), pythonTaskId, e.getMessage());
                Map<String, Object> out = resp(record.getTaskId(), record.getStatus(), state, e.getMessage());
                out.put("success", false);
                out.put("message", "取消Python任务失败");
                return out;
            }
            if (Boolean.FALSE.equals(py.get("success"))) {
                Map<String, Object> out = resp(record.getTaskId(), record.getStatus(), state, txt(py.get("message")));
                out.put("success", false);
                out.put("message", txt(py.get("message")));
                return out;
            }
        }
        state.put("status", STATUS_CANCELED);
        state.put("message", "任务已取消");
        state.put("rawPythonCancel", py);
        AlgTaskResult cancel = new AlgTaskResult();
        cancel.setTaskId(record.getTaskId());
        cancel.setStatus(STATUS_CANCELED);
        cancel.setResJson(json(state));
        cancel.setEndAt(new Date());
        algTaskMapper.updateTask(cancel);
        return resp(record.getTaskId(), STATUS_CANCELED, state, null);
    }

    @Override
    public Map<String, Object> detectStatus(String taskId) {
        AlgTaskResult record = syncPythonTask(task(taskId));
        return resp(record.getTaskId(), record.getStatus(), jsonMap(record.getResJson()), record.getErrMsg());
    }

    @Override
    public List<Map<String, Object>> detectLogs(String taskId) {
        AlgTaskResult record = task(taskId);
        List<Map<String, Object>> logs = new ArrayList<>();
        logs.add(log(record.getCreateTime(), "INFO", "异常检测任务已创建"));
        if (record.getStartAt() != null) {
            logs.add(log(record.getStartAt(), "INFO", "异常检测算法调用中"));
        }
        if (STATUS_SUCCESS.equals(record.getStatus())) {
            logs.add(log(record.getEndAt(), "INFO", "异常检测完成"));
        }
        if (STATUS_FAILED.equals(record.getStatus())) {
            logs.add(log(record.getEndAt(), "ERROR", record.getErrMsg() == null ? "异常检测失败" : record.getErrMsg()));
        }
        return logs;
    }

    @Override
    public Map<String, Object> cancelDetect(String taskId) {
        AlgTaskResult record = task(taskId);
        if ("CANCELED".equals(record.getStatus())) {
            return resp(record.getTaskId(), "CANCELED", jsonMap(record.getResJson()), null);
        }
        if (STATUS_SUCCESS.equals(record.getStatus()) || STATUS_FAILED.equals(record.getStatus())) {
            Map<String, Object> out = resp(record.getTaskId(), record.getStatus(), jsonMap(record.getResJson()), record.getErrMsg());
            out.put("success", false);
            out.put("message", "任务已结束，不能取消");
            return out;
        }
        Map<String, Object> state = jsonMap(record.getResJson());
        String pythonTaskId = txt(pick(state, "pythonTaskId", "python_task_id"));
        Map<String, Object> py = Collections.emptyMap();
        if (!blank(pythonTaskId)) {
            try {
                py = pythonAlgorithmClient.cancelPythonTask(pythonTaskId);
            } catch (Exception e) {
                log.warn("取消Python工序异常检测任务失败，taskId={}, pythonTaskId={}, error={}", record.getTaskId(), pythonTaskId, e.getMessage());
                Map<String, Object> out = resp(record.getTaskId(), record.getStatus(), state, e.getMessage());
                out.put("success", false);
                out.put("message", "取消Python任务失败");
                return out;
            }
            if (Boolean.FALSE.equals(py.get("success"))) {
                Map<String, Object> out = resp(record.getTaskId(), record.getStatus(), state, txt(py.get("message")));
                out.put("success", false);
                out.put("message", txt(py.get("message")));
                return out;
            }
        }
        state.put("status", "CANCELED");
        state.put("message", "任务已取消");
        state.put("rawPythonCancel", py);
        AlgTaskResult cancel = new AlgTaskResult();
        cancel.setTaskId(record.getTaskId());
        cancel.setStatus("CANCELED");
        cancel.setResJson(json(state));
        cancel.setEndAt(new Date());
        algTaskMapper.updateTask(cancel);
        return resp(record.getTaskId(), "CANCELED", state, null);
    }

    @Override
    public PageRows detectResults(String status, String targetType, String targetId, String beginTime, String endTime, Integer pageNum, Integer pageSize) {
        int p = pageNum(pageNum);
        int s = pageSize(pageSize);
        int offset = (p - 1) * s;
        List<AlgTaskResult> list = algTaskMapper.getWarningResult(txt(status), txt(targetType), txt(targetId), txt(beginTime), txt(endTime), offset, s);
        Long total = algTaskMapper.countWarningResult(txt(status), txt(targetType), txt(targetId), txt(beginTime), txt(endTime));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AlgTaskResult item : list) {
            rows.add(row(item));
        }
        return page(rows, total);
    }

    @Override
    public PageRows kqcMiningResults(String keyword, String status, Integer pageNum, Integer pageSize) {
        int p = pageNum(pageNum);
        int s = pageSize(pageSize);
        int offset = (p - 1) * s;
        List<AlgTaskResult> list = algTaskMapper.getKqcMiningResult(txt(keyword), txt(status), offset, s);
        Long total = algTaskMapper.countKqcMiningResult(txt(keyword), txt(status));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AlgTaskResult item : list) {
            rows.add(kqcRow(item));
        }
        return page(rows, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteAlgorithmResult(String taskId) {
        AlgTaskResult record = algTask(taskId);
        int deletedFiles = deleteAlgorithmArtifactFiles(record);
        int deleted = algTaskMapper.deleteByFlowTaskId(record.getTaskId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", record.getTaskId());
        result.put("task_id", record.getTaskId());
        result.put("deleted", deleted);
        result.put("deletedCount", deleted);
        result.put("deleted_count", deleted);
        result.put("deletedAlgorithmFiles", deletedFiles);
        result.put("deleted_algorithm_files", deletedFiles);
        return result;
    }

    private AlgTaskResult meta(WarningDetectReq req) {
        AlgTaskResult r = new AlgTaskResult();
        r.setTaskId(req.getTaskId());
        r.setTaskType(TASK_TYPE);
        r.setTaskName(TASK_NAME);
        r.setStatus(STATUS_RUNNING);
        r.setRequestId(req.getRequestId());
        r.setBizId(req.getTargetId());
        r.setBizLevel(req.getTargetType());
        r.setBizName(req.getTargetName());
        r.setReqJson(json(req));
        r.setStartAt(new Date());
        return r;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> reqMap(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new ServiceException("请求体不能为空");
        }
        try {
            JSONObject obj = JSON.parseObject(body);
            return obj == null ? Collections.emptyMap() : new LinkedHashMap<>(obj);
        } catch (Exception e) {
            throw new ServiceException("请求体不是合法JSON");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> norm(Map<String, Object> res, String taskId) {
        Map<String, Object> data = asMap(res == null ? null : res.get("data"));
        Map<String, Object> result = asMap(data.get("result"));
        if (result.isEmpty()) {
            result = new LinkedHashMap<>(data);
            result.remove("logs");
            result.remove("status");
            result.remove("taskId");
            result.remove("task_id");
            result.remove("requestId");
        }
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        result.putIfAbsent("taskId", taskId);
        result.putIfAbsent("task_id", taskId);
        result.putIfAbsent("status", data.get("status") == null ? STATUS_SUCCESS : data.get("status"));
        if (data.get("logs") != null) {
            result.put("logs", data.get("logs"));
        }
        return result;
    }

    private Map<String, Object> row(AlgTaskResult record) {
        Map<String, Object> result = jsonMap(record.getResJson());
        Map<String, Object> req = jsonMap(record.getReqJson());
        Map<String, Object> fileInfo = asMap(pick(req, "fileInfo", "file_info"));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("taskId", record.getTaskId());
        row.put("task_id", record.getTaskId());
        row.put("status", record.getStatus());
        row.put("targetType", record.getBizLevel());
        row.put("targetId", record.getBizId());
        row.put("targetName", record.getBizName());
        row.put("dataFile", txt(pick(fileInfo, "fileName", "file_name")));
        row.put("dataFileUrl", txt(pick(fileInfo, "fileUrl", "file_url")));
        row.put("isAbnormal", pick(result, "isAbnormal", "is_abnormal"));
        row.put("abnormalLevel", txt(pick(result, "abnormalLevel", "abnormal_level")));
        row.put("abnormalScore", pick(result, "abnormalScore", "abnormal_score"));
        row.put("abnormalTime", txt(pick(result, "abnormalTime", "abnormal_time")));
        row.put("summary", record.getSummary());
        row.put("errorMessage", record.getErrMsg());
        row.put("createTime", record.getCreateTime());
        return row;
    }

    private Map<String, Object> kqcRow(AlgTaskResult record) {
        Map<String, Object> result = jsonMap(record.getResJson());
        Map<String, Object> top = topKqc(result);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("taskId", record.getTaskId());
        row.put("task_id", record.getTaskId());
        row.put("status", record.getStatus());
        row.put("summary", record.getSummary());
        row.put("resultValue", record.getResultVal());
        row.put("result_value", record.getResultVal());
        row.put("outputDir", txt(pick(result, "outputDir", "output_dir", "resultDir", "result_dir")));
        row.put("output_dir", row.get("outputDir"));
        row.put("kqcCount", kqcCount(result));
        row.put("kqc_count", row.get("kqcCount"));
        row.put("topFeature", txt(pick(top, "source_variable", "sourceVariable", "feature", "kqc", "name")));
        row.put("top_feature", row.get("topFeature"));
        row.put("score", pick(top, "weight", "score", "frequency"));
        row.put("errorMessage", record.getErrMsg());
        row.put("error_message", record.getErrMsg());
        row.put("createTime", record.getCreateTime());
        row.put("updateTime", record.getUpdateTime());
        row.put("result", result);
        return row;
    }

    private AlgTaskResult task(String taskId) {
        String id = txt(taskId);
        if (id == null) {
            throw new ServiceException("任务ID不能为空");
        }
        AlgTaskResult record = algTaskMapper.getByTaskId(id);
        if (record == null || !TASK_TYPE.equals(record.getTaskType())) {
            throw new ServiceException("未找到异常检测任务");
        }
        return record;
    }

    private AlgTaskResult kqcTask(String taskId) {
        String id = txt(taskId);
        if (id == null) {
            throw new ServiceException("任务ID不能为空");
        }
        AlgTaskResult record = algTaskMapper.getByTaskId(id);
        if (record == null || !KQC_TASK_TYPE.equals(record.getTaskType())) {
            throw new ServiceException("未找到关键质量特性挖掘任务");
        }
        return record;
    }

    private AlgTaskResult algTask(String taskId) {
        String id = txt(taskId);
        if (id == null) {
            throw new ServiceException("任务ID不能为空");
        }
        AlgTaskResult record = algTaskMapper.getByTaskId(id);
        if (record == null || !(TASK_TYPE.equals(record.getTaskType()) || KQC_TASK_TYPE.equals(record.getTaskType()))) {
            throw new ServiceException("未找到可删除的算法结果");
        }
        return record;
    }

    private int deleteAlgorithmArtifactFiles(AlgTaskResult record) {
        Set<String> paths = new LinkedHashSet<>();
        collectArtifactPaths(jsonMap(record.getResJson()), paths);
        return deleteFiles(paths);
    }

    @SuppressWarnings("unchecked")
    private void collectArtifactPaths(Object value, Set<String> paths) {
        if (value == null) {
            return;
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (isArtifactPathKey(entry.getKey())) {
                    String path = txt(entry.getValue());
                    if (path != null) {
                        paths.add(path);
                    }
                }
                collectArtifactPaths(entry.getValue(), paths);
            }
            return;
        }
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                collectArtifactPaths(item, paths);
            }
        }
    }

    private boolean isArtifactPathKey(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase();
        if ("sourcefile".equals(lower) || "source_file".equals(lower) || lower.endsWith("url")) {
            return false;
        }
        return lower.endsWith("path") || lower.endsWith("dir") || lower.endsWith("file") || lower.endsWith("filepath") || lower.endsWith("filename");
    }

    private int deleteFiles(Set<String> paths) {
        int deleted = 0;
        for (String item : paths == null ? Collections.<String>emptySet() : paths) {
            Path path = safeDeletePath(item);
            if (path == null || !Files.exists(path)) {
                continue;
            }
            try {
                if (Files.isDirectory(path)) {
                    try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
                        List<Path> targets = stream.sorted(Comparator.reverseOrder()).toList();
                        for (Path target : targets) {
                            Files.deleteIfExists(target);
                        }
                    }
                } else {
                    Files.deleteIfExists(path);
                }
                deleted++;
            } catch (IOException e) {
                log.warn("Delete warning algorithm file failed, path={}", path, e);
            }
        }
        return deleted;
    }

    private Path safeDeletePath(String text) {
        String value = cleanLocalPath(text);
        if (value == null || value.startsWith("http://") || value.startsWith("https://")) {
            return null;
        }
        Path path;
        try {
            path = Paths.get(value).toAbsolutePath().normalize();
        } catch (Exception e) {
            log.warn("Skip invalid warning algorithm file path, path={}", value, e);
            return null;
        }
        if (isUnderRoot(path, faultIdenFileProps.getSourceRoot()) || isUnderRoot(path, faultIdenFileProps.getExportDir())) {
            return path;
        }
        log.warn("Skip deleting warning algorithm file outside configured roots, path={}", path);
        return null;
    }

    private String cleanLocalPath(Object value) {
        String text = txt(value);
        if (text == null) {
            return null;
        }
        text = text.replaceAll("[\\u200e\\u200f\\u202a-\\u202e\\u2066-\\u2069\\ufeff]", "").trim();
        return text.isEmpty() ? null : text;
    }

    private boolean isUnderRoot(Path path, String rootText) {
        String rootValue = txt(rootText);
        if (rootValue == null) {
            return false;
        }
        Path root = Paths.get(rootValue).toAbsolutePath().normalize();
        return path.startsWith(root);
    }

    private boolean usePythonTaskMode() {
        return true;
    }

    private boolean isTerminalStatus(String status) {
        return STATUS_SUCCESS.equals(status) || STATUS_FAILED.equals(status) || STATUS_CANCELED.equals(status);
    }

    private String kqcFingerprint(Map<String, Object> reqMap) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("taskType", KQC_TASK_TYPE);
        key.put("request", reqMap == null ? Collections.emptyMap() : reqMap);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(JSON.toJSONString(key).getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder("KQC_MINING:");
            for (byte b : bytes) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            return "KQC_MINING:" + JSON.toJSONString(key);
        }
    }

    private String requestFingerprint(Map<String, Object> reqMap) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("taskType", TASK_TYPE);
        key.put("partId", txt(pick(reqMap, "partId", "part_id")));
        key.put("processId", txt(pick(reqMap, "processId", "process_id")));
        key.put("dataUsage", txt(pick(reqMap, "dataUsage", "data_usage")));
        key.put("boschTrainNumericPath", txt(pick(reqMap, "boschTrainNumericPath", "bosch_train_numeric_path", "trainNumericPath", "train_numeric_path")));
        key.put("station", txt(pick(reqMap, "station")));
        key.put("featureColName", txt(pick(reqMap, "featureColName", "feature_col")));
        key.put("featureCol", pick(reqMap, "featureCol", "feature_col"));
        key.put("sampleIds", pick(reqMap, "sampleIds", "sample_ids"));
        key.put("trainSampleIds", pick(reqMap, "trainSampleIds", "train_sample_ids"));
        key.put("detectSampleIds", pick(reqMap, "detectSampleIds", "detect_sample_ids"));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(JSON.toJSONString(key).getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder("PROCESS_ANOMALY:");
            for (byte b : bytes) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            return "PROCESS_ANOMALY:" + JSON.toJSONString(key);
        }
    }

    private Map<String, Object> pythonState(String algorithmType, Map<String, Object> pyTask) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("pythonTaskId", txt(pyTask.get("taskId")));
        state.put("python_task_id", txt(pyTask.get("taskId")));
        state.put("pythonAlgorithmType", algorithmType);
        state.put("python_algorithm_type", algorithmType);
        state.put("status", txt(pyTask.get("status")));
        state.put("progress", pyTask.get("progress"));
        state.put("message", txt(pyTask.get("message")));
        state.put("errorMessage", txt(pick(pyTask, "error", "errorMessage", "message")));
        state.put("rawPythonTask", pyTask);
        return state;
    }

    private Map<String, Object> kqcPythonState(String algorithmType, Map<String, Object> pyTask) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("pythonTaskId", pyTask == null ? null : txt(pyTask.get("taskId")));
        state.put("python_task_id", pyTask == null ? null : txt(pyTask.get("taskId")));
        state.put("pythonAlgorithmType", algorithmType);
        state.put("python_algorithm_type", algorithmType);
        state.put("status", pyTask == null ? STATUS_PENDING : txt(pyTask.get("status")));
        state.put("progress", pyTask == null ? 0 : pyTask.get("progress"));
        state.put("message", pyTask == null ? "任务已创建" : txt(pyTask.get("message")));
        state.put("errorMessage", pyTask == null ? null : txt(pick(pyTask, "error", "errorMessage", "message")));
        state.put("error_message", state.get("errorMessage"));
        state.put("rawPythonTask", pyTask);
        return state;
    }

    @SuppressWarnings("unchecked")
    private AlgTaskResult syncKqcMiningTask(AlgTaskResult record) {
        if (record == null || !KQC_TASK_TYPE.equals(record.getTaskType()) || isTerminalStatus(record.getStatus())) {
            return record;
        }
        Map<String, Object> state = jsonMap(record.getResJson());
        String pythonTaskId = txt(pick(state, "pythonTaskId", "python_task_id"));
        if (blank(pythonTaskId)) {
            return record;
        }
        Map<String, Object> py;
        try {
            py = pythonAlgorithmClient.getPythonTaskStatus(pythonTaskId);
        } catch (Exception e) {
            log.warn("同步Python关键质量特性挖掘任务状态失败，taskId={}, pythonTaskId={}, error={}", record.getTaskId(), pythonTaskId, e.getMessage());
            state.put("status", STATUS_RUNNING);
            state.put("message", "查询Python任务状态失败，等待下次轮询重试");
            state.put("errorMessage", e.getMessage());
            state.put("error_message", e.getMessage());
            AlgTaskResult running = new AlgTaskResult();
            running.setTaskId(record.getTaskId());
            running.setStatus(STATUS_RUNNING);
            running.setResJson(json(state));
            running.setSummary(cut(txt(state.get("message")), 512));
            algTaskMapper.updateTask(running);
            record.setStatus(STATUS_RUNNING);
            record.setResJson(json(state));
            return record;
        }

        String pyStatus = txt(py.get("status"));
        Map<String, Object> next = kqcPythonState(KQC_TASK_TYPE, py);
        if (STATUS_SUCCESS.equals(pyStatus)) {
            try {
                Map<String, Object> resultResponse = pythonAlgorithmClient.getPythonTaskResult(pythonTaskId);
                Map<String, Object> result = normKqcResult(resultResponse, record.getTaskId());
                if (result.isEmpty()) {
                    throw new ServiceException("KQC_MINING算法返回空结果");
                }
                next.putAll(saveKqcMiningResult(record.getTaskId(), result));
                record.setStatus(STATUS_SUCCESS);
                record.setResJson(json(next));
                record.setErrMsg(null);
                record.setEndAt(new Date());
            } catch (Exception e) {
                log.error("Python关键质量特性挖掘已完成但Java保存结果失败，taskId={}, pythonTaskId={}, error={}", record.getTaskId(), pythonTaskId, e.getMessage(), e);
                next.put("status", STATUS_RUNNING);
                next.put("progress", 99);
                next.put("message", "Python算法已完成，Java保存KQC_MINING结果失败，等待下次轮询重试");
                next.put("errorMessage", e.getMessage());
                next.put("error_message", e.getMessage());
                AlgTaskResult retry = new AlgTaskResult();
                retry.setTaskId(record.getTaskId());
                retry.setStatus(STATUS_RUNNING);
                retry.setResJson(json(next));
                retry.setSummary(cut(txt(next.get("message")), 512));
                algTaskMapper.updateTask(retry);
                record.setStatus(STATUS_RUNNING);
                record.setResJson(json(next));
            }
            return record;
        }
        if (STATUS_FAILED.equals(pyStatus)) {
            String err = kqcError(txt(pick(py, "error", "message", "errorMessage")));
            next.put("errorMessage", err);
            next.put("error_message", err);
            AlgTaskResult fail = new AlgTaskResult();
            fail.setTaskId(record.getTaskId());
            fail.setStatus(STATUS_FAILED);
            fail.setErrMsg(err);
            fail.setResJson(json(next));
            fail.setEndAt(new Date());
            algTaskMapper.updateTask(fail);
            record.setStatus(STATUS_FAILED);
            record.setErrMsg(err);
            record.setResJson(json(next));
            return record;
        }
        if (STATUS_CANCELED.equals(pyStatus)) {
            AlgTaskResult cancel = new AlgTaskResult();
            cancel.setTaskId(record.getTaskId());
            cancel.setStatus(STATUS_CANCELED);
            cancel.setResJson(json(next));
            cancel.setEndAt(new Date());
            algTaskMapper.updateTask(cancel);
            record.setStatus(STATUS_CANCELED);
            record.setResJson(json(next));
            return record;
        }

        AlgTaskResult running = new AlgTaskResult();
        running.setTaskId(record.getTaskId());
        running.setStatus(STATUS_RUNNING);
        running.setResJson(json(next));
        running.setSummary(cut(txt(py.get("message")), 512));
        algTaskMapper.updateTask(running);
        record.setStatus(STATUS_RUNNING);
        record.setResJson(json(next));
        return record;
    }

    @SuppressWarnings("unchecked")
    private AlgTaskResult syncPythonTask(AlgTaskResult record) {
        if (record == null || isTerminalStatus(record.getStatus()) || !(STATUS_RUNNING.equals(record.getStatus()) || "PENDING".equals(record.getStatus()))) {
            return record;
        }
        Map<String, Object> state = jsonMap(record.getResJson());
        String pythonTaskId = txt(pick(state, "pythonTaskId", "python_task_id"));
        if (blank(pythonTaskId)) {
            return record;
        }
        Map<String, Object> py;
        try {
            py = pythonAlgorithmClient.getPythonTaskStatus(pythonTaskId);
        } catch (Exception e) {
            log.warn("同步Python工序异常检测任务状态失败，taskId={}, pythonTaskId={}, error={}", record.getTaskId(), pythonTaskId, e.getMessage());
            state.put("message", "查询Python任务状态失败，等待下次轮询重试");
            state.put("errorMessage", e.getMessage());
            AlgTaskResult running = new AlgTaskResult();
            running.setTaskId(record.getTaskId());
            running.setStatus(STATUS_RUNNING);
            running.setResJson(json(state));
            running.setSummary(cut(txt(state.get("message")), 512));
            algTaskMapper.updateTask(running);
            record.setResJson(json(state));
            return record;
        }
        String pyStatus = txt(py.get("status"));
        Map<String, Object> next = pythonState(txt(pick(state, "pythonAlgorithmType", "python_algorithm_type")), py);
        Object pyResult = py.get("result");
        if ("SUCCESS".equals(pyStatus) && pyResult instanceof Map) {
            try {
                Map<String, Object> result = norm((Map<String, Object>) pyResult, record.getTaskId());
                next.putAll(result);
                AlgTaskResult ok = new AlgTaskResult();
                ok.setTaskId(record.getTaskId());
                ok.setStatus(STATUS_SUCCESS);
                ok.setResJson(json(next));
                ok.setEndAt(new Date());
                ok.setSummary(cut(summary(result), 512));
                ok.setResultVal(cut(mainVal(result), 128));
                algTaskMapper.updateTask(ok);
                record.setStatus(STATUS_SUCCESS);
                record.setResJson(json(next));
                record.setErrMsg(null);
            } catch (Exception e) {
                log.error("Python工序异常检测已完成但Java保存结果失败，taskId={}, pythonTaskId={}, error={}", record.getTaskId(), pythonTaskId, e.getMessage(), e);
                next.put("status", STATUS_RUNNING);
                next.put("progress", 99);
                next.put("message", "Python算法已完成，Java保存结果失败，等待下次轮询重试");
                next.put("errorMessage", e.getMessage());
                AlgTaskResult retry = new AlgTaskResult();
                retry.setTaskId(record.getTaskId());
                retry.setStatus(STATUS_RUNNING);
                retry.setResJson(json(next));
                retry.setSummary(cut(txt(next.get("message")), 512));
                algTaskMapper.updateTask(retry);
                record.setStatus(STATUS_RUNNING);
                record.setResJson(json(next));
            }
            return record;
        }
        if ("FAILED".equals(pyStatus)) {
            String err = txt(pick(py, "error", "message"));
            AlgTaskResult fail = new AlgTaskResult();
            fail.setTaskId(record.getTaskId());
            fail.setStatus(STATUS_FAILED);
            fail.setErrMsg(err);
            fail.setResJson(json(next));
            fail.setEndAt(new Date());
            algTaskMapper.updateTask(fail);
            record.setStatus(STATUS_FAILED);
            record.setErrMsg(err);
            record.setResJson(json(next));
            return record;
        }
        if ("CANCELED".equals(pyStatus)) {
            AlgTaskResult cancel = new AlgTaskResult();
            cancel.setTaskId(record.getTaskId());
            cancel.setStatus("CANCELED");
            cancel.setResJson(json(next));
            cancel.setEndAt(new Date());
            algTaskMapper.updateTask(cancel);
            record.setStatus("CANCELED");
            record.setResJson(json(next));
            return record;
        }
        AlgTaskResult running = new AlgTaskResult();
        running.setTaskId(record.getTaskId());
        running.setStatus(STATUS_RUNNING);
        running.setResJson(json(next));
        running.setSummary(cut(txt(py.get("message")), 512));
        algTaskMapper.updateTask(running);
        record.setResJson(json(next));
        return record;
    }

    private void fail(String taskId, String msg) {
        AlgTaskResult fail = new AlgTaskResult();
        fail.setTaskId(taskId);
        fail.setStatus(STATUS_FAILED);
        fail.setErrMsg(msg);
        fail.setEndAt(new Date());
        algTaskMapper.updateTask(fail);
    }

    private Map<String, Object> resp(String taskId, String status, Map<String, Object> result, String err) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("task_id", taskId);
        out.put("status", status);
        out.put("result", result == null ? Collections.emptyMap() : result);
        out.put("errorMessage", err);
        out.put("error_message", err);
        return out;
    }

    private String kqcError(String message) {
        String text = txt(message);
        if (blank(text)) {
            return text;
        }
        if ("'Response'".equals(text) || "Response".equals(text) || text.contains("KeyError") || text.contains("响应结果列")) {
            return "当前 train_numeric.csv 缺少响应结果列，无法直接进行关键质量特性挖掘。请使用包含响应结果列的训练数据；如果没有响应结果列，请提供已完成的 KQC输出目录用于关键工序识别。该目录需要包含 bosch_adj_matrix_selected.csv 和 bosch_edge_frequency.csv。";
        }
        return text;
    }

    private String summary(Map<String, Object> result) {
        List<String> parts = new ArrayList<>();
        String level = txt(pick(result, "abnormalLevel", "abnormal_level"));
        String score = txt(pick(result, "abnormalScore", "abnormal_score"));
        String time = txt(pick(result, "abnormalTime", "abnormal_time"));
        String suggestion = txt(pick(result, "suggestion"));
        if (level != null) parts.add("异常等级：" + level);
        if (score != null) parts.add("异常分数：" + score);
        if (time != null) parts.add("异常时间：" + time);
        if (suggestion != null) parts.add("建议：" + suggestion);
        return parts.isEmpty() ? "异常检测完成" : String.join("，", parts);
    }

    private String mainVal(Map<String, Object> result) {
        String level = txt(pick(result, "abnormalLevel", "abnormal_level"));
        String score = txt(pick(result, "abnormalScore", "abnormal_score"));
        if (level != null && score != null) {
            return level + "/" + score;
        }
        return level == null ? score : level;
    }

    private Map<String, Object> normKqcResult(Map<String, Object> response, String taskId) {
        Map<String, Object> result = asMap(response == null ? null : response.get("result"));
        Map<String, Object> data = asMap(result.get("data"));
        Map<String, Object> dataResult = asMap(data.get("result"));
        if (!dataResult.isEmpty()) {
            result = new LinkedHashMap<>(dataResult);
        } else {
            Map<String, Object> payload = asMap(result.get("payload"));
            if (!payload.isEmpty()) {
                result = new LinkedHashMap<>(payload);
            } else if (result.containsKey("success") && !data.isEmpty()) {
                result = new LinkedHashMap<>(data);
                result.remove("logs");
                result.remove("status");
                result.remove("taskId");
                result.remove("task_id");
            }
        }
        if (result.isEmpty()) {
            result = asMap(response == null ? null : response.get("data"));
        }
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        result.putIfAbsent("taskId", taskId);
        result.putIfAbsent("task_id", taskId);
        result.putIfAbsent("status", STATUS_SUCCESS);
        return result;
    }

    private Map<String, Object> saveKqcMiningResult(String taskId, Map<String, Object> pythonResult) {
        Map<String, Object> result = pythonResult == null ? Collections.emptyMap() : new LinkedHashMap<>(pythonResult);
        if (result.isEmpty()) {
            throw new ServiceException("KQC_MINING算法返回空结果");
        }
        AlgTaskResult ok = new AlgTaskResult();
        ok.setTaskId(taskId);
        ok.setStatus(STATUS_SUCCESS);
        ok.setResJson(json(result));
        ok.setEndAt(new Date());
        ok.setSummary(cut(kqcSummary(result), 512));
        ok.setResultVal(cut(kqcResultVal(result), 128));
        int rows = algTaskMapper.updateTask(ok);
        if (rows <= 0) {
            throw new ServiceException("KQC_MINING结果保存失败");
        }
        return result;
    }

    private String kqcSummary(Map<String, Object> result) {
        int count = kqcCount(result);
        return "关键质量特性挖掘完成，共识别出 " + count + " 个关键质量特性";
    }

    private String kqcResultVal(Map<String, Object> result) {
        int count = kqcCount(result);
        Map<String, Object> top = topKqc(result);
        String name = txt(pick(top, "source_variable", "sourceVariable", "feature", "kqc", "name"));
        String score = txt(pick(top, "weight", "score", "frequency"));
        if (name != null && score != null) {
            return count + "/" + name + "/" + score;
        }
        if (name != null) {
            return count + "/" + name;
        }
        return String.valueOf(count);
    }

    private int kqcCount(Map<String, Object> result) {
        Object influenceCount = pick(result, "influenceCount", "influence_count", "kqcCount", "kqc_count", "selectedFeatureCount", "selected_feature_count");
        if (influenceCount instanceof Number) {
            return ((Number) influenceCount).intValue();
        }
        try {
            if (influenceCount != null) {
                return Integer.parseInt(String.valueOf(influenceCount));
            }
        } catch (Exception ignored) {
        }
        Object rows = pick(result, "topInfluences", "top_influences", "candidateKqcs", "candidate_kqcs");
        return rows instanceof List ? ((List<?>) rows).size() : 0;
    }

    private Map<String, Object> topKqc(Map<String, Object> result) {
        Object rows = pick(result, "topInfluences", "top_influences", "candidateKqcs", "candidate_kqcs");
        if (rows instanceof List && !((List<?>) rows).isEmpty()) {
            return asMap(((List<?>) rows).get(0));
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> log(Date time, String level, String content) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("time", time);
        item.put("level", level);
        item.put("content", content);
        return item;
    }

    private String json(Object obj) {
        try {
            return obj == null ? null : JSON.toJSONString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private Map<String, Object> jsonMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            JSONObject obj = JSON.parseObject(json);
            return obj == null ? Collections.emptyMap() : new LinkedHashMap<>(obj);
        } catch (Exception e) {
            throw new ServiceException("异常检测任务结果JSON解析失败");
        }
    }

    private Map<String, Object> processExecutionRow(Map<String, Object> row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("process_exec_id", txt(pick(row, "process_exec_id", "processExecId")));
        data.put("work_order_id", txt(pick(row, "work_order_id", "workOrderId")));
        data.put("process_def_id", txt(pick(row, "process_def_id", "processDefId", "process_id", "processId")));
        data.put("device_id", txt(pick(row, "device_id", "deviceId")));
        data.put("start_time", txt(pick(row, "start_time", "startTime")));
        data.put("end_time", txt(pick(row, "end_time", "endTime")));
        data.put("operator", txt(pick(row, "operator")));
        data.put("status", txt(pick(row, "status")));
        return data;
    }

    private void requireProcessExecution(Map<String, Object> row) {
        if (blank(txt(row.get("work_order_id")))) {
            throw new ServiceException("工单ID不能为空");
        }
        if (blank(txt(row.get("process_def_id")))) {
            throw new ServiceException("工序定义ID不能为空");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
    }

    private Object pick(Map<String, Object> source, String... names) {
        if (source == null) {
            return null;
        }
        for (String name : names) {
            if (source.containsKey(name)) {
                return source.get(name);
            }
        }
        return null;
    }

    private String txt(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String cut(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private PageRows page(List<Map<String, Object>> rows, Long total) {
        PageRows pageRows = new PageRows();
        pageRows.set_rows(rows == null ? Collections.emptyList() : rows);
        pageRows.set_total(total == null ? 0L : total);
        return pageRows;
    }

    private int pageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int pageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }

        // FIXME: 大分页仍会拖慢聚合查询，后续需切换到游标或预聚合视图。
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

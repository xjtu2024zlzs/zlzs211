package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.assembler.algorithm.WarningDetectReqAssembler;
import com.ruoyi.project3.client.PythonAlgorithmClient;
import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.domain.algorithm.warning.WarningDetectReq;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import com.ruoyi.project3.mapper.FeedbackWarningMapper;
import com.ruoyi.project3.service.FeedbackWarningService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackWarningServiceImpl implements FeedbackWarningService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;
    private static final String TASK_TYPE = "PROCESS_ANOMALY_DETECT";
    private static final String TASK_NAME = "工序异常检测";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String TASK_TIME_PATTERN = "yyyyMMddHHmmssSSS";

    private final FeedbackWarningMapper warningMapper;
    private final AlgTaskMapper algTaskMapper;
    private final WarningDetectReqAssembler reqAssembler;
    private final PythonAlgorithmClient pythonAlgorithmClient;

    public FeedbackWarningServiceImpl(
            FeedbackWarningMapper warningMapper,
            AlgTaskMapper algTaskMapper,
            WarningDetectReqAssembler reqAssembler,
            PythonAlgorithmClient pythonAlgorithmClient
    ) {
        this.warningMapper = warningMapper;
        this.algTaskMapper = algTaskMapper;
        this.reqAssembler = reqAssembler;
        this.pythonAlgorithmClient = pythonAlgorithmClient;
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
        algTaskMapper.insertTask(record);

        try {
            WarningDetectReq req = reqAssembler.assemble(reqMap, taskId, requestId);
            algTaskMapper.updateTask(meta(req));

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
    public Map<String, Object> detectStatus(String taskId) {
        AlgTaskResult record = task(taskId);
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

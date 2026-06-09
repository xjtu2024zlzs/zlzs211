package com.ruoyi.project3.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.domain.algorithm.feature.FeatureReq;
import com.ruoyi.project3.domain.algorithm.predict.PredictReq;
import com.ruoyi.project3.domain.algorithm.warning.WarningDetectReq;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PythonAlgorithmClient
{

    private static final Logger log = LoggerFactory.getLogger(PythonAlgorithmClient.class);
    private static final String DEFAULT_FAILURE_MESSAGE = "Python算法服务返回失败";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    @Resource
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    public Map<String, Object> identify_process(Map<String, Object> req)
    {
        String taskId = req == null ? null : get_string(req.get("taskId"));
        if (req != null && StringUtils.isEmpty(get_string(req.get("authCode"))))
        {
            req.put("authCode", pythonAlgorithmProperties.getKeyProcessAuthCode());
        }
        if (pythonAlgorithmProperties.isMock())
        {
            Map<String, Object> mock = mock_identify(req);
            log.info("Python关键工序识别算法返回模拟响应，任务ID={}，摘要={}", taskId, response_summary(mock));
            return mock;
        }


        String url = identify_url();
        log.info("调用Python关键工序识别算法，任务ID={}，地址={}", taskId, url);

        Map<String, Object> res = parse_identify(
                sendPost(url, JSON.toJSONString(req), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        log.info("Python关键工序识别算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> detect_degradation(Map<String, Object> req)
    {
        String taskId = req == null ? null : get_string(req.get("taskId"));
        if (pythonAlgorithmProperties.isMock())
        {
            log.warn("退化点检测不使用模拟模式，正在调用真实Python服务，任务ID={}", taskId);
        }
        String url = pythonAlgorithmProperties.getFaultUrl();
        if (StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少配置：python.algorithm.faultUrl 或 python.algorithm.baseUrl");
        }


        log.info("调用Python退化点检测算法，任务ID={}，地址={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(req), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        log.info("Python退化点检测算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> runFeature(FeatureReq request)
    {
        String taskId = request == null ? null : request.getTaskId();
        if (pythonAlgorithmProperties.isMock())
        {
            log.warn("特征分析不使用模拟模式，正在调用真实Python服务，任务ID={}", taskId);
        }

        String url = feature_url();
        log.info("调用Python特征分析算法，任务ID={}，地址={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(feature_payload(request)), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        if (!feature_ok(res))
        {
            throw new ServiceException(feature_err(res));
        }
        log.info("Python特征分析算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> runFeatureProcessing(Map<String, Object> request)
    {
        String taskId = request == null ? null : get_string(request.get("taskId"));
        if (pythonAlgorithmProperties.isMock())
        {
            log.warn("特征处理不使用模拟模式，正在调用真实Python服务，任务ID={}", taskId);
        }

        String url = feature_processing_url();
        log.info("调用Python特征处理算法，任务ID={}，地址={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(request), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        if (!feature_ok(res))
        {
            throw new ServiceException(feature_err(res));
        }
        log.info("Python特征处理算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    private Map<String, Object> feature_payload(FeatureReq request)
    {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (request == null)
        {
            return payload;
        }
        put(payload, "taskId", request.getTaskId());
        put(payload, "datasetId", request.getDatasetId());
        put(payload, "taskType", "FEATURE_ANALYSIS");
        put(payload, "fileMode", request.getFileMode());
        put(payload, "sampleIds", request.getSampleIds());
        put(payload, "filePath", request.getFilePath());
        put(payload, "fileUrl", request.getFileUrl());
        put(payload, "filePaths", request.getFilePaths());
        put(payload, "fileUrls", request.getFileUrls());
        put(payload, "batchPath", request.getBatchPath());
        put(payload, "samplingFrequency", request.getSamplingFrequency());
        put(payload, "columnIndex", request.getColumnIndex());
        put(payload, "windowSize", request.getWindowSize());
        put(payload, "overlapPercent", request.getOverlapPercent());
        put(payload, "removeOutliers", request.getRemoveOutliers());
        put(payload, "maxSeconds", request.getMaxSeconds());
        return payload;
    }

    private void put(Map<String, Object> payload, String key, Object value)
    {
        if (value == null)
        {
            return;
        }
        if (value instanceof String && StringUtils.isEmpty((String) value))
        {
            return;
        }
        if (value instanceof java.util.Collection && ((java.util.Collection<?>) value).isEmpty())
        {
            return;
        }
        payload.put(key, value);
    }

    public Map<String, Object> runPredict(PredictReq request)
    {
        String taskId = request == null ? null : request.getTaskId();
        if (request != null && StringUtils.isEmpty(request.getAuthCode()))
        {
            request.setAuthCode(pythonAlgorithmProperties.getPredictAuthCode());
        }
        if (pythonAlgorithmProperties.isMock())
        {
            Map<String, Object> mock = mock_predict(request);
            log.info("Python预测算法返回模拟响应，任务ID={}，摘要={}", taskId, response_summary(mock));
            return mock;
        }

        String url = predict_url();
        log.info("调用Python预测算法，任务ID={}，地址={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(request), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        if (!isAlgorithmSuccess(res))
        {
            throw new ServiceException(predict_err(res));
        }
        log.info("Python预测算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> runPredict(Map<String, Object> request)
    {
        String taskId = request == null ? null : get_string(request.get("taskId"));
        if (request != null && StringUtils.isEmpty(get_string(request.get("authCode"))))
        {
            request.put("authCode", pythonAlgorithmProperties.getPredictAuthCode());
        }
        if (pythonAlgorithmProperties.isMock())
        {
            log.warn("故障预防不使用模拟模式，正在调用真实Python服务，任务ID={}", taskId);
        }

        String url = predict_url();
        log.info("调用Python故障预防算法，任务ID={}，地址={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(request), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        if (!isAlgorithmSuccess(res))
        {
            throw new ServiceException(predict_err(res));
        }
        log.info("Python故障预防算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> runWarningDetect(WarningDetectReq request)
    {
        String taskId = request == null ? null : request.getTaskId();
        if (request != null && StringUtils.isEmpty(request.getAuthCode()))
        {
            request.setAuthCode(pythonAlgorithmProperties.getWarningAuthCode());
        }
        if (pythonAlgorithmProperties.isMock())
        {
            log.warn("工序异常检测已禁用模拟响应，当前 python.algorithm.mock=true 也将调用真实 Python 服务，任务ID={}", taskId);
        }
        if (pythonAlgorithmProperties.isMock() && shouldUseWarningMock())
        {
            Map<String, Object> mock = mock_warning(request);
            log.info("Python预警检测算法返回模拟响应，任务ID={}，摘要={}", taskId, response_summary(mock));
            return mock;
        }

        String url = warning_url();
        log.info("调用Python预警检测算法，任务ID={}，地址={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(request), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        if (!isAlgorithmSuccess(res))
        {
            throw new ServiceException(warning_err(res));
        }
        log.info("Python预警检测算法已返回，任务ID={}，摘要={}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> runKqcMining(Map<String, Object> request)
    {
        String taskId = request == null ? null : get_string(request.get("taskId"));
        if (pythonAlgorithmProperties.isMock())
        {
            log.warn("鍏抽敭璐ㄩ噺鐗规€ф寲鎺樹笉浣跨敤妯℃嫙妯″紡锛屾鍦ㄨ皟鐢ㄧ湡瀹濸ython鏈嶅姟锛屼换鍔D={}", taskId);
        }

        String url = kqc_mining_url();
        log.info("璋冪敤Python鍏抽敭璐ㄩ噺鐗规€ф寲鎺樼畻娉曪紝浠诲姟ID={}锛屽湴鍧€={}", taskId, url);
        Map<String, Object> res = parse_json(
                sendPost(url, JSON.toJSONString(request == null ? Collections.emptyMap() : request), MediaType.APPLICATION_JSON_VALUE),
                url
        );
        if (!isAlgorithmSuccess(res))
        {
            throw new ServiceException(algorithm_err(res));
        }
        log.info("Python鍏抽敭璐ㄩ噺鐗规€ф寲鎺樼畻娉曞凡杩斿洖锛屼换鍔D={}锛屾憳瑕?{}", taskId, response_summary(res));
        return res;
    }

    public Map<String, Object> submitPythonTask(String algorithmType, Object payload)
    {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("algorithmType", algorithmType);
        request.put("payload", payload == null ? Collections.emptyMap() : payload);
        return parse_json(sendPost(task_url(), JSON.toJSONString(request), MediaType.APPLICATION_JSON_VALUE), task_url());
    }

    public Map<String, Object> getPythonTaskStatus(String pythonTaskId)
    {
        return parse_json(sendGet(task_url() + "/" + pythonTaskId), task_url() + "/" + pythonTaskId);
    }

    public Map<String, Object> getPythonTaskLogs(String pythonTaskId)
    {
        return parse_json(sendGet(task_url() + "/" + pythonTaskId + "/logs"), task_url() + "/" + pythonTaskId + "/logs");
    }

    public Map<String, Object> getPythonTaskResult(String pythonTaskId)
    {
        return parse_json(sendGet(task_url() + "/" + pythonTaskId + "/result"), task_url() + "/" + pythonTaskId + "/result");
    }

    public Map<String, Object> cancelPythonTask(String pythonTaskId)
    {
        return parse_json(sendPost(task_url() + "/" + pythonTaskId + "/cancel", "{}", MediaType.APPLICATION_JSON_VALUE), task_url() + "/" + pythonTaskId + "/cancel");
    }

    private String identify_url()
    {
        String url = pythonAlgorithmProperties.getKeyProcessUrl();
        if (StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少配置：python.algorithm.baseUrl");
        }

        String path = pythonAlgorithmProperties.getKeyProcessPath();
        if (StringUtils.isEmpty(path) && StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少配置：python.algorithm.keyProcessPath");
        }

        return url;
    }

    private String feature_url()
    {
        String baseUrl = pythonAlgorithmProperties.getBaseUrl();
        if (StringUtils.isEmpty(baseUrl))
        {
            throw new ServiceException("缺少配置：python.algorithm.baseUrl");
        }

        String path = pythonAlgorithmProperties.getFeatureAnalysisPath();
        if (StringUtils.isEmpty(path))
        {
            throw new ServiceException("缺少配置：python.algorithm.featureAnalysisPath");
        }

        return StringUtils.endsWith(baseUrl, "/") ? baseUrl + path.substring(1) : baseUrl + path;
    }

    private String feature_processing_url()
    {
        String baseUrl = pythonAlgorithmProperties.getBaseUrl();
        if (StringUtils.isEmpty(baseUrl))
        {
            throw new ServiceException("缺少配置：python.algorithm.baseUrl");
        }

        String path = pythonAlgorithmProperties.getFeatureProcessingPath();
        if (StringUtils.isEmpty(path))
        {
            throw new ServiceException("缺少配置：python.algorithm.featureProcessingPath");
        }

        return StringUtils.endsWith(baseUrl, "/") ? baseUrl + path.substring(1) : baseUrl + path;
    }

    private String predict_url()
    {
        String url = pythonAlgorithmProperties.getPredictUrl();
        if (StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少配置：python.algorithm.predictUrl 或 python.algorithm.baseUrl");
        }
        return url;
    }

    private String warning_url()
    {
        String url = pythonAlgorithmProperties.getWarningUrl();
        if (StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少配置：python.algorithm.warningUrl 或 python.algorithm.baseUrl");
        }
        return url;
    }

    private String kqc_mining_url()
    {
        String url = pythonAlgorithmProperties.getKqcMiningUrl();
        if (StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少配置：python.algorithm.kqcMiningUrl 或 python.algorithm.baseUrl");
        }
        return url;
    }

    private String task_url()
    {
        String baseUrl = pythonAlgorithmProperties.getBaseUrl();
        if (StringUtils.isEmpty(baseUrl))
        {
            throw new ServiceException("缺少配置：python.algorithm.baseUrl");
        }
        return StringUtils.endsWith(baseUrl, "/") ? baseUrl + "algorithm/tasks" : baseUrl + "/algorithm/tasks";
    }




    private String sendPost(String url, String body, String mediaType)
    {
        try
        {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofMinutes(10))
                    .header("Content-Type", mediaType)
                    .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400)
            {
                throw http_status_exception(url, response.statusCode(), response.body());
            }
            return response.body();
        }
        catch (HttpTimeoutException e)
        {
            log.error("调用Python算法服务超时，地址={}", url, e);
            throw new ServiceException("Python算法服务请求超时，请确认服务已启动且算法未卡住：" + url);
        }
        catch (ConnectException e)
        {
            log.error("无法连接Python算法服务，地址={}", url, e);
            throw new ServiceException("无法连接Python算法服务，请确认服务已启动并监听端口：" + url);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            log.error("调用Python算法服务被中断，地址={}", url, e);
            throw new ServiceException("调用Python算法服务被中断，请稍后重试");
        }
        catch (IllegalArgumentException e)
        {
            log.error("Python算法服务地址配置错误，地址={}", url, e);
            throw new ServiceException("Python算法服务地址配置错误：" + url);
        }
        catch (Exception e)
        {
            log.error("调用Python算法服务失败，地址={}", url, e);
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.contains("Connection refused") || message.contains("No connection"))
            {
                throw new ServiceException("无法连接Python算法服务，请确认服务已启动并监听端口：" + url);
            }
            if (message.contains("timed out") || message.contains("timeout"))
            {
                throw new ServiceException("Python算法服务请求超时，请确认服务已启动且算法未卡住：" + url);
            }
            throw new ServiceException("调用Python算法服务失败：" + message);
        }
    }

    private String sendGet(String url)
    {
        try
        {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofMinutes(2))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400)
            {
                throw http_status_exception(url, response.statusCode(), response.body());
            }
            return response.body();
        }
        catch (HttpTimeoutException e)
        {
            throw new ServiceException("Python算法任务状态查询超时：" + url);
        }
        catch (ConnectException e)
        {
            throw new ServiceException("无法连接Python算法服务，请确认服务已启动并监听端口：" + url);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("查询Python算法任务被中断，请稍后重试");
        }
        catch (IllegalArgumentException e)
        {
            throw new ServiceException("Python算法服务地址配置错误：" + url);
        }
        catch (Exception e)
        {
            throw new ServiceException("查询Python算法任务失败：" + (e.getMessage() == null ? "" : e.getMessage()));
        }
    }

    private ServiceException http_status_exception(String url, int statusCode, String body)
    {
        String message = python_error_message(body);
        if (statusCode == 404)
        {
            return new ServiceException("Python算法服务未注册当前接口，请确认9741端口启动的是最新的 python/project3/main.py，并已重启服务。接口：" + url);
        }
        if (statusCode == 422)
        {
            return new ServiceException("Python算法服务参数校验失败：" + (StringUtils.isEmpty(message) ? raw_summary(body) : message));
        }
        if (statusCode >= 500)
        {
            return new ServiceException("Python算法服务内部错误：" + (StringUtils.isEmpty(message) ? raw_summary(body) : message));
        }
        return new ServiceException("Python算法服务返回HTTP " + statusCode + "：" + (StringUtils.isEmpty(message) ? raw_summary(body) : message));
    }

    private String python_error_message(String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return "";
        }
        try
        {
            JSONObject json = JSON.parseObject(body);
            String msg = get_string(json.get("message"));
            if (StringUtils.isEmpty(msg))
            {
                msg = get_string(json.get("msg"));
            }
            if (StringUtils.isEmpty(msg))
            {
                msg = get_string(json.get("error"));
            }
            if (StringUtils.isEmpty(msg))
            {
                msg = get_string(json.get("detail"));
            }
            return msg;
        }
        catch (Exception ignored)
        {
            return "";
        }
    }
    private Map<String, Object> parse_identify(String rawPythonResponse, String requestUrl)
    {
        if (StringUtils.isEmpty(rawPythonResponse))
        {
            throw new ServiceException("Python算法服务超时或返回空响应");
        }
        JSONObject identifyResponse;
        try
        {
            identifyResponse = JSON.parseObject(rawPythonResponse);
        }
        catch (Exception parseException)
        {
            log.error("解析Python响应失败，地址={}，响应体={}", requestUrl, raw_summary(rawPythonResponse), parseException);
            throw new ServiceException("Python算法服务返回了非法响应体");
        }
        if (identifyResponse == null || identifyResponse.isEmpty())
        {
            throw new ServiceException("Python算法服务返回空JSON");
        }
        if (!ok(identifyResponse))
        {
            throw new ServiceException(err_msg(identifyResponse));
        }

        return new LinkedHashMap<>(identifyResponse);
    }

    private Map<String, Object> parse_json(String rawPythonResponse, String requestUrl)
    {
        if (StringUtils.isEmpty(rawPythonResponse))
        {
            throw new ServiceException("Python算法服务超时或返回空响应");
        }
        JSONObject responseBody;
        try
        {
            responseBody = JSON.parseObject(rawPythonResponse);
        }
        catch (Exception parseException)
        {
            log.error("解析Python响应失败，地址={}，响应体={}", requestUrl, raw_summary(rawPythonResponse), parseException);
            throw new ServiceException("Python算法服务返回了非法响应体");
        }
        if (responseBody == null || responseBody.isEmpty())
        {
            throw new ServiceException("Python算法服务返回空JSON");
        }
        return new LinkedHashMap<>(responseBody);
    }

    private boolean ok(JSONObject identifyResponse)
    {
        if (identifyResponse.containsKey("success"))
        {
            return identifyResponse.getBooleanValue("success");
        }
        if (identifyResponse.containsKey("accepted"))
        {
            return identifyResponse.getBooleanValue("accepted");
        }
        if (!identifyResponse.containsKey("code"))
        {
            return false;
        }
        return is_success_code(identifyResponse.get("code"));
    }

    private boolean is_success_code(Object responseCode)
    {
        if (responseCode == null)
        {
            return false;
        }

        if (responseCode instanceof Number)
        {
            int codeValue = ((Number) responseCode).intValue();
            return codeValue == 200 || codeValue == 0 || codeValue == 1;
        }
        String codeText = StringUtils.trim(String.valueOf(responseCode));
        if (StringUtils.isEmpty(codeText))
        {
            return false;
        }
        return "200".equals(codeText)
                || "0".equals(codeText)
                || "1".equals(codeText)
                || "ok".equalsIgnoreCase(codeText)
                || "success".equalsIgnoreCase(codeText);
    }

    private String err_msg(JSONObject identifyResponse)
    {
        String message = identifyResponse.getString("message");
        if (StringUtils.isNotEmpty(message))
        {
            return message;
        }

        String msg = identifyResponse.getString("msg");
        if (StringUtils.isNotEmpty(msg))
        {
            return msg;
        }

        String error = identifyResponse.getString("error");
        if (StringUtils.isNotEmpty(error))
        {
            return error;
        }

        return DEFAULT_FAILURE_MESSAGE;
    }

    private Map<String, Object> mock_identify(Map<String, Object> req)
    {
        String taskId = req == null ? null : get_string(req.get("taskId"));
        String requestId = req == null ? null : get_string(req.get("requestId"));

        Map<String, Object> identifyResult = new LinkedHashMap<>();
        identifyResult.put("success", true);
        identifyResult.put("taskId", taskId);
        identifyResult.put("requestId", requestId);
        identifyResult.put("message", "模拟识别请求已受理");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("keyProcessCode", "KP-MOCK-001");
        payload.put("confidence", 0.91);
        payload.put("keyProcessName", "模拟关键工序");
        payload.put("score", 0.91);
        payload.put("reason", "mock reason");
        payload.put("suggestion", "mock suggestion");
        payload.put("candidateProcesses", Arrays.asList(
                candidate("KP-MOCK-001", "模拟关键工序", 0.91, 1),
                candidate("KP-MOCK-002", "模拟工序2", 0.84, 2),
                candidate("KP-MOCK-003", "模拟工序", 0.76, 3)
        ));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("requestId", requestId);
        data.put("status", "SUCCESS");
        data.put("result", payload);
        data.put("logs", Collections.singletonList(log("模拟关键工序识别完成")));

        identifyResult.put("code", 0);
        identifyResult.put("payload", payload);
        identifyResult.put("data", data);
        return identifyResult;
    }

    private Map<String, Object> candidate(String code, String name, Object score, Object rank)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("processCode", code);
        item.put("processName", name);
        item.put("score", score);
        item.put("rank", rank);
        return item;
    }


    private Map<String, Object> mock_degradation(Map<String, Object> req)
    {
        String taskId = req == null ? null : get_string(req.get("taskId"));
        String requestId = req == null ? null : get_string(req.get("requestId"));

        Map<String, Object> degradationResult = new LinkedHashMap<>();

        degradationResult.put("deviceId", req == null ? null : req.get("deviceId"));
        degradationResult.put("earlyDegradationPoint", 600.0);
        degradationResult.put("degradationPointUnit", "second");
        degradationResult.put("detectTime", format_now());



        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("taskId", taskId);
        responseData.put("requestId", requestId);
        responseData.put("status", "SUCCESS");

        responseData.put("result", degradationResult);
        responseData.put("logs", java.util.Collections.emptyList());



        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 1);
        response.put("msg", "success");
        response.put("data", responseData);
        return response;
    }

    private Map<String, Object> mock_feature(FeatureReq request)
    {
        String taskId = request == null ? null : request.getTaskId();
        String requestId = request == null ? null : request.getRequestId();

        Map<String, Object> series = new LinkedHashMap<>();
        series.put("time", Arrays.asList(0.5, 1.0, 1.5, 2.0, 2.5));
        series.put("mean", Arrays.asList(0.012, 0.015, 0.014, 0.017, 0.019));
        series.put("std", Arrays.asList(0.421, 0.438, 0.452, 0.467, 0.489));
        series.put("peak", Arrays.asList(1.28, 1.35, 1.42, 1.51, 1.63));
        series.put("rms", Arrays.asList(0.425, 0.442, 0.455, 0.471, 0.493));
        series.put("kurtosis", Arrays.asList(3.12, 3.25, 3.41, 3.58, 3.76));
        series.put("max", Arrays.asList(1.21, 1.30, 1.38, 1.44, 1.59));

        Map<String, Object> signal = new LinkedHashMap<>();
        signal.put("title", "time domain signal");
        signal.put("xAxisName", "time(s)");
        signal.put("yAxisName", "振动加速度");
        signal.put("time", Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5));
        signal.put("amplitude", Arrays.asList(0.012, -0.031, 0.045, -0.022, 0.018, 0.025));

        Map<String, Object> spectrum = new LinkedHashMap<>();
        spectrum.put("title", "time frequency spectrum");
        spectrum.put("xAxisName", "time(s)");
        spectrum.put("yAxisName", "frequency(Hz)");
        spectrum.put("zAxisName", "amplitude");
        spectrum.put("time", Arrays.asList(0, 1, 2, 3, 4, 5));
        spectrum.put("frequency", Arrays.asList(0, 500, 1000, 1500, 2000, 2500));
        spectrum.put("amplitudeMatrix", Arrays.asList(
                Arrays.asList(0.0012, 0.0018, 0.0021, 0.0015, 0.0011, 0.0008),
                Arrays.asList(0.0013, 0.0020, 0.0025, 0.0017, 0.0012, 0.0009),
                Arrays.asList(0.0014, 0.0022, 0.0030, 0.0021, 0.0015, 0.0010),
                Arrays.asList(0.0016, 0.0025, 0.0034, 0.0024, 0.0017, 0.0012),
                Arrays.asList(0.0019, 0.0029, 0.0039, 0.0028, 0.0020, 0.0014),
                Arrays.asList(0.0022, 0.0032, 0.0045, 0.0031, 0.0024, 0.0016)
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("sampleCount", 256);
        summary.put("duration", 2.56);
        summary.put("featureWindowCount", 5);
        summary.put("maxAmplitude", 1.63);
        summary.put("dominantFrequency", 2500.0);

        Map<String, Object> result = new LinkedHashMap<>();
        if (request != null)
        {
            result.put("aircraftId", request.getAircraftId());
            result.put("aircraftCode", request.getAircraftCode());
            result.put("targetType", request.getTargetType());
            result.put("targetId", request.getTargetId());
            result.put("targetName", request.getTargetName());
            result.put("hierarchyContext", request.getHierarchyContext());
            result.put("signalType", request.getSignalType());
            result.put("channel", request.getChannel());
            result.put("samplingRate", request.getSamplingRate());
            result.put("sourceType", request.getSourceType());
            result.put("sourceFile", request.getFileInfo());
            result.put("analysisStartTime", request.getAnalysisStartTime());
            result.put("analysisEndTime", request.getAnalysisEndTime());
        }
        result.put("featureSeries", series);
        result.put("timeDomainSignal", signal);
        result.put("timeFrequencySpectrum", spectrum);
        result.put("summary", summary);

        Map<String, Object> logItem = new LinkedHashMap<>();
        logItem.put("time", format_now());
        logItem.put("level", "INFO");
        logItem.put("content", "模拟特征分析完成");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("requestId", requestId);
        data.put("status", "SUCCESS");
        data.put("result", result);
        data.put("logs", Collections.singletonList(logItem));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 1);
        response.put("msg", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> mock_predict(PredictReq request)
    {
        String taskId = request == null ? null : request.getTaskId();
        String requestId = request == null ? null : request.getRequestId();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("predictedFailureTime", "2026-05-24 12:00:00");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("requestId", requestId);
        data.put("status", "SUCCESS");
        data.putAll(result);
        data.put("logs", Collections.singletonList(log("模拟预测完成")));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private boolean shouldUseWarningMock()
    {
        return false;
    }

    private Map<String, Object> mock_warning(WarningDetectReq request)
    {
        String taskId = request == null ? null : request.getTaskId();
        String requestId = request == null ? null : request.getRequestId();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isAbnormal", true);
        result.put("abnormalLevel", "HIGH");
        result.put("abnormalScore", 0.86);
        result.put("abnormalTime", format_now());
        result.put("abnormalPoint", point(120, 0.86));
        result.put("suggestion", "模拟预警建议");
        result.put("curveData", Arrays.asList(
                point(0, 0.12),
                point(20, 0.18),
                point(40, 0.31),
                point(60, 0.45),
                point(80, 0.63),
                point(100, 0.78),
                point(120, 0.86)
        ));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("requestId", requestId);
        data.put("status", "SUCCESS");
        data.put("result", result);
        data.put("logs", Collections.singletonList(log("模拟预警检测完成")));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> point(Object x, Object y)
    {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("x", x);
        point.put("y", y);
        return point;
    }

    @SuppressWarnings("unchecked")
    private boolean feature_ok(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return false;
        }
        Object code = response.get("code");
        if (code instanceof Number && ((Number) code).intValue() == 1)
        {
            return true;
        }
        if ("1".equals(String.valueOf(code)))
        {
            return true;
        }
        Object topStatus = response.get("status");
        if ("SUCCESS".equalsIgnoreCase(String.valueOf(topStatus)))
        {
            return true;
        }
        Object data = response.get("data");
        if (data instanceof Map)
        {
            Object status = ((Map<String, Object>) data).get("status");
            return "SUCCESS".equalsIgnoreCase(String.valueOf(status));
        }
        return false;
    }

    /**
     * Checks common success flags returned by algorithm services, including success, code, and data.status.
     */
    @SuppressWarnings("unchecked")
    private boolean isAlgorithmSuccess(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return false;
        }
        Object success = response.get("success");
        if (success instanceof Boolean)
        {
            return (Boolean) success;
        }
        Object topStatus = response.get("status");
        if ("SUCCESS".equalsIgnoreCase(String.valueOf(topStatus)))
        {
            return true;
        }
        Object code = response.get("code");
        if (code instanceof Number)
        {
            int c = ((Number) code).intValue();
            return c == 0 || c == 200 || c == 1;
        }
        if ("0".equals(String.valueOf(code)) || "1".equals(String.valueOf(code)) || "200".equals(String.valueOf(code)) || "success".equalsIgnoreCase(String.valueOf(code)))
        {
            return true;
        }
        Object data = response.get("data");
        if (data instanceof Map)
        {
            Object status = ((Map<String, Object>) data).get("status");
            return "SUCCESS".equalsIgnoreCase(String.valueOf(status));
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private String predict_err(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return "故障预测算法返回空结果";
        }
        String msg = get_string(response.get("message"));
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("msg"));
        }
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("error"));
        }
        Object data = response.get("data");
        if (StringUtils.isEmpty(msg) && data instanceof Map)
        {
            msg = get_string(((Map<String, Object>) data).get("errorMessage"));
        }
        return StringUtils.isEmpty(msg) ? "故障预测算法执行失败" : "故障预测算法执行失败：" + msg;
    }

    @SuppressWarnings("unchecked")
    private String warning_err(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return "预警检测算法返回空结果";
        }
        String msg = get_string(response.get("message"));
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("msg"));
        }
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("error"));
        }
        Object data = response.get("data");
        if (StringUtils.isEmpty(msg) && data instanceof Map)
        {
            msg = get_string(((Map<String, Object>) data).get("errorMessage"));
        }
        return StringUtils.isEmpty(msg) ? "预警检测算法执行失败" : "预警检测算法执行失败：" + msg;
    }

    @SuppressWarnings("unchecked")
    private String algorithm_err(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return "Python算法服务返回空结果";
        }
        String msg = get_string(response.get("message"));
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("msg"));
        }
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("error"));
        }
        Object payload = response.get("payload");
        if (StringUtils.isEmpty(msg) && payload instanceof Map)
        {
            msg = get_string(((Map<String, Object>) payload).get("message"));
        }
        Object data = response.get("data");
        if (StringUtils.isEmpty(msg) && data instanceof Map)
        {
            msg = get_string(((Map<String, Object>) data).get("errorMessage"));
        }
        return StringUtils.isEmpty(msg) ? "Python算法执行失败" : "Python算法执行失败：" + msg;
    }

    private Map<String, Object> log(String content)
    {
        Map<String, Object> logItem = new LinkedHashMap<>();
        logItem.put("time", format_now());
        logItem.put("level", "INFO");
        logItem.put("content", content);
        return logItem;
    }

    @SuppressWarnings("unchecked")
    private String feature_err(Map<String, Object> response)
    {
        if (response == null || response.isEmpty())
        {
            return "特征算法返回空结果";
        }
        String msg = get_string(response.get("msg"));
        if (StringUtils.isEmpty(msg))
        {
            msg = get_string(response.get("message"));
        }
        if (StringUtils.isEmpty(msg))
        {
            msg = DEFAULT_FAILURE_MESSAGE;
        }
        Object data = response.get("data");
        if (data instanceof Map)
        {
            Object logs = ((Map<String, Object>) data).get("logs");
            if (logs != null)
            {
                return msg + ", logs=" + JSON.toJSONString(logs);
            }
        }
        return msg;
    }


    private String format_now()
    {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(new Date());
    }
    private String get_string(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private String raw_summary(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return "空响应";
        }
        String text = raw.replaceAll("\\s+", " ");
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }

    private String response_summary(Map<String, Object> identifyResponse)
    {
        if (identifyResponse == null || identifyResponse.isEmpty())
        {
            return "空响应";
        }

        Object success = identifyResponse.get("success");
        if (success != null)
        {
            return "成功=" + success;
        }

        Object accepted = identifyResponse.get("accepted");
        if (accepted != null)
        {
            return "瀹稿弶甯撮弨?" + accepted;
        }

        Object code = identifyResponse.get("code");
        if (code != null)
        {
            return "响应码=" + code;
        }
        return "未知";
    }
}

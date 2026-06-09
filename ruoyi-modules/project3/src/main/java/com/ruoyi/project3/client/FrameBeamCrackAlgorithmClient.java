package com.ruoyi.project3.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.project3.config.FrameBeamCrackProperties;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyRequest;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyResultVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamMetricsVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamProbabilityVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamStftVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamTaskVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamWaveformVo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FrameBeamCrackAlgorithmClient
{
    private static final Logger log = LoggerFactory.getLogger(FrameBeamCrackAlgorithmClient.class);

    @Resource
    private FrameBeamCrackProperties properties;

    public FrameBeamIdentifyResultVo identify(String taskId, String objectName, FrameBeamIdentifyRequest request)
    {
        String url = properties.getAlgorithm().getPredictUrl();
        if (StringUtils.isEmpty(url))
        {
            throw new ServiceException("缺少飞机框梁裂纹识别 Python 服务地址配置");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("taskId", taskId);
        body.put("objectInfo", request == null ? null : request.getObjectInfo());
        body.put("params", request == null ? null : request.getParams());
        if (request != null && request.getParams() != null)
        {
            body.put("filePath", firstText(
                    request.getParams().getBatchPath(),
                    firstText(request.getParams().getDataDir(), request.getParams().getFilePath())));
        }

        String responseBody = postJson(url, JSON.toJSONString(body));
        JSONObject response;
        try
        {
            response = JSON.parseObject(responseBody);
        }
        catch (Exception e)
        {
            throw new ServiceException("Python服务返回格式异常，无法解析JSON");
        }
        if (response == null)
        {
            throw new ServiceException("Python服务返回为空");
        }
        Boolean success = response.getBoolean("success");
        if (Boolean.FALSE.equals(success))
        {
            String message = response.getString("message");
            throw new ServiceException(StringUtils.isEmpty(message) ? "Python算法执行失败" : message);
        }

        FrameBeamIdentifyResultVo result;
        try
        {
            result = response.toJavaObject(FrameBeamIdentifyResultVo.class);
        }
        catch (Exception e)
        {
            throw new ServiceException("Python服务返回数据结构不符合预期");
        }
        return normalizeResult(taskId, objectName, result);
    }

    private String postJson(String url, String body)
    {
        try
        {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getAlgorithm().getConnectTimeout()))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(properties.getAlgorithm().getReadTimeout()))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Python服务返回HTTP " + response.statusCode() + "：" + safeBody(response.body()));
            }
            return response.body();
        }
        catch (HttpTimeoutException e)
        {
            log.error("调用飞机框梁裂纹识别 Python 服务超时，url={}", url, e);
            throw new ServiceException("Python算法服务请求超时，请确认服务已启动且算法未卡住");
        }
        catch (ConnectException e)
        {
            log.error("无法连接飞机框梁裂纹识别 Python 服务，url={}", url, e);
            throw new ServiceException("无法连接Python算法服务，请确认9741端口服务已启动");
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("调用Python算法服务被中断");
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("调用飞机框梁裂纹识别 Python 服务失败，url={}", url, e);
            throw new ServiceException("调用Python算法服务失败：" + (e.getMessage() == null ? "未知错误" : e.getMessage()));
        }
    }

    private FrameBeamIdentifyResultVo normalizeResult(String taskId, String objectName, FrameBeamIdentifyResultVo result)
    {
        if (result == null)
        {
            throw new ServiceException("Python服务未返回识别结果");
        }
        if (result.getTask() == null)
        {
            result.setTask(new FrameBeamTaskVo());
        }
        result.getTask().setTaskId(firstText(result.getTask().getTaskId(), taskId));
        result.getTask().setObjectName(firstText(result.getTask().getObjectName(), objectName));
        result.getTask().setStatus(firstText(result.getTask().getStatus(), "SUCCESS"));
        result.getTask().setStatusType(firstText(result.getTask().getStatusType(), "success"));
        result.getTask().setResult(firstText(result.getTask().getResult(), ""));
        if (result.getProbabilities() == null)
        {
            result.setProbabilities(new FrameBeamProbabilityVo());
        }
        if (result.getMetrics() == null)
        {
            result.setMetrics(new FrameBeamMetricsVo());
        }
        if (result.getWaveform() == null)
        {
            result.setWaveform(new FrameBeamWaveformVo());
        }
        if (result.getWaveform().getXAxis() == null)
        {
            result.getWaveform().setXAxis(new ArrayList<>());
        }
        if (result.getWaveform().getRaw() == null)
        {
            result.getWaveform().setRaw(new ArrayList<>());
        }
        if (result.getWaveform().getDenoised() == null)
        {
            result.getWaveform().setDenoised(new ArrayList<>());
        }
        if (result.getStft() == null)
        {
            result.setStft(new FrameBeamStftVo());
        }
        if (result.getStft().getXAxis() == null)
        {
            result.getStft().setXAxis(new ArrayList<>());
        }
        if (result.getStft().getYAxis() == null)
        {
            result.getStft().setYAxis(new ArrayList<>());
        }
        if (result.getStft().getData() == null)
        {
            result.getStft().setData(new ArrayList<>());
        }
        if (result.getHistoryRows() == null)
        {
            result.setHistoryRows(new ArrayList<>());
        }
        return result;
    }

    private String firstText(String first, String second)
    {
        return StringUtils.isNotEmpty(StringUtils.trim(first)) ? StringUtils.trim(first) : StringUtils.trim(second);
    }

    private String safeBody(String body)
    {
        if (body == null)
        {
            return "";
        }
        return body.length() > 300 ? body.substring(0, 300) : body;
    }
}

package com.ruoyi.project1.service.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Internal client for the project1 schema matching FastAPI service.
 */
@Component
public class AlgorithmMatchClient
{
    private static final Logger log = LoggerFactory.getLogger(AlgorithmMatchClient.class);

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${project1.algorithm.base-url:http://127.0.0.1:9204}")
    private String baseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> run(Map<String, Object> requestPayload)
    {
        return post("/api/v1/match/run", requestPayload);
    }

    private Map<String, Object> post(String endpoint, Map<String, Object> payload)
    {
        if (payload == null || payload.isEmpty())
        {
            throw new ServiceException("算法请求体不能为空，无法调用模式匹配服务");
        }

        String url = buildUrl(endpoint);
        String requestId = asString(payload.get("requestId"));
        try
        {
            String body = objectMapper.writeValueAsString(payload);
            int bodyLength = body.getBytes(StandardCharsets.UTF_8).length;
            log.info("Calling algorithm service: url={}, requestId={}, bodyLength={}, bodyPreview={}",
                    url, requestId, bodyLength, preview(body));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofSeconds(180))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            return send(request, requestId, bodyLength);
        }
        catch (IOException e)
        {
            throw new ServiceException("算法请求序列化失败: " + e.getMessage());
        }
    }

    private Map<String, Object> send(HttpRequest request, String requestId, int bodyLength)
    {
        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Algorithm service response: requestId={}, statusCode={}, version={}",
                    requestId, response.statusCode(), response.version());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw buildHttpError(response.statusCode(), response.body(), requestId, bodyLength);
            }
            if (isBlank(response.body()))
            {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(response.body(), MAP_TYPE);
        }
        catch (IOException e)
        {
            throw new ServiceException("算法服务调用失败: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("算法服务调用被中断");
        }
    }

    private ServiceException buildHttpError(int statusCode, String responseBody, String requestId, int bodyLength)
    {
        String detail = "requestId=" + defaultString(requestId, "-")
                + ", bodyLength=" + bodyLength
                + ", response=" + defaultString(responseBody, "");
        if (statusCode == 422)
        {
            return new ServiceException("算法服务调用失败，HTTP 422，FastAPI 未识别到合法请求体或请求体字段不符合接口要求；" + detail);
        }
        return new ServiceException("算法服务调用失败，HTTP " + statusCode + "；" + detail);
    }

    private String buildUrl(String endpoint)
    {
        if (isBlank(baseUrl))
        {
            throw new ServiceException("算法服务 base-url 不能为空");
        }
        String normalizedBase = baseUrl.trim();
        while (normalizedBase.endsWith("/"))
        {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        String normalizedEndpoint = isBlank(endpoint) ? "/" : endpoint.trim();
        if (!normalizedEndpoint.startsWith("/"))
        {
            normalizedEndpoint = "/" + normalizedEndpoint;
        }
        return normalizedBase + normalizedEndpoint;
    }

    private String preview(String body)
    {
        if (body == null)
        {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 500) + "...";
    }

    private String asString(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private String defaultString(String value, String defaultValue)
    {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}

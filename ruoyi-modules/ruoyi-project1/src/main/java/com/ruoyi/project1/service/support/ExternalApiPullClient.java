package com.ruoyi.project1.service.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project1.domain.ApiPullDatasource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Internal HTTP client for API Pull external systems.
 */
@Component
public class ExternalApiPullClient
{
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> health(ApiPullDatasource detail)
    {
        return get(detail, endpointOrDefault(detail.getHealthEndpoint(), "/api/health"));
    }

    public Map<String, Object> schema(ApiPullDatasource detail)
    {
        return get(detail, endpointOrDefault(detail.getSchemaEndpoint(), "/api/schema"));
    }

    public Map<String, Object> pull(ApiPullDatasource detail, Map<String, Object> requestPayload)
    {
        return post(detail, endpointOrDefault(detail.getPullEndpoint(), "/api/pull"), requestPayload);
    }

    private Map<String, Object> get(ApiPullDatasource detail, String endpoint)
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(detail.getBaseUrl(), endpoint)))
                .timeout(Duration.ofSeconds(30))
                .GET();
        applyHeaders(builder, detail);
        return send(builder.build());
    }

    private Map<String, Object> post(ApiPullDatasource detail, String endpoint, Map<String, Object> payload)
    {
        try
        {
            String body = objectMapper.writeValueAsString(payload == null ? Collections.emptyMap() : payload);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(buildUrl(detail.getBaseUrl(), endpoint)))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            applyHeaders(builder, detail);
            return send(builder.build());
        }
        catch (IOException e)
        {
            throw new ServiceException("API Pull 请求序列化失败: " + e.getMessage());
        }
    }

    private Map<String, Object> send(HttpRequest request)
    {
        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("API Pull 调用失败，HTTP " + response.statusCode() + ": " + response.body());
            }
            if (isBlank(response.body()))
            {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(response.body(), MAP_TYPE);
        }
        catch (IOException e)
        {
            throw new ServiceException("API Pull 调用失败: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("API Pull 调用被中断");
        }
    }

    private void applyHeaders(HttpRequest.Builder builder, ApiPullDatasource detail)
    {
        Map<String, Object> configuredHeaders = parseHeaders(detail.getHeadersConfig());
        for (Map.Entry<String, Object> entry : configuredHeaders.entrySet())
        {
            if (!isBlank(entry.getKey()) && entry.getValue() != null)
            {
                builder.header(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        if (!isBlank(detail.getApiKeyEnc()) && !isBlank(detail.getAuthType()))
        {
            String authType = detail.getAuthType().trim().toLowerCase();
            if ("bearer".equals(authType) || "bearer token".equals(authType))
            {
                builder.header("Authorization", "Bearer " + detail.getApiKeyEnc());
            }
            else if ("api_key".equals(authType) || "apikey".equals(authType) || "api key".equals(authType))
            {
                builder.header("X-API-Key", detail.getApiKeyEnc());
            }
        }
    }

    private Map<String, Object> parseHeaders(String headersConfig)
    {
        if (isBlank(headersConfig))
        {
            return Collections.emptyMap();
        }
        try
        {
            return objectMapper.readValue(headersConfig, MAP_TYPE);
        }
        catch (IOException e)
        {
            throw new ServiceException("API Pull headers_config 不是合法 JSON: " + e.getMessage());
        }
    }

    private String buildUrl(String baseUrl, String endpoint)
    {
        if (isBlank(baseUrl))
        {
            throw new ServiceException("API Pull baseUrl 不能为空");
        }
        String normalizedBase = baseUrl.trim();
        while (normalizedBase.endsWith("/"))
        {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }

        String normalizedEndpoint = endpointOrDefault(endpoint, "/");
        if (!normalizedEndpoint.startsWith("/"))
        {
            normalizedEndpoint = "/" + normalizedEndpoint;
        }
        return normalizedBase + normalizedEndpoint;
    }

    private String endpointOrDefault(String endpoint, String defaultEndpoint)
    {
        return isBlank(endpoint) ? defaultEndpoint : endpoint.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}


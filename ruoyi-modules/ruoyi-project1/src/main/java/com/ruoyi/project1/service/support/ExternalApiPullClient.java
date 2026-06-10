package com.ruoyi.project1.service.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project1.domain.ApiPullDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Internal HTTP client for API Pull external systems.
 */
@Component
public class ExternalApiPullClient
{
    private static final Logger log = LoggerFactory.getLogger(ExternalApiPullClient.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
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
                .version(HttpClient.Version.HTTP_1_1)
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
            String url = buildUrl(detail.getBaseUrl(), endpoint);
            log.info("API Pull POST {} bodyLength={} bodyPreview={}", url,
                    body.getBytes(StandardCharsets.UTF_8).length, preview(body));

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(180))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            applyHeaders(builder, detail);
            return send(builder.build());
        }
        catch (IOException e)
        {
            throw new ServiceException("API Pull request serialization failed: " + exceptionDetail(e));
        }
    }

    private Map<String, Object> send(HttpRequest request)
    {
        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("API Pull {} {} -> HTTP {}", request.method(), request.uri(), response.statusCode());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException(normalizeError(response.statusCode(), response.body()));
            }
            if (isBlank(response.body()))
            {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(response.body(), MAP_TYPE);
        }
        catch (IOException e)
        {
            throw new ServiceException("API Pull call failed: " + exceptionDetail(e));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("API Pull call was interrupted");
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
            throw new ServiceException("API Pull headers_config is not valid JSON: " + exceptionDetail(e));
        }
    }

    private String buildUrl(String baseUrl, String endpoint)
    {
        if (isBlank(baseUrl))
        {
            throw new ServiceException("API Pull baseUrl is required");
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

    private String normalizeError(int statusCode, String responseBody)
    {
        String detail = extractDetail(responseBody);
        if (!isBlank(detail) && detail.startsWith("Unknown source_table"))
        {
            return "API Pull source table does not exist: " + detail
                    + ". Regenerate the default result set or check the final access rules.";
        }
        if (!isBlank(detail) && detail.startsWith("Unknown column"))
        {
            return "API Pull source column does not exist: " + detail
                    + ". Regenerate the default result set or check field mappings.";
        }
        return "API Pull call failed, HTTP " + statusCode + ": " + (isBlank(detail) ? responseBody : detail);
    }

    private String extractDetail(String responseBody)
    {
        if (isBlank(responseBody))
        {
            return "";
        }
        try
        {
            Map<String, Object> payload = objectMapper.readValue(responseBody, MAP_TYPE);
            Object detail = payload.get("detail");
            return detail == null ? responseBody : String.valueOf(detail);
        }
        catch (IOException e)
        {
            return responseBody;
        }
    }

    private String preview(String body)
    {
        if (body == null)
        {
            return "";
        }
        String normalized = body.replaceAll("\\s+", " ");
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
    }

    private String exceptionDetail(Exception e)
    {
        if (e == null)
        {
            return "unknown";
        }
        return isBlank(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}

package com.ruoyi.project3.util;

import com.ruoyi.common.core.exception.ServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class ApiImportFileFetcher {
    private static final long MAX_BYTES = 100L * 1024L * 1024L;

    private ApiImportFileFetcher() {
    }

    public static MultipartFile fetch(Map<String, Object> req, String defaultFileName) {
        if (req == null) {
            throw new ServiceException("API导入参数不能为空");
        }
        String apiUrl = text(req.get("apiUrl"));
        if (apiUrl == null) {
            throw new ServiceException("API地址不能为空");
        }

        URI uri;
        try {
            uri = URI.create(apiUrl);
        } catch (Exception e) {
            throw new ServiceException("API地址不合法");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new ServiceException("API地址仅支持HTTP/HTTPS");
        }

        String method = text(req.get("method"));
        method = method == null ? "GET" : method.toUpperCase();
        if (!"GET".equals(method) && !"POST".equals(method)) {
            throw new ServiceException("API请求方法仅支持GET/POST");
        }

        String fileName = apiFileName(text(req.get("fileName")), defaultFileName);
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(120));
            addHeaders(builder, req.get("headers"));
            String body = text(req.get("body"));
            if ("POST".equals(method)) {
                builder.POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.GET();
            }

            HttpResponse<InputStream> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build()
                    .send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException("API请求失败，HTTP状态码：" + response.statusCode());
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            copyWithLimit(response.body(), output);
            return new BytesMultipartFile(fileName, output.toByteArray());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("API导入失败：" + e.getMessage());
        }
    }

    private static String apiFileName(String fileName, String defaultFileName) {
        String clean = text(fileName);
        if (clean == null) {
            clean = defaultFileName;
        }
        clean = clean == null ? "api_import.xlsx" : clean.trim();
        clean = clean.replace("\\", "/");
        int slash = clean.lastIndexOf('/');
        if (slash >= 0) {
            clean = clean.substring(slash + 1);
        }
        if (clean.isEmpty() || clean.contains("..")) {
            throw new ServiceException("API导入文件名不合法");
        }
        String lower = clean.toLowerCase();
        if (!lower.endsWith(".xlsx") && !lower.endsWith(".xls")) {
            clean = clean + ".xlsx";
        }
        return clean;
    }

    private static void addHeaders(HttpRequest.Builder builder, Object headers) {
        if (!(headers instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = text(entry.getKey());
            String value = text(entry.getValue());
            if (key == null || value == null) {
                continue;
            }
            String lower = key.toLowerCase();
            if ("host".equals(lower) || "content-length".equals(lower) || "connection".equals(lower)) {
                continue;
            }
            builder.header(key, value);
        }
    }

    private static void copyWithLimit(InputStream input, ByteArrayOutputStream output) throws Exception {
        byte[] buffer = new byte[8192];
        long total = 0L;
        int len;
        try (InputStream source = input) {
            while ((len = source.read(buffer)) >= 0) {
                total += len;
                if (total > MAX_BYTES) {
                    throw new ServiceException("API响应内容超过100MB限制");
                }
                output.write(buffer, 0, len);
            }
        }
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static final class BytesMultipartFile implements MultipartFile {
        private final String fileName;
        private final byte[] bytes;

        private BytesMultipartFile(String fileName, byte[] bytes) {
            this.fileName = fileName;
            this.bytes = bytes == null ? new byte[0] : bytes;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }
}

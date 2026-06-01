package com.ruoyi.project3.controller;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;
import com.ruoyi.project3.service.FaultIdenFileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/quality/fault-iden")
public class FaultIdenFileController
{
    private static final String USAGE_PREDICT = "FAULT_PREDICT";
    private static final String USAGE_WARNING = "PROCESS_ANOMALY";

    @Resource
    private FaultIdenFileService faultIdenFileService;

    @GetMapping("/source-file/{sampleId}")
    public void source(@PathVariable("sampleId") Long sampleId, HttpServletResponse response) throws Exception
    {
        FaultIdenSampleFile sample = faultIdenFileService.sample(sampleId);
        Path file = faultIdenFileService.sourceFile(sampleId);
        stream(response, file, sample.getFileName(), contentType(sample.getFileName()));
    }

    @GetMapping("/predict-source-file/{sampleId}")
    public void predictSource(@PathVariable("sampleId") Long sampleId, HttpServletResponse response) throws Exception
    {
        FaultIdenSampleFile sample = faultIdenFileService.sample(sampleId);
        if (!USAGE_PREDICT.equals(sample.getDataUsage()))
        {
            throw new ServiceException("预测数据文件不存在或用途不匹配");
        }
        Path file = faultIdenFileService.sourceFile(sampleId);
        stream(response, file, sample.getFileName(), contentType(sample.getFileName()));
    }

    @GetMapping("/warning-source-file/{sampleId}")
    public void warningSource(@PathVariable("sampleId") Long sampleId, HttpServletResponse response) throws Exception
    {
        FaultIdenSampleFile sample = faultIdenFileService.sample(sampleId);
        if (!USAGE_WARNING.equals(sample.getDataUsage()))
        {
            throw new ServiceException("异常检测数据文件不存在或用途不匹配");
        }
        Path file = faultIdenFileService.sourceFile(sampleId);
        stream(response, file, sample.getFileName(), contentType(sample.getFileName()));
    }

    @GetMapping("/export-file/{fileName}")
    public void export(@PathVariable("fileName") String fileName, HttpServletResponse response) throws Exception
    {
        Path file = faultIdenFileService.exportFile(fileName);
        stream(response, file, fileName, "application/zip");
    }

    private void stream(HttpServletResponse response, Path file, String name, String contentType) throws Exception
    {
        response.setContentType(contentType);
        response.setHeader("Content-Length", String.valueOf(Files.size(file)));
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20")
        );
        try (InputStream in = Files.newInputStream(file); OutputStream out = response.getOutputStream())
        {
            in.transferTo(out);
        }
    }

    private String contentType(String fileName)
    {
        String name = fileName == null ? "" : fileName.toLowerCase();
        return name.endsWith(".csv") ? "text/csv;charset=UTF-8" : "text/plain;charset=UTF-8";
    }
}

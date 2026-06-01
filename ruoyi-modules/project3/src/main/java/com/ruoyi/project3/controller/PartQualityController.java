package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.project3.domain.partquality.PartQualityImportError;
import com.ruoyi.project3.domain.partquality.PartQualityImportResult;
import com.ruoyi.project3.service.PartQualityService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/quality/part-quality")
public class PartQualityController
{
    private static final Logger log = LoggerFactory.getLogger(PartQualityController.class);

    @Resource
    private PartQualityService partQualityService;

    @PostMapping("/import")
    public AjaxResult importPartQuality(@RequestParam("file") MultipartFile file)
    {
        try {
            PartQualityImportResult result = partQualityService.importPartQuality(file);
            if (result.isSuccess()) {
                return AjaxResult.success("导入成功", result);
            }
            return AjaxResult.error("导入失败", result);
        } catch (Exception e) {
            log.error("零件质量信息导入失败，文件名={}", file == null ? null : file.getOriginalFilename(), e);
            PartQualityImportResult result = new PartQualityImportResult();
            result.setSuccess(false);
            result.getErrors().add(new PartQualityImportError(null, null, "导入服务", "导入服务异常，请查看后端日志"));
            return AjaxResult.error("导入失败", result);
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws Exception
    {
        String fileName = "零件质量信息导入模板.xlsx";
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, fileName);
        partQualityService.writeTemplate(response.getOutputStream());
    }
}

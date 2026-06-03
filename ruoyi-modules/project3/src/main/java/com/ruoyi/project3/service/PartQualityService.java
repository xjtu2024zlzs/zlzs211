package com.ruoyi.project3.service;

import com.ruoyi.project3.domain.partquality.PartQualityImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

public interface PartQualityService
{
    PartQualityImportResult importPartQuality(MultipartFile file);

    void writeTemplate(OutputStream outputStream) throws IOException;
}

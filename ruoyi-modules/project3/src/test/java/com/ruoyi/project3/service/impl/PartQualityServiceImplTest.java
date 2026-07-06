package com.ruoyi.project3.service.impl;

import com.ruoyi.project3.domain.partquality.PartQualityImportResult;
import com.ruoyi.project3.mapper.PartQualityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartQualityServiceImplTest
{
    @Mock
    private PartQualityMapper partQualityMapper;

    @InjectMocks
    private PartQualityServiceImpl service;

    @Test
    void importRejectsMissingFile()
    {
        PartQualityImportResult result = service.importPartQuality(null);

        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void importRejectsUnsupportedExtension()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "quality.xls",
                "application/vnd.ms-excel",
                new byte[]{1}
        );

        PartQualityImportResult result = service.importPartQuality(file);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().get(0).getMessage().contains("xlsx"));
    }

    @Test
    void importRejectsEmptyWorkbook()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "quality.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        PartQualityImportResult result = service.importPartQuality(file);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().get(0).getMessage().contains("不能为空"));
    }

    @Test
    void writeTemplateProducesWorkbook() throws Exception
    {
        when(partQualityMapper.selectManufacturingQualityRows()).thenReturn(Collections.emptyList());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        service.writeTemplate(output);

        assertTrue(output.size() > 0);
    }
}

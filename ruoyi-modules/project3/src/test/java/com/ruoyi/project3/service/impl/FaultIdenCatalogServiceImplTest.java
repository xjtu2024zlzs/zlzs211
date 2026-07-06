package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.mapper.FaultIdenSampleMapper;
import com.ruoyi.project3.mapper.MonitorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaultIdenCatalogServiceImplTest
{
    @TempDir
    Path tempDir;

    @Mock
    private FaultIdenFileProps props;

    @Mock
    private FaultIdenSampleMapper sampleMapper;

    @Mock
    private MonitorMapper monitorMapper;

    @InjectMocks
    private FaultIdenCatalogServiceImpl service;

    @Test
    void numericChunkRejectsEmptyAndInvalidExtension()
    {
        MockMultipartFile empty = new MockMultipartFile("chunk", "0.part", "application/octet-stream", new byte[0]);
        MockMultipartFile content = new MockMultipartFile("chunk", "0.part", "application/octet-stream", new byte[]{1});

        assertThrows(ServiceException.class,
                () -> service.uploadNumericChunk("UPLOAD1", 0, 1, "data.csv", empty));
        assertThrows(ServiceException.class,
                () -> service.uploadNumericChunk("UPLOAD1", 0, 1, "data.exe", content));
    }

    @Test
    void duplicateNumericChunkUploadIsIdempotent()
    {
        when(props.getSourceRoot()).thenReturn(tempDir.toString());
        MockMultipartFile first = new MockMultipartFile("chunk", "0.part", "application/octet-stream", "first".getBytes());
        MockMultipartFile replacement = new MockMultipartFile("chunk", "0.part", "application/octet-stream", "second".getBytes());

        Map<String, Object> initial = service.uploadNumericChunk("UPLOAD1", 0, 1, "data.csv", first);
        Map<String, Object> repeated = service.uploadNumericChunk("UPLOAD1", 0, 1, "data.csv", replacement);

        assertEquals(1L, ((Number) initial.get("uploadedCount")).longValue());
        assertEquals(1L, ((Number) repeated.get("uploadedCount")).longValue());
    }

    @Test
    void strictNumericChunkValidationRejectsMissingHashMimeAndBounds()
    {
        MockMultipartFile content = new MockMultipartFile("chunk", "0.part", "application/octet-stream", "first".getBytes());
        MockMultipartFile fakeMime = new MockMultipartFile("chunk", "0.part", "application/pdf", "first".getBytes());

        assertThrows(ServiceException.class,
                () -> service.uploadNumericChunk("UPLOAD1", 0, 1, "data.csv", "", content));
        assertThrows(ServiceException.class,
                () -> service.uploadNumericChunk("UPLOAD1", 0, 1, "data.csv", "abcdef", fakeMime));
        assertThrows(ServiceException.class,
                () -> service.uploadNumericChunk("UPLOAD1", 1, 1, "data.csv", "abcdef", content));
    }

    @Test
    void strictNumericChunkUploadAcceptsHashAndReturnsMetadata()
    {
        when(props.getSourceRoot()).thenReturn(tempDir.toString());
        MockMultipartFile content = new MockMultipartFile("chunk", "0.part", "application/octet-stream", "first".getBytes());

        Map<String, Object> result = service.uploadNumericChunk("UPLOAD2", 0, 2, "data.csv", "abcdef123456", content);

        assertEquals("UPLOAD2", result.get("uploadId"));
        assertEquals("abcdef123456", result.get("fileHash"));
        assertEquals(0, result.get("chunkIndex"));
    }
}

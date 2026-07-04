package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.client.FrameBeamCrackAlgorithmClient;
import com.ruoyi.project3.config.FrameBeamCrackProperties;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FrameBeamCrackIdentifyServiceImplTest
{
    @TempDir
    Path tempDir;

    private FrameBeamCrackIdentifyServiceImpl service;

    @BeforeEach
    void setUp()
    {
        service = new FrameBeamCrackIdentifyServiceImpl(mock(PlatformTransactionManager.class));
        ReflectionTestUtils.setField(service, "sourceRoot", tempDir.toString());
        ReflectionTestUtils.setField(service, "algTaskMapper", mock(AlgTaskMapper.class));
        ReflectionTestUtils.setField(service, "algorithmClient", mock(FrameBeamCrackAlgorithmClient.class));
        ReflectionTestUtils.setField(service, "properties", mock(FrameBeamCrackProperties.class));
    }

    @Test
    void chunkUploadRejectsEmptyAndInvalidIndex()
    {
        MockMultipartFile empty = new MockMultipartFile("chunk", "0.part", "application/octet-stream", new byte[0]);
        MockMultipartFile content = new MockMultipartFile("chunk", "0.part", "application/octet-stream", new byte[]{1});

        assertThrows(ServiceException.class,
                () -> service.uploadDataChunk("UPLOAD1", 0, 1, "data.csv", empty));
        assertThrows(ServiceException.class,
                () -> service.uploadDataChunk("UPLOAD1", 2, 2, "data.csv", content));
    }

    @Test
    void outOfOrderAndDuplicateChunksRemainMergeable()
    {
        MockMultipartFile second = new MockMultipartFile("chunk", "1.part", "application/octet-stream", "B".getBytes());
        MockMultipartFile first = new MockMultipartFile("chunk", "0.part", "application/octet-stream", "A".getBytes());
        MockMultipartFile replacement = new MockMultipartFile("chunk", "0.part", "application/octet-stream", "C".getBytes());

        service.uploadDataChunk("UPLOAD1", 1, 2, "data.csv", second);
        service.uploadDataChunk("UPLOAD1", 0, 2, "data.csv", first);
        service.uploadDataChunk("UPLOAD1", 0, 2, "data.csv", replacement);
        Map<String, Object> result = service.mergeDataChunks("BATCH1", "UPLOAD1", "data.csv", 1, "data.csv");

        assertEquals("SUCCESS", result.get("uploadStatus"));
        assertEquals(2L, ((Number) result.get("fileSize")).longValue());
    }

    @Test
    void mergeRejectsMissingChunk()
    {
        MockMultipartFile second = new MockMultipartFile("chunk", "1.part", "application/octet-stream", "B".getBytes());
        service.uploadDataChunk("UPLOAD2", 1, 2, "data.csv", second);

        assertThrows(ServiceException.class,
                () -> service.mergeDataChunks("BATCH2", "UPLOAD2", "data.csv", 1, "data.csv"));
    }
}

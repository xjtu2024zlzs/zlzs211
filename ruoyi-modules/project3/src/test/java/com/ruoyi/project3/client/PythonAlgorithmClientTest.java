package com.ruoyi.project3.client;

import com.ruoyi.project3.config.PythonAlgorithmProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PythonAlgorithmClientTest
{
    @Test
    void identifyUsesConfiguredMockWithoutNetworkCall()
    {
        PythonAlgorithmProperties properties = new PythonAlgorithmProperties();
        properties.setMock(true);
        PythonAlgorithmClient client = new PythonAlgorithmClient();
        ReflectionTestUtils.setField(client, "pythonAlgorithmProperties", properties);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("taskId", "TASK-1");

        Map<String, Object> response = client.identify_process(request);

        assertEquals(Boolean.TRUE, response.get("success"));
        assertEquals("TASK-1", response.get("taskId"));
        assertTrue(request.containsKey("authCode"));
    }
}

package com.ruoyi.project1.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AccessDateJsonFormatTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void accessPlanTableResultAndBatchKeepTimeInJson() throws Exception
    {
        Date clickedAt = parseBeijingTime("2026-07-05 17:58:25");

        AccessPlan plan = new AccessPlan();
        plan.setLastExecuteTime(clickedAt);
        AccessTableResult tableResult = new AccessTableResult();
        tableResult.setLastExecuteTime(clickedAt);
        AccessBatch batch = new AccessBatch();
        batch.setStartedAt(clickedAt);
        batch.setFinishedAt(clickedAt);

        assertTrue(objectMapper.writeValueAsString(plan).contains("\"lastExecuteTime\":\"2026-07-05 17:58:25\""));
        assertTrue(objectMapper.writeValueAsString(tableResult).contains("\"lastExecuteTime\":\"2026-07-05 17:58:25\""));
        String batchJson = objectMapper.writeValueAsString(batch);
        assertTrue(batchJson.contains("\"startedAt\":\"2026-07-05 17:58:25\""));
        assertTrue(batchJson.contains("\"finishedAt\":\"2026-07-05 17:58:25\""));
    }

    private static Date parseBeijingTime(String value) throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return format.parse(value);
    }
}

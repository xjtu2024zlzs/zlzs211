package com.ruoyi.project1.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.project1.domain.AccessBatch;
import com.ruoyi.project1.domain.AccessScopeTable;

class Project1PresetScenarioServiceTimeTest
{
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    void truncatesAccessExecutionTimeToHour() throws Exception
    {
        Date source = format.parse("2026-07-05 16:50:29");

        assertEquals("2026-07-05 16:00:00", format.format(Project1PresetScenarioService.truncateToHour(source)));
    }

    @Test
    void manualExecutionKeepsClickedMinuteAndSecond() throws Exception
    {
        Date source = format.parse("2026-07-05 17:58:25");

        assertEquals("2026-07-05 17:58:25", format.format(Project1PresetScenarioService.manualBatchFinishedAt(source)));
    }

    @Test
    void scheduleBaselineUsesFiveConsecutiveHourlyBatches() throws Exception
    {
        Date source = format.parse("2026-07-05 16:50:29");

        assertEquals("2026-07-05 12:00:00", format.format(Project1PresetScenarioService.scheduledBatchFinishedAt(source, 1)));
        assertEquals("2026-07-05 13:00:00", format.format(Project1PresetScenarioService.scheduledBatchFinishedAt(source, 2)));
        assertEquals("2026-07-05 14:00:00", format.format(Project1PresetScenarioService.scheduledBatchFinishedAt(source, 3)));
        assertEquals("2026-07-05 15:00:00", format.format(Project1PresetScenarioService.scheduledBatchFinishedAt(source, 4)));
        assertEquals("2026-07-05 16:00:00", format.format(Project1PresetScenarioService.scheduledBatchFinishedAt(source, 5)));
    }

    @Test
    void detectsScheduleWindowNeedsRollingWhenLatestScheduleIsPreviousHour() throws Exception
    {
        Date current = format.parse("2026-07-05 18:36:00");
        List<AccessBatch> batches = Arrays.asList(
            scheduleBatch("2026-07-05 13:00:00"),
            scheduleBatch("2026-07-05 14:00:00"),
            scheduleBatch("2026-07-05 15:00:00"),
            scheduleBatch("2026-07-05 16:00:00"),
            scheduleBatch("2026-07-05 17:00:00"));

        assertEquals(true, Project1PresetScenarioService.shouldRollScheduleBatches(batches, current));
    }

    @Test
    void keepsScheduleWindowWhenFiveSchedulesEndAtCurrentHour() throws Exception
    {
        Date current = format.parse("2026-07-05 18:36:00");
        List<AccessBatch> batches = Arrays.asList(
            scheduleBatch("2026-07-05 14:00:00"),
            scheduleBatch("2026-07-05 15:00:00"),
            scheduleBatch("2026-07-05 16:00:00"),
            scheduleBatch("2026-07-05 17:00:00"),
            scheduleBatch("2026-07-05 18:00:00"));

        assertEquals(false, Project1PresetScenarioService.shouldRollScheduleBatches(batches, current));
    }

    @Test
    void latestBatchPrefersManualClickedMinuteWhenManualIsAfterCurrentSchedule() throws Exception
    {
        AccessBatch latest = Project1PresetScenarioService.latestFinishedBatch(Arrays.asList(
            scheduleBatch("2026-07-05 18:00:00"),
            manualBatch("2026-07-05 18:36:25")));

        assertEquals("2026-07-05 18:36:25", format.format(latest.getFinishedAt()));
        assertEquals("manual", latest.getTriggerType());
    }

    @Test
    void latestBatchUsesCurrentScheduleWhenManualWasPreviousHour() throws Exception
    {
        AccessBatch latest = Project1PresetScenarioService.latestFinishedBatch(Arrays.asList(
            manualBatch("2026-07-05 17:58:25"),
            scheduleBatch("2026-07-05 18:00:00")));

        assertEquals("2026-07-05 18:00:00", format.format(latest.getFinishedAt()));
        assertEquals("schedule", latest.getTriggerType());
    }

    @Test
    void presetReviewTimesUseFixedBaseAndDeterministicManualOffsets()
    {
        assertEquals("2026-07-03 14:13:35",
            format.format(Project1PresetScenarioService.presetReviewedAt("auto_approved", 1L)));
        assertEquals("2026-07-03 14:13:46",
            format.format(Project1PresetScenarioService.presetReviewedAt("approved", 1L)));
        assertEquals("2026-07-03 14:13:55",
            format.format(Project1PresetScenarioService.presetReviewedAt("rejected", 10L)));
        assertEquals("2026-07-03 14:13:45",
            format.format(Project1PresetScenarioService.presetReviewedAt("approved", 11L)));
        assertNull(Project1PresetScenarioService.presetReviewedAt("pending", 1L));
    }

    @Test
    void accessScopeIdentityUsesPlanSourceTableAndTargetTable()
    {
        AccessScopeTable row = new AccessScopeTable();
        row.setAccessPlanId(1L);
        row.setSourceTable("actuator_model");
        row.setTargetTable("component_type");

        assertEquals(true, Project1PresetScenarioService.isSameAccessScope(row, 1L, "actuator_model", "component_type"));
        assertEquals(false, Project1PresetScenarioService.isSameAccessScope(row, 2L, "actuator_model", "component_type"));
        assertEquals(false, Project1PresetScenarioService.isSameAccessScope(row, 1L, "actuator_model", "part_definition"));
    }

    private AccessBatch scheduleBatch(String finishedAt) throws Exception
    {
        return batch("schedule", finishedAt);
    }

    private AccessBatch manualBatch(String finishedAt) throws Exception
    {
        return batch("manual", finishedAt);
    }

    private AccessBatch batch(String triggerType, String finishedAt) throws Exception
    {
        AccessBatch batch = new AccessBatch();
        batch.setTriggerType(triggerType);
        batch.setFinishedAt(format.parse(finishedAt));
        return batch;
    }
}

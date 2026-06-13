package com.ruoyi.project1.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PresetAccessDemoDataFactoryTest
{
    private static final List<String> FORBIDDEN_TERMS = Arrays.asList("演示", "预置", "模拟", "preset", "mock", "GroundTruth 预置");

    @Test
    void profilesUseBusinessNamesAndExpectedTotals()
    {
        PresetAccessDemoDataFactory factory = PresetAccessDemoDataFactory.createDefault();

        assertProfile(factory, "plm", "CF PLM API Pull 接入计划", 28640L, 86L, 4860L, 1020L, 7L);
        assertProfile(factory, "erp", "CF ERP API Pull 接入计划", 18320L, 54L, 2940L, 740L, 5L);
        assertProfile(factory, "mes", "CF MES API Pull 接入计划", 27480L, 112L, 4320L, 1220L, 12L);
        assertProfile(factory, "qms", "CF QMS API Pull 接入计划", 21460L, 73L, 3360L, 930L, 8L);
        assertProfile(factory, "mro", "CF MRO API Pull 接入计划", 12680L, 38L, 1960L, 540L, 4L);
    }

    @Test
    void batchesHaveFiveRunsAndLatestCountsMatchPlanSummary()
    {
        PresetAccessDemoDataFactory factory = PresetAccessDemoDataFactory.createDefault();

        for (String systemKey : Arrays.asList("plm", "erp", "mes", "qms", "mro"))
        {
            PresetAccessDemoDataFactory.AccessProfile profile = factory.profileFor(systemKey);
            List<PresetAccessDemoDataFactory.BatchProfile> batches = factory.batchesFor(systemKey);

            assertEquals(5, batches.size());
            assertEquals(profile.totalSuccessCount(), batches.stream().mapToLong(PresetAccessDemoDataFactory.BatchProfile::successCount).sum());
            assertEquals(profile.totalFailedCount(), batches.stream().mapToLong(PresetAccessDemoDataFactory.BatchProfile::failedCount).sum());

            PresetAccessDemoDataFactory.BatchProfile latest = batches.get(batches.size() - 1);
            assertEquals(profile.lastInsertedCount(), latest.insertedCount());
            assertEquals(profile.lastUpdatedCount(), latest.updatedCount());
            assertEquals(profile.lastFailedCount(), latest.failedCount());
            assertNoForbiddenText(profile.batchPrefix());
        }
    }

    @Test
    void visibleMessagesContainNoDemoTerms()
    {
        PresetAccessDemoDataFactory factory = PresetAccessDemoDataFactory.createDefault();

        assertNoForbiddenText(factory.successMessage());
        assertNoForbiddenText(factory.partialMessage());
        for (String reason : factory.fieldErrorReasons())
        {
            assertNoForbiddenText(reason);
        }
    }

    private static void assertProfile(PresetAccessDemoDataFactory factory, String systemKey, String planName,
            long totalSuccessCount, long totalFailedCount, long lastInsertedCount, long lastUpdatedCount,
            long lastFailedCount)
    {
        PresetAccessDemoDataFactory.AccessProfile profile = factory.profileFor(systemKey);

        assertEquals(planName, profile.planName());
        assertEquals("continuous", profile.accessType());
        assertEquals(1L, profile.cycleHours());
        assertEquals("enabled", profile.useStatus());
        assertEquals(totalSuccessCount, profile.totalSuccessCount());
        assertEquals(totalFailedCount, profile.totalFailedCount());
        assertEquals(lastInsertedCount, profile.lastInsertedCount());
        assertEquals(lastUpdatedCount, profile.lastUpdatedCount());
        assertEquals(lastFailedCount, profile.lastFailedCount());

        assertNoForbiddenText(profile.planName());
        assertNoForbiddenText(profile.batchPrefix());
    }

    private static void assertNoForbiddenText(String value)
    {
        String lowerValue = value == null ? "" : value.toLowerCase();
        for (String term : FORBIDDEN_TERMS)
        {
            assertFalse(lowerValue.contains(term.toLowerCase()), value);
        }
    }
}

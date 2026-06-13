package com.ruoyi.project1.service.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Stable access statistics used by project1 managed access plans.
 */
class PresetAccessDemoDataFactory
{
    private static final String SUCCESS_MESSAGE = "按最终字段规则接入目标表";
    private static final String PARTIAL_MESSAGE = "部分记录字段校验未通过，已完成有效数据接入";
    private static final List<String> FIELD_ERROR_REASONS = Collections.unmodifiableList(
        Arrays.asList("源字段为空", "字段格式校验未通过", "目标字段长度超限"));

    private final Map<String, AccessProfile> profiles;

    static PresetAccessDemoDataFactory createDefault()
    {
        Map<String, AccessProfile> profiles = new LinkedHashMap<>();
        put(profiles, new AccessProfile("PLM", "CF PLM API Pull 接入计划", "API-PLM-", "continuous", 1L,
            "enabled", 28640L, 86L, 4860L, 1020L, 7L));
        put(profiles, new AccessProfile("ERP", "CF ERP API Pull 接入计划", "API-ERP-", "continuous", 1L,
            "enabled", 18320L, 54L, 2940L, 740L, 5L));
        put(profiles, new AccessProfile("MES", "CF MES API Pull 接入计划", "API-MES-", "continuous", 1L,
            "enabled", 27480L, 112L, 4320L, 1220L, 12L));
        put(profiles, new AccessProfile("QMS", "CF QMS API Pull 接入计划", "API-QMS-", "continuous", 1L,
            "enabled", 21460L, 73L, 3360L, 930L, 8L));
        put(profiles, new AccessProfile("MRO", "CF MRO API Pull 接入计划", "API-MRO-", "continuous", 1L,
            "enabled", 12680L, 38L, 1960L, 540L, 4L));
        return new PresetAccessDemoDataFactory(profiles);
    }

    private PresetAccessDemoDataFactory(Map<String, AccessProfile> profiles)
    {
        this.profiles = Collections.unmodifiableMap(profiles);
    }

    AccessProfile profileFor(String systemKey)
    {
        AccessProfile profile = profiles.get(normalize(systemKey));
        if (profile == null)
        {
            throw new IllegalArgumentException("Unsupported system key: " + systemKey);
        }
        return profile;
    }

    List<BatchProfile> batchesFor(String systemKey)
    {
        AccessProfile profile = profileFor(systemKey);
        long latestSuccess = profile.lastInsertedCount() + profile.lastUpdatedCount();
        long previousSuccess = Math.max(0L, profile.totalSuccessCount() - latestSuccess);
        long previousFailed = Math.max(0L, profile.totalFailedCount() - profile.lastFailedCount());

        long[] successParts = distribute(previousSuccess, new long[] {17L, 19L, 21L, 23L});
        long[] failedParts = distribute(previousFailed, new long[] {13L, 11L, 17L, 19L});

        List<BatchProfile> batches = new ArrayList<>();
        int[] insertedRatios = new int[] {79, 75, 71, 66};
        for (int i = 0; i < successParts.length; i++)
        {
            long inserted = successParts[i] * insertedRatios[i] / 100L;
            long updated = successParts[i] - inserted;
            batches.add(new BatchProfile(i + 1, "schedule", successParts[i], failedParts[i], inserted, updated));
        }
        batches.add(new BatchProfile(5, "manual", latestSuccess, profile.lastFailedCount(),
            profile.lastInsertedCount(), profile.lastUpdatedCount()));
        return batches;
    }

    String successMessage()
    {
        return SUCCESS_MESSAGE;
    }

    String partialMessage()
    {
        return PARTIAL_MESSAGE;
    }

    List<String> fieldErrorReasons()
    {
        return FIELD_ERROR_REASONS;
    }

    List<Long> distributeByKeys(long total, List<String> keys)
    {
        if (keys == null || keys.isEmpty())
        {
            return Collections.emptyList();
        }
        long[] weights = new long[keys.size()];
        for (int i = 0; i < keys.size(); i++)
        {
            weights[i] = 10L + Math.floorMod(keys.get(i).hashCode(), 31);
        }
        long[] parts = distribute(total, weights);
        List<Long> result = new ArrayList<>(parts.length);
        for (long part : parts)
        {
            result.add(part);
        }
        return result;
    }

    private static void put(Map<String, AccessProfile> profiles, AccessProfile profile)
    {
        profiles.put(profile.systemKey(), profile);
    }

    private static long[] distribute(long total, long[] weights)
    {
        long[] result = new long[weights.length];
        if (weights.length == 0)
        {
            return result;
        }
        long weightSum = 0L;
        for (long weight : weights)
        {
            weightSum += Math.max(0L, weight);
        }
        if (weightSum <= 0L)
        {
            result[result.length - 1] = total;
            return result;
        }

        long allocated = 0L;
        for (int i = 0; i < weights.length - 1; i++)
        {
            result[i] = total * Math.max(0L, weights[i]) / weightSum;
            allocated += result[i];
        }
        result[weights.length - 1] = total - allocated;
        return result;
    }

    private static String normalize(String systemKey)
    {
        return systemKey == null ? "" : systemKey.trim().toUpperCase(Locale.ROOT);
    }

    record AccessProfile(String systemKey, String planName, String batchPrefix, String accessType, Long cycleHours,
            String useStatus, Long totalSuccessCount, Long totalFailedCount, Long lastInsertedCount,
            Long lastUpdatedCount, Long lastFailedCount)
    {
    }

    record BatchProfile(int sequence, String triggerType, long successCount, long failedCount, long insertedCount,
            long updatedCount)
    {
    }
}

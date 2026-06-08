package com.ruoyi.project1.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessBatchMapper;
import com.ruoyi.project1.mapper.AccessTableResultMapper;
import com.ruoyi.project1.domain.AccessBatch;
import com.ruoyi.project1.domain.AccessTableResult;
import com.ruoyi.project1.service.IAccessTableResultService;

/**
 * 数据接入结果展示Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessTableResultServiceImpl implements IAccessTableResultService 
{
    @Autowired
    private AccessTableResultMapper accessTableResultMapper;

    @Autowired
    private AccessBatchMapper accessBatchMapper;

    /**
     * 查询数据接入结果展示
     * 
     * @param tableResultId 数据接入结果展示主键
     * @return 数据接入结果展示
     */
    @Override
    public AccessTableResult selectAccessTableResultByTableResultId(Long tableResultId)
    {
        return accessTableResultMapper.selectAccessTableResultByTableResultId(tableResultId);
    }

    /**
     * 查询数据接入结果展示列表
     * 
     * @param accessTableResult 数据接入结果展示
     * @return 数据接入结果展示
     */
    @Override
    public List<AccessTableResult> selectAccessTableResultList(AccessTableResult accessTableResult)
    {
        return accessTableResultMapper.selectAccessTableResultList(accessTableResult);
    }

    /**
     * 新增数据接入结果展示
     * 
     * @param accessTableResult 数据接入结果展示
     * @return 结果
     */
    @Override
    public int insertAccessTableResult(AccessTableResult accessTableResult)
    {
        accessTableResult.setCreateTime(DateUtils.getNowDate());
        return accessTableResultMapper.insertAccessTableResult(accessTableResult);
    }

    /**
     * 修改数据接入结果展示
     * 
     * @param accessTableResult 数据接入结果展示
     * @return 结果
     */
    @Override
    public int updateAccessTableResult(AccessTableResult accessTableResult)
    {
        accessTableResult.setUpdateTime(DateUtils.getNowDate());
        return accessTableResultMapper.updateAccessTableResult(accessTableResult);
    }

    /**
     * 批量删除数据接入结果展示
     * 
     * @param tableResultIds 需要删除的数据接入结果展示主键
     * @return 结果
     */
    @Override
    public int deleteAccessTableResultByTableResultIds(Long[] tableResultIds)
    {
        return accessTableResultMapper.deleteAccessTableResultByTableResultIds(tableResultIds);
    }

    /**
     * 删除数据接入结果展示信息
     * 
     * @param tableResultId 数据接入结果展示主键
     * @return 结果
     */
    @Override
    public int deleteAccessTableResultByTableResultId(Long tableResultId)
    {
        return accessTableResultMapper.deleteAccessTableResultByTableResultId(tableResultId);
    }

    @Override
    public Map<String, Object> summary(Long accessPlanId)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        List<AccessTableResult> rows = selectAccessTableResultList(query);

        long totalSuccess = 0L;
        long totalFailed = 0L;
        long lastInserted = 0L;
        long lastUpdated = 0L;
        long lastFailed = 0L;
        Long latestBatchId = null;

        for (AccessTableResult row : rows)
        {
            totalSuccess += value(row.getSuccessCount());
            totalFailed += value(row.getFailedCount());
            if (row.getAccessBatchId() != null && (latestBatchId == null || row.getAccessBatchId() > latestBatchId))
            {
                latestBatchId = row.getAccessBatchId();
            }
        }
        for (AccessTableResult row : rows)
        {
            if (latestBatchId != null && latestBatchId.equals(row.getAccessBatchId()))
            {
                lastInserted += value(row.getInsertedCount());
                lastUpdated += value(row.getUpdatedCount());
                lastFailed += value(row.getFailedCount());
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("accessPlanId", accessPlanId);
        payload.put("latestBatchId", latestBatchId);
        payload.put("totalSuccess", totalSuccess);
        payload.put("totalFailed", totalFailed);
        payload.put("lastInserted", lastInserted);
        payload.put("lastUpdated", lastUpdated);
        payload.put("lastFailed", lastFailed);
        payload.put("rowCount", rows.size());
        return payload;
    }

    @Override
    public Map<String, Object> dashboard(Long accessPlanId)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        List<AccessTableResult> rows = selectAccessTableResultList(query);

        List<Map<String, Object>> batchOptions = buildBatchOptions(accessPlanId);
        List<Map<String, Object>> recentBatches = buildRecentBatches(rows, batchOptions);
        List<Map<String, Object>> topTables = buildTopTables(rows);

        Map<String, Object> payload = new HashMap<>();
        payload.put("accessPlanId", accessPlanId);
        payload.put("recentBatches", recentBatches);
        payload.put("topTables", topTables);
        payload.put("batchOptions", batchOptions);
        return payload;
    }

    private List<Map<String, Object>> buildBatchOptions(Long accessPlanId)
    {
        AccessBatch query = new AccessBatch();
        query.setAccessPlanId(accessPlanId);
        List<AccessBatch> batches = accessBatchMapper.selectAccessBatchList(query);
        batches.sort(Comparator.comparing(AccessBatch::getAccessBatchId, Comparator.nullsLast(Long::compareTo)).reversed());

        List<Map<String, Object>> rows = new ArrayList<>();
        for (AccessBatch batch : batches)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("batchId", batch.getAccessBatchId());
            item.put("batchName", batch.getBatchNo());
            item.put("batchStatus", batch.getBatchStatus());
            item.put("startedAt", batch.getStartedAt());
            rows.add(item);
        }
        return rows;
    }

    private List<Map<String, Object>> buildRecentBatches(List<AccessTableResult> rows, List<Map<String, Object>> batchOptions)
    {
        Map<Long, BatchStats> stats = new LinkedHashMap<>();
        for (AccessTableResult row : rows)
        {
            if (row.getAccessBatchId() == null)
            {
                continue;
            }
            BatchStats batchStats = stats.computeIfAbsent(row.getAccessBatchId(), BatchStats::new);
            batchStats.insertedCount += value(row.getInsertedCount());
            batchStats.updatedCount += value(row.getUpdatedCount());
            batchStats.failedCount += value(row.getFailedCount());
        }

        List<Map<String, Object>> recent = new ArrayList<>();
        int limit = Math.min(5, batchOptions.size());
        for (int i = limit - 1; i >= 0; i--)
        {
            Map<String, Object> option = batchOptions.get(i);
            Long batchId = (Long) option.get("batchId");
            BatchStats batchStats = stats.getOrDefault(batchId, new BatchStats(batchId));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("batchId", batchId);
            item.put("batchName", option.get("batchName"));
            item.put("insertedCount", batchStats.insertedCount);
            item.put("updatedCount", batchStats.updatedCount);
            item.put("failedCount", batchStats.failedCount);
            recent.add(item);
        }
        return recent;
    }

    private List<Map<String, Object>> buildTopTables(List<AccessTableResult> rows)
    {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (AccessTableResult row : rows)
        {
            String label = row.getTargetTable();
            if (label == null || label.trim().isEmpty())
            {
                continue;
            }
            stats.put(label, stats.getOrDefault(label, 0L) + value(row.getSuccessCount()));
        }

        List<Map.Entry<String, Long>> entries = new ArrayList<>(stats.entrySet());
        entries.sort(Map.Entry.<String, Long>comparingByValue().reversed());
        List<Map<String, Object>> topTables = new ArrayList<>();
        for (int i = 0; i < entries.size() && i < 10; i++)
        {
            Map.Entry<String, Long> entry = entries.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", entry.getKey());
            item.put("value", entry.getValue());
            topTables.add(item);
        }
        return topTables;
    }

    private static class BatchStats
    {
        private final Long batchId;
        private long insertedCount;
        private long updatedCount;
        private long failedCount;

        private BatchStats(Long batchId)
        {
            this.batchId = batchId;
        }
    }

    private long value(Long value)
    {
        return value == null ? 0L : value;
    }
}

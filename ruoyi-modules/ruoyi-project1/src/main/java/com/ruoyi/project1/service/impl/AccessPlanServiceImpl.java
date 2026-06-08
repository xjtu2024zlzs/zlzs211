package com.ruoyi.project1.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.project1.domain.AccessBatch;
import com.ruoyi.project1.domain.AccessPlan;
import com.ruoyi.project1.domain.AccessTableResult;
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.MappingSpecSet;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.mapper.AccessBatchMapper;
import com.ruoyi.project1.mapper.AccessPlanMapper;
import com.ruoyi.project1.mapper.AccessTableResultMapper;
import com.ruoyi.project1.mapper.ApiPullDatasourceMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.MappingSpecSetMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.service.IAccessPlanService;
import com.ruoyi.project1.service.support.ApiPullRuntimeService;

/**
 * 数据接入管理 Service 业务层处理
 *
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessPlanServiceImpl implements IAccessPlanService
{
    @Autowired
    private AccessPlanMapper accessPlanMapper;

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private MappingSpecSetMapper mappingSpecSetMapper;

    @Autowired
    private MatchResultSetMapper matchResultSetMapper;

    @Autowired
    private AccessBatchMapper accessBatchMapper;

    @Autowired
    private AccessTableResultMapper accessTableResultMapper;

    @Autowired
    private ApiPullDatasourceMapper apiPullDatasourceMapper;

    @Autowired
    private ApiPullRuntimeService apiPullRuntimeService;

    @Override
    public AccessPlan selectAccessPlanByAccessPlanId(Long accessPlanId)
    {
        AccessPlan plan = accessPlanMapper.selectAccessPlanByAccessPlanId(accessPlanId);
        enrichAccessPlan(plan);
        return plan;
    }

    @Override
    public List<AccessPlan> selectAccessPlanList(AccessPlan accessPlan)
    {
        List<AccessPlan> list = accessPlanMapper.selectAccessPlanList(accessPlan);
        for (AccessPlan row : list)
        {
            enrichAccessPlan(row);
        }
        return list;
    }

    @Override
    public int insertAccessPlan(AccessPlan accessPlan)
    {
        fillAccessPlanDefaults(accessPlan);
        accessPlan.setCreateTime(DateUtils.getNowDate());
        return accessPlanMapper.insertAccessPlan(accessPlan);
    }

    @Override
    public int updateAccessPlan(AccessPlan accessPlan)
    {
        fillAccessPlanDefaults(accessPlan);
        accessPlan.setUpdateTime(DateUtils.getNowDate());
        return accessPlanMapper.updateAccessPlan(accessPlan);
    }

    @Override
    public int deleteAccessPlanByAccessPlanIds(Long[] accessPlanIds)
    {
        return accessPlanMapper.deleteAccessPlanByAccessPlanIds(accessPlanIds);
    }

    @Override
    public int deleteAccessPlanByAccessPlanId(Long accessPlanId)
    {
        return accessPlanMapper.deleteAccessPlanByAccessPlanId(accessPlanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> execute(Long accessPlanId)
    {
        AccessPlan plan = requirePlan(accessPlanId);
        Datasource datasource = requireDatasource(plan.getSourceDatasourceId());
        if (plan.getSpecSetId() == null)
        {
            plan.setSpecSetId(resolveDefaultSpecSetId(plan.getSourceDatasourceId()));
        }

        ApiPullDatasource apiPullDetail = apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
        if ("api_pull".equals(datasource.getAccessMode()) && apiPullRuntimeService.canCallApiPull(apiPullDetail))
        {
            return apiPullRuntimeService.executeAccessPlan(plan, datasource, apiPullDetail);
        }

        throw new ServiceException("当前阶段只支持 API Pull 适配器执行，请检查数据源配置");
    }

    @Override
    public int pause(Long accessPlanId)
    {
        return updateUseStatus(accessPlanId, "paused");
    }

    @Override
    public int resume(Long accessPlanId)
    {
        return updateUseStatus(accessPlanId, "enabled");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancel(Long accessPlanId)
    {
        AccessPlan plan = requirePlan(accessPlanId);
        AccessBatch runningBatch = findRunningBatch(accessPlanId);
        if (runningBatch == null)
        {
            throw new ServiceException("当前没有执行中的接入批次");
        }

        runningBatch.setBatchStatus("canceled");
        runningBatch.setCancelRequestedAt(DateUtils.getNowDate());
        runningBatch.setFinishedAt(DateUtils.getNowDate());
        runningBatch.setUpdateTime(DateUtils.getNowDate());
        accessBatchMapper.updateAccessBatch(runningBatch);

        plan.setCurrentBatchId(runningBatch.getAccessBatchId());
        plan.setLastExecuteTime(DateUtils.getNowDate());
        plan.setUpdateTime(DateUtils.getNowDate());
        accessPlanMapper.updateAccessPlan(plan);
        return 1;
    }

    private void fillAccessPlanDefaults(AccessPlan accessPlan)
    {
        Datasource datasource = requireDatasource(accessPlan.getSourceDatasourceId());
        accessPlan.setAccessMode(datasource.getAccessMode());

        if (isBlank(accessPlan.getAccessType()))
        {
            accessPlan.setAccessType("once");
        }
        if ("continuous".equals(accessPlan.getAccessType()))
        {
            if (accessPlan.getCycleHours() == null || accessPlan.getCycleHours() < 1L)
            {
                accessPlan.setCycleHours(1L);
            }
        }
        else
        {
            accessPlan.setCycleHours(null);
        }
        if (isBlank(accessPlan.getUseStatus()))
        {
            accessPlan.setUseStatus("enabled");
        }
        if (isBlank(accessPlan.getStatus()))
        {
            accessPlan.setStatus("0");
        }
        if (isBlank(accessPlan.getDelFlag()))
        {
            accessPlan.setDelFlag("0");
        }
        if (accessPlan.getLastSuccessCount() == null)
        {
            accessPlan.setLastSuccessCount(0L);
        }
        if (accessPlan.getLastFailedCount() == null)
        {
            accessPlan.setLastFailedCount(0L);
        }
        if (accessPlan.getLastInsertedCount() == null)
        {
            accessPlan.setLastInsertedCount(0L);
        }
        if (accessPlan.getLastUpdatedCount() == null)
        {
            accessPlan.setLastUpdatedCount(0L);
        }
        if (accessPlan.getTotalSuccessCount() == null)
        {
            accessPlan.setTotalSuccessCount(0L);
        }
        if (accessPlan.getTotalFailedCount() == null)
        {
            accessPlan.setTotalFailedCount(0L);
        }
        if (accessPlan.getSpecSetId() == null)
        {
            accessPlan.setSpecSetId(resolveDefaultSpecSetId(accessPlan.getSourceDatasourceId()));
        }
    }

    private Long resolveDefaultSpecSetId(Long sourceDatasourceId)
    {
        try
        {
            MappingSpecSet query = new MappingSpecSet();
            query.setSourceDatasourceId(sourceDatasourceId);
            query.setIsDefault(1);
            query.setSpecStatus("active");
            List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
            rows.sort(Comparator.comparing(MappingSpecSet::getSpecSetId, Comparator.nullsLast(Long::compareTo)).reversed());
            if (!rows.isEmpty())
            {
                return rows.get(0).getSpecSetId();
            }
            throw new ServiceException("请先在模式映射结果展示中设为默认结果集并生成最终接入规则");
        }
        catch (ServiceException e)
        {
            throw new ServiceException("请先在模式映射结果展示中设为默认结果集并生成最终接入规则");
        }
    }

    private void enrichAccessPlan(AccessPlan plan)
    {
        if (plan == null)
        {
            return;
        }

        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(plan.getSourceDatasourceId());
        if (datasource != null)
        {
            plan.setSourceDatasourceName(datasource.getDatasourceName());
            if (isBlank(plan.getAccessMode()))
            {
                plan.setAccessMode(datasource.getAccessMode());
            }
        }

        plan.setDefaultResultSetName(resolveResultSetName(plan.getSpecSetId()));
        AccessBatch latestBatch = findLatestBatch(plan.getAccessPlanId());
        AccessStats stats = latestBatch == null ? new AccessStats() : sumTableResults(plan.getAccessPlanId(), latestBatch.getAccessBatchId());

        if ("once".equals(plan.getAccessType()))
        {
            plan.setLastInsertedCount(0L);
            plan.setLastUpdatedCount(0L);
            plan.setLastFailedCount(0L);
        }
        else
        {
            plan.setLastInsertedCount(stats.insertedCount);
            plan.setLastUpdatedCount(stats.updatedCount);
            plan.setLastFailedCount(stats.failedCount);
        }

        if (plan.getTotalSuccessCount() == null)
        {
            plan.setTotalSuccessCount(sumAllSuccess(plan.getAccessPlanId()));
        }
        if (plan.getTotalFailedCount() == null)
        {
            plan.setTotalFailedCount(sumAllFailed(plan.getAccessPlanId()));
        }
        plan.setDisplayStatus(resolveDisplayStatus(plan, latestBatch));
    }

    private String resolveResultSetName(Long specSetId)
    {
        if (specSetId == null)
        {
            return null;
        }
        MappingSpecSet specSet = mappingSpecSetMapper.selectMappingSpecSetBySpecSetId(specSetId);
        if (specSet == null)
        {
            return null;
        }
        if (specSet.getResultSetId() != null)
        {
            MatchResultSet resultSet = matchResultSetMapper.selectMatchResultSetByResultSetId(specSet.getResultSetId());
            if (resultSet != null && !isBlank(resultSet.getResultSetName()))
            {
                return resultSet.getResultSetName();
            }
        }
        return specSet.getSpecSetName();
    }

    private String resolveDisplayStatus(AccessPlan plan, AccessBatch latestBatch)
    {
        if ("paused".equals(plan.getUseStatus()))
        {
            return "paused";
        }
        if ("disabled".equals(plan.getUseStatus()))
        {
            return "disabled";
        }
        if ("unavailable".equals(plan.getUseStatus()))
        {
            return "blocked";
        }
        if (latestBatch != null && "running".equals(latestBatch.getBatchStatus()))
        {
            return "running";
        }
        if ("continuous".equals(plan.getAccessType()) && "enabled".equals(plan.getUseStatus()))
        {
            return "running";
        }
        if (value(plan.getTotalSuccessCount()) > 0L)
        {
            return "success";
        }
        return "pending";
    }

    private AccessPlan requirePlan(Long accessPlanId)
    {
        if (accessPlanId == null)
        {
            throw new ServiceException("接入计划 ID 不能为空");
        }
        AccessPlan plan = accessPlanMapper.selectAccessPlanByAccessPlanId(accessPlanId);
        if (plan == null)
        {
            throw new ServiceException("接入计划不存在，请先刷新列表");
        }
        return plan;
    }

    private Datasource requireDatasource(Long datasourceId)
    {
        if (datasourceId == null)
        {
            throw new ServiceException("来源数据源不能为空");
        }
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(datasourceId);
        if (datasource == null)
        {
            throw new ServiceException("来源数据源不存在，请先刷新数据源列表");
        }
        return datasource;
    }

    private int updateUseStatus(Long accessPlanId, String useStatus)
    {
        AccessPlan plan = requirePlan(accessPlanId);
        plan.setUseStatus(useStatus);
        plan.setUpdateTime(DateUtils.getNowDate());
        return accessPlanMapper.updateAccessPlan(plan);
    }

    private AccessBatch findRunningBatch(Long accessPlanId)
    {
        AccessBatch query = new AccessBatch();
        query.setAccessPlanId(accessPlanId);
        return accessBatchMapper.selectAccessBatchList(query).stream()
            .filter(item -> "running".equals(item.getBatchStatus()))
            .max(Comparator.comparing(AccessBatch::getAccessBatchId))
            .orElse(null);
    }

    private AccessBatch findLatestBatch(Long accessPlanId)
    {
        AccessBatch query = new AccessBatch();
        query.setAccessPlanId(accessPlanId);
        return accessBatchMapper.selectAccessBatchList(query).stream()
            .max(Comparator.comparing(AccessBatch::getAccessBatchId))
            .orElse(null);
    }

    private AccessStats sumTableResults(Long accessPlanId, Long accessBatchId)
    {
        AccessStats stats = new AccessStats();
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        query.setAccessBatchId(accessBatchId);
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            stats.insertedCount += value(row.getInsertedCount());
            stats.updatedCount += value(row.getUpdatedCount());
            stats.failedCount += value(row.getFailedCount());
        }
        return stats;
    }

    private long sumAllSuccess(Long accessPlanId)
    {
        long total = 0L;
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            total += value(row.getSuccessCount());
        }
        return total;
    }

    private long sumAllFailed(Long accessPlanId)
    {
        long total = 0L;
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            total += value(row.getFailedCount());
        }
        return total;
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private long value(Long value)
    {
        return value == null ? 0L : value;
    }

    private static class AccessStats
    {
        private long insertedCount;
        private long updatedCount;
        private long failedCount;
    }
}

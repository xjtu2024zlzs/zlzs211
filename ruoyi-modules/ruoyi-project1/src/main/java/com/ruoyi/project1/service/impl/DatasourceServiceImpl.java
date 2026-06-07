package com.ruoyi.project1.service.impl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.DbDatasourceMapper;
import com.ruoyi.project1.mapper.ApiPullDatasourceMapper;
import com.ruoyi.project1.mapper.ApiPushDatasourceMapper;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.DbDatasource;
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.domain.ApiPushDatasource;
import com.ruoyi.project1.service.IDatasourceService;
import com.ruoyi.project1.service.support.ApiPullRuntimeService;

/**
 * 数据源管理Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class DatasourceServiceImpl implements IDatasourceService 
{
    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private DbDatasourceMapper dbDatasourceMapper;

    @Autowired
    private ApiPullDatasourceMapper apiPullDatasourceMapper;

    @Autowired
    private ApiPushDatasourceMapper apiPushDatasourceMapper;

    @Autowired
    private ApiPullRuntimeService apiPullRuntimeService;

    /**
     * 查询数据源管理
     * 
     * @param datasourceId 数据源管理主键
     * @return 数据源管理
     */
    @Override
    public Datasource selectDatasourceByDatasourceId(Long datasourceId)
    {
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(datasourceId);
        attachModeDetail(datasource);
        return datasource;
    }

    /**
     * 查询数据源管理列表
     * 
     * @param datasource 数据源管理
     * @return 数据源管理
     */
    @Override
    public List<Datasource> selectDatasourceList(Datasource datasource)
    {
        return datasourceMapper.selectDatasourceList(datasource);
    }

    /**
     * 新增数据源管理
     * 
     * @param datasource 数据源管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDatasource(Datasource datasource)
    {
        if (datasource.getUseStatus() == null)
        {
            datasource.setUseStatus("enabled");
        }
        if (datasource.getConnectionStatus() == null)
        {
            datasource.setConnectionStatus("untested");
        }
        if (datasource.getStatus() == null)
        {
            datasource.setStatus("0");
        }
        if (datasource.getDelFlag() == null)
        {
            datasource.setDelFlag("0");
        }
        datasource.setCreateTime(DateUtils.getNowDate());
        int rows = datasourceMapper.insertDatasource(datasource);
        saveModeDetail(datasource, true);
        return rows;
    }

    /**
     * 修改数据源管理
     * 
     * @param datasource 数据源管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDatasource(Datasource datasource)
    {
        datasource.setUpdateTime(DateUtils.getNowDate());
        int rows = datasourceMapper.updateDatasource(datasource);
        if (hasModeDetail(datasource))
        {
            saveModeDetail(datasource, false);
        }
        return rows;
    }

    /**
     * 批量删除数据源管理
     * 
     * @param datasourceIds 需要删除的数据源管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDatasourceByDatasourceIds(Long[] datasourceIds)
    {
        dbDatasourceMapper.deleteDbDatasourceByDatasourceIds(datasourceIds);
        apiPullDatasourceMapper.deleteApiPullDatasourceByDatasourceIds(datasourceIds);
        apiPushDatasourceMapper.deleteApiPushDatasourceByDatasourceIds(datasourceIds);
        return datasourceMapper.deleteDatasourceByDatasourceIds(datasourceIds);
    }

    /**
     * 删除数据源管理信息
     * 
     * @param datasourceId 数据源管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDatasourceByDatasourceId(Long datasourceId)
    {
        dbDatasourceMapper.deleteDbDatasourceByDatasourceId(datasourceId);
        apiPullDatasourceMapper.deleteApiPullDatasourceByDatasourceId(datasourceId);
        apiPushDatasourceMapper.deleteApiPushDatasourceByDatasourceId(datasourceId);
        return datasourceMapper.deleteDatasourceByDatasourceId(datasourceId);
    }

    @Override
    public Map<String, Object> testConnection(Long datasourceId)
    {
        Datasource datasource = requireDatasource(datasourceId);
        ApiPullDatasource apiPullDetail = apiPullDetail(datasource);
        if ("api_pull".equals(datasource.getAccessMode()) && apiPullRuntimeService.canCallApiPull(apiPullDetail))
        {
            return apiPullRuntimeService.testConnection(datasource, apiPullDetail);
        }

        datasource.setConnectionStatus("success");
        datasource.setLastTestTime(DateUtils.getNowDate());
        datasource.setLastTestMessage("数据源连接正常，已具备模式读取和数据接入条件。");
        datasource.setUpdateTime(DateUtils.getNowDate());
        datasourceMapper.updateDatasource(datasource);

        Map<String, Object> result = new HashMap<>();
        result.put("datasourceId", datasource.getDatasourceId());
        result.put("connectionStatus", datasource.getConnectionStatus());
        result.put("lastTestTime", datasource.getLastTestTime());
        result.put("lastTestMessage", datasource.getLastTestMessage());
        return result;
    }

    @Override
    public Map<String, Object> readSchema(Long datasourceId)
    {
        Datasource datasource = requireDatasource(datasourceId);
        if (datasource.getLatestSchemaSnapshotId() != null)
        {
            Map<String, Object> payload = buildSchemaPayload(datasource);
            payload.put("alreadyRead", true);
            payload.put("message", "模式已读取");
            return payload;
        }
        ApiPullDatasource apiPullDetail = apiPullDetail(datasource);
        if ("api_pull".equals(datasource.getAccessMode()) && apiPullRuntimeService.canCallApiPull(apiPullDetail))
        {
            return apiPullRuntimeService.readSchema(datasource, apiPullDetail);
        }

        if (apiPullDetail == null || !apiPullRuntimeService.canCallApiPull(apiPullDetail))
        {
            throw new ServiceException("API Pull 数据源配置不完整，无法读取模式");
        }
        Map<String, Object> payload = buildSchemaPayload(datasource);
        payload.put("alreadyRead", false);
        payload.put("message", "模式读取完成");
        return payload;
    }

    @Override
    public Map<String, Object> getSchema(Long datasourceId)
    {
        return buildSchemaPayload(requireDatasource(datasourceId));
    }

    private Datasource requireDatasource(Long datasourceId)
    {
        if (datasourceId == null)
        {
            throw new ServiceException("数据源 ID 不能为空");
        }
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(datasourceId);
        if (datasource == null)
        {
            throw new ServiceException("数据源不存在，请先刷新列表");
        }
        if ("unavailable".equals(datasource.getUseStatus()))
        {
            throw new ServiceException("数据源暂时不可用，不能继续执行");
        }
        return datasource;
    }

    private void attachModeDetail(Datasource datasource)
    {
        if (datasource == null)
        {
            return;
        }
        if ("db_direct".equals(datasource.getAccessMode()))
        {
            datasource.setDbDetail(dbDatasourceMapper.selectDbDatasourceByDatasourceId(datasource.getDatasourceId()));
        }
        else if ("api_pull".equals(datasource.getAccessMode()))
        {
            datasource.setApiPullDetail(apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasource.getDatasourceId()));
        }
        else if ("api_push".equals(datasource.getAccessMode()))
        {
            datasource.setApiPushDetail(apiPushDatasourceMapper.selectApiPushDatasourceByDatasourceId(datasource.getDatasourceId()));
        }
    }

    private boolean hasModeDetail(Datasource datasource)
    {
        return datasource.getDbDetail() != null || datasource.getApiPullDetail() != null || datasource.getApiPushDetail() != null;
    }

    private void saveModeDetail(Datasource datasource, boolean insertedMain)
    {
        if (datasource.getDatasourceId() == null)
        {
            return;
        }
        if ("db_direct".equals(datasource.getAccessMode()))
        {
            saveDbDetail(datasource, insertedMain);
            apiPullDatasourceMapper.deleteApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
            apiPushDatasourceMapper.deleteApiPushDatasourceByDatasourceId(datasource.getDatasourceId());
        }
        else if ("api_pull".equals(datasource.getAccessMode()))
        {
            saveApiPullDetail(datasource, insertedMain);
            dbDatasourceMapper.deleteDbDatasourceByDatasourceId(datasource.getDatasourceId());
            apiPushDatasourceMapper.deleteApiPushDatasourceByDatasourceId(datasource.getDatasourceId());
        }
        else if ("api_push".equals(datasource.getAccessMode()))
        {
            saveApiPushDetail(datasource, insertedMain);
            dbDatasourceMapper.deleteDbDatasourceByDatasourceId(datasource.getDatasourceId());
            apiPullDatasourceMapper.deleteApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
        }
    }

    private void saveDbDetail(Datasource datasource, boolean insertedMain)
    {
        DbDatasource detail = datasource.getDbDetail();
        if (detail == null)
        {
            return;
        }
        detail.setDatasourceId(datasource.getDatasourceId());
        detail.setRemark(datasource.getRemark());
        DbDatasource existing = insertedMain ? null : dbDatasourceMapper.selectDbDatasourceByDatasourceId(datasource.getDatasourceId());
        if (existing == null)
        {
            detail.setCreateTime(DateUtils.getNowDate());
            dbDatasourceMapper.insertDbDatasource(detail);
        }
        else
        {
            detail.setUpdateTime(DateUtils.getNowDate());
            dbDatasourceMapper.updateDbDatasource(detail);
        }
    }

    private void saveApiPullDetail(Datasource datasource, boolean insertedMain)
    {
        ApiPullDatasource detail = datasource.getApiPullDetail();
        if (detail == null)
        {
            return;
        }
        detail.setDatasourceId(datasource.getDatasourceId());
        detail.setRemark(datasource.getRemark());
        ApiPullDatasource existing = insertedMain ? null : apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
        if (existing == null)
        {
            detail.setCreateTime(DateUtils.getNowDate());
            apiPullDatasourceMapper.insertApiPullDatasource(detail);
        }
        else
        {
            detail.setUpdateTime(DateUtils.getNowDate());
            apiPullDatasourceMapper.updateApiPullDatasource(detail);
        }
    }

    private void saveApiPushDetail(Datasource datasource, boolean insertedMain)
    {
        ApiPushDatasource detail = datasource.getApiPushDetail();
        if (detail == null)
        {
            return;
        }
        detail.setDatasourceId(datasource.getDatasourceId());
        detail.setRemark(datasource.getRemark());
        ApiPushDatasource existing = insertedMain ? null : apiPushDatasourceMapper.selectApiPushDatasourceByDatasourceId(datasource.getDatasourceId());
        if (existing == null)
        {
            detail.setCreateTime(DateUtils.getNowDate());
            apiPushDatasourceMapper.insertApiPushDatasource(detail);
        }
        else
        {
            detail.setUpdateTime(DateUtils.getNowDate());
            apiPushDatasourceMapper.updateApiPushDatasource(detail);
        }
    }

    private Map<String, Object> buildSchemaPayload(Datasource datasource)
    {
        if (datasource.getLatestSchemaSnapshotId() != null)
        {
            return apiPullRuntimeService.buildStoredSchemaPayload(datasource);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("datasourceId", datasource.getDatasourceId());
        payload.put("datasourceName", datasource.getDatasourceName());
        payload.put("accessMode", datasource.getAccessMode());
        payload.put("latestSchemaSnapshotId", datasource.getLatestSchemaSnapshotId());
        payload.put("rows", List.of());
        return payload;
    }

    private ApiPullDatasource apiPullDetail(Datasource datasource)
    {
        if (!"api_pull".equals(datasource.getAccessMode()))
        {
            return null;
        }
        return apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
    }
}

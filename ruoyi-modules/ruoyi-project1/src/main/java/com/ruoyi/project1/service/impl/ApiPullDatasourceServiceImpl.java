package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.ApiPullDatasourceMapper;
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.service.IApiPullDatasourceService;

/**
 * API 拉取数据源明细Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class ApiPullDatasourceServiceImpl implements IApiPullDatasourceService 
{
    @Autowired
    private ApiPullDatasourceMapper apiPullDatasourceMapper;

    /**
     * 查询API 拉取数据源明细
     * 
     * @param datasourceId API 拉取数据源明细主键
     * @return API 拉取数据源明细
     */
    @Override
    public ApiPullDatasource selectApiPullDatasourceByDatasourceId(Long datasourceId)
    {
        return apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasourceId);
    }

    /**
     * 查询API 拉取数据源明细列表
     * 
     * @param apiPullDatasource API 拉取数据源明细
     * @return API 拉取数据源明细
     */
    @Override
    public List<ApiPullDatasource> selectApiPullDatasourceList(ApiPullDatasource apiPullDatasource)
    {
        return apiPullDatasourceMapper.selectApiPullDatasourceList(apiPullDatasource);
    }

    /**
     * 新增API 拉取数据源明细
     * 
     * @param apiPullDatasource API 拉取数据源明细
     * @return 结果
     */
    @Override
    public int insertApiPullDatasource(ApiPullDatasource apiPullDatasource)
    {
        apiPullDatasource.setCreateTime(DateUtils.getNowDate());
        return apiPullDatasourceMapper.insertApiPullDatasource(apiPullDatasource);
    }

    /**
     * 修改API 拉取数据源明细
     * 
     * @param apiPullDatasource API 拉取数据源明细
     * @return 结果
     */
    @Override
    public int updateApiPullDatasource(ApiPullDatasource apiPullDatasource)
    {
        apiPullDatasource.setUpdateTime(DateUtils.getNowDate());
        return apiPullDatasourceMapper.updateApiPullDatasource(apiPullDatasource);
    }

    /**
     * 批量删除API 拉取数据源明细
     * 
     * @param datasourceIds 需要删除的API 拉取数据源明细主键
     * @return 结果
     */
    @Override
    public int deleteApiPullDatasourceByDatasourceIds(Long[] datasourceIds)
    {
        return apiPullDatasourceMapper.deleteApiPullDatasourceByDatasourceIds(datasourceIds);
    }

    /**
     * 删除API 拉取数据源明细信息
     * 
     * @param datasourceId API 拉取数据源明细主键
     * @return 结果
     */
    @Override
    public int deleteApiPullDatasourceByDatasourceId(Long datasourceId)
    {
        return apiPullDatasourceMapper.deleteApiPullDatasourceByDatasourceId(datasourceId);
    }
}

package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.ApiPushDatasourceMapper;
import com.ruoyi.project1.domain.ApiPushDatasource;
import com.ruoyi.project1.service.IApiPushDatasourceService;

/**
 * API 推送数据源明细Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class ApiPushDatasourceServiceImpl implements IApiPushDatasourceService 
{
    @Autowired
    private ApiPushDatasourceMapper apiPushDatasourceMapper;

    /**
     * 查询API 推送数据源明细
     * 
     * @param datasourceId API 推送数据源明细主键
     * @return API 推送数据源明细
     */
    @Override
    public ApiPushDatasource selectApiPushDatasourceByDatasourceId(Long datasourceId)
    {
        return apiPushDatasourceMapper.selectApiPushDatasourceByDatasourceId(datasourceId);
    }

    /**
     * 查询API 推送数据源明细列表
     * 
     * @param apiPushDatasource API 推送数据源明细
     * @return API 推送数据源明细
     */
    @Override
    public List<ApiPushDatasource> selectApiPushDatasourceList(ApiPushDatasource apiPushDatasource)
    {
        return apiPushDatasourceMapper.selectApiPushDatasourceList(apiPushDatasource);
    }

    /**
     * 新增API 推送数据源明细
     * 
     * @param apiPushDatasource API 推送数据源明细
     * @return 结果
     */
    @Override
    public int insertApiPushDatasource(ApiPushDatasource apiPushDatasource)
    {
        apiPushDatasource.setCreateTime(DateUtils.getNowDate());
        return apiPushDatasourceMapper.insertApiPushDatasource(apiPushDatasource);
    }

    /**
     * 修改API 推送数据源明细
     * 
     * @param apiPushDatasource API 推送数据源明细
     * @return 结果
     */
    @Override
    public int updateApiPushDatasource(ApiPushDatasource apiPushDatasource)
    {
        apiPushDatasource.setUpdateTime(DateUtils.getNowDate());
        return apiPushDatasourceMapper.updateApiPushDatasource(apiPushDatasource);
    }

    /**
     * 批量删除API 推送数据源明细
     * 
     * @param datasourceIds 需要删除的API 推送数据源明细主键
     * @return 结果
     */
    @Override
    public int deleteApiPushDatasourceByDatasourceIds(Long[] datasourceIds)
    {
        return apiPushDatasourceMapper.deleteApiPushDatasourceByDatasourceIds(datasourceIds);
    }

    /**
     * 删除API 推送数据源明细信息
     * 
     * @param datasourceId API 推送数据源明细主键
     * @return 结果
     */
    @Override
    public int deleteApiPushDatasourceByDatasourceId(Long datasourceId)
    {
        return apiPushDatasourceMapper.deleteApiPushDatasourceByDatasourceId(datasourceId);
    }
}

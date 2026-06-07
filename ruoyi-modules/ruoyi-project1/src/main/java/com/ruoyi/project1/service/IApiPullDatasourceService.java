package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.ApiPullDatasource;

/**
 * API 拉取数据源明细Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IApiPullDatasourceService 
{
    /**
     * 查询API 拉取数据源明细
     * 
     * @param datasourceId API 拉取数据源明细主键
     * @return API 拉取数据源明细
     */
    public ApiPullDatasource selectApiPullDatasourceByDatasourceId(Long datasourceId);

    /**
     * 查询API 拉取数据源明细列表
     * 
     * @param apiPullDatasource API 拉取数据源明细
     * @return API 拉取数据源明细集合
     */
    public List<ApiPullDatasource> selectApiPullDatasourceList(ApiPullDatasource apiPullDatasource);

    /**
     * 新增API 拉取数据源明细
     * 
     * @param apiPullDatasource API 拉取数据源明细
     * @return 结果
     */
    public int insertApiPullDatasource(ApiPullDatasource apiPullDatasource);

    /**
     * 修改API 拉取数据源明细
     * 
     * @param apiPullDatasource API 拉取数据源明细
     * @return 结果
     */
    public int updateApiPullDatasource(ApiPullDatasource apiPullDatasource);

    /**
     * 批量删除API 拉取数据源明细
     * 
     * @param datasourceIds 需要删除的API 拉取数据源明细主键集合
     * @return 结果
     */
    public int deleteApiPullDatasourceByDatasourceIds(Long[] datasourceIds);

    /**
     * 删除API 拉取数据源明细信息
     * 
     * @param datasourceId API 拉取数据源明细主键
     * @return 结果
     */
    public int deleteApiPullDatasourceByDatasourceId(Long datasourceId);
}

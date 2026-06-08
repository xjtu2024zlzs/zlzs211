package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.ApiPushDatasource;

/**
 * API 推送数据源明细Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IApiPushDatasourceService 
{
    /**
     * 查询API 推送数据源明细
     * 
     * @param datasourceId API 推送数据源明细主键
     * @return API 推送数据源明细
     */
    public ApiPushDatasource selectApiPushDatasourceByDatasourceId(Long datasourceId);

    /**
     * 查询API 推送数据源明细列表
     * 
     * @param apiPushDatasource API 推送数据源明细
     * @return API 推送数据源明细集合
     */
    public List<ApiPushDatasource> selectApiPushDatasourceList(ApiPushDatasource apiPushDatasource);

    /**
     * 新增API 推送数据源明细
     * 
     * @param apiPushDatasource API 推送数据源明细
     * @return 结果
     */
    public int insertApiPushDatasource(ApiPushDatasource apiPushDatasource);

    /**
     * 修改API 推送数据源明细
     * 
     * @param apiPushDatasource API 推送数据源明细
     * @return 结果
     */
    public int updateApiPushDatasource(ApiPushDatasource apiPushDatasource);

    /**
     * 批量删除API 推送数据源明细
     * 
     * @param datasourceIds 需要删除的API 推送数据源明细主键集合
     * @return 结果
     */
    public int deleteApiPushDatasourceByDatasourceIds(Long[] datasourceIds);

    /**
     * 删除API 推送数据源明细信息
     * 
     * @param datasourceId API 推送数据源明细主键
     * @return 结果
     */
    public int deleteApiPushDatasourceByDatasourceId(Long datasourceId);
}

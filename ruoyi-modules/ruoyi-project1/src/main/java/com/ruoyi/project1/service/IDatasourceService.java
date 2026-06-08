package com.ruoyi.project1.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.Datasource;

/**
 * 数据源管理Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IDatasourceService 
{
    /**
     * 查询数据源管理
     * 
     * @param datasourceId 数据源管理主键
     * @return 数据源管理
     */
    public Datasource selectDatasourceByDatasourceId(Long datasourceId);

    /**
     * 查询数据源管理列表
     * 
     * @param datasource 数据源管理
     * @return 数据源管理集合
     */
    public List<Datasource> selectDatasourceList(Datasource datasource);

    /**
     * 新增数据源管理
     * 
     * @param datasource 数据源管理
     * @return 结果
     */
    public int insertDatasource(Datasource datasource);

    /**
     * 修改数据源管理
     * 
     * @param datasource 数据源管理
     * @return 结果
     */
    public int updateDatasource(Datasource datasource);

    /**
     * 批量删除数据源管理
     * 
     * @param datasourceIds 需要删除的数据源管理主键集合
     * @return 结果
     */
    public int deleteDatasourceByDatasourceIds(Long[] datasourceIds);

    /**
     * 删除数据源管理信息
     * 
     * @param datasourceId 数据源管理主键
     * @return 结果
     */
    public int deleteDatasourceByDatasourceId(Long datasourceId);

    /**
     * Test datasource connectivity for the access flow demo.
     *
     * @param datasourceId datasource id
     * @return test result payload
     */
    public Map<String, Object> testConnection(Long datasourceId);

    /**
     * Read datasource schema and advance the schema snapshot version.
     *
     * @param datasourceId datasource id
     * @return schema preview payload
     */
    public Map<String, Object> readSchema(Long datasourceId);

    /**
     * Get current schema preview rows for datasource.
     *
     * @param datasourceId datasource id
     * @return schema preview payload
     */
    public Map<String, Object> getSchema(Long datasourceId);
}

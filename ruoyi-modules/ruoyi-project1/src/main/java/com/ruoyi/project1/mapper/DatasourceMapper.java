package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.Datasource;

/**
 * 数据源管理Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface DatasourceMapper 
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
     * 删除数据源管理
     * 
     * @param datasourceId 数据源管理主键
     * @return 结果
     */
    public int deleteDatasourceByDatasourceId(Long datasourceId);

    /**
     * 批量删除数据源管理
     * 
     * @param datasourceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDatasourceByDatasourceIds(Long[] datasourceIds);
}

package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.DbDatasource;

/**
 * 数据库直连数据源明细Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IDbDatasourceService 
{
    /**
     * 查询数据库直连数据源明细
     * 
     * @param datasourceId 数据库直连数据源明细主键
     * @return 数据库直连数据源明细
     */
    public DbDatasource selectDbDatasourceByDatasourceId(Long datasourceId);

    /**
     * 查询数据库直连数据源明细列表
     * 
     * @param dbDatasource 数据库直连数据源明细
     * @return 数据库直连数据源明细集合
     */
    public List<DbDatasource> selectDbDatasourceList(DbDatasource dbDatasource);

    /**
     * 新增数据库直连数据源明细
     * 
     * @param dbDatasource 数据库直连数据源明细
     * @return 结果
     */
    public int insertDbDatasource(DbDatasource dbDatasource);

    /**
     * 修改数据库直连数据源明细
     * 
     * @param dbDatasource 数据库直连数据源明细
     * @return 结果
     */
    public int updateDbDatasource(DbDatasource dbDatasource);

    /**
     * 批量删除数据库直连数据源明细
     * 
     * @param datasourceIds 需要删除的数据库直连数据源明细主键集合
     * @return 结果
     */
    public int deleteDbDatasourceByDatasourceIds(Long[] datasourceIds);

    /**
     * 删除数据库直连数据源明细信息
     * 
     * @param datasourceId 数据库直连数据源明细主键
     * @return 结果
     */
    public int deleteDbDatasourceByDatasourceId(Long datasourceId);
}

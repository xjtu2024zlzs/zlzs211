package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.DbDatasourceMapper;
import com.ruoyi.project1.domain.DbDatasource;
import com.ruoyi.project1.service.IDbDatasourceService;

/**
 * 数据库直连数据源明细Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class DbDatasourceServiceImpl implements IDbDatasourceService 
{
    @Autowired
    private DbDatasourceMapper dbDatasourceMapper;

    /**
     * 查询数据库直连数据源明细
     * 
     * @param datasourceId 数据库直连数据源明细主键
     * @return 数据库直连数据源明细
     */
    @Override
    public DbDatasource selectDbDatasourceByDatasourceId(Long datasourceId)
    {
        return dbDatasourceMapper.selectDbDatasourceByDatasourceId(datasourceId);
    }

    /**
     * 查询数据库直连数据源明细列表
     * 
     * @param dbDatasource 数据库直连数据源明细
     * @return 数据库直连数据源明细
     */
    @Override
    public List<DbDatasource> selectDbDatasourceList(DbDatasource dbDatasource)
    {
        return dbDatasourceMapper.selectDbDatasourceList(dbDatasource);
    }

    /**
     * 新增数据库直连数据源明细
     * 
     * @param dbDatasource 数据库直连数据源明细
     * @return 结果
     */
    @Override
    public int insertDbDatasource(DbDatasource dbDatasource)
    {
        dbDatasource.setCreateTime(DateUtils.getNowDate());
        return dbDatasourceMapper.insertDbDatasource(dbDatasource);
    }

    /**
     * 修改数据库直连数据源明细
     * 
     * @param dbDatasource 数据库直连数据源明细
     * @return 结果
     */
    @Override
    public int updateDbDatasource(DbDatasource dbDatasource)
    {
        dbDatasource.setUpdateTime(DateUtils.getNowDate());
        return dbDatasourceMapper.updateDbDatasource(dbDatasource);
    }

    /**
     * 批量删除数据库直连数据源明细
     * 
     * @param datasourceIds 需要删除的数据库直连数据源明细主键
     * @return 结果
     */
    @Override
    public int deleteDbDatasourceByDatasourceIds(Long[] datasourceIds)
    {
        return dbDatasourceMapper.deleteDbDatasourceByDatasourceIds(datasourceIds);
    }

    /**
     * 删除数据库直连数据源明细信息
     * 
     * @param datasourceId 数据库直连数据源明细主键
     * @return 结果
     */
    @Override
    public int deleteDbDatasourceByDatasourceId(Long datasourceId)
    {
        return dbDatasourceMapper.deleteDbDatasourceByDatasourceId(datasourceId);
    }
}

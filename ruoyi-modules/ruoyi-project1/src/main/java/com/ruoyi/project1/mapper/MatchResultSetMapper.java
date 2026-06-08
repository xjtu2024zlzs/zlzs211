package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MatchResultSet;

/**
 * 模式映射结果展示Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MatchResultSetMapper 
{
    /**
     * 查询模式映射结果展示
     * 
     * @param resultSetId 模式映射结果展示主键
     * @return 模式映射结果展示
     */
    public MatchResultSet selectMatchResultSetByResultSetId(Long resultSetId);

    /**
     * 查询模式映射结果展示列表
     * 
     * @param matchResultSet 模式映射结果展示
     * @return 模式映射结果展示集合
     */
    public List<MatchResultSet> selectMatchResultSetList(MatchResultSet matchResultSet);

    /**
     * 新增模式映射结果展示
     * 
     * @param matchResultSet 模式映射结果展示
     * @return 结果
     */
    public int insertMatchResultSet(MatchResultSet matchResultSet);

    /**
     * 修改模式映射结果展示
     * 
     * @param matchResultSet 模式映射结果展示
     * @return 结果
     */
    public int updateMatchResultSet(MatchResultSet matchResultSet);

    /**
     * 删除模式映射结果展示
     * 
     * @param resultSetId 模式映射结果展示主键
     * @return 结果
     */
    public int deleteMatchResultSetByResultSetId(Long resultSetId);

    /**
     * 批量删除模式映射结果展示
     * 
     * @param resultSetIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMatchResultSetByResultSetIds(Long[] resultSetIds);
}

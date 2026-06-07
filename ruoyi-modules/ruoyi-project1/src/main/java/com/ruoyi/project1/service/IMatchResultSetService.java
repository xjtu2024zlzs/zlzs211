package com.ruoyi.project1.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.MatchResultSet;

/**
 * 模式映射结果展示Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IMatchResultSetService 
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
     * 批量删除模式映射结果展示
     * 
     * @param resultSetIds 需要删除的模式映射结果展示主键集合
     * @return 结果
     */
    public int deleteMatchResultSetByResultSetIds(Long[] resultSetIds);

    /**
     * 删除模式映射结果展示信息
     * 
     * @param resultSetId 模式映射结果展示主键
     * @return 结果
     */
    public int deleteMatchResultSetByResultSetId(Long resultSetId);

    /**
     * Mark one result set as default for its task/source.
     *
     * @param resultSetId result set id
     * @return default result payload
     */
    public Map<String, Object> setDefault(Long resultSetId);

    /**
     * Query field-level match rows for one result set.
     *
     * @param resultSetId result set id
     * @return detail payload
     */
    public Map<String, Object> detail(Long resultSetId, String reviewStatus, String reviewedBy);

    /**
     * Query metric visualization payload for one result set.
     *
     * @param resultSetId result set id
     * @return metrics payload
     */
    public Map<String, Object> metrics(Long resultSetId);
}

package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MatchResultRow;

/**
 * 模式映射结果明细Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MatchResultRowMapper 
{
    /**
     * 查询模式映射结果明细
     * 
     * @param resultRowId 模式映射结果明细主键
     * @return 模式映射结果明细
     */
    public MatchResultRow selectMatchResultRowByResultRowId(Long resultRowId);

    /**
     * 查询模式映射结果明细列表
     * 
     * @param matchResultRow 模式映射结果明细
     * @return 模式映射结果明细集合
     */
    public List<MatchResultRow> selectMatchResultRowList(MatchResultRow matchResultRow);

    /**
     * 新增模式映射结果明细
     * 
     * @param matchResultRow 模式映射结果明细
     * @return 结果
     */
    public int insertMatchResultRow(MatchResultRow matchResultRow);

    /**
     * 修改模式映射结果明细
     * 
     * @param matchResultRow 模式映射结果明细
     * @return 结果
     */
    public int updateMatchResultRow(MatchResultRow matchResultRow);

    /**
     * 删除模式映射结果明细
     * 
     * @param resultRowId 模式映射结果明细主键
     * @return 结果
     */
    public int deleteMatchResultRowByResultRowId(Long resultRowId);

    /**
     * 批量删除模式映射结果明细
     * 
     * @param resultRowIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMatchResultRowByResultRowIds(Long[] resultRowIds);
}

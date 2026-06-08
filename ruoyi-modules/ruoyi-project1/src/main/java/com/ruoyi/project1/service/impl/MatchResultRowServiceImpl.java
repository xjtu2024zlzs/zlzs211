package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.MatchResultRowMapper;
import com.ruoyi.project1.domain.MatchResultRow;
import com.ruoyi.project1.service.IMatchResultRowService;

/**
 * 模式映射结果明细Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class MatchResultRowServiceImpl implements IMatchResultRowService 
{
    @Autowired
    private MatchResultRowMapper matchResultRowMapper;

    /**
     * 查询模式映射结果明细
     * 
     * @param resultRowId 模式映射结果明细主键
     * @return 模式映射结果明细
     */
    @Override
    public MatchResultRow selectMatchResultRowByResultRowId(Long resultRowId)
    {
        return matchResultRowMapper.selectMatchResultRowByResultRowId(resultRowId);
    }

    /**
     * 查询模式映射结果明细列表
     * 
     * @param matchResultRow 模式映射结果明细
     * @return 模式映射结果明细
     */
    @Override
    public List<MatchResultRow> selectMatchResultRowList(MatchResultRow matchResultRow)
    {
        return matchResultRowMapper.selectMatchResultRowList(matchResultRow);
    }

    /**
     * 新增模式映射结果明细
     * 
     * @param matchResultRow 模式映射结果明细
     * @return 结果
     */
    @Override
    public int insertMatchResultRow(MatchResultRow matchResultRow)
    {
        matchResultRow.setCreateTime(DateUtils.getNowDate());
        return matchResultRowMapper.insertMatchResultRow(matchResultRow);
    }

    /**
     * 修改模式映射结果明细
     * 
     * @param matchResultRow 模式映射结果明细
     * @return 结果
     */
    @Override
    public int updateMatchResultRow(MatchResultRow matchResultRow)
    {
        return matchResultRowMapper.updateMatchResultRow(matchResultRow);
    }

    /**
     * 批量删除模式映射结果明细
     * 
     * @param resultRowIds 需要删除的模式映射结果明细主键
     * @return 结果
     */
    @Override
    public int deleteMatchResultRowByResultRowIds(Long[] resultRowIds)
    {
        return matchResultRowMapper.deleteMatchResultRowByResultRowIds(resultRowIds);
    }

    /**
     * 删除模式映射结果明细信息
     * 
     * @param resultRowId 模式映射结果明细主键
     * @return 结果
     */
    @Override
    public int deleteMatchResultRowByResultRowId(Long resultRowId)
    {
        return matchResultRowMapper.deleteMatchResultRowByResultRowId(resultRowId);
    }
}

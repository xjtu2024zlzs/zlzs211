package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.MatchTaskRecordMapper;
import com.ruoyi.project1.domain.MatchTaskRecord;
import com.ruoyi.project1.service.IMatchTaskRecordService;

/**
 * 模式映射运行记录Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class MatchTaskRecordServiceImpl implements IMatchTaskRecordService 
{
    @Autowired
    private MatchTaskRecordMapper matchTaskRecordMapper;

    /**
     * 查询模式映射运行记录
     * 
     * @param recordId 模式映射运行记录主键
     * @return 模式映射运行记录
     */
    @Override
    public MatchTaskRecord selectMatchTaskRecordByRecordId(Long recordId)
    {
        return matchTaskRecordMapper.selectMatchTaskRecordByRecordId(recordId);
    }

    /**
     * 查询模式映射运行记录列表
     * 
     * @param matchTaskRecord 模式映射运行记录
     * @return 模式映射运行记录
     */
    @Override
    public List<MatchTaskRecord> selectMatchTaskRecordList(MatchTaskRecord matchTaskRecord)
    {
        return matchTaskRecordMapper.selectMatchTaskRecordList(matchTaskRecord);
    }

    /**
     * 新增模式映射运行记录
     * 
     * @param matchTaskRecord 模式映射运行记录
     * @return 结果
     */
    @Override
    public int insertMatchTaskRecord(MatchTaskRecord matchTaskRecord)
    {
        matchTaskRecord.setCreateTime(DateUtils.getNowDate());
        return matchTaskRecordMapper.insertMatchTaskRecord(matchTaskRecord);
    }

    /**
     * 修改模式映射运行记录
     * 
     * @param matchTaskRecord 模式映射运行记录
     * @return 结果
     */
    @Override
    public int updateMatchTaskRecord(MatchTaskRecord matchTaskRecord)
    {
        return matchTaskRecordMapper.updateMatchTaskRecord(matchTaskRecord);
    }

    /**
     * 批量删除模式映射运行记录
     * 
     * @param recordIds 需要删除的模式映射运行记录主键
     * @return 结果
     */
    @Override
    public int deleteMatchTaskRecordByRecordIds(Long[] recordIds)
    {
        return matchTaskRecordMapper.deleteMatchTaskRecordByRecordIds(recordIds);
    }

    /**
     * 删除模式映射运行记录信息
     * 
     * @param recordId 模式映射运行记录主键
     * @return 结果
     */
    @Override
    public int deleteMatchTaskRecordByRecordId(Long recordId)
    {
        return matchTaskRecordMapper.deleteMatchTaskRecordByRecordId(recordId);
    }
}

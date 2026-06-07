package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.MatchTaskRecord;

/**
 * 模式映射运行记录Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IMatchTaskRecordService 
{
    /**
     * 查询模式映射运行记录
     * 
     * @param recordId 模式映射运行记录主键
     * @return 模式映射运行记录
     */
    public MatchTaskRecord selectMatchTaskRecordByRecordId(Long recordId);

    /**
     * 查询模式映射运行记录列表
     * 
     * @param matchTaskRecord 模式映射运行记录
     * @return 模式映射运行记录集合
     */
    public List<MatchTaskRecord> selectMatchTaskRecordList(MatchTaskRecord matchTaskRecord);

    /**
     * 新增模式映射运行记录
     * 
     * @param matchTaskRecord 模式映射运行记录
     * @return 结果
     */
    public int insertMatchTaskRecord(MatchTaskRecord matchTaskRecord);

    /**
     * 修改模式映射运行记录
     * 
     * @param matchTaskRecord 模式映射运行记录
     * @return 结果
     */
    public int updateMatchTaskRecord(MatchTaskRecord matchTaskRecord);

    /**
     * 批量删除模式映射运行记录
     * 
     * @param recordIds 需要删除的模式映射运行记录主键集合
     * @return 结果
     */
    public int deleteMatchTaskRecordByRecordIds(Long[] recordIds);

    /**
     * 删除模式映射运行记录信息
     * 
     * @param recordId 模式映射运行记录主键
     * @return 结果
     */
    public int deleteMatchTaskRecordByRecordId(Long recordId);
}

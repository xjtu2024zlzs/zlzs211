package com.ruoyi.quality.mapper;

import java.util.List;
import com.ruoyi.quality.domain.QmsFlowLog;

/**
 * 质量问题流程日志Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public interface QmsFlowLogMapper 
{
    /**
     * 查询质量问题流程日志
     * 
     * @param logId 质量问题流程日志主键
     * @return 质量问题流程日志
     */
    public QmsFlowLog selectQmsFlowLogByLogId(Long logId);

    /**
     * 查询质量问题流程日志列表
     * 
     * @param qmsFlowLog 质量问题流程日志
     * @return 质量问题流程日志集合
     */
    public List<QmsFlowLog> selectQmsFlowLogList(QmsFlowLog qmsFlowLog);

    /**
     * 新增质量问题流程日志
     * 
     * @param qmsFlowLog 质量问题流程日志
     * @return 结果
     */
    public int insertQmsFlowLog(QmsFlowLog qmsFlowLog);

    /**
     * 修改质量问题流程日志
     * 
     * @param qmsFlowLog 质量问题流程日志
     * @return 结果
     */
    public int updateQmsFlowLog(QmsFlowLog qmsFlowLog);

    /**
     * 删除质量问题流程日志
     * 
     * @param logId 质量问题流程日志主键
     * @return 结果
     */
    public int deleteQmsFlowLogByLogId(Long logId);

    /**
     * 批量删除质量问题流程日志
     * 
     * @param logIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQmsFlowLogByLogIds(Long[] logIds);
}

package com.ruoyi.quality.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.quality.mapper.QmsFlowLogMapper;
import com.ruoyi.quality.domain.QmsFlowLog;
import com.ruoyi.quality.service.IQmsFlowLogService;

/**
 * 质量问题流程日志Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@Service
public class QmsFlowLogServiceImpl implements IQmsFlowLogService 
{
    @Autowired
    private QmsFlowLogMapper qmsFlowLogMapper;

    /**
     * 查询质量问题流程日志
     * 
     * @param logId 质量问题流程日志主键
     * @return 质量问题流程日志
     */
    @Override
    public QmsFlowLog selectQmsFlowLogByLogId(Long logId)
    {
        return qmsFlowLogMapper.selectQmsFlowLogByLogId(logId);
    }

    /**
     * 查询质量问题流程日志列表
     * 
     * @param qmsFlowLog 质量问题流程日志
     * @return 质量问题流程日志
     */
    @Override
    public List<QmsFlowLog> selectQmsFlowLogList(QmsFlowLog qmsFlowLog)
    {
        return qmsFlowLogMapper.selectQmsFlowLogList(qmsFlowLog);
    }

    /**
     * 新增质量问题流程日志
     * 
     * @param qmsFlowLog 质量问题流程日志
     * @return 结果
     */
    @Override
    public int insertQmsFlowLog(QmsFlowLog qmsFlowLog)
    {
        qmsFlowLog.setCreateTime(DateUtils.getNowDate());
        return qmsFlowLogMapper.insertQmsFlowLog(qmsFlowLog);
    }

    /**
     * 修改质量问题流程日志
     * 
     * @param qmsFlowLog 质量问题流程日志
     * @return 结果
     */
    @Override
    public int updateQmsFlowLog(QmsFlowLog qmsFlowLog)
    {
        return qmsFlowLogMapper.updateQmsFlowLog(qmsFlowLog);
    }

    /**
     * 批量删除质量问题流程日志
     * 
     * @param logIds 需要删除的质量问题流程日志主键
     * @return 结果
     */
    @Override
    public int deleteQmsFlowLogByLogIds(Long[] logIds)
    {
        return qmsFlowLogMapper.deleteQmsFlowLogByLogIds(logIds);
    }

    /**
     * 删除质量问题流程日志信息
     * 
     * @param logId 质量问题流程日志主键
     * @return 结果
     */
    @Override
    public int deleteQmsFlowLogByLogId(Long logId)
    {
        return qmsFlowLogMapper.deleteQmsFlowLogByLogId(logId);
    }
}

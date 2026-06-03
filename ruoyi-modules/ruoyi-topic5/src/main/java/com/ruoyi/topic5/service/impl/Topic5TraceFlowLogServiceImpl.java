package com.ruoyi.topic5.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.Topic5TraceFlowLogMapper;
import com.ruoyi.topic5.domain.Topic5TraceFlowLog;
import com.ruoyi.topic5.service.ITopic5TraceFlowLogService;

/**
 * 追溯流程记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
@Service
public class Topic5TraceFlowLogServiceImpl implements ITopic5TraceFlowLogService 
{
    @Autowired
    private Topic5TraceFlowLogMapper topic5TraceFlowLogMapper;

    /**
     * 查询追溯流程记录
     * 
     * @param id 追溯流程记录主键
     * @return 追溯流程记录
     */
    @Override
    public Topic5TraceFlowLog selectTopic5TraceFlowLogById(Long id)
    {
        return topic5TraceFlowLogMapper.selectTopic5TraceFlowLogById(id);
    }

    /**
     * 查询追溯流程记录列表
     * 
     * @param topic5TraceFlowLog 追溯流程记录
     * @return 追溯流程记录
     */
    @Override
    public List<Topic5TraceFlowLog> selectTopic5TraceFlowLogList(Topic5TraceFlowLog topic5TraceFlowLog)
    {
        return topic5TraceFlowLogMapper.selectTopic5TraceFlowLogList(topic5TraceFlowLog);
    }

    /**
     * 新增追溯流程记录
     * 
     * @param topic5TraceFlowLog 追溯流程记录
     * @return 结果
     */
    @Override
    public int insertTopic5TraceFlowLog(Topic5TraceFlowLog topic5TraceFlowLog)
    {
        return topic5TraceFlowLogMapper.insertTopic5TraceFlowLog(topic5TraceFlowLog);
    }

    /**
     * 修改追溯流程记录
     * 
     * @param topic5TraceFlowLog 追溯流程记录
     * @return 结果
     */
    @Override
    public int updateTopic5TraceFlowLog(Topic5TraceFlowLog topic5TraceFlowLog)
    {
        return topic5TraceFlowLogMapper.updateTopic5TraceFlowLog(topic5TraceFlowLog);
    }

    /**
     * 批量删除追溯流程记录
     * 
     * @param ids 需要删除的追溯流程记录主键
     * @return 结果
     */
    @Override
    public int deleteTopic5TraceFlowLogByIds(Long[] ids)
    {
        return topic5TraceFlowLogMapper.deleteTopic5TraceFlowLogByIds(ids);
    }

    /**
     * 删除追溯流程记录信息
     * 
     * @param id 追溯流程记录主键
     * @return 结果
     */
    @Override
    public int deleteTopic5TraceFlowLogById(Long id)
    {
        return topic5TraceFlowLogMapper.deleteTopic5TraceFlowLogById(id);
    }
}

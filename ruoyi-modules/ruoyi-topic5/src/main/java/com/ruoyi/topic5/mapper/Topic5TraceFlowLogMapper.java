package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.Topic5TraceFlowLog;

/**
 * 追溯流程记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public interface Topic5TraceFlowLogMapper 
{
    /**
     * 查询追溯流程记录
     * 
     * @param id 追溯流程记录主键
     * @return 追溯流程记录
     */
    public Topic5TraceFlowLog selectTopic5TraceFlowLogById(Long id);

    /**
     * 查询追溯流程记录列表
     * 
     * @param topic5TraceFlowLog 追溯流程记录
     * @return 追溯流程记录集合
     */
    public List<Topic5TraceFlowLog> selectTopic5TraceFlowLogList(Topic5TraceFlowLog topic5TraceFlowLog);

    /**
     * 新增追溯流程记录
     * 
     * @param topic5TraceFlowLog 追溯流程记录
     * @return 结果
     */
    public int insertTopic5TraceFlowLog(Topic5TraceFlowLog topic5TraceFlowLog);

    /**
     * 修改追溯流程记录
     * 
     * @param topic5TraceFlowLog 追溯流程记录
     * @return 结果
     */
    public int updateTopic5TraceFlowLog(Topic5TraceFlowLog topic5TraceFlowLog);

    /**
     * 删除追溯流程记录
     * 
     * @param id 追溯流程记录主键
     * @return 结果
     */
    public int deleteTopic5TraceFlowLogById(Long id);

    /**
     * 批量删除追溯流程记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTopic5TraceFlowLogByIds(Long[] ids);
}

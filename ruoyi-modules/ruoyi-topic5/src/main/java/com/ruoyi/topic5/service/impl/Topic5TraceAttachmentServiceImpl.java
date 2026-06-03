package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.Topic5TraceAttachmentMapper;
import com.ruoyi.topic5.domain.Topic5TraceAttachment;
import com.ruoyi.topic5.service.ITopic5TraceAttachmentService;

/**
 * 追溯附件Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
@Service
public class Topic5TraceAttachmentServiceImpl implements ITopic5TraceAttachmentService 
{
    @Autowired
    private Topic5TraceAttachmentMapper topic5TraceAttachmentMapper;

    /**
     * 查询追溯附件
     * 
     * @param id 追溯附件主键
     * @return 追溯附件
     */
    @Override
    public Topic5TraceAttachment selectTopic5TraceAttachmentById(Long id)
    {
        return topic5TraceAttachmentMapper.selectTopic5TraceAttachmentById(id);
    }

    /**
     * 查询追溯附件列表
     * 
     * @param topic5TraceAttachment 追溯附件
     * @return 追溯附件
     */
    @Override
    public List<Topic5TraceAttachment> selectTopic5TraceAttachmentList(Topic5TraceAttachment topic5TraceAttachment)
    {
        return topic5TraceAttachmentMapper.selectTopic5TraceAttachmentList(topic5TraceAttachment);
    }

    /**
     * 新增追溯附件
     * 
     * @param topic5TraceAttachment 追溯附件
     * @return 结果
     */
    @Override
    public int insertTopic5TraceAttachment(Topic5TraceAttachment topic5TraceAttachment)
    {
        topic5TraceAttachment.setCreateTime(DateUtils.getNowDate());
        return topic5TraceAttachmentMapper.insertTopic5TraceAttachment(topic5TraceAttachment);
    }

    /**
     * 修改追溯附件
     * 
     * @param topic5TraceAttachment 追溯附件
     * @return 结果
     */
    @Override
    public int updateTopic5TraceAttachment(Topic5TraceAttachment topic5TraceAttachment)
    {
        return topic5TraceAttachmentMapper.updateTopic5TraceAttachment(topic5TraceAttachment);
    }

    /**
     * 批量删除追溯附件
     * 
     * @param ids 需要删除的追溯附件主键
     * @return 结果
     */
    @Override
    public int deleteTopic5TraceAttachmentByIds(Long[] ids)
    {
        return topic5TraceAttachmentMapper.deleteTopic5TraceAttachmentByIds(ids);
    }

    /**
     * 删除追溯附件信息
     * 
     * @param id 追溯附件主键
     * @return 结果
     */
    @Override
    public int deleteTopic5TraceAttachmentById(Long id)
    {
        return topic5TraceAttachmentMapper.deleteTopic5TraceAttachmentById(id);
    }
}

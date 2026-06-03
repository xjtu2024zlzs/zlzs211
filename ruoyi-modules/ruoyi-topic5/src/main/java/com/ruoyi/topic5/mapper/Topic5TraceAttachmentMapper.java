package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.Topic5TraceAttachment;

/**
 * 追溯附件Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public interface Topic5TraceAttachmentMapper 
{
    /**
     * 查询追溯附件
     * 
     * @param id 追溯附件主键
     * @return 追溯附件
     */
    public Topic5TraceAttachment selectTopic5TraceAttachmentById(Long id);

    /**
     * 查询追溯附件列表
     * 
     * @param topic5TraceAttachment 追溯附件
     * @return 追溯附件集合
     */
    public List<Topic5TraceAttachment> selectTopic5TraceAttachmentList(Topic5TraceAttachment topic5TraceAttachment);

    /**
     * 新增追溯附件
     * 
     * @param topic5TraceAttachment 追溯附件
     * @return 结果
     */
    public int insertTopic5TraceAttachment(Topic5TraceAttachment topic5TraceAttachment);

    /**
     * 修改追溯附件
     * 
     * @param topic5TraceAttachment 追溯附件
     * @return 结果
     */
    public int updateTopic5TraceAttachment(Topic5TraceAttachment topic5TraceAttachment);

    /**
     * 删除追溯附件
     * 
     * @param id 追溯附件主键
     * @return 结果
     */
    public int deleteTopic5TraceAttachmentById(Long id);

    /**
     * 批量删除追溯附件
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTopic5TraceAttachmentByIds(Long[] ids);
}

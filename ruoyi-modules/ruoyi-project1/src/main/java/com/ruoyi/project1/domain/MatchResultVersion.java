package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式映射结果版本对象 p1p_match_result_version
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class MatchResultVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结果版本主键 */
    private Long resultVersionId;

    /** 任务 ID */
    @Excel(name = "任务 ID")
    private Long taskId;

    /** 任务版本 ID */
    @Excel(name = "任务版本 ID")
    private Long versionId;

    /** 运行记录 ID */
    @Excel(name = "运行记录 ID")
    private Long recordId;

    /** 结果版本标签 */
    @Excel(name = "结果版本标签")
    private String versionLabel;

    public void setResultVersionId(Long resultVersionId) 
    {
        this.resultVersionId = resultVersionId;
    }

    public Long getResultVersionId() 
    {
        return resultVersionId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setVersionId(Long versionId) 
    {
        this.versionId = versionId;
    }

    public Long getVersionId() 
    {
        return versionId;
    }

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setVersionLabel(String versionLabel) 
    {
        this.versionLabel = versionLabel;
    }

    public String getVersionLabel() 
    {
        return versionLabel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("resultVersionId", getResultVersionId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("versionLabel", getVersionLabel())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}

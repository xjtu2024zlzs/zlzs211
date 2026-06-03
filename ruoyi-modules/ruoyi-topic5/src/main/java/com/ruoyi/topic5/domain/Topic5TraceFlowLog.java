package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 追溯流程记录对象 topic5_trace_flow_log
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public class Topic5TraceFlowLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 追溯任务ID */
    @Excel(name = "追溯任务ID")
    private Long traceId;

    /** 流程阶段编号 */
    @Excel(name = "流程阶段编号")
    private Long stageCode;

    /** 流程阶段名称 */
    @Excel(name = "流程阶段名称")
    private String stageName;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operateUser;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date operateTime;

    /** 操作结果 */
    @Excel(name = "操作结果")
    private String operateResult;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTraceId(Long traceId) 
    {
        this.traceId = traceId;
    }

    public Long getTraceId() 
    {
        return traceId;
    }

    public void setStageCode(Long stageCode) 
    {
        this.stageCode = stageCode;
    }

    public Long getStageCode() 
    {
        return stageCode;
    }

    public void setStageName(String stageName) 
    {
        this.stageName = stageName;
    }

    public String getStageName() 
    {
        return stageName;
    }

    public void setOperateUser(String operateUser) 
    {
        this.operateUser = operateUser;
    }

    public String getOperateUser() 
    {
        return operateUser;
    }

    public void setOperateTime(Date operateTime) 
    {
        this.operateTime = operateTime;
    }

    public Date getOperateTime() 
    {
        return operateTime;
    }

    public void setOperateResult(String operateResult) 
    {
        this.operateResult = operateResult;
    }

    public String getOperateResult() 
    {
        return operateResult;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("traceId", getTraceId())
            .append("stageCode", getStageCode())
            .append("stageName", getStageName())
            .append("operateUser", getOperateUser())
            .append("operateTime", getOperateTime())
            .append("operateResult", getOperateResult())
            .append("remark", getRemark())
            .toString();
    }
}

package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 追溯问题对象 topic5_trace_problem
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public class Topic5TraceProblem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 追溯任务编号 */
    @Excel(name = "追溯任务编号")
    private String traceNo;

    /** 问题发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "问题发生时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date eventTime;

    /** 架次编号 */
    @Excel(name = "架次编号")
    private String aircraftNo;

    /** 发生部位 */
    @Excel(name = "发生部位")
    private String partName;

    /** 部件编号 */
    @Excel(name = "部件编号")
    private String partCode;

    /** 具体发生位置 */
    @Excel(name = "具体发生位置")
    private String occurrencePosition;

    /** 问题类型 */
    @Excel(name = "问题类型")
    private String problemType;

    /** 严重程度 */
    @Excel(name = "严重程度")
    private String severityLevel;

    /** 问题描述 */
    @Excel(name = "问题描述")
    private String problemDescription;

    /** 流程阶段：1问题填报，2数据调用，3任务保存，4推送课题四，5课题四完成，6结果回填，7算法分析，8归档展示 */
    @Excel(name = "流程阶段：1问题填报，2数据调用，3任务保存，4推送课题四，5课题四完成，6结果回填，7算法分析，8归档展示")
    private Long workflowStage;

    /** 追溯任务状态：未处理、处理中、已完成 */
    @Excel(name = "追溯任务状态：未处理、处理中、已完成")
    private String status;

    /** 课题四状态：0未推送，1处理中，2已完成，3失败 */
    @Excel(name = "课题四状态：0未推送，1处理中，2已完成，3失败")
    private Long topic4Status;

    /** 课题四返回结果 */
    @Excel(name = "课题四返回结果")
    private String topic4Result;

    /** 课题四建议原因 */
    @Excel(name = "课题四建议原因")
    private String suggestedCause;

    /** 风险等级 */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 建议处理措施 */
    @Excel(name = "建议处理措施")
    private String suggestedAction;

    /** 课题四结果是否已回填：0否，1是 */
    @Excel(name = "课题四结果是否已回填：0否，1是")
    private Long topic4ResultFilled;

    /** 算法状态：0未运行，1运行中，2已完成，3失败 */
    @Excel(name = "算法状态：0未运行，1运行中，2已完成，3失败")
    private Long algorithmStatus;

    /** 算法追溯结果 */
    @Excel(name = "算法追溯结果")
    private String algorithmResult;

    /** 填报人 */
    @Excel(name = "填报人")
    private String reporter;

    /** 问题来源 */
    @Excel(name = "问题来源")
    private String source;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTraceNo(String traceNo) 
    {
        this.traceNo = traceNo;
    }

    public String getTraceNo() 
    {
        return traceNo;
    }

    public void setEventTime(Date eventTime) 
    {
        this.eventTime = eventTime;
    }

    public Date getEventTime() 
    {
        return eventTime;
    }

    public void setAircraftNo(String aircraftNo) 
    {
        this.aircraftNo = aircraftNo;
    }

    public String getAircraftNo() 
    {
        return aircraftNo;
    }

    public void setPartName(String partName) 
    {
        this.partName = partName;
    }

    public String getPartName() 
    {
        return partName;
    }

    public void setPartCode(String partCode) 
    {
        this.partCode = partCode;
    }

    public String getPartCode() 
    {
        return partCode;
    }

    public void setOccurrencePosition(String occurrencePosition) 
    {
        this.occurrencePosition = occurrencePosition;
    }

    public String getOccurrencePosition() 
    {
        return occurrencePosition;
    }

    public void setProblemType(String problemType) 
    {
        this.problemType = problemType;
    }

    public String getProblemType() 
    {
        return problemType;
    }

    public void setSeverityLevel(String severityLevel) 
    {
        this.severityLevel = severityLevel;
    }

    public String getSeverityLevel() 
    {
        return severityLevel;
    }

    public void setProblemDescription(String problemDescription) 
    {
        this.problemDescription = problemDescription;
    }

    public String getProblemDescription() 
    {
        return problemDescription;
    }

    public void setWorkflowStage(Long workflowStage) 
    {
        this.workflowStage = workflowStage;
    }

    public Long getWorkflowStage() 
    {
        return workflowStage;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setTopic4Status(Long topic4Status) 
    {
        this.topic4Status = topic4Status;
    }

    public Long getTopic4Status() 
    {
        return topic4Status;
    }

    public void setTopic4Result(String topic4Result) 
    {
        this.topic4Result = topic4Result;
    }

    public String getTopic4Result() 
    {
        return topic4Result;
    }

    public void setSuggestedCause(String suggestedCause) 
    {
        this.suggestedCause = suggestedCause;
    }

    public String getSuggestedCause() 
    {
        return suggestedCause;
    }

    public void setRiskLevel(String riskLevel) 
    {
        this.riskLevel = riskLevel;
    }

    public String getRiskLevel() 
    {
        return riskLevel;
    }

    public void setSuggestedAction(String suggestedAction) 
    {
        this.suggestedAction = suggestedAction;
    }

    public String getSuggestedAction() 
    {
        return suggestedAction;
    }

    public void setTopic4ResultFilled(Long topic4ResultFilled) 
    {
        this.topic4ResultFilled = topic4ResultFilled;
    }

    public Long getTopic4ResultFilled() 
    {
        return topic4ResultFilled;
    }

    public void setAlgorithmStatus(Long algorithmStatus) 
    {
        this.algorithmStatus = algorithmStatus;
    }

    public Long getAlgorithmStatus() 
    {
        return algorithmStatus;
    }

    public void setAlgorithmResult(String algorithmResult) 
    {
        this.algorithmResult = algorithmResult;
    }

    public String getAlgorithmResult() 
    {
        return algorithmResult;
    }

    public void setReporter(String reporter) 
    {
        this.reporter = reporter;
    }

    public String getReporter() 
    {
        return reporter;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("traceNo", getTraceNo())
            .append("eventTime", getEventTime())
            .append("aircraftNo", getAircraftNo())
            .append("partName", getPartName())
            .append("partCode", getPartCode())
            .append("occurrencePosition", getOccurrencePosition())
            .append("problemType", getProblemType())
            .append("severityLevel", getSeverityLevel())
            .append("problemDescription", getProblemDescription())
            .append("workflowStage", getWorkflowStage())
            .append("status", getStatus())
            .append("topic4Status", getTopic4Status())
            .append("topic4Result", getTopic4Result())
            .append("suggestedCause", getSuggestedCause())
            .append("riskLevel", getRiskLevel())
            .append("suggestedAction", getSuggestedAction())
            .append("topic4ResultFilled", getTopic4ResultFilled())
            .append("algorithmStatus", getAlgorithmStatus())
            .append("algorithmResult", getAlgorithmResult())
            .append("reporter", getReporter())
            .append("source", getSource())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
                .append("kgRebuildStatus", getKgRebuildStatus())
                .append("kgGraphUrl", getKgGraphUrl())
                .append("traceReportStatus", getTraceReportStatus())
                .append("traceReportUrl", getTraceReportUrl())
                .append("attachmentSavePath", getAttachmentSavePath())
            .toString();
    }
    /** 知识图谱重构状态：0未生成，1已生成 */
    private Long kgRebuildStatus;

    /** 知识图谱可视化页面或文件地址 */
    private String kgGraphUrl;

    /** 溯源报告状态：0未生成，1已生成 */
    private Long traceReportStatus;

    /** 溯源结果Word报告地址 */
    private String traceReportUrl;


    public Long getKgRebuildStatus()
    {
        return kgRebuildStatus;
    }

    public void setKgRebuildStatus(Long kgRebuildStatus)
    {
        this.kgRebuildStatus = kgRebuildStatus;
    }

    public String getKgGraphUrl()
    {
        return kgGraphUrl;
    }

    public void setKgGraphUrl(String kgGraphUrl)
    {
        this.kgGraphUrl = kgGraphUrl;
    }

    public Long getTraceReportStatus()
    {
        return traceReportStatus;
    }

    public void setTraceReportStatus(Long traceReportStatus)
    {
        this.traceReportStatus = traceReportStatus;
    }

    public String getTraceReportUrl()
    {
        return traceReportUrl;
    }

    public void setTraceReportUrl(String traceReportUrl)
    {
        this.traceReportUrl = traceReportUrl;
    }
    /** 附件保存位置 */
    @Excel(name = "附件保存位置")
    private String attachmentSavePath;
    public String getAttachmentSavePath()
    {
        return attachmentSavePath;
    }

    public void setAttachmentSavePath(String attachmentSavePath)
    {
        this.attachmentSavePath = attachmentSavePath;
    }
}

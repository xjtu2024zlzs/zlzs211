package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-追溯任务主对象 t5_trace_task
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5TraceTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 追溯任务ID */
    private Long taskId;

    /** 追溯任务编号 */
    @Excel(name = "追溯任务编号")
    private String taskNo;

    /** 追溯任务名称 */
    @Excel(name = "追溯任务名称")
    private String taskName;

    /** 关联故障表征ID */
    @Excel(name = "关联故障表征ID")
    private Long symptomId;

    /** 任务状态：0待分析，1分析中，2已完成，3失败 */
    @Excel(name = "任务状态：0待分析，1分析中，2已完成，3失败")
    private String taskStatus;

    /** 优先级：1低，2中，3高 */
    @Excel(name = "优先级：1低，2中，3高")
    private String priorityLevel;

    /** 追溯目标 */
    @Excel(name = "追溯目标")
    private String traceGoal;

    /** 输入信息摘要 */
    @Excel(name = "输入信息摘要")
    private String inputSummary;

    /** 最终定位零部件ID */
    @Excel(name = "最终定位零部件ID")
    private Long finalPartId;

    /** 最终定位零件编号 */
    @Excel(name = "最终定位零件编号")
    private String finalPartNo;

    /** 最终定位零件名称 */
    @Excel(name = "最终定位零件名称")
    private String finalPartName;

    /** 最终故障阶段编码 */
    @Excel(name = "最终故障阶段编码")
    private String finalStageCode;

    /** 最终故障阶段名称 */
    @Excel(name = "最终故障阶段名称")
    private String finalStageName;

    /** 最终置信度 */
    @Excel(name = "最终置信度")
    private BigDecimal finalConfidence;

    /** 追溯结果摘要 */
    @Excel(name = "追溯结果摘要")
    private String resultSummary;

    /** 任务开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "任务开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 任务结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "任务结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setTaskNo(String taskNo) 
    {
        this.taskNo = taskNo;
    }

    public String getTaskNo() 
    {
        return taskNo;
    }

    public void setTaskName(String taskName) 
    {
        this.taskName = taskName;
    }

    public String getTaskName() 
    {
        return taskName;
    }

    public void setSymptomId(Long symptomId) 
    {
        this.symptomId = symptomId;
    }

    public Long getSymptomId() 
    {
        return symptomId;
    }

    public void setTaskStatus(String taskStatus) 
    {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatus() 
    {
        return taskStatus;
    }

    public void setPriorityLevel(String priorityLevel) 
    {
        this.priorityLevel = priorityLevel;
    }

    public String getPriorityLevel() 
    {
        return priorityLevel;
    }

    public void setTraceGoal(String traceGoal) 
    {
        this.traceGoal = traceGoal;
    }

    public String getTraceGoal() 
    {
        return traceGoal;
    }

    public void setInputSummary(String inputSummary) 
    {
        this.inputSummary = inputSummary;
    }

    public String getInputSummary() 
    {
        return inputSummary;
    }

    public void setFinalPartId(Long finalPartId) 
    {
        this.finalPartId = finalPartId;
    }

    public Long getFinalPartId() 
    {
        return finalPartId;
    }

    public void setFinalPartNo(String finalPartNo) 
    {
        this.finalPartNo = finalPartNo;
    }

    public String getFinalPartNo() 
    {
        return finalPartNo;
    }

    public void setFinalPartName(String finalPartName) 
    {
        this.finalPartName = finalPartName;
    }

    public String getFinalPartName() 
    {
        return finalPartName;
    }

    public void setFinalStageCode(String finalStageCode) 
    {
        this.finalStageCode = finalStageCode;
    }

    public String getFinalStageCode() 
    {
        return finalStageCode;
    }

    public void setFinalStageName(String finalStageName) 
    {
        this.finalStageName = finalStageName;
    }

    public String getFinalStageName() 
    {
        return finalStageName;
    }

    public void setFinalConfidence(BigDecimal finalConfidence) 
    {
        this.finalConfidence = finalConfidence;
    }

    public BigDecimal getFinalConfidence() 
    {
        return finalConfidence;
    }

    public void setResultSummary(String resultSummary) 
    {
        this.resultSummary = resultSummary;
    }

    public String getResultSummary() 
    {
        return resultSummary;
    }

    public void setStartTime(Date startTime) 
    {
        this.startTime = startTime;
    }

    public Date getStartTime() 
    {
        return startTime;
    }

    public void setEndTime(Date endTime) 
    {
        this.endTime = endTime;
    }

    public Date getEndTime() 
    {
        return endTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskNo", getTaskNo())
            .append("taskName", getTaskName())
            .append("symptomId", getSymptomId())
            .append("taskStatus", getTaskStatus())
            .append("priorityLevel", getPriorityLevel())
            .append("traceGoal", getTraceGoal())
            .append("inputSummary", getInputSummary())
            .append("finalPartId", getFinalPartId())
            .append("finalPartNo", getFinalPartNo())
            .append("finalPartName", getFinalPartName())
            .append("finalStageCode", getFinalStageCode())
            .append("finalStageName", getFinalStageName())
            .append("finalConfidence", getFinalConfidence())
            .append("resultSummary", getResultSummary())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

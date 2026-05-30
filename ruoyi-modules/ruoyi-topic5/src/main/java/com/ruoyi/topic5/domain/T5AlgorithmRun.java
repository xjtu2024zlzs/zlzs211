package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-算法运行记录对象 t5_algorithm_run
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5AlgorithmRun extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 算法运行ID */
    private Long runId;

    /** 追溯任务ID */
    @Excel(name = "追溯任务ID")
    private Long taskId;

    /** 算法类型：rule/kg/transe/gnn/hybrid */
    @Excel(name = "算法类型：rule/kg/transe/gnn/hybrid")
    private String algorithmType;

    /** 算法名称 */
    @Excel(name = "算法名称")
    private String algorithmName;

    /** 模型版本 */
    @Excel(name = "模型版本")
    private String modelVersion;

    /** 算法输入数据JSON */
    @Excel(name = "算法输入数据JSON")
    private String inputData;

    /** 算法输出数据JSON */
    @Excel(name = "算法输出数据JSON")
    private String outputData;

    /** 运行状态：0待运行，1运行中，2成功，3失败 */
    @Excel(name = "运行状态：0待运行，1运行中，2成功，3失败")
    private String runStatus;

    /** 错误信息 */
    @Excel(name = "错误信息")
    private String errorMessage;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;

    /** 耗时ms */
    @Excel(name = "耗时ms")
    private Long costTimeMs;

    public void setRunId(Long runId) 
    {
        this.runId = runId;
    }

    public Long getRunId() 
    {
        return runId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setAlgorithmType(String algorithmType) 
    {
        this.algorithmType = algorithmType;
    }

    public String getAlgorithmType() 
    {
        return algorithmType;
    }

    public void setAlgorithmName(String algorithmName) 
    {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() 
    {
        return algorithmName;
    }

    public void setModelVersion(String modelVersion) 
    {
        this.modelVersion = modelVersion;
    }

    public String getModelVersion() 
    {
        return modelVersion;
    }

    public void setInputData(String inputData) 
    {
        this.inputData = inputData;
    }

    public String getInputData() 
    {
        return inputData;
    }

    public void setOutputData(String outputData) 
    {
        this.outputData = outputData;
    }

    public String getOutputData() 
    {
        return outputData;
    }

    public void setRunStatus(String runStatus) 
    {
        this.runStatus = runStatus;
    }

    public String getRunStatus() 
    {
        return runStatus;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
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

    public void setCostTimeMs(Long costTimeMs) 
    {
        this.costTimeMs = costTimeMs;
    }

    public Long getCostTimeMs() 
    {
        return costTimeMs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("runId", getRunId())
            .append("taskId", getTaskId())
            .append("algorithmType", getAlgorithmType())
            .append("algorithmName", getAlgorithmName())
            .append("modelVersion", getModelVersion())
            .append("inputData", getInputData())
            .append("outputData", getOutputData())
            .append("runStatus", getRunStatus())
            .append("errorMessage", getErrorMessage())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("costTimeMs", getCostTimeMs())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

package com.ruoyi.quality.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 质量问题模块处理任务对象 qms_quality_task
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public class QmsQualityTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 质量问题ID */
    @Excel(name = "质量问题ID")
    private Long problemId;

    /** 质量问题编号 */
    @Excel(name = "质量问题编号")
    private String problemCode;

    /** 处理模块编码 */
    @Excel(name = "处理模块编码")
    private String moduleCode;

    /** 处理模块名称 */
    @Excel(name = "处理模块名称")
    private String moduleName;

    /** 任务状态：PROCESSING处理中 SUBMITTED待确认 CONFIRMED已确认 */
    @Excel(name = "任务状态：PROCESSING处理中 SUBMITTED待确认 CONFIRMED已确认")
    private String taskStatus;

    /** 分派说明 */
    @Excel(name = "分派说明")
    private String dispatchOpinion;

    /** 处理结果 */
    @Excel(name = "处理结果")
    private String processResult;

    /** 处理附件 */
    @Excel(name = "处理附件")
    private String processFile;

    /** 分派人ID */
    @Excel(name = "分派人ID")
    private Long dispatchUserId;

    /** 分派人姓名 */
    @Excel(name = "分派人姓名")
    private String dispatchUserName;

    /** 分派时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "分派时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dispatchTime;

    /** 提交人ID */
    @Excel(name = "提交人ID")
    private Long submitUserId;

    /** 提交人姓名 */
    @Excel(name = "提交人姓名")
    private String submitUserName;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date submitTime;

    /** 确认人ID */
    @Excel(name = "确认人ID")
    private Long confirmUserId;

    /** 确认人姓名 */
    @Excel(name = "确认人姓名")
    private String confirmUserName;

    /** 确认意见 */
    @Excel(name = "确认意见")
    private String confirmOpinion;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "确认时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date confirmTime;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setProblemId(Long problemId) 
    {
        this.problemId = problemId;
    }

    public Long getProblemId() 
    {
        return problemId;
    }

    public void setProblemCode(String problemCode) 
    {
        this.problemCode = problemCode;
    }

    public String getProblemCode() 
    {
        return problemCode;
    }

    public void setModuleCode(String moduleCode) 
    {
        this.moduleCode = moduleCode;
    }

    public String getModuleCode() 
    {
        return moduleCode;
    }

    public void setModuleName(String moduleName) 
    {
        this.moduleName = moduleName;
    }

    public String getModuleName() 
    {
        return moduleName;
    }

    public void setTaskStatus(String taskStatus) 
    {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatus() 
    {
        return taskStatus;
    }

    public void setDispatchOpinion(String dispatchOpinion) 
    {
        this.dispatchOpinion = dispatchOpinion;
    }

    public String getDispatchOpinion() 
    {
        return dispatchOpinion;
    }

    public void setProcessResult(String processResult) 
    {
        this.processResult = processResult;
    }

    public String getProcessResult() 
    {
        return processResult;
    }

    public void setProcessFile(String processFile) 
    {
        this.processFile = processFile;
    }

    public String getProcessFile() 
    {
        return processFile;
    }

    public void setDispatchUserId(Long dispatchUserId) 
    {
        this.dispatchUserId = dispatchUserId;
    }

    public Long getDispatchUserId() 
    {
        return dispatchUserId;
    }

    public void setDispatchUserName(String dispatchUserName) 
    {
        this.dispatchUserName = dispatchUserName;
    }

    public String getDispatchUserName() 
    {
        return dispatchUserName;
    }

    public void setDispatchTime(Date dispatchTime) 
    {
        this.dispatchTime = dispatchTime;
    }

    public Date getDispatchTime() 
    {
        return dispatchTime;
    }

    public void setSubmitUserId(Long submitUserId) 
    {
        this.submitUserId = submitUserId;
    }

    public Long getSubmitUserId() 
    {
        return submitUserId;
    }

    public void setSubmitUserName(String submitUserName) 
    {
        this.submitUserName = submitUserName;
    }

    public String getSubmitUserName() 
    {
        return submitUserName;
    }

    public void setSubmitTime(Date submitTime) 
    {
        this.submitTime = submitTime;
    }

    public Date getSubmitTime() 
    {
        return submitTime;
    }

    public void setConfirmUserId(Long confirmUserId) 
    {
        this.confirmUserId = confirmUserId;
    }

    public Long getConfirmUserId() 
    {
        return confirmUserId;
    }

    public void setConfirmUserName(String confirmUserName) 
    {
        this.confirmUserName = confirmUserName;
    }

    public String getConfirmUserName() 
    {
        return confirmUserName;
    }

    public void setConfirmOpinion(String confirmOpinion) 
    {
        this.confirmOpinion = confirmOpinion;
    }

    public String getConfirmOpinion() 
    {
        return confirmOpinion;
    }

    public void setConfirmTime(Date confirmTime) 
    {
        this.confirmTime = confirmTime;
    }

    public Date getConfirmTime() 
    {
        return confirmTime;
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
            .append("taskId", getTaskId())
            .append("problemId", getProblemId())
            .append("problemCode", getProblemCode())
            .append("moduleCode", getModuleCode())
            .append("moduleName", getModuleName())
            .append("taskStatus", getTaskStatus())
            .append("dispatchOpinion", getDispatchOpinion())
            .append("processResult", getProcessResult())
            .append("processFile", getProcessFile())
            .append("dispatchUserId", getDispatchUserId())
            .append("dispatchUserName", getDispatchUserName())
            .append("dispatchTime", getDispatchTime())
            .append("submitUserId", getSubmitUserId())
            .append("submitUserName", getSubmitUserName())
            .append("submitTime", getSubmitTime())
            .append("confirmUserId", getConfirmUserId())
            .append("confirmUserName", getConfirmUserName())
            .append("confirmOpinion", getConfirmOpinion())
            .append("confirmTime", getConfirmTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}

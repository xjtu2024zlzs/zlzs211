package com.ruoyi.qms.api.domain;

import java.io.Serializable;

/**
 * 模块任务处理结果提交对象
 */
public class QualityTaskSubmitDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 质量问题ID */
    private Long problemId;

    /** 质量问题编号 */
    private String problemCode;

    /** 模块编码 */
    private String moduleCode;

    /** 模块名称 */
    private String moduleName;

    /** 处理结果摘要 */
    private String processResult;

    /** 处理文件路径 */
    private String processFile;

    /** 提交用户ID */
    private Long submitUserId;

    /** 提交用户名称 */
    private String submitUserName;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getProblemId()
    {
        return problemId;
    }

    public void setProblemId(Long problemId)
    {
        this.problemId = problemId;
    }

    public String getProblemCode()
    {
        return problemCode;
    }

    public void setProblemCode(String problemCode)
    {
        this.problemCode = problemCode;
    }

    public String getModuleCode()
    {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode)
    {
        this.moduleCode = moduleCode;
    }

    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    public String getProcessResult()
    {
        return processResult;
    }

    public void setProcessResult(String processResult)
    {
        this.processResult = processResult;
    }

    public String getProcessFile()
    {
        return processFile;
    }

    public void setProcessFile(String processFile)
    {
        this.processFile = processFile;
    }

    public Long getSubmitUserId()
    {
        return submitUserId;
    }

    public void setSubmitUserId(Long submitUserId)
    {
        this.submitUserId = submitUserId;
    }

    public String getSubmitUserName()
    {
        return submitUserName;
    }

    public void setSubmitUserName(String submitUserName)
    {
        this.submitUserName = submitUserName;
    }
}
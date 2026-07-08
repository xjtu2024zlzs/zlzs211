package com.ruoyi.qms.api.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 质量任务远程传输对象
 */
public class QualityTaskDto implements Serializable
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

    /** 任务状态 */
    private String taskStatus;

    /** 分派意见 */
    private String dispatchOpinion;

    /** 模块处理结果 */
    private String processResult;

    /** 模块处理文件 */
    private String processFile;

    /** 分派时间 */
    private Date dispatchTime;

    /** 提交时间 */
    private Date submitTime;

    /** 确认时间 */
    private Date confirmTime;

    /** 问题标题 */
    private String problemTitle;

    /** 问题描述 */
    private String problemDescription;

    /** 产品型号 */
    private String productModel;

    /** 涉及系统 */
    private String involvedSystem;

    /** 严重程度 */
    private String severity;

    /** 发生时间 */
    private Date occurTime;

    private String reporter;

    /** 质量问题状态 */
    private String problemStatus;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getProblemCode() {
        return problemCode;
    }

    public void setProblemCode(String problemCode) {
        this.problemCode = problemCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getReporter()
    {
        return reporter;
    }

    public void setReporter(String reporter)
    {
        this.reporter = reporter;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDispatchOpinion() {
        return dispatchOpinion;
    }

    public void setDispatchOpinion(String dispatchOpinion) {
        this.dispatchOpinion = dispatchOpinion;
    }

    public String getProcessResult() {
        return processResult;
    }

    public void setProcessResult(String processResult) {
        this.processResult = processResult;
    }

    public String getProcessFile() {
        return processFile;
    }

    public void setProcessFile(String processFile) {
        this.processFile = processFile;
    }

    public Date getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(Date dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public void setProblemTitle(String problemTitle) {
        this.problemTitle = problemTitle;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getProductModel() {
        return productModel;
    }

    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }

    public String getInvolvedSystem() {
        return involvedSystem;
    }

    public void setInvolvedSystem(String involvedSystem) {
        this.involvedSystem = involvedSystem;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }

    public String getProblemStatus() {
        return problemStatus;
    }

    public void setProblemStatus(String problemStatus) {
        this.problemStatus = problemStatus;
    }
}
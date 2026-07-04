package com.ruoyi.qms.api.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 质量问题远程调用DTO
 */
public class QualityProblemDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long problemId;

    private String problemCode;

    private String problemTitle;

    private String problemDescription;

    private String productModel;

    private String relatedSystem;

    private String severityLevel;

    private String problemStatus;

    private Date occurTime;

    private String reporter;

    private String moduleCode;

    private String moduleName;

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

    public String getProblemTitle()
    {
        return problemTitle;
    }

    public void setProblemTitle(String problemTitle)
    {
        this.problemTitle = problemTitle;
    }

    public String getProblemDescription()
    {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription)
    {
        this.problemDescription = problemDescription;
    }

    public String getProductModel()
    {
        return productModel;
    }
    public String getReporter()
    {
        return reporter;
    }

    public void setReporter(String reporter)
    {
        this.reporter = reporter;
    }
    public void setProductModel(String productModel)
    {
        this.productModel = productModel;
    }

    public String getRelatedSystem()
    {
        return relatedSystem;
    }

    public void setRelatedSystem(String relatedSystem)
    {
        this.relatedSystem = relatedSystem;
    }

    public String getSeverityLevel()
    {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel)
    {
        this.severityLevel = severityLevel;
    }

    public String getProblemStatus()
    {
        return problemStatus;
    }

    public void setProblemStatus(String problemStatus)
    {
        this.problemStatus = problemStatus;
    }

    public Date getOccurTime()
    {
        return occurTime;
    }

    public void setOccurTime(Date occurTime)
    {
        this.occurTime = occurTime;
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
}
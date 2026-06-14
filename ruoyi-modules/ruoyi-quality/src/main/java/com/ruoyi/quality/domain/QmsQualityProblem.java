package com.ruoyi.quality.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 质量问题对象 qms_quality_problem
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public class QmsQualityProblem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 质量问题ID */
    private Long problemId;

    /** 质量问题编号 */
    @Excel(name = "质量问题编号")
    private String problemCode;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String title;

    /** 发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发生时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date occurTime;

    /** 产品型号 */
    @Excel(name = "产品型号")
    private String productModel;

    /** 涉及系统 */
    @Excel(name = "涉及系统")
    private String involvedSystem;

    /** 发生部位 */
    @Excel(name = "发生部位")
    private String occurPart;

    /** 部件编号 */
    @Excel(name = "部件编号")
    private String componentCode;

    /** 严重程度 */
    @Excel(name = "严重程度")
    private String severity;

    /** 问题来源 */
    @Excel(name = "问题来源")
    private String source;

    /** 填报人员 */
    @Excel(name = "填报人员")
    private String reporter;

    /** 问题描述 */
    @Excel(name = "问题描述")
    private String description;

    /** 影响范围 */
    @Excel(name = "影响范围")
    private String influenceScope;

    /** 问题状态：DISPATCHING待处理 PROCESSING处理中 WAIT_CONFIRM待确认 FINISHED已结束 */
    @Excel(name = "问题状态：DISPATCHING待处理 PROCESSING处理中 WAIT_CONFIRM待确认 FINISHED已结束")
    private String status;

    /** 当前处理模块编码 */
    @Excel(name = "当前处理模块编码")
    private String currentModuleCode;

    /** 当前处理模块名称 */
    @Excel(name = "当前处理模块名称")
    private String currentModuleName;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

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

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setOccurTime(Date occurTime) 
    {
        this.occurTime = occurTime;
    }

    public Date getOccurTime() 
    {
        return occurTime;
    }

    public void setProductModel(String productModel) 
    {
        this.productModel = productModel;
    }

    public String getProductModel() 
    {
        return productModel;
    }

    public void setInvolvedSystem(String involvedSystem) 
    {
        this.involvedSystem = involvedSystem;
    }

    public String getInvolvedSystem() 
    {
        return involvedSystem;
    }

    public void setOccurPart(String occurPart) 
    {
        this.occurPart = occurPart;
    }

    public String getOccurPart() 
    {
        return occurPart;
    }

    public void setComponentCode(String componentCode) 
    {
        this.componentCode = componentCode;
    }

    public String getComponentCode() 
    {
        return componentCode;
    }

    public void setSeverity(String severity) 
    {
        this.severity = severity;
    }

    public String getSeverity() 
    {
        return severity;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    public void setReporter(String reporter) 
    {
        this.reporter = reporter;
    }

    public String getReporter() 
    {
        return reporter;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setInfluenceScope(String influenceScope) 
    {
        this.influenceScope = influenceScope;
    }

    public String getInfluenceScope() 
    {
        return influenceScope;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setCurrentModuleCode(String currentModuleCode) 
    {
        this.currentModuleCode = currentModuleCode;
    }

    public String getCurrentModuleCode() 
    {
        return currentModuleCode;
    }

    public void setCurrentModuleName(String currentModuleName) 
    {
        this.currentModuleName = currentModuleName;
    }

    public String getCurrentModuleName() 
    {
        return currentModuleName;
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
            .append("problemId", getProblemId())
            .append("problemCode", getProblemCode())
            .append("title", getTitle())
            .append("occurTime", getOccurTime())
            .append("productModel", getProductModel())
            .append("involvedSystem", getInvolvedSystem())
            .append("occurPart", getOccurPart())
            .append("componentCode", getComponentCode())
            .append("severity", getSeverity())
            .append("source", getSource())
            .append("reporter", getReporter())
            .append("description", getDescription())
            .append("influenceScope", getInfluenceScope())
            .append("status", getStatus())
            .append("currentModuleCode", getCurrentModuleCode())
            .append("currentModuleName", getCurrentModuleName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}

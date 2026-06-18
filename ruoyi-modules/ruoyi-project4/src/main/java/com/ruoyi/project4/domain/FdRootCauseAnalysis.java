package com.ruoyi.project4.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 故障诊断-根因分析结果对象 fd_root_cause_analysis
 *
 * 该实体面向“课题四 → 课题五”的根因分析结果输出接口，
 * 核心输出字段包括：根因判断、证据链、根因分析置信度。
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public class FdRootCauseAnalysis extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 根因分析ID */
    private Long analysisId;

    /** 根因分析编号 */
    @Excel(name = "根因分析编号")
    private String analysisCode;

    /** 诊断ID */
    @Excel(name = "诊断ID")
    private Long diagnosisId;

    /** 样本ID */
    @Excel(name = "样本ID")
    private Long sampleId;

    /** 根因判断-类型 */
    @Excel(name = "根因判断-类型")
    private String rootCauseType;

    /** 根因判断-描述 */
    @Excel(name = "根因判断-描述")
    private String rootCauseDesc;

    /** 根因分析置信度 */
    @Excel(name = "根因分析置信度")
    private BigDecimal probability;

    /** 证据链 */
    @Excel(name = "证据链")
    private String evidenceJson;

    /** 整改建议 */
    @Excel(name = "整改建议")
    private String maintenanceSuggestion;

    /** 分析方法 */
    @Excel(name = "分析方法")
    private String analysisMethod;

    /** 分析状态 */
    @Excel(name = "分析状态")
    private String analysisStatus;

    /** 分析人 */
    @Excel(name = "分析人")
    private String analyst;

    /** 分析时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "分析时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date analysisTime;

    /** 删除标志 */
    private String delFlag;

    public void setAnalysisId(Long analysisId)
    {
        this.analysisId = analysisId;
    }

    public Long getAnalysisId()
    {
        return analysisId;
    }

    public void setAnalysisCode(String analysisCode)
    {
        this.analysisCode = analysisCode;
    }

    public String getAnalysisCode()
    {
        return analysisCode;
    }

    public void setDiagnosisId(Long diagnosisId)
    {
        this.diagnosisId = diagnosisId;
    }

    public Long getDiagnosisId()
    {
        return diagnosisId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setRootCauseType(String rootCauseType)
    {
        this.rootCauseType = rootCauseType;
    }

    public String getRootCauseType()
    {
        return rootCauseType;
    }

    public void setRootCauseDesc(String rootCauseDesc)
    {
        this.rootCauseDesc = rootCauseDesc;
    }

    public String getRootCauseDesc()
    {
        return rootCauseDesc;
    }

    public void setProbability(BigDecimal probability)
    {
        this.probability = probability;
    }

    public BigDecimal getProbability()
    {
        return probability;
    }

    public void setEvidenceJson(String evidenceJson)
    {
        this.evidenceJson = evidenceJson;
    }

    public String getEvidenceJson()
    {
        return evidenceJson;
    }

    public void setMaintenanceSuggestion(String maintenanceSuggestion)
    {
        this.maintenanceSuggestion = maintenanceSuggestion;
    }

    public String getMaintenanceSuggestion()
    {
        return maintenanceSuggestion;
    }

    public void setAnalysisMethod(String analysisMethod)
    {
        this.analysisMethod = analysisMethod;
    }

    public String getAnalysisMethod()
    {
        return analysisMethod;
    }

    public void setAnalysisStatus(String analysisStatus)
    {
        this.analysisStatus = analysisStatus;
    }

    public String getAnalysisStatus()
    {
        return analysisStatus;
    }

    public void setAnalyst(String analyst)
    {
        this.analyst = analyst;
    }

    public String getAnalyst()
    {
        return analyst;
    }

    public void setAnalysisTime(Date analysisTime)
    {
        this.analysisTime = analysisTime;
    }

    public Date getAnalysisTime()
    {
        return analysisTime;
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
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("analysisId", getAnalysisId())
                .append("analysisCode", getAnalysisCode())
                .append("diagnosisId", getDiagnosisId())
                .append("sampleId", getSampleId())
                .append("rootCauseType", getRootCauseType())
                .append("rootCauseDesc", getRootCauseDesc())
                .append("probability", getProbability())
                .append("evidenceJson", getEvidenceJson())
                .append("maintenanceSuggestion", getMaintenanceSuggestion())
                .append("analysisMethod", getAnalysisMethod())
                .append("analysisStatus", getAnalysisStatus())
                .append("analyst", getAnalyst())
                .append("analysisTime", getAnalysisTime())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
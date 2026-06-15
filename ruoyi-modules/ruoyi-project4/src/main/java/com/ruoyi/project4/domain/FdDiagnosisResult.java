package com.ruoyi.project4.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 故障诊断-诊断结果对象 fd_diagnosis_result
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public class FdDiagnosisResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 诊断ID */
    private Long diagnosisId;

    /** 诊断编号 */
    @Excel(name = "诊断编号")
    private String diagnosisCode;

    /** 样本ID */
    @Excel(name = "样本ID")
    private Long sampleId;

    /** 样本编号 */
    @Excel(name = "样本编号")
    private String sampleCode;

    /** 融合结果ID */
    @Excel(name = "融合结果ID")
    private Long fusionId;

    /** 模型ID */
    @Excel(name = "模型ID")
    private Long modelId;

    /** 模型名称 */
    @Excel(name = "模型名称")
    private String modelName;

    /** 故障类型 */
    @Excel(name = "故障类型")
    private String faultType;

    /** 故障位置 */
    @Excel(name = "故障位置")
    private String faultLocation;

    /** 置信度 */
    @Excel(name = "置信度")
    private BigDecimal confidence;

    /** 诊断阈值 */
    @Excel(name = "诊断阈值")
    private BigDecimal thresholdValue;

    /** 健康评分 */
    @Excel(name = "健康评分")
    private BigDecimal healthScore;

    /** 告警等级：正常/一般/严重/危险 */
    @Excel(name = "告警等级")
    private String alarmLevel;

    /** 诊断时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "诊断时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date diagnosisTime;

    /** 根因状态：待根因分析/已完成根因分析 */
    @Excel(name = "根因状态")
    private String rootStatus;

    /** 诊断结果JSON字符串 */
    @Excel(name = "诊断结果JSON字符串")
    private String resultJson;

    /** 处置建议 */
    @Excel(name = "处置建议")
    private String advice;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public Long getDiagnosisId()
    {
        return diagnosisId;
    }

    public void setDiagnosisId(Long diagnosisId)
    {
        this.diagnosisId = diagnosisId;
    }

    public String getDiagnosisCode()
    {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode)
    {
        this.diagnosisCode = diagnosisCode;
    }

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public Long getFusionId()
    {
        return fusionId;
    }

    public void setFusionId(Long fusionId)
    {
        this.fusionId = fusionId;
    }

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getFaultType()
    {
        return faultType;
    }

    public void setFaultType(String faultType)
    {
        this.faultType = faultType;
    }

    public String getFaultLocation()
    {
        return faultLocation;
    }

    public void setFaultLocation(String faultLocation)
    {
        this.faultLocation = faultLocation;
    }

    public BigDecimal getConfidence()
    {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence)
    {
        this.confidence = confidence;
    }

    public BigDecimal getThresholdValue()
    {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue)
    {
        this.thresholdValue = thresholdValue;
    }

    public BigDecimal getHealthScore()
    {
        return healthScore;
    }

    public void setHealthScore(BigDecimal healthScore)
    {
        this.healthScore = healthScore;
    }

    public String getAlarmLevel()
    {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel)
    {
        this.alarmLevel = alarmLevel;
    }

    public Date getDiagnosisTime()
    {
        return diagnosisTime;
    }

    public void setDiagnosisTime(Date diagnosisTime)
    {
        this.diagnosisTime = diagnosisTime;
    }

    public String getRootStatus()
    {
        return rootStatus;
    }

    public void setRootStatus(String rootStatus)
    {
        this.rootStatus = rootStatus;
    }

    public String getResultJson()
    {
        return resultJson;
    }

    public void setResultJson(String resultJson)
    {
        this.resultJson = resultJson;
    }

    public String getAdvice()
    {
        return advice;
    }

    public void setAdvice(String advice)
    {
        this.advice = advice;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("diagnosisId", getDiagnosisId())
                .append("diagnosisCode", getDiagnosisCode())
                .append("sampleId", getSampleId())
                .append("sampleCode", getSampleCode())
                .append("fusionId", getFusionId())
                .append("modelId", getModelId())
                .append("modelName", getModelName())
                .append("faultType", getFaultType())
                .append("faultLocation", getFaultLocation())
                .append("confidence", getConfidence())
                .append("thresholdValue", getThresholdValue())
                .append("healthScore", getHealthScore())
                .append("alarmLevel", getAlarmLevel())
                .append("diagnosisTime", getDiagnosisTime())
                .append("rootStatus", getRootStatus())
                .append("resultJson", getResultJson())
                .append("advice", getAdvice())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}